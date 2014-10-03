package net.pterodactylus.sone.database;

import java.util.Set;

import net.pterodactylus.sone.data.Post;

/**
 * Database interface for bookmark-related functionality.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface BookmarkDatabase {

	void bookmarkPost(String postId); // FIXME – remove me
	void bookmarkPost(Post post);
	void unbookmarkPost(Post post);
	boolean isPostBookmarked(Post post);
	Set<Post> getBookmarkedPosts();

}
