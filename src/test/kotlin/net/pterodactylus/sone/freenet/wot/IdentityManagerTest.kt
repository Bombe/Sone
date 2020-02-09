package net.pterodactylus.sone.freenet.wot

import com.google.common.eventbus.*
import net.pterodactylus.sone.freenet.plugin.*
import net.pterodactylus.sone.test.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import org.junit.*
import org.mockito.Mockito.*

/**
 * Unit test for [IdentityManagerImpl].
 */
class IdentityManagerTest {

	private val eventBus = mock<EventBus>()
	private val webOfTrustConnector = mock<WebOfTrustConnector>()
	private val identityManager = IdentityManagerImpl(eventBus, webOfTrustConnector, IdentityLoader(webOfTrustConnector, Context("Test")))

	@Test
	fun identityManagerPingsWotConnector() {
		assertThat(identityManager.isConnected, equalTo(true))
		verify(webOfTrustConnector).ping()
	}

	@Test
	fun disconnectedWotConnectorIsRecognized() {
		doThrow(PluginException::class.java).whenever(webOfTrustConnector).ping()
		assertThat(identityManager.isConnected, equalTo(false))
		verify(webOfTrustConnector).ping()
	}

}
