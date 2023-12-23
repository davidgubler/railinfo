function color(factor) {
    var red, green, blue;
    red = 255;
    green = 25 + Math.round(230 * (1-factor));
    blue = 25 + Math.round(230 * (1-factor));
    redHex = ("00" + red.toString(16)).substr(-2);
    greenHex = ("00" + green.toString(16)).substr(-2);
    blueHex = ("00" + blue.toString(16)).substr(-2);
    return "#" + redHex + greenHex + blueHex;
}

$(document).ready(function () {
    var highlight = function() {
        var epochSecond = Date.now() / 1000;
        $('[data-passtime]').each(function() {
            var passTime = $(this).data('passtime');
            if(Math.abs(passTime - epochSecond) < 450) {
                factor = 1 - Math.abs(passTime - epochSecond) / 450;
                $(this).css("color", color(factor));
            } else {
                $(this).css("color", "");
            }
        });
    };
    highlight();
    window.setInterval(highlight, 10000);
});