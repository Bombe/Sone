/*
 * Sone - PostBuilderImpl.java - Copyright © 2013 David Roden
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

import java.util.UUID;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.database.PostBuilder;
import net.pterodactylus.sone.database.SoneProvider;

/**
 * {@link PostBuilder} implementation that creates {@link PostImpl} objects.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PostBuilderImpl extends AbstractPostBuilder {

	/**
	 * Creates a new post builder.
	 *
	 * @param soneProvider
	 *            The Sone provider
	 */
	public PostBuilderImpl(SoneProvider soneProvider) {
		super(soneProvider);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Post build() {
		validate();
		return new PostImpl(soneProvider, randomId ? UUID.randomUUID().toString() : id, senderId, recipientId, currentTime ? System.currentTimeMillis() : time, text);
	}

}
