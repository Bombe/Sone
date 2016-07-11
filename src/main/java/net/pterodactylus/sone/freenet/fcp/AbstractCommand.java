/*
 * Sone - AbstractCommand.java - Copyright © 2011–2016 David Roden
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

package net.pterodactylus.sone.freenet.fcp;

import freenet.node.FSParseException;
import freenet.support.SimpleFieldSet;

/**
 * Basic implementation of a {@link Command} with various helper methods to
 * simplify processing of input parameters.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public abstract class AbstractCommand implements Command {

	//
	// PROTECTED METHODS
	//

	/**
	 * Returns a String value from the given simple field set.
	 *
	 * @param simpleFieldSet
	 *            The simple field set to get the value from
	 * @param key
	 *            The key of the value
	 * @return The String value
	 * @throws FcpException
	 *             if there is no value for the given key in the simple field
	 *             set, or the value can not be converted to a String
	 */
	protected static String getString(SimpleFieldSet simpleFieldSet, String key) throws FcpException {
		try {
			return simpleFieldSet.getString(key);
		} catch (FSParseException fspe1) {
			throw new FcpException("Could not get parameter “" + key + "” as String.", fspe1);
		}
	}

	/**
	 * Returns an int value from the given simple field set.
	 *
	 * @param simpleFieldSet
	 *            The simple field set to get the value from
	 * @param key
	 *            The key of the value
	 * @return The int value
	 * @throws FcpException
	 *             if there is no value for the given key in the simple field
	 *             set, or the value can not be converted to an int
	 */
	protected static int getInt(SimpleFieldSet simpleFieldSet, String key) throws FcpException {
		try {
			return simpleFieldSet.getInt(key);
		} catch (FSParseException fspe1) {
			throw new FcpException("Could not get parameter “" + key + "” as int.", fspe1);
		}
	}

	/**
	 * Returns an int value from the given simple field set, returning a default
	 * value if the value can not be found or converted.
	 *
	 * @param simpleFieldSet
	 *            The simple field set to get the value from
	 * @param key
	 *            The key of the value
	 * @param defaultValue
	 *            The default value
	 * @return The int value
	 */
	protected static int getInt(SimpleFieldSet simpleFieldSet, String key, int defaultValue) {
		return simpleFieldSet.getInt(key, defaultValue);
	}

	/**
	 * Returns a boolean value from the given simple field set.
	 *
	 * @param simpleFieldSet
	 *            The simple field set to get the value from
	 * @param key
	 *            The key of the value
	 * @return The boolean value
	 * @throws FcpException
	 *             if there is no value for the given key in the simple field
	 *             set, or the value can not be converted to a boolean
	 */
	protected static boolean getBoolean(SimpleFieldSet simpleFieldSet, String key) throws FcpException {
		try {
			return simpleFieldSet.getBoolean(key);
		} catch (FSParseException fspe1) {
			throw new FcpException("Could not get parameter “" + key + "” as boolean.", fspe1);
		}
	}

	/**
	 * Returns a boolean value from the given simple field set, returning a
	 * default value if the value can not be found or converted.
	 *
	 * @param simpleFieldSet
	 *            The simple field set to get the value from
	 * @param key
	 *            The key of the value
	 * @param defaultValue
	 *            The default value
	 * @return The boolean value
	 */
	protected static boolean getBoolean(SimpleFieldSet simpleFieldSet, String key, boolean defaultValue) {
		return simpleFieldSet.getBoolean(key, defaultValue);
	}

}
