using System;
using System.IO;


namespace DroidPresModB.Server.Core
{
    public static class ApkManager
    {
        public static byte[] GetNewApk(int currentVersion)
        {
            try
            {
                DirectoryInfo directorySelected = new DirectoryInfo(AppDomain.CurrentDomain.BaseDirectory + "apk\\");

                foreach (FileInfo fileInFolder in directorySelected.GetFiles())
                    if (!fileInFolder.Name.Contains(currentVersion.ToString()))
                        return File.ReadAllBytes(fileInFolder.FullName);             
            }
            catch
            {
            }
            return new byte[] { 0 };
        }
    }
}
