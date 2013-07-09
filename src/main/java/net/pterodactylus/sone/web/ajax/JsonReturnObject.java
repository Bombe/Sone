/*
 * © 2013 xplosion interactive
 */

package net.pterodactylus.sone.web.ajax;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

/**
 * JSON return object for AJAX requests.
 *
 * @author <a href="mailto:d.roden@xplosion.de">David Roden</a>
 */
public class JsonReturnObject {

	/** Whether the request was successful. */
	@JsonProperty
	private final boolean success;

	/** The returned values. */
	private final Map<String, JsonNode> content = Maps.newHashMap();

	/**
	 * Creates a new JSON return object.
	 *
	 * @param success
	 * 		{@code true} if the request was successful, {@code false} otherwise
	 */
	public JsonReturnObject(boolean success) {
		this.success = success;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns whether the request was successful.
	 *
	 * @return {@code true} if the request was successful, {@code false} otherwise
	 */
	@VisibleForTesting
	public boolean isSuccess() {
		return success;
	}

	/**
	 * Returns the value stored under the given key.
	 *
	 * @param key
	 * 		The key of the value to retrieve
	 * @return The value of the key, or {@code null} if there is no value for the
	 *         given key
	 */
	@VisibleForTesting
	public JsonNode get(String key) {
		return content.get(key);
	}

	/**
	 * Returns the content of this object for serialization.
	 *
	 * @return The content of this object
	 */
	@JsonAnyGetter
	public Map<String, JsonNode> getContent() {
		return content;
	}

	//
	// ACTIONS
	//

	/**
	 * Stores the given value under the given key.
	 *
	 * @param key
	 * 		The key under which to store the value
	 * @param value
	 * 		The value to store
	 * @return This JSON return object
	 */
	public JsonReturnObject put(String key, boolean value) {
		return put(key, BooleanNode.valueOf(value));
	}

	/**
	 * Stores the given value under the given key.
	 *
	 * @param key
	 * 		The key under which to store the value
	 * @param value
	 * 		The value to store
	 * @return This JSON return object
	 */
	public JsonReturnObject put(String key, int value) {
		return put(key, new IntNode(value));
	}

	/**
	 * Stores the given value under the given key.
	 *
	 * @param key
	 * 		The key under which to store the value
	 * @param value
	 * 		The value to store
	 * @return This JSON return object
	 */
	public JsonReturnObject put(String key, String value) {
		return put(key, new TextNode(value));
	}

	/**
	 * Stores the given value under the given key.
	 *
	 * @param key
	 * 		The key under which to store the value
	 * @param value
	 * 		The value to store
	 * @return This JSON return object
	 */
	public JsonReturnObject put(String key, JsonNode value) {
		content.put(key, value);
		return this;
	}

}
