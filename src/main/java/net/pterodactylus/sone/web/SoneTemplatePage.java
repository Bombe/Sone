/*
 * Sone - SoneTemplatePage.java - Copyright © 2010–2016 David Roden
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.main.SonePlugin;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.sone.web.page.FreenetTemplatePage;
import net.pterodactylus.util.notify.Notification;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;

import freenet.clients.http.SessionManager.Session;
import freenet.clients.http.ToadletContext;
import freenet.support.api.HTTPRequest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Base page for the Sone web interface.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneTemplatePage extends FreenetTemplatePage {

	/** The Sone core. */
	protected final WebInterface webInterface;

	/** The page title l10n key. */
	private final String pageTitleKey;

	/** Whether to require a login. */
	private final boolean requireLogin;

	/**
	 * Creates a new template page for Sone that does not require the user to be
	 * logged in.
	 *
	 * @param path
	 *            The path of the page
	 * @param template
	 *            The template to render
	 * @param pageTitleKey
	 *            The l10n key of the page title
	 * @param webInterface
	 *            The Sone web interface
	 */
	public SoneTemplatePage(String path, Template template, String pageTitleKey, WebInterface webInterface) {
		this(path, template, pageTitleKey, webInterface, false);
	}

	/**
	 * Creates a new template page for Sone.
	 *
	 * @param path
	 *            The path of the page
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 * @param requireLogin
	 *            Whether this page requires a login
	 */
	public SoneTemplatePage(String path, Template template, WebInterface webInterface, boolean requireLogin) {
		this(path, template, null, webInterface, requireLogin);
	}

	/**
	 * Creates a new template page for Sone.
	 *
	 * @param path
	 *            The path of the page
	 * @param template
	 *            The template to render
	 * @param pageTitleKey
	 *            The l10n key of the page title
	 * @param webInterface
	 *            The Sone web interface
	 * @param requireLogin
	 *            Whether this page requires a login
	 */
	public SoneTemplatePage(String path, Template template, String pageTitleKey, WebInterface webInterface, boolean requireLogin) {
		super(path, webInterface.getTemplateContextFactory(), template, "noPermission.html");
		this.pageTitleKey = pageTitleKey;
		this.webInterface = webInterface;
		this.requireLogin = requireLogin;
	}

	//
	// PROTECTED METHODS
	//

	/**
	 * Returns the currently logged in Sone.
	 *
	 * @param toadletContext
	 *            The toadlet context
	 * @return The currently logged in Sone, or {@code null} if no Sone is
	 *         currently logged in
	 */
	protected Sone getCurrentSone(ToadletContext toadletContext) {
		return webInterface.getCurrentSoneCreatingSession(toadletContext);
	}

	protected Sone getCurrentSoneWithoutCreatingSession(ToadletContext toadletContext) {
		return webInterface.getCurrentSoneWithoutCreatingSession(toadletContext);
	}

	/**
	 * Sets the currently logged in Sone.
	 *
	 * @param toadletContext
	 *            The toadlet context
	 * @param sone
	 *            The Sone to set as currently logged in
	 */
	protected void setCurrentSone(ToadletContext toadletContext, Sone sone) {
		webInterface.setCurrentSone(toadletContext, sone);
	}

	//
	// TEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getPageTitle(FreenetRequest request) {
		if (pageTitleKey != null) {
			return webInterface.getL10n().getString(pageTitleKey);
		}
		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<Map<String, String>> getAdditionalLinkNodes(FreenetRequest request) {
		return ImmutableList.<Map<String, String>> builder().add(ImmutableMap.<String, String> builder().put("rel", "search").put("type", "application/opensearchdescription+xml").put("title", "Sone").put("href", "http://" + request.getHttpRequest().getHeader("host") + "/Sone/OpenSearch.xml").build()).build();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Collection<String> getStyleSheets() {
		return Arrays.asList("css/sone.css");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getShortcutIcon() {
		return "images/icon.png";
	}

	/**
	 * Returns whether this page requires the user to log in.
	 *
	 * @return {@code true} if the user is required to be logged in to use this
	 *         page, {@code false} otherwise
	 */
	protected boolean requiresLogin() {
		return requireLogin;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void processTemplate(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		super.processTemplate(request, templateContext);
		Sone currentSone = getCurrentSoneWithoutCreatingSession(request.getToadletContext());
		templateContext.set("core", webInterface.getCore());
		templateContext.set("currentSone", currentSone);
		templateContext.set("localSones", webInterface.getCore().getLocalSones());
		templateContext.set("request", request);
		templateContext.set("currentVersion", SonePlugin.getPluginVersion());
		templateContext.set("hasLatestVersion", webInterface.getCore().getUpdateChecker().hasLatestVersion());
		templateContext.set("latestEdition", webInterface.getCore().getUpdateChecker().getLatestEdition());
		templateContext.set("latestVersion", webInterface.getCore().getUpdateChecker().getLatestVersion());
		templateContext.set("latestVersionTime", webInterface.getCore().getUpdateChecker().getLatestVersionDate());
		List<Notification> notifications = new ArrayList<Notification>(webInterface.getNotifications(currentSone));
		Collections.sort(notifications, Notification.CREATED_TIME_SORTER);
		templateContext.set("notifications", notifications);
		templateContext.set("notificationHash", notifications.hashCode());
		handleRequest(request, templateContext);
	}

	protected void handleRequest(@Nonnull FreenetRequest request, @Nonnull TemplateContext templateContext) throws RedirectException {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected String getRedirectTarget(FreenetRequest request) {
		if (requiresLogin() && (getCurrentSoneWithoutCreatingSession(request.getToadletContext()) == null)) {
			HTTPRequest httpRequest = request.getHttpRequest();
			String originalUrl = httpRequest.getPath();
			if (httpRequest.hasParameters()) {
				StringBuilder requestParameters = new StringBuilder();
				for (String parameterName : httpRequest.getParameterNames()) {
					if (requestParameters.length() > 0) {
						requestParameters.append("&");
					}
					String[] parameterValues = httpRequest.getMultipleParam(parameterName);
					for (String parameterValue : parameterValues) {
						requestParameters.append(urlEncode(parameterName)).append("=").append(urlEncode(parameterValue));
					}
				}
				originalUrl += "?" + requestParameters.toString();
			}
			return "login.html?target=" + urlEncode(originalUrl);
		}
		return null;
	}

	private static String urlEncode(String value) {
		try {
			return URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException uee1) {
							/* A JVM without UTF-8? I don’t think so. */
			throw new RuntimeException(uee1);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isFullAccessOnly() {
		return webInterface.getCore().getPreferences().isRequireFullAccess();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEnabled(ToadletContext toadletContext) {
		if (webInterface.getCore().getPreferences().isRequireFullAccess() && !toadletContext.isAllowedFullAccess()) {
			return false;
		}
		if (requiresLogin()) {
			return getCurrentSoneWithoutCreatingSession(toadletContext) != null;
		}
		return true;
	}

}
