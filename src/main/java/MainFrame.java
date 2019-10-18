import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import md.CmdPool;
import md.SerialPortManager;
import utils.DataUtils;
import utils.LastError;
import utils.ShowUtils;
import utils.UartManager;
import ymode.PrintInfo;
import ymode.YModem;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static md.CmdPool.*;

/**
 * 主界面
 *
 * @author yangle
 */
@SuppressWarnings("all")
public class MainFrame extends JFrame implements ItemListener {

    // 程序界面宽度
    public final int WIDTH = 550;
    // 程序界面高度
    public final int HEIGHT = 640;

    // 数据显示区
    private JTextArea mDataView = new JTextArea();
    private JScrollPane mScrollDataView = new JScrollPane(mDataView);

    // 串口设置面板
    private JPanel mSerialPortPanel = new JPanel();
    private JLabel mSerialPortLabel = new JLabel("串口");
    private JLabel mBaudrateLabel = new JLabel("波特率");
    private JComboBox mCommChoice = new JComboBox();
    private JComboBox mBaudrateChoice = new JComboBox();
    private ButtonGroup mDataChoice = new ButtonGroup();
    private JRadioButton mDataASCIIChoice = new JRadioButton("ASCII", true);
    private JRadioButton mDataHexChoice = new JRadioButton("Hex");

    // 操作面板
    private JPanel mOperatePanel = new JPanel();
    private JTextArea mDataInput = new JTextArea();
    private JButton mSerialPortOperate = new JButton("打开串口");
    private JButton mSendData = new JButton("发送数据");
    private JButton mBurn = new JButton("Burn");

    // 串口列表
    private List<String> mCommList = null;
    // 串口对象
    private SerialPort mSerialport;
    private boolean isBurning;
    private boolean netOk;
    private int lastStep;

    private UartManager uartManager;

    public MainFrame() {
        initView();
        initComponents();
        actionListener();
        initData();
    }

    /**
     * 初始化窗口
     */
    private void initView() {
        // 关闭程序
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        // 禁止窗口最大化
        setResizable(false);

        // 设置程序窗口居中显示
        Point p = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        setBounds(p.x - WIDTH / 2, p.y - HEIGHT / 2, WIDTH, HEIGHT);
        this.setLayout(null);

        setTitle("串口通信");
    }

    /**
     * 初始化控件
     */
    private void initComponents() {
        // 数据显示
        mDataView.setFocusable(true);
        mScrollDataView.setBounds(10, 10, 355, 580);
        add(mScrollDataView);

        // 串口设置
        mSerialPortPanel.setBorder(BorderFactory.createTitledBorder("串口设置"));
//        mSerialPortPanel.setForeground(Color.WHITE);
        mSerialPortPanel.setBounds(370, 10, 170, 130);
        mSerialPortPanel.setLayout(null);
//        mSerialPortPanel.setBackground(Color.DARK_GRAY);
        add(mSerialPortPanel);

//        mSerialPortLabel.setForeground(Color.gray);
        mSerialPortLabel.setBounds(10, 25, 40, 20);
        mSerialPortPanel.add(mSerialPortLabel);

        mCommChoice.setFocusable(false);
        mCommChoice.setBounds(60, 25, 100, 20);
        mSerialPortPanel.add(mCommChoice);

//        mBaudrateLabel.setForeground(Color.gray);
        mBaudrateLabel.setBounds(10, 60, 40, 20);
        mSerialPortPanel.add(mBaudrateLabel);

        mBaudrateChoice.setFocusable(false);
        mBaudrateChoice.setBounds(60, 60, 100, 20);
        mSerialPortPanel.add(mBaudrateChoice);

        mDataASCIIChoice.setBounds(20, 95, 55, 20);
        mDataHexChoice.setBounds(95, 95, 55, 20);
        mDataChoice.add(mDataASCIIChoice);
        mDataChoice.add(mDataHexChoice);
        mSerialPortPanel.add(mDataASCIIChoice);
        mSerialPortPanel.add(mDataHexChoice);

        // 操作
        mOperatePanel.setBorder(BorderFactory.createTitledBorder("操作"));
        mOperatePanel.setBounds(370, 200, 170, 390);
        mOperatePanel.setLayout(null);
//        mOperatePanel.setForeground(Color.WHITE);
//        mOperatePanel.setBackground(Color.DARK_GRAY);
        add(mOperatePanel);

        mDataInput.setBounds(10, 25, 150, 180);
        mDataInput.setLineWrap(true);
        mDataInput.setWrapStyleWord(true);
        mOperatePanel.add(mDataInput);

        mSerialPortOperate.setFocusable(false);
        mSerialPortOperate.setBounds(45, 230, 90, 30);
        mOperatePanel.add(mSerialPortOperate);

        mSendData.setFocusable(false);
        mSendData.setBounds(45, 270, 90, 30);
        mOperatePanel.add(mSendData);

        mBurn.setFocusable(false);
        mBurn.setBounds(45, 310, 90, 30);
//        mBurn.setForeground(Color.WHITE);
//        mBurn.setBackground(Color.CYAN);
        mOperatePanel.add(mBurn);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mCommList = SerialPortManager.findPorts();
        // 检查是否有可用串口，有则加入选项中
        if (mCommList == null || mCommList.size() < 1) {
            ShowUtils.warningMessage("没有搜索到有效串口！");
        } else {
            for (String s : mCommList) {
                mCommChoice.addItem(s);
            }
        }

//        mBaudrateChoice.addItem("9600");
//        mBaudrateChoice.addItem("19200");
//        mBaudrateChoice.addItem("38400");
//        mBaudrateChoice.addItem("57600");
        mBaudrateChoice.addItem("115200");
    }

    /**
     * 按钮监听事件
     */
    private void actionListener() {
        // 串口
        mCommChoice.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                mCommList = SerialPortManager.findPorts();
                // 检查是否有可用串口，有则加入选项中
                if (mCommList == null || mCommList.size() < 1) {
                    ShowUtils.warningMessage("没有搜索到有效串口！");
                } else {
                    int index = mCommChoice.getSelectedIndex();
                    mCommChoice.removeAllItems();
                    for (String s : mCommList) {
                        mCommChoice.addItem(s);
                    }
                    mCommChoice.setSelectedIndex(index);
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                // NO OP
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                // NO OP
            }
        });

        // 打开|关闭串口
        mSerialPortOperate.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if ("打开串口".equals(mSerialPortOperate.getText()) && mSerialport == null) {
                    openSerialPort(e);
                } else {
                    closeSerialPort(e);
                }
            }
        });

        // 发送数据
        mSendData.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                sendData(e);
            }
        });

        mBurn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendFile();
//                if (!isBurning) {
//                    isBurning = true;
//                    startBurn();
//                } else {
//                    isBurning = false;
//                }
            }
        });

        mDataASCIIChoice.addItemListener(this);
        mDataHexChoice.addItemListener(this);
    }

    private void sendFile() {

        try {
            uartManager = new UartManager();
            uartManager.open(mSerialport.getName(), UartManager.getBaudRate(mSerialport.getBaudRate()));

            YModem yModem = new YModem(uartManager);
            File file = new File("F:\\XsleepBurn\\out\\production\\resources\\WakeUp.bin");
            if (file != null && file.exists()) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        if (uartManager != null) {
                            try {
                                if (uartManager.isOpen()) {
                                    yModem.send(file);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }).start();
            }else {
                System.out.println("文件不存在");
            }
        } catch (LastError lastError) {
            lastError.printStackTrace();
        }
    }

    private static PrintInfo printInfo;

    private static void startPrintInfo(InputStream is) {
        if (printInfo == null) {
            printInfo = new PrintInfo(is);
            printInfo.setRunning(true);
            printInfo.start();
        }
    }

    private static void stopPrintInfo() {
        if (printInfo != null) {
            printInfo.setRunning(false);
            printInfo.interrupt();
            printInfo = null;
        }
    }

    private void startBurn() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (currentSetp < 100 && isBurning) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("----");
                    if (currentSetp == 5) {
                    }
                    if (currentSetp == lastStep && currentSetp > 1) return;
                    lastStep = currentSetp;
                    switch (currentSetp) {
                        case 0:
                            sendData(CmdPool.ENTER);
                            break;
                        case 1:
                            sendData(CmdPool.ENTER);
                            break;
                        case 2:
                            sendData(CmdPool.LOGIN_ROOT);
                            break;
                        case 3:
                            sendData(CmdPool.LOGIN_PW_WDL);
                            break;
                        case 4:
                            sendData(CmdPool.WLAN_SET);
                            break;
                        case 5:
                            System.out.println("276");
                            sendData(CmdPool.DL_OPENWRT);
                            break;
                        case 6:
                            sendData(CmdPool.DL_ENSURE);
                            break;
                        case 7:
                            sendData(CmdPool.DL_PW);
                            break;
                        case 8:
                            sendData(CmdPool.UPGRADE);
                            break;
                        case 9:
                            sendData(CmdPool.ENTER);
                            break;
                        case 10:
                            sendData(CmdPool.LOGIN_ROOT);
                            break;
                        case 11:
                            sendData(CmdPool.LOGIN_PW_WDL);
                            break;
                        case 12:
                            sendData(CmdPool.MOUNT);
                            break;
                        case 13:
                            sendData(CmdPool.BURN_FILE);
                            break;
                        case 14:
                            sendData(CmdPool.BURN);
                            break;
                        case 15:
                            sendData(CmdPool.REBOOT);
                            break;
                        case 16:
                            sendData(CmdPool.ENTER);
                            break;
                        case 17:
                            sendData(CmdPool.LOGIN_ROOT);
                            break;
                        case 18:
                            sendData(CmdPool.LOGIN_PW_XSLEEP);
                            break;
                        case 19:
                            sendData(CmdPool.REBOOT);
                            break;
                        case 20:
                            break;
                    }
                }
                System.out.println("---" + currentSetp + isBurning);
            }
        }).start();
    }

    /**
     * 打开串口
     *
     * @param evt 点击事件
     */
    private void openSerialPort(java.awt.event.ActionEvent evt) {
        // 获取串口名称
        String commName = (String) mCommChoice.getSelectedItem();
        // 获取波特率，默认为115200
        int baudrate = 115200;
        String bps = (String) mBaudrateChoice.getSelectedItem();
        baudrate = Integer.parseInt(bps);

        // 检查串口名称是否获取正确
        if (commName == null || commName.equals("")) {
            ShowUtils.warningMessage("没有搜索到有效串口！");
        } else {
            try {
                mSerialport = SerialPortManager.openPort(commName, baudrate);
                if (mSerialport != null) {
                    mDataView.setText("串口已打开" + "\r\n");
                    mSerialPortOperate.setText("关闭串口");
                }
            } catch (PortInUseException e) {
                ShowUtils.warningMessage("串口已被占用！");
            }
        }

        // 添加串口监听
        SerialPortManager.addListener(mSerialport, new SerialPortManager.DataAvailableListener() {

            @Override
            public void dataAvailable() {
                byte[] data = null;
                try {
                    if (mSerialport == null) {
                        ShowUtils.errorMessage("串口对象为空，监听失败！");
                    } else {
                        // 读取串口数据
                        data = SerialPortManager.readFromPort(mSerialport);

                        String dataStr = "";

                        // 以字符串的形式接收数据
                        if (mDataASCIIChoice.isSelected()) {
                            dataStr = new String(data) + "\r\n";
                        }

                        // 以十六进制的形式接收数据
                        if (mDataHexChoice.isSelected()) {
                            dataStr = DataUtils.bytes2hex(data) + "\r\n";
                        }
                        mDataView.append(dataStr);


                        switch (CmdPool.currentSetp) {
                            case STEP_INIT:
                                if (dataStr.contains("nonbloking pool is initialized")) {
                                    CmdPool.currentSetp = STEP_INIT + 1;
                                } else if (dataStr.contains("Widora login:")) {
                                    CmdPool.currentSetp = STEP_INIT_ENTER + 1;
                                    System.out.println("MainFrame.dataAvailable " + currentSetp);
                                } else if (dataStr.contains("Password:")) {
                                    CmdPool.currentSetp = STEP_LOGIN_ROOT + 1;
                                    System.out.println("MainFrame.dataAvailable " + currentSetp);
                                } else if (dataStr.contains("root@Widora:~#")) {
                                    CmdPool.currentSetp = STEP_LOGIN_PW + 1;
                                } else if (dataStr.contains("APCLI LINK UP")) {
                                    CmdPool.currentSetp = STEP_WLANSET + 1;
                                    System.out.println("417 " + currentSetp);
                                    netOk = true;
                                } else if (dataStr.contains("APCLI LINK DOWN") ||
                                        dataStr.contains("by peer")) {
                                    CmdPool.currentSetp = STEP_LOGIN_PW + 1;
                                    netOk = false;
                                } else if (dataStr.contains("y/n")) {
                                    CmdPool.currentSetp = STEP_DL_OPENWRT + 1;
                                } else if (dataStr.contains("100%")) {
                                    CmdPool.currentSetp = STEP_DL_PW + 1;
                                }
                                break;
                            case STEP_INIT_ENTER:
                                if (dataStr.contains("Widora login:")) {
                                    CmdPool.currentSetp = STEP_INIT_ENTER + 1;
                                    System.out.println("MainFrame.dataAvailable " + currentSetp);
                                } else {

                                }
                                break;
                            case STEP_LOGIN_ROOT:
                                if (dataStr.contains("Password:")) {
                                    CmdPool.currentSetp = STEP_LOGIN_ROOT + 1;
                                    System.out.println("MainFrame.dataAvailable " + currentSetp);
                                } else {

                                }
                                break;
                            case STEP_LOGIN_PW:
                                if (dataStr.contains("root@Widora:~#")) {
                                    CmdPool.currentSetp = STEP_LOGIN_PW + 1;
                                    System.out.println("428 " + currentSetp);
                                } else {

                                }
                                break;
                            case STEP_WLANSET:

                                if (dataStr.contains("APCLI LINK UP")) {
                                    CmdPool.currentSetp = STEP_WLANSET + 1;
                                    System.out.println("438 " + currentSetp);
                                    netOk = true;
                                } else {

                                }
                                if (dataStr.contains("APCLI LINK DOWN") ||
                                        dataStr.contains("by peer")) {
                                    CmdPool.currentSetp = STEP_LOGIN_PW + 1;
                                    System.out.println("446 " + currentSetp);
                                    netOk = false;
                                }
                                break;
                            case STEP_DL_OPENWRT:
                                if (dataStr.contains("y/n")) {
                                    CmdPool.currentSetp = STEP_DL_OPENWRT + 1;
                                    System.out.println("452 " + currentSetp);
                                } else {

                                }
                                break;
                            case STEP_DL_ENSURE:
                                if (dataStr.contains("password:")) {
                                    CmdPool.currentSetp = STEP_DL_ENSURE + 1;
                                    System.out.println("461 " + currentSetp);
                                } else {

                                }
                                break;
                            case STEP_DL_PW:
                                if (dataStr.contains("100%")) {
                                    CmdPool.currentSetp = STEP_DL_PW + 1;
                                } else {

                                }
                                break;
                            case STEP_UPGRADE:
                                if (dataStr.contains("nonbloking pool is initialized")) {
                                    CmdPool.currentSetp = STEP_UPGRADE + 1;
                                } else {

                                }
                                break;
                            case STEP_UPGRADE_SUCCESS:
                                if (dataStr.contains("login:")) {
                                    CmdPool.currentSetp = STEP_UPGRADE_SUCCESS + 1;
                                } else {

                                }
                                break;
                            case STEP_LOGIN_ROOT_AGAIN:
                                if (dataStr.contains("password:")) {
                                    CmdPool.currentSetp = STEP_LOGIN_ROOT_AGAIN + 1;
                                } else {

                                }
                                break;
                            case STEP_LOGIN_PW_AGAIN:
                                if (dataStr.contains(":~#")) {
                                    CmdPool.currentSetp = STEP_LOGIN_PW_AGAIN + 1;
                                } else {

                                }
                                break;
                            case STEP_MOUNT:
                                if (dataStr.contains("cdcdcdcdcd")) {
                                    CmdPool.currentSetp = STEP_MOUNT + 1;
                                } else {

                                }
                                break;
                            case STEP_BURN_FILE:
                                if (dataStr.contains("burnstart")) {
                                    CmdPool.currentSetp = STEP_BURN_FILE + 1;
                                } else {

                                }
                                break;
                            case STEP_BURN:
                                if (dataStr.contains("burnsuccess")) {
                                    CmdPool.currentSetp = STEP_BURN + 1;
                                } else {

                                }
                                break;
                            case STEP_REBOOT:
                                if (dataStr.contains("nonbloking pool is initialized")) {
                                    CmdPool.currentSetp = STEP_REBOOT + 1;
                                } else {

                                }
                                break;
                            case STEP_LAST_INIT:
                                if (dataStr.contains("login:")) {
                                    CmdPool.currentSetp = STEP_LAST_INIT + 1;
                                } else {

                                }
                                break;
                            case STEP_LAST_LOGIN:
                                if (dataStr.contains("password:")) {
                                    CmdPool.currentSetp = STEP_LAST_LOGIN + 1;
                                } else {

                                }
                                break;
                            case STEP_LAST_LOGIN_PW:
                                if (dataStr.contains("~#:")) {
                                    CmdPool.currentSetp = STEP_LAST_LOGIN_PW + 1;
                                } else {

                                }
                                break;
                            case STEP_LAST_REBOOT:
                                if (dataStr.contains("~#:")) {
                                    CmdPool.currentSetp = STEP_LAST_REBOOT + 1;
                                } else {

                                }
                                break;
                        }
                    }
                } catch (Exception e) {
                    ShowUtils.errorMessage(e.toString());
                    // 发生读取错误时显示错误信息后退出系统
                    System.exit(0);
                }
            }
        });
    }

    /**
     * 关闭串口
     *
     * @param evt 点击事件
     */
    private void closeSerialPort(java.awt.event.ActionEvent evt) {
        SerialPortManager.closePort(mSerialport);
        mDataView.setText("串口已关闭" + "\r\n");
        mSerialPortOperate.setText("打开串口");
        mSerialport = null;
    }

    /**
     * 发送数据
     *
     * @param evt 点击事件
     */
    private void sendData(java.awt.event.ActionEvent evt) {
        // 待发送数据
        String data = mDataInput.getText().toString();

        if (mSerialport == null) {
            ShowUtils.warningMessage("请先打开串口！");
            return;
        }

        if ("".equals(data) || data == null) {
            ShowUtils.warningMessage("请输入要发送的数据！");
            return;
        }

        // 以字符串的形式发送数据
        if (mDataASCIIChoice.isSelected()) {
            SerialPortManager.sendToPort(mSerialport, data.getBytes());
        }

        // 以十六进制的形式发送数据
        if (mDataHexChoice.isSelected()) {
            SerialPortManager.sendToPort(mSerialport, DataUtils.hexStrToByteArray(data));
        }
    }

    private void sendData(String data) {

        if (mSerialport == null) {
            ShowUtils.warningMessage("请先打开串口！");
            return;
        }

        mDataInput.setText(data);
        // 以字符串的形式发送数据
        if (mDataASCIIChoice.isSelected()) {
            SerialPortManager.sendToPort(mSerialport, data.getBytes());
        } else {// 以十六进制的形式发送数据
            SerialPortManager.sendToPort(mSerialport, DataUtils.hexStrToByteArray(data));
        }
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
//        if (e.getSource() == mDataASCIIChoice){
//            String data = mDataInput.getText().toString();
//            byte[] a = data.getBytes();
//            String dataHex = DataUtils.bytes2hex(a);
//            mDataInput.setText("");
//            mDataInput.setText(dataHex);
//        }else {
//            String data = mDataInput.getText().toString();
//            byte[] a = DataUtils.hexStrToByteArray(data);
//            String dataStr = new String(a);
//            mDataInput.setText("");
//            mDataInput.setText(dataStr);
//        }
    }
}

//版权声明：本文为CSDN博主「容华谢后」的原创文章，遵循CC 4.0 by-sa版权协议，转载请附上原文出处链接及本声明。
//原文链接：https://blog.csdn.net/kong_gu_you_lan/article/details/80589859