/*
 * Sone - PostReplyBuilderImpl.java - Copyright © 2013–2016 David Roden
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

import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.database.PostProvider;
import net.pterodactylus.sone.database.PostReplyBuilder;
import net.pterodactylus.sone.database.SoneProvider;

/**
 * {@link PostReplyBuilder} implementation that creates {@link PostReplyImpl}
 * objects.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PostReplyBuilderImpl extends AbstractPostReplyBuilder {

	/** The Sone provider. */
	private final SoneProvider soneProvider;

	/** The post provider. */
	private final PostProvider postProvider;

	/**
	 * Creates a new post reply builder.
	 *
	 * @param soneProvider
	 *            The Sone provider
	 * @param postProvider
	 *            The post provider
	 */
	public PostReplyBuilderImpl(SoneProvider soneProvider, PostProvider postProvider) {
		this.soneProvider = soneProvider;
		this.postProvider = postProvider;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PostReply build() {
		checkState((randomId && (id == null)) || (!randomId && (id != null)), "either random ID nor custom ID must be set");
		checkState(senderId != null, "sender must not be null");
		checkState((currentTime && (time == 0)) || (!currentTime && (time >= 0)), "either current time or custom time must be set");
		checkState((text != null) && !text.trim().isEmpty(), "text must not be empty");
		checkState(postId != null, "post must not be null");

		/* create new post reply. */
		return new PostReplyImpl(soneProvider, postProvider, randomId ? UUID.randomUUID().toString() : id, senderId, currentTime ? System.currentTimeMillis() : time, text, postId);
	}

}
