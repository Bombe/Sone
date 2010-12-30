/*
 * FreenetSone - IndexPage.java - Copyright © 2010 David Roden
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

package net.pterodactylus.sone.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.util.template.Template;

/**
 * The index page shows the main page of Sone. This page will contain the posts
 * of all friends of the current user.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class IndexPage extends SoneTemplatePage {

	/**
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public IndexPage(Template template, WebInterface webInterface) {
		super("index.html", template, "Page.Index.Title", webInterface, true);
	}

	//
	// TEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(Request request, Template template) throws RedirectException {
		super.processTemplate(request, template);
		Sone currentSone = getCurrentSone(request.getToadletContext());
		List<Post> allPosts = new ArrayList<Post>();
		allPosts.addAll(currentSone.getPosts());
		for (String friendSoneId : currentSone.getFriends()) {
			if (!webInterface.getCore().hasSone(friendSoneId)) {
				continue;
			}
			allPosts.addAll(webInterface.getCore().getSone(friendSoneId).getPosts());
		}
		for (Sone sone : webInterface.getCore().getSones()) {
			for (Post post : sone.getPosts()) {
				if (currentSone.equals(post.getRecipient()) && !allPosts.contains(post)) {
					allPosts.add(post);
				}
			}
		}
		Collections.sort(allPosts, Post.TIME_COMPARATOR);
		template.set("posts", allPosts);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void postProcess(Request request, Template template) {
		@SuppressWarnings("unchecked")
		List<Post> posts = (List<Post>) template.get("posts");
		for (Post post : posts) {
			webInterface.getCore().markPostKnown(post);
			for (Reply reply : webInterface.getCore().getReplies(post)) {
				webInterface.getCore().markReplyKnown(reply);
			}
		}
	}

}
