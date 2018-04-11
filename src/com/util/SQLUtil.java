package com.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.ResourceBundle;

import com.model.SearchModel;

public class SQLUtil {

	private static SQLUtil instance;

	private SQLUtil() {
	}

	public static SQLUtil getInstance() {
		if (instance == null) {
			instance = new SQLUtil();
		}
		return instance;
	}

	/**
	 * 数据库连接
	 * 
	 * @return
	 */
	private Connection getConnection() {

		ResourceBundle resource = ResourceBundle.getBundle("config");
		String connectionUrl = resource.getString("connectionUrl"); 
		
		// Declare the JDBC objects.
		Connection con = null;
		try {
			// Establish the connection.
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			con = DriverManager.getConnection(connectionUrl);

		}
		// Handle any errors that may have occurred.
		catch (Exception e) {
			//e.printStackTrace();
			//OutputUtil output = new OutputUtil();
			//output.createFile("debug", e.toString() + " sqlutil line 52");
		}
		return con;
	}
	
	/**
	 * 获取测试数据
	 * 
	 * @return
	 */
 	public ArrayList<SearchModel> getTestList() {

		ArrayList<SearchModel> datalist = new ArrayList<SearchModel>();
		
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			con = this.getConnection();
			String SQL = "select t.id,t.name,t.model,t.detail,t.classid,t.hits,t.updatetime,t.visible,t.other,tc.classname,tc.classtip from Test t inner join TestClass tc on t.classid = tc.id where t.visible <> 0";
			st = con.createStatement();
			rs = st.executeQuery(SQL);

			while (rs.next()) {
				SearchModel data = new SearchModel();
				data.setId(Integer.parseInt(rs.getString(1)));
				data.setName(rs.getString(2));
				data.setModel(rs.getString(3));
				data.setDetail(rs.getString(4));
				int classid = rs.getString(5) == null ? 0 : Integer.parseInt(rs.getString(6));
				data.setClassid(classid);
				data.setHits(Integer.parseInt(rs.getString(6)));
				data.setUpdatetime(HelpUtil.stringToDate(rs.getString(7)));
				data.setVisible(rs.getBoolean(8));
				data.setOther(rs.getString(9));
				data.setClassName(rs.getString(10));
				datalist.add(data);
			}
			//System.out.println("test 数目 :" + datalist.size());
		}
		// Handle any errors that may have occurred.
		catch (Exception e) {
			//e.printStackTrace();
			//OutputUtil output = new OutputUtil();
			//output.createFile("debug", e.toString() + " sqlutil line 96");
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
					//e.printStackTrace();
				}
			if (st != null)
				try {
					st.close();
				} catch (Exception e) {
					//e.printStackTrace();
				}
			if (con != null)
				try {
					con.close();
				} catch (Exception e) {
					//e.printStackTrace();
				}
		}

		return datalist;
	}
	
	/**
	 * 获取改动过的数据
	 * @param id
	 * @return
	 */
	public SearchModel getTestChangedData(String id){
		SearchModel data = new SearchModel();
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		
		try {
			con = this.getConnection();
			String SQL = "select t.id,t.name,t.model,t.detail,t.classid,t.hits,t.updatetime,t.visible,t.other,tc.classname,tc.classtip from Test t inner join TestClass tc on t.classid = tc.id where t.visible <> 0 and t.id=" + id;
			st = con.createStatement();
			rs = st.executeQuery(SQL);
			rs.next();
			data.setId(Integer.parseInt(rs.getString(1)));
			data.setName(rs.getString(2));
			data.setModel(rs.getString(3));
			data.setDetail(rs.getString(4));
			int classid = rs.getString(5) == null ? 0 : Integer.parseInt(rs.getString(6));
			data.setClassid(classid);
			data.setHits(Integer.parseInt(rs.getString(6)));
			data.setUpdatetime(HelpUtil.stringToDate(rs.getString(7)));
			data.setVisible(rs.getBoolean(8));
			data.setOther(rs.getString(9));
			data.setClassName(rs.getString(10));
		}
		// Handle any errors that may have occurred.
		catch (Exception e) {
			//e.printStackTrace();
			return null;
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e) {
					//e.printStackTrace();
				}
			if (st != null)
				try {
					st.close();
				} catch (Exception e) {
					//e.printStackTrace();
				}
			if (con != null)
				try {
					con.close();
				} catch (Exception e) {
					//e.printStackTrace();
				}
		}
		
		return data;
	}
	

}
