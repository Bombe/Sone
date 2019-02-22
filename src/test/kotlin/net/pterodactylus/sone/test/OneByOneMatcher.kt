package net.pterodactylus.sone.test

import org.hamcrest.Description
import org.hamcrest.TypeSafeDiagnosingMatcher

class OneByOneMatcher<A> : TypeSafeDiagnosingMatcher<A>() {
	private data class Matcher<in A, out V>(val expected: V, val actual: (A) -> V, val description: String)

	private val matchers = mutableListOf<Matcher<A, *>>()

	fun <V> expect(description: String, expected: V, actual: (A) -> V) {
		matchers += Matcher(expected, actual, description)
	}

	override fun describeTo(description: Description) {
		matchers.forEachIndexed { index, matcher ->
			if (index > 0) {
				description.appendText(", ")
			}
			description.appendText("${matcher.description} is ").appendValue(matcher.expected)
		}
	}

	override fun matchesSafely(item: A, mismatchDescription: Description) =
			matchers.all {
				if (it.expected != it.actual(item)) {
					mismatchDescription.appendText("${it.description} is ").appendValue(it.actual(item))
					false
				} else {
					true
				}
			}
}
