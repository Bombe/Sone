/*
 * Sone - Preferences.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sone.core;

import net.pterodactylus.sone.core.event.InsertionDelayChangedEvent;
import net.pterodactylus.sone.fcp.FcpInterface;
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired;

import com.google.common.eventbus.EventBus;

/**
 * Convenience interface for external classes that want to access the core’s
 * configuration.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Preferences {

	private final EventBus eventBus;
	private final Options options;

	public Preferences(EventBus eventBus, Options options) {
		this.eventBus = eventBus;
		this.options = options;
	}

	/**
	 * Returns the insertion delay.
	 *
	 * @return The insertion delay
	 */
	public int getInsertionDelay() {
		return options.getIntegerOption("InsertionDelay").get();
	}

	/**
	 * Validates the given insertion delay.
	 *
	 * @param insertionDelay
	 *            The insertion delay to validate
	 * @return {@code true} if the given insertion delay was valid,
	 *         {@code false} otherwise
	 */
	public boolean validateInsertionDelay(Integer insertionDelay) {
		return options.getIntegerOption("InsertionDelay").validate(insertionDelay);
	}

	/**
	 * Sets the insertion delay
	 *
	 * @param insertionDelay
	 *            The new insertion delay, or {@code null} to restore it to
	 *            the default value
	 * @return This preferences
	 */
	public Preferences setInsertionDelay(Integer insertionDelay) {
		options.getIntegerOption("InsertionDelay").set(insertionDelay);
		eventBus.post(new InsertionDelayChangedEvent(getInsertionDelay()));
		return this;
	}

	/**
	 * Returns the number of posts to show per page.
	 *
	 * @return The number of posts to show per page
	 */
	public int getPostsPerPage() {
		return options.getIntegerOption("PostsPerPage").get();
	}

	/**
	 * Validates the number of posts per page.
	 *
	 * @param postsPerPage
	 *            The number of posts per page
	 * @return {@code true} if the number of posts per page was valid,
	 *         {@code false} otherwise
	 */
	public boolean validatePostsPerPage(Integer postsPerPage) {
		return options.getIntegerOption("PostsPerPage").validate(postsPerPage);
	}

	/**
	 * Sets the number of posts to show per page.
	 *
	 * @param postsPerPage
	 *            The number of posts to show per page
	 * @return This preferences object
	 */
	public Preferences setPostsPerPage(Integer postsPerPage) {
		options.getIntegerOption("PostsPerPage").set(postsPerPage);
		return this;
	}

	/**
	 * Returns the number of images to show per page.
	 *
	 * @return The number of images to show per page
	 */
	public int getImagesPerPage() {
		return options.getIntegerOption("ImagesPerPage").get();
	}

	/**
	 * Validates the number of images per page.
	 *
	 * @param imagesPerPage
	 *            The number of images per page
	 * @return {@code true} if the number of images per page was valid,
	 *         {@code false} otherwise
	 */
	public boolean validateImagesPerPage(Integer imagesPerPage) {
		return options.getIntegerOption("ImagesPerPage").validate(imagesPerPage);
	}

	/**
	 * Sets the number of images per page.
	 *
	 * @param imagesPerPage
	 *            The number of images per page
	 * @return This preferences object
	 */
	public Preferences setImagesPerPage(Integer imagesPerPage) {
		options.getIntegerOption("ImagesPerPage").set(imagesPerPage);
		return this;
	}

	/**
	 * Returns the number of characters per post, or <code>-1</code> if the
	 * posts should not be cut off.
	 *
	 * @return The numbers of characters per post
	 */
	public int getCharactersPerPost() {
		return options.getIntegerOption("CharactersPerPost").get();
	}

	/**
	 * Validates the number of characters per post.
	 *
	 * @param charactersPerPost
	 *            The number of characters per post
	 * @return {@code true} if the number of characters per post was valid,
	 *         {@code false} otherwise
	 */
	public boolean validateCharactersPerPost(Integer charactersPerPost) {
		return options.getIntegerOption("CharactersPerPost").validate(charactersPerPost);
	}

	/**
	 * Sets the number of characters per post.
	 *
	 * @param charactersPerPost
	 *            The number of characters per post, or <code>-1</code> to
	 *            not cut off the posts
	 * @return This preferences objects
	 */
	public Preferences setCharactersPerPost(Integer charactersPerPost) {
		options.getIntegerOption("CharactersPerPost").set(charactersPerPost);
		return this;
	}

	/**
	 * Returns the number of characters the shortened post should have.
	 *
	 * @return The number of characters of the snippet
	 */
	public int getPostCutOffLength() {
		return options.getIntegerOption("PostCutOffLength").get();
	}

	/**
	 * Validates the number of characters after which to cut off the post.
	 *
	 * @param postCutOffLength
	 *            The number of characters of the snippet
	 * @return {@code true} if the number of characters of the snippet is
	 *         valid, {@code false} otherwise
	 */
	public boolean validatePostCutOffLength(Integer postCutOffLength) {
		return options.getIntegerOption("PostCutOffLength").validate(postCutOffLength);
	}

	/**
	 * Sets the number of characters the shortened post should have.
	 *
	 * @param postCutOffLength
	 *            The number of characters of the snippet
	 * @return This preferences
	 */
	public Preferences setPostCutOffLength(Integer postCutOffLength) {
		options.getIntegerOption("PostCutOffLength").set(postCutOffLength);
		return this;
	}

	/**
	 * Returns whether Sone requires full access to be even visible.
	 *
	 * @return {@code true} if Sone requires full access, {@code false}
	 *         otherwise
	 */
	public boolean isRequireFullAccess() {
		return options.getBooleanOption("RequireFullAccess").get();
	}

	/**
	 * Sets whether Sone requires full access to be even visible.
	 *
	 * @param requireFullAccess
	 *            {@code true} if Sone requires full access, {@code false}
	 *            otherwise
	 */
	public void setRequireFullAccess(Boolean requireFullAccess) {
		options.getBooleanOption("RequireFullAccess").set(requireFullAccess);
	}

	/**
	 * Returns the positive trust.
	 *
	 * @return The positive trust
	 */
	public int getPositiveTrust() {
		return options.getIntegerOption("PositiveTrust").get();
	}

	/**
	 * Validates the positive trust.
	 *
	 * @param positiveTrust
	 *            The positive trust to validate
	 * @return {@code true} if the positive trust was valid, {@code false}
	 *         otherwise
	 */
	public boolean validatePositiveTrust(Integer positiveTrust) {
		return options.getIntegerOption("PositiveTrust").validate(positiveTrust);
	}

	/**
	 * Sets the positive trust.
	 *
	 * @param positiveTrust
	 *            The new positive trust, or {@code null} to restore it to
	 *            the default vlaue
	 * @return This preferences
	 */
	public Preferences setPositiveTrust(Integer positiveTrust) {
		options.getIntegerOption("PositiveTrust").set(positiveTrust);
		return this;
	}

	/**
	 * Returns the negative trust.
	 *
	 * @return The negative trust
	 */
	public int getNegativeTrust() {
		return options.getIntegerOption("NegativeTrust").get();
	}

	/**
	 * Validates the negative trust.
	 *
	 * @param negativeTrust
	 *            The negative trust to validate
	 * @return {@code true} if the negative trust was valid, {@code false}
	 *         otherwise
	 */
	public boolean validateNegativeTrust(Integer negativeTrust) {
		return options.getIntegerOption("NegativeTrust").validate(negativeTrust);
	}

	/**
	 * Sets the negative trust.
	 *
	 * @param negativeTrust
	 *            The negative trust, or {@code null} to restore it to the
	 *            default value
	 * @return The preferences
	 */
	public Preferences setNegativeTrust(Integer negativeTrust) {
		options.getIntegerOption("NegativeTrust").set(negativeTrust);
		return this;
	}

	/**
	 * Returns the trust comment. This is the comment that is set in the web
	 * of trust when a trust value is assigned to an identity.
	 *
	 * @return The trust comment
	 */
	public String getTrustComment() {
		return options.getStringOption("TrustComment").get();
	}

	/**
	 * Sets the trust comment.
	 *
	 * @param trustComment
	 *            The trust comment, or {@code null} to restore it to the
	 *            default value
	 * @return This preferences
	 */
	public Preferences setTrustComment(String trustComment) {
		options.getStringOption("TrustComment").set(trustComment);
		return this;
	}

	/**
	 * Returns whether the {@link FcpInterface FCP interface} is currently
	 * active.
	 *
	 * @see FcpInterface#setActive(boolean)
	 * @return {@code true} if the FCP interface is currently active,
	 *         {@code false} otherwise
	 */
	public boolean isFcpInterfaceActive() {
		return options.getBooleanOption("ActivateFcpInterface").get();
	}

	/**
	 * Sets whether the {@link FcpInterface FCP interface} is currently
	 * active.
	 *
	 * @see FcpInterface#setActive(boolean)
	 * @param fcpInterfaceActive
	 *            {@code true} to activate the FCP interface, {@code false}
	 *            to deactivate the FCP interface
	 * @return This preferences object
	 */
	public Preferences setFcpInterfaceActive(boolean fcpInterfaceActive) {
		options.getBooleanOption("ActivateFcpInterface").set(fcpInterfaceActive);
		return this;
	}

	/**
	 * Returns the action level for which full access to the FCP interface
	 * is required.
	 *
	 * @return The action level for which full access to the FCP interface
	 *         is required
	 */
	public FullAccessRequired getFcpFullAccessRequired() {
		return FullAccessRequired.values()[options.getIntegerOption("FcpFullAccessRequired").get()];
	}

	/**
	 * Sets the action level for which full access to the FCP interface is
	 * required
	 *
	 * @param fcpFullAccessRequired
	 *            The action level
	 * @return This preferences
	 */
	public Preferences setFcpFullAccessRequired(FullAccessRequired fcpFullAccessRequired) {
		options.getIntegerOption("FcpFullAccessRequired").set((fcpFullAccessRequired != null) ? fcpFullAccessRequired.ordinal() : null);
		return this;
	}

}
