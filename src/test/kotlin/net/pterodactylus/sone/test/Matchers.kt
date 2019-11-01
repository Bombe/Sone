package net.pterodactylus.sone.test

import net.pterodactylus.sone.freenet.wot.*
import net.pterodactylus.sone.utils.*
import net.pterodactylus.util.web.*
import org.hamcrest.*
import org.hamcrest.Matchers.*

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

fun isIdentity(id: String, nickname: String, requestUri: String, contexts: Matcher<out Iterable<String>>, properties: Matcher<out Map<out String, String>>) =
		AttributeMatcher<Identity>("identity")
				.addAttribute("id", id, Identity::getId)
				.addAttribute("nickname", nickname, Identity::getNickname)
				.addAttribute("requestUri", requestUri, Identity::getRequestUri)
				.addAttribute("contexts", Identity::getContexts, contexts)
				.addAttribute("properties", Identity::getProperties, properties)

