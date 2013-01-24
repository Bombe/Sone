/*
 * Sone - DefaultPostReplyBuilderFactory.java - Copyright © 2013 David Roden
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

import net.pterodactylus.sone.core.PostProvider;
import net.pterodactylus.sone.data.PostReplyBuilder;
import net.pterodactylus.sone.data.PostReplyBuilderFactory;

import com.google.inject.Inject;

/**
 * {@link PostReplyBuilderFactory} that creates {@link PostReplyBuilderImpl}s.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DefaultPostReplyBuilderFactory implements PostReplyBuilderFactory {

	/** The post provider. */
	private final PostProvider postProvider;

	/**
	 * Creates a new default post reply builder factory.
	 *
	 * @param postProvider
	 *            The post provider
	 */
	@Inject
	public DefaultPostReplyBuilderFactory(PostProvider postProvider) {
		this.postProvider = postProvider;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PostReplyBuilder newPostReplyBuilder() {
		return new PostReplyBuilderImpl(postProvider);
	}

}
