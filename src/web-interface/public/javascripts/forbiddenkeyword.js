$(function() {
     $(document).ready(function() {
            $('table#ForbiddenKeywordTable').DataTable(
            {
                "processing" : true,
                "serverSide" : true,
                "searching" : true,
                "scrollY" : "400px",
                "scrollCollapse" : true,
                "ordering" : false,
                "ajax" : "forbidden/getAjax",
                "columns" : [
                    {"data" : "str" }

                ]

            }
            );
            $('table#ForbiddenKeywordTable tbody').on( 'click', 'tr', function () {
                if ( $(this).hasClass('selected') ) {
                    $(this).removeClass('selected');
                }
                else {
                    $('table#ForbiddenKeywordTable').DataTable().$('tr.selected').removeClass('selected');
                    $(this).addClass('selected');
                }
            } );



    $('button#delete').click(function()
    {
        var row = $('table#ForbiddenKeywordTable').DataTable().row('.selected')
        if(typeof row.data().str == "undefined")
        {
            alert("삭제할 단어를 선택해주세요")
            return;
        }

        var answer = confirm("'" + row.data().str +"' 을(를)삭제하시겠습니까?");
        if(answer)
        {
            $.get("forbidden/del", {keyword : row.data().str }, function(data) {
                if(data=="Success")
                {
                    alert("삭제하였습니다");
                    location.reload();
                }
                else
                {
                    alert(data);
                }
            })
        }

    })

    $('button#addKeywordButton').click(function()
    {
        $.get("forbidden/add", {keyword : $('input#addKeyword').val()}, function(data) {
            if(data=="Success")
            {
                alert("추가하였습니다");
                location.reload();
            }
            else
            {
                alert(data);
            }
        })
    })


        })


})