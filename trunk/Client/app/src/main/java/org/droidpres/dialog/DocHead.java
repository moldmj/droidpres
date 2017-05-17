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
package org.droidpres.dialog;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.droidpres.R;
import org.droidpres.activity.SetupRootActivity;
import org.droidpres.db.DBDroidPres;
import org.droidpres.db.SpinnerDB;

import android.R.bool;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

/**
 * Диалог для заполнения шапки документа
 * @author Eugene Vorobkalo
 *
 */
public class DocHead extends AlertDialog {
	public static final int DOC_PRES = 0;
	public static final int DOC_VEN = 1;
	private DateFormat df;
	private View view;
	private ArrayList<SpinnerDB> itemsDocType;
    private ArrayList<SpinnerDB> itemsWarehouse;
	private Spinner spTypeDoc;
    private Spinner spWarehouse;
	

	public DocHead(Context context, android.content.DialogInterface.OnClickListener listener) {
		super(context);
		setTitle(R.string.lb_title_doc_params);
        itemsDocType = new ArrayList<SpinnerDB>();
        itemsWarehouse= new ArrayList<SpinnerDB>();
		//df = SetupRootActivity.getDateFormat(context);
		df=new SimpleDateFormat("dd.MM.yyyy");
		ArrayAdapter<SpinnerDB> adapter = new ArrayAdapter<SpinnerDB>(context,
                android.R.layout.simple_spinner_item,
                itemsDocType);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		
		SQLiteDatabase db = (new DBDroidPres(context)).Open();
		Cursor cursor = db.query(DBDroidPres.TABLE_TYPEDOC, 
        		new String[] {"_id","name"}, null, null, null, null, null);
		
		if (cursor.moveToFirst())
			do {
                itemsDocType.add(new SpinnerDB(cursor.getInt(0), cursor.getString(1)));
			} while (cursor.moveToNext());
		
		cursor.close();

        ArrayAdapter<SpinnerDB> adapter2 = new ArrayAdapter<SpinnerDB>(context,
                android.R.layout.simple_spinner_item,
                itemsWarehouse);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        Cursor cursor2 = db.query(DBDroidPres.TABLE_WAREHOUSE,
                new String[] {"_id","name"}, null, null, null, null, null);

        if (cursor2.moveToFirst())
            do {
                itemsWarehouse.add(new SpinnerDB(cursor2.getInt(0), cursor2.getString(1)));
            } while (cursor2.moveToNext());
        cursor2.close();

		db.close();

		LayoutInflater inflater = LayoutInflater.from(context);
		view = inflater.inflate(R.layout.dlg_document_head, null);
		
		((RadioGroup) view.findViewById(R.id.rgPresVen)).check(R.id.rbPres);
		spTypeDoc = (Spinner) view.findViewById(R.id.spTypeDoc);
        spTypeDoc.setAdapter(adapter);
        spTypeDoc.getSelectedItemPosition();


        spWarehouse = (Spinner) view.findViewById(R.id.spWarehouse);
        spWarehouse.setAdapter(adapter2);
        spWarehouse.getSelectedItemPosition();



		
		setView(view);
		//setIcon(android.R.drawable.ic_dialog_map);
		setButton(DialogInterface.BUTTON_POSITIVE, context.getText(android.R.string.ok), listener);
		setButton(DialogInterface.BUTTON_NEGATIVE, context.getText(android.R.string.cancel), listener);
	}
	
	public int getPresVenType() {
		if ( ((RadioButton) view.findViewById(R.id.rbPres)).isChecked() )
			return DOC_PRES;
		else 
			return DOC_VEN;
	}
	
	public void setPresVenType(int val) {
		((RadioButton) view.findViewById(R.id.rbPres)).setChecked(val == DOC_PRES);
	}

	public int getDocTypeID() {
		return itemsDocType.get(spTypeDoc.getSelectedItemPosition()).id ;
	}

    public int getWarehouseID() {
        return itemsWarehouse.get(spWarehouse.getSelectedItemPosition()).id ;
    }
	
	public void setDocTypeID(int val) {
		for (int i = 0; i < itemsDocType.size(); i++) {
			if (itemsDocType.get(i).id == val ) {
				spTypeDoc.setSelection(i);
				break;
			}
		}
	}


    public void setWarwhouseID(int val) {
        for (int i = 0; i < itemsWarehouse.size(); i++) {
            if (itemsWarehouse.get(i).id == val ) {
                spWarehouse.setSelection(i);
                break;
            }
        }
    }

	public String getDocDesc() {
		return ((EditText) view.findViewById(R.id.eDescription)).getText().toString();
	}
	
	public String getDocDate() {
		DatePicker picker= ((DatePicker) view.findViewById(R.id.DocumentDateTimePicket));
		java.util.Date date1= new java.util.Date (picker.getYear()-1900, picker.getMonth(), picker.getDayOfMonth());
		try {
			return df.format(date1);
		} catch (Exception e) {
			return date1.toString();
		}
		
	}
	
	public void setDocDesc(String val) {
		((EditText) view.findViewById(R.id.eDescription)).setText(val);
	}
	
	public void setDocDate(String val) {
		DatePicker picker= ((DatePicker) view.findViewById(R.id.DocumentDateTimePicket));
		//java.util.Date date1=(java.util.Date) new java.text.SimpleDateFormat("yyyy-MM-dd").parse(val);
		
	}
	
	
}
