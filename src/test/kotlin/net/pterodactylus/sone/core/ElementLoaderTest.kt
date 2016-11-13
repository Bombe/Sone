package net.pterodactylus.sone.core

import com.google.inject.Guice.createInjector
import net.pterodactylus.sone.test.bindMock
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.notNullValue
import org.junit.Test

/**
 * Unit test for [ElementLoader].
 */
class ElementLoaderTest {

	@Test
	fun `default image loader can be loaded by guice`() {
		val injector = createInjector(bindMock<FreenetInterface>())
		assertThat(injector.getInstance(ElementLoader::class.java), notNullValue());
	}

}
