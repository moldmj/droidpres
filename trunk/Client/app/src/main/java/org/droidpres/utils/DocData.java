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
package org.droidpres.utils;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.droidpres.db.DBDroidPres;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;


public class DocData implements Parcelable {
	private static final int QTY = 0;
	private static final int PRICE = 1;
	private static final int minqty = 2;


	private onDocDataChange DataChange;
	//private Map<Long, float[]> map = new HashMap<Long, float[]>();
    private DocProducts map=new DocProducts();
	private float summ;
	private boolean bUbdateFlag;

	public DocData() {
		this.DataChange = null;
		summ = 0;
        map.products=new ArrayList<DocProduct>();
	}
	
	public DocData(onDocDataChange DataChange) {
		this.DataChange = DataChange;
		summ = 0;
	}

	public DocData(Parcel in) {
        this.DataChange = null;
		summ = 0;
		readFromParcel(in);
	}
	
	public void setOnDocDataChange(onDocDataChange DataChange) {
		this.DataChange = DataChange;
	}
	
	public void put(Long product_id, float qty, float price,Long characteristic_id) {
		summ -= getRecSummByProductAndCharacteristicId(product_id, characteristic_id);
		map.putByProductAndCharacteristicId(product_id, characteristic_id, qty, price);
		summ += price * qty;
		if (DataChange != null && !bUbdateFlag) DataChange.onDataChange(summ);
	}

    private float getRecSummByProductAndCharacteristicId(Long product_id, Long characteristic_id)
    {
        DocProduct prod=map.getByProductAndCharacteristicId(product_id,characteristic_id);
        if (prod==null)
        {
            return 0;
        }
        return prod.Price*prod.QTY;
    }

    public DocProduct getProdyctByProductId(Long product_id) {
		return map.getByProductId(product_id);
	}

	public float getPriceByProductId(Long product_id) {
        DocProduct val = map.getByProductId(product_id);
		if (val != null) return val.Price;
		else return 0;
	}

	public float getQtyByProductId(Long product_id) {
        DocProduct val = map.getByProductId(product_id);
		if (val != null) return val.QTY;
		else return 0;
	}

    public float getQtyByCharacteristicId(long characteristic_id) {
        DocProduct val = map.getByCharacteristicId(characteristic_id);
        if (val != null) return val.QTY;
        else return 0;
    }

	public float getQtySumm() {
		float result = 0;
		for (DocProduct val: map.products)
			result += val.QTY;
		return result;
	}

	public float getRecSummByProductId(Long product_id) {
		DocProduct val = map.getByProductId(product_id);
		if (val != null) return (val.Price * val.QTY);
		else return 0;
	}

	public float getSumm() {
		return summ;
	}

	public int getRecCount() {
		return map.products.size();
	}
	
	public void removeByProductId(Long product_id) {
		summ -= getRecSummByProductId(product_id);
		map.removeByProductId(product_id);
		if (DataChange != null && !bUbdateFlag) DataChange.onDataChange(summ);
	}
	
	public void clear() {
		map.products.clear();
		summ = 0;
		if (DataChange != null && !bUbdateFlag) DataChange.onDataChange(summ);
	}

	public void StartUpdate() {
		bUbdateFlag = true;
	}

	public void StopUpdate() {
		bUbdateFlag = false;
		if (DataChange != null) DataChange.onDataChange(summ);
	}
	
	public void Load(long id, SQLiteDatabase db) {
		Cursor cursor = db.query(DBDroidPres.TABLE_DOCUMENT_DET,
				new String[] {"product_id", "qty", "price","characteristic_id"},
				"document_id = " + id, null, null, null, null,null);

		if (cursor.moveToFirst()) {
			StartUpdate();
			do {				
				put(cursor.getLong(0), cursor.getFloat(1), cursor.getFloat(2), cursor.getLong(3));
			} while (cursor.moveToNext());
			StopUpdate();
		}
		cursor.close();
	}
	
	public void Save(long document_id, SQLiteDatabase db) {
		ContentValues _val = new ContentValues();
		for (DocProduct curDocprod: map.products) {
			_val.put("document_id", document_id);
			_val.put("product_id", curDocprod.ProductId);
            _val.put("characteristic_id", curDocprod.CharacteristicId);
			_val.put("qty", curDocprod.QTY);
			_val.put("price", curDocprod.Price);
			db.insert(DBDroidPres.TABLE_DOCUMENT_DET, null, _val);
			_val.clear();
		}
	}
	
	public int describeContents() {
		return 0;
	}

     public void writeToParcel(Parcel dest,int i) {
		dest.writeInt(map.products.size());
        for (DocProduct curDocprod: map.products) {
			dest.writeLong(curDocprod.ProductId);
            dest.writeLong(curDocprod.CharacteristicId);
			dest.writeFloat(curDocprod.QTY);
            dest.writeFloat(curDocprod.Price);
		}
	}


    public static final Parcelable.Creator<DocData> CREATOR = new Parcelable.Creator<DocData>() {
        // распаковываем объект из Parcel
        public DocData createFromParcel(Parcel in) {
            return new DocData(in);
        }
        public DocData[] newArray(int size) {
            return new DocData[size];
        }
    };

	public void readFromParcel(Parcel in) {
        map=new DocProducts();
        map.products=new ArrayList<DocProduct>();
		summ = 0;
		int count = in.readInt();
		for (int i = 0; i < count; i++) {
            DocProduct newDoc=new DocProduct();
            newDoc.ProductId=in.readLong();
            newDoc.CharacteristicId=in.readLong();
            newDoc.QTY=in.readFloat();
            newDoc.Price=in.readFloat();
            summ += newDoc.Price*newDoc.QTY;
            map.products.add(newDoc);
        }
	}

    public void removeByCharacteristicId(long characteristic_id)
    {
        map.removeByCharacteristicId(characteristic_id);
    }


    public class MyCreator implements Parcelable.Creator<DocData> {
	      public DocData createFromParcel(Parcel source) {
	            return new DocData(source);
	      }
	      public DocData[] newArray(int size) {
	            return new DocData[size];
	      }
	}

    public class DocProducts{
        List<DocProduct> products;
        public void putByProductAndCharacteristicId(Long _productId,Long _characteristicId,Float _qty,Float _price){
            removeByProductAndCharacteristicId(_productId, _characteristicId);
            DocProduct newDoc=new DocProduct();

            newDoc.ProductId=_productId;
            newDoc.CharacteristicId=_characteristicId;
            newDoc.QTY=_qty;
            newDoc.Price=_price;
            products.add(newDoc);
        }

        private void removeByProductAndCharacteristicId(Long productId, Long characteristicId)
        {
            for (DocProduct curProd:products)
            {
                if (curProd.ProductId.intValue()==productId.intValue()&&curProd.CharacteristicId.intValue()==characteristicId.intValue())
                {
                    products.remove(curProd);
                    removeByProductAndCharacteristicId(productId, characteristicId);
                    return;
                }
            }
        }

        public DocProduct getByProductAndCharacteristicId(Long product_id,Long characteristic_id) {
            for (DocProduct curProd:products)
            {
                if (curProd.ProductId.intValue()==product_id.intValue()&&curProd.CharacteristicId.intValue()==characteristic_id.intValue())
                {
                    return curProd;
                }
            }
            return null;
        }

        public DocProduct getByProductId(Long product_id) {
            for (DocProduct curProd:products)
            {
                if (curProd.ProductId.intValue()==product_id.intValue())
                {
                    return curProd;
                }
            }
            return null;
        }

        public DocProduct getByCharacteristicId(Long characteristic_id) {
            for (DocProduct curProd:products)
            {
                if (curProd.CharacteristicId.intValue()==characteristic_id.intValue())
                {
                    return curProd;
                }
            }
            return null;
        }


        public void removeByProductId(Long product_id)
        {
            for (DocProduct curProd:products)
            {
                if (curProd.ProductId.intValue()==product_id.intValue())
                {
                    products.remove(curProd);
                    removeByProductId(product_id);
                    return;
                }
            }
        }


        public void removeByCharacteristicId(Long characteristic_id)
        {
            for (DocProduct curProd:products)
            {
                if (curProd.CharacteristicId.intValue()==characteristic_id.intValue())
                {
                    products.remove(curProd);
                    removeByCharacteristicId(characteristic_id);
                    return;
                }
            }
        }
    }

    public class DocProduct{
        public Long ProductId;
        public Long CharacteristicId;
        public Float QTY;
        public Float Price;

    }
}
