/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.gui.folders;

import com.wurmonline.server.gui.folders.DistFolder;
import com.wurmonline.server.gui.folders.GameFolder;
import com.wurmonline.server.gui.folders.PresetFolder;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public enum Folders {
    INSTANCE;

    private static final Logger logger;
    private HashMap<String, GameFolder> gameFolders = new HashMap();
    private HashMap<String, PresetFolder> presets = new HashMap();
    private GameFolder current;
    private DistFolder dist;
    private Path distPath = Paths.get(System.getProperty("wurm.distRoot", "./dist"), new String[0]);
    private Path gamesPath = Paths.get(System.getProperty("wurm.gameFolderRoot", "."), new String[0]);
    private Path presetsPath = Paths.get(System.getProperty("wurm.presetsRoot", "./presets"), new String[0]);

    public static Folders getInstance() {
        return INSTANCE;
    }

    public static ArrayList<GameFolder> getGameFolders() {
        ArrayList<GameFolder> gameFolders = new ArrayList<GameFolder>();
        gameFolders.addAll(Folders.getInstance().gameFolders.values());
        return gameFolders;
    }

    @Nullable
    public static GameFolder getGameFolder(String folderName) {
        return Folders.getInstance().gameFolders.get(folderName);
    }

    public static boolean setCurrent(GameFolder gameFolder) {
        if (Folders.getInstance().current != null && !Folders.getInstance().current.setCurrent(false)) {
            return false;
        }
        Folders.getInstance().current = gameFolder;
        if (!gameFolder.setCurrent(true)) {
            return false;
        }
        logger.info("Current game folder: " + gameFolder.getName());
        return true;
    }

    public static void clear() {
        Folders.getInstance().gameFolders.clear();
        Folders.getInstance().current = null;
        logger.info("Game folders cleared.");
    }

    public static GameFolder getCurrent() {
        return Folders.getInstance().current;
    }

    public static boolean loadGames() {
        return Folders.loadGamesFrom(Folders.getInstance().gamesPath);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static boolean loadGamesFrom(Path parent) {
        if (!Folders.getInstance().gameFolders.isEmpty()) {
            Folders.getInstance().gameFolders = new HashMap();
        }
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(parent);){
            block21: {
                Iterator<Path> iterator = directoryStream.iterator();
                while (iterator.hasNext()) {
                    Path path = iterator.next();
                    GameFolder gameFolder = GameFolder.fromPath(path);
                    if (gameFolder == null) continue;
                    Folders.getInstance().gameFolders.put(gameFolder.getName(), gameFolder);
                    if (!gameFolder.isCurrent()) continue;
                    if (Folders.getInstance().current == null) {
                        Folders.getInstance().current = gameFolder;
                        continue;
                    }
                    if (gameFolder.setCurrent(false)) {
                        continue;
                    }
                    break block21;
                }
                return true;
            }
            boolean bl = false;
            return bl;
        }
        catch (IOException ex) {
            logger.warning("IOException while reading game folders");
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean loadDist() {
        Folders.getInstance().dist = DistFolder.fromPath(Folders.getInstance().distPath);
        return Folders.getInstance().dist != null;
    }

    public static DistFolder getDist() {
        if (Folders.getInstance().dist == null && !Folders.loadDist()) {
            logger.warning("Unable to load 'dist' folder, please run Steam validation");
            return new DistFolder(Folders.getInstance().distPath);
        }
        return Folders.getInstance().dist;
    }

    public static boolean loadPresets() {
        if (!Folders.getInstance().presets.isEmpty()) {
            Folders.getInstance().presets = new HashMap();
        }
        if (Folders.getInstance().dist == null && !Folders.loadDist()) {
            logger.warning("Unable to load 'dist' folder, please run Steam validation");
            return false;
        }
        if (!Folders.loadPresetsFrom(Folders.getInstance().dist.getPath())) {
            logger.warning("Unable to load presets from 'dist', please run Steam validation");
            return false;
        }
        if (!Files.exists(Folders.getInstance().presetsPath, new LinkOption[0])) {
            try {
                Files.createDirectory(Folders.getInstance().presetsPath, new FileAttribute[0]);
            }
            catch (IOException ex) {
                logger.warning("Could not create presets folder");
                return false;
            }
        }
        return Folders.loadPresetsFrom(Folders.getInstance().presetsPath);
    }

    private static boolean loadPresetsFrom(Path parent) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(parent);){
            for (Path path : directoryStream) {
                PresetFolder folder = PresetFolder.fromPath(path);
                if (folder == null) continue;
                Folders.getInstance().presets.put(folder.getName(), folder);
            }
        }
        catch (IOException ex) {
            logger.warning("IOException while reading game folders");
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public static Path getGamesPath() {
        return Folders.getInstance().gamesPath;
    }

    public static void addGame(GameFolder folder) {
        Folders.getInstance().gameFolders.put(folder.getName(), folder);
    }

    public static void removeGame(GameFolder folder) {
        Folders.getInstance().gameFolders.remove(folder.getName());
    }

    static {
        logger = Logger.getLogger(Folders.class.getName());
    }
}

