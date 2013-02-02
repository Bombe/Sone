/*
 * Sone - PostDatabase.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sone.database;

import net.pterodactylus.util.config.Configuration;
import net.pterodactylus.util.config.ConfigurationException;

/**
 * Combines a {@link PostProvider}, a {@link PostBuilderFactory}, and a
 * {@link PostStore} into a complete post database.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface PostDatabase extends PostProvider, PostBuilderFactory, PostStore {

	/*
	 * these methods have to be here until the database knows how to save its
	 * own stuff. all the configuration-specific stuff will have to leave!
	 */

	/**
	 * Loads the knows posts.
	 *
	 * @param configuration
	 *            The configuration to load the known posts from
	 * @param prefix
	 *            The prefix for the configuration keys
	 */
	public void loadKnownPosts(Configuration configuration, String prefix);

	/**
	 * Saves the knows posts.
	 *
	 * @param configuration
	 *            The configuration to save the known posts to
	 * @param prefix
	 *            The prefix for the configuration keys
	 * @throws ConfigurationException
	 *             if a value can not be stored in the configuration
	 */
	public void saveKnownPosts(Configuration configuration, String prefix) throws ConfigurationException;

}
