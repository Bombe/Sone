/*
 * Sone - ReplyGroupFilter.java - Copyright © 2010–2016 David Roden
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

package net.pterodactylus.sone.template;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.util.template.Filter;
import net.pterodactylus.util.template.TemplateContext;

import com.google.common.base.Optional;

/**
 * {@link Filter} implementation that groups replies by the post the are in
 * reply to, returning a map with the post as key and the list of replies as
 * values.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ReplyGroupFilter implements Filter {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object format(TemplateContext templateContext, Object data, Map<String, Object> parameters) {
		@SuppressWarnings("unchecked")
		List<PostReply> allReplies = (List<PostReply>) data;
		Map<Post, Set<Sone>> postSones = new HashMap<Post, Set<Sone>>();
		Map<Post, Set<PostReply>> postReplies = new HashMap<Post, Set<PostReply>>();
		for (PostReply reply : allReplies) {
			/*
			 * All replies from a new-reply notification have posts,
			 * ListNotificationFilters takes care of that.
			 */
			Optional<Post> post = reply.getPost();
			Set<Sone> sones = postSones.get(post.get());
			if (sones == null) {
				sones = new HashSet<Sone>();
				postSones.put(post.get(), sones);
			}
			sones.add(reply.getSone());
			Set<PostReply> replies = postReplies.get(post.get());
			if (replies == null) {
				replies = new HashSet<PostReply>();
				postReplies.put(post.get(), replies);
			}
			replies.add(reply);
		}
		Map<Post, Map<String, Set<?>>> result = new HashMap<Post, Map<String, Set<?>>>();
		for (Entry<Post, Set<Sone>> postEntry : postSones.entrySet()) {
			if (result.containsKey(postEntry.getKey())) {
				continue;
			}
			Map<String, Set<?>> postResult = new HashMap<String, Set<?>>();
			postResult.put("sones", postEntry.getValue());
			postResult.put("replies", postReplies.get(postEntry.getKey()));
			result.put(postEntry.getKey(), postResult);
		}
		return result;
	}

}
