package net.pterodactylus.sone.web.pages

import com.google.common.eventbus.EventBus
import freenet.clients.http.ToadletContext
import freenet.support.SimpleReadOnlyArrayBucket
import freenet.support.api.HTTPRequest
import freenet.support.api.HTTPUploadedFile
import net.pterodactylus.sone.core.Preferences
import net.pterodactylus.sone.data.Album
import net.pterodactylus.sone.data.Image
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.data.TemporaryImage
import net.pterodactylus.sone.freenet.wot.OwnIdentity
import net.pterodactylus.sone.test.deepMock
import net.pterodactylus.sone.test.get
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.utils.asList
import net.pterodactylus.sone.utils.asOptional
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.sone.web.page.FreenetTemplatePage.RedirectException
import net.pterodactylus.util.notify.Notification
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import net.pterodactylus.util.web.Method
import net.pterodactylus.util.web.Method.GET
import net.pterodactylus.util.web.Response
import org.junit.Assert.fail
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import java.io.ByteArrayOutputStream
import java.net.URI
import java.nio.charset.Charset
import kotlin.text.Charsets.UTF_8

/**
 * Base class for web page tests.
 */
open class WebPageTest(pageSupplier: (Template, WebInterface) -> SoneTemplatePage = { _, _ -> mock() }) {

	val currentSone = mock<Sone>()
	val template = mock<Template>()
	val webInterface = deepMock<WebInterface>()
	val core = webInterface.core
	val eventBus = mock<EventBus>()
	val preferences = Preferences(eventBus)
	val l10n = webInterface.l10n!!

	val page by lazy { pageSupplier(template, webInterface) }
	val httpRequest = mock<HTTPRequest>()
	val freenetRequest = mock<FreenetRequest>()
	val templateContext = TemplateContext()
	val toadletContext = deepMock<ToadletContext>()
	val responseContent = ByteArrayOutputStream()
	val response = Response(responseContent)

	private val requestHeaders = mutableMapOf<String, String>()
	private val getRequestParameters = mutableMapOf<String, MutableList<String>>()
	private val postRequestParameters = mutableMapOf<String, ByteArray>()
	private val uploadedFileNames = mutableMapOf<String, String>()
	private val uploadedFileContentTypes = mutableMapOf<String, String>()
	private val uploadedFileResources = mutableMapOf<String, String>()
	private val ownIdentities = mutableSetOf<OwnIdentity>()
	private val allSones = mutableMapOf<String, Sone>()
	private val localSones = mutableMapOf<String, Sone>()
	private val allPosts = mutableMapOf<String, Post>()
	private val allPostReplies = mutableMapOf<String, PostReply>()
	private val perPostReplies = mutableMapOf<String, PostReply>()
	private val allAlbums = mutableMapOf<String, Album>()
	private val allImages = mutableMapOf<String, Image>()
	private val notifications = mutableMapOf<String, Notification>()
	private val translations = mutableMapOf<String, String>()

	init {
		setupCore()
		setupWebInterface()
		setupHttpRequest()
		setupFreenetRequest()
		setupTranslations()
	}

	private fun setupCore() {
		whenever(core.preferences).thenReturn(preferences)
		whenever(core.identityManager.allOwnIdentities).then { ownIdentities }
		whenever(core.sones).then { allSones.values }
		whenever(core.getSone(anyString())).then { allSones[it[0]] }
		whenever(core.localSones).then { localSones.values }
		whenever(core.getLocalSone(anyString())).then { localSones[it[0]] }
		whenever(core.getPost(anyString())).then { allPosts[it[0]] }
		whenever(core.getPostReply(anyString())).then { allPostReplies[it[0]] }
		whenever(core.getReplies(anyString())).then { perPostReplies[it[0]].asList() }
		whenever(core.getAlbum(anyString())).then { allAlbums[it[0]] }
		whenever(core.getImage(anyString())).then { allImages[it[0]]}
		whenever(core.getImage(anyString(), anyBoolean())).then { allImages[it[0]]}
		whenever(core.getTemporaryImage(anyString())).thenReturn(null)
	}

	private fun setupWebInterface() {
		whenever(webInterface.getCurrentSoneCreatingSession(eq(toadletContext))).thenReturn(currentSone)
		whenever(webInterface.getCurrentSone(eq(toadletContext), anyBoolean())).thenReturn(currentSone)
		whenever(webInterface.getCurrentSoneWithoutCreatingSession(eq(toadletContext))).thenReturn(currentSone)
		whenever(webInterface.getNotifications(currentSone)).then { notifications.values }
		whenever(webInterface.getNotification(anyString())).then { notifications[it[0]].asOptional() }
	}

	private fun setupHttpRequest() {
		whenever(httpRequest.method).thenReturn("GET")
		whenever(httpRequest.getHeader(anyString())).then { requestHeaders[it.get<String>(0).toLowerCase()] }
		whenever(httpRequest.hasParameters()).then { getRequestParameters.isNotEmpty() }
		whenever(httpRequest.parameterNames).then { getRequestParameters.keys }
		whenever(httpRequest.isParameterSet(anyString())).then { it[0] in getRequestParameters }
		whenever(httpRequest.getParam(anyString())).then { getRequestParameters[it[0]]?.firstOrNull() ?: "" }
		whenever(httpRequest.getParam(anyString(), anyString())).then { getRequestParameters[it[0]]?.firstOrNull() ?: it[1] }
		whenever(httpRequest.getIntParam(anyString())).then { getRequestParameters[it[0]]?.first()?.toIntOrNull() ?: 0 }
		whenever(httpRequest.getIntParam(anyString(), anyInt())).then { getRequestParameters[it[0]]?.first()?.toIntOrNull() ?: it[1] }
		whenever(httpRequest.getLongParam(anyString(), anyLong())).then { getRequestParameters[it[0]]?.first()?.toLongOrNull() ?: it[1] }
		whenever(httpRequest.getMultipleParam(anyString())).then { getRequestParameters[it[0]]?.toTypedArray() ?: emptyArray<String>() }
		whenever(httpRequest.getMultipleIntParam(anyString())).then { getRequestParameters[it[0]]?.map { it.toIntOrNull() ?: 0 } ?: emptyArray<Int>() }
		whenever(httpRequest.isPartSet(anyString())).then { it[0] in postRequestParameters }
		whenever(httpRequest.getPartAsStringFailsafe(anyString(), anyInt())).then { postRequestParameters[it[0]]?.decode()?.take(it[1]) ?: "" }
		whenever(httpRequest.getUploadedFile(anyString())).then {
			it.get<String>(0).takeIf { it in uploadedFileNames }
					?.let { name -> UploadedFile(uploadedFileNames[name]!!, uploadedFileContentTypes[name]!!, uploadedFileResources[name]!!)
			}
		}
	}

	private class UploadedFile(private val filename: String, private val contentType: String, private val resourceName: String): HTTPUploadedFile {
		override fun getFilename() = filename
		override fun getContentType() = contentType
		override fun getData() = javaClass.getResourceAsStream(resourceName).readBytes().let(::SimpleReadOnlyArrayBucket)
	}

	private fun ByteArray.decode(charset: Charset = UTF_8) = String(this, charset)

	private fun setupFreenetRequest() {
		whenever(freenetRequest.method).thenReturn(GET)
		whenever(freenetRequest.httpRequest).thenReturn(httpRequest)
		whenever(freenetRequest.toadletContext).thenReturn(toadletContext)
	}

	private fun setupTranslations() {
		whenever(l10n.getString(anyString())).then { translations[it[0]] ?: it[0] }
	}

	fun setMethod(method: Method) {
		whenever(httpRequest.method).thenReturn(method.name)
		whenever(freenetRequest.method).thenReturn(method)
	}

	fun request(uri: String) {
		whenever(httpRequest.path).thenReturn(uri)
		whenever(freenetRequest.uri).thenReturn(URI(uri))
	}

	fun addHttpRequestHeader(name: String, value: String) {
		requestHeaders[name.toLowerCase()] = value
	}

	fun addHttpRequestParameter(name: String, value: String) {
		getRequestParameters[name] = getRequestParameters.getOrElse(name) { mutableListOf() }.apply { add(value) }
	}

	fun addHttpRequestPart(name: String, value: String) {
		postRequestParameters[name] = value.toByteArray(UTF_8)
	}

	fun unsetCurrentSone() {
		whenever(webInterface.getCurrentSoneCreatingSession(eq(toadletContext))).thenReturn(null)
		whenever(webInterface.getCurrentSone(eq(toadletContext), anyBoolean())).thenReturn(null)
		whenever(webInterface.getCurrentSoneWithoutCreatingSession(eq(toadletContext))).thenReturn(null)
	}

	fun addOwnIdentity(ownIdentity: OwnIdentity) {
		ownIdentities += ownIdentity
	}

	fun addSone(id: String, sone: Sone) {
		allSones[id] = sone
	}

	fun addLocalSone(id: String, localSone: Sone) {
		localSones[id] = localSone
	}

	fun addPost(id: String, post: Post) {
		allPosts[id] = post
	}

	fun addPostReply(id: String, postReply: PostReply) {
		allPostReplies[id] = postReply
		postReply.postId?.also { perPostReplies[it] = postReply }
	}

	fun addAlbum(id: String, album: Album) {
		allAlbums[id] = album
	}

	fun addImage(id: String, image: Image) {
		allImages[id] = image
	}

	fun addTranslation(key: String, value: String) {
		translations[key] = value
	}

	fun addNotification(id: String, notification: Notification) {
		notifications[id] = notification
	}

	fun addTemporaryImage(id: String, temporaryImage: TemporaryImage) {
		whenever(core.getTemporaryImage(id)).thenReturn(temporaryImage)
	}

	fun addUploadedFile(name: String, filename: String, contentType: String, resource: String) {
		uploadedFileNames[name] = filename
		uploadedFileContentTypes[name] = contentType
		uploadedFileResources[name] = resource
	}

	fun verifyNoRedirect(assertions: () -> Unit) {
		var caughtException: Exception? = null
		try {
			page.handleRequest(freenetRequest, templateContext)
		} catch (e: Exception) {
			caughtException = e
		}
		caughtException?.run { throw this } ?: assertions()
	}

	fun verifyRedirect(target: String, assertions: () -> Unit = {}) {
		try {
			page.handleRequest(freenetRequest, templateContext)
			fail()
		} catch (re: RedirectException) {
			if (re.target != target) {
				throw re
			}
			assertions()
		} catch (e: Exception) {
			throw e
		}
	}

}
