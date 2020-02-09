package net.pterodactylus.sone.freenet

import freenet.client.*
import freenet.keys.*
import freenet.support.io.*
import net.pterodactylus.sone.test.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import kotlin.test.*

class FreenetClientTest {

	private val highLevelSimpleClient = mock<HighLevelSimpleClient>()
	private val freenetClient = DefaultFreenetClient(highLevelSimpleClient)

	@Test
	fun `fetch method calls method on hlsc`() {
		val fetchResult = FetchResult(ClientMetadata(), NullBucket())
		whenever(highLevelSimpleClient.fetch(FreenetURI("KSK@GPL.txt"))).thenReturn(fetchResult)
		assertThat(freenetClient.fetch(FreenetURI("KSK@GPL.txt")), equalTo(fetchResult))
	}

}
