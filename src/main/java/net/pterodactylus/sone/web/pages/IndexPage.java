/*
 * Sone - IndexPage.java - Copyright © 2010–2016 David Roden
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

package net.pterodactylus.sone.web.pages;

import static net.pterodactylus.sone.utils.NumberParsers.parseInt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.notify.PostVisibilityFilter;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.collection.Pagination;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;

import com.google.common.base.Optional;
import com.google.common.collect.Collections2;

/**
 * The index page shows the main page of Sone. This page will contain the posts
 * of all friends of the current user.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class IndexPage extends SoneTemplatePage {

	private final PostVisibilityFilter postVisibilityFilter;

	public IndexPage(Template template, WebInterface webInterface, PostVisibilityFilter postVisibilityFilter) {
		super("index.html", template, "Page.Index.Title", webInterface, true);
		this.postVisibilityFilter = postVisibilityFilter;
	}

	//
	// TEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void handleRequest(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		final Sone currentSone = getCurrentSone(request.getToadletContext());
		Collection<Post> allPosts = new ArrayList<Post>();
		allPosts.addAll(currentSone.getPosts());
		for (String friendSoneId : currentSone.getFriends()) {
			Optional<Sone> friendSone = webInterface.getCore().getSone(friendSoneId);
			if (!friendSone.isPresent()) {
				continue;
			}
			allPosts.addAll(friendSone.get().getPosts());
		}
		for (Sone sone : webInterface.getCore().getSones()) {
			for (Post post : sone.getPosts()) {
				if (currentSone.equals(post.getRecipient().orNull()) && !allPosts.contains(post)) {
					allPosts.add(post);
				}
			}
		}
		allPosts = Collections2.filter(allPosts, postVisibilityFilter.isVisible(currentSone));
		List<Post> sortedPosts = new ArrayList<Post>(allPosts);
		Collections.sort(sortedPosts, Post.NEWEST_FIRST);
		Pagination<Post> pagination = new Pagination<Post>(sortedPosts, webInterface.getCore().getPreferences().getPostsPerPage()).setPage(parseInt(request.getHttpRequest().getParam("page"), 0));
		templateContext.set("pagination", pagination);
		templateContext.set("posts", pagination.getItems());
	}

}
