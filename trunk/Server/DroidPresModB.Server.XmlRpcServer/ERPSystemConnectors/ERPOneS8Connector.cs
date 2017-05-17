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
    internal class ErpOneS8XConnector : MarshalByRefObject, IErpSystemConnector
    {
        private static dynamic _connection1C8X;
        private static dynamic _droidPresManager;
        private readonly string _connectionString;
        private readonly string _oleObjectName;

        public ErpOneS8XConnector()
        {
            _connectionString = ConfigManager.Config.ConnectionString1C;
            _oleObjectName = ConfigManager.Config.OleObjectName;
        }

        #region IErpSystemConnector Members

        public void Connect()
        {
            try
            {
                Type v8XComConnector = Type.GetTypeFromProgID(_oleObjectName);
                object v8X = Activator.CreateInstance(v8XComConnector);
                Object[] arguments = { _connectionString };
                _connection1C8X = v8XComConnector.InvokeMember("Connect",
                                                               BindingFlags.Public | BindingFlags.InvokeMethod |
                                                               BindingFlags.Static, null, v8X, arguments);
                _droidPresManager = _connection1C8X.Обработки.DroidPresManager.Создать();
            }
            catch (Exception exception)
            {

                throw new Exception(exception.Message + " " + (exception.InnerException != null ? " " + exception.InnerException.Message : ""));
            }
        }

        public void Disconnect()
        {
            _connection1C8X = null;
        }

        public ClientGroupStruct[] GetRefClientGroup(int agentId)
        {
            object result = ExcecFunc(_droidPresManager, "GetRefClientGroup", new object[] { agentId });
            IEnumerable<string> listrez = ParseResponse(result);
            List<ClientGroupStruct> listStruct =
                GenerateStruct(listrez, (new ClientGroupStruct()).GetType()).Cast<ClientGroupStruct>().ToList();
            return listStruct.ToArray();
        }

        public byte[] GetUpdateApp(int currentVersion)
        {
            try
            {
                return ApkManager.GetNewApk(currentVersion);
            }
            catch (Exception)
            {
                return new byte[] { 0 };
            }
           
        }

        public TypeDocStruct[] GetRefTypeDoc(int agentId)
        {
            object result = ExcecFunc(_droidPresManager, "GetRefTypeDoc", new object[] { agentId });
            IEnumerable<string> listrez = ParseResponse(result);
            List<TypeDocStruct> listStruct =
                GenerateStruct(listrez, (new TypeDocStruct()).GetType()).Cast<TypeDocStruct>().ToList();
            return listStruct.ToArray();
        }

        public ClientStruct[] GetRefClient(int agentId)
        {
            try
            {
                object result = ExcecFunc(_droidPresManager, "GetRefClient", new object[] { agentId });
                IEnumerable<string> listrez = ParseResponse(result);
                List<ClientStruct> listStruct =
                    GenerateStruct(listrez, (new ClientStruct()).GetType()).Cast<ClientStruct>().ToList();
                return listStruct.ToArray();
            }
            catch (Exception exception)
            {
                // Log.Error(e.InnerException.Message);
                throw new Exception(exception.Message + " " + (exception.InnerException != null ? " " + exception.InnerException.Message : ""));
            }
        }

        public ProductGroupStruct[] GetRefProductGroup(int agentId)
        {
            object result = ExcecFunc(_droidPresManager, "GetRefProductGroup", new object[] { agentId });
            IEnumerable<string> listrez = ParseResponse(result);
            List<ProductGroupStruct> listStruct =
                GenerateStruct(listrez, (new ProductGroupStruct()).GetType()).Cast<ProductGroupStruct>().ToList();
            return listStruct.ToArray();
        }

        public ProductStruct[] GetRefProduct(int agentId)
        {
            object result = ExcecFunc(_droidPresManager, "GetRefProduct", new object[] { agentId });
            IEnumerable<string> listrez = ParseResponse(result);
            List<ProductStruct> listStruct =
                GenerateStruct(listrez, (new ProductStruct()).GetType()).Cast<ProductStruct>().ToList();
            return listStruct.ToArray();
        }

        public CharacteristicStruct[] GetRefcharacteristic(int agentId)
        {
            object result = ExcecFunc(_droidPresManager, "GetRefCharacteristic", new object[] { agentId });
            IEnumerable<string> listrez = ParseResponse(result);
            List<CharacteristicStruct> listStruct =
                GenerateStruct(listrez, (new CharacteristicStruct()).GetType()).Cast<CharacteristicStruct>().ToList();
            return listStruct.ToArray();
        }

        public PriceStruct[] GetRefPrices(int agentId)
        {
            object result = ExcecFunc(_droidPresManager, "GetRefPrices", new object[] { agentId });
            IEnumerable<string> listrez = ParseResponse(result);
            List<PriceStruct> listStruct =
                GenerateStruct(listrez, (new PriceStruct()).GetType()).Cast<PriceStruct>().ToList();
            return listStruct.ToArray();
        }

        public ProductsPricesStruct[] GetRefProductPrices(int agentId)
        {
            object result = ExcecFunc(_droidPresManager, "GetRefProductPrices", new object[] { agentId });
            IEnumerable<string> listrez = ParseResponse(result);
            List<ProductsPricesStruct> listStruct =
                GenerateStruct(listrez, (new ProductsPricesStruct()).GetType()).Cast<ProductsPricesStruct>().ToList();
            return listStruct.ToArray();
        }

        public WarehouseStruct[] GetRefWarehouse(int agentId)
        {
            object result = ExcecFunc(_droidPresManager, "GetRefWarehouse", new object[] { agentId });
            IEnumerable<string> listrez = ParseResponse(result);
            List<WarehouseStruct> listStruct =
                GenerateStruct(listrez, (new WarehouseStruct()).GetType()).Cast<WarehouseStruct>().ToList();
            return listStruct.ToArray();
        }

        public ProductsAvailableStruct[] GetRefProductsAvailable(int agentId)
        {
            object result = ExcecFunc(_droidPresManager, "GetRefProductsAvailable", new object[] { agentId });
            IEnumerable<string> listrez = ParseResponse(result);
            List<ProductsAvailableStruct> listStruct =
                GenerateStruct(listrez, (new ProductsAvailableStruct()).GetType()).Cast<ProductsAvailableStruct>().ToList();
            return listStruct.ToArray();
        }

        public int SetDoc(XmlRpcStruct docHeader, XmlRpcStruct[] docDetStruct)
        {
            try
            {
                object result = ExcecFunc(_droidPresManager, "SetDoc",
                                          new[]
                                              {
                                                  ConvertStructToValueList1C(docHeader),
                                                  ConvertStructArrayToValueList1C(docDetStruct)
                                              });
                return Convert.ToInt32(result);
            }
            catch (Exception e)
            {
                // Log.Error(e.InnerException.Message);
                throw new Exception(e.Message + " " + (e.InnerException != null ? " " + e.InnerException.Message : ""));
            }
        }

        public bool SetLocation(int agentId, LocationStruct[] location)
        {
            return true;
        }

        public string Registration()
        {
            return "1c8";
        }

        #endregion

        private IEnumerable<string> ParseResponse(object listFromOneS)
        {
            decimal countrez = decimal.Parse(ExcecFunc(listFromOneS, "Количество", new object[0]).ToString());

            var listrez = new List<string>();
            for (int i = 0; i < countrez; i++)
            {
                string currentValue =
                    ExcecFunc(_droidPresManager, "ПолучитьЗначениеЭлементаСпискаЗначений", new[] { listFromOneS, i }).
                        ToString();
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
                                clientGroupBoxed.GetType().GetField(currentProperty.Name).SetValue(clientGroupBoxed,string.IsNullOrEmpty(value)?0:
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
                                clientGroupBoxed.GetType().GetField(currentProperty.Name).SetValue(clientGroupBoxed, string.IsNullOrEmpty(value) ? 0 :
                                                                                                   Convert.ToDouble(value,
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
            try
            {
                return obj.GetType().InvokeMember(nameFunction,
                                         BindingFlags.InvokeMethod, null, obj, arguments);
            }
            catch (Exception exception)
            {
                throw new Exception(exception.Message + " " + (exception.InnerException != null ? " " + exception.InnerException.Message : ""));
            }
            
        }

        private object ConvertStructArrayToValueList1C(IEnumerable<XmlRpcStruct> obj)
        {
            object valueList1C = ExcecFunc(_droidPresManager, "СоздатьСписокЗначений", new object[] { });
            foreach (XmlRpcStruct currentobj in obj)
            {
                ExcecFunc(valueList1C, "Add",
                          new[] { ConvertStructToValueList1C(currentobj) });
            }
            return valueList1C;
        }

        private object ConvertStructToValueList1C(XmlRpcStruct obj)
        {
            object valueList1C = ExcecFunc(_droidPresManager, "СоздатьСписокЗначений", new object[] { });

            foreach (DictionaryEntry currentobj in obj)
            {
                ExcecFunc(valueList1C, "Add",
                          new object[] { currentobj.Value.ToString(), currentobj.Key.ToString() });
            }
            return valueList1C;
        }
    }
}