package net.pterodactylus.sone.core;

import static net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.ALWAYS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.pterodactylus.sone.core.Options.Option;
import net.pterodactylus.sone.core.event.InsertionDelayChangedEvent;
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired;

import com.google.common.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link Preferences}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PreferencesTest {

	private static final int INTEGER_VALUE = 1;
	private static final String STRING_VALUE = "string-value";
	private final Options options = mock(Options.class);
	private final EventBus eventBus = mock(EventBus.class);
	private final Preferences preferences = new Preferences(eventBus, options);
	private final Option<Integer> integerOption = when(mock(Option.class).get()).thenReturn(INTEGER_VALUE).getMock();
	private final Option<Boolean> booleanOption = when(mock(Option.class).get()).thenReturn(true).getMock();
	private final Option<String> stringOption = when(mock(Option.class).get()).thenReturn(STRING_VALUE).getMock();

	@Before
	public void setupOptions() {
		when(integerOption.validate(INTEGER_VALUE)).thenReturn(true);
		when(options.getIntegerOption("InsertionDelay")).thenReturn(integerOption);
		when(options.getIntegerOption("PostsPerPage")).thenReturn(integerOption);
		when(options.getIntegerOption("ImagesPerPage")).thenReturn(integerOption);
		when(options.getIntegerOption("CharactersPerPost")).thenReturn(integerOption);
		when(options.getIntegerOption("PostCutOffLength")).thenReturn(integerOption);
		when(options.getBooleanOption("RequireFullAccess")).thenReturn(booleanOption);
		when(options.getIntegerOption("PositiveTrust")).thenReturn(integerOption);
		when(options.getIntegerOption("NegativeTrust")).thenReturn(integerOption);
		when(options.getStringOption("TrustComment")).thenReturn(stringOption);
		when(options.getBooleanOption("ActivateFcpInterface")).thenReturn(booleanOption);
		when(options.getIntegerOption("FcpFullAccessRequired")).thenReturn(integerOption);
	}

	@Test
	public void testGettingInsertionDelay() {
		assertThat(preferences.getInsertionDelay(), is(INTEGER_VALUE));
		verify(integerOption).get();
	}

	@Test
	public void validationOfInsertionDelayIsForwardedToOptions() {
		preferences.validateInsertionDelay(INTEGER_VALUE);
		verify(integerOption).validate(INTEGER_VALUE);
	}

	@Test
	public void settingInsertionDelayIsForwardedToOptions() {
		assertThat(preferences.setInsertionDelay(INTEGER_VALUE), instanceOf(Preferences.class));
		verify(integerOption).set(INTEGER_VALUE);
	}

	@Test
	public void settingInsertionDelayIsForwardedToEventBus() {
		assertThat(preferences.setInsertionDelay(INTEGER_VALUE), instanceOf(Preferences.class));
		verify(eventBus).post(any(InsertionDelayChangedEvent.class));
	}

	@Test
	public void testGettingPostsPerPage() {
		assertThat(preferences.getPostsPerPage(), is(INTEGER_VALUE));
		verify(integerOption).get();
	}

	@Test
	public void validationOfPostsPerPageIsForwardedToOptions() {
		preferences.validatePostsPerPage(INTEGER_VALUE);
		verify(integerOption).validate(INTEGER_VALUE);
	}

	@Test
	public void settingPostsPerPageIsForwardedToOptions() {
		assertThat(preferences.setPostsPerPage(INTEGER_VALUE), instanceOf(Preferences.class));
		verify(integerOption).set(INTEGER_VALUE);
	}

	@Test
	public void testGettingImagesPerPage() {
		assertThat(preferences.getImagesPerPage(), is(INTEGER_VALUE));
		verify(integerOption).get();
	}

	@Test
	public void validationOfImagesPerPageIsForwardedToOptions() {
		preferences.validateImagesPerPage(INTEGER_VALUE);
		verify(integerOption).validate(INTEGER_VALUE);
	}

	@Test
	public void settingImagesPerPageIsForwardedToOptions() {
		assertThat(preferences.setImagesPerPage(INTEGER_VALUE), instanceOf(Preferences.class));
		verify(integerOption).set(INTEGER_VALUE);
	}

	@Test
	public void testGettingCharactersPerPost() {
		assertThat(preferences.getCharactersPerPost(), is(INTEGER_VALUE));
		verify(integerOption).get();
	}

	@Test
	public void validationOfCharactersPerPostIsForwardedToOptions() {
		preferences.validateCharactersPerPost(INTEGER_VALUE);
		verify(integerOption).validate(INTEGER_VALUE);
	}

	@Test
	public void settingCharactersPerPostIsForwardedToOptions() {
		assertThat(preferences.setCharactersPerPost(INTEGER_VALUE), instanceOf(Preferences.class));
		verify(integerOption).set(INTEGER_VALUE);
	}

	@Test
	public void testGettingPostCutOffLength() {
		assertThat(preferences.getPostCutOffLength(), is(INTEGER_VALUE));
		verify(integerOption).get();
	}

	@Test
	public void validationOfPostCutOffLengthIsForwardedToOptions() {
		preferences.validatePostCutOffLength(INTEGER_VALUE);
		verify(integerOption).validate(INTEGER_VALUE);
	}

	@Test
	public void settingPostCutOffLengthIsForwardedToOptions() {
		assertThat(preferences.setPostCutOffLength(INTEGER_VALUE), instanceOf(Preferences.class));
		verify(integerOption).set(INTEGER_VALUE);
	}

	@Test
	public void testGettingRequireFullAccess() {
		assertThat(preferences.isRequireFullAccess(), is(true));
		verify(booleanOption).get();
	}

	@Test
	public void settingRequireFullAccessIsForwardedToOption() {
		preferences.setRequireFullAccess(true);
		verify(booleanOption).set(true);
	}

	@Test
	public void testGettingPositiveTrust() {
		assertThat(preferences.getPositiveTrust(), is(INTEGER_VALUE));
		verify(integerOption).get();
	}

	@Test
	public void validationOfPositiveTrustIsForwardedToOptions() {
		preferences.validatePositiveTrust(INTEGER_VALUE);
		verify(integerOption).validate(INTEGER_VALUE);
	}

	@Test
	public void settingPositiveTrustIsForwardedToOptions() {
		assertThat(preferences.setPositiveTrust(INTEGER_VALUE), instanceOf(Preferences.class));
		verify(integerOption).set(INTEGER_VALUE);
	}

	@Test
	public void testGettingNegativeTrust() {
		assertThat(preferences.getNegativeTrust(), is(INTEGER_VALUE));
		verify(integerOption).get();
	}

	@Test
	public void validationOfNegativeTrustIsForwardedToOptions() {
		preferences.validateNegativeTrust(INTEGER_VALUE);
		verify(integerOption).validate(INTEGER_VALUE);
	}

	@Test
	public void settingNegativeTrustIsForwardedToOptions() {
		assertThat(preferences.setNegativeTrust(INTEGER_VALUE), instanceOf(Preferences.class));
		verify(integerOption).set(INTEGER_VALUE);
	}

	@Test
	public void gettingTrustCommentIsForwardedToOption() {
		assertThat(preferences.getTrustComment(), is(STRING_VALUE));
		verify(stringOption).get();
	}

	@Test
	public void settingTrustCommentIsForwardedToOption() {
		preferences.setTrustComment(STRING_VALUE);
		verify(stringOption).set(STRING_VALUE);
	}

	@Test
	public void gettingFcpInterfaceActiveIsForwardedToOption() {
		assertThat(preferences.isFcpInterfaceActive(), is(true));
		verify(booleanOption).get();
	}

	@Test
	public void settingFcpInterfaceActiveIsForwardedToOption() {
		preferences.setFcpInterfaceActive(true);
		verify(booleanOption).set(true);
	}

	@Test
	public void gettingFcpFullAccessRequired() {
		assertThat(preferences.getFcpFullAccessRequired(), is(FullAccessRequired.values()[INTEGER_VALUE]));
		verify(integerOption).get();
	}

	@Test
	public void settingFcpFullAccessRequiredIsForwardedToOption() {
		preferences.setFcpFullAccessRequired(ALWAYS);
		verify(integerOption).set(ALWAYS.ordinal());
	}

	@Test
	public void settingFcpFullAccessRequiredToNullIsForwardedToOption() {
		preferences.setFcpFullAccessRequired(null);
		verify(integerOption).set(null);
	}

}
