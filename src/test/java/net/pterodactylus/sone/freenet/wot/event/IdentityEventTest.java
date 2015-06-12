package net.pterodactylus.sone.freenet.wot.event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;

import net.pterodactylus.sone.freenet.wot.Identity;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;

import org.junit.Test;

/**
 * Unit test for {@link IdentityEvent}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class IdentityEventTest {

	private final OwnIdentity ownIdentity = mock(OwnIdentity.class);
	private final Identity identity = mock(Identity.class);
	private final IdentityEvent identityEvent = createIdentityEvent(ownIdentity, identity);

	private IdentityEvent createIdentityEvent(final OwnIdentity ownIdentity, final Identity identity) {
		return new IdentityEvent(ownIdentity, identity) {
		};
	}

	@Test
	public void identityEventRetainsIdentities() {
		assertThat(identityEvent.ownIdentity(), is(ownIdentity));
		assertThat(identityEvent.identity(), is(identity));
	}

	@Test
	public void eventsWithTheSameIdentityHaveTheSameHashCode() {
		IdentityEvent secondIdentityEvent = createIdentityEvent(ownIdentity, identity);
		assertThat(identityEvent.hashCode(), is(secondIdentityEvent.hashCode()));
	}

	@Test
	public void eventsWithTheSameIdentitiesAreEqual() {
		IdentityEvent secondIdentityEvent = createIdentityEvent(ownIdentity, identity);
		assertThat(identityEvent, is(secondIdentityEvent));
		assertThat(secondIdentityEvent, is(identityEvent));
	}

	@Test
	public void nullDoesNotEqualIdentityEvent() {
		assertThat(identityEvent, not(is((Object) null)));
	}


}
