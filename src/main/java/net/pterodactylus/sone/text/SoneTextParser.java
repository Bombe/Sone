/*
 * Sone - SoneTextParser.java - Copyright © 2010–2016 David Roden
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

package net.pterodactylus.sone.text;

import static com.google.common.base.Optional.absent;
import static com.google.common.base.Optional.of;
import static java.util.logging.Logger.getLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.impl.IdOnlySone;
import net.pterodactylus.sone.database.PostProvider;
import net.pterodactylus.sone.database.SoneProvider;

import com.google.common.base.Optional;
import org.bitpedia.util.Base32;

import freenet.keys.FreenetURI;
import freenet.support.Base64;

/**
 * {@link Parser} implementation that can recognize Freenet URIs.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneTextParser implements Parser<SoneTextParserContext> {

	/** The logger. */
	private static final Logger logger = getLogger(SoneTextParser.class.getName());

	/** Pattern to detect whitespace. */
	private static final Pattern whitespacePattern = Pattern.compile("[\\u000a\u0020\u00a0\u1680\u180e\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200a\u200b\u200c\u200d\u202f\u205f\u2060\u2800\u3000]");

	private static class NextLink {

		private final int position;
		private final String link;
		private final String remainder;
		private final LinkType linkType;

		private NextLink(int position, String link, String remainder, LinkType linkType) {
			this.position = position;
			this.link = link;
			this.remainder = remainder;
			this.linkType = linkType;
		}

		public int getPosition() {
			return position;
		}

		public String getLink() {
			return link;
		}

		public String getRemainder() {
			return remainder;
		}

		public LinkType getLinkType() {
			return linkType;
		}

	}

	/**
	 * Enumeration for all recognized link types.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	private enum LinkType {

		KSK("KSK@", true),
		CHK("CHK@", true),
		SSK("SSK@", true),
		USK("USK@", true),
		HTTP("http://", false),
		HTTPS("https://", false),
		SONE("sone://", false),
		POST("post://", false),

		FREEMAIL("", true) {
			@Override
			public Optional<NextLink> findNext(String line) {
				int nextFreemailSuffix = line.indexOf(".freemail");
				if (nextFreemailSuffix < 54) {
					/* 52 chars for the id, 1 on @, at least 1 for the local part. */
					return absent();
				}
				if (line.charAt(nextFreemailSuffix - 53) != '@') {
					return absent();
				}
				if (!line.substring(nextFreemailSuffix - 52, nextFreemailSuffix).matches("^[a-z2-7]*$")) {
					return absent();
				}
				int startOfLocalPart = nextFreemailSuffix - 54;
				if (!isAllowedInLocalPart(line.charAt(startOfLocalPart))) {
					return absent();
				}
				while ((startOfLocalPart > 0) && isAllowedInLocalPart(line.charAt(startOfLocalPart - 1))) {
					startOfLocalPart--;
				}
				return of(new NextLink(startOfLocalPart, line.substring(startOfLocalPart, nextFreemailSuffix + 9), line.substring(nextFreemailSuffix + 9), this));
			}

			private boolean isAllowedInLocalPart(char character) {
				return ((character >= 'A') && (character <= 'Z'))
						|| ((character >= 'a') && (character <= 'z'))
						|| ((character >= '0') && (character <= '9'))
						|| (character == '.') || (character == '-') || (character == '_');
			}
		};

		private final String scheme;
		private final boolean freenetLink;

		LinkType(String scheme, boolean freenetLink) {
			this.scheme = scheme;
			this.freenetLink = freenetLink;
		}

		/**
		 * Returns the scheme of this link type.
		 *
		 * @return The scheme of this link type
		 */
		public String getScheme() {
			return scheme;
		}

		public boolean isFreenetLink() {
			return freenetLink;
		}

		public Optional<NextLink> findNext(String line) {
			int nextLinkPosition = line.indexOf(getScheme());
			if (nextLinkPosition == -1) {
				return absent();
			}
			int endOfLink = findEndOfLink(line.substring(nextLinkPosition));
			return of(new NextLink(nextLinkPosition, line.substring(nextLinkPosition, nextLinkPosition + endOfLink), line.substring(nextLinkPosition + endOfLink), this));
		}

		private static int findEndOfLink(String line) {
			Matcher matcher = whitespacePattern.matcher(line);
			int endOfLink = matcher.find() ? matcher.start() : line.length();
			while (isPunctuation(line.charAt(endOfLink - 1))) {
				endOfLink--;
			}
			int openParens = 0;
			for (int i = 0; i < endOfLink; i++) {
				switch (line.charAt(i)) {
					case '(':
						openParens++;
						break;
					case ')':
						openParens--;
						if (openParens < 0) {
							return i;
						}
					default:
				}
			}
			return endOfLink;
		}

	}

	/** The Sone provider. */
	private final SoneProvider soneProvider;

	/** The post provider. */
	private final PostProvider postProvider;

	/**
	 * Creates a new freenet link parser.
	 *
	 * @param soneProvider
	 *            The Sone provider
	 * @param postProvider
	 *            The post provider
	 */
	public SoneTextParser(SoneProvider soneProvider, PostProvider postProvider) {
		this.soneProvider = soneProvider;
		this.postProvider = postProvider;
	}

	//
	// PART METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Nonnull
	@Override
	public Iterable<Part> parse(@Nonnull String source, @Nullable SoneTextParserContext context) {
		PartContainer parts = new PartContainer();
		try (Reader sourceReader = new StringReader(source);
				BufferedReader bufferedReader = new BufferedReader(sourceReader)) {
			String line;
			boolean lastLineEmpty = true;
			int emptyLines = 0;
			while ((line = bufferedReader.readLine()) != null) {
				if (line.trim().length() == 0) {
					if (lastLineEmpty) {
						continue;
					}
					parts.add(new PlainTextPart("\n"));
					++emptyLines;
					lastLineEmpty = emptyLines == 2;
					continue;
				}
				emptyLines = 0;
				/*
				 * lineComplete tracks whether the block you are parsing is the
				 * first block of the line. this is important because sometimes
				 * you have to add an additional line break.
				 */
				boolean lineComplete = true;
				while (line.length() > 0) {
					Optional<NextLink> nextLink = findNextLink(line);
					if (!nextLink.isPresent()) {
						if (lineComplete && !lastLineEmpty) {
							parts.add(new PlainTextPart("\n" + line));
						} else {
							parts.add(new PlainTextPart(line));
						}
						break;
					}
					LinkType linkType = nextLink.get().getLinkType();
					int next = nextLink.get().getPosition();

					/* cut off “freenet:” from before keys. */
					if (linkType.isFreenetLink() && (next >= 8) && (line.substring(next - 8, next).equals("freenet:"))) {
						next -= 8;
						line = line.substring(0, next) + line.substring(next + 8);
					}

					/* if there is text before the next item, write it out. */
					if (lineComplete && !lastLineEmpty) {
						parts.add(new PlainTextPart("\n"));
					}
					if (next > 0) {
						parts.add(new PlainTextPart(line.substring(0, next)));
						line = line.substring(next);
					}
					lineComplete = false;

					String link = nextLink.get().getLink();
					logger.log(Level.FINER, String.format("Found link: %s", link));

					/* if there is no text after the scheme, it’s not a link! */
					if (link.equals(linkType.getScheme())) {
						parts.add(new PlainTextPart(linkType.getScheme()));
						line = line.substring(linkType.getScheme().length());
						continue;
					}

					switch (linkType) {
						case SONE:
							renderSoneLink(parts, link);
							break;
						case POST:
							renderPostLink(parts, link);
							break;
						case KSK:
						case CHK:
						case SSK:
						case USK:
							renderFreenetLink(parts, link, linkType, context);
							break;
						case HTTP:
						case HTTPS:
							renderHttpLink(parts, link, linkType);
							break;
						case FREEMAIL:
							renderFreemailLink(parts, link);
					}

					line = nextLink.get().getRemainder();
				}
				lastLineEmpty = false;
			}
		} catch (IOException ioe1) {
			// a buffered reader around a string reader should never throw.
			throw new RuntimeException(ioe1);
		}
		for (int partIndex = parts.size() - 1; partIndex >= 0; --partIndex) {
			Part part = parts.getPart(partIndex);
			if (!(part instanceof PlainTextPart) || !"\n".equals(part.getText())) {
				break;
			}
			parts.removePart(partIndex);
		}
		return parts;
	}

	public static Optional<NextLink> findNextLink(String line) {
		int earliestLinkPosition = Integer.MAX_VALUE;
		NextLink earliestNextLink = null;
		for (LinkType possibleLinkType : LinkType.values()) {
			Optional<NextLink> nextLink = possibleLinkType.findNext(line);
			if (nextLink.isPresent()) {
				if (nextLink.get().getPosition() < earliestLinkPosition) {
					earliestNextLink = nextLink.get();
				}
			}
		}
		return Optional.fromNullable(earliestNextLink);
	}

	private void renderSoneLink(PartContainer parts, String line) {
		if (line.length() >= (7 + 43)) {
			String soneId = line.substring(7, 50);
			Optional<Sone> sone = soneProvider.getSone(soneId);
			parts.add(new SonePart(sone.or(new IdOnlySone(soneId))));
		} else {
			parts.add(new PlainTextPart(line));
		}
	}

	private void renderPostLink(PartContainer parts, String line) {
		if (line.length() >= (7 + 36)) {
			String postId = line.substring(7, 43);
			Optional<Post> post = postProvider.getPost(postId);
			if (post.isPresent()) {
				parts.add(new PostPart(post.get()));
			} else {
				parts.add(new PlainTextPart(line.substring(0, 43)));
			}
		} else {
			parts.add(new PlainTextPart(line));
		}
	}

	private void renderFreenetLink(PartContainer parts, String link, LinkType linkType, @Nullable SoneTextParserContext context) {
		String name = link;
		if (name.indexOf('?') > -1) {
			name = name.substring(0, name.indexOf('?'));
		}
		if (name.endsWith("/")) {
			name = name.substring(0, name.length() - 1);
		}
		try {
			FreenetURI uri = new FreenetURI(name);
			name = uri.lastMetaString();
			if (name == null) {
				name = uri.getDocName();
			}
			if (name == null) {
				name = link.substring(0, Math.min(9, link.length()));
			}
			boolean fromPostingSone = ((linkType == LinkType.SSK) || (linkType == LinkType.USK)) && (context != null) && (context.getPostingSone() != null) && link.substring(4, Math.min(link.length(), 47)).equals(context.getPostingSone().getId());
			parts.add(new FreenetLinkPart(link, name, fromPostingSone));
		} catch (MalformedURLException mue1) {
			/* not a valid link, insert as plain text. */
			parts.add(new PlainTextPart(link));
		} catch (NullPointerException npe1) {
			/* FreenetURI sometimes throws these, too. */
			parts.add(new PlainTextPart(link));
		} catch (ArrayIndexOutOfBoundsException aioobe1) {
			/* oh, and these, too. */
			parts.add(new PlainTextPart(link));
		}
	}

	private void renderHttpLink(PartContainer parts, String link, LinkType linkType) {
		String name;
		name = link.substring(linkType == LinkType.HTTP ? 7 : 8);
		int firstSlash = name.indexOf('/');
		int lastSlash = name.lastIndexOf('/');
		if ((lastSlash - firstSlash) > 3) {
			name = name.substring(0, firstSlash + 1) + "…" + name.substring(lastSlash);
		}
		if (name.endsWith("/")) {
			name = name.substring(0, name.length() - 1);
		}
		if (((name.indexOf('/') > -1) && (name.indexOf('.') < name.lastIndexOf('.', name.indexOf('/'))) || ((name.indexOf('/') == -1) && (name.indexOf('.') < name.lastIndexOf('.')))) && name.startsWith("www.")) {
			name = name.substring(4);
		}
		if (name.indexOf('?') > -1) {
			name = name.substring(0, name.indexOf('?'));
		}
		parts.add(new LinkPart(link, name));
	}

	private void renderFreemailLink(PartContainer parts, String line) {
		int separator = line.indexOf('@');
		String freemailId = line.substring(separator + 1, separator + 53);
		String identityId = Base64.encode(Base32.decode(freemailId));
		String emailLocalPart = line.substring(0, separator);
		parts.add(new FreemailPart(emailLocalPart, freemailId, identityId));
	}

	private static boolean isPunctuation(char character) {
		return (character == '.') || (character == ',') || (character == '!') || (character == '?');
	}

}
