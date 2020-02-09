/*
 * Sone - ListNotification.kt - Copyright © 2010–2020 David Roden
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

package net.pterodactylus.sone.notify

import net.pterodactylus.util.notify.*
import net.pterodactylus.util.template.*
import java.lang.System.*
import java.util.concurrent.*

/**
 * Notification that maintains a list of elements.
 *
 * @param <T>
 * The type of the items
 */
class ListNotification<T> : TemplateNotification {

	private val key: String
	private val realElements = CopyOnWriteArrayList<T>()

	val elements: List<T> get() = realElements.toList()

	val isEmpty
		get() = elements.isEmpty()

	@JvmOverloads
	constructor(id: String, key: String, template: Template, dismissable: Boolean = true) : super(id, currentTimeMillis(), currentTimeMillis(), dismissable, template) {
		this.key = key
		template.initialContext.set(key, realElements)
	}

	constructor(listNotification: ListNotification<T>) : super(listNotification.id, listNotification.createdTime, listNotification.lastUpdatedTime, listNotification.isDismissable, Template()) {
		this.key = listNotification.key
		template.add(listNotification.template)
		template.initialContext.set(key, realElements)
	}

	fun setElements(elements: Collection<T>) {
		realElements.clear()
		realElements.addAll(elements.distinct())
		touch()
	}

	fun add(element: T) {
		if (element !in realElements) {
			realElements.add(element)
			touch()
		}
	}

	fun remove(element: T) {
		while (realElements.remove(element)) {
			/* do nothing, just remove all instances of the element. */
		}
		if (realElements.isEmpty()) {
			dismiss()
		}
		touch()
	}

	override fun dismiss() {
		super.dismiss()
		realElements.clear()
	}

	override fun hashCode() =
			realElements.fold(super.hashCode()) { hash, element -> hash xor element.hashCode() }

	override fun equals(other: Any?): Boolean {
		if (other !is ListNotification<*>) {
			return false
		}
		val listNotification = other as ListNotification<*>?
		if (!super.equals(listNotification)) {
			return false
		}
		return (key == listNotification.key) && (realElements == listNotification.realElements)
	}

}
