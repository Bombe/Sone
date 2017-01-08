/*
 * Sone - DeletePostCommand.java - Copyright © 2011–2016 David Roden
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
import net.pterodactylus.sone.freenet.SimpleFieldSetBuilder;
import net.pterodactylus.sone.freenet.fcp.FcpException;
import freenet.support.SimpleFieldSet;

/**
 * FCP command that deletes a {@link Post}.
 *
 * @see Core#deletePost(Post)
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DeletePostCommand extends AbstractSoneCommand {

	/**
	 * Creates a new “DeletePost” FCP command.
	 *
	 * @param core
	 *            The Sone core
	 */
	public DeletePostCommand(Core core) {
		super(core, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response execute(SimpleFieldSet parameters) throws FcpException {
		Post post = getPost(parameters, "Post");
		if (!post.getSone().isLocal()) {
			return new ErrorResponse(401, "Not allowed.");
		}
		getCore().deletePost(post);
		return new Response("PostDeleted", new SimpleFieldSetBuilder().get());
	}

}
