package net.pterodactylus.sone.database.memory;

import static java.util.logging.Level.WARNING;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.pterodactylus.util.config.Configuration;
import net.pterodactylus.util.config.ConfigurationException;

/**
 * Helper class for interacting with a {@link Configuration}.
 */
public class ConfigurationLoader {

	private static final Logger logger = Logger.getLogger(ConfigurationLoader.class.getName());
	private final Configuration configuration;

	public ConfigurationLoader(Configuration configuration) {
		this.configuration = configuration;
	}

	public synchronized Set<String> loadFriends(String localSoneId) {
		return loadIds("Sone/" + localSoneId + "/Friends");
	}

	public void saveFriends(String soneId, Collection<String> friends) {
		saveIds("Sone/" + soneId + "/Friends", friends);
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

	@Nullable
	public synchronized Long getSoneFollowingTime(@Nonnull String soneId) {
		return loadSoneFollowingTimes().get(soneId);
	}

	public synchronized void removeSoneFollowingTime(@Nonnull String soneId) {
		Map<String, Long> soneFollowingTimes = loadSoneFollowingTimes();
		soneFollowingTimes.remove(soneId);
		storeSoneFollowingTimes(soneFollowingTimes);
	}

	public synchronized void setSoneFollowingTime(@Nonnull String soneId, long time) {
		Map<String, Long> soneFollowingTimes = loadSoneFollowingTimes();
		soneFollowingTimes.put(soneId, time);
		storeSoneFollowingTimes(soneFollowingTimes);
	}

	private synchronized Map<String, Long> loadSoneFollowingTimes() {
		Map<String, Long> soneFollowingTimes = new HashMap<>();
		int soneCounter = 0;
		while (true) {
			String soneId = configuration.getStringValue("SoneFollowingTimes/" + soneCounter + "/Sone").getValue(null);
			if (soneId == null) {
				break;
			}
			soneFollowingTimes.put(soneId, configuration.getLongValue("SoneFollowingTimes/" + soneCounter++ + "/Time").getValue(null));
		}
		return soneFollowingTimes;
	}

	private synchronized void storeSoneFollowingTimes(Map<String, Long> soneFollowingTimes) {
		int soneCounter = 0;
		try {
			for (Entry<String, Long> soneFollowingTime : soneFollowingTimes.entrySet()) {
				configuration.getStringValue("SoneFollowingTimes/" + soneCounter + "/Sone").setValue(soneFollowingTime.getKey());
				configuration.getLongValue("SoneFollowingTimes/" + soneCounter + "/Time").setValue(soneFollowingTime.getValue());
				++soneCounter;
			}
			configuration.getStringValue("SoneFollowingTimes/" + soneCounter + "/Sone").setValue(null);
		} catch (ConfigurationException ce1) {
			logger.log(WARNING, "Could not save Sone following times!", ce1);
		}
	}

	private Set<String> loadIds(String prefix) {
		Set<String> ids = new HashSet<>();
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

	private void saveIds(String prefix, Collection<String> ids) {
		try {
			int idCounter = 0;
			for (String id : ids) {
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
