/*
 * Sone - Options.java - Copyright © 2010–2020 David Roden
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.pterodactylus.sone.utils.Option;

/**
 * Stores various options that influence Sone’s behaviour.
 */
public class Options {

	/** Holds all {@link Boolean} {@link Option}s. */
	private final Map<String, Option<Boolean>> booleanOptions = Collections.synchronizedMap(new HashMap<String, Option<Boolean>>());

	/** Holds all {@link Integer} {@link Option}s. */
	private final Map<String, Option<Integer>> integerOptions = Collections.synchronizedMap(new HashMap<String, Option<Integer>>());

	/** Holds all {@link String} {@link Option}s. */
	private final Map<String, Option<String>> stringOptions = Collections.synchronizedMap(new HashMap<String, Option<String>>());

	/** Holds all {@link Enum} {@link Option}s. */
	private final Map<String, Option<? extends Enum<?>>> enumOptions = Collections.synchronizedMap(new HashMap<String, Option<? extends Enum<?>>>());

	/**
	 * Adds a boolean option.
	 *
	 * @param name
	 *            The name of the option
	 * @param booleanOption
	 *            The option
	 * @return The given option
	 */
	public Option<Boolean> addBooleanOption(String name, Option<Boolean> booleanOption) {
		booleanOptions.put(name, booleanOption);
		return booleanOption;
	}

	/**
	 * Returns the boolean option with the given name.
	 *
	 * @param name
	 *            The name of the option
	 * @return The option, or {@code null} if there is no option with the given
	 *         name
	 */
	public Option<Boolean> getBooleanOption(String name) {
		return booleanOptions.get(name);
	}

	/**
	 * Adds an {@link Integer} {@link Option}.
	 *
	 * @param name
	 *            The name of the option
	 * @param integerOption
	 *            The option
	 * @return The given option
	 */
	public Option<Integer> addIntegerOption(String name, Option<Integer> integerOption) {
		integerOptions.put(name, integerOption);
		return integerOption;
	}

	/**
	 * Returns an {@link Integer} {@link Option}.
	 *
	 * @param name
	 *            The name of the integer option to get
	 * @return The integer option, or {@code null} if there is no option with
	 *         the given name
	 */
	public Option<Integer> getIntegerOption(String name) {
		return integerOptions.get(name);
	}

	/**
	 * Adds a {@link String} {@link Option}.
	 *
	 * @param name
	 *            The name of the option
	 * @param stringOption
	 *            The option
	 * @return The given option
	 */
	public Option<String> addStringOption(String name, Option<String> stringOption) {
		stringOptions.put(name, stringOption);
		return stringOption;
	}

	/**
	 * Returns a {@link String} {@link Option}.
	 *
	 * @param name
	 *            The name of the string option to get
	 * @return The string option, or {@code null} if there is no option with the
	 *         given name
	 */
	public Option<String> getStringOption(String name) {
		return stringOptions.get(name);
	}

	/**
	 * Adds an {@link Enum} {@link Option}.
	 *
	 * @param <T>
	 *            The enum type
	 * @param name
	 *            The name of the option
	 * @param enumOption
	 *            The option
	 * @return The given option
	 */
	public <T extends Enum<T>> Option<T> addEnumOption(String name, Option<T> enumOption) {
		enumOptions.put(name, enumOption);
		return enumOption;
	}

	/**
	 * Returns a {@link Enum} {@link Option}. As the type can probably not be
	 * interred correctly you could help the compiler by calling this method
	 * like this:
	 * <p>
	 *
	 * <pre>
	 * options.&lt;SomeEnum&gt; getEnumOption(&quot;SomeEnumOption&quot;).get();
	 * </pre>
	 *
	 * @param <T>
	 *            The enum type
	 * @param name
	 *            The name of the option
	 * @return The enum option, or {@code null} if there is no enum option with
	 *         the given name
	 */
	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> Option<T> getEnumOption(String name) {
		return (Option<T>) enumOptions.get(name);
	}

}
