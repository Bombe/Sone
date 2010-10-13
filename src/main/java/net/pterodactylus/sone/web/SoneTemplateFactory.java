/*
 * shortener - L10nTemplateFactory.java - Copyright © 2010 David Roden
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

import java.io.Reader;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import net.pterodactylus.util.template.DataProvider;
import net.pterodactylus.util.template.DefaultTemplateFactory;
import net.pterodactylus.util.template.Filter;
import net.pterodactylus.util.template.ReflectionAccessor;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateFactory;
import freenet.l10n.BaseL10n;

/**
 * {@link TemplateFactory} implementation that creates {@link Template}s that
 * have an {@link L10nFilter} added.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class SoneTemplateFactory implements TemplateFactory {

	/** The base template factory. */
	private final TemplateFactory templateFactory;

	/** The L10n filter. */
	private final L10nFilter l10nFilter;

	/** The date filter. */
	private final DateFilter dateFilter;

	/** The reflection accessor. */
	private final ReflectionAccessor reflectionAccessor = new ReflectionAccessor();

	/**
	 * Creates a new Freetalk template factory.
	 *
	 * @param l10n
	 *            The L10n handler
	 */
	public SoneTemplateFactory(BaseL10n l10n) {
		this(DefaultTemplateFactory.getInstance(), l10n);
	}

	/**
	 * Creates a new Freetalk template factory, retrieving templates from the
	 * given template factory, then adding all filters used by Freetalk to them.
	 *
	 * @param templateFactory
	 *            The base template factory
	 * @param l10n
	 *            The L10n handler
	 */
	public SoneTemplateFactory(TemplateFactory templateFactory, BaseL10n l10n) {
		this.templateFactory = templateFactory;
		this.l10nFilter = new L10nFilter(l10n);
		this.dateFilter = new DateFilter();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Template createTemplate(Reader templateSource) {
		Template template = templateFactory.createTemplate(templateSource);
		template.addAccessor(Object.class, reflectionAccessor);
		template.addFilter("l10n", l10nFilter);
		template.addFilter("date", dateFilter);
		return template;
	}

	/**
	 * {@link Filter} implementation replaces {@link String} values with their
	 * translated equivalents.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public static class L10nFilter implements Filter {

		/** The l10n handler. */
		private final BaseL10n l10n;

		/**
		 * Creates a new L10n filter.
		 *
		 * @param l10n
		 *            The l10n handler
		 */
		public L10nFilter(BaseL10n l10n) {
			this.l10n = l10n;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String format(DataProvider dataProvider, Object data, Map<String, String> parameters) {
			return l10n.getString(String.valueOf(data));
		}

	}

	/**
	 * {@link Filter} implementation that formats a date. The date may be given
	 * either as a {@link Date} or a {@link Long} object.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public static class DateFilter implements Filter {

		/** The date formatter. */
		private final DateFormat dateFormat = DateFormat.getInstance();

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String format(DataProvider dataProvider, Object data, Map<String, String> parameters) {
			if (data instanceof Date) {
				return dateFormat.format((Date) data);
			} else if (data instanceof Long) {
				return dateFormat.format(new Date((Long) data));
			}
			return "";
		}

	}

}
