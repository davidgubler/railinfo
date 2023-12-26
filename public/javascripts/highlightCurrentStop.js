$(document).ready(function () {
    var highlight = function() {
        var epochSecond = Date.now() / 1000;
        var marked = false;
        var rows = $('[data-passtime]');
        var prev = rows.first();
        rows.each(function() {
            var passTime = $(this).data('passtime');
            if (passTime > epochSecond && !marked) {
                prev.addClass("highlight");
                marked = true;
            }
            $(this).removeClass("highlight");
            prev = $(this);
        });
        if (!marked) {
            rows.last().css("color", "red");
        }
    };
    highlight();
    window.setInterval(highlight, 1000);
});