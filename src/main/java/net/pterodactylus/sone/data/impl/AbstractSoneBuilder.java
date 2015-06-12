package net.pterodactylus.sone.data.impl;

import static com.google.common.base.Preconditions.checkState;

import net.pterodactylus.sone.database.SoneBuilder;
import net.pterodactylus.sone.freenet.wot.Identity;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;

/**
 * Abstract {@link SoneBuilder} implementation.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public abstract class AbstractSoneBuilder implements SoneBuilder {

	protected Identity identity;
	protected boolean local;

	@Override
	public SoneBuilder from(Identity identity) {
		this.identity = identity;
		return this;
	}

	@Override
	public SoneBuilder local() {
		this.local = true;
		return this;
	}

	protected void validate() throws IllegalStateException {
		checkState(identity != null, "identity must not be null");
		checkState(!local || (identity instanceof OwnIdentity),
				"can not create local Sone from remote identity");
	}

}
