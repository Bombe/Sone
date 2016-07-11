/*
 * Sone - FreenetRequest.java - Copyright © 2011–2016 David Roden
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

import net.pterodactylus.util.web.Method;
import net.pterodactylus.util.web.Request;
import freenet.clients.http.ToadletContext;
import freenet.support.api.HTTPRequest;

/**
 * Encapsulates all Freenet-specific properties of a request.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FreenetRequest extends Request {

	/** The underlying HTTP request from Freenet. */
	private final HTTPRequest httpRequest;

	/** The toadlet context. */
	private final ToadletContext toadletContext;

	/**
	 * Creates a new freenet request.
	 *
	 * @param uri
	 *            The URI that is being accessed
	 * @param method
	 *            The method used to access this page
	 * @param httpRequest
	 *            The underlying HTTP request from Freenet
	 * @param toadletContext
	 *            The toadlet context
	 */
	public FreenetRequest(URI uri, Method method, HTTPRequest httpRequest, ToadletContext toadletContext) {
		super(uri, method);
		this.httpRequest = httpRequest;
		this.toadletContext = toadletContext;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the underlying HTTP request from Freenet.
	 *
	 * @return The underlying HTTP request from Freenet
	 */
	public HTTPRequest getHttpRequest() {
		return httpRequest;
	}

	/**
	 * Returns the toadlet context.
	 *
	 * @return The toadlet context
	 */
	public ToadletContext getToadletContext() {
		return toadletContext;
	}

}
