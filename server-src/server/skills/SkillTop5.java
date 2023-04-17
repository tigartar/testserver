/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.skills;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SkillTop5 {
    private static final String GET_TOP_5 = "SELECT S.NUMBER, S.OWNER, P.NAME, S.VALUE FROM WURMPLAYERS.SKILLS S JOIN WURMPLAYERS.PLAYERS P ON S.OWNER=P.WURMID WHERE S.NUMBER=? AND P.POWER=0 ORDER BY S.VALUE DESC LIMIT 5;";
    private static final int[] skills = new int[]{1, 104, 103, 102, 2, 100, 101, 3, 106, 105, 1021, 10042, 10069, 10095, 1030, 10081, 10080, 10079, 1003, 10003, 10025, 10024, 10001, 1005, 1031, 10044, 1032, 10082, 10051, 10073, 1025, 10064, 10036, 1018, 10039, 10083, 10059, 10037, 10038, 1009, 1023, 10053, 10054, 10055, 10058, 10057, 10052, 1010, 1027, 10070, 1024, 10056, 1001, 10029, 10007, 1013, 10074, 1004, 10061, 10062, 10063, 10040, 1008, 1020, 10026, 10009, 10004, 10035, 10008, 10047, 10002, 10046, 10030, 1019, 10078, 10085, 10072, 10049, 10033, 10071, 10048, 10045, 10086, 10060, 10091, 10031, 1033, 10089, 10088, 10090, 1011, 10032, 1026, 10067, 10068, 10066, 10065, 1014, 1002, 10023, 10021, 10006, 10020, 10022, 10019, 1015, 1017, 10012, 10013, 10014, 10015, 10043, 10034, 10041, 1016, 10010, 10011, 1000, 10005, 10027, 10028, 1012, 10016, 10017, 10092, 1028, 10076, 10075, 10084, 1022, 10087, 10050, 10018, 1029, 10093, 10077, 10094, 1007};
    private final int number;
    private final int pos;
    private final long owner;
    private final String ownerName;
    private final double value;

    public SkillTop5(int aNumber, int aPos, long aOwner, String aOwnerName, double aValue) {
        this.number = aNumber;
        this.pos = aPos;
        this.owner = aOwner;
        this.ownerName = aOwnerName;
        this.value = aValue;
    }

    public int getNumber() {
        return this.number;
    }

    public int getPos() {
        return this.pos;
    }

    public long getOwner() {
        return this.owner;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public double getValue() {
        return this.value;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static SkillTop5[][] getAllSkillsTop5() throws Exception {
        SkillTop5[][] skillsTop5 = new SkillTop5[skills.length][5];
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement(GET_TOP_5);
            for (int i = 0; i < skills.length; ++i) {
                ps.setInt(1, skills[i]);
                rs = ps.executeQuery();
                for (int count = 0; rs.next() && count < 5; ++count) {
                    SkillTop5 skill;
                    skillsTop5[i][count] = skill = new SkillTop5(rs.getInt("NUMBER"), count, rs.getLong("OWNER"), rs.getString("NAME"), rs.getDouble("VALUE"));
                }
                DbUtilities.closeDatabaseObjects(null, rs);
            }
        }
        catch (Throwable throwable) {
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            throw throwable;
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        return skillsTop5;
    }
}

