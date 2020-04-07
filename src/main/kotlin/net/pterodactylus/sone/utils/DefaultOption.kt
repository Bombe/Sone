package net.pterodactylus.sone.utils

import java.util.function.Predicate

/**
 * Basic implementation of an [Option].
 *
 * @param <T> The type of the option
 */
class DefaultOption<T> @JvmOverloads constructor(
		private val defaultValue: T,
		private val validator: ((T) -> Boolean)? = null
) : Option<T> {

	@Volatile
	private var value: T? = null

	override fun get() = value ?: defaultValue

	override fun getReal(): T? = value

	override fun validate(value: T?): Boolean =
			value == null || validator?.invoke(value) ?: true

	override fun set(value: T?) {
		require(validate(value)) { "New Value ($value) could not be validated." }
		this.value = value
	}

}
