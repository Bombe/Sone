/*
 * Sone - FcpInterface.java - Copyright © 2011 David Roden
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

package net.pterodactylus.sone.fcp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.freenet.fcp.Command;
import net.pterodactylus.sone.freenet.fcp.Command.AccessType;
import net.pterodactylus.sone.freenet.fcp.Command.ErrorResponse;
import net.pterodactylus.sone.freenet.fcp.Command.Response;
import net.pterodactylus.sone.freenet.fcp.FcpException;
import net.pterodactylus.util.logging.Logging;
import freenet.pluginmanager.FredPluginFCP;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginReplySender;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;

/**
 * Implementation of an FCP interface for other clients or plugins to
 * communicate with Sone.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FcpInterface {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(FcpInterface.class);

	/** All available FCP commands. */
	private final Map<String, Command> commands = Collections.synchronizedMap(new HashMap<String, Command>());

	/**
	 * Creates a new FCP interface.
	 *
	 * @param core
	 *            The core
	 */
	public FcpInterface(Core core) {
		commands.put("Version", new VersionCommand());
		commands.put("GetLocalSones", new GetLocalSonesCommand(core));
		commands.put("GetPosts", new GetPostsCommand(core));
		commands.put("GetPostFeed", new GetPostFeedCommand(core));
	}

	/**
	 * Handles a plugin FCP request.
	 *
	 * @param pluginReplySender
	 *            The reply sender
	 * @param parameters
	 *            The message parameters
	 * @param data
	 *            The message data (may be {@code null})
	 * @param accessType
	 *            One of {@link FredPluginFCP#ACCESS_DIRECT},
	 *            {@link FredPluginFCP#ACCESS_FCP_FULL},
	 *            {@link FredPluginFCP#ACCESS_FCP_RESTRICTED}
	 */
	public void handle(PluginReplySender pluginReplySender, SimpleFieldSet parameters, Bucket data, int accessType) {
		Command command = commands.get(parameters.get("Message"));
		try {
			if (command == null) {
				sendReply(pluginReplySender, null, new ErrorResponse("Unrecognized Message: " + parameters.get("Message")));
				return;
			}
			String identifier = parameters.get("Identifier");
			if ((identifier == null) || (identifier.length() == 0)) {
				sendReply(pluginReplySender, null, new ErrorResponse("Missing Identifier."));
				return;
			}
			try {
				Response response = command.execute(parameters, data, AccessType.values()[accessType]);
				sendReply(pluginReplySender, identifier, response);
			} catch (FcpException fe1) {
				sendReply(pluginReplySender, identifier, new ErrorResponse("Error executing command: " + fe1.getMessage()));
			}
		} catch (PluginNotFoundException pnfe1) {
			logger.log(Level.WARNING, "Could not find destination plugin: " + pluginReplySender);
		}
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Sends the given response to the given plugin.
	 *
	 * @param pluginReplySender
	 *            The reply sender
	 * @param identifier
	 *            The identifier (may be {@code null})
	 * @param response
	 *            The response to send
	 * @throws PluginNotFoundException
	 *             if the plugin can not be found
	 */
	private void sendReply(PluginReplySender pluginReplySender, String identifier, Response response) throws PluginNotFoundException {
		SimpleFieldSet replyParameters = response.getReplyParameters();
		if (identifier != null) {
			replyParameters.putOverwrite("Identifier", identifier);
		}
		if (response.hasData()) {
			pluginReplySender.send(replyParameters, response.getData());
		} else if (response.hasBucket()) {
			pluginReplySender.send(replyParameters, response.getBucket());
		} else {
			pluginReplySender.send(replyParameters);
		}
	}

}
