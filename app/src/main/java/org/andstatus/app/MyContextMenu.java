/*
 * Copyright (c) 2016 yvolk (Yuri Volkov), http://yurivolkov.com
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

package org.andstatus.app;

import android.support.annotation.NonNull;
import android.view.ContextMenu;
import android.view.View;

import org.andstatus.app.account.MyAccount;
import org.andstatus.app.context.MyContext;
import org.andstatus.app.util.MyLog;

/**
 * @author yvolk@yurivolkov.com
 */
public class MyContextMenu implements View.OnCreateContextMenuListener {
    protected final LoadableListActivity listActivity;
    private View viewOfTheContext = null;
    protected ViewItem mViewItem = null;
    /**
     *  Corresponding account information ( "Reply As..." ... )
     *  oh whose behalf we are going to execute an action on this line in the list (message/user...)
     */
    @NonNull
    private MyAccount myActor = MyAccount.EMPTY;

    public MyContextMenu(LoadableListActivity listActivity) {
        this.listActivity = listActivity;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        saveContextOfSelectedItem(v);
    }

    protected void saveContextOfSelectedItem(View v) {
        viewOfTheContext = v;
        ViewItem viewItem = listActivity.saveContextOfSelectedItem(v);
        if (viewItem == null || mViewItem == null || mViewItem.getId() != viewItem.getId()) {
            myActor = MyAccount.EMPTY;
        }
        mViewItem = viewItem;
    }

    public LoadableListActivity getActivity() {
        return listActivity;
    }

    public void showContextMenu() {
        if (viewOfTheContext != null &&  viewOfTheContext.getParent() != null) {
            viewOfTheContext.post(new Runnable() {

                @Override
                public void run() {
                    try {
                        viewOfTheContext.showContextMenu();
                    } catch (NullPointerException e) {
                        MyLog.d(this, "on showContextMenu", e);
                    }
                }
            });
        }
    }

    @NonNull
    public MyAccount getMyActor() {
        return myActor;
    }

    public void setMyActor(MyAccount myAccount) {
        if (myAccount != null) {
            this.myActor = myAccount;
        }
    }

    public MyContext getMyContext() {
        return getActivity().getMyContext();
    }
}
