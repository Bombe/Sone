/*
 * Sone - ParserFilter.java - Copyright © 2011 David Roden
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

package net.pterodactylus.sone.template;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.text.FreenetLinkParser;
import net.pterodactylus.sone.text.FreenetLinkParserContext;
import net.pterodactylus.util.template.Filter;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.template.TemplateContextFactory;

/**
 * Filter that filters a given text through a {@link FreenetLinkParser}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ParserFilter implements Filter {

	/** The link parser. */
	private final FreenetLinkParser linkParser;

	/**
	 * Creates a new filter that runs its input through a
	 * {@link FreenetLinkParser}.
	 *
	 * @param templateContextFactory
	 *            The context factory for rendering the parts
	 */
	public ParserFilter(TemplateContextFactory templateContextFactory) {
		linkParser = new FreenetLinkParser(templateContextFactory);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object format(TemplateContext templateContext, Object data, Map<String, String> parameters) {
		String text = String.valueOf(data);
		String soneKey = parameters.get("sone");
		if (soneKey == null) {
			soneKey = "sone";
		}
		Sone sone = (Sone) templateContext.get(soneKey);
		FreenetLinkParserContext context = new FreenetLinkParserContext(sone);
		try {
			return linkParser.parse(context, new StringReader(text));
		} catch (IOException ioe1) {
			/* no exceptions in a StringReader, ignore. */
		}
		return null;
	}

}
