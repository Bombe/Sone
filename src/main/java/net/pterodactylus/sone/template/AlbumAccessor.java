/*
 * Sone - AlbumAccessor.java - Copyright © 2011 David Roden
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
			List<Map<String, String>> backlinks = new ArrayList<Map<String, String>>();
			Album currentAlbum = album;
			while (currentAlbum != null) {
				backlinks.add(0, createLink("imageBrowser.html?album=" + currentAlbum.getId(), currentAlbum.getTitle()));
				currentAlbum = currentAlbum.getParent();
			}
			backlinks.add(0, createLink("imageBrowser.html?sone=" + album.getSone().getId(), SoneAccessor.getNiceName(album.getSone())));
			return backlinks;
		}
		return super.get(templateContext, object, member);
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Creates a map containing mappings for “target” and “link.”
	 *
	 * @param target
	 *            The target to link to
	 * @param name
	 *            The name of the link
	 * @return The created map containing the mappings
	 */
	private Map<String, String> createLink(String target, String name) {
		Map<String, String> link = new HashMap<String, String>();
		link.put("target", target);
		link.put("name", name);
		return link;
	}

}
