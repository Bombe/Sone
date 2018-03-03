package net.pterodactylus.sone.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;

import net.pterodactylus.sone.utils.Option;

import org.junit.Test;

/**
 * Unit test for {@link Options}.
 */
public class OptionsTest {

	private final Options options = new Options();

	@Test
	public void booleanOptionIsAdded() {
		Option<Boolean> booleanOption = mock(Option.class);
		options.addBooleanOption("test", booleanOption);
		assertThat(options.getBooleanOption("test"), is(booleanOption));
		assertThat(options.getBooleanOption("not-test"), nullValue());
	}

	@Test
	public void integerOptionIsAdded() {
		Option<Integer> integerOption = mock(Option.class);
		options.addIntegerOption("test", integerOption);
		assertThat(options.getIntegerOption("test"), is(integerOption));
		assertThat(options.getIntegerOption("not-test"), nullValue());
	}

	@Test
	public void stringOptionIsAdded() {
		Option<String> stringOption = mock(Option.class);
		options.addStringOption("test", stringOption);
		assertThat(options.getStringOption("test"), is(stringOption));
		assertThat(options.getStringOption("not-test"), nullValue());
	}

	@Test
	public void enumOptionIsAdded() {
		Option<TestEnum> enumOption = mock(Option.class);
		options.addEnumOption("test", enumOption);
		assertThat(options.<TestEnum>getEnumOption("test"), is(enumOption));
		assertThat(options.<TestEnum>getEnumOption("not-test"), nullValue());
	}

	private enum TestEnum {TEST, NOT_TEST}

}
