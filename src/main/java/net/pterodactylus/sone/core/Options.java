/*
 * Sone - Options.java - Copyright © 2010 David Roden
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.pterodactylus.util.validation.Validator;

/**
 * Stores various options that influence Sone’s behaviour.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Options {

	/**
	 * Contains current and default value of an option.
	 *
	 * @param <T>
	 *            The type of the option
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public static interface Option<T> {

		/**
		 * Returns the default value of the option.
		 *
		 * @return The default value of the option
		 */
		public T getDefault();

		/**
		 * Returns the current value of the option. If the current value is not
		 * set (usually {@code null}), the default value is returned.
		 *
		 * @return The current value of the option
		 */
		public T get();

		/**
		 * Returns the real value of the option. This will also return an unset
		 * value (usually {@code null})!
		 *
		 * @return The real value of the option
		 */
		public T getReal();

		/**
		 * Validates the given value. Note that {@code null} is always a valid
		 * value!
		 *
		 * @param value
		 *            The value to validate
		 * @return {@code true} if this option does not have a {@link Validator}
		 *         , or the {@link Validator} validates this object, {@code
		 *         false} otherwise
		 */
		public boolean validate(T value);

		/**
		 * Sets the current value of the option.
		 *
		 * @param value
		 *            The new value of the option
		 * @throws IllegalArgumentException
		 *             if the value is not valid for this option
		 */
		public void set(T value) throws IllegalArgumentException;

	}

	/**
	 * Interface for objects that want to be notified when an option changes its
	 * value.
	 *
	 * @param <T>
	 *            The type of the option
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public static interface OptionWatcher<T> {

		/**
		 * Notifies an object that an option has been changed.
		 *
		 * @param option
		 *            The option that has changed
		 * @param oldValue
		 *            The old value of the option
		 * @param newValue
		 *            The new value of the option
		 */
		public void optionChanged(Option<T> option, T oldValue, T newValue);

	}

	/**
	 * Basic implementation of an {@link Option} that notifies an
	 * {@link OptionWatcher} if the value changes.
	 *
	 * @param <T>
	 *            The type of the option
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public static class DefaultOption<T> implements Option<T> {

		/** The default value. */
		private final T defaultValue;

		/** The current value. */
		private volatile T value;

		/** The validator. */
		private Validator<T> validator;

		/** The option watcher. */
		private final List<OptionWatcher<T>> optionWatchers = new ArrayList<OptionWatcher<T>>();

		/**
		 * Creates a new default option.
		 *
		 * @param defaultValue
		 *            The default value of the option
		 * @param optionWatchers
		 *            The option watchers
		 */
		public DefaultOption(T defaultValue, OptionWatcher<T>... optionWatchers) {
			this(defaultValue, null, optionWatchers);
		}

		/**
		 * Creates a new default option.
		 *
		 * @param defaultValue
		 *            The default value of the option
		 * @param validator
		 *            The validator for value validation
		 * @param optionWatchers
		 *            The option watchers
		 */
		public DefaultOption(T defaultValue, Validator<T> validator, OptionWatcher<T>... optionWatchers) {
			this.defaultValue = defaultValue;
			this.validator = validator;
			this.optionWatchers.addAll(Arrays.asList(optionWatchers));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T getDefault() {
			return defaultValue;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T get() {
			return (value != null) ? value : defaultValue;
		}

		/**
		 * Returns the real value of the option. This will also return an unset
		 * value (usually {@code null})!
		 *
		 * @return The real value of the option
		 */
		@Override
		public T getReal() {
			return value;
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean validate(T value) {
			return (validator == null) || (value == null) || validator.validate(value);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void set(T value) {
			if ((value != null) && (validator != null) && (!validator.validate(value))) {
				throw new IllegalArgumentException("New Value (" + value + ") could not be validated.");
			}
			T oldValue = this.value;
			this.value = value;
			if (!get().equals(oldValue)) {
				for (OptionWatcher<T> optionWatcher : optionWatchers) {
					optionWatcher.optionChanged(this, oldValue, get());
				}
			}
		}

	}

	/** Holds all {@link Boolean} {@link Option}s. */
	private final Map<String, Option<Boolean>> booleanOptions = Collections.synchronizedMap(new HashMap<String, Option<Boolean>>());

	/** Holds all {@link Integer} {@link Option}s. */
	private final Map<String, Option<Integer>> integerOptions = Collections.synchronizedMap(new HashMap<String, Option<Integer>>());

	/** Holds all {@link String} {@link Option}s. */
	private final Map<String, Option<String>> stringOptions = Collections.synchronizedMap(new HashMap<String, Option<String>>());

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

}
