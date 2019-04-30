package cn.nzxxx.predict.toolitem.service.impl;


import cn.nzxxx.predict.toolitem.service.TestPageServiceI;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service("testPageService")
@Transactional
public class TestPageServiceImpl implements TestPageServiceI {

	/**
	 * 测试用例随便整
	 * @author 子火 
	 * 2018年4月3日15:49:42
	 */
	@Override
	public String testService(String str)throws Exception{

		return str;
	}

 
	
}