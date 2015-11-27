/*
 * Sone - AbstractPostReplyBuilder.java - Copyright © 2013–2015 David Roden
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

package net.pterodactylus.sone.data.impl;

import static com.google.common.base.Preconditions.checkState;

import org.apache.commons.lang.StringUtils;

import net.pterodactylus.sone.database.PostReplyBuilder;

/**
 * Abstract {@link PostReplyBuilder} implementation. It stores the state of the
 * new post and performs validation, implementations only need to implement
 * {@link #build()}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public abstract class AbstractPostReplyBuilder extends AbstractReplyBuilder<PostReplyBuilder> implements PostReplyBuilder {

	/** The ID of the post the created reply refers to. */
	protected String postId;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PostReplyBuilder to(String postId) {
		this.postId = postId;
		return this;
	}

	//
	// PROTECTED METHODS
	//

	/**
	 * Validates the state of this post reply builder.
	 *
	 * @throws IllegalStateException
	 *             if the state is not valid for building a new post reply
	 */
	protected void validate() throws IllegalStateException {
		checkState((randomId && (id == null)) || (!randomId && (id != null)), "either random ID nor custom ID must be set");
		checkState(senderId != null, "sender must not be null");
		checkState((currentTime && (time == 0)) || (!currentTime && (time >= 0)), "either current time or custom time must be set");
		checkState(!StringUtils.isBlank(text), "text must not be empty");
		checkState(postId != null, "post must not be null");
	}

}
