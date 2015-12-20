package com.justep.weixin.mp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.bean.WxMenu;
import me.chanjar.weixin.common.bean.WxMenu.WxMenuButton;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpConfigStorage;
import me.chanjar.weixin.mp.api.WxMpMessageHandler;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.bean.WxMpXmlOutTextMessage;

import org.apache.commons.lang3.StringUtils;

public class WxMpServiceInstance { 
	private WxMpService wxMpService;

	public WxMpService getWxMpService() {
		return wxMpService;
	}

	public void setWxMpService(WxMpService wxMpService) {
		this.wxMpService = wxMpService;
	}

	public WxMpConfigStorage getWxMpConfigStorage() {
		return wxMpConfigStorage;
	}

	public void setWxMpConfigStorage(WxMpConfigStorage wxMpConfigStorage) {
		this.wxMpConfigStorage = wxMpConfigStorage;
	}

	public WxMpMessageRouter getWxMpMessageRouter() {
		return wxMpMessageRouter;
	}

	public void setWxMpMessageRouter(WxMpMessageRouter wxMpMessageRouter) {
		this.wxMpMessageRouter = wxMpMessageRouter;
	}

	private WxMpConfigStorage wxMpConfigStorage;
	private WxMpMessageRouter wxMpMessageRouter;

	private static WxMpServiceInstance instance = null;

	public static WxMpServiceInstance getInstance() {
		if (instance == null) {
			instance = new WxMpServiceInstance();
		}
		return instance;
	}

	public WxMpServiceInstance() {
		try {
			InputStream inputStream = WxMpServiceInstance.class.getResourceAsStream("/../weixin.config.xml");
			WxMpXMLInMemoryConfigStorage config = WxMpXMLInMemoryConfigStorage.fromXml(inputStream);
			wxMpConfigStorage = config; 
			wxMpService = new WxMpServiceImpl();
			wxMpService.setWxMpConfigStorage(config);
			wxMpMessageRouter = new WxMpMessageRouter(wxMpService);
			this.addTestRouter();
			this.addMenuRouter();

		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}

	}

	// 拦截内容为test的消息
	private void addTestRouter() {
		WxMpMessageHandler handler = new WxMpMessageHandler() {
						@Override
			public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService wxMpService, WxSessionManager sessionManager) throws WxErrorException {
				System.out.println("响应test指令********************");
				WxMpXmlOutTextMessage m = WxMpXmlOutMessage.TEXT().content("成功收到测试指令").fromUser(wxMessage.getToUserName()).toUser(wxMessage.getFromUserName()).build();
				return m;
			}
		};
		wxMpMessageRouter.rule().async(false).content("test").handler(handler).end();
	}

	// 创建菜单
	private void buildMenu() {
		List<WxMenuButton> x5Meuns = new ArrayList<WxMenuButton>();

		WxMenuButton indexPage = new WxMenuButton();
		indexPage.setName("起步科技");
		indexPage.setType(WxConsts.BUTTON_VIEW);
		indexPage.setUrl("http://www.justep.com");
		x5Meuns.add(indexPage);

		WxMenuButton demoPage = new WxMenuButton();
		demoPage.setName("你好");
		demoPage.setType(WxConsts.BUTTON_VIEW);
		demoPage.setUrl("http://x5.justep.com/x5/UI2/takeout/index.w"); 
		x5Meuns.add(demoPage);

		WxMenuButton takeout = new WxMenuButton();
		takeout.setName("外卖案例");
		takeout.setType(WxConsts.BUTTON_VIEW);
		takeout.setUrl("https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + this.getWxMpConfigStorage().getAppId() + "&redirect_uri=http%3A%2F%2Fx5.justep.com%2Fx5%2FUI2%2Ftakeout%2Findex.w&"
				+ "response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect");
		System.out.println(takeout.getUrl());
		x5Meuns.add(takeout);

		WxMenu x5Menu = new WxMenu();
		x5Menu.setButtons(x5Meuns);
		try {
			wxMpService.menuDelete();
			
			wxMpService.menuCreate(x5Menu);
		} catch (WxErrorException e) {
			e.printStackTrace();
		}
	}

	// 接收到menu指令后重构菜单
	private void addMenuRouter() {
		WxMpMessageHandler handler = new WxMpMessageHandler() {
			@Override
			public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage, Map<String, Object> context, WxMpService wxMpService, WxSessionManager sessionManager) throws WxErrorException {
				System.out.println("响应menu指令********************");
				buildMenu();
				WxMpXmlOutTextMessage m = WxMpXmlOutMessage.TEXT().content("菜单已重构").fromUser(wxMessage.getToUserName()).toUser(wxMessage.getFromUserName()).build();
				return m;
			}
		};
		// 拦截内容为menu的消息
		wxMpMessageRouter.rule().async(false).content("menu").handler(handler).end();
	}
	// 下载图片
	public File downloadImage(String s) throws WxErrorException{
		//9PxNKlEKWXokU5QpHcyVKxe8-CxMlLlOClQmM6iwicnkttNuP4VDv0YMBPvzxm83
		File f = wxMpService.mediaDownload(s);
		System.out.println(f.toPath());
		return f;
	}
	public void doResponse(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String signature = request.getParameter("signature");
		String nonce = request.getParameter("nonce");
		String timestamp = request.getParameter("timestamp");

		response.setContentType("text/html;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);

		if (!wxMpService.checkSignature(timestamp, nonce, signature)) {
			// 消息签名不正确，说明不是公众平台发过来的消息
			response.getWriter().println("非法请求");
			return;
		}

		String echostr = request.getParameter("echostr");
		if (StringUtils.isNotBlank(echostr)) {
			// 说明是一个仅仅用来验证的请求，回显echostr
			response.getWriter().println(echostr);
			return;
		}

		String encryptType = StringUtils.isBlank(request.getParameter("encrypt_type")) ? "raw" : request.getParameter("encrypt_type");

		WxMpXmlMessage inMessage = null;

		if ("raw".equals(encryptType)) {
			// 明文传输的消息
			inMessage = WxMpXmlMessage.fromXml(request.getInputStream());
		} else if ("aes".equals(encryptType)) {
			// 是aes加密的消息
			String msgSignature = request.getParameter("msg_signature");
			inMessage = WxMpXmlMessage.fromEncryptedXml(request.getInputStream(), wxMpConfigStorage, timestamp, nonce, msgSignature);
		} else {
			response.getWriter().println("不可识别的加密类型");
			return;
		}

		WxMpXmlOutMessage outMessage = wxMpMessageRouter.route(inMessage);
		if (outMessage != null) {
			if ("raw".equals(encryptType)) {
				response.getWriter().write(outMessage.toXml());
			} else if ("aes".equals(encryptType)) {
				response.getWriter().write(outMessage.toEncryptedXml(wxMpConfigStorage));
			}
			return;
		}
	}
}
