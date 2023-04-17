/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.webinterface.WcGlobalAlarmMessage;
import java.util.Properties;

public final class AlertServerMessageQuestion
extends Question {
    public AlertServerMessageQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget) {
        super(aResponder, aTitle, aQuestion, 45, aTarget);
    }

    @Override
    public void answer(Properties answers) {
        String time1 = answers.getProperty("alt1");
        if (time1 == null || time1.length() == 0) {
            Server.timeBetweenAlertMess1 = Long.MAX_VALUE;
        } else {
            try {
                long seconds = Long.parseLong(time1);
                if (seconds <= 0L) {
                    Server.timeBetweenAlertMess1 = Long.MAX_VALUE;
                    Server.lastAlertMess1 = Long.MAX_VALUE;
                } else {
                    Server.timeBetweenAlertMess1 = Math.max(10L, seconds) * 1000L;
                    Server.lastAlertMess1 = 0L;
                }
            }
            catch (Exception e) {
                this.getResponder().getCommunicator().sendAlertServerMessage(time1 + " is not a number.");
            }
        }
        String time2 = answers.getProperty("alt2");
        if (time2 == null || time2.length() == 0) {
            Server.timeBetweenAlertMess2 = Long.MAX_VALUE;
        } else {
            try {
                long seconds = Long.parseLong(time2);
                if (seconds <= 0L) {
                    Server.timeBetweenAlertMess2 = Long.MAX_VALUE;
                    Server.lastAlertMess2 = Long.MAX_VALUE;
                } else {
                    Server.timeBetweenAlertMess2 = Math.max(10L, seconds) * 1000L;
                    Server.lastAlertMess2 = 0L;
                }
            }
            catch (Exception e) {
                this.getResponder().getCommunicator().sendAlertServerMessage(time2 + " is not a number.");
            }
        }
        String time3 = answers.getProperty("alt3");
        if (time3 == null || time3.length() == 0) {
            Server.timeBetweenAlertMess3 = Long.MAX_VALUE;
        } else {
            try {
                long seconds = Long.parseLong(time3);
                if (seconds <= 0L) {
                    Server.timeBetweenAlertMess3 = Long.MAX_VALUE;
                    Server.lastAlertMess3 = Long.MAX_VALUE;
                } else {
                    Server.timeBetweenAlertMess3 = Math.max(10L, seconds) * 1000L;
                    Server.lastAlertMess3 = 0L;
                }
            }
            catch (Exception e) {
                this.getResponder().getCommunicator().sendAlertServerMessage(time3 + " is not a number.");
            }
        }
        String time4 = answers.getProperty("alt4");
        if (time4 == null || time4.length() == 0) {
            Server.timeBetweenAlertMess4 = Long.MAX_VALUE;
        } else {
            try {
                long seconds = Long.parseLong(time3);
                if (seconds <= 0L) {
                    Server.timeBetweenAlertMess4 = Long.MAX_VALUE;
                    Server.lastAlertMess4 = Long.MAX_VALUE;
                } else {
                    Server.timeBetweenAlertMess4 = Math.max(10L, seconds) * 1000L;
                    Server.lastAlertMess4 = 0L;
                }
            }
            catch (Exception e) {
                this.getResponder().getCommunicator().sendAlertServerMessage(time4 + " is not a number.");
            }
        }
        String mess1 = answers.getProperty("alm1");
        if (mess1 == null || mess1.length() == 0) {
            if (Server.alertMessage1.length() > 0) {
                this.getResponder().getCommunicator().sendSafeServerMessage("Reset message 1.");
            }
            Server.alertMessage1 = "";
            Server.timeBetweenAlertMess1 = Long.MAX_VALUE;
            Server.lastAlertMess1 = Long.MAX_VALUE;
        } else {
            String msg1;
            Server.alertMessage1 = msg1 = mess1.replaceAll("\"", "");
            this.getResponder().getCommunicator().sendSafeServerMessage("Set message 1.");
        }
        String mess2 = answers.getProperty("alm2");
        if (mess2 == null || mess2.length() == 0) {
            if (Server.alertMessage2.length() > 0) {
                this.getResponder().getCommunicator().sendSafeServerMessage("Reset message 2.");
            }
            Server.alertMessage2 = "";
            Server.timeBetweenAlertMess2 = Long.MAX_VALUE;
            Server.lastAlertMess2 = Long.MAX_VALUE;
        } else {
            String msg2;
            Server.alertMessage2 = msg2 = mess2.replaceAll("\"", "");
            this.getResponder().getCommunicator().sendSafeServerMessage("Set message 2.");
        }
        String mess3 = answers.getProperty("alm3");
        if (mess3 == null || mess3.length() == 0) {
            if (Server.alertMessage3.length() > 0) {
                this.getResponder().getCommunicator().sendSafeServerMessage("Reset global alert 3.");
            }
            Server.alertMessage3 = "";
            Server.timeBetweenAlertMess3 = Long.MAX_VALUE;
            Server.lastAlertMess3 = Long.MAX_VALUE;
        } else {
            String msg3;
            Server.alertMessage3 = msg3 = mess3.replaceAll("\"", "");
            this.getResponder().getCommunicator().sendSafeServerMessage("Set message 3.");
        }
        String mess4 = answers.getProperty("alm4");
        if (mess4 == null || mess4.length() == 0) {
            if (Server.alertMessage4.length() > 0) {
                this.getResponder().getCommunicator().sendSafeServerMessage("Reset global alert 4.");
            }
            Server.alertMessage4 = "";
            Server.timeBetweenAlertMess4 = Long.MAX_VALUE;
            Server.lastAlertMess4 = Long.MAX_VALUE;
        } else {
            String msg4;
            Server.alertMessage4 = msg4 = mess4.replaceAll("\"", "");
            this.getResponder().getCommunicator().sendSafeServerMessage("Set message 4.");
        }
        WcGlobalAlarmMessage wgam = new WcGlobalAlarmMessage(Server.alertMessage3, Server.timeBetweenAlertMess3, Server.alertMessage4, Server.timeBetweenAlertMess4);
        if (Servers.isThisLoginServer()) {
            wgam.sendFromLoginServer();
        } else {
            wgam.sendToLoginServer();
        }
    }

    @Override
    public void sendQuestion() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getBmlHeader());
        sb.append("text{text='You may have 3 alert messages going, set at various intervals.'}");
        sb.append("text{text='If you omit the text or the time, or set time in seconds to 0 or less the message will not be displayed.'}");
        sb.append("text{text='The minimum number of seconds between alerts is 10.'}");
        sb.append("label{text='Alert message 1:'};input{id='alm1';text=\"" + Server.alertMessage1 + "\"}");
        sb.append("label{text='Seconds between polls:'};input{id='alt1';text='" + (Server.timeBetweenAlertMess1 == Long.MAX_VALUE ? 180L : Server.timeBetweenAlertMess1 / 1000L) + "'}");
        sb.append("label{text='Alert message 2:'};input{id='alm2';text=\"" + Server.alertMessage2 + "\"}");
        sb.append("label{text='Seconds between polls:'};input{id='alt2';text='" + (Server.timeBetweenAlertMess2 == Long.MAX_VALUE ? 180L : Server.timeBetweenAlertMess2 / 1000L) + "'}");
        sb.append("label{text=\"Global Alert message 3:\"};input{id='alm3';text=\"" + Server.alertMessage3 + "\"}");
        sb.append("label{text='Seconds between polls:'};input{id='alt3';text='" + (Server.timeBetweenAlertMess3 == Long.MAX_VALUE ? 180L : Server.timeBetweenAlertMess3 / 1000L) + "'}");
        sb.append("label{text=\"Global Alert message 4:\"};input{id='alm4';text=\"" + Server.alertMessage4 + "\"}");
        sb.append("label{text='Seconds between polls:'};input{id=\"alt4\";text='" + (Server.timeBetweenAlertMess4 == Long.MAX_VALUE ? 180L : Server.timeBetweenAlertMess4 / 1000L) + "'}");
        sb.append(this.createAnswerButton2());
        this.getResponder().getCommunicator().sendBml(300, 350, true, true, sb.toString(), 200, 200, 200, this.title);
    }
}

