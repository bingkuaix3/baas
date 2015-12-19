package com.justep.templates.common;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.naming.NamingException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;
import com.justep.baas.data.Util;

public class Common {

	public static void queryTestAll(ServletRequest request, ServletResponse response, String dbSource, String tableName) throws SQLException, IOException, NamingException {// 参数序列化
		JSONObject params = (JSONObject) JSONObject.parse(request.getParameter("params"));

		// 获取参数
		Object columns = params.get("columns");

		/*
		 * String id = params.getString("id");
		 * 
		 * List<Object> sqlParams = new ArrayList<Object>(); List<String>
		 * filters = new ArrayList<String>(); filters.add("fID = ?");
		 * sqlParams.add(id);
		 */

		Table table = null;
		Connection conn = Util.getConnection(dbSource);
		try {
			table = Util.queryData(conn, tableName, columns, null, "fID ASC", null, null, null);
		} finally {
			conn.close();
		}

		// 输出返回结果
		Util.writeTableToResponse(response, table);
	}

	public static void saveTest(ServletRequest request, ServletResponse response, String dbSource, String tableName) throws ParseException, SQLException, NamingException {
		// 参数序列化
		JSONObject params = (JSONObject) JSONObject.parse(request.getParameter("params"));
		// 获取参数
		JSONObject data = params.getJSONObject("data"); // 订单数据的JSON格式

		// JSON转换Table
		Table table = Transform.jsonToTable(data);
		Collection<String> columns = table.getColumnNames();

		// 获取数据源连接
		Connection conn = Util.getConnection(dbSource);
		try {
			// 开启事务
			conn.setAutoCommit(false);
			try {
				// 保存Table
				Util.saveData(conn, table, tableName, columns);
				// 提交事务
				conn.commit();
			} catch (SQLException e) {
				// 如果发生异常，首先回滚事务，然后把异常继续抛出
				conn.rollback();
				throw e;
			}
		} finally {
			// 必须关闭数据源连接
			conn.close();
		}
	}

	// 查询订单，实现了分页查询和按检索关键字过滤
	public static void queryTreeTest(ServletRequest request, ServletResponse response, String dbSource, String tableName) throws SQLException, IOException, NamingException {
		// 参数序列化
		JSONObject params = (JSONObject) JSONObject.parse(request.getParameter("params"));

		// 获取参数
		Object columns = params.get("columns"); // 列定义
		Integer limit = params.getInteger("limit"); // 分页查询的行数
		Integer offset = params.getInteger("offset"); // 分页查询的行偏移
		String search = params.getString("search"); // 检索关键字

		// 存放SQL中的参数值
		List<Object> sqlParams = new ArrayList<Object>();
		// 存放SQL中的过滤条件
		List<String> filters = new ArrayList<String>();
		if (!Util.isEmptyString(search)) {
			// 增加过滤条件
			filters.add("fUserName LIKE ? OR fPhoneNumber LIKE ? OR fAddress LIKE ? OR fContent LIKE ?");
			// 检索关键字中如果没有%，则前后自动加%
			search = (search.indexOf("%") != -1) ? search : "%" + search + "%";
			// sqlParams的参数个数和顺序必须与过滤条件的?相匹配
			for (int i = 0; i < 4; i++) {
				sqlParams.add(search);
			}
		}
		String sParent = params.getString("sParent");
		if (!Util.isEmptyString(sParent)) {
			filters.add("sParent = ?");
			sqlParams.add(sParent);
		}

		Table table = null;
		// 获取数据源连接
		Connection conn = Util.getConnection(dbSource);
		try {
			// 执行单表数据查询，返回Table
			table = Util.queryData(conn, tableName, columns, filters, "sID ASC", sqlParams, offset, limit);
		} finally {
			// 必须关闭数据源连接
			conn.close();
		}

		// 输出Table做为返回结果，这里会自动转换为Table的JSON格式
		Util.writeTableToResponse(response, table);
	}

	// 查询订单，实现了分页查询和按检索关键字过滤
	public static void queryTest(ServletRequest request, ServletResponse response, String dbSource, String tableName) throws SQLException, IOException, NamingException {
		// 参数序列化
		JSONObject params = (JSONObject) JSONObject.parse(request.getParameter("params"));

		// 获取参数
		Object columns = params.get("columns"); // 列定义
		Integer limit = params.getInteger("limit"); // 分页查询的行数
		Integer offset = params.getInteger("offset"); // 分页查询的行偏移
		String search = params.getString("search"); // 检索关键字

		// 存放SQL中的参数值
		List<Object> sqlParams = new ArrayList<Object>();
		// 存放SQL中的过滤条件
		List<String> filters = new ArrayList<String>();
		if (!Util.isEmptyString(search)) {
			// 增加过滤条件
			filters.add("fUserName LIKE ? OR fPhoneNumber LIKE ? OR fAddress LIKE ? OR fContent LIKE ?");
			// 检索关键字中如果没有%，则前后自动加%
			search = (search.indexOf("%") != -1) ? search : "%" + search + "%";
			// sqlParams的参数个数和顺序必须与过滤条件的?相匹配
			for (int i = 0; i < 4; i++) {
				sqlParams.add(search);
			}
		}

		Table table = null;
		// 获取数据源连接
		Connection conn = Util.getConnection(dbSource);
		try {
			// 执行单表数据查询，返回Table
			table = Util.queryData(conn, tableName, columns, filters, "fID ASC", sqlParams, offset, limit);
		} finally {
			// 必须关闭数据源连接
			conn.close();
		}

		// 输出Table做为返回结果，这里会自动转换为Table的JSON格式
		Util.writeTableToResponse(response, table);
	}

}
