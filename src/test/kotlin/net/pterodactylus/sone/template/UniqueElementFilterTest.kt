package net.pterodactylus.sone.template

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for [UniqueElementFilter].
 */
class UniqueElementFilterTest {

	private val filter = UniqueElementFilter()

	@Test
	fun `filter returns object if object is not a collection`() {
		val someObject = Any()
		assertThat(filter.format(null, someObject, null), equalTo<Any>(someObject))
	}

	@Test
	fun `filter returns a set containing all unique elements of a given collection`() {
		val objects = listOf(Any(), Any(), Any())
		val collection = listOf(objects[0], objects[1], objects[2], objects[0], objects[1])
		assertThat(filter.format(null, collection, null), equalTo<Any>(objects.toSet()))
	}

}
