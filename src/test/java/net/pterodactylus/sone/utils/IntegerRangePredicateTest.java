package net.pterodactylus.sone.utils;

import static net.pterodactylus.sone.utils.IntegerRangePredicate.range;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import net.pterodactylus.sone.test.TestUtil;

import org.junit.Test;

/**
 * Unit test for {@link IntegerRangePredicate}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class IntegerRangePredicateTest {

	private final IntegerRangePredicate predicate =
			new IntegerRangePredicate(-50, 50);

	@Test
	public void predicateMatchesNumberWithinBounds() {
		assertThat(predicate.apply(17), is(true));
	}

	@Test
	public void predicateMatchesLowerBoundary() {
		assertThat(predicate.apply(-50), is(true));
	}

	@Test
	public void predicateDoesNotMatchOneBelowLowerBoundary() {
		assertThat(predicate.apply(-51), is(false));
	}

	@Test
	public void predicateMatchesUpperBoundary() {
		assertThat(predicate.apply(50), is(true));
	}

	@Test
	public void predicateDoesNotMatchesOneAboveUpperBoundary() {
		assertThat(predicate.apply(51), is(false));
	}

	@Test
	public void staticCreatorMethodCreatesPredicate() {
		IntegerRangePredicate predicate = range(-50, 50);
		assertThat(TestUtil.<Integer>getPrivateField(predicate, "lowerBound"),
				is(-50));
		assertThat(TestUtil.<Integer>getPrivateField(predicate, "upperBound"),
				is(50));
	}

}
