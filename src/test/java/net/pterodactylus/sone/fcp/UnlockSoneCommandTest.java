/*
 * Sone - UnlockSoneCommandTest.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sone.fcp;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.SimpleFieldSetBuilder;
import net.pterodactylus.sone.freenet.fcp.Command.Response;
import net.pterodactylus.sone.freenet.fcp.FcpException;

import freenet.support.SimpleFieldSet;

import com.google.common.base.Optional;
import org.junit.Test;

/**
 * Tests for {@link LockSoneCommand}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class UnlockSoneCommandTest {

	@Test
	public void testUnlockingALocalSone() throws FcpException {
		Sone localSone = mock(Sone.class);
		when(localSone.getId()).thenReturn("LocalSone");
		when(localSone.isLocal()).thenReturn(true);
		Core core = mock(Core.class);
		when(core.getSone(eq("LocalSone"))).thenReturn(Optional.of(localSone));
		when(core.getLocalSone(eq("LocalSone"))).thenReturn(localSone);
		SimpleFieldSet fields = new SimpleFieldSetBuilder().put("Sone", "LocalSone").get();

		UnlockSoneCommand unlockSoneCommand = new UnlockSoneCommand(core);
		Response response = unlockSoneCommand.execute(fields, null, null);

		verify(core).unlockSone(eq(localSone));
		assertThat(response, notNullValue());
		assertThat(response.getReplyParameters(), notNullValue());
		assertThat(response.getReplyParameters().get("Message"), is("SoneUnlocked"));
		assertThat(response.getReplyParameters().get("Sone"), is("LocalSone"));
	}

	@Test(expected = FcpException.class)
	public void testUnlockingARemoteSone() throws FcpException {
		Sone removeSone = mock(Sone.class);
		Core core = mock(Core.class);
		when(core.getSone(eq("RemoteSone"))).thenReturn(Optional.of(removeSone));
		SimpleFieldSet fields = new SimpleFieldSetBuilder().put("Sone", "RemoteSone").get();

		UnlockSoneCommand unlockSoneCommand = new UnlockSoneCommand(core);
		unlockSoneCommand.execute(fields, null, null);
	}

	@Test(expected = FcpException.class)
	public void testMissingSone() throws FcpException {
		Core core = mock(Core.class);
		SimpleFieldSet fields = new SimpleFieldSetBuilder().get();

		UnlockSoneCommand unlockSoneCommand = new UnlockSoneCommand(core);
		unlockSoneCommand.execute(fields, null, null);
	}

}
