using System;
using System.Collections;
using System.Linq;
using System.Runtime.Remoting;
using System.Runtime.Remoting.Channels;
using System.Runtime.Remoting.Channels.Http;
using System.Text;
using CookComputing.XmlRpc;
using DroidPresModB.Server.Core.Interface;
using DroidPresModB.Server.XmlRpcServer.Config;

namespace DroidPresModB.Server.Core
{
    public class ServerDroidPres
    {
        private HttpChannel _channel;
        private bool _starting;

        //public string Start()
        //{
        //    if (!TestConnectiodErp())
        //    {
        //        return "Ошибка подключения к базе, проверьте Лог.";
        //    }
        //    if (_starting)
        //    {
        //        return "Сервер уже был запущен ранее.";
        //    }

        //    IDictionary props = new Hashtable();
        //    props["name"] = "droidpressrpc2";
        //    props["port"] = 8888;
        //    _channel = new HttpChannel(
        //        props,
        //        null,
        //        new XmlRpcServerFormatterSinkProvider()
        //        );

        //    ChannelServices.RegisterChannel(_channel, false);

        //    RemotingConfiguration.RegisterWellKnownServiceType(Type.GetType(ConfigManager.Config.ERPConnector),
        //                                                       "droidpressrpc2",
        //                                                       ConfigManager.Config.DebugMod ? WellKnownObjectMode.SingleCall : WellKnownObjectMode.Singleton);
        //    _starting = true;

        //    return string.Empty;
        //}

        public string StartWeb()
        {
            if (!TestConnectiodErp())
            {
                return "Ошибка подключения к базе, проверьте Лог.";
            }
            if (_starting)
            {
                return "Сервер уже был запущен ранее.";
            }

            IDictionary props = new Hashtable();
            props["name"] = "droidpressrpc2";
            props["port"] = 8888;
            _channel = new HttpChannel(
                props,
                null,
                new XmlRpcServerFormatterSinkProvider()
                );

            ChannelServices.RegisterChannel(_channel, false);

            RemotingConfiguration.RegisterWellKnownServiceType(Type.GetType(ConfigManager.Config.ERPConnector),
                "droidpressrpc2", ConfigManager.Config.DebugMod?WellKnownObjectMode.SingleCall:
                                                               WellKnownObjectMode.Singleton);
            _starting = true;

            return string.Empty;
        }


        public bool TestConnectiodErp()
        {
            //try
            //{
            if (ConfigManager.Config.ERPConnector != null)
            {
                var server = (IErpSystemConnector)Activator.CreateInstance(Type.GetType(ConfigManager.Config.ERPConnector));
                server.Connect();
                server.Disconnect();
            }
            return true;
            //}
            //catch (Exception e)
            //{
            //    return false;
            //}
        }

        public void Stop()
        {
            throw new NotImplementedException();
        }
    }
}


//using System;
//using System.Collections;
//using System.IO;
//using System.Linq;
//using System.Net;
//using System.Runtime.Remoting;
//using System.Runtime.Remoting.Channels;
//using System.Runtime.Remoting.Channels.Http;
//using System.Text;
//using System.Threading.Tasks;
//using CookComputing.XmlRpc;
//using DroidPresModB.Server.Core.Interface;
//using DroidPresModB.Server.Core.ListenerServices;
//using DroidPresModB.Server.XmlRpcServer.Config;

//namespace DroidPresModB.Server.Core
//{
//    public class ServerDroidPres
//    {
//        private HttpChannel _channel;
//        private bool _starting;
//        private IErpSystemConnector _erpSystemConnector;
//        HttpListener Listener = new HttpListener();
//        private Boolean StopServer = false;

//        public ServerDroidPres()
//        {
//            _erpSystemConnector = (IErpSystemConnector)Activator.CreateInstance(Type.GetType(ConfigManager.Config.ERPConnector));
//        }
//        //public string Start()
//        //{
//        //    if (!TestConnectiodErp())
//        //    {
//        //        return "Ошибка подключения к базе, проверьте Лог.";
//        //    }
//        //    if (_starting)
//        //    {
//        //        return "Сервер уже был запущен ранее.";
//        //    }

//        //    IDictionary props = new Hashtable();
//        //    props["name"] = "droidpressrpc2";
//        //    props["port"] = 8888;
//        //    _channel = new HttpChannel(
//        //        props,
//        //        null,
//        //        new XmlRpcServerFormatterSinkProvider()
//        //        );

//        //    ChannelServices.RegisterChannel(_channel, false);

//        //    RemotingConfiguration.RegisterWellKnownServiceType(Type.GetType(ConfigManager.Config.ERPConnector),
//        //                                                       "droidpressrpc2",
//        //                                                       ConfigManager.Config.DebugMod ? WellKnownObjectMode.SingleCall : WellKnownObjectMode.Singleton);
//        //    _starting = true;

//        //    return string.Empty;
//        //}

//        public string StartWeb()
//        {
//            try
//            {
//                if (_starting)
//                    return "Сервер уже был запущен ранее.";

//                StopServer = false;
//                if (!TestConnectiodErp())
//                    return "Ошибка подключения к базе, проверьте Лог.";
                
//                Listener.Prefixes.Add("http://*:8887/droidpressrpc2/");
//                Listener.Start();
//                Listen();
//                _starting = true;
//            }
//            catch (Exception exception)
//            {

//                return exception.Message+(exception.InnerException==null?string.Empty:"\n"+exception.InnerException.Message);
//            }

//            return string.Empty;
//        }

//        private async void Listen()
//        {

//            while (!StopServer)
//            {
//                var context = await Listener.GetContextAsync();
//                await Task.Factory.StartNew(() => ProcessRequest(context));
//            }

//            Listener.Close();
//        }

//        private void ProcessRequest(HttpListenerContext context)
//        {
//            ListenerService svc = new DroidPres2Service(_erpSystemConnector);
//            svc.ProcessRequest(context);
//        }

//        private bool TestConnectiodErp()
//        {
//            //try
//            //{
//            if (ConfigManager.Config.ERPConnector != null)
//            {
//                var server = (IErpSystemConnector)Activator.CreateInstance(Type.GetType(ConfigManager.Config.ERPConnector));
//                server.Connect();
//                server.Disconnect();
//            }
//            return true;
//            //}
//            //catch (Exception e)
//            //{
//            //    return false;
//            //}
//        }

//        public void Stop()
//        {
//            Listener.Stop();
//            StopServer = true;
//        }
//    }
//}