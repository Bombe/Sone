/*
 * Sone - AboutPage.java - Copyright © 2010–2016 David Roden
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

import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;
import net.pterodactylus.util.version.Version;

/**
 * Shows some information about Sone.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class AboutPage extends SoneTemplatePage {

	private final String version;
	private final int year;
	private final String homepage;

	public AboutPage(Template template, WebInterface webInterface, String version, int year, String homepage) {
		super("about.html", template, "Page.About.Title", webInterface, false);
		this.version = version;
		this.year = year;
		this.homepage = homepage;
	}

	@Override
	protected void processTemplate(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		super.processTemplate(request, templateContext);
		templateContext.set("version", version);
		templateContext.set("year", year);
		templateContext.set("homepage", homepage);
	}

}
