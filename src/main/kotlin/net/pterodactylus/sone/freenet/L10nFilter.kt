/*
 * Sone - L10nFilter.java - Copyright © 2010–2019 David Roden
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

package net.pterodactylus.sone.freenet

import net.pterodactylus.util.template.*
import java.text.*

/**
 * [Filter] implementation replaces [String] values with their
 * translated equivalents.
 */
class L10nFilter(private val translation: Translation) : Filter {

	override fun format(templateContext: TemplateContext?, data: Any?, parameters: Map<String, Any>?): String {
		val parameterValues = getParameters(data, parameters)
		val text = getText(data)
		return if (parameterValues.isEmpty()) {
			translation.translate(text)
		} else
			MessageFormat(translation.translate(text), translation.currentLocale).format(parameterValues.toTypedArray())
	}

	private fun getText(data: Any?) = (data as? L10nText)?.text ?: data.toString()

	private fun getParameters(data: Any?, parameters: Map<String, Any>?) =
			if (data is L10nText)
				data.parameters
			else
				(parameters ?: emptyMap()).let { params ->
					generateSequence(0) { it + 1 }
							.takeWhile { it.toString() in params }
							.map { params[it.toString()] }
							.toList()
				}

}
