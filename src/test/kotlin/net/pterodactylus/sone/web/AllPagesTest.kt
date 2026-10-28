package net.pterodactylus.sone.web

import com.google.inject.Guice.createInjector
import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.core.FreenetInterface
import net.pterodactylus.sone.main.PluginHomepage
import net.pterodactylus.sone.main.PluginVersion
import net.pterodactylus.sone.main.PluginYear
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.web.page.*
import net.pterodactylus.sone.web.pages.*
import net.pterodactylus.util.template.Template
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Test

/**
 * Test for [AllPages].
 */
class AllPagesTest {

	private val injector by lazy {
		baseInjector.createChildInjector(
				PluginVersion::class.isProvidedByMock(),
				PluginYear::class.isProvidedByMock(),
				PluginHomepage::class.isProvidedByMock()
		)!!
	}
	private val allPages by lazy { injector.getInstance<AllPages>() }

	private inline fun <reified T> instanceOf(): Matcher<T> = Matchers.instanceOf<T>(T::class.java)

	@Test
	fun `about page can be injected`() {
		assertThat(allPages.aboutPage, instanceOf<AboutPage>())
	}

	@Test
	fun `bookmark page can be injected`() {
		assertThat(allPages.bookmarkPage, instanceOf<BookmarkPage>())
	}

	@Test
	fun `bookmarks page can be injected`() {
		assertThat(allPages.bookmarksPage, instanceOf<BookmarksPage>())
	}

	@Test
	fun `create album page can be injected`() {
		assertThat(allPages.createAlbumPage, instanceOf<CreateAlbumPage>())
	}

	@Test
	fun `create post page can be injected`() {
		assertThat(allPages.createPostPage, instanceOf<CreatePostPage>())
	}

	@Test
	fun `create reply page can be injected`() {
		assertThat(allPages.createReplyPage, instanceOf<CreateReplyPage>())
	}

	@Test
	fun `create sone page can be injected`() {
		assertThat(allPages.createSonePage, instanceOf<CreateSonePage>())
	}

	@Test
	fun `delete album page can be injected`() {
		assertThat(allPages.deleteAlbumPage, instanceOf<DeleteAlbumPage>())
	}

	@Test
	fun `delete image page can be injected`() {
		assertThat(allPages.deleteImagePage, instanceOf<DeleteImagePage>())
	}

	@Test
	fun `delete post page can be injected`() {
		assertThat(allPages.deletePostPage, instanceOf<DeletePostPage>())
	}

	@Test
	fun `delete profile field page can be injected`() {
		assertThat(allPages.deleteProfileFieldPage, instanceOf<DeleteProfileFieldPage>())
	}

	@Test
	fun `delete reply page can be injected`() {
		assertThat(allPages.deleteReplyPage, instanceOf<DeleteReplyPage>())
	}

	@Test
	fun `delete sone page can be injected`() {
		assertThat(allPages.deleteSonePage, instanceOf<DeleteSonePage>())
	}

	@Test
	fun `dismiss notification page can be injected`() {
		assertThat(allPages.dismissNotificationPage, instanceOf<DismissNotificationPage>())
	}

	@Test
	fun `edit album page can be injected`() {
		assertThat(allPages.editAlbumPage, instanceOf<EditAlbumPage>())
	}

	@Test
	fun `edit image page can be injected`() {
		assertThat(allPages.editImagePage, instanceOf<EditImagePage>())
	}

	@Test
	fun `edit profile field page can be injected`() {
		assertThat(allPages.editProfileFieldPage, instanceOf<EditProfileFieldPage>())
	}

	@Test
	fun `edit profile page can be injected`() {
		assertThat(allPages.editProfilePage, instanceOf<EditProfilePage>())
	}

	@Test
	fun `follow sone page can be injected`() {
		assertThat(allPages.followSonePage, instanceOf<FollowSonePage>())
	}

	@Test
	fun `get image page can be injected`() {
		assertThat(allPages.getImagePage, instanceOf<GetImagePage>())
	}

	@Test
	fun `image browser page can be injected`() {
		assertThat(allPages.imageBrowserPage, instanceOf<ImageBrowserPage>())
	}

	@Test
	fun `index page can be injected`() {
		assertThat(allPages.indexPage, instanceOf<IndexPage>())
	}

	@Test
	fun `known sones page can be injected`() {
		assertThat(allPages.knownSonesPage, instanceOf<KnownSonesPage>())
	}

	@Test
	fun `like page can be injected`() {
		assertThat(allPages.likePage, instanceOf<LikePage>())
	}

	@Test
	fun `lock sone page can be injected`() {
		assertThat(allPages.lockSonePage, instanceOf<LockSonePage>())
	}

	@Test
	fun `login page can be injected`() {
		assertThat(allPages.loginPage, instanceOf<LoginPage>())
	}

	@Test
	fun `logout page can be injected`() {
		assertThat(allPages.logoutPage, instanceOf<LogoutPage>())
	}

	@Test
	fun `mark as known page can be injected`() {
		assertThat(allPages.markAsKnownPage, instanceOf<MarkAsKnownPage>())
	}

	@Test
	fun `new page can be injected`() {
		assertThat(allPages.newPage, instanceOf<NewPage>())
	}

	@Test
	fun `options page can be injected`() {
		assertThat(allPages.optionsPage, instanceOf<OptionsPage>())
	}

	@Test
	fun `rescue page can be injected`() {
		assertThat(allPages.rescuePage, instanceOf<RescuePage>())
	}

	@Test
	fun `search page can be injected`() {
		assertThat(allPages.searchPage, instanceOf<SearchPage>())
	}

	@Test
	fun `unbookmark page can be injected`() {
		assertThat(allPages.unbookmarkPage, instanceOf<UnbookmarkPage>())
	}

	@Test
	fun `unfollow sone page can be injected`() {
		assertThat(allPages.unfollowSonePage, instanceOf<UnfollowSonePage>())
	}

	@Test
	fun `unlike page can be injected`() {
		assertThat(allPages.unlikePage, instanceOf<UnlikePage>())
	}

	@Test
	fun `unlock sone page can be injected`() {
		assertThat(allPages.unlockSonePage, instanceOf<UnlockSonePage>())
	}

	@Test
	fun `upload image page can be injected`() {
		assertThat(allPages.uploadImagePage, instanceOf<UploadImagePage>())
	}

	@Test
	fun `view post page can be injected`() {
		assertThat(allPages.viewPostPage, instanceOf<ViewPostPage>())
	}

	@Test
	fun `view sone page can be injected`() {
		assertThat(allPages.viewSonePage, instanceOf<ViewSonePage>())
	}
}

val baseInjector by lazy {
	createInjector(
			Core::class.isProvidedByMock(),
			FreenetInterface::class.isProvidedByMock(),
			Template::class.isProvidedByMock(),
			WebInterface::class.isProvidedByDeepMock(),
			TemplateRenderer::class.isProvidedByMock(),
			NewElements::class.isProvidedByMock()
	)!!
}
