package net.pterodactylus.sone.template

import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.util.template.TemplateContext
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test

/**
 * Unit test for [ReplyAccessor].
 */
class ReplyAccessorTest {

	private val core = mock<Core>()
	private val accessor = ReplyAccessor(core)
	private val templateContext = mock<TemplateContext>()
	private val reply = mock<PostReply>()
	private val currentSone = mock<Sone>()

	@Before
	fun setupReply() {
		whenever(reply.id).thenReturn("reply-id")
	}

	@Before
	fun setupTemplateContext() {
		whenever(templateContext.get("currentSone")).thenReturn(currentSone)
	}

	@Test
	fun `returns the likes correctly`() {
		val sones = setOf(mock<Sone>(), mock(), mock())
		whenever(core.getLikes(reply)).thenReturn(sones)
		assertThat(accessor.get(templateContext, reply, "likes"), equalTo<Any>(sones))
	}

	@Test
	fun `returns that the reply is not liked if the current sone is null`() {
		whenever(templateContext.get("currentSone")).thenReturn(null)
		assertThat(accessor.get(templateContext, reply, "liked"), equalTo<Any>(false))
	}

	@Test
	fun `returns that the reply is not liked if the current sone does not like the reply`() {
		assertThat(accessor.get(templateContext, reply, "liked"), equalTo<Any>(false))
	}

	@Test
	fun `returns that the reply is liked if the current sone does like the reply`() {
		whenever(currentSone.isLikedReplyId("reply-id")).thenReturn(true)
		assertThat(accessor.get(templateContext, reply, "liked"), equalTo<Any>(true))
	}

	@Test
	fun `returns that the reply is new if the reply is not known`() {
		assertThat(accessor.get(templateContext, reply, "new"), equalTo<Any>(true))
	}

	@Test
	fun `returns that the reply is not new if the reply is known`() {
		whenever(reply.isKnown).thenReturn(true)
		assertThat(accessor.get(templateContext, reply, "new"), equalTo<Any>(false))
	}

	@Test
	fun `return that a reply is not loaded if its sone is null`() {
		assertThat(accessor.get(templateContext, reply, "loaded"), equalTo<Any>(false))
	}

	@Test
	fun `return that a reply is loaded if its sone is not null`() {
		whenever(reply.sone).thenReturn(mock())
		assertThat(accessor.get(templateContext, reply, "loaded"), equalTo<Any>(true))
	}

	@Test
	fun `reflection accessor is used for all other members`() {
		assertThat(accessor.get(templateContext, reply, "hashCode"), equalTo<Any>(reply.hashCode()))
	}

}
