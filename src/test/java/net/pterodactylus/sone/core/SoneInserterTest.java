package net.pterodactylus.sone.core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import net.pterodactylus.sone.core.SoneInserter.SetInsertionDelay;

import org.junit.Test;

/**
 * Unit test for {@link SoneInserter} and its subclasses.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneInserterTest {

	@Test
	public void insertionDelayIsForwardedToSoneInserter() {
		SetInsertionDelay setInsertionDelay = new SetInsertionDelay();
		setInsertionDelay.optionChanged(null, null, 15);
		assertThat(SoneInserter.getInsertionDelay().get(), is(15));
	}

}
