package com.justep.templates;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import com.justep.templates.common.Common;

public class Test extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6936354683539232032L;
	//下边两组常量是访问数据库的数据源和表名！请按照您的需求手动修改自己定义！common中查询和保存的实现也按照需求自己手动修改
	private static final String DATASOURCE_X5DEMO = "jdbc/x5demo";//jdbc/库名
	private static final String DATASOURCE_X5DEMO_BORROWBOOK = "DEMO_BorrowBook";//表名

	private static final String DATASOURCE_X5SYS = "jdbc/x5sys";
	private static final String DATASOURCE_X5SYS_SA_OPORG = "sa_oporg";
	
	
	
	public void service(ServletRequest request, ServletResponse response) throws ServletException {
		try {
			String action = request.getParameter("action");
			if ("queryTest".equals(action)) {
				Common.queryTest(request, response, DATASOURCE_X5DEMO, DATASOURCE_X5DEMO_BORROWBOOK);
			} else if ("saveTest".equals(action)) {
				Common.saveTest(request, response, DATASOURCE_X5DEMO, DATASOURCE_X5DEMO_BORROWBOOK);
			} else if("queryOrgTest".equals(action)){
				Common.queryTreeTest(request, response, DATASOURCE_X5SYS, DATASOURCE_X5SYS_SA_OPORG);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		}
	}
	
}
