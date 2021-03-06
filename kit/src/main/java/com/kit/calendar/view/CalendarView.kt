package com.kit.calendar.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.kit.calendar.adapter.CalendarRecAdapter
import com.kit.calendar.bean.CalendarAttribute
import com.kit.calendar.bean.CalendarConstants.*
import com.kit.calendar.bean.DateInfo
import com.kit.calendar.listener.DateItemClickListener
import com.kit.calendar.listener.DatePagerChangeListener
import com.kit.calendar.listener.DateSetListener
import com.kit.calendar.listener.PagerListener
import com.kit.calendar.utils.CalendarUtils
import com.kit.guide.R
import com.kit.guide.utils.GuideViewUtils
import kotlinx.android.synthetic.main.calendar_foot.view.*
import kotlinx.android.synthetic.main.calendar_head.view.*
import kotlinx.android.synthetic.main.calendar_view.view.*
import kotlinx.android.synthetic.main.calendar_week.view.*
import java.lang.Exception
import java.util.*

/**
 * 日历控件
 * @author libowu
 * @date 2019/12/22
 * 冬至撸代码，别有一番风味
 */
class CalendarView : LinearLayout, View.OnClickListener {
    private var manager: androidx.recyclerview.widget.LinearLayoutManager?= null
    private lateinit var pager: androidx.recyclerview.widget.PagerSnapHelper
    private lateinit var adapter: CalendarRecAdapter
    private var footLayout: Int = 0
    private var headLayout: Int = 0
    private var dateViewItem: MutableList<View>? = null
    //当前年份
    private var currentYear: Int = 0
    //当前月份
    private var currentMonth: Int = 0
    //当前日期
    private var currentDay: Int = 0
    //当前日期，初始化后就确定为今天所在的月份了，后面不会再变动。
    private var todayMonth: Int = 0
    //点击监听
    private var dateItemClickListener: OnDateItemClickListener? = null
    //日期信息，里面记录了年，月，日，农历，节日，是否是假期等信息。
    private var dateList: MutableList<DateInfo>? = null
    //是否默认选中今天
    private var selectToday: Boolean = true
    //上一次点击的view
    private var oldDateItem: View? = null
    //今天的日期
    private var todayDateInfo:DateInfo ?= null
    //是否执行过自动设置字体大小的步骤了
    private var isAuthorSetTextSize : Boolean = false
    //今天的日期
    private var cal:Calendar ?= null
    //比较合适的屏幕尺寸
    private val SUITABLE_WIDTH : Float= 1080f
    //合适的高度
    private val SUITABLE_HEIGHT : Float = 1313f
    private var enableCalendarScroll : Boolean = true


    //日期的文字大小
    private var dateDayTextSize: Int = 0
    //日期下面的节日或农历文字大小
    private var dateFestivalTextSize: Int = 0
    //非当前月份日期的文字颜色，此日历插件分为三个部分，前面部分为上个月日期，当前日期和下一个月的日期
    private var notCurrentMonthDayTextColor: Int = 0
    //非当前月份农历或节日的文字颜色，此日历插件分为三个部分，前面部分为上个月日期，当前日期和下一个月的日期
    private var notCurrentMonthFestivalTextColor: Int = 0
    //当前月份日期的文字颜色，此日历插件分为三个部分，前面部分为上个月日期，当前日期和下一个月的日期
    private var currentMonthDayTextColor: Int = 0
    //非当前月份农历或节日的文字颜色，此日历插件分为三个部分，前面部分为上个月日期，当前日期和下一个月的日期
    private var currentMonthFestivalTextColor: Int = 0
    //日历的顶部周一至周日的字体颜色
    private var headWeekTextColor: Int = 0
    //日历顶部周一至周六的字体大小
    private var headWeekTextSize: Int = 0
    //是否打开对头部的支持
    private var enableHeadLayout: Boolean = true
    //是否打开对尾部的支持
    private var enableFootLayout: Boolean = false
    //dateItem的view id
    private var dateItemLayout: Int = 0
    //默认尾部节日的字体大小
    private var footDefaultFestivalTextSize = 12
    //节假日提示文字大小
    private var holidayTipTextSize : Int= 8
    //节假日文字颜色
    private var holidayTipTextColor : Int = Color.RED
    //默认选中日期的字体颜色
    private var selectTodayDayTextColor : Int = Color.WHITE
    //默认选中日期的节日字体颜色
    private var selectTodayFestivalTextColor : Int = Color.WHITE
    //是否允许日期点击
    private var enableItemClick : Boolean = true
    private var itemClickBackground:Drawable ?= null

    //点击监听器
    var clickListener:DateItemClickListener ?= null
    //滑动监听器
    var pagerChangeListener:DatePagerChangeListener ?= null
    //今天日期的index
    private var currentDateIndex : Int = 0
    //日期组件的属性
    var attrubute:CalendarAttribute ?= null
    //每项日期设置完后的监听器
    var dateItemSetListener:DateSetListener ?= null
    //上一次的点击view
    var oldClickView:View ?= null
    //普通dateitem的background
    var itemBackground:Drawable ?= null

    //当前页面的月份
    var currentPagerMonth = 0
    var currentPagerYear = 0

    object Holiday{
        //节日
        const val HOLIDAY : Int = 1
        //放假后的补班
        const val WORK : Int = 0
        //普通日期，即不放假也不补班
        const val COMMON_DAY : Int = -1
        //日期属性
        var ATTRIBUTE : CalendarAttribute ?= null
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context?, nothing: AttributeSet?) : this(context, nothing, 0)

    constructor(context: Context?, nothing: AttributeSet?, def: Int?) : super(context, nothing, 0) {
        cal = Calendar.getInstance()
        initArr(context, nothing, def)
        initView()
        initListener()
    }



    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var width = MeasureSpec.getSize(widthMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)
        authoSetSize(width,height)
    }

    /**
     * 自动设置字体大小，布局高度等
     */
    private fun authoSetSize(width:Int,height:Int){
        if (width == 0 || height == 0){
            return
        }
        //标准情况下满屏宽度时使用16的字体是合适的，就用16在全宽下的比例来计算最合适的字体大小(只在屏幕宽度与当前画布宽度不同时调用)。预览模式下无法自动调节，只能改为预览模式下跳过字体自适应功能
        if (context.resources.displayMetrics.widthPixels == width || isInEditMode){
            isAuthorSetTextSize = true
            return
        }
        var percentage = GuideViewUtils.dip2px(context,16f) / SUITABLE_WIDTH
        var percentageFestival = GuideViewUtils.dip2px(context,8f) / SUITABLE_WIDTH
        var percentageHoliday = GuideViewUtils.dip2px(context,8f) / SUITABLE_WIDTH
        var percentageWidth = GuideViewUtils.dip2px(context,8f) / SUITABLE_WIDTH
        if (!isAuthorSetTextSize && (headLayout ==0 || headLayout == R.layout.calendar_head) && (dateItemLayout == 0 || dateItemLayout == R.layout.calendar_view_item_date) && (footLayout == 0 || footLayout == R.layout.calendar_foot)){
            Log.e("日志","执行重写")
            isAuthorSetTextSize = true
            if (dateDayTextSize ==16 && dateFestivalTextSize == 10 && headWeekTextSize == 16){
                dateDayTextSize = (width * percentage).toInt()
                dateFestivalTextSize = (width * percentageFestival).toInt()
                headWeekTextSize = (width * percentage).toInt()
                holidayTipTextSize = (width * percentageHoliday).toInt()
            }

            //设置头部字体
            if (headLayout == 0 || headLayout == R.layout.calendar_head){
                var percentage = resources.getDimensionPixelSize(R.dimen.titleTwo_16) / SUITABLE_WIDTH
                var percentage10 = resources.getDimensionPixelSize(R.dimen.titleOne_10) / SUITABLE_WIDTH
                var headDate = (width * percentage).toInt()
                var headLunarDate = (width * percentage10).toInt()
                var headLayout10 = (width * percentage10).toInt()
                calendarHeadTime.setTextSize(TypedValue.COMPLEX_UNIT_PX,headDate.toFloat())
                calendarHeadFestival.setTextSize(TypedValue.COMPLEX_UNIT_PX,headLunarDate.toFloat())
                calendarHeadBackToTodayTv.setTextSize(TypedValue.COMPLEX_UNIT_PX,headLunarDate.toFloat())
                calendarYearTextTv.setTextSize(TypedValue.COMPLEX_UNIT_PX,headLunarDate.toFloat())
                calendarMonthTextTv.setTextSize(TypedValue.COMPLEX_UNIT_PX,headLunarDate.toFloat())
                calendarYearPre.setTextSize(TypedValue.COMPLEX_UNIT_PX,headLunarDate.toFloat())
                calendarYearNext.setTextSize(TypedValue.COMPLEX_UNIT_PX,headLunarDate.toFloat())
                calendarMonthNext.setTextSize(TypedValue.COMPLEX_UNIT_PX,headLunarDate.toFloat())
                calendarMonthPre.setTextSize(TypedValue.COMPLEX_UNIT_PX,headLunarDate.toFloat())
                calendarBox.setPadding(headLayout10,headLayout10,headLayout10,headLayout10)
            }


            //设置尾部
            if (footLayout == 0 || footLayout == R.layout.calendar_foot){
                var percentageFootTitle = resources.getDimensionPixelSize(R.dimen.titleTwo_12) / SUITABLE_WIDTH
                var percentageFootContent = resources.getDimensionPixelSize(R.dimen.titleTwo_12) / SUITABLE_WIDTH
                var percentageFootBox = GuideViewUtils.dip2px(context,110f) / SUITABLE_HEIGHT
                var title12 = (width * percentageFootTitle).toInt()
                var title16 = (width * percentageFootContent).toInt()
                var titleBox = (width * percentageFootBox).toInt()
                footDefaultFestivalTextSize = title16
                calendarFootLunarTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX,title12.toFloat())
                calendarFootFestivalTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX,title12.toFloat())
                calendarFootSolaTerms.setTextSize(TypedValue.COMPLEX_UNIT_PX,title12.toFloat())
                calendarFootDate.setTextSize(TypedValue.COMPLEX_UNIT_PX,title16.toFloat())
                calendarFootSolarTerms.setTextSize(TypedValue.COMPLEX_UNIT_PX,title16.toFloat())
                for (index in 0..calendarFootFestival.childCount-1){
                    (calendarFootFestival.getChildAt(index) as TextView).setTextSize(TypedValue.COMPLEX_UNIT_PX,title16.toFloat())
                }
                calendarFootBox.layoutParams = calendarFootBox.layoutParams
                //设置尾部的高度到最适合的大小
                var lp = calendarFootBox.layoutParams
                lp.height = titleBox
                calendarFootBox.layoutParams = lp
            }

            attrubute?.dateDayTextSize = dateDayTextSize
            attrubute?.dateFestivalTextSize = dateFestivalTextSize
            attrubute?.holidayTipTextSize = holidayTipTextSize
            attrubute?.headWeekTextSize = headWeekTextSize
            //Log.e("日志","holidayTipTextSize大小为："+holidayTipTextSize)
            adapter.setAttribute(attrubute)
            calendarViewContent.scrollToPosition(currentDateIndex)

            //设置整个日历的padding
            var paddingPrecentage = 10 / (context.resources.displayMetrics.widthPixels*1.0)
            var calendarBoxPadding = (width * paddingPrecentage).toInt()
            calendarBox.setPadding(calendarBoxPadding,calendarBoxPadding,calendarBoxPadding,calendarBoxPadding)
        }
    }

    /**
     * 初始化界面属性
     */
    private fun initArr(context: Context?, nothing: AttributeSet?, def: Int?) {
        footDefaultFestivalTextSize = GuideViewUtils.dip2px(context,12f)
        val typedArray = context!!.theme.obtainStyledAttributes(nothing, R.styleable.CalendarView, def!!, 0)
        dateDayTextSize = typedArray.getDimensionPixelSize(R.styleable.CalendarView_dateDayTextSize, 16)
        dateFestivalTextSize = typedArray.getDimensionPixelSize(R.styleable.CalendarView_dateFestivalTextSize, 10)
        notCurrentMonthDayTextColor = typedArray.getColor(R.styleable.CalendarView_notCurrentMonthDayTextColor, context.resources.getColor(R.color.notCurrentMonthColor))
        notCurrentMonthFestivalTextColor = typedArray.getColor(R.styleable.CalendarView_notCurrentMonthFestivalTextColor, context.resources.getColor(R.color.notCurrentMonthColor))
        currentMonthDayTextColor = typedArray.getColor(R.styleable.CalendarView_currentMonthDayTextColor, context.resources.getColor(R.color.currentMonthColor))
        currentMonthFestivalTextColor = typedArray.getColor(R.styleable.CalendarView_currentMonthFestivalTextColor, context.resources.getColor(R.color.currentMonthColor))
        headWeekTextColor = typedArray.getColor(R.styleable.CalendarView_headWeekTextColor, context.resources.getColor(R.color.weekBarTextColor))
        headWeekTextSize = typedArray.getDimensionPixelSize(R.styleable.CalendarView_headWeekTextSize, 16)
        selectToday = typedArray.getBoolean(R.styleable.CalendarView_selectToday, true)
        headLayout = typedArray.getResourceId(R.styleable.CalendarView_calendarHeadLayout, 0)
        footLayout = typedArray.getResourceId(R.styleable.CalendarView_calendarFootLayout, 0)
        enableFootLayout = typedArray.getBoolean(R.styleable.CalendarView_enableFootLayout, false)
        enableHeadLayout = typedArray.getBoolean(R.styleable.CalendarView_enableHeadLayout, true)
        dateItemLayout = typedArray.getResourceId(R.styleable.CalendarView_dateItemLayout, R.layout.calendar_view_item_date)
        holidayTipTextSize = typedArray.getDimensionPixelSize(R.styleable.CalendarView_holidayTipTextSize, 8)
        holidayTipTextColor = typedArray.getColor(R.styleable.CalendarView_holidayTipTextColor, Color.RED)
        selectTodayDayTextColor = typedArray.getColor(R.styleable.CalendarView_selectTodayDayTextColor, Color.WHITE)
        selectTodayFestivalTextColor = typedArray.getColor(R.styleable.CalendarView_selectTodayFestivalTextColor, Color.WHITE)
        enableItemClick = typedArray.getBoolean(R.styleable.CalendarView_enableItemClick, true)
        var workDayTipTextColor = typedArray.getColor(R.styleable.CalendarView_workDayTipTextColor, Color.GREEN)
        val selectTodayBackgroundResource = typedArray.getDrawable(R.styleable.CalendarView_selectTodayBackground)
        enableCalendarScroll = typedArray.getBoolean(R.styleable.CalendarView_enableCalendarScroll, true)
        itemClickBackground = typedArray.getDrawable(R.styleable.CalendarView_itemClickBackground)
        var weekBarLayout = typedArray.getResourceId(R.styleable.CalendarView_weekBarLayout,R.layout.calendar_week)

        typedArray.recycle()
        attrubute = CalendarAttribute(dateDayTextSize,
                dateFestivalTextSize,
                notCurrentMonthDayTextColor,
                notCurrentMonthFestivalTextColor,
                currentMonthDayTextColor,
                currentMonthFestivalTextColor,
                headWeekTextColor,
                headWeekTextSize,
                dateItemLayout,
                holidayTipTextSize,
                holidayTipTextColor,
                selectTodayDayTextColor,
                selectTodayFestivalTextColor,enableItemClick,workDayTipTextColor,weekBarLayout,selectToday,selectTodayBackgroundResource)
        Holiday.ATTRIBUTE = attrubute
    }

    /**
     * 初始化监听器
     */
    private fun initListener() {
        calendarMonthNext?.setOnClickListener(this)
        calendarMonthPre?.setOnClickListener(this)
        calendarYearPre?.setOnClickListener(this)
        calendarYearNext?.setOnClickListener(this)
        calendarHeadBackToTodayTv?.setOnClickListener(this)

        calendarViewContent.addOnScrollListener(PagerListener(pager,object : PagerListener.OnPageChangeListener {
            override fun onScrollStateChanged(recyclerView: androidx.recyclerview.widget.RecyclerView?, newState: Int) {

            }

            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView?, dx: Int, dy: Int) {

            }

            override fun onPageSelected(position: Int) {
               /* var calendar:CalendarContentView = manager?.findViewByPosition(position) as CalendarContentView
                Log.e("日志","长度输出:"+calendar.dateViewItem!!.size)*/
                var date = adapter.title.get(position).split("-")
                calendarYearTextTv?.text = "${date[0]}"
                calendarMonthTextTv?.text = "${date[1]}"
                var maxYear = adapter.title.get(adapter.title.size-1).split("-")[0].toInt()
                var minYear = adapter.title.get(0).split("-")[0].toInt()
                //如果该年份已经存在于适配器中且年份不处于边缘值时，直接使用现有的数据，否则重新构造
                if( ((maxYear - date[0].toInt()) < RELOAD_NUM || (date[0].toInt() - minYear) < RELOAD_NUM)) {
                    jumpToDate(date[0].toInt(),date[1].toInt())
                }

                currentPagerMonth = date[1].toInt()
                currentPagerYear = date[0].toInt()
                pagerChangeListener?.let {
                    pagerChangeListener!!.onDatePagerChange(date[0].toInt(),date[1].toInt(),CalendarUtils.getDayOfMonthList(date[0].toInt(), date[1].toInt()),position)
                }

                oldClickView?.background = itemBackground
            }
        }))
    }

    /**
     * 初始化适配器
     */
    fun initAdapter(){
        adapter = CalendarRecAdapter(attrubute)
        adapter.setClickListener(object : DateItemClickListener{
            override fun onDateItemClickListener(currentView: View, dateItem: DateInfo, dateList: MutableList<DateInfo>, index: Int,oldView: View?) {
                if (oldClickView != currentView && !(dateItem.day == cal!!.get(Calendar.DAY_OF_MONTH) && dateItem.month == cal!!.get(Calendar.MONTH)+1 && dateItem.year == cal!!.get(Calendar.YEAR))){
                    oldClickView?.background = currentView.background
                    itemBackground = currentView.background
                    if (itemClickBackground != null){
                        currentView.background = itemClickBackground
                    }
                    clickListener?.onDateItemClickListener(currentView,dateItem,dateList,index,oldClickView)
                    oldClickView = currentView
                }else{
                    clickListener?.onDateItemClickListener(currentView,dateItem,dateList,index,currentView)
                    oldClickView = oldClickView
                }
                setDefaultCalendarFootInfo(dateItem)
            }
        })
        adapter.setDateSetListener(object : DateSetListener{
            override fun onDateSetListener(custonView: View, dateItem:DateInfo, dateList: MutableList<DateInfo>, index: Int) {
                dateItemSetListener?.onDateSetListener(custonView,dateItem,dateList,index)
            }
        })

        var calendarViewTitle = mutableListOf<String>()
        for (year in cal!!.get(Calendar.YEAR)- YEAR_DURATION..cal!!.get(Calendar.YEAR)+ YEAR_DURATION){
            for (month in MIN_MONTH..MAX_MONTH){
                if (year == cal!!.get(Calendar.YEAR) && month == cal!!.get(Calendar.MONTH)+1){
                    currentDateIndex = (year - (cal!!.get(Calendar.YEAR)- YEAR_DURATION))* MAX_MONTH + month-1
                }
                calendarViewTitle.add("${year}-${month}")
            }
        }
        adapter.setTitle(calendarViewTitle)

        if (enableCalendarScroll){
            manager = androidx.recyclerview.widget.LinearLayoutManager(context)
        }else{
            manager = object : androidx.recyclerview.widget.LinearLayoutManager(context){
                override fun canScrollHorizontally(): Boolean {
                    return false
                }

                override fun canScrollVertically(): Boolean {
                    return false
                }
            }
        }
        manager!!.orientation = androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
        calendarViewContent.layoutManager = manager
        calendarViewContent.adapter = adapter
        pager = androidx.recyclerview.widget.PagerSnapHelper()
        pager.attachToRecyclerView(calendarViewContent)
        calendarViewContent.scrollToPosition(currentDateIndex)
    }

    /**
     * 初始化布局
     */
    private fun initView() {
        currentPagerMonth = cal!!.get(Calendar.MONTH)+1
        currentPagerYear = cal!!.get(Calendar.YEAR)
        LayoutInflater.from(context).inflate(R.layout.calendar_view, this, true)
        initAdapter()

        //日历默认值(当前时间)
        var cal = Calendar.getInstance()
        currentMonth = cal.get(Calendar.MONTH) + 1
        todayMonth = cal.get(Calendar.MONTH) + 1
        currentYear = cal.get(Calendar.YEAR)
        currentDay = cal.get(Calendar.DAY_OF_MONTH)
        dateList = CalendarUtils.getDayOfMonthList(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        dateViewItem = mutableListOf()

        //设置头部
        if (headLayout != 0) {
            var headView = LayoutInflater.from(context).inflate(headLayout, this, false);
            if (headView == null){
                calendarHead.addView(LayoutInflater.from(context).inflate(R.layout.calendar_head, this, false))
                headLayout = R.layout.calendar_head
            }else{
                calendarHead.addView(headView)
            }

        } else {
            calendarHead.addView(LayoutInflater.from(context).inflate(R.layout.calendar_head, this, false))
            //设置当前头部的日期
            calendarMonthTextTv.setText("${currentMonth}")
            calendarYearTextTv.setText("${currentYear}")
            calendarHeadTime.setText("${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.DAY_OF_MONTH)}")
            //设置日期下面的农历或节日
            calendarHeadFestival.setText("农历："+getTodayDateInfo()?.lunar.toString())
            var  festivalInfo = getTodayDateInfo()?.getFesitval(context)
            if (festivalInfo != null){
                if (festivalInfo.getImportantFestival() != null){
                    calendarHeadFestival.setText(festivalInfo.getImportantFestival()[0])
                }
                if (festivalInfo.getLunarFestival() != null){
                    calendarHeadFestival.setText(festivalInfo.getLunarFestival()[0])
                }
                if (festivalInfo.getSolaTerms() != null){
                    calendarHeadFestival.setText(festivalInfo.getSolaTerms().name)
                }
            }
        }

        //设置尾部
        if (footLayout == 0) {
            var footView = LayoutInflater.from(context).inflate(R.layout.calendar_foot, this, false)
            if (footView == null){
                calendarFoot.addView(LayoutInflater.from(context).inflate(footLayout, this, false))
                footLayout = R.layout.calendar_foot
            }else{
                calendarFoot.addView(LayoutInflater.from(context).inflate(R.layout.calendar_foot, this, false))
                setDefaultCalendarFootInfo(getTodayDateInfo()!!)
            }

        } else {
            calendarFoot.addView(LayoutInflater.from(context).inflate(footLayout, this, false))
        }

        if (!enableHeadLayout) {
            hideHeadView()
        }
        if (!enableFootLayout) {
            hideFootView()
        }
    }


    /**
     * 设置点击监听
     */
    fun setItemClickListener(clickListener: DateItemClickListener){
        this.clickListener = clickListener
    }

    /**
     * 设置点击监听
     */
    fun setDateSetListener(dateSetListener: DateSetListener){
        this.dateItemSetListener = dateSetListener
    }

    /**
     * 设置日历滑动监听器
     */
    fun setDatePagerChangeListener(pagerChangeListener: DatePagerChangeListener){
        this.pagerChangeListener = pagerChangeListener
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    /**
     * 设置底部默认布局的文案，比如农历，节日，节气
     * @param dateInfo 日期详情
     */
    private fun setDefaultCalendarFootInfo(dateInfo:DateInfo){
        if (!enableFootLayout){
            return
        }
        var festivalList = dateInfo?.getFesitval(context)
        if (footLayout == 0){
            if (dateInfo?.lunar == null){
                calendarLunar.visibility = View.GONE
            }else{
                calendarLunar.visibility = View.VISIBLE
                calendarFootDate?.setText(dateInfo?.lunar.toString())
            }
            festivalList?.let {
                calendarFootFestival.removeAllViews()
                //设置农历节日
                var lunarFestival = festivalList.lunarFestival
                lunarFestival?.let {
                    for (item in lunarFestival){
                        if (item.contains(",")){
                            var festivalChild = item.split(",")
                            for (itemChild in festivalChild){
                                var festival = TextView(context)
                                festival.setTextSize(TypedValue.COMPLEX_UNIT_PX,footDefaultFestivalTextSize.toFloat())
                                var lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
                                lp.marginEnd = GuideViewUtils.dip2px(context,5f)
                                festival.layoutParams = lp
                                festival.setText(itemChild)
                                festival.setTextColor(context.resources.getColor(R.color.colorTitle))
                                calendarFootFestival.addView(festival)
                            }
                        }else{
                            var festival = TextView(context)
                            festival.setTextSize(TypedValue.COMPLEX_UNIT_PX,footDefaultFestivalTextSize.toFloat())
                            var lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
                            lp.marginEnd = GuideViewUtils.dip2px(context,5f)
                            festival.layoutParams = lp
                            festival.setText(item)
                            festival.setTextColor(context.resources.getColor(R.color.colorTitle))
                            calendarFootFestival.addView(festival)
                        }
                    }
                }

                //设置重要节日
                var importantFestival = festivalList.importantFestival
                importantFestival?.let {
                    for (item in importantFestival){
                        if (item.contains(",")){
                            var festivalChild = item.split(",")
                            for (itemChild in festivalChild){
                                var festival = TextView(context)
                                festival.setTextSize(TypedValue.COMPLEX_UNIT_PX,footDefaultFestivalTextSize.toFloat())
                                var lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
                                lp.marginEnd = GuideViewUtils.dip2px(context,5f)
                                festival.layoutParams = lp
                                festival.setText(itemChild)
                                festival.setTextColor(context.resources.getColor(R.color.colorTitle))
                                calendarFootFestival.addView(festival)
                            }
                        }else{
                            var festival = TextView(context)
                            festival.setTextSize(TypedValue.COMPLEX_UNIT_PX,footDefaultFestivalTextSize.toFloat())
                            var lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
                            lp.marginEnd = GuideViewUtils.dip2px(context,5f)
                            festival.layoutParams = lp
                            festival.setText(item)
                            festival.setTextColor(context.resources.getColor(R.color.colorTitle))
                            calendarFootFestival.addView(festival)
                        }
                    }
                }

                //设置其他节日
                var otherFestival = festivalList.otherFestival
                otherFestival?.let {
                    for (item in otherFestival){
                        if (item.contains(",")){
                            var festivalChild = item.split(",")
                            for (itemChild in festivalChild){
                                var festival = TextView(context)
                                festival.setTextSize(TypedValue.COMPLEX_UNIT_PX,footDefaultFestivalTextSize.toFloat())
                                var lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
                                lp.marginEnd = GuideViewUtils.dip2px(context,5f)
                                festival.layoutParams = lp
                                festival.setText(itemChild)
                                festival.setTextColor(context.resources.getColor(R.color.colorTitle))
                                calendarFootFestival.addView(festival)
                            }
                        }else{
                            var festival = TextView(context)
                            festival.setTextSize(TypedValue.COMPLEX_UNIT_PX,footDefaultFestivalTextSize.toFloat())
                            var lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT)
                            lp.marginEnd = GuideViewUtils.dip2px(context,5f)
                            festival.layoutParams = lp
                            festival.setText(item)
                            festival.setTextColor(context.resources.getColor(R.color.colorTitle))
                            calendarFootFestival.addView(festival)
                        }
                    }
                }

                if (calendarFootFestival.childCount == 0){
                    calendarFootFestivalBox.visibility = View.GONE
                }else{
                    calendarFootFestivalBox.visibility = View.VISIBLE
                }

                //设置节气
                if (festivalList.solaTerms != null){
                    calendarFootSolarTerms.setText(festivalList.solaTerms.name)
                    calendarFootSolarTermsBox.visibility = View.VISIBLE
                }else{
                    calendarFootSolarTermsBox.visibility = View.GONE
                }
            }
        }

    }




    /**
     * 获取当前时间的dateInfo
     */
    fun getTodayDateInfo(): DateInfo? {
        if (todayDateInfo != null){
            return todayDateInfo
        }
        var day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        var month = Calendar.getInstance().get(Calendar.MONTH) + 1
        var year = Calendar.getInstance().get(Calendar.YEAR)
        if (day > 32 || day <= 0) {
            return null
        }
        var dateList = CalendarUtils.getDayOfMonthList(year,month)
        if (dateList != null) {
            for (item in dateList!!.withIndex()) {
                if (item.value.day == day && item.value.isCurrentMonth) {
                    return item.value!!
                }
            }
        }
        return null
    }



    /**
     * 获取头部信息
     * @return 头部的view
     */
    fun getHeadView(): View {
        if (calendarHead.childCount != 0){
            return calendarHead.getChildAt(0)
        }else{
            return calendarHead
        }
    }

    /**
     * 隐藏头部内容
     */
    fun hideHeadView() {
        if (calendarHead != null) {
            calendarHead?.visibility = View.GONE
        }
        if (calendarHeadLine != null) {
            calendarHeadLine?.visibility = View.GONE
        }
    }

    /**
     * 隐藏尾部
     */
    fun hideFootView() {
        calendarFoot.visibility = View.GONE
    }

    fun getFootView(): View {
        return calendarFoot
    }

    override fun onClick(p0: View?) {
        when (p0) {
            calendarMonthNext -> {
                nextMonth()
                calendarMonthTextTv.setText("${currentPagerMonth}")
                calendarYearTextTv.setText("${currentPagerYear}")
            }
            calendarMonthPre -> {
                preMonth()
                calendarMonthTextTv.setText("${currentPagerMonth}")
                calendarYearTextTv.setText("${currentPagerYear}")
            }
            calendarYearPre -> {
                preYear()
                calendarMonthTextTv.setText("${currentPagerMonth}")
                calendarYearTextTv.setText("${currentPagerYear}")
            }
            calendarYearNext -> {
                nextYear()
                calendarMonthTextTv.setText("${currentPagerMonth}")
                calendarYearTextTv.setText("${currentPagerYear}")
            }
            calendarHeadBackToTodayTv -> {
                //日历默认值(当前时间)
               /* updateDate()*/
                calendarMonthTextTv.setText("${cal!!.get(Calendar.MONTH)+1}")
                calendarYearTextTv.setText("${cal!!.get(Calendar.YEAR)}")
                backToToday()
            }
        }
    }

    /**
     * 获取当前日历界面的date信息
     * 这个方法绘制完日历后立即执行可能返回内容为空值，因为可能recyclerview还没有填充完就进行获取，此时是没法获取到的
     */
    fun getCurrentPagerDateList():MutableList<DateInfo>?{
        try{
            var view:CalendarContentView =manager?.findViewByPosition(adapter.title.indexOf("${currentPagerYear}-${currentPagerMonth}")) as CalendarContentView
            return view?.dateList
        }catch (e:Exception){
            Log.e("日志","出现错误："+e.localizedMessage)
            return null
        }
    }

    /**
     * 获取当前日历界面的42宫格view
     * 这个方法绘制完日历后立即执行可能返回内容为空值，因为可能recyclerview还没有填充完就进行获取，此时是没法获取到的
     */
    fun getCurrentPagerDateView():MutableList<View>?{
        try{
            var view:CalendarContentView =manager?.findViewByPosition(adapter.title.indexOf("${currentPagerYear}-${currentPagerMonth}")) as CalendarContentView
            return view?.dateViewItem
        }catch (e:Exception){
            Log.e("日志","出现错误："+e.localizedMessage)
            return null
        }
    }

    /**
     * 返回到今天的日期
     */
    public fun backToToday(){
        currentPagerYear = cal!!.get(Calendar.YEAR)
        currentPagerMonth = cal!!.get(Calendar.MONTH)+1
        jumpToDate(cal!!.get(Calendar.YEAR),cal!!.get(Calendar.MONTH)+1)
        pagerChangeListener?.onDatePagerChange(cal!!.get(Calendar.YEAR),cal!!.get(Calendar.MONTH)+1,CalendarUtils.getDayOfMonthList(cal!!.get(Calendar.YEAR), cal!!.get(Calendar.MONTH)+1),adapter.title.indexOf("${cal!!.get(Calendar.YEAR)}-${cal!!.get(Calendar.MONTH)+1}"))
    }

    /**
     * 跳转到下一年
     */
    public fun nextYear(){
        currentPagerYear++

        var maxYear = adapter.title.get(adapter.title.size-1).split("-")[0].toInt()
        var minYear = adapter.title.get(0).split("-")[0].toInt()
        //如果该年份已经存在于适配器中且年份不处于边缘值时，直接使用现有的数据，否则重新构造
        if( ((maxYear - currentPagerYear) < RELOAD_NUM || (currentPagerYear - minYear) < RELOAD_NUM)) {
            jumpToDate(currentPagerYear,currentPagerMonth)
        }else{
            calendarViewContent.scrollToPosition( adapter.title.indexOf("${currentPagerYear}-${currentPagerMonth}") )
        }
        pagerChangeListener?.let {
            pagerChangeListener!!.onDatePagerChange(currentPagerYear,currentPagerMonth,CalendarUtils.getDayOfMonthList(currentPagerYear, currentPagerMonth),adapter.title.indexOf("${currentPagerYear}-${currentPagerMonth}"))
        }
    }

    /**
     * 跳转到上一年
     */
    public fun preYear(){
        currentPagerYear--
        var maxYear = adapter.title.get(adapter.title.size-1).split("-")[0].toInt()
        var minYear = adapter.title.get(0).split("-")[0].toInt()
        //如果该年份已经存在于适配器中且年份不处于边缘值时，直接使用现有的数据，否则重新构造
        if( ((maxYear - currentPagerYear) < RELOAD_NUM || (currentPagerYear - minYear) < RELOAD_NUM)) {
            jumpToDate(currentPagerYear,currentPagerMonth)
        }else{
            calendarViewContent.scrollToPosition( adapter.title.indexOf("${currentPagerYear}-${currentPagerMonth}") )
        }
        pagerChangeListener?.let {
            pagerChangeListener!!.onDatePagerChange(currentPagerYear,currentPagerMonth,CalendarUtils.getDayOfMonthList(currentPagerYear, currentPagerMonth),adapter.title.indexOf("${currentPagerYear}-${currentPagerMonth}"))
        }
    }

    /**
     * 跳转到上一个月
     */
    public fun preMonth(){
        if (currentPagerMonth-1<1){
            currentPagerMonth = MAX_MONTH
            currentPagerYear--
        }else{
            currentPagerMonth--
        }
        var maxYear = adapter.title.get(adapter.title.size-1).split("-")[0].toInt()
        var minYear = adapter.title.get(0).split("-")[0].toInt()
        //如果该年份已经存在于适配器中且年份不处于边缘值时，直接使用现有的数据，否则重新构造
        if( ((maxYear - currentPagerYear) < RELOAD_NUM || (currentPagerYear - minYear) < RELOAD_NUM)) {
            jumpToDate(currentPagerYear,currentPagerMonth)
        }else{
            calendarViewContent.scrollToPosition( adapter.title.indexOf("${currentPagerYear}-${currentPagerMonth}") )
        }
        pagerChangeListener?.let {
            pagerChangeListener!!.onDatePagerChange(currentPagerYear,currentPagerMonth,CalendarUtils.getDayOfMonthList(currentPagerYear, currentPagerMonth),adapter.title.indexOf("${currentPagerYear}-${currentPagerMonth}"))
        }

    }

    /**
     * 跳转到下一个月
     */
    public fun nextMonth(){
        if (currentPagerMonth+1> MAX_MONTH){
            currentPagerMonth = MIN_MONTH
            currentPagerYear++
        }else{
            currentPagerMonth++
        }
        var maxYear = adapter.title.get(adapter.title.size-1).split("-")[0].toInt()
        var minYear = adapter.title.get(0).split("-")[0].toInt()
        //如果该年份已经存在于适配器中且年份不处于边缘值时，直接使用现有的数据，否则重新构造
        if( ((maxYear - currentPagerYear) < RELOAD_NUM || (currentPagerYear - minYear) < RELOAD_NUM)) {
            jumpToDate(currentPagerYear,currentPagerMonth)
        }else{
            calendarViewContent.scrollToPosition( adapter.title.indexOf("${currentPagerYear}-${currentPagerMonth}") )
        }
        pagerChangeListener?.let {
            pagerChangeListener!!.onDatePagerChange(currentPagerYear,currentPagerMonth,CalendarUtils.getDayOfMonthList(currentPagerYear, currentPagerMonth),adapter.title.indexOf("${currentPagerYear}-${currentPagerMonth}"))
        }
    }

    /**
     * 跳转到指定年月
     */
    public fun jumpToDate(year:Int,month:Int){
        Log.e("日志","执行jump")
        var maxYear = adapter.title.get(adapter.title.size-1).split("-")[0].toInt()
        var minYear = adapter.title.get(0).split("-")[0].toInt()
        //如果该年份已经存在于适配器中且年份不处于边缘值时，直接使用现有的数据，否则重新构造
        if( adapter.title.contains("${year}-${month}") && !((maxYear - year) < RELOAD_NUM || (year - minYear) < RELOAD_NUM)){
            var index = adapter.title.indexOf("${year}-${month}")
            calendarViewContent.scrollToPosition(index)
        }else{
            var calendarViewTitle = mutableListOf<String>()
            for (y in year-YEAR_DURATION..year+YEAR_DURATION){
                for (m in MIN_MONTH..MAX_MONTH){
                    if (y == year && m == month){
                        currentDateIndex = (y - (year-(year-YEAR_DURATION)))* MAX_MONTH + m-1
                        currentPagerMonth = m
                        currentPagerYear = y
                    }
                    calendarViewTitle.add("${y}-${m}")
                }
            }
            adapter.setTitle(calendarViewTitle)
            calendarViewContent.scrollToPosition(currentDateIndex)
        }
        calendarMonthTextTv?.setText("${currentPagerMonth}")
        calendarYearTextTv?.setText("${currentPagerYear}")
    }

    interface OnDateItemClickListener {
        fun dateItemClickListener(index: Int, currentView: View, dateInfo: DateInfo,oldView:View)
    }


}