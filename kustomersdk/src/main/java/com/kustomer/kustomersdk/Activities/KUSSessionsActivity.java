package com.kustomer.kustomersdk.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.kustomer.kustomersdk.API.KUSUserSession;
import com.kustomer.kustomersdk.Adapters.SessionListAdapter;
import com.kustomer.kustomersdk.BaseClasses.BaseActivity;
import com.kustomer.kustomersdk.DataSources.KUSChatSessionsDataSource;
import com.kustomer.kustomersdk.DataSources.KUSPaginatedDataSource;
import com.kustomer.kustomersdk.Interfaces.KUSPaginatedDataSourceListener;
import com.kustomer.kustomersdk.Kustomer;
import com.kustomer.kustomersdk.Models.KUSChatSession;
import com.kustomer.kustomersdk.R;
import com.kustomer.kustomersdk.R2;
import com.kustomer.kustomersdk.Utils.KUSConstants;
import com.kustomer.kustomersdk.Views.KUSToolbar;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;

public class KUSSessionsActivity extends BaseActivity implements KUSPaginatedDataSourceListener, SessionListAdapter.onItemClickListener, KUSToolbar.OnToolbarItemClickListener {

    //region Properties
    @BindView(R2.id.rvSessions)
    RecyclerView rvSessions;
    @BindView(R2.id.btnNewConversation)
    Button btnNewConversation;

    private KUSUserSession userSession;
    private KUSChatSessionsDataSource chatSessionsDataSource;

    private boolean didHandleFirstLoad = false;
    private SessionListAdapter adapter;
    //endregion

    //region LifeCycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setLayout(R.layout.activity_kussessions,R.id.toolbar_main,null,false);
        super.onCreate(savedInstanceState);

        userSession = Kustomer.getSharedInstance().getUserSession();
        userSession.getPushClient().setSupportScreenShown(true);

        setupAdapter();
        setupToolbar();

        chatSessionsDataSource = userSession.getChatSessionsDataSource();
        chatSessionsDataSource.addListener(this);
        chatSessionsDataSource.fetchLatest();

        if(chatSessionsDataSource.isFetched()){
            handleFirstLoadIfNecessary();
        }else{
            rvSessions.setVisibility(View.INVISIBLE);
            btnNewConversation.setVisibility(View.INVISIBLE);
            showProgressBar();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(chatSessionsDataSource != null)
            chatSessionsDataSource.fetchLatest();
    }

    @Override
    protected void onDestroy() {
        userSession.getPushClient().setSupportScreenShown(false);
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.stay, R.anim.kus_slide_down);
    }
    //endregion

    //region Initializer
    private void setupToolbar(){
        KUSToolbar kusToolbar = (KUSToolbar)toolbar;
        kusToolbar.initWithUserSession(userSession);
        kusToolbar.setShowLabel(false);
        kusToolbar.setListener(this);
        kusToolbar.setShowDismissButton(true);
    }

    private void setupAdapter(){
        adapter = new SessionListAdapter(chatSessionsDataSource, userSession, this);
        rvSessions.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,false);
        rvSessions.setLayoutManager(layoutManager);

        adapter.notifyDataSetChanged();
    }
    //endregion

    //region Private Methods
    private void handleFirstLoadIfNecessary(){
        if(didHandleFirstLoad)
            return;

        didHandleFirstLoad = true;

        if(chatSessionsDataSource != null && chatSessionsDataSource.getSize() == 0){
            Intent intent = new Intent(this, KUSChatActivity.class);
            intent.putExtra(KUSConstants.BundleName.CHAT_SCREEN_BACK_BUTTON_KEY,false);
            startActivity(intent);
        }else if (chatSessionsDataSource != null && chatSessionsDataSource.getSize() == 1){
            KUSChatSession chatSession = (KUSChatSession) chatSessionsDataSource.getFirst();

            Intent intent = new Intent(this, KUSChatActivity.class);
            intent.putExtra(KUSConstants.BundleName.CHAT_SESSION_BUNDLE__KEY,chatSession);
            startActivity(intent);
        }
    }
    //endregion

    //region Listeners
    @Optional @OnClick(R2.id.btnRetry)
    void userTappedRetry(){
        chatSessionsDataSource.fetchLatest();
        showProgressBar();
    }

    @OnClick(R2.id.btnNewConversation)
    void newConversationClicked(){
        Intent intent = new Intent(this, KUSChatActivity.class);
        startActivity(intent);
    }

    @Override
    public void onLoad(KUSPaginatedDataSource dataSource) {
        hideProgressBar();
        handleFirstLoadIfNecessary();
        rvSessions.setVisibility(View.VISIBLE);
        btnNewConversation.setVisibility(View.VISIBLE);
    }

    @Override
    public void onError(KUSPaginatedDataSource dataSource, Error error) {
        progressDialog.hide();
        String errorText = getResources().getString(R.string.something_went_wrong_please_try_again);
        showErrorWithText(errorText);
        rvSessions.setVisibility(View.INVISIBLE);
        btnNewConversation.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onContentChange(KUSPaginatedDataSource dataSource) {
        setupAdapter();
    }

    @Override
    public void onSessionItemClicked(KUSChatSession chatSession) {
        Intent intent = new Intent(this, KUSChatActivity.class);
        intent.putExtra(KUSConstants.BundleName.CHAT_SESSION_BUNDLE__KEY,chatSession);
        startActivity(intent);
    }

    @Override
    public void onToolbarBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onToolbarClosePressed() {
        clearAllLibraryActivities();
    }
    //endregion
}
