/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

import com.wurmonline.mesh.MeshIO;
import com.wurmonline.server.Constants;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

final class MeshSaver
implements Runnable {
    private static final Logger logger = Logger.getLogger(MeshSaver.class.getName());
    private final MeshIO iMapLayer;
    private final String iMapLayerName;
    private final int iNumberOfRowsPerCall;

    MeshSaver(MeshIO aMapLayerToSave, String aMapLayerName, int aNumberOfRowsPerCall) {
        this.iMapLayer = aMapLayerToSave;
        this.iMapLayerName = aMapLayerName;
        this.iNumberOfRowsPerCall = aNumberOfRowsPerCall;
        logger.info("Created MeshSaver for map layer: '" + this.iMapLayerName + "', " + aMapLayerToSave + ", rowsPerCall: " + aNumberOfRowsPerCall);
    }

    MeshSaver(MeshIO aMapLayerToSave, String aMapLayerName) {
        this(aMapLayerToSave, aMapLayerName, 1);
    }

    @Override
    public void run() {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("Running MeshSaver for calling MeshIO.saveDirtyRow() for '" + this.iMapLayerName + "', " + this.iMapLayer + ", rowsPerCall: " + this.iNumberOfRowsPerCall);
        }
        try {
            long now = System.nanoTime();
            int numberOfRowsSaved = this.iNumberOfRowsPerCall;
            if (this.iNumberOfRowsPerCall <= 0) {
                numberOfRowsSaved = this.iMapLayer.saveAllDirtyRows();
            } else if (this.iNumberOfRowsPerCall > this.iMapLayer.getSize()) {
                this.iMapLayer.saveAll();
            } else {
                for (int i = 0; i < this.iNumberOfRowsPerCall && !this.iMapLayer.saveNextDirtyRow(); ++i) {
                }
            }
            float lElapsedTime = (float)(System.nanoTime() - now) / 1000000.0f;
            if (lElapsedTime > (float)Constants.lagThreshold || logger.isLoggable(Level.FINER)) {
                logger.info("Finished saving " + numberOfRowsSaved + " rows for '" + this.iMapLayerName + "', which took " + lElapsedTime + " millis.");
            }
        }
        catch (RuntimeException e) {
            logger.log(Level.WARNING, "Caught exception in MeshSaver while saving Mesh for '" + this.iMapLayerName + "' " + this.iMapLayer, e);
            throw e;
        }
        catch (IOException e) {
            logger.log(Level.WARNING, "Caught exception in MeshSaver while saving Mesh for '" + this.iMapLayerName + "' " + this.iMapLayer, e);
            throw new RuntimeException(e);
        }
    }
}

