$(document).ready(function(){
    $('#userManager').on('click',function(){
        $("#userManagerTable").is(":hidden") ? $("#userManagerTable").slideDown() : document.getElementById('userManagerTable').style.display='none';

    })

    $('#email').on('click',function(){
        $("#userEmailTable").is(":hidden") ? $("#userEmailTable").slideDown() : document.getElementById('userEmailTable').style.display='none';
    })
})
