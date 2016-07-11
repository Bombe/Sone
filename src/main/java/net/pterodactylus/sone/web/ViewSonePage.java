/*
 * Sone - ViewSonePage.java - Copyright © 2010–2016 David Roden
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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.template.SoneAccessor;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.collection.Pagination;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;

import com.google.common.base.Optional;

/**
 * Lets the user browser another Sone.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ViewSonePage extends SoneTemplatePage {

	/**
	 * Creates a new “view Sone” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public ViewSonePage(Template template, WebInterface webInterface) {
		super("viewSone.html", template, webInterface, false);
	}

	//
	// TEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getPageTitle(FreenetRequest request) {
		String soneId = request.getHttpRequest().getParam("sone");
		Optional<Sone> sone = webInterface.getCore().getSone(soneId);
		if (sone.isPresent()) {
			String soneName = SoneAccessor.getNiceName(sone.get());
			return soneName + " - " + webInterface.getL10n().getString("Page.ViewSone.Title");
		}
		return webInterface.getL10n().getString("Page.ViewSone.Page.TitleWithoutSone");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		super.processTemplate(request, templateContext);
		String soneId = request.getHttpRequest().getParam("sone");
		Optional<Sone> sone = webInterface.getCore().getSone(soneId);
		templateContext.set("sone", sone.orNull());
		templateContext.set("soneId", soneId);
		if (!sone.isPresent()) {
			return;
		}
		List<Post> sonePosts = sone.get().getPosts();
		sonePosts.addAll(webInterface.getCore().getDirectedPosts(sone.get().getId()));
		Collections.sort(sonePosts, Post.TIME_COMPARATOR);
		Pagination<Post> postPagination = new Pagination<Post>(sonePosts, webInterface.getCore().getPreferences().getPostsPerPage()).setPage(parseInt(request.getHttpRequest().getParam("postPage"), 0));
		templateContext.set("postPagination", postPagination);
		templateContext.set("posts", postPagination.getItems());
		Set<PostReply> replies = sone.get().getReplies();
		final Map<Post, List<PostReply>> repliedPosts = new HashMap<Post, List<PostReply>>();
		for (PostReply reply : replies) {
			Optional<Post> post = reply.getPost();
			if (!post.isPresent() || repliedPosts.containsKey(post.get()) || sone.get().equals(post.get().getSone()) || (sone.get().getId().equals(post.get().getRecipientId().orNull()))) {
				continue;
			}
			repliedPosts.put(post.get(), webInterface.getCore().getReplies(post.get().getId()));
		}
		List<Post> posts = new ArrayList<Post>(repliedPosts.keySet());
		Collections.sort(posts, new Comparator<Post>() {

			@Override
			public int compare(Post leftPost, Post rightPost) {
				return (int) Math.min(Integer.MAX_VALUE, Math.max(Integer.MIN_VALUE, repliedPosts.get(rightPost).get(0).getTime() - repliedPosts.get(leftPost).get(0).getTime()));
			}

		});

		Pagination<Post> repliedPostPagination = new Pagination<Post>(posts, webInterface.getCore().getPreferences().getPostsPerPage()).setPage(parseInt(request.getHttpRequest().getParam("repliedPostPage"), 0));
		templateContext.set("repliedPostPagination", repliedPostPagination);
		templateContext.set("repliedPosts", repliedPostPagination.getItems());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLinkExcepted(URI link) {
		return true;
	}

}
