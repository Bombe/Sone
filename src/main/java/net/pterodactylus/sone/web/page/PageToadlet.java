/*
 * shortener - PageToadlet.java - Copyright © 2010 David Roden
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
import java.net.URI;
import java.util.Map.Entry;

import net.pterodactylus.sone.web.page.Page.Request.Method;
import freenet.client.HighLevelSimpleClient;
import freenet.clients.http.LinkEnabledCallback;
import freenet.clients.http.Toadlet;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.ToadletContextClosedException;
import freenet.support.MultiValueTable;
import freenet.support.api.Bucket;
import freenet.support.api.HTTPRequest;
import freenet.support.io.BucketTools;
import freenet.support.io.Closer;

/**
 * {@link Toadlet} implementation that is wrapped around a {@link Page}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PageToadlet extends Toadlet implements LinkEnabledCallback {

	/** The name of the menu item. */
	private final String menuName;

	/** The page that handles processing. */
	private final Page page;

	/** The path prefix for the page. */
	private final String pathPrefix;

	/**
	 * Creates a new toadlet that hands off processing to a {@link Page}.
	 *
	 * @param highLevelSimpleClient
	 * @param menuName
	 *            The name of the menu item
	 * @param page
	 *            The page to handle processing
	 * @param pathPrefix
	 *            Prefix that is prepended to all {@link Page#getPath()} return
	 *            values
	 */
	protected PageToadlet(HighLevelSimpleClient highLevelSimpleClient, String menuName, Page page, String pathPrefix) {
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
		handleRequest(new Page.Request(uri, Method.GET, httpRequest, toadletContext));
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
		handleRequest(new Page.Request(uri, Method.POST, httpRequest, toadletContext));
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
	private void handleRequest(Page.Request pageRequest) throws IOException, ToadletContextClosedException {
		Bucket data = null;
		try {
			Page.Response pageResponse = page.handleRequest(pageRequest);
			MultiValueTable<String, String> headers = new MultiValueTable<String, String>();
			if (pageResponse.getHeaders() != null) {
				for (Entry<String, String> headerEntry : pageResponse.getHeaders().entrySet()) {
					headers.put(headerEntry.getKey(), headerEntry.getValue());
				}
			}
			data = pageRequest.getToadletContext().getBucketFactory().makeBucket(-1);
			if (pageResponse.getContent() != null) {
				try {
					BucketTools.copyFrom(data, pageResponse.getContent(), -1);
				} finally {
					Closer.close(pageResponse.getContent());
				}
			} else {
				/* get an OutputStream and close it immediately. */
				Closer.close(data.getOutputStream());
			}
			writeReply(pageRequest.getToadletContext(), pageResponse.getStatusCode(), pageResponse.getContentType(), pageResponse.getStatusText(), headers, data);
		} catch (Throwable t1) {
			writeInternalError(t1, pageRequest.getToadletContext());
		} finally {
			Closer.close(data);
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

}
