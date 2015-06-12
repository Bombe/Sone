package net.pterodactylus.sone.data;

import net.pterodactylus.sone.data.Profile.Field;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit test for {@link Profile}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ProfileTest {

	private final Sone sone = Mockito.mock(Sone.class);
	private final Profile profile = new Profile(sone);

	@Test
	public void newFieldsAreInitializedWithAnEmptyString() {
		Field newField = profile.addField("testField");
		MatcherAssert.assertThat(newField.getValue(), Matchers.is(""));
	}

}
