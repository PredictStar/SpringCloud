package cn.nzxxx.predict.webrequest.service;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface PdfServiceI{
 
	public void translateTaskCard(String analyPdfData, HttpServletRequest request, HttpServletResponse response)throws Exception ;
	public String getAnalyPdfData(String idd);
}
