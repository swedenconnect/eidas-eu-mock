var natSelectCookie = "natpSelect-"+spCountry;
var legSelectCookie = "legalpSelect-"+spCountry;
var loaSelectCookie = "loaSelect-"+spCountry;

$(document).ready(function(){
    // Unfold function
    $('.drop-down > p').click(function(){
        $(this).parent('.drop-down').toggleClass('open');
    });

    // Not to be used in production
    $('#headertoggle').click(function(){
        $('.header').toggleClass('hide');
    });

    $('#elemtoggle').click(function(){
        $('.ns-providers').toggleClass('hide');
    });

    // Set preselected values
    const $els = $('.selectpicker');

    // Destroy per-element (your previous check only handled the first one)
    if ($els.data('selectpicker')) {
        $els.selectpicker('destroy');
    }

    setSelectpickerValue($('#userSelect'), $.cookie(natSelectCookie));
    setSelectpickerValue($('#orgSelect'),$.cookie(legSelectCookie));
    setSelectpickerValue($('#loaSelect'),$.cookie(loaSelectCookie));
    $els.selectpicker();

});

function setSelectpickerValue($sel, value) {
    if (!value) return;

    // hard-clear any existing selected options
    $sel.find('option').prop('selected', false);

    // set the one we want
    $sel.val(value);

    // update bootstrap-select UI (render updates the button text)
    $sel.selectpicker('render');
}



function saveSelectedUser(type){
    switch (type) {
        case "nat":
            var selectedNat =  $("#userSelect").val();
            $.cookie(natSelectCookie, selectedNat, {expires : 200} );
        case "leg":
            var selectedLegal =  $("#orgSelect").val();
            $.cookie(legSelectCookie, selectedLegal, {expires : 200} );
    }
}

function saveSelectedLoa(){
    var selectedLoa =  $("#loaSelect").val();
    $.cookie(loaSelectCookie, selectedLoa, {expires : 200} );
}

function submitForm(){
    $("#authnForm").submit();
}

function cancelAuthn(){
    $("#cancelOption").attr("value", "true");
    $('#authnForm').submit();
}

