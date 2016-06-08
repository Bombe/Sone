package net.pterodactylus.sone.notify;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.SoneOptions;
import net.pterodactylus.sone.freenet.wot.OwnIdentity;
import net.pterodactylus.util.notify.Notification;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Unit test for {@link ListNotificationFilterTest}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ListNotificationFilterTest {

	private static final String LOCAL_ID = "local-id";

	private final PostVisibilityFilter postVisibilityFilter = mock(PostVisibilityFilter.class);
	private final ReplyVisibilityFilter replyVisibilityFilter = mock(ReplyVisibilityFilter.class);
	private final ListNotificationFilter listNotificationFilter = new ListNotificationFilter(postVisibilityFilter, replyVisibilityFilter);

	private final Sone localSone = mock(Sone.class);
	private final SoneOptions soneOptions = mock(SoneOptions.class);
	private final OwnIdentity localIdentity = mock(OwnIdentity.class);
	private final List<ListNotification<Post>> newPostNotifications = Arrays.asList(createNewPostNotification());
	private final List<ListNotification<PostReply>> newReplyNotifications = Arrays.asList(createNewReplyNotification());
	private final List<ListNotification<Post>> mentionNotifications = Arrays.asList(createMentionNotification());

	public ListNotificationFilterTest() {
		when(localSone.getId()).thenReturn(LOCAL_ID);
		when(localSone.isLocal()).thenReturn(true);
		when(localSone.getIdentity()).thenReturn(localIdentity);
		when(localIdentity.getId()).thenReturn(LOCAL_ID);
		when(localSone.getOptions()).thenReturn(soneOptions);
	}

	@Test
	public void filterIsOnlyCreatedOnce() {
	    Injector injector = Guice.createInjector();
		ListNotificationFilter firstFilter = injector.getInstance(ListNotificationFilter.class);
		ListNotificationFilter secondFilter = injector.getInstance(ListNotificationFilter.class);
		assertThat(firstFilter, sameInstance(secondFilter));
	}

	@Test
	public void newSoneNotificationsAreNotRemovedIfNotLoggedIn() {
		List<Notification> notifications = Arrays.asList(createNewSoneNotification());
		List<Notification> filteredNotifications = listNotificationFilter.filterNotifications(notifications, null);
		assertThat(filteredNotifications, contains(notifications.get(0)));
	}

	private Notification createNewSoneNotification() {
		ListNotification<Sone> newSoneNotification = mock(ListNotification.class);
		when(newSoneNotification.getId()).thenReturn("new-sone-notification");
		return newSoneNotification;
	}

	@Test
	public void newSoneNotificationsAreRemovedIfLoggedInAndNewSonesShouldNotBeShown() {
		List<Notification> notifications = Arrays.asList(createNewSoneNotification());
		List<Notification> filteredNotifications = listNotificationFilter.filterNotifications(notifications, localSone);
		assertThat(filteredNotifications, emptyIterable());
	}

	@Test
	public void newSoneNotificationsAreNotRemovedIfLoggedInAndNewSonesShouldBeShown() {
		List<Notification> notifications = Arrays.asList(createNewSoneNotification());
		when(soneOptions.isShowNewSoneNotifications()).thenReturn(true);
		List<Notification> filteredNotifications = listNotificationFilter.filterNotifications(notifications, localSone);
		assertThat(filteredNotifications, contains(notifications.get(0)));
	}

	private ListNotification<Post> createNewPostNotification() {
		ListNotification<Post> newSoneNotification = mock(ListNotification.class);
		when(newSoneNotification.getElements()).thenReturn(new ArrayList<Post>());
		when(newSoneNotification.getId()).thenReturn("new-post-notification");
		return newSoneNotification;
	}

	@Test
	public void newPostNotificationIsNotShownIfOptionsSetAccordingly() {
		List<ListNotification<Post>> notifications = Arrays.asList(createNewPostNotification());
		List<Notification> filteredNotifications = listNotificationFilter.filterNotifications(notifications, localSone);
		assertThat(filteredNotifications, hasSize(0));
	}

	private void activateNewPostNotifications() {
		when(soneOptions.isShowNewPostNotifications()).thenReturn(true);
	}

	private boolean addPostToPostNotification(List<ListNotification<Post>> notifications) {
		return notifications.get(0).getElements().add(mock(Post.class));
	}

	private void setPostVisibilityPredicate(Predicate<Post> value) {
		when(postVisibilityFilter.isVisible(any(Sone.class))).thenReturn(value);
	}

	@Test
	public void newPostNotificationIsNotShownIfNoNewPostsAreVisible() {
		activateNewPostNotifications();
		addPostToPostNotification(newPostNotifications);
		setPostVisibilityPredicate(Predicates.<Post>alwaysFalse());
		List<Notification> filteredNotifications = listNotificationFilter.filterNotifications(newPostNotifications, localSone);
		assertThat(filteredNotifications, hasSize(0));
	}

	@Test
	public void newPostNotificationIsShownIfNewPostsAreVisible() {
		activateNewPostNotifications();
		addPostToPostNotification(newPostNotifications);
		setPostVisibilityPredicate(Predicates.<Post>alwaysTrue());
		List<Notification> filteredNotifications = listNotificationFilter.filterNotifications(newPostNotifications, localSone);
		assertThat(filteredNotifications, contains((Notification) newPostNotifications.get(0)));
	}

	@Test
	public void newPostNotificationIsNotShownIfNewPostsAreVisibleButLocalSoneIsNull() {
		activateNewPostNotifications();
		addPostToPostNotification(newPostNotifications);
		setPostVisibilityPredicate(Predicates.<Post>alwaysTrue());
		List<Notification> filteredNotifications = listNotificationFilter.filterNotifications(newPostNotifications, null);
		assertThat(filteredNotifications, Matchers.<Notification>emptyIterable());
	}

	@Test
	public void newPostNotificationContainsOnlyVisiblePosts() {
		activateNewPostNotifications();
		addPostToPostNotification(newPostNotifications);
		addPostToPostNotification(newPostNotifications);
		setPostVisibilityPredicate(new Predicate<Post>() {
			@Override
			public boolean apply(@Nullable Post post) {
				return post.equals(newPostNotifications.get(0).getElements().get(1));
			}
		});
		List<Notification> filteredNotifications = listNotificationFilter.filterNotifications(newPostNotifications, localSone);
		assertThat(filteredNotifications, hasSize(1));
		assertThat(((ListNotification<Post>) filteredNotifications.get(0)).getElements().get(0), is(newPostNotifications.get(0).getElements().get(1)));
	}

	private ListNotification<PostReply> createNewReplyNotification() {
		ListNotification<PostReply> newReplyNotifications = mock(ListNotification.class);
		when(newReplyNotifications.getElements()).thenReturn(new ArrayList<PostReply>());
		when(newReplyNotifications.getId()).thenReturn("new-reply-notification");
		return newReplyNotifications;
	}

	private void activateNewReplyNotifications() {
		when(soneOptions.isShowNewReplyNotifications()).thenReturn(true);
	}

	private void addReplyToNewReplyNotification(List<ListNotification<PostReply>> notifications) {
		notifications.get(0).getElements().add(mock(PostReply.class));
	}

	private void setReplyVisibilityPredicate(Predicate<PostReply> value) {
		when(replyVisibilityFilter.isVisible(any(Sone.class))).thenReturn(value);
	}

	@Test
	public void newReplyNotificationContainsOnlyVisibleReplies() {
		activateNewReplyNotifications();
		addReplyToNewReplyNotification(newReplyNotifications);
		addReplyToNewReplyNotification(newReplyNotifications);
		setReplyVisibilityPredicate(new Predicate<PostReply>() {
			@Override
			public boolean apply(@Nullable PostReply postReply) {
				return postReply.equals(newReplyNotifications.get(0).getElements().get(1));
			}
		});
		List<Notification> filteredNotifications = listNotificationFilter.filterNotifications(newReplyNotifications, localSone);
		assertThat(filteredNotifications, hasSize(1));
		assertThat(((ListNotification<PostReply>) filteredNotifications.get(0)).getElements().get(0), is(newReplyNotifications.get(0).getElements().get(1)));
	}

	@Test
	public void newReplyNotificationIsNotModifiedIfAllRepliesAreVisible() {
		activateNewReplyNotifications();
		addReplyToNewReplyNotification(newReplyNotifications);
		addReplyToNewReplyNotification(newReplyNotifications);
		setReplyVisibilityPredicate(Predicates.<PostReply>alwaysTrue());
		List<Notification> filteredNotifications = listNotificationFilter.filterNotifications(newReplyNotifications, localSone);
		assertThat(filteredNotifications, hasSize(1));
		assertThat(filteredNotifications.get(0), is((Notification) newReplyNotifications.get(0)));
		assertThat(((ListNotification<PostReply>) filteredNotifications.get(0)).getElements(), hasSize(2));
	}

	@Test
	public void newReplyNotificationIsNotShownIfNoRepliesAreVisible() {
		activateNewReplyNotifications();
		addReplyToNewReplyNotification(newReplyNotifications);
		addReplyToNewReplyNotification(newReplyNotifications);
		setReplyVisibilityPredicate(Predicates.<PostReply>alwaysFalse());
		List<Notification> filteredNotifications = listNotificationFilter.filterNotifications(newReplyNotifications, localSone);
		assertThat(filteredNotifications, hasSize(0));
	}

	@Test
	public void newReplyNotificationIsNotShownIfDeactivatedInOptions() {
		addReplyToNewReplyNotification(newReplyNotifications);
		addReplyToNewReplyNotification(newReplyNotifications);
		setReplyVisibilityPredicate(Predicates.<PostReply>alwaysTrue());
		List<Notification> filteredNotifications = listNotificationFilter.filterNotifications(newReplyNotifications, localSone);
		assertThat(filteredNotifications, hasSize(0));
	}

	@Test
	public void newReplyNotificationIsNotShownIfCurrentSoneIsNull() {
		addReplyToNewReplyNotification(newReplyNotifications);
		addReplyToNewReplyNotification(newReplyNotifications);
		setReplyVisibilityPredicate(Predicates.<PostReply>alwaysTrue());
		List<Notification> filteredNotifications = listNotificationFilter.filterNotifications(newReplyNotifications, null);
		assertThat(filteredNotifications, hasSize(0));
	}

	private ListNotification<Post> createMentionNotification() {
		ListNotification<Post> newSoneNotification = mock(ListNotification.class);
		when(newSoneNotification.getElements()).thenReturn(new ArrayList<Post>());
		when(newSoneNotification.getId()).thenReturn("mention-notification");
		return newSoneNotification;
	}

	@Test
	public void mentionNotificationContainsOnlyVisiblePosts() {
		addPostToPostNotification(mentionNotifications);
		addPostToPostNotification(mentionNotifications);
		setPostVisibilityPredicate(new Predicate<Post>() {
			@Override
			public boolean apply(@Nullable Post post) {
				return post.equals(mentionNotifications.get(0).getElements().get(1));
			}
		});
		List<Notification> filteredNotifications = listNotificationFilter.filterNotifications(mentionNotifications, localSone);
		assertThat(filteredNotifications, hasSize(1));
		assertThat(((ListNotification<Post>) filteredNotifications.get(0)).getElements().get(0), is(mentionNotifications.get(0).getElements().get(1)));
	}

	@Test
	public void mentionNotificationIsNotShownIfNoPostsAreVisible() {
		addPostToPostNotification(mentionNotifications);
		addPostToPostNotification(mentionNotifications);
		setPostVisibilityPredicate(Predicates.<Post>alwaysFalse());
		List<Notification> filteredNotifications = listNotificationFilter.filterNotifications(mentionNotifications, localSone);
		assertThat(filteredNotifications, hasSize(0));
	}

	@Test
	public void unfilterableNotificationIsNotFiltered() {
		Notification notification = mock(Notification.class);
		when(notification.getId()).thenReturn("random-notification");
		List<Notification> notifications = Arrays.asList(notification);
		List<Notification> filteredNotifications = listNotificationFilter.filterNotifications(notifications, null);
		assertThat(filteredNotifications, contains(notification));
	}

}
