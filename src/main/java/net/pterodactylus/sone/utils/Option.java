package net.pterodactylus.sone.utils;

/**
 * Contains current and default value of an option.
 *
 * @param <T>
 *            The type of the option
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface Option<T> {

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
	 * @return {@code true} if this option does not have a validator, or the
	 *         validator validates this object, {@code false} otherwise
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
