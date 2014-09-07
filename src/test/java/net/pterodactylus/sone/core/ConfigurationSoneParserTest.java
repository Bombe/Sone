package net.pterodactylus.sone.core;

import static com.google.common.base.Optional.fromNullable;
import static com.google.common.base.Optional.of;
import static java.lang.System.currentTimeMillis;
import static java.util.UUID.randomUUID;
import static net.pterodactylus.sone.Matchers.isPost;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import net.pterodactylus.sone.core.ConfigurationSoneParser.InvalidPostFound;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Profile.Field;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.database.PostBuilder;
import net.pterodactylus.sone.database.PostBuilderFactory;
import net.pterodactylus.util.config.Configuration;
import net.pterodactylus.util.config.ConfigurationException;
import net.pterodactylus.util.config.Value;

import com.google.common.base.Optional;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Unit test for {@link ConfigurationSoneParser}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ConfigurationSoneParserTest {

	private final Configuration configuration = mock(Configuration.class);
	private final Sone sone = mock(Sone.class);
	private final ConfigurationSoneParser configurationSoneParser;

	public ConfigurationSoneParserTest() {
		when(sone.getId()).thenReturn("1");
		configurationSoneParser =
				new ConfigurationSoneParser(configuration, sone);
	}

	@Test
	public void emptyProfileIsLoadedCorrectly() {
		setupEmptyProfile();
		Profile profile = configurationSoneParser.parseProfile();
		assertThat(profile, notNullValue());
		assertThat(profile.getFirstName(), nullValue());
		assertThat(profile.getMiddleName(), nullValue());
		assertThat(profile.getLastName(), nullValue());
		assertThat(profile.getBirthDay(), nullValue());
		assertThat(profile.getBirthMonth(), nullValue());
		assertThat(profile.getBirthYear(), nullValue());
		assertThat(profile.getFields(), emptyIterable());
	}

	private void setupEmptyProfile() {
		when(configuration.getStringValue(anyString())).thenReturn(
				new TestValue<String>(null));
		when(configuration.getIntValue(anyString())).thenReturn(
				new TestValue<Integer>(null));
	}

	@Test
	public void filledProfileWithFieldsIsParsedCorrectly() {
		setupFilledProfile();
		Profile profile = configurationSoneParser.parseProfile();
		assertThat(profile, notNullValue());
		assertThat(profile.getFirstName(), is("First"));
		assertThat(profile.getMiddleName(), is("M."));
		assertThat(profile.getLastName(), is("Last"));
		assertThat(profile.getBirthDay(), is(18));
		assertThat(profile.getBirthMonth(), is(12));
		assertThat(profile.getBirthYear(), is(1976));
		final List<Field> fields = profile.getFields();
		assertThat(fields, hasSize(2));
		assertThat(fields.get(0).getName(), is("Field1"));
		assertThat(fields.get(0).getValue(), is("Value1"));
		assertThat(fields.get(1).getName(), is("Field2"));
		assertThat(fields.get(1).getValue(), is("Value2"));
	}

	private void setupFilledProfile() {
		setupString("Sone/1/Profile/FirstName", "First");
		setupString("Sone/1/Profile/MiddleName", "M.");
		setupString("Sone/1/Profile/LastName", "Last");
		setupInteger("Sone/1/Profile/BirthDay", 18);
		setupInteger("Sone/1/Profile/BirthMonth", 12);
		setupInteger("Sone/1/Profile/BirthYear", 1976);
		setupString("Sone/1/Profile/Fields/0/Name", "Field1");
		setupString("Sone/1/Profile/Fields/0/Value", "Value1");
		setupString("Sone/1/Profile/Fields/1/Name", "Field2");
		setupString("Sone/1/Profile/Fields/1/Value", "Value2");
		setupString("Sone/1/Profile/Fields/2/Name", null);
	}

	private void setupString(String nodeName, String value) {
		when(configuration.getStringValue(eq(nodeName))).thenReturn(
				new TestValue<String>(value));
	}

	private void setupInteger(String nodeName, Integer value) {
		when(configuration.getIntValue(eq(nodeName))).thenReturn(
				new TestValue<Integer>(value));
	}

	@Test
	public void postsAreParsedCorrectly() {
		setupCompletePosts();
		PostBuilderFactory postBuilderFactory = createPostBuilderFactory();
		Collection<Post> posts =
				configurationSoneParser.parsePosts(postBuilderFactory);
		assertThat(posts,
				Matchers.<Post>containsInAnyOrder(
						isPost("P0", 1000L, "T0", Optional.<String>absent()),
						isPost("P1", 1001L, "T1",
								of("1234567890123456789012345678901234567890123"))));
	}

	private PostBuilderFactory createPostBuilderFactory() {
		PostBuilderFactory postBuilderFactory =
				mock(PostBuilderFactory.class);
		when(postBuilderFactory.newPostBuilder()).thenAnswer(
				new Answer<PostBuilder>() {
					@Override
					public PostBuilder answer(InvocationOnMock invocation)
					throws Throwable {
						return new TestPostBuilder();
					}
				});
		return postBuilderFactory;
	}

	private void setupCompletePosts() {
		setupPost("0", "P0", 1000L, "T0", null);
		setupPost("1", "P1", 1001L, "T1",
				"1234567890123456789012345678901234567890123");
		setupPost("2", null, 0L, null, null);
	}

	private void setupPost(String postNumber, String postId, long time,
			String text, String recipientId) {
		setupString("Sone/1/Posts/" + postNumber + "/ID", postId);
		setupLong("Sone/1/Posts/" + postNumber + "/Time", time);
		setupString("Sone/1/Posts/" + postNumber + "/Text", text);
		setupString("Sone/1/Posts/" + postNumber + "/Recipient", recipientId);
	}

	private void setupLong(String nodeName, Long value) {
		when(configuration.getLongValue(eq(nodeName))).thenReturn(
				new TestValue<Long>(value));
	}

	@Test(expected = InvalidPostFound.class)
	public void postWithoutTimeIsRecognized() {
		setupPostWithoutTime();
		configurationSoneParser.parsePosts(createPostBuilderFactory());
	}

	private void setupPostWithoutTime() {
		setupPost("0", "P0", 0L, "T0", null);
	}

	@Test(expected = InvalidPostFound.class)
	public void postWithoutTextIsRecognized() {
		setupPostWithoutText();
		configurationSoneParser.parsePosts(createPostBuilderFactory());
	}

	private void setupPostWithoutText() {
		setupPost("0", "P0", 1000L, null, null);
	}

	@Test
	public void postWithInvalidRecipientIdIsRecognized() {
		setupPostWithInvalidRecipientId();
		Collection<Post> posts = configurationSoneParser.parsePosts(
				createPostBuilderFactory());
		assertThat(posts, contains(
				isPost("P0", 1000L, "T0", Optional.<String>absent())));
	}

	private void setupPostWithInvalidRecipientId() {
		setupPost("0", "P0", 1000L, "T0", "123");
		setupPost("1", null, 0L, null, null);
	}


	private static class TestValue<T> implements Value<T> {

		private final AtomicReference<T> value = new AtomicReference<T>();

		public TestValue(T originalValue) {
			value.set(originalValue);
		}

		@Override
		public T getValue() throws ConfigurationException {
			return value.get();
		}

		@Override
		public T getValue(T defaultValue) {
			final T realValue = value.get();
			return (realValue != null) ? realValue : defaultValue;
		}

		@Override
		public void setValue(T newValue) throws ConfigurationException {
			value.set(newValue);
		}

	}

	private static class TestPostBuilder implements PostBuilder {

		private final Post post = mock(Post.class);
		private String recipientId = null;

		@Override
		public PostBuilder copyPost(Post post) throws NullPointerException {
			return this;
		}

		@Override
		public PostBuilder from(String senderId) {
			final Sone sone = mock(Sone.class);
			when(sone.getId()).thenReturn(senderId);
			when(post.getSone()).thenReturn(sone);
			return this;
		}

		@Override
		public PostBuilder randomId() {
			when(post.getId()).thenReturn(randomUUID().toString());
			return this;
		}

		@Override
		public PostBuilder withId(String id) {
			when(post.getId()).thenReturn(id);
			return this;
		}

		@Override
		public PostBuilder currentTime() {
			when(post.getTime()).thenReturn(currentTimeMillis());
			return this;
		}

		@Override
		public PostBuilder withTime(long time) {
			when(post.getTime()).thenReturn(time);
			return this;
		}

		@Override
		public PostBuilder withText(String text) {
			when(post.getText()).thenReturn(text);
			return this;
		}

		@Override
		public PostBuilder to(String recipientId) {
			this.recipientId = recipientId;
			return this;
		}

		@Override
		public Post build() throws IllegalStateException {
			when(post.getRecipientId()).thenReturn(fromNullable(recipientId));
			return post;
		}

	}

}
