package net.pterodactylus.sone.core

import com.google.common.eventbus.*
import freenet.client.*
import freenet.client.FetchException.FetchExceptionMode.*
import freenet.client.InsertException.*
import freenet.client.async.*
import freenet.crypt.*
import freenet.keys.*
import freenet.keys.InsertableClientSSK.*
import freenet.node.*
import freenet.node.RequestStarter.*
import freenet.support.api.*
import freenet.support.io.*
import net.pterodactylus.sone.core.FreenetInterface.*
import net.pterodactylus.sone.core.event.*
import net.pterodactylus.sone.data.*
import net.pterodactylus.sone.data.impl.*
import net.pterodactylus.sone.freenet.wot.DefaultIdentity
import net.pterodactylus.sone.test.*
import net.pterodactylus.sone.test.Matchers.*
import net.pterodactylus.sone.test.TestUtil.*
import net.pterodactylus.sone.utils.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.*
import org.junit.rules.*
import org.mockito.*
import org.mockito.ArgumentCaptor.*
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.*
import java.io.*
import java.util.*
import kotlin.test.Test

/**
 * Unit test for [FreenetInterface].
 */
class FreenetInterfaceTest {

	@Rule
	@JvmField
	val expectionException: ExpectedException = ExpectedException.none()

	@Rule
	@JvmField
	val silencedLogging = silencedLogging()

	@Suppress("UnstableApiUsage")
	private val eventBus = mock<EventBus>()
	private val node = mock<Node>()
	private val nodeClientCore = mock<NodeClientCore>()
	private val highLevelSimpleClient: HighLevelSimpleClient = mock(HighLevelSimpleClient::class.java, withSettings().extraInterfaces(RequestClient::class.java))
	private val randomSource = DummyRandomSource()
	private val uskManager = mock<USKManager>()
	private val sone = mock<Sone>()
	private val callbackCaptor: ArgumentCaptor<USKCallback> = forClass(USKCallback::class.java)
	private val image: Image = ImageImpl()
	private val insertToken: InsertToken
	private val bucket = mock<Bucket>()
	private val clientGetCallback: ArgumentCaptor<ClientGetCallback> = forClass(ClientGetCallback::class.java)
	private val uri = FreenetURI("KSK@pgl.png")
	private val fetchResult = mock<FetchResult>()
	private val backgroundFetchCallback = mock<BackgroundFetchCallback>()
	private val clientGetter = mock<ClientGetter>()
	private val soneUriCreator = SoneUriCreator()
	private val freenetInterface: FreenetInterface

	init {
		whenever(nodeClientCore.makeClient(anyShort(), anyBoolean(), anyBoolean())).thenReturn(highLevelSimpleClient)
		setField(node, "clientCore", nodeClientCore)
		setField(node, "random", randomSource)
		setField(nodeClientCore, "uskManager", uskManager)
		setField(nodeClientCore, "clientContext", mock<ClientContext>())
		freenetInterface = FreenetInterface(eventBus, node, soneUriCreator)
		insertToken = freenetInterface.InsertToken(image)
		insertToken.setBucket(bucket)
	}

	@Before
	fun setupHighLevelSimpleClient() {
		whenever(highLevelSimpleClient.fetchContext).thenReturn(mock())
		whenever(highLevelSimpleClient.fetch(eq(uri), anyLong(), any(ClientGetCallback::class.java), any(FetchContext::class.java), anyShort())).thenReturn(clientGetter)
	}

	@Before
	fun setupSone() {
		val insertSsk = createRandom(randomSource, "test-0")
		whenever(sone.id).thenReturn(insertSsk.uri.routingKey.asFreenetBase64)
		whenever(sone.identity).thenReturn(DefaultIdentity("id", "name", insertSsk.uri.toString()))
	}

	@Before
	fun setupCallbackCaptorAndUskManager() {
		doNothing().whenever(uskManager).subscribe(any(USK::class.java), callbackCaptor.capture(), anyBoolean(), any(RequestClient::class.java))
	}

	@Test
	fun `can fetch uri`() {
		val freenetUri = FreenetURI("KSK@GPLv3.txt")
		val fetchResult = createFetchResult()
		whenever(highLevelSimpleClient.fetch(freenetUri)).thenReturn(fetchResult)
		val fetched = freenetInterface.fetchUri(freenetUri)
		assertThat(fetched, notNullValue())
		assertThat(fetched!!.fetchResult, equalTo(fetchResult))
		assertThat(fetched.freenetUri, equalTo(freenetUri))
	}

	@Test
	fun `fetch follows redirect`() {
		val freenetUri = FreenetURI("KSK@GPLv2.txt")
		val newFreenetUri = FreenetURI("KSK@GPLv3.txt")
		val fetchResult = createFetchResult()
		val fetchException = FetchException(PERMANENT_REDIRECT, newFreenetUri)
		whenever(highLevelSimpleClient.fetch(freenetUri)).thenThrow(fetchException)
		whenever(highLevelSimpleClient.fetch(newFreenetUri)).thenReturn(fetchResult)
		val fetched = freenetInterface.fetchUri(freenetUri)
		assertThat(fetched!!.fetchResult, equalTo(fetchResult))
		assertThat(fetched.freenetUri, equalTo(newFreenetUri))
	}

	@Test
	fun `fetch returns null on fetch exceptions`() {
		val freenetUri = FreenetURI("KSK@GPLv2.txt")
		val fetchException = FetchException(ALL_DATA_NOT_FOUND)
		whenever(highLevelSimpleClient.fetch(freenetUri)).thenThrow(fetchException)
		val fetched = freenetInterface.fetchUri(freenetUri)
		assertThat(fetched, nullValue())
	}

	private fun createFetchResult(): FetchResult {
		val clientMetadata = ClientMetadata("text/plain")
		val bucket = ArrayBucket("Some Data.".toByteArray())
		return FetchResult(clientMetadata, bucket)
	}

	@Test
	fun `inserting an image`() {
		val temporaryImage = TemporaryImage("image-id")
		temporaryImage.mimeType = "image/png"
		val imageData = byteArrayOf(1, 2, 3, 4)
		temporaryImage.imageData = imageData
		val image = ImageImpl("image-id")
		val insertToken = freenetInterface.InsertToken(image)
		val insertContext = mock<InsertContext>()
		whenever(highLevelSimpleClient.getInsertContext(anyBoolean())).thenReturn(insertContext)
		val clientPutter = mock<ClientPutter>()
		val insertBlockCaptor = forClass(InsertBlock::class.java)
		whenever(highLevelSimpleClient.insert(insertBlockCaptor.capture(), eq(null as String?), eq(false), eq(insertContext), eq(insertToken), anyShort())).thenReturn(clientPutter)
		freenetInterface.insertImage(temporaryImage, image, insertToken)
		assertThat(insertBlockCaptor.value.data.inputStream, delivers(byteArrayOf(1, 2, 3, 4)))
		assertThat(getPrivateField(insertToken, "clientPutter"), equalTo(clientPutter))
		verify(eventBus).post(any(ImageInsertStartedEvent::class.java))
	}

	@Test
	fun `insert exception causes a sone exception`() {
		val temporaryImage = TemporaryImage("image-id")
		temporaryImage.mimeType = "image/png"
		val imageData = byteArrayOf(1, 2, 3, 4)
		temporaryImage.imageData = imageData
		val image = ImageImpl("image-id")
		val insertToken = freenetInterface.InsertToken(image)
		val insertContext = mock<InsertContext>()
		whenever(highLevelSimpleClient.getInsertContext(anyBoolean())).thenReturn(insertContext)
		val insertBlockCaptor = forClass(InsertBlock::class.java)
		whenever(highLevelSimpleClient.insert(insertBlockCaptor.capture(), eq(null as String?), eq(false), eq(insertContext), eq(insertToken), anyShort())).thenThrow(InsertException::class.java)
		expectionException.expect(SoneInsertException::class.java)
		freenetInterface.insertImage(temporaryImage, image, insertToken)
	}

	@Test
	fun `inserting a directory`() {
		val freenetUri = mock<FreenetURI>()
		val manifestEntries = HashMap<String, Any>()
		val defaultFile = "index.html"
		val resultingUri = mock<FreenetURI>()
		whenever(highLevelSimpleClient.insertManifest(eq(freenetUri), eq(manifestEntries), eq(defaultFile))).thenReturn(resultingUri)
		assertThat(freenetInterface.insertDirectory(freenetUri, manifestEntries, defaultFile), equalTo(resultingUri))
	}

	@Test
	fun `insert exception is forwarded as sone exception`() {
		whenever(highLevelSimpleClient.insertManifest(any(), any(), any())).thenThrow(InsertException::class.java)
		expectionException.expect(SoneException::class.java)
		freenetInterface.insertDirectory(null, null, null)
	}

	@Test
	fun `sone with wrong request uri will not be subscribed`() {
		freenetInterface.registerUsk(FreenetURI("KSK@GPLv3.txt"), null)
		verify(uskManager, never()).subscribe(any(USK::class.java), any(USKCallback::class.java), anyBoolean(), any(RequestClient::class.java))
	}

	@Test
	fun `registering a usk`() {
		val freenetUri = createRandom(randomSource, "test-0").uri.uskForSSK()
		val callback = mock<Callback>()
		freenetInterface.registerUsk(freenetUri, callback)
		verify(uskManager).subscribe(any(USK::class.java), any(USKCallback::class.java), anyBoolean(), any(RequestClient::class.java))
	}

	@Test
	fun `registering a non-usk key will not be subscribed`() {
		val freenetUri = FreenetURI("KSK@GPLv3.txt")
		val callback = mock<Callback>()
		freenetInterface.registerUsk(freenetUri, callback)
		verify(uskManager, never()).subscribe(any(USK::class.java), any(USKCallback::class.java), anyBoolean(), any(RequestClient::class.java))
	}

	@Test
	fun `registering an active usk will subscribe to it correctly`() {
		val freenetUri = createRandom(randomSource, "test-0").uri.uskForSSK()
		val uskCallback = mock<USKCallback>()
		freenetInterface.registerActiveUsk(freenetUri, uskCallback)
		verify(uskManager).subscribe(any(USK::class.java), eq(uskCallback), eq(true), any(RequestClient::class.java))
	}

	@Test
	fun `registering an inactive usk will subscribe to it correctly`() {
		val freenetUri = createRandom(randomSource, "test-0").uri.uskForSSK()
		val uskCallback = mock<USKCallback>()
		freenetInterface.registerPassiveUsk(freenetUri, uskCallback)
		verify(uskManager).subscribe(any(USK::class.java), eq(uskCallback), eq(false), any(RequestClient::class.java))
	}

	@Test
	fun `registering an active non-usk will not subscribe to a usk`() {
		val freenetUri = createRandom(randomSource, "test-0").uri
		freenetInterface.registerActiveUsk(freenetUri, null)
		verify(uskManager, never()).subscribe(any(USK::class.java), any(USKCallback::class.java), anyBoolean(), any(RequestClient::class.java))
	}

	@Test
	fun `registering an inactive non-usk will not subscribe to a usk`() {
		val freenetUri = createRandom(randomSource, "test-0").uri
		freenetInterface.registerPassiveUsk(freenetUri, null)
		verify(uskManager, never()).subscribe(any(USK::class.java), any(USKCallback::class.java), anyBoolean(), any(RequestClient::class.java))
	}

	@Test
	fun `unregistering a not registered usk does nothing`() {
		val freenetURI = createRandom(randomSource, "test-0").uri.uskForSSK()
		freenetInterface.unregisterUsk(freenetURI)
		verify(uskManager, never()).unsubscribe(any(USK::class.java), any(USKCallback::class.java))
	}

	@Test
	fun `unregistering a registered usk`() {
		val freenetURI = createRandom(randomSource, "test-0").uri.uskForSSK()
		val callback = mock<Callback>()
		freenetInterface.registerUsk(freenetURI, callback)
		freenetInterface.unregisterUsk(freenetURI)
		verify(uskManager).unsubscribe(any(USK::class.java), any(USKCallback::class.java))
	}

	@Test
	fun `unregistering a not registered sone does nothing`() {
		freenetInterface.unregisterUsk(sone)
		verify(uskManager, never()).unsubscribe(any(USK::class.java), any(USKCallback::class.java))
	}

	@Test
	fun `unregistering a registered sone unregisters the sone`() {
		freenetInterface.registerActiveUsk(soneUriCreator.getRequestUri(sone), mock())
		freenetInterface.unregisterUsk(sone)
		verify(uskManager).unsubscribe(any(USK::class.java), any(USKCallback::class.java))
	}

	@Test
	fun `unregistering a sone with a wrong request key will not unsubscribe`() {
		freenetInterface.registerUsk(FreenetURI("KSK@GPLv3.txt"), null)
		freenetInterface.unregisterUsk(sone)
		verify(uskManager, never()).unsubscribe(any(USK::class.java), any(USKCallback::class.java))
	}

	@Test
	fun `callback for normal usk uses different priorities`() {
		val callback = mock<Callback>()
		val soneUri = createRandom(randomSource, "test-0").uri.uskForSSK()
		freenetInterface.registerUsk(soneUri, callback)
		assertThat(callbackCaptor.value.pollingPriorityNormal, equalTo(PREFETCH_PRIORITY_CLASS))
		assertThat(callbackCaptor.value.pollingPriorityProgress, equalTo(INTERACTIVE_PRIORITY_CLASS))
	}

	@Test
	fun `callback for normal usk forwards important parameters`() {
		val callback = mock<Callback>()
		val uri = createRandom(randomSource, "test-0").uri.uskForSSK()
		freenetInterface.registerUsk(uri, callback)
		val key = mock<USK>()
		whenever(key.uri).thenReturn(uri)
		callbackCaptor.value.onFoundEdition(3, key, null, false, 0.toShort(), null, true, true)
		verify(callback).editionFound(eq(uri), eq(3L), eq(true), eq(true))
	}

	@Test
	fun `fetched retains uri and fetch result`() {
		val freenetUri = mock<FreenetURI>()
		val fetchResult = mock<FetchResult>()
		val (freenetUri1, fetchResult1) = Fetched(freenetUri, fetchResult)
		assertThat(freenetUri1, equalTo(freenetUri))
		assertThat(fetchResult1, equalTo(fetchResult))
	}

	@Test
	fun `cancelling an insert will fire image insert aborted event`() {
		val clientPutter = mock<ClientPutter>()
		insertToken.setClientPutter(clientPutter)
		val imageInsertStartedEvent = forClass(ImageInsertStartedEvent::class.java)
		verify(eventBus).post(imageInsertStartedEvent.capture())
		assertThat(imageInsertStartedEvent.value.image, equalTo(image))
		insertToken.cancel()
		val imageInsertAbortedEvent = forClass(ImageInsertAbortedEvent::class.java)
		verify(eventBus, times(2)).post(imageInsertAbortedEvent.capture())
		verify(bucket).free()
		assertThat(imageInsertAbortedEvent.value.image, equalTo(image))
	}

	@Test
	fun `failure without exception sends failed event`() {
		val insertException = InsertException(mock<InsertException>())
		insertToken.onFailure(insertException, null)
		val imageInsertFailedEvent = forClass(ImageInsertFailedEvent::class.java)
		verify(eventBus).post(imageInsertFailedEvent.capture())
		verify(bucket).free()
		assertThat(imageInsertFailedEvent.value.image, equalTo(image))
		assertThat(imageInsertFailedEvent.value.cause, equalTo<Throwable>(insertException))
	}

	@Test
	fun `failure sends failed event with exception`() {
		val insertException = InsertException(InsertExceptionMode.INTERNAL_ERROR, "Internal error", null)
		insertToken.onFailure(insertException, null)
		val imageInsertFailedEvent = forClass(ImageInsertFailedEvent::class.java)
		verify(eventBus).post(imageInsertFailedEvent.capture())
		verify(bucket).free()
		assertThat(imageInsertFailedEvent.value.image, equalTo(image))
		assertThat(imageInsertFailedEvent.value.cause, equalTo(insertException as Throwable))
	}

	@Test
	fun `failure because cancelled by user sends aborted event`() {
		val insertException = InsertException(InsertExceptionMode.CANCELLED, null)
		insertToken.onFailure(insertException, null)
		val imageInsertAbortedEvent = forClass(ImageInsertAbortedEvent::class.java)
		verify(eventBus).post(imageInsertAbortedEvent.capture())
		verify(bucket).free()
		assertThat(imageInsertAbortedEvent.value.image, equalTo(image))
	}

	@Test
	fun `ignored methods do not throw exceptions`() {
		insertToken.onResume(null)
		insertToken.onFetchable(null)
		insertToken.onGeneratedMetadata(null, null)
	}

	@Test
	fun `generated uri is posted on success`() {
		val generatedUri = mock<FreenetURI>()
		insertToken.onGeneratedURI(generatedUri, null)
		insertToken.onSuccess(null)
		val imageInsertFinishedEvent = forClass(ImageInsertFinishedEvent::class.java)
		verify(eventBus).post(imageInsertFinishedEvent.capture())
		verify(bucket).free()
		assertThat(imageInsertFinishedEvent.value.image, equalTo(image))
		assertThat(imageInsertFinishedEvent.value.resultingUri, equalTo(generatedUri))
	}

	@Test
	fun `insert token supplier supplies insert tokens`() {
		val insertTokenSupplier = InsertTokenSupplier(freenetInterface)
		assertThat(insertTokenSupplier.apply(image), notNullValue())
	}

	@Test
	fun `background fetch can be started`() {
		freenetInterface.startFetch(uri, backgroundFetchCallback)
		verify(highLevelSimpleClient).fetch(eq(uri), anyLong(), any(ClientGetCallback::class.java), any(FetchContext::class.java), anyShort())
	}

	@Test
	fun `background fetch registers snoop and restarts the request`() {
		freenetInterface.startFetch(uri, backgroundFetchCallback)
		verify(clientGetter).metaSnoop = any(SnoopMetadata::class.java)
		verify(clientGetter).restart(eq(uri), anyBoolean(), any(ClientContext::class.java))
	}

	@Test
	fun `request is not cancelled for image mime type`() {
		verifySnoopCancelsRequestForMimeType("image/png", false)
		verify(backgroundFetchCallback, never()).failed(uri)
	}

	@Test
	fun `request is cancelled for null mime type`() {
		verifySnoopCancelsRequestForMimeType(null, true)
		verify(backgroundFetchCallback, never()).shouldCancel(eq(uri), any(), anyLong())
		verify(backgroundFetchCallback).failed(uri)
	}

	@Test
	fun `request is cancelled for video mime type`() {
		verifySnoopCancelsRequestForMimeType("video/mkv", true)
		verify(backgroundFetchCallback).failed(uri)
	}

	@Test
	fun `request is cancelled for audio mime type`() {
		verifySnoopCancelsRequestForMimeType("audio/mpeg", true)
		verify(backgroundFetchCallback).failed(uri)
	}

	@Test
	fun `request is cancelled for text mime type`() {
		verifySnoopCancelsRequestForMimeType("text/plain", true)
		verify(backgroundFetchCallback).failed(uri)
	}

	private fun verifySnoopCancelsRequestForMimeType(mimeType: String?, cancel: Boolean) {
		whenever(backgroundFetchCallback.shouldCancel(eq(uri), if (mimeType != null) eq(mimeType) else isNull(), anyLong())).thenReturn(cancel)
		freenetInterface.startFetch(uri, backgroundFetchCallback)
		val snoopMetadata = forClass(SnoopMetadata::class.java)
		verify(clientGetter).metaSnoop = snoopMetadata.capture()
		val metadata = mock<Metadata>()
		whenever(metadata.mimeType).thenReturn(mimeType)
		assertThat(snoopMetadata.value.snoopMetadata(metadata, mock()), equalTo(cancel))
	}

	@Test
	fun `callback of background fetch is notified on success`() {
		freenetInterface.startFetch(uri, backgroundFetchCallback)
		verify(highLevelSimpleClient).fetch(eq(uri), anyLong(), clientGetCallback.capture(), any(FetchContext::class.java), anyShort())
		whenever(fetchResult.mimeType).thenReturn("image/png")
		whenever(fetchResult.asByteArray()).thenReturn(byteArrayOf(1, 2, 3, 4, 5))
		clientGetCallback.value.onSuccess(fetchResult, mock())
		verify(backgroundFetchCallback).loaded(uri, "image/png", byteArrayOf(1, 2, 3, 4, 5))
		verifyNoMoreInteractions(backgroundFetchCallback)
	}

	@Test
	fun `callback of background fetch is notified on failure`() {
		freenetInterface.startFetch(uri, backgroundFetchCallback)
		verify(highLevelSimpleClient).fetch(eq(uri), anyLong(), clientGetCallback.capture(), any(FetchContext::class.java), anyShort())
		whenever(fetchResult.mimeType).thenReturn("image/png")
		whenever(fetchResult.asByteArray()).thenReturn(byteArrayOf(1, 2, 3, 4, 5))
		clientGetCallback.value.onFailure(FetchException(ALL_DATA_NOT_FOUND), mock())
		verify(backgroundFetchCallback).failed(uri)
		verifyNoMoreInteractions(backgroundFetchCallback)
	}

	@Test
	fun `callback of background fetch is notified as failure if bucket can not be loaded`() {
		freenetInterface.startFetch(uri, backgroundFetchCallback)
		verify(highLevelSimpleClient).fetch(eq(uri), anyLong(), clientGetCallback.capture(), any(FetchContext::class.java), anyShort())
		whenever(fetchResult.mimeType).thenReturn("image/png")
		whenever(fetchResult.asByteArray()).thenThrow(IOException::class.java)
		clientGetCallback.value.onSuccess(fetchResult, mock())
		verify(backgroundFetchCallback).failed(uri)
		verifyNoMoreInteractions(backgroundFetchCallback)
	}

	@Test
	fun `unregistering a registered USK with different edition unregisters USK`() {
		val callback = mock<Callback>()
		val uri = createRandom(randomSource, "test-123").uri.uskForSSK()
		freenetInterface.registerUsk(uri, callback)
		freenetInterface.unregisterUsk(uri.setSuggestedEdition(234))
		verify(uskManager).unsubscribe(any<USK>(), any<USKCallback>())
	}

}
