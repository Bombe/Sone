package net.pterodactylus.sone.main;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import net.pterodactylus.util.template.Filter;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.template.TemplateException;
import net.pterodactylus.util.template.TemplateParser;

/**
 * Takes the input and parses it as a new {@link Template}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ReparseFilter implements Filter {

	@Override
	public Object format(TemplateContext templateContext, Object data, Map<String, Object> parameters) {
		Template template = TemplateParser.parse(new StringReader(String.valueOf(data)));
		StringWriter stringWriter = new StringWriter();
		try {
			template.render(templateContext, stringWriter);
		} catch (TemplateException e) {
			throw new RuntimeException(e);
		} finally {
			return stringWriter.toString();
		}
	}

}
