package net.pterodactylus.sone.database.memory;

import java.util.HashSet;
import java.util.Set;

import net.pterodactylus.util.config.Configuration;

/**
 * Helper class for interacting with a {@link Configuration}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ConfigurationLoader {

	private final Configuration configuration;

	public ConfigurationLoader(Configuration configuration) {
		this.configuration = configuration;
	}

	public synchronized Set<String> loadKnownPosts() {
		Set<String> knownPosts = new HashSet<String>();
		int postCounter = 0;
		while (true) {
			String knownPostId = configuration
					.getStringValue("KnownPosts/" + postCounter++ + "/ID")
					.getValue(null);
			if (knownPostId == null) {
				break;
			}
			knownPosts.add(knownPostId);
		}
		return knownPosts;
	}

	public synchronized Set<String> loadKnownPostReplies() {
		Set<String> knownPostReplies = new HashSet<String>();
		int replyCounter = 0;
		while (true) {
			String knownReplyId = configuration
					.getStringValue("KnownReplies/" + replyCounter++ + "/ID")
					.getValue(null);
			if (knownReplyId == null) {
				break;
			}
			knownPostReplies.add(knownReplyId);
		}
		return knownPostReplies;
	}

}
