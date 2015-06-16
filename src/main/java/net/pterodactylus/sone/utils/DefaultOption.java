package net.pterodactylus.sone.utils;

import com.google.common.base.Predicate;

/**
 * Basic implementation of an {@link Option}.
 *
 * @param <T>
 *            The type of the option
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DefaultOption<T> implements Option<T> {

	/** The default value. */
	private final T defaultValue;

	/** The current value. */
	private volatile T value;

	/** The validator. */
	private Predicate<T> validator;

	/**
	 * Creates a new default option.
	 *
	 * @param defaultValue
	 *            The default value of the option
	 */
	public DefaultOption(T defaultValue) {
		this(defaultValue, null);
	}

	/**
	 * Creates a new default option.
	 *
	 * @param defaultValue
	 *            The default value of the option
	 * @param validator
	 *            The validator for value validation (may be {@code null})
	 */
	public DefaultOption(T defaultValue, Predicate<T> validator) {
		this.defaultValue = defaultValue;
		this.validator = validator;
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
	@Override
	public boolean validate(T value) {
		return (validator == null) || (value == null) || validator.apply(value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void set(T value) {
		if ((value != null) && (validator != null) && (!validator.apply(value))) {
			throw new IllegalArgumentException("New Value (" + value + ") could not be validated.");
		}
		this.value = value;
	}

}
