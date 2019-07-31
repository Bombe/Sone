package net.pterodactylus.sone.web

import com.google.inject.*
import freenet.l10n.*
import freenet.support.api.*
import net.pterodactylus.sone.core.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.database.*
import net.pterodactylus.sone.freenet.*
import net.pterodactylus.sone.freenet.wot.*
import net.pterodactylus.sone.main.*
import net.pterodactylus.sone.template.*
import net.pterodactylus.sone.text.*
import net.pterodactylus.util.template.*
import javax.inject.*
import javax.inject.Singleton

class WebInterfaceModule : AbstractModule() {

	@Provides
	@Singleton
	fun getTemplateContextFactory(
			soneAccessor: SoneAccessor,
			postAccessor: PostAccessor,
			replyAccessor: ReplyAccessor,
			identityAccessor: IdentityAccessor,
			profileAccessor: ProfileAccessor,
			l10nFilter: L10nFilter,
			parserFilter: ParserFilter,
			renderFilter: RenderFilter,
			linkedElementsFilter: LinkedElementsFilter,
			unknownDateFilter: UnknownDateFilter,
			imageLinkFilter: ImageLinkFilter,
			loaders: Loaders
	) =
			TemplateContextFactory().apply {
				addAccessor(Any::class.java, ReflectionAccessor())
				addAccessor(Collection::class.java, CollectionAccessor())
				addAccessor(Sone::class.java, soneAccessor)
				addAccessor(Post::class.java, postAccessor)
				addAccessor(Reply::class.java, replyAccessor)
				addAccessor(Album::class.java, AlbumAccessor())
				addAccessor(Image::class.java, ImageAccessor())
				addAccessor(Identity::class.java, identityAccessor)
				addAccessor(Trust::class.java, TrustAccessor())
				addAccessor(HTTPRequest::class.java, HttpRequestAccessor())
				addAccessor(Profile::class.java, profileAccessor)

				addFilter("date", DateFilter())
				addFilter("html", HtmlFilter())
				addFilter("replace", ReplaceFilter())
				addFilter("store", StoreFilter())
				addFilter("l10n", l10nFilter)
				addFilter("substring", SubstringFilter())
				addFilter("xml", XmlFilter())
				addFilter("change", RequestChangeFilter())
				addFilter("match", MatchFilter())
				addFilter("css", CssClassNameFilter())
				addFilter("js", JavascriptFilter())
				addFilter("parse", parserFilter)
				addFilter("shorten", ShortenFilter())
				addFilter("render", renderFilter)
				addFilter("linked-elements", linkedElementsFilter)
				addFilter("render-linked-element", LinkedElementRenderFilter())
				addFilter("reparse", ReparseFilter())
				addFilter("unknown", unknownDateFilter)
				addFilter("format", FormatFilter())
				addFilter("duration", DurationFormatFilter())
				addFilter("sort", CollectionSortFilter())
				addFilter("image-link", imageLinkFilter)
				addFilter("replyGroup", ReplyGroupFilter())
				addFilter("in", ContainsFilter())
				addFilter("unique", UniqueElementFilter())
				addFilter("mod", ModFilter())
				addFilter("paginate", PaginationFilter())

				addProvider(TemplateProvider.TEMPLATE_CONTEXT_PROVIDER)
				addProvider(loaders.templateProvider)
			}

	@Provides
	fun getSoneAccessor(core: Core, timeTextConverter: TimeTextConverter) =
			SoneAccessor(core, timeTextConverter)

	@Provides
	fun getPostAccessor(core: Core) =
			PostAccessor(core)

	@Provides
	fun getReplyAccessor(core: Core) =
			ReplyAccessor(core)

	@Provides
	fun getIdentityAccessor(core: Core) =
			IdentityAccessor(core)

	@Provides
	fun getProfileAccessor(core: Core) =
			ProfileAccessor(core)

	@Provides
	fun getL10nFilter(l10n: BaseL10n) =
			L10nFilter(l10n)

	@Provides
	fun getParserFilter(core: Core, soneTextParser: SoneTextParser) =
			ParserFilter(core, soneTextParser)

	@Provides
	fun getRenderFilter(soneProvider: SoneProvider, soneTextParser: SoneTextParser, htmlFilter: HtmlFilter) =
			RenderFilter(soneProvider, soneTextParser, htmlFilter)

	@Provides
	fun getLinkedElementsFilter(elementLoader: ElementLoader) =
			LinkedElementsFilter(elementLoader)

	@Provides
	fun getUnknownDateFilter(l10n: BaseL10n) =
			UnknownDateFilter(l10n, "View.Sone.Text.UnknownDate")

	@Provides
	fun getImageLinkFilter(core: Core) =
			ImageLinkFilter(core)

	@Provides
	@Named("toadletPathPrefix")
	fun getPathPrefix(): String = "/Sone/"

}
