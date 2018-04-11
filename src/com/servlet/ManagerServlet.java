package com.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.model.SearchModel;
import com.search.SearchHelper;
import com.util.HelpUtil;
import com.util.SQLUtil;

public class ManagerServlet extends HttpServlet {

	/**
	 * Constructor of the object.
	 */
	public ManagerServlet() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/json");
		response.setCharacterEncoding("utf-8");
		PrintWriter out = response.getWriter();
		
		SearchModel data = new SearchModel();
		SearchHelper lh = new SearchHelper();
		
		String type = request.getParameter("type");
		int id = Integer.parseInt(request.getParameter("id"));
		Boolean visible = Integer.parseInt(request.getParameter("visible"))==0?false:true;
		//System.out.println("type:"+type);
		//System.out.println("id:"+id);
		//System.out.println("visible:"+visible);
		
		//Îª±ÜÃâÉ¾³ý²Ù×÷Ò²Òªdata¸³Öµ£¬¹Êµ÷ÕûÅÐ¶ÏË³Ðò
		int searchNum = lh.searchByTerm("id",String.valueOf(id),type);
		if(visible){
			if(type.equals("test")){
				data = SQLUtil.getInstance().getTestChangedData(String.valueOf(id));
			}
			
			if(data == null){
				//System.out.println("[{\"errCode\":-1}]");
				out.print("{\"errCode\":-1}");
			}else{
				if(searchNum > 0){
					//System.out.println("update "+type+" by id:"+id);
					lh.updateIndex(String.valueOf(id), data, type);
				}else{
					//System.out.println("insert "+type+" by id:"+id);
					lh.addIndex(data, type);
				}
				//System.out.println("[{\"errCode\":0}]");
				out.print("{\"errCode\":0}");
			}
		}else{
			if(searchNum > 0){
				//System.out.println("delete "+type+" by id:"+id);
				lh.deleteIndex(String.valueOf(id), type);
				out.print("{\"errCode\":0}");
			}
		}
		out.flush();
		out.close();
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
	}

}
