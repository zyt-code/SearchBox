package com.searchbox;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchActivity extends AppCompatActivity {

    private EditText searchInput;
    private ImageView clearButton;
    private RecyclerView appsRecyclerView;
    private LinearLayout emptyState;
    private ProgressBar loadingProgressBar;
    private AppAdapter appAdapter;
    private List<AppInfo> allApps = new ArrayList<>();

    // 是否已加载过系统 App
    private boolean appsLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        setupRecyclerView();
        setupSearchFunctionality();
    }

    /* ---------------- 初始化 ---------------- */

    private void initViews() {
        searchInput = findViewById(R.id.search_input);
        clearButton = findViewById(R.id.clear_button);
        appsRecyclerView = findViewById(R.id.apps_recycler_view);
        emptyState = findViewById(R.id.empty_state);
        loadingProgressBar = findViewById(R.id.loading_progress_bar);

        searchInput.requestFocus();      // 自动弹出键盘
    }

    private void setupRecyclerView() {
        appsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /* ---------------- 搜索 & 加载逻辑 ---------------- */

    private void setupSearchFunctionality() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();

                // 显示/隐藏清空按钮
                clearButton.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);

                if (query.isEmpty()) {
                    // 用户清空了输入：展示空提示、隐藏列表
                    if (appAdapter != null) appAdapter.filter("");   // 清空
                    updateEmptyState();
                    return;
                }

                // 首次输入 -> 加载
                if (!appsLoaded) {
                    loadAppsThenFilter(query);
                } else {
                    // 已加载 -> 直接过滤
                    appAdapter.filter(query);
                    updateEmptyState();
                }
            }
        });

        clearButton.setOnClickListener(v -> {
            searchInput.setText("");
            searchInput.requestFocus();
        });
    }

    /* ---------------- 异步加载 ---------------- */

    private void loadAppsThenFilter(String query) {
        loadingProgressBar.setVisibility(View.VISIBLE);
        appsRecyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<AppInfo> apps = loadAppsSynchronously();
            runOnUiThread(() -> {
                appsLoaded = true;
                allApps = apps;

                appAdapter = new AppAdapter(SearchActivity.this, allApps);
                appsRecyclerView.setAdapter(appAdapter);

                loadingProgressBar.setVisibility(View.GONE);
                appAdapter.filter(query);   // 用当前输入过滤
                updateEmptyState();
            });
        });
    }

    private List<AppInfo> loadAppsSynchronously() {
        List<AppInfo> apps = new ArrayList<>();
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> installedApps = pm.getInstalledApplications(
                PackageManager.GET_META_DATA);

        for (ApplicationInfo info : installedApps) {
            if (pm.getLaunchIntentForPackage(info.packageName) != null) {
                try {
                    apps.add(new AppInfo(
                            info.loadLabel(pm).toString(),
                            info.packageName,
                            info.loadIcon(pm)
                    ));
                } catch (Exception ignore) {
                }
            }
        }

        // 按名称排序
        apps.sort((a, b) -> a.getAppName().compareToIgnoreCase(b.getAppName()));
        return apps;
    }

    /* ---------------- UI 辅助 ---------------- */

    private void updateEmptyState() {
        if (appAdapter == null) return;
        boolean isEmpty = appAdapter.getFilteredCount() == 0;
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        appsRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    /* ---------------- 生命周期 ---------------- */

    @Override
    protected void onPause() {
        super.onPause();
        finish();   // 按 Home 键返回桌面时直接关闭
    }
}