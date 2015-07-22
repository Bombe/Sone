package net.pterodactylus.sone.core;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicInteger;

import net.pterodactylus.sone.core.SoneModificationDetector.LockableFingerprintProvider;

import com.google.common.base.Ticker;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for {@link SoneModificationDetector}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneModificationDetectorTest {

	private final Ticker ticker = mock(Ticker.class);
	private final AtomicInteger insertionDelay = new AtomicInteger(60);
	private final SoneModificationDetector soneModificationDetector;
	private final LockableFingerprintProvider lockableFingerprintProvider = mock(LockableFingerprintProvider.class);

	public SoneModificationDetectorTest() {
		when(lockableFingerprintProvider.getFingerprint()).thenReturn("original");
		when(lockableFingerprintProvider.isLocked()).thenReturn(false);
		soneModificationDetector = new SoneModificationDetector(ticker, lockableFingerprintProvider, insertionDelay);
	}

	private void modifySone() {
		modifySone("");
	}

	private void modifySone(String uniqueValue) {
		when(lockableFingerprintProvider.getFingerprint()).thenReturn("modified" + uniqueValue);
	}

	private void passTime(int seconds) {
		when(ticker.read()).thenReturn(SECONDS.toNanos(seconds));
	}

	private void lockSone() {
		when(lockableFingerprintProvider.isLocked()).thenReturn(true);
	}

	private void unlockSone() {
		when(lockableFingerprintProvider.isLocked()).thenReturn(false);
	}

	@Before
	public void setupOriginalFingerprint() {
	    soneModificationDetector.setFingerprint("original");
	}

	@Test
	public void normalConstructorCanBeCalled() {
		new SoneModificationDetector(lockableFingerprintProvider, insertionDelay);
	}

	@Test
	public void sonesStartOutAsNotEligible() {
		assertThat(soneModificationDetector.isModified(), is(false));
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
	}

	@Test
	public void originalFingerprintIsRetained() {
		assertThat(soneModificationDetector.getLastInsertFingerprint(), is("original"));
	}

	@Test
	public void modifiedSoneIsEligibleAfter60Seconds() {
		modifySone();
		assertThat(soneModificationDetector.isModified(), is(true));
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		passTime(100);
		assertThat(soneModificationDetector.isModified(), is(true));
		assertThat(soneModificationDetector.isEligibleForInsert(), is(true));
	}

	@Test
	public void modifiedAndRemodifiedSoneIsEligibleAfter90Seconds() {
		modifySone();
		assertThat(soneModificationDetector.isModified(), is(true));
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		passTime(30);
		modifySone("2");
		assertThat(soneModificationDetector.isModified(), is(true));
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		passTime(61);
		assertThat(soneModificationDetector.isModified(), is(true));
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		passTime(91);
		assertThat(soneModificationDetector.isModified(), is(true));
		assertThat(soneModificationDetector.isEligibleForInsert(), is(true));
	}

	@Test
	public void modifiedSoneIsNotEligibleAfter30Seconds() {
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
	public void lockingAndUnlockingASoneRestartsTheWaitPeriod() {
		modifySone();
		lockSone();
		passTime(30);
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		unlockSone();
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		passTime(60);
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		passTime(90);
		assertThat(soneModificationDetector.isEligibleForInsert(), is(true));
	}

	@Test
	public void settingFingerprintWillResetTheEligibility() {
		modifySone();
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		passTime(100);
		assertThat(soneModificationDetector.isEligibleForInsert(), is(true));
		soneModificationDetector.setFingerprint("modified");
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
	}

	@Test
	public void changingInsertionDelayWillInfluenceEligibility() {
		modifySone();
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		passTime(100);
		assertThat(soneModificationDetector.isEligibleForInsert(), is(true));
		insertionDelay.set(120);
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
	}

	@Test
	public void soneWithoutOriginalFingerprintIsNotEligibleAfter59Seconds() {
		SoneModificationDetector soneModificationDetector = createDetectorWithoutOriginalFingerprint();
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		passTime(59);
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
	}

	private SoneModificationDetector createDetectorWithoutOriginalFingerprint() {
		return new SoneModificationDetector(ticker, new LockableFingerprintProvider() {
			@Override
			public boolean isLocked() {
				return false;
			}

			@Override
			public String getFingerprint() {
				return "changed";
			}
		}, insertionDelay);
	}

	@Test
	public void soneWithoutOriginalFingerprintIsEligibleAfter60Seconds() {
		SoneModificationDetector soneModificationDetector = createDetectorWithoutOriginalFingerprint();
		assertThat(soneModificationDetector.isEligibleForInsert(), is(false));
		passTime(60);
		assertThat(soneModificationDetector.isEligibleForInsert(), is(true));
	}

}
