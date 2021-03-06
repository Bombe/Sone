package net.pterodactylus.sone.utils;

import javax.annotation.Nullable;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

/**
 * Parses numbers from strings.
 */
public class NumberParsers {

	@Nullable
	public static Integer parseInt(@Nullable String text,
			@Nullable Integer defaultValue) {
		if (text == null) {
			return defaultValue;
		}
		Integer value = Ints.tryParse(text);
		return (value == null) ? defaultValue : value;
	}

	@Nullable
	public static Long parseLong(@Nullable String text,
			@Nullable Long defaultValue) {
		if (text == null) {
			return defaultValue;
		}
		Long value = Longs.tryParse(text);
		return (value == null) ? defaultValue : value;
	}

}
