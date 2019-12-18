package com.jhj.slimadapter.model

interface TreeItemTypeModel<T : TreeItemTypeModel<T>> {

    /**
     * 子children是否已展示
     */
    var isChildrenDisplay: Boolean

    /**
     * 是否是根节点
     */
    var isRoot: Boolean

    /**
     * 当前节点所在级别
     */
    var itemLevels: Int

    /**
     * 获取所有子类
     *
     * @return list
     */
    fun getChildren(): List<T>?


}