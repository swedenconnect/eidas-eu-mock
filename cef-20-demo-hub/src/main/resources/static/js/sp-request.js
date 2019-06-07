var countrySelectCookie = "selectedCountry";
$(document).ready(function () {
    getPreselected();
    storeCountry();
    $('.selectpicker').selectpicker('refresh');
});

function storeCountry() {
    $.cookie(countrySelectCookie, $("#citizenCountry").val(), {expires: 200});
}
function getPreselected() {
    var storedSelection = $.cookie(countrySelectCookie);
    if (storedSelection !== undefined) {
        $("#citizenCountry").val(storedSelection);
    }
}
function submitForm(){
    $("#requestForm").submit();
}
