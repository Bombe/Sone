package net.pterodactylus.sone.template;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.text.FreemailPart;
import net.pterodactylus.sone.text.FreenetLinkPart;
import net.pterodactylus.sone.text.LinkPart;
import net.pterodactylus.sone.text.Part;
import net.pterodactylus.sone.text.PartContainer;
import net.pterodactylus.sone.text.PlainTextPart;
import net.pterodactylus.sone.text.PostPart;
import net.pterodactylus.sone.text.SonePart;
import net.pterodactylus.sone.text.SoneTextParser;
import net.pterodactylus.sone.text.SoneTextParserContext;
import net.pterodactylus.util.template.HtmlFilter;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.template.TemplateContextFactory;

import com.google.common.base.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * Unit test for {@link ParserFilter}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ParserFilterTest {

	private static final String FREEMAIL_ID = "t4dlzfdww3xvsnsc6j6gtliox6zaoak7ymkobbmcmdw527ubuqra";
	private static final String SONE_FREEMAIL = "sone@" + FREEMAIL_ID + ".freemail";
	private static final String SONE_IDENTITY = "nwa8lHa271k2QvJ8aa0Ov7IHAV-DFOCFgmDt3X6BpCI";
	private static final String POST_ID = "37a06250-6775-4b94-86ff-257ba690953c";

	private final Core core = mock(Core.class);
	private final TemplateContextFactory templateContextFactory = new TemplateContextFactory();
	private final TemplateContext templateContext;
	private final SoneTextParser soneTextParser = mock(SoneTextParser.class);
	private final ParserFilter filter = new ParserFilter(core, templateContextFactory, soneTextParser);
	private final Sone sone = setupSone(SONE_IDENTITY, "Sone", "First");
	private final Map<String, Object> parameters = new HashMap<>();

	public ParserFilterTest() {
		templateContextFactory.addFilter("html", new HtmlFilter());
		templateContext = templateContextFactory.createTemplateContext();
	}

	@Test
	public void givenSoneIsUsedInParseContext() {
		setupSoneAndVerifyItIsUsedInContext(sone, sone);
	}

	private void setupSoneAndVerifyItIsUsedInContext(Object soneOrSoneId, Sone sone) {
		setupParser("text", new PlainTextPart("text"));
		parameters.put("sone", sone);
		filter.format(templateContext, "text", parameters);
		ArgumentCaptor<SoneTextParserContext> context = forClass(SoneTextParserContext.class);
		verify(soneTextParser).parse(eq("text"), context.capture());
		assertThat(context.getValue().getPostingSone(), is(sone));
	}

	@Test
	public void soneWithGivenSoneIdIsUsedInParseContext() {
		setupSoneAndVerifyItIsUsedInContext(SONE_IDENTITY, sone);
	}

	@Test
	public void plainTextIsRenderedCorrectly() {
		setupParser("plain text", new PlainTextPart("plain text"));
		String result = (String) filter.format(templateContext, "plain text", Collections.<String, Object>emptyMap());
		assertThat(result, is("plain text"));
	}

	private void setupParser(String text, Part... parsedParts) {
		when(soneTextParser.parse(eq(text), any(SoneTextParserContext.class))).thenReturn(asList(parsedParts));
	}

	@Test
	public void plainTextPartIsShortenedIfLengthExceedsMaxLength() {
		setupParser("text", new PlainTextPart("This is a long text."));
		setLengthAndCutOffLength(15, 10);
		String output = (String) filter.format(templateContext, "text", parameters);
		assertThat(output, is("This is a &hellip;"));
	}

	@Test
	public void plainTextPartIsNotShortenedIfLengthDoesNotExceedMaxLength() {
		setupParser("text", new PlainTextPart("This is a long text."));
		setLengthAndCutOffLength(20, 10);
		String output = (String) filter.format(templateContext, "text", parameters);
		assertThat(output, is("This is a &hellip;"));
	}

	@Test
	public void shortPartsAreNotShortened() {
		setupParser("text", new PlainTextPart("This."));
		setLengthAndCutOffLength(15, 10);
		String output = (String) filter.format(templateContext, "text", parameters);
		assertThat(output, is("This."));
	}

	@Test
	public void multiplePlainTextPartsAreShortened() {
		setupParser("text", new PlainTextPart("This "), new PlainTextPart("is a long text."));
		setLengthAndCutOffLength(15, 10);
		String output = (String) filter.format(templateContext, "text", parameters);
		assertThat(output, is("This is a &hellip;"));
	}

	@Test
	public void partsAfterLengthHasBeenReachedAreIgnored() {
		setupParser("text", new PlainTextPart("This is a long text."), new PlainTextPart(" And even more."));
		setLengthAndCutOffLength(15, 10);
		String output = (String) filter.format(templateContext, "text", parameters);
		assertThat(output, is("This is a &hellip;"));
	}

	@Test
	public void linkPartsAreNotShortened() {
		setupParser("text", new FreenetLinkPart("KSK@gpl.txt", "This is a long text.", false));
		setLengthAndCutOffLength(15, 10);
		String output = (String) filter.format(templateContext, "text", parameters);
		Element linkNode = Jsoup.parseBodyFragment(output).body().child(0);
		verifyLink(linkNode, "/KSK@gpl.txt", "freenet", "KSK@gpl.txt", "This is a long text.");
	}

	@Test
	public void additionalLinkPartsAreIgnored() {
		setupParser("text", new PlainTextPart("This is a long text."), new FreenetLinkPart("KSK@gpl.txt", "This is a long text.", false));
		setLengthAndCutOffLength(15, 10);
		String output = (String) filter.format(templateContext, "text", parameters);
		assertThat(output, is("This is a &hellip;"));
	}

	private void setLengthAndCutOffLength(int length, int cutOffLength) {
		parameters.put("length", length);
		parameters.put("cut-off-length", cutOffLength);
	}

	@Test
	public void sonePartsAreAddedButTheirLengthIsIgnored() {
		setupParser("text", new SonePart(sone), new PlainTextPart("This is a long text."));
		setLengthAndCutOffLength(15, 10);
		String output = (String) filter.format(templateContext, "text", parameters);
		Element body = Jsoup.parseBodyFragment(output).body();
		Element linkNode = (Element) body.childNode(0);
		System.out.println(linkNode);
		verifyLink(linkNode, "viewSone.html?sone=" + SONE_IDENTITY, "in-sone", "First", "First");
		assertThat(((TextNode) body.childNode(1)).text(), is("This is a …"));
	}

	@Test
	public void additionalSonePartsAreIgnored() {
		setupParser("text", new PlainTextPart("This is a long text."), new SonePart(sone));
		setLengthAndCutOffLength(15, 10);
		String output = (String) filter.format(templateContext, "text", parameters);
		assertThat(output, is("This is a &hellip;"));
	}

	@Test
	public void freenetLinkIsRenderedCorrectly() {
		setupParser("KSK@gpl.txt", new FreenetLinkPart("KSK@gpl.txt", "gpl.txt", false));
		Element linkNode = filterText("KSK@gpl.txt");
		verifyLink(linkNode, "/KSK@gpl.txt", "freenet", "KSK@gpl.txt", "gpl.txt");
	}

	private void verifyLink(Element linkNode, String url, String cssClass, String tooltip, String text) {
		assertThat(linkNode.nodeName(), is("a"));
		assertThat(linkNode.attributes().asList(), containsInAnyOrder(
				new Attribute("href", url),
				new Attribute("class", cssClass),
				new Attribute("title", tooltip)
		));
		assertThat(linkNode.text(), is(text));
	}

	@Test
	public void trustedFreenetLinkIsRenderedWithCorrectCssClass() {
		setupParser("KSK@gpl.txt", new FreenetLinkPart("KSK@gpl.txt", "gpl.txt", true));
		Element linkNode = filterText("KSK@gpl.txt");
		verifyLink(linkNode, "/KSK@gpl.txt", "freenet-trusted", "KSK@gpl.txt", "gpl.txt");
	}

	private Element filterText(String text) {
		String output = (String) filter.format(templateContext, text, Collections.<String, Object>emptyMap());
		return Jsoup.parseBodyFragment(output).body().child(0);
	}

	@Test
	public void internetLinkIsRenderedCorrectly() throws Exception {
		setupParser("http://test.com/test.html", new LinkPart("http://test.com/test.html", "test.com/test.html"));
		Element linkNode = filterText("http://test.com/test.html");
		verifyLink(linkNode, "/external-link/?_CHECKED_HTTP_=" + URLEncoder.encode("http://test.com/test.html", "UTF-8"), "internet",
				"http://test.com/test.html", "test.com/test.html");
	}

	@Test
	public void sonePartsAreRenderedCorrectly() {
		setupParser("sone://" + SONE_IDENTITY, new SonePart(sone));
		Element linkNode = filterText("sone://" + SONE_IDENTITY);
		verifyLink(linkNode, "viewSone.html?sone=" + SONE_IDENTITY, "in-sone", "First", "First");
	}

	private Sone setupSone(String identity, String name, String firstName) {
		Sone sone = mock(Sone.class);
		when(sone.getId()).thenReturn(identity);
		when(sone.getProfile()).thenReturn(new Profile(sone));
		when(sone.getName()).thenReturn(name);
		sone.getProfile().setFirstName(firstName);
		when(core.getSone(identity)).thenReturn(Optional.of(sone));
		return sone;
	}

	@Test
	public void sonePartsWithUnknownSoneIsRenderedAsLinkToWebOfTrust() {
		Sone sone = setupSone(SONE_IDENTITY, null, "First");
		setupParser("sone://" + SONE_IDENTITY, new SonePart(sone));
		Element linkNode = filterText("sone://" + SONE_IDENTITY);
		verifyLink(linkNode, "/WebOfTrust/ShowIdentity?id=" + SONE_IDENTITY, "in-sone", SONE_IDENTITY, SONE_IDENTITY);
	}

	@Test
	public void postPartIsCutOffCorrectlyWhenThereAreSpaces() {
		Post post = setupPost(sone, "1234 678901 345 789012 45678 01.");
		setupParser("post://" + POST_ID, new PostPart(post));
		Element linkNode = filterText("post://" + POST_ID);
		verifyLink(linkNode, "viewPost.html?post=" + POST_ID, "in-sone", "First", "1234 678901 345…");
	}

	private Post setupPost(Sone sone, String value) {
		Post post = mock(Post.class);
		when(post.getId()).thenReturn(POST_ID);
		when(post.getSone()).thenReturn(sone);
		when(post.getText()).thenReturn(value);
		return post;
	}

	@Test
	public void postPartIsCutOffCorrectlyWhenThereAreNoSpaces() {
		Post post = setupPost(sone, "1234567890123456789012345678901.");
		setupParser("post://" + POST_ID, new PostPart(post));
		Element linkNode = filterText("post://" + POST_ID);
		verifyLink(linkNode, "viewPost.html?post=" + POST_ID, "in-sone", "First", "12345678901234567890…");
	}

	@Test
	public void postPartShorterThan21CharsIsNotCutOff() {
		Post post = setupPost(sone, "12345678901234567890");
		setupParser("post://" + POST_ID, new PostPart(post));
		Element linkNode = filterText("post://" + POST_ID);
		verifyLink(linkNode, "viewPost.html?post=" + POST_ID, "in-sone", "First", "12345678901234567890");
	}

	@Test
	public void multiplePartsAreRenderedCorrectly() {
		PartContainer parts = new PartContainer();
		parts.add(new PlainTextPart("te"));
		parts.add(new PlainTextPart("xt"));
		setupParser("text", parts);
		String result = (String) filter.format(templateContext, "text", Collections.<String, Object>emptyMap());
		assertThat(result, is("text"));
	}

	@Test
	public void freemailAddressIsDisplayedCorrectly() {
		setupParser(SONE_FREEMAIL, new FreemailPart("sone", FREEMAIL_ID, SONE_IDENTITY));
		Element linkNode = filterText(SONE_FREEMAIL);
		verifyLink(linkNode, "/Freemail/NewMessage?to=" + SONE_IDENTITY, "in-sone", "First\n" + SONE_FREEMAIL, "sone@First.freemail");
	}

}
