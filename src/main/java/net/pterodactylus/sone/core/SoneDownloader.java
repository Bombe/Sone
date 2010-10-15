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
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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
		super("Sone Downloader");
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
	 * Fetches the updated Sone. This method is a callback method for
	 * {@link FreenetInterface#registerUsk(Sone, SoneDownloader)}.
	 *
	 * @param sone
	 *            The Sone to fetch
	 */
	public void fetchSone(Sone sone) {
		logger.log(Level.FINE, "Starting fetch for Sone “%s” from %s…", new Object[] { sone, sone.getRequestUri().setMetaString(new String[] { "sone.xml" }) });
		FetchResult fetchResult = freenetInterface.fetchUri(sone.getRequestUri().setMetaString(new String[] { "sone.xml" }));
		logger.log(Level.FINEST, "Got %d bytes back.", fetchResult.size());
		updateSoneFromXml(sone, fetchResult);
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

	//
	// PRIVATE METHODS
	//

	/**
	 * Updates the contents of the given Sone from the given fetch result.
	 *
	 * @param sone
	 *            The Sone to update
	 * @param fetchResult
	 *            The fetch result
	 */
	private void updateSoneFromXml(Sone sone, FetchResult fetchResult) {
		logger.log(Level.FINEST, "Persing FetchResult (%d bytes, %s) for %s…", new Object[] { fetchResult.size(), fetchResult.getMimeType(), sone });
		/* TODO - impose a size limit? */
		InputStream xmlInputStream = null;
		Bucket xmlBucket = null;
		try {
			xmlBucket = fetchResult.asBucket();
			xmlInputStream = xmlBucket.getInputStream();
			Document document = XML.transformToDocument(xmlInputStream);
			SimpleXML soneXml = SimpleXML.fromDocument(document);

			/* check ID. */
			String soneId = soneXml.getValue("id", null);
			if (!sone.getId().equals(soneId)) {
				/* TODO - mark Sone as bad. */
				logger.log(Level.WARNING, "Downloaded ID for Sone %s (%s) does not match known ID (%s)!", new Object[] { sone, sone.getId(), soneId });
				return;
			}

			String soneName = soneXml.getValue("name", null);
			if (soneName == null) {
				/* TODO - mark Sone as bad. */
				logger.log(Level.WARNING, "Downloaded name for Sone %s was null!", new Object[] { sone });
				return;
			}

			SimpleXML profileXml = soneXml.getNode("profile");
			if (profileXml == null) {
				/* TODO - mark Sone as bad. */
				logger.log(Level.WARNING, "Downloaded Sone %s has no profile!", new Object[] { sone });
				return;
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
				return;
			}

			Set<Post> posts = new HashSet<Post>();
			for (SimpleXML postXml : postsXml.getNodes("post")) {
				String postId = postXml.getValue("id", null);
				String postTime = postXml.getValue("time", null);
				String postText = postXml.getValue("text", null);
				if ((postId == null) || (postTime == null) || (postText == null)) {
					/* TODO - mark Sone as bad. */
					logger.log(Level.WARNING, "Downloaded post for Sone %s with missing data! ID: %s, Time: %s, Text: %s", new Object[] { sone, postId, postTime, postText });
					return;
				}
				try {
					posts.add(core.getPost(postId).setSone(sone).setTime(Long.parseLong(postTime)).setText(postText));
				} catch (NumberFormatException nfe1) {
					/* TODO - mark Sone as bad. */
					logger.log(Level.WARNING, "Downloaded post for Sone %s with invalid time: %s", new Object[] { sone, postTime });
					return;
				}
			}

			/* parse replies. */
			SimpleXML repliesXml = soneXml.getNode("replies");
			if (repliesXml == null) {
				/* TODO - mark Sone as bad. */
				logger.log(Level.WARNING, "Downloaded Sone %s has no replies!", new Object[] { sone });
				return;
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
					return;
				}
				try {
					replies.add(core.getReply(replyId).setSone(sone).setPost(core.getPost(replyPostId)).setTime(Long.parseLong(replyTime)).setText(replyText));
				} catch (NumberFormatException nfe1) {
					/* TODO - mark Sone as bad. */
					logger.log(Level.WARNING, "Downloaded reply for Sone %s with invalid time: %s", new Object[] { sone, replyTime });
					return;
				}
			}

			/* okay, apparently everything was parsed correctly. Now import. */
			/* atomic setter operation on the Sone. */
			synchronized (sone) {
				sone.setProfile(profile);
			}
		} catch (IOException ioe1) {
			logger.log(Level.WARNING, "Could not read XML file from " + sone + "!", ioe1);
		} finally {
			if (xmlBucket != null) {
				xmlBucket.free();
			}
			Closer.close(xmlInputStream);
		}
	}

}
