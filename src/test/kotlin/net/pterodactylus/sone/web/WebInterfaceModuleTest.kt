package net.pterodactylus.sone.web

import com.google.inject.Guice.*
import freenet.client.*
import freenet.clients.http.*
import freenet.l10n.*
import freenet.support.api.*
import net.pterodactylus.sone.core.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.database.*
import net.pterodactylus.sone.freenet.*
import net.pterodactylus.sone.freenet.wot.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.template.*
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.text.*
import net.pterodactylus.sone.web.notification.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.util.notify.*
import net.pterodactylus.util.template.*
import net.pterodactylus.util.web.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import java.util.*
import kotlin.test.Test

class WebInterfaceModuleTest {

	private val webInterfaceModule = WebInterfaceModule()
	private val loaders = mock<Loaders>()
	private val translation = object : Translation {
		override val currentLocale = Locale.ENGLISH
		override fun translate(key: String) = if (key == "View.Sone.Text.UnknownDate") "unknown" else key
	}
	private val additionalModules = arrayOf(
			Core::class.isProvidedByMock(),
			SoneProvider::class.isProvidedByMock(),
			Translation::class.isProvidedBy(translation),
			SoneTextParser::class.isProvidedByMock(),
			ElementLoader::class.isProvidedByMock(),
			Loaders::class.isProvidedBy(loaders),
			HighLevelSimpleClient::class.isProvidedByMock(),
			SessionManager::class.isProvidedByMock()
	)
	private val injector = createInjector(webInterfaceModule, *additionalModules)!!
	private val templateContext by lazy { injector.getInstance<TemplateContextFactory>().createTemplateContext()!! }

	@Test
	fun `template context factory creates template with reflection accessor for objects`() {
		verifyAccessor<Any, ReflectionAccessor>()
	}

	@Test
	fun `template context factory creates template with collection accessor for collections`() {
		verifyAccessor<Collection<*>, CollectionAccessor>()
	}

	@Test
	fun `template context contains sone accessor for sones`() {
		verifyAccessor<Sone, SoneAccessor>()
	}

	@Test
	fun `template context contains post accessor for posts`() {
		verifyAccessor<Post, PostAccessor>()
	}

	@Test
	fun `template context contains reply accessor for replies`() {
		verifyAccessor<Reply<*>, ReplyAccessor>()
	}

	@Test
	fun `template context contains album accessor for albums`() {
		verifyAccessor<Album, AlbumAccessor>()
	}

	@Test
	fun `template context contains image accessor for images`() {
		verifyAccessor<Image, ImageAccessor>()
	}

	@Test
	fun `template context contains identity accessor for identities`() {
		verifyAccessor<Identity, IdentityAccessor>()
	}

	@Test
	fun `template context contains trust accessor for trusts`() {
		verifyAccessor<Trust, TrustAccessor>()
	}

	@Test
	fun `template context contains http request accessor for http requests`() {
		verifyAccessor<HTTPRequest, HttpRequestAccessor>()
	}

	@Test
	fun `template context contains profile accessor for profiles`() {
		verifyAccessor<Profile, ProfileAccessor>()
	}

	private inline fun <reified O, reified A : Accessor> verifyAccessor() {
		assertThat(templateContext.getAccessor(O::class.java), instanceOf(A::class.java))
	}

	@Test
	fun `template context contains date filter`() {
		verifyFilter<DateFilter>("date")
	}

	@Test
	fun `template context contains html filter`() {
		verifyFilter<HtmlFilter>("html")
	}

	@Test
	fun `template context contains replace filter`() {
		verifyFilter<ReplaceFilter>("replace")
	}

	@Test
	fun `template context contains store filter`() {
		verifyFilter<StoreFilter>("store")
	}

	@Test
	fun `template context contains l10n filter`() {
		verifyFilter<L10nFilter>("l10n")
	}

	@Test
	fun `template context contains substring filter`() {
		verifyFilter<SubstringFilter>("substring")
	}

	@Test
	fun `template context contains xml filter`() {
		verifyFilter<XmlFilter>("xml")
	}

	@Test
	fun `template context contains change filter`() {
		verifyFilter<RequestChangeFilter>("change")
	}

	@Test
	fun `template context contains match filter`() {
		verifyFilter<MatchFilter>("match")
	}

	@Test
	fun `template context contains css filter`() {
		verifyFilter<CssClassNameFilter>("css")
	}

	@Test
	fun `template context contains js filter`() {
		verifyFilter<JavascriptFilter>("js")
	}

	@Test
	fun `template context contains parser filter`() {
		verifyFilter<ParserFilter>("parse")
	}

	@Test
	fun `template context contains shorten filter`() {
		verifyFilter<ShortenFilter>("shorten")
	}

	@Test
	fun `template context contains render filter`() {
		verifyFilter<RenderFilter>("render")
	}

	@Test
	fun `template context contains linked elements filter`() {
		verifyFilter<LinkedElementsFilter>("linked-elements")
	}

	@Test
	fun `template context contains linked elements render filter`() {
		verifyFilter<LinkedElementRenderFilter>("render-linked-element")
	}

	@Test
	fun `template context contains reparse filter`() {
		verifyFilter<ReparseFilter>("reparse")
	}

	@Test
	fun `template context contains unknown date filter`() {
		verifyFilter<UnknownDateFilter>("unknown")
	}

	@Test
	fun `unknown date filter uses correct l10n key`() {
		assertThat(getFilter("unknown")!!.format(null, 0L, emptyMap()), equalTo<Any>("unknown"))
	}

	@Test
	fun `template context contains format filter`() {
		verifyFilter<FormatFilter>("format")
	}

	@Test
	fun `template context contains duration format filter`() {
		verifyFilter<DurationFormatFilter>("duration")
	}

	@Test
	fun `template context contains collection sort filter`() {
		verifyFilter<CollectionSortFilter>("sort")
	}

	@Test
	fun `template context contains image link filter`() {
		verifyFilter<ImageLinkFilter>("image-link")
	}

	@Test
	fun `template context contains reply group filter`() {
		verifyFilter<ReplyGroupFilter>("replyGroup")
	}

	@Test
	fun `template context contains contains filter`() {
		verifyFilter<ContainsFilter>("in")
	}

	@Test
	fun `template context unique elements filter`() {
		verifyFilter<UniqueElementFilter>("unique")
	}

	@Test
	fun `template context mod filter`() {
		verifyFilter<ModFilter>("mod")
	}

	@Test
	fun `template context pagination filter`() {
		verifyFilter<PaginationFilter>("paginate")
	}

	@Test
	fun `template context histogram renderer`() {
		verifyFilter<HistogramRenderer>("render-histogram")
	}

	private inline fun <reified F : Filter> verifyFilter(name: String) {
		assertThat(getFilter(name), instanceOf(F::class.java))
	}

	private fun getFilter(name: String): Filter? = templateContext.getFilter(name)

	@Test
	fun `template context factory is created as singleton`() {
	    val factory1 = injector.getInstance<TemplateContextFactory>()
	    val factory2 = injector.getInstance<TemplateContextFactory>()
		assertThat(factory1, sameInstance(factory2))
	}

	@Test
	fun `template from classpath is returned`() {
		val template = Template()
		templateContext["testTemplate"] = template
		assertThat(templateContext.getTemplate("testTemplate"), sameInstance(template))
	}

	@Test
	fun `template from loaders’ provider is returned`() {
		val template = Template()
		whenever(loaders.templateProvider).thenReturn(TemplateProvider { _, templateName ->
			template.takeIf { templateName == "testTemplate" }
		})
		assertThat(templateContext.getTemplate("testTemplate"), sameInstance(template))
	}

	@Test
	fun `page toadlet factory is created with correct prefix`() {
		val page = mock<Page<FreenetRequest>>()
	    assertThat(injector.getInstance<PageToadletFactory>().createPageToadlet(page).path(), startsWith("/Sone/"))
	}

	@Test
	fun `notification manager is created as singleton`() {
		val firstNotificationManager = injector.getInstance<NotificationManager>()
		val secondNotificationManager = injector.getInstance<NotificationManager>()
		assertThat(firstNotificationManager, sameInstance(secondNotificationManager))
	}

	@Test
	fun `notification handler can be created`() {
		assertThat(injector.getInstance<NotificationHandler>(), notNullValue())
	}

	@Test
	fun `notification handler is created as singleton`() {
		val firstNotificationHandler = injector.getInstance<NotificationHandler>()
		val secondNotificationHandler = injector.getInstance<NotificationHandler>()
		assertThat(firstNotificationHandler, sameInstance(secondNotificationHandler))
	}

}
