package net.pterodactylus.sone.template;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.util.template.HtmlFilter;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.template.TemplateContextFactory;

import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link ImageLinkFilterTest}.
 */
public class ImageLinkFilterTest {

	private final Core core = mock(Core.class);
	private final TemplateContextFactory templateContextFactory = new TemplateContextFactory();
	private final ImageLinkFilter imageLinkFilter = new ImageLinkFilter(core, templateContextFactory);
	private final TemplateContext templateContext = null;
	private final Image image = mock(Image.class);

	@Before
	public void setupTemplateContextFactory() {
		templateContextFactory.addFilter("html", new HtmlFilter());
	}

	@Before
	public void setupCore() {
		when(core.getImage("image-id", false)).thenReturn(image);
	}

	@Before
	public void setupImage() {
		when(image.getId()).thenReturn("image-id");
		when(image.getKey()).thenReturn("image-key");
		when(image.isInserted()).thenReturn(true);
		when(image.getWidth()).thenReturn(640);
		when(image.getHeight()).thenReturn(270);
		when(image.getTitle()).thenReturn("image title");
		when(image.getDescription()).thenReturn("image description");
	}

	@Test
	public void imageLinkIsGeneratedCorrectlyForNotInsertedImages() {
		when(image.isInserted()).thenReturn(false);
		String result = String.valueOf(imageLinkFilter.format(templateContext, image, ImmutableMap.<String, Object>of()));
		Element imageElement = getSingleElement(result);
		assertThat(imageElement.attr("class"), is(""));
		assertThat(imageElement.attr("src"), is("getImage.html?image=image-id"));
		assertThat(imageElement.attr("title"), is("image title"));
		assertThat(imageElement.attr("alt"), is("image description"));
		assertThat(imageElement.attr("width"), is("640"));
		assertThat(imageElement.attr("height"), is("270"));
	}

	@Test
	public void imageLinkIsGeneratedCorrectlyForInsertedImages() {
		String result = String.valueOf(imageLinkFilter.format(templateContext, image, ImmutableMap.<String, Object>of()));
		Element imageElement = getSingleElement(result);
		assertThat(imageElement.attr("class"), is(""));
		assertThat(imageElement.attr("src"), is("/image-key"));
		assertThat(imageElement.attr("title"), is("image title"));
		assertThat(imageElement.attr("alt"), is("image description"));
		assertThat(imageElement.attr("width"), is("640"));
		assertThat(imageElement.attr("height"), is("270"));
	}

	@Test
	public void imageTitleAndDescriptionAreOverriddenCorrectly() {
		String result = String.valueOf(imageLinkFilter.format(templateContext, image, ImmutableMap.<String, Object>of("title", "Test Title")));
		Element imageElement = getSingleElement(result);
		assertThat(imageElement.attr("title"), is("Test Title"));
		assertThat(imageElement.attr("alt"), is("Test Title"));
	}

	@Test
	public void imageIsScaledByWidthCorrectly() {
		String result = String.valueOf(imageLinkFilter.format(templateContext, image, ImmutableMap.<String, Object>of("max-width", "320")));
		Element imageElement = getSingleElement(result);
		assertThat(imageElement.attr("width"), is("320"));
		assertThat(imageElement.attr("height"), is("135"));
	}

	@Test
	public void imageIsScaledByHeightCorrectly() {
		String result = String.valueOf(imageLinkFilter.format(templateContext, image, ImmutableMap.<String, Object>of("max-height", "135")));
		Element imageElement = getSingleElement(result);
		assertThat(imageElement.attr("width"), is("320"));
		assertThat(imageElement.attr("height"), is("135"));
	}

	@Test
	public void wideImageIsEnlargedCorrectly() {
		String result = String.valueOf(imageLinkFilter.format(templateContext, image,
				ImmutableMap.<String, Object>of("mode", "enlarge", "max-width", "100", "max-height", "100")));
		Element imageElement = getSingleElement(result);
		assertThat(imageElement.attr("width"), is("237"));
		assertThat(imageElement.attr("height"), is("100"));
		assertThat(imageElement.attr("style"), containsString("left: -68px"));
		assertThat(imageElement.attr("style"), containsString("top: 0px"));
	}

	@Test
	public void highImageIsEnlargedCorrectly() {
		when(image.getWidth()).thenReturn(270);
		when(image.getHeight()).thenReturn(640);
		String result = String.valueOf(imageLinkFilter.format(templateContext, image,
				ImmutableMap.<String, Object>of("mode", "enlarge", "max-width", "100", "max-height", "100")));
		Element imageElement = getSingleElement(result);
		assertThat(imageElement.attr("width"), is("100"));
		assertThat(imageElement.attr("height"), is("237"));
		assertThat(imageElement.attr("style"), containsString("left: 0px"));
		assertThat(imageElement.attr("style"), containsString("top: -68px"));
	}

	@Test
	public void nullImageIsReturnedAsNull() {
		assertThat(imageLinkFilter.format(templateContext, null, null), nullValue());
	}

	@Test
	public void stringIsUsedToLoadImageFromCore() {
		String result = String.valueOf(imageLinkFilter.format(templateContext, "image-id", ImmutableMap.<String, Object>of()));
		Element imageElement = getSingleElement(result);
		assertThat(imageElement.attr("class"), is(""));
		assertThat(imageElement.attr("src"), is("/image-key"));
		assertThat(imageElement.attr("title"), is("image title"));
		assertThat(imageElement.attr("alt"), is("image description"));
		assertThat(imageElement.attr("width"), is("640"));
		assertThat(imageElement.attr("height"), is("270"));
	}

	private Element getSingleElement(String result) {
		Document document = Jsoup.parseBodyFragment(result);
		assertThatBodyHasASingleElement(document);
		return getSingleElement(document);
	}

	private void assertThatBodyHasASingleElement(Document document) {
		assertThat(document.body().select("> *"), Matchers.hasSize(1));
	}

	private Element getSingleElement(Document document) {
		return document.body().select("> *").get(0);
	}

}
