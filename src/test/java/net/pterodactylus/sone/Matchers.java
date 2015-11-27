/*
 * Sone - Matchers.java - Copyright © 2013–2015 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.sone;

import static java.util.regex.Pattern.compile;

import java.io.IOException;
import java.io.InputStream;

import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;

import com.google.common.base.Optional;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matchers used throughout the tests.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Matchers {

	public static Matcher<String> matchesRegex(final String regex) {
		return new TypeSafeMatcher<String>() {
			@Override
			protected boolean matchesSafely(String item) {
				return compile(regex).matcher(item).matches();
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("matches: ").appendValue(regex);
			}
		};
	}

	public static Matcher<InputStream> delivers(final byte[] data) {
		return new TypeSafeMatcher<InputStream>() {
			byte[] readData = new byte[data.length];

			@Override
			protected boolean matchesSafely(InputStream inputStream) {
				int offset = 0;
				try {
					while (true) {
						int r = inputStream.read();
						if (r == -1) {
							return offset == data.length;
						}
						if (offset == data.length) {
							return false;
						}
						if (data[offset] != (readData[offset] = (byte) r)) {
							return false;
						}
						offset++;
					}
				} catch (IOException ioe1) {
					return false;
				}
			}

			@Override
			public void describeTo(Description description) {
				description.appendValue(data);
			}

			@Override
			protected void describeMismatchSafely(InputStream item,
					Description mismatchDescription) {
				mismatchDescription.appendValue(readData);
			}
		};
	}

	public static Matcher<Post> isPost(String postId, long time,
			String text, Optional<String> recipient) {
		return new PostMatcher(postId, time, text, recipient);
	}

	public static Matcher<Post> isPostWithId(String postId) {
		return new PostIdMatcher(postId);
	}

	public static Matcher<PostReply> isPostReply(String postReplyId,
			String postId, long time, String text) {
		return new PostReplyMatcher(postReplyId, postId, time, text);
	}

	public static Matcher<Album> isAlbum(final String albumId,
			final String parentAlbumId,
			final String title, final String albumDescription,
			final String imageId) {
		return new TypeSafeDiagnosingMatcher<Album>() {
			@Override
			protected boolean matchesSafely(Album album,
					Description mismatchDescription) {
				if (!album.getId().equals(albumId)) {
					mismatchDescription.appendText("ID is ")
							.appendValue(album.getId());
					return false;
				}
				if (parentAlbumId == null) {
					if (album.getParent() != null) {
						mismatchDescription.appendText("has parent album");
						return false;
					}
				} else {
					if (album.getParent() == null) {
						mismatchDescription.appendText("has no parent album");
						return false;
					}
					if (!album.getParent().getId().equals(parentAlbumId)) {
						mismatchDescription.appendText("parent album is ")
								.appendValue(album.getParent().getId());
						return false;
					}
				}
				if (!title.equals(album.getTitle())) {
					mismatchDescription.appendText("has title ")
							.appendValue(album.getTitle());
					return false;
				}
				if (!albumDescription.equals(album.getDescription())) {
					mismatchDescription.appendText("has description ")
							.appendValue(album.getDescription());
					return false;
				}
				if (imageId == null) {
					if (album.getAlbumImage() != null) {
						mismatchDescription.appendText("has album image");
						return false;
					}
				} else {
					if (album.getAlbumImage() == null) {
						mismatchDescription.appendText("has no album image");
						return false;
					}
					if (!album.getAlbumImage().getId().equals(imageId)) {
						mismatchDescription.appendText("has album image ")
								.appendValue(album.getAlbumImage().getId());
						return false;
					}
				}
				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is album ").appendValue(albumId);
				if (parentAlbumId == null) {
					description.appendText(", has no parent");
				} else {
					description.appendText(", has parent ")
							.appendValue(parentAlbumId);
				}
				description.appendText(", has title ").appendValue(title);
				description.appendText(", has description ")
						.appendValue(albumDescription);
				if (imageId == null) {
					description.appendText(", has no album image");
				} else {
					description.appendText(", has album image ")
							.appendValue(imageId);
				}
			}
		};
	}

	public static Matcher<Image> isImage(final String id,
			final long creationTime,
			final String key, final String title,
			final String imageDescription,
			final int width, final int height) {
		return new TypeSafeDiagnosingMatcher<Image>() {
			@Override
			protected boolean matchesSafely(Image image,
					Description mismatchDescription) {
				if (!image.getId().equals(id)) {
					mismatchDescription.appendText("ID is ")
							.appendValue(image.getId());
					return false;
				}
				if (image.getCreationTime() != creationTime) {
					mismatchDescription.appendText("created at @")
							.appendValue(image.getCreationTime());
					return false;
				}
				if (!image.getKey().equals(key)) {
					mismatchDescription.appendText("key is ")
							.appendValue(image.getKey());
					return false;
				}
				if (!image.getTitle().equals(title)) {
					mismatchDescription.appendText("title is ")
							.appendValue(image.getTitle());
					return false;
				}
				if (!image.getDescription().equals(imageDescription)) {
					mismatchDescription.appendText("description is ")
							.appendValue(image.getDescription());
					return false;
				}
				if (image.getWidth() != width) {
					mismatchDescription.appendText("width is ")
							.appendValue(image.getWidth());
					return false;
				}
				if (image.getHeight() != height) {
					mismatchDescription.appendText("height is ")
							.appendValue(image.getHeight());
					return false;
				}
				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("image with ID ").appendValue(id);
				description.appendText(", created at @")
						.appendValue(creationTime);
				description.appendText(", has key ").appendValue(key);
				description.appendText(", has title ").appendValue(title);
				description.appendText(", has description ")
						.appendValue(imageDescription);
				description.appendText(", has width ").appendValue(width);
				description.appendText(", has height ").appendValue(height);
			}
		};
	}

	private static class PostMatcher extends TypeSafeDiagnosingMatcher<Post> {

		private final String postId;
		private final long time;
		private final String text;
		private final Optional<String> recipient;

		private PostMatcher(String postId, long time, String text,
				Optional<String> recipient) {
			this.postId = postId;
			this.time = time;
			this.text = text;
			this.recipient = recipient;
		}

		@Override
		protected boolean matchesSafely(Post post,
				Description mismatchDescription) {
			if (!post.getId().equals(postId)) {
				mismatchDescription.appendText("ID is not ")
						.appendValue(postId);
				return false;
			}
			if (post.getTime() != time) {
				mismatchDescription.appendText("Time is not @")
						.appendValue(time);
				return false;
			}
			if (!post.getText().equals(text)) {
				mismatchDescription.appendText("Text is not ")
						.appendValue(text);
				return false;
			}
			if (recipient.isPresent()) {
				if (!post.getRecipientId().isPresent()) {
					mismatchDescription.appendText(
							"Recipient not present");
					return false;
				}
				if (!post.getRecipientId().get().equals(recipient.get())) {
					mismatchDescription.appendText("Recipient is not ")
							.appendValue(recipient.get());
					return false;
				}
			} else {
				if (post.getRecipientId().isPresent()) {
					mismatchDescription.appendText("Recipient is present");
					return false;
				}
			}
			return true;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("is post with ID ")
					.appendValue(postId);
			description.appendText(", created at @").appendValue(time);
			description.appendText(", text ").appendValue(text);
			if (recipient.isPresent()) {
				description.appendText(", directed at ")
						.appendValue(recipient.get());
			}
		}

	}

	private static class PostIdMatcher extends TypeSafeDiagnosingMatcher<Post> {

		private final String id;

		private PostIdMatcher(String id) {
			this.id = id;
		}

		@Override
		protected boolean matchesSafely(Post item,
				Description mismatchDescription) {
			if (!item.getId().equals(id)) {
				mismatchDescription.appendText("post has ID ").appendValue(item.getId());
				return false;
			}
			return true;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("post with ID ").appendValue(id);
		}

	}

	private static class PostReplyMatcher
			extends TypeSafeDiagnosingMatcher<PostReply> {

		private final String postReplyId;
		private final String postId;
		private final long time;
		private final String text;

		private PostReplyMatcher(String postReplyId, String postId, long time,
				String text) {
			this.postReplyId = postReplyId;
			this.postId = postId;
			this.time = time;
			this.text = text;
		}

		@Override
		protected boolean matchesSafely(PostReply postReply,
				Description mismatchDescription) {
			if (!postReply.getId().equals(postReplyId)) {
				mismatchDescription.appendText("is post reply ")
						.appendValue(postReply.getId());
				return false;
			}
			if (!postReply.getPostId().equals(postId)) {
				mismatchDescription.appendText("is reply to ")
						.appendValue(postReply.getPostId());
				return false;
			}
			if (postReply.getTime() != time) {
				mismatchDescription.appendText("is created at @").appendValue(
						postReply.getTime());
				return false;
			}
			if (!postReply.getText().equals(text)) {
				mismatchDescription.appendText("says ")
						.appendValue(postReply.getText());
				return false;
			}
			return true;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("is post reply ").appendValue(postReplyId);
			description.appendText(", replies to post ").appendValue(postId);
			description.appendText(", is created at @").appendValue(time);
			description.appendText(", says ").appendValue(text);
		}

	}

}
