package net.pterodactylus.sone.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Utilities for testing.
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
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T getPrivateField(Object object, String fieldName) {
		try {
			Field field = object.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return (T) field.get(object);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T callPrivateMethod(Object object, String methodName) {
		try {
			Method method = object.getClass().getDeclaredMethod(methodName, new Class[0]);
			method.setAccessible(true);
			return (T) method.invoke(object);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T createObject(Class<T> clazz, Class[] parameterTypes, Object... arguments) {
		try {
			Constructor<T> constructor = clazz.getDeclaredConstructor(parameterTypes);
			constructor.setAccessible(true);
			return constructor.newInstance(arguments);
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

}
