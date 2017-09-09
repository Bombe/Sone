/*
 * Sone - UnfollowSoneAjaxPage.java - Copyright © 2010–2016 David Roden
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

import javax.annotation.Nonnull;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.sone.web.page.FreenetRequest;

/**
 * AJAX page that lets a Sone unfollow another Sone.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class UnfollowSoneAjaxPage extends LoggedInJsonPage {

	/**
	 * Creates a new “unfollow Sone” AJAX page.
	 *
	 * @param webInterface
	 *            The Sone web interface
	 */
	public UnfollowSoneAjaxPage(WebInterface webInterface) {
		super("unfollowSone.ajax", webInterface);
	}

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	protected JsonReturnObject createJsonObject(@Nonnull Sone currentSone, @Nonnull FreenetRequest request) {
		String soneId = request.getHttpRequest().getParam("sone");
		if (!webInterface.getCore().getSone(soneId).isPresent()) {
			return createErrorJsonObject("invalid-sone-id");
		}
		webInterface.getCore().unfollowSone(currentSone, soneId);
		return createSuccessJsonObject();
	}

}
