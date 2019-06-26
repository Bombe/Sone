package net.pterodactylus.sone.core;

import static freenet.keys.InsertableClientSSK.createRandom;
import static net.pterodactylus.sone.core.SoneUri.create;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import freenet.crypt.DummyRandomSource;
import freenet.keys.FreenetURI;

import org.junit.Test;

/**
 * Unit test for {@link SoneUri}.
 */
public class SoneUriTest {

	@Test
	public void callConstructorForIncreasedTestCoverage() {
		new SoneUri();
	}

	@Test
	public void returnedUriHasCorrectDocNameAndMetaStrings() {
		FreenetURI uri = createRandom(new DummyRandomSource(), "test-0").getURI().uskForSSK();
		assertThat(create(uri.toString()).getDocName(), is("Sone"));
		assertThat(create(uri.toString()).getAllMetaStrings(), is(new String[0]));
	}

	@Test
	public void malformedUriReturnsNull() {
		assertThat(create("not a key"), nullValue());
	}

}
