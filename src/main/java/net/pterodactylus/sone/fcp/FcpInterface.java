/*
 * Sone - FcpInterface.java - Copyright © 2011–2015 David Roden
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

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.logging.Logger.getLogger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.fcp.event.FcpInterfaceActivatedEvent;
import net.pterodactylus.sone.fcp.event.FcpInterfaceDeactivatedEvent;
import net.pterodactylus.sone.fcp.event.FullAccessRequiredChanged;
import net.pterodactylus.sone.freenet.fcp.Command.AccessType;
import net.pterodactylus.sone.freenet.fcp.Command.ErrorResponse;
import net.pterodactylus.sone.freenet.fcp.Command.Response;

import freenet.pluginmanager.FredPluginFCP;
import freenet.pluginmanager.PluginNotFoundException;
import freenet.pluginmanager.PluginReplySender;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Implementation of an FCP interface for other clients or plugins to
 * communicate with Sone.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
@Singleton
public class FcpInterface {

	/**
	 * The action level that full access for the FCP connection is required.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public enum FullAccessRequired {

		/** No action requires full access. */
		NO,

		/** All writing actions require full access. */
		WRITING,

		/** All actions require full access. */
		ALWAYS,

	}

	/** The logger. */
	private static final Logger logger = getLogger(FcpInterface.class.getName());

	/** Whether the FCP interface is currently active. */
	private final AtomicBoolean active = new AtomicBoolean();

	/** What function full access is required for. */
	private final AtomicReference<FullAccessRequired> fullAccessRequired = new AtomicReference<FullAccessRequired>(FullAccessRequired.ALWAYS);

	/** All available FCP commands. */
	private final Map<String, AbstractSoneCommand> commands = Collections.synchronizedMap(new HashMap<String, AbstractSoneCommand>());

	/**
	 * Creates a new FCP interface.
	 *
	 * @param core
	 *            The core
	 */
	@Inject
	public FcpInterface(Core core) {
		commands.put("Version", new VersionCommand(core));
		commands.put("GetLocalSones", new GetLocalSonesCommand(core));
		commands.put("GetSones", new GetSonesCommand(core));
		commands.put("GetSone", new GetSoneCommand(core));
		commands.put("GetPost", new GetPostCommand(core));
		commands.put("GetPosts", new GetPostsCommand(core));
		commands.put("GetPostFeed", new GetPostFeedCommand(core));
		commands.put("LockSone", new LockSoneCommand(core));
		commands.put("UnlockSone", new UnlockSoneCommand(core));
		commands.put("LikePost", new LikePostCommand(core));
		commands.put("LikeReply", new LikeReplyCommand(core));
		commands.put("CreatePost", new CreatePostCommand(core));
		commands.put("CreateReply", new CreateReplyCommand(core));
		commands.put("DeletePost", new DeletePostCommand(core));
		commands.put("DeleteReply", new DeleteReplyCommand(core));
	}

	//
	// ACCESSORS
	//

	@VisibleForTesting
	boolean isActive() {
		return active.get();
	}

	private void setActive(boolean active) {
		this.active.set(active);
	}

	@VisibleForTesting
	FullAccessRequired getFullAccessRequired() {
		return fullAccessRequired.get();
	}

	private void setFullAccessRequired(FullAccessRequired fullAccessRequired) {
		this.fullAccessRequired.set(checkNotNull(fullAccessRequired, "fullAccessRequired must not be null"));
	}

	//
	// ACTIONS
	//

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
		if (!active.get()) {
			try {
				sendReply(pluginReplySender, null, new ErrorResponse(400, "FCP Interface deactivated"));
			} catch (PluginNotFoundException pnfe1) {
				logger.log(Level.FINE, "Could not set error to plugin.", pnfe1);
			}
			return;
		}
		AbstractSoneCommand command = commands.get(parameters.get("Message"));
		if ((accessType == FredPluginFCP.ACCESS_FCP_RESTRICTED) && (((fullAccessRequired.get() == FullAccessRequired.WRITING) && command.requiresWriteAccess()) || (fullAccessRequired.get() == FullAccessRequired.ALWAYS))) {
			try {
				sendReply(pluginReplySender, null, new ErrorResponse(401, "Not authorized"));
			} catch (PluginNotFoundException pnfe1) {
				logger.log(Level.FINE, "Could not set error to plugin.", pnfe1);
			}
			return;
		}
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
			} catch (Exception e1) {
				logger.log(Level.WARNING, "Could not process FCP command “%s”.", command);
				sendReply(pluginReplySender, identifier, new ErrorResponse("Error executing command: " + e1.getMessage()));
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
	private static void sendReply(PluginReplySender pluginReplySender, String identifier, Response response) throws PluginNotFoundException {
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

	@Subscribe
	public void fcpInterfaceActivated(FcpInterfaceActivatedEvent fcpInterfaceActivatedEvent) {
		setActive(true);
	}

	@Subscribe
	public void fcpInterfaceDeactivated(FcpInterfaceDeactivatedEvent fcpInterfaceDeactivatedEvent) {
		setActive(false);
	}

	@Subscribe
	public void fullAccessRequiredChanged(FullAccessRequiredChanged fullAccessRequiredChanged) {
		setFullAccessRequired(fullAccessRequiredChanged.getFullAccessRequired());
	}

}
