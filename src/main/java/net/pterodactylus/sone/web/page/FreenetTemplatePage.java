/*
 * Sone - FreenetTemplatePage.java - Copyright © 2010–2019 David Roden
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

import static java.lang.String.format;
import static java.util.logging.Logger.getLogger;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.main.Loaders;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.template.TemplateContextFactory;
import net.pterodactylus.util.web.Method;
import net.pterodactylus.util.web.Page;
import net.pterodactylus.util.web.RedirectResponse;
import net.pterodactylus.util.web.Response;
import freenet.clients.http.LinkEnabledCallback;
import freenet.clients.http.PageMaker;
import freenet.clients.http.PageNode;
import freenet.clients.http.ToadletContext;
import freenet.support.HTMLNode;

/**
 * Base class for all {@link Page}s that are rendered with {@link Template}s and
 * fit into Freenet’s web interface.
 */
public class FreenetTemplatePage implements FreenetPage, LinkEnabledCallback {

	/** The logger. */
	private static final Logger logger = getLogger(FreenetTemplatePage.class.getName());

	/** The path of the page. */
	private final String path;

	/** The template context factory. */
	private final TemplateContextFactory templateContextFactory;

	/** The template to render. */
	private final Template template;
	private final Loaders loaders;

	/** Where to redirect for invalid form passwords. */
	private final String invalidFormPasswordRedirectTarget;

	public FreenetTemplatePage(String path, TemplateContextFactory templateContextFactory, Loaders loaders, Template template, String invalidFormPasswordRedirectTarget) {
		this.path = path;
		this.templateContextFactory = templateContextFactory;
		this.loaders = loaders;
		this.template = template;
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
	 * Returns the title of the page.
	 *
	 * @param request
	 *            The request to serve
	 * @return The title of the page
	 */
	@SuppressWarnings("static-method")
	protected String getPageTitle(FreenetRequest request) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPrefixPage() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Response handleRequest(FreenetRequest request, Response response) throws IOException {
		String redirectTarget = getRedirectTarget(request);
		if (redirectTarget != null) {
			return new RedirectResponse(redirectTarget);
		}

		if (isFullAccessOnly() && !request.getToadletContext().isAllowedFullAccess()) {
			return response.setStatusCode(401).setStatusText("Not authorized").setContentType("text/html");
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
		PageNode pageNode = pageMaker.getPageNode(getPageTitle(request), toadletContext);
		for (String styleSheet : getStyleSheets()) {
			pageNode.addCustomStyleSheet(styleSheet);
		}
		for (Map<String, String> linkNodeParameters : getAdditionalLinkNodes(request)) {
			HTMLNode linkNode = pageNode.headNode.addChild("link");
			for (Entry<String, String> parameter : linkNodeParameters.entrySet()) {
				linkNode.addAttribute(parameter.getKey(), parameter.getValue());
			}
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
			logger.log(Level.FINEST, format("Template was rendered in %.2fms.", (finish - start) / 1000000.0));
		} catch (RedirectException re1) {
			return new RedirectResponse(re1.getTarget());
		}

		StringWriter stringWriter = new StringWriter();
		template.render(templateContext, stringWriter);
		pageNode.content.addChild("%", stringWriter.toString());

		postProcess(request, templateContext);

		return response.setStatusCode(200).setStatusText("OK").setContentType("text/html").write(pageNode.outer.generate());
	}

	/**
	 * Can be overridden to return a custom set of style sheets that are to be
	 * included in the page’s header.
	 *
	 * @return Additional style sheets to load
	 */
	@SuppressWarnings("static-method")
	protected Collection<String> getStyleSheets() {
		return Collections.emptySet();
	}

	/**
	 * Returns the name of the shortcut icon to include in the page’s header.
	 *
	 * @return The URL of the shortcut icon, or {@code null} for no icon
	 */
	@SuppressWarnings("static-method")
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
	protected void processTemplate(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		/* do nothing. */
	}

	/**
	 * This method will be called after
	 * {@link #processTemplate(FreenetRequest, TemplateContext)} has processed
	 * the template and the template was rendered. This method will not be
	 * called if {@link #processTemplate(FreenetRequest, TemplateContext)}
	 * throws a {@link RedirectException}!
	 *
	 * @param request
	 *            The request being processed
	 * @param templateContext
	 *            The template context that supplied the rendered data
	 */
	protected void postProcess(FreenetRequest request, TemplateContext templateContext) {
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
	@SuppressWarnings("static-method")
	protected String getRedirectTarget(FreenetRequest request) {
		return null;
	}

	/**
	 * Returns additional &lt;link&gt; nodes for the HTML’s &lt;head&gt; node.
	 *
	 * @param request
	 *            The request for which to return the link nodes
	 * @return All link nodes that should be added to the HTML head
	 */
	@SuppressWarnings("static-method")
	protected List<Map<String, String>> getAdditionalLinkNodes(FreenetRequest request) {
		return Collections.emptyList();
	}

	/**
	 * Returns whether this page should only be allowed for requests from hosts
	 * with full access.
	 *
	 * @return {@code true} if this page should only be allowed for hosts with
	 *         full access, {@code false} to allow this page for any host
	 */
	@SuppressWarnings("static-method")
	protected boolean isFullAccessOnly() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLinkExcepted(URI link) {
		return false;
	}

	//
	// INTERFACE LinkEnabledCallback
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEnabled(ToadletContext toadletContext) {
		return !isFullAccessOnly();
	}

	/**
	 * Exception that can be thrown to signal that a subclassed {@link Page}
	 * wants to redirect the user during the
	 * {@link FreenetTemplatePage#processTemplate(FreenetRequest, TemplateContext)}
	 * method call.
	 */
	public static class RedirectException extends Exception {

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

		@Override
		public String toString() {
			return format("RedirectException{target='%s'}", target);
		}

	}

}
