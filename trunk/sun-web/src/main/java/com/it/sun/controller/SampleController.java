package com.it.sun.controller;

import com.alibaba.fastjson.JSONObject;
import com.it.sun.uitl.WeiXinPop;
import com.it.sun.uitl.ZwwxPostTencentUtil;
import com.qq.weixin.mp.aes.WXBizMsgCrypt;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Api(value="/Sample",description="微信主动回调类")
@RestController
@RequestMapping(value="/Sample")
public class SampleController {
    //http://localhost:8085/swagger-ui.html


    /**
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @ApiOperation(value="微信回调地址", notes="微信回调地址")
    @RequestMapping(value="/weixin", method = RequestMethod.GET)
    public String weixin(@ApiParam(name = "msg_signature", value = "企业微信加密签名", required = true) @RequestParam(required = true) String msg_signature,
                       @ApiParam(name = "timestamp", value = "时间戳", required = true) @RequestParam(required = true) String timestamp,
                       @ApiParam(name = "nonce", value = "随机数,与timestamp结合使用，用于防止请求重放攻击", required = true) @RequestParam(required = true) String nonce,
                       @ApiParam(name = "echostr", value = "加密的字符串", required = true) @RequestParam(required = true) String echostr,
                       HttpServletRequest request, HttpServletResponse response)  throws Exception{

        String sToken = WeiXinPop.sToken;
        String sCorpID = WeiXinPop.sCorpID;
        String sEncodingAESKey = WeiXinPop.sEncodingAESKey;

        WXBizMsgCrypt wxcpt = new WXBizMsgCrypt(sToken, sEncodingAESKey, sCorpID);

        JSONObject jo=new JSONObject();
        jo.put("msg_signature",msg_signature);
        jo.put("timestamp",timestamp);
        jo.put("nonce",nonce);
        jo.put("echostr",echostr);
        System.out.println();
        System.out.println(jo.toJSONString());
        System.out.println();

        String sEchoStr; //需要返回的明文
        try {
            sEchoStr = wxcpt.VerifyURL(msg_signature, timestamp,nonce, echostr);
            System.out.println();
            System.out.println("verifyurl echostr: " + sEchoStr);
            System.out.println();
            return sEchoStr;
        } catch (Exception e) {
            //验证URL失败，错误原因请查看异常
            e.printStackTrace();
        }
        return "success";
    }

    @ApiOperation(value="微信回调地址", notes="微信回调地址")
    @RequestMapping(value="/weixin", method = RequestMethod.POST)
    public void weixinzhilin(HttpServletRequest request, HttpServletResponse response)  throws Exception{
        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.print("success");
        } catch (Exception e) {
            //验证URL失败，错误原因请查看异常
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }




    @ApiOperation(value="微信指令回调地址", notes="微信指令回调地址")
    @ResponseBody
    @RequestMapping(value = "/qywxServer", method = RequestMethod.POST)
    public void qywxServerPOST(HttpServletRequest request, HttpServletResponse response){
        String sToken = WeiXinPop.sToken;
        String sID = WeiXinPop.sSuiteID;
        String sEncodingAESKey = WeiXinPop.sEncodingAESKey;
        String msg_signature = request.getParameter("msg_signature");
        String timestamp = request.getParameter("timestamp");
        String nonce = request.getParameter("nonce");
        //String echostr = request.getParameter("echostr");
        PrintWriter out = null;
        try {
            out = response.getWriter();
            WXBizMsgCrypt wxcpt = new WXBizMsgCrypt(sToken, sEncodingAESKey, sID);
            out.print("success");

            //从请求中读取整个post数据
            InputStream inputStream = request.getInputStream();
            String postData = IOUtils.toString(inputStream, "UTF-8");
            String sMsg = wxcpt.DecryptMsg(msg_signature, timestamp,nonce, postData);

            Document document = DocumentHelper.parseText(sMsg);
            Map<String,String> map = new HashMap<String, String>();
            Element root = document.getRootElement();
            List<Element> eles = root.elements();
            for(Element e:eles) {
                map.put(e.getName(), e.getTextTrim());
            }
            System.out.println();
            System.out.println("map:"+map.toString());
            System.out.println();

            if(StringUtils.isNotBlank(map.get("InfoType"))) {
                if (map.get("InfoType").toString().equals("suite_ticket")) {
                    String suiteTicket = map.get("SuiteTicket").toString();
                    WeiXinPop.SuiteTicket=suiteTicket;//得到suite_ticket

                    //开始获取suite_access_token
                    JSONObject jo=new JSONObject();
                    jo.put("suite_id",WeiXinPop.sSuiteID);
                    jo.put("suite_secret",WeiXinPop.sSecret);
                    jo.put("suite_ticket",suiteTicket);
                    String suite_access_tokenResult = ZwwxPostTencentUtil.postHttpClentsJsonNoEntity("https://qyapi.weixin.qq.com/cgi-bin/service/get_suite_token",jo.toJSONString());

                    JSONObject result = JSONObject.parseObject(suite_access_tokenResult);
                    String suiteAccessToken = result.getString("suite_access_token");
                    WeiXinPop.suite_access_token=suiteAccessToken;
                    System.out.println();
                    System.out.println("suiteAccessToken:"+suiteAccessToken);
                    System.out.println();

                    //开始获得 预授权码
                    String url="https://qyapi.weixin.qq.com/cgi-bin/service/get_pre_auth_code?suite_access_token="+WeiXinPop.suite_access_token;
                    String result2=ZwwxPostTencentUtil.getHttpClentsJson(url);
                    JSONObject jo2=JSONObject.parseObject(result2);
                    String pre_auth_code=jo2.getString("pre_auth_code");
                    WeiXinPop.pre_auth_code=pre_auth_code;

                } else if (map.get("InfoType").toString().equals("create_auth")) { //获取企业access_token
                    String authCode = map.get("AuthCode").toString();
                    System.out.println();
                    System.out.println("AuthCode:"+authCode);
                    System.out.println();

                }

            }

        } catch (Exception e) {
            //验证URL失败，错误原因请查看异常
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    @ApiOperation(value="微信指令回调地址", notes="微信指令回调地址")
    @ResponseBody
    @RequestMapping(value = "/qywxServer", method = RequestMethod.GET)
    public void qywxServerGet(HttpServletRequest request, HttpServletResponse response){
        String sToken = WeiXinPop.sToken;
        String sCorpID = WeiXinPop.sCorpID;
        String sEncodingAESKey = WeiXinPop.sEncodingAESKey;

        String msg_signature = request.getParameter("msg_signature");
        String timestamp = request.getParameter("timestamp");
        String nonce = request.getParameter("nonce");
        String echostr = request.getParameter("echostr");

        String sEchoStr = ""; //需要返回的明文
        PrintWriter out = null;
        try {
            out = response.getWriter();
            WXBizMsgCrypt wxcpt = new WXBizMsgCrypt(sToken, sEncodingAESKey, sCorpID);
            sEchoStr = wxcpt.VerifyURL(msg_signature, timestamp,nonce, echostr);
            System.out.println("qywxServer:"+sEchoStr+"\r\n");
            out.print(sEchoStr);
        } catch (Exception e) {
            //验证URL失败，错误原因请查看异常
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

}
