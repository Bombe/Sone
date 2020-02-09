/*
 * Sone - Parser.java - Copyright © 2010–2020 David Roden
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

import java.io.Reader;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface for parsers that can create {@link Part}s from a text source
 * (usually a {@link Reader}).
 *
 * @param <C>
 *            The type of the parser context
 */
public interface Parser<C extends ParserContext> {

	/**
	 * Create one or more {@link Part}s from the given text source.
	 *
	 * @param source
	 *            The text source
	 * @param context
	 *            The parser context (may be {@code null})
	 * @return The parsed parts
	 */
	@Nonnull
	Iterable<Part> parse(@Nonnull String source, @Nullable C context);

}
