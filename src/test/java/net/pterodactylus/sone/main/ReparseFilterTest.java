package net.pterodactylus.sone.main;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Collections;

import net.pterodactylus.util.template.TemplateContext;

import org.junit.Test;

/**
 * Unit test for {@link ReparseFilter}.
 */
public class ReparseFilterTest {

	private final ReparseFilter reparseFilter = new ReparseFilter();

	@Test
	public void filterParsesTemplateCorrectly() {
		TemplateContext templateContext = new TemplateContext();
		templateContext.set("bar", "baz");
		assertThat(reparseFilter.format(templateContext, "foo <% bar>", Collections.<String, Object>emptyMap()),
				is((Object) "foo baz"));
	}

}
