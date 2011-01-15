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

import net.pterodactylus.sone.core.Options;
import net.pterodactylus.sone.web.page.Page.Request.Method;
import net.pterodactylus.util.number.Numbers;
import net.pterodactylus.util.template.DataProvider;
import net.pterodactylus.util.template.Template;

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
	protected void processTemplate(Request request, DataProvider dataProvider) throws RedirectException {
		super.processTemplate(request, dataProvider);
		Options options = webInterface.getCore().getOptions();
		if (request.getMethod() == Method.POST) {
			Integer insertionDelay = Numbers.safeParseInteger(request.getHttpRequest().getPartAsStringFailsafe("insertion-delay", 16));
			options.getIntegerOption("InsertionDelay").set(insertionDelay);
			Integer positiveTrust = Numbers.safeParseInteger(request.getHttpRequest().getPartAsStringFailsafe("positive-trust", 3), options.getIntegerOption("PositiveTrust").getReal());
			options.getIntegerOption("PositiveTrust").set(positiveTrust);
			Integer negativeTrust = Numbers.safeParseInteger(request.getHttpRequest().getPartAsStringFailsafe("negative-trust", 3), options.getIntegerOption("NegativeTrust").getReal());
			options.getIntegerOption("NegativeTrust").set(negativeTrust);
			String trustComment = request.getHttpRequest().getPartAsStringFailsafe("trust-comment", 256);
			if (trustComment.trim().length() == 0) {
				trustComment = null;
			}
			options.getStringOption("TrustComment").set(trustComment);
			boolean soneRescueMode = Boolean.parseBoolean(request.getHttpRequest().getPartAsStringFailsafe("sone-rescue-mode", 5));
			options.getBooleanOption("SoneRescueMode").set(soneRescueMode);
			boolean clearOnNextRestart = Boolean.parseBoolean(request.getHttpRequest().getPartAsStringFailsafe("clear-on-next-restart", 5));
			options.getBooleanOption("ClearOnNextRestart").set(clearOnNextRestart);
			boolean reallyClearOnNextRestart = Boolean.parseBoolean(request.getHttpRequest().getPartAsStringFailsafe("really-clear-on-next-restart", 5));
			options.getBooleanOption("ReallyClearOnNextRestart").set(reallyClearOnNextRestart);
			webInterface.getCore().saveConfiguration();
			throw new RedirectException(getPath());
		}
		dataProvider.set("insertion-delay", options.getIntegerOption("InsertionDelay").get());
		dataProvider.set("positive-trust", options.getIntegerOption("PositiveTrust").get());
		dataProvider.set("negative-trust", options.getIntegerOption("NegativeTrust").get());
		dataProvider.set("trust-comment", options.getStringOption("TrustComment").get());
		dataProvider.set("sone-rescue-mode", options.getBooleanOption("SoneRescueMode").get());
		dataProvider.set("clear-on-next-restart", options.getBooleanOption("ClearOnNextRestart").get());
		dataProvider.set("really-clear-on-next-restart", options.getBooleanOption("ReallyClearOnNextRestart").get());
	}

}
