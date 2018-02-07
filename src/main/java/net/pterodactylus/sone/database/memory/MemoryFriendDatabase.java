package net.pterodactylus.sone.database.memory;

import java.util.Collection;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * In-memory implementation of friend-related functionality.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
class MemoryFriendDatabase {

	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final ConfigurationLoader configurationLoader;
	private final Multimap<String, String> soneFriends = HashMultimap.create();

	MemoryFriendDatabase(ConfigurationLoader configurationLoader) {
		this.configurationLoader = configurationLoader;
	}

	Collection<String> getFriends(String localSoneId) {
		loadFriends(localSoneId);
		lock.readLock().lock();
		try {
			return soneFriends.get(localSoneId);
		} finally {
			lock.readLock().unlock();
		}
	}

	boolean isFriend(String localSoneId, String friendSoneId) {
		loadFriends(localSoneId);
		lock.readLock().lock();
		try {
			return soneFriends.containsEntry(localSoneId, friendSoneId);
		} finally {
			lock.readLock().unlock();
		}
	}

	void addFriend(String localSoneId, String friendSoneId) {
		loadFriends(localSoneId);
		lock.writeLock().lock();
		try {
			if (soneFriends.put(localSoneId, friendSoneId)) {
				configurationLoader.saveFriends(localSoneId, soneFriends.get(localSoneId));
				if (configurationLoader.getSoneFollowingTime(friendSoneId) == null) {
					configurationLoader.setSoneFollowingTime(friendSoneId, System.currentTimeMillis());
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	void removeFriend(String localSoneId, String friendSoneId) {
		loadFriends(localSoneId);
		lock.writeLock().lock();
		try {
			if (soneFriends.remove(localSoneId, friendSoneId)) {
				configurationLoader.saveFriends(localSoneId, soneFriends.get(localSoneId));
				boolean unfollowedSoneStillFollowed = false;
				for (String soneId : soneFriends.keys()) {
					unfollowedSoneStillFollowed |= getFriends(soneId).contains(friendSoneId);
				}
				if (!unfollowedSoneStillFollowed) {
					configurationLoader.removeSoneFollowingTime(friendSoneId);
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Nullable
	Long getFollowingTime(@Nonnull String soneId) {
		return configurationLoader.getSoneFollowingTime(soneId);
	}

	private void loadFriends(String localSoneId) {
		lock.writeLock().lock();
		try {
			if (soneFriends.containsKey(localSoneId)) {
				return;
			}
			soneFriends.putAll(localSoneId, configurationLoader.loadFriends(localSoneId));
		} finally {
			lock.writeLock().unlock();
		}
	}

}
