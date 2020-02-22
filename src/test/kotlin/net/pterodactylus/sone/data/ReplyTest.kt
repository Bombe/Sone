/**
 * Sone - ReplyTest.kt - Copyright © 2020 David ‘Bombe’ Roden
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

import net.pterodactylus.sone.test.emptyPostReply
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.lessThan
import java.util.concurrent.TimeUnit.DAYS
import kotlin.test.Test

class ReplyTest {

	@Test
	fun `newestReplyFirst comparator returns less-than 0 is first reply is newer than second`() {
		val newerReply = emptyPostReply(time = 2000)
		val olderReply = emptyPostReply(time = 1000)
		assertThat(newestReplyFirst.compare(newerReply, olderReply), lessThan(0))
	}

	@Test
	fun `newestReplyFirst comparator returns greater-than 0 is first reply is older than second`() {
		val newerReply = emptyPostReply(time = 2000)
		val olderReply = emptyPostReply(time = 1000)
		assertThat(newestReplyFirst.compare(olderReply, newerReply), greaterThan(0))
	}

	@Test
	fun `newestReplyFirst comparator returns 0 is first and second reply have same age`() {
		val reply1 = emptyPostReply(time = 1000)
		val reply2 = emptyPostReply(time = 1000)
		assertThat(newestReplyFirst.compare(reply1, reply2), equalTo(0))
	}

	@Test
	fun `noFutureReply filter recognizes reply from the future`() {
		val futureReply = emptyPostReply(time = System.currentTimeMillis() + DAYS.toMillis(1))
		assertThat(noFutureReply(futureReply), equalTo(false))
	}

	@Test
	fun `noFutureReply filter recognizes reply from the present`() {
		val futureReply = emptyPostReply(time = System.currentTimeMillis())
		assertThat(noFutureReply(futureReply), equalTo(true))
	}

}
