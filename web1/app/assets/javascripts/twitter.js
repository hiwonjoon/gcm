$(function() {
    $(document).ready(function(){
        refreshDiv();
        setInterval(refreshDiv, 60000);
    });

  function refreshDiv(){
    $("#twitter_favorite_words").empty();
    jsRoutes.controllers.GetTwitterWords.getMessage().ajax({
        cache: false,
        success: function(data) {
            console.log(data)
            $("#twitter_favorite_words").jQCloud(data);
        }
    });
  }

})