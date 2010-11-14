/* Sone JavaScript functions. */

function isOnline() {
	return $("#sone").hasClass("online");
}

function registerInputTextareaSwap(inputSelector, defaultText, inputFieldName, optional, dontUseTextarea) {
	$(inputSelector).each(function() {
		textarea = $(dontUseTextarea ? "<input type=\"text\" name=\"" + inputFieldName + "\">" : "<textarea name=\"" + inputFieldName + "\"></textarea>").blur(function() {
			if ($(this).val() == "") {
				$(this).hide();
				inputField = $(this).data("inputField");
				inputField.show().removeAttr("disabled").addClass("default");
				inputField.val(defaultText);
			}
		}).hide().data("inputField", $(this)).val($(this).val());
		$(this).after(textarea);
		(function(inputField, textarea) {
			inputField.focus(function() {
				$(this).hide().attr("disabled", "disabled");
				textarea.show().focus();
			});
			if (inputField.val() == "") {
				inputField.addClass("default");
				inputField.val(defaultText);
			} else {
				inputField.hide().attr("disabled", "disabled");
				textarea.show();
			}
			$(inputField.get(0).form).submit(function() {
				if (!optional && (textarea.val() == "")) {
					return false;
				}
			});
		})($(this), textarea);
	});
}

/* hide all the “create reply” forms until a link is clicked. */
function addCommentLinks() {
	if (!isOnline()) {
		return;
	}
	$("#sone .post").each(function() {
		postId = $(this).attr("id");
		addCommentLink(postId, $(this));
	});
}

/**
 * Adds a “comment” link to all status lines contained in the given element.
 *
 * @param postId
 *            The ID of the post
 * @param element
 *            The element to add a “comment” link to
 */
function addCommentLink(postId, element) {
	commentElement = (function(postId) {
		var commentElement = $("<div><span>Comment</span></div>").addClass("show-reply-form").click(function() {
			replyElement = $("#sone .post#" + postId + " .create-reply");
			replyElement.removeClass("hidden");
			replyElement.removeClass("light");
			(function(replyElement) {
				replyElement.find("input.reply-input").blur(function() {
					if ($(this).hasClass("default")) {
						replyElement.addClass("light");
					}
				}).focus(function() {
					replyElement.removeClass("light");
				});
			})(replyElement);
			replyElement.find("input.reply-input").focus();
		});
		return commentElement;
	})(postId);
	element.find(".create-reply").addClass("hidden");
	element.find(".status-line .time").each(function() {
		$(this).after(commentElement.clone(true));
	});
}

/**
 * Retrieves the translation for the given key and calls the callback function.
 * The callback function takes a single parameter, the translated string.
 *
 * @param key
 *            The key of the translation string
 * @param callback
 *            The callback function
 */
function getTranslation(key, callback) {
	$.getJSON("ajax/getTranslation.ajax", {"key": key}, function(data, textStatus) {
		callback(data.value);
	});
}

/**
 * Fires off an AJAX request to retrieve the current status of a Sone.
 *
 * @param soneId
 *            The ID of the Sone
 * @param local
 *            <code>true</code> if the Sone is local, <code>false</code>
 *            otherwise
 */
function getSoneStatus(soneId, local) {
	$.getJSON("ajax/getSoneStatus.ajax", {"sone": soneId}, function(data, textStatus) {
		updateSoneStatus(soneId, data.name, data.status, data.modified, data.lastUpdated);
		/* seconds! */
		updateInterval = 60;
		if (local || data.modified || (data.status == "downloading") || (data.status == "inserting")) {
			updateInterval = 5;
		}
		setTimeout(function() {
			getSoneStatus(soneId, local);
		}, updateInterval * 1000);
	});
}

/**
 * Filters the given Sone ID, replacing all “~” characters by an underscore.
 *
 * @param soneId
 *            The Sone ID to filter
 * @returns The filtered Sone ID
 */
function filterSoneId(soneId) {
	return soneId.replace(/[^a-zA-Z0-9-]/g, "_");
}

/**
 * Updates the status of the given Sone.
 *
 * @param soneId
 *            The ID of the Sone to update
 * @param status
 *            The status of the Sone (“idle”, “unknown”, “inserting”,
 *            “downloading”)
 * @param modified
 *            Whether the Sone is modified
 * @param lastUpdated
 *            The date and time of the last update (formatted for display)
 */
function updateSoneStatus(soneId, name, status, modified, lastUpdated) {
	$("#sone .sone." + filterSoneId(soneId)).
		toggleClass("unknown", status == "unknown").
		toggleClass("idle", status == "idle").
		toggleClass("inserting", status == "inserting").
		toggleClass("downloading", status == "downloading").
		toggleClass("modified", modified);
	$("#sone .sone." + filterSoneId(soneId) + " .last-update span.time").text(lastUpdated);
	$("#sone .sone." + filterSoneId(soneId) + " .profile-link a").text(name);
}

var watchedSones = {};

/**
 * Watches this Sone for updates to its status.
 *
 * @param soneId
 *            The ID of the Sone to watch
 * @param local
 *            <code>true</code> if the Sone is local, <code>false</code>
 *            otherwise
 */
function watchSone(soneId, local) {
	if (watchedSones[soneId]) {
		return;
	}
	watchedSones[soneId] = true;
	(function(soneId) {
		setTimeout(function() {
			getSoneStatus(soneId, local);
		}, 5000);
	})(soneId);
}

/**
 * Enhances a “delete” button so that the confirmation is done on the same page.
 *
 * @param buttonId
 *            The selector of the button
 * @param text
 *            The text to show on the button
 * @param deleteCallback
 *            The callback that actually deletes something
 */
function enhanceDeleteButton(buttonId, text, deleteCallback) {
	button = $(buttonId);
	(function(button) {
		newButton = $("<button></button>").addClass("confirm").hide().text(text).click(function() {
			$(this).fadeOut("slow");
			deleteCallback();
			return false;
		}).insertAfter(button);
		(function(button, newButton) {
			button.click(function() {
				button.fadeOut("slow", function() {
					newButton.fadeIn("slow");
					$(document).one("click", function() {
						if (this != newButton.get(0)) {
							newButton.fadeOut(function() {
								button.fadeIn();
							});
						}
					});
				});
				return false;
			});
		})(button, newButton);
	})(button);
}

/**
 * Enhances a post’s “delete” button.
 *
 * @param buttonId
 *            The selector of the button
 * @param postId
 *            The ID of the post to delete
 * @param text
 *            The text to replace the button with
 */
function enhanceDeletePostButton(buttonId, postId, text) {
	enhanceDeleteButton(buttonId, text, function() {
		$.getJSON("ajax/deletePost.ajax", { "post": postId, "formPassword": $("#sone #formPassword").text() }, function(data, textStatus) {
			if (data.success) {
				$("#sone .post#" + postId).slideUp();
			} else if (data.error == "invalid-post-id") {
				alert("Invalid post ID given!");
			} else if (data.error == "auth-required") {
				alert("You need to be logged in.");
			} else if (data.error == "not-authorized") {
				alert("You are not allowed to delete this post.");
			}
		});
	});
}

/**
 * Enhances a reply’s “delete” button.
 *
 * @param buttonId
 *            The selector of the button
 * @param replyId
 *            The ID of the reply to delete
 * @param text
 *            The text to replace the button with
 */
function enhanceDeleteReplyButton(buttonId, replyId, text) {
	enhanceDeleteButton(buttonId, text, function() {
		$.getJSON("ajax/deleteReply.ajax", { "reply": replyId, "formPassword": $("#sone #formPassword").text() }, function(data, textStatus) {
			if (data.success) {
				$("#sone .reply#" + replyId).slideUp();
			} else if (data.error == "invalid-reply-id") {
				alert("Invalid reply ID given!");
			} else if (data.error == "auth-required") {
				alert("You need to be logged in.");
			} else if (data.error == "not-authorized") {
				alert("You are not allowed to delete this reply.");
			}
		});
	});
}

function getFormPassword() {
	return $("#sone #formPassword").text();
}

function getSoneElement(element) {
	return $(element).parents(".sone");
}

/**
 * Generates a list of Sones by concatening the names of the given sones with a
 * new line character (“\n”).
 *
 * @param sones
 *            The sones to format
 * @returns {String} The created string
 */
function generateSoneList(sones) {
	var soneList = "";
	$.each(sones, function() {
		if (soneList != "") {
			soneList += "\n";
		}
		soneList += this.name;
	});
	return soneList;
}

/**
 * Returns the ID of the Sone that this element belongs to.
 *
 * @param element
 *            The element to locate the matching Sone ID for
 * @returns The ID of the Sone, or undefined
 */
function getSoneId(element) {
	return getSoneElement(element).find(".id").text();
}

function getPostElement(element) {
	return $(element).parents(".post");
}

function getPostId(element) {
	return getPostElement(element).attr("id");
}

function getReplyElement(element) {
	return $(element).parents(".reply");
}

function getReplyId(element) {
	return getReplyElement(element).attr("id");
}

function likePost(postId) {
	$.getJSON("ajax/like.ajax", { "type": "post", "post" : postId, "formPassword": getFormPassword() }, function() {
		$("#sone .post#" + postId + " > .inner-part > .status-line .like").addClass("hidden");
		$("#sone .post#" + postId + " > .inner-part > .status-line .unlike").removeClass("hidden");
		updatePostLikes(postId);
	});
}

function unlikePost(postId) {
	$.getJSON("ajax/unlike.ajax", { "type": "post", "post" : postId, "formPassword": getFormPassword() }, function() {
		$("#sone .post#" + postId + " > .inner-part > .status-line .unlike").addClass("hidden");
		$("#sone .post#" + postId + " > .inner-part > .status-line .like").removeClass("hidden");
		updatePostLikes(postId);
	});
}

function updatePostLikes(postId) {
	$.getJSON("ajax/getLikes.ajax", { "type": "post", "post": postId }, function(data, textStatus) {
		if (data.success) {
			$("#sone .post#" + postId + " > .inner-part > .status-line .likes").toggleClass("hidden", data.likes == 0)
			$("#sone .post#" + postId + " > .inner-part > .status-line .likes span.like-count").text(data.likes);
			$("#sone .post#" + postId + " > .inner-part > .status-line .likes > span").attr("title", generateSoneList(data.sones));
		}
	});
}

function likeReply(replyId) {
	$.getJSON("ajax/like.ajax", { "type": "reply", "reply" : replyId, "formPassword": getFormPassword() }, function() {
		$("#sone .reply#" + replyId + " .status-line .like").addClass("hidden");
		$("#sone .reply#" + replyId + " .status-line .unlike").removeClass("hidden");
		updateReplyLikes(replyId);
	});
}

function unlikeReply(replyId) {
	$.getJSON("ajax/unlike.ajax", { "type": "reply", "reply" : replyId, "formPassword": getFormPassword() }, function() {
		$("#sone .reply#" + replyId + " .status-line .unlike").addClass("hidden");
		$("#sone .reply#" + replyId + " .status-line .like").removeClass("hidden");
		updateReplyLikes(replyId);
	});
}

function updateReplyLikes(replyId) {
	$.getJSON("ajax/getLikes.ajax", { "type": "reply", "reply": replyId }, function(data, textStatus) {
		if (data.success) {
			$("#sone .reply#" + replyId + " .status-line .likes").toggleClass("hidden", data.likes == 0)
			$("#sone .reply#" + replyId + " .status-line .likes span.like-count").text(data.likes);
			$("#sone .reply#" + replyId + " .status-line .likes > span").attr("title", generateSoneList(data.sones));
		}
	});
}

/**
 * Posts a reply and calls the given callback when the request finishes.
 *
 * @param postId
 *            The ID of the post the reply refers to
 * @param text
 *            The text to post
 * @param callbackFunction
 *            The callback function to call when the request finishes (takes 3
 *            parameters: success, error, replyId)
 */
function postReply(postId, text, callbackFunction) {
	$.getJSON("ajax/createReply.ajax", { "formPassword" : getFormPassword(), "post" : postId, "text": text }, function(data, textStatus) {
		if (data.success) {
			callbackFunction(true, null, data.reply);
		} else {
			callbackFunction(false, data.error);
		}
	});
}

/**
 * Requests information about the reply with the given ID.
 *
 * @param replyId
 *            The ID of the reply
 * @param callbackFunction
 *            A callback function (parameters soneId, soneName, replyTime,
 *            replyDisplayTime, text, html)
 */
function getReply(replyId, callbackFunction) {
	$.getJSON("ajax/getReply.ajax", { "reply" : replyId }, function(data, textStatus) {
		if (data.success) {
			callbackFunction(data.soneId, data.soneName, data.time, data.displayTime, data.text, data.html);
		}
	});
}

/**
 * Ajaxifies the given notification by replacing the form with AJAX.
 *
 * @param notification
 *            jQuery object representing the notification.
 */
function ajaxifyNotificationArea(notification) {
	notification.find("form.dismiss").submit(function() {
		return false;
	});
	notification.find("form.dismiss button").click(function() {
		$.getJSON("ajax/dismissNotification.ajax", { "formPassword" : getFormPassword(), "notification" : notification.attr("id") }, function(data, textStatus) {
			if (data.success) {
				notification.slideUp();
			}
		});
	});
}
