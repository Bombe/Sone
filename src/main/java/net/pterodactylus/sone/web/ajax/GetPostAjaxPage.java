/*
 * Sone - GetPostAjaxPage.java - Copyright © 2010–2013 David Roden
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

package net.pterodactylus.sone.web.ajax;

import java.io.StringWriter;

import com.google.common.base.Optional;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.io.Closer;
import net.pterodactylus.util.json.JsonObject;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.template.TemplateException;

/**
 * This AJAX handler retrieves information and rendered representation of a
 * {@link Post}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class GetPostAjaxPage extends JsonPage {

	/** The template to render for posts. */
	private final Template postTemplate;

	/**
	 * Creates a new “get post” AJAX handler.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 * @param postTemplate
	 *            The template to render for posts
	 */
	public GetPostAjaxPage(WebInterface webInterface, Template postTemplate) {
		super("getPost.ajax", webInterface);
		this.postTemplate = postTemplate;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JsonObject createJsonObject(FreenetRequest request) {
		String postId = request.getHttpRequest().getParam("post");
		Optional<Post> post = webInterface.getCore().getPost(postId);
		if (!post.isPresent()) {
			return createErrorJsonObject("invalid-post-id");
		}
		return createSuccessJsonObject().put("post", createJsonPost(request, post.get(), getCurrentSone(request.getToadletContext())));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean needsFormPassword() {
		return false;
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Creates a JSON object from the given post. The JSON object will only
	 * contain the ID of the post, its time, and its rendered HTML code.
	 *
	 * @param request
	 *            The request being processed
	 * @param post
	 *            The post to create a JSON object from
	 * @param currentSone
	 *            The currently logged in Sone (to store in the template)
	 * @return The JSON representation of the post
	 */
	private JsonObject createJsonPost(FreenetRequest request, Post post, Sone currentSone) {
		JsonObject jsonPost = new JsonObject();
		jsonPost.put("id", post.getId());
		jsonPost.put("sone", post.getSone().getId());
		jsonPost.put("recipient", post.getRecipientId().orNull());
		jsonPost.put("time", post.getTime());
		StringWriter stringWriter = new StringWriter();
		TemplateContext templateContext = webInterface.getTemplateContextFactory().createTemplateContext();
		templateContext.set("core", webInterface.getCore());
		templateContext.set("request", request);
		templateContext.set("post", post);
		templateContext.set("currentSone", currentSone);
		templateContext.set("localSones", webInterface.getCore().getLocalSones());
		try {
			postTemplate.render(templateContext, stringWriter);
		} catch (TemplateException te1) {
			/* TODO - shouldn’t happen. */
		} finally {
			Closer.close(stringWriter);
		}
		jsonPost.put("html", stringWriter.toString());
		return jsonPost;
	}

}
