package com.marchelo.developerslite.favorites;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.SpannableStringBuilder;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.marchelo.developerslite.R;
import com.marchelo.developerslite.db.DbHelper;
import com.marchelo.developerslite.model.Favorite;
import com.marchelo.developerslite.utils.SmartLoader;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

/**
 * @author Oleg Green
 * @since 27.01.16
 */
public class FavoritesListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Favorite>> {
    private static final String TAG = FavoritesListFragment.class.getSimpleName();

    @Bind(R.id.favorites_list)
    protected GridView mFavoritesListView;

    @Bind(R.id.favorites_list_empty_view)
    protected TextView mEmptyView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites_list, container, false);
        ButterKnife.bind(this, view);

        initEmptyView();

        mFavoritesListView.setAdapter(new FavoritesListAdapter(getActivity()));
        mFavoritesListView.setEmptyView(mEmptyView);
        mFavoritesListView.setMultiChoiceModeListener(new FavoritesMultiChoiceModeListener());

        getLoaderManager().initLoader(GetFavoritesLoader.ID, null, this);

        return view;
    }

    private void initEmptyView() {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(getActivity().getString(R.string.favorites_empty_view_text_first_part)).append(" *");
        builder.setSpan(new ImageSpan(getActivity(), R.drawable.ic_favorite_white_24dp, DynamicDrawableSpan.ALIGN_BASELINE),
                builder.length() - 1, builder.length(), 0);
        builder.append(getActivity().getString(R.string.favorites_empty_view_text_last_part));
        mEmptyView.setText(builder);
    }

    @OnItemClick(R.id.favorites_list)
    void onFavoritesListItemClicked(AdapterView<?> parent, int position) {
        Favorite favorite = (Favorite) parent.getAdapter().getItem(position);
        Intent openGifIntent = new Intent(Intent.ACTION_VIEW);
        openGifIntent.setType("image/gif");
        openGifIntent.setData(Uri.parse(favorite.getGifURL()));
        FavoritesListFragment.this.startActivity(openGifIntent);
    }

    @Override
    public Loader<List<Favorite>> onCreateLoader(int id, Bundle args) {
        return new GetFavoritesLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Favorite>> loader, List<Favorite> data) {
        FavoritesListAdapter adapter = (FavoritesListAdapter) mFavoritesListView.getAdapter();
        adapter.setData(data);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<Favorite>> loader) {
        //do nothing
    }

    ///loader
    private static class GetFavoritesLoader extends SmartLoader<List<Favorite>> {
        public static final int ID = 444;

        public GetFavoritesLoader(Context context) {
            super(context);
        }

        @Override
        public List<Favorite> loadInBackground() {
            try {
                return DbHelper.from(getContext()).getAllFavorites();
            } catch (SQLException e) {
                e.printStackTrace();
                return Collections.emptyList();
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private class FavoritesMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.favorites_action_mode_menu, menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            MenuItem selectAllMenuItem = menu.findItem(R.id.action_select_all);
            selectAllMenuItem.setEnabled(mFavoritesListView.getAdapter().getCount() != mFavoritesListView.getCheckedItemCount());

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            //
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            mode.setTitle(mFavoritesListView.getCheckedItemCount() + " selected");
            mode.invalidate();
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_select_all:
                    for (int i = 0; i < mFavoritesListView.getAdapter().getCount(); i++) {
                        mFavoritesListView.setItemChecked(i, true);
                    }
                    mode.invalidate();

                    return true;

                case R.id.action_delete:
                    FavoritesListAdapter adapter = ((FavoritesListAdapter) mFavoritesListView.getAdapter());
                    SparseBooleanArray checkedItemPositions = mFavoritesListView.getCheckedItemPositions();
                    List<Favorite> listToDelete = new ArrayList<>(mFavoritesListView.getCheckedItemCount());

                    for (int i = 0; i < checkedItemPositions.size(); i++) {
                        if (checkedItemPositions.valueAt(i)) {
                            listToDelete.add(adapter.getItem(checkedItemPositions.keyAt(i)));
                        }
                    }
                    deleteFavorites(mode, listToDelete);

                    return true;
            }
            return false;
        }

        private void deleteFavorites(ActionMode mode, List<Favorite> listToDelete) {
            try {
                DbHelper dbHelper = DbHelper.from(getContext());

                dbHelper.deleteFavorites(listToDelete);
                getLoaderManager().getLoader(GetFavoritesLoader.ID).onContentChanged();
                mode.finish();

                Snackbar.make(getView(), R.string.favorites_success_deletion_msg, Snackbar.LENGTH_LONG)
                        .setAction(R.string.favorites_undo_deletion_label, v -> {
                            try {
                                dbHelper.addFavorites(listToDelete);
                                Snackbar.make(getView(), R.string.favorites_success_restoration_msg, Snackbar.LENGTH_SHORT).show();
                                getLoaderManager().getLoader(GetFavoritesLoader.ID).onContentChanged();

                            } catch (SQLException e) {
                                Log.e(TAG, "deleteFavorites: failed to restore deleted favorites: ", e);
                                Snackbar.make(getView(), R.string.favorites_failed_restoration_msg, Snackbar.LENGTH_LONG).show();
                            }
                        })
                        .show();

            } catch (SQLException e) {
                Log.e(TAG, "deleteFavorites: failed to delete favorites: ", e);
                Snackbar.make(getView(), R.string.favorites_failed_deletion_msg, Snackbar.LENGTH_LONG).show();
            }
        }
    }
}