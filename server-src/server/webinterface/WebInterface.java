/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.webinterface;

import com.wurmonline.server.players.Ban;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface WebInterface
extends Remote {
    public static final int DEFAULT_RMI_PORT = 7220;
    public static final int DEFAULT_REGISTRATION_PORT = 7221;

    public int getPower(String var1, long var2) throws RemoteException;

    public boolean isRunning(String var1) throws RemoteException;

    public int getPlayerCount(String var1) throws RemoteException;

    public int getPremiumPlayerCount(String var1) throws RemoteException;

    public String getTestMessage(String var1) throws RemoteException;

    public void broadcastMessage(String var1, String var2) throws RemoteException;

    public long getAccountStatusForPlayer(String var1, String var2) throws RemoteException;

    public long chargeMoney(String var1, String var2, long var3) throws RemoteException;

    public String getServerStatus(String var1) throws RemoteException;

    public Map<String, Integer> getBattleRanks(String var1, int var2) throws RemoteException;

    public Map<String, Long> getFriends(String var1, long var2) throws RemoteException;

    public Map<String, String> getInventory(String var1, long var2) throws RemoteException;

    public Map<Long, Long> getBodyItems(String var1, long var2) throws RemoteException;

    public Map<String, Float> getSkills(String var1, long var2) throws RemoteException;

    public Map<String, ?> getPlayerSummary(String var1, long var2) throws RemoteException;

    public long getLocalCreationTime(String var1) throws RemoteException;

    public String ban(String var1, String var2, String var3, int var4) throws RemoteException;

    public String pardonban(String var1, String var2) throws RemoteException;

    public String addBannedIp(String var1, String var2, String var3, int var4) throws RemoteException;

    public Ban[] getPlayersBanned(String var1) throws RemoteException;

    public Ban[] getIpsBanned(String var1) throws RemoteException;

    public String removeBannedIp(String var1, String var2) throws RemoteException;

    public Map<Integer, String> getKingdoms(String var1) throws RemoteException;

    public Map<Long, String> getPlayersForKingdom(String var1, int var2) throws RemoteException;

    public long getPlayerId(String var1, String var2) throws RemoteException;

    public Map<String, ?> createPlayer(String var1, String var2, String var3, String var4, String var5, String var6, byte var7, byte var8, long var9, byte var11) throws RemoteException;

    public Map<String, String> createPlayerPhaseOne(String var1, String var2, String var3) throws RemoteException;

    public Map<String, ?> createPlayerPhaseTwo(String var1, String var2, String var3, String var4, String var5, String var6, byte var7, byte var8, long var9, byte var11, String var12) throws RemoteException;

    public Map<String, ?> createPlayerPhaseTwo(String var1, String var2, String var3, String var4, String var5, String var6, byte var7, byte var8, long var9, byte var11, String var12, int var13) throws RemoteException;

    public Map<String, ?> createPlayerPhaseTwo(String var1, String var2, String var3, String var4, String var5, String var6, byte var7, byte var8, long var9, byte var11, String var12, int var13, boolean var14) throws RemoteException;

    public byte[] createAndReturnPlayer(String var1, String var2, String var3, String var4, String var5, String var6, byte var7, byte var8, long var9, byte var11, boolean var12, boolean var13, boolean var14) throws RemoteException;

    public Map<String, String> addMoneyToBank(String var1, String var2, long var3, String var5) throws RemoteException;

    public long getMoney(String var1, long var2, String var4) throws RemoteException;

    public Map<String, String> reversePayment(String var1, long var2, int var4, int var5, String var6, String var7, String var8) throws RemoteException;

    public Map<String, String> addMoneyToBank(String var1, String var2, long var3, String var5, boolean var6) throws RemoteException;

    public Map<String, String> addMoneyToBank(String var1, String var2, long var3, long var5, String var7, boolean var8) throws RemoteException;

    public Map<String, String> addPlayingTime(String var1, String var2, int var3, int var4, String var5, boolean var6) throws RemoteException;

    public Map<String, String> addPlayingTime(String var1, String var2, int var3, int var4, String var5) throws RemoteException;

    public Map<Integer, String> getDeeds(String var1) throws RemoteException;

    public Map<String, ?> getDeedSummary(String var1, int var2) throws RemoteException;

    public Map<String, Long> getPlayersForDeed(String var1, int var2) throws RemoteException;

    public Map<String, Integer> getAlliesForDeed(String var1, int var2) throws RemoteException;

    public String[] getHistoryForDeed(String var1, int var2, int var3) throws RemoteException;

    public String[] getAreaHistory(String var1, int var2) throws RemoteException;

    public Map<String, ?> getItemSummary(String var1, long var2) throws RemoteException;

    public Map<String, String> getPlayerIPAddresses(String var1) throws RemoteException;

    public Map<String, String> getNameBans(String var1) throws RemoteException;

    public Map<String, String> getIPBans(String var1) throws RemoteException;

    public Map<String, String> getWarnings(String var1) throws RemoteException;

    public String getWurmTime(String var1) throws RemoteException;

    public String getUptime(String var1) throws RemoteException;

    public String getNews(String var1) throws RemoteException;

    public String getGameInfo(String var1) throws RemoteException;

    public Map<String, String> getKingdomInfluence(String var1) throws RemoteException;

    public Map<String, ?> getMerchantSummary(String var1, long var2) throws RemoteException;

    public Map<String, ?> getBankAccount(String var1, long var2) throws RemoteException;

    public Map<String, ?> authenticateUser(String var1, String var2, String var3, String var4, Map var5) throws RemoteException;

    public Map<String, ?> authenticateUser(String var1, String var2, String var3, String var4) throws RemoteException;

    public Map<String, String> changePassword(String var1, String var2, String var3, String var4) throws RemoteException;

    public Map<String, String> changePassword(String var1, String var2, String var3, String var4, String var5) throws RemoteException;

    public boolean changePassword(String var1, long var2, String var4) throws RemoteException;

    public Map<String, String> changeEmail(String var1, String var2, String var3, String var4) throws RemoteException;

    public String getChallengePhrase(String var1, String var2) throws RemoteException;

    public String[] getPlayerNamesForEmail(String var1, String var2) throws RemoteException;

    public String getEmailAddress(String var1, String var2) throws RemoteException;

    public Map<String, String> requestPasswordReset(String var1, String var2, String var3) throws RemoteException;

    public Map<Integer, String> getAllServers(String var1) throws RemoteException;

    public Map<Integer, String> getAllServerInternalAddresses(String var1) throws RemoteException;

    public boolean sendMail(String var1, String var2, String var3, String var4, String var5) throws RemoteException;

    public Map<String, String> getPendingAccounts(String var1) throws RemoteException;

    public void shutDown(String var1, String var2, String var3, String var4, int var5) throws RemoteException;

    public Map<String, Byte> getReferrers(String var1, long var2) throws RemoteException;

    public String addReferrer(String var1, String var2, long var3) throws RemoteException;

    public String acceptReferrer(String var1, long var2, String var4, boolean var5) throws RemoteException;

    public Map<String, Double> getSkillStats(String var1, int var2) throws RemoteException;

    public Map<Integer, String> getSkills(String var1) throws RemoteException;

    public Map<String, ?> getStructureSummary(String var1, long var2) throws RemoteException;

    public long getStructureIdFromWrit(String var1, long var2) throws RemoteException;

    public Map<String, ?> getTileSummary(String var1, int var2, int var3, boolean var4) throws RemoteException;

    public String getReimbursementInfo(String var1, String var2) throws RemoteException;

    public boolean withDraw(String var1, String var2, String var3, String var4, int var5, int var6, boolean var7, int var8) throws RemoteException;

    public boolean transferPlayer(String var1, String var2, int var3, int var4, boolean var5, int var6, byte[] var7) throws RemoteException;

    public boolean setCurrentServer(String var1, String var2, int var3) throws RemoteException;

    public boolean addDraggedItem(String var1, long var2, byte[] var4, long var5, int var7, int var8) throws RemoteException;

    public String rename(String var1, String var2, String var3, String var4, int var5) throws RemoteException;

    public String changePassword(String var1, String var2, String var3, String var4, int var5) throws RemoteException;

    public String changeEmail(String var1, String var2, String var3, String var4, String var5, int var6, String var7, String var8) throws RemoteException;

    public String addReimb(String var1, String var2, String var3, int var4, int var5, int var6, boolean var7) throws RemoteException;

    public long[] getCurrentServerAndWurmid(String var1, String var2, long var3) throws RemoteException;

    public Map<Long, byte[]> getPlayerStates(String var1, long[] var2) throws RemoteException, WurmServerException;

    public void manageFeature(String var1, int var2, int var3, boolean var4, boolean var5, boolean var6) throws RemoteException;

    public void startShutdown(String var1, String var2, int var3, String var4) throws RemoteException;

    public String sendMail(String var1, byte[] var2, byte[] var3, long var4, long var6, int var8) throws RemoteException;

    public String setPlayerPremiumTime(String var1, long var2, long var4, int var6, int var7, String var8) throws RemoteException;

    public String setPlayerMoney(String var1, long var2, long var4, long var6, String var8) throws RemoteException;

    public Map<String, String> doesPlayerExist(String var1, String var2) throws RemoteException;

    public void setWeather(String var1, float var2, float var3, float var4) throws RemoteException;

    public String sendVehicle(String var1, byte[] var2, byte[] var3, long var4, long var6, int var8, int var9, int var10, int var11, float var12) throws RemoteException;

    public void requestDemigod(String var1, byte var2, String var3) throws RemoteException;

    public String ascend(String var1, int var2, String var3, long var4, byte var6, byte var7, byte var8, float var9, float var10, float var11, float var12, float var13, float var14, float var15) throws RemoteException;

    public boolean requestDeityMove(String var1, int var2, int var3, String var4) throws RemoteException;

    public void setKingdomInfo(String var1, int var2, byte var3, byte var4, String var5, String var6, String var7, String var8, String var9, String var10, boolean var11) throws RemoteException;

    public boolean kingdomExists(String var1, int var2, byte var3, boolean var4) throws RemoteException;

    public void genericWebCommand(String var1, short var2, long var3, byte[] var5) throws RemoteException;

    public int[] getPremTimeSilvers(String var1, long var2) throws RemoteException;

    public void awardPlayer(String var1, long var2, String var4, int var5, int var6) throws RemoteException;

    public boolean isFeatureEnabled(String var1, int var2) throws RemoteException;

    public boolean setPlayerFlag(String var1, long var2, int var4, boolean var5) throws RemoteException;
}

