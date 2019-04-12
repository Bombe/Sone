package net.pterodactylus.sone.test

import net.pterodactylus.util.web.*
import org.hamcrest.*

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

