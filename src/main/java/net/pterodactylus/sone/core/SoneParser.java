package net.pterodactylus.sone.core;

import static java.util.logging.Logger.getLogger;
import static net.pterodactylus.sone.utils.NumberParsers.parseInt;
import static net.pterodactylus.sone.utils.NumberParsers.parseLong;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.Client;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Profile.DuplicateField;
import net.pterodactylus.sone.data.Profile.EmptyFieldName;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.database.PostBuilder;
import net.pterodactylus.sone.database.PostReplyBuilder;
import net.pterodactylus.sone.database.SoneBuilder;
import net.pterodactylus.util.xml.SimpleXML;
import net.pterodactylus.util.xml.XML;

import org.w3c.dom.Document;

/**
 * Parses a {@link Sone} from an XML {@link InputStream}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneParser {

	private static final Logger logger = getLogger(SoneParser.class.getName());
	private static final int MAX_PROTOCOL_VERSION = 0;
	private final Core core;

	public SoneParser(Core core) {
		this.core = core;
	}

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

		SoneBuilder soneBuilder = core.soneBuilder().from(originalSone.getIdentity());
		if (originalSone.isLocal()) {
			soneBuilder = soneBuilder.local();
		}
		Sone sone = soneBuilder.build();

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
			protocolVersion = parseInt(soneProtocolVersion, null);
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
		Integer profileBirthDay = parseInt(profileXml.getValue("birth-day", ""), null);
		Integer profileBirthMonth = parseInt(profileXml.getValue("birth-month", ""), null);
		Integer profileBirthYear = parseInt(profileXml.getValue("birth-year", ""), null);
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
					profile.addField(fieldName.trim()).setValue(fieldValue);
				} catch (EmptyFieldName efn1) {
					logger.log(Level.WARNING, "Empty field name!", efn1);
					return null;
				} catch (DuplicateField df1) {
					logger.log(Level.WARNING, String.format("Duplicate field: %s", fieldName), df1);
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
		Map<String, Image> allImages = new HashMap<String, Image>();
		List<Album> topLevelAlbums = new ArrayList<Album>();
		if (albumsXml != null) {
			for (SimpleXML albumXml : albumsXml.getNodes("album")) {
				String id = albumXml.getValue("id", null);
				String parentId = albumXml.getValue("parent", null);
				String title = albumXml.getValue("title", null);
				String description = albumXml.getValue("description", "");
				String albumImageId = albumXml.getValue("album-image", null);
				if ((id == null) || (title == null)) {
					logger.log(Level.WARNING, String.format("Downloaded Sone %s contains invalid album!", sone));
					return null;
				}
				Album parent = null;
				if (parentId != null) {
					parent = core.getAlbum(parentId);
					if (parent == null) {
						logger.log(Level.WARNING, String.format("Downloaded Sone %s has album with invalid parent!", sone));
						return null;
					}
				}
				Album album = core.albumBuilder()
						.withId(id)
						.by(sone)
						.build()
						.modify()
						.setTitle(title)
						.setDescription(description)
						.update();
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
						long creationTime = parseLong(imageCreationTimeString, 0L);
						int imageWidth = parseInt(imageWidthString, 0);
						int imageHeight = parseInt(imageHeightString, 0);
						if ((imageWidth < 1) || (imageHeight < 1)) {
							logger.log(Level.WARNING, String.format("Downloaded Sone %s contains image %s with invalid dimensions (%s, %s)!", sone, imageId, imageWidthString, imageHeightString));
							return null;
						}
						Image image = core.imageBuilder().withId(imageId).build().modify().setSone(sone).setKey(imageKey).setCreationTime(creationTime).update();
						image = image.modify().setTitle(imageTitle).setDescription(imageDescription).update();
						image = image.modify().setWidth(imageWidth).setHeight(imageHeight).update();
						album.addImage(image);
						allImages.put(imageId, image);
					}
				}
				album.modify().setAlbumImage(albumImageId).update();
			}
		}

		/* process avatar. */
		if (avatarId != null) {
			profile.setAvatar(allImages.get(avatarId));
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

}
