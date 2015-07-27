package net.pterodactylus.sone.main;

import java.io.File;

import net.pterodactylus.sone.template.FilesystemTemplate;
import net.pterodactylus.util.template.Template;

/**
 * {@link Loaders} implementation that loads all resources from the filesystem.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DebugLoaders implements Loaders {

	private final String filesystemPath;

	public DebugLoaders(String filesystemPath) {
		this.filesystemPath = filesystemPath;
	}

	@Override
	public Template loadTemplate(String path) {
		return new FilesystemTemplate(new File(filesystemPath, path).getAbsolutePath());
	}

}
