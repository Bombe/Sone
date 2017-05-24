package net.pterodactylus.sone.utils

import com.google.common.base.Optional.fromNullable
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.Test

class IterablesTest {

	@Test
	fun testMapPresent() {
		val originalList = listOf(1, 2, null, 3, null)
		assertThat(originalList.mapPresent { fromNullable(it) }, contains(1, 2, 3))
	}

}
