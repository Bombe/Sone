/*
 * Sone - Command.java - Copyright © 2011–2016 David Roden
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

package net.pterodactylus.sone.freenet.fcp;

import net.pterodactylus.sone.freenet.SimpleFieldSetBuilder;
import freenet.support.SimpleFieldSet;

/**
 * Implementation of an FCP interface for other clients or plugins to
 * communicate with Sone.
 */
public interface Command {

	/**
	 * Executes the command, returning a reply that will be sent back to the
	 * requesting plugin.
	 *
	 * @param parameters
	 *            The parameters of the comand
	 * @return A reply to send back to the plugin
	 * @throws FcpException
	 *             if an error processing the parameters occurs
	 */
	public Response execute(SimpleFieldSet parameters) throws FcpException;

	/**
	 * The access type of the request.
	 */
	public static enum AccessType {

		/** Access from another plugin. */
		DIRECT,

		/** Access via restricted FCP. */
		RESTRICTED_FCP,

		/** Access via FCP with full access. */
		FULL_FCP,

	}

	/**
	 * Interface for command replies.
	 */
	public static class Response {

		/** The message name of the reponse. */
		private final String messageName;

		/** The reply parameters. */
		private final SimpleFieldSet replyParameters;

		/**
		 * Creates a new reply with the given parameters.
		 *
		 * @param messageName
		 *            The message name
		 * @param replyParameters
		 *            The reply parameters
		 */
		public Response(String messageName, SimpleFieldSet replyParameters) {
			this.messageName = messageName;
			this.replyParameters = replyParameters;
		}

		/**
		 * Returns the reply parameters.
		 *
		 * @return The reply parameters
		 */
		public SimpleFieldSet getReplyParameters() {
			return new SimpleFieldSetBuilder(replyParameters).put("Message", messageName).get();
		}

	}

	/**
	 * Response implementation that can return an error message and an optional
	 * error code.
	 */
	public class ErrorResponse extends Response {

		/**
		 * Creates a new error response with the given message.
		 *
		 * @param message
		 *            The error message
		 */
		public ErrorResponse(String message) {
			super("Error", new SimpleFieldSetBuilder().put("ErrorMessage", message).get());
		}

		/**
		 * Creates a new error response with the given code and message.
		 *
		 * @param code
		 *            The error code
		 * @param message
		 *            The error message
		 */
		public ErrorResponse(int code, String message) {
			super("Error", new SimpleFieldSetBuilder().put("ErrorMessage", message).put("ErrorCode", code).get());
		}

	}

}
