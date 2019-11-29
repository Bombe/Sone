package net.pterodactylus.sone.main;

import static net.pterodactylus.util.template.TemplateParser.parse;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import javax.annotation.Nonnull;

import net.pterodactylus.sone.web.WebInterface;
import net.pterodactylus.util.io.Closer;
import net.pterodactylus.util.template.ClassPathTemplateProvider;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateProvider;
import net.pterodactylus.util.web.Page;
import net.pterodactylus.util.web.Request;
import net.pterodactylus.util.web.StaticPage;

/**
 * Default {@link Loaders} implementation that loads resources from the classpath.
 */
public class DefaultLoaders implements Loaders {

	@Nonnull
	@Override
	public Template loadTemplate(@Nonnull String path) {
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

	@Nonnull
	@Override
	public <REQ extends Request> Page<REQ> loadStaticPage(@Nonnull String pathPrefix, @Nonnull String basePath, @Nonnull String mimeType) {
		return new StaticPage<REQ>(pathPrefix, basePath, mimeType) {
		};
	}

	@Nonnull
	@Override
	public TemplateProvider getTemplateProvider() {
		return new ClassPathTemplateProvider(WebInterface.class, "/templates/");
	}

}
