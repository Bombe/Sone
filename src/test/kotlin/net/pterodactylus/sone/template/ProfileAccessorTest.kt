package net.pterodactylus.sone.template

import net.pterodactylus.sone.core.Core
import net.pterodactylus.sone.data.Image
import net.pterodactylus.sone.data.Profile
import net.pterodactylus.sone.data.Sone
import net.pterodactylus.sone.data.SoneOptions.DefaultSoneOptions
import net.pterodactylus.sone.data.SoneOptions.LoadExternalContent.ALWAYS
import net.pterodactylus.sone.data.SoneOptions.LoadExternalContent.FOLLOWED
import net.pterodactylus.sone.data.SoneOptions.LoadExternalContent.MANUALLY_TRUSTED
import net.pterodactylus.sone.data.SoneOptions.LoadExternalContent.NEVER
import net.pterodactylus.sone.data.SoneOptions.LoadExternalContent.TRUSTED
import net.pterodactylus.sone.freenet.wot.Identity
import net.pterodactylus.sone.freenet.wot.OwnIdentity
import net.pterodactylus.sone.freenet.wot.Trust
import net.pterodactylus.sone.test.mock
import net.pterodactylus.sone.test.whenever
import net.pterodactylus.util.template.TemplateContext
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.eq

/**
 * Unit test for [ProfileAccessor].
 */
class ProfileAccessorTest {

	private val core = mock<Core>()
	private val accessor = ProfileAccessor(core)
	private val profile = mock<Profile>()
	private val templateContext = mock<TemplateContext>()
	private val currentSone = mock<Sone>()
	private val remoteSone = mock<Sone>()

	@Before
	fun setupTemplateContext() {
		whenever(templateContext.get("currentSone")).thenReturn(currentSone)
	}

	@Before
	fun setupProfile() {
		whenever(profile.sone).thenReturn(remoteSone)
		whenever(profile.avatar).thenReturn("avatar-id")
	}

	@Before
	fun setupSones() {
		val currentIdentity = mock<OwnIdentity>()
		whenever(currentSone.options).thenReturn(DefaultSoneOptions())
		whenever(currentSone.identity).thenReturn(currentIdentity)
		whenever(remoteSone.id).thenReturn("remote-sone")
		val identity = mock<Identity>()
		val trust = Trust(null, null, null)
		whenever(remoteSone.identity).thenReturn(identity)
		whenever(identity.getTrust(currentIdentity)).thenReturn(trust)
	}

	@Before
	fun setupCore() {
		whenever(core.getImage(eq("avatar-id"), anyBoolean())).thenReturn(mock<Image>())
	}


	@Test
	fun `avatar is null if there is no current sone`() {
		whenever(templateContext.get("currentSone")).thenReturn(null)
		assertThat(accessor.get(templateContext, profile, "avatar"), nullValue())
	}

	@Test
	fun `avatar is null if profile has no avatar id`() {
		whenever(profile.avatar).thenReturn(null)
		assertThat(accessor.get(templateContext, profile, "avatar"), nullValue())
	}

	@Test
	fun `avatar is null if core has no image with avatar ID`() {
		whenever(core.getImage(eq("avatar-id"), anyBoolean())).thenReturn(null)
		assertThat(accessor.get(templateContext, profile, "avatar"), nullValue())
	}

	@Test
	fun `avatar ID is returned if profile belongs to local sone`() {
		whenever(remoteSone.isLocal).thenReturn(true)
		assertThat(accessor.get(templateContext, profile, "avatar"), `is`<Any>("avatar-id"))
	}

	@Test
	fun `avatar is null if sone is configure to never show avatars`() {
		currentSone.options.showCustomAvatars = NEVER
		assertThat(accessor.get(templateContext, profile, "avatar"), nullValue())
	}

	@Test
	fun `avatar ID is returned if sone is configure to always show avatars`() {
		currentSone.options.showCustomAvatars = ALWAYS
		assertThat(accessor.get(templateContext, profile, "avatar"), `is`<Any>("avatar-id"))
	}

	@Test
	fun `avatar ID is returned if sone is configure to show avatars of followed sones and remote sone is followed`() {
		currentSone.options.showCustomAvatars = FOLLOWED
		whenever(currentSone.hasFriend("remote-sone")).thenReturn(true)
		assertThat(accessor.get(templateContext, profile, "avatar"), `is`<Any>("avatar-id"))
	}

	@Test
	fun `avatar is null if sone is configure to show avatars of followed sones but remote sone is not followed`() {
		currentSone.options.showCustomAvatars = FOLLOWED
		assertThat(accessor.get(templateContext, profile, "avatar"), nullValue())
	}

	@Test
	fun `avatar is null if sone is configure to show avatars based on trust but there is no trust`() {
		setTrust(null)
		currentSone.options.showCustomAvatars = MANUALLY_TRUSTED
		assertThat(accessor.get(templateContext, profile, "avatar"), nullValue())
	}

	private fun setTrust(trust: Trust?) {
		whenever(remoteSone.identity.getTrust(currentSone.identity as OwnIdentity)).thenReturn(trust)
	}

	@Test
	fun `avatar is null if sone is configure to show avatars based on manual trust but there is no explicit trust`() {
		currentSone.options.showCustomAvatars = MANUALLY_TRUSTED
		assertThat(accessor.get(templateContext, profile, "avatar"), nullValue())
	}

	@Test
	fun `avatar is null if sone is configure to show avatars based on manual trust but explicit trust is zero`() {
		currentSone.options.showCustomAvatars = MANUALLY_TRUSTED
		setTrust(Trust(0, null, null))
		assertThat(accessor.get(templateContext, profile, "avatar"), nullValue())
	}

	@Test
	fun `avatar ID is returned if sone is configure to show avatars based on manual trust and explicit trust is one`() {
		currentSone.options.showCustomAvatars = MANUALLY_TRUSTED
		setTrust(Trust(1, null, null))
		assertThat(accessor.get(templateContext, profile, "avatar"), `is`<Any>("avatar-id"))
	}

	@Test
	fun `avatar is null if sone is configure to show avatars based on trust but explicit trust is zero`() {
		currentSone.options.showCustomAvatars = TRUSTED
		setTrust(Trust(0, null, null))
		assertThat(accessor.get(templateContext, profile, "avatar"), nullValue())
	}

	@Test
	fun `avatar ID is returned if sone is configure to show avatars based on trust and explicit trust is one`() {
		currentSone.options.showCustomAvatars = TRUSTED
		setTrust(Trust(1, null, null))
		assertThat(accessor.get(templateContext, profile, "avatar"), `is`<Any>("avatar-id"))
	}

	@Test
	fun `avatar is null if sone is configure to show avatars based on trust but both explicit and implicit trust are null`() {
		currentSone.options.showCustomAvatars = TRUSTED
		setTrust(Trust(null, null, null))
		assertThat(accessor.get(templateContext, profile, "avatar"), nullValue())
	}

	@Test
	fun `avatar is null if sone is configure to show avatars based on trust but both implicit trust is zero`() {
		currentSone.options.showCustomAvatars = TRUSTED
		setTrust(Trust(null, 0, null))
		assertThat(accessor.get(templateContext, profile, "avatar"), nullValue())
	}

	@Test
	fun `avatar ID is returned if sone is configure to show avatars based on trust and implicit trust is one`() {
		currentSone.options.showCustomAvatars = TRUSTED
		setTrust(Trust(0, 1, null))
		assertThat(accessor.get(templateContext, profile, "avatar"), `is`<Any>("avatar-id"))
	}

	@Test
	fun `accessing other members uses reflection accessor`() {
		assertThat(accessor.get(templateContext, profile, "hashCode"), `is`<Any>(profile.hashCode()))
	}

}
