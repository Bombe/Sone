/*
 * Sone - StaticTemplatePage.java - Copyright © 2011 David Roden
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.util.io.Closer;
import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.template.TemplateContextFactory;

/**
 * A template page is a single page that is created from a {@link Template} but
 * does not necessarily return HTML.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class TemplatePage implements Page {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(TemplatePage.class);

	/** The path of this page. */
	private final String path;

	/** The content type of this page. */
	private final String contentType;

	/** The template context factory. */
	private final TemplateContextFactory templateContextFactory;

	/** The template to render. */
	private final Template template;

	/**
	 * Creates a new template page.
	 *
	 * @param path
	 *            The path of the page
	 * @param contentType
	 *            The content type of the page
	 * @param templateContextFactory
	 *            The template context factory
	 * @param template
	 *            The template to render
	 */
	public TemplatePage(String path, String contentType, TemplateContextFactory templateContextFactory, Template template) {
		this.path = path;
		this.contentType = contentType;
		this.templateContextFactory = templateContextFactory;
		this.template = template;
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
		ByteArrayOutputStream responseOutputStream = new ByteArrayOutputStream();
		OutputStreamWriter responseWriter = null;
		try {
			responseWriter = new OutputStreamWriter(responseOutputStream, "UTF-8");
			TemplateContext templateContext = templateContextFactory.createTemplateContext();
			templateContext.set("request", request);
			template.render(templateContext, responseWriter);
		} catch (IOException ioe1) {
			logger.log(Level.WARNING, "Could not render template for path “" + path + "”!", ioe1);
		} finally {
			Closer.close(responseWriter);
			Closer.close(responseOutputStream);
		}
		ByteArrayInputStream responseInputStream = new ByteArrayInputStream(responseOutputStream.toByteArray());
		/* no need to close a ByteArrayInputStream. */
		return new Response(200, "OK", contentType, null, responseInputStream);
	}

}
