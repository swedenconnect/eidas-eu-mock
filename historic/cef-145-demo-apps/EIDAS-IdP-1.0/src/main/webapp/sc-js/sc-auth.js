var userSelectCookie = "userSelect";
var loaSelectCookie = "loaSelect";

$(document).ready(function(){
    var selectedUser = $.cookie(userSelectCookie);
    if (selectedUser != undefined){
        $('#username').val(selectedUser);
        $('.selectpicker').selectpicker('refresh');
    }
    var selectedLoa = $.cookie(loaSelectCookie);
    if (selectedLoa != undefined){
        $('#eidasloa').val(selectedLoa);
        $('.selectpicker').selectpicker('refresh');
    }
});

function saveSelectedUser(){
   var selectedUser =  $("#username").val();
   $.cookie(userSelectCookie, selectedUser, {expires : 200} );
}

function saveSelectedLoa(){
   var selectedLoa =  $("#eidasloa").val();
   $.cookie(loaSelectCookie, selectedLoa, {expires : 200} );
}

function cancelAuthn(){
    $("#cancelOption").attr("value", "true");
    $('#authenticationForm').submit();
}
