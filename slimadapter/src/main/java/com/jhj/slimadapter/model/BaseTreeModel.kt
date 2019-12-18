package com.jhj.slimadapter.model

abstract class BaseTreeModel<T : BaseTreeModel<T>> : TreeItemTypeModel<T> {

    override var isChildrenDisplay: Boolean = false

    override var itemLevels: Int = 1

}