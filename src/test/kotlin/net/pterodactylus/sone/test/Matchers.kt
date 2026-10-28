package net.pterodactylus.sone.test

import freenet.support.*
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.freenet.wot.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.util.web.*
import org.hamcrest.*
import org.hamcrest.Matchers.*

/**
 * A kotlin-ified version of Hamcrest’s [anything()][anything]. It matches
 * everything and has the right type, too!
 */
inline fun <reified T> everything(): Matcher<T> = any(T::class.java)

/**
 * Returns a [hamcrest matcher][Matcher] constructed from the given predicate.
 */
fun <T> matches(description: String? = null, predicate: (T) -> Boolean) = object : TypeSafeDiagnosingMatcher<T>() {

	override fun matchesSafely(item: T, mismatchDescription: Description) =
			predicate(item).onFalse {
				mismatchDescription.appendValue(item).appendText(" did not match predicate")
			}

	override fun describeTo(description: Description) {
		description.appendText("matches predicate ").appendValue(predicate)
	}

}.let { matcher ->
	description?.let { describedAs(description, matcher) } ?: matcher
}

fun hasHeader(name: String, value: String) = object : TypeSafeDiagnosingMatcher<Header>() {
	override fun matchesSafely(item: Header, mismatchDescription: Description) =
			compare(item.name, { it.equals(name, ignoreCase = true) }) { mismatchDescription.appendText("name is ").appendValue(it) }
					?: compare(item.hasValue(value), { it }) { mismatchDescription.appendText("does not have value ").appendValue(value) }
					?: true

	override fun describeTo(description: Description) {
		description.appendText("name is ").appendValue(name)
				.appendText(", value is ").appendValue(value)
	}
}

fun <T> handleMatcher(matcher: Matcher<T>, item: T, mismatchDescription: Description) =
	matcher.matches(item)
		.onFalse { matcher.describeMismatch(item, mismatchDescription) }

fun <T : Any> compare(value: T, comparison: (T) -> Boolean, onError: (T) -> Unit) =
		false.takeUnless { comparison(value) }
				?.also { onError(value) }

fun <K, V> isEmptyMap() = object : TypeSafeDiagnosingMatcher<Map<K, V>>() {
	override fun describeTo(description: Description) {
		description.appendText("empty map")
	}

	override fun matchesSafely(item: Map<K, V>, mismatchDescription: Description) =
			item.isEmpty().onFalse {
				mismatchDescription.appendText("was ").appendValue(item)
			}
}

fun isTrust(trust: Int?, score: Int?, rank: Int?) =
		AttributeMatcher<Trust>("trust")
				.addAttribute("trust", trust, Trust::explicit)
				.addAttribute("score", score, Trust::implicit)
				.addAttribute("rank", rank, Trust::distance)

fun isTrusted(ownIdentity: OwnIdentity, trust: Matcher<Trust>) = object : TypeSafeDiagnosingMatcher<Identity>() {
	override fun matchesSafely(item: Identity, mismatchDescription: Description) =
			item.getTrust(ownIdentity)?.let { foundTrust ->
				trust.matches(foundTrust).onFalse {
					trust.describeMismatch(foundTrust, mismatchDescription)
				}
			} ?: {
				mismatchDescription.appendText("not trusted")
				false
			}()

	override fun describeTo(description: Description) {
		description
				.appendText("trusted by ").appendValue(ownIdentity)
				.appendText(" with ").appendValue(trust)
	}
}

fun isIdentity(id: String, nickname: String?, requestUri: String, contexts: Matcher<out Iterable<String>>, properties: Matcher<out Map<out String, String>>) =
		AttributeMatcher<Identity>("identity")
				.addAttribute("id", id, Identity::getId)
				.addAttribute("nickname", nickname, Identity::getNickname)
				.addAttribute("requestUri", requestUri, Identity::getRequestUri)
				.addAttribute("contexts", Identity::getContexts, contexts)
				.addAttribute("properties", Identity::getProperties, properties)

fun isOwnIdentity(id: String, nickname: String, requestUri: String, insertUri: String, contexts: Matcher<Iterable<String>>, properties: Matcher<Map<out String, String>>) =
		AttributeMatcher<OwnIdentity>("own identity")
				.addAttribute("id", id, OwnIdentity::getId)
				.addAttribute("nickname", nickname, OwnIdentity::getNickname)
				.addAttribute("request uri", requestUri, OwnIdentity::getRequestUri)
				.addAttribute("insert uri", insertUri, OwnIdentity::getInsertUri)
				.addAttribute("contexts", OwnIdentity::getContexts, contexts)
				.addAttribute("properties", OwnIdentity::getProperties, properties)

fun hasField(name: String, valueMatcher: Matcher<String>) = object : TypeSafeDiagnosingMatcher<SimpleFieldSet>() {
	override fun matchesSafely(item: SimpleFieldSet, mismatchDescription: Description) =
		handleMatcher(valueMatcher, item.get(name), mismatchDescription)

	override fun describeTo(description: Description) {
		description
				.appendText("simple field set with key ").appendValue(name)
				.appendText(", value ").appendValue(valueMatcher)
	}
}

fun isPost(isRecipientId: Matcher<String?> = any(String::class.java)) = AttributeMatcher<Post>("post")
		.addAttribute("recipient ID", { it.recipientId.orNull() }, isRecipientId)

/**
 * [TypeSafeDiagnosingMatcher] implementation that aims to cut down boilerplate on verifying the attributes
 * of typical container objects.
 */
class AttributeMatcher<T>(private val objectName: String) : TypeSafeDiagnosingMatcher<T>() {

	private data class AttributeToMatch<T, V>(
			val name: String,
			val getter: (T) -> V,
			val matcher: Matcher<out V>
	)

	private val attributesToMatch = mutableListOf<AttributeToMatch<T, *>>()

	/**
	 * Adds an attribute to check for equality, returning `this`.
	 */
	fun <V> addAttribute(name: String, expected: V, getter: (T) -> V): AttributeMatcher<T> = apply {
		attributesToMatch.add(AttributeToMatch(name, getter, describedAs("$name %0", equalTo(expected), expected)))
	}

	/**
	 * Adds an attribute to check with the given [hamcrest matcher][Matcher].
	 */
	fun <V> addAttribute(name: String, getter: (T) -> V, matcher: Matcher<out V>) = apply {
		attributesToMatch.add(AttributeToMatch(name, getter, matcher))
	}

	override fun describeTo(description: Description) {
		attributesToMatch.forEachIndexed { index, attributeToMatch ->
			if (index == 0) {
				description.appendText("$objectName with ")
			} else {
				description.appendText(", ")
			}
			attributeToMatch.matcher.describeTo(description)
		}
	}

	override fun matchesSafely(item: T, mismatchDescription: Description): Boolean =
			attributesToMatch.fold(true) { matches, attributeToMatch ->
				if (!matches) {
					false
				} else {
					if (!attributeToMatch.matcher.matches(attributeToMatch.getter(item))) {
						mismatchDescription.appendText("but ${attributeToMatch.name} ")
						attributeToMatch.matcher.describeMismatch(attributeToMatch.getter(item), mismatchDescription)
						false
					} else {
						true
					}
				}
			}

}
