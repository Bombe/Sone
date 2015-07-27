package net.pterodactylus.sone.main;

import static net.pterodactylus.util.template.TemplateParser.parse;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import net.pterodactylus.util.io.Closer;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.web.Page;
import net.pterodactylus.util.web.Request;
import net.pterodactylus.util.web.StaticPage;

/**
 * Default {@link Loaders} implementation that loads resources from the classpath.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DefaultLoaders implements Loaders {

	@Override
	public Template loadTemplate(String path) {
		InputStream templateInputStream = null;
		Reader reader = null;
		try {
			templateInputStream = getClass().getResourceAsStream(path);
			reader = new InputStreamReader(templateInputStream, "UTF-8");
			return parse(reader);
		} catch (UnsupportedEncodingException uee1) {
			throw new RuntimeException("UTF-8 not supported.");
		} finally {
			Closer.close(reader);
			Closer.close(templateInputStream);
		}
	}

	@Override
	public <REQ extends Request> Page<REQ> loadStaticPage(String pathPrefix, String basePath, String mimeType) {
		return new StaticPage<REQ>(pathPrefix, basePath, mimeType);
	}

}
