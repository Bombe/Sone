package net.pterodactylus.sone.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.database.PostBuilder;
import net.pterodactylus.sone.database.PostBuilderFactory;
import net.pterodactylus.util.config.Configuration;

/**
 * Parses a {@link Sone}’s data from a {@link Configuration}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ConfigurationSoneParser {

	private final Configuration configuration;
	private final Sone sone;
	private final String sonePrefix;

	public ConfigurationSoneParser(Configuration configuration, Sone sone) {
		this.configuration = configuration;
		this.sone = sone;
		sonePrefix = "Sone/" + sone.getId();
	}

	public Profile parseProfile() {
		Profile profile = new Profile(sone);
		profile.setFirstName(getString("/Profile/FirstName", null));
		profile.setMiddleName(getString("/Profile/MiddleName", null));
		profile.setLastName(getString("/Profile/LastName", null));
		profile.setBirthDay(getInt("/Profile/BirthDay", null));
		profile.setBirthMonth(getInt("/Profile/BirthMonth", null));
		profile.setBirthYear(getInt("/Profile/BirthYear", null));

		/* load profile fields. */
		int fieldCount = 0;
		while (true) {
			String fieldPrefix = "/Profile/Fields/" + fieldCount++;
			String fieldName = getString(fieldPrefix + "/Name", null);
			if (fieldName == null) {
				break;
			}
			String fieldValue = getString(fieldPrefix + "/Value", "");
			profile.addField(fieldName).setValue(fieldValue);
		}

		return profile;
	}

	private String getString(String nodeName, @Nullable String defaultValue) {
		return configuration.getStringValue(sonePrefix + nodeName)
				.getValue(defaultValue);
	}

	private Integer getInt(String nodeName, @Nullable Integer defaultValue) {
		return configuration.getIntValue(sonePrefix + nodeName)
				.getValue(defaultValue);
	}

	private Long getLong(String nodeName, @Nullable Long defaultValue) {
		return configuration.getLongValue(sonePrefix + nodeName)
				.getValue(defaultValue);
	}

	public Collection<Post> parsePosts(PostBuilderFactory postBuilderFactory)
	throws InvalidPostFound {
		Set<Post> posts = new HashSet<Post>();
		while (true) {
			String postPrefix = "/Posts/" + posts.size();
			String postId = getString(postPrefix + "/ID", null);
			if (postId == null) {
				break;
			}
			long postTime = getLong(postPrefix + "/Time", 0L);
			String postText = getString(postPrefix + "/Text", null);
			if (postAttributesAreInvalid(postTime, postText)) {
				throw new InvalidPostFound();
			}
			PostBuilder postBuilder = postBuilderFactory.newPostBuilder()
					.withId(postId)
					.from(sone.getId())
					.withTime(postTime)
					.withText(postText);
			String postRecipientId =
					getString(postPrefix + "/Recipient", null);
			if (postRecipientIsValid(postRecipientId)) {
				postBuilder.to(postRecipientId);
			}
			posts.add(postBuilder.build());
		}
		return posts;
	}

	private boolean postAttributesAreInvalid(long postTime, String postText) {
		return (postTime == 0) || (postText == null);
	}

	private boolean postRecipientIsValid(String postRecipientId) {
		return (postRecipientId != null) && (postRecipientId.length() == 43);
	}

	public static class InvalidPostFound extends RuntimeException { }

}
