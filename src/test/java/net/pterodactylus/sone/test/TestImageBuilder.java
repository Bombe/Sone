package net.pterodactylus.sone.test;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.database.ImageBuilder;

/**
 * {@link ImageBuilder} implementation that returns a mocked {@link Image}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class TestImageBuilder implements ImageBuilder {

	private final Image image;

	public TestImageBuilder() {
		image = mock(Image.class);
		Image.Modifier imageModifier = new Image.Modifier() {
			private Sone sone = image.getSone();
			private long creationTime = image.getCreationTime();
			private String key = image.getKey();
			private String title = image.getTitle();
			private String description = image.getDescription();
			private int width = image.getWidth();
			private int height = image.getHeight();

			@Override
			public Image.Modifier setSone(Sone sone) {
				this.sone = sone;
				return this;
			}

			@Override
			public Image.Modifier setCreationTime(long creationTime) {
				this.creationTime = creationTime;
				return this;
			}

			@Override
			public Image.Modifier setKey(String key) {
				this.key = key;
				return this;
			}

			@Override
			public Image.Modifier setTitle(String title) {
				this.title = title;
				return this;
			}

			@Override
			public Image.Modifier setDescription(String description) {
				this.description = description;
				return this;
			}

			@Override
			public Image.Modifier setWidth(int width) {
				this.width = width;
				return this;
			}

			@Override
			public Image.Modifier setHeight(int height) {
				this.height = height;
				return this;
			}

			@Override
			public Image update() throws IllegalStateException {
				when(image.getSone()).thenReturn(sone);
				when(image.getCreationTime()).thenReturn(creationTime);
				when(image.getKey()).thenReturn(key);
				when(image.getTitle()).thenReturn(title);
				when(image.getDescription()).thenReturn(description);
				when(image.getWidth()).thenReturn(width);
				when(image.getHeight()).thenReturn(height);
				return image;
			}
		};
		when(image.modify()).thenReturn(imageModifier);
	}

	@Override
	public ImageBuilder randomId() {
		when(image.getId()).thenReturn(randomUUID().toString());
		return this;
	}

	@Override
	public ImageBuilder withId(String id) {
		when(image.getId()).thenReturn(id);
		return this;
	}

	@Override
	public Image build() throws IllegalStateException {
		return image;
	}

}
