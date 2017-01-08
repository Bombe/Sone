package net.pterodactylus.sone.fcp;

import static net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.ALWAYS;
import static net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.NO;
import static net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired.WRITING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired;
import net.pterodactylus.sone.fcp.event.FcpInterfaceActivatedEvent;
import net.pterodactylus.sone.fcp.event.FcpInterfaceDeactivatedEvent;
import net.pterodactylus.sone.fcp.event.FullAccessRequiredChanged;

import org.junit.Test;

/**
 * Unit test for {@link FcpInterface} and its subclasses.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FcpInterfaceTest {

	private final FcpInterface fcpInterface = new FcpInterface(null);

	@Test
	public void fcpInterfaceCanBeActivated() {
		fcpInterface.fcpInterfaceActivated(new FcpInterfaceActivatedEvent());
		assertThat(fcpInterface.isActive(), is(true));
	}

	@Test
	public void fcpInterfaceCanBeDeactivated() {
		fcpInterface.fcpInterfaceDeactivated(new FcpInterfaceDeactivatedEvent());
		assertThat(fcpInterface.isActive(), is(false));
	}

	private void setAndVerifyAccessRequired(FullAccessRequired fullAccessRequired) {
		fcpInterface.fullAccessRequiredChanged(new FullAccessRequiredChanged(fullAccessRequired));
		assertThat(fcpInterface.getFullAccessRequired(), is(fullAccessRequired));
	}

	@Test
	public void setFullAccessRequiredCanSetAccessToNo() {
		setAndVerifyAccessRequired(NO);
	}

	@Test
	public void setFullAccessRequiredCanSetAccessToWriting() {
		setAndVerifyAccessRequired(WRITING);
	}

	@Test
	public void setFullAccessRequiredCanSetAccessToAlways() {
		setAndVerifyAccessRequired(ALWAYS);
	}

}
