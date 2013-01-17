/*
 * Sone - GetReplyAjaxPage.java - Copyright © 2010–2013 David Roden
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

import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.io.Closer;
import net.pterodactylus.util.json.JsonObject;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.template.TemplateException;

/**
 * This AJAX page returns the details of a reply.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class GetReplyAjaxPage extends JsonPage {

	/** The template to render. */
	private final Template replyTemplate;

	/**
	 * Creates a new “get reply” page.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 * @param replyTemplate
	 *            The template to render
	 */
	public GetReplyAjaxPage(WebInterface webInterface, Template replyTemplate) {
		super("getReply.ajax", webInterface);
		this.replyTemplate = replyTemplate;
	}

	//
	// JSONPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JsonObject createJsonObject(FreenetRequest request) {
		String replyId = request.getHttpRequest().getParam("reply");
		PostReply reply = webInterface.getCore().getPostReply(replyId, false);
		if ((reply == null) || (reply.getSone() == null)) {
			return createErrorJsonObject("invalid-reply-id");
		}
		return createSuccessJsonObject().put("reply", createJsonReply(request, reply, getCurrentSone(request.getToadletContext())));
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
	 * Creates a JSON representation of the given reply.
	 *
	 * @param request
	 *            The request being processed
	 * @param reply
	 *            The reply to convert
	 * @param currentSone
	 *            The currently logged in Sone (to store in the template)
	 * @return The JSON representation of the reply
	 */
	private JsonObject createJsonReply(FreenetRequest request, PostReply reply, Sone currentSone) {
		JsonObject jsonReply = new JsonObject();
		jsonReply.put("id", reply.getId());
		jsonReply.put("postId", reply.getPost().getId());
		jsonReply.put("soneId", reply.getSone().getId());
		jsonReply.put("time", reply.getTime());
		StringWriter stringWriter = new StringWriter();
		TemplateContext templateContext = webInterface.getTemplateContextFactory().createTemplateContext();
		templateContext.set("core", webInterface.getCore());
		templateContext.set("request", request);
		templateContext.set("reply", reply);
		templateContext.set("currentSone", currentSone);
		try {
			replyTemplate.render(templateContext, stringWriter);
		} catch (TemplateException te1) {
			/* TODO - shouldn’t happen. */
		} finally {
			Closer.close(stringWriter);
		}
		return jsonReply.put("html", stringWriter.toString());
	}

}
