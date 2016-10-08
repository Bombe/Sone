package net.pterodactylus.sone.notify;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import java.util.Arrays;

import net.pterodactylus.util.notify.NotificationListener;
import net.pterodactylus.util.template.Template;
import net.pterodactylus.util.template.TemplateContext;

import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Unit test for {@link ListNotification}.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class ListNotificationTest {

	private static final String ID = "notification-id";
	private static final String KEY = "element-key";
	private static final String OTHER_KEY = "other-key";

	private final Template template = mock(Template.class);
	private final TemplateContext templateInitialContext = mock(TemplateContext.class);
	private ListNotification<Object> listNotification;

	public ListNotificationTest() {
		when(template.getInitialContext()).thenReturn(templateInitialContext);
		listNotification = new ListNotification<Object>(ID, KEY, template);
	}

	@Test
	public void creatingAListNotificationSetsEmptyIterableOnElementKeyInTemplateContext() {
		verify(templateInitialContext).set(eq(KEY), argThat(emptyIterable()));
	}

	@Test
	public void newListNotificationHasNoElement() {
		assertThat(listNotification.getElements(), emptyIterable());
	}

	@Test
	public void newListNotificationIsEmpty() {
		assertThat(listNotification.isEmpty(), is(true));
	}

	@Test
	public void listNotificationRetainsSetElements() {
		listNotification.setElements(Arrays.asList("a", "b", "c"));
		assertThat(listNotification.getElements(), Matchers.<Object>contains("a", "b", "c"));
	}

	@Test
	public void listNotificationRetainsAddedElements() {
		listNotification.add("a");
		listNotification.add("b");
		listNotification.add("c");
		assertThat(listNotification.getElements(), Matchers.<Object>contains("a", "b", "c"));
	}

	@Test
	public void listNotificationRemovesCorrectElement() {
		listNotification.setElements(Arrays.asList("a", "b", "c"));
		listNotification.remove("b");
		assertThat(listNotification.getElements(), Matchers.<Object>contains("a", "c"));
	}

	@Test
	public void removingTheLastElementDismissesTheNotification() {
		NotificationListener notificationListener = mock(NotificationListener.class);
		listNotification.addNotificationListener(notificationListener);
		listNotification.add("a");
		listNotification.remove("a");
		verify(notificationListener).notificationDismissed(listNotification);
	}

	@Test
	public void dismissingTheListNotificationRemovesAllElements() {
		listNotification.setElements(Arrays.asList("a", "b", "c"));
		listNotification.dismiss();
		assertThat(listNotification.getElements(), emptyIterable());
	}

	@Test
	public void listNotificationWithDifferentElementsIsNotEqual() {
		ListNotification secondNotification = new ListNotification(ID, KEY, template);
		listNotification.add("a");
		secondNotification.add("b");
		assertThat(listNotification, not(is(secondNotification)));
	}

	@Test
	public void listNotificationWithDifferentKeyIsNotEqual() {
		ListNotification secondNotification = new ListNotification(ID, OTHER_KEY, template);
		assertThat(listNotification, not(is(secondNotification)));
	}

	@Test
	public void copiedNotificationsHaveTheSameHashCode() {
		ListNotification secondNotification = new ListNotification(listNotification);
		listNotification.add("a");
		secondNotification.add("a");
		assertThat(listNotification.hashCode(), is(secondNotification.hashCode()));
	}

	@Test
	public void listNotificationIsNotEqualToOtherObjects() {
	    assertThat(listNotification, not(is(new Object())));
	}

}
