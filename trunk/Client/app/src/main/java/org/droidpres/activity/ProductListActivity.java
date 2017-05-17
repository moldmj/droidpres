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

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.droidpres.R;
import org.droidpres.adapter.ProductListAdapter;
import org.droidpres.db.DBDroidPres;
import org.droidpres.db.QueryHelper;
import org.droidpres.dialog.DocHead;
import org.droidpres.utils.Const;
import org.droidpres.utils.DocData;
import org.droidpres.utils.MenuItemInfo;
import org.droidpres.utils.Utils;
import org.droidpres.utils.onDocDataChange;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.CursorAdapter;
import android.widget.FilterQueryProvider;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class ProductListActivity extends AbsListActivity implements FilterQueryProvider,
		OnKeyListener, OnClickListener,	onDocDataChange {
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
    private static final int DLG_CHARACTERISTIC = 4;

	private TextView mTvDocTotal;
	private Bundle mActivityExtras;
	private MenuItem mMiSave;
	private boolean mDocNewFlag = true;
	private boolean mFiltredFlag = false;
	private boolean mAvailableSaveFlag;
	private float mOldQty = 1;
    private Bundle mProductParams = new Bundle();
	
	public ProductListActivity() {
		super(R.layout.product_list, DBDroidPres.TABLE_PRODUCT,
				new String[] {"_id", "name", "casesize", "productgroup_id","minqty"},
				"name",null);		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mTvDocTotal = (TextView) findViewById(R.id.tvDocTotal);

		ProductListAdapter adp = (ProductListAdapter) mAdapter;
		
		
		//Сроки поджимают. поэтому такой костыль
		if ((mActivityExtras = getIntent().getExtras()) != null) {
				mQueryHelper.appendFilter("pricelist_id", QueryHelper.FILTER_AND, "pricelist_id =" +mActivityExtras.getInt(Const.EXTRA_CLIENT_PRICE_ID));
                mQueryHelper.appendFilter("warehouse_id", QueryHelper.FILTER_AND, "warehouse_id =" +mActivityExtras.getInt(Const.EXTRA_WAREHOUSE));
                mQueryHelper.appendFilter("characteristic_id", QueryHelper.FILTER_AND, "characteristic_id = 0");
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
		mAdapter.setFilterQueryProvider(this);

		getListView().setOnKeyListener(this);
		
		
		findViewById(R.id.tbProdInBox).setOnClickListener(this);
		findViewById(R.id.tbProdPresence).setOnClickListener(this);
        findViewById(R.id.tbProdSelected).setOnClickListener(this);
        findViewById(R.id.tbCategortOpen).setOnClickListener(this);

        showDialog(DLG_PRODUCT_GROUP);

	}

	@Override
	protected CursorAdapter createAdapter(Cursor cursor) {
		return new ProductListAdapter(this,
				R.layout.product_list_item,
				cursor,
                new String[] {"name", "available", "price", "_id"},
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
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_GROUP, Menu.NONE, R.string.lb_product_group).
		setIcon(android.R.drawable.ic_menu_directions);

		menu.add(0, MENU_SEARCH, Menu.NONE, R.string.lb_search).
		setIcon(android.R.drawable.ic_menu_search);

		menu.add(0, MENU_HEAD, Menu.NONE, R.string.lb_title_doc_params).
		setIcon(android.R.drawable.ic_menu_upload);

		mMiSave = menu.add(0, MENU_SAVE, Menu.NONE, R.string.lb_save);
		mMiSave.setIcon(android.R.drawable.ic_menu_save);
		mMiSave.setEnabled(mAvailableSaveFlag);

		menu.add(0, MENU_CLEAN, Menu.NONE, R.string.lb_clean).
		setIcon(android.R.drawable.ic_menu_delete);
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
			((ProductListAdapter) mAdapter).mDocData.clear();
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
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN &&
				mAvailableSaveFlag && !mFiltredFlag) {
			showDialog(DLG_QUERY_SAVE_DOCUMENT);
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
	protected void onListItemClick(ListView l, View v, int pos, long id) {
		super.onListItemClick(l, v, pos, id);

        if (SetupRootActivity.getCharacteristicsuse(this))
        {
            OpenCharacteristicsDialog(id,pos);
            return;
        }
		ProductListAdapter adp = (ProductListAdapter) mAdapter;

		float _qty = adp.mDocData.getQtyByProductId(id);
        float casesize=QueryHelper.fieldByNameFloat(getCursor(), "casesize");
        float minqty=QueryHelper.fieldByNameFloat(getCursor(), "minqty");
		if (_qty > 0) {
            if (adp.mCaseShowFlag) _qty = _qty/casesize;
            else _qty=_qty+(minqty>0?minqty:1)-1;
			ChangeQty(id, _qty + mOldQty, QueryHelper.fieldByNameFloat(getCursor(), "price"),
                    casesize);
		} else {
			ChangeQty(id, mOldQty*(minqty>0&&!adp.mCaseShowFlag?minqty:1), QueryHelper.fieldByNameFloat(getCursor(), "price"),
                    casesize);
		}
	}

    private void OpenCharacteristicsDialog(long productId,int position)
    {
        Cursor cur = getCursor();
        cur.moveToPosition(position);
        mProductParams.putLong(Const.EXTRA_PRODUCT_ID, productId);
        mProductParams.putString(Const.EXTRA_PRODUCT_NAME, QueryHelper.fieldByNameString(cur, "name"));
        mProductParams.putLong(Const.EXTRA_WAREHOUSE,mActivityExtras.getInt(Const.EXTRA_WAREHOUSE));

//        showDialog(DLG_CHARACTERISTIC);

        ProductListAdapter adp = (ProductListAdapter) mAdapter;
        Intent document = new Intent(this, CharacteristicListActivity.class);
        document.putExtras(mProductParams);
        document.putExtra("docdata",adp.mDocData);
        startActivityForResult(document,1);
    }


    @Override
	protected List<MenuItemInfo> createContextMenus(int position, long id) {
        if (SetupRootActivity.getCharacteristicsuse(this))
        {
            return null;
        }
		Cursor cur = getCursor();
		ProductListAdapter adp = (ProductListAdapter) mAdapter;

		cur.moveToPosition(position);
		Intent intent = new Intent(this, InputQtyActivity.class);
		intent.putExtra(Const.EXTRA_PRODUCT_NAME, QueryHelper.fieldByNameString(cur, "name"));
		intent.putExtra(Const.EXTRA_PRODUCT_ID, id);
		intent.putExtra(Const.EXTRA_PRICE, QueryHelper.fieldByNameFloat(cur, "price"));
		intent.putExtra(Const.EXTRA_CASESIZE, QueryHelper.fieldByNameFloat(cur, "casesize"));

		float tqty = adp.mDocData.getQtyByProductId(id);
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
	 * При нажатии на Button
	 */
	public void onClick(View v) {
		ProductListAdapter adp = (ProductListAdapter) mAdapter;
		switch (v.getId()) {
		case R.id.tbProdInBox:
			
			
			adp.mCaseShowFlag = ((ToggleButton)v).isChecked();
			if (adp.mCaseShowFlag) adp.nf = SetupRootActivity.getQtyFormat(this, getString(R.string.lb_qty_box));
			else adp.nf = SetupRootActivity.getQtyFormat(this, getString(R.string.lb_qty));
			adp.notifyDataSetChanged();
			break;
		case R.id.tbProdPresence:
			if (((ToggleButton)v).isChecked()) {
				mQueryHelper.appendFilter("QTY", QueryHelper.FILTER_AND, "Products_available.available > 0");
			} else {
				mQueryHelper.removeFilter("QTY");
			}
			mAdapter.changeCursor(createCursor());
			break;
        case R.id.tbProdSelected:
        //	if (((ToggleButton)v).isChecked()) {
			//	mQueryHelper.appendFilter("QTY", QueryHelper.FILTER_AND, "available > 0");
			//} else {
				//mQueryHelper.removeFilter("QTY");
        	
        	if (((ToggleButton)v).isChecked()) {
        		
        		ListAdapter adapter = (ListAdapter)getListView().getAdapter();
                 
                 
                 String filterText=new String();
                 
                 for(int i = 0; i < adapter.getCount(); i++)
                 {                                         
                	 android.database.sqlite.SQLiteCursor cursor =(android.database.sqlite.SQLiteCursor) adapter.getItem(i);  
                		if (cursor.moveToFirst()) {
                			do {				
                				float _qty = adp.mDocData.getQtyByProductId(cursor.getLong(0));
                				if(_qty>0){
                					filterText=filterText+" OR product._id="+cursor.getLong(0);
                				}
                			} while (cursor.moveToNext());                			
                		}
                		
                		cursor.close();                      
                 }
                 
				mQueryHelper.appendFilter("product_id", QueryHelper.FILTER_AND, "(product._id = 0"+filterText+")");
			} else {
				mQueryHelper.removeFilter("product_id");
			}
			mAdapter.changeCursor(createCursor());
        	
           break;
            case R.id.tbCategortOpen:
                showDialog(DLG_PRODUCT_GROUP);
                break;


		}
	}

	public Cursor runQuery(CharSequence constraint) {
		String selection = constraint.toString().trim().toUpperCase();
		mFiltredFlag = (selection.length() > 0);
		if (mFiltredFlag) {
			selection = "name_case like ('%" + selection + "%')";
			mQueryHelper.appendFilter("LIKE_NAME", QueryHelper.FILTER_AND, selection);
		} else
			mQueryHelper.removeFilter("LIKE_NAME");
		return createCursor();
	}

	/**
	 * Генерация диалогов
	 */
	@Override
	protected Dialog onCreateDialog(int dialogID) {
		super.onCreateDialog(dialogID);

		switch (dialogID) {
		case DLG_QUERY_SAVE_DOCUMENT: // Диалог запроса на сохранение документа
			return new AlertDialog.Builder(this)
			.setTitle(android.R.string.dialog_alert_title)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setMessage(R.string.msg_QyerySaveDocument)
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog,	int whichButton) {
					showDialog(DLG_QUERY_STATE_DOCUMENT);
				}

			})
			.setNegativeButton(R.string.no,  new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog,	int whichButton) {
					ProductListActivity.this.finish();
				}

			})
			.create();  // END: Диалог запроса на сохранение документа
		case DLG_QUERY_STATE_DOCUMENT: // Диалог запроса статуса документа
			return new AlertDialog.Builder(this).
			setTitle(R.string.lb_documents_status).
			setItems(R.array.itemDocState, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog, int item) {
					SaveDocument(item);
				}

			}).create();

		case DLG_PRODUCT_GROUP: // Диалог группы товара
			Cursor cursor = mDataBase.rawQuery("select _id, name from " +
					DBDroidPres.TABLE_PRODUCT_GROUP + "\n" +
					"where _id in (select distinct productgroup_id from product) order by name", null);

			final CharSequence[] names = new CharSequence[cursor.getCount()+1];
			final int[] ids = new int[cursor.getCount()+1];
			names[0] = "Вся продукция";
			ids[0] = 0;

			int i = 1;
			if (cursor.moveToFirst())
				do {
					ids[i] = cursor.getInt(0);
					names[i++] = cursor.getString(1);
				} while (cursor.moveToNext());
			cursor.close();

			Dialog dlg = new AlertDialog.Builder(this).
			setTitle(R.string.lb_product_group).
			setItems(names, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					if (item > 0) {
						mQueryHelper.appendFilter("GR", QueryHelper.FILTER_AND, "productgroup_id = %d", ids[item]);
						getListView().setFastScrollEnabled(false);
					} else {
						mQueryHelper.removeFilter("GR");
						getListView().setFastScrollEnabled(true);
					}
					mAdapter.changeCursor(createCursor());
				}
			}).create();
			return dlg;

		case DLG_DOCHEAD:
			return new DocHead(this, new DialogInterface.OnClickListener() {

				public void onClick(DialogInterface dialog,	int whichButton) {
					if (whichButton == DialogInterface.BUTTON_POSITIVE) {
						mActivityExtras.putInt(Const.EXTRA_DOC_PRESVEN, ((DocHead) dialog).getPresVenType());
						mActivityExtras.putInt(Const.EXTRA_DOC_TYPE, ((DocHead) dialog).getDocTypeID());
                        mActivityExtras.putInt(Const.EXTRA_WAREHOUSE, ((DocHead) dialog).getDocTypeID());
						mActivityExtras.putString(Const.EXTRA_DOC_DESC, ((DocHead) dialog).getDocDesc());
					}
				}

			});

		default:
			return null;
		}
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
					data.getFloatExtra(Const.EXTRA_PRICE, 0), data.getFloatExtra(Const.EXTRA_CASESIZE, 0));
			break;
            case 1:
                ProductListAdapter adp = (ProductListAdapter) mAdapter;
                adp.mDocData=(DocData) data.getParcelableExtra("docdata");
                onDataChange(adp.mDocData.getSumm());
                mAdapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		ProductListAdapter adp = (ProductListAdapter) mAdapter;
		outState.putParcelable(Utils.getConstName(mAdapter.getClass(), "mDocData"), adp.mDocData);
	}


	public void onDataChange(float summ) {
		mTvDocTotal.setText(((ProductListAdapter) mAdapter).cf.format(summ));
		mAvailableSaveFlag = (summ > 0.0001);
		if (mMiSave != null) mMiSave.setEnabled(mAvailableSaveFlag);
	}

	private void SaveDocument(int doc_state) {
		ProductListAdapter adp = (ProductListAdapter) mAdapter;
		Cursor cur_type_doc = QueryHelper.createCurcorOneRecord(DBDroidPres.TABLE_TYPEDOC, mDataBase,
				mActivityExtras.getInt(Const.EXTRA_DOC_TYPE, 0), "paytype", "days");

		Calendar cl = Calendar.getInstance();
		cl.add(Calendar.DAY_OF_MONTH, cur_type_doc.getInt(1));
		Date sql_date = new Date(cl.getTime().getTime());

		ContentValues cval = new ContentValues();
		cval.put("presventype",	mActivityExtras.getInt(Const.EXTRA_DOC_PRESVEN, 0));
		cval.put("client_id", 	mActivityExtras.getLong(Const.EXTRA_CLIENT_ID, 0));
		cval.put("itemcount", 	adp.mDocData.getQtySumm());
		cval.put("mainsumm", 	adp.mDocData.getSumm());
		cval.put("description", 	mActivityExtras.getString(Const.EXTRA_DOC_DESC).trim());
		cval.put("typedoc_id",	mActivityExtras.getInt(Const.EXTRA_DOC_TYPE, 0));
        cval.put("warehouse_id",	mActivityExtras.getInt(Const.EXTRA_WAREHOUSE, 0));
		cval.put("paytype",		cur_type_doc.getInt(0));
		cval.put("docstate", 	doc_state);
		cval.put("paymentdate",	sql_date.toString());
		cval.put("docdate",mActivityExtras.getString(Const.EXTRA_DOC_DATE));
		cval.put("isdiscount",mActivityExtras.getInt(Const.EXTRA_DOC_ISDISCOUNT));
				
		cur_type_doc.close();

		long _id = 0;
		if (! mDocNewFlag) {
			_id = mActivityExtras.getLong(Const.EXTRA_DOC_ID);
			mDataBase.update(DBDroidPres.TABLE_DOCUMENT, cval, QueryHelper.KEY_ID + "=" + _id, null);
		} else	_id = mDataBase.insert(DBDroidPres.TABLE_DOCUMENT, null, cval);

		if (_id > 0) {
			if (! mDocNewFlag)
				mDataBase.delete(DBDroidPres.TABLE_DOCUMENT_DET, "document_id = " + _id, null);
			adp.mDocData.Save(_id, mDataBase);
		}

		// TODO: Сделать с этим что то
		switch (doc_state) {
		case 0:
			Utils.ToastMsg(this, "Документ сохранен до следующего изменения.");
			break;
		case 1:
			Utils.ToastMsg(this, "Документ сохранен и подготовлен к отправке.");
			break;
		}
		finish();
	}

	private void ChangeQty(long goosdsID, float qty, float price, float casesize) {
		ProductListAdapter adp = (ProductListAdapter) mAdapter;
		if (qty > 0) {
			if (adp.mCaseShowFlag) qty = qty*casesize;
			adp.mDocData.put(goosdsID, qty, price,null);
		} else {
			adp.mDocData.removeByProductId(goosdsID);
		}
		mAdapter.notifyDataSetChanged();
	}


}