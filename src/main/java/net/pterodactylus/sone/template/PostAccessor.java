/*
 * Sone - PostAccessor.java - Copyright © 2010 David Roden
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

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.util.template.DataProvider;
import net.pterodactylus.util.template.ReflectionAccessor;

/**
 * Accessor for {@link Post} objects that adds additional properties:
 * <dl>
 * <dd>replies</dd>
 * <dt>All replies to this post, sorted by time, oldest first</dt>
 * </dl>
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class PostAccessor extends ReflectionAccessor {

	/** The core to get the replies from. */
	private final Core core;

	/**
	 * Creates a new post accessor.
	 *
	 * @param core
	 *            The core to get the replies from
	 */
	public PostAccessor(Core core) {
		this.core = core;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object get(DataProvider dataProvider, Object object, String member) {
		if ("replies".equals(member)) {
			return core.getReplies((Post) object);
		}
		return super.get(dataProvider, object, member);
	}

}
