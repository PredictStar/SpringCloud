	//上传文件按钮点击
	//region 即 '模块标志,使多模块复用表';
	//dataid 即业务数据id,表业务数据关联此处上传的文件
	//callback 回调函数
	var uploadcallback;
	function fileSubmit(region,dataid,callback){		
		$("#doc").val("");//上传同一文件不会响应,所以写此;不会触发change事件
		$("#doc").trigger("click"); 
		if(!dataid){
			dataid='';
		}
		if(!region){
			region='';
		}
		if(!callback){
			uploadcallback='';
		}else{
			uploadcallback=callback;
		}
		$("#doc").attr("data-dataid",dataid);
		$("#doc").attr("data-region",region);
	}

	//选中文件的文件域改变运行
	$(document).on('change', "[type='file']", function() {
		var dataid=$("#doc").attr("data-dataid");
		var region=$("#doc").attr("data-region");
		$("form").ajaxForm({//上传的初始化声明,可写在事件中,不是立即执行,提交后才执行
			type: 'post', 
			url: '/joyair/surveyreportController/uploadFiles', 
			dataType:'json',//注意大小写
			data: {
				'dataid':dataid,
				'region':region
			},
			success: function(data) { 
				if(!data){data=[];}
				if(data.statusCode&&data.statusCode!=200){						
					wx_error('上传失败！');
					return;
				}else{
					if(data.valueDescribe){
						data=data.valueDescribe;
					}
					wx_success('上传成功！');
					if(typeof uploadcallback=="function"){uploadcallback();}
				}	
			}
		});
		$("[type='submit']").trigger("click");
	});
