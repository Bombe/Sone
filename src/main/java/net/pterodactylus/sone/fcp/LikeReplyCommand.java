/*
 * Sone - LikeReplyCommand.java - Copyright © 2011 David Roden
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
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.SimpleFieldSetBuilder;
import net.pterodactylus.sone.freenet.fcp.FcpException;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;

/**
 * Implements the “LikeReply” FCP command which allows the user to like a reply.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class LikeReplyCommand extends AbstractSoneCommand {

	/**
	 * Creates a new “LikeReply” FCP command.
	 *
	 * @param core
	 *            The Sone core
	 */
	protected LikeReplyCommand(Core core) {
		super(core, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response execute(SimpleFieldSet parameters, Bucket data, AccessType accessType) throws FcpException {
		PostReply reply = getReply(parameters, "Reply");
		Sone sone = getSone(parameters, "Sone", true);
		sone.addLikedReplyId(reply.getId());
		return new Response("ReplyLiked", new SimpleFieldSetBuilder().put("LikeCount", getCore().getLikes(reply).size()).get());
	}

}
