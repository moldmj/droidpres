using System;
using CookComputing.XmlRpc;

namespace DroidPresModB.Server.Core.Interface
{
    public interface IErpSystemConnector
    {
        void Connect();
        void Disconnect();

        [XmlRpcMethod("GetRefClientGroup")]
        ClientGroupStruct[] GetRefClientGroup(int agentId);

        [XmlRpcMethod("GetUpdateApp")]
        Byte[] GetUpdateApp(int currentVersion);

        [XmlRpcMethod("GetRefTypeDoc")]
        TypeDocStruct[] GetRefTypeDoc(int agentId);

        [XmlRpcMethod("GetRefClient")]
        ClientStruct[] GetRefClient(int agentId);

        [XmlRpcMethod("GetRefProductGroup")]
        ProductGroupStruct[] GetRefProductGroup(int agentId);

        [XmlRpcMethod("GetRefProduct")]
        ProductStruct[] GetRefProduct(int agentId);

        [XmlRpcMethod("GetRefCharacteristic")]
        CharacteristicStruct[] GetRefcharacteristic(int agentId);

        [XmlRpcMethod("GetRefPrices")]
        PriceStruct[] GetRefPrices(int agentId);

        [XmlRpcMethod("GetRefProductPrices")]
        ProductsPricesStruct[] GetRefProductPrices(int agentId);


        [XmlRpcMethod("GetRefWarehouse")]
        WarehouseStruct[] GetRefWarehouse(int agentId);

        [XmlRpcMethod("GetRefProductsAvailable")]
        ProductsAvailableStruct[] GetRefProductsAvailable(int agentId);

        [XmlRpcMethod("SetDoc")]
        int SetDoc(XmlRpcStruct docStruct, XmlRpcStruct[] docDetStruct);

        [XmlRpcMethod("SetLocation")]
        bool SetLocation(int agentId, LocationStruct[] location);





        string Registration();
    }


}