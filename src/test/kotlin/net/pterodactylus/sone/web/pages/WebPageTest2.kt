package net.pterodactylus.sone.web.pages

import com.google.common.eventbus.EventBus
import freenet.clients.http.ToadletContext
import freenet.support.api.HTTPRequest
import net.pterodactylus.sone.core.Preferences
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.deepMock
import net.pterodactylus.sone.test.get
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.sone.utils.asOptional
import net.pterodactylus.sone.web.WebInterface
import net.pterodactylus.sone.web.page.FreenetRequest
import net.pterodactylus.sone.web.page.FreenetTemplatePage.RedirectException
import net.pterodactylus.util.template.Template
import net.pterodactylus.util.template.TemplateContext
import net.pterodactylus.util.web.Method
import net.pterodactylus.util.web.Method.GET
import org.junit.Assert.fail
import org.junit.Before
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import java.nio.charset.Charset
import kotlin.text.Charsets.UTF_8

/**
 * Base class for web page tests.
 */
abstract class WebPageTest2(pageSupplier: (Template, WebInterface) -> SoneTemplatePage) {

	protected val currentSone = mock<Sone>()
	private val template = mock<Template>()
	private val webInterface = deepMock<WebInterface>()
	protected val core = webInterface.core!!
	private val eventBus = mock<EventBus>()
	private val preferences = Preferences(eventBus)
	private val l10n = webInterface.l10n!!

	protected val page by lazy { pageSupplier(template, webInterface) }
	private val httpRequest = mock<HTTPRequest>()
	protected val freenetRequest = mock<FreenetRequest>()
	protected val templateContext = TemplateContext()

	private val toadletContext = deepMock<ToadletContext>()
	private val getRequestParameters = mutableMapOf<String, MutableList<String>>()
	private val postRequestParameters = mutableMapOf<String, ByteArray>()
	private val allSones = mutableMapOf<String, Sone>()
	private val allPosts = mutableMapOf<String, Post>()
	private val translations = mutableMapOf<String, String>()

	@Before
	fun setupCore() {
		whenever(core.preferences).thenReturn(preferences)
		whenever(core.sones).then { allSones.values }
		whenever(core.getSone(anyString())).then { allSones[it[0]].asOptional() }
		whenever(core.getPost(anyString())).then { allPosts[it[0]].asOptional() }
	}

	@Before
	fun setupWebInterface() {
		whenever(webInterface.getCurrentSoneCreatingSession(eq(toadletContext))).thenReturn(currentSone)
		whenever(webInterface.getCurrentSone(eq(toadletContext), anyBoolean())).thenReturn(currentSone)
		whenever(webInterface.getCurrentSoneWithoutCreatingSession(eq(toadletContext))).thenReturn(currentSone)
	}

	@Before
	fun setupHttpRequest() {
		whenever(httpRequest.method).thenReturn("GET")
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
		whenever(httpRequest.getPartAsStringFailsafe(anyString(), anyInt())).then { postRequestParameters[it[0]]?.decode() }
	}

	private fun ByteArray.decode(charset: Charset = UTF_8) = String(this, charset)

	@Before
	fun setupFreenetRequest() {
		whenever(freenetRequest.method).thenReturn(GET)
		whenever(freenetRequest.httpRequest).thenReturn(httpRequest)
		whenever(freenetRequest.toadletContext).thenReturn(toadletContext)
	}

	@Before
	fun setupTranslations() {
		whenever(l10n.getString(anyString())).then { translations[it[0]] ?: it[0] }
	}

	fun setMethod(method: Method) {
		whenever(httpRequest.method).thenReturn(method.name)
		whenever(freenetRequest.method).thenReturn(method)
	}

	fun addHttpRequestParameter(name: String, value: String) {
		getRequestParameters[name] = getRequestParameters.getOrElse(name) { mutableListOf<String>() }.apply { add(value) }
	}

	fun addHttpRequestPart(name: String, value: String) {
		postRequestParameters[name] = value.toByteArray(UTF_8)
	}

	fun addSone(id: String, sone: Sone) {
		allSones[id] = sone
	}

	fun addPost(id: String, post: Post) {
		allPosts[id] = post
	}

	fun addTranslation(key: String, value: String) {
		translations[key] = value
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

	fun verifyRedirect(target: String, assertions: () -> Unit) {
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
