/*
 * Sone - FcpInterface.java - Copyright © 2011 David Roden
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

package net.pterodactylus.sone.fcp;

import java.util.Collection;
import java.util.List;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.SimpleFieldSetBuilder;
import net.pterodactylus.sone.freenet.fcp.AbstractCommand;
import net.pterodactylus.sone.freenet.fcp.Command;
import net.pterodactylus.sone.freenet.fcp.FcpException;
import net.pterodactylus.sone.template.SoneAccessor;
import net.pterodactylus.util.filter.Filters;
import freenet.node.FSParseException;
import freenet.support.SimpleFieldSet;

/**
 * Abstract base implementation of a {@link Command} with Sone-related helper
 * methods.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public abstract class AbstractSoneCommand extends AbstractCommand {

	/** The Sone core. */
	private final Core core;

	/**
	 * Creates a new abstract Sone FCP command.
	 *
	 * @param core
	 *            The Sone core
	 */
	protected AbstractSoneCommand(Core core) {
		this.core = core;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the Sone core.
	 *
	 * @return The Sone core
	 */
	protected Core getCore() {
		return core;
	}

	//
	// PROTECTED METHODS
	//

	/**
	 * Returns a Sone whose ID is a parameter in the given simple field set.
	 *
	 * @param simpleFieldSet
	 *            The simple field set containing the ID of the Sone
	 * @param parameterName
	 *            The name under which the Sone ID is stored in the simple field
	 *            set
	 * @return The Sone
	 * @throws FcpException
	 *             if there is no Sone ID stored under the given parameter name,
	 *             or if the Sone ID is invalid
	 */
	protected Sone getSone(SimpleFieldSet simpleFieldSet, String parameterName) throws FcpException {
		try {
			String soneId = simpleFieldSet.getString(parameterName);
			Sone sone = core.getSone(soneId, false);
			if (sone == null) {
				throw new FcpException("Could not load Sone from “" + soneId + "”.");
			}
			return sone;
		} catch (FSParseException fspe1) {
			throw new FcpException("Could not load Sone ID from “" + parameterName + "”.", fspe1);
		}
	}

	/**
	 * Returns a post whose ID is a parameter in the given simple field set.
	 *
	 * @param simpleFieldSet
	 *            The simple field set containing the ID of the post
	 * @param parameterName
	 *            The name under which the post ID is stored in the simple field
	 *            set
	 * @return The post
	 * @throws FcpException
	 *             if there is no post ID stored under the given parameter name,
	 *             or if the post ID is invalid
	 */
	protected Post getPost(SimpleFieldSet simpleFieldSet, String parameterName) throws FcpException {
		try {
			String postId = simpleFieldSet.getString(parameterName);
			Post post = core.getPost(postId, false);
			if (post == null) {
				throw new FcpException("Could not load post from “" + postId + "”.");
			}
			return post;
		} catch (FSParseException fspe1) {
			throw new FcpException("Could not post ID from “" + parameterName + "”.", fspe1);
		}
	}

	/**
	 * Returns a reply whose ID is a parameter in the given simple field set.
	 *
	 * @param simpleFieldSet
	 *            The simple field set containing the ID of the reply
	 * @param parameterName
	 *            The name under which the reply ID is stored in the simple
	 *            field set
	 * @return The reply
	 * @throws FcpException
	 *             if there is no reply ID stored under the given parameter
	 *             name, or if the reply ID is invalid
	 */
	protected Reply getReply(SimpleFieldSet simpleFieldSet, String parameterName) throws FcpException {
		try {
			String replyId = simpleFieldSet.getString(parameterName);
			Reply reply = core.getReply(replyId, false);
			if (reply == null) {
				throw new FcpException("Could not load reply from “" + replyId + "”.");
			}
			return reply;
		} catch (FSParseException fspe1) {
			throw new FcpException("Could not reply ID from “" + parameterName + "”.", fspe1);
		}
	}

	/**
	 * Creates a simple field set from the given collection of Sones.
	 *
	 * @param sones
	 *            The Sones to encode
	 * @param prefix
	 *            The prefix for the field names (may be empty but not
	 *            {@code null})
	 * @return The simple field set containing the given Sones
	 */
	protected SimpleFieldSet encodeSones(Collection<? extends Sone> sones, String prefix) {
		SimpleFieldSetBuilder soneBuilder = new SimpleFieldSetBuilder();

		int soneIndex = 0;
		soneBuilder.put(prefix + "Count", sones.size());
		for (Sone sone : sones) {
			String sonePrefix = prefix + soneIndex++ + ".";
			soneBuilder.put(sonePrefix + "ID", sone.getId());
			soneBuilder.put(sonePrefix + "Name", sone.getName());
			soneBuilder.put(sonePrefix + "NiceName", SoneAccessor.getNiceName(sone));
			soneBuilder.put(sonePrefix + "Time", sone.getTime());
		}

		return soneBuilder.get();
	}

	/**
	 * Creates a simple field set from the given post.
	 *
	 * @param post
	 *            The post to encode
	 * @param prefix
	 *            The prefix for the field names (may be empty but not
	 *            {@code null})
	 * @param includeReplies
	 *            {@code true} to include replies, {@code false} to not include
	 *            replies
	 * @return The simple field set containing the post
	 */
	protected SimpleFieldSet encodePost(Post post, String prefix, boolean includeReplies) {
		SimpleFieldSetBuilder postBuilder = new SimpleFieldSetBuilder();

		postBuilder.put(prefix + "ID", post.getId());
		postBuilder.put(prefix + "Sone", post.getSone().getId());
		if (post.getRecipient() != null) {
			postBuilder.put(prefix + "Recipient", post.getRecipient().getId());
		}
		postBuilder.put(prefix + "Time", post.getTime());
		postBuilder.put(prefix + "Text", post.getText());
		postBuilder.put(encodeLikes(core.getLikes(post), prefix + "Likes."));

		if (includeReplies) {
			List<Reply> replies = core.getReplies(post);
			postBuilder.put(encodeReplies(replies, prefix));
		}

		return postBuilder.get();
	}

	/**
	 * Creates a simple field set from the given collection of posts.
	 *
	 * @param posts
	 *            The posts to encode
	 * @param prefix
	 *            The prefix for the field names (may be empty but not
	 *            {@code null})
	 * @param includeReplies
	 *            {@code true} to include the replies, {@code false} to not
	 *            include the replies
	 * @return The simple field set containing the posts
	 */
	protected SimpleFieldSet encodePosts(Collection<? extends Post> posts, String prefix, boolean includeReplies) {
		SimpleFieldSetBuilder postBuilder = new SimpleFieldSetBuilder();

		int postIndex = 0;
		postBuilder.put(prefix + "Count", posts.size());
		for (Post post : posts) {
			String postPrefix = prefix + postIndex++;
			postBuilder.put(encodePost(post, postPrefix + ".", includeReplies));
			if (includeReplies) {
				postBuilder.put(encodeReplies(Filters.filteredList(core.getReplies(post), Reply.FUTURE_REPLIES_FILTER), postPrefix + "."));
			}
		}

		return postBuilder.get();
	}

	/**
	 * Creates a simple field set from the given collection of replies.
	 *
	 * @param replies
	 *            The replies to encode
	 * @param prefix
	 *            The prefix for the field names (may be empty, but not
	 *            {@code null})
	 * @return The simple field set containing the replies
	 */
	protected SimpleFieldSet encodeReplies(Collection<? extends Reply> replies, String prefix) {
		SimpleFieldSetBuilder replyBuilder = new SimpleFieldSetBuilder();

		int replyIndex = 0;
		replyBuilder.put(prefix + "Replies.Count", replies.size());
		for (Reply reply : replies) {
			String replyPrefix = prefix + "Replies." + replyIndex++ + ".";
			replyBuilder.put(replyPrefix + "ID", reply.getId());
			replyBuilder.put(replyPrefix + "Sone", reply.getSone().getId());
			replyBuilder.put(replyPrefix + "Time", reply.getTime());
			replyBuilder.put(replyPrefix + "Text", reply.getText());
		}

		return replyBuilder.get();
	}

	/**
	 * Creates a simple field set from the given collection of Sones that like
	 * an element.
	 *
	 * @param likes
	 *            The liking Sones
	 * @param prefix
	 *            The prefix for the field names (may be empty but not
	 *            {@code null})
	 * @return The simple field set containing the likes
	 */
	protected SimpleFieldSet encodeLikes(Collection<? extends Sone> likes, String prefix) {
		SimpleFieldSetBuilder likesBuilder = new SimpleFieldSetBuilder();

		int likeIndex = 0;
		likesBuilder.put(prefix + "Count", likes.size());
		for (Sone sone : likes) {
			String sonePrefix = prefix + likeIndex++ + ".";
			likesBuilder.put(sonePrefix + "ID", sone.getId());
		}

		return likesBuilder.get();
	}

}
