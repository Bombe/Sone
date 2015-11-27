/*
 * Sone - GetPostFeedCommand.java - Copyright © 2011–2015 David Roden
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.fcp.FcpException;

import com.google.common.base.Optional;
import com.google.common.collect.Collections2;

import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;

/**
 * Implementation of an FCP interface for other clients or plugins to
 * communicate with Sone.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class GetPostFeedCommand extends AbstractSoneCommand {

	/**
	 * Creates a new “GetPostFeed” command.
	 *
	 * @param core
	 *            The core
	 */
	public GetPostFeedCommand(Core core) {
		super(core);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response execute(SimpleFieldSet parameters, Bucket data, AccessType accessType) throws FcpException {
		Sone sone = getSone(parameters, "Sone", true);
		int startPost = getInt(parameters, "StartPost", 0);
		int maxPosts = getInt(parameters, "MaxPosts", -1);

		Collection<Post> allPosts = new HashSet<Post>();
		allPosts.addAll(sone.getPosts());
		for (String friendSoneId : sone.getFriends()) {
			Optional<Sone> friendSone = getCore().getSone(friendSoneId);
			if (!friendSone.isPresent()) {
				continue;
			}
			allPosts.addAll(friendSone.get().getPosts());
		}
		allPosts.addAll(getCore().getDirectedPosts(sone.getId()));
		allPosts = Collections2.filter(allPosts, Post.FUTURE_POSTS_FILTER);

		List<Post> sortedPosts = new ArrayList<Post>(allPosts);
		Collections.sort(sortedPosts, Post.TIME_COMPARATOR);

		if (sortedPosts.size() < startPost) {
			return new Response("PostFeed", encodePosts(Collections.<Post> emptyList(), "Posts.", false));
		}

		return new Response("PostFeed", encodePosts(sortedPosts.subList(startPost, (maxPosts == -1) ? sortedPosts.size() : Math.min(startPost + maxPosts, sortedPosts.size())), "Posts.", true));
	}

}
