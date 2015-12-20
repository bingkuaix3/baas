package com.justep.weixin.mp;

import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import me.chanjar.weixin.common.exception.WxErrorException;

public class DownloadPictureServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -160178422365625049L;
	WxMpServiceInstance instance = WxMpServiceInstance.getInstance();

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		 
		// 参数序列化
		JSONObject params = (JSONObject) JSONObject.parse(request.getParameter("params"));

		// 获取参数
		String picture = params.getString("picturedata");
		
		System.out.println(picture);
		try {
			File picturefile = instance.downloadImage(picture);
		} catch (WxErrorException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}

}
