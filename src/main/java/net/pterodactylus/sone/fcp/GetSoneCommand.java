/*
 * Sone - GetSoneCommand.java - Copyright © 2011–2016 David Roden
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
import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.fcp.FcpException;

import com.google.common.base.Optional;

import freenet.support.SimpleFieldSet;

/**
 * Implements the “GetSone“ FCP command which returns {@link Profile}
 * information about a {@link Sone}.
 */
public class GetSoneCommand extends AbstractSoneCommand {

	/**
	 * Creates a new “GetSone” FCP command.
	 *
	 * @param core
	 *            The Sone core
	 */
	protected GetSoneCommand(Core core) {
		super(core);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response execute(SimpleFieldSet parameters) throws FcpException {
		Sone sone = getSone(parameters, "Sone", false);
		Optional<Sone> localSone = getSone(parameters, "LocalSone", true, false);
		return new Response("Sone", encodeSone(sone, "Sone.", localSone));
	}

}
