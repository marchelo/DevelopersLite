package com.marchelo.developerslite2.details;

import com.marchelo.developerslite2.model.Comment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Oleg Green
 * @since 22.05.16
 */
public class CommentsAdapterListItem {
    public final CommentsAdapterListItem parent;
    public final List<CommentsAdapterListItem> children;
    public final int itemDepth;
    public final Comment comment;
    public boolean isChildrenVisible = true;

    CommentsAdapterListItem(Comment comment, CommentsAdapterListItem parent, int itemDepth) {
        this.parent = parent;
        this.comment = comment;
        this.children = new ArrayList<>();
        this.itemDepth = itemDepth;
    }

    public CommentsAdapterListItem getParent() {
        return parent;
    }

    @SuppressWarnings("unused")
    public boolean hasParent() {
        return parent != null;
    }

    @SuppressWarnings("unused")
    public List<CommentsAdapterListItem> getChildren() {
        return children;
    }

    @SuppressWarnings("unused")
    public int getItemDepth() {
        return itemDepth;
    }

    public void setChildrenVisible(boolean isVisible) {
        this.isChildrenVisible = isVisible;
    }

    public int calculateChildCountToDeep() {
        if (children == null || children.isEmpty()) {
            return 0;
        }

        int result = children.size();
        for (CommentsAdapterListItem item : children) {
            result += item.calculateChildCountToDeep();
        }
        return result;
    }

    public boolean isChildrenVisible() {
        return isChildrenVisible;
    }

    @SuppressWarnings("unused")
    public Comment getComment() {
        return comment;
    }

    /*package*/ void addChildren(List<CommentsAdapterListItem> children) {
        this.children.addAll(children);
    }

    /*package*/ void addAllItemHierarchy(List<CommentsAdapterListItem> resultList) {
        resultList.add(this);
        if (children.isEmpty() || !isChildrenVisible) {
            return;
        }

        for (CommentsAdapterListItem item : children) {
            item.addAllItemHierarchy(resultList);
        }
    }

    public static class Builder {
        public List<CommentsAdapterListItem.Builder> children = new ArrayList<>();
        public Comment comment;

        public CommentsAdapterListItem build(CommentsAdapterListItem parent, int itemDepth) {
            CommentsAdapterListItem result = new CommentsAdapterListItem(comment, parent, itemDepth);
            List<CommentsAdapterListItem> resultChildren = new ArrayList<>(children.size());

            for (CommentsAdapterListItem.Builder childBuilder : children) {
                resultChildren.add(childBuilder.build(result, itemDepth + 1));
            }

            Collections.sort(resultChildren, CommentsAdapterList.DATE_COMPARATOR);
            result.addChildren(resultChildren);

            return result;
        }
    }
}