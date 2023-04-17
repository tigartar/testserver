/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.support;

import com.wurmonline.server.Constants;
import com.wurmonline.server.Servers;
import com.wurmonline.server.VoteServer;
import com.wurmonline.server.support.JSONArray;
import com.wurmonline.server.support.JSONException;
import com.wurmonline.server.support.JSONObject;
import com.wurmonline.server.support.JSONTokener;
import com.wurmonline.server.support.Ticket;
import com.wurmonline.server.support.TicketAction;
import com.wurmonline.server.support.Tickets;
import com.wurmonline.server.support.TrelloCard;
import com.wurmonline.server.support.TrelloCardNotFoundException;
import com.wurmonline.server.support.TrelloException;
import com.wurmonline.server.support.TrelloURL;
import com.wurmonline.server.support.VoteQuestion;
import com.wurmonline.server.support.VoteQuestions;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.net.ssl.HttpsURLConnection;

public final class Trello {
    private static Logger logger = Logger.getLogger(Trello.class.getName());
    private static final TrelloThread trelloThread = new TrelloThread();
    private static final ConcurrentLinkedDeque<TrelloCard> trelloCardQueue = new ConcurrentLinkedDeque();
    private static int tickerCount = 0;
    private static final String[] trelloLists = new String[]{"None", "Waiting GM Calls", "Waiting ARCH or Dev action", "Resolved/Cancelled", "Watching", "Feedback"};
    private static String[] trelloListIds = new String[]{"", "", "", "", "", ""};
    private static String trelloFeedbackTemplateCardId = "";
    private static final String trelloMutes = "Mutes";
    private static final String trelloMutewarns = "Mutewarns";
    private static final String trelloVotings = "Voting";
    private static final String trelloHighways = "Highways";
    private static final String trelloDeaths = "Deaths";
    private static String trelloMuteIds = "";
    private static String trelloMutewarnIds = "";
    private static String trelloVotingIds = "";
    private static String trelloHighwaysIds = "";
    private static String trelloDeathsIds = "";
    private static boolean trelloMuteStorage = false;
    public static final byte LIST_NONE = 0;
    public static final byte LIST_WAITING_GM = 1;
    public static final byte LIST_WAITING_ARCH = 2;
    public static final byte LIST_CLOSED = 3;
    public static final byte LIST_WATCHING = 4;
    public static final byte LIST_FEEDBACK = 5;
    private static final String METHOD_GET = "GET";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_PUT = "PUT";
    private static final String GZIP_ENCODING = "gzip";
    private static final String HTTP_CHARACTER_ENCODING = "UTF-8";

    private Trello() {
    }

    public static final TrelloThread getTrelloThread() {
        return trelloThread;
    }

    public static void addHighwayMessage(String server, String title, String description) {
        if (Constants.trelloMVBoardId.length() == 0) {
            return;
        }
        StringBuilder buf = new StringBuilder();
        buf.append(Tickets.convertTime(System.currentTimeMillis()));
        buf.append(" (");
        buf.append(server);
        buf.append(") ");
        buf.append(title);
        String tList = trelloHighwaysIds;
        TrelloCard tc = new TrelloCard(Constants.trelloMVBoardId, tList, buf.toString(), description, "");
        trelloCardQueue.add(tc);
    }

    public static void addImportantDeathsMessage(String server, String title, String description) {
        if (Constants.trelloMVBoardId.length() == 0) {
            return;
        }
        StringBuilder buf = new StringBuilder();
        buf.append(Tickets.convertTime(System.currentTimeMillis()));
        buf.append(" (");
        buf.append(server);
        buf.append(") ");
        buf.append(title);
        String tList = trelloDeathsIds;
        TrelloCard tc = new TrelloCard(Constants.trelloMVBoardId, tList, buf.toString(), description, "");
        trelloCardQueue.add(tc);
    }

    public static void addMessage(String sender, String playerName, String reason, int hours) {
        if (Constants.trelloMVBoardId.length() == 0) {
            return;
        }
        StringBuilder buf = new StringBuilder();
        buf.append(Tickets.convertTime(System.currentTimeMillis()) + " " + sender + " ");
        String tList = "";
        String tLabel = "";
        if (hours == 0) {
            tList = trelloMutewarnIds;
            buf.append("Mutewarn " + playerName);
            tLabel = "Orange";
        } else if (reason.length() == 0) {
            tList = trelloMuteIds;
            buf.append("Unmute " + playerName);
            tLabel = "Unmute";
        } else {
            tList = trelloMuteIds;
            buf.append("Mute " + playerName + " for " + hours + " hour" + (hours == 1 ? " " : "s "));
            tLabel = "Mute";
        }
        TrelloCard tc = new TrelloCard(Constants.trelloMVBoardId, tList, buf.toString(), reason, tLabel);
        trelloCardQueue.add(tc);
    }

    private static void updateTicketsInTrello() {
        if (trelloListIds[1].length() == 0) {
            Trello.obtainListIds();
            return;
        }
        try {
            for (Ticket ticket : Tickets.getDirtyTickets()) {
                if (ticket.getTrelloCardId().length() == 0) {
                    Trello.createCard(ticket);
                } else {
                    Trello.updateCard(ticket);
                }
                for (TicketAction ta : ticket.getDirtyTicketActions()) {
                    Trello.addAction(ticket, ta);
                }
                if (!ticket.hasFeedback() || ticket.getTrelloFeedbackCardId().length() != 0) continue;
                Trello.createFeedbackCard(ticket);
            }
            for (Ticket ticket : Tickets.getArchiveTickets()) {
                if (ticket.getTrelloCardId().length() == 0) {
                    Trello.createCard(ticket);
                }
                Trello.archiveCard(ticket);
            }
            tickerCount = 0;
        }
        catch (RuntimeException e) {
            if (tickerCount % 10 == 0) {
                logger.log(Level.INFO, "Problem communicating with Trello " + (tickerCount + 1) + " times.", e);
            }
            if (tickerCount >= 1000) {
                throw e;
            }
            ++tickerCount;
        }
    }

    private static void updateMuteVoteInTrello() {
        if (!trelloMuteStorage) {
            Trello.obtainMVListIds();
            return;
        }
        try {
            for (VoteQuestion vq : VoteQuestions.getFinishedQuestions()) {
                if (vq.getTrelloCardId().length() != 0) continue;
                Trello.createCard(vq);
            }
            for (VoteQuestion vq : VoteQuestions.getArchiveVoteQuestions()) {
                if (vq.getTrelloCardId().length() == 0) {
                    Trello.createCard(vq);
                }
                Trello.archiveCard(vq);
            }
            TrelloCard card = trelloCardQueue.pollFirst();
            while (card != null) {
                Trello.createCard(card);
                card = trelloCardQueue.pollFirst();
            }
            tickerCount = 0;
        }
        catch (RuntimeException e) {
            if (tickerCount % 10 == 0) {
                logger.log(Level.INFO, "Problem communicating with Trello " + (tickerCount + 1) + " times.", e);
            }
            if (tickerCount >= 1000) {
                throw e;
            }
            ++tickerCount;
        }
    }

    private static void archiveCardsInList(String trelloList, String listName) {
        long archiveTime = System.currentTimeMillis() - (Servers.isThisATestServer() ? 604800000L : 2419200000L);
        int archived = 0;
        String lurl = TrelloURL.make("https://api.trello.com/1/lists/{0}/cards", trelloList);
        HashMap<String, String> argumentsMap = new HashMap<String, String>();
        argumentsMap.put("fields", "id");
        JSONArray lja = Trello.doGetArray(lurl, argumentsMap);
        for (int x = 0; x < lja.length(); ++x) {
            JSONObject jo = lja.getJSONObject(x);
            String id = jo.getString("id");
            String createdDateHex = id.substring(0, 8);
            long dms = Long.parseLong(createdDateHex, 16) * 1000L;
            if (archiveTime <= dms || !Trello.archiveCard(id)) continue;
            ++archived;
        }
        if (archived > 0) {
            logger.log(Level.INFO, "Archived " + archived + " " + listName + " cards in Trello");
        }
    }

    private static void addAction(Ticket ticket, TicketAction ta) {
        try {
            String url = TrelloURL.make("https://api.trello.com/1/cards/{0}/actions/comments", ticket.getTrelloCardId());
            ta.setTrelloCommentId(Trello.addComment(url, ta.getTrelloComment()));
        }
        catch (TrelloCardNotFoundException tcnfe) {
            ta.setTrelloCommentId("Failed");
            logger.log(Level.WARNING, tcnfe.getMessage(), tcnfe);
        }
    }

    private static String addComment(String url, String text) throws TrelloCardNotFoundException {
        HashMap<String, String> keyValueMap = new HashMap<String, String>();
        keyValueMap.put("text", text);
        JSONObject jo = Trello.doPost(url, keyValueMap);
        return jo.get("id").toString();
    }

    private static void updateCard(Ticket ticket) {
        try {
            String cardId = ticket.getTrelloCardId();
            if (ticket.hasSummaryChanged()) {
                Trello.updateCard(TrelloURL.make("https://api.trello.com/1/cards/{0}/name", cardId), ticket.getTrelloName());
            }
            if (ticket.hasDescriptionChanged()) {
                Trello.updateCard(TrelloURL.make("https://api.trello.com/1/cards/{0}/desc", cardId), ticket.getDescription());
            }
            if (ticket.hasListChanged()) {
                Trello.updateCard(TrelloURL.make("https://api.trello.com/1/cards/{0}/idList", cardId), trelloListIds[ticket.getTrelloListCode()]);
            }
            Tickets.setTicketIsDirty(ticket, false);
        }
        catch (TrelloCardNotFoundException tcnfe) {
            Tickets.setTicketIsDirty(ticket, false);
            logger.log(Level.WARNING, tcnfe.getMessage(), tcnfe);
        }
    }

    private static boolean archiveCard(String cardId) {
        try {
            Trello.updateCard(TrelloURL.make("https://api.trello.com/1/cards/{0}/closed", cardId), "true");
            return true;
        }
        catch (TrelloCardNotFoundException tcnfe) {
            logger.log(Level.WARNING, tcnfe.getMessage(), tcnfe);
            return false;
        }
    }

    private static void archiveCard(VoteQuestion voteQuestion) {
        try {
            Trello.updateCard(TrelloURL.make("https://api.trello.com/1/cards/{0}/closed", voteQuestion.getTrelloCardId()), "true");
        }
        catch (TrelloCardNotFoundException tcnfe) {
            logger.log(Level.WARNING, tcnfe.getMessage(), tcnfe);
        }
        VoteQuestions.queueSetArchiveState(voteQuestion.getQuestionId(), (byte)3);
    }

    private static void archiveCard(Ticket ticket) {
        try {
            Trello.updateCard(TrelloURL.make("https://api.trello.com/1/cards/{0}/closed", ticket.getTrelloCardId()), "true");
            if (ticket.hasFeedback() && ticket.getTrelloFeedbackCardId().length() > 0) {
                Trello.updateCard(TrelloURL.make("https://api.trello.com/1/cards/{0}/closed", ticket.getTrelloFeedbackCardId()), "true");
            }
        }
        catch (TrelloCardNotFoundException tcnfe) {
            logger.log(Level.WARNING, tcnfe.getMessage(), tcnfe);
        }
        Tickets.setTicketArchiveState(ticket, (byte)3);
    }

    private static void updateCard(String url, String value) throws TrelloCardNotFoundException {
        HashMap<String, String> keyValueMap = new HashMap<String, String>();
        keyValueMap.put("value", value);
        Trello.doPut(url, keyValueMap);
    }

    private static void createCard(Ticket ticket) {
        try {
            String url = TrelloURL.make("https://api.trello.com/1/cards", new String[0]);
            HashMap<String, String> keyValueMap = new HashMap<String, String>();
            keyValueMap.put("name", ticket.getTrelloName());
            keyValueMap.put("desc", ticket.getDescription());
            keyValueMap.put("idList", trelloListIds[ticket.getTrelloListCode()]);
            JSONObject jo = Trello.doPost(url, keyValueMap);
            String shortLink = Trello.getShortLink(jo.getString("shortUrl"));
            Tickets.setTicketTrelloCardId(ticket, shortLink);
        }
        catch (TrelloCardNotFoundException tcnfe) {
            logger.log(Level.WARNING, tcnfe.getMessage(), tcnfe);
        }
    }

    private static void createCard(TrelloCard card) {
        try {
            String url = TrelloURL.make("https://api.trello.com/1/cards", new String[0]);
            HashMap<String, String> keyValueMap = new HashMap<String, String>();
            keyValueMap.put("name", card.getTitle());
            keyValueMap.put("desc", card.getDescription());
            keyValueMap.put("idList", card.getListId());
            JSONObject jSONObject = Trello.doPost(url, keyValueMap);
        }
        catch (TrelloCardNotFoundException tcnfe) {
            logger.log(Level.WARNING, tcnfe.getMessage(), tcnfe);
        }
    }

    private static void createCard(VoteQuestion question) {
        try {
            String name = Tickets.convertTime(question.getVoteStart()) + " " + question.getQuestionTitle() + " (" + Tickets.convertTime(question.getVoteEnd()) + ")";
            String desc = question.getQuestionText();
            String url = TrelloURL.make("https://api.trello.com/1/cards", new String[0]);
            HashMap<String, String> keyValueMap = new HashMap<String, String>();
            keyValueMap.put("name", name);
            keyValueMap.put("desc", desc);
            keyValueMap.put("idList", trelloVotingIds);
            JSONObject jo = Trello.doPost(url, keyValueMap);
            String shortLink = Trello.getShortLink(jo.getString("shortUrl"));
            Trello.addVoteQuestionDetails(question, shortLink);
            VoteQuestions.queueSetTrelloCardId(question.getQuestionId(), shortLink);
        }
        catch (TrelloCardNotFoundException tcnfe) {
            logger.log(Level.WARNING, tcnfe.getMessage(), tcnfe);
        }
    }

    private static void addVoteQuestionDetails(VoteQuestion question, String shortLink) {
        try {
            String url = TrelloURL.make("https://api.trello.com/1/cards/{0}/actions/comments", shortLink);
            StringBuilder buf = new StringBuilder();
            buf.append("SUMMARY\n\n");
            buf.append(Trello.getOptionSummary(question.getOption1Text(), question.getOption1Count(), question.getVoteCount()));
            buf.append(Trello.getOptionSummary(question.getOption2Text(), question.getOption2Count(), question.getVoteCount()));
            buf.append(Trello.getOptionSummary(question.getOption3Text(), question.getOption3Count(), question.getVoteCount()));
            buf.append(Trello.getOptionSummary(question.getOption4Text(), question.getOption4Count(), question.getVoteCount()));
            buf.append("\nTotal Players Voted: " + question.getVoteCount());
            String reply = Trello.addComment(url, buf.toString());
            buf = new StringBuilder();
            buf.append("OPTIONS\n");
            buf.append("\nVote Start: " + Tickets.convertTime(question.getVoteStart()));
            buf.append("\nVote End: " + Tickets.convertTime(question.getVoteEnd()));
            buf.append("\nAllow Multiple: " + question.isAllowMultiple());
            buf.append("\nPrem Only: " + question.isPremOnly());
            buf.append("\nJK: " + question.isJK());
            buf.append("\nMR: " + question.isMR());
            buf.append("\nHots: " + question.isHots());
            buf.append("\nFreedom: " + question.isFreedom());
            reply = Trello.addComment(url, buf.toString());
            buf = new StringBuilder();
            buf.append("SERVERS\n");
            for (VoteServer vs : question.getServers()) {
                buf.append("\n" + Servers.getServerWithId((int)vs.getServerId()).name);
            }
            reply = Trello.addComment(url, buf.toString());
        }
        catch (TrelloCardNotFoundException tcnfe) {
            logger.log(Level.WARNING, tcnfe.getMessage(), tcnfe);
        }
    }

    private static String getOptionSummary(String text, int count, int total) {
        if (text.length() == 0) {
            return "";
        }
        int perc = -1;
        String percText = " (Nan%)";
        if (total > 0) {
            perc = count * 100 / total;
            percText = " (" + perc + "%)";
        }
        StringBuilder buf = new StringBuilder();
        buf.append(text + " [" + count + percText + "]\n");
        return buf.toString();
    }

    private static void createFeedbackCard(Ticket ticket) {
        try {
            String url = TrelloURL.make("https://api.trello.com/1/cards", new String[0]);
            HashMap<String, String> keyValueMap = new HashMap<String, String>();
            keyValueMap.put("name", ticket.getTrelloFeedbackTitle());
            keyValueMap.put("idList", trelloListIds[5]);
            keyValueMap.put("idCardSource", trelloFeedbackTemplateCardId);
            JSONObject fjo = Trello.doPost(url, keyValueMap);
            String shortLink = Trello.getShortLink(fjo.getString("shortUrl"));
            Trello.updateCard(TrelloURL.make("https://api.trello.com/1/cards/{0}/desc", shortLink), ticket.getFeedbackText());
            Trello.tickSelected(ticket, shortLink);
            Tickets.setTicketTrelloFeedbackCardId(ticket, shortLink);
        }
        catch (TrelloCardNotFoundException tcnfe) {
            logger.log(Level.WARNING, tcnfe.getMessage(), tcnfe);
        }
    }

    private static void tickSelected(Ticket ticket, String cardId) {
        try {
            int i;
            String curl = TrelloURL.make("https://api.trello.com/1/cards/{0}/checklists", cardId);
            HashMap<String, String> argumentsMap = new HashMap<String, String>();
            argumentsMap.put("card_fields", "checkItemStates,idChecklists,name");
            argumentsMap.put("checkItem_fields", "name");
            argumentsMap.put("fields", "name");
            JSONArray ja = Trello.doGetArray(curl, argumentsMap);
            String[] nService = new String[]{"Quality Of Service", "Superior", "Good", "Average", "Fair", "Poor"};
            String[] nCourteous = new String[]{"Courteous", "Strongly Agree", "Somewhat Agree", "Neutral", "Somewhat Disagree", "Strongly Disagree"};
            String[] nKnowledgeable = new String[]{"Knowledgeable", "Strongly Agree", "Somewhat Agree", "Neutral", "Somewhat Disagree", "Strongly Disagree"};
            String[] nGeneral = new String[]{"General", "Wrong Info", "No Understand", "Unclear", "No Solve", "Disorganized", "Other", "Fine"};
            String[] nQuality = new String[]{"Quality", "Patient", "Enthusiastic", "Listened", "Friendly", "Responsive", "Nothing"};
            String[] nIrked = new String[]{"Irked", "Patient", "Enthusiastic", "Listened", "Friendly", "Responsive", "Nothing"};
            String[] idService = new String[]{"", "", "", "", "", ""};
            String[] idCourteous = new String[]{"", "", "", "", "", ""};
            String[] idKnowledgeable = new String[]{"", "", "", "", "", ""};
            String[] idGeneral = new String[]{"", "", "", "", "", "", "", ""};
            String[] idQuality = new String[]{"", "", "", "", "", "", ""};
            String[] idIrked = new String[]{"", "", "", "", "", "", ""};
            idService[0] = ja.getJSONObject(nService[0]).getString("id");
            for (i = 1; i < idService.length; ++i) {
                idService[i] = ja.getJSONObject(nService[0]).getJSONArray("checkItems").getJSONObject(nService[i]).getString("id");
            }
            idCourteous[0] = ja.getJSONObject(nCourteous[0]).getString("id");
            for (i = 1; i < idCourteous.length; ++i) {
                idCourteous[i] = ja.getJSONObject(nCourteous[0]).getJSONArray("checkItems").getJSONObject(nCourteous[i]).getString("id");
            }
            idKnowledgeable[0] = ja.getJSONObject(nKnowledgeable[0]).getString("id");
            for (i = 1; i < idKnowledgeable.length; ++i) {
                idKnowledgeable[i] = ja.getJSONObject(nKnowledgeable[0]).getJSONArray("checkItems").getJSONObject(nKnowledgeable[i]).getString("id");
            }
            idGeneral[0] = ja.getJSONObject(nGeneral[0]).getString("id");
            for (i = 1; i < idGeneral.length; ++i) {
                idGeneral[i] = ja.getJSONObject(nGeneral[0]).getJSONArray("checkItems").getJSONObject(nGeneral[i]).getString("id");
            }
            idQuality[0] = ja.getJSONObject(nQuality[0]).getString("id");
            for (i = 1; i < idQuality.length; ++i) {
                idQuality[i] = ja.getJSONObject(nQuality[0]).getJSONArray("checkItems").getJSONObject(nQuality[i]).getString("id");
            }
            idIrked[0] = ja.getJSONObject(nIrked[0]).getString("id");
            for (i = 1; i < idIrked.length; ++i) {
                idIrked[i] = ja.getJSONObject(nIrked[0]).getJSONArray("checkItems").getJSONObject(nIrked[i]).getString("id");
            }
            TicketAction ta = ticket.getFeedback();
            if (ta.wasServiceSuperior()) {
                Trello.tick(cardId, idService[0], idService[1]);
            }
            if (ta.wasServiceGood()) {
                Trello.tick(cardId, idService[0], idService[2]);
            }
            if (ta.wasServiceAverage()) {
                Trello.tick(cardId, idService[0], idService[3]);
            }
            if (ta.wasServiceFair()) {
                Trello.tick(cardId, idService[0], idService[4]);
            }
            if (ta.wasServicePoor()) {
                Trello.tick(cardId, idService[0], idService[5]);
            }
            if (ta.wasCourteousStronglyAgree()) {
                Trello.tick(cardId, idCourteous[0], idCourteous[1]);
            }
            if (ta.wasCourteousSomewhatAgree()) {
                Trello.tick(cardId, idCourteous[0], idCourteous[2]);
            }
            if (ta.wasCourteousNeutral()) {
                Trello.tick(cardId, idCourteous[0], idCourteous[3]);
            }
            if (ta.wasCourteousSomewhatDisagree()) {
                Trello.tick(cardId, idCourteous[0], idCourteous[4]);
            }
            if (ta.wasCourteousStronglyDisagree()) {
                Trello.tick(cardId, idCourteous[0], idCourteous[5]);
            }
            if (ta.wasKnowledgeableStronglyAgree()) {
                Trello.tick(cardId, idKnowledgeable[0], idKnowledgeable[1]);
            }
            if (ta.wasKnowledgeableSomewhatAgree()) {
                Trello.tick(cardId, idKnowledgeable[0], idKnowledgeable[2]);
            }
            if (ta.wasKnowledgeableNeutral()) {
                Trello.tick(cardId, idKnowledgeable[0], idKnowledgeable[3]);
            }
            if (ta.wasKnowledgeableSomewhatDisagree()) {
                Trello.tick(cardId, idKnowledgeable[0], idKnowledgeable[4]);
            }
            if (ta.wasKnowledgeableStronglyDisagree()) {
                Trello.tick(cardId, idKnowledgeable[0], idKnowledgeable[5]);
            }
            if (ta.wasGeneralWrongInfo()) {
                Trello.tick(cardId, idGeneral[0], idGeneral[1]);
            }
            if (ta.wasGeneralNoUnderstand()) {
                Trello.tick(cardId, idGeneral[0], idGeneral[2]);
            }
            if (ta.wasGeneralUnclear()) {
                Trello.tick(cardId, idGeneral[0], idGeneral[3]);
            }
            if (ta.wasGeneralNoSolve()) {
                Trello.tick(cardId, idGeneral[0], idGeneral[4]);
            }
            if (ta.wasGeneralDisorganized()) {
                Trello.tick(cardId, idGeneral[0], idGeneral[5]);
            }
            if (ta.wasGeneralOther()) {
                Trello.tick(cardId, idGeneral[0], idGeneral[6]);
            }
            if (ta.wasGeneralFine()) {
                Trello.tick(cardId, idGeneral[0], idGeneral[7]);
            }
            if (ta.wasQualityPatient()) {
                Trello.tick(cardId, idQuality[0], idQuality[1]);
            }
            if (ta.wasQualityEnthusiastic()) {
                Trello.tick(cardId, idQuality[0], idQuality[2]);
            }
            if (ta.wasQualityListened()) {
                Trello.tick(cardId, idQuality[0], idQuality[3]);
            }
            if (ta.wasQualityFriendly()) {
                Trello.tick(cardId, idQuality[0], idQuality[4]);
            }
            if (ta.wasQualityResponsive()) {
                Trello.tick(cardId, idQuality[0], idQuality[5]);
            }
            if (ta.wasQualityNothing()) {
                Trello.tick(cardId, idQuality[0], idQuality[6]);
            }
            if (ta.wasIrkedPatient()) {
                Trello.tick(cardId, idIrked[0], idIrked[1]);
            }
            if (ta.wasIrkedEnthusiastic()) {
                Trello.tick(cardId, idIrked[0], idIrked[2]);
            }
            if (ta.wasIrkedListened()) {
                Trello.tick(cardId, idIrked[0], idIrked[3]);
            }
            if (ta.wasIrkedFriendly()) {
                Trello.tick(cardId, idIrked[0], idIrked[4]);
            }
            if (ta.wasIrkedResponsive()) {
                Trello.tick(cardId, idIrked[0], idIrked[5]);
            }
            if (ta.wasIrkedNothing()) {
                Trello.tick(cardId, idIrked[0], idIrked[6]);
            }
        }
        catch (TrelloCardNotFoundException tcnfe) {
            logger.log(Level.WARNING, tcnfe.getMessage(), tcnfe);
        }
    }

    private static void tick(String cardId, String checkListId, String checkItemId) throws TrelloCardNotFoundException {
        String url = TrelloURL.make("https://api.trello.com/1/cards/{0}/checklist/{1}/checkItem/{2}/state", cardId, checkListId, checkItemId);
        HashMap<String, String> keyValueMap = new HashMap<String, String>();
        keyValueMap.put("idCheckList", checkListId);
        keyValueMap.put("idCheckItem", checkItemId);
        keyValueMap.put("value", "true");
        JSONObject jo = Trello.doPut(url, keyValueMap);
    }

    private static void obtainListIds() {
        String url = TrelloURL.make("https://api.trello.com/1/boards/{0}/lists", Constants.trelloBoardid);
        JSONArray ja = Trello.doGetArray(url);
        int count = 0;
        block0: for (int x = 0; x < ja.length(); ++x) {
            JSONObject jo = ja.getJSONObject(x);
            String name = jo.getString("name");
            for (int y = 1; y <= 5; ++y) {
                if (!name.equalsIgnoreCase(trelloLists[y])) continue;
                Trello.trelloListIds[y] = jo.getString("id");
                ++count;
                continue block0;
            }
        }
        if (count != trelloListIds.length - 1) {
            throw new JSONException("Not all the required lists found on Trello Ticket board.");
        }
        String lurl = TrelloURL.make("https://api.trello.com/1/lists/{0}/cards", trelloListIds[5]);
        HashMap<String, String> argumentsMap = new HashMap<String, String>();
        argumentsMap.put("fields", "name");
        argumentsMap.put("card_fields", "name");
        JSONArray lja = Trello.doGetArray(lurl, argumentsMap);
        for (int x = 0; x < lja.length(); ++x) {
            JSONObject jo = lja.getJSONObject(x);
            String name = jo.getString("name");
            if (!name.equalsIgnoreCase("Feedback Checklist Template")) continue;
            trelloFeedbackTemplateCardId = jo.getString("id");
            break;
        }
        if (trelloFeedbackTemplateCardId.length() == 0) {
            throw new JSONException("Could not find the Feedback Checklist Template on Trello Ticket board.");
        }
    }

    private static void obtainMVListIds() {
        if (Constants.trelloMVBoardId.length() == 0) {
            return;
        }
        String url = TrelloURL.make("https://api.trello.com/1/boards/{0}/lists", Constants.trelloMVBoardId);
        JSONArray ja = Trello.doGetArray(url);
        for (int x = 0; x < ja.length(); ++x) {
            JSONObject jo = ja.getJSONObject(x);
            String name = jo.getString("name");
            if (name.equals(trelloMutes)) {
                trelloMuteIds = jo.getString("id");
                continue;
            }
            if (name.equals(trelloMutewarns)) {
                trelloMutewarnIds = jo.getString("id");
                continue;
            }
            if (name.equals(trelloVotings)) {
                trelloVotingIds = jo.getString("id");
                continue;
            }
            if (name.equals(trelloHighways)) {
                trelloHighwaysIds = jo.getString("id");
                continue;
            }
            if (!name.equals(trelloDeaths)) continue;
            trelloDeathsIds = jo.getString("id");
        }
        if (trelloMuteIds.length() == 0 || trelloMutewarnIds.length() == 0 || trelloVotingIds.length() == 0 || trelloHighwaysIds.length() == 0) {
            throw new JSONException("Not all the required lists found on Trello Mute Vote board.");
        }
        trelloMuteStorage = true;
        Trello.archiveCardsInList(trelloMuteIds, trelloMutes);
        Trello.archiveCardsInList(trelloMutewarnIds, trelloMutewarns);
        Trello.archiveCardsInList(trelloHighwaysIds, trelloHighways);
    }

    private static String getShortLink(String shortUrl) {
        String[] parts = shortUrl.split("/");
        String shortLink = parts[parts.length - 1];
        return shortLink;
    }

    private static JSONArray doGetArray(String url) {
        try {
            InputStream in = Trello.doRequest(url, METHOD_GET, null);
            if (in == null) {
                throw new JSONException("Failed read permissions for Trello board.");
            }
            JSONTokener tk = new JSONTokener(in);
            return new JSONArray(tk);
        }
        catch (TrelloCardNotFoundException tcnfe) {
            throw new JSONException("Cannot find ticket, but were not looking for one");
        }
    }

    private static JSONArray doGetArray(String url, Map<String, String> map) {
        String lurl = url;
        try {
            boolean hasMap;
            StringBuilder sb = new StringBuilder();
            boolean bl = hasMap = map != null && !map.isEmpty();
            if (hasMap) {
                for (String key : map.keySet()) {
                    sb.append("&");
                    sb.append(URLEncoder.encode(key, HTTP_CHARACTER_ENCODING));
                    sb.append("=");
                    sb.append(URLEncoder.encode(map.get(key), HTTP_CHARACTER_ENCODING));
                }
                lurl = url + sb.toString();
            }
        }
        catch (UnsupportedEncodingException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        try {
            InputStream in = Trello.doRequest(lurl, METHOD_GET, null);
            if (in == null) {
                throw new JSONException("Failed read permissions for Trello board.");
            }
            JSONTokener tk = new JSONTokener(in);
            return new JSONArray(tk);
        }
        catch (TrelloCardNotFoundException tcnfe) {
            throw new JSONException("Cannot find ticket, but were not looking for one");
        }
    }

    private static JSONObject doPut(String url, Map<String, String> map) throws TrelloCardNotFoundException {
        InputStream in = Trello.doRequest(url, METHOD_PUT, map);
        if (in == null) {
            throw new JSONException("Failed read permissions for Trello board.");
        }
        JSONTokener tk = new JSONTokener(in);
        return new JSONObject(tk);
    }

    private static JSONObject doPost(String url, Map<String, String> map) throws TrelloCardNotFoundException {
        InputStream in = Trello.doRequest(url, METHOD_POST, map);
        if (in == null) {
            throw new JSONException("Failed read permissions for Trello board.");
        }
        JSONTokener tk = new JSONTokener(in);
        return new JSONObject(tk);
    }

    private static InputStream doRequest(String url, String requestMethod, Map<String, String> map) throws TrelloCardNotFoundException {
        try {
            boolean hasMap = map != null && !map.isEmpty();
            HttpsURLConnection conn = (HttpsURLConnection)new URL(url).openConnection();
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            conn.setDoOutput(requestMethod.equals(METHOD_POST) || requestMethod.equals(METHOD_PUT));
            conn.setRequestMethod(requestMethod);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            String arguments = "";
            if (hasMap) {
                StringBuilder sb = new StringBuilder();
                for (String key : map.keySet()) {
                    sb.append(sb.length() > 0 ? "&" : "");
                    sb.append(URLEncoder.encode(key, HTTP_CHARACTER_ENCODING));
                    sb.append("=");
                    sb.append(URLEncoder.encode(map.get(key), HTTP_CHARACTER_ENCODING));
                }
                conn.getOutputStream().write(sb.toString().getBytes());
                conn.getOutputStream().close();
                arguments = sb.toString();
            }
            conn.connect();
            int rc = conn.getResponseCode();
            String responseMessage = conn.getResponseMessage();
            if (rc != 200) {
                logger.info("response " + rc + " (" + responseMessage + ") from " + requestMethod + " " + url + " args:" + arguments);
            }
            if (rc == 404) {
                throw new TrelloCardNotFoundException("Ticket not found");
            }
            if (rc > 399) {
                String str = Trello.stream2String(conn.getErrorStream());
                logger.info("error response:" + str);
                return null;
            }
            return Trello.getWrappedInputStream(conn.getInputStream(), GZIP_ENCODING.equalsIgnoreCase(conn.getContentEncoding()));
        }
        catch (IOException e) {
            throw new TrelloException(e.getMessage());
        }
    }

    private static InputStream getWrappedInputStream(InputStream is, boolean gzip) throws IOException {
        if (gzip) {
            return new BufferedInputStream(new GZIPInputStream(is));
        }
        return new BufferedInputStream(is);
    }

    private static String stream2String(InputStream in) {
        InputStreamReader is = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(is);
        StringBuilder sb = new StringBuilder();
        try {
            String read = br.readLine();
            while (read != null) {
                sb.append(read);
                read = br.readLine();
            }
        }
        catch (IOException e) {
            return "Error trying to read stream:" + e.getMessage();
        }
        return sb.toString();
    }

    private static final class TrelloThread
    implements Runnable {
        TrelloThread() {
        }

        @Override
        public void run() {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("Running newSingleThreadScheduledExecutor for calling Tickets.ticker()");
            }
            try {
                float lElapsedTime;
                long now = System.nanoTime();
                if (Servers.isThisLoginServer() && Constants.trelloApiKey.length() > 0) {
                    Trello.updateTicketsInTrello();
                    Trello.updateMuteVoteInTrello();
                }
                if ((lElapsedTime = (float)(System.nanoTime() - now) / 1000000.0f) > (float)Constants.lagThreshold) {
                    logger.info("Finished calling Tickets.ticker(), which took " + lElapsedTime + " millis.");
                }
            }
            catch (RuntimeException e) {
                logger.log(Level.WARNING, "Caught exception in ScheduledExecutorService while calling Tickets.ticker()", e);
                throw e;
            }
        }
    }
}

