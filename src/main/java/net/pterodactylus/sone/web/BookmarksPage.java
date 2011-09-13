/*
 * Sone - BookmarksPage.java - Copyright © 2011 David Roden
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
import java.util.Set;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.collection.Pagination;
import net.pterodactylus.util.filter.Filter;
import net.pterodactylus.util.filter.Filters;
import net.pterodactylus.util.number.Numbers;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;

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
	protected void processTemplate(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		super.processTemplate(request, templateContext);
		Set<Post> allPosts = webInterface.getCore().getBookmarkedPosts();
		Set<Post> loadedPosts = Filters.filteredSet(allPosts, new Filter<Post>() {

			@Override
			public boolean filterObject(Post post) {
				return post.getSone() != null;
			}
		});
		List<Post> sortedPosts = new ArrayList<Post>(loadedPosts);
		Collections.sort(sortedPosts, Post.TIME_COMPARATOR);
		Pagination<Post> pagination = new Pagination<Post>(sortedPosts, 25).setPage(Numbers.safeParseInteger(request.getHttpRequest().getParam("page"), 0));
		templateContext.set("pagination", pagination);
		templateContext.set("posts", pagination.getItems());
		templateContext.set("postsNotLoaded", allPosts.size() != loadedPosts.size());
	}

}
