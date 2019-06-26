package net.pterodactylus.sone.web

import net.pterodactylus.sone.web.pages.*
import javax.inject.Inject

/**
 * Container for all web pages. This uses field injection because there are way too many pages
 * to sensibly use constructor injection.
 */
class AllPages {

	@Inject lateinit var aboutPage: AboutPage
	@Inject lateinit var bookmarkPage: BookmarkPage
	@Inject lateinit var bookmarksPage: BookmarksPage
	@Inject lateinit var createAlbumPage: CreateAlbumPage
	@Inject lateinit var createPostPage: CreatePostPage
	@Inject lateinit var createReplyPage: CreateReplyPage
	@Inject lateinit var createSonePage: CreateSonePage
	@Inject lateinit var deleteAlbumPage: DeleteAlbumPage
	@Inject lateinit var deleteImagePage: DeleteImagePage
	@Inject lateinit var deletePostPage: DeletePostPage
	@Inject lateinit var deleteProfileFieldPage: DeleteProfileFieldPage
	@Inject lateinit var deleteReplyPage: DeleteReplyPage
	@Inject lateinit var deleteSonePage: DeleteSonePage
	@Inject lateinit var dismissNotificationPage: DismissNotificationPage
	@Inject lateinit var distrustPage: DistrustPage
	@Inject lateinit var editAlbumPage: EditAlbumPage
	@Inject lateinit var editImagePage: EditImagePage
	@Inject lateinit var editProfileFieldPage: EditProfileFieldPage
	@Inject lateinit var editProfilePage: EditProfilePage
	@Inject lateinit var followSonePage: FollowSonePage
	@Inject lateinit var getImagePage: GetImagePage
	@Inject lateinit var imageBrowserPage: ImageBrowserPage
	@Inject lateinit var indexPage: IndexPage
	@Inject lateinit var knownSonesPage: KnownSonesPage
	@Inject lateinit var likePage: LikePage
	@Inject lateinit var lockSonePage: LockSonePage
	@Inject lateinit var loginPage: LoginPage
	@Inject lateinit var logoutPage: LogoutPage
	@Inject lateinit var markAsKnownPage: MarkAsKnownPage
	@Inject lateinit var newPage: NewPage
	@Inject lateinit var optionsPage: OptionsPage
	@Inject lateinit var rescuePage: RescuePage
	@Inject lateinit var searchPage: SearchPage
	@Inject lateinit var trustPage: TrustPage
	@Inject lateinit var unbookmarkPage: UnbookmarkPage
	@Inject lateinit var unfollowSonePage: UnfollowSonePage
	@Inject lateinit var unlikePage: UnlikePage
	@Inject lateinit var unlockSonePage: UnlockSonePage
	@Inject lateinit var untrustPage: UntrustPage
	@Inject lateinit var uploadImagePage: UploadImagePage
	@Inject lateinit var viewPostPage: ViewPostPage
	@Inject lateinit var viewSonePage: ViewSonePage

}
