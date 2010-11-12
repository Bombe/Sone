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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.template.SoneAccessor;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.util.json.JsonObject;

/**
 * This AJAX page returns the details of a reply.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class GetReplyAjaxPage extends JsonPage {

	/** Date formatter. */
	private static final DateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy, HH:mm:ss");

	/**
	 * Creates a new “get reply” page.
	 *
	 * @param webInterface
	 */
	public GetReplyAjaxPage(WebInterface webInterface) {
		super("ajax/getReply.ajax", webInterface);
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
		synchronized (dateFormat) {
			return new JsonObject().put("success", true).put("sone-id", reply.getSone().getId()).put("sone-name", SoneAccessor.getNiceName(reply.getSone())).put("time", reply.getTime()).put("display-time", dateFormat.format(new Date(reply.getTime()))).put("text", reply.getText());
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
