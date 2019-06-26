package net.pterodactylus.sone.core

data class PreferenceChangedEvent(val preferenceName: String, val newValue: Any)
