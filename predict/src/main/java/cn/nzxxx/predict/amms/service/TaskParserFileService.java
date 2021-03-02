package cn.nzxxx.predict.amms.service;

import java.io.File;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import cn.nzxxx.predict.amms.inter.AmmErrorInterface;
import cn.nzxxx.predict.amms.inter.AmmFileBean;
import cn.nzxxx.predict.amms.jobcard.entity.AmmsJobCard;
import cn.nzxxx.predict.amms.jobcard.mapper.AmmsJobCardMapper;
import cn.nzxxx.predict.toolitem.entity.Help;
import cn.nzxxx.predict.toolitem.tool.Helper;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.data.DocxRenderData;
import com.deepoove.poi.data.PictureRenderData;
import com.deepoove.poi.data.PictureType;
import com.deepoove.poi.data.Pictures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

@Service
public class TaskParserFileService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private int stateNow=1;
	@Resource(name = "AmComFileImpl")
	AmComFileImpl sqlService;
	@Autowired
	private AmmsJobCardMapper ammsJobCardMapper;
	@Resource(name = "taskParserExecutorThread")
	TaskParserExecutorThread taskParserExecutorThread;

	public static Map<Long, TaskParserExecutorThread> executorMap = new ConcurrentHashMap<Long, TaskParserExecutorThread>();
	public String createWord(String idd) throws Exception {
		//获取一条需要生成word的数据
		Integer id=sqlService.getOneFile(idd);
		String res="";
		if(id==null){
			return Help.returnClass(200,"无查询结果","");
		}
		List<Map<String, Object>> getBody=sqlService.getBody(id);
		Map<String,Object> operatemap=sqlService.operateList(getBody);
		operatemap.put("id",id);
		String pathh=setTemp(operatemap);
		res=Help.returnClass(200,"word生成成功",pathh);
		return res;
	}
	public String createTask(Long ammfileid,String type) throws Exception {

		logger.info("开始解析数据:["+ammfileid+"]");
		logger.info("解析的类型是:["+type+"]");
		//多次请求接口方法时,只同时在跑一个;非1表之前的此方法还未结束
		if(stateNow!=1){
			return AmmErrorInterface.TASK_IS_RUN;
		}
		try {
			stateNow++;
			AmmFileBean fileBean = sqlService.loadAmmFile(ammfileid);
			System.out.println(Helper.pojoToStringJSON(fileBean));
			if(fileBean==null) {
				logger.error("没有找到解析的数据");
				stateNow=1;
				return AmmErrorInterface.NO_FIND_FILE;
			}

			/*taskParserExecutorThread.setAmmfileid(ammfileid);
			taskParserExecutorThread.setFileBean(fileBean);
			taskParserExecutorThread.setParserType(type);*/

			executorMap.put(ammfileid, taskParserExecutorThread);
			/*taskExecutorThreadPool.execute(taskParserExecutorThread);*/

		}catch (Exception e){
			stateNow=1;
			String strE=Helper.exceptionToString(e);
			logger.error(strE);
			String strEInfo=strE.substring(0,500>strE.length()?strE.length():500);
			System.out.println(strEInfo);
			return strEInfo;
		}
		stateNow=1;
		return AmmErrorInterface.SUCCESS;
	}

	/* 给模板赋值
	 * {{titV}}
	 * {{+images}}
	 */
	public String setTemp(Map<String,Object> operatemap)throws Exception{
		String filePath = ResourceUtils.getURL("classpath:").getPath();//D:/SpringCloud/predict/target/classes/
		//主模板名称
		String mainNameT="taskCardAMMS.docx";
		int imageW=669;//图片宽
		int imageH=960;//图片高
		filePath=filePath+"META-INF/resources/wordtemplate/";
		String templatePath = filePath+mainNameT;
		XWPFTemplate template = XWPFTemplate.compile(templatePath);
		Map<String, Object> params = new HashMap<String, Object>();
		//文本赋值
		String textt =(String) operatemap.get("textt");
		params.put("titV", textt);
		//图片赋值
		List<byte[]> imageL =(List) operatemap.get("image");
		if(imageL.size()>0){
			List<Map> subData = new ArrayList<Map>();
			for(int i=0;i<imageL.size();i++){
				byte[] bytes = imageL.get(i);
				PictureRenderData pictureRenderData = Pictures.ofBytes(bytes, PictureType.PNG).size(imageW, imageH).create();
				Map s1 = new HashMap();
				s1.put("image", pictureRenderData);
				subData.add(s1);
			}
			params.put("images", new DocxRenderData(new File(filePath+"imageT.docx"), subData));
		}
		// 模板赋值
		template.render(params);
		//配置文件值获取
		ResourceBundle re = java.util.ResourceBundle.getBundle("application");//application.properties里值
		String saveMain = re.getString("saveurl.main");
		String saveExtend = re.getString("saveurl.taskcard.extend");
		//保存后的文件夹位置(要事先存在)
		String saveUrl=saveMain+saveExtend+"amms";
		// 创建文件夹
		File file = new File(saveUrl);
		if (!file.exists()) {
			file.mkdirs();
		}
		Integer idd=(Integer)operatemap.get("id");
		//保存后文件名
		String saveName=String.valueOf(idd);
		String pathh=saveUrl+"/"+saveName+".docx";
		template.writeToFile(pathh);
		template.close();
		AmmsJobCard ajc=new AmmsJobCard();
		ajc.setCardid(idd);
		ajc.setWordpath(pathh);
		ammsJobCardMapper.updateById(ajc);
		return pathh;
	}



}
