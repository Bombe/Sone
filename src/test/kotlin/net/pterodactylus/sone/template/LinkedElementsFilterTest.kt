package net.pterodactylus.sone.template

import net.pterodactylus.sone.core.ElementLoader
import net.pterodactylus.sone.core.LinkedElement
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.data.SoneOptions.DefaultSoneOptions
import net.pterodactylus.sone.data.SoneOptions.LoadExternalContent.ALWAYS
import net.pterodactylus.sone.data.SoneOptions.LoadExternalContent.FOLLOWED
import net.pterodactylus.sone.data.SoneOptions.LoadExternalContent.MANUALLY_TRUSTED
import net.pterodactylus.sone.data.SoneOptions.LoadExternalContent.TRUSTED
import net.pterodactylus.sone.freenet.wot.OwnIdentity
import net.pterodactylus.sone.freenet.wot.Trust
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.text.FreenetLinkPart
import net.pterodactylus.sone.text.LinkPart
import net.pterodactylus.sone.text.Part
import net.pterodactylus.sone.text.PlainTextPart
import net.pterodactylus.util.template.TemplateContext
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.emptyIterable
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`

/**
 * Unit test for [LinkedElementsFilter].
 */
class LinkedElementsFilterTest {

	private val imageLoader = mock<ElementLoader>()
	private val filter = LinkedElementsFilter(imageLoader)
	private val templateContext = TemplateContext()
	private val parameters = mutableMapOf<String, Any?>()
	private val sone = createSone()
	private val remoteSone = createSone("remote-id")
	private val parts: List<Part> = listOf(
			PlainTextPart("text"),
			LinkPart("http://link", "link"),
			FreenetLinkPart("KSK@link", "link", false),
			FreenetLinkPart("KSK@loading.png", "link", false),
			FreenetLinkPart("KSK@link.png", "link", false)
	)

	@Before
	fun setupSone() {
		`when`(sone.options).thenReturn(DefaultSoneOptions())
	}

	@Before
	fun setupImageLoader() {
		`when`(imageLoader.loadElement("KSK@link")).thenReturn(LinkedElement("KSK@link", failed = true))
		`when`(imageLoader.loadElement("KSK@loading.png")).thenReturn(LinkedElement("KSK@loading.png", loading = true))
		`when`(imageLoader.loadElement("KSK@link.png")).thenReturn(LinkedElement("KSK@link.png"))
	}

	@Test
	fun `filter does not find any image if there is no template context`() {
		assertThat(filter.format(null, parts, parameters), emptyIterable())
	}

	@Test
	fun `filter does not find any image if there is no current sone`() {
		verifyThatImagesAreNotPresent()
	}

	@Test
	fun `filter does not find any images if there is no remote sone`() {
		sone.options.loadLinkedImages = ALWAYS
		templateContext.set("currentSone", sone)
		verifyThatImagesAreNotPresent()
	}

	@Test
	fun `filter does not find any images if sone does not allow to load images`() {
		templateContext.set("currentSone", sone)
		parameters["sone"] = remoteSone
		verifyThatImagesAreNotPresent()
	}

	@Test
	fun `filter finds all loaded freenet images from the sone itself`() {
		templateContext.set("currentSone", sone)
		parameters["sone"] = sone
		verifyThatImagesArePresent()
	}

	@Test
	fun `filter finds images if the remote sone is local`() {
		sone.options.loadLinkedImages = MANUALLY_TRUSTED
		templateContext.set("currentSone", sone)
		`when`(remoteSone.isLocal).thenReturn(true)
		parameters["sone"] = remoteSone
		verifyThatImagesArePresent()
	}

	@Test
	fun `filter does not find images if local sone requires manual trust and remote sone has not trust`() {
		sone.options.loadLinkedImages = MANUALLY_TRUSTED
		templateContext.set("currentSone", sone)
		parameters["sone"] = remoteSone
		verifyThatImagesAreNotPresent()
	}

	@Test
	fun `filter does not find images if local sone requires manual trust and remote sone has only implicit trust`() {
		sone.options.loadLinkedImages = MANUALLY_TRUSTED
		templateContext.set("currentSone", sone)
		`when`(remoteSone.identity.getTrust(this.sone.identity as OwnIdentity)).thenReturn(Trust(null, 100, null))
		parameters["sone"] = remoteSone
		verifyThatImagesAreNotPresent()
	}

	@Test
	fun `filter does not find images if local sone requires manual trust and remote sone has explicit trust of zero`() {
		sone.options.loadLinkedImages = MANUALLY_TRUSTED
		templateContext.set("currentSone", sone)
		`when`(remoteSone.identity.getTrust(this.sone.identity as OwnIdentity)).thenReturn(Trust(0, null, null))
		parameters["sone"] = remoteSone
		verifyThatImagesAreNotPresent()
	}

	@Test
	fun `filter finds images if local sone requires manual trust and remote sone has explicit trust of one`() {
		sone.options.loadLinkedImages = MANUALLY_TRUSTED
		templateContext.set("currentSone", sone)
		`when`(remoteSone.identity.getTrust(this.sone.identity as OwnIdentity)).thenReturn(Trust(1, null, null))
		parameters["sone"] = remoteSone
		verifyThatImagesArePresent()
	}

	@Test
	fun `filter does not find images if local sone requires following and remote sone is not followed`() {
	    sone.options.loadLinkedImages = FOLLOWED
		templateContext["currentSone"] = sone
		parameters["sone"] = remoteSone
		verifyThatImagesAreNotPresent()
	}

	@Test
	fun `filter finds images if local sone requires following and remote sone is followed`() {
	    sone.options.loadLinkedImages = FOLLOWED
		`when`(sone.hasFriend("remote-id")).thenReturn(true)
		templateContext["currentSone"] = sone
		parameters["sone"] = remoteSone
		verifyThatImagesArePresent()
	}

	@Test
	fun `filter finds images if local sone requires following and remote sone is the same as the local sone`() {
	    sone.options.loadLinkedImages = FOLLOWED
		templateContext["currentSone"] = sone
		parameters["sone"] = sone
		verifyThatImagesArePresent()
	}

	@Test
	fun `filter finds images if following is required and remote sone is a local sone`() {
		sone.options.loadLinkedImages = FOLLOWED
		templateContext["currentSone"] = sone
		`when`(remoteSone.isLocal).thenReturn(true)
		parameters["sone"] = remoteSone
		verifyThatImagesArePresent()
	}

	@Test
	fun `filter does not find images if any trust is required and remote sone does not have any trust`() {
	    sone.options.loadLinkedImages = TRUSTED
		templateContext["currentSone"] = sone
		parameters["sone"] = remoteSone
		verifyThatImagesAreNotPresent()
	}

	@Test
	fun `filter does not find images if any trust is required and remote sone has implicit trust of zero`() {
	    sone.options.loadLinkedImages = TRUSTED
		templateContext["currentSone"] = sone
		`when`(remoteSone.identity.getTrust(sone.identity as OwnIdentity)).thenReturn(Trust(null, 0, null))
		parameters["sone"] = remoteSone
		verifyThatImagesAreNotPresent()
	}

	@Test
	fun `filter finds images if any trust is required and remote sone has implicit trust of one`() {
	    sone.options.loadLinkedImages = TRUSTED
		templateContext["currentSone"] = sone
		`when`(remoteSone.identity.getTrust(sone.identity as OwnIdentity)).thenReturn(Trust(null, 1, null))
		parameters["sone"] = remoteSone
		verifyThatImagesArePresent()
	}

	@Test
	fun `filter does not find images if any trust is required and remote sone has explicit trust of zero but implicit trust of one`() {
		sone.options.loadLinkedImages = TRUSTED
		templateContext["currentSone"] = sone
		`when`(remoteSone.identity.getTrust(sone.identity as OwnIdentity)).thenReturn(Trust(0, 1, null))
		parameters["sone"] = remoteSone
		verifyThatImagesAreNotPresent()
	}

	@Test
	fun `filter finds images if any trust is required and remote sone has explicit trust of one but no implicit trust`() {
		sone.options.loadLinkedImages = TRUSTED
		templateContext["currentSone"] = sone
		`when`(remoteSone.identity.getTrust(sone.identity as OwnIdentity)).thenReturn(Trust(1, null, null))
		parameters["sone"] = remoteSone
		verifyThatImagesArePresent()
	}

	@Test
	fun `filter finds images if any trust is required and remote sone is a local sone`() {
		sone.options.loadLinkedImages = TRUSTED
		templateContext["currentSone"] = sone
		`when`(remoteSone.isLocal).thenReturn(true)
		parameters["sone"] = remoteSone
		verifyThatImagesArePresent()
	}

	@Test
	fun `filter finds images if no trust is required`() {
	    sone.options.loadLinkedImages = ALWAYS
		templateContext["currentSone"] = sone
		parameters["sone"] = remoteSone
		verifyThatImagesArePresent()
	}

	private fun verifyThatImagesArePresent() {
		val loadedImages = filter.format(templateContext, parts, parameters)
		assertThat(loadedImages, contains<LinkedElement>(
				LinkedElement("KSK@loading.png", failed = false, loading = true),
				LinkedElement("KSK@link.png", failed = false, loading = false)
		))
	}

	private fun verifyThatImagesAreNotPresent() {
		assertThat(filter.format(templateContext, parts, parameters), emptyIterable())
	}

	private fun createSone(id: String = "sone-id"): Sone {
		val sone = mock<Sone>()
		`when`(sone.id).thenReturn(id)
		`when`(sone.options).thenReturn(DefaultSoneOptions())
		`when`(sone.identity).thenReturn(mock<OwnIdentity>())
		return sone
	}

}
