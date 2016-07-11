package net.pterodactylus.sone.core;

import static com.google.common.base.Optional.of;
import static freenet.keys.InsertableClientSSK.createRandom;
import static java.lang.System.currentTimeMillis;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.DAYS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.Album.Modifier;
import net.pterodactylus.sone.data.Client;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.database.AlbumBuilder;
import net.pterodactylus.sone.database.ImageBuilder;
import net.pterodactylus.sone.database.PostBuilder;
import net.pterodactylus.sone.database.PostReplyBuilder;
import net.pterodactylus.sone.database.SoneBuilder;
import net.pterodactylus.sone.database.memory.MemorySoneBuilder;
import net.pterodactylus.sone.freenet.wot.Identity;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;

import freenet.crypt.DummyRandomSource;
import freenet.keys.FreenetURI;
import freenet.keys.InsertableClientSSK;

import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Unit test for {@link SoneParser}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneParserTest {

	private final Core core = mock(Core.class);
	private final SoneParser soneParser = new SoneParser(core);
	private final Sone sone = mock(Sone.class);
	private FreenetURI requestUri = mock(FreenetURI.class);
	private final PostBuilder postBuilder = mock(PostBuilder.class);
	private final List<Post> createdPosts = new ArrayList<Post>();
	private Post post = mock(Post.class);
	private final PostReplyBuilder postReplyBuilder = mock(PostReplyBuilder.class);
	private final Set<PostReply> createdPostReplies = new HashSet<PostReply>();
	private PostReply postReply = mock(PostReply.class);
	private final AlbumBuilder albumBuilder = mock(AlbumBuilder.class);
	private final ListMultimap<Album, Album>
			nestedAlbums = ArrayListMultimap.create();
	private final ListMultimap<Album, Image> albumImages = ArrayListMultimap.create();
	private Album album = mock(Album.class);
	private final Map<String, Album> albums = new HashMap<String, Album>();
	private final ImageBuilder imageBuilder = mock(ImageBuilder.class);
	private Image image = mock(Image.class);
	private final Map<String, Image> images = new HashMap<String, Image>();

	@Before
	public void setupSone() {
		setupSone(this.sone, Identity.class);
	}

	private void setupSone(Sone sone, Class<? extends Identity> identityClass) {
		Identity identity = mock(identityClass);
		InsertableClientSSK clientSSK =
				createRandom(new DummyRandomSource(), "WoT");
		when(identity.getRequestUri()).thenReturn(clientSSK.getURI().toString());
		when(identity.getId()).thenReturn("identity");
		when(sone.getId()).thenReturn("identity");
		when(sone.getIdentity()).thenReturn(identity);
		requestUri = clientSSK.getURI().setKeyType("USK").setDocName("Sone");
		when(sone.getRequestUri()).thenAnswer(new Answer<FreenetURI>() {
			@Override
			public FreenetURI answer(InvocationOnMock invocation)
			throws Throwable {
				return requestUri;
			}
		});
		when(sone.getTime())
				.thenReturn(currentTimeMillis() - DAYS.toMillis(1));
	}

	@Before
	public void setupSoneBuilder() {
		when(core.soneBuilder()).thenAnswer(new Answer<SoneBuilder>() {
			@Override
			public SoneBuilder answer(InvocationOnMock invocation) {
				return new MemorySoneBuilder(null);
			}
		});
	}

	@Before
	public void setupPost() {
		when(post.getRecipientId()).thenReturn(Optional.<String>absent());
	}

	@Before
	public void setupPostBuilder() {
		when(postBuilder.withId(anyString())).thenAnswer(new Answer<PostBuilder>() {
			@Override
			public PostBuilder answer(InvocationOnMock invocation) throws Throwable {
				when(post.getId()).thenReturn((String) invocation.getArguments()[0]);
				return postBuilder;
			}
		});
		when(postBuilder.from(anyString())).thenAnswer(new Answer<PostBuilder>() {
			@Override
			public PostBuilder answer(InvocationOnMock invocation) throws Throwable {
				final Sone sone = mock(Sone.class);
				when(sone.getId()).thenReturn((String) invocation.getArguments()[0]);
				when(post.getSone()).thenReturn(sone);
				return postBuilder;
			}
		});
		when(postBuilder.withTime(anyLong())).thenAnswer(new Answer<PostBuilder>() {
			@Override
			public PostBuilder answer(InvocationOnMock invocation) throws Throwable {
				when(post.getTime()).thenReturn((Long) invocation.getArguments()[0]);
				return postBuilder;
			}
		});
		when(postBuilder.withText(anyString())).thenAnswer(new Answer<PostBuilder>() {
			@Override
			public PostBuilder answer(InvocationOnMock invocation) throws Throwable {
				when(post.getText()).thenReturn((String) invocation.getArguments()[0]);
				return postBuilder;
			}
		});
		when(postBuilder.to(anyString())).thenAnswer(new Answer<PostBuilder>() {
			@Override
			public PostBuilder answer(InvocationOnMock invocation) throws Throwable {
				when(post.getRecipientId()).thenReturn(of((String) invocation.getArguments()[0]));
				return postBuilder;
			}
		});
		when(postBuilder.build()).thenAnswer(new Answer<Post>() {
			@Override
			public Post answer(InvocationOnMock invocation) throws Throwable {
				Post post = SoneParserTest.this.post;
				SoneParserTest.this.post = mock(Post.class);
				setupPost();
				createdPosts.add(post);
				return post;
			}
		});
		when(core.postBuilder()).thenReturn(postBuilder);
	}

	@Before
	public void setupPostReplyBuilder() {
		when(postReplyBuilder.withId(anyString())).thenAnswer(new Answer<PostReplyBuilder>() {
			@Override
			public PostReplyBuilder answer(InvocationOnMock invocation) throws Throwable {
				when(postReply.getId()).thenReturn((String) invocation.getArguments()[0]);
				return postReplyBuilder;
			}
		});
		when(postReplyBuilder.from(anyString())).thenAnswer(
				new Answer<PostReplyBuilder>() {
					@Override
					public PostReplyBuilder answer(
							InvocationOnMock invocation) throws Throwable {
						Sone sone = when(mock(Sone.class).getId()).thenReturn(
								(String) invocation.getArguments()[0])
								.getMock();
						when(postReply.getSone()).thenReturn(sone);
						return postReplyBuilder;
					}
				});
		when(postReplyBuilder.to(anyString())).thenAnswer(
				new Answer<PostReplyBuilder>() {
					@Override
					public PostReplyBuilder answer(
							InvocationOnMock invocation) throws Throwable {
						when(postReply.getPostId()).thenReturn(
								(String) invocation.getArguments()[0]);
						Post post = when(mock(Post.class).getId()).thenReturn(
								(String) invocation.getArguments()[0])
								.getMock();
						when(postReply.getPost()).thenReturn(of(post));
						return postReplyBuilder;
					}
				});
		when(postReplyBuilder.withTime(anyLong())).thenAnswer(
				new Answer<PostReplyBuilder>() {
					@Override
					public PostReplyBuilder answer(
							InvocationOnMock invocation) throws Throwable {
						when(postReply.getTime()).thenReturn(
								(Long) invocation.getArguments()[0]);
						return postReplyBuilder;
					}
				});
		when(postReplyBuilder.withText(anyString())).thenAnswer(new Answer<PostReplyBuilder>() {
			@Override
			public PostReplyBuilder answer(InvocationOnMock invocation) throws Throwable {
				when(postReply.getText()).thenReturn((String) invocation.getArguments()[0]);
				return postReplyBuilder;
			}
		});
		when(postReplyBuilder.build()).thenAnswer(new Answer<PostReply>() {
			@Override
			public PostReply answer(InvocationOnMock invocation) throws Throwable {
				PostReply postReply = SoneParserTest.this.postReply;
				createdPostReplies.add(postReply);
				SoneParserTest.this.postReply = mock(PostReply.class);
				return postReply;
			}
		});
		when(core.postReplyBuilder()).thenReturn(postReplyBuilder);
	}

	@Before
	public void setupAlbum() {
		final Album album = SoneParserTest.this.album;
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) {
				nestedAlbums.put(album, (Album) invocation.getArguments()[0]);
				return null;
			}
		}).when(album).addAlbum(any(Album.class));
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) {
				albumImages.put(album, (Image) invocation.getArguments()[0]);
				return null;
			}
		}).when(album).addImage(any(Image.class));
		when(album.getAlbums()).thenAnswer(new Answer<List<Album>>() {
			@Override
			public List<Album> answer(InvocationOnMock invocation) {
				return nestedAlbums.get(album);
			}
		});
		when(album.getImages()).thenAnswer(new Answer<List<Image>>() {
			@Override
			public List<Image> answer(InvocationOnMock invocation) {
				return albumImages.get(album);
			}
		});
		final Modifier albumModifier = new Modifier() {
			private String title = album.getTitle();
			private String description = album.getDescription();

			@Override
			public Modifier setTitle(String title) {
				this.title = title;
				return this;
			}

			@Override
			public Modifier setDescription(String description) {
				this.description = description;
				return this;
			}

			@Override
			public Album update() throws IllegalStateException {
				when(album.getTitle()).thenReturn(title);
				when(album.getDescription()).thenReturn(description);
				return album;
			}
		};
		when(album.modify()).thenReturn(albumModifier);
	}

	@Before
	public void setupAlbumBuilder() {
		when(albumBuilder.withId(anyString())).thenAnswer(new Answer<AlbumBuilder>() {
			@Override
			public AlbumBuilder answer(InvocationOnMock invocation) {
				when(album.getId()).thenReturn((String) invocation.getArguments()[0]);
				return albumBuilder;
			}
		});
		when(albumBuilder.randomId()).thenAnswer(new Answer<AlbumBuilder>() {
			@Override
			public AlbumBuilder answer(InvocationOnMock invocation) {
				when(album.getId()).thenReturn(randomUUID().toString());
				return albumBuilder;
			}
		});
		when(albumBuilder.by(any(Sone.class))).thenAnswer(new Answer<AlbumBuilder>() {
			@Override
			public AlbumBuilder answer(InvocationOnMock invocation) {
				when(album.getSone()).thenReturn((Sone) invocation.getArguments()[0]);
				return albumBuilder;
			}
		});
		when(albumBuilder.build()).thenAnswer(new Answer<Album>() {
			@Override
			public Album answer(InvocationOnMock invocation) {
				Album album = SoneParserTest.this.album;
				albums.put(album.getId(), album);
				SoneParserTest.this.album = mock(Album.class);
				setupAlbum();
				return album;
			}
		});
		when(core.albumBuilder()).thenReturn(albumBuilder);
	}

	@Before
	public void setupAlbums() {
		when(core.getAlbum(anyString())).thenAnswer(new Answer<Album>() {
			@Override
			public Album answer(InvocationOnMock invocation)
			throws Throwable {
				return albums.get(invocation.getArguments()[0]);
			}
		});
	}

	@Before
	public void setupImage() {
		final Image image = SoneParserTest.this.image;
		Image.Modifier modifier = new Image.Modifier() {
			private Sone sone = image.getSone();
			private long creationTime = image.getCreationTime();
			private String key = image.getKey();
			private String title = image.getTitle();
			private String description = image.getDescription();
			private int width = image.getWidth();
			private int height = image.getHeight();

			@Override
			public Image.Modifier setSone(Sone sone) {
				this.sone = sone;
				return this;
			}

			@Override
			public Image.Modifier setCreationTime(long creationTime) {
				this.creationTime = creationTime;
				return this;
			}

			@Override
			public Image.Modifier setKey(String key) {
				this.key = key;
				return this;
			}

			@Override
			public Image.Modifier setTitle(String title) {
				this.title = title;
				return this;
			}

			@Override
			public Image.Modifier setDescription(String description) {
				this.description = description;
				return this;
			}

			@Override
			public Image.Modifier setWidth(int width) {
				this.width = width;
				return this;
			}

			@Override
			public Image.Modifier setHeight(int height) {
				this.height = height;
				return this;
			}

			@Override
			public Image update() throws IllegalStateException {
				when(image.getSone()).thenReturn(sone);
				when(image.getCreationTime()).thenReturn(creationTime);
				when(image.getKey()).thenReturn(key);
				when(image.getTitle()).thenReturn(title);
				when(image.getDescription()).thenReturn(description);
				when(image.getWidth()).thenReturn(width);
				when(image.getHeight()).thenReturn(height);
				return image;
			}
		};
		when(image.getSone()).thenReturn(sone);
		when(image.modify()).thenReturn(modifier);
	}

	@Before
	public void setupImageBuilder() {
		when(imageBuilder.randomId()).thenAnswer(new Answer<ImageBuilder>() {
			@Override
			public ImageBuilder answer(InvocationOnMock invocation) {
				when(image.getId()).thenReturn(randomUUID().toString());
				return imageBuilder;
			}
		});
		when(imageBuilder.withId(anyString())).thenAnswer(new Answer<ImageBuilder>() {
			@Override
			public ImageBuilder answer(InvocationOnMock invocation) {
				when(image.getId()).thenReturn(
						(String) invocation.getArguments()[0]);
				return imageBuilder;
			}
		});
		when(imageBuilder.build()).thenAnswer(new Answer<Image>() {
			@Override
			public Image answer(InvocationOnMock invocation) {
				Image image = SoneParserTest.this.image;
				images.put(image.getId(), image);
				SoneParserTest.this.image = mock(Image.class);
				setupImage();
				return image;
			}
		});
		when(core.imageBuilder()).thenReturn(imageBuilder);
	}

	@Before
	public void setupImages() {
		when(core.getImage(anyString())).thenAnswer(new Answer<Image>() {
			@Override
			public Image answer(InvocationOnMock invocation)
			throws Throwable {
				return images.get(invocation.getArguments()[0]);
			}
		});
	}
	@Test
	public void parsingASoneFailsWhenDocumentIsNotXml() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-not-xml.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWhenDocumentHasNegativeProtocolVersion() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-negative-protocol-version.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWhenProtocolVersionIsTooLarge() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-too-large-protocol-version.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWhenThereIsNoTime() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-no-time.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWhenTimeIsNotNumeric() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-time-not-numeric.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWhenProfileIsMissing() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-no-profile.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWhenProfileFieldIsMissingAFieldName() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-profile-missing-field-name.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWhenProfileFieldNameIsEmpty() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-profile-empty-field-name.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWhenProfileFieldNameIsNotUnique() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-profile-duplicate-field-name.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneSucceedsWithoutPayload() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-no-payload.xml");
		assertThat(soneParser.parseSone(sone, inputStream).getTime(), is(
				1407197508000L));
	}

	@Test
	public void parsingALocalSoneSucceedsWithoutPayload() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-no-payload.xml");
		Sone localSone = mock(Sone.class);
		setupSone(localSone, OwnIdentity.class);
		when(localSone.isLocal()).thenReturn(true);
		Sone parsedSone = soneParser.parseSone(localSone, inputStream);
		assertThat(parsedSone.getTime(), is(1407197508000L));
		assertThat(parsedSone.isLocal(), is(true));
	}

	@Test
	public void parsingASoneSucceedsWithoutProtocolVersion() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-missing-protocol-version.xml");
		assertThat(soneParser.parseSone(sone, inputStream), not(
				nullValue()));
	}

	@Test
	public void parsingASoneFailsWithMissingClientName() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-missing-client-name.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithMissingClientVersion() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-missing-client-version.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneSucceedsWithClientInfo() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-client-info.xml");
		assertThat(soneParser.parseSone(sone, inputStream).getClient(), is(new Client("some-client", "some-version")));
	}

	@Test
	public void parsingASoneSucceedsWithProfile() throws SoneException,
	MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-profile.xml");
		final Profile profile = soneParser.parseSone(sone, inputStream).getProfile();
		assertThat(profile.getFirstName(), is("first"));
		assertThat(profile.getMiddleName(), is("middle"));
		assertThat(profile.getLastName(), is("last"));
		assertThat(profile.getBirthDay(), is(18));
		assertThat(profile.getBirthMonth(), is(12));
		assertThat(profile.getBirthYear(), is(1976));
	}

	@Test
	public void parsingASoneSucceedsWithoutProfileFields() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-fields.xml");
		assertThat(soneParser.parseSone(sone, inputStream), notNullValue());
	}

	@Test
	public void parsingASoneFailsWithoutPostId() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-post-id.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithoutPostTime() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-post-time.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithoutPostText() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-post-text.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithInvalidPostTime() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-invalid-post-time.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneSucceedsWithValidPostTime() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-valid-post-time.xml");
		final List<Post> posts = soneParser.parseSone(sone, inputStream).getPosts();
		assertThat(posts, is(createdPosts));
		assertThat(posts.get(0).getSone().getId(), is(sone.getId()));
		assertThat(posts.get(0).getId(), is("post-id"));
		assertThat(posts.get(0).getTime(), is(1407197508000L));
		assertThat(posts.get(0).getRecipientId(), is(Optional.<String>absent()));
		assertThat(posts.get(0).getText(), is("text"));
	}

	@Test
	public void parsingASoneSucceedsWithRecipient() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-recipient.xml");
		final List<Post> posts = soneParser.parseSone(sone, inputStream).getPosts();
		assertThat(posts, is(createdPosts));
		assertThat(posts.get(0).getSone().getId(), is(sone.getId()));
		assertThat(posts.get(0).getId(), is("post-id"));
		assertThat(posts.get(0).getTime(), is(1407197508000L));
		assertThat(posts.get(0).getRecipientId(), is(of(
				"1234567890123456789012345678901234567890123")));
		assertThat(posts.get(0).getText(), is("text"));
	}

	@Test
	public void parsingASoneSucceedsWithInvalidRecipient() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-invalid-recipient.xml");
		final List<Post> posts = soneParser.parseSone(sone, inputStream).getPosts();
		assertThat(posts, is(createdPosts));
		assertThat(posts.get(0).getSone().getId(), is(sone.getId()));
		assertThat(posts.get(0).getId(), is("post-id"));
		assertThat(posts.get(0).getTime(), is(1407197508000L));
		assertThat(posts.get(0).getRecipientId(), is(Optional.<String>absent()));
		assertThat(posts.get(0).getText(), is("text"));
	}

	@Test
	public void parsingASoneFailsWithoutPostReplyId() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-post-reply-id.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithoutPostReplyPostId() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-post-reply-post-id.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithoutPostReplyTime() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-post-reply-time.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithoutPostReplyText() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-post-reply-text.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithInvalidPostReplyTime() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-invalid-post-reply-time.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneSucceedsWithValidPostReplyTime() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-valid-post-reply-time.xml");
		final Set<PostReply> postReplies = soneParser.parseSone(sone, inputStream).getReplies();
		assertThat(postReplies, is(createdPostReplies));
		PostReply postReply = createdPostReplies.iterator().next();
		assertThat(postReply.getId(), is("reply-id"));
		assertThat(postReply.getPostId(), is("post-id"));
		assertThat(postReply.getPost().get().getId(), is("post-id"));
		assertThat(postReply.getSone().getId(), is("identity"));
		assertThat(postReply.getTime(), is(1407197508000L));
		assertThat(postReply.getText(), is("reply-text"));
	}

	@Test
	public void parsingASoneSucceedsWithoutLikedPostIds() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-liked-post-ids.xml");
		assertThat(soneParser.parseSone(sone, inputStream), not(
				nullValue()));
	}

	@Test
	public void parsingASoneSucceedsWithLikedPostIds() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-liked-post-ids.xml");
		assertThat(soneParser.parseSone(sone, inputStream).getLikedPostIds(), is(
				(Set<String>) ImmutableSet.of("liked-post-id")));
	}

	@Test
	public void parsingASoneSucceedsWithoutLikedPostReplyIds() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-liked-post-reply-ids.xml");
		assertThat(soneParser.parseSone(sone, inputStream), not(
				nullValue()));
	}

	@Test
	public void parsingASoneSucceedsWithLikedPostReplyIds() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-liked-post-reply-ids.xml");
		assertThat(soneParser.parseSone(sone, inputStream).getLikedReplyIds(), is(
				(Set<String>) ImmutableSet.of("liked-post-reply-id")));
	}

	@Test
	public void parsingASoneSucceedsWithoutAlbums() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-albums.xml");
		assertThat(soneParser.parseSone(sone, inputStream), not(
				nullValue()));
	}

	@Test
	public void parsingASoneFailsWithoutAlbumId() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-album-id.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithoutAlbumTitle() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-album-title.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneSucceedsWithNestedAlbums() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-multiple-albums.xml");
		final Sone parsedSone = soneParser.parseSone(sone, inputStream);
		assertThat(parsedSone, not(nullValue()));
		assertThat(parsedSone.getRootAlbum().getAlbums(), hasSize(1));
		Album album = parsedSone.getRootAlbum().getAlbums().get(0);
		assertThat(album.getId(), is("album-id-1"));
		assertThat(album.getTitle(), is("album-title"));
		assertThat(album.getDescription(), is("album-description"));
		assertThat(album.getAlbums(), hasSize(1));
		Album nestedAlbum = album.getAlbums().get(0);
		assertThat(nestedAlbum.getId(), is("album-id-2"));
		assertThat(nestedAlbum.getTitle(), is("album-title-2"));
		assertThat(nestedAlbum.getDescription(), is("album-description-2"));
		assertThat(nestedAlbum.getAlbums(), hasSize(0));
	}

	@Test
	public void parsingASoneFailsWithInvalidParentAlbumId() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-invalid-parent-album-id.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneSucceedsWithoutImages() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-images.xml");
		assertThat(soneParser.parseSone(sone, inputStream), not(
				nullValue()));
	}

	@Test
	public void parsingASoneFailsWithoutImageId() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-image-id.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithoutImageTime() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-image-time.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithoutImageKey() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-image-key.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithoutImageTitle() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-image-title.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithoutImageWidth() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-image-width.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithoutImageHeight() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-image-height.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithInvalidImageWidth() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-invalid-image-width.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithInvalidImageHeight() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-invalid-image-height.xml");
		assertThat(soneParser.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneSucceedsWithImage() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-image.xml");
		final Sone sone = soneParser.parseSone(this.sone, inputStream);
		assertThat(sone, not(nullValue()));
		assertThat(sone.getRootAlbum().getAlbums(), hasSize(1));
		assertThat(sone.getRootAlbum().getAlbums().get(0).getImages(), hasSize(1));
		Image image = sone.getRootAlbum().getAlbums().get(0).getImages().get(0);
		assertThat(image.getId(), is("image-id"));
		assertThat(image.getCreationTime(), is(1407197508000L));
		assertThat(image.getKey(), is("KSK@GPLv3.txt"));
		assertThat(image.getTitle(), is("image-title"));
		assertThat(image.getDescription(), is("image-description"));
		assertThat(image.getWidth(), is(1920));
		assertThat(image.getHeight(), is(1080));
		assertThat(sone.getProfile().getAvatar(), is("image-id"));
	}


}
