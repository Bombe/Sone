package net.pterodactylus.sone.core;

import static com.google.common.base.Optional.of;
import static net.pterodactylus.sone.data.Sone.SoneStatus.downloading;
import static net.pterodactylus.sone.data.Sone.SoneStatus.idle;
import static net.pterodactylus.sone.data.Sone.SoneStatus.unknown;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.pterodactylus.sone.core.FreenetInterface.Fetched;
import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.AlbumImpl;
import net.pterodactylus.sone.data.Client;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.data.ImageImpl;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.Sone.SoneStatus;
import net.pterodactylus.sone.database.PostBuilder;
import net.pterodactylus.sone.database.PostReplyBuilder;
import net.pterodactylus.sone.freenet.wot.Identity;

import freenet.client.ClientMetadata;
import freenet.client.FetchResult;
import freenet.keys.FreenetURI;
import freenet.support.api.Bucket;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Unit test for {@link SoneDownloaderImpl} and its subclasses.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneDownloaderTest {

	private final Core core = mock(Core.class);
	private final FreenetInterface freenetInterface = mock(FreenetInterface.class);
	private final SoneDownloaderImpl soneDownloader = new SoneDownloaderImpl(core, freenetInterface);
	private final FreenetURI requestUri = mock(FreenetURI.class);
	private final Sone sone = mock(Sone.class);
	private final PostBuilder postBuilder = mock(PostBuilder.class);
	private final List<Post> createdPosts = new ArrayList<Post>();
	private Post post = mock(Post.class);
	private final PostReplyBuilder postReplyBuilder = mock(PostReplyBuilder.class);
	private final Set<PostReply> createdPostReplies = new HashSet<PostReply>();
	private PostReply postReply = mock(PostReply.class);
	private final Map<String, Album> albums = new HashMap<String, Album>();

	@Before
	public void setupSone() {
		Identity identity = mock(Identity.class);
		when(identity.getId()).thenReturn("identity");
		when(sone.getId()).thenReturn("identity");
		when(sone.getIdentity()).thenReturn(identity);
		when(sone.getRequestUri()).thenReturn(requestUri);
	}

	@Before
	public void setupRequestUri() {
		when(requestUri.setKeyType(anyString())).thenReturn(requestUri);
		when(requestUri.sskForUSK()).thenReturn(requestUri);
		when(requestUri.setDocName(anyString())).thenReturn(requestUri);
		when(requestUri.setMetaString(any(String[].class))).thenReturn(requestUri);
		when(requestUri.getKeyType()).thenReturn("USK");
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
				Post post = SoneDownloaderTest.this.post;
				SoneDownloaderTest.this.post = mock(Post.class);
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
		when(postReplyBuilder.from(anyString())).thenAnswer(new Answer<PostReplyBuilder>() {
			@Override
			public PostReplyBuilder answer(InvocationOnMock invocation) throws Throwable {
				Sone sone = when(mock(Sone.class).getId()).thenReturn((String) invocation.getArguments()[0]).getMock();
				when(postReply.getSone()).thenReturn(sone);
				return postReplyBuilder;
			}
		});
		when(postReplyBuilder.to(anyString())).thenAnswer(new Answer<PostReplyBuilder>() {
			@Override
			public PostReplyBuilder answer(InvocationOnMock invocation) throws Throwable {
				when(postReply.getPostId()).thenReturn((String) invocation.getArguments()[0]);
				Post post = when(mock(Post.class).getId()).thenReturn((String) invocation.getArguments()[0]).getMock();
				when(postReply.getPost()).thenReturn(of(post));
				return postReplyBuilder;
			}
		});
		when(postReplyBuilder.withTime(anyLong())).thenAnswer(new Answer<PostReplyBuilder>() {
			@Override
			public PostReplyBuilder answer(InvocationOnMock invocation) throws Throwable {
				when(postReply.getTime()).thenReturn((Long) invocation.getArguments()[0]);
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
				PostReply postReply = SoneDownloaderTest.this.postReply;
				createdPostReplies.add(postReply);
				SoneDownloaderTest.this.postReply = mock(PostReply.class);
				return postReply;
			}
		});
		when(core.postReplyBuilder()).thenReturn(postReplyBuilder);
	}

	@Before
	public void setupAlbums() {
		albums.put("album-id-1", new AlbumImpl("album-id-1"));
		albums.put("album-id-2", new AlbumImpl("album-id-2"));
		when(core.getOrCreateAlbum(anyString())).thenAnswer(new Answer<Album>() {
			@Override
			public Album answer(InvocationOnMock invocation) throws Throwable {
				return albums.get(invocation.getArguments()[0]);
			}
		});
		when(core.getAlbum(anyString(), anyBoolean())).thenAnswer(new Answer<Album>() {
			@Override
			public Album answer(InvocationOnMock invocation) throws Throwable {
				return albums.get(invocation.getArguments()[0]);
			}
		});
	}

	@Before
	public void setupImages() {
		Image image = new ImageImpl("image-id");
		when(core.getImage("image-id")).thenReturn(image);
		when(core.getImage(eq("image-id"), anyBoolean())).thenReturn(image);
	}

	@Test
	public void addingASoneWillRegisterItsKey() {
		soneDownloader.addSone(sone);
		verify(freenetInterface).registerUsk(sone, soneDownloader);
		verify(freenetInterface, never()).unregisterUsk(sone);
	}

	@Test
	public void addingASoneTwiceWillAlsoDeregisterItsKey() {
		soneDownloader.addSone(sone);
		soneDownloader.addSone(sone);
		verify(freenetInterface, times(2)).registerUsk(sone, soneDownloader);
		verify(freenetInterface).unregisterUsk(sone);
	}

	@Test
	public void parsingASoneFailsWhenDocumentIsNotXml() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-not-xml.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWhenDocumentHasNegativeProtocolVersion() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-negative-protocol-version.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWhenProtocolVersionIsTooLarge() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-too-large-protocol-version.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWhenThereIsNoTime() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-no-time.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWhenTimeIsNotNumeric() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-time-not-numeric.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWhenProfileIsMissing() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-no-profile.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWhenProfileFieldIsMissingAFieldName() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-profile-missing-field-name.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWhenProfileFieldNameIsEmpty() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-profile-empty-field-name.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWhenProfileFieldNameIsNotUnique() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-profile-duplicate-field-name.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneSucceedsWithoutPayload() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-no-payload.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream).getTime(), is(1407197508000L));
	}

	@Test
	public void parsingASoneSucceedsWithoutProtocolVersion() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-missing-protocol-version.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), not(nullValue()));
	}

	@Test
	public void parsingASoneFailsWithMissingClientName() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-missing-client-name.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithMissingClientVersion() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-missing-client-version.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneSucceedsWithClientInfo() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-client-info.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream).getClient(), is(new Client("some-client", "some-version")));
	}

	@Test
	public void parsingASoneSucceedsWithRequestUri() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-request-uri.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream).getRequestUri().toString(), is("USK@w~RyTGmv12Lg9oO91q1Untupi7my9qczT1RheGkEkVE,E8ElVfUgukSCPHxIEJp-gHMiR80wpM7sID3Jo5O7w1s,AQACAAE/Sone/0"));
	}

	@Test
	public void parsingASoneFailsWithInvalidRequestUri() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-invalid-request-uri.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void soneInsertUriIsCopiedToNewSone() throws SoneException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-no-payload.xml");
		FreenetURI insertUri = mock(FreenetURI.class);
		when(insertUri.setKeyType(anyString())).thenReturn(insertUri);
		when(insertUri.setDocName(anyString())).thenReturn(insertUri);
		when(insertUri.setMetaString(any(String[].class))).thenReturn(insertUri);
		when(insertUri.setSuggestedEdition(anyLong())).thenReturn(insertUri);
		when(sone.getInsertUri()).thenReturn(insertUri);
		assertThat(soneDownloader.parseSone(sone, inputStream).getInsertUri(), is(insertUri));
	}

	@Test
	public void parsingASoneSucceedsWithProfile() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-profile.xml");
		final Profile profile = soneDownloader.parseSone(sone, inputStream).getProfile();
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
		assertThat(soneDownloader.parseSone(sone, inputStream), notNullValue());
	}

	@Test
	public void parsingASoneFailsWithoutPostId() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-post-id.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithoutPostTime() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-post-time.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithoutPostText() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-post-text.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithInvalidPostTime() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-invalid-post-time.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneSucceedsWithValidPostTime() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-valid-post-time.xml");
		final List<Post> posts = soneDownloader.parseSone(sone, inputStream).getPosts();
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
		final List<Post> posts = soneDownloader.parseSone(sone, inputStream).getPosts();
		assertThat(posts, is(createdPosts));
		assertThat(posts.get(0).getSone().getId(), is(sone.getId()));
		assertThat(posts.get(0).getId(), is("post-id"));
		assertThat(posts.get(0).getTime(), is(1407197508000L));
		assertThat(posts.get(0).getRecipientId(), is(of("1234567890123456789012345678901234567890123")));
		assertThat(posts.get(0).getText(), is("text"));
	}

	@Test
	public void parsingASoneSucceedsWithInvalidRecipient() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-invalid-recipient.xml");
		final List<Post> posts = soneDownloader.parseSone(sone, inputStream).getPosts();
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
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithoutPostReplyPostId() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-post-reply-post-id.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithoutPostReplyTime() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-post-reply-time.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithoutPostReplyText() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-post-reply-text.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithInvalidPostReplyTime() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-invalid-post-reply-time.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneSucceedsWithValidPostReplyTime() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-valid-post-reply-time.xml");
		final Set<PostReply> postReplies = soneDownloader.parseSone(sone, inputStream).getReplies();
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
		assertThat(soneDownloader.parseSone(sone, inputStream), not(nullValue()));
	}

	@Test
	public void parsingASoneSucceedsWithLikedPostIds() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-liked-post-ids.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream).getLikedPostIds(), is((Set<String>) ImmutableSet.of("liked-post-id")));
	}

	@Test
	public void parsingASoneSucceedsWithoutLikedPostReplyIds() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-liked-post-reply-ids.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), not(nullValue()));
	}

	@Test
	public void parsingASoneSucceedsWithLikedPostReplyIds() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-liked-post-reply-ids.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream).getLikedReplyIds(), is((Set<String>) ImmutableSet.of("liked-post-reply-id")));
	}

	@Test
	public void parsingASoneSucceedsWithoutAlbums() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-albums.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), not(nullValue()));
	}

	@Test
	public void parsingASoneFailsWithoutAlbumId() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-album-id.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithoutAlbumTitle() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-album-title.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneSucceedsWithNestedAlbums() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-multiple-albums.xml");
		final Sone parsedSone = soneDownloader.parseSone(sone, inputStream);
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
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneSucceedsWithoutImages() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-images.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), not(nullValue()));
	}

	@Test
	public void parsingASoneFailsWithoutImageId() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-image-id.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithoutImageTime() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-image-time.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithoutImageKey() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-image-key.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithoutImageTitle() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-image-title.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithoutImageWidth() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-image-width.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithoutImageHeight() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-without-image-height.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithInvalidImageWidth() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-invalid-image-width.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneFailsWithInvalidImageHeight() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-invalid-image-height.xml");
		assertThat(soneDownloader.parseSone(sone, inputStream), nullValue());
	}

	@Test
	public void parsingASoneSucceedsWithImage() throws SoneException, MalformedURLException {
		InputStream inputStream = getClass().getResourceAsStream("sone-parser-with-image.xml");
		final Sone sone = soneDownloader.parseSone(this.sone, inputStream);
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

	@Test
	public void stoppingTheSoneDownloaderUnregistersTheSone() {
		soneDownloader.addSone(sone);
		soneDownloader.stop();
		verify(freenetInterface).unregisterUsk(sone);
	}

	@Test
	public void notBeingAbleToFetchAnUnknownSoneDoesNotUpdateCore() {
		soneDownloader.fetchSone(sone);
		verify(freenetInterface).fetchUri(requestUri);
		verifyThatSoneStatusWasChangedToDownloadingAndBackTo(unknown);
		verify(core, never()).updateSone(any(Sone.class));
	}

	private void verifyThatSoneStatusWasChangedToDownloadingAndBackTo(SoneStatus soneStatus) {
		ArgumentCaptor<SoneStatus> soneStatuses = forClass(SoneStatus.class);
		verify(sone, times(2)).setStatus(soneStatuses.capture());
		assertThat(soneStatuses.getAllValues().get(0), is(downloading));
		assertThat(soneStatuses.getAllValues().get(1), is(soneStatus));
	}

	@Test
	public void notBeingAbleToFetchAKnownSoneDoesNotUpdateCore() {
		when(sone.getTime()).thenReturn(1000L);
		soneDownloader.fetchSone(sone);
		verify(freenetInterface).fetchUri(requestUri);
		verifyThatSoneStatusWasChangedToDownloadingAndBackTo(idle);
		verify(core, never()).updateSone(any(Sone.class));
	}

	@Test(expected = NullPointerException.class)
	public void exceptionWhileFetchingAnUnknownSoneDoesNotUpdateCore() {
		when(freenetInterface.fetchUri(requestUri)).thenThrow(NullPointerException.class);
		try {
			soneDownloader.fetchSone(sone);
		} finally {
			verify(freenetInterface).fetchUri(requestUri);
			verifyThatSoneStatusWasChangedToDownloadingAndBackTo(unknown);
			verify(core, never()).updateSone(any(Sone.class));
		}
	}

	@Test(expected = NullPointerException.class)
	public void exceptionWhileFetchingAKnownSoneDoesNotUpdateCore() {
		when(sone.getTime()).thenReturn(1000L);
		when(freenetInterface.fetchUri(requestUri)).thenThrow(NullPointerException.class);
		try {
			soneDownloader.fetchSone(sone);
		} finally {
			verify(freenetInterface).fetchUri(requestUri);
			verifyThatSoneStatusWasChangedToDownloadingAndBackTo(idle);
			verify(core, never()).updateSone(any(Sone.class));
		}
	}

	@Test
	public void successfulFetchingOfSoneWithUskRequestUriUpdatesTheCoreWithASone() throws IOException {
		final Fetched fetchResult = createFetchResult(requestUri, getClass().getResourceAsStream("sone-parser-no-payload.xml"));
		when(freenetInterface.fetchUri(requestUri)).thenReturn(fetchResult);
		soneDownloader.fetchSone(sone);
		verifyThatParsedSoneHasTheSameIdAsTheOriginalSone();
	}

	private void verifyThatParsedSoneHasTheSameIdAsTheOriginalSone() {
		ArgumentCaptor<Sone> soneCaptor = forClass(Sone.class);
		verify(core).updateSone(soneCaptor.capture());
		assertThat(soneCaptor.getValue().getId(), is(sone.getId()));
	}

	@Test
	public void successfulFetchingOfSoneWithSskRequestUriUpdatesTheCoreWithASone() throws IOException {
		when(requestUri.getKeyType()).thenReturn("SSK");
		final Fetched fetchResult = createFetchResult(requestUri, getClass().getResourceAsStream("sone-parser-no-payload.xml"));
		when(freenetInterface.fetchUri(requestUri)).thenReturn(fetchResult);
		soneDownloader.fetchSone(sone);
		verifyThatParsedSoneHasTheSameIdAsTheOriginalSone();
	}

	@Test
	public void successfulFetchingAnUnknownSoneWithSskRequestUriUpdatesTheCoreWithASone() throws IOException {
		when(requestUri.getKeyType()).thenReturn("SSK");
		final Fetched fetchResult = createFetchResult(requestUri, getClass().getResourceAsStream("sone-parser-with-zero-time.xml"));
		when(freenetInterface.fetchUri(requestUri)).thenReturn(fetchResult);
		soneDownloader.fetchSone(sone);
		verifyThatParsedSoneHasTheSameIdAsTheOriginalSone();
	}

	@Test
	public void fetchingSoneWithInvalidXmlWillNotUpdateTheCore() throws IOException {
		final Fetched fetchResult = createFetchResult(requestUri, getClass().getResourceAsStream("sone-parser-not-xml.xml"));
		when(freenetInterface.fetchUri(requestUri)).thenReturn(fetchResult);
		soneDownloader.fetchSone(sone);
		verify(core, never()).updateSone(any(Sone.class));
	}

	@Test
	public void exceptionWhileFetchingSoneWillNotUpdateTheCore() throws IOException {
		final Fetched fetchResult = createFetchResult(requestUri, getClass().getResourceAsStream("sone-parser-no-payload.xml"));
		when(sone.getId()).thenThrow(NullPointerException.class);
		when(freenetInterface.fetchUri(requestUri)).thenReturn(fetchResult);
		soneDownloader.fetchSone(sone);
		verify(core, never()).updateSone(any(Sone.class));
	}

	@Test
	public void onlyFetchingASoneWillNotUpdateTheCore() throws IOException {
		final Fetched fetchResult = createFetchResult(requestUri, getClass().getResourceAsStream("sone-parser-no-payload.xml"));
		when(freenetInterface.fetchUri(requestUri)).thenReturn(fetchResult);
		soneDownloader.fetchSone(sone, sone.getRequestUri(), true);
		verify(core, never()).updateSone(any(Sone.class));
		verifyThatSoneStatusWasChangedToDownloadingAndBackTo(unknown);
	}

	private Fetched createFetchResult(FreenetURI uri, InputStream inputStream) throws IOException {
		ClientMetadata clientMetadata = new ClientMetadata("application/xml");
		Bucket bucket = mock(Bucket.class);
		when(bucket.getInputStream()).thenReturn(inputStream);
		FetchResult fetchResult = new FetchResult(clientMetadata, bucket);
		return new Fetched(uri, fetchResult);
	}

}
