package net.pterodactylus.sone.core;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.base.Ticker.systemTicker;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.util.concurrent.atomic.AtomicInteger;

import net.pterodactylus.sone.data.Sone;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Ticker;

/**
 * Class that detects {@link Sone} modifications (as per their {@link
 * Sone#getFingerprint() fingerprints} and determines when a modified Sone may
 * be inserted.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
class SoneModificationDetector {

	private final Ticker ticker;
	private final Core core;
	private final Sone sone;
	private final AtomicInteger insertionDelay;
	private Optional<Long> lastModificationTime;
	private String originalFingerprint;
	private String lastFingerprint;

	SoneModificationDetector(Core core, Sone sone, AtomicInteger insertionDelay) {
		this(systemTicker(), core, sone, insertionDelay);
	}

	@VisibleForTesting
	SoneModificationDetector(Ticker ticker, Core core, Sone sone, AtomicInteger insertionDelay) {
		this.ticker = ticker;
		this.core = core;
		this.sone = sone;
		this.insertionDelay = insertionDelay;
		originalFingerprint = sone.getFingerprint();
		lastFingerprint = originalFingerprint;
	}

	public boolean isEligibleForInsert() {
		if (core.isLocked(sone)) {
			lastModificationTime = absent();
			lastFingerprint = "";
			return false;
		}
		String fingerprint = sone.getFingerprint();
		if (originalFingerprint.equals(fingerprint)) {
			lastModificationTime = absent();
			lastFingerprint = fingerprint;
			return false;
		}
		if (!lastFingerprint.equals(fingerprint)) {
			lastModificationTime = of(ticker.read());
			lastFingerprint = fingerprint;
			return false;
		}
		return insertionDelayHasPassed();
	}

	public void setFingerprint(String fingerprint) {
		originalFingerprint = fingerprint;
		lastFingerprint = originalFingerprint;
		lastModificationTime = absent();
	}

	private boolean insertionDelayHasPassed() {
		return lastModificationTime.isPresent() && (NANOSECONDS.toSeconds(ticker.read() - lastModificationTime.get()) >= insertionDelay.get());
	}

}
