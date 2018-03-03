/*
 * Sone - DeleteReplyCommand.java - Copyright © 2011–2016 David Roden
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

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.freenet.SimpleFieldSetBuilder;
import net.pterodactylus.sone.freenet.fcp.FcpException;
import freenet.support.SimpleFieldSet;

/**
 * FCP command that deletes a {@link PostReply}.
 *
 * @see Core#deleteReply(PostReply)
 */
public class DeleteReplyCommand extends AbstractSoneCommand {

	/**
	 * Creates a new “DeleteReply” FCP command.
	 *
	 * @param core
	 *            The Sone core
	 */
	public DeleteReplyCommand(Core core) {
		super(core, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response execute(SimpleFieldSet parameters) throws FcpException {
		PostReply reply = getReply(parameters, "Reply");
		if (!reply.getSone().isLocal()) {
			return new ErrorResponse(401, "Not allowed.");
		}
		getCore().deleteReply(reply);
		return new Response("ReplyDeleted", new SimpleFieldSetBuilder().get());
	}

}
