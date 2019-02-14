package com.hyls.utils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateUtils {
private static transient int gregorianCutoverYear = 1582;
    
    /** 闰年中每月天数 */
    private static final int[] DAYS_P_MONTH_LY= {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    
    /** 非闰年中每月天数 */
    private static final int[] DAYS_P_MONTH_CY= {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    
    /** 代表数组里的年、月、日 */
    private static final int Y = 0, M = 1, D = 2;
    
    /**
    * 将代表日期的字符串分割为代表年月日的整形数组
    * @param date
    * @return
    */
    public static int[] splitYMD(String date){
        date = date.replace("-", "");
        int[] ymd = {0, 0, 0};
        ymd[Y] = Integer.parseInt(date.substring(0, 4));
        ymd[M] = Integer.parseInt(date.substring(4, 6));
        ymd[D] = Integer.parseInt(date.substring(6, 8));
        return ymd;
    }
    
    /**
    * 检查传入的参数代表的年份是否为闰年
    * @param year
    * @return
    */
    public static boolean isLeapYear(int year) {
        return year >= gregorianCutoverYear ?
            ((year%4 == 0) && ((year%100 != 0) || (year%400 == 0))) : (year%4 == 0); 
    }
      
    /**
    * 日期加1天
    * @param year
    * @param month
    * @param day
    * @return
    */
    private static int[] addOneDay(int year, int month, int day){
        if(isLeapYear( year )){
            day++;
            if( day > DAYS_P_MONTH_LY[month -1 ] ){
                month++;
                if(month > 12){
                    year++;
                    month = 1;
                }
                day = 1;
            }
        }else{
            day++;
            if( day > DAYS_P_MONTH_CY[month -1 ] ){
                month++;
                if(month > 12){
                    year++;
                    month = 1;
                }
                day = 1;
            }
        }
        int[] ymd = {year, month, day};
        return ymd;
    }
    
    /**
    * 将不足两位的月份或日期补足为两位
    * @param decimal
    * @return
    */
    public static String formatMonthDay(int decimal){
        DecimalFormat df = new DecimalFormat("00");
        return df.format( decimal );
    }
    
    /**
    * 将不足四位的年份补足为四位
    * @param decimal
    * @return
    */
    public static String formatYear(int decimal){
        DecimalFormat df = new DecimalFormat("0000");
        return df.format( decimal );
    }
    
    public static Date stringToDate(String date,String format) {
    	SimpleDateFormat sdf = new SimpleDateFormat(format);
    	try {
			return sdf.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }
    
    public static Date addDate(Date date, int days) {
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(date);
    	calendar.add(Calendar.DAY_OF_YEAR, days);
    	return calendar.getTime();
    }
    
    
    public static String dateToString(Date date,String format) {
    	SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
    	return simpleDateFormat.format(date);
    }
    /**
    * 计算两个日期之间相隔的天数
    * @param begin
    * @param end
    * @return
    * @throws ParseException
    */
    public static long countDay(String begin,String end){
           SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
           Date beginDate , endDate;
           long day = 0;
           try {
            beginDate= format.parse(begin);
            endDate=  format.parse(end);
            day=(endDate.getTime()-beginDate.getTime())/(24*60*60*1000);  
        } catch (ParseException e) {
            e.printStackTrace();
        }                
           return day;
    }
    
    /**
    * 以循环的方式计算日期
    * @param beginDate endDate
    * @param days
    * @return
    */
    public static List<String> getEveryday(String beginDate , String endDate){
        long days = countDay(beginDate, endDate);
        int[] ymd = splitYMD( beginDate );
        List<String> everyDays = new ArrayList<String>();
        everyDays.add(beginDate);
        for(int i = 0; i < days; i++){
            ymd = addOneDay(ymd[Y], ymd[M], ymd[D]);
            everyDays.add(formatYear(ymd[Y])+"-"+formatMonthDay(ymd[M])+"-"+formatMonthDay(ymd[D]));
        }
        return everyDays;
    }
    
    public static void main(String[] args) {
        /*List<String> list = DateUtils.getEveryday("2018-01-01", "2018-09-30");
        for (String result : list) {
            System.out.println(result);
        }*/
    	SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
    	String  beginTime = "20180130";
    	String endTime = "20180130";
    	System.out.println(beginTime.compareTo(endTime));
    	Date sd1;
		try {
			sd1 = df.parse(beginTime);
			Date sd2=df.parse(endTime);
			System.out.println(sd1.before(sd2));
	    	System.out.println(sd1.after(sd2));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	

    	

    	
    }
}
