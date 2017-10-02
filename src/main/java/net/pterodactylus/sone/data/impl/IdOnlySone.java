package net.pterodactylus.sone.data.impl;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.pterodactylus.sone.data.Album;
import net.pterodactylus.sone.data.Client;
import net.pterodactylus.sone.data.Post;
import net.pterodactylus.sone.data.PostReply;
import net.pterodactylus.sone.data.Profile;
import net.pterodactylus.sone.data.Sone;
import net.pterodactylus.sone.data.SoneOptions;
import net.pterodactylus.sone.freenet.wot.Identity;

import freenet.keys.FreenetURI;

import com.google.common.base.Objects;

/**
 * {@link Sone} implementation that only stores the ID of a Sone and returns
 * {@code null}, {@code 0}, or empty collections where appropriate.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public class IdOnlySone implements Sone {

	private final String id;

	public IdOnlySone(String id) {
		this.id = id;
	}

	@Override
	public Identity getIdentity() {
		return null;
	}

	@Override
	public String getName() {
		return id;
	}

	@Override
	public boolean isLocal() {
		return false;
	}

	@Override
	public FreenetURI getRequestUri() {
		return null;
	}

	@Override
	public FreenetURI getInsertUri() {
		return null;
	}

	@Override
	public long getLatestEdition() {
		return 0;
	}

	@Override
	public void setLatestEdition(long latestEdition) {
	}

	@Override
	public long getTime() {
		return 0;
	}

	@Override
	public Sone setTime(long time) {
		return null;
	}

	@Override
	public SoneStatus getStatus() {
		return null;
	}

	@Override
	public Sone setStatus(SoneStatus status) {
		return null;
	}

	@Override
	public Profile getProfile() {
		return new Profile(this);
	}

	@Override
	public void setProfile(Profile profile) {
	}

	@Override
	public Client getClient() {
		return null;
	}

	@Override
	public Sone setClient(Client client) {
		return null;
	}

	@Override
	public boolean isKnown() {
		return false;
	}

	@Override
	public Sone setKnown(boolean known) {
		return null;
	}

	@Override
	public List<String> getFriends() {
		return emptyList();
	}

	@Override
	public boolean hasFriend(String friendSoneId) {
		return false;
	}

	@Override
	public List<Post> getPosts() {
		return emptyList();
	}

	@Override
	public Sone setPosts(Collection<Post> posts) {
		return this;
	}

	@Override
	public void addPost(Post post) {
	}

	@Override
	public void removePost(Post post) {
	}

	@Override
	public Set<PostReply> getReplies() {
		return emptySet();
	}

	@Override
	public Sone setReplies(Collection<PostReply> replies) {
		return this;
	}

	@Override
	public void addReply(PostReply reply) {
	}

	@Override
	public void removeReply(PostReply reply) {
	}

	@Override
	public Set<String> getLikedPostIds() {
		return emptySet();
	}

	@Override
	public Sone setLikePostIds(Set<String> likedPostIds) {
		return this;
	}

	@Override
	public boolean isLikedPostId(String postId) {
		return false;
	}

	@Override
	public Sone addLikedPostId(String postId) {
		return this;
	}

	@Override
	public void removeLikedPostId(String postId) {
	}

	@Override
	public Set<String> getLikedReplyIds() {
		return emptySet();
	}

	@Override
	public Sone setLikeReplyIds(Set<String> likedReplyIds) {
		return this;
	}

	@Override
	public boolean isLikedReplyId(String replyId) {
		return false;
	}

	@Override
	public Sone addLikedReplyId(String replyId) {
		return this;
	}

	@Override
	public void removeLikedReplyId(String replyId) {
	}

	@Override
	public Album getRootAlbum() {
		return null;
	}

	@Override
	public SoneOptions getOptions() {
		return null;
	}

	@Override
	public void setOptions(SoneOptions options) {
	}

	@Override
	public int compareTo(Sone o) {
		return 0;
	}

	@Override
	public String getFingerprint() {
		return null;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		return (object != null) && (object.getClass() == getClass()) && Objects.equal(id, ((IdOnlySone) object).id);
	}

}
