/*
 * Sone - GetSoneStatusPage.java - Copyright © 2010 David Roden
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

import java.text.SimpleDateFormat;
import java.util.Date;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.core.Core.SoneStatus;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.util.json.JsonObject;

/**
 * AJAX page that reeturns the status of a sone, as a) a {@link SoneStatus} name
 * and b) a “modified” boolean (as per {@link Sone#getModificationCounter()}).
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class GetSoneStatusPage extends JsonPage {

	/** The Sone core. */
	private final Core core;

	/**
	 * Creates a new AJAX sone status handler.
	 *
	 * @param core
	 *            The Sone core
	 */
	public GetSoneStatusPage(Core core) {
		super("ajax/getSoneStatus.ajax");
		this.core = core;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected JsonObject createJsonObject(Request request) {
		String soneId = request.getHttpRequest().getParam("sone");
		Sone sone = core.getSone(soneId);
		SoneStatus soneStatus = core.getSoneStatus(sone);
		return new JsonObject().put("status", soneStatus.name()).put("modified", sone.getModificationCounter() > 0).put("lastUpdated", new SimpleDateFormat("MMM d, yyyy, HH:mm:ss").format(new Date(sone.getTime()))).put("age", (System.currentTimeMillis() - sone.getTime()) / 1000);
	}

}
