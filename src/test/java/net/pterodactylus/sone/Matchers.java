/*
 * Sone - Matchers.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sone;

import static com.google.common.base.Objects.equal;
import static com.google.common.collect.Iterators.size;
import static java.util.Arrays.asList;
import static java.util.regex.Pattern.compile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import freenet.support.SimpleFieldSet;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matchers used throughout the tests.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class Matchers {

	public static Matcher<String> matchesRegex(final String regex) {
		return new TypeSafeMatcher<String>() {
			@Override
			protected boolean matchesSafely(String item) {
				return compile(regex).matcher(item).matches();
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("matches: ").appendValue(regex);
			}
		};
	}

}
