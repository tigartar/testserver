/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureMove;
import com.wurmonline.server.epic.HexMap;
import com.wurmonline.server.epic.Valrei;
import com.wurmonline.server.webinterface.WebInterface;
import com.wurmonline.server.zones.FaithZone;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WebInterfaceTest
implements MiscConstants {
    private WebInterface wurm = null;
    private static Logger logger = Logger.getLogger(WebInterfaceTest.class.getName());
    private static FaithZone[][] surfaceDomains = new FaithZone[40][40];
    private final String intraServerPassword = "";
    private static final LinkedList<CreatureMove> list = new LinkedList();
    private static final LinkedList<CreatureMove> list2 = new LinkedList();

    private void connect(String ip) throws MalformedURLException, RemoteException, NotBoundException {
        this.connect(ip, "7220");
    }

    private void connect(String ip, String port) throws MalformedURLException, RemoteException, NotBoundException {
        if (this.wurm == null) {
            String name = "//" + ip + ":" + port + "/WebInterface";
            this.wurm = (WebInterface)Naming.lookup(name);
        }
    }

    public void shutDown(String host, String port, String user, String pass) {
        this.wurm = null;
        try {
            this.connect(host, port);
            if (this.wurm != null) {
                this.wurm.shutDown("", user, pass, "Console initiated shutdown.", 30);
                System.out.println("Two. Host " + host + " shutting down.");
            }
        }
        catch (Exception ex) {
            logger.log(Level.INFO, "failed to shut down localhost");
        }
    }

    public void globalShutdown(String reason, int time, String user, String pass) {
        if (Servers.localServer != Servers.loginServer) {
            System.out.println("You must initiate a global shutdown from " + Servers.loginServer.getName() + ".");
            return;
        }
        for (ServerEntry server : Servers.getAllServers()) {
            this.wurm = null;
            System.out.println("Sending shutdown command to " + server.getName() + " @ " + server.INTRASERVERADDRESS);
            try {
                this.connect(server.INTRASERVERADDRESS);
                if (this.wurm != null) {
                    this.wurm.shutDown(server.INTRASERVERPASSWORD, user, pass, reason, time);
                    continue;
                }
                System.out.println("Failed to shutdown " + server.getName());
            }
            catch (Exception e) {
                System.out.println("Failed to shutdown " + server.getName());
                e.printStackTrace();
            }
        }
    }

    public void shutdownAll(String reason, int time) throws MalformedURLException, RemoteException, NotBoundException {
    }

    private CreatureMove getMove(int ts) {
        for (CreatureMove c : list) {
            if (c.timestamp != (long)ts) continue;
            return c;
        }
        for (CreatureMove c : list2) {
            if (c.timestamp != (long)ts) continue;
            return c;
        }
        return null;
    }

    public static final double getModifiedEffect(double eff) {
        return (10000.0 - (100.0 - eff) * (100.0 - eff)) / 100.0;
    }

    public static void runEpic() {
        new Thread(){
            private final HexMap val = new Valrei();
            private boolean loaded = false;

            @Override
            public void run() {
                if (!this.loaded) {
                    this.val.loadAllEntities();
                    this.loaded = true;
                }
                while (true) {
                    try {
                        while (true) {
                            1.sleep(20L);
                            this.val.pollAllEntities(true);
                        }
                    }
                    catch (Exception ex) {
                        logger.log(Level.INFO, ex.getMessage(), ex);
                        continue;
                    }
                    break;
                }
            }
        }.start();
    }

    public static String encryptMD5(String plaintext) throws Exception {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e) {
            throw new WurmServerException("No such algorithm 'MD5'", e);
        }
        try {
            md.update(plaintext.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            throw new WurmServerException("No such encoding: UTF-8", e);
        }
        byte[] raw = md.digest();
        BigInteger bi = new BigInteger(1, raw);
        String hash = bi.toString(16);
        return hash;
    }

    public static void main(String[] args) {
    }

    public static String bytesToStringUTFCustom(byte[] bytes) {
        char[] buffer = new char[bytes.length >> 1];
        for (int i = 0; i < buffer.length; ++i) {
            char c;
            int bpos = i << 1;
            buffer[i] = c = (char)(((bytes[bpos] & 0xFF) << 8) + (bytes[bpos + 1] & 0xFF));
        }
        return new String(buffer);
    }

    public static String bytesToStringUTFNIO(byte[] bytes) {
        CharBuffer cBuffer = ByteBuffer.wrap(bytes).asCharBuffer();
        return cBuffer.toString();
    }

    public static final void printZone(int tilex, int tiley) {
        System.out.println(surfaceDomains[tilex >> 3][tiley >> 3].getStartX() + ", " + surfaceDomains[tilex >> 3][tiley >> 3].getStartY() + ":" + surfaceDomains[tilex >> 3][tiley >> 3].getCenterX() + ", " + surfaceDomains[tilex >> 3][tiley >> 3].getCenterY());
    }

    public static int getDir(int ctx, int cty, int targetX, int targetY) {
        double newrot = Math.atan2((targetY << 2) + 2 - ((cty << 2) + 2), (targetX << 2) + 2 - ((ctx << 2) + 2));
        float attAngle = (float)(newrot * 57.29577951308232) + 90.0f;
        attAngle = Creature.normalizeAngle(attAngle);
        float degree = 22.5f;
        if ((double)attAngle >= 337.5 || attAngle < 22.5f) {
            return 0;
        }
        for (int x = 0; x < 8; ++x) {
            if (!(attAngle < 22.5f + (float)(45 * x))) continue;
            return x;
        }
        return 0;
    }

    static /* synthetic */ Logger access$000() {
        return logger;
    }
}

