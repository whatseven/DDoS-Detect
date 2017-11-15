$(document).ready(function(){
    $('#whiteTable').on('click',function(evt){
        var v_id = $(evt.target).attr('id');
        $('.'+v_id).hasClass('active') ?
            $('.'+v_id).removeClass('active') : $('.'+v_id).addClass('active');
    });

    $('#removeWhite').on('click',function(){
        $('tr.active').each(function(){
            if(!this)   return;
            $(this).remove();
        });
    })
})
