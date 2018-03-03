package net.pterodactylus.sone.freenet.wot.event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;

import net.pterodactylus.sone.freenet.wot.OwnIdentity;

import org.junit.Test;

/**
 * Unit test for {@link OwnIdentityEvent}.
 */
public class OwnIdentityEventTest {

	private final OwnIdentity ownIdentity = mock(OwnIdentity.class);
	private final OwnIdentityEvent ownIdentityEvent = createOwnIdentityEvent(ownIdentity);

	@Test
	public void eventRetainsOwnIdentity() {
		assertThat(ownIdentityEvent.ownIdentity(), is(ownIdentity));
	}

	protected OwnIdentityEvent createOwnIdentityEvent(final OwnIdentity ownIdentity) {
		return new OwnIdentityEvent(ownIdentity) {
		};
	}

	@Test
	public void twoOwnIdentityEventsWithTheSameIdentityHaveTheSameHashCode() {
		OwnIdentityEvent secondOwnIdentityEvent = createOwnIdentityEvent(ownIdentity);
		assertThat(secondOwnIdentityEvent.hashCode(), is(ownIdentityEvent.hashCode()));
	}

	@Test
	public void ownIdentityEventDoesNotMatchNull() {
		assertThat(ownIdentityEvent, not(is((Object) null)));
	}

	@Test
	public void ownIdentityEventDoesNotMatchObjectWithADifferentClass() {
		assertThat(ownIdentityEvent, not(is(new Object())));
	}

}
