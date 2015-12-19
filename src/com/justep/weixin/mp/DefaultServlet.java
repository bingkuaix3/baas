package com.justep.weixin.mp;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DefaultServlet extends HttpServlet {
	private static final long serialVersionUID = -3085539764084393258L;

	@Override
	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException,
			IOException {
		
		WxMpServiceInstance.getInstance().doResponse(request, response);
	}
}