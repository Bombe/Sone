/*
 * Sone - PageToadlet.kt - Copyright © 2010–2020 David Roden
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
import freenet.clients.http.LinkEnabledCallback
import freenet.clients.http.LinkFilterExceptedToadlet
import freenet.clients.http.Toadlet
import freenet.clients.http.ToadletContext
import freenet.support.MultiValueTable
import freenet.support.api.HTTPRequest
import net.pterodactylus.sone.utils.use
import net.pterodactylus.util.web.Method
import net.pterodactylus.util.web.Page
import net.pterodactylus.util.web.Response
import java.net.URI

/**
 * [Toadlet] implementation that is wrapped around a [Page].
 */
class PageToadlet(
		highLevelSimpleClient: HighLevelSimpleClient,
		val menuName: String?,
		private val page: Page<FreenetRequest>,
		private val pathPrefix: String
) : Toadlet(highLevelSimpleClient), LinkEnabledCallback, LinkFilterExceptedToadlet {

	override fun path() = pathPrefix + page.path

	override fun handleMethodGET(uri: URI, httpRequest: HTTPRequest, toadletContext: ToadletContext) =
			handleRequest(FreenetRequest(uri, Method.GET, httpRequest, toadletContext))

	fun handleMethodPOST(uri: URI?, httpRequest: HTTPRequest?, toadletContext: ToadletContext?) =
			handleRequest(FreenetRequest(uri!!, Method.POST, httpRequest!!, toadletContext!!))

	private fun handleRequest(pageRequest: FreenetRequest) {
		pageRequest.toadletContext.bucketFactory.makeBucket(-1).use { pageBucket ->
			pageBucket.outputStream.use { pageBucketOutputStream ->
				val pageResponse = page.handleRequest(pageRequest, Response(pageBucketOutputStream))
				// according to the javadoc, headers is allowed to return null but that’s stupid and it doesn’t do that.
				val headers = pageResponse.headers.fold(MultiValueTable<String, String>()) { headers, header ->
					headers.apply {
						header.forEach { put(header.name, it) }
					}
				}
				with(pageResponse) {
					writeReply(pageRequest.toadletContext, statusCode, contentType, statusText, headers, pageBucket)
				}
			}
		}
	}

	override fun isEnabled(toadletContext: ToadletContext) =
			if (page is LinkEnabledCallback) {
				page.isEnabled(toadletContext)
			} else
				true

	override fun isLinkExcepted(link: URI) =
			page is FreenetPage && page.isLinkExcepted(link)

	override fun toString() = "${javaClass.name}[path=${path()},page=$page]"

}
