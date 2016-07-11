/*
 * Sone - ListNotification.java - Copyright © 2010–2016 David Roden
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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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

	/** The key under which to store the elements in the template. */
	private final String key;

	/** The list of new elements. */
	private final List<T> elements = new CopyOnWriteArrayList<T>();

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
		this(id, key, template, true);
	}

	/**
	 * Creates a new list notification.
	 *
	 * @param id
	 *            The ID of the notification
	 * @param key
	 *            The key under which to store the elements in the template
	 * @param template
	 *            The template to render
	 * @param dismissable
	 *            {@code true} if this notification should be dismissable by the
	 *            user, {@code false} otherwise
	 */
	public ListNotification(String id, String key, Template template, boolean dismissable) {
		super(id, System.currentTimeMillis(), System.currentTimeMillis(), dismissable, template);
		this.key = key;
		template.getInitialContext().set(key, elements);
	}

	/**
	 * Creates a new list notification that copies its ID and the template from
	 * the given list notification.
	 *
	 * @param listNotification
	 *            The list notification to copy
	 */
	public ListNotification(ListNotification<T> listNotification) {
		super(listNotification.getId(), listNotification.getCreatedTime(), listNotification.getLastUpdatedTime(), listNotification.isDismissable(), new Template());
		this.key = listNotification.key;
		getTemplate().add(listNotification.getTemplate());
		getTemplate().getInitialContext().set(key, elements);
	}

	//
	// ACTIONS
	//

	/**
	 * Returns the current list of elements.
	 *
	 * @return The current list of elements
	 */
	public List<T> getElements() {
		return new ArrayList<T>(elements);
	}

	/**
	 * Sets the elements to show in this notification. This method will not call
	 * {@link #touch()}.
	 *
	 * @param elements
	 *            The elements to show
	 */
	public void setElements(Collection<? extends T> elements) {
		this.elements.clear();
		this.elements.addAll(elements);
		touch();
	}

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
		while (elements.remove(element)) {
			/* do nothing, just remove all instances of the element. */
		}
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

	//
	// OBJECT METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hashCode = super.hashCode();
		for (T element : elements) {
			hashCode ^= element.hashCode();
		}
		return hashCode;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof ListNotification)) {
			return false;
		}
		ListNotification<?> listNotification = (ListNotification<?>) object;
		if (!super.equals(listNotification)) {
			return false;
		}
		if (!key.equals(listNotification.key)) {
			return false;
		}
		return elements.equals(listNotification.elements);
	}

}
