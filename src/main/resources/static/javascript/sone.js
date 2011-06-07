/* Sone JavaScript functions. */

function ajaxGet(url, data, successCallback, errorCallback) {
	(function(url, data, successCallback, errorCallback) {
		$.ajax({"type": "GET", "url": url, "data": data, "dataType": "json", "success": function(data, textStatus, xmlHttpRequest) {
			ajaxSuccess();
			if (typeof successCallback != "undefined") {
				successCallback(data, textStatus);
			}
		}, "error": function(xmlHttpRequest, textStatus, errorThrown) {
			if (xmlHttpRequest.status == 403) {
				notLoggedIn = true;
			}
			if (typeof errorCallback != "undefined") {
				errorCallback();
			} else {
				ajaxError();
			}
		}});
	})(url, data, successCallback, errorCallback);
}

function registerInputTextareaSwap(inputElement, defaultText, inputFieldName, optional, dontUseTextarea) {
	$(inputElement).each(function() {
		textarea = $(dontUseTextarea ? "<input type=\"text\" name=\"" + inputFieldName + "\">" : "<textarea name=\"" + inputFieldName + "\"></textarea>").blur(function() {
			if ($(this).val() == "") {
				$(this).hide();
				inputField = $(this).data("inputField");
				inputField.show().removeAttr("disabled").addClass("default");
				inputField.val(defaultText);
			}
		}).hide().data("inputField", $(this)).val($(this).val());
		$(this).data("textarea", textarea).after(textarea);
		(function(inputField, textarea) {
			inputField.focus(function() {
				$(this).hide().attr("disabled", "disabled");
				/* no, show(), “display: block” is not what I need. */
				textarea.attr("style", "display: inline").focus();
			});
			if (inputField.val() == "") {
				inputField.addClass("default");
				inputField.val(defaultText);
			} else {
				inputField.hide().attr("disabled", "disabled");
				textarea.show();
			}
			$(inputField.get(0).form).submit(function() {
				inputField.attr("disabled", "disabled");
				if (!optional && (textarea.val() == "")) {
					inputField.removeAttr("disabled").focus();
					return false;
				}
			});
		})($(this), textarea);
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
function addCommentLink(postId, author, element, insertAfterThisElement) {
	if (($(element).find(".show-reply-form").length > 0) || (getPostElement(element).find(".create-reply").length == 0)) {
		return;
	}
	commentElement = (function(postId, author) {
		separator = $("<span> · </span>").addClass("separator");
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
			textArea = replyElement.find("input.reply-input").focus().data("textarea");
			if (author != getCurrentSoneId()) {
				textArea.val(textArea.val() + "@sone://" + author + " ");
			}
		});
		return commentElement;
	})(postId, author);
	$(insertAfterThisElement).after(commentElement.clone(true));
	$(insertAfterThisElement).after(separator);
}

var translations = {};

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
	if (key in translations) {
		callback(translations[key]);
		return;
	}
	ajaxGet("getTranslation.ajax", {"key": key}, function(data, textStatus) {
		if ((data != null) && data.success) {
			translations[key] = data.value;
			callback(data.value);
		}
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
 * @param locked
 *            Whether the Sone is locked
 * @param lastUpdated
 *            The date and time of the last update (formatted for display)
 */
function updateSoneStatus(soneId, name, status, modified, locked, lastUpdated, lastUpdatedText) {
	$("#sone .sone." + filterSoneId(soneId)).
		toggleClass("unknown", status == "unknown").
		toggleClass("idle", status == "idle").
		toggleClass("inserting", status == "inserting").
		toggleClass("downloading", status == "downloading").
		toggleClass("modified", modified);
	$("#sone .sone." + filterSoneId(soneId) + " .lock").toggleClass("hidden", locked);
	$("#sone .sone." + filterSoneId(soneId) + " .unlock").toggleClass("hidden", !locked);
	if (lastUpdated != null) {
		$("#sone .sone." + filterSoneId(soneId) + " .last-update span.time").attr("title", lastUpdated).text(lastUpdatedText);
	} else {
		getTranslation("View.Sone.Text.UnknownDate", function(unknown) {
			$("#sone .sone." + filterSoneId(soneId) + " .last-update span.time").text(unknown);
		});
	}
	$("#sone .sone." + filterSoneId(soneId) + " .profile-link a").text(name);
}

/**
 * Enhances a “delete” button so that the confirmation is done on the same page.
 *
 * @param button
 *            The button element
 * @param text
 *            The text to show on the button
 * @param deleteCallback
 *            The callback that actually deletes something
 */
function enhanceDeleteButton(button, text, deleteCallback) {
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
	})($(button));
}

/**
 * Enhances a post’s “delete” button.
 *
 * @param button
 *            The button element
 * @param postId
 *            The ID of the post to delete
 * @param text
 *            The text to replace the button with
 */
function enhanceDeletePostButton(button, postId, text) {
	enhanceDeleteButton(button, text, function() {
		ajaxGet("deletePost.ajax", { "post": postId, "formPassword": getFormPassword() }, function(data, textStatus) {
			if (data == null) {
				return;
			}
			if (data.success) {
				$("#sone .post#" + postId).slideUp();
			} else if (data.error == "invalid-post-id") {
				/* pretend the post is already gone. */
				getPost(postId).slideUp();
			} else if (data.error == "auth-required") {
				alert("You need to be logged in.");
			} else if (data.error == "not-authorized") {
				alert("You are not allowed to delete this post.");
			}
		}, function(xmlHttpRequest, textStatus, error) {
			/* ignore error. */
		});
	});
}

/**
 * Enhances a reply’s “delete” button.
 *
 * @param button
 *            The button element
 * @param replyId
 *            The ID of the reply to delete
 * @param text
 *            The text to replace the button with
 */
function enhanceDeleteReplyButton(button, replyId, text) {
	enhanceDeleteButton(button, text, function() {
		ajaxGet("deleteReply.ajax", { "reply": replyId, "formPassword": $("#sone #formPassword").text() }, function(data, textStatus) {
			if (data == null) {
				return;
			}
			if (data.success) {
				$("#sone .reply#" + replyId).slideUp();
			} else if (data.error == "invalid-reply-id") {
				/* pretend the reply is already gone. */
				getReply(replyId).slideUp();
			} else if (data.error == "auth-required") {
				alert("You need to be logged in.");
			} else if (data.error == "not-authorized") {
				alert("You are not allowed to delete this reply.");
			}
		}, function(xmlHttpRequest, textStatus, error) {
			/* ignore error. */
		});
	});
}

function getFormPassword() {
	return $("#sone #formPassword").text();
}

/**
 * Returns the element of the Sone with the given ID.
 *
 * @param soneId
 *            The ID of the Sone
 * @returns All Sone elements with the given ID
 */
function getSone(soneId) {
	return $("#sone .sone").filter(function(index) {
		return $(".id", this).text() == soneId;
	});
}

function getSoneElement(element) {
	return $(element).closest(".sone");
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
			soneList += ", ";
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

/**
 * Returns the element of the post with the given ID.
 *
 * @param postId
 *            The ID of the post
 * @returns The element of the post
 */
function getPost(postId) {
	return $("#sone .post#" + postId);
}

function getPostElement(element) {
	return $(element).closest(".post");
}

function getPostId(element) {
	return getPostElement(element).attr("id");
}

function getPostTime(element) {
	return getPostElement(element).find(".post-time").text();
}

/**
 * Returns the author of the post the given element belongs to.
 *
 * @param element
 *            The element whose post to get the author for
 * @returns The ID of the authoring Sone
 */
function getPostAuthor(element) {
	return getPostElement(element).find(".post-author").text();
}

/**
 * Returns the element of the reply with the given ID.
 *
 * @param replyId
 *            The ID of the reply
 * @returns The element of the reply
 */
function getReply(replyId) {
	return $("#sone .reply#" + replyId);
}

function getReplyElement(element) {
	return $(element).closest(".reply");
}

function getReplyId(element) {
	return getReplyElement(element).attr("id");
}

function getReplyTime(element) {
	return getReplyElement(element).find(".reply-time").text();
}

/**
 * Returns the author of the reply the given element belongs to.
 *
 * @param element
 *            The element whose reply to get the author for
 * @returns The ID of the authoring Sone
 */
function getReplyAuthor(element) {
	return getReplyElement(element).find(".reply-author").text();
}

/**
 * Returns the notification with the given ID.
 *
 * @param notificationId
 *            The ID of the notification
 * @returns The notification element
 */
function getNotification(notificationId) {
	return $("#sone #notification-area .notification#" + notificationId);
}

/**
 * Returns the notification element closest to the given element.
 *
 * @param element
 *            The element to get the closest notification of
 * @return The closest notification element
 */
function getNotificationElement(element) {
	return $(element).closest(".notification");
}

/**
 * Returns the ID of the notification element.
 *
 * @param notificationElement
 *            The notification element
 * @returns The ID of the notification
 */
function getNotificationId(notificationElement) {
	return $(notificationElement).attr("id");
}

/**
 * Returns the time the notification was last updated.
 *
 * @param notificationElement
 *            The notification element
 * @returns The last update time of the notification
 */
function getNotificationLastUpdatedTime(notificationElement) {
	return $(notificationElement).attr("lastUpdatedTime");
}

function likePost(postId) {
	ajaxGet("like.ajax", { "type": "post", "post" : postId, "formPassword": getFormPassword() }, function(data, textStatus) {
		if ((data == null) || !data.success) {
			return;
		}
		$("#sone .post#" + postId + " > .inner-part > .status-line .like").addClass("hidden");
		$("#sone .post#" + postId + " > .inner-part > .status-line .unlike").removeClass("hidden");
		updatePostLikes(postId);
	}, function(xmlHttpRequest, textStatus, error) {
		/* ignore error. */
	});
}

function unlikePost(postId) {
	ajaxGet("unlike.ajax", { "type": "post", "post" : postId, "formPassword": getFormPassword() }, function(data, textStatus) {
		if ((data == null) || !data.success) {
			return;
		}
		$("#sone .post#" + postId + " > .inner-part > .status-line .unlike").addClass("hidden");
		$("#sone .post#" + postId + " > .inner-part > .status-line .like").removeClass("hidden");
		updatePostLikes(postId);
	}, function(xmlHttpRequest, textStatus, error) {
		/* ignore error. */
	});
}

function updatePostLikes(postId) {
	ajaxGet("getLikes.ajax", { "type": "post", "post": postId }, function(data, textStatus) {
		if ((data != null) && data.success) {
			$("#sone .post#" + postId + " > .inner-part > .status-line .likes").toggleClass("hidden", data.likes == 0);
			$("#sone .post#" + postId + " > .inner-part > .status-line .likes span.like-count").text(data.likes);
			$("#sone .post#" + postId + " > .inner-part > .status-line .likes > span").attr("title", generateSoneList(data.sones));
		}
	}, function(xmlHttpRequest, textStatus, error) {
		/* ignore error. */
	});
}

function likeReply(replyId) {
	ajaxGet("like.ajax", { "type": "reply", "reply" : replyId, "formPassword": getFormPassword() }, function(data, textStatus) {
		if ((data == null) || !data.success) {
			return;
		}
		$("#sone .reply#" + replyId + " .status-line .like").addClass("hidden");
		$("#sone .reply#" + replyId + " .status-line .unlike").removeClass("hidden");
		updateReplyLikes(replyId);
	}, function(xmlHttpRequest, textStatus, error) {
		/* ignore error. */
	});
}

function unlikeReply(replyId) {
	ajaxGet("unlike.ajax", { "type": "reply", "reply" : replyId, "formPassword": getFormPassword() }, function(data, textStatus) {
		if ((data == null) || !data.success) {
			return;
		}
		$("#sone .reply#" + replyId + " .status-line .unlike").addClass("hidden");
		$("#sone .reply#" + replyId + " .status-line .like").removeClass("hidden");
		updateReplyLikes(replyId);
	}, function(xmlHttpRequest, textStatus, error) {
		/* ignore error. */
	});
}

/**
 * Trusts the Sone with the given ID.
 *
 * @param soneId
 *            The ID of the Sone to trust
 */
function trustSone(soneId) {
	ajaxGet("trustSone.ajax", { "formPassword" : getFormPassword(), "sone" : soneId }, function(data, textStatus) {
		if ((data != null) && data.success) {
			updateTrustControls(soneId, data.trustValue);
		}
	});
}

/**
 * Distrusts the Sone with the given ID, i.e. assigns a negative trust value.
 *
 * @param soneId
 *            The ID of the Sone to distrust
 */
function distrustSone(soneId) {
	ajaxGet("distrustSone.ajax", { "formPassword" : getFormPassword(), "sone" : soneId }, function(data, textStatus) {
		if ((data != null) && data.success) {
			updateTrustControls(soneId, data.trustValue);
		}
	});
}

/**
 * Untrusts the Sone with the given ID, i.e. removes any trust assignment.
 *
 * @param soneId
 *            The ID of the Sone to untrust
 */
function untrustSone(soneId) {
	ajaxGet("untrustSone.ajax", { "formPassword" : getFormPassword(), "sone" : soneId }, function(data, textStatus) {
		if ((data != null) && data.success) {
			updateTrustControls(soneId, data.trustValue);
		}
	});
}

/**
 * Updates the trust controls for all posts and replies of the given Sone,
 * according to the given trust value.
 *
 * @param soneId
 *            The ID of the Sone to update all trust controls for
 * @param trustValue
 *            The trust value for the Sone
 */
function updateTrustControls(soneId, trustValue) {
	$("#sone .post").each(function() {
		if (getPostAuthor(this) == soneId) {
			getPostElement(this).find(".post-trust").toggleClass("hidden", trustValue != null);
			getPostElement(this).find(".post-distrust").toggleClass("hidden", trustValue != null);
			getPostElement(this).find(".post-untrust").toggleClass("hidden", trustValue == null);
		}
	});
	$("#sone .reply").each(function() {
		if (getReplyAuthor(this) == soneId) {
			getReplyElement(this).find(".reply-trust").toggleClass("hidden", trustValue != null);
			getReplyElement(this).find(".reply-distrust").toggleClass("hidden", trustValue != null);
			getReplyElement(this).find(".reply-untrust").toggleClass("hidden", trustValue == null);
		}
	});
}

/**
 * Bookmarks the post with the given ID.
 *
 * @param postId
 *            The ID of the post to bookmark
 */
function bookmarkPost(postId) {
	(function(postId) {
		ajaxGet("bookmark.ajax", {"formPassword": getFormPassword(), "type": "post", "post": postId}, function(data, textStatus) {
			if ((data != null) && data.success) {
				getPost(postId).find(".bookmark").toggleClass("hidden", true);
				getPost(postId).find(".unbookmark").toggleClass("hidden", false);
			}
		});
	})(postId);
}

/**
 * Unbookmarks the post with the given ID.
 *
 * @param postId
 *            The ID of the post to unbookmark
 */
function unbookmarkPost(postId) {
	ajaxGet("unbookmark.ajax", {"formPassword": getFormPassword(), "type": "post", "post": postId}, function(data, textStatus) {
		if ((data != null) && data.success) {
			getPost(postId).find(".bookmark").toggleClass("hidden", false);
			getPost(postId).find(".unbookmark").toggleClass("hidden", true);
		}
	});
}

function updateReplyLikes(replyId) {
	ajaxGet("getLikes.ajax", { "type": "reply", "reply": replyId }, function(data, textStatus) {
		if ((data != null) && data.success) {
			$("#sone .reply#" + replyId + " .status-line .likes").toggleClass("hidden", data.likes == 0);
			$("#sone .reply#" + replyId + " .status-line .likes span.like-count").text(data.likes);
			$("#sone .reply#" + replyId + " .status-line .likes > span").attr("title", generateSoneList(data.sones));
		}
	}, function(xmlHttpRequest, textStatus, error) {
		/* ignore error. */
	});
}

/**
 * Posts a reply and calls the given callback when the request finishes.
 *
 * @param sender
 *            The ID of the sender
 * @param postId
 *            The ID of the post the reply refers to
 * @param text
 *            The text to post
 * @param callbackFunction
 *            The callback function to call when the request finishes (takes 3
 *            parameters: success, error, replyId)
 */
function postReply(sender, postId, text, callbackFunction) {
	ajaxGet("createReply.ajax", { "formPassword" : getFormPassword(), "sender": sender, "post" : postId, "text": text }, function(data, textStatus) {
		if (data == null) {
			/* TODO - show error */
			return;
		}
		if (data.success) {
			callbackFunction(true, null, data.reply, data.sone);
		} else {
			callbackFunction(false, data.error);
		}
	}, function(xmlHttpRequest, textStatus, error) {
		/* ignore error. */
	});
}

/**
 * Ajaxifies the given Sone by enhancing all eligible elements with AJAX.
 *
 * @param soneElement
 *            The Sone to ajaxify
 */
function ajaxifySone(soneElement) {
	/*
	 * convert all “follow”, “unfollow”, “lock”, and “unlock” links to something
	 * nicer.
	 */
	$(".follow", soneElement).submit(function() {
		var followElement = this;
		ajaxGet("followSone.ajax", { "sone": getSoneId(this), "formPassword": getFormPassword() }, function() {
			$(followElement).addClass("hidden");
			$(followElement).parent().find(".unfollow").removeClass("hidden");
		});
		return false;
	});
	$(".unfollow", soneElement).submit(function() {
		var unfollowElement = this;
		ajaxGet("unfollowSone.ajax", { "sone": getSoneId(this), "formPassword": getFormPassword() }, function() {
			$(unfollowElement).addClass("hidden");
			$(unfollowElement).parent().find(".follow").removeClass("hidden");
		});
		return false;
	});
	$(".lock", soneElement).submit(function() {
		var lockElement = this;
		ajaxGet("lockSone.ajax", { "sone" : getSoneId(this), "formPassword" : getFormPassword() }, function() {
			$(lockElement).addClass("hidden");
			$(lockElement).parent().find(".unlock").removeClass("hidden");
		});
		return false;
	});
	$(".unlock", soneElement).submit(function() {
		var unlockElement = this;
		ajaxGet("unlockSone.ajax", { "sone" : getSoneId(this), "formPassword" : getFormPassword() }, function() {
			$(unlockElement).addClass("hidden");
			$(unlockElement).parent().find(".lock").removeClass("hidden");
		});
		return false;
	});

	/* mark Sone as known when clicking it. */
	$(soneElement).click(function() {
		markSoneAsKnown(this);
	});
}

/**
 * Ajaxifies the given post by enhancing all eligible elements with AJAX.
 *
 * @param postElement
 *            The post element to ajaxify
 */
function ajaxifyPost(postElement) {
	$(postElement).find("form").submit(function() {
		return false;
	});
	$(postElement).find(".create-reply button:submit").click(function() {
		button = $(this);
		button.attr("disabled", "disabled");
		sender = $(this.form).find(":input[name=sender]").val();
		inputField = $(this.form).find(":input[name=text]:enabled").get(0);
		postId = getPostId(this);
		text = $(inputField).val();
		(function(sender, postId, text, inputField) {
			postReply(sender, postId, text, function(success, error, replyId, soneId) {
				if (success) {
					$(inputField).val("");
					loadNewReply(replyId, soneId, postId);
					$("#sone .post#" + postId + " .create-reply").addClass("hidden");
					$("#sone .post#" + postId + " .create-reply .sender").hide();
					$("#sone .post#" + postId + " .create-reply .select-sender").show();
					$("#sone .post#" + postId + " .create-reply :input[name=sender]").val(getCurrentSoneId());
				} else {
					alert(error);
				}
				button.removeAttr("disabled");
			});
		})(sender, postId, text, inputField);
		return false;
	});

	/* replace all “delete” buttons with javascript. */
	(function(postElement) {
		getTranslation("WebInterface.Confirmation.DeletePostButton", function(deletePostText) {
			postId = getPostId(postElement);
			enhanceDeletePostButton($(postElement).find(".delete-post button"), postId, deletePostText);
		});
	})(postElement);

	/* convert all “like” buttons to javascript functions. */
	$(postElement).find(".like-post").submit(function() {
		likePost(getPostId(this));
		return false;
	});
	$(postElement).find(".unlike-post").submit(function() {
		unlikePost(getPostId(this));
		return false;
	});

	/* convert trust control buttons to javascript functions. */
	$(postElement).find(".post-trust").submit(function() {
		trustSone(getPostAuthor(this));
		return false;
	});
	$(postElement).find(".post-distrust").submit(function() {
		distrustSone(getPostAuthor(this));
		return false;
	});
	$(postElement).find(".post-untrust").submit(function() {
		untrustSone(getPostAuthor(this));
		return false;
	});

	/* convert bookmark/unbookmark buttons to javascript functions. */
	$(postElement).find(".bookmark").submit(function() {
		bookmarkPost(getPostId(this));
		return false;
	});
	$(postElement).find(".unbookmark").submit(function() {
		unbookmarkPost(getPostId(this));
		return false;
	});

	/* convert “show source” link into javascript function. */
	$(postElement).find(".show-source").each(function() {
		$("a", this).click(function() {
			$(".post-text.text", getPostElement(this)).toggleClass("hidden");
			$(".post-text.raw-text", getPostElement(this)).toggleClass("hidden");
			return false;
		});
	});

	/* add “comment” link. */
	addCommentLink(getPostId(postElement), getPostAuthor(postElement), postElement, $(postElement).find(".post-status-line .permalink-author"));

	/* process all replies. */
	replyIds = [];
	$(postElement).find(".reply").each(function() {
		replyIds.push(getReplyId(this));
		ajaxifyReply(this);
	});
	updateReplyTimes(replyIds.join(","));

	/* process reply input fields. */
	getTranslation("WebInterface.DefaultText.Reply", function(text) {
		$(postElement).find("input.reply-input").each(function() {
			registerInputTextareaSwap(this, text, "text", false, false);
		});
	});

	/* process sender selection. */
	$(".select-sender", postElement).css("display", "inline");
	$(".sender", postElement).hide();
	$(".select-sender button", postElement).click(function() {
		$(".sender", postElement).show();
		$(".select-sender", postElement).hide();
		return false;
	});

	/* mark everything as known on click. */
	$(postElement).click(function(event) {
		if ($(event.target).hasClass("click-to-show")) {
			return false;
		}
		markPostAsKnown(this);
	});

	/* hide reply input field. */
	$(postElement).find(".create-reply").addClass("hidden");
}

/**
 * Ajaxifies the given reply element.
 *
 * @param replyElement
 *            The reply element to ajaxify
 */
function ajaxifyReply(replyElement) {
	$(replyElement).find(".like-reply").submit(function() {
		likeReply(getReplyId(this));
		return false;
	});
	$(replyElement).find(".unlike-reply").submit(function() {
		unlikeReply(getReplyId(this));
		return false;
	});
	(function(replyElement) {
		getTranslation("WebInterface.Confirmation.DeleteReplyButton", function(deleteReplyText) {
			$(replyElement).find(".delete-reply button").each(function() {
				enhanceDeleteReplyButton(this, getReplyId(replyElement), deleteReplyText);
			});
		});
	})(replyElement);
	addCommentLink(getPostId(replyElement), getReplyAuthor(replyElement), replyElement, $(replyElement).find(".reply-status-line .permalink-author"));

	/* convert “show source” link into javascript function. */
	$(replyElement).find(".show-reply-source").each(function() {
		$("a", this).click(function() {
			$(".reply-text.text", getReplyElement(this)).toggleClass("hidden");
			$(".reply-text.raw-text", getReplyElement(this)).toggleClass("hidden");
			return false;
		});
	});

	/* convert trust control buttons to javascript functions. */
	$(replyElement).find(".reply-trust").submit(function() {
		trustSone(getReplyAuthor(this));
		return false;
	});
	$(replyElement).find(".reply-distrust").submit(function() {
		distrustSone(getReplyAuthor(this));
		return false;
	});
	$(replyElement).find(".reply-untrust").submit(function() {
		untrustSone(getReplyAuthor(this));
		return false;
	});
}

/**
 * Ajaxifies the given notification by replacing the form with AJAX.
 *
 * @param notification
 *            jQuery object representing the notification.
 */
function ajaxifyNotification(notification) {
	notification.find("form").submit(function() {
		return false;
	});
	notification.find("input[name=returnPage]").val($.url.attr("relative"));
	if (notification.find(".short-text").length > 0) {
		notification.find(".short-text").removeClass("hidden");
		notification.find(".text").addClass("hidden");
	}
	notification.find("form.mark-as-read button").click(function() {
		ajaxGet("markAsKnown.ajax", {"formPassword": getFormPassword(), "type": $(":input[name=type]", this.form).val(), "id": $(":input[name=id]", this.form).val()});
	});
	notification.find("a[class^='link-']").each(function() {
		linkElement = $(this);
		if (linkElement.is("[href^='viewPost']")) {
			id = linkElement.attr("class").substr(5);
			if (hasPost(id)) {
				linkElement.attr("href", "#post-" + id).addClass("in-page-link");
			}
		}
	});
	notification.find("form.dismiss button").click(function() {
		ajaxGet("dismissNotification.ajax", { "formPassword" : getFormPassword(), "notification" : notification.attr("id") }, function(data, textStatus) {
			/* dismiss in case of error, too. */
			notification.slideUp();
		}, function(xmlHttpRequest, textStatus, error) {
			/* ignore error. */
		});
	});
	return notification;
}

/**
 * Retrieves element IDs from notification elements.
 *
 * @param notification
 *            The notification element
 * @param selector
 *            The selector of the element containing the ID as text
 * @returns All extracted IDs
 */
function getElementIds(notification, selector) {
	elementIds = [];
	$(selector, notification).each(function() {
		elementIds.push($(this).text());
	});
	return elementIds;
}

/**
 * Compares the given notification elements and calls {@link #markSoneAsKnown()}
 * for every ID that is contained in the old notification but not in the new.
 *
 * @param oldNotification
 *            The old notification element
 * @param newNotification
 *            The new notification element
 */
function checkForRemovedSones(oldNotification, newNotification) {
	if (getNotificationId(oldNotification) != "new-sone-notification") {
		return;
	}
	oldIds = getElementIds(oldNotification, ".new-sone-id");
	newIds = getElementIds(newNotification, ".new-sone-id");
	$.each(oldIds, function(index, value) {
		if ($.inArray(value, newIds) == -1) {
			markSoneAsKnown(getSone(value), true);
		}
	});
}

/**
 * Compares the given notification elements and calls {@link #markPostAsKnown()}
 * for every ID that is contained in the old notification but not in the new.
 *
 * @param oldNotification
 *            The old notification element
 * @param newNotification
 *            The new notification element
 */
function checkForRemovedPosts(oldNotification, newNotification) {
	if (getNotificationId(oldNotification) != "new-post-notification") {
		return;
	}
	oldIds = getElementIds(oldNotification, ".post-id");
	newIds = getElementIds(newNotification, ".post-id");
	$.each(oldIds, function(index, value) {
		if ($.inArray(value, newIds) == -1) {
			markPostAsKnown(getPost(value), true);
		}
	});
}

/**
 * Compares the given notification elements and calls
 * {@link #markReplyAsKnown()} for every ID that is contained in the old
 * notification but not in the new.
 *
 * @param oldNotification
 *            The old notification element
 * @param newNotification
 *            The new notification element
 */
function checkForRemovedReplies(oldNotification, newNotification) {
	if (getNotificationId(oldNotification) != "new-replies-notification") {
		return;
	}
	oldIds = getElementIds(oldNotification, ".reply-id");
	newIds = getElementIds(newNotification, ".reply-id");
	$.each(oldIds, function(index, value) {
		if ($.inArray(value, newIds) == -1) {
			markReplyAsKnown(getReply(value), true);
		}
	});
}

function getStatus() {
	ajaxGet("getStatus.ajax", isViewSonePage() ? {"soneIds": getShownSoneId() } : {"loadAllSones": isKnownSonesPage()}, function(data, textStatus) {
		if ((data != null) && data.success) {
			/* process Sone information. */
			$.each(data.sones, function(index, value) {
				updateSoneStatus(value.id, value.name, value.status, value.modified, value.locked, value.lastUpdatedUnknown ? null : value.lastUpdated, value.lastUpdatedText);
			});
			notLoggedIn = !data.loggedIn;
			if (!notLoggedIn) {
				showOfflineMarker(!online);
			}
			/* search for removed notifications. */
			$("#sone #notification-area .notification").each(function() {
				notificationId = $(this).attr("id");
				foundNotification = false;
				$.each(data.notifications, function(index, value) {
					if (value.id == notificationId) {
						foundNotification = true;
						return false;
					}
				});
				if (!foundNotification) {
					if (notificationId == "new-sone-notification") {
						$(".new-sone-id", this).each(function(index, element) {
							soneId = $(this).text();
							markSoneAsKnown(getSone(soneId), true);
						});
					} else if (notificationId == "new-post-notification") {
						$(".post-id", this).each(function(index, element) {
							postId = $(this).text();
							markPostAsKnown(getPost(postId), true);
						});
					} else if (notificationId == "new-replies-notification") {
						$(".reply-id", this).each(function(index, element) {
							replyId = $(this).text();
							markReplyAsKnown(getReply(replyId), true);
						});
					}
					$(this).slideUp("normal", function() {
						$(this).remove();
						/* remove activity when no notifications are visible. */
						if ($("#sone #notification-area .notification").length == 0) {
							resetActivity();
						}
					});
				}
			});
			/* process notifications. */
			notificationIds = [];
			$.each(data.notifications, function(index, value) {
				oldNotification = getNotification(value.id);
				if ((oldNotification.length == 0) || (value.lastUpdatedTime > getNotificationLastUpdatedTime(oldNotification))) {
					notificationIds.push(value.id);
				}
			});
			if (notificationIds.length > 0) {
				loadNotifications(notificationIds);
			}
			/* process new posts. */
			$.each(data.newPosts, function(index, value) {
				loadNewPost(value.id, value.sone, value.recipient, value.time);
			});
			/* process new replies. */
			$.each(data.newReplies, function(index, value) {
				loadNewReply(value.id, value.sone, value.post, value.postSone);
			});
			/* do it again in 5 seconds. */
			setTimeout(getStatus, 5000);
		} else {
			/* data.success was false, wait 30 seconds. */
			setTimeout(getStatus, 30000);
		}
	}, function() {
		statusRequestQueued = false;
		ajaxError();
	});
}

/**
 * Requests multiple notifications from Sone and displays them.
 *
 * @param notificationIds
 *            Array of IDs of the notifications to load
 */
function loadNotifications(notificationIds) {
	ajaxGet("getNotification.ajax", {"notifications": notificationIds.join(",")}, function(data, textStatus) {
		if (!data || !data.success) {
			// TODO - show error
			return;
		}
		$.each(data.notifications, function(index, value) {
			oldNotification = getNotification(value.id);
			notification = ajaxifyNotification(createNotification(value.id, value.lastUpdatedTime, value.text, value.dismissable)).hide();
			if (oldNotification.length != 0) {
				if ((oldNotification.find(".short-text").length > 0) && (notification.find(".short-text").length > 0)) {
					opened = oldNotification.is(":visible") && oldNotification.find(".short-text").hasClass("hidden");
					notification.find(".short-text").toggleClass("hidden", opened);
					notification.find(".text").toggleClass("hidden", !opened);
				}
				checkForRemovedSones(oldNotification, notification);
				checkForRemovedPosts(oldNotification, notification);
				checkForRemovedReplies(oldNotification, notification);
				oldNotification.replaceWith(notification.show());
			} else {
				$("#sone #notification-area").append(notification);
				notification.slideDown();
				setActivity();
			}
		});
	});
}

/**
 * Returns the ID of the currently logged in Sone.
 *
 * @return The ID of the current Sone, or an empty string if no Sone is logged
 *         in
 */
function getCurrentSoneId() {
	return $("#currentSoneId").text();
}

/**
 * Returns the content of the page-id attribute.
 *
 * @returns The page ID
 */
function getPageId() {
	return $("#sone .page-id").text();
}

/**
 * Returns whether the current page is the index page.
 *
 * @returns {Boolean} <code>true</code> if the current page is the index page,
 *          <code>false</code> otherwise
 */
function isIndexPage() {
	return getPageId() == "index";
}

/**
 * Returns the current page of the selected pagination. If no pagination can be
 * found with the given selector, {@code 1} is returned.
 *
 * @param paginationSelector
 *            The pagination selector
 * @returns The current page of the pagination
 */
function getPage(paginationSelector) {
	pagination = $(paginationSelector);
	if (pagination.length > 0) {
		return $(".current-page", paginationSelector).text();
	}
	return 1;
}

/**
 * Returns whether the current page is a “view Sone” page.
 *
 * @returns {Boolean} <code>true</code> if the current page is a “view Sone”
 *          page, <code>false</code> otherwise
 */
function isViewSonePage() {
	return getPageId() == "view-sone";
}

/**
 * Returns the ID of the currently shown Sone. This will only return a sensible
 * value if isViewSonePage() returns <code>true</code>.
 *
 * @returns The ID of the currently shown Sone
 */
function getShownSoneId() {
	return $("#sone .sone-id").text();
}

/**
 * Returns whether the current page is a “view post” page.
 *
 * @returns {Boolean} <code>true</code> if the current page is a “view post”
 *          page, <code>false</code> otherwise
 */
function isViewPostPage() {
	return getPageId() == "view-post";
}

/**
 * Returns the ID of the currently shown post. This will only return a sensible
 * value if isViewPostPage() returns <code>true</code>.
 *
 * @returns The ID of the currently shown post
 */
function getShownPostId() {
	return $("#sone .post-id").text();
}

/**
 * Returns whether the current page is the “known Sones” page.
 *
 * @returns {Boolean} <code>true</code> if the current page is the “known
 *          Sones” page, <code>false</code> otherwise
 */
function isKnownSonesPage() {
	return getPageId() == "known-sones";
}

/**
 * Returns whether a post with the given ID exists on the current page.
 *
 * @param postId
 *            The post ID to check for
 * @returns {Boolean} <code>true</code> if a post with the given ID already
 *          exists on the page, <code>false</code> otherwise
 */
function hasPost(postId) {
	return $(".post#" + postId).length > 0;
}

/**
 * Returns whether a reply with the given ID exists on the current page.
 *
 * @param replyId
 *            The reply ID to check for
 * @returns {Boolean} <code>true</code> if a reply with the given ID already
 *          exists on the page, <code>false</code> otherwise
 */
function hasReply(replyId) {
	return $("#sone .reply#" + replyId).length > 0;
}

function loadNewPost(postId, soneId, recipientId, time) {
	if (hasPost(postId)) {
		return;
	}
	if (!isIndexPage() || (getPage(".pagination-index") > 1)) {
		if (!isViewPostPage() || (getShownPostId() != postId)) {
			if (!isViewSonePage() || ((getShownSoneId() != soneId) && (getShownSoneId() != recipientId))) {
				return;
			}
		}
	}
	if (getPostTime($("#sone .post").last()) > time) {
		return;
	}
	ajaxGet("getPost.ajax", { "post" : postId }, function(data, textStatus) {
		if ((data != null) && data.success) {
			if (hasPost(data.post.id)) {
				return;
			}
			if ((!isIndexPage() || (getPage(".pagination-index") > 1)) && !(isViewSonePage() && ((getShownSoneId() == data.post.sone) || (getShownSoneId() == data.post.recipient)))) {
				return;
			}
			var firstOlderPost = null;
			$("#sone .post").each(function() {
				if (getPostTime(this) < data.post.time) {
					firstOlderPost = $(this);
					return false;
				}
			});
			newPost = $(data.post.html).addClass("hidden");
			if (firstOlderPost != null) {
				newPost.insertBefore(firstOlderPost);
			}
			ajaxifyPost(newPost);
			updatePostTimes(data.post.id);
			newPost.slideDown();
			setActivity();
		}
	});
}

function loadNewReply(replyId, soneId, postId, postSoneId) {
	if (hasReply(replyId)) {
		return;
	}
	if (!hasPost(postId)) {
		return;
	}
	ajaxGet("getReply.ajax", { "reply": replyId }, function(data, textStatus) {
		/* find post. */
		if ((data != null) && data.success) {
			if (hasReply(data.reply.id)) {
				return;
			}
			$("#sone .post#" + data.reply.postId).each(function() {
				var firstNewerReply = null;
				$(this).find(".replies .reply").each(function() {
					if (getReplyTime(this) > data.reply.time) {
						firstNewerReply = $(this);
						return false;
					}
				});
				newReply = $(data.reply.html).addClass("hidden");
				if (firstNewerReply != null) {
					newReply.insertBefore(firstNewerReply);
				} else {
					if ($(this).find(".replies .create-reply")) {
						$(this).find(".replies .create-reply").before(newReply);
					} else {
						$(this).find(".replies").append(newReply);
					}
				}
				ajaxifyReply(newReply);
				updateReplyTimes(data.reply.id);
				newReply.slideDown();
				setActivity();
				return false;
			});
		}
	});
}

/**
 * Marks the given Sone as known if it is still new.
 *
 * @param soneElement
 *            The Sone to mark as known
 * @param skipRequest
 *            true to skip the JSON request, false or omit to perform the JSON
 *            request
 */
function markSoneAsKnown(soneElement, skipRequest) {
	if ($(soneElement).is(".new")) {
		$(soneElement).removeClass("new");
		if ((typeof skipRequest == "undefined") || !skipRequest) {
			ajaxGet("markAsKnown.ajax", {"formPassword": getFormPassword(), "type": "sone", "id": getSoneId(soneElement)});
		}
	}
}

function markPostAsKnown(postElements, skipRequest) {
	$(postElements).each(function() {
		postElement = this;
		if ($(postElement).hasClass("new")) {
			(function(postElement) {
				$(postElement).removeClass("new");
				if ((typeof skipRequest == "undefined") || !skipRequest) {
					ajaxGet("markAsKnown.ajax", {"formPassword": getFormPassword(), "type": "post", "id": getPostId(postElement)});
				}
			})(postElement);
		}
		$(".click-to-show", postElement).removeClass("new");
	});
	markReplyAsKnown($(postElements).find(".reply"));
}

function markReplyAsKnown(replyElements, skipRequest) {
	$(replyElements).each(function() {
		replyElement = this;
		if ($(replyElement).hasClass("new")) {
			(function(replyElement) {
				$(replyElement).removeClass("new");
				if ((typeof skipRequest == "undefined") || !skipRequest) {
					ajaxGet("markAsKnown.ajax", {"formPassword": getFormPassword(), "type": "reply", "id": getReplyId(replyElement)});
				}
			})(replyElement);
		}
	});
}

/**
 * Updates the time of the post with the given ID.
 *
 * @param postId
 *            The ID of the post to update
 * @param timeText
 *            The text of the time to show
 * @param refreshTime
 *            The refresh time after which to request a new time (in seconds)
 * @param tooltip
 *            The tooltip to show
 */
function updatePostTime(postId, timeText, refreshTime, tooltip) {
	if (!getPost(postId).is(":visible")) {
		return;
	}
	getPost(postId).find(".post-status-line > .time a").html(timeText).attr("title", tooltip);
	(function(postId, refreshTime) {
		setTimeout(function() {
			updatePostTimes(postId);
		}, refreshTime * 1000);
	})(postId, refreshTime);
}

/**
 * Requests new rendered times for the posts with the given IDs.
 *
 * @param postIds
 *            Comma-separated post IDs
 */
function updatePostTimes(postIds) {
	ajaxGet("getTimes.ajax", { "posts" : postIds }, function(data, textStatus) {
		if ((data != null) && data.success) {
			$.each(data.postTimes, function(index, value) {
				updatePostTime(index, value.timeText, value.refreshTime, value.tooltip);
			});
		}
	});
}

/**
 * Updates the time of the reply with the given ID.
 *
 * @param postId
 *            The ID of the reply to update
 * @param timeText
 *            The text of the time to show
 * @param refreshTime
 *            The refresh time after which to request a new time (in seconds)
 * @param tooltip
 *            The tooltip to show
 */
function updateReplyTime(replyId, timeText, refreshTime, tooltip) {
	getReply(replyId).find(".reply-status-line > .time").html(timeText).attr("title", tooltip);
	(function(replyId, refreshTime) {
		setTimeout(function() {
			updateReplyTimes(replyId);
		}, refreshTime * 1000);
	})(replyId, refreshTime);
}

/**
 * Requests new rendered times for the posts with the given IDs.
 *
 * @param postIds
 *            Comma-separated post IDs
 */
function updateReplyTimes(replyIds) {
	ajaxGet("getTimes.ajax", { "replies" : replyIds }, function(data, textStatus) {
		if ((data != null) && data.success) {
			$.each(data.replyTimes, function(index, value) {
				updateReplyTime(index, value.timeText, value.refreshTime, value.tooltip);
			});
		}
	});
}

function resetActivity() {
	title = document.title;
	if (title.indexOf('(') == 0) {
		setTitle(title.substr(title.indexOf(' ') + 1));
	}
	iconBlinking = false;
}

function setActivity() {
	if (!focus) {
		title = document.title;
		if (title.indexOf('(') != 0) {
			setTitle("(!) " + title);
		}
		if (!iconBlinking) {
			setTimeout(toggleIcon, 1500);
			iconBlinking = true;
		}
	}
}

/**
 * Sets the window title after a small delay to prevent race-condition issues.
 *
 * @param title
 *            The title to set
 */
function setTitle(title) {
	setTimeout(function() {
		document.title = title;
	}, 50);
}

/** Whether the icon is currently showing activity. */
var iconActive = false;

/** Whether the icon is currently supposed to blink. */
var iconBlinking = false;

/**
 * Toggles the icon. If the window has gained focus and the icon is still
 * showing the activity state, it is returned to normal.
 */
function toggleIcon() {
	if (focus || !iconBlinking) {
		if (iconActive) {
			changeIcon("images/icon.png");
			iconActive = false;
		}
		iconBlinking = false;
	} else {
		iconActive = !iconActive;
		changeIcon(iconActive ? "images/icon-activity.png" : "images/icon.png");
		setTimeout(toggleIcon, 1500);
	}
}

/**
 * Changes the icon of the page.
 *
 * @param iconUrl
 *            The new URL of the icon
 */
function changeIcon(iconUrl) {
	$("link[rel=icon]").remove();
	$("head").append($("<link>").attr("rel", "icon").attr("type", "image/png").attr("href", iconUrl));
	$("iframe[id=icon-update]")[0].src += "";
}

/**
 * Creates a new notification.
 *
 * @param id
 *            The ID of the notificaiton
 * @param text
 *            The text of the notification
 * @param dismissable
 *            <code>true</code> if the notification can be dismissed by the
 *            user
 */
function createNotification(id, lastUpdatedTime, text, dismissable) {
	notification = $("<div></div>").addClass("notification").attr("id", id).attr("lastUpdatedTime", lastUpdatedTime);
	if (dismissable) {
		dismissForm = $("#sone #notification-area #notification-dismiss-template").clone().removeClass("hidden").removeAttr("id");
		dismissForm.find("input[name=notification]").val(id);
		notification.append(dismissForm);
	}
	notification.append(text);
	return notification;
}

/**
 * Shows the details of the notification with the given ID.
 *
 * @param notificationId
 *            The ID of the notification
 */
function showNotificationDetails(notificationId) {
	$("#sone .notification#" + notificationId + " .text").removeClass("hidden");
	$("#sone .notification#" + notificationId + " .short-text").addClass("hidden");
}

/**
 * Deletes the field with the given ID from the profile.
 *
 * @param fieldId
 *            The ID of the field to delete
 */
function deleteProfileField(fieldId) {
	ajaxGet("deleteProfileField.ajax", {"formPassword": getFormPassword(), "field": fieldId}, function(data, textStatus) {
		if (data && data.success) {
			$("#sone .profile-field#" + data.field.id).slideUp();
		}
	});
}

/**
 * Renames a profile field.
 *
 * @param fieldId
 *            The ID of the field to rename
 * @param newName
 *            The new name of the field
 * @param successFunction
 *            Called when the renaming was successful
 */
function editProfileField(fieldId, newName, successFunction) {
	ajaxGet("editProfileField.ajax", {"formPassword": getFormPassword(), "field": fieldId, "name": newName}, function(data, textStatus) {
		if (data && data.success) {
			successFunction();
		}
	});
}

/**
 * Moves the profile field with the given ID one slot in the given direction.
 *
 * @param fieldId
 *            The ID of the field to move
 * @param direction
 *            The direction to move in (“up” or “down”)
 * @param successFunction
 *            Function to call on success
 */
function moveProfileField(fieldId, direction, successFunction) {
	ajaxGet("moveProfileField.ajax", {"formPassword": getFormPassword(), "field": fieldId, "direction": direction}, function(data, textStatus) {
		if (data && data.success) {
			successFunction();
		}
	});
}

/**
 * Moves the profile field with the given ID up one slot.
 *
 * @param fieldId
 *            The ID of the field to move
 * @param successFunction
 *            Function to call on success
 */
function moveProfileFieldUp(fieldId, successFunction) {
	moveProfileField(fieldId, "up", successFunction);
}

/**
 * Moves the profile field with the given ID down one slot.
 *
 * @param fieldId
 *            The ID of the field to move
 * @param successFunction
 *            Function to call on success
 */
function moveProfileFieldDown(fieldId, successFunction) {
	moveProfileField(fieldId, "down", successFunction);
}

var statusRequestQueued = true;

/**
 * Sets the status of the web interface as offline.
 */
function ajaxError() {
	online = false;
	showOfflineMarker(true);
	if (!statusRequestQueued) {
		setTimeout(getStatus, 5000);
		statusRequestQueued = true;
	}
}

/**
 * Sets the status of the web interface as online.
 */
function ajaxSuccess() {
	online = true;
	showOfflineMarker(!online || (initiallyLoggedIn && notLoggedIn));
}

/**
 * Shows or hides the offline marker.
 *
 * @param visible
 *            {@code true} to display the offline marker, {@code false} to hide
 *            it
 */
function showOfflineMarker(visible) {
	/* jQuery documentation says toggle() works the other way around?! */
	$("#sone #offline-marker").toggle(visible);
	if (visible) {
		$("#sone #main").addClass("offline");
	} else {
		$("#sone #main").removeClass("offline");
	}
}

//
// EVERYTHING BELOW HERE IS EXECUTED AFTER LOADING THE PAGE
//

var focus = true;
var online = true;
var initiallyLoggedIn = $("#sone #loggedIn").text() == "true";
var notLoggedIn = !initiallyLoggedIn;

$(document).ready(function() {

	/* this initializes the status update input field. */
	getTranslation("WebInterface.DefaultText.StatusUpdate", function(defaultText) {
		registerInputTextareaSwap("#sone #update-status .status-input", defaultText, "text", false, false);
		$("#sone #update-status .select-sender").css("display", "inline");
		$("#sone #update-status .sender").hide();
		$("#sone #update-status .select-sender button").click(function() {
			$("#sone #update-status .sender").show();
			$("#sone #update-status .select-sender").hide();
			return false;
		});
		$("#sone #update-status").submit(function() {
			button = $("button:submit", this);
			button.attr("disabled", "disabled");
			if ($(this).find(":input.default:enabled").length > 0) {
				return false;
			}
			sender = $(this).find(":input[name=sender]").val();
			text = $(this).find(":input[name=text]:enabled").val();
			ajaxGet("createPost.ajax", { "formPassword": getFormPassword(), "sender": sender, "text": text }, function(data, textStatus) {
				button.removeAttr("disabled");
			});
			$(this).find(":input[name=sender]").val(getCurrentSoneId());
			$(this).find(":input[name=text]:enabled").val("").blur();
			$(this).find(".sender").hide();
			$(this).find(".select-sender").show();
			return false;
		});
	});

	/* ajaxify the search input field. */
	getTranslation("WebInterface.DefaultText.Search", function(defaultText) {
		registerInputTextareaSwap("#sone #search input[name=query]", defaultText, "query", false, true);
	});

	/* ajaxify input field on “view Sone” page. */
	getTranslation("WebInterface.DefaultText.Message", function(defaultText) {
		registerInputTextareaSwap("#sone #post-message input[name=text]", defaultText, "text", false, false);
		$("#sone #post-message .select-sender").css("display", "inline");
		$("#sone #post-message .sender").hide();
		$("#sone #post-message .select-sender button").click(function() {
			$("#sone #post-message .sender").show();
			$("#sone #post-message .select-sender").hide();
			return false;
		});
		$("#sone #post-message").submit(function() {
			sender = $(this).find(":input[name=sender]").val();
			text = $(this).find(":input[name=text]:enabled").val();
			ajaxGet("createPost.ajax", { "formPassword": getFormPassword(), "recipient": getShownSoneId(), "sender": sender, "text": text });
			$(this).find(":input[name=sender]").val(getCurrentSoneId());
			$(this).find(":input[name=text]:enabled").val("").blur();
			$(this).find(".sender").hide();
			$(this).find(".select-sender").show();
			return false;
		});
	});

	/* Ajaxifies all posts. */
	/* calling getTranslation here will cache the necessary values. */
	getTranslation("WebInterface.Confirmation.DeletePostButton", function(text) {
		getTranslation("WebInterface.Confirmation.DeleteReplyButton", function(text) {
			getTranslation("WebInterface.DefaultText.Reply", function(text) {
				$("#sone .post").each(function() {
					ajaxifyPost(this);
				});
			});
		});
	});

	/* update post times. */
	postIds = [];
	$("#sone .post").each(function() {
		postIds.push(getPostId(this));
	});
	updatePostTimes(postIds.join(","));

	/* hides all replies but the latest two. */
	if (!isViewPostPage()) {
		getTranslation("WebInterface.ClickToShow.Replies", function(text) {
			$("#sone .post .replies").each(function() {
				allReplies = $(this).find(".reply");
				if (allReplies.length > 2) {
					newHidden = false;
					for (replyIndex = 0; !newHidden && (replyIndex < (allReplies.length - 2)); ++replyIndex) {
						$(allReplies[replyIndex]).addClass("hidden");
						newHidden |= $(allReplies[replyIndex]).hasClass("new");
					}
					clickToShowElement = $("<div></div>").addClass("click-to-show");
					if (newHidden) {
						clickToShowElement.addClass("new");
					}
					(function(clickToShowElement, allReplies, text) {
						clickToShowElement.text(text);
						clickToShowElement.click(function() {
							allReplies.removeClass("hidden");
							clickToShowElement.addClass("hidden");
						});
					})(clickToShowElement, allReplies, text);
					$(allReplies[0]).before(clickToShowElement);
				}
			});
		});
	}

	$("#sone .sone").each(function() {
		ajaxifySone($(this));
	});

	/* process all existing notifications, ajaxify dismiss buttons. */
	$("#sone #notification-area .notification").each(function() {
		ajaxifyNotification($(this));
	});

	/* disable all permalinks. */
	$(".permalink").click(function() {
		return false;
	});

	/* activate status polling. */
	setTimeout(getStatus, 5000);

	/* reset activity counter when the page has focus. */
	$(window).focus(function() {
		focus = true;
		resetActivity();
	}).blur(function() {
		focus = false;
	});

});
