/*
 * Sone - FreenetLinkPart.java - Copyright © 2011–2016 David Roden
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

import javax.annotation.Nonnull;

/**
 * {@link LinkPart} implementation that stores an additional attribute: if the
 * link is an SSK or USK link and the post was created by an identity that owns
 * the keyspace in question.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FreenetLinkPart extends LinkPart {

	private final boolean trusted;

	public FreenetLinkPart(@Nonnull String link, @Nonnull String text, boolean trusted) {
		this(link, text, link, trusted);
	}

	public FreenetLinkPart(@Nonnull String link, @Nonnull String text, @Nonnull String title, boolean trusted) {
		super(link, text, title);
		this.trusted = trusted;
	}

	public boolean isTrusted() {
		return trusted;
	}

}
