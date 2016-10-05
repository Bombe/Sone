/*
 * Sone - PartContainer.java - Copyright © 2010–2016 David Roden
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Part implementation that can contain an arbitrary amount of other parts.
 * Parts are added using the {@link #add(Part)} method and will be rendered in
 * the order they are added.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PartContainer implements Part, Iterable<Part> {

	private final List<Part> parts = new ArrayList<Part>();

	public void add(@Nonnull Part part) {
		parts.add(Objects.requireNonNull(part));
	}

	@Nonnull
	public Part getPart(int index) {
		return parts.get(index);
	}

	public void removePart(int index) {
		parts.remove(index);
	}

	public int size() {
		return parts.size();
	}

	@Override
	@Nonnull
	public String getText() {
		StringBuilder partText = new StringBuilder();
		for (Part part : parts) {
			partText.append(part.getText());
		}
		return partText.toString();
	}

	@Override
	@Nonnull
	@SuppressWarnings("synthetic-access")
	public Iterator<Part> iterator() {
		return new Iterator<Part>() {

			private Deque<Iterator<Part>> partStack = new ArrayDeque<Iterator<Part>>();
			private Part nextPart;
			private boolean foundNextPart;
			private boolean noNextPart;

			{
				partStack.push(parts.iterator());
			}

			private void findNext() {
				if (foundNextPart) {
					return;
				}
				noNextPart = true;
				while (!partStack.isEmpty()) {
					Iterator<Part> parts = partStack.pop();
					if (parts.hasNext()) {
						nextPart = parts.next();
						partStack.push(parts);
						if (nextPart instanceof PartContainer) {
							partStack.push(((PartContainer) nextPart).iterator());
						} else {
							noNextPart = false;
							break;
						}
					}
				}
				foundNextPart = true;
			}

			@Override
			public boolean hasNext() {
				findNext();
				return !noNextPart;
			}

			@Override
			public Part next() {
				findNext();
				if (noNextPart) {
					throw new NoSuchElementException();
				}
				foundNextPart = false;
				return nextPart;
			}

			@Override
			public void remove() {
				/* ignore. */
			}

		};
	}

}
