package cn.nzxxx.predict.amms.service;

import javax.annotation.Resource;

import cn.nzxxx.predict.config.pdftable.FormPdf;
import cn.nzxxx.predict.toolitem.tool.Helper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import cn.nzxxx.predict.amms.inter.AmmFileBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @ClassName: AmmComFileImpl  
* @Description: 用于处理导入数据相关信息的核心类  
* @author zhigangwang
* @date 2016年6月28日 上午9:15:46  
*/

@Service("AmComFileImpl")
public class AmComFileImpl {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public AmmFileBean loadAmmFile(Long id) throws Exception{
		String sql = "select * from AMMS_FILE where id =?";
		List<AmmFileBean> po=jdbcTemplate.query(sql,new BeanPropertyRowMapper(AmmFileBean.class),new Object[]{id});
	    if(po.size()>0){
			return po.get(0);
		}else{
			return null;
		}
	}
	public Integer getOneFile(String idd){
		Integer id=null;
		String sql="SELECT CARDID from amms_job_card t \n" +
				"where WORDPATH is null";
		if(StringUtils.isNotBlank(idd)){
			sql+=" and CARDID="+idd;
		}
		sql+=" limit 1";
		List<Map<String, Object>> re=jdbcTemplate.queryForList(sql);
		if(re.size()>0){
			Integer i=(Integer) re.get(0).get("CARDID");
			int updateN=updateid( i,"");
			if(updateN!=re.size()){
				String s="数据"+i+"已执行过了";
				logger.error(s);
			}else{
				id=i;
			}
		}
		return id;
	}
	//占用这条数据
	public int updateid(Integer CARDID,String WORDPATH){
		String updatesql="update amms_job_card set WORDPATH='"+WORDPATH+"' where CARDID="+CARDID+" and WORDPATH is null";
		int update = jdbcTemplate.update(updatesql);
		return update;
	}
	//查询主体数据
	public List<Map<String, Object>> getBody(Integer idd){
		String sql="select b.BODY,b.BODYTYPE,b.BODYIMAG from amms_job_cardbody b where b.cardid ="+idd +
				" ORDER BY b.SORTBY ";
		List<Map<String, Object>> re=jdbcTemplate.queryForList(sql);
		return re;
	}

	//对查询结果的二次处理
	public Map<String,Object> operateList(List<Map<String, Object>> re){
		Map reMap=new HashMap();
		List<byte[]> imageL=new ArrayList();
		reMap.put("image",imageL);
		StringBuilder sb=new StringBuilder();
		FormPdf fpdf=new FormPdf();
		Map mMap=new HashMap();
		Map<String,Integer> spaceRule=new HashMap<>();
		spaceRule.put("^\\d\\. ",0);
		spaceRule.put("^[A-Z]\\. ",3);
		spaceRule.put("^\\(\\d+\\) ",6);
		spaceRule.put("^\\([a-z]\\) ",9);
		spaceRule.put("^[0-9]+\\)",12);
		spaceRule.put("^[a-z]\\)",15);
		spaceRule.put("^(\\S+ )?NOTE:",-3);
		mMap.put("spaceRule",spaceRule);
		mMap.put("spaceNextNum",3);
		for(Map<String, Object> mapp: re){
			String BODYTYPE=(String) mapp.get("BODYTYPE");
			if("IMAG".equals(BODYTYPE)){
                byte[] by=(byte[]) mapp.get("BODYIMAG");
                if(by.length!=0){
                    List<byte[]> getImage=(List<byte[]>)reMap.get("image");
                    getImage.add(by);
                }
            }else{
                String BODY=(String) mapp.get("BODY");
                BODY=BODY.replaceAll("</div>|</tr>","\n").replaceAll("<[^>]+>|</span>","");
				BODY=Helper.nvlString(BODY);
				String[] bs=BODY.split("\n");
				StringBuilder bodysb=new StringBuilder();
				for(String str:bs){ //应该是有序的
					Map mapRule=new HashMap();
					str=fpdf.setSpace(mMap,str,mapRule);
					bodysb.append("\n"+str);

				}
				if(StringUtils.isNotBlank(BODY)){
					sb.append(bodysb.toString());
				}
            }
		}
        reMap.put("textt",Helper.nvlString(sb.toString()));
		return reMap;
	}
	

}
