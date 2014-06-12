package net.pterodactylus.sone.core;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicInteger;

import net.pterodactylus.sone.data.Sone;

import com.google.common.base.Ticker;
import org.junit.Test;

/**
 * Unit test for {@link SoneModificationDetector}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneModificationDetectorTest {

	@Test
	public void modifiedSoneIsEligibleAfter60Seconds() {
		Ticker ticker = mock(Ticker.class);
		Sone sone = mock(Sone.class);
		when(sone.getFingerprint()).thenReturn("original");
		Core core = mock(Core.class);
		SoneModificationDetector soneModificationDetector = new SoneModificationDetector(ticker, core, sone, new AtomicInteger(60));
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		when(sone.getFingerprint()).thenReturn("modified");
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		when(ticker.read()).thenReturn(SECONDS.toNanos(100));
		assertThat(soneModificationDetector.isEligibleForInsert(), is(true));
	}

	@Test
	public void modifiedSoneIsNotEligibleAfter30Seconds() {
		Ticker ticker = mock(Ticker.class);
		Sone sone = mock(Sone.class);
		when(sone.getFingerprint()).thenReturn("original");
		Core core = mock(Core.class);
		SoneModificationDetector soneModificationDetector = new SoneModificationDetector(ticker, core, sone, new AtomicInteger(60));
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		when(sone.getFingerprint()).thenReturn("modified");
		when(ticker.read()).thenReturn(SECONDS.toNanos(30));
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
	}

	@Test
	public void lockedAndModifiedSoneIsNotEligibleAfter60Seconds() {
		Ticker ticker = mock(Ticker.class);
		Sone sone = mock(Sone.class);
		when(sone.getFingerprint()).thenReturn("original");
		Core core = mock(Core.class);
		when(core.isLocked(sone)).thenReturn(true);
		SoneModificationDetector soneModificationDetector = new SoneModificationDetector(ticker, core, sone, new AtomicInteger(60));
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		when(sone.getFingerprint()).thenReturn("modified");
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		when(ticker.read()).thenReturn(SECONDS.toNanos(100));
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
	}

	@Test
	public void settingFingerprintWillResetTheEligibility() {
		Ticker ticker = mock(Ticker.class);
		Sone sone = mock(Sone.class);
		when(sone.getFingerprint()).thenReturn("original");
		Core core = mock(Core.class);
		SoneModificationDetector soneModificationDetector = new SoneModificationDetector(ticker, core, sone, new AtomicInteger(60));
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		when(sone.getFingerprint()).thenReturn("modified");
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		when(ticker.read()).thenReturn(SECONDS.toNanos(100));
		assertThat(soneModificationDetector.isEligibleForInsert(), is(true));
		soneModificationDetector.setFingerprint("modified");
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
	}

	@Test
	public void changingInsertionDelayWillInfluenceEligibility() {
		Ticker ticker = mock(Ticker.class);
		Sone sone = mock(Sone.class);
		when(sone.getFingerprint()).thenReturn("original");
		Core core = mock(Core.class);
		AtomicInteger insertionDelay = new AtomicInteger(60);
		SoneModificationDetector soneModificationDetector = new SoneModificationDetector(ticker, core, sone, insertionDelay);
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		when(sone.getFingerprint()).thenReturn("modified");
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		when(ticker.read()).thenReturn(SECONDS.toNanos(100));
		assertThat(soneModificationDetector.isEligibleForInsert(), is(true));
		insertionDelay.set(120);
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
	}

}
