package md;

/**
 1. 登录串口
 root
 12345678

 2. 配置网络
 wifimode apsta MK_RD moonmark123

 3. 下载openwrt
 scp cdq@192.168.102.200:/media/nfsboot/openwrt-ramips-mt7688-WIDORA32128-squashfs-sysupgrade-20190301v3.bin .
 y
 123456

 4. 升级固件
 sysupgrade openwrt-ramips-mt7688-WIDORA32128-squashfs-sysupgrade-20190301v3.bin

 5. 登录串口
 root
 12345678

 6. 挂接网络文件系统
 mount -t nfs -o nolock 192.168.102.200:/media/nfsboot /mnt

 7. 烧写文件系统
 cd /mnt
 tar xvf pro20190710v2.tar.gz -C /

 8. 重启
 reboot

 9. 烧写校验
 听到以2秒为间隔的蜂鸣器响

 如果出错可以用如下命令查看网络状态
 ifconfig apcli0|grep "inet addr:"|awk '{print $2}'

 10. 登录
 root
 884BD91E

 10. 重启
 reboot
 听到1次蜂鸣器响
 即完成

 */
public class CmdPool {
    public static final int STEP_INIT = 0;
    public static final int STEP_INIT_ENTER = 1;
    public static final int STEP_LOGIN_ROOT = 2;
    public static final int STEP_LOGIN_PW = 3;
    public static final int STEP_WLANSET = 4;
    public static final int STEP_DL_OPENWRT = 5;
    public static final int STEP_DL_ENSURE = 6;
    public static final int STEP_DL_PW = 7;
    public static final int STEP_UPGRADE = 8;
    public static final int STEP_UPGRADE_SUCCESS = 9;
    public static final int STEP_LOGIN_ROOT_AGAIN = 10;
    public static final int STEP_LOGIN_PW_AGAIN = 11;
    public static final int STEP_MOUNT = 12;
    public static final int STEP_BURN_FILE = 13;
    public static final int STEP_BURN = 14;
    public static final int STEP_REBOOT = 15;
    public static final int STEP_LAST_INIT = 16;
    public static final int STEP_LAST_LOGIN = 17;
    public static final int STEP_LAST_LOGIN_PW = 18;
    public static final int STEP_LAST_REBOOT = 19;

    public static int currentSetp = 0;
    public final static String ENTER = "\n";
    public final static String LOGIN_ROOT = "root\n";
    public final static String LOGIN_PW_WDL = "12345678\n";
    public final static String WLAN_SET ="wifimode apsta MK_RD moonmark123\n";
    public final static String DL_OPENWRT="scp cdq@192.168.102.200:/media/nfsboot/openwrt-ramips-mt7688-WIDORA32128-squashfs-sysupgrade-20190301v3.bin .\n";
    public final static String DL_ENSURE = "y\n";
    public final static String DL_PW = "123456\n";
    public final static String UPGRADE = "sysupgrade openwrt-ramips-mt7688-WIDORA32128-squashfs-sysupgrade-20190301v3.bin\n";
    public final static String MOUNT = "mount -t nfs -o nolock 192.168.102.200:/media/nfsboot /mnt\n";
    public final static String BURN_FILE = "cd /mnt\n";
    public final static String BURN ="tar xvf pro20190710v2.tar.gz -C /\n";
    public final static String REBOOT = "reboot\n";
    public final static String LOGIN_PW_XSLEEP = "884BD91E\n";

}
