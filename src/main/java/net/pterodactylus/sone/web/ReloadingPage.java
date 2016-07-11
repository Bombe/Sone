/*
 * Sone - ReloadingPage.java - Copyright © 2010–2016 David Roden
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

package net.pterodactylus.sone.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.pterodactylus.util.io.Closer;
import net.pterodactylus.util.io.StreamCopier;
import net.pterodactylus.util.web.Page;
import net.pterodactylus.util.web.Request;
import net.pterodactylus.util.web.Response;

/**
 * {@link Page} implementation that delivers static files from the filesystem.
 *
 * @param <REQ>
 *            The type of the request
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ReloadingPage<REQ extends Request> implements Page<REQ> {

	private final String pathPrefix;
	private final String filesystemPath;
	private final String mimeType;

	public ReloadingPage(String pathPrefix, String filesystemPathPrefix, String mimeType) {
		this.pathPrefix = pathPrefix;
		this.filesystemPath = filesystemPathPrefix;
		this.mimeType = mimeType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getPath() {
		return pathPrefix;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isPrefixPage() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Response handleRequest(REQ request, Response response) throws IOException {
		String path = request.getUri().getPath();
		int lastSlash = path.lastIndexOf('/');
		String filename = path.substring(lastSlash + 1);
		InputStream fileInputStream;
		try {
			fileInputStream = new FileInputStream(new File(filesystemPath, filename));
		} catch (FileNotFoundException fnfe1) {
			return response.setStatusCode(404).setStatusText("Not found.");
		}
		OutputStream contentOutputStream = response.getContent();
		try {
			StreamCopier.copy(fileInputStream, contentOutputStream);
		} finally {
			Closer.close(fileInputStream);
			Closer.close(contentOutputStream);
		}
		return response.setStatusCode(200).setStatusText("OK").setContentType(mimeType);
	}
}
