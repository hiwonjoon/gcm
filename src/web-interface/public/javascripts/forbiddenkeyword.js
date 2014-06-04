$(function() {

    $('button.delete').click(function()
    {
        var answer = confirm("삭제하시겠습니까?");
        if(answer)
        {
            $.get("forbidden/del", {keyword : $(this).closest(".keyword_li").children("div.keyword_div").text() }, function(data) {
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