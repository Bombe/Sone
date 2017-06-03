package net.pterodactylus.sone.web.pages

import com.google.common.base.Ticker
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.utils.Pagination
import net.pterodactylus.sone.utils.emptyToNull
import net.pterodactylus.sone.utils.paginate
import net.pterodactylus.sone.utils.parameters
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.sone.web.pages.SearchPage.Optionality.FORBIDDEN
import net.pterodactylus.sone.web.pages.SearchPage.Optionality.OPTIONAL
import net.pterodactylus.sone.web.pages.SearchPage.Optionality.REQUIRED
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import net.pterodactylus.util.text.StringEscaper
import net.pterodactylus.util.text.TextException
import java.util.concurrent.TimeUnit.MINUTES

/**
 * This page lets the user search for posts and replies that contain certain
 * words.
 */
class SearchPage @JvmOverloads constructor(template: Template, webInterface: WebInterface, ticker: Ticker = Ticker.systemTicker()):
		SoneTemplatePage("search.html", template, "Page.Search.Title", webInterface, false) {

	private val cache: Cache<Iterable<Phrase>, Pagination<Post>> = CacheBuilder.newBuilder().ticker(ticker).expireAfterAccess(5, MINUTES).build()

	override fun handleRequest(request: FreenetRequest, templateContext: TemplateContext) {
		val phrases = try {
			request.parameters["query"].emptyToNull?.parse()
		} catch (te: TextException) {
			redirect("index.html")
		}
				?: redirect("index.html")

		when (phrases.size) {
			0 -> redirect("index.html")
			1 -> phrases.first().phrase.also { word ->
				when {
					word.removePrefix("sone://").let(webInterface.core::getSone).isPresent -> redirect("viewSone.html?sone=${word.removePrefix("sone://")}")
					word.removePrefix("post://").let(webInterface.core::getPost).isPresent -> redirect("viewPost.html?post=${word.removePrefix("post://")}")
					word.removePrefix("reply://").let(webInterface.core::getPostReply).isPresent -> redirect("viewPost.html?post=${word.removePrefix("reply://").let(webInterface.core::getPostReply).get().postId}")
					word.removePrefix("album://").let(webInterface.core::getAlbum) != null -> redirect("imageBrowser.html?album=${word.removePrefix("album://")}")
					word.removePrefix("image://").let { webInterface.core.getImage(it, false) } != null -> redirect("imageBrowser.html?image=${word.removePrefix("image://")}")
				}
			}
		}

		val sonePagination = webInterface.core.sones
				.scoreAndPaginate(phrases) { it.allText() }
				.apply { page = request.parameters["sonePage"].emptyToNull?.toIntOrNull() ?: 0 }
		val postPagination = cache.get(phrases) {
			webInterface.core.sones
					.flatMap(Sone::getPosts)
					.filter { Post.FUTURE_POSTS_FILTER.apply(it) }
					.scoreAndPaginate(phrases) { it.allText() }
		}.apply { page = request.parameters["postPage"].emptyToNull?.toIntOrNull() ?: 0 }

		templateContext["sonePagination"] = sonePagination
		templateContext["soneHits"] = sonePagination.items
		templateContext["postPagination"] = postPagination
		templateContext["postHits"] = postPagination.items
	}

	private fun <T> Iterable<T>.scoreAndPaginate(phrases: Iterable<Phrase>, texter: (T) -> String) =
			map { it to score(texter(it), phrases) }
					.filter { it.second > 0 }
					.sortedByDescending { it.second }
					.map { it.first }
					.paginate(webInterface.core.preferences.postsPerPage)

	private fun Sone.names() =
			listOf(name, profile.firstName, profile.middleName, profile.lastName)
					.filterNotNull()
					.joinToString("")

	private fun Sone.allText() =
			(names() + profile.fields.map { "${it.name} ${it.value}" }.joinToString(" ", " ")).toLowerCase()

	private fun Post.allText() =
			(text + recipient.orNull()?.let { " ${it.names()}" } + webInterface.core.getReplies(id)
					.filter { PostReply.FUTURE_REPLY_FILTER.apply(it) }
					.map { "${it.sone.names()} ${it.text}" }.joinToString(" ", " ")).toLowerCase()

	private fun score(text: String, phrases: Iterable<Phrase>): Double {
		val requiredPhrases = phrases.count { it.required }
		val requiredHits = phrases.filter(Phrase::required)
				.map(Phrase::phrase)
				.flatMap { text.findAll(it) }
				.map { Math.pow(1 - it / text.length.toDouble(), 2.0) }
				.sum()
		val optionalHits = phrases.filter(Phrase::optional)
				.map(Phrase::phrase)
				.flatMap { text.findAll(it) }
				.map { Math.pow(1 - it / text.length.toDouble(), 2.0) }
				.sum()
		val forbiddenHits = phrases.filter(Phrase::forbidden)
				.map(Phrase::phrase)
				.map { text.findAll(it).size }
				.sum()
		return requiredHits * 3 + optionalHits + (requiredHits - requiredPhrases) * 5 - (forbiddenHits * 2)
	}

	private fun String.findAll(needle: String): List<Int> {
		var nextIndex = indexOf(needle)
		val positions = mutableListOf<Int>()
		while (nextIndex != -1) {
			positions += nextIndex
			nextIndex = indexOf(needle, nextIndex + 1)
		}
		return positions
	}

	private fun String.parse() =
			StringEscaper.parseLine(this)
					.map(String::toLowerCase)
					.map {
						when {
							it == "+" || it == "-" -> Phrase(it, OPTIONAL)
							it.startsWith("+") -> Phrase(it.drop(1), REQUIRED)
							it.startsWith("-") -> Phrase(it.drop(1), FORBIDDEN)
							else -> Phrase(it, OPTIONAL)
						}
					}

	private fun redirect(target: String): Nothing = throw RedirectException(target)

	enum class Optionality {
		OPTIONAL,
		REQUIRED,
		FORBIDDEN
	}

	private data class Phrase(val phrase: String, val optionality: Optionality) {
		val required = optionality == REQUIRED
		val forbidden = optionality == FORBIDDEN
		val optional = optionality == OPTIONAL
	}

}
