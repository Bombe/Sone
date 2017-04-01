package net.pterodactylus.sone.utils

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory.instance
import com.fasterxml.jackson.databind.node.ObjectNode

fun jsonObject(block: ObjectNode.() -> Unit): ObjectNode = ObjectNode(instance).apply(block)

fun Iterable<ObjectNode>.toArray(): ArrayNode = fold(ArrayNode(instance), ArrayNode::add)
