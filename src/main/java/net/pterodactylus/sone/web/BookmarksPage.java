/*
 * Sone - BookmarksPage.java - Copyright © 2011–2016 David Roden
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

import static net.pterodactylus.sone.utils.NumberParsers.parseInt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.collection.Pagination;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * Page that lets the user browse all his bookmarked posts.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class BookmarksPage extends SoneTemplatePage {

	/**
	 * Creates a new bookmarks page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public BookmarksPage(Template template, WebInterface webInterface) {
		super("bookmarks.html", template, "Page.Bookmarks.Title", webInterface);
	}

	//
	// SONETEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void handleRequest(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		Set<Post> allPosts = webInterface.getCore().getBookmarkedPosts();
		Collection<Post> loadedPosts = Collections2.filter(allPosts, new Predicate<Post>() {

			@Override
			public boolean apply(Post post) {
				return post.isLoaded();
			}
		});
		List<Post> sortedPosts = new ArrayList<Post>(loadedPosts);
		Collections.sort(sortedPosts, Post.TIME_COMPARATOR);
		Pagination<Post> pagination = new Pagination<Post>(sortedPosts, webInterface.getCore().getPreferences().getPostsPerPage()).setPage(parseInt(request.getHttpRequest().getParam("page"), 0));
		templateContext.set("pagination", pagination);
		templateContext.set("posts", pagination.getItems());
		templateContext.set("postsNotLoaded", allPosts.size() != loadedPosts.size());
	}

}
