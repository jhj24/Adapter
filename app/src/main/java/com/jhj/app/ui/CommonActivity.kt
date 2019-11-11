package com.jhj.app.ui

import com.jhj.app.BaseCommonListActivity
import com.jhj.app.R
import com.jhj.slimadapter.SlimAdapter
import com.jhj.slimadapter.holder.ViewInjector

/**
 * Created by jhj on 18-10-22.
 */
class CommonActivity : BaseCommonListActivity<Bean>() {
    override val itemLayoutRes: Int
        get() = R.layout.list_item_white

    override fun getDataList(): List<Bean> {
        return listOf(
            Bean("刘德华"), Bean("周杰伦"), Bean("成龙"), Bean("李连杰"),
            Bean("周星驰"), Bean("周润华"), Bean("吴京"), Bean("黄渤"),
            Bean("王宝强"), Bean("徐峥")
        )
    }

    override fun itemViewConvert(adapter: SlimAdapter, injector: ViewInjector, bean: Bean, position: Int) {
        injector.text(R.id.textView, bean.name)
    }
}

data class Bean(val name: String, val id: Int = 0)