package com.searchbox;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {
    
    private Context context;
    private List<AppInfo> appList;
    private List<AppInfo> filteredAppList;

    public AppAdapter(Context context, List<AppInfo> appList) {
        this.context = context;
        this.appList = appList;
        this.filteredAppList = new ArrayList<>(appList);
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_app, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppInfo appInfo = filteredAppList.get(position);
        
        holder.appName.setText(appInfo.getAppName());
        holder.appPackage.setText(appInfo.getPackageName());
        holder.appIcon.setImageDrawable(appInfo.getAppIcon());
        
        holder.itemView.setOnClickListener(v -> {
            try {
                Intent launchIntent = context.getPackageManager()
                    .getLaunchIntentForPackage(appInfo.getPackageName());
                if (launchIntent != null) {
                    context.startActivity(launchIntent);
                } else {
                    Toast.makeText(context, "Cannot launch " + appInfo.getAppName(), 
                        Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(context, "Error launching " + appInfo.getAppName(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredAppList.size();
    }

    public void filter(String query) {
        filteredAppList.clear();
        
        if (query == null || query.trim().isEmpty()) {
            filteredAppList.addAll(appList);
        } else {
            String lowerCaseQuery = query.toLowerCase().trim();
            for (AppInfo app : appList) {
                if (app.getAppName().toLowerCase().contains(lowerCaseQuery) ||
                    app.getPackageName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredAppList.add(app);
                }
            }
        }
        
        notifyDataSetChanged();
    }

    public int getFilteredCount() {
        return filteredAppList.size();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        TextView appPackage;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.app_icon);
            appName = itemView.findViewById(R.id.app_name);
            appPackage = itemView.findViewById(R.id.app_package);
        }
    }
}