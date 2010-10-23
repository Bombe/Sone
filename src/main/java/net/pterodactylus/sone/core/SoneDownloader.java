/*
 * Sone - SoneDownloader.java - Copyright © 2010 David Roden
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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.core.Core.SoneStatus;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.util.io.Closer;
import net.pterodactylus.util.logging.Logging;
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
		if (sones.add(sone)) {
			freenetInterface.registerUsk(sone, this);
		}
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
		if (core.getSoneStatus(sone) == SoneStatus.downloading) {
			return;
		}
		logger.log(Level.FINE, "Starting fetch for Sone “%s” from %s…", new Object[] { sone, sone.getRequestUri().setMetaString(new String[] { "sone.xml" }) });
		FreenetURI requestUri = sone.getRequestUri().setMetaString(new String[] { "sone.xml" });
		core.setSoneStatus(sone, SoneStatus.downloading);
		try {
			FetchResult fetchResult = freenetInterface.fetchUri(requestUri);
			if (fetchResult == null) {
				/* TODO - mark Sone as bad. */
				return;
			}
			logger.log(Level.FINEST, "Got %d bytes back.", fetchResult.size());
			Sone parsedSone = parseSone(sone, fetchResult, requestUri);
			if (parsedSone != null) {
				core.addSone(parsedSone);
			}
		} finally {
			core.setSoneStatus(sone, (sone.getTime() == 0) ? SoneStatus.unknown : SoneStatus.idle);
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
		logger.log(Level.FINEST, "Persing FetchResult (%d bytes, %s) for %s…", new Object[] { fetchResult.size(), fetchResult.getMimeType(), originalSone });
		/* TODO - impose a size limit? */
		InputStream xmlInputStream = null;
		Bucket xmlBucket = null;
		Sone sone;
		try {
			xmlBucket = fetchResult.asBucket();
			xmlInputStream = xmlBucket.getInputStream();
			Document document;
			/* XML parsing is not thread-safe. */
			synchronized (this) {
				document = XML.transformToDocument(xmlInputStream);
			}
			if (document == null) {
				/* TODO - mark Sone as bad. */
				logger.log(Level.WARNING, "Could not parse XML for Sone %s at %s!", new Object[] { originalSone, requestUri });
				return null;
			}
			SimpleXML soneXml;
			try {
				soneXml = SimpleXML.fromDocument(document);
			} catch (NullPointerException npe1) {
				/* for some reason, invalid XML can cause NPEs. */
				logger.log(Level.WARNING, "XML for Sone " + originalSone + " can not be parsed!", npe1);
				return null;
			}

			/* check ID. */
			String soneId = soneXml.getValue("id", null);
			if ((originalSone != null) && !originalSone.getId().equals(soneId)) {
				/* TODO - mark Sone as bad. */
				logger.log(Level.WARNING, "Downloaded ID for Sone %s (%s) does not match known ID (%s)!", new Object[] { originalSone, originalSone.getId(), soneId });
				return null;
			}

			/* load Sone from core. */
			sone = originalSone;
			if (sone == null) {
				sone = core.getSone(soneId).setRequestUri(requestUri.setMetaString(new String[] {}));
			}

			String soneName = soneXml.getValue("name", null);
			if (soneName == null) {
				/* TODO - mark Sone as bad. */
				logger.log(Level.WARNING, "Downloaded name for Sone %s was null!", new Object[] { sone });
				return null;
			}
			sone.setName(soneName);

			String soneTime = soneXml.getValue("time", null);
			if (soneTime == null) {
				/* TODO - mark Sone as bad. */
				logger.log(Level.WARNING, "Downloaded time for Sone %s was null!", new Object[] { sone });
				return null;
			}
			try {
				sone.setTime(Long.parseLong(soneTime));
			} catch (NumberFormatException nfe1) {
				/* TODO - mark Sone as bad. */
				logger.log(Level.WARNING, "Downloaded Sone %s with invalid time: %s", new Object[] { sone, soneTime });
				return null;
			}

			SimpleXML profileXml = soneXml.getNode("profile");
			if (profileXml == null) {
				/* TODO - mark Sone as bad. */
				logger.log(Level.WARNING, "Downloaded Sone %s has no profile!", new Object[] { sone });
				return null;
			}

			/* parse profile. */
			String profileFirstName = profileXml.getValue("first-name", null);
			String profileMiddleName = profileXml.getValue("middle-name", null);
			String profileLastName = profileXml.getValue("last-name", null);
			Profile profile = new Profile().setFirstName(profileFirstName).setMiddleName(profileMiddleName).setLastName(profileLastName);

			/* parse posts. */
			SimpleXML postsXml = soneXml.getNode("posts");
			if (postsXml == null) {
				/* TODO - mark Sone as bad. */
				logger.log(Level.WARNING, "Downloaded Sone %s has no posts!", new Object[] { sone });
				return null;
			}

			Set<Post> posts = new HashSet<Post>();
			for (SimpleXML postXml : postsXml.getNodes("post")) {
				String postId = postXml.getValue("id", null);
				String postTime = postXml.getValue("time", null);
				String postText = postXml.getValue("text", null);
				if ((postId == null) || (postTime == null) || (postText == null)) {
					/* TODO - mark Sone as bad. */
					logger.log(Level.WARNING, "Downloaded post for Sone %s with missing data! ID: %s, Time: %s, Text: %s", new Object[] { sone, postId, postTime, postText });
					return null;
				}
				try {
					posts.add(core.getPost(postId).setSone(sone).setTime(Long.parseLong(postTime)).setText(postText));
				} catch (NumberFormatException nfe1) {
					/* TODO - mark Sone as bad. */
					logger.log(Level.WARNING, "Downloaded post for Sone %s with invalid time: %s", new Object[] { sone, postTime });
					return null;
				}
			}

			/* parse replies. */
			SimpleXML repliesXml = soneXml.getNode("replies");
			if (repliesXml == null) {
				/* TODO - mark Sone as bad. */
				logger.log(Level.WARNING, "Downloaded Sone %s has no replies!", new Object[] { sone });
				return null;
			}

			Set<Reply> replies = new HashSet<Reply>();
			for (SimpleXML replyXml : repliesXml.getNodes("reply")) {
				String replyId = replyXml.getValue("id", null);
				String replyPostId = replyXml.getValue("post-id", null);
				String replyTime = replyXml.getValue("time", null);
				String replyText = replyXml.getValue("text", null);
				if ((replyId == null) || (replyPostId == null) || (replyTime == null) || (replyText == null)) {
					/* TODO - mark Sone as bad. */
					logger.log(Level.WARNING, "Downloaded reply for Sone %s with missing data! ID: %s, Post: %s, Time: %s, Text: %s", new Object[] { sone, replyId, replyPostId, replyTime, replyText });
					return null;
				}
				try {
					replies.add(core.getReply(replyId).setSone(sone).setPost(core.getPost(replyPostId)).setTime(Long.parseLong(replyTime)).setText(replyText));
				} catch (NumberFormatException nfe1) {
					/* TODO - mark Sone as bad. */
					logger.log(Level.WARNING, "Downloaded reply for Sone %s with invalid time: %s", new Object[] { sone, replyTime });
					return null;
				}
			}

			/* parse liked post IDs. */
			SimpleXML likePostIdsXml = soneXml.getNode("post-likes");
			if (likePostIdsXml == null) {
				/* TODO - mark Sone as bad. */
				logger.log(Level.WARNING, "Downloaded Sone %s has no known Sones!", new Object[] { sone });
				return null;
			}

			Set<String> likedPostIds = new HashSet<String>();
			for (SimpleXML likedPostIdXml : likePostIdsXml.getNodes("post-like")) {
				String postId = likedPostIdXml.getValue();
				likedPostIds.add(postId);
			}

			/* parse known Sones. */
			SimpleXML knownSonesXml = soneXml.getNode("known-sones");
			if (knownSonesXml == null) {
				/* TODO - mark Sone as bad. */
				logger.log(Level.WARNING, "Downloaded Sone %s has no known Sones!", new Object[] { sone });
				return null;
			}

			Set<Sone> knownSones = new HashSet<Sone>();
			for (SimpleXML knownSoneXml : knownSonesXml.getNodes("known-sone")) {
				String knownSoneId = knownSoneXml.getValue("sone-id", null);
				String knownSoneKey = knownSoneXml.getValue("sone-key", null);
				String knownSoneName = knownSoneXml.getValue("sone-name", null);
				if ((knownSoneId == null) || (knownSoneKey == null) || (knownSoneName == null)) {
					/* TODO - mark Sone as bad. */
					logger.log(Level.WARNING, "Downloaded known Sone for Sone %s with missing data! ID: %s, Key: %s, Name: %s", new Object[] { sone, knownSoneId, knownSoneKey, knownSoneName });
					return null;
				}
				try {
					knownSones.add(core.getSone(knownSoneId).setRequestUri(new FreenetURI(knownSoneKey)).setName(knownSoneName));
				} catch (MalformedURLException mue1) {
					/* TODO - mark Sone as bad. */
					logger.log(Level.WARNING, "Downloaded known Sone for Sone %s with invalid key: %s", new Object[] { sone, knownSoneKey });
					return null;
				}
			}

			/* okay, apparently everything was parsed correctly. Now import. */
			/* atomic setter operation on the Sone. */
			synchronized (sone) {
				sone.setProfile(profile);
				sone.setPosts(posts);
				sone.setReplies(replies);
				sone.setLikePostIds(likedPostIds);
				sone.setModificationCounter(0);
			}

			/* add all known Sones to core for downloading. */
			for (Sone knownSone : knownSones) {
				core.addSone(knownSone);
			}

		} catch (IOException ioe1) {
			logger.log(Level.WARNING, "Could not read XML file from " + originalSone + "!", ioe1);
			return null;
		} finally {
			if (xmlBucket != null) {
				xmlBucket.free();
			}
			Closer.close(xmlInputStream);
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
