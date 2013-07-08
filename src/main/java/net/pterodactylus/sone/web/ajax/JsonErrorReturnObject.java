/*
 * © 2013 xplosion interactive
 */

package net.pterodactylus.sone.web.ajax;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * {@link JsonReturnObject} that signals an error has occured.
 *
 * @author <a href="mailto:d.roden@xplosion.de">David Roden</a>
 */
public class JsonErrorReturnObject extends JsonReturnObject {

	/** The error that has occured. */
	@JsonProperty
	private final String error;

	/**
	 * Creates a new error JSON return object.
	 *
	 * @param error
	 * 		The error that occured
	 */
	public JsonErrorReturnObject(String error) {
		super(false);
		this.error = error;
	}

}
