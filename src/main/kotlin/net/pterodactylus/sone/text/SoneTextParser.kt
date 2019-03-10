package net.pterodactylus.sone.text

import freenet.keys.FreenetURI
import freenet.support.Base64
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.data.impl.IdOnlySone
import net.pterodactylus.sone.database.PostProvider
import net.pterodactylus.sone.database.SoneProvider
import net.pterodactylus.sone.text.LinkType.CHK
import net.pterodactylus.sone.text.LinkType.FREEMAIL
import net.pterodactylus.sone.text.LinkType.HTTP
import net.pterodactylus.sone.text.LinkType.HTTPS
import net.pterodactylus.sone.text.LinkType.KSK
import net.pterodactylus.sone.text.LinkType.POST
import net.pterodactylus.sone.text.LinkType.SONE
import net.pterodactylus.sone.text.LinkType.SSK
import net.pterodactylus.sone.text.LinkType.USK
import org.bitpedia.util.Base32
import java.net.MalformedURLException
import javax.inject.*

/**
 * [Parser] implementation that can recognize Freenet URIs.
 */
class SoneTextParser @Inject constructor(private val soneProvider: SoneProvider?, private val postProvider: PostProvider?) {

	fun parse(source: String, context: SoneTextParserContext?) =
			source.split("\n")
					.dropWhile { it.trim() == "" }
					.dropLastWhile { it.trim() == "" }
					.mergeMultipleEmptyLines()
					.flatMap { splitLineIntoParts(it, context) }
					.removeEmptyPlainTextParts()
					.mergeAdjacentPlainTextParts()

	private fun splitLineIntoParts(line: String, context: SoneTextParserContext?) =
			generateSequence(PlainTextPart("") as Part to line) { remainder ->
				if (remainder.second == "")
					null
				else
					LinkType.values()
							.mapNotNull { it.findNext(remainder.second) }
							.minBy { it.position }
							.let {
								when {
									it == null -> PlainTextPart(remainder.second) to ""
									it.position == 0 -> it.toPart(context) to it.remainder
									else -> PlainTextPart(remainder.second.substring(0, it.position)) to (it.link + it.remainder)
								}
							}
			}.map { it.first }.toList()

	private fun NextLink.toPart(context: SoneTextParserContext?) = when (linkType) {
		KSK, CHK -> try {
			FreenetURI(link).let { freenetUri ->
				FreenetLinkPart(
						link,
						if (freenetUri.isKSK) {
							freenetUri.guessableKey
						} else {
							freenetUri.metaString ?: freenetUri.docName ?: link.substring(0, 9)
						},
						link.split('?').first()
				)
			}
		} catch (e: MalformedURLException) {
			PlainTextPart(link)
		}
		SSK, USK ->
			try {
				FreenetURI(link).let { uri ->
					uri.docName ?: "${uri.keyType}@${uri.routingKey.freenetBase64}"
				}.let { FreenetLinkPart(link, it, trusted = context?.routingKey?.contentEquals(FreenetURI(link).routingKey) == true) }
			} catch (e: MalformedURLException) {
				PlainTextPart(link)
			}
		SONE -> link.substring(7).let { SonePart(soneProvider?.getSone(it) ?: IdOnlySone(it)) }
		POST -> postProvider?.getPost(link.substring(7))?.let { PostPart(it) } ?: PlainTextPart(link)
		FREEMAIL -> link.indexOf('@').let { atSign ->
			link.substring(atSign + 1, link.length - 9).let { freemailId ->
				FreemailPart(link.substring(0, atSign), freemailId, freemailId.decodedId)
			}
		}
		HTTP, HTTPS -> LinkPart(link, link
				.withoutProtocol
				.withoutWwwPrefix
				.withoutUrlParameters
				.withoutMiddlePathComponents
				.withoutTrailingSlash)
	}

}

private fun List<String>.mergeMultipleEmptyLines() = fold(emptyList<String>()) { previous, current ->
	if (previous.isEmpty()) {
		previous + current
	} else {
		if ((previous.last() == "\n") && (current == "")) {
			previous
		} else {
			previous + ("\n" + current)
		}
	}
}

private fun List<Part>.mergeAdjacentPlainTextParts() = fold(emptyList<Part>()) { parts, part ->
	if ((parts.lastOrNull() is PlainTextPart) && (part is PlainTextPart)) {
		parts.dropLast(1) + PlainTextPart(parts.last().text + part.text)
	} else {
		parts + part
	}
}

private fun List<Part>.removeEmptyPlainTextParts() = filterNot { it == PlainTextPart("") }

private val String.decodedId: String get() = Base64.encode(Base32.decode(this))
private val String.withoutProtocol get() = substring(indexOf("//") + 2)
private val String.withoutUrlParameters get() = split('?').first()

private val String.withoutWwwPrefix
	get() = split("/")
			.replaceFirst { it.split(".").dropWhile { it == "www" }.joinToString(".") }
			.joinToString("/")

private fun <T> List<T>.replaceFirst(replacement: (T) -> T) = mapIndexed { index, element ->
	if (index == 0) replacement(element) else element
}

private val String.withoutMiddlePathComponents
	get() = split("/").let {
		if (it.size > 2) {
			"${it.first()}/…/${it.last()}"
		} else {
			it.joinToString("/")
		}
	}
private val String.withoutTrailingSlash get() = if (endsWith("/")) substring(0, length - 1) else this
private val SoneTextParserContext.routingKey: ByteArray? get() = postingSone?.routingKey
private val Sone.routingKey: ByteArray get() = Base64.decode(id)

private enum class LinkType(private val scheme: String, private val freenetLink: Boolean) {

	KSK("KSK@", true),
	CHK("CHK@", true),
	SSK("SSK@", true),
	USK("USK@", true),
	HTTP("http://", false),
	HTTPS("https://", false),
	SONE("sone://", false) {
		override fun validateLinkLength(length: Int) = length.takeIf { it == 50 }
	},
	POST("post://", false),
	FREEMAIL("", true) {
		override fun findNext(line: String): NextLink? {
			val nextFreemailSuffix = line.indexOf(".freemail").takeIf { it >= 54 } ?: return null
			if (line[nextFreemailSuffix - 53] != '@') return null
			if (!line.substring(nextFreemailSuffix - 52, nextFreemailSuffix).matches(Regex("^[a-z2-7]*\$"))) return null
			val firstCharacterIndex = generateSequence(nextFreemailSuffix - 53) {
				it.minus(1).takeIf { (it >= 0) && line[it].validLocalPart }
			}.lastOrNull() ?: return null
			return NextLink(firstCharacterIndex, this, line.substring(firstCharacterIndex, nextFreemailSuffix + 9), line.substring(nextFreemailSuffix + 9))
		}

		private val Char.validLocalPart get() = (this in ('A'..'Z')) || (this in ('a'..'z')) || (this in ('0'..'9')) || (this == '-') || (this == '_') || (this == '.')
	};

	open fun findNext(line: String): NextLink? {
		val nextLinkPosition = line.indexOf(scheme).takeIf { it != -1 } ?: return null
		val endOfLink = line.substring(nextLinkPosition).findEndOfLink().validate() ?: return null
		val link = line.substring(nextLinkPosition, nextLinkPosition + endOfLink)
		val realNextLinkPosition = if (freenetLink && line.substring(0, nextLinkPosition).endsWith("freenet:")) nextLinkPosition - 8 else nextLinkPosition
		return NextLink(realNextLinkPosition, this, link, line.substring(nextLinkPosition + endOfLink))
	}

	private fun String.findEndOfLink() =
			substring(0, whitespace.find(this)?.range?.start ?: length)
					.dropLastWhile(::isPunctuation)
					.upToFirstUnmatchedParen()

	private fun Int.validate() = validateLinkLength(this)
	protected open fun validateLinkLength(length: Int) = length.takeIf { it > scheme.length }

	private fun String.upToFirstUnmatchedParen() =
			foldIndexed(Pair<Int, Int?>(0, null)) { index, (openParens, firstUnmatchedParen), currentChar ->
				when (currentChar) {
					'(' -> (openParens + 1) to firstUnmatchedParen
					')' -> ((openParens - 1) to (if (openParens == 0) (firstUnmatchedParen ?: index) else firstUnmatchedParen))
					else -> openParens to firstUnmatchedParen
				}
			}.second ?: length

}

private val punctuationChars = listOf('.', ',', '?', '!')
private fun isPunctuation(char: Char) = char in punctuationChars

private val whitespace = Regex("[\\u000a\u0020\u00a0\u1680\u180e\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200a\u200b\u200c\u200d\u202f\u205f\u2060\u2800\u3000]")

private data class NextLink(val position: Int, val linkType: LinkType, val link: String, val remainder: String)

private val ByteArray.freenetBase64 get() = Base64.encode(this)!!
