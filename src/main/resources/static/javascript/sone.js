/* Sone JavaScript functions. */

function registerInputTextareaSwap(inputSelector, defaultText) {
	$(inputSelector).focus(function() {
		if ($(this).val() == defaultText) {
			$(this).val("").removeClass("default");
		}
	}).blur(function() {
		if ($(this).val() == "") {
			$(this).val(defaultText).addClass("default");
		}
	}).addClass("default").val(defaultText);
	$($(inputSelector).get(0).form).submit(function() {
		if ($(inputSelector).hasClass("default")) {
			$(inputSelector).val("");
		}
	});
}

/* hide all the “create reply” forms until a link is clicked. */
function addCommentLinks() {
	$("#sone .post").each(function() {
		postId = $(this).attr("id");
		commentElement = (function(postId) {
			var commentElement = $("<div>Comment</div>").addClass("show-reply-form").click(function() {
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
