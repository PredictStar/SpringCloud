package cn.nzxxx.predict.toolitem.tool.controller;


import cn.nzxxx.predict.toolitem.tool.HelperServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/helperController")
public class HelperController{
	@Autowired
	private HelperServiceI helperService;
	
	

	
}
