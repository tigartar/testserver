/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.website;

import com.wurmonline.website.LoginInfo;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;

public abstract class Block {
    public abstract void write(HttpServletRequest var1, PrintWriter var2, LoginInfo var3);
}

