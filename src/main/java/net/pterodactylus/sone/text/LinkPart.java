/*
 * Sone - LinkPart.java - Copyright © 2011–2016 David Roden
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

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * {@link Part} implementation that can hold a link. A link contains of three
 * attributes: the link itself, the text that is shown instead of the link, and
 * an explanatory text that can be displayed e.g. as a tooltip.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class LinkPart implements Part {

	private final String link;
	private final String text;
	private final String title;

	public LinkPart(@Nonnull String link, @Nonnull String text) {
		this(link, text, text);
	}

	public LinkPart(@Nonnull String link, @Nonnull String text, @Nonnull String title) {
		this.link = Objects.requireNonNull(link);
		this.text = Objects.requireNonNull(text);
		this.title = Objects.requireNonNull(title);
	}

	@Nonnull
	public String getLink() {
		return link;
	}

	@Nonnull
	public String getTitle() {
		return title;
	}

	@Override
	@Nonnull
	public String getText() {
		return text;
	}

}
