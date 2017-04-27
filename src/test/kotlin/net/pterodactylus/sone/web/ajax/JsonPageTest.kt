package net.pterodactylus.sone.web.ajax

import freenet.clients.http.ToadletContext
import freenet.support.SimpleReadOnlyArrayBucket
import freenet.support.api.HTTPRequest
import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.core.ElementLoader
import net.pterodactylus.sone.core.LinkedElement
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
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import java.util.NoSuchElementException
import javax.naming.SizeLimitExceededException
import kotlin.coroutines.experimental.EmptyCoroutineContext.plus

/**
 * Base class for tests for any [JsonPage] implementations.
 */
open class JsonPageTest {

	protected val webInterface = mock<WebInterface>()
	protected val core = mock<Core>()
	protected val elementLoader = mock<ElementLoader>()
	protected open lateinit var page: JsonPage
	protected val json by lazy { page.createJsonObject(freenetRequest)!! }

	protected val toadletContext = mock<ToadletContext>()
	protected val freenetRequest = mock<FreenetRequest>()
	protected val httpRequest = mock<HTTPRequest>()
	protected val currentSone = deepMock<Sone>()

	private val requestParameters = mutableMapOf<String, String>()
	private val requestParts = mutableMapOf<String, String>()
	private val localSones = mutableMapOf<String, Sone>()
	private val remoteSones = mutableMapOf<String, Sone>()
	private val newPosts = mutableMapOf<String, Post>()
	private val newReplies = mutableMapOf<String, PostReply>()
	private val linkedElements = mutableMapOf<String, LinkedElement>()
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
		whenever(freenetRequest.httpRequest).thenReturn(httpRequest)
	}

	@Before
	fun setupHttpRequest() {
		whenever(httpRequest.getParam(anyString())).thenAnswer { requestParameters[it.getArgument(0)] ?: "" }
		whenever(httpRequest.getParam(anyString(), anyString())).thenAnswer { requestParameters[it.getArgument(0)] ?: it.getArgument(1) }
		whenever(httpRequest.getPart(anyString())).thenAnswer { requestParts[it.getArgument(0)]?.let { SimpleReadOnlyArrayBucket(it.toByteArray()) } }
		whenever(httpRequest.getPartAsBytesFailsafe(anyString(), anyInt())).thenAnswer { requestParts[it.getArgument(0)]?.toByteArray()?.copyOf(it.getArgument(1)) ?: ByteArray(0) }
		whenever(httpRequest.getPartAsBytesThrowing(anyString(), anyInt())).thenAnswer { invocation -> requestParts[invocation.getArgument(0)]?.let { it.toByteArray().let { if (it.size > invocation.getArgument<Int>(1)) throw SizeLimitExceededException() else it } } ?: throw NoSuchElementException() }
		whenever(httpRequest.getPartAsStringFailsafe(anyString(), anyInt())).thenAnswer { requestParts[it.getArgument(0)]?.substring(0, it.getArgument(1)) ?: "" }
		whenever(httpRequest.getPartAsStringThrowing(anyString(), anyInt())).thenAnswer { invocation -> requestParts[invocation.getArgument(0)]?.let { if (it.length > invocation.getArgument<Int>(1)) throw SizeLimitExceededException() else it } ?: throw NoSuchElementException() }
		whenever(httpRequest.getIntPart(anyString(), anyInt())).thenAnswer { invocation -> requestParts[invocation.getArgument(0)]?.toIntOrNull() ?: invocation.getArgument(1) }
		whenever(httpRequest.isPartSet(anyString())).thenAnswer { it.getArgument(0) in requestParts }
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

	protected fun addRequestPart(key: String, value: String) {
		requestParts += key to value
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

	protected fun addLinkedElement(link: String, loading: Boolean, failed: Boolean) {
		linkedElements[link] = LinkedElement(link, failed, loading)
	}

}
