/*
 * Sone - KnownSonesPage.java - Copyright © 2010 David Roden
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.util.collection.Pagination;
import net.pterodactylus.util.collection.ReverseComparator;
import net.pterodactylus.util.filter.Filter;
import net.pterodactylus.util.filter.Filters;
import net.pterodactylus.util.number.Numbers;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;

/**
 * This page shows all known Sones.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class KnownSonesPage extends SoneTemplatePage {

	/**
	 * Creates a “known Sones” page.
	 *
	 * @param template
	 *            The template to render
	 * @param webInterface
	 *            The Sone web interface
	 */
	public KnownSonesPage(Template template, WebInterface webInterface) {
		super("knownSones.html", template, "Page.KnownSones.Title", webInterface, false);
	}

	//
	// TEMPLATEPAGE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void processTemplate(Request request, TemplateContext templateContext) throws RedirectException {
		super.processTemplate(request, templateContext);
		String sortField = request.getHttpRequest().getParam("sort");
		String sortOrder = request.getHttpRequest().getParam("order");
		String followedSones = request.getHttpRequest().getParam("followedSones");
		templateContext.set("sort", (sortField != null) ? sortField : "name");
		templateContext.set("order", (sortOrder != null) ? sortOrder : "asc");
		templateContext.set("followedSones", followedSones);
		final Sone currentSone = getCurrentSone(request.getToadletContext(), false);
		List<Sone> knownSones = Filters.filteredList(new ArrayList<Sone>(webInterface.getCore().getSones()), Sone.EMPTY_SONE_FILTER);
		if ((currentSone != null) && "show-only".equals(followedSones)) {
			knownSones = Filters.filteredList(knownSones, new Filter<Sone>() {

				@Override
				public boolean filterObject(Sone sone) {
					return currentSone.hasFriend(sone.getId());
				}
			});
		} else if ((currentSone != null) && "hide".equals(followedSones)) {
			knownSones = Filters.filteredList(knownSones, new Filter<Sone>() {

				@Override
				public boolean filterObject(Sone sone) {
					return !currentSone.hasFriend(sone.getId());
				}
			});
		}
		if ("activity".equals(sortField)) {
			if ("asc".equals(sortOrder)) {
				Collections.sort(knownSones, new ReverseComparator<Sone>(Sone.LAST_ACTIVITY_COMPARATOR));
			} else {
				Collections.sort(knownSones, Sone.LAST_ACTIVITY_COMPARATOR);
			}
		} else if ("posts".equals(sortField)) {
			if ("asc".equals(sortOrder)) {
				Collections.sort(knownSones, new ReverseComparator<Sone>(Sone.POST_COUNT_COMPARATOR));
			} else {
				Collections.sort(knownSones, Sone.POST_COUNT_COMPARATOR);
			}
		} else {
			if ("desc".equals(sortOrder)) {
				Collections.sort(knownSones, new ReverseComparator<Sone>(Sone.NICE_NAME_COMPARATOR));
			} else {
				Collections.sort(knownSones, Sone.NICE_NAME_COMPARATOR);
			}
		}
		Pagination<Sone> sonePagination = new Pagination<Sone>(knownSones, 25).setPage(Numbers.safeParseInteger(request.getHttpRequest().getParam("page"), 0));
		templateContext.set("pagination", sonePagination);
		templateContext.set("knownSones", sonePagination.getItems());
	}
}
