package net.pterodactylus.sone.web.ajax

import com.google.common.eventbus.EventBus
import freenet.clients.http.ToadletContext
import freenet.support.SimpleReadOnlyArrayBucket
import freenet.support.api.HTTPRequest
import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.core.ElementLoader
import net.pterodactylus.sone.core.LinkedElement
import net.pterodactylus.sone.core.Preferences
import net.pterodactylus.sone.data.Album
import net.pterodactylus.sone.data.Image
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Profile
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.data.Sone.SoneStatus
import net.pterodactylus.sone.data.Sone.SoneStatus.idle
import net.pterodactylus.sone.test.deepMock
import net.pterodactylus.sone.test.get
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.utils.asOptional
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.notify.Notification
import net.pterodactylus.util.web.Method.GET
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.ArgumentMatchers.isNull
import java.util.NoSuchElementException
import javax.naming.SizeLimitExceededException

/**
 * Base class for tests for any [JsonPage] implementations.
 */
abstract class JsonPageTest(
		private val expectedPath: String,
		private val requiresLogin: Boolean = true,
		private val needsFormPassword: Boolean = true,
		pageSupplier: (WebInterface) -> JsonPage = { _ -> mock<JsonPage>() }) {

	protected val webInterface = mock<WebInterface>()
	protected val core = mock<Core>()
	protected val eventBus = mock<EventBus>()
	protected val preferences = Preferences(eventBus)
	protected val elementLoader = mock<ElementLoader>()
	protected open val page: JsonPage by lazy { pageSupplier(webInterface) }
	protected val json by lazy { page.createJsonObject(freenetRequest) }

	protected val toadletContext = mock<ToadletContext>()
	protected val freenetRequest = mock<FreenetRequest>()
	protected val httpRequest = mock<HTTPRequest>()
	protected val currentSone = deepMock<Sone>()
	protected val profile = Profile(currentSone)

	private val requestHeaders = mutableMapOf<String, String>()
	private val requestParameters = mutableMapOf<String, String>()
	private val requestParts = mutableMapOf<String, String>()
	private val localSones = mutableMapOf<String, Sone>()
	private val remoteSones = mutableMapOf<String, Sone>()
	private val posts = mutableMapOf<String, Post>()
	private val newPosts = mutableMapOf<String, Post>()
	private val replies = mutableMapOf<String, PostReply>()
	private val newReplies = mutableMapOf<String, PostReply>()
	private val linkedElements = mutableMapOf<String, LinkedElement>()
	private val notifications = mutableMapOf<String, Notification>()
	private val albums = mutableMapOf<String, Album>()
	private val images = mutableMapOf<String, Image>()

	@Before
	fun setupWebInterface() {
		whenever(webInterface.getCurrentSone(eq(toadletContext), anyBoolean())).thenReturn(currentSone)
		whenever(webInterface.getCurrentSoneCreatingSession(toadletContext)).thenReturn(currentSone)
		whenever(webInterface.getCurrentSoneWithoutCreatingSession(toadletContext)).thenReturn(currentSone)
		whenever(webInterface.core).thenReturn(core)
		whenever(webInterface.getNotifications(currentSone)).thenAnswer { notifications.values }
		whenever(webInterface.getNotification(anyString())).then { notifications[it[0]].asOptional() }
		whenever(webInterface.getNewPosts(currentSone)).thenAnswer { newPosts.values }
		whenever(webInterface.getNewReplies(currentSone)).thenAnswer { newReplies.values }
	}

	@Before
	fun setupCore() {
		whenever(core.preferences).thenReturn(preferences)
		whenever(core.getSone(anyString())).thenAnswer { (localSones + remoteSones)[it.getArgument(0)].asOptional() }
		whenever(core.getLocalSone(anyString())).thenAnswer { localSones[it[0]] }
		whenever(core.getPost(anyString())).thenAnswer { (posts + newPosts)[it[0]].asOptional() }
		whenever(core.getPostReply(anyString())).then { replies[it[0]].asOptional() }
		whenever(core.getAlbum(anyString())).then { albums[it[0]] }
		whenever(core.getImage(anyString())).then { images[it[0]] }
		whenever(core.getImage(anyString(), anyBoolean())).then { images[it[0]] }
	}

	@Before
	fun setupElementLoader() {
		whenever(elementLoader.loadElement(anyString())).thenAnswer {
			linkedElements[it.getArgument(0)] ?: LinkedElement(it.getArgument(0), loading = true)
		}
	}

	@Before
	fun setupCurrentSone() {
		currentSone.mock("soneId", "Sone_Id", true, 1000, idle)
	}

	@Before
	fun setupFreenetRequest() {
		whenever(freenetRequest.toadletContext).thenReturn(toadletContext)
		whenever(freenetRequest.method).thenReturn(GET)
		whenever(freenetRequest.httpRequest).thenReturn(httpRequest)
	}

	@Before
	fun setupHttpRequest() {
		whenever(httpRequest.method).thenReturn("GET")
		whenever(httpRequest.getHeader(anyString())).thenAnswer { requestHeaders[it.get<String>(0).toLowerCase()] }
		whenever(httpRequest.getParam(anyString())).thenAnswer { requestParameters[it.getArgument(0)] ?: "" }
		whenever(httpRequest.getParam(anyString(), anyString())).thenAnswer { requestParameters[it.getArgument(0)] ?: it.getArgument(1) }
		whenever(httpRequest.getParam(anyString(), isNull())).thenAnswer { requestParameters[it.getArgument(0)] }
		whenever(httpRequest.getPart(anyString())).thenAnswer { requestParts[it.getArgument(0)]?.let { SimpleReadOnlyArrayBucket(it.toByteArray()) } }
		whenever(httpRequest.getPartAsBytesFailsafe(anyString(), anyInt())).thenAnswer { requestParts[it.getArgument(0)]?.toByteArray()?.copyOf(it.getArgument(1)) ?: ByteArray(0) }
		whenever(httpRequest.getPartAsBytesThrowing(anyString(), anyInt())).thenAnswer { invocation -> requestParts[invocation.getArgument(0)]?.let { it.toByteArray().let { if (it.size > invocation.getArgument<Int>(1)) throw SizeLimitExceededException() else it } } ?: throw NoSuchElementException() }
		whenever(httpRequest.getPartAsStringFailsafe(anyString(), anyInt())).thenAnswer { requestParts[it.getArgument(0)]?.substring(0, it.getArgument(1)) ?: "" }
		whenever(httpRequest.getPartAsStringThrowing(anyString(), anyInt())).thenAnswer { invocation -> requestParts[invocation.getArgument(0)]?.let { if (it.length > invocation.getArgument<Int>(1)) throw SizeLimitExceededException() else it } ?: throw NoSuchElementException() }
		whenever(httpRequest.getIntPart(anyString(), anyInt())).thenAnswer { invocation -> requestParts[invocation.getArgument(0)]?.toIntOrNull() ?: invocation.getArgument(1) }
		whenever(httpRequest.isPartSet(anyString())).thenAnswer { it.getArgument(0) in requestParts }
	}

	@Before
	fun setupProfile() {
		whenever(currentSone.profile).thenReturn(profile)
	}

	protected val JsonReturnObject.error get() = if (this is JsonErrorReturnObject) this.error else null

	protected fun Sone.mock(id: String, name: String, local: Boolean = false, time: Long, status: SoneStatus = idle) = apply {
		whenever(this.id).thenReturn(id)
		whenever(this.name).thenReturn(name)
		whenever(isLocal).thenReturn(local)
		whenever(this.time).thenReturn(time)
		whenever(this.status).thenReturn(status)
	}

	protected fun unsetCurrentSone() {
		whenever(webInterface.getCurrentSone(eq(toadletContext), anyBoolean())).thenReturn(null)
		whenever(webInterface.getCurrentSoneWithoutCreatingSession(toadletContext)).thenReturn(null)
		whenever(webInterface.getCurrentSoneCreatingSession(toadletContext)).thenReturn(null)
	}

	protected fun addRequestHeader(key: String, value: String) {
		requestHeaders += key.toLowerCase() to value
	}

	protected fun addRequestParameter(key: String, value: String) {
		requestParameters += key to value
	}

	protected fun addRequestPart(key: String, value: String) {
		requestParts += key to value
	}

	protected fun addNotification(notification: Notification, notificationId: String? = null) {
		notifications[notificationId ?: notification.id] = notification
	}

	protected fun addSone(sone: Sone, soneId: String? = null) {
		remoteSones += (soneId ?: sone.id) to sone
	}

	protected fun addLocalSone(id: String, sone: Sone) {
		localSones += id to sone
	}

	protected fun addPost(post: Post, id: String? = null) {
		posts[id ?: post.id] = post
	}

	protected fun addNewPost(id: String, soneId: String, time: Long, recipientId: String? = null) =
			mock<Post>().apply {
				whenever(this.id).thenReturn(id)
				val sone = mock<Sone>().apply { whenever(this.id).thenReturn(soneId) }
				whenever(this.sone).thenReturn(sone)
				whenever(this.time).thenReturn(time)
				whenever(this.recipientId).thenReturn(recipientId.asOptional())
			}.also { newPosts[id] = it }

	protected fun addReply(id: String, reply: PostReply) {
		replies[id] = reply
	}

	protected fun addNewReply(id: String, soneId: String, postId: String, postSoneId: String) {
		newReplies[id] = mock<PostReply>().apply {
			whenever(this.id).thenReturn(id)
			val sone = mock<Sone>().apply { whenever(this.id).thenReturn(soneId) }
			whenever(this.sone).thenReturn(sone)
			val postSone = mock<Sone>().apply { whenever(this.id).thenReturn(postSoneId) }
			val post = mock<Post>().apply {
				whenever(this.sone).thenReturn(postSone)
			}
			whenever(this.post).thenReturn(post.asOptional())
			whenever(this.postId).thenReturn(postId)
		}
	}

	protected fun addLinkedElement(link: String, loading: Boolean, failed: Boolean) {
		linkedElements[link] = LinkedElement(link, failed, loading)
	}

	protected fun addAlbum(album: Album, albumId: String? = null) {
		albums[albumId ?: album.id] = album
	}

	protected fun addImage(image: Image, imageId: String? = null) {
		images[imageId ?: image.id] = image
	}

	@Test
	fun `page returns correct path`() {
		assertThat(page.path, equalTo(expectedPath))
	}

	@Test
	fun `page needs form password`() {
		assertThat(page.needsFormPassword(), equalTo(needsFormPassword))
	}

	@Test
	fun `page requires login`() {
		assertThat(page.requiresLogin(), equalTo(requiresLogin))
	}

}
