package net.pterodactylus.sone.web.ajax

/**
 * [JsonReturnObject] that signals an error has occured.
 */
data class JsonErrorReturnObject(val error: String) : JsonReturnObject(false)
