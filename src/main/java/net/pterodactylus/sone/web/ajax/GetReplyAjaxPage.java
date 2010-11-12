/*
 * Sone - GetReplyAjaxPage.java - Copyright © 2010 David Roden
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.template.SoneAccessor;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.util.io.Closer;
import net.pterodactylus.util.json.JsonObject;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateException;

/**
 * This AJAX page returns the details of a reply.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class GetReplyAjaxPage extends JsonPage {

	/** Date formatter. */
	private static final DateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy, HH:mm:ss");

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
		super("ajax/getReply.ajax", webInterface);
		this.replyTemplate = replyTemplate;
	}

	//
	// JSONPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JsonObject createJsonObject(Request request) {
		String replyId = request.getHttpRequest().getParam("reply");
		Reply reply = webInterface.getCore().getReply(replyId);
		if ((reply == null) || (reply.getSone() == null)) {
			return createErrorJsonObject("invalid-reply-id");
		}
		replyTemplate.set("reply", reply);
		replyTemplate.set("currentSone", getCurrentSone(request.getToadletContext()));
		StringWriter templateWriter = new StringWriter();
		try {
			replyTemplate.render(templateWriter);
		} catch (TemplateException te1) {
			/* TODO - shouldn’t happen. */
		} finally {
			Closer.close(templateWriter);
		}
		synchronized (dateFormat) {
			return new JsonObject().put("success", true).put("soneId", reply.getSone().getId()).put("soneName", SoneAccessor.getNiceName(reply.getSone())).put("time", reply.getTime()).put("displayTime", dateFormat.format(new Date(reply.getTime()))).put("text", reply.getText()).put("html", templateWriter.toString());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean needsFormPassword() {
		return false;
	}

}
