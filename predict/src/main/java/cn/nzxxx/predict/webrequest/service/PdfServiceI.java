package cn.nzxxx.predict.webrequest.service;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface PdfServiceI{

	public String transTCInStorage(Map analyPdfM,Integer jobCardId)throws Exception ;
	public String translateTaskCard(Map analyPdfM, HttpServletRequest request, HttpServletResponse response)throws Exception ;
	public String getAnalyPdfData(String idd,String type);
}
