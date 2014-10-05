/*
 * Sone - SoneTextParser.java - Copyright © 2010–2013 David Roden
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

import static java.util.logging.Logger.getLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.SoneImpl;
import net.pterodactylus.sone.data.impl.IdOnlySone;
import net.pterodactylus.sone.database.PostProvider;
import net.pterodactylus.sone.database.SoneProvider;
import net.pterodactylus.util.io.Closer;

import com.google.common.base.Optional;

import freenet.keys.FreenetURI;

/**
 * {@link Parser} implementation that can recognize Freenet URIs.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneTextParser implements Parser<SoneTextParserContext> {

	/** The logger. */
	private static final Logger logger = getLogger("Sone.Data.Parser");

	/** Pattern to detect whitespace. */
	private static final Pattern whitespacePattern = Pattern.compile("[\\u000a\u0020\u00a0\u1680\u180e\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200a\u200b\u200c\u200d\u202f\u205f\u2060\u2800\u3000]");

	/**
	 * Enumeration for all recognized link types.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	private enum LinkType {

		/** Link is a KSK. */
		KSK("KSK@"),

		/** Link is a CHK. */
		CHK("CHK@"),

		/** Link is an SSK. */
		SSK("SSK@"),

		/** Link is a USK. */
		USK("USK@"),

		/** Link is HTTP. */
		HTTP("http://"),

		/** Link is HTTPS. */
		HTTPS("https://"),

		/** Link is a Sone. */
		SONE("sone://"),

		/** Link is a post. */
		POST("post://");

		/** The scheme identifying this link type. */
		private final String scheme;

		/**
		 * Creates a new link type identified by the given scheme.
		 *
		 * @param scheme
		 *            The scheme of the link type
		 */
		private LinkType(String scheme) {
			this.scheme = scheme;
		}

		/**
		 * Returns the scheme of this link type.
		 *
		 * @return The scheme of this link type
		 */
		public String getScheme() {
			return scheme;
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
	@Override
	public Iterable<Part> parse(SoneTextParserContext context, Reader source) throws IOException {
		PartContainer parts = new PartContainer();
		BufferedReader bufferedReader = (source instanceof BufferedReader) ? (BufferedReader) source : new BufferedReader(source);
		try {
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
					int nextKsk = line.indexOf("KSK@");
					int nextChk = line.indexOf("CHK@");
					int nextSsk = line.indexOf("SSK@");
					int nextUsk = line.indexOf("USK@");
					int nextHttp = line.indexOf("http://");
					int nextHttps = line.indexOf("https://");
					int nextSone = line.indexOf("sone://");
					int nextPost = line.indexOf("post://");
					if ((nextKsk == -1) && (nextChk == -1) && (nextSsk == -1) && (nextUsk == -1) && (nextHttp == -1) && (nextHttps == -1) && (nextSone == -1) && (nextPost == -1)) {
						if (lineComplete && !lastLineEmpty) {
							parts.add(new PlainTextPart("\n" + line));
						} else {
							parts.add(new PlainTextPart(line));
						}
						break;
					}
					int next = Integer.MAX_VALUE;
					LinkType linkType = null;
					if ((nextKsk > -1) && (nextKsk < next)) {
						next = nextKsk;
						linkType = LinkType.KSK;
					}
					if ((nextChk > -1) && (nextChk < next)) {
						next = nextChk;
						linkType = LinkType.CHK;
					}
					if ((nextSsk > -1) && (nextSsk < next)) {
						next = nextSsk;
						linkType = LinkType.SSK;
					}
					if ((nextUsk > -1) && (nextUsk < next)) {
						next = nextUsk;
						linkType = LinkType.USK;
					}
					if ((nextHttp > -1) && (nextHttp < next)) {
						next = nextHttp;
						linkType = LinkType.HTTP;
					}
					if ((nextHttps > -1) && (nextHttps < next)) {
						next = nextHttps;
						linkType = LinkType.HTTPS;
					}
					if ((nextSone > -1) && (nextSone < next)) {
						next = nextSone;
						linkType = LinkType.SONE;
					}
					if ((nextPost > -1) && (nextPost < next)) {
						next = nextPost;
						linkType = LinkType.POST;
					}

					/* cut off “freenet:” from before keys. */
					if (((linkType == LinkType.KSK) || (linkType == LinkType.CHK) || (linkType == LinkType.SSK) || (linkType == LinkType.USK)) && (next >= 8) && (line.substring(next - 8, next).equals("freenet:"))) {
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
						next = 0;
					}
					lineComplete = false;

					Matcher matcher = whitespacePattern.matcher(line);
					int nextSpace = matcher.find(0) ? matcher.start() : line.length();
					String link = line.substring(0, nextSpace);
					String name = link;
					logger.log(Level.FINER, String.format("Found link: %s", link));
					logger.log(Level.FINEST, String.format("CHK: %d, SSK: %d, USK: %d", nextChk, nextSsk, nextUsk));

					/* if there is no text after the scheme, it’s not a link! */
					if (link.equals(linkType.getScheme())) {
						parts.add(new PlainTextPart(linkType.getScheme()));
						line = line.substring(linkType.getScheme().length());
						continue;
					}

					if (linkType == LinkType.SONE) {
						if (line.length() >= (7 + 43)) {
							String soneId = line.substring(7, 50);
							Optional<Sone> sone = soneProvider.getSone(soneId);
							if (!sone.isPresent()) {
								/*
								 * don’t use create=true above, we don’t want
								 * the empty shell.
								 */
								sone = Optional.<Sone>of(new IdOnlySone(soneId));
							}
							parts.add(new SonePart(sone.get()));
							line = line.substring(50);
						} else {
							parts.add(new PlainTextPart(line));
							line = "";
						}
						continue;
					}
					if (linkType == LinkType.POST) {
						if (line.length() >= (7 + 36)) {
							String postId = line.substring(7, 43);
							Optional<Post> post = postProvider.getPost(postId);
							if (post.isPresent()) {
								parts.add(new PostPart(post.get()));
							} else {
								parts.add(new PlainTextPart(line.substring(0, 43)));
							}
							line = line.substring(43);
						} else {
							parts.add(new PlainTextPart(line));
							line = "";
						}
						continue;
					}

					if ((linkType == LinkType.KSK) || (linkType == LinkType.CHK) || (linkType == LinkType.SSK) || (linkType == LinkType.USK)) {
						FreenetURI uri;
						if (name.indexOf('?') > -1) {
							name = name.substring(0, name.indexOf('?'));
						}
						if (name.endsWith("/")) {
							name = name.substring(0, name.length() - 1);
						}
						try {
							uri = new FreenetURI(name);
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
					} else if ((linkType == LinkType.HTTP) || (linkType == LinkType.HTTPS)) {
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
					line = line.substring(nextSpace);
				}
				lastLineEmpty = false;
			}
		} finally {
			if (bufferedReader != source) {
				Closer.close(bufferedReader);
			}
		}
		for (int partIndex = parts.size() - 1; partIndex >= 0; --partIndex) {
			Part part = parts.getPart(partIndex);
			if (!(part instanceof PlainTextPart) || !"\n".equals(((PlainTextPart) part).getText())) {
				break;
			}
			parts.removePart(partIndex);
		}
		return parts;
	}

}
