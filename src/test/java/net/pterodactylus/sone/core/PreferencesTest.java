package net.pterodactylus.sone.core;

import static net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.ALWAYS;
import static net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.NO;
import static net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.WRITING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import net.pterodactylus.sone.core.event.InsertionDelayChangedEvent;
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired;
import net.pterodactylus.sone.fcp.event.FcpInterfaceActivatedEvent;
import net.pterodactylus.sone.fcp.event.FcpInterfaceDeactivatedEvent;
import net.pterodactylus.sone.fcp.event.FullAccessRequiredChanged;

import com.google.common.eventbus.EventBus;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * Unit test for {@link Preferences}.
 */
public class PreferencesTest {

	private final EventBus eventBus = mock(EventBus.class);
	private final Preferences preferences = new Preferences(eventBus);

	@Test
	public void preferencesRetainInsertionDelay() {
		preferences.setInsertionDelay(15);
		assertThat(preferences.getInsertionDelay(), is(15));
	}

	@Test
	public void preferencesSendsEventOnSettingInsertionDelay() {
		preferences.setInsertionDelay(15);
		ArgumentCaptor<Object> eventsCaptor = forClass(Object.class);
		verify(eventBus, atLeastOnce()).post(eventsCaptor.capture());
		assertThat(eventsCaptor.getAllValues(), hasItem(new InsertionDelayChangedEvent(15)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidInsertionDelayIsRejected() {
		preferences.setInsertionDelay(-15);
	}

	@Test
	public void noEventIsSentWhenInvalidInsertionDelayIsSet() {
		try {
			preferences.setInsertionDelay(-15);
		} catch (IllegalArgumentException iae) {
			/* ignore. */
		}
		verify(eventBus, never()).post(any());
	}

	@Test
	public void preferencesReturnDefaultValueWhenInsertionDelayIsSetToNull() {
		preferences.setInsertionDelay(null);
		assertThat(preferences.getInsertionDelay(), is(60));
	}

	@Test
	public void preferencesStartWithInsertionDelayDefaultValue() {
		assertThat(preferences.getInsertionDelay(), is(60));
	}

	@Test
	public void preferencesRetainPostsPerPage() {
		preferences.setPostsPerPage(15);
		assertThat(preferences.getPostsPerPage(), is(15));
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidPostsPerPageIsRejected() {
		preferences.setPostsPerPage(-15);
	}

	@Test
	public void preferencesReturnDefaultValueWhenPostsPerPageIsSetToNull() {
		preferences.setPostsPerPage(null);
		assertThat(preferences.getPostsPerPage(), is(10));
	}

	@Test
	public void preferencesStartWithPostsPerPageDefaultValue() {
		assertThat(preferences.getPostsPerPage(), is(10));
	}

	@Test
	public void preferencesRetainImagesPerPage() {
		preferences.setImagesPerPage(15);
		assertThat(preferences.getImagesPerPage(), is(15));
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidImagesPerPageIsRejected() {
		preferences.setImagesPerPage(-15);
	}

	@Test
	public void preferencesReturnDefaultValueWhenImagesPerPageIsSetToNull() {
		preferences.setImagesPerPage(null);
		assertThat(preferences.getImagesPerPage(), is(9));
	}

	@Test
	public void preferencesStartWithImagesPerPageDefaultValue() {
		assertThat(preferences.getImagesPerPage(), is(9));
	}

	@Test
	public void preferencesRetainCharactersPerPost() {
		preferences.setCharactersPerPost(150);
		assertThat(preferences.getCharactersPerPost(), is(150));
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidCharactersPerPostIsRejected() {
		preferences.setCharactersPerPost(-15);
	}

	@Test
	public void preferencesReturnDefaultValueWhenCharactersPerPostIsSetToNull() {
		preferences.setCharactersPerPost(null);
		assertThat(preferences.getCharactersPerPost(), is(400));
	}

	@Test
	public void preferencesStartWithCharactersPerPostDefaultValue() {
		assertThat(preferences.getCharactersPerPost(), is(400));
	}

	@Test
	public void preferencesRetainPostCutOffLength() {
		preferences.setPostCutOffLength(150);
		assertThat(preferences.getPostCutOffLength(), is(150));
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidPostCutOffLengthIsRejected() {
		preferences.setPostCutOffLength(-15);
	}

	@Test(expected = IllegalArgumentException.class)
	public void cutOffLengthOfMinusOneIsNotAllowed() {
		preferences.setPostCutOffLength(-1);
	}

	@Test
	public void preferencesReturnDefaultValueWhenPostCutOffLengthIsSetToNull() {
		preferences.setPostCutOffLength(null);
		assertThat(preferences.getPostCutOffLength(), is(200));
	}

	@Test
	public void preferencesStartWithPostCutOffLengthDefaultValue() {
		assertThat(preferences.getPostCutOffLength(), is(200));
	}

	@Test
	public void preferencesRetainRequireFullAccessOfTrue() {
		preferences.setRequireFullAccess(true);
		assertThat(preferences.isRequireFullAccess(), is(true));
	}

	@Test
	public void preferencesRetainRequireFullAccessOfFalse() {
		preferences.setRequireFullAccess(false);
		assertThat(preferences.isRequireFullAccess(), is(false));
	}

	@Test
	public void preferencesReturnDefaultValueWhenRequireFullAccessIsSetToNull() {
		preferences.setRequireFullAccess(null);
		assertThat(preferences.isRequireFullAccess(), is(false));
	}

	@Test
	public void preferencesStartWithRequireFullAccessDefaultValue() {
		assertThat(preferences.isRequireFullAccess(), is(false));
	}

	@Test
	public void preferencesRetainPositiveTrust() {
		preferences.setPositiveTrust(15);
		assertThat(preferences.getPositiveTrust(), is(15));
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidPositiveTrustIsRejected() {
		preferences.setPositiveTrust(-15);
	}

	@Test
	public void preferencesReturnDefaultValueWhenPositiveTrustIsSetToNull() {
		preferences.setPositiveTrust(null);
		assertThat(preferences.getPositiveTrust(), is(75));
	}

	@Test
	public void preferencesStartWithPositiveTrustDefaultValue() {
		assertThat(preferences.getPositiveTrust(), is(75));
	}

	@Test
	public void preferencesRetainNegativeTrust() {
		preferences.setNegativeTrust(-15);
		assertThat(preferences.getNegativeTrust(), is(-15));
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidNegativeTrustIsRejected() {
		preferences.setNegativeTrust(150);
	}

	@Test
	public void preferencesReturnDefaultValueWhenNegativeTrustIsSetToNull() {
		preferences.setNegativeTrust(null);
		assertThat(preferences.getNegativeTrust(), is(-25));
	}

	@Test
	public void preferencesStartWithNegativeTrustDefaultValue() {
		assertThat(preferences.getNegativeTrust(), is(-25));
	}

	@Test
	public void preferencesRetainTrustComment() {
		preferences.setTrustComment("Trust");
		assertThat(preferences.getTrustComment(), is("Trust"));
	}

	@Test
	public void preferencesReturnDefaultValueWhenTrustCommentIsSetToNull() {
		preferences.setTrustComment(null);
		assertThat(preferences.getTrustComment(),
				is("Set from Sone Web Interface"));
	}

	@Test
	public void preferencesStartWithTrustCommentDefaultValue() {
		assertThat(preferences.getTrustComment(),
				is("Set from Sone Web Interface"));
	}

	@Test
	public void preferencesRetainFcpInterfaceActiveOfTrue() {
		preferences.setFcpInterfaceActive(true);
		assertThat(preferences.isFcpInterfaceActive(), is(true));
		verify(eventBus).post(any(FcpInterfaceActivatedEvent.class));
	}

	@Test
	public void preferencesRetainFcpInterfaceActiveOfFalse() {
		preferences.setFcpInterfaceActive(false);
		assertThat(preferences.isFcpInterfaceActive(), is(false));
		verify(eventBus).post(any(FcpInterfaceDeactivatedEvent.class));
	}

	@Test
	public void preferencesReturnDefaultValueWhenFcpInterfaceActiveIsSetToNull() {
		preferences.setFcpInterfaceActive(null);
		assertThat(preferences.isFcpInterfaceActive(), is(false));
		verify(eventBus).post(any(FcpInterfaceDeactivatedEvent.class));
	}

	@Test
	public void preferencesStartWithFcpInterfaceActiveDefaultValue() {
		assertThat(preferences.isFcpInterfaceActive(), is(false));
	}

	@Test
	public void preferencesRetainFcpFullAccessRequiredOfNo() {
		preferences.setFcpFullAccessRequired(NO);
		assertThat(preferences.getFcpFullAccessRequired(), is(NO));
		verifyFullAccessRequiredChangedEvent(NO);
	}

	private void verifyFullAccessRequiredChangedEvent(
			FullAccessRequired fullAccessRequired) {
		ArgumentCaptor<FullAccessRequiredChanged> fullAccessRequiredCaptor =
				forClass(FullAccessRequiredChanged.class);
		verify(eventBus).post(fullAccessRequiredCaptor.capture());
		assertThat(
				fullAccessRequiredCaptor.getValue().getFullAccessRequired(),
				is(fullAccessRequired));
	}

	@Test
	public void preferencesRetainFcpFullAccessRequiredOfWriting() {
		preferences.setFcpFullAccessRequired(WRITING);
		assertThat(preferences.getFcpFullAccessRequired(), is(WRITING));
		verifyFullAccessRequiredChangedEvent(WRITING);
	}

	@Test
	public void preferencesRetainFcpFullAccessRequiredOfAlways() {
		preferences.setFcpFullAccessRequired(ALWAYS);
		assertThat(preferences.getFcpFullAccessRequired(), is(ALWAYS));
		verifyFullAccessRequiredChangedEvent(ALWAYS);
	}

	@Test
	public void preferencesReturnDefaultValueWhenFcpFullAccessRequiredIsSetToNull() {
		preferences.setFcpFullAccessRequired(null);
		assertThat(preferences.getFcpFullAccessRequired(), is(ALWAYS));
		verifyFullAccessRequiredChangedEvent(ALWAYS);
	}

	@Test
	public void preferencesStartWithFcpFullAccessRequiredDefaultValue() {
		assertThat(preferences.getFcpFullAccessRequired(), is(ALWAYS));
	}

	@Test
	public void settingInsertionDelayToValidValueSendsChangeEvent() {
		preferences.setInsertionDelay(30);
		ArgumentCaptor<Object> eventsCaptor = forClass(Object.class);
		verify(eventBus, atLeastOnce()).post(eventsCaptor.capture());
		assertThat(eventsCaptor.getAllValues(), hasItem(new PreferenceChangedEvent("InsertionDelay", 30)));
	}

}
