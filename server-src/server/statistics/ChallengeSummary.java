/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.statistics;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Servers;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.statistics.ChallengePointEnum;
import com.wurmonline.server.statistics.ChallengeRound;
import com.wurmonline.server.statistics.ChallengeScore;
import com.wurmonline.server.statistics.ScoreNamePair;
import com.wurmonline.server.utils.DbUtilities;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChallengeSummary
implements MiscConstants {
    private static final String loadAllScores = "SELECT * FROM CHALLENGE";
    private static final String insertScore = "INSERT INTO CHALLENGE(LASTUPDATED,WURMID,ROUND,TYPE,POINTS,LASTPOINTS) VALUES (?,?,?,?,?,?)";
    private static final String updateScore = "UPDATE CHALLENGE SET LASTUPDATED=?,POINTS=?,LASTPOINTS=? WHERE WURMID=? AND ROUND=? AND TYPE=?";
    private static final Logger logger = Logger.getLogger(ChallengeSummary.class.getName());
    private static final Map<Long, ChallengeSummary> allScores = new ConcurrentHashMap<Long, ChallengeSummary>();
    private static boolean isDirty = false;
    private final long wid;
    private final String name;
    private static final ChallengeScore[] topScores = new ChallengeScore[ChallengePointEnum.ChallengePoint.getTypes().length];
    private static final String[] topScorers = new String[ChallengePointEnum.ChallengePoint.getTypes().length];
    private final ConcurrentHashMap<Integer, ChallengeRound> privateRounds = new ConcurrentHashMap();
    static final String start = "<TABLE id=\"gameDataTable\">\n\t\t<TR class=\"gameDataTopTenTR\">\n\t\t\t<TH>Name</TH>\n\t\t\t<TH>Points</TH>\n\t\t\t<TH>Last points</TH>\n\t\t\t<TH>Date</TH>\n\t\t</TR>\n\t\t";
    private static final String header = "<!DOCTYPE html><HTML>\n\t<HEAD>\n\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><TITLE>Wurm Online Challenge Standings</TITLE>\n\t<link rel=\"stylesheet\" type=\"text/css\" href=\"http://www.wurmonline.com/css/gameData.css\" />\n\t</HEAD>\n\n<BODY id=\"body\" class=\"gameDataBody\">\n\t";
    private static final String rootdir = "/var/www/challenge/";
    private static String headerFilename = "/var/www/challenge/main" + ChallengePointEnum.ChallengeScenario.current.getNum() + ".html";
    private static final String mainHeader = "<!DOCTYPE html><HTML>\n\t<HEAD>\n\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><TITLE>Wurm Online Challenge Standings</TITLE>\n\t<link rel=\"stylesheet\" type=\"text/css\" href=\"http://www.wurmonline.com/css/gameData.css\" />\n\t</HEAD>\n\n<BODY id=\"body\" class=\"gameDataBody\">\n\t";
    static final String headerStart = "<TABLE id=\"gameDataTable\">\n\t\t<TR>\n\t\t\t<TH>Challenge</TH>\n\t\t\t<TH>Leader</TH>\n\t\t\t<TH>Points</TH>\n\t\t\t<TH>Last Points</TH>\n\t\t\t<TH>Date</TH>\n\t\t</TR>\n\t\t";
    private static final String tablefooter = "</TABLE>\n\n";
    private static final String pagefooter = "</BODY>\n</HTML>";
    private boolean fileExists = false;
    private String filename = "";
    static boolean writing = false;
    static final String hscpStart = "<TABLE id=\"gameDataTable\">\n\t\t<TR>\n\t\t\t<TH>Rank</TH>\n\t\t\t<TH>Name</TH>\n\t\t\t<TH>Points</TH>\n\t\t\t<TH>Last Points</TH>\n\t\t\t<TH>Date</TH>\n\t\t</TR>\n\t\t";

    public ChallengeSummary(long wurmId, String playerName) {
        this.wid = wurmId;
        this.name = playerName;
    }

    public static final void addToScore(PlayerInfo pid, int scoreType, float added) {
        if (pid.getPower() > 0) {
            return;
        }
        if (added != 0.0f) {
            ChallengeScore scoreObj;
            ChallengeRound round;
            boolean newScore = false;
            ChallengeSummary summary = ChallengeSummary.getSummary(pid.wurmId);
            if (summary == null) {
                summary = new ChallengeSummary(pid.wurmId, pid.getName());
                ChallengeSummary.addChallengeSummary(summary);
            }
            if ((round = summary.getPrivateChallengeRound(ChallengePointEnum.ChallengeScenario.current.getNum())) == null) {
                round = new ChallengeRound(ChallengePointEnum.ChallengeScenario.current.getNum());
                summary.addPrivateChallengeRound(round);
            }
            if ((scoreObj = round.getCurrentScoreForType(scoreType)) == null) {
                scoreObj = new ChallengeScore(scoreType, added, System.currentTimeMillis(), added);
                newScore = true;
            } else {
                scoreObj.setPoints(scoreObj.getPoints() + added);
                scoreObj.setLastPoints(added);
            }
            round.setScore(scoreObj);
            ChallengePointEnum.ChallengePoint.fromInt(scoreType).setDirty(true);
            if (newScore) {
                ChallengeSummary.createScore(pid.wurmId, ChallengePointEnum.ChallengeScenario.current.getNum(), scoreObj);
            } else {
                ChallengeSummary.updateScore(pid.wurmId, ChallengePointEnum.ChallengeScenario.current.getNum(), scoreObj);
            }
            if (ChallengeSummary.checkIfTopScore(scoreObj, pid)) {
                try {
                    Player player = Players.getInstance().getPlayer(pid.wurmId);
                    player.getCommunicator().sendSafeServerMessage("New High Score: " + ChallengePointEnum.ChallengePoint.fromInt(scoreType).getName() + " " + scoreObj.getPoints() + "!");
                }
                catch (NoSuchPlayerException noSuchPlayerException) {
                    // empty catch block
                }
            }
            if (scoreType == ChallengePointEnum.ChallengePoint.OVERALL.getEnumtype()) {
                summary.saveCurrentPersonalHtmlPage();
            }
        }
    }

    private static final boolean checkIfTopScore(ChallengeScore score, PlayerInfo pinf) {
        if (score.getType() != 0) {
            if (topScores[score.getType()] == null && score.getPoints() > 0.0f) {
                ChallengeSummary.topScores[score.getType()] = score;
                ChallengeSummary.topScorers[score.getType()] = pinf.getName();
                return true;
            }
            if (score.getPoints() > 0.0f && score.getPoints() > topScores[score.getType()].getPoints()) {
                ChallengeSummary.topScores[score.getType()] = score;
                ChallengeSummary.topScorers[score.getType()] = pinf.getName();
                return true;
            }
        }
        return false;
    }

    public static final void addScoreFromLoad(PlayerInfo pid, int roundNumber, ChallengeScore score) {
        ChallengeRound round;
        ChallengeSummary summary = ChallengeSummary.getSummary(pid.wurmId);
        if (summary == null) {
            summary = new ChallengeSummary(pid.wurmId, pid.getName());
            ChallengeSummary.addChallengeSummary(summary);
        }
        if ((round = summary.getPrivateChallengeRound(roundNumber)) == null) {
            round = new ChallengeRound(roundNumber);
            summary.addPrivateChallengeRound(round);
        }
        round.setScore(score);
        ChallengeSummary.checkIfTopScore(score, pid);
    }

    public static final void addChallengeSummary(ChallengeSummary summary) {
        allScores.put(summary.getPlayerId(), summary);
    }

    public static final ChallengeSummary getSummary(long playerId) {
        return allScores.get(playerId);
    }

    public static final ChallengeRound getRoundSummary(long playerId, int round) {
        ChallengeSummary summary = allScores.get(playerId);
        if (summary != null) {
            return summary.getPrivateChallengeRound(round);
        }
        return null;
    }

    public final ChallengeRound getPrivateChallengeRound(int round) {
        return this.privateRounds.get(round);
    }

    public final void addPrivateChallengeRound(ChallengeRound round) {
        this.privateRounds.put(round.getRound(), round);
    }

    public final long getPlayerId() {
        return this.wid;
    }

    public final String getPlayerName() {
        return this.name;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void loadLocalChallengeScores() {
        if (Servers.localServer.isChallengeServer()) {
            Connection dbcon = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            int loadedScores = 0;
            long lStart = System.nanoTime();
            try {
                dbcon = DbConnector.getLoginDbCon();
                ps = dbcon.prepareStatement(loadAllScores);
                rs = ps.executeQuery();
                while (rs.next()) {
                    long wurmid = rs.getLong("WURMID");
                    PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
                    int round = rs.getInt("ROUND");
                    float points = rs.getFloat("POINTS");
                    int scoreType = rs.getInt("TYPE");
                    long lastUpdated = rs.getLong("LASTUPDATED");
                    long lastAdded = rs.getLong("LASTPOINTS");
                    if (pinf != null) {
                        ChallengeSummary.addScoreFromLoad(pinf, round, new ChallengeScore(scoreType, points, lastUpdated, lastAdded));
                    }
                    ++loadedScores;
                }
            }
            catch (SQLException sqx) {
                block7: {
                    try {
                        logger.log(Level.WARNING, "Failed to load scores, SqlState: " + sqx.getSQLState() + ", ErrorCode: " + sqx.getErrorCode(), sqx);
                        SQLException lNext = sqx.getNextException();
                        if (lNext == null) break block7;
                        logger.log(Level.WARNING, "Failed to load scores, Next Exception", lNext);
                    }
                    catch (Throwable throwable) {
                        DbUtilities.closeDatabaseObjects(ps, rs);
                        DbConnector.returnConnection(dbcon);
                        long end = System.nanoTime();
                        logger.info("Loaded " + loadedScores + " challenge scores from database took " + (float)(end - lStart) / 1000000.0f + " ms");
                        throw throwable;
                    }
                }
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                long end = System.nanoTime();
                logger.info("Loaded " + loadedScores + " challenge scores from database took " + (float)(end - lStart) / 1000000.0f + " ms");
            }
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
            long end = System.nanoTime();
            logger.info("Loaded " + loadedScores + " challenge scores from database took " + (float)(end - lStart) / 1000000.0f + " ms");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void createScore(long pid, int round, ChallengeScore score) {
        block6: {
            try {
                if (!Servers.localServer.isChallengeServer()) break block6;
                Connection dbcon = null;
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    dbcon = DbConnector.getLoginDbCon();
                    ps = dbcon.prepareStatement(insertScore);
                    ps.setLong(1, score.getLastUpdated());
                    ps.setLong(2, pid);
                    ps.setInt(3, round);
                    ps.setInt(4, score.getType());
                    ps.setFloat(5, score.getPoints());
                    ps.setFloat(6, score.getLastPoints());
                    ps.execute();
                }
                catch (SQLException sqx) {
                    block7: {
                        try {
                            logger.log(Level.WARNING, "Failed to save score " + pid + "," + round + "," + score.getPoints() + ", SqlState: " + sqx.getSQLState() + ", ErrorCode: " + sqx.getErrorCode(), sqx);
                            SQLException lNext = sqx.getNextException();
                            if (lNext == null) break block7;
                            logger.log(Level.WARNING, "Failed to save scores, Next Exception", lNext);
                        }
                        catch (Throwable throwable) {
                            DbUtilities.closeDatabaseObjects(ps, rs);
                            DbConnector.returnConnection(dbcon);
                            throw throwable;
                        }
                    }
                    DbUtilities.closeDatabaseObjects(ps, rs);
                    DbConnector.returnConnection(dbcon);
                    break block6;
                }
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Exception saving challenge score " + ex.getMessage(), ex);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void updateScore(long pid, int round, ChallengeScore score) {
        block6: {
            try {
                if (!Servers.localServer.isChallengeServer()) break block6;
                Connection dbcon = null;
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    dbcon = DbConnector.getLoginDbCon();
                    ps = dbcon.prepareStatement(updateScore);
                    ps.setLong(1, score.getLastUpdated());
                    ps.setFloat(2, score.getPoints());
                    ps.setFloat(3, score.getLastPoints());
                    ps.setLong(4, pid);
                    ps.setInt(5, round);
                    ps.setInt(6, score.getType());
                    ps.executeUpdate();
                }
                catch (SQLException sqx) {
                    block7: {
                        try {
                            logger.log(Level.WARNING, "Failed to save score " + pid + "," + round + "," + score.getPoints() + ", SqlState: " + sqx.getSQLState() + ", ErrorCode: " + sqx.getErrorCode(), sqx);
                            SQLException lNext = sqx.getNextException();
                            if (lNext == null) break block7;
                            logger.log(Level.WARNING, "Failed to load scores, Next Exception", lNext);
                        }
                        catch (Throwable throwable) {
                            DbUtilities.closeDatabaseObjects(ps, rs);
                            DbConnector.returnConnection(dbcon);
                            throw throwable;
                        }
                    }
                    DbUtilities.closeDatabaseObjects(ps, rs);
                    DbConnector.returnConnection(dbcon);
                    break block6;
                }
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
            }
            catch (Exception ex) {
                logger.log(Level.WARNING, "Exception " + ex.getMessage(), ex);
            }
        }
    }

    private final File createFile() {
        if (!this.fileExists) {
            String dir = rootdir + this.name.substring(0, 1) + File.separator;
            File dirFile = new File(dir.toLowerCase());
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            this.fileExists = true;
            this.filename = dir.toLowerCase() + this.name.toLowerCase() + ".html";
        }
        return new File(this.filename);
    }

    public final void saveCurrentPersonalHtmlPage() {
        isDirty = true;
        new Thread(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void run() {
                Writer output = null;
                try {
                    File aFile = ChallengeSummary.this.createFile();
                    output = new BufferedWriter(new FileWriter(aFile));
                    output.write("<!DOCTYPE html><HTML>\n\t<HEAD>\n\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><TITLE>Wurm Online Challenge Standings</TITLE>\n\t<link rel=\"stylesheet\" type=\"text/css\" href=\"http://www.wurmonline.com/css/gameData.css\" />\n\t</HEAD>\n\n<BODY id=\"body\" class=\"gameDataBody\">\n\t");
                    output.write("<H1>Summary for " + ChallengeSummary.this.name + "</H1>\n\t<br>");
                    for (ChallengePointEnum.ChallengeScenario scenario : ChallengePointEnum.ChallengeScenario.getScenarios()) {
                        ChallengeRound summary;
                        if (scenario.getNum() <= 0 || (summary = ChallengeSummary.this.getPrivateChallengeRound(scenario.getNum())) == null) continue;
                        output.write("<img src=\"" + summary.getRoundIcon() + "\" alt=\"round icon\"/><p><a href=\"../main" + summary.getRound() + ".html\">" + summary.getRoundName() + "</a></p>\n\t");
                        try {
                            output.write(ChallengeSummary.start);
                        }
                        catch (IOException iox) {
                            logger.log(Level.WARNING, iox.getMessage(), iox);
                        }
                        for (ChallengeScore score : summary.getScores()) {
                            if (score.getType() == 0) continue;
                            output.write("<TR class=\"gameDataTopTenTR\">\n\t\t\t<TD class=\"gameDataTopTenTDName\">" + ChallengePointEnum.ChallengePoint.fromInt(score.getType()).getName() + "</TD>\n\t\t\t<TD class=\"gameDataTopTenTDValue\">" + score.getPoints() + "</TD>\n\t\t\t<TD>" + score.getLastPoints() + "</TD>\n\t\t\t<TD>" + new Date(score.getLastUpdated()) + "</TD>\n\t\t</TR>\n\t\t");
                        }
                        output.write(ChallengeSummary.tablefooter);
                    }
                    output.write(ChallengeSummary.pagefooter);
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, "Failed to close html file for " + ChallengeSummary.this.name, iox);
                }
                finally {
                    try {
                        if (output != null) {
                            output.close();
                        }
                    }
                    catch (IOException iOException) {}
                }
            }
        }.start();
    }

    private static final File createHeaderFile() {
        return new File(headerFilename);
    }

    private static final String getHighScoreUrl(int pointType) {
        return ChallengePointEnum.ChallengePoint.fromInt(pointType).getName().replace(" ", "").trim().toLowerCase() + ChallengePointEnum.ChallengeScenario.current.getNum() + ".html";
    }

    private static final String getPlayerHomePageUrl(String playerName) {
        return playerName.substring(0, 1).toLowerCase() + "/" + playerName.toLowerCase() + ".html";
    }

    public static final void saveCurrentGlobalHtmlPage() {
        if (isDirty) {
            isDirty = false;
            if (!writing) {
                writing = true;
                new Thread(){

                    /*
                     * WARNING - Removed try catching itself - possible behaviour change.
                     */
                    @Override
                    public void run() {
                        Writer output = null;
                        try {
                            File aFile = ChallengeSummary.createHeaderFile();
                            output = new BufferedWriter(new FileWriter(aFile));
                            output.write("<!DOCTYPE html><HTML>\n\t<HEAD>\n\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><TITLE>Wurm Online Challenge Standings</TITLE>\n\t<link rel=\"stylesheet\" type=\"text/css\" href=\"http://www.wurmonline.com/css/gameData.css\" />\n\t</HEAD>\n\n<BODY id=\"body\" class=\"gameDataBody\">\n\t");
                            output.write("<H1>Summary for " + ChallengePointEnum.ChallengeScenario.current.getName() + "</H1>\n\t<br>");
                            output.write("<img src=\"" + ChallengePointEnum.ChallengeScenario.current.getUrl() + "\" alt=\"round icon\"/><p>" + ChallengePointEnum.ChallengeScenario.current.getDesc() + "</p>\n\t");
                            try {
                                output.write(ChallengeSummary.headerStart);
                            }
                            catch (IOException iox) {
                                logger.log(Level.WARNING, iox.getMessage(), iox);
                            }
                            for (int x = 0; x < topScores.length; ++x) {
                                if (topScores[x] == null) continue;
                                String scorerUrl = ChallengeSummary.getPlayerHomePageUrl(topScorers[x]);
                                output.write("<TR class=\"gameDataTopTenTR\">\n\t\t\t<TD class=\"gameDataTopTenTDName\"><a href=\"" + ChallengeSummary.getHighScoreUrl(topScores[x].getType()) + "\">" + ChallengePointEnum.ChallengePoint.fromInt(topScores[x].getType()).getName() + "</a></TD>\n\t\t\t<TD class=\"gameDataTopTenTDName\"><a href=\"" + scorerUrl.toLowerCase() + "\">" + topScorers[x] + "</a></TD>\n\t\t\t<TD class=\"gameDataTopTenTDValue\">" + topScores[x].getPoints() + "</TD>\n\t\t\n\t\t<TR>\n\t\t\t<TH>" + topScores[x].getLastPoints() + "</TH>\n\t\t\t<TH>" + new Date(topScores[x].getLastUpdated()) + "</TH>\n\t\t</TR>\n\t\t");
                            }
                            output.write(ChallengeSummary.tablefooter);
                            output.write(ChallengeSummary.pagefooter);
                            for (ChallengePointEnum.ChallengePoint point : ChallengePointEnum.ChallengePoint.getTypes()) {
                                if (point.getEnumtype() <= 0 || !point.isDirty()) continue;
                                ChallengeSummary.createHighScorePage(point.getEnumtype());
                            }
                        }
                        catch (IOException iox) {
                            logger.log(Level.WARNING, "Failed to close html file for main page", iox);
                        }
                        finally {
                            try {
                                if (output != null) {
                                    output.close();
                                }
                            }
                            catch (IOException iOException) {}
                        }
                        writing = false;
                    }
                }.start();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static final void createHighScorePage(int scoreType) {
        String fileName = rootdir + ChallengeSummary.getHighScoreUrl(scoreType);
        File aFile = new File(fileName);
        Writer output = null;
        try {
            ChallengePointEnum.ChallengePoint point = ChallengePointEnum.ChallengePoint.fromInt(scoreType);
            output = new BufferedWriter(new FileWriter(aFile));
            output.write("<!DOCTYPE html><HTML>\n\t<HEAD>\n\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><TITLE>Wurm Online Challenge Standings</TITLE>\n\t<link rel=\"stylesheet\" type=\"text/css\" href=\"http://www.wurmonline.com/css/gameData.css\" />\n\t</HEAD>\n\n<BODY id=\"body\" class=\"gameDataBody\">\n\t");
            output.write("<H1>Summary for " + point.getName() + "</H1>\n\t<br>");
            output.write("<img src=\"" + ChallengePointEnum.ChallengeScenario.current.getUrl() + "\" alt=\"round icon\"/><p><a href=\"main" + ChallengePointEnum.ChallengeScenario.current.getNum() + ".html\">" + ChallengePointEnum.ChallengeScenario.current.getName() + "</a></p>\n\t");
            try {
                output.write(hscpStart);
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, iox.getMessage(), iox);
            }
            ConcurrentSkipListSet<ScoreNamePair> scores = new ConcurrentSkipListSet<ScoreNamePair>();
            block13: for (ChallengeSummary summary : allScores.values()) {
                ChallengeScore[] scoreArr;
                ChallengeRound round = summary.getPrivateChallengeRound(ChallengePointEnum.ChallengeScenario.current.getNum());
                if (round == null) continue;
                for (ChallengeScore score : scoreArr = round.getScores()) {
                    if (score.getType() != scoreType || !(score.getPoints() > 0.0f)) continue;
                    scores.add(new ScoreNamePair(summary.getPlayerName(), score));
                    continue block13;
                }
            }
            Object[] topScoreArr = scores.toArray(new ScoreNamePair[scores.size()]);
            Arrays.sort(topScoreArr);
            for (int x = 0; x < topScoreArr.length; ++x) {
                if (topScoreArr[x] == null) continue;
                String scorerUrl = ChallengeSummary.getPlayerHomePageUrl(((ScoreNamePair)topScoreArr[x]).name);
                output.write("<TR class=\"gameDataTopTenTR\">\n\t\t\t<TD class=\"gameDataTopTenTDValue\">" + (x + 1) + "</TD>\n\t\t\t<TD class=\"gameDataTopTenTDName\"><a href=\"" + scorerUrl + "\">" + ((ScoreNamePair)topScoreArr[x]).name + "</a></TD>\n\t\t\t<TD class=\"gameDataTopTenTDValue\">" + ((ScoreNamePair)topScoreArr[x]).score.getPoints() + "</TD>\n\t\t\n\t\t<TR>\n\t\t\t<TH>" + ((ScoreNamePair)topScoreArr[x]).score.getLastPoints() + "</TH>\n\t\t\t<TH>" + new Date(((ScoreNamePair)topScoreArr[x]).score.getLastUpdated()) + "</TH>\n\t\t</TR>\n\t\t");
            }
            output.write(tablefooter);
            output.write(pagefooter);
        }
        catch (IOException iox) {
            logger.log(Level.WARNING, "Failed to close html file for main page", iox);
        }
        finally {
            try {
                if (output != null) {
                    output.close();
                }
            }
            catch (IOException iOException) {}
        }
        ChallengePointEnum.ChallengePoint.fromInt(scoreType).setDirty(false);
    }

    static /* synthetic */ File access$000(ChallengeSummary x0) {
        return x0.createFile();
    }

    static /* synthetic */ String access$100(ChallengeSummary x0) {
        return x0.name;
    }

    static /* synthetic */ Logger access$200() {
        return logger;
    }

    static /* synthetic */ File access$300() {
        return ChallengeSummary.createHeaderFile();
    }

    static /* synthetic */ ChallengeScore[] access$400() {
        return topScores;
    }

    static /* synthetic */ String[] access$500() {
        return topScorers;
    }

    static /* synthetic */ String access$600(String x0) {
        return ChallengeSummary.getPlayerHomePageUrl(x0);
    }

    static /* synthetic */ String access$700(int x0) {
        return ChallengeSummary.getHighScoreUrl(x0);
    }
}

