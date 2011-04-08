/*
 * Sone - Command.java - Copyright © 2011 David Roden
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

package net.pterodactylus.sone.freenet.fcp;

import freenet.support.SimpleFieldSet;
import freenet.support.api.Bucket;

/**
 * Implementation of an FCP interface for other clients or plugins to
 * communicate with Sone.
 *
 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
 */
public interface Command {

	/**
	 * Executes the command, returning a reply that will be sent back to the
	 * requesting plugin.
	 *
	 * @param parameters
	 *            The parameters of the comand
	 * @param data
	 *            The data of the command (may be {@code null})
	 * @param accessType
	 *            The access type
	 * @return A reply to send back to the plugin
	 */
	public Reply execute(SimpleFieldSet parameters, Bucket data, AccessType accessType);

	/**
	 * The access type of the request.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public static enum AccessType {

		/** Access from another plugin. */
		DIRECT,

		/** Access via restricted FCP. */
		RESTRICTED_FCP,

		/** Access via FCP with full access. */
		FULL_FCP,

	}

	/**
	 * Interface for command replies.
	 *
	 * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
	 */
	public static class Reply {

		/** The reply parameters. */
		private final SimpleFieldSet replyParameters;

		/** The reply data, may be {@code null}. */
		private final byte[] data;

		/** The data bucket, may be {@code null}. */
		private final Bucket bucket;

		/**
		 * Creates a new reply with the given parameters.
		 *
		 * @param replyParameters
		 *            The reply parameters
		 */
		public Reply(SimpleFieldSet replyParameters) {
			this(replyParameters, null, null);
		}

		/**
		 * Creates a new reply with the given parameters.
		 *
		 * @param replyParameters
		 *            The reply parameters
		 * @param data
		 *            The data of the reply (may be {@code null})
		 */
		public Reply(SimpleFieldSet replyParameters, byte[] data) {
			this(replyParameters, data, null);
		}

		/**
		 * Creates a new reply with the given parameters.
		 *
		 * @param replyParameters
		 *            The reply parameters
		 * @param bucket
		 *            The bucket of the reply (may be {@code null})
		 */
		public Reply(SimpleFieldSet replyParameters, Bucket bucket) {
			this(replyParameters, null, bucket);
		}

		/**
		 * Creates a new reply with the given parameters.
		 *
		 * @param replyParameters
		 *            The reply parameters
		 * @param data
		 *            The data of the reply (may be {@code null})
		 * @param bucket
		 *            The bucket of the reply (may be {@code null})
		 */
		private Reply(SimpleFieldSet replyParameters, byte[] data, Bucket bucket) {
			this.replyParameters = replyParameters;
			this.data = data;
			this.bucket = bucket;
		}

		/**
		 * Returns the reply parameters.
		 *
		 * @return The reply parameters
		 */
		public SimpleFieldSet getReplyParameters() {
			return replyParameters;
		}

		/**
		 * Returns whether the reply has reply data.
		 *
		 * @see #getData()
		 * @return {@code true} if this reply has data, {@code false} otherwise
		 */
		public boolean hasData() {
			return data != null;
		}

		/**
		 * Returns the data of the reply.
		 *
		 * @return The data of the reply
		 */
		public byte[] getData() {
			return data;
		}

		/**
		 * Returns whether the reply has a data bucket.
		 *
		 * @see #getBucket()
		 * @return {@code true} if the reply has a data bucket, {@code false}
		 *         otherwise
		 */
		public boolean hasBucket() {
			return bucket != null;
		}

		/**
		 * Returns the data bucket of the reply.
		 *
		 * @return The data bucket of the reply
		 */
		public Bucket getBucket() {
			return bucket;
		}

	}

}
