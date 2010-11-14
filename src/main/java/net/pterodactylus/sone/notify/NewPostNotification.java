/*
 * Sone - NewPostNotification.java - Copyright © 2010 David Roden
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

package net.pterodactylus.sone.notify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.util.notify.TemplateNotification;
import net.pterodactylus.util.template.Template;

/**
 * Notification that signals that new {@link Post}s have been discovered.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class NewPostNotification extends TemplateNotification {

	/** The new posts. */
	private List<Post> newPosts = Collections.synchronizedList(new ArrayList<Post>());

	/**
	 * Creates a new “new post discovered” notification.
	 *
	 * @param template
	 *            The template to render
	 */
	public NewPostNotification(Template template) {
		super(template);
		template.set("posts", newPosts);
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns whether there are any new posts.
	 *
	 * @return {@code true} if there are no new posts, {@code false} if there
	 *         are new posts
	 */
	public boolean isEmpty() {
		return newPosts.isEmpty();
	}

	/**
	 * Adds a discovered post.
	 *
	 * @param post
	 *            The new post
	 */
	public void addPost(Post post) {
		newPosts.add(post);
		touch();
	}

	/**
	 * Removes the given post from the list of new posts.
	 *
	 * @param post
	 *            The post to remove
	 */
	public void removePost(Post post) {
		newPosts.remove(post);
		touch();
	}

	//
	// ABSTRACTNOTIFICATION METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dismiss() {
		super.dismiss();
		newPosts.clear();
	}

}
