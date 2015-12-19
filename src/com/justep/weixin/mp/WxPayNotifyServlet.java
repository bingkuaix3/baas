package com.justep.weixin.mp;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author 007slm
 * @email 007slm@163.com
 *
 */
public class WxPayNotifyServlet extends HttpServlet{
	private static final long serialVersionUID = -8977805346295292012L;
	protected final Logger log = LoggerFactory.getLogger(WxPayNotifyServlet.class);
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doNotify(req,resp);
	}

	private void doNotify(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/xml;charset=utf-8");
		resp.setCharacterEncoding("utf-8");
		String resultCode = req.getParameter("return_code");
		if("SUCCESS".equals(resultCode)){
			String result_code = req.getParameter("result_code");
			if(result_code.equals("FAIL")){
				String err_code = req.getParameter("err_code");
				String err_code_des = req.getParameter("err_code_des");
				String out_trade_no = req.getParameter("out_trade_no");
				log.info("有一个交易失败["+err_code+"]:" +  err_code_des + "out_trade_no:" + out_trade_no);
			}else if(result_code.equals("SUCCESS")){
				String out_trade_no = req.getParameter("out_trade_no");
				/**
				 TODO 最好数据库里面有单独的订单支付模块
				 		首先检查对应业务数据的状态，判断该通知是否已经处理过，
				 		如果没有处理过再进行处理，如果处理过直接返回结果成功。
				 		在对业务数据进行状态检查和处理之前，要采用数据锁进行并发控制，以避免函数重入造成的数据混乱。
				 * 
				 */
				log.info("订单:" + out_trade_no + ",交易成功");
			}
		}else if("FAIL".equals(resultCode)){
			String returnMsg = req.getParameter("return_msg");
			log.error("有失败交易需要关注:" + returnMsg);
		}else{
			resp.setContentType("text/html;charset=utf-8");
			resp.getWriter().write("不要闹！");
			return;
		}
		resp.getWriter().write("<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");
	}
}
