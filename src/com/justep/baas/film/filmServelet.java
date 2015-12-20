package com.justep.baas.film;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Map;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.WxMpCustomMessage;
import me.chanjar.weixin.mp.bean.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.WxMpXmlOutTextMessage;

import com.alibaba.fastjson.JSONObject;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;
import com.justep.baas.data.Util;
import com.justep.weixin.mp.WxMpServiceInstance;

public class filmServelet extends HttpServlet {
	private static final long serialVersionUID = 5506302727994136101L;
	static WxMpServiceInstance instance = WxMpServiceInstance.getInstance();
	private static final String DATASOURCE_TAKEOUT = "jdbc/takeout";
	private static final String TABLE_TAKEOUT_FOOD = "picture";
	String doctor_suggestion = "";
	String userid = "";
	String price = "";
	protected static WxMpService wxMpService;

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
			case "message":
				message(request, response);
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
		} catch (WxErrorException e) {
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
		String doctor_suggestion = params.getString("doctor_suggestion");

		
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

	private static void message(ServletRequest request, ServletResponse response) throws SQLException, IOException, NamingException, WxErrorException {
		JSONObject params = (JSONObject) JSONObject.parse(request.getParameter("params"));
		// 获取参数

		String userid = params.getString("userid");
		String doctor_suggestion = params.getString("doctor_suggestion");
		String price = params.getString("price");
		instance.message(userid,doctor_suggestion,price);
	}

}
