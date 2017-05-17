using System;
using System.Collections;
using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Reflection;
using CookComputing.XmlRpc;
using DroidPresModB.Server.Core.Interface;
using DroidPresModB.Server.XmlRpcServer.Config;

namespace DroidPresModB.Server.Core.ERPSystemConnectors
{
    public class ErpOneS77Connector : MarshalByRefObject, IErpSystemConnector
    {
        private readonly string _connectionString;
        private readonly string _oleObjectName;
        public bool Connecting;
        private object _v7;

        public ErpOneS77Connector()
        {
            _connectionString = ConfigManager.Config.ConnectionString1C;
            _oleObjectName = ConfigManager.Config.OleObjectName;
            Connecting = false;
            Connect();
        }

        #region IErpSystemConnector Members

        public void Connect()
        {
            if (Connecting)
            {
                return;
            }
            Type t = Type.GetTypeFromProgID(_oleObjectName);
            _v7 = Activator.CreateInstance(t);
            var arg = new Object[3];
            arg[0] = _v7.GetType().InvokeMember(@"RMTrade",
                                                BindingFlags.Public | BindingFlags.InvokeMethod, null, _v7, null);
            arg[1] = _connectionString;
            Connecting = (Boolean) _v7.GetType().InvokeMember(@"Initialize",
                                                              BindingFlags.Public | BindingFlags.InvokeMethod, null, _v7,
                                                              arg);
        }


        public void Disconnect()
        {
            _v7 = null;
        }

        public ClientGroupStruct[] GetRefClientGroup(int agentId)
        {
            object result = ExcecFunc(_v7, "GetRefClientGroup", new object[] {agentId});
            IEnumerable<string> listrez = ParseResponse(result);
            List<ClientGroupStruct> listStruct =
                GenerateStruct(listrez, (new ClientGroupStruct()).GetType()).Cast<ClientGroupStruct>().ToList();
            return listStruct.ToArray();
        }

        public byte[] GetUpdateApp(int currentVersion)
        {
            return new byte[] {0};
        }

        public TypeDocStruct[] GetRefTypeDoc(int agentId)
        {
            object result = ExcecFunc(_v7, "GetRefTypeDoc", new object[] {agentId});
            IEnumerable<string> listrez = ParseResponse(result);
            List<TypeDocStruct> listStruct =
                GenerateStruct(listrez, (new TypeDocStruct()).GetType()).Cast<TypeDocStruct>().ToList();
            return listStruct.ToArray();
        }

        public ClientStruct[] GetRefClient(int agentId)
        {
            object result = ExcecFunc(_v7, "GetRefClient", new object[] {agentId});
            IEnumerable<string> listrez = ParseResponse(result);
            List<ClientStruct> listStruct =
                GenerateStruct(listrez, (new ClientStruct()).GetType()).Cast<ClientStruct>().ToList();
            return listStruct.ToArray();
        }

        public ProductGroupStruct[] GetRefProductGroup(int agentId)
        {
            object result = ExcecFunc(_v7, "GetRefProductGroup", new object[] {agentId});
            IEnumerable<string> listrez = ParseResponse(result);
            List<ProductGroupStruct> listStruct =
                GenerateStruct(listrez, (new ProductGroupStruct()).GetType()).Cast<ProductGroupStruct>().ToList();
            return listStruct.ToArray();
        }

        public ProductStruct[] GetRefProduct(int agentId)
        {
            object result = ExcecFunc(_v7, "GetRefProduct", new object[] {agentId});
            IEnumerable<string> listrez = ParseResponse(result);
            List<ProductStruct> listStruct =
                GenerateStruct(listrez, (new ProductStruct()).GetType()).Cast<ProductStruct>().ToList();
            return listStruct.ToArray();
        }

        public CharacteristicStruct[] GetRefcharacteristic(int agentId)
        {
            throw new NotImplementedException();
        }

        public PriceStruct[] GetRefPrices(int agentId)
        {
            object result = ExcecFunc(_v7, "GetRefPrices", new object[] { agentId });
            IEnumerable<string> listrez = ParseResponse(result);
            List<PriceStruct> listStruct =
                GenerateStruct(listrez, (new PriceStruct()).GetType()).Cast<PriceStruct>().ToList();
            return listStruct.ToArray();
        }

        public ProductsPricesStruct[] GetRefProductPrices(int agentId)
        {
            object result = ExcecFunc(_v7, "GetRefProductPrices", new object[] { agentId });
            IEnumerable<string> listrez = ParseResponse(result);
            List<ProductsPricesStruct> listStruct =
                GenerateStruct(listrez, (new ProductsPricesStruct()).GetType()).Cast<ProductsPricesStruct>().ToList();
            return listStruct.ToArray();
        }

        public WarehouseStruct[] GetRefWarehouse(int agentId)
        {
            object result = ExcecFunc(_v7, "GetRefWarehouse", new object[] { agentId });
            IEnumerable<string> listrez = ParseResponse(result);
            List<WarehouseStruct> listStruct =
                GenerateStruct(listrez, (new WarehouseStruct()).GetType()).Cast<WarehouseStruct>().ToList();
            return listStruct.ToArray();
        }

        public ProductsAvailableStruct[] GetRefProductsAvailable(int agentId)
        {
            object result = ExcecFunc(_v7, "GetRefProductsAvailable", new object[] { agentId });
            IEnumerable<string> listrez = ParseResponse(result);
            List<ProductsAvailableStruct> listStruct =
                GenerateStruct(listrez, (new ProductsAvailableStruct()).GetType()).Cast<ProductsAvailableStruct>().ToList();
            return listStruct.ToArray();
        }

        public int SetDoc(XmlRpcStruct docHeader, XmlRpcStruct[] docDetStruct)
        {
            object result = ExcecFunc(_v7, "SetDoc",
                                      new[]
                                          {
                                              ConvertStructToValueList1C(docHeader),
                                              ConvertStructArrayToValueList1C(docDetStruct)
                                          });
            return Convert.ToInt32(result);
        }

        public bool SetLocation(int agentId, LocationStruct[] location)
        {
            return true;
        }

        public string Registration()
        {
            return "1c7.7";
        }

        #endregion

        private IEnumerable<string> ParseResponse(object listFromOneS)
        {
            decimal countrez = decimal.Parse(ExcecFunc(listFromOneS, "РазмерСписка", new object[0]).ToString());

            var listrez = new List<string>();
            for (int i = 1; i <= countrez; i++)
            {
                string currentValue = ExcecFunc(listFromOneS, "ПолучитьЗначение", new object[] {i}).ToString();
                listrez.Add(currentValue);
            }
            return listrez;
        }

        private IEnumerable<object> GenerateStruct(IEnumerable<string> listrez, Type type)
        {
            var listResult = new List<object>();


            foreach (string currentRez in listrez)
            {
                object clientGroupBoxed = Activator.CreateInstance(type);

                foreach (FieldInfo currentProperty in clientGroupBoxed.GetType().GetFields())
                {
                    string[] groupsValue =
                        currentRez.Replace('{', ' ').Replace('}', ' ').Replace("'".First(), ' ').Split(',');
                    foreach (string s in groupsValue)
                    {
                        if (clientGroupBoxed.GetType().GetField(currentProperty.Name).GetValue(clientGroupBoxed) == null)
                        {
                            if (currentProperty.FieldType.Name.ToLower() == "int32")
                            {
                                clientGroupBoxed.GetType().GetField(currentProperty.Name).SetValue(clientGroupBoxed,
                                                                                                   Convert.ToInt32(0));
                            }

                            if (currentProperty.FieldType.Name.ToLower() == "string")
                            {
                                clientGroupBoxed.GetType().GetField(currentProperty.Name).SetValue(clientGroupBoxed,
                                                                                                   string.Empty);
                            }

                            if (currentProperty.FieldType.Name.ToLower() == "double")
                            {
                                clientGroupBoxed.GetType().GetField(currentProperty.Name).SetValue(clientGroupBoxed,
                                                                                                   double.Parse("0"));
                            }

                            if (currentProperty.FieldType.Name.ToLower() == "boolean")
                            {
                                clientGroupBoxed.GetType().GetField(currentProperty.Name).SetValue(clientGroupBoxed,
                                                                                                   false);
                            }
                        }


                        if (s.Trim().ToLower().StartsWith(currentProperty.Name.Trim().ToLower()))
                        {
                            string value = s.Split(':').Last().Trim();

                            if (currentProperty.FieldType.Name.ToLower() == "int32")
                            {
                                clientGroupBoxed.GetType().GetField(currentProperty.Name).SetValue(clientGroupBoxed,
                                                                                                   Convert.ToInt32(
                                                                                                       value));
                            }

                            if (currentProperty.FieldType.Name.ToLower() == "string")
                            {
                                clientGroupBoxed.GetType().GetField(currentProperty.Name).SetValue(clientGroupBoxed,
                                                                                                   value);
                            }

                            if (currentProperty.FieldType.Name.ToLower() == "double")
                            {
                                clientGroupBoxed.GetType().GetField(currentProperty.Name).SetValue(clientGroupBoxed,
                                                                                                   double.Parse(
                                                                                                       value,
                                                                                                       CultureInfo.
                                                                                                           InvariantCulture));
                            }

                            if (currentProperty.FieldType.Name.ToLower() == "boolean")
                            {
                                clientGroupBoxed.GetType().GetField(currentProperty.Name).SetValue(clientGroupBoxed,
                                                                                                   value == "0");
                            }
                        }
                    }
                }
                listResult.Add(clientGroupBoxed);
            }
            return listResult;
        }

        private object ExcecFunc(object obj, string nameFunction, object[] arguments)
        {
            return obj.GetType().InvokeMember(nameFunction,
                                              BindingFlags.InvokeMethod, null, obj, arguments);
        }

        private object ConvertStructArrayToValueList1C(IEnumerable<XmlRpcStruct> obj)
        {
            object valueList1C = ExcecFunc(_v7, "CreateObject", new object[] {"СписокЗначений"});
            foreach (XmlRpcStruct currentobj in obj)
            {
                ExcecFunc(valueList1C, "AddValue",
                          new[] {ConvertStructToValueList1C(currentobj)});
            }
            return valueList1C;
        }

        private object ConvertStructToValueList1C(XmlRpcStruct obj)
        {
            object valueList1C = ExcecFunc(_v7, "CreateObject", new object[] {"СписокЗначений"});

            foreach (DictionaryEntry currentobj in obj)
            {
                ExcecFunc(valueList1C, "AddValue",
                          new object[] {currentobj.Value.ToString(), currentobj.Key.ToString()});
            }
            return valueList1C;
        }
    }
}