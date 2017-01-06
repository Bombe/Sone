package net.pterodactylus.sone.utils

import freenet.support.api.Bucket

class AutoCloseableBucket(val bucket: Bucket) : AutoCloseable {

	override fun close() {
		bucket.free()
	}

}
