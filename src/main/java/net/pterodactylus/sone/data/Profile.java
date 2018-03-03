/*
 * Sone - Profile.java - Copyright © 2010–2016 David Roden
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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

/**
 * A profile stores personal information about a {@link Sone}. All information
 * is optional and can be {@code null}.
 */
public class Profile implements Fingerprintable {

	/** The Sone this profile belongs to. */
	private final Sone sone;

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

	/** The ID of the avatar image. */
	private volatile String avatar;

	/** Additional fields in the profile. */
	private final List<Field> fields = Collections.synchronizedList(new ArrayList<Field>());

	/**
	 * Creates a new empty profile.
	 *
	 * @param sone
	 *            The Sone this profile belongs to
	 */
	public Profile(Sone sone) {
		this.sone = sone;
	}

	/**
	 * Creates a copy of a profile.
	 *
	 * @param profile
	 *            The profile to copy
	 */
	public Profile(@Nonnull Profile profile) {
		this.sone = profile.sone;
		this.firstName = profile.firstName;
		this.middleName = profile.middleName;
		this.lastName = profile.lastName;
		this.birthDay = profile.birthDay;
		this.birthMonth = profile.birthMonth;
		this.birthYear = profile.birthYear;
		this.avatar = profile.avatar;
		this.fields.addAll(profile.fields);
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the Sone this profile belongs to.
	 *
	 * @return The Sone this profile belongs to
	 */
	@Nonnull
	public Sone getSone() {
		return sone;
	}

	/**
	 * Returns the first name.
	 *
	 * @return The first name
	 */
	@Nullable
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
	@Nonnull
	public Profile setFirstName(@Nullable String firstName) {
		this.firstName = firstName;
		return this;
	}

	/**
	 * Returns the middle name(s).
	 *
	 * @return The middle name
	 */
	@Nullable
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
	@Nonnull
	public Profile setMiddleName(@Nullable String middleName) {
		this.middleName = middleName;
		return this;
	}

	/**
	 * Returns the last name.
	 *
	 * @return The last name
	 */
	@Nullable
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
	@Nonnull
	public Profile setLastName(@Nullable String lastName) {
		this.lastName = lastName;
		return this;
	}

	/**
	 * Returns the day of the birth date.
	 *
	 * @return The day of the birth date (from 1 to 31)
	 */
	@Nullable
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
	@Nonnull
	public Profile setBirthDay(@Nullable Integer birthDay) {
		this.birthDay = birthDay;
		return this;
	}

	/**
	 * Returns the month of the birth date.
	 *
	 * @return The month of the birth date (from 1 to 12)
	 */
	@Nullable
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
	@Nonnull
	public Profile setBirthMonth(@Nullable Integer birthMonth) {
		this.birthMonth = birthMonth;
		return this;
	}

	/**
	 * Returns the year of the birth date.
	 *
	 * @return The year of the birth date
	 */
	@Nullable
	public Integer getBirthYear() {
		return birthYear;
	}

	/**
	 * Returns the ID of the currently selected avatar image.
	 *
	 * @return The ID of the currently selected avatar image, or {@code null} if
	 *         no avatar is selected.
	 */
	@Nullable
	public String getAvatar() {
		return avatar;
	}

	/**
	 * Sets the avatar image.
	 *
	 * @param avatar
	 *            The new avatar image, or {@code null} to not select an avatar
	 *            image.
	 * @return This Sone
	 */
	@Nonnull
	public Profile setAvatar(@Nullable Image avatar) {
		if (avatar == null) {
			this.avatar = null;
			return this;
		}
		checkArgument(avatar.getSone().equals(sone), "avatar must belong to Sone");
		this.avatar = avatar.getId();
		return this;
	}

	/**
	 * Sets the year of the birth date.
	 *
	 * @param birthYear
	 *            The year of the birth date
	 * @return This profile (for method chaining)
	 */
	@Nonnull
	public Profile setBirthYear(@Nullable Integer birthYear) {
		this.birthYear = birthYear;
		return this;
	}

	/**
	 * Returns the fields of this profile.
	 *
	 * @return The fields of this profile
	 */
	@Nonnull
	public List<Field> getFields() {
		return new ArrayList<Field>(fields);
	}

	/**
	 * Returns whether this profile contains the given field.
	 *
	 * @param field
	 *            The field to check for
	 * @return {@code true} if this profile contains the field, false otherwise
	 */
	public boolean hasField(@Nonnull Field field) {
		return fields.contains(field);
	}

	/**
	 * Returns the field with the given ID.
	 *
	 * @param fieldId
	 *            The ID of the field to get
	 * @return The field, or {@code null} if this profile does not contain a
	 *         field with the given ID
	 */
	@Nullable
	public Field getFieldById(@Nonnull String fieldId) {
		checkNotNull(fieldId, "fieldId must not be null");
		for (Field field : fields) {
			if (field.getId().equals(fieldId)) {
				return field;
			}
		}
		return null;
	}

	/**
	 * Returns the field with the given name.
	 *
	 * @param fieldName
	 *            The name of the field to get
	 * @return The field, or {@code null} if this profile does not contain a
	 *         field with the given name
	 */
	@Nullable
	public Field getFieldByName(@Nonnull String fieldName) {
		for (Field field : fields) {
			if (field.getName().equals(fieldName)) {
				return field;
			}
		}
		return null;
	}

	/**
	 * Appends a new field to the list of fields.
	 *
	 * @param fieldName
	 *            The name of the new field
	 * @return The new field
	 * @throws IllegalArgumentException
	 *             if the name is not valid
	 */
	@Nonnull
	public Field addField(@Nonnull String fieldName) throws IllegalArgumentException {
		checkNotNull(fieldName, "fieldName must not be null");
		if (fieldName.length() == 0) {
			throw new EmptyFieldName();
		}
		if (getFieldByName(fieldName) != null) {
			throw new DuplicateField();
		}
		@SuppressWarnings("synthetic-access")
		Field field = new Field().setName(fieldName).setValue("");
		fields.add(field);
		return field;
	}

	/**
	 * Moves the given field up one position in the field list. The index of the
	 * field to move must be greater than {@code 0} (because you obviously can
	 * not move the first field further up).
	 *
	 * @param field
	 *            The field to move up
	 */
	public void moveFieldUp(@Nonnull Field field) {
		checkNotNull(field, "field must not be null");
		checkArgument(hasField(field), "field must belong to this profile");
		checkArgument(getFieldIndex(field) > 0, "field index must be > 0");
		int fieldIndex = getFieldIndex(field);
		fields.remove(field);
		fields.add(fieldIndex - 1, field);
	}

	/**
	 * Moves the given field down one position in the field list. The index of
	 * the field to move must be less than the index of the last field (because
	 * you obviously can not move the last field further down).
	 *
	 * @param field
	 *            The field to move down
	 */
	public void moveFieldDown(@Nonnull Field field) {
		checkNotNull(field, "field must not be null");
		checkArgument(hasField(field), "field must belong to this profile");
		checkArgument(getFieldIndex(field) < fields.size() - 1, "field index must be < " + (fields.size() - 1));
		int fieldIndex = getFieldIndex(field);
		fields.remove(field);
		fields.add(fieldIndex + 1, field);
	}

	/**
	 * Removes the given field.
	 *
	 * @param field
	 *            The field to remove
	 */
	public void removeField(@Nonnull Field field) {
		checkNotNull(field, "field must not be null");
		checkArgument(hasField(field), "field must belong to this profile");
		fields.remove(field);
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
	private int getFieldIndex(@Nonnull Field field) {
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
		Hasher hash = Hashing.sha256().newHasher();
		hash.putString("Profile(");
		if (firstName != null) {
			hash.putString("FirstName(").putString(firstName).putString(")");
		}
		if (middleName != null) {
			hash.putString("MiddleName(").putString(middleName).putString(")");
		}
		if (lastName != null) {
			hash.putString("LastName(").putString(lastName).putString(")");
		}
		if (birthDay != null) {
			hash.putString("BirthDay(").putInt(birthDay).putString(")");
		}
		if (birthMonth != null) {
			hash.putString("BirthMonth(").putInt(birthMonth).putString(")");
		}
		if (birthYear != null) {
			hash.putString("BirthYear(").putInt(birthYear).putString(")");
		}
		if (avatar != null) {
			hash.putString("Avatar(").putString(avatar).putString(")");
		}
		hash.putString("ContactInformation(");
		for (Field field : fields) {
			hash.putString(field.getName()).putString("(").putString(field.getValue()).putString(")");
		}
		hash.putString(")");
		hash.putString(")");

		return hash.hash().toString();
	}

	/**
	 * Container for a profile field.
	 */
	public class Field {

		/** The ID of the field. */
		private final String id;

		/** The name of the field. */
		private String name;

		/** The value of the field. */
		private String value;

		/**
		 * Creates a new field with a random ID.
		 */
		private Field() {
			this(UUID.randomUUID().toString());
		}

		/**
		 * Creates a new field with the given ID.
		 *
		 * @param id
		 *            The ID of the field
		 */
		private Field(@Nonnull String id) {
			this.id = checkNotNull(id, "id must not be null");
		}

		/**
		 * Returns the ID of this field.
		 *
		 * @return The ID of this field
		 */
		@Nonnull
		public String getId() {
			return id;
		}

		/**
		 * Returns the name of this field.
		 *
		 * @return The name of this field
		 */
		@Nonnull
		public String getName() {
			return name;
		}

		/**
		 * Sets the name of this field. The name must not be {@code null} and
		 * must not match any other fields in this profile but my match the name
		 * of this field.
		 *
		 * @param name
		 *            The new name of this field
		 * @return This field
		 */
		@Nonnull
		public Field setName(@Nonnull String name) {
			checkNotNull(name, "name must not be null");
			checkArgument(getFieldByName(name) == null, "name must be unique");
			this.name = name;
			return this;
		}

		/**
		 * Returns the value of this field.
		 *
		 * @return The value of this field
		 */
		@Nullable
		public String getValue() {
			return value;
		}

		/**
		 * Sets the value of this field. While {@code null} is allowed, no
		 * guarantees are made that {@code null} values are correctly persisted
		 * across restarts of the plugin!
		 *
		 * @param value
		 *            The new value of this field
		 * @return This field
		 */
		@Nonnull
		public Field setValue(@Nullable String value) {
			this.value = value;
			return this;
		}

		//
		// OBJECT METHODS
		//

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object object) {
			if (!(object instanceof Field)) {
				return false;
			}
			Field field = (Field) object;
			return id.equals(field.id);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			return id.hashCode();
		}

	}

	/**
	 * Exception that signals the addition of a field with an empty name.
	 */
	public static class EmptyFieldName extends IllegalArgumentException { }

	/**
	 * Exception that signals the addition of a field that already exists.
	 */
	public static class DuplicateField extends IllegalArgumentException { }

}
