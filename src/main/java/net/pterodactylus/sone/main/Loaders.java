package net.pterodactylus.sone.main;

import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.web.Page;
import net.pterodactylus.util.web.Request;
import net.pterodactylus.util.web.StaticPage;

import com.google.inject.ImplementedBy;

/**
 * Defines loaders for resources that can be loaded from various locations.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
@ImplementedBy(DefaultLoaders.class)
public interface Loaders {

	Template loadTemplate(String path);
	<REQ extends Request> Page<REQ> loadStaticPage(String basePath, String prefix, String mimeType);

}
