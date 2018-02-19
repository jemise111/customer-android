package com.kustomer.kustomersdk.Views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kustomer.kustomersdk.API.KUSUserSession;
import com.kustomer.kustomersdk.DataSources.KUSChatMessagesDataSource;
import com.kustomer.kustomersdk.DataSources.KUSObjectDataSource;
import com.kustomer.kustomersdk.DataSources.KUSPaginatedDataSource;
import com.kustomer.kustomersdk.DataSources.KUSUserDataSource;
import com.kustomer.kustomersdk.Interfaces.KUSObjectDataSourceListener;
import com.kustomer.kustomersdk.Interfaces.KUSPaginatedDataSourceListener;
import com.kustomer.kustomersdk.Models.KUSChatSettings;
import com.kustomer.kustomersdk.Models.KUSUser;
import com.kustomer.kustomersdk.R;

import java.util.ArrayList;

/**
 * Created by Junaid on 1/30/2018.
 */

public class KUSToolbar extends Toolbar implements KUSObjectDataSourceListener, KUSPaginatedDataSourceListener {
    //region Properties
    private String sessionId;
    private boolean showLabel;
    private boolean showBackButton;
    private boolean showDismissButton;

    KUSUserSession userSession;
    KUSChatMessagesDataSource chatMessagesDataSource;
    KUSUserDataSource userDataSource;

    TextView tvName;
    TextView tvGreetingMessage;
    KUSMultipleAvatarsView kusMultipleAvatarsView;
    ImageView ivBack;
    ImageView ivClose;
    OnToolbarItemClickListener listener;
    //endregion

    //region Initializer
    public KUSToolbar(Context context) {
        super(context);
    }

    public KUSToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public KUSToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initViews();
        setListeners();
    }

    //endregion

    //region Public Methods
    public void initWithUserSession(KUSUserSession userSession){
        this.userSession = userSession;
        kusMultipleAvatarsView.initWithUserSession(userSession);
        //TODO: Incomplete Show Avatars & buttons etc

        this.userSession.getChatSettingsDataSource().addListener(this);
        updateTextLabel();
        //TODO: updateBackButtonBadge

    }
    //endregion

    //region Private Methods
    private void initViews(){
        tvName = findViewById(R.id.tvName);
        tvGreetingMessage = findViewById(R.id.tvGreetingMessage);
        kusMultipleAvatarsView = findViewById(R.id.multipleAvatarViews);
        ivBack = findViewById(R.id.ivBack);
        ivClose = findViewById(R.id.ivClose);

        //TODO: adjust Sizes etc

        if(showLabel){
            tvName.setVisibility(VISIBLE);
            tvGreetingMessage.setVisibility(VISIBLE);
        }else{
            tvName.setVisibility(GONE);
            tvGreetingMessage.setVisibility(GONE);
        }


    }

    private void setListeners(){
        ivBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null)
                    listener.onToolbarBackPressed();
            }
        });

        ivClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null)
                    listener.onToolbarClosePressed();
            }
        });
    }

    private void updateTextLabel() {
        if (userDataSource != null) {
            userDataSource.removeListener(this);
        }

        if(chatMessagesDataSource != null)
            userDataSource = userSession.userDataSourceForUserId(chatMessagesDataSource.getFirstOtherUserId());

        KUSUser firstOtherUser = null;
        if(userDataSource != null) {
            userDataSource.addListener(this);
            firstOtherUser = (KUSUser) userDataSource.getObject();
        }

        String responderName = "";
        if (firstOtherUser != null) {
            responderName = firstOtherUser.getDisplayName();
        }

        KUSChatSettings chatSettings = (KUSChatSettings) userSession.getChatSettingsDataSource().getObject();

        if(chatSettings != null) {
            if (responderName == null || responderName.length() == 0) {
                if (chatSettings.getTeamName() != null && chatSettings.getTeamName().length() != 0)
                    responderName = chatSettings.getTeamName();
                else
                    responderName = userSession.getOrganizationName();
            }

            tvGreetingMessage.setText(chatSettings.getGreeting());
        }
        tvName.setText(responderName);

    }
    //endregion

    //region Accessors & Mutators
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        if(this.sessionId !=null && this.sessionId.equals(sessionId))
            return;

        if(sessionId == null)
            return;

        this.sessionId = sessionId;

        if(chatMessagesDataSource != null)
            chatMessagesDataSource.removeListener(this);

        chatMessagesDataSource = userSession.getChatMessagesDataSources().get(sessionId);
        chatMessagesDataSource.addListener(this);

        kusMultipleAvatarsView.setUserIds((ArrayList<String>) chatMessagesDataSource.getOtherUserIds());

        updateTextLabel();

        //TODO: updateBackButtonBadge
    }

    public boolean isShowLabel() {
        return showLabel;
    }

    public void setShowLabel(boolean showLabel) {
        this.showLabel = showLabel;
        initViews();
    }

    public boolean isShowBackButton() {
        return showBackButton;
    }

    public void setShowBackButton(boolean showBackButton) {
        this.showBackButton = showBackButton;

        if(showBackButton)
            ivBack.setVisibility(VISIBLE);
        else
            ivBack.setVisibility(INVISIBLE);
    }

    public boolean isShowDismissButton() {
        return showDismissButton;
    }

    public void setShowDismissButton(boolean showDismissButton) {
        this.showDismissButton = showDismissButton;
    }

    public void setListener(OnToolbarItemClickListener listener) {
        this.listener = listener;
    }

    //endregion

    //region Listeners
    @Override
    public void objectDataSourceOnLoad(KUSObjectDataSource dataSource) {
        updateTextLabel();
    }

    @Override
    public void objectDataSourceOnError(KUSObjectDataSource dataSource, Error error) {

    }

    @Override
    public void onLoad(KUSPaginatedDataSource dataSource) {

    }

    @Override
    public void onError(KUSPaginatedDataSource dataSource, Error error) {

    }

    @Override
    public void onContentChange(final KUSPaginatedDataSource dataSource) {

        if(dataSource == chatMessagesDataSource){
            kusMultipleAvatarsView.setUserIds((ArrayList<String>) chatMessagesDataSource.getOtherUserIds());
            updateTextLabel();
        }else if(dataSource == userSession.getChatSessionsDataSource()){
            updateTextLabel();
        }
    }
    //endregion

    //region Interface
    public interface OnToolbarItemClickListener{
        void onToolbarBackPressed();
        void onToolbarClosePressed();
    }
    //endregion


}
