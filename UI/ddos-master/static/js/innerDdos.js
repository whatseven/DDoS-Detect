$(document).ready(function(){
    var COUNT =100000000;
    $('#wrongTable').on('click',function(evt){
        var v_id = $(evt.target).attr('id');
        $('.'+v_id).hasClass('active') ?
            $('.'+v_id).removeClass('active') : $('.'+v_id).addClass('active');
    });
    $('#blackTable').on('click',function(evt){
        var v_id = $(evt.target).attr('id');
        $('.'+v_id).hasClass('active') ?
            $('.'+v_id).removeClass('active') : $('.'+v_id).addClass('active');
    });

    $('#addBlack').on('click',function(){
        var wrongList = $('tr').css('active');
        var ipList =[];
        var str =[];
        //wrongList.remove();

        //$('tr.active').insertAfter($(".bt2"));
        //$("tr").remove(".active");
        $('tr.active').each(function(){
            if(!this)   return;
            ipList.push($(this).children().first().text());
            $(this).remove();
        });
        ipList.map(function(ip){
            var d =getDate();
            COUNT++;
            var t ="<tr class=\"bt"+COUNT+"\" ><td id=\"bt"+COUNT+"\">"+ip+"</td><td id=\"bt"+COUNT+"\">"+d+"</td></tr>";
            str.push(t)
        });
        str.map(function(s){
            //$(s).insertBefore($("#tblackTable").children().first());
            $("#tblackTable").append(s);
        });
    });

    $('#removeBlack').on('click',function(){
        var blackList = $('tr').css('active');
        var ipList =[];
        var str =[];
        $('tr.active').each(function(){
            if(!this)   return;
            ipList.push($(this).children().first().text());
            $(this).remove();
        });
        ipList.map(function(ip){
            COUNT++;
            var t ="<tr class=\"wt"+COUNT+"\" ><td id=\"wt"+COUNT+"\">"+ip+"</td><td id=\"wt"+COUNT+"\">"+'20 MB/s'+"</td><td id=\"wt"+COUNT+"\">"+'原因'+"</td></tr>";
            str.push(t)
        });
        str.map(function(s){
            //$(s).insertBefore($("#twrongTable").children().first());
            $("#twrongTable").append(s);
        });

    });

    var getDate = function(){
        var d = new Date();
	    var date = d.getFullYear()+"-"+(d.getMonth()+1)+"-"+d.getDate();
	    return date;
    }

})
