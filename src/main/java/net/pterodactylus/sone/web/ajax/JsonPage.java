/*
 * Sone - JsonPage.java - Copyright © 2010–2016 David Roden
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

import static java.util.logging.Logger.getLogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.SessionProvider;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.io.Closer;
import net.pterodactylus.util.web.Page;
import net.pterodactylus.util.web.Response;

import com.fasterxml.jackson.databind.ObjectMapper;
import freenet.clients.http.ToadletContext;

/**
 * A JSON page is a specialized {@link Page} that will always return a JSON
 * object to the browser, e.g. for use with AJAX or other scripting frameworks.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public abstract class JsonPage implements Page<FreenetRequest> {

	/** The logger. */
	private static final Logger logger = getLogger(JsonPage.class.getName());

	/** The JSON serializer. */
	private static final ObjectMapper objectMapper = new ObjectMapper();

	/** The path of the page. */
	private final String path;

	/** The Sone web interface. */
	protected final WebInterface webInterface;
	private final SessionProvider sessionProvider;

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
		this.sessionProvider = webInterface;
	}

	//
	// ACCESSORS
	//

	protected Sone getCurrentSone(ToadletContext toadletContext) {
		return sessionProvider.getCurrentSone(toadletContext, true);
	}

	protected Sone getCurrentSone(ToadletContext toadletContext, boolean createSession) {
		return sessionProvider.getCurrentSone(toadletContext, createSession);
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
	@Nonnull
	protected abstract JsonReturnObject createJsonObject(@Nonnull FreenetRequest request);

	/**
	 * Returns whether this command needs the form password for authentication
	 * and to prevent abuse.
	 *
	 * @return {@code true} if the form password (given as “formPassword”) is
	 *         required, {@code false} otherwise
	 */
	@SuppressWarnings("static-method")
	protected boolean needsFormPassword() {
		return true;
	}

	/**
	 * Returns whether this page requires the user to be logged in.
	 *
	 * @return {@code true} if the user needs to be logged in to use this page,
	 *         {@code false} otherwise
	 */
	@SuppressWarnings("static-method")
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
	@Nonnull
	protected static JsonReturnObject createSuccessJsonObject() {
		return new JsonReturnObject(true);
	}

	/**
	 * Creates an error reply.
	 *
	 * @param error
	 *            The error that has occured
	 * @return The JSON object, signalling failure and the error code
	 */
	@Nonnull
	protected static JsonReturnObject createErrorJsonObject(String error) {
		return new JsonErrorReturnObject(error);
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
			return response.setStatusCode(403).setStatusText("Forbidden").setContentType("application/json").write(objectMapper.writeValueAsString(new JsonErrorReturnObject("auth-required")));
		}
		if (needsFormPassword()) {
			String formPassword = request.getHttpRequest().getParam("formPassword");
			if (!webInterface.getFormPassword().equals(formPassword)) {
				return response.setStatusCode(403).setStatusText("Forbidden").setContentType("application/json").write(objectMapper.writeValueAsString(new JsonErrorReturnObject("auth-required")));
			}
		}
		if (requiresLogin()) {
			if (getCurrentSone(request.getToadletContext(), false) == null) {
				return response.setStatusCode(403).setStatusText("Forbidden").setContentType("application/json").write(objectMapper.writeValueAsString(new JsonErrorReturnObject("auth-required")));
			}
		}
		try {
			JsonReturnObject jsonObject = createJsonObject(request);
			return response.setStatusCode(200).setStatusText("OK").setContentType("application/json").write(objectMapper.writeValueAsString(jsonObject));
		} catch (Exception e1) {
			logger.log(Level.WARNING, "Error executing JSON page!", e1);
			return response.setStatusCode(500).setStatusText(e1.getMessage()).setContentType("text/plain").write(dumpStackTrace(e1));
		}
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Returns a byte array containing the stack trace of the given throwable.
	 *
	 * @param t
	 *            The throwable whose stack trace to dump into an array
	 * @return The array with the stack trace, or an empty array if the stack
	 *         trace could not be dumped
	 */
	private static byte[] dumpStackTrace(Throwable t) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		OutputStreamWriter writer = null;
		PrintWriter printWriter = null;
		try {
			writer = new OutputStreamWriter(byteArrayOutputStream, "UTF-8");
			printWriter = new PrintWriter(writer);
			t.printStackTrace(printWriter);
			printWriter.flush();
			return byteArrayOutputStream.toByteArray();
		} catch (IOException ioe1) {
			/* quite not possible. */
			return new byte[0];
		} finally {
			Closer.close(printWriter);
			Closer.close(writer);
			Closer.close(byteArrayOutputStream);
		}
	}

}
