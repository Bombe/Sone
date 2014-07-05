/*
 * Sone - DefaultIdentityTest.java - Copyright © 2013 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.sone.freenet.wot;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Arrays.asList;
import static net.pterodactylus.sone.Matchers.matchesRegex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.junit.Test;

/**
 * Unit test for {@link DefaultIdentity}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class DefaultIdentityTest {

	protected final DefaultIdentity identity = createIdentity();

	protected DefaultIdentity createIdentity() {
		return new DefaultIdentity("Id", "Nickname", "RequestURI");
	}

	@Test
	public void identityCanBeCreated() {
		assertThat(identity.getId(), is("Id"));
		assertThat(identity.getNickname(), is("Nickname"));
		assertThat(identity.getRequestUri(), is("RequestURI"));
		assertThat(identity.getContexts(), empty());
		assertThat(identity.getProperties(), is(Collections.<String, String>emptyMap()));
	}

	@Test
	public void contextsAreAddedCorrectly() {
		identity.addContext("Test");
		assertThat(identity.getContexts(), contains("Test"));
		assertThat(identity.hasContext("Test"), is(true));
	}

	@Test
	public void contextsAreRemovedCorrectly() {
		identity.addContext("Test");
		identity.removeContext("Test");
		assertThat(identity.getContexts(), empty());
		assertThat(identity.hasContext("Test"), is(false));
	}

	@Test
	public void contextsAreSetCorrectlyInBulk() {
		identity.addContext("Test");
		identity.setContexts(asList("Test1", "Test2"));
		assertThat(identity.getContexts(), containsInAnyOrder("Test1", "Test2"));
		assertThat(identity.hasContext("Test"), is(false));
		assertThat(identity.hasContext("Test1"), is(true));
		assertThat(identity.hasContext("Test2"), is(true));
	}

	@Test
	public void propertiesAreAddedCorrectly() {
		identity.setProperty("Key", "Value");
		assertThat(identity.getProperties().size(), is(1));
		assertThat(identity.getProperties(), hasEntry("Key", "Value"));
		assertThat(identity.getProperty("Key"), is("Value"));
	}

	@Test
	public void propertiesAreRemovedCorrectly() {
		identity.setProperty("Key", "Value");
		identity.removeProperty("Key");
		assertThat(identity.getProperties(), is(Collections.<String, String>emptyMap()));
		assertThat(identity.getProperty("Key"), nullValue());
	}

	@Test
	public void propertiesAreSetCorrectlyInBulk() {
		identity.setProperty("Key", "Value");
		identity.setProperties(of("Key1", "Value1", "Key2", "Value2"));
		assertThat(identity.getProperties().size(), is(2));
		assertThat(identity.getProperty("Key"), nullValue());
		assertThat(identity.getProperty("Key1"), is("Value1"));
		assertThat(identity.getProperty("Key2"), is("Value2"));
	}

	@Test
	public void trustRelationshipsAreAddedCorrectly() {
		OwnIdentity ownIdentity = mock(OwnIdentity.class);
		Trust trust = mock(Trust.class);
		identity.setTrust(ownIdentity, trust);
		assertThat(identity.getTrust(ownIdentity), is(trust));
	}

	@Test
	public void trustRelationshipsAreRemovedCorrectly() {
		OwnIdentity ownIdentity = mock(OwnIdentity.class);
		Trust trust = mock(Trust.class);
		identity.setTrust(ownIdentity, trust);
		identity.removeTrust(ownIdentity);
		assertThat(identity.getTrust(ownIdentity), nullValue());
	}

	@Test
	public void identitiesWithTheSameIdAreEqual() {
		DefaultIdentity identity2 = new DefaultIdentity("Id", "Nickname2", "RequestURI2");
		assertThat(identity2, is(identity));
		assertThat(identity, is(identity2));
	}

	@Test
	public void twoEqualIdentitiesHaveTheSameHashCode() {
		DefaultIdentity identity2 = new DefaultIdentity("Id", "Nickname2", "RequestURI2");
		assertThat(identity.hashCode(), is(identity2.hashCode()));
	}

	@Test
	public void nullDoesNotMatchAnIdentity() {
		assertThat(identity, not(is((Object) null)));
	}

	@Test
	public void toStringContainsIdAndNickname() {
		String identityString = identity.toString();
		assertThat(identityString, matchesRegex(".*\\bId\\b.*"));
		assertThat(identityString, matchesRegex(".*\\bNickname\\b.*"));
	}

}
