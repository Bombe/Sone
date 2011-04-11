/*
 * Sone - GetSonesCommand.java - Copyright © 2011 David Roden
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.fcp.FcpException;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;

/**
 * Implements the “GetSones” FCP command that returns the list of known Sones.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class GetSonesCommand extends AbstractSoneCommand {

	/**
	 * Creates a new “GetSones” FCP command.
	 *
	 * @param core
	 *            The Sone core
	 */
	public GetSonesCommand(Core core) {
		super(core);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response execute(SimpleFieldSet parameters, Bucket data, AccessType accessType) throws FcpException {
		int startSone = getInt(parameters, "StartSone", 0);
		int maxSones = getInt(parameters, "MaxSones", -1);
		List<Sone> sones = new ArrayList<Sone>(getCore().getSones());
		if (sones.size() < startSone) {
			return new Response("Sones", encodeSones(Collections.<Sone> emptyList(), ""));
		}
		Collections.sort(sones, Sone.NICE_NAME_COMPARATOR);
		return new Response("Sones", encodeSones(sones.subList(startSone, (maxSones == -1) ? sones.size() : Math.min(startSone + maxSones, sones.size())), ""));
	}

}
