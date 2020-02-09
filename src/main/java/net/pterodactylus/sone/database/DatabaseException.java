/*
 * Sone - DatabaseException.java - Copyright © 2013–2020 David Roden
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

package net.pterodactylus.sone.database;

/**
 * Exception that signals a database error.
 */
public class DatabaseException extends Exception {

	/**
	 * Creates a new database exception.
	 */
	public DatabaseException() {
		super();
	}

	/**
	 * Creates a new database exception.
	 *
	 * @param message
	 *            The message of the exception
	 */
	public DatabaseException(String message) {
		super(message);
	}

	/**
	 * Creates a new database exception.
	 *
	 * @param cause
	 *            The cause of the exception
	 */
	public DatabaseException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new database exception.
	 *
	 * @param message
	 *            The message of the exception
	 * @param cause
	 *            The cause of the exception
	 */
	public DatabaseException(String message, Throwable cause) {
		super(message, cause);
	}

}
