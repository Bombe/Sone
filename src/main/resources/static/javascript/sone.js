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
