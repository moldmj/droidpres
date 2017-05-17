using System;
using CookComputing.XmlRpc;

namespace DroidPresModB.Server.Core.Interface
{
    public struct ClientGroupStruct
    {
        public int _id;
        public string name;
    }

    public struct ProductGroupStruct
    {
        public int _Id;
        public int parentgroup_id;
        public string name;
    }

    public struct PriceStruct
    {
        public int _Id;
        public string name;
    }

    public struct WarehouseStruct
    {
        public int _Id;
        public string name;
    }

    public struct ProductsAvailableStruct
    {
        public int warehouse_id;
        public int product_id;
        public int characteristic_id;
        public double available;
    }

    public struct ProductsPricesStruct
    {
        public int product_id;
        public int pricelist_id;
        public double price;
    }



    public struct ClientStruct
    {
        public string Address;
        public string Addresslaw;
        public string Bankname;
        public int Debtdays1;
        public int Debtdays2;
        public double Debtsumm1;
        public double Debtsumm2;
        public double Defaultdiscount;
        public int pricelist_id;
        public string Fname;
        public string Mfo;
        public string Name;
        public string Okpo;
        public string Phone;
        public bool Stopshipment;
        public string Taxcode;
        public string Taxnum;
        public int _id;
        public int category_id;
        public string clientgroup_id;
        public string parent_id;
    }

    public struct TypeDocStruct
    {
        public int Days;
        public double Discount;
        public string Name;
        public int Paytype;
        public int Paytype1Or2;
        public int _id;
    }

    public struct ProductStruct
    {
        //public double Available;
        public double Casesize;
        public string Name;
        //public double Price;
        public int Sortorder;
        public int _id;
        public int productgroup_id;
        public double minqty;
    }


    public struct CharacteristicStruct
    {   
        public double Casesize;
        public string Name;
        public int _id;
        public int product_id;
    }

    public struct DocStruct
    {
        public int _id;
        public int agent_id;
        public int client_id;
        public string description;
        public DateTime docdate;
        public DateTime doctime;
        public double itemcount;
        public double mainsumm;
        public DateTime paymentdate;
        public int paytype;
        public int paytype1or2;
        public int presventype;
        public int typedoc_id;
        public int isdiscount;
        public int warehouse_id ;
    }

    public struct DocDetStruct
    {
        public double price;
        public int product_id;
        public int characteristic_id;
        public double qty;
    }

    public struct LocationStruct
    {
        public int accuracy;
        public string date_location;
        public int lat;
        public int lon;
        public string provider;
    }
}