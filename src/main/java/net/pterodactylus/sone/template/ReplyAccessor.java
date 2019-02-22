/*
 * Sone - ReplyAccessor.java - Copyright © 2010–2019 David Roden
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
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.util.template.Accessor;
import net.pterodactylus.util.template.ReflectionAccessor;
import net.pterodactylus.util.template.TemplateContext;

/**
 * {@link Accessor} implementation that adds a couple of properties to
 * {@link Reply}s.
 */
public class ReplyAccessor extends ReflectionAccessor {

	/** The core. */
	private final Core core;

	/**
	 * Creates a new reply accessor.
	 *
	 * @param core
	 *            The core
	 */
	public ReplyAccessor(Core core) {
		this.core = core;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object get(TemplateContext templateContext, Object object, String member) {
		PostReply reply = (PostReply) object;
		if ("likes".equals(member)) {
			return core.getLikes(reply);
		} else if (member.equals("liked")) {
			Sone currentSone = (Sone) templateContext.get("currentSone");
			return (currentSone != null) && (currentSone.isLikedReplyId(reply.getId()));
		} else if (member.equals("new")) {
			return !reply.isKnown();
		} else if (member.equals("loaded")) {
			return reply.getSone() != null;
		}
		return super.get(templateContext, object, member);
	}

}
