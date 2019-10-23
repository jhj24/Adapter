package com.jhj.slimadapter.holder

import android.support.annotation.LayoutRes


/**
 * 获取布局以及实现数据绑定
 *
 *
 * Created by jhj on 18-10-12.
 */

interface ItemViewDelegate<T> {

    @get:LayoutRes
    val itemViewLayoutId: Int

    fun injector(injector: ViewInjector, data: T, position: Int)

}
