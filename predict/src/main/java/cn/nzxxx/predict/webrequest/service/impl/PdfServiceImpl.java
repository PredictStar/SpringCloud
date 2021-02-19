package cn.nzxxx.predict.webrequest.service.impl;


import cn.nzxxx.predict.toolitem.tool.Helper;
import cn.nzxxx.predict.webrequest.service.PdfServiceI;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


@Service("pdfService")
@Transactional
public class PdfServiceImpl implements PdfServiceI {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	/**
	 * 获取数据并翻译下载
	 * @author 子火 
	 * 2021年2月19日13:59:01
	 */
	@Override
	public void translateTaskCard(String analyPdfData, HttpServletRequest request, HttpServletResponse response)throws Exception{
        Map analyPdfM = Helper.stringJSONToMap(analyPdfData);

    }
	/*
	 * 根据id 获取 ANALY_PDF_DATA 数据
	 * 2021年2月19日13:58:47
	 */
    @Override
	public String getAnalyPdfData(String idd){
        String reStr="";
        if(StringUtils.isBlank(idd)){
            return reStr;
        }
		String sql="SELECT\n" +
				"crj_card.ANALY_PDF_DATA AS pdfdata\n" +
				"FROM\n" +
				"crj_card\n" +
				"WHERE CRJ_CARD_ID="+idd;
		List<Map<String, Object>> re=jdbcTemplate.queryForList(sql);
		if(re.size()>0){
            Map<String, Object> stringObjectMap = re.get(0);
            reStr=(String) stringObjectMap.get("pdfdata");
        }
		return reStr;
	}

 
	
}