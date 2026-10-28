package net.pterodactylus.sone.freenet.wot

import com.google.common.eventbus.EventBus
import net.pterodactylus.sone.freenet.plugin.PluginException
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for [IdentityManagerImpl].
 */
class IdentityManagerTest {

	private val eventBus = EventBus()

	@Test
	fun identityManagerPingsWotConnector() {
		var pingCalled = false
		val webOfTrustConnector = dummyWebOfTrustConnector.overridePing { Unit.also { pingCalled = true } }
		val identityManager = IdentityManagerImpl(eventBus, webOfTrustConnector, IdentityLoader(webOfTrustConnector, Context("Test")))
		assertThat(identityManager.isConnected, equalTo(true))
		assertThat(pingCalled, equalTo(true))
	}

	@Test
	fun disconnectedWotConnectorIsRecognized() {
		val webOfTrustConnector = dummyWebOfTrustConnector.overridePing { throw PluginException() }
		val identityManager = IdentityManagerImpl(eventBus, webOfTrustConnector, IdentityLoader(webOfTrustConnector, Context("Test")))
		assertThat(identityManager.isConnected, equalTo(false))
	}

}
