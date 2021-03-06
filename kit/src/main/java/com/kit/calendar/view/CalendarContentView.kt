package com.kit.calendar.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue.COMPLEX_UNIT_PX
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.Gson
import com.kit.calendar.bean.CalendarAttribute
import com.kit.calendar.bean.CalendarConstants
import com.kit.calendar.bean.DateInfo
import com.kit.calendar.bean.Festival
import com.kit.calendar.listener.DateItemClickListener
import com.kit.calendar.listener.DateSetListener
import com.kit.calendar.utils.CalendarUtils
import com.kit.guide.R
import com.kit.guide.utils.GuideViewUtils.px2dip
import com.kit.utils.L
import kotlinx.android.synthetic.main.calendar_view.view.*
import kotlinx.android.synthetic.main.calendar_view.view.calendarLineFive
import kotlinx.android.synthetic.main.calendar_view.view.calendarLineFour
import kotlinx.android.synthetic.main.calendar_view.view.calendarLineOne
import kotlinx.android.synthetic.main.calendar_view.view.calendarLineSix
import kotlinx.android.synthetic.main.calendar_view.view.calendarLineThree
import kotlinx.android.synthetic.main.calendar_view.view.calendarLineTwo
import kotlinx.android.synthetic.main.calendar_view_content.view.*
import kotlinx.android.synthetic.main.calendar_week.view.*
import java.lang.Exception
import java.util.*

/**
 * 日历的主体view
 * @author libowu
 */
class CalendarContentView : LinearLayout {

    var dateViewItem: MutableList<View>? = null
    var dateList: MutableList<DateInfo>? = null
    var cal: Calendar? = null
    var date: String? = null
    var clickListener: DateItemClickListener? = null
    var attribute: CalendarAttribute? = null
    var dateSetListener:DateSetListener ?= null
    var oldView: View? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context?, nothing: AttributeSet?) : this(context, nothing, 0)

    constructor(context: Context?, nothing: AttributeSet?, def: Int?) : super(context, nothing, 0) {
        cal = Calendar.getInstance()
        initView()
    }


    private fun initView() {
        LayoutInflater.from(context).inflate(R.layout.calendar_view_content, this, true)
        dateViewItem = mutableListOf()
        addItemView()
    }


    /**
     * 初始化时设置默认数据
     */
    private fun setDateData(parentView: LinearLayout, view: View, index: Int) {
        dateViewItem?.add(view)
        //Log.e("日志","获取的农历为："+ dateList?.get(index)?.lunar!![2])
        parentView.addView(view)
    }

    /**
     * 设置日期
     */
    fun setDate(date: String,dateSetListener: DateSetListener?) {
        this.dateSetListener = dateSetListener
        if (attribute!!.weekBarLayout == R.layout.calendar_week){
            for (i in 0..(calendarWeekBar.childCount - 1)) {
                if (attribute?.headWeekTextSize != 16) {
                    (calendarWeekBar.getChildAt(i) as TextView).setTextSize(COMPLEX_UNIT_PX, attribute?.headWeekTextSize!!.toFloat())
                    (calendarWeekBar.getChildAt(i) as TextView).setTextColor(attribute!!.headWeekTextColor)
                } else {
                    (calendarWeekBar.getChildAt(i) as TextView).setTextSize((attribute?.headWeekTextSize)!!.toFloat())
                    (calendarWeekBar.getChildAt(i) as TextView).setTextColor(attribute!!.headWeekTextColor)
                }
            }
        }

        dateViewItem?.let {
            this.date = date
            var dateResult = date.split("-")
            dateList = CalendarUtils.getDayOfMonthList(dateResult[0].toInt(), dateResult[1].toInt())
            for (item in dateViewItem!!.withIndex()) {
                //如果日期item是自定义view，则不需要执行下面的日期设置，设置都交由调用者来设置
                if (attribute?.dateItemLayout != R.layout.calendar_view_item_date){
                    try{
                        dateSetListener?.onDateSetListener(dateViewItem!!.get(item.index),dateList!!.get(item.index),dateList!!,item.index)
                        dateViewItem!!.get(item.index).setOnClickListener {
                           if (attribute!!.isEnableItemClick){
                               clickListener?.onDateItemClickListener(it,dateList!!.get(item.index),dateList!!,item.index,oldView)
                               oldView = it
                           }
                        }
                    }catch (e:Exception){
                        L.e(CalendarConstants.CALENDAR_L_TITLE,"reason============>${e.message}")
                    }
                    continue
                }
                var day = item.value.findViewById<TextView>(R.id.calendarDay)
                var festival = item.value.findViewById<TextView>(R.id.calendarFestivalOrLunar)
                day.setText("${dateList?.get(item.index)?.day}")
                festival.setText("${dateList?.get(item.index)?.lunar?._date}")

                if (attribute?.dateDayTextSize != 16) {
                    day.setTextSize(COMPLEX_UNIT_PX, attribute?.dateDayTextSize!!.toFloat())
                } else {
                    day.setTextSize(attribute!!.dateDayTextSize.toFloat())
                }
                if (attribute?.dateFestivalTextSize != 10) {
                    festival.setTextSize(COMPLEX_UNIT_PX, attribute!!.dateFestivalTextSize.toFloat())
                } else {
                    festival.setTextSize(attribute!!.dateFestivalTextSize.toFloat())
                }

                //设置字体颜色
                if (!dateList?.get(item.index)?.isCurrentMonth!!) {
                    day.setTextColor(attribute!!.notCurrentMonthDayTextColor!!)
                    festival.setTextColor(attribute!!.notCurrentMonthFestivalTextColor!!)
                } else {
                    day.setTextColor(attribute!!.currentMonthDayTextColor!!)
                    festival.setTextColor(attribute!!.currentMonthFestivalTextColor!!)
                    //是今天，则设置选中状态
                    if (dateList?.get(item.index)!!.year == cal?.get(Calendar.YEAR) && dateList?.get(item.index)!!.month == (cal?.get(Calendar.MONTH)!! + 1) && dateList!!.get(item.index).day == cal?.get(Calendar.DAY_OF_MONTH) && attribute!!.isSelectToday) {
                        day.setTextColor(attribute!!.selectTodayDayTextColor)
                        festival.setTextColor(attribute!!.selectTodayFestivalTextColor!!)
                        item.value.background = attribute!!.selectToayBackground
                    }
                }

                if (dateList!!.get(item.index).isHoliday(context) == CalendarView.Holiday.HOLIDAY) {
                    var holiday = item.value.findViewById<TextView>(R.id.calendarHolidayStatus)
                    holiday.setText("休")
                    holiday.setTextColor(attribute!!.holidayTipTextColor)
                    if (attribute!!.holidayTipTextSize.toFloat() == 8f) {
                        holiday.setTextSize(attribute!!.holidayTipTextSize.toFloat())
                    } else {
                        holiday.setTextSize(COMPLEX_UNIT_PX, attribute!!.holidayTipTextSize.toFloat())
                    }
                    holiday.visibility = View.VISIBLE
                } else if (dateList!!.get(item.index).isHoliday(context) == CalendarView.Holiday.WORK) {
                    var holiday = item.value.findViewById<TextView>(R.id.calendarHolidayStatus)
                    holiday.setText("班")
                    holiday.setTextColor(attribute!!.workDayTipTextColor)
                    if (attribute!!.holidayTipTextSize.toFloat() == 8f) {
                        holiday.setTextSize(attribute!!.holidayTipTextSize.toFloat())
                    } else {
                        holiday.setTextSize(COMPLEX_UNIT_PX, attribute!!.holidayTipTextSize.toFloat())
                    }
                    holiday.visibility = View.VISIBLE

                } else {
                    var holiday = item.value.findViewById<TextView>(R.id.calendarHolidayStatus)
                    holiday.setText("班")
                    holiday.setTextColor(attribute!!.workDayTipTextColor)
                    if (attribute!!.holidayTipTextSize.toFloat() == 8f) {
                        holiday.setTextSize(attribute!!.holidayTipTextSize.toFloat())
                    } else {
                        holiday.setTextSize(COMPLEX_UNIT_PX, attribute!!.holidayTipTextSize.toFloat())
                    }
                    holiday.visibility = View.GONE
                }
                item.value.setOnClickListener(OnClickListener {

                })

                item.value.setOnClickListener {
                    if (attribute!!.isEnableItemClick){
                        clickListener?.onDateItemClickListener(it, dateList!!.get(item.index), dateList!!, item.index,oldView)
                        oldView = item.value
                    }
                }

                setFestival(item.index, dateList, festival)

                //之所以头尾都设置这个，是应为尾部这个监听是给默认日期item使用的
                dateSetListener?.let {
                    dateSetListener!!.onDateSetListener(dateViewItem!!.get(item.index),dateList!!.get(item.index),dateList!!,item.index)
                }
            }
        }
    }




    /**
     * 添加日期item到view中，添加允许添加自定义的view
     */
    private fun addItemView() {
        //添加周栏
        if (CalendarView.Holiday.ATTRIBUTE?.weekBarLayout == R.layout.calendar_week){
            var weekBar = LayoutInflater.from(context).inflate(R.layout.calendar_week,this,false)
            calendarWeekBarBox.addView(weekBar)
        }else{
            var weekBar = LayoutInflater.from(context).inflate(CalendarView.Holiday.ATTRIBUTE!!.weekBarLayout,this,false)
            if (weekBar == null){
                weekBar = LayoutInflater.from(context).inflate(R.layout.calendar_week,this,false)
            }
            calendarWeekBarBox.addView(weekBar)
        }
        for (index in 0..6) {
            if (CalendarView.Holiday.ATTRIBUTE?.dateItemLayout == R.layout.calendar_view_item_date) {
                var view = LayoutInflater.from(context).inflate(R.layout.calendar_view_item_date, this, false)
                setDateData(calendarLineOne, view, index)
            } else {
                var view = LayoutInflater.from(context).inflate(CalendarView.Holiday.ATTRIBUTE!!.dateItemLayout, this, false)
               /* var llp = view.layoutParams as LinearLayout.LayoutParams
                llp.weight = 1f
                llp.width = 0
                view.layoutParams = llp*/
                calendarLineOne.addView(view)
                dateViewItem?.add(view)
            }
        }
        for (index in 0..6) {
            if (CalendarView.Holiday.ATTRIBUTE?.dateItemLayout == R.layout.calendar_view_item_date) {
                var view = LayoutInflater.from(context).inflate(R.layout.calendar_view_item_date, this, false)
                setDateData(calendarLineTwo, view, 6 + index)
            } else {
                 var view = LayoutInflater.from(context).inflate(CalendarView.Holiday.ATTRIBUTE!!.dateItemLayout, this, false)
             /*    var llp = view.layoutParams as LinearLayout.LayoutParams
                 llp.weight = 1f
                 llp.width = 0
                 view.layoutParams = llp*/
                 calendarLineTwo.addView(view)
                 dateViewItem?.add(view)
            }
        }
        for (index in 0..6) {
            if (CalendarView.Holiday.ATTRIBUTE?.dateItemLayout == R.layout.calendar_view_item_date) {
                var view = LayoutInflater.from(context).inflate(R.layout.calendar_view_item_date, this, false)
                setDateData(calendarLineThree, view, 12 + index)
            } else {
                var view = LayoutInflater.from(context).inflate(CalendarView.Holiday.ATTRIBUTE!!.dateItemLayout, this, false)
             /*   var llp = view.layoutParams as LinearLayout.LayoutParams
                llp.weight = 1f
                llp.width = 0
                view.layoutParams = llp*/
                calendarLineThree.addView(view)
                dateViewItem?.add(view)
            }
        }
        for (index in 0..6) {
            if (CalendarView.Holiday.ATTRIBUTE?.dateItemLayout == R.layout.calendar_view_item_date) {
                var view = LayoutInflater.from(context).inflate(R.layout.calendar_view_item_date, this, false)
                setDateData(calendarLineFour, view, 18 + index)
            } else {
                 var view = LayoutInflater.from(context).inflate(CalendarView.Holiday.ATTRIBUTE!!.dateItemLayout, this, false)
               /*  var llp = view.layoutParams as LinearLayout.LayoutParams
                 llp.weight = 1f
                 llp.width = 0
                 view.layoutParams = llp*/
                 calendarLineFour.addView(view)
                 dateViewItem?.add(view)
            }
        }
        for (index in 0..6) {
            if (CalendarView.Holiday.ATTRIBUTE?.dateItemLayout == R.layout.calendar_view_item_date) {
                var view = LayoutInflater.from(context).inflate(R.layout.calendar_view_item_date, this, false)
                setDateData(calendarLineFive, view, 24 + index)
            } else {
                 var view = LayoutInflater.from(context).inflate(CalendarView.Holiday.ATTRIBUTE!!.dateItemLayout, this, false)
               /*  var llp = view.layoutParams as LinearLayout.LayoutParams
                 llp.weight = 1f
                 llp.width = 0
                 view.layoutParams = llp*/
                 calendarLineFive.addView(view)
                 dateViewItem?.add(view)
            }

        }
        for (index in 0..6) {
            if (CalendarView.Holiday.ATTRIBUTE?.dateItemLayout == R.layout.calendar_view_item_date) {
                var view = LayoutInflater.from(context).inflate(R.layout.calendar_view_item_date, this, false)
                setDateData(calendarLineSix, view, 30 + index)
            } else {
                 var view = LayoutInflater.from(context).inflate(CalendarView.Holiday.ATTRIBUTE!!.dateItemLayout, this, false)
                /* var llp = view.layoutParams as LinearLayout.LayoutParams
                 llp.weight = 1f
                 llp.width = 0
                 view.layoutParams = llp*/
                 calendarLineSix.addView(view)
                 dateViewItem?.add(view)
            }
        }
    }

    /**
     * 设置节日或节气
     */
    fun setFestival(index: Int, dateList: MutableList<DateInfo>?, festival: TextView) {
        var item = dateList?.get(index)
        festival.setText(dateList?.get(index)?.lunar?._date)
        var  festivalResult = item?.getFesitval(context)
        //同一日期可能存在多个节日，优先获取数组的第一个节日
        if (festivalResult != null) {
            if (festivalResult.getImportantFestival() != null) {
                //是否存在简称，有则优先显示简称
                if (festivalResult.getImportantFestival()[0].contains("-")) {
                    festival.setText(festivalResult.getImportantFestival()[0].split("-")[0])
                } else {
                    festival.setText(festivalResult.getImportantFestival()[0])
                }
            }
            if (festivalResult.getLunarFestival() != null) {
                //是否存在简称，有则优先显示简称
                if (festivalResult.getLunarFestival()[0].contains("-")) {
                    festival.setText(festivalResult.getLunarFestival()[0].split("-")[0])
                } else {
                    festival.setText(festivalResult.getLunarFestival()[0])
                }
            }
            if (festivalResult.getSolaTerms() != null) {
                festival.setText(festivalResult.solaTerms.name)
            }
        }
    }

    //fun MutableList<View

}