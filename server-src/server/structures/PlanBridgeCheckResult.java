/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.structures;

public class PlanBridgeCheckResult {
    final boolean failed;
    String pMsg = "";
    String qMsg = "";

    public PlanBridgeCheckResult(boolean fail, String PMsg, String QMsg) {
        this.failed = fail;
        this.pMsg = PMsg;
        this.qMsg = QMsg;
    }

    public PlanBridgeCheckResult(boolean fail) {
        this.failed = fail;
    }

    public boolean failed() {
        return this.failed;
    }

    public String pMsg() {
        return this.pMsg;
    }

    public String qMsg() {
        return this.qMsg;
    }
}

