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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.CursorAdapter;
import android.widget.FilterQueryProvider;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.droidpres.R;
import org.droidpres.adapter.CharasteristicListAdapter;
import org.droidpres.adapter.ProductListAdapter;
import org.droidpres.db.DBDroidPres;
import org.droidpres.db.QueryHelper;
import org.droidpres.dialog.DocHead;
import org.droidpres.utils.Const;
import org.droidpres.utils.DocData;
import org.droidpres.utils.MenuItemInfo;
import org.droidpres.utils.Utils;
import org.droidpres.utils.onDocDataChange;

import java.sql.Date;
import java.util.Calendar;
import java.util.List;

public class CharacteristicListActivity extends AbsListActivity implements
		OnKeyListener, 	onDocDataChange {
	private static final int A_RESULT_QTY = 100;

	private static final int MENU_GROUP		= Menu.FIRST;
	private static final int MENU_SEARCH	= Menu.FIRST + 1;
	private static final int MENU_HEAD 		= Menu.FIRST + 2;
	private static final int MENU_CLEAN 	= Menu.FIRST + 3;
	private static final int MENU_SAVE	 	= Menu.FIRST + 4;

	private static final int DLG_QUERY_SAVE_DOCUMENT = 1;
	private static final int DLG_QUERY_STATE_DOCUMENT = 2;
	private static final int DLG_PRODUCT_GROUP = 3;
	private static final int DLG_DOCHEAD = 4;

	private Bundle mActivityExtras;
	private MenuItem mMiSave;
	private boolean mDocNewFlag = true;
	private boolean mFiltredFlag = false;
	private boolean mAvailableSaveFlag;
	private float mOldQty = 1;

	public CharacteristicListActivity() {
		super(R.layout.characteristic_list, DBDroidPres.TABLE_CHARACTERISTIC,
				new String[] {"_id", "name", "casesize", "product_id"},
				"name",null);		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		CharasteristicListAdapter adp = (CharasteristicListAdapter) mAdapter;
		
		
		//Сроки поджимают. поэтому такой костыль
		if ((mActivityExtras = getIntent().getExtras()) != null) {
				//mQueryHelper.appendFilter("pricelist_id", QueryHelper.FILTER_AND, "pricelist_id =" +mActivityExtras.getInt(Const.EXTRA_CLIENT_PRICE_ID));
                mQueryHelper.appendFilter("warehouse_id", QueryHelper.FILTER_AND, "warehouse_id =" +mActivityExtras.getLong(Const.EXTRA_WAREHOUSE));
                long i=mActivityExtras.getLong(Const.EXTRA_PRODUCT_ID);
                mQueryHelper.appendFilter("product_id", QueryHelper.FILTER_AND, "Characteristic.product_id =" +mActivityExtras.getLong(Const.EXTRA_PRODUCT_ID));
				mAdapter.changeCursor(createCursor());
		}		
		
		
		if (savedInstanceState != null) {
			adp.mDocData = savedInstanceState.getParcelable(
					Utils.getConstName(mAdapter.getClass(), "mDocData"));
		} else {
			adp.mDocData = new DocData();
		}
		adp.mDocData.setOnDocDataChange(this);
		
		if ((mActivityExtras = getIntent().getExtras()) != null) {
			setTitle(mActivityExtras.getString(Const.EXTRA_CLIENT_NAME));
			mDocNewFlag = (mActivityExtras.getLong(Const.EXTRA_DOC_ID, 0) == 0);
			if (! mDocNewFlag) {
				adp.mDocData.Load(mActivityExtras.getLong(Const.EXTRA_DOC_ID), mDataBase);
			}
		}
		
		mAvailableSaveFlag = false;
		//mAdapter.setFilterQueryProvider(this);

        adp.mDocData=(DocData) getIntent().getParcelableExtra("docdata");
        mAdapter.notifyDataSetChanged();
		getListView().setOnKeyListener(this);
	}

	@Override
	protected CursorAdapter createAdapter(Cursor cursor) {
		return new CharasteristicListAdapter(this,
				R.layout.characteristic_list_item,
				cursor,
                new String[] {"name", "available","price", "_id"},
                new int[] {R.id.tvDocGoods, R.id.tvDocAvilable, R.id.tvDocPrice, R.id.tvDocQty});
	}

	@Override
	protected Cursor createCursor() {
		return mQueryHelper.createCurcor(mDataBase);
	}

	/**
	 * Основное меню
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		return true;
	}

	/**
	 * Выполнение основного меню
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch  (item.getItemId()) {
		case (MENU_GROUP):
			showDialog(DLG_PRODUCT_GROUP);
			return true;
		case (MENU_SEARCH):
			InputMethodManager inputMgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMgr.toggleSoftInput(0, 0);
			return true;
		case (MENU_HEAD):
			showDialog(DLG_DOCHEAD);
			return true;
		case (MENU_CLEAN):
			((CharasteristicListAdapter) mAdapter).mDocData.clear();
			mAdapter.notifyDataSetChanged();
			return true;
		case (MENU_SAVE):
			showDialog(DLG_QUERY_STATE_DOCUMENT);
			return true;
		}
		return false;
	}

	/**
	 * Перехват аппаратных клавиш
	 */
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            CharasteristicListAdapter adp = (CharasteristicListAdapter) mAdapter;
            Parcel doc=Parcel.obtain();
            adp.mDocData.writeToParcel(doc,1);
            Intent intent = new Intent();
            intent.putExtra("docdata",adp.mDocData);
            setResult(RESULT_OK, intent);
			finish();
			return true;
		};
		if (keyCode == KeyEvent.KEYCODE_SEARCH && event.getAction() == KeyEvent.ACTION_DOWN) {
			InputMethodManager inputMgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMgr.toggleSoftInput(0, 0);
			return true;
		}
		return false;
	}

	/**
	 * При выборе товара из списка
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int pos, long characteristic_id) {
		super.onListItemClick(l, v, pos, characteristic_id);

        CharasteristicListAdapter adp = (CharasteristicListAdapter) mAdapter;

		float _qty = adp.mDocData.getQtyByCharacteristicId(characteristic_id);
        float casesize=QueryHelper.fieldByNameFloat(getCursor(), "casesize");
        long product_id=QueryHelper.fieldByNameLong(getCursor(), "product_id");
        //float minqty=QueryHelper.fieldByNameFloat(getCursor(), "minqty");
		if (_qty > 0) {
            if (adp.mCaseShowFlag) _qty = _qty/casesize;
            else _qty=_qty+(casesize>0?casesize:1)-1;
			ChangeQty(product_id, _qty + mOldQty, QueryHelper.fieldByNameFloat(getCursor(), "price"),
                    casesize,characteristic_id);
		} else {
			ChangeQty(product_id, mOldQty*(casesize>0&&!adp.mCaseShowFlag?casesize:1), QueryHelper.fieldByNameFloat(getCursor(), "price"),
                    casesize,characteristic_id);
		}
	}

    @Override
	protected List<MenuItemInfo> createContextMenus(int position, long characteristic_id) {
		Cursor cur = getCursor();
        CharasteristicListAdapter adp = (CharasteristicListAdapter) mAdapter;

		cur.moveToPosition(position);
		Intent intent = new Intent(this, InputQtyActivity.class);
		intent.putExtra(Const.EXTRA_PRODUCT_NAME, QueryHelper.fieldByNameString(cur, "name"));
		intent.putExtra(Const.EXTRA_CHARACTERISTIC_ID, characteristic_id);
		intent.putExtra(Const.EXTRA_PRICE, QueryHelper.fieldByNameFloat(cur, "price"));
		intent.putExtra(Const.EXTRA_CASESIZE, QueryHelper.fieldByNameFloat(cur, "casesize"));

		float tqty = adp.mDocData.getQtyByCharacteristicId(characteristic_id);
		if (tqty > 0) {
			if (adp.mCaseShowFlag) {
				intent.putExtra(Const.EXTRA_QTY, tqty / QueryHelper.fieldByNameFloat(cur, "casesize"));
			} else {
				intent.putExtra(Const.EXTRA_QTY, tqty);
			}
		}
		startActivityForResult(intent, A_RESULT_QTY);
		return null;
	}

	/**
	 * Генерация диалогов
	 */
	@Override
	protected Dialog onCreateDialog(int dialogID) {
		super.onCreateDialog(dialogID);


			return null;

	}

	/**
	 *  Подготовка диалога
	 */
	@Override
	protected void onPrepareDialog(int dialogID, Dialog dialog) {
		super.onPrepareDialog(dialogID, dialog);
		switch (dialogID) {
		case DLG_DOCHEAD:
			((DocHead) dialog).setPresVenType(mActivityExtras.getInt(Const.EXTRA_DOC_PRESVEN));
			((DocHead) dialog).setDocTypeID(mActivityExtras.getInt(Const.EXTRA_DOC_TYPE));
            ((DocHead) dialog).setWarwhouseID(mActivityExtras.getInt(Const.EXTRA_WAREHOUSE));
			((DocHead) dialog).setDocDesc(mActivityExtras.getString(Const.EXTRA_DOC_DESC));
			((DocHead) dialog).setDocDate(mActivityExtras.getString(Const.EXTRA_DOC_DATE));
			//((DocHead) dialog).setDocDiscount(mActivityExtras.getBoolean(Const.EXTRA_DOC_DISCOUNT));

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode != RESULT_OK) return;

		switch (requestCode) {
		case A_RESULT_QTY:
			float tqty = data.getFloatExtra(Const.EXTRA_QTY, 0);
            ChangeQty(data.getLongExtra(Const.EXTRA_PRODUCT_ID, 0),tqty,
					data.getFloatExtra(Const.EXTRA_PRICE, 0), data.getFloatExtra(Const.EXTRA_CASESIZE, 0),data.getLongExtra(Const.EXTRA_PRODUCT_ID, 0));
			break;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
        CharasteristicListAdapter adp = (CharasteristicListAdapter) mAdapter;
		outState.putParcelable(Utils.getConstName(mAdapter.getClass(), "mDocData"), adp.mDocData);
	}


	public void onDataChange(float summ) {
		mAvailableSaveFlag = (summ > 0.0001);
		if (mMiSave != null) mMiSave.setEnabled(mAvailableSaveFlag);
	}


	private void ChangeQty(long goosdsID, float qty, float price, float casesize,long characteristic_id) {
        CharasteristicListAdapter adp = (CharasteristicListAdapter) mAdapter;
		if (qty > 0) {
			if (adp.mCaseShowFlag) qty = qty*casesize;
			adp.mDocData.put(goosdsID, qty, price,characteristic_id);
		} else {
			adp.mDocData.removeByCharacteristicId(goosdsID);
		}
		mAdapter.notifyDataSetChanged();
	}
}