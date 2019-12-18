package com.jhj.app.bean;

import com.jhj.slimadapter.model.BaseTreeModel;

import java.util.List;

public class TreeBean extends BaseTreeModel<TreeBean> {

    private String name;
    private List<TreeBean> children;
    private boolean root;

    private int parentLevels;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setChildren(List<TreeBean> children) {
        this.children = children;
    }

    @Override
    public void setRoot(boolean root) {
        this.root = root;
    }

    @Override
    public List<TreeBean> getChildren() {
        return children;
    }

    @Override
    public boolean isRoot() {
        return root;
    }
}
