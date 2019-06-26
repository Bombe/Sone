package net.pterodactylus.sone.freenet;

import static freenet.support.Base64.encode;
import static java.lang.String.format;

import freenet.keys.FreenetURI;

import com.google.common.annotations.VisibleForTesting;

/**
 * Encapsulates the parts of a {@link FreenetURI} that do not change while
 * being converted from SSK to USK and/or back.
 */
public class Key {

	private final byte[] routingKey;
	private final byte[] cryptoKey;
	private final byte[] extra;

	private Key(byte[] routingKey, byte[] cryptoKey, byte[] extra) {
		this.routingKey = routingKey;
		this.cryptoKey = cryptoKey;
		this.extra = extra;
	}

	@VisibleForTesting
	public String getRoutingKey() {
		return encode(routingKey);
	}

	@VisibleForTesting
	public String getCryptoKey() {
		return encode(cryptoKey);
	}

	@VisibleForTesting
	public String getExtra() {
		return encode(extra);
	}

	public FreenetURI toUsk(String docName, long edition, String... paths) {
		return new FreenetURI("USK", docName, paths, routingKey, cryptoKey,
				extra, edition);
	}

	public FreenetURI toSsk(String docName, String... paths) {
		return new FreenetURI("SSK", docName, paths, routingKey, cryptoKey,
				extra);
	}

	public FreenetURI toSsk(String docName, long edition, String... paths) {
		return new FreenetURI("SSK", format("%s-%d", docName, edition), paths,
				routingKey, cryptoKey, extra, edition);
	}

	public static Key from(FreenetURI freenetURI) {
		return new Key(freenetURI.getRoutingKey(), freenetURI.getCryptoKey(),
				freenetURI.getExtra());
	}

	public static String routingKey(FreenetURI freenetURI) {
		return from(freenetURI).getRoutingKey();
	}

}
