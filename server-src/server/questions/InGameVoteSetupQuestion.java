/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.VoteServer;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.PlayerVote;
import com.wurmonline.server.players.PlayerVotes;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.support.Tickets;
import com.wurmonline.server.support.VoteQuestion;
import com.wurmonline.server.support.VoteQuestions;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InGameVoteSetupQuestion
extends Question {
    private static final Logger logger = Logger.getLogger(InGameVoteSetupQuestion.class.getName());
    private static final byte SHOWLIST = 0;
    private static final byte ADDNEW = 1;
    private static final byte EDITQUESTION = 2;
    private static final byte VIEWQUESTION = 3;
    private static final byte SHOWRESULTS = 4;
    private static final SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    private byte part = 0;
    private int questionId = 0;
    private String qtitle = "";
    private String qtext = "";
    private String opt1 = "";
    private String opt2 = "";
    private String opt3 = "";
    private String opt4 = "";
    private boolean ma = false;
    private boolean po = false;
    private boolean jk = true;
    private boolean mr = true;
    private boolean hots = true;
    private boolean freedom = true;
    private long voteStart = 0L;
    private long voteEnd = 0L;
    private int startDay;
    private int startMonth;
    private int startYear;
    private int startHour;
    private int startMins;
    private int lasts;
    private String failReason = "";
    private Set<Integer> serverIds = new HashSet<Integer>();
    private int width;
    private int height;

    public InGameVoteSetupQuestion(Creature aResponder) {
        this(aResponder, "In game voting setup", "List of Questions", 0);
    }

    public InGameVoteSetupQuestion(Creature aResponder, String aTitle, String aQuestion, byte aPart) {
        super(aResponder, aTitle, aQuestion, 114, aResponder.getWurmId());
        this.part = aPart;
    }

    public InGameVoteSetupQuestion(Creature aResponder, String aTitle, String aQuestion, byte aPart, int aQuestionId, String aFailed) {
        super(aResponder, aTitle, aQuestion, 114, aResponder.getWurmId());
        this.part = aPart;
        this.questionId = aQuestionId;
        this.failReason = aFailed;
    }

    public InGameVoteSetupQuestion(Creature aResponder, String aTitle, String aQuestion, byte aPart, int aQuestionId) {
        this(aResponder, aTitle, aQuestion, aPart, aQuestionId, "");
    }

    @Override
    public void answer(Properties aAnswer) {
        this.setAnswer(aAnswer);
        if (this.type == 0) {
            logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
            return;
        }
        if (this.type == 114) {
            switch (this.part) {
                case 0: {
                    String header;
                    String windowTitle;
                    String strId = aAnswer.getProperty("qid");
                    this.questionId = Integer.parseInt(strId);
                    if (this.questionId == 0) {
                        InGameVoteSetupQuestion igvsq = new InGameVoteSetupQuestion(this.getResponder(), "In game voting setup", "Add New Question", 1);
                        igvsq.sendQuestion();
                        break;
                    }
                    byte nextPart = 0;
                    VoteQuestion vq = VoteQuestions.getVoteQuestion(this.questionId);
                    if (vq.getSent() == 0) {
                        nextPart = 2;
                        windowTitle = "Edit Question";
                        header = "";
                    } else if (vq.getVoteEnd() > System.currentTimeMillis()) {
                        nextPart = 3;
                        windowTitle = "View Question";
                        header = vq.getQuestionTitle();
                    } else {
                        nextPart = 4;
                        windowTitle = "Poll Results";
                        header = vq.getQuestionTitle();
                    }
                    InGameVoteSetupQuestion igvsq = new InGameVoteSetupQuestion(this.getResponder(), windowTitle, header, nextPart, this.questionId);
                    igvsq.sendQuestion();
                    break;
                }
                case 1: {
                    this.questionId = VoteQuestions.getNextQuestionId();
                }
                case 2: {
                    String delete = aAnswer.getProperty("remove");
                    if (delete != null && Boolean.parseBoolean(delete)) {
                        VoteQuestions.deleteVoteQuestion(this.questionId);
                        return;
                    }
                    this.qtitle = aAnswer.getProperty("qtitle").trim();
                    this.qtext = aAnswer.getProperty("qtext").trim();
                    this.opt1 = aAnswer.getProperty("opt1").trim();
                    this.opt2 = aAnswer.getProperty("opt2").trim();
                    this.opt3 = aAnswer.getProperty("opt3").trim();
                    this.opt4 = aAnswer.getProperty("opt4").trim();
                    String strMa = aAnswer.getProperty("ma");
                    String strPo = aAnswer.getProperty("po");
                    String strJk = aAnswer.getProperty("jk");
                    String strMr = aAnswer.getProperty("mr");
                    String strHots = aAnswer.getProperty("hots");
                    String strFreedom = aAnswer.getProperty("freedom");
                    String strVoteDay = aAnswer.getProperty("day");
                    String strVoteMonth = aAnswer.getProperty("month");
                    String strVoteYear = aAnswer.getProperty("year");
                    String strVoteHour = aAnswer.getProperty("hour");
                    String strVoteMins = aAnswer.getProperty("mins");
                    String strVoteLasts = aAnswer.getProperty("lasts");
                    int day = 1;
                    int month = 12;
                    int year = 2020;
                    int hour = 0;
                    int mins = 0;
                    int votelasts = 7;
                    String fail = "";
                    try {
                        day = Integer.parseInt(strVoteDay);
                        month = Integer.parseInt(strVoteMonth);
                        year = Integer.parseInt(strVoteYear);
                        hour = Integer.parseInt(strVoteHour);
                        mins = Integer.parseInt(strVoteMins);
                        votelasts = Integer.parseInt(strVoteLasts);
                    }
                    catch (NumberFormatException nfe) {
                        fail = "Invalid number.";
                    }
                    this.startDay = 0;
                    this.startMonth = 0;
                    this.startYear = 0;
                    try {
                        Date t = ft.parse(strVoteDay + "-" + (month + 1) + "-" + strVoteYear + " " + strVoteHour + ":" + strVoteMins);
                        Calendar calStart = Calendar.getInstance();
                        calStart.setTime(t);
                        this.startDay = calStart.get(5);
                        this.startMonth = calStart.get(2);
                        this.startYear = calStart.get(1);
                        this.voteStart = t.getTime();
                        calStart.add(5, votelasts);
                        this.voteEnd = calStart.getTimeInMillis();
                    }
                    catch (ParseException t) {
                        // empty catch block
                    }
                    this.ma = Boolean.parseBoolean(strMa);
                    this.po = Boolean.parseBoolean(strPo);
                    this.jk = Boolean.parseBoolean(strJk);
                    this.mr = Boolean.parseBoolean(strMr);
                    this.hots = Boolean.parseBoolean(strHots);
                    this.freedom = Boolean.parseBoolean(strFreedom);
                    this.serverIds = new HashSet<Integer>();
                    ServerEntry[] entries = Servers.getAllServers();
                    for (int x = 0; x < entries.length; ++x) {
                        String strServer = aAnswer.getProperty("s" + entries[x].getId());
                        if (strServer == null || !Boolean.parseBoolean(strServer)) continue;
                        this.serverIds.add(entries[x].getId());
                    }
                    if (fail.length() == 0) {
                        if (this.qtitle.length() < 5) {
                            fail = "Title not long enough";
                        } else if (this.qtext.length() < 10) {
                            fail = "Question is not long enough";
                        } else if (this.opt1.length() == 0 || this.opt2.length() == 0) {
                            fail = "Must have at least 2 options to vote for";
                        } else if (!(this.jk || this.mr || this.hots || this.freedom)) {
                            fail = "Must have at least one kingdom to be allowed to vote";
                        } else if (this.serverIds.isEmpty()) {
                            fail = "Must have at least one server selected";
                        } else if (this.voteStart == 0L) {
                            fail = "Start date is not a date";
                        } else if (day != this.startDay) {
                            fail = "Start day is not correct for month";
                        } else if (month != this.startMonth) {
                            fail = "Start month is not correct for year!";
                        } else if (year != this.startYear || this.startYear < 2014) {
                            fail = "Start year is wrong! or too early";
                        } else if (this.voteEnd <= this.voteStart) {
                            fail = "Vote duration is not long enough OR not a number";
                        } else if (this.voteEnd < System.currentTimeMillis()) {
                            fail = "Can't modify as voting has already ended";
                        } else if (this.voteStart < System.currentTimeMillis()) {
                            fail = "Can't modify as voting has already started";
                        }
                    }
                    if (fail.length() > 0) {
                        InGameVoteSetupQuestion igvsq = new InGameVoteSetupQuestion(this.getResponder(), this.title, "Error: " + fail, this.part, this.questionId, fail);
                        igvsq.setData(this.questionId, this.qtitle, this.qtext, this.opt1, this.opt2, this.opt3, this.opt4, this.ma, this.po, this.jk, this.mr, this.hots, this.freedom, this.voteStart, this.voteEnd, day, month, year, hour, mins, this.lasts, this.serverIds);
                        igvsq.sendQuestion();
                        break;
                    }
                    ArrayList<VoteServer> servers = new ArrayList<VoteServer>();
                    for (Integer serverId : this.serverIds) {
                        servers.add(new VoteServer(this.questionId, serverId));
                    }
                    VoteQuestion newVoteQuestion = new VoteQuestion(this.questionId, this.qtitle, this.qtext, this.opt1, this.opt2, this.opt3, this.opt4, this.ma, this.po, this.jk, this.mr, this.hots, this.freedom, this.voteStart, this.voteEnd, servers);
                    VoteQuestions.addVoteQuestion(newVoteQuestion, false);
                    break;
                }
                case 3: {
                    String remove = aAnswer.getProperty("remove");
                    if (remove == null || !Boolean.parseBoolean(remove)) break;
                    Calendar calNow = Calendar.getInstance();
                    VoteQuestions.closeVoteing(this.questionId, calNow.getTimeInMillis());
                    return;
                }
            }
        }
    }

    @Override
    public void sendQuestion() {
        this.width = 350;
        this.height = 500;
        StringBuilder buf = new StringBuilder();
        buf.append(this.getBmlHeader());
        if (this.getResponder().getPower() >= 2) {
            try {
                switch (this.part) {
                    case 0: {
                        String colour = "";
                        ServerEntry[] servers = Servers.getAllServers();
                        VoteQuestion[] vqs = VoteQuestions.getVoteQuestions();
                        buf.append("table{rows=\"" + (vqs.length + 1) + "\";cols=\"" + (4 + servers.length) + "\";label{text=\"\"};label{type=\"bold\";text=\"Question\"};label{type=\"bold\";text=\"Vote Start\"};label{type=\"bold\";text=\"Vote End\"};");
                        ServerEntry[] serverEntryArray = servers;
                        int n = serverEntryArray.length;
                        for (int i = 0; i < n; ++i) {
                            ServerEntry se = serverEntryArray[i];
                            buf.append("label{type=\"bold\";text=\"" + se.getAbbreviation() + "\"};");
                        }
                        for (int x = 0; x < vqs.length; ++x) {
                            colour = vqs[x].getSent() == 0 ? ";color=\"100,100,255\"" : (vqs[x].getVoteEnd() > System.currentTimeMillis() ? ";color=\"100,255,100\"" : "");
                            buf.append("radio{group=\"qid\";id=\"" + vqs[x].getQuestionId() + "\"};label{text=\"" + vqs[x].getQuestionTitle() + "\"" + colour + "};label{text=\"" + Tickets.convertTime(vqs[x].getVoteStart()) + "\"" + colour + "};label{text=\"" + Tickets.convertTime(vqs[x].getVoteEnd() - 1000L) + "\"" + colour + "};");
                            List<VoteServer> vsl = vqs[x].getServers();
                            for (ServerEntry se : servers) {
                                String sss = " o";
                                for (VoteServer vs : vsl) {
                                    if (vs.getServerId() != se.getId()) continue;
                                    sss = " X";
                                    break;
                                }
                                buf.append("label{text=\"" + sss + "\"};");
                            }
                        }
                        buf.append("radio{group=\"qid\";id=\"0\";selected=\"true\"};label{text=\"New\"};label{text=\"\"};label{text=\"\"};");
                        for (ServerEntry se : servers) {
                            buf.append("label{text=\"" + se.getAbbreviation() + "\"};");
                        }
                        buf.append("}");
                        this.width = 300 + servers.length * 20;
                        this.height = 140 + vqs.length * 16;
                        break;
                    }
                    case 1: 
                    case 2: 
                    case 3: {
                        buf.append(this.showQuestion());
                        break;
                    }
                    case 4: {
                        this.width = 500;
                        this.height = 300;
                        VoteQuestion vq = VoteQuestions.getVoteQuestion(this.questionId);
                        int rows = 3;
                        if (vq.getOption3Text().length() > 0) {
                            ++rows;
                        }
                        if (vq.getOption4Text().length() > 0) {
                            ++rows;
                        }
                        buf.append("table{rows=\"" + rows + "\";cols=\"3\";");
                        buf.append("label{type=\"bold\";text=\"Options\"}label{type=\"bold\";text=\"Count\"}label{type=\"bold\";text=\"Percentage\"}");
                        buf.append(this.showSummaryRow(vq.getOption1Text(), vq.getOption1Count(), vq.getVoteCount()));
                        buf.append(this.showSummaryRow(vq.getOption2Text(), vq.getOption2Count(), vq.getVoteCount()));
                        buf.append(this.showSummaryRow(vq.getOption3Text(), vq.getOption3Count(), vq.getVoteCount()));
                        buf.append(this.showSummaryRow(vq.getOption4Text(), vq.getOption4Count(), vq.getVoteCount()));
                        buf.append("label{type=\"bold\";text=\"Total Players who voted\"};label{type=\"bold\";text=\"" + vq.getVoteCount() + "\"};label{text=\"\"}");
                        buf.append("}");
                        buf.append("text{text=\"\"}");
                        break;
                    }
                }
                buf.append(this.createAnswerButton2());
                this.getResponder().getCommunicator().sendBml(this.width, this.height, true, true, buf.toString(), 200, 200, 200, this.title);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, e.getMessage(), e);
                this.getResponder().getCommunicator().sendNormalServerMessage("Exception:" + e.getMessage());
            }
        }
    }

    private String showSummaryRow(String text, int count, int total) {
        if (text.length() == 0) {
            return "";
        }
        int perc = -1;
        String percText = "NA";
        String colour = "";
        if (total > 0) {
            perc = count * 100 / total;
            percText = "(" + perc + "%)";
            colour = perc >= 75 ? ";color=\"100,255,100\"" : (perc <= 25 ? ";color=\"255,100,100\"" : ";color=\"100,100,255\"");
        }
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"" + text + "\"" + colour + "}label{text=\"" + count + "\"" + colour + "}label{text=\"" + percText + "\"" + colour + "};");
        return buf.toString();
    }

    private String showQuestion() {
        Calendar calStart = Calendar.getInstance();
        Calendar calEnd = Calendar.getInstance();
        VoteQuestion vq = null;
        String disabled = "";
        String noamend = "";
        String remove = "";
        String totalVoted = "";
        if (this.part == 1) {
            if (this.failReason.length() == 0) {
                this.qtitle = "";
                this.qtext = "";
                this.opt1 = "";
                this.opt2 = "";
                this.opt3 = "";
                this.opt4 = "";
                this.ma = false;
                this.po = false;
                this.jk = true;
                this.mr = true;
                this.hots = true;
                this.freedom = true;
                calStart.add(5, 1);
                calEnd.add(5, 8);
                this.startDay = calStart.get(5);
                this.startMonth = calStart.get(2);
                this.startYear = calStart.get(1);
                this.startHour = 0;
                this.startMins = 0;
                this.lasts = (int)InGameVoteSetupQuestion.daysBetween(calStart, calEnd);
            }
        } else if (this.failReason.length() == 0) {
            vq = VoteQuestions.getVoteQuestion(this.questionId);
            calStart.setTimeInMillis(vq.getVoteStart());
            calEnd.setTimeInMillis(vq.getVoteEnd());
            this.qtitle = vq.getQuestionTitle();
            this.qtext = vq.getQuestionText();
            this.opt1 = vq.getOption1Text();
            this.opt2 = vq.getOption2Text();
            this.opt3 = vq.getOption3Text();
            this.opt4 = vq.getOption4Text();
            this.ma = vq.isAllowMultiple();
            this.po = vq.isPremOnly();
            this.jk = vq.isJK();
            this.mr = vq.isMR();
            this.hots = vq.isHots();
            this.freedom = vq.isFreedom();
            this.startDay = calStart.get(5);
            this.startMonth = calStart.get(2);
            this.startYear = calStart.get(1);
            this.startHour = calStart.get(11);
            this.startMins = calStart.get(12);
            this.lasts = (int)InGameVoteSetupQuestion.daysBetween(calStart, calEnd);
            if (this.part == 3) {
                remove = "checkbox{id=\"remove\";text=\"Close Poll?\"};";
                int total = 0;
                int count1 = 0;
                int count2 = 0;
                int count3 = 0;
                int count4 = 0;
                for (PlayerVote pv : PlayerVotes.getPlayerVotesByQuestion(vq.getQuestionId())) {
                    total = (short)(total + 1);
                    if (pv.getOption1()) {
                        count1 = (short)(count1 + 1);
                    }
                    if (pv.getOption2()) {
                        count2 = (short)(count2 + 1);
                    }
                    if (pv.getOption3()) {
                        count3 = (short)(count3 + 1);
                    }
                    if (!pv.getOption4()) continue;
                    count4 = (short)(count4 + 1);
                }
                totalVoted = " (" + total + " voted so far).";
                if (total > 0) {
                    if (this.opt1.length() > 0) {
                        this.opt1 = this.opt1 + " (" + count1 + ":" + count1 * 100 / total + "%)";
                    }
                    if (this.opt2.length() > 0) {
                        this.opt2 = this.opt2 + " (" + count2 + ":" + count2 * 100 / total + "%)";
                    }
                    if (this.opt3.length() > 0) {
                        this.opt3 = this.opt3 + " (" + count3 + ":" + count3 * 100 / total + "%)";
                    }
                    if (this.opt4.length() > 0) {
                        this.opt4 = this.opt4 + " (" + count4 + ":" + count4 * 100 / total + "%)";
                    }
                }
            } else {
                remove = "checkbox{id=\"remove\";text=\"Delete Poll?\"};";
            }
        }
        if (this.part == 3) {
            disabled = ";enabled=\"false\"";
            noamend = ";label{type=\"bolditalic\";color=\"255,100,100\";text=\"  In progress so cant amend" + totalVoted + "\"}";
        }
        StringBuilder buf = new StringBuilder();
        buf.append("harray{label{text=\"Question:  Title->\"};input{id=\"qtitle\";maxchars=\"40\";text=\"" + this.qtitle + "\"" + disabled + "}" + noamend + "}");
        buf.append("input{id=\"qtext\";maxlines=\"3\";maxchars=\"200\";text=\"" + this.qtext + "\"" + disabled + "}");
        buf.append("harray{label{text=\"Option 1 [\"};label{color=\"100,100,255\";text=\"Mandatory\"};label{text=\"]:\"};input{id=\"opt1\";maxchars=\"50\";text=\"" + this.opt1 + "\"" + disabled + "}}");
        buf.append("harray{label{text=\"Option 2 [\"};label{color=\"100,100,255\";text=\"Mandatory\"};label{text=\"]:\"};input{id=\"opt2\";maxchars=\"50\";text=\"" + this.opt2 + "\"" + disabled + "}}");
        buf.append("harray{label{text=\"Option 3 [ Optional ]  :\"};input{id=\"opt3\";maxchars=\"50\";text=\"" + this.opt3 + "\"" + disabled + "}}");
        buf.append("harray{label{text=\"Option 4 [ Optional ]  :\"};input{id=\"opt4\";maxchars=\"50\";text=\"" + this.opt4 + "\"" + disabled + "}}");
        buf.append("harray{label{text=\"Multiple answers allowed?\"};checkbox{id=\"ma\";selected=\"" + this.ma + "\"" + disabled + "};label{text=\"   Premium Only?\"};checkbox{id=\"po\";selected=\"" + this.po + "\"" + disabled + "}}");
        buf.append("harray{label{type=\"bold\";text=\"By Kingdoms: \"}label{text=\"JK?\"};checkbox{id=\"jk\";selected=\"" + this.jk + "\"" + disabled + "}label{text=\"MR?\"};checkbox{id=\"mr\";selected=\"" + this.mr + "\"" + disabled + "}label{text=\"HOTS?\"};checkbox{id=\"hots\";selected=\"" + this.hots + "\"" + disabled + "}label{text=\"Freedom?\"};checkbox{id=\"freedom\";selected=\"" + this.freedom + "\"" + disabled + "}}");
        buf.append("label{type=\"bold\";text=\"On Server(s) - must have at least one\"}");
        ServerEntry[] entries = Servers.getAllServers();
        for (int x = 0; x < entries.length; ++x) {
            String selected = "";
            if (vq != null) {
                for (VoteServer vs : vq.getServers()) {
                    if (vs.getServerId() != entries[x].getId()) continue;
                    selected = ";selected=\"true\"";
                    break;
                }
            } else {
                for (Integer sid : this.serverIds) {
                    if (sid.intValue() != entries[x].getId()) continue;
                    selected = ";selected=\"true\"";
                    break;
                }
            }
            if (entries[x].isAvailable(this.getResponder().getPower(), this.getResponder().isPaying())) {
                buf.append("harray{label{color=\"100,255,100\";text=\"" + entries[x].getName() + "?\"}checkbox{id=\"s" + entries[x].getId() + "\"" + selected + disabled + "}}");
                continue;
            }
            buf.append("harray{label{color=\"255,100,100\";text=\"" + entries[x].getName() + "?\"}checkbox{id=\"s" + entries[x].getId() + "\"" + selected + disabled + "}}");
        }
        String textMonth = new DateFormatSymbols().getMonths()[this.startMonth];
        String txtMonth = textMonth.substring(0, 3);
        String strMonth = "";
        String strHour = "";
        String strMins = "";
        if (this.part == 3) {
            strMonth = "input{id=\"month\";maxchars=\"3\";text=\"" + txtMonth + "\";enabled=\"false\"}";
            strHour = "input{id=\"hour\";maxchars=\"2\";text=\"" + (this.startHour < 10 ? "0" : "") + this.startHour + "\";enabled=\"false\"}";
            strMins = "input{id=\"mins\";maxchars=\"2\";text=\"" + (this.startMins < 10 ? "0" : "") + this.startMins + "\";enabled=\"false\"}";
        } else {
            strMonth = "dropdown{id=\"month\";default=\"" + this.startMonth + "\";options=\"Jan,Feb,Mar,Apr,May,Jun,Jul,Aug,Sep,Oct,Nov,Dec\"}";
            strHour = "dropdown{id=\"hour\";default=\"" + this.startHour + "\";options=\"00,01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,16,17,18,19,20,21,22,23\"}";
            strMins = "dropdown{id=\"mins\";default=\"" + this.startMins + "\";options=\"00,01,02,03,04,05,06,07,08,09,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59\"}";
        }
        buf.append("harray{label{text=\"Start:\"};input{id=\"day\";maxchars=\"2\";text=\"" + this.startDay + "\"" + disabled + "};label{text=\"/\"};" + strMonth + ";label{text=\"/\"};input{id=\"year\";maxchars=\"4\";text=\"" + this.startYear + "\"" + disabled + "};label{text=\" \"};" + strHour + "label{text=\":\"};" + strMins + "}");
        buf.append("harray{label{text=\"Duration (in days):\"};input{id=\"lasts\";maxchars=\"2\";text=\"" + this.lasts + "\"" + disabled + "}}");
        if (remove.length() > 0) {
            buf.append(remove);
        }
        this.width = 400;
        this.height = 380 + entries.length * 16;
        return buf.toString();
    }

    public void setData(int aquestionId, String aqtitle, String aqtext, String aopt1, String aopt2, String aopt3, String aopt4, boolean ama, boolean apo, boolean ajk, boolean amr, boolean ahots, boolean afreedom, long avoteStart, long avoteEnd, int avoteDay, int avoteMonth, int avoteYear, int aVoteHour, int aVoteMins, int alasts, Set<Integer> aserverIds) {
        this.questionId = aquestionId;
        this.qtitle = aqtitle;
        this.qtext = aqtext;
        this.opt1 = aopt1;
        this.opt2 = aopt2;
        this.opt3 = aopt3;
        this.opt4 = aopt4;
        this.ma = ama;
        this.po = apo;
        this.jk = ajk;
        this.mr = amr;
        this.hots = ahots;
        this.freedom = afreedom;
        this.voteStart = avoteStart;
        this.voteEnd = avoteEnd;
        this.startDay = avoteDay;
        this.startMonth = avoteMonth;
        this.startYear = avoteYear;
        this.startHour = aVoteHour;
        this.startMins = aVoteMins;
        this.lasts = alasts;
        this.serverIds = aserverIds;
    }

    private static long daysBetween(Calendar startDate, Calendar endDate) {
        Calendar date = (Calendar)startDate.clone();
        long daysBetween = 0L;
        while (date.before(endDate)) {
            date.add(5, 1);
            ++daysBetween;
        }
        return daysBetween;
    }
}

