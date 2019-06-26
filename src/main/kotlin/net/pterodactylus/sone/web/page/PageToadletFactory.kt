/*
 * Sone - PageToadletFactory.kt - Copyright © 2010–2019 David Roden
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

import freenet.client.*
import freenet.clients.http.*
import net.pterodactylus.util.web.*
import javax.inject.*

class PageToadletFactory @Inject constructor(
		private val highLevelSimpleClient: HighLevelSimpleClient,
		private val sessionManager: SessionManager,
		@Named("toadletPathPrefix") private val pathPrefix: String
) {

	@JvmOverloads
	fun createPageToadlet(page: Page<FreenetRequest>, menuName: String? = null) =
			PageToadlet(highLevelSimpleClient, sessionManager, menuName ?: page.menuName, page, pathPrefix)

}
