package net.pterodactylus.sone.utils

import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import java.util.concurrent.*
import kotlin.test.*

/**
 * Unit test for Object utils.
 */
class ObjectsTest {

	@Test
	fun `non-null value is turned into a list with one element`() {
		assertThat(5.asList(), contains(5))
	}

	@Test
	fun `null value is turned into empty list`() {
		assertThat(null.asList(), empty())
	}

	@Test
	fun `exception is thrown for null and true condition`() {
		assertFailsWith(IllegalArgumentException::class) {
			null.throwOnNullIf(true) { IllegalArgumentException() }
		}
	}

	@Test
	fun `exception is not thrown for null and false condition`() {
		assertThat(null.throwOnNullIf(false) { IllegalArgumentException() }, nullValue())
	}

	@Test
	fun `exception is not thrown for any and true condition`() {
		val any = Any()
		assertThat(any.throwOnNullIf(true) { IllegalArgumentException() }, equalTo(any))
	}

	@Test
	fun `exception is not thrown for any and false condition`() {
		val any = Any()
		assertThat(any.throwOnNullIf(false) { IllegalArgumentException() }, equalTo(any))
	}

	@Test
	fun `onNull is executed on null`() {
		val called = CountDownLatch(1)
		null.onNull { called.countDown() }
		assertThat(called.count, equalTo(0L))
	}

	@Test
	fun `onNull returns null when called on null`() {
		assertThat(null.onNull {}, nullValue())
	}

	@Test
	fun `onNull is not executed on non-null`() {
		val called = CountDownLatch(1)
		Any().onNull { called.countDown() }
		assertThat(called.count, equalTo(1L))
	}

	@Test
	fun `onNull returns object when called on non-null`() {
		val any = Any()
		assertThat(any.onNull {}, sameInstance(any))
	}

}
