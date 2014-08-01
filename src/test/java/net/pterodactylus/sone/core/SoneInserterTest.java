package net.pterodactylus.sone.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import net.pterodactylus.sone.core.SoneInserter.InsertInformation;
import net.pterodactylus.sone.core.SoneInserter.SetInsertionDelay;
import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.Sone;

import freenet.keys.FreenetURI;

import com.google.common.eventbus.EventBus;
import org.junit.Test;

/**
 * Unit test for {@link SoneInserter} and its subclasses.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneInserterTest {

	@Test
	public void insertionDelayIsForwardedToSoneInserter() {
		SetInsertionDelay setInsertionDelay = new SetInsertionDelay();
		setInsertionDelay.optionChanged(null, null, 15);
		assertThat(SoneInserter.getInsertionDelay().get(), is(15));
	}

	@Test
	/* this test is hilariously bad. */
	public void manifestEntriesAreCreated() {
		FreenetURI insertUri = mock(FreenetURI.class);
		String fingerprint = "fingerprint";
		Sone sone = createSone(insertUri, fingerprint);
		Core core = mock(Core.class);
		UpdateChecker updateChecker = mock(UpdateChecker.class);
		when(core.getUpdateChecker()).thenReturn(updateChecker);
		EventBus eventBus = mock(EventBus.class);
		FreenetInterface freenetInterface = mock(FreenetInterface.class);
		SoneInserter soneInserter = new SoneInserter(core, eventBus, freenetInterface, sone);
		InsertInformation insertInformation = soneInserter.new InsertInformation(sone);
		HashMap<String, Object> manifestEntries = insertInformation.generateManifestEntries();
		assertThat(manifestEntries.keySet(), containsInAnyOrder("index.html", "sone.xml"));
		assertThat(insertInformation.getInsertUri(), is(insertUri));
		assertThat(insertInformation.getFingerprint(), is(fingerprint));
	}

	private Sone createSone(FreenetURI insertUri, String fingerprint) {
		Sone sone = mock(Sone.class);
		when(sone.getInsertUri()).thenReturn(insertUri);
		when(sone.getFingerprint()).thenReturn(fingerprint);
		when(sone.getRootAlbum()).thenReturn(mock(Album.class));
		return sone;
	}

}
