package cn.nzxxx.predict.webinterface.service;


import java.util.List;
import java.util.Map;

public interface InterfaceServiceI {

	public String syncJobCard(String ACTYPE,String CARDSOURCE,String JOBCARDNO)throws Exception;

	public String translateAirbusRC(Integer idInit)throws Exception;
}
