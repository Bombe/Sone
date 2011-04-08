/*
 * Sone - VersionCommand.java - Copyright © 2011 David Roden
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

package net.pterodactylus.sone.freenet.fcp;

import net.pterodactylus.sone.freenet.SimpleFieldSetBuilder;
import net.pterodactylus.sone.main.SonePlugin;
import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;

/**
 * Returns version information about the Sone plugin.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class VersionCommand implements Command {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Reply execute(SimpleFieldSet parameters, Bucket data, AccessType accessType) {
		return new Reply(new SimpleFieldSetBuilder().put("Version", SonePlugin.VERSION.toString()).put("ProtocolVersion", 1).get());
	}

}
