package net.pterodactylus.sone.database.memory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import net.pterodactylus.sone.TestValue;
import net.pterodactylus.util.config.Configuration;

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
				.thenReturn(new TestValue<String>("Post2"));
		when(configuration.getStringValue("KnownPosts/1/ID"))
				.thenReturn(new TestValue<String>("Post1"));
		when(configuration.getStringValue("KnownPosts/2/ID"))
				.thenReturn(new TestValue<String>(null));
		Set<String> knownPosts = configurationLoader.loadKnownPosts();
		assertThat(knownPosts, containsInAnyOrder("Post1", "Post2"));
	}

	@Test
	public void loaderCanLoadKnownPostReplies() {
		when(configuration.getStringValue("KnownReplies/0/ID"))
				.thenReturn(new TestValue<String>("PostReply2"));
		when(configuration.getStringValue("KnownReplies/1/ID"))
				.thenReturn(new TestValue<String>("PostReply1"));
		when(configuration.getStringValue("KnownReplies/2/ID"))
				.thenReturn(new TestValue<String>(null));
		Set<String> knownPosts = configurationLoader.loadKnownPostReplies();
		assertThat(knownPosts,
				containsInAnyOrder("PostReply1", "PostReply2"));
	}

}
