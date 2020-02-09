/*
 * Sone - GetLocalSonesCommand.java - Copyright © 2011–2020 David Roden
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

import static net.pterodactylus.sone.fcp.AbstractSoneCommandKt.encodeSones;

import net.pterodactylus.sone.core.Core;
import freenet.support.SimpleFieldSet;

/**
 * Implements the “GetLocalSones” FCP command that returns the list of local
 * Sones to the sender.
 */
public class GetLocalSonesCommand extends AbstractSoneCommand {

	/**
	 * Creates a new “GetLocalSones” FCP command.
	 *
	 * @param core
	 *            The Sone core
	 */
	public GetLocalSonesCommand(Core core) {
		super(core);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response execute(SimpleFieldSet parameters) {
		return new Response("ListLocalSones", encodeSones(getCore().getLocalSones(), "LocalSones."));
	}

}
