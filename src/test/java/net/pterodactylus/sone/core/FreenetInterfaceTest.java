package net.pterodactylus.sone.core;

import static freenet.keys.InsertableClientSSK.createRandom;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.DAYS;
import static net.pterodactylus.sone.Matchers.delivers;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyShort;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.util.HashMap;

import net.pterodactylus.sone.core.FreenetInterface.Callback;
import net.pterodactylus.sone.core.FreenetInterface.Fetched;
import net.pterodactylus.sone.core.FreenetInterface.InsertToken;
import net.pterodactylus.sone.core.event.ImageInsertStartedEvent;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.data.ImageImpl;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.TemporaryImage;
import net.pterodactylus.sone.freenet.StringBucket;

import freenet.client.ClientMetadata;
import freenet.client.FetchException;
import freenet.client.FetchResult;
import freenet.client.HighLevelSimpleClient;
import freenet.client.InsertBlock;
import freenet.client.InsertContext;
import freenet.client.InsertException;
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

import com.google.common.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

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

	@Before
	public void setupFreenetInterface() {
		when(nodeClientCore.makeClient(anyShort(), anyBoolean(), anyBoolean())).thenReturn(highLevelSimpleClient);
		setFinalField(node, "clientCore", nodeClientCore);
		setFinalField(node, "random", randomSource);
		setFinalField(nodeClientCore, "uskManager", uskManager);
		freenetInterface = new FreenetInterface(eventBus, node);
	}

	@Before
	public void setupSone() {
		InsertableClientSSK insertSsk = createRandom(randomSource, "test-0");
		when(sone.getId()).thenReturn(Base64.encode(insertSsk.getURI().getRoutingKey()));
		when(sone.getRequestUri()).thenReturn(insertSsk.getURI().uskForSSK());
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
		FetchException fetchException = new FetchException(FetchException.PERMANENT_REDIRECT, newFreenetUri);
		when(highLevelSimpleClient.fetch(freenetUri)).thenThrow(fetchException);
		when(highLevelSimpleClient.fetch(newFreenetUri)).thenReturn(fetchResult);
		Fetched fetched = freenetInterface.fetchUri(freenetUri);
		assertThat(fetched.getFetchResult(), is(fetchResult));
		assertThat(fetched.getFreenetUri(), is(newFreenetUri));
	}

	@Test
	public void fetchReturnsNullOnFetchExceptions() throws MalformedURLException, FetchException {
		FreenetURI freenetUri = new FreenetURI("KSK@GPLv2.txt");
		FetchException fetchException = new FetchException(FetchException.ALL_DATA_NOT_FOUND);
		when(highLevelSimpleClient.fetch(freenetUri)).thenThrow(fetchException);
		Fetched fetched = freenetInterface.fetchUri(freenetUri);
		assertThat(fetched, nullValue());
	}

	private FetchResult createFetchResult() {
		ClientMetadata clientMetadata = new ClientMetadata("text/plain");
		Bucket bucket = new StringBucket("Some Data.");
		return new FetchResult(clientMetadata, bucket);
	}

	private void setFinalField(Object object, String fieldName, Object value) {
		try {
			Field clientCoreField = object.getClass().getField(fieldName);
			clientCoreField.setAccessible(true);
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(clientCoreField, clientCoreField.getModifiers() & ~Modifier.FINAL);
			clientCoreField.set(object, value);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
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
		when(highLevelSimpleClient.insert(insertBlockCaptor.capture(), eq(false), eq((String) null), eq(false), eq(insertContext), eq(insertToken), anyShort())).thenReturn(clientPutter);
		freenetInterface.insertImage(temporaryImage, image, insertToken);
		assertThat(insertBlockCaptor.getValue().getData().getInputStream(), delivers(new byte[] { 1, 2, 3, 4 }));
		assertThat(this.<ClientPutter>getPrivateField(insertToken, "clientPutter"), is(clientPutter));
		verify(eventBus).post(any(ImageInsertStartedEvent.class));
	}

	private <T> T getPrivateField(Object object, String fieldName) {
		try {
			Field field = object.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return (T) field.get(object);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
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
		when(highLevelSimpleClient.insert(insertBlockCaptor.capture(), eq(false), eq((String) null), eq(false), eq(insertContext), eq(insertToken), anyShort())).thenThrow(InsertException.class);
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
		when(highLevelSimpleClient.insertManifest(any(FreenetURI.class), any(HashMap.class), any(String.class))).thenThrow(InsertException.class);
		freenetInterface.insertDirectory(null, null, null);
	}

	@Test
	public void soneWithWrongRequestUriWillNotBeSubscribed() throws MalformedURLException {
		when(sone.getRequestUri()).thenReturn(new FreenetURI("KSK@GPLv3.txt"));
		freenetInterface.registerUsk(sone, null);
		verify(uskManager, never()).subscribe(any(USK.class), any(USKCallback.class), anyBoolean(), any(RequestClient.class));
	}

	@Test
	public void registeringAUskForARecentlyModifiedSone() throws MalformedURLException {
		when(sone.getTime()).thenReturn(currentTimeMillis() - DAYS.toMillis(1));
		freenetInterface.registerUsk(sone, null);
		verify(uskManager).subscribe(any(USK.class), any(USKCallback.class), eq(true), eq((RequestClient) highLevelSimpleClient));
	}

	@Test
	public void registeringAUskForAnOldSone() throws MalformedURLException {
		when(sone.getTime()).thenReturn(currentTimeMillis() - DAYS.toMillis(365));
		freenetInterface.registerUsk(sone, null);
		verify(uskManager).subscribe(any(USK.class), any(USKCallback.class), eq(false), eq((RequestClient) highLevelSimpleClient));
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
	public void unregisteringARegisteredSoneUnregistersTheSone() {
		freenetInterface.registerUsk(sone, null);
		freenetInterface.unregisterUsk(sone);
		verify(uskManager).unsubscribe(any(USK.class), any(USKCallback.class));
	}

	@Test
	public void unregisteringASoneWithAWrongRequestKeyWillNotUnsubscribe() throws MalformedURLException {
		freenetInterface.registerUsk(sone, null);
		when(sone.getRequestUri()).thenReturn(new FreenetURI("KSK@GPLv3.txt"));
		freenetInterface.unregisterUsk(sone);
		verify(uskManager, never()).unsubscribe(any(USK.class), any(USKCallback.class));
	}

	@Test
	public void fetchedRetainsUriAndFetchResult() {
		FreenetURI freenetUri = mock(FreenetURI.class);
		FetchResult fetchResult = mock(FetchResult.class);
		Fetched fetched = new Fetched(freenetUri, fetchResult);
		assertThat(fetched.getFreenetUri(), is(freenetUri));
		assertThat(fetched.getFetchResult(), is(fetchResult));
	}

}
