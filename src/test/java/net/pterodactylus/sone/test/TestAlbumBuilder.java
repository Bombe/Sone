package net.pterodactylus.sone.test;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.Album.Modifier;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.database.AlbumBuilder;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * {@link AlbumBuilder} that returns a mocked {@link Album}.
 */
public class TestAlbumBuilder implements AlbumBuilder {

	private final Album album = mock(Album.class);
	private final List<Album> albums = new ArrayList<>();
	private final List<Image> images = new ArrayList<>();
	private Album parentAlbum;
	private String title;
	private String description;
	private String imageId;

	public TestAlbumBuilder() {
		when(album.getTitle()).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) {
				return title;
			}
		});
		when(album.getDescription()).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) {
				return description;
			}
		});
		when(album.getAlbums()).thenReturn(albums);
		when(album.getImages()).thenReturn(images);
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) {
				albums.add((Album) invocation.getArguments()[0]);
				((Album) invocation.getArguments()[0]).setParent(album);
				return null;
			}
		}).when(album).addAlbum(any(Album.class));
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) {
				images.add((Image) invocation.getArguments()[0]);
				return null;
			}
		}).when(album).addImage(any(Image.class));
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) {
				parentAlbum = (Album) invocation.getArguments()[0];
				return null;
			}
		}).when(album).setParent(any(Album.class));
		when(album.getParent()).thenAnswer(new Answer<Album>() {
			@Override
			public Album answer(InvocationOnMock invocation) {
				return parentAlbum;
			}
		});
		when(album.modify()).thenReturn(new Modifier() {
			@Override
			public Modifier setTitle(String title) {
				TestAlbumBuilder.this.title = title;
				return this;
			}

			@Override
			public Modifier setDescription(String description) {
				TestAlbumBuilder.this.description = description;
				return this;
			}

			@Override
			public Album update() throws IllegalStateException {
				return album;
			}
		});
	}

	@Override
	public AlbumBuilder randomId() {
		when(album.getId()).thenReturn(randomUUID().toString());
		return this;
	}

	@Override
	public AlbumBuilder withId(String id) {
		when(album.getId()).thenReturn(id);
		return this;
	}

	@Override
	public AlbumBuilder by(Sone sone) {
		when(album.getSone()).thenReturn(sone);
		return this;
	}

	@Override
	public Album build() throws IllegalStateException {
		return album;
	}

}
