package net.pterodactylus.sone.core

import net.pterodactylus.sone.core.FreenetInterface.InsertToken
import net.pterodactylus.sone.core.FreenetInterface.InsertTokenSupplier
import net.pterodactylus.sone.data.TemporaryImage
import net.pterodactylus.sone.data.impl.*
import net.pterodactylus.sone.test.getInstance
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.baseInjector
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.notNullValue
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

/**
 * Unit test for [ImageInserter].
 */
class ImageInserterTest {

	private val temporaryImage = mock<TemporaryImage>().apply { whenever(id).thenReturn("image-id") }
	private val image = ImageImpl("image-id")
	private val freenetInterface = mock<FreenetInterface>()
	private val insertToken = mock<InsertToken>()
	private val insertTokenSupplier: InsertTokenSupplier = mock<InsertTokenSupplier>().apply { whenever(apply(any())).thenReturn(insertToken) }
	private val imageInserter = ImageInserter(freenetInterface, insertTokenSupplier)

	@Test
	fun `inserter inserts image`() {
		imageInserter.insertImage(temporaryImage, image)
		verify(freenetInterface).insertImage(eq(temporaryImage), eq(image), any(InsertToken::class.java))
	}

	@Test
	fun `exception when inserting image is ignored`() {
		doThrow(SoneException::class.java).whenever(freenetInterface).insertImage(eq(temporaryImage), eq(image), any(InsertToken::class.java))
		imageInserter.insertImage(temporaryImage, image)
		verify(freenetInterface).insertImage(eq(temporaryImage), eq(image), any(InsertToken::class.java))
	}

	@Test
	fun `cancelling image insert that is not running does nothing`() {
		imageInserter.cancelImageInsert(image)
		verify(insertToken, never()).cancel()
	}

	@Test
	fun `cancelling image cancels the insert token`() {
		imageInserter.insertImage(temporaryImage, image)
		imageInserter.cancelImageInsert(image)
		verify(insertToken).cancel()
	}

	@Test
	fun `image inserter can be created by dependency injection`() {
	    assertThat(baseInjector.getInstance<ImageInserter>(), notNullValue())
	}

}
