package net.pterodactylus.sone.core;

import net.pterodactylus.sone.fcp.FcpInterface.FullAccessRequired;
import net.pterodactylus.util.config.Configuration;
import net.pterodactylus.util.config.ConfigurationException;

/**
 * Loads preferences stored in a {@link Configuration} into a {@link
 * Preferences} object.
 */
public class PreferencesLoader {

	private final Preferences preferences;

	public PreferencesLoader(Preferences preferences) {
		this.preferences = preferences;
	}

	public void loadFrom(Configuration configuration) {
		loadInsertionDelay(configuration);
		loadPostsPerPage(configuration);
		loadImagesPerPage(configuration);
		loadCharactersPerPost(configuration);
		loadPostCutOffLength(configuration);
		loadRequireFullAccess(configuration);
		loadPositiveTrust(configuration);
		loadNegativeTrust(configuration);
		loadTrustComment(configuration);
		loadFcpInterfaceActive(configuration);
		loadFcpFullAccessRequired(configuration);
	}

	private void loadInsertionDelay(Configuration configuration) {
		preferences.setInsertionDelay(configuration.getIntValue(
				"Option/InsertionDelay").getValue(null));
	}

	private void loadPostsPerPage(Configuration configuration) {
		preferences.setPostsPerPage(
				configuration.getIntValue("Option/PostsPerPage")
						.getValue(null));
	}

	private void loadImagesPerPage(Configuration configuration) {
		preferences.setImagesPerPage(
				configuration.getIntValue("Option/ImagesPerPage")
						.getValue(null));
	}

	private void loadCharactersPerPost(Configuration configuration) {
		preferences.setCharactersPerPost(
				configuration.getIntValue("Option/CharactersPerPost")
						.getValue(null));
	}

	private void loadPostCutOffLength(Configuration configuration) {
		try {
			preferences.setPostCutOffLength(
					configuration.getIntValue("Option/PostCutOffLength")
							.getValue(null));
		} catch (IllegalArgumentException iae1) {
			/* previous versions allowed -1, ignore and use default. */
		}
	}

	private void loadRequireFullAccess(Configuration configuration) {
		preferences.setRequireFullAccess(
				configuration.getBooleanValue("Option/RequireFullAccess")
						.getValue(null));
	}

	private void loadPositiveTrust(Configuration configuration) {
		preferences.setPositiveTrust(
				configuration.getIntValue("Option/PositiveTrust")
						.getValue(null));
	}

	private void loadNegativeTrust(Configuration configuration) {
		preferences.setNegativeTrust(
				configuration.getIntValue("Option/NegativeTrust")
						.getValue(null));
	}

	private void loadTrustComment(Configuration configuration) {
		preferences.setTrustComment(
				configuration.getStringValue("Option/TrustComment")
						.getValue(null));
	}

	private void loadFcpInterfaceActive(Configuration configuration) {
		preferences.setFcpInterfaceActive(configuration.getBooleanValue(
				"Option/ActivateFcpInterface").getValue(null));
	}

	private void loadFcpFullAccessRequired(Configuration configuration) {
		Integer fullAccessRequiredInteger = configuration
				.getIntValue("Option/FcpFullAccessRequired").getValue(null);
		preferences.setFcpFullAccessRequired(
				(fullAccessRequiredInteger == null) ? null :
						FullAccessRequired.values()[fullAccessRequiredInteger]);
	}

}
