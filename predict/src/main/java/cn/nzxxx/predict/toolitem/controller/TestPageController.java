package cn.nzxxx.predict.toolitem.controller;


import cn.nzxxx.predict.toolitem.service.TestPageServiceI;
import cn.nzxxx.predict.toolitem.tool.Helper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/testPageController")
public class TestPageController{
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private TestPageServiceI testSer;
	/**
	 * 页面跳转
	 * @return
	 */
	@RequestMapping(params = "testPage")
	public ModelAndView list(HttpServletRequest request) {
		return new ModelAndView("com/weather/testpage/testPage");
	}

	@RequestMapping(value="test/upload")
    @ResponseBody
	public String testAA(String a,@RequestParam("filesName") MultipartFile file)
			throws Exception{
		String originalFilename = file.getOriginalFilename(); //原名
		System.out.println("XX"+originalFilename+"XX"+a+"YY");
		return "[\""+originalFilename+"\"]";
		//return  originalFilename;
	}
	@RequestMapping(value="test/bb")
	@ResponseBody
	public List testBB(String str) throws Exception{
		System.out.println(str);//元素顺序即排序顺序
		System.out.println(Helper.stringJSONToList(str).size());
		return null;
	}
	/*
	 * 测试接口
	 * testPageController/text/xx
	 */
	@RequestMapping(value="/text/xx")
	@ResponseBody
	public String text(String str)throws Exception  {
		String testService = testSer.testService(str);
    	return "[1,2]";
	}
	/*
	 * 空接口,用于前台异步加载数据
	 */
	@RequestMapping(value="/returnEmpty")
	@ResponseBody
	public List returnEmpty(String str)  {
		//System.out.println("是是是"+UserUtil.getCruUserRoleNo());
		List list=new ArrayList();
    	return list;
	}
	
	
	 
}
