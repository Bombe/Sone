package net.pterodactylus.sone.core

import net.pterodactylus.sone.data.impl.IdOnlySone
import net.pterodactylus.sone.freenet.wot.DefaultIdentity
import net.pterodactylus.sone.freenet.wot.DefaultOwnIdentity
import net.pterodactylus.sone.test.createInsertUri
import net.pterodactylus.sone.test.createRequestUri
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.emptyArray
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import kotlin.test.Test

/**
 * Unit test for [SoneUriCreator].
 */
class SoneUriCreatorTest {

	private val soneUriCreator = SoneUriCreator()

	private val requestUri = soneUriCreator.getRequestUri(sone)
	private val insertUri = soneUriCreator.getInsertUri(sone)

	@Test
	fun `generated request URI is a USK`() {
		assertThat(requestUri.keyType, equalTo("USK"))
	}

	@Test
	fun `generated request URI has correct doc name`() {
		assertThat(requestUri.docName, equalTo("Sone"))
	}

	@Test
	fun `generated request URI has no meta strings`() {
		assertThat(requestUri.allMetaStrings, emptyArray())
	}

	@Test
	fun `generated request URI has correct edition`() {
		assertThat(requestUri.suggestedEdition, equalTo(123L))
	}

	@Test
	fun `insert URI is null if sone’s identity is not an own identity`() {
		val remoteSone = object : IdOnlySone("id") {
			override fun getIdentity() = DefaultIdentity("id", "name", createRequestUri.toString())
		}
		assertThat(soneUriCreator.getInsertUri(remoteSone), nullValue())
	}

	@Test
	fun `generated insert URI is a USK`() {
		assertThat(insertUri!!.keyType, equalTo("USK"))
	}

	@Test
	fun `generated insert URI has correct doc name`() {
		assertThat(insertUri!!.docName, equalTo("Sone"))
	}

	@Test
	fun `generated insert URI has no meta strings`() {
		assertThat(insertUri!!.allMetaStrings, emptyArray())
	}

	@Test
	fun `generated insert URI has correct edition`() {
		assertThat(insertUri!!.suggestedEdition, equalTo(123L))
	}

}

private val sone = object : IdOnlySone("id") {
	override fun getIdentity() =
			DefaultOwnIdentity("id", "name", createRequestUri.toString(), createInsertUri.toString())

	override fun getLatestEdition() = 123L
}
