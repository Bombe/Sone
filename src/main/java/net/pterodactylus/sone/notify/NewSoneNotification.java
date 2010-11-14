/*
 * Sone - NewSoneNotification.java - Copyright © 2010 David Roden
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

package net.pterodactylus.sone.notify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.util.notify.TemplateNotification;
import net.pterodactylus.util.template.Template;

/**
 * TODO
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class NewSoneNotification extends TemplateNotification {

	private List<Sone> newSones = Collections.synchronizedList(new ArrayList<Sone>());

	/**
	 * TODO
	 */
	public NewSoneNotification(Template template) {
		super(template);
	}

	//
	// ACCESSORS
	//

	public boolean isEmpty() {
		return newSones.isEmpty();
	}

	public void addSone(Sone sone) {
		newSones.add(sone);
		touch();
	}

	//
	// ABSTRACTNOTIFICATION METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dismiss() {
		super.dismiss();
		newSones.clear();
	}

}
