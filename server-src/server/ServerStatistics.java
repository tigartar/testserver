/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

import com.wurmonline.server.Constants;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Servers;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

final class ServerStatistics {
    private static final Logger logger = Logger.getLogger(ServerStatistics.class.getName());
    private static final String INSERT_SERVER_STATISTIC_INT = "insert into SERVER_STATS_LOG ( SERVER_ID, SERVER_NAME, STATISTIC_ID, VALUE_INT ) VALUES(?,?,?,?)";
    private static final int SERVER_ID = Servers.getLocalServerId();
    private static final String SERVER_NAME = Servers.getLocalServerName();
    private static final String STATISTIC_TYPE_NUMBER_OF_PREMIUM_PLAYERS = "numberOfPremiumPlayers";
    private static final String STATISTIC_TYPE_NUMBER_OF_PLAYERS = "numberOfPlayers";
    private static final String STATISTIC_TYPE_NUMBER_OF_CLIENT_IPS = "numberOfClientIps";
    private static final String STATISTIC_TYPE_NUMBER_OF_PLAYERS_THROUGH_TUTORIAL = "playersThroughTutorial";
    private static final String STATISTIC_TYPE_NUMBER_OF_PREMIUM_PLAYER_LOGONS = "logonsPrem";
    private static final String STATISTIC_TYPE_NUMBER_OF_PLAYER_LOGONS = "logons";
    private static final String STATISTIC_TYPE_NUMBER_OF_SECONDS_LAG = "secondsLag";
    private static final String STATISTIC_TYPE_NUMBER_OF_NEW_PLAYERS = "newbies";
    private static final String STATISTIC_TYPE_NUMBER_OF_PREMIUM_PLAYER_REGISTERED = "premiumPlayersRegistered";

    private ServerStatistics() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void writeIntegerStatisticsToMrtgFile(String aStatisticType, String aFileName, int aFirstIntegerStatistic, int aSecondIntegerStatistic) {
        File file = null;
        FileOutputStream fos = null;
        try {
            file = new File(aFileName);
            fos = new FileOutputStream(file);
            fos.write((aFirstIntegerStatistic + "\n").getBytes());
            fos.write((aSecondIntegerStatistic + "\n\nwww.wurmonline.com").getBytes());
            fos.flush();
        }
        catch (IOException e) {
            try {
                logger.log(Level.WARNING, "Problem writing " + aStatisticType + " to file - " + e.getMessage(), e);
            }
            catch (Throwable throwable) {
                StreamUtilities.closeOutputStreamIgnoreExceptions(fos);
                throw throwable;
            }
            StreamUtilities.closeOutputStreamIgnoreExceptions(fos);
        }
        StreamUtilities.closeOutputStreamIgnoreExceptions(fos);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void storeOneIntegerStatisticInDatabase(String aStatisticType, int aIntegerStatisticValue) {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Going to log " + aStatisticType + " to database");
        }
        Connection logsConnection = null;
        PreparedStatement logsStatement = null;
        try {
            logsConnection = DbConnector.getLogsDbCon();
            logsStatement = logsConnection.prepareStatement(INSERT_SERVER_STATISTIC_INT);
            logsStatement.setInt(1, SERVER_ID);
            logsStatement.setString(2, SERVER_NAME);
            logsStatement.setString(3, aStatisticType);
            logsStatement.setInt(4, aIntegerStatisticValue);
            logsStatement.execute();
        }
        catch (SQLException sqex) {
            try {
                logger.log(Level.WARNING, "Failed to store " + aStatisticType + " - " + sqex.getMessage(), sqex);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(logsStatement, null);
                DbConnector.returnConnection(logsConnection);
                throw throwable;
            }
            DbUtilities.closeDatabaseObjects(logsStatement, null);
            DbConnector.returnConnection(logsConnection);
        }
        DbUtilities.closeDatabaseObjects(logsStatement, null);
        DbConnector.returnConnection(logsConnection);
    }

    static void storeNumberOfPlayers(int numberOfPremiumPlayers, int numberOfPlayers) {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("storeNumberOfPlayers - numberOfPremiumPlayers: " + numberOfPremiumPlayers + ", numberOfPlayers: " + numberOfPlayers);
        }
        ServerStatistics.writeIntegerStatisticsToMrtgFile(STATISTIC_TYPE_NUMBER_OF_PLAYERS, Constants.playerStatLog, numberOfPremiumPlayers, numberOfPlayers);
        if (Constants.useDatabaseForServerStatisticsLog) {
            ServerStatistics.storeOneIntegerStatisticInDatabase(STATISTIC_TYPE_NUMBER_OF_PREMIUM_PLAYERS, numberOfPremiumPlayers);
            ServerStatistics.storeOneIntegerStatisticInDatabase(STATISTIC_TYPE_NUMBER_OF_PLAYERS, numberOfPlayers);
        }
    }

    static void storeNumberOfClientIps(int numberOfClientIps) {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("storeNumberOfClientIps - numberOfClientIps: " + numberOfClientIps);
        }
        ServerStatistics.writeIntegerStatisticsToMrtgFile(STATISTIC_TYPE_NUMBER_OF_CLIENT_IPS, Constants.ipStatLog, numberOfClientIps, numberOfClientIps);
        if (Constants.useDatabaseForServerStatisticsLog) {
            ServerStatistics.storeOneIntegerStatisticInDatabase(STATISTIC_TYPE_NUMBER_OF_CLIENT_IPS, numberOfClientIps);
        }
    }

    static void storeNumberOfPlayersThroughTutorial(int playersThroughTutorial) {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("storeNumberOfPlayersThroughTutorial - playersThroughTutorial: " + playersThroughTutorial);
        }
        ServerStatistics.writeIntegerStatisticsToMrtgFile(STATISTIC_TYPE_NUMBER_OF_PLAYERS_THROUGH_TUTORIAL, Constants.tutorialLog, playersThroughTutorial, playersThroughTutorial);
        if (Constants.useDatabaseForServerStatisticsLog) {
            ServerStatistics.storeOneIntegerStatisticInDatabase(STATISTIC_TYPE_NUMBER_OF_PLAYERS_THROUGH_TUTORIAL, playersThroughTutorial);
        }
    }

    static void storeNumberOfPlayerLogons(int logonsPrem, int logons) {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("storeNumberOfPlayerLogons - logonsPrem: " + logonsPrem + ", logons: " + logons);
        }
        ServerStatistics.writeIntegerStatisticsToMrtgFile(STATISTIC_TYPE_NUMBER_OF_PLAYER_LOGONS, Constants.logonStatLog, logonsPrem, logons);
        if (Constants.useDatabaseForServerStatisticsLog) {
            ServerStatistics.storeOneIntegerStatisticInDatabase(STATISTIC_TYPE_NUMBER_OF_PREMIUM_PLAYER_LOGONS, logonsPrem);
            ServerStatistics.storeOneIntegerStatisticInDatabase(STATISTIC_TYPE_NUMBER_OF_PLAYER_LOGONS, logons);
        }
    }

    static void storeNumberOfSecondsLag(int secondsLag) {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("storeNumberOfSecondsLag - secondsLag: " + secondsLag);
        }
        ServerStatistics.writeIntegerStatisticsToMrtgFile(STATISTIC_TYPE_NUMBER_OF_SECONDS_LAG, Constants.lagLog, secondsLag, secondsLag);
        if (Constants.useDatabaseForServerStatisticsLog) {
            ServerStatistics.storeOneIntegerStatisticInDatabase(STATISTIC_TYPE_NUMBER_OF_SECONDS_LAG, secondsLag);
        }
    }

    static void storeNumberOfNewPlayers(int newbies) {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("storeNumberOfNewPlayers - newbies: " + newbies);
        }
        ServerStatistics.writeIntegerStatisticsToMrtgFile(STATISTIC_TYPE_NUMBER_OF_NEW_PLAYERS, Constants.newbieStatLog, newbies, newbies);
        if (Constants.useDatabaseForServerStatisticsLog) {
            ServerStatistics.storeOneIntegerStatisticInDatabase(STATISTIC_TYPE_NUMBER_OF_NEW_PLAYERS, newbies);
        }
    }

    static void storeNumberOfPayingPlayers(int paying, int payingMWithoutnewPremiums) {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("storeNumberOfPayingPlayers - paying: " + paying + ", payingMWithoutnewPremiums: " + payingMWithoutnewPremiums);
        }
        ServerStatistics.writeIntegerStatisticsToMrtgFile(STATISTIC_TYPE_NUMBER_OF_PREMIUM_PLAYER_REGISTERED, Constants.payingLog, paying, payingMWithoutnewPremiums);
        if (Constants.useDatabaseForServerStatisticsLog) {
            ServerStatistics.storeOneIntegerStatisticInDatabase(STATISTIC_TYPE_NUMBER_OF_PREMIUM_PLAYER_REGISTERED, paying);
        }
    }
}

