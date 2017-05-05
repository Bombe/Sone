package net.pterodactylus.sone.utils

/**
 * Helper class for lists that need pagination. Setting the page or the page
 * size will automatically recalculate all other parameters, and the next call
 * to [Pagination.items] retrieves all items on the current page.
 * <p>
 * A pagination object can be used as an [Iterable]. When the [Iterator]
 * from [Pagination.iterator] is requested, the iterator over
 * [Pagination.items] is returned.
 *
 * @param <T>
 *            The type of the list elements
 */
class Pagination<out T>(private val originalItems: List<T>, pageSize: Int): Iterable<T> {

	var page: Int = 0
		set(value) {
			field = maxOf(0, minOf(value, lastPage))
		}

	var pageSize = pageSize
		set(value) {
			val oldFirstIndex = page * field
			field = maxOf(1, value)
			page = oldFirstIndex / field
		}

	val pageNumber get() = page + 1
	val pageCount get() = maxOf((originalItems.size - 1) / pageSize + 1, 1)
	val itemCount get() = minOf(originalItems.size - page * pageSize, pageSize)
	val items get() = originalItems.subList(page * pageSize, minOf(originalItems.size, (page + 1) * pageSize))
	val isFirst get() = page == 0
	val isLast get() = page == lastPage
	val isNecessary get() = pageCount > 1
	val previousPage get() = page - 1
	val nextPage get() = page + 1
	val lastPage get() = pageCount - 1

	override fun iterator() = items.iterator()

}
