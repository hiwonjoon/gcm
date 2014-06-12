$(function() {
    $(document).ready(function() {
        $('table#logTable').dataTable(
        {
            "processing" : true,
            "serverSide" : true,
            "searching" : false,
            "scrollY" : "400px",
            "scrollCollapse" : true,
            "ordering" : false,
            "ajax" : "logView/getAjax?logType="+$('span#logType').html(),
            "columns" : [
                {"data" : "logType" },
                {"data" : "username"},
                {"data" : "contents"},
                {"data" : "date"}
            ]
        }
        );

    })
});