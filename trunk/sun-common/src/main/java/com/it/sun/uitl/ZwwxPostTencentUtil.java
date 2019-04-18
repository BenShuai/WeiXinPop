package com.it.sun.uitl;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.PostMethod;

public class ZwwxPostTencentUtil {
    private static String encoding = "UTF-8";

    /**
     * @param conn 通过get方式获取StringBuffer(内部方法)
     * @return
     */
    private static StringBuffer getJsonString(URLConnection conn) {
        InputStreamReader isr = null;
        BufferedReader br = null;
        StringBuffer sb = null;
        try {
            isr = new InputStreamReader(conn.getInputStream(), encoding);
            br = new BufferedReader(isr);
            String line = null;
            sb = new StringBuffer();
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\r\n");
            }
        } catch (Exception e) {
            System.out.println("读取流异常");
        } finally {
            try {
                if (isr != null)
                    isr.close();
            } catch (IOException e) {
                System.out.println("流关闭异常");
            }
        }
        return sb;
    }

    /**
     * 调用远端接口返回数据(无参数)
     *
     * @param urlStr 远端数据接口地址
     * @return
     */
    public static String getHttpClentsJson(String urlStr){
        try{
            URL u = new URL(urlStr);
            URLConnection conn = u.openConnection();// 打开网页链接
            //返回结果
            String cloudJson = getJsonString(conn).toString();
            return cloudJson;
        }catch (Exception e) {
            return "请求或数据接口出现错误";
        }

    }


    /**
     * 有参数的httpClent请求(POST)
     * @param urlStr    请求地址
     * @param params    参数[Map类型] Map<String,Object>
     * @return  请求结果
     */
    public static String postHttpClentsJson(String urlStr,Map<String,Object> params){
        HttpClient clients = new HttpClient();
        clients.getParams().setAuthenticationPreemptive(true);//使用抢先认证

        PostMethod connPost = new PostMethod();
        try{
            URI u=new URI(urlStr);
            connPost.setURI(u);
            if(params!=null && params.size()>0){
                for (String key : params.keySet()) {
                    connPost.setParameter(key, params.get(key).toString());
                    connPost.getParams().setContentCharset(encoding);//参数转码
                }
            }
            clients.getHttpConnectionManager().getParams().setConnectionTimeout(20000);//默认20秒链接超时
            clients.getHttpConnectionManager().getParams().setSoTimeout(20000);//默认20秒读取超时
            int status = clients.executeMethod(connPost);
            if(status==200){
                BufferedReader buReader = new BufferedReader(new InputStreamReader(connPost.getResponseBodyAsStream(),encoding));
                StringBuffer cloudJson = new StringBuffer();
                String line;
                while((line=buReader.readLine())!=null){
                    cloudJson.append(line);
                }
                buReader.close();
                return cloudJson.toString();
            }else{
                return "请求或数据接口出现错误";
            }

        }catch (Exception e) {
            return "请求或数据接口出现错误";
        }finally {
            //释放掉HTTP连接
            connPost.releaseConnection();
            clients.getHttpConnectionManager().closeIdleConnections(0);
        }
    }


    /**
     * 没有参数名，只有参数值的httpClent请求(POST)
     *
     * @param urlStr 请求地址
     * @param params 参数[JSONArray类型(数据格式模拟：/---[{"key":"username","value":"sunshuai"},{"key","password","value":"123456"}]---/ )]
     * @return 结果
     */

    public static String postHttpClentsJsonNoEntity(String urlStr,String params){
        HttpClient clients = new HttpClient();
        PostMethod connPost = new PostMethod();
        try{
            URI u=new URI(urlStr);
            connPost.setURI(u);
            if(params!=null && !params.equals("")){
                connPost.setRequestBody(params);
            }
            clients.getHttpConnectionManager().getParams().setConnectionTimeout(20000);//默认20秒链接超时
            clients.getHttpConnectionManager().getParams().setSoTimeout(20000);//默认20秒读取超时
            int status = clients.executeMethod(connPost);
            if(status==200){
                BufferedReader buReader = new BufferedReader(new InputStreamReader(connPost.getResponseBodyAsStream(),encoding));
                StringBuffer cloudJson = new StringBuffer();
                String line;
                while((line=buReader.readLine())!=null){
                    cloudJson.append(line);
                }
                buReader.close();
                return cloudJson.toString();
            }else{
                return "请求或数据接口出现错误";
            }

        }catch (Exception e) {
            return "请求或数据接口出现错误";
        }finally {
            //释放掉HTTP连接
            connPost.releaseConnection();
            clients.getHttpConnectionManager().closeIdleConnections(0);
        }
    }

}
