package net.pterodactylus.sone.notify;

import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.Sone;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultPostVisibilityFilter.class)
public interface PostVisibilityFilter {

	boolean isPostVisible(@Nullable Sone sone, @Nonnull Post post);

	@Nonnull
	Predicate<Post> isVisible(@Nullable Sone currentSone);

}
