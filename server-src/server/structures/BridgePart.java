/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.structures;

import com.wurmonline.math.Vector3f;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.highways.HighwayPos;
import com.wurmonline.server.highways.MethodsHighways;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.structures.Blocker;
import com.wurmonline.server.structures.DbBridgePart;
import com.wurmonline.server.structures.IFloor;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.StructureSupport;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.BridgeConstants;
import com.wurmonline.shared.constants.SoundNames;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BridgePart
implements MiscConstants,
TimeConstants,
Blocker,
IFloor,
SoundNames,
Permissions.IAllow {
    private static final Logger logger = Logger.getLogger(Wall.class.getName());
    private long structureId = -10L;
    private int number = -10;
    float originalQL;
    float currentQL;
    float damage;
    private int tilex;
    private int tiley;
    private int realHeight;
    long lastUsed;
    private BridgeConstants.BridgeType type;
    private BridgeConstants.BridgeMaterial material;
    private BridgeConstants.BridgeState bridgePartState;
    protected byte dbState = (byte)-1;
    private byte dir = 0;
    private byte slope = 0;
    private int northExit = -1;
    private int eastExit = -1;
    private int southExit = -1;
    private int westExit = -1;
    byte roadType = 0;
    int layer = 0;
    private int materialCount = -1;
    private static final Set<DbBridgePart> bridgeParts = new HashSet<DbBridgePart>();
    private static final String GETALLBRIDGEPARTS = "SELECT * FROM BRIDGEPARTS";
    private static final Vector3f normal = new Vector3f(0.0f, 0.0f, 1.0f);
    private static Rectangle2D verticalBlocker;
    Permissions permissions = new Permissions();
    private Vector3f centerPoint;

    public BridgePart(int id, BridgeConstants.BridgeType floorType, int aTileX, int aTileY, byte aDbState, int aHeightOffset, float ql, long structure, BridgeConstants.BridgeMaterial floorMaterial, float origQl, float dam, int materialcount, long lastmaint, byte aDir, byte aSlope, int aNorthExit, int aEastExit, int aSouthExit, int aWestExit, byte roadType, int layer) {
        this.setNumber(id);
        this.type = floorType;
        this.tilex = aTileX;
        this.tiley = aTileY;
        this.dbState = aDbState;
        this.bridgePartState = BridgeConstants.BridgeState.fromByte(this.dbState);
        this.realHeight = aHeightOffset;
        this.currentQL = ql;
        this.originalQL = origQl;
        this.damage = dam;
        this.structureId = structure;
        this.material = floorMaterial;
        this.materialCount = materialcount;
        this.lastUsed = lastmaint;
        this.dir = aDir;
        this.slope = aSlope;
        this.northExit = aNorthExit;
        this.eastExit = aEastExit;
        this.southExit = aSouthExit;
        this.westExit = aWestExit;
        this.roadType = roadType;
        this.layer = layer;
    }

    public BridgePart(BridgeConstants.BridgeType floorType, int aTileX, int aTileY, int height, float ql, long structure, BridgeConstants.BridgeMaterial floorMaterial, byte aDir, byte aSlope, int aNorthExit, int aEastExit, int aSouthExit, int aWestExit, byte roadType, int layer) {
        this.type = floorType;
        this.tilex = aTileX;
        this.tiley = aTileY;
        this.bridgePartState = BridgeConstants.BridgeState.PLANNED;
        this.dbState = this.bridgePartState.getCode();
        this.damage = 0.0f;
        this.realHeight = height;
        this.currentQL = ql;
        this.originalQL = ql;
        this.structureId = structure;
        this.material = floorMaterial;
        this.materialCount = 0;
        this.dir = aDir;
        this.slope = aSlope;
        this.northExit = aNorthExit;
        this.eastExit = aEastExit;
        this.southExit = aSouthExit;
        this.westExit = aWestExit;
        this.roadType = roadType;
        this.layer = layer;
        try {
            this.save();
        }
        catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    @Override
    public boolean isFloor() {
        return true;
    }

    @Override
    public boolean isRoof() {
        return false;
    }

    @Override
    public final boolean isStair() {
        return false;
    }

    @Override
    public final Vector3f getNormal() {
        return normal;
    }

    private final Vector3f calculateCenterPoint() {
        return new Vector3f(this.tilex * 4 + 2, this.tiley * 4 + 2, (float)(this.getRealHeight() + this.slope) / 10.0f);
    }

    private final Rectangle2D getVerticalBlocker() {
        if (this.slope == 0) {
            return null;
        }
        if (verticalBlocker == null) {
            if (this.dir == 0 || this.dir == 4) {
                verticalBlocker = this.slope < 0 ? new Rectangle2D.Float(this.tilex * 4, this.tiley * 4, 4.0f, Math.abs((float)this.slope / 10.0f)) : new Rectangle2D.Float(this.tilex * 4, (this.tiley + 1) * 4, 4.0f, Math.abs((float)this.slope / 10.0f));
            }
            if (this.dir == 6 || this.dir == 2) {
                verticalBlocker = this.slope < 0 ? new Rectangle2D.Float(this.tilex * 4, this.tiley * 4, 4.0f, Math.abs((float)this.slope / 10.0f)) : new Rectangle2D.Float((this.tilex + 1) * 4, this.tiley * 4, 4.0f, Math.abs((float)this.slope / 10.0f));
            }
        }
        return verticalBlocker;
    }

    @Override
    public final Vector3f getCenterPoint() {
        if (this.centerPoint == null) {
            this.centerPoint = this.calculateCenterPoint();
        }
        return this.centerPoint;
    }

    @Override
    public int getTileX() {
        return this.tilex;
    }

    @Override
    public int getTileY() {
        return this.tiley;
    }

    @Override
    public final float getPositionX() {
        return this.tilex * 4;
    }

    @Override
    public final float getPositionY() {
        return this.tiley * 4;
    }

    public int getHeightOffset() {
        return this.realHeight;
    }

    public int getHeight() {
        if (this.isOnSurface()) {
            short ht = Tiles.decodeHeight(Server.surfaceMesh.getTile(this.tilex, this.tiley));
            return this.realHeight - ht;
        }
        short ht = Tiles.decodeHeight(Server.caveMesh.getTile(this.tilex, this.tiley));
        return this.realHeight - ht;
    }

    public int getRealHeight() {
        return this.realHeight;
    }

    public byte getDir() {
        return this.dir;
    }

    public byte getSlope() {
        return this.slope;
    }

    public boolean hasHouseNorthExit() {
        return this.northExit > 0;
    }

    public boolean hasHouseEastExit() {
        return this.eastExit > 0;
    }

    public boolean hasHouseSouthExit() {
        return this.southExit > 0;
    }

    public boolean hasHouseWestExit() {
        return this.westExit > 0;
    }

    public boolean hasHouseExit() {
        return this.hasHouseNorthExit() || this.hasHouseEastExit() || this.hasHouseSouthExit() || this.hasHouseWestExit();
    }

    public boolean hasNorthExit() {
        return this.northExit > -1;
    }

    public boolean hasEastExit() {
        return this.eastExit > -1;
    }

    public boolean hasSouthExit() {
        return this.southExit > -1;
    }

    public boolean hasWestExit() {
        return this.westExit > -1;
    }

    public boolean hasAnExit() {
        return this.hasNorthExit() || this.hasEastExit() || this.hasSouthExit() || this.hasWestExit();
    }

    @Override
    public boolean isFinished() {
        return this.bridgePartState == BridgeConstants.BridgeState.COMPLETED;
    }

    @Override
    public final boolean isMetal() {
        return false;
    }

    @Override
    public final boolean isWood() {
        return this.material == BridgeConstants.BridgeMaterial.WOOD;
    }

    @Override
    public final boolean isStone() {
        return this.material == BridgeConstants.BridgeMaterial.BRICK;
    }

    @Override
    public final boolean isSlate() {
        return false;
    }

    @Override
    public final boolean isThatch() {
        return false;
    }

    @Override
    public final boolean isMarble() {
        return this.material == BridgeConstants.BridgeMaterial.MARBLE;
    }

    @Override
    public final boolean isSandstone() {
        return this.material == BridgeConstants.BridgeMaterial.SANDSTONE;
    }

    public BridgeConstants.BridgeType getType() {
        return this.type;
    }

    public BridgeConstants.BridgeMaterial getMaterial() {
        return this.material;
    }

    public int minRequiredSkill() {
        switch (this.material) {
            case ROPE: {
                return 10;
            }
            case WOOD: {
                return 10;
            }
            case BRICK: {
                return 30;
            }
            case MARBLE: 
            case SLATE: 
            case ROUNDED_STONE: 
            case POTTERY: 
            case SANDSTONE: 
            case RENDERED: {
                return 40;
            }
        }
        return 99;
    }

    public abstract void save() throws IOException;

    @Override
    public long getId() {
        return Tiles.getBridgePartId(this.tilex, this.tiley, this.realHeight, (byte)this.layer, (byte)0);
    }

    public static int getHeightOffsetFromWurmId(long wurmId) {
        return Tiles.decodeHeightOffset(wurmId);
    }

    public BridgeConstants.BridgeState getBridgePartState() {
        return this.bridgePartState;
    }

    public String getFloorStageAsString() {
        return this.bridgePartState.getDescription();
    }

    public long getLastUsed() {
        return this.lastUsed;
    }

    @Override
    public abstract void setLastUsed(long var1);

    int getNumber() {
        return this.number;
    }

    void setNumber(int aNumber) {
        this.number = aNumber;
    }

    public float getOriginalQL() {
        return this.originalQL;
    }

    public float getCurrentQL() {
        return this.currentQL;
    }

    public byte getState() {
        return this.dbState;
    }

    protected abstract void setState(byte var1);

    public void incBridgePartStage() {
        byte currentStage = this.bridgePartState.getCode();
        byte nextStage = (byte)(currentStage + 1);
        this.bridgePartState = BridgeConstants.BridgeState.fromByte(nextStage);
        this.setState(nextStage);
        this.setMaterialCount(0);
    }

    public void setBridgePartState(BridgeConstants.BridgeState aBridgeState) {
        if (aBridgeState.isBeingBuilt() && this.bridgePartState == BridgeConstants.BridgeState.PLANNED) {
            this.setDamage(0.0f);
        }
        this.bridgePartState = aBridgeState;
        switch (this.bridgePartState) {
            case COMPLETED: {
                this.setState(BridgeConstants.BridgeState.COMPLETED.getCode());
                break;
            }
            case PLANNED: {
                this.setState(BridgeConstants.BridgeState.PLANNED.getCode());
                break;
            }
            default: {
                this.setState(this.bridgePartState.getCode());
            }
        }
    }

    public int getMaterialCount() {
        return this.materialCount;
    }

    public int getNorthExit() {
        return this.northExit;
    }

    public int getEastExit() {
        return this.eastExit;
    }

    public int getSouthExit() {
        return this.southExit;
    }

    public int getWestExit() {
        return this.westExit;
    }

    public int getNorthExitFloorLevel() {
        if (this.northExit == -1) {
            return -1;
        }
        return this.northExit / 30;
    }

    public int getEastExitFloorLevel() {
        if (this.eastExit == -1) {
            return -1;
        }
        return this.eastExit / 30;
    }

    public int getSouthExitFloorLevel() {
        if (this.southExit == -1) {
            return -1;
        }
        return this.southExit / 30;
    }

    public int getWestExitFloorLevel() {
        if (this.westExit == -1) {
            return -1;
        }
        return this.westExit / 30;
    }

    public void setMaterialCount(int count) {
        this.materialCount = count;
    }

    public byte getRoadType() {
        return this.roadType;
    }

    public byte getLayer() {
        return (byte)this.layer;
    }

    public abstract void saveRoadType(byte var1);

    public abstract void delete();

    public String getFullName() {
        if (this.bridgePartState == BridgeConstants.BridgeState.PLANNED) {
            return "Planned " + this.getName();
        }
        if (this.bridgePartState.isBeingBuilt()) {
            return "Unfinished " + this.getName();
        }
        return this.getName();
    }

    @Override
    public boolean isHorizontal() {
        return false;
    }

    @Override
    public final Vector3f isBlocking(Creature creature, Vector3f startPos, Vector3f endPos, Vector3f aNormal, int blockType, long target, boolean followGround) {
        if (target == this.getId()) {
            return null;
        }
        if (this.isAPlan()) {
            return null;
        }
        Vector3f inter = this.getIntersectionPoint(startPos, endPos, aNormal, creature, blockType);
        return inter;
    }

    @Override
    public final boolean isDoor() {
        return false;
    }

    @Override
    public final boolean isTile() {
        return false;
    }

    @Override
    public final boolean canBeOpenedBy(Creature creature, boolean wentThroughDoor) {
        return true;
    }

    @Override
    public final float getBlockPercent(Creature creature) {
        return Math.min(100, Math.max(0, this.bridgePartState.getCode() * 14));
    }

    @Override
    public final boolean isWithinFloorLevels(int maxFloorLevel, int minFloorLevel) {
        int maxHt = (maxFloorLevel + 1) * 30;
        int minHt = minFloorLevel * 30;
        int ht = this.getRealHeight();
        if (this.getType().isSupportType() && ht > maxHt) {
            return true;
        }
        return ht > minHt && ht < maxHt;
    }

    @Override
    public float getFloorZ() {
        return 0.0f;
    }

    @Override
    public float getMinZ() {
        return (float)this.getRealHeight() / 10.0f;
    }

    @Override
    public float getMaxZ() {
        return this.getMinZ() + 0.25f + (float)this.slope / 10.0f;
    }

    @Override
    public boolean isWithinZ(float maxZ, float minZ, boolean followGround) {
        return minZ <= this.getMaxZ() && maxZ >= this.getMinZ();
    }

    public final Vector3f getFloorIntersection(Vector3f startPos, Vector3f endPos, Vector3f aNormal, Creature creature, int blockType) {
        Vector3f diff = this.getCenterPoint().subtract(startPos);
        float steps = diff.z / aNormal.z;
        Vector3f intersection = startPos.add(aNormal.mult(steps));
        Vector3f diffend = endPos.subtract(startPos);
        Vector3f interDiff = intersection.subtract(startPos);
        if (diffend.length() < interDiff.length()) {
            return null;
        }
        float u = this.getNormal().dot(this.getCenterPoint().subtract(startPos)) / this.getNormal().dot(endPos.subtract(startPos));
        if (this.isWithinFloorBounds(intersection, creature, blockType)) {
            if (u >= 0.0f && u <= 1.0f) {
                return intersection;
            }
            return null;
        }
        return null;
    }

    public final Vector3f getVerticalIntersection(Vector3f startPos, Vector3f endPos, Vector3f aNormal, Creature creature) {
        if (this.getFloorLevel() == 0 && startPos.z <= this.getMinZ()) {
            startPos.z = this.getMinZ() + 0.5f;
        }
        Vector3f diff = this.getCenterPoint().subtract(startPos);
        Vector3f diffend = endPos.subtract(startPos);
        if (this.isHorizontal()) {
            float steps = diff.y / BridgePart.normal.y;
            Vector3f intersection = startPos.add(normal.mult(steps));
            Vector3f interDiff = intersection.subtract(startPos);
            if (diffend.length() + 0.01f < interDiff.length()) {
                return null;
            }
            if (this.isWithinVerticalBounds(intersection, creature)) {
                float u = this.getNormal().dot(this.getCenterPoint().subtract(startPos)) / this.getNormal().dot(endPos.subtract(startPos));
                if (u >= 0.0f && u <= 1.0f) {
                    return intersection;
                }
                return null;
            }
        } else {
            float steps = diff.x / BridgePart.normal.x;
            Vector3f intersection = startPos.add(normal.mult(steps));
            Vector3f interDiff = intersection.subtract(startPos);
            if (diffend.length() < interDiff.length()) {
                return null;
            }
            if (this.isWithinVerticalBounds(intersection, creature)) {
                float u = this.getNormal().dot(this.getCenterPoint().subtract(startPos)) / this.getNormal().dot(endPos.subtract(startPos));
                if (u >= 0.0f && u <= 1.0f) {
                    return intersection;
                }
                return null;
            }
        }
        return null;
    }

    public final Vector3f getIntersectionPoint(Vector3f startPos, Vector3f endPos, Vector3f aNormal, Creature creature, int blockType) {
        Vector3f intersection = this.getFloorIntersection(startPos, endPos, aNormal, creature, blockType);
        if (intersection == null) {
            if (blockType == 6) {
                return null;
            }
            this.getVerticalIntersection(startPos, endPos, aNormal, creature);
        }
        return intersection;
    }

    private final boolean isWithinFloorBounds(Vector3f pointToCheck, Creature creature, int blockType) {
        if (pointToCheck.getY() >= (float)(this.tiley * 4) && pointToCheck.getY() <= (float)((this.tiley + 1) * 4) && pointToCheck.getX() >= (float)(this.tilex * 4) && pointToCheck.getX() <= (float)((this.tilex + 1) * 4)) {
            if (Servers.isThisATestServer()) {
                logger.info("WithinBounds?:" + this.getName() + " height checked:" + pointToCheck.getZ() + " against bridge real height:" + (float)this.getRealHeight() / 10.0f + " (" + this.tilex + "," + this.tiley + ")");
            }
            if (this.getType().isSupportType() && blockType == 4) {
                if (pointToCheck.getZ() <= this.getMinZ() + 0.25f) {
                    return true;
                }
            } else {
                return pointToCheck.getZ() >= this.getMinZ() && pointToCheck.getZ() <= this.getMinZ() + 0.25f;
            }
        }
        return false;
    }

    private final boolean isWithinVerticalBounds(Vector3f pointToCheck, Creature creature) {
        Rectangle2D rect = this.getVerticalBlocker();
        if (rect == null) {
            return false;
        }
        return this.dir == 0 || this.dir == 4 ? (double)pointToCheck.getY() >= rect.getY() - (double)0.1f && (double)pointToCheck.getY() <= rect.getY() + (double)0.1f && (double)pointToCheck.getX() >= rect.getX() && (double)pointToCheck.getX() <= rect.getX() + rect.getWidth() && pointToCheck.getZ() >= this.getMinZ() && (double)pointToCheck.getZ() <= (double)this.getMinZ() + rect.getHeight() : (double)pointToCheck.getX() >= rect.getX() - (double)0.1f && (double)pointToCheck.getX() <= rect.getX() + (double)0.1f && (double)pointToCheck.getY() >= rect.getY() && (double)pointToCheck.getY() <= rect.getY() + rect.getWidth() && pointToCheck.getZ() >= this.getMinZ() && (double)pointToCheck.getZ() <= (double)this.getMinZ() + rect.getHeight();
    }

    public static final void loadAllBridgeParts() throws IOException {
        logger.log(Level.INFO, "Loading all bridge parts.");
        long s = System.nanoTime();
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement(GETALLBRIDGEPARTS);
            rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("ID");
                BridgeConstants.BridgeType floorType = BridgeConstants.BridgeType.fromByte(rs.getByte("TYPE"));
                BridgeConstants.BridgeMaterial floorMaterial = BridgeConstants.BridgeMaterial.fromByte(rs.getByte("MATERIAL"));
                byte state = rs.getByte("STATE");
                int stageCount = rs.getInt("STAGECOUNT");
                int x = rs.getInt("TILEX");
                int y = rs.getInt("TILEY");
                long structureId = rs.getLong("STRUCTURE");
                int h = rs.getInt("HEIGHTOFFSET");
                float currentQL = rs.getFloat("CURRENTQL");
                float origQL = rs.getFloat("ORIGINALQL");
                float dam = rs.getFloat("DAMAGE");
                byte dir = rs.getByte("DIR");
                byte slope = rs.getByte("SLOPE");
                long last = rs.getLong("LASTMAINTAINED");
                int northExit = rs.getInt("NORTHEXIT");
                int eastExit = rs.getInt("EASTEXIT");
                int southExit = rs.getInt("SOUTHEXIT");
                int westExit = rs.getInt("WESTEXIT");
                byte roadType = rs.getByte("ROADTYPE");
                int layer = rs.getInt("LAYER");
                bridgeParts.add(new DbBridgePart(id, floorType, x, y, state, h, currentQL, structureId, floorMaterial, origQL, dam, stageCount, last, dir, slope, northExit, eastExit, southExit, westExit, roadType, layer));
            }
        }
        catch (SQLException sqx) {
            try {
                logger.log(Level.WARNING, "Failed to load bridge parts!" + sqx.getMessage(), sqx);
                throw new IOException(sqx);
            }
            catch (Throwable throwable) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                DbConnector.returnConnection(dbcon);
                long e = System.nanoTime();
                logger.log(Level.INFO, "Loaded " + bridgeParts.size() + " bridge parts. That took " + (float)(e - s) / 1000000.0f + " ms.");
                throw throwable;
            }
        }
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
        long e = System.nanoTime();
        logger.log(Level.INFO, "Loaded " + bridgeParts.size() + " bridge parts. That took " + (float)(e - s) / 1000000.0f + " ms.");
    }

    public static final Set<BridgePart> getBridgePartsFor(long structureId) {
        HashSet<BridgePart> toReturn = new HashSet<BridgePart>();
        for (BridgePart bridgePart : bridgeParts) {
            if (bridgePart.getStructureId() != structureId) continue;
            toReturn.add(bridgePart);
        }
        return toReturn;
    }

    @Override
    public long getStructureId() {
        return this.structureId;
    }

    void setStructureId(long aStructureId) {
        this.structureId = aStructureId;
    }

    @Override
    public boolean isAPlan() {
        return this.bridgePartState == BridgeConstants.BridgeState.PLANNED;
    }

    public void revertToPlan() {
        MethodsHighways.removeNearbyMarkers(this);
        this.setBridgePartState(BridgeConstants.BridgeState.PLANNED);
        this.setDamage(0.0f);
        this.setQualityLevel(1.0f);
        this.saveRoadType((byte)0);
        try {
            this.save();
        }
        catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        VolaTile volaTile = Zones.getOrCreateTile(this.getTileX(), this.getTileY(), this.layer == 0);
        volaTile.updateBridgePart(this);
    }

    @Override
    public void destroyOrRevertToPlan() {
        this.revertToPlan();
    }

    Structure getStructure() {
        Structure struct = null;
        try {
            struct = Structures.getStructure(this.getStructureId());
        }
        catch (NoSuchStructureException e) {
            logger.log(Level.WARNING, " Failed to find Structures.getStructure(" + this.getStructureId() + " for a BridgePart about to be deleted: " + e.getMessage(), e);
        }
        return struct;
    }

    public void destroy() {
        this.delete();
        bridgeParts.remove(this);
        VolaTile volaTile = Zones.getOrCreateTile(this.getTileX(), this.getTileY(), this.layer == 0);
        volaTile.removeBridgePart(this);
    }

    @Override
    public final float getDamageModifierForItem(Item item) {
        float mod;
        switch (this.material) {
            case ROPE: {
                if (item.isWeaponSlash()) {
                    mod = 0.03f;
                    break;
                }
                mod = 0.007f;
                break;
            }
            case BRICK: {
                if (item.isWeaponCrush()) {
                    mod = 0.01f;
                    break;
                }
                mod = 0.002f;
                break;
            }
            case MARBLE: 
            case SLATE: 
            case ROUNDED_STONE: 
            case POTTERY: 
            case SANDSTONE: 
            case RENDERED: {
                if (item.isWeaponCrush()) {
                    mod = 0.005f;
                    break;
                }
                mod = 0.001f;
                break;
            }
            case WOOD: {
                if (item.isWeaponAxe()) {
                    mod = 0.03f;
                    break;
                }
                mod = 0.007f;
                break;
            }
            default: {
                mod = 0.0f;
            }
        }
        return mod;
    }

    @Override
    public final boolean isOnPvPServer() {
        return Zones.isOnPvPServer(this.tilex, this.tiley);
    }

    @Override
    public final int getFloorLevel() {
        return 0;
    }

    public void buildProgress(int numSteps) {
        if (this.getBridgePartState().isBeingBuilt()) {
            this.setMaterialCount(this.getMaterialCount() + numSteps);
        } else {
            logger.log(Level.WARNING, "buildProgress method called on bridge part when bridge part was not in buildable state: " + this.getId() + " " + this.bridgePartState.toString());
        }
    }

    @Override
    public final VolaTile getTile() {
        try {
            Zone zone = Zones.getZone(this.tilex, this.tiley, this.layer == 0);
            VolaTile toReturn = zone.getTileOrNull(this.tilex, this.tiley);
            if (toReturn != null) {
                if (toReturn.isTransition()) {
                    return Zones.getZone(this.tilex, this.tiley, false).getOrCreateTile(this.tilex, this.tiley);
                }
                return toReturn;
            }
            logger.log(Level.WARNING, "Tile not in zone, this shouldn't happen " + this.tilex + ", " + this.tiley);
        }
        catch (NoSuchZoneException nsz) {
            logger.log(Level.WARNING, "This shouldn't happen " + this.tilex + ", " + this.tiley, nsz);
        }
        return null;
    }

    public final Village getVillage() {
        VolaTile t = this.getTile();
        if (t != null && t.getVillage() != null) {
            return t.getVillage();
        }
        return null;
    }

    public final float getModByMaterial() {
        switch (this.material) {
            case ROPE: {
                return 4.0f;
            }
            case BRICK: {
                return 10.0f;
            }
            case MARBLE: 
            case SLATE: 
            case ROUNDED_STONE: 
            case POTTERY: 
            case SANDSTONE: 
            case RENDERED: {
                return 12.0f;
            }
            case WOOD: {
                return 7.0f;
            }
        }
        return 1.0f;
    }

    @Override
    public final float getDamageModifier() {
        return 100.0f / Math.max(1.0f, this.currentQL * (100.0f - this.damage) / 100.0f);
    }

    public final boolean poll(long currTime, Structure struct) {
        if (struct == null) {
            return true;
        }
        if (currTime - struct.getCreationDate() <= (Servers.localServer.testServer ? 3600000L : 86400000L) * 2L) {
            return false;
        }
        if (this.isAPlan()) {
            return false;
        }
        HighwayPos highwaypos = MethodsHighways.getHighwayPos(this);
        if (highwaypos != null && MethodsHighways.onHighway(highwaypos)) {
            return false;
        }
        float mod = 1.0f;
        Village v = this.getVillage();
        if (v != null) {
            if (v.moreThanMonthLeft()) {
                return false;
            }
            if (!v.lessThanWeekLeft()) {
                mod = 10.0f;
            }
        } else if (Zones.getKingdom(this.tilex, this.tiley) == 0) {
            mod = 0.5f;
        }
        float f = currTime - this.lastUsed;
        float f2 = Servers.localServer.testServer ? 8.64E7f + 60000.0f * mod : 6.048E8f + 8.64E7f * mod;
        if (f > f2 && !this.hasNoDecay()) {
            this.setLastUsed(currTime);
            if (this.setDamage(this.damage + this.getDamageModifier() * (0.1f + this.getModByMaterial() / 1000.0f))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public final float getCurrentQualityLevel() {
        return this.currentQL * Math.max(1.0f, 100.0f - this.damage) / 100.0f;
    }

    @Override
    public final int getRepairItemTemplate() {
        switch (this.material) {
            case BRICK: {
                return 132;
            }
            case MARBLE: {
                return 786;
            }
            case SLATE: {
                return 1123;
            }
            case ROUNDED_STONE: {
                return 1122;
            }
            case POTTERY: {
                return 776;
            }
            case SANDSTONE: {
                return 1121;
            }
            case RENDERED: {
                return 132;
            }
            case ROPE: {
                return 22;
            }
            case WOOD: {
                return 22;
            }
        }
        return 22;
    }

    public String getSoundByMaterial() {
        switch (this.getMaterial()) {
            case BRICK: 
            case MARBLE: 
            case SLATE: 
            case ROUNDED_STONE: 
            case POTTERY: 
            case SANDSTONE: 
            case RENDERED: {
                return "sound.work.masonry";
            }
            case WOOD: {
                return Server.rand.nextInt(2) == 0 ? "sound.work.carpentry.mallet1" : "sound.work.carpentry.mallet2";
            }
        }
        return Server.rand.nextInt(2) == 0 ? "sound.work.carpentry.mallet1" : "sound.work.carpentry.mallet2";
    }

    @Override
    public final int getStartX() {
        return this.getTileX();
    }

    @Override
    public final int getStartY() {
        return this.getTileY();
    }

    @Override
    public final int getMinX() {
        return this.getTileX();
    }

    @Override
    public final int getMinY() {
        return this.getTileY();
    }

    @Override
    public final boolean supports() {
        return true;
    }

    public int getNumberOfExtensions() {
        int extensions = 0;
        if (this.type.isSupportType()) {
            int extensionTop;
            int htOff = this.getHeightOffset() + Math.abs(this.getSlope());
            int lowestCorner = (int)(Zones.getLowestCorner(this.getTileX(), this.getTileY(), 0) * 10.0f);
            int extensionOffset = (int)this.getMaterial().getExtensionOffset() * 10;
            for (int ht = extensionTop = htOff - extensionOffset; ht > lowestCorner; ht -= 30) {
                ++extensions;
            }
        }
        return extensions;
    }

    @Override
    public final boolean supports(StructureSupport support) {
        if (!this.supports()) {
            return false;
        }
        return false;
    }

    @Override
    public final boolean equals(StructureSupport support) {
        return support.getId() == this.getId();
    }

    @Override
    public final int getEndX() {
        return this.getStartX() + 1;
    }

    @Override
    public final int getEndY() {
        return this.getStartY() + 1;
    }

    @Override
    public boolean isSupportedByGround() {
        return true;
    }

    public String toString() {
        return "BridgePart [number=" + this.number + ", structureId=" + this.structureId + ", type=" + (Object)((Object)this.type) + "]";
    }

    @Override
    public boolean isOnSurface() {
        return this.layer == 0;
    }

    @Override
    public boolean canBeAlwaysLit() {
        return false;
    }

    @Override
    public boolean canBeAutoFilled() {
        return false;
    }

    @Override
    public boolean canBeAutoLit() {
        return false;
    }

    @Override
    public final boolean canBePeggedByPlayer() {
        return false;
    }

    @Override
    public boolean canBePlanted() {
        return false;
    }

    @Override
    public final boolean canBeSealedByPlayer() {
        return false;
    }

    @Override
    public boolean canChangeCreator() {
        return false;
    }

    @Override
    public boolean canDisableDecay() {
        return true;
    }

    @Override
    public boolean canDisableDestroy() {
        return true;
    }

    @Override
    public boolean canDisableDrag() {
        return false;
    }

    @Override
    public boolean canDisableDrop() {
        return false;
    }

    @Override
    public boolean canDisableEatAndDrink() {
        return false;
    }

    @Override
    public boolean canDisableImprove() {
        return true;
    }

    @Override
    public boolean canDisableLocking() {
        return false;
    }

    @Override
    public boolean canDisableLockpicking() {
        return false;
    }

    @Override
    public boolean canDisableMoveable() {
        return false;
    }

    @Override
    public final boolean canDisableOwnerMoveing() {
        return false;
    }

    @Override
    public final boolean canDisableOwnerTurning() {
        return false;
    }

    @Override
    public boolean canDisablePainting() {
        return false;
    }

    @Override
    public boolean canDisablePut() {
        return false;
    }

    @Override
    public boolean canDisableRepair() {
        return true;
    }

    @Override
    public boolean canDisableRuneing() {
        return false;
    }

    @Override
    public boolean canDisableSpellTarget() {
        return false;
    }

    @Override
    public boolean canDisableTake() {
        return false;
    }

    @Override
    public boolean canDisableTurning() {
        return false;
    }

    @Override
    public boolean canHaveCourier() {
        return false;
    }

    @Override
    public boolean canHaveDakrMessenger() {
        return false;
    }

    @Override
    public String getCreatorName() {
        return null;
    }

    @Override
    public float getDamage() {
        return this.damage;
    }

    @Override
    public String getName() {
        return this.material.getName() + " " + this.type.getName();
    }

    @Override
    public float getQualityLevel() {
        return this.currentQL;
    }

    @Override
    public boolean hasCourier() {
        return this.permissions.hasPermission(Permissions.Allow.HAS_COURIER.getBit());
    }

    @Override
    public boolean hasDarkMessenger() {
        return this.permissions.hasPermission(Permissions.Allow.HAS_DARK_MESSENGER.getBit());
    }

    @Override
    public boolean hasNoDecay() {
        return this.permissions.hasPermission(Permissions.Allow.DECAY_DISABLED.getBit());
    }

    @Override
    public boolean isAlwaysLit() {
        return this.permissions.hasPermission(Permissions.Allow.ALWAYS_LIT.getBit());
    }

    @Override
    public boolean isAutoFilled() {
        return this.permissions.hasPermission(Permissions.Allow.AUTO_FILL.getBit());
    }

    @Override
    public boolean isAutoLit() {
        return this.permissions.hasPermission(Permissions.Allow.AUTO_LIGHT.getBit());
    }

    @Override
    public boolean isIndestructible() {
        return this.permissions.hasPermission(Permissions.Allow.NO_BASH.getBit());
    }

    @Override
    public boolean isNoDrag() {
        return this.permissions.hasPermission(Permissions.Allow.NO_DRAG.getBit());
    }

    @Override
    public boolean isNoDrop() {
        return this.permissions.hasPermission(Permissions.Allow.NO_DROP.getBit());
    }

    @Override
    public boolean isNoEatOrDrink() {
        return this.permissions.hasPermission(Permissions.Allow.NO_EAT_OR_DRINK.getBit());
    }

    @Override
    public boolean isNoImprove() {
        return this.permissions.hasPermission(Permissions.Allow.NO_IMPROVE.getBit());
    }

    @Override
    public boolean isNoMove() {
        return this.permissions.hasPermission(Permissions.Allow.NOT_MOVEABLE.getBit());
    }

    @Override
    public boolean isNoPut() {
        return this.permissions.hasPermission(Permissions.Allow.NO_PUT.getBit());
    }

    @Override
    public boolean isNoRepair() {
        return this.permissions.hasPermission(Permissions.Allow.NO_REPAIR.getBit());
    }

    @Override
    public boolean isNoTake() {
        return this.permissions.hasPermission(Permissions.Allow.NO_TAKE.getBit());
    }

    @Override
    public boolean isNotLockable() {
        return this.permissions.hasPermission(Permissions.Allow.NOT_LOCKABLE.getBit());
    }

    @Override
    public boolean isNotLockpickable() {
        return this.permissions.hasPermission(Permissions.Allow.NOT_LOCKPICKABLE.getBit());
    }

    @Override
    public boolean isNotPaintable() {
        return this.permissions.hasPermission(Permissions.Allow.NOT_PAINTABLE.getBit());
    }

    @Override
    public boolean isNotRuneable() {
        return true;
    }

    @Override
    public boolean isNotSpellTarget() {
        return this.permissions.hasPermission(Permissions.Allow.NO_SPELLS.getBit());
    }

    @Override
    public boolean isNotTurnable() {
        return this.permissions.hasPermission(Permissions.Allow.NOT_TURNABLE.getBit());
    }

    @Override
    public boolean isOwnerMoveable() {
        return this.permissions.hasPermission(Permissions.Allow.OWNER_MOVEABLE.getBit());
    }

    @Override
    public boolean isOwnerTurnable() {
        return this.permissions.hasPermission(Permissions.Allow.OWNER_TURNABLE.getBit());
    }

    @Override
    public boolean isPlanted() {
        return this.permissions.hasPermission(Permissions.Allow.PLANTED.getBit());
    }

    @Override
    public final boolean isSealedByPlayer() {
        return this.permissions.hasPermission(Permissions.Allow.SEALED_BY_PLAYER.getBit());
    }

    @Override
    public void setCreator(String aNewCreator) {
    }

    @Override
    public abstract boolean setDamage(float var1);

    @Override
    public void setHasCourier(boolean aCourier) {
        this.permissions.setPermissionBit(Permissions.Allow.HAS_COURIER.getBit(), aCourier);
    }

    @Override
    public void setHasDarkMessenger(boolean aDarkmessenger) {
        this.permissions.setPermissionBit(Permissions.Allow.HAS_DARK_MESSENGER.getBit(), aDarkmessenger);
    }

    @Override
    public void setHasNoDecay(boolean aNoDecay) {
        this.permissions.setPermissionBit(Permissions.Allow.DECAY_DISABLED.getBit(), aNoDecay);
    }

    @Override
    public void setIsAlwaysLit(boolean aAlwaysLit) {
        this.permissions.setPermissionBit(Permissions.Allow.ALWAYS_LIT.getBit(), aAlwaysLit);
    }

    @Override
    public void setIsAutoFilled(boolean aAutoFill) {
        this.permissions.setPermissionBit(Permissions.Allow.AUTO_FILL.getBit(), aAutoFill);
    }

    @Override
    public void setIsAutoLit(boolean aAutoLight) {
        this.permissions.setPermissionBit(Permissions.Allow.AUTO_LIGHT.getBit(), aAutoLight);
    }

    @Override
    public void setIsIndestructible(boolean aNoDestroy) {
        this.permissions.setPermissionBit(Permissions.Allow.NO_BASH.getBit(), aNoDestroy);
    }

    @Override
    public void setIsNoDrag(boolean aNoDrag) {
        this.permissions.setPermissionBit(Permissions.Allow.NO_DRAG.getBit(), aNoDrag);
    }

    @Override
    public void setIsNoDrop(boolean aNoDrop) {
        this.permissions.setPermissionBit(Permissions.Allow.NO_DROP.getBit(), aNoDrop);
    }

    @Override
    public void setIsNoEatOrDrink(boolean aNoEatOrDrink) {
        this.permissions.setPermissionBit(Permissions.Allow.NO_EAT_OR_DRINK.getBit(), aNoEatOrDrink);
    }

    @Override
    public void setIsNoImprove(boolean aNoImprove) {
        this.permissions.setPermissionBit(Permissions.Allow.NO_IMPROVE.getBit(), aNoImprove);
    }

    @Override
    public void setIsNoMove(boolean aNoMove) {
        this.permissions.setPermissionBit(Permissions.Allow.NOT_MOVEABLE.getBit(), aNoMove);
    }

    @Override
    public void setIsNoPut(boolean aNoPut) {
        this.permissions.setPermissionBit(Permissions.Allow.NO_PUT.getBit(), aNoPut);
    }

    @Override
    public void setIsNoRepair(boolean aNoRepair) {
        this.permissions.setPermissionBit(Permissions.Allow.NO_REPAIR.getBit(), aNoRepair);
    }

    @Override
    public void setIsNoTake(boolean aNoTake) {
        this.permissions.setPermissionBit(Permissions.Allow.NO_TAKE.getBit(), aNoTake);
    }

    @Override
    public void setIsNotLockable(boolean aNoLock) {
        this.permissions.setPermissionBit(Permissions.Allow.NOT_LOCKABLE.getBit(), aNoLock);
    }

    @Override
    public void setIsNotLockpickable(boolean aNoLockpick) {
        this.permissions.setPermissionBit(Permissions.Allow.NOT_LOCKPICKABLE.getBit(), aNoLockpick);
    }

    @Override
    public void setIsNotPaintable(boolean aNoPaint) {
        this.permissions.setPermissionBit(Permissions.Allow.NOT_PAINTABLE.getBit(), aNoPaint);
    }

    @Override
    public void setIsNotRuneable(boolean aNoRune) {
        this.permissions.setPermissionBit(Permissions.Allow.NOT_RUNEABLE.getBit(), aNoRune);
    }

    @Override
    public void setIsNotSpellTarget(boolean aNoSpells) {
        this.permissions.setPermissionBit(Permissions.Allow.NO_SPELLS.getBit(), aNoSpells);
    }

    @Override
    public void setIsNotTurnable(boolean aNoTurn) {
        this.permissions.setPermissionBit(Permissions.Allow.NOT_TURNABLE.getBit(), aNoTurn);
    }

    @Override
    public void setIsOwnerMoveable(boolean aOwnerMove) {
        this.permissions.setPermissionBit(Permissions.Allow.OWNER_MOVEABLE.getBit(), aOwnerMove);
    }

    @Override
    public void setIsOwnerTurnable(boolean aOwnerTurn) {
        this.permissions.setPermissionBit(Permissions.Allow.OWNER_TURNABLE.getBit(), aOwnerTurn);
    }

    @Override
    public void setIsPlanted(boolean aPlant) {
        this.permissions.setPermissionBit(Permissions.Allow.PLANTED.getBit(), aPlant);
    }

    @Override
    public void setIsSealedByPlayer(boolean aSealed) {
        this.permissions.setPermissionBit(Permissions.Allow.SEALED_BY_PLAYER.getBit(), aSealed);
    }

    @Override
    public abstract boolean setQualityLevel(float var1);

    @Override
    public void setOriginalQualityLevel(float newQL) {
    }

    @Override
    public abstract void savePermissions();
}

