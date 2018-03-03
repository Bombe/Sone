package net.pterodactylus.sone.core;

import freenet.client.FetchResult;
import freenet.keys.FreenetURI;

/**
 * Container for a fetched URI and the {@link FetchResult}.
 *
 * @author <a href="mailto:d.roden@xplosion.de">David Roden</a>
 */
public class Fetched {

	/** The fetched URI. */
	private final FreenetURI freenetUri;

	/** The fetch result. */
	private final FetchResult fetchResult;

	/**
	 * Creates a new fetched URI.
	 *
	 * @param freenetUri
	 *            The URI that was fetched
	 * @param fetchResult
	 *            The fetch result
	 */
	public Fetched(FreenetURI freenetUri, FetchResult fetchResult) {
		this.freenetUri = freenetUri;
		this.fetchResult = fetchResult;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the fetched URI.
	 *
	 * @return The fetched URI
	 */
	public FreenetURI getFreenetUri() {
		return freenetUri;
	}

	/**
	 * Returns the fetch result.
	 *
	 * @return The fetch result
	 */
	public FetchResult getFetchResult() {
		return fetchResult;
	}

}
