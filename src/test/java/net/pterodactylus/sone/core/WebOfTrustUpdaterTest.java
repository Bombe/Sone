package net.pterodactylus.sone.core;

import static java.lang.Thread.sleep;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.concurrent.CountDownLatch;

import net.pterodactylus.sone.core.WebOfTrustUpdaterImpl.AddContextJob;
import net.pterodactylus.sone.core.WebOfTrustUpdaterImpl.RemoveContextJob;
import net.pterodactylus.sone.core.WebOfTrustUpdaterImpl.SetPropertyJob;
import net.pterodactylus.sone.core.WebOfTrustUpdaterImpl.WebOfTrustContextUpdateJob;
import net.pterodactylus.sone.core.WebOfTrustUpdaterImpl.WebOfTrustUpdateJob;
import net.pterodactylus.sone.freenet.plugin.PluginException;
import net.pterodactylus.sone.freenet.wot.Identity;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;
import net.pterodactylus.sone.freenet.wot.WebOfTrustConnector;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Unit test for {@link WebOfTrustUpdaterImpl} and its subclasses.
 */
public class WebOfTrustUpdaterTest {

	private static final String CONTEXT = "test-context";
	private static final String PROPERTY_NAME = "test-property";
	private final WebOfTrustConnector webOfTrustConnector = mock(WebOfTrustConnector.class);
	private final WebOfTrustUpdaterImpl webOfTrustUpdater = new WebOfTrustUpdaterImpl(webOfTrustConnector);
	private final OwnIdentity ownIdentity = when(mock(OwnIdentity.class).getId()).thenReturn("own-identity-id").getMock();
	private final WebOfTrustUpdateJob successfulWebOfTrustUpdateJob = createWebOfTrustUpdateJob(true);
	private final WebOfTrustUpdateJob failingWebOfTrustUpdateJob = createWebOfTrustUpdateJob(false);
	private final WebOfTrustContextUpdateJob contextUpdateJob = webOfTrustUpdater.new WebOfTrustContextUpdateJob(ownIdentity, CONTEXT);
	private final AddContextJob addContextJob = webOfTrustUpdater.new AddContextJob(ownIdentity, CONTEXT);
	private final RemoveContextJob removeContextJob = webOfTrustUpdater.new RemoveContextJob(ownIdentity, CONTEXT);

	private WebOfTrustUpdateJob createWebOfTrustUpdateJob(final boolean success) {
		return webOfTrustUpdater.new WebOfTrustUpdateJob() {
			@Override
			public void run() {
				super.run();
				try {
					sleep(100);
				} catch (InterruptedException ie1) {
					throw new RuntimeException(ie1);
				}
				finish(success);
			}
		};
	}

	@Test
	public void webOfTrustUpdateJobWaitsUntilFinishedHasBeenCalledAndReturnsSuccess() throws InterruptedException {
		new Thread(successfulWebOfTrustUpdateJob).start();
		assertThat(successfulWebOfTrustUpdateJob.waitForCompletion(), is(true));
	}

	@Test
	public void webOfTrustUpdateJobWaitsUntilFinishedHasBeenCalledAndReturnsFailure() throws InterruptedException {
		new Thread(failingWebOfTrustUpdateJob).start();
		assertThat(failingWebOfTrustUpdateJob.waitForCompletion(), is(false));
	}

	@Test
	public void webOfTrustContextUpdateJobsAreEqualIfTheirClassOwnIdentityAndContextAreEqual() {
		WebOfTrustContextUpdateJob secondContextUpdateJob = webOfTrustUpdater.new WebOfTrustContextUpdateJob(ownIdentity, CONTEXT);
		assertThat(contextUpdateJob.equals(secondContextUpdateJob), is(true));
		assertThat(secondContextUpdateJob.equals(contextUpdateJob), is(true));
		assertThat(contextUpdateJob.hashCode(), is(secondContextUpdateJob.hashCode()));
	}

	@Test
	public void webOfTrustContextUpdatesJobsAreNotEqualIfTheirClassDiffers() {
		assertThat(contextUpdateJob.equals(addContextJob), is(false));
	}

	@Test
	public void webOfTrustContextUpdateJobToStringContainsIdentityAndContext() {
		assertThat(contextUpdateJob.toString(), containsString(ownIdentity.toString()));
		assertThat(contextUpdateJob.toString(), containsString(CONTEXT));
	}

	@Test
	public void webOfTrustContextUpdateJobsAreNotEqualIfTheIdentitiesDiffer() {
		OwnIdentity ownIdentity = mock(OwnIdentity.class);
		WebOfTrustContextUpdateJob secondContextUpdateJob = webOfTrustUpdater.new WebOfTrustContextUpdateJob(ownIdentity, CONTEXT);
		assertThat(contextUpdateJob.equals(secondContextUpdateJob), is(false));
		assertThat(secondContextUpdateJob.equals(contextUpdateJob), is(false));
	}

	@Test
	public void webOfTrustContextUpdateJobsAreNotEqualIfTheirContextsDiffer() {
		WebOfTrustContextUpdateJob secondContextUpdateJob = webOfTrustUpdater.new WebOfTrustContextUpdateJob(ownIdentity, CONTEXT + CONTEXT);
		assertThat(contextUpdateJob.equals(secondContextUpdateJob), is(false));
		assertThat(secondContextUpdateJob.equals(contextUpdateJob), is(false));
	}

	@Test
	public void webOfTrustContextUpdateJobsAreNotEqualToNull() {
		assertThat(contextUpdateJob.equals(null), is(false));
	}

	@Test
	public void addContextJobAddsTheContext() throws PluginException {
		addContextJob.run();
		verify(webOfTrustConnector).addContext(eq(ownIdentity), eq(CONTEXT));
		verify(ownIdentity).addContext(eq(CONTEXT));
		assertThat(addContextJob.waitForCompletion(), is(true));
	}

	@Test
	public void exceptionWhileAddingAContextIsExposed() throws PluginException {
		doThrow(PluginException.class).when(webOfTrustConnector).addContext(eq(ownIdentity), eq(CONTEXT));
		addContextJob.run();
		verify(webOfTrustConnector).addContext(eq(ownIdentity), eq(CONTEXT));
		verify(ownIdentity, never()).addContext(eq(CONTEXT));
		assertThat(addContextJob.waitForCompletion(), is(false));
	}

	@Test
	public void removeContextJobRemovesTheContext() throws PluginException {
		removeContextJob.run();
		verify(webOfTrustConnector).removeContext(eq(ownIdentity), eq(CONTEXT));
		verify(ownIdentity).removeContext(eq(CONTEXT));
		assertThat(removeContextJob.waitForCompletion(), is(true));
	}

	@Test
	public void exceptionWhileRemovingAContextIsExposed() throws PluginException {
		doThrow(PluginException.class).when(webOfTrustConnector).removeContext(eq(ownIdentity), eq(CONTEXT));
		removeContextJob.run();
		verify(webOfTrustConnector).removeContext(eq(ownIdentity), eq(CONTEXT));
		verify(ownIdentity, never()).removeContext(eq(CONTEXT));
		assertThat(removeContextJob.waitForCompletion(), is(false));
	}

	@Test
	public void settingAPropertySetsTheProperty() throws PluginException {
		String propertyName = "property-name";
		String propertyValue = "property-value";
		SetPropertyJob setPropertyJob = webOfTrustUpdater.new SetPropertyJob(ownIdentity, propertyName, propertyValue);
		setPropertyJob.run();
		verify(webOfTrustConnector).setProperty(eq(ownIdentity), eq(propertyName), eq(propertyValue));
		verify(ownIdentity).setProperty(eq(propertyName), eq(propertyValue));
		assertThat(setPropertyJob.waitForCompletion(), is(true));
	}

	@Test
	public void settingAPropertyToNullRemovesTheProperty() throws PluginException {
		String propertyName = "property-name";
		SetPropertyJob setPropertyJob = webOfTrustUpdater.new SetPropertyJob(ownIdentity, propertyName, null);
		setPropertyJob.run();
		verify(webOfTrustConnector).removeProperty(eq(ownIdentity), eq(propertyName));
		verify(ownIdentity).removeProperty(eq(propertyName));
		assertThat(setPropertyJob.waitForCompletion(), is(true));
	}

	@Test
	public void pluginExceptionWhileSettingAPropertyIsHandled() throws PluginException {
		String propertyName = "property-name";
		String propertyValue = "property-value";
		doThrow(PluginException.class).when(webOfTrustConnector).setProperty(eq(ownIdentity), eq(propertyName), eq(propertyValue));
		SetPropertyJob setPropertyJob = webOfTrustUpdater.new SetPropertyJob(ownIdentity, propertyName, propertyValue);
		setPropertyJob.run();
		verify(webOfTrustConnector).setProperty(eq(ownIdentity), eq(propertyName), eq(propertyValue));
		verify(ownIdentity, never()).setProperty(eq(propertyName), eq(propertyValue));
		assertThat(setPropertyJob.waitForCompletion(), is(false));
	}

	@Test
	public void setPropertyJobsWithSameClassPropertyAndValueAreEqual() {
		String propertyName = "property-name";
		String propertyValue = "property-value";
		SetPropertyJob firstSetPropertyJob = webOfTrustUpdater.new SetPropertyJob(ownIdentity, propertyName, propertyValue);
		SetPropertyJob secondSetPropertyJob = webOfTrustUpdater.new SetPropertyJob(ownIdentity, propertyName, propertyValue);
		assertThat(firstSetPropertyJob, is(secondSetPropertyJob));
		assertThat(secondSetPropertyJob, is(firstSetPropertyJob));
		assertThat(firstSetPropertyJob.hashCode(), is(secondSetPropertyJob.hashCode()));
	}

	@Test
	public void setPropertyJobsWithDifferentClassesAreNotEqual() {
		String propertyName = "property-name";
		String propertyValue = "property-value";
		SetPropertyJob firstSetPropertyJob = webOfTrustUpdater.new SetPropertyJob(ownIdentity, propertyName, propertyValue);
		SetPropertyJob secondSetPropertyJob = webOfTrustUpdater.new SetPropertyJob(ownIdentity, propertyName, propertyValue) {
		};
		assertThat(firstSetPropertyJob, not(is(secondSetPropertyJob)));
	}

	@Test
	public void nullIsNotASetProjectJobEither() {
		String propertyName = "property-name";
		String propertyValue = "property-value";
		SetPropertyJob setPropertyJob = webOfTrustUpdater.new SetPropertyJob(ownIdentity, propertyName, propertyValue);
		assertThat(setPropertyJob, not(is((Object) null)));
	}

	@Test
	public void setPropertyJobsWithDifferentPropertiesAreNotEqual() {
		String propertyName = "property-name";
		String propertyValue = "property-value";
		SetPropertyJob firstSetPropertyJob = webOfTrustUpdater.new SetPropertyJob(ownIdentity, propertyName, propertyValue);
		SetPropertyJob secondSetPropertyJob = webOfTrustUpdater.new SetPropertyJob(ownIdentity, propertyName + "2", propertyValue);
		assertThat(firstSetPropertyJob, not(is(secondSetPropertyJob)));
	}

	@Test
	public void setPropertyJobsWithDifferentOwnIdentitiesAreNotEqual() {
		OwnIdentity otherOwnIdentity = mock(OwnIdentity.class);
		String propertyName = "property-name";
		String propertyValue = "property-value";
		SetPropertyJob firstSetPropertyJob = webOfTrustUpdater.new SetPropertyJob(ownIdentity, propertyName, propertyValue);
		SetPropertyJob secondSetPropertyJob = webOfTrustUpdater.new SetPropertyJob(otherOwnIdentity, propertyName, propertyValue);
		assertThat(firstSetPropertyJob, not(is(secondSetPropertyJob)));
	}

	@Test
	public void webOfTrustUpdaterStopsWhenItShould() {
		webOfTrustUpdater.stop();
		webOfTrustUpdater.serviceRun();
	}

	@Test
	public void webOfTrustUpdaterStopsAfterItWasStarted() {
		webOfTrustUpdater.start();
		webOfTrustUpdater.stop();
	}

	@Test
	public void removePropertyRemovesProperty() throws InterruptedException, PluginException {
		final CountDownLatch wotCallTriggered = new CountDownLatch(1);
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				wotCallTriggered.countDown();
				return null;
			}
		}).when(webOfTrustConnector).removeProperty(eq(ownIdentity), eq(PROPERTY_NAME));
		webOfTrustUpdater.removeProperty(ownIdentity, PROPERTY_NAME);
		webOfTrustUpdater.start();
		assertThat(wotCallTriggered.await(1, SECONDS), is(true));
	}

	@Test
	public void multipleCallsToSetPropertyAreCollapsed() throws InterruptedException, PluginException {
		final CountDownLatch wotCallTriggered = new CountDownLatch(1);
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				wotCallTriggered.countDown();
				return null;
			}
		}).when(webOfTrustConnector).removeProperty(eq(ownIdentity), eq(PROPERTY_NAME));
		webOfTrustUpdater.removeProperty(ownIdentity, PROPERTY_NAME);
		webOfTrustUpdater.removeProperty(ownIdentity, PROPERTY_NAME);
		webOfTrustUpdater.start();
		assertThat(wotCallTriggered.await(1, SECONDS), is(true));
		verify(webOfTrustConnector).removeProperty(eq(ownIdentity), eq(PROPERTY_NAME));
	}

	@Test
	public void addContextWaitWaitsForTheContextToBeAdded() {
		webOfTrustUpdater.start();
		assertThat(webOfTrustUpdater.addContextWait(ownIdentity, CONTEXT), is(true));
		verify(ownIdentity).addContext(eq(CONTEXT));
	}

	@Test
	public void removeContextRemovesAContext() throws InterruptedException, PluginException {
		webOfTrustUpdater.start();
		final CountDownLatch removeContextTrigger = new CountDownLatch(1);
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				removeContextTrigger.countDown();
				return null;
			}
		}).when(ownIdentity).removeContext(eq(CONTEXT));
		webOfTrustUpdater.removeContext(ownIdentity, CONTEXT);
		removeContextTrigger.await(1, SECONDS);
		verify(webOfTrustConnector).removeContext(eq(ownIdentity), eq(CONTEXT));
		verify(ownIdentity).removeContext(eq(CONTEXT));
	}

	@Test
	public void removeContextRequestsAreCoalesced() throws InterruptedException, PluginException {
		final CountDownLatch contextRemovedTrigger = new CountDownLatch(1);
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				contextRemovedTrigger.countDown();
				return null;
			}
		}).when(ownIdentity).removeContext(eq(CONTEXT));
		for (int i = 1; i <= 2; i++) {
			/* this is so fucking volatile. */
			if (i > 1) {
				sleep(200);
			}
			new Thread(new Runnable() {
				public void run() {
					webOfTrustUpdater.removeContext(ownIdentity, CONTEXT);
				}
			}).start();
		}
		webOfTrustUpdater.start();
		assertThat(contextRemovedTrigger.await(1, SECONDS), is(true));
		verify(webOfTrustConnector).removeContext(eq(ownIdentity), eq(CONTEXT));
		verify(ownIdentity).removeContext(eq(CONTEXT));
	}

}
