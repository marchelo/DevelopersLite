package com.marchelo.developerslite;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.marchelo.developerslite.favorites.FavoritesActivity;
import com.marchelo.developerslite.post_list.BookPostListFragment;
import com.marchelo.developerslite.post_list.PostListFragment;
import com.marchelo.developerslite.post_list.QuickReturnScrollListener;
import com.marchelo.developerslite.network.ApiFactory;
import com.marchelo.developerslite.network.Constants;
import com.marchelo.developerslite.post_list.SearchPostsFragment;
import com.marchelo.developerslite.utils.IntentHelper;
import com.marchelo.developerslite.utils.StorageUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, QuickReturnScrollListener.ToolbarListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String SPINNER_VISIBILITY_KEY = "SPINNER_VISIBILITY";
    private static final String SEARCH_VISIBILITY_KEY = "SEARCH_VISIBILITY";
    private static final String SEARCH_FRAGMENT_TAG = "SEARCH_FRAGMENT";
    private static final String BOOKMARK_FRAGMENT_TAG = "BOOKMARK_FRAGMENT";

    @Nullable
    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @Bind(R.id.navigation_view)
    NavigationView mNavigationView;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.toolbar_stub_home_icon)
    View mToolbarStubIcon;

    @Bind(R.id.toolbar_title)
    TextView mTitle;

    @Bind(R.id.toolbar_spinner)
    Spinner mToolbarSpinner;

    @Bind(R.id.toolbar_search)
    View mSearchLayout;

    @Bind(R.id.txt_toolbar_search)
    EditText mSearchView;

    @Bind(R.id.icon_toolbar_search)
    ImageView mSearchViewIcon;

    @Bind(R.id.btn_toolbar_search_clear)
    ImageButton mClearSearchView;

    ActionBarDrawerToggle mDrawerToggle;
    private String mBestOfAllSpinnerItemValue;
    private String mBestOfMonthSpinnerItemValue;
    private String mBestOfWeekSpinnerItemValue;
    private String mBestOfDaySpinnerItemValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (!isSplitView() && !BuildConfig.DEBUG) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        //noinspection ConstantConditions
        actionBar.setDisplayHomeAsUpEnabled(!isSplitView());
        actionBar.setHomeButtonEnabled(!isSplitView());
        actionBar.setTitle("");

        initToolbarSearchView();
        initToolbarSpinner();
        initNavigationViewHeader();

        if (mDrawerLayout != null) {
            mDrawerToggle = new AppDrawerToggle(
                    this,
                    mDrawerLayout,
                    R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close
            );

            mDrawerLayout.addDrawerListener(mDrawerToggle);
        }

        mNavigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState != null) {
            boolean isSpinnerVisible = savedInstanceState.getBoolean(SPINNER_VISIBILITY_KEY, false);
            boolean isSearchVisible = savedInstanceState.getBoolean(SEARCH_VISIBILITY_KEY, false);

            mToolbarSpinner.setVisibility(isSpinnerVisible ? View.VISIBLE : View.GONE);
            mSearchLayout.setVisibility(isSearchVisible ? View.VISIBLE : View.GONE);

        } else {
            Category category = Category.fromString(StorageUtils.getSelectedCategory(this));
            mNavigationView.getMenu().performIdentifierAction(getNavigationItemIdFromCategory(category), 0);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (!isSplitView()) {
            mDrawerToggle.syncState();
            mToolbarStubIcon.setVisibility(View.GONE);

        } else {
            mToolbarStubIcon.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        if (!TextUtils.isEmpty(title)) {
            mTitle.setText(title);
            mTitle.setVisibility(View.VISIBLE);

        } else {
            mTitle.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_auto_load_images).setChecked(StorageUtils.isAutoLoadGifEnabled(this));
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_auto_load_images).setChecked(StorageUtils.isAutoLoadGifEnabled(this));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_auto_load_images) {

            boolean autoLoad = StorageUtils.isAutoLoadGifEnabled(this);

            if (!autoLoad) {
                new AlertDialog.Builder(this)
                        .setTitle(item.getTitle())
                        .setMessage(R.string.auto_load_gifs_warning)
                        .setCancelable(true)
                        .setNegativeButton(R.string.auto_load_gifs_cancel_enable_btn, null)
                        .setPositiveButton(R.string.auto_load_gifs_confirm_enable_btn,
                                (dialog, which) -> {
                                    StorageUtils.setAutoLoadGifEnabled(this, true);
                                    DevLifeApplication.getInstance().setAutoLoadGifs(true);
                                })
                        .show();
            } else {
                StorageUtils.setAutoLoadGifEnabled(this, false);
                DevLifeApplication.getInstance().setAutoLoadGifs(false);
            }

            return true;
        }
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (isSplitView()) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SPINNER_VISIBILITY_KEY, mToolbarSpinner.getVisibility() == View.VISIBLE);
        outState.putBoolean(SEARCH_VISIBILITY_KEY, mSearchLayout.getVisibility() == View.VISIBLE);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        mToolbarSpinner.setVisibility(View.GONE);
        mSearchLayout.setVisibility(View.GONE);

        switch (menuItem.getItemId()) {
            case R.id.navigation_item_last:
                openCategoryFragment(Category.LATEST);
                break;
            case R.id.navigation_item_best:
                mToolbarSpinner.setVisibility(View.VISIBLE);
                openCategoryFragment(Category.BEST);
                break;
            case R.id.navigation_item_hot:
                openCategoryFragment(Category.HOT);
                break;
            case R.id.navigation_item_random:
                openCategoryFragment(Category.RANDOM);
                break;
            case R.id.navigation_item_search:
                openSearchFragment();
                break;
            case R.id.navigation_item_bookmarks:
                openBookmarksFragment();
                break;
            case R.id.navigation_item_favorites:
                openFavoritesActivity();
                break;
            case R.id.navigation_item_settings:
                openSettings();
                break;
        }

        if (menuItem.getGroupId() == R.id.group_primary || menuItem.getGroupId() == R.id.group_secondary) {
            for (int i = 0; i < mNavigationView.getMenu().size(); i++) {
                mNavigationView.getMenu().getItem(i).setChecked(false);
            }
            menuItem.setChecked(true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawers();
        }

        return true;
    }

    private void initToolbarSearchView() {
        //noinspection deprecation
        int primaryColor = getResources().getColor(R.color.colorPrimary);
        mClearSearchView.setColorFilter(primaryColor);
        mSearchViewIcon.setColorFilter(primaryColor);

        mClearSearchView.setOnClickListener(v -> {
            mSearchView.setText("");
            openSearchFragment();
        });

        mSearchView.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });

        mSearchView.setOnEditorActionListener((textView, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                String query = textView.getText().toString();

                if (query.length() < 2) {
                    Toast.makeText(MainActivity.this, R.string.main_menu_search_request_too_short, Toast.LENGTH_SHORT).show();

                } else {
                    openSearchFragment();
                }
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                return true;
            }
            return false;
        });
    }

    private void initToolbarSpinner() {
        mBestOfAllSpinnerItemValue = getString(R.string.subtitle_best_of_all);
        mBestOfMonthSpinnerItemValue = getString(R.string.subtitle_best_of_month);
        mBestOfWeekSpinnerItemValue = getString(R.string.subtitle_best_of_week);
        mBestOfDaySpinnerItemValue = getString(R.string.subtitle_best_of_day);

        String[] spinnerItems = {
                mBestOfAllSpinnerItemValue,
                mBestOfMonthSpinnerItemValue,
                mBestOfWeekSpinnerItemValue,
                mBestOfDaySpinnerItemValue,
        };
        mToolbarSpinner.setAdapter(new ArrayAdapter<>(
                this,
                R.layout.simple_spinner_dropdown_item,
                spinnerItems));
        int spinnerSelPos = StorageUtils.getBestCategorySelectedPos(this);
        mToolbarSpinner.setSelection(spinnerSelPos >= spinnerItems.length ? spinnerItems.length - 1 : spinnerSelPos);

        mToolbarSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                StorageUtils.setBestCategorySelectedPos(MainActivity.this, position);
                openCategoryFragment(Category.BEST);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //nothing to do
            }
        });
    }

    private void initNavigationViewHeader() {
        View header = mNavigationView.getHeaderView(0);
        View openInBrowser = header.findViewById(R.id.btn_open_in_browser);
        View openSettings = header.findViewById(R.id.btn_open_settings);
        openInBrowser.setOnClickListener(v -> openInBrowser());
        openInBrowser.setOnLongClickListener(v -> openInBrowserHint());
        openSettings.setOnClickListener(v -> openSettings());
        openSettings.setOnLongClickListener(v -> openSettingsHint());
    }

    private int getNavigationItemIdFromCategory(Category category) {
        switch (category) {
            case BEST:
                return R.id.navigation_item_best;
            case HOT:
                return R.id.navigation_item_hot;
            case RANDOM:
                return R.id.navigation_item_random;
            default:
                Log.w(TAG, "getNavigationItemIdFromCategory: unknown category: " + category);
            case LATEST:
                return R.id.navigation_item_last;
        }
    }

    @OnClick(R.id.toolbar_title)
    public void onTitleClicked() {
        if (!isSplitView()) {
            //noinspection ConstantConditions
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private boolean isSplitView() {
        return mDrawerLayout == null;
    }

    public void openInBrowser() {
        boolean activityFound = IntentHelper.openWebLinkExcludeSelf(this,
                Uri.parse(Constants.WEB_SERVICE_BASE_URL));

        if (!activityFound) {
            new AlertDialog.Builder(this)
                    .setCancelable(true)
                    .setMessage(R.string.no_browser_found)
                    .setNegativeButton(R.string.dialog_ok, null)
                    .create()
                    .show();
        }
    }

    public void openSettings() {
        Toast.makeText(this, "Not implemented yet", Toast.LENGTH_SHORT).show();
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawers();
        }
    }

    public boolean openInBrowserHint() {
        Toast.makeText(this, R.string.open_in_browser_hint, Toast.LENGTH_SHORT).show();
        return true;
    }

    public boolean openSettingsHint() {
        Toast.makeText(this, R.string.open_settings_hint, Toast.LENGTH_SHORT).show();
        return true;
    }

    private void openCategoryFragment(Category category) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (getFragmentManager().findFragmentById(R.id.fragment_container) != null) {
            ft.replace(R.id.fragment_container, createFragmentToShow(category), category.getTag());
        } else {
            ft.add(R.id.fragment_container, createFragmentToShow(category), category.getTag());
        }
        ft.commit();

        StorageUtils.setSelectedCategory(this, category.getTag());
    }

    public void openSearchFragment() {
        mSearchLayout.setVisibility(View.VISIBLE);
        SearchPostsFragment searchFragment = SearchPostsFragment.newInstance(mSearchView.getText().toString());
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, searchFragment, SEARCH_FRAGMENT_TAG)
                .commit();

        setTitle(null);
    }

    public void openBookmarksFragment() {
        String title = getString(R.string.title_bookmarks);
        BookPostListFragment bookmarksFragment = BookPostListFragment.newInstance(title);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, bookmarksFragment, BOOKMARK_FRAGMENT_TAG)
                .commit();
    }

    public void openFavoritesActivity() {
        startActivity(new Intent(this, FavoritesActivity.class));
    }

    private PostListFragment createFragmentToShow(Category category) {
        ApiFactory.ApiByPage api;
        String title;
        switch (category) {
            case HOT:
                api = ApiFactory.hotPostsApi();
                title = getString(R.string.title_hot);
                break;
            case BEST:
                api = getBestCategoryApiByPage();
                title = getString(R.string.title_best);
                break;
            case RANDOM:
                api = ApiFactory.randomApi();
                title = getString(R.string.title_random);
                break;
            default:
            case LATEST:
                api = ApiFactory.lastPostsApi();
                title = getString(R.string.title_last);
                break;
        }
        return PostListFragment.newInstance(api, title);
    }

    @NonNull
    private ApiFactory.ApiByPage getBestCategoryApiByPage() {
        String selectedItem = (String) mToolbarSpinner.getSelectedItem();
        if (mBestOfDaySpinnerItemValue.equals(selectedItem)) {
            return ApiFactory.bestOfDayPostsApi();

        } else if (mBestOfWeekSpinnerItemValue.equals(selectedItem)) {
            return ApiFactory.bestOfWeekPostsApi();

        } else if (mBestOfMonthSpinnerItemValue.equals(selectedItem)) {
            return ApiFactory.bestOfMonthPostsApi();

        } else if (mBestOfAllSpinnerItemValue.equals(selectedItem)) {
            return ApiFactory.bestOfAllPostsApi();

        } else {
            throw new UnsupportedOperationException("Unknown subtype of best category: " + selectedItem);
        }
    }

    @Override
    public void onUp() {
        if (!isSplitView()) {
            mToolbar.animate().translationY(-mToolbar.getHeight())
                    .setInterpolator(new DecelerateInterpolator(2)).start();
        }
    }

    @Override
    public void onDown() {
        if (!isSplitView()) {
            mToolbar.animate().translationY(0)
                    .setInterpolator(new AccelerateInterpolator(2)).start();
        }
    }

    public enum Category {
        LATEST("latest"), RANDOM("random"), BEST("best"), HOT("hot");

        private final String mTag;

        Category(String tag) {
            mTag = tag;
        }

        public String getTag() {
            return mTag;
        }

        public static Category fromString(String tag) {
            if (LATEST.getTag().equalsIgnoreCase(tag)) {
                return LATEST;
            } else if (RANDOM.getTag().equalsIgnoreCase(tag)) {
                return RANDOM;
            } else if (BEST.getTag().equalsIgnoreCase(tag)) {
                return BEST;
            } else if (HOT.getTag().equalsIgnoreCase(tag)) {
                return HOT;
            } else {
                Log.w(TAG, "Category.fromString: unknown tag: " + tag);
                return LATEST;
            }
        }

        @Override
        public String toString() {
            return "Category{name='" + name() + "', mTag='" + mTag + "'}";
        }
    }

    private static class AppDrawerToggle extends ActionBarDrawerToggle {
        private final AppCompatActivity mActivity;

        public AppDrawerToggle(AppCompatActivity mainActivity, DrawerLayout drawerLayout,
                               int navigation_drawer_open, int navigation_drawer_close) {
            super(mainActivity, drawerLayout, navigation_drawer_open, navigation_drawer_close);
            mActivity = mainActivity;
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            super.onDrawerClosed(drawerView);
            mActivity.supportInvalidateOptionsMenu();
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            mActivity.supportInvalidateOptionsMenu();
        }
    }
}
