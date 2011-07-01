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

import java.util.ArrayList;
import java.util.List;

import net.pterodactylus.sone.core.Core.Preferences;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired;
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
		Sone currentSone = webInterface.getCurrentSone(request.getToadletContext(), false);
		if (request.getMethod() == Method.POST) {
			List<String> fieldErrors = new ArrayList<String>();
			if (currentSone != null) {
				boolean autoFollow = request.getHttpRequest().isPartSet("auto-follow");
				currentSone.getOptions().getBooleanOption("AutoFollow").set(autoFollow);
				webInterface.getCore().saveSone(currentSone);
			}
			Integer insertionDelay = Numbers.safeParseInteger(request.getHttpRequest().getPartAsStringFailsafe("insertion-delay", 16));
			if (!preferences.validateInsertionDelay(insertionDelay)) {
				fieldErrors.add("insertion-delay");
			} else {
				preferences.setInsertionDelay(insertionDelay);
			}
			Integer postsPerPage = Numbers.safeParseInteger(request.getHttpRequest().getPartAsStringFailsafe("posts-per-page", 4), null);
			if (!preferences.validatePostsPerPage(postsPerPage)) {
				fieldErrors.add("posts-per-page");
			} else {
				preferences.setPostsPerPage(postsPerPage);
			}
			Integer charactersPerPost = Numbers.safeParseInteger(request.getHttpRequest().getPartAsStringFailsafe("characters-per-post", 10), null);
			if (!preferences.validateCharactersPerPost(charactersPerPost)) {
				fieldErrors.add("characters-per-post");
			} else {
				preferences.setCharactersPerPost(charactersPerPost);
			}
			boolean requireFullAccess = request.getHttpRequest().isPartSet("require-full-access");
			preferences.setRequireFullAccess(requireFullAccess);
			Integer positiveTrust = Numbers.safeParseInteger(request.getHttpRequest().getPartAsStringFailsafe("positive-trust", 3));
			if (!preferences.validatePositiveTrust(positiveTrust)) {
				fieldErrors.add("positive-trust");
			} else {
				preferences.setPositiveTrust(positiveTrust);
			}
			Integer negativeTrust = Numbers.safeParseInteger(request.getHttpRequest().getPartAsStringFailsafe("negative-trust", 4));
			if (!preferences.validateNegativeTrust(negativeTrust)) {
				fieldErrors.add("negative-trust");
			} else {
				preferences.setNegativeTrust(negativeTrust);
			}
			String trustComment = request.getHttpRequest().getPartAsStringFailsafe("trust-comment", 256);
			if (trustComment.trim().length() == 0) {
				trustComment = null;
			}
			preferences.setTrustComment(trustComment);
			boolean fcpInterfaceActive = request.getHttpRequest().isPartSet("fcp-interface-active");
			preferences.setFcpInterfaceActive(fcpInterfaceActive);
			Integer fcpFullAccessRequiredInteger = Numbers.safeParseInteger(request.getHttpRequest().getPartAsStringFailsafe("fcp-full-access-required", 1), preferences.getFcpFullAccessRequired().ordinal());
			FullAccessRequired fcpFullAccessRequired = FullAccessRequired.values()[fcpFullAccessRequiredInteger];
			preferences.setFcpFullAccessRequired(fcpFullAccessRequired);
			boolean clearOnNextRestart = Boolean.parseBoolean(request.getHttpRequest().getPartAsStringFailsafe("clear-on-next-restart", 5));
			preferences.setClearOnNextRestart(clearOnNextRestart);
			boolean reallyClearOnNextRestart = Boolean.parseBoolean(request.getHttpRequest().getPartAsStringFailsafe("really-clear-on-next-restart", 5));
			preferences.setReallyClearOnNextRestart(reallyClearOnNextRestart);
			webInterface.getCore().saveConfiguration();
			if (fieldErrors.isEmpty()) {
				throw new RedirectException(getPath());
			}
			templateContext.set("fieldErrors", fieldErrors);
		}
		if (currentSone != null) {
			templateContext.set("auto-follow", currentSone.getOptions().getBooleanOption("AutoFollow").get());
		}
		templateContext.set("insertion-delay", preferences.getInsertionDelay());
		templateContext.set("posts-per-page", preferences.getPostsPerPage());
		templateContext.set("characters-per-post", preferences.getCharactersPerPost());
		templateContext.set("require-full-access", preferences.isRequireFullAccess());
		templateContext.set("positive-trust", preferences.getPositiveTrust());
		templateContext.set("negative-trust", preferences.getNegativeTrust());
		templateContext.set("trust-comment", preferences.getTrustComment());
		templateContext.set("fcp-interface-active", preferences.isFcpInterfaceActive());
		templateContext.set("fcp-full-access-required", preferences.getFcpFullAccessRequired().ordinal());
		templateContext.set("clear-on-next-restart", preferences.isClearOnNextRestart());
		templateContext.set("really-clear-on-next-restart", preferences.isReallyClearOnNextRestart());
	}

}
