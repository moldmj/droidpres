using System;
using System.Xml.Serialization;

namespace DroidPresModB.Server.XmlRpcServer.Config
{
    [Serializable]
    [XmlRoot("ConfigRoot")]
    public class Config
    {
        public ErpSystemType SystemType { get; set; }
        public ErpSystemConnectionType ConnectionType { get; set; }
        public string DirDb { get; set; }
        public string User { get; set; }
        public string Password { get; set; }
        public bool DebugMod { get; set; }

        public string ConnectionString1C
        {
            get
            {
                if (SystemType == ErpSystemType.OneS77)
                {
                    return string.Format("/d {0} /N {1} /P {2}", DirDb, User, Password);
                }

                if (SystemType == ErpSystemType.OneS82 || SystemType == ErpSystemType.OneS83)
                {
                    if (ConnectionType == ErpSystemConnectionType.File)
                        return string.Format("file='{0}'; usr='{1}'; pwd='{2}';", DirDb, User, Password);
                    else
                        return string.Format("{0}; usr='{1}'; pwd='{2}'", DirDb, User, Password);
                    
                    
                }
                return null;
            }
        }

        public string OleObjectName
        {
            get
            {
                if (SystemType == ErpSystemType.OneS77)
                    return "V77.Application";
                if (SystemType == ErpSystemType.OneS82)
                    return "V82.ComConnector";
                if (SystemType == ErpSystemType.OneS83)
                    return "V83.ComConnector";

                return null;
            }
        }

        public string ERPConnector
        {
            get
            {
                if (SystemType == ErpSystemType.OneS77)
                    return "DroidPresModB.Server.Core.ERPSystemConnectors.ErpOneS77Connector";
                if (SystemType == ErpSystemType.OneS82)
                    return "DroidPresModB.Server.Core.ERPSystemConnectors.ErpOneS8XConnector";
                if (SystemType == ErpSystemType.OneS83)
                    return "DroidPresModB.Server.Core.ERPSystemConnectors.ErpOneS8XConnector";
                return null;
            }
        }
    }

    public enum ErpSystemType
    {
        OneS77,
        OneS82,
        OneS83
    }

    public enum ErpSystemConnectionType
    {
        File,
        Server
    }
}