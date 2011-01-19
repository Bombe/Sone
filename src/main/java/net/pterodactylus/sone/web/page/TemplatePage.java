/*
 * shortener - TemplatePage.java - Copyright © 2010 David Roden
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

package net.pterodactylus.sone.web.page;

import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.web.page.Page.Request.Method;
import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.template.TemplateContextFactory;
import freenet.clients.http.LinkEnabledCallback;
import freenet.clients.http.PageMaker;
import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.l10n.BaseL10n;

/**
 * Base class for all {@link Page}s that are rendered with {@link Template}s.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class TemplatePage implements Page, LinkEnabledCallback {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(TemplatePage.class);

	/** The path of the page. */
	private final String path;

	/** The template context factory. */
	private final TemplateContextFactory templateContextFactory;

	/** The template to render. */
	private final Template template;

	/** The L10n handler. */
	private final BaseL10n l10n;

	/** The l10n key for the page title. */
	private final String pageTitleKey;

	/** Where to redirect for invalid form passwords. */
	private final String invalidFormPasswordRedirectTarget;

	/**
	 * Creates a new template page.
	 *
	 * @param path
	 *            The path of the page
	 * @param templateContextFactory
	 *            The template context factory
	 * @param template
	 *            The template to render
	 * @param l10n
	 *            The L10n handler
	 * @param pageTitleKey
	 *            The l10n key of the title page
	 * @param invalidFormPasswordRedirectTarget
	 *            The target to redirect to if a POST request does not contain
	 *            the correct form password
	 */
	public TemplatePage(String path, TemplateContextFactory templateContextFactory, Template template, BaseL10n l10n, String pageTitleKey, String invalidFormPasswordRedirectTarget) {
		this.path = path;
		this.templateContextFactory = templateContextFactory;
		this.template = template;
		this.l10n = l10n;
		this.pageTitleKey = pageTitleKey;
		this.invalidFormPasswordRedirectTarget = invalidFormPasswordRedirectTarget;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		return path;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response handleRequest(Request request) {
		String redirectTarget = getRedirectTarget(request);
		if (redirectTarget != null) {
			return new RedirectResponse(redirectTarget);
		}

		ToadletContext toadletContext = request.getToadletContext();
		if (request.getMethod() == Method.POST) {
			/* require form password. */
			String formPassword = request.getHttpRequest().getPartAsStringFailsafe("formPassword", 32);
			if (!formPassword.equals(toadletContext.getContainer().getFormPassword())) {
				return new RedirectResponse(invalidFormPasswordRedirectTarget);
			}
		}
		PageMaker pageMaker = toadletContext.getPageMaker();
		PageNode pageNode = pageMaker.getPageNode(l10n.getString(pageTitleKey), toadletContext);
		for (String styleSheet : getStyleSheets()) {
			pageNode.addCustomStyleSheet(styleSheet);
		}
		String shortcutIcon = getShortcutIcon();
		if (shortcutIcon != null) {
			pageNode.addForwardLink("icon", shortcutIcon);
		}

		TemplateContext templateContext = templateContextFactory.createTemplateContext();
		templateContext.mergeContext(template.getInitialContext());
		try {
			long start = System.nanoTime();
			processTemplate(request, templateContext);
			long finish = System.nanoTime();
			logger.log(Level.FINEST, "Template was rendered in " + ((finish - start) / 1000) / 1000.0 + "ms.");
		} catch (RedirectException re1) {
			return new RedirectResponse(re1.getTarget());
		}

		StringWriter stringWriter = new StringWriter();
		template.render(templateContext, stringWriter);
		pageNode.content.addChild("%", stringWriter.toString());

		postProcess(request, templateContext);

		return new Response(200, "OK", "text/html", pageNode.outer.generate());
	}

	/**
	 * Can be overridden to return a custom set of style sheets that are to be
	 * included in the page’s header.
	 *
	 * @return Additional style sheets to load
	 */
	protected Collection<String> getStyleSheets() {
		return Collections.emptySet();
	}

	/**
	 * Returns the name of the shortcut icon to include in the page’s header.
	 *
	 * @return The URL of the shortcut icon, or {@code null} for no icon
	 */
	protected String getShortcutIcon() {
		return null;
	}

	/**
	 * Can be overridden when extending classes need to set variables in the
	 * template before it is rendered.
	 *
	 * @param request
	 *            The request that is rendered
	 * @param templateContext
	 *            The template context to set variables in
	 * @throws RedirectException
	 *             if the processing page wants to redirect after processing
	 */
	protected void processTemplate(Request request, TemplateContext templateContext) throws RedirectException {
		/* do nothing. */
	}

	/**
	 * This method will be called after
	 * {@link #processTemplate(net.pterodactylus.sone.web.page.Page.Request, DataProvider)}
	 * has processed the template and the template was rendered. This method
	 * will not be called if
	 * {@link #processTemplate(net.pterodactylus.sone.web.page.Page.Request, DataProvider)}
	 * throws a {@link RedirectException}!
	 *
	 * @param request
	 *            The request being processed
	 * @param templateContext
	 *            The template context that supplied the rendered data
	 */
	protected void postProcess(Request request, TemplateContext templateContext) {
		/* do nothing. */
	}

	/**
	 * Can be overridden to redirect the user to a different page, in case a log
	 * in is required, or something else is wrong.
	 *
	 * @param request
	 *            The request that is processed
	 * @return The URL to redirect to, or {@code null} to not redirect
	 */
	protected String getRedirectTarget(Page.Request request) {
		return null;
	}

	//
	// INTERFACE LinkEnabledCallback
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEnabled(ToadletContext toadletContext) {
		return true;
	}

	/**
	 * Exception that can be thrown to signal that a subclassed {@link Page}
	 * wants to redirect the user during the
	 * {@link TemplatePage#processTemplate(net.pterodactylus.sone.web.page.Page.Request, TemplateContext)}
	 * method call.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public class RedirectException extends Exception {

		/** The target to redirect to. */
		private final String target;

		/**
		 * Creates a new redirect exception.
		 *
		 * @param target
		 *            The target of the redirect
		 */
		public RedirectException(String target) {
			this.target = target;
		}

		/**
		 * Returns the target to redirect to.
		 *
		 * @return The target to redirect to
		 */
		public String getTarget() {
			return target;
		}

	}

}
