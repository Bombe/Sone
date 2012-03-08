/*
 * Sone - Trust.java - Copyright © 2010–2012 David Roden
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

/**
 * Container class for trust in the web of trust.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Trust {

	/** Explicitely assigned trust. */
	private final Integer explicit;

	/** Implicitely calculated trust. */
	private final Integer implicit;

	/** The distance from the owner of the trust tree. */
	private final Integer distance;

	/**
	 * Creates a new trust container.
	 *
	 * @param explicit
	 *            The explicit trust
	 * @param implicit
	 *            The implicit trust
	 * @param distance
	 *            The distance
	 */
	public Trust(Integer explicit, Integer implicit, Integer distance) {
		this.explicit = explicit;
		this.implicit = implicit;
		this.distance = distance;
	}

	/**
	 * Returns the trust explicitely assigned to an identity.
	 *
	 * @return The explicitely assigned trust, or {@code null} if the identity
	 *         is not in the own identity’s trust tree
	 */
	public Integer getExplicit() {
		return explicit;
	}

	/**
	 * Returns the implicitely assigned trust, or the calculated trust.
	 *
	 * @return The calculated trust, or {@code null} if the identity is not in
	 *         the own identity’s trust tree
	 */
	public Integer getImplicit() {
		return implicit;
	}

	/**
	 * Returns the distance of the trusted identity from the trusting identity.
	 *
	 * @return The distance from the own identity, or {@code null} if the
	 *         identity is not in the own identity’s trust tree
	 */
	public Integer getDistance() {
		return distance;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getClass().getName() + "[explicit=" + explicit + ",implicit=" + implicit + ",distance=" + distance + "]";
	}

}
