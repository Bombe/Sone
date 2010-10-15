/*
 * FreenetSone - WebInterface.java - Copyright © 2010 David Roden
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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.L10nFilter;
import net.pterodactylus.sone.main.SonePlugin;
import net.pterodactylus.sone.template.PostAccessor;
import net.pterodactylus.sone.template.SoneAccessor;
import net.pterodactylus.sone.web.page.PageToadlet;
import net.pterodactylus.sone.web.page.PageToadletFactory;
import net.pterodactylus.sone.web.page.StaticPage;
import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.service.AbstractService;
import net.pterodactylus.util.template.DateFilter;
import net.pterodactylus.util.template.DefaultTemplateFactory;
import net.pterodactylus.util.template.ReflectionAccessor;
import net.pterodactylus.util.template.Template;
import freenet.clients.http.LinkEnabledCallback;
import freenet.clients.http.SessionManager;
import freenet.clients.http.ToadletContainer;
import freenet.clients.http.ToadletContext;
import freenet.l10n.BaseL10n;

/**
 * Bundles functionality that a web interface of a Freenet plugin needs, e.g.
 * references to l10n helpers.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class WebInterface extends AbstractService {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(WebInterface.class);

	/** The Sone plugin. */
	private final SonePlugin sonePlugin;

	/** The registered toadlets. */
	private final List<PageToadlet> pageToadlets = new ArrayList<PageToadlet>();

	/**
	 * Creates a new web interface.
	 *
	 * @param sonePlugin
	 *            The Sone plugin
	 */
	public WebInterface(SonePlugin sonePlugin) {
		super("Sone Web Interface");
		this.sonePlugin = sonePlugin;
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the Sone core used by the Sone plugin.
	 *
	 * @return The Sone core
	 */
	public Core core() {
		return sonePlugin.core();
	}

	/**
	 * Returns the l10n helper of the node.
	 *
	 * @return The node’s l10n helper
	 */
	public BaseL10n l10n() {
		return sonePlugin.l10n().getBase();
	}

	/**
	 * Returns the session manager of the node.
	 *
	 * @return The node’s session manager
	 */
	public SessionManager sessionManager() {
		try {
			return sonePlugin.pluginRespirator().getSessionManager(new URI("/"));
		} catch (URISyntaxException use1) {
			logger.log(Level.SEVERE, "Could not get Session Manager!", use1);
			return null;
		}
	}

	//
	// SERVICE METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void serviceStart() {
		registerToadlets();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void serviceStop() {
		unregisterToadlets();
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Register all toadlets.
	 */
	private void registerToadlets() {
		DefaultTemplateFactory templateFactory = new DefaultTemplateFactory();
		templateFactory.addAccessor(Object.class, new ReflectionAccessor());
		templateFactory.addAccessor(Sone.class, new SoneAccessor());
		templateFactory.addAccessor(Post.class, new PostAccessor(core()));
		templateFactory.addFilter("date", new DateFilter());
		templateFactory.addFilter("l10n", new L10nFilter(l10n()));

		String formPassword = sonePlugin.pluginRespirator().getToadletContainer().getFormPassword();

		Template loginTemplate = templateFactory.createTemplate(createReader("/templates/login.html"));
		loginTemplate.set("formPassword", formPassword);

		Template indexTemplate = templateFactory.createTemplate(createReader("/templates/index.html"));
		indexTemplate.set("formPassword", formPassword);

		Template createSoneTemplate = templateFactory.createTemplate(createReader("/templates/createSone.html"));
		createSoneTemplate.set("formPassword", formPassword);

		Template createPostTemplate = templateFactory.createTemplate(createReader("/templates/createPost.html"));
		createPostTemplate.set("formPassword", formPassword);

		Template editProfileTemplate = templateFactory.createTemplate(createReader("/templates/editProfile.html"));
		editProfileTemplate.set("formPassword", formPassword);

		Template viewSoneTemplate = templateFactory.createTemplate(createReader("/templates/viewSone.html"));
		viewSoneTemplate.set("formPassword", formPassword);

		Template followSoneTemplate = templateFactory.createTemplate(createReader("/templates/followSone.html"));
		followSoneTemplate.set("formPassword", formPassword);

		Template deleteSoneTemplate = templateFactory.createTemplate(createReader("/templates/deleteSone.html"));
		deleteSoneTemplate.set("formPassword", formPassword);

		Template logoutTemplate = templateFactory.createTemplate(createReader("/templates/logout.html"));

		PageToadletFactory pageToadletFactory = new PageToadletFactory(sonePlugin.pluginRespirator().getHLSimpleClient(), "/Sone/");
		pageToadlets.add(pageToadletFactory.createPageToadlet(new IndexPage(indexTemplate, this), "Index"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new CreateSonePage(createSoneTemplate, this), "CreateSone"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new EditProfilePage(editProfileTemplate, this), "EditProfile"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new CreatePostPage(createPostTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new ViewSonePage(viewSoneTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new FollowSonePage(followSoneTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new DeleteSonePage(deleteSoneTemplate, this), "DeleteSone"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new LoginPage(loginTemplate, this), "Login"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new LogoutPage(logoutTemplate, this), "Logout"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new StaticPage("css/", "/static/css/", "text/css")));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new StaticPage("javascript/", "/static/javascript/", "text/javascript")));

		ToadletContainer toadletContainer = sonePlugin.pluginRespirator().getToadletContainer();
		toadletContainer.getPageMaker().addNavigationCategory("/Sone/index.html", "Navigation.Menu.Name", "Navigation.Menu.Tooltip", sonePlugin);
		for (PageToadlet toadlet : pageToadlets) {
			String menuName = toadlet.getMenuName();
			if (menuName != null) {
				toadletContainer.register(toadlet, "Navigation.Menu.Name", toadlet.path(), true, "Navigation.Menu.Item." + menuName + ".Name", "Navigation.Menu.Item." + menuName + ".Tooltip", false, toadlet);
			} else {
				toadletContainer.register(toadlet, null, toadlet.path(), true, false);
			}
		}
	}

	/**
	 * Unregisters all toadlets.
	 */
	private void unregisterToadlets() {
		ToadletContainer toadletContainer = sonePlugin.pluginRespirator().getToadletContainer();
		for (PageToadlet pageToadlet : pageToadlets) {
			toadletContainer.unregister(pageToadlet);
		}
		toadletContainer.getPageMaker().removeNavigationCategory("Navigation.Menu.Name");
	}

	/**
	 * Creates a {@link Reader} from the {@link InputStream} for the resource
	 * with the given name.
	 *
	 * @param resourceName
	 *            The name of the resource
	 * @return A {@link Reader} for the resource
	 */
	private Reader createReader(String resourceName) {
		try {
			return new InputStreamReader(getClass().getResourceAsStream(resourceName), "UTF-8");
		} catch (UnsupportedEncodingException uee1) {
			return null;
		}
	}

	/**
	 * {@link LinkEnabledCallback} implementation that always returns
	 * {@code true} when {@link LinkEnabledCallback#isEnabled(ToadletContext)}
	 * is called.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public class AlwaysEnabledCallback implements LinkEnabledCallback {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isEnabled(ToadletContext toadletContext) {
			return true;
		}
	}

}
