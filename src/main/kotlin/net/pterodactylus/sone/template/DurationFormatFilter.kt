package net.pterodactylus.sone.template

import net.pterodactylus.util.template.*
import java.time.*

class DurationFormatFilter : Filter {

	override fun format(templateContext: TemplateContext?, data: Any?, parameters: Map<String, Any?>?): Any? {
		if (data is Number) {
			val scale = parameters?.get("scale")
			val duration = when (scale) {
				"ms" -> Duration.ofSeconds(data.toLong() / 1_000, (data.toDouble() * 1_000_000 % 1_000_000_000).toLong())
				"μs" -> Duration.ofSeconds(data.toLong() / 1_000_000, (data.toDouble() * 1_000 % 1_000_000_000).toLong())
				"ns" -> Duration.ofSeconds(data.toLong() / 1_000_000_000, data.toLong() % 1_000_000_000)
				else -> Duration.ofSeconds(data.toLong(), (data.toDouble() * 1_000_000_000 % 1_000_000_000).toLong())
			}
			return FixedDuration.values()
					.map { it to it.number(duration) }
					.firstOrNull { it.second >= 1 }
					?.let { "${"%.1f".format(it.second)}${it.first.symbol}" }
					?: "0s"
		}
		return data
	}

}

@Suppress("unused")
private enum class FixedDuration {

	WEEKS {
		override fun number(duration: Duration) = DAYS.number(duration) / 7.0
		override val symbol = "w"
	},
	DAYS {
		override fun number(duration: Duration) = HOURS.number(duration) / 24
		override val symbol = "d"
	},
	HOURS {
		override fun number(duration: Duration) = MINUTES.number(duration) / 60
		override val symbol = "h"
	},
	MINUTES {
		override fun number(duration: Duration) = SECONDS.number(duration) / 60
		override val symbol = "m"
	},
	SECONDS {
		override fun number(duration: Duration) = duration.seconds + duration.nano / 1_000_000_000.0
		override val symbol = "s"
	},
	MILLIS {
		override fun number(duration: Duration) = duration.nano / 1_000_000.0
		override val symbol = "ms"
	},
	MICROS {
		override fun number(duration: Duration) = duration.nano / 1_1000.0
		override val symbol = "μs"
	},
	NANOS {
		override fun number(duration: Duration) = duration.nano.toDouble()
		override val symbol = "ns"
	};

	abstract fun number(duration: Duration): Double
	abstract val symbol: String

}
