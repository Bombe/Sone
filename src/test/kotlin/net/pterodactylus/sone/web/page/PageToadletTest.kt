/*
 * Sone - PageToadletTest.kt - Copyright © 2020 David Roden
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

package net.pterodactylus.sone.web.page

import freenet.client.HighLevelSimpleClient
import freenet.clients.http.Cookie
import freenet.clients.http.FProxyFetchInProgress.REFILTER_POLICY
import freenet.clients.http.LinkEnabledCallback
import freenet.clients.http.PageMaker
import freenet.clients.http.ReceivedCookie
import freenet.clients.http.SessionManager
import freenet.clients.http.Toadlet
import freenet.clients.http.ToadletContainer
import freenet.clients.http.ToadletContext
import freenet.clients.http.bookmark.BookmarkManager
import freenet.node.useralerts.UserAlertManager
import freenet.support.HTMLNode
import freenet.support.MultiValueTable
import freenet.support.api.Bucket
import freenet.support.api.BucketFactory
import freenet.support.api.HTTPRequest
import freenet.support.io.ArrayBucket
import net.pterodactylus.sone.test.deepMock
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.util.web.Method
import net.pterodactylus.util.web.Page
import net.pterodactylus.util.web.Response
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.arrayContaining
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.sameInstance
import org.junit.Test
import org.mockito.ArgumentMatchers.anyLong
import java.net.URI
import java.util.Date
import kotlin.text.Charsets.UTF_8

/**
 * Unit test for PageToadletTest.
 */
class PageToadletTest {

	private val highLevelSimpleClient = mock<HighLevelSimpleClient>()
	private val sessionManager = mock<SessionManager>()
	private val httpRequest = mock<HTTPRequest>()
	private val toadletContext = deepMock<ToadletContext>()

	init {
		whenever(toadletContext.bucketFactory.makeBucket(anyLong())).then { ArrayBucket() }
	}

	@Test
	fun `get request is forwarded to page correctly`() {
		var capturedRequest: FreenetRequest? = null
		val page = object : TestPage() {
			override fun handleRequest(request: FreenetRequest, response: Response) =
					super.handleRequest(request, response)
							.also { capturedRequest = request }
		}
		val pageToadlet = PageToadlet(highLevelSimpleClient, sessionManager, "MenuName", page, "/path/")
		pageToadlet.handleMethodGET(URI("/test"), httpRequest, toadletContext)
		assertThat(capturedRequest!!.uri, equalTo(URI("/test")))
		assertThat(capturedRequest!!.method, equalTo(Method.GET))
	}

	@Test
	fun `post request is forwarded to page correctly`() {
		var capturedRequest: FreenetRequest? = null
		val page = object : TestPage() {
			override fun handleRequest(request: FreenetRequest, response: Response) =
					super.handleRequest(request, response)
							.also { capturedRequest = request }
		}
		val pageToadlet = PageToadlet(highLevelSimpleClient, sessionManager, "MenuName", page, "/path/")
		pageToadlet.handleMethodPOST(URI("/test"), httpRequest, toadletContext)
		assertThat(capturedRequest!!.uri, equalTo(URI("/test")))
		assertThat(capturedRequest!!.method, equalTo(Method.POST))
	}

	@Test
	fun `content written to response is written to context`() {
		val page = object : TestPage() {
			override fun handleRequest(request: FreenetRequest, response: Response) =
					response.apply {
						statusCode = 123
						statusText = "Works"
						contentType = "data/test"
						addHeader("Test", "Value")
						addHeader("More", "true")
						addHeader("Test", "Another")
						write("Content")
					}
		}
		val pageToadlet = PageToadlet(highLevelSimpleClient, sessionManager, "MenuName", page, "/path/")
		var writtenData: ByteArray? = null
		var capturedReply: CapturedReply? = null
		val toadletContext = object : DelegatingToadletContext(this.toadletContext) {
			override fun sendReplyHeaders(code: Int, desc: String?, mvt: MultiValueTable<String, String>?, mimeType: String?, length: Long) =
					sendReplyHeaders(code, desc, mvt, mimeType, length, false)

			override fun sendReplyHeaders(code: Int, desc: String?, mvt: MultiValueTable<String, String>?, mimeType: String?, length: Long, forceDisableJavascript: Boolean) {
				capturedReply = CapturedReply(code, desc, mvt, mimeType, length)
			}

			override fun writeData(data: ByteArray?, offset: Int, length: Int) {
				writtenData = data!!.copyOfRange(offset, offset + length)
			}

			override fun writeData(data: ByteArray?) = writeData(data, 0, data!!.size)
			override fun writeData(data: Bucket?) = writeData(data!!.inputStream.readBytes())
		}
		pageToadlet.handleMethodGET(URI("/test"), httpRequest, toadletContext)
		assertThat(capturedReply!!.code, equalTo(123))
		assertThat(capturedReply!!.status, equalTo("Works"))
		assertThat(capturedReply!!.mimeType, equalTo("data/test"))
		assertThat(capturedReply!!.length, equalTo(7L))
		assertThat(capturedReply!!.headers!!.getArray("Test"), arrayContaining<Any>("Value", "Another"))
		assertThat(capturedReply!!.headers!!.getArray("More"), arrayContaining<Any>("true"))
		assertThat(writtenData!!.toString(UTF_8), equalTo("Content"))
	}

	@Test
	fun `link-enabled is true for non-callback pages`() {
		val page = TestPage()
		val pageToadlet = PageToadlet(highLevelSimpleClient, sessionManager, "MenuName", page, "/path/")
		assertThat(pageToadlet.isEnabled(toadletContext), equalTo(true))
	}

	@Test
	fun `link-enabled is passed through for callback pages`() {
		var capturedToadletContext: ToadletContext? = null
		val page = object : TestPage(), LinkEnabledCallback {
			override fun isEnabled(ctx: ToadletContext?) = false.also { capturedToadletContext = toadletContext }
		}
		val pageToadlet = PageToadlet(highLevelSimpleClient, sessionManager, "MenuName", page, "/path/")
		assertThat(pageToadlet.isEnabled(toadletContext), equalTo(false))
		assertThat(capturedToadletContext, sameInstance(toadletContext))
	}

	@Test
	fun `link excemption is false for non-freenet pages`() {
		val page = TestPage()
		val pageToadlet = PageToadlet(highLevelSimpleClient, sessionManager, "MenuName", page, "/path/")
		assertThat(pageToadlet.isLinkExcepted(URI("/test")), equalTo(false))
	}

	@Test
	fun `link excemption is passed through for freenet pages`() {
		var capturedUri: URI? = null
		val page = object : TestPage(), FreenetPage {
			override fun isLinkExcepted(link: URI) = true.also { capturedUri = link }
		}
		val pageToadlet = PageToadlet(highLevelSimpleClient, sessionManager, "MenuName", page, "/path/")
		assertThat(pageToadlet.isLinkExcepted(URI("/test")), equalTo(true))
		assertThat(capturedUri, equalTo(URI("/test")))
	}

	@Test
	fun `path is created correctly from prefix and page path`() {
		val page = object : TestPage() {
			override fun getPath() = "test-path"
		}
		val pageToadlet = PageToadlet(highLevelSimpleClient, sessionManager, "MenuName", page, "/path/")
		assertThat(pageToadlet.path(), equalTo("/path/test-path"))
	}

	@Test
	fun `menu name is returned correctly`() {
		val pageToadlet = PageToadlet(highLevelSimpleClient, sessionManager, "MenuName", TestPage(), "/path/")
		assertThat(pageToadlet.menuName, equalTo("MenuName"))
	}

}

private data class CapturedReply(val code: Int, val status: String?, val headers: MultiValueTable<String, String>?, val mimeType: String?, val length: Long?)

private open class TestPage : Page<FreenetRequest> {
	override fun getPath() = ""
	override fun isPrefixPage() = false
	override fun handleRequest(request: FreenetRequest, response: Response) = response
}

private open class DelegatingToadletContext(private val toadletContext: ToadletContext) : ToadletContext {
	override fun activeToadlet(): Toadlet = toadletContext.activeToadlet()
	override fun forceDisconnect() = toadletContext.forceDisconnect()
	override fun sendReplyHeaders(code: Int, desc: String?, mvt: MultiValueTable<String, String>?, mimeType: String?, length: Long) = toadletContext.sendReplyHeaders(code, desc, mvt, mimeType, length)
	override fun sendReplyHeaders(code: Int, desc: String?, mvt: MultiValueTable<String, String>?, mimeType: String?, length: Long, forceDisableJavascript: Boolean) = toadletContext.sendReplyHeaders(code, desc, mvt, mimeType, length, forceDisableJavascript)
	override fun sendReplyHeaders(code: Int, desc: String?, mvt: MultiValueTable<String, String>?, mimeType: String?, length: Long, mTime: Date?) = toadletContext.sendReplyHeaders(code, desc, mvt, mimeType, length, mTime)
	override fun getUri(): URI = toadletContext.uri
	override fun getPageMaker(): PageMaker = toadletContext.pageMaker
	override fun getBucketFactory(): BucketFactory = toadletContext.bucketFactory
	override fun getHeaders(): MultiValueTable<String, String> = toadletContext.headers
	override fun checkFullAccess(toadlet: Toadlet?): Boolean = toadletContext.checkFullAccess(toadlet)
	override fun doRobots(): Boolean = toadletContext.doRobots()
	override fun getReFilterPolicy(): REFILTER_POLICY = toadletContext.reFilterPolicy
	override fun getAlertManager(): UserAlertManager = toadletContext.alertManager
	override fun checkFormPassword(request: HTTPRequest?, redirectTo: String?): Boolean = toadletContext.checkFormPassword(request, redirectTo)
	override fun checkFormPassword(request: HTTPRequest?): Boolean = toadletContext.checkFormPassword(request)
	override fun addFormChild(parentNode: HTMLNode?, target: String?, id: String?): HTMLNode = toadletContext.addFormChild(parentNode, target, id)
	override fun sendReplyHeadersFProxy(code: Int, desc: String?, mvt: MultiValueTable<String, String>?, mimeType: String?, length: Long) = toadletContext.sendReplyHeadersFProxy(code, desc, mvt, mimeType, length)
	override fun setCookie(newCookie: Cookie?) = toadletContext.setCookie(newCookie)
	override fun isAdvancedModeEnabled(): Boolean = toadletContext.isAdvancedModeEnabled
	override fun disableProgressPage(): Boolean = toadletContext.disableProgressPage()
	override fun writeData(data: ByteArray?, offset: Int, length: Int) = toadletContext.writeData(data, offset, length)
	override fun writeData(data: ByteArray?) = toadletContext.writeData(data)
	override fun writeData(data: Bucket?) = toadletContext.writeData(data)
	override fun getCookie(domain: URI?, path: URI?, name: String?): ReceivedCookie? = toadletContext.getCookie(domain, path, name)
	override fun getUniqueId(): String = toadletContext.uniqueId
	override fun sendReplyHeadersStatic(code: Int, desc: String?, mvt: MultiValueTable<String, String>?, mimeType: String?, length: Long, mTime: Date?) = toadletContext.sendReplyHeadersStatic(code, desc, mvt, mimeType, length, mTime)
	override fun getBookmarkManager(): BookmarkManager = toadletContext.bookmarkManager
	override fun isAllowedFullAccess(): Boolean = toadletContext.isAllowedFullAccess
	override fun hasFormPassword(request: HTTPRequest?): Boolean = toadletContext.hasFormPassword(request)
	override fun getFormPassword(): String = toadletContext.formPassword
	override fun getContainer(): ToadletContainer = toadletContext.container
}
