/*
 * FreenetSone - SoneInserter.java - Copyright © 2010 David Roden
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

package net.pterodactylus.sone.core;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.StringBucket;
import net.pterodactylus.util.io.Closer;
import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.service.AbstractService;
import net.pterodactylus.util.template.DefaultTemplateFactory;
import net.pterodactylus.util.template.ReflectionAccessor;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateException;
import net.pterodactylus.util.template.XmlFilter;
import freenet.client.async.ManifestElement;
import freenet.keys.FreenetURI;

/**
 * A Sone inserter is responsible for inserting a Sone if it has changed.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneInserter extends AbstractService {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(SoneInserter.class);

	/** The template factory used to create the templates. */
	private static final DefaultTemplateFactory templateFactory = new DefaultTemplateFactory();

	static {
		templateFactory.addAccessor(Object.class, new ReflectionAccessor());
		templateFactory.addFilter("xml", new XmlFilter());
	}

	/** The UTF-8 charset. */
	private static final Charset utf8Charset = Charset.forName("UTF-8");

	/** The Freenet interface. */
	private final FreenetInterface freenetInterface;

	/** The Sone to insert. */
	private final Sone sone;

	/**
	 * Creates a new Sone inserter.
	 *
	 * @param freenetInterface
	 *            The freenet interface
	 * @param sone
	 *            The Sone to insert
	 */
	public SoneInserter(FreenetInterface freenetInterface, Sone sone) {
		super("Sone Inserter for “" + sone.getName() + "”");
		this.freenetInterface = freenetInterface;
		this.sone = sone;
	}

	//
	// SERVICE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void serviceRun() {
		long modificationCounter = 0;
		boolean restartNow = true;
		while (!shouldStop()) {
			if (!restartNow) {
				logger.log(Level.FINEST, "Waiting 60 seconds before checking Sone “" + sone.getName() + "”.");
				sleep(60 * 1000);
			}
			restartNow = false;
			InsertInformation insertInformation = null;
			synchronized (sone) {
				modificationCounter = sone.getModificationCounter();
				if (modificationCounter > 0) {
					sone.setTime(System.currentTimeMillis());
					insertInformation = new InsertInformation(sone.getRequestUri(), sone.getInsertUri());
				}
			}
			if (insertInformation != null) {
				logger.log(Level.INFO, "Inserting Sone “%s”…", new Object[] { sone.getName() });

				boolean success = false;
				try {
					FreenetURI finalUri = freenetInterface.insertDirectory(insertInformation.getInsertUri().setKeyType("USK").setDocName("Sone-" + sone.getName()).setSuggestedEdition(0), insertInformation.generateManifestEntries(), "index.html");
					sone.updateUris(finalUri);
					success = true;
					logger.log(Level.INFO, "Inserted Sone “%s” at %s.", new Object[] { sone.getName(), finalUri });
				} catch (SoneException se1) {
					logger.log(Level.WARNING, "Could not insert Sone “" + sone.getName() + "”!", se1);
				}

				/*
				 * reset modification counter if Sone has not been modified
				 * while it was inserted.
				 */
				if (success) {
					synchronized (sone) {
						if (sone.getModificationCounter() == modificationCounter) {
							logger.log(Level.FINE, "Sone “%s” was not modified further, resetting counter…", new Object[] { sone });
							sone.setModificationCounter(0);
						} else {
							logger.log(Level.FINE, "Sone “%s” was modified since the insert started, starting another insert…", new Object[] { sone });
							restartNow = true;
						}
					}
				}
			}
		}
	}

	/**
	 * Container for information that are required to insert a Sone. This
	 * container merely exists to copy all relevant data without holding a lock
	 * on the {@link Sone} object for too long.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	private class InsertInformation {

		/** The request URI of the Sone. */
		private final FreenetURI requestUri;

		/** The insert URI of the Sone. */
		private final FreenetURI insertUri;

		/**
		 * Creates a new insert information container.
		 *
		 * @param requestUri
		 *            The request URI of the Sone
		 * @param insertUri
		 *            The insert URI of the Sone
		 */
		public InsertInformation(FreenetURI requestUri, FreenetURI insertUri) {
			this.requestUri = requestUri;
			this.insertUri = insertUri;
		}

		//
		// ACCESSORS
		//

		/**
		 * Returns the request URI of the Sone.
		 *
		 * @return The request URI of the Sone
		 */
		@SuppressWarnings("unused")
		public FreenetURI getRequestUri() {
			return requestUri;
		}

		/**
		 * Returns the insert URI of the Sone.
		 *
		 * @return The insert URI of the Sone
		 */
		public FreenetURI getInsertUri() {
			return insertUri;
		}

		//
		// ACTIONS
		//

		/**
		 * Generates all manifest entries required to insert this Sone.
		 *
		 * @return The manifest entries for the Sone insert
		 */
		public HashMap<String, Object> generateManifestEntries() {
			HashMap<String, Object> manifestEntries = new HashMap<String, Object>();

			/* first, create an index.html. */
			manifestEntries.put("index.html", createManifestElement("index.html", "text/html; charset=utf-8", "/templates/insert/index.html"));

			/* now, store the sone. */
			manifestEntries.put("sone.xml", createManifestElement("sone.xml", "text/xml; charset=utf-8", "/templates/insert/sone.xml"));

			return manifestEntries;
		}

		//
		// PRIVATE METHODS
		//

		/**
		 * Creates a new manifest element.
		 *
		 * @param name
		 *            The name of the file
		 * @param contentType
		 *            The content type of the file
		 * @param templateName
		 *            The name of the template to render
		 * @return The manifest element
		 */
		@SuppressWarnings("synthetic-access")
		private ManifestElement createManifestElement(String name, String contentType, String templateName) {
			InputStreamReader templateInputStreamReader;
			Template template = templateFactory.createTemplate(templateInputStreamReader = new InputStreamReader(getClass().getResourceAsStream(templateName), utf8Charset));
			try {
				template.parse();
			} catch (TemplateException te1) {
				logger.log(Level.SEVERE, "Could not parse template “" + templateName + "”!", te1);
				return null;
			} finally {
				Closer.close(templateInputStreamReader);
			}
			template.set("currentSone", sone);
			StringWriter writer = new StringWriter();
			StringBucket bucket = null;
			try {
				template.render(writer);
				bucket = new StringBucket(writer.toString(), utf8Charset);
				return new ManifestElement(name, bucket, contentType, bucket.size());
			} catch (TemplateException te1) {
				logger.log(Level.SEVERE, "Could not render template “" + templateName + "”!", te1);
				return null;
			} finally {
				Closer.close(writer);
				if (bucket != null) {
					bucket.free();
				}
			}
		}

	}

}
