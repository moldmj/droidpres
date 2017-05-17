using System;
using System.IO;
using System.Net;
using CookComputing.XmlRpc;
using DroidPresModB.Server.Core.Interface;

namespace DroidPresModB.Server.Core.XMLRPCListener
{
    public abstract class ListenerService : XmlRpcHttpServerProtocol
    {
        public virtual void ProcessRequest(HttpListenerContext requestContext)
        {
            try
            {
                IHttpRequest req = new ListenerRequest(requestContext.Request);
                IHttpResponse resp = new ListenerResponse(requestContext.Response);
                HandleHttpRequest(req, resp);
                requestContext.Response.AddHeader("Content-Encoding", "gzip");
                requestContext.Response.OutputStream.Close();
            }
            catch (Exception ex)
            {
                // "Internal server error"
                requestContext.Response.StatusCode = 500;
                requestContext.Response.StatusDescription = ex.Message;
            }
        }
    }


    public class DroidPres2Service : ListenerService
    {
        private readonly IErpSystemConnector _erpSystemConnector;
        public DroidPres2Service(IErpSystemConnector connector)
        {
            _erpSystemConnector = connector;
        }
        [XmlRpcMethod("GetRefClientGroup")]
        public ClientGroupStruct[] GetRefClientGroup(int agentId)
        {
            return _erpSystemConnector.GetRefClientGroup(agentId);
        }

        [XmlRpcMethod("GetUpdateApp")]
        public Byte[] GetUpdateApp(int currentVersion)
        {
            return _erpSystemConnector.GetUpdateApp(currentVersion);
        }

        [XmlRpcMethod("GetRefTypeDoc")]
        public TypeDocStruct[] GetRefTypeDoc(int agentId)
        {
            return _erpSystemConnector.GetRefTypeDoc(agentId);
        }

        [XmlRpcMethod("GetRefClient")]
        public ClientStruct[] GetRefClient(int agentId)
        {
            return _erpSystemConnector.GetRefClient(agentId);
        }

        [XmlRpcMethod("GetRefProductGroup")]
        public ProductGroupStruct[] GetRefProductGroup(int agentId)
        {
            return _erpSystemConnector.GetRefProductGroup(agentId);
        }

        [XmlRpcMethod("GetRefProduct")]
        public ProductStruct[] GetRefProduct(int agentId)
        {
            return _erpSystemConnector.GetRefProduct(agentId);
        }

        [XmlRpcMethod("GetRefPrices")]
        public PriceStruct[] GetRefPrices(int agentId)
        {
            return _erpSystemConnector.GetRefPrices(agentId);
        }

        [XmlRpcMethod("GetRefProductPrices")]
        public ProductsPricesStruct[] GetRefProductPrices(int agentId)
        {
            return _erpSystemConnector.GetRefProductPrices(agentId);
        }


        [XmlRpcMethod("GetRefWarehouse")]
        public WarehouseStruct[] GetRefWarehouse(int agentId)
        {
            return _erpSystemConnector.GetRefWarehouse(agentId);
        }

        [XmlRpcMethod("GetRefProductsAvailable")]
        public ProductsAvailableStruct[] GetRefProductsAvailable(int agentId)
        {
            return _erpSystemConnector.GetRefProductsAvailable(agentId);
        }

        [XmlRpcMethod("SetDoc")]
        public int SetDoc(XmlRpcStruct docStruct, XmlRpcStruct[] docDetStruct)
        {
            return _erpSystemConnector.SetDoc(docStruct, docDetStruct);
        }

        [XmlRpcMethod("SetLocation")]
        public bool SetLocation(int agentId, LocationStruct[] location)
        {
            return _erpSystemConnector.SetLocation(agentId, location);
        }
    }

    public class ListenerRequest :IHttpRequest
    {
        public ListenerRequest(HttpListenerRequest request)
        {
            this.request = request;
        }

        public Stream InputStream
        {
            get { return request.InputStream; }
        }

        public string HttpMethod
        {
            get { return request.HttpMethod; }
        }

        private HttpListenerRequest request;
    }

    public class ListenerResponse : IHttpResponse
    {
        public ListenerResponse(HttpListenerResponse response)
        {
            this._response = response;
        }

        public long ContentLength { set; private get; }

        string IHttpResponse.ContentType
        {
            get { return _response.ContentType; }
            set { _response.ContentType = value; }
        }

        TextWriter IHttpResponse.Output
        {
            get { return new StreamWriter(_response.OutputStream); }
        }

        Stream IHttpResponse.OutputStream
        {
            get { return _response.OutputStream; }
        }

        public bool SendChunked { get; set; }

        int IHttpResponse.StatusCode
        {
            get { return _response.StatusCode; }
            set { _response.StatusCode = value; }
        }

        string IHttpResponse.StatusDescription
        {
            get { return _response.StatusDescription; }
            set { _response.StatusDescription = value; }
        }

        private readonly HttpListenerResponse _response;
    }
}
