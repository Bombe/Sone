package net.pterodactylus.sone.utils;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

/**
 * Helper methods for dealing with {@link Optional}s.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Optionals {

	public static Predicate<Optional<?>> isPresent() {
		return new Predicate<Optional<?>>() {
			@Override
			public boolean apply(Optional<?> input) {
				return input.isPresent();
			}
		};
	}

	public static <T> Function<Optional<T>, T> get() {
		return new Function<Optional<T>, T>() {
			@Override
			public T apply(Optional<T> input) {
				return input.get();
			}
		};
	}

}
