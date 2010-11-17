/*
 * Sone - ListNotification.java - Copyright © 2010 David Roden
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

package net.pterodactylus.sone.notify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.pterodactylus.util.notify.TemplateNotification;
import net.pterodactylus.util.template.Template;

/**
 * Notification that maintains a list of new elements.
 *
 * @param <T>
 *            The type of the items
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ListNotification<T> extends TemplateNotification {

	/** The list of new elements. */
	private final List<T> elements = Collections.synchronizedList(new ArrayList<T>());

	/**
	 * Creates a new list notification.
	 *
	 * @param id
	 *            The ID of the notification
	 * @param key
	 *            The key under which to store the elements in the template
	 * @param template
	 *            The template to render
	 */
	public ListNotification(String id, String key, Template template) {
		super(id, template);
		template.set(key, elements);
	}

	//
	// ACTIONS
	//

	/**
	 * Returns whether there are any new elements.
	 *
	 * @return {@code true} if there are no new elements, {@code false} if there
	 *         are new elements
	 */
	public boolean isEmpty() {
		return elements.isEmpty();
	}

	/**
	 * Adds a discovered element.
	 *
	 * @param element
	 *            The new element
	 */
	public void add(T element) {
		elements.add(element);
		touch();
	}

	/**
	 * Removes the given element from the list of new elements.
	 *
	 * @param element
	 *            The element to remove
	 */
	public void remove(T element) {
		elements.remove(element);
		if (elements.isEmpty()) {
			dismiss();
		}
		touch();
	}

	//
	// ABSTRACTNOTIFICATION METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dismiss() {
		super.dismiss();
		elements.clear();
	}

}
