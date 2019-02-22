package net.pterodactylus.sone.utils

import net.pterodactylus.sone.test.hasPages
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for [Pagination].
 */
class PaginationTest {

	private val items = listOf(1, 2, 3, 4, 5)
	private val pagination = Pagination(items, 2)

	@Test
	fun `pagination can be created from iterable`() {
		val pagination = listOf(1, 2, 3, 4, 5).asIterable().paginate(2)
		assertThat(pagination, hasPages(3).isOnPage(0))
	}

	@Test
	fun `new pagination is at page 0`() {
		assertThat(pagination.page, equalTo(0))
	}

	@Test
	fun `new pagination is at page number 1`() {
		assertThat(pagination.pageNumber, equalTo(1))
	}

	@Test
	fun `setting a page to less than 0 sets page to 0`() {
		pagination.page = -1
		assertThat(pagination.page, equalTo(0))
	}

	@Test
	fun `setting page to a valid page sets page`() {
		pagination.page = 1
		assertThat(pagination.page, equalTo(1))
	}

	@Test
	fun `setting a too large page will cap the page`() {
		pagination.page = 100
		assertThat(pagination.page, equalTo(2))
	}

	@Test
	fun `the page count is returned correctly`() {
		assertThat(pagination.pageCount, equalTo(3))
	}

	@Test
	fun `page size is returned correctly`() {
		assertThat(pagination.pageSize, equalTo(2))
	}

	@Test
	fun `a page size of less than 1 is set to 1`() {
		pagination.pageSize = 0
		assertThat(pagination.pageSize, equalTo(1))
	}

	@Test
	fun `changing page size sets new page correctly`() {
		pagination.page = 1
		pagination.pageSize = 1
		assertThat(pagination.page, equalTo(2))
	}

	@Test
	fun `changing page size to very large returns to page 0`() {
		pagination.page = 1
		pagination.pageSize = 20
		assertThat(pagination.page, equalTo(0))
	}

	@Test
	fun `item count for current page is page size of first page`() {
		assertThat(pagination.itemCount, equalTo(2))
	}

	@Test
	fun `item count for last page is 1`() {
		pagination.page = 2
		assertThat(pagination.itemCount, equalTo(1))
	}

	@Test
	fun `items on first page are returned correctly`() {
		assertThat(pagination.items, contains(1, 2))
	}

	@Test
	fun `items on last page are returned correctly`() {
		pagination.page = 2
		assertThat(pagination.items, contains(5))
	}

	@Test
	fun `pagination is first on first page`() {
		assertThat(pagination.isFirst, equalTo(true))
	}

	@Test
	fun `pagination is not first on second page`() {
		pagination.page = 1
		assertThat(pagination.isFirst, equalTo(false))
	}

	@Test
	fun `pagination is not first on last page`() {
		pagination.page = 2
		assertThat(pagination.isFirst, equalTo(false))
	}

	@Test
	fun `pagination is not last  on first page`() {
		assertThat(pagination.isLast, equalTo(false))
	}

	@Test
	fun `pagination is not last on second page`() {
		pagination.page = 1
		assertThat(pagination.isLast, equalTo(false))
	}

	@Test
	fun `pagination is last on last page`() {
		pagination.page = 2
		assertThat(pagination.isLast, equalTo(true))
	}

	@Test
	fun `pagination is necessary for three pages`() {
		assertThat(pagination.isNecessary, equalTo(true))
	}

	@Test
	fun `pagination is necessary for two pages`() {
		pagination.pageSize = 4
		assertThat(pagination.isNecessary, equalTo(true))
	}

	@Test
	fun `pagination is not necessary for one page`() {
		pagination.pageSize = 20
		assertThat(pagination.isNecessary, equalTo(false))
	}

	@Test
	fun `previous page is returned correctly for second page`() {
		pagination.page = 1
		assertThat(pagination.previousPage, equalTo(0))
	}

	@Test
	fun `previous page is returned correctly for last page`() {
		pagination.page = 2
		assertThat(pagination.previousPage, equalTo(1))
	}

	@Test
	fun `next page is returned correctly for first page`() {
		assertThat(pagination.nextPage, equalTo(1))
	}

	@Test
	fun `next page is returned correctly for second page`() {
		pagination.page = 1
		assertThat(pagination.nextPage, equalTo(2))
	}

	@Test
	fun `last page is returned correctly`() {
		assertThat(pagination.lastPage, equalTo(2))
	}

	@Test
	fun `iterator returns items on the current page`() {
		assertThat(pagination.iterator().asSequence().toList(), contains(1, 2))
	}

}
