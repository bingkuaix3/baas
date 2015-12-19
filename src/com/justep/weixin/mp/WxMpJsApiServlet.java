package com.justep.weixin.mp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import me.chanjar.weixin.common.bean.WxJsapiPrepayOrder;
import me.chanjar.weixin.common.bean.WxJsapiUnifiedOrder;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.common.util.json.WxGsonBuilder;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import me.chanjar.weixin.mp.util.crypto.WxMpCryptUtil;

/**
 * 
 * @author 007slm
 * @email 007slm@163.com
 *
 */
public class WxMpJsApiServlet extends HttpServlet{
	private static final long serialVersionUID = -425619570208237144L;
	
	WxMpServiceInstance instance = WxMpServiceInstance.getInstance();
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			String action = req.getParameter("action");
			if ("getTicket".equals(action)) {
				getTicket(req, resp);
			} else if ("chooseWXPay".equals(action)) {
				chooseWXPay(req, resp);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e);
		}
	}
	
	private void chooseWXPay(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		WxJsapiPrepayOrder prepayOrder = this.getPrepayOrder(req);    
	    Map<String,String> payReq = new HashMap<String,String>();
	    payReq.put("appId", prepayOrder.getAppId());
	    payReq.put("timeStamp", "" + (System.currentTimeMillis() / 1000));
	    payReq.put("nonceStr", prepayOrder.getNonceStr());
	    payReq.put("package", "prepay_id=" + prepayOrder.getPrepayId());
	    payReq.put("signType", "MD5");
	    
	    String paySign = WxMpCryptUtil.genWithAmple(payReq,instance.getWxMpConfigStorage().getAppKey());
	    payReq.remove("appId");
	    payReq.put("paySign", paySign);
	    
	    String payReqResult = WxGsonBuilder.create().toJson(payReq, HashMap.class);
	    resp.setCharacterEncoding("utf-8");
	    resp.getWriter().write(payReqResult);
	}
	
	private WxJsapiPrepayOrder getPrepayOrder(HttpServletRequest req){
		WxJsapiUnifiedOrder unifiedOrder = new WxJsapiUnifiedOrder();
		WxMpOAuth2AccessToken oauth2AccessToken = (WxMpOAuth2AccessToken)req.getSession().getAttribute("weixinOauth2AccessToken");
		if(oauth2AccessToken == null){
			throw new RuntimeException("请先通过微信Oauth2对x5外面授权,然后才能发起支付");
		}
		unifiedOrder.setOpenId(oauth2AccessToken.getOpenId());
		unifiedOrder.setAppId(instance.getWxMpConfigStorage().getAppId());
		unifiedOrder.setBody(req.getParameter("body"));
		unifiedOrder.setMchId(req.getParameter("mchId"));
		unifiedOrder.setNotifyUrl(req.getParameter("notifyUrl"));
		unifiedOrder.setOutTradeNo(req.getParameter("outTradeNo"));
		unifiedOrder.setSpbillCreateIp(req.getRemoteAddr());
		unifiedOrder.setTotalFee(req.getParameter("totalFee"));
		try {
			WxJsapiPrepayOrder prepayOrder = instance.getWxMpService().getPrePayOrder(unifiedOrder);
			return prepayOrder;
		} catch (WxErrorException e) {
			throw new RuntimeException(e);
		}
	}

	private void getTicket(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		try {
			String jsapiTicket = instance.getWxMpService().getJsapiTicket();
			resp.getWriter().write(jsapiTicket);
		} catch (WxErrorException e) {
			throw new RuntimeException(e);
		}
	}
}
