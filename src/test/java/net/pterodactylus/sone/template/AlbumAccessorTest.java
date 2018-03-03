package net.pterodactylus.sone.template;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.test.TestUtil;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link AlbumAccessor}.
 */
public class AlbumAccessorTest {

	private final AlbumAccessor albumAccessor = new AlbumAccessor();
	private final Album album = mock(Album.class);

	@Before
	public void setupAlbum() {
		when(album.getId()).thenReturn("Album");
		when(album.getTitle()).thenReturn("Album Title");
	}

	@Test
	public void backlinksAreGenerated() {
		Sone sone = mock(Sone.class);
		Profile profile = new Profile(sone);
		when(sone.getId()).thenReturn("Sone");
		when(sone.getName()).thenReturn("Sone Name");
		when(sone.getProfile()).thenReturn(profile);
		Album parentAlbum = mock(Album.class);
		when(parentAlbum.isRoot()).thenReturn(true);
		when(album.getSone()).thenReturn(sone);
		when(album.getParent()).thenReturn(parentAlbum);
		List<Object> backlinks =
				(List<Object>) albumAccessor.get(null, album, "backlinks");
		assertThat(backlinks, contains(isLink("sone=Sone", "Sone Name"),
				isLink("album=Album", "Album Title")));
	}

	@Test
	public void nameIsGenerated() {
		assertThat((String) albumAccessor.get(null, album, "id"),
				is("Album"));
		assertThat((String) albumAccessor.get(null, album, "title"),
				is("Album Title"));
	}

	private static Matcher<Object> isLink(final String target,
			final String name) {
		return new TypeSafeDiagnosingMatcher<Object>() {
			@Override
			protected boolean matchesSafely(Object item,
					Description mismatchDescription) {
				if (!TestUtil.<String>callPrivateMethod(item, "getTarget")
						.contains(target)) {
					mismatchDescription.appendText("link does not contain ")
							.appendValue(target);
					return false;
				}
				if (!TestUtil.<String>callPrivateMethod(item, "getName")
						.equals(name)) {
					mismatchDescription.appendText("is not named ")
							.appendValue(name);
					return false;
				}
				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("link containing ")
						.appendValue(target);
				description.appendText(", named ").appendValue(name);
			}
		};
	}

	@Test
	public void albumImageIsGeneratedRandomly() {
		Image image = mock(Image.class);
		List<Image> albumImages = Arrays.asList(mock(Image.class), image);
		when(album.getImages()).thenReturn(albumImages);
		int matchedImage = 0;
		for (int i = 0; i < 1000; i++) {
			Image randomImage = (Image) albumAccessor.get(null, album, "albumImage");
			if (randomImage == image) {
				matchedImage++;
			}
		}
		assertThat(matchedImage, allOf(greaterThanOrEqualTo(250), lessThanOrEqualTo(750)));
	}

	@Test
	public void albumImageIsNullIfThereAreNoImagesInAnAlbum() {
		when(album.getImages()).thenReturn(Collections.<Image>emptyList());
		assertThat(albumAccessor.get(null, album, "albumImage"), nullValue());
	}

}
