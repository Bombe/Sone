package net.pterodactylus.sone.utils

import freenet.support.api.Bucket

fun <R> Bucket.use(block: (Bucket) -> R): R = try {
	block(this)
} finally {
	free()
}
