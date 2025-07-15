package com.searchbox;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    
    private EditText searchInput;
    private ImageView clearButton;
    private RecyclerView appsRecyclerView;
    private LinearLayout emptyState;
    private ProgressBar loadingProgressBar;
    private AppAdapter appAdapter;
    private List<AppInfo> allApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        
        initViews();
        setupRecyclerView();
        loadInstalledApps();
        setupSearchFunctionality();
    }

    private void initViews() {
        searchInput = findViewById(R.id.search_input);
        clearButton = findViewById(R.id.clear_button);
        appsRecyclerView = findViewById(R.id.apps_recycler_view);
        emptyState = findViewById(R.id.empty_state);
        loadingProgressBar = findViewById(R.id.loading_progress_bar);
        
        // Focus on search input when activity starts
        searchInput.requestFocus();
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        appsRecyclerView.setLayoutManager(layoutManager);
    }

    private void loadInstalledApps() {
        // Show loading indicator
        loadingProgressBar.setVisibility(View.VISIBLE);
        appsRecyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);
        
        // Load apps in background thread
        new LoadAppsTask().execute();
    }
    
    private class LoadAppsTask extends AsyncTask<Void, Void, List<AppInfo>> {
        @Override
        protected List<AppInfo> doInBackground(Void... voids) {
            List<AppInfo> apps = new ArrayList<>();
            PackageManager packageManager = getPackageManager();
            
            List<ApplicationInfo> installedApps = packageManager.getInstalledApplications(
                PackageManager.GET_META_DATA);
            
            for (ApplicationInfo appInfo : installedApps) {
                // Only include apps that can be launched (have a launcher intent)
                if (packageManager.getLaunchIntentForPackage(appInfo.packageName) != null) {
                    String appName = appInfo.loadLabel(packageManager).toString();
                    String packageName = appInfo.packageName;
                    
                    try {
                        AppInfo app = new AppInfo(
                            appName,
                            packageName,
                            appInfo.loadIcon(packageManager)
                        );
                        apps.add(app);
                    } catch (Exception e) {
                        // Skip apps that can't load icon
                    }
                }
            }
            
            // Sort apps alphabetically
            apps.sort((app1, app2) -> app1.getAppName().compareToIgnoreCase(app2.getAppName()));
            
            return apps;
        }
        
        @Override
        protected void onPostExecute(List<AppInfo> apps) {
            allApps = apps;
            
            // Initialize adapter with all apps
            appAdapter = new AppAdapter(SearchActivity.this, allApps);
            appsRecyclerView.setAdapter(appAdapter);
            
            // Hide loading indicator and show content
            loadingProgressBar.setVisibility(View.GONE);
            updateEmptyState();
        }
    }

    private void setupSearchFunctionality() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                
                // Show/hide clear button
                clearButton.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                
                // Filter apps
                if (appAdapter != null) {
                    appAdapter.filter(query);
                    updateEmptyState();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Clear button functionality
        clearButton.setOnClickListener(v -> {
            searchInput.setText("");
            searchInput.requestFocus();
        });
    }

    private void updateEmptyState() {
        if (appAdapter != null) {
            boolean isEmpty = appAdapter.getFilteredCount() == 0;
            emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            appsRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        // Close the activity when back is pressed
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Close the activity when user returns to desktop
        finish();
    }
}