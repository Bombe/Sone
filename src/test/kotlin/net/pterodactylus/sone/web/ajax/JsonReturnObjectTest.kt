package net.pterodactylus.sone.web.ajax

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test

/**
 * Unit test for [JsonReturnObject].
 */
class JsonReturnObjectTest {

	private val jsonReturnObject = JsonReturnObject(true)
	private val objectMapper = ObjectMapper()

	@Test
	fun `json object retains success status if true`() {
		assertThat(JsonReturnObject(true).isSuccess, equalTo(true))
	}

	@Test
	fun `json object retains success status if false`() {
		assertThat(JsonReturnObject(false).isSuccess, equalTo(false))
	}

	@Test
	fun `json object returns text nodes for string properties`() {
		jsonReturnObject.put("foo", "bar")
		assertThat(jsonReturnObject["foo"], equalTo<Any>(TextNode("bar")))
	}

	@Test
	fun `json object returns int nodes for int properties`() {
		jsonReturnObject.put("foo", 123)
		assertThat(jsonReturnObject["foo"], equalTo<Any>(IntNode(123)))
	}

	@Test
	fun `json object returns boolean nodes for boolean properties`() {
		jsonReturnObject.put("foo", true)
		assertThat(jsonReturnObject["foo"], equalTo<Any>(BooleanNode.TRUE))
	}

	@Test
	fun `json object returns json node for json properties`() {
		val objectNode = ObjectNode(JsonNodeFactory.instance)
		jsonReturnObject.put("foo", objectNode)
		assertThat(jsonReturnObject["foo"], equalTo<Any>(objectNode))
	}

	@Test
	fun `json object returns all properties`() {
		val objectNode = ObjectNode(JsonNodeFactory.instance)
		jsonReturnObject.put("text", "text")
		jsonReturnObject.put("int", 123)
		jsonReturnObject.put("boolean", true)
		jsonReturnObject.put("object", objectNode)
		assertThat(jsonReturnObject.content, equalTo<Any>(mapOf(
				"text" to TextNode("text"),
				"int" to IntNode(123),
				"boolean" to BooleanNode.TRUE,
				"object" to objectNode
		)))
	}

	@Test
	fun `json object is serialized correctly`() {
		val objectNode = ObjectNode(JsonNodeFactory.instance)
		jsonReturnObject.put("text", "text")
		jsonReturnObject.put("int", 123)
		jsonReturnObject.put("boolean", true)
		jsonReturnObject.put("object", objectNode)
		val json = objectMapper.writeValueAsString(jsonReturnObject)
		val parsedJson = objectMapper.readTree(json)
		assertThat(parsedJson, equalTo<JsonNode>(ObjectNode(JsonNodeFactory.instance).apply {
			put("success", true)
			put("text", "text")
			put("int", 123)
			put("boolean", true)
			put("object", objectNode)
		}))
	}

}
