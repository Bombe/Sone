/*
 * Sone - SearchPage.java - Copyright © 2010–2013 David Roden
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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Profile.Field;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.collection.Pagination;
import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.number.Numbers;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.text.StringEscaper;
import net.pterodactylus.util.text.TextException;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;

/**
 * This page lets the user search for posts and replies that contain certain
 * words.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SearchPage extends SoneTemplatePage {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(SearchPage.class);

	/** Short-term cache. */
	private final LoadingCache<List<Phrase>, Set<Hit<Post>>> hitCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build(new CacheLoader<List<Phrase>, Set<Hit<Post>>>() {

		@Override
		@SuppressWarnings("synthetic-access")
		public Set<Hit<Post>> load(List<Phrase> phrases) {
			Set<Post> posts = new HashSet<Post>();
			for (Sone sone : webInterface.getCore().getSones()) {
				posts.addAll(sone.getPosts());
			}
			return getHits(Collections2.filter(posts, Post.FUTURE_POSTS_FILTER), phrases, new PostStringGenerator());
		}
	});

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
	@SuppressWarnings("synthetic-access")
	protected void processTemplate(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		super.processTemplate(request, templateContext);
		String query = request.getHttpRequest().getParam("query").trim();
		if (query.length() == 0) {
			throw new RedirectException("index.html");
		}

		List<Phrase> phrases = parseSearchPhrases(query);
		if (phrases.isEmpty()) {
			throw new RedirectException("index.html");
		}

		/* check for a couple of shortcuts. */
		if (phrases.size() == 1) {
			String phrase = phrases.get(0).getPhrase();

			/* is it a Sone ID? */
			redirectIfNotNull(getSoneId(phrase), "viewSone.html?sone=");

			/* is it a post ID? */
			redirectIfNotNull(getPostId(phrase), "viewPost.html?post=");

			/* is it a reply ID? show the post. */
			redirectIfNotNull(getReplyPostId(phrase), "viewPost.html?post=");

			/* is it an album ID? */
			redirectIfNotNull(getAlbumId(phrase), "imageBrowser.html?album=");

			/* is it an image ID? */
			redirectIfNotNull(getImageId(phrase), "imageBrowser.html?image=");
		}

		Collection<Sone> sones = webInterface.getCore().getSones();
		Collection<Hit<Sone>> soneHits = getHits(sones, phrases, SoneStringGenerator.COMPLETE_GENERATOR);

		Collection<Hit<Post>> postHits = hitCache.getUnchecked(phrases);

		/* now filter. */
		soneHits = Collections2.filter(soneHits, Hit.POSITIVE_FILTER);
		postHits = Collections2.filter(postHits, Hit.POSITIVE_FILTER);

		/* now sort. */
		List<Hit<Sone>> sortedSoneHits = Ordering.from(Hit.DESCENDING_COMPARATOR).sortedCopy(soneHits);
		List<Hit<Post>> sortedPostHits = Ordering.from(Hit.DESCENDING_COMPARATOR).sortedCopy(postHits);

		/* extract Sones and posts. */
		List<Sone> resultSones = FluentIterable.from(sortedSoneHits).transform(new HitMapper<Sone>()).toList();
		List<Post> resultPosts = FluentIterable.from(sortedPostHits).transform(new HitMapper<Post>()).toList();

		/* pagination. */
		Pagination<Sone> sonePagination = new Pagination<Sone>(resultSones, webInterface.getCore().getPreferences().getPostsPerPage()).setPage(Numbers.safeParseInteger(request.getHttpRequest().getParam("sonePage"), 0));
		Pagination<Post> postPagination = new Pagination<Post>(resultPosts, webInterface.getCore().getPreferences().getPostsPerPage()).setPage(Numbers.safeParseInteger(request.getHttpRequest().getParam("postPage"), 0));

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
	private static <T> Set<Hit<T>> getHits(Collection<T> objects, List<Phrase> phrases, StringGenerator<T> stringGenerator) {
		Set<Hit<T>> hits = new HashSet<Hit<T>>();
		for (T object : objects) {
			String objectString = stringGenerator.generateString(object);
			double score = calculateScore(phrases, objectString);
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
	private static List<Phrase> parseSearchPhrases(String query) {
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
				if (phrase.length() > 1) {
					phrases.add(new Phrase(phrase.substring(1), Phrase.Optionality.REQUIRED));
				} else {
					phrases.add(new Phrase("+", Phrase.Optionality.OPTIONAL));
				}
			} else if (phrase.startsWith("-")) {
				if (phrase.length() > 1) {
					phrases.add(new Phrase(phrase.substring(1), Phrase.Optionality.FORBIDDEN));
				} else {
					phrases.add(new Phrase("-", Phrase.Optionality.OPTIONAL));
				}
			} else {
				phrases.add(new Phrase(phrase, Phrase.Optionality.OPTIONAL));
			}
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
	private static double calculateScore(List<Phrase> phrases, String expression) {
		logger.log(Level.FINEST, String.format("Calculating Score for “%s”…", expression));
		double optionalHits = 0;
		double requiredHits = 0;
		int forbiddenHits = 0;
		int requiredPhrases = 0;
		for (Phrase phrase : phrases) {
			String phraseString = phrase.getPhrase().toLowerCase();
			if (phrase.getOptionality() == Phrase.Optionality.REQUIRED) {
				++requiredPhrases;
			}
			int matches = 0;
			int index = 0;
			double score = 0;
			while (index < expression.length()) {
				int position = expression.toLowerCase().indexOf(phraseString, index);
				if (position == -1) {
					break;
				}
				score += Math.pow(1 - position / (double) expression.length(), 2);
				index = position + phraseString.length();
				logger.log(Level.FINEST, String.format("Got hit at position %d.", position));
				++matches;
			}
			logger.log(Level.FINEST, String.format("Score: %f", score));
			if (matches == 0) {
				continue;
			}
			if (phrase.getOptionality() == Phrase.Optionality.REQUIRED) {
				requiredHits += score;
			}
			if (phrase.getOptionality() == Phrase.Optionality.OPTIONAL) {
				optionalHits += score;
			}
			if (phrase.getOptionality() == Phrase.Optionality.FORBIDDEN) {
				forbiddenHits += matches;
			}
		}
		return requiredHits * 3 + optionalHits + (requiredHits - requiredPhrases) * 5 - (forbiddenHits * 2);
	}

	/**
	 * Throws a
	 * {@link net.pterodactylus.sone.web.page.FreenetTemplatePage.RedirectException}
	 * if the given object is not {@code null}, appending the object to the
	 * given target URL.
	 *
	 * @param object
	 *            The object on which to redirect
	 * @param target
	 *            The target of the redirect
	 * @throws RedirectException
	 *             if {@code object} is not {@code null}
	 */
	private static void redirectIfNotNull(String object, String target) throws RedirectException {
		if (object != null) {
			throw new RedirectException(target + object);
		}
	}

	/**
	 * If the given phrase contains a Sone ID (optionally prefixed by
	 * “sone://”), returns said Sone ID, otherwise return {@code null}.
	 *
	 * @param phrase
	 *            The phrase that maybe is a Sone ID
	 * @return The Sone ID, or {@code null}
	 */
	private String getSoneId(String phrase) {
		String soneId = phrase.startsWith("sone://") ? phrase.substring(7) : phrase;
		return (webInterface.getCore().getSone(soneId).isPresent()) ? soneId : null;
	}

	/**
	 * If the given phrase contains a post ID (optionally prefixed by
	 * “post://”), returns said post ID, otherwise return {@code null}.
	 *
	 * @param phrase
	 *            The phrase that maybe is a post ID
	 * @return The post ID, or {@code null}
	 */
	private String getPostId(String phrase) {
		String postId = phrase.startsWith("post://") ? phrase.substring(7) : phrase;
		return (webInterface.getCore().getPost(postId).isPresent()) ? postId : null;
	}

	/**
	 * If the given phrase contains a reply ID (optionally prefixed by
	 * “reply://”), returns the ID of the post the reply belongs to, otherwise
	 * return {@code null}.
	 *
	 * @param phrase
	 *            The phrase that maybe is a reply ID
	 * @return The reply’s post ID, or {@code null}
	 */
	private String getReplyPostId(String phrase) {
		String replyId = phrase.startsWith("reply://") ? phrase.substring(8) : phrase;
		Optional<PostReply> postReply = webInterface.getCore().getPostReply(replyId);
		if (!postReply.isPresent()) {
			return null;
		}
		return postReply.get().getPostId();
	}

	/**
	 * If the given phrase contains an album ID (optionally prefixed by
	 * “album://”), returns said album ID, otherwise return {@code null}.
	 *
	 * @param phrase
	 *            The phrase that maybe is an album ID
	 * @return The album ID, or {@code null}
	 */
	private String getAlbumId(String phrase) {
		String albumId = phrase.startsWith("album://") ? phrase.substring(8) : phrase;
		return (webInterface.getCore().getAlbum(albumId, false) != null) ? albumId : null;
	}

	/**
	 * If the given phrase contains an image ID (optionally prefixed by
	 * “image://”), returns said image ID, otherwise return {@code null}.
	 *
	 * @param phrase
	 *            The phrase that maybe is an image ID
	 * @return The image ID, or {@code null}
	 */
	private String getImageId(String phrase) {
		String imageId = phrase.startsWith("image://") ? phrase.substring(8) : phrase;
		return (webInterface.getCore().getImage(imageId, false) != null) ? imageId : null;
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
			if (post.getRecipient().isPresent()) {
				postString.append(' ').append(SoneStringGenerator.NAME_GENERATOR.generateString(post.getRecipient().get()));
			}
			for (PostReply reply : Collections2.filter(webInterface.getCore().getReplies(post.getId()), Reply.FUTURE_REPLY_FILTER)) {
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

		//
		// OBJECT METHODS
		//

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			return phrase.hashCode() ^ ((optionality == Optionality.FORBIDDEN) ? (0xaaaaaaaa) : ((optionality == Optionality.REQUIRED) ? 0x55555555 : 0));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object object) {
			if (!(object instanceof Phrase)) {
				return false;
			}
			Phrase phrase = (Phrase) object;
			return (this.optionality == phrase.optionality) && this.phrase.equals(phrase.phrase);
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
		public static final Predicate<Hit<?>> POSITIVE_FILTER = new Predicate<Hit<?>>() {

			@Override
			public boolean apply(Hit<?> hit) {
				return hit.getScore() > 0;
			}

		};

		/** Comparator that sorts {@link Hit}s descending by score. */
		public static final Comparator<Hit<?>> DESCENDING_COMPARATOR = new Comparator<Hit<?>>() {

			@Override
			public int compare(Hit<?> leftHit, Hit<?> rightHit) {
				return (rightHit.getScore() < leftHit.getScore()) ? -1 : ((rightHit.getScore() > leftHit.getScore()) ? 1 : 0);
			}

		};

		/** The object that was searched. */
		private final T object;

		/** The score of the object. */
		private final double score;

		/**
		 * Creates a new hit.
		 *
		 * @param object
		 *            The object that was searched
		 * @param score
		 *            The score of the object
		 */
		public Hit(T object, double score) {
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
		public double getScore() {
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
	private static class HitMapper<T> implements Function<Hit<T>, T> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T apply(Hit<T> input) {
			return input.getObject();
		}

	}

}
