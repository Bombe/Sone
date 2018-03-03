package net.pterodactylus.sone.freenet.wot;

import static com.google.common.base.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import net.pterodactylus.sone.freenet.plugin.PluginException;

import com.google.common.eventbus.EventBus;
import org.junit.Test;

/**
 * Unit test for {@link IdentityManagerImpl}.
 */
public class IdentityManagerTest {

	private final EventBus eventBus = mock(EventBus.class);
	private final WebOfTrustConnector webOfTrustConnector = mock(WebOfTrustConnector.class);
	private final IdentityManager identityManager = new IdentityManagerImpl(eventBus, webOfTrustConnector, new IdentityLoader(webOfTrustConnector, of(new Context("Test"))));

	@Test
	public void identityManagerPingsWotConnector() throws PluginException {
		assertThat(identityManager.isConnected(), is(true));
		verify(webOfTrustConnector).ping();
	}

	@Test
	public void disconnectedWotConnectorIsRecognized() throws PluginException {
		doThrow(PluginException.class).when(webOfTrustConnector).ping();
		assertThat(identityManager.isConnected(), is(false));
		verify(webOfTrustConnector).ping();
	}

}
