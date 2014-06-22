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

	private final Ticker ticker = mock(Ticker.class);
	private final Sone sone = mock(Sone.class);
	private final Core core = mock(Core.class);
	private final AtomicInteger insertionDelay = new AtomicInteger(60);
	private final SoneModificationDetector soneModificationDetector;

	public SoneModificationDetectorTest() {
		when(sone.getFingerprint()).thenReturn("original");
		soneModificationDetector = new SoneModificationDetector(ticker, core, sone, insertionDelay);
	}

	private void modifySone() {
		when(sone.getFingerprint()).thenReturn("modified");
	}

	private void passTime(int seconds) {
		when(ticker.read()).thenReturn(SECONDS.toNanos(seconds));
	}

	private void lockSone() {
		when(core.isLocked(sone)).thenReturn(true);
	}

	@Test
	public void modifiedSoneIsEligibleAfter60Seconds() {
		assertThat(soneModificationDetector.isModified(), is(false));
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		modifySone();
		assertThat(soneModificationDetector.isModified(), is(true));
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		passTime(100);
		assertThat(soneModificationDetector.isModified(), is(true));
		assertThat(soneModificationDetector.isEligibleForInsert(), is(true));
	}

	@Test
	public void modifiedSoneIsNotEligibleAfter30Seconds() {
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		modifySone();
		passTime(30);
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
	}

	@Test
	public void lockedAndModifiedSoneIsNotEligibleAfter60Seconds() {
		lockSone();
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		modifySone();
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		passTime(100);
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
	}

	@Test
	public void settingFingerprintWillResetTheEligibility() {
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		modifySone();
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		passTime(100);
		assertThat(soneModificationDetector.isEligibleForInsert(), is(true));
		soneModificationDetector.setFingerprint("modified");
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
	}

	@Test
	public void changingInsertionDelayWillInfluenceEligibility() {
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		modifySone();
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		passTime(100);
		assertThat(soneModificationDetector.isEligibleForInsert(), is(true));
		insertionDelay.set(120);
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
	}

}
