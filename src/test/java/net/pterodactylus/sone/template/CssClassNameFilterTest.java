package net.pterodactylus.sone.template;

import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Unit test for {@link CssClassNameFilter}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class CssClassNameFilterTest {

	private static final Map<String, Object> EMPTY_MAP = emptyMap();
	private final CssClassNameFilter filter = new CssClassNameFilter();

	@Test
	public void stringsAreFiltered() {
		String allCharacters = "name with äöü";
		String filteredCharacters = "name_with____";
		assertThat(filter.format(null, allCharacters, EMPTY_MAP),
				Matchers.<Object>is(filteredCharacters));
	}

	@Test
	public void nullIsFiltered() {
		assertThat(filter.format(null, null, EMPTY_MAP),
				Matchers.<Object>is("null"));
	}

}
