package com.jhj.app.ui

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jhj.app.R
import com.jhj.app.bean.MultiBean
import com.jhj.app.bean.TreeBean
import com.jhj.slimadapter.SlimAdapter
import com.jhj.slimadapter.itemdecoration.LineItemDecoration
import kotlinx.android.synthetic.main.activity_recyclerview.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.leftPadding
import org.jetbrains.anko.toast

class TreeActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recyclerview)
        val list = Gson().fromJson<List<TreeBean>>(json, object : TypeToken<List<TreeBean>>() {}.type)

        recyclerView.addItemDecoration(LineItemDecoration())
        SlimAdapter.creator()
                .registerTree<TreeBean>(R.layout.list_item_root, R.layout.list_item_node) { injector, bean, position ->
                    //val imageView = injector.getView<ImageView>(R.id.image_view)
                    injector.text(R.id.textView, bean.name)
                            .with<LinearLayout>(R.id.linear_layout) {
                                it.leftPadding = dip(20) * bean.itemLevels
                            }
                            .clicked {
                                /*if (bean.isRoot) {
                                    injector.image(R.id.image_view, if (bean.isChildrenDisplay) R.mipmap.ic_tree_close else R.mipmap.ic_tree_open)
                                    if (bean.isChildrenDisplay) {
                                        rootItemClose(bean)
                                    } else {
                                        rootItemExpend(bean)
                                    }
                                    bean.isChildrenDisplay = !bean.isChildrenDisplay
                                }
*/
                                itemClickResponse(
                                        bean = bean,
                                        itemClose = {
                                            injector.image(R.id.image_view, R.mipmap.ic_tree_close)
                                            toast("关闭")
                                        },
                                        itemExpend = {
                                            injector.image(R.id.image_view, R.mipmap.ic_tree_open)
                                            toast("开启")
                                        },
                                        itemNode = {
                                            injector.image(R.id.image_view, R.mipmap.ic_launcher_round)
                                            toast("点击")
                                        }
                                )
                            }
                }
                .attachTo(recyclerView)
                .setDataList(list)


    }

    fun drawable(id: Int): Drawable {
        return ContextCompat.getDrawable(this, id) ?: resources.getDrawable(id)
    }

    val json = """
     [
  {
    "name": "根节点1",
    "root": true,
    "children": [
      {
        "name": "子节点1.1",
        "root": true,
        "children": [
          {
            "name": "子节点1.1.1",
            "root": true,
            "children": [
              {
                "name": "子节点1.1.1.1",
                "root": false,
                "children": []
              },
              {
                "name": "子节点1.1.1.2",
                "root": false,
                "children": []
              },
              {
                "name": "子节点1.1.1.3",
                "root": false,
                "children": []
              },
              {
                "name": "子节点1.1.1.4",
                "root": false,
                "children": []
              },
              {
                "name": "子节点1.1.1.5",
                "root": false,
                "children": []
              },
              {
                "name": "子节点1.1.1.6",
                "root": false,
                "children": []
              },
              {
                "name": "子节点1.1.1.7",
                "root": false,
                "children": []
              }
            ]
          },
          {
            "name": "子节点1.1.2",
            "root": false,
            "children": []
          },
          {
            "name": "子节点1.1.3",
            "root": false,
            "children": []
          },
          {
            "name": "子节点1.1.4",
            "root": false,
            "children": []
          },
          {
            "name": "子节点1.1.5",
            "root": false,
            "children": []
          },
          {
            "name": "子节点1.1.6",
            "root": false,
            "children": []
          },
          {
            "name": "子节点1.1.7",
            "root": false,
            "children": []
          }
        ]
      },
      {
        "name": "子节点1.2",
        "root": true,
        "children": []
      },
      {
        "name": "子节点1.3",
        "root": true,
        "children": []
      },
      {
        "name": "子节点1.4",
        "root": true,
        "children": []
      },
      {
        "name": "子节点1.5",
        "root": true,
        "children": []
      },
      {
        "name": "子节点1.6",
        "root": true,
        "children": []
      },
      {
        "name": "子节点1.7",
        "root": true,
        "children": [
          {
            "name": "子节点1.7.1",
            "root": true,
            "children": [
              {
                "name": "子节点1.7.1.1",
                "root": false,
                "children": []
              },
              {
                "name": "子节点1.7.1.2",
                "root": false,
                "children": []
              },
              {
                "name": "子节点1.7.1.3",
                "root": false,
                "children": []
              },
              {
                "name": "子节点1.7.1.4",
                "root": false,
                "children": []
              },
              {
                "name": "子节点1.7.1.5",
                "root": false,
                "children": []
              },
              {
                "name": "子节点1.7.1.6",
                "root": false,
                "children": []
              },
              {
                "name": "子节点1.7.1.7",
                "root": false,
                "children": []
              }
            ]
          },
          {
            "name": "子节点1.7.2",
            "root": false,
            "children": []
          },
          {
            "name": "子节点1.7.3",
            "root": false,
            "children": []
          },
          {
            "name": "子节点1.7.4",
            "root": false,
            "children": []
          },
          {
            "name": "子节点1.7.5",
            "root": false,
            "children": []
          },
          {
            "name": "子节点1.7.6",
            "root": false,
            "children": []
          },
          {
            "name": "子节点1.7.7",
            "root": false,
            "children": []
          }
        ]
      }
    ]
  }
]
    """.trimIndent()
}