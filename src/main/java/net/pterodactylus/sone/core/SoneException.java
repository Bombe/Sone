/*
 * FreenetSone - SoneException.java - Copyright © 2010 David Roden
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

package net.pterodactylus.sone.core;

/**
 * A Sone exception.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneException extends Exception {

	/**
	 * Defines the different error. This is an enum instead of custom exceptions
	 * to keep the number of exceptions down. Specialized exceptions might still
	 * exist, though.
	 */
	public static enum Type {

		/** An invalid Sone name was specified. */
		INVALID_SONE_NAME,

		/** An invalid URI was specified. */
		INVALID_URI,

	}

	/** The type of the exception. */
	private final Type type;

	/**
	 * Creates a new Sone exception.
	 *
	 * @param type
	 *            The type of the occured error
	 */
	public SoneException(Type type) {
		this.type = type;
	}

	/**
	 * Creates a new Sone exception.
	 *
	 * @param type
	 *            The type of the occured error
	 * @param message
	 *            The message of the exception
	 */
	public SoneException(Type type, String message) {
		super(message);
		this.type = type;
	}

	/**
	 * Creates a new Sone exception.
	 *
	 * @param type
	 *            The type of the occured error
	 * @param cause
	 *            The cause of the exception
	 */
	public SoneException(Type type, Throwable cause) {
		super(cause);
		this.type = type;
	}

	/**
	 * Creates a new Sone exception.
	 *
	 * @param type
	 *            The type of the occured error
	 * @param message
	 *            The message of the exception
	 * @param cause
	 *            The cause of the exception
	 */
	public SoneException(Type type, String message, Throwable cause) {
		super(message, cause);
		this.type = type;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the type of this exception.
	 *
	 * @return The type of this exception (may be {@code null})
	 */
	public Type getType() {
		return type;
	}

}
