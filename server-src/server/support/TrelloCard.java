/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.support;

public final class TrelloCard {
    private final String trelloBoardId;
    private final String trelloListId;
    private final String trelloCardTitle;
    private final String trelloCardDescription;
    private final String trelloLabel;

    public TrelloCard(String boardId, String listId, String title, String description, String label) {
        this.trelloBoardId = boardId;
        this.trelloListId = listId;
        this.trelloCardTitle = title;
        this.trelloCardDescription = description;
        this.trelloLabel = label;
    }

    public String getBoardId() {
        return this.trelloBoardId;
    }

    public String getListId() {
        return this.trelloListId;
    }

    public String getTitle() {
        return this.trelloCardTitle;
    }

    public String getDescription() {
        return this.trelloCardDescription;
    }

    public String getLabel() {
        return this.trelloLabel;
    }
}

