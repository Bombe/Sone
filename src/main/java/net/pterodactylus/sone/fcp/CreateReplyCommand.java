/*
 * Sone - CreateReplyCommand.java - Copyright © 2011–2019 David Roden
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
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.SimpleFieldSetBuilder;
import net.pterodactylus.sone.freenet.fcp.FcpException;
import freenet.support.SimpleFieldSet;

/**
 * FCP command that creates a new {@link Reply}.
 *
 * @see Core#createReply(Sone, Post, String)
 */
public class CreateReplyCommand extends AbstractSoneCommand {

	/**
	 * Creates a new “CreateReply” FCP command.
	 *
	 * @param core
	 *            The Sone core
	 */
	public CreateReplyCommand(Core core) {
		super(core, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response execute(SimpleFieldSet parameters) throws FcpException {
		Sone sone = getSone(parameters, "Sone", true);
		Post post = getPost(parameters, "Post");
		String text = getString(parameters, "Text");
		PostReply reply = getCore().createReply(sone, post, text);
		return new Response("ReplyCreated", new SimpleFieldSetBuilder().put("Reply", reply.getId()).get());
	}

}
