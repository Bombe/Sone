/*
 * Sone - KnownSonesPage.java - Copyright © 2010–2013 David Roden
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.util.collection.Pagination;
import net.pterodactylus.util.number.Numbers;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Ordering;

/**
 * This page shows all known Sones.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class KnownSonesPage extends SoneTemplatePage {

	private static final String defaultSortField = "activity";
	private static final String defaultSortOrder = "desc";

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
	protected void processTemplate(FreenetRequest request, TemplateContext templateContext) throws RedirectException {
		super.processTemplate(request, templateContext);
		String sortField = request.getHttpRequest().getParam("sort", defaultSortField);
		String sortOrder = request.getHttpRequest().getParam("order", defaultSortOrder);
		String filter = request.getHttpRequest().getParam("filter");
		templateContext.set("sort", sortField);
		templateContext.set("order", sortOrder);
		templateContext.set("filter", filter);
		final Sone currentSone = getCurrentSone(request.getToadletContext(), false);
		Collection<Sone> knownSones = Collections2.filter(webInterface.getCore().getSones(), Sone.EMPTY_SONE_FILTER);
		if ((currentSone != null) && "followed".equals(filter)) {
			knownSones = Collections2.filter(knownSones, new Predicate<Sone>() {

				@Override
				public boolean apply(Sone sone) {
					return currentSone.hasFriend(sone.getId());
				}
			});
		} else if ((currentSone != null) && "not-followed".equals(filter)) {
			knownSones = Collections2.filter(knownSones, new Predicate<Sone>() {

				@Override
				public boolean apply(Sone sone) {
					return !currentSone.hasFriend(sone.getId());
				}
			});
		} else if ("new".equals(filter)) {
			knownSones = Collections2.filter(knownSones, new Predicate<Sone>() {

				/**
				 * {@inheritDoc}
				 */
				@Override
				public boolean apply(Sone sone) {
					return !sone.isKnown();
				}
			});
		} else if ("not-new".equals(filter)) {
			knownSones = Collections2.filter(knownSones, new Predicate<Sone>() {

				/**
				 * {@inheritDoc}
				 */
				@Override
				public boolean apply(Sone sone) {
					return sone.isKnown();
				}
			});
		} else if ("own".equals(filter)) {
			knownSones = Collections2.filter(knownSones, Sone.LOCAL_SONE_FILTER);
		} else if ("not-own".equals(filter)) {
			knownSones = Collections2.filter(knownSones, Predicates.not(Sone.LOCAL_SONE_FILTER));
		}
		List<Sone> sortedSones = new ArrayList<Sone>(knownSones);
		if ("activity".equals(sortField)) {
			if ("asc".equals(sortOrder)) {
				Collections.sort(sortedSones, Ordering.from(Sone.LAST_ACTIVITY_COMPARATOR).reverse());
			} else {
				Collections.sort(sortedSones, Sone.LAST_ACTIVITY_COMPARATOR);
			}
		} else if ("posts".equals(sortField)) {
			if ("asc".equals(sortOrder)) {
				Collections.sort(sortedSones, Ordering.from(Sone.POST_COUNT_COMPARATOR).reverse());
			} else {
				Collections.sort(sortedSones, Sone.POST_COUNT_COMPARATOR);
			}
		} else if ("images".equals(sortField)) {
			if ("asc".equals(sortOrder)) {
				Collections.sort(sortedSones, Ordering.from(Sone.IMAGE_COUNT_COMPARATOR).reverse());
			} else {
				Collections.sort(sortedSones, Sone.IMAGE_COUNT_COMPARATOR);
			}
		} else {
			if ("desc".equals(sortOrder)) {
				Collections.sort(sortedSones, Ordering.from(Sone.NICE_NAME_COMPARATOR).reverse());
			} else {
				Collections.sort(sortedSones, Sone.NICE_NAME_COMPARATOR);
			}
		}
		Pagination<Sone> sonePagination = new Pagination<Sone>(sortedSones, 25).setPage(Numbers.safeParseInteger(request.getHttpRequest().getParam("page"), 0));
		templateContext.set("pagination", sonePagination);
		templateContext.set("knownSones", sonePagination.getItems());
	}

}
