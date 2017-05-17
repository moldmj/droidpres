using System;
using System.Collections.Generic;
using System.Reflection;
using System.Windows.Forms;
using DroidPresModB.Server.Core;
using DroidPresModB.Server.XmlRpcServer.Config;


namespace DroidPresModB.Server
{
    public partial class MainForm : Form
    {

        private readonly ServerDroidPres _serverDroidPres;
        
        public MainForm()
        {
            InitializeComponent();
            LoadConfig();
            //DroidPresLogManager.InitLogger();
            //DroidPresLogManager.Log.Info("App started");
            _serverDroidPres = new ServerDroidPres();
        }

        private void LoadConfig()
        {
            try
            {
                ConfigManager.Load();
            }
            catch (Exception)
            {
                ConfigManager.Config = new Config();
            }
        }

        private void StartServer()
        {
            string strtresult = _serverDroidPres.StartWeb();
            if (!string.IsNullOrEmpty(strtresult))
            {
                MessageBox.Show(strtresult, "Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private void ShowFormConfigErpSystemConnectorForm()
        {
            var configForm = new ConfigErpSystemConnectorForm();
            configForm.Show();
        }

        private void eRPToolStripMenuItem_Click(object sender, EventArgs e)
        {
            ShowFormConfigErpSystemConnectorForm();
        }

        private void btnStart_Click(object sender, EventArgs e)
        {
            StartServer();
        }

        private void serverToolStripMenuItem_Click(object sender, EventArgs e)
        {

        }

        private void BtnStop_Click(object sender, EventArgs e)
        {
            _serverDroidPres.Stop();
        }

        private void button1_Click(object sender, EventArgs e)
        {
            ApkManager.GetNewApk(1);
        }

    }
}