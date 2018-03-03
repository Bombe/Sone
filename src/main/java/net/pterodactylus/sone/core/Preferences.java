/*
 * Sone - Preferences.java - Copyright © 2013–2016 David Roden
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

import static com.google.common.base.Predicates.equalTo;
import static java.lang.Integer.MAX_VALUE;
import static net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.ALWAYS;
import static net.pterodactylus.sone.utils.IntegerRangePredicate.range;

import net.pterodactylus.sone.core.event.InsertionDelayChangedEvent;
import net.pterodactylus.sone.fcp.FcpInterface;
import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired;
import net.pterodactylus.sone.fcp.event.FcpInterfaceActivatedEvent;
import net.pterodactylus.sone.fcp.event.FcpInterfaceDeactivatedEvent;
import net.pterodactylus.sone.fcp.event.FullAccessRequiredChanged;
import net.pterodactylus.sone.utils.DefaultOption;
import net.pterodactylus.sone.utils.Option;
import net.pterodactylus.util.config.Configuration;
import net.pterodactylus.util.config.ConfigurationException;

import com.google.common.base.Predicates;
import com.google.common.eventbus.EventBus;

/**
 * Convenience interface for external classes that want to access the core’s
 * configuration.
 */
public class Preferences {

	private final EventBus eventBus;
	private final Option<Integer> insertionDelay =
			new DefaultOption<Integer>(60, range(0, MAX_VALUE));
	private final Option<Integer> postsPerPage =
			new DefaultOption<Integer>(10, range(1, MAX_VALUE));
	private final Option<Integer> imagesPerPage =
			new DefaultOption<Integer>(9, range(1, MAX_VALUE));
	private final Option<Integer> charactersPerPost =
			new DefaultOption<Integer>(400, Predicates.<Integer>or(
					range(50, MAX_VALUE), equalTo(-1)));
	private final Option<Integer> postCutOffLength =
			new DefaultOption<Integer>(200, range(50, MAX_VALUE));
	private final Option<Boolean> requireFullAccess =
			new DefaultOption<Boolean>(false);
	private final Option<Integer> positiveTrust =
			new DefaultOption<Integer>(75, range(0, 100));
	private final Option<Integer> negativeTrust =
			new DefaultOption<Integer>(-25, range(-100, 100));
	private final Option<String> trustComment =
			new DefaultOption<String>("Set from Sone Web Interface");
	private final Option<Boolean> activateFcpInterface =
			new DefaultOption<Boolean>(false);
	private final Option<FullAccessRequired> fcpFullAccessRequired =
			new DefaultOption<FullAccessRequired>(ALWAYS);

	public Preferences(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	/**
	 * Returns the insertion delay.
	 *
	 * @return The insertion delay
	 */
	public int getInsertionDelay() {
		return insertionDelay.get();
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
		return this.insertionDelay.validate(insertionDelay);
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
		this.insertionDelay.set(insertionDelay);
		eventBus.post(new InsertionDelayChangedEvent(getInsertionDelay()));
		return this;
	}

	/**
	 * Returns the number of posts to show per page.
	 *
	 * @return The number of posts to show per page
	 */
	public int getPostsPerPage() {
		return postsPerPage.get();
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
		return this.postsPerPage.validate(postsPerPage);
	}

	/**
	 * Sets the number of posts to show per page.
	 *
	 * @param postsPerPage
	 *            The number of posts to show per page
	 * @return This preferences object
	 */
	public Preferences setPostsPerPage(Integer postsPerPage) {
		this.postsPerPage.set(postsPerPage);
		return this;
	}

	/**
	 * Returns the number of images to show per page.
	 *
	 * @return The number of images to show per page
	 */
	public int getImagesPerPage() {
		return imagesPerPage.get();
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
		return this.imagesPerPage.validate(imagesPerPage);
	}

	/**
	 * Sets the number of images per page.
	 *
	 * @param imagesPerPage
	 *            The number of images per page
	 * @return This preferences object
	 */
	public Preferences setImagesPerPage(Integer imagesPerPage) {
		this.imagesPerPage.set(imagesPerPage);
		return this;
	}

	/**
	 * Returns the number of characters per post, or <code>-1</code> if the
	 * posts should not be cut off.
	 *
	 * @return The numbers of characters per post
	 */
	public int getCharactersPerPost() {
		return charactersPerPost.get();
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
		return this.charactersPerPost.validate(charactersPerPost);
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
		this.charactersPerPost.set(charactersPerPost);
		return this;
	}

	/**
	 * Returns the number of characters the shortened post should have.
	 *
	 * @return The number of characters of the snippet
	 */
	public int getPostCutOffLength() {
		return postCutOffLength.get();
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
		return this.postCutOffLength.validate(postCutOffLength);
	}

	/**
	 * Sets the number of characters the shortened post should have.
	 *
	 * @param postCutOffLength
	 *            The number of characters of the snippet
	 * @return This preferences
	 */
	public Preferences setPostCutOffLength(Integer postCutOffLength) {
		this.postCutOffLength.set(postCutOffLength);
		return this;
	}

	/**
	 * Returns whether Sone requires full access to be even visible.
	 *
	 * @return {@code true} if Sone requires full access, {@code false}
	 *         otherwise
	 */
	public boolean isRequireFullAccess() {
		return requireFullAccess.get();
	}

	/**
	 * Sets whether Sone requires full access to be even visible.
	 *
	 * @param requireFullAccess
	 *            {@code true} if Sone requires full access, {@code false}
	 *            otherwise
	 */
	public void setRequireFullAccess(Boolean requireFullAccess) {
		this.requireFullAccess.set(requireFullAccess);
	}

	/**
	 * Returns the positive trust.
	 *
	 * @return The positive trust
	 */
	public int getPositiveTrust() {
		return positiveTrust.get();
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
		return this.positiveTrust.validate(positiveTrust);
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
		this.positiveTrust.set(positiveTrust);
		return this;
	}

	/**
	 * Returns the negative trust.
	 *
	 * @return The negative trust
	 */
	public int getNegativeTrust() {
		return negativeTrust.get();
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
		return this.negativeTrust.validate(negativeTrust);
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
		this.negativeTrust.set(negativeTrust);
		return this;
	}

	/**
	 * Returns the trust comment. This is the comment that is set in the web
	 * of trust when a trust value is assigned to an identity.
	 *
	 * @return The trust comment
	 */
	public String getTrustComment() {
		return trustComment.get();
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
		this.trustComment.set(trustComment);
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
		return activateFcpInterface.get();
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
	public Preferences setFcpInterfaceActive(Boolean fcpInterfaceActive) {
		this.activateFcpInterface.set(fcpInterfaceActive);
		if (isFcpInterfaceActive()) {
			eventBus.post(new FcpInterfaceActivatedEvent());
		} else {
			eventBus.post(new FcpInterfaceDeactivatedEvent());
		}
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
		return fcpFullAccessRequired.get();
	}

	/**
	 * Sets the action level for which full access to the FCP interface is
	 * required
	 *
	 * @param fcpFullAccessRequired
	 *            The action level
	 * @return This preferences
	 */
	public Preferences setFcpFullAccessRequired(
			FullAccessRequired fcpFullAccessRequired) {
		this.fcpFullAccessRequired.set(fcpFullAccessRequired);
		eventBus.post(new FullAccessRequiredChanged(getFcpFullAccessRequired()));
		return this;
	}

	public void saveTo(Configuration configuration) throws ConfigurationException {
		configuration.getIntValue("Option/ConfigurationVersion").setValue(0);
		configuration.getIntValue("Option/InsertionDelay").setValue(insertionDelay.getReal());
		configuration.getIntValue("Option/PostsPerPage").setValue(postsPerPage.getReal());
		configuration.getIntValue("Option/ImagesPerPage").setValue(imagesPerPage.getReal());
		configuration.getIntValue("Option/CharactersPerPost").setValue(charactersPerPost.getReal());
		configuration.getIntValue("Option/PostCutOffLength").setValue(postCutOffLength.getReal());
		configuration.getBooleanValue("Option/RequireFullAccess").setValue(requireFullAccess.getReal());
		configuration.getIntValue("Option/PositiveTrust").setValue(positiveTrust.getReal());
		configuration.getIntValue("Option/NegativeTrust").setValue(negativeTrust.getReal());
		configuration.getStringValue("Option/TrustComment").setValue(trustComment.getReal());
		configuration.getBooleanValue("Option/ActivateFcpInterface").setValue(activateFcpInterface.getReal());
		configuration.getIntValue("Option/FcpFullAccessRequired").setValue(toInt(fcpFullAccessRequired.getReal()));
	}

	private Integer toInt(FullAccessRequired fullAccessRequired) {
		return (fullAccessRequired == null) ? null : fullAccessRequired.ordinal();
	}

}
