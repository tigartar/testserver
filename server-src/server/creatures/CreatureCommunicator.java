/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures;

import com.wurmonline.server.Message;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.sounds.Sound;
import com.wurmonline.server.structures.Door;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.zones.VolaTile;
import java.io.IOException;
import java.util.List;

public final class CreatureCommunicator
extends Communicator {
    private final Creature creature;

    public CreatureCommunicator(Creature aCreature) {
        this.creature = aCreature;
        this.justLoggedIn = false;
    }

    @Override
    public void disconnect() {
    }

    @Override
    public void sendMessage(Message message) {
    }

    @Override
    public void sendSafeServerMessage(String message) {
    }

    @Override
    public void sendNormalServerMessage(String message) {
    }

    @Override
    public void sendAlertServerMessage(String message) {
    }

    @Override
    public void sendUpdateInventoryItem(Item item) {
    }

    @Override
    public void sendRemoveFromInventory(Item item) {
    }

    @Override
    public void sendAddToInventory(Item item, long inventoryWindow, long rootid, int price) {
        if (this.creature.getTrade() != null) {
            this.creature.getTradeHandler().addToInventory(item, inventoryWindow);
        }
    }

    @Override
    public void sendStartTrading(Creature opponent) {
    }

    @Override
    public void sendCloseTradeWindow() {
        this.creature.endTrade();
    }

    @Override
    public void sendTradeChanged(int id) {
        this.creature.getTradeHandler().tradeChanged();
    }

    @Override
    public void sendTradeAgree(Creature agreer, boolean agree) {
    }

    @Override
    public void sendUpdateInventoryItem(Item item, long inventoryWindow, int price) {
    }

    @Override
    public void sendRemoveFromInventory(Item item, long inventoryWindow) {
    }

    @Override
    public void sendNewCreature(long id, String name, String model, float x, float y, float z, long onBridge, float rot, byte layer, boolean onGround, boolean floating, boolean isSolid, byte kingdomId, long face, byte blood, boolean isUndead, boolean isCopy, byte modtype) {
    }

    @Override
    public void sendMoveCreature(long id, float x, float y, int rot, boolean isMoving) {
    }

    @Override
    public void sendMoveCreatureAndSetZ(long id, float x, float y, float z, int rot) {
    }

    @Override
    public void sendDeleteCreature(long id) {
    }

    @Override
    public void sendTileStripFar(short xStart, short yStart, int width, int height) {
    }

    @Override
    public void sendTileStrip(short xStart, short yStart, int width, int height) {
    }

    @Override
    public void sendCaveStrip(short xStart, short yStart, int width, int height) {
    }

    @Override
    public void sendAvailableActions(byte requestId, List<ActionEntry> availableActions, String helpstring) {
    }

    @Override
    public void sendItem(Item item, long creatureId, boolean onGroundLevel) {
    }

    @Override
    public void sendRemoveItem(Item item) {
    }

    @Override
    public void sendAddSkill(int id, int parentSkillId, String name, float value, float maxValue, int affinities) {
    }

    @Override
    public void sendUpdateSkill(int id, float value, int affinities) {
    }

    @Override
    public void sendAddEffect(long id, short type, float x, float y, float z, byte layer) {
    }

    @Override
    public void sendRemoveEffect(long id) {
    }

    @Override
    public void sendStamina(int stamina, int damage) {
    }

    @Override
    public void sendThirst(int thirst) {
    }

    @Override
    public void sendHunger(int hunger, float nutrition, float calories, float carbs, float fats, float proteins) {
    }

    @Override
    public void sendWeight(byte weight) {
    }

    @Override
    public void sendSpeedModifier(float speedModifier) {
    }

    @Override
    public void sendTimeLeft(short tenthOfSeconds) {
    }

    @Override
    public void sendSingleBuildMarker(long structureId, int tilex, int tiley, byte layer) {
    }

    @Override
    public void sendMultipleBuildMarkers(long structureId, VolaTile[] tiles, byte layer) {
    }

    @Override
    public void sendAddStructure(String name, short centerTilex, short centerTiley, long structureId, byte structureType, byte layer) {
    }

    @Override
    public void sendRemoveStructure(long structureId) {
    }

    @Override
    public void sendAddWall(long structureId, Wall wall) {
    }

    @Override
    public void sendPassable(boolean passable, Door door) {
    }

    @Override
    public void sendOpenDoor(Door door) {
    }

    @Override
    public void sendCloseDoor(Door door) {
    }

    @Override
    public void sendChangeStructureName(long structureId, String newName) {
    }

    @Override
    public void sendTeleport(boolean aLocal) {
        this.sendTeleport(aLocal, true, (byte)0);
    }

    @Override
    public void sendTeleport(boolean aLocal, boolean disembark, byte commandType) {
        this.creature.teleport();
    }

    @Override
    public void sendOpenInventoryWindow(long inventoryWindow, String title) {
    }

    @Override
    public boolean sendCloseInventoryWindow(long inventoryWindow) {
        return true;
    }

    @Override
    public void sendAddFence(Fence fence) {
    }

    @Override
    public void sendRemoveFence(Fence fence) {
    }

    @Override
    public void sendRename(Item item, String newName, String newModelName) {
    }

    @Override
    public void sendOpenFence(Fence fence, boolean passable, boolean changePassable) {
    }

    @Override
    public void sendCloseFence(Fence fence, boolean passable, boolean changePassable) {
    }

    @Override
    public void sendSound(Sound sound) {
    }

    @Override
    public void sendMusic(Sound sound) {
    }

    @Override
    public void sendStatus(String status) {
    }

    @Override
    public void sendAddWound(Wound wound, Item bodyPart) {
    }

    @Override
    public void sendRemoveWound(Wound wound) {
    }

    @Override
    public void sendUpdateWound(Wound wound, Item bodyPart) {
    }

    @Override
    public void sendAddFriend(String name, long wurmid) {
    }

    @Override
    public void sendRemoveFriend(String name) {
    }

    @Override
    public void sendAddVillager(String name, long wurmid) {
    }

    @Override
    public void sendRemoveVillager(String name) {
    }

    @Override
    public void sendAddGm(String name, long wurmid) {
    }

    @Override
    public void sendRemoveGm(String name) {
    }

    @Override
    public void changeAttitude(long creatureId, byte status) {
    }

    @Override
    public void sendAddLocal(String name, long wurmid) {
    }

    @Override
    public void sendRemoveLocal(String name) {
    }

    @Override
    public void sendDead() {
    }

    @Override
    public void sendClimb(boolean climbing) {
    }

    @Override
    public void sendReconnect(String ip, int port, String session) {
    }

    @Override
    public void sendHasMoreItems(long inventoryId, long wurmid) {
    }

    @Override
    public void sendIsEmpty(long inventoryId, long wurmid) {
    }

    @Override
    public void sendCreatureChangedLayer(long wurmid, byte newlayer) {
    }

    @Override
    public void sendCompass(Item item) {
    }

    @Override
    public void sendServerTime() {
    }

    @Override
    public void sendAttachEffect(long targetId, byte effectType, byte data0, byte data1, byte data2, byte dimension) {
    }

    @Override
    public void sendRemoveEffect(long targetId, byte effectType) {
    }

    @Override
    public void sendWieldItem(long creatureId, byte slot, String modelname, byte rarity, int colorRed, int colorGreen, int colorBlue, int secondaryColorRed, int secondaryColorGreen, int secondaryColorBlue) {
    }

    @Override
    public void sendUseItem(long creatureId, String modelname, byte rarity, int colorRed, int colorGreen, int colorBlue, int secondaryColorRed, int secondaryColorGreen, int secondaryColorBlue) {
    }

    @Override
    public void sendStopUseItem(long creatureId) {
    }

    @Override
    public void sendRepaint(long wurmid, byte r, byte g, byte b, byte alpha, byte paintType) {
    }

    @Override
    public void sendResize(long wurmid, byte xscaleMod, byte yscaleMod, byte zscaleMod) {
    }

    @Override
    public void sendNewMovingItem(long id, String name, String model, float x, float y, float z, long onBridge, float rot, byte layer, boolean onGround, boolean floating, boolean isSolid, byte material, byte rarity) {
    }

    @Override
    public void sendMoveMovingItem(long id, float x, float y, int rot) {
    }

    @Override
    public void sendMoveMovingItemAndSetZ(long id, float x, float y, float z, int rot) {
    }

    @Override
    public void sendMovingItemChangedLayer(long wurmid, byte newlayer) {
    }

    @Override
    public void sendDeleteMovingItem(long id) {
    }

    @Override
    public void sendShutDown(String reason, boolean requested) {
    }

    @Override
    public void attachCreature(long source, long target, float offx, float offy, float offz, int seatId) {
    }

    @Override
    public void setVehicleController(long playerId, long targetId, float offx, float offy, float offz, float maxDepth, float maxHeight, float maxHeightDiff, float vehicleRotation, int seatId) {
    }

    @Override
    public void sendAnimation(long creatureId, String animationName, boolean looping, boolean freezeAtFinish) {
    }

    @Override
    public void sendAnimation(long creatureId, String animationName, boolean looping, boolean freezeAtFinish, long targetId) {
    }

    @Override
    public void sendCombatOptions(byte[] options, short tenthsOfSeconds) {
    }

    @Override
    public void sendCombatStatus(float distanceToTarget, float footing, byte stance) {
    }

    @Override
    public void sendCombatNormalMessage(String message) {
    }

    @Override
    public void sendCombatAlertMessage(String message) {
    }

    @Override
    public void sendCombatSafeMessage(String message) {
    }

    @Override
    public void sendCombatServerMessage(String message, byte r, byte g, byte b) {
    }

    @Override
    public void sendStunned(boolean stunned) {
    }

    @Override
    public void sendSpecialMove(short move, String movename) {
    }

    @Override
    public void sendTarget(long id) {
    }

    @Override
    public void sendToggleShield(boolean on) {
    }

    @Override
    public void sendFightStyle(byte style) {
    }

    @Override
    public void setCreatureDamage(long wurmid, float damagePercent) {
    }

    @Override
    public void sendWindImpact(byte windimpact) {
    }

    @Override
    public void sendRotate(long itemId, float rotation) {
    }

    @Override
    public void sendWeather() {
    }

    @Override
    public void sendTileDoor(short tilex, short tiley, boolean openHole) throws IOException {
    }

    @Override
    public void sendAddPa(String name, long wurmid) {
    }

    @Override
    public void sendRemovePa(String name) {
    }

    @Override
    public void sendAddSpellEffect(long id, String name, byte type, byte effectType, byte influence, int duration, float power) {
    }

    @Override
    public void sendAck(float xpos, float ypos) {
    }

    @Override
    public void sendAddTeam(String name, long wurmid) {
    }

    @Override
    public void sendDamageState(long wurmid, byte damage) {
    }

    @Override
    public void sendRemoveTeam(String name) {
    }

    @Override
    public void sendAddAreaSpellEffect(int tilex, int tiley, int layer, byte type, int floorLevel, int heightOffset, boolean loop) {
    }

    @Override
    public void sendRemoveAreaSpellEffect(int tilex, int tiley, int layer) {
    }

    @Override
    public void sendMissionState(long wurmId, String name, String description, String creator, float state, long start, long endDate, long expires, boolean restartable, byte difficulty, String rewards) {
    }

    @Override
    public void sendRemoveMissionState(long wurmId) {
    }

    @Override
    public void sendProjectile(long id, byte type, String modelName, String name, byte material, float startX, float startY, float startH, float rot, byte layer, float endX, float endY, float endH, long sourceId, long targetId, float projectedSecondsInAir, float actualSecondsInAir) {
    }

    @Override
    public void sendBridgeId(long creatureId, long bridgeId) {
    }

    @Override
    public void sendTargetStatus(long targetId, byte kingdom, float conquerLevel) {
    }
}

