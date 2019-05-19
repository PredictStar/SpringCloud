(function ($) {
    var bolSelect=false;//不让能选择"请选择"
    //1.定义jquery的扩展方法bootstrapSelect
    $.fn.bootstrapSelect = function (options, param) {
        if (typeof options == 'string') {
            return $.fn.bootstrapSelect.methods[options](this, param);
        }
        //2.将调用时候传过来的参数和default参数合并
        options = $.extend({}, $.fn.bootstrapSelect.defaults, options || {});
        //3.添加默认值
        var target = $(this);
        if (!target.hasClass("selectpicker")) target.addClass("selectpicker");
        target.attr('valuefield', options.valueField);
        target.attr('textfield', options.textField);
        target.empty();
        var option = $('<option disabled=\'disabled\'></option>');
        if(bolSelect){
            option = $('<option></option>');
        }
        option.attr('value', '');
        option.text(options.placeholder);
        target.append(option);
        //4.判断用户传过来的参数列表里面是否包含数据data数据集，如果包含，不用发ajax从后台取，否则否送ajax从后台取数据
        if (options.data&&options.data.length>0) {
            setTimeout(function(){//解决静态数据生成后点击无响应
                init(target, options.data);
            },300);

        }
        else {
            options.onBeforeLoad.call(target, options.param);
            if (!options.url) return;
            $.getJSON(options.url, options.param, function (data) {
                init(target, data);
            });
        }

        function init(target, data) {
            $.each(data, function (i, item) {
                var option = $('<option></option>');
                option.attr('value', item[options.valueField]);
                option.text(item[options.textField]);
                target.append(option);
            });
            options.onLoadSuccess.call(target);
        }

        target.unbind("change");
        target.on("change", function (e) {
            if (options.onChange)
                return options.onChange(target.val());
        });
    }

    //5.如果传过来的是字符串，代表调用方法。
    $.fn.bootstrapSelect.methods = {
        getValue: function (jq) {
            return jq.val();
        },
        setValue: function (jq, param) {
            jq.val(param);
        },
        load: function (jq, url) {
            $.getJSON(url, function (data) {
                jq.empty();
                var option = $('<option disabled=\'disabled\'></option>');
                if(bolSelect){
                    option = $('<option></option>');
                }
                option.attr('value', '');
                option.text('请选择');
                jq.append(option);
                $.each(data, function (i, item) {
                    var option = $('<option></option>');
                    option.attr('value', item[jq.attr('valuefield')]);
                    option.text(item[jq.attr('textfield')]);
                    jq.append(option);
                });
            });
        }
    };

    //6.默认参数列表
    $.fn.bootstrapSelect.defaults = {
        url: null,
        param: null,
        data: null,
        valueField: 'value',
        textField: 'text',
        placeholder: '请选择',

    };
    $.fn.selectpicker.defaults = {
        noneSelectedText: '没有选中任何项',
        noneResultsText: '没有找到匹配项',
        countSelectedText: '选中{1}中的{0}项',
        maxOptionsText: ['超出限制 (最多选择{n}项)', '组选择超出限制(最多选择{n}组)'],
        multipleSeparator: ', ',
        selectAllText: '全选',
        deselectAllText: '取消全选'
    };
    var bootstrapSelect={};
    window.bootstrapSelect=bootstrapSelect;
    //初始化
    bootstrapSelect.init=function(select){
        $(select).selectpicker('refresh');//使生成新的下拉选择框
        $(select).on('change', function () {
            if($(this)){
                var listValue=$(this).parents(".bootstrap-select").eq(0).find(".filter-option-inner-inner").attr("data-filter-option-inner-inner-value");


                if(!listValue){
                    $(this).next(".dropdown-toggle").addClass("bs-placeholder");
                    $(this).parents(".bootstrap-select").eq(0).find(".filter-option-inner-inner").text("没有选中任何项!");
                }else{
                    $(this).next(".dropdown-toggle").removeClass("bs-placeholder");

                    /*if(!$(select).attr("multiple")){//说明是单选
                        listValueStr=$(select).find("[value='"+$(select).val()+"']").text();
                        //console.log(listValueStr);
                    }*/
                    $(this).parents(".bootstrap-select").eq(0).find(".filter-option-inner-inner").text(listValue);
                }
            }
        });
    }
    //字典函数//例bootstrapSelect.dictionaries({selectID:'test1',mark:"dwgl_orgType",type:"1"});//若设置bolSelect为true则可选中"请选择"(即空)
    var dataRecordS={};
    bootstrapSelect.dictionaries=function(param){
        $('#'+param.selectID).selectpicker('destroy');//若已存在则销毁,此句作用见文档
        if(param.bolSelect){
            bolSelect=true;
        }
        if(param.bolRecord&&dataRecordS.hasOwnProperty(param.mark)){
            $('#'+param.selectID).bootstrapSelect({
                data: dataRecordS[param.mark],
                valueField: 'value',//根据返回值去写此进行绑定值
                textField: 'text',
                onLoadSuccess:function () {
                    bootstrapSelect.init("#"+param.selectID);
                    if(param.onLoadSuccess){
                        param.onLoadSuccess();
                    }
                }
            });

        }else{
            var asyncT=true;
            if(param.bolRecord){
                asyncT=false;//异步原因是为了使缓存dataRecord能一开始就赋上值
            }
            $.ajax({
                'url' :'/system/config/dictionary/getListByMark',
                'type':'get',
                'dataType':'json',
                'async': asyncT,
                'data' : {
                    "mark":param.mark,
                    "type":param.type
                },
                'success' : function(data){
                    if(data.flag == "1"){
                        var list=[];
                        $(data.data).each(function(index,item){
                            var map={};
                            map['text']=item.name;
                            map['value']=item.code;
                            if(param.global){//传进全局变量进行赋值
                                param.global[item.code]=item;
                            }
                            list.push(map);
                        });
                        if(list.length!=0){
                            dataRecordS[param.mark]=list;
                            $('#'+param.selectID).bootstrapSelect({
                                data: list,
                                valueField: 'value',//根据返回值去写此进行绑定值
                                textField: 'text',
                                onLoadSuccess:function () {
                                    //console.log(("成功返回"));
                                    bootstrapSelect.init("#"+param.selectID);
                                    if(param.onLoadSuccess){
                                        //console.log("成功后执行的回调函数");
                                        param.onLoadSuccess();
                                    }
                                }
                            });
                        }
                    }else{
                        if(data.msg){
                            layer.msg("获取字典项失败！" + data.msg,{icon:2});
                        }else{
                            layer.msg("获取字典项异常！请刷新重试！",{icon:2});
                        }
                    }
                },'error':function(){
                    layer.msg("加载字典项异常！请刷新重试！",{icon:2});
                },'complete':function(){
                }
            });
        }
    }


    //ajax封装//bootstrapSelect.ajax({selectID:'test1',url:'/user/getUserLevel',data:{"type":"1"},text:'occupation_name',value:'occupation_level'});
    bootstrapSelect.ajax=function(param){
        $.ajax({
            'url' :param.url,
            'type':'get',
            'dataType':'json',
            'data' : param.data,
            'success' : function(data){
                if(data.flag == "1"){
                    var list=[];
                    $(data.data).each(function(index,item){
                        var map={};
                        map['text']=item[param.text];
                        map['value']=item[param.value];
                        if(param.global){//传进全局变量进行赋值
                            param.global[item[param.value]]=item;
                        }
                        list.push(map);
                    });
                    //console.log(list);
                    if(list.length!=0){
                        $('#'+param.selectID).bootstrapSelect({
                            data: list,
                            valueField: 'value',//根据返回值去写此进行绑定值
                            textField: 'text',
                            onLoadSuccess:function () {
                                //console.log(("成功返回"));
                                bootstrapSelect.init("#"+param.selectID);
                                if(param.onLoadSuccess){
                                    //console.log("成功后执行的回调函数");
                                    param.onLoadSuccess();
                                }
                            }
                        });
                    }
                }else{
                    if(data.msg){
                        layer.msg("获取字典项失败！" + data.msg,{icon:2});
                    }else{
                        layer.msg("获取字典项异常！请刷新重试！",{icon:2});
                    }
                }
            },'error':function(){
                layer.msg("加载字典项异常！请刷新重试！",{icon:2});
            },'complete':function(){
            }
        });
    }
})(jQuery);
