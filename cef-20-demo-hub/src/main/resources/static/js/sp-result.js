$(document).ready(function () {
    $('#metadataViewDiv').hide();
    $('#requestViewDiv').hide();
    var windowHeight = window.innerHeight;
    windowHeight = parseInt((windowHeight - 115) * 94 / 100);
    var mdheigth = windowHeight > 100 ? windowHeight : 100;
    $('#responseDisplayDiv').css("height", mdheigth)
    $('#responseDisplayDiv').attr("overflow", "auto")
    $('#requestDisplayDiv').css("height", mdheigth)
    $('#requestDisplayDiv').attr("overflow", "auto")

    $('pre code').each(function (i, block) {
        hljs.highlightBlock(block);
    });
    $(".full-box-view").css("height", mdheigth);
});
