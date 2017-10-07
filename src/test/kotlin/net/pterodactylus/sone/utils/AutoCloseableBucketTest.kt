package net.pterodactylus.sone.utils

import freenet.support.api.Bucket
import net.pterodactylus.sone.test.mock
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.Mockito.verify

class AutoCloseableBucketTest {

	private val bucket = mock<Bucket>()
	private val autoCloseableBucket = AutoCloseableBucket(bucket)

	@Test
	fun `bucket can be retrieved`() {
		assertThat(autoCloseableBucket.bucket, equalTo(bucket))
	}

	@Test
	fun `bucket will be free’d when close is called`() {
		autoCloseableBucket.close()
		verify(bucket).free()
	}

}
