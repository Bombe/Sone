package net.pterodactylus.sone.core;

import static java.lang.Thread.sleep;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.pterodactylus.sone.core.WebOfTrustUpdater.AddContextJob;
import net.pterodactylus.sone.core.WebOfTrustUpdater.RemoveContextJob;
import net.pterodactylus.sone.core.WebOfTrustUpdater.SetPropertyJob;
import net.pterodactylus.sone.core.WebOfTrustUpdater.WebOfTrustContextUpdateJob;
import net.pterodactylus.sone.core.WebOfTrustUpdater.WebOfTrustUpdateJob;
import net.pterodactylus.sone.freenet.plugin.PluginException;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;
import net.pterodactylus.sone.freenet.wot.WebOfTrustConnector;

import org.junit.Test;

/**
 * Unit test for {@link WebOfTrustUpdater} and its subclasses.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class WebOfTrustUpdaterTest {

	private static final String CONTEXT = "test-context";
	private final WebOfTrustConnector webOfTrustConnector = mock(WebOfTrustConnector.class);
	private final WebOfTrustUpdater webOfTrustUpdater = new WebOfTrustUpdater(webOfTrustConnector);
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

}
