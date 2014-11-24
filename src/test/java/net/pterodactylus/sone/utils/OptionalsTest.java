package net.pterodactylus.sone.utils;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Unit test for {@link Optionals}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class OptionalsTest {

	private final Object object1 = new Object();
	private final Object object2 = new Object();
	private final Object object3 = new Object();

	@Test
	public void canCreateOptionals() {
		new Optionals();
	}

	@Test
	public void isPresentFiltersCorrectOptionals() {
		List<Optional<Object>> optionals = Arrays.asList(
				Optional.of(object1), Optional.absent(),
				Optional.of(object2), Optional.absent(),
				Optional.of(object3), Optional.absent()
		);
		List<Optional<Object>> filteredOptionals =
				FluentIterable.from(optionals).filter(Optionals.isPresent()).toList();
		MatcherAssert.assertThat(filteredOptionals, Matchers.contains(
				Optional.of(object1), Optional.of(object2), Optional.of(object3)));
	}

	@Test
	public void getReturnsCorrectValues() {
		List<Optional<Object>> optionals = Arrays.asList(
				Optional.of(object1),
				Optional.of(object2),
				Optional.of(object3)
		);
		List<Object> objects = FluentIterable.from(optionals).transform(Optionals.get()).toList();
		MatcherAssert.assertThat(objects, Matchers.contains(object1, object2, object3));
	}

}
