/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

import com.wurmonline.server.Constants;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.EigcClient;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Players;
import com.wurmonline.server.Servers;
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

public final class Eigc
implements MiscConstants {
    private static final Logger logger = Logger.getLogger(Eigc.class.getName());
    private static final String LOAD_ALL_EIGC = "SELECT * FROM EIGC";
    private static final String INSERT_EIGC_ACCOUNT = "INSERT INTO EIGC(USERNAME,PASSWORD,SERVICEBUNDLE,EXPIRATION,EMAIL) VALUES (?,?,?,?,?)";
    private static final String DELETE_EIGC_ACCOUNT = "DELETE FROM EIGC WHERE USERNAME=?";
    private static final LinkedList<EigcClient> EIGC_CLIENTS = new LinkedList();
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
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){

            @Override
            public boolean verify(String hostname, SSLSession sslSession) {
                return hostname.equals(HOST_PROVISIONING);
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void loadAllAccounts() {
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getLoginDbCon();
            ps = dbcon.prepareStatement(LOAD_ALL_EIGC);
            rs = ps.executeQuery();
            while (rs.next()) {
                String eigcUserId = rs.getString("USERNAME");
                EigcClient eigclient = new EigcClient(eigcUserId, rs.getString("PASSWORD"), rs.getString("SERVICEBUNDLE"), rs.getLong("EXPIRATION"), rs.getString("EMAIL"));
                EIGC_CLIENTS.add(eigclient);
            }
            logger.log(Level.INFO, "Loaded " + EIGC_CLIENTS.size() + " eigc accounts.");
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Problem loading eigc clients for server due to " + sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        if (Constants.isEigcEnabled && EIGC_CLIENTS.size() < initialAccountsToProvision) {
            try {
                Eigc.installCert();
                Eigc.provisionAccounts(initialAccountsToProvision, false);
            }
            catch (Exception exc) {
                logger.log(Level.WARNING, exc.getMessage(), exc);
            }
        }
        if (Servers.localServer.testServer) {
            HOST_PROVISIONING = "provisioning.eigctestnw.com";
            URL_PROXIMITY = "sip:wurmonline.eigctestnw.com";
            URL_SIP_REGISTRAR = "wurmonline.eigctestnw.com";
            URL_SIP_PROXY = "sip:gateway.eigctestnw.com:35060";
            EIGC_REALM = "wurmonline.eigctestnw.com";
            EIGC_PASSWORD = "admin";
            Eigc.changeEigcUrls();
        }
    }

    private static final void changeEigcUrls() {
    }

    public static final void installCert() throws Exception {
    }

    public static final void deleteAccounts(int nums) {
        if (Constants.isEigcEnabled && !isProvisioning) {
            isProvisioning = true;
            new Thread(){

                @Override
                public void run() {
                    for (int x = 0; x < nums; ++x) {
                        String userName = "Wurmpool" + Servers.localServer.id * 20000 + x + 1;
                        logger.log(Level.INFO, Eigc.deleteUser(userName));
                    }
                    isProvisioning = false;
                }
            }.start();
        }
    }

    public static final void deleteAccounts() {
        if (!isProvisioning) {
            EigcClient[] clients = EIGC_CLIENTS.toArray(new EigcClient[EIGC_CLIENTS.size()]);
            isProvisioning = true;
            new Thread(){

                @Override
                public void run() {
                    for (EigcClient client : clients) {
                        String userName = client.getClientId();
                        logger.log(Level.INFO, Eigc.deleteUser(userName));
                    }
                    isProvisioning = false;
                }
            }.start();
        }
    }

    public static final void provisionAccounts(int numberToProvision, boolean overRide) {
        if ((Constants.isEigcEnabled || overRide) && !isProvisioning) {
            isProvisioning = true;
            new Thread(){

                @Override
                public void run() {
                    String[] paramNames = new String[]{"servicebundle"};
                    String[] paramVals = new String[]{"proximity"};
                    String userName = "Wurmpool" + (Servers.localServer.id * 20000 + EIGC_CLIENTS.size() + 1);
                    int failed = 0;
                    for (int x = 0; x < numberToProvision; ++x) {
                        try {
                            userName = "Wurmpool" + (Servers.localServer.id * 20000 + EIGC_CLIENTS.size() + failed + 1);
                            String response = Eigc.httpPost(CREATE_URL, paramNames, paramVals, userName);
                            if (logger.isLoggable(Level.INFO)) {
                                logger.info("Called " + CREATE_URL + " with user name " + userName + " and received response " + response);
                            }
                            boolean created = false;
                            try {
                                InputSource inStream = new InputSource();
                                inStream.setCharacterStream(new StringReader(response));
                                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                                Document doc = dBuilder.parse(inStream);
                                doc.getDocumentElement().normalize();
                                logger.log(Level.INFO, "Root element :" + doc.getDocumentElement().getNodeName());
                                NodeList nList = doc.getElementsByTagName("user");
                                for (int temp = 0; temp < nList.getLength(); ++temp) {
                                    Node nNode = nList.item(temp);
                                    if (nNode.getNodeType() != 1) continue;
                                    Element eElement = (Element)nNode;
                                    String uname = Eigc.getTagValue("username", eElement);
                                    logger.log(Level.INFO, "UserName : " + uname);
                                    String authid = Eigc.getTagValue("authid", eElement);
                                    logger.log(Level.INFO, "Auth Id : " + authid);
                                    String password = Eigc.getTagValue("passwd", eElement);
                                    logger.log(Level.INFO, "Password : " + password);
                                    String services = Eigc.getTagValue("servicebundle", eElement);
                                    logger.log(Level.INFO, "Service bundle : " + services);
                                    Eigc.createAccount(uname, password, services, Long.MAX_VALUE, "");
                                    created = true;
                                }
                            }
                            catch (Exception e) {
                                logger.log(Level.WARNING, e.getMessage());
                            }
                            if (created) continue;
                            ++failed;
                            continue;
                        }
                        catch (Exception e) {
                            logger.log(Level.WARNING, "Problem calling " + CREATE_URL + " with user name " + userName, e);
                        }
                    }
                    isProvisioning = false;
                    if (overRide) {
                        Constants.isEigcEnabled = true;
                    }
                }
            }.start();
        }
    }

    private static String getTagValue(String sTag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
        Node nValue = nlList.item(0);
        return nValue.getNodeValue();
    }

    public static final String getEigcInfo(String eigcId) {
        try {
            String answer = Eigc.httpGet(VIEW_URL, eigcId);
            return answer;
        }
        catch (IOException iox) {
            logger.log(Level.INFO, iox.getMessage(), iox);
            return "Failed to retrieve information about " + eigcId;
        }
    }

    public static final String deleteUser(String userId) {
        try {
            String[] paramNames = new String[]{};
            String[] paramVals = new String[]{};
            String answer = Eigc.httpDelete(DELETE_URL, paramNames, paramVals, userId);
            logger.log(Level.INFO, "Called " + DELETE_URL + " with userId=" + userId);
            if (answer.toLowerCase().contains("<rsp stat=\"ok\">")) {
                logger.log(Level.INFO, "Deleting " + userId + " from database.");
                Eigc.deleteAccount(userId);
            }
            return answer;
        }
        catch (Exception iox) {
            logger.log(Level.INFO, iox.getMessage(), iox);
            return "Failed to delete " + userId;
        }
    }

    public static final String modifyUser(String userId, String servicesAsCommaSeparatedString, long expiration) {
        try {
            String[] paramNames = new String[]{"servicebundle"};
            String[] paramVals = new String[]{servicesAsCommaSeparatedString};
            try {
                String response = Eigc.httpPost(MODIFY_URL, paramNames, paramVals, userId);
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
                    for (int temp = 0; temp < nList.getLength(); ++temp) {
                        Node nNode = nList.item(temp);
                        if (nNode.getNodeType() != 1) continue;
                        Element eElement = (Element)nNode;
                        String uname = Eigc.getTagValue("username", eElement);
                        logger.log(Level.INFO, "UserName : " + uname);
                        String authid = Eigc.getTagValue("authid", eElement);
                        logger.log(Level.INFO, "Auth Id : " + authid);
                        String password = Eigc.getTagValue("passwd", eElement);
                        logger.log(Level.INFO, "Password : " + password);
                        String services = Eigc.getTagValue("servicebundle", eElement);
                        logger.log(Level.INFO, "Service bundle : " + services);
                        EigcClient old = Eigc.getClientWithId(uname);
                        if (old != null) {
                            Eigc.updateAccount(uname, password, services, expiration, old.getAccountName());
                            continue;
                        }
                        Eigc.updateAccount(uname, password, services, expiration, "");
                    }
                }
                catch (Exception e) {
                    logger.log(Level.WARNING, e.getMessage());
                }
            }
            catch (Exception e) {
                logger.log(Level.WARNING, "Problem calling " + CREATE_URL + " with user name " + userId, e);
            }
        }
        catch (Exception iox) {
            logger.log(Level.INFO, iox.getMessage(), iox);
        }
        return "Failed to modify " + userId;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String httpPost(String urlStr, String[] paramName, String[] paramVal, String userName) throws Exception {
        StringBuilder sb;
        URL url = new URL(urlStr + userName);
        Eigc.setEigcHttpsOverride();
        HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(false);
            conn.setRequestProperty("Authorization", "Digest " + EIGC_PASSWORD);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            OutputStream out = conn.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(out, HTTP_CHARACTER_ENCODING);
            try {
                for (int i = 0; i < paramName.length; ++i) {
                    writer.write(paramName[i]);
                    writer.write("=");
                    logger.log(Level.INFO, "Sending " + paramName[i] + "=" + paramVal[i]);
                    writer.write(URLEncoder.encode(paramVal[i], HTTP_CHARACTER_ENCODING));
                    writer.write("&");
                }
            }
            finally {
                IoUtilities.closeClosable(writer);
                IoUtilities.closeClosable(out);
            }
            if (conn.getResponseCode() != HttpResponseStatus.OK.getStatusCode()) {
                throw new IOException(conn.getResponseMessage());
            }
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            try {
                String line;
                sb = new StringBuilder();
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
            }
            finally {
                IoUtilities.closeClosable(rd);
            }
        }
        finally {
            IoUtilities.closeHttpURLConnection(conn);
        }
        return sb.toString();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String httpDelete(String urlStr, String[] paramName, String[] paramVal, String userName) throws Exception {
        StringBuilder sb;
        URL url = new URL(urlStr + userName);
        Eigc.setEigcHttpsOverride();
        HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
        try {
            conn.setRequestMethod("DELETE");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setAllowUserInteraction(false);
            conn.setRequestProperty("Authorization", "Digest " + EIGC_PASSWORD);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            OutputStream out = conn.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(out, HTTP_CHARACTER_ENCODING);
            try {
                for (int i = 0; i < paramName.length; ++i) {
                    writer.write(paramName[i]);
                    writer.write("=");
                    logger.log(Level.INFO, "Sending " + paramName[i] + "=" + paramVal[i]);
                    writer.write(URLEncoder.encode(paramVal[i], HTTP_CHARACTER_ENCODING));
                    writer.write("&");
                }
            }
            finally {
                IoUtilities.closeClosable(writer);
                IoUtilities.closeClosable(out);
            }
            if (conn.getResponseCode() != HttpResponseStatus.OK.getStatusCode()) {
                throw new IOException(conn.getResponseMessage());
            }
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            try {
                String line;
                sb = new StringBuilder();
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                }
            }
            finally {
                IoUtilities.closeClosable(rd);
            }
        }
        finally {
            IoUtilities.closeHttpURLConnection(conn);
        }
        return sb.toString();
    }

    public static String httpGet(String urlStr, String userName) throws IOException {
        URL url = new URL(urlStr + userName);
        Eigc.setEigcHttpsOverride();
        HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
        conn.setRequestProperty("Authorization", EIGC_PASSWORD);
        if (conn.getResponseCode() != HttpResponseStatus.OK.getStatusCode()) {
            throw new IOException(conn.getResponseMessage());
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));){
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
        }
        conn.disconnect();
        return sb.toString();
    }

    public static final String addPlayer(String playerName) {
        EigcClient found = Eigc.getClientForPlayer(playerName);
        if (found != null) {
            logger.log(Level.INFO, playerName + " already in use: " + found.getClientId());
            return found.getClientId();
        }
        for (EigcClient gcc : EIGC_CLIENTS) {
            if (!gcc.getAccountName().equalsIgnoreCase(playerName)) continue;
            return Eigc.setClientUsed(gcc, playerName, "found unused reserved client");
        }
        EigcClient client = null;
        for (EigcClient gcc : EIGC_CLIENTS) {
            if (gcc.isUsed() || gcc.getAccountName().length() > 0) continue;
            client = gcc;
            break;
        }
        if (client != null) {
            EIGC_CLIENTS.remove(client);
            EIGC_CLIENTS.add(client);
            return Eigc.setClientUsed(client, playerName, "found unused free client");
        }
        Eigc.provisionAccounts(accountsToProvision, false);
        return "";
    }

    private static final String setClientUsed(EigcClient client, String playerName, String reason) {
        client.setPlayerName(playerName.toLowerCase(), reason);
        client.setAccountName(playerName.toLowerCase());
        return client.getClientId();
    }

    public static final EigcClient getClientForPlayer(String playerName) {
        boolean mustTrim;
        String nameSearched = LoginHandler.raiseFirstLetter(playerName);
        boolean bl = mustTrim = playerName.indexOf(" ") > 0;
        if (mustTrim) {
            nameSearched = playerName.substring(0, playerName.indexOf(" "));
            logger.log(Level.INFO, "Trimmed " + playerName + " to " + nameSearched);
        }
        for (EigcClient client : EIGC_CLIENTS) {
            if (!client.getPlayerName().equalsIgnoreCase(nameSearched)) continue;
            return client;
        }
        return null;
    }

    public static final EigcClient getReservedClientForPlayer(String playerName) {
        boolean mustTrim;
        String nameSearched = LoginHandler.raiseFirstLetter(playerName);
        boolean bl = mustTrim = playerName.indexOf(" ") > 0;
        if (mustTrim) {
            nameSearched = playerName.substring(0, playerName.indexOf(" "));
            logger.log(Level.INFO, "Trimmed " + playerName + " to " + nameSearched);
        }
        for (EigcClient client : EIGC_CLIENTS) {
            if (!client.getAccountName().equalsIgnoreCase(nameSearched)) continue;
            return client;
        }
        return null;
    }

    public static final EigcClient removePlayer(String playerName) {
        EigcClient client = Eigc.getClientForPlayer(playerName);
        if (client != null) {
            client.setPlayerName("", "removed");
            if (client.getExpiration() == Long.MAX_VALUE || client.getExpiration() < System.currentTimeMillis()) {
                client.setAccountName("");
            }
        }
        return client;
    }

    public static final void sendAllClientInfo(Communicator comm) {
        for (EigcClient entry : EIGC_CLIENTS) {
            comm.sendNormalServerMessage("ClientId: " + entry.getClientId() + ": user: " + entry.getPlayerName() + " occupied=" + entry.isUsed() + " accountname=" + entry.getAccountName() + " secs since last use=" + (entry.isUsed() ? entry.timeSinceLastUse() : "N/A"));
        }
    }

    public static final EigcClient transferPlayer(String playerName) {
        EigcClient client = Eigc.getReservedClientForPlayer(playerName);
        if (client != null) {
            if (client.getExpiration() < Long.MAX_VALUE && client.getExpiration() > System.currentTimeMillis()) {
                return client;
            }
            logger.log(Level.INFO, "Setting expired reserved client to unused at server transfer. This should be detected earlier.");
            client.setAccountName("");
            new Thread(){

                @Override
                public void run() {
                    Eigc.modifyUser(client.getClientId(), "proximity", Long.MAX_VALUE);
                }
            }.start();
        }
        return null;
    }

    public static final void updateAccount(String eigcUserId, String clientPass, String services, long expirationTime, String accountName) {
        EigcClient oldClient = Eigc.getClientWithId(eigcUserId);
        if (oldClient == null) {
            Eigc.createAccount(eigcUserId, clientPass, services, expirationTime, accountName.toLowerCase());
        } else {
            oldClient.setPassword(clientPass);
            oldClient.setServiceBundle(services);
            oldClient.setExpiration(expirationTime);
            oldClient.setAccountName(accountName.toLowerCase());
            oldClient.updateAccount();
            Players.getInstance().updateEigcInfo(oldClient);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void createAccount(String eigcUserId, String clientPass, String services, long expirationTime, String accountName) {
        EigcClient eigclient = new EigcClient(eigcUserId, clientPass, services, expirationTime, accountName.toLowerCase());
        EIGC_CLIENTS.add(eigclient);
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getLoginDbCon();
            ps = dbcon.prepareStatement(INSERT_EIGC_ACCOUNT);
            ps.setString(1, eigcUserId);
            ps.setString(2, clientPass);
            ps.setString(3, services);
            ps.setLong(4, expirationTime);
            ps.setString(5, accountName.toLowerCase());
            ps.executeUpdate();
            logger.log(Level.INFO, "Successfully saved " + eigcUserId);
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    public static final EigcClient getClientWithId(String eigcUserId) {
        for (EigcClient entry : EIGC_CLIENTS) {
            if (!entry.getClientId().equalsIgnoreCase(eigcUserId)) continue;
            return entry;
        }
        return null;
    }

    static void removeClientWithId(String eigcUserId) {
        EigcClient toRemove = null;
        for (EigcClient c : EIGC_CLIENTS) {
            if (!c.getClientId().equalsIgnoreCase(eigcUserId)) continue;
            toRemove = c;
            break;
        }
        if (toRemove != null) {
            EIGC_CLIENTS.remove(toRemove);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void deleteAccount(String eigcUserId) {
        Eigc.removeClientWithId(eigcUserId);
        Connection dbcon = null;
        PreparedStatement ps = null;
        try {
            dbcon = DbConnector.getLoginDbCon();
            ps = dbcon.prepareStatement(DELETE_EIGC_ACCOUNT);
            ps.setString(1, eigcUserId);
            ps.executeUpdate();
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, sqx.getMessage(), sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(ps, null);
            DbConnector.returnConnection(dbcon);
        }
        DbUtilities.closeDatabaseObjects(ps, null);
        DbConnector.returnConnection(dbcon);
    }

    static /* synthetic */ String access$000() {
        return HOST_PROVISIONING;
    }

    static /* synthetic */ Logger access$100() {
        return logger;
    }

    static /* synthetic */ boolean access$202(boolean x0) {
        isProvisioning = x0;
        return isProvisioning;
    }

    static /* synthetic */ LinkedList access$300() {
        return EIGC_CLIENTS;
    }

    static /* synthetic */ String access$400() {
        return CREATE_URL;
    }

    static /* synthetic */ String access$500(String x0, Element x1) {
        return Eigc.getTagValue(x0, x1);
    }
}

