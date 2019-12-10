/*
 * Sone - WebInterface.java - Copyright © 2010–2019 David Roden
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

import static com.google.common.collect.FluentIterable.from;
import static java.util.logging.Logger.getLogger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;

import net.pterodactylus.sone.core.Core;
import net.pterodactylus.sone.core.ElementLoader;
import net.pterodactylus.sone.core.event.*;
import net.pterodactylus.sone.data.Image;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.freenet.L10nFilter;
import net.pterodactylus.sone.freenet.Translation;
import net.pterodactylus.sone.main.Loaders;
import net.pterodactylus.sone.main.PluginHomepage;
import net.pterodactylus.sone.main.PluginVersion;
import net.pterodactylus.sone.main.PluginYear;
import net.pterodactylus.sone.main.SonePlugin;
import net.pterodactylus.sone.notify.ListNotification;
import net.pterodactylus.sone.notify.ListNotificationFilter;
import net.pterodactylus.sone.notify.PostVisibilityFilter;
import net.pterodactylus.sone.notify.ReplyVisibilityFilter;
import net.pterodactylus.sone.template.LinkedElementRenderFilter;
import net.pterodactylus.sone.template.ParserFilter;
import net.pterodactylus.sone.template.RenderFilter;
import net.pterodactylus.sone.template.ShortenFilter;
import net.pterodactylus.sone.text.Part;
import net.pterodactylus.sone.text.SonePart;
import net.pterodactylus.sone.text.SoneTextParser;
import net.pterodactylus.sone.text.TimeTextConverter;
import net.pterodactylus.sone.web.ajax.BookmarkAjaxPage;
import net.pterodactylus.sone.web.ajax.CreatePostAjaxPage;
import net.pterodactylus.sone.web.ajax.CreateReplyAjaxPage;
import net.pterodactylus.sone.web.ajax.DeletePostAjaxPage;
import net.pterodactylus.sone.web.ajax.DeleteProfileFieldAjaxPage;
import net.pterodactylus.sone.web.ajax.DeleteReplyAjaxPage;
import net.pterodactylus.sone.web.ajax.DismissNotificationAjaxPage;
import net.pterodactylus.sone.web.ajax.EditAlbumAjaxPage;
import net.pterodactylus.sone.web.ajax.EditImageAjaxPage;
import net.pterodactylus.sone.web.ajax.EditProfileFieldAjaxPage;
import net.pterodactylus.sone.web.ajax.FollowSoneAjaxPage;
import net.pterodactylus.sone.web.ajax.GetLikesAjaxPage;
import net.pterodactylus.sone.web.ajax.GetLinkedElementAjaxPage;
import net.pterodactylus.sone.web.ajax.GetNotificationsAjaxPage;
import net.pterodactylus.sone.web.ajax.GetPostAjaxPage;
import net.pterodactylus.sone.web.ajax.GetReplyAjaxPage;
import net.pterodactylus.sone.web.ajax.GetStatusAjaxPage;
import net.pterodactylus.sone.web.ajax.GetTimesAjaxPage;
import net.pterodactylus.sone.web.ajax.GetTranslationAjaxPage;
import net.pterodactylus.sone.web.ajax.LikeAjaxPage;
import net.pterodactylus.sone.web.ajax.LockSoneAjaxPage;
import net.pterodactylus.sone.web.ajax.MarkAsKnownAjaxPage;
import net.pterodactylus.sone.web.ajax.MoveProfileFieldAjaxPage;
import net.pterodactylus.sone.web.ajax.UnbookmarkAjaxPage;
import net.pterodactylus.sone.web.ajax.UnfollowSoneAjaxPage;
import net.pterodactylus.sone.web.ajax.UnlikeAjaxPage;
import net.pterodactylus.sone.web.ajax.UnlockSoneAjaxPage;
import net.pterodactylus.sone.web.page.FreenetRequest;
import net.pterodactylus.sone.web.page.TemplateRenderer;
import net.pterodactylus.sone.web.pages.*;
import net.pterodactylus.util.notify.Notification;
import net.pterodactylus.util.notify.NotificationManager;
import net.pterodactylus.util.notify.TemplateNotification;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContextFactory;
import net.pterodactylus.util.web.RedirectPage;
import net.pterodactylus.util.web.TemplatePage;

import freenet.clients.http.SessionManager;
import freenet.clients.http.SessionManager.Session;
import freenet.clients.http.ToadletContext;

import com.codahale.metrics.*;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

/**
 * Bundles functionality that a web interface of a Freenet plugin needs, e.g.
 * references to l10n helpers.
 */
public class WebInterface implements SessionProvider {

	/** The logger. */
	private static final Logger logger = getLogger(WebInterface.class.getName());

	/** The loaders for templates, pages, and classpath providers. */
	private final Loaders loaders;

	/** The notification manager. */
	private final NotificationManager notificationManager;

	/** The Sone plugin. */
	private final SonePlugin sonePlugin;

	/** The form password. */
	private final String formPassword;

	/** The template context factory. */
	private final TemplateContextFactory templateContextFactory;
	private final TemplateRenderer templateRenderer;

	/** The Sone text parser. */
	private final SoneTextParser soneTextParser;

	/** The parser filter. */
	private final ParserFilter parserFilter;
	private final ShortenFilter shortenFilter;
	private final RenderFilter renderFilter;

	private final ListNotificationFilter listNotificationFilter;
	private final PostVisibilityFilter postVisibilityFilter;
	private final ReplyVisibilityFilter replyVisibilityFilter;

	private final ElementLoader elementLoader;
	private final LinkedElementRenderFilter linkedElementRenderFilter;
	private final TimeTextConverter timeTextConverter = new TimeTextConverter();
	private final L10nFilter l10nFilter;

	private final PageToadletRegistry pageToadletRegistry;
	private final MetricRegistry metricRegistry;
	private final Translation translation;

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
	private final Map<Sone, TemplateNotification> soneInsertNotifications = new HashMap<>();

	/** Sone locked notification ticker objects. */
	private final Map<Sone, ScheduledFuture<?>> lockedSonesTickerObjects = Collections.synchronizedMap(new HashMap<Sone, ScheduledFuture<?>>());

	/** The “new version” notification. */
	private final TemplateNotification newVersionNotification;

	/** The “inserting images” notification. */
	private final ListNotification<Image> insertingImagesNotification;

	/** The “inserted images” notification. */
	private final ListNotification<Image> insertedImagesNotification;

	/** The “image insert failed” notification. */
	private final ListNotification<Image> imageInsertFailedNotification;

	/** Scheduled executor for time-based notifications. */
	private final ScheduledExecutorService ticker = Executors.newScheduledThreadPool(1);

	@Inject
	public WebInterface(SonePlugin sonePlugin, Loaders loaders, ListNotificationFilter listNotificationFilter,
			PostVisibilityFilter postVisibilityFilter, ReplyVisibilityFilter replyVisibilityFilter,
			ElementLoader elementLoader, TemplateContextFactory templateContextFactory,
			TemplateRenderer templateRenderer,
			ParserFilter parserFilter, ShortenFilter shortenFilter,
			RenderFilter renderFilter,
			LinkedElementRenderFilter linkedElementRenderFilter,
			PageToadletRegistry pageToadletRegistry, MetricRegistry metricRegistry, Translation translation, L10nFilter l10nFilter,
			NotificationManager notificationManager, @Named("newRemotePost") ListNotification<Post> newPostNotification) {
		this.sonePlugin = sonePlugin;
		this.loaders = loaders;
		this.listNotificationFilter = listNotificationFilter;
		this.postVisibilityFilter = postVisibilityFilter;
		this.replyVisibilityFilter = replyVisibilityFilter;
		this.elementLoader = elementLoader;
		this.templateRenderer = templateRenderer;
		this.parserFilter = parserFilter;
		this.shortenFilter = shortenFilter;
		this.renderFilter = renderFilter;
		this.linkedElementRenderFilter = linkedElementRenderFilter;
		this.pageToadletRegistry = pageToadletRegistry;
		this.metricRegistry = metricRegistry;
		this.l10nFilter = l10nFilter;
		this.translation = translation;
		this.notificationManager = notificationManager;
		this.newPostNotification = newPostNotification;
		formPassword = sonePlugin.pluginRespirator().getToadletContainer().getFormPassword();
		soneTextParser = new SoneTextParser(getCore(), getCore());

		this.templateContextFactory = templateContextFactory;
		templateContextFactory.addTemplateObject("webInterface", this);
		templateContextFactory.addTemplateObject("formPassword", formPassword);

		/* create notifications. */
		Template localPostNotificationTemplate = loaders.loadTemplate("/templates/notify/newPostNotification.html");
		localPostNotification = new ListNotification<>("local-post-notification", "posts", localPostNotificationTemplate, false);

		Template newReplyNotificationTemplate = loaders.loadTemplate("/templates/notify/newReplyNotification.html");
		newReplyNotification = new ListNotification<>("new-reply-notification", "replies", newReplyNotificationTemplate, false);

		Template localReplyNotificationTemplate = loaders.loadTemplate("/templates/notify/newReplyNotification.html");
		localReplyNotification = new ListNotification<>("local-reply-notification", "replies", localReplyNotificationTemplate, false);

		Template mentionNotificationTemplate = loaders.loadTemplate("/templates/notify/mentionNotification.html");
		mentionNotification = new ListNotification<>("mention-notification", "posts", mentionNotificationTemplate, false);

		Template newVersionTemplate = loaders.loadTemplate("/templates/notify/newVersionNotification.html");
		newVersionNotification = new TemplateNotification("new-version-notification", newVersionTemplate);

		Template insertingImagesTemplate = loaders.loadTemplate("/templates/notify/inserting-images-notification.html");
		insertingImagesNotification = new ListNotification<>("inserting-images-notification", "images", insertingImagesTemplate);

		Template insertedImagesTemplate = loaders.loadTemplate("/templates/notify/inserted-images-notification.html");
		insertedImagesNotification = new ListNotification<>("inserted-images-notification", "images", insertedImagesTemplate);

		Template imageInsertFailedTemplate = loaders.loadTemplate("/templates/notify/image-insert-failed-notification.html");
		imageInsertFailedNotification = new ListNotification<>("image-insert-failed-notification", "images", imageInsertFailedTemplate);
	}

	//
	// ACCESSORS
	//

	/**
	 * Returns the Sone core used by the Sone plugin.
	 *
	 * @return The Sone core
	 */
	@Nonnull
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

	private Session getCurrentSessionWithoutCreation(ToadletContext toadletContenxt) {
		return getSessionManager().useSession(toadletContenxt);
	}

	private Session getOrCreateCurrentSession(ToadletContext toadletContenxt) {
		Session session = getCurrentSessionWithoutCreation(toadletContenxt);
		if (session == null) {
			session = getSessionManager().createSession(UUID.randomUUID().toString(), toadletContenxt);
		}
		return session;
	}

	public Sone getCurrentSoneCreatingSession(ToadletContext toadletContext) {
		Collection<Sone> localSones = getCore().getLocalSones();
		if (localSones.size() == 1) {
			return localSones.iterator().next();
		}
		return getCurrentSone(getOrCreateCurrentSession(toadletContext));
	}

	public Sone getCurrentSoneWithoutCreatingSession(ToadletContext toadletContext) {
		Collection<Sone> localSones = getCore().getLocalSones();
		if (localSones.size() == 1) {
			return localSones.iterator().next();
		}
		return getCurrentSone(getCurrentSessionWithoutCreation(toadletContext));
	}

	/**
	 * Returns the currently logged in Sone.
	 *
	 * @param session
	 *            The session
	 * @return The currently logged in Sone, or {@code null} if no Sone is
	 *         currently logged in
	 */
	private Sone getCurrentSone(Session session) {
		if (session == null) {
			return null;
		}
		String soneId = (String) session.getAttribute("Sone.CurrentSone");
		if (soneId == null) {
			return null;
		}
		return getCore().getLocalSone(soneId);
	}

	@Override
	@Nullable
	public Sone getCurrentSone(@Nonnull ToadletContext toadletContext, boolean createSession) {
		return createSession ? getCurrentSoneCreatingSession(toadletContext) : getCurrentSoneWithoutCreatingSession(toadletContext);
	}

	/**
	 * Sets the currently logged in Sone.
	 *
	 * @param toadletContext
	 *            The toadlet context
	 * @param sone
	 *            The Sone to set as currently logged in
	 */
	@Override
	public void setCurrentSone(@Nonnull ToadletContext toadletContext, @Nullable Sone sone) {
		Session session = getOrCreateCurrentSession(toadletContext);
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

	@Nonnull
	public Optional<Notification> getNotification(@Nonnull String notificationId) {
		return Optional.fromNullable(notificationManager.getNotification(notificationId));
	}

	@Nonnull
	public Collection<Notification> getNotifications(@Nullable Sone currentSone) {
		return listNotificationFilter.filterNotifications(notificationManager.getNotifications(), currentSone);
	}

	public Translation getTranslation() {
		return translation;
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

	@Nonnull
	public Collection<Post> getNewPosts(@Nullable Sone currentSone) {
		Set<Post> allNewPosts = ImmutableSet.<Post> builder()
				.addAll(newPostNotification.getElements())
				.addAll(localPostNotification.getElements())
				.build();
		return from(allNewPosts).filter(postVisibilityFilter.isVisible(currentSone)).toSet();
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

	@Nonnull
	public Collection<PostReply> getNewReplies(@Nullable Sone currentSone) {
		Set<PostReply> allNewReplies = ImmutableSet.<PostReply>builder()
				.addAll(newReplyNotification.getElements())
				.addAll(localReplyNotification.getElements())
				.build();
		return from(allNewReplies).filter(replyVisibilityFilter.isVisible(currentSone)).toSet();
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
			Template firstStartNotificationTemplate = loaders.loadTemplate("/templates/notify/firstStartNotification.html");
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
			Template configNotReadNotificationTemplate = loaders.loadTemplate("/templates/notify/configNotReadNotification.html");
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
		Template startupNotificationTemplate = loaders.loadTemplate("/templates/notify/startupNotification.html");

		final TemplateNotification startupNotification = new TemplateNotification("startup-notification", startupNotificationTemplate);
		notificationManager.addNotification(startupNotification);

		ticker.schedule(new Runnable() {

			@Override
			public void run() {
				startupNotification.dismiss();
			}
		}, 2, TimeUnit.MINUTES);

		Template wotMissingNotificationTemplate = loaders.loadTemplate("/templates/notify/wotMissingNotification.html");
		final TemplateNotification wotMissingNotification = new TemplateNotification("wot-missing-notification", wotMissingNotificationTemplate);
		ticker.scheduleAtFixedRate(new Runnable() {

			@Override
			@SuppressWarnings("synthetic-access")
			public void run() {
				if (getCore().getIdentityManager().isConnected()) {
					wotMissingNotification.dismiss();
				} else {
					notificationManager.addNotification(wotMissingNotification);
				}
			}

		}, 15, 15, TimeUnit.SECONDS);
	}

	/**
	 * Stops the web interface and unregisters all toadlets.
	 */
	public void stop() {
		pageToadletRegistry.unregisterToadlets();
		ticker.shutdownNow();
	}

	//
	// PRIVATE METHODS
	//

	/**
	 * Register all toadlets.
	 */
	private void registerToadlets() {
		Template postTemplate = loaders.loadTemplate("/templates/include/viewPost.html");
		Template replyTemplate = loaders.loadTemplate("/templates/include/viewReply.html");
		Template openSearchTemplate = loaders.loadTemplate("/templates/xml/OpenSearch.xml");

		pageToadletRegistry.addPage(new RedirectPage<FreenetRequest>("", "index.html"));
		pageToadletRegistry.addPage(new IndexPage(this, loaders, templateRenderer, postVisibilityFilter));
		pageToadletRegistry.addPage(new NewPage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new CreateSonePage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new KnownSonesPage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new EditProfilePage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new EditProfileFieldPage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new DeleteProfileFieldPage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new CreatePostPage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new CreateReplyPage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new ViewSonePage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new ViewPostPage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new LikePage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new UnlikePage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new DeletePostPage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new DeleteReplyPage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new LockSonePage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new UnlockSonePage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new FollowSonePage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new UnfollowSonePage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new ImageBrowserPage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new CreateAlbumPage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new EditAlbumPage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new DeleteAlbumPage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new UploadImagePage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new EditImagePage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new DeleteImagePage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new MarkAsKnownPage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new BookmarkPage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new UnbookmarkPage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new BookmarksPage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new SearchPage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new DeleteSonePage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new LoginPage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new LogoutPage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new OptionsPage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new RescuePage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new AboutPage(this, loaders, templateRenderer, new PluginVersion(SonePlugin.getPluginVersion()), new PluginYear(sonePlugin.getYear()), new PluginHomepage(sonePlugin.getHomepage())));
		pageToadletRegistry.addPage(new InvalidPage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new NoPermissionPage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new EmptyImageTitlePage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new EmptyAlbumTitlePage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new DismissNotificationPage(this, loaders, templateRenderer));
		pageToadletRegistry.addPage(new DebugPage(this, loaders, templateRenderer));
		pageToadletRegistry.addDebugPage(new MetricsPage(this, loaders, templateRenderer, metricRegistry));
		pageToadletRegistry.addPage(loaders.<FreenetRequest>loadStaticPage("css/", "/static/css/", "text/css"));
		pageToadletRegistry.addPage(loaders.<FreenetRequest>loadStaticPage("javascript/", "/static/javascript/", "text/javascript"));
		pageToadletRegistry.addPage(loaders.<FreenetRequest>loadStaticPage("images/", "/static/images/", "image/png"));
		pageToadletRegistry.addPage(new TemplatePage<FreenetRequest>("OpenSearch.xml", "application/opensearchdescription+xml", templateContextFactory, openSearchTemplate));
		pageToadletRegistry.addPage(new GetImagePage(this));
		pageToadletRegistry.addPage(new GetTranslationAjaxPage(this));
		pageToadletRegistry.addPage(new GetStatusAjaxPage(this, elementLoader, timeTextConverter, l10nFilter, TimeZone.getDefault()));
		pageToadletRegistry.addPage(new GetNotificationsAjaxPage(this));
		pageToadletRegistry.addPage(new DismissNotificationAjaxPage(this));
		pageToadletRegistry.addPage(new CreatePostAjaxPage(this));
		pageToadletRegistry.addPage(new CreateReplyAjaxPage(this));
		pageToadletRegistry.addPage(new GetReplyAjaxPage(this, replyTemplate));
		pageToadletRegistry.addPage(new GetPostAjaxPage(this, postTemplate));
		pageToadletRegistry.addPage(new GetLinkedElementAjaxPage(this, elementLoader, linkedElementRenderFilter));
		pageToadletRegistry.addPage(new GetTimesAjaxPage(this, timeTextConverter, l10nFilter, TimeZone.getDefault()));
		pageToadletRegistry.addPage(new MarkAsKnownAjaxPage(this));
		pageToadletRegistry.addPage(new DeletePostAjaxPage(this));
		pageToadletRegistry.addPage(new DeleteReplyAjaxPage(this));
		pageToadletRegistry.addPage(new LockSoneAjaxPage(this));
		pageToadletRegistry.addPage(new UnlockSoneAjaxPage(this));
		pageToadletRegistry.addPage(new FollowSoneAjaxPage(this));
		pageToadletRegistry.addPage(new UnfollowSoneAjaxPage(this));
		pageToadletRegistry.addPage(new EditAlbumAjaxPage(this));
		pageToadletRegistry.addPage(new EditImageAjaxPage(this, parserFilter, shortenFilter, renderFilter));
		pageToadletRegistry.addPage(new LikeAjaxPage(this));
		pageToadletRegistry.addPage(new UnlikeAjaxPage(this));
		pageToadletRegistry.addPage(new GetLikesAjaxPage(this));
		pageToadletRegistry.addPage(new BookmarkAjaxPage(this));
		pageToadletRegistry.addPage(new UnbookmarkAjaxPage(this));
		pageToadletRegistry.addPage(new EditProfileFieldAjaxPage(this));
		pageToadletRegistry.addPage(new DeleteProfileFieldAjaxPage(this));
		pageToadletRegistry.addPage(new MoveProfileFieldAjaxPage(this));

		pageToadletRegistry.registerToadlets();
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
		Set<Sone> mentionedSones = new HashSet<>();
		for (Part part : soneTextParser.parse(text, null)) {
			if (part instanceof SonePart) {
				mentionedSones.add(((SonePart) part).getSone());
			}
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
				templateNotification = new TemplateNotification(loaders.loadTemplate("/templates/notify/soneInsertNotification.html"));
				templateNotification.set("insertSone", sone);
				soneInsertNotifications.put(sone, templateNotification);
			}
			return templateNotification;
		}
	}

	private boolean localSoneMentionedInNewPostOrReply(Post post) {
		if (!post.getSone().isLocal()) {
			if (!getMentionedSones(post.getText()).isEmpty() && !post.isKnown()) {
				return true;
			}
		}
		for (PostReply postReply : getCore().getReplies(post.getId())) {
			if (postReply.getSone().isLocal()) {
				continue;
			}
			if (!getMentionedSones(postReply.getText()).isEmpty() && !postReply.isKnown()) {
				return true;
			}
		}
		return false;
	}

	//
	// EVENT HANDLERS
	//

	/**
	 * Notifies the web interface that a new {@link Post} was found.
	 *
	 * @param newPostFoundEvent
	 *            The event
	 */
	@Subscribe
	public void newPostFound(NewPostFoundEvent newPostFoundEvent) {
		Post post = newPostFoundEvent.getPost();
		boolean isLocal = post.getSone().isLocal();
		if (isLocal) {
			localPostNotification.add(post);
		}
		if (!hasFirstStartNotification()) {
			if (isLocal) {
				notificationManager.addNotification(localPostNotification);
			}
			if (!getMentionedSones(post.getText()).isEmpty() && !isLocal) {
				mentionNotification.add(post);
				notificationManager.addNotification(mentionNotification);
			}
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
		PostReply reply = newPostReplyFoundEvent.getPostReply();
		boolean isLocal = reply.getSone().isLocal();
		if (isLocal) {
			localReplyNotification.add(reply);
		} else {
			newReplyNotification.add(reply);
		}
		if (!hasFirstStartNotification()) {
			notificationManager.addNotification(isLocal ? localReplyNotification : newReplyNotification);
			if (reply.getPost().isPresent() && localSoneMentionedInNewPostOrReply(reply.getPost().get())) {
				mentionNotification.add(reply.getPost().get());
				notificationManager.addNotification(mentionNotification);
			}
		} else {
			getCore().markReplyKnown(reply);
		}
	}

	@Subscribe
	public void markPostKnown(MarkPostKnownEvent markPostKnownEvent) {
		removePost(markPostKnownEvent.getPost());
	}

	@Subscribe
	public void markReplyKnown(MarkPostReplyKnownEvent markPostReplyKnownEvent) {
		removeReply(markPostReplyKnownEvent.getPostReply());
	}

	@Subscribe
	public void postRemoved(PostRemovedEvent postRemovedEvent) {
		removePost(postRemovedEvent.getPost());
	}

	private void removePost(Post post) {
		newPostNotification.remove(post);
		localPostNotification.remove(post);
		if (!localSoneMentionedInNewPostOrReply(post)) {
			mentionNotification.remove(post);
		}
	}

	@Subscribe
	public void replyRemoved(PostReplyRemovedEvent postReplyRemovedEvent) {
		removeReply(postReplyRemovedEvent.getPostReply());
	}

	private void removeReply(PostReply reply) {
		newReplyNotification.remove(reply);
		localReplyNotification.remove(reply);
		if (reply.getPost().isPresent() && !localSoneMentionedInNewPostOrReply(reply.getPost().get())) {
			mentionNotification.remove(reply.getPost().get());
		}
	}

	/**
	 * Notifies the web interface that a {@link Sone} is being inserted.
	 *
	 * @param soneInsertingEvent
	 *            The event
	 */
	@Subscribe
	public void soneInserting(SoneInsertingEvent soneInsertingEvent) {
		TemplateNotification soneInsertNotification = getSoneInsertNotification(soneInsertingEvent.getSone());
		soneInsertNotification.set("soneStatus", "inserting");
		if (soneInsertingEvent.getSone().getOptions().isSoneInsertNotificationEnabled()) {
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
		TemplateNotification soneInsertNotification = getSoneInsertNotification(soneInsertedEvent.getSone());
		soneInsertNotification.set("soneStatus", "inserted");
		soneInsertNotification.set("insertDuration", soneInsertedEvent.getInsertDuration() / 1000);
		if (soneInsertedEvent.getSone().getOptions().isSoneInsertNotificationEnabled()) {
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
		TemplateNotification soneInsertNotification = getSoneInsertNotification(soneInsertAbortedEvent.getSone());
		soneInsertNotification.set("soneStatus", "insert-aborted");
		soneInsertNotification.set("insert-error", soneInsertAbortedEvent.getCause());
		if (soneInsertAbortedEvent.getSone().getOptions().isSoneInsertNotificationEnabled()) {
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
		newVersionNotification.set("latestVersion", updateFoundEvent.getVersion());
		newVersionNotification.set("latestEdition", updateFoundEvent.getLatestEdition());
		newVersionNotification.set("releaseTime", updateFoundEvent.getReleaseTime());
		newVersionNotification.set("disruptive", updateFoundEvent.isDisruptive());
		notificationManager.addNotification(newVersionNotification);
	}

	/**
	 * Notifies the web interface that an image insert was started
	 *
	 * @param imageInsertStartedEvent
	 *            The event
	 */
	@Subscribe
	public void imageInsertStarted(ImageInsertStartedEvent imageInsertStartedEvent) {
		insertingImagesNotification.add(imageInsertStartedEvent.getImage());
		notificationManager.addNotification(insertingImagesNotification);
	}

	/**
	 * Notifies the web interface that an {@link Image} insert was aborted.
	 *
	 * @param imageInsertAbortedEvent
	 *            The event
	 */
	@Subscribe
	public void imageInsertAborted(ImageInsertAbortedEvent imageInsertAbortedEvent) {
		insertingImagesNotification.remove(imageInsertAbortedEvent.getImage());
	}

	/**
	 * Notifies the web interface that an {@link Image} insert is finished.
	 *
	 * @param imageInsertFinishedEvent
	 *            The event
	 */
	@Subscribe
	public void imageInsertFinished(ImageInsertFinishedEvent imageInsertFinishedEvent) {
		insertingImagesNotification.remove(imageInsertFinishedEvent.getImage());
		insertedImagesNotification.add(imageInsertFinishedEvent.getImage());
		notificationManager.addNotification(insertedImagesNotification);
	}

	/**
	 * Notifies the web interface that an {@link Image} insert has failed.
	 *
	 * @param imageInsertFailedEvent
	 *            The event
	 */
	@Subscribe
	public void imageInsertFailed(ImageInsertFailedEvent imageInsertFailedEvent) {
		insertingImagesNotification.remove(imageInsertFailedEvent.getImage());
		imageInsertFailedNotification.add(imageInsertFailedEvent.getImage());
		notificationManager.addNotification(imageInsertFailedNotification);
	}

	@Subscribe
	public void debugActivated(@Nonnull DebugActivatedEvent debugActivatedEvent) {
		pageToadletRegistry.activateDebugMode();
	}

}
