package com.jhj.app

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.jhj.slimadapter.SlimAdapter
import com.jhj.slimadapter.holder.ViewInjector
import com.jhj.slimadapter.itemdecoration.LineItemDecoration
import kotlinx.android.synthetic.main.activity_recyclerview.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

abstract class BaseCommonListActivity<T> : AppCompatActivity() {

    abstract val itemLayoutRes: Int

    //输入搜索
    open val inputSearch = false
    //分割线
    open val hasSplitLine = true
    //输入框变化就开始搜索
    open val filterFunc: (T, String) -> Boolean = { _, _ -> true }

    lateinit var adapterLocal: SlimAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recyclerview)

        val dataList = getDataList()
        if (dataList.isNullOrEmpty()) {
            include_empty_view.visibility = View.VISIBLE
        } else {
            include_empty_view.visibility = View.GONE
        }


        adapterLocal = initAdapter(dataList)
        if (hasSplitLine) {
            recyclerView.addItemDecoration(LineItemDecoration())
        }

    }

    private fun initAdapter(dataList: List<T>): SlimAdapter {

        return SlimAdapter.creator()
            .setGenericActualType(getTClazz())
            .register<T>(itemLayoutRes) { injector, bean, position ->
                itemViewConvert(this, injector, bean, position)
            }
            .attachTo(recyclerView)
            .setDataList(dataList)
    }


    /**
     * 获取泛参数实际类型
     */
    private fun getTClazz(): Type {
        //获取当前类带有泛型的父类
        val clazz: Type? = this.javaClass.genericSuperclass
        return if (clazz is ParameterizedType) {
            //获取父类的泛型参数（参数可能有多个，获取第一个）
            clazz.actualTypeArguments[0]
        } else {
            throw IllegalArgumentException()
        }
    }

    abstract fun getDataList(): List<T>
    abstract fun itemViewConvert(adapter: SlimAdapter, injector: ViewInjector, bean: T, position: Int)
}