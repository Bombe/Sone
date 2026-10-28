package net.pterodactylus.sone.freenet.wot

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.empty
import org.junit.Test

class NoNegativeIdentityFilterTest {

	@Test
	fun `filter retains all identities with an explicit trust larger than or equal to 0`() {
		val allIdentities = mapOf(
			ownIdentity1.trust(identityA to trustExplicitely(50), identityB to trustExplicitely(0), identityC to trustExplicitely(-50))
		)
		val filteredIdentities = filter.filter(allIdentities)
		assertThat(filteredIdentities[ownIdentity1]!!, containsInAnyOrder(identityA, identityB))
	}

	@Test
	fun `filter retains all identities with an implicit trust larger than or equal to 0`() {
		val allIdentities = mapOf(
			ownIdentity1.trust(identityA to trustImplicitely(50), identityB to trustImplicitely(0), identityC to trustImplicitely(-50))
		)
		val filteredIdentities = filter.filter(allIdentities)
		assertThat(filteredIdentities[ownIdentity1]!!, containsInAnyOrder(identityA, identityB))
	}

	@Test
	fun `filter retains all identities with an explicit trust larger than or equal to 0 from all local identities that know that remote identity`() {
		val allIdentities = mapOf(
			ownIdentity1.trust(identityA to trustExplicitely(50), identityB to trustExplicitely(0)),
			ownIdentity2.trust(identityA to trustExplicitely(50), identityC to trustExplicitely(0))
		)
		val filteredIdentities = filter.filter(allIdentities)
		assertThat(filteredIdentities[ownIdentity1]!!, containsInAnyOrder(identityA, identityB))
		assertThat(filteredIdentities[ownIdentity2]!!, containsInAnyOrder(identityA, identityC))
	}

	@Test
	fun `filter retains all identities with an implicit trust larger than or equal to 0 from all local identities that know that remote identity`() {
		val allIdentities = mapOf(
			ownIdentity1.trust(identityA to trustImplicitely(50), identityB to trustImplicitely(0)),
			ownIdentity2.trust(identityA to trustImplicitely(50), identityC to trustImplicitely(0))
		)
		val filteredIdentities = filter.filter(allIdentities)
		assertThat(filteredIdentities[ownIdentity1]!!, containsInAnyOrder(identityA, identityB))
		assertThat(filteredIdentities[ownIdentity2]!!, containsInAnyOrder(identityA, identityC))
	}

	@Test
	fun `strict filter removes all identities that have an explicit negative value for any of the local identities`() {
		val allIdentities = mapOf(
			ownIdentity1.trust(identityA to trustExplicitely(50), identityB to trustExplicitely(50)),
			ownIdentity2.trust(identityB to trustExplicitely(-50))
		)
		val filteredIdentities = filter.filter(allIdentities)
		assertThat(filteredIdentities[ownIdentity1]!!, containsInAnyOrder(identityA))
		assertThat(filteredIdentities[ownIdentity2]!!, empty())
	}

	@Test
	fun `strict filter removes all identities that have an implicit negative value for any of the local identities`() {
		val allIdentities = mapOf(
			ownIdentity1.trust(identityA to trustExplicitely(50), identityB to trustExplicitely(50)),
			ownIdentity2.trust(identityB to trustImplicitely(-50))
		)
		val filteredIdentities = filter.filter(allIdentities)
		assertThat(filteredIdentities[ownIdentity1]!!, containsInAnyOrder(identityA))
		assertThat(filteredIdentities[ownIdentity2]!!, empty())
	}

	private val filter = NoNegativeIdentityFilter()
	private val ownIdentity1 = createOwnIdentity("1")
	private val ownIdentity2 = createOwnIdentity("2")
	private val identityA = createIdentity("A")
	private val identityB = createIdentity("B")
	private val identityC = createIdentity("C")

}

private fun OwnIdentity.trust(vararg identityTrust: Pair<Identity, Trust>) =
	this to identityTrust.map { (identity, trust) -> identity.copy().setTrust(this, trust) }.toSet()

private fun Identity.copy() = DefaultIdentity(id, nickname, requestUri).apply {
	contexts = this@copy.contexts
	properties = this@copy.properties
}
