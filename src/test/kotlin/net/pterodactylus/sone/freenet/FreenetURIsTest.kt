package net.pterodactylus.sone.freenet

import freenet.keys.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*

/**
 * Unit test for [Key].
 */
class FreenetURIsTest {

	private val uri = FreenetURI("SSK@$routingKey,$cryptoKey,$extra/some-site-12/foo/bar.html")

	@Test
	fun routingKeyIsExtractCorrectly() {
		assertThat(uri.routingKeyString, equalTo(routingKey))
	}

}

private const val routingKey = "NfUYvxDwU9vqb2mh-qdT~DYJ6U0XNbxMGGoLe0aCHJs"
private const val cryptoKey = "Miglsgix0VR56ZiPl4NgjnUd~UdrnHqIvXJ3KKHmxmI"
private const val extra = "AQACAAE"
