/*
 * shortener - Page.java - Copyright © 2010 David Roden
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import freenet.clients.http.ToadletContext;
import freenet.support.api.HTTPRequest;

/**
 * A page is responsible for handling HTTP requests and creating appropriate
 * responses.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface Page {

	/**
	 * Returns the path of this toadlet.
	 *
	 * @return The path of this toadlet
	 */
	public String getPath();

	/**
	 * Handles a request.
	 *
	 * @param request
	 *            The request to handle
	 * @return The response
	 */
	public Response handleRequest(Request request);

	/**
	 * Container for request data.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public class Request {

		/**
		 * Enumeration for all possible HTTP request methods.
		 *
		 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’
		 *         Roden</a>
		 */
		public enum Method {

			/** GET. */
			GET,

			/** POST. */
			POST,

			/** PUT. */
			PUT,

			/** DELETE. */
			DELETE,

			/** HEAD. */
			HEAD,

			/** OPTIONS. */
			OPTIONS,

			/** TRACE. */
			TRACE,

		}

		/** The URI that was accessed. */
		private final URI uri;

		/** The HTTP method that was used. */
		private final Method method;

		/** The HTTP request. */
		private final HTTPRequest httpRequest;

		/** The toadlet context. */
		private final ToadletContext toadletContext;

		/**
		 * Creates a new request that holds the given data.
		 *
		 * @param uri
		 *            The URI of the request
		 * @param method
		 *            The HTTP method of the request
		 * @param httpRequest
		 *            The HTTP request
		 * @param toadletContext
		 *            The toadlet context of the request
		 */
		public Request(URI uri, Method method, HTTPRequest httpRequest, ToadletContext toadletContext) {
			this.uri = uri;
			this.method = method;
			this.httpRequest = httpRequest;
			this.toadletContext = toadletContext;
		}

		/**
		 * Returns the URI that was accessed.
		 *
		 * @return The accessed URI
		 */
		public URI getURI() {
			return uri;
		}

		/**
		 * Returns the HTTP method that was used to access the page.
		 *
		 * @return The HTTP method
		 */
		public Method getMethod() {
			return method;
		}

		/**
		 * Returns the HTTP request.
		 *
		 * @return The HTTP request
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

	/**
	 * Container for the HTTP response of a {@link Page}.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public class Response {

		/** The HTTP status code of the response. */
		private final int statusCode;

		/** The HTTP status text of the response. */
		private final String statusText;

		/** The content type of the response. */
		private final String contentType;

		/** The headers of the response. */
		private final Map<String, String> headers;

		/** The content of the response body. */
		private final InputStream content;

		/**
		 * Creates a new response.
		 *
		 * @param statusCode
		 *            The HTTP status code of the response
		 * @param statusText
		 *            The HTTP status text of the response
		 * @param contentType
		 *            The content type of the response
		 * @param text
		 *            The text in the response body
		 */
		public Response(int statusCode, String statusText, String contentType, String text) {
			this(statusCode, statusText, contentType, getBytes(text));
		}

		/**
		 * Creates a new response.
		 *
		 * @param statusCode
		 *            The HTTP status code of the response
		 * @param statusText
		 *            The HTTP status text of the response
		 * @param contentType
		 *            The content type of the response
		 * @param content
		 *            The content of the reponse body
		 */
		public Response(int statusCode, String statusText, String contentType, byte[] content) {
			this(statusCode, statusText, contentType, new HashMap<String, String>(), content);
		}

		/**
		 * Creates a new response.
		 *
		 * @param statusCode
		 *            The HTTP status code of the response
		 * @param statusText
		 *            The HTTP status text of the response
		 * @param contentType
		 *            The content type of the response
		 * @param headers
		 *            The headers of the response
		 */
		public Response(int statusCode, String statusText, String contentType, Map<String, String> headers) {
			this(statusCode, statusText, contentType, headers, (InputStream) null);
		}

		/**
		 * Creates a new response.
		 *
		 * @param statusCode
		 *            The HTTP status code of the response
		 * @param statusText
		 *            The HTTP status text of the response
		 * @param contentType
		 *            The content type of the response
		 * @param headers
		 *            The headers of the response
		 * @param content
		 *            The content of the reponse body
		 */
		public Response(int statusCode, String statusText, String contentType, Map<String, String> headers, byte[] content) {
			this(statusCode, statusText, contentType, headers, new ByteArrayInputStream(content));
		}

		/**
		 * Creates a new response.
		 *
		 * @param statusCode
		 *            The HTTP status code of the response
		 * @param statusText
		 *            The HTTP status text of the response
		 * @param contentType
		 *            The content type of the response
		 * @param headers
		 *            The headers of the response
		 * @param content
		 *            The content of the reponse body
		 */
		public Response(int statusCode, String statusText, String contentType, Map<String, String> headers, InputStream content) {
			this.statusCode = statusCode;
			this.statusText = statusText;
			this.contentType = contentType;
			this.headers = headers;
			this.content = content;
		}

		/**
		 * Returns the HTTP status code of the response.
		 *
		 * @return The HTTP status code
		 */
		public int getStatusCode() {
			return statusCode;
		}

		/**
		 * Returns the HTTP status text.
		 *
		 * @return The HTTP status text
		 */
		public String getStatusText() {
			return statusText;
		}

		/**
		 * Returns the content type of the response.
		 *
		 * @return The content type of the reponse
		 */
		public String getContentType() {
			return contentType;
		}

		/**
		 * Returns HTTP headers of the response. May be {@code null} if no
		 * headers are returned.
		 *
		 * @return The response headers, or {@code null} if there are no
		 *         response headers
		 */
		public Map<String, String> getHeaders() {
			return headers;
		}

		/**
		 * Returns the content of the response body. May be {@code null} if the
		 * response does not have a body.
		 *
		 * @return The content of the response body
		 */
		public InputStream getContent() {
			return content;
		}

		//
		// PRIVATE METHODS
		//

		/**
		 * Returns the UTF-8 representation of the given text.
		 *
		 * @param text
		 *            The text to encode
		 * @return The encoded text
		 */
		private static byte[] getBytes(String text) {
			try {
				return text.getBytes("UTF-8");
			} catch (UnsupportedEncodingException uee1) {
				/* every JVM needs to support UTF-8. */
			}
			return null;
		}

		/**
		 * Creates a header map containing a single header.
		 *
		 * @param name
		 *            The name of the header
		 * @param value
		 *            The value of the header
		 * @return The map containing the single header
		 */
		protected static Map<String, String> createHeader(String name, String value) {
			Map<String, String> headers = new HashMap<String, String>();
			headers.put(name, value);
			return headers;
		}

	}

	/**
	 * {@link Response} implementation that performs an HTTP redirect.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public class RedirectResponse extends Response {

		/**
		 * Creates a new redirect response to the new location.
		 *
		 * @param newLocation
		 *            The new location
		 */
		public RedirectResponse(String newLocation) {
			this(newLocation, true);
		}

		/**
		 * Creates a new redirect response to the new location.
		 *
		 * @param newLocation
		 *            The new location
		 * @param permanent
		 *            Whether the redirect should be marked as permanent
		 */
		public RedirectResponse(String newLocation, boolean permanent) {
			super(permanent ? 302 : 307, "Redirected", null, createHeader("Location", newLocation));
		}

	}

}
