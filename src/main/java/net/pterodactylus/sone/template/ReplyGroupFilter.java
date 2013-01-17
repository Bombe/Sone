/*
 * Sone - ReplyGroupFilter.java - Copyright © 2010–2013 David Roden
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
import java.util.Set;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.util.template.Filter;
import net.pterodactylus.util.template.TemplateContext;

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
			Post post = reply.getPost();
			Set<Sone> sones = postSones.get(post);
			if (sones == null) {
				sones = new HashSet<Sone>();
				postSones.put(post, sones);
			}
			sones.add(reply.getSone());
			Set<PostReply> replies = postReplies.get(post);
			if (replies == null) {
				replies = new HashSet<PostReply>();
				postReplies.put(post, replies);
			}
			replies.add(reply);
		}
		Map<Post, Map<String, Set<?>>> result = new HashMap<Post, Map<String, Set<?>>>();
		for (Post post : postSones.keySet()) {
			if (result.containsKey(post)) {
				continue;
			}
			Map<String, Set<?>> postResult = new HashMap<String, Set<?>>();
			postResult.put("sones", postSones.get(post));
			postResult.put("replies", postReplies.get(post));
			result.put(post, postResult);
		}
		return result;
	}

}
