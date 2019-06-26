package net.pterodactylus.sone.template;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import net.pterodactylus.util.template.Part;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.template.TemplateException;
import net.pterodactylus.util.template.TemplateParser;

import com.google.common.base.Charsets;

/**
 * {@link Template} implementation that can be reloaded from the filesystem.
 */
public class FilesystemTemplate extends Template {

	private final String filename;
	private final AtomicReference<LastLoadedTemplate> lastTemplate = new AtomicReference<>();
	private final TemplateContext initialContext = new TemplateContext();
	private final List<Part> parts = new ArrayList<>();

	public FilesystemTemplate(String filename) {
		this.filename = filename;
	}

	@Override
	public TemplateContext getInitialContext() {
		loadTemplate();
		return initialContext;
	}

	private void loadTemplate() {
		File templateFile = new File(filename);
		if (templateWasLoaded() && !templateFileHasBeenModifiedAfterLoading(templateFile)) {
			return;
		}
		try (InputStream templateInputStream = new FileInputStream(templateFile);
				Reader templateReader = new InputStreamReader(templateInputStream, Charsets.UTF_8)) {
			Template template = TemplateParser.parse(templateReader);
			lastTemplate.set(new LastLoadedTemplate(template));
			template.getInitialContext().mergeContext(initialContext);
			for (Part part : parts) {
				template.add(part);
			}
		} catch (IOException e) {
			throw new TemplateFileNotFoundException(filename);
		}
	}

	private boolean templateWasLoaded() {
		return lastTemplate.get() != null;
	}

	private boolean templateFileHasBeenModifiedAfterLoading(File templateFile) {
		return templateFile.lastModified() > lastTemplate.get().getLoadTime();
	}

	@Override
	public void add(Part part) {
		loadTemplate();
		parts.add(part);
		lastTemplate.get().getTemplate().add(part);
	}

	@Override
	public void render(TemplateContext templateContext, Writer writer) throws TemplateException {
		loadTemplate();
		lastTemplate.get().getTemplate().render(templateContext, writer);
	}

	@Override
	public Iterator<Part> iterator() {
		loadTemplate();
		return lastTemplate.get().getTemplate().iterator();
	}

	@Override
	public int getLine() {
		loadTemplate();
		return lastTemplate.get().getTemplate().getLine();
	}

	@Override
	public int getColumn() {
		loadTemplate();
		return lastTemplate.get().getTemplate().getColumn();
	}

	private static class LastLoadedTemplate {

		private final Template template;
		private final long loadTime = System.currentTimeMillis();

		private LastLoadedTemplate(Template template) {
			this.template = template;
		}

		public Template getTemplate() {
			return template;
		}

		public long getLoadTime() {
			return loadTime;
		}

	}

	/**
	 * Exception that signals that a template file could not be found.
	 */
	public static class TemplateFileNotFoundException extends RuntimeException {

		public TemplateFileNotFoundException(String filename) {
			super(filename);
		}

	}

}
