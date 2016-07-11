package net.pterodactylus.sone.web;

import javax.annotation.Nonnull;

import net.pterodactylus.sone.web.page.FreenetTemplatePage.RedirectException;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * Utilities for testing the <code>web</code> package.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class WebTestUtils {

	@Nonnull
	public static Matcher<RedirectException> redirectsTo(@Nonnull final String page) {
		return new TypeSafeDiagnosingMatcher<RedirectException>() {
			@Override
			protected boolean matchesSafely(RedirectException exception, Description mismatchDescription) {
				if (!exception.getTarget().equals(page)) {
					mismatchDescription.appendText("target is ").appendValue(exception.getTarget());
					return false;
				}
				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("target is ").appendValue(page);
			}
		};
	}

}
