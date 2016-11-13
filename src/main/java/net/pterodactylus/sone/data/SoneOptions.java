package net.pterodactylus.sone.data;

import static net.pterodactylus.sone.data.SoneOptions.LoadExternalContent.NEVER;

/**
 * All Sone-specific options.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface SoneOptions {

	boolean isAutoFollow();
	void setAutoFollow(boolean autoFollow);

	boolean isSoneInsertNotificationEnabled();
	void setSoneInsertNotificationEnabled(boolean soneInsertNotificationEnabled);

	boolean isShowNewSoneNotifications();
	void setShowNewSoneNotifications(boolean showNewSoneNotifications);

	boolean isShowNewPostNotifications();
	void setShowNewPostNotifications(boolean showNewPostNotifications);

	boolean isShowNewReplyNotifications();
	void setShowNewReplyNotifications(boolean showNewReplyNotifications);

	LoadExternalContent getShowCustomAvatars();
	void setShowCustomAvatars(LoadExternalContent showCustomAvatars);

	/**
	 * Possible values for all options that are related to loading external content.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	enum LoadExternalContent {

		/** Never show custom avatars. */
		NEVER,

		/** Only show custom avatars of followed Sones. */
		FOLLOWED,

		/** Only show custom avatars of Sones you manually trust. */
		MANUALLY_TRUSTED,

		/** Only show custom avatars of automatically trusted Sones. */
		TRUSTED,

		/** Always show custom avatars. */
		ALWAYS,

	}

	/**
	 * {@link SoneOptions} implementation.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public class DefaultSoneOptions implements SoneOptions {

		private boolean autoFollow = false;
		private boolean soneInsertNotificationsEnabled = false;
		private boolean showNewSoneNotifications = true;
		private boolean showNewPostNotifications = true;
		private boolean showNewReplyNotifications = true;
		private LoadExternalContent showCustomAvatars = NEVER;

		@Override
		public boolean isAutoFollow() {
			return autoFollow;
		}

		@Override
		public void setAutoFollow(boolean autoFollow) {
			this.autoFollow = autoFollow;
		}

		@Override
		public boolean isSoneInsertNotificationEnabled() {
			return soneInsertNotificationsEnabled;
		}

		@Override
		public void setSoneInsertNotificationEnabled(boolean soneInsertNotificationEnabled) {
			this.soneInsertNotificationsEnabled = soneInsertNotificationEnabled;
		}

		@Override
		public boolean isShowNewSoneNotifications() {
			return showNewSoneNotifications;
		}

		@Override
		public void setShowNewSoneNotifications(boolean showNewSoneNotifications) {
			this.showNewSoneNotifications = showNewSoneNotifications;
		}

		@Override
		public boolean isShowNewPostNotifications() {
			return showNewPostNotifications;
		}

		@Override
		public void setShowNewPostNotifications(boolean showNewPostNotifications) {
			this.showNewPostNotifications = showNewPostNotifications;
		}

		@Override
		public boolean isShowNewReplyNotifications() {
			return showNewReplyNotifications;
		}

		@Override
		public void setShowNewReplyNotifications(boolean showNewReplyNotifications) {
			this.showNewReplyNotifications = showNewReplyNotifications;
		}

		@Override
		public LoadExternalContent getShowCustomAvatars() {
			return showCustomAvatars;
		}

		@Override
		public void setShowCustomAvatars(LoadExternalContent showCustomAvatars) {
			this.showCustomAvatars = showCustomAvatars;
		}

	}

}
