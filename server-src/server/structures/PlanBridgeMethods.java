/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.structures;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Point;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.questions.PlanBridgeQuestion;
import com.wurmonline.server.structures.DbBridgePart;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.PlanBridgeChecks;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.BridgeConstants;
import com.wurmonline.shared.constants.StructureTypeEnum;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PlanBridgeMethods
implements MiscConstants {
    private static final Logger logger = Logger.getLogger(PlanBridgeQuestion.class.getName());
    private static boolean DEBUG = true;
    private static final int[] highest = new int[]{0, 2, 5, 10, 20, 25, 30, 32, 40, 45, 57, 58, 65, 70, 80, 83, 90, 96, 105, 108, 115, 121, 129, 134, 141, 147, 155, 159, 166, 172, 180, 185, 191, 197, 205, 210, 217, 223, 230, 236, 242, 248, 256, 261, 268, 274, 281, 287, 293, 299, 306, 312, 319, 325, 332, 337, 344, 350, 357, 363, 369, 376, 383, 388, 395, 401, 408, 414, 420, 427, 434, 439, 446, 452, 459, 465, 471, 477, 484, 490, 497, 503, 510, 516, 522, 528, 535, 541, 548, 554, 561, 567, 573, 579, 586, 592, 599, 605, 612, 618, 624, 630, 637};
    private static final int[] height0 = new int[0];
    private static final int[] height1 = new int[]{20};
    private static final int[] height2 = new int[]{15, 30};
    private static final int[] height3 = new int[]{10, 30, 40};
    private static final int[] height4 = new int[]{8, 28, 48, 57};
    private static final int[] height5 = new int[]{6, 22, 42, 59, 65};
    private static final int[] height6 = new int[]{5, 20, 40, 60, 75, 80};
    private static final int[] height7 = new int[]{4, 17, 35, 55, 73, 85, 90};
    private static final int[] height8 = new int[]{4, 15, 32, 52, 72, 89, 101, 105};
    private static final int[] height9 = new int[]{3, 13, 29, 48, 68, 86, 102, 112, 115};
    private static final int[] height10 = new int[]{3, 12, 27, 45, 65, 85, 103, 117, 126, 129};
    private static final int[] height11 = new int[]{3, 11, 24, 41, 60, 80, 99, 116, 129, 138, 141};
    private static final int[] height12 = new int[]{3, 10, 23, 39, 57, 77, 97, 116, 132, 144, 152, 155};
    private static final int[] height13 = new int[]{2, 10, 21, 36, 54, 73, 93, 112, 130, 145, 156, 164, 166};
    private static final int[] height14 = new int[]{2, 9, 20, 34, 51, 70, 90, 110, 129, 146, 160, 171, 178, 180};
    private static final int[] height15 = new int[]{2, 8, 18, 32, 48, 66, 86, 106, 125, 144, 160, 173, 183, 189, 191};
    private static final int[] height16 = new int[]{2, 8, 17, 30, 46, 63, 83, 103, 123, 142, 159, 175, 188, 197, 203, 205};
    private static final int[] height17 = new int[]{2, 7, 16, 28, 43, 60, 79, 98, 118, 138, 157, 174, 188, 201, 209, 215, 217};
    private static final int[] height18 = new int[]{2, 7, 15, 27, 41, 58, 76, 95, 115, 135, 155, 173, 189, 203, 215, 223, 229, 230};
    private static final int[] height19 = new int[]{2, 7, 15, 26, 39, 55, 72, 91, 111, 131, 151, 170, 187, 203, 217, 228, 236, 241, 242};
    private static final int[] height20 = new int[]{2, 6, 14, 24, 37, 53, 70, 88, 108, 128, 148, 167, 186, 203, 218, 231, 242, 249, 254, 256};
    private static final int[] height21 = new int[]{1, 6, 13, 23, 36, 50, 67, 85, 104, 124, 144, 164, 183, 201, 217, 232, 244, 254, 262, 266, 268};
    private static final int[] height22 = new int[]{1, 6, 13, 22, 34, 49, 65, 82, 101, 121, 141, 161, 180, 199, 217, 233, 247, 259, 268, 275, 280, 281};
    private static final int[] height23 = new int[]{1, 5, 12, 21, 33, 47, 62, 79, 97, 117, 137, 157, 176, 196, 214, 231, 247, 260, 272, 281, 288, 292, 293};
    private static final int[] height24 = new int[]{1, 5, 12, 21, 32, 45, 60, 77, 95, 114, 133, 153, 173, 193, 212, 230, 247, 262, 275, 286, 295, 301, 305, 306};
    private static final int[] height25 = new int[]{1, 5, 11, 20, 30, 43, 58, 74, 91, 110, 129, 149, 169, 189, 208, 227, 245, 261, 275, 288, 299, 307, 314, 317, 319};
    private static final int[] height26 = new int[]{1, 5, 11, 19, 29, 42, 56, 72, 89, 107, 126, 146, 166, 186, 206, 225, 243, 260, 276, 290, 302, 313, 321, 327, 331, 332};
    private static final int[] height27 = new int[]{1, 5, 10, 18, 28, 40, 54, 69, 86, 104, 123, 142, 162, 182, 202, 221, 240, 258, 275, 290, 304, 316, 326, 334, 339, 343, 344};
    private static final int[] height28 = new int[]{1, 4, 10, 18, 27, 39, 52, 67, 84, 101, 120, 139, 159, 179, 199, 218, 238, 256, 274, 290, 305, 318, 330, 340, 347, 353, 356, 357};
    private static final int[] height29 = new int[]{1, 4, 10, 17, 26, 38, 51, 65, 81, 98, 116, 135, 155, 175, 195, 215, 234, 253, 271, 288, 304, 319, 332, 343, 352, 360, 365, 368, 369};
    private static final int[] height30 = new int[]{1, 4, 9, 17, 26, 37, 49, 63, 79, 96, 114, 132, 152, 171, 191, 211, 231, 250, 269, 287, 304, 319, 334, 346, 357, 366, 373, 378, 382, 383};
    private static final int[] height31 = new int[]{1, 4, 9, 16, 25, 35, 48, 61, 77, 93, 110, 129, 148, 168, 187, 207, 227, 247, 266, 284, 302, 318, 333, 347, 359, 370, 379, 386, 391, 394, 395};
    private static final int[] height32 = new int[]{1, 4, 9, 16, 24, 34, 46, 60, 75, 91, 108, 126, 145, 164, 184, 204, 224, 244, 263, 282, 300, 317, 333, 348, 362, 374, 384, 393, 399, 404, 407, 408};
    private static final int[] height33 = new int[]{1, 4, 9, 15, 23, 33, 45, 58, 73, 88, 105, 123, 141, 161, 180, 200, 220, 240, 260, 279, 297, 315, 332, 348, 362, 375, 387, 397, 405, 412, 417, 419, 420};
    private static final int[] height34 = new int[]{1, 4, 8, 15, 23, 32, 44, 57, 71, 86, 103, 120, 138, 157, 177, 197, 217, 237, 257, 276, 295, 313, 331, 347, 363, 377, 390, 401, 411, 419, 425, 430, 433, 434};
    private static final int[] height35 = new int[]{1, 4, 8, 14, 22, 32, 43, 55, 69, 84, 100, 117, 135, 154, 173, 193, 213, 233, 253, 272, 292, 310, 329, 346, 362, 377, 391, 403, 414, 424, 432, 438, 442, 445, 446};
    private static final int[] height36 = new int[]{1, 3, 8, 14, 21, 31, 41, 54, 67, 82, 98, 115, 132, 151, 170, 190, 209, 229, 249, 269, 289, 308, 326, 344, 361, 377, 392, 405, 417, 428, 437, 445, 451, 455, 458, 459};
    private static final int[] height37 = new int[]{1, 3, 8, 13, 21, 30, 40, 52, 66, 80, 96, 112, 130, 148, 167, 186, 206, 226, 246, 266, 285, 305, 323, 342, 359, 376, 391, 406, 419, 431, 441, 450, 458, 464, 468, 470, 471};
    private static final int[] height38 = new int[]{1, 3, 7, 13, 20, 29, 39, 51, 64, 78, 93, 110, 127, 145, 164, 183, 202, 222, 242, 262, 282, 302, 321, 339, 357, 375, 391, 406, 420, 433, 445, 455, 464, 471, 477, 481, 484, 484};
    private static final int[] height39 = new int[]{1, 3, 7, 13, 20, 28, 38, 50, 62, 76, 91, 107, 124, 142, 160, 179, 199, 218, 238, 258, 278, 298, 317, 336, 355, 373, 389, 405, 420, 434, 447, 458, 468, 477, 484, 489, 493, 496, 497};
    private static final int[] height40 = new int[]{1, 3, 7, 12, 19, 28, 38, 49, 61, 75, 89, 105, 122, 139, 157, 176, 195, 215, 235, 255, 275, 295, 314, 334, 352, 371, 388, 405, 420, 435, 449, 461, 472, 482, 490, 497, 503, 507, 509, 510};
    private static final int[] height41 = new int[]{1, 3, 7, 12, 19, 27, 37, 48, 60, 73, 87, 103, 119, 136, 154, 173, 192, 211, 231, 251, 271, 291, 311, 330, 349, 368, 386, 403, 419, 435, 449, 462, 475, 485, 495, 503, 510, 515, 519, 521, 522};
    private static final int[] height42 = new int[]{1, 3, 7, 12, 19, 27, 36, 47, 58, 71, 86, 101, 117, 134, 152, 170, 189, 208, 228, 248, 268, 288, 308, 327, 347, 365, 384, 401, 418, 434, 450, 464, 477, 489, 499, 509, 517, 523, 529, 532, 535, 535};
    private static final int[] height43 = new int[]{1, 3, 7, 12, 18, 26, 35, 45, 57, 70, 84, 99, 114, 131, 149, 167, 185, 205, 224, 244, 264, 284, 304, 324, 343, 362, 381, 399, 416, 433, 449, 464, 478, 491, 502, 513, 522, 530, 536, 541, 545, 547, 548};
    private static final int[] height44 = new int[]{1, 3, 6, 11, 18, 25, 34, 45, 56, 68, 82, 97, 112, 129, 146, 164, 182, 201, 221, 240, 260, 280, 300, 320, 340, 359, 378, 397, 415, 432, 448, 464, 479, 492, 505, 516, 526, 535, 543, 549, 554, 558, 560, 561};
    private static final int[] height45 = new int[]{1, 3, 6, 11, 17, 25, 34, 44, 55, 67, 80, 95, 110, 126, 143, 161, 179, 198, 217, 237, 257, 277, 297, 316, 336, 356, 375, 394, 412, 430, 447, 463, 478, 493, 506, 518, 530, 540, 548, 556, 562, 567, 570, 572, 573};
    private static final int[] height46 = new int[]{1, 3, 6, 11, 17, 24, 33, 43, 54, 66, 79, 93, 108, 124, 141, 158, 176, 195, 214, 233, 253, 273, 293, 313, 333, 353, 372, 391, 410, 428, 445, 462, 478, 493, 507, 520, 533, 543, 553, 562, 569, 575, 580, 583, 585, 586};
    private static final int[] height47 = new int[]{1, 3, 6, 11, 17, 24, 32, 42, 53, 64, 77, 91, 106, 122, 138, 155, 173, 192, 211, 230, 249, 269, 289, 309, 329, 349, 369, 388, 407, 425, 443, 460, 477, 492, 507, 521, 534, 546, 557, 566, 575, 582, 588, 593, 596, 598, 599};
    private static final int[] height48 = new int[]{1, 3, 6, 10, 16, 23, 32, 41, 52, 63, 76, 90, 104, 120, 136, 153, 171, 189, 208, 227, 246, 266, 286, 306, 326, 346, 365, 385, 404, 423, 441, 459, 476, 492, 507, 522, 536, 548, 560, 571, 580, 588, 595, 601, 606, 609, 611, 612};
    private static final int[] height49 = new int[]{1, 3, 6, 10, 16, 23, 31, 40, 51, 62, 74, 88, 102, 117, 133, 150, 168, 186, 204, 223, 243, 262, 282, 302, 322, 342, 362, 381, 401, 420, 438, 456, 474, 490, 507, 522, 536, 550, 562, 573, 584, 593, 601, 608, 614, 618, 621, 623, 624};
    private static final int[] height50 = new int[]{1, 3, 6, 10, 16, 22, 30, 39, 50, 61, 73, 86, 100, 115, 131, 148, 165, 183, 201, 220, 239, 259, 279, 299, 319, 339, 358, 378, 398, 417, 436, 454, 472, 489, 506, 522, 537, 551, 564, 576, 587, 598, 607, 615, 621, 627, 631, 635, 636, 637};
    private static final int[][] heights = new int[][]{height0, height1, height2, height3, height4, height5, height6, height7, height8, height9, height10, height11, height12, height13, height14, height15, height16, height17, height18, height19, height20, height21, height22, height23, height24, height25, height26, height27, height28, height29, height30, height31, height32, height33, height34, height35, height36, height37, height38, height39, height40, height41, height42, height43, height44, height45, height46, height47, height48, height49, height50};

    private PlanBridgeMethods() {
    }

    public static int[] getHighest() {
        return highest;
    }

    public static void planBridge(Creature performer, byte dir, byte bridgeType, boolean arched, String bridgePlan, int steepness, Point start, Point end, String bridgeName) {
        boolean insta;
        int layer = performer.getLayer();
        if (Servers.isThisATestServer()) {
            performer.getCommunicator().sendNormalServerMessage("(" + bridgePlan + ")");
        }
        byte[] parts = bridgePlan.getBytes();
        int[] hts = PlanBridgeMethods.calcHeights(performer, dir, bridgeType, arched, bridgePlan, steepness, start, end);
        boolean bl = insta = performer.getPower() > 1;
        if (!PlanBridgeChecks.passChecks(performer, start, end, dir, hts, insta)) {
            return;
        }
        if (dir == 0) {
            int y;
            for (y = start.getY(); y <= end.getY(); ++y) {
                for (int x = start.getX(); x <= end.getX(); ++x) {
                    try {
                        VolaTile t = Zones.getOrCreateTile(x, y, layer == 0);
                        performer.addStructureTile(t, (byte)1);
                        t.addBridge(performer.getStructure());
                        continue;
                    }
                    catch (NoSuchStructureException e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
            PlanBridgeMethods.finaliseBridge(performer, bridgeName);
            for (y = start.getY(); y <= end.getY(); ++y) {
                int northExit = -1;
                int eastExit = -1;
                int southExit = -1;
                int westExit = -1;
                if (y == start.getY()) {
                    northExit = start.getH() - (int)(Zones.getHeightForNode(start.getX(), y, layer) * 10.0f);
                }
                if (y == end.getY()) {
                    southExit = end.getH() - (int)(Zones.getHeightForNode(end.getX(), y + 1, layer) * 10.0f);
                }
                int yy = y - start.getY();
                byte rdir = dir;
                if (parts[yy] == 97 || parts[yy] == 98) {
                    rdir = (byte)((dir + 4) % 8);
                }
                byte slope = (byte)(hts[yy + 1] - hts[yy]);
                for (int x = start.getX(); x <= end.getX(); ++x) {
                    byte ndir = rdir;
                    BridgeConstants.BridgeType bridgetype = PlanBridgeMethods.getBridgeType(dir, parts[yy], end.getX(), start.getX(), x, ndir);
                    if (!bridgetype.isAbutment() && !bridgetype.isBracing() && PlanBridgeMethods.onLeft(dir, end.getX(), start.getX(), x, ndir)) {
                        ndir = (byte)((ndir + 4) % 8);
                    }
                    try {
                        VolaTile t = Zones.getOrCreateTile(x, y, layer == 0);
                        DbBridgePart bridgePart = new DbBridgePart(bridgetype, x, y, hts[yy], 1.0f, performer.getStructure().getWurmId(), BridgeConstants.BridgeMaterial.fromByte(bridgeType), ndir, slope, northExit, -1, southExit, -1, 0, layer);
                        t.addBridgePart(bridgePart);
                        continue;
                    }
                    catch (NoSuchStructureException e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
        } else {
            int x;
            for (x = start.getX(); x <= end.getX(); ++x) {
                for (int y = start.getY(); y <= end.getY(); ++y) {
                    try {
                        VolaTile t = Zones.getOrCreateTile(x, y, layer == 0);
                        performer.addStructureTile(t, (byte)1);
                        t.addBridge(performer.getStructure());
                        continue;
                    }
                    catch (NoSuchStructureException e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
            PlanBridgeMethods.finaliseBridge(performer, bridgeName);
            for (x = start.getX(); x <= end.getX(); ++x) {
                int northExit = -1;
                int eastExit = -1;
                int southExit = -1;
                int westExit = -1;
                if (x == start.getX()) {
                    westExit = start.getH() - (int)(Zones.getHeightForNode(x, start.getY(), layer) * 10.0f);
                }
                if (x == end.getX()) {
                    eastExit = end.getH() - (int)(Zones.getHeightForNode(x + 1, end.getY(), layer) * 10.0f);
                }
                int xx = x - start.getX();
                byte rdir = dir;
                if (parts[xx] == 65 || parts[xx] == 66) {
                    rdir = (byte)((dir + 4) % 8);
                }
                byte slope = (byte)(hts[xx + 1] - hts[xx]);
                for (int y = start.getY(); y <= end.getY(); ++y) {
                    byte ndir = rdir;
                    BridgeConstants.BridgeType bridgetype = PlanBridgeMethods.getBridgeType(dir, parts[xx], end.getY(), start.getY(), y, ndir);
                    if (!bridgetype.isAbutment() && !bridgetype.isBracing() && PlanBridgeMethods.onLeft(dir, end.getY(), start.getY(), y, ndir)) {
                        ndir = (byte)((ndir + 4) % 8);
                    }
                    try {
                        VolaTile t = Zones.getOrCreateTile(x, y, layer == 0);
                        DbBridgePart bridgePart = new DbBridgePart(bridgetype, x, y, hts[xx], 1.0f, performer.getStructure().getWurmId(), BridgeConstants.BridgeMaterial.fromByte(bridgeType), ndir, slope, -1, eastExit, -1, westExit, 0, layer);
                        t.addBridgePart(bridgePart);
                        continue;
                    }
                    catch (NoSuchStructureException e) {
                        logger.log(Level.WARNING, e.getMessage(), e);
                    }
                }
            }
        }
    }

    public static int[] calcHeights(Creature performer, byte dir, byte bridgeType, boolean arched, String bridge, int steepness, Point start, Point end) {
        int[] hts = new int[bridge.length() + 1];
        if (arched) {
            if (bridgeType == BridgeConstants.BridgeMaterial.ROPE.getCode()) {
                if (bridge.length() == 1) {
                    hts[0] = start.getH();
                    hts[hts.length - 1] = end.getH();
                } else {
                    int x;
                    int odd = bridge.length() % 2;
                    int lenp1 = bridge.length() + 1;
                    int lowBorder = lenp1 >>> 1;
                    int hiBorder = bridge.length() - lowBorder;
                    float totalSag = (float)steepness / 100.0f;
                    float sagDistance = (float)lenp1 * 4.0f * totalSag;
                    double scaleCosh = 5.0E-5;
                    double scaleFactor = (double)sagDistance / 5.0E-5;
                    int htDiff = end.getH() - start.getH();
                    float htd = (float)htDiff / 10.0f;
                    int[] tempBorders = new int[lenp1];
                    int[] borders = new int[lenp1];
                    float[] scale = new float[lenp1];
                    double[] floatSag = new double[lenp1];
                    float[] tempSign = new float[lenp1];
                    float[] tempAdjust = new float[lenp1];
                    float[] adjust = new float[lenp1];
                    float[] slopeSag = new float[lenp1];
                    int[] dirtSag = new int[lenp1];
                    int[] oppSag = new int[lenp1];
                    for (x = 0; x < lenp1; ++x) {
                        tempBorders[x] = lowBorder - x;
                    }
                    for (x = 0; x < lenp1; ++x) {
                        borders[x] = tempBorders[x] > 0 ? tempBorders[x] : tempBorders[x] - odd;
                    }
                    for (x = 0; x < lenp1; ++x) {
                        scale[x] = borders[x] >= 0 ? (float)borders[x] / (float)lowBorder : (float)(-borders[x]) / (float)(hiBorder + odd);
                    }
                    for (x = 0; x < lenp1; ++x) {
                        floatSag[x] = scaleFactor * Math.cosh(scale[x] / 100.0f) - scaleFactor - (double)sagDistance;
                    }
                    for (x = 0; x < lenp1; ++x) {
                        tempSign[x] = Math.signum(borders[x]);
                    }
                    for (x = 0; x < lenp1; ++x) {
                        tempAdjust[x] = (tempSign[x] * scale[x] * htd - htd) / 2.0f;
                    }
                    for (x = 0; x < lenp1; ++x) {
                        adjust[x] = Math.abs(tempAdjust[x]);
                    }
                    for (x = 0; x < lenp1; ++x) {
                        slopeSag[x] = (float)(floatSag[x] + (double)adjust[x]);
                    }
                    for (x = 0; x < lenp1; ++x) {
                        dirtSag[x] = (int)(slopeSag[x] * 10.0f);
                    }
                    for (x = 0; x < lenp1; ++x) {
                        oppSag[x] = dirtSag[bridge.length() - x];
                    }
                    for (x = 0; x < lenp1; ++x) {
                        hts[x] = htDiff >= 0 ? start.getH() + dirtSag[x] : end.getH() + oppSag[x];
                    }
                    hts[0] = start.getH();
                    hts[hts.length - 1] = end.getH();
                    PlanBridgeMethods.outputheights(performer, "tBorders", tempBorders);
                    PlanBridgeMethods.outputheights(performer, "borders", borders);
                    PlanBridgeMethods.outputheights(performer, "scale", scale);
                    PlanBridgeMethods.outputheights(performer, "fSag", floatSag);
                    PlanBridgeMethods.outputheights(performer, "tSign", tempSign);
                    PlanBridgeMethods.outputheights(performer, "tAdjust", tempAdjust);
                    PlanBridgeMethods.outputheights(performer, "adjust", adjust);
                    PlanBridgeMethods.outputheights(performer, "slopeSag", slopeSag);
                    PlanBridgeMethods.outputheights(performer, "dirtSag", dirtSag);
                    PlanBridgeMethods.outputheights(performer, "oppSag", oppSag);
                }
                PlanBridgeMethods.outputheights(performer, "Hts", hts);
                return hts;
            }
            return PlanBridgeMethods.calcArch(performer, steepness, bridge.length(), start, end);
        }
        float slope = (float)(start.getH() - end.getH()) / (float)bridge.length();
        int ht = end.getH();
        float sd = 0.0f;
        if (dir == 0) {
            for (int y = start.getY(); y <= end.getY(); ++y) {
                hts[y - start.getY()] = ht = (int)((float)start.getH() - sd);
                sd += slope;
            }
            hts[hts.length - 1] = end.getH();
        } else {
            for (int x = start.getX(); x <= end.getX(); ++x) {
                hts[x - start.getX()] = ht = (int)((float)start.getH() - sd);
                sd += slope;
            }
            hts[hts.length - 1] = end.getH();
        }
        return hts;
    }

    public static int[] calcArch(Creature performer, int maxSlope, int len, Point start, Point end) {
        int lenn;
        int i;
        if (len == 1) {
            int[] hts = new int[]{end.getH(), start.getH()};
            return hts;
        }
        float factor = (float)maxSlope / 20.0f;
        int odd = len % 2;
        int middle = 0;
        int closest = 9999;
        int mindiff = 9999;
        StringBuilder bufL = new StringBuilder();
        StringBuilder bufR = new StringBuilder();
        StringBuilder bufS = new StringBuilder();
        StringBuilder bufE = new StringBuilder();
        StringBuilder bufD = new StringBuilder();
        int[] ahtL = new int[len + 2];
        int[] ahtR = new int[len + 2];
        int[] ahtS = new int[len + 2];
        int[] ahtE = new int[len + 2];
        for (i = 0; i <= len; ++i) {
            ahtL[i] = (int)((float)highest[i * 2] * factor);
            ahtS[i] = start.getH() + ahtL[i];
            ahtR[i] = (int)((float)highest[(len - i) * 2] * factor);
            ahtE[i] = end.getH() + ahtR[i];
        }
        ahtL[len + 1] = (int)((float)highest[(len + 1) * 2] * factor);
        ahtS[len + 1] = start.getH() + ahtL[len + 1];
        for (i = 0; i <= len; ++i) {
            int diff = ahtE[i] - ahtS[i + odd];
            if (performer.getPower() > 1 && DEBUG) {
                PlanBridgeMethods.addTo(bufL, "Left", ahtL[i]);
                PlanBridgeMethods.addTo(bufR, "Rght", ahtR[i]);
                PlanBridgeMethods.addTo(bufS, "Strt", ahtS[i]);
                PlanBridgeMethods.addTo(bufE, "End ", ahtE[i]);
                PlanBridgeMethods.addTo(bufD, "Diff", diff);
            }
            if (Math.abs(diff) >= mindiff) continue;
            mindiff = Math.abs(diff);
            closest = diff;
            middle = i;
        }
        if (performer.getPower() > 1 && DEBUG) {
            performer.getCommunicator().sendNormalServerMessage(bufL.toString() + ")");
            performer.getCommunicator().sendNormalServerMessage(bufR.toString() + ")");
            performer.getCommunicator().sendNormalServerMessage(bufS.toString() + ")");
            performer.getCommunicator().sendNormalServerMessage(bufE.toString() + ")");
            performer.getCommunicator().sendNormalServerMessage(bufD.toString() + ")");
        }
        if ((lenn = len - middle - odd) < 0) {
            lenn = 0;
        }
        if (performer.getPower() > 1 && DEBUG) {
            performer.getCommunicator().sendNormalServerMessage("(len:" + len + " middle:" + middle + "," + lenn + " s:" + start.getH() + " e:" + end.getH() + ")");
        }
        float slopeLeft = 0.0f;
        float slopeRight = 0.0f;
        if (closest < 0 && middle > 0) {
            slopeLeft = (float)mindiff / (float)middle;
        }
        if (closest > 0 && lenn > 0) {
            slopeRight = (float)closest / (float)lenn;
        }
        if (performer.getPower() > 1 && DEBUG) {
            performer.getCommunicator().sendNormalServerMessage("(middiff:" + closest + " L:" + Float.toString(slopeLeft) + " R:" + Float.toString(slopeRight) + ")");
        }
        int[] hts = new int[len + 1];
        float slopeLeftAdjustment = 0.0f;
        int[] shts = heights[middle];
        PlanBridgeMethods.outputheights(performer, "shts", shts);
        for (int i2 = 0; i2 < middle; ++i2) {
            hts[i2 + 1] = (int)((float)start.getH() + ((float)shts[i2] - slopeLeftAdjustment) * factor);
            slopeLeftAdjustment += slopeLeft;
        }
        int[] ehts = heights[lenn];
        float slopeRightAdjustment = 0.0f;
        PlanBridgeMethods.outputheights(performer, "ehts", ehts);
        for (int i3 = 0; i3 < lenn; ++i3) {
            hts[len - i3 - 1] = (int)((float)end.getH() + ((float)ehts[i3] - slopeRightAdjustment) * factor);
            slopeRightAdjustment += slopeRight;
        }
        PlanBridgeMethods.outputheights(performer, "Hts", hts);
        hts[0] = start.getH();
        hts[hts.length - 1] = end.getH();
        PlanBridgeMethods.outputheights(performer, "Hts", hts);
        return hts;
    }

    private static void outputheights(Creature performer, String name, int[] hts) {
        if (performer.getPower() > 1 && DEBUG) {
            StringBuilder buf = new StringBuilder();
            for (int ht : hts) {
                PlanBridgeMethods.addTo(buf, name, ht);
            }
            performer.getCommunicator().sendNormalServerMessage(buf.toString() + ")");
        }
    }

    private static void addTo(StringBuilder buf, String name, int value) {
        if (buf.length() > 0) {
            buf.append(", ");
        } else {
            buf.append("(" + name + ":");
        }
        buf.append(value);
    }

    private static void outputheights(Creature performer, String name, double[] hts) {
        if (performer.getPower() > 1 && DEBUG) {
            StringBuilder buf = new StringBuilder();
            for (double ht : hts) {
                PlanBridgeMethods.addTo(buf, name, ht);
            }
            performer.getCommunicator().sendNormalServerMessage(buf.toString() + ")");
        }
    }

    private static void addTo(StringBuilder buf, String name, double value) {
        if (buf.length() > 0) {
            buf.append(", ");
        } else {
            buf.append("(" + name + ":");
        }
        buf.append(value);
    }

    private static void outputheights(Creature performer, String name, float[] hts) {
        if (performer.getPower() > 1 && DEBUG) {
            StringBuilder buf = new StringBuilder();
            for (float ht : hts) {
                PlanBridgeMethods.addTo(buf, name, ht);
            }
            performer.getCommunicator().sendNormalServerMessage(buf.toString() + ")");
        }
    }

    private static void addTo(StringBuilder buf, String name, float value) {
        if (buf.length() > 0) {
            buf.append(", ");
        } else {
            buf.append("(" + name + ":");
        }
        buf.append(value);
    }

    private static boolean finaliseBridge(Creature performer, String bridgeName) {
        try {
            Structure structure = performer.getStructure();
            structure.makeFinal(performer, bridgeName);
            performer.getStatus().setBuildingId(structure.getWurmId());
            return true;
        }
        catch (NoSuchZoneException e1) {
            logger.log(Level.WARNING, e1.getMessage(), e1);
        }
        catch (NoSuchStructureException e1) {
            logger.log(Level.WARNING, e1.getMessage(), e1);
        }
        catch (IOException e1) {
            logger.log(Level.WARNING, e1.getMessage(), e1);
        }
        return false;
    }

    private static BridgeConstants.BridgeType getBridgeType(byte dir, byte part, int left, int right, int pos, byte direction) {
        switch (part) {
            case 65: 
            case 97: {
                if (left == right) {
                    return BridgeConstants.BridgeType.ABUTMENT_NARROW;
                }
                if (pos == left) {
                    if (dir == direction) {
                        return BridgeConstants.BridgeType.ABUTMENT_LEFT;
                    }
                    return BridgeConstants.BridgeType.ABUTMENT_RIGHT;
                }
                if (pos == right) {
                    if (dir == direction) {
                        return BridgeConstants.BridgeType.ABUTMENT_RIGHT;
                    }
                    return BridgeConstants.BridgeType.ABUTMENT_LEFT;
                }
                return BridgeConstants.BridgeType.ABUTMENT_CENTER;
            }
            case 66: 
            case 98: {
                if (left == right) {
                    return BridgeConstants.BridgeType.BRACING_NARROW;
                }
                if (pos == left) {
                    if (dir == direction) {
                        return BridgeConstants.BridgeType.BRACING_LEFT;
                    }
                    return BridgeConstants.BridgeType.BRACING_RIGHT;
                }
                if (pos == right) {
                    if (dir == direction) {
                        return BridgeConstants.BridgeType.BRACING_RIGHT;
                    }
                    return BridgeConstants.BridgeType.BRACING_LEFT;
                }
                return BridgeConstants.BridgeType.BRACING_CENTER;
            }
            case 67: {
                if (left == right) {
                    return BridgeConstants.BridgeType.CROWN_NARROW;
                }
                if (pos == left || pos == right) {
                    return BridgeConstants.BridgeType.CROWN_SIDE;
                }
                return BridgeConstants.BridgeType.CROWN_CENTER;
            }
            case 68: {
                if (left == right) {
                    return BridgeConstants.BridgeType.DOUBLE_NARROW;
                }
                if (pos == left || pos == right) {
                    return BridgeConstants.BridgeType.DOUBLE_SIDE;
                }
                return BridgeConstants.BridgeType.DOUBLE_CENTER;
            }
            case 69: {
                if (left == right) {
                    return BridgeConstants.BridgeType.END_NARROW;
                }
                if (pos == left || pos == right) {
                    return BridgeConstants.BridgeType.END_SIDE;
                }
                return BridgeConstants.BridgeType.END_CENTER;
            }
            case 83: {
                if (left == right) {
                    return BridgeConstants.BridgeType.SUPPORT_NARROW;
                }
                if (pos == left || pos == right) {
                    return BridgeConstants.BridgeType.SUPPORT_SIDE;
                }
                return BridgeConstants.BridgeType.SUPPORT_CENTER;
            }
        }
        if (left == right) {
            return BridgeConstants.BridgeType.FLOATING_NARROW;
        }
        if (pos == left || pos == right) {
            return BridgeConstants.BridgeType.FLOATING_SIDE;
        }
        return BridgeConstants.BridgeType.FLOATING_CENTER;
    }

    private static boolean onLeft(byte dir, int left, int right, int pos, byte direction) {
        if (pos == left) {
            return dir == direction;
        }
        return false;
    }

    public static String[] isBuildingOk(byte bridgeType, byte dir, boolean onSurface, Point start, int startFloorlevel, Point end, int endFloorlevel) {
        if (dir == 0) {
            String nos;
            VolaTile checkedTile;
            int x;
            int y = start.getY();
            for (x = start.getX(); x <= end.getX(); ++x) {
                checkedTile = Zones.getTileOrNull(x, y - 1, onSurface);
                if (checkedTile == null) continue;
                if (PlanBridgeMethods.hasNoDoor(bridgeType, checkedTile, startFloorlevel, x, y, x + 1, y)) {
                    return new String[]{"N:No Door", "North end of bridge plan requires a door."};
                }
                nos = PlanBridgeMethods.noSupport(bridgeType, checkedTile, startFloorlevel, x, y, x + 1, y);
                if (nos.length() <= 0) continue;
                return new String[]{"N:Too Weak", "North end " + nos};
            }
            y = end.getY() + 1;
            for (x = start.getX(); x <= end.getX(); ++x) {
                checkedTile = Zones.getTileOrNull(x, y, onSurface);
                if (checkedTile == null) continue;
                if (PlanBridgeMethods.hasNoDoor(bridgeType, checkedTile, endFloorlevel, x, y, x + 1, y)) {
                    return new String[]{"S:No Door", "South end of bridge plan requires a door."};
                }
                nos = PlanBridgeMethods.noSupport(bridgeType, checkedTile, endFloorlevel, x, y, x + 1, y);
                if (nos.length() <= 0) continue;
                return new String[]{"S:Too Weak", "South end " + nos};
            }
        } else {
            String nos;
            VolaTile checkedTile;
            int y;
            int x = start.getX();
            for (y = start.getY(); y <= end.getY(); ++y) {
                checkedTile = Zones.getTileOrNull(x - 1, y, onSurface);
                if (checkedTile == null) continue;
                if (PlanBridgeMethods.hasNoDoor(bridgeType, checkedTile, startFloorlevel, x, y, x, y + 1)) {
                    return new String[]{"W:No Door", "West end of bridge plan requires a door."};
                }
                nos = PlanBridgeMethods.noSupport(bridgeType, checkedTile, startFloorlevel, x, y, x, y + 1);
                if (nos.length() <= 0) continue;
                return new String[]{"W:Too Weak", "West end " + nos};
            }
            x = end.getX() + 1;
            for (y = start.getY(); y <= end.getY(); ++y) {
                checkedTile = Zones.getTileOrNull(x, y, onSurface);
                if (checkedTile == null) continue;
                if (PlanBridgeMethods.hasNoDoor(bridgeType, checkedTile, endFloorlevel, x, y, x, y + 1)) {
                    return new String[]{"E:No Door", "East end of bridge plan requires a door."};
                }
                nos = PlanBridgeMethods.noSupport(bridgeType, checkedTile, endFloorlevel, x, y, x, y + 1);
                if (nos.length() <= 0) continue;
                return new String[]{"E:Too Weak", "East end " + nos};
            }
        }
        return new String[]{"", ""};
    }

    private static boolean hasNoDoor(byte bridgeType, VolaTile checkedTile, int floorlevel, int startX, int startY, int endX, int endY) {
        Wall wall = PlanBridgeMethods.getWall(checkedTile, floorlevel, startX, startY, endX, endY);
        return wall != null && !wall.isDoor();
    }

    private static String noSupport(byte bridgeType, VolaTile checkedTile, int floorlevel, int startX, int startY, int endX, int endY) {
        if (bridgeType != BridgeConstants.BridgeMaterial.ROPE.getCode() && floorlevel >= 1) {
            Wall wall = PlanBridgeMethods.getWall(checkedTile, floorlevel - 1, startX, startY, endX, endY);
            if (wall == null || wall.getType() != StructureTypeEnum.SOLID) {
                return "needs a solid wall just below planned connection";
            }
            if (bridgeType != BridgeConstants.BridgeMaterial.WOOD.getCode() && !wall.canSupportStoneBridges()) {
                return "needs a solid stone wall just below planned connection";
            }
            if (!(bridgeType == BridgeConstants.BridgeMaterial.WOOD.getCode() || floorlevel < 2 || (wall = PlanBridgeMethods.getWall(checkedTile, floorlevel - 2, startX, startY, endX, endY)) != null && wall.getType() == StructureTypeEnum.SOLID && wall.canSupportStoneBridges())) {
                return "needs a solid stone wall two floors below planned connection";
            }
        }
        return "";
    }

    private static Wall getWall(VolaTile checkedTile, int floorlevel, int startX, int startY, int endX, int endY) {
        for (Wall wall : checkedTile.getWalls()) {
            if (wall.getFloorLevel() != floorlevel || wall.getStartX() != startX || wall.getStartY() != startY || wall.getEndX() != endX || wall.getEndY() != endY) continue;
            return wall;
        }
        return null;
    }
}

