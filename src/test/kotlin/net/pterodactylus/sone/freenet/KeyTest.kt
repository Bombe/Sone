package net.pterodactylus.sone.freenet

import freenet.keys.*
import net.pterodactylus.sone.freenet.Key.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*

/**
 * Unit test for [Key].
 */
class KeyTest {

	private val uri = FreenetURI("SSK@$routingKey,$cryptoKey,$extra/some-site-12/foo/bar.html")
	private val key: Key = from(uri)

	@Test
	fun keyCanBeCreatedFromFreenetUri() {
		assertThat(key.routingKey, equalTo(routingKey))
		assertThat(key.cryptoKey, equalTo(cryptoKey))
		assertThat(key.extra, equalTo(extra))
	}

	@Test
	fun keyCanBeConvertedToUsk() {
		val uskUri = key.toUsk("other-site", 15, "some", "path.html")
		assertThat(uskUri.toString(), equalTo("USK@$routingKey,$cryptoKey,$extra/other-site/15/some/path.html"))
	}

	@Test
	fun keyCanBeConvertedToSskWithoutEdition() {
		val uskUri = key.toSsk("other-site", "some", "path.html")
		assertThat(uskUri.toString(), equalTo("SSK@$routingKey,$cryptoKey,$extra/other-site/some/path.html"))
	}

	@Test
	fun keyCanBeConvertedToSskWithEdition() {
		val uskUri = key.toSsk("other-site", 15, "some", "path.html")
		assertThat(uskUri.toString(), equalTo("SSK@$routingKey,$cryptoKey,$extra/other-site-15/some/path.html"))
	}

	@Test
	fun routingKeyIsExtractCorrectly() {
		assertThat(routingKey(uri), equalTo(routingKey))
	}

}

private const val routingKey = "NfUYvxDwU9vqb2mh-qdT~DYJ6U0XNbxMGGoLe0aCHJs"
private const val cryptoKey = "Miglsgix0VR56ZiPl4NgjnUd~UdrnHqIvXJ3KKHmxmI"
private const val extra = "AQACAAE"
