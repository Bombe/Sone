package net.pterodactylus.sone.notify;

import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Sone;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultReplyVisibilityFilter.class)
public interface ReplyVisibilityFilter {

	boolean isReplyVisible(@Nullable Sone sone, @Nonnull PostReply reply);

	default Predicate<PostReply> isVisible(@Nullable final Sone currentSone) {
		return reply -> (reply != null) && isReplyVisible(currentSone, reply);
	}

}
