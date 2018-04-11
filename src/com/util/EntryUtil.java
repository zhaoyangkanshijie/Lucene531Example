package com.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.model.SearchModel;
import com.search.SearchHelper;

public class EntryUtil {
	
	static{
		EntryUtil.initializeIndex();
		EntryUtil.setTime();
	}
	
	public static Boolean initializeIndex(){
		SearchHelper lh = new SearchHelper();
		Boolean flagt = lh.createIndex(SQLUtil.getInstance().getTestList(),"test");
		//System.out.println("initializing.....................................");
		if(flagt == false){
			return false;
		}else{
			return true;
		}
	}
	
	public static void setTime() {
		//System.out.println("setTiming.....................................");
	    Calendar calendar = Calendar.getInstance();
	    calendar.set(Calendar.HOUR_OF_DAY, 24); // 控制时
	    calendar.set(Calendar.MINUTE, 0);       // 控制分
	    calendar.set(Calendar.SECOND, 0);       // 控制秒
	    Date time = calendar.getTime();         // 得出执行任务的时间,此处为今天的12：00：00
	    
	    //String now = HelpUtil.dateToString(time);
	    //System.out.println(now);
	    
	    Timer timer = new Timer();
	    timer.scheduleAtFixedRate(new TimerTask() {
	        public void run() {
	        	//System.out.println("setTime.....................................");
	        	EntryUtil.initializeIndex();
	        }
	    }, time, 1000 * 60 * 60 * 24);// 这里设定将延时每天固定执行
	}
	
	public String searchServer(String keywords,String type) {
		SearchHelper lh = new SearchHelper();
		String result = "[]";
		String dataCount = "";
		if(keywords.length() < 2){
			result = "\"result\":" + "[]";
		}else{
			int testCount = lh.getSearchCount(keywords, "test");
			dataCount = "\"testCount\":\""+testCount+"\"";
			//System.out.println(testCount);
			
			if(type.equals("test")){
				ArrayList<SearchModel> datalistt = new ArrayList<SearchModel>();
				datalistt = lh.search(keywords, "test");
				
				if(datalistt.size() == 0){
					result = "\"result\":" + "[]";
				}else{
					datalistt = HelpUtil.fillDisplayorder(datalistt);
					result = HelpUtil.convertToJson(datalistt);
					result = "\"result\":" + result;
				}
			}else{
				result = "\"result\":" + "[]";
			}
		}
		//System.out.println(result);
		result = HelpUtil.responseFormat(result, dataCount);
		return result;
	}
	 
}
