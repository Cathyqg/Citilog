package com.wiscom.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.log4j.Logger;

import com.wiscom.main.VariablesMain;

public class Connections {
	private static Logger logger = Logger.getLogger(Connections.class);

	public static Connection conMysql = null;

	public static Connection getMysqlConn() {

		/**
		 * @method getConn()
		 * @return Connection
		 */
//		 String driver = "com.mysql.jdbc.Driver";
		String driver = "com.mysql.cj.jdbc.Driver";
		// String url =
		// "jdbc:mysql://localhost:3306/users?useUnicode=true&characterEncoding=UTF-8";
		String url = "jdbc:mysql://" + VariablesMain.mysql_url
				+ "?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true";
		// String username = "root";
		// String password = "wiscom123@";
		// Connection conn = null;

		try {
			Class.forName(driver);
			conMysql = (Connection) DriverManager.getConnection(url, VariablesMain.mysql_username,
					VariablesMain.mysql_password);
		} catch (ClassNotFoundException | SQLException e) {
			logger.error(e.getMessage());
			 e.printStackTrace();
			try {
				logger.info("try to reconnect...");
				Thread.sleep(3 * 1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return getMysqlConn();
		}

		return conMysql;
	}

}
