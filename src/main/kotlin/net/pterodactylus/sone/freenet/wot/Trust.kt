/*
 * Sone - Trust.kt - Copyright © 2010–2020 David Roden
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

package net.pterodactylus.sone.freenet.wot

/**
 * Container class for trust in the web of trust.
 */
data class Trust(val explicit: Int?, val implicit: Int?, val distance: Int?)

fun trustExplicitely(value: Int) = Trust(explicit = value, implicit = null, distance = 1)
fun trustImplicitely(value: Int, distance: Int = 2) = Trust(explicit = null, implicit = value, distance)
