package net.pterodactylus.sone.data.impl;

import static org.mockito.Mockito.mock;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.wot.Identity;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;

import org.junit.Test;

/**
 * Unit test for {@link AbstractSoneBuilder}.
 */
public class AbstractSoneBuilderTest {

	private final AbstractSoneBuilder soneBuilder = new AbstractSoneBuilder() {
		@Override
		public Sone build() throws IllegalStateException {
			validate();
			return null;
		}
	};

	@Test
	public void localSoneIsValidated() {
		Identity ownIdentity = mock(OwnIdentity.class);
		soneBuilder.local().from(ownIdentity).build();
	}

	@Test(expected = IllegalStateException.class)
	public void localSoneIsNotValidatedIfIdentityIsNotAnOwnIdentity() {
		Identity identity = mock(Identity.class);
		soneBuilder.local().from(identity).build();
	}

	@Test(expected = IllegalStateException.class)
	public void localSoneIsNotValidatedIfIdentityIsNull() {
		soneBuilder.local().build();
	}

	@Test
	public void removeSoneIsValidate() {
		Identity identity = mock(Identity.class);
		soneBuilder.from(identity).build();
	}

	@Test(expected = IllegalStateException.class)
	public void remoteSoneIsNotValidatedIfIdentityIsNull() {
		soneBuilder.build();
	}

}
