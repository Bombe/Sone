package net.pterodactylus.sone.database.memory;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import net.pterodactylus.sone.test.TestValue;
import net.pterodactylus.util.config.Configuration;
import net.pterodactylus.util.config.ConfigurationException;
import net.pterodactylus.util.config.Value;

import org.junit.Test;

/**
 * Unit test for {@link ConfigurationLoader}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ConfigurationLoaderTest {

	private final Configuration configuration = mock(Configuration.class);
	private final ConfigurationLoader configurationLoader =
			new ConfigurationLoader(configuration);

	@Test
	public void loaderCanLoadKnownPosts() {
		when(configuration.getStringValue("KnownPosts/0/ID"))
				.thenReturn(TestValue.from("Post2"));
		when(configuration.getStringValue("KnownPosts/1/ID"))
				.thenReturn(TestValue.from("Post1"));
		when(configuration.getStringValue("KnownPosts/2/ID"))
				.thenReturn(TestValue.<String>from(null));
		Set<String> knownPosts = configurationLoader.loadKnownPosts();
		assertThat(knownPosts, containsInAnyOrder("Post1", "Post2"));
	}

	@Test
	public void loaderCanLoadKnownPostReplies() {
		when(configuration.getStringValue("KnownReplies/0/ID"))
				.thenReturn(TestValue.from("PostReply2"));
		when(configuration.getStringValue("KnownReplies/1/ID"))
				.thenReturn(TestValue.from("PostReply1"));
		when(configuration.getStringValue("KnownReplies/2/ID"))
				.thenReturn(TestValue.<String>from(null));
		Set<String> knownPosts = configurationLoader.loadKnownPostReplies();
		assertThat(knownPosts,
				containsInAnyOrder("PostReply1", "PostReply2"));
	}

	@Test
	public void loaderCanLoadBookmarkedPosts() {
		when(configuration.getStringValue("Bookmarks/Post/0/ID"))
				.thenReturn(TestValue.from("Post2"));
		when(configuration.getStringValue("Bookmarks/Post/1/ID"))
				.thenReturn(TestValue.from("Post1"));
		when(configuration.getStringValue("Bookmarks/Post/2/ID"))
				.thenReturn(TestValue.<String>from(null));
		Set<String> knownPosts = configurationLoader.loadBookmarkedPosts();
		assertThat(knownPosts, containsInAnyOrder("Post1", "Post2"));
	}

	@Test
	public void loaderCanSaveBookmarkedPosts() throws ConfigurationException {
		final Value<String> post1 = TestValue.<String>from(null);
		final Value<String> post2 = TestValue.<String>from(null);
		final Value<String> post3 = TestValue.<String>from(null);
		when(configuration.getStringValue("Bookmarks/Post/0/ID")).thenReturn(post1);
		when(configuration.getStringValue("Bookmarks/Post/1/ID")).thenReturn(post2);
		when(configuration.getStringValue("Bookmarks/Post/2/ID")).thenReturn(post3);
		HashSet<String> originalPosts = new HashSet<String>(asList("Post1", "Post2"));
		configurationLoader.saveBookmarkedPosts(originalPosts);
		HashSet<String> extractedPosts =
				new HashSet<String>(asList(post1.getValue(), post2.getValue()));
		assertThat(extractedPosts, containsInAnyOrder("Post1", "Post2"));
		assertThat(post3.getValue(), nullValue());
	}

}
