$(function() {
    $(document).ready(function() {
        $('table#logTable').dataTable(
        {
            "processing" : true,
            "serverSide" : true,
            "searching" : true,
            "scrollY" : "400px",
            "scrollCollapse" : true,
            "ordering" : false,
            "ajax" : "logView/getAjax?logType="+$('span#logType').html(),
            "columns" : [
                {"data" : "logType" },
                {"data" : "logSubType"},
                {"data" : "username"},
                {"data" : "contents"},
                {"data" : "date"}
            ]
        }
        );

        var select = $('#search_tfoot select')
                    .on('change',function() {
                        $('table#logTable').DataTable()
                        .column(1).search($(this).val(), true, false)
                        .draw();
                    });



    })
});