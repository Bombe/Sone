package net.pterodactylus.sone.core.event;

import com.google.common.eventbus.EventBus;

/**
 * Notifies interested {@link EventBus} clients that the Sone insertion delay
 * has changed.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class InsertionDelayChangedEvent {

	private final int insertionDelay;

	public InsertionDelayChangedEvent(int insertionDelay) {
		this.insertionDelay = insertionDelay;
	}

	public int getInsertionDelay() {
		return insertionDelay;
	}

}
