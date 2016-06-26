/*
 * Sone - DefaultPostBuilderFactory.java - Copyright © 2013–2016 David Roden
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

import net.pterodactylus.sone.database.PostBuilder;
import net.pterodactylus.sone.database.PostBuilderFactory;
import net.pterodactylus.sone.database.SoneProvider;

import com.google.inject.Inject;

/**
 * {@link PostBuilderFactory} implementation that creates
 * {@link PostBuilderImpl}s.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DefaultPostBuilderFactory implements PostBuilderFactory {

	/** The Sone provider. */
	private final SoneProvider soneProvider;

	/**
	 * Creates a new default post builder factory.
	 *
	 * @param soneProvider
	 *            The Sone provider
	 */
	@Inject
	public DefaultPostBuilderFactory(SoneProvider soneProvider) {
		this.soneProvider = soneProvider;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PostBuilder newPostBuilder() {
		return new PostBuilderImpl(soneProvider);
	}

}
