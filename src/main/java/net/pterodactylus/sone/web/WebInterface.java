/*
 * Sone - WebInterface.java - Copyright © 2010–2012 David Roden
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.core.CoreListener;
import net.pterodactylus.sone.core.event.MarkPostKnownEvent;
import net.pterodactylus.sone.core.event.MarkPostReplyKnownEvent;
import net.pterodactylus.sone.core.event.MarkSoneKnownEvent;
import net.pterodactylus.sone.core.event.NewPostFoundEvent;
import net.pterodactylus.sone.core.event.NewPostReplyFoundEvent;
import net.pterodactylus.sone.core.event.NewSoneFoundEvent;
import net.pterodactylus.sone.core.event.PostRemovedEvent;
import net.pterodactylus.sone.core.event.PostReplyRemovedEvent;
import net.pterodactylus.sone.core.event.SoneInsertAbortedEvent;
import net.pterodactylus.sone.core.event.SoneInsertedEvent;
import net.pterodactylus.sone.core.event.SoneInsertingEvent;
import net.pterodactylus.sone.core.event.SoneLockedEvent;
import net.pterodactylus.sone.core.event.SoneRemovedEvent;
import net.pterodactylus.sone.core.event.SoneUnlockedEvent;
import net.pterodactylus.sone.core.event.UpdateFoundEvent;
import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Reply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.L10nFilter;
import net.pterodactylus.sone.freenet.wot.Identity;
import net.pterodactylus.sone.freenet.wot.Trust;
import net.pterodactylus.sone.main.SonePlugin;
import net.pterodactylus.sone.notify.ListNotification;
import net.pterodactylus.sone.template.AlbumAccessor;
import net.pterodactylus.sone.template.CollectionAccessor;
import net.pterodactylus.sone.template.CssClassNameFilter;
import net.pterodactylus.sone.template.HttpRequestAccessor;
import net.pterodactylus.sone.template.IdentityAccessor;
import net.pterodactylus.sone.template.ImageAccessor;
import net.pterodactylus.sone.template.ImageLinkFilter;
import net.pterodactylus.sone.template.JavascriptFilter;
import net.pterodactylus.sone.template.ParserFilter;
import net.pterodactylus.sone.template.PostAccessor;
import net.pterodactylus.sone.template.ProfileAccessor;
import net.pterodactylus.sone.template.ReplyAccessor;
import net.pterodactylus.sone.template.ReplyGroupFilter;
import net.pterodactylus.sone.template.RequestChangeFilter;
import net.pterodactylus.sone.template.SoneAccessor;
import net.pterodactylus.sone.template.SubstringFilter;
import net.pterodactylus.sone.template.TrustAccessor;
import net.pterodactylus.sone.template.UniqueElementFilter;
import net.pterodactylus.sone.template.UnknownDateFilter;
import net.pterodactylus.sone.text.Part;
import net.pterodactylus.sone.text.SonePart;
import net.pterodactylus.sone.text.SoneTextParser;
import net.pterodactylus.sone.web.ajax.BookmarkAjaxPage;
import net.pterodactylus.sone.web.ajax.CreatePostAjaxPage;
import net.pterodactylus.sone.web.ajax.CreateReplyAjaxPage;
import net.pterodactylus.sone.web.ajax.DeletePostAjaxPage;
import net.pterodactylus.sone.web.ajax.DeleteProfileFieldAjaxPage;
import net.pterodactylus.sone.web.ajax.DeleteReplyAjaxPage;
import net.pterodactylus.sone.web.ajax.DismissNotificationAjaxPage;
import net.pterodactylus.sone.web.ajax.DistrustAjaxPage;
import net.pterodactylus.sone.web.ajax.EditAlbumAjaxPage;
import net.pterodactylus.sone.web.ajax.EditImageAjaxPage;
import net.pterodactylus.sone.web.ajax.EditProfileFieldAjaxPage;
import net.pterodactylus.sone.web.ajax.FollowSoneAjaxPage;
import net.pterodactylus.sone.web.ajax.GetLikesAjaxPage;
import net.pterodactylus.sone.web.ajax.GetNotificationsAjaxPage;
import net.pterodactylus.sone.web.ajax.GetPostAjaxPage;
import net.pterodactylus.sone.web.ajax.GetReplyAjaxPage;
import net.pterodactylus.sone.web.ajax.GetStatusAjaxPage;
import net.pterodactylus.sone.web.ajax.GetTimesAjaxPage;
import net.pterodactylus.sone.web.ajax.GetTranslationPage;
import net.pterodactylus.sone.web.ajax.LikeAjaxPage;
import net.pterodactylus.sone.web.ajax.LockSoneAjaxPage;
import net.pterodactylus.sone.web.ajax.MarkAsKnownAjaxPage;
import net.pterodactylus.sone.web.ajax.MoveProfileFieldAjaxPage;
import net.pterodactylus.sone.web.ajax.TrustAjaxPage;
import net.pterodactylus.sone.web.ajax.UnbookmarkAjaxPage;
import net.pterodactylus.sone.web.ajax.UnfollowSoneAjaxPage;
import net.pterodactylus.sone.web.ajax.UnlikeAjaxPage;
import net.pterodactylus.sone.web.ajax.UnlockSoneAjaxPage;
import net.pterodactylus.sone.web.ajax.UntrustAjaxPage;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.sone.web.page.PageToadlet;
import net.pterodactylus.sone.web.page.PageToadletFactory;
import net.pterodactylus.util.logging.Logging;
import net.pterodactylus.util.notify.Notification;
import net.pterodactylus.util.notify.NotificationManager;
import net.pterodactylus.util.notify.TemplateNotification;
import net.pterodactylus.util.template.ClassPathTemplateProvider;
import net.pterodactylus.util.template.CollectionSortFilter;
import net.pterodactylus.util.template.ContainsFilter;
import net.pterodactylus.util.template.DateFilter;
import net.pterodactylus.util.template.FormatFilter;
import net.pterodactylus.util.template.HtmlFilter;
import net.pterodactylus.util.template.MatchFilter;
import net.pterodactylus.util.template.ModFilter;
import net.pterodactylus.util.template.PaginationFilter;
import net.pterodactylus.util.template.ReflectionAccessor;
import net.pterodactylus.util.template.ReplaceFilter;
import net.pterodactylus.util.template.StoreFilter;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContextFactory;
import net.pterodactylus.util.template.TemplateParser;
import net.pterodactylus.util.template.TemplateProvider;
import net.pterodactylus.util.template.XmlFilter;
import net.pterodactylus.util.thread.Ticker;
import net.pterodactylus.util.web.RedirectPage;
import net.pterodactylus.util.web.StaticPage;
import net.pterodactylus.util.web.TemplatePage;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import freenet.clients.http.SessionManager;
import freenet.clients.http.SessionManager.Session;
import freenet.clients.http.ToadletContainer;
import freenet.clients.http.ToadletContext;
import freenet.l10n.BaseL10n;
import freenet.support.api.HTTPRequest;

/**
 * Bundles functionality that a web interface of a Freenet plugin needs, e.g.
 * references to l10n helpers.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class WebInterface implements CoreListener {

	/** The logger. */
	private static final Logger logger = Logging.getLogger(WebInterface.class);

	/** The notification manager. */
	private final NotificationManager notificationManager = new NotificationManager();

	/** The Sone plugin. */
	private final SonePlugin sonePlugin;

	/** The registered toadlets. */
	private final List<PageToadlet> pageToadlets = new ArrayList<PageToadlet>();

	/** The form password. */
	private final String formPassword;

	/** The template context factory. */
	private final TemplateContextFactory templateContextFactory;

	/** The Sone text parser. */
	private final SoneTextParser soneTextParser;

	/** The parser filter. */
	private final ParserFilter parserFilter;

	/** The “new Sone” notification. */
	private final ListNotification<Sone> newSoneNotification;

	/** The “new post” notification. */
	private final ListNotification<Post> newPostNotification;

	/** The “new reply” notification. */
	private final ListNotification<PostReply> newReplyNotification;

	/** The invisible “local post” notification. */
	private final ListNotification<Post> localPostNotification;

	/** The invisible “local reply” notification. */
	private final ListNotification<PostReply> localReplyNotification;

	/** The “you have been mentioned” notification. */
	private final ListNotification<Post> mentionNotification;

	/** Notifications for sone inserts. */
	private final Map<Sone, TemplateNotification> soneInsertNotifications = new HashMap<Sone, TemplateNotification>();

	/** Sone locked notification ticker objects. */
	private final Map<Sone, Object> lockedSonesTickerObjects = Collections.synchronizedMap(new HashMap<Sone, Object>());

	/** The “Sone locked” notification. */
	private final ListNotification<Sone> lockedSonesNotification;

	/** The “new version” notification. */
	private final TemplateNotification newVersionNotification;

	/** The “inserting images” notification. */
	private final ListNotification<Image> insertingImagesNotification;

	/** The “inserted images” notification. */
	private final ListNotification<Image> insertedImagesNotification;

	/** The “image insert failed” notification. */
	private final ListNotification<Image> imageInsertFailedNotification;

	/**
	 * Creates a new web interface.
	 *
	 * @param sonePlugin
	 *            The Sone plugin
	 */
	@Inject
	public WebInterface(SonePlugin sonePlugin) {
		this.sonePlugin = sonePlugin;
		formPassword = sonePlugin.pluginRespirator().getToadletContainer().getFormPassword();
		soneTextParser = new SoneTextParser(getCore(), getCore());

		templateContextFactory = new TemplateContextFactory();
		templateContextFactory.addAccessor(Object.class, new ReflectionAccessor());
		templateContextFactory.addAccessor(Collection.class, new CollectionAccessor());
		templateContextFactory.addAccessor(Sone.class, new SoneAccessor(getCore()));
		templateContextFactory.addAccessor(Post.class, new PostAccessor(getCore()));
		templateContextFactory.addAccessor(Reply.class, new ReplyAccessor(getCore()));
		templateContextFactory.addAccessor(Album.class, new AlbumAccessor());
		templateContextFactory.addAccessor(Image.class, new ImageAccessor());
		templateContextFactory.addAccessor(Identity.class, new IdentityAccessor(getCore()));
		templateContextFactory.addAccessor(Trust.class, new TrustAccessor());
		templateContextFactory.addAccessor(HTTPRequest.class, new HttpRequestAccessor());
		templateContextFactory.addAccessor(Profile.class, new ProfileAccessor(getCore()));
		templateContextFactory.addFilter("date", new DateFilter());
		templateContextFactory.addFilter("html", new HtmlFilter());
		templateContextFactory.addFilter("replace", new ReplaceFilter());
		templateContextFactory.addFilter("store", new StoreFilter());
		templateContextFactory.addFilter("l10n", new L10nFilter(this));
		templateContextFactory.addFilter("substring", new SubstringFilter());
		templateContextFactory.addFilter("xml", new XmlFilter());
		templateContextFactory.addFilter("change", new RequestChangeFilter());
		templateContextFactory.addFilter("match", new MatchFilter());
		templateContextFactory.addFilter("css", new CssClassNameFilter());
		templateContextFactory.addFilter("js", new JavascriptFilter());
		templateContextFactory.addFilter("parse", parserFilter = new ParserFilter(getCore(), templateContextFactory, soneTextParser));
		templateContextFactory.addFilter("unknown", new UnknownDateFilter(getL10n(), "View.Sone.Text.UnknownDate"));
		templateContextFactory.addFilter("format", new FormatFilter());
		templateContextFactory.addFilter("sort", new CollectionSortFilter());
		templateContextFactory.addFilter("image-link", new ImageLinkFilter(getCore(), templateContextFactory));
		templateContextFactory.addFilter("replyGroup", new ReplyGroupFilter());
		templateContextFactory.addFilter("in", new ContainsFilter());
		templateContextFactory.addFilter("unique", new UniqueElementFilter());
		templateContextFactory.addFilter("mod", new ModFilter());
		templateContextFactory.addFilter("paginate", new PaginationFilter());
		templateContextFactory.addProvider(TemplateProvider.TEMPLATE_CONTEXT_PROVIDER);
		templateContextFactory.addProvider(new ClassPathTemplateProvider(WebInterface.class, "/templates/"));
		templateContextFactory.addTemplateObject("webInterface", this);
		templateContextFactory.addTemplateObject("formPassword", formPassword);

		/* create notifications. */
		Template newSoneNotificationTemplate = TemplateParser.parse(createReader("/templates/notify/newSoneNotification.html"));
		newSoneNotification = new ListNotification<Sone>("new-sone-notification", "sones", newSoneNotificationTemplate, false);

		Template newPostNotificationTemplate = TemplateParser.parse(createReader("/templates/notify/newPostNotification.html"));
		newPostNotification = new ListNotification<Post>("new-post-notification", "posts", newPostNotificationTemplate, false);

		Template localPostNotificationTemplate = TemplateParser.parse(createReader("/templates/notify/newPostNotification.html"));
		localPostNotification = new ListNotification<Post>("local-post-notification", "posts", localPostNotificationTemplate, false);

		Template newReplyNotificationTemplate = TemplateParser.parse(createReader("/templates/notify/newReplyNotification.html"));
		newReplyNotification = new ListNotification<PostReply>("new-reply-notification", "replies", newReplyNotificationTemplate, false);

		Template localReplyNotificationTemplate = TemplateParser.parse(createReader("/templates/notify/newReplyNotification.html"));
		localReplyNotification = new ListNotification<PostReply>("local-reply-notification", "replies", localReplyNotificationTemplate, false);

		Template mentionNotificationTemplate = TemplateParser.parse(createReader("/templates/notify/mentionNotification.html"));
		mentionNotification = new ListNotification<Post>("mention-notification", "posts", mentionNotificationTemplate, false);

		Template lockedSonesTemplate = TemplateParser.parse(createReader("/templates/notify/lockedSonesNotification.html"));
		lockedSonesNotification = new ListNotification<Sone>("sones-locked-notification", "sones", lockedSonesTemplate);

		Template newVersionTemplate = TemplateParser.parse(createReader("/templates/notify/newVersionNotification.html"));
		newVersionNotification = new TemplateNotification("new-version-notification", newVersionTemplate);

		Template insertingImagesTemplate = TemplateParser.parse(createReader("/templates/notify/inserting-images-notification.html"));
		insertingImagesNotification = new ListNotification<Image>("inserting-images-notification", "images", insertingImagesTemplate);

		Template insertedImagesTemplate = TemplateParser.parse(createReader("/templates/notify/inserted-images-notification.html"));
		insertedImagesNotification = new ListNotification<Image>("inserted-images-notification", "images", insertedImagesTemplate);

		Template imageInsertFailedTemplate = TemplateParser.parse(createReader("/templates/notify/image-insert-failed-notification.html"));
		imageInsertFailedNotification = new ListNotification<Image>("image-insert-failed-notification", "images", imageInsertFailedTemplate);
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the Sone core used by the Sone plugin.
	 *
	 * @return The Sone core
	 */
	public Core getCore() {
		return sonePlugin.core();
	}

	/**
	 * Returns the template context factory of the web interface.
	 *
	 * @return The template context factory
	 */
	public TemplateContextFactory getTemplateContextFactory() {
		return templateContextFactory;
	}

	/**
	 * Returns the current session, creating a new session if there is no
	 * current session.
	 *
	 * @param toadletContenxt
	 *            The toadlet context
	 * @return The current session, or {@code null} if there is no current
	 *         session
	 */
	public Session getCurrentSession(ToadletContext toadletContenxt) {
		return getCurrentSession(toadletContenxt, true);
	}

	/**
	 * Returns the current session, creating a new session if there is no
	 * current session and {@code create} is {@code true}.
	 *
	 * @param toadletContenxt
	 *            The toadlet context
	 * @param create
	 *            {@code true} to create a new session if there is no current
	 *            session, {@code false} otherwise
	 * @return The current session, or {@code null} if there is no current
	 *         session
	 */
	public Session getCurrentSession(ToadletContext toadletContenxt, boolean create) {
		Session session = getSessionManager().useSession(toadletContenxt);
		if (create && (session == null)) {
			session = getSessionManager().createSession(UUID.randomUUID().toString(), toadletContenxt);
		}
		return session;
	}

	/**
	 * Returns the currently logged in Sone.
	 *
	 * @param toadletContext
	 *            The toadlet context
	 * @return The currently logged in Sone, or {@code null} if no Sone is
	 *         currently logged in
	 */
	public Sone getCurrentSone(ToadletContext toadletContext) {
		return getCurrentSone(toadletContext, true);
	}

	/**
	 * Returns the currently logged in Sone.
	 *
	 * @param toadletContext
	 *            The toadlet context
	 * @param create
	 *            {@code true} to create a new session if no session exists,
	 *            {@code false} to not create a new session
	 * @return The currently logged in Sone, or {@code null} if no Sone is
	 *         currently logged in
	 */
	public Sone getCurrentSone(ToadletContext toadletContext, boolean create) {
		Collection<Sone> localSones = getCore().getLocalSones();
		if (localSones.size() == 1) {
			return localSones.iterator().next();
		}
		return getCurrentSone(getCurrentSession(toadletContext, create));
	}

	/**
	 * Returns the currently logged in Sone.
	 *
	 * @param session
	 *            The session
	 * @return The currently logged in Sone, or {@code null} if no Sone is
	 *         currently logged in
	 */
	public Sone getCurrentSone(Session session) {
		if (session == null) {
			return null;
		}
		String soneId = (String) session.getAttribute("Sone.CurrentSone");
		if (soneId == null) {
			return null;
		}
		return getCore().getLocalSone(soneId, false);
	}

	/**
	 * Sets the currently logged in Sone.
	 *
	 * @param toadletContext
	 *            The toadlet context
	 * @param sone
	 *            The Sone to set as currently logged in
	 */
	public void setCurrentSone(ToadletContext toadletContext, Sone sone) {
		Session session = getCurrentSession(toadletContext);
		if (sone == null) {
			session.removeAttribute("Sone.CurrentSone");
		} else {
			session.setAttribute("Sone.CurrentSone", sone.getId());
		}
	}

	/**
	 * Returns the notification manager.
	 *
	 * @return The notification manager
	 */
	public NotificationManager getNotifications() {
		return notificationManager;
	}

	/**
	 * Returns the l10n helper of the node.
	 *
	 * @return The node’s l10n helper
	 */
	public BaseL10n getL10n() {
		return sonePlugin.l10n().getBase();
	}

	/**
	 * Returns the session manager of the node.
	 *
	 * @return The node’s session manager
	 */
	public SessionManager getSessionManager() {
		return sonePlugin.pluginRespirator().getSessionManager("Sone");
	}

	/**
	 * Returns the node’s form password.
	 *
	 * @return The form password
	 */
	public String getFormPassword() {
		return formPassword;
	}

	/**
	 * Returns the posts that have been announced as new in the
	 * {@link #newPostNotification}.
	 *
	 * @return The new posts
	 */
	public Set<Post> getNewPosts() {
		return ImmutableSet.<Post> builder().addAll(newPostNotification.getElements()).addAll(localPostNotification.getElements()).build();
	}

	/**
	 * Returns the replies that have been announced as new in the
	 * {@link #newReplyNotification}.
	 *
	 * @return The new replies
	 */
	public Set<PostReply> getNewReplies() {
		return ImmutableSet.<PostReply> builder().addAll(newReplyNotification.getElements()).addAll(localReplyNotification.getElements()).build();
	}

	/**
	 * Sets whether the current start of the plugin is the first start. It is
	 * considered a first start if the configuration file does not exist.
	 *
	 * @param firstStart
	 *            {@code true} if no configuration file existed when Sone was
	 *            loaded, {@code false} otherwise
	 */
	public void setFirstStart(boolean firstStart) {
		if (firstStart) {
			Template firstStartNotificationTemplate = TemplateParser.parse(createReader("/templates/notify/firstStartNotification.html"));
			Notification firstStartNotification = new TemplateNotification("first-start-notification", firstStartNotificationTemplate);
			notificationManager.addNotification(firstStartNotification);
		}
	}

	/**
	 * Sets whether Sone was started with a fresh configuration file.
	 *
	 * @param newConfig
	 *            {@code true} if Sone was started with a fresh configuration,
	 *            {@code false} if the existing configuration could be read
	 */
	public void setNewConfig(boolean newConfig) {
		if (newConfig && !hasFirstStartNotification()) {
			Template configNotReadNotificationTemplate = TemplateParser.parse(createReader("/templates/notify/configNotReadNotification.html"));
			Notification configNotReadNotification = new TemplateNotification("config-not-read-notification", configNotReadNotificationTemplate);
			notificationManager.addNotification(configNotReadNotification);
		}
	}

	//
	// PRIVATE ACCESSORS
	//

	/**
	 * Returns whether the first start notification is currently displayed.
	 *
	 * @return {@code true} if the first-start notification is currently
	 *         displayed, {@code false} otherwise
	 */
	private boolean hasFirstStartNotification() {
		return notificationManager.getNotification("first-start-notification") != null;
	}

	//
	// ACTIONS
	//

	/**
	 * Starts the web interface and registers all toadlets.
	 */
	public void start() {
		registerToadlets();

		/* notification templates. */
		Template startupNotificationTemplate = TemplateParser.parse(createReader("/templates/notify/startupNotification.html"));

		final TemplateNotification startupNotification = new TemplateNotification("startup-notification", startupNotificationTemplate);
		notificationManager.addNotification(startupNotification);

		Ticker.getInstance().registerEvent(System.currentTimeMillis() + (120 * 1000), new Runnable() {

			@Override
			public void run() {
				startupNotification.dismiss();
			}
		}, "Sone Startup Notification Remover");

		Template wotMissingNotificationTemplate = TemplateParser.parse(createReader("/templates/notify/wotMissingNotification.html"));
		final TemplateNotification wotMissingNotification = new TemplateNotification("wot-missing-notification", wotMissingNotificationTemplate);
		Ticker.getInstance().registerEvent(System.currentTimeMillis() + (15 * 1000), new Runnable() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void run() {
				if (getCore().getIdentityManager().isConnected()) {
					wotMissingNotification.dismiss();
				} else {
					notificationManager.addNotification(wotMissingNotification);
				}
				Ticker.getInstance().registerEvent(System.currentTimeMillis() + (15 * 1000), this, "Sone WoT Connector Checker");
			}

		}, "Sone WoT Connector Checker");
	}

	/**
	 * Stops the web interface and unregisters all toadlets.
	 */
	public void stop() {
		unregisterToadlets();
		Ticker.getInstance().stop();
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Register all toadlets.
	 */
	private void registerToadlets() {
		Template emptyTemplate = TemplateParser.parse(new StringReader(""));
		Template loginTemplate = TemplateParser.parse(createReader("/templates/login.html"));
		Template indexTemplate = TemplateParser.parse(createReader("/templates/index.html"));
		Template newTemplate = TemplateParser.parse(createReader("/templates/new.html"));
		Template knownSonesTemplate = TemplateParser.parse(createReader("/templates/knownSones.html"));
		Template createSoneTemplate = TemplateParser.parse(createReader("/templates/createSone.html"));
		Template createPostTemplate = TemplateParser.parse(createReader("/templates/createPost.html"));
		Template createReplyTemplate = TemplateParser.parse(createReader("/templates/createReply.html"));
		Template bookmarksTemplate = TemplateParser.parse(createReader("/templates/bookmarks.html"));
		Template searchTemplate = TemplateParser.parse(createReader("/templates/search.html"));
		Template editProfileTemplate = TemplateParser.parse(createReader("/templates/editProfile.html"));
		Template editProfileFieldTemplate = TemplateParser.parse(createReader("/templates/editProfileField.html"));
		Template deleteProfileFieldTemplate = TemplateParser.parse(createReader("/templates/deleteProfileField.html"));
		Template viewSoneTemplate = TemplateParser.parse(createReader("/templates/viewSone.html"));
		Template viewPostTemplate = TemplateParser.parse(createReader("/templates/viewPost.html"));
		Template deletePostTemplate = TemplateParser.parse(createReader("/templates/deletePost.html"));
		Template deleteReplyTemplate = TemplateParser.parse(createReader("/templates/deleteReply.html"));
		Template deleteSoneTemplate = TemplateParser.parse(createReader("/templates/deleteSone.html"));
		Template imageBrowserTemplate = TemplateParser.parse(createReader("/templates/imageBrowser.html"));
		Template createAlbumTemplate = TemplateParser.parse(createReader("/templates/createAlbum.html"));
		Template deleteAlbumTemplate = TemplateParser.parse(createReader("/templates/deleteAlbum.html"));
		Template deleteImageTemplate = TemplateParser.parse(createReader("/templates/deleteImage.html"));
		Template noPermissionTemplate = TemplateParser.parse(createReader("/templates/noPermission.html"));
		Template optionsTemplate = TemplateParser.parse(createReader("/templates/options.html"));
		Template rescueTemplate = TemplateParser.parse(createReader("/templates/rescue.html"));
		Template aboutTemplate = TemplateParser.parse(createReader("/templates/about.html"));
		Template invalidTemplate = TemplateParser.parse(createReader("/templates/invalid.html"));
		Template postTemplate = TemplateParser.parse(createReader("/templates/include/viewPost.html"));
		Template replyTemplate = TemplateParser.parse(createReader("/templates/include/viewReply.html"));
		Template openSearchTemplate = TemplateParser.parse(createReader("/templates/xml/OpenSearch.xml"));

		PageToadletFactory pageToadletFactory = new PageToadletFactory(sonePlugin.pluginRespirator().getHLSimpleClient(), "/Sone/");
		pageToadlets.add(pageToadletFactory.createPageToadlet(new RedirectPage<FreenetRequest>("", "index.html")));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new IndexPage(indexTemplate, this), "Index"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new NewPage(newTemplate, this), "New"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new CreateSonePage(createSoneTemplate, this), "CreateSone"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new KnownSonesPage(knownSonesTemplate, this), "KnownSones"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new EditProfilePage(editProfileTemplate, this), "EditProfile"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new EditProfileFieldPage(editProfileFieldTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new DeleteProfileFieldPage(deleteProfileFieldTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new CreatePostPage(createPostTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new CreateReplyPage(createReplyTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new ViewSonePage(viewSoneTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new ViewPostPage(viewPostTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new LikePage(emptyTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new UnlikePage(emptyTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new DeletePostPage(deletePostTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new DeleteReplyPage(deleteReplyTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new LockSonePage(emptyTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new UnlockSonePage(emptyTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new FollowSonePage(emptyTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new UnfollowSonePage(emptyTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new ImageBrowserPage(imageBrowserTemplate, this), "ImageBrowser"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new CreateAlbumPage(createAlbumTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new EditAlbumPage(emptyTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new DeleteAlbumPage(deleteAlbumTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new UploadImagePage(invalidTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new EditImagePage(emptyTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new DeleteImagePage(deleteImageTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new TrustPage(emptyTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new DistrustPage(emptyTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new UntrustPage(emptyTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new MarkAsKnownPage(emptyTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new BookmarkPage(emptyTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new UnbookmarkPage(emptyTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new BookmarksPage(bookmarksTemplate, this), "Bookmarks"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new SearchPage(searchTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new DeleteSonePage(deleteSoneTemplate, this), "DeleteSone"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new LoginPage(loginTemplate, this), "Login"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new LogoutPage(emptyTemplate, this), "Logout"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new OptionsPage(optionsTemplate, this), "Options"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new RescuePage(rescueTemplate, this), "Rescue"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new AboutPage(aboutTemplate, this, SonePlugin.VERSION), "About"));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new SoneTemplatePage("noPermission.html", noPermissionTemplate, "Page.NoPermission.Title", this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new DismissNotificationPage(emptyTemplate, this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new SoneTemplatePage("invalid.html", invalidTemplate, "Page.Invalid.Title", this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new StaticPage<FreenetRequest>("css/", "/static/css/", "text/css")));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new StaticPage<FreenetRequest>("javascript/", "/static/javascript/", "text/javascript")));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new StaticPage<FreenetRequest>("images/", "/static/images/", "image/png")));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new TemplatePage<FreenetRequest>("OpenSearch.xml", "application/opensearchdescription+xml", templateContextFactory, openSearchTemplate)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new GetImagePage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new GetTranslationPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new GetStatusAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new GetNotificationsAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new DismissNotificationAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new CreatePostAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new CreateReplyAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new GetReplyAjaxPage(this, replyTemplate)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new GetPostAjaxPage(this, postTemplate)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new GetTimesAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new MarkAsKnownAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new DeletePostAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new DeleteReplyAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new LockSoneAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new UnlockSoneAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new FollowSoneAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new UnfollowSoneAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new EditAlbumAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new EditImageAjaxPage(this, parserFilter)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new TrustAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new DistrustAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new UntrustAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new LikeAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new UnlikeAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new GetLikesAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new BookmarkAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new UnbookmarkAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new EditProfileFieldAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new DeleteProfileFieldAjaxPage(this)));
		pageToadlets.add(pageToadletFactory.createPageToadlet(new MoveProfileFieldAjaxPage(this)));

		ToadletContainer toadletContainer = sonePlugin.pluginRespirator().getToadletContainer();
		toadletContainer.getPageMaker().addNavigationCategory("/Sone/index.html", "Navigation.Menu.Sone.Name", "Navigation.Menu.Sone.Tooltip", sonePlugin);
		for (PageToadlet toadlet : pageToadlets) {
			String menuName = toadlet.getMenuName();
			if (menuName != null) {
				toadletContainer.register(toadlet, "Navigation.Menu.Sone.Name", toadlet.path(), true, "Navigation.Menu.Sone.Item." + menuName + ".Name", "Navigation.Menu.Sone.Item." + menuName + ".Tooltip", false, toadlet);
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
		toadletContainer.getPageMaker().removeNavigationCategory("Navigation.Menu.Sone.Name");
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
	 * Returns all {@link Sone#isLocal() local Sone}s that are referenced by
	 * {@link SonePart}s in the given text (after parsing it using
	 * {@link SoneTextParser}).
	 *
	 * @param text
	 *            The text to parse
	 * @return All mentioned local Sones
	 */
	private Collection<Sone> getMentionedSones(String text) {
		/* we need no context to find mentioned Sones. */
		Set<Sone> mentionedSones = new HashSet<Sone>();
		try {
			for (Part part : soneTextParser.parse(null, new StringReader(text))) {
				if (part instanceof SonePart) {
					mentionedSones.add(((SonePart) part).getSone());
				}
			}
		} catch (IOException ioe1) {
			logger.log(Level.WARNING, String.format("Could not parse post text: %s", text), ioe1);
		}
		return Collections2.filter(mentionedSones, Sone.LOCAL_SONE_FILTER);
	}

	/**
	 * Returns the Sone insert notification for the given Sone. If no
	 * notification for the given Sone exists, a new notification is created and
	 * cached.
	 *
	 * @param sone
	 *            The Sone to get the insert notification for
	 * @return The Sone insert notification
	 */
	private TemplateNotification getSoneInsertNotification(Sone sone) {
		synchronized (soneInsertNotifications) {
			TemplateNotification templateNotification = soneInsertNotifications.get(sone);
			if (templateNotification == null) {
				templateNotification = new TemplateNotification(TemplateParser.parse(createReader("/templates/notify/soneInsertNotification.html")));
				templateNotification.set("insertSone", sone);
				soneInsertNotifications.put(sone, templateNotification);
			}
			return templateNotification;
		}
	}

	//
	// EVENT HANDLERS
	//

	/**
	 * Notifies the web interface that a new {@link Sone} was found.
	 *
	 * @param newSoneFoundEvent
	 *            The event
	 */
	@Subscribe
	public void newSoneFound(NewSoneFoundEvent newSoneFoundEvent) {
		newSoneNotification.add(newSoneFoundEvent.sone());
		if (!hasFirstStartNotification()) {
			notificationManager.addNotification(newSoneNotification);
		}
	}

	/**
	 * Notifies the web interface that a new {@link Post} was found.
	 *
	 * @param newPostFoundEvent
	 *            The event
	 */
	@Subscribe
	public void newPostFound(NewPostFoundEvent newPostFoundEvent) {
		Post post = newPostFoundEvent.post();
		boolean isLocal = post.getSone().isLocal();
		if (isLocal) {
			localPostNotification.add(post);
		} else {
			newPostNotification.add(post);
		}
		if (!hasFirstStartNotification()) {
			notificationManager.addNotification(isLocal ? localPostNotification : newPostNotification);
			if (!getMentionedSones(post.getText()).isEmpty() && !isLocal) {
				mentionNotification.add(post);
				notificationManager.addNotification(mentionNotification);
			}
		} else {
			getCore().markPostKnown(post);
		}
	}

	/**
	 * Notifies the web interface that a new {@link PostReply} was found.
	 *
	 * @param newPostReplyFoundEvent
	 *            The event
	 */
	@Subscribe
	public void newReplyFound(NewPostReplyFoundEvent newPostReplyFoundEvent) {
		PostReply reply = newPostReplyFoundEvent.postReply();
		boolean isLocal = reply.getSone().isLocal();
		if (isLocal) {
			localReplyNotification.add(reply);
		} else {
			newReplyNotification.add(reply);
		}
		if (!hasFirstStartNotification()) {
			notificationManager.addNotification(isLocal ? localReplyNotification : newReplyNotification);
			if (!getMentionedSones(reply.getText()).isEmpty() && !isLocal && (reply.getPost().getSone() != null) && (reply.getTime() <= System.currentTimeMillis())) {
				mentionNotification.add(reply.getPost());
				notificationManager.addNotification(mentionNotification);
			}
		} else {
			getCore().markReplyKnown(reply);
		}
	}

	/**
	 * Notifies the web interface that a {@link Sone} was marked as known.
	 *
	 * @param markSoneKnownEvent
	 *            The event
	 */
	@Subscribe
	public void markSoneKnown(MarkSoneKnownEvent markSoneKnownEvent) {
		newSoneNotification.remove(markSoneKnownEvent.sone());
	}

	/**
	 * Notifies the web interface that a {@link Post} was marked as known.
	 *
	 * @param markPostKnownEvent
	 *            The event
	 */
	@Subscribe
	public void markPostKnown(MarkPostKnownEvent markPostKnownEvent) {
		newPostNotification.remove(markPostKnownEvent.post());
		localPostNotification.remove(markPostKnownEvent.post());
		mentionNotification.remove(markPostKnownEvent.post());
	}

	/**
	 * Notifies the web interface that a {@link PostReply} was marked as known.
	 *
	 * @param markPostReplyKnownEvent
	 *            The event
	 */
	@Subscribe
	public void markReplyKnown(MarkPostReplyKnownEvent markPostReplyKnownEvent) {
		newReplyNotification.remove(markPostReplyKnownEvent.postReply());
		localReplyNotification.remove(markPostReplyKnownEvent.postReply());
		mentionNotification.remove(markPostReplyKnownEvent.postReply().getPost());
	}

	/**
	 * Notifies the web interface that a {@link Sone} was removed.
	 *
	 * @param soneRemovedEvent
	 *            The event
	 */
	@Subscribe
	public void soneRemoved(SoneRemovedEvent soneRemovedEvent) {
		newSoneNotification.remove(soneRemovedEvent.sone());
	}

	/**
	 * Notifies the web interface that a {@link Post} was removed.
	 *
	 * @param postRemovedEvent
	 *            The event
	 */
	@Subscribe
	public void postRemoved(PostRemovedEvent postRemovedEvent) {
		newPostNotification.remove(postRemovedEvent.post());
		localPostNotification.remove(postRemovedEvent.post());
		mentionNotification.remove(postRemovedEvent.post());
	}

	/**
	 * Notifies the web interface that a {@link PostReply} was removed.
	 *
	 * @param postReplyRemovedEvent
	 *            The event
	 */
	@Subscribe
	public void replyRemoved(PostReplyRemovedEvent postReplyRemovedEvent) {
		PostReply reply = postReplyRemovedEvent.postReply();
		newReplyNotification.remove(reply);
		localReplyNotification.remove(reply);
		if (!getMentionedSones(reply.getText()).isEmpty()) {
			boolean isMentioned = false;
			for (PostReply existingReply : getCore().getReplies(reply.getPost())) {
				isMentioned |= !reply.isKnown() && !getMentionedSones(existingReply.getText()).isEmpty();
			}
			if (!isMentioned) {
				mentionNotification.remove(reply.getPost());
			}
		}
	}

	/**
	 * Notifies the web interface that a Sone was locked.
	 *
	 * @param soneLockedEvent
	 *            The event
	 */
	@Subscribe
	public void soneLocked(SoneLockedEvent soneLockedEvent) {
		final Sone sone = soneLockedEvent.sone();
		Object tickerObject = Ticker.getInstance().registerEvent(System.currentTimeMillis() + (5 * 60) * 1000, new Runnable() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void run() {
				lockedSonesNotification.add(sone);
				lockedSonesTickerObjects.remove(sone);
				notificationManager.addNotification(lockedSonesNotification);
			}
		}, "Sone Locked Notification");
		lockedSonesTickerObjects.put(sone, tickerObject);
	}

	/**
	 * Notifies the web interface that a Sone was unlocked.
	 *
	 * @param soneUnlockedEvent
	 *            The event
	 */
	@Subscribe
	public void soneUnlocked(SoneUnlockedEvent soneUnlockedEvent) {
		lockedSonesNotification.remove(soneUnlockedEvent.sone());
		Ticker.getInstance().deregisterEvent(lockedSonesTickerObjects.remove(soneUnlockedEvent.sone()));
	}

	/**
	 * Notifies the web interface that a {@link Sone} is being inserted.
	 *
	 * @param soneInsertingEvent
	 *            The event
	 */
	@Subscribe
	public void soneInserting(SoneInsertingEvent soneInsertingEvent) {
		TemplateNotification soneInsertNotification = getSoneInsertNotification(soneInsertingEvent.sone());
		soneInsertNotification.set("soneStatus", "inserting");
		if (soneInsertingEvent.sone().getOptions().getBooleanOption("EnableSoneInsertNotifications").get()) {
			notificationManager.addNotification(soneInsertNotification);
		}
	}

	/**
	 * Notifies the web interface that a {@link Sone} was inserted.
	 *
	 * @param soneInsertedEvent
	 *            The event
	 */
	@Subscribe
	public void soneInserted(SoneInsertedEvent soneInsertedEvent) {
		TemplateNotification soneInsertNotification = getSoneInsertNotification(soneInsertedEvent.sone());
		soneInsertNotification.set("soneStatus", "inserted");
		soneInsertNotification.set("insertDuration", soneInsertedEvent.insertDuration() / 1000);
		if (soneInsertedEvent.sone().getOptions().getBooleanOption("EnableSoneInsertNotifications").get()) {
			notificationManager.addNotification(soneInsertNotification);
		}
	}

	/**
	 * Notifies the web interface that a {@link Sone} insert was aborted.
	 *
	 * @param soneInsertAbortedEvent
	 *            The event
	 */
	@Subscribe
	public void soneInsertAborted(SoneInsertAbortedEvent soneInsertAbortedEvent) {
		TemplateNotification soneInsertNotification = getSoneInsertNotification(soneInsertAbortedEvent.sone());
		soneInsertNotification.set("soneStatus", "insert-aborted");
		soneInsertNotification.set("insert-error", soneInsertAbortedEvent.cause());
		if (soneInsertAbortedEvent.sone().getOptions().getBooleanOption("EnableSoneInsertNotifications").get()) {
			notificationManager.addNotification(soneInsertNotification);
		}
	}

	/**
	 * Notifies the web interface that a new Sone version was found.
	 *
	 * @param updateFoundEvent
	 *            The event
	 */
	@Subscribe
	public void updateFound(UpdateFoundEvent updateFoundEvent) {
		newVersionNotification.getTemplateContext().set("latestVersion", updateFoundEvent.version());
		newVersionNotification.getTemplateContext().set("latestEdition", updateFoundEvent.latestEdition());
		newVersionNotification.getTemplateContext().set("releaseTime", updateFoundEvent.releaseTime());
		notificationManager.addNotification(newVersionNotification);
	}

	//
	// CORELISTENER METHODS
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void imageInsertStarted(Image image) {
		insertingImagesNotification.add(image);
		notificationManager.addNotification(insertingImagesNotification);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void imageInsertAborted(Image image) {
		insertingImagesNotification.remove(image);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void imageInsertFinished(Image image) {
		insertingImagesNotification.remove(image);
		insertedImagesNotification.add(image);
		notificationManager.addNotification(insertedImagesNotification);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void imageInsertFailed(Image image, Throwable cause) {
		insertingImagesNotification.remove(image);
		imageInsertFailedNotification.add(image);
		notificationManager.addNotification(imageInsertFailedNotification);
	}

}
