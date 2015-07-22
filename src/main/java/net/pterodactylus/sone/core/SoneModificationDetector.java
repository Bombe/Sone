package net.pterodactylus.sone.core;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static com.google.common.base.Ticker.systemTicker;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.util.concurrent.atomic.AtomicInteger;

import net.pterodactylus.sone.data.Sone;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
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
	private final LockableFingerprintProvider lockableFingerprintProvider;
	private final AtomicInteger insertionDelay;
	private Optional<Long> lastModificationTime;
	private String lastInsertFingerprint;
	private String lastCheckFingerprint;

	SoneModificationDetector(LockableFingerprintProvider lockableFingerprintProvider, AtomicInteger insertionDelay) {
		this(systemTicker(), lockableFingerprintProvider, insertionDelay);
	}

	@VisibleForTesting
	SoneModificationDetector(Ticker ticker, LockableFingerprintProvider lockableFingerprintProvider, AtomicInteger insertionDelay) {
		this.ticker = ticker;
		this.lockableFingerprintProvider = lockableFingerprintProvider;
		this.insertionDelay = insertionDelay;
		lastCheckFingerprint = lastInsertFingerprint;
	}

	public boolean isEligibleForInsert() {
		if (lockableFingerprintProvider.isLocked()) {
			lastModificationTime = absent();
			lastCheckFingerprint = "";
			return false;
		}
		String fingerprint = lockableFingerprintProvider.getFingerprint();
		if (fingerprint.equals(lastInsertFingerprint)) {
			lastModificationTime = absent();
			lastCheckFingerprint = fingerprint;
			return false;
		}
		if (!Objects.equal(lastCheckFingerprint, fingerprint)) {
			lastModificationTime = of(ticker.read());
			lastCheckFingerprint = fingerprint;
			return false;
		}
		return insertionDelayHasPassed();
	}

	public String getLastInsertFingerprint() {
		return lastInsertFingerprint;
	}

	public void setFingerprint(String fingerprint) {
		lastInsertFingerprint = fingerprint;
		lastCheckFingerprint = lastInsertFingerprint;
		lastModificationTime = absent();
	}

	private boolean insertionDelayHasPassed() {
		return NANOSECONDS.toSeconds(ticker.read() - lastModificationTime.get()) >= insertionDelay.get();
	}

	public boolean isModified() {
		return !Objects.equal(lockableFingerprintProvider.getFingerprint(), lastInsertFingerprint);
	}

	/**
	 * Provider for a fingerprint and the information if a {@link Sone} is locked. This
	 * prevents us from having to lug a Sone object around.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	static interface LockableFingerprintProvider {

		boolean isLocked();
		String getFingerprint();

	}

}
