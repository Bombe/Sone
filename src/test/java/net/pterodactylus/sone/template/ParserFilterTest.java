package net.pterodactylus.sone.template;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.text.FreemailPart;
import net.pterodactylus.sone.text.Part;
import net.pterodactylus.sone.text.SoneTextParser;
import net.pterodactylus.sone.text.SoneTextParserContext;
import net.pterodactylus.util.template.HtmlFilter;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.template.TemplateContextFactory;

import com.google.common.base.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link ParserFilter}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ParserFilterTest {

	private static final String FREEMAIL_ID = "t4dlzfdww3xvsnsc6j6gtliox6zaoak7ymkobbmcmdw527ubuqra";
	private static final String SONE_FREEMAIL = "sone@" + FREEMAIL_ID + ".freemail";
	private static final String SONE_IDENTITY = "nwa8lHa271k2QvJ8aa0Ov7IHAV-DFOCFgmDt3X6BpCI";

	private final Core core = mock(Core.class);
	private final TemplateContextFactory templateContextFactory = new TemplateContextFactory();
	private final SoneTextParser soneTextParser = mock(SoneTextParser.class);
	private final ParserFilter filter = new ParserFilter(core, templateContextFactory, soneTextParser);

	@Before
	public void setupTemplateContextFactory() {
		templateContextFactory.addFilter("html", new HtmlFilter());
	}

	@Test
	public void freemailAddressIsDisplayedCorrectly() {
		Sone sone = mock(Sone.class);
		when(sone.getProfile()).thenReturn(new Profile(sone));
		sone.getProfile().setFirstName("Sone");
		when(core.getSone(SONE_IDENTITY)).thenReturn(Optional.of(sone));
		when(soneTextParser.parse(anyString(), any(SoneTextParserContext.class))).thenReturn(Collections.<Part>singletonList(new FreemailPart("sone", FREEMAIL_ID, SONE_IDENTITY)));
		TemplateContext templateContext = templateContextFactory.createTemplateContext();
		String output = String.valueOf(filter.format(templateContext, SONE_FREEMAIL, Collections.<String, Object>emptyMap()));
		Document document = Jsoup.parseBodyFragment(output);
		Element linkNode = document.body().child(0);
		assertThat(linkNode.nodeName(), is("a"));
		assertThat(linkNode.attributes().asList(), containsInAnyOrder(
				new Attribute("href", "/Freemail/NewMessage?to=" + SONE_IDENTITY),
				new Attribute("class", "in-sone"),
				new Attribute("title", "Sone")
		));
		assertThat(linkNode.text(), is("sone@Sone.freemail"));
	}

}
