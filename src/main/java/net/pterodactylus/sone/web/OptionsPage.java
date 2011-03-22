/*
 * Sone - OptionsPage.java - Copyright © 2010 David Roden
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

import net.pterodactylus.sone.core.Core.Preferences;
import net.pterodactylus.sone.web.page.Page.Request.Method;
import net.pterodactylus.util.number.Numbers;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;

/**
 * This page lets the user edit the options of the Sone plugin.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class OptionsPage extends SoneTemplatePage {

	/**
	 * Creates a new options page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public OptionsPage(Template template, WebInterface webInterface) {
		super("options.html", template, "Page.Options.Title", webInterface, false);
	}

	//
	// TEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(Request request, TemplateContext templateContext) throws RedirectException {
		super.processTemplate(request, templateContext);
		Preferences preferences = webInterface.getCore().getPreferences();
		if (request.getMethod() == Method.POST) {
			Integer insertionDelay = Numbers.safeParseInteger(request.getHttpRequest().getPartAsStringFailsafe("insertion-delay", 16));
			preferences.setInsertionDelay(insertionDelay);
			Integer positiveTrust = Numbers.safeParseInteger(request.getHttpRequest().getPartAsStringFailsafe("positive-trust", 3));
			preferences.setPositiveTrust(positiveTrust);
			Integer negativeTrust = Numbers.safeParseInteger(request.getHttpRequest().getPartAsStringFailsafe("negative-trust", 4));
			preferences.setNegativeTrust(negativeTrust);
			String trustComment = request.getHttpRequest().getPartAsStringFailsafe("trust-comment", 256);
			if (trustComment.trim().length() == 0) {
				trustComment = null;
			}
			preferences.setTrustComment(trustComment);
			boolean soneRescueMode = Boolean.parseBoolean(request.getHttpRequest().getPartAsStringFailsafe("sone-rescue-mode", 5));
			preferences.setSoneRescueMode(soneRescueMode);
			boolean clearOnNextRestart = Boolean.parseBoolean(request.getHttpRequest().getPartAsStringFailsafe("clear-on-next-restart", 5));
			preferences.setClearOnNextRestart(clearOnNextRestart);
			boolean reallyClearOnNextRestart = Boolean.parseBoolean(request.getHttpRequest().getPartAsStringFailsafe("really-clear-on-next-restart", 5));
			preferences.setReallyClearOnNextRestart(reallyClearOnNextRestart);
			webInterface.getCore().saveConfiguration();
			throw new RedirectException(getPath());
		}
		templateContext.set("insertion-delay", preferences.getInsertionDelay());
		templateContext.set("positive-trust", preferences.getPositiveTrust());
		templateContext.set("negative-trust", preferences.getNegativeTrust());
		templateContext.set("trust-comment", preferences.getTrustComment());
		templateContext.set("sone-rescue-mode", preferences.isSoneRescueMode());
		templateContext.set("clear-on-next-restart", preferences.isClearOnNextRestart());
		templateContext.set("really-clear-on-next-restart", preferences.isReallyClearOnNextRestart());
	}

}
