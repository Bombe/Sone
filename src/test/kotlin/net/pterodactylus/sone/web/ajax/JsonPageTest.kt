package net.pterodactylus.sone.web.ajax

import freenet.clients.http.ToadletContext
import freenet.support.api.HTTPRequest
import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.data.Sone.SoneStatus
import net.pterodactylus.sone.data.Sone.SoneStatus.idle
import net.pterodactylus.sone.test.asOptional
import net.pterodactylus.sone.test.deepMock
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.notify.Notification
import org.junit.Before
import org.mockito.ArgumentMatchers.anyString

/**
 * Base class for tests for any [JsonPage] implementations.
 */
open class JsonPageTest {

	protected val webInterface = mock<WebInterface>()
	protected val core = mock<Core>()
	protected open lateinit var page: JsonPage
	protected val json by lazy { page.createJsonObject(freenetRequest)!! }

	protected val toadletContext = mock<ToadletContext>()
	protected val freenetRequest = mock<FreenetRequest>()
	protected val httpRequest = mock<HTTPRequest>()
	protected val currentSone = deepMock<Sone>()

	private val requestParameters = mutableMapOf<String, String>()
	private val localSones = mutableMapOf<String, Sone>()
	private val remoteSones = mutableMapOf<String, Sone>()
	private val newPosts = mutableMapOf<String, Post>()
	private val newReplies = mutableMapOf<String, PostReply>()
	private val notifications = mutableListOf<Notification>()

	@Before
	fun setupWebInterface() {
		whenever(webInterface.getCurrentSoneCreatingSession(toadletContext)).thenReturn(currentSone)
		whenever(webInterface.getCurrentSoneWithoutCreatingSession(toadletContext)).thenReturn(currentSone)
		whenever(webInterface.core).thenReturn(core)
		whenever(webInterface.getNotifications(currentSone)).thenAnswer { notifications }
		whenever(webInterface.getNewPosts(currentSone)).thenAnswer { newPosts.values }
		whenever(webInterface.getNewReplies(currentSone)).thenAnswer { newReplies.values }
	}

	@Before
	fun setupCore() {
		whenever(core.getSone(anyString())).thenAnswer { (localSones + remoteSones)[it.getArgument(0)].asOptional() }
	}

	@Before
	fun setupCurrentSone() {
		currentSone.mock("soneId", "Sone_Id", true, 1000, idle)
	}

	@Before
	fun setupFreenetRequest() {
		whenever(freenetRequest.toadletContext).thenReturn(toadletContext)
		whenever(freenetRequest.httpRequest).thenReturn(httpRequest)
	}

	@Before
	fun setupHttpRequest() {
		whenever(httpRequest.getParam(anyString())).thenAnswer { requestParameters[it.getArgument(0)] ?: "" }
	}

	protected fun Sone.mock(id: String, name: String, local: Boolean = false, time: Long, status: SoneStatus = idle) = apply {
		whenever(this.id).thenReturn(id)
		whenever(this.name).thenReturn(name)
		whenever(isLocal).thenReturn(local)
		whenever(this.time).thenReturn(time)
		whenever(this.status).thenReturn(status)
	}

	protected fun unsetCurrentSone() {
		whenever(webInterface.getCurrentSoneWithoutCreatingSession(toadletContext)).thenReturn(null)
		whenever(webInterface.getCurrentSoneCreatingSession(toadletContext)).thenReturn(null)
	}

	protected fun addRequestParameter(key: String, value: String) {
		requestParameters += key to value
	}

	protected fun addNotification(vararg notifications: Notification) {
		this.notifications += notifications
	}

	protected fun addSone(sone: Sone) {
		remoteSones += sone.id to sone
	}

	protected fun addNewPost(id: String, soneId: String, time: Long, recipientId: String? = null) {
		newPosts[id] = mock<Post>().apply {
			whenever(this.id).thenReturn(id)
			val sone = mock<Sone>().apply { whenever(this.id).thenReturn(soneId) }
			whenever(this.sone).thenReturn(sone)
			whenever(this.time).thenReturn(time)
			whenever(this.recipientId).thenReturn(recipientId.asOptional())
		}
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

}
