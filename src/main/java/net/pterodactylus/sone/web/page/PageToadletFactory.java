/*
 * Sone - PageToadletFactory.java - Copyright © 2010–2015 David Roden
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

package net.pterodactylus.sone.web.page;

import net.pterodactylus.util.web.Page;
import freenet.client.HighLevelSimpleClient;

/**
 * Factory that creates {@link PageToadlet}s using a given
 * {@link HighLevelSimpleClient}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PageToadletFactory {

	/** The client to use when creating the toadlets. */
	private final HighLevelSimpleClient highLevelSimpleClient;

	/** The prefix for all pages’ paths. */
	private final String pathPrefix;

	/**
	 * Creates a new {@link PageToadlet} factory.
	 *
	 * @param highLevelSimpleClient
	 *            The client to use when creating the toadlets
	 * @param pathPrefix
	 *            The path that is prepended to all pages’ paths
	 */
	public PageToadletFactory(HighLevelSimpleClient highLevelSimpleClient, String pathPrefix) {
		this.highLevelSimpleClient = highLevelSimpleClient;
		this.pathPrefix = pathPrefix;
	}

	/**
	 * Creates a {@link PageToadlet} that wraps the given page and does not
	 * appear in the node’s menu.
	 *
	 * @param page
	 *            The page to wrap
	 * @return The toadlet wrapped around the page
	 */
	public PageToadlet createPageToadlet(Page<FreenetRequest> page) {
		return createPageToadlet(page, null);
	}

	/**
	 * Creates a {@link PageToadlet} that wraps the given page and appears in
	 * the node’s menu under the given name.
	 *
	 * @param page
	 *            The page to wrap
	 * @param menuName
	 *            The name of the menu item
	 * @return The toadlet wrapped around the page
	 */
	public PageToadlet createPageToadlet(Page<FreenetRequest> page, String menuName) {
		return new PageToadlet(highLevelSimpleClient, menuName, page, pathPrefix);
	}

}
