package com.justep.baas.takeout;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

import com.alibaba.fastjson.JSONObject;
import com.justep.baas.data.Table;
import com.justep.baas.data.Transform;
import com.justep.baas.data.Util;


public class DxServlet extends HttpServlet {

        private static final long serialVersionUID = 1452041171629615961L;
        private static final String DATASOURCE_TAKEOUT = "jdbc/takeout";
        private static final String TABLE_USER_INFO = "user_info";
        
        @Override
        // Servlet入口，通过判断action参数，进入各自对应的实现方法
        public void service(ServletRequest request, ServletResponse response) throws ServletException {
                try {
                        String action = request.getParameter("action");
                        if ("DuanXinCheck".equals(action)) {
                                DuanXinCheck(request, response);
                        }else if("saveUser".equals(action)){
                        	saveUser(request, response);
                        }else if("login".equals(action)){
                        	login(request, response);
                        }
                        
                } catch (Exception e) {
                        e.printStackTrace();
                        throw new ServletException(e);
                }
        }

         private static void DuanXinCheck(ServletRequest request, ServletResponse response) throws SQLException, IOException, NamingException , Exception{
                JSONObject params = (JSONObject) JSONObject.parse(request.getParameter("params"));
                        
                String fPhoneNumber = params.getString("fPhoneNumber");
                  //生成6位数字的随机字符串
                 int suiji = new Random().nextInt(899999)+100000;
                 String backStr=Integer.toString(suiji);
                 // 将随机字符串通过jsonOBJ返回前台 
                 JSONObject jsonObj = new JSONObject();
//                 System.out.println("backStr==="+backStr);
                 jsonObj.put("CheckCode", backStr);
                 Util.writeJsonToResponse(response, jsonObj);

                 HttpClient client = new HttpClient();
                 PostMethod post = new PostMethod("http://gbk.sms.webchinese.cn"); 
                 post.addRequestHeader("Content-Type","application/x-www-form-urlencoded;charset=gbk");
                 //在头文件中设置转码
                 NameValuePair[] data ={ new NameValuePair("Uid", "sky198127"),new NameValuePair("Key", "7b6da72cf462854530fa"),new NameValuePair("smsMob",fPhoneNumber ),new NameValuePair("smsText","验证码："+backStr+" (本信息由系统自动发出，不要回复）")};

        //这段Java代码是直接从他家demo拷贝下来的，只需要修改这句话，填写你在他家注册的用户名、短信密钥，参数中加要发送的手机号和验证码短信
                 post.setRequestBody(data);
        try{
                 client.executeMethod(post);
                 Header[] headers = post.getResponseHeaders();
                 int statusCode = post.getStatusCode();
                 System.out.println("状态码:"+statusCode);
                 for(Header h : headers)
                 {
                 System.out.println(h.toString());
                 }
                 String result = new String(post.getResponseBodyAsString().getBytes("gbk")); 
                 System.out.println(result); //打印返回消息状态

        }
        finally{
                 post.releaseConnection();
                 
        }
        

                 }
         
     	private static void saveUser(ServletRequest request, ServletResponse response) throws ParseException, SQLException, NamingException {
    		// 参数序列化
    		JSONObject params = (JSONObject) JSONObject.parse(request.getParameter("params"));
    		
    		// 获取参数
    		JSONObject userData = params.getJSONObject("userData");
    		
    		
    		Connection conn = Util.getConnection(DATASOURCE_TAKEOUT);
    		
    		try {
    			conn.setAutoCommit(false);
    			try {
    				if (userData != null) {
    					
    					Table userTable = Transform.jsonToTable(userData);
    					
    					Util.saveData(conn, userTable, TABLE_USER_INFO);
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
     	
     	private  void login(ServletRequest request, ServletResponse response) throws ParseException, SQLException, NamingException, IOException {
    		// 参数序列化
    		JSONObject params = (JSONObject) JSONObject.parse(request.getParameter("params"));
    		

    		// 获取参数
    		Object columns = params.get("columns");
    		Integer limit = params.getInteger("limit");
    		Integer offset = params.getInteger("offset");
    		String search = params.getString("search");
    		
    		String fPhoneNumber = params.getString("fPhoneNumber");
    		String fPassWord = params.getString("fPassWord");
//            System.out.println("fPhoneNumber="+fPhoneNumber+"#fPassWord="+fPassWord);

            

    		List<Object> sqlParams = new ArrayList<Object>();
    		// 构造过滤条件
    		List<String> filters = new ArrayList<String>();
    		if (!Util.isEmptyString(search)) {
    			filters.add(" fPhoneNumber = ? OR fPassWord = ? ");
    			// 多个问号参数的值
    			search = (search.indexOf("%") != -1) ? search : "%" + search + "%";
    			for (int i = 0; i < 2; i++) {
    				sqlParams.add(search);
    			}
    		}
  
    		sqlParams.add(fPhoneNumber);
    		sqlParams.add(fPassWord);
    		
    		Table table = null;
    		Connection conn = Util.getConnection(DATASOURCE_TAKEOUT);

    		String querysql = "select * from user_info u where u.fPhoneNumber=? and u.fPassWord=?";
    		 
    		try {
    			table = Util.queryData(conn, querysql, sqlParams, columns, offset, limit);
 
//    			System.out.println("jsonObj==="+Transform.tableToJson(table));	
    			
    		} finally {
    			conn.close();
    		}

    			Util.writeJsonToResponse(response,Transform.tableToJson(table) );
//				Util.writeTableToResponse(response, table);// 输出返回结果 
				
    	}
     	
     	
    /*	private static void login(ServletRequest request, ServletResponse response) throws SQLException, IOException, NamingException {
    		// 参数序列化
    		JSONObject params = (JSONObject) JSONObject.parse(request.getParameter("params"));

    		// 获取参数
    		Object columns = params.get("columns");
    		Integer limit = params.getInteger("limit");
    		Integer offset = params.getInteger("offset");
    		String search = params.getString("search");

    		// 存放SQL中的参数值
    		List<Object> sqlParams = new ArrayList<Object>();
    		// 构造过滤条件
    		List<String> filters = new ArrayList<String>();
    		if (!Util.isEmptyString(search)) {
    			filters.add(" fPhoneNumber = ? OR fPassWord = ? ");
    			// 多个问号参数的值
    			search = (search.indexOf("%") != -1) ? search : "%" + search + "%";
    			for (int i = 0; i < 2; i++) {
    				sqlParams.add(search);
    			}
    		}
    		
    		// 按电话号码过滤
    		String fPhoneNumber = params.getString("fPhoneNumber");
    		if (!Util.isEmptyString(fPhoneNumber)) {
    			filters.add("fPhoneNumber = ?");
    			sqlParams.add(fPhoneNumber);
    		}

    		Table table = null;
    		Connection conn = Util.getConnection(DATASOURCE_TAKEOUT);
    		try {
    			table = Util.queryData(conn, TABLE_USER_INFO, columns, filters, null, sqlParams, offset, limit);
    		} finally {
    			conn.close();
    		}

    		// 输出返回结果 
    		Util.writeTableToResponse(response, table);
    	}*/

     	
         
}