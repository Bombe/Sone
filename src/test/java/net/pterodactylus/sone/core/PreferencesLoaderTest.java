package net.pterodactylus.sone.core;

import static net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.WRITING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.pterodactylus.sone.TestValue;
import net.pterodactylus.util.config.Configuration;

import com.google.common.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link PreferencesLoader}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PreferencesLoaderTest {

	private final EventBus eventBus = mock(EventBus.class);
	private final Preferences preferences = new Preferences(eventBus);
	private final Configuration configuration = mock(Configuration.class);
	private final PreferencesLoader preferencesLoader =
			new PreferencesLoader(preferences);

	@Before
	public void setupConfiguration() {
		setupIntValue("InsertionDelay", 15);
		setupIntValue("PostsPerPage", 25);
		setupIntValue("ImagesPerPage", 12);
		setupIntValue("CharactersPerPost", 150);
		setupIntValue("PostCutOffLength", 300);
		setupBooleanValue("RequireFullAccess", true);
		setupIntValue("PositiveTrust", 50);
		setupIntValue("NegativeTrust", -50);
		when(configuration.getStringValue("Option/TrustComment")).thenReturn(
				new TestValue<String>("Trusted"));
		setupBooleanValue("ActivateFcpInterface", true);
		setupIntValue("FcpFullAccessRequired", 1);
	}

	private void setupIntValue(String optionName, int value) {
		when(configuration.getIntValue("Option/" + optionName)).thenReturn(
				new TestValue<Integer>(value));
	}

	private void setupBooleanValue(String optionName, boolean value) {
		when(configuration.getBooleanValue(
				"Option/" + optionName)).thenReturn(
				new TestValue<Boolean>(value));
	}

	@Test
	public void configurationIsLoadedCorrectly() {
		setupConfiguration();
		preferencesLoader.loadFrom(configuration);
		assertThat(preferences.getInsertionDelay(), is(15));
		assertThat(preferences.getPostsPerPage(), is(25));
		assertThat(preferences.getImagesPerPage(), is(12));
		assertThat(preferences.getCharactersPerPost(), is(150));
		assertThat(preferences.getPostCutOffLength(), is(300));
		assertThat(preferences.isRequireFullAccess(), is(true));
		assertThat(preferences.getPositiveTrust(), is(50));
		assertThat(preferences.getNegativeTrust(), is(-50));
		assertThat(preferences.getTrustComment(), is("Trusted"));
		assertThat(preferences.isFcpInterfaceActive(), is(true));
		assertThat(preferences.getFcpFullAccessRequired(), is(WRITING));
	}

	@Test
	public void configurationIsLoadedCorrectlyWithCutOffLengthMinusOne() {
	    setupConfiguration();
		setupIntValue("PostCutOffLength", -1);
		preferencesLoader.loadFrom(configuration);
		assertThat(preferences.getPostCutOffLength(), not(is(-1)));
	}

}
