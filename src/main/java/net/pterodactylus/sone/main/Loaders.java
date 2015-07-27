package net.pterodactylus.sone.main;

import net.pterodactylus.util.template.Template;

import com.google.inject.ImplementedBy;

/**
 * Defines loaders for resources that can be loaded from various locations.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
@ImplementedBy(DefaultLoaders.class)
public interface Loaders {

	Template loadTemplate(String path);

}
