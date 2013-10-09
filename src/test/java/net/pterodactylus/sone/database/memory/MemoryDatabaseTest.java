/*
 * Sone - MemoryDatabaseTest.java - Copyright © 2013 David Roden
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

package net.pterodactylus.sone.database.memory;

import static com.google.common.base.Optional.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.AlbumImpl;

import com.google.common.base.Optional;
import org.junit.Test;

/**
 * Tests for {@link MemoryDatabase}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class MemoryDatabaseTest {

	private final MemoryDatabase memoryDatabase = new MemoryDatabase(null, null);

	@Test
	public void testBasicAlbumFunctionality() {
		Album newAlbum = new AlbumImpl();
		assertThat(memoryDatabase.getAlbum(newAlbum.getId()), is(Optional.<Album>absent()));
		memoryDatabase.storeAlbum(newAlbum);
		assertThat(memoryDatabase.getAlbum(newAlbum.getId()), is(of(newAlbum)));
		memoryDatabase.removeAlbum(newAlbum);
		assertThat(memoryDatabase.getAlbum(newAlbum.getId()), is(Optional.<Album>absent()));
	}

}
