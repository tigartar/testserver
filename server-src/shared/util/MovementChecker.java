/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.shared.util;

import com.wurmonline.mesh.Tiles;
import java.util.logging.Logger;

public strictfp abstract class MovementChecker {
    protected static final Logger logger = Logger.getLogger(MovementChecker.class.getName());
    int blocks = 0;
    public static final float DEGS_TO_RADS = (float)Math.PI / 180;
    public static final int BIT_FORWARD = 1;
    public static final int BIT_BACK = 2;
    public static final int BIT_LEFT = 4;
    public static final int BIT_RIGHT = 8;
    private static final float WALK_SPEED = 0.08f;
    public static final float FLOATING_HEIGHT = -1.45f;
    public boolean serverWestAvailable = true;
    public boolean serverNorthAvailable = true;
    public boolean serverEastAvailable = true;
    public boolean serverSouthAvailable = true;
    private static final float CLIMB_SPEED_MODIFIER = 0.25f;
    private float speedMod = 1.0f;
    private boolean climbing = false;
    private long bridgeId = -10L;
    private int bridgeCounter = 0;
    private float x;
    private float y;
    private float z;
    private float xRot;
    public float xOld;
    public float yOld;
    private float zOld;
    private float xa;
    private float ya;
    private float za;
    private float groundOffset;
    private int targetGroundOffset;
    private byte bitmask;
    public boolean onGround = false;
    public boolean inWater = false;
    private int layer = 0;
    private boolean abort = false;
    public boolean ignoreErrors = false;
    public boolean started = false;
    public float diffWindX = 0.0f;
    public float diffWindY = 0.0f;
    private float windRotation = 0.0f;
    private float windStrength = 0.0f;
    private float windImpact = 0.0f;
    private float mountSpeed = 0.1f;
    public boolean commandingBoat = false;
    private float vehicleRotation = 0.0f;
    private float currx;
    private float curry;
    private boolean first = true;
    public static final float MINHEIGHTC = -3000.0f;
    public static final float MAXHEIGHTC = 3000.0f;
    private static final float fallMod = 0.04f;
    private static final float deltaH = 1.0f;
    private static final float moveMod = 0.4f;
    private boolean movingVehicle = false;
    private float leftTurnMod = 1.0f;
    private float rightTurnMod = 1.0f;
    public float offZ = 0.0f;
    private boolean flying = false;
    protected boolean wasOnStair = false;
    private int counter = 0;
    private boolean acceptedError = false;
    private boolean isFalling = false;
    private boolean onFloorOverridden = false;
    float mhdlog = 0.0f;

    protected boolean isPressed(int key) {
        return (this.bitmask & key) != 0;
    }

    public final void setFlying(boolean fly) {
        this.flying = fly;
    }

    public final void setIsFalling(boolean falling) {
        this.isFalling = falling;
    }

    public boolean isFalling() {
        return this.isFalling;
    }

    public final boolean isFlying() {
        return this.flying;
    }

    public boolean isAborted() {
        return this.abort;
    }

    protected void setAbort(boolean abort) {
        this.abort = abort;
    }

    public boolean isKeyPressed() {
        return this.isPressed(1) || this.isPressed(2) || this.isPressed(4) || this.isPressed(8);
    }

    public void resetBm() {
        this.bitmask = 0;
    }

    public final void movestep(float maxHeightDiff, float xTarget, float zTarget, float yTarget, float maxDepth, float maxHeight, float rotation, byte _bitmask, int estimatedLayer) {
        float dHeight;
        float tileSpeedMod;
        float dHeight2;
        int mod;
        boolean isCommanding;
        this.abort = false;
        this.currx = xTarget;
        this.curry = yTarget;
        this.movingVehicle = false;
        this.mhdlog = maxHeightDiff;
        if (estimatedLayer != this.layer) {
            this.handleWrongLayer(estimatedLayer, this.layer);
        }
        if (isCommanding = this.isCommanding(maxDepth, maxHeight)) {
            this.maybePrintDebugInfo(1);
        }
        if (xTarget != this.x || yTarget != this.y) {
            float expxDist = this.x - this.xOld;
            float expyDist = this.y - this.yOld;
            float expectedDistance = (float)StrictMath.sqrt(expxDist * expxDist + expyDist * expyDist);
            float realxDist = xTarget - this.xOld;
            float realyDist = yTarget - this.yOld;
            float realDistance = (float)StrictMath.sqrt(realxDist * realxDist + realyDist * realyDist);
            if (this.bridgeCounter <= 0 && !this.movedOnStair()) {
                if (!this.isFalling && realDistance > expectedDistance) {
                    if (this.acceptedError) {
                        this.handleMoveTooFar(realDistance, expectedDistance);
                    } else {
                        this.acceptedError = true;
                    }
                } else if (this.acceptedError) {
                    this.handleMoveTooShort(realDistance, expectedDistance);
                } else {
                    this.acceptedError = true;
                }
            }
        } else if (zTarget != this.z && Math.abs(zTarget - this.z) > 0.25f) {
            if (!this.isFalling && this.bridgeCounter <= 0 && this.bridgeId == -10L && !this.movedOnStair()) {
                if (this.acceptedError) {
                    this.handleZError(zTarget, this.z);
                } else {
                    this.acceptedError = true;
                }
            }
        } else {
            this.acceptedError = false;
        }
        this.bridgeCounter = Math.max(0, this.bridgeCounter - 1);
        if (this.abort) {
            return;
        }
        int currentTileX = (int)(xTarget / 4.0f);
        int currentTileY = (int)(yTarget / 4.0f);
        this.x = xTarget;
        this.y = yTarget;
        this.z = zTarget;
        this.layer = estimatedLayer;
        this.xRot = rotation;
        this.bitmask = _bitmask;
        float speedModifier = this.speedMod;
        float heightTarget = this.getHeight(this.x, this.y, -3000.0f);
        boolean bl = this.inWater = this.z + this.za <= -1.0f;
        if (isCommanding) {
            this.inWater = false;
        }
        int dirs = 0;
        float xPosMod = 0.0f;
        float yPosMod = 0.0f;
        if (!(this.onGround || this.inWater || this.commandingBoat || this.isOnFloor())) {
            speedModifier *= 0.1f;
        }
        speedModifier *= 1.5f;
        if (this.climbing) {
            speedModifier *= 0.25f;
        }
        if (this.isPressed(1)) {
            ++dirs;
            if (isCommanding) {
                if (speedModifier > 0.0f) {
                    xPosMod += (float)StrictMath.sin(this.vehicleRotation * ((float)Math.PI / 180)) * this.mountSpeed;
                    if (!this.serverWestAvailable && xPosMod < 0.0f) {
                        xPosMod = 0.0f;
                    } else if (!this.serverEastAvailable && xPosMod > 0.0f) {
                        xPosMod = 0.0f;
                    }
                    yPosMod -= (float)StrictMath.cos(this.vehicleRotation * ((float)Math.PI / 180)) * this.mountSpeed;
                    if (!this.serverNorthAvailable && yPosMod < 0.0f) {
                        yPosMod = 0.0f;
                    } else if (!this.serverSouthAvailable && yPosMod > 0.0f) {
                        yPosMod = 0.0f;
                    }
                    this.movingVehicle = true;
                }
            } else {
                xPosMod += (float)StrictMath.sin(rotation * ((float)Math.PI / 180)) * 0.08f * speedModifier;
                yPosMod -= (float)StrictMath.cos(rotation * ((float)Math.PI / 180)) * 0.08f * speedModifier;
            }
        }
        if (this.isPressed(2)) {
            ++dirs;
            if (isCommanding) {
                if (speedModifier > 0.0f) {
                    xPosMod -= (float)StrictMath.sin(this.vehicleRotation * ((float)Math.PI / 180)) * this.mountSpeed * 0.3f;
                    if (!this.serverWestAvailable && xPosMod < 0.0f) {
                        xPosMod = 0.0f;
                    } else if (!this.serverEastAvailable && xPosMod > 0.0f) {
                        xPosMod = 0.0f;
                    }
                    yPosMod += (float)StrictMath.cos(this.vehicleRotation * ((float)Math.PI / 180)) * this.mountSpeed * 0.3f;
                    if (!this.serverNorthAvailable && yPosMod < 0.0f) {
                        yPosMod = 0.0f;
                    } else if (!this.serverSouthAvailable && yPosMod > 0.0f) {
                        yPosMod = 0.0f;
                    }
                    this.movingVehicle = true;
                }
            } else {
                xPosMod -= (float)StrictMath.sin(rotation * ((float)Math.PI / 180)) * 0.08f * speedModifier;
                yPosMod += (float)StrictMath.cos(rotation * ((float)Math.PI / 180)) * 0.08f * speedModifier;
            }
        }
        if (this.isPressed(4)) {
            ++dirs;
            if (isCommanding) {
                if (!this.commandingBoat || this.windImpact != 0.0f) {
                    if (!this.commandingBoat) {
                        if (this.movingVehicle) {
                            this.leftTurnMod += 1.0f;
                            mod = 3;
                            if (this.leftTurnMod > 20.0f) {
                                mod = 2;
                            }
                            if (this.leftTurnMod > 40.0f) {
                                mod = 1;
                            }
                            this.vehicleRotation = MovementChecker.normalizeAngle(this.vehicleRotation - (float)mod);
                        } else if (speedModifier > 0.0f) {
                            this.leftTurnMod += 1.0f;
                            mod = 3;
                            if (this.leftTurnMod > 20.0f) {
                                mod = 2;
                            }
                            if (this.leftTurnMod > 40.0f) {
                                mod = 1;
                            }
                            this.vehicleRotation = MovementChecker.normalizeAngle(this.vehicleRotation - (float)mod);
                            xPosMod += (float)StrictMath.sin(this.vehicleRotation * ((float)Math.PI / 180)) * this.mountSpeed * 0.3f;
                            if (!this.serverWestAvailable && xPosMod < 0.0f) {
                                xPosMod = 0.0f;
                            } else if (!this.serverEastAvailable && xPosMod > 0.0f) {
                                xPosMod = 0.0f;
                            }
                            yPosMod -= (float)StrictMath.cos(this.vehicleRotation * ((float)Math.PI / 180)) * this.mountSpeed * 0.3f;
                            if (!this.serverNorthAvailable && yPosMod < 0.0f) {
                                yPosMod = 0.0f;
                            } else if (!this.serverSouthAvailable && yPosMod > 0.0f) {
                                yPosMod = 0.0f;
                            }
                            this.movingVehicle = true;
                        }
                    } else {
                        this.vehicleRotation = MovementChecker.normalizeAngle(this.vehicleRotation - 1.0f);
                    }
                }
            } else {
                xPosMod -= (float)StrictMath.cos(rotation * ((float)Math.PI / 180)) * 0.08f * speedModifier;
                yPosMod -= (float)StrictMath.sin(rotation * ((float)Math.PI / 180)) * 0.08f * speedModifier;
            }
        } else {
            this.leftTurnMod = 0.0f;
        }
        if (this.isPressed(8)) {
            ++dirs;
            if (isCommanding) {
                if (!this.commandingBoat || this.windImpact != 0.0f) {
                    if (!this.commandingBoat) {
                        if (this.movingVehicle) {
                            this.rightTurnMod += 1.0f;
                            mod = 3;
                            if (this.rightTurnMod > 20.0f) {
                                mod = 2;
                            }
                            if (this.rightTurnMod > 40.0f) {
                                mod = 1;
                            }
                            this.vehicleRotation = MovementChecker.normalizeAngle(this.vehicleRotation + (float)mod);
                        } else if (speedModifier > 0.0f) {
                            this.rightTurnMod += 1.0f;
                            mod = 3;
                            if (this.rightTurnMod > 20.0f) {
                                mod = 2;
                            }
                            if (this.rightTurnMod > 40.0f) {
                                mod = 1;
                            }
                            this.vehicleRotation = MovementChecker.normalizeAngle(this.vehicleRotation + (float)mod);
                            xPosMod += (float)StrictMath.sin(this.vehicleRotation * ((float)Math.PI / 180)) * this.mountSpeed * 0.3f;
                            if (!this.serverWestAvailable && xPosMod < 0.0f) {
                                xPosMod = 0.0f;
                            } else if (!this.serverEastAvailable && xPosMod > 0.0f) {
                                xPosMod = 0.0f;
                            }
                            yPosMod -= (float)StrictMath.cos(this.vehicleRotation * ((float)Math.PI / 180)) * this.mountSpeed * 0.3f;
                            if (!this.serverNorthAvailable && yPosMod < 0.0f) {
                                yPosMod = 0.0f;
                            } else if (!this.serverSouthAvailable && yPosMod > 0.0f) {
                                yPosMod = 0.0f;
                            }
                            this.movingVehicle = true;
                        }
                    } else {
                        this.vehicleRotation = MovementChecker.normalizeAngle(this.vehicleRotation + 1.0f);
                    }
                }
            } else {
                xPosMod += (float)StrictMath.cos(rotation * ((float)Math.PI / 180)) * 0.08f * speedModifier;
                yPosMod += (float)StrictMath.sin(rotation * ((float)Math.PI / 180)) * 0.08f * speedModifier;
            }
        } else {
            this.rightTurnMod = 0.0f;
        }
        if (dirs > 0) {
            this.xa = (float)((double)this.xa + (double)xPosMod / StrictMath.sqrt(dirs));
            this.ya = (float)((double)this.ya + (double)yPosMod / StrictMath.sqrt(dirs));
        }
        if (this.windImpact != 0.0f && speedModifier > 0.0f) {
            float strength = MovementChecker.getWindPower(this.windRotation - 180.0f, this.vehicleRotation);
            float driftx = this.diffWindX * this.windImpact * 0.05f;
            float drifty = this.diffWindY * this.windImpact * 0.05f;
            if (!this.serverWestAvailable && driftx < 0.0f) {
                driftx = 0.0f;
            }
            if (!this.serverEastAvailable && driftx > 0.0f) {
                driftx = 0.0f;
            }
            if (!this.serverSouthAvailable && drifty > 0.0f) {
                drifty = 0.0f;
            }
            if (!this.serverNorthAvailable && drifty < 0.0f) {
                drifty = 0.0f;
            }
            this.xa += driftx;
            this.ya += drifty;
            float windx = (float)StrictMath.sin(this.vehicleRotation * ((float)Math.PI / 180)) * Math.abs(this.windStrength) * this.windImpact * strength;
            float windy = (float)StrictMath.cos(this.vehicleRotation * ((float)Math.PI / 180)) * Math.abs(this.windStrength) * this.windImpact * strength;
            if (!this.serverWestAvailable && windx < 0.0f) {
                windx = 0.0f;
            }
            if (!this.serverEastAvailable && windx > 0.0f) {
                windx = 0.0f;
            }
            if (!this.serverSouthAvailable && windy > 0.0f) {
                windy = 0.0f;
            }
            if (!this.serverNorthAvailable && windy < 0.0f) {
                windy = 0.0f;
            }
            this.xa += windx;
            this.ya -= windy;
        }
        float waterHeight = -1.45f;
        if (this.commandingBoat) {
            dHeight2 = this.getHeight(this.x + this.xa, this.y + this.ya, this.getHeight(this.x, this.y, -3000.0f));
            if (dHeight2 < maxDepth || dHeight2 > maxHeight) {
                this.xa = 0.0f;
                this.ya = 0.0f;
            }
            if (this.layer == 0 && this.getTextureForTile(currentTileX, currentTileY, this.layer, this.bridgeId) == Tiles.Tile.TILE_HOLE.id) {
                this.layer = -1;
                this.setLayer(this.layer);
            }
        } else if (heightTarget < waterHeight) {
            heightTarget = waterHeight;
            this.xa *= 0.6f;
            this.ya *= 0.6f;
            dHeight2 = this.getHeight(this.x + this.xa, this.y + this.ya, this.getHeight(this.x, this.y, -3000.0f));
            if (dHeight2 < maxDepth || dHeight2 > maxHeight) {
                this.xa = 0.0f;
                this.ya = 0.0f;
            }
            if (this.onGround && this.layer == 0 && this.getTextureForTile(currentTileX, currentTileY, this.layer, this.bridgeId) == Tiles.Tile.TILE_HOLE.id) {
                this.layer = -1;
                this.setLayer(this.layer);
            }
        } else if (this.onGround) {
            if (this.layer == 0 && this.getTextureForTile(currentTileX, currentTileY, this.layer, this.bridgeId) == Tiles.Tile.TILE_HOLE.id) {
                this.layer = -1;
                this.setLayer(this.layer);
            }
            tileSpeedMod = this.isOnFloor() ? 1.0f : this.getSpeedForTile(currentTileX, currentTileY, this.layer);
            this.xa *= tileSpeedMod;
            this.ya *= tileSpeedMod;
            dHeight = this.getHeight(this.x + this.xa, this.y + this.ya, this.getHeight(this.x, this.y, -3000.0f));
            if (dHeight < maxDepth || dHeight > maxHeight) {
                this.xa = 0.0f;
                this.ya = 0.0f;
            } else {
                byte text;
                float hDiff = this.getHeight(this.x + this.xa, this.y + this.ya, heightTarget) - heightTarget;
                if (hDiff > 0.0f) {
                    float dist = (float)StrictMath.sqrt(this.xa * this.xa + this.ya * this.ya);
                    this.xa /= hDiff * hDiff / dist * 10.0f + 1.0f;
                    this.ya /= hDiff * hDiff / dist * 10.0f + 1.0f;
                }
                int ntx = (int)StrictMath.floor((this.x + this.xa) / 4.0f);
                int nty = (int)StrictMath.floor((this.y + this.ya) / 4.0f);
                if ((currentTileX != ntx || currentTileY != nty) && !Tiles.isSolidCave(text = this.getTextureForTile(ntx, nty, this.layer, this.bridgeId)) && text != Tiles.Tile.TILE_HOLE.id && this.getTileSteepness(ntx, nty, this.layer) > maxHeightDiff * 100.0f && (this.getHeightOfBridge(ntx, nty, this.layer) <= -1000.0f || this.bridgeId <= 0L && hDiff > 0.0f)) {
                    this.xa = 0.0f;
                    this.ya = 0.0f;
                }
                if (this.started && !this.climbing && !isCommanding) {
                    float suggestedHeight = this.getHeight(this.x, this.y, -3000.0f);
                    float xSlip = (this.getHeight(this.x - 0.25f, this.y, suggestedHeight) - this.getHeight(this.x + 0.25f, this.y, suggestedHeight)) / 0.5f;
                    float ySlip = (this.getHeight(this.x, this.y - 0.25f, suggestedHeight) - this.getHeight(this.x, this.y + 0.25f, suggestedHeight)) / 0.5f;
                    float slipTreshold = 0.6f;
                    float slipDampen = 0.3f;
                    xSlip = xSlip > 0.6f ? (xSlip -= 0.3f) : (xSlip < -0.6f ? (xSlip += 0.3f) : 0.0f);
                    ySlip = ySlip > 0.6f ? (ySlip -= 0.3f) : (ySlip < -0.6f ? (ySlip += 0.3f) : 0.0f);
                    if (xSlip != 0.0f || ySlip != 0.0f) {
                        float slipDist = xSlip * xSlip + ySlip * ySlip;
                        float dist = slipDist * 0.25f;
                        if (dist > 0.2f) {
                            dist = 0.2f;
                        }
                        slipDist = (float)Math.sqrt(slipDist);
                        xSlip = xSlip * dist / slipDist;
                        ySlip = ySlip * dist / slipDist;
                        this.xa += xSlip;
                        this.ya += ySlip;
                    }
                }
            }
        } else if (this.layer == 0 && this.getTextureForTile(currentTileX, currentTileY, this.layer, this.bridgeId) == Tiles.Tile.TILE_HOLE.id) {
            this.layer = -1;
            this.setLayer(this.layer);
        } else {
            float dist;
            float hDiff;
            if (this.isOnFloor() && this.bridgeId <= 0L) {
                tileSpeedMod = 1.0f;
                this.xa *= 1.0f;
                this.ya *= 1.0f;
            } else if (this.bridgeId > 0L) {
                tileSpeedMod = this.getSpeedForTile(currentTileX, currentTileY, this.layer);
                this.xa *= tileSpeedMod;
                this.ya *= tileSpeedMod;
            }
            boolean onBridge = false;
            if (isCommanding && !this.commandingBoat && this.bridgeId == -10L) {
                byte text;
                hDiff = this.getHeight(this.x + this.xa, this.y + this.ya, heightTarget) - heightTarget;
                if (hDiff > 0.0f) {
                    dist = (float)StrictMath.sqrt(this.xa * this.xa + this.ya * this.ya);
                    this.xa /= hDiff * hDiff / dist * 20.0f + 1.0f;
                    this.ya /= hDiff * hDiff / dist * 20.0f + 1.0f;
                }
                int ntx = (int)StrictMath.floor((this.x + this.xa) / 4.0f);
                int nty = (int)StrictMath.floor((this.y + this.ya) / 4.0f);
                if ((currentTileX != ntx || currentTileY != nty) && !Tiles.isSolidCave(text = this.getTextureForTile(ntx, nty, this.layer, this.bridgeId)) && text != Tiles.Tile.TILE_HOLE.id && this.getTileSteepness(ntx, nty, this.layer) > maxHeightDiff * 100.0f) {
                    if (this.getHeightOfBridge(ntx, nty, this.layer) <= -1000.0f || this.bridgeId <= 0L && hDiff > 0.0f) {
                        this.xa = 0.0f;
                        this.ya = 0.0f;
                    } else {
                        onBridge = true;
                    }
                }
            } else if (this.bridgeId != -10L) {
                hDiff = this.getHeight(this.x + this.xa, this.y + this.ya, heightTarget) - heightTarget;
                if (hDiff > 0.0f && hDiff < 1.0f) {
                    dist = (float)StrictMath.sqrt(this.xa * this.xa + this.ya * this.ya);
                    this.xa /= hDiff * hDiff / dist * 10.0f + 1.0f;
                    this.ya /= hDiff * hDiff / dist * 10.0f + 1.0f;
                }
                this.maybePrintDebugInfo(75);
            }
            dHeight = this.getHeight(this.x + this.xa, this.y + this.ya, this.getHeight(this.x, this.y, -3000.0f));
            if (!onBridge && (dHeight < maxDepth || dHeight > maxHeight)) {
                this.xa = 0.0f;
                this.ya = 0.0f;
            }
        }
        if (Math.abs(this.getTargetGroundOffset() - this.getGroundOffset()) > 3.0f) {
            this.xa = 0.0f;
            this.ya = 0.0f;
        }
        float dist = this.xa * this.xa + this.ya * this.ya;
        float maxSpeed = 0.65000004f;
        if (dist > 0.42250004f) {
            dist = (float)Math.sqrt(dist);
            this.xa = this.xa / dist * 0.65000004f;
            this.ya = this.ya / dist * 0.65000004f;
            this.za = this.za / dist * 0.65000004f;
        }
        this.xOld = this.x;
        this.yOld = this.y;
        this.zOld = this.z;
        int nextTileX = (int)((this.x + this.xa) / 4.0f);
        int nextTileY = (int)((this.y + this.ya) / 4.0f);
        if (this.layer == -1 && Tiles.isSolidCave(this.getTextureForTile(currentTileX, currentTileY, this.layer, this.bridgeId))) {
            this.handlePlayerInRock();
        } else if (this.layer == -1 && this.getTextureForTile(currentTileX, currentTileY, this.layer, this.bridgeId) == Tiles.Tile.TILE_CAVE_EXIT.id) {
            if (Tiles.isSolidCave(this.getTextureForTile(nextTileX, nextTileY, this.layer, this.bridgeId))) {
                this.layer = 0;
                float dHeight3 = this.getHeight(this.x + this.xa, this.y + this.ya, this.getHeight(this.x, this.y, -3000.0f));
                if (dHeight3 > maxHeight) {
                    this.layer = -1;
                    this.xa = 0.0f;
                    this.ya = 0.0f;
                } else {
                    this.setLayer(this.layer);
                }
            } else {
                int diffx = nextTileX - currentTileX;
                int diffy = nextTileY - currentTileY;
                if (diffx != 0 && diffy != 0) {
                    if (diffx < 0 && diffy < 0) {
                        byte text = this.getTextureForTile(currentTileX - 1, currentTileY, -1, this.bridgeId);
                        byte text2 = this.getTextureForTile(currentTileX, currentTileY - 1, -1, this.bridgeId);
                        if (Tiles.isSolidCave(text) && Tiles.isSolidCave(text2)) {
                            this.xa = 0.0f;
                            this.ya = 0.0f;
                        }
                    }
                    if (diffx > 0 && diffy < 0) {
                        byte text = this.getTextureForTile(currentTileX + 1, currentTileY, -1, this.bridgeId);
                        byte text2 = this.getTextureForTile(currentTileX, currentTileY - 1, -1, this.bridgeId);
                        if (Tiles.isSolidCave(text) && Tiles.isSolidCave(text2)) {
                            this.xa = 0.0f;
                            this.ya = 0.0f;
                        }
                    }
                    if (diffx > 0 && diffy > 0) {
                        byte text = this.getTextureForTile(currentTileX + 1, currentTileY, -1, this.bridgeId);
                        byte text2 = this.getTextureForTile(currentTileX, currentTileY + 1, -1, this.bridgeId);
                        if (Tiles.isSolidCave(text) && Tiles.isSolidCave(text2)) {
                            this.xa = 0.0f;
                            this.ya = 0.0f;
                        }
                    }
                    if (diffx < 0 && diffy > 0) {
                        byte text = this.getTextureForTile(currentTileX - 1, currentTileY, -1, this.bridgeId);
                        byte text2 = this.getTextureForTile(currentTileX, currentTileY + 1, -1, this.bridgeId);
                        if (Tiles.isSolidCave(text) && Tiles.isSolidCave(text2)) {
                            this.xa = 0.0f;
                            this.ya = 0.0f;
                        }
                    }
                }
            }
        }
        this.x += this.xa;
        this.y += this.ya;
        this.z += this.za;
        this.updateGroundOffset();
        float nextHeightTarget = Math.max(this.getHeight(this.x, this.y, -3000.0f), waterHeight);
        boolean bl2 = this.onGround = this.z <= nextHeightTarget && !this.isOnFloor();
        if (!(!isCommanding && this.offZ == 0.0f || this.isOnFloor() && this.commandingBoat)) {
            this.onGround = false;
            this.inWater = false;
            if (!this.commandingBoat) {
                if (this.isOnFloor() && (double)(this.z - nextHeightTarget) > 2.9 + (double)(this.groundOffset / 10.0f)) {
                    this.za = 0.0f;
                } else {
                    this.z = nextHeightTarget + (this.isOnFloor() ? 0.25f : 0.0f);
                    this.za = 0.0f;
                }
            }
        } else if (this.onGround) {
            boolean landed = false;
            if ((double)this.za < -0.25 && !this.inWater && this.bridgeId <= 0L) {
                this.hitGround(-this.za);
                landed = true;
            }
            if (landed && nextHeightTarget > heightTarget) {
                float dzPlayer = this.z - this.zOld;
                float dzTerrain = nextHeightTarget - heightTarget;
                float intersection = (this.zOld - heightTarget) / (dzTerrain - dzPlayer);
                this.xa = 0.0f;
                this.ya = 0.0f;
                this.za = 0.0f;
                this.x = this.xOld + intersection * (this.x - this.xOld);
                this.y = this.yOld + intersection * (this.y - this.yOld);
                this.z = this.zOld + intersection * dzPlayer;
            } else {
                this.z = nextHeightTarget;
                this.za = 0.0f;
            }
        } else if (this.isOnFloor()) {
            if (this.bridgeId <= 0L || this.z < nextHeightTarget) {
                this.z = this.isAdjustingGroundOffset() || this.xa == 0.0f && this.ya == 0.0f ? nextHeightTarget : this.zOld;
            }
            if ((double)this.za < -0.25 && !this.inWater && this.isOnFloor() && !isCommanding) {
                this.hitGround(-this.za);
            }
            this.za = 0.0f;
        }
        if (this.onGround || this.inWater || isCommanding || this.isOnFloor()) {
            this.xa *= this.getMoveMod();
            this.ya *= this.getMoveMod();
        }
        if ((isCommanding || this.offZ != 0.0f || this.flying) && !this.isFalling()) {
            this.za = 0.0f;
        } else if (this.started) {
            this.za -= this.getFallMod();
        }
        if (this.wasOnStair) {
            this.wasOnStair = false;
        }
        if (isCommanding) {
            this.maybePrintDebugInfo(100);
        }
    }

    protected float getWaterLevel(float x, float y) {
        return 0.0f;
    }

    private final void maybePrintDebugInfo(int step) {
        this.maybePrintDebugInfo(step, 0.0f, 0.0f, 0.0f);
    }

    private final void maybePrintDebugInfo(int step, float val1, float val2, float val3) {
    }

    public void setOnFloorOverride(boolean onFloor) {
        if (onFloor != this.onFloorOverridden) {
            this.counter = 0;
        }
        this.onFloorOverridden = onFloor;
    }

    public boolean getOnFloorOverride() {
        return this.onFloorOverridden;
    }

    public boolean isOnFloor() {
        return this.getGroundOffset() > 0.0f && !this.isAdjustingGroundOffset() || this.bridgeId > 0L || this.onFloorOverridden;
    }

    public final float getFallMod() {
        return 0.04f;
    }

    public final float getMoveMod() {
        return 0.4f;
    }

    public boolean movedOnStair() {
        return this.wasOnStair;
    }

    private final boolean isCommanding(float maxDepth, float maxHeight) {
        return maxDepth > -2500.0f || maxHeight < 2500.0f;
    }

    protected abstract void hitGround(float var1);

    public abstract float getTileSteepness(int var1, int var2, int var3);

    private float getHeight(float xp, float yp, float suggestedHeight) {
        int xx = (int)StrictMath.floor(xp / 4.0f);
        int yy = (int)StrictMath.floor(yp / 4.0f);
        if (this.layer == 0 && this.getTextureForTile(xx, yy, this.layer, this.bridgeId) == Tiles.Tile.TILE_HOLE.id) {
            return this.getHeight(xp, yp, suggestedHeight, -1);
        }
        if (this.layer == -1 && Tiles.isSolidCave(this.getTextureForTile(xx, yy, this.layer, this.bridgeId))) {
            return suggestedHeight;
        }
        return this.getHeight(xp, yp, suggestedHeight, this.layer);
    }

    private final float getHeight(float xp, float yp, float suggestedHeight, int layer) {
        byte id;
        int xx = (int)StrictMath.floor(xp / 4.0f);
        int yy = (int)StrictMath.floor(yp / 4.0f);
        float xa = xp / 4.0f - (float)xx;
        float ya = yp / 4.0f - (float)yy;
        if (layer == -1 && suggestedHeight > -2999.0f && ((id = this.getTextureForTile(xx, yy, layer, this.bridgeId)) == Tiles.Tile.TILE_CAVE_WALL.id || id == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id)) {
            return suggestedHeight;
        }
        float[] hts = this.getNodeHeights(xx, yy, layer, this.bridgeId);
        float height = hts[0] * (1.0f - xa) * (1.0f - ya) + hts[1] * xa * (1.0f - ya) + hts[2] * (1.0f - xa) * ya + hts[3] * xa * ya;
        return height + this.getCurrentGroundOffset() / 10.0f;
    }

    public final void setSpeedModifier(float speedModifier) {
        this.speedMod = speedModifier;
    }

    public final void setPosition(float x, float y, float z, float xRot, int _layer) {
        this.abort = true;
        this.onGround = false;
        this.inWater = false;
        this.x = x;
        this.y = y;
        this.z = z;
        this.xRot = xRot;
        this.layer = _layer;
        this.xa = 0.0f;
        this.ya = 0.0f;
        this.za = 0.0f;
        if (this.layer != _layer) {
            this.setLayer(_layer);
        }
    }

    public final void changeLayer(int _layer) {
        this.layer = _layer;
    }

    private float getSpeedForTile(int xTile, int yTile, int layer) {
        try {
            return Tiles.getTile(this.getTextureForTile(xTile, yTile, layer, this.bridgeId)).getSpeed();
        }
        catch (NullPointerException e) {
            System.out.println("Can't get speed for tile " + xTile + ", " + yTile + ", layer " + layer + ", since it's of id " + this.getTextureForTile(xTile, yTile, layer, this.bridgeId));
            return 0.1f;
        }
    }

    public static final byte buildBitmap(boolean f, boolean b, boolean l, boolean r) {
        byte result = 0;
        if (f) {
            result = (byte)(result | 1);
        }
        if (b) {
            result = (byte)(result | 2);
        }
        if (l) {
            result = (byte)(result | 4);
        }
        if (r) {
            result = (byte)(result | 8);
        }
        return result;
    }

    public final float getX() {
        return this.x;
    }

    public final float getY() {
        return this.y;
    }

    public final float getZ() {
        return this.z;
    }

    public final float getRot() {
        return this.xRot;
    }

    protected boolean isServerWestAvailable() {
        return this.serverWestAvailable;
    }

    protected void setServerWestAvailable(boolean serverWestAvailable) {
        this.serverWestAvailable = serverWestAvailable;
    }

    protected boolean isServerNorthAvailable() {
        return this.serverNorthAvailable;
    }

    protected void setServerNorthAvailable(boolean serverNorthAvailable) {
        this.serverNorthAvailable = serverNorthAvailable;
    }

    protected boolean isServerEastAvailable() {
        return this.serverEastAvailable;
    }

    protected void setServerEastAvailable(boolean serverEastAvailable) {
        this.serverEastAvailable = serverEastAvailable;
    }

    protected boolean isServerSouthAvailable() {
        return this.serverSouthAvailable;
    }

    protected void setServerSouthAvailable(boolean serverSouthAvailable) {
        this.serverSouthAvailable = serverSouthAvailable;
    }

    protected float getXa() {
        return this.xa;
    }

    protected void setXa(float xa) {
        this.xa = xa;
    }

    protected float getYa() {
        return this.ya;
    }

    protected void setYa(float ya) {
        this.ya = ya;
    }

    protected float getZa() {
        return this.za;
    }

    protected void setZa(float za) {
        this.za = za;
    }

    protected boolean isOnGround() {
        return this.onGround;
    }

    protected void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public boolean isInWater() {
        return this.inWater;
    }

    protected void setInWater(boolean inWater) {
        this.inWater = inWater;
    }

    protected boolean isIgnoreErrors() {
        return this.ignoreErrors;
    }

    protected void setIgnoreErrors(boolean ignoreErrors) {
        this.ignoreErrors = ignoreErrors;
    }

    protected boolean isStarted() {
        return this.started;
    }

    protected void setStarted(boolean started) {
        this.started = started;
    }

    protected float getDiffWindX() {
        return this.diffWindX;
    }

    protected void setDiffWindX(float diffWindX) {
        this.diffWindX = diffWindX;
    }

    protected float getDiffWindY() {
        return this.diffWindY;
    }

    protected void setDiffWindY(float diffWindY) {
        this.diffWindY = diffWindY;
    }

    protected boolean isCommandingBoat() {
        return this.commandingBoat;
    }

    protected void setCommandingBoat(boolean commandingBoat) {
        this.commandingBoat = commandingBoat;
    }

    protected float getCurrx() {
        return this.currx;
    }

    protected float getCurry() {
        return this.curry;
    }

    protected boolean isFirst() {
        return this.first;
    }

    protected void setFirst(boolean first) {
        this.first = first;
    }

    protected boolean isMovingVehicle() {
        return this.movingVehicle;
    }

    protected float getOffZ() {
        return this.offZ;
    }

    protected void setOffZ(float offZ) {
        this.offZ = offZ;
    }

    protected boolean isClimbing() {
        return this.climbing;
    }

    protected void setX(float x) {
        this.x = x;
    }

    protected void setY(float y) {
        this.y = y;
    }

    protected void setZ(float z) {
        this.z = z;
    }

    public abstract float getHeightOfBridge(int var1, int var2, int var3);

    protected abstract byte getTextureForTile(int var1, int var2, int var3, long var4);

    protected abstract float getCeilingForNode(int var1, int var2);

    protected abstract float getHeightForNode(int var1, int var2, int var3);

    protected abstract float[] getNodeHeights(int var1, int var2, int var3, long var4);

    protected abstract boolean handleWrongLayer(int var1, int var2);

    protected abstract boolean handleMoveTooFar(float var1, float var2);

    protected abstract boolean handleMoveTooShort(float var1, float var2);

    protected abstract boolean handleZError(float var1, float var2);

    protected abstract void handlePlayerInRock();

    protected void setLayer(int layer) {
    }

    public final void fly(float xTarget, float yTarget, float zTarget, float xRot, float yRot, byte bitmask, int layerTarget) {
        float height;
        this.x = xTarget;
        this.y = yTarget;
        this.z = zTarget;
        this.layer = layerTarget;
        this.onGround = false;
        this.xRot = xRot;
        this.bitmask = bitmask;
        float speedModifier = 1.0f;
        int dirs = 0;
        float xPosMod = 0.0f;
        float yPosMod = 0.0f;
        float zPosMod = 0.0f;
        if (this.isPressed(1)) {
            ++dirs;
            xPosMod += (float)StrictMath.sin(xRot * ((float)Math.PI / 180)) * 0.08f * 1.0f * (float)StrictMath.cos(yRot * ((float)Math.PI / 180));
            yPosMod -= (float)StrictMath.cos(xRot * ((float)Math.PI / 180)) * 0.08f * 1.0f * (float)StrictMath.cos(yRot * ((float)Math.PI / 180));
            zPosMod -= (float)StrictMath.sin(yRot * ((float)Math.PI / 180)) * 0.08f * 1.0f;
        }
        if (this.isPressed(2)) {
            ++dirs;
            xPosMod -= (float)StrictMath.sin(xRot * ((float)Math.PI / 180)) * 0.08f * 1.0f * (float)StrictMath.cos(yRot * ((float)Math.PI / 180));
            yPosMod += (float)StrictMath.cos(xRot * ((float)Math.PI / 180)) * 0.08f * 1.0f * (float)StrictMath.cos(yRot * ((float)Math.PI / 180));
            zPosMod += (float)StrictMath.sin(yRot * ((float)Math.PI / 180)) * 0.08f * 1.0f;
        }
        if (this.isPressed(4)) {
            ++dirs;
            xPosMod -= (float)StrictMath.cos(xRot * ((float)Math.PI / 180)) * 0.08f * 1.0f;
            yPosMod -= (float)StrictMath.sin(xRot * ((float)Math.PI / 180)) * 0.08f * 1.0f;
        }
        if (this.isPressed(8)) {
            ++dirs;
            xPosMod += (float)StrictMath.cos(xRot * ((float)Math.PI / 180)) * 0.08f * 1.0f;
            yPosMod += (float)StrictMath.sin(xRot * ((float)Math.PI / 180)) * 0.08f * 1.0f;
        }
        if (dirs > 0) {
            this.xa = (float)((double)this.xa + (double)xPosMod / StrictMath.sqrt(dirs));
            this.ya = (float)((double)this.ya + (double)yPosMod / StrictMath.sqrt(dirs));
            this.za = (float)((double)this.za + (double)zPosMod / StrictMath.sqrt(dirs));
        }
        if ((double)(height = this.getHeight(this.x, this.y, -3000.0f)) < -1.45) {
            height = -1.45f;
        }
        float dist = this.xa * this.xa + this.ya * this.ya;
        float maxSpeed = 0.65000004f;
        if (dist > 0.42250004f) {
            dist = (float)Math.sqrt(dist);
            this.xa = this.xa / dist * 0.65000004f;
            this.ya = this.ya / dist * 0.65000004f;
            this.za = this.za / dist * 0.65000004f;
        }
        this.xOld = this.x;
        this.yOld = this.y;
        this.zOld = this.z;
        int xx = (int)(this.x / 4.0f);
        int yy = (int)(this.y / 4.0f);
        this.x += this.xa;
        this.y += this.ya;
        this.z += this.za;
        int newxx = (int)(this.x / 4.0f);
        int newyy = (int)(this.y / 4.0f);
        if (this.layer == -1 && this.getTextureForTile(xx, yy, this.layer, this.bridgeId) != Tiles.Tile.TILE_CAVE.id && !Tiles.isReinforcedFloor(this.getTextureForTile(xx, yy, this.layer, this.bridgeId)) && this.getTextureForTile(xx, yy, this.layer, this.bridgeId) != Tiles.Tile.TILE_CAVE_EXIT.id) {
            this.handlePlayerInRock();
        } else if (this.layer == -1 && this.getTextureForTile(xx, yy, this.layer, this.bridgeId) == Tiles.Tile.TILE_CAVE_EXIT.id) {
            if (this.getTextureForTile(newxx, newyy, this.layer, this.bridgeId) == Tiles.Tile.TILE_CAVE_WALL.id || this.getTextureForTile(newxx, newyy, this.layer, this.bridgeId) == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id) {
                this.layer = 0;
                this.setLayer(this.layer);
            } else {
                if (newyy == yy) {
                    int xa;
                    int n = xa = newxx < xx ? 0 : 1;
                    if (this.getCeilingForNode(xx + xa, yy) < 0.5f && this.getCeilingForNode(xx + xa, yy + 1) < 0.5f) {
                        this.layer = 0;
                        this.setLayer(this.layer);
                    }
                }
                if (newxx == xx) {
                    int ya;
                    int n = ya = newyy < yy ? 0 : 1;
                    if (this.getCeilingForNode(xx, yy + ya) < 0.5f && this.getCeilingForNode(xx + 1, yy + ya) < 0.5f) {
                        this.layer = 0;
                        this.setLayer(this.layer);
                    }
                }
            }
        }
        if (this.z < height) {
            this.z = height;
            this.za = 0.0f;
        }
        this.xa *= 0.9f;
        this.ya *= 0.9f;
        this.za *= 0.9f;
    }

    public final void setClimbing(boolean climbing) {
        this.climbing = climbing;
    }

    public final int getLayer() {
        return this.layer;
    }

    public void setMountSpeed(float newMountSpeed) {
        this.mountSpeed = newMountSpeed;
    }

    public float getWindImpact() {
        return this.windImpact;
    }

    public void setWindImpact(float wrot) {
        this.windImpact = wrot;
    }

    public static final float normalizeAngle(float angle) {
        if ((angle -= (float)((int)(angle / 360.0f) * 360)) < 0.0f) {
            angle += 360.0f;
        }
        return angle;
    }

    public void reset() {
        this.setMountSpeed(0.0f);
        this.setWindImpact(0.0f);
        this.setWindRotation(0.0f);
        this.setWindStrength(0.0f);
        this.diffWindX = 0.0f;
        this.diffWindY = 0.0f;
    }

    public static final float getWindPower(float aWindRotation, float aVehicleRotation) {
        float lWindRotation = MovementChecker.normalizeAngle(aWindRotation);
        float lVehicleRotation = lWindRotation > aVehicleRotation ? MovementChecker.normalizeAngle(lWindRotation - aVehicleRotation) : MovementChecker.normalizeAngle(aVehicleRotation - lWindRotation);
        if (lVehicleRotation > 150.0f && lVehicleRotation < 210.0f) {
            return 0.0f;
        }
        if (lVehicleRotation > 120.0f && lVehicleRotation < 240.0f) {
            return 0.5f;
        }
        if (lVehicleRotation > 90.0f && lVehicleRotation < 270.0f) {
            return 0.65f;
        }
        if (lVehicleRotation > 60.0f && lVehicleRotation < 300.0f) {
            return 0.8f;
        }
        if (lVehicleRotation > 30.0f && lVehicleRotation < 330.0f) {
            return 1.0f;
        }
        return 0.9f;
    }

    public final float getSpeedMod() {
        return this.speedMod;
    }

    public final float getMountSpeed() {
        return this.mountSpeed;
    }

    protected float getXold() {
        return this.xOld;
    }

    protected float getYold() {
        return this.yOld;
    }

    protected float getZold() {
        return this.zOld;
    }

    protected void setLog(boolean log) {
    }

    public byte getBitMask() {
        return this.bitmask;
    }

    public float getVehicleRotation() {
        return this.vehicleRotation;
    }

    public void setVehicleRotation(float rotation) {
        this.vehicleRotation = rotation;
    }

    public float getWindStrength() {
        return this.windStrength;
    }

    public void setWindStrength(float wstr) {
        this.windStrength = wstr;
    }

    public float getWindRotation() {
        return this.windRotation;
    }

    public void setWindRotation(float wrot) {
        this.windRotation = wrot;
    }

    public void setGroundOffset(int newOffset, boolean immediately) {
        this.setTargetGroundOffset(Math.min(this.getMaxTargetGroundOffset(newOffset), newOffset));
        if (immediately) {
            this.setGroundOffset(this.getTargetGroundOffset());
        }
    }

    public int getMaxTargetGroundOffset(int suggestedOffset) {
        return suggestedOffset;
    }

    public final float getTargetGroundOffset() {
        return this.targetGroundOffset;
    }

    public final void setTargetGroundOffset(int newOffset) {
        this.targetGroundOffset = newOffset;
    }

    public final float getGroundOffset() {
        return this.groundOffset;
    }

    public final void setGroundOffset(float newOffset) {
        this.groundOffset = newOffset;
    }

    private final float getCurrentGroundOffset() {
        return this.getGroundOffset();
    }

    private final void updateGroundOffset() {
        if (this.getTargetGroundOffset() > this.getGroundOffset() + 1.0f) {
            this.setGroundOffset(this.getGroundOffset() + 1.0f);
        } else if (this.getTargetGroundOffset() < this.getGroundOffset() - 1.0f) {
            this.setGroundOffset(this.getGroundOffset() - 1.0f);
        } else {
            this.setGroundOffset(this.getTargetGroundOffset());
        }
    }

    public final boolean isAdjustingGroundOffset() {
        return this.getGroundOffset() != this.getTargetGroundOffset();
    }

    public String getInfo() {
        return "commanding boat: " + this.commandingBoat + "in water=" + this.inWater + " onground=" + this.onGround + " speedmod=" + this.speedMod + ",mountspeed=" + this.mountSpeed + " vehic rot " + this.vehicleRotation + " windrot=" + this.windRotation + " wind str=" + this.windStrength + " windImpact=" + this.windImpact;
    }

    public long getBridgeId() {
        return this.bridgeId;
    }

    public void setBridgeCounter(int nums) {
        this.bridgeCounter = nums;
    }

    public void setBridgeId(long bridgeId) {
        this.bridgeId = bridgeId;
        this.bridgeCounter = 10;
    }
}

