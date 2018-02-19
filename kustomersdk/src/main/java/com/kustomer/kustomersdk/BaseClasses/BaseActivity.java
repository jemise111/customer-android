package com.kustomer.kustomersdk.BaseClasses;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.kustomer.kustomersdk.R;
import com.kustomer.kustomersdk.R2;
import com.kustomer.kustomersdk.Receivers.NetworkStateReceiver;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class BaseActivity extends AppCompatActivity implements NetworkStateReceiver.NetworkStateReceiverListener {

    //region Properties
    public ProgressDialog progressDialog;
    private NetworkStateReceiver networkStateReceiver;
    protected Toolbar toolbar;

    private static List<Activity> libraryActivities = new ArrayList<>();

    @Nullable @BindView(R2.id.retryView)
    View retryView;
    @Nullable @BindView(R2.id.tvError)
    TextView tvError;
    //endregion

    //region Activity LifeCycle
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        networkStateReceiver = new NetworkStateReceiver();
        setupDialog();

        libraryActivities.add(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        networkStateReceiver.addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        networkStateReceiver.removeListener(this);
        unregisterReceiver(networkStateReceiver);
    }
    //endregion

    //region Methods
    private void setupDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.processing));
        progressDialog.setCancelable(false);
    }

    protected void setLayout(int layoutId, int toolbarId, String title, boolean homeEnabled) {
        setContentView(layoutId);

        if(toolbarId != -1)
            setupToolbar(title, toolbarId, homeEnabled);
    }

    protected void setLayout(int layoutId) {
        setContentView(layoutId);
    }

    protected void showProgressBar(String text){
        progressDialog.setMessage(text);
        showProgressBar();
    }

    protected void showProgressBar(){
        progressDialog.show();

        if(retryView != null && retryView.getVisibility() == View.VISIBLE)
            retryView.setVisibility(View.GONE);
    }

    protected void hideProgressBar(){
        progressDialog.hide();

        if(retryView != null)
            retryView.setVisibility(View.GONE);
    }

    protected void showErrorWithText(String text){
        progressDialog.hide();

        if(retryView != null && retryView.getVisibility() == View.GONE)
            retryView.setVisibility(View.VISIBLE);

        if(tvError != null)
            tvError.setText(text);
    }

    private void setupToolbar(String title, int toolbarId, boolean enabled) {
        toolbar = findViewById(toolbarId);
        setSupportActionBar(toolbar);
        if (title != null && !title.isEmpty())
            setTitle(title);
        else
            setTitle("");

        setBackButton(enabled);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @
                    Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    protected void setBackButton(boolean enabled){
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(enabled);
    }

    protected static void clearAllLibraryActivities(){
        for(Activity activity : libraryActivities){
            activity.finish();
        }
    }
    //endregion

    //region Callbacks
    @Override
    public void networkAvailable() {

    }

    @Override
    public void networkUnavailable() {
    }
    //endregion
}
