package net.pterodactylus.sone.utils;

import static net.pterodactylus.sone.utils.NumberParsers.parseInt;
import static net.pterodactylus.sone.utils.NumberParsers.parseLong;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

/**
 * Unit test for {@link NumberParsers}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class NumberParsersTest {

	@Test
	// yes, this test is for coverage only.
	public void constructorCanBeCalled() {
		new NumberParsers();
	}

	@Test
	public void nullIsParsedToDefaultInt() {
		assertThat(parseInt(null, 17), is(17));
	}

	@Test
	public void notANumberIsParsedToDefaultInt() {
		assertThat(parseInt("not a number", 18), is(18));
	}

	@Test
	public void intIsCorrectlyParsed() {
		assertThat(parseInt("19", 0), is(19));
	}

	@Test
	public void valueTooLargeForIntIsParsedToDefault() {
		assertThat(parseInt("2147483648", 20), is(20));
	}

	@Test
	public void valueTooSmallForIntIsParsedToDefault() {
		assertThat(parseInt("-2147483649", 20), is(20));
	}

	@Test
	public void nullCanBeDefaultIntValue() {
		assertThat(parseInt("not a number", null), nullValue());
	}

	@Test
	public void nullIsParsedToDefaultLong() {
		assertThat(parseLong(null, 17L), is(17L));
	}

	@Test
	public void notANumberIsParsedToDefaultLong() {
		assertThat(parseLong("not a number", 18L), is(18L));
	}

	@Test
	public void LongIsCorrectlyParsed() {
		assertThat(parseLong("19", 0L), is(19L));
	}

	@Test
	public void valueTooLargeForLongIsParsedToDefault() {
		assertThat(parseLong("9223372036854775808", 20L), is(20L));
	}

	@Test
	public void valueTooSmallForLongIsParsedToDefault() {
		assertThat(parseLong("-9223372036854775809", 20L), is(20L));
	}

	@Test
	public void nullCanBeDefaultLongValue() {
		assertThat(parseLong("not a number", null), nullValue());
	}

}
