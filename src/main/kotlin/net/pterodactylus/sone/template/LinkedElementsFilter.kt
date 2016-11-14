package net.pterodactylus.sone.template

import net.pterodactylus.sone.core.ElementLoader
import net.pterodactylus.sone.core.LinkedElement
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.data.SoneOptions.LoadExternalContent.ALWAYS
import net.pterodactylus.sone.data.SoneOptions.LoadExternalContent.FOLLOWED
import net.pterodactylus.sone.data.SoneOptions.LoadExternalContent.MANUALLY_TRUSTED
import net.pterodactylus.sone.data.SoneOptions.LoadExternalContent.NEVER
import net.pterodactylus.sone.data.SoneOptions.LoadExternalContent.TRUSTED
import net.pterodactylus.sone.freenet.wot.OwnIdentity
import net.pterodactylus.sone.text.FreenetLinkPart
import net.pterodactylus.sone.text.Part
import net.pterodactylus.util.template.Filter
import net.pterodactylus.util.template.TemplateContext

/**
 * Filter that takes a number of pre-rendered [Part]s and replaces all identified links to freenet elements
 * with [LinkedElement]s.
 */
class LinkedElementsFilter(private val elementLoader: ElementLoader) : Filter {

	@Suppress("UNCHECKED_CAST")
	override fun format(templateContext: TemplateContext?, data: Any?, parameters: MutableMap<String, Any?>?) =
			if (showLinkedImages(templateContext?.get("currentSone") as Sone?, parameters?.get("sone") as Sone?)) {
				(data as? Iterable<Part>)
						?.filterIsInstance<FreenetLinkPart>()
						?.map { elementLoader.loadElement(it.link) }
						?.filter { !it.failed }
						?: listOf<LinkedElement>()
			} else {
				listOf<LinkedElement>()
			}

	private fun showLinkedImages(currentSone: Sone?, sone: Sone?): Boolean {
		return (currentSone != null) && (sone != null) && ((currentSone == sone) || currentSoneAllowsImagesFromSone(currentSone, sone))
	}

	private fun currentSoneAllowsImagesFromSone(currentSone: Sone, externalSone: Sone) =
			when (currentSone.options.loadLinkedImages) {
				NEVER -> false
				MANUALLY_TRUSTED -> externalSone.isLocal || currentSone.explicitelyTrusts(externalSone)
				FOLLOWED -> externalSone.isLocal || currentSone.hasFriend(externalSone.id)
				TRUSTED -> externalSone.isLocal || currentSone.implicitelyTrusts(externalSone)
				ALWAYS -> true
			}

	private fun Sone.implicitelyTrusts(other: Sone): Boolean {
		val explicitTrust = other.identity.getTrust(this.identity as OwnIdentity)?.explicit
		val implicitTrust = other.identity.getTrust(this.identity as OwnIdentity)?.implicit
		return ((explicitTrust != null) && (explicitTrust > 0)) || ((explicitTrust == null) && (implicitTrust != null) && (implicitTrust > 0))
	}

	private fun Sone.explicitelyTrusts(other: Sone) =
			other.identity.getTrust(this.identity as OwnIdentity)?.explicit ?: -1 > 0

}
