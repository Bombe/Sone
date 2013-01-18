/*
 * Sone - PostReplyBuilderImpl.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sone.data.impl;

import static com.google.common.base.Preconditions.checkState;

import java.util.UUID;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.PostReplyBuilder;

import org.apache.commons.lang.StringUtils;

/**
 * {@link PostReplyBuilder} implementation that creates {@link PostReplyImpl}
 * objects.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PostReplyBuilderImpl extends AbstractReplyBuilder<PostReplyBuilderImpl, PostReplyBuilder> implements PostReplyBuilder {

	/** The post the created reply refers to. */
	private Post post;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PostReplyBuilder to(Post post) {
		this.post = post;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PostReply build() {
		checkState((randomId && (id == null)) || (!randomId && (id != null)), "either random ID nor custom ID must be set");
		checkState(sender != null, "sender must not be null");
		checkState((currentTime && (time == 0)) || (!currentTime && (time >= 0)), "either current time or custom time must be set");
		checkState(!StringUtils.isBlank(text), "text must not be empty");
		checkState(post != null, "post must not be null");

		/* create new post reply. */
		PostReplyImpl postReplyImpl = new PostReplyImpl(randomId ? UUID.randomUUID().toString() : id);
		postReplyImpl.setSone(sender);
		postReplyImpl.setPost(post);
		postReplyImpl.setTime(currentTime ? System.currentTimeMillis() : time);
		postReplyImpl.setText(text);
		return postReplyImpl;
	}
}
