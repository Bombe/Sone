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
