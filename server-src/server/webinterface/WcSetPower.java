/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.webinterface;

import com.wurmonline.server.WurmId;
import com.wurmonline.server.webinterface.WebCommand;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WcSetPower
extends WebCommand {
    private static final Logger logger = Logger.getLogger(WcSetPower.class.getName());
    private String playerName;
    private int newPower;
    private String senderName;
    private int senderPower;
    private String response;

    public WcSetPower(String playerName, int newPower, String senderName, int senderPower, String response) {
        this();
        this.playerName = playerName;
        this.newPower = newPower;
        this.senderName = senderName;
        this.senderPower = senderPower;
        this.response = response;
    }

    WcSetPower(WcSetPower copy) {
        this();
        this.playerName = copy.playerName;
        this.newPower = copy.newPower;
        this.senderName = copy.senderName;
        this.senderPower = copy.senderPower;
        this.response = copy.response;
    }

    WcSetPower() {
        super(WurmId.getNextWCCommandId(), (short)33);
    }

    public WcSetPower(long aId, byte[] _data) {
        super(aId, (short)33, _data);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    byte[] encode() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = null;
        byte[] byteArr = null;
        try {
            dos = new DataOutputStream(bos);
            dos.writeUTF(this.playerName);
            dos.writeInt(this.newPower);
            dos.writeUTF(this.senderName);
            dos.writeInt(this.senderPower);
            dos.writeUTF(this.response);
            dos.flush();
            dos.close();
        }
        catch (Exception ex) {
            try {
                logger.log(Level.WARNING, ex.getMessage(), ex);
            }
            catch (Throwable throwable) {
                StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
                byteArr = bos.toByteArray();
                StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
                this.setData(byteArr);
                throw throwable;
            }
            StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
            byteArr = bos.toByteArray();
            StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
            this.setData(byteArr);
        }
        StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
        byteArr = bos.toByteArray();
        StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
        this.setData(byteArr);
        return byteArr;
    }

    @Override
    public boolean autoForward() {
        return false;
    }

    @Override
    public void execute() {
        new Thread(){

            /*
             * Exception decompiling
             */
            @Override
            public void run() {
                /*
                 * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
                 * 
                 * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [0[TRYBLOCK], 7[CATCHBLOCK]], but top level block is 2[TRYBLOCK]
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
                 *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
                 *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
                 *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
                 *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseInnerClassesPass1(ClassFile.java:923)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1035)
                 *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
                 *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
                 *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
                 *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
                 *     at org.benf.cfr.reader.Main.main(Main.java:54)
                 */
                throw new IllegalStateException("Decompilation failed");
            }

            private String getPowerName(int power) {
                String powString = "normal adventurer";
                if (WcSetPower.this.newPower == 1) {
                    powString = "hero";
                } else if (WcSetPower.this.newPower == 2) {
                    powString = "demigod";
                } else if (WcSetPower.this.newPower == 3) {
                    powString = "high god";
                } else if (WcSetPower.this.newPower == 4) {
                    powString = "arch angel";
                } else if (WcSetPower.this.newPower == 5) {
                    powString = "implementor";
                }
                return powString;
            }
        }.start();
    }

    static /* synthetic */ String access$002(WcSetPower x0, String x1) {
        x0.playerName = x1;
        return x0.playerName;
    }

    static /* synthetic */ int access$102(WcSetPower x0, int x1) {
        x0.newPower = x1;
        return x0.newPower;
    }

    static /* synthetic */ String access$202(WcSetPower x0, String x1) {
        x0.senderName = x1;
        return x0.senderName;
    }

    static /* synthetic */ int access$302(WcSetPower x0, int x1) {
        x0.senderPower = x1;
        return x0.senderPower;
    }

    static /* synthetic */ String access$402(WcSetPower x0, String x1) {
        x0.response = x1;
        return x0.response;
    }

    static /* synthetic */ String access$400(WcSetPower x0) {
        return x0.response;
    }

    static /* synthetic */ String access$200(WcSetPower x0) {
        return x0.senderName;
    }

    static /* synthetic */ String access$000(WcSetPower x0) {
        return x0.playerName;
    }

    static /* synthetic */ int access$300(WcSetPower x0) {
        return x0.senderPower;
    }

    static /* synthetic */ Logger access$500() {
        return logger;
    }
}

