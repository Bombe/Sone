package net.pterodactylus.sone.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import javax.annotation.Nullable;

import net.pterodactylus.sone.utils.DefaultOption;

import com.google.common.base.Predicate;
import org.junit.Test;

/**
 * Unit test for {@link DefaultOption}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DefaultOptionTest {

	private final Object defaultValue = new Object();
	private final Object acceptedValue = new Object();
	private final Predicate<Object> matchesAcceptedValue = new Predicate<Object>() {
		@Override
		public boolean apply(@Nullable Object object) {
			return acceptedValue.equals(object);
		}
	};

	@Test
	public void defaultOptionReturnsDefaultValueWhenUnset() {
		DefaultOption<Object> defaultOption = new DefaultOption<Object>(defaultValue);
		assertThat(defaultOption.get(), is(defaultValue));
	}

	@Test
	public void defaultOptionReturnsNullForRealWhenUnset() {
		DefaultOption<Object> defaultOption = new DefaultOption<Object>(defaultValue);
		assertThat(defaultOption.getReal(), nullValue());
	}

	@Test
	public void defaultOptionWillReturnSetValue() {
		DefaultOption<Object> defaultOption = new DefaultOption<Object>(defaultValue);
		Object newValue = new Object();
		defaultOption.set(newValue);
		assertThat(defaultOption.get(), is(newValue));
	}

	@Test
	public void defaultOptionWithValidatorAcceptsValidValues() {
		DefaultOption<Object> defaultOption = new DefaultOption<Object>(defaultValue, matchesAcceptedValue);
		defaultOption.set(acceptedValue);
		assertThat(defaultOption.get(), is(acceptedValue));
	}

	@Test(expected = IllegalArgumentException.class)
	public void defaultOptionWithValidatorRejectsInvalidValues() {
		DefaultOption<Object> defaultOption = new DefaultOption<Object>(defaultValue, matchesAcceptedValue);
		defaultOption.set(new Object());
	}

	@Test
	public void defaultOptionValidatesObjectsCorrectly() {
		DefaultOption<Object> defaultOption = new DefaultOption<Object>(defaultValue, matchesAcceptedValue);
		assertThat(defaultOption.validate(acceptedValue), is(true));
		assertThat(defaultOption.validate(new Object()), is(false));
	}

	@Test
	public void settingToNullWillRestoreDefaultValue() {
		DefaultOption<Object> defaultOption = new DefaultOption<Object>(defaultValue);
		defaultOption.set(null);
		assertThat(defaultOption.get(), is(defaultValue));
	}

	@Test
	public void validateWithoutValidatorWillValidateNull() {
		DefaultOption<Object> defaultOption = new DefaultOption<Object>(defaultValue);
		assertThat(defaultOption.validate(null), is(true));
	}

	@Test
	public void validateWithValidatorWillValidateNull() {
		DefaultOption<Object> defaultOption = new DefaultOption<Object>(defaultValue, matchesAcceptedValue);
		assertThat(defaultOption.validate(null), is(true));
	}

}
