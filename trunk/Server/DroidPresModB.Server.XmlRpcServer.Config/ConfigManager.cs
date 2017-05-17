using System;
using System.IO;
using System.Xml.Serialization;

namespace DroidPresModB.Server.XmlRpcServer.Config
{
    public static class ConfigManager
    {
        public static Config Config;

        private static readonly string configPatch =
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData) + "\\DroidPresModB\\Config.xml";


        public static void Save()
        {
            Directory.CreateDirectory(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData) +
                                      "\\DroidPresModB\\");

            var serializer = new XmlSerializer(typeof (Config));
            TextWriter textWriter = new StreamWriter(configPatch);
            serializer.Serialize(textWriter, Config);
            textWriter.Close();
        }


        public static void Load()
        {
            var deserializer = new XmlSerializer(typeof (Config));
            TextReader textReader = new StreamReader(configPatch);
            Object obj = deserializer.Deserialize(textReader);
            Config = (Config) obj;
            textReader.Dispose();
        }
    }
}