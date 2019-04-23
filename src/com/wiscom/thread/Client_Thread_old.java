package com.wiscom.thread;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.wiscom.bean.AlertDetail;
import com.wiscom.main.VariablesMain;
import com.wiscom.util.Connections;
import com.wiscom.util.JdbcUtil;

public class Client_Thread_old extends Thread {
	private static Logger logger = Logger.getLogger(Client_Thread_old.class);

	public static int interval = 30;
	public static boolean isInit = false;
	public static String ip = "172.16.37.64";
	public static int port = 33000;
	// public String alertFormat =
	// "摄像机编号：%s，事件开始时间：%s，车道类型：%s，事件类型：%s，路径名：%s，文件名：%s，事件在监控画面的横，纵坐标：%s，%s";
	public String alertFormat = "摄像机编号：%s，事件开始时间：%s，车道类型：%s，事件类型：%s%n";
	public String RemoveFormat = "摄像机编号：%s，事件开始时间：%s，事件解除时间：%s，车道类型：%s，事件类型：%s%n";
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Override
	public void run() {
		initConfig();
		if (isInit) {
			getIpConf();
			RecvTask();
			handleTask();
		}
	}

	private void getIpConf() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					String sql = "SELECT b.ID,b.CAM_ID,a.RTSPURL from camera_asset_info a,ctlog_ip_id b  where a.ID = b.ID";
					try {
						Map<String, Object> selectMap = JdbcUtil.selectMysql(sql);
						ResultSet res = (ResultSet) selectMap.get("ResultSet");
						PreparedStatement pstmt = (PreparedStatement) selectMap.get("PreparedStatement");
						while (res.next()) {
							String id = res.getString("ID");
							int camera_id = res.getInt("CAM_ID");
							String url = res.getString("RTSPURL");
							VariablesMain.confMap.put(String.valueOf(camera_id), id + "-" + url);
						}
						res.close();
						pstmt.close();
					} catch (SQLException e) {
						logger.error("get data from from table : camera_asset_info andctlog_ip_id time failed: "
								+ e.getMessage());
						e.printStackTrace();
					}

					try {
						Thread.sleep(60 * 60 * 1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		}).start();

	}

	private void RecvTask() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					Socket socket = null;
					InputStream in = null;
					OutputStream os = null;
					try {
						socket = new Socket(ip, port);
						os = socket.getOutputStream();
						os.write(hexStringToBytes("160A"));
						// os.write("SYNLF".getBytes());
						os.flush();
						in = socket.getInputStream();
						byte[] receive = new byte[65530];
						int len = 0;
						while (len == 0) {
							len = in.available();
						}
						in.read(receive);
						byte[] result = new byte[len];
						for (int i = 0; i < len; i++) {
							result[i] = receive[i];
						}
						String event = new String(result, "UTF-8");
						// System.out.println(event);
						if (event.startsWith("A 2 ") || event.startsWith("A 3")) {
							handleTask(event);
							// logger.info(event);
						}
						os.close();
						in.close();
						socket.close();
					} catch (IOException e) {
						logger.error(e.getMessage());
						if (socket != null) {
							try {
								socket.close();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
						e.printStackTrace();
					}

					try {
						Thread.sleep(interval * 1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}

				}

			}
		}).start();

	}

	protected void handleTask(String event) {
		String[] split = event.split(" ");
		String type = split[1];
		String date;
		String time;
		String startTime;
		String endTime;
		date = split[5];
		time = split[6];
		String key = split[2] + split[4] + split[3];
		startTime = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6) + " "
				+ time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4);
		// Date dateFor;
		while (VariablesMain.confMap.isEmpty()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		switch (type) {
		case "2":
			if (VariablesMain.confMap.containsKey(split[2])) {
				VariablesMain.alertMap.put(key,
						new AlertDetail(VariablesMain.confMap.get(split[2]).split("-")[0], convertType(split[4]),
								Integer.parseInt(split[3]), 0, "N", startTime, null, dateFormat.format(new Date()),
								VariablesMain.confMap.get(split[2]).split("-")[1]));
			}
			break;
		case "3":
			date = split[7];
			time = split[8];
			endTime = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6) + " "
					+ time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4, 6);

			if (VariablesMain.confMap.containsKey(split[2])) {
				VariablesMain.alertMap.put(key,
						new AlertDetail(VariablesMain.confMap.get(split[2]).split("-")[0], convertType(split[4]),
								Integer.parseInt(split[3]), 1, "N", startTime, endTime, dateFormat.format(new Date()),
								VariablesMain.confMap.get(split[2]).split("-")[1]));
			}
			break;
		default:
			break;
		}
	}

	// alert--1 remove--0
	public void handleTask() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				if (Connections.conMysql == null) {
					Connections.conMysql = Connections.getMysqlConn();
				}
				while (VariablesMain.alertMap.isEmpty()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				// AlertDetail detail = VariablesMain.alertMap.get(0);
				String insert = "replace into CTLOG_ALERT_INFO values(?,?,?,?,?,?,?,?)";
				try {
					PreparedStatement ps = Connections.conMysql.prepareStatement(insert);
					Connections.conMysql.setAutoCommit(false);
					for (Map.Entry<String, AlertDetail> entry : VariablesMain.alertMap.entrySet()) {
						ps.setString(1, entry.getValue().getCamId());
						ps.setString(2, entry.getValue().getTypeId());
						ps.setInt(3, entry.getValue().getLanNo());
						ps.setString(4, entry.getValue().getUrl());
						ps.setInt(5, entry.getValue().getAlertSta());
						ps.setString(6, entry.getValue().getIsConfirm());
						ps.setString(7, entry.getValue().getStartTime());
						ps.setString(8, entry.getValue().getEndTime());
						ps.setString(9, entry.getValue().getCollectedTime());
						ps.addBatch();
						if (entry.getValue().getAlertSta() == 0) {
							logger.info(String.format(alertFormat, entry.getValue().getCamId(),
									entry.getValue().getStartTime(), entry.getValue().getLanNo(),
									entry.getValue().getTypeId()));
							logger.info("insert alert event successfully");
						}
						if (entry.getValue().getAlertSta() == 1) {
							logger.info(String.format(RemoveFormat, entry.getValue().getCamId(),
									entry.getValue().getStartTime(), entry.getValue().getEndTime(),
									entry.getValue().getLanNo(), entry.getValue().getTypeId()));
							logger.info("insert remove alert event successfully");
						}
						VariablesMain.alertMap.remove(entry.getKey());
					}
					ps.executeBatch();
					Connections.conMysql.commit();
					ps.close();
				} catch (SQLException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}

			}
		}).start();
	}

	private String convertType(String type) {
		String result = type;
		switch (type) {
		case "0":
			result = "在流畅道路上停止(Stop-F)";
			break;
		case "2":
			result = "减速";
			break;
		case "3":
			result = "在拥堵道路上停止 (Stop-C)";
			break;
		case "4":
			result = "隔离带的慢行";
			break;
		case "5":
			result = "逆行";
			break;
		case "6":
			result = "行人在紧急车道";
			break;
		case "7":
			result = "抛撒物";
			break;
		case "8":
			result = "车距超过阈值";
			break;
		case "9":
			result = "排队超过阈值";
			break;
		case "10":
			result = "丢失能见度";
			break;
		case "14":
			result = "是车速超过阈值";
			break;
		case "15":
			result = "隔离带上的停车(城市交通)";
			break;
		case "16":
			result = "排队停车(城市交通)";
			break;
		case "26":
			result = "针对于云台摄像机 VisioPad 的停止事件(VisioPad 系统的报警事件)";
			break;
		default:
			break;
		}
		return result;

	}

	public short bytes2Short2(byte[] b) {
		short i = (short) (((b[1] & 0xff) << 8) | b[0] & 0xff);
		return i;
	}

	public static String convertHexToString(String hex) {

		StringBuilder sb = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();

		for (int i = 0; i < hex.length() - 1; i += 2) {

			String s = hex.substring(i, (i + 2));
			int decimal = Integer.parseInt(s, 16);
			sb.append((char) decimal);
			sb2.append(decimal);
		}

		return sb.toString();
	}

	public byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	private byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	public String BinaryToHexString(byte[] bytes) {
		String hexStr = "0123456789ABCDEF";
		String result = "";
		String hex = "";
		for (byte b : bytes) {
			hex = String.valueOf(hexStr.charAt((b & 0xF0) >> 4));
			hex += String.valueOf(hexStr.charAt(b & 0x0F));
			result += hex + " ";
		}
		return result;
	}

	public void initConfig() {
		logger.info("start init conf");
		Properties props = new Properties();
		try {
			props.load(new FileInputStream("conf/itworks.config"));
			interval = Integer.parseInt(props.getProperty("interval", "30").trim());
			ip = props.getProperty("ip", "172.16.37.64");
			port = Integer.parseInt(props.getProperty("port", "33000").trim());
			VariablesMain.mysql_url = props.getProperty("mysql_url", "172.18.137.233:3306/SDKKDB").trim();
			VariablesMain.mysql_username = props.getProperty("mysql_username", "root").trim();
			VariablesMain.mysql_password = props.getProperty("mysql_password", "wiscom123@").trim();

		} catch (IOException e) {
			e.printStackTrace();
		}
		isInit = true;
	}

}
