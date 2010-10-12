/*
 * FreenetSone - WebInterface.java - Copyright © 2010 David Roden
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

package net.pterodactylus.sone.web;

import freenet.clients.http.SessionManager;
import freenet.l10n.BaseL10n;

/**
 * Bundles functionality that a web interface of a Freenet plugin needs, e.g.
 * references to l10n helpers.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class WebInterface {

	/** The node’s l10n helper. */
	private final BaseL10n l10n;

	/** The node’s session manager. */
	private final SessionManager sessionManager;

	/**
	 * Creates a new web interface.
	 *
	 * @param l10n
	 *            The node’s l10n helper
	 * @param sessionManager
	 *            The node’s session manager
	 */
	public WebInterface(BaseL10n l10n, SessionManager sessionManager) {
		this.l10n = l10n;
		this.sessionManager = sessionManager;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the l10n helper of the node.
	 *
	 * @return The node’s l10n helper
	 */
	public BaseL10n l10n() {
		return l10n;
	}

	/**
	 * Returns the session manager of the node.
	 *
	 * @return The node’s session manager
	 */
	public SessionManager sessionManager() {
		return sessionManager;
	}

}
