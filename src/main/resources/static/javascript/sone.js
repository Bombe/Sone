/* Sone JavaScript functions. */

function registerInputTextareaSwap(inputSelector, defaultText, inputFieldName, optional) {
	$(inputSelector).each(function() {
		textarea = $("<textarea name=\"" + inputFieldName + "\"></textarea>").blur(function() {
			if ($(this).val() == "") {
				$(this).hide();
				$(this).data("inputField").show().removeAttr("disabled");
			}
		}).hide().data("inputField", $(this));
		$(this).after(textarea);
		(function(inputField, textarea) {
			$(inputField).focus(function() {
				$(this).hide().attr("disabled", "disabled");
				textarea.show().focus();
			}).addClass("default");
			(function(inputField) {
				$.getJSON("ajax/getTranslation.ajax", {"key": defaultText}, function(data, textStatus) {
					$(inputField).val(data.value);
				});
			})(inputField);
			$(inputField.form).submit(function() {
				if (!optional && (textarea.val() == "")) {
					return false;
				}
			});
		})(this, textarea);
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
		if (data.age < 600) {
			updateInterval = 5;
		} else if (data.age < 86400) {
			updateInterval = 30;
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
	$("#sone .sone#" + soneId).
		toggleClass("unknown", status == "unknown").
		toggleClass("idle", status == "idle").
		toggleClass("inserting", status == "inserting").
		toggleClass("downloading", status == "downloading").
		toggleClass("modified", modified);
	$("#sone .sone#" + soneId + " .last-update span.time").text(lastUpdated);
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
