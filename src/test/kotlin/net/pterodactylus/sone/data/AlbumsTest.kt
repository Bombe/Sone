/**
 * Sone - AlbumsTest.kt - Copyright © 2019–2020 David ‘Bombe’ Roden
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

package net.pterodactylus.sone.data

import net.pterodactylus.sone.data.impl.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import kotlin.test.*

/**
 * Unit test for various helper method in `Albums.kt`.
 */
class AlbumsTest {

	@Test
	fun `recursive list of all images for album is returned correctly`() {
		val sone = IdOnlySone("sone")
		val album = AlbumImpl(sone)
		val firstNestedAlbum = AlbumImpl(sone)
		val secondNestedAlbum = AlbumImpl(sone)
		firstNestedAlbum.addImage(createImage(sone, "image-1"))
		firstNestedAlbum.addImage(createImage(sone, "image-2"))
		secondNestedAlbum.addImage(createImage(sone, "image-3"))
		album.addImage(createImage(sone, "image-4"))
		album.addAlbum(firstNestedAlbum)
		album.addAlbum(secondNestedAlbum)
		val images = album.allImages
		assertThat(images.map(Image::id), containsInAnyOrder("image-1", "image-2", "image-3", "image-4"))
	}

	private fun createImage(sone: IdOnlySone, id: String) = ImageImpl(id).modify().setSone(sone).update()

}
