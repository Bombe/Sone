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

	private fun createImage(sone: IdOnlySone, id: String, key: String? = null) = ImageImpl(id).modify().setSone(sone).setKey(key).update()

	@Test
	fun `allAlbums returns itself and all its subalbums`() {
		val sone = IdOnlySone("sone")
		val album = AlbumImpl(sone)
		val firstNestedAlbum = AlbumImpl(sone)
		val secondNestedAlbum = AlbumImpl(sone)
		val albumNestedInFirst = AlbumImpl(sone)
		album.addAlbum(firstNestedAlbum)
		album.addAlbum(secondNestedAlbum)
		firstNestedAlbum.addAlbum(albumNestedInFirst)
		val albums = album.allAlbums
		assertThat(albums, containsInAnyOrder<Album>(album, firstNestedAlbum, secondNestedAlbum, albumNestedInFirst))
		assertThat(albums.indexOf(firstNestedAlbum), greaterThan(albums.indexOf(album)))
		assertThat(albums.indexOf(secondNestedAlbum), greaterThan(albums.indexOf(album)))
		assertThat(albums.indexOf(albumNestedInFirst), greaterThan(albums.indexOf(firstNestedAlbum)))
	}

	@Test
	fun `notEmpty finds album without images is empty`() {
		val sone = IdOnlySone("sone")
		val album = AlbumImpl(sone)
		assertThat(notEmpty(album), equalTo(false))
	}

	@Test
	fun `notEmpty finds album with one inserted image is not empty`() {
		val sone = IdOnlySone("sone")
		val album = AlbumImpl(sone)
		album.addImage(createImage(sone, "1", "key"))
		assertThat(notEmpty(album), equalTo(true))
	}

	@Test
	fun `notEmpty finds album with one not-inserted image is empty`() {
		val sone = IdOnlySone("sone")
		val album = AlbumImpl(sone)
		album.addImage(createImage(sone, "1"))
		assertThat(notEmpty(album), equalTo(false))
	}

	@Test
	fun `notEmpty finds album with empty subalbums is empty`() {
		val sone = IdOnlySone("sone")
		val album = AlbumImpl(sone)
		val firstNestedAlbum = AlbumImpl(sone)
		album.addAlbum(firstNestedAlbum)
		assertThat(notEmpty(album), equalTo(false))
	}

	@Test
	fun `notEmpty finds album with subalbum with not inserted image is empty`() {
		val sone = IdOnlySone("sone")
		val album = AlbumImpl(sone)
		val firstNestedAlbum = AlbumImpl(sone)
		firstNestedAlbum.addImage(createImage(sone, "1"))
		album.addAlbum(firstNestedAlbum)
		assertThat(notEmpty(album), equalTo(false))
	}

	@Test
	fun `notEmpty finds album with subalbum with inserted image is not empty`() {
		val sone = IdOnlySone("sone")
		val album = AlbumImpl(sone)
		val firstNestedAlbum = AlbumImpl(sone)
		firstNestedAlbum.addImage(createImage(sone, "1", "key"))
		album.addAlbum(firstNestedAlbum)
		assertThat(notEmpty(album), equalTo(true))
	}

	@Test
	fun `allImages returns images from album`() {
		val sone = IdOnlySone("sone")
		val album = AlbumImpl(sone)
		val image1 = createImage(sone, "1").also(album::addImage)
		val image2 = createImage(sone, "2").also(album::addImage)
		assertThat(album.allImages, contains(image1, image2))
	}

	@Test
	fun `allImages returns images from subalbum`() {
		val sone = IdOnlySone("sone")
		val album1 = AlbumImpl(sone)
		val album2 = AlbumImpl(sone).also(album1::addAlbum)
		val image1 = createImage(sone, "1").also(album1::addImage)
		val image2 = createImage(sone, "2").also(album2::addImage)
		assertThat(album1.allImages, contains(image1, image2))
	}

}
