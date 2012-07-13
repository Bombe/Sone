/*
 * Sone - OwnIdentity.java - Copyright © 2010–2012 David Roden
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

import java.util.Map;
import java.util.Set;

/**
 * Defines a local identity, an own identity.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface OwnIdentity extends Identity {

	/**
	 * Returns the insert URI of the identity.
	 *
	 * @return The insert URI of the identity
	 */
	public String getInsertUri();

	/**
	 * Adds the given context to this identity.
	 * <p>
	 * This method is only called by the {@link IdentityManager}.
	 *
	 * @param context
	 *            The context to add
	 * @throws WebOfTrustException
	 *             if an error occurs
	 */
	public void addContext(String context) throws WebOfTrustException;

	/**
	 * Sets all contexts of this identity.
	 * <p>
	 * This method is only called by the {@link IdentityManager}.
	 *
	 * @param contexts
	 *            All contexts of the identity
	 * @throws WebOfTrustException
	 *             if an error occurs
	 */
	public void setContexts(Set<String> contexts) throws WebOfTrustException;

	/**
	 * Removes the given context from this identity.
	 * <p>
	 * This method is only called by the {@link IdentityManager}.
	 *
	 * @param context
	 *            The context to remove
	 * @throws WebOfTrustException
	 *             if an error occurs
	 */
	public void removeContext(String context) throws WebOfTrustException;

	/**
	 * Sets the property with the given name to the given value.
	 *
	 * @param name
	 *            The name of the property
	 * @param value
	 *            The value of the property
	 * @throws WebOfTrustException
	 *             if an error occurs
	 */
	public void setProperty(String name, String value) throws WebOfTrustException;

	/**
	 * Sets all properties of this identity.
	 * <p>
	 * This method is only called by the {@link IdentityManager}.
	 *
	 * @param properties
	 *            The new properties of this identity
	 * @throws WebOfTrustException
	 *             if an error occurs
	 */
	public void setProperties(Map<String, String> properties) throws WebOfTrustException;

	/**
	 * Removes the property with the given name.
	 * <p>
	 * This method is only called by the {@link IdentityManager}.
	 *
	 * @param name
	 *            The name of the property to remove
	 * @throws WebOfTrustException
	 *             if an error occurs
	 */
	public void removeProperty(String name) throws WebOfTrustException;

	/**
	 * Sets the trust for the given target identity.
	 *
	 * @param target
	 *            The target to set the trust for
	 * @param trustValue
	 *            The new trust value (from {@code -100} or {@code 100})
	 * @param comment
	 *            The comment for the trust assignment
	 * @throws WebOfTrustException
	 *             if an error occurs
	 */
	public void setTrust(Identity target, int trustValue, String comment) throws WebOfTrustException;

	/**
	 * Removes any trust assignment for the given target identity.
	 *
	 * @param target
	 *            The targe to remove the trust assignment for
	 * @throws WebOfTrustException
	 *             if an error occurs
	 */
	public void removeTrust(Identity target) throws WebOfTrustException;

}
