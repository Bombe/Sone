/*
 * Sone - AbstractSoneCommand.java - Copyright © 2011–2012 David Roden
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
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Profile.Field;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.SimpleFieldSetBuilder;
import net.pterodactylus.sone.freenet.fcp.AbstractCommand;
import net.pterodactylus.sone.freenet.fcp.Command;
import net.pterodactylus.sone.freenet.fcp.FcpException;
import net.pterodactylus.sone.template.SoneAccessor;

import com.google.common.collect.Collections2;

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

	/** Whether this command needs write access. */
	private final boolean writeAccess;

	/**
	 * Creates a new abstract Sone FCP command.
	 *
	 * @param core
	 *            The Sone core
	 */
	protected AbstractSoneCommand(Core core) {
		this(core, false);
	}

	/**
	 * Creates a new abstract Sone FCP command.
	 *
	 * @param core
	 *            The Sone core
	 * @param writeAccess
	 *            {@code true} if this command requires write access,
	 *            {@code false} otherwise
	 */
	protected AbstractSoneCommand(Core core, boolean writeAccess) {
		this.core = core;
		this.writeAccess = writeAccess;
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

	/**
	 * Returns whether this command requires write access.
	 *
	 * @return {@code true} if this command require write access, {@code false}
	 *         otherwise
	 */
	public boolean requiresWriteAccess() {
		return writeAccess;
	}

	//
	// PROTECTED METHODS
	//

	/**
	 * Encodes text in a way that makes it possible for the text to be stored in
	 * a {@link SimpleFieldSet}. Backslashes, CR, and LF are prepended with a
	 * backslash.
	 *
	 * @param text
	 *            The text to encode
	 * @return The encoded text
	 */
	protected static String encodeString(String text) {
		return text.replaceAll("\\\\", "\\\\").replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r");
	}

	/**
	 * Returns a Sone whose ID is a parameter in the given simple field set.
	 *
	 * @param simpleFieldSet
	 *            The simple field set containing the ID of the Sone
	 * @param parameterName
	 *            The name under which the Sone ID is stored in the simple field
	 *            set
	 * @param localOnly
	 *            {@code true} to only return local Sones, {@code false} to
	 *            return any Sones
	 * @return The Sone
	 * @throws FcpException
	 *             if there is no Sone ID stored under the given parameter name,
	 *             or if the Sone ID is invalid
	 */
	protected Sone getSone(SimpleFieldSet simpleFieldSet, String parameterName, boolean localOnly) throws FcpException {
		return getSone(simpleFieldSet, parameterName, localOnly, true);
	}

	/**
	 * Returns a Sone whose ID is a parameter in the given simple field set.
	 *
	 * @param simpleFieldSet
	 *            The simple field set containing the ID of the Sone
	 * @param parameterName
	 *            The name under which the Sone ID is stored in the simple field
	 *            set
	 * @param localOnly
	 *            {@code true} to only return local Sones, {@code false} to
	 *            return any Sones
	 * @param mandatory
	 *            {@code true} if a valid Sone ID is required, {@code false}
	 *            otherwise
	 * @return The Sone, or {@code null} if {@code mandatory} is {@code false}
	 *         and the Sone ID is invalid
	 * @throws FcpException
	 *             if there is no Sone ID stored under the given parameter name,
	 *             or if {@code mandatory} is {@code true} and the Sone ID is
	 *             invalid
	 */
	protected Sone getSone(SimpleFieldSet simpleFieldSet, String parameterName, boolean localOnly, boolean mandatory) throws FcpException {
		String soneId = simpleFieldSet.get(parameterName);
		if (mandatory && (soneId == null)) {
			throw new FcpException("Could not load Sone ID from “" + parameterName + "”.");
		}
		Sone sone = localOnly ? core.getLocalSone(soneId, false) : core.getSone(soneId, false);
		if (mandatory && (sone == null)) {
			throw new FcpException("Could not load Sone from “" + soneId + "”.");
		}
		return sone;
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
	protected PostReply getReply(SimpleFieldSet simpleFieldSet, String parameterName) throws FcpException {
		try {
			String replyId = simpleFieldSet.getString(parameterName);
			PostReply reply = core.getPostReply(replyId, false);
			if (reply == null) {
				throw new FcpException("Could not load reply from “" + replyId + "”.");
			}
			return reply;
		} catch (FSParseException fspe1) {
			throw new FcpException("Could not reply ID from “" + parameterName + "”.", fspe1);
		}
	}

	/**
	 * Creates a simple field set from the given Sone, including {@link Profile}
	 * information.
	 *
	 * @param sone
	 *            The Sone to encode
	 * @param prefix
	 *            The prefix for the field names (may be empty but not {@code
	 *            null})
	 * @param localSone
	 *            An optional local Sone that is used for Sone-specific data,
	 *            such as if the Sone is followed by the local Sone
	 * @return The simple field set containing the given Sone
	 */
	protected static SimpleFieldSet encodeSone(Sone sone, String prefix, Sone localSone) {
		SimpleFieldSetBuilder soneBuilder = new SimpleFieldSetBuilder();

		soneBuilder.put(prefix + "Name", sone.getName());
		soneBuilder.put(prefix + "NiceName", SoneAccessor.getNiceName(sone));
		soneBuilder.put(prefix + "LastUpdated", sone.getTime());
		if (localSone != null) {
			soneBuilder.put(prefix + "Followed", String.valueOf(localSone.hasFriend(sone.getId())));
		}
		Profile profile = sone.getProfile();
		soneBuilder.put(prefix + "Field.Count", profile.getFields().size());
		int fieldIndex = 0;
		for (Field field : profile.getFields()) {
			soneBuilder.put(prefix + "Field." + fieldIndex + ".Name", field.getName());
			soneBuilder.put(prefix + "Field." + fieldIndex + ".Value", field.getValue());
			++fieldIndex;
		}

		return soneBuilder.get();
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
	protected static SimpleFieldSet encodeSones(Collection<? extends Sone> sones, String prefix) {
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
		postBuilder.put(prefix + "Text", encodeString(post.getText()));
		postBuilder.put(encodeLikes(core.getLikes(post), prefix + "Likes."));

		if (includeReplies) {
			List<PostReply> replies = core.getReplies(post);
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
				postBuilder.put(encodeReplies(Collections2.filter(core.getReplies(post), Reply.FUTURE_REPLY_FILTER), postPrefix + "."));
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
	protected static SimpleFieldSet encodeReplies(Collection<? extends PostReply> replies, String prefix) {
		SimpleFieldSetBuilder replyBuilder = new SimpleFieldSetBuilder();

		int replyIndex = 0;
		replyBuilder.put(prefix + "Replies.Count", replies.size());
		for (PostReply reply : replies) {
			String replyPrefix = prefix + "Replies." + replyIndex++ + ".";
			replyBuilder.put(replyPrefix + "ID", reply.getId());
			replyBuilder.put(replyPrefix + "Sone", reply.getSone().getId());
			replyBuilder.put(replyPrefix + "Time", reply.getTime());
			replyBuilder.put(replyPrefix + "Text", encodeString(reply.getText()));
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
	protected static SimpleFieldSet encodeLikes(Collection<? extends Sone> likes, String prefix) {
		SimpleFieldSetBuilder likesBuilder = new SimpleFieldSetBuilder();

		int likeIndex = 0;
		likesBuilder.put(prefix + "Count", likes.size());
		for (Sone sone : likes) {
			String sonePrefix = prefix + likeIndex++ + ".";
			likesBuilder.put(sonePrefix + "ID", sone.getId());
		}

		return likesBuilder.get();
	}

	//
	// OBJECT METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getClass().getName() + "[writeAccess=" + writeAccess + "]";
	}

}
