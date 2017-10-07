package net.pterodactylus.sone.template;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import net.pterodactylus.util.template.Part;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.template.TemplateException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link FilesystemTemplate}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FilesystemTemplateTest {

	private final File tempFile;
	private final FilesystemTemplate filesystemTemplate;
	private final AtomicReference<StringWriter> stringWriter = new AtomicReference<StringWriter>(new StringWriter());
	private final TemplateContext templateContext = new TemplateContext();

	public FilesystemTemplateTest() throws IOException {
		tempFile = File.createTempFile("template-", ".dat");
		writeTemplate("Text");
		filesystemTemplate = new FilesystemTemplate(tempFile.getAbsolutePath());
	}

	private void writeTemplate(String text) throws IOException {
		Files.write(text + ".<%foreach values value><% value><%/foreach>", tempFile, Charsets.UTF_8);
	}

	@Before
	public void setupTemplateContext() {
		templateContext.set("values", Arrays.asList("a", 1));
	}

	@Test(expected = FilesystemTemplate.TemplateFileNotFoundException.class)
	public void loadingTemplateFromNonExistingFileThrowsException() throws IOException {
		FilesystemTemplate filesystemTemplate = new FilesystemTemplate("/a/b/c.dat");
		filesystemTemplate.getInitialContext();
	}

	@Test
	public void templateCanBeLoadedFromTheFilesystem() {
		filesystemTemplate.render(templateContext, stringWriter.get());
		assertThat(getRenderedString(), is("Text.a1"));
	}

	@Test
	public void templateCanBeReloaded() throws IOException, InterruptedException {
		filesystemTemplate.render(templateContext, stringWriter.get());
		assertThat(getRenderedString(), is("Text.a1"));
		Thread.sleep(1000);
		writeTemplate("New");
		filesystemTemplate.render(templateContext, stringWriter.get());
		assertThat(getRenderedString(), is("New.a1"));
	}

	@Test
	public void templateIsNotReloadedIfNotChanged() {
		filesystemTemplate.render(templateContext, stringWriter.get());
		assertThat(getRenderedString(), is("Text.a1"));
		filesystemTemplate.render(templateContext, stringWriter.get());
		assertThat(getRenderedString(), is("Text.a1"));
	}

	private String getRenderedString() {
		String renderedString = stringWriter.get().toString();
		stringWriter.set(new StringWriter());
		return renderedString;
	}

	@Test
	public void initialContextIsCopiedToReloadedTemplates() throws IOException, InterruptedException {
		filesystemTemplate.getInitialContext().set("values", "test");
		Thread.sleep(1000);
		writeTemplate("New");
		assertThat(filesystemTemplate.getInitialContext().get("values"), is((Object) "test"));
	}

	@Test
	public void partsAreCopiedToReloadedTemplates() throws InterruptedException, IOException {
		filesystemTemplate.add(new Part() {
			@Override
			public void render(TemplateContext templateContext, Writer writer) throws TemplateException {
				try {
					writer.write(".Test");
				} catch (IOException e) {
					throw new TemplateException(e);
				}
			}
		});
		Thread.sleep(1000);
		writeTemplate("New");
		filesystemTemplate.render(templateContext, stringWriter.get());
		assertThat(getRenderedString(), is("New.a1.Test"));
	}

	@Test
	public void columnOfReturnedTemplateIsReturnedAsZero() {
		assertThat(filesystemTemplate.getColumn(), is(0));
	}

	@Test
	public void lineOfReturnedTemplateIsReturnedAsZero() {
		assertThat(filesystemTemplate.getLine(), is(0));
	}

	@Test
	public void templateCanBeIteratedOver() {
	    assertThat(filesystemTemplate.iterator(), notNullValue());
	}

}
