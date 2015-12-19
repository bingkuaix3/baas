package com.justep.baas.film;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import com.alibaba.fastjson.JSONObject;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;
import com.justep.baas.data.Util;

public class filmServelet extends HttpServlet {
	private static final long serialVersionUID = 5506302727994136101L;

	private static final String DATASOURCE_TAKEOUT = "jdbc/takeout";
	private static final String TABLE_TAKEOUT_FOOD = "picture";
	
	public void service(ServletRequest request, ServletResponse response) throws ServletException {
		
		String action = request.getParameter("action");
		try {
			switch (action) {
			case "queryfilm":
				queryfilm(request, response);
				break;
			case "savefilm":
				savefilm(request, response);
				break;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ServletException(e);
		} catch (NamingException e) {
			e.printStackTrace();
			throw new ServletException(e);
		} catch (ParseException e) {
			e.printStackTrace();
			throw new ServletException(e);
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
	private static void queryfilm(ServletRequest request, ServletResponse response) throws SQLException, IOException, NamingException {
		// 参数序列化
		JSONObject params = (JSONObject) JSONObject.parse(request.getParameter("params"));

		// 获取参数
		Object columns = params.get("columns");
		Integer limit = params.getInteger("limit");
		Integer offset = params.getInteger("offset");
		Object picture= params.get("picture");
		System.out.println(picture);

		Table table = null;
		Connection conn = Util.getConnection(DATASOURCE_TAKEOUT);
		try {
			table = Util.queryData(conn, TABLE_TAKEOUT_FOOD, columns, null, "id ASC", null, offset, limit);
		} finally {
			conn.close();
		}

		// 输出返回结果
		Util.writeTableToResponse(response, table);
	}
	private static void savefilm(ServletRequest request, ServletResponse response) throws ParseException, SQLException, NamingException {
		// 参数序列化
		JSONObject params = (JSONObject) JSONObject.parse(request.getParameter("params"));
		// 获取参数
		JSONObject DemoData = params.getJSONObject("picturedata");
		Connection conn = Util.getConnection(DATASOURCE_TAKEOUT);
		try {
			conn.setAutoCommit(false);
			try {
				if (DemoData != null) {
					Table DemoTable = Transform.jsonToTable(DemoData);
					Util.saveData(conn, DemoTable, "picture");
				}
				conn.commit();
			} catch (SQLException e) {
				conn.rollback();
				throw e;
			}
		} finally {
			conn.close();
		}
	}
}
