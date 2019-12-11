/*
 * Sone - SonePlugin.java - Copyright © 2010–2019 David Roden
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

import static java.util.logging.Logger.*;

import java.util.logging.Logger;
import java.util.logging.*;

import net.pterodactylus.sone.core.*;
import net.pterodactylus.sone.core.event.*;
import net.pterodactylus.sone.fcp.*;
import net.pterodactylus.sone.freenet.wot.*;
import net.pterodactylus.sone.web.*;
import net.pterodactylus.sone.web.notification.NotificationHandler;
import net.pterodactylus.sone.web.notification.NotificationHandlerModule;

import freenet.l10n.BaseL10n.*;
import freenet.l10n.*;
import freenet.pluginmanager.*;
import freenet.support.*;
import freenet.support.api.*;

import com.google.common.annotations.*;
import com.google.common.eventbus.*;
import com.google.common.cache.*;
import com.google.inject.*;
import com.google.inject.Module;
import com.google.inject.name.*;
import kotlin.jvm.functions.*;

/**
 * This class interfaces with Freenet. It is the class that is loaded by the
 * node and starts up the whole Sone system.
 */
public class SonePlugin implements FredPlugin, FredPluginFCP, FredPluginL10n, FredPluginBaseL10n, FredPluginThreadless, FredPluginVersioned {

	private static final Logger soneLogger = getLogger("net.pterodactylus.sone");

	static {
		/* initialize logging. */
		soneLogger.setUseParentHandlers(false);
		soneLogger.setLevel(Level.ALL);
		soneLogger.addHandler(new Handler() {
			private final LoadingCache<String, Class<?>> classCache = CacheBuilder.newBuilder()
					.build(new CacheLoader<String, Class<?>>() {
						@Override
						public Class<?> load(String key) throws Exception {
							return SonePlugin.class.getClassLoader().loadClass(key);
						}
					});

			@Override
			public void publish(LogRecord logRecord) {
				int recordLevel = logRecord.getLevel().intValue();
				Class<?> loggingClass = classCache.getUnchecked(logRecord.getLoggerName());
				if (recordLevel < Level.FINE.intValue()) {
					freenet.support.Logger.debug(loggingClass, logRecord.getMessage(), logRecord.getThrown());
				} else if (recordLevel < Level.INFO.intValue()) {
					freenet.support.Logger.minor(loggingClass, logRecord.getMessage(), logRecord.getThrown());
				} else if (recordLevel < Level.WARNING.intValue()) {
					freenet.support.Logger.normal(loggingClass, logRecord.getMessage(), logRecord.getThrown());
				} else if (recordLevel < Level.SEVERE.intValue()) {
					freenet.support.Logger.warning(loggingClass, logRecord.getMessage(), logRecord.getThrown());
				} else {
					freenet.support.Logger.error(loggingClass, logRecord.getMessage(), logRecord.getThrown());
				}
			}

			@Override
			public void flush() {
			}

			@Override
			public void close() {
			}
		});
	}

	/** The current year at time of release. */
	private static final int YEAR = 2019;
	private static final String SONE_HOMEPAGE = "USK@nwa8lHa271k2QvJ8aa0Ov7IHAV-DFOCFgmDt3X6BpCI,DuQSUZiI~agF8c-6tjsFFGuZ8eICrzWCILB60nT8KKo,AQACAAE/sone/";
	private static final int LATEST_EDITION = 79;

	/** The logger. */
	private static final Logger logger = getLogger(SonePlugin.class.getName());

	private final Function1<Module[], Injector> injectorCreator;

	/** The plugin respirator. */
	private PluginRespirator pluginRespirator;

	/** The core. */
	private Core core;

	/** The web interface. */
	private WebInterface webInterface;

	/** The FCP interface. */
	private FcpInterface fcpInterface;

	/** The l10n helper. */
	private PluginL10n l10n;

	/** The web of trust connector. */
	private WebOfTrustConnector webOfTrustConnector;

	public SonePlugin() {
		this(Guice::createInjector);
	}

	@VisibleForTesting
	public SonePlugin(Function1<Module[], Injector> injectorCreator) {
		this.injectorCreator = injectorCreator;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the plugin respirator for this plugin.
	 *
	 * @return The plugin respirator
	 */
	public PluginRespirator pluginRespirator() {
		return pluginRespirator;
	}

	/**
	 * Returns the core started by this plugin.
	 *
	 * @return The core
	 */
	public Core core() {
		return core;
	}

	/**
	 * Returns the plugin’s l10n helper.
	 *
	 * @return The plugin’s l10n helper
	 */
	public PluginL10n l10n() {
		return l10n;
	}

	public static String getPluginVersion() {
		net.pterodactylus.sone.main.Version version = VersionParserKt.getParsedVersion();
		return (version == null) ? "unknown" : version.getNice();
	}

	public int getYear() {
		return YEAR;
	}

	public String getHomepage() {
		return SONE_HOMEPAGE + LATEST_EDITION;
	}

	public static long getLatestEdition() {
		return LATEST_EDITION;
	}

	//
	// FREDPLUGIN METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void runPlugin(PluginRespirator pluginRespirator) {
		this.pluginRespirator = pluginRespirator;

		Injector injector = createInjector();
		core = injector.getInstance(Core.class);

		/* create web of trust connector. */
		webOfTrustConnector = injector.getInstance(WebOfTrustConnector.class);

		/* create FCP interface. */
		fcpInterface = injector.getInstance(FcpInterface.class);

		/* create the web interface. */
		webInterface = injector.getInstance(WebInterface.class);

		/* we need to request this to install all notification handlers. */
		injector.getInstance(NotificationHandler.class);

		/* start core! */
		core.start();

		/* start the web interface! */
		webInterface.start();

		/* first start? */
		if (injector.getInstance(Key.get(Boolean.class, Names.named("FirstStart")))) {
			injector.getInstance(EventBus.class).post(new FirstStart());
		} else {
			/* new config? */
			if (injector.getInstance(Key.get(Boolean.class, Names.named("NewConfig")))) {
				injector.getInstance(EventBus.class).post(new ConfigNotRead());
			}
		}
	}

	@VisibleForTesting
	protected Injector createInjector() {
		FreenetModule freenetModule = new FreenetModule(pluginRespirator);
		AbstractModule soneModule = new SoneModule(this, new EventBus());
		Module webInterfaceModule = new WebInterfaceModule();
		Module notificationHandlerModule = new NotificationHandlerModule();

		return createInjector(freenetModule, soneModule, webInterfaceModule, notificationHandlerModule);
	}

	@VisibleForTesting
	protected Injector createInjector(Module... modules) {
		return injectorCreator.invoke(modules);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void terminate() {
		try {
			/* stop the web interface. */
			webInterface.stop();

			/* stop the core. */
			core.stop();

			/* stop the web of trust connector. */
			webOfTrustConnector.stop();
		} catch (Throwable t1) {
			logger.log(Level.SEVERE, "Error while shutting down!", t1);
		} finally {
			deregisterLoggerHandlers();
		}
	}

	private void deregisterLoggerHandlers() {
		for (Handler handler : soneLogger.getHandlers()) {
			soneLogger.removeHandler(handler);
		}
	}

	//
	// INTERFACE FredPluginFCP
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handle(PluginReplySender pluginReplySender, SimpleFieldSet parameters, Bucket data, int accessType) {
		fcpInterface.handle(pluginReplySender, parameters, data, accessType);
	}

	//
	// INTERFACE FredPluginL10n
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getString(String key) {
		return l10n.getBase().getString(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setLanguage(LANGUAGE newLanguage) {
		l10n = new PluginL10n(this, newLanguage);
	}

	//
	// INTERFACE FredPluginBaseL10n
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getL10nFilesBasePath() {
		return "i18n";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getL10nFilesMask() {
		return "sone.${lang}.properties";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getL10nOverrideFilesMask() {
		return "sone.${lang}.override.properties";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ClassLoader getPluginClassLoader() {
		return SonePlugin.class.getClassLoader();
	}

	//
	// INTERFACE FredPluginVersioned
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getVersion() {
		return getPluginVersion();
	}

}
