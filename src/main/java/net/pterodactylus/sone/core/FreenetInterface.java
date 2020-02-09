/*
 * Sone - FreenetInterface.java - Copyright © 2010–2020 David Roden
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

import static freenet.keys.USK.create;
import static java.lang.String.format;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import net.pterodactylus.sone.core.event.ImageInsertAbortedEvent;
import net.pterodactylus.sone.core.event.ImageInsertFailedEvent;
import net.pterodactylus.sone.core.event.ImageInsertFinishedEvent;
import net.pterodactylus.sone.core.event.ImageInsertStartedEvent;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.TemporaryImage;

import com.google.common.base.Function;
import com.google.common.eventbus.EventBus;
import com.google.inject.Singleton;

import freenet.client.ClientMetadata;
import freenet.client.FetchContext;
import freenet.client.FetchException;
import freenet.client.FetchException.FetchExceptionMode;
import freenet.client.FetchResult;
import freenet.client.HighLevelSimpleClient;
import freenet.client.InsertBlock;
import freenet.client.InsertContext;
import freenet.client.InsertException;
import freenet.client.Metadata;
import freenet.client.async.BaseClientPutter;
import freenet.client.async.ClientContext;
import freenet.client.async.ClientGetCallback;
import freenet.client.async.ClientGetter;
import freenet.client.async.ClientPutCallback;
import freenet.client.async.ClientPutter;
import freenet.client.async.SnoopMetadata;
import freenet.client.async.USKCallback;
import freenet.keys.FreenetURI;
import freenet.keys.InsertableClientSSK;
import freenet.keys.USK;
import freenet.node.Node;
import freenet.node.RequestClient;
import freenet.node.RequestClientBuilder;
import freenet.node.RequestStarter;
import freenet.support.api.Bucket;
import freenet.support.api.RandomAccessBucket;
import freenet.support.io.ArrayBucket;
import freenet.support.io.ResumeFailedException;
import net.pterodactylus.sone.freenet.*;

/**
 * Contains all necessary functionality for interacting with the Freenet node.
 */
@Singleton
public class FreenetInterface {

	/** The logger. */
	private static final Logger logger = getLogger(FreenetInterface.class.getName());

	/** The event bus. */
	private final EventBus eventBus;

	/** The node to interact with. */
	private final Node node;

	/** The high-level client to use for requests. */
	private final HighLevelSimpleClient client;
	private final RequestClient requestClient = new RequestClientBuilder().realTime().build();

	/** The USK callbacks. */
	private final Map<String, USKCallback> soneUskCallbacks = new HashMap<>();

	/** The not-Sone-related USK callbacks. */
	private final Map<FreenetURI, USKCallback> uriUskCallbacks = Collections.synchronizedMap(new HashMap<FreenetURI, USKCallback>());

	private final RequestClient imageInserts = new RequestClientBuilder().realTime().build();
	private final RequestClient imageLoader = new RequestClientBuilder().realTime().build();

	/**
	 * Creates a new Freenet interface.
	 *
	 * @param eventBus
	 *            The event bus
	 * @param node
	 *            The node to interact with
	 */
	@Inject
	public FreenetInterface(EventBus eventBus, Node node) {
		this.eventBus = eventBus;
		this.node = node;
		this.client = node.clientCore.makeClient(RequestStarter.INTERACTIVE_PRIORITY_CLASS, false, true);
	}

	//
	// ACTIONS
	//

	/**
	 * Fetches the given URI.
	 *
	 * @param uri
	 *            The URI to fetch
	 * @return The result of the fetch, or {@code null} if an error occured
	 */
	public Fetched fetchUri(FreenetURI uri) {
		FreenetURI currentUri = new FreenetURI(uri);
		while (true) {
			try {
				FetchResult fetchResult = client.fetch(currentUri);
				return new Fetched(currentUri, fetchResult);
			} catch (FetchException fe1) {
				if (fe1.getMode() == FetchExceptionMode.PERMANENT_REDIRECT) {
					currentUri = fe1.newURI;
					continue;
				}
				logger.log(Level.WARNING, String.format("Could not fetch “%s”!", uri), fe1);
				return null;
			}
		}
	}

	public void startFetch(final FreenetURI uri, final BackgroundFetchCallback backgroundFetchCallback) {
		ClientGetCallback callback = new ClientGetCallback() {
			@Override
			public void onSuccess(FetchResult result, ClientGetter state) {
				try {
					backgroundFetchCallback.loaded(uri, result.getMimeType(), result.asByteArray());
				} catch (IOException e) {
					backgroundFetchCallback.failed(uri);
				}
			}

			@Override
			public void onFailure(FetchException e, ClientGetter state) {
				backgroundFetchCallback.failed(uri);
			}

			@Override
			public void onResume(ClientContext context) throws ResumeFailedException {
				/* do nothing. */
			}

			@Override
			public RequestClient getRequestClient() {
				return imageLoader;
			}
		};
		SnoopMetadata snoop = new SnoopMetadata() {
			@Override
			public boolean snoopMetadata(Metadata meta, ClientContext context) {
				String mimeType = meta.getMIMEType();
				boolean cancel = (mimeType == null) || backgroundFetchCallback.shouldCancel(uri, mimeType, meta.dataLength());
				if (cancel) {
					backgroundFetchCallback.failed(uri);
				}
				return cancel;
			}
		};
		FetchContext fetchContext = client.getFetchContext();
		try {
			ClientGetter clientGetter = client.fetch(uri, 2097152, callback, fetchContext, RequestStarter.INTERACTIVE_PRIORITY_CLASS);
			clientGetter.setMetaSnoop(snoop);
			clientGetter.restart(uri, fetchContext.filterData, node.clientCore.clientContext);
		} catch (FetchException fe) {
			/* stupid exception that can not actually be thrown! */
		}
	}

	public interface BackgroundFetchCallback {
		boolean shouldCancel(@Nonnull FreenetURI uri, @Nonnull String mimeType, long size);
		void loaded(@Nonnull FreenetURI uri, @Nonnull String mimeType, @Nonnull byte[] data);
		void failed(@Nonnull FreenetURI uri);
	}

	/**
	 * Inserts the image data of the given {@link TemporaryImage} and returns
	 * the given insert token that can be used to add listeners or cancel the
	 * insert.
	 *
	 * @param temporaryImage
	 *            The temporary image data
	 * @param image
	 *            The image
	 * @param insertToken
	 *            The insert token
	 * @throws SoneException
	 *             if the insert could not be started
	 */
	public void insertImage(TemporaryImage temporaryImage, Image image, InsertToken insertToken) throws SoneException {
		String filenameHint = image.getId() + "." + temporaryImage.getMimeType().substring(temporaryImage.getMimeType().lastIndexOf("/") + 1);
		InsertableClientSSK key = InsertableClientSSK.createRandom(node.random, "");
		FreenetURI targetUri = key.getInsertURI().setDocName(filenameHint);
		InsertContext insertContext = client.getInsertContext(true);
		RandomAccessBucket bucket = new ArrayBucket(temporaryImage.getImageData());
		insertToken.setBucket(bucket);
		ClientMetadata metadata = new ClientMetadata(temporaryImage.getMimeType());
		InsertBlock insertBlock = new InsertBlock(bucket, metadata, targetUri);
		try {
			ClientPutter clientPutter = client.insert(insertBlock, null, false, insertContext, insertToken, RequestStarter.INTERACTIVE_PRIORITY_CLASS);
			insertToken.setClientPutter(clientPutter);
		} catch (InsertException ie1) {
			throw new SoneInsertException("Could not start image insert.", ie1);
		}
	}

	/**
	 * Inserts a directory into Freenet.
	 *
	 * @param insertUri
	 *            The insert URI
	 * @param manifestEntries
	 *            The directory entries
	 * @param defaultFile
	 *            The name of the default file
	 * @return The generated URI
	 * @throws SoneException
	 *             if an insert error occurs
	 */
	public FreenetURI insertDirectory(FreenetURI insertUri, HashMap<String, Object> manifestEntries, String defaultFile) throws SoneException {
		try {
			return client.insertManifest(insertUri, manifestEntries, defaultFile);
		} catch (InsertException ie1) {
			throw new SoneException(ie1);
		}
	}

	public void registerActiveUsk(FreenetURI requestUri,
			USKCallback uskCallback) {
		try {
			soneUskCallbacks.put(FreenetURIsKt.getRoutingKeyString(requestUri), uskCallback);
			node.clientCore.uskManager.subscribe(create(requestUri),
					uskCallback, true, requestClient);
		} catch (MalformedURLException mue1) {
			logger.log(WARNING, format("Could not subscribe USK “%s”!",
					requestUri), mue1);
		}
	}

	public void registerPassiveUsk(FreenetURI requestUri,
			USKCallback uskCallback) {
		try {
			soneUskCallbacks.put(FreenetURIsKt.getRoutingKeyString(requestUri), uskCallback);
			node.clientCore
					.uskManager
					.subscribe(create(requestUri), uskCallback, false, requestClient);
		} catch (MalformedURLException mue1) {
			logger.log(WARNING,
					format("Could not subscribe USK “%s”!", requestUri),
					mue1);
		}
	}

	/**
	 * Unsubscribes the request URI of the given Sone.
	 *
	 * @param sone
	 *            The Sone to unregister
	 */
	public void unregisterUsk(Sone sone) {
		USKCallback uskCallback = soneUskCallbacks.remove(sone.getId());
		if (uskCallback == null) {
			return;
		}
		try {
			logger.log(Level.FINEST, String.format("Unsubscribing from USK for %s…", sone));
			node.clientCore.uskManager.unsubscribe(USK.create(sone.getRequestUri()), uskCallback);
		} catch (MalformedURLException mue1) {
			logger.log(Level.FINE, String.format("Could not unsubscribe USK “%s”!", sone.getRequestUri()), mue1);
		}
	}

	/**
	 * Registers an arbitrary URI and calls the given callback if a new edition
	 * is found.
	 *
	 * @param uri
	 *            The URI to watch
	 * @param callback
	 *            The callback to call
	 */
	public void registerUsk(FreenetURI uri, final Callback callback) {
		USKCallback uskCallback = new USKCallback() {

			@Override
			public void onFoundEdition(long edition, USK key, ClientContext clientContext, boolean metadata, short codec, byte[] data, boolean newKnownGood, boolean newSlotToo) {
				callback.editionFound(key.getURI(), edition, newKnownGood, newSlotToo);
			}

			@Override
			public short getPollingPriorityNormal() {
				return RequestStarter.PREFETCH_PRIORITY_CLASS;
			}

			@Override
			public short getPollingPriorityProgress() {
				return RequestStarter.INTERACTIVE_PRIORITY_CLASS;
			}

		};
		try {
			node.clientCore.uskManager.subscribe(USK.create(uri), uskCallback, true, requestClient);
			uriUskCallbacks.put(USK.create(uri).clearCopy().getURI(), uskCallback);
		} catch (MalformedURLException mue1) {
			logger.log(Level.WARNING, String.format("Could not subscribe to USK: %s", uri), mue1);
		}
	}

	/**
	 * Unregisters the USK watcher for the given URI.
	 *
	 * @param uri
	 *            The URI to unregister the USK watcher for
	 */
	public void unregisterUsk(FreenetURI uri) {
		try {
			USKCallback uskCallback = uriUskCallbacks.remove(USK.create(uri).clearCopy().getURI());
			if (uskCallback == null) {
				logger.log(Level.INFO, String.format("Could not unregister unknown USK: %s", uri));
				return;
			}
			node.clientCore.uskManager.unsubscribe(USK.create(uri), uskCallback);
		} catch (MalformedURLException mue1) {
			logger.log(Level.INFO, String.format("Could not unregister invalid USK: %s", uri), mue1);
		}
	}

	/**
	 * Callback for USK watcher events.
	 */
	public static interface Callback {

		/**
		 * Notifies a listener that a new edition was found for a URI.
		 *
		 * @param uri
		 *            The URI that a new edition was found for
		 * @param edition
		 *            The found edition
		 * @param newKnownGood
		 *            Whether the found edition was actually fetched
		 * @param newSlot
		 *            Whether the found edition is higher than all previously
		 *            found editions
		 */
		public void editionFound(FreenetURI uri, long edition, boolean newKnownGood, boolean newSlot);

	}

	/**
	 * Insert token that can cancel a running insert and sends events.
	 *
	 * @see ImageInsertAbortedEvent
	 * @see ImageInsertStartedEvent
	 * @see ImageInsertFailedEvent
	 * @see ImageInsertFinishedEvent
	 */
	public class InsertToken implements ClientPutCallback {

		/** The image being inserted. */
		private final Image image;

		/** The client putter. */
		private ClientPutter clientPutter;
		private Bucket bucket;

		/** The final URI. */
		private volatile FreenetURI resultingUri;

		/**
		 * Creates a new insert token for the given image.
		 *
		 * @param image
		 *            The image being inserted
		 */
		public InsertToken(Image image) {
			this.image = image;
		}

		//
		// ACCESSORS
		//

		/**
		 * Sets the client putter that is inserting the image. This will also
		 * signal all registered listeners that the image has started.
		 *
		 * @param clientPutter
		 *            The client putter
		 */
		@SuppressWarnings("synthetic-access")
		public void setClientPutter(ClientPutter clientPutter) {
			this.clientPutter = clientPutter;
			eventBus.post(new ImageInsertStartedEvent(image));
		}

		public void setBucket(Bucket bucket) {
			this.bucket = bucket;
		}

		//
		// ACTIONS
		//

		/**
		 * Cancels the running insert.
		 */
		@SuppressWarnings("synthetic-access")
		public void cancel() {
			clientPutter.cancel(node.clientCore.clientContext);
			eventBus.post(new ImageInsertAbortedEvent(image));
			bucket.free();
		}

		//
		// INTERFACE ClientPutCallback
		//

		@Override
		public RequestClient getRequestClient() {
			return imageInserts;
		}

		@Override
		public void onResume(ClientContext context) throws ResumeFailedException {
			/* ignore. */
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("synthetic-access")
		public void onFailure(InsertException insertException, BaseClientPutter clientPutter) {
			if ((insertException != null) && ("Cancelled by user".equals(insertException.getMessage()))) {
				eventBus.post(new ImageInsertAbortedEvent(image));
			} else {
				eventBus.post(new ImageInsertFailedEvent(image, insertException));
			}
			bucket.free();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onFetchable(BaseClientPutter clientPutter) {
			/* ignore, we don’t care. */
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onGeneratedMetadata(Bucket metadata, BaseClientPutter clientPutter) {
			/* ignore, we don’t care. */
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onGeneratedURI(FreenetURI generatedUri, BaseClientPutter clientPutter) {
			resultingUri = generatedUri;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("synthetic-access")
		public void onSuccess(BaseClientPutter clientPutter) {
			eventBus.post(new ImageInsertFinishedEvent(image, resultingUri));
			bucket.free();
		}

	}

	public static class InsertTokenSupplier implements Function<Image, InsertToken> {

		private final FreenetInterface freenetInterface;

		@Inject
		public InsertTokenSupplier(FreenetInterface freenetInterface) {
			this.freenetInterface = freenetInterface;
		}

		@Override
		public InsertToken apply(Image image) {
			return freenetInterface.new InsertToken(image);
		}

	}

}
