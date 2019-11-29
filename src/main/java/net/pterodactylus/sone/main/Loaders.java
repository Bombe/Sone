package net.pterodactylus.sone.main;

import javax.annotation.Nonnull;

import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateProvider;
import net.pterodactylus.util.web.Page;
import net.pterodactylus.util.web.Request;

import com.google.inject.ImplementedBy;

/**
 * Defines loaders for resources that can be loaded from various locations.
 */
@ImplementedBy(DefaultLoaders.class)
public interface Loaders {

	@Nonnull Template loadTemplate(@Nonnull String path);
	@Nonnull <REQ extends Request> Page<REQ> loadStaticPage(@Nonnull String basePath, @Nonnull String prefix, @Nonnull String mimeType);
	@Nonnull TemplateProvider getTemplateProvider();

}
