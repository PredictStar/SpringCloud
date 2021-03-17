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
	 *if里变量为false返回0
	 *if里变量为false返回""	
	 *去除全部空格 	
	 */

	
	/**
	*date转为例2016-9-9格式;参要为Date类型
	*参2为false月补零,参2不写,默认false
	*js中str->Date例:new Date("2016-5-5");
	*	()里也可为其它格式例2016/8/8等
	*	若为 new Date("2016-05-05");即天和日都是(0几)时异常;时不会为0时会为8时!!
			解决new Date("2016-05-05 0:0:0");
	*	即使只要时,时分秒也(都!)要写例"2005-12-15 09:00:0";不能为"2005-12-15 09"
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
	*date转为例2016-9-9 11:12:56.332格式
	*参2为false补零,参2不写,默认false
	*/
	hp.dateToStringTimeMs=function(date,bol){
		var d=date;
		var year=d.getFullYear();
		var month=d.getMonth()+1;
		var ri=d.getDate();	
		var hours=d.getHours();	
		var minutes=d.getMinutes();
		var seconds=d.getSeconds();
		var ms=d.getMilliseconds();
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
		return year+"-"+month+"-"+ri+" "+hours+":"+minutes+":"+seconds+"."+ms;
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
		var t=new Date(d.setDate(d.getDate()-(31*month)));
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
	 * 参是'' NaN false "" null undefined 返回""
	 */
	hp.falseToe=function(str){
		if(!str&&str!=0){
			str="";
		}
		return str;
	}
	/**
	 * 去除全部空格 
	 * 去除两端空格用$.trim(str)--推荐(null|undefined返回""(猜某些看不到的特殊符也会清掉))
	   或 字符串.trim()若变量为null|undefined报错
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
     * 	获取 var paramMap={}; paramMap=hp.analyticParam(url|无); paramMap.id->"4028da0667743e330167782eaf1d0038"
	 *  问号后有中文,获取时是URL编码格式,所以需要decodeURI() 转换一下
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


})()		
		