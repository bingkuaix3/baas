package com.justep.weixin.mp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import me.chanjar.weixin.mp.bean.result.WxMpUser;

public class UserInfoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	WxMpServiceInstance instance = WxMpServiceInstance.getInstance();
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String userCode =request.getParameter("code");
		System.out.println(userCode);
		
		WxMpOAuth2AccessToken oauth2AccessToken;
		try {
			oauth2AccessToken = instance.getWxMpService().oauth2getAccessToken(userCode);
			request.getSession().setAttribute("weixinOauth2AccessToken", oauth2AccessToken);
			WxMpUser userInfo  = instance.getWxMpService().oauth2getUserInfo(oauth2AccessToken, "zh_CN");
			Map<String, String> map = new HashMap<String, String>();

			map.put("openid", oauth2AccessToken.getOpenId());
			map.put("nickname", userInfo.getNickname());
			map.put("country", userInfo.getCountry());
			map.put("province", userInfo.getProvince());
			map.put("city", userInfo.getCity());
			map.put("headimgurl", userInfo.getHeadImgUrl());

			response.setContentType("text/html;charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);

			ObjectMapper objectMapper = new ObjectMapper();
			String s = objectMapper.writeValueAsString(map);
	        response.getWriter().write(s);
		} catch (WxErrorException e) {
			e.printStackTrace();
		}
	}
	
}
