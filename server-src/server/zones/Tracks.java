/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.zones;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.zones.Track;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class Tracks
implements TimeConstants,
MiscConstants {
    private static final long decayTimeRock = 1800000L;
    private static final long decayTimeDirt = 10800000L;
    private static final long decayTimeGrass = 3600000L;
    private static final long decayTimeClay = 86400000L;
    private static final long decayTimeBush = 0x6DDD00L;
    private static final long decayTimeTree = 3600000L;
    private static final long decayTimeField = 10800000L;
    private static final long decayTimeSand = 10800000L;
    private static final int MAX_TRACKS = 1000;
    private final ConcurrentLinkedDeque<Track> tracks = new ConcurrentLinkedDeque();

    Tracks() {
    }

    final void addTrack(Track track) {
        if (this.tracks.size() > 1000) {
            this.tracks.removeFirst();
        }
        this.tracks.addLast(track);
    }

    final void decay() {
        Iterator<Track> it = this.tracks.iterator();
        while (it.hasNext()) {
            Track track = it.next();
            long decayTime = 1800000L;
            byte type = Tiles.decodeType(track.getTile());
            Tiles.Tile theTile = Tiles.getTile(type);
            if (type == Tiles.Tile.TILE_GRASS.id || type == Tiles.Tile.TILE_MYCELIUM.id || type == Tiles.Tile.TILE_STEPPE.id) {
                decayTime = 3600000L;
            } else if (type == Tiles.Tile.TILE_KELP.id) {
                decayTime = 3600000L;
            } else if (type == Tiles.Tile.TILE_REED.id) {
                decayTime = 3600000L;
            } else if (type == Tiles.Tile.TILE_LAWN.id) {
                decayTime = 3600000L;
            } else if (theTile.isTree()) {
                decayTime = 3600000L;
            } else if (theTile.isBush()) {
                decayTime = 0x6DDD00L;
            } else if (type == Tiles.Tile.TILE_DIRT.id) {
                decayTime = 10800000L;
            } else if (type == Tiles.Tile.TILE_FIELD.id || type == Tiles.Tile.TILE_FIELD2.id) {
                decayTime = 10800000L;
            } else if (type == Tiles.Tile.TILE_ROCK.id) {
                decayTime = 1800000L;
            } else if (type == Tiles.Tile.TILE_SAND.id) {
                decayTime = 10800000L;
            } else if (type == Tiles.Tile.TILE_CLAY.id || type == Tiles.Tile.TILE_MARSH.id || type == Tiles.Tile.TILE_PEAT.id) {
                decayTime = 86400000L;
            }
            if (System.currentTimeMillis() - track.getTime() <= decayTime) continue;
            it.remove();
        }
    }

    final Track[] getTracksFor(int tilex, int tiley) {
        HashSet<Track> matches = new HashSet<Track>();
        for (Track track : this.tracks) {
            if (track.getTileX() != tilex || track.getTileY() != tiley) continue;
            matches.add(track);
        }
        Track[] toReturn = new Track[matches.size()];
        return matches.toArray(toReturn);
    }

    final Track[] getTracksFor(int tilex, int tiley, int dist) {
        HashMap<String, Track> matches = new HashMap<String, Track>();
        boolean add = false;
        int tx = tilex;
        int ty = tiley;
        int dir = 0;
        int maxy = tiley + dist;
        int maxx = tilex + dist;
        int minx = tilex - dist;
        int miny = tiley - dist;
        for (Track track : this.tracks) {
            tx = track.getTileX();
            ty = track.getTileY();
            dir = track.getDirection();
            if (tx == minx && ty == miny) {
                if (dir >= 5 || dir <= 1) {
                    add = true;
                }
            } else if (tx == maxx && ty == miny) {
                if (dir >= 7 || dir <= 3) {
                    add = true;
                }
            } else if (tx == maxx && ty == maxy) {
                if (dir >= 1 && dir <= 5) {
                    add = true;
                }
            } else if (tx == minx && ty == maxy) {
                if (dir >= 3 && dir <= 7) {
                    add = true;
                }
            } else if (ty == miny && tx > minx && tx < maxx) {
                if (dir >= 7 || dir <= 1) {
                    add = true;
                }
            } else if (tx == maxx && ty > miny && ty < maxy) {
                if (dir >= 1 && dir <= 3) {
                    add = true;
                }
            } else if (ty == miny && tx > minx && tx < maxx) {
                if (dir >= 3 && dir <= 5) {
                    add = true;
                }
            } else if (tx == minx && ty > miny && ty < maxy && dir >= 5 && dir <= 7) {
                add = true;
            }
            if (add) {
                Track t = (Track)matches.get(track.getCreatureName());
                if (t != null) {
                    if (t.getTime() > track.getTime()) {
                        matches.put(track.getCreatureName(), track);
                    }
                } else {
                    matches.put(track.getCreatureName(), track);
                }
            }
            add = false;
        }
        Track[] toReturn = new Track[matches.size()];
        return matches.values().toArray(toReturn);
    }

    public final int getNumberOfTracks() {
        if (this.tracks != null) {
            return this.tracks.size();
        }
        return 0;
    }
}

