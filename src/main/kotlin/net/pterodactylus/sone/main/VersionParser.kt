package net.pterodactylus.sone.main

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory

fun parseVersion(file: String = "/version.yaml"): Version? =
		Version::class.java.getResourceAsStream(file)?.use {
			objectMapper.readValue(it, Version::class.java)
		}

private val objectMapper = ObjectMapper(YAMLFactory())

@NoArg
data class Version(val id: String, val nice: String)
