/*
 * Sone - SimpleFieldSetBuilder.java - Copyright © 2011 David Roden
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

package net.pterodactylus.sone.freenet;

import net.pterodactylus.util.validation.Validation;
import freenet.support.SimpleFieldSet;

/**
 * Helper class to construct {@link SimpleFieldSet} objects in a single call.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SimpleFieldSetBuilder {

	/** The simple field set that is being constructed. */
	private final SimpleFieldSet simpleFieldSet;

	/**
	 * Creates a new simple field set builder using a new, empty simple field
	 * set.
	 */
	public SimpleFieldSetBuilder() {
		this(new SimpleFieldSet(true));
	}

	/**
	 * Creates a new simple field set builder that will return the given simple
	 * field set on {@link #get()}.
	 *
	 * @param simpleFieldSet
	 *            The simple field set to build
	 */
	public SimpleFieldSetBuilder(SimpleFieldSet simpleFieldSet) {
		Validation.begin().isNotNull("Simple Field Set", simpleFieldSet).check();
		this.simpleFieldSet = simpleFieldSet;
	}

	/**
	 * Returns the constructed simple field set.
	 *
	 * @return The construct simple field set
	 */
	public SimpleFieldSet get() {
		return simpleFieldSet;
	}

	/**
	 * Copies the given simple field set into the simple field set being built
	 * in this builder, overwriting all previously existing values.
	 *
	 * @param simpleFieldSet
	 *            The simple field set to copy
	 * @return This simple field set builder
	 */
	public SimpleFieldSetBuilder put(@SuppressWarnings("hiding") SimpleFieldSet simpleFieldSet) {
		this.simpleFieldSet.putAllOverwrite(simpleFieldSet);
		return this;
	}

	/**
	 * Stores the given value under the given key, overwriting any previous
	 * value.
	 *
	 * @param key
	 *            The key of the value
	 * @param value
	 *            The value to store
	 * @return This simple field set builder
	 */
	public SimpleFieldSetBuilder put(String key, String value) {
		simpleFieldSet.putOverwrite(key, value);
		return this;
	}

	/**
	 * Stores the given value under the given key, overwriting any previous
	 * value.
	 *
	 * @param key
	 *            The key of the value
	 * @param value
	 *            The value to store
	 * @return This simple field set builder
	 */
	public SimpleFieldSetBuilder put(String key, int value) {
		simpleFieldSet.put(key, value);
		return this;
	}

	/**
	 * Stores the given value under the given key, overwriting any previous
	 * value.
	 *
	 * @param key
	 *            The key of the value
	 * @param value
	 *            The value to store
	 * @return This simple field set builder
	 */
	public SimpleFieldSetBuilder put(String key, long value) {
		simpleFieldSet.put(key, value);
		return this;
	}

}
