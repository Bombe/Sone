/*
 * Sone - FcpInterface.java - Copyright © 2011–2019 David Roden
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
import static net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.NO;
import static net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.WRITING;
import static net.pterodactylus.sone.freenet.fcp.Command.AccessType.RESTRICTED_FCP;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

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

/**
 * Implementation of an FCP interface for other clients or plugins to
 * communicate with Sone.
 */
@Singleton
public class FcpInterface {

	/**
	 * The action level that full access for the FCP connection is required.
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
	private final AtomicReference<FullAccessRequired> fullAccessRequired = new AtomicReference<>(FullAccessRequired.ALWAYS);

	/** All available FCP commands. */
	private final Map<String, AbstractSoneCommand> commands;
	private final AccessAuthorizer accessAuthorizer;

	/**
	 * Creates a new FCP interface.
	 *
	 * @param core
	 *            The core
	 */
	@Inject
	public FcpInterface(Core core, CommandSupplier commandSupplier, AccessAuthorizer accessAuthorizer) {
		commands = commandSupplier.supplyCommands(core);
		this.accessAuthorizer = accessAuthorizer;
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
		String identifier = parameters.get("Identifier");
		if ((identifier == null) || (identifier.length() == 0)) {
			sendErrorReply(pluginReplySender, null, 400, "Missing Identifier.");
			return;
		}
		if (!active.get()) {
			sendErrorReply(pluginReplySender, identifier, 503, "FCP Interface deactivated");
			return;
		}
		AbstractSoneCommand command = commands.get(parameters.get("Message"));
		if (command == null) {
			sendErrorReply(pluginReplySender, identifier, 404, "Unrecognized Message: " + parameters.get("Message"));
			return;
		}
		if (!accessAuthorizer.authorized(AccessType.values()[accessType], fullAccessRequired.get(), command.requiresWriteAccess())) {
			sendErrorReply(pluginReplySender, identifier, 401, "Not authorized");
			return;
		}
		try {
			Response response = command.execute(parameters);
			sendReply(pluginReplySender, identifier, response);
		} catch (Exception e1) {
			logger.log(Level.WARNING, "Could not process FCP command “%s”.", command);
			sendErrorReply(pluginReplySender, identifier, 500, "Error executing command: " + e1.getMessage());
		}
	}

	private void sendErrorReply(PluginReplySender pluginReplySender, String identifier, int errorCode, String message) {
		try {
			sendReply(pluginReplySender, identifier, new ErrorResponse(errorCode, message));
		} catch (PluginNotFoundException pnfe1) {
			logger.log(Level.FINE, "Could not send error to plugin.", pnfe1);
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
		pluginReplySender.send(replyParameters);
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

	@Singleton
	public static class CommandSupplier {

		public Map<String, AbstractSoneCommand> supplyCommands(Core core) {
			Map<String, AbstractSoneCommand> commands = new HashMap<>();
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
			return commands;
		}

	}

	@Singleton
	public static class AccessAuthorizer {

		public boolean authorized(@Nonnull AccessType accessType, @Nonnull FullAccessRequired fullAccessRequired, boolean commandRequiresWriteAccess) {
			return (accessType != RESTRICTED_FCP) || (fullAccessRequired == NO) || ((fullAccessRequired == WRITING) && !commandRequiresWriteAccess);
		}

	}

}
