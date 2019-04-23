package com.wiscom.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class JdbcUtil {
	private static Logger logger = Logger.getLogger(JdbcUtil.class);

	public static Map<String, Object> selectMysql(String sql) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		PreparedStatement ps = null;
		ResultSet result = null;
		try {
			if (Connections.conMysql == null) {
				Connections.conMysql = Connections.getMysqlConn();
			}
			ps = Connections.conMysql.prepareStatement(sql);
			result = ps.executeQuery();
		} catch (SQLException e) {
			// e.printStackTrace();
			logger.error("select ERR:" + sql + " " + e.getMessage());
			Connections.getMysqlConn();
			return selectMysql(sql);
		}
		resultMap.put("ResultSet", result);
		resultMap.put("PreparedStatement", ps);
		return resultMap;
	}
	
	

}
