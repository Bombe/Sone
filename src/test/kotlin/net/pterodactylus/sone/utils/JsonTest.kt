package net.pterodactylus.sone.utils

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.instanceOf
import org.junit.Test

/**
 * Unit test for JSON utilities.
 */
class JsonTest {

	@Test
	fun `object node is created correctly`() {
		val objectNode = jsonObject {
			put("foo", "bar")
		}
		assertThat(objectNode, instanceOf(ObjectNode::class.java))
		assertThat(objectNode.toString(), equalTo("{\"foo\":\"bar\"}"))
	}

	@Test
	fun `array node is created correctly`() {
		val arrayNode = listOf(
				jsonObject { put("foo", "bar") },
				jsonObject { put("baz", "quo") }
		).toArray()
		assertThat(arrayNode, instanceOf(ArrayNode::class.java))
		assertThat(arrayNode.toString(), equalTo("[{\"foo\":\"bar\"},{\"baz\":\"quo\"}]"))
	}

	@Test
	fun `array is created correctly for strings`() {
	    val arrayNode = jsonArray("foo", "bar", "baz")
		assertThat(arrayNode.toString(), equalTo("[\"foo\",\"bar\",\"baz\"]"))
	}

}
