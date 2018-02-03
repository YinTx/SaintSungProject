package com.saintsung.saintpmc.asynctask;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.saintsung.saintpmc.lock.FileStream;

import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;


/**
 * Created by XLzY on 2017/9/6.
 */

public class QueryAddressTask extends AsyncTask<String, Integer, String> {
    private String result;
    public static final String serviceNameSpace = "http://server.webservice.core.epm";
    //172.10.0.100:4011
    public static final String serviceUrl = "http://172.10.0.100:4021/web/services/GenericServer?wsdl";//营销前置机地址
    public static final String soapAction = "";
    public static final String operationName = "invoke";
    private String path = "epm/am/smartlock/interfaces/service/SmartLockInterfacesService";
    private String getLockspath = "epm/am/smartlock/interfaces/service/GetLocksInfoService";

    public resultService getResultStr() {
        return resultStr;
    }

    public void setResultStr(resultService resultStr) {
        this.resultStr = resultStr;
    }

    public resultService resultStr;

    public interface resultService {
        public void getResult(String res);
    }

    public String sendSoapStr(String methodStr, String xmlStr) {
        String method = methodStr;
        String result = "请求超时";
        try {
            //创建Soap报文的Body
            SoapObject sBody = new SoapObject(serviceNameSpace, operationName);
            //传递给invoke方法的3个参数
            sBody.addProperty("path", path);
            sBody.addProperty("methodName", method);
            sBody.addProperty("dataXmlStr", xmlStr);

            //创建Soap报文的Head
            Element[] sHead = new Element[2];
            sHead[0] = new Element().createElement("Authorization", "username");
            sHead[0].addChild(Node.TEXT, "ZNSJK");
            sHead[1] = new Element().createElement("Authorization", "password");
            sHead[1].addChild(Node.TEXT, "znsjk1");

            //创建Soap报文，使用版本12
            SoapSerializationEnvelope ssEnv = new SoapSerializationEnvelope(SoapSerializationEnvelope.VER11);
            //把报文的头指定成sHead,把报文体指定成sBody
            ssEnv.bodyOut = sBody;
            ssEnv.headerOut = sHead;

            ssEnv.dotNet = false;//由于是.net开发的webservice，所以这里要设置为true

            HttpTransportSE httpTransportSE = new HttpTransportSE(serviceUrl);
            httpTransportSE.call(soapAction, ssEnv);//调用

            // 获取返回的数据
            SoapObject object = (SoapObject) ssEnv.bodyIn;
            // 获取返回的结果


            result = object.getProperty(0).toString();
            Log.d("debug", result);
/*
            // 通过getProperty方法获得返回对象的属性值
            result = "结果标记：" + object.getProperty("RTN_FLAG") + "\n";
            result += "登录时间：" + object.getProperty("LOGIN_TIME") + "\n";
            result += "错误代码：" + object.getProperty("RTN_ERRCD") + "\n";
            result += "错误描述：" + object.getProperty("RTN_MSG") + "\n";
*/

        } catch (Exception e) {
            e.printStackTrace();
            FileStream fileStream = new FileStream();
            fileStream.fileStream(FileStream.log, FileStream.write, e.getMessage().toString().getBytes());
//            result = e.getMessage();
        }
        return result;
    }


    @Override
    protected String doInBackground(String... params) {
        String xmlStr = null;
        String methodStr = null;
        try {
            /******登陆开始*******/
            if (params[0].equals("GET_USER_LOGIN")) {
                //备注用户名默认需要转成大写

                xmlStr =
                        "<?xml version='1.0' encoding='UTF-8'?>" +
                                "<DBSET RESULT=\"1\">" +
                                "<R>" +
                                "<C N=\"USER_PWD\">" + params[2] + "</C>" +
                                "<C N=\"USER_NO\">" + params[1] + "</C>" +
                                "</R>" +
                                "</DBSET>";
                methodStr = params[0];

//                result = "<?xml version='1.0' encoding='UTF-8'?>" +
//                        "<DBSET RESULT=\"1\">" +
//                        "<R>" +
//                        "<C N=\"RTN_MSG\">成功</C>" +
//                        "<C N=\"RTN_ERRCD\"></C>" +
//                        "<C N=\"LOGIN_TIME\">2017-09-01 13:56:24</C>" +
//                        "<C N=\"RTN_FLAG\">1</C>" +
//                        "</R>" +
//                        "</DBSET>";
                /*******登陆结束******/
            }
            /*******获取单个锁权限开始****OPT_TYPE=1表示开锁，=0表示关锁**/
            if (params[0].equals("GET_LOCK_INFO")) {
                xmlStr =
                        "<?xml version='1.0' encoding='UTF-8'?>" +
                                "<DBSET RESULT=\"1\">" +
                                "<R>" +
                                "<C N=\"OPT_TYPE\">" + params[3] + "</C>" +
                                "<C N=\"LOCK_SN\">" + params[2] + "</C>" +
                                "<C N=\"USER_NO\">" + params[1] + "</C>" +
                                "</R>" +
                                "</DBSET>";

                methodStr = params[0];
//                result = "<?xml version='1.0' encoding='UTF-8'?>" +
//                        "<DBSET RESULT=\"1\">" +
//                        "<R>" +
//                        "<C N=\"OPT_PWD\">135050170155160</C>" +
//                        "<C N=\"RTN_MSG\">成功</C>" +
//                        "<C N=\"RTN_ERRCD\"></C>" +
//                        "<C N=\"LOCK_SN\">287097872</C>" +
//                        "<C N=\"RTN_FLAG\">1</C>" +
//                        "</R>" +
//                        "</DBSET>";
//                result = "<?xml version='1.0' encoding='UTF-8'?>" +
//                        "<DBSET RESULT=\"1\">" +
//                        "<R>" +
//                        "<C N=\"OPT_PWD\">301141063156193</C>" +
//                        "<C N=\"RTN_MSG\">成功</C>" +
//                        "<C N=\"RTN_ERRCD\"></C>" +
//                        "<C N=\"LOCK_SN\">283657166</C>" +
//                        "<C N=\"RTN_FLAG\">1</C>" +
//                        "</R>" +
//                        "</DBSET>";
            }
/*******获取单个锁权限结束******/
            if (params[0].equals("GET_LOCKS_INFO")) {
/*******获取批量权限开始******开锁的时候发现数据中没有，通过这个接口可以获取锁号所在台区下所有有权限的锁号**/
                 methodStr = "GET_LOCKS_INFO";
                 xmlStr ="<?xml version='1.0' encoding='UTF-8'?>"+
                                    "<DBSET RESULT=\"1\">"+
                                    "<R>"+
                                    "<C N=\"LOCK_SN\">287134962</C>"+
                                    "<C N=\"USER_NO\">8605314116A</C>"+
                                    "</R>"+
                                    "</DBSET>";
                path=getLockspath;//访问路径和其他的不一样
/*******获取批量权限结束******从数据库中获取一个锁号，通过这个接口发现没有权限则情况本地数据库(定时执行)**/
            }
/******上次历史记录开始**************/
//                String xmlStr="";
//                String xmlStrHead=
//
//                "<?xml version='1.0' encoding='UTF-8'?>"+
//                                "<DBSET RESULT=\"1\">";
//                String xmlStrEnd=
//                        "</DBSET>";
//                String  xml =
//                        "<R>"+
//                                "<C N=\"USER_NO\">8605314116A</C>"+
//                                "<C N=\"LOCK_SN\">278544710</C>"+
//                                "<C N=\"OPT_TIME\">2017-09-04 13:51:00</C>"+
//                                "<C N=\"OPT_TYPE\">1</C>"+
//                                "</R>";
//                xml +=
//                        "<R>"+
//                                "<C N=\"USER_NO\">8605314116A</C>"+
//                                "<C N=\"LOCK_SN\">278544710</C>"+
//                                "<C N=\"OPT_TIME\">2017-09-04 13:51:50</C>"+
//                                "<C N=\"OPT_TYPE\">0</C>"+
//                                "</R>";
//                xmlStr=xmlStrHead+xml+xmlStrEnd;
//
//                methodStr = "SET_LOCKS_OPTHIS";
/******上次历史记录结束**************/


            result = sendSoapStr(methodStr, xmlStr);


        } catch (Exception e) {

            e.printStackTrace();
        }
        //将结果返回给onPostExecute方法
        return result;
    }

    @Override
    //此方法可以在主线程改变UI
    protected void onPostExecute(String result) {
        // 将WebService返回的结果显示在TextView中
//            queryButton.setEnabled(true);
//            resultView.setText(result);
        //利用接口回调将服务器返回的数据发送出去
        resultStr.getResult(result);
    }
}
