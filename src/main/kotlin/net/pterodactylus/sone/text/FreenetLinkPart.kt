package net.pterodactylus.sone.text

/**
 * [LinkPart] implementation that stores an additional attribute: if the
 * link is an SSK or USK link and the post was created by an identity that owns
 * the keyspace in question.
 */
data class FreenetLinkPart(val link: String, override val text: String, val title: String, val trusted: Boolean) : Part {

	constructor(link: String, text: String, trusted: Boolean) : this(link, text, link, trusted)

}
