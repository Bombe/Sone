/*
 * Sone - TemplatePart.java - Copyright © 2010 David Roden
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

package net.pterodactylus.sone.text;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.template.TemplateContextFactory;
import net.pterodactylus.util.template.TemplateException;

/**
 * {@link Part} implementation that is rendered using a {@link Template}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class TemplatePart implements Part, net.pterodactylus.util.template.Part {

	/** The template context factory. */
	private final TemplateContextFactory templateContextFactory;

	/** The template to render for this part. */
	private final Template template;

	/**
	 * Creates a new template part.
	 *
	 * @param templateContextFactory
	 *            The template context factory
	 * @param template
	 *            The template to render
	 */
	public TemplatePart(TemplateContextFactory templateContextFactory, Template template) {
		this.templateContextFactory = templateContextFactory;
		this.template = template;
	}

	//
	// ACTIONS
	//

	/**
	 * Sets a variable in the template.
	 *
	 * @param key
	 *            The key of the variable
	 * @param value
	 *            The value of the variable
	 * @return This template part (for method chaining)
	 */
	public TemplatePart set(String key, Object value) {
		template.getInitialContext().set(key, value);
		return this;
	}

	//
	// PART METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void render(Writer writer) throws IOException {
		template.render(templateContextFactory.createTemplateContext().mergeContext(template.getInitialContext()), writer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void render(TemplateContext templateContext, Writer writer) throws TemplateException {
		template.render(templateContext.mergeContext(template.getInitialContext()), writer);
	}

	//
	// OBJECT METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringWriter stringWriter = new StringWriter();
		try {
			render(stringWriter);
		} catch (IOException ioe1) {
			/* should never throw, ignore. */
		}
		return stringWriter.toString();
	}

}
