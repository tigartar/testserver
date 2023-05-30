package com.wurmonline.server;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.utils.HttpResponseStatus;
import com.wurmonline.shared.util.IoUtilities;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public final class Eigc implements MiscConstants {
   private static final Logger logger = Logger.getLogger(Eigc.class.getName());
   private static final String LOAD_ALL_EIGC = "SELECT * FROM EIGC";
   private static final String INSERT_EIGC_ACCOUNT = "INSERT INTO EIGC(USERNAME,PASSWORD,SERVICEBUNDLE,EXPIRATION,EMAIL) VALUES (?,?,?,?,?)";
   private static final String DELETE_EIGC_ACCOUNT = "DELETE FROM EIGC WHERE USERNAME=?";
   private static final LinkedList<EigcClient> EIGC_CLIENTS = new LinkedList<>();
   private static final String HTTP_CHARACTER_ENCODING = "UTF-8";
   private static final int initialAccountsToProvision = Servers.isThisATestServer() ? 5 : 25;
   private static final int accountsToProvision = Servers.isThisATestServer() ? 5 : 25;
   private static boolean isProvisioning = false;
   public static final String SERVICE_PROXIMITY = "proximity";
   public static final String SERVICE_P2P = "p2p";
   public static final String SERVICE_TEAM = "team";
   public static final String SERVICE_LECTURE = "lecture";
   public static final String SERVICE_HIFI = "hifi";
   public static final String SERVICES_FREE = "proximity";
   public static final String SERVICES_BUNDLE = "proximity,team,p2p,hifi";
   public static final String PROTOCOL_PROVISIONING = "https://";
   private static String HOST_PROVISIONING = "bla";
   public static String URL_PROXIMITY = "bla";
   public static String URL_SIP_REGISTRAR = "bla";
   public static String URL_SIP_PROXY = "bla";
   private static String EIGC_REALM = "bla";
   private static final int PORT_PROVISIONING = 5002;
   private static String URL_PROVISIONING = "https://" + HOST_PROVISIONING + ":" + 5002 + "/";
   private static String CREATE_URL = URL_PROVISIONING + "userprovisioning/v1/create/" + EIGC_REALM + "/";
   private static String MODIFY_URL = URL_PROVISIONING + "userprovisioning/v1/modify/" + EIGC_REALM + "/";
   private static String VIEW_URL = URL_PROVISIONING + "userprovisioning/v1/view/" + EIGC_REALM + "/";
   private static String DELETE_URL = URL_PROVISIONING + "userprovisioning/v1/delete/" + EIGC_REALM + "/";
   private static String EIGC_PASSWORD = "tL4PDKim";

   private Eigc() {
   }

   private static final void setEigcHttpsOverride() {
      HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
         @Override
         public boolean verify(String hostname, SSLSession sslSession) {
            return hostname.equals(Eigc.HOST_PROVISIONING);
         }
      });
   }

   public static final void loadAllAccounts() {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM EIGC");
         rs = ps.executeQuery();

         while(rs.next()) {
            String eigcUserId = rs.getString("USERNAME");
            EigcClient eigclient = new EigcClient(
               eigcUserId, rs.getString("PASSWORD"), rs.getString("SERVICEBUNDLE"), rs.getLong("EXPIRATION"), rs.getString("EMAIL")
            );
            EIGC_CLIENTS.add(eigclient);
         }

         logger.log(Level.INFO, "Loaded " + EIGC_CLIENTS.size() + " eigc accounts.");
      } catch (SQLException var10) {
         logger.log(Level.WARNING, "Problem loading eigc clients for server due to " + var10.getMessage(), (Throwable)var10);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
      }

      if (Constants.isEigcEnabled && EIGC_CLIENTS.size() < initialAccountsToProvision) {
         try {
            installCert();
            provisionAccounts(initialAccountsToProvision, false);
         } catch (Exception var9) {
            logger.log(Level.WARNING, var9.getMessage(), (Throwable)var9);
         }
      }

      if (Servers.localServer.testServer) {
         HOST_PROVISIONING = "provisioning.eigctestnw.com";
         URL_PROXIMITY = "sip:wurmonline.eigctestnw.com";
         URL_SIP_REGISTRAR = "wurmonline.eigctestnw.com";
         URL_SIP_PROXY = "sip:gateway.eigctestnw.com:35060";
         EIGC_REALM = "wurmonline.eigctestnw.com";
         EIGC_PASSWORD = "admin";
         changeEigcUrls();
      }
   }

   private static final void changeEigcUrls() {
   }

   public static final void installCert() throws Exception {
   }

   public static final void deleteAccounts(final int nums) {
      if (Constants.isEigcEnabled && !isProvisioning) {
         isProvisioning = true;
         (new Thread() {
            @Override
            public void run() {
               for(int x = 0; x < nums; ++x) {
                  String userName = "Wurmpool" + Servers.localServer.id * 20000 + x + 1;
                  Eigc.logger.log(Level.INFO, Eigc.deleteUser(userName));
               }

               Eigc.isProvisioning = false;
            }
         }).start();
      }
   }

   public static final void deleteAccounts() {
      if (!isProvisioning) {
         final EigcClient[] clients = EIGC_CLIENTS.toArray(new EigcClient[EIGC_CLIENTS.size()]);
         isProvisioning = true;
         (new Thread() {
            @Override
            public void run() {
               for(EigcClient client : clients) {
                  String userName = client.getClientId();
                  Eigc.logger.log(Level.INFO, Eigc.deleteUser(userName));
               }

               Eigc.isProvisioning = false;
            }
         }).start();
      }
   }

   public static final void provisionAccounts(final int numberToProvision, final boolean overRide) {
      if ((Constants.isEigcEnabled || overRide) && !isProvisioning) {
         isProvisioning = true;
         (new Thread() {
            @Override
            public void run() {
               String[] paramNames = new String[]{"servicebundle"};
               String[] paramVals = new String[]{"proximity"};
               String userName = "Wurmpool" + (Servers.localServer.id * 20000 + Eigc.EIGC_CLIENTS.size() + 1);
               int failed = 0;

               for(int x = 0; x < numberToProvision; ++x) {
                  try {
                     userName = "Wurmpool" + (Servers.localServer.id * 20000 + Eigc.EIGC_CLIENTS.size() + failed + 1);
                     String response = Eigc.httpPost(Eigc.CREATE_URL, paramNames, paramVals, userName);
                     if (Eigc.logger.isLoggable(Level.INFO)) {
                        Eigc.logger.info("Called " + Eigc.CREATE_URL + " with user name " + userName + " and received response " + response);
                     }

                     boolean created = false;

                     try {
                        InputSource inStream = new InputSource();
                        inStream.setCharacterStream(new StringReader(response));
                        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                        Document doc = dBuilder.parse(inStream);
                        doc.getDocumentElement().normalize();
                        Eigc.logger.log(Level.INFO, "Root element :" + doc.getDocumentElement().getNodeName());
                        NodeList nList = doc.getElementsByTagName("user");

                        for(int temp = 0; temp < nList.getLength(); ++temp) {
                           Node nNode = nList.item(temp);
                           if (nNode.getNodeType() == 1) {
                              Element eElement = (Element)nNode;
                              String uname = Eigc.getTagValue("username", eElement);
                              Eigc.logger.log(Level.INFO, "UserName : " + uname);
                              String authid = Eigc.getTagValue("authid", eElement);
                              Eigc.logger.log(Level.INFO, "Auth Id : " + authid);
                              String password = Eigc.getTagValue("passwd", eElement);
                              Eigc.logger.log(Level.INFO, "Password : " + password);
                              String services = Eigc.getTagValue("servicebundle", eElement);
                              Eigc.logger.log(Level.INFO, "Service bundle : " + services);
                              Eigc.createAccount(uname, password, services, Long.MAX_VALUE, "");
                              created = true;
                           }
                        }
                     } catch (Exception var20) {
                        Eigc.logger.log(Level.WARNING, var20.getMessage());
                     }

                     if (!created) {
                        ++failed;
                     }
                  } catch (Exception var21) {
                     Eigc.logger.log(Level.WARNING, "Problem calling " + Eigc.CREATE_URL + " with user name " + userName, (Throwable)var21);
                  }
               }

               Eigc.isProvisioning = false;
               if (overRide) {
                  com.wurmonline.server.Constants.isEigcEnabled = true;
               }
            }
         }).start();
      }
   }

   private static String getTagValue(String sTag, Element eElement) {
      NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
      Node nValue = nlList.item(0);
      return nValue.getNodeValue();
   }

   public static final String getEigcInfo(String eigcId) {
      try {
         return httpGet(VIEW_URL, eigcId);
      } catch (IOException var2) {
         logger.log(Level.INFO, var2.getMessage(), (Throwable)var2);
         return "Failed to retrieve information about " + eigcId;
      }
   }

   public static final String deleteUser(String userId) {
      try {
         String[] paramNames = new String[0];
         String[] paramVals = new String[0];
         String answer = httpDelete(DELETE_URL, paramNames, paramVals, userId);
         logger.log(Level.INFO, "Called " + DELETE_URL + " with userId=" + userId);
         if (answer.toLowerCase().contains("<rsp stat=\"ok\">")) {
            logger.log(Level.INFO, "Deleting " + userId + " from database.");
            deleteAccount(userId);
         }

         return answer;
      } catch (Exception var4) {
         logger.log(Level.INFO, var4.getMessage(), (Throwable)var4);
         return "Failed to delete " + userId;
      }
   }

   public static final String modifyUser(String userId, String servicesAsCommaSeparatedString, long expiration) {
      try {
         String[] paramNames = new String[]{"servicebundle"};
         String[] paramVals = new String[]{servicesAsCommaSeparatedString};

         try {
            String response = httpPost(MODIFY_URL, paramNames, paramVals, userId);
            if (logger.isLoggable(Level.INFO)) {
               logger.info("Called " + MODIFY_URL + " with user name " + userId + " and received response " + response);
            }

            try {
               InputSource inStream = new InputSource();
               inStream.setCharacterStream(new StringReader(response));
               DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
               DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
               Document doc = dBuilder.parse(inStream);
               doc.getDocumentElement().normalize();
               logger.log(Level.INFO, "Root element :" + doc.getDocumentElement().getNodeName());
               NodeList nList = doc.getElementsByTagName("user");

               for(int temp = 0; temp < nList.getLength(); ++temp) {
                  Node nNode = nList.item(temp);
                  if (nNode.getNodeType() == 1) {
                     Element eElement = (Element)nNode;
                     String uname = getTagValue("username", eElement);
                     logger.log(Level.INFO, "UserName : " + uname);
                     String authid = getTagValue("authid", eElement);
                     logger.log(Level.INFO, "Auth Id : " + authid);
                     String password = getTagValue("passwd", eElement);
                     logger.log(Level.INFO, "Password : " + password);
                     String services = getTagValue("servicebundle", eElement);
                     logger.log(Level.INFO, "Service bundle : " + services);
                     EigcClient old = getClientWithId(uname);
                     if (old != null) {
                        updateAccount(uname, password, services, expiration, old.getAccountName());
                     } else {
                        updateAccount(uname, password, services, expiration, "");
                     }
                  }
               }
            } catch (Exception var20) {
               logger.log(Level.WARNING, var20.getMessage());
            }
         } catch (Exception var21) {
            logger.log(Level.WARNING, "Problem calling " + CREATE_URL + " with user name " + userId, (Throwable)var21);
         }
      } catch (Exception var22) {
         logger.log(Level.INFO, var22.getMessage(), (Throwable)var22);
      }

      return "Failed to modify " + userId;
   }

   public static String httpPost(String urlStr, String[] paramName, String[] paramVal, String userName) throws Exception {
      URL url = new URL(urlStr + userName);
      setEigcHttpsOverride();
      HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();

      StringBuilder sb;
      try {
         conn.setRequestMethod("POST");
         conn.setDoOutput(true);
         conn.setDoInput(true);
         conn.setUseCaches(false);
         conn.setAllowUserInteraction(false);
         conn.setRequestProperty("Authorization", "Digest " + EIGC_PASSWORD);
         conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
         OutputStream out = conn.getOutputStream();
         Writer writer = new OutputStreamWriter(out, "UTF-8");

         try {
            for(int i = 0; i < paramName.length; ++i) {
               writer.write(paramName[i]);
               writer.write("=");
               logger.log(Level.INFO, "Sending " + paramName[i] + "=" + paramVal[i]);
               writer.write(URLEncoder.encode(paramVal[i], "UTF-8"));
               writer.write("&");
            }
         } finally {
            IoUtilities.closeClosable(writer);
            IoUtilities.closeClosable(out);
         }

         if (conn.getResponseCode() != HttpResponseStatus.OK.getStatusCode()) {
            throw new IOException(conn.getResponseMessage());
         }

         BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

         try {
            sb = new StringBuilder();

            String line;
            while((line = rd.readLine()) != null) {
               sb.append(line);
            }
         } finally {
            IoUtilities.closeClosable(rd);
         }
      } finally {
         IoUtilities.closeHttpURLConnection(conn);
      }

      return sb.toString();
   }

   public static String httpDelete(String urlStr, String[] paramName, String[] paramVal, String userName) throws Exception {
      URL url = new URL(urlStr + userName);
      setEigcHttpsOverride();
      HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();

      StringBuilder sb;
      try {
         conn.setRequestMethod("DELETE");
         conn.setDoOutput(true);
         conn.setDoInput(true);
         conn.setUseCaches(false);
         conn.setAllowUserInteraction(false);
         conn.setRequestProperty("Authorization", "Digest " + EIGC_PASSWORD);
         conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
         OutputStream out = conn.getOutputStream();
         Writer writer = new OutputStreamWriter(out, "UTF-8");

         try {
            for(int i = 0; i < paramName.length; ++i) {
               writer.write(paramName[i]);
               writer.write("=");
               logger.log(Level.INFO, "Sending " + paramName[i] + "=" + paramVal[i]);
               writer.write(URLEncoder.encode(paramVal[i], "UTF-8"));
               writer.write("&");
            }
         } finally {
            IoUtilities.closeClosable(writer);
            IoUtilities.closeClosable(out);
         }

         if (conn.getResponseCode() != HttpResponseStatus.OK.getStatusCode()) {
            throw new IOException(conn.getResponseMessage());
         }

         BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

         try {
            sb = new StringBuilder();

            String line;
            while((line = rd.readLine()) != null) {
               sb.append(line);
            }
         } finally {
            IoUtilities.closeClosable(rd);
         }
      } finally {
         IoUtilities.closeHttpURLConnection(conn);
      }

      return sb.toString();
   }

   public static String httpGet(String urlStr, String userName) throws IOException {
      URL url = new URL(urlStr + userName);
      setEigcHttpsOverride();
      HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
      conn.setRequestProperty("Authorization", EIGC_PASSWORD);
      if (conn.getResponseCode() != HttpResponseStatus.OK.getStatusCode()) {
         throw new IOException(conn.getResponseMessage());
      } else {
         StringBuilder sb = new StringBuilder();

         try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while((line = rd.readLine()) != null) {
               sb.append(line);
            }

            rd.close();
         }

         conn.disconnect();
         return sb.toString();
      }
   }

   public static final String addPlayer(String playerName) {
      EigcClient found = getClientForPlayer(playerName);
      if (found != null) {
         logger.log(Level.INFO, playerName + " already in use: " + found.getClientId());
         return found.getClientId();
      } else {
         for(EigcClient gcc : EIGC_CLIENTS) {
            if (gcc.getAccountName().equalsIgnoreCase(playerName)) {
               return setClientUsed(gcc, playerName, "found unused reserved client");
            }
         }

         EigcClient client = null;

         for(EigcClient gcc : EIGC_CLIENTS) {
            if (!gcc.isUsed() && gcc.getAccountName().length() <= 0) {
               client = gcc;
               break;
            }
         }

         if (client != null) {
            EIGC_CLIENTS.remove(client);
            EIGC_CLIENTS.add(client);
            return setClientUsed(client, playerName, "found unused free client");
         } else {
            provisionAccounts(accountsToProvision, false);
            return "";
         }
      }
   }

   private static final String setClientUsed(EigcClient client, String playerName, String reason) {
      client.setPlayerName(playerName.toLowerCase(), reason);
      client.setAccountName(playerName.toLowerCase());
      return client.getClientId();
   }

   public static final EigcClient getClientForPlayer(String playerName) {
      String nameSearched = LoginHandler.raiseFirstLetter(playerName);
      boolean mustTrim = playerName.indexOf(" ") > 0;
      if (mustTrim) {
         nameSearched = playerName.substring(0, playerName.indexOf(" "));
         logger.log(Level.INFO, "Trimmed " + playerName + " to " + nameSearched);
      }

      for(EigcClient client : EIGC_CLIENTS) {
         if (client.getPlayerName().equalsIgnoreCase(nameSearched)) {
            return client;
         }
      }

      return null;
   }

   public static final EigcClient getReservedClientForPlayer(String playerName) {
      String nameSearched = LoginHandler.raiseFirstLetter(playerName);
      boolean mustTrim = playerName.indexOf(" ") > 0;
      if (mustTrim) {
         nameSearched = playerName.substring(0, playerName.indexOf(" "));
         logger.log(Level.INFO, "Trimmed " + playerName + " to " + nameSearched);
      }

      for(EigcClient client : EIGC_CLIENTS) {
         if (client.getAccountName().equalsIgnoreCase(nameSearched)) {
            return client;
         }
      }

      return null;
   }

   public static final EigcClient removePlayer(String playerName) {
      EigcClient client = getClientForPlayer(playerName);
      if (client != null) {
         client.setPlayerName("", "removed");
         if (client.getExpiration() == Long.MAX_VALUE || client.getExpiration() < System.currentTimeMillis()) {
            client.setAccountName("");
         }
      }

      return client;
   }

   public static final void sendAllClientInfo(Communicator comm) {
      for(EigcClient entry : EIGC_CLIENTS) {
         comm.sendNormalServerMessage(
            "ClientId: "
               + entry.getClientId()
               + ": user: "
               + entry.getPlayerName()
               + " occupied="
               + entry.isUsed()
               + " accountname="
               + entry.getAccountName()
               + " secs since last use="
               + (entry.isUsed() ? entry.timeSinceLastUse() : "N/A")
         );
      }
   }

   public static final EigcClient transferPlayer(String playerName) {
      final EigcClient client = getReservedClientForPlayer(playerName);
      if (client != null) {
         if (client.getExpiration() < Long.MAX_VALUE && client.getExpiration() > System.currentTimeMillis()) {
            return client;
         }

         logger.log(Level.INFO, "Setting expired reserved client to unused at server transfer. This should be detected earlier.");
         client.setAccountName("");
         (new Thread() {
            @Override
            public void run() {
               Eigc.modifyUser(client.getClientId(), "proximity", Long.MAX_VALUE);
            }
         }).start();
      }

      return null;
   }

   public static final void updateAccount(String eigcUserId, String clientPass, String services, long expirationTime, String accountName) {
      EigcClient oldClient = getClientWithId(eigcUserId);
      if (oldClient == null) {
         createAccount(eigcUserId, clientPass, services, expirationTime, accountName.toLowerCase());
      } else {
         oldClient.setPassword(clientPass);
         oldClient.setServiceBundle(services);
         oldClient.setExpiration(expirationTime);
         oldClient.setAccountName(accountName.toLowerCase());
         oldClient.updateAccount();
         Players.getInstance().updateEigcInfo(oldClient);
      }
   }

   public static final void createAccount(String eigcUserId, String clientPass, String services, long expirationTime, String accountName) {
      EigcClient eigclient = new EigcClient(eigcUserId, clientPass, services, expirationTime, accountName.toLowerCase());
      EIGC_CLIENTS.add(eigclient);
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement("INSERT INTO EIGC(USERNAME,PASSWORD,SERVICEBUNDLE,EXPIRATION,EMAIL) VALUES (?,?,?,?,?)");
         ps.setString(1, eigcUserId);
         ps.setString(2, clientPass);
         ps.setString(3, services);
         ps.setLong(4, expirationTime);
         ps.setString(5, accountName.toLowerCase());
         ps.executeUpdate();
         logger.log(Level.INFO, "Successfully saved " + eigcUserId);
      } catch (SQLException var13) {
         logger.log(Level.WARNING, var13.getMessage(), (Throwable)var13);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public static final EigcClient getClientWithId(String eigcUserId) {
      for(EigcClient entry : EIGC_CLIENTS) {
         if (entry.getClientId().equalsIgnoreCase(eigcUserId)) {
            return entry;
         }
      }

      return null;
   }

   static void removeClientWithId(String eigcUserId) {
      EigcClient toRemove = null;

      for(EigcClient c : EIGC_CLIENTS) {
         if (c.getClientId().equalsIgnoreCase(eigcUserId)) {
            toRemove = c;
            break;
         }
      }

      if (toRemove != null) {
         EIGC_CLIENTS.remove(toRemove);
      }
   }

   public static final void deleteAccount(String eigcUserId) {
      removeClientWithId(eigcUserId);
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getLoginDbCon();
         ps = dbcon.prepareStatement("DELETE FROM EIGC WHERE USERNAME=?");
         ps.setString(1, eigcUserId);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }
}
