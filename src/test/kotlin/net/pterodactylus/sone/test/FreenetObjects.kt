package net.pterodactylus.sone.test

import freenet.client.HighLevelSimpleClient
import net.pterodactylus.sone.freenet.HighLevelSimpleClientCreator

val dummyHighLevelSimpleClient = deepMock<HighLevelSimpleClient>()

val dummyHighLevelSimpleClientCreator: HighLevelSimpleClientCreator = HighLevelSimpleClientCreator { _, _, _ -> dummyHighLevelSimpleClient }
