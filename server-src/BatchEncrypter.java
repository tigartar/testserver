/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  sun.misc.BASE64Encoder
 */
package com.wurmonline;

import com.wurmonline.server.DbConnector;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import sun.misc.BASE64Encoder;

public final class BatchEncrypter {
    private static final String getPlayers = "select * from PLAYERS";
    private static final String updatePw = "update PLAYERS set PASSWORD=? where NAME=?";
    private static Logger logger = Logger.getLogger(BatchEncrypter.class.getName());
    protected static final String destroyString = "ALTER TABLE PLAYERS DROP COLUMN PASSWORD";
    protected static final String createString = "ALTER TABLE PLAYERS ADD PASSWORD VARCHAR(30)";

    private BatchEncrypter() {
    }

    public static String encrypt(String plaintext) throws Exception {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA");
        }
        catch (NoSuchAlgorithmException e) {
            throw new WurmServerException("No such algorithm 'SHA'");
        }
        try {
            md.update(plaintext.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            throw new WurmServerException("No such encoding: UTF-8");
        }
        byte[] raw = md.digest();
        String hash = new BASE64Encoder().encode(raw);
        return hash;
    }

    public static void encryptPasswords() {
        try {
            Connection dbcon = DbConnector.getPlayerDbCon();
            PreparedStatement ps = dbcon.prepareStatement(getPlayers);
            ResultSet rs = ps.executeQuery();
            PreparedStatement destroy = dbcon.prepareStatement(destroyString);
            destroy.execute();
            destroy.close();
            PreparedStatement create = dbcon.prepareStatement(createString);
            create.execute();
            create.close();
            while (rs.next()) {
                String password = rs.getString("PASSWORD");
                String name = rs.getString("NAME");
                String newPw = "";
                try {
                    newPw = BatchEncrypter.encrypt(name + password);
                }
                catch (Exception ex) {
                    logger.log(Level.WARNING, ex.getMessage(), ex);
                }
                PreparedStatement ps2 = dbcon.prepareStatement(updatePw);
                ps2.setString(1, newPw);
                ps2.setString(2, name);
                ps2.executeUpdate();
                ps2.close();
            }
            ps.close();
            DbConnector.closeAll();
        }
        catch (Exception ex) {
            logger.log(Level.INFO, ex.getMessage(), ex);
        }
    }
}

