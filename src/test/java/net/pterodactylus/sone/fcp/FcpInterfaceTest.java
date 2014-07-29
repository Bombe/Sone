package net.pterodactylus.sone.fcp;

import static net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.ALWAYS;
import static net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.NO;
import static net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.WRITING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import net.pterodactylus.sone.fcp.FcpInterface.SetActive;
import net.pterodactylus.sone.fcp.FcpInterface.SetFullAccessRequired;

import org.junit.Test;

/**
 * Unit test for {@link FcpInterface} and its subclasses.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FcpInterfaceTest {

	private final FcpInterface fcpInterface = new FcpInterface(null);
	private final SetActive setActive = fcpInterface.new SetActive();
	private final SetFullAccessRequired setFullAccessRequired = fcpInterface.new SetFullAccessRequired();

	@Test
	public void setActiveCanActivateFcpInterface() {
		setActive.optionChanged(null, null, true);
		assertThat(fcpInterface.isActive(), is(true));
	}

	@Test
	public void setActiveCanDeactivateFcpInterface() {
		setActive.optionChanged(null, null, false);
		assertThat(fcpInterface.isActive(), is(false));
	}

	@Test
	public void setFullAccessRequiredCanSetAccessToNo() {
		setFullAccessRequired.optionChanged(null, null, 0);
		assertThat(fcpInterface.getFullAccessRequired(), is(NO));
	}

	@Test
	public void setFullAccessRequiredCanSetAccessToWriting() {
		setFullAccessRequired.optionChanged(null, null, 1);
		assertThat(fcpInterface.getFullAccessRequired(), is(WRITING));
	}

	@Test
	public void setFullAccessRequiredCanSetAccessToAlways() {
		setFullAccessRequired.optionChanged(null, null, 2);
		assertThat(fcpInterface.getFullAccessRequired(), is(ALWAYS));
	}

}
