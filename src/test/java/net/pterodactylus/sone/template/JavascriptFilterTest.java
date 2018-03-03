package net.pterodactylus.sone.template;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import net.pterodactylus.util.number.Hex;

import org.junit.Test;

/**
 * Unit test for {@link JavascriptFilter}.
 */
public class JavascriptFilterTest {

	private final JavascriptFilter filter = new JavascriptFilter();

	@Test
	public void filterEscapesAllCharactersBelowSpace() {
		String source = buildStringWithAllCharactersToEscape();
		String target = buildStringWithEscapedCharacters();
		assertThat((String) filter.format(null, source, null), is("\"" + target + "\""));
	}

	private String buildStringWithAllCharactersToEscape() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < 32; i++) {
			stringBuilder.append((char) i);
		}
		stringBuilder.append('"').append("\\").append("!");
		return stringBuilder.toString();
	}

	private String buildStringWithEscapedCharacters() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < 32; i++) {
			switch (i) {
				case 9:
					stringBuilder.append("\\t");
					break;
				case 10:
					stringBuilder.append("\\n");
					break;
				case 13:
					stringBuilder.append("\\r");
					break;
				default:
					stringBuilder.append("\\x").append(Hex.toHex(i, 2));
			}
		}
		stringBuilder.append("\\\"").append("\\\\").append("!");
		return stringBuilder.toString();
	}

}
