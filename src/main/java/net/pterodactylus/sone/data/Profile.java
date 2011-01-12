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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.pterodactylus.util.validation.Validation;

/**
 * A profile stores personal information about a {@link Sone}. All information
 * is optional and can be {@code null}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Profile implements Fingerprintable {

	/** The first name. */
	private volatile String firstName;

	/** The middle name(s). */
	private volatile String middleName;

	/** The last name. */
	private volatile String lastName;

	/** The day of the birth date. */
	private volatile Integer birthDay;

	/** The month of the birth date. */
	private volatile Integer birthMonth;

	/** The year of the birth date. */
	private volatile Integer birthYear;

	/** Additional fields in the profile. */
	private final List<String> fields = Collections.synchronizedList(new ArrayList<String>());

	/** The field values. */
	private final Map<String, String> fieldValues = Collections.synchronizedMap(new HashMap<String, String>());

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
		this.fieldValues.putAll(profile.fieldValues);
	}

	//
	// ACCESSORS
	//

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
		this.birthYear = birthYear;
		return this;
	}

	/**
	 * Appends a new field to the list of fields.
	 *
	 * @param field
	 *            The field to add
	 * @throws IllegalArgumentException
	 *             if the name is not valid
	 */
	public void addField(String field) throws IllegalArgumentException {
		Validation.begin().isNotNull("Field Name", field).check().isGreater("Field Name Length", field.length(), 0).isEqual("Field Name Unique", !fields.contains(field), true).check();
		fields.add(field);
	}

	/**
	 * Moves the field with the given index up one position in the field list.
	 * The index of the field to move must be greater than {@code 0} (because
	 * you obviously can not move the first field further up).
	 *
	 * @param fieldIndex
	 *            The index of the field to move
	 */
	public void moveFieldUp(int fieldIndex) {
		Validation.begin().isGreater("Field Index", fieldIndex, 0).isLess("Field Index", fieldIndex, fields.size()).check();
		String field = fields.remove(fieldIndex);
		fields.add(fieldIndex - 1, field);
	}

	/**
	 * Moves the field with the given name up one position in the field list.
	 * The field must not be the first field (because you obviously can not move
	 * the first field further up).
	 *
	 * @param field
	 *            The name of the field to move
	 */
	public void moveFieldUp(String field) {
		Validation.begin().isNotNull("Field Name", field).check().isGreater("Field Name Length", field.length(), 0).isEqual("Field Name Existing", fields.contains(field), true).check();
		moveFieldUp(getFieldIndex(field));
	}

	/**
	 * Moves the field with the given index down one position in the field list.
	 * The index of the field to move must be less than the index of the last
	 * field (because you obviously can not move the last field further down).
	 *
	 * @param fieldIndex
	 *            The index of the field to move
	 */
	public void moveFieldDown(int fieldIndex) {
		Validation.begin().isGreaterOrEqual("Field Index", fieldIndex, 0).isLess("Field Index", fieldIndex, fields.size() - 1).check();
		String field = fields.remove(fieldIndex);
		fields.add(fieldIndex + 1, field);
	}

	/**
	 * Moves the field with the given name down one position in the field list.
	 * The field must not be the last field (because you obviously can not move
	 * the last field further down).
	 *
	 * @param field
	 *            The name of the field to move
	 */
	public void moveFieldDown(String field) {
		Validation.begin().isNotNull("Field Name", field).check().isGreater("Field Name Length", field.length(), 0).isEqual("Field Name Existing", fields.contains(field), true).check();
		moveFieldDown(getFieldIndex(field));
	}

	/**
	 * Removes the field at the given index.
	 *
	 * @param fieldIndex
	 *            The index of the field to remove
	 */
	public void removeField(int fieldIndex) {
		Validation.begin().isGreaterOrEqual("Field Index", fieldIndex, 0).isLess("Field Index", fieldIndex, fields.size()).check();
		String field = fields.remove(fieldIndex);
		fieldValues.remove(field);
	}

	/**
	 * Removes the field with the given name.
	 *
	 * @param field
	 *            The name of the field
	 */
	public void removeField(String field) {
		Validation.begin().isNotNull("Field Name", field).check().isGreater("Field Name Length", field.length(), 0).isEqual("Field Name Existing", fields.contains(field), true).check();
		removeField(getFieldIndex(field));
	}

	/**
	 * Returns the value of the field with the given name.
	 *
	 * @param field
	 *            The name of the field
	 * @return The value of the field, or {@code null} if there is no such field
	 */
	public String getField(String field) {
		return fieldValues.get(field);
	}

	/**
	 * Sets the value of the field with the given name.
	 *
	 * @param field
	 *            The name of the field
	 * @param value
	 *            The value of the field
	 */
	public void setField(String field, String value) {
		Validation.begin().isNotNull("Field Name", field).check().isGreater("Field Name Length", field.length(), 0).isEqual("Field Name Existing", fields.contains(field), true).check();
		fieldValues.put(field, value);
	}

	/**
	 * Returns a list of all fields stored in this profile.
	 *
	 * @return The fields of this profile
	 */
	public List<String> getFields() {
		return Collections.unmodifiableList(fields);
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Returns the index of the field with the given name.
	 *
	 * @param field
	 *            The name of the field
	 * @return The index of the field, or {@code -1} if there is no field with
	 *         the given name
	 */
	private int getFieldIndex(String field) {
		return fields.indexOf(field);
	}

	//
	// INTERFACE Fingerprintable
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFingerprint() {
		StringBuilder fingerprint = new StringBuilder();
		fingerprint.append("Profile(");
		if (firstName != null) {
			fingerprint.append("FirstName(").append(firstName).append(')');
		}
		if (middleName != null) {
			fingerprint.append("MiddleName(").append(middleName).append(')');
		}
		if (lastName != null) {
			fingerprint.append("LastName(").append(lastName).append(')');
		}
		if (birthDay != null) {
			fingerprint.append("BirthDay(").append(birthDay).append(')');
		}
		if (birthMonth != null) {
			fingerprint.append("BirthMonth(").append(birthMonth).append(')');
		}
		if (birthYear != null) {
			fingerprint.append("BirthYear(").append(birthYear).append(')');
		}
		fingerprint.append("ContactInformation(");
		for (String field : fields) {
			fingerprint.append(field).append('(').append(fieldValues.get(field)).append(')');
		}
		fingerprint.append(")");
		fingerprint.append(")");

		return fingerprint.toString();
	}

}
