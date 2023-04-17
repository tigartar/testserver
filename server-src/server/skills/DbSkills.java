/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.skills;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.skills.DbSkill;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.shared.constants.CounterTypes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeMap;
import java.util.logging.Logger;

public class DbSkills
extends Skills
implements MiscConstants,
CounterTypes {
    private static Logger logger = Logger.getLogger(DbSkills.class.getName());
    private static final String loadPlayerSkills2 = "select * FROM SKILLS where OWNER=?";
    private static final String loadCreatureSkills2 = "select * FROM SKILLS where OWNER=?";
    private static final String deleteCreatureSkills = "delete from SKILLS where OWNER=?";

    DbSkills(long aId) {
        PlayerInfo p;
        this.id = aId;
        if (aId != -10L && WurmId.getType(aId) == 0 && (p = PlayerInfoFactory.getPlayerInfoWithWurmId(aId)) != null) {
            if (!p.isPaying()) {
                this.paying = false;
            }
            if (!p.hasSkillGain) {
                this.hasSkillGain = false;
            }
            if (Servers.localServer.isChallengeOrEpicServer() && p.realdeath == 0) {
                this.priest = p.isPriest;
            }
        }
    }

    DbSkills(String aTemplateName) {
        this.templateName = aTemplateName;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void load() throws Exception {
        block8: {
            block7: {
                if (this.id == -10L) break block7;
                this.skills = new TreeMap();
                Connection dbcon = null;
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    if (WurmId.getType(this.id) == 0) {
                        dbcon = DbConnector.getPlayerDbCon();
                        ps = dbcon.prepareStatement("select * FROM SKILLS where OWNER=?");
                        ps.setLong(1, this.id);
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            DbSkill skill = new DbSkill(rs.getLong("ID"), this, rs.getInt("NUMBER"), rs.getDouble("VALUE"), rs.getDouble("MINVALUE"), rs.getLong("LASTUSED"));
                            if (this.skills.containsKey(skill.getNumber()) && !(skill.getMinimumValue() > ((Skill)this.skills.get(skill.getNumber())).getMinimumValue())) continue;
                            this.skills.put(skill.getNumber(), skill);
                        }
                    } else {
                        dbcon = DbConnector.getCreatureDbCon();
                        ps = dbcon.prepareStatement("select * FROM SKILLS where OWNER=?");
                        ps.setLong(1, this.id);
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            DbSkill skill = new DbSkill(rs.getLong("ID"), this, rs.getInt("NUMBER"), rs.getDouble("VALUE"), rs.getDouble("MINVALUE"), rs.getLong("LASTUSED"));
                            this.skills.put(skill.getNumber(), skill);
                        }
                    }
                    this.addTempSkills();
                }
                catch (Throwable throwable) {
                    DbUtilities.closeDatabaseObjects(ps, rs);
                    DbConnector.returnConnection(dbcon);
                    throw throwable;
                }
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                break block8;
            }
            if (this.templateName != null) {
                return;
            }
        }
    }

    @Override
    public void delete() throws SQLException {
        block6: {
            PreparedStatement ps;
            Connection dbcon;
            block7: {
                dbcon = null;
                ps = null;
                if (this.id == -10L) break block6;
                if (WurmId.getType(this.id) == 0) {
                    dbcon = DbConnector.getPlayerDbCon();
                    break block7;
                }
                if (WurmId.getType(this.id) == 1) {
                    dbcon = DbConnector.getCreatureDbCon();
                    break block7;
                }
                logger.warning("Unexpected Counter Type: " + WurmId.getType(this.id) + " for WurmID: " + this.id);
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
                return;
            }
            try {
                ps = dbcon.prepareStatement(deleteCreatureSkills);
                ps.setLong(1, this.id);
                ps.executeUpdate();
            }
            finally {
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
        }
    }
}

