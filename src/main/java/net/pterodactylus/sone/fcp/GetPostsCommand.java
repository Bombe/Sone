/*
 * Sone - GetPostsCommand.java - Copyright © 2011–2015 David Roden
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
import java.util.List;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.fcp.FcpException;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;

/**
 * Implements the “GetPosts” FCP command that returns the list of posts a Sone
 * made.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class GetPostsCommand extends AbstractSoneCommand {

	/**
	 * Creates a new “GetPosts” FCP command.
	 *
	 * @param core
	 *            The Sone core
	 */
	public GetPostsCommand(Core core) {
		super(core);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response execute(SimpleFieldSet parameters, Bucket data, AccessType accessType) throws FcpException {
		Sone sone = getSone(parameters, "Sone", false);
		int startPost = getInt(parameters, "StartPost", 0);
		int maxPosts = getInt(parameters, "MaxPosts", -1);
		List<Post> posts = sone.getPosts();
		if (posts.size() < startPost) {
			return new Response("Posts", encodePosts(Collections.<Post> emptyList(), "Posts.", false));
		}
		return new Response("Posts", encodePosts(sone.getPosts().subList(startPost, (maxPosts == -1) ? posts.size() : Math.min(startPost + maxPosts, posts.size())), "Posts.", true));
	}

}
