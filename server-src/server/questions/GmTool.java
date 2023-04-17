/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.mesh.BushData;
import com.wurmonline.mesh.FoliageAge;
import com.wurmonline.mesh.GrassData;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.TreeData;
import com.wurmonline.server.Features;
import com.wurmonline.server.HistoryEvent;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.banks.Bank;
import com.wurmonline.server.banks.BankSlot;
import com.wurmonline.server.banks.BankUnavailableException;
import com.wurmonline.server.banks.Banks;
import com.wurmonline.server.behaviours.Crops;
import com.wurmonline.server.behaviours.Seat;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.creatures.AnimalSettings;
import com.wurmonline.server.creatures.Brand;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.MineDoorPermission;
import com.wurmonline.server.creatures.MineDoorSettings;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.Traits;
import com.wurmonline.server.creatures.Wagoner;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.epic.ValreiFightHistory;
import com.wurmonline.server.epic.ValreiFightHistoryManager;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.highways.Node;
import com.wurmonline.server.highways.Route;
import com.wurmonline.server.highways.Routes;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemSettings;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.Materials;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.Recipe;
import com.wurmonline.server.items.Recipes;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.items.WurmMail;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Cults;
import com.wurmonline.server.players.Friend;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.players.PermissionsByPlayer;
import com.wurmonline.server.players.PermissionsHistories;
import com.wurmonline.server.players.PermissionsHistoryEntry;
import com.wurmonline.server.players.PermissionsPlayerList;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.PlayerState;
import com.wurmonline.server.players.Titles;
import com.wurmonline.server.questions.ManageObjectList;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.questions.VillageRolesManageQuestion;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.SkillTemplate;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.skills.SkillsFactory;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.BridgePartEnum;
import com.wurmonline.server.structures.Door;
import com.wurmonline.server.structures.DoorSettings;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.FenceGate;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.NoSuchLockException;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.RoofFloorEnum;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.StructureSettings;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Reputation;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Rift;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.PlayerOnlineStatus;
import com.wurmonline.shared.constants.ValreiConstants;
import com.wurmonline.shared.exceptions.WurmServerException;
import com.wurmonline.shared.util.StringUtilities;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

public class GmTool
extends Question {
    private static final Logger logger = Logger.getLogger(GmTool.class.getName());
    private final byte displayType;
    private final byte displaySubType;
    private final long wurmId;
    private final String backString;
    private final byte currentPage;
    private final String searchString;
    private final int rowsPerPage;
    private final List<Player> playerlist = new LinkedList<Player>();
    private final List<Village> villagelist = new LinkedList<Village>();
    private static final String red = "color=\"255,127,127\"";
    private static final String green = "color=\"127,255,127\"";
    private static final String orange = "color=\"255,177,40\"";
    final DecimalFormat df = new DecimalFormat("#.##");
    public static final byte TYPE_VILLAGEID = 0;
    public static final byte TYPE_WURMID = 1;
    public static final byte TYPE_SEARCH = 2;
    public static final byte TYPE_DEITYID = 3;
    public static final byte TYPE_SERVERID = 4;
    public static final byte TYPE_HIGHWAYID = 5;
    public static final byte TYPE_WAGONERID = 6;
    private static final byte GO_BACK = 0;
    private static final byte WURM_ID = 1;
    private static final byte VILLAGE_ID = 2;
    private static final byte TILE_XY = 3;
    private static final byte PLAYER_NAME = 4;
    private static final byte STRING_WURMID = 5;
    private static final byte STRING_XY = 6;
    private static final byte PREV_PAGE = 7;
    private static final byte NEXT_PAGE = 8;
    private static final byte PLAYER_DROPDOWN = 9;
    private static final byte VILLAGE_DROPDOWN = 10;
    private static final byte SEARCH = 11;
    private static final byte TELEPORT_XY = 12;
    private static final byte DEITY_ID = 13;
    private static final byte SERVER_ID = 14;
    private static final byte HIGHWAY_ID = 15;
    private static final byte WAGONER_ID = 16;
    public static final byte SUMMARY = 1;
    private static final byte GUESTS = 2;
    private static final byte HISTORY = 3;
    private static final byte CITIZENS = 4;
    private static final byte ALLIES = 5;
    private static final byte KOS = 6;
    private static final byte ROLES = 7;
    private static final byte BRANDED = 8;
    private static final byte SETTINGS = 9;
    private static final byte BONUS = 4;
    private static final byte AFFINITY = 6;
    private static final byte PRIESTS = 7;
    private static final byte FOLLOWERS = 8;
    private static final byte ALTARS = 9;
    private static final byte VALREIFIGHTS = 10;
    private static final byte FRIENDS = 4;
    private static final byte SKILLS = 5;
    private static final byte BANK = 6;
    private static final byte TITLES = 7;
    private static final byte INVENTORY = 8;
    private static final byte BODY = 9;
    private static final byte CARING_FOR = 10;
    private static final byte MAIL_SENT = 11;
    private static final byte MAIL_WAITING = 12;
    private static final byte HISTORY_IP = 13;
    private static final byte HISTORY_EMAIL = 14;
    private static final byte IGNORING = 15;
    private static final byte CORPSES = 16;
    private static final byte REFERRS = 17;
    private static final byte KOSLIST = 18;
    private static final byte BUILDINGS = 19;
    private static final byte CARTS = 20;
    private static final byte SHIPS = 21;
    private static final byte MINEDOORS = 22;
    private static final byte GATES = 23;
    private static final byte PATH = 24;
    private static final byte TILES = 5;
    private static final byte WALLS = 6;
    private static final byte FLOORS = 7;
    private static final byte BRIDGE_PARTS = 8;
    private static final byte CREATURES = 9;
    private static final byte FENCES = 10;
    private static final byte ITEMS = 11;
    private static final byte TRAITS = 12;
    private static final byte MAIL_ITEMS = 13;
    private static final byte GM_SIGNS = 14;
    private static final byte SERVERS = 15;
    private static final byte TRADERS = 16;
    private static final byte NAMED_RECIPIES = 17;
    private static final byte RIFTS = 18;
    private static final byte ROUTELIST = 19;
    private static final byte ROUTESUMMARY = 20;
    private static final byte NODELIST = 21;
    private static final byte NODESUMMARY = 22;
    private static final byte CATSEYES = 23;
    private static final byte AVATARS = 24;
    private static final byte WAGONERLIST = 25;
    private static final byte KINGDOMS = 26;
    public static final byte SEARCH_BY_EMAIL = 1;
    public static final byte SEARCH_BY_IP = 2;

    public GmTool(Creature aResponder, long aTargetId) {
        super(aResponder, "InGame Web Interface lookalike", "----- " + GmTool.makeTitle((byte)1, (byte)1, aTargetId, "") + " -----", 101, GmTool.findWand(aResponder));
        this.displayType = 1;
        this.displaySubType = 1;
        this.wurmId = aTargetId;
        this.searchString = "";
        this.backString = "";
        this.currentPage = 0;
        this.rowsPerPage = 50;
    }

    public GmTool(Creature aResponder, byte aType, byte aSubType, long aId, String aSearch, String aBack, int aRowsPerPage, byte aPage) {
        super(aResponder, "GM Tool", "----- " + GmTool.makeTitle(aType, aSubType, aId, aSearch) + " -----", 101, GmTool.findWand(aResponder));
        this.displayType = aType;
        this.displaySubType = aSubType;
        this.wurmId = aId;
        this.searchString = aSearch;
        this.backString = aBack;
        this.currentPage = aPage;
        this.rowsPerPage = aRowsPerPage;
    }

    static long findWand(Creature performer) {
        Item[] inv;
        int wand = performer.getPower() >= 4 ? 176 : 315;
        for (Item item : inv = performer.getInventory().getAllItems(false)) {
            if (item.getTemplateId() != wand) continue;
            return item.getWurmId();
        }
        return -10L;
    }

    static String makeTitle(byte aType, byte aSubType, long aId, String aSearch) {
        String strType = "Village";
        if (aType == 0) {
            if (aSubType == 1) {
                strType = "Summary";
            } else if (aSubType == 4) {
                strType = "Citizens";
            } else if (aSubType == 5) {
                strType = "Allies";
            } else if (aSubType == 6) {
                strType = "KOS list";
            } else if (aSubType == 7) {
                strType = "Roles";
            } else if (aSubType == 8) {
                strType = "Branded animals";
            } else if (aSubType == 3) {
                strType = "History";
            } else if (aSubType == 9) {
                strType = "Settings";
            }
            return strType + " of Village with Id:" + aId;
        }
        if (aType == 3) {
            if (aId == -10L) {
                return "List of deities";
            }
            if (aSubType == 1) {
                strType = "Summary";
            } else if (aSubType == 4) {
                strType = "Bonuses";
            } else if (aSubType == 6) {
                strType = "Affinities";
            } else if (aSubType == 7) {
                strType = "Priests";
            } else if (aSubType == 8) {
                strType = "Followers";
            } else if (aSubType == 9) {
                strType = "Altars";
            } else if (aSubType == 10) {
                return "Valrei Fight with FightId: " + aId;
            }
            return strType + " of Deity with Id:" + aId;
        }
        if (aType == 4) {
            if (aSubType == 15 || aSubType == 1 && aId == -10L) {
                strType = "Server List";
            } else if (aSubType == 1) {
                strType = "Summary of Server with id:" + aId;
            } else if (aSubType == 5) {
                strType = "Top Skills";
            } else if (aSubType == 16) {
                strType = "Traders";
            } else if (aSubType == 17) {
                strType = "Named Recipes";
            } else if (aSubType == 18) {
                strType = "Rifts";
            } else if (aSubType == 19) {
                strType = "Highway Routes";
            } else if (aSubType == 21) {
                strType = "Highway Nodes";
            } else if (aSubType == 24) {
                strType = "Avatars";
            } else if (aSubType == 25) {
                strType = "Wagoners";
            } else if (aSubType == 26) {
                strType = "Kingdoms";
            }
            return strType;
        }
        if (aType == 5) {
            strType = aSubType == 20 ? "Summary of Highway Route with id:" + aId : (aSubType == 19 ? "Highway Routes" : (aSubType == 22 ? "Summary of Highway Node with id:" + aId : (aSubType == 21 ? "Highway Nodes" : (aSubType == 23 ? "Catseyes on Route with id:" + aId : "Highway Route with id:" + aId))));
            return strType;
        }
        if (aType == 6) {
            if (aSubType == 25) {
                strType = "Wagoner list";
            } else if (aSubType == 1) {
                strType = "Summary of Wagoner with id:" + aId;
            }
            return strType;
        }
        if (aType == 1) {
            int idType = WurmId.getType(aId);
            strType = "(" + Integer.toString(idType) + ")";
            if (idType == 0) {
                if (aSubType == 1) {
                    strType = "Summary";
                } else if (aSubType == 4) {
                    strType = "Friends";
                } else if (aSubType == 5) {
                    strType = "Skills";
                } else if (aSubType == 6) {
                    strType = "Bank";
                } else if (aSubType == 7) {
                    strType = "Titles";
                } else if (aSubType == 8) {
                    strType = "Inventory";
                } else if (aSubType == 9) {
                    strType = "Body";
                } else if (aSubType == 10) {
                    strType = "Caring for";
                } else if (aSubType == 11) {
                    strType = "Mail Sent";
                } else if (aSubType == 12) {
                    strType = "Mail Waiting";
                } else if (aSubType == 13) {
                    strType = "IP History";
                } else if (aSubType == 14) {
                    strType = "Email History";
                } else if (aSubType == 15) {
                    strType = "Ignore List";
                } else if (aSubType == 16) {
                    strType = "Corpses";
                } else if (aSubType == 17) {
                    strType = "Referrs";
                } else if (aSubType == 18) {
                    strType = "On Kos";
                } else if (aSubType == 19) {
                    strType = "Owned Buildings";
                } else if (aSubType == 20) {
                    strType = "Owned Carts";
                } else if (aSubType == 21) {
                    strType = "Owned Ships";
                } else if (aSubType == 22) {
                    strType = "Owned Minedoors";
                } else if (aSubType == 23) {
                    strType = "Owned Gates";
                } else if (aSubType == 24) {
                    strType = "Highway Route";
                }
                return strType + " of Player with Id:" + aId;
            }
            if (idType == 1) {
                if (aSubType == 1) {
                    strType = "Summary";
                } else if (aSubType == 2) {
                    strType = "Permissionss";
                } else if (aSubType == 3) {
                    strType = "History";
                } else if (aSubType == 8) {
                    strType = "Inventory";
                } else if (aSubType == 9) {
                    strType = "Body";
                } else if (aSubType == 12) {
                    strType = "Traits";
                }
                return strType + " of Creature with Id:" + aId;
            }
            if (idType == 2) {
                if (aSubType == 1) {
                    strType = "Summary of Item";
                } else if (aSubType == 2) {
                    strType = "Permissionss";
                } else if (aSubType == 3) {
                    strType = "History";
                } else if (aSubType == 11) {
                    strType = "Items in Container";
                }
                return strType + " with Id:" + aId;
            }
            if (idType == 3) {
                if (aSubType == 1) {
                    strType = "Summary";
                } else if (aSubType == 2) {
                    strType = "Permissionss";
                } else if (aSubType == 3) {
                    strType = "History";
                } else if (aSubType == 9) {
                    strType = "Creatures";
                } else if (aSubType == 6) {
                    strType = "Walls";
                } else if (aSubType == 10) {
                    strType = "Fences";
                } else if (aSubType == 11) {
                    strType = "Items";
                } else if (aSubType == 7) {
                    strType = "Floors";
                } else if (aSubType == 8) {
                    strType = "Bridge Part";
                } else if (aSubType == 13) {
                    strType = "Items in Mail";
                } else if (aSubType == 14) {
                    return "GM Signs";
                }
                return strType + " on Tile with Id:" + aId;
            }
            if (idType == 4) {
                if (aSubType == 1) {
                    strType = "Summary";
                } else if (aSubType == 2) {
                    strType = "Guests";
                } else if (aSubType == 3) {
                    strType = "History";
                } else if (aSubType == 5) {
                    strType = "Tiles";
                } else if (aSubType == 6) {
                    strType = "Walls";
                } else if (aSubType == 10) {
                    strType = "Fences";
                } else if (aSubType == 7) {
                    strType = "Floors";
                } else if (aSubType == 8) {
                    strType = "Bridge parts";
                }
                return strType + " of Structure with Id:" + aId;
            }
            if (idType == 5) {
                if (aSubType == 1) {
                    strType = "Summary";
                } else if (aSubType == 2) {
                    strType = "Permissionss";
                } else if (aSubType == 3) {
                    strType = "History";
                }
                return strType + " of Walls with Id:" + aId;
            }
            if (idType == 6) {
                return "Temp Items with Id:" + aId;
            }
            if (idType == 7) {
                if (aSubType == 1) {
                    strType = "Summary";
                } else if (aSubType == 2) {
                    strType = "Permissionss";
                } else if (aSubType == 3) {
                    strType = "History";
                }
                return strType + " of Fence with Id:" + aId;
            }
            if (idType == 8 || idType == 32) {
                return "Wounds with Id:" + aId;
            }
            if (idType == 9) {
                return "Creature Skill with Id:" + aId;
            }
            if (idType == 10) {
                return "Player Skill with Id:" + aId;
            }
            if (idType == 31) {
                return "Temporary Skill with Id:" + aId;
            }
            if (idType == 11) {
                return "Template Skill with Id:" + aId;
            }
            if (idType == 12) {
                return "Tile Border with Id:" + aId;
            }
            if (idType == 13) {
                return "Banks with Id:" + aId;
            }
            if (idType == 14) {
                return "Planets with Id:" + aId;
            }
            if (idType == 15) {
                return "Spells with Id:" + aId;
            }
            if (idType == 16) {
                return "Plans with Id:" + aId;
            }
            if (idType == 17) {
                if (aSubType == 1) {
                    strType = "Summary";
                } else if (aSubType == 9) {
                    strType = "Creatures";
                }
                return strType + " of Cave Tile with Id:" + aId;
            }
            if (idType == 18) {
                return "Skill Ids with Id:" + aId;
            }
            if (idType == 19) {
                return "Body Ids with Id:" + aId;
            }
            if (idType == 20) {
                return "Coin Ids with Id:" + aId;
            }
            if (idType == 21) {
                return "WC Commands with Id:" + aId;
            }
            if (idType == 22) {
                return "Mission Performed with Id:" + aId;
            }
            if (idType == 23) {
                return "Floors with Id:" + aId;
            }
            if (idType == 28) {
                return "Bridge part with Id:" + aId;
            }
            return "Unknown type(" + idType + ") with Id:" + aId;
        }
        if (aType == 2) {
            if (aSubType == 1) {
                return "Searching by email " + aSearch;
            }
            if (aSubType == 2) {
                return "Searching by IP " + aSearch;
            }
            return "Unknown search type (" + aSubType + ") for " + aSearch;
        }
        return "Unknown type(" + aType + ") with Id:" + aId + " and Search " + aSearch;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public void answer(Properties aAnswer) {
        String sBack;
        int tableRowsPerPage;
        byte iPage;
        String sSearch;
        long wId;
        byte qSubType;
        int qType;
        block30: {
            String[] parts;
            byte iAction;
            block40: {
                block39: {
                    block38: {
                        block37: {
                            block36: {
                                block35: {
                                    block34: {
                                        block33: {
                                            block32: {
                                                String strback;
                                                block31: {
                                                    block29: {
                                                        String[] backs;
                                                        this.setAnswer(aAnswer);
                                                        if (this.type == 0) {
                                                            logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
                                                            return;
                                                        }
                                                        if (this.type != 101) return;
                                                        if (this.getResponder().getPower() < 2) return;
                                                        String sAction = aAnswer.getProperty("tid");
                                                        iAction = 0;
                                                        qType = 1;
                                                        qSubType = 1;
                                                        wId = this.wurmId;
                                                        sSearch = "";
                                                        iPage = 0;
                                                        strback = aAnswer.getProperty("back");
                                                        String strcurrent = aAnswer.getProperty("current");
                                                        String strRowsPerPage = aAnswer.getProperty("rpp");
                                                        tableRowsPerPage = Integer.parseInt(strRowsPerPage);
                                                        sBack = strback.length() > 0 ? ((backs = strback.split(Pattern.quote("|"))).length >= 5 ? backs[1] + "|" + backs[2] + "|" + backs[3] + "|" + backs[4] + "|" + strcurrent : strback + "|" + strcurrent) : strcurrent;
                                                        parts = sAction.split(",");
                                                        if (parts.length <= 0) return;
                                                        iAction = Byte.parseByte(parts[0]);
                                                        if (iAction != 7) break block29;
                                                        iPage = (byte)(this.currentPage - 1);
                                                        sBack = strback;
                                                        qType = this.displayType;
                                                        qSubType = this.displaySubType;
                                                        sSearch = this.searchString;
                                                        break block30;
                                                    }
                                                    if (iAction != 8) break block31;
                                                    iPage = (byte)(this.currentPage + 1);
                                                    sBack = strback;
                                                    qType = this.displayType;
                                                    qSubType = this.displaySubType;
                                                    sSearch = this.searchString;
                                                    break block30;
                                                }
                                                if (iAction != 0) break block32;
                                                String[] backs = strback.split(Pattern.quote("|"));
                                                if (backs.length <= 0) return;
                                                StringBuilder buf = new StringBuilder();
                                                if (backs.length > 1) {
                                                    buf.append(backs[0]);
                                                    for (int s = 1; s < backs.length - 1; ++s) {
                                                        buf.append("|" + backs[s]);
                                                    }
                                                }
                                                String[] lparts = backs[backs.length - 1].split(",");
                                                qType = Byte.parseByte(lparts[0]);
                                                qSubType = Byte.parseByte(lparts[1]);
                                                wId = Long.parseLong(lparts[2]);
                                                if (lparts.length > 3) {
                                                    sSearch = lparts[3];
                                                }
                                                sBack = buf.toString();
                                                break block30;
                                            }
                                            if (iAction != 1) break block33;
                                            wId = Long.parseLong(parts[1]);
                                            qSubType = Byte.parseByte(parts[2]);
                                            break block30;
                                        }
                                        if (iAction != 2) break block34;
                                        qType = 0;
                                        wId = Long.parseLong(parts[1]);
                                        qSubType = Byte.parseByte(parts[2]);
                                        break block30;
                                    }
                                    if (iAction != 13) break block35;
                                    qType = 3;
                                    wId = Long.parseLong(parts[1]);
                                    qSubType = Byte.parseByte(parts[2]);
                                    break block30;
                                }
                                if (iAction != 14) break block36;
                                qType = 4;
                                wId = Long.parseLong(parts[1]);
                                qSubType = Byte.parseByte(parts[2]);
                                break block30;
                            }
                            if (iAction != 15) break block37;
                            qType = 5;
                            wId = Long.parseLong(parts[1]);
                            qSubType = Byte.parseByte(parts[2]);
                            break block30;
                        }
                        if (iAction != 16) break block38;
                        qType = 6;
                        wId = Long.parseLong(parts[1]);
                        qSubType = Byte.parseByte(parts[2]);
                        break block30;
                    }
                    if (iAction != 3) break block39;
                    int tilex = Integer.parseInt(parts[1]);
                    int tiley = Integer.parseInt(parts[2]);
                    boolean surfaced = Boolean.parseBoolean(parts[3]);
                    wId = Tiles.getTileId(tilex, tiley, 0, surfaced);
                    break block30;
                }
                if (iAction == 12) {
                    int tilex = Integer.parseInt(parts[1]);
                    int tiley = Integer.parseInt(parts[2]);
                    boolean surfaced = Boolean.parseBoolean(parts[3]);
                    try {
                        Item wand = Items.getItem(this.target);
                        this.getResponder().setTeleportLayer(surfaced ? 0 : -1);
                        if (!surfaced && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(tilex, tiley)))) {
                            this.getResponder().getCommunicator().sendNormalServerMessage("The tile " + tilex + ", " + tiley + " is solid cave.");
                        }
                        wand.setData(tilex, tiley);
                        this.getResponder().getCommunicator().sendNormalServerMessage("You quietly mumble: " + tilex + ", " + tiley + " surfaced=" + surfaced);
                        return;
                    }
                    catch (NoSuchItemException e) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("Cannot find wand");
                    }
                    return;
                }
                if (iAction != 4) break block40;
                String pname = aAnswer.getProperty("pname");
                if (pname != null) {
                    PlayerInfo playerInfo = PlayerInfoFactory.createPlayerInfo(pname);
                    try {
                        playerInfo.load();
                    }
                    catch (IOException iox) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("Failed to load data for the player with name " + pname + ".");
                        return;
                    }
                    if (playerInfo == null || playerInfo.wurmId <= 0L) {
                        long[] info = new long[]{Servers.localServer.id, -1L};
                        LoginServerWebConnection lsw = new LoginServerWebConnection();
                        try {
                            info = lsw.getCurrentServer(pname, -1L);
                        }
                        catch (Exception e) {
                            info = new long[]{-1L, -1L};
                        }
                        if (info[0] == -1L) {
                            this.getResponder().getCommunicator().sendNormalServerMessage("Player with name " + pname + " not found anywhere!.");
                            return;
                        }
                        this.getResponder().getCommunicator().sendNormalServerMessage("Player with name " + pname + " has never been on this server, but is currently on server " + info[0] + ", their WurmId is " + info[1] + ".");
                        return;
                    }
                    wId = playerInfo.wurmId;
                }
                break block30;
            }
            if (iAction == 5) {
                String strWurmId = aAnswer.getProperty("wurmid");
                if (strWurmId != null && strWurmId.length() > 0) {
                    try {
                        wId = Long.parseLong(strWurmId);
                    }
                    catch (Exception e) {
                        this.getResponder().getCommunicator().sendNormalServerMessage("Wurm Id is not a number!");
                        return;
                    }
                } else {
                    this.getResponder().getCommunicator().sendNormalServerMessage("name missing");
                    return;
                }
            }
            if (iAction == 6) {
                String strtilex = aAnswer.getProperty("tilex");
                String strtiley = aAnswer.getProperty("tiley");
                String strtilesurface = aAnswer.getProperty("tilesurface");
                int tilex = Integer.parseInt(strtilex);
                int tiley = Integer.parseInt(strtiley);
                boolean surfaced = Integer.parseInt(strtilesurface) == 0;
                wId = Tiles.getTileId(tilex, tiley, 0, surfaced);
            } else if (iAction == 9) {
                String pid = aAnswer.getProperty("playerid");
                int listid = Integer.parseInt(pid);
                Player p = this.playerlist.get(listid);
                if (p == null) {
                    this.getResponder().getCommunicator().sendNormalServerMessage("No player found.");
                    return;
                }
                wId = p.getWurmId();
            } else if (iAction == 10) {
                String vid = aAnswer.getProperty("villid");
                int vidd = Integer.parseInt(vid);
                Village vill = this.villagelist.get(vidd);
                if (vill == null) {
                    this.getResponder().getCommunicator().sendNormalServerMessage("No village found.");
                    return;
                }
                qType = 0;
                wId = vill.id;
            } else {
                if (iAction != 11) return;
                qType = 2;
                qSubType = Byte.parseByte(parts[1]);
                sSearch = qSubType == 1 ? aAnswer.getProperty("searchemail") : aAnswer.getProperty("searchip");
                if (sSearch.length() < 1) {
                    this.getResponder().getCommunicator().sendNormalServerMessage("Nothing to search for!");
                    return;
                }
            }
        }
        GmTool gt = new GmTool(this.getResponder(), (byte)qType, qSubType, wId, sSearch, sBack, tableRowsPerPage, iPage);
        gt.sendQuestion();
    }

    @Override
    public void sendQuestion() {
        String mess = "Running GM Tool for " + GmTool.makeTitle(this.displayType, this.displaySubType, this.wurmId, this.searchString);
        logger.log(Level.INFO, this.getResponder().getName() + " " + mess);
        long startTime = System.currentTimeMillis();
        this.getResponder().getCommunicator().sendNormalServerMessage(mess);
        StringBuilder buf = new StringBuilder();
        buf.append("border{center{text{type='bold';text=\"" + this.question + "\"}};null;scroll{vertical=\"true\";horizontal=\"true\";varray{passthrough{id=\"id\";text=\"" + this.getId() + "\"}");
        if (this.getResponder().getPower() >= 2) {
            try {
                int x;
                buf.append("passthrough{id=\"back\";text=\"" + this.backString + "\"}");
                buf.append("passthrough{id=\"current\";text=\"" + this.displayType + "," + this.displaySubType + "," + this.wurmId + "," + this.searchString + "\"}");
                PlayerInfo pInfo = null;
                if (this.displayType == 0) {
                    try {
                        Village aVillage = Villages.getVillage((int)this.wurmId);
                        String vd = this.villageDetails(aVillage);
                        if (vd.length() == 0) {
                            return;
                        }
                        buf.append(vd);
                    }
                    catch (NoSuchVillageException e1) {
                        logger.log(Level.WARNING, e1.getMessage(), e1);
                        this.getResponder().getCommunicator().sendNormalServerMessage("Village not found");
                    }
                } else if (this.displayType == 3) {
                    if (this.wurmId == -10L) {
                        buf.append(this.deityDetails(null, this.wurmId));
                    } else {
                        Deity deity = Deities.getDeity((int)this.wurmId);
                        if (deity != null || this.displaySubType == 10) {
                            buf.append(this.deityDetails(deity, this.wurmId));
                        } else {
                            buf.append("label{text=\"no such deity\"}");
                        }
                    }
                } else if (this.displayType == 4) {
                    if (this.wurmId != -10L) {
                        ServerEntry sentry = Servers.getServerWithId((int)this.wurmId);
                        if (sentry != null) {
                            buf.append(this.serverDetails(sentry));
                        } else {
                            buf.append("label{text=\"no such server\"}");
                        }
                    } else {
                        buf.append(this.serverDetails(null));
                    }
                } else if (this.displayType == 5) {
                    if (this.wurmId != -10L) {
                        if (this.displaySubType == 23 || this.displaySubType == 20) {
                            Route route = Routes.getRoute((int)this.wurmId);
                            if (route != null) {
                                buf.append(this.highwayDetails(route, null));
                            } else {
                                buf.append("label{text=\"no such route\"}");
                            }
                        } else {
                            Node node = Routes.getNode(this.wurmId);
                            if (node != null) {
                                buf.append(this.highwayDetails(null, node));
                            } else {
                                buf.append("label{text=\"no such node\"}");
                            }
                        }
                    } else {
                        buf.append(this.highwayDetails(null, null));
                    }
                } else if (this.displayType == 6) {
                    if (this.wurmId != -10L) {
                        Wagoner wagoner = Wagoner.getWagoner(this.wurmId);
                        if (wagoner != null) {
                            buf.append(this.wagonerDetails(wagoner));
                        } else {
                            buf.append("label{text=\"no such wagoner\"}");
                        }
                    } else {
                        buf.append(this.wagonerDetails(null));
                    }
                } else if (this.displayType == 2) {
                    if (this.displaySubType == 1) {
                        Object[] pInfos = PlayerInfoFactory.getPlayerInfosWithEmail(this.searchString);
                        Arrays.sort(pInfos);
                        buf.append(this.playerInfoTable((PlayerInfo[])pInfos));
                    } else if (this.displaySubType == 2) {
                        Object[] pInfos = PlayerInfoFactory.getPlayerInfosWithIpAddress(this.searchString);
                        Arrays.sort(pInfos);
                        buf.append(this.playerInfoTable((PlayerInfo[])pInfos));
                    } else {
                        buf.append("label{text=\"Not done (yet)\"};");
                    }
                } else {
                    int idType = WurmId.getType(this.wurmId);
                    if (idType == 0) {
                        pInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(this.wurmId);
                        if (pInfo != null) {
                            buf.append(this.playerDetails(pInfo));
                        }
                    } else if (idType == 4) {
                        Structure lStructure = Structures.getStructure(this.wurmId);
                        if (lStructure != null && lStructure.isTypeHouse()) {
                            buf.append(this.buildingDetails(lStructure));
                        } else if (lStructure != null && lStructure.isTypeBridge()) {
                            buf.append(this.bridgeDetails(lStructure));
                        } else {
                            buf.append("label{text=\"Error - No such Structure\"};");
                        }
                    } else if (idType == 2) {
                        buf.append(this.itemDetails());
                    } else if (idType == 3 || idType == 17) {
                        buf.append(this.tileDetails(idType));
                    } else if (idType == 5) {
                        buf.append(this.wallDetails());
                    } else if (idType == 1) {
                        buf.append(this.creatureDetails());
                    } else if (idType == 7) {
                        buf.append(this.fenceDetails());
                    } else if (idType == 20) {
                        buf.append(this.itemDetails());
                    } else if (idType == 19) {
                        buf.append(this.itemDetails());
                    } else if (idType == 23) {
                        buf.append(this.tileDetails(idType));
                    } else if (idType == 28) {
                        buf.append(this.tileDetails(idType));
                    } else {
                        buf.append("label{text=\"Not done (yet)\"};");
                    }
                }
                buf.append("label{type=\"bold\";text=\"--------------------------- Table rows per page -------------------\"}");
                buf.append("harray{");
                buf.append("radio{group=\"rpp\";id=\"25\"" + (this.rowsPerPage == 25 ? ";selected=\"true\"" : "") + "};label{text=\"25\"};");
                buf.append("radio{group=\"rpp\";id=\"50\"" + (this.rowsPerPage == 50 ? ";selected=\"true\"" : "") + "};label{text=\"50\"};");
                buf.append("radio{group=\"rpp\";id=\"100\"" + (this.rowsPerPage == 100 ? ";selected=\"true\"" : "") + "};label{text=\"100\"};");
                buf.append("radio{group=\"rpp\";id=\"250\"" + (this.rowsPerPage == 250 ? ";selected=\"true\"" : "") + "};label{text=\"250\"};");
                buf.append("radio{group=\"rpp\";id=\"500\"" + (this.rowsPerPage == 500 ? ";selected=\"true\"" : "") + "};label{text=\"500\"};");
                buf.append("radio{group=\"rpp\";id=\"1000\"" + (this.rowsPerPage == 1000 ? ";selected=\"true\"" : "") + "};label{text=\"1000\"};");
                buf.append("}");
                buf.append("label{type=\"bold\";text=\"--------------------------- Options ------------------------------------\"}");
                Object[] players = Players.getInstance().getPlayers();
                Arrays.sort(players);
                Object[] vills = Villages.getVillages();
                Arrays.sort(vills);
                int rows = 6;
                if (this.backString.length() > 0) {
                    ++rows;
                }
                buf.append("table{rows=\"" + rows + "\";cols=\"3\";");
                if (this.backString.length() > 0) {
                    buf.append("radio{group=\"tid\";id=\"0\"};label{text=\"Back\"};label{text=\"\"};");
                }
                buf.append("radio{group=\"tid\";id=\"4\"};label{text=\"Player by Name\"};input{id=\"pname\";maxchars=\"32\";text=\"\"};");
                buf.append("radio{group=\"tid\";id=\"9\"};label{text=\"Online Player\"};dropdown{id=\"playerid\";default=\"0\";options=\"");
                for (x = 0; x < players.length; ++x) {
                    if (x > 0) {
                        buf.append(",");
                    }
                    this.playerlist.add((Player)players[x]);
                    buf.append(((Creature)players[x]).getName());
                }
                buf.append("\"};");
                buf.append("radio{group=\"tid\";id=\"10\"};label{text=\"Village\"};dropdown{id=\"villid\";default=\"0\";options=\"");
                for (x = 0; x < vills.length; ++x) {
                    if (x > 0) {
                        buf.append(",");
                    }
                    this.villagelist.add((Village)vills[x]);
                    buf.append(((Village)vills[x]).getName());
                }
                buf.append("\"};");
                buf.append("radio{group=\"tid\";id=\"5\";selected=\"true\"};label{text=\"Wurm Id\"};input{id=\"wurmid\";maxchars=\"20\";text=\"" + this.wurmId + "\"}");
                buf.append("radio{group=\"tid\";id=\"6\"};label{text=\"Coord (X,Y,surface)\"};harray{label{text=\"(\"};input{id=\"tilex\";maxchars=\"5\";text=\"-1\"};label{text=\",\"};input{id=\"tiley\";maxchars=\"5\";text=\"-1\"};label{text=\",\"};dropdown{id=\"tilesurface\";options=\"true,false\"};label{text=\")\"};}");
                buf.append("radio{group=\"tid\";id=\"11,1\";};label{text=\"Email search\"};input{id=\"searchemail\";maxchars=\"60\";text=\"" + (pInfo != null ? pInfo.emailAddress : "") + "\"}");
                buf.append("radio{group=\"tid\";id=\"11,2\";};label{text=\"IP search\"};input{id=\"searchip\";maxchars=\"30\";text=\"" + (pInfo != null ? pInfo.getIpaddress() : "") + "\"}");
                buf.append("}");
                buf.append("label{text=\"\"};");
                buf.append("label{text=\"Query time: " + (System.currentTimeMillis() - startTime) + "ms\"};");
                buf.append("label{text=\"\"};");
                buf.append(this.createAnswerButton2());
                this.getResponder().getCommunicator().sendBml(600, 500, true, true, buf.toString(), 200, 200, 200, this.title);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, this.wurmId + ": " + e.getMessage(), e);
                this.getResponder().getCommunicator().sendNormalServerMessage("Exception:" + e.getMessage());
            }
        }
    }

    private String deityDetails(Deity aDeity, long id) {
        StringBuilder buf = new StringBuilder();
        if (aDeity == null && id == -10L) {
            if (this.displaySubType == 10) {
                buf.append(this.deityValreiFightsList());
            } else {
                buf.append(this.deityList());
            }
        } else if (this.displaySubType == 1) {
            buf.append(this.deitySummary(aDeity));
        } else if (this.displaySubType == 4) {
            buf.append(this.deityBonuses(aDeity));
        } else if (this.displaySubType == 6) {
            buf.append(this.deityAffinities(aDeity));
        } else if (this.displaySubType == 7) {
            buf.append(this.deityPriests(aDeity));
        } else if (this.displaySubType == 8) {
            buf.append(this.deityFollowers(aDeity));
        } else if (this.displaySubType == 9) {
            buf.append(this.deityAltars(aDeity));
        } else if (this.displaySubType == 10) {
            buf.append(this.deityValreiFightDetails(id));
        }
        buf.append("label{type=\"bold\";text=\"------------------------------ Links --------------------------------------\"}");
        buf.append("table{rows=\"4\";cols=\"4\";");
        buf.append("radio{group=\"tid\";id=\"13,-10,1\"};label{text=\"List of Deities" + (aDeity == null ? " (Showing)" : "") + "\"};");
        buf.append("radio{group=\"tid\";id=\"13,-10,10\"};label{text=\"List of Valrei Fights" + (aDeity == null && this.displaySubType == 10 ? " (Showing)" : "") + "\"};");
        if (aDeity != null) {
            int numbPriests = PlayerInfoFactory.getActivePriestsForDeity(aDeity.getNumber()).length;
            int numbFollowers = PlayerInfoFactory.getActiveFollowersForDeity(aDeity.getNumber()).length;
            int numbAltars = Zones.getAltars(aDeity.getNumber()).length;
            buf.append("radio{group=\"tid\";id=\"13," + this.wurmId + "," + 1 + "\"};label{text=\"Summary" + (this.displaySubType == 1 ? " (Showing)" : "") + "\"};");
            if (this.getResponder().getPower() >= 4) {
                buf.append("radio{group=\"tid\";id=\"13," + this.wurmId + "," + 4 + "\"};label{text=\"Bonus" + (this.displaySubType == 4 ? " (Showing)" : "") + "\"};");
                buf.append("radio{group=\"tid\";id=\"13," + this.wurmId + "," + 6 + "\"};label{text=\"Affinities" + (this.displaySubType == 6 ? " (Showing)" : "") + "\"};");
            }
            buf.append((numbPriests == 0 ? "label{text=\"\"}" : "radio{group=\"tid\";id=\"13," + this.wurmId + "," + 7 + "\"};") + "label{text=\"" + numbPriests + " Active Priests" + (this.displaySubType == 7 ? " (Showing)" : "") + "\"};");
            buf.append((numbFollowers == 0 ? "label{text=\"\"}" : "radio{group=\"tid\";id=\"13," + this.wurmId + "," + 8 + "\"};") + "label{text=\"" + numbFollowers + " Active Followers" + (this.displaySubType == 8 ? " (Showing)" : "") + "\"};");
            buf.append((numbAltars == 0 ? "label{text=\"\"}" : "radio{group=\"tid\";id=\"13," + this.wurmId + "," + 9 + "\"};") + "label{text=\"" + numbAltars + " Altars" + (this.displaySubType == 9 ? " (Showing)" : "") + "\"};");
        }
        buf.append("label{text=\"\"};label{text=\"\"};");
        buf.append("}");
        return buf.toString();
    }

    private String deityList() {
        StringBuilder buf = new StringBuilder();
        Deity[] deities = Deities.getDeities();
        buf.append(this.deityTable(deities));
        return buf.toString();
    }

    private String deityValreiFightsList() {
        StringBuilder buf = new StringBuilder();
        buf.append(this.deityValreiFightsListTable(ValreiFightHistoryManager.getInstance().getAllFights()));
        return buf.toString();
    }

    private String deityValreiFightDetails(long fightId) {
        StringBuilder buf = new StringBuilder();
        ValreiFightHistory fight = ValreiFightHistoryManager.getInstance().getFight(fightId);
        if (fight == null) {
            buf.append("label{text=\"Invalid fight id: " + fightId + "\"};");
        } else {
            buf.append("table{rows=\"1\";cols=\"4\";");
            buf.append("label{text=\"\"};label{text=\"Fight Id\"};label{text=\"\"};label{text=\"" + fightId + "\"};");
            for (int i = 0; i <= fight.getTotalActions(); ++i) {
                buf.append("label{text=\"\"};label{text=\"" + ValreiConstants.getFightActionName(fight.getFightAction(i)) + "\"};label{text=\"\"};label{text=\"" + ValreiConstants.getFightActionSummary(fight.getFightAction(i)) + "\"};");
            }
            buf.append("}");
        }
        return buf.toString();
    }

    private String deitySummary(Deity aDeity) {
        StringBuilder buf = new StringBuilder();
        buf.append("table{rows=\"1\";cols=\"4\";");
        buf.append("label{text=\"\"};label{text=\"Name\"};label{text=\"\"};label{text=\"" + aDeity.getName() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Followers\"};label{text=\"\"};label{text=\"" + aDeity.getActiveFollowers() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Alignment\"};label{text=\"\"};label{text=\"" + aDeity.getAlignment() + "\"};");
        long pid = aDeity.getBestHelper(false);
        if (pid == -10L) {
            buf.append("label{text=\"\"};label{text=\"Best Helper\"};label{text=\"\"};label{type=\"italic\";text=\"Noone!\"};");
        } else {
            PlayerInfo pInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(pid);
            String pName = pInfo == null ? "unknown" : pInfo.getName();
            buf.append("label{text=\"\"};label{text=\"Best Helper\"};label{text=\"\"};label{text=\"" + pName + "\"};");
        }
        buf.append("label{text=\"\"};label{text=\"Average Faith\"};label{text=\"\"};label{text=\"" + aDeity.getFaithPerFollower() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Favor\"};label{text=\"\"};label{text=\"" + aDeity.getFavor() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Favored Kingdom\"};label{text=\"\"};label{text=\"" + Kingdoms.getNameFor(aDeity.getFavoredKingdom()) + " (" + aDeity.getFavoredKingdom() + ")\"};");
        buf.append("label{text=\"\"};label{text=\"Gender\"};label{text=\"\"};label{text=\"" + (aDeity.getSex() == 0 ? "Male" : "Female") + "\"};");
        try {
            ItemTemplate it = ItemTemplateFactory.getInstance().getTemplate(aDeity.getHolyItem());
            buf.append("label{text=\"\"};label{text=\"Holy item?\"};label{text=\"\"};label{text=\"" + it.getName() + "\"};");
        }
        catch (NoSuchTemplateException e) {
            buf.append("label{text=\"\"};label{text=\"Holy item?\"};label{text=\"\"};label{text=\"unknown (" + aDeity.getHolyItem() + ")\"};");
        }
        buf.append("label{text=\"\"};label{text=\"Mountain God?\"};label{text=\"\"};label{" + this.showBoolean(aDeity.isMountainGod()) + "};");
        buf.append("label{text=\"\"};label{text=\"Water God?\"};label{text=\"\"};label{" + this.showBoolean(aDeity.isWaterGod()) + "};");
        buf.append("label{text=\"\"};label{text=\"Forest God?\"};label{text=\"\"};label{" + this.showBoolean(aDeity.isForestGod()) + "};");
        buf.append("label{text=\"\"};label{text=\"Hate God?\"};label{text=\"\"};label{" + this.showBoolean(aDeity.isHateGod()) + "};");
        buf.append("label{text=\"\"};label{text=\"Alignment\"};label{text=\"\"};label{text=\"" + aDeity.getAlignment() + "\"};");
        buf.append("}");
        return buf.toString();
    }

    private String deityBonuses(Deity aDeity) {
        StringBuilder buf = new StringBuilder();
        buf.append("table{rows=\"1\";cols=\"4\";");
        buf.append("label{text=\"\"};label{text=\"Build Wall Bonus\"};label{text=\"\"};label{text=\"" + aDeity.getBuildWallBonus() + "\"};");
        buf.append("}");
        return buf.toString();
    }

    private String deityAffinities(Deity aDeity) {
        StringBuilder buf = new StringBuilder();
        buf.append("table{rows=\"1\";cols=\"4\";");
        buf.append("label{text=\"\"};label{text=\"Meat Affinity?\"};label{text=\"\"};label{" + this.showBoolean(aDeity.isMeatAffinity()) + "};");
        buf.append("label{text=\"\"};label{text=\"Allows Butchering?\"};label{text=\"\"};label{" + this.showBoolean(aDeity.isAllowsButchering()) + "};");
        buf.append("label{text=\"\"};label{text=\"Road Protector?\"};label{text=\"\"};label{" + this.showBoolean(aDeity.isRoadProtector()) + "};");
        buf.append("label{text=\"\"};label{text=\"Item Protector?\"};label{text=\"\"};label{" + this.showBoolean(aDeity.isItemProtector()) + "};");
        buf.append("label{text=\"\"};label{text=\"Warrior?\"};label{text=\"\"};label{" + this.showBoolean(aDeity.isWarrior()) + "};");
        buf.append("label{text=\"\"};label{text=\"Death Protector?\"};label{text=\"\"};label{" + this.showBoolean(aDeity.isDeathProtector()) + "};");
        buf.append("label{text=\"\"};label{text=\"Death Item Protector?\"};label{text=\"\"};label{" + this.showBoolean(aDeity.isDeathItemProtector()) + "};");
        buf.append("label{text=\"\"};label{text=\"Metal Affinity?\"};label{text=\"\"};label{" + this.showBoolean(aDeity.isMetalAffinity()) + "};");
        buf.append("label{text=\"\"};label{text=\"Repairer?\"};label{text=\"\"};label{" + this.showBoolean(aDeity.isRepairer()) + "};");
        buf.append("label{text=\"\"};label{text=\"Learner?\"};label{text=\"\"};label{" + this.showBoolean(aDeity.isLearner()) + "};");
        buf.append("label{text=\"\"};label{text=\"Wood Affinity?\"};label{text=\"\"};label{" + this.showBoolean(aDeity.isWoodAffinity()) + "};");
        buf.append("label{text=\"\"};label{text=\"Befriend Creature?\"};label{text=\"\"};label{" + this.showBoolean(aDeity.isBefriendCreature()) + "};");
        buf.append("label{text=\"\"};label{text=\"Stamina Bonus?\"};label{text=\"\"};label{" + this.showBoolean(aDeity.isStaminaBonus()) + "};");
        buf.append("label{text=\"\"};label{text=\"Food Bonus?\"};label{text=\"\"};label{" + this.showBoolean(aDeity.isFoodBonus()) + "};");
        buf.append("label{text=\"\"};label{text=\"Healer?\"};label{text=\"\"};label{" + this.showBoolean(aDeity.isHealer()) + "};");
        buf.append("label{text=\"\"};label{text=\"Clay Affinity?\"};label{text=\"\"};label{" + this.showBoolean(aDeity.isClayAffinity()) + "};");
        buf.append("label{text=\"\"};label{text=\"Cloth Affinity?\"};label{text=\"\"};label{" + this.showBoolean(aDeity.isClothAffinity()) + "};");
        buf.append("label{text=\"\"};label{text=\"Food Affinity?\"};label{text=\"\"};label{" + this.showBoolean(aDeity.isFoodAffinity()) + "};");
        buf.append("}");
        return buf.toString();
    }

    private String deityPriests(Deity deity) {
        StringBuilder buf = new StringBuilder();
        PlayerInfo[] priests = PlayerInfoFactory.getActivePriestsForDeity(deity.getNumber());
        buf.append(this.deityFollowersTable(priests, deity.getName(), "active priests"));
        return buf.toString();
    }

    private String deityFollowers(Deity deity) {
        StringBuilder buf = new StringBuilder();
        PlayerInfo[] followers = PlayerInfoFactory.getActiveFollowersForDeity(deity.getNumber());
        buf.append(this.deityFollowersTable(followers, deity.getName(), "active followers"));
        return buf.toString();
    }

    private String deityAltars(Deity deity) {
        StringBuilder buf = new StringBuilder();
        Item[] altars = Zones.getAltars(deity.getNumber());
        buf.append(this.deityAltarsTable(altars, deity.getName()));
        return buf.toString();
    }

    private String serverDetails(ServerEntry aServer) {
        ServerEntry[] servers = Servers.getAllServers();
        Shop[] shops = Economy.getTraders();
        Recipe[] recipes = Recipes.getNamedRecipes();
        Creature[] avatars = Creatures.getInstance().getAvatars();
        Kingdom[] kingdoms = Kingdoms.getAllKingdoms();
        StringBuilder buf = new StringBuilder();
        if (this.displaySubType == 15 || this.displaySubType == 1 && aServer == null) {
            buf.append(this.serversTable(servers));
        } else if (this.displaySubType == 1) {
            buf.append(this.serverSummary(aServer));
        } else if (this.displaySubType == 5) {
            buf.append(this.serverTopSkills());
        } else if (this.displaySubType == 16) {
            buf.append(this.traderTable(shops));
        } else if (this.displaySubType == 17) {
            buf.append(this.recipeTable(recipes));
        } else if (this.displaySubType == 18) {
            buf.append(this.riftsTable());
        } else if (this.displaySubType == 19) {
            buf.append(this.routesList());
        } else if (this.displaySubType == 21) {
            buf.append(this.nodesList());
        } else if (this.displaySubType == 24) {
            buf.append(this.avatarsTable(avatars));
        } else if (this.displaySubType == 26) {
            buf.append(this.kingdomsTable(kingdoms));
        }
        buf.append("label{type=\"bold\";text=\"------------------------------ Links --------------------------------------\"}");
        buf.append("table{rows=\"3\";cols=\"6\";");
        buf.append("radio{group=\"tid\";id=\"14," + this.wurmId + "," + 1 + "\"};label{text=\"Summary" + (this.displaySubType == 1 ? " (Showing)" : "") + "\"};");
        buf.append("radio{group=\"tid\";id=\"14,-10,15\"};label{text=\"" + servers.length + " Servers" + (this.displaySubType == 15 ? " (Showing)" : "") + "\"};");
        buf.append("radio{group=\"tid\";id=\"14,-10,16\"};label{text=\"" + shops.length + " Traders" + (this.displaySubType == 16 ? " (Showing)" : "") + "\"};");
        buf.append("radio{group=\"tid\";id=\"14,-10,17\"};label{text=\"" + recipes.length + " Named Recipes" + (this.displaySubType == 17 ? " (Showing)" : "") + "\"};");
        buf.append("radio{group=\"tid\";id=\"14,-10,18\"};label{text=\"Rift List" + (this.displaySubType == 18 ? " (Showing)" : "") + "\"};");
        buf.append("radio{group=\"tid\";id=\"14,-10,24\"};label{text=\"" + avatars.length + " Avatars" + (this.displaySubType == 24 ? " (Showing)" : "") + "\"};");
        buf.append("radio{group=\"tid\";id=\"14,-10,26\"};label{text=\"" + kingdoms.length + " Kingdoms" + (this.displaySubType == 26 ? " (Showing)" : "") + "\"};");
        buf.append("label{text=\"\"};label{text=\"\"};");
        buf.append("label{text=\"\"};label{text=\"\"};");
        if (Features.Feature.HIGHWAYS.isEnabled()) {
            buf.append("radio{group=\"tid\";id=\"15,-10,19\"};label{text=\"Highway Routes" + (this.displaySubType == 19 ? " (Showing)" : "") + "\"};");
            buf.append("radio{group=\"tid\";id=\"15,-10,21\"};label{text=\"Highway Nodes" + (this.displaySubType == 21 ? " (Showing)" : "") + "\"};");
            if (Features.Feature.WAGONER.isEnabled()) {
                buf.append("radio{group=\"tid\";id=\"16,-10,25\"};label{text=\"Wagoner List" + (this.displaySubType == 25 ? " (Showing)" : "") + "\"};");
            } else {
                buf.append("label{text=\"\"};label{text=\"\"};");
            }
        }
        buf.append("}");
        return buf.toString();
    }

    private String serverSummary(ServerEntry aServer) {
        StringBuilder buf = new StringBuilder();
        buf.append("table{rows=\"1\";cols=\"4\";");
        buf.append("label{text=\"\"};label{text=\"Name\"};label{text=\"\"};label{text=\"" + aServer.getName() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Abbreviation\"};label{text=\"\"};label{text=\"" + aServer.getAbbreviation() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Map Name\"};label{text=\"\"};label{text=\"" + aServer.mapname + "\"};");
        buf.append("label{text=\"\"};label{text=\"Kingdom\"};label{text=\"\"};label{text=\"" + Kingdoms.getNameFor(aServer.getKingdom()) + " (" + aServer.getKingdom() + ")\"};");
        buf.append("label{text=\"\"};label{text=\"Max Creatures\"};label{text=\"\"};label{text=\"" + aServer.maxCreatures + "\"};");
        buf.append("label{text=\"\"};label{text=\"Max Typed Creatures\"};label{text=\"\"};label{text=\"" + aServer.maxTypedCreatures + "\"};");
        buf.append("label{text=\"\"};label{text=\"Mesh Size\"};label{text=\"\"};label{text=\"" + aServer.meshSize + "\"};");
        buf.append("label{text=\"\"};label{text=\"Agg Creature %\"};label{text=\"\"};label{text=\"" + aServer.percentAggCreatures + "\"};");
        buf.append("label{text=\"\"};label{text=\"CA Help Group\"};label{text=\"\"};label{text=\"" + aServer.getCAHelpGroup() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Last Spawned Unique\"};label{text=\"\"};label{text=\"" + aServer.getLastSpawnedUnique() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Motd\"};label{text=\"\"};label{text=\"" + aServer.getMotd() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Available?\"};label{text=\"\"};label{" + this.showBoolean(aServer.isAvailable(this.getResponder().getPower(), this.getResponder().isPaying())) + "};");
        buf.append("label{text=\"\"};label{text=\"Challenge Server?\"};label{text=\"\"};label{" + this.showBoolean(aServer.challengeServer) + "};");
        buf.append("label{text=\"\"};label{text=\"Entry Server?\"};label{text=\"\"};label{" + this.showBoolean(aServer.entryServer) + "};");
        buf.append("label{text=\"\"};label{text=\"EPIC?\"};label{text=\"\"};label{" + this.showBoolean(aServer.EPIC) + "};");
        buf.append("label{text=\"\"};label{text=\"HOMESERVER?\"};label{text=\"\"};label{" + this.showBoolean(aServer.HOMESERVER) + "};");
        buf.append("label{text=\"\"};label{text=\"Is Local?\"};label{text=\"\"};label{" + this.showBoolean(aServer.isLocal) + "};");
        buf.append("label{text=\"\"};label{text=\"Is Chaos Server?\"};label{text=\"\"};label{" + this.showBoolean(aServer.isChaosServer()) + "};");
        buf.append("label{text=\"\"};label{text=\"Is Free Deeds?\"};label{text=\"\"};label{" + this.showBoolean(aServer.isFreeDeeds()) + "};");
        buf.append("label{text=\"\"};label{text=\"Is Full?\"};label{text=\"\"};label{" + this.showBoolean(aServer.isFull()) + "};");
        buf.append("label{text=\"\"};label{text=\"Is Payment?\"};label{text=\"\"};label{" + this.showBoolean(aServer.ISPAYMENT) + "};");
        buf.append("label{text=\"\"};label{text=\"PvP Server?\"};label{text=\"\"};label{" + this.showBoolean(aServer.PVPSERVER) + "};");
        buf.append("label{text=\"\"};label{text=\"Drive on...\"};label{text=\"\"};label{text=\"" + (Features.Feature.DRIVE_ON_LEFT.isEnabled() ? "Left" : "Right") + "\"};");
        ServerEntry se = Servers.loginServer;
        buf.append((se != null ? "radio{group=\"tid\";id=\"14," + se.getId() + "," + 1 + "\"};" : "label{text=\"\"};") + "label{text=\"Login Server\"};label{text=\"\"};label{text=\"" + (se != null ? se.getName() : "none") + "\"};");
        se = aServer.serverNorth;
        buf.append((se != null ? "radio{group=\"tid\";id=\"14," + se.getId() + "," + 1 + "\"};" : "label{text=\"\"};") + "label{text=\"North Server\"};label{text=\"\"};label{text=\"" + (se != null ? se.getName() : "none") + "\"};");
        se = aServer.serverWest;
        buf.append((se != null ? "radio{group=\"tid\";id=\"14," + se.getId() + "," + 1 + "\"};" : "label{text=\"\"};") + "label{text=\"West Server\"};label{text=\"\"};label{text=\"" + (se != null ? se.getName() : "none") + "\"};");
        se = aServer.serverEast;
        buf.append((se != null ? "radio{group=\"tid\";id=\"14," + se.getId() + "," + 1 + "\"};" : "label{text=\"\"};") + "label{text=\"East Server\"};label{text=\"\"};label{text=\"" + (se != null ? se.getName() : "none") + "\"};");
        se = aServer.serverSouth;
        buf.append((se != null ? "radio{group=\"tid\";id=\"14," + se.getId() + "," + 1 + "\"};" : "label{text=\"\"};") + "label{text=\"South Server\"};label{text=\"\"};label{text=\"" + (se != null ? se.getName() : "none") + "\"};");
        buf.append("}");
        return buf.toString();
    }

    private String serverTopSkills() {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"Dont use - Causes too much lag\"};");
        return buf.toString();
    }

    private int getParentSkillId(Skill skill) {
        short parentType;
        int[] needed = skill.getDependencies();
        int parentSkillId = 0;
        if (needed.length > 0) {
            parentSkillId = needed[0];
        }
        if (parentSkillId != 0 && (parentType = SkillSystem.getTypeFor(parentSkillId)) == 0) {
            parentSkillId = 0;
        }
        return parentSkillId;
    }

    private String highwayDetails(Route route, Node node) {
        StringBuilder buf = new StringBuilder();
        if (this.displaySubType == 19) {
            buf.append(this.routesList());
        } else if (this.displaySubType == 20) {
            buf.append(this.routeSummary(route));
        } else if (this.displaySubType == 23) {
            buf.append(this.routeCatseyes(route));
        } else if (this.displaySubType == 21) {
            buf.append(this.nodesList());
        } else if (this.displaySubType == 22) {
            buf.append(this.nodeSummary(node));
        }
        int ncats = 0;
        if (route != null) {
            ncats = route.getCatseyes().length;
        }
        buf.append("label{type=\"bold\";text=\"------------------------------ Links --------------------------------------\"}");
        buf.append("table{rows=\"3\";cols=\"6\";");
        buf.append("radio{group=\"tid\";id=\"15,-10,19\"};label{text=\"Route List" + (this.displaySubType == 19 ? " (Showing)" : "") + "\"};");
        if (route != null) {
            buf.append("radio{group=\"tid\";id=\"15," + this.wurmId + "," + 20 + "\"};label{text=\"Route Summary" + (this.displaySubType == 20 ? " (Showing)" : "") + "\"};");
            buf.append("radio{group=\"tid\";id=\"15," + this.wurmId + "," + 23 + "\"};label{text=\"" + ncats + " Route Catseyes" + (this.displaySubType == 23 ? " (Showing)" : "") + "\"};");
        } else {
            buf.append("label{text=\"\"};label{text=\"Route Summary\"};");
            buf.append("label{text=\"\"};label{text=\"Route Catseyes\"};");
        }
        buf.append("radio{group=\"tid\";id=\"15,-10,21\"};label{text=\"Node List" + (this.displaySubType == 21 ? " (Showing)" : "") + "\"};");
        if (node != null) {
            buf.append("radio{group=\"tid\";id=\"15," + this.wurmId + "," + 22 + "\"};label{text=\"Node Summary" + (this.displaySubType == 22 ? " (Showing)" : "") + "\"};");
        } else {
            buf.append("label{text=\"\"};label{text=\"Node Summary\"};");
        }
        buf.append("label{text=\"\"};label{text=\"\"};");
        buf.append("}");
        return buf.toString();
    }

    private String routeSummary(Route route) {
        StringBuilder buf = new StringBuilder();
        buf.append("table{rows=\"1\";cols=\"4\";");
        buf.append("radio{group=\"tid\";id=\"1," + route.getStartNode().getWaystone().getWurmId() + "," + 1 + "\"}label{text=\"Start Waystone\"};label{text=\"\"};label{text=\"" + route.getStartNode().getWaystone().getWurmId() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Start Dir\"};label{text=\"\"};label{text=\"" + MethodsHighways.getLinkAsString(route.getDirection()) + " (" + route.getDirection() + ")\"};");
        int stx = route.getStartNode().getWaystone().getTileX();
        int sty = route.getStartNode().getWaystone().getTileY();
        boolean soS = route.getStartNode().getWaystone().isOnSurface();
        buf.append("radio{group=\"tid\";id=\"3," + stx + "," + sty + "," + soS + "\"};label{text=\"Coord (X,Y,surface)\"};radio{group=\"tid\";id=\"" + 12 + "," + stx + "," + sty + "," + soS + "\"};label{text=\"(" + stx + "," + sty + "," + soS + ")\"}");
        buf.append("radio{group=\"tid\";id=\"15," + this.wurmId + "," + 23 + "\"};label{text=\"Catseyes\"};label{text=\"\"};label{text=\"" + route.getCatseyes().length + " Catseyes\"};");
        Node node = route.getEndNode();
        if (node != null) {
            buf.append("radio{group=\"tid\";id=\"1," + node.getWaystone().getWurmId() + "," + 1 + "\"}label{text=\"End Waystone\"};label{text=\"\"};label{text=\"" + node.getWaystone().getWurmId() + "\"};");
            int etx = route.getEndNode().getWaystone().getTileX();
            int ety = route.getEndNode().getWaystone().getTileY();
            boolean eoS = route.getEndNode().getWaystone().isOnSurface();
            buf.append("radio{group=\"tid\";id=\"3," + etx + "," + ety + "," + eoS + "\"};label{text=\"Coord (X,Y,surface)\"};radio{group=\"tid\";id=\"" + 12 + "," + etx + "," + ety + "," + eoS + "\"};label{text=\"(" + etx + "," + ety + "," + eoS + ")\"}");
        } else {
            buf.append("label{text=\"\"};label{text=\"End Waystone\"};label{text=\"\"};label{type=\"italic\"text=\"missing\"};");
            buf.append("label{text=\"\"};label{text=\"Coord (X,Y,surface)\"};label{text=\"\"};label{type=\"italic\"text=\"missing\"};");
        }
        Route reverseRoute = route.getOppositeRoute();
        if (reverseRoute == null) {
            buf.append("label{text=\"\"};label{text=\"Reverse Route\"};label{text=\"\"};label{type=\"italic\"text=\"missing\"};");
        } else {
            buf.append("radio{group=\"tid\";id=\"15," + reverseRoute.getId() + "," + 20 + "\"};label{text=\"Reverse Route\"};label{text=\"\"};label{" + orange + "text=\"R" + reverseRoute.getId() + "\"};");
        }
        buf.append("label{text=\"\"};label{text=\"Dist\"};label{text=\"\"};label{text=\"" + route.getDistance() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Cost\"};label{text=\"\"};label{text=\"" + route.getCost() + "\"};");
        buf.append("}");
        return buf.toString();
    }

    private String routeCatseyes(Route route) {
        StringBuilder buf = new StringBuilder();
        Item[] catseyes = route.getCatseyes();
        buf.append(this.itemsTable(catseyes));
        return buf.toString();
    }

    private String nodeSummary(Node node) {
        StringBuilder buf = new StringBuilder();
        buf.append("table{rows=\"1\";cols=\"4\";");
        buf.append("radio{group=\"tid\";id=\"1," + this.wurmId + "," + 1 + "\"}label{text=\"Waystone\"};label{text=\"\"};label{text=\"" + node.getWaystone().getWurmId() + "\"};");
        int ntx = node.getWaystone().getTileX();
        int nty = node.getWaystone().getTileY();
        boolean noS = node.getWaystone().isOnSurface();
        buf.append("radio{group=\"tid\";id=\"3," + ntx + "," + nty + "," + noS + "\"};label{text=\"Coord (X,Y,surface)\"};radio{group=\"tid\";id=\"" + 12 + "," + ntx + "," + nty + "," + noS + "\"};label{text=\"(" + ntx + "," + nty + "," + noS + ")\"}");
        buf.append(this.nodeDir(node, (byte)1));
        buf.append(this.nodeDir(node, (byte)2));
        buf.append(this.nodeDir(node, (byte)4));
        buf.append(this.nodeDir(node, (byte)8));
        buf.append(this.nodeDir(node, (byte)16));
        buf.append(this.nodeDir(node, (byte)32));
        buf.append(this.nodeDir(node, (byte)64));
        buf.append(this.nodeDir(node, (byte)-128));
        buf.append("}");
        return buf.toString();
    }

    private String nodeDir(Node node, byte dir) {
        StringBuilder buf = new StringBuilder();
        Route route = node.getRoute(dir);
        if (route != null) {
            buf.append("radio{group=\"tid\";id=\"15," + route.getId() + "," + 20 + "\"};");
            buf.append("label{text=\"" + MethodsHighways.getLinkDirString(dir) + "\"};");
            buf.append("label{text=\"\"};");
            buf.append("label{text=\"Route " + route.getId() + "\"};");
        } else {
            buf.append("label{text=\"\"};");
            buf.append("label{text=\"" + MethodsHighways.getLinkDirString(dir) + "\"};");
            buf.append("label{text=\"\"};");
            buf.append("label{type=\"italic\"text=\"none\"};");
        }
        return buf.toString();
    }

    private String wagonerDetails(Wagoner wagoner) {
        StringBuilder buf = new StringBuilder();
        if (wagoner == null || this.displaySubType == 25) {
            buf.append(this.wagonerList());
        } else if (this.displaySubType == 1) {
            buf.append(this.wagonerSummary(wagoner));
        }
        buf.append("label{type=\"bold\";text=\"------------------------------ Links --------------------------------------\"}");
        buf.append("table{rows=\"3\";cols=\"6\";");
        buf.append("radio{group=\"tid\";id=\"16,-10,25\"};label{text=\"Wagoner List" + (wagoner == null || this.displaySubType == 25 ? " (Showing)" : "") + "\"};");
        buf.append("label{text=\"\"};label{text=\"Wagoner Summary" + (this.displaySubType == 1 ? " (Showing)" : "") + "\"};");
        buf.append("label{text=\"\"};label{text=\"\"};");
        buf.append("}");
        return buf.toString();
    }

    private String wagonerSummary(Wagoner wagoner) {
        StringBuilder buf = new StringBuilder();
        buf.append("table{rows=\"1\";cols=\"4\";");
        buf.append("radio{group=\"tid\";id=\"16," + wagoner.getWurmId() + "," + 1 + "\"}label{text=\"Id\"};label{text=\"\"};label{text=\"" + wagoner.getWurmId() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Name\"};label{text=\"\"};label{text=\"" + wagoner.getName() + "\"};");
        Creature creature = wagoner.getCreature();
        if (creature != null) {
            int posX = creature.getTileX();
            int posY = creature.getTileY();
            boolean onS = creature.isOnSurface();
            buf.append("radio{group=\"tid\";id=\"3," + posX + "," + posY + "," + onS + "\"};label{text=\"Current Coord (X,Y,surface)\"};radio{group=\"tid\";id=\"" + 12 + "," + posX + "," + posY + "," + onS + "\"};label{text=\"(" + posX + "," + posY + "," + onS + ")\"}");
        } else {
            buf.append("label{text=\"\"};label{text=\"Current Coord\"};label{text=\"\"};label{text=\"missing creature\"};");
        }
        buf.append("label{text=\"\"};label{text=\"State\"};label{text=\"\"};label{text=\"" + wagoner.getStateName() + "\"};");
        try {
            Item home = Items.getItem(wagoner.getHomeWaystoneId());
            int posX = home.getTileX();
            int posY = home.getTileY();
            boolean onS = home.isOnSurface();
            buf.append("radio{group=\"tid\";id=\"3," + posX + "," + posY + "," + onS + "\"};label{text=\"Home Coord (X,Y,surface)\"};radio{group=\"tid\";id=\"" + 12 + "," + posX + "," + posY + "," + onS + "\"};label{text=\"(" + posX + "," + posY + "," + onS + ")\"}");
        }
        catch (NoSuchItemException e) {
            buf.append("label{text=\"\"};label{text=\"Home Coord\"};label{text=\"\"};label{text=\"missing waystone (" + wagoner.getHomeWaystoneId() + ")\"};");
        }
        buf.append("}");
        return buf.toString();
    }

    private String villageDetails(Village aVillage) {
        StringBuilder buf = new StringBuilder();
        int numbCitizens = aVillage.getCitizens().length;
        int numbAllies = aVillage.getAllies().length;
        int numbKos = aVillage.getReputations().length;
        int numbBranded = Creatures.getInstance().getBranded(aVillage.id).length;
        int numbRoles = aVillage.getRoles().length;
        if (this.displaySubType == 1) {
            buf.append(this.villageSummary(aVillage));
        } else if (this.displaySubType == 4) {
            buf.append(this.villageCitizens(aVillage));
        } else if (this.displaySubType == 5) {
            buf.append(this.villageAllies(aVillage));
        } else if (this.displaySubType == 6) {
            buf.append(this.villageKos(aVillage));
        } else {
            if (this.displaySubType == 7) {
                VillageRolesManageQuestion vrmq = new VillageRolesManageQuestion(this.getResponder(), "Show roles", "Showing roles and titles.", aVillage.getId(), -10, this.backString, this.rowsPerPage, this.currentPage);
                vrmq.sendQuestion();
                return "";
            }
            if (this.displaySubType == 8) {
                buf.append(this.villageBranded(aVillage));
            } else if (this.displaySubType == 3) {
                buf.append(this.villageHistory(aVillage));
            } else if (this.displaySubType == 9) {
                buf.append(this.villageSettings(aVillage));
            }
        }
        buf.append("label{type=\"bold\";text=\"------------------------------ Links --------------------------------------\"}");
        buf.append("table{rows=\"4\";cols=\"4\";");
        buf.append("radio{group=\"tid\";id=\"2," + this.wurmId + "," + 1 + "\"};label{text=\"Summary" + (this.displaySubType == 1 ? " (Showing)" : "") + "\"};");
        buf.append((numbCitizens == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"2," + this.wurmId + "," + 4 + "\"};") + "label{text=\"" + numbCitizens + " Citizens" + (this.displaySubType == 4 ? " (Showing)" : "") + "\"};");
        buf.append((numbAllies == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"2," + this.wurmId + "," + 5 + "\"};") + "label{text=\"" + numbAllies + " Allies" + (this.displaySubType == 5 ? " (Showing)" : "") + "\"};");
        buf.append((numbKos == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"2," + this.wurmId + "," + 6 + "\"};") + "label{text=\"" + numbKos + " KOS" + (this.displaySubType == 6 ? " (Showing)" : "") + "\"};");
        buf.append((numbBranded == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"2," + this.wurmId + "," + 8 + "\"};") + "label{text=\"" + numbBranded + " Branded animals" + (this.displaySubType == 8 ? " (Showing)" : "") + "\"};");
        buf.append((aVillage.getHistorySize() == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"2," + this.wurmId + "," + 3 + "\"};") + "label{text=\"" + aVillage.getHistorySize() + " History" + (this.displaySubType == 3 ? " (Showing)" : "") + "\"};");
        buf.append("radio{group=\"tid\";id=\"2," + this.wurmId + "," + 7 + "\"};label{text=\"" + numbRoles + " Roles" + (this.displaySubType == 7 ? " (Showing)" : "") + "\"};");
        buf.append("radio{group=\"tid\";id=\"2," + this.wurmId + "," + 9 + "\"};label{text=\"Settings" + (this.displaySubType == 9 ? " (Showing)" : "") + "\"};");
        buf.append("}");
        return buf.toString();
    }

    private String villageSummary(Village aVillage) {
        StringBuilder buf = new StringBuilder();
        buf.append("table{rows=\"16\";cols=\"4\";");
        buf.append("label{text=\"\"};label{text=\"Name\"};label{text=\"\"};label{text=\"" + aVillage.getName() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Motto\"};label{text=\"\"};label{text=\"" + aVillage.getMotto() + "\"};");
        buf.append("radio{group=\"tid\";id=\"1," + aVillage.getDeedId() + "," + 1 + "\"};label{text=\"Deed Id\"};label{text=\"\"};label{text=\"" + aVillage.getDeedId() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Permenent?\"};label{text=\"\"};label{text=\"" + aVillage.isPermanent + "\"}");
        buf.append("label{text=\"\"};label{text=\"Location\"};label{text=\"\"};label{text=\"" + Kingdoms.getNameFor(aVillage.kingdom) + " (" + aVillage.kingdom + ")\"};");
        int tokenx = aVillage.getTokenX();
        int tokeny = aVillage.getTokenY();
        int sizeWest = tokenx - aVillage.getStartX();
        int sizeEast = aVillage.getEndX() - tokenx;
        int sizeNorth = tokeny - aVillage.getStartY();
        int sizeSouth = aVillage.getEndY() - tokeny;
        buf.append("label{text=\"\"};label{text=\"Size E-W\"};label{text=\"\"};label{text=\"E:" + sizeEast + " W:" + sizeWest + " (X:" + aVillage.getDiameterX() + ")\"};");
        buf.append("label{text=\"\"};label{text=\"Size N-S\"};label{text=\"\"};label{text=\"N:" + sizeNorth + " S:" + sizeSouth + " (Y:" + aVillage.getDiameterY() + ")\"};");
        buf.append("label{text=\"\"};label{text=\"Perimeter\"};label{text=\"\"};label{text=\"" + (5 + aVillage.getPerimeterSize()) + "\"};");
        PlayerInfo founderInfo = PlayerInfoFactory.createPlayerInfo(aVillage.getFounderName());
        buf.append("radio{group=\"tid\";id=\"1," + founderInfo.wurmId + "," + 1 + "\"};label{text=\"Founder\"};label{text=\"\"};label{text=\"" + aVillage.getFounderName() + "\"};");
        PlayerInfo mayorInfo = PlayerInfoFactory.createPlayerInfo(aVillage.mayorName);
        buf.append("radio{group=\"tid\";id=\"1," + mayorInfo.wurmId + "," + 1 + "\"};label{text=\"Mayor\"};label{text=\"\"};label{text=\"" + aVillage.mayorName + "\"};");
        if (aVillage.isCapital()) {
            buf.append("label{text=\"\"};label{text=\"Capital of\"};label{text=\"\"};label{text=\"" + Kingdoms.getNameFor(aVillage.kingdom) + "(" + aVillage.kingdom + ")\"}");
        } else {
            buf.append("label{text=\"\"};label{text=\"Capital of\"};label{text=\"\"};label{type=\"italic\";text=\"nothing\"}");
        }
        if (aVillage.disband > 0L) {
            buf.append("label{text=\"\"};label{text=\"Disbanding in\"};label{text=\"\"};label{text=\"" + Server.getTimeFor(aVillage.disband - System.currentTimeMillis()) + "\"};");
            PlayerInfo bInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(aVillage.disbander);
            if (bInfo != null) {
                buf.append("radio{group=\"tid\";id=\"1," + bInfo.wurmId + "," + 1 + "\"};label{text=\"Disbander\"};label{text=\"\"};label{text=\"" + bInfo.getName() + "\"};");
            } else {
                buf.append("label{text=\"\"};label{text=\"Disbander\"};label{text=\"\"};label{type=\"italic\";text=\"unknown\"};");
            }
        } else {
            buf.append("label{text=\"\"};label{text=\"Disbanding in\"};label{text=\"\"};label{type=\"italic\";text=\"N/A\"};");
            buf.append("label{text=\"\"};label{text=\"Disbander\"};label{text=\"\"};label{type=\"italic\";text=\"N/A\"};");
        }
        if (aVillage.guards != null) {
            buf.append("label{text=\"\"};label{text=\"Guards\"};label{text=\"\"};label{text=\"" + aVillage.guards.size() + "\"}");
        } else {
            buf.append("label{text=\"\"};label{text=\"Guards\"};label{text=\"\"};label{type=\"italic\";text=\"none\"}");
        }
        try {
            short[] sp = aVillage.getTokenCoords();
            Integer tx = sp[0];
            Integer ty = sp[1];
            buf.append("radio{group=\"tid\";id=\"3," + tx + "," + ty + ",true\"};label{text=\"Token Coord (X,Y,surface)\"};radio{group=\"tid\";id=\"" + 12 + "," + tx + "," + ty + ",true\"};label{text=\"(" + tx + "," + ty + ",true)\"}");
        }
        catch (NoSuchItemException e) {
            buf.append("label{text=\"\"};label{text=\"Token Coord (X,Y,surface)\"};label{text=\"\"};label{type=\"italic\";text=\"unknown\"}");
        }
        buf.append("label{text=\"\"};label{text=\"in Alliance\"};label{text=\"\"};label{text=\"" + aVillage.getAllianceName() + "\"}");
        buf.append("}");
        return buf.toString();
    }

    private String villageCitizens(Village aVillage) {
        StringBuilder buf = new StringBuilder();
        Object[] citizens = aVillage.getCitizens();
        if (citizens.length > 0) {
            Arrays.sort(citizens);
            buf.append("label{text=\"total # of citizens:" + citizens.length + "\"};");
            int pages = (citizens.length - 1) / this.rowsPerPage;
            int start = this.currentPage * this.rowsPerPage;
            int last = start + this.rowsPerPage < citizens.length ? start + this.rowsPerPage : citizens.length;
            String nav = this.makeNav(this.currentPage, pages);
            if (pages > 0) {
                buf.append(nav);
            }
            if (last > start) {
                buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"4\";label{text=\"\"};label{text=\"Wurm Id\"};label{text=\"Name\"};label{text=\"Role\"};");
                for (int x = start; x < last; ++x) {
                    Object citizen = citizens[x];
                    buf.append("radio{group=\"tid\";id=\"1," + ((Citizen)citizen).getId() + "," + 1 + "\"};label{text=\"" + ((Citizen)citizen).getId() + "\"};label{text=\"" + ((Citizen)citizen).getName() + "\"};label{text=\"" + ((Citizen)citizen).getRole().getName() + "\"};");
                }
                buf.append("}");
            } else {
                buf.append("label{text=\"Nothing to show\"}");
            }
            if (pages > 0) {
                buf.append(nav);
            }
        } else {
            buf.append("label{text=\"" + aVillage.getName() + " has no citizens!\"};");
        }
        return buf.toString();
    }

    private String villageAllies(Village aVillage) {
        StringBuilder buf = new StringBuilder();
        Object[] allies = aVillage.getAllies();
        Arrays.sort(allies);
        buf.append("label{text=\"total # of allies:" + allies.length + " in " + aVillage.getAllianceName() + "\"};");
        int pages = (allies.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < allies.length ? start + this.rowsPerPage : allies.length;
        String nav = "harray{" + (this.currentPage > 0 ? "radio{group=\"tid\";id=\"7\";text=\"Prev \"};" : "") + "label{text=\"Page " + (this.currentPage + 1) + " of " + (pages + 1) + " \"};" + (this.currentPage < pages ? "radio{group=\"tid\";id=\"8\";text=\"Next\"};" : "") + "}";
        if (pages > 0) {
            buf.append(nav);
        }
        if (allies.length > 0) {
            if (last > start) {
                buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"2\";label{text=\"\"};label{text=\"Name\"};");
                for (int x = start; x < last; ++x) {
                    Object allie = allies[x];
                    buf.append("radio{group=\"tid\";id=\"2," + ((Village)allie).getId() + "," + 1 + "\"};label{text=\"" + ((Village)allie).getName() + "\"};");
                }
                buf.append("}");
            } else {
                buf.append("label{text=\"Nothing to show\"}");
            }
        } else {
            buf.append("label{text=\"No Allies\"};");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String villageKos(Village aVillage) {
        StringBuilder buf = new StringBuilder();
        Object[] reputations = aVillage.getReputations();
        Arrays.sort(reputations);
        buf.append("label{text=\"total # of players:" + reputations.length + "\"};");
        int pages = (reputations.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < reputations.length ? start + this.rowsPerPage : reputations.length;
        String nav = "harray{" + (this.currentPage > 0 ? "radio{group=\"tid\";id=\"7\";text=\"Prev \"};" : "") + "label{text=\"Page " + (this.currentPage + 1) + " of " + (pages + 1) + " \"};" + (this.currentPage < pages ? "radio{group=\"tid\";id=\"8\";text=\"Next\"};" : "") + "}";
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"5\";label{text=\"\"};label{text=\"Wurm Id\"};label{text=\"Name\"};label{text=\"Reputation\"};label{text=\"Permanent\"};");
            for (int x = start; x < last; ++x) {
                Object rep = reputations[x];
                long playerWurmId = ((Reputation)rep).getWurmId();
                try {
                    buf.append("radio{group=\"tid\";id=\"1," + playerWurmId + "," + 1 + "\"};label{text=\"" + playerWurmId + "\"};label{text=\"" + ((Reputation)rep).getNameFor() + "\"};label{text=\"" + ((Reputation)rep).getValue() + "\"};label{text=\"" + ((Reputation)rep).isPermanent() + "\"};");
                    continue;
                }
                catch (NoSuchPlayerException e) {
                    buf.append("radio{group=\"tid\";id=\"1," + playerWurmId + "," + 1 + "\"};label{text=\"" + playerWurmId + "\"};label{text=\"Player not found\"};label{text=\"" + ((Reputation)rep).getValue() + "\"};label{text=\"" + ((Reputation)rep).isPermanent() + "\"};");
                }
            }
            buf.append("}");
        } else if (reputations.length == 0) {
            buf.append("label{text=\"No one on KOS\"}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String villageSettings(Village aVillage) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"Settlement is a " + (aVillage.isDemocracy() ? "PERMANENT " : "non-") + "democracy.\"}");
        buf.append("label{text=\"Guards " + (aVillage.allowsAggCreatures() ? "DO" : "dont") + " ignore aggressive creatures.\"}");
        buf.append("label{text=\"" + (aVillage.unlimitedCitizens ? "UNLIMITED" : "Max " + aVillage.getMaxCitizens()) + " citizens.\"}");
        buf.append("label{text=\"Highways are " + (aVillage.isHighwayAllowed() ? "" : "NOT ") + "allowed.\"}");
        buf.append("label{text=\"Kos is " + (aVillage.isKosAllowed() ? "" : "NOT ") + "allowed.\"}");
        buf.append("label{text=\"Players " + (aVillage.isHighwayFound() ? "can" : "cannot") + " find route to village.\"}");
        buf.append("label{text=\"Currently " + (aVillage.hasHighway() ? "has" : "does not have") + " a highway.\"}");
        if (aVillage.getMotd().length() > 0) {
            buf.append("label{text=\"MOTD: " + aVillage.getMotd() + "\";}");
        } else {
            buf.append("label{text=\"No MOTD set.\"}");
        }
        return buf.toString();
    }

    private String villageBranded(Village aVillage) {
        StringBuilder buf = new StringBuilder();
        Creature[] creatures = Creatures.getInstance().getBranded(aVillage.id);
        if (creatures.length > 0) {
            buf.append(this.creaturesTable(creatures));
        } else {
            buf.append("label{text=\"Nothing Branded by this vilage\"};");
        }
        return buf.toString();
    }

    private String villageHistory(Village aVillage) {
        StringBuilder buf = new StringBuilder();
        Object[] histories = aVillage.getHistoryEvents();
        Arrays.sort(histories);
        if (histories.length > 0) {
            buf.append(this.villageHistoryTable((HistoryEvent[])histories));
        } else {
            buf.append("label{text=\"" + aVillage.getName() + " has no history!?\"};");
        }
        return buf.toString();
    }

    private String playerDetails(PlayerInfo pInfo) {
        StringBuilder buf = new StringBuilder(8000);
        Player p = null;
        int numbBody = 0;
        int numbInventory = 0;
        Skills skills = null;
        Bank bank = null;
        try {
            p = this.getPlayerFromInfo(pInfo);
            numbBody = p.getBody().getAllItems().length;
            numbInventory = p.getInventory().getItemsAsArray().length;
        }
        catch (Exception exception) {
            // empty catch block
        }
        int numbSkills = 0;
        try {
            if (p != null) {
                skills = p.getSkills();
            } else {
                skills = SkillsFactory.createSkills(this.wurmId);
                skills.load();
            }
            Skill[] skillarr = skills.getSkills();
            numbSkills = skillarr.length;
        }
        catch (Exception skillarr) {
            // empty catch block
        }
        int numbBankItems = 0;
        try {
            BankSlot[] slots;
            bank = Banks.getBank(this.wurmId);
            if (bank != null && (slots = bank.slots) != null) {
                for (int x = 0; x < slots.length; ++x) {
                    if (slots[x] == null) continue;
                    ++numbBankItems;
                }
            }
        }
        catch (Exception slots) {
            // empty catch block
        }
        Map<String, Byte> referrals = Servers.isRealLoginServer() ? PlayerInfoFactory.getReferrers(pInfo.wurmId) : new LoginServerWebConnection().getReferrers(this.getResponder(), pInfo.wurmId);
        Village[] kosList = Villages.getKosVillagesFor(pInfo.wurmId);
        int numbCaring = Creatures.getInstance().getProtectedCreaturesFor(pInfo.wurmId).length;
        int numbTitles = pInfo.getTitles().length;
        int numbFriends = pInfo.getFriends().length;
        Set<WurmMail> sent = WurmMail.getMailsSendBy(this.wurmId);
        Set<WurmMail> waiting = WurmMail.getMailsFor(this.wurmId);
        int numbSent = sent.size();
        int numbWaiting = waiting.size();
        int numbHistoryIP = pInfo.historyIPStart.size();
        int numbHistoryEmail = pInfo.historyEmail.size();
        int numbIgnoring = pInfo.getIgnored().length;
        int numbReferrals = referrals == null ? 0 : referrals.size();
        int numbKos = kosList.length;
        Structure[] buildings = new Structure[]{};
        ArrayList<Item> corpses = new ArrayList<Item>();
        ArrayList<Item> carts = new ArrayList<Item>();
        ArrayList<Item> ships = new ArrayList<Item>();
        MineDoorPermission[] minedoors = new MineDoorPermission[]{};
        FenceGate[] gates = new FenceGate[]{};
        List<Route> path = null;
        if (p != null) {
            Items.getOwnedCorpsesCartsShipsFor(p, corpses, carts, ships);
            buildings = Structures.getOwnedBuildingFor(p);
            minedoors = MineDoorPermission.getOwnedMinedoorsFor(p);
            gates = FenceGate.getOwnedGatesFor(p);
            path = p.getHighwayPath();
        }
        int numbCorpses = corpses.size();
        int numbBuildings = buildings.length;
        int numbCarts = carts.size();
        int numbShips = ships.size();
        int numbMinedoors = minedoors.length;
        int numbGates = gates.length;
        if (this.displaySubType == 1) {
            buf.append(this.playerSummary(pInfo, p));
        } else if (this.displaySubType == 4) {
            buf.append(this.playerFriends(pInfo));
        } else if (this.displaySubType == 5) {
            buf.append(this.playerSkills(skills));
        } else if (this.displaySubType == 6) {
            buf.append(this.playerBank(bank));
        } else if (this.displaySubType == 7) {
            buf.append(this.playerTitles(pInfo));
        } else if (this.displaySubType == 8) {
            buf.append(this.playerInventory(pInfo, p));
        } else if (this.displaySubType == 9) {
            buf.append(this.playerBody(pInfo, p));
        } else if (this.displaySubType == 10) {
            buf.append(this.playerCaringFor(pInfo));
        } else if (this.displaySubType == 11) {
            buf.append(this.playerMail(pInfo, "from", sent));
        } else if (this.displaySubType == 12) {
            buf.append(this.playerMail(pInfo, "to", waiting));
        } else if (this.displaySubType == 13) {
            buf.append(this.playerHistoryIP(pInfo));
        } else if (this.displaySubType == 14) {
            buf.append(this.playerHistoryEmail(pInfo));
        } else if (this.displaySubType == 15) {
            buf.append(this.playerIgnoreList(pInfo));
        } else if (this.displaySubType == 16) {
            buf.append(this.playerCorpses(pInfo));
        } else if (this.displaySubType == 17) {
            buf.append(this.playerReferrs(referrals, pInfo));
        } else if (this.displaySubType == 18) {
            buf.append(this.kosVillageTable(kosList, pInfo));
        } else if (this.displaySubType == 19) {
            buf.append(this.playerBuildings(p, buildings));
        } else if (this.displaySubType == 20) {
            buf.append(this.playerItems("carts", p, carts.toArray(new Item[carts.size()])));
        } else if (this.displaySubType == 21) {
            buf.append(this.playerItems("ships", p, ships.toArray(new Item[ships.size()])));
        } else if (this.displaySubType == 22) {
            buf.append(this.playerMinedoors(p, minedoors));
        } else if (this.displaySubType == 23) {
            buf.append(this.playerGates(p, gates));
        } else if (this.displaySubType == 24) {
            buf.append(this.playerPath(p, path));
        }
        buf.append("label{type=\"bold\";text=\"------------------------------ Links --------------------------------------\"}");
        buf.append("table{rows=\"3\";cols=\"6\";");
        buf.append("radio{group=\"tid\";id=\"1," + this.wurmId + "," + 1 + "\"};label{text=\"Summary" + (this.displaySubType == 1 ? " (Showing)" : "") + "\"};");
        buf.append((numbFriends == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 4 + "\"};") + "label{text=\"" + numbFriends + " Friends" + (this.displaySubType == 4 ? " (Showing)" : "") + "\"};");
        buf.append((numbSkills == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 5 + "\"};") + "label{text=\"" + numbSkills + " Skills" + (this.displaySubType == 5 ? " (Showing)" : "") + "\"};");
        buf.append((numbBankItems == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 6 + "\"};") + "label{text=\"" + numbBankItems + " Bank Items" + (this.displaySubType == 6 ? " (Showing)" : "") + "\"};");
        buf.append((numbTitles == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 7 + "\"};") + "label{text=\"" + numbTitles + " Titles" + (this.displaySubType == 7 ? " (Showing)" : "") + "\"};");
        buf.append((numbCaring == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 10 + "\"};") + "label{text=\"" + numbCaring + " Caring For" + (this.displaySubType == 10 ? " (Showing)" : "") + "\"};");
        buf.append((numbSent == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 11 + "\"};") + "label{text=\"" + numbSent + " Mail Sent" + (this.displaySubType == 11 ? " (Showing)" : "") + "\"};");
        buf.append((numbWaiting == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 12 + "\"};") + "label{text=\"" + numbWaiting + " Mail Waiting" + (this.displaySubType == 12 ? " (Showing)" : "") + "\"};");
        buf.append((numbIgnoring == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 15 + "\"};") + "label{text=\"" + numbIgnoring + " Ignored" + (this.displaySubType == 15 ? " (Showing)" : "") + "\"};");
        buf.append((numbHistoryIP == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 13 + "\"};") + "label{text=\"" + numbHistoryIP + " IP History" + (this.displaySubType == 13 ? " (Showing)" : "") + "\"};");
        buf.append((numbHistoryEmail == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 14 + "\"};") + "label{text=\"" + numbHistoryEmail + " Email History" + (this.displaySubType == 14 ? " (Showing)" : "") + "\"};");
        buf.append((numbCorpses == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 16 + "\"};") + "label{text=\"" + numbCorpses + " Corpses" + (this.displaySubType == 16 ? " (Showing)" : "") + "\"};");
        buf.append((numbReferrals == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 17 + "\"};") + "label{text=\"" + numbReferrals + " Referrs" + (this.displaySubType == 17 ? " (Showing)" : "") + "\"};");
        buf.append((numbKos == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 18 + "\"};") + "label{text=\"" + numbKos + " times on KOS" + (this.displaySubType == 18 ? " (Showing)" : "") + "\"};");
        if (p != null) {
            buf.append((numbInventory == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 8 + "\"};") + "label{text=\"" + numbInventory + " Inventory" + (this.displaySubType == 8 ? " (Showing)" : "") + "\"};");
            buf.append((numbBody == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 9 + "\"};") + "label{text=\"" + numbBody + " Body" + (this.displaySubType == 9 ? " (Showing)" : "") + "\"};");
            buf.append((numbBuildings == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 19 + "\"};") + "label{text=\"" + numbBuildings + " Owned Buildings" + (this.displaySubType == 19 ? " (Showing)" : "") + "\"};");
            buf.append((numbCarts == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 20 + "\"};") + "label{text=\"" + numbCarts + " Owned Carts" + (this.displaySubType == 20 ? " (Showing)" : "") + "\"};");
            buf.append((numbShips == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 21 + "\"};") + "label{text=\"" + numbShips + " Owned Ships" + (this.displaySubType == 21 ? " (Showing)" : "") + "\"};");
            buf.append((numbMinedoors == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 22 + "\"};") + "label{text=\"" + numbMinedoors + " Owned Minedoors" + (this.displaySubType == 22 ? " (Showing)" : "") + "\"};");
            buf.append((numbGates == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 23 + "\"};") + "label{text=\"" + numbGates + " Owned Gates" + (this.displaySubType == 23 ? " (Showing)" : "") + "\"};");
            if (Features.Feature.HIGHWAYS.isEnabled()) {
                buf.append((path == null ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 24 + "\"};") + "label{text=\"" + (path == null ? "No" : "Has") + " Highway Route" + (this.displaySubType == 24 ? " (Showing)" : "") + "\"};");
                buf.append("label{text=\"\"};label{text=\"\"};");
                buf.append("label{text=\"\"};label{text=\"\"};");
            }
        } else {
            buf.append("label{text=\"\"};label{text=\"\"};");
        }
        buf.append("}");
        return buf.toString();
    }

    private String playerSummary(PlayerInfo pInfo, @Nullable Player p) {
        boolean playerExists;
        boolean playerFound;
        String serverName;
        boolean thisServer;
        StringBuilder buf;
        block64: {
            buf = new StringBuilder();
            thisServer = false;
            serverName = "";
            playerFound = false;
            playerExists = true;
            PlayerInfo localInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(this.wurmId);
            try {
                if (localInfo != null) {
                    localInfo.load();
                }
                if (localInfo == null || localInfo.currentServer != Servers.localServer.id) {
                    LoginServerWebConnection lsw = new LoginServerWebConnection();
                    try {
                        long[] wurmids = new long[]{this.wurmId};
                        PlayerState loginState = new PlayerState(lsw.getPlayerStates(wurmids).get(this.wurmId));
                        if (loginState != null && loginState.getServerId() >= 0) {
                            playerFound = true;
                            serverName = loginState.getServerName();
                            break block64;
                        }
                        playerExists = false;
                    }
                    catch (Exception e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                    break block64;
                }
                thisServer = true;
                serverName = Servers.localServer.getName();
                playerFound = true;
            }
            catch (IOException e) {
                logger.log(Level.WARNING, e.getMessage(), e);
            }
            catch (Exception e) {
                logger.log(Level.WARNING, "CatchAll:" + e.getMessage(), e);
            }
        }
        String pStatus = "On Server " + serverName;
        String pColour = "66,66,225";
        if (playerFound) {
            if (thisServer) {
                try {
                    Players.getInstance().getPlayer(pInfo.getPlayerId());
                    serverName = Servers.localServer.getName();
                    pStatus = "Online";
                    pColour = "66,225,66";
                }
                catch (NoSuchPlayerException e) {
                    pStatus = "Offline";
                    pColour = "255,66,66";
                }
            }
        } else if (!playerExists) {
            buf.append("label{text=\"Unknown Player!\"};");
        } else {
            buf.append("label{text=\"Player not found\"};");
        }
        if (pInfo != null) {
            String aState = pStatus;
            if (!thisServer) {
                aState = "Wrong Server!";
            }
            buf.append("table{rows=\"35\";cols=\"4\";");
            buf.append("label{text=\"\"};label{text=\"Name\"};label{text=\"\"};label{text=\"" + pInfo.getName() + "\"};");
            buf.append("label{text=\"\"};label{text=\"Wurm Id\"};label{text=\"\"};label{text=\"" + this.wurmId + "\"}");
            buf.append("label{text=\"\"};label{text=\"Steam Id\"};label{text=\"\"};label{text=\"" + pInfo.getSteamId() + "\"}");
            buf.append("label{text=\"\"};label{text=\"Current Server\"};label{text=\"\"};label{text=\"" + serverName + "\"};");
            buf.append("label{text=\"\"};label{text=\"Status\"};label{text=\"\"};label{color=\"" + pColour + "\";text=\"" + pStatus + "\"};");
            Village citizenVillage = Villages.getVillageForCreature(this.wurmId);
            if (citizenVillage != null) {
                Citizen citiz = citizenVillage.getCitizen(this.wurmId);
                buf.append("radio{group=\"tid\";id=\"2," + citizenVillage.getId() + "," + 1 + "\"};label{text=\"CitizenVillage\"};label{text=\"\"};label{text=\"" + citizenVillage.getName() + "\"};");
                buf.append("label{text=\"\"};label{text=\"CitizenRole\"};label{text=\"\"};label{text=\"" + citiz.getRole().getName() + "\"};");
            } else {
                buf.append("label{text=\"\"};label{text=\"CitizenVillage\"};label{text=\"\"};label{type=\"italic\";text=\"None\"};");
                buf.append("label{text=\"\"};label{text=\"CitizenRole\"};label{text=\"\"};label{type=\"italic\";text=\"N/A\"};");
            }
            buf.append("label{text=\"\"};label{text=\"Alignment\"};label{text=\"\"};label{text=\"" + new Float(pInfo.getAlignment()) + "\"};");
            buf.append("label{text=\"\"};label{text=\"Reputation\"};label{text=\"\"};label{text=\"" + pInfo.getReputation() + "\"};");
            buf.append("label{text=\"\"};label{text=\"Karma\"};label{text=\"\"};label{text=\"" + pInfo.getKarma() + "\"};");
            buf.append("label{text=\"\"};label{text=\"Sleep Bonus\"};label{text=\"\"};label{text=\"" + pInfo.getSleepLeft() + " (" + (pInfo.isSleepFrozen() ? "Frozen" : "Active") + ")\"};");
            buf.append("label{text=\"\"};label{text=\"Battle Rank\"};label{text=\"\"};label{text=\"" + Integer.valueOf(pInfo.getRank()) + "\"};");
            if (pInfo.getDeity() != null) {
                buf.append("radio{group=\"tid\";id=\"13," + pInfo.getDeity().getNumber() + "," + 1 + "\"};label{text=\"Deity\"};label{text=\"\"};label{text=\"" + pInfo.getDeity().name + "\"};");
            } else {
                buf.append("label{text=\"\"};label{text=\"Deity\"};label{text=\"\"};label{type=\"italic\";text=\"None\"};");
            }
            buf.append("label{text=\"\"};label{text=\"Faith\"};label{text=\"\"};label{text=\"" + new Float(pInfo.getFaith()) + "\"};");
            buf.append("label{text=\"\"};label{text=\"Favor\"};label{text=\"\"};label{text=\"" + new Float(pInfo.getFavor()) + "\"};");
            if (p != null && p.getCultist() != null) {
                buf.append("label{text=\"\"};label{text=\"Meditation Path\"};label{text=\"\"};label{text=\"" + Cults.getPathNameFor(p.getCultist().getPath()) + " (ID: " + p.getCultist().getPath() + ")\"};");
                buf.append("label{text=\"\"};label{text=\"Path Level\"};label{text=\"\"};label{text=\"" + Cults.getNameForLevel(p.getCultist().getPath(), p.getCultist().getLevel()) + " (" + p.getCultist().getLevel() + ")\"};");
                buf.append("label{text=\"\"};label{text=\"Last Meditated\"};label{text=\"\"};label{text=\"" + WurmCalendar.formatGmt(p.getCultist().getLastMeditated()) + ")\"};");
                buf.append("label{text=\"\"};label{text=\"Last Path Gained\"};label{text=\"\"};label{text=\"" + WurmCalendar.formatGmt(p.getCultist().getLastReceivedLevel()) + ")\"};");
                buf.append("label{text=\"\"};label{text=\"Last Enlightened\"};label{text=\"\"};label{text=\"" + WurmCalendar.formatGmt(p.getCultist().getLastEnlightened()) + ")\"};");
            } else {
                buf.append("label{text=\"\"};label{text=\"Meditation Path\"};label{text=\"\"};label{type=\"italic\";text=\"None\"};");
            }
            buf.append("label{text=\"\"};label{text=\"Gender\"};label{text=\"\"};");
            if (p != null) {
                if (p.getSex() == 0) {
                    buf.append("label{text=\"Male\"}");
                } else {
                    buf.append("label{text=\"Female\"}");
                }
            } else {
                buf.append("label{type=\"italic\";text=\"(" + aState + ")\"}");
            }
            buf.append("label{text=\"\"};label{text=\"Money in Bank\"};label{text=\"\"};label{text=\"" + Long.valueOf(pInfo.money) + "\"};");
            buf.append("label{text=\"\"};label{text=\"Account Creation Date\"};label{text=\"\"};label{text=\"" + new Date(pInfo.creationDate) + "\"};");
            buf.append("label{text=\"\"};label{text=\"Premium Account Expiry\"};label{text=\"\"};label{text=\"" + new Date(pInfo.getPaymentExpire()) + "\"};");
            if (p != null) {
                int posX = (int)p.getStatus().getPositionX() >> 2;
                int posY = (int)p.getStatus().getPositionY() >> 2;
                boolean onSurface = p.getStatus().isOnSurface();
                buf.append("radio{group=\"tid\";id=\"3," + posX + "," + posY + "," + onSurface + "\"};label{text=\"Coord (X,Y,surface)\"};radio{group=\"tid\";id=\"" + 12 + "," + posX + "," + posY + "," + onSurface + "\"};label{text=\"(" + posX + "," + posY + "," + onSurface + ")\"};");
                buf.append("label{text=\"\"};label{text=\"Actual (X,Y,Z)\"};label{text=\"\"};label{text=\"(" + p.getPosX() + "," + p.getPosY() + "," + p.getPositionZ() + ")\"}");
                Recipe[] currentVillage = Villages.getVillage(posX, posY, true);
                Village perimVillage = Villages.getVillageWithPerimeterAt(posX, posY, true);
                if (currentVillage != null) {
                    buf.append("radio{group=\"tid\";id=\"2," + currentVillage.getId() + "," + 1 + "\"};label{text=\"In Village\"};label{text=\"\"};label{text=\"" + currentVillage.getName() + "\"};");
                    buf.append("label{text=\"\"};label{text=\"In Kingdom\"};label{text=\"\"};label{text=\"" + Kingdoms.getNameFor(currentVillage.kingdom) + "(" + currentVillage.kingdom + ")\"};");
                } else if (perimVillage != null) {
                    buf.append("radio{group=\"tid\";id=\"2," + perimVillage.getId() + "," + 1 + "\"};label{text=\"In Perimeter\"};label{text=\"\"};label{text=\"" + perimVillage.getName() + "\"};");
                    buf.append("label{text=\"\"};label{text=\"In Kingdom\"};label{text=\"\"};label{text=\"" + Kingdoms.getNameFor(perimVillage.kingdom) + "(" + perimVillage.kingdom + ")\"};");
                } else if (p.currentKingdom != 0) {
                    buf.append("label{text=\"\"};label{text=\"In Village\"};label{text=\"\"};label{type=\"italic\";text=\"none\"};");
                    buf.append("label{text=\"\"};label{text=\"In Kingdom\"};label{text=\"\"};label{text=\"" + Kingdoms.getNameFor(p.currentKingdom) + "(" + p.currentKingdom + ")\"};");
                } else {
                    buf.append("label{text=\"\"};label{text=\"In Village\"};label{text=\"\"};label{type=\"italic\";text=\"unknown\"};");
                    buf.append("label{text=\"\"};label{text=\"In Kingdom\"};label{text=\"\"};label{type=\"italic\";text=\"unknown (0)\"};");
                }
                buf.append(this.addBridge(p.getBridgeId()));
                buf.append("label{text=\"\"};label{text=\"Floor Level\"};label{text=\"\"};label{text=\"" + p.getFloorLevel() + "\"};");
                if (p.getBestCompass() != null) {
                    buf.append("radio{group=\"tid\";id=\"1," + p.getBestCompass().getWurmId() + "," + 1 + "\"};label{text=\"Best Compass\"};label{text=\"\"};label{text=\"" + p.getBestCompass().getWurmId() + "\"};");
                } else {
                    buf.append("label{text=\"\"};label{text=\"Best Compass\"};label{text=\"\"};label{text=\"None\"};");
                }
                if (p.getBestBeeSmoker() != null) {
                    buf.append("radio{group=\"tid\";id=\"1," + p.getBestBeeSmoker().getWurmId() + "," + 1 + "\"};label{text=\"Best Bee Smoker\"};label{text=\"\"};label{text=\"" + p.getBestBeeSmoker().getWurmId() + "\"};");
                } else {
                    buf.append("label{text=\"\"};label{text=\"Best Bee Smoker\"};label{text=\"\"};label{text=\"None\"};");
                }
                if (p.getBestTackleBox() != null) {
                    buf.append("radio{group=\"tid\";id=\"1," + p.getBestTackleBox().getWurmId() + "," + 1 + "\"};label{text=\"Best Tackle Box\"};label{text=\"\"};label{text=\"" + p.getBestTackleBox().getWurmId() + "\"};");
                } else {
                    buf.append("label{text=\"\"};label{text=\"Best Tackle Box\"};label{text=\"\"};label{text=\"None\"};");
                }
            } else {
                buf.append("label{text=\"\"};label{text=\"Coord (X,Y,surface)\"};label{text=\"\"};label{type=\"italic\";text=\"(" + aState + ")\"};");
                buf.append("label{text=\"\"};label{text=\"In Village\"};label{text=\"\"};label{type=\"italic\";text=\"(" + aState + ")\"};");
                buf.append("label{text=\"\"};label{text=\"In Kingdom\"};label{text=\"\"};label{type=\"italic\";text=\"(" + aState + ")\"};");
                buf.append("label{text=\"\"};label{text=\"On Bridge\"};label{text=\"\"};label{type=\"italic\";text=\"Unknown\"};");
            }
            buf.append("label{text=\"\"};label{text=\"Banned\"};label{text=\"\"};label{text=\"" + pInfo.isBanned() + "\"};");
            if (pInfo.isBanned()) {
                buf.append("label{text=\"\"};label{text=\"IPBan reason\"};label{text=\"\"};label{text=\"" + pInfo.banreason + "\"};");
                buf.append("label{text=\"\"};label{text=\"IPBan expires in\"};label{text=\"\"};label{text=\"" + Server.getTimeFor(pInfo.banexpiry - System.currentTimeMillis()) + "\"};");
            }
            buf.append("label{text=\"\"};label{text=\"Warnings\"};label{text=\"\"};label{text=\"" + String.valueOf(pInfo.getWarnings()) + "\"};");
            buf.append("label{text=\"\"};label{text=\"Muted\"};label{text=\"\"};label{text=\"" + pInfo.isMute() + "\"};");
            if (pInfo.isMute()) {
                buf.append("label{text=\"\"};label{text=\"Mute reason\"};label{text=\"\"};label{text=\"" + pInfo.mutereason + "\"};");
                buf.append("label{text=\"\"};label{text=\"Mute expires in\"};label{text=\"\"};label{text=\"" + Server.getTimeFor(pInfo.muteexpiry - System.currentTimeMillis()) + "\"};");
            }
            buf.append("label{text=\"\"};label{text=\"IP Address\"};label{text=\"\"};label{text=\"" + pInfo.getIpaddress() + "\"};");
            buf.append("label{text=\"\"};label{text=\"Email Address\"};label{text=\"\"};label{text=\"" + pInfo.emailAddress + "\"};");
            if (p != null) {
                buf.append("label{text=\"\"};label{text=\"Player Kingdom\"};label{text=\"\"};label{text=\"" + Kingdoms.getNameFor(p.getKingdomId()) + "(" + p.getKingdomId() + ")\"};");
                byte kingdom = Kingdoms.getKingdomTemplateFor(p.getKingdomId());
                buf.append("label{text=\"\"};label{text=\"Player Kingdom Template\"};label{text=\"\"};label{text=\"" + Kingdoms.getNameFor(kingdom) + "(" + kingdom + ")\"};");
            } else {
                buf.append("label{text=\"\"};label{text=\"Player Kingdom\"};label{text=\"\"};label{type=\"italic\";text=\"(" + aState + ")\"};");
            }
            buf.append("label{text=\"\"};label{text=\"Last login\"};label{text=\"\"};label{text=\"" + pInfo.getLastLogin() + " (" + new Date(pInfo.getLastLogin()) + ")\"};");
            buf.append("label{text=\"\"};label{text=\"Last logout\"};label{text=\"\"};label{text=\"" + pInfo.getLastLogout() + " (" + new Date(pInfo.getLastLogout()) + ")\"};");
            PlayerState ps = PlayerInfoFactory.getPlayerState(this.wurmId);
            if (ps == null) {
                buf.append("label{text=\"\"};label{text=\"state login\"};label{text=\"\"};label{text=\"not found\"};");
                buf.append("label{text=\"\"};label{text=\"state logout\"};label{text=\"\"};label{text=\"not found\"};");
            } else {
                buf.append("label{text=\"\"};label{text=\"state login\"};label{text=\"\"};label{text=\"" + ps.getLastLogin() + " (" + new Date(ps.getLastLogin()) + ")\"};");
                buf.append("label{text=\"\"};label{text=\"state logout\"};label{text=\"\"};label{text=\"" + ps.getLastLogout() + " (" + new Date(ps.getLastLogout()) + ")\"};");
            }
            buf.append("label{text=\"\"};label{text=\"PlayingTime\"};label{text=\"\"};label{text=\"" + Server.getTimeFor(pInfo.playingTime) + "\"};");
            if (pInfo.title != null) {
                buf.append("label{text=\"\"};label{text=\"First Title\"};label{text=\"\"};label{text=\"" + pInfo.title.getName(pInfo.isMale()) + "\"};");
            } else {
                buf.append("label{text=\"\"};label{text=\"First Title\"};label{text=\"\"};label{type=\"italic\";text=\"{none}\"};");
            }
            if (pInfo.secondTitle != null) {
                buf.append("label{text=\"\"};label{text=\"Second Title\"};label{text=\"\"};label{text=\"" + pInfo.secondTitle.getName(pInfo.isMale()) + "\"};");
            } else {
                buf.append("label{text=\"\"};label{text=\"Second Title\"};label{text=\"\"};label{type=\"italic\";text=\"{none}\"};");
            }
            buf.append("label{text=\"\"};label{text=\"Priest\"};label{text=\"\"};label{text=\"" + pInfo.isPriest + "\"};");
            if (p != null) {
                if (p.hasPet()) {
                    Creature pet = p.getPet();
                    if (pet != null) {
                        buf.append("radio{group=\"tid\";id=\"1," + pet.getWurmId() + "," + 1 + "\"};label{text=\"Pet\"};label{text=\"\"};label{text=\"" + pet.getName() + "\"};");
                    } else {
                        buf.append("label{text=\"\"};label{text=\"Pet\"};label{text=\"\"};label{type=\"italic\";text=\"Not found\"};");
                    }
                } else {
                    buf.append("label{text=\"\"};label{text=\"Pet\"};label{text=\"\"};label{type=\"italic\";text=\"No\"};");
                }
            } else {
                buf.append("label{text=\"\"};label{text=\"Pet\"};label{text=\"\"};label{type=\"italic\";text=\"{unknown}\"};");
            }
            if (p != null) {
                buf.append("label{text=\"\"};label{text=\"Thirst\"};label{text=\"\"};label{text=\"" + p.getStatus().getThirst() + "\"};");
                buf.append("label{text=\"\"};label{text=\"Hunger\"};label{text=\"\"};label{text=\"" + p.getStatus().getHunger() + "\"};");
                buf.append("label{text=\"\"};label{text=\"Damage\"};label{text=\"\"};label{text=\"" + p.getStatus().damage + "\"};");
                buf.append("label{text=\"\"};label{text=\"Nutrition\"};label{text=\"\"};label{text=\"" + p.getStatus().getNutritionlevel() + "\"};");
                buf.append("label{text=\"\"};label{text=\"CCFP Values\"};label{text=\"\"};label{text=\"" + (int)p.getStatus().getCalories() + ", " + (int)p.getStatus().getCarbs() + ", " + (int)p.getStatus().getFats() + ", " + (int)p.getStatus().getProteins() + "\"};");
                buf.append("label{text=\"\"};label{text=\"CCFP Percentages\"};label{text=\"\"};label{text=\"" + (int)p.getStatus().getCaloriesAsPercent() + "%, " + (int)p.getStatus().getCarbsAsPercent() + "%, " + (int)p.getStatus().getFatsAsPercent() + "%, " + (int)p.getStatus().getProteinsAsPercent() + "%\"};");
            }
            buf.append("label{text=\"\"};label{text=\"Chat no PMs\"};label{text=\"\"};label{text=\"" + pInfo.isFlagSet(1) + "\"};");
            buf.append("label{text=\"\"};label{text=\"Chat x-Kingdom\"};label{text=\"\"};label{text=\"" + pInfo.isFlagSet(2) + "\"};");
            buf.append("label{text=\"\"};label{text=\"Chat x-Server\"};label{text=\"\"};label{text=\"" + pInfo.isFlagSet(3) + "\"};");
            buf.append("label{text=\"\"};label{text=\"Chat Friends\"};label{text=\"\"};label{text=\"" + pInfo.isFlagSet(4) + "\"};");
            buf.append("label{text=\"\"};label{text=\"CCFP Hidden\"};label{text=\"\"};label{text=\"" + pInfo.isFlagSet(52) + "\"};");
            buf.append("label{text=\"\"};label{text=\"New Affinity calc\"};label{text=\"\"};label{text=\"" + pInfo.isFlagSet(53) + "\"};");
            Recipe[] recipes = Recipes.getNamedRecipesFor(pInfo.getName());
            StringBuilder buf2 = new StringBuilder();
            if (recipes.length == 0) {
                buf2.append("none");
            } else {
                for (Recipe recipe : recipes) {
                    if (buf2.length() > 0) {
                        buf2.append(',');
                    }
                    buf2.append(recipe.getName() + " (" + recipe.getRecipeId() + ")");
                }
            }
            buf.append("label{text=\"\"};label{text=\"Their Recipe\"};label{text=\"\"};label{text=\"" + buf2.toString() + "\"};");
            if (pInfo.referrer <= 0L) {
                buf.append("label{text=\"\"};label{text=\"Refered\"};label{text=\"\"};label{text=\"Noone\"};");
            } else {
                PlayerState rstate = PlayerInfoFactory.getPlayerState(pInfo.referrer);
                buf.append("radio{group=\"tid\";id=\"1," + pInfo.referrer + "," + 1 + "\"};label{text=\"Refered\"};label{text=\"\"};label{text=\"" + (rstate != null ? rstate.getPlayerName() : "" + pInfo.referrer) + "\"};");
            }
            buf.append("}");
        }
        return buf.toString();
    }

    private String playerBuildings(Player player, Structure[] buildings) {
        StringBuilder buf = new StringBuilder();
        Arrays.sort(buildings, new Comparator<Structure>(){

            @Override
            public int compare(Structure param1, Structure param2) {
                return param1.getObjectName().compareTo(param2.getObjectName());
            }
        });
        if (buildings.length > 0) {
            buf.append(this.buildingsTable(player, buildings));
        } else {
            buf.append("label{text=\"not owner of any buildings.\"};");
        }
        buf.append("label{text=\"\"};");
        return buf.toString();
    }

    private String playerMinedoors(Player player, MineDoorPermission[] mineDoors) {
        StringBuilder buf = new StringBuilder();
        Arrays.sort(mineDoors, new Comparator<MineDoorPermission>(){

            @Override
            public int compare(MineDoorPermission param1, MineDoorPermission param2) {
                return param1.getObjectName().compareTo(param2.getObjectName());
            }
        });
        if (mineDoors.length > 0) {
            buf.append(this.minedoorsTable(player, mineDoors));
        } else {
            buf.append("label{text=\"not owner of any mine doors.\"};");
        }
        buf.append("label{text=\"\"};");
        return buf.toString();
    }

    private String playerGates(Player player, FenceGate[] gates) {
        StringBuilder buf = new StringBuilder();
        Arrays.sort(gates, new Comparator<FenceGate>(){

            @Override
            public int compare(FenceGate param1, FenceGate param2) {
                if (param1.getFloorLevel() == param2.getFloorLevel()) {
                    int comp = param1.getTypeName().compareTo(param2.getTypeName());
                    if (comp == 0) {
                        return param1.getObjectName().compareTo(param2.getObjectName());
                    }
                    return comp;
                }
                if (param1.getFloorLevel() < param2.getFloorLevel()) {
                    return 1;
                }
                return -1;
            }
        });
        if (gates.length > 0) {
            buf.append(this.gatesTable(player, gates));
        } else {
            buf.append("label{text=\"not owner of any gates.\"};");
        }
        buf.append("label{text=\"\"};");
        return buf.toString();
    }

    private String playerPath(Player player, List<Route> path) {
        StringBuilder buf = new StringBuilder();
        if (path != null && !path.isEmpty()) {
            buf.append(this.pathTable(player, path));
        } else {
            buf.append("label{text=\"no highway routing found.\"};");
        }
        return buf.toString();
    }

    private String playerItems(String itemtype, Player player, Item[] carts) {
        StringBuilder buf = new StringBuilder();
        Arrays.sort(carts, new Comparator<Item>(){

            @Override
            public int compare(Item param1, Item param2) {
                return param1.getObjectName().compareTo(param2.getObjectName());
            }
        });
        if (carts.length > 0) {
            buf.append(this.itemsTable(itemtype, player, carts));
        } else {
            buf.append("label{text=\"not owner of any " + itemtype + ".\"};");
        }
        buf.append("label{text=\"\"};");
        return buf.toString();
    }

    private String playerFriends(PlayerInfo pInfo) {
        StringBuilder buf = new StringBuilder();
        Object[] lfriends = pInfo.getFriends();
        Arrays.sort(lfriends);
        if (lfriends.length > 0) {
            buf.append(this.friendsTable((Friend[])lfriends));
        } else {
            buf.append("label{text=\"" + pInfo.getName() + " has no friends :(\"};");
        }
        buf.append("label{text=\"\"};");
        return buf.toString();
    }

    private String playerSkills(Skills skills) {
        StringBuilder buf = new StringBuilder();
        try {
            Collection<SkillTemplate> temps = SkillSystem.templates.values();
            SkillTemplate[] templates = temps.toArray(new SkillTemplate[temps.size()]);
            buf.append(this.skillsTable(templates, skills));
        }
        catch (Exception iox) {
            logger.log(Level.WARNING, this.wurmId + ": " + iox.getMessage(), iox);
            buf.append("label{text=\"Error getting Skills\"}");
            buf.append("label{text=\"Exception:\"};");
            buf.append("label{text=\"" + iox.getMessage() + "\"};");
        }
        return buf.toString();
    }

    private String playerBank(Bank bank) {
        StringBuilder buf = new StringBuilder();
        if (bank != null) {
            try {
                Village vill = bank.getCurrentVillage();
                if (vill != null) {
                    buf.append("label{text=\"Bank is in " + vill.getName() + ".\"}");
                } else {
                    buf.append("label{text=\"Bank is in an unavailable village\"}");
                }
            }
            catch (BankUnavailableException bua) {
                buf.append("label{text=\"Bank is not in a village?\"}");
            }
            buf.append("label{text=\"\"};");
            BankSlot[] slots = bank.slots;
            if (slots != null) {
                HashSet<Item> allItems = new HashSet<Item>();
                for (int x = 0; x < slots.length; ++x) {
                    if (slots[x] == null) continue;
                    allItems.add(slots[x].getItem());
                }
                Item[] litems = allItems.toArray(new Item[allItems.size()]);
                if (litems.length > 0) {
                    buf.append(this.itemsTable(litems));
                } else {
                    buf.append("label{text=\"Nothing found in  Bank\"}");
                }
            } else {
                buf.append("label{text=\"No Bank Slots?\"}");
            }
        } else {
            buf.append("label{text=\"No Bank found\"}");
        }
        return buf.toString();
    }

    private String playerTitles(PlayerInfo pInfo) {
        StringBuilder buf = new StringBuilder();
        try {
            Titles.Title[] titlearr = pInfo.getTitles();
            if (titlearr.length > 0) {
                buf.append("label{text=\"# of items:" + titlearr.length + "\"};");
                Arrays.sort(titlearr, new Comparator<Titles.Title>(){

                    @Override
                    public int compare(Titles.Title t1, Titles.Title t2) {
                        return t1.getName().compareTo(t2.getName());
                    }
                });
                buf.append("table{rows=\"" + (titlearr.length + 1) + "\";cols=\"2\";label{text=\"\"};label{text=\"Name\"};");
                for (int x = 0; x < titlearr.length; ++x) {
                    buf.append("label{text=\"\"};label{text=\"" + titlearr[x].getName(false) + "\"};");
                }
                buf.append("}");
            } else {
                buf.append("label{text=\"No Titles found\"}");
            }
        }
        catch (Exception iox) {
            logger.log(Level.WARNING, this.wurmId + ": " + iox.getMessage(), iox);
            buf.append("label{text=\"Error getting Titles\"}");
            buf.append("label{text=\"Exception:\"};");
            buf.append("label{text=\"" + iox.getMessage() + "\"};");
        }
        return buf.toString();
    }

    private String playerInventory(PlayerInfo pInfo, Player p) {
        StringBuilder buf = new StringBuilder();
        if (p != null) {
            Item inventory = p.getInventory();
            if (inventory != null) {
                Item[] litems = inventory.getItemsAsArray();
                if (litems.length > 0) {
                    buf.append(this.itemsTable(litems));
                } else {
                    buf.append("label{text=\"No Items found.\"}");
                }
            } else {
                buf.append("label{text=\"No Inventory found.\"}");
            }
        } else {
            buf.append("label{text=\"Player not found.\"}");
        }
        return buf.toString();
    }

    private String playerBody(PlayerInfo pInfo, Player p) {
        StringBuilder buf = new StringBuilder();
        if (p != null) {
            Body lBody = p.getBody();
            if (lBody != null) {
                Item[] litems = lBody.getAllItems();
                if (litems.length > 0) {
                    buf.append(this.itemsTable(litems));
                } else {
                    buf.append("label{text=\"No Items found\"}");
                }
            } else {
                buf.append("label{text=\"Body not found.\"}");
            }
        } else {
            buf.append("label{text=\"Player not found.\"}");
        }
        return buf.toString();
    }

    private String playerCaringFor(PlayerInfo pInfo) {
        StringBuilder buf = new StringBuilder();
        Creature[] creatures = Creatures.getInstance().getProtectedCreaturesFor(pInfo.wurmId);
        if (creatures.length > 0) {
            buf.append(this.creaturesTable(creatures));
        } else {
            buf.append("label{text=\"Nothing Cared for by " + pInfo.getName() + "\"};");
        }
        return buf.toString();
    }

    private String playerMail(PlayerInfo pInfo, String fromto, Set<WurmMail> mails) {
        StringBuilder buf = new StringBuilder();
        Object[] mailList = mails.toArray(new WurmMail[mails.size()]);
        Arrays.sort(mailList);
        if (mailList.length > 0) {
            buf.append(this.mailTable((WurmMail[])mailList));
        } else {
            buf.append("label{text=\"No Mail " + fromto + pInfo.getName() + "\"};");
        }
        return buf.toString();
    }

    private String playerHistoryIP(PlayerInfo pInfo) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{type=\"italic\";text=\"First entry may have wrong date.\"}");
        buf.append("table{rows=\"" + pInfo.historyIPStart.size() + "\";cols=\"3\";label{type=\"bold\";text=\"IP\"};label{type=\"bold\";text=\"First Used\"};label{type=\"bold\";text=\"Last Used\"};");
        for (Map.Entry<String, Long> entry : pInfo.historyIPStart.entrySet()) {
            buf.append("label{text=\"" + entry.getKey() + "\"};label{text=\"" + new Date(entry.getValue()) + "\"};label{text=\"" + new Date(pInfo.historyIPLast.get(entry.getKey())) + "\"};");
        }
        buf.append("}");
        return buf.toString();
    }

    private String playerHistoryEmail(PlayerInfo pInfo) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{type=\"italic\";text=\"First entry may have wrong date.\"}");
        buf.append("table{rows=\"" + pInfo.historyEmail.size() + "\";cols=\"2\";label{type=\"bold\";text=\"IP\"};label{type=\"bold\";text=\"First Used\"};");
        for (Map.Entry<String, Long> entry : pInfo.historyEmail.entrySet()) {
            buf.append("label{text=\"" + entry.getKey() + "\"};label{text=\"" + new Date(entry.getValue()) + "\"};");
        }
        buf.append("}");
        return buf.toString();
    }

    private String playerIgnoreList(PlayerInfo pInfo) {
        StringBuilder buf = new StringBuilder();
        long[] ignoring = pInfo.getIgnored();
        Arrays.sort(ignoring);
        if (ignoring.length > 0) {
            buf.append(this.playersTable(ignoring));
        } else {
            buf.append("label{text=\"" + pInfo.getName() + " is not ignoring anyone.\"};");
        }
        buf.append("label{text=\"\"};");
        return buf.toString();
    }

    private String playerCorpses(PlayerInfo pInfo) {
        StringBuilder buf = new StringBuilder();
        Set<Item> corpses = this.findCorpses(pInfo);
        if (corpses.size() > 0) {
            buf.append(this.corpseTable(corpses, pInfo));
        } else {
            buf.append("label{text=\"No corpses found for " + pInfo.getName() + ".\"};");
        }
        buf.append("label{text=\"\"};");
        return buf.toString();
    }

    private String playerReferrs(Map<String, Byte> referrals, PlayerInfo pInfo) {
        StringBuilder buf = new StringBuilder();
        if (referrals == null) {
            buf.append("label{text=\"Error contacting login server.\"};");
        } else if (referrals.size() > 0) {
            buf.append("table{rows=\"" + (referrals.size() + 1) + "\";cols=\"2\";");
            buf.append("label{type=\"bold\";text=\"Name\"};label{type=\"bold\"text=\"Type\"}");
            for (Map.Entry<String, Byte> entry : referrals.entrySet()) {
                String name = entry.getKey();
                byte referralType = entry.getValue();
                if (referralType == 0) {
                    buf.append("label{text=\"" + name + "\"};label{text=\"Undecided\"}");
                    continue;
                }
                if (referralType == 1) {
                    buf.append("label{text=\"" + name + "\"};label{text=\"3 silver\"}");
                    continue;
                }
                buf.append("label{text=\"" + name + "\"};label{text=\"20 days\"}");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"No referrals found for " + pInfo.getName() + ".\"};");
        }
        buf.append("label{text=\"\"};");
        return buf.toString();
    }

    private Player getPlayerFromInfo(PlayerInfo pInfo) throws Exception {
        try {
            return Players.getInstance().getPlayer(pInfo.getPlayerId());
        }
        catch (NoSuchPlayerException e2) {
            Player p = new Player(pInfo);
            p.getBody().createBodyParts();
            p.checkBodyInventoryConsistency();
            p.loadSkills();
            Items.loadAllItemsForCreature(p, p.getStatus().getInventoryId());
            return p;
        }
    }

    private String buildingDetails(Structure lBuilding) {
        StringBuilder buf = new StringBuilder();
        if (this.displaySubType == 1) {
            buf.append(this.buildingSummary(lBuilding));
        } else if (this.displaySubType == 2) {
            buf.append(this.showGuests(StructureSettings.StructurePermissions.getPermissions(), lBuilding.getPermissionsPlayerList()));
        } else if (this.displaySubType == 3) {
            buf.append(this.showHistory(lBuilding.getWurmId()));
        } else if (this.displaySubType == 5) {
            buf.append(this.buildingTiles(lBuilding));
        } else if (this.displaySubType == 6) {
            buf.append(this.buildingWalls(lBuilding));
        } else if (this.displaySubType == 7) {
            buf.append(this.buildingFloors(lBuilding));
        }
        buf.append("label{type=\"bold\";text=\"--------------- Links -------------------\"}");
        buf.append("table{rows=\"2\";cols=\"4\";");
        buf.append("radio{group=\"tid\";id=\"1," + this.wurmId + "," + 1 + "\"};label{text=\"Summary" + (this.displaySubType == 1 ? " (Showing)" : "") + "\"};");
        buf.append((lBuilding.getStructureTiles().length == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 5 + "\"};") + "label{text=\"" + lBuilding.getStructureTiles().length + " Tiles" + (this.displaySubType == 5 ? " (Showing)" : "") + "\"};");
        buf.append((lBuilding.getPermissionsPlayerList().size() == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 2 + "\"};") + "label{text=\"" + lBuilding.getPermissionsPlayerList().size() + " Guests" + (this.displaySubType == 2 ? " (Showing)" : "") + "\"};");
        buf.append((PermissionsHistories.getPermissionsHistoryFor(lBuilding.getWurmId()).getHistoryEvents().length == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 3 + "\"};") + "label{text=\"" + PermissionsHistories.getPermissionsHistoryFor(lBuilding.getWurmId()).getHistoryEvents().length + " History" + (this.displaySubType == 3 ? " (Showing)" : "") + "\"};");
        buf.append((lBuilding.getWalls().length == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 6 + "\"};") + "label{text=\"" + lBuilding.getWalls().length + " Walls" + (this.displaySubType == 6 ? " (Showing)" : "") + "\"};");
        buf.append((lBuilding.getFloors().length == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 7 + "\"};") + "label{text=\"" + lBuilding.getFloors().length + " Floors" + (this.displaySubType == 7 ? " (Showing)" : "") + "\"};");
        buf.append("label{text=\"\"};label{text=\"\"};");
        buf.append("}");
        return buf.toString();
    }

    private String buildingSummary(Structure lBuilding) {
        StringBuilder buf = new StringBuilder();
        buf.append("table{rows=\"18\";cols=\"4\";");
        buf.append("label{text=\"\"};label{text=\"Wurm Id\"};label{text=\"\"};label{text=\"" + this.wurmId + "\"}");
        buf.append("label{text=\"\"};label{text=\"Name\"};label{text=\"\"};label{text=\"" + lBuilding.getName() + "\"};");
        if (lBuilding.getWritId() == -10L) {
            buf.append("label{text=\"\"};label{text=\"WritID\"};label{text=\"\"};label{text=\"" + lBuilding.getWritId() + "\"};");
        } else {
            buf.append("radio{group=\"tid\";id=\"1," + lBuilding.getWritId() + "," + 1 + "\"};label{text=\"WritID\"};label{text=\"\"};label{text=\"" + lBuilding.getWritId() + "\"};");
        }
        PlayerInfo pInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(lBuilding.getOwnerId());
        if (pInfo != null) {
            buf.append("radio{group=\"tid\";id=\"1," + pInfo.wurmId + "," + 1 + "\"};label{text=\"Owner Name\"};label{text=\"\"};label{text=\"" + pInfo.getName() + "\"};");
        } else {
            buf.append("label{text=\"\"};label{text=\"OwnerID\"};label{text=\"\"};label{text=\"" + lBuilding.getOwnerId() + "\"};");
        }
        int sx = lBuilding.getCenterX();
        int sy = lBuilding.getCenterY();
        boolean onSurface = lBuilding.isOnSurface();
        buf.append("radio{group=\"tid\";id=\"3," + sx + "," + sy + "," + onSurface + "\"};label{text=\"Center (X,Y,surface)\"};radio{group=\"tid\";id=\"" + 12 + "," + sx + "," + sy + "," + onSurface + "\"};label{text=\"(" + sx + "," + sy + "," + onSurface + ")\"};");
        buf.append("label{text=\"\"};label{text=\"Creation Date\"};label{text=\"\"};label{text=\"" + new Date(lBuilding.getCreationDate()) + "\"};");
        buf.append("label{text=\"\"};label{text=\"Door Count\"};label{text=\"\"};label{text=\"" + lBuilding.getDoors() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Final Finished\"};label{text=\"\"};label{text=\"" + lBuilding.isFinalFinished() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Finalized\"};label{text=\"\"};label{text=\"" + lBuilding.isFinalized() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Finished\"};label{text=\"\"};label{text=\"" + lBuilding.isFinished() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Limit\"};label{text=\"\"};label{text=\"" + lBuilding.getLimit() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Lockable\"};label{text=\"\"};label{text=\"" + lBuilding.isLockable() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Locked\"};label{text=\"\"};label{text=\"" + lBuilding.isLocked() + "\"};");
        int maxx = lBuilding.getMaxX();
        int maxy = lBuilding.getMaxY();
        buf.append("radio{group=\"tid\";id=\"3," + maxx + "," + maxy + "," + onSurface + "\"};label{text=\"Max (X,Y,surface)\"};radio{group=\"tid\";id=\"" + 12 + "," + maxx + "," + maxy + "," + onSurface + "\"};label{text=\"(" + maxx + "," + maxy + "," + onSurface + ")\"};");
        int minx = lBuilding.getMinX();
        int miny = lBuilding.getMinY();
        buf.append("radio{group=\"tid\";id=\"3," + minx + "," + miny + "," + onSurface + "\"};label{text=\"Min (X,Y,surface)\"};radio{group=\"tid\";id=\"" + 12 + "," + minx + "," + miny + "," + onSurface + "\"};label{text=\"(" + minx + "," + miny + "," + onSurface + ")\"};");
        buf.append("label{text=\"\"};label{text=\"Roof\"};label{text=\"\"};label{text=\"" + lBuilding.getRoof() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Size\"};label{text=\"\"};label{text=\"" + lBuilding.getSize() + "\"};");
        buf.append("label{text=\"\"};label{text=\"HasWalls\"};label{text=\"\"};label{text=\"" + lBuilding.hasWalls() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Planner\"};label{text=\"\"};label{text=\"" + lBuilding.getPlanner() + "\"};");
        Village v = lBuilding.getVillage();
        buf.append("label{text=\"\"};label{text=\"In Settlement\"};label{text=\"\"};label{text=\"" + (v != null ? v.getName() : "-") + "\"};");
        buf.append("}");
        return buf.toString();
    }

    private String showGuests(Permissions.IPermission[] ips, PermissionsPlayerList ppl) {
        StringBuilder buf = new StringBuilder();
        PermissionsByPlayer[] lGuests = ppl.getPermissionsByPlayer();
        if (lGuests.length > 0) {
            buf.append(this.PermissionsByPlayerTable(ips, lGuests));
        } else {
            buf.append("label{text=\"no guests\"};");
        }
        return buf.toString();
    }

    private String showHistory(long objectId) {
        StringBuilder buf = new StringBuilder();
        PermissionsHistoryEntry[] lHistory = PermissionsHistories.getPermissionsHistoryFor(objectId).getHistoryEvents();
        if (lHistory.length > 0) {
            buf.append(this.historyTable(lHistory));
        } else {
            buf.append("label{text=\"no guests\"};");
        }
        return buf.toString();
    }

    private String buildingTiles(Structure lBuilding) {
        StringBuilder buf = new StringBuilder();
        VolaTile[] tiles = lBuilding.getStructureTiles();
        if (tiles.length > 0) {
            buf.append(this.tilesTable(tiles));
        } else {
            buf.append("label{text=\"No Tiles found\"}");
        }
        return buf.toString();
    }

    private String buildingWalls(Structure lBuilding) {
        StringBuilder buf = new StringBuilder();
        Wall[] lWalls = lBuilding.getWalls();
        if (lWalls.length > 0) {
            buf.append(this.wallsTable(lWalls, -1, -1));
        } else {
            buf.append("label{text=\"No Walls found\"}");
        }
        return buf.toString();
    }

    private String buildingFloors(Structure lBuilding) {
        StringBuilder buf = new StringBuilder();
        Floor[] lFloors = lBuilding.getFloors();
        if (lFloors.length > 0) {
            buf.append(this.floorsTable(lFloors));
        } else {
            buf.append("label{text=\"No Floors found\"}");
        }
        return buf.toString();
    }

    private String bridgeDetails(Structure lBridge) {
        StringBuilder buf = new StringBuilder();
        if (this.displaySubType == 1) {
            buf.append(this.bridgeSummary(lBridge));
        } else if (this.displaySubType == 5) {
            buf.append(this.bridgeTiles(lBridge));
        } else if (this.displaySubType == 8) {
            buf.append(this.bridgeParts(lBridge));
        }
        buf.append("label{type=\"bold\";text=\"--------------- Links -------------------\"}");
        buf.append("table{rows=\"2\";cols=\"4\";");
        buf.append("radio{group=\"tid\";id=\"1," + this.wurmId + "," + 1 + "\"};label{text=\"Summary" + (this.displaySubType == 1 ? " (Showing)" : "") + "\"};");
        buf.append((lBridge.getBridgeParts().length == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 8 + "\"};") + "label{text=\"" + lBridge.getBridgeParts().length + " Bridge parts" + (this.displaySubType == 8 ? " (Showing)" : "") + "\"};");
        buf.append((lBridge.getStructureTiles().length == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 5 + "\"};") + "label{text=\"" + lBridge.getStructureTiles().length + " Tiles" + (this.displaySubType == 5 ? " (Showing)" : "") + "\"};");
        buf.append("label{text=\"\"};label{text=\"\"};");
        buf.append("}");
        return buf.toString();
    }

    private String bridgeSummary(Structure lBridge) {
        StringBuilder buf = new StringBuilder();
        buf.append("table{rows=\"10\";cols=\"4\";");
        buf.append("label{text=\"\"};label{text=\"Wurm Id\"};label{text=\"\"};label{text=\"" + this.wurmId + "\"}");
        buf.append("label{text=\"\"};label{text=\"Name\"};label{text=\"\"};label{text=\"" + lBridge.getName() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Creation Date\"};label{text=\"\"};label{text=\"" + new Date(lBridge.getCreationDate()) + "\"};");
        PlayerInfo pInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(lBridge.getOwnerId());
        if (pInfo != null) {
            buf.append("radio{group=\"tid\";id=\"1," + pInfo.wurmId + "," + 1 + "\"};label{text=\"Owner Name\"};label{text=\"\"};label{text=\"" + pInfo.getName() + "\"};");
        } else {
            buf.append("label{text=\"\"};label{text=\"OwnerID\"};label{text=\"\"};label{text=\"" + lBridge.getOwnerId() + "\"};");
        }
        buf.append("label{text=\"\"};label{text=\"Final Finished\"};label{text=\"\"};label{text=\"" + lBridge.isFinalFinished() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Finalized\"};label{text=\"\"};label{text=\"" + lBridge.isFinalized() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Finished\"};label{text=\"\"};label{text=\"" + lBridge.isFinished() + "\"};");
        int maxx = lBridge.getMaxX();
        int maxy = lBridge.getMaxY();
        buf.append("radio{group=\"tid\";id=\"3," + maxx + "," + maxy + ",true\"};label{text=\"Max (X,Y,surface)\"};radio{group=\"tid\";id=\"" + 12 + "," + maxx + "," + maxy + ",true\"};label{text=\"(" + maxx + "," + maxy + ",true)\"};");
        int minx = lBridge.getMinX();
        int miny = lBridge.getMinY();
        buf.append("radio{group=\"tid\";id=\"3," + minx + "," + miny + ",true\"};label{text=\"Min (X,Y,surface)\"};radio{group=\"tid\";id=\"" + 12 + "," + minx + "," + miny + ",true\"};label{text=\"(" + minx + "," + miny + ",true)\"};");
        buf.append("label{text=\"\"};label{text=\"Size\"};label{text=\"\"};label{text=\"" + lBridge.getSize() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Planner\"};label{text=\"\"};label{text=\"" + lBridge.getPlanner() + "\"};");
        buf.append("}");
        return buf.toString();
    }

    private String bridgeTiles(Structure lBridge) {
        StringBuilder buf = new StringBuilder();
        VolaTile[] tiles = lBridge.getStructureTiles();
        if (tiles.length > 0) {
            buf.append(this.tilesTable(tiles));
        } else {
            buf.append("label{text=\"No Tiles found\"}");
        }
        return buf.toString();
    }

    private String bridgeParts(Structure lBridge) {
        StringBuilder buf = new StringBuilder();
        BridgePart[] lBridgeParts = lBridge.getBridgeParts();
        if (lBridgeParts.length > 0) {
            buf.append(this.bridgePartsTable(lBridgeParts, -1, -1));
        } else {
            buf.append("label{text=\"No Bridge Parts found\"}");
        }
        return buf.toString();
    }

    private String tileDetails(int aIdType) {
        StringBuilder buf = new StringBuilder();
        try {
            boolean isOnSurface;
            short tilex = Tiles.decodeTileX(this.wurmId);
            int tiley = Tiles.decodeTileY(this.wurmId);
            boolean bl = isOnSurface = aIdType != 17;
            if (aIdType == 23) {
                Floor floor = RoofFloorEnum.getFloorOrRoofFromId(this.wurmId);
                isOnSurface = floor.isOnSurface();
            }
            if (aIdType == 28) {
                isOnSurface = Tiles.decodeLayer(this.wurmId) == 0;
            }
            WurmMail[] inMail = WurmMail.getAllMail();
            Zone zone = Zones.getZone(tilex, tiley, isOnSurface);
            VolaTile tile = zone.getOrCreateTile(tilex, tiley);
            int numbCreatures = tile.getCreatures().length;
            int numbWalls = tile.getWalls().length;
            int numbFences = tile.getAllFences().length;
            int numbItems = tile.getItems().length;
            int numbDeities = Deities.getDeities().length;
            Structure lStructure = tile.getStructure();
            int numbFloors = lStructure == null ? 0 : lStructure.getFloorsAtTile(tilex, tiley, 0, 9999).length;
            int numbBridgeParts = tile.getBridgeParts().length;
            MineDoorPermission minedoor = MineDoorPermission.getPermission(tilex, tiley);
            Item[] gmSigns = Items.getGMSigns();
            if (this.displaySubType == 1) {
                buf.append(this.tileSummary(tile, aIdType, tilex, tiley, isOnSurface));
            } else if (this.displaySubType == 9) {
                buf.append(this.tileCreatures(tile));
            } else if (this.displaySubType == 6) {
                buf.append(this.tileWalls(tile));
            } else if (this.displaySubType == 10) {
                buf.append(this.tileFences(tile));
            } else if (this.displaySubType == 11) {
                buf.append(this.tileItems(tile));
            } else if (this.displaySubType == 7) {
                buf.append(this.tileFloors(tile));
            } else if (this.displaySubType == 8) {
                buf.append(this.tileBridgeParts(tile));
            } else if (this.displaySubType == 13) {
                buf.append(this.mailItems(inMail));
            } else if (this.displaySubType == 14) {
                buf.append(this.gmSignsTable(gmSigns));
            } else if (minedoor != null && this.displaySubType == 2) {
                buf.append(this.showGuests(MineDoorSettings.MinedoorPermissions.getPermissions(), minedoor.getPermissionsPlayerList()));
            } else if (minedoor != null && this.displaySubType == 3) {
                buf.append(this.showHistory(minedoor.getWurmId()));
            }
            buf.append("label{type=\"bold\";text=\"------------------------------ Links --------------------------------------\"}");
            buf.append("table{rows=\"3\";cols=\"6\";");
            buf.append("radio{group=\"tid\";id=\"1," + this.wurmId + "," + 1 + "\"};label{text=\"Summary" + (this.displaySubType == 1 ? " (Showing)" : "") + "\"};");
            buf.append((numbCreatures == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 9 + "\"};") + "label{text=\"" + numbCreatures + " Creatures" + (this.displaySubType == 9 ? " (Showing)" : "") + "\"};");
            buf.append((numbWalls == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 6 + "\"};") + "label{text=\"" + numbWalls + " Walls" + (this.displaySubType == 6 ? " (Showing)" : "") + "\"};");
            buf.append((numbFences == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 10 + "\"};") + "label{text=\"" + numbFences + " Fences" + (this.displaySubType == 10 ? " (Showing)" : "") + "\"};");
            buf.append((numbItems == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 11 + "\"};") + "label{text=\"" + numbItems + " Items" + (this.displaySubType == 11 ? " (Showing)" : "") + "\"};");
            buf.append((numbFloors == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 7 + "\"};") + "label{text=\"" + numbFloors + " Floors" + (this.displaySubType == 7 ? " (Showing)" : "") + "\"};");
            buf.append((numbBridgeParts == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 8 + "\"};") + "label{text=\"" + numbBridgeParts + " Bridge Parts" + (this.displaySubType == 8 ? " (Showing)" : "") + "\"};");
            try {
                buf.append((inMail.length == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 13 + "\"};") + "label{text=\"" + inMail.length + " Mail Items" + (this.displaySubType == 13 ? " (Showing)" : "") + "\"};");
            }
            catch (Exception e) {
                buf.append("label{text=\"error\"};label{text=\"" + e.getMessage() + "\"};");
            }
            buf.append((gmSigns.length == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 14 + "\"};") + "label{text=\"" + gmSigns.length + " GM Signs" + (this.displaySubType == 14 ? " (Showing)" : "") + "\"};");
            buf.append("radio{group=\"tid\";id=\"14,-10,1\"};label{text=\"Servers\"};");
            buf.append("radio{group=\"tid\";id=\"13,-10,1\"};label{text=\"" + numbDeities + " Deities\"};");
            if (minedoor != null) {
                buf.append((minedoor.getPermissionsPlayerList().size() == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 2 + "\"};") + "label{text=\"" + minedoor.getPermissionsPlayerList().size() + " Minedoor Guests" + (this.displaySubType == 2 ? " (Showing)" : "") + "\"};");
                buf.append((PermissionsHistories.getPermissionsHistoryFor(minedoor.getWurmId()).getHistoryEvents().length == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 3 + "\"};") + "label{text=\"" + PermissionsHistories.getPermissionsHistoryFor(minedoor.getWurmId()).getHistoryEvents().length + " Minedoor History" + (this.displaySubType == 3 ? " (Showing)" : "") + "\"};");
            }
            if (Features.Feature.HIGHWAYS.isEnabled()) {
                buf.append("radio{group=\"tid\";id=\"15,-10,19\"};label{text=\"Highway Routes" + (this.displaySubType == 19 ? " (Showing)" : "") + "\"};");
                buf.append("radio{group=\"tid\";id=\"15,-10,21\"};label{text=\"Highway Nodes" + (this.displaySubType == 21 ? " (Showing)" : "") + "\"};");
            }
            buf.append("label{text=\"\"};label{text=\"\"};");
            buf.append("label{text=\"\"};label{text=\"\"};");
            buf.append("}");
        }
        catch (NoSuchZoneException e) {
            logger.log(Level.WARNING, "Error: " + e.getMessage(), e);
            buf.append("label{text=\"Error - No such zone\"};");
            buf.append("label{text=\"Exception:\"};");
            buf.append("label{text=\"" + e.getMessage() + "\"};");
        }
        catch (RuntimeException e) {
            logger.log(Level.WARNING, "Error: " + e.getMessage(), e);
            buf.append("label{text=\"Exception:\"};");
            buf.append("label{text=\"" + e.getMessage() + "\"};");
        }
        catch (Exception e) {
            logger.log(Level.WARNING, "Error: " + e.getMessage(), e);
            buf.append("label{text=\"(Catch all) Exception:\"};");
            buf.append("label{text=\"" + e.getMessage() + "\"};");
        }
        return buf.toString();
    }

    private String tileSummary(VolaTile tile, int aIdType, int tilex, int tiley, boolean isOnSurface) {
        int meshtile;
        int resource;
        StringBuilder buf = new StringBuilder();
        if (isOnSurface) {
            resource = Server.getWorldResource(tilex, tiley);
            meshtile = Server.surfaceMesh.getTile(tilex, tiley);
        } else {
            resource = Server.getCaveResource(tilex, tiley);
            meshtile = Server.caveMesh.getTile(tilex, tiley);
        }
        byte tileType = Tiles.decodeType(meshtile);
        byte data = Tiles.decodeData(meshtile);
        int rows = 7;
        Structure lStructure = tile.getStructure();
        if (lStructure != null) {
            rows += 2;
        }
        Village lVillage = tile.getVillage();
        Village perimVillage = null;
        if (lVillage != null) {
            ++rows;
        } else {
            perimVillage = Villages.getVillageWithPerimeterAt(tile.getTileX(), tile.getTileY(), true);
        }
        if (perimVillage != null) {
            ++rows;
        }
        buf.append("table{rows=\"" + rows + "\";cols=\"4\";");
        buf.append("label{text=\"\"};label{text=\"Wurm Id\"};label{text=\"\"};label{text=\"" + this.wurmId + "\"}");
        buf.append("label{text=\"\"};label{text=\"Layer\"};label{text=\"\"};label{text=\"" + tile.getLayer() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Tile type\"};label{text=\"\"};label{text=\"" + Tiles.getTile(tileType).getName() + "(" + (tileType & 0xFF) + ")\"};");
        if (aIdType == 23) {
            Floor floor = RoofFloorEnum.getFloorOrRoofFromId(this.wurmId);
            buf.append("label{text=\"\"};label{text=\"Floor type\"};label{text=\"\"};label{text=\"" + floor.getType().getName() + " (" + floor.getType().getCode() + ")\"};");
            buf.append("label{text=\"\"};label{text=\"Floor material\"};label{text=\"\"};label{text=\"" + floor.getMaterial().getName() + " (" + floor.getMaterial().getCode() + ")\"};");
            buf.append("label{text=\"\"};label{text=\"Floor QL\"};label{text=\"\"};label{text=\"" + floor.getCurrentQL() + "\"};");
            buf.append("label{text=\"\"};label{text=\"Floor Damage\"};label{text=\"\"};label{text=\"" + floor.getDamage() + "\"};");
            buf.append("label{text=\"\"};label{text=\"Floor Dir\"};label{text=\"\"};label{text=\"" + floor.getDir() + "\"};");
            buf.append("label{text=\"\"};label{text=\"Floor Level\"};label{text=\"\"};label{text=\"" + floor.getFloorLevel() + "\"};");
            buf.append("label{text=\"\"};label{text=\"Floor Height Offset\"};label{text=\"\"};label{text=\"" + floor.getHeightOffset() + "\"};");
        } else if (aIdType == 28) {
            BridgePart bridgePart = BridgePartEnum.getBridgePartFromId(this.wurmId);
            buf.append("label{text=\"\"};label{text=\"Bridge Part type\"};label{text=\"\"};label{text=\"" + bridgePart.getType().getName() + " (" + bridgePart.getType().getCode() + ")\"};");
            buf.append("label{text=\"\"};label{text=\"Bridge Part material\"};label{text=\"\"};label{text=\"" + bridgePart.getMaterial().getName() + " (" + bridgePart.getMaterial().getCode() + ")\"};");
            buf.append("label{text=\"\"};label{text=\"Bridge Part QL\"};label{text=\"\"};label{text=\"" + bridgePart.getCurrentQL() + "\"};");
            buf.append("label{text=\"\"};label{text=\"Bridge Part Damage\"};label{text=\"\"};label{text=\"" + bridgePart.getDamage() + "\"};");
            buf.append("label{text=\"\"};label{text=\"Bridge Part Dir\"};label{text=\"\"};label{text=\"" + bridgePart.getDir() + "\"};");
            buf.append("label{text=\"\"};label{text=\"Bridge Part Slope\"};label{text=\"\"};label{text=\"" + bridgePart.getSlope() + "\"};");
            buf.append("label{text=\"\"};label{text=\"Bridge Part Height\"};label{text=\"\"};label{text=\"" + bridgePart.getHeight() + " (" + bridgePart.getHeight() + ")\"};");
            String roadSurface = "default ";
            if (bridgePart.getRoadType() != 0) {
                Tiles.Tile roadTile = Tiles.getTile(bridgePart.getRoadType());
                roadSurface = roadTile.getDesc() + " ";
            }
            buf.append("label{text=\"\"};label{text=\"Bridge Part Road\"};label{text=\"\"};label{text=\"" + roadSurface + "(" + bridgePart.getRoadType() + ")\"};");
            buf.append("label{text=\"\"};label{text=\"Last Maintained\"};label{text=\"\"};label{text=\"" + new Date(bridgePart.getLastUsed()) + "\"};");
        } else {
            if (Tiles.getTile(tileType).isBush() || Tiles.getTile(tileType).isTree()) {
                buf.append("label{text=\"\"};label{text=\"Resource:Damage\"};label{text=\"\"};label{text=\"" + resource + "\"};");
            } else if (tileType == Tiles.Tile.TILE_FIELD.getIntId() || tileType == Tiles.Tile.TILE_FIELD2.getIntId()) {
                buf.append("label{text=\"\"};label{text=\"Resource:Farmed|Chance\"};label{text=\"\"};label{text=\"" + (resource >>> 11 & 0x1F) + "|" + (resource & 0x3FF) + "(Max:" + 1023 + ")\"};");
            } else if (tileType == Tiles.Tile.TILE_CLAY.getIntId()) {
                buf.append("label{text=\"\"};label{text=\"Resource:Dug count\"};label{text=\"\"};label{text=\"" + resource + "\"};");
            } else if (tileType == Tiles.Tile.TILE_MINE_DOOR_WOOD.getIntId() || tileType == Tiles.Tile.TILE_MINE_DOOR_STONE.getIntId() || tileType == Tiles.Tile.TILE_MINE_DOOR_GOLD.getIntId() || tileType == Tiles.Tile.TILE_MINE_DOOR_SILVER.getIntId() || tileType == Tiles.Tile.TILE_MINE_DOOR_STEEL.getIntId()) {
                buf.append("label{text=\"\"};label{text=\"Resource:Door Strength\"};label{text=\"\"};label{text=\"" + resource + "\"};");
            } else {
                buf.append("label{text=\"\"};label{text=\"Resource:\"};label{text=\"\"};label{text=\"" + resource + "\"};");
            }
            if (tileType == Tiles.Tile.TILE_GRASS.getIntId()) {
                buf.append("label{text=\"\"};label{text=\"Data:Growth|Flower\"};label{text=\"\"};label{text=\"" + GrassData.GrowthStage.decodeTileData(data).name().toLowerCase() + "|" + GrassData.FlowerType.decodeTileData(data).name().toLowerCase() + "\"};");
            } else if (tileType == Tiles.Tile.TILE_KELP.getIntId() || tileType == Tiles.Tile.TILE_REED.getIntId()) {
                buf.append("label{text=\"\"};label{text=\"Data:Growth\"};label{text=\"\"};label{text=\"" + GrassData.GrowthStage.decodeTileData(data).name().toLowerCase() + "\"};");
            } else if (tileType == Tiles.Tile.TILE_TREE.getIntId() || tileType == Tiles.Tile.TILE_ENCHANTED_TREE.getIntId() || tileType == Tiles.Tile.TILE_MYCELIUM_TREE.getIntId()) {
                buf.append("label{text=\"\"};label{text=\"Data:Age|Type\"};label{text=\"\"};label{text=\"" + FoliageAge.getFoliageAge(data).getAgeName() + "(" + (data >>> 4 & 0xF) + ")|" + TreeData.getTypeName((byte)(data & 0xF)) + "(" + (data & 0xF) + ")\"};");
            } else if (tileType == Tiles.Tile.TILE_BUSH.getIntId() || tileType == Tiles.Tile.TILE_ENCHANTED_BUSH.getIntId() || tileType == Tiles.Tile.TILE_MYCELIUM_BUSH.getIntId()) {
                buf.append("label{text=\"\"};label{text=\"Data:Age|Type\"};label{text=\"\"};label{text=\"" + FoliageAge.getFoliageAge(data).getAgeName() + "(" + (data >>> 4 & 0xF) + ")|" + BushData.getTypeName((byte)(data & 0xF)) + "(" + (data & 0xF) + ")\"};");
            } else if (Tiles.getTile(tileType).isTree() || Tiles.getTile(tileType).isBush()) {
                buf.append("label{text=\"\"};label{text=\"Data:Age|hasFruit|inCentre|GrassLength\"};label{text=\"\"};label{text=\"" + FoliageAge.getFoliageAge(data).getAgeName() + "(" + (data >>> 4 & 0xF) + ")|" + ((data & 8) == 0 ? "no Fruit" : "has Fruit") + "|" + ((data & 4) == 0 ? "Natural" : "In Centre") + "|" + GrassData.GrowthTreeStage.fromInt(data & 3).name() + "\"};");
            } else if (tileType == Tiles.Tile.TILE_FIELD.getIntId() || tileType == Tiles.Tile.TILE_FIELD2.getIntId()) {
                buf.append("label{text=\"\"};label{text=\"Data:Tended|Age|Crop\"};label{text=\"\"};label{text=\"" + Crops.decodeFieldState(data) + "|" + Crops.decodeFieldAge(data) + "|" + Crops.getCropName(Crops.getCropNumber(tileType, data)) + "(" + (data & 0xF) + ")\"};");
            } else {
                buf.append("label{text=\"\"};label{text=\"Data:\"};label{text=\"\"};label{text=\"" + data + "\"};");
            }
        }
        if (lStructure != null) {
            buf.append("label{text=\"\"};label{text=\"Structure ID\"};label{text=\"\"};label{text=\"" + lStructure.getWurmId() + "\"};");
            buf.append("radio{group=\"tid\";id=\"1," + lStructure.getWurmId() + "," + 1 + "\"};label{text=\"Structure Name\"};label{text=\"\"};label{text=\"" + lStructure.getName() + "\"};");
        }
        buf.append("label{text=\"\"};label{text=\"Kingdom\"};label{text=\"\"};label{text=\"" + Kingdoms.getNameFor(tile.getKingdom()) + "(" + tile.getKingdom() + ")\"};");
        if (lVillage != null) {
            buf.append("radio{group=\"tid\";id=\"2," + lVillage.getId() + "," + 1 + "\"};label{text=\"Village Name\"};label{text=\"\"};label{text=\"" + lVillage.getName() + "\"};");
        } else if (perimVillage != null) {
            buf.append("radio{group=\"tid\";id=\"2," + perimVillage.getId() + "," + 1 + "\"};label{text=\"Perimeter Village Name\"};label{text=\"\"};label{text=\"" + perimVillage.getName() + "\"};");
        }
        int tx = tile.getTileX();
        int ty = tile.getTileY();
        buf.append("radio{group=\"tid\";id=\"3," + tx + "," + ty + "," + isOnSurface + "\"};label{text=\"Coord (X,Y,surface)\"};radio{group=\"tid\";id=\"" + 12 + "," + tx + "," + ty + "," + isOnSurface + "\"};label{text=\"(" + tx + "," + ty + "," + isOnSurface + ")\"};");
        if (isOnSurface) {
            for (int x = 0; x <= 1; ++x) {
                for (int y = 0; y <= 1; ++y) {
                    meshtile = Server.surfaceMesh.getTile(tilex + x, tiley + y);
                    short tileHeight = Tiles.decodeHeight(meshtile);
                    int rockTile = Server.rockMesh.getTile(tilex + x, tiley + y);
                    short rockHeight = Tiles.decodeHeight(rockTile);
                    buf.append("label{text=\"\"};label{text=\"" + (y == 0 ? "N" : "S") + (x == 0 ? "W" : "E") + " Surface,Rock Heights\"};label{text=\"\"};label{text=\"" + tileHeight + "," + rockHeight + "\"};");
                }
            }
        } else {
            for (int x = 0; x <= 1; ++x) {
                for (int y = 0; y <= 1; ++y) {
                    int currtile = Server.caveMesh.getTile(tilex + x, tiley + y);
                    short currHeight = Tiles.decodeHeight(currtile);
                    short cceil = (short)(Tiles.decodeData(currtile) & 0xFF);
                    buf.append("label{text=\"\"};label{text=\"" + (y == 0 ? "N" : "S") + (x == 0 ? "W" : "E") + " Cave floor, Ceiling gap (Ceiling height)\"};label{text=\"\"};label{text=\"" + currHeight + "," + cceil + " (" + (currHeight + cceil) + ")\"};");
                }
            }
        }
        buf.append("}");
        return buf.toString();
    }

    private String tileCreatures(VolaTile tile) {
        StringBuilder buf = new StringBuilder();
        try {
            Creature[] lCreatures = tile.getCreatures();
            if (lCreatures.length > 0) {
                buf.append(this.creaturesTable(lCreatures));
            } else {
                buf.append("label{text=\"No Creatures found\"}");
            }
        }
        catch (Exception e1) {
            logger.log(Level.WARNING, e1.getMessage(), e1);
            buf.append("label{text=\"Error reading Creatures\"}");
            buf.append("label{text=\"Exception:\"};");
            buf.append("label{text=\"" + e1.getMessage() + "\"};");
        }
        return buf.toString();
    }

    private String tileWalls(VolaTile tile) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{type=\"italic\";text=\"Only returns walls if tile is in a structure (atm)\"}");
        try {
            Wall[] lWalls = tile.getWalls();
            if (lWalls.length > 0) {
                buf.append(this.wallsTable(lWalls, tile.getTileX(), tile.getTileY()));
            } else {
                buf.append("label{text=\"No Walls found\"}");
            }
        }
        catch (Exception e1) {
            logger.log(Level.WARNING, e1.getMessage(), e1);
            buf.append("label{text=\"Error reading Walls\"}");
            buf.append("label{text=\"Exception:\"};");
            buf.append("label{text=\"" + e1.getMessage() + "\"};");
        }
        return buf.toString();
    }

    private String tileFences(VolaTile tile) {
        StringBuilder buf = new StringBuilder();
        try {
            Fence[] lFences = tile.getAllFences();
            if (lFences.length > 0) {
                buf.append(this.fencesTable(lFences, tile.getTileX(), tile.getTileY()));
            } else {
                buf.append("label{text=\"No Fences found\"}");
            }
        }
        catch (Exception e1) {
            logger.log(Level.WARNING, e1.getMessage(), e1);
            buf.append("label{text=\"Error reading Fences\"}");
            buf.append("label{text=\"Exception:\"};");
            buf.append("label{text=\"" + e1.getMessage() + "\"};");
        }
        return buf.toString();
    }

    private String tileItems(VolaTile tile) {
        StringBuilder buf = new StringBuilder();
        try {
            Item[] litems = tile.getItems();
            if (litems.length > 0) {
                buf.append(this.itemsTable(litems));
            } else {
                buf.append("label{text=\"No Items found\"}");
            }
        }
        catch (Exception e1) {
            logger.log(Level.WARNING, e1.getMessage(), e1);
            buf.append("label{text=\"Error reading Items\"}");
            buf.append("label{text=\"Exception:\"};");
            buf.append("label{text=\"" + e1.getMessage() + "\"};");
        }
        return buf.toString();
    }

    private String tileFloors(VolaTile tile) {
        StringBuilder buf = new StringBuilder();
        Structure lStructure = tile.getStructure();
        try {
            Floor[] lfloors = lStructure.getFloorsAtTile(tile.getTileX(), tile.getTileY(), 0, 9999);
            if (lfloors.length > 0) {
                buf.append(this.floorsTable(lfloors));
            } else {
                buf.append("label{text=\"No floors found\"}");
            }
        }
        catch (Exception e1) {
            logger.log(Level.WARNING, e1.getMessage(), e1);
            buf.append("label{text=\"Error reading Floors\"}");
            buf.append("label{text=\"Exception:\"};");
            buf.append("label{text=\"" + e1.getMessage() + "\"};");
        }
        return buf.toString();
    }

    private String tileBridgeParts(VolaTile tile) {
        StringBuilder buf = new StringBuilder();
        try {
            BridgePart[] lbridgeParts = tile.getBridgeParts();
            if (lbridgeParts.length > 0) {
                buf.append(this.bridgePartTable(lbridgeParts));
            } else {
                buf.append("label{text=\"No bridge parts found\"}");
            }
        }
        catch (Exception e1) {
            logger.log(Level.WARNING, e1.getMessage(), e1);
            buf.append("label{text=\"Error reading Bridge Parts\"}");
            buf.append("label{text=\"Exception:\"};");
            buf.append("label{text=\"" + e1.getMessage() + "\"};");
        }
        return buf.toString();
    }

    private String creatureDetails() {
        StringBuilder buf = new StringBuilder();
        Creature creature = null;
        Item inventory = null;
        Body body = null;
        int numbInventory = 0;
        int numbBody = 0;
        int numbTraits = 0;
        try {
            creature = Creatures.getInstance().getCreature(this.wurmId);
            inventory = creature.getInventory();
            numbInventory = inventory.getItems().size();
            body = creature.getBody();
            numbBody = body.getAllItems().length;
            numbTraits = creature.getStatus().traitsCount();
            if (this.displaySubType == 1) {
                buf.append(this.creatureSummary(creature));
            } else if (this.displaySubType == 2) {
                buf.append(this.showGuests(AnimalSettings.Animal2Permissions.getPermissions(), creature.getPermissionsPlayerList()));
            } else if (this.displaySubType == 3) {
                buf.append(this.showHistory(creature.getWurmId()));
            } else if (this.displaySubType == 8) {
                buf.append(this.containerItems(inventory));
            } else if (this.displaySubType == 9) {
                buf.append(this.containerItems(body.getBodyItem()));
            } else {
                buf.append(this.creatureTraits(creature));
            }
        }
        catch (NoSuchCreatureException e) {
            buf.append("label{text=\"no creature found with that Id\"}");
        }
        buf.append("label{type=\"bold\";text=\"------------------------------ Links --------------------------------------\"}");
        buf.append("table{rows=\"2\";cols=\"4\";");
        buf.append("radio{group=\"tid\";id=\"1," + this.wurmId + "," + 1 + "\"};label{text=\"Summary" + (this.displaySubType == 1 ? " (Showing)" : "") + "\"};");
        if (creature != null) {
            buf.append((creature.getPermissionsPlayerList().size() == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 2 + "\"};") + "label{text=\"" + creature.getPermissionsPlayerList().size() + " Guests" + (this.displaySubType == 2 ? " (Showing)" : "") + "\"};");
            buf.append((PermissionsHistories.getPermissionsHistoryFor(creature.getWurmId()).getHistoryEvents().length == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 3 + "\"};") + "label{text=\"" + PermissionsHistories.getPermissionsHistoryFor(creature.getWurmId()).getHistoryEvents().length + " History" + (this.displaySubType == 3 ? " (Showing)" : "") + "\"};");
        }
        if (inventory != null) {
            buf.append((numbInventory == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 8 + "\"};") + "label{text=\"" + numbInventory + " Inventory" + (this.displaySubType == 8 ? " (Showing)" : "") + "\"};");
        } else {
            buf.append("label{text=\"error\"};label{text=\"inventory\"};");
        }
        if (body != null) {
            buf.append((numbBody == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 9 + "\"};") + "label{text=\"" + numbBody + " Body" + (this.displaySubType == 9 ? " (Showing)" : "") + "\"};");
        } else {
            buf.append("label{text=\"error\"};label{text=\"body\"};");
        }
        buf.append((numbTraits == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 12 + "\"};") + "label{text=\"" + numbTraits + " Traits" + (this.displaySubType == 12 ? " (Showing)" : "") + "\"};");
        buf.append("label{text=\"\"};label{text=\"\"};");
        buf.append("}");
        return buf.toString();
    }

    private String creatureSummary(Creature creature) {
        Brand brand;
        StringBuilder buf = new StringBuilder();
        int rows = 6;
        if (creature.isCaredFor()) {
            ++rows;
        }
        if ((brand = Creatures.getInstance().getBrand(this.wurmId)) != null) {
            ++rows;
        }
        Shop shop = null;
        if (creature.isNpcTrader() && (shop = Economy.getEconomy().getShop(creature)) != null) {
            rows += 2;
            if (shop != null && shop.isPersonal()) {
                ++rows;
            }
        }
        buf.append("table{rows=\"" + rows + "\";cols=\"4\";");
        buf.append("label{text=\"\"};label{text=\"Wurm Id\"};label{text=\"\"};label{text=\"" + this.wurmId + "\"}");
        buf.append("label{text=\"\"};label{text=\"Name\"};label{text=\"\"};label{text=\"" + creature.getName() + "\"}");
        buf.append("label{text=\"\"};label{text=\"Template Name\"};label{text=\"\"};label{text=\"" + creature.getTemplate().getName() + "\"}");
        int cx = creature.getTileX();
        int cy = creature.getTileY();
        boolean cs = creature.isOnSurface();
        buf.append("radio{group=\"tid\";id=\"3," + cx + "," + cy + "," + cs + "\"};label{text=\"Coords (X,Y,surface)\"};radio{group=\"tid\";id=\"" + 12 + "," + cx + "," + cy + "," + cs + "\"};label{text=\"(" + cx + "," + cy + "," + cs + ")\"}");
        buf.append("label{text=\"\"};label{text=\"Actual (X,Y,Z)\"};label{text=\"\"};label{text=\"(" + creature.getPosX() + "," + creature.getPosY() + "," + creature.getPositionZ() + ")\"}");
        buf.append("label{text=\"\"};label{text=\"Rotation\"};label{text=\"\"};label{text=\"(" + creature.getStatus().getRotation() + ")\"}");
        buf.append("label{text=\"\"};label{text=\"Sex\"};label{text=\"\"};");
        if (creature.getSex() == 0) {
            buf.append("label{text=\"Male\"}");
        } else if (creature.getSex() == 1) {
            buf.append("label{text=\"Female\"}");
        } else {
            buf.append("label{type=\"italic\";text=\"Unknown!\"}");
        }
        if (brand != null) {
            try {
                Village brandVillage = Villages.getVillage((int)brand.getBrandId());
                buf.append("radio{group=\"tid\";id=\"2," + brandVillage.getId() + "," + 1 + "\"};label{text=\"Branded by Village\"};label{text=\"\"};label{text=\"" + brandVillage.getName() + "\"}");
            }
            catch (NoSuchVillageException e) {
                buf.append("label{text=\"\"};label{text=\"Branded by Village Id\"};label{text=\"\"};label{text=\"" + brand.getBrandId() + "\"}");
            }
        }
        buf.append("label{text=\"\"};label{text=\"Age\"};label{text=\"\"};label{text=\"" + creature.getStatus().getAgeString() + " (" + creature.getStatus().age + ")\"}");
        buf.append("label{text=\"\"};label{text=\"Carried Weight\"};label{text=\"\"};label{text=\"" + creature.getCarriedWeight() + "\"}");
        buf.append("label{text=\"\"};label{text=\"Kingdom\"};label{text=\"\"};label{text=\"" + Kingdoms.getNameFor(creature.getKingdomId()) + " (" + creature.getKingdomId() + ")\"}");
        if (creature.isCaredFor()) {
            PlayerInfo pInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(creature.getCareTakerId());
            if (pInfo != null) {
                buf.append("radio{group=\"tid\";id=\"1," + pInfo.wurmId + "," + 1 + "\"};label{text=\"Cared For By Name\"};label{text=\"\"};label{text=\"" + pInfo.getName() + "\"};");
            } else {
                buf.append("label{text=\"\"};label{text=\"Cared For By Id\"};label{text=\"\"};label{text=\"" + creature.getCareTakerId() + "\"};");
            }
        }
        buf.append(this.addBridge(creature.getBridgeId()));
        Vehicle hitch = creature.getHitched();
        if (hitch != null) {
            buf.append("radio{group=\"tid\";id=\"1," + hitch.wurmid + "," + 1 + "\"};label{text=\"Hitched To\"};label{text=\"\"};label{text=\"" + hitch.name.replace("\"", "'") + "\"}");
        } else if (creature.vehicle != -10L) {
            Vehicle vehic = null;
            String stype = "";
            if (WurmId.getType(creature.vehicle) == 1) {
                try {
                    Creature creat = Server.getInstance().getCreature(creature.vehicle);
                    vehic = Vehicles.getVehicle(creat);
                    Seat seat = vehic.getSeatFor(creature.getWurmId());
                    stype = seat.getType() == 0 ? "Commander of" : "Passenger of";
                }
                catch (NoSuchPlayerException | NoSuchCreatureException e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            } else {
                try {
                    Item vehicle = Items.getItem(creature.vehicle);
                    vehic = Vehicles.getVehicle(vehicle);
                    Seat seat = vehic.getSeatFor(creature.getWurmId());
                    stype = seat.getType() == 0 ? "Commander of" : (vehicle.isChair() ? "Sitting on" : "Passenger of");
                }
                catch (NoSuchItemException e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                }
            }
            if (stype.length() > 0) {
                buf.append("radio{group=\"tid\";id=\"1," + vehic.wurmid + "," + 1 + "\"};label{text=\"" + stype + "\"};label{text=\"\"};label{text=\"" + vehic.name.replace("\"", "'") + "\"}");
            }
        }
        if (shop != null) {
            buf.append("label{text=\"\"};label{text=\"# items\"};label{text=\"\"};label{text=\"" + shop.getNumberOfItems() + "\"}");
            long millis = shop.howLongEmpty();
            long secsAll = TimeUnit.MILLISECONDS.toSeconds(millis);
            long minsAll = TimeUnit.SECONDS.toMinutes(secsAll);
            long hoursAll = TimeUnit.MINUTES.toHours(minsAll);
            long days = TimeUnit.HOURS.toDays(hoursAll);
            long secs = secsAll - TimeUnit.MINUTES.toSeconds(minsAll);
            long mins = minsAll - TimeUnit.HOURS.toMinutes(hoursAll);
            long hours = hoursAll - TimeUnit.DAYS.toHours(days);
            buf.append("label{text=\"\"};label{text=\"When Empty\"};label{text=\"\"};label{text=\"" + days + " days, " + hours + " hours, " + mins + " mins and " + secs + " secs ago\"}");
            if (shop.isPersonal()) {
                Item item = Items.findMerchantContractFromId(creature.getWurmId());
                if (item == null) {
                    buf.append("label{text=\"\"};label{text=\"Contract Id\"};label{text=\"\"};label{text=\"Not Found\"}");
                } else {
                    buf.append("radio{group=\"tid\";id=\"1," + item.getWurmId() + "," + 1 + "\"};label{text=\"Contract Id\"};label{text=\"\"};label{text=\"" + item.getName() + "\"}");
                }
            }
        }
        buf.append("label{text=\"\"};label{text=\"Hunger\"};label{text=\"\"};label{text=\"" + creature.getStatus().getHunger() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Thirst\"};label{text=\"\"};label{text=\"" + creature.getStatus().getThirst() + "\"};");
        buf.append("label{text=\"\"};label{text=\"Nutrition\"};label{text=\"\"};label{text=\"" + creature.getStatus().getNutritionlevel() + "\"};");
        buf.append("}");
        return buf.toString();
    }

    private String creatureTraits(Creature creature) {
        StringBuilder buf = new StringBuilder();
        for (int x = 0; x < 64; ++x) {
            String colour = "";
            if (Traits.isTraitNegative(x)) {
                colour = "color=\"255,66,66\";";
            } else if (!Traits.isTraitNeutral(x)) {
                colour = "color=\"66,255,66\";";
            }
            if (creature.getStatus().isTraitBitSet(x) && Traits.getTraitString(x).length() > 0) {
                buf.append("label{" + colour + "text=\"" + Traits.getTraitString(x) + "\"};");
                continue;
            }
            if (x == 15 && creature.getStatus().isTraitBitSet(x)) {
                buf.append("label{" + colour + "text=\"Colour: " + StringUtilities.raiseFirstLetterOnly(creature.getColourName(x)) + "\"};");
                continue;
            }
            if (x == 16 && creature.getStatus().isTraitBitSet(x)) {
                buf.append("label{" + colour + "text=\"Colour: " + StringUtilities.raiseFirstLetterOnly(creature.getColourName(x)) + "\"};");
                continue;
            }
            if (x == 17 && creature.getStatus().isTraitBitSet(x)) {
                buf.append("label{" + colour + "text=\"Colour: " + StringUtilities.raiseFirstLetterOnly(creature.getColourName(x)) + "\"};");
                continue;
            }
            if (x == 18 && creature.getStatus().isTraitBitSet(x)) {
                buf.append("label{" + colour + "text=\"Colour: " + StringUtilities.raiseFirstLetterOnly(creature.getColourName(x)) + "\"};");
                continue;
            }
            if (x == 24 && creature.getStatus().isTraitBitSet(x)) {
                buf.append("label{" + colour + "text=\"Colour: " + StringUtilities.raiseFirstLetterOnly(creature.getColourName(x)) + "\"};");
                continue;
            }
            if (x == 25 && creature.getStatus().isTraitBitSet(x)) {
                buf.append("label{" + colour + "text=\"Colour: " + StringUtilities.raiseFirstLetterOnly(creature.getColourName(x)) + "\"};");
                continue;
            }
            if (x == 23 && creature.getStatus().isTraitBitSet(x)) {
                buf.append("label{" + colour + "text=\"Colour: " + StringUtilities.raiseFirstLetterOnly(creature.getColourName(x)) + "\"};");
                continue;
            }
            if (x == 30 && creature.getStatus().isTraitBitSet(x)) {
                buf.append("label{" + colour + "text=\"Colour: " + StringUtilities.raiseFirstLetterOnly(creature.getColourName(x)) + "\"};");
                continue;
            }
            if (x == 31 && creature.getStatus().isTraitBitSet(x)) {
                buf.append("label{" + colour + "text=\"Colour: " + StringUtilities.raiseFirstLetterOnly(creature.getColourName(x)) + "\"};");
                continue;
            }
            if (x == 32 && creature.getStatus().isTraitBitSet(x)) {
                buf.append("label{" + colour + "text=\"Colour: " + StringUtilities.raiseFirstLetterOnly(creature.getColourName(x)) + "\"};");
                continue;
            }
            if (x == 33 && creature.getStatus().isTraitBitSet(x)) {
                buf.append("label{" + colour + "text=\"Colour: " + StringUtilities.raiseFirstLetterOnly(creature.getColourName(x)) + "\"};");
                continue;
            }
            if (x != 34 || !creature.getStatus().isTraitBitSet(x)) continue;
            buf.append("label{" + colour + "text=\"Colour: " + StringUtilities.raiseFirstLetterOnly(creature.getColourName(x)) + "\"};");
        }
        return buf.toString();
    }

    private String itemDetails() {
        StringBuilder buf = new StringBuilder();
        Item item = null;
        try {
            item = Items.getItem(this.wurmId);
            if (this.displaySubType == 1) {
                buf.append(this.itemSummary(item));
            } else if (this.displaySubType == 2) {
                buf.append(this.showGuests(ItemSettings.GMItemPermissions.getPermissions(), item.getPermissionsPlayerList()));
            } else if (this.displaySubType == 3) {
                buf.append(this.showHistory(item.getWurmId()));
            } else {
                buf.append(this.containerItems(item));
            }
        }
        catch (NoSuchItemException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            buf.append("label{text=\"Error - no such Item\"}");
            buf.append("label{text=\"Exception:\"};");
            buf.append("label{text=\"" + e.getMessage() + "\"};");
        }
        buf.append("label{type=\"bold\";text=\"------------------------------ Links --------------------------------------\"}");
        boolean rows = true;
        buf.append("table{rows=\"1\";cols=\"4\";");
        buf.append("radio{group=\"tid\";id=\"1," + this.wurmId + "," + 1 + "\"};label{text=\"Summary" + (this.displaySubType == 1 ? " (Showing)" : "") + "\"};");
        if (item != null) {
            buf.append((item.getPermissionsPlayerList().size() == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 2 + "\"};") + "label{text=\"" + item.getPermissionsPlayerList().size() + " Guests" + (this.displaySubType == 2 ? " (Showing)" : "") + "\"};");
            buf.append((PermissionsHistories.getPermissionsHistoryFor(item.getWurmId()).getHistoryEvents().length == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 3 + "\"};") + "label{text=\"" + PermissionsHistories.getPermissionsHistoryFor(item.getWurmId()).getHistoryEvents().length + " History" + (this.displaySubType == 3 ? " (Showing)" : "") + "\"};");
            buf.append((item.getItemsAsArray().length == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 11 + "\"};") + "label{text=\"" + item.getItemsAsArray().length + " Items" + (this.displaySubType == 11 ? " (Showing)" : "") + "\"};");
        } else {
            buf.append("label{text=\"error\"};label{text=\"\"};");
        }
        if (Features.Feature.HIGHWAYS.isEnabled()) {
            buf.append("radio{group=\"tid\";id=\"15,-10,19\"};label{text=\"Highway Routes" + (this.displaySubType == 19 ? " (Showing)" : "") + "\"};");
            buf.append("radio{group=\"tid\";id=\"15,-10,21\"};label{text=\"Highway Nodes" + (this.displaySubType == 21 ? " (Showing)" : "") + "\"};");
        }
        buf.append("}");
        return buf.toString();
    }

    private String itemSummary(Item item) {
        long wurmcurrent;
        long wurmdiff;
        PlayerInfo pInfo;
        StringBuilder buf = new StringBuilder();
        Item lock = null;
        long lockid = -10L;
        int rows = 17;
        if (item.isVillageDeed()) {
            rows += 2;
        }
        if (item.isLockable()) {
            ++rows;
            lockid = item.getLockId();
            try {
                lock = Items.getItem(lockid);
            }
            catch (NoSuchItemException nsi) {
                lock = null;
            }
        }
        if (item.getTemplateId() == 300) {
            ++rows;
        }
        if (item.getTemplateId() == 166) {
            ++rows;
        }
        buf.append("table{rows=\"" + rows + "\";cols=\"4\";");
        buf.append("label{text=\"\"};label{text=\"Wurm Id\"};label{text=\"\"};label{text=\"" + this.wurmId + "\"}");
        buf.append("label{text=\"\"};label{text=\"Name\"};label{text=\"\"};" + GmTool.itemNameWithColorByRarity(item));
        buf.append("label{text=\"\"};label{text=\"QL\"};label{text=\"\"};label{text=\"" + item.getQualityLevel() + "\"}");
        buf.append("label{text=\"\"};label{text=\"DMG\"};label{text=\"\"};label{text=\"" + item.getDamage() + "\"}");
        buf.append("label{text=\"\"};label{text=\"Size (X,Y,Z)\"};label{text=\"\"};label{text=\"(" + item.getSizeX() + "," + item.getSizeY() + "," + item.getSizeZ() + ")\"}");
        if (item.getParentId() == -10L) {
            buf.append("label{text=\"\"};label{text=\"Rotation (degrees)\"};label{text=\"\"};label{text=\"" + item.getRotation() + "\"}");
        }
        if (item.getOwnerId() != -10L) {
            pInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(item.getOwnerId());
            if (pInfo != null) {
                buf.append("radio{group=\"tid\";id=\"1," + pInfo.wurmId + "," + 1 + "\"};label{text=\"Owner Name\"};label{text=\"\"};label{text=\"" + pInfo.getName() + "\"};");
            } else {
                buf.append("label{text=\"\"};label{text=\"Owner ID\"};label{text=\"\"};label{text=\"" + item.getOwnerId() + "\"};");
            }
        } else {
            buf.append("label{text=\"\"};label{text=\"Owner\"};label{text=\"\"};label{type=\"italic\";text=\"On Ground\"};");
        }
        pInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(item.lastOwner);
        if (pInfo != null) {
            buf.append("radio{group=\"tid\";id=\"1," + pInfo.wurmId + "," + 1 + "\"};label{text=\"Last Owner Name\"};label{text=\"\"};label{text=\"" + pInfo.getName() + "\"};");
        } else if (item.lastOwner == -10L) {
            buf.append("label{text=\"\"};label{text=\"Last OwnerID\"};label{text=\"\"};label{text=\"NoOne (" + item.lastOwner + ")\"};");
        } else if (WurmId.getType(item.lastOwner) == 1) {
            Wagoner wagoner = Wagoner.getWagoner(item.lastOwner);
            if (wagoner != null) {
                buf.append("label{text=\"\"};label{text=\"Last OwnerID\"};label{text=\"\"};label{text=\"Wagoner:" + wagoner.getName() + " (" + item.lastOwner + ")\"};");
            } else {
                buf.append("label{text=\"\"};label{text=\"Last OwnerID\"};label{text=\"\"};label{text=\"Creature:Unknown (" + item.lastOwner + ")\"};");
            }
        } else {
            buf.append("label{text=\"\"};label{text=\"Last OwnerID\"};label{text=\"\"};label{text=\"Unknown (" + item.lastOwner + ")\"};");
        }
        int posX = (int)item.getPosX() >> 2;
        int posY = (int)item.getPosY() >> 2;
        boolean isOnSurface = item.isOnSurface();
        long bridgeId = item.getBridgeId();
        buf.append("radio{group=\"tid\";id=\"3," + posX + "," + posY + "," + isOnSurface + "\"};label{text=\"Coord (X,Y,surface)\"};radio{group=\"tid\";id=\"" + 12 + "," + posX + "," + posY + "," + isOnSurface + "\"};label{text=\"(" + posX + "," + posY + "," + isOnSurface + ")\"}");
        buf.append("label{text=\"\"};label{text=\"Actual (X,Y,Z)\"};label{text=\"\"};label{text=\"(" + item.getPosX() + "," + item.getPosY() + "," + item.getPosZ() + ")\"}");
        buf.append(this.addBridge(bridgeId));
        if (item.isVehicle() || item.isHitchTarget()) {
            Vehicle vehicle = Vehicles.getVehicle(item);
            if (vehicle == null) {
                buf.append("label{text=\"\"};label{text=\"Vehicle\"};label{text=\"\"};label{type=\"italic\";text=\"Not Found\"}");
            } else {
                Seat[] seats;
                Seat[] hitches;
                int hitchedCount = 0;
                for (Seat seat : hitches = vehicle.getHitched()) {
                    if (!seat.isOccupied()) continue;
                    ++hitchedCount;
                    long occupant = seat.getOccupant();
                    try {
                        Creature cret = Server.getInstance().getCreature(occupant);
                        buf.append("radio{group=\"tid\";id=\"1," + occupant + "," + 1 + "\"};label{text=\"Hitched " + hitchedCount + ":\"};label{text=\"\"};label{text=\"" + cret.getName() + "\"};");
                    }
                    catch (NoSuchPlayerException | NoSuchCreatureException e) {
                        buf.append("label{text=\"\"};label{text=\"Hitched " + hitchedCount + ":\"};label{text=\"\"};label{type=\"italic\";text=\"Not Found " + occupant + "\"};");
                    }
                }
                if (!item.isChair()) {
                    Creature dragger = Items.getDragger(item);
                    if (dragger != null) {
                        buf.append("radio{group=\"tid\";id=\"1," + dragger.getWurmId() + "," + 1 + "\"};label{text=\"Dragger:\"};label{text=\"\"};label{text=\"" + dragger.getName() + "\"};");
                    } else {
                        buf.append("label{text=\"\"};label{text=\"Dragger:\"};label{text=\"\"};label{type=\"italic\";text=\"None\"};");
                    }
                }
                String driver = item.isBoat() ? "Commander" : "Driver";
                String passenger = item.isChair() ? "Sitter" : "Passenger";
                int passengerCount = 0;
                for (Seat seat : seats = vehicle.getSeats()) {
                    Creature cret;
                    long occupant;
                    if (seat.type == 0) {
                        if (seat.isOccupied()) {
                            occupant = seat.getOccupant();
                            try {
                                cret = Server.getInstance().getCreature(occupant);
                                buf.append("radio{group=\"tid\";id=\"1," + occupant + "," + 1 + "\"};label{text=\"" + driver + ":\"};label{text=\"\"};label{text=\"" + cret.getName() + "\"};");
                            }
                            catch (NoSuchPlayerException | NoSuchCreatureException e) {
                                buf.append("label{text=\"\"};label{text=\"" + driver + ":\"};label{text=\"\"};label{type=\"italic\";text=\"Not Found (" + occupant + ")\"};");
                            }
                            continue;
                        }
                        buf.append("label{text=\"\"};label{text=\"" + driver + ":\"};label{text=\"\"};label{type=\"italic\";text=\"None\"};");
                        continue;
                    }
                    if (seat.type == 1) {
                        ++passengerCount;
                        if (seat.isOccupied()) {
                            occupant = seat.getOccupant();
                            try {
                                cret = Server.getInstance().getCreature(occupant);
                                buf.append("radio{group=\"tid\";id=\"1," + occupant + "," + 1 + "\"};label{text=\"Passenger " + passengerCount + ":\"};label{text=\"\"};label{text=\"" + cret.getName() + "\"};");
                            }
                            catch (NoSuchPlayerException | NoSuchCreatureException e) {
                                buf.append("label{text=\"\"};label{text=\"" + passenger + " " + passengerCount + ":\"};label{text=\"\"};label{type=\"italic\";text=\"Not Found (" + occupant + ")\"};");
                            }
                            continue;
                        }
                        buf.append("label{text=\"\"};label{text=\"" + passenger + " " + passengerCount + ":\"};label{text=\"\"};label{type=\"italic\";text=\"None\"};");
                        continue;
                    }
                    buf.append("label{text=\"\"};label{text=\"Unknown:\"};label{text=\"\"};label{type=\"italic\";text=\"Seat type (" + seat.type + ")\"};");
                }
            }
        }
        buf.append("label{text=\"\"};label{text=\"Creator\"};label{text=\"\"};label{text=\"" + item.creator + "\"}");
        buf.append("label{text=\"\"};label{text=\"Description\"};label{text=\"\"};label{text=\"" + item.getDescription() + "\"}");
        buf.append("label{text=\"\"};label{text=\"Is busy?\"};label{text=\"\"};label{" + this.showBoolean(item.isBusy()) + "}");
        buf.append("label{text=\"\"};label{text=\"Material\"};label{text=\"\"};label{text=\"" + Materials.convertMaterialByteIntoString(item.getMaterial()) + " (" + item.getMaterial() + ")\"}");
        buf.append("label{text=\"\"};label{text=\"Item Template\"};label{text=\"\"};label{text=\"" + item.getTemplate().getName() + " (" + item.getTemplateId() + ")\"}");
        int rt = item.getRealTemplateId();
        try {
            ItemTemplate realTemplate = ItemTemplateFactory.getInstance().getTemplate(rt);
            buf.append("label{text=\"\"};label{text=\"Real Template\"};label{text=\"\"};label{text=\"" + realTemplate.getName() + " (" + rt + ")\"}");
        }
        catch (NoSuchTemplateException e1) {
            buf.append("label{text=\"\"};label{text=\"Real Template\"};label{text=\"\"};label{type=\"italics\";text=\"" + (rt == -10 ? "None" : "Unknown") + " (" + rt + ")\"}");
        }
        buf.append("label{text=\"\"};label{text=\"Data\"};label{text=\"\"};label{" + (item.hasData() ? "" : red) + "text=\"" + item.getData1() + "," + item.getData2() + " (" + item.getData() + ")\"}");
        buf.append("label{text=\"\"};label{text=\"Extra\"};label{text=\"\"};label{" + (item.hasData() ? (item.hasExtraData() ? "" : orange) : red) + "text=\"" + item.getExtra1() + "," + item.getExtra2() + " (" + item.getExtra() + ")\"}");
        if (item.usesFoodState()) {
            buf.append("label{text=\"\"};label{text=\"Food state\"};label{text=\"\"};label{type=\"italics\";text=\"" + item.getFoodAuxByteName(item.getTemplate(), true, true) + " (" + item.getAuxData() + ")\"}");
        } else if (item.isRoadMarker()) {
            buf.append("label{text=\"\"};label{text=\"Links (AuxByte)\"};label{text=\"\"};label{type=\"italics\";text=\"" + MethodsHighways.getLinkAsString(item.getAuxData()) + " (" + item.getAuxData() + ")\"}");
        } else {
            buf.append("label{text=\"\"};label{text=\"AuxByte\"};label{text=\"\"};label{type=\"italics\";text=\"" + item.getActualAuxData() + "\"}");
        }
        if (item.getBless() == null) {
            buf.append("label{text=\"\"};label{text=\"Bless\"};label{text=\"\"};label{type=\"italics\";text=\"none\"}");
        } else {
            buf.append("label{text=\"\"};label{text=\"Bless\"};label{text=\"\"};label{text=\"" + item.getBless().getName() + "\"}");
        }
        if (item.getColor() == -1) {
            buf.append("label{text=\"\"};label{text=\"Colour\"};label{text=\"\"};label{text=\"not initialized (-1)\"}");
        } else {
            buf.append("label{text=\"\"};label{text=\"Colour\"};label{text=\"\"};label{text=\"" + WurmColor.getRGBDescription(item.getColor()) + " (" + item.getColor() + ")\"}");
        }
        if (item.supportsSecondryColor()) {
            if (item.getColor2() == -1) {
                buf.append("label{text=\"\"};label{text=\"Secondary Colour\"};label{text=\"\"};label{text=\"not initialized (-1)\"}");
            } else {
                buf.append("label{text=\"\"};label{text=\"Secondary Colour\"};label{text=\"\"};label{text=\"" + WurmColor.getRGBDescription(item.getColor2()) + " (" + item.getColor2() + ")\"}");
            }
        }
        Item parent = null;
        try {
            parent = item.getParent();
        }
        catch (NoSuchItemException hitches) {
            // empty catch block
        }
        if (parent != null) {
            buf.append("radio{group=\"tid\";id=\"1," + item.getParentId() + "," + 1 + "\"};label{text=\"Parent\"};label{text=\"\"};label{text=\"" + parent.getName() + "\"};");
        } else {
            buf.append("label{text=\"\"};label{text=\"Parent\"};label{text=\"\"};label{type=\"italic\";text=\"no parent (" + item.getParentId() + ")\"};");
        }
        String tempString = "";
        switch (item.getTemperatureState(item.getTemperature())) {
            case -1: {
                tempString = "Frozen";
                break;
            }
            case 1: {
                tempString = "Very warm";
                break;
            }
            case 2: {
                tempString = "Hot";
                break;
            }
            case 3: {
                tempString = "Boiling";
                break;
            }
            case 4: {
                tempString = "Searing";
                break;
            }
            case 5: {
                tempString = "Glowing";
                break;
            }
            case 0: {
                tempString = "Room temperature";
                break;
            }
            default: {
                tempString = "Unknown state";
            }
        }
        buf.append("label{text=\"\"};label{text=\"Temperature\"};label{text=\"\"};label{text=\"" + tempString + " (" + (float)item.getTemperature() / 10.0f + "C)\"}");
        buf.append("label{text=\"\"};label{text=\"Weight (Grams)\"};label{text=\"\"};label{text=\"" + item.getWeightGrams() + "\"}");
        if (item.isFood() || item.isLiquid()) {
            buf.append("label{text=\"\"};label{text=\"CCFP Kg Values\"};label{text=\"\"};label{text=\"" + item.getCalories() + ", " + item.getCarbs() + ", " + item.getFats() + ", " + item.getProteins() + "\"};");
            buf.append("label{text=\"\"};label{text=\"CCFP by Weight\"};label{text=\"\"};label{text=\"" + (int)item.getCaloriesByWeight() + ", " + (int)item.getCarbsByWeight() + ", " + (int)item.getFatsByWeight() + ", " + (int)item.getProteinsByWeight() + "\"};");
        }
        if (item.isVillageDeed()) {
            try {
                Village aVillage = Villages.getVillage(item.getData2());
                buf.append("radio{group=\"tid\";id=\"2," + aVillage.getId() + "," + 1 + "\"};label{text=\"Village Name\"};label{text=\"\"};label{text=\"" + aVillage.getName() + "\"};");
                buf.append("label{text=\"\"};label{text=\"Village Motto\"};label{text=\"\"};label{text=\"" + aVillage.getMotto() + "\"};");
            }
            catch (NoSuchVillageException e) {
                buf.append("label{text=\"\"};label{text=\"Village Name\"};label{text=\"\"};label{type=\"italic\";text=\"not found\"};");
                buf.append("label{text=\"\"};label{text=\"Village Motto\"};label{text=\"\"};label{type=\"italic\";text=\"not found\"};");
            }
        }
        String whenisit = (wurmdiff = (wurmcurrent = WurmCalendar.currentTime) - item.getLastMaintained()) < 0L ? " (Approx real time until that date: " + Server.getTimeFor(-wurmdiff / 8L) + ")" : " (Approx real time since that date: " + Server.getTimeFor(wurmdiff / 8L) + ")";
        buf.append("label{text=\"\"};label{text=\"Last Maintained\"};label{text=\"\"};label{text=\"" + item.getLastMaintained() + whenisit + "\"};");
        if (item.isLockable()) {
            if (lockid != -10L) {
                if (lock == null) {
                    buf.append("radio{group=\"tid\";id=\"1," + lockid + "," + 1 + "\"};label{text=\"Weird.\"};label{text=\"\"};label{text=\"Lock id exists, but not the lock!\"}");
                } else if (lock.isLocked()) {
                    buf.append("radio{group=\"tid\";id=\"1," + lock.getWurmId() + "," + 1 + "\"};label{text=\"" + lock.getName() + "\"};label{text=\"\"};label{text=\"Locked\"}");
                } else {
                    buf.append("radio{group=\"tid\";id=\"1," + lock.getWurmId() + "," + 1 + "\"};label{text=\"" + lock.getName() + "\"};label{text=\"\"};label{text=\"Unlocked\"}");
                }
            } else {
                buf.append("label{text=\"\"};label{text=\"Gate\"};label{text=\"\"};label{text=\"No Lock\"}");
            }
        }
        if (item.getTemplateId() == 300) {
            try {
                Creature merchant = null;
                Shop shop = null;
                long merchantId = -1L;
                merchantId = item.getData();
                if (merchantId != -1L) {
                    merchant = Server.getInstance().getCreature(merchantId);
                    if (merchant.isNpcTrader()) {
                        shop = Economy.getEconomy().getShop(merchant);
                    }
                    buf.append("radio{group=\"tid\";id=\"1," + merchantId + "," + 1 + "\"};label{text=\"Merchant\"};label{text=\"\"};label{text=\"" + merchant.getName() + "\"}");
                } else {
                    buf.append("label{text=\"\"};label{text=\"Merchant\"};label{text=\"\"};label{type=\"italic\";text=\"None.\"}");
                }
            }
            catch (NoSuchPlayerException e) {
                buf.append("label{text=\"\"};label{text=\"Merchant\"};label{text=\"\"};label{type=\"italic\";text=\"is a player? Well it can't be found.\"}");
            }
            catch (NoSuchCreatureException e) {
                buf.append("label{text=\"\"};label{text=\"Merchant\"};label{text=\"\"};label{type=\"italic\";text=\"can't be found.\"}");
            }
        }
        if (item.getTemplateId() == 166) {
            try {
                Structure building = Structures.getStructureForWrit(item.getWurmId());
                buf.append("radio{group=\"tid\";id=\"1," + building.getWurmId() + "," + 1 + "\"};label{text=\"Building\"};label{text=\"\"};label{text=\"" + building.getName() + "\"}");
            }
            catch (NoSuchStructureException e) {
                buf.append("label{text=\"\"};label{text=\"Building\"};label{text=\"\"};label{type=\"italic\";text=\"can't be found.\"}");
            }
        }
        if (item.mailed) {
            buf.append("label{text=\"\"};label{text=\"Mailed\"};label{text=\"\"};label{text=\"Yes\"}");
            WurmMail wm = WurmMail.getWurmMailForItem(item.getWurmId());
            if (wm != null) {
                long sender;
                ServerEntry se = Servers.getServerWithId(wm.getSourceserver());
                String sourceServer = " (Unk)";
                if (se != null) {
                    sourceServer = " (" + se.getAbbreviation() + ")";
                }
                if ((sender = wm.getSender()) == -10L) {
                    buf.append("label{text=\"\"};label{text=\" Sender\"};label{text=\"\"};label{type=\"italic\";text=\"NOID\"}");
                } else {
                    PlayerState ps = PlayerInfoFactory.getPlayerState(sender);
                    if (ps == null) {
                        buf.append("label{text=\"\"};label{text=\" Sender\"};label{text=\"\"};label{type=\"italic\";text=\"" + sender + "?" + sourceServer + "\"}");
                    } else {
                        buf.append("radio{group=\"tid\";id=\"1," + sender + "," + 1 + "\"};label{text=\" Sender\"};label{text=\"\"};label{text=\"" + ps.getPlayerName() + sourceServer + "\"}");
                    }
                }
                long receiver = wm.getReceiver();
                if (receiver == -10L) {
                    buf.append("label{text=\"\"};label{text=\" Receiver\"};label{text=\"\"};label{type=\"italic\";text=\"NOID\"}");
                } else {
                    PlayerState ps = PlayerInfoFactory.getPlayerState(receiver);
                    if (ps == null) {
                        buf.append("label{text=\"\"};label{text=\" Receiver\"};label{text=\"\"};label{type=\"italic\";text=\"" + receiver + "?\"}");
                    } else {
                        buf.append("radio{group=\"tid\";id=\"1," + receiver + "," + 1 + "\"};label{text=\" Receiver\"};label{text=\"\"};label{text=\"" + ps.getPlayerName() + "\"}");
                    }
                }
                buf.append("label{text=\"\"};label{text=\" Expires\"};label{text=\"\"};label{text=\"" + new Date(wm.getExpiration()) + "\"}");
                buf.append("label{text=\"\"};label{text=\" Price\"};label{text=\"\"};label{text=\"" + (wm.getType() == 0 ? "" : "COD ") + new Change(wm.getPrice()).getChangeShortString() + "\"}");
                buf.append("label{text=\"\"};label{text=\" Rejected\"};label{text=\"\"};label{text=\"" + wm.isRejected() + "\"}");
            } else {
                buf.append("label{text=\"\"};label{text=\"Mailed\"};label{text=\"\"};label{type=\"italic\";text=\"Mail not found\"}");
            }
        } else {
            buf.append("label{text=\"\"};label{text=\"Mailed\"};label{text=\"\"};label{text=\"No\"}");
        }
        if (item.isLock()) {
            boolean found = false;
            if (item.getTemplateId() == 252) {
                buf.append("label{text=\"\"};label{text=\"Attached To\"};label{text=\"\"};label{text=\"gate? TODO find gate\"};");
                found = true;
            } else if (item.getTemplateId() == 167) {
                buf.append("label{text=\"\"};label{text=\"Attached To\"};label{text=\"\"};label{text=\"door? TODO find door\"};");
                found = true;
            } else {
                for (Item litem : Items.getAllItems()) {
                    if (litem.getLockId() != item.getWurmId() || litem.isKey()) continue;
                    buf.append("radio{group=\"tid\";id=\"1," + litem.getWurmId() + "," + 1 + "\"};label{text=\"Attached To\"};label{text=\"\"};" + GmTool.itemNameWithColorByRarity(litem));
                    found = true;
                    break;
                }
            }
            if (!found) {
                buf.append("label{text=\"\"};label{text=\"Attached To\"};label{text=\"\"};label{text=\"nothing!\"};");
            }
        }
        if (item.isBed()) {
            PlayerInfo pinf = PlayerInfoFactory.getPlayerSleepingInBed(item.getWurmId());
            if (pinf != null) {
                buf.append("radio{group=\"tid\";id=\"1," + pinf.wurmId + "," + 1 + "\"};label{text=\"In Use by\"};label{text=\"\"};label{text=\"" + pinf.getName() + "\"};");
            } else {
                buf.append("label{text=\"\"};label{text=\"Bed In Use by\"};label{text=\"\"};label{type=\"italics\";text=\"Noone!\"};");
            }
            if (item.getWhenRented() != 0L) {
                buf.append("label{text=\"\"};label{text=\"Bed When Rented\"};label{text=\"\"};label{text=\"" + new Date(item.getWhenRented()) + "\"};");
                long pid = item.getData();
                if (pid > 0L) {
                    buf.append("radio{group=\"tid\";id=\"1," + pid + "," + 1 + "\"};label{text=\"Bed Rented by\"};label{text=\"\"};label{text=\"" + PlayerInfoFactory.getPlayerName(pid) + "\"};");
                }
            }
        }
        if (item.isRoadMarker()) {
            Village inVillage = Villages.getVillage(posX, posY, true);
            if (inVillage != null) {
                buf.append("radio{group=\"tid\";id=\"2," + inVillage.getId() + "," + 1 + "\"};label{text=\"In Village\"};label{text=\"\"};label{text=\"" + inVillage.getName() + "\"};");
            } else {
                Village lVillage = Villages.getVillageFor(item);
                if (lVillage != null) {
                    buf.append("radio{group=\"tid\";id=\"2," + lVillage.getId() + "," + 1 + "\"};label{text=\"Village Border\";hover=\"In first 2 tiles of perimeter, where permissions apply.\"};label{text=\"\"};label{text=\"" + lVillage.getName() + "\"};");
                } else {
                    buf.append("label{text=\"\"};label{text=\"in Village\"};label{text=\"\"};label{text=\"none\"};");
                }
            }
            if (item.getTemplateId() == 1112) {
                Node node = Routes.getNode(item);
                if (node.getVillage() == null) {
                    buf.append("label{text=\"\"};label{text=\"Route Village\"};label{text=\"\"};label{text=\"none\"};");
                } else {
                    buf.append("radio{group=\"tid\";id=\"2," + node.getVillage().getId() + "," + 1 + "\"};label{text=\"Route Village\";hover=\"Should be same as In Village above.\"};label{text=\"\"};label{text=\"" + node.getVillage().getName() + "\"};");
                }
            }
        }
        if (item.getTemplateId() == 272) {
            long wb = item.getWasBrandedTo();
            Village wasBrandedByVilage = null;
            if (wb != -10L) {
                try {
                    wasBrandedByVilage = Villages.getVillage((int)wb);
                }
                catch (NoSuchVillageException e) {
                    wb = -10L;
                }
            }
            if (wb == -10L) {
                buf.append("label{text=\"\"};label{text=\"Was Branded By\";hover=\"will be reset if there is a server reboot\"};label{text=\"\"};label{text=\"none\"};");
            } else {
                buf.append("radio{group=\"tid\";id=\"2," + wasBrandedByVilage.getId() + "," + 1 + "\"};label{text=\"Was Branded By\";hover=\"will be reset if there is a server reboot\"};label{text=\"\"};label{text=\"" + wasBrandedByVilage.getName() + "\"};");
            }
        }
        buf.append("}");
        if (lock != null) {
            buf.append(this.keysTable(lock));
        }
        if (item.isLock()) {
            buf.append(this.keysTable(item));
        }
        if (item.isKey()) {
            try {
                Item key = Items.getItem(item.getLockId());
                buf.append("label{type=\"bold\";text=\"Associated Lock:\"}");
                buf.append("table{rows=\"1\";cols=\"2\";");
                buf.append("radio{group=\"tid\";id=\"1," + key.getWurmId() + "," + 1 + "\"};" + GmTool.itemNameWithColorByRarity(key));
                buf.append("}");
            }
            catch (NoSuchItemException noSuchItemException) {
                // empty catch block
            }
        }
        buf.append("label{text=\"Creation date: " + WurmCalendar.getTimeFor(item.creationDate) + "\"}");
        buf.append("label{type=\"bold\";text=\"Examine text:\"};" + item.getExamineAsBml(this.getResponder()));
        return buf.toString();
    }

    private String containerItems(Item item) {
        StringBuilder buf = new StringBuilder();
        try {
            Item[] litems = item.getItemsAsArray();
            if (litems.length > 0) {
                buf.append(this.itemsTable(litems));
            } else {
                buf.append("label{text=\"No Items found\"}");
            }
        }
        catch (Exception e1) {
            logger.log(Level.WARNING, e1.getMessage(), e1);
            buf.append("label{text=\"Error reading Items\"}");
            buf.append("label{text=\"Exception:\"};");
            buf.append("label{text=\"" + e1.getMessage() + "\"};");
        }
        return buf.toString();
    }

    private String mailItems(WurmMail[] mail) {
        StringBuilder buf = new StringBuilder();
        if (mail.length > 0) {
            Arrays.sort(mail);
            buf.append(this.mailTable(mail));
        } else {
            buf.append("label{text=\"No Mail Items found\"}");
        }
        return buf.toString();
    }

    private String wallDetails() {
        StringBuilder buf = new StringBuilder();
        Wall wall = Wall.getWall(this.wurmId);
        if (wall != null) {
            Door door = wall.getDoor();
            if (door != null && this.displaySubType == 2) {
                buf.append(this.showGuests(DoorSettings.DoorPermissions.getPermissions(), door.getPermissionsPlayerList()));
            } else if (door != null && this.displaySubType == 3) {
                buf.append(this.showHistory(door.getWurmId()));
            } else {
                buf.append(this.wallSummary(wall));
            }
            buf.append("label{type=\"bold\";text=\"------------------------------ Links --------------------------------------\"}");
            boolean rows = true;
            buf.append("table{rows=\"1\";cols=\"4\";");
            buf.append("radio{group=\"tid\";id=\"1," + this.wurmId + "," + 1 + "\"};label{text=\"Summary" + (this.displaySubType == 1 ? " (Showing)" : "") + "\"};");
            if (door != null) {
                buf.append((door.getPermissionsPlayerList().size() == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 2 + "\"};") + "label{text=\"" + door.getPermissionsPlayerList().size() + " Guests" + (this.displaySubType == 2 ? " (Showing)" : "") + "\"};");
                buf.append((PermissionsHistories.getPermissionsHistoryFor(door.getWurmId()).getHistoryEvents().length == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 3 + "\"};") + "label{text=\"" + PermissionsHistories.getPermissionsHistoryFor(door.getWurmId()).getHistoryEvents().length + " History" + (this.displaySubType == 3 ? " (Showing)" : "") + "\"};");
            }
            buf.append("label{text=\"\"};label{text=\"\"};");
            buf.append("}");
        } else {
            buf.append("label{text=\"no wall found with that Id\"}");
        }
        return buf.toString();
    }

    private String wallSummary(Wall wall) {
        StringBuilder buf = new StringBuilder();
        buf.append("table{rows=\"9\";cols=\"4\";");
        buf.append("label{text=\"\"};label{text=\"Wurm Id\"};label{text=\"\"};label{text=\"" + this.wurmId + "\"}");
        buf.append("label{text=\"\"};label{text=\"Name\"};label{text=\"\"};label{text=\"" + wall.getName() + "\"}");
        buf.append("label{text=\"\"};label{text=\"Building Id\"};label{text=\"\"};label{text=\"" + wall.getStructureId() + "\"}");
        try {
            buf.append("radio{group=\"tid\";id=\"1," + wall.getStructureId() + "," + 1 + "\"};label{text=\"Building Name\"};label{text=\"\"};label{text=\"" + Structures.getStructure(wall.getStructureId()).getName() + "\"}");
        }
        catch (Exception e) {
            buf.append("label{text=\"\"};label{text=\"Building Name\"};label{text=\"\"};label{type=\"italic\";text=\"Building not found!\"}");
        }
        buf.append("label{text=\"\"};label{text=\"Last Maintained\"};label{text=\"\"};label{text=\"" + new Date(wall.getLastUsed()) + "\"};");
        int wx = wall.getTileX();
        int wy = wall.getTileY();
        boolean ws = wall.getLayer() >= 0;
        buf.append("radio{group=\"tid\";id=\"3," + wx + "," + wy + "," + ws + "\"};label{text=\"Coords (X,Y,surface)\"};radio{group=\"tid\";id=\"" + 12 + "," + wx + "," + wy + "," + ws + "\"};label{text=\"(" + wx + "," + wy + "," + ws + ")\"}");
        buf.append("label{text=\"\"};label{text=\"Material\"};label{text=\"\"};label{text=\"" + wall.getMaterialString() + "\"}");
        buf.append("label{text=\"\"};label{text=\"QL\"};label{text=\"\"};label{text=\"" + wall.getQualityLevel() + "\"}");
        buf.append("label{text=\"\"};label{text=\"DMG\"};label{text=\"\"};label{text=\"" + wall.getDamage() + "\"}");
        buf.append("label{text=\"\"};label{text=\"Indoors\"};label{text=\"\"};label{text=\"" + wall.isIndoor() + "\"}");
        buf.append("label{text=\"\"};label{text=\"Floor Level\"};label{text=\"\"};label{text=\"" + wall.getFloorLevel() + "\"}");
        buf.append("label{text=\"\"};label{text=\"Height Offset\"};label{text=\"\"};label{text=\"" + wall.getHeight() + "\"}");
        buf.append("}");
        return buf.toString();
    }

    private String fenceDetails() {
        StringBuilder buf = new StringBuilder();
        Fence fence = Fence.getFence(this.wurmId);
        if (fence != null) {
            FenceGate gate = null;
            if (fence.isDoor()) {
                gate = FenceGate.getFenceGate(this.wurmId);
            }
            if (gate != null && this.displaySubType == 2) {
                buf.append(this.showGuests(DoorSettings.GatePermissions.getPermissions(), gate.getPermissionsPlayerList()));
            } else if (gate != null && this.displaySubType == 3) {
                buf.append(this.showHistory(gate.getWurmId()));
            } else {
                buf.append(this.fenceSummary(fence));
            }
            buf.append("label{type=\"bold\";text=\"------------------------------ Links --------------------------------------\"}");
            boolean rows = true;
            buf.append("table{rows=\"1\";cols=\"4\";");
            buf.append("radio{group=\"tid\";id=\"1," + this.wurmId + "," + 1 + "\"};label{text=\"Summary" + (this.displaySubType == 1 ? " (Showing)" : "") + "\"};");
            if (gate != null) {
                buf.append((gate.getPermissionsPlayerList().size() == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 2 + "\"};") + "label{text=\"" + gate.getPermissionsPlayerList().size() + " Guests" + (this.displaySubType == 2 ? " (Showing)" : "") + "\"};");
                buf.append((PermissionsHistories.getPermissionsHistoryFor(gate.getWurmId()).getHistoryEvents().length == 0 ? "label{text=\"\"};" : "radio{group=\"tid\";id=\"1," + this.wurmId + "," + 3 + "\"};") + "label{text=\"" + PermissionsHistories.getPermissionsHistoryFor(gate.getWurmId()).getHistoryEvents().length + " History" + (this.displaySubType == 3 ? " (Showing)" : "") + "\"};");
            }
            buf.append("label{text=\"\"};label{text=\"\"};");
            buf.append("}");
        } else {
            buf.append("label{text=\"no fence found with that Id\"}");
        }
        return buf.toString();
    }

    private String fenceSummary(Fence fence) {
        StringBuilder buf = new StringBuilder();
        FenceGate gate = null;
        Item lock = null;
        long lockid = -10L;
        int rows = 7;
        if (fence.isDoor() && (gate = FenceGate.getFenceGate(fence.getId())) != null) {
            try {
                ++rows;
                lockid = gate.getLockId();
                try {
                    lock = Items.getItem(lockid);
                }
                catch (NoSuchItemException nsi) {
                    logger.log(Level.WARNING, "Weird. Lock id exists, but not the item.", nsi);
                }
            }
            catch (NoSuchLockException nsi) {
                // empty catch block
            }
        }
        buf.append("table{rows=\"" + rows + "\";cols=\"4\";");
        buf.append("label{text=\"\"};label{text=\"Wurm Id\"};label{text=\"\"};label{text=\"" + this.wurmId + "\"}");
        buf.append("label{text=\"\"};label{text=\"Name\"};label{text=\"\"};label{text=\"" + fence.getName() + "\"}");
        buf.append("label{text=\"\"};label{text=\"On Border\"};label{text=\"\"};label{text=\"" + (fence.isHorizontal() ? "North" : "West") + "\"}");
        int fx = fence.getTileX();
        int fy = fence.getTileY();
        boolean fs = fence.getLayer() >= 0;
        buf.append("radio{group=\"tid\";id=\"3," + fx + "," + fy + "," + fs + "\"};label{text=\"Coords (X,Y,surface)\"};radio{group=\"tid\";id=\"" + 12 + "," + fx + "," + fy + "," + fs + "\"};label{text=\"(" + fx + "," + fy + "," + fs + ")\"}");
        buf.append("label{text=\"\"};label{text=\"Finished?\"};label{text=\"\"};label{text=\"" + fence.isFinished() + "\"}");
        buf.append("label{text=\"\"};label{text=\"QL\"};label{text=\"\"};label{text=\"" + fence.getQualityLevel() + "\"}");
        buf.append("label{text=\"\"};label{text=\"DMG\"};label{text=\"\"};label{text=\"" + fence.getDamage() + "\"}");
        buf.append("label{text=\"\"};label{text=\"FloorLevel\"};label{text=\"\"};label{text=\"" + fence.getFloorLevel() + "\"}");
        buf.append("label{text=\"\"};label{text=\"Height Offset\"};label{text=\"\"};label{text=\"" + fence.getHeightOffset() + "\"}");
        if (fence.isDoor() && gate != null) {
            if (lockid != -10L) {
                if (lock == null) {
                    buf.append("label{text=\"\"};label{text=\"Gate\"};label{text=\"\"};label{text=\"Lock Missing (" + lockid + ")\"}");
                } else if (lock.isLocked()) {
                    buf.append("radio{group=\"tid\";id=\"1," + lock.getWurmId() + "," + 1 + "\"};label{text=\"" + lock.getName() + "\"};label{text=\"\"};label{text=\"Locked\"}");
                } else {
                    buf.append("radio{group=\"tid\";id=\"1," + lock.getWurmId() + "," + 1 + "\"};label{text=\"" + lock.getName() + "\"};label{text=\"\"};label{text=\"Unlocked\"}");
                }
            } else {
                buf.append("label{text=\"\"};label{text=\"Gate\"};label{text=\"\"};label{text=\"No Lock\"}");
            }
        }
        buf.append("}");
        if (lock != null) {
            buf.append(this.keysTable(lock));
        }
        return buf.toString();
    }

    private Set<Item> findCorpses(PlayerInfo pinf) {
        HashSet<Item> corpses = new HashSet<Item>();
        Item[] its = Items.getAllItems();
        for (int itx = 0; itx < its.length; ++itx) {
            if (its[itx].getTemplateId() != 272 || !its[itx].getName().equals("corpse of " + pinf.getName()) || its[itx].getZoneId() <= -1) continue;
            corpses.add(its[itx]);
        }
        return corpses;
    }

    private String keysTable(Item lock) {
        StringBuilder buf = new StringBuilder();
        long[] keyarr = lock.getKeyIds();
        buf.append("label{type=\"bold\";text=\"total # of keys:" + keyarr.length + "\"};");
        buf.append("table{rows=\"" + keyarr.length + "\";cols=\"2\";");
        for (long lElement : keyarr) {
            try {
                Item key = Items.getItem(lElement);
                buf.append("radio{group=\"tid\";id=\"1," + lElement + "," + 1 + "\"};" + GmTool.itemNameWithColorByRarity(key));
            }
            catch (NoSuchItemException e) {
                buf.append("radio{group=\"tid\";id=\"1," + lElement + "," + 1 + "\"};label{text=\"(not found) " + lElement + "\"};");
            }
        }
        buf.append("}");
        return buf.toString();
    }

    private String playerInfoTable(PlayerInfo[] lPlayerInfos) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"total # of players:" + lPlayerInfos.length + "\"};");
        int pages = (lPlayerInfos.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < lPlayerInfos.length ? start + this.rowsPerPage : lPlayerInfos.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"6\";label{text=\"\"};label{text=\"WurmId\"};label{text=\"Name\"};label{text=\"Status\"};label{text=\"Server\"};label{text=\"Banned?\"};");
            for (int x = start; x < last; ++x) {
                PlayerInfo playerInfo = lPlayerInfos[x];
                long playerWurmId = playerInfo.wurmId;
                String pServer = "Unknown";
                String pStatus = "Unknown";
                String pColour = "";
                PlayerState pState = PlayerInfoFactory.getPlayerState(playerWurmId);
                if (pState != null) {
                    if (pState.getState() == PlayerOnlineStatus.OTHER_SERVER) {
                        pStatus = "Other";
                        pColour = orange;
                        pServer = pState.getServerName();
                    } else if (pState.getState() == PlayerOnlineStatus.ONLINE) {
                        pStatus = "Online";
                        pColour = green;
                        pServer = pState.getServerName();
                    } else if (pState.getState() == PlayerOnlineStatus.OFFLINE) {
                        pStatus = "Offline";
                        pColour = red;
                        pServer = pState.getServerName();
                    }
                }
                buf.append("radio{group=\"tid\";id=\"1," + playerWurmId + "," + 1 + "\"};label{text=\"" + playerWurmId + "\"};label{text=\"" + playerInfo.getName() + "\"};label{" + pColour + "text=\"" + pStatus + "\"};label{text=\"" + pServer + "\"};label{text=\"" + playerInfo.isBanned() + "\"};");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String buildingsTable(Player player, Structure[] buildings) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"total # of owned buildings:" + buildings.length + "\"};");
        int pages = (buildings.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < buildings.length ? start + this.rowsPerPage : buildings.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"7\";label{text=\"\"};label{text=\"WurmId\"}label{text=\"Owner?\"}label{text=\"Doors have locks?\"}label{text=\"On Deed?\"}label{text=\"Deed Managed?\"}label{text=\"Name\"}");
            for (int x = start; x < last; ++x) {
                Structure structure = buildings[x];
                buf.append("radio{group=\"tid\";id=\"1," + structure.getWurmId() + "," + 1 + "\"};label{text=\"" + structure.getWurmId() + "\"};label{" + this.showBoolean(structure.isActualOwner(player.getWurmId())) + "};");
                if (structure.getAllDoors().length == 0) {
                    buf.append("label{color=\"255,177,40\"text=\"No lockable doors.\"};");
                } else if (structure.isLockable()) {
                    buf.append("label{color=\"127,255,127\"text=\"true\"};");
                } else {
                    buf.append("label{color=\"255,127,127\"text=\"Not all doors have locks.\"};");
                }
                buf.append("label{" + this.showBoolean(structure.getVillage() != null) + "};label{" + this.showBoolean(structure.isManaged()) + "};label{text=\"" + structure.getObjectName() + "\"};");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String minedoorsTable(Player player, MineDoorPermission[] mds) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"total # of owned minde doors:" + mds.length + "\"};");
        int pages = (mds.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < mds.length ? start + this.rowsPerPage : mds.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"7\";label{text=\"\"};label{text=\"TileId\"}label{text=\"Door Type\"}label{text=\"Owner?\"}label{text=\"On Deed?\"}label{text=\"Deed Managed?\"}label{text=\"Name\"}");
            for (int x = start; x < last; ++x) {
                MineDoorPermission mineDoor = mds[x];
                buf.append("radio{group=\"tid\";id=\"1," + mineDoor.getWurmId() + "," + 1 + "\"};label{text=\"" + mineDoor.getWurmId() + "\"};label{text=\"" + mineDoor.getTypeName() + "\"};label{" + this.showBoolean(mineDoor.isActualOwner(player.getWurmId())) + "};label{" + this.showBoolean(mineDoor.getVillage() != null) + "};label{" + this.showBoolean(mineDoor.isManaged()) + "};label{text=\"" + mineDoor.getObjectName() + "\"};");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String gatesTable(Player player, FenceGate[] gates) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"total # of owned gates:" + gates.length + "\"};");
        int pages = (gates.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < gates.length ? start + this.rowsPerPage : gates.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"10\";label{text=\"\"};label{text=\"WurmId\"}label{text=\"Gate Type\"}label{text=\"Level\"}label{text=\"Has Lock?\"}label{text=\"Locked?\"}label{text=\"Owner?\"}label{text=\"On Deed?\"}label{text=\"Deed Managed?\"}label{text=\"Name\"}");
            for (int x = start; x < last; ++x) {
                FenceGate gate = gates[x];
                buf.append("radio{group=\"tid\";id=\"1," + gate.getWurmId() + "," + 1 + "\"};label{text=\"" + gate.getWurmId() + "\"};label{text=\"" + gate.getTypeName() + "\"};label{text=\"" + gate.getFloorLevel() + "\"};label{" + this.showBoolean(gate.hasLock()) + "};label{" + this.showBoolean(gate.isLocked()) + "};label{" + this.showBoolean(gate.isActualOwner(player.getWurmId())) + "};label{" + this.showBoolean(gate.getVillage() != null) + "};label{" + this.showBoolean(gate.isManaged()) + "};label{text=\"" + gate.getObjectName() + "\"};");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String pathTable(Player player, List<Route> path) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"total # of Nodes:" + path.size() + "\"};");
        int pages = (path.size() - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < path.size() ? start + this.rowsPerPage : path.size();
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"5\";label{text=\"\"};label{text=\"Node Id\"}label{text=\"\"};label{text=\"Coords\"}label{text=\"Dir\"}");
            for (int x = start; x < last; ++x) {
                Node startNode = path.get(x).getStartNode();
                int posX = startNode.getWaystone().getTileX();
                int posY = startNode.getWaystone().getTileY();
                boolean onS = startNode.getWaystone().isOnSurface();
                buf.append("radio{group=\"tid\";id=\"15," + startNode.getWaystone().getWurmId() + "," + 22 + "\"};");
                buf.append("label{text=\"" + startNode.getWaystone().getWurmId() + "\"};");
                buf.append("radio{group=\"tid\";id=\"12," + posX + "," + posY + "," + onS + "\"};");
                buf.append("label{text=\"(" + posX + "," + posY + "," + onS + ")\"}");
                if (x < path.size() - 1) {
                    Node endNode = path.get(x + 1).getEndNode();
                    byte dir = startNode.getNodeDir(endNode);
                    buf.append("label{text=\"" + MethodsHighways.getLinkDirString(dir) + "\"}");
                    continue;
                }
                buf.append("label{type=\"italics\";text=\"target\"}");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String itemsTable(String itemtype, Player player, Item[] items) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"total # of owned " + itemtype + ":" + items.length + "\"};");
        int pages = (items.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < items.length ? start + this.rowsPerPage : items.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"7\";label{text=\"\"};label{text=\"WurmId\"}label{text=\"Type\"}label{text=\"Owner?\"}label{text=\"Locked?\"}label{text=\"Name\"}label{text=\"Is busy?\"};");
            for (int x = start; x < last; ++x) {
                Item item = items[x];
                buf.append("radio{group=\"tid\";id=\"1," + item.getWurmId() + "," + 1 + "\"};label{text=\"" + item.getWurmId() + "\"};" + ManageObjectList.addRariryColour(item, item.getTypeName()) + "label{" + this.showBoolean(item.isActualOwner(player.getWurmId())) + "};label{" + this.showBoolean(item.isLocked()) + "};label{text=\"" + item.getObjectName() + "\"};label{" + this.showBoolean(item.isBusy()) + "};");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String friendsTable(Friend[] lFriends) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"total # of players:" + lFriends.length + "\"};");
        int pages = (lFriends.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < lFriends.length ? start + this.rowsPerPage : lFriends.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            long[] wurmids = new long[last - start];
            for (int x = start; x < last; ++x) {
                wurmids[x - start] = lFriends[x].getFriendId();
            }
            try {
                Map<Long, byte[]> playerStates = PlayerInfoFactory.getPlayerStates(wurmids);
                buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"5\";label{text=\"\"};label{type=\"bold\";text=\"WurmId\"};label{type=\"bold\";text=\"Name\"};label{type=\"bold\";text=\"OnServer\"};label{type=\"bold\";text=\"Category\"};");
                for (int x = 0; x < wurmids.length; ++x) {
                    PlayerState pState = new PlayerState(playerStates.get(wurmids[x]));
                    buf.append("radio{group=\"tid\";id=\"1," + wurmids[x] + "," + 1 + "\"};label{text=\"" + pState.getPlayerId() + "\"};label{text=\"" + pState.getPlayerName() + "\"};label{text=\"" + pState.getServerName() + "\"};label{text=\"" + lFriends[x].getCategory().name() + "\"};");
                }
                buf.append("}");
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, rx.getMessage(), rx);
            }
            catch (WurmServerException wse) {
                logger.log(Level.WARNING, wse.getMessage(), wse);
            }
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String PermissionsByPlayerTable(Permissions.IPermission[] ips, PermissionsByPlayer[] lplayers) {
        StringBuilder buf = new StringBuilder();
        Arrays.sort(lplayers);
        buf.append("label{text=\"total # of players:" + lplayers.length + "\"};");
        int pages = (lplayers.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < lplayers.length ? start + this.rowsPerPage : lplayers.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            long[] wurmids = new long[last - start];
            for (int x = start; x < last; ++x) {
                wurmids[x - start] = lplayers[x].getPlayerId();
            }
            try {
                Map<Long, byte[]> playerStates = PlayerInfoFactory.getPlayerStates(wurmids);
                buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"" + (ips.length + 4) + "\";label{text=\"\"};label{text=\"\"};label{text=\"\"};label{type=\"bold\";text=\"On\"};");
                for (Permissions.IPermission perm : ips) {
                    buf.append("label{type=\"bold\";text=\"" + perm.getHeader1() + "\"};");
                }
                buf.append("label{text=\"\"};label{type=\"bold\";text=\"WurmId\"};label{type=\"bold\";text=\"Name\"};label{type=\"bold\";text=\"Server\"};");
                for (Permissions.IPermission perm : ips) {
                    buf.append("label{type=\"bold\";text=\"" + perm.getHeader2() + "\"};");
                }
                for (int x = 0; x < wurmids.length; ++x) {
                    PlayerState pState = new PlayerState(playerStates.get(wurmids[x]));
                    buf.append("radio{group=\"tid\";id=\"1," + wurmids[x] + "," + 1 + "\"};label{text=\"" + pState.getPlayerId() + "\"};label{text=\"" + PermissionsByPlayer.getPlayerOrGroupName(pState.getPlayerId()) + "\"};label{text=\"" + (pState.getPlayerId() < 0L ? Servers.getLocalServerName() : pState.getServerName()) + "\"};");
                    for (Permissions.IPermission perm : ips) {
                        buf.append("label{" + this.showBoolean(lplayers[start + x].hasPermission(perm.getBit())) + "};");
                    }
                }
                buf.append("}");
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, rx.getMessage(), rx);
            }
            catch (WurmServerException wse) {
                logger.log(Level.WARNING, wse.getMessage(), wse);
            }
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String showBoolean(boolean flag) {
        StringBuilder buf = new StringBuilder();
        if (flag) {
            buf.append(green);
        } else {
            buf.append(red);
        }
        buf.append("text=\"" + flag + "\"");
        return buf.toString();
    }

    private String playersTable(long[] lplayers) {
        StringBuilder buf = new StringBuilder();
        Arrays.sort(lplayers);
        buf.append("label{text=\"total # of players:" + lplayers.length + "\"};");
        int pages = (lplayers.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < lplayers.length ? start + this.rowsPerPage : lplayers.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            long[] wurmids = new long[last - start];
            for (int x = start; x < last; ++x) {
                wurmids[x - start] = lplayers[x];
            }
            try {
                Map<Long, byte[]> playerStates = PlayerInfoFactory.getPlayerStates(wurmids);
                buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"4\";label{text=\"\"};label{type=\"bold\";text=\"WurmId\"};label{type=\"bold\";text=\"Name\"};label{type=\"bold\";text=\"OnServer\"};");
                for (int x = 0; x < wurmids.length; ++x) {
                    PlayerState pState = new PlayerState(playerStates.get(wurmids[x]));
                    buf.append("radio{group=\"tid\";id=\"1," + wurmids[x] + "," + 1 + "\"};label{text=\"" + pState.getPlayerId() + "\"};label{text=\"" + pState.getPlayerName() + "\"};label{text=\"" + pState.getServerName() + "\"};");
                }
                buf.append("}");
            }
            catch (RemoteException rx) {
                logger.log(Level.WARNING, rx.getMessage(), rx);
            }
            catch (WurmServerException wse) {
                logger.log(Level.WARNING, wse.getMessage(), wse);
            }
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String avatarsTable(Creature[] lcreatures) {
        StringBuilder buf = new StringBuilder();
        Arrays.sort(lcreatures);
        buf.append("label{text=\"total # of avatars:" + lcreatures.length + "\"};");
        int pages = (lcreatures.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < lcreatures.length ? start + this.rowsPerPage : lcreatures.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"6\";label{text=\"\"};label{type=\"bold\";text=\"WurmId\"};label{type=\"bold\";text=\"Type\"};label{text=\"\"};label{type=\"bold\";text=\"Coords\"};label{type=\"bold\";text=\"FullName\"};");
            for (int x = start; x < last; ++x) {
                Creature lcreature = lcreatures[x];
                int posX = lcreature.getTileX();
                int posY = lcreature.getTileY();
                boolean onS = lcreature.isOnSurface();
                buf.append("radio{group=\"tid\";id=\"1," + lcreature.getWurmId() + "," + 1 + "\"};label{text=\"" + lcreature.getWurmId() + "\"};label{text=\"" + lcreature.getTemplate().getName() + "\"};radio{group=\"tid\";id=\"" + 12 + "," + posX + "," + posY + "," + onS + "\"};label{text=\"(" + posX + "," + posY + "," + onS + ")\"}label{text=\"" + lcreature.getName() + "\"};");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String kingdomsTable(Kingdom[] lkingdoms) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"total # of kingdoms:" + lkingdoms.length + "\"};");
        int pages = (lkingdoms.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < lkingdoms.length ? start + this.rowsPerPage : lkingdoms.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"11\";label{text=\"\"};label{type=\"bold\";text=\"Id\"};label{type=\"bold\";text=\"Name\"};label{type=\"bold\";text=\"Template\"};label{type=\"bold\";text=\"Suffix\"};label{type=\"bold\";text=\"Chat Name\"};label{type=\"bold\";text=\"Here?\"};label{type=\"bold\";text=\"1st Motto\"};label{type=\"bold\";text=\"2nd Motto\"};label{type=\"bold\";text=\"Accepts\"};label{type=\"bold\";text=\"WinPoints\"};");
            for (int x = start; x < last; ++x) {
                Kingdom lkingdom = lkingdoms[x];
                String tempking = "";
                switch (lkingdom.getTemplate()) {
                    case 0: {
                        tempking = "none";
                        break;
                    }
                    case 1: {
                        tempking = "JK";
                        break;
                    }
                    case 2: {
                        tempking = "MR";
                        break;
                    }
                    case 3: {
                        tempking = "HOTS";
                        break;
                    }
                    case 4: {
                        tempking = "Freedom";
                        break;
                    }
                    default: {
                        tempking = "(" + lkingdom.getTemplate() + ")";
                    }
                }
                buf.append("label{text=\"\"};label{text=\"" + lkingdom.getId() + "\"};label{text=\"" + lkingdom.getName() + "\"};label{text=\"" + tempking + "\"};label{text=\"" + lkingdom.getSuffix() + "\"};label{text=\"" + lkingdom.getChatName() + "\"};label{text=\"" + lkingdom.existsHere() + "\"};label{text=\"" + lkingdom.getFirstMotto() + "\"};label{text=\"" + lkingdom.getSecondMotto() + "\"};label{text=\"" + lkingdom.acceptsTransfers() + "\"};label{text=\"" + lkingdom.getWinpoints() + "\"};");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String creaturesTable(Creature[] lcreatures) {
        StringBuilder buf = new StringBuilder();
        Arrays.sort(lcreatures);
        buf.append("label{text=\"total # of creatures:" + lcreatures.length + "\"};");
        int pages = (lcreatures.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < lcreatures.length ? start + this.rowsPerPage : lcreatures.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"5\";label{text=\"\"};label{type=\"bold\";text=\"WurmId\"};label{type=\"bold\";text=\"Type\"};label{type=\"bold\";text=\"Name\"};label{type=\"bold\";text=\"FullName\"};");
            for (int x = start; x < last; ++x) {
                Creature lcreature = lcreatures[x];
                buf.append("radio{group=\"tid\";id=\"1," + lcreature.getWurmId() + "," + 1 + "\"};label{text=\"" + lcreature.getWurmId() + "\"};label{text=\"" + lcreature.getTemplate().getName() + "\"};label{text=\"" + lcreature.getNameWithoutPrefixes() + "\"};label{text=\"" + lcreature.getName() + "\"};");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String skillsTable(SkillTemplate[] templates, Skills skills) {
        StringBuilder buf = new StringBuilder();
        Arrays.sort(templates);
        buf.append("label{type=\"italic\";text=\"Affinities are only shown for online players.\"};");
        buf.append("label{text=\"total # of skills:" + templates.length + "\"};");
        int pages = (templates.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < templates.length ? start + this.rowsPerPage : templates.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"5\";label{type=\"bold\";text=\"\"};label{type=\"bold\";text=\"Name\"};label{type=\"bold\";text=\"Value\"};label{type=\"bold\";text=\"Value2\"};label{type=\"bold\";text=\"Highest\"};");
            for (int x = start; x < last; ++x) {
                int sk = templates[x].getNumber();
                try {
                    Skill skill = skills.getSkill(sk);
                    String affs = "******".substring(0, skill.affinity);
                    buf.append("label{text=\"\"};label{text=\"" + templates[x].getName() + " " + affs + "\"};label{text=\"" + new Float(skill.getKnowledge()) + "\"};label{text=\"" + new Float(skill.getKnowledge(0.0)) + "\"};label{text=\"" + new Float(skill.getMinimumValue()) + "\"};");
                    continue;
                }
                catch (NoSuchSkillException e) {
                    buf.append("label{text=\"\"};label{text=\"" + templates[x].getName() + "\"};label{text=\"1\"};label{text=\"1\"};label{text=\"N/A\"};");
                }
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"No Skills found\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String tilesTable(VolaTile[] tiles) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"Total # of tiles:" + tiles.length + "\"};");
        int pages = (tiles.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < tiles.length ? start + this.rowsPerPage : tiles.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"5\";label{text=\"\"};label{type=\"bold\";text=\"Wurm Id\"};label{type=\"bold\";text=\"TileX\"};label{type=\"bold\";text=\"TileY\"};label{type=\"bold\";text=\"Type\"}");
            for (int x = start; x < last; ++x) {
                VolaTile tile = tiles[x];
                long Id = Tiles.getTileId(tile.tilex, tile.tiley, 0);
                int stile = Server.surfaceMesh.getTile(tile.tilex, tile.tiley);
                byte ttype = Tiles.decodeType(stile);
                Tiles.Tile theTile = Tiles.getTile(ttype);
                buf.append("radio{group=\"tid\";id=\"1," + Id + "," + 1 + "\"};label{text=\"" + Id + "\"};label{text=\"" + tile.tilex + "\"};label{text=\"" + tile.tiley + "\"};label{text=\"" + theTile.getDesc() + "\"};");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String wallsTable(Wall[] lWalls, int tilex, int tiley) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"Total # of walls:" + lWalls.length + "\"};");
        int pages = (lWalls.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < lWalls.length ? start + this.rowsPerPage : lWalls.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"8\";label{text=\"\"};label{type=\"bold\";text=\"Wurm Id\"};label{type=\"bold\";text=\"QL\"};label{type=\"bold\";text=\"DMG\"};label{type=\"bold\";text=\"Border\"}label{type=\"bold\";text=\"Name\"};label{type=\"bold\";text=\"Level\"};label{type=\"bold\";text=\"Indoor\"};");
            for (int x = start; x < last; ++x) {
                Wall lWall = lWalls[x];
                String sBorder = tilex == lWall.getStartX() - 1 ? "East" : (tiley == lWall.getStartY() - 1 ? "South" : (lWall.isHorizontal() ? "North" : "West"));
                buf.append("radio{group=\"tid\";id=\"1," + lWall.getId() + "," + 1 + "\"};label{text=\"" + lWall.getId() + "\"};label{text=\"" + this.df.format(lWall.getQualityLevel()) + "\"};label{text=\"" + this.df.format(lWall.getDamage()) + "\"};label{text=\"" + sBorder + "\"}label{text=\"" + lWall.getName() + "\"};label{text=\"" + lWall.getFloorLevel() + "\"};label{" + this.showBoolean(lWall.isIndoor()) + "};");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String floorsTable(Floor[] lFloors) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"Total # of floors:" + lFloors.length + "\"};");
        int pages = (lFloors.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < lFloors.length ? start + this.rowsPerPage : lFloors.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"7\";label{text=\"\"};label{type=\"bold\";text=\"Wurm Id\"};label{type=\"bold\";text=\"QL\"};label{type=\"bold\";text=\"DMG\"};label{type=\"bold\";text=\"Name\"};label{type=\"bold\";text=\"Level\"};label{type=\"bold\";text=\"Dir\"};");
            for (int x = start; x < last; ++x) {
                Floor lFloor = lFloors[x];
                buf.append("radio{group=\"tid\";id=\"1," + lFloor.getId() + "," + 1 + "\"};label{text=\"" + lFloor.getId() + "\"};label{text=\"" + this.df.format(lFloor.getQualityLevel()) + "\"};label{text=\"" + this.df.format(lFloor.getDamage()) + "\"};label{text=\"" + lFloor.getName() + "\"};label{text=\"" + lFloor.getFloorLevel() + "\"};label{text=\"" + lFloor.getDir() + "\"};");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String bridgePartTable(BridgePart[] lBridgeParts) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"Total # of Bridge Parts:" + lBridgeParts.length + "\"};");
        int pages = (lBridgeParts.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < lBridgeParts.length ? start + this.rowsPerPage : lBridgeParts.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"7\";label{text=\"\"};label{type=\"bold\";text=\"Wurm Id\"};label{type=\"bold\";text=\"QL\"};label{type=\"bold\";text=\"DMG\"};label{type=\"bold\";text=\"Name\"};label{type=\"bold\";text=\"Dir\"};label{type=\"bold\";text=\"Slope\"};");
            for (int x = start; x < last; ++x) {
                BridgePart lBridgePart = lBridgeParts[x];
                buf.append("radio{group=\"tid\";id=\"1," + lBridgePart.getId() + "," + 1 + "\"};label{text=\"" + lBridgePart.getId() + "\"};label{text=\"" + this.df.format(lBridgePart.getQualityLevel()) + "\"};label{text=\"" + this.df.format(lBridgePart.getDamage()) + "\"};label{text=\"" + lBridgePart.getName() + "\"};label{text=\"" + lBridgePart.getDir() + "\"};label{text=\"" + lBridgePart.getSlope() + "\"};");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String bridgePartsTable(BridgePart[] lBridgeParts, int tilex, int tiley) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"Total # of bridge parts:" + lBridgeParts.length + "\"};");
        int pages = (lBridgeParts.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < lBridgeParts.length ? start + this.rowsPerPage : lBridgeParts.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"5\";label{text=\"\"};label{type=\"bold\";text=\"Wurm Id\"};label{type=\"bold\";text=\"QL\"};label{type=\"bold\";text=\"DMG\"};label{type=\"bold\";text=\"Name\"};");
            for (int x = start; x < last; ++x) {
                BridgePart lBridgePart = lBridgeParts[x];
                buf.append("radio{group=\"tid\";id=\"1," + lBridgePart.getId() + "," + 1 + "\"};label{text=\"" + lBridgePart.getId() + "\"};label{text=\"" + this.df.format(lBridgePart.getQualityLevel()) + "\"};label{text=\"" + this.df.format(lBridgePart.getDamage()) + "\"};label{text=\"" + lBridgePart.getName() + "\"};");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String fencesTable(Fence[] lFences, int tilex, int tiley) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"Total # of fences:" + lFences.length + "\"};");
        int pages = (lFences.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < lFences.length ? start + this.rowsPerPage : lFences.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"7\";label{text=\"\"};label{type=\"bold\";text=\"Wurm Id\"};label{type=\"bold\";text=\"QL\"};label{type=\"bold\";text=\"DMG\"};label{type=\"bold\";text=\"Border\"};label{type=\"bold\";text=\"Name\"};label{type=\"bold\";text=\"Level\"};");
            for (int x = start; x < last; ++x) {
                Fence lFence = lFences[x];
                String sBorder = tilex > -1 && tilex == lFence.getTileX() - 1 ? "East" : (tiley > -1 && tiley == lFence.getTileY() - 1 ? "South" : (lFence.isHorizontal() ? "North" : "West"));
                buf.append("radio{group=\"tid\";id=\"1," + lFence.getId() + "," + 1 + "\"};label{text=\"" + lFence.getId() + "\"};label{text=\"" + this.df.format(lFence.getQualityLevel()) + "\"};label{text=\"" + this.df.format(lFence.getDamage()) + "\"};label{text=\"" + sBorder + "\"};label{text=\"" + lFence.getName() + "\"};label{text=\"" + lFence.getFloorLevel() + "\"};");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String gmSignsTable(Item[] litems) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"total # of GM Signs:" + litems.length + "\"};");
        Arrays.sort(litems);
        int pages = (litems.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < litems.length ? start + this.rowsPerPage : litems.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"7\";label{text=\"\"};label{type=\"bold\";text=\"Wurm Id\"};label{type=\"bold\";text=\"QL\"};label{type=\"bold\";text=\"DAM\"};label{text=\"\"};label{type=\"bold\";text=\"Coords\"};label{type=\"bold\";text=\"Description\"};");
            for (int x = start; x < last; ++x) {
                int posX = litems[x].getTileX();
                int posY = litems[x].getTileY();
                boolean onS = litems[x].isOnSurface();
                buf.append("radio{group=\"tid\";id=\"1," + litems[x].getWurmId() + "," + 1 + "\"};label{text=\"" + litems[x].getWurmId() + "\"};label{text=\"" + this.df.format(litems[x].getQualityLevel()) + "\"};label{text=\"" + this.df.format(litems[x].getDamage()) + "\"};radio{group=\"tid\";id=\"" + 12 + "," + posX + "," + posY + "," + onS + "\"};label{text=\"(" + posX + "," + posY + "," + onS + ")\"}label{text=\"" + litems[x].getDescription() + "\"}");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String itemsTable(Item[] litems) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"total # of items:" + litems.length + "\"};");
        Arrays.sort(litems);
        int pages = (litems.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < litems.length ? start + this.rowsPerPage : litems.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"7\";label{text=\"\"};label{type=\"bold\";text=\"Wurm Id\"};label{type=\"bold\";text=\"QL\"};label{type=\"bold\";text=\"DAM\"};label{type=\"bold\";text=\"Name\"};label{type=\"bold\";text=\"Aux\"};label{type=\"bold\";text=\"Is busy?\"};");
            for (int x = start; x < last; ++x) {
                String aux = "" + litems[x].getAuxData();
                if (litems[x].isRoadMarker()) {
                    aux = MethodsHighways.getLinkAsString(litems[x].getAuxData()) + "(" + aux + ")";
                }
                buf.append("radio{group=\"tid\";id=\"1," + litems[x].getWurmId() + "," + 1 + "\"};label{text=\"" + litems[x].getWurmId() + "\"};label{text=\"" + this.df.format(litems[x].getQualityLevel()) + "\"};label{text=\"" + this.df.format(litems[x].getDamage()) + "\"};" + GmTool.itemNameWithColorByRarity(litems[x]) + "label{text=\"" + aux + "\"};label{" + this.showBoolean(litems[x].isBusy()) + "};");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String traderTable(Shop[] lShops) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"total # of traders:" + lShops.length + "\"};");
        int pages = (lShops.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < lShops.length ? start + this.rowsPerPage : lShops.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"8\";label{type=\"bold\";text=\"TP\"};label{type=\"bold\";text=\"Wurm Id\"};label{type=\"bold\";text=\"X\"};label{type=\"bold\";text=\"Y\"};label{type=\"bold\";text=\"Name\"};label{type=\"bold\";text=\"vTP\"};label{type=\"bold\";text=\"Village\"};label{type=\"bold\";text=\"Kingdom\"};");
            for (int x = start; x < last; ++x) {
                try {
                    Creature trader = Creatures.getInstance().getCreature(lShops[x].getWurmId());
                    if (trader.getCurrentVillage() == null) {
                        buf.append("radio{group=\"tid\";id=\"12," + trader.getTileX() + "," + trader.getTileY() + "," + trader.isOnSurface() + "\"};label{text=\"" + lShops[x].getWurmId() + "\"};label{text=\"" + trader.getTileX() + "\"};label{text=\"" + trader.getTileY() + "\"};label{text=\"" + trader.getName() + "\"};label{text=\"\"};label{text=\"n/a\"};label{text=\"" + Kingdoms.getNameFor(trader.getCurrentKingdom()) + "\"};");
                        continue;
                    }
                    buf.append("radio{group=\"tid\";id=\"12," + trader.getTileX() + "," + trader.getTileY() + "," + trader.isOnSurface() + "\"};label{text=\"" + lShops[x].getWurmId() + "\"};label{text=\"" + trader.getTileX() + "\"};label{text=\"" + trader.getTileY() + "\"};label{text=\"" + trader.getName() + "\"};radio{group=\"tid\";id=\"" + 12 + "," + trader.getCurrentVillage().getTokenX() + "," + trader.getCurrentVillage().getTokenY() + ",true\"};label{text=\"" + trader.getCurrentVillage().getName() + "\"};label{text=\"" + Kingdoms.getNameFor(trader.getCurrentKingdom()) + "\"};");
                    continue;
                }
                catch (NoSuchCreatureException nsc) {
                    buf.append("label{text=\"\"}label{text=\"" + lShops[x].getWurmId() + "\"};label{text=\"n/a\"};label{text=\"n/a\"};label{text=\"unknown\"};label{text=\"\"};label{text=\"n/a\"};label{text=\"n/a\"};");
                }
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String recipeTable(Recipe[] lRecipes) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"total # of named recipes:" + lRecipes.length + "\"};");
        int pages = (lRecipes.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < lRecipes.length ? start + this.rowsPerPage : lRecipes.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"4\";label{text=\"\"};label{type=\"bold\";text=\"Recipe Id\"};label{type=\"bold\";text=\"Recipe Name\"};label{type=\"bold\";text=\"Namer\"};");
            for (int x = start; x < last; ++x) {
                String namer = Recipes.getRecipeNamer(lRecipes[x].getRecipeId());
                buf.append("label{text=\"\"};label{text=\"" + lRecipes[x].getRecipeId() + "\"};label{text=\"" + lRecipes[x].getRecipeName() + "\"};label{text=\"" + namer + "\"};");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"No recipes are named\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String routesList() {
        Route[] routes = Routes.getAllRoutes();
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"total # of highway routes:" + routes.length + "\"};");
        int pages = (routes.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < routes.length ? start + this.rowsPerPage : routes.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"14\";label{text=\"\"};label{type=\"bold\";text=\"Id\"};label{text=\"\"};label{type=\"bold\";text=\"Coords\"};label{text=\"\"};label{type=\"bold\";text=\"Start node\"};label{text=\"\"};label{type=\"bold\";text=\"Catseyes\"};label{text=\"\"};label{type=\"bold\";text=\"Coords\"};label{text=\"\"};label{type=\"bold\";text=\"End waystone\"};label{type=\"bold\";text=\"Dist\"};label{type=\"bold\";text=\"Cost\"};");
            for (int x = start; x < last; ++x) {
                Node startNode = routes[x].getStartNode();
                int posX = startNode.getWaystone().getTileX();
                int posY = startNode.getWaystone().getTileY();
                boolean onS = startNode.getWaystone().isOnSurface();
                buf.append("radio{group=\"tid\";id=\"15," + routes[x].getId() + "," + 20 + "\"};");
                buf.append("label{text=\"" + routes[x].getId() + "\"};");
                buf.append("radio{group=\"tid\";id=\"12," + posX + "," + posY + "," + onS + "\"};");
                buf.append("label{text=\"(" + posX + "," + posY + "," + onS + ")\"}");
                buf.append("radio{group=\"tid\";id=\"15," + startNode.getWaystone().getWurmId() + "," + 22 + "\"};");
                buf.append("label{text=\"" + startNode.getWaystone().getWurmId() + "\"};");
                buf.append("radio{group=\"tid\";id=\"15," + routes[x].getId() + "," + 23 + "\"};");
                buf.append("label{text=\"" + routes[x].getCatseyes().length + " catseyes\"};");
                Node endNode = routes[x].getEndNode();
                if (endNode != null) {
                    int endX = endNode.getWaystone().getTileX();
                    int endY = endNode.getWaystone().getTileY();
                    boolean enS = endNode.getWaystone().isOnSurface();
                    buf.append("radio{group=\"tid\";id=\"12," + endX + "," + endY + "," + enS + "\"};");
                    buf.append("label{text=\"(" + endX + "," + endY + "," + enS + ")\"}");
                    buf.append("radio{group=\"tid\";id=\"15," + endNode.getWaystone().getWurmId() + "," + 22 + "\"};");
                    buf.append("label{text=\"" + endNode.getWaystone().getWurmId() + "\"};");
                } else {
                    buf.append("label{text=\"\"};");
                    buf.append("label{text=\"\"};");
                    buf.append("label{text=\"\"};");
                    buf.append("label{text=\"missing\"};");
                }
                buf.append("label{text=\"" + this.df.format(routes[x].getDistance()) + "\"};");
                buf.append("label{text=\"" + this.df.format(routes[x].getCost()) + "\"};");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"No highway routes found\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String wagonerList() {
        Wagoner[] wagoners = Wagoner.getAllWagoners();
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"total # of wagoners:" + wagoners.length + "\"};");
        int pages = (wagoners.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < wagoners.length ? start + this.rowsPerPage : wagoners.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"12\";label{text=\"\"};label{type=\"bold\";text=\"Wurm Id\"};label{text=\"\"};label{type=\"bold\";text=\"Name\"};label{text=\"\"};label{type=\"bold\";text=\"Current Coords\"};label{text=\"\"};label{type=\"bold\";text=\"State\"};label{text=\"\"};label{type=\"bold\";text=\"Camp Coords\"};label{text=\"\"};label{type=\"bold\";text=\"Doing Delivery\"};");
            for (int x = start; x < last; ++x) {
                int posX = -1;
                int posY = -1;
                boolean onS = false;
                Creature creature = wagoners[x].getCreature();
                if (creature != null) {
                    posX = creature.getTileX();
                    posY = creature.getTileY();
                    onS = creature.isOnSurface();
                }
                int campX = -1;
                int campY = -1;
                boolean campS = true;
                try {
                    Item home = Items.getItem(wagoners[x].getHomeWaystoneId());
                    campX = home.getTileX();
                    campY = home.getTileY();
                    campS = home.isOnSurface();
                }
                catch (NoSuchItemException noSuchItemException) {
                    // empty catch block
                }
                buf.append("radio{group=\"tid\";id=\"16," + wagoners[x].getWurmId() + "," + 1 + "\"};");
                buf.append("label{text=\"" + wagoners[x].getWurmId() + "\"};");
                buf.append("label{text=\"\"}");
                buf.append("label{text=\"" + wagoners[x].getName() + "\"};");
                buf.append("radio{group=\"tid\";id=\"12," + posX + "," + posY + "," + onS + "\"};");
                buf.append("label{text=\"(" + posX + "," + posY + "," + onS + ")\"}");
                buf.append("label{text=\"\"}");
                buf.append("label{text=\"" + wagoners[x].getStateName() + "\"};");
                if (campX == -1) {
                    buf.append("label{text=\"\"}");
                    buf.append("label{text=\"camp missing ?\"};");
                } else {
                    buf.append("radio{group=\"tid\";id=\"12," + campX + "," + campY + "," + campS + "\"};");
                    buf.append("label{text=\"(" + campX + "," + campY + "," + campS + ")\"}");
                }
                if (wagoners[x].getDeliveryId() == -10L) {
                    buf.append("label{text=\"\"}");
                    buf.append("label{text=\"none\"}");
                    continue;
                }
                buf.append("label{text=\"\"}");
                buf.append("label{text=\"" + wagoners[x].getDeliveryId() + "\"}");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"No wagoners found\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String nodesList() {
        Node[] nodes = Routes.getAllNodes();
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"total # of highway nodes:" + nodes.length + "\"};");
        int pages = (nodes.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < nodes.length ? start + this.rowsPerPage : nodes.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"36\";label{text=\"\"};label{type=\"bold\";text=\"Highway Node\"};label{text=\"\"};label{type=\"bold\";text=\"Coords\"};label{text=\"\"};label{type=\"bold\";text=\"N\"};label{text=\"\"};label{text=\"\"};label{text=\"\"};label{type=\"bold\";text=\"NE\"};label{text=\"\"};label{text=\"\"};label{text=\"\"};label{type=\"bold\";text=\"E\"};label{text=\"\"};label{text=\"\"};label{text=\"\"};label{type=\"bold\";text=\"SE\"};label{text=\"\"};label{text=\"\"};label{text=\"\"};label{type=\"bold\";text=\"S\"};label{text=\"\"};label{text=\"\"};label{text=\"\"};label{type=\"bold\";text=\"SW\"};label{text=\"\"};label{text=\"\"};label{text=\"\"};label{type=\"bold\";text=\"W\"};label{text=\"\"};label{text=\"\"};label{text=\"\"};label{type=\"bold\";text=\"NW\"};label{text=\"\"};label{text=\"\"};");
            for (int x = start; x < last; ++x) {
                int posX = nodes[x].getWaystone().getTileX();
                int posY = nodes[x].getWaystone().getTileY();
                boolean onS = nodes[x].getWaystone().isOnSurface();
                buf.append("radio{group=\"tid\";id=\"15," + nodes[x].getWaystone().getWurmId() + "," + 22 + "\"};");
                buf.append("label{text=\"" + nodes[x].getWaystone().getWurmId() + "\"};");
                buf.append("radio{group=\"tid\";id=\"12," + posX + "," + posY + "," + onS + "\"};");
                buf.append("label{text=\"(" + posX + "," + posY + "," + onS + ")\"}");
                buf.append(this.nodeRow(nodes[x], (byte)1));
                buf.append(this.nodeRow(nodes[x], (byte)2));
                buf.append(this.nodeRow(nodes[x], (byte)4));
                buf.append(this.nodeRow(nodes[x], (byte)8));
                buf.append(this.nodeRow(nodes[x], (byte)16));
                buf.append(this.nodeRow(nodes[x], (byte)32));
                buf.append(this.nodeRow(nodes[x], (byte)64));
                buf.append(this.nodeRow(nodes[x], (byte)-128));
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"No highway nodes found\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String nodeRow(Node node, byte dir) {
        StringBuilder buf = new StringBuilder();
        Route route = node.getRoute(dir);
        if (route != null) {
            Route oppRoute = route.getOppositeRoute();
            buf.append("radio{group=\"tid\";id=\"15," + route.getId() + "," + 20 + "\"};");
            buf.append("label{color=\"127,255,127\"text=\"R" + route.getId() + "\"};");
            if (oppRoute == null) {
                buf.append("label{text=\"\"};");
                buf.append("label{text=\"\"};");
            } else {
                buf.append("radio{group=\"tid\";id=\"15," + oppRoute.getId() + "," + 20 + "\"};");
                buf.append("label{color=\"255,177,40\"text=\"R" + oppRoute.getId() + "\"};");
            }
        } else {
            buf.append("label{text=\"\"};");
            buf.append("label{text=\"\"};");
            buf.append("label{text=\"\"};");
            buf.append("label{text=\"\"};");
        }
        return buf.toString();
    }

    private String riftsTable() {
        StringBuilder buf = new StringBuilder();
        Rift activeRift = Rift.getActiveRift();
        Rift lastRift = Rift.getLastRift();
        int count = 0;
        if (activeRift != null) {
            ++count;
        }
        if (lastRift != null) {
            ++count;
        }
        buf.append("label{text=\"total # of rifts:" + count + "\"};");
        if (count > 0) {
            buf.append("table{rows=\"1\";cols=\"7\";label{text=\"\"};label{type=\"bold\";text=\"Type\"};label{type=\"bold\";text=\"Id\"};label{type=\"bold\";text=\"\"};label{type=\"bold\";text=\"Coords\"};label{type=\"bold\";text=\"Started\"};label{type=\"bold\";text=\"Ended\"};");
            if (activeRift != null) {
                buf.append(this.riftRow(activeRift));
                if (lastRift != null && lastRift != activeRift) {
                    buf.append(this.riftRow(lastRift));
                }
            } else if (lastRift != null) {
                buf.append(this.riftRow(lastRift));
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        return buf.toString();
    }

    private String riftRow(Rift rift) {
        int posX = rift.getCenterPos().x;
        int posY = rift.getCenterPos().y;
        String type = rift.isActive() ? "Active" : "Last";
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"\"};label{text=\"" + type + "\"};label{text=\"" + rift.getNumber() + "\"};radio{group=\"tid\";id=\"" + 12 + "," + posX + "," + posY + ",true\"};label{text=\"(" + posX + "," + posY + ",true)\"}label{text=\"" + rift.getActivated().toString() + "\"};label{text=\"" + (rift.isActive() ? "Running" : rift.getEnded().toString()) + "\"};");
        return buf.toString();
    }

    private String serversTable(ServerEntry[] lServers) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"total # of servers:" + lServers.length + "\"};");
        Arrays.sort(lServers);
        int pages = (lServers.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < lServers.length ? start + this.rowsPerPage : lServers.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"11\";label{text=\"\"};label{type=\"bold\";text=\"Id\"};label{type=\"bold\";text=\"Name\"};label{type=\"bold\";text=\"Abbreviation\"};label{type=\"bold\";text=\"Local\"};label{type=\"bold\";text=\"PvP\"};label{type=\"bold\";text=\"Epic\"};label{type=\"bold\";text=\"Chaos\"};label{type=\"bold\";text=\"HomeServer\"};label{type=\"bold\";text=\"Chaos\"};label{type=\"bold\";text=\"Kingdom\"};");
            for (int x = start; x < last; ++x) {
                buf.append("radio{group=\"tid\";id=\"14," + lServers[x].getId() + "," + 1 + "\"};label{text=\"" + lServers[x].getId() + "\"};label{text=\"" + lServers[x].getName() + "\"};label{text=\"" + lServers[x].getAbbreviation() + "\"};label{" + this.showBoolean(lServers[x].isLocal) + "};label{" + this.showBoolean(lServers[x].PVPSERVER) + "};label{" + this.showBoolean(lServers[x].EPIC) + "};label{" + this.showBoolean(lServers[x].isChaosServer()) + "};label{" + this.showBoolean(lServers[x].HOMESERVER) + "};label{" + this.showBoolean(lServers[x].ISPAYMENT) + "};label{text=\"" + lServers[x].getKingdom() + "\"};");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String deityAltarsTable(Item[] altars, String aType) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"total # of " + aType + " Altars:" + altars.length + "\"};");
        Arrays.sort(altars);
        int pages = (altars.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < altars.length ? start + this.rowsPerPage : altars.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"6\";label{text=\"\"};label{type=\"bold\";text=\"Wurm Id\"};label{type=\"bold\";text=\"Name\"};label{type=\"bold\";text=\"\"};label{type=\"bold\";text=\"Coords\"};label{type=\"bold\";text=\"Owner\"};");
            for (int x = start; x < last; ++x) {
                PlayerInfo pInfo;
                Item item = altars[x];
                int posX = item.getTileX();
                int posY = item.getTileY();
                boolean onSurface = item.isOnSurface();
                String owner = "";
                owner = item.getOwnerId() != -10L ? ((pInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(item.getOwnerId())) != null ? pInfo.getName() : "" + item.getOwnerId()) : ((pInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(item.lastOwner)) != null ? pInfo.getName() : "" + item.lastOwner);
                buf.append("radio{group=\"tid\";id=\"1," + altars[x].getWurmId() + "," + 1 + "\"};label{text=\"" + altars[x].getWurmId() + "\"};" + GmTool.itemNameWithColorByRarity(altars[x]) + "radio{group=\"tid\";id=\"" + 12 + "," + posX + "," + posY + "," + onSurface + "\"};label{text=\"(" + posX + "," + posY + "," + onSurface + ")\"}label{text=\"" + owner + "\"}");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String corpseTable(Set<Item> corpses, PlayerInfo pInfo) {
        Item[] items = corpses.toArray(new Item[corpses.size()]);
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"total # of Corpses of " + pInfo.getName() + ":" + corpses.size() + "\"};");
        int pages = (corpses.size() - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < corpses.size() ? start + this.rowsPerPage : corpses.size();
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"5\";label{text=\"\"};label{type=\"bold\";text=\"Wurm Id\"};label{type=\"bold\";text=\"Name\"};label{type=\"bold\";text=\"\"};label{type=\"bold\";text=\"Coords\"};");
            for (int x = start; x < last; ++x) {
                Item item = items[x];
                int posX = item.getTileX();
                int posY = item.getTileY();
                boolean onSurface = item.isOnSurface();
                buf.append("radio{group=\"tid\";id=\"1," + items[x].getWurmId() + "," + 1 + "\"};label{text=\"" + items[x].getWurmId() + "\"};" + GmTool.itemNameWithColorByRarity(items[x]) + "radio{group=\"tid\";id=\"" + 12 + "," + posX + "," + posY + "," + onSurface + "\"};label{text=\"(" + posX + "," + posY + "," + onSurface + ")\"}");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String mailTable(WurmMail[] mails) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"total # of items:" + mails.length + "\"};");
        int pages = (mails.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < mails.length ? start + this.rowsPerPage : mails.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"5\";label{text=\"\"};label{type=\"bold\";text=\"Sender\"};label{type=\"bold\";text=\"To\"};label{type=\"bold\";text=\"Name\"};label{type=\"bold\";text=\"Price\"};");
            for (int x = start; x < last; ++x) {
                PlayerState sps = PlayerInfoFactory.getPlayerState(mails[x].sender);
                PlayerState rps = PlayerInfoFactory.getPlayerState(mails[x].receiver);
                String sName = sps != null ? sps.getPlayerName() : "(Unk:" + mails[x].sender + ")";
                String rName = rps != null ? rps.getPlayerName() : "(Unk:" + mails[x].receiver + ")";
                try {
                    Item item = Items.getItem(mails[x].itemId);
                    ServerEntry se = Servers.getServerWithId(mails[x].getSourceserver());
                    String sourceServer = " (Unk)";
                    if (se != null) {
                        sourceServer = " (" + se.getAbbreviation() + ")";
                    }
                    buf.append("radio{group=\"tid\";id=\"1," + mails[x].itemId + "," + 1 + "\"};label{text=\"" + sName + sourceServer + "\"};label{text=\"" + rName + "\"};" + GmTool.itemNameWithColorByRarity(item) + "label{text=\"" + (mails[x].getType() == 0 ? "" : "COD ") + new Change(mails[x].getPrice()).getChangeShortString() + "\"};");
                    continue;
                }
                catch (NoSuchItemException e) {
                    logger.log(Level.WARNING, e.getMessage(), e);
                    buf.append("label{text=\"\"};label{text=\"" + sName + "\"};label{text=\"" + rName + "\"};label{type=\"italic\";text=\"" + mails[x].itemId + "\"};label{text=\"" + (mails[x].getType() == 0 ? "" : "COD ") + new Change(mails[x].getPrice()).getChangeShortString() + "\"};");
                }
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String villageHistoryTable(HistoryEvent[] histories) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"# of history entries:" + histories.length + "\"};");
        int pages = (histories.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < histories.length ? start + this.rowsPerPage : histories.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"3\";label{text=\"Dated\"};label{text=\"Who\"};label{text=\"Event\"};");
            for (int x = start; x < last; ++x) {
                HistoryEvent hEvent = histories[x];
                buf.append("label{text=\"" + hEvent.getDate() + "\"};label{text=\"" + (hEvent.performer == null ? "" : hEvent.performer) + "\"};label{text=\"" + hEvent.event + "\"};");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String historyTable(PermissionsHistoryEntry[] histories) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"# of history entries:" + histories.length + "\"};");
        int pages = (histories.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < histories.length ? start + this.rowsPerPage : histories.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"3\";label{text=\"Dated\"};label{text=\"Who\"};label{text=\"Event\"};");
            for (int x = start; x < last; ++x) {
                PermissionsHistoryEntry hEvent = histories[x];
                buf.append("label{text=\"" + hEvent.getDate() + "\"};label{text=\"" + (hEvent.performer == null ? "" : hEvent.performer) + "\"};label{text=\"" + hEvent.event + "\"};");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String kosVillageTable(Village[] lvillages, PlayerInfo pInfo) {
        StringBuilder buf = new StringBuilder();
        Arrays.sort(lvillages);
        buf.append("label{text=\"total # of villages:" + lvillages.length + "\"};");
        int pages = (lvillages.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < lvillages.length ? start + this.rowsPerPage : lvillages.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"3\";label{text=\"\"};label{type=\"bold\";text=\"Village\"};label{type=\"bold\";text=\"Reputation\"};");
            for (int x = start; x < last; ++x) {
                Village lvillage = lvillages[x];
                Reputation rep = lvillage.getReputationObject(pInfo.wurmId);
                buf.append("radio{group=\"tid\";id=\"2," + lvillage.getId() + "," + 1 + "\"};label{text=\"" + lvillage.getName() + "\"};label{text=\"" + rep.getValue() + "\"};");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String deityTable(Deity[] deities) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"total # of Deities:" + deities.length + "\"};");
        int pages = (deities.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < deities.length ? start + this.rowsPerPage : deities.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"9\";label{text=\"\"};label{type=\"bold\";text=\"Id\"};label{type=\"bold\";text=\"Name\"};label{text=\"\"};label{type=\"bold\";text=\"Priests\"};label{text=\"\"};label{type=\"bold\";text=\"Followers\"};label{text=\"\"};label{type=\"bold\";text=\"Altars\"};");
            for (int x = start; x < last; ++x) {
                int numbPriests = PlayerInfoFactory.getActivePriestsForDeity(deities[x].getNumber()).length;
                int numbFollowers = PlayerInfoFactory.getActiveFollowersForDeity(deities[x].getNumber()).length;
                int numbAltars = Zones.getAltars(deities[x].getNumber()).length;
                buf.append("radio{group=\"tid\";id=\"13," + deities[x].getNumber() + "," + 1 + "\"};label{text=\"" + deities[x].getNumber() + "\"};label{text=\"" + deities[x].getName() + "\"}" + (numbPriests == 0 ? "label{text=\"\"}" : "radio{group=\"tid\";id=\"13," + deities[x].getNumber() + "," + 7 + "\"};") + "label{text=\"" + numbPriests + "\"}" + (numbFollowers == 0 ? "label{text=\"\"}" : "radio{group=\"tid\";id=\"13," + deities[x].getNumber() + "," + 8 + "\"};") + "label{text=\"" + numbFollowers + "\"}" + (numbAltars == 0 ? "label{text=\"\"}" : "radio{group=\"tid\";id=\"13," + deities[x].getNumber() + "," + 9 + "\"};") + "label{text=\"" + numbAltars + "\"}");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String deityValreiFightsListTable(ArrayList<ValreiFightHistory> fights) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"total # of fights:" + fights.size() + "\"};");
        int pages = (fights.size() - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < fights.size() ? start + this.rowsPerPage : fights.size();
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"3\";label{text=\"\"};label{type=\"bold\";text=\"FightId\"};label{type=\"bold\";text=\"Preview\"};");
            for (int x = start; x < last; ++x) {
                ValreiFightHistory fight = fights.get(x);
                buf.append("radio{group=\"tid\";id=\"13," + fight.getFightId() + "," + 10 + "\"};label{text=\"" + fight.getFightId() + "\"};label{text=\"" + fight.getPreviewString() + "\"}");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String deityFollowersTable(PlayerInfo[] followers, String deityName, String aType) {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"total # of " + deityName + " " + aType + ":" + followers.length + "\"};");
        Arrays.sort(followers);
        int pages = (followers.length - 1) / this.rowsPerPage;
        int start = this.currentPage * this.rowsPerPage;
        int last = start + this.rowsPerPage < followers.length ? start + this.rowsPerPage : followers.length;
        String nav = this.makeNav(this.currentPage, pages);
        if (pages > 0) {
            buf.append(nav);
        }
        if (last > start) {
            buf.append("table{rows=\"" + (last - start + 1) + "\";cols=\"6\";label{text=\"\"};label{type=\"bold\";text=\"Wurm Id\"};label{type=\"bold\";text=\"Name\"};label{type=\"bold\";text=\"Priest\"};label{type=\"bold\";text=\"Faith\"};label{type=\"bold\";text=\"Favor\"};");
            for (int x = start; x < last; ++x) {
                buf.append("radio{group=\"tid\";id=\"1," + followers[x].wurmId + "," + 1 + "\"};label{text=\"" + followers[x].wurmId + "\"};label{text=\"" + followers[x].getName() + "\"};label{" + this.showBoolean(followers[x].isPriest) + "};label{text=\"" + followers[x].getFaith() + "\"}label{text=\"" + followers[x].getFavor() + "\"}");
            }
            buf.append("}");
        } else {
            buf.append("label{text=\"Nothing to show\"}");
        }
        if (pages > 0) {
            buf.append(nav);
        }
        return buf.toString();
    }

    private String makeNav(int aCurrentPage, int aTotalPages) {
        if (aTotalPages > 0) {
            return "harray{" + (aCurrentPage > 0 ? "radio{group=\"tid\";id=\"7\";text=\"Prev \"};" : "") + "label{text=\"Page " + (aCurrentPage + 1) + " of " + (aTotalPages + 1) + " \"};" + (aCurrentPage < aTotalPages ? "radio{group=\"tid\";id=\"8\";text=\"Next\"};" : "") + "}";
        }
        return "";
    }

    private String addBridge(long bridgeId) {
        StringBuilder buf;
        block5: {
            buf = new StringBuilder();
            if (bridgeId != -10L) {
                try {
                    Structure lStructure = Structures.getStructure(bridgeId);
                    if (lStructure != null && lStructure.isTypeBridge()) {
                        buf.append("radio{group=\"tid\";id=\"1," + bridgeId + "," + 1 + "\"};label{text=\"On Bridge\"};label{text=\"\"};label{text=\"" + lStructure.getName() + "\"};");
                        break block5;
                    }
                    buf.append("label{text=\"\"};label{text=\"On Bridge\"};label{text=\"\"};label{text=\"Not a bridge? " + bridgeId + "\"};");
                }
                catch (NoSuchStructureException nsse) {
                    buf.append("label{text=\"\"};label{text=\"On Bridge\"};label{text=\"\"};label{text=\"Not Found " + bridgeId + "\"};");
                }
            } else {
                buf.append("label{text=\"\"};label{text=\"On Bridge\"};label{text=\"\"};label{text=\"No\"};");
            }
        }
        return buf.toString();
    }
}

