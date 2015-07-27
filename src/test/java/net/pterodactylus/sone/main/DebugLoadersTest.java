package net.pterodactylus.sone.main;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit test for {@link DebugLoaders}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DebugLoadersTest {

	@Rule
	public final TemporaryFolder temporaryFolder = new TemporaryFolder();

	private final StringWriter stringWriter = new StringWriter();
	private final TemplateContext templateContext = new TemplateContext();
	private String templatePath;
	private Loaders loaders;

	@Before
	public void setupLoader() throws IOException {
		templatePath = temporaryFolder.newFolder("temps").getPath();
		loaders = new DebugLoaders(templatePath);
	}

	@Test
	public void debugLoaderCanLoadTemplatesFromFilesystem() throws IOException {
		File templateFile = new File(templatePath, "template.txt");
		Files.write("<%if foo>foo<%else>bar<%/if>", templateFile, Charsets.UTF_8);
		Template template = loaders.loadTemplate("/template.txt");
		template.render(templateContext, stringWriter);
		assertThat(stringWriter.toString(), is("bar"));
	}

}
