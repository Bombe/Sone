package net.pterodactylus.sone;

import java.util.concurrent.atomic.AtomicReference;

import net.pterodactylus.util.config.ConfigurationException;
import net.pterodactylus.util.config.Value;

import com.google.common.base.Objects;

/**
 * Simple {@link Value} implementation.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class TestValue<T> implements Value<T> {

	private final AtomicReference<T> value = new AtomicReference<T>();

	public TestValue(T originalValue) {
		value.set(originalValue);
	}

	@Override
	public T getValue() throws ConfigurationException {
		return value.get();
	}

	@Override
	public T getValue(T defaultValue) {
		final T realValue = value.get();
		return (realValue != null) ? realValue : defaultValue;
	}

	@Override
	public void setValue(T newValue) throws ConfigurationException {
		value.set(newValue);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof TestValue) && Objects.equal(value.get(),
				((TestValue) obj).value.get());
	}

	@Override
	public String toString() {
		return String.valueOf(value.get());
	}

	public static <T> Value<T> from(T value) {
		return new TestValue<T>(value);
	}

}
