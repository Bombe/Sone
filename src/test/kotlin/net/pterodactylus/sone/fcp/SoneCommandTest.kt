package net.pterodactylus.sone.fcp

import com.google.common.base.Optional
import com.google.common.base.Optional.absent
import freenet.support.SimpleFieldSet
import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.data.Post
import net.pterodactylus.sone.data.PostReply
import net.pterodactylus.sone.data.Profile
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.freenet.fcp.FcpException
import net.pterodactylus.sone.template.SoneAccessor
import net.pterodactylus.sone.test.OneByOneMatcher
import net.pterodactylus.sone.test.asOptional
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import org.junit.Before
import org.junit.Rule
import org.junit.rules.ExpectedException
import org.mockito.ArgumentMatchers.anyString

/**
 * Base class for Sone FCP command tests.
 */
abstract class SoneCommandTest {

	@Rule @JvmField val expectedException = ExpectedException.none()!!

	protected val core = mock<Core>()
	protected val command: AbstractSoneCommand by lazy { createCommand(core) }

	protected val parameters = SimpleFieldSet(true)
	protected val localSone = mock<Sone>().apply {
		whenever(isLocal).thenReturn(true)
	}
	protected val remoteSone = mock<Sone>()

	protected abstract fun createCommand(core: Core): AbstractSoneCommand

	@Before
	fun setupCore() {
		whenever(core.getSone(anyString())).thenReturn(absent())
		whenever(core.getPost(anyString())).thenReturn(absent())
		whenever(core.getPostReply(anyString())).thenReturn(absent())
	}

	protected fun createSone(id: String, name: String, firstName: String, lastName: String, time: Long) = mock<Sone>().apply {
		whenever(this.id).thenReturn(id)
		whenever(this.name).thenReturn(name)
		whenever(profile).thenReturn(Profile(this).apply {
			this.firstName = firstName
			this.lastName = lastName
		})
		whenever(this.time).thenReturn(time)
	}

	protected fun createPost(id: String, sone: Sone, recipientId: String?, time: Long, text: String) = mock<Post>().apply {
		whenever(this.id).thenReturn(id)
		whenever(this.sone).thenReturn(sone)
		whenever(this.recipientId).thenReturn(recipientId.asOptional())
		whenever(this.time).thenReturn(time)
		whenever(this.text).thenReturn(text)
	}

	protected fun createReply(id: String, sone: Sone, post: Post, time: Long, text: String) = mock<PostReply>().apply {
		whenever(this.id).thenReturn(id)
		whenever(this.sone).thenReturn(sone)
		whenever(this.post).thenReturn(post.asOptional())
		whenever(this.time).thenReturn(time)
		whenever(this.text).thenReturn(text)
	}

	protected fun executeCommandAndExpectFcpException() {
		expectedException.expect(FcpException::class.java)
		command.execute(parameters)
	}

	protected fun requestWithoutAnyParameterResultsInFcpException() {
		expectedException.expect(FcpException::class.java)
		command.execute(parameters)
	}

	protected fun requestWithEmptySoneParameterResultsInFcpException() {
		parameters += "Sone" to null
		executeCommandAndExpectFcpException()
	}

	protected fun requestWithInvalidSoneParameterResultsInFcpException() {
		parameters += "Sone" to "InvalidSoneId"
		executeCommandAndExpectFcpException()
	}

	fun requestWithValidRemoteSoneParameterResultsInFcpException() {
		parameters += "Sone" to "RemoteSoneId"
		whenever(core.getSone("RemoteSoneId")).thenReturn(Optional.of(remoteSone))
		executeCommandAndExpectFcpException()
	}

	protected operator fun SimpleFieldSet.plusAssign(keyValue: Pair<String, String?>) = putSingle(keyValue.first, keyValue.second)
	protected fun SimpleFieldSet.parsePost(prefix: String) = parseFromSimpleFieldSet(prefix, "ID", "Sone", "Recipient", "Time", "Text")
	protected fun SimpleFieldSet.parseReply(prefix: String) = parseFromSimpleFieldSet(prefix, "ID", "Sone", "Time", "Text")
	protected fun SimpleFieldSet.parseSone(prefix: String) = parseFromSimpleFieldSet(prefix, "ID", "Name", "NiceName", "LastUpdated", "Followed") +
			(0 until this["${prefix}Field.Count"].toInt()).map {
				("Field." + this["${prefix}Field.$it.Name"]) to this["${prefix}Field.$it.Value"]
			}

	private fun SimpleFieldSet.parseFromSimpleFieldSet(prefix: String, vararg fields: String) = listOf(*fields)
			.map { it to (get(prefix + it) as String?) }
			.toMap()

	protected fun matchesPost(post: Post) = OneByOneMatcher<Map<String, String?>>().apply {
		expect("ID", post.id) { it["ID"] }
		expect("Sone", post.sone.id) { it["Sone"] }
		expect("recipient", post.recipientId.orNull()) { it["Recipient"] }
		expect("time", post.time.toString()) { it["Time"] }
		expect("text", post.text.replace("\\", "\\\\").replace("\r", "\\r").replace("\n", "\\n")) { it["Text"] }
	}

	protected fun matchesReply(reply: PostReply) = OneByOneMatcher<Map<String, String?>>().apply {
		expect("ID", reply.id) { it["ID"] }
		expect("Sone", reply.sone.id) { it["Sone"] }
		expect("time", reply.time.toString()) { it["Time"] }
		expect("text", reply.text.replace("\\", "\\\\").replace("\r", "\\r").replace("\n", "\\n")) { it["Text"] }
	}

	protected fun matchesSone(sone: Sone) = OneByOneMatcher<Map<String, String?>>().apply {
		expect("ID", sone.id) { it["ID"] }
		expect("name", sone.name) { it["Name"] }
		expect("last updated", sone.time.toString()) { it["LastUpdated"] }
		expect("nice name", SoneAccessor.getNiceName(sone)) { it["NiceName"] }
		sone.profile.fields.forEach { field ->
			expect("field: ${field.name}", field.value) { it["Field.${field.name}"] }
		}
	}

}
