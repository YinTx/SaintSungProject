package com.saintsung.saintpmc.tool;


import android.os.Build;

import com.saintsung.saintpmc.configuration.MD5;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2016/4/8.
 */
public class DataProcess {
    /**
     * 对数据进行MD5加密
     * 服务器ID+补足后的用户名+补足后的密码+补足后的IMEI
     *
     * @param res
     * @return
     */
    public static String createRequestPacket(String res) {
        String request = res;
        String md5Crc = MD5.toMD5(request);
        request += md5Crc;
        int len = request.length();
        String lenStr = len + "";
        //数据报文前6位为长度位,指明其后数据内容的长度.
        lenStr = ComplementIMEI(lenStr, 6);
        request = lenStr + request;
        return request;
    }

    /**
     * 补足空格，用户名，密码
     *
     * @param rst
     * @return
     */
    public static String ComplementSpace(String rst) {
        String rtn = rst;
        for (int i = 0; i < 10 - rtn.length(); i++) {
            rst = " " + rst;
        }
        rtn = rst;
        return rtn;
    }

    /**
     * 补足空格
     *
     * @param rst
     * @return
     */
    public static String ComplementSpace2(String rst, int x) {
        String rtn = rst;
        for (int i = 0; i < x - rtn.length(); i++) {
            rst = rst + " ";
        }
        rtn = rst;
        return rtn;
    }

    /**
     * 补足零
     *
     * @param rst
     * @return
     */
    public static String ComplementZeor2(String rst, int x) {
        String rtn = rst;
        for (int i = 0; i < x - rtn.length(); i++) {
            rst = rst + "0";
        }
        rtn = rst;
        return rtn;
    }

    /**
     * 补足零
     *
     * @param rst
     * @return
     */
    public static String ComplementZeor(String rst, int x) {
        String rtn = rst;
        for (int i = 0; i < x - rtn.length(); i++) {
            rst = "0" + rst;
        }
        rtn = rst;
        return rtn;
    }

    /**
     * 补足IMEI
     *
     * @return
     */
    public static String ComplementIMEI(String rst, int len) {
        String rtn = rst;
        for (int i = 0; i < len - rtn.length(); i++) {
            rst = "0" + rst;
        }
        rtn = rst;
        return rtn;
    }

    public static boolean isMNC() {
        return Build.VERSION.SDK_INT >= 23;
    }

    /**
     * 将图片内容解析成字节数组.
     *
     * @param
     * @param inStream
     * @return byte[]
     * @throws Exception
     */
    public static byte[] readStream(InputStream inStream) throws Exception {
        byte[] buffer = new byte[1024];
        int len = -1;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        inStream.close();
        return data;

    }


    //RTN_FLAG	返回标志	VARCHAR2 (1)	1表示成功，0表示失败
    //RTN_ERRCD	错误代号	VARCHAR2 (4)	验证错误的错误代号，字母/数字
    //RTN_UID	成功登陆的用户ID	VARCHAR2(16)	由服务器返还的用户ID，供业务操作验证使用
    //LOGIN_TIME	登录时间	VARCHAR2(14)	成功则返回登录服务器时间YYYYMMDDhhmmss,24小时格式
    //SX_TIMES	无信号自动失效时间分钟	VARCHAR2(4)	数字(默认60分钟)
    //ZQ_TIMES	周期检查时间分钟	VARCHAR2(4)	数字(默认15分钟)
    //数据长度+数据+MD5检验码
    public static boolean getLoginReturn(String res) {
        if (res.substring(10, 11).equals("0")) {
            return false;
        } else
            return true;
    }

    /**
     * 判断时间是否在时间段内
     *
     * @param date         当前时间 yyyy-MM-dd HH:mm:ss
     * @param strDateBegin 开始时间 00:00:00
     * @param strDateEnd   结束时间 00:05:00
     * @return
     */
    public static boolean isInDate(String date, String strDateBegin,
                                   String strDateEnd) {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date starDate = null;
        Date newDate=null;
        Date endDate=null;
        try {
            starDate = sdf.parse(strDateBegin);
             newDate = sdf.parse(date);
            endDate= sdf.parse(strDateEnd);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (newDate.before(starDate) && endDate.after(endDate) ) {
            return true;
        } else {
            return false;
        }
    }

    public static String byte2hex(byte[] b) // 二进制转字符串
    {
        StringBuffer sb = new StringBuffer();
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1) {
                sb.append("0" + stmp);
            } else {
                sb.append(stmp);
            }

        }
        return sb.toString();
    }

    /**
     * 数组去空
     *
     * @param strArray
     * @return
     */
    public static String[] removeArrayEmptyTextBackNewArray(String[] strArray) {
        List<String> strList = Arrays.asList(strArray);
        List<String> strListNew = new ArrayList<>();
        for (int i = 0; i < strList.size(); i++) {
            if (strList.get(i) != null && !strList.get(i).equals("")) {
                strListNew.add(strList.get(i));
            }
        }
        String[] strNewArray = strListNew.toArray(new String[strListNew.size()]);
        return strNewArray;
    }

    /**
     * @param @param  date
     * @param @param  strDateBegin
     * @param @param  strDateEnd
     * @param @return 设定文件
     * @return boolean    返回类型
     * @throws
     * @Title: isInDate
     * @Description: 判断一个时间段（yyyy-MM-dd HH:mm:ss）是否在一个区间
     */
    public static boolean isInDate(Date date, String strDateBegin, String strDateEnd) {
        Date tempDateBegin = null;
        Date tempDateEnd = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        String strDate = sdf.format(date);   //2017-04-11
        try {
            tempDateBegin = sdf.parse(strDateBegin);
            tempDateEnd = sdf.parse(strDateEnd);
            date = sdf.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date.after(tempDateBegin) && date.before(tempDateEnd)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 传入时间，在传入的时间上增加 upData 月
     * 格式必须为yyyy-MM-dd HH:mm:ss
     * @param initData 要增加时间的初始时间
     * @param upData 需要增加的月份
     * @return
     */
    public static Date getTimeAdd(String initData, int upData) {
        Date newTime = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(initData));
            calendar.add(Calendar.MINUTE, upData);
            newTime = calendar.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newTime;
    }
}
