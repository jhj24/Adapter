package com.jhj.app.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.jhj.app.R
import com.jhj.slimadapter.SlimAdapter
import com.jhj.slimadapter.itemdecoration.LineItemDecoration
import com.jhj.slimadapter.swipe.SwipeMenuLayout
import kotlinx.android.synthetic.main.activity_recyclerview.*
import org.jetbrains.anko.toast

/**
 * 滑动菜单
 *
 * Created by jhj on 18-10-25.
 */
class SwipeMenuActivity : AppCompatActivity() {

    val dataList = arrayListOf<String>("刘德华", "周杰伦", "成龙", "李连杰", "周星驰", "周润华", "吴京", "黄渤", "王宝强", "徐峥")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recyclerview)
        recyclerView.addItemDecoration(LineItemDecoration())
        recyclerView.layoutManager = LinearLayoutManager(this)
        SlimAdapter.creator()
                .register<String>(R.layout.list_item_swipe_menu) { injector, bean, position ->
                    val a = injector.getView<SwipeMenuLayout>(R.id.swipeMenuLayout)
                    injector.text(R.id.tv_content, bean)
                            .clicked(R.id.tv_delete) {
                                toast("删除")
                                a.quickClose()
                            }
                            .clicked(R.id.tv_top) {
                                toast("置顶")
                                a.quickClose()
                            }
                }
                .attachTo(recyclerView)
                .setDataList(dataList)

    }

}