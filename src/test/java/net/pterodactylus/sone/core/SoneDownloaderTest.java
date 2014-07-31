package net.pterodactylus.sone.core;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.pterodactylus.sone.core.SoneDownloader.FetchSone;
import net.pterodactylus.sone.core.SoneDownloader.FetchSoneWithUri;
import net.pterodactylus.sone.data.Sone;

import freenet.keys.FreenetURI;

import org.junit.Test;

/**
 * Unit test for {@link SoneDownloader} and its subclasses.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneDownloaderTest {

	@Test
	public void fetchSoneWithUriDownloadsSoneWithUri() {
		SoneDownloader soneDownloader = mock(SoneDownloader.class);
		Sone sone = mock(Sone.class);
		FreenetURI soneUri = mock(FreenetURI.class);
		when(sone.getRequestUri()).thenReturn(soneUri);
		FetchSoneWithUri fetchSoneWithUri = soneDownloader.new FetchSoneWithUri(sone);
		fetchSoneWithUri.run();
		verify(soneDownloader).fetchSone(eq(sone), eq(soneUri));
	}

	@Test
	public void fetchSoneDownloadsSone() {
		SoneDownloader soneDownloader = mock(SoneDownloader.class);
		Sone sone = mock(Sone.class);
		FetchSone fetchSone = soneDownloader.new FetchSone(sone);
		fetchSone.run();
		verify(soneDownloader).fetchSone(eq(sone));
	}

}
