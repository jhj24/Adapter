package com.jhj.app.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.jhj.app.net.HttpConfig
import com.jhj.app.R
import com.jhj.app.bean.ApplyBean
import com.jhj.app.net.DataResult
import com.jhj.httplibrary.HttpCall
import com.jhj.httplibrary.callback.JsonHttpCallback
import com.jhj.slimadapter.SlimAdapter
import com.jhj.slimadapter.itemdecoration.LineItemDecoration
import com.jhj.slimadapter.more.LoadMoreView
import kotlinx.android.synthetic.main.activity_recyclerview.*
import org.jetbrains.anko.toast

/**
 * Created by jhj on 18-10-22.
 */
class LoadMoreActivity : AppCompatActivity() {

    val pageSize = 14;
    var pageNo = 0;
    var isHasData = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recyclerview)

        recyclerView.addItemDecoration(LineItemDecoration())
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = SlimAdapter.creator()
            .register<ApplyBean>(R.layout.list_item_white) { injector, bean, position ->
                injector.text(R.id.textView, bean.leaveTypeName)
            }
            .attachTo(recyclerView)
            .setOnLoadMoreListener {
                if (isHasData) {
                    setData(it, 0)
                } else {
                    it.loadMoreEnd()
                }
            }
        setData(adapter, 1)
    }

    private fun setData(adapter: SlimAdapter, i: Int) {
        HttpCall.post(HttpConfig.a)
            .addParam("memberId", "754")
            .addParam("pageSize", pageSize.toString())
            .addParam("pageNo", pageNo.toString())
            .enqueue(object : JsonHttpCallback<DataResult<List<ApplyBean>>>(this) {
                override fun onFailure(msg: String) {
                    adapter.loadMoreFail(object : LoadMoreView.OnLoadMoreFailClickClListener {
                        override fun onClicked() {
                            toast("重新加载")
                            setData(adapter, i)
                        }
                    })
                }

                override fun onResult(data: DataResult<List<ApplyBean>>?, resultType: ResultType) {
                    val list = data?.data
                    isHasData = list?.size ?: 0 >= pageSize
                    pageNo++
                    if (i == 0) {
                        adapter.addDataList(list.orEmpty())
                    } else {
                        adapter.setDataList(list.orEmpty())
                    }
                }
            })
    }
}