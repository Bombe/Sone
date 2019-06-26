/*
 * Sone - HttpRequestAccessor.java - Copyright © 2011–2019 David Roden
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

import net.pterodactylus.util.template.Accessor;
import net.pterodactylus.util.template.ReflectionAccessor;
import net.pterodactylus.util.template.TemplateContext;
import freenet.support.api.HTTPRequest;

/**
 * {@link Accessor} implementation that can parse headers from
 * {@link HTTPRequest}s.
 *
 * @see HTTPRequest#getHeader(String)
 */
public class HttpRequestAccessor extends ReflectionAccessor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object get(TemplateContext templateContext, Object object, String member) {
		Object parentValue = super.get(templateContext, object, member);
		if (parentValue != null) {
			return parentValue;
		}
		HTTPRequest httpRequest = (HTTPRequest) object;
		return httpRequest.getHeader(member);
	}

}
