package net.pterodactylus.sone.text

import net.pterodactylus.sone.freenet.L10nText
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import java.util.concurrent.TimeUnit.DAYS
import java.util.concurrent.TimeUnit.HOURS
import java.util.concurrent.TimeUnit.MINUTES
import java.util.concurrent.TimeUnit.SECONDS

/**
 * Unit test for [TimeTextConverter].
 */
class TimeTextConverterTest {

	val now = System.currentTimeMillis()
	val converter = TimeTextConverter { now }

	private fun verifyInterval(startTime: Long, end: Long, vararg expectedTimeTexts: TimeText) {
		assertThat(converter.getTimeText(now - startTime), equalTo(expectedTimeTexts[0]))
		assertThat(converter.getTimeText(now - (end - 1)), equalTo(expectedTimeTexts[1 % expectedTimeTexts.size]))
	}

	@Test
	fun `time of zero returns the l10n key for "unknown" and a refresh time of 12 hours`() {
		assertThat(converter.getTimeText(0), equalTo(TimeText(L10nText("View.Sone.Text.UnknownDate"), HOURS.toMillis(12))))
	}

	@Test
	fun `time in the future returns the correct l10n key and refresh of 5 minutes`() {
		assertThat(converter.getTimeText(now + 1), equalTo(TimeText(L10nText("View.Time.InTheFuture"), MINUTES.toMillis(5))))
	}

	@Test
	fun `time of zero to twenty seconds ago returns l10n key for "a few seconds ago" and refresh of 10 seconds`() {
		verifyInterval(0, SECONDS.toMillis(20), TimeText(L10nText("View.Time.AFewSecondsAgo"), SECONDS.toMillis(10)))
	}

	@Test
	fun `time of twenty to forty-five seconds ago returns l10n key for "half a minute ago" and refresh of 20 seconds`() {
		verifyInterval(SECONDS.toMillis(20), SECONDS.toMillis(45), TimeText(L10nText("View.Time.HalfAMinuteAgo"), SECONDS.toMillis(20)))
	}

	@Test
	fun `time of forty-five to ninety seconds ago returns l10n key for "a minute ago" and a refresh time of 1 minute`() {
		verifyInterval(SECONDS.toMillis(45), SECONDS.toMillis(90), TimeText(L10nText("View.Time.AMinuteAgo"), MINUTES.toMillis(1)))
	}

	@Test
	fun `time of ninety seconds to thirty minutes ago returns l10n key for "x minutes ago," the number of minutes, and a refresh time of 1 minute`() {
		verifyInterval(SECONDS.toMillis(90), MINUTES.toMillis(30),
				TimeText(L10nText("View.Time.XMinutesAgo", listOf(2L)), MINUTES.toMillis(1)),
				TimeText(L10nText("View.Time.XMinutesAgo", listOf(30L)), MINUTES.toMillis(1)))
	}

	@Test
	fun `time of thirty to forty-five minutes ago returns l10n key for "half an hour ago" and a refresh time of 10 minutes`() {
		verifyInterval(MINUTES.toMillis(30), MINUTES.toMillis(45), TimeText(L10nText("View.Time.HalfAnHourAgo"), MINUTES.toMillis(10)))
	}

	@Test
	fun `time of forty-five to ninety minutes ago returns l10n key for "an hour ago" and a refresh time of 1 hour`() {
		verifyInterval(MINUTES.toMillis(45), MINUTES.toMillis(90), TimeText(L10nText("View.Time.AnHourAgo"), HOURS.toMillis(1)))
	}

	@Test
	fun `time of ninety minutes to twenty-one hours ago returns l10n key for "x hours ago," the number of hours, and a refresh time of 1 hour`() {
		verifyInterval(MINUTES.toMillis(90), HOURS.toMillis(21),
				TimeText(L10nText("View.Time.XHoursAgo", listOf(2L)), HOURS.toMillis(1)),
				TimeText(L10nText("View.Time.XHoursAgo", listOf(21L)), HOURS.toMillis(1)))
	}

	@Test
	fun `time of twenty-one to forty-two hours ago returns l10n key for "a day ago" and a refresh time of 1 day`() {
		verifyInterval(HOURS.toMillis(21), HOURS.toMillis(42), TimeText(L10nText("View.Time.ADayAgo"), DAYS.toMillis(1)))
	}

	@Test
	fun `time of forty-two hours to six days ago returns l10n key for "x days ago," the number of days, and a refresh time of 1 day`() {
		verifyInterval(HOURS.toMillis(42), DAYS.toMillis(6),
				TimeText(L10nText("View.Time.XDaysAgo", listOf(2L)), DAYS.toMillis(1)),
				TimeText(L10nText("View.Time.XDaysAgo", listOf(6L)), DAYS.toMillis(1)))
	}

	@Test
	fun `time of six to eleven days ago returns l10n key for "a week ago" and a refresh time of 1 day`() {
		verifyInterval(DAYS.toMillis(6), DAYS.toMillis(11), TimeText(L10nText("View.Time.AWeekAgo"), DAYS.toMillis(1)))
	}

	@Test
	fun `time of eleven to twenty-eight days ago returns l10n key for "x weeks ago," the number of weeks, and a refresh time of 1 day`() {
		verifyInterval(DAYS.toMillis(11), DAYS.toMillis(28),
				TimeText(L10nText("View.Time.XWeeksAgo", listOf(2L)), DAYS.toMillis(1)),
				TimeText(L10nText("View.Time.XWeeksAgo", listOf(4L)), DAYS.toMillis(1)))
	}

	@Test
	fun `time of twenty-eight to forty-two days ago returns l10n key for "a month ago" and a refresh time of 1 day`() {
		verifyInterval(DAYS.toMillis(28), DAYS.toMillis(42), TimeText(L10nText("View.Time.AMonthAgo"), DAYS.toMillis(1)))
	}

	@Test
	fun `time of forty-two to three hundred and thirty days ago returns l10n key for "x months ago," the number of months, and a refresh time of 1 day`() {
		verifyInterval(DAYS.toMillis(42), DAYS.toMillis(330),
				TimeText(L10nText("View.Time.XMonthsAgo", listOf(1L)), DAYS.toMillis(1)),
				TimeText(L10nText("View.Time.XMonthsAgo", listOf(11L)), DAYS.toMillis(1)))
	}

	@Test
	fun `time of three hundred and thirty to five hundred and forty days days ago returns l10n key for "a year ago" and a refresh time of 7 days`() {
		verifyInterval(DAYS.toMillis(330), DAYS.toMillis(540), TimeText(L10nText("View.Time.AYearAgo"), DAYS.toMillis(7)))
	}

	@Test
	fun `time of five hunder and forty to infinity days ago returns l10n key for "x years ago," the number of years, and a refresh time of 7 days`() {
		verifyInterval(DAYS.toMillis(540), DAYS.toMillis(6000),
				TimeText(L10nText("View.Time.XYearsAgo", listOf(1L)), DAYS.toMillis(7)),
				TimeText(L10nText("View.Time.XYearsAgo", listOf(16L)), DAYS.toMillis(7)))
	}

}
