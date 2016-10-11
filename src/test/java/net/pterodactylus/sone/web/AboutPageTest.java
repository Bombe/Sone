package net.pterodactylus.sone.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

/**
 * Unit test for {@link AboutPage}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class AboutPageTest extends WebPageTest {

	private final String version = "0.1.2";
	private final int year = 1234;
	private final String homepage = "home://page";
	private final AboutPage page = new AboutPage(template, webInterface, version, year, homepage);

	@Test
	public void pageReturnsCorrectPath() {
		assertThat(page.getPath(), is("about.html"));
	}

	@Test
	public void pageSetsCorrectVersionInTemplateContext() throws Exception {
		page.processTemplate(freenetRequest, templateContext);
		assertThat(templateContext.get("version"), is((Object) version));
	}

	@Test
	public void pageSetsCorrectHomepageInTemplateContext() throws Exception {
		page.processTemplate(freenetRequest, templateContext);
		assertThat(templateContext.get("homepage"), is((Object) homepage));
	}

	@Test
	public void pageSetsCorrectYearInTemplateContext() throws Exception {
		page.processTemplate(freenetRequest, templateContext);
		assertThat(templateContext.get("year"), is((Object) year));
	}

}
