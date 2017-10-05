package net.pterodactylus.sone.web.ajax

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.TextNode

/**
 * JSON return object for AJAX requests.
 */
open class JsonReturnObject(val isSuccess: Boolean) {

	private val values = mutableMapOf<String, JsonNode?>()

	val content: Map<String, Any?>
		@JsonAnyGetter get() = values

	operator fun get(key: String) = values[key]

	fun put(key: String, value: String?) = apply {
		values[key] = TextNode.valueOf(value)
	}

	fun put(key: String, value: Int) = apply {
		values[key] = IntNode.valueOf(value)
	}

	fun put(key: String, value: Boolean) = apply {
		values[key] = BooleanNode.valueOf(value)
	}

	fun put(key: String, value: JsonNode) = apply {
		values[key] = value
	}

	override fun hashCode(): Int {
		return isSuccess.hashCode() xor content.hashCode()
	}

	override fun equals(other: Any?) =
			(other as? JsonReturnObject)?.let {
				it.isSuccess == isSuccess && it.content == content
			} ?: false

}
