/*
 * Sone - AbstractPostBuilder.java - Copyright © 2013–2016 David Roden
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

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.database.PostBuilder;
import net.pterodactylus.sone.database.SoneProvider;

/**
 * Abstract {@link PostBuilder} implementation. It stores the state of the new
 * post and performs validation, you only need to implement {@link #build()}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public abstract class AbstractPostBuilder implements PostBuilder {

	/** The Sone provider for the created posts. */
	protected final SoneProvider soneProvider;

	/** Wether to create a post with a random ID. */
	protected boolean randomId;

	/** The ID of the post. */
	protected String id;

	/** The sender of the post. */
	protected String senderId;

	/** Whether to use the current time when creating the post. */
	protected boolean currentTime;

	/** The time of the post. */
	protected long time;

	/** The text of the post. */
	protected String text;

	/** The (optional) recipient of the post. */
	protected String recipientId;

	/**
	 * Creates a new abstract post builder.
	 *
	 * @param soneProvider
	 *            The Sone provider
	 */
	public AbstractPostBuilder(SoneProvider soneProvider) {
		this.soneProvider = soneProvider;
	}

	//
	// POSTBUILDER METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PostBuilder copyPost(Post post) {
		this.randomId = false;
		this.id = post.getId();
		this.senderId = post.getSone().getId();
		this.currentTime = false;
		this.time = post.getTime();
		this.text = post.getText();
		this.recipientId = post.getRecipientId().orNull();
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PostBuilder randomId() {
		randomId = true;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PostBuilder withId(String id) {
		this.id = id;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PostBuilder from(String senderId) {
		this.senderId = senderId;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PostBuilder currentTime() {
		currentTime = true;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PostBuilder withTime(long time) {
		this.time = time;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PostBuilder withText(String text) {
		this.text = text;
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PostBuilder to(String recipientId) {
		this.recipientId = recipientId;
		return this;
	}

	//
	// PROTECTED METHODS
	//

	/**
	 * Validates the state of this post builder.
	 *
	 * @throws IllegalStateException
	 *             if the state is not valid for building a new post
	 */
	protected void validate() throws IllegalStateException {
		checkState((randomId && (id == null)) || (!randomId && (id != null)), "exactly one of random ID or custom ID must be set");
		checkState(senderId != null, "sender must not be null");
		checkState((currentTime && (time == 0)) || (!currentTime && (time > 0)), "one of current time or custom time must be set");
		checkState((text != null) && !text.trim().isEmpty(), "text must not be empty");
		checkState((recipientId == null) || !recipientId.equals(senderId), "sender and recipient must not be the same");
	}

}
