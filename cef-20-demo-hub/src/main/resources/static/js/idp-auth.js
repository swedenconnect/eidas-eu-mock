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

    //Nat person
    var selectedNat = $.cookie(natSelectCookie);
    if (selectedNat != undefined){
        $('#userSelect').val(selectedNat);
    }
    //Legal person
    var selectedLegal = $.cookie(legSelectCookie);
    if (selectedLegal != undefined){
        $('#orgSelect').val(selectedLegal);
    }
    var selectedLoa = $.cookie(loaSelectCookie);
    if (selectedLoa != undefined){
        $('#loaSelect').val(selectedLoa);
    }
    $('.selectpicker').selectpicker('refresh');

});



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

