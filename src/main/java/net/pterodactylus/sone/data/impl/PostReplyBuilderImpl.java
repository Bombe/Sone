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
		validate();

		/* create new post reply. */
		return new PostReplyImpl(soneProvider, postProvider, randomId ? UUID.randomUUID().toString() : id, senderId, currentTime ? System.currentTimeMillis() : time, text, postId);
	}

}
