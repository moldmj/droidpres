/*******************************************************************************
 * Copyright (c) 2010 Eugene Vorobkalo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Eugene Vorobkalo - initial API and implementation
 ******************************************************************************/
package org.droidpres.activity;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import org.droidpres.R;
import org.droidpres.adapter.DocumentListAdapter;
import org.droidpres.db.DBDroidPres;
import org.droidpres.db.QueryHelper;
import org.droidpres.utils.Const;
import org.droidpres.utils.MenuItemInfo;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;

public class DocumentListActivity extends AbsListActivity {
	private static final int MENU_EDIT					= Menu.FIRST;
	private static final int MENU_DELETE				= Menu.FIRST + 1;
	private static final int MENU_RESEND				= Menu.FIRST + 2;
	private static final int MENU_STATUS_EDIT			= Menu.FIRST + 3;
	private static final int MENU_STATUS_PREPARE_SEND	= Menu.FIRST + 4;

	private Bundle mActivityExtras;
	
	public DocumentListActivity() {
		super(R.layout.document_list, DBDroidPres.TABLE_DOCUMENT,
				new String[] {"_id", "docdate", "mainsumm", "presventype", "typedoc_id",
				"description", "docstate","warehouse_id","client_id"},
				"docdate");
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if ((mActivityExtras = getIntent().getExtras()) != null) { 
			setTitle(mActivityExtras.getString(Const.EXTRA_CLIENT_NAME));
		}
		
	}

	@Override
	protected CursorAdapter createAdapter(Cursor cursor) {
		return new DocumentListAdapter(this,
				android.R.layout.simple_list_item_2,
				cursor, 
                new String[] {"docdate", "mainsumm"}, 
                new int[] {android.R.id.text1, android.R.id.text2},mDataBase);
	}

	@Override
	protected Cursor createCursor() {
		long client_id = getIntent().getExtras().getLong(Const.EXTRA_CLIENT_ID, 0);
		if (client_id > 0 ) {
			mQueryHelper.appendFilter("CL", QueryHelper.FILTER_AND, "client_id = %d", client_id);
		}
		return mQueryHelper.createCurcor(mDataBase);
	}

	@Override
	protected List<MenuItemInfo> createContextMenus(int position, long id) {
		List<MenuItemInfo> menus = new LinkedList<MenuItemInfo>();
		Cursor cursor = getCursor();
		cursor.moveToPosition(position);
		int doc_state = QueryHelper.fieldByNameInt(cursor, "docstate");
		if (doc_state != Const.DOCSTATE_SEND) {
			menus.add(new MenuItemInfo(MENU_EDIT, R.string.lb_edit));
			menus.add(new MenuItemInfo(MENU_DELETE, R.string.lb_delete));
			switch (doc_state) {
			case Const.DOCSTATE_PREPARE_SEND:
				menus.add(new MenuItemInfo(MENU_STATUS_EDIT, R.string.lb_doc_status_edit));
				break;
			case Const.DOCSTATE_EDIT:
				menus.add(new MenuItemInfo(MENU_STATUS_PREPARE_SEND, R.string.lb_doc_status_preparesend));
				break;
			}
		} else
        {
			menus.add(new MenuItemInfo(MENU_RESEND, R.string.lb_resend));
            menus.add(new MenuItemInfo(MENU_EDIT, R.string.lb_edit));
		}
		return menus;
	}

	@Override
	protected String getContextMenuTitle(int position, long id) {
		Cursor cursor = getCursor();
		cursor.moveToPosition(position);
		DecimalFormat cf = SetupRootActivity.getCurrencyFormat(this);
		return "№" + cursor.getString(0) + " (" + 
			cf.format(QueryHelper.fieldByNameFloat(cursor, "mainsumm")) + ")";
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
		AdapterView.AdapterContextMenuInfo mi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
			case MENU_EDIT:
				editDoc(mi.position, mi.id);
				return true;
			case MENU_DELETE: 
				mDataBase.delete(DBDroidPres.TABLE_DOCUMENT,
						QueryHelper.KEY_ID + "=" + mi.id, null);
				requeryCursor();
				return true;
			case MENU_RESEND: 
				changeStatus(mi.id, Const.DOCSTATE_PREPARE_SEND);
				return true;
			case MENU_STATUS_PREPARE_SEND:
				changeStatus(mi.id, Const.DOCSTATE_PREPARE_SEND);
				return true;
			case MENU_STATUS_EDIT:
				changeStatus(mi.id, Const.DOCSTATE_EDIT);
				return true;
		}
		return false;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		//if (QueryHelper.fieldByNameInt(getCursor(), "docstate") != Const.DOCSTATE_SEND)	{
			editDoc(position, id);
		//}
	}

	private void changeStatus(long id, int status) {
		ContentValues cval = new ContentValues();
		cval.put("docstate", status);
		mDataBase.update(DBDroidPres.TABLE_DOCUMENT, cval,	QueryHelper.KEY_ID + "=" + id, null);
		requeryCursor();
	}
	
	private void editDoc(int position, long id) {
		Cursor cursor = getCursor();
		cursor.moveToPosition(position);
		Intent intent = new Intent(this, ProductListActivity.class);
		mActivityExtras.putLong(Const.EXTRA_DOC_ID, id);
        mActivityExtras.putInt(Const.EXTRA_DOC_PRESVEN,
				QueryHelper.fieldByNameInt(cursor, "presventype"));
		mActivityExtras.putInt(Const.EXTRA_DOC_TYPE,
				QueryHelper.fieldByNameInt(cursor, "typedoc_id"));
        mActivityExtras.putInt(Const.EXTRA_WAREHOUSE,
                QueryHelper.fieldByNameInt(cursor, "warehouse_id"));
		mActivityExtras.putString(Const.EXTRA_DOC_DATE,
				QueryHelper.fieldByNameString(cursor, "docdate"));
		mActivityExtras.putString(Const.EXTRA_DOC_DESC,
				QueryHelper.fieldByNameString(cursor, "description"));
        mActivityExtras.putInt(Const.EXTRA_CLIENT_PRICE_ID, GetPriceIdByDocId(id));

		intent.putExtras(mActivityExtras);
		startActivity(intent);
		finish();
	}

    private int GetPriceIdByDocId(long docId){
        //Небольшой костыль, сделан для работы отображения списка всех документов, потом поправлю.
        //TODO:Переделать через JOIN.
        QueryHelper docQueryHelper=new QueryHelper("document",new String[]{"client_id"});
        docQueryHelper.appendFilter("document_id", QueryHelper.FILTER_AND, "_id="+docId);
        Cursor docCursor=docQueryHelper.createCurcor(getDb());
        if (docCursor.moveToFirst())
        {
            long clientId= QueryHelper.fieldByNameInt(docCursor,"client_id");
            if(clientId!=0)
            {
                QueryHelper clientQueryHelper=new QueryHelper("client",new String[]{"pricelist_id"});
                clientQueryHelper.appendFilter("client_id",QueryHelper.FILTER_AND, "_id="+clientId);
                Cursor clientCursor=clientQueryHelper.createCurcor(getDb());
                if (clientCursor.moveToFirst())
                {
                    int priceId=QueryHelper.fieldByNameInt(clientCursor,"pricelist_id");
                    return priceId;
                }
            }
        }

        return 0;
    }





}
