/*
 * Sone - Context.java - Copyright © 2014–2019 David Roden
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

package net.pterodactylus.sone.freenet.wot;

import javax.annotation.Nullable;

import com.google.common.base.Function;

/**
 * Custom container for the Web of Trust context. This allows easier
 * configuration of dependency injection.
 */
public class Context {

	public static final Function<Context, String> extractContext = new Function<Context, String>() {
		@Nullable
		@Override
		public String apply(@Nullable Context context) {
			return (context == null) ? null : context.getContext();
		}
	};

	private final String context;

	public Context(String context) {
		this.context = context;
	}

	public String getContext() {
		return context;
	}

}
