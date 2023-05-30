package com.wurmonline.server.gui;

import coffee.keenan.network.helpers.address.AddressHelper;
import coffee.keenan.network.helpers.port.Port;
import coffee.keenan.network.helpers.port.PortHelper;
import coffee.keenan.network.helpers.port.Protocol;
import coffee.keenan.network.validators.address.IP4Validator;
import coffee.keenan.network.wrappers.upnp.UPNPService;
import com.wurmonline.server.Constants;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.ServerDirInfo;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.ServerLauncher;
import com.wurmonline.server.ServerProperties;
import com.wurmonline.server.Servers;
import com.wurmonline.server.console.CommandReader;
import com.wurmonline.server.gui.folders.DistEntity;
import com.wurmonline.server.gui.folders.Folders;
import com.wurmonline.server.gui.folders.GameFolder;
import com.wurmonline.server.gui.folders.PresetFolder;
import com.wurmonline.server.gui.propertysheet.PlayerPropertySheet;
import com.wurmonline.server.gui.propertysheet.ServerPropertySheet;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.steam.SteamHandler;
import com.wurmonline.shared.constants.SteamVersion;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sun.management.VMManagement;

public final class WurmServerGuiController {
   private static final Logger logger = Logger.getLogger(WurmServerGuiController.class.getName());
   private static ServerPropertySheet lServerPropertySheet;
   private static ServerPropertySheet localServerPropertySheet;
   private static PlayerPropertySheet playerPropertySheet;
   private ConcurrentHashMap<Integer, ServerEntry> servers = new ConcurrentHashMap<>();
   private ConcurrentHashMap<Integer, ServerEntry> selectionBoxServers = new ConcurrentHashMap<>();
   public static String adminPassword = "";
   private ServerEntry nullServer;
   private ServerLauncher launcher;
   private Stage primaryStage;
   @FXML
   Button updateGameBtn;
   @FXML
   Label updateRequiredLabel;
   @FXML
   GridPane gameControls;
   @FXML
   GridPane runningControls;
   @FXML
   Tab localServerTab;
   @FXML
   Tab serverNeighborTab;
   @FXML
   Tab serverTravelTab;
   @FXML
   ScrollPane tab3ScrollPane1;
   @FXML
   AnchorPane tab3ContentPane1;
   @FXML
   Tab playersTab;
   @FXML
   TabPane rootTabPane;
   @FXML
   private AnchorPane tab1ContentPane;
   @FXML
   private AnchorPane tab2ContentPane;
   @FXML
   private AnchorPane tab3ContentPane;
   @FXML
   private ScrollPane tab1ScrollPane;
   @FXML
   private ScrollPane tab2ScrollPane;
   @FXML
   private ScrollPane tab3ScrollPane;
   @FXML
   private AnchorPane tab5ContentPane;
   @FXML
   private ScrollPane tab5ScrollPane;
   @FXML
   private Button saveServerButton;
   @FXML
   private Button saveServerButtonLocal;
   @FXML
   private Text localServerText;
   @FXML
   private Button startBtn;
   @FXML
   private Button startOfflineBtn;
   @FXML
   private Button deleteServerBtn;
   @FXML
   private Button deleteBtn;
   @FXML
   private CheckBox chkAutoNetwork;
   private static final Map<GuiCommandLineArgument, WurmServerGuiController.ArgumentApplication> ARGUMENT_APPLICATIONS;
   @FXML
   private Button shutdownButton;
   @FXML
   private Button btnCopy;
   @FXML
   private Button savePlayerBtn;
   @FXML
   private Button btnRename;
   @FXML
   private ComboBox<String> selectServerBox;
   @FXML
   private ComboBox<String> selectNeighbourBoxNorth;
   @FXML
   private ComboBox<String> selectNeighbourBoxSouth;
   @FXML
   private ComboBox<String> selectNeighbourBoxWest;
   @FXML
   private ComboBox<String> selectNeighbourBoxEast;
   @FXML
   private ComboBox<String> databaseComboBox;
   @FXML
   private TextField renameTxtfield;
   @FXML
   private TextField secondsTxtField;
   @FXML
   private TextField reasonTxtfield;
   @FXML
   private TextField copyTextField;
   @FXML
   private ComboBox<String> selectPlayerComboBox;
   @FXML
   private Button saveNeighboursButton;
   boolean resettingPlayers = false;
   boolean rebuilding = false;

   @FXML
   void startBtnClicked(ActionEvent event) {
      this.startGame(false);
   }

   @FXML
   public void startOfflineBtnClicked() {
      this.startGame(true);
   }

   @FXML
   public void updateGameBtnClicked() {
      this.updateGameBtn.setText("Updating...");
      this.updateGameBtn.setDisable(true);
      DbConnector.performMigrations();
      this.updateGameBtn.setText("Updated");
      this.updateRequiredLabel.setVisible(false);
      this.startBtn.setDisable(false);
      this.startOfflineBtn.setDisable(false);
      this.chkAutoNetwork.setVisible(true);
      this.setDisableTabs(false);
   }

   private static boolean applyArguments() {
      boolean hasChanged = false;

      for(GuiCommandLineArgument commandLineArgument : ARGUMENT_APPLICATIONS.keySet()) {
         String argumentString = commandLineArgument.getArgumentString();
         if (Servers.arguments.hasOption(argumentString)) {
            ARGUMENT_APPLICATIONS.get(commandLineArgument).applyArgument(Servers.arguments.getOptionValue(argumentString));
            hasChanged = true;
         }
      }

      return hasChanged;
   }

   static void initServer(String dbName) {
      Servers.loadAllServers(true);

      for(ServerEntry entry : Servers.getAllServers()) {
         if (entry.name.equals(dbName)) {
            Servers.localServer = entry;
            break;
         }
      }

      if (Servers.localServer != null && Servers.arguments != null) {
         logger.log(Level.INFO, "Setting server settings from command line");
         if (applyArguments()) {
            Servers.localServer.saveNewGui(Servers.localServer.id);
         }
      }
   }

   @FXML
   void deleteBtnClicked(ActionEvent event) {
      GameFolder orig = Folders.getCurrent();
      if (orig != null) {
         Alert alert = new Alert(AlertType.CONFIRMATION);
         alert.setTitle("Delete Game");
         alert.setHeaderText("This will delete the game " + orig.getName());
         alert.setContentText("Are you really sure you want to do this?");
         Optional<ButtonType> result = alert.showAndWait();
         if (result.isPresent() && result.get() == ButtonType.OK) {
            DbConnector.closeAll();
            if (!orig.delete()) {
               this.showErrorDialog("Delete", "Unable to delete the game", false);
               return;
            }

            Folders.removeGame(orig);
            ArrayList<GameFolder> games = Folders.getGameFolders();
            if (games.size() > 0) {
               this.setCurrent(games.get(0));
            }

            this.buildDatabaseComboBox(true);
         }
      }
   }

   @FXML
   void shutdownButtonClicked(ActionEvent event) {
      if (this.launcher != null) {
         this.launcher.getServer().startShutdown(Integer.valueOf(this.secondsTxtField.getText()), this.reasonTxtfield.getText());
         System.out
            .println("The server is shutting down in " + Integer.valueOf(this.secondsTxtField.getText()) + " seconds. " + this.reasonTxtfield.getText());
      } else {
         this.showErrorDialog("Could not shut down", "The system found no server to shut down.", false);
      }
   }

   @FXML
   void btnCopyClicked(ActionEvent event) {
      if (Folders.getCurrent() == null) {
         this.showErrorDialog("Copy Game", "Could not copy game, no game selected.", false);
      } else {
         GameFolder copyTo = new GameFolder(Folders.getGamesPath().resolve(this.copyTextField.getText()));
         if (!copyTo.create()) {
            this.showErrorDialog("Copy Game", "Could not create new game folder", false);
         } else {
            if (!Folders.getCurrent().copyTo(copyTo.getPath())) {
               this.showErrorDialog("Copy Game", "Failed to copy game to new folder. Please see logs.", false);
            } else {
               Folders.addGame(copyTo);
            }

            this.buildDatabaseComboBox(false);
         }
      }
   }

   private void showErrorDialog(String headerText, String contentText, boolean isResizable) {
      Alert alert2 = new Alert(AlertType.ERROR);
      alert2.initStyle(StageStyle.UTILITY);
      alert2.setTitle("Error");
      alert2.setHeaderText(headerText);
      alert2.setContentText(contentText);
      alert2.setResizable(isResizable);
      alert2.showAndWait();
   }

   @FXML
   void playerTabSelected(Event event) {
      this.buildSelectPlayerBox(true);
      this.buildPlayerPropertyTab(true);
   }

   @FXML
   void savePlayerBtnClicked(ActionEvent event) {
      logger.info("savePlayerlButtonClicked " + event);
      String error = playerPropertySheet.save();
      if (error != null && error.length() > 0 && error.equalsIgnoreCase("ok")) {
         Alert alert2 = new Alert(AlertType.INFORMATION);
         alert2.setTitle("Player saved");
         alert2.setHeaderText("Updated accordingly");
         alert2.setContentText("The player was saved.");
         alert2.showAndWait();
         if (this.launcher != null && this.launcher.wasStarted() && this.launcher.getServer() != null && playerPropertySheet.getCurrentData() != null) {
            PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(playerPropertySheet.getCurrentData().getWurmid());
            if (info != null) {
               info.loaded = false;

               try {
                  info.load();
               } catch (IOException var6) {
                  logger.log(Level.WARNING, "Failed to loadGames player info for " + info.getName(), (Throwable)var6);
               }
            }
         }
      } else if (error != null && error.length() > 0) {
         Alert alert2 = new Alert(AlertType.ERROR);
         alert2.setTitle("Error Dialog");
         alert2.setHeaderText("Could not save player");
         alert2.setContentText("There was an error trying to save the player. This error was reported: " + error + ".");
         alert2.showAndWait();
      } else {
         this.savePlayerBtn.setDisable(true);
      }
   }

   public static final void setCurrentFolder(GameFolder folder) {
      if (Folders.getCurrent() == null || Folders.getCurrent() != folder) {
         Folders.setCurrent(folder);
      }

      ServerDirInfo.setPath(folder.getPath());
   }

   public final void setCurrent(GameFolder folder) {
      setCurrentFolder(folder);
      this.setStageTitle("(" + folder.getName() + ") Wurm Unlimited Server");
   }

   @FXML
   void btnRenameClicked(ActionEvent event) {
      GameFolder orig = Folders.getCurrent();
      if (orig != null && orig.exists()) {
         GameFolder dest = new GameFolder(Folders.getGamesPath().resolve(this.renameTxtfield.getText()));
         if (dest.exists()) {
            this.showErrorDialog("Rename", "A game with that name already exists", false);
         } else if (!dest.create()) {
            this.showErrorDialog("Rename", "Unable to rename the folder: could not create destination folder", false);
         } else {
            DbConnector.closeAll();
            if (orig.copyTo(dest.getPath())) {
               if (!orig.delete()) {
                  this.showErrorDialog("Rename", "Could not delete original", false);
               }

               if (!dest.getError().isEmpty()) {
                  this.showErrorDialog("Rename", "Incomplete move: " + dest.getError(), false);
                  dest.delete();
               } else {
                  Folders.addGame(dest);
                  this.setCurrent(dest);
                  Folders.removeGame(orig);
                  this.buildDatabaseComboBox(false);
               }
            } else {
               this.showErrorDialog("Rename", "Could create destination", false);
               this.setCurrent(orig);
            }
         }
      } else {
         this.showErrorDialog("Rename", "No game selected or game does not exist", false);
      }
   }

   @FXML
   void secondsTextFieldChanged(ActionEvent event) {
      logger.info("secondsTextFieldChanged " + event);
   }

   @FXML
   void reasonTextFieldChanged(ActionEvent event) {
      logger.info("reasonTextFieldChanged " + event);
   }

   @FXML
   void renameTxtfieldChanged(ActionEvent event) {
      logger.info("renameTxtfieldChanged " + event);
      this.btnRename.setDisable(false);
   }

   @FXML
   void copyTextFieldChanged(ActionEvent event) {
      logger.info("copyTextFieldChanged " + event);
   }

   public final int getPid() {
      try {
         RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
         Field jvm = runtime.getClass().getDeclaredField("jvm");
         jvm.setAccessible(true);
         VMManagement mgmt = (VMManagement)jvm.get(runtime);
         Method pid_method = mgmt.getClass().getDeclaredMethod("getProcessId");
         pid_method.setAccessible(true);
         return pid_method.invoke(mgmt);
      } catch (Exception var5) {
         return 0;
      }
   }

   @FXML
   void selectPlayerComboBoxChanged(ActionEvent event) {
      this.savePlayerBtn.setDisable(false);
   }

   @FXML
   void selectNeighbourBoxSouthChanged(ActionEvent event) {
      this.saveNeighboursButton.setDisable(false);
   }

   @FXML
   void selectNeighbourBoxEastChanged(ActionEvent event) {
      this.saveNeighboursButton.setDisable(false);
   }

   @FXML
   void selectNeighbourBoxNorthChanged(ActionEvent event) {
      this.saveNeighboursButton.setDisable(false);
   }

   @FXML
   void selectNeighbourBoxWestChanged(ActionEvent event) {
      this.saveNeighboursButton.setDisable(false);
   }

   @FXML
   void saveNeighboursButtonClicked(ActionEvent event) {
      int index = this.selectNeighbourBoxWest.getSelectionModel().getSelectedIndex();
      if (index > 0) {
         ServerEntry entry = this.selectionBoxServers.get(index);
         if (Servers.localServer.serverWest != entry) {
            Servers.addServerNeighbour(entry.id, "WEST");
         }
      } else if (Servers.localServer.serverWest != null) {
         Servers.deleteServerNeighbour("WEST");
      }

      index = this.selectNeighbourBoxNorth.getSelectionModel().getSelectedIndex();
      if (index > 0) {
         ServerEntry entry = this.selectionBoxServers.get(index);
         if (Servers.localServer.serverNorth != entry) {
            Servers.addServerNeighbour(entry.id, "NORTH");
         }
      } else if (Servers.localServer.serverNorth != null) {
         Servers.deleteServerNeighbour("NORTH");
      }

      index = this.selectNeighbourBoxSouth.getSelectionModel().getSelectedIndex();
      if (index > 0) {
         ServerEntry entry = this.selectionBoxServers.get(index);
         if (Servers.localServer.serverSouth != entry) {
            Servers.addServerNeighbour(entry.id, "SOUTH");
         }
      } else if (Servers.localServer.serverSouth != null) {
         Servers.deleteServerNeighbour("SOUTH");
      }

      index = this.selectNeighbourBoxEast.getSelectionModel().getSelectedIndex();
      if (index > 0) {
         ServerEntry entry = this.selectionBoxServers.get(index);
         if (Servers.localServer.serverEast != entry) {
            Servers.addServerNeighbour(entry.id, "EAST");
         }
      } else if (Servers.localServer.serverEast != null) {
         Servers.deleteServerNeighbour("EAST");
      }

      this.saveNeighboursButton.setDisable(true);
   }

   @FXML
   void databaseComboBoxChanged(ActionEvent event) {
      if (!this.rebuilding) {
         GameFolder folder = Folders.getGameFolder((String)this.databaseComboBox.getValue());
         DbConnector.closeAll();
         this.setCurrent(folder);
         this.buildDatabaseComboBox(true);
         String gameErr = Folders.getCurrent().getError();
         if (!gameErr.isEmpty()) {
            this.showErrorDialog("Corrupt game folder", gameErr, true);
         }
      }
   }

   @FXML
   void menuAboutActionPerformed(ActionEvent event) {
      logger.info("menuAboutActionPerformed " + event);
   }

   @FXML
   void menuQuitActionPerformed(ActionEvent event) {
      if (this.launcher != null && this.launcher.wasStarted()) {
         Alert alert = new Alert(AlertType.CONFIRMATION);
         alert.setTitle("Stop Server");
         alert.setHeaderText("This will shut down the server.");
         alert.setContentText("Are you really sure you want to do this?");
         Optional<ButtonType> result = alert.showAndWait();
         if (result.isPresent() && result.get() == ButtonType.OK) {
            System.out.println("The server is shutting down");
            this.launcher.getServer().shutDown();
         }
      } else {
         DbConnector.closeAll();
         System.exit(0);
      }
   }

   @FXML
   void deleteServerBtnClicked(ActionEvent event) {
      int index = this.selectServerBox.getSelectionModel().getSelectedIndex();
      ServerEntry entry = this.servers.get(index);
      if (entry != null) {
         Alert alert = new Alert(AlertType.CONFIRMATION);
         alert.setTitle("Delete Server");
         alert.setHeaderText("This will delete the server " + entry.getName());
         alert.setContentText("Are you really sure you want to do this?");
         Optional<ButtonType> result = alert.showAndWait();
         if (result.isPresent() && result.get() == ButtonType.OK) {
            Servers.deleteServerEntry(entry.getId());
            Servers.deleteServerNeighbour(entry.getId());
            Servers.loadNeighbours();
            this.buildSelectServerBox(true);
            lServerPropertySheet = new ServerPropertySheet(this.servers.get(0));
            System.out.println("Property sheet using " + this.servers.get(0).getName());
         }
      } else {
         Alert alert2 = new Alert(AlertType.ERROR);
         alert2.setTitle("Error Dialog");
         alert2.setHeaderText("No such server");
         alert2.setContentText("Please select the server to delete in the drop down.");
         alert2.showAndWait();
      }
   }

   @FXML
   void saveServerButtonLocalClicked(ActionEvent event) {
      logger.info("saveServerLocalButtonClicked " + event);
      String error = localServerPropertySheet.save();
      if (error != null && error.length() > 0 && error.equalsIgnoreCase("properties saved")) {
         Alert alert2 = new Alert(AlertType.INFORMATION);
         alert2.setTitle("Properties saved");
         alert2.setHeaderText("Updated accordingly");
         alert2.setContentText("The settings were saved.");
         alert2.showAndWait();
      } else if (error != null && error.length() > 0) {
         Alert alert2 = new Alert(AlertType.ERROR);
         alert2.setTitle("Error Dialog");
         alert2.setHeaderText("Could not save");
         alert2.setContentText("Errors were reported when saving.");
         Label label = new Label("These are the errors:");
         TextArea textArea = new TextArea(error);
         textArea.setEditable(false);
         textArea.setWrapText(true);
         textArea.setMaxWidth(Double.MAX_VALUE);
         textArea.setMaxHeight(Double.MAX_VALUE);
         GridPane.setVgrow(textArea, Priority.ALWAYS);
         GridPane.setHgrow(textArea, Priority.ALWAYS);
         GridPane expContent = new GridPane();
         expContent.setMaxWidth(Double.MAX_VALUE);
         expContent.add(label, 0, 0);
         expContent.add(textArea, 0, 1);
         alert2.getDialogPane().setExpandableContent(expContent);
         alert2.showAndWait();
      }
   }

   @FXML
   void saveServerButtonClicked(ActionEvent event) {
      logger.info("saveServerButtonClicked " + event);
      String error = lServerPropertySheet.save();
      if (error != null && error.length() > 0 && !error.toLowerCase(Locale.ENGLISH).contains("invalid")) {
         Alert alert2 = new Alert(AlertType.INFORMATION);
         alert2.setTitle("Properties saved");
         alert2.setHeaderText("Updated accordingly");
         alert2.setContentText(error);
         alert2.showAndWait();
         this.buildSelectServerBox(true);
      } else if (error != null && error.length() > 0) {
         Alert alert2 = new Alert(AlertType.ERROR);
         alert2.setTitle("Error Dialog");
         alert2.setHeaderText("Could not save");
         alert2.setContentText("Errors were reported when saving.");
         Label label = new Label("These are the errors:");
         TextArea textArea = new TextArea(error);
         textArea.setEditable(false);
         textArea.setWrapText(true);
         textArea.setMaxWidth(Double.MAX_VALUE);
         textArea.setMaxHeight(Double.MAX_VALUE);
         GridPane.setVgrow(textArea, Priority.ALWAYS);
         GridPane.setHgrow(textArea, Priority.ALWAYS);
         GridPane expContent = new GridPane();
         expContent.setMaxWidth(Double.MAX_VALUE);
         expContent.add(label, 0, 0);
         expContent.add(textArea, 0, 1);
         alert2.getDialogPane().setExpandableContent(expContent);
         alert2.showAndWait();
      }
   }

   @FXML
   void selectServerBoxChanged(ActionEvent event) {
      int index = this.selectServerBox.getSelectionModel().getSelectedIndex();
      ServerEntry entry = this.servers.get(index);
      lServerPropertySheet = new ServerPropertySheet(entry);
      this.buildOtherPropertyTab();
      this.tab2ScrollPane.setContent(localServerPropertySheet);
      this.tab3ScrollPane.setContent(lServerPropertySheet);
      this.tab3ScrollPane.requestFocus();
   }

   @FXML
   void selectPlayerBoxChanged(ActionEvent event) {
      if (!this.resettingPlayers) {
         String name = ((String)this.selectPlayerComboBox.getSelectionModel().getSelectedItem()).toString();
         logger.log(Level.INFO, "Selecting " + name);
         PlayerData data = PlayerDBInterface.getPlayerData(name);
         if (data != null) {
            playerPropertySheet = new PlayerPropertySheet(data);
            this.buildPlayerPropertyTab(false);
            this.tab5ScrollPane.setContent(playerPropertySheet);
            this.tab5ScrollPane.requestFocus();
         }
      }
   }

   @FXML
   void initialize() {
      assert this.tab1ContentPane != null : "fx:id=\"tab1ContentPane\" was not injected: check your FXML file 'WurmServerGui.fxml'.";

      assert this.tab2ContentPane != null : "fx:id=\"tab2ContentPane\" was not injected: check your FXML file 'WurmServerGui.fxml'.";

      assert this.tab1ScrollPane != null : "fx:id=\"tab1ScrollPane\" was not injected: check your FXML file 'WurmServerGui.fxml'.";

      assert this.tab2ScrollPane != null : "fx:id=\"tab2ScrollPane\" was not injected: check your FXML file 'WurmServerGui.fxml'.";

      assert this.runningControls != null : "fx:id=\"runningControls\" was not injected: check your FXML file 'WurmServerGui.fxml'.";

      assert this.gameControls != null : "fx:id=\"gameControls\" was not injected: check your FXML file 'WurmServerGui.fxml'.";

      assert this.playersTab != null : "fx:id=\"playersTab\" was not injected: check your FXML file 'WurmServerGui.fxml'.";

      assert this.serverTravelTab != null : "fx:id=\"serverTravelTab\" was not injected: check your FXML file 'WurmServerGui.fxml'.";

      assert this.serverNeighborTab != null : "fx:id=\"serverNeighborTab\" was not injected: check your FXML file 'WurmServerGui.fxml'.";

      assert this.localServerTab != null : "fx:id=\"localServerTab\" was not injected: check your FXML file 'WurmServerGui.fxml'.";

      assert this.updateGameBtn != null : "fx:id=\"updateGameBtn\" was not injected: check your FXML file 'WurmServerGui.fxml'.";

      assert this.updateRequiredLabel != null : "fx:id=\"updateRequiredLabel\" was not injected: check your FXML file 'WurmServerGui.fxml'.";

      assert this.chkAutoNetwork != null : "fx:id=\"chkAutoNetwork\" was not injected: check your FXML file 'WurmServerGui.fxml'";

      if (!Folders.loadDist()) {
         logger.warning("Unable to load 'dist' folder, please run Steam validation");
         this.showErrorDialog("Corrupt install", "Please run Steam validation to correct issues.", false);
      } else {
         if (!Folders.loadPresets()) {
            logger.warning("Failed to load all presets");
         }

         if (!Folders.loadGames()) {
            logger.warning("Failed to load game folders");
         }

         DbConnector.setUseSqlite(true);
         if (Folders.getGameFolders().size() == 0) {
            if (DistEntity.Creative.existsIn(Folders.getDist().getPath())) {
               GameFolder creative = new GameFolder(Folders.getGamesPath().resolve("Creative"));
               creative.create();
               PresetFolder creativePreset = PresetFolder.fromPath(Folders.getDist().getPath().resolve(DistEntity.Creative.filename()));
               if (creativePreset != null && creativePreset.copyTo(creative.getPath())) {
                  Folders.addGame(creative);
               }
            }

            if (DistEntity.Adventure.existsIn(Folders.getDist().getPath())) {
               GameFolder adventure = new GameFolder(Folders.getGamesPath().resolve("Adventure"));
               adventure.create();
               PresetFolder adventurePreset = PresetFolder.fromPath(Folders.getDist().getPath().resolve(DistEntity.Adventure.filename()));
               if (adventurePreset != null && adventurePreset.copyTo(adventure.getPath())) {
                  Folders.addGame(adventure);
               }
            }
         }

         if (Folders.getCurrent() != null) {
            this.setCurrent(Folders.getCurrent());
         } else {
            this.setCurrent(Folders.getGameFolders().get(0));
         }

         this.nullServer = new ServerEntry();
         this.nullServer.isCreating = true;
         this.buildDatabaseComboBox(false);
         if (!Objects.equals(Constants.dbHost, "localhost")) {
            this.buildSelectServerBox(false);
         }

         lServerPropertySheet = new ServerPropertySheet(this.servers.get(0));
         this.buildOtherPropertyTab();
         this.buildLocalPropertyTab();
         this.tab2ScrollPane.setContent(localServerPropertySheet);
         this.tab3ScrollPane.setContent(lServerPropertySheet);
         this.shutdownButton.setDisable(true);
         this.rootTabPane
            .getSelectionModel()
            .selectedIndexProperty()
            .addListener((observable, oldValue, newValue) -> this.checkIfWeWantToSaveTab(oldValue.intValue(), newValue.intValue()));
         this.buildSelectPlayerBox(false);
         if (Folders.getCurrent() != null) {
            this.setStageTitle("(" + Folders.getCurrent().getName() + ") Wurm Unlimited Server v" + SteamVersion.getCurrentVersion().toString());
         } else {
            this.setStageTitle("Wurm Unlimited Server v" + SteamVersion.getCurrentVersion().toString());
         }

         String gameErrors = Folders.getCurrent().getError();
         if (!gameErrors.isEmpty()) {
            this.showErrorDialog("Selected game is corrupt", gameErrors, true);
         }

         this.chkAutoNetwork.setSelected(ServerProperties.getBoolean("AUTO_NETWORKING", Constants.enableAutoNetworking));
         this.chkAutoNetwork.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != newValue && newValue != ServerProperties.getBoolean("AUTO_NETWORKING", Constants.enableAutoNetworking)) {
               ServerProperties.setValue("AUTO_NETWORKING", Boolean.toString(newValue));
               ServerProperties.checkProperties();
            }
         });
      }
   }

   private Port checkAndShowPortErrors(Port port) {
      if (port.getExceptions().length == 0) {
         return port;
      } else {
         String msg = "Error setting up " + port.getDescription() + " " + port.getPorts().toString() + "\n" + String.join("\n", port.getExceptions());
         logger.warning(msg);
         this.showErrorDialog("Network Configuration Error", msg, true);
         return port;
      }
   }

   private boolean setupExternalNetwork(ServerEntry serverEntry) {
      boolean autoPF = ServerProperties.getBoolean("ENABLE_PNP_PORT_FORWARD", Constants.enablePnpPortForward);
      boolean autoNet = ServerProperties.getBoolean("AUTO_NETWORKING", Constants.enableAutoNetworking);
      AddressHelper addressHelper = new AddressHelper();
      InetAddress externalAddress = null;

      try {
         externalAddress = InetAddress.getByName(serverEntry.EXTERNALIP);
      } catch (UnknownHostException var9) {
         if (!autoNet) {
            this.showErrorDialog("Exception", "Unknown IP address: " + var9.getMessage() + ". Try enabling Auto Networking.", true);
            return false;
         }
      }

      if (externalAddress != null && !addressHelper.validateAddress(externalAddress)) {
         if (!autoNet) {
            String msg = "The value for External IP is invalid:\n" + String.join("\n", addressHelper.getExceptions());
            logger.warning(msg);
            this.showErrorDialog("Network Configuration Error", msg, true);
            return false;
         }

         externalAddress = null;
      }

      if (externalAddress == null) {
         externalAddress = AddressHelper.getFirstValidAddress();
      }

      if (externalAddress == null) {
         return false;
      } else {
         Port steamAuth = new Port(externalAddress, Protocol.Both).setDescription("Steam communication").addPort(8766).toMap(autoPF);
         Port wuPort = new Port(externalAddress, Protocol.TCP).setDescription("Wurm Unlimited").toMap(autoPF);
         Port query = new Port(externalAddress, Protocol.UDP).setDescription("Steam Query port").toMap(autoPF);
         wuPort.setFavoredPort(Integer.parseInt(serverEntry.EXTERNALPORT));
         query.setFavoredPort(SteamHandler.steamQueryPort);
         if (autoNet) {
            wuPort.addPortRange(3000, 4000);
            query.addPortRange(27016, 27030);
         }

         if (autoPF) {
            UPNPService.initialize();
         }

         steamAuth = this.checkAndShowPortErrors(PortHelper.assignPort(steamAuth));
         wuPort = this.checkAndShowPortErrors(PortHelper.assignPort(wuPort));
         query = this.checkAndShowPortErrors(PortHelper.assignPort(query));
         if (steamAuth.getExceptions().length <= 0 && wuPort.getExceptions().length <= 0 && query.getExceptions().length <= 0) {
            SteamHandler.steamQueryPort = (short)query.getAssignedPort();
            ServerProperties.setValue("STEAMQUERYPORT", String.valueOf(query.getAssignedPort()));
            serverEntry.EXTERNALIP = externalAddress.getHostAddress();
            serverEntry.EXTERNALPORT = String.valueOf(wuPort.getAssignedPort());
            return true;
         } else {
            return false;
         }
      }
   }

   private boolean setupInternalNetwork(ServerEntry serverEntry) {
      boolean autoNet = ServerProperties.getBoolean("AUTO_NETWORKING", Constants.enableAutoNetworking);
      AddressHelper addressHelper = new AddressHelper();
      addressHelper.setAddressValidators(new IP4Validator());
      InetAddress internalAddress = null;
      if (!serverEntry.INTRASERVERADDRESS.isEmpty()) {
         try {
            internalAddress = InetAddress.getByName(serverEntry.INTRASERVERADDRESS);
         } catch (UnknownHostException var8) {
            if (!autoNet) {
               this.showErrorDialog("Exception", "Unknown IP address: " + var8.getMessage() + ". Try enabling Auto Networking.", true);
               return false;
            }
         }
      }

      if (internalAddress != null && internalAddress != InetAddress.getLoopbackAddress() && !addressHelper.validateAddress(internalAddress)) {
         if (!autoNet) {
            String msg = "The value for Internal Address is invalid:\n" + String.join("\n", addressHelper.getExceptions());
            logger.warning(msg);
            this.showErrorDialog("Network Configuration Error", msg, true);
            return false;
         }

         internalAddress = null;
      }

      if (internalAddress == null) {
         internalAddress = InetAddress.getLoopbackAddress();
      }

      Port internalPort = new Port(internalAddress, Protocol.TCP).setDescription("Wurm Unlimited Internal");
      Port rmiPort = new Port(internalAddress, Protocol.TCP).setDescription("WU RMI Port");
      Port rmiRegistrationPort = new Port(internalAddress, Protocol.TCP).setDescription("WU RMI Registration");
      if (!autoNet) {
         internalPort.addPort(Integer.parseInt(serverEntry.INTRASERVERPORT));
         rmiPort.addPort(serverEntry.RMI_PORT);
         rmiRegistrationPort.addPort(serverEntry.REGISTRATION_PORT);
      } else {
         internalPort.addPortRange(40000, 41000);
         rmiPort.addPortRange(7120, 7220);
         rmiRegistrationPort.addPortRange(7221, 7320);
      }

      internalPort = this.checkAndShowPortErrors(PortHelper.assignPort(internalPort));
      if (Constants.useIncomingRMI) {
         rmiPort = this.checkAndShowPortErrors(PortHelper.assignPort(rmiPort));
         rmiRegistrationPort = this.checkAndShowPortErrors(PortHelper.assignPort(rmiRegistrationPort));
      }

      if (internalPort.getExceptions().length > 0) {
         return false;
      } else if (!Constants.useIncomingRMI || rmiPort.getExceptions().length <= 0 && rmiRegistrationPort.getExceptions().length <= 0) {
         serverEntry.INTRASERVERADDRESS = internalAddress.getHostAddress();
         serverEntry.INTRASERVERPORT = String.valueOf(internalPort.getAssignedPort());
         serverEntry.RMI_PORT = rmiPort.getAssignedPort();
         serverEntry.REGISTRATION_PORT = rmiRegistrationPort.getAssignedPort();
         return true;
      } else {
         return false;
      }
   }

   void startGame(boolean offline) {
      if (this.launcher != null) {
         if (this.launcher.wasStarted()) {
            this.showErrorDialog("Failed to start", "The server has already been started. You need to restart this gui.", false);
            return;
         }
      } else {
         this.launcher = new ServerLauncher();
      }

      if (!offline) {
         if (!this.setupInternalNetwork(Servers.localServer)) {
            return;
         }

         if (!this.setupExternalNetwork(Servers.localServer)) {
            return;
         }
      }

      Servers.localServer.saveNewGui(Servers.localServer.id);
      if (this.launcher.getServer() != null) {
         this.launcher.getServer().setExternalIp();
      }

      try {
         LocateRegistry.createRegistry(Servers.localServer.REGISTRATION_PORT);
         ServerProperties.setValue("ADMINPASSWORD", adminPassword);
         this.launcher.runServer(true, offline);
         this.gameControls.setVisible(false);
         this.startBtn.setDisable(true);
         this.startOfflineBtn.setDisable(true);
         this.databaseComboBox.setDisable(true);
         this.chkAutoNetwork.setVisible(false);
         this.shutdownButton.setDisable(false);
         this.runningControls.setVisible(true);
         localServerPropertySheet.setReadOnly();
         this.localServerText.setText("Can't change values when the server is running");
      } catch (IOException var6) {
         this.showErrorDialog("Failed to start", "This is the message received when attempting to start the server: " + var6.getMessage(), true);
      } finally {
         System.out.println("\n==================================================================\n");
         System.out.println("Wurm Server launcher finished at " + new Date());
         System.out.println("\n==================================================================\n");
      }
   }

   public static void startDB(String dbName) {
      if (Folders.loadDist()) {
         GameFolder folder = GameFolder.fromPath(Paths.get(dbName));
         setCurrentFolder(folder);
         if (folder == null) {
            logger.warning("Null game folder");
         } else {
            String fileErr = folder.getError();
            if (!fileErr.isEmpty()) {
               logger.warning(fileErr);
            } else if (folder.setCurrent(true)) {
               try {
                  DbConnector.closeAll();
                  ServerDirInfo.setPath(folder.getPath());
                  Constants.load();
                  Constants.dbHost = dbName;
                  Constants.dbPort = "";
                  DbConnector.closeAll();
                  DbConnector.initialize();
                  if (Constants.dbAutoMigrate) {
                     DbConnector.performMigrations();
                  } else if (DbConnector.hasPendingMigrations()) {
                     logger.warning("Pending migrations found but auto-migration disabled.");
                  }

                  initServer(dbName);
               } catch (Exception var13) {
                  var13.printStackTrace();

                  try {
                     folder.setCurrent(false);
                     DbConnector.closeAll();
                     return;
                  } catch (Exception var10) {
                     var10.printStackTrace();
                     return;
                  }
               }

               ServerLauncher launcher = new ServerLauncher();

               try {
                  LocateRegistry.createRegistry(Servers.localServer.REGISTRATION_PORT);
                  ServerProperties.setValue("ADMINPASSWORD", adminPassword);
                  launcher.runServer(true, false);
                  new Thread(new CommandReader(launcher.getServer(), System.in), "Console Command Reader").start();
               } catch (IOException var11) {
                  var11.printStackTrace();
               } finally {
                  System.out.println("\n==================================================================\n");
                  System.out.println("Wurm Server launcher finished at " + new Date());
                  System.out.println("\n==================================================================\n");
               }
            }
         }
      }
   }

   public final void buildSelectNeighbourBoxes() {
      this.selectNeighbourBoxNorth.getItems().clear();
      this.selectNeighbourBoxEast.getItems().clear();
      this.selectNeighbourBoxWest.getItems().clear();
      this.selectNeighbourBoxSouth.getItems().clear();
      int northIndex = -1;
      int southIndex = -1;
      int westIndex = -1;
      int eastIndex = -1;
      this.selectionBoxServers.clear();
      this.selectNeighbourBoxNorth.getItems().add("None");
      this.selectNeighbourBoxNorth.getSelectionModel().select(Integer.valueOf(0));
      this.selectNeighbourBoxEast.getItems().add("None");
      this.selectNeighbourBoxEast.getSelectionModel().select(Integer.valueOf(0));
      this.selectNeighbourBoxWest.getItems().add("None");
      this.selectNeighbourBoxWest.getSelectionModel().select(Integer.valueOf(0));
      this.selectNeighbourBoxSouth.getItems().add("None");
      this.selectNeighbourBoxSouth.getSelectionModel().select(Integer.valueOf(0));
      int index = 0;
      this.selectionBoxServers.put(index++, this.nullServer);
      List<ServerEntry> entries = Arrays.asList(Servers.getAllServers());
      entries.sort(new Comparator<ServerEntry>() {
         public int compare(ServerEntry s1, ServerEntry s2) {
            return s1.getName().compareToIgnoreCase(s2.getName());
         }
      });

      for(ServerEntry entry : entries) {
         if (!entry.isLocal) {
            this.selectNeighbourBoxNorth.getItems().add(entry.getName());
            if (Servers.localServer.serverNorth == entry) {
               northIndex = index;
            }

            this.selectNeighbourBoxEast.getItems().add(entry.getName());
            if (Servers.localServer.serverEast == entry) {
               eastIndex = index;
            }

            this.selectNeighbourBoxWest.getItems().add(entry.getName());
            if (Servers.localServer.serverWest == entry) {
               westIndex = index;
            }

            this.selectNeighbourBoxSouth.getItems().add(entry.getName());
            if (Servers.localServer.serverSouth == entry) {
               southIndex = index;
            }

            this.selectionBoxServers.put(index, entry);
            ++index;
         }
      }

      if (northIndex >= 0) {
         this.selectNeighbourBoxNorth.getSelectionModel().select(northIndex);
      }

      if (westIndex >= 0) {
         this.selectNeighbourBoxWest.getSelectionModel().select(westIndex);
      }

      if (eastIndex >= 0) {
         this.selectNeighbourBoxEast.getSelectionModel().select(eastIndex);
      }

      if (southIndex >= 0) {
         this.selectNeighbourBoxSouth.getSelectionModel().select(southIndex);
      }

      this.saveNeighboursButton.setDisable(true);
   }

   public final void buildSelectPlayerBox(boolean reload) {
      this.resettingPlayers = reload;
      this.selectPlayerComboBox.getItems().clear();
      PlayerDBInterface.loadAllData();
      PlayerDBInterface.loadAllPositionData();

      for(PlayerData entry : PlayerDBInterface.getAllData()) {
         this.selectPlayerComboBox.getItems().add(entry.getName());
      }

      this.selectPlayerComboBox.getItems().sort(new Comparator<String>() {
         public int compare(String s1, String s2) {
            return s1.compareToIgnoreCase(s2);
         }
      });
      this.resettingPlayers = false;
   }

   public final void buildSelectServerBox(boolean reload) {
      this.selectServerBox.getSelectionModel().clearSelection();
      this.selectServerBox.getItems().clear();
      Servers.loadAllServers(reload);
      this.servers.clear();
      int index = 0;
      short newRand = ServerPropertySheet.getNewServerId();
      List<ServerEntry> entries = Arrays.asList(Servers.getAllServers());
      entries.sort(new Comparator<ServerEntry>() {
         public int compare(ServerEntry s1, ServerEntry s2) {
            return s1.getName().compareToIgnoreCase(s2.getName());
         }
      });

      for(ServerEntry entry : entries) {
         if (entry.isLocal) {
            localServerPropertySheet = new ServerPropertySheet(entry);
         } else {
            this.selectServerBox.getItems().add(entry.getName());
            this.servers.put(index, entry);
            ++index;
         }
      }

      if (newRand > 0) {
         this.selectServerBox.getItems().add("Create new ...");
         ServerEntry newServer = new ServerEntry();
         newServer.isCreating = true;
         newServer.name = "The New Server";
         newServer.id = newRand;
         if (Servers.localServer != null) {
            newServer.EXTERNALIP = Servers.localServer.EXTERNALIP;
            newServer.EXTERNALPORT = Servers.localServer.EXTERNALPORT;
            newServer.INTRASERVERADDRESS = Servers.localServer.INTRASERVERADDRESS;
            newServer.INTRASERVERPORT = Servers.localServer.INTRASERVERPORT;
            newServer.INTRASERVERPASSWORD = Servers.localServer.INTRASERVERPASSWORD;
            newServer.RMI_PORT = Servers.localServer.RMI_PORT;
            newServer.REGISTRATION_PORT = Servers.localServer.REGISTRATION_PORT;
         } else {
            newServer.EXTERNALPORT = "3724";
            newServer.INTRASERVERPORT = "48020";
            newServer.RMI_PORT = 7220;
            newServer.REGISTRATION_PORT = 7221;
            newServer.isLocal = true;
            newServer.LOGINSERVER = true;
            newServer.SPAWNPOINTJENNX = 200;
            newServer.SPAWNPOINTJENNY = 200;
            Servers.localServer = newServer;
            localServerPropertySheet = new ServerPropertySheet(newServer);
         }

         this.servers.put(index, newServer);
      }

      this.selectServerBox.getSelectionModel().select(Integer.valueOf(0));
      this.buildSelectNeighbourBoxes();
   }

   final void buildLocalPropertyTab() {
      List<Node> nodes = new ArrayList(localServerPropertySheet.getChildren());
      nodes.remove(this.saveServerButtonLocal);
      nodes.remove(this.localServerText);
      localServerPropertySheet.getChildren().clear();
      localServerPropertySheet.getChildren().add(this.saveServerButtonLocal);
      localServerPropertySheet.getChildren().add(this.localServerText);
      localServerPropertySheet.getChildren().addAll(nodes);
   }

   final void buildOtherPropertyTab() {
      List<Node> nodes = new ArrayList(lServerPropertySheet.getChildren());
      nodes.remove(this.selectServerBox);
      lServerPropertySheet.getChildren().clear();
      lServerPropertySheet.getChildren().add(this.selectServerBox);
      HBox hbox = new HBox();
      hbox.getChildren().add(this.saveServerButton);
      hbox.getChildren().add(this.deleteServerBtn);
      lServerPropertySheet.getChildren().add(hbox);
      lServerPropertySheet.getChildren().addAll(nodes);
   }

   final void buildPlayerPropertyTab(boolean clear) {
      if (!clear || playerPropertySheet != null) {
         List<Node> nodes = new ArrayList(playerPropertySheet.getChildren());
         playerPropertySheet.getChildren().clear();
         playerPropertySheet.getChildren().add(this.selectPlayerComboBox);
         playerPropertySheet.getChildren().add(this.savePlayerBtn);
         if (!clear) {
            playerPropertySheet.getChildren().addAll(nodes);
         }
      }
   }

   final void buildDatabaseComboBox(boolean changedDirectory) {
      this.rebuilding = true;
      this.databaseComboBox.getItems().clear();

      for(GameFolder folder : Folders.getGameFolders()) {
         this.databaseComboBox.getItems().add(folder.getName());
      }

      this.databaseComboBox.getItems().sort(new Comparator<String>() {
         public int compare(String s1, String s2) {
            return s1.compareToIgnoreCase(s2);
         }
      });
      if (Folders.getCurrent() != null) {
         this.databaseComboBox.getSelectionModel().select(Folders.getCurrent().getName());
         this.renameTxtfield.setText(Folders.getCurrent().getName());
         this.copyTextField.setText(Folders.getCurrent().getName() + "Copy");
         Constants.load();
         Constants.dbHost = Folders.getCurrent().getName();
         Constants.dbPort = "";
         DbConnector.closeAll();
         DbConnector.initialize(true);
         this.gameControls.setVisible(true);
         if (DbConnector.hasPendingMigrations()) {
            this.startBtn.setDisable(true);
            this.startOfflineBtn.setDisable(true);
            this.setDisableTabs(true);
            this.chkAutoNetwork.setVisible(false);
            this.updateRequiredLabel.setVisible(true);
            this.updateGameBtn.setDisable(false);
         } else if (Folders.getCurrent().getError().isEmpty()) {
            this.startBtn.setDisable(false);
            this.startOfflineBtn.setDisable(false);
            this.setDisableTabs(false);
            this.chkAutoNetwork.setVisible(true);
            this.updateRequiredLabel.setVisible(false);
            this.updateGameBtn.setDisable(true);
         }

         if (changedDirectory) {
            this.buildSelectServerBox(true);
            lServerPropertySheet = new ServerPropertySheet(this.servers.get(0));
            this.buildOtherPropertyTab();
            this.buildLocalPropertyTab();
            this.tab2ScrollPane.setContent(localServerPropertySheet);
            this.tab3ScrollPane.setContent(lServerPropertySheet);
            this.buildSelectPlayerBox(true);
            this.buildPlayerPropertyTab(true);
         }
      }

      this.rebuilding = false;
   }

   private void checkIfWeWantToSaveTab(int oldvalue, int newValue) {
      if (oldvalue != newValue && oldvalue == 1 && localServerPropertySheet.haveChanges()) {
         localServerPropertySheet.AskIfSave();
      }
   }

   public void setStage(Stage primaryStage) {
      this.primaryStage = primaryStage;
      if (Folders.getCurrent() != null) {
         this.setStageTitle("(" + Folders.getCurrent().getName() + ") Wurm Unlimited Server v" + SteamVersion.getCurrentVersion().toString());
      } else {
         this.setStageTitle("Wurm Unlimited Server v" + SteamVersion.getCurrentVersion().toString());
      }
   }

   private void setStageTitle(String title) {
      if (this.primaryStage != null) {
         this.primaryStage.setTitle(title);
      }
   }

   public boolean shutdown() {
      if (this.launcher != null && this.launcher.wasStarted()) {
         Alert alert = new Alert(AlertType.CONFIRMATION);
         alert.setTitle("Stop Server");
         alert.setHeaderText("This will shut down the server.");
         alert.setContentText("Are you really sure you want to do this?");
         Optional<ButtonType> result = alert.showAndWait();
         if (!result.isPresent() || result.get() != ButtonType.OK) {
            return false;
         }

         System.out.println("The server is shutting down");
         this.launcher.getServer().shutDown();
      }

      return true;
   }

   void setDisableTabs(boolean disable) {
      this.playersTab.setDisable(disable);
      this.serverNeighborTab.setDisable(disable);
      this.localServerTab.setDisable(disable);
      this.serverTravelTab.setDisable(disable);
   }

   static {
      EnumMap<GuiCommandLineArgument, WurmServerGuiController.ArgumentApplication> applications = new EnumMap<>(GuiCommandLineArgument.class);
      applications.put(GuiCommandLineArgument.IP_ADDR, ip -> {
         Servers.localServer.EXTERNALIP = ip;
         Servers.localServer.INTRASERVERADDRESS = ip;
         logger.log(Level.INFO, "Internal and External IP set to: " + ip);
      });
      applications.put(GuiCommandLineArgument.EXTERNAL_PORT, externalPort -> {
         Servers.localServer.EXTERNALPORT = externalPort;
         logger.log(Level.INFO, "External port set to: " + externalPort);
      });
      applications.put(GuiCommandLineArgument.INTERNAL_PORT, internalPort -> {
         Servers.localServer.INTRASERVERPORT = internalPort;
         logger.log(Level.INFO, "Internal port set to: " + internalPort);
      });
      applications.put(GuiCommandLineArgument.EPIC_SETTINGS, epicSettingStr -> {
         boolean epic = Boolean.parseBoolean(epicSettingStr);
         Servers.localServer.EPIC = epic;
         logger.log(Level.INFO, "Epic settings set to: " + epic);
      });
      applications.put(GuiCommandLineArgument.HOME_KINGDOM, kIdStr -> {
         byte kId = Byte.parseByte(kIdStr);
         Servers.localServer.KINGDOM = kId;
         String kName = Kingdoms.getNameFor(kId);
         logger.log(Level.INFO, "Home server kingdom id set to: " + kId + " name: " + kName);
      });
      applications.put(GuiCommandLineArgument.HOME_SERVER, isHomeStr -> {
         boolean isHome = Boolean.parseBoolean(isHomeStr);
         Servers.localServer.HOMESERVER = isHome;
         logger.log(Level.INFO, "Is home server: " + isHome);
      });
      applications.put(GuiCommandLineArgument.LOGIN_SERVER, isLoginStr -> {
         boolean isLogin = Boolean.parseBoolean(isLoginStr);
         Servers.localServer.LOGINSERVER = isLogin;
         logger.log(Level.INFO, "Is loginserver: " + isLogin);
      });
      applications.put(GuiCommandLineArgument.PLAYER_NUM, playerNumStr -> {
         int playerLimit = Integer.parseInt(playerNumStr);
         Servers.localServer.pLimit = playerLimit;
         Servers.localServer.playerLimitOverridable = false;
         logger.log(Level.INFO, "Player Limit: " + playerLimit);
      });
      applications.put(GuiCommandLineArgument.PVP, pvpStr -> {
         boolean isPvP = Boolean.parseBoolean(pvpStr);
         Servers.localServer.PVPSERVER = isPvP;
         logger.log(Level.INFO, "Allow PvP: " + isPvP);
      });
      applications.put(GuiCommandLineArgument.QUERY_PORT, qPortStr -> {
         ServerProperties.loadProperties();
         ServerProperties.setValue("STEAMQUERYPORT", qPortStr);
         Servers.localServer.STEAMQUERYPORT = qPortStr;
         logger.log(Level.INFO, "Steam query port: " + qPortStr);
      });
      applications.put(GuiCommandLineArgument.RMI_PORT, rmiPortStr -> {
         int rmiPort = Integer.parseInt(rmiPortStr);
         Servers.localServer.RMI_PORT = rmiPort;
         logger.log(Level.INFO, "RMI port: " + rmiPort);
      });
      applications.put(GuiCommandLineArgument.RMI_REG, rmiPortStr -> {
         int rmiPort = Integer.parseInt(rmiPortStr);
         Servers.localServer.REGISTRATION_PORT = rmiPort;
         logger.log(Level.INFO, "RMI register port: " + rmiPort);
      });
      applications.put(GuiCommandLineArgument.SERVER_NAME, sName -> {
         Servers.localServer.name = sName;
         logger.log(Level.INFO, "Server broadcast name: " + sName);
      });
      applications.put(GuiCommandLineArgument.SERVER_PASS, sPass -> Servers.localServer.setSteamServerPassword(sPass));
      ARGUMENT_APPLICATIONS = Collections.unmodifiableMap(applications);
   }

   private interface ArgumentApplication {
      void applyArgument(String var1);
   }
}
