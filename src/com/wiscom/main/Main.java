package com.wiscom.main;

import com.wiscom.thread.CleanThread;
import com.wiscom.thread.Client_Thread_new;

public class Main {

	public static void main(String[] args) {
//     	Client_singleThread client = new Client_singleThread();
     	Client_Thread_new client = new Client_Thread_new();
//		Client_Thread client = new Client_Thread();
		client.start();
		new CleanThread().start();
		
	}
}
