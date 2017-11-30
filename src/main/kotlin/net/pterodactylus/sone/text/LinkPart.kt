package net.pterodactylus.sone.text

/**
 * {@link Part} implementation that can hold a link. A link contains of three
 * attributes: the link itself, the text that is shown instead of the link, and
 * an explanatory text that can be displayed e.g. as a tooltip.
 */
data class LinkPart @JvmOverloads constructor(val link: String, override val text: String, val title: String = link) : Part
