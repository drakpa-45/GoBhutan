/**
 * Created by drakpa on 25-Jan-20.
 */

//region *** Function for Global Object ***

let dateFormat = 'dd-mm-yy';

function allowKeys(e) {
    //Allow: backspace, delete, tab, escape, enter
    if ($.inArray(e.keyCode, [46, 8, 9, 27, 13, 110]) !== -1 ||
        //Allow: Ctr+A
        (e.keyCode == 65 && e.ctrlKey === true) ||
        //Allow: home, end, left, right, down, up
        (e.keyCode >= 35 && e.keyCode <= 40)) {
        return true;
    }
    return false;
}
/*
function validateFileUpload(data){
    var acceptFileTypes = /(\.|\/)(gif|jpe?g|png|pdf|tiff|docx|doc|xml|xlsx|txt|zip)$/i;
    if(data.originalFiles[0]['type'].length && !acceptFileTypes.test(data.originalFiles[0]['name'])) {
        uploadErrors.push(data.originalFiles[0]['name'] + ' is not alloawed. Invalid file type.');
    }
    if(data.originalFiles[0]['size'] > 100000000) {
        uploadErrors.push(data.originalFiles[0]['name'] +' is too big, ' + parseInt(data.originalFiles[0]['size'] / 1024 / 1024) + 'MB. File should be smaller than your current file size.');
    }
}*/

function getDate(dateParam) {
    var d = new Date(dateParam);

    if (d.getTimezoneOffset() > 0) {
        d.setTime(d.getTime() + d.getTimezoneOffset() * 60 * 1000);
    } else {
        d.setTime(d.getTime() - d.getTimezoneOffset() * 60 * 1000);
    }

    return d;
}

/**
 * Responsible to populate the entire form
 * @param {json} data
 * @return {void}
 */
function populate(data) {

    for (var i in data) {
        if (typeof (data[i]) === 'object') {
            //populate(data[i]);
        } else {
            $(
                "input[type='text'][name='" + i + "'], " +
                "input[type='hidden'][name='" + i + "'], " +
                "input[type='checkbox'][name='" + i + "'], " +
                "input[type='date'][name='" + i + "'], " +
                "select[name='" + i + "'], textarea[name='" + i + "']"
            ).val((data[i]===null || data[i]==='null' ?'':data[i]));


            //$("input[type='date'][name='" + i + "']").val($.datepicker.formatDate(dateFormat,new Date(parseInt(data[i]))));


            $("input[type='radio'][name='" + i + "'][value='" + data[i] + "']").prop('checked', true);

        }
    }

    $('form').find('input[type="checkbox"]').each(
        function () {
            if ($(this).siblings('input[type="hidden"]').val() == "true" ||
                $(this).siblings('input[type="hidden"]').val() == 1) {
                $(this).prop('checked', true);
            } else {
                $(this).prop('checked', false);
            }
        }
    );
}


//region sweet alert

function successMsg(msg, url) {
    if (!url) {
        swal({
            title: "SUCCESS!",
            title: "Success!",
            text: msg,
            type: "success"
        })
    } else {
        swal({
            title: "SUCCESS!",
            text: msg,
            type: "success"
        }, function () {
            window.location = url;
        });
    }
}

function errorMsg(msg) {
    swal({
        title: "Error!",
        text: msg,
        type: "error",
        button: "ok"
    });

}
function warningMsg(msg) {
    swal({
        title: "Warning!",
        text: msg,
        type: "warning",
        button: "ok"
    });
}
//endregion


//endregion *** Function for Global Object ***

//*********************************************************************************************************

//region *** goBhutanGlobal Object ***
goBhutanGlobal = (function () {
    "use strict";

    function baseURL() {
        //TODO for production environment
        return window.location.protocol + '//' + window.location.host +  globalConf.context + '';
    }

    function baseReportLocation() {
        //TODO for production environment
        return window.location.protocol + '//' + window.location.host + '/amc/resources/reports/';
    }

    function ajax(url, type, data, callback) {
        $.ajax({
            url: url,
            type: type,
            data: data,
            success: callback
        });
    }

    function loadDropDown(element, data) {
        element.empty();
        if (!data) {
            data = [];
        }
        if (data.length) {
            element.append(
                $(
                    '<option/>', {
                        value: "",
                        text: "--- Please select ---"
                    }
                )
            );
            $.each(
                data, function (index, itemData) {
                    element.append(
                        $(
                            '<option/>', {
                                value: itemData.value,
                                text: itemData.text,
                                id:itemData.id
                            }
                        )
                    );
                }
            );
        }
    }

    function formIndexing(tableBody, serialNo, iterator) {
        if (!iterator)
            iterator = 0;
        var row = tableBody.find('tr');

        for (var i = 0; i < row.length; i++) {
            tableBody.children().eq(i).find(':input').each(
                function () {
                    if (this.name) {
                        this.name = this.name.replace(
                            /\[(\d+)\]/, function (str, p) {
                                return '[' + (i + iterator) + ']';
                            });
                    }

                    if ($(this).hasClass(serialNo)) {
                        $(this).val(i + 1);
                    }
                }
            );
        }
    }

    function formIndexingDiv(outerdiv, iterator) {
        if (!iterator)
            iterator = 0;

        var length = outerdiv.children().length;
        for (var i = 0; i < length; i++) {
            outerdiv.children().eq(i).find('.form-control').each(
                function () {
                    if (this.name) {
                        this.name = this.name.replace(
                            /\[(\d+)\]/, function (str, p) {
                                return '[' + (i + iterator) + ']';
                            });
                    }

                }
            );
        }
    }

    function formIndexingM(tbl, iterator) {
        if (!iterator)
            iterator = 0;

        var length = tbl.find('tbody').length;
        for (var i = 0; i < length; i++) {
            tbl.find('tbody').eq(i).find('.form-control').each(
                function () {
                    if (this.name) {
                        this.name = this.name.replace(
                            /\[(\d+)\]/, function (str, p) {
                                return '[' + (i + iterator) + ']';
                            });
                    }

                }
            );
        }
    }

    function formIndexingN(tbody, iterator) {
        if (!iterator)
            iterator = 0;

        var row = tbody.find('tr');
        for (var i = 0; i < row.length; i++) {
            tbody.children().eq(i).children().children().each(
                function () {
                    var nth = 0;
                    if (this.name) {
                        this.name = this.name.replace(
                            /\[(\d+)\]/g, function (str, p) {
                                nth++;
                                return (nth === 2) ? '[' + (i - 1 + iterator) + ']' : str;
                            });
                    }
                }
            );
        }
    }

    function getSelectedOptionText(element, event) {
        return element.options[event.target.selectedIndex].text;
    }

    function handleCheckboxBeforeSave(form) {
        $(form).find('input[type="checkbox"]').each(
            function () {
                if ($(this).is(':checked')) {
                    $(this).next('input[type="hidden"]').val('true');
                } else {
                    $(this).next('input[type="hidden"]').val('false');
                }
            }
        );
    }

    function switchToInvalidTab(targetControl) {
        if (!targetControl)
            targetControl = $('.error');
        let errorContainedPane = targetControl.first().parents('.tab-pane'), tabPaneID = errorContainedPane.attr('id');
        errorContainedPane.addClass('active in');
        errorContainedPane.siblings().removeClass('active in');
        $('a[href$="#' + tabPaneID + '"]').parent('li').siblings().removeClass('active');
        $('a[href$="#' + tabPaneID + '"]').parent('li').addClass('active');
    }

    function convertAmountInWord(amount) {
        let th = ['', 'Thousand', 'Million', 'Billion', 'Trillion'];

        let dg = ['Zero', 'One', 'Two', 'Three', 'Four', 'Five', 'Six', 'Seven', 'Eight', 'Nine'];
        let tn = ['Ten', 'Eleven', 'Twelve', 'Thriteen', 'Fourteen', 'Fifteen', 'Sixteen', 'Seventeen', 'Eighteen', 'Nineteen'];
        let tw = ['Twenty', 'Thirty', 'Fourty', 'Fifty', 'Sixty', 'Seventy', 'Eighty', 'Ninety'];

        amount = amount.toString();
        let x = amount.indexOf('.');
        amount = amount.replace(/[\. ]/g, '');
        if (amount != parseFloat(amount))
            return 'not a number';

        if (x == -1) x = amount.length;
        if (x > 15) return 'Too Big';

        let n = amount.split('');
        let str = '';
        let sk = 0;
        for (let i = 0; i < x; i++) {
            if ((x - i) % 3 == 2) {
                if (n[i] == '1') {
                    str += tn[Number(n[i + 1])] + ' ';
                    i++;
                    sk = 1;
                } else if (n[i] != '0') {
                    str += tw[n[i] - 2] + ' ';
                    sk = 1;
                }
            }
            else if (n[i] != '0') {
                str += dg[n[i]] + ' ';
                if ((x - i) % 3 == 0) str += 'Hundred ';
                sk = 1;
            }
            if ((x - i) % 3 == 1) {
                if (sk) str += th[(x - i - 1) / 3] + ' ';
                sk = 0;
            }
        }

        if (x != amount.length) {
            let y = amount.length;
            str += 'Point ';
            for (let z = x; z < y; z++) str += dg[n[z]] + ' ';
        }
        return str.replace(/\s+/g, ' ');

    }

    function enterKeyAsTab(){
        $('form input').keydown(function(e){
            if(e.keyCode == 13){
                let inputs = $(this).parents('form').eq(0).find(':input');
                if(inputs[inputs.index(this)+1] != null){
                    inputs[inputs.index(this)+1].focus();
                }
                e.preventDefault();
                return false;
            }
        });
    }


    return {
        baseURL: baseURL,
        ajax: ajax,
        switchToInvalidTab: switchToInvalidTab,
        enterKeyAsTab:enterKeyAsTab
    };
})();
//endregion

//*********************************************************************************************************

//region *** Document Ready Method ***
$(document).ready(
    function () {
        //Local variable for show errors on pop instead of tooltip
        let submitted = false;

        //region *** jQuery custom validator ***
        $.validator.setDefaults(
            {
                ignore: '.ignore',
                focusCleanup: true,
                showErrors: function (errorMap, errorList) {
                    $.each(
                        this.validElements(), function (index, element) {
                            let $element = $(element);
                            $element.removeClass("error");
                        }
                    );
                    $.each(
                        errorList, function (index, error) {
                            let $element = $(error.element);
                            $element.addClass("error");
                        }
                    );
                    if(errorList && errorList.length > 0 ){
                        errorList[0].element.focus();
                    }

                },
                invalidHandler: function (event, validator) {
                    submitted = true;
                    goBhutanGlobal.switchToInvalidTab();
                }
            }
        );
        //endregion

        //region *** Restriction Event ***
        $('body').on(
            'keypress', '.number', function (e) {
                if (allowKeys(e)) {
                    return true;
                }
                let regex = new RegExp("^[0-9]+$");
                let str = String.fromCharCode(!e.charCode ? e.which : e.charCode);
                if (regex.test(str)) {
                    return true;
                }

                e.preventDefault();
                return false;
            }
        );
        $('body').on(
            'keypress', '.alphanumeric', function (e) {
                if (allowKeys(e)) {
                    return true;
                }
                let regex = new RegExp("^[a-zA-Z0-9]+$");
                let str = String.fromCharCode(!e.charCode ? e.which : e.charCode);
                if (regex.test(str)) {
                    return true;
                }

                e.preventDefault();
                return false;
            }
        );


        function isAmount(event, element) {
            let charCode = (event.which) ? event.which : event.keyCode;
            if (
                (charCode != 46 || $(element).val().indexOf('.') != -1) &&
                (charCode != 9) &&
                (charCode < 48 || charCode > 57) &&
                (charCode != 8))
                return false;
            else
                return true;
        }

        $('body').on(
            'keypress', '.amount', function (e) {
                if (allowKeys(e)) {
                    return;
                }
                return isAmount(e, this);
            }
        );


        $('body').on(
            'keydown', '.percentage', function (e) {
                if (allowKeys(e)) {
                    return;
                }

                if ((e.which >= 96 && e.which <= 105) || (e.which >= 48 && e.which <= 57) || e.which === 190 ||
                    e.which === 110) {

                } else {
                    e.preventDefault();
                }
            }
        );

        //endregion *** Restriction Event ***
        $('body').on('change','input:file',function(){
            if(this.files[0].size > 1024*1024*2){ //if size more than 2MB, donot allow file selection
                warningMsg('File exceeded the 2 MB capacity. File should be less than 2 MB!!!');
                $(this).val('');
            }
        });

        //endregion

        //region *** jQuery Custom Plugin ***
        $.fn.disableElements = function (status) {
            $(this).removeClass('error');
            $(this).each(
                function () {
                    $(this).attr('readonly', status);
                    $(this).find('option').prop('disabled', status);

                    if ($(this).is(':checkbox') || $(this).is(':radio')) {
                        $(this).attr('disabled', status);
                    }
                    $('input:checkbox[name=checkme]').attr('disabled', status);
                }
            );
        };

        $.fn.SimpleGridWithoutPaging = function (data, column_def) {
            //debugger;
            $(this).dataTable().fnDestroy();
            $(this).dataTable(
                {
                    "paging": false,
                    "retrieve": true,
                    "data": data,
                    "columns": column_def
                }
            );
        };

        $.fn.ditSimpleGrid = function (data, column_def) {
            //debugger;
            $(this).dataTable().fnDestroy();
            $(this).dataTable(
                {
                    "scrollY": "200px",
                    "data": data,
                    "columns": column_def,
                    "editable": true
                }
            );
        };

        $.fn.showModalDialog = function (url) {
            $.ajax(
                {
                    url: url,
                    success: function (data) {
                        $(this).html(data).dialog(
                            {
                                modal: true,
                                buttons: {
                                    Close: function () {
                                        $(this).dialog("close");
                                    }
                                }
                            }
                        );
                    }
                }
            );
        };
        //endregion *** jQuery Custom Plugin ***
    }
);
//endregion *** Document Ready Method ***