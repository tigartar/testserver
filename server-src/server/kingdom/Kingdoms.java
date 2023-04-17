/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.kingdom;

import com.wurmonline.server.Features;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.kingdom.GuardTower;
import com.wurmonline.server.kingdom.InfluenceChain;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.Zones;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public final class Kingdoms
implements MiscConstants {
    public static final String KINGDOM_NAME_JENN = "Jenn-Kellon";
    public static final String KINGDOM_CHAT_JENN = "Jenn-Kellon";
    public static final String KINGDOM_NAME_MOLREHAN = "Mol Rehan";
    public static final String KINGDOM_CHAT_MOLREHAN = "Mol Rehan";
    public static final String KINGDOM_NAME_LIBILA = "Horde of the Summoned";
    public static final String KINGDOM_CHAT_HOTS = "HOTS";
    public static final String KINGDOM_NAME_FREEDOM = "Freedom Isles";
    public static final String KINGDOM_CHAT_FREEDOM = "Freedom";
    public static final String KINGDOM_NAME_NONE = "no known kingdom";
    public static final String KINGDOM_SUFFIX_JENN = "jenn.";
    public static final String KINGDOM_SUFFIX_MOLREHAN = "molr.";
    public static final String KINGDOM_SUFFIX_HOTS = "hots.";
    public static final String KINGDOM_SUFFIX_FREEDOM = "free.";
    public static final String KINGDOM_SUFFIX_NONE = "";
    public static int activePremiumJenn = 0;
    public static int activePremiumMolr = 0;
    public static int activePremiumHots = 0;
    public static final int TOWER_INFLUENCE = 60;
    public static final int CHALLENGE_ITEM_INFLUENCE = 20;
    public static final int minKingdomDist = Servers.localServer.isChallengeServer() ? 60 : (Servers.localServer.id == 3 ? 100 : 150);
    public static final int maxTowerDistance = 100;
    private static final Map<Byte, Kingdom> kingdoms = new HashMap<Byte, Kingdom>();
    private static Logger logger = Logger.getLogger(Kingdoms.class.getName());
    private static final ConcurrentHashMap<Item, GuardTower> towers = new ConcurrentHashMap();
    public static final int minOwnTowerDistance = 50;
    public static final int minArcheryTowerDistance = 20;

    public static final void createBasicKingdoms() {
        Kingdoms.addKingdom(new Kingdom(0, 0, KINGDOM_NAME_NONE, "abofk7ba", "none", KINGDOM_SUFFIX_NONE, "Unknown", "Unknown", false));
        Kingdoms.addKingdom(new Kingdom(1, 1, "Jenn-Kellon", "abosdsd", "Jenn-Kellon", KINGDOM_SUFFIX_JENN, "Noble", "Protectors", true));
        Kingdoms.addKingdom(new Kingdom(2, 2, "Mol Rehan", "ajajkjh3d", "Mol Rehan", KINGDOM_SUFFIX_MOLREHAN, "Fire", "Gold", true));
        Kingdoms.addKingdom(new Kingdom(3, 3, KINGDOM_NAME_LIBILA, "11dfkjutyd", KINGDOM_CHAT_HOTS, KINGDOM_SUFFIX_HOTS, "Hate", "Vengeance", true));
        Kingdoms.addKingdom(new Kingdom(4, 4, KINGDOM_NAME_FREEDOM, "asiuytsr", KINGDOM_CHAT_FREEDOM, KINGDOM_SUFFIX_FREEDOM, "Peaceful", "Friendly", true));
    }

    protected static final int numKingdoms() {
        return kingdoms.size();
    }

    private Kingdoms() {
    }

    static int getActivePremiumJenn() {
        return activePremiumJenn;
    }

    static void setActivePremiumJenn(int aActivePremiumJenn) {
        activePremiumJenn = aActivePremiumJenn;
    }

    static int getActivePremiumMolr() {
        return activePremiumMolr;
    }

    static void setActivePremiumMolr(int aActivePremiumMolr) {
        activePremiumMolr = aActivePremiumMolr;
    }

    static int getActivePremiumHots() {
        return activePremiumHots;
    }

    static void setActivePremiumHots(int aActivePremiumHots) {
        activePremiumHots = aActivePremiumHots;
    }

    public static final String getNameFor(byte kingdom) {
        Kingdom k = Kingdoms.getKingdomOrNull(kingdom);
        if (k != null) {
            return k.getName();
        }
        return KINGDOM_NAME_NONE;
    }

    public static final boolean isKingdomChat(String chatTitle) {
        for (Kingdom k : kingdoms.values()) {
            if (!k.getChatName().equals(chatTitle)) continue;
            return true;
        }
        return false;
    }

    public static final boolean isGlobalKingdomChat(String chatTitle) {
        for (Kingdom k : kingdoms.values()) {
            if (!("GL-" + k.getChatName()).equals(chatTitle)) continue;
            return true;
        }
        return false;
    }

    public static final boolean mayCreateKingdom() {
        return kingdoms.size() < 255;
    }

    public static final Kingdom getKingdomWithName(String kname) {
        for (Kingdom k : kingdoms.values()) {
            if (!k.getName().equalsIgnoreCase(kname)) continue;
            return k;
        }
        return null;
    }

    public static final Kingdom getKingdomWithChatTitle(String chatTitle) {
        for (Kingdom k : kingdoms.values()) {
            if (!k.getChatName().equals(chatTitle)) continue;
            return k;
        }
        return null;
    }

    public static final Kingdom getKingdomWithSuffix(String suffix) {
        for (Kingdom k : kingdoms.values()) {
            if (!k.getSuffix().equals(suffix)) continue;
            return k;
        }
        return null;
    }

    public static final void loadKingdom(Kingdom kingdom) {
        Kingdom oldk = kingdoms.get(kingdom.kingdomId);
        if (oldk != null) {
            kingdom.setAlliances(oldk.getAllianceMap());
        }
        kingdoms.put(kingdom.kingdomId, kingdom);
    }

    public static final boolean addKingdom(Kingdom kingdom) {
        boolean isNew = false;
        boolean exists = false;
        Kingdom oldk = kingdoms.get(kingdom.kingdomId);
        if (oldk != null) {
            exists = true;
            kingdom.setAlliances(oldk.getAllianceMap());
            kingdom.setExistsHere(oldk.existsHere());
            kingdom.activePremiums = oldk.activePremiums;
            if (!(oldk.acceptsTransfers() == kingdom.acceptsTransfers() && oldk.getFirstMotto().equals(kingdom.getFirstMotto()) && oldk.getSecondMotto().equals(kingdom.getSecondMotto()) && oldk.getPassword().equals(kingdom.getPassword()))) {
                kingdom.update();
            }
        } else {
            isNew = true;
        }
        kingdoms.put(kingdom.kingdomId, kingdom);
        if (isNew) {
            Players.getInstance().sendKingdomToPlayers(kingdom);
        }
        kingdom.setShouldBeDeleted(false);
        if (!exists) {
            kingdom.saveToDisk();
        }
        return isNew;
    }

    public static void markAllKingdomsForDeletion() {
        Kingdom[] allKingdoms;
        for (Kingdom k : allKingdoms = Kingdoms.getAllKingdoms()) {
            k.setShouldBeDeleted(true);
        }
    }

    public static void trimKingdoms() {
        Kingdom[] allKingdoms;
        for (Kingdom k : allKingdoms = Kingdoms.getAllKingdoms()) {
            if (!k.isShouldBeDeleted()) continue;
            k.delete();
            Kingdoms.removeKingdom(k.getId());
        }
    }

    public static final void removeKingdom(byte id) {
        King.purgeKing(id);
        kingdoms.remove(id);
    }

    public static final Kingdom getKingdomOrNull(byte id) {
        return kingdoms.get(id);
    }

    public static final Kingdom getKingdom(byte id) {
        Kingdom toret = kingdoms.get(id);
        if (toret == null) {
            return kingdoms.get((byte)0);
        }
        return toret;
    }

    public static final byte getKingdomTemplateFor(byte id) {
        Kingdom toret = kingdoms.get(id);
        if (toret == null) {
            return 0;
        }
        return toret.getTemplate();
    }

    public static final Kingdom[] getAllKingdoms() {
        return kingdoms.values().toArray(new Kingdom[kingdoms.values().size()]);
    }

    public static ConcurrentHashMap<Item, GuardTower> getTowers() {
        return towers;
    }

    public static final byte getNextAvailableKingdomId() {
        for (byte b = -128; b < 127; b = (byte)(b + 1)) {
            if (b >= 0 && b <= 4 || kingdoms.get(b) != null) continue;
            return b;
        }
        return 0;
    }

    public static final String getSuffixFor(byte kingdom) {
        Kingdom k = Kingdoms.getKingdomOrNull(kingdom);
        if (k != null) {
            return k.getSuffix();
        }
        return KINGDOM_SUFFIX_NONE;
    }

    public static final String getChatNameFor(byte kingdom) {
        Kingdom k = Kingdoms.getKingdomOrNull(kingdom);
        if (k != null) {
            return k.getChatName();
        }
        return KINGDOM_NAME_NONE;
    }

    public static final void addTower(Item tower) {
        if (!((ConcurrentHashMap.KeySetView)towers.keySet()).contains(tower)) {
            towers.put(tower, new GuardTower(tower));
            Kingdoms.addTowerKingdom(tower);
        }
    }

    public static final void reAddKingdomInfluences(int startx, int starty, int endx, int endy) {
        for (Village v : Villages.getVillagesWithin(startx, starty, endx, endy)) {
            v.setKingdomInfluence();
        }
        Zones.addWarDomains();
        for (Item it : towers.keySet()) {
            if (it.getTileX() < startx || it.getTileX() > endx || it.getTileY() < starty || it.getTileY() >= endy) continue;
            Kingdoms.addTowerKingdom(it);
        }
    }

    public static void addTowerKingdom(Item tower) {
        Kingdom k;
        if (tower.getKingdom() != 0 && tower.getTemplateId() != 996 && (k = Kingdoms.getKingdom(tower.getKingdom())).getId() != 0) {
            for (int x = tower.getTileX() - 60; x < tower.getTileX() + 60; ++x) {
                for (int y = tower.getTileY() - 60; y < tower.getTileY() + 60; ++y) {
                    if (Zones.getKingdom(x, y) != 0) continue;
                    Zones.setKingdom(x, y, tower.getKingdom());
                }
            }
            if (Features.Feature.TOWER_CHAINING.isEnabled()) {
                InfluenceChain.addTowerToChain(k.getId(), tower);
            }
        }
    }

    public static final void removeInfluenceForTower(Item item) {
        boolean extraCheckedTiles = true;
        for (int x = item.getTileX() - 60 - 1; x < item.getTileX() + 60 + 1; ++x) {
            for (int y = item.getTileY() - 60 - 1; y < item.getTileY() + 60 + 1; ++y) {
                if (Zones.getKingdom(x, y) != item.getKingdom() || Villages.getVillageWithPerimeterAt(x, y, true) != null) continue;
                Zones.setKingdom(x, y, (byte)0);
            }
        }
        if (Features.Feature.TOWER_CHAINING.isEnabled()) {
            InfluenceChain.removeTowerFromChain(item.getKingdom(), item);
        }
    }

    public static final void addWarTargetKingdom(Item target) {
        Kingdom k;
        if (target.getKingdom() != 0 && (k = Kingdoms.getKingdom(target.getKingdom())).getId() != 0) {
            int sx = Zones.safeTileX(target.getTileX() - 60);
            int ex = Zones.safeTileX(target.getTileX() + 60);
            int sy = Zones.safeTileY(target.getTileY() - 60);
            int ey = Zones.safeTileY(target.getTileY() + 60);
            for (int x = sx; x <= ex; ++x) {
                for (int y = sy; y <= ey; ++y) {
                    if (Villages.getVillageWithPerimeterAt(x, y, true) != null) continue;
                    Zones.setKingdom(x, y, target.getKingdom());
                }
            }
        }
    }

    public static final void destroyTower(Item item) {
        Kingdoms.destroyTower(item, false);
    }

    public static final void destroyTower(Item item, boolean destroyItem) {
        if (towers == null || towers.size() == 0) {
            GuardTower t = new GuardTower(item);
            t.destroy();
            Items.destroyItem(item.getWurmId());
        } else {
            GuardTower t = towers.get(item);
            if (t != null) {
                t.destroy();
            }
            towers.remove(item);
            if (destroyItem) {
                Items.destroyItem(item.getWurmId());
            }
            Kingdoms.removeInfluenceForTower(item);
            Zones.removeGuardTower(item);
            Kingdoms.reAddKingdomInfluences(item.getTileX() - 200, item.getTileY() - 200, item.getTileX() + 200, item.getTileY() + 200);
        }
    }

    public static final GuardTower getTower(Item tower) {
        return towers.get(tower);
    }

    public static final GuardTower getClosestTower(int tilex, int tiley, boolean surfaced) {
        GuardTower closest = null;
        int minDist = 2000;
        for (GuardTower tower : towers.values()) {
            if (tower.getTower().isOnSurface() != surfaced) continue;
            int distx = Math.abs(tower.getTower().getTileX() - tilex);
            int disty = Math.abs(tower.getTower().getTileY() - tiley);
            if (distx >= 50 || disty >= 50 || distx > minDist && disty > minDist) continue;
            minDist = Math.min(distx, disty);
            closest = tower;
        }
        return closest;
    }

    public static final GuardTower getClosestEnemyTower(int tilex, int tiley, boolean surfaced, Creature searcher) {
        GuardTower closest = null;
        if (searcher.getKingdomId() != 0) {
            int minDist = 2000;
            for (GuardTower tower : towers.values()) {
                if (tower.getTower().isOnSurface() != surfaced) continue;
                int distx = Math.abs(tower.getTower().getTileX() - tilex);
                int disty = Math.abs(tower.getTower().getTileY() - tiley);
                if (distx > minDist && disty > minDist || searcher.isFriendlyKingdom(tower.getKingdom())) continue;
                minDist = Math.min(distx, disty);
                closest = tower;
            }
        }
        return closest;
    }

    public static final Item getClosestWarTarget(int tilex, int tiley, Creature searcher) {
        Item closest = null;
        if (searcher.getKingdomId() != 0) {
            int minDist = 200;
            for (Item target : Items.getWarTargets()) {
                int distx = Math.abs(target.getTileX() - tilex);
                int disty = Math.abs(target.getTileY() - tiley);
                if (distx > minDist && disty > minDist || !searcher.isFriendlyKingdom(target.getKingdom())) continue;
                minDist = Math.min(distx, disty);
                closest = target;
            }
        }
        return closest;
    }

    public static final GuardTower getTower(Creature guard) {
        return guard.getGuardTower();
    }

    public static final GuardTower getRandomTowerForKingdom(byte kingdom) {
        LinkedList<GuardTower> tows = new LinkedList<GuardTower>();
        for (GuardTower tower : towers.values()) {
            if (tower.getKingdom() != kingdom) continue;
            tows.add(tower);
        }
        if (tows.size() > 0) {
            return (GuardTower)tows.get(Server.rand.nextInt(tows.size()));
        }
        return null;
    }

    public static final boolean isTowerTooNear(int tilex, int tiley, boolean surfaced, boolean archery) {
        if (archery) {
            for (Item gt : Items.getAllItems()) {
                if (!gt.isProtectionTower() || gt.isOnSurface() != surfaced || Math.abs(((int)gt.getPosX() >> 2) - tilex) >= 20 || Math.abs(((int)gt.getPosY() >> 2) - tiley) >= 20) continue;
                return true;
            }
        } else {
            for (Item gt : towers.keySet()) {
                if (gt.isOnSurface() != surfaced || Math.abs(((int)gt.getPosX() >> 2) - tilex) >= 50 || Math.abs(((int)gt.getPosY() >> 2) - tiley) >= 50) continue;
                return true;
            }
        }
        return false;
    }

    public static final void convertTowersWithin(int startx, int starty, int endx, int endy, byte newKingdom) {
        for (Item it : towers.keySet()) {
            if (it.getTileX() < startx || it.getTileX() > endx || it.getTileY() < starty || it.getTileY() >= endy) continue;
            Kingdoms.removeInfluenceForTower(it);
            it.setAuxData(newKingdom);
            Kingdoms.addTowerKingdom(it);
            Kingdom k = Kingdoms.getKingdom(newKingdom);
            boolean changed = false;
            if (k != null) {
                String aName = k.getName() + " guard tower";
                it.setName(aName);
                int templateId = 384;
                if (k.getTemplate() == 2) {
                    templateId = 528;
                } else if (k.getTemplate() == 3) {
                    templateId = 430;
                }
                if (k.getTemplate() == 4) {
                    templateId = 638;
                }
                if (it.getTemplateId() != templateId) {
                    it.setTemplateId(templateId);
                    changed = true;
                }
            }
            if (!changed) {
                it.updateIfGroundItem();
            }
            towers.get(it).destroyGuards();
        }
    }

    public static final void poll() {
        Iterator<GuardTower> it = towers.values().iterator();
        while (it.hasNext()) {
            it.next().poll();
        }
        King[] kings = King.getKings();
        if (kings != null) {
            for (King king : kings) {
                try {
                    Player player = Players.getInstance().getPlayer(king.kingid);
                    if (player.getKingdomId() == king.kingdom) continue;
                    king.abdicate(player.isOnSurface(), false);
                }
                catch (NoSuchPlayerException noSuchPlayerException) {
                    // empty catch block
                }
            }
        }
    }

    public static int getNumberOfGuardTowers() {
        int numberOfTowers = towers != null ? towers.size() : 0;
        return numberOfTowers;
    }

    public static void checkIfDisbandKingdom() {
    }

    public static final void destroyTowersWithKingdom(byte deletedKingdom) {
        if (towers != null) {
            for (GuardTower tower : towers.values()) {
                if (tower.getKingdom() != deletedKingdom) continue;
                Kingdoms.destroyTower(tower.getTower(), true);
            }
        }
    }

    public static final boolean isCustomKingdom(byte kingdomId) {
        return kingdomId < 0 || kingdomId > 4;
    }
}

