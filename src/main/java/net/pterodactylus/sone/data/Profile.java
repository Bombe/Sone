/*
 * FreenetSone - Profile.java - Copyright © 2010 David Roden
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

package net.pterodactylus.sone.data;

/**
 * A profile stores personal information about a {@link Sone}. All information
 * is optional and can be {@code null}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Profile {

	/** Whether the profile was modified. */
	private boolean modified;

	/** The first name. */
	private String firstName;

	/** The middle name(s). */
	private String middleName;

	/** The last name. */
	private String lastName;

	/** The day of the birth date. */
	private Integer birthDay;

	/** The month of the birth date. */
	private Integer birthMonth;

	/** The year of the birth date. */
	private Integer birthYear;

	/**
	 * Creates a new empty profile.
	 */
	public Profile() {
		/* do nothing. */
	}

	/**
	 * Creates a copy of a profile.
	 *
	 * @param profile
	 *            The profile to copy
	 */
	public Profile(Profile profile) {
		if (profile == null) {
			return;
		}
		this.firstName = profile.firstName;
		this.middleName = profile.middleName;
		this.lastName = profile.lastName;
		this.birthDay = profile.birthDay;
		this.birthMonth = profile.birthMonth;
		this.birthYear = profile.birthYear;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns whether this profile was modified after creation. To clear the
	 * “is modified” flag you need to create a new profile from this one using
	 * the {@link #Profile(Profile)} constructor.
	 *
	 * @return {@code true} if this profile was modified after creation,
	 *         {@code false} otherwise
	 */
	public boolean isModified() {
		return modified;
	}

	/**
	 * Returns the first name.
	 *
	 * @return The first name
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Sets the first name.
	 *
	 * @param firstName
	 *            The first name to set
	 * @return This profile (for method chaining)
	 */
	public Profile setFirstName(String firstName) {
		modified |= ((firstName != null) && (!firstName.equals(this.firstName))) || (this.firstName != null);
		this.firstName = firstName;
		return this;
	}

	/**
	 * Returns the middle name(s).
	 *
	 * @return The middle name
	 */
	public String getMiddleName() {
		return middleName;
	}

	/**
	 * Sets the middle name.
	 *
	 * @param middleName
	 *            The middle name to set
	 * @return This profile (for method chaining)
	 */
	public Profile setMiddleName(String middleName) {
		modified |= ((middleName != null) && (!middleName.equals(this.middleName))) || (this.middleName != null);
		this.middleName = middleName;
		return this;
	}

	/**
	 * Returns the last name.
	 *
	 * @return The last name
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Sets the last name.
	 *
	 * @param lastName
	 *            The last name to set
	 * @return This profile (for method chaining)
	 */
	public Profile setLastName(String lastName) {
		modified |= ((lastName != null) && (!lastName.equals(this.lastName))) || (this.lastName != null);
		this.lastName = lastName;
		return this;
	}

	/**
	 * Returns the day of the birth date.
	 *
	 * @return The day of the birth date (from 1 to 31)
	 */
	public Integer getBirthDay() {
		return birthDay;
	}

	/**
	 * Sets the day of the birth date.
	 *
	 * @param birthDay
	 *            The day of the birth date (from 1 to 31)
	 * @return This profile (for method chaining)
	 */
	public Profile setBirthDay(Integer birthDay) {
		modified |= ((birthDay != null) && (!birthDay.equals(this.birthDay))) || (this.birthDay != null);
		this.birthDay = birthDay;
		return this;
	}

	/**
	 * Returns the month of the birth date.
	 *
	 * @return The month of the birth date (from 1 to 12)
	 */
	public Integer getBirthMonth() {
		return birthMonth;
	}

	/**
	 * Sets the month of the birth date.
	 *
	 * @param birthMonth
	 *            The month of the birth date (from 1 to 12)
	 * @return This profile (for method chaining)
	 */
	public Profile setBirthMonth(Integer birthMonth) {
		modified |= ((birthMonth != null) && (!birthMonth.equals(this.birthMonth))) || (this.birthMonth != null);
		this.birthMonth = birthMonth;
		return this;
	}

	/**
	 * Returns the year of the birth date.
	 *
	 * @return The year of the birth date
	 */
	public Integer getBirthYear() {
		return birthYear;
	}

	/**
	 * Sets the year of the birth date.
	 *
	 * @param birthYear
	 *            The year of the birth date
	 * @return This profile (for method chaining)
	 */
	public Profile setBirthYear(Integer birthYear) {
		modified |= ((birthYear != null) && (!birthYear.equals(this.birthYear))) || (this.birthYear != null);
		this.birthYear = birthYear;
		return this;
	}

}
