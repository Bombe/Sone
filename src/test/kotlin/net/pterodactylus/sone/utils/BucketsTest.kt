package net.pterodactylus.sone.utils

import freenet.support.api.Bucket
import net.pterodactylus.sone.test.mock
import org.junit.Test
import org.mockito.Mockito.verify
import kotlin.test.fail

/**
 * Unit test for [freenet.support.api.Bucket]-related utilities.
 */
class BucketsTest {

	private val bucket = mock<Bucket>()

	@Test
	fun `bucket is freed after use without exception`() {
		bucket.use { }
		verify(bucket).free()
	}

	@Test
	fun `bucket is freed after use with exceptions`() {
		try {
			bucket.use { throw Exception() }
			@Suppress("UNREACHABLE_CODE")
			fail()
		} catch (e: Exception) {
		} finally {
			verify(bucket).free()
		}
	}

}
