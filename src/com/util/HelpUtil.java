package com.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;

import com.model.SearchModel;

public class HelpUtil {
	
	/**
	 * 过滤html标签
	 * @param html
	 * @return
	 */
	public static String filterHtml(String html){
		
		//去除JS style &nbsp;
		String str=html.replaceAll("<script[\\s\\S]*script>|<style[\\s\\S]*style>|&nbsp;","");
		//去除html及其属性
		str=str.replaceAll("<[.[^<]]*>","");
		//将标点符号 Unicode空白符替换为“|”
		str=str.replaceAll("\\pP", "\\|").replaceAll("\\s", "\\|").replaceAll("\\|{1,}", " ");
		return str;
	} 
	
	/**
	 * 过滤tl等 不设置权重
	 * @param html
	 * @return
	 */
	public static Boolean filterString_setBootst(String s){
		if(!s.equals("aa")&&!s.equals("bb")&&!s.equals("cc")&&!s.equals("dd"))
		return true;
		else 
		return false;
	}
	
	/**
	 * 合并字符串数组中相同的字符串
	 * @param String[]
	 * @return
	 */
	public static String[] mergeIdentical(String[] str){
		Set<String> set = new TreeSet<String>();
		for (int i = 0; i < str.length; i++) {
			set.add(str[i]);
		}
		str = (String[]) set.toArray(new String[0]);
		return str;
	}
	
	/**
	 * string2json
	 * @return
	 */
	public static String convertToJson(ArrayList<SearchModel> datalist) {

		JSONArray jsonA = new JSONArray();
		
		for(SearchModel data : datalist)
		{
			Map<String, String> map = new HashMap<String, String>();
			map.put("id",String.valueOf(data.getId()));
			map.put("name",data.getName());
			map.put("model",data.getModel());
			map.put("detail",data.getDetail());
			map.put("classid",String.valueOf(data.getClassid()));
			map.put("hits",String.valueOf(data.getHits()));
			map.put("updatetime",HelpUtil.dateToString(data.getUpdatetime()));
			map.put("other",data.getOther());
			map.put("visible",String.valueOf(data.isVisible()));
			map.put("displayorder",String.valueOf(data.getDisplayorder()));
			map.put("classname",data.getClassName());
			
			jsonA.put(map);
		}
		
		return jsonA.toString();
	}
	
	/**
	 * fill displayorder by increasing number
	 * @return
	 */
	public static ArrayList<SearchModel> fillDisplayorder(ArrayList<SearchModel> datalist) {

		int i = 1;
		for(SearchModel sm : datalist){
			sm.setDisplayorder(i);
			i++;
			//System.out.println(sm.getDisplayorder());
		}
		return datalist;
	}
	
	/**
	 * change result into response format
	 * @return
	 */
	public static String responseFormat(String maindata,String dataCount){
		
		String result = "{" + maindata + "," + dataCount + "}";
				
		return result;
	}
	
	/**
	 * date2string
	 * @param date "yyyy-MM-dd HH:mm:ss"
	 * @return
	 */
	public static String dateToString(Date date){
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return formatter.format(date);
	}
	
	/**
	 * string2date
	 * @param dateString "yyyy-MM-dd HH:mm:ss"
	 * @return
	 * @throws ParseException 
	 */
	public static Date stringToDate(String dateString) throws ParseException{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   
		return  formatter.parse(dateString);
	}
}
