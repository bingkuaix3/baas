package com.justep.jpush;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import cn.jpush.api.JPushClient;
import cn.jpush.api.common.ClientConfig;
import cn.jpush.api.common.resp.APIConnectionException;
import cn.jpush.api.common.resp.APIRequestException;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.Notification;
import cn.jpush.api.schedule.ScheduleResult;

public class JPushServlet extends HttpServlet {
	
	private static final long serialVersionUID = 7114369087148230633L;
	private String appKey = "";
	private String masterSecret = "";
	private Boolean apnsProduction = false;
	private JPushClient jpushClient;
	
	private static final Logger logger = Logger.getLogger(JPushServlet.class);
	
	public void init() throws ServletException {
		super.init();
		
		String configFile = this.getServletContext().getRealPath("/WEB-INF/jpush.config.xml");
		try{
			SAXReader reader = new SAXReader();
			File file = new File(configFile);
			Document doc = reader.read(file);
			Element config = doc.getRootElement();
			appKey = config.elementTextTrim("appKey");
			masterSecret = config.elementTextTrim("masterSecret");
			apnsProduction = (config.elementTextTrim("apnsProduction").equals("true"))?true:false;
		}catch(Exception e){
			e.printStackTrace();
		}		
	}
	
	protected void service(HttpServletRequest request, HttpServletResponse response){
		String registrationId = request.getParameter("registrationId");
		String key = request.getParameter("appKey");
		String secret = request.getParameter("masterSecret");
		logger.info("registrationId:" + registrationId + ", key: " + key + ", masterSecret: " + secret);
		try {
			if (key=="" || key==null){
				key = appKey;
			}
			if (secret=="" || secret==null){
				secret = masterSecret;
			}
			this.sendPushMessage(registrationId, key, secret);
		} catch (APIConnectionException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (APIRequestException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	
	public ScheduleResult sendPushMessage(String registrationId, String key, String secret) throws APIConnectionException, APIRequestException{
		ClientConfig config = ClientConfig.getInstance();
		jpushClient = new JPushClient(secret, key, 3, null, config);
        PushPayload payload = PushPayload.newBuilder()
                .setPlatform(Platform.all())
                .setAudience(Audience.registrationId(registrationId))
                .setNotification(Notification.alert("亲爱的x5外卖用户，您的订单已经配货完毕，正在运送中.."))
                .build();
        payload.resetOptionsTimeToLive(86400);
        logger.info("apnsProduction:" + apnsProduction);
        payload.resetOptionsApnsProduction(apnsProduction);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar nowTime = Calendar.getInstance();
		nowTime.add(Calendar.MINUTE, 1);
		String scheduleTime =sdf.format(nowTime.getTime());
		ScheduleResult result = jpushClient.createSingleSchedule(UUID.randomUUID().toString().replaceAll("-", ""), scheduleTime, payload);
		logger.info("ScheduleResult:" + result);
        return result;
    }
}
