package net.pterodactylus.sone.template;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.freenet.wot.Identity;
import net.pterodactylus.sone.freenet.wot.IdentityManager;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;
import net.pterodactylus.sone.test.GuiceKt;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link IdentityAccessor}.
 */
public class IdentityAccessorTest {

	private static final String TEST_ID =
			"LrNQbyBBZW-7pHqChtp9lfPA7eXFPW~FLbJ2WrvEx5g";
	private static final String TEST_ID_WITH_CHANGED_LETTER =
			"LrMQbyBBZW-7pHqChtp9lfPA7eXFPW~FLbJ2WrvEx5g";
	private final Core core = mock(Core.class);
	private final IdentityAccessor accessor = new IdentityAccessor(core);
	private final IdentityManager identityManager =
			mock(IdentityManager.class);
	private final OwnIdentity identity = mock(OwnIdentity.class);

	@Before
	public void setupCore() {
		when(core.getIdentityManager()).thenReturn(identityManager);
	}

	@Before
	public void setupIdentity() {
		setupIdentity(identity, TEST_ID, "Test");
	}

	private void setupIdentity(Identity identity, String id,
			String nickname) {
		when(identity.getId()).thenReturn(id);
		when(identity.getNickname()).thenReturn(nickname);
	}

	private void serveIdentities(Set<OwnIdentity> identities) {
		when(identityManager.getAllOwnIdentities()).thenReturn(identities);
	}

	@Test
	public void accessorReturnsTheCorrectlyAbbreviatedNickname() {
		OwnIdentity ownIdentity = mock(OwnIdentity.class);
		setupIdentity(ownIdentity, TEST_ID_WITH_CHANGED_LETTER, "Test");
		serveIdentities(new HashSet(asList(identity, ownIdentity)));
		assertThat(accessor.get(null, identity, "uniqueNickname"),
				Matchers.<Object>is("Test@LrN"));
	}

	@Test
	public void accessorComparesTheFullLengthIfNecessary() {
		OwnIdentity ownIdentity = mock(OwnIdentity.class);
		setupIdentity(ownIdentity, TEST_ID, "Test");
		serveIdentities(new HashSet(asList(identity, ownIdentity)));
		assertThat(accessor.get(null, identity, "uniqueNickname"),
				Matchers.<Object>is("Test@" + TEST_ID));
	}

	@Test
	public void reflectionAccessorIsUsedForOtherMembers() {
		assertThat(accessor.get(null, identity, "hashCode"),
				Matchers.<Object>is(identity.hashCode()));
	}

	@Test
	public void accessorCanBeCreatedByGuice() {
		Injector injector = Guice.createInjector(
				GuiceKt.supply(Core.class).byInstance(mock(Core.class))
		);
		assertThat(injector.getInstance(IdentityAccessor.class), notNullValue());
	}

	@Test
	public void accessorIsCreatedAsSingleton() {
		Injector injector = Guice.createInjector(
				GuiceKt.supply(Core.class).byInstance(mock(Core.class))
		);
		IdentityAccessor firstAccessor = injector.getInstance(IdentityAccessor.class);
		IdentityAccessor secondAccessor = injector.getInstance(IdentityAccessor.class);
		assertThat(firstAccessor, sameInstance(secondAccessor));
	}

}
