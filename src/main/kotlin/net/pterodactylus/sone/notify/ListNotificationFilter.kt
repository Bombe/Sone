/*
 * Sone - ListNotificationFilter.kt - Copyright © 2010–2020 David Roden
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

import com.google.inject.ImplementedBy
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.util.notify.Notification

/**
 * Filters [list notifications][ListNotification], depending on the current
 * [Sone] and [its options][Sone.getOptions].
 */
@ImplementedBy(DefaultListNotificationFilter::class)
interface ListNotificationFilter {

	fun filterNotifications(notifications: Collection<Notification>, currentSone: Sone?): Collection<Notification>

}
