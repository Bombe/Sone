/*
 * Sone - FreenetLinkParser.java - Copyright © 2010 David Roden
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.template.SoneAccessor;
import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContextFactory;
import net.pterodactylus.util.template.TemplateParser;
import freenet.keys.FreenetURI;

/**
 * {@link Parser} implementation that can recognize Freenet URIs.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneTextParser implements Parser<SoneTextParserContext> {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(SoneTextParser.class);

	/** Pattern to detect whitespace. */
	private static final Pattern whitespacePattern = Pattern.compile("[\\u000a\u0020\u00a0\u1680\u180e\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200a\u200b\u200c\u200d\u202f\u205f\u2060\u2800\u3000]");

	/**
	 * Enumeration for all recognized link types.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	private enum LinkType {

		/** Link is a KSK. */
		KSK,

		/** Link is a CHK. */
		CHK,

		/** Link is an SSK. */
		SSK,

		/** Link is a USK. */
		USK,

		/** Link is HTTP. */
		HTTP,

		/** Link is HTTPS. */
		HTTPS,

		/** Link is a Sone. */
		SONE,

		/** Link is a post. */
		POST,

	}

	/** The core. */
	private final Core core;

	/**
	 * Creates a new freenet link parser.
	 *
	 * @param core
	 *            The core
	 */
	public SoneTextParser(Core core) {
		this.core = core;
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
				if (linkType == LinkType.SONE) {
					if (next > 0) {
						parts.add(new PlainTextPart(line.substring(0, next)));
					}
					if (line.length() >= (next + 7 + 43)) {
						String soneId = line.substring(next + 7, next + 50);
						Sone sone = core.getSone(soneId, false);
						if (sone != null) {
							parts.add(new SonePart(sone));
						} else {
							parts.add(new PlainTextPart(line.substring(next, next + 50)));
						}
						line = line.substring(next + 50);
					} else {
						parts.add(new PlainTextPart(line.substring(next)));
						line = "";
					}
					continue;
				}
				if (linkType == LinkType.POST) {
					if (next > 0) {
						parts.add(new PlainTextPart(line.substring(0, next)));
					}
					if (line.length() >= (next + 7 + 36)) {
						String postId = line.substring(next + 7, next + 43);
						Post post = core.getPost(postId, false);
						if ((post != null) && (post.getSone() != null)) {
							String postText = post.getText();
							postText = postText.substring(0, Math.min(postText.length(), 20)) + "…";
							parts.add(new PostPart(post));
						} else {
							parts.add(new PlainTextPart(line.substring(next, next + 43)));
						}
						line = line.substring(next + 43);
					} else {
						parts.add(new PlainTextPart(line.substring(next)));
						line = "";
					}
					continue;
				}
				if ((next >= 8) && (line.substring(next - 8, next).equals("freenet:"))) {
					next -= 8;
					line = line.substring(0, next) + line.substring(next + 8);
				}
				Matcher matcher = whitespacePattern.matcher(line);
				int nextSpace = matcher.find(next) ? matcher.start() : line.length();
				if (nextSpace > (next + 4)) {
					if (!lastLineEmpty && lineComplete) {
						parts.add(new PlainTextPart("\n" + line.substring(0, next)));
					} else {
						parts.add(new PlainTextPart(line.substring(0, next)));
					}
					String link = line.substring(next, nextSpace);
					String name = link;
					logger.log(Level.FINER, "Found link: %s", link);
					logger.log(Level.FINEST, "Next: %d, CHK: %d, SSK: %d, USK: %d", new Object[] { next, nextChk, nextSsk, nextUsk });

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
							boolean fromPostingSone = ((linkType == LinkType.SSK) || (linkType == LinkType.USK)) && (context.getPostingSone() != null) && link.substring(4, Math.min(link.length(), 47)).equals(context.getPostingSone().getId());
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
				} else {
					if (!lastLineEmpty && lineComplete) {
						parts.add(new PlainTextPart("\n" + line.substring(0, next + 4)));
					} else {
						parts.add(new PlainTextPart(line.substring(0, next + 4)));
					}
					line = line.substring(next + 4);
				}
				lineComplete = false;
			}
			lastLineEmpty = false;
		}
		for (int partIndex = parts.size() - 1; partIndex >= 0; --partIndex) {
			Part part = parts.getPart(partIndex);
			if ((part instanceof PlainTextPart) && !"\n".equals(((PlainTextPart) part).getText())) {
				break;
			}
			parts.removePart(partIndex);
		}
		return parts;
	}

}
