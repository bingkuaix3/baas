package com.justep.weixin.mp;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
public class WxMpDemoServer {
	public static void main(String[] args) throws Exception {
		Server server = new Server(80);
		ServletHandler handler = new ServletHandler();
		server.setHandler(handler);
		handler.addServletWithMapping(DefaultServlet.class, "/*");
		handler.addServletWithMapping(UserInfoServlet.class, "/oauth2Callback");
		handler.addServletWithMapping(WxMpJsApiServlet.class, "/jsapi");
		server.start();
		server.join();
	}
}
