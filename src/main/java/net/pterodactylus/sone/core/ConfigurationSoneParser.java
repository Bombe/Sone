package net.pterodactylus.sone.core;

import static java.util.Collections.unmodifiableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.database.AlbumBuilderFactory;
import net.pterodactylus.sone.database.ImageBuilderFactory;
import net.pterodactylus.sone.database.PostBuilder;
import net.pterodactylus.sone.database.PostBuilderFactory;
import net.pterodactylus.sone.database.PostReplyBuilder;
import net.pterodactylus.sone.database.PostReplyBuilderFactory;
import net.pterodactylus.util.config.Configuration;

/**
 * Parses a {@link Sone}’s data from a {@link Configuration}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ConfigurationSoneParser {

	private final Configuration configuration;
	private final Sone sone;
	private final String sonePrefix;
	private final Map<String, Album> albums = new HashMap<String, Album>();
	private final List<Album> topLevelAlbums = new ArrayList<Album>();
	private final Map<String, Image> images = new HashMap<String, Image>();

	public ConfigurationSoneParser(Configuration configuration, Sone sone) {
		this.configuration = configuration;
		this.sone = sone;
		sonePrefix = "Sone/" + sone.getId();
	}

	public Profile parseProfile() {
		Profile profile = new Profile(sone);
		profile.setFirstName(getString("/Profile/FirstName", null));
		profile.setMiddleName(getString("/Profile/MiddleName", null));
		profile.setLastName(getString("/Profile/LastName", null));
		profile.setBirthDay(getInt("/Profile/BirthDay", null));
		profile.setBirthMonth(getInt("/Profile/BirthMonth", null));
		profile.setBirthYear(getInt("/Profile/BirthYear", null));

		/* load profile fields. */
		int fieldCount = 0;
		while (true) {
			String fieldPrefix = "/Profile/Fields/" + fieldCount++;
			String fieldName = getString(fieldPrefix + "/Name", null);
			if (fieldName == null) {
				break;
			}
			String fieldValue = getString(fieldPrefix + "/Value", "");
			profile.addField(fieldName).setValue(fieldValue);
		}

		return profile;
	}

	private String getString(String nodeName, @Nullable String defaultValue) {
		return configuration.getStringValue(sonePrefix + nodeName)
				.getValue(defaultValue);
	}

	private Integer getInt(String nodeName, @Nullable Integer defaultValue) {
		return configuration.getIntValue(sonePrefix + nodeName)
				.getValue(defaultValue);
	}

	private Long getLong(String nodeName, @Nullable Long defaultValue) {
		return configuration.getLongValue(sonePrefix + nodeName)
				.getValue(defaultValue);
	}

	public Set<Post> parsePosts(PostBuilderFactory postBuilderFactory)
	throws InvalidPostFound {
		Set<Post> posts = new HashSet<Post>();
		while (true) {
			String postPrefix = "/Posts/" + posts.size();
			String postId = getString(postPrefix + "/ID", null);
			if (postId == null) {
				break;
			}
			long postTime = getLong(postPrefix + "/Time", 0L);
			String postText = getString(postPrefix + "/Text", null);
			if (postAttributesAreInvalid(postTime, postText)) {
				throw new InvalidPostFound();
			}
			PostBuilder postBuilder = postBuilderFactory.newPostBuilder()
					.withId(postId)
					.from(sone.getId())
					.withTime(postTime)
					.withText(postText);
			String postRecipientId =
					getString(postPrefix + "/Recipient", null);
			if (postRecipientIsValid(postRecipientId)) {
				postBuilder.to(postRecipientId);
			}
			posts.add(postBuilder.build());
		}
		return posts;
	}

	private boolean postAttributesAreInvalid(long postTime, String postText) {
		return (postTime == 0) || (postText == null);
	}

	private boolean postRecipientIsValid(String postRecipientId) {
		return (postRecipientId != null) && (postRecipientId.length() == 43);
	}

	public Set<PostReply> parsePostReplies(
			PostReplyBuilderFactory postReplyBuilderFactory) {
		Set<PostReply> replies = new HashSet<PostReply>();
		while (true) {
			String replyPrefix = "/Replies/" + replies.size();
			String replyId = getString(replyPrefix + "/ID", null);
			if (replyId == null) {
				break;
			}
			String postId = getString(replyPrefix + "/Post/ID", null);
			long replyTime = getLong(replyPrefix + "/Time", 0L);
			String replyText = getString(replyPrefix + "/Text", null);
			if ((postId == null) || (replyTime == 0) || (replyText == null)) {
				throw new InvalidPostReplyFound();
			}
			PostReplyBuilder postReplyBuilder = postReplyBuilderFactory
					.newPostReplyBuilder()
					.withId(replyId)
					.from(sone.getId())
					.to(postId)
					.withTime(replyTime)
					.withText(replyText);
			replies.add(postReplyBuilder.build());
		}
		return replies;
	}

	public Set<String> parseLikedPostIds() {
		Set<String> likedPostIds = new HashSet<String>();
		while (true) {
			String likedPostId =
					getString("/Likes/Post/" + likedPostIds.size() + "/ID",
							null);
			if (likedPostId == null) {
				break;
			}
			likedPostIds.add(likedPostId);
		}
		return likedPostIds;
	}

	public Set<String> parseLikedPostReplyIds() {
		Set<String> likedPostReplyIds = new HashSet<String>();
		while (true) {
			String likedReplyId = getString(
					"/Likes/Reply/" + likedPostReplyIds.size() + "/ID", null);
			if (likedReplyId == null) {
				break;
			}
			likedPostReplyIds.add(likedReplyId);
		}
		return likedPostReplyIds;
	}

	public Set<String> parseFriends() {
		Set<String> friends = new HashSet<String>();
		while (true) {
			String friendId =
					getString("/Friends/" + friends.size() + "/ID", null);
			if (friendId == null) {
				break;
			}
			friends.add(friendId);
		}
		return friends;
	}

	public List<Album> parseTopLevelAlbums(
			AlbumBuilderFactory albumBuilderFactory) {
		int albumCounter = 0;
		while (true) {
			String albumPrefix = "/Albums/" + albumCounter++;
			String albumId = getString(albumPrefix + "/ID", null);
			if (albumId == null) {
				break;
			}
			String albumTitle = getString(albumPrefix + "/Title", null);
			String albumDescription =
					getString(albumPrefix + "/Description", null);
			String albumParentId = getString(albumPrefix + "/Parent", null);
			String albumImageId =
					getString(albumPrefix + "/AlbumImage", null);
			if ((albumTitle == null) || (albumDescription == null)) {
				throw new InvalidAlbumFound();
			}
			Album album = albumBuilderFactory.newAlbumBuilder()
					.withId(albumId)
					.by(sone)
					.build()
					.modify()
					.setTitle(albumTitle)
					.setDescription(albumDescription)
					.setAlbumImage(albumImageId)
					.update();
			if (albumParentId != null) {
				Album parentAlbum = albums.get(albumParentId);
				if (parentAlbum == null) {
					throw new InvalidParentAlbumFound(albumParentId);
				}
				parentAlbum.addAlbum(album);
			} else {
				topLevelAlbums.add(album);
			}
			albums.put(albumId, album);
		}
		return topLevelAlbums;
	}

	public Map<String, Album> getAlbums() {
		return unmodifiableMap(albums);
	}

	public void parseImages(ImageBuilderFactory imageBuilderFactory) {
		int imageCounter = 0;
		while (true) {
			String imagePrefix = "/Images/" + imageCounter++;
			String imageId = getString(imagePrefix + "/ID", null);
			if (imageId == null) {
				break;
			}
			String albumId = getString(imagePrefix + "/Album", null);
			String key = getString(imagePrefix + "/Key", null);
			String title = getString(imagePrefix + "/Title", null);
			String description =
					getString(imagePrefix + "/Description", null);
			Long creationTime = getLong(imagePrefix + "/CreationTime", null);
			Integer width = getInt(imagePrefix + "/Width", null);
			Integer height = getInt(imagePrefix + "/Height", null);
			if (albumAttributesAreInvalid(albumId, key, title, description,
					creationTime,
					width, height)) {
				throw new InvalidImageFound();
			}
			Album album = albums.get(albumId);
			if (album == null) {
				throw new InvalidParentAlbumFound(albumId);
			}
			Image image = imageBuilderFactory.newImageBuilder()
					.withId(imageId)
					.build()
					.modify()
					.setSone(sone)
					.setCreationTime(creationTime)
					.setKey(key)
					.setTitle(title)
					.setDescription(description)
					.setWidth(width)
					.setHeight(height)
					.update();
			album.addImage(image);
			images.put(image.getId(), image);
		}
	}

	public Map<String, Image> getImages() {
		return images;
	}

	private boolean albumAttributesAreInvalid(String albumId, String key,
			String title, String description, Long creationTime,
			Integer width, Integer height) {
		return (albumId == null) || (key == null) || (title == null) || (
				description == null) || (creationTime == null) || (width
				== null) || (height == null);
	}

	public static class InvalidPostFound extends RuntimeException { }

	public static class InvalidPostReplyFound extends RuntimeException { }

	public static class InvalidAlbumFound extends RuntimeException { }

	public static class InvalidParentAlbumFound extends RuntimeException {

		private final String albumParentId;

		public InvalidParentAlbumFound(String albumParentId) {
			this.albumParentId = albumParentId;
		}

		public String getAlbumParentId() {
			return albumParentId;
		}

	}

	public static class InvalidImageFound extends RuntimeException { }

}
