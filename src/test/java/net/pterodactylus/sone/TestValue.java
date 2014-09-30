package net.pterodactylus.sone;

import java.util.concurrent.atomic.AtomicReference;

import net.pterodactylus.util.config.ConfigurationException;
import net.pterodactylus.util.config.Value;

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

}
