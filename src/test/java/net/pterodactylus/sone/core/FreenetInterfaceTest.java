package net.pterodactylus.sone.core;

import static freenet.client.FetchException.FetchExceptionMode.ALL_DATA_NOT_FOUND;
import static freenet.keys.InsertableClientSSK.createRandom;
import static freenet.node.RequestStarter.INTERACTIVE_PRIORITY_CLASS;
import static freenet.node.RequestStarter.PREFETCH_PRIORITY_CLASS;
import static net.pterodactylus.sone.Matchers.delivers;
import static net.pterodactylus.sone.TestUtil.setFinalField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import net.pterodactylus.sone.TestUtil;
import net.pterodactylus.sone.core.FreenetInterface.BackgroundFetchCallback;
import net.pterodactylus.sone.core.FreenetInterface.Callback;
import net.pterodactylus.sone.core.FreenetInterface.Fetched;
import net.pterodactylus.sone.core.FreenetInterface.InsertToken;
import net.pterodactylus.sone.core.FreenetInterface.InsertTokenSupplier;
import net.pterodactylus.sone.core.event.ImageInsertAbortedEvent;
import net.pterodactylus.sone.core.event.ImageInsertFailedEvent;
import net.pterodactylus.sone.core.event.ImageInsertFinishedEvent;
import net.pterodactylus.sone.core.event.ImageInsertStartedEvent;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.TemporaryImage;
import net.pterodactylus.sone.data.impl.ImageImpl;

import freenet.client.ClientMetadata;
import freenet.client.FetchContext;
import freenet.client.FetchException;
import freenet.client.FetchException.FetchExceptionMode;
import freenet.client.FetchResult;
import freenet.client.HighLevelSimpleClient;
import freenet.client.InsertBlock;
import freenet.client.InsertContext;
import freenet.client.InsertException;
import freenet.client.InsertException.InsertExceptionMode;
import freenet.client.async.ClientGetCallback;
import freenet.client.async.ClientGetter;
import freenet.client.async.ClientPutter;
import freenet.client.async.USKCallback;
import freenet.client.async.USKManager;
import freenet.crypt.DummyRandomSource;
import freenet.crypt.RandomSource;
import freenet.keys.FreenetURI;
import freenet.keys.InsertableClientSSK;
import freenet.keys.USK;
import freenet.node.Node;
import freenet.node.NodeClientCore;
import freenet.node.RequestClient;
import freenet.support.Base64;
import freenet.support.api.Bucket;
import freenet.support.io.ArrayBucket;
import freenet.support.io.ResumeFailedException;

import com.google.common.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

/**
 * Unit test for {@link FreenetInterface}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class FreenetInterfaceTest {

	private final EventBus eventBus = mock(EventBus.class);
	private final Node node = mock(Node.class);
	private final NodeClientCore nodeClientCore = mock(NodeClientCore.class);
	private final HighLevelSimpleClient highLevelSimpleClient = mock(HighLevelSimpleClient.class, withSettings().extraInterfaces(RequestClient.class));
	private final RandomSource randomSource = new DummyRandomSource();
	private final USKManager uskManager = mock(USKManager.class);
	private FreenetInterface freenetInterface;
	private final Sone sone = mock(Sone.class);
	private final ArgumentCaptor<USKCallback> callbackCaptor = forClass(USKCallback.class);
	private final Image image = mock(Image.class);
	private InsertToken insertToken;
	private final Bucket bucket = mock(Bucket.class);
	private final ArgumentCaptor<ClientGetCallback> clientGetCallback = forClass(ClientGetCallback.class);
	private final FreenetURI uri = new FreenetURI("KSK@pgl.png");
	private final FetchResult fetchResult = mock(FetchResult.class);
	private final BackgroundFetchCallback backgroundFetchCallback = mock(BackgroundFetchCallback.class);

	public FreenetInterfaceTest() throws MalformedURLException {
	}

	@Before
	public void setupHighLevelSimpleClient() {
		when(highLevelSimpleClient.getFetchContext()).thenReturn(mock(FetchContext.class));
	}

	@Before
	public void setupFreenetInterface() {
		when(nodeClientCore.makeClient(anyShort(), anyBoolean(), anyBoolean())).thenReturn(highLevelSimpleClient);
		setFinalField(node, "clientCore", nodeClientCore);
		setFinalField(node, "random", randomSource);
		setFinalField(nodeClientCore, "uskManager", uskManager);
		freenetInterface = new FreenetInterface(eventBus, node);
		insertToken = freenetInterface.new InsertToken(image);
		insertToken.setBucket(bucket);
	}

	@Before
	public void setupSone() {
		InsertableClientSSK insertSsk = createRandom(randomSource, "test-0");
		when(sone.getId()).thenReturn(Base64.encode(insertSsk.getURI().getRoutingKey()));
		when(sone.getRequestUri()).thenReturn(insertSsk.getURI().uskForSSK());
	}

	@Before
	public void setupCallbackCaptorAndUskManager() {
		doNothing().when(uskManager).subscribe(any(USK.class), callbackCaptor.capture(), anyBoolean(), any(RequestClient.class));
	}

	@Test
	public void canFetchUri() throws MalformedURLException, FetchException {
		FreenetURI freenetUri = new FreenetURI("KSK@GPLv3.txt");
		FetchResult fetchResult = createFetchResult();
		when(highLevelSimpleClient.fetch(freenetUri)).thenReturn(fetchResult);
		Fetched fetched = freenetInterface.fetchUri(freenetUri);
		assertThat(fetched, notNullValue());
		assertThat(fetched.getFetchResult(), is(fetchResult));
		assertThat(fetched.getFreenetUri(), is(freenetUri));
	}

	@Test
	public void fetchFollowsRedirect() throws MalformedURLException, FetchException {
		FreenetURI freenetUri = new FreenetURI("KSK@GPLv2.txt");
		FreenetURI newFreenetUri = new FreenetURI("KSK@GPLv3.txt");
		FetchResult fetchResult = createFetchResult();
		FetchException fetchException = new FetchException(FetchExceptionMode.PERMANENT_REDIRECT, newFreenetUri);
		when(highLevelSimpleClient.fetch(freenetUri)).thenThrow(fetchException);
		when(highLevelSimpleClient.fetch(newFreenetUri)).thenReturn(fetchResult);
		Fetched fetched = freenetInterface.fetchUri(freenetUri);
		assertThat(fetched.getFetchResult(), is(fetchResult));
		assertThat(fetched.getFreenetUri(), is(newFreenetUri));
	}

	@Test
	public void fetchReturnsNullOnFetchExceptions() throws MalformedURLException, FetchException {
		FreenetURI freenetUri = new FreenetURI("KSK@GPLv2.txt");
		FetchException fetchException = new FetchException(ALL_DATA_NOT_FOUND);
		when(highLevelSimpleClient.fetch(freenetUri)).thenThrow(fetchException);
		Fetched fetched = freenetInterface.fetchUri(freenetUri);
		assertThat(fetched, nullValue());
	}

	private FetchResult createFetchResult() {
		ClientMetadata clientMetadata = new ClientMetadata("text/plain");
		Bucket bucket = new ArrayBucket("Some Data.".getBytes());
		return new FetchResult(clientMetadata, bucket);
	}

	@Test
	public void insertingAnImage() throws SoneException, InsertException, IOException {
		TemporaryImage temporaryImage = new TemporaryImage("image-id");
		temporaryImage.setMimeType("image/png");
		byte[] imageData = new byte[] { 1, 2, 3, 4 };
		temporaryImage.setImageData(imageData);
		Image image = new ImageImpl("image-id");
		InsertToken insertToken = freenetInterface.new InsertToken(image);
		InsertContext insertContext = mock(InsertContext.class);
		when(highLevelSimpleClient.getInsertContext(anyBoolean())).thenReturn(insertContext);
		ClientPutter clientPutter = mock(ClientPutter.class);
		ArgumentCaptor<InsertBlock> insertBlockCaptor = forClass(InsertBlock.class);
		when(highLevelSimpleClient.insert(insertBlockCaptor.capture(), eq((String) null), eq(false), eq(insertContext), eq(insertToken), anyShort())).thenReturn(clientPutter);
		freenetInterface.insertImage(temporaryImage, image, insertToken);
		assertThat(insertBlockCaptor.getValue().getData().getInputStream(), delivers(new byte[] { 1, 2, 3, 4 }));
		assertThat(TestUtil.<ClientPutter>getPrivateField(insertToken, "clientPutter"), is(clientPutter));
		verify(eventBus).post(any(ImageInsertStartedEvent.class));
	}

	@Test(expected = SoneInsertException.class)
	public void insertExceptionCausesASoneException() throws InsertException, SoneException, IOException {
		TemporaryImage temporaryImage = new TemporaryImage("image-id");
		temporaryImage.setMimeType("image/png");
		byte[] imageData = new byte[] { 1, 2, 3, 4 };
		temporaryImage.setImageData(imageData);
		Image image = new ImageImpl("image-id");
		InsertToken insertToken = freenetInterface.new InsertToken(image);
		InsertContext insertContext = mock(InsertContext.class);
		when(highLevelSimpleClient.getInsertContext(anyBoolean())).thenReturn(insertContext);
		ArgumentCaptor<InsertBlock> insertBlockCaptor = forClass(InsertBlock.class);
		when(highLevelSimpleClient.insert(insertBlockCaptor.capture(), eq((String) null), eq(false), eq(insertContext), eq(insertToken), anyShort())).thenThrow(InsertException.class);
		freenetInterface.insertImage(temporaryImage, image, insertToken);
	}

	@Test
	public void insertingADirectory() throws InsertException, SoneException {
		FreenetURI freenetUri = mock(FreenetURI.class);
		HashMap<String, Object> manifestEntries = new HashMap<String, Object>();
		String defaultFile = "index.html";
		FreenetURI resultingUri = mock(FreenetURI.class);
		when(highLevelSimpleClient.insertManifest(eq(freenetUri), eq(manifestEntries), eq(defaultFile))).thenReturn(resultingUri);
		assertThat(freenetInterface.insertDirectory(freenetUri, manifestEntries, defaultFile), is(resultingUri));
	}

	@Test(expected = SoneException.class)
	public void insertExceptionIsForwardedAsSoneException() throws InsertException, SoneException {
		when(highLevelSimpleClient.insertManifest(ArgumentMatchers.<FreenetURI>any(), ArgumentMatchers.<HashMap<String, Object>>any(), ArgumentMatchers.<String>any())).thenThrow(InsertException.class);
		freenetInterface.insertDirectory(null, null, null);
	}

	@Test
	public void soneWithWrongRequestUriWillNotBeSubscribed() throws MalformedURLException {
		when(sone.getRequestUri()).thenReturn(new FreenetURI("KSK@GPLv3.txt"));
		freenetInterface.registerUsk(new FreenetURI("KSK@GPLv3.txt"), null);
		verify(uskManager, never()).subscribe(any(USK.class), any(USKCallback.class), anyBoolean(), any(RequestClient.class));
	}

	@Test
	public void registeringAUsk() {
		FreenetURI freenetUri = createRandom(randomSource, "test-0").getURI().uskForSSK();
		Callback callback = mock(Callback.class);
		freenetInterface.registerUsk(freenetUri, callback);
		verify(uskManager).subscribe(any(USK.class), any(USKCallback.class), anyBoolean(), eq((RequestClient) highLevelSimpleClient));
	}

	@Test
	public void registeringANonUskKeyWillNotBeSubscribed() throws MalformedURLException {
		FreenetURI freenetUri = new FreenetURI("KSK@GPLv3.txt");
		Callback callback = mock(Callback.class);
		freenetInterface.registerUsk(freenetUri, callback);
		verify(uskManager, never()).subscribe(any(USK.class), any(USKCallback.class), anyBoolean(), eq((RequestClient) highLevelSimpleClient));
	}

	@Test
	public void registeringAnActiveUskWillSubscribeToItCorrectly() {
		FreenetURI freenetUri = createRandom(randomSource, "test-0").getURI().uskForSSK();
		final USKCallback uskCallback = mock(USKCallback.class);
		freenetInterface.registerActiveUsk(freenetUri, uskCallback);
		verify(uskManager).subscribe(any(USK.class), eq(uskCallback), eq(true), any(RequestClient.class));
	}

	@Test
	public void registeringAnInactiveUskWillSubscribeToItCorrectly() {
		FreenetURI freenetUri = createRandom(randomSource, "test-0").getURI().uskForSSK();
		final USKCallback uskCallback = mock(USKCallback.class);
		freenetInterface.registerPassiveUsk(freenetUri, uskCallback);
		verify(uskManager).subscribe(any(USK.class), eq(uskCallback), eq(false), any(RequestClient.class));
	}

	@Test
	public void registeringAnActiveNonUskWillNotSubscribeToAUsk()
	throws MalformedURLException {
		FreenetURI freenetUri = createRandom(randomSource, "test-0").getURI();
		freenetInterface.registerActiveUsk(freenetUri, null);
		verify(uskManager, never()).subscribe(any(USK.class),
				any(USKCallback.class), anyBoolean(),
				eq((RequestClient) highLevelSimpleClient));
	}

	@Test
	public void registeringAnInactiveNonUskWillNotSubscribeToAUsk()
	throws MalformedURLException {
		FreenetURI freenetUri = createRandom(randomSource, "test-0").getURI();
		freenetInterface.registerPassiveUsk(freenetUri, null);
		verify(uskManager, never()).subscribe(any(USK.class),
				any(USKCallback.class), anyBoolean(),
				eq((RequestClient) highLevelSimpleClient));
	}

	@Test
	public void unregisteringANotRegisteredUskDoesNothing() {
		FreenetURI freenetURI = createRandom(randomSource, "test-0").getURI().uskForSSK();
		freenetInterface.unregisterUsk(freenetURI);
		verify(uskManager, never()).unsubscribe(any(USK.class), any(USKCallback.class));
	}

	@Test
	public void unregisteringARegisteredUsk() {
		FreenetURI freenetURI = createRandom(randomSource, "test-0").getURI().uskForSSK();
		Callback callback = mock(Callback.class);
		freenetInterface.registerUsk(freenetURI, callback);
		freenetInterface.unregisterUsk(freenetURI);
		verify(uskManager).unsubscribe(any(USK.class), any(USKCallback.class));
	}

	@Test
	public void unregisteringANotRegisteredSoneDoesNothing() {
		freenetInterface.unregisterUsk(sone);
		verify(uskManager, never()).unsubscribe(any(USK.class), any(USKCallback.class));
	}

	@Test
	public void unregisteringARegisteredSoneUnregistersTheSone()
	throws MalformedURLException {
		freenetInterface.registerActiveUsk(sone.getRequestUri(), mock(USKCallback.class));
		freenetInterface.unregisterUsk(sone);
		verify(uskManager).unsubscribe(any(USK.class), any(USKCallback.class));
	}

	@Test
	public void unregisteringASoneWithAWrongRequestKeyWillNotUnsubscribe() throws MalformedURLException {
		when(sone.getRequestUri()).thenReturn(new FreenetURI("KSK@GPLv3.txt"));
		freenetInterface.registerUsk(sone.getRequestUri(), null);
		freenetInterface.unregisterUsk(sone);
		verify(uskManager, never()).unsubscribe(any(USK.class), any(USKCallback.class));
	}

	@Test
	public void callbackForNormalUskUsesDifferentPriorities() {
		Callback callback = mock(Callback.class);
		FreenetURI soneUri = createRandom(randomSource, "test-0").getURI().uskForSSK();
		freenetInterface.registerUsk(soneUri, callback);
		assertThat(callbackCaptor.getValue().getPollingPriorityNormal(), is(PREFETCH_PRIORITY_CLASS));
		assertThat(callbackCaptor.getValue().getPollingPriorityProgress(), is(INTERACTIVE_PRIORITY_CLASS));
	}

	@Test
	public void callbackForNormalUskForwardsImportantParameters() throws MalformedURLException {
		Callback callback = mock(Callback.class);
		FreenetURI uri = createRandom(randomSource, "test-0").getURI().uskForSSK();
		freenetInterface.registerUsk(uri, callback);
		USK key = mock(USK.class);
		when(key.getURI()).thenReturn(uri);
		callbackCaptor.getValue().onFoundEdition(3, key, null, false, (short) 0, null, true, true);
		verify(callback).editionFound(eq(uri), eq(3L), eq(true), eq(true));
	}

	@Test
	public void fetchedRetainsUriAndFetchResult() {
		FreenetURI freenetUri = mock(FreenetURI.class);
		FetchResult fetchResult = mock(FetchResult.class);
		Fetched fetched = new Fetched(freenetUri, fetchResult);
		assertThat(fetched.getFreenetUri(), is(freenetUri));
		assertThat(fetched.getFetchResult(), is(fetchResult));
	}

	@Test
	public void cancellingAnInsertWillFireImageInsertAbortedEvent() {
		ClientPutter clientPutter = mock(ClientPutter.class);
		insertToken.setClientPutter(clientPutter);
		ArgumentCaptor<ImageInsertStartedEvent> imageInsertStartedEvent = forClass(ImageInsertStartedEvent.class);
		verify(eventBus).post(imageInsertStartedEvent.capture());
		assertThat(imageInsertStartedEvent.getValue().image(), is(image));
		insertToken.cancel();
		ArgumentCaptor<ImageInsertAbortedEvent> imageInsertAbortedEvent = forClass(ImageInsertAbortedEvent.class);
		verify(eventBus, times(2)).post(imageInsertAbortedEvent.capture());
		verify(bucket).free();
		assertThat(imageInsertAbortedEvent.getValue().image(), is(image));
	}

	@Test
	public void failureWithoutExceptionSendsFailedEvent() {
		insertToken.onFailure(null, null);
		ArgumentCaptor<ImageInsertFailedEvent> imageInsertFailedEvent = forClass(ImageInsertFailedEvent.class);
		verify(eventBus).post(imageInsertFailedEvent.capture());
		verify(bucket).free();
		assertThat(imageInsertFailedEvent.getValue().image(), is(image));
		assertThat(imageInsertFailedEvent.getValue().cause(), nullValue());
	}

	@Test
	public void failureSendsFailedEventWithException() {
		InsertException insertException = new InsertException(InsertExceptionMode.INTERNAL_ERROR, "Internal error", null);
		insertToken.onFailure(insertException, null);
		ArgumentCaptor<ImageInsertFailedEvent> imageInsertFailedEvent = forClass(ImageInsertFailedEvent.class);
		verify(eventBus).post(imageInsertFailedEvent.capture());
		verify(bucket).free();
		assertThat(imageInsertFailedEvent.getValue().image(), is(image));
		assertThat(imageInsertFailedEvent.getValue().cause(), is((Throwable) insertException));
	}

	@Test
	public void failureBecauseCancelledByUserSendsAbortedEvent() {
		InsertException insertException = new InsertException(InsertExceptionMode.CANCELLED, null);
		insertToken.onFailure(insertException, null);
		ArgumentCaptor<ImageInsertAbortedEvent> imageInsertAbortedEvent = forClass(ImageInsertAbortedEvent.class);
		verify(eventBus).post(imageInsertAbortedEvent.capture());
		verify(bucket).free();
		assertThat(imageInsertAbortedEvent.getValue().image(), is(image));
	}

	@Test
	public void ignoredMethodsDoNotThrowExceptions() throws ResumeFailedException {
		insertToken.onResume(null);
		insertToken.onFetchable(null);
		insertToken.onGeneratedMetadata(null, null);
	}

	@Test
	public void generatedUriIsPostedOnSuccess() {
		FreenetURI generatedUri = mock(FreenetURI.class);
		insertToken.onGeneratedURI(generatedUri, null);
		insertToken.onSuccess(null);
		ArgumentCaptor<ImageInsertFinishedEvent> imageInsertFinishedEvent = forClass(ImageInsertFinishedEvent.class);
		verify(eventBus).post(imageInsertFinishedEvent.capture());
		verify(bucket).free();
		assertThat(imageInsertFinishedEvent.getValue().image(), is(image));
		assertThat(imageInsertFinishedEvent.getValue().resultingUri(), is(generatedUri));
	}

	@Test
	public void insertTokenSupplierSuppliesInsertTokens() {
		InsertTokenSupplier insertTokenSupplier = freenetInterface.new InsertTokenSupplier();
		assertThat(insertTokenSupplier.apply(image), notNullValue());
	}

	@Test
	public void backgroundFetchCanBeStarted() throws Exception {
		freenetInterface.startFetch(uri, backgroundFetchCallback);
		verify(highLevelSimpleClient).fetch(eq(uri), anyLong(), any(ClientGetCallback.class), any(FetchContext.class), anyShort());
	}

	@Test
	public void callbackOfBackgroundFetchIsNotifiedOnSuccess() throws Exception {
		freenetInterface.startFetch(uri, backgroundFetchCallback);
		verify(highLevelSimpleClient).fetch(eq(uri), anyLong(), clientGetCallback.capture(), any(FetchContext.class), anyShort());
		when(fetchResult.getMimeType()).thenReturn("image/png");
		when(fetchResult.asByteArray()).thenReturn(new byte[] { 1, 2, 3, 4, 5 });
		clientGetCallback.getValue().onSuccess(fetchResult, mock(ClientGetter.class));
		verify(backgroundFetchCallback).loaded(uri, "image/png", new byte[] { 1, 2, 3, 4, 5 });
		verifyNoMoreInteractions(backgroundFetchCallback);
	}

	@Test
	public void callbackOfBackgroundFetchIsNotifiedOnFailure() throws Exception {
		freenetInterface.startFetch(uri, backgroundFetchCallback);
		verify(highLevelSimpleClient).fetch(eq(uri), anyLong(), clientGetCallback.capture(), any(FetchContext.class), anyShort());
		when(fetchResult.getMimeType()).thenReturn("image/png");
		when(fetchResult.asByteArray()).thenReturn(new byte[] { 1, 2, 3, 4, 5 });
		clientGetCallback.getValue().onFailure(new FetchException(ALL_DATA_NOT_FOUND), mock(ClientGetter.class));
		verify(backgroundFetchCallback).failed(uri);
		verifyNoMoreInteractions(backgroundFetchCallback);
	}

	@Test
	public void callbackOfBackgroundFetchIsNotifiedAsFailureIfBucketCanNotBeLoaded() throws Exception {
		freenetInterface.startFetch(uri, backgroundFetchCallback);
		verify(highLevelSimpleClient).fetch(eq(uri), anyLong(), clientGetCallback.capture(), any(FetchContext.class), anyShort());
		when(fetchResult.getMimeType()).thenReturn("image/png");
		when(fetchResult.asByteArray()).thenThrow(IOException.class);
		clientGetCallback.getValue().onSuccess(fetchResult, mock(ClientGetter.class));
		verify(backgroundFetchCallback).failed(uri);
		verifyNoMoreInteractions(backgroundFetchCallback);
	}

}
