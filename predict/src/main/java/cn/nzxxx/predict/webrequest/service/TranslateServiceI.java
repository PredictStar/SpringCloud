package cn.nzxxx.predict.webrequest.service;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface TranslateServiceI {

	public String wordTranslate(String vall,String professional);
	public String sentenceTranslate(String vall,String professional);
}
