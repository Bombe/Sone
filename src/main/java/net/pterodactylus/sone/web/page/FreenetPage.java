/*
 * Sone - FreenetPage.java - Copyright © 2011–2015 David Roden
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

import java.net.URI;

import net.pterodactylus.util.web.Page;

/**
 * Freenet-specific {@link Page} extension that adds the capability to allow a
 * link to a page to be unharmed by Freenet’s content filter.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface FreenetPage extends Page<FreenetRequest> {

	/**
	 * Returns whether the given should be excepted from being filtered.
	 *
	 * @param link
	 *            The link to check
	 * @return {@code true} if the link should not be filtered, {@code false} if
	 *         it should be filtered
	 */
	public boolean isLinkExcepted(URI link);

}
