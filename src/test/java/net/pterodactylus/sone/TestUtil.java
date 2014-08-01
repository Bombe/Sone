package net.pterodactylus.sone;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Utilities for testing.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class TestUtil {

	public static void setFinalField(Object object, String fieldName, Object value) {
		try {
			Field clientCoreField = object.getClass().getField(fieldName);
			clientCoreField.setAccessible(true);
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(clientCoreField, clientCoreField.getModifiers() & ~Modifier.FINAL);
			clientCoreField.set(object, value);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T getPrivateField(Object object, String fieldName) {
		try {
			Field field = object.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return (T) field.get(object);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

}
