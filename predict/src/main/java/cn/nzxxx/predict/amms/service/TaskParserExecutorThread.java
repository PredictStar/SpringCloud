package cn.nzxxx.predict.amms.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("taskParserExecutorThread")
@Scope("prototype")
public class TaskParserExecutorThread extends Thread{

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	

}
