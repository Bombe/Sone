/*
 * Sone - SoneDownloader.java - Copyright © 2010–2013 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.sone.core;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.core.FreenetInterface.Fetched;
import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.Client;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.Sone.SoneStatus;
import net.pterodactylus.sone.database.PostBuilder;
import net.pterodactylus.sone.database.PostReplyBuilder;
import net.pterodactylus.util.io.Closer;
import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.number.Numbers;
import net.pterodactylus.util.service.AbstractService;
import net.pterodactylus.util.xml.SimpleXML;
import net.pterodactylus.util.xml.XML;

import org.w3c.dom.Document;

import freenet.client.FetchResult;
import freenet.keys.FreenetURI;
import freenet.support.api.Bucket;

/**
 * The Sone downloader is responsible for download Sones as they are updated.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneDownloader extends AbstractService {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(SoneDownloader.class);

	/** The maximum protocol version. */
	private static final int MAX_PROTOCOL_VERSION = 0;

	/** The core. */
	private final Core core;

	/** The Freenet interface. */
	private final FreenetInterface freenetInterface;

	/** The sones to update. */
	private final Set<Sone> sones = new HashSet<Sone>();

	/**
	 * Creates a new Sone downloader.
	 *
	 * @param core
	 *            The core
	 * @param freenetInterface
	 *            The Freenet interface
	 */
	public SoneDownloader(Core core, FreenetInterface freenetInterface) {
		super("Sone Downloader", false);
		this.core = core;
		this.freenetInterface = freenetInterface;
	}

	//
	// ACTIONS
	//

	/**
	 * Adds the given Sone to the set of Sones that will be watched for updates.
	 *
	 * @param sone
	 *            The Sone to add
	 */
	public void addSone(Sone sone) {
		if (!sones.add(sone)) {
			freenetInterface.unregisterUsk(sone);
		}
		freenetInterface.registerUsk(sone, this);
	}

	/**
	 * Removes the given Sone from the downloader.
	 *
	 * @param sone
	 *            The Sone to stop watching
	 */
	public void removeSone(Sone sone) {
		if (sones.remove(sone)) {
			freenetInterface.unregisterUsk(sone);
		}
	}

	/**
	 * Fetches the updated Sone. This method is a callback method for
	 * {@link FreenetInterface#registerUsk(Sone, SoneDownloader)}.
	 *
	 * @param sone
	 *            The Sone to fetch
	 */
	public void fetchSone(Sone sone) {
		fetchSone(sone, sone.getRequestUri().sskForUSK());
	}

	/**
	 * Fetches the updated Sone. This method can be used to fetch a Sone from a
	 * specific URI.
	 *
	 * @param sone
	 *            The Sone to fetch
	 * @param soneUri
	 *            The URI to fetch the Sone from
	 */
	public void fetchSone(Sone sone, FreenetURI soneUri) {
		fetchSone(sone, soneUri, false);
	}

	/**
	 * Fetches the Sone from the given URI.
	 *
	 * @param sone
	 *            The Sone to fetch
	 * @param soneUri
	 *            The URI of the Sone to fetch
	 * @param fetchOnly
	 *            {@code true} to only fetch and parse the Sone, {@code false}
	 *            to {@link Core#updateSone(Sone) update} it in the core
	 * @return The downloaded Sone, or {@code null} if the Sone could not be
	 *         downloaded
	 */
	public Sone fetchSone(Sone sone, FreenetURI soneUri, boolean fetchOnly) {
		logger.log(Level.FINE, String.format("Starting fetch for Sone “%s” from %s…", sone, soneUri));
		FreenetURI requestUri = soneUri.setMetaString(new String[] { "sone.xml" });
		sone.setStatus(SoneStatus.downloading);
		try {
			Fetched fetchResults = freenetInterface.fetchUri(requestUri);
			if (fetchResults == null) {
				/* TODO - mark Sone as bad. */
				return null;
			}
			logger.log(Level.FINEST, String.format("Got %d bytes back.", fetchResults.getFetchResult().size()));
			Sone parsedSone = parseSone(sone, fetchResults.getFetchResult(), fetchResults.getFreenetUri());
			if (parsedSone != null) {
				if (!fetchOnly) {
					parsedSone.setStatus((parsedSone.getTime() == 0) ? SoneStatus.unknown : SoneStatus.idle);
					core.updateSone(parsedSone);
					addSone(parsedSone);
				}
			}
			return parsedSone;
		} finally {
			sone.setStatus((sone.getTime() == 0) ? SoneStatus.unknown : SoneStatus.idle);
		}
	}

	/**
	 * Parses a Sone from a fetch result.
	 *
	 * @param originalSone
	 *            The sone to parse, or {@code null} if the Sone is yet unknown
	 * @param fetchResult
	 *            The fetch result
	 * @param requestUri
	 *            The requested URI
	 * @return The parsed Sone, or {@code null} if the Sone could not be parsed
	 */
	public Sone parseSone(Sone originalSone, FetchResult fetchResult, FreenetURI requestUri) {
		logger.log(Level.FINEST, String.format("Parsing FetchResult (%d bytes, %s) for %s…", fetchResult.size(), fetchResult.getMimeType(), originalSone));
		Bucket soneBucket = fetchResult.asBucket();
		InputStream soneInputStream = null;
		try {
			soneInputStream = soneBucket.getInputStream();
			Sone parsedSone = parseSone(originalSone, soneInputStream);
			if (parsedSone != null) {
				parsedSone.setLatestEdition(requestUri.getEdition());
				if (requestUri.getKeyType().equals("USK")) {
					parsedSone.setRequestUri(requestUri.setMetaString(new String[0]));
				} else {
					parsedSone.setRequestUri(requestUri.setKeyType("USK").setDocName("Sone").setMetaString(new String[0]));
				}
			}
			return parsedSone;
		} catch (Exception e1) {
			logger.log(Level.WARNING, String.format("Could not parse Sone from %s!", requestUri), e1);
		} finally {
			Closer.close(soneInputStream);
			soneBucket.free();
		}
		return null;
	}

	/**
	 * Parses a Sone from the given input stream and creates a new Sone from the
	 * parsed data.
	 *
	 * @param originalSone
	 *            The Sone to update
	 * @param soneInputStream
	 *            The input stream to parse the Sone from
	 * @return The parsed Sone
	 * @throws SoneException
	 *             if a parse error occurs, or the protocol is invalid
	 */
	public Sone parseSone(Sone originalSone, InputStream soneInputStream) throws SoneException {
		/* TODO - impose a size limit? */

		Document document;
		/* XML parsing is not thread-safe. */
		synchronized (this) {
			document = XML.transformToDocument(soneInputStream);
		}
		if (document == null) {
			/* TODO - mark Sone as bad. */
			logger.log(Level.WARNING, String.format("Could not parse XML for Sone %s!", originalSone));
			return null;
		}

		Sone sone = new Sone(originalSone.getId(), originalSone.isLocal()).setIdentity(originalSone.getIdentity());

		SimpleXML soneXml;
		try {
			soneXml = SimpleXML.fromDocument(document);
		} catch (NullPointerException npe1) {
			/* for some reason, invalid XML can cause NPEs. */
			logger.log(Level.WARNING, String.format("XML for Sone %s can not be parsed!", sone), npe1);
			return null;
		}

		Integer protocolVersion = null;
		String soneProtocolVersion = soneXml.getValue("protocol-version", null);
		if (soneProtocolVersion != null) {
			protocolVersion = Numbers.safeParseInteger(soneProtocolVersion);
		}
		if (protocolVersion == null) {
			logger.log(Level.INFO, "No protocol version found, assuming 0.");
			protocolVersion = 0;
		}

		if (protocolVersion < 0) {
			logger.log(Level.WARNING, String.format("Invalid protocol version: %d! Not parsing Sone.", protocolVersion));
			return null;
		}

		/* check for valid versions. */
		if (protocolVersion > MAX_PROTOCOL_VERSION) {
			logger.log(Level.WARNING, String.format("Unknown protocol version: %d! Not parsing Sone.", protocolVersion));
			return null;
		}

		String soneTime = soneXml.getValue("time", null);
		if (soneTime == null) {
			/* TODO - mark Sone as bad. */
			logger.log(Level.WARNING, String.format("Downloaded time for Sone %s was null!", sone));
			return null;
		}
		try {
			sone.setTime(Long.parseLong(soneTime));
		} catch (NumberFormatException nfe1) {
			/* TODO - mark Sone as bad. */
			logger.log(Level.WARNING, String.format("Downloaded Sone %s with invalid time: %s", sone, soneTime));
			return null;
		}

		SimpleXML clientXml = soneXml.getNode("client");
		if (clientXml != null) {
			String clientName = clientXml.getValue("name", null);
			String clientVersion = clientXml.getValue("version", null);
			if ((clientName == null) || (clientVersion == null)) {
				logger.log(Level.WARNING, String.format("Download Sone %s with client XML but missing name or version!", sone));
				return null;
			}
			sone.setClient(new Client(clientName, clientVersion));
		}

		String soneRequestUri = soneXml.getValue("request-uri", null);
		if (soneRequestUri != null) {
			try {
				sone.setRequestUri(new FreenetURI(soneRequestUri));
			} catch (MalformedURLException mue1) {
				/* TODO - mark Sone as bad. */
				logger.log(Level.WARNING, String.format("Downloaded Sone %s has invalid request URI: %s", sone, soneRequestUri), mue1);
				return null;
			}
		}

		String soneInsertUri = soneXml.getValue("insert-uri", null);
		if ((soneInsertUri != null) && (sone.getInsertUri() == null)) {
			try {
				sone.setInsertUri(new FreenetURI(soneInsertUri));
				sone.setLatestEdition(Math.max(sone.getRequestUri().getEdition(), sone.getInsertUri().getEdition()));
			} catch (MalformedURLException mue1) {
				/* TODO - mark Sone as bad. */
				logger.log(Level.WARNING, String.format("Downloaded Sone %s has invalid insert URI: %s", sone, soneInsertUri), mue1);
				return null;
			}
		}

		SimpleXML profileXml = soneXml.getNode("profile");
		if (profileXml == null) {
			/* TODO - mark Sone as bad. */
			logger.log(Level.WARNING, String.format("Downloaded Sone %s has no profile!", sone));
			return null;
		}

		/* parse profile. */
		String profileFirstName = profileXml.getValue("first-name", null);
		String profileMiddleName = profileXml.getValue("middle-name", null);
		String profileLastName = profileXml.getValue("last-name", null);
		Integer profileBirthDay = Numbers.safeParseInteger(profileXml.getValue("birth-day", null));
		Integer profileBirthMonth = Numbers.safeParseInteger(profileXml.getValue("birth-month", null));
		Integer profileBirthYear = Numbers.safeParseInteger(profileXml.getValue("birth-year", null));
		Profile profile = new Profile(sone).setFirstName(profileFirstName).setMiddleName(profileMiddleName).setLastName(profileLastName);
		profile.setBirthDay(profileBirthDay).setBirthMonth(profileBirthMonth).setBirthYear(profileBirthYear);
		/* avatar is processed after images are loaded. */
		String avatarId = profileXml.getValue("avatar", null);

		/* parse profile fields. */
		SimpleXML profileFieldsXml = profileXml.getNode("fields");
		if (profileFieldsXml != null) {
			for (SimpleXML fieldXml : profileFieldsXml.getNodes("field")) {
				String fieldName = fieldXml.getValue("field-name", null);
				String fieldValue = fieldXml.getValue("field-value", "");
				if (fieldName == null) {
					logger.log(Level.WARNING, String.format("Downloaded profile field for Sone %s with missing data! Name: %s, Value: %s", sone, fieldName, fieldValue));
					return null;
				}
				try {
					profile.addField(fieldName).setValue(fieldValue);
				} catch (IllegalArgumentException iae1) {
					logger.log(Level.WARNING, String.format("Duplicate field: %s", fieldName), iae1);
					return null;
				}
			}
		}

		/* parse posts. */
		SimpleXML postsXml = soneXml.getNode("posts");
		Set<Post> posts = new HashSet<Post>();
		if (postsXml == null) {
			/* TODO - mark Sone as bad. */
			logger.log(Level.WARNING, String.format("Downloaded Sone %s has no posts!", sone));
		} else {
			for (SimpleXML postXml : postsXml.getNodes("post")) {
				String postId = postXml.getValue("id", null);
				String postRecipientId = postXml.getValue("recipient", null);
				String postTime = postXml.getValue("time", null);
				String postText = postXml.getValue("text", null);
				if ((postId == null) || (postTime == null) || (postText == null)) {
					/* TODO - mark Sone as bad. */
					logger.log(Level.WARNING, String.format("Downloaded post for Sone %s with missing data! ID: %s, Time: %s, Text: %s", sone, postId, postTime, postText));
					return null;
				}
				try {
					PostBuilder postBuilder = core.postBuilder();
					/* TODO - parse time correctly. */
					postBuilder.withId(postId).from(sone.getId()).withTime(Long.parseLong(postTime)).withText(postText);
					if ((postRecipientId != null) && (postRecipientId.length() == 43)) {
						postBuilder.to(postRecipientId);
					}
					posts.add(postBuilder.build());
				} catch (NumberFormatException nfe1) {
					/* TODO - mark Sone as bad. */
					logger.log(Level.WARNING, String.format("Downloaded post for Sone %s with invalid time: %s", sone, postTime));
					return null;
				}
			}
		}

		/* parse replies. */
		SimpleXML repliesXml = soneXml.getNode("replies");
		Set<PostReply> replies = new HashSet<PostReply>();
		if (repliesXml == null) {
			/* TODO - mark Sone as bad. */
			logger.log(Level.WARNING, String.format("Downloaded Sone %s has no replies!", sone));
		} else {
			for (SimpleXML replyXml : repliesXml.getNodes("reply")) {
				String replyId = replyXml.getValue("id", null);
				String replyPostId = replyXml.getValue("post-id", null);
				String replyTime = replyXml.getValue("time", null);
				String replyText = replyXml.getValue("text", null);
				if ((replyId == null) || (replyPostId == null) || (replyTime == null) || (replyText == null)) {
					/* TODO - mark Sone as bad. */
					logger.log(Level.WARNING, String.format("Downloaded reply for Sone %s with missing data! ID: %s, Post: %s, Time: %s, Text: %s", sone, replyId, replyPostId, replyTime, replyText));
					return null;
				}
				try {
					PostReplyBuilder postReplyBuilder = core.postReplyBuilder();
					/* TODO - parse time correctly. */
					postReplyBuilder.withId(replyId).from(sone.getId()).to(replyPostId).withTime(Long.parseLong(replyTime)).withText(replyText);
					replies.add(postReplyBuilder.build());
				} catch (NumberFormatException nfe1) {
					/* TODO - mark Sone as bad. */
					logger.log(Level.WARNING, String.format("Downloaded reply for Sone %s with invalid time: %s", sone, replyTime));
					return null;
				}
			}
		}

		/* parse liked post IDs. */
		SimpleXML likePostIdsXml = soneXml.getNode("post-likes");
		Set<String> likedPostIds = new HashSet<String>();
		if (likePostIdsXml == null) {
			/* TODO - mark Sone as bad. */
			logger.log(Level.WARNING, String.format("Downloaded Sone %s has no post likes!", sone));
		} else {
			for (SimpleXML likedPostIdXml : likePostIdsXml.getNodes("post-like")) {
				String postId = likedPostIdXml.getValue();
				likedPostIds.add(postId);
			}
		}

		/* parse liked reply IDs. */
		SimpleXML likeReplyIdsXml = soneXml.getNode("reply-likes");
		Set<String> likedReplyIds = new HashSet<String>();
		if (likeReplyIdsXml == null) {
			/* TODO - mark Sone as bad. */
			logger.log(Level.WARNING, String.format("Downloaded Sone %s has no reply likes!", sone));
		} else {
			for (SimpleXML likedReplyIdXml : likeReplyIdsXml.getNodes("reply-like")) {
				String replyId = likedReplyIdXml.getValue();
				likedReplyIds.add(replyId);
			}
		}

		/* parse albums. */
		SimpleXML albumsXml = soneXml.getNode("albums");
		List<Album> topLevelAlbums = new ArrayList<Album>();
		if (albumsXml != null) {
			for (SimpleXML albumXml : albumsXml.getNodes("album")) {
				String id = albumXml.getValue("id", null);
				String parentId = albumXml.getValue("parent", null);
				String title = albumXml.getValue("title", null);
				String description = albumXml.getValue("description", "");
				String albumImageId = albumXml.getValue("album-image", null);
				if ((id == null) || (title == null) || (description == null)) {
					logger.log(Level.WARNING, String.format("Downloaded Sone %s contains invalid album!", sone));
					return null;
				}
				Album parent = null;
				if (parentId != null) {
					parent = core.getAlbum(parentId, false);
					if (parent == null) {
						logger.log(Level.WARNING, String.format("Downloaded Sone %s has album with invalid parent!", sone));
						return null;
					}
				}
				Album album = core.getAlbum(id).setSone(sone).modify().setTitle(title).setDescription(description).update();
				if (parent != null) {
					parent.addAlbum(album);
				} else {
					topLevelAlbums.add(album);
				}
				SimpleXML imagesXml = albumXml.getNode("images");
				if (imagesXml != null) {
					for (SimpleXML imageXml : imagesXml.getNodes("image")) {
						String imageId = imageXml.getValue("id", null);
						String imageCreationTimeString = imageXml.getValue("creation-time", null);
						String imageKey = imageXml.getValue("key", null);
						String imageTitle = imageXml.getValue("title", null);
						String imageDescription = imageXml.getValue("description", "");
						String imageWidthString = imageXml.getValue("width", null);
						String imageHeightString = imageXml.getValue("height", null);
						if ((imageId == null) || (imageCreationTimeString == null) || (imageKey == null) || (imageTitle == null) || (imageWidthString == null) || (imageHeightString == null)) {
							logger.log(Level.WARNING, String.format("Downloaded Sone %s contains invalid images!", sone));
							return null;
						}
						long creationTime = Numbers.safeParseLong(imageCreationTimeString, 0L);
						int imageWidth = Numbers.safeParseInteger(imageWidthString, 0);
						int imageHeight = Numbers.safeParseInteger(imageHeightString, 0);
						if ((imageWidth < 1) || (imageHeight < 1)) {
							logger.log(Level.WARNING, String.format("Downloaded Sone %s contains image %s with invalid dimensions (%s, %s)!", sone, imageId, imageWidthString, imageHeightString));
							return null;
						}
						Image image = core.getImage(imageId).modify().setSone(sone).setKey(imageKey).setCreationTime(creationTime).update();
						image = image.modify().setTitle(imageTitle).setDescription(imageDescription).update();
						image = image.modify().setWidth(imageWidth).setHeight(imageHeight).update();
						album.addImage(image);
					}
				}
				album.modify().setAlbumImage(albumImageId).update();
			}
		}

		/* process avatar. */
		if (avatarId != null) {
			profile.setAvatar(core.getImage(avatarId, false));
		}

		/* okay, apparently everything was parsed correctly. Now import. */
		/* atomic setter operation on the Sone. */
		synchronized (sone) {
			sone.setProfile(profile);
			sone.setPosts(posts);
			sone.setReplies(replies);
			sone.setLikePostIds(likedPostIds);
			sone.setLikeReplyIds(likedReplyIds);
			for (Album album : topLevelAlbums) {
				sone.getRootAlbum().addAlbum(album);
			}
		}

		return sone;
	}

	//
	// SERVICE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void serviceStop() {
		for (Sone sone : sones) {
			freenetInterface.unregisterUsk(sone);
		}
	}

}
