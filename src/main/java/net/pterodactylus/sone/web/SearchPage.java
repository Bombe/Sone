/*
 * Sone - OptionsPage.java - Copyright © 2010 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.sone.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Profile.Field;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.util.collection.Converter;
import net.pterodactylus.util.collection.Converters;
import net.pterodactylus.util.collection.Pagination;
import net.pterodactylus.util.filter.Filter;
import net.pterodactylus.util.filter.Filters;
import net.pterodactylus.util.number.Numbers;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.text.StringEscaper;
import net.pterodactylus.util.text.TextException;

/**
 * This page lets the user search for posts and replies that contain certain
 * words.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SearchPage extends SoneTemplatePage {

	/**
	 * Creates a new search page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public SearchPage(Template template, WebInterface webInterface) {
		super("search.html", template, "Page.Search.Title", webInterface);
	}

	//
	// SONETEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(Request request, TemplateContext templateContext) throws RedirectException {
		super.processTemplate(request, templateContext);
		String query = request.getHttpRequest().getParam("query").trim();
		if (query.length() == 0) {
			throw new RedirectException("index.html");
		}

		List<Phrase> phrases = parseSearchPhrases(query);

		Set<Sone> sones = webInterface.getCore().getSones();
		Set<Hit<Sone>> soneHits = getHits(sones, phrases, SoneStringGenerator.COMPLETE_GENERATOR);

		Set<Post> posts = new HashSet<Post>();
		for (Sone sone : sones) {
			posts.addAll(sone.getPosts());
		}
		@SuppressWarnings("synthetic-access")
		Set<Hit<Post>> postHits = getHits(posts, phrases, new PostStringGenerator());

		/* now filter. */
		soneHits = Filters.filteredSet(soneHits, Hit.POSITIVE_FILTER);
		postHits = Filters.filteredSet(postHits, Hit.POSITIVE_FILTER);

		/* now sort. */
		List<Hit<Sone>> sortedSoneHits = new ArrayList<Hit<Sone>>(soneHits);
		Collections.sort(sortedSoneHits, Hit.DESCENDING_COMPARATOR);
		List<Hit<Post>> sortedPostHits = new ArrayList<Hit<Post>>(postHits);
		Collections.sort(sortedPostHits, Hit.DESCENDING_COMPARATOR);

		/* extract Sones and posts. */
		List<Sone> resultSones = Converters.convertList(sortedSoneHits, new HitConverter<Sone>());
		List<Post> resultPosts = Converters.convertList(sortedPostHits, new HitConverter<Post>());

		/* pagination. */
		Pagination<Sone> sonePagination = new Pagination<Sone>(resultSones, 10).setPage(Numbers.safeParseInteger(request.getHttpRequest().getParam("sonePage"), 0));
		Pagination<Post> postPagination = new Pagination<Post>(resultPosts, 10).setPage(Numbers.safeParseInteger(request.getHttpRequest().getParam("postPage"), 0));

		templateContext.set("sonePagination", sonePagination);
		templateContext.set("soneHits", sonePagination.getItems());
		templateContext.set("postPagination", postPagination);
		templateContext.set("postHits", postPagination.getItems());
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Collects hit information for the given objects. The objects are converted
	 * to a {@link String} using the given {@link StringGenerator}, and the
	 * {@link #calculateScore(List, String) calculated score} is stored together
	 * with the object in a {@link Hit}, and all resulting {@link Hit}s are then
	 * returned.
	 *
	 * @param <T>
	 *            The type of the objects
	 * @param objects
	 *            The objects to search over
	 * @param phrases
	 *            The phrases to search for
	 * @param stringGenerator
	 *            The string generator for the objects
	 * @return The hits for the given phrases
	 */
	private <T> Set<Hit<T>> getHits(Collection<T> objects, List<Phrase> phrases, StringGenerator<T> stringGenerator) {
		Set<Hit<T>> hits = new HashSet<Hit<T>>();
		for (T object : objects) {
			String objectString = stringGenerator.generateString(object);
			int score = calculateScore(phrases, objectString);
			hits.add(new Hit<T>(object, score));
		}
		return hits;
	}

	/**
	 * Parses the given query into search phrases. The query is split on
	 * whitespace while allowing to group words using single or double quotes.
	 * Isolated phrases starting with a “+” are
	 * {@link Phrase.Optionality#REQUIRED}, phrases with a “-” are
	 * {@link Phrase.Optionality#FORBIDDEN}.
	 *
	 * @param query
	 *            The query to parse
	 * @return The parsed phrases
	 */
	private List<Phrase> parseSearchPhrases(String query) {
		List<String> parsedPhrases = null;
		try {
			parsedPhrases = StringEscaper.parseLine(query);
		} catch (TextException te1) {
			/* invalid query. */
			return Collections.emptyList();
		}

		List<Phrase> phrases = new ArrayList<Phrase>();
		for (String phrase : parsedPhrases) {
			if (phrase.startsWith("+")) {
				phrases.add(new Phrase(phrase.substring(1), Phrase.Optionality.REQUIRED));
			} else if (phrase.startsWith("-")) {
				phrases.add(new Phrase(phrase.substring(1), Phrase.Optionality.FORBIDDEN));
			}
			phrases.add(new Phrase(phrase, Phrase.Optionality.OPTIONAL));
		}
		return phrases;
	}

	/**
	 * Calculates the score for the given expression when using the given
	 * phrases.
	 *
	 * @param phrases
	 *            The phrases to search for
	 * @param expression
	 *            The expression to search
	 * @return The score of the expression
	 */
	private int calculateScore(List<Phrase> phrases, String expression) {
		int optionalHits = 0;
		int requiredHits = 0;
		int forbiddenHits = 0;
		int requiredPhrases = 0;
		for (Phrase phrase : phrases) {
			if (phrase.getOptionality() == Phrase.Optionality.REQUIRED) {
				++requiredPhrases;
			}
			boolean matches = expression.toLowerCase().contains(phrase.getPhrase().toLowerCase());
			if (!matches) {
				continue;
			}
			if (phrase.getOptionality() == Phrase.Optionality.REQUIRED) {
				++requiredHits;
			}
			if (phrase.getOptionality() == Phrase.Optionality.OPTIONAL) {
				++optionalHits;
			}
			if (phrase.getOptionality() == Phrase.Optionality.FORBIDDEN) {
				++forbiddenHits;
			}
		}
		return requiredHits * 3 + optionalHits + (requiredHits - requiredPhrases) * 5 - (forbiddenHits * 2);
	}

	/**
	 * Converts a given object into a {@link String}.
	 *
	 * @param <T>
	 *            The type of the objects
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	private static interface StringGenerator<T> {

		/**
		 * Generates a {@link String} for the given object.
		 *
		 * @param object
		 *            The object to generate the {@link String} for
		 * @return The generated {@link String}
		 */
		public String generateString(T object);

	}

	/**
	 * Generates a {@link String} from a {@link Sone}, concatenating the name of
	 * the Sone and all {@link Profile} {@link Field} values.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	private static class SoneStringGenerator implements StringGenerator<Sone> {

		/** A static instance of a complete Sone string generator. */
		public static final SoneStringGenerator COMPLETE_GENERATOR = new SoneStringGenerator(true);

		/**
		 * A static instance of a Sone string generator that will only use the
		 * name of the Sone.
		 */
		public static final SoneStringGenerator NAME_GENERATOR = new SoneStringGenerator(false);

		/** Whether to generate a string from all data of a Sone. */
		private final boolean complete;

		/**
		 * Creates a new Sone string generator.
		 *
		 * @param complete
		 *            {@code true} to use the profile’s fields, {@code false} to
		 *            not to use the profile‘s fields
		 */
		private SoneStringGenerator(boolean complete) {
			this.complete = complete;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String generateString(Sone sone) {
			StringBuilder soneString = new StringBuilder();
			soneString.append(sone.getName());
			Profile soneProfile = sone.getProfile();
			if (soneProfile.getFirstName() != null) {
				soneString.append(' ').append(soneProfile.getFirstName());
			}
			if (soneProfile.getMiddleName() != null) {
				soneString.append(' ').append(soneProfile.getMiddleName());
			}
			if (soneProfile.getLastName() != null) {
				soneString.append(' ').append(soneProfile.getLastName());
			}
			if (complete) {
				for (Field field : soneProfile.getFields()) {
					soneString.append(' ').append(field.getValue());
				}
			}
			return soneString.toString();
		}

	}

	/**
	 * Generates a {@link String} from a {@link Post}, concatenating the text of
	 * the post, the text of all {@link Reply}s, and the name of all
	 * {@link Sone}s that have replied.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	private class PostStringGenerator implements StringGenerator<Post> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String generateString(Post post) {
			StringBuilder postString = new StringBuilder();
			postString.append(post.getText());
			if (post.getRecipient() != null) {
				postString.append(' ').append(SoneStringGenerator.NAME_GENERATOR.generateString(post.getRecipient()));
			}
			for (Reply reply : webInterface.getCore().getReplies(post)) {
				postString.append(' ').append(SoneStringGenerator.NAME_GENERATOR.generateString(reply.getSone()));
				postString.append(' ').append(reply.getText());
			}
			return postString.toString();
		}

	}

	/**
	 * A search phrase.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	private static class Phrase {

		/**
		 * The optionality of a search phrase.
		 *
		 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’
		 *         Roden</a>
		 */
		public enum Optionality {

			/** The phrase is optional. */
			OPTIONAL,

			/** The phrase is required. */
			REQUIRED,

			/** The phrase is forbidden. */
			FORBIDDEN

		}

		/** The phrase to search for. */
		private final String phrase;

		/** The optionality of the phrase. */
		private final Optionality optionality;

		/**
		 * Creates a new phrase.
		 *
		 * @param phrase
		 *            The phrase to search for
		 * @param optionality
		 *            The optionality of the phrase
		 */
		public Phrase(String phrase, Optionality optionality) {
			this.optionality = optionality;
			this.phrase = phrase;
		}

		/**
		 * Returns the phrase to search for.
		 *
		 * @return The phrase to search for
		 */
		public String getPhrase() {
			return phrase;
		}

		/**
		 * Returns the optionality of the phrase.
		 *
		 * @return The optionality of the phrase
		 */
		public Optionality getOptionality() {
			return optionality;
		}

	}

	/**
	 * A hit consists of a searched object and the score it got for the phrases
	 * of the search.
	 *
	 * @see SearchPage#calculateScore(List, String)
	 * @param <T>
	 *            The type of the searched object
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	private static class Hit<T> {

		/** Filter for {@link Hit}s with a score of more than 0. */
		public static final Filter<Hit<?>> POSITIVE_FILTER = new Filter<Hit<?>>() {

			@Override
			public boolean filterObject(Hit<?> hit) {
				return hit.getScore() > 0;
			}

		};

		/** Comparator that sorts {@link Hit}s descending by score. */
		public static final Comparator<Hit<?>> DESCENDING_COMPARATOR = new Comparator<Hit<?>>() {

			@Override
			public int compare(Hit<?> leftHit, Hit<?> rightHit) {
				return rightHit.getScore() - leftHit.getScore();
			}

		};

		/** The object that was searched. */
		private final T object;

		/** The score of the object. */
		private final int score;

		/**
		 * Creates a new hit.
		 *
		 * @param object
		 *            The object that was searched
		 * @param score
		 *            The score of the object
		 */
		public Hit(T object, int score) {
			this.object = object;
			this.score = score;
		}

		/**
		 * Returns the object that was searched.
		 *
		 * @return The object that was searched
		 */
		public T getObject() {
			return object;
		}

		/**
		 * Returns the score of the object.
		 *
		 * @return The score of the object
		 */
		public int getScore() {
			return score;
		}

	}

	/**
	 * Extracts the object from a {@link Hit}.
	 *
	 * @param <T>
	 *            The type of the object to extract
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public static class HitConverter<T> implements Converter<Hit<T>, T> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T convert(Hit<T> input) {
			return input.getObject();
		}

	}

}
