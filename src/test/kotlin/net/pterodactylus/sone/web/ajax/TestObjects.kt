package net.pterodactylus.sone.web.ajax

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.eventbus.EventBus
import freenet.clients.http.ToadletContext
import freenet.support.SimpleReadOnlyArrayBucket
import freenet.support.api.HTTPRequest
import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.core.ElementLoader
import net.pterodactylus.sone.core.LinkedElement
import net.pterodactylus.sone.core.Preferences
import net.pterodactylus.sone.core.UpdateChecker
import net.pterodactylus.sone.data.Album
import net.pterodactylus.sone.data.Image
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Profile
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.data.Sone.SoneStatus
import net.pterodactylus.sone.data.Sone.SoneStatus.idle
import net.pterodactylus.sone.data.SoneOptions.DefaultSoneOptions
import net.pterodactylus.sone.freenet.*
import net.pterodactylus.sone.test.deepMock
import net.pterodactylus.sone.test.get
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.utils.asOptional
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.util.notify.Notification
import net.pterodactylus.util.template.TemplateContextFactory
import net.pterodactylus.util.web.Method.GET
import net.pterodactylus.util.web.Method.POST
import org.mockito.ArgumentMatchers
import java.util.*
import javax.naming.SizeLimitExceededException

/**
 * Base class for tests that supplies commonly used objects.
 */
open class TestObjects {

	val objectMapper = ObjectMapper()

	val webInterface = mock<WebInterface>()
	var formPassword = "form-password"
	val core = mock<Core>()
	val eventBus = mock<EventBus>()
	val preferences = Preferences(eventBus)
	val updateChecker = mock<UpdateChecker>()
	val elementLoader = mock<ElementLoader>()

	val toadletContext = mock<ToadletContext>()
	val freenetRequest = mock<FreenetRequest>()
	val httpRequest = mock<HTTPRequest>()
	val currentSone = deepMock<Sone>()
	val profile = Profile(currentSone)

	val requestHeaders = mutableMapOf<String, String>()
	val requestParameters = mutableMapOf<String, String>()
	val requestParts = mutableMapOf<String, String>()
	val localSones = mutableMapOf<String, Sone>()
	val remoteSones = mutableMapOf<String, Sone>()
	val posts = mutableMapOf<String, Post>()
	val postLikes = mutableMapOf<Post, Set<Sone>>()
	val newPosts = mutableMapOf<String, Post>()
	val replies = mutableMapOf<String, PostReply>()
	val replyLikes = mutableMapOf<PostReply, Set<Sone>>()
	val newReplies = mutableMapOf<String, PostReply>()
	val linkedElements = mutableMapOf<String, LinkedElement>()
	val notifications = mutableMapOf<String, Notification>()
	val albums = mutableMapOf<String, Album>()
	val images = mutableMapOf<String, Image>()
	val translations = mutableMapOf<String, String>()

	private val translation = object : Translation {
		override val currentLocale = Locale.ENGLISH
		override fun translate(key: String) = translations[key] ?: ""
	}

	init {
		whenever(webInterface.templateContextFactory).thenReturn(TemplateContextFactory())
		whenever(webInterface.getCurrentSone(ArgumentMatchers.eq(toadletContext))).thenReturn(currentSone)
		whenever(webInterface.core).thenReturn(core)
		whenever(webInterface.formPassword).then { formPassword }
		whenever(webInterface.getNotifications(currentSone)).thenAnswer { notifications.values }
		whenever(webInterface.getNotification(ArgumentMatchers.anyString())).then { notifications[it[0]].asOptional() }
		whenever(webInterface.getNewPosts(currentSone)).thenAnswer { newPosts.values }
		whenever(webInterface.getNewReplies(currentSone)).thenAnswer { newReplies.values }
		whenever(webInterface.translation).thenReturn(translation)

		whenever(core.preferences).thenReturn(preferences)
		whenever(core.updateChecker).thenReturn(updateChecker)
		whenever(core.getSone(ArgumentMatchers.anyString())).thenAnswer { (localSones + remoteSones)[it.getArgument(0)] }
		whenever(core.getLocalSone(ArgumentMatchers.anyString())).thenAnswer { localSones[it[0]] }
		whenever(core.getPost(ArgumentMatchers.anyString())).thenAnswer { (posts + newPosts)[it[0]] }
		whenever(core.getLikes(ArgumentMatchers.any<Post>())).then { postLikes[it[0]] ?: emptySet<Sone>() }
		whenever(core.getLikes(ArgumentMatchers.any<PostReply>())).then { replyLikes[it[0]] ?: emptySet<Sone>() }
		whenever(core.getPostReply(ArgumentMatchers.anyString())).then { replies[it[0]] }
		whenever(core.getAlbum(ArgumentMatchers.anyString())).then { albums[it[0]] }
		whenever(core.getImage(ArgumentMatchers.anyString())).then { images[it[0]] }
		whenever(core.getImage(ArgumentMatchers.anyString(), ArgumentMatchers.anyBoolean())).then { images[it[0]] }

		whenever(elementLoader.loadElement(ArgumentMatchers.anyString())).thenAnswer {
			linkedElements[it.getArgument(0)] ?: LinkedElement(it.getArgument(0), loading = true)
		}

		whenever(currentSone.options).thenReturn(DefaultSoneOptions())
		currentSone.mock("soneId", "Sone_Id", true, 1000, idle)

		whenever(freenetRequest.toadletContext).thenReturn(toadletContext)
		whenever(freenetRequest.method).thenReturn(GET)
		whenever(freenetRequest.httpRequest).thenReturn(httpRequest)

		whenever(httpRequest.method).thenReturn("GET")
		whenever(httpRequest.getHeader(ArgumentMatchers.anyString())).thenAnswer { requestHeaders[it.get<String>(0).toLowerCase()] }
		whenever(httpRequest.getParam(ArgumentMatchers.anyString())).thenAnswer { requestParameters[it.getArgument(0)] ?: "" }
		whenever(httpRequest.getParam(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenAnswer { requestParameters[it.getArgument(0)] ?: it.getArgument(1) }
		whenever(httpRequest.getParam(ArgumentMatchers.anyString(), ArgumentMatchers.isNull())).thenAnswer { requestParameters[it.getArgument(0)] }
		whenever(httpRequest.getPart(ArgumentMatchers.anyString())).thenAnswer { requestParts[it.getArgument(0)]?.let { SimpleReadOnlyArrayBucket(it.toByteArray()) } }
		whenever(httpRequest.getPartAsBytesFailsafe(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt())).thenAnswer { requestParts[it.getArgument(0)]?.toByteArray()?.copyOf(it.getArgument(1)) ?: ByteArray(0) }
		whenever(httpRequest.getPartAsBytesThrowing(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt())).thenAnswer { invocation -> requestParts[invocation.getArgument(0)]?.let { it.toByteArray().let { if (it.size > invocation.getArgument<Int>(1)) throw SizeLimitExceededException() else it } } ?: throw NoSuchElementException() }
		whenever(httpRequest.getPartAsStringFailsafe(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt())).thenAnswer { requestParts[it.getArgument(0)]?.substring(0, it.getArgument(1)) ?: "" }
		whenever(httpRequest.getPartAsStringThrowing(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt())).thenAnswer { invocation -> requestParts[invocation.getArgument(0)]?.let { if (it.length > invocation.getArgument<Int>(1)) throw SizeLimitExceededException() else it } ?: throw NoSuchElementException() }
		whenever(httpRequest.getIntPart(ArgumentMatchers.anyString(), ArgumentMatchers.anyInt())).thenAnswer { invocation -> requestParts[invocation.getArgument(0)]?.toIntOrNull() ?: invocation.getArgument(1) }
		whenever(httpRequest.isPartSet(ArgumentMatchers.anyString())).thenAnswer { it.getArgument(0) in requestParts }

		whenever(currentSone.profile).thenReturn(profile)
	}

	protected fun Sone.mock(id: String, name: String, local: Boolean = false, time: Long, status: SoneStatus = idle) = apply {
		whenever(this.id).thenReturn(id)
		whenever(this.name).thenReturn(name)
		whenever(isLocal).thenReturn(local)
		whenever(this.time).thenReturn(time)
		whenever(this.status).thenReturn(status)
	}

	protected fun unsetCurrentSone() {
		whenever(webInterface.getCurrentSone(ArgumentMatchers.eq(toadletContext))).thenReturn(null)
	}

	protected fun postRequest() {
		whenever(freenetRequest.method).thenReturn(POST)
		whenever(httpRequest.method).thenReturn("POST")
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

	protected fun addLocalSone(sone: Sone, id: String? = null) {
		localSones[id ?: sone.id] = sone
	}

	protected fun addPost(post: Post, id: String? = null) {
		posts[id ?: post.id] = post
	}

	protected fun addLikes(post: Post, vararg sones: Sone) {
		postLikes[post] = setOf(*sones)
	}

	protected fun addLikes(reply: PostReply, vararg sones: Sone) {
		replyLikes[reply] = setOf(*sones)
	}

	protected fun addNewPost(id: String, soneId: String, time: Long, recipientId: String? = null) =
			mock<Post>().apply {
				whenever(this.id).thenReturn(id)
				val sone = mock<Sone>().apply { whenever(this.id).thenReturn(soneId) }
				whenever(this.sone).thenReturn(sone)
				whenever(this.time).thenReturn(time)
				whenever(this.recipientId).thenReturn(recipientId.asOptional())
			}.also { newPosts[id] = it }

	protected fun addReply(reply: PostReply, id: String? = null) {
		replies[id ?: reply.id] = reply
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

	protected fun addTranslation(key: String, value: String) {
		translations[key] = value
	}

}
