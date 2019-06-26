package net.pterodactylus.sone.template;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Sone;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link CollectionAccessor}.
 */
public class CollectionAccessorTest {

	private final CollectionAccessor accessor = new CollectionAccessor();
	private final Collection<Object> collection = new ArrayList<>();

	@Before
	public void setupCollection() {
		collection.add(new Object());
		collection.add(createSone("One", "1.", "First"));
		collection.add(new Object());
		collection.add(createSone("Two", "2.", "Second"));
	}

	private Sone createSone(String firstName, String middleName,
			String lastName) {
		Sone sone = mock(Sone.class);
		Profile profile = new Profile(sone);
		profile.setFirstName(firstName).setMiddleName(middleName).setLastName(
				lastName);
		when(sone.getProfile()).thenReturn(profile);
		return sone;
	}

	@Test
	public void soneNamesAreConcatenatedCorrectly() {
		assertThat(accessor.get(null, collection, "soneNames"),
				is((Object) "One 1. First, Two 2. Second"));
	}

	@Test
	public void sizeIsReportedCorrectly() {
		assertThat(accessor.get(null, collection, "size"),
				is((Object) Integer.valueOf(4)));
	}

}
