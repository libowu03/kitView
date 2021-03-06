package com.kit.calendar.bean;

import android.content.Context;

import com.kit.calendar.utils.CalendarUtils;
import com.kit.calendar.utils.Lunar;
import com.kit.calendar.utils.SolaTermsUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * 日期信息
 *
 * @author libowu
 */
public class DateInfo {
    /**
     * 天
     */
    private int day;
    /**
     * 月
     */
    private int month;
    /**
     * 年
     */
    private int year;
    /**
     * 是不是当前月份的日期
     */
    private boolean isCurrentMonth;
    /**
     * 周
     */
    private int week;
    /**
     * 节日信息,里面包含：要显示的节日，农历节日，有但不现实的节日，节气
     */
    private Festival festivalInfo;
    /**
     * 农历信息
     */
    private Lunar lunar;

    public DateInfo(int day, int month, int year, boolean isCurrentMonth, int week) {
        this.day = day;
        this.month = month;
        this.isCurrentMonth = isCurrentMonth;
        this.week = week;
        this.year = year;
        if (getCalendar().get(Calendar.YEAR) >= 2050 || getCalendar().get(Calendar.YEAR) <= 1900) {
            this.lunar = null;
        } else {
            this.lunar = new Lunar(getCalendar());
        }
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }


    public boolean isCurrentMonth() {
        return isCurrentMonth;
    }

    public void setCurrentMonth(boolean currentMonth) {
        isCurrentMonth = currentMonth;
    }


    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }


    public Lunar getLunar() {
        return lunar;
    }

    public int isHoliday() {
        return CalendarUtils.isHoliday(year, month, day);
    }

    /**
     * 获取日期是不是节假日
     *
     * @return 返回类型，可能返回下面三个中的一个：
     * {@link com.kit.calendar.view.CalendarView.Holiday.HOLIDAY},
     * {@link com.kit.calendar.view.CalendarView.Holiday.WORK},
     * {@link com.kit.calendar.view.CalendarView.Holiday.COMMON_DAY}
     */
    public int isHoliday(Context application) {
        return CalendarUtils.isHoliday(year, month, day, application);
    }

    /**
     * 获取周的中文
     *
     * @return 放回数组，数组中存放3个值，比如：星期日,周日,日
     */
    public String[] getWeekCn() {
        if (CalendarUtils.weekCn != null && CalendarUtils.weekCn.get(week) != null) {
            return CalendarUtils.weekCn.get(week).split(",");
        }
        return null;
    }

    /**
     * 获取节日
     *
     * @return 节日的对象
     */
    public Festival getFesitval() {
        return getFesitval(null);
    }

    public Festival getFesitval(Context application) {
        if (festivalInfo != null) {
            return festivalInfo;
        }
        String monthStr = null;
        String dayStr;
        String lunarStr = null;
        String lunarMonthStr = null;
        if (month < 10) {
            monthStr = "0" + month;
        } else {
            monthStr = String.valueOf(month);
        }
        if (day < 10) {
            dayStr = "0" + day;
        } else {
            dayStr = String.valueOf(day);
        }

        if (lunar != null) {
            if (lunar.getDay() < 10) {
                lunarStr = "0" + lunar.getDay();
            } else {
                lunarStr = String.valueOf(lunar.getDay());
            }
            if (lunar.getMonth() < 10) {
                lunarMonthStr = "0" + lunar.getMonth();
            } else {
                lunarMonthStr = String.valueOf(lunar.getMonth());
            }
        }

        HashMap<String, String> festivalMap = CalendarUtils.getFestivalMap(application);
        if (festivalMap == null) {
            return null;
        } else {
            Festival festival = new Festival();
            //获取农历节日
            if (lunar != null) {
                String lunarFestival = festivalMap.get("N" + lunarMonthStr + lunarStr);
                if (lunarFestival != null) {
                    String[] lunarFestivalResult = lunarFestival.split(",");
                    festival.setLunarFestival(lunarFestivalResult);
                }
            }
            //获取要显示的节日
            String commentFestival = festivalMap.get("H" + monthStr + dayStr);
            if (commentFestival != null) {
                String[] commentFestivalResult = commentFestival.split(",");
                festival.setImportantFestival(commentFestivalResult);
            }
            //获取其他节日，即存在的节日，但存在感不是很强的节日
            String otherFestival = festivalMap.get("L" + monthStr + dayStr);
            if (otherFestival != null) {
                String[] otherFestivalResult = otherFestival.split(",");
                festival.setOtherFestival(otherFestivalResult);
            }

            //获取二十四节气
            if (year <= 2100 && year >= 1900) {
                SolaTerms solaTerms = SolaTermsUtils.getSolarTerms(year, month, day);
                festival.setSolaTerms(solaTerms);
                festivalInfo = festival;
            }

            return festival;
        }
    }

    /**
     * 获取calendar
     *
     * @return
     */
    public Calendar getCalendar() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = format.parse(getDateStr());
        } catch (ParseException e) {
            return null;
        }
        calendar.setTime(date);
        return calendar;
    }

    /**
     * 获取时间戳,输出单位为毫秒
     *
     * @return
     */
    public long getTimeMillis() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = format.parse(getDateStr());
        } catch (ParseException e) {
            return -1;
        }
        return date.getTime();
    }

    /**
     * 获取date
     *
     * @return
     */
    public Date getDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = format.parse(getDateStr());
        } catch (ParseException e) {
            return null;
        }
        return date;
    }

    /**
     * 获取yyyy-MM-dd格式的时间
     *
     * @return
     */
    private String getDateStr() {
        String month;
        String day;
        if (this.month < 10) {
            month = "0" + this.month;
        } else {
            month = String.valueOf(this.month);
        }
        if (this.day < 10) {
            day = "0" + this.day;
        } else {
            day = String.valueOf(this.day);
        }
        return year + "-" + month + "-" + day;
    }
}
