/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.website.news;

import com.wurmonline.website.Block;
import com.wurmonline.website.LoginInfo;
import com.wurmonline.website.Section;
import com.wurmonline.website.news.News;
import com.wurmonline.website.news.NewsBlock;
import com.wurmonline.website.news.SubmitNewsBlock;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

public class NewsSection
extends Section {
    private SubmitNewsBlock submitBlock = new SubmitNewsBlock();
    private List<NewsBlock> news = new ArrayList<NewsBlock>();

    @Override
    public String getName() {
        return "News";
    }

    @Override
    public String getId() {
        return "news";
    }

    @Override
    public List<Block> getBlocks(HttpServletRequest req, LoginInfo loginInfo) {
        ArrayList<Block> list = new ArrayList<Block>();
        if ("delete".equals(req.getParameter("action")) && loginInfo != null && loginInfo.isAdmin()) {
            this.delete(req.getParameter("id"));
        }
        list.addAll(this.news);
        if (loginInfo != null && loginInfo.isAdmin()) {
            list.add(this.submitBlock);
        }
        return list;
    }

    private void delete(String id) {
    }

    @Override
    public void handlePost(HttpServletRequest req, LoginInfo loginInfo) {
        if (loginInfo != null && loginInfo.isAdmin()) {
            String title = req.getParameter("title");
            String text = req.getParameter("text");
            text = text.replaceAll("\r\n", "<br>");
            text = text.replaceAll("\r", "<br>");
            text = text.replaceAll("\n", "<br>");
            this.news.add(new NewsBlock(new News(title, text, loginInfo.getName())));
        }
    }
}

