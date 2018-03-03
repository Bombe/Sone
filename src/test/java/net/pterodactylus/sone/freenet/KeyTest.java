package net.pterodactylus.sone.freenet;

import static freenet.support.Base64.encode;
import static net.pterodactylus.sone.freenet.Key.from;
import static net.pterodactylus.sone.freenet.Key.routingKey;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.net.MalformedURLException;

import freenet.keys.FreenetURI;

import org.junit.Test;

/**
 * Unit test for {@link Key}.
 */
public class KeyTest {

	private final FreenetURI uri;
	private final Key key;

	public KeyTest() throws MalformedURLException {
		uri = new FreenetURI(
				"SSK@NfUYvxDwU9vqb2mh-qdT~DYJ6U0XNbxMGGoLe0aCHJs,Miglsgix0VR56ZiPl4NgjnUd~UdrnHqIvXJ3KKHmxmI,AQACAAE/some-site-12/foo/bar.html");
		key = from(uri);
	}

	@Test
	public void keyCanBeCreatedFromFreenetUri() throws MalformedURLException {
		assertThat(key.getRoutingKey(),
				is("NfUYvxDwU9vqb2mh-qdT~DYJ6U0XNbxMGGoLe0aCHJs"));
		assertThat(key.getCryptoKey(),
				is("Miglsgix0VR56ZiPl4NgjnUd~UdrnHqIvXJ3KKHmxmI"));
		assertThat(key.getExtra(), is("AQACAAE"));
	}

	@Test
	public void keyCanBeConvertedToUsk() throws MalformedURLException {
		FreenetURI uskUri = key.toUsk("other-site", 15, "some", "path.html");
		assertThat(uskUri.toString(),
				is("USK@NfUYvxDwU9vqb2mh-qdT~DYJ6U0XNbxMGGoLe0aCHJs,Miglsgix0VR56ZiPl4NgjnUd~UdrnHqIvXJ3KKHmxmI,AQACAAE/other-site/15/some/path.html"));
	}

	@Test
	public void keyCanBeConvertedToSskWithoutEdition()
	throws MalformedURLException {
		FreenetURI uskUri = key.toSsk("other-site", "some", "path.html");
		assertThat(uskUri.toString(),
				is("SSK@NfUYvxDwU9vqb2mh-qdT~DYJ6U0XNbxMGGoLe0aCHJs,Miglsgix0VR56ZiPl4NgjnUd~UdrnHqIvXJ3KKHmxmI,AQACAAE/other-site/some/path.html"));
	}

	@Test
	public void keyCanBeConvertedToSskWithEdition()
	throws MalformedURLException {
		FreenetURI uskUri = key.toSsk("other-site", 15, "some", "path.html");
		assertThat(uskUri.toString(),
				is("SSK@NfUYvxDwU9vqb2mh-qdT~DYJ6U0XNbxMGGoLe0aCHJs,Miglsgix0VR56ZiPl4NgjnUd~UdrnHqIvXJ3KKHmxmI,AQACAAE/other-site-15/some/path.html"));
	}

	@Test
	public void routingKeyIsExtractCorrectly() {
		assertThat(routingKey(uri),
				is("NfUYvxDwU9vqb2mh-qdT~DYJ6U0XNbxMGGoLe0aCHJs"));
	}

}
