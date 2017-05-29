package net.pterodactylus.sone.test

import net.pterodactylus.sone.utils.Pagination
import org.hamcrest.Description
import org.hamcrest.TypeSafeDiagnosingMatcher

/**
 * Hamcrest matcher for [Pagination]s.
 */
class PaginationMatcher(private val page: Int? = null, private val pages: Int? = null):
		TypeSafeDiagnosingMatcher<Pagination<*>>() {

	override fun matchesSafely(pagination: Pagination<*>, mismatchDescription: Description): Boolean {
		page?.let {
			if (pagination.page != page) {
				mismatchDescription.appendText("page is ").appendValue(pagination.page)
				return false
			}
		}
		pages?.let {
			if (pagination.pageCount != pages) {
				mismatchDescription.appendText("total pages is ").appendValue(pagination.pageCount)
				return false
			}
		}
		return true
	}

	override fun describeTo(description: Description) {
		page?.also {
			description.appendText("is on page ").appendValue(page)
			pages?.also {
					description.appendText(" of ").appendValue(pages)
			}
		} ?: pages?.also {
			description.appendText("has ").appendValue(pages).appendText(" pages")
		}
	}

	fun isOnPage(page: Int) = PaginationMatcher(page = page, pages = pages)
	fun hasPages(pages: Int) = PaginationMatcher(page = page, pages = pages)

}

fun isOnPage(page: Int) = PaginationMatcher(page = page)
fun hasPages(pages: Int) = PaginationMatcher(pages = pages)
