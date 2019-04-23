package com.wiscom.thread;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;

import org.apache.log4j.Logger;

import com.wiscom.util.Connections;

public class CleanThread extends Thread{
	private static Logger logger = Logger.getLogger(CleanThread.class);
	
	@Override
	public void run() {
		while (true) {
			Calendar calendar = Calendar.getInstance();
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			if (day == 1) {
				logger.info("start to clean data");
				new Client_Thread_new().initConfig();
				String sql = "DELETE FROM ctlog_alert_info "
						+ "where COLLECTEDTIME < date_add(curdate(),INTERVAL -1 month)";
				if (Connections.conMysql == null) {
					Connections.conMysql = Connections.getMysqlConn();
				}
				try {
					PreparedStatement ps = Connections.conMysql.prepareStatement(sql);
					ps.execute();
					ps.close();
					logger.info("delete success");
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(60 * 60 * 24 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

}
