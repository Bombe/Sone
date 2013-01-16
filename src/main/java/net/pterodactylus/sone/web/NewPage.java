/*
 * Sone - NewPage.java - Copyright © 2012 David Roden
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.notify.ListNotificationFilters;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.collection.Pagination;
import net.pterodactylus.util.number.Numbers;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;

/**
 * Page that displays all new posts and replies. The posts are filtered using
 * {@link ListNotificationFilters#filterPosts(java.util.Collection, net.pterodactylus.sone.data.Sone)}
 * and sorted by time.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class NewPage extends SoneTemplatePage {

	/**
	 * Creates a new “new posts and replies” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public NewPage(Template template, WebInterface webInterface) {
		super("new.html", template, "Page.New.Title", webInterface);
	}

	//
	// SONETEMPLATEPAGE METHODS
	//

	/**
	 * {@inherit}
	 */
	@Override
	protected void processTemplate(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		super.processTemplate(request, templateContext);

		/* collect new elements from notifications. */
		Set<Post> posts = new HashSet<Post>(webInterface.getNewPosts());
		for (PostReply reply : webInterface.getNewReplies()) {
			posts.add(reply.getPost());
		}

		/* filter and sort them. */
		List<Post> sortedPosts = ListNotificationFilters.filterPosts(new ArrayList<Post>(posts), webInterface.getCurrentSone(request.getToadletContext(), false));
		Collections.sort(sortedPosts, Post.TIME_COMPARATOR);

		/* paginate them. */
		Pagination<Post> pagination = new Pagination<Post>(sortedPosts, webInterface.getCore().getPreferences().getPostsPerPage()).setPage(Numbers.safeParseInteger(request.getHttpRequest().getParam("page"), 0));
		templateContext.set("pagination", pagination);
		templateContext.set("posts", pagination.getItems());
	}

}
