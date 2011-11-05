/*
 * Sone - JsonPage.java - Copyright © 2010 David Roden
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

package net.pterodactylus.sone.web.ajax;

import java.io.IOException;
import java.net.URI;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetPage;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.json.JsonObject;
import net.pterodactylus.util.json.JsonUtils;
import net.pterodactylus.util.web.Page;
import net.pterodactylus.util.web.Response;
import freenet.clients.http.ToadletContext;
import freenet.clients.http.SessionManager.Session;

/**
 * A JSON page is a specialized {@link Page} that will always return a JSON
 * object to the browser, e.g. for use with AJAX or other scripting frameworks.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public abstract class JsonPage implements FreenetPage {

	/** The path of the page. */
	private final String path;

	/** The Sone web interface. */
	protected final WebInterface webInterface;

	/**
	 * Creates a new JSON page at the given path.
	 *
	 * @param path
	 *            The path of the page
	 * @param webInterface
	 *            The Sone web interface
	 */
	public JsonPage(String path, WebInterface webInterface) {
		this.path = path;
		this.webInterface = webInterface;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the current session, creating a new session if there is no
	 * current session.
	 *
	 * @param toadletContenxt
	 *            The toadlet context
	 * @return The current session, or {@code null} if there is no current
	 *         session
	 */
	protected Session getCurrentSession(ToadletContext toadletContenxt) {
		return webInterface.getCurrentSession(toadletContenxt);
	}

	/**
	 * Returns the current session, creating a new session if there is no
	 * current session and {@code create} is {@code true}.
	 *
	 * @param toadletContenxt
	 *            The toadlet context
	 * @param create
	 *            {@code true} to create a new session if there is no current
	 *            session, {@code false} otherwise
	 * @return The current session, or {@code null} if there is no current
	 *         session
	 */
	protected Session getCurrentSession(ToadletContext toadletContenxt, boolean create) {
		return webInterface.getCurrentSession(toadletContenxt, create);
	}

	/**
	 * Returns the currently logged in Sone.
	 *
	 * @param toadletContext
	 *            The toadlet context
	 * @return The currently logged in Sone, or {@code null} if no Sone is
	 *         currently logged in
	 */
	protected Sone getCurrentSone(ToadletContext toadletContext) {
		return webInterface.getCurrentSone(toadletContext);
	}

	/**
	 * Returns the currently logged in Sone.
	 *
	 * @param toadletContext
	 *            The toadlet context
	 * @param create
	 *            {@code true} to create a new session if no session exists,
	 *            {@code false} to not create a new session
	 * @return The currently logged in Sone, or {@code null} if no Sone is
	 *         currently logged in
	 */
	protected Sone getCurrentSone(ToadletContext toadletContext, boolean create) {
		return webInterface.getCurrentSone(toadletContext, create);
	}

	//
	// METHODS FOR SUBCLASSES TO OVERRIDE
	//

	/**
	 * This method is called to create the JSON object that is returned back to
	 * the browser.
	 *
	 * @param request
	 *            The request to handle
	 * @return The created JSON object
	 */
	protected abstract JsonObject createJsonObject(FreenetRequest request);

	/**
	 * Returns whether this command needs the form password for authentication
	 * and to prevent abuse.
	 *
	 * @return {@code true} if the form password (given as “formPassword”) is
	 *         required, {@code false} otherwise
	 */
	protected boolean needsFormPassword() {
		return true;
	}

	/**
	 * Returns whether this page requires the user to be logged in.
	 *
	 * @return {@code true} if the user needs to be logged in to use this page,
	 *         {@code false} otherwise
	 */
	protected boolean requiresLogin() {
		return true;
	}

	//
	// PROTECTED METHODS
	//

	/**
	 * Creates a success reply.
	 *
	 * @return A reply signaling success
	 */
	protected JsonObject createSuccessJsonObject() {
		return new JsonObject().put("success", true);
	}

	/**
	 * Creates an error reply.
	 *
	 * @param error
	 *            The error that has occured
	 * @return The JSON object, signalling failure and the error code
	 */
	protected JsonObject createErrorJsonObject(String error) {
		return new JsonObject().put("success", false).put("error", error);
	}

	//
	// PAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		return path;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPrefixPage() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response handleRequest(FreenetRequest request, Response response) throws IOException {
		if (webInterface.getCore().getPreferences().isRequireFullAccess() && !request.getToadletContext().isAllowedFullAccess()) {
			return response.setStatusCode(403).setStatusText("Forbidden").setContentType("application/json").write(JsonUtils.format(new JsonObject().put("success", false).put("error", "auth-required")));
		}
		if (needsFormPassword()) {
			String formPassword = request.getHttpRequest().getParam("formPassword");
			if (!webInterface.getFormPassword().equals(formPassword)) {
				return response.setStatusCode(403).setStatusText("Forbidden").setContentType("application/json").write(JsonUtils.format(new JsonObject().put("success", false).put("error", "auth-required")));
			}
		}
		if (requiresLogin()) {
			if (getCurrentSone(request.getToadletContext(), false) == null) {
				return response.setStatusCode(403).setStatusText("Forbidden").setContentType("application/json").write(JsonUtils.format(new JsonObject().put("success", false).put("error", "auth-required")));
			}
		}
		JsonObject jsonObject = createJsonObject(request);
		return response.setStatusCode(200).setStatusText("OK").setContentType("application/json").write(JsonUtils.format(jsonObject));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLinkExcepted(URI link) {
		return false;
	}

}
