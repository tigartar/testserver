/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.support;

import com.wurmonline.server.Constants;

public final class TrelloURL {
    private static final String TRELLO = "https://api.trello.com/1/";
    public static final String BOARD_URL = "https://api.trello.com/1/boards/{0}";
    public static final String BOARD_ACTIONS_URL = "https://api.trello.com/1/boards/{0}/actions";
    public static final String ACTION_URL = "https://api.trello.com/1/actions/{0}";
    public static final String ORG_URL = "https://api.trello.com/1/organizations/{0}";
    public static final String ORG_BOARDS_URL = "https://api.trello.com/1/organizations/{0}/boards";
    public static final String ORG_ACTIONS_URL = "https://api.trello.com/1/organizations/{0}/actions";
    public static final String MEMBER_URL = "https://api.trello.com/1/members/{0}";
    public static final String MEMBER_BOARDS_URL = "https://api.trello.com/1/members/{0}/boards";
    public static final String CARD_URL = "https://api.trello.com/1/cards/{0}";
    public static final String LIST_URL = "https://api.trello.com/1/lists/{0}";
    public static final String NOTIFICATION_URL = "https://api.trello.com/1/notifications/{0}";
    public static final String CHECKLIST_URL = "https://api.trello.com/1/checklists/{0}";
    public static final String TYPE_URL = "https://api.trello.com/1/types/{0}";
    public static final String ACTION_BOARD_URL = "https://api.trello.com/1/actions/{0}/board";
    public static final String ACTION_CARD_URL = "https://api.trello.com/1/actions/{0}/card";
    public static final String ACTION_MEMBER_URL = "https://api.trello.com/1/actions/{0}/member";
    public static final String ACTION_LIST_URL = "https://api.trello.com/1/actions/{0}/list";
    public static final String ACTION_MEMBERCREATOR_URL = "https://api.trello.com/1/actions/{0}/memberCreator";
    public static final String ACTION_ORG_URL = "https://api.trello.com/1/actions/{0}/organization";
    public static final String BOARD_CARDS_URL = "https://api.trello.com/1/boards/{0}/cards";
    public static final String BOARD_CHECKLISTS_URL = "https://api.trello.com/1/boards/{0}/checklists";
    public static final String BOARD_LISTS_URL = "https://api.trello.com/1/boards/{0}/lists";
    public static final String BOARD_MEMBERS_URL = "https://api.trello.com/1/boards/{0}/members";
    public static final String BOARD_MEMBERS_INVITED_URL = "https://api.trello.com/1/boards/{0}/membersInvited";
    public static final String BOARD_PREFS_URL = "https://api.trello.com/1/boards/{0}/myPrefs";
    public static final String BOARD_ORGANIZAION_URL = "https://api.trello.com/1/boards/{0}/organization";
    public static final String BOARD_CLOSED_URL = "https://api.trello.com/1/boards/{0}/closed";
    public static final String BOARD_PUT_DESC_URL = "https://api.trello.com/1/boards/{0}/desc";
    public static final String CARD_ACTION_URL = "https://api.trello.com/1/cards/{0}/actions";
    public static final String CARD_ATTACHEMENT_URL = "https://api.trello.com/1/cards/{0}/attachments";
    public static final String CARD_BOARD_URL = "https://api.trello.com/1/cards/{0}/board";
    public static final String CARD_CHECK_ITEM_STATES_URL = "https://api.trello.com/1/cards/{0}/checkItemStates";
    public static final String CARD_CHECKLISTS_URL = "https://api.trello.com/1/cards/{0}/checklists";
    public static final String CARD_COMMENT_URL = "https://api.trello.com/1/cards/{0}/actions/comments";
    public static final String CARD_LIST_URL = "https://api.trello.com/1/cards/{0}/list";
    public static final String CARD_MEMBERS_URL = "https://api.trello.com/1/cards/{0}/members";
    public static final String CARD_POST_URL = "https://api.trello.com/1/cards";
    public static final String CARD_PUT_URL = "https://api.trello.com/1/cards/{0}";
    public static final String CARD_PUT_CHECKITEM_STATE_URL = "https://api.trello.com/1/cards/{0}/checklist/{1}/checkItem/{2}/state";
    public static final String CARD_PUT_CLOSED_URL = "https://api.trello.com/1/cards/{0}/closed";
    public static final String CARD_PUT_DESC_URL = "https://api.trello.com/1/cards/{0}/desc";
    public static final String CARD_PUT_IDLIST_URL = "https://api.trello.com/1/cards/{0}/idList";
    public static final String CARD_PUT_NAME_URL = "https://api.trello.com/1/cards/{0}/name";
    public static final String CARD_PUT_LABEL_URL = "https://api.trello.com/1/cards/{0}/labels";
    public static final String LIST_ACTIONS_URL = "https://api.trello.com/1/lists/{0}/action";
    public static final String LIST_BOARD_URL = "https://api.trello.com/1/lists/{0}/board";
    public static final String LIST_CARDS_URL = "https://api.trello.com/1/lists/{0}/cards";
    public static final String MEMBER_ACTIONS_URL = "https://api.trello.com/1/members/{0}/actions";
    public static final String MEMBER_BOARD_INVITED_URL = "https://api.trello.com/1/members/{0}/boardInvited";
    public static final String MEMBER_CARDS_URL = "https://api.trello.com/1/members/{0}/cards";
    public static final String MEMBER_NOTIFICTIONS_URL = "https://api.trello.com/1/members/{0}/notifications";
    public static final String MEMBER_ORG_URL = "https://api.trello.com/1/members/{0}/organizations";
    public static final String MEMBER_ORG_INVITED_URL = "https://api.trello.com/1/members/{0}/organizationsInvited";
    public static final String NOTIFICATION_ACTIONS_URL = "https://api.trello.com/1/notifications/{0}/actions";
    public static final String NOTIFICATION_BOARDS_URL = "https://api.trello.com/1/notifications/{0}/boards";
    public static final String NOTIFICATION_MEMBERS_URL = "https://api.trello.com/1/notifications/{0}/members";
    public static final String NOTIFICATION_CARDS_URL = "https://api.trello.com/1/notifications/{0}/cards";
    public static final String NOTIFICATION_LIST_URL = "https://api.trello.com/1/notifications/{0}/list";
    public static final String NOTIFICATION_CREATOR_URL = "https://api.trello.com/1/notifications/{0}/membersCreator";
    public static final String NOTIFICATION_ORG_URL = "https://api.trello.com/1/notifications/{0}/organization";
    public static final String CHECKLIST_BOARD_URL = "https://api.trello.com/1/checklists/{0}/board";
    public static final String CHECKLIST_CHECKITEMS_URL = "https://api.trello.com/1/checklists/{0}/checkItems";
    public static final String CHECKLIST_CARDS_URL = "https://api.trello.com/1/checklists/{0}/cards";
    public static final String TOKENS_URL = "https://api.trello.com/1/tokens/{0}";
    public static final String TOKENS_MEMBER_URL = "https://api.trello.com/1/tokens/{0}/member";
    public static final String ORG_MEMBERS_URL = "https://api.trello.com/1/organizations/{0}/members";
    private static final String PATH_PARAM_ARG_PREFIX = "\\{";
    private static final String PATH_PARAM_ARG_SUFFIX = "\\}";
    private static final String KEY_QUERY_PARAM = "?key=";
    private static final String TOKEN_QUERY_PARAM = "&token=";
    private static final String FILTER_QUERY_PARAM = "&filter=";
    private final String[] pathParams;
    private final String url;
    private final String apiKey = Constants.trelloApiKey;
    private String token = Constants.trelloToken;
    private String[] filters = null;

    public static TrelloURL create(String url, String ... pathParams) {
        return new TrelloURL(url, pathParams);
    }

    public static String make(String url, String ... pathParams) {
        return new TrelloURL(url, pathParams).build();
    }

    private TrelloURL(String aUrl, String ... aPathParams) {
        this.url = aUrl;
        this.pathParams = aPathParams;
    }

    public TrelloURL filter(String ... aFilters) {
        this.filters = TrelloURL.isArrayEmpty(aFilters) ? null : aFilters;
        return this;
    }

    public String build() {
        if (this.apiKey == null || this.url == null) {
            throw new NullPointerException("Cannot build trello URL: API key and URL must be set");
        }
        return this.createUrlWithPathParams() + this.createAuthQueryString() + this.createFilterQuery();
    }

    private String createFilterQuery() {
        String filterStr = "";
        if (this.filters != null) {
            StringBuilder sb = new StringBuilder(FILTER_QUERY_PARAM);
            for (int i = 0; i < this.filters.length; ++i) {
                sb.append(i > 0 ? "," : "").append(this.filters[i]);
            }
            filterStr = sb.toString();
        }
        return filterStr;
    }

    private String createAuthQueryString() {
        StringBuilder sb = new StringBuilder(KEY_QUERY_PARAM).append(this.apiKey);
        if (this.token != null) {
            sb.append(TOKEN_QUERY_PARAM).append(this.token);
        }
        return sb.toString();
    }

    private String createUrlWithPathParams() {
        if (this.pathParams == null || this.pathParams.length == 0) {
            return this.url;
        }
        String compiledUrl = this.url;
        for (int i = 0; i < this.pathParams.length; ++i) {
            compiledUrl = compiledUrl.replaceAll(PATH_PARAM_ARG_PREFIX + i + PATH_PARAM_ARG_SUFFIX, this.pathParams[i]);
        }
        return compiledUrl;
    }

    private static boolean isArrayEmpty(String[] arr) {
        return arr == null || arr.length == 0;
    }
}

