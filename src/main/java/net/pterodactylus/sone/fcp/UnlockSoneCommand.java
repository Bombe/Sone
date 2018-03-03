/*
 * Sone - UnlockSoneCommand.java - Copyright © 2013–2016 David Roden
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

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.SimpleFieldSetBuilder;
import net.pterodactylus.sone.freenet.fcp.FcpException;

import freenet.support.SimpleFieldSet;

import com.google.common.base.Optional;

/**
 * Implements the “UnlockSone” FCP command. If a valid local Sone was given as
 * parameter “Sone,” this command will always unlock the Sone and reply with
 * “SoneUnlocked.”
 */
public class UnlockSoneCommand extends AbstractSoneCommand {

	/**
	 * Creates a new “LockSone” FCP command.
	 *
	 * @param core
	 * 		The core to operate on
	 */
	public UnlockSoneCommand(Core core) {
		super(core, true);
	}

	//
	// COMMAND METHODS
	//

	@Override
	public Response execute(SimpleFieldSet parameters) throws FcpException {
		Optional<Sone> sone = getSone(parameters, "Sone", true, true);
		getCore().unlockSone(sone.get());
		return new Response("SoneUnlocked", new SimpleFieldSetBuilder().put("Sone", sone.get().getId()).get());
	}

}
