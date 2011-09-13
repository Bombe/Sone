/*
 * Sone - PartContainer.java - Copyright © 2010 David Roden
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

/**
 * Part implementation that can contain an arbitrary amount of other parts.
 * Parts are added using the {@link #add(Part)} method and will be rendered in
 * the order they are added.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PartContainer implements Part, Iterable<Part> {

	/** The parts to render. */
	private final List<Part> parts = new ArrayList<Part>();

	//
	// ACCESSORS
	//

	/**
	 * Adds a part to render.
	 *
	 * @param part
	 *            The part to add
	 */
	public void add(Part part) {
		parts.add(part);
	}

	/**
	 * Returns the part at the given index.
	 *
	 * @param index
	 *            The index of the part
	 * @return The part
	 */
	public Part getPart(int index) {
		return parts.get(index);
	}

	/**
	 * Removes the part at the given index.
	 *
	 * @param index
	 *            The index of the part to remove
	 */
	public void removePart(int index) {
		parts.remove(index);
	}

	/**
	 * Returns the number of parts.
	 *
	 * @return The number of parts
	 */
	public int size() {
		return parts.size();
	}

	//
	// ITERABLE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
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
					@SuppressWarnings("hiding")
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
