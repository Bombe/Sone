package net.pterodactylus.sone.main;

import java.io.File;

import net.pterodactylus.sone.template.FilesystemTemplate;
import net.pterodactylus.sone.web.pages.ReloadingPage;
import net.pterodactylus.util.template.FilesystemTemplateProvider;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateProvider;
import net.pterodactylus.util.web.Page;
import net.pterodactylus.util.web.Request;

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

	@Override
	public <REQ extends Request> Page<REQ> loadStaticPage(String basePath, String prefix, String mimeType) {
		return new ReloadingPage<REQ>(basePath, new File(filesystemPath, prefix).getAbsolutePath(), mimeType);
	}

	@Override
	public TemplateProvider getTemplateProvider() {
		return new FilesystemTemplateProvider(new File(filesystemPath, "/templates/").getAbsolutePath());
	}

}
