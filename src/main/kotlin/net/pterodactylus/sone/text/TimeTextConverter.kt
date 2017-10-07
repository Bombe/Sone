package net.pterodactylus.sone.text

import net.pterodactylus.sone.freenet.L10nText
import java.util.concurrent.TimeUnit.DAYS
import java.util.concurrent.TimeUnit.HOURS
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.MINUTES
import java.util.concurrent.TimeUnit.SECONDS

/**
 * Converts a time (in Java milliseconds) to an L10n key and a refresh time.
 */
class TimeTextConverter(private val timeSuppler: () -> Long = { System.currentTimeMillis() }) {

	fun getTimeText(time: Long): TimeText {
		val age = timeSuppler.invoke() - time
		return when {
			time == 0L -> TimeText(L10nText("View.Sone.Text.UnknownDate"), 12.hours())
			age < 0 -> TimeText(L10nText("View.Time.InTheFuture"), 5.minutes())
			age < 20.seconds() -> TimeText(L10nText("View.Time.AFewSecondsAgo"), 10.seconds())
			age < 45.seconds() -> TimeText(L10nText("View.Time.HalfAMinuteAgo"), 20.seconds())
			age < 90.seconds() -> TimeText(L10nText("View.Time.AMinuteAgo"), 1.minutes())
			age < 30.minutes() -> TimeText(L10nText("View.Time.XMinutesAgo", listOf((age + 30.seconds()).toMinutes())), 1.minutes())
			age < 45.minutes() -> TimeText(L10nText("View.Time.HalfAnHourAgo"), 10.minutes())
			age < 90.minutes() -> TimeText(L10nText("View.Time.AnHourAgo"), 1.hours())
			age < 21.hours() -> TimeText(L10nText("View.Time.XHoursAgo", listOf((age + 30.minutes()).toHours())), 1.hours())
			age < 42.hours() -> TimeText(L10nText("View.Time.ADayAgo"), 1.days())
			age < 6.days() -> TimeText(L10nText("View.Time.XDaysAgo", listOf((age + 12.hours()).toDays())), 1.days())
			age < 11.days() -> TimeText(L10nText("View.Time.AWeekAgo"), 1.days())
			age < 28.days() -> TimeText(L10nText("View.Time.XWeeksAgo", listOf((age + 3.days() + 12.hours()).toWeeks())), 1.days())
			age < 42.days() -> TimeText(L10nText("View.Time.AMonthAgo"), 1.days())
			age < 330.days() -> TimeText(L10nText("View.Time.XMonthsAgo", listOf((age + 15.days()).toMonths())), 1.days())
			age < 540.days() -> TimeText(L10nText("View.Time.AYearAgo"), 7.days())
			else -> TimeText(L10nText("View.Time.XYearsAgo", listOf((age + 182.days() + 12.hours()).toYears())), 7.days())
		}
	}

	private fun Long.toMinutes() = MILLISECONDS.toMinutes(this)
	private fun Long.toHours() = MILLISECONDS.toHours(this)
	private fun Long.toDays() = MILLISECONDS.toDays(this)
	private fun Long.toWeeks() = MILLISECONDS.toDays(this) / 7
	private fun Long.toMonths() = MILLISECONDS.toDays(this) / 30
	private fun Long.toYears() = MILLISECONDS.toDays(this) / 365
	private fun Int.seconds() = SECONDS.toMillis(this.toLong())
	private fun Int.minutes() = MINUTES.toMillis(this.toLong())
	private fun Int.hours() = HOURS.toMillis(this.toLong())
	private fun Int.days() = DAYS.toMillis(this.toLong())

}

