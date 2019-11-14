package com.jhj.slimadapter


import android.content.Context
import android.support.annotation.LayoutRes
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.jhj.slimadapter.holder.SlimViewHolder
import com.jhj.slimadapter.holder.ViewInjector
import com.jhj.slimadapter.model.MultiItemTypeModel
import com.jhj.slimadapter.more.LoadMoreView
import com.jhj.slimadapter.more.SimpleLoadMoreView
import com.jhj.slimadapter.swipe.ItemTouchHelperCallback
import com.jhj.slimadapter.swipe.OnItemDragListener
import com.jhj.slimadapter.swipe.OnItemSwipeListener
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by jhj on 18-10-25.
 */

class SlimAdapter : RecyclerView.Adapter<SlimViewHolder>() {

    private var genericActualType: Type? = null
    private val dataViewTypeList = ArrayList<Type>()
    private val multiViewTypeList = ArrayList<Int>()
    private val itemViewMap = HashMap<Type, ItemViewDelegate<*>>()
    private val multiViewMap = SparseArray<ItemViewDelegate<*>>()
    private val undefineViewList = arrayListOf<TypeValue>()

    private var dataList: List<*>? = null
    private var recyclerView: RecyclerView? = null


    private val headerItemViewList = ArrayList<View>()
    private val footerItemViewList = ArrayList<View>()
    private var loadMoreView: LoadMoreView = SimpleLoadMoreView()
    private var emptyItemView: View? = null

    private var blockLoadMore: ((SlimAdapter) -> Unit)? = null
    private var isHeaderWholeLine = true
    private var isFooterWholeLine = true

    //滑动拖拽
    private var isLongPressDragEnable = false
    private var isItemSwipeEnable = false
    private var itemTouchHelperCallback: ItemTouchHelperCallback? = null

    val headerViewCount: Int
        get() = headerItemViewList.size

    private val loadMoreViewPosition: Int
        get() = headerItemViewList.size + dataListCount + footerItemViewList.size

    private val dataListCount: Int
        get() = dataList?.size ?: 0

    fun <T> register(@LayoutRes layoutRes: Int, bind: SlimAdapter.(injector: ViewInjector, bean: T, position: Int) -> Unit): SlimAdapter {
        val interfaceArray = bind.javaClass.genericInterfaces
        if (!interfaceArray.isNullOrEmpty()) {
            val bean = interfaceArray[0]
            if (bean is ParameterizedType) {
                val types = bean.actualTypeArguments
                val type = genericActualType ?: types[2]

                if (dataViewTypeList.contains(type)) {
                    throw IllegalArgumentException("The same data type can only use the register() method once.")
                }
                dataViewTypeList.add(type)
                itemViewMap[type] = object : ItemViewDelegate<T> {

                    override val itemViewLayoutId: Int
                        get() = layoutRes

                    override fun injector(injector: ViewInjector, data: T, position: Int) {
                        this@SlimAdapter.bind(injector, data, position)
                    }
                }
            }
        }
        return this
    }


    fun <T : MultiItemTypeModel> register(viewType: Int, @LayoutRes layoutRes: Int, bind: SlimAdapter.(injector: ViewInjector, bean: T, position: Int) -> Unit): SlimAdapter {
        if (multiViewTypeList.contains(viewType)) {
            throw IllegalArgumentException("please use different viewType")
        }
        multiViewTypeList.add(viewType)
        multiViewMap.put(viewType, object : ItemViewDelegate<T> {
            override val itemViewLayoutId: Int
                get() = layoutRes

            override fun injector(injector: ViewInjector, data: T, position: Int) {
                this@SlimAdapter.bind(injector, data, position)
            }
        })
        return this
    }

    fun <T> getDataList(): List<T> {
        val list = arrayListOf<T>()
        dataList?.forEach {
            try {
                val bean = it as T
                list.add(bean)
            } catch (e: ClassCastException) {
                e.printStackTrace()
            }
        }
        return list
    }

    fun getDataList(): ArrayList<*> {
        return ArrayList(dataList.orEmpty())
    }

    fun isDataListNotEmpty(): Boolean {
        return dataList.orEmpty().isNotEmpty()
    }

    fun getRecyclerView(): RecyclerView {
        return recyclerView
            ?: throw NullPointerException("RecyclerView is null,Please first use attachTo(recyclerView) method")
    }


    fun <D> setDataList(dataList: List<D>): SlimAdapter {
        this.dataList = dataList
        notifyDataSetChanged()
        resetLoadMoreStates()
        return this
    }

    fun <D> addDataList(dataList: List<D>): SlimAdapter {
        val startIndex = getDataList<D>().size + headerViewCount
        insert(dataList, startIndex)
        return this
    }


    fun <D> addDataList(index: Int, dataList: List<D>): SlimAdapter {
        insert(dataList, index)
        return this
    }

    fun <D> addData(data: D): SlimAdapter {
        val startIndex = getDataList<D>().size + headerViewCount
        insert(arrayListOf(data), startIndex)
        return this
    }

    fun <D> addData(index: Int, data: D): SlimAdapter {
        insert(arrayListOf(data), index)
        return this
    }


    fun remove(index: Int): SlimAdapter {
        val list = ArrayList(this.dataList.orEmpty())
        list.removeAt(index)
        notifyItemRemoved(index + headerItemViewList.size)
        resetLoadMoreStates()
        this.dataList = list
        return this
    }

    fun <D> insert(dataList: List<D>, index: Int) {
        val list = ArrayList(this.dataList.orEmpty())
        list.addAll(index, dataList)
        notifyItemRangeInserted(index + headerViewCount, dataList.size)
        this.dataList = list
    }


    fun attachTo(recyclerView: RecyclerView): SlimAdapter {
        recyclerView.adapter = this
        if (recyclerView.layoutManager == null) {
            recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        }
        this.recyclerView = recyclerView
        return this
    }

    // ======================================= header ================================

    fun addHeader(context: Context, layoutRes: Int, block: (SlimAdapter, View) -> Unit): SlimAdapter {
        val view = LayoutInflater.from(context).inflate(layoutRes, null, false)
        block(this, view)
        return addHeader(view)
    }

    fun addHeader(context: Context, layoutRes: Int): SlimAdapter {
        return addHeader(LayoutInflater.from(context).inflate(layoutRes, null, false))
    }

    fun addHeader(view: View): SlimAdapter {
        if (recyclerView?.layoutManager == null) {
            throw NullPointerException("layoutManager is null,Please first use attachTo(RecyclerView,LinearLayoutManager) ")
        }
        if (recyclerView?.layoutManager is LinearLayoutManager) {
            view.layoutParams =
                if ((recyclerView?.layoutManager as LinearLayoutManager).orientation == LinearLayout.VERTICAL) {
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                } else {
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
                }
        }
        headerItemViewList.add(view)
        notifyDataSetChanged()
        return this
    }

    fun removeHeader(index: Int): SlimAdapter {
        this.headerItemViewList.removeAt(index)
        notifyItemChanged(index)
        return this
    }

    fun setHeaderWholeLine(headerWholeLine: Boolean): SlimAdapter {
        this.isHeaderWholeLine = headerWholeLine
        return this
    }

    //====================================== footer ==============================
    fun addFooter(context: Context, layoutRes: Int, block: (SlimAdapter, View) -> Unit): SlimAdapter {
        val view = LayoutInflater.from(context).inflate(layoutRes, null, false)
        block(this, view)
        return addFooter(view)
    }


    fun addFooter(context: Context, layoutRes: Int): SlimAdapter {
        return addFooter(LayoutInflater.from(context).inflate(layoutRes, null, false))
    }

    fun addFooter(view: View): SlimAdapter {
        if (recyclerView?.layoutManager == null) {
            throw NullPointerException("layoutManager is null,Please first use attachTo(RecyclerView,LinearLayoutManager) ")
        }
        if (recyclerView?.layoutManager is LinearLayoutManager) {
            view.layoutParams =
                if ((recyclerView?.layoutManager as LinearLayoutManager).orientation == LinearLayout.VERTICAL) {
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                } else {
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
                }
        }
        footerItemViewList.add(view)
        notifyDataSetChanged()
        return this
    }

    fun removeFooter(index: Int): SlimAdapter {
        this.headerItemViewList.removeAt(index)
        notifyItemChanged(index + headerItemViewList.size + dataListCount)
        return this
    }

    fun setFooterWholeLine(footerWholeLine: Boolean): SlimAdapter {
        this.isFooterWholeLine = footerWholeLine
        return this
    }

    //============================= load more ===============================

    fun setOnLoadMoreListener(block: (SlimAdapter) -> Unit): SlimAdapter {
        this.blockLoadMore = block
        return this
    }

    fun setLoadMoreView(loadingView: LoadMoreView): SlimAdapter {
        this.loadMoreView = loadingView
        return this
    }

    fun loadMoreEnd() {
        if (blockLoadMore == null || loadMoreViewPosition == 0) {
            return
        }
        loadMoreView.loadMoreStatus = LoadMoreView.STATUS_END
        notifyItemChanged(loadMoreViewPosition)
    }


    fun loadMoreFail() {
        if (blockLoadMore == null || loadMoreViewPosition == 0) {
            return
        }
        loadMoreView.loadMoreStatus = LoadMoreView.STATUS_FAIL
        notifyItemChanged(loadMoreViewPosition)
    }

    fun loadMoreFail(listener: LoadMoreView.OnLoadMoreFailClickClListener) {
        loadMoreFail()
        loadMoreView.setLoadFailOnClickListener(listener)
    }

    private fun resetLoadMoreStates() {
        if (blockLoadMore != null) {
            loadMoreView.loadMoreStatus = LoadMoreView.STATUS_LOADING
            notifyItemChanged(loadMoreViewPosition)
        }
    }


    //==================================empty view ================================
    //设置Empty　View 时，该View只在header 、footer、dataList的大小都是０时显示

    fun setEmptyView(context: Context, layoutRes: Int, block: (SlimAdapter, View) -> Unit): SlimAdapter {
        val view = LayoutInflater.from(context).inflate(layoutRes, null, false)
        block(this, view)
        setEmptyView(view)
        return this
    }

    fun setEmptyView(context: Context, layoutRes: Int): SlimAdapter {
        val view = LayoutInflater.from(context).inflate(layoutRes, null, false)
        setEmptyView(view)
        return this
    }

    fun setEmptyView(emptyView: View): SlimAdapter {
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        emptyView.layoutParams = params
        emptyItemView = emptyView
        notifyDataSetChanged()
        return this
    }

    //==============================滑动拖拽=======================================

    fun setItemTouchHelper(): SlimAdapter {
        itemTouchHelperCallback = ItemTouchHelperCallback(this)
        itemTouchHelperCallback?.let {
            val itemTouchHelper = ItemTouchHelper(it)
            itemTouchHelper.attachToRecyclerView(getRecyclerView())
        }

        return this
    }

    //======　drag　======


    fun setDragItem(isDrag: Boolean): SlimAdapter {
        this.isLongPressDragEnable = isDrag
        return this
    }

    fun setDragFlags(dragFlags: Int): SlimAdapter {
        itemTouchHelperCallback?.setDragFlags(dragFlags)
        return this
    }

    fun setOnItemDragListener(onItemDragListener: OnItemDragListener): SlimAdapter {
        itemTouchHelperCallback?.setOnItemDragListener(onItemDragListener)
        return this
    }

    fun setMoveThreshold(moveThreshold: Float): SlimAdapter {
        itemTouchHelperCallback?.setMoveThreshold(moveThreshold)
        return this
    }

    //======= swipe =======

    fun getItemSwipeEnable(): Boolean {
        return isItemSwipeEnable
    }

    fun setItemSwipe(isSwipe: Boolean): SlimAdapter {
        isItemSwipeEnable = isSwipe
        return this
    }

    fun getLongPressDragEnable(): Boolean {
        return isLongPressDragEnable
    }

    fun setSwipeFlags(swipeFlags: Int): SlimAdapter {
        itemTouchHelperCallback?.setSwipeFlags(swipeFlags)
        return this
    }

    fun setOnItemSwipeListener(listener: OnItemSwipeListener): SlimAdapter {
        itemTouchHelperCallback?.setOnItemSwipeListener(listener)
        return this
    }

    fun setSwipeThreshold(swipeThreshold: Float): SlimAdapter {
        itemTouchHelperCallback?.setSwipeThreshold(swipeThreshold)
        return this
    }

    fun setSwipeFadeOutAnim(swipeFadeOutAnim: Boolean): SlimAdapter {
        itemTouchHelperCallback?.setSwipeFadeOutAnim(swipeFadeOutAnim)
        return this
    }


    private fun getGenericActualType(): Type? {
        return genericActualType
    }

    /**
     * 二次泛型封装会出现错误，可以通过该方法获取实际类型
     *
     * @return 泛型的实际类型
     */
    fun setGenericActualType(genericActualType: Type): SlimAdapter {
        this.genericActualType = genericActualType
        return this
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlimViewHolder {

        when {
            isHeaderView(viewType) -> //header
                return SlimViewHolder(headerItemViewList[HEADER_VIEW_TYPE - viewType])
            isFooterView(viewType) -> //footer
                return SlimViewHolder(footerItemViewList[FOOTER_VIEW_TYPE - viewType])
            isLoadMoreView(viewType) -> {//more
                val view = LayoutInflater.from(parent.context).inflate(loadMoreView.layoutId, parent, false)
                return SlimViewHolder(view)

            }
            isEmptyView(viewType) -> //empty
                return SlimViewHolder(emptyItemView ?: throw NullPointerException())
            isNormalBodyView(viewType) -> { //normal body
                val dataType = dataViewTypeList[BODY_VIEW_TYPE - viewType]
                val itemView = itemViewMap[dataType]
                    ?: throw NullPointerException("missing related layouts corresponding to data types , please add related layout:$dataType")
                val layoutRes = itemView.itemViewLayoutId
                return SlimViewHolder(parent, layoutRes)

            }
            multiViewTypeList.contains(viewType) -> {//multi body
                val itemView = multiViewMap.get(viewType)
                    ?: throw NullPointerException("Because you used a multi-style layout to inherit the com.jhj.slimadapter.model.MutilItemTypeModel" + ", But did not find the layout corresponding to the return value of the getItemType() method.")
                val layoutRes = itemView.itemViewLayoutId
                return SlimViewHolder(parent, layoutRes)

            }
            isUndefineBodyView(viewType) -> {
                val context = parent.context
                val density = context.resources.displayMetrics.density
                val textView = TextView(context)
                val layoutParams =
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                textView.setBackgroundColor(0xffff0000.toInt())
                textView.layoutParams = layoutParams
                textView.setPadding(
                    (20 * density).toInt(),
                    (10 * density).toInt(),
                    (20 * density).toInt(),
                    (10 * density).toInt()
                )
                textView.setTextColor(0xffffffff.toInt())
                val typeValue = undefineViewList.find { it.value == viewType }
                textView.text =
                    "\"${typeValue?.type}\" type layout not found , please call the register(...) method for layout"
                return SlimViewHolder(textView)
            }
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: SlimViewHolder, position: Int) {

        val bodyPosition = position - headerItemViewList.size

        if (bodyPosition in 0 until dataListCount) {//body

            val data = getDataList()[bodyPosition]
            if (data != null) {
                if (data is MultiItemTypeModel) {
                    val itemView = multiViewMap.get(data.itemType) as ItemViewDelegate<Any>?
                    itemView?.injector(holder.viewInjector, data, position)
                } else {
                    val itemView = itemViewMap[data::class.java] as ItemViewDelegate<Any>?
                    itemView?.injector(holder.viewInjector, data, position)
                }
            }
        } else if (isShowLoadMoreView(position)) {

            autoLoadMore()
            loadMoreView.convert(holder)
        }
    }

    override fun getItemViewType(position: Int): Int {

        val bodyPosition = position - headerItemViewList.size
        val footerPosition = position - (dataListCount + headerItemViewList.size)

        return if (position < headerItemViewList.size) { //header
            HEADER_VIEW_TYPE - position

        } else if (bodyPosition in 0 until dataListCount) { //body
            val item = getDataList()[bodyPosition]
            if (item != null) {
                if (item is MultiItemTypeModel) { //多样式布局
                    item.itemType

                } else {//普通布局
                    val index = dataViewTypeList.indexOf(item::class.java)
                    if (index == -1) {
                        var current = undefineViewList.find { it.type == item::class.java }
                        if (undefineViewList.isNullOrEmpty()) {
                            current = TypeValue(item::class.java, BODY_VIEW_UNDEFINE)
                            undefineViewList.add(current)
                        } else if (current == null) {
                            current = TypeValue(item::class.java, undefineViewList.last().value - 1)
                            undefineViewList.add(current)
                        }
                        current.value
                    } else {
                        BODY_VIEW_TYPE - index
                    }
                }
            } else {
                super.getItemViewType(position)
            }

        } else if (position >= headerItemViewList.size + dataListCount && position < dataListCount + headerItemViewList.size + footerItemViewList.size) { //footer
            FOOTER_VIEW_TYPE - footerPosition

        } else if (isShowLoadMoreView(position)) { //more
            MORE_VIEW_TYPE

        } else if (emptyItemView != null && dataListCount + headerItemViewList.size + footerItemViewList.size == 0) {  //empty
            EMPTY_VIEW_TYPE

        } else {
            super.getItemViewType(position)
        }
    }

    override fun getItemCount(): Int {
        if (emptyItemView != null && loadMoreViewPosition == 0) {
            return 1
        }
        return if (blockLoadMore != null) {
            if (loadMoreViewPosition == 0) {
                0
            } else {
                dataListCount + headerItemViewList.size + footerItemViewList.size + 1
            }
        } else {
            dataListCount + headerItemViewList.size + footerItemViewList.size
        }
    }

    override fun onViewAttachedToWindow(holder: SlimViewHolder) {
        super.onViewAttachedToWindow(holder)
        val type = holder.itemViewType
        if (isHeaderView(type) || isFooterView(type) || isLoadMoreView(type) || isEmptyView(type)) {
            setFullSpan(holder)
        } else {
            //addAnimation(holder);
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is GridLayoutManager) {
            val spanSizeLookup = layoutManager.spanSizeLookup

            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val viewType = getItemViewType(position)
                    if (isLoadMoreView(viewType)) {
                        return layoutManager.spanCount
                    } else if (isFooterView(viewType) && isFooterWholeLine) {
                        return layoutManager.spanCount
                    } else if (isHeaderView(viewType) && isHeaderWholeLine) {
                        return layoutManager.spanCount
                    } else if (isEmptyView(viewType)) {
                        return layoutManager.spanCount
                    }

                    return spanSizeLookup?.getSpanSize(position) ?: 1
                }
            }
            layoutManager.spanCount = layoutManager.spanCount
        }

    }

    //========= ViewType判断 ========
    fun isHeaderView(viewType: Int): Boolean {
        return viewType in (FOOTER_VIEW_TYPE + 1)..HEADER_VIEW_TYPE
    }

    fun isFooterView(viewType: Int): Boolean {
        return viewType in (MORE_VIEW_TYPE + 1)..FOOTER_VIEW_TYPE
    }

    fun isLoadMoreView(viewType: Int): Boolean {
        return viewType == MORE_VIEW_TYPE
    }

    fun isEmptyView(viewType: Int): Boolean {
        return viewType == EMPTY_VIEW_TYPE
    }

    fun isNormalBodyView(viewType: Int): Boolean {
        return viewType in (BODY_VIEW_UNDEFINE + 1)..BODY_VIEW_TYPE
    }

    fun isUndefineBodyView(viewType: Int): Boolean {
        return viewType <= BODY_VIEW_UNDEFINE
    }

    //====== 其他 =======


    private fun autoLoadMore() {

        if (loadMoreView.loadMoreStatus != LoadMoreView.STATUS_LOADING) {
            return
        }

        loadMoreView.loadMoreStatus = LoadMoreView.STATUS_LOADING

        if (recyclerView != null) {
            getRecyclerView().post {
                blockLoadMore?.invoke(this@SlimAdapter)
            }
        } else {
            blockLoadMore?.invoke(this@SlimAdapter)
        }

    }


    /**
     * 是否加载更多，当位于０位置的时候，不显示
     *
     * @param position 　position
     * @return boolean
     */
    private fun isShowLoadMoreView(position: Int): Boolean {
        return blockLoadMore != null && position != 0 && position == loadMoreViewPosition
    }

    private fun setFullSpan(holder: RecyclerView.ViewHolder) {
        if (holder.itemView.layoutParams is StaggeredGridLayoutManager.LayoutParams) {
            val params = holder
                .itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
            params.isFullSpan = true
        }
    }


    companion object {

        private const val HEADER_VIEW_TYPE = -0x00100000
        private const val FOOTER_VIEW_TYPE = -0x00200000
        private const val MORE_VIEW_TYPE = -0x00300000
        private const val EMPTY_VIEW_TYPE = -0x00400000
        private const val BODY_VIEW_TYPE = -0x00500000
        private const val BODY_VIEW_UNDEFINE = -0x00600000

        fun creator(): SlimAdapter {
            return SlimAdapter()
        }
    }

    private data class TypeValue(val type: Type, val value: Int)

    private interface ItemViewDelegate<T> {

        @get:LayoutRes
        val itemViewLayoutId: Int

        fun injector(injector: ViewInjector, data: T, position: Int)

    }
}
