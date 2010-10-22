/* Sone JavaScript functions. */

function registerInputTextareaSwap(inputSelector, defaultText, inputFieldName, optional) {
	$(inputSelector).each(function() {
		textarea = $("<textarea name=\"" + inputFieldName + "\"></textarea>").blur(function() {
			if ($(this).val() == "") {
				$(this).hide();
				inputField = $(this).data("inputField");
				inputField.show().removeAttr("disabled").addClass("default");
				(function(inputField) {
					getTranslation(defaultText, function(translation) {
						inputField.val(translation);
					});
				})(inputField);
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
				(function(inputField) {
					getTranslation(defaultText, function(translation) {
						inputField.val(translation);
					});
				})(inputField);
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
	$("#sone .post").each(function() {
		postId = $(this).attr("id");
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
		$(this).find(".create-reply").addClass("hidden");
		$(this).find(".status-line .time").each(function() {
			$(this).after(commentElement.clone(true));
		});
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
 */
function getSoneStatus(soneId) {
	$.getJSON("ajax/getSoneStatus.ajax", {"sone": soneId}, function(data, textStatus) {
		updateSoneStatus(soneId, data.status, data.modified, data.lastUpdated);
		/* seconds! */
		updateInterval = 60;
		if (data.modified || (data.status == "downloading") || (data.status == "inserting")) {
			updateInterval = 5;
		}
		setTimeout(function() {
			getSoneStatus(soneId);
		}, updateInterval * 1000);
	});
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
function updateSoneStatus(soneId, status, modified, lastUpdated) {
	$("#sone .sone." + soneId).
		toggleClass("unknown", status == "unknown").
		toggleClass("idle", status == "idle").
		toggleClass("inserting", status == "inserting").
		toggleClass("downloading", status == "downloading").
		toggleClass("modified", modified);
	$("#sone .sone." + soneId + " .last-update span.time").text(lastUpdated);
}

var watchedSones = {};

/**
 * Watches this Sone for updates to its status.
 *
 * @param soneId
 *            The ID of the Sone to watch
 */
function watchSone(soneId) {
	if (watchedSones[soneId]) {
		return;
	}
	watchedSones[soneId] = true;
	(function(soneId) {
		setTimeout(function() {
			getSoneStatus(soneId);
		}, 5000);
	})(soneId);
}

/**
 * Enhances a “delete” button so that the confirmation is done on the same page.
 *
 * @param buttonId
 *            The selector of the button
 * @param translationKey
 *            The translation key of the text to show on the button
 * @param deleteCallback
 *            The callback that actually deletes something
 */
function enhanceDeleteButton(buttonId, translationKey, deleteCallback) {
	button = $(buttonId);
	(function(button) {
		getTranslation(translationKey, function(translation) {
			newButton = $("<button></button>").addClass("confirm").hide().text(translation).click(function() {
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
		});
	})(button);
}

/**
 * Enhances a post’s “delete” button.
 *
 * @param buttonId
 *            The selector of the button
 * @param postId
 *            The ID of the post to delete
 */
function enhanceDeletePostButton(buttonId, postId) {
	enhanceDeleteButton(buttonId, "WebInterface.Confirmation.DeletePostButton", function() {
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
 */
function enhanceDeleteReplyButton(buttonId, replyId) {
	enhanceDeleteButton(buttonId, "WebInterface.Confirmation.DeleteReplyButton", function() {
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
