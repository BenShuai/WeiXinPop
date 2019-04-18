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
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Api(value="/main",description="主动调微信接口类")
@RestController
@RequestMapping(value="/main")
public class mainController {
    //http://localhost:8085/swagger-ui.html

    @ApiOperation(value="获取参数信息", notes="获取参数信息")
    @RequestMapping(value="/getPop", method = RequestMethod.GET)
    public String getPop(HttpServletRequest request, HttpServletResponse response)  throws Exception{
        JSONObject jo=new JSONObject();
        jo.put("SuiteTicket",WeiXinPop.SuiteTicket);
        jo.put("suite_access_token",WeiXinPop.suite_access_token);
        jo.put("pre_auth_code",WeiXinPop.pre_auth_code);
        return jo.toJSONString();
    }

    @ApiOperation(value="获取user_ticket以及user详细信息", notes="获取user_ticket以及user详细信息")
    @RequestMapping(value="/getuserinfo3rd", method = RequestMethod.GET)
    public String getuserinfo3rd(@ApiParam(name = "code", value = "code", required = false) @RequestParam(required = false) String code,
                                 HttpServletRequest request, HttpServletResponse response)  throws Exception{
        String url="https://qyapi.weixin.qq.com/cgi-bin/service/getuserinfo3rd?access_token="+WeiXinPop.suite_access_token+"&code="+code;
        String result = ZwwxPostTencentUtil.getHttpClentsJson(url);

        JSONObject jo=JSONObject.parseObject(result);
        String UserId=jo.getString("UserId");
        String DeviceId=jo.getString("DeviceId");
        String user_ticket=jo.getString("user_ticket");

        //获得用户详细信息
        String url2="https://qyapi.weixin.qq.com/cgi-bin/service/getuserdetail3rd?access_token="+WeiXinPop.suite_access_token;
        JSONObject jo2=new JSONObject();
        jo2.put("user_ticket",user_ticket);
        String result2 = ZwwxPostTencentUtil.postHttpClentsJsonNoEntity(url2,jo2.toJSONString());

        return result2;
    }

}
