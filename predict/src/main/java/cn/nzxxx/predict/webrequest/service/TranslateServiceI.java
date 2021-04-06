package cn.nzxxx.predict.webrequest.service;


import cn.nzxxx.predict.webrequest.mybatisJ.jobcard.entity.JobCardBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface TranslateServiceI {

	public String wordTranslate(String vall,String professional);
	public String sentenceTranslate(String vall,String professional,Map<Integer, List<Map<String, Object>>> splitSentenceL,List<JobCardBody> list,String initEnglish);
}
