/*
 * Sone - CreatePostCommand.java - Copyright © 2011–2016 David Roden
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

import com.google.common.base.Optional;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.SimpleFieldSetBuilder;
import net.pterodactylus.sone.freenet.fcp.FcpException;
import freenet.support.SimpleFieldSet;

/**
 * FCP command that creates a new {@link Post}.
 *
 * @see Core#createPost(Sone, Sone, String)
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class CreatePostCommand extends AbstractSoneCommand {

	/**
	 * Creates a new “CreatePost” FCP command.
	 *
	 * @param core
	 *            The Sone core
	 */
	public CreatePostCommand(Core core) {
		super(core, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response execute(SimpleFieldSet parameters) throws FcpException {
		Sone sone = getSone(parameters, "Sone", true);
		String text = getString(parameters, "Text");
		Sone recipient = null;
		if (parameters.get("Recipient") != null) {
			recipient = getSone(parameters, "Recipient", false);
		}
		if (sone.equals(recipient)) {
			return new ErrorResponse("Sone and Recipient must not be the same.");
		}
		Post post = getCore().createPost(sone, Optional.fromNullable(recipient), text);
		return new Response("PostCreated", new SimpleFieldSetBuilder().put("Post", post.getId()).get());
	}

}
