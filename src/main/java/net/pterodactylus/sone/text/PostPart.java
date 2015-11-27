/*
 * Sone - PostPart.java - Copyright © 2011–2015 David Roden
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

package net.pterodactylus.sone.text;

import net.pterodactylus.sone.data.Post;

/**
 * {@link Part} implementation that stores a reference to a {@link Post}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PostPart implements Part {

	/** The post this part refers to. */
	private final Post post;

	/**
	 * Creates a new post part.
	 *
	 * @param post
	 *            The referenced post
	 */
	public PostPart(Post post) {
		this.post = post;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the post referenced by this part.
	 *
	 * @return The post referenced by this part
	 */
	public Post getPost() {
		return post;
	}

	//
	// PART METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getText() {
		return post.getText();
	}

}
