$(document).ready(function () {
    $('#metadataViewDiv').hide();
    $('#assertionViewDiv').hide();
    var windowHeight = window.innerHeight;
    windowHeight = parseInt((windowHeight - 115) * 94 / 100);
    var mdheigth = windowHeight > 100 ? windowHeight : 100;
    $('#responseDisplayDiv').css("height", mdheigth)
    $('#responseDisplayDiv').attr("overflow", "auto")
    $('#assertionDisplayDiv').css("height", mdheigth)
    $('#assertionDisplayDiv').attr("overflow", "auto")

    $('pre code').each(function (i, block) {
        hljs.highlightBlock(block);
        $(this).css("height", mdheigth);
    });
});
