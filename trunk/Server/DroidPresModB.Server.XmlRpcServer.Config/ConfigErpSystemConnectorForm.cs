using System;
using System.Windows.Forms;

namespace DroidPresModB.Server.XmlRpcServer.Config
{
    public partial class ConfigErpSystemConnectorForm : Form
    {
        public ConfigErpSystemConnectorForm()
        {
            InitializeComponent();
            erpSystemComboBox.Items.Add(ErpSystemType.OneS77);
            erpSystemComboBox.Items.Add(ErpSystemType.OneS82);
            erpSystemComboBox.Items.Add(ErpSystemType.OneS83);
            connectionTypeComboBox.Items.Add(ErpSystemConnectionType.File);
            connectionTypeComboBox.Items.Add(ErpSystemConnectionType.Server);
            erpSystemComboBox.SelectedItem = ConfigManager.Config.SystemType;
            connectionTypeComboBox.SelectedItem = ConfigManager.Config.ConnectionType;
            CBDebugMod.Checked = ConfigManager.Config.DebugMod;
            dirDBTextBox.Text = ConfigManager.Config.DirDb;
            userTextBox.Text = ConfigManager.Config.User;
            passwordTextBox.Text = ConfigManager.Config.Password;
        }

        private void cancelBtn_Click(object sender, EventArgs e)
        {
            Close();
        }

        private void okBtn_Click(object sender, EventArgs e)
        {
            ConfigManager.Config.SystemType = (ErpSystemType) erpSystemComboBox.SelectedItem;
            ConfigManager.Config.DirDb = dirDBTextBox.Text;
            ConfigManager.Config.User = userTextBox.Text;
            ConfigManager.Config.Password = passwordTextBox.Text;
            ConfigManager.Config.ConnectionType = (ErpSystemConnectionType) connectionTypeComboBox.SelectedItem;
            ConfigManager.Config.DebugMod = CBDebugMod.Checked;
            ConfigManager.Save();
            Close();
        }
    }
}