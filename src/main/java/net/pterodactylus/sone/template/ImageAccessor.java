/*
 * Sone - ImageAccessor.java - Copyright © 2011–2016 David Roden
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pterodactylus.sone.template;

import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.util.template.Accessor;
import net.pterodactylus.util.template.ReflectionAccessor;
import net.pterodactylus.util.template.TemplateContext;

/**
 * {@link Accessor} implementation for {@link Image} objects. It adds the
 * following properties:
 * <ul>
 * <li>{@code previous}: returns the previous image in the image’s album, or
 * {@code null} if the image is the first image of its album.</li>
 * <li>{@code next}: returns the next image in the image’s album, or {@code
 * null} if the image is the last image of its album.</li>
 * </ul>
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ImageAccessor extends ReflectionAccessor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object get(TemplateContext templateContext, Object object, String member) {
		Image image = (Image) object;
		if ("next".equals(member)) {
			Album album = image.getAlbum();
			int imagePosition = album.getImages().indexOf(image);
			if (imagePosition < album.getImages().size() - 1) {
				return album.getImages().get(imagePosition + 1);
			}
			return null;
		} else if ("previous".equals(member)) {
			Album album = image.getAlbum();
			int imagePosition = album.getImages().indexOf(image);
			if (imagePosition > 0) {
				return album.getImages().get(imagePosition - 1);
			}
			return null;
		}
		return super.get(templateContext, object, member);
	}

}
