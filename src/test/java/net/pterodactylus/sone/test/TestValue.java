package net.pterodactylus.sone.test;

import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.pterodactylus.util.config.ConfigurationException;
import net.pterodactylus.util.config.Value;

import com.google.common.base.Objects;

/**
 * Simple {@link Value} implementation.
 */
public class TestValue<T> implements Value<T> {

	private final AtomicReference<T> value = new AtomicReference<>();

	public TestValue(@Nullable T originalValue) {
		value.set(originalValue);
	}

	@Override
	@Nullable
	public T getValue() throws ConfigurationException {
		return value.get();
	}

	@Override
	@Nullable
	public T getValue(@Nullable T defaultValue) {
		final T realValue = value.get();
		return (realValue != null) ? realValue : defaultValue;
	}

	@Override
	public void setValue(@Nullable T newValue) throws ConfigurationException {
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
	@Nonnull
	public String toString() {
		return String.valueOf(value.get());
	}

	@Nonnull
	public static <T> Value<T> from(@Nullable T value) {
		return new TestValue<>(value);
	}

}
