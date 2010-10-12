/*
 * FreenetSone - SonePlugin.java - Copyright © 2010 David Roden
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

package net.pterodactylus.sone.main;

import java.io.File;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.core.FreenetInterface;
import net.pterodactylus.sone.freenet.PluginStoreConfigurationBackend;
import net.pterodactylus.util.config.Configuration;
import net.pterodactylus.util.config.ConfigurationException;
import net.pterodactylus.util.config.MapConfigurationBackend;
import net.pterodactylus.util.config.XMLConfigurationBackend;
import net.pterodactylus.util.logging.Logging;
import freenet.client.async.DatabaseDisabledException;
import freenet.pluginmanager.FredPlugin;
import freenet.pluginmanager.PluginRespirator;

/**
 * This class interfaces with Freenet. It is the class that is loaded by the
 * node and starts up the whole Sone system.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SonePlugin implements FredPlugin {

	static {
		/* initialize logging. */
		Logging.setup("sone");
	}

	/** The logger. */
	private static final Logger logger = Logging.getLogger(SonePlugin.class);

	/** The core. */
	private Core core;

	//
	// FREDPLUGIN METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void runPlugin(PluginRespirator pluginRespirator) {

		/* create a configuration. */
		Configuration configuration;
		try {
			configuration = new Configuration(new PluginStoreConfigurationBackend(pluginRespirator.getStore()));
		} catch (DatabaseDisabledException dde1) {
			logger.log(Level.WARNING, "Could not load plugin store, using XML files.");
			try {
				configuration = new Configuration(new XMLConfigurationBackend(new File("sone.xml"), true));
			} catch (ConfigurationException ce1) {
				logger.log(Level.SEVERE, "Could not load or create the “sone.xml” configuration file!");
				configuration = new Configuration(new MapConfigurationBackend(Collections.<String, String> emptyMap()));
			}
		}

		/* create freenet interface. */
		FreenetInterface freenetInterface = new FreenetInterface(pluginRespirator.getNode(), pluginRespirator.getHLSimpleClient());

		/* create core. */
		core = new Core();
		core.configuration(configuration);
		core.freenetInterface(freenetInterface);

		/* start core! */
		core.start();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void terminate() {
		/* stop the core. */
		core.stop();

		/* TODO wait for core to stop? */
	}

}
