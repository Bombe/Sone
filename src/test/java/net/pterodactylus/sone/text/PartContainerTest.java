package net.pterodactylus.sone.text;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Test;

/**
 * Unit test for {@link PartContainer}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PartContainerTest {

	private final PartContainer container = new PartContainer();

	@Test
	public void emptyContainerHasSizeZero() {
		assertThat(container.size(), is(0));
	}

	@Test(expected = NullPointerException.class)
	public void canNotAddNullPart() {
	    container.add(null);
	}

	@Test
	public void containerWithSinglePartHasSizeOne() {
		container.add(mock(Part.class));
		assertThat(container.size(), is(1));
	}

	@Test
	public void containerWithSinglePartCanReturnPart() {
		Part part = mock(Part.class);
		container.add(part);
		assertThat(container.getPart(0), is(part));
	}

	@Test
	public void containerIsEmptyAfterPartIsAddedAndRemoved() {
		container.add(mock(Part.class));
		container.removePart(0);
		assertThat(container.size(), is(0));
	}

	@Test
	public void containerContainsSecondPartIfFirstPartIsRemoved() {
		container.add(mock(Part.class));
		Part part = mock(Part.class);
		container.add(part);
		container.removePart(0);
		assertThat(container.getPart(0), is(part));
	}

	@Test
	public void textOfContainerPartIsTextOfPartsConcatenated() {
		container.add(createPartWithText("first"));
		container.add(createPartWithText("second"));
		assertThat(container.getText(), is("firstsecond"));
	}

	private Part createPartWithText(String text) {
		Part part = mock(Part.class);
		when(part.getText()).thenReturn(text);
		return part;
	}

	@Test(expected = NoSuchElementException.class)
	public void emptyContainerIteratorThrowsOnNext() {
		container.iterator().next();
	}

	@Test
	public void iteratorIteratesPartsRecursivelyInCorrectOrder() {
		Part firstPart = mock(Part.class);
		PartContainer secondPart = new PartContainer();
		Part thirdPart = mock(Part.class);
		Part nestedFirstPart = mock(Part.class);
		Part nestedSecondPart = mock(Part.class);
		secondPart.add(nestedFirstPart);
		secondPart.add(nestedSecondPart);
		container.add(firstPart);
		container.add(secondPart);
		container.add(thirdPart);
		Iterator<Part> parts = container.iterator();
		assertThat(parts.next(), is(firstPart));
		assertThat(parts.next(), is(nestedFirstPart));
		assertThat(parts.next(), is(nestedSecondPart));
		assertThat(parts.next(), is(thirdPart));
		assertThat(parts.hasNext(), is(false));
	}

}
