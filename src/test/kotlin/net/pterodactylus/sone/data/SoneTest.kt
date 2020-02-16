/**
 * Sone - SoneTest.kt - Copyright © 2020 David ‘Bombe’ Roden
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
import net.pterodactylus.sone.test.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.*
import kotlin.test.*

/**
 * Unit test for functions in Sone.
 */
class SoneTest {

	@Test
	fun `nice name comparator correctly compares Sones by their nice name`() {
		val sone1 = object : IdOnlySone("1") {
			override fun getProfile() = Profile(this).apply { firstName = "Left" }
		}
		val sone2 = object : IdOnlySone("2") {
			override fun getProfile() = Profile(this).apply { firstName = "Right" }
		}
		assertThat(niceNameComparator.compare(sone1, sone2), lessThan(0))
	}

	@Test
	fun `nice name comparator correctly compares Sones by their ID if nice name is the same`() {
		val sone1 = object : IdOnlySone("1") {
			override fun getProfile() = Profile(this).apply { firstName = "Left" }
		}
		val sone2 = object : IdOnlySone("2") {
			override fun getProfile() = Profile(this).apply { firstName = "Left" }
		}
		assertThat(niceNameComparator.compare(sone1, sone2), lessThan(0))
	}

	@Test
	fun `nice name comparator treats Sones as equal if nice name and ID are the same`() {
		val sone1 = object : IdOnlySone("1") {
			override fun getProfile() = Profile(this).apply { firstName = "Left" }
		}
		val sone2 = object : IdOnlySone("1") {
			override fun getProfile() = Profile(this).apply { firstName = "Left" }
		}
		assertThat(niceNameComparator.compare(sone1, sone2), equalTo(0))
	}

	@Test
	fun `last activity comparator correctly compares Sones by last activity`() {
		val sone1 = object : IdOnlySone("1") {
			override fun getTime() = 1000L
		}
		val sone2 = object : IdOnlySone("2") {
			override fun getTime() = 2000L
		}
		assertThat(lastActivityComparator.compare(sone1, sone2), greaterThan(0))
	}

	@Test
	fun `last activity comparator treats Sones as equal if last activity is the same`() {
		val sone1 = object : IdOnlySone("1") {
			override fun getTime() = 1000L
		}
		val sone2 = object : IdOnlySone("2") {
			override fun getTime() = 1000L
		}
		assertThat(lastActivityComparator.compare(sone1, sone2), equalTo(0))
	}

	@Test
	fun `post count comparator sorts sones with different number of posts correctly`() {
		val sone1 = object : IdOnlySone("1") {
			override fun getPosts() = listOf(createPost(), createPost())
		}
		val sone2 = object : IdOnlySone("2") {
			override fun getPosts() = listOf(createPost(), createPost(), createPost())
		}
		assertThat(postCountComparator.compare(sone1, sone2), greaterThan(0))
	}

	@Test
	fun `post count comparator compares replies if posts are not different`() {
		val sone1 = object : IdOnlySone("1") {
			override fun getPosts() = listOf(createPost(), createPost())
			override fun getReplies() = setOf(emptyPostReply(), emptyPostReply())
		}
		val sone2 = object : IdOnlySone("2") {
			override fun getPosts() = listOf(createPost(), createPost())
			override fun getReplies() = setOf(emptyPostReply(), emptyPostReply(), emptyPostReply())
		}
		assertThat(postCountComparator.compare(sone1, sone2), greaterThan(0))
	}

	@Test
	fun `post count comparator sorts sone with same amount of posts and replies as equal`() {
		val sone1 = object : IdOnlySone("1") {
			override fun getPosts() = listOf(createPost(), createPost())
			override fun getReplies() = setOf(emptyPostReply(), emptyPostReply())
		}
		val sone2 = object : IdOnlySone("2") {
			override fun getPosts() = listOf(createPost(), createPost())
			override fun getReplies() = setOf(emptyPostReply(), emptyPostReply())
		}
		assertThat(postCountComparator.compare(sone1, sone2), equalTo(0))
	}

	@Test
	fun `image count comparator sorts Sones correctly if number of images is different`() {
		val sone1 = object : IdOnlySone("1") {
			override fun getRootAlbum() = AlbumImpl(this).also { it.addImage(createImage(this)) }
		}
		val sone2 = object : IdOnlySone("2") {
			override fun getRootAlbum() = AlbumImpl(this).also { it.addImage(createImage(this)); it.addImage(createImage(this)) }
		}
		assertThat(imageCountComparator.compare(sone1, sone2), greaterThan(0))
	}

	@Test
	fun `image count comparator treats Sones as equal if number of images is the same`() {
		val sone1 = object : IdOnlySone("1") {
			override fun getRootAlbum() = AlbumImpl(this).also { it.addImage(createImage(this)) }
		}
		val sone2 = object : IdOnlySone("2") {
			override fun getRootAlbum() = AlbumImpl(this).also { it.addImage(createImage(this)) }
		}
		assertThat(imageCountComparator.compare(sone1, sone2), equalTo(0))
	}

}
