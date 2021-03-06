/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.attacksimulator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.productivity.java.syslog4j.Syslog;
import org.productivity.java.syslog4j.SyslogConfigIF;
import org.productivity.java.syslog4j.SyslogIF;
import org.productivity.java.syslog4j.SyslogRuntimeException;
import org.productivity.java.syslog4j.impl.net.udp.UDPNetSyslogConfig;

/**
 *
 * @author securonix
 */
public class SyslogDatabaseFeedGenerator extends FeedGeneratorInterface {

	Properties props;

	private static enum LEVEL {

		alert, critical, debug, emergency, error, info, notice, warn
	}
	private final Map<LEVEL, Pattern> linePatterns = new EnumMap<LEVEL, Pattern>(LEVEL.class);
	private final Map<LEVEL, String> facility = new EnumMap<LEVEL, String>(LEVEL.class);
	private final List<SyslogIF> syslogArray = new ArrayList<SyslogIF>();
	private final List<SyslogBean> syslogBeans = new ArrayList<SyslogBean>();
	private final Random generator2 = new Random(Calendar.getInstance().getTimeInMillis());
	private int interval;
	SyslogConfigIF config;

	public SyslogDatabaseFeedGenerator(String syslogConfigFile, Integer userid, String destinationIp, String destinationPort, int orderId, String frequency) {
		config = new UDPNetSyslogConfig(destinationIp, Integer.parseInt(destinationPort));
		try {
			Syslog.createInstance("Database" + orderId, config);
		} catch (Exception e) {
			System.out.println("Already created instance");
		}
	}

	@Override
	public void syslogProxyGenerator(String syslogConfigFile, Integer userid, String destinationIp, String destinationPort, int orderId, String frequency) {
		running = true;
		loadSyslogConfProperties(syslogConfigFile, destinationIp, destinationPort);
		// TODO code application logic here
		BytesGenerator randombytes = new BytesGenerator(0, 500);
		Calendar baseDate = Calendar.getInstance();
		try {
			/*
			 * Create the userset
			 */
			HashMap<String, ArrayList<String>> usersIps;
			ArrayList<String> users;
			ArrayList<String> transactions;
			for (SyslogBean syslogBean : syslogBeans) {

				usersIps = new HashMap<String, ArrayList<String>>();
				users = new ArrayList<String>();
				transactions = new ArrayList<String>();

				String fileline;
				BufferedReader FileRead = null;
				/*new BufferedReader(new FileReader("Proxy/user-IPAddress.csv"));
				 FileRead.readLine();

				 while ((fileline = FileRead.readLine()) != null) {
				 fileline = fileline.trim();

				 String[] split = fileline.split("\\,");

				 for (int i = 0; i < split.length; i++) {
				 split[i] = split[i].trim().replaceAll("\"", "");
				 }

				 ArrayList<String> ips = new ArrayList<String>();

				 ips.add(split[1]);
				 ips.add(split[2]);
				 ips.add(split[3]);

				 usersIps.put(split[0], ips);
				 users.add(split[0]);
				 }
                
				 FileRead.close();
				 */
				MySQLDBClass mysqldbclass = new MySQLDBClass();
				ArrayList<Object> userIpMappingsFromDb = mysqldbclass.getUserIPForSecUser(userid);
				users = (ArrayList<String>) userIpMappingsFromDb.get(2);
				usersIps = (HashMap<String, ArrayList<String>>) userIpMappingsFromDb.get(0);
				ArrayList<String> ipaddresses = (ArrayList<String>) userIpMappingsFromDb.get(1);
				syslogBean.setUsers(users);
				syslogBean.setUsersIps(usersIps);
				syslogBean.setIPAddresses(ipaddresses);
			}
			String[] serverIDs = generateRandomServerIds(10);
			String[] commandType = {"Data Defination", "Data Modification", "Data View", "Select Query", "Exec Stored Procedure", "Exec Trigger"};
			Map<String, ArrayList<String>> commandTypeArgMap = new HashMap<>();
			ArrayList<String> temp = new ArrayList<>();
			temp.add("CREATE TABLE");
			temp.add("CREATE DATABASE");
			temp.add("CREATE STORED PROC");
			temp.add("CREATE FUNCTION");
			temp.add("DROP DATABASE");
			temp.add("DROP TABLE");
			temp.add("DROP STORED PROC");
			temp.add("DROP FUNCTION");
			temp.add("DROP TRIGGER");
			temp.add("CREATE TRIGGER");
			commandTypeArgMap.put("Data Defination", temp);
			temp = new ArrayList<>();
			temp.add("UPDATE TABLE");
			temp.add("TRUNCATE TABLE");
			temp.add("ALTER TABLE");
			temp.add("ALTER FUNCTION");
			temp.add("ALTER STORED PROC");
			temp.add("ALTER TRIGGER");
			commandTypeArgMap.put("Data Modification", temp);
			temp = new ArrayList<>();
			temp.add("SELECT $COLS FROM ");
			commandTypeArgMap.put("Data View", temp);
			commandTypeArgMap.put("Select Query", temp);
			temp = new ArrayList<>();
			temp.add("EXEC STORED PROC");
			temp.add("EXEC FUNCTION ");
			commandTypeArgMap.put("Exec Stored Procedure", temp);
			temp = new ArrayList<>();
			temp.add("EXEC TRIGGER ");
			commandTypeArgMap.put("Exec Trigger", temp);

			SyslogBean syslogBean;
			String dateString = (baseDate.get(Calendar.MONTH) + 1) + "/" + baseDate.get(Calendar.DATE) + "/" + baseDate.get(Calendar.YEAR);
			while (running) {
				syslogBean = syslogBeans.get(generator2.nextInt(syslogBeans.size()));
				users = syslogBean.getUsers();
				String userName = users.get(generator2.nextInt(users.size()));
				baseDate = Calendar.getInstance();
				ArrayList<String> ipaddresses = syslogBean.getIPAddresses();
				String timeString = baseDate.get(Calendar.HOUR) + ":" + baseDate.get(Calendar.MINUTE) + ":" + baseDate.get(Calendar.SECOND);
				String command = commandType[generator2.nextInt(commandType.length)];
				System.out.println(dateString + "," + timeString + "," + ipaddresses.get(generator2.nextInt(ipaddresses.size())) + "," + generator2.nextLong() + "," + serverIDs[generator2.nextInt(serverIDs.length)] + "," + command + "," + commandTypeArgMap.get(command).get(generator2.nextInt(commandTypeArgMap.get(command).size())) + " " + UUID.randomUUID().toString() + ", The current destinationip and port: " + destinationIp + ":" + destinationPort);
				publish("," + ipaddresses.get(generator2.nextInt(ipaddresses.size())) + "," + generator2.nextLong() + "," + serverIDs[generator2.nextInt(serverIDs.length)] + "," + command + "," + commandTypeArgMap.get(command).get(generator2.nextInt(commandTypeArgMap.get(command).size())) + " " + UUID.randomUUID().toString().substring(0, generator2.nextInt(10)));

				MySQLDBClass tempObj = new MySQLDBClass();
				boolean shouldFire = tempObj.getAttackVectorsAndStep(orderId);
				System.out.println("Value of should fire variable: " + shouldFire);

				if (shouldFire) {
					//build proxy Attack based on the parameters saved from the UI
					HashMap<String, String> resultSetHMap = (tempObj).getAttackDefinition(orderId);
                    //Integer configid = Integer.parseInt(resultSetHMap.get("feedconfigid"));
					//ArrayList<String> keyList = (tempObject).getKeyList(configid);

					//create the String to be published.
					String attackString;
					attackString = resultSetHMap.get("test1") + "," + resultSetHMap.get("test2") + "," + "87654654768" + "," + dateString + "," + timeString + "," + "87.23.44.3" + "," + "type=SyslogDatabase" + " -- orderId: " + orderId + "\n";

					//publish the attack
					System.out.println(attackString);
					//update the current orderid showing that it has already been run
					tempObj.updateStepForOrderId(orderId);

					//set the next attack in a separate thread with a timer for it to go off after whatever time it was specified
					new Thread(new StepUpdater(orderId, 5000)).start();
				}

				Thread.sleep(interval);
			}
		} catch (InterruptedException ex) {
			Logger.getLogger(SyslogProxyFeedGenerator.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private String[] generateRandomHostIPAddresses(int size) {
		String[] rip = new String[size];
		for (int i = 0; i < rip.length; i++) {
			rip[i] = "10." + generator2.nextInt(254) + "." + generator2.nextInt(254) + "." + generator2.nextInt(254);
		}
		return rip;
	}

	private String[] generateRandomServerIds(int size) {
		String[] sids = new String[size];
		String[] rips = generateRandomHostIPAddresses(size);
		for (int i = 0; i < sids.length; i++) {
			sids[i] = rips[i] + "__" + UUID.randomUUID().toString().substring(0, 4);
		}
		return sids;
	}

	private void loadSyslogConfProperties(String filepath, String destinationIp, String destinationPort) {
		//Generate Properties from Syslog Config file

		props = new Properties();
		try {
			props.load(new FileReader(filepath));
			//System.out.println("Configuration loaded!");
		} catch (IOException ex) {
			System.err.println("Error loading properties file - " + ex.getMessage());
			return;
		}
		String userIpFile;
		String transactionFile;
		for (int i = 1; i <= 10; i++) {
			userIpFile = props.getProperty("HRDataFileBase");
			transactionFile = props.getProperty("transactionFile");
			SyslogBean syslogBean = new SyslogBean();
			syslogBean.setUserIpFile(userIpFile);
			syslogBean.setTransactionFile(transactionFile);
			syslogBeans.add(syslogBean);
		}

		interval = Integer.parseInt(props.getProperty("interval"));

		SyslogIF syslog;
		SyslogConfigIF config;

		String PROTOCOL = "protocol_1";
		String HOST = destinationIp;
		String PORT = destinationPort;

		try {
			syslog = Syslog.getInstance("Database" + orderId);
//            config = syslog.getConfig();
//            config.setHost(HOST);
//            config.setPort(Integer.parseInt(PORT));

			String pattern;
			for (SyslogDatabaseFeedGenerator.LEVEL level : SyslogDatabaseFeedGenerator.LEVEL.values()) {
				pattern = props.getProperty(level.name() + ".pattern");
				if (pattern != null) {
					linePatterns.put(level, Pattern.compile(pattern));
					facility.put(level, props.getProperty(level.name() + ".facility"));
				}
			}
			syslogArray.add(syslog);

		} catch (SyslogRuntimeException ex) {
			System.err.println("Error reading file - " + ex.getMessage());
		} catch (NumberFormatException ex) {
			System.err.println("Error reading file - " + ex.getMessage());
		}
	}

	private void publish(String line) {
		if (line.trim().length() > 0) {
			Pattern pattern;

			for (SyslogIF syslog : syslogArray) {
				SyslogDatabaseFeedGenerator.LEVEL level = SyslogDatabaseFeedGenerator.LEVEL.values()[generator2.nextInt(SyslogDatabaseFeedGenerator.LEVEL.values().length)];
				pattern = linePatterns.get(level);
				if (pattern.matcher(line).matches()) {
					syslog.getConfig().setFacility(facility.get(level));
				}
				switch (level) {
					case alert:
						syslog.alert(line);
						break;
					case critical:
						syslog.critical(line);
						break;
					case debug:
						syslog.debug(line);
						break;
					case emergency:
						syslog.emergency(line);
						break;
					case error:
						syslog.error(line);
						break;
					case info:
						syslog.info(line);
						break;
					case notice:
						syslog.notice(line);
						break;
					case warn:
						syslog.warn(line);
						break;
				}
			}
		}
	}

	@Override
	public void shutdown() {
		running = false;
	}
}
