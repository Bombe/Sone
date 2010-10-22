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

import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.Page;
import net.pterodactylus.util.json.JsonObject;
import net.pterodactylus.util.json.JsonUtils;

/**
 * A JSON page is a specialized {@link Page} that will always return a JSON
 * object to the browser, e.g. for use with AJAX or other scripting frameworks.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public abstract class JsonPage implements Page {

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
	protected abstract JsonObject createJsonObject(Request request);

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
	public Response handleRequest(Request request) {
		if (needsFormPassword()) {
			String formPassword = request.getHttpRequest().getParam("formPassword");
			if (!webInterface.formPassword().equals(formPassword)) {
				return new Response(401, "Not authorized", "application/json", JsonUtils.format(new JsonObject().put("success", false)));
			}
		}
		JsonObject jsonObject = createJsonObject(request);
		return new Response(200, "OK", "application/json", JsonUtils.format(jsonObject));
	}

}
