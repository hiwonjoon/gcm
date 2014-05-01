@(menu: String)(implicit r: RequestHeader)

$(function() {
    $("#menu_@menu").attr("class", "active");
})