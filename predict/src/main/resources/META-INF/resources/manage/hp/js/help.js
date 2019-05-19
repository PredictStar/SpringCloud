//   console.log();   	(function(){	})()	
(function(){
	var hp={};
	window.hp=hp;
	/**
	 *方法简记
	 *date转为例2016-9-9格式
	 *date转为例2016-9-9 11:12:56格式
	 *根据所传日期获取多少天前或后日期
	 *根据所传日期获取多少小时前或后日期	
	 *返回两个日期的天数差
	 *时间验证-前台用if(!hp.yzdate(datea)||!hp.yzdate(dateb))形式避免弹多次	 * 
	 *打开全局加载遮罩层
	 *关闭全局加载遮罩层
	 *给特定ID加载遮罩层
	 *给特定ID关闭遮罩层 
	 *if里变量为false返回0
	 *if里变量为false返回""	
	 *去除全部空格 	
	 */
	
	
	/**
	 * 运算时一定不能为string,否则有时运算异常
	 * 非数字检测
	 if(endNum==""||isNaN(endNum)){
		 alertTip("检测到非数字",""); 
		 return;
	 }
	 *时间验证
	 //观测开始时间
		var starttime = $("#starttime").val();
		if(!starttime){
			  alertTip("请填写开始时间"); 
			  return;
		 }
		//观测结束时间
		var endtime = $("#endtime").val();
		if(!endtime){
			  alertTip("请填写结束时间"); 
			  return;
		}
		
		if(starttime>endtime){
			starttime=endtime;
			endtime= $("#starttime").val();
	 	}
	 	*集合判断是否为空
		if(array.length ==0)
	 *日期字符串比大小用"2007-01-02">"2007-01-01"		
	 *js中str->Date例:new Date("2016-5-5");//()里也可为其它格式例2016/8/8等
	 * */
	 
	/**
	 * 打开全局加载遮罩层,
	 * 用此注意,会和(easyUI还是t标签呢?)冲突,当执行关闭时,
	 * 解决:用openLoadingDimmerT方法
	 */
	hp.openLoadingDimmer=function(){
		$.mask_fullscreen();
	}
	/**
	 * 关闭所有加载遮罩层
	 */
	hp.closeLoadingDimmer=function(){
		$.mask_close_all();
		$(".scroll-off").removeClass("scroll-off");
	}
	/**
	 * 给特定ID加载遮罩层,例hp.openLoadingDimmerT("xx")
	 * 可对此单独改样式
	 * $("[ele='#xx'] div").css("margin-bottom","400px");
	 */
	hp.openLoadingDimmerT=function(id){
		$.mask_element('#'+id);
	}
	/**
	 * 给特定ID关闭遮罩层
	 */
	hp.closeLoadingDimmerT=function(id){
		$.mask_close('#'+id);
		$(".scroll-off").removeClass("scroll-off");
	}
	/**
	 * 自建全局遮罩层加载器
	 */
	hp.openLoading=function(){
		var html="<div id='hp_loadingL' style='width:100%;height:100%;left:0;top: 0;position: fixed;background:rgba(240, 240, 240, 0.4);z-index: 2999'>"+
					"<div style='position:fixed;left:50%;top:50%;transform:translate(-50%,-50%);'>"+
						" <link rel='stylesheet' href='manage/hp/css/hpStructure.css' type='text/css'/>  "+
						"<div style='width:30px;height:30px;margin-bottom:0px;display: inline-block;background:#42d00f;border-radius:50%;animation:LoadingMoveBTL 0.6s linear infinite alternate;'></div>"+
						"<div style='width:10px;height:10px;margin-bottom:10px;display: inline-block;background:#0abbc6;border-radius:50%;animation:LoadingMoveLTB 0.6s linear infinite alternate;'></div>"+
					"</div>"+
				 "</div>";
		parent.$("body").prepend(html);
	}
	/**
	 * 关闭自建全局遮罩层加载器
	 */
	hp.closeLoading=function(){
		parent.$("#hp_loadingL").remove();
	}
	
	/**
	 * 给特定ID加自建遮罩层加载器
	 */
	hp.openLoadingT=function(id){
		var html="<div id='hp_loadingL' style='width:100%;height:100%;left:0;top: 0;position: absolute;background:rgba(240, 240, 240, 0.4);z-index: 2999'>"+
					"<div style='position:absolute;left:50%;top:50%;transform:translate(-50%,-50%)'>"+						
						"<div style='width:30px;height:30px;margin-bottom:0px;display: inline-block;background:#42d00f;border-radius:50%;animation:LoadingMoveBTL 0.6s linear infinite alternate;'></div>"+
						"<div style='width:10px;height:10px;margin-bottom:10px;display: inline-block;background:#0abbc6;border-radius:50%;animation:LoadingMoveLTB 0.6s linear infinite alternate;'></div>"+
					"</div>"+
				 "</div>";
		$("#"+id).css("position","relative");
		$("#"+id).prepend(html);
	}
	/**
	 * 给特定ID关闭自建遮罩层加载器
	 */
	hp.closeLoadingT=function(id){
		$("#"+id+" #hp_loadingL").remove();
	}
	/**
	*date转为例2016-9-9格式;参要为Date类型
	*参2为false月补零,参2不写,默认false
	*js中str->Date例:new Date("2016-5-5");
	*()里也可为其它格式例2016/8/8等
	*若为 new Date("2016-05-05");即天和日都是(0几)时异常;时不会为0时会为8时
			 解决new Date("2016-05-05 0:0:0");
	*若只要时,时分秒(都!)要写例"2005-12-15 09:00:0";不能为"2005-12-15 09"
	*/
	hp.dateToString=function(date,bol){
		 var d=date;
		 var year=d.getFullYear();
		 var month=d.getMonth()+1;
		 var ri=d.getDate();
		 if(!bol){
			 if(month<10){
				 month="0"+month;
			 }
			 if(ri<10){
				 ri="0"+ri;
			 }
		 }				 			 
		 return year+"-"+month+"-"+ri;
	};
	hp.dateToStringMonth=function(date,bol){
		 var d=date;
		 var year=d.getFullYear();
		 var month=d.getMonth()+1;
		 var ri=d.getDate();
		 if(!bol){
			 if(month<10){
				 month="0"+month;
			 }
			 if(ri<10){
				 ri="0"+ri;
			 }
		 }				 			 
		 return year+"-"+month;
	};
	/**
	*date转为例2016-9-9 11:12:56格式
	*参2为false补零,参2不写,默认false
	*/
	hp.dateToStringTime=function(date,bol){
			 var d=date;
			 var year=d.getFullYear();
			 var month=d.getMonth()+1;
			 var ri=d.getDate();	
			 var hours=d.getHours();	
			 var minutes=d.getMinutes();
			 var seconds=d.getSeconds();
			 if(!bol){
				 if(month<10){
					 month="0"+month;
				 }
				 if(ri<10){
					 ri="0"+ri;
				 }
				 if(hours<10){
					 hours="0"+hours;
				 }
				 if(minutes<10){
					 minutes="0"+minutes;
				 }
				 if(seconds<10){
					 seconds="0"+seconds;
				 }
			 }				
			 return year+"-"+month+"-"+ri+" "+hours+":"+minutes+":"+seconds;
	};
	/**
	*date转为例2016-9-9 11格式
	*参2为false补零,参2不写,默认false
	*/
	hp.dateToStringHours=function(date,bol){
			 var d=date;
			 var year=d.getFullYear();
			 var month=d.getMonth()+1;
			 var ri=d.getDate();	
			 var hours=d.getHours();	
			 var minutes=d.getMinutes();
			 var seconds=d.getSeconds();
			 if(!bol){
				 if(month<10){
					 month="0"+month;
				 }
				 if(ri<10){
					 ri="0"+ri;
				 }
				 if(hours<10){
					 hours="0"+hours;
				 }
				 if(minutes<10){
					 minutes="0"+minutes;
				 }
				 if(seconds<10){
					 seconds="0"+seconds;
				 }
			 }				
			 return year+"-"+month+"-"+ri+" "+hours;
	};
	/**
	*根据所传日期获取多少天前或后日期	
	*/
	 hp.getdate=function(today,day){		
		var d=new Date(today-0);//若改为var d=today;则datea值会改变有继承性,会同dateb值; 同new Date().getTime()				
		var t=new Date(d.setDate(d.getDate()+day));//例返回8天前日期day为-8			
		return t;				
	};
	/**
	*根据所传日期获取多少小时前或后日期	
	*/
	 hp.gethours=function(today,hour){		
		var d=new Date(today-0);
		var t=new Date(d.setHours(d.getHours()+hour));	
		return t;				
	 };
	/**
	*根据所传日期获取多少月前日期	
	*/
	 hp.getMonth=function(today,month){		
		var d=new Date(today-0);
		var t=new Date(d.setDate(d.getDate()-(30*month)));
		return t;				
	 };
    /**
	 *根据所传日期获取多少分钟前或后日期	
	 */
	 hp.getMinutes=function(today,minutes){		
		var d=new Date(today-0);
		var t=new Date(d.setMinutes(d.getMinutes()+minutes));	
		return t;				
	 };
	 /**
	  * 返回两个日期的天数差
	  * 日期要为yyyy-MM-dd
	  * 例2-1与2-2返回1
	  */
	 hp.getDifferenceDay=function(aDate,bDate){
		 var cha=aDate.getTime()-bDate.getTime();
		 cha=Math.floor(Math.abs(cha))
		 return Math.floor(cha / (1000 * 60 * 60 * 24 ));
	 }
	 /**
	  * 返回两个日期的分钟差
	  * 猜返回值5:10:55同5:10:00
	  */
	 hp.getDifferenceMinutes=function(aDate,bDate){
		 var cha=aDate.getTime()-bDate.getTime();
		 cha=Math.floor(Math.abs(cha))
		 return Math.floor(cha / (1000 * 60));
	 }
   /**
	* ZG时间控件会拦截错误的时间,所以只需验证非空,0即可
	*时间验证-不符合日期弹框
	*date是字符串
	*前台用if(!hp.yzdate(datea)||!hp.yzdate(dateb)){return;}形式避免弹多次
	*现在用的是if(new Date('字符串')=="Invalid Date"){console.log("非日期格式"); }//判断字符串是否是日期				
	*/
	 hp.yzdate=function(date){
		var zz=/^(((20[1-2][0-9]-)(((0{0,1}[13578]|1[02])-(0{0,1}[1-9]|[12][0-9]|3[01]))|((0{0,1}[469]|11)-(0{0,1}[1-9]|[12][0-9]|30))|(0{0,1}2-(0{0,1}[1-9]|1[0-9]|2[0-8]))))|(2000-0{0,1}2-29)|(20(0[48]|[246][048]|[135][26])-0{0,1}2-29))$/
		if(!zz.test(date)){
			 pop_up("请选择2010-2029期间的日期","书写格式要为例:2010-1-1形式!");//(一些日期也会返回true如2000-2-29|2004-2-29|2068-2-29等)
			 return false;
			
		}
		return true;				
	};
	/**
	*ZG时间控件会拦截错误的时间,所以只需验证非空,0即可 
	*时间验证-不弹框
	*date是字符串
	*前台用hp.yzdateNo(datea)返回false|true
	*现在用的是if(new Date('字符串')=="Invalid Date"){console.log("非日期格式"); }//判断字符串是否是日期	
	*/
	 hp.yzdateNo=function(date){
		var zz=/^(((20[1-2][0-9]-)(((0{0,1}[13578]|1[02])-(0{0,1}[1-9]|[12][0-9]|3[01]))|((0{0,1}[469]|11)-(0{0,1}[1-9]|[12][0-9]|30))|(0{0,1}2-(0{0,1}[1-9]|1[0-9]|2[0-8]))))|(2000-0{0,1}2-29)|(20(0[48]|[246][048]|[135][26])-0{0,1}2-29))$/
		if(!zz.test(date)){
			// pop_up("请选择2010-2029期间的日期","书写格式要为例:2010-1-1形式!");//(一些日期也会返回true如2000-2-29|2004-2-29|2068-2-29等)
			 return false;			
		}
		return true;				
	};
//   console.log(); 
	
	 
	/**
	 * 参是0 '' NaN false "" null undefined返回0
	 */
	hp.falseTo0=function(num){
		if(!num){
			num=0;
	     }
		return num;
	}
	/**
	 * 参是0 '' NaN false "" null undefined返回""
	 */
	hp.falseToe=function(str){
		if(!str){
			str="";
	     }
		return str;
	}
	/**
	 * 去除全部空格 
	 * 去除两端空格用$.trim(str)--推荐(null返回"") 或 字符串.trim()若变量为null|undefined报错
	 */     
	hp.trims=function(str){
        var result;
        if(typeof str == "string"){
        	result = str.replace(/\s/g,"");//去除全部空格            
        }        
        return result;
	}

    /**
	 * 对当前路径问号传值获取
	 * 例location.search -> "?id=4028da0667743e330167782eaf1d0038"
	 * 若有参url表解析自定义路径,而不是location.search当前路径
	 * 	例 http://localhost:8080/moduleIndex.html?id=4028da0667743e330167782eaf1d0038
	 * 	获取 var paramMap={}; paramMap=hp.analyticParam(paramMap); paramMap.id->"4028da0667743e330167782eaf1d0038"
	 * 	value 类型是string(即使是 x=123)
     */
    hp.analyticParam=function(url){
        var map={};
        var local=location.search;
        if(url){
            local=url;
		}
        var search=local.replace(/\?/g,"&");
        var searchArray=search.split("&");
        for(var i = 0; i < searchArray.length; i ++) {
            var index=searchArray[i].indexOf("=");
            if(index!=-1){
                map[searchArray[i].substring(0,index)]=searchArray[i].substring(index+1);
            }
        }
        return map;
    }
//-------------------------------数据校验功能开始(使用见学习资料\JSP.HTML.CSS\UI\BootstrapUI\表单验证\说明 里的自定义验证)------------------------------------


	var checkoutLGValue={};//记录缓存数据使相同字典不重复请求
    //正确异常的样式共用方法
    function checkoutLGStyle(bol,element,input,message){//bol为true走正常样式;element:<small>的选择器;input:被绑定焦点事件的表单元素;message:错误提示语句
        //debugger;
    	if(!bol){//报错样式
    		console.log(input.val());
    		//debugger;
            input.css("cssText","border-color :#a94442!important;color:#a94442").attr({"data-checkout-value":input.val(),"data-checkout-status":"false","data-checkout-errormessage":message}).val(message);
            $(element).parent(".checkoutInputLG").addClass("has-error").removeClass("has-success");
            $(element).prev("i.glyphicon").addClass("glyphicon-remove").removeClass("glyphicon-ok").show();
        }else{//正确样式
            input.css("cssText","border-color :#3c763d!important").attr("data-checkout-status","true").removeAttr("data-checkout-value data-checkout-errormessage");
            $(element).parent(".checkoutInputLG").addClass("has-success").removeClass("has-error");
            $(element).prev("i.glyphicon").addClass("glyphicon-ok").removeClass("glyphicon-remove").show();
        }
    }
    //校验的实现
    function checkoutLGVerify(element,signvalue){
        var value=$(element).val();
        var input=$(element);
        var datacheckout=input.attr("data-checkout");
		var verify=checkoutLGValue[signvalue][datacheckout];
		if(!verify){verify={};}
		var trueM=true;//多个判断条件,最终是否是异常
		var messageM="";//异常时的信息
		if(trueM&&(verify.notEmpty)){//非空验证(即使输入的是0,val()获取的是"0",而不是0)
			var allowspace=value;
			if(!verify.notEmpty.allowspace){
				allowspace=$.trim(value);
			}
			if(!allowspace){//为空时
				trueM=false;
				messageM=verify.notEmpty.message;
			}
		}
		if(trueM&&(verify.notNumber)){//数字校验
			if($.trim(value)==""||isNaN(value)||value.indexOf(" ")!=-1){//非数字时
				trueM=false;
				messageM=verify.notNumber.message;
			}
		}
		if(trueM&&(verify.stringLength)){//长度验证
			var valLength=value.length;
			var minlengthBol=true;
			var maxlengthBol=true;
			if(verify.stringLength.min){
				if(verify.stringLength.min>valLength){
					var minlengthBol=false;
				}
			}
			if(verify.stringLength.max){
				if(verify.stringLength.max<valLength){
					var maxlengthBol=false;
				}
			}
			if(!(minlengthBol&&maxlengthBol)){//错误时
				trueM=false;
				messageM=verify.stringLength.message;
			}
		}
		if(trueM&&(verify.regexp)){//正则校验(注意所有信息是保存在samll元素里的)
			if(!(verify.regexp.source.test(value))){
				trueM=false;
				messageM=verify.regexp.message;
			}
		}
        if(trueM&&(verify.callback)){//
		    var inputJQ=$(element).parents(".checkoutInputLG").eq(0);
		    var fontJQ=$(".checkoutFontLG,[data-checkout='"+datacheckout+"']");
			var result=verify.callback.func(inputJQ,fontJQ,value);
            if((result)&&(!(result.result ))){
                trueM=false;
                messageM=result.message;
            }
        }
		if(trueM){//正确样式
			checkoutLGStyle(true,element,input,messageM);
		}else{//报错样式
			checkoutLGStyle(false,element,input,messageM);
		}
    }

    //初始化数据生成;并元素失去焦点时,进行校验
    hp.checkoutLG=function (set){
        var selector=set.sign;
        if(!selector){
            selector="body";
        }
        //解绑事件
        $(document).off(".signEvent");//必须放这,因为此方法一执行就会绑定事件
        $(document).on('mouseenter.signEvent', selector+" [data-checkout-status='false']", function() {
            var that = this;
            layer.tips($(this).attr("data-checkout-errormessage"), that);
        });


        var signvalue=selector.replace(/\W/g,"");
        checkoutLGValue[signvalue]={};
        checkoutLGValue[signvalue]=set.validators;
        var sign="."+signvalue;//去掉#.等特殊符号,此做事件的命名空间
        if(!set.hideStar){//生成米花
            $(selector+" .checkoutFontLG").prepend("<b style='color: red;'>*</b>");
        }
        $(selector+" .checkoutInputLG [data-checkout]").addClass('form-control').attr("data-checkout-status","true");//使符合bootstrap样式
        $(selector+" .checkoutInputLG").css("position","relative");

        $.each(checkoutLGValue[signvalue],function(key,value){//key:data-checkout值;也是传参时,validators下的key
            var ihide=value.glyphicon_hide==true||value.glyphicon_hide==false ?value.glyphicon_hide :set.glyphicon_hide ;
            if(!ihide){//加图标
                var style="";//加定位的top或right值
                var style_top=value.glyphicon_top ? value.glyphicon_top: set.glyphicon_top ? set.glyphicon_top :"";
                var style_right=value.glyphicon_right ? value.glyphicon_right: set.glyphicon_right ? set.glyphicon_right :"";
                if(style_top){style+="top:"+style_top+";";}
                if(style_right){style+="right:"+style_right+";";}
                $(selector+" [data-checkout='"+key+"']").parent(".checkoutInputLG").append("<i class='form-control-feedback glyphicon glyphicon-ok' style='display: none;"+style+"'></i>");
            }
            /*$(selector+" [data-checkout='"+key+"']").parent(".checkoutInputLG").append("<small  class='help-block' style='display: none;'></small>");*/

        });
        if(!set.isNotfocus){//不绑定事件
            //绑定失去焦点验证事件
            $(document).on('blur.signEvent', selector+' .checkoutInputLG [data-checkout]', function() {
                checkoutLGVerify(this,signvalue);
            });
            //绑定获取焦点时,数值还原
            $(document).on('focus.signEvent', selector+' .checkoutInputLG [data-checkout]', function() {
                $(this).nextAll("i.bv-no-label").remove();//解决与其它js的冲突
                if($(this).attr("data-checkout-status")=="false"){//是异常数据进入时,还原原数据清除警告语句
                    $(this).css("color","#191818").val($(this).attr("data-checkout-value"));
                }
            });
		}
    }
    //返回true与false ,使全部进行校验;参为任意表所在父元素
    hp.verifyLG=function (selector){
        if(!selector){
            selector=body;
        }
        var signvalue=selector.replace(/\W/g,"");
        $(selector+" [data-checkout-status='true']").each(function(index,element){
            checkoutLGVerify(element,signvalue);
        });
        var lengthFalse=$(selector+" [data-checkout-status='false']").size();
        if(lengthFalse!=0){
            return false;
        }
        return true;
    }
//-------------------------------数据校验功能结束---------------------------------------------
//仅仅是请求字典方法
	var dataRecord={};
    //bolRecord为true 使缓存字典
    hp.dictionaries=function (mark,type,callback,name,bolRecord) {
    	if(bolRecord&&dataRecord.hasOwnProperty(mark)){
            callback(dataRecord[mark],name);
		}else{
            var asyncT=true;
            if(bolRecord){
                asyncT=false;//异步原因是为了使缓存dataRecord能一开始就赋上值
            }
            $.ajax({
                'url': '/system/config/dictionary/getListByMark',
                'type': 'get',
                'dataType': 'json',
                'async': asyncT,
                'data': {
                    "mark": mark,
                    "type": type
                },
                'success': function (data) {
                    if (data.flag == "1") {
                        var list=[];
                        $(data.data).each(function (index, item) {
                            var map = {};
                            map['text'] = item.name;
                            map['code'] = item.code;
                            list.push(map);
                        });
                        dataRecord[mark]=list;
                        callback(list,name);
                    }
                }
            });
		}

    }
    //回调函数,可灵活去写此方法(适应各种情况),下方法使实现简单的下拉选择框
    hp.politicsStatusCallback =function (data,name) {
        //data=[{'text':'党员','code':'01'},{'text':'群众','code':'02'}];
        for (var i = 0; i < data.length; i++){
            $("[name='"+name+"']").append("<option value='"+data[i].code+"'>"+data[i].text+"</option>");
        }
    }
	//使用可 hp.dictionaries("dttz_zslx","1",hp.politicsStatusCallback,"zzsAge");

})()		
		