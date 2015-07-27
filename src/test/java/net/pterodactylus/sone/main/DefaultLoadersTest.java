package net.pterodactylus.sone.main;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.StringWriter;

import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;

import org.junit.Test;

/**
 * Unit test for {@link DefaultLoaders}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DefaultLoadersTest {

	private final Loaders loaders = new DefaultLoaders();
	private final StringWriter stringWriter = new StringWriter();
	private final TemplateContext templateContext = new TemplateContext();

	@Test
	public void templateCanBeLoadedFromTheClasspath() {
		Template template = loaders.loadTemplate("/net/pterodactylus/sone/main/template.txt");
		template.render(templateContext, stringWriter);
		assertThat(stringWriter.toString(), is("Template. bar\n"));
	}

}
