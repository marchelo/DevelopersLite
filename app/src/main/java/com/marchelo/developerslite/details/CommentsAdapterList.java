package com.marchelo.developerslite.details;

import androidx.annotation.NonNull;

import com.marchelo.developerslite.model.Comment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Oleg Green
 * @since 22.05.16
 */
public class CommentsAdapterList {
    public static final Comparator<CommentsAdapterListItem> DATE_COMPARATOR = (lhs, rhs)
            -> lhs.comment.getDate().compareTo(rhs.comment.getDate());

    private final List<CommentsAdapterListItem> mRootItems;

    private List<CommentsAdapterListItem> mFullOrderedList = new ArrayList<>();

    public static CommentsAdapterList from(List<Comment> comments) {
        List<CommentsAdapterListItem> rootItems = new ArrayList<>();

        List<CommentsAdapterListItem.Builder> rootBuilders = new ArrayList<>();

        Map<Integer, CommentsAdapterListItem.Builder> itemsBuildersMap = new HashMap<>();
        for (Comment comment : comments) {
            CommentsAdapterListItem.Builder itemBuilder = new CommentsAdapterListItem.Builder();
            itemBuilder.comment = comment;
            itemsBuildersMap.put(comment.getId(), itemBuilder);

            if (comment.getParentId() == 0) {
                rootBuilders.add(itemBuilder);
            }
        }

        for (Comment comment : comments) {
            if (comment.getParentId() != 0) {
                CommentsAdapterListItem.Builder itemBuilder = itemsBuildersMap.get(comment.getId());
                CommentsAdapterListItem.Builder parentItemBuilder = itemsBuildersMap.get(comment.getParentId());

                parentItemBuilder.children.add(itemBuilder);
            }
        }
        for (CommentsAdapterListItem.Builder itemBuilder : rootBuilders) {
            CommentsAdapterListItem item = itemBuilder.build(null, 0);
            rootItems.add(item);
        }

        return new CommentsAdapterList(rootItems);
    }

    public CommentsAdapterList() {
        mRootItems = new ArrayList<>();
    }

    public CommentsAdapterList(List<CommentsAdapterListItem> rootItems) {
        mRootItems = rootItems;
        rebuildList();
    }

    public void rebuildList() {
        Collections.sort(mRootItems, DATE_COMPARATOR);

        List<CommentsAdapterListItem> resultList = new ArrayList<>();
        for (CommentsAdapterListItem item :  mRootItems) {
            item.addAllItemHierarchy(resultList);
        }
        mFullOrderedList = resultList;
    }

    public ChangeInfo showChildrenAndRebuild(CommentsAdapterListItem item) {
        item.setChildrenVisible(true);
        int startPos = mFullOrderedList.indexOf(item) + 1;
        int count = item.calculateChildCountToDeep();

        rebuildList();

        return new ChangeInfo(startPos, count);
    }

    public ChangeInfo hideChildrenAndRebuild(CommentsAdapterListItem item) {
        item.setChildrenVisible(false);
        int startPos = mFullOrderedList.indexOf(item) + 1;
        int count = item.calculateChildCountToDeep();

        rebuildList();

        return new ChangeInfo(startPos, count);
    }

    @NonNull
    public List<CommentsAdapterListItem> getItems() {
        return mFullOrderedList;
    }

    public static final class ChangeInfo {
        public final int startPos;
        public final int count;

        public ChangeInfo(int startPos, int count) {
            this.startPos = startPos;
            this.count = count;
        }
    }
}