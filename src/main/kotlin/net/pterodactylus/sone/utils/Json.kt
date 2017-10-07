package net.pterodactylus.sone.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory.instance
import com.fasterxml.jackson.databind.node.ObjectNode

fun jsonObject(block: ObjectNode.() -> Unit): ObjectNode = ObjectNode(instance).apply(block)

fun jsonObject(vararg properties: Pair<String, Any?>) = jsonObject {
	properties.forEach {
		it.second.let { value ->
			when (value) {
				is String -> put(it.first, value)
				is Int -> put(it.first, value)
				is Long -> put(it.first, value)
				is Boolean -> put(it.first, value)
				else -> Unit
			}
		}
	}
}

fun jsonArray(vararg objects: String?): ArrayNode = objects.fold(ArrayNode(instance), ArrayNode::add)
fun jsonArray(vararg objects: JsonNode?): ArrayNode = objects.fold(ArrayNode(instance), ArrayNode::add)

fun Iterable<ObjectNode>.toArray(): ArrayNode = fold(ArrayNode(instance), ArrayNode::add)
