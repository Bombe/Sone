/*
 * Sone - IntegerRangePredicate.java - Copyright © 2013–2019 David Roden
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

package net.pterodactylus.sone.utils;

import com.google.common.base.Predicate;

/**
 * {@link Predicate} that verifies that an {@link Integer} value is not
 * {@code null} and is between a lower and an upper bound. Both bounds are
 * inclusive.
 */
public class IntegerRangePredicate implements Predicate<Integer> {

	/** The lower bound. */
	private final int lowerBound;

	/** The upper bound. */
	private final int upperBound;

	/**
	 * Creates a new integer range predicate.
	 *
	 * @param lowerBound
	 *            The lower bound
	 * @param upperBound
	 *            The upper bound
	 */
	public IntegerRangePredicate(int lowerBound, int upperBound) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	//
	// PREDICATE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean apply(Integer value) {
		return (value != null) && (value >= lowerBound) && (value <= upperBound);
	}

	public static IntegerRangePredicate range(int lowerBound, int upperBound) {
		return new IntegerRangePredicate(lowerBound, upperBound);
	}

}
