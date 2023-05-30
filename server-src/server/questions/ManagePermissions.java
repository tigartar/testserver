package com.wurmonline.server.questions;

import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.ItemSettings;
import com.wurmonline.server.players.Friend;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.players.PermissionsByPlayer;
import com.wurmonline.server.players.PermissionsHistories;
import com.wurmonline.server.players.PermissionsPlayerList;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.structures.StructureSettings;
import com.wurmonline.server.utils.BMLBuilder;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.Village;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ManagePermissions extends Question {
   private static final Logger logger = Logger.getLogger(ManagePermissions.class.getName());
   private static final String legalNameChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
   private static final String legalDescriptionChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'- 1234567890.,+/!()";
   private Player player;
   private ManageObjectList.Type objectType;
   private PermissionsPlayerList.ISettings object;
   private boolean hasBackButton;
   private boolean parentHasBack;
   private ManageObjectList.Type parentType;
   private String error;
   private boolean hadManage = true;

   public ManagePermissions(
      Creature aResponder,
      ManageObjectList.Type aObjectType,
      PermissionsPlayerList.ISettings anObject,
      boolean canGoBack,
      long parent,
      boolean parentCanGoBack,
      @Nullable ManageObjectList.Type aParentType,
      String errorText
   ) {
      super(aResponder, "Managing " + anObject.getObjectName(), "Manage Permissions", 119, parent);
      this.player = (Player)aResponder;
      this.objectType = aObjectType;
      this.object = anObject;
      this.hasBackButton = canGoBack;
      this.parentHasBack = parentCanGoBack;
      this.parentType = aParentType;
      this.error = errorText;
   }

   @Override
   public void answer(Properties aAnswers) {
      this.setAnswer(aAnswers);
      String sback = aAnswers.getProperty("back");
      if (sback != null && sback.equals("true")) {
         ManageObjectList mol = new ManageObjectList(this.player, this.parentType, this.target, this.parentHasBack, 1, "", true);
         mol.sendQuestion();
      } else {
         String sclose = aAnswers.getProperty("close");
         if (sclose == null || !sclose.equals("true")) {
            if (this.object.isItem() && this.object.getTemplateId() == 272) {
               ManagePermissions mp = new ManagePermissions(
                  this.player,
                  this.objectType,
                  this.object,
                  this.hasBackButton,
                  this.target,
                  this.parentHasBack,
                  this.parentType,
                  "Cannot modify corpse permissions."
               );
               mp.sendQuestion();
            } else {
               boolean canChangeName = this.object.canChangeName(this.player);
               String newObjectName = aAnswers.getProperty("object");
               String newOwnerName = aAnswers.getProperty("owner");
               String newOwner = newOwnerName != null ? LoginHandler.raiseFirstLetter(newOwnerName) : "";
               boolean manage = Boolean.parseBoolean(aAnswers.getProperty("manage"));
               boolean manageChanged = false;
               if (canChangeName
                  && !newObjectName.equalsIgnoreCase(this.object.getObjectName())
                  && containsIllegalCharacters("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'- 1234567890.,+/!()", newObjectName)) {
                  ManagePermissions mp = new ManagePermissions(
                     this.player,
                     this.objectType,
                     this.object,
                     this.hasBackButton,
                     this.target,
                     this.parentHasBack,
                     this.parentType,
                     "Illegal new object name '" + newObjectName + "'."
                  );
                  mp.sendQuestion();
               } else {
                  long ownerId = -10L;
                  if (newOwner.length() != 0 && !newOwner.equalsIgnoreCase(this.object.getOwnerName())) {
                     if (containsIllegalCharacters("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ", newOwner)) {
                        String msg = "Illegal new owners name '" + newOwner + "'.";
                        if (this.object.mayShowPermissions(this.player)) {
                           ManagePermissions mp = new ManagePermissions(
                              this.player, this.objectType, this.object, this.hasBackButton, this.target, this.parentHasBack, this.parentType, msg
                           );
                           mp.sendQuestion();
                        } else {
                           this.player.getCommunicator().sendNormalServerMessage(msg);
                        }

                        return;
                     }

                     if (!this.object.canChangeOwner(this.getResponder())) {
                        String msg = "Not allowed to change owner.";
                        if (this.object.mayShowPermissions(this.player)) {
                           ManagePermissions mp = new ManagePermissions(
                              this.player,
                              this.objectType,
                              this.object,
                              this.hasBackButton,
                              this.target,
                              this.parentHasBack,
                              this.parentType,
                              "Not allowed to change owner."
                           );
                           mp.sendQuestion();
                        } else {
                           this.player.getCommunicator().sendNormalServerMessage("Not allowed to change owner.");
                        }

                        return;
                     }

                     ownerId = PlayerInfoFactory.getWurmId(newOwnerName);
                     if (ownerId == -10L) {
                        String msg = "Cannot find new owner '" + newOwnerName + "'.";
                        if (this.object.mayShowPermissions(this.player)) {
                           ManagePermissions mp = new ManagePermissions(
                              this.player, this.objectType, this.object, this.hasBackButton, this.target, this.parentHasBack, this.parentType, msg
                           );
                           mp.sendQuestion();
                        } else {
                           this.player.getCommunicator().sendNormalServerMessage(msg);
                        }

                        return;
                     }
                  }

                  LinkedList<String> changes = new LinkedList<>();
                  LinkedList<String> removed = new LinkedList<>();
                  LinkedList<String> added = new LinkedList<>();
                  LinkedList<String> updated = new LinkedList<>();
                  if (!this.object.mayShowPermissions(this.player)) {
                     if (newObjectName != null && !newObjectName.equalsIgnoreCase(this.object.getObjectName())) {
                        if (!this.object.setObjectName(newObjectName, this.player)) {
                           this.player.getCommunicator().sendNormalServerMessage("Problem changing name.");
                           return;
                        }

                        changes.add("Name");
                     }

                     if (ownerId != -10L) {
                        if (!this.object.setNewOwner(ownerId)) {
                           if (!changes.isEmpty()) {
                              PermissionsHistories.addHistoryEntry(
                                 this.object.getWurmId(),
                                 System.currentTimeMillis(),
                                 this.player.getWurmId(),
                                 this.player.getName(),
                                 "Changed " + (String)changes.getFirst()
                              );
                           }

                           this.player.getCommunicator().sendNormalServerMessage("Problem changing name.");
                           return;
                        }

                        changes.add("Owner to '" + newOwner + "'");
                     }

                     if (this.objectType == ManageObjectList.Type.DOOR
                        && ownerId == -10L
                        && this.object.getWarning().length() == 0
                        && this.object.isManaged() != manage) {
                        this.object.setIsManaged(manage, this.player);
                        manageChanged = true;
                     }

                     try {
                        this.object.save();
                     } catch (IOException var32) {
                        logger.log(Level.WARNING, var32.getMessage(), (Throwable)var32);
                     }

                     if (!changes.isEmpty() || manageChanged) {
                        StringBuilder buf = new StringBuilder();
                        if (!changes.isEmpty()) {
                           buf.append("Changed " + String.join(", ", changes));
                        }

                        if (manageChanged) {
                           if (buf.length() > 0) {
                              buf.append(", ");
                           }

                           if (manage) {
                              buf.append("Ticked Controlled by Flag");
                           } else {
                              buf.append("UnTicked Controlled by Flag");
                           }
                        }

                        PermissionsHistories.addHistoryEntry(
                           this.object.getWurmId(), System.currentTimeMillis(), this.player.getWurmId(), this.player.getName(), buf.toString()
                        );
                        this.player.getCommunicator().sendNormalServerMessage("You " + buf.toString());
                        ManagePermissions mp = new ManagePermissions(
                           this.player, this.objectType, this.object, this.hasBackButton, this.target, this.parentHasBack, this.parentType, ""
                        );
                        mp.sendQuestion();
                     }
                  } else {
                     int rows = Integer.parseInt(aAnswers.getProperty("rows"));
                     if (rows > this.object.getMaxAllowed()) {
                        ManagePermissions mp = new ManagePermissions(
                           this.player,
                           this.objectType,
                           this.object,
                           this.hasBackButton,
                           this.target,
                           this.parentHasBack,
                           this.parentType,
                           "Too many allowed entries, Max is " + this.object.getMaxAllowed() + "."
                        );
                        mp.sendQuestion();
                     } else {
                        PermissionsPlayerList ppl = this.object.getPermissionsPlayerList();
                        long[] pIds = new long[rows];
                        int[] settings = new int[rows];
                        int excludeBit = StructureSettings.StructurePermissions.EXCLUDE.getBit();
                        if (rows > 0) {
                           int cols = Integer.parseInt(aAnswers.getProperty("cols"));
                           Permissions.IPermission[] values = this.objectType.getEnumValues();
                           int[] bits = new int[cols];

                           for(int x = 0; x < cols; ++x) {
                              bits[x] = values[x].getBit();
                           }

                           for(int row = 0; row < rows; ++row) {
                              pIds[row] = Long.parseLong(aAnswers.getProperty("r" + row));
                              BitSet bitset = new BitSet(32);
                              boolean otherThanExclude = false;

                              for(int col = 0; col < cols; ++col) {
                                 boolean flag = Boolean.parseBoolean(aAnswers.getProperty("r" + row + "c" + col));
                                 bitset.set(bits[col], flag);
                                 if (bits[col] != excludeBit && flag) {
                                    otherThanExclude = true;
                                 }
                              }

                              boolean wasExcluded = false;
                              if (ppl.exists(pIds[row])) {
                                 wasExcluded = ppl.hasPermission(pIds[row], excludeBit);
                              }

                              if (wasExcluded && otherThanExclude) {
                                 bitset.clear(excludeBit);
                              } else if (bitset.get(excludeBit)) {
                                 bitset.clear();
                                 bitset.set(excludeBit);
                              }

                              if (pIds[row] != -30L && pIds[row] != -20L && pIds[row] != -40L && pIds[row] != -50L) {
                                 if (bitset.get(StructureSettings.StructurePermissions.MANAGE.getBit())) {
                                    for(int x = 1; x < cols; ++x) {
                                       if (bits[x] != excludeBit) {
                                          bitset.set(bits[x], true);
                                       }
                                    }
                                 }

                                 if (this.object.isItem() && this.object.getTemplateId() == 1271) {
                                    if (bitset.get(ItemSettings.MessageBoardPermissions.MANAGE_NOTICES.getBit())) {
                                       for(int x = 2; x < cols; ++x) {
                                          if (bits[x] != excludeBit) {
                                             bitset.set(bits[x], true);
                                          }
                                       }
                                    } else if (bitset.get(ItemSettings.MessageBoardPermissions.MAY_POST_NOTICES.getBit())) {
                                       bitset.set(ItemSettings.MessageBoardPermissions.ACCESS_HOLD.getBit(), true);
                                    } else if (bitset.get(ItemSettings.MessageBoardPermissions.MAY_ADD_PMS.getBit())) {
                                       bitset.set(ItemSettings.MessageBoardPermissions.ACCESS_HOLD.getBit(), true);
                                    }
                                 }
                              } else if (bitset.get(StructureSettings.StructurePermissions.MANAGE.getBit())) {
                                 bitset.clear(StructureSettings.StructurePermissions.MANAGE.getBit());
                              }

                              settings[row] = GeneralUtilities.getIntSettingsFrom(bitset);
                           }
                        }

                        if (canChangeName && !newObjectName.equalsIgnoreCase(this.object.getObjectName())) {
                           if (!this.object.setObjectName(newObjectName, this.player)) {
                              ManagePermissions mp = new ManagePermissions(
                                 this.player,
                                 this.objectType,
                                 this.object,
                                 this.hasBackButton,
                                 this.target,
                                 this.parentHasBack,
                                 this.parentType,
                                 "Problem changing name."
                              );
                              mp.sendQuestion();
                              return;
                           }

                           changes.add("Name");
                        }

                        if (ownerId != -10L) {
                           if (!this.object.setNewOwner(ownerId)) {
                              if (!changes.isEmpty()) {
                                 PermissionsHistories.addHistoryEntry(
                                    this.object.getWurmId(),
                                    System.currentTimeMillis(),
                                    this.player.getWurmId(),
                                    this.player.getName(),
                                    "Changed " + (String)changes.getFirst()
                                 );
                              }

                              ManagePermissions mp = new ManagePermissions(
                                 this.player,
                                 this.objectType,
                                 this.object,
                                 this.hasBackButton,
                                 this.target,
                                 this.parentHasBack,
                                 this.parentType,
                                 "Problem changing owner."
                              );
                              mp.sendQuestion();
                              return;
                           }

                           changes.add("Owner to '" + newOwner + "'");
                        }

                        if (ownerId == -10L && this.object.isManaged() != manage) {
                           this.object.setIsManaged(manage, this.player);
                           manageChanged = true;
                        }

                        try {
                           this.object.save();
                        } catch (IOException var33) {
                           logger.log(Level.WARNING, var33.getMessage(), (Throwable)var33);
                        }

                        if (ownerId == -10L && !manageChanged) {
                           for(PermissionsByPlayer pbp : ppl.getPermissionsByPlayer()) {
                              boolean found = false;

                              for(long pId : pIds) {
                                 if (pbp.getPlayerId() == pId) {
                                    found = true;
                                    break;
                                 }
                              }

                              if (!found) {
                                 removed.add(pbp.getName());
                                 this.object.removeGuest(pbp.getPlayerId());
                              }
                           }

                           Permissions.IPermission[] values = this.objectType.getEnumValues();
                           String[] title = new String[values.length];
                           int[] bits = new int[values.length];

                           for(int x = 0; x < values.length; ++x) {
                              title[x] = values[x].getHeader1() + " " + values[x].getHeader2();
                              bits[x] = values[x].getBit();
                           }

                           for(int x = 0; x < pIds.length; ++x) {
                              int oldSettings = 0;
                              if (ppl.exists(pIds[x])) {
                                 oldSettings = ppl.getPermissionsFor(pIds[x]).getPermissions();
                              }

                              LinkedList<String> perms = new LinkedList<>();

                              for(int y = 0; y < 32; ++y) {
                                 boolean oldBit = (oldSettings >>> y & 1) == 1;
                                 boolean newBit = (settings[x] >>> y & 1) == 1;
                                 if (oldBit != newBit) {
                                    int bit = -1;

                                    for(int j = 0; j < values.length; ++j) {
                                       if (bits[j] == y) {
                                          bit = j;
                                          break;
                                       }
                                    }

                                    if (bit != -1) {
                                       if (oldBit) {
                                          perms.add("-" + title[bit]);
                                       } else {
                                          perms.add("+" + title[bit]);
                                       }
                                    } else if (oldBit) {
                                       perms.add("-Bad Bit");
                                    } else {
                                       perms.add("+Bad Bit");
                                    }
                                 }
                              }

                              String fields = "(" + String.join(", ", perms) + ")";
                              if (ppl.exists(pIds[x])) {
                                 if (oldSettings != settings[x]) {
                                    this.object.addGuest(pIds[x], settings[x]);
                                    updated.add(PermissionsByPlayer.getPlayerOrGroupName(pIds[x]) + fields);
                                 }
                              } else if (settings[x] != 0) {
                                 this.object.addGuest(pIds[x], settings[x]);
                                 added.add(PermissionsByPlayer.getPlayerOrGroupName(pIds[x]) + fields);
                              }
                           }
                        }

                        if (this.hadManage && !this.object.mayShowPermissions(this.player)) {
                           this.player.getCommunicator().sendHidePermissions();
                        }

                        StringBuilder buf = new StringBuilder();
                        if (!changes.isEmpty()) {
                           buf.append("Changed " + String.join(", ", changes));
                        }

                        if (manageChanged) {
                           if (buf.length() > 0) {
                              buf.append(", ");
                           }

                           if (this.objectType == ManageObjectList.Type.DOOR) {
                              if (manage) {
                                 buf.append("Ticked Controlled by Flag");
                              } else {
                                 buf.append("UnTicked Controlled by Flag");
                              }
                           } else if (manage) {
                              buf.append("Ticked Manage Flag");
                           } else {
                              buf.append("UnTicked Manage Flag");
                           }
                        }

                        if (!added.isEmpty()) {
                           if (buf.length() > 0) {
                              buf.append(", ");
                           }

                           buf.append("Added " + String.join(", ", added));
                        }

                        if (!removed.isEmpty()) {
                           if (buf.length() > 0) {
                              buf.append(", ");
                           }

                           buf.append("Removed " + String.join(", ", removed));
                        }

                        if (!updated.isEmpty()) {
                           if (buf.length() > 0) {
                              buf.append(", ");
                           }

                           buf.append("Updated " + String.join(", ", updated));
                        }

                        if (buf.length() > 0) {
                           String historyevent = buf.toString();
                           if (buf.length() > 255) {
                              historyevent = historyevent.substring(0, 250) + " ...";
                           }

                           PermissionsHistories.addHistoryEntry(
                              this.object.getWurmId(), System.currentTimeMillis(), this.player.getWurmId(), this.player.getName(), historyevent
                           );
                           this.player.getCommunicator().sendNormalServerMessage("You " + historyevent);
                        }

                        ManagePermissions mp = new ManagePermissions(
                           this.player, this.objectType, this.object, this.hasBackButton, this.target, this.parentHasBack, this.parentType, ""
                        );
                        mp.sendQuestion();
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public void sendQuestion() {
      String objectName = this.object.getObjectName();
      boolean canChangeName = this.object.canChangeName(this.player);
      String ownerName = this.player.getPower() > 1 ? this.object.getOwnerName() : "";
      boolean canChangeOwner = this.object.canChangeOwner(this.player);
      String warningText = this.object.getWarning();
      boolean isManaged = this.object.isManaged();
      boolean isManageEnabled = this.object.isManageEnabled(this.player);
      String mayManageText = this.object.mayManageText(this.player);
      String mayManageHover = this.object.mayManageHover(this.player);
      String messageOnTick = this.object.messageOnTick();
      String questionOnTick = this.object.questionOnTick();
      String messageUnTick = this.object.messageUnTick();
      String questionUnTick = this.object.questionUnTick();
      if (!this.object.mayShowPermissions(this.player)) {
         if (canChangeOwner || canChangeName || isManageEnabled && this.objectType == ManageObjectList.Type.DOOR) {
            this.hadManage = false;
            String oldOwnerName = this.player.getPower() > 1 ? "(" + this.object.getOwnerName() + ") " : "";
            String door = "";
            if (this.objectType == ManageObjectList.Type.DOOR) {
               door = "checkbox{id=\"manage\";text=\""
                  + mayManageText
                  + "\";hover=\""
                  + mayManageHover
                  + "\";confirm=\""
                  + messageOnTick
                  + "\";question=\""
                  + questionOnTick
                  + "\";unconfirm=\""
                  + messageUnTick
                  + "\";unquestion=\""
                  + questionUnTick
                  + "\";selected=\""
                  + isManaged
                  + "\"}";
            }

            StringBuilder buf = new StringBuilder();
            buf.append(
               "border{border{size=\"20,20\";null;null;label{type='bold';text=\""
                  + this.question
                  + "\"};harray{"
                  + (this.hasBackButton ? "button{text=\"Back\";id=\"back\"};label{text=\" \"};" : "")
                  + "button{text=\"Close\";id=\"close\"};label{text=\" \"}};null;};harray{passthrough{id=\"id\";text=\""
                  + this.getId()
                  + "\"};label{text=\"\"}};varray{label{text=\"You are not allowed to manage permissions.\"};harray{label{text=\"Name:\"};"
                  + (canChangeName ? "input{id=\"object\";text=\"" + objectName + "\"};" : "label{text=\"" + objectName + "\"};")
                  + "}"
                  + (
                     canChangeOwner
                        ? "harray{label{text=\"Change owner " + oldOwnerName + "to \"};input{id=\"owner\";text=\"\"}};"
                        : (this.objectType == ManageObjectList.Type.DOOR && this.object.getWarning().length() == 0 ? door : "label{text=\" \"};")
                  )
                  + "};varray{label{text=\"\"};label{text=\"\"};harray{button{text=\"Apply Changes\";id=\"save\"};label{text=\" \"}}};"
                  + (warningText.length() > 0 ? "center{label{type=\"bold\";color=\"240,40,40\";text=\"" + warningText + "\"}};" : "null;")
                  + "}"
            );
            BMLBuilder completePanel = BMLBuilder.createBMLBorderPanel(
               BMLBuilder.createBMLBorderPanel(
                  null,
                  null,
                  BMLBuilder.createGenericBuilder().addLabel(this.question, null, BMLBuilder.TextType.BOLD, null),
                  BMLBuilder.createHorizArrayNode(false)
                     .addString(this.hasBackButton ? BMLBuilder.createButton("back", "Back") : BMLBuilder.createLabel(" "))
                     .addButton("close", "Close")
                     .addLabel(" "),
                  null,
                  20,
                  20
               ),
               BMLBuilder.createHorizArrayNode(false).addPassthrough("id", Integer.toString(this.getId())).addLabel(""),
               BMLBuilder.createVertArrayNode(false)
                  .addLabel("You are not allowed to manage permissions.")
                  .addString(
                     BMLBuilder.createHorizArrayNode(false)
                        .addLabel("Name:")
                        .addString(canChangeName ? BMLBuilder.createInput("object", objectName) : BMLBuilder.createLabel(objectName))
                        .toString()
                  )
                  .addString(
                     canChangeOwner
                        ? BMLBuilder.createHorizArrayNode(false).addLabel("Change owner " + oldOwnerName + "to ").addInput("owner", "", 0, 0).toString()
                        : (
                           this.objectType == ManageObjectList.Type.DOOR && this.object.getWarning().length() == 0
                              ? BMLBuilder.createGenericBuilder()
                                 .addCheckbox(
                                    "manage",
                                    mayManageText,
                                    questionOnTick,
                                    messageOnTick,
                                    questionUnTick,
                                    messageUnTick,
                                    mayManageHover,
                                    isManaged,
                                    true,
                                    null
                                 )
                                 .toString()
                              : BMLBuilder.createLabel(" ")
                        )
                  ),
               BMLBuilder.createVertArrayNode(false)
                  .addLabel("")
                  .addLabel("")
                  .addString(BMLBuilder.createHorizArrayNode(false).addButton("save", "Apply Changes").addLabel(" ").toString()),
               warningText.length() > 0
                  ? BMLBuilder.createCenteredNode(BMLBuilder.createGenericBuilder().addLabel(warningText, null, BMLBuilder.TextType.BOLD, Color.RED))
                  : null
            );
            int ht = warningText.length() > 0 ? 150 : 125;
            this.getResponder()
               .getCommunicator()
               .sendBml(320, ht, true, true, buf.toString(), 200, 200, 200, this.objectType.getTitle() + " - Manage Permissions");
         }
      } else {
         if (this.error.length() > 0) {
            this.player.getCommunicator().sendPermissionsApplyChangesFailed(this.getId(), this.error);
            return;
         }

         String mySettlement = this.player.getCitizenVillage() != null ? "Citizens of my deed" : "";
         String allowAlliesText = this.object.getAllianceName();
         String allowCitizensText = this.object.getSettlementName();
         String allowKingdomText = this.object.getKingdomName();
         String allowEveryoneText = this.object.canAllowEveryone() ? "\"Everyone\"" : "";
         String allowRolePermissionText = this.object.getRolePermissionName();
         Permissions.IPermission[] values = this.objectType.getEnumValues();
         String[] header1 = new String[values.length];
         String[] header2 = new String[values.length];
         String[] hover = new String[values.length];
         int[] bits = new int[values.length];

         for(int x = 0; x < values.length; ++x) {
            header1[x] = values[x].getHeader1();
            header2[x] = values[x].getHeader2();
            hover[x] = values[x].getHover();
            bits[x] = values[x].getBit();
         }

         PermissionsPlayerList allowedList = this.object.getPermissionsPlayerList();
         String[] permittedNames = new String[allowedList.size()];
         long[] permittedIds = new long[allowedList.size()];
         boolean[][] allowed = new boolean[allowedList.size()][header1.length];
         int count = 0;
         PermissionsByPlayer[] pbpList = allowedList.getPermissionsByPlayer();
         Arrays.sort((Object[])pbpList);

         for(PermissionsByPlayer pbp : pbpList) {
            long playerId = pbp.getPlayerId();
            permittedNames[count] = pbp.getName();
            permittedIds[count] = playerId;

            for(int bit = 0; bit < bits.length; ++bit) {
               allowed[count][bit] = pbp.hasPermission(bits[bit]);
            }

            ++count;
         }

         Friend[] friendsList = this.player.getFriends();
         Arrays.sort((Object[])friendsList);
         List<Long> trusted = new ArrayList<>();
         List<Long> friends = new ArrayList<>();
         List<Long> citizens = new ArrayList<>();

         for(int var52 = 0; var52 < friendsList.length; ++var52) {
            if (friendsList[var52].getCategory().getCatId() == Friend.Category.Trusted.getCatId() && !allowedList.exists(friendsList[var52].getFriendId())) {
               trusted.add(friendsList[var52].getFriendId());
            }

            if (friendsList[var52].getCategory().getCatId() == Friend.Category.Friends.getCatId() && !allowedList.exists(friendsList[var52].getFriendId())) {
               friends.add(friendsList[var52].getFriendId());
            }
         }

         ManagePermissions.playerIdName[] trustedIdNames = new ManagePermissions.playerIdName[trusted.size()];

         for(int var53 = 0; var53 < trusted.size(); ++var53) {
            trustedIdNames[var53] = new ManagePermissions.playerIdName(trusted.get(var53));
         }

         Arrays.sort((Object[])trustedIdNames);
         long[] trustedIds = new long[trusted.size()];
         String[] trustedNames = new String[trusted.size()];

         for(int var54 = 0; var54 < trusted.size(); ++var54) {
            trustedIds[var54] = trustedIdNames[var54].getWurmId();
            trustedNames[var54] = trustedIdNames[var54].getName();
         }

         ManagePermissions.playerIdName[] friendIdNames = new ManagePermissions.playerIdName[friends.size()];

         for(int var55 = 0; var55 < friends.size(); ++var55) {
            friendIdNames[var55] = new ManagePermissions.playerIdName(friends.get(var55));
         }

         Arrays.sort((Object[])friendIdNames);
         long[] friendIds = new long[friends.size()];
         String[] friendNames = new String[friends.size()];

         for(int var56 = 0; var56 < friends.size(); ++var56) {
            friendIds[var56] = friendIdNames[var56].getWurmId();
            friendNames[var56] = friendIdNames[var56].getName();
         }

         Village village = this.player.getCitizenVillage();
         if (village != null) {
            for(Citizen c : village.getCitizens()) {
               if (!allowedList.exists(c.wurmId) && c.isPlayer()) {
                  citizens.add(c.wurmId);
               }
            }
         }

         ManagePermissions.playerIdName[] citizenIdNames = new ManagePermissions.playerIdName[citizens.size()];

         for(int var57 = 0; var57 < citizens.size(); ++var57) {
            citizenIdNames[var57] = new ManagePermissions.playerIdName(citizens.get(var57));
         }

         Arrays.sort((Object[])citizenIdNames);
         long[] citizenIds = new long[citizens.size()];
         String[] citizenNames = new String[citizens.size()];

         for(int var58 = 0; var58 < citizens.size(); ++var58) {
            citizenIds[var58] = citizenIdNames[var58].getWurmId();
            citizenNames[var58] = citizenIdNames[var58].getName();
         }

         this.player
            .getCommunicator()
            .sendShowPermissions(
               this.getId(),
               this.hasBackButton,
               this.objectType.getTitle(),
               objectName,
               ownerName,
               canChangeName,
               canChangeOwner,
               isManaged,
               isManageEnabled,
               mayManageText,
               mayManageHover,
               warningText,
               messageOnTick,
               questionOnTick,
               messageUnTick,
               questionUnTick,
               allowAlliesText,
               allowCitizensText,
               allowKingdomText,
               allowEveryoneText,
               allowRolePermissionText,
               header1,
               header2,
               hover,
               trustedNames,
               trustedIds,
               friendNames,
               friendIds,
               mySettlement,
               citizenNames,
               citizenIds,
               permittedNames,
               permittedIds,
               allowed
            );
      }
   }

   public static final boolean containsIllegalCharacters(String legalChars, String name) {
      char[] chars = name.toCharArray();

      for(char lC : chars) {
         if (legalChars.indexOf(lC) < 0) {
            return true;
         }
      }

      return false;
   }

   private class playerIdName implements Comparable<ManagePermissions.playerIdName> {
      final long id;
      final String name;

      playerIdName(long aId) {
         this.id = aId;
         this.name = PermissionsByPlayer.getPlayerOrGroupName(this.id);
      }

      public long getWurmId() {
         return this.id;
      }

      public String getName() {
         return this.name;
      }

      public int compareTo(ManagePermissions.playerIdName pin) {
         return this.getName().compareTo(pin.getName());
      }
   }
}
