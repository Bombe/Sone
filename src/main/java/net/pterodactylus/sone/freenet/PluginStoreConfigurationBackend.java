/*
 * FreenetSone - PluginStoreConfigurationBackend.java - Copyright © 2010 David Roden
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

package net.pterodactylus.sone.freenet;

import net.pterodactylus.util.config.Configuration;
import net.pterodactylus.util.config.ConfigurationException;
import net.pterodactylus.util.config.ExtendedConfigurationBackend;
import freenet.pluginmanager.PluginStore;

/**
 * Backend for a {@link Configuration} that is based on a {@link PluginStore}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PluginStoreConfigurationBackend implements ExtendedConfigurationBackend {

	/** The backing plugin store. */
	private final PluginStore pluginStore;

	/**
	 * Creates a new configuration backend based on a plugin store.
	 *
	 * @param pluginStore
	 *            The backing plugin store
	 */
	public PluginStoreConfigurationBackend(PluginStore pluginStore) {
		this.pluginStore = pluginStore;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getValue(String attribute) throws ConfigurationException {
		return pluginStore.strings.get(attribute);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void putValue(String attribute, String value) throws ConfigurationException {
		pluginStore.strings.put(attribute, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean getBooleanValue(String attribute) throws ConfigurationException {
		return pluginStore.booleans.get(attribute);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setBooleanValue(String attribute, Boolean value) throws ConfigurationException {
		pluginStore.booleans.put(attribute, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Double getDoubleValue(String attribute) throws ConfigurationException {
		String stringValue = pluginStore.strings.get(attribute);
		if (stringValue == null) {
			return null;
		}
		try {
			return Double.valueOf(pluginStore.strings.get(attribute));
		} catch (NumberFormatException nfe1) {
			throw new ConfigurationException("Could not parse “" + stringValue + "”.", nfe1);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDoubleValue(String attribute, Double value) throws ConfigurationException {
		pluginStore.strings.put(attribute, String.valueOf(value));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer getIntegerValue(String attribute) throws ConfigurationException {
		return pluginStore.integers.get(attribute);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIntegerValue(String attribute, Integer value) throws ConfigurationException {
		pluginStore.integers.put(attribute, value);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getLongValue(String attribute) throws ConfigurationException {
		return pluginStore.longs.get(attribute);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLongValue(String attribute, Long value) throws ConfigurationException {
		pluginStore.longs.put(attribute, value);
	}

}
