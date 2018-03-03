/*
 * Sone - PageToadlet.java - Copyright © 2010–2016 David Roden
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

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import net.pterodactylus.sone.utils.AutoCloseableBucket;
import net.pterodactylus.util.web.Header;
import net.pterodactylus.util.web.Method;
import net.pterodactylus.util.web.Page;
import net.pterodactylus.util.web.Response;

import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.LinkEnabledCallback;
import freenet.clients.http.LinkFilterExceptedToadlet;
import freenet.clients.http.Toadlet;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.support.MultiValueTable;
import freenet.support.api.HTTPRequest;

/**
 * {@link Toadlet} implementation that is wrapped around a {@link Page}.
 */
public class PageToadlet extends Toadlet implements LinkEnabledCallback, LinkFilterExceptedToadlet {

	/** The name of the menu item. */
	private final String menuName;

	/** The page that handles processing. */
	private final Page<FreenetRequest> page;

	/** The path prefix for the page. */
	private final String pathPrefix;

	/**
	 * Creates a new toadlet that hands off processing to a {@link Page}.
	 *
	 * @param highLevelSimpleClient
	 *            The high-level simple client
	 * @param menuName
	 *            The name of the menu item
	 * @param page
	 *            The page to handle processing
	 * @param pathPrefix
	 *            Prefix that is prepended to all {@link Page#getPath()} return
	 *            values
	 */
	protected PageToadlet(HighLevelSimpleClient highLevelSimpleClient, String menuName, Page<FreenetRequest> page, String pathPrefix) {
		super(highLevelSimpleClient);
		this.menuName = menuName;
		this.page = page;
		this.pathPrefix = pathPrefix;
	}

	/**
	 * Returns the name to display in the menu.
	 *
	 * @return The name in the menu
	 */
	public String getMenuName() {
		return menuName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String path() {
		return pathPrefix + page.getPath();
	}

	/**
	 * Handles a HTTP GET request.
	 *
	 * @param uri
	 *            The URI of the request
	 * @param httpRequest
	 *            The HTTP request
	 * @param toadletContext
	 *            The toadlet context
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws ToadletContextClosedException
	 *             if the toadlet context is closed
	 */
	public void handleMethodGET(URI uri, HTTPRequest httpRequest, ToadletContext toadletContext) throws IOException, ToadletContextClosedException {
		handleRequest(new FreenetRequest(uri, Method.GET, httpRequest, toadletContext));
	}

	/**
	 * Handles a HTTP POST request.
	 *
	 * @param uri
	 *            The URI of the request
	 * @param httpRequest
	 *            The HTTP request
	 * @param toadletContext
	 *            The toadlet context
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws ToadletContextClosedException
	 *             if the toadlet context is closed
	 */
	public void handleMethodPOST(URI uri, HTTPRequest httpRequest, ToadletContext toadletContext) throws IOException, ToadletContextClosedException {
		handleRequest(new FreenetRequest(uri, Method.POST, httpRequest, toadletContext));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getClass().getName() + "[path=" + path() + ",page=" + page + "]";
	}

	/**
	 * Handles a HTTP request.
	 *
	 * @param pageRequest
	 *            The request to handle
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws ToadletContextClosedException
	 *             if the toadlet context is closed
	 */
	private void handleRequest(FreenetRequest pageRequest) throws IOException, ToadletContextClosedException {
		try (AutoCloseableBucket pageBucket = new AutoCloseableBucket(pageRequest.getToadletContext().getBucketFactory().makeBucket(-1));
		     OutputStream pageBucketOutputStream = pageBucket.getBucket().getOutputStream()) {
			Response pageResponse = page.handleRequest(pageRequest, new Response(pageBucketOutputStream));
			MultiValueTable<String, String> headers = new MultiValueTable<String, String>();
			if (pageResponse.getHeaders() != null) {
				for (Header header : pageResponse.getHeaders()) {
					for (String value : header) {
						headers.put(header.getName(), value);
					}
				}
			}
			writeReply(pageRequest.getToadletContext(), pageResponse.getStatusCode(), pageResponse.getContentType(), pageResponse.getStatusText(), headers, pageBucket.getBucket());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEnabled(ToadletContext toadletContext) {
		if (page instanceof LinkEnabledCallback) {
			return ((LinkEnabledCallback) page).isEnabled(toadletContext);
		}
		return true;
	}

	//
	// LINKFILTEREXCEPTEDTOADLET METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLinkExcepted(URI link) {
		return (page instanceof FreenetPage) && ((FreenetPage) page).isLinkExcepted(link);
	}

}
