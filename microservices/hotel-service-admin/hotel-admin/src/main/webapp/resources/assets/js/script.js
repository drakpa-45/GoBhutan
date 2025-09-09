//region === Global Ajax Settings ===
$(document)
    .ajaxSend(function (e, xhr, options){
        var token = $('input[name="_csrf"]').val();
        xhr.setRequestHeader("X-CSRF-TOKEN",token);
        if (options.type.toUpperCase()==="POST"){
            $.blockUI();
        }
        NProgress.start();
    }).ajaxStart(function (){
        NProgress.set(0.4);
    }).ajaxStop(function (){
        $.unblockUI();
        NProgress.done();
    }).ajaxError(function (event, jgxhr, settings, thrownError){
        $.unblockUI();
        NProgress.done();
        if (event.redirect){
            window.location.href = event.redirect;
        }
        switch (jgxhr.status){
            case 901:
                window.location.href = dofpsGlobal.baseURL() + "loginMain";
                break;
            case 500:
                errorMsg("System encountered with problem(s). Please try again or if the problem persist, contact with Administrator.");
                break;
            case 404:
                errorMsg("Invalid Request - 404");
                break;
            case 400:
                errorMsg("Bad Request - 400");
                break;
            case 401:
                errorMsg("Unauthorized - 401");
                break;
            case 403:
                errorMsg("Forbidden - 403");
                break;
            case 502:
                errorMsg("Bad Gateway - 502");
                break;
            case 503:
                errorMsg("Service Unavailable - 503");
                break;
            /*case 406:
             window.location.href = ditGlobal.baseUrl() + "dayCloseError";
             break;
             case 407:
             window.location.href = ditGlobal.baseUrl() + "dayOpenError";
             break;
             case 408:
             window.location.href = ditGlobal.baseUrl() + "headOfficeError";
             break;
             case 409:
             window.location.href = ditGlobal.baseUrl() + "branchOfficeError";
             break;*/
        }
    }).ajaxComplete(function (){
        $.unblockUI();
        NProgress.done();
    }).ajaxSuccess(function (event, request, settings){
        $.unblockUI();
        NProgress.done();
        if (event.redirect){
            window.location.href = event.redirect;
        }
        var location = request.getResponseHeader('Location');
        if (location && location != window.location.href){
            window.location.href = location;
        }
    });
//endregion === Global Ajax Settings ===


//region === Start of When Document Is Ready ===
$(document).ready(function() {
    $("body").css('overflow', 'hidden');
    $("body").css('overflow', 'auto');

    //region === Menu Heighlighting ===
    var url  = window.location.pathname + window.location.search;
    var menuLink  = $('.sidebar-menu').find('.fi-menu').children('a');
    $.each(menuLink, function (){
        if ($(this).attr('href') === url) {
            $(this).parent('li').addClass('active');
            var ul = $(this).parent('li').parent('ul');
            while(!ul.hasClass('sidebar-menu')){
                ul.parent('li').addClass("active");
                ul= ul.parent('li').parent('ul');
            }


        }
    });
    //endregion === Menu Highlighting ===

});