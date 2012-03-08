/*
 * Sone - SoneInsertListener.java - Copyright © 2011–2012 David Roden
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

package net.pterodactylus.sone.core;

import java.util.EventListener;

import net.pterodactylus.sone.data.Sone;

/**
 * Listener for Sone insert events.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface SoneInsertListener extends EventListener {

	/**
	 * Notifies a listener that a Sone is now being inserted.
	 *
	 * @param sone
	 *            The Sone being inserted
	 */
	public void insertStarted(Sone sone);

	/**
	 * Notifies a listener that a Sone has been successfully inserted.
	 *
	 * @param sone
	 *            The Sone that was inserted
	 * @param insertDuration
	 *            The duration of the insert (in milliseconds)
	 */
	public void insertFinished(Sone sone, long insertDuration);

	/**
	 * Notifies a listener that the insert of the given Sone was aborted.
	 *
	 * @param sone
	 *            The Sone that was being inserted
	 * @param cause
	 *            The cause of the abortion (may be {@code null})
	 */
	public void insertAborted(Sone sone, Throwable cause);

}
