/* 
 * Copyright (c) 2015 yvolk (Yuri Volkov), http://yurivolkov.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.andstatus.app.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import org.andstatus.app.ActivityRequestCode;
import org.andstatus.app.IntentExtra;
import org.andstatus.app.R;
import org.andstatus.app.account.MyAccount;
import org.andstatus.app.msg.MessageEditorListActivity;
import org.andstatus.app.service.CommandData;
import org.andstatus.app.util.I18n;
import org.andstatus.app.util.MyHtml;
import org.andstatus.app.util.MyLog;
import org.andstatus.app.util.StringUtils;
import org.andstatus.app.widget.MyBaseAdapter;

/**
 *  List of users for different contexts 
 *  e.g. "Users of the message", "Followers of my account(s)" etc.
 *  @author yvolk@yurivolkov.com
 */
public class UserList extends MessageEditorListActivity {
    protected UserListType mUserListType = UserListType.UNKNOWN;
    private UserListContextMenu contextMenu = null;

    public UserList() {
        mLayoutId = R.layout.my_list_swipe;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUserListType = getParsedUri().getUserListType();
        contextMenu = new UserListContextMenu(this);
    }

    @Override
    protected UserListLoader newSyncLoader(Bundle args) {
        switch (mUserListType) {
            case USERS_OF_MESSAGE:
                return new UsersOfMessageListLoader(mUserListType, getCurrentMyAccount(), centralItemId,
                        getParsedUri().getSearchQuery());
            default:
                return new UserListLoader(mUserListType, getCurrentMyAccount(), getParsedUri().getOrigin(myContext),
                        centralItemId, getParsedUri().getSearchQuery());
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (contextMenu != null) {
            contextMenu.onContextItemSelected(item);
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected MyBaseAdapter newListAdapter() {
        return new UserListViewAdapter(contextMenu, R.layout.user, getListLoader().getList(),
                getParsedUri().getOriginId() == 0);
    }

    @SuppressWarnings("unchecked")
    protected UserListLoader getListLoader() {
        return (UserListLoader) getLoaded();
    }

    @Override
    protected CharSequence getCustomTitle() {
        mSubtitle = I18n.trimTextAt(MyHtml.fromHtml(getListLoader().getTitle()), 80);
        final StringBuilder title = new StringBuilder(mUserListType.getTitle(this));
        if (StringUtils.nonEmpty(getParsedUri().getSearchQuery())) {
            I18n.appendWithSpace(title, "'" + getParsedUri().getSearchQuery() + "'");
        }
        if (getParsedUri().getOrigin(myContext).isValid()) {
            I18n.appendWithSpace(title, myContext.context().getText(R.string.combined_timeline_off_origin));
            I18n.appendWithSpace(title, getParsedUri().getOrigin(myContext).getName());
        }
        return title.toString();
    }

    @Override
    protected boolean isCommandToShowInSyncIndicator(CommandData commandData) {
        switch (commandData.getCommand()) {
            case GET_USER:
            case GET_FOLLOWERS:
            case GET_FRIENDS:
            case FOLLOW_USER:
            case STOP_FOLLOWING_USER:
                return true;
            default:
                return false;
        }
    }

    @Override
    protected boolean isRefreshNeededAfterExecuting(CommandData commandData) {
        switch(commandData.getCommand()) {
            case FOLLOW_USER:
            case STOP_FOLLOWING_USER:
                return true;
            default:
                return super.isRefreshNeededAfterExecuting(commandData);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final String method = "onActivityResult";
        MyLog.v(this, method + "; request:" + requestCode + ", result:" + (resultCode == RESULT_OK ? "ok" : "fail"));
        if (resultCode != RESULT_OK || data == null) {
            return;
        }
        switch (ActivityRequestCode.fromId(requestCode)) {
            case SELECT_ACCOUNT_TO_ACT_AS:
                accountToActAsSelected(data);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void accountToActAsSelected(Intent data) {
        MyAccount ma = myContext.persistentAccounts().fromAccountName(
                data.getStringExtra(IntentExtra.ACCOUNT_NAME.key));
        if (ma.isValid()) {
            contextMenu.setMyActor(ma);
            contextMenu.showContextMenu();
        }
    }
}
