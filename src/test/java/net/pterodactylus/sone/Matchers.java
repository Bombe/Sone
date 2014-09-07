/*
 * Sone - Matchers.java - Copyright © 2013 David Roden
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

import net.pterodactylus.sone.data.Post;

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

}
