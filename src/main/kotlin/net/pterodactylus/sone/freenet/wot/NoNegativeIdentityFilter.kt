package net.pterodactylus.sone.freenet.wot

/**
 * Filter for identities that retains only those remote identities that do
 * not have a single negative trust value (explicit or implicit) from any own
 * identity.
 */
class NoNegativeIdentityFilter {

	fun filter(identities: Map<OwnIdentity, Set<Identity>>) =
		identities.run {
			val identitiesWithTrust = values.flatten()
				.groupBy { it.id }
				.mapValues { (_, identities) ->
					identities.reduce { accIdentity, identity ->
						identity.trust.forEach { (ownIdentity: OwnIdentity?, trust: Trust?) ->
							accIdentity.setTrust(ownIdentity, trust)
						}
						accIdentity
					}
				}

			mapValues { (_, trustedIdentities) ->
				trustedIdentities.filter { trustedIdentity ->
					identitiesWithTrust[trustedIdentity.id]!!.trust.all { it.value.hasZeroOrPositiveTrust() }
				}
			}
		}

}

private fun Trust.hasZeroOrPositiveTrust() =
		if (explicit == null) {
			implicit == null || implicit >= 0
		} else {
			explicit >= 0
		}
