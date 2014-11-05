package net.pterodactylus.sone.database.memory;

import static java.util.logging.Level.WARNING;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import net.pterodactylus.util.config.Configuration;
import net.pterodactylus.util.config.ConfigurationException;

/**
 * Helper class for interacting with a {@link Configuration}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ConfigurationLoader {

	private static final Logger logger =
			Logger.getLogger("Sone.Database.Memory.Configuration");
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

	public synchronized void saveBookmarkedPosts(
			Set<String> bookmarkedPosts) {
		saveIds("Bookmarks/Post", bookmarkedPosts);
	}

	private void saveIds(String prefix, Set<String> bookmarkedPosts) {
		try {
			int idCounter = 0;
			for (String id : bookmarkedPosts) {
				configuration
						.getStringValue(prefix + "/" + idCounter++ + "/ID")
						.setValue(id);
			}
			configuration
					.getStringValue(prefix + "/" + idCounter + "/ID")
					.setValue(null);
		} catch (ConfigurationException ce1) {
			logger.log(WARNING, "Could not save bookmarked posts!", ce1);
		}
	}

}
