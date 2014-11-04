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
		return loadIds("KnownPosts");
	}

	public synchronized Set<String> loadKnownPostReplies() {
		return loadIds("KnownReplies");
	}

	public synchronized Set<String> loadBookmarkedPosts() {
		return loadIds("Bookmarks/Post");
	}

	private Set<String> loadIds(String prefix) {
		Set<String> ids = new HashSet<String>();
		int idCounter = 0;
		while (true) {
			String id = configuration
					.getStringValue(prefix + "/" + idCounter++ + "/ID")
					.getValue(null);
			if (id == null) {
				break;
			}
			ids.add(id);
		}
		return ids;
	}

}
