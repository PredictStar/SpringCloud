package cn.nzxxx.predict.webinterface.service;


import java.util.List;
import java.util.Map;

public interface InterfaceServiceI {

	public String syncJobCard(String ID,String CARDSOURCE,String CREATEDBY)throws Exception;

	public String translateAirbusRC(Integer idInit)throws Exception;
}
