package net.pterodactylus.sone.core;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.pterodactylus.sone.core.FreenetInterface.InsertToken;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.data.TemporaryImage;

import com.google.common.base.Function;
import org.junit.Test;

/**
 * Unit test for {@link ImageInserter}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ImageInserterTest {

	private final TemporaryImage temporaryImage = when(mock(TemporaryImage.class).getId()).thenReturn("image-id").getMock();
	private final Image image = when(mock(Image.class).getId()).thenReturn("image-id").getMock();
	private final FreenetInterface freenetInterface = mock(FreenetInterface.class);
	private final InsertToken insertToken = mock(InsertToken.class);
	private final Function<Image, InsertToken> insertTokenSupplier = when(mock(Function.class).apply(any(Image.class))).thenReturn(insertToken).getMock();
	private final ImageInserter imageInserter = new ImageInserter(freenetInterface, insertTokenSupplier);

	@Test
	public void inserterInsertsImage() throws SoneException {
		imageInserter.insertImage(temporaryImage, image);
		verify(freenetInterface).insertImage(eq(temporaryImage), eq(image), any(InsertToken.class));
	}

	@Test
	public void exceptionWhenInsertingImageIsIgnored() throws SoneException {
		doThrow(SoneException.class).when(freenetInterface).insertImage(eq(temporaryImage), eq(image), any(InsertToken.class));
		imageInserter.insertImage(temporaryImage, image);
		verify(freenetInterface).insertImage(eq(temporaryImage), eq(image), any(InsertToken.class));
	}

	@Test
	public void cancellingImageInsertThatIsNotRunningDoesNothing() {
		imageInserter.cancelImageInsert(image);
		verify(insertToken, never()).cancel();
	}

	@Test
	public void cancellingImage() {
		imageInserter.insertImage(temporaryImage, image);
		imageInserter.cancelImageInsert(image);
		verify(insertToken).cancel();
	}

}
