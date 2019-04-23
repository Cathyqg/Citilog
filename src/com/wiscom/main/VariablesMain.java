package com.wiscom.main;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.wiscom.bean.AlertDetail;


public class VariablesMain {

	public static String mysql_url = "localhost/dpywk";
	public static String mysql_username = "root";
	public static String mysql_password = "123";
	public static Map<String,AlertDetail> alertMap = Collections.synchronizedMap(new HashMap<String, AlertDetail>());
	public static Map<String,String> confMap = Collections.synchronizedMap(new HashMap<String, String>());
	
}
