/*
 * Sone - AlbumAccessor.java - Copyright © 2011–2015 David Roden
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

import java.util.ArrayList;
import java.util.List;

import net.pterodactylus.sone.data.Album;
import net.pterodactylus.util.template.Accessor;
import net.pterodactylus.util.template.ReflectionAccessor;
import net.pterodactylus.util.template.TemplateContext;

/**
 * {@link Accessor} implementation for {@link Album}s. A property named
 * “backlinks” is added, it returns links to all parents and the owner Sone of
 * an album.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class AlbumAccessor extends ReflectionAccessor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object get(TemplateContext templateContext, Object object, String member) {
		Album album = (Album) object;
		if ("backlinks".equals(member)) {
			List<Link> backlinks = new ArrayList<Link>();
			Album currentAlbum = album;
			while (!currentAlbum.isRoot()) {
				backlinks.add(0, new Link("imageBrowser.html?album=" + currentAlbum.getId(), currentAlbum.getTitle()));
				currentAlbum = currentAlbum.getParent();
			}
			backlinks.add(0, new Link("imageBrowser.html?sone=" + album.getSone().getId(), SoneAccessor.getNiceName(album.getSone())));
			return backlinks;
		}
		return super.get(templateContext, object, member);
	}

	/**
	 * Container for links.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	private static class Link {

		/** The target of the link. */
		private final String target;

		/** The name of the link. */
		private final String name;

		/**
		 * Creates a new link.
		 *
		 * @param target
		 * 		The target of the link
		 * @param name
		 * 		The name of the link
		 */
		private Link(String target, String name) {
			this.target = target;
			this.name = name;
		}

		//
		// ACCESSORS
		//

		/**
		 * Returns the target of the link.
		 *
		 * @return The target of the link
		 */
		public String getTarget() {
			return target;
		}

		/**
		 * Returns the name of the link.
		 *
		 * @return The name of the link
		 */
		public String getName() {
			return name;
		}

	}

}
