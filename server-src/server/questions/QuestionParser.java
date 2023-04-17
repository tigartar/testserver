/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.mesh.BushData;
import com.wurmonline.mesh.FoliageAge;
import com.wurmonline.mesh.GrassData;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.TreeData;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Features;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.Message;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.BehaviourDispatcher;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.behaviours.MethodsItems;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.behaviours.NoSuchBehaviourException;
import com.wurmonline.server.behaviours.WurmPermissions;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreaturePos;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.economy.Change;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.MonetaryConstants;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.items.AdvancedCreationEntry;
import com.wurmonline.server.items.InscriptionData;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.NotOwnedException;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.kingdom.Appointment;
import com.wurmonline.server.kingdom.Appointments;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.players.Cultist;
import com.wurmonline.server.players.Cults;
import com.wurmonline.server.players.Friend;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.players.PlayerJournal;
import com.wurmonline.server.players.Spawnpoint;
import com.wurmonline.server.players.Titles;
import com.wurmonline.server.questions.AllianceQuestion;
import com.wurmonline.server.questions.AltarConversionQuestion;
import com.wurmonline.server.questions.AscensionQuestion;
import com.wurmonline.server.questions.AskConvertQuestion;
import com.wurmonline.server.questions.AskKingdomQuestion;
import com.wurmonline.server.questions.ConvertQuestion;
import com.wurmonline.server.questions.CreatureCreationQuestion;
import com.wurmonline.server.questions.DeclareWarQuestion;
import com.wurmonline.server.questions.FriendQuestion;
import com.wurmonline.server.questions.GateManagementQuestion;
import com.wurmonline.server.questions.GmVillageAdInterface;
import com.wurmonline.server.questions.GuardManagementQuestion;
import com.wurmonline.server.questions.ItemCreationQuestion;
import com.wurmonline.server.questions.ItemDataQuestion;
import com.wurmonline.server.questions.LCMManagementQuestion;
import com.wurmonline.server.questions.LearnSkillQuestion;
import com.wurmonline.server.questions.ManageAllianceQuestion;
import com.wurmonline.server.questions.MultiPriceManageQuestion;
import com.wurmonline.server.questions.PaymentQuestion;
import com.wurmonline.server.questions.PeaceQuestion;
import com.wurmonline.server.questions.PlayerPaymentQuestion;
import com.wurmonline.server.questions.PowerManagementQuestion;
import com.wurmonline.server.questions.PriestQuestion;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.questions.QuestionTypes;
import com.wurmonline.server.questions.RealDeathQuestion;
import com.wurmonline.server.questions.ReputationQuestion;
import com.wurmonline.server.questions.ServerQuestion;
import com.wurmonline.server.questions.SetDeityQuestion;
import com.wurmonline.server.questions.SetKingdomQuestion;
import com.wurmonline.server.questions.ShutDownQuestion;
import com.wurmonline.server.questions.SinglePriceManageQuestion;
import com.wurmonline.server.questions.SpawnQuestion;
import com.wurmonline.server.questions.StructureManagement;
import com.wurmonline.server.questions.TeleportQuestion;
import com.wurmonline.server.questions.TextInputQuestion;
import com.wurmonline.server.questions.TileDataQuestion;
import com.wurmonline.server.questions.TitleCompoundQuestion;
import com.wurmonline.server.questions.TitleQuestion;
import com.wurmonline.server.questions.TraderManagementQuestion;
import com.wurmonline.server.questions.TraderRentalQuestion;
import com.wurmonline.server.questions.VillageCitizenManageQuestion;
import com.wurmonline.server.questions.VillageExpansionQuestion;
import com.wurmonline.server.questions.VillageFoundationQuestion;
import com.wurmonline.server.questions.VillageInfo;
import com.wurmonline.server.questions.VillageJoinQuestion;
import com.wurmonline.server.questions.VillageSettingsManageQuestion;
import com.wurmonline.server.questions.VillageTeleportQuestion;
import com.wurmonline.server.questions.VillageUpkeep;
import com.wurmonline.server.questions.WithdrawMoneyQuestion;
import com.wurmonline.server.skills.Affinities;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.SkillTemplate;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.sounds.SoundPlayer;
import com.wurmonline.server.structures.FenceGate;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.NoSuchWallException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.utils.NameCountList;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.GuardPlan;
import com.wurmonline.server.villages.KosWarning;
import com.wurmonline.server.villages.NoSuchRoleException;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.PvPAlliance;
import com.wurmonline.server.villages.RecruitmentAd;
import com.wurmonline.server.villages.RecruitmentAds;
import com.wurmonline.server.villages.Reputation;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.VillageRole;
import com.wurmonline.server.villages.VillageStatus;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.villages.WarDeclaration;
import com.wurmonline.server.webinterface.WcKingdomChat;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.CounterTypes;
import com.wurmonline.shared.util.StringUtilities;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class QuestionParser
implements QuestionTypes,
CounterTypes,
ItemTypes,
VillageStatus,
TimeConstants,
MonetaryConstants,
MiscConstants,
CreatureTemplateIds {
    private static Logger logger = Logger.getLogger(QuestionParser.class.getName());
    public static final String legalChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'- 1234567890.,+/!() ";
    private static final String numbers = "1234567890";
    public static final String villageLegalChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'- ";

    private QuestionParser() {
    }

    static final boolean containsIllegalCharacters(String name) {
        char[] chars = name.toCharArray();
        for (int x = 0; x < chars.length; ++x) {
            if (legalChars.indexOf(chars[x]) >= 0) continue;
            return true;
        }
        return false;
    }

    public static final boolean containsIllegalVillageCharacters(String name) {
        char[] chars = name.toCharArray();
        for (int x = 0; x < chars.length; ++x) {
            if (villageLegalChars.indexOf(chars[x]) >= 0) continue;
            return true;
        }
        return false;
    }

    static void parseShutdownQuestion(ShutDownQuestion question) {
        block9: {
            int type = question.getType();
            Creature responder = question.getResponder();
            long target = question.getTarget();
            if (type == 13 && (WurmId.getType(target) == 2 || WurmId.getType(target) == 19 || WurmId.getType(target) == 20)) {
                try {
                    Item item = Items.getItem(target);
                    if (item.getTemplateId() == 176 && responder.getPower() >= 3) {
                        String minutest = question.getAnswer().getProperty("minutes");
                        String secondst = question.getAnswer().getProperty("seconds");
                        String reason = question.getAnswer().getProperty("reason");
                        try {
                            int minutes = Integer.parseInt(minutest);
                            int seconds = Integer.parseInt(secondst);
                            String globalStr = question.getAnswer().getProperty("global");
                            boolean global = false;
                            if (globalStr != null && globalStr.equals("true")) {
                                global = true;
                            }
                            if (global) {
                                Servers.startShutdown(responder.getName(), minutes * 60 + seconds, reason);
                            }
                            if (!Servers.isThisLoginServer() && global) break block9;
                            Server.getInstance().startShutdown(minutes * 60 + seconds, reason);
                            logger.log(Level.INFO, responder.getName() + " shutting down server in " + minutes + " minutes and " + seconds + " seconds, reason: " + reason);
                            if (responder.getLogger() != null) {
                                responder.getLogger().log(Level.INFO, responder.getName() + " shutting down server in " + minutes + " minutes and " + seconds + " seconds, reason: " + reason);
                            }
                            break block9;
                        }
                        catch (NumberFormatException nfe) {
                            responder.getCommunicator().sendNormalServerMessage("Failed to parse " + minutest + " or " + secondst + " to a number. Please try again.");
                        }
                        break block9;
                    }
                    logger.log(Level.WARNING, responder.getName() + " managed to try to shutdown with item " + item + ".");
                    responder.getCommunicator().sendNormalServerMessage("You can't shutdown with that. In fact you should not even manage to try. This has been logged.");
                }
                catch (NoSuchItemException nsi) {
                    logger.log(Level.WARNING, "Failed to locate item with id=" + target + " for which shutdown was intended.");
                    responder.getCommunicator().sendNormalServerMessage("Failed to locate that item.");
                }
            }
        }
    }

    static final boolean containsNonNumber(String name) {
        char[] chars = name.toCharArray();
        for (int x = 0; x < chars.length; ++x) {
            if (numbers.indexOf(chars[x]) >= 0) continue;
            return true;
        }
        return false;
    }

    private static final boolean isMultiplierName(String answer) {
        if ((answer.toLowerCase().startsWith("x") || answer.toLowerCase().endsWith("x")) && answer.length() > 1) {
            String rest = answer;
            rest = answer.toLowerCase().startsWith("x") ? answer.substring(1, answer.length()) : answer.substring(0, answer.length() - 1);
            return !QuestionParser.containsNonNumber(rest);
        }
        return false;
    }

    static void parseTextInputQuestion(TextInputQuestion question, Item liquid) {
        block39: {
            long target;
            Creature responder;
            int type;
            block41: {
                block42: {
                    block40: {
                        type = question.getType();
                        responder = question.getResponder();
                        target = question.getTarget();
                        if (type == 0) {
                            logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
                            return;
                        }
                        if (type != 2) break block40;
                        if (liquid != null && !Items.exists(liquid)) {
                            responder.getCommunicator().sendNormalServerMessage("Your " + liquid.getName() + " you started inscribing with has vanished.");
                            return;
                        }
                        try {
                            Item item = Items.getItem(target);
                            if (item.getInscription() != null && responder.getPower() < 2) {
                                responder.getCommunicator().sendNormalServerMessage("The " + item.getName() + " already have an inscription.");
                                return;
                            }
                            String answer = question.getAnswer().getProperty("answer").trim();
                            if (InscriptionData.containsIllegalCharacters(answer)) {
                                responder.getCommunicator().sendNormalServerMessage("The inscription contains some characters that are too complex for you to inscribe.");
                                return;
                            }
                            if (answer.length() == 0) {
                                responder.getCommunicator().sendNormalServerMessage("You decide not to inscribe the " + item.getName() + " at the moment.");
                                return;
                            }
                            int colour = 0;
                            if (liquid != null && liquid.getTemplateId() != 753) {
                                colour = liquid.color;
                            }
                            item.setInscription(answer, responder.getName(), colour);
                            responder.getCommunicator().sendNormalServerMessage("You carefully inscribe the " + item.getName() + " with " + answer.length() + " printed letters.");
                            if (liquid != null) {
                                liquid.setWeight(liquid.getWeightGrams() - 10, true);
                            }
                            break block39;
                        }
                        catch (NoSuchItemException nsi) {
                            logger.log(Level.WARNING, "Failed to locate item with id=" + target + " for which an inscription was intended.");
                            responder.getCommunicator().sendNormalServerMessage("Failed to locate that item.");
                        }
                        break block39;
                    }
                    if (type != 1 || target == -10L) break block41;
                    if (WurmId.getType(target) != 2 && WurmId.getType(target) != 19 && WurmId.getType(target) != 20) break block42;
                    try {
                        Item item = Items.getItem(target);
                        if (!item.isNoRename()) {
                            int templateId = item.getTemplateId();
                            String answer = question.getAnswer().getProperty("answer");
                            if (QuestionParser.containsIllegalCharacters(answer)) {
                                responder.getCommunicator().sendNormalServerMessage("The name contains illegal characters.");
                                return;
                            }
                            boolean updated = false;
                            if (item.isSign()) {
                                byte bt;
                                int mod = 1;
                                if (templateId == 209) {
                                    mod = 2;
                                }
                                int maxSize = Math.max(5, (int)((float)(item.getRarity() * 3) + item.getCurrentQualityLevel() * (float)mod));
                                if (answer.length() > maxSize) {
                                    responder.getCommunicator().sendSafeServerMessage("The text is too long. Only " + maxSize + " letters can be imprinted on this sign.");
                                }
                                answer = answer.substring(0, Math.min(answer.length(), maxSize));
                                String stype = question.getAnswer().getProperty("data1");
                                if (stype != null && stype.length() > 0 && (bt = Byte.parseByte(stype)) > 0 && bt <= 22) {
                                    VolaTile t;
                                    item.setAuxData(bt);
                                    if (!item.setDescription(answer) && item.getZoneId() > 0 && item.getParentId() == -10L && (t = Zones.getTileOrNull(item.getTileX(), item.getTileY(), item.isOnSurface())) != null) {
                                        t.renameItem(item);
                                    }
                                    updated = true;
                                }
                            }
                            if (templateId == 521 && responder.getPower() > 0) {
                                item.setName(answer);
                                break block39;
                            }
                            if (templateId == 651) {
                                if (question.getOldtext().length() > 0 && !item.getCreatorName().toLowerCase().equals(responder.getName().toLowerCase())) {
                                    responder.getCommunicator().sendNormalServerMessage("You can't change the recipient of the gift.");
                                    return;
                                }
                                item.setDescription(answer);
                                item.setName("From " + item.getSignature() + " to " + answer);
                                break block39;
                            }
                            if (templateId == 824) {
                                if (answer.length() == 0) {
                                    responder.getCommunicator().sendNormalServerMessage("Groups must have a name.");
                                }
                                item.setName(answer);
                                break block39;
                            }
                            if (templateId == 1128) {
                                if (answer.length() == 0) {
                                    responder.getCommunicator().sendNormalServerMessage("Almanac folders must have a name.");
                                }
                                item.setName(answer);
                                break block39;
                            }
                            if (QuestionParser.isMultiplierName(answer)) {
                                responder.getCommunicator().sendNormalServerMessage("Starting or ending the description with x indicates a multiplier, which is not allowed.");
                                return;
                            }
                            if (updated) break block39;
                            item.setDescription(answer);
                            break block39;
                        }
                        logger.log(Level.WARNING, responder.getName() + " managed to try to rename item " + item.getName() + " which is non-renamable.");
                        responder.getCommunicator().sendNormalServerMessage("You can't rename that.");
                    }
                    catch (NoSuchItemException nsi) {
                        logger.log(Level.WARNING, "Failed to locate item with id=" + target + " for which name change was intended.");
                        responder.getCommunicator().sendNormalServerMessage("Failed to locate that item.");
                    }
                    break block39;
                }
                if (WurmId.getType(target) != 4) break block39;
                try {
                    Structure structure = Structures.getStructure(target);
                    if (structure.isTypeHouse()) {
                        Item writ = Items.getItem(structure.getWritId());
                        if (responder.getPower() < 2 && writ.getOwnerId() != responder.getWurmId()) {
                            return;
                        }
                    }
                    logger.log(Level.INFO, "Setting structure " + structure.getName() + " to " + question.getAnswer().getProperty("answer"));
                    String answer = question.getAnswer().getProperty("answer");
                    if (QuestionParser.containsIllegalCharacters(answer)) {
                        responder.getCommunicator().sendNormalServerMessage("The name contains illegal characters.");
                        return;
                    }
                    if (!structure.getName().equals(answer)) {
                        structure.setName(answer, true);
                    }
                    break block39;
                }
                catch (NoSuchStructureException nss) {
                    logger.log(Level.WARNING, "Failed to locate structure with id=" + target + " for which name change was intended.");
                }
                catch (NoSuchItemException nsi) {
                    logger.log(Level.WARNING, "Failed to locate writ for structure with id=" + target + ".");
                }
                break block39;
            }
            if (type == 1 && target == -10L) {
                String answer = question.getAnswer().getProperty("answer").trim();
                if (QuestionParser.containsIllegalCharacters(answer)) {
                    responder.getCommunicator().sendNormalServerMessage("The name contains illegal characters.");
                    return;
                }
                if (QuestionParser.isMultiplierName(answer)) {
                    responder.getCommunicator().sendNormalServerMessage("Starting or ending the description with x indicates a multiplier, which is not allowed.");
                    return;
                }
                for (Item item : question.getItems()) {
                    if (!item.isNoRename()) {
                        int templateId = item.getTemplateId();
                        if (templateId != 521 && templateId != 651 && templateId != 824 && !item.isCoin()) {
                            item.setDescription(answer);
                            continue;
                        }
                        responder.getCommunicator().sendNormalServerMessage("Cannot rename " + item.getName() + ".");
                        continue;
                    }
                    responder.getCommunicator().sendNormalServerMessage("Cannot rename " + item.getName() + ".");
                }
            }
        }
    }

    static void parseStructureManagement(StructureManagement question) {
        int type = question.getType();
        Creature responder = question.getResponder();
        long target = question.getTarget();
        if (type == 0) {
            logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
            return;
        }
        if (WurmId.getType(target) == 4) {
            try {
                Structure structure = Structures.getStructure(target);
                Item writ = Items.getItem(structure.getWritId());
                if (writ.getOwnerId() != responder.getWurmId()) {
                    return;
                }
                Properties props = question.getAnswer();
                for (String string : props.keySet()) {
                    String name;
                    String val;
                    if (string.equals("demolish") && props.get(string).equals("true")) {
                        structure.totallyDestroy();
                        return;
                    }
                    if (string.equals("allowAllies")) {
                        if (props.get(string) != null && props.get(string).equals("true")) {
                            structure.setAllowAllies(true);
                            continue;
                        }
                        structure.setAllowAllies(false);
                        continue;
                    }
                    if (string.equals("allowVillagers")) {
                        if (props.get(string) != null && props.get(string).equals("true")) {
                            structure.setAllowVillagers(true);
                            continue;
                        }
                        structure.setAllowVillagers(false);
                        continue;
                    }
                    if (string.equals("allowKingdom")) {
                        if (props.get(string) != null && props.get(string).equals("true")) {
                            structure.setAllowKingdom(true);
                            continue;
                        }
                        structure.setAllowKingdom(false);
                        continue;
                    }
                    if (string.charAt(0) == 'f') {
                        boolean set = false;
                        try {
                            val = props.getProperty(string);
                            set = val.equals("true");
                        }
                        catch (Exception ex) {
                            logger.log(Level.WARNING, "Failed to set " + props.getProperty(string) + " to a boolean.");
                        }
                        if (!set) continue;
                        String fis = string.substring(1, string.length());
                        try {
                            long fid = Long.parseLong(fis);
                            structure.addGuest(fid, 42);
                        }
                        catch (Exception ex) {
                            logger.log(Level.WARNING, "Faiiled to add guest " + fis, ex);
                        }
                        continue;
                    }
                    if (string.charAt(0) == 'g') {
                        boolean set = false;
                        try {
                            val = props.getProperty(string);
                            set = val.equals("true");
                        }
                        catch (Exception ex) {
                            logger.log(Level.WARNING, "Failed to set " + props.getProperty(string) + " to a boolean.");
                        }
                        if (!set) continue;
                        String gis = string.substring(1, string.length());
                        try {
                            long gid = Long.parseLong(gis);
                            structure.removeGuest(gid);
                        }
                        catch (Exception ex) {
                            logger.log(Level.WARNING, "Faiiled to remove guest " + gis, ex);
                        }
                        continue;
                    }
                    if (string.equals("lock")) {
                        boolean set = false;
                        try {
                            val = props.getProperty(string);
                            set = val.equals("true");
                        }
                        catch (Exception ex) {
                            logger.log(Level.WARNING, "Failed to set " + props.getProperty(string) + " to a boolean.");
                        }
                        if (!set) continue;
                        structure.lockAllDoors();
                        continue;
                    }
                    if (string.equals("unlock")) {
                        boolean set = false;
                        try {
                            val = props.getProperty(string);
                            set = val.equals("true");
                        }
                        catch (Exception ex) {
                            logger.log(Level.WARNING, "Failed to set " + props.getProperty(string) + " to a boolean.");
                        }
                        if (!set) continue;
                        structure.unlockAllDoors();
                        continue;
                    }
                    if (!string.equals("sname") || (name = props.getProperty("sname")) == null || name.equals(structure.getName())) continue;
                    if (name.length() >= 41) {
                        name = name.substring(0, 39);
                        responder.getCommunicator().sendNormalServerMessage("The name has been truncated to " + name + ".");
                        continue;
                    }
                    if (name.length() < 3) {
                        responder.getCommunicator().sendSafeServerMessage("Please select a longer name.");
                        continue;
                    }
                    if (QuestionParser.containsIllegalCharacters(name)) {
                        responder.getCommunicator().sendSafeServerMessage("The name " + name + " contain illegal characters. Please select another name.");
                        continue;
                    }
                    structure.setName(name, false);
                    try {
                        writ = Items.getItem(structure.getWritId());
                        writ.setDescription(name);
                    }
                    catch (NoSuchItemException nsi) {
                        logger.log(Level.WARNING, "Structure " + target + " has no writ with id " + structure.getWritId() + "?", nsi);
                    }
                }
                try {
                    structure.save();
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, "Failed to save structure " + target, iox);
                }
            }
            catch (NoSuchStructureException nss) {
                logger.log(Level.WARNING, "Failed to locate structure with id=" + target + " for which name change was intended.", nss);
            }
            catch (NoSuchItemException nsi) {
                logger.log(Level.WARNING, "Failed to locate writ for structure with id=" + target + ".", nsi);
            }
        }
    }

    static void parseItemDataQuestion(ItemDataQuestion question) {
        block46: {
            int type = question.getType();
            Creature responder = question.getResponder();
            long target = question.getTarget();
            if (type == 0) {
                logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
                return;
            }
            if (type == 4 && (WurmId.getType(target) == 2 || WurmId.getType(target) == 19 || WurmId.getType(target) == 20)) {
                String name = question.getAnswer().getProperty("itemName");
                String d1 = question.getAnswer().getProperty("data1");
                String d2 = question.getAnswer().getProperty("data2");
                String e1 = question.getAnswer().getProperty("extra1");
                String e2 = question.getAnswer().getProperty("extra2");
                String aux = "?";
                try {
                    String fruit;
                    int extra2;
                    Item item = Items.getItem(target);
                    String extra = "";
                    if (name != null) {
                        item.setName(name, true);
                    }
                    int data1 = d1 == null ? -1 : Integer.parseInt(d1);
                    int data2 = d2 == null ? -1 : Integer.parseInt(d2);
                    int extra1 = e1 == null ? -1 : Integer.parseInt(e1);
                    int n = extra2 = e2 == null ? -1 : Integer.parseInt(e2);
                    if (item.hasData()) {
                        item.setAllData(data1, data2, extra1, extra2);
                    }
                    byte auxd = 0;
                    if (item.usesFoodState()) {
                        String raux = question.getAnswer().getProperty("raux");
                        auxd = Byte.parseByte(raux);
                        if (Boolean.parseBoolean(question.getAnswer().getProperty("chopped"))) {
                            auxd = (byte)(auxd + 16);
                        }
                        if (Boolean.parseBoolean(question.getAnswer().getProperty("mashed"))) {
                            auxd = (byte)(auxd + 32);
                        }
                        if (Boolean.parseBoolean(question.getAnswer().getProperty("wrap"))) {
                            auxd = (byte)(auxd + 64);
                        }
                        if (Boolean.parseBoolean(question.getAnswer().getProperty("fresh"))) {
                            auxd = (byte)(auxd + 128);
                        }
                    } else {
                        aux = question.getAnswer().getProperty("aux");
                        auxd = (byte)Integer.parseInt(aux);
                    }
                    item.setAuxData(auxd);
                    String val = question.getAnswer().getProperty("dam");
                    if (val != null) {
                        try {
                            item.setDamage(Float.parseFloat(val));
                            extra = extra + ", dam=" + val;
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    if ((val = question.getAnswer().getProperty("temp")) != null) {
                        try {
                            item.setTemperature(Short.parseShort(val));
                            extra = extra + ", temp=" + val;
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    if ((val = question.getAnswer().getProperty("weight")) != null) {
                        try {
                            if (Integer.parseInt(val) <= 0) {
                                responder.getCommunicator().sendNormalServerMessage("Weight cannot be below 1.");
                            } else {
                                item.setWeight(Integer.parseInt(val), false);
                                extra = extra + ", weight=" + val;
                            }
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    if ((val = question.getAnswer().getProperty("rarity")) != null) {
                        try {
                            byte vals = Byte.parseByte(val);
                            if (vals < 0) {
                                vals = 0;
                            }
                            if (vals > 3) {
                                vals = 3;
                            }
                            extra = extra + ", rarity=" + vals;
                            item.setRarity(vals);
                        }
                        catch (Exception vals) {
                            // empty catch block
                        }
                    }
                    if ((fruit = question.getAnswer().getProperty("fruit")) != null) {
                        ItemTemplate template = question.getTemplate(Integer.parseInt(fruit));
                        if (template != null && template.getTemplateId() != item.getRealTemplateId()) {
                            item.setRealTemplate(template.getTemplateId());
                            extra = " and " + template.getName();
                            responder.getCommunicator().sendUpdateInventoryItem(item);
                        }
                        if (template == null && item.getRealTemplateId() != -10 && item.getTemplateId() != 1307) {
                            item.setRealTemplate(-10);
                            item.setName("fruit juice");
                            responder.getCommunicator().sendUpdateInventoryItem(item);
                        }
                    }
                    String red = question.getAnswer().getProperty("c_red");
                    String green = question.getAnswer().getProperty("c_green");
                    String blue = question.getAnswer().getProperty("c_blue");
                    String tickp = question.getAnswer().getProperty("primary");
                    try {
                        int r = Integer.parseInt(red);
                        int g = Integer.parseInt(green);
                        int b = Integer.parseInt(blue);
                        boolean tick = Boolean.parseBoolean(tickp);
                        if (tick) {
                            item.setColor(WurmColor.createColor(r < 0 ? 0 : r, g < 0 ? 0 : g, b < 0 ? 0 : b));
                            extra = extra + ", color=[R:" + r + " G:" + g + " B:" + b + "]";
                        } else {
                            item.setColor(-1);
                            extra = extra + ", color=none";
                        }
                    }
                    catch (NullPointerException | NumberFormatException e) {
                        item.setColor(-1);
                        extra = extra + ", color=none";
                    }
                    String tick2 = question.getAnswer().getProperty("secondary");
                    if (tick2 != null) {
                        String red2 = question.getAnswer().getProperty("c2_red");
                        String green2 = question.getAnswer().getProperty("c2_green");
                        String blue2 = question.getAnswer().getProperty("c2_blue");
                        try {
                            int r = Integer.parseInt(red2);
                            int g = Integer.parseInt(green2);
                            int b = Integer.parseInt(blue2);
                            boolean tick = Boolean.parseBoolean(tick2);
                            if (tick) {
                                item.setColor2(WurmColor.createColor(r < 0 ? 0 : r, g < 0 ? 0 : g, b < 0 ? 0 : b));
                                extra = extra + ", color2=[R:" + r + " G:" + g + " B:" + b + "]";
                            } else {
                                item.setColor2(-1);
                                extra = extra + ", color2=none";
                            }
                        }
                        catch (NullPointerException | NumberFormatException e) {
                            item.setColor2(-1);
                            extra = extra + ", color2=none";
                        }
                    }
                    if (responder.getPower() >= 5) {
                        long lastMaintained = Long.parseLong(question.getAnswer().getProperty("lastMaintained"));
                        extra = extra + ", lastMaintained=" + lastMaintained;
                        item.setLastMaintained(lastMaintained);
                    }
                    String lastOwner = question.getAnswer().getProperty("lastowner");
                    try {
                        long lastOwnerId = Long.parseLong(lastOwner);
                        if (lastOwnerId != item.getLastOwnerId()) {
                            extra = extra + ", lastowner=" + lastOwnerId;
                            item.setLastOwnerId(lastOwnerId);
                        }
                    }
                    catch (NullPointerException | NumberFormatException runtimeException) {
                        // empty catch block
                    }
                    responder.getCommunicator().sendNormalServerMessage("You quietly mumble: " + data1 + ", " + data2 + " as well as " + auxd + extra);
                    if (responder.getLogger() != null) {
                        responder.getLogger().info("Sets item data of " + target + " (" + item.getName() + ") to : " + data1 + ", " + data2 + ", and aux: " + auxd + extra);
                    }
                }
                catch (NoSuchItemException nsi) {
                    logger.log(Level.WARNING, "Failed to locate item with id=" + target + " for which data change was intended.", nsi);
                    responder.getCommunicator().sendNormalServerMessage("Failed to locate that item.");
                }
                catch (NumberFormatException nfe) {
                    responder.getCommunicator().sendNormalServerMessage("You realize that something doesn't match your requirements. Did you mistype a number?");
                    question.getAnswer().forEach((BiConsumer<? super Object, ? super Object>)((BiConsumer<Object, Object>)(k, v) -> responder.getCommunicator().sendNormalServerMessage(k + " = " + v)));
                    if (!logger.isLoggable(Level.FINE)) break block46;
                    logger.log(Level.FINE, responder.getName() + " realises that data1: " + d1 + ", data2: " + d2 + " or aux: " + aux + " doesn't match their requirements. " + nfe, nfe);
                }
            }
        }
    }

    static void parseTileDataQuestion(TileDataQuestion question) {
        block30: {
            int type = question.getType();
            Creature responder = question.getResponder();
            long target = question.getTarget();
            int tilex = question.getTilex();
            int tiley = question.getTiley();
            if (type == 0) {
                logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
                return;
            }
            if (type == 35 && (WurmId.getType(target) == 2 || WurmId.getType(target) == 19 || WurmId.getType(target) == 20)) {
                String d1 = question.getAnswer().getProperty("surf");
                String d2 = question.getAnswer().getProperty("cave");
                boolean bot = Boolean.parseBoolean(question.getAnswer().getProperty("bot"));
                boolean forage = Boolean.parseBoolean(question.getAnswer().getProperty("forage"));
                boolean collect = Boolean.parseBoolean(question.getAnswer().getProperty("collect"));
                boolean transforming = Boolean.parseBoolean(question.getAnswer().getProperty("transforming"));
                boolean transformed = Boolean.parseBoolean(question.getAnswer().getProperty("transformed"));
                boolean hive = Boolean.parseBoolean(question.getAnswer().getProperty("hive"));
                boolean hasGrubs = Boolean.parseBoolean(question.getAnswer().getProperty("hasGrubs"));
                byte serverCaveFlags = Byte.parseByte(question.getAnswer().getProperty("caveserverflag"));
                byte clientCaveFlags = Byte.parseByte(question.getAnswer().getProperty("caveclientflag"));
                short sHeight = Short.parseShort(question.getAnswer().getProperty("surfaceheight"));
                short rHeight = Short.parseShort(question.getAnswer().getProperty("rockheight"));
                short cHeight = Short.parseShort(question.getAnswer().getProperty("caveheight"));
                byte cceil = Byte.parseByte(question.getAnswer().getProperty("caveceiling"));
                int surfMesh = Server.surfaceMesh.getTile(tilex, tiley);
                byte surfType = Tiles.decodeType(surfMesh);
                short surfHeight = Tiles.decodeHeight(surfMesh);
                short rockHeight = Tiles.decodeHeight(Server.rockMesh.getTile(tilex, tiley));
                int caveMesh = Server.caveMesh.getTile(tilex, tiley);
                short caveHeight = Tiles.decodeHeight(caveMesh);
                byte caveCeiling = Tiles.decodeData(caveMesh);
                boolean updateCave = false;
                Tiles.Tile tile = Tiles.getTile(surfType);
                try {
                    int data1 = Integer.parseInt(d1);
                    int data2 = Integer.parseInt(d2);
                    if (data1 >= 0 && ((surfType == 7 || surfType == 43) && data1 <= 2047 || data1 <= 65535)) {
                        int count;
                        if (surfType == 7 || surfType == 43) {
                            count = Integer.parseInt(question.getAnswer().getProperty("count"));
                            Server.setWorldResource(tilex, tiley, (count & 0x1F) << 11 | data1);
                        } else if (surfType == 1 || surfType == 2 || surfType == 10 || surfType == 22 || surfType == 6 || surfType == 18 || surfType == 24) {
                            count = Integer.parseInt(question.getAnswer().getProperty("qlcnt"));
                            Server.setWorldResource(tilex, tiley, ((count & 0xFF) << 8) + (data1 & 0xFF));
                        } else {
                            Server.setWorldResource(tilex, tiley, data1);
                        }
                    } else {
                        responder.getCommunicator().sendNormalServerMessage("Surface resource must be 0-32767");
                    }
                    Server.setBotanizable(tilex, tiley, bot);
                    Server.setForagable(tilex, tiley, forage);
                    Server.setGatherable(tilex, tiley, collect);
                    Server.setBeingTransformed(tilex, tiley, transforming);
                    Server.setTransformed(tilex, tiley, transformed);
                    Server.setCheckHive(tilex, tiley, hive);
                    Server.setGrubs(tilex, tiley, hasGrubs);
                    Server.setClientCaveFlags(tilex, tiley, clientCaveFlags);
                    Server.setServerCaveFlags(tilex, tiley, serverCaveFlags);
                    if (data2 <= 65535 && data2 >= 0) {
                        Server.setCaveResource(tilex, tiley, data2);
                    } else {
                        responder.getCommunicator().sendNormalServerMessage("Cave resource must be 0-65535");
                    }
                    if (Math.abs(sHeight - surfHeight) <= 300) {
                        surfHeight = sHeight;
                    } else {
                        responder.getCommunicator().sendNormalServerMessage("Unable to change the surface layer height by more than 300 dirt.");
                    }
                    if (Math.abs(rHeight - rockHeight) <= 300) {
                        rockHeight = rHeight;
                    } else {
                        responder.getCommunicator().sendNormalServerMessage("Unable to change the rock layer height by more than 300 dirt.");
                    }
                    if (rockHeight > surfHeight) {
                        surfHeight = rockHeight;
                    }
                    if (caveHeight != cHeight || caveCeiling != cceil) {
                        caveHeight = cHeight;
                        caveCeiling = cceil;
                        updateCave = true;
                    }
                    byte surfaceTileData = 0;
                    if (tile.isGrass() || surfType == 10) {
                        int ggrowth = Integer.parseInt(question.getAnswer().getProperty("growth"));
                        int gflower = Integer.parseInt(question.getAnswer().getProperty("flower"));
                        surfaceTileData = (byte)((ggrowth & 3) << 6 | gflower & 0xF & 0xFF);
                    } else if (surfType == 31 || surfType == 34 || surfType == 3 || surfType == 14 || surfType == 35 || surfType == 11) {
                        int tage = Integer.parseInt(question.getAnswer().getProperty("age"));
                        int ttype = Integer.parseInt(question.getAnswer().getProperty("type"));
                        surfaceTileData = surfType == 31 || surfType == 34 || surfType == 35 ? (byte)(BushData.BushType.encodeTileData(tage, ttype) & 0xFF) : (byte)(TreeData.TreeType.encodeTileData(tage, ttype) & 0xFF);
                    } else if (tile.usesNewData()) {
                        byte tage = Byte.parseByte(question.getAnswer().getProperty("age"));
                        int harvestable = Integer.parseInt(question.getAnswer().getProperty("harvestable"));
                        int incentre = Integer.parseInt(question.getAnswer().getProperty("incentre"));
                        int growth = Integer.parseInt(question.getAnswer().getProperty("growth"));
                        FoliageAge age = FoliageAge.fromByte(tage);
                        GrassData.GrowthTreeStage grass = GrassData.GrowthTreeStage.fromInt(growth);
                        surfaceTileData = Tiles.encodeTreeData(age, harvestable != 0, incentre != 0, grass);
                    } else if (surfType == 7 || surfType == 43) {
                        boolean ftended = Boolean.parseBoolean(question.getAnswer().getProperty("tended"));
                        int fage = Integer.parseInt(question.getAnswer().getProperty("age"));
                        int fcrop = Integer.parseInt(question.getAnswer().getProperty("crop"));
                        surfaceTileData = (byte)((ftended ? 128 : 0) | (fage & 7) << 4 | fcrop & 0xF & 0xFF);
                    } else if (Tiles.isRoadType(surfType)) {
                        int funused = Integer.parseInt(question.getAnswer().getProperty("unused"));
                        int fdir = Integer.parseInt(question.getAnswer().getProperty("dir"));
                        surfaceTileData = (byte)((funused & 0x1F) << 3 | fdir & 7 & 0xFF);
                    } else {
                        String d3 = question.getAnswer().getProperty("surftiledata");
                        surfaceTileData = Byte.parseByte(d3);
                    }
                    Server.setSurfaceTile(tilex, tiley, surfHeight, surfType, surfaceTileData);
                    Server.rockMesh.setTile(tilex, tiley, Tiles.encode(rockHeight, (short)0));
                    if (updateCave) {
                        byte ctype = Tiles.decodeType(Server.caveMesh.getTile(tilex, tiley));
                        Server.caveMesh.setTile(tilex, tiley, Tiles.encode(caveHeight, ctype, caveCeiling));
                    }
                    int surf = Server.getWorldResource(tilex, tiley);
                    byte flag = Server.getServerSurfaceFlags(tilex, tiley);
                    responder.getCommunicator().sendNormalServerMessage("You quietly mumble: " + tilex + "," + tiley + ":flags:" + flag + ":d1:" + surf + ":d2:" + data2 + "," + (surfaceTileData & 0xFF));
                    if (responder.getLogger() != null) {
                        responder.getLogger().info("Sets tile data of " + tilex + "," + tiley + " to flags:" + flag + ":d1:" + surf + ":d2:" + data2 + "," + (surfaceTileData & 0xFF));
                    }
                    Players.getInstance().sendChangedTile(tilex, tiley, true, true);
                }
                catch (NumberFormatException nfe) {
                    responder.getCommunicator().sendNormalServerMessage("You realize that " + d1 + " or " + d2 + " doesn't match your requirements.");
                    if (!logger.isLoggable(Level.FINE)) break block30;
                    logger.log(Level.FINE, responder.getName() + " realises that surface resource: " + d1 + " or cave resource: " + d2 + " doesn't match their requirements. " + nfe, nfe);
                }
            }
        }
    }

    static void parseTeleportQuestion(TeleportQuestion question) {
        block23: {
            int type = question.getType();
            Creature responder = question.getResponder();
            long target = question.getTarget();
            if (type == 0) {
                logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
                return;
            }
            if (type == 17 && (WurmId.getType(target) == 2 || WurmId.getType(target) == 19 || WurmId.getType(target) == 20)) {
                String d1 = question.getAnswer().getProperty("data1");
                String d2 = question.getAnswer().getProperty("data2");
                String wid = question.getAnswer().getProperty("wurmid");
                String vid = question.getAnswer().getProperty("villid");
                if (d1.equals("-1") || d2.equals("-1")) {
                    try {
                        Village village;
                        int vidd = Integer.parseInt(vid);
                        if (vidd == 0) {
                            int listid = Integer.parseInt(wid);
                            Player p = question.getPlayer(listid);
                            if (p == null) {
                                responder.getCommunicator().sendNormalServerMessage("No player found.");
                                return;
                            }
                            int tx = p.getTileX();
                            int ty = p.getTileY();
                            responder.setTeleportLayer(p.isOnSurface() ? 0 : -1);
                            responder.setTeleportFloorLevel(p.getFloorLevel());
                            Item item = Items.getItem(target);
                            if (!p.isOnSurface() && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(tx, ty)))) {
                                responder.getCommunicator().sendNormalServerMessage("The tile " + tx + ", " + ty + " is solid cave.");
                                return;
                            }
                            item.setData(tx, ty);
                            responder.getLogger().log(Level.INFO, "Located " + p.getName() + " at " + tx + ", " + ty);
                            responder.getCommunicator().sendNormalServerMessage("You quietly mumble: " + p.getName() + "... " + tx + ", " + ty);
                            if (responder.getPower() >= 5) {
                                try {
                                    Zone z = Zones.getZone(tx, ty, responder.getTeleportLayer() >= 0);
                                    responder.getCommunicator().sendNormalServerMessage("That zone is number " + z.getId() + " x=" + z.getStartX() + " to " + z.getEndX() + " and " + z.getStartY() + " to " + z.getEndY() + ". Size=" + z.getSize());
                                }
                                catch (Exception e) {
                                    responder.getCommunicator().sendNormalServerMessage("Exception: " + e.getMessage());
                                    logger.warning(responder.getName() + " had problems getting zone information while teleporting, data1: " + tx + ", data2: " + ty + ", layer: " + responder.getTeleportLayer() + ", wurmid: " + wid + ", villageid: " + vid);
                                }
                            }
                            break block23;
                        }
                        if ((village = question.getVillage(--vidd)) == null) {
                            responder.getCommunicator().sendNormalServerMessage("No village found.");
                            return;
                        }
                        int tx = village.getTokenX();
                        int ty = village.getTokenY();
                        Item item = Items.getItem(target);
                        responder.setTeleportLayer(village.isOnSurface() ? 0 : -1);
                        if (!village.isOnSurface() && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(tx, ty)))) {
                            responder.getCommunicator().sendNormalServerMessage("The tile " + tx + ", " + ty + " is solid cave.");
                            return;
                        }
                        item.setData(tx, ty);
                        responder.getCommunicator().sendNormalServerMessage("You quietly mumble: " + village.getName() + "... " + tx + ", " + ty);
                    }
                    catch (NoSuchItemException nsi) {
                        logger.log(Level.WARNING, "Failed to locate item with id=" + target + " for which name change was intended.", nsi);
                        responder.getCommunicator().sendNormalServerMessage("Failed to locate that item.");
                    }
                    catch (NumberFormatException nfe) {
                        responder.getCommunicator().sendNormalServerMessage("You realize that the player id " + wid + " doesn't match your requirements.");
                        if (logger.isLoggable(Level.FINE)) {
                            logger.log(Level.FINE, responder.getName() + " realises that player id " + wid + " doesn't match their requirements. " + nfe, nfe);
                        }
                        break block23;
                    }
                }
                try {
                    Item item = Items.getItem(target);
                    int data1 = Integer.parseInt(d1);
                    int data2 = Integer.parseInt(d2);
                    String surfaced = question.getAnswer().getProperty("layer");
                    int layer = 0;
                    if (surfaced != null && surfaced.equals("1")) {
                        layer = -1;
                    }
                    responder.setTeleportLayer(layer);
                    if (layer < 0 && Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.getTile(data1, data2)))) {
                        responder.getCommunicator().sendNormalServerMessage("The tile " + data1 + ", " + data2 + " is solid cave.");
                        return;
                    }
                    item.setData(data1, data2);
                    responder.getCommunicator().sendNormalServerMessage("You quietly mumble: " + data1 + ", " + data2 + " surfaced=" + (layer == 0));
                    if (responder.getPower() >= 5) {
                        try {
                            Zone z = Zones.getZone(data1, data2, layer == 0);
                            responder.getCommunicator().sendNormalServerMessage("That zone is number " + z.getId() + " x=" + z.getStartX() + " to " + z.getEndX() + " and " + z.getStartY() + " to " + z.getEndY() + ". Size=" + z.getSize());
                        }
                        catch (Exception e) {
                            responder.getCommunicator().sendNormalServerMessage("Exception: " + e.getMessage());
                            logger.warning(responder.getName() + " had problems getting zone information while teleporting, data1: " + data1 + ", data2: " + data2 + ", layer: " + layer + ", wurmid: " + wid + ", villageid: " + vid);
                        }
                    }
                }
                catch (NoSuchItemException nsi) {
                    logger.log(Level.WARNING, "Failed to locate item with id=" + target + " for which name change was intended.", nsi);
                    responder.getCommunicator().sendNormalServerMessage("Failed to locate that item.");
                }
                catch (NumberFormatException nfe) {
                    responder.getCommunicator().sendNormalServerMessage("You realize that " + d1 + " or " + d2 + " doesn't match your requirements.");
                    if (!logger.isLoggable(Level.FINE)) break block23;
                    logger.log(Level.FINE, responder.getName() + " realises that " + d1 + " or " + d2 + " doesn't match their requirements. " + nfe, nfe);
                }
            }
        }
    }

    static void parseItemCreationQuestion(ItemCreationQuestion question) {
        block69: {
            int type = question.getType();
            Creature responder = question.getResponder();
            long target = question.getTarget();
            if (type == 0) {
                logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
                return;
            }
            if (type == 5 && (WurmId.getType(target) == 2 || WurmId.getType(target) == 19 || WurmId.getType(target) == 20)) {
                String d1 = question.getAnswer().getProperty("data1");
                String d2 = question.getAnswer().getProperty("data2");
                String sm = question.getAnswer().getProperty("sizemod");
                String number = question.getAnswer().getProperty("number");
                String materialString = question.getAnswer().getProperty("material");
                String alltypes = question.getAnswer().getProperty("alltypes");
                boolean allMaterialTypes = alltypes != null && alltypes.equals("true");
                int maxWoodTypes = 16;
                byte[] woodTypes = MethodsItems.getAllNormalWoodTypes();
                byte[] metalTypes = MethodsItems.getAllMetalTypes();
                String rareString = question.getAnswer().getProperty("rare");
                byte material = 0;
                try {
                    Item item = Items.getItem(target);
                    long now = System.currentTimeMillis();
                    if (Servers.localServer.testServer || (item.getTemplateId() == 176 || item.getTemplateId() == 301) && (responder.getPower() >= 2 || Players.isArtist(question.getResponder().getWurmId(), false, false))) {
                        int data1 = Integer.parseInt(d1);
                        int data2 = Integer.parseInt(d2);
                        int num = Integer.parseInt(number);
                        byte rare = Byte.parseByte(rareString);
                        String name = question.getAnswer().getProperty("itemName");
                        String red = question.getAnswer().getProperty("c_red");
                        String green = question.getAnswer().getProperty("c_green");
                        String blue = question.getAnswer().getProperty("c_blue");
                        int colour = -1;
                        if (red != null && green != null && blue != null && red.length() > 0 && green.length() > 0 && blue.length() > 0) {
                            try {
                                int r = Integer.parseInt(red);
                                int g = Integer.parseInt(green);
                                int b = Integer.parseInt(blue);
                                colour = WurmColor.createColor(r < 0 ? 0 : r, g < 0 ? 0 : g, b < 0 ? 0 : b);
                            }
                            catch (NullPointerException | NumberFormatException e) {
                                logger.log(Level.WARNING, "Bad colours:" + red + "," + green + "," + blue);
                            }
                        }
                        try {
                            material = Byte.parseByte(materialString);
                        }
                        catch (NumberFormatException nfe) {
                            logger.log(Level.WARNING, "Material was " + materialString);
                        }
                        ItemTemplate template = question.getTemplate(data1);
                        if (template == null) {
                            responder.getCommunicator().sendNormalServerMessage("You decide not to create anything.");
                            return;
                        }
                        float sizemod = 1.0f;
                        if (sm != null && sm.length() > 0) {
                            try {
                                sizemod = Math.abs(Float.parseFloat(sm));
                                if (template.getTemplateId() != 995) {
                                    sizemod = Math.min(5.0f, sizemod);
                                }
                            }
                            catch (NumberFormatException nfen1) {
                                responder.getCommunicator().sendAlertServerMessage("The size mod " + sm + " is not a float value.");
                            }
                        }
                        if (template.getTemplateId() != 179 && template.getTemplateId() != 386) {
                            if (num != 1 || material != 0) {
                                allMaterialTypes = false;
                            }
                            if (!template.isWood() && !template.isMetal()) {
                                allMaterialTypes = false;
                            }
                            if (num == 1 && allMaterialTypes) {
                                num = template.isWood() ? (template.getTemplateId() == 65 || template.getTemplateId() == 413 ? woodTypes.length : maxWoodTypes) : metalTypes.length;
                            }
                            for (int x = 0; x < num; ++x) {
                                data2 = Math.min(100, data2);
                                data2 = Math.max(1, data2);
                                Item newItem = null;
                                if (!Servers.localServer.testServer && responder.getPower() <= 3) {
                                    if (material == 0) {
                                        material = template.getMaterial();
                                    }
                                    newItem = ItemFactory.createItem(387, data2, material, rare, responder.getName());
                                    newItem.setRealTemplate(template.getTemplateId());
                                    if (template.getTemplateId() == 729) {
                                        newItem.setName("This cake is a lie!");
                                    } else {
                                        newItem.setName("weird " + ItemFactory.generateName(template, material));
                                    }
                                } else {
                                    int t = template.getTemplateId();
                                    int color = colour;
                                    if (template.isColor) {
                                        t = 438;
                                        if (colour == -1) {
                                            color = WurmColor.getInitialColor(template.getTemplateId(), Math.max(1, data2));
                                        }
                                    }
                                    newItem = allMaterialTypes && template.isWood() ? ItemFactory.createItem(t, data2, woodTypes[x], rare, responder.getName()) : (allMaterialTypes && template.isMetal() ? ItemFactory.createItem(t, data2, metalTypes[x], rare, responder.getName()) : (material != 0 ? ItemFactory.createItem(t, data2, material, rare, responder.getName()) : ItemFactory.createItem(t, data2, rare, responder.getName())));
                                    if (t != -1 && color != -1) {
                                        newItem.setColor(color);
                                    }
                                    if (name != null && name.length() > 0) {
                                        newItem.setName(name, true);
                                    }
                                    if (template.getTemplateId() == 175) {
                                        newItem.setAuxData((byte)2);
                                    }
                                }
                                if (newItem.getTemplateId() == 995 && sizemod > 0.0f) {
                                    newItem.setAuxData((byte)sizemod);
                                    if (newItem.getAuxData() > 4) {
                                        newItem.setRarity((byte)2);
                                    }
                                    newItem.fillTreasureChest();
                                }
                                if (newItem.isCoin()) {
                                    long newIron;
                                    long newSilver;
                                    long newCopper;
                                    long val = Economy.getValueFor(template.getTemplateId());
                                    if (val * (long)num > 500000000L) {
                                        responder.getCommunicator().sendNormalServerMessage("You aren't allowed to create that amount of money.");
                                        responder.getLogger().log(Level.WARNING, responder.getName() + " tried to create " + num + " " + newItem.getName() + " but wasn't allowed to.");
                                        return;
                                    }
                                    Change change = new Change(val);
                                    long newGold = change.getGoldCoins();
                                    if (newGold > 0L) {
                                        long oldGold = Economy.getEconomy().getGold();
                                        Economy.getEconomy().updateCreatedGold(oldGold + newGold);
                                    }
                                    if ((newCopper = change.getCopperCoins()) > 0L) {
                                        long oldCopper = Economy.getEconomy().getCopper();
                                        Economy.getEconomy().updateCreatedCopper(oldCopper + newCopper);
                                    }
                                    if ((newSilver = change.getSilverCoins()) > 0L) {
                                        long oldSilver = Economy.getEconomy().getSilver();
                                        Economy.getEconomy().updateCreatedSilver(oldSilver + newSilver);
                                    }
                                    if ((newIron = change.getIronCoins()) > 0L) {
                                        long oldIron = Economy.getEconomy().getIron();
                                        Economy.getEconomy().updateCreatedIron(oldIron + newIron);
                                    }
                                }
                                if (responder.getLogger() != null) {
                                    responder.getLogger().log(Level.INFO, responder.getName() + " created item " + newItem.getName() + ", item template: " + newItem.getTemplate().getName() + ", WurmID: " + newItem.getWurmId() + ", QL: " + newItem.getQualityLevel() + ", Rarity: " + newItem.getRarity());
                                } else if (responder.getPower() != 0) {
                                    logger.log(Level.INFO, responder.getName() + " created item " + newItem.getName() + ", WurmID: " + newItem.getWurmId() + ", QL: " + newItem.getQualityLevel());
                                }
                                if (sizemod != 1.0f && template.getTemplateId() != 995) {
                                    newItem.setWeight((int)Math.max(1.0f, sizemod * (float)template.getWeightGrams()), false);
                                    newItem.setSizes(newItem.getWeightGrams());
                                }
                                Item inventory = responder.getInventory();
                                if (newItem.isKingdomMarker() || newItem.isWind() && template.getTemplateId() == 579 || template.getTemplateId() == 578 || template.getTemplateId() == 999) {
                                    newItem.setAuxData(responder.getKingdomId());
                                }
                                if (newItem.isLock() && newItem.getTemplateId() != 167) {
                                    try {
                                        Item key = ItemFactory.createItem(168, newItem.getQualityLevel(), responder.getName());
                                        key.setMaterial(newItem.getMaterial());
                                        inventory.insertItem(key);
                                        newItem.addKey(key.getWurmId());
                                        key.setLockId(newItem.getWurmId());
                                    }
                                    catch (NoSuchTemplateException nst) {
                                        logger.log(Level.WARNING, responder.getName() + " failed to create key: " + nst.getMessage(), nst);
                                    }
                                    catch (FailedException fe) {
                                        logger.log(Level.WARNING, responder.getName() + " failed to create key: " + fe.getMessage(), fe);
                                    }
                                }
                                Item container = null;
                                if (newItem.isLiquid()) {
                                    if (template.getTemplateId() == 654) {
                                        container = ItemFactory.createItem(653, 99.0f, responder.getName());
                                        responder.getInventory().insertItem(container, true);
                                        container.insertItem(newItem);
                                    } else {
                                        Item[] allItems = inventory.getAllItems(false);
                                        for (int a = 0; a < allItems.length; ++a) {
                                            if (!allItems[a].isContainerLiquid() || !allItems[a].isEmpty(false) || !allItems[a].insertItem(newItem)) continue;
                                            container = allItems[a];
                                            newItem.setWeight(container.getFreeVolume(), false);
                                            break;
                                        }
                                    }
                                    if (container != null) {
                                        if (item.getTemplateId() == 301) {
                                            responder.getCommunicator().sendNormalServerMessage("You pour some " + newItem.getNameWithGenus() + " from the horn into the " + container.getName() + ".");
                                            Server.getInstance().broadCastAction(responder.getName() + " pours something out of a huge goat horn full of fruit and flowers.", responder, 5);
                                            continue;
                                        }
                                        responder.getCommunicator().sendNormalServerMessage("You wave your wand and create " + newItem.getName() + " in " + container.getNameWithGenus() + " [" + container.getDescription() + "].");
                                        Server.getInstance().broadCastAction(responder.getName() + " waves a black wand vividly.", responder, 5);
                                        continue;
                                    }
                                    responder.getCommunicator().sendNormalServerMessage("You need an empty container to put the " + newItem.getNameWithGenus() + " in!");
                                    Items.decay(newItem.getWurmId(), newItem.getDbStrings());
                                    continue;
                                }
                                if (inventory.insertItem(newItem)) {
                                    if (item.getTemplateId() == 301) {
                                        responder.getCommunicator().sendNormalServerMessage("You pull " + newItem.getNameWithGenus() + " out from the horn.");
                                        Server.getInstance().broadCastAction(responder.getName() + " pulls something out of a huge goat horn full of fruit and flowers.", responder, 5);
                                    } else {
                                        responder.getCommunicator().sendNormalServerMessage("You wave your wand and create " + newItem.getNameWithGenus() + ".");
                                        Server.getInstance().broadCastAction(responder.getName() + " waves a black wand vividly.", responder, 5);
                                    }
                                    if (!newItem.isEpicTargetItem() || !Servers.localServer.testServer) continue;
                                    newItem.setAuxData(responder.getKingdomTemplateId());
                                    AdvancedCreationEntry.onEpicItemCreated(responder, newItem, newItem.getTemplateId(), true);
                                    continue;
                                }
                                try {
                                    newItem.putItemInfrontof(responder);
                                    if (item.getTemplateId() == 301) {
                                        responder.getCommunicator().sendNormalServerMessage("You pull " + newItem.getNameWithGenus() + " out from the horn and puts it on the ground.");
                                        Server.getInstance().broadCastAction(responder.getName() + " pulls something out of a huge goat horn full of fruit and flowers.", responder, 5);
                                        continue;
                                    }
                                    responder.getCommunicator().sendNormalServerMessage("You wave your wand and create " + newItem.getNameWithGenus() + " in front of you.");
                                    Server.getInstance().broadCastAction(responder.getName() + " waves a black wand vividly.", responder, 5);
                                    continue;
                                }
                                catch (NoSuchPlayerException nsp) {
                                    responder.getCommunicator().sendAlertServerMessage("Could not locate your identity! Check the logs!");
                                    logger.log(Level.WARNING, nsp.getMessage(), nsp);
                                    continue;
                                }
                                catch (NoSuchCreatureException nsc) {
                                    responder.getCommunicator().sendAlertServerMessage("Could not locate your identity! Check the logs!");
                                    logger.log(Level.WARNING, nsc.getMessage(), nsc);
                                    continue;
                                }
                                catch (NoSuchZoneException nsz) {
                                    responder.getCommunicator().sendAlertServerMessage("You need to be in valid zones, since you cannot carry the item.");
                                }
                            }
                        } else {
                            responder.getCommunicator().sendAlertServerMessage("Don't create these. They will lack important data.");
                        }
                    } else {
                        logger.log(Level.WARNING, responder.getName() + " tries to create items by hacking the protocol. data1: " + d1 + ", data2: " + d2 + ", number: " + number);
                    }
                    if (responder.loggerCreature1 > 0L) {
                        responder.getCommunicator().sendNormalServerMessage("That took " + (System.currentTimeMillis() - now) + " ms.");
                    }
                }
                catch (NoSuchItemException nsi) {
                    logger.log(Level.WARNING, "Failed to locate item with id=" + target + " for which name change was intended.");
                    responder.getCommunicator().sendNormalServerMessage("Failed to locate that item.");
                }
                catch (NumberFormatException nfe) {
                    responder.getCommunicator().sendNormalServerMessage("You realize that " + d1 + ", " + d2 + " or " + number + " doesn't match your requirements.");
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, responder.getName() + " realises that " + d1 + ", " + d2 + " or " + number + " doesn't match their requirements. " + nfe, nfe);
                    }
                }
                catch (FailedException fe) {
                    responder.getCommunicator().sendNormalServerMessage("Failed!: " + fe.getMessage());
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, "Failed to create an Item " + fe, fe);
                    }
                }
                catch (NoSuchTemplateException nst) {
                    responder.getCommunicator().sendNormalServerMessage("Failed!: " + nst.getMessage());
                    if (!logger.isLoggable(Level.FINE)) break block69;
                    logger.log(Level.FINE, "Failed to create an Item " + nst, nst);
                }
            }
        }
    }

    public static void parseLearnSkillQuestion(LearnSkillQuestion question) {
        block44: {
            int type = question.getType();
            Creature responder = question.getResponder();
            long target = question.getTarget();
            if (type == 0) {
                logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
                return;
            }
            if (type == 16) {
                String d1 = question.getAnswer().getProperty("data1");
                String value = question.getAnswer().getProperty("val");
                String dec = question.getAnswer().getProperty("dec");
                String aff = question.getAnswer().getProperty("aff");
                String align = question.getAnswer().getProperty("align");
                String karma = question.getAnswer().getProperty("karma");
                String strPath = question.getAnswer().getProperty("path");
                String strLevel = question.getAnswer().getProperty("level");
                if (WurmPermissions.mayUseDeityWand(responder)) {
                    try {
                        float oldKarma;
                        int data1 = Integer.parseInt(d1);
                        Collection<SkillTemplate> temps = SkillSystem.templates.values();
                        SkillTemplate[] templates = temps.toArray(new SkillTemplate[temps.size()]);
                        Arrays.sort(templates, new Comparator<SkillTemplate>(){

                            @Override
                            public int compare(SkillTemplate o1, SkillTemplate o2) {
                                return o1.getName().compareTo(o2.getName());
                            }
                        });
                        int sk = templates[data1].getNumber();
                        double skillval = Double.parseDouble(value + "." + dec);
                        boolean changeSkill = skillval != 0.0;
                        boolean changeAff = !aff.equals("-1");
                        boolean changeAlign = align.length() > 0;
                        boolean changeKarma = karma.length() > 0;
                        boolean hasCult = strPath != null;
                        int newAff = Integer.parseInt(aff);
                        int newAlign = changeAlign ? Integer.parseInt(align) : 0;
                        int newKarma = changeKarma ? Integer.parseInt(karma) : 0;
                        skillval = Math.min(100.0, skillval);
                        skillval = Math.max(1.0, skillval);
                        Skills skills = null;
                        if (WurmId.getType(target) == 1 || WurmId.getType(target) == 0) {
                            if (sk != 10086) {
                                try {
                                    Creature receiver = Server.getInstance().getCreature(target);
                                    skills = receiver.getSkills();
                                    try {
                                        byte newPath;
                                        float oldKarma2;
                                        float oldAlign;
                                        Skill skill = skills.getSkill(sk);
                                        if (changeSkill) {
                                            skill.setKnowledge(skillval, false);
                                            responder.getCommunicator().sendNormalServerMessage("You set the " + skill.getName() + " skill of " + receiver.getName() + " to " + skillval + ".");
                                            receiver.getCommunicator().sendNormalServerMessage(responder.getName() + " sets your " + skill.getName() + " skill to " + skillval + ".");
                                            logger.log(Level.INFO, responder.getName() + " set " + skill.getName() + " skill of " + receiver.getName() + " to " + skillval + ".");
                                        }
                                        if (changeAff) {
                                            int oldAff = skill.affinity;
                                            if (oldAff != newAff) {
                                                Affinities.setAffinity(receiver.getWurmId(), sk, newAff, false);
                                                if (oldAff < newAff) {
                                                    responder.getCommunicator().sendNormalServerMessage("You increased affinities from " + oldAff + " to " + newAff + ".");
                                                } else {
                                                    responder.getCommunicator().sendNormalServerMessage("You decrease affinities from " + oldAff + " to " + newAff + ".");
                                                }
                                                logger.log(Level.INFO, responder.getName() + " set affinities for " + skill.getName() + " skill of " + receiver.getName() + " to " + newAff + ".");
                                            }
                                            if (receiver.isNpc() && skill.getNumber() == 1023) {
                                                Item shield;
                                                receiver.setSkill(10056, (float)(newAff * 10) + Server.rand.nextFloat() * (float)newAff * 10.0f);
                                                receiver.setSkill(10058, (float)(newAff * 10) + Server.rand.nextFloat() * (float)newAff * 10.0f);
                                                receiver.setSkill(10081, (float)(newAff * 10) + Server.rand.nextFloat() * (float)newAff * 10.0f);
                                                receiver.setSkill(10080, (float)(newAff * 10) + Server.rand.nextFloat() * (float)newAff * 10.0f);
                                                receiver.setSkill(10079, (float)(newAff * 10) + Server.rand.nextFloat() * (float)newAff * 10.0f);
                                                receiver.setSkill(1030, (float)(newAff * 10) + Server.rand.nextFloat() * (float)newAff * 10.0f);
                                                receiver.setSkill(1002, (float)(newAff * 10) + Server.rand.nextFloat() * (float)newAff * 10.0f);
                                                receiver.setSkill(103, (float)(Math.max(1, newAff) * 10) + Server.rand.nextFloat() * (float)Math.max(10, newAff * 10));
                                                receiver.setSkill(102, (float)(Math.max(1, newAff) * 10) + Server.rand.nextFloat() * (float)Math.max(10, newAff * 10));
                                                receiver.setSkill(10054, (float)(Math.max(1, newAff) * 10) + Server.rand.nextFloat() * (float)Math.max(10, newAff * 10));
                                                receiver.setSkill(10053, (float)(Math.max(1, newAff) * 10) + Server.rand.nextFloat() * (float)Math.max(10, newAff * 10));
                                                receiver.setSkill(10055, (float)(Math.max(1, newAff) * 10) + Server.rand.nextFloat() * (float)Math.max(10, newAff * 10));
                                                receiver.setSkill(10052, (float)(Math.max(1, newAff) * 10) + Server.rand.nextFloat() * (float)Math.max(10, newAff * 10));
                                                Item prim = receiver.getPrimWeapon();
                                                if (prim != null) {
                                                    receiver.setSkill(prim.getPrimarySkill(), (float)(Math.max(1, newAff) * 10) + Server.rand.nextFloat() * (float)Math.max(10, newAff * 10));
                                                }
                                                if ((shield = receiver.getShield()) != null) {
                                                    receiver.setSkill(shield.getPrimarySkill(), (float)(Math.max(1, newAff) * 10) + Server.rand.nextFloat() * (float)Math.max(10, newAff * 10));
                                                }
                                            }
                                        }
                                        if (changeAlign && (oldAlign = receiver.getAlignment()) != (float)newAlign) {
                                            receiver.setAlignment(newAlign);
                                            if (oldAlign < (float)newAlign) {
                                                responder.getCommunicator().sendNormalServerMessage("You increased alignment from " + oldAlign + " to " + newAlign + ".");
                                            } else {
                                                responder.getCommunicator().sendNormalServerMessage("You decrease alignment from " + oldAlign + " to " + newAlign + ".");
                                            }
                                        }
                                        if (changeKarma && (oldKarma2 = (float)receiver.getKarma()) != (float)newKarma) {
                                            receiver.setKarma(newKarma);
                                            if (oldKarma2 < (float)newKarma) {
                                                responder.getCommunicator().sendNormalServerMessage("You increased karma from " + oldKarma2 + " to " + newKarma + ".");
                                            } else {
                                                responder.getCommunicator().sendNormalServerMessage("You decrease karma from " + oldKarma2 + " to " + newKarma + ".");
                                            }
                                            logger.log(Level.INFO, responder.getName() + " set karma of " + receiver.getName() + " from " + oldKarma2 + " to " + newKarma + ".");
                                        }
                                        if (!hasCult) break block44;
                                        Cultist cultist = Cultist.getCultist(target);
                                        byte path = 0;
                                        byte level = 0;
                                        if (cultist != null) {
                                            path = cultist.getPath();
                                            level = cultist.getLevel();
                                        }
                                        if (path != (newPath = Byte.parseByte(strPath))) {
                                            if (path != 0) {
                                                try {
                                                    cultist.deleteCultist();
                                                    responder.getCommunicator().sendNormalServerMessage("You have removed " + receiver.getName() + " from " + Cults.getPathNameFor(path) + ".");
                                                    receiver.getCommunicator().sendNormalServerMessage(responder.getName() + " removed you from " + Cults.getPathNameFor(path) + ".");
                                                    logger.log(Level.INFO, responder.getName() + " removed " + receiver.getName() + " from " + Cults.getPathNameFor(path) + ".");
                                                }
                                                catch (IOException e) {
                                                    responder.getCommunicator().sendNormalServerMessage("Problem leaving cultist path for " + receiver.getName() + ".");
                                                    logger.log(Level.INFO, responder.getName() + " had problem resetting cultist path for " + receiver.getName() + ".");
                                                    return;
                                                }
                                            }
                                            if (newPath != 0) {
                                                cultist = new Cultist(target, newPath);
                                                responder.getCommunicator().sendNormalServerMessage("You have added " + receiver.getName() + " to " + Cults.getPathNameFor(newPath) + ".");
                                                receiver.getCommunicator().sendNormalServerMessage(responder.getName() + " added you to " + Cults.getPathNameFor(newPath) + ".");
                                                logger.log(Level.INFO, responder.getName() + " added " + receiver.getName() + " to " + Cults.getPathNameFor(newPath) + ".");
                                            }
                                        }
                                        if (newPath != 0 && strLevel.length() > 0) {
                                            level = (byte)Math.min(Byte.parseByte(strLevel), 15);
                                            cultist.setLevel(level);
                                            responder.getCommunicator().sendNormalServerMessage("You have changes cult level for " + receiver.getName() + " to " + Cults.getNameForLevel(newPath, level) + ".");
                                            receiver.getCommunicator().sendNormalServerMessage(responder.getName() + " changed your cult level to " + Cults.getNameForLevel(newPath, level) + ".");
                                            logger.log(Level.INFO, responder.getName() + " changed cult level of " + receiver.getName() + " to " + Cults.getNameForLevel(newPath, level) + ".");
                                        }
                                        break block44;
                                    }
                                    catch (NoSuchSkillException nss) {
                                        skills.learn(sk, (float)skillval);
                                        responder.getCommunicator().sendNormalServerMessage("You teach " + receiver.getName() + " " + SkillSystem.getNameFor(sk) + " to " + skillval + ".");
                                        receiver.getCommunicator().sendNormalServerMessage(responder.getName() + " teaches you the " + SkillSystem.getNameFor(sk) + " skill to " + skillval + ".");
                                        logger.log(Level.INFO, responder.getName() + " set " + SkillSystem.getNameFor(sk) + " skill of " + receiver.getName() + " to " + skillval + ".");
                                        break block44;
                                    }
                                    catch (IOException e) {
                                        responder.getCommunicator().sendNormalServerMessage("Problem changing alignment for " + receiver.getName() + ".");
                                    }
                                }
                                catch (NoSuchCreatureException nsc) {
                                    responder.getCommunicator().sendNormalServerMessage("Failed to locate creature with id " + target + ".");
                                }
                                catch (NoSuchPlayerException nsp) {
                                    responder.getCommunicator().sendNormalServerMessage("Failed to locate player with id " + target + ".");
                                }
                                break block44;
                            }
                            responder.getCommunicator().sendNormalServerMessage("This skill is impossible to learn like that.");
                            break block44;
                        }
                        skills = responder.getSkills();
                        try {
                            int oldAff;
                            Skill skill = skills.getSkill(sk);
                            if (changeSkill) {
                                skill.setKnowledge(skillval, false);
                                responder.getCommunicator().sendNormalServerMessage("You set " + SkillSystem.getNameFor(sk) + " to " + skillval + ".");
                            }
                            if (changeAff && (oldAff = skill.affinity) != newAff) {
                                skill.setAffinity(newAff);
                                if (oldAff < newAff) {
                                    responder.getCommunicator().sendNormalServerMessage("You increased affinities from " + oldAff + " to " + newAff + ".");
                                } else {
                                    responder.getCommunicator().sendNormalServerMessage("You descrmented affinities from " + oldAff + " to " + newAff + ".");
                                }
                            }
                        }
                        catch (NoSuchSkillException nss) {
                            skills.learn(sk, (float)skillval);
                            responder.getCommunicator().sendNormalServerMessage("You learn " + SkillSystem.getNameFor(sk) + " to " + skillval + ".");
                        }
                        logger.log(Level.INFO, responder.getName() + " learnt " + SkillSystem.getNameFor(sk) + " to " + skillval + ".");
                        if (changeKarma && (oldKarma = (float)responder.getKarma()) != (float)newKarma) {
                            responder.setKarma(newKarma);
                            if (oldKarma < (float)newKarma) {
                                responder.getCommunicator().sendNormalServerMessage("You increased your karma from " + oldKarma + " to " + newKarma + ".");
                            } else {
                                responder.getCommunicator().sendNormalServerMessage("You decrease your karma from " + oldKarma + " to " + newKarma + ".");
                            }
                            logger.log(Level.INFO, responder.getName() + " set karma of " + responder.getName() + " from " + oldKarma + " to " + newKarma + ".");
                        }
                    }
                    catch (NumberFormatException nfe) {
                        responder.getCommunicator().sendNormalServerMessage("Failed to interpret " + d1 + " or " + value + " as a number.");
                    }
                } else {
                    logger.log(Level.WARNING, responder.getName() + " tries to learn skills but their power is only " + responder.getPower() + ". data1: " + d1 + ", value: " + value);
                }
            }
        }
    }

    public static void summon(String pname, Creature responder, int tilex, int tiley, byte layer) {
        try {
            Player player = Players.getInstance().getPlayer(StringUtilities.raiseFirstLetter(pname));
            responder.getCommunicator().sendNormalServerMessage("You summon " + player.getName() + ".");
            Server.getInstance().broadCastAction(responder.getName() + " makes a commanding gesture!", responder, 5);
            player.getCommunicator().sendNormalServerMessage("You are summoned by a great force.");
            Server.getInstance().broadCastAction(player.getName() + " suddenly disappears.", player, 5);
            if (responder.getLogger() != null) {
                responder.getLogger().log(Level.INFO, responder.getName() + " summons " + pname);
            }
            player.setTeleportPoints((short)tilex, (short)tiley, (int)layer, responder.getFloorLevel());
            player.startTeleporting();
            Server.getInstance().broadCastAction(player.getName() + " suddenly appears.", player, 5);
            player.getCommunicator().sendTeleport(false);
            player.setBridgeId(responder.getBridgeId());
            logger.log(Level.INFO, responder.getName() + " summoned player " + player.getName() + ", with ID: " + player.getWurmId() + " at coords " + tilex + ',' + tiley + " teleportlayer " + player.getTeleportLayer());
        }
        catch (NoSuchPlayerException nsp) {
            PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(pname);
            try {
                pinf.load();
            }
            catch (IOException iox) {
                responder.getCommunicator().sendNormalServerMessage("Failed to load data for the player with name " + pname + ".");
                return;
            }
            if (pinf != null && pinf.wurmId > 0L) {
                CreaturePos cs = CreaturePos.getPosition(pinf.wurmId);
                cs.setPosX((tilex << 2) + 2);
                cs.setPosY((tiley << 2) + 2);
                float z = 0.0f;
                try {
                    z = Zones.calculateHeight(cs.getPosX(), cs.getPosY(), layer == 0);
                }
                catch (NoSuchZoneException nsz) {
                    responder.getCommunicator().sendNormalServerMessage("No such zone: " + tilex + "," + tiley + ", surf=" + (layer == 0));
                    return;
                }
                cs.setLayer(layer);
                cs.setPosZ(z, true);
                cs.setRotation(responder.getStatus().getRotation() - 180.0f);
                try {
                    int zoneid = Zones.getZoneIdFor(tilex, tiley, layer == 0);
                    cs.setZoneId(zoneid);
                    cs.setBridgeId(responder.getBridgeId());
                    cs.save(false);
                    responder.getCommunicator().sendNormalServerMessage("Okay, " + pname + " set to " + tilex + "," + tiley + " surfaced=" + (layer == 0));
                    logger.log(Level.INFO, responder.getName() + " set " + pname + " to " + tilex + "," + tiley + " surfaced=" + (layer == 0));
                    if (responder.getLogger() != null) {
                        responder.getLogger().log(Level.INFO, "Set " + pname + " to " + tilex + "," + tiley + " surfaced=" + (layer == 0));
                    }
                }
                catch (NoSuchZoneException nsz) {
                    responder.getCommunicator().sendNormalServerMessage("No such zone: " + tilex + "," + tiley + ", surf=" + (layer == 0));
                    return;
                }
            }
            responder.getCommunicator().sendNormalServerMessage("No player with the name " + pname + " found.");
        }
    }

    static void parseCreatureCreationQuestion(CreatureCreationQuestion question) {
        block69: {
            int type = question.getType();
            Creature responder = question.getResponder();
            long target = question.getTarget();
            if (type == 0) {
                logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
                return;
            }
            if (type == 6 && (WurmId.getType(target) == 2 || WurmId.getType(target) == 19 || WurmId.getType(target) == 20)) {
                String pname = question.getAnswer().getProperty("pname");
                if (pname != null && pname.length() > 0) {
                    if (pname.equalsIgnoreCase(responder.getName())) {
                        responder.getCommunicator().sendNormalServerMessage("You cannot summon yourself.");
                    } else {
                        QuestionParser.summon(pname, responder, responder.getTileX(), responder.getTileY(), (byte)responder.getLayer());
                    }
                } else {
                    String d1 = question.getAnswer().getProperty("data1");
                    String number = question.getAnswer().getProperty("number");
                    String cname = question.getAnswer().getProperty("cname");
                    String gender = question.getAnswer().getProperty("gender");
                    String sage = question.getAnswer().getProperty("age");
                    try {
                        Item item = Items.getItem(target);
                        if (item.getTemplateId() == 176 && responder.getPower() >= 3 || Servers.localServer.testServer && responder.getPower() >= 1) {
                            float posx = (question.getTileX() << 2) + 2;
                            float posy = (question.getTileY() << 2) + 2;
                            int layer = question.getLayer();
                            int floorLevel = responder.getFloorLevel();
                            float rot = responder.getStatus().getRotation();
                            if ((rot -= 180.0f) < 0.0f) {
                                rot += 360.0f;
                            } else if (rot > 360.0f) {
                                rot -= 360.0f;
                            }
                            int tId = Integer.parseInt(d1);
                            int num = Integer.parseInt(number);
                            int dage = Integer.parseInt(sage);
                            CreatureTemplate template = question.getTemplate(tId);
                            if (template.isUnique() && responder.getPower() < 5 && !Servers.localServer.testServer) {
                                responder.getCommunicator().sendNormalServerMessage("You may not summon that creature.");
                                return;
                            }
                            if (template.getTemplateId() == 53 && !WurmCalendar.isEaster() && !Servers.localServer.testServer) {
                                responder.getCommunicator().sendNormalServerMessage("You may not summon that creature now.");
                                return;
                            }
                            long start = System.nanoTime();
                            for (int x = 0; x < num; ++x) {
                                if (x > 0) {
                                    posx += -2.0f + Server.rand.nextFloat() * 4.0f;
                                    posy += -2.0f + Server.rand.nextFloat() * 4.0f;
                                }
                                try {
                                    Creature newCreature;
                                    byte sex = 0;
                                    if (gender.equals("female") || template.getSex() == 1) {
                                        sex = 1;
                                    }
                                    byte kingd = 0;
                                    if (template.isHuman()) {
                                        kingd = responder.getKingdomId();
                                    }
                                    long sid = question.getStructureId();
                                    long bridgeId = -10L;
                                    if (sid > 0L) {
                                        try {
                                            Structure struct = Structures.getStructure(sid);
                                            if (struct.isTypeBridge()) {
                                                bridgeId = sid;
                                            }
                                        }
                                        catch (NoSuchStructureException struct) {
                                            // empty catch block
                                        }
                                    }
                                    byte ttype = 0;
                                    if (template.hasDen() || template.isRiftCreature() || Servers.localServer.testServer) {
                                        ttype = Byte.parseByte(question.getAnswer().getProperty("tid"));
                                    }
                                    if (template.getTemplateId() != 69) {
                                        int age = dage - 1;
                                        if (dage < 2) {
                                            age = (int)(Server.rand.nextFloat() * 5.0f);
                                        }
                                        if (template.getTemplateId() == 65 || template.getTemplateId() == 48 || template.getTemplateId() == 98 || template.getTemplateId() == 101 || template.getTemplateId() == 50 || template.getTemplateId() == 117 || template.getTemplateId() == 118) {
                                            newCreature = Creature.doNew(template.getTemplateId(), true, posx, posy, rot, layer, cname, sex, kingd, ttype, false, (byte)age, floorLevel);
                                        } else {
                                            if (dage < 2) {
                                                age = (int)(Server.rand.nextFloat() * (float)Math.min(48, template.getMaxAge()));
                                            }
                                            newCreature = Creature.doNew(template.getTemplateId(), true, posx, posy, rot, layer, cname, sex, kingd, ttype, false, (byte)age, floorLevel);
                                        }
                                    } else {
                                        newCreature = Creature.doNew(template.getTemplateId(), false, posx, posy, rot, layer, cname, sex, kingd, ttype, true, (byte)0, floorLevel);
                                    }
                                    if (sid > 0L && bridgeId > 0L) {
                                        newCreature.setBridgeId(bridgeId);
                                    }
                                    logger.log(Level.INFO, responder.getName() + " created " + gender + " " + template.getName() + ", with ID: " + newCreature.getWurmId() + ", age: " + newCreature.getStatus().age + " at coords " + posx + ',' + posy);
                                    responder.getCommunicator().sendNormalServerMessage("You wave your wand, demanding " + newCreature.getNameWithGenus() + " to appear from the mists of the void.");
                                    Server.getInstance().broadCastAction(responder.getName() + " waves a black wand vividly and " + newCreature.getNameWithGenus() + " quickly appears from nowhere.", responder, 5);
                                    if (newCreature.isHorse()) {
                                        newCreature.setVisible(false);
                                        Creature.setRandomColor(newCreature);
                                        newCreature.setVisible(true);
                                    } else if (newCreature.getTemplate().isColoured) {
                                        newCreature.setVisible(false);
                                        int randCol = Server.rand.nextInt(newCreature.getTemplate().maxColourCount);
                                        if (randCol == 1) {
                                            newCreature.getStatus().setTraitBit(15, true);
                                        } else if (randCol == 2) {
                                            newCreature.getStatus().setTraitBit(16, true);
                                        } else if (randCol == 3) {
                                            newCreature.getStatus().setTraitBit(17, true);
                                        } else if (randCol == 4) {
                                            newCreature.getStatus().setTraitBit(18, true);
                                        } else if (randCol == 5) {
                                            newCreature.getStatus().setTraitBit(24, true);
                                        } else if (randCol == 6) {
                                            newCreature.getStatus().setTraitBit(25, true);
                                        } else if (randCol == 7) {
                                            newCreature.getStatus().setTraitBit(23, true);
                                        } else if (randCol == 8) {
                                            newCreature.getStatus().setTraitBit(30, true);
                                        } else if (randCol == 9) {
                                            newCreature.getStatus().setTraitBit(31, true);
                                        } else if (randCol == 10) {
                                            newCreature.getStatus().setTraitBit(32, true);
                                        } else if (randCol == 11) {
                                            newCreature.getStatus().setTraitBit(33, true);
                                        } else if (randCol == 12) {
                                            newCreature.getStatus().setTraitBit(34, true);
                                        } else if (randCol == 13) {
                                            newCreature.getStatus().setTraitBit(35, true);
                                        } else if (randCol == 14) {
                                            newCreature.getStatus().setTraitBit(36, true);
                                        } else if (randCol == 15) {
                                            newCreature.getStatus().setTraitBit(37, true);
                                        } else if (randCol == 16) {
                                            newCreature.getStatus().setTraitBit(38, true);
                                        }
                                        newCreature.setVisible(true);
                                    }
                                    SoundPlayer.playSound("sound.work.carpentry.carvingknife", newCreature, 1.0f);
                                    continue;
                                }
                                catch (Exception ex) {
                                    logger.log(Level.WARNING, responder.getName() + " tried to create creature but failed: " + ex.getMessage(), ex);
                                }
                            }
                            float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0f;
                            logger.info(responder.getName() + " created " + num + ' ' + template.getName() + ", which took " + lElapsedTime + " millis.");
                            break block69;
                        }
                        if (item.getTemplateId() == 315 && responder.getPower() >= 1) {
                            responder.getCommunicator().sendNormalServerMessage("Sorry, but you cannot summon creatures at this moment.");
                        } else {
                            responder.getCommunicator().sendNormalServerMessage("Sorry, but you cannot summon creatures at this moment.");
                            logger.log(Level.WARNING, responder.getName() + ", power=" + responder.getPower() + " tried to summon a " + cname + " creature using a " + item);
                        }
                    }
                    catch (NoSuchItemException nsi) {
                        logger.log(Level.INFO, responder.getName() + " tried to use a deitywand itemid to create a creature, but the item did not exist.", nsi);
                    }
                    catch (NumberFormatException nfe) {
                        responder.getCommunicator().sendNormalServerMessage("You realize that " + d1 + ", or " + number + " doesn't match your requirements.");
                        if (!logger.isLoggable(Level.FINE)) break block69;
                        logger.log(Level.FINE, responder.getName() + " realises that " + d1 + " or " + number + " doesn't match their requirements. " + nfe, nfe);
                    }
                }
            }
        }
    }

    static void parseVillageExpansionQuestion(VillageExpansionQuestion question) {
        Creature responder = question.getResponder();
        long target = question.getTarget();
        try {
            Citizen mayor;
            Item altar;
            Item deed = Items.getItem(target);
            Item token = question.getToken();
            int villid = token.getData2();
            Village village = Villages.getVillage(villid);
            int tilex = village.getTokenX();
            int tiley = village.getTokenY();
            int oldSize = (village.getEndX() - village.getStartX()) / 2;
            int size = Villages.getSizeForDeed(deed.getTemplateId());
            if (oldSize == size) {
                responder.getCommunicator().sendSafeServerMessage("There is no difference in the sizes of the deeds.");
                return;
            }
            Structure[] newstructures = Zones.getStructuresInArea(tilex - size, tiley - size, tilex + size, tiley + size, responder.isOnSurface());
            HashSet<Structure> notOverlaps = new HashSet<Structure>();
            Structure[] oldstructures = Zones.getStructuresInArea(village.getStartX(), village.getStartY(), village.getEndX(), village.getEndY(), responder.isOnSurface());
            for (int o = 0; o < oldstructures.length; ++o) {
                boolean found = false;
                for (int n = 0; n < newstructures.length; ++n) {
                    if (newstructures[n] != oldstructures[o]) continue;
                    found = true;
                    break;
                }
                if (found) continue;
                notOverlaps.add(oldstructures[o]);
            }
            if (notOverlaps.size() > 0) {
                Structure[] structures = notOverlaps.toArray(new Structure[notOverlaps.size()]);
                Item[] writs = responder.getKeys();
                boolean error = false;
                for (int x = 0; x < structures.length; ++x) {
                    long writid = structures[x].getWritId();
                    boolean found = false;
                    for (int k = 0; k < writs.length; ++k) {
                        if (writs[k].getWurmId() != writid) continue;
                        found = true;
                    }
                    if (found) continue;
                    error = true;
                    if (responder.getPower() > 0) {
                        responder.getLogger().log(Level.INFO, responder.getName() + " founding village over existing structure " + structures[x].getName());
                        responder.getCommunicator().sendSafeServerMessage("Skipping the writ requirement for the structure " + structures[x].getName() + " to expand the village. Are you sure you know what you are doing?");
                        continue;
                    }
                    responder.getCommunicator().sendSafeServerMessage("You need the writ for the structure " + structures[x].getName() + " to expand the village.");
                }
                if (error) {
                    if (responder.getPower() <= 0) {
                        return;
                    }
                    logger.log(Level.WARNING, responder.getName() + " founding village over existing structures.");
                }
            }
            if ((altar = Villages.isAltarOnDeed(size, size, size, size, tilex, tiley, responder.isOnSurface())) != null) {
                if (altar.isEpicTargetItem() && Servers.localServer.PVPSERVER) {
                    if (EpicServerStatus.getRitualMissionForTarget(altar.getWurmId()) != null) {
                        responder.getCommunicator().sendSafeServerMessage("You cannot found the settlement here, since the " + altar.getName() + " is currently required for an active mission.");
                        return;
                    }
                } else {
                    responder.getCommunicator().sendSafeServerMessage("You cannot found the settlement here, since the " + altar.getName() + " makes it holy ground.");
                    return;
                }
            }
            if ((mayor = village.getMayor()) != null) {
                LoginServerWebConnection lsw;
                PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(mayor.wurmId);
                long moneyToReimburse = 0L;
                if (pinf != null) {
                    try {
                        Item olddeed = Items.getItem(village.deedid);
                        moneyToReimburse = olddeed.getValue() / 2;
                    }
                    catch (NoSuchItemException nsi) {
                        logger.log(Level.WARNING, village.getName() + " No deed id with id=" + village.deedid, nsi);
                    }
                }
                if (moneyToReimburse > 0L && !(lsw = new LoginServerWebConnection()).addMoney(mayor.wurmId, pinf.getName(), moneyToReimburse, "Expand " + village.getName())) {
                    logger.log(Level.INFO, "Expanding did not yield money for " + village.getName() + " to " + pinf.getName() + ": " + moneyToReimburse + "?");
                    responder.getCommunicator().sendSafeServerMessage("You expand the village here, but the mayor could not be reimbursed.");
                }
            }
            village.setNewBounds(tilex - size, tiley - size, tilex + size, tiley + size);
            village.plan.updateGuardPlan(village.plan.type, village.plan.moneyLeft + village.plan.calculateMonthlyUpkeepTimeforType(1), village.plan.getNumHiredGuards());
            String villageName = village.getName();
            if (responder.getPower() < 5) {
                Shop shop = Economy.getEconomy().getKingsShop();
                shop.setMoney(shop.getMoney() - (long)((int)((float)deed.getValue() * 0.4f)));
            }
            int g = 0;
            if (village.getGuards() != null) {
                g = village.getGuards().length;
            }
            int maxCitizens = (int)((float)(size * size) / 2.5f);
            int numOldCitizens = village.getCitizens().length - g;
            int citizToRemove = numOldCitizens - maxCitizens;
            if (citizToRemove > 0) {
                Citizen[] citz = village.getCitizens();
                for (int x = 0; x < citizToRemove; ++x) {
                    if (citz[x].getRole().id == 2 || citz[x].getRole().id == 4) continue;
                    village.removeCitizen(citz[x]);
                }
            }
            responder.getCommunicator().sendNormalServerMessage(villageName + " has been expanded to size " + size + ".");
            Server.getInstance().broadCastSafe(WurmCalendar.getTime());
            Server.getInstance().broadCastSafe("The homestead of " + villageName + " has just been expanded by " + responder.getName() + " to the size of " + size + ".");
            village.addHistory(responder.getName(), "expanded to the size of " + size);
            HistoryManager.addHistory(responder.getName(), "expanded " + village.getName() + " to the size of " + size);
            logger.info("The deed of " + villageName + ", id: " + village.deedid + ", has just been expanded by " + responder.getName() + " to the size of " + size + ".");
        }
        catch (NoSuchItemException nsi) {
            logger.log(Level.WARNING, "Failed to locate village deed with id " + target, nsi);
            responder.getCommunicator().sendNormalServerMessage("Failed to locate the deed item for that request. Please contact administration.");
        }
        catch (NoSuchVillageException nsv) {
            logger.log(Level.WARNING, "Failed to locate village with id " + target, nsv);
            responder.getCommunicator().sendNormalServerMessage("Failed to locate the village for that request. Please contact administration.");
        }
    }

    static void parseGateManageQuestion(GateManagementQuestion question) {
        block13: {
            Creature responder = question.getResponder();
            long target = question.getTarget();
            try {
                Item deed = Items.getItem(target);
                Village village = Villages.getVillage(deed.getData2());
                Set<FenceGate> gates = village.getGates();
                String key = "";
                String val = "";
                if (gates == null) break block13;
                for (FenceGate gate : gates) {
                    int time;
                    long id = gate.getWurmId();
                    key = "gate" + id;
                    val = question.getAnswer().getProperty(key);
                    if (val != null && val.length() > 0) {
                        val = val.replaceAll("\"", "");
                        gate.setName(val);
                    }
                    key = "open" + id;
                    val = question.getAnswer().getProperty(key);
                    if (val != null && val.length() > 0) {
                        try {
                            time = Integer.parseInt(val);
                            if (time > 23 || time < 0) {
                                responder.getCommunicator().sendNormalServerMessage("When setting open time for gate " + gate.getName() + " the time was " + val + " which is out of the range 0-23.");
                            } else {
                                gate.setOpenTime(time);
                            }
                        }
                        catch (NumberFormatException nfe) {
                            responder.getCommunicator().sendNormalServerMessage("When setting open time for gate " + gate.getName() + " the time was " + val + " which did not work.");
                        }
                    }
                    key = "close" + id;
                    val = question.getAnswer().getProperty(key);
                    if (val == null || val.length() <= 0) continue;
                    try {
                        time = Integer.parseInt(val);
                        if (time > 23 || time < 0) {
                            responder.getCommunicator().sendNormalServerMessage("When setting close time for gate " + gate.getName() + " the time was " + val + " which is out of the range 0-23.");
                            continue;
                        }
                        gate.setCloseTime(time);
                    }
                    catch (NumberFormatException nfe) {
                        responder.getCommunicator().sendNormalServerMessage("When setting close time for gate " + gate.getName() + " the time was " + val + " which did not work.");
                    }
                }
                responder.getCommunicator().sendSafeServerMessage("Settings updated.");
            }
            catch (NoSuchItemException nsi) {
                logger.log(Level.WARNING, "Failed to locate village deed with id " + target, nsi);
                responder.getCommunicator().sendNormalServerMessage("Failed to locate the deed item for that request. Please contact administration.");
            }
            catch (NoSuchVillageException nsv) {
                logger.log(Level.WARNING, "Failed to locate village for deed with id " + target, nsv);
                responder.getCommunicator().sendNormalServerMessage("Failed to locate the village for that request. Please contact administration.");
            }
        }
    }

    static void parseVillageSettingsManageQuestion(VillageSettingsManageQuestion question) {
        Creature responder = question.getResponder();
        long target = question.getTarget();
        Properties props = question.getAnswer();
        try {
            String key;
            String val;
            Village village;
            if (target == -10L) {
                village = responder.getCitizenVillage();
                if (village == null) {
                    throw new NoSuchVillageException("You are not a citizen of any village (on this server).");
                }
            } else {
                Item deed = Items.getItem(target);
                int villageId = deed.getData2();
                village = Villages.getVillage(villageId);
            }
            boolean highways = false;
            boolean kos = false;
            boolean routing = false;
            if (Features.Feature.HIGHWAYS.isEnabled()) {
                highways = Boolean.parseBoolean(props.getProperty("highways"));
                kos = Boolean.parseBoolean(props.getProperty("kos"));
                routing = Boolean.parseBoolean(props.getProperty("routing"));
                boolean hasHighway = village.hasHighway();
                if (!highways && hasHighway) {
                    responder.getCommunicator().sendNormalServerMessage("Cannot disallow highways as you have one on (or next to) your settlement.");
                    return;
                }
                if (village.getReputations().length > 0 && highways) {
                    responder.getCommunicator().sendNormalServerMessage("Cannot allow highways if you have an active kos.");
                    return;
                }
                if (village.getReputations().length > 0 && !kos) {
                    responder.getCommunicator().sendNormalServerMessage("Cannot disallow kos if you have an active kos. Clear the kos list first.");
                    return;
                }
                if (highways && kos) {
                    responder.getCommunicator().sendNormalServerMessage("Cannot allow kos and allow highways at same time.");
                    return;
                }
                if (routing && !highways) {
                    responder.getCommunicator().sendNormalServerMessage("Cannot opt-in for you village to be found when routing, if you dont allow highways.");
                    return;
                }
            }
            String villageName = village.getName();
            boolean changingName = false;
            if (village.mayChangeName()) {
                villageName = props.getProperty("vname");
                villageName = villageName.replaceAll("\"", "");
                if ((villageName = villageName.trim()).length() > 3) {
                    villageName = StringUtilities.raiseFirstLetter(villageName);
                    StringTokenizer tokens = new StringTokenizer(villageName);
                    String newName = tokens.nextToken();
                    while (tokens.hasMoreTokens()) {
                        newName = newName + " " + StringUtilities.raiseFirstLetter(tokens.nextToken());
                    }
                    villageName = newName;
                }
                if (villageName.length() >= 41) {
                    villageName = villageName.substring(0, 39);
                    responder.getCommunicator().sendNormalServerMessage("The name of the settlement would be ''" + villageName + "''. Please select a shorter name.");
                    return;
                }
                if (villageName.length() < 3) {
                    responder.getCommunicator().sendNormalServerMessage("The name of the settlement would be ''" + villageName + "''. Please select a name with at least 3 letters.");
                    return;
                }
                if (QuestionParser.containsIllegalVillageCharacters(villageName)) {
                    responder.getCommunicator().sendNormalServerMessage("The name ''" + villageName + "'' contains illegal characters. Please select another name.");
                    return;
                }
                if (villageName.equals("Wurm")) {
                    responder.getCommunicator().sendNormalServerMessage("The name ''" + villageName + "'' is illegal. Please select another name.");
                    return;
                }
                if (!Villages.isNameOk(villageName, village.id)) {
                    responder.getCommunicator().sendNormalServerMessage("The name ''" + villageName + "'' is already taken. Please select another name.");
                    return;
                }
                if (!villageName.equals(village.getName())) {
                    if (!village.mayChangeName()) {
                        responder.getCommunicator().sendNormalServerMessage("You try to change the settlement name as its just been changed. The action was aborted.");
                        return;
                    }
                    long moneyNeeded = 50000L;
                    long rest = VillageFoundationQuestion.getExpandMoneyNeededFromBank(moneyNeeded, village);
                    if (rest > 0L) {
                        if (Servers.localServer.testServer) {
                            responder.getCommunicator().sendNormalServerMessage("We need " + moneyNeeded + ". " + rest + " must be taken from the bank.");
                        }
                        if (!((Player)responder).chargeMoney(rest)) {
                            responder.getCommunicator().sendNormalServerMessage("You try to change the settlement size, but your bank account could not be charged. The action was aborted.");
                            return;
                        }
                        if (Servers.localServer.testServer) {
                            responder.getCommunicator().sendNormalServerMessage("We also take " + village.getAvailablePlanMoney() + " from upkeep.");
                        }
                        village.plan.updateGuardPlan(village.plan.moneyLeft - village.getAvailablePlanMoney());
                    } else {
                        if (Servers.localServer.testServer) {
                            responder.getCommunicator().sendNormalServerMessage("We charge " + moneyNeeded + " from the plan which has " + village.plan.moneyLeft);
                        }
                        village.plan.updateGuardPlan(village.plan.moneyLeft - moneyNeeded);
                    }
                    Item deed = null;
                    try {
                        deed = Items.getItem(village.getDeedId());
                    }
                    catch (NoSuchItemException nsi) {
                        logger.log(Level.WARNING, "Failed to locate settlement deed with id " + village.getDeedId(), nsi);
                        responder.getCommunicator().sendNormalServerMessage("Failed to locate the deed item for that request. Please contact administration.");
                    }
                    village.setName(villageName);
                    village.setFaithCreate(0.0f);
                    village.setFaithHeal(0.0f);
                    village.setFaithWar(0.0f);
                    village.setLastChangedName(System.currentTimeMillis());
                    deed.setDescription(villageName);
                    responder.getCommunicator().sendNormalServerMessage("Changed settlement name to \"" + villageName + "\".");
                }
            }
            if ((val = props.getProperty(key = "motto")) != null) {
                if (!QuestionParser.containsIllegalCharacters(val)) {
                    try {
                        village.setMotto(val.replaceAll("\"", ""));
                    }
                    catch (IOException iox) {
                        responder.getCommunicator().sendNormalServerMessage("Failed to update the motto.");
                        logger.log(Level.WARNING, iox.getMessage(), iox);
                    }
                } else {
                    responder.getCommunicator().sendNormalServerMessage("The motto contains some illegal characters.");
                }
            }
            if ((val = props.getProperty(key = "motd")) != null) {
                try {
                    String oldMotd = village.getMotd();
                    village.setMotd(val.replaceAll("\"", ""));
                    if (oldMotd != village.getMotd()) {
                        village.addHistory(responder.getName(), "changed Motd");
                    }
                }
                catch (IOException iox) {
                    responder.getCommunicator().sendNormalServerMessage("Failed to update the motd.");
                    logger.log(Level.WARNING, iox.getMessage(), iox);
                }
            }
            boolean setdemocracy = false;
            key = "democracy";
            val = props.getProperty(key);
            if (val != null) {
                setdemocracy = val.equals("true");
            }
            if (setdemocracy) {
                try {
                    village.setDemocracy(true);
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, "Failed to set " + village.getName() + " to democracy. " + iox.getMessage(), iox);
                }
            }
            if (setdemocracy = (val = props.getProperty(key = "nondemocracy")) != null ? val.equals("true") : false) {
                try {
                    village.setDemocracy(false);
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, "Failed to set " + village.getName() + " to dictatorship. " + iox.getMessage(), iox);
                }
            }
            boolean unlimitedCitizens = false;
            key = "unlimitC";
            val = props.getProperty(key);
            unlimitedCitizens = val != null ? val.equals("true") : false;
            try {
                village.setUnlimitedCitizens(unlimitedCitizens);
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, "Failed to set " + village.getName() + " to unlimitedC - " + unlimitedCitizens + ":" + iox.getMessage(), iox);
            }
            int oldSettings = village.getSettings();
            if (routing != village.isHighwayFound()) {
                responder.getCommunicator().sendNormalServerMessage((routing ? "Enabled" : "Disabled") + " finding village through routing.");
            }
            if (highways != village.isHighwayAllowed()) {
                responder.getCommunicator().sendNormalServerMessage((highways ? "Enabled" : "Disabled") + " highways through village.");
            }
            if (kos != village.isKosAllowed()) {
                responder.getCommunicator().sendNormalServerMessage((kos ? "Enabled" : "Disabled") + " KOS.");
            }
            try {
                village.setIsHighwayFound(routing);
                village.setIsKosAllowed(kos);
                village.setIsHighwayAllowed(highways);
                if (village.getSettings() != oldSettings) {
                    village.saveSettings();
                }
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, "Failed to save " + village.getName() + " settings:" + iox.getMessage(), iox);
            }
            boolean setKingdomSpawn = false;
            key = "spawns";
            val = props.getProperty(key);
            setKingdomSpawn = val != null ? val.equals("true") : false;
            if (setKingdomSpawn) {
                village.setSpawnSituation((byte)1);
            } else {
                village.setSpawnSituation((byte)0);
            }
            boolean setAllowAggros = false;
            key = "aggros";
            val = props.getProperty(key);
            setAllowAggros = val != null ? val.equals("true") : false;
            try {
                village.setAllowsAggroCreatures(setAllowAggros);
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, "Failed to set " + village.getName() + " to setAllowsAggros - " + setAllowAggros + ":" + iox.getMessage(), iox);
            }
            responder.getCommunicator().sendSafeServerMessage("Settings updated.");
        }
        catch (NoSuchItemException nsi) {
            logger.log(Level.WARNING, "Failed to locate settlement deed with id " + target, nsi);
            responder.getCommunicator().sendNormalServerMessage("Failed to locate the deed item for that request. Please contact administration.");
        }
        catch (NoSuchVillageException nsv) {
            logger.log(Level.WARNING, "Failed to locate settlement for deed with id " + target, nsv);
            responder.getCommunicator().sendNormalServerMessage("Failed to locate the settlement for that request. Please contact administration.");
        }
        catch (IOException ioe) {
            responder.getCommunicator().sendNormalServerMessage("Failed to change name of settlement. Please contact administration.");
        }
    }

    static void parseVillageWarQuestion(DeclareWarQuestion question) {
        Creature responder = question.getResponder();
        boolean declare = question.getAnswer().getProperty("declare").equals("true");
        if (responder.getCitizenVillage() != null) {
            if (!declare) {
                responder.getCommunicator().sendNormalServerMessage("You decide not to declare war.");
            } else {
                Villages.declareWar(responder.getCitizenVillage(), question.getTargetVillage());
            }
        } else {
            responder.getCommunicator().sendNormalServerMessage("You are not citizen of a village.");
        }
    }

    static void parseVillagePeaceQuestion(PeaceQuestion question) {
        Creature asker = question.getResponder();
        Creature responder = question.getInvited();
        boolean peace = question.getAnswer().getProperty("peace").equals("true");
        Village village = asker.getCitizenVillage();
        if (!peace) {
            responder.getCommunicator().sendNormalServerMessage("You decline the peace offer.");
            asker.getCommunicator().sendNormalServerMessage(responder.getName() + " declines your generous peace offer!");
        } else {
            Villages.declarePeace(asker, responder, village, responder.getCitizenVillage());
        }
    }

    static void parseVillageJoinQuestion(VillageJoinQuestion question) {
        Creature asker = question.getResponder();
        Creature responder = question.getInvited();
        boolean join = question.getAnswer().getProperty("join").equals("true");
        Village village = asker.getCitizenVillage();
        if (!join) {
            responder.getCommunicator().sendNormalServerMessage("You decline to join the settlement.");
            asker.getCommunicator().sendNormalServerMessage(responder.getName() + " declines to join the settlement.");
        } else if (responder.isPlayer() && responder.mayChangeVillageInMillis() > 0L) {
            responder.getCommunicator().sendNormalServerMessage("You may not change settlement in " + Server.getTimeFor(responder.mayChangeVillageInMillis()) + ".");
            asker.getCommunicator().sendNormalServerMessage(responder.getName() + " may join your settlement in " + Server.getTimeFor(responder.mayChangeVillageInMillis()) + ".");
        } else {
            try {
                village.addCitizen(responder, village.getRoleForStatus((byte)3));
                if (((Player)responder).canUseFreeVillageTeleport()) {
                    VillageTeleportQuestion vtq = new VillageTeleportQuestion(responder);
                    vtq.sendQuestion();
                }
            }
            catch (IOException iox) {
                logger.log(Level.INFO, "Failed to add " + responder.getName() + " to settlement " + village.getName() + "." + iox.getMessage(), iox);
                responder.getCommunicator().sendNormalServerMessage("Failed to add you to the settlement. Please contact administration.");
                asker.getCommunicator().sendNormalServerMessage("Failed to add " + responder.getName() + " to the settlement. Please contact administration.");
            }
            catch (NoSuchRoleException nsr) {
                logger.log(Level.INFO, "Failed to add " + responder.getName() + " to settlement " + village.getName() + "." + nsr.getMessage(), nsr);
                responder.getCommunicator().sendNormalServerMessage("Failed to add you to the settlement. Please contact administration.");
                asker.getCommunicator().sendNormalServerMessage("Failed to add " + responder.getName() + " to the settlement. Please contact administration.");
            }
        }
    }

    static void parseVillageCitizenManageQuestion(VillageCitizenManageQuestion question) {
        Creature responder = question.getResponder();
        long target = question.getTarget();
        Properties props = question.getAnswer();
        try {
            Village village;
            if (target == -10L) {
                village = responder.getCitizenVillage();
                if (village == null) {
                    throw new NoSuchVillageException("You are not a citizen of any village (on this server).");
                }
            } else {
                Item deed = Items.getItem(target);
                int villageId = deed.getData2();
                village = Villages.getVillage(villageId);
            }
            boolean unlimitedCitizens = false;
            String key = "unlimitC";
            String valu = question.getAnswer().getProperty("unlimitC");
            unlimitedCitizens = valu != null ? valu.equals("true") : false;
            try {
                village.setUnlimitedCitizens(unlimitedCitizens);
            }
            catch (IOException iox) {
                logger.log(Level.WARNING, "Failed to set " + village.getName() + " to unlimitedC - " + unlimitedCitizens + ":" + iox.getMessage(), iox);
            }
            if (question.isSelecting()) {
                String startLetter = props.getProperty("selectRange");
                VillageCitizenManageQuestion vc = new VillageCitizenManageQuestion(responder, "Citizen management", "Set statuses of citizens.", target);
                vc.setSelecting(false);
                if (startLetter != null) {
                    if (startLetter.equals("0")) {
                        vc.setAllowedLetters("abcdef");
                    } else if (startLetter.equals("1")) {
                        vc.setAllowedLetters("ghijkl");
                    } else if (startLetter.equals("2")) {
                        vc.setAllowedLetters("mnopqr");
                    } else if (startLetter.equals("3")) {
                        vc.setAllowedLetters("stuvwxyz");
                    }
                }
                vc.sendQuestion();
                return;
            }
            VillageRole[] roles = village.getRoles();
            for (Integer i : question.getIdMap().keySet()) {
                int x = i;
                long citid = question.getIdMap().get(i);
                Citizen citiz = village.getCitizen(citid);
                if (citiz == null) continue;
                try {
                    String revString = (String)props.get(x + "revoke");
                    boolean revoked = false;
                    if (revString != null) {
                        revoked = revString.equals("true");
                    }
                    if (!revoked) {
                        String val = (String)props.get(String.valueOf(x));
                        if (val == null) continue;
                        int roleid = Integer.parseInt(val);
                        VillageRole newRole = null;
                        int count = 0;
                        for (int r = 0; r < roles.length; ++r) {
                            if (roles[r].getStatus() == 4 || roles[r].getStatus() == 5 || roles[r].getStatus() == 1 || roles[r].getStatus() == 6) continue;
                            if (roleid == count) {
                                newRole = roles[r];
                                break;
                            }
                            ++count;
                        }
                        if (newRole == null) {
                            responder.getCommunicator().sendNormalServerMessage("Failed to locate role for " + citiz.getName() + ".");
                            continue;
                        }
                        VillageRole role = citiz.getRole();
                        if (role.getStatus() == 2 && newRole.getStatus() != 2) {
                            responder.getCommunicator().sendNormalServerMessage(citiz.getName() + " is the mayor as long as he/she possesses the village deed. You cannot change that manually.");
                            continue;
                        }
                        if (newRole.getStatus() != 2) {
                            if (role.equals(newRole)) continue;
                            village.updateGatesForRole(newRole);
                            citiz.setRole(newRole);
                            village.updateGatesForRole(newRole);
                            responder.getCommunicator().sendNormalServerMessage(citiz.getName() + " role changed to \"" + newRole.getName() + "\".");
                            continue;
                        }
                        if (role.equals(newRole)) continue;
                        responder.getCommunicator().sendNormalServerMessage("Did not set " + citiz.getName() + " to Mayor. The possessor of the village deed is Mayor.");
                        continue;
                    }
                    if (village.isDemocracy()) continue;
                    if (citiz.getRole().getStatus() == 2) {
                        responder.getCommunicator().sendNormalServerMessage("You cannot revoke the citizenship of the mayor this way. He/she has to give away the village deed.");
                        continue;
                    }
                    if (citiz.getId() == responder.getWurmId()) {
                        responder.getCommunicator().sendNormalServerMessage("You must revoke your own citizenship by typing '/revoke " + village.getName() + "' on the command line.");
                        continue;
                    }
                    village.removeCitizen(citiz);
                }
                catch (NumberFormatException nfe) {
                    logger.log(Level.WARNING, "This is bad: " + nfe.getMessage(), nfe);
                }
                catch (IOException iox) {
                    logger.log(Level.WARNING, "This is bad: " + iox.getMessage(), iox);
                    responder.getCommunicator().sendNormalServerMessage("Failed to set role for one or more citizens. Please contact administration.");
                }
            }
        }
        catch (NoSuchItemException nsi) {
            logger.log(Level.WARNING, "Failed to locate village deed with id " + target, nsi);
            responder.getCommunicator().sendNormalServerMessage("Failed to locate the deed item for that request. Please contact administration.");
        }
        catch (NoSuchVillageException nsv) {
            logger.log(Level.WARNING, "Failed to locate village for deed with id " + target, nsv);
            responder.getCommunicator().sendNormalServerMessage("Failed to locate the village for that request. Please contact administration.");
        }
    }

    static final boolean charge(Creature responder, long coinsNeeded, String reason, float taxrate) throws FailedException {
        Item[] items = responder.getInventory().getAllItems(false);
        LinkedList<Item> coins = new LinkedList<Item>();
        long value = 0L;
        for (int x = 0; x < items.length; ++x) {
            if (!items[x].isCoin()) continue;
            coins.add(items[x]);
            value += (long)Economy.getValueFor(items[x].getTemplateId());
        }
        if (value < coinsNeeded) {
            Change change = new Change(coinsNeeded);
            throw new FailedException("You need " + change.getChangeString() + " coins.");
        }
        long curv = 0L;
        ListIterator it = coins.listIterator();
        while (it.hasNext()) {
            Item coin = (Item)it.next();
            curv += (long)Economy.getValueFor(coin.getTemplateId());
            try {
                Item parent = coin.getParent();
                parent.dropItem(coin.getWurmId(), false);
                Economy.getEconomy().returnCoin(coin, reason);
            }
            catch (NoSuchItemException nsi) {
                logger.log(Level.WARNING, responder.getName() + ":  Failed to locate the container for coin " + coin.getName() + ". Value returned is " + new Change(curv).getChangeString() + " coins.", nsi);
                Item[] newCoins = Economy.getEconomy().getCoinsFor(Economy.getValueFor(coin.getTemplateId()));
                Item inventory = responder.getInventory();
                for (int x = 0; x < newCoins.length; ++x) {
                    inventory.insertItem(newCoins[x]);
                }
                throw new FailedException("Failed to locate the container for coin " + coin.getName() + ". This is serious and should be reported. Returned " + new Change(curv).getChangeString() + " coins.", nsi);
            }
            if (curv < coinsNeeded) continue;
            break;
        }
        if (curv > coinsNeeded) {
            Item[] newCoins = Economy.getEconomy().getCoinsFor(curv - coinsNeeded);
            Item inventory = responder.getInventory();
            for (int x = 0; x < newCoins.length; ++x) {
                inventory.insertItem(newCoins[x]);
            }
        }
        Shop kingsMoney = Economy.getEconomy().getKingsShop();
        if (taxrate > 1.0f) {
            logger.log(Level.WARNING, responder.getName() + ":  Taxrate should be max 1 but is " + taxrate, new Exception());
            taxrate = 1.0f;
        }
        kingsMoney.setMoney(kingsMoney.getMoney() + (long)((float)coinsNeeded * (1.0f - taxrate)));
        logger.log(Level.INFO, "King now has " + kingsMoney.getMoney());
        return true;
    }

    static void parseGuardRentalQuestion(GuardManagementQuestion question) {
        String val;
        String key;
        Creature responder;
        Properties props;
        block26: {
            props = question.getAnswer();
            responder = question.getResponder();
            key = "12345678910";
            val = null;
            Village village = responder.citizenVillage;
            long money = responder.getMoney();
            if (money > 0L) {
                long valueWithdrawn = QuestionParser.getValueWithdrawn(question);
                if (valueWithdrawn > 0L) {
                    try {
                        if (village.plan != null) {
                            if (responder.chargeMoney(valueWithdrawn)) {
                                village.plan.addMoney(valueWithdrawn);
                                village.plan.addPayment(responder.getName(), responder.getWurmId(), valueWithdrawn);
                                Change newch = Economy.getEconomy().getChangeFor(valueWithdrawn);
                                responder.getCommunicator().sendNormalServerMessage("You pay " + newch.getChangeString() + " to the upkeep fund of " + village.getName() + ".");
                                logger.log(Level.INFO, responder.getName() + " added " + valueWithdrawn + " irons to " + village.getName() + " upkeep.");
                            } else {
                                responder.getCommunicator().sendNormalServerMessage("You don't have that much money.");
                            }
                            break block26;
                        }
                        responder.getCommunicator().sendNormalServerMessage("This village does not have an upkeep plan.");
                    }
                    catch (IOException iox) {
                        logger.log(Level.WARNING, "Failed to withdraw money from " + responder.getName() + ":" + iox.getMessage(), iox);
                        responder.getCommunicator().sendNormalServerMessage("The transaction failed. Please contact the game masters using the <i>/dev</i> command.");
                    }
                } else {
                    responder.getCommunicator().sendNormalServerMessage("No money withdrawn.");
                }
            }
        }
        if (responder.mayManageGuards()) {
            GuardPlan plan = responder.getCitizenVillage().plan;
            if (plan != null) {
                int nums;
                boolean changed = false;
                key = "hired";
                val = (String)props.get(key);
                int oldnums = nums = plan.getNumHiredGuards();
                if (val != null) {
                    try {
                        nums = Integer.parseInt(val);
                    }
                    catch (NumberFormatException nfe) {
                        responder.getCommunicator().sendNormalServerMessage("Failed to parse the value " + val + ". Please enter a number if you wish to change the number of guards.");
                        return;
                    }
                    if (nums != plan.getNumHiredGuards()) {
                        boolean aboveMax = nums > GuardPlan.getMaxGuards(responder.getCitizenVillage());
                        int diff = (nums = Math.min(nums, GuardPlan.getMaxGuards(responder.getCitizenVillage()))) - plan.getNumHiredGuards();
                        if (diff > 0) {
                            long moneyOver = plan.moneyLeft - plan.calculateMonthlyUpkeepTimeforType(0);
                            if (moneyOver > (long)(10000 * diff)) {
                                changed = true;
                                plan.changePlan(0, nums);
                                plan.updateGuardPlan(0, plan.moneyLeft - (long)(10000 * diff), nums);
                            } else {
                                responder.getCommunicator().sendNormalServerMessage("There was not enough upkeep to increase the number of guards. Please make sure that there is at least one month of upkeep left after you hire the guards.");
                            }
                        } else if (diff < 0) {
                            changed = true;
                            plan.changePlan(0, nums);
                        }
                        if (aboveMax) {
                            responder.getCommunicator().sendNormalServerMessage("You tried to increase the amount of guards above the max of " + GuardPlan.getMaxGuards(responder.getCitizenVillage()) + " which was denied.");
                        }
                    }
                } else {
                    responder.getCommunicator().sendNormalServerMessage("Failed to parse the value " + val + ". Please enter a number if you wish to change the number of guards.");
                    return;
                }
                if (changed && oldnums < nums) {
                    responder.getCommunicator().sendNormalServerMessage("You change the upkeep plan. New guards will arrive soon.");
                } else if (changed) {
                    responder.getCommunicator().sendNormalServerMessage("You change the upkeep plan.");
                } else {
                    responder.getCommunicator().sendNormalServerMessage("No change was made.");
                }
            }
        } else {
            logger.log(Level.WARNING, responder.getName() + " tried to manage guards without the right.");
        }
    }

    private static final long getValueWithdrawn(Question question) {
        String golds = question.getAnswer().getProperty("gold");
        String silvers = question.getAnswer().getProperty("silver");
        String coppers = question.getAnswer().getProperty("copper");
        String irons = question.getAnswer().getProperty("iron");
        try {
            long wantedGold = 0L;
            if (golds != null && golds.length() > 0) {
                wantedGold = Long.parseLong(golds);
            }
            long wantedSilver = 0L;
            if (silvers != null && silvers.length() > 0) {
                wantedSilver = Long.parseLong(silvers);
            }
            long wantedCopper = 0L;
            if (coppers != null && coppers.length() > 0) {
                wantedCopper = Long.parseLong(coppers);
            }
            long wantedIron = 0L;
            if (irons != null && irons.length() > 0) {
                wantedIron = Long.parseLong(irons);
            }
            if (wantedGold < 0L) {
                question.getResponder().getCommunicator().sendNormalServerMessage("You may not withdraw a negative amount of gold coins!");
                return 0L;
            }
            if (wantedSilver < 0L) {
                question.getResponder().getCommunicator().sendNormalServerMessage("You may not withdraw a negative amount of silver coins!");
                return 0L;
            }
            if (wantedCopper < 0L) {
                question.getResponder().getCommunicator().sendNormalServerMessage("You may not withdraw a negative amount of copper coins!");
                return 0L;
            }
            if (wantedIron < 0L) {
                question.getResponder().getCommunicator().sendNormalServerMessage("You may not withdraw a negative amount of iron coins!");
                return 0L;
            }
            long valueWithdrawn = 1000000L * wantedGold;
            valueWithdrawn += 10000L * wantedSilver;
            valueWithdrawn += 100L * wantedCopper;
            return valueWithdrawn += 1L * wantedIron;
        }
        catch (NumberFormatException nfe) {
            question.getResponder().getCommunicator().sendNormalServerMessage("The values were incorrect.");
            return 0L;
        }
    }

    static final void parsePlayerPaymentQuestion(PlayerPaymentQuestion question) {
        Properties props = question.getAnswer();
        String purch = props.getProperty("purchase");
        Creature responder = question.getResponder();
        boolean purchase = Boolean.parseBoolean(purch);
        long money = responder.getMoney();
        if (money < 100000L) {
            responder.getCommunicator().sendAlertServerMessage("You do not have enough money to purchase game time for. You need at least 10 silver in your bank account.");
        } else if (purchase) {
            try {
                if (responder.chargeMoney(100000L)) {
                    LoginServerWebConnection lsw = new LoginServerWebConnection();
                    lsw.addPlayingTime(responder, responder.getName(), 1, 0, System.currentTimeMillis() + Servers.localServer.name);
                    responder.getCommunicator().sendSafeServerMessage("Your request for playing time is being processed. It may take up to half an hour until the system is fully updated.");
                    long diff = 30000L;
                    Economy.getEconomy().getKingsShop().setMoney(Economy.getEconomy().getKingsShop().getMoney() + 30000L);
                    logger.log(Level.INFO, responder.getName() + " purchased 1 month premium time for " + 10L + " silver coins. " + 30000L + " iron added to king.");
                } else {
                    responder.getCommunicator().sendAlertServerMessage("Failed to charge you 10 silvers. Please try later.");
                }
            }
            catch (IOException ex) {
                responder.getCommunicator().sendSafeServerMessage("Your request for playing time could not be processed.");
            }
        }
    }

    static String generateGuardMaleName() {
        int rand = Server.rand.nextInt(50);
        String[] firstPart = new String[]{"Carl", "John", "Bil", "Strong", "Dare", "Grave", "Hard", "Marde", "Verde", "Vold", "Tolk", "Roe", "Bee", "Har", "Rol", "Ma", "Lo", "Claw", "Drag", "Hug", "Te", "Two", "Fu", "Ji", "La", "Ze", "Jal", "Milk", "War", "Wild", "Hang", "Just", "Fan", "Cloclo", "Buy", "Bought", "Sard", "Smart", "Slo", "Shield", "Dark", "Hung", "Sed", "Sold", "Swing", "Gar", "Dig", "Bur", "Angel", "Sorrow"};
        int rand2 = Server.rand.nextInt(50);
        String[] secondPart = new String[]{"ho", "john", "fish", "tree", "ooy", "olli", "tack", "rank", "sy", "moy", "dangly", "tok", "rich", "do", "mark", "stuf", "sin", "nyt", "wer", "mor", "emort", "vaar", "salm", "holm", "wyr", "zah", "ty", "fast", "der", "mar", "star", "bark", "oo", "flifil", "innow", "shoo", "husk", "eric", "ic", "o", "moon", "little", "ien", "strong", "arm", "hope", "slem", "tro", "rot", "heart"};
        return firstPart[rand] + secondPart[rand2];
    }

    static String generateGuardFemaleName() {
        int rand = Server.rand.nextInt(50);
        String[] firstPart = new String[]{"Too", "Sand", "Tree", "Whisper", "Lore", "Yan", "Van", "Vard", "Nard", "Oli", "Ala", "Krady", "Whe", "Har", "Zizi", "Zaza", "Lyn", "Claw", "Mali", "High", "Bright", "Star", "Nord", "Jala", "Yna", "Ze", "Jal", "Milk", "War", "Wild", "Fine", "Sweet", "Witty", "Cloclo", "Lory", "Tran", "Vide", "Lax", "Quick", "Shield", "Dark", "Light", "Cry", "Sold", "Juna", "Tear", "Cheek", "Ani", "Angel", "Sorro"};
        int rand2 = Server.rand.nextInt(50);
        String[] secondPart = new String[]{"peno", "hag", "maiden", "woman", "loy", "oa", "dei", "sai", "nai", "nae", "ane", "aei", "peno", "doa", "ela", "hofaire", "sina", "nyta", "wera", "more", "emorta", "vaara", "salma", "holmi", "wyre", "zahe", "tya", "faste", "dere", "mara", "stare", "barkia", "ooa", "fila", "innowyn", "shoein", "huskyn", "erica", "ica", "oa", "moonie", "littly", "ieny", "strongie", "ermy", "hope", "steam", "high", "wind", "heart"};
        return firstPart[rand] + secondPart[rand2];
    }

    public static final void parseFriendQuestion(FriendQuestion question) {
        Properties props = question.getAnswer();
        Player responder = (Player)question.getResponder();
        try {
            Player sender = Players.getInstance().getPlayer(question.getTarget());
            String accept = props.getProperty("join");
            if (accept != null) {
                if (accept.equals("accept")) {
                    sender.addFriend(responder.getWurmId(), Friend.Category.Other.getCatId(), "");
                    responder.addFriend(sender.getWurmId(), Friend.Category.Other.getCatId(), "");
                    sender.getCommunicator().sendNormalServerMessage("You are now friends with " + responder.getName() + ".");
                    responder.getCommunicator().sendNormalServerMessage("You are now friends with " + sender.getName() + ".");
                } else {
                    responder.getCommunicator().sendNormalServerMessage("You decline the friendlist offer.");
                    sender.getCommunicator().sendNormalServerMessage(responder.getName() + " declines your friends list invitation.");
                }
            }
        }
        catch (NoSuchPlayerException nsp) {
            responder.getCommunicator().sendNormalServerMessage("The player who wanted to add you to the friends list has logged off. You will not be added.");
        }
    }

    static final void parsePvPAllianceQuestion(AllianceQuestion question) {
        try {
            Properties props = question.getAnswer();
            Creature responder = question.getResponder();
            Creature sender = Server.getInstance().getCreature(question.getTarget());
            Village senderVillage = sender.getCitizenVillage();
            Village responderVillage = responder.getCitizenVillage();
            PvPAlliance respAlliance = PvPAlliance.getPvPAlliance(responderVillage.getAllianceNumber());
            if (respAlliance != null) {
                sender.getCommunicator().sendAlertServerMessage(responder.getName() + " is already in the " + respAlliance.getName() + " alliance.");
                responder.getCommunicator().sendAlertServerMessage("You are already in the " + respAlliance.getName() + " alliance.");
                return;
            }
            String accept = props.getProperty("join");
            if (accept != null) {
                if (accept.equals("accept")) {
                    PvPAlliance oldAlliance = PvPAlliance.getPvPAlliance(sender.getCitizenVillage().getAllianceNumber());
                    if (oldAlliance != null) {
                        responder.getCitizenVillage().setAllianceNumber(oldAlliance.getId());
                        senderVillage.broadCastNormal("Under the rule of " + senderVillage.getMayor().getName() + " of " + senderVillage.getName() + ", " + responderVillage.getName() + " has been convinced to join the " + oldAlliance.getName() + ". Citizens rejoice!");
                        responderVillage.broadCastNormal("Under the rule of " + responderVillage.getMayor().getName() + ", " + responderVillage.getName() + " has joined the " + oldAlliance.getName() + ". Citizens rejoice!");
                        oldAlliance.setWins(Math.max(oldAlliance.getNumberOfWins(), responder.getCitizenVillage().getHotaWins()));
                        responder.getCitizenVillage().sendMapAnnotationsToVillagers(oldAlliance.getAllianceMapAnnotationsArray());
                    } else {
                        PvPAlliance newAlliance = new PvPAlliance(sender.getCitizenVillage().getId(), question.getAllianceName());
                        responder.getCitizenVillage().setAllianceNumber(newAlliance.getId());
                        sender.getCitizenVillage().setAllianceNumber(newAlliance.getId());
                        senderVillage.broadCastNormal("Under the rule of " + senderVillage.getMayor().getName() + " of " + senderVillage.getName() + ", " + responderVillage.getName() + " has been convinced to form the " + newAlliance.getName() + " alliance. Citizens rejoice!");
                        responderVillage.broadCastNormal("Under the rule of " + responderVillage.getMayor().getName() + ", " + responderVillage.getName() + " has formed the " + newAlliance.getName() + " alliance. Citizens rejoice!");
                        newAlliance.setWins(Math.max(responder.getCitizenVillage().getHotaWins(), sender.getCitizenVillage().getHotaWins()));
                        responder.getCitizenVillage().sendMapAnnotationsToVillagers(newAlliance.getAllianceMapAnnotationsArray());
                    }
                } else {
                    responder.getCommunicator().sendNormalServerMessage("You decline the alliance offer.");
                    sender.getCommunicator().sendNormalServerMessage(responder.getName() + " declines your generous offer to form an alliance.");
                }
            }
        }
        catch (NoSuchCreatureException nsc) {
            logger.log(Level.WARNING, nsc.getMessage(), nsc);
        }
        catch (NoSuchPlayerException nsp) {
            logger.log(Level.WARNING, nsp.getMessage(), nsp);
        }
    }

    static final void parseManageAllianceQuestion(ManageAllianceQuestion question) {
        String val;
        Properties props = question.getAnswer();
        Creature responder = question.getResponder();
        Village responderVillage = responder.getCitizenVillage();
        PvPAlliance pvpAll = PvPAlliance.getPvPAlliance(responderVillage.getAllianceNumber());
        if (pvpAll != null && responderVillage.mayDoDiplomacy(responder)) {
            String motd = question.getAnswer().getProperty("motd");
            if (motd != null) {
                pvpAll.setMotd(motd.replaceAll("\"", ""));
            }
            Village[] alliances = question.getAllies();
            for (int x = 0; x < alliances.length; ++x) {
                int id = alliances[x].getId();
                String val2 = props.getProperty("break" + String.valueOf(id));
                if (val2 == null || !Boolean.parseBoolean(val2)) continue;
                if (alliances[x].getId() == pvpAll.getId()) {
                    responder.getCitizenVillage().setAllianceNumber(0);
                    responder.getCitizenVillage().sendClearMapAnnotationsOfType((byte)2);
                    responder.getCitizenVillage().broadCastAlert(responder.getName() + " made " + responder.getCitizenVillage().getName() + " leave the " + pvpAll.getName() + ".");
                    alliances[x].broadCastAlert(responder.getCitizenVillage().getName() + " has left the " + pvpAll.getName());
                    alliances[x].addHistory(responder.getCitizenVillage().getName(), "left the " + pvpAll.getName() + ".");
                    if (pvpAll.exists()) continue;
                    alliances[x].broadCastAlert(pvpAll.getName() + " alliance has been disbanded.");
                    pvpAll.delete();
                    pvpAll.sendClearAllianceAnnotations();
                    pvpAll.deleteAllianceMapAnnotations();
                    alliances[x].setAllianceNumber(0);
                    continue;
                }
                if (responder.getCitizenVillage().getId() == pvpAll.getId()) {
                    alliances[x].setAllianceNumber(0);
                    alliances[x].sendClearMapAnnotationsOfType((byte)2);
                    responder.getCitizenVillage().broadCastAlert(responder.getName() + " ousted " + alliances[x].getName() + " from the " + pvpAll.getName() + ".");
                    alliances[x].broadCastAlert("You have been ousted from the " + pvpAll.getName() + ".");
                    alliances[x].addHistory(responder.getName(), "ousted you from the " + pvpAll.getName() + ".");
                    if (pvpAll.exists()) continue;
                    responder.getCitizenVillage().broadCastAlert(pvpAll.getName() + " alliance has been disbanded.");
                    pvpAll.delete();
                    pvpAll.sendClearAllianceAnnotations();
                    pvpAll.deleteAllianceMapAnnotations();
                    responder.getCitizenVillage().setAllianceNumber(0);
                    continue;
                }
                try {
                    Villages.getVillage(pvpAll.getId());
                }
                catch (NoSuchVillageException nsv) {
                    responder.getCitizenVillage().setAllianceNumber(0);
                    responder.getCitizenVillage().sendClearMapAnnotationsOfType((byte)2);
                    logger.log(Level.INFO, responder.getName() + " made " + responder.getCitizenVillage().getName() + " leave the " + pvpAll.getName() + ".");
                    responder.getCitizenVillage().broadCastAlert(responder.getName() + " made " + responder.getCitizenVillage().getName() + " leave the " + pvpAll.getName() + ".");
                    alliances[x].broadCastAlert(responder.getCitizenVillage().getName() + " has left the " + pvpAll.getName());
                    alliances[x].addHistory(responder.getCitizenVillage().getName(), "left the " + pvpAll.getName() + ".");
                }
                if (pvpAll.exists()) continue;
                alliances[x].broadCastAlert(pvpAll.getName() + " alliance has been disbanded.");
                pvpAll.delete();
                pvpAll.sendClearAllianceAnnotations();
                pvpAll.deleteAllianceMapAnnotations();
                alliances[x].setAllianceNumber(0);
            }
            String key = "declareWar";
            val = props.getProperty(key);
            if (responderVillage.getMayor().getId() == responder.getWurmId()) {
                key = "masterVill";
                val = props.getProperty(key);
                if (val != null && val.length() > 0) {
                    try {
                        int index = Integer.parseInt(val);
                        if (index < alliances.length) {
                            if (alliances[index].getAllianceNumber() == responderVillage.getAllianceNumber()) {
                                pvpAll.transferControl(responder, alliances[index].getId());
                            } else {
                                responder.getCommunicator().sendNormalServerMessage("Unable to set " + alliances[index].getName() + " as alliance capital, as they are no longer in the alliance");
                            }
                        }
                    }
                    catch (NumberFormatException nfe) {
                        responder.getCommunicator().sendAlertServerMessage("Failed to parse value for new capital.");
                    }
                }
                if ((val = props.getProperty(key = "disbandAll")) != null && val.equals("true")) {
                    pvpAll.disband(responder);
                }
                if ((val = props.getProperty(key = "allName")) != null && val.length() > 0) {
                    pvpAll.setName(responder, val);
                }
            }
        }
        if (responderVillage.warDeclarations != null) {
            WarDeclaration[] declArr = responderVillage.warDeclarations.values().toArray(new WarDeclaration[responderVillage.warDeclarations.size()]);
            for (int y = 0; y < declArr.length; ++y) {
                int id;
                if (declArr[y].declarer == responderVillage) {
                    id = declArr[y].receiver.getId();
                    val = props.getProperty("decl" + id);
                    if (!Boolean.parseBoolean(val)) continue;
                    declArr[y].dissolve(false);
                    continue;
                }
                id = declArr[y].declarer.getId();
                val = props.getProperty("recv" + id);
                if (!Boolean.parseBoolean(val)) continue;
                declArr[y].accept();
            }
        }
    }

    public static final void parsePaymentQuestion(PaymentQuestion question) {
        Properties props = question.getAnswer();
        String ds = props.getProperty("days");
        String ms = props.getProperty("months");
        String wid = props.getProperty("wurmid");
        Creature responder = question.getResponder();
        int days = 0;
        int months = 0;
        int playerid = 0;
        long wurmid = -10L;
        if (ds != null && ds.length() > 0) {
            try {
                days = Integer.parseInt(ds);
            }
            catch (NumberFormatException nfe) {
                responder.getCommunicator().sendAlertServerMessage("Failed to parse the string " + ds + " to a valid number.");
                return;
            }
        }
        if (ms != null && ms.length() > 0) {
            try {
                months = Integer.parseInt(ms);
            }
            catch (NumberFormatException nfe) {
                responder.getCommunicator().sendAlertServerMessage("Failed to parse the string " + ms + " to a valid number.");
                return;
            }
        }
        if (wid != null) {
            try {
                playerid = Integer.parseInt(wid);
                Long pid = question.getPlayerId(playerid);
                wurmid = pid;
            }
            catch (NumberFormatException nfe) {
                responder.getCommunicator().sendAlertServerMessage("Failed to parse the string " + wid + " to a valid number.");
                return;
            }
        }
        if (wurmid != -10L) {
            LoginServerWebConnection wit = new LoginServerWebConnection();
            PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(wurmid);
            if (pinf != null) {
                if (wit != null && wit.addPlayingTime(responder, pinf.getName(), months, days, responder.getName() + Server.rand.nextInt(10000) + "add" + Servers.localServer.id)) {
                    responder.getCommunicator().sendNormalServerMessage("Ok, added " + months + " months and " + days + " days to " + pinf.getName() + ".");
                }
            } else {
                responder.getCommunicator().sendAlertServerMessage("Failed to find a player with that wurmid. Try on the login server.");
                return;
            }
            responder.getCommunicator().sendNormalServerMessage("The payment request is being processed.");
        }
    }

    public static final void parsePowerManagementQuestion(PowerManagementQuestion question) {
        block23: {
            Properties props = question.getAnswer();
            String pow = props.getProperty("power");
            String wid = props.getProperty("wurmid");
            Creature responder = question.getResponder();
            if (responder.getPower() >= 3) {
                long wurmid = -10L;
                byte power = 0;
                if (pow != null) {
                    try {
                        power = Byte.parseByte(pow);
                    }
                    catch (NumberFormatException nfe) {
                        responder.getCommunicator().sendNormalServerMessage("Failed to parse the string " + pow + " to a valid number between -127 and 127.");
                        return;
                    }
                }
                if (wid != null) {
                    try {
                        int pos = Integer.parseInt(wid);
                        Long widdy = question.getPlayerId(pos);
                        wurmid = widdy;
                    }
                    catch (NumberFormatException nfe) {
                        responder.getCommunicator().sendNormalServerMessage("Failed to parse the string " + wid + " to a valid number.");
                        return;
                    }
                }
                if (wurmid != -10L) {
                    try {
                        Player player = Players.getInstance().getPlayer(wurmid);
                        if (responder.getPower() >= power) {
                            try {
                                player.setPower(power);
                                String powString = "normal adventurer";
                                if (power == 1) {
                                    powString = "hero";
                                } else if (power == 2) {
                                    powString = "demigod";
                                } else if (power == 3) {
                                    powString = "high god";
                                } else if (power == 4) {
                                    powString = "arch angel";
                                } else if (power == 5) {
                                    powString = "implementor";
                                }
                                player.getCommunicator().sendSafeServerMessage("Your status has been set by " + responder.getName() + " to " + powString + "!");
                                responder.getCommunicator().sendSafeServerMessage("You set the power of " + player.getName() + " to the status of " + powString);
                                responder.getLogger().log(Level.INFO, responder.getName() + " set the power of " + player.getName() + " to " + powString + ".");
                            }
                            catch (IOException iox) {
                                logger.log(Level.WARNING, "Failed to change the power of " + player.getName() + " to " + power + ": " + iox.getMessage(), iox);
                                responder.getCommunicator().sendSafeServerMessage("Failed to set the power of " + player.getName() + " to the new status: " + iox.getMessage());
                            }
                            break block23;
                        }
                        responder.getCommunicator().sendSafeServerMessage("You can not set powers above your level.");
                    }
                    catch (NoSuchPlayerException nsp) {
                        responder.getCommunicator().sendNormalServerMessage("Failed to locate player with wurmid " + wurmid + ".");
                    }
                }
            } else {
                logger.warning(responder.getName() + " tried to set the power of a Player but did not have enough power.");
            }
        }
    }

    static void parseGmVillageAdQuestion(GmVillageAdInterface question) {
        Properties props = question.getAnswer();
        RecruitmentAd[] ads = RecruitmentAds.getAllRecruitmentAds();
        for (int i = 0; i < ads.length; ++i) {
            String key = ads[i].getVillageId() + "remove";
            String val = props.getProperty(key);
            if (val == null || !Boolean.parseBoolean(val)) continue;
            RecruitmentAds.remove(ads[i]);
        }
    }

    static void parseTraderManagementQuestion(TraderManagementQuestion question) {
        long traderId;
        Creature trader;
        Properties props;
        Item contract;
        Shop shop;
        Creature responder;
        block39: {
            responder = question.getResponder();
            shop = null;
            contract = null;
            props = question.getAnswer();
            trader = null;
            traderId = -1L;
            try {
                contract = Items.getItem(question.getTarget());
                if (contract.getOwner() == responder.getWurmId()) {
                    traderId = contract.getData();
                    if (traderId != -1L) {
                        trader = Server.getInstance().getCreature(traderId);
                        shop = Economy.getEconomy().getShop(trader);
                    }
                    break block39;
                }
                responder.getCommunicator().sendNormalServerMessage("You are no longer in possesion of the " + contract.getName() + "!");
                return;
            }
            catch (NoSuchItemException nsi) {
                logger.log(Level.WARNING, responder.getName() + " contract is missing! Contract ID: " + question.getTarget());
                responder.getCommunicator().sendNormalServerMessage("You are no longer in possesion of the contract!");
                return;
            }
            catch (NoSuchPlayerException nsp) {
                logger.log(Level.WARNING, "Trader for " + responder.getName() + " is a player? Well it can't be found. Contract ID: " + question.getTarget());
                responder.getCommunicator().sendNormalServerMessage("The contract has been damaged by water. You can't read the letters!");
                if (contract != null) {
                    contract.setData(-1, -1);
                }
                return;
            }
            catch (NoSuchCreatureException nsc) {
                logger.log(Level.WARNING, "Trader for " + responder.getName() + " can't be found. Contract ID: " + question.getTarget());
                responder.getCommunicator().sendNormalServerMessage("The contract has been damaged by water. You can't read the letters!");
                if (contract != null) {
                    contract.setData(-1, -1);
                }
                return;
            }
            catch (NotOwnedException no) {
                responder.getCommunicator().sendNormalServerMessage("You are no longer in possesion of the " + contract.getName() + "!");
                return;
            }
        }
        if (shop != null) {
            String key = traderId + "dismiss";
            String val = props.getProperty(key);
            if (Boolean.parseBoolean(val)) {
                if (trader != null) {
                    if (!trader.isTrading()) {
                        Server.getInstance().broadCastAction(trader.getName() + " grunts, packs " + trader.getHisHerItsString() + " things and is off.", trader, 5);
                        responder.getCommunicator().sendNormalServerMessage("You dismiss " + trader.getName() + " from " + trader.getHisHerItsString() + " post.");
                        logger.log(Level.INFO, responder.getName() + " dismisses trader " + trader.getName() + " with Contract ID: " + question.getTarget());
                        trader.destroy();
                        contract.setData(-1, -1);
                    } else {
                        responder.getCommunicator().sendNormalServerMessage(trader.getName() + " is trading. Try later.");
                    }
                } else {
                    responder.getCommunicator().sendNormalServerMessage("An error occured on the server while dismissing the trader.");
                }
            } else {
                key = traderId + "local";
                val = props.getProperty(key);
                boolean useLocal = Boolean.parseBoolean(val);
                key = traderId + "pricemod";
                val = props.getProperty(key);
                float priceMod = shop.getPriceModifier();
                if (val != null) {
                    try {
                        priceMod = Float.parseFloat(val);
                        shop.setPriceModifier(priceMod);
                    }
                    catch (NumberFormatException f) {
                        responder.getCommunicator().sendSafeServerMessage("Failed to set price modifier to " + val + ". Make sure it is a decimal figure using '.'-notation.");
                    }
                }
                key = traderId + "manage";
                val = props.getProperty(key);
                boolean manageItems = Boolean.parseBoolean(val);
                shop.setUseLocalPrice(useLocal);
                if (manageItems) {
                    MultiPriceManageQuestion mpm = new MultiPriceManageQuestion(responder, "Price management.", "Set prices for items", traderId);
                    mpm.sendQuestion();
                }
            }
        } else {
            String tname = props.getProperty("ptradername");
            String gender = props.getProperty("gender");
            byte sex = 0;
            if (gender.equals("female")) {
                sex = 1;
            }
            if (tname != null && tname.length() > 0) {
                if (tname.length() < 3 || tname.length() > 20 || QuestionParser.containsIllegalCharacters(tname)) {
                    if (sex == 0) {
                        tname = QuestionParser.generateGuardMaleName();
                        responder.getCommunicator().sendSafeServerMessage("The name didn't fit the trader, so he chose another one.");
                    } else {
                        responder.getCommunicator().sendSafeServerMessage("The name didn't fit the trader, so she chose another one.");
                        tname = QuestionParser.generateGuardFemaleName();
                    }
                }
                tname = StringUtilities.raiseFirstLetter(tname);
                tname = "Merchant_" + tname;
                VolaTile tile = responder.getCurrentTile();
                if (tile != null) {
                    boolean stall = false;
                    Item[] items = tile.getItems();
                    for (int xx = 0; xx < items.length; ++xx) {
                        if (!items[xx].isMarketStall()) continue;
                        stall = true;
                        break;
                    }
                    if (!Methods.isActionAllowed(responder, (short)85)) {
                        return;
                    }
                    Structure struct = tile.getStructure();
                    if (stall || struct != null && struct.isFinished() || responder.getPower() > 1) {
                        Creature[] crets = tile.getCreatures();
                        boolean notok = false;
                        for (int x = 0; x < crets.length; ++x) {
                            if (crets[x].isPlayer()) continue;
                            notok = true;
                            break;
                        }
                        if (!notok) {
                            if (struct != null && !struct.isTypeBridge() && !struct.mayPlaceMerchants(responder)) {
                                responder.getCommunicator().sendNormalServerMessage("You do not have permission to place a trader in this building.");
                            } else {
                                try {
                                    trader = Creature.doNew(9, (float)(tile.getTileX() << 2) + 2.0f, (float)(tile.getTileY() << 2) + 2.0f, 180.0f, responder.getLayer(), tname, sex, responder.getKingdomId());
                                    if (responder.getFloorLevel(true) != 0) {
                                        trader.pushToFloorLevel(responder.getFloorLevel());
                                    }
                                    shop = Economy.getEconomy().createShop(trader.getWurmId(), responder.getWurmId());
                                    contract.setData(trader.getWurmId());
                                    logger.info(responder.getName() + " created a trader: " + trader);
                                }
                                catch (Exception ex) {
                                    responder.getCommunicator().sendAlertServerMessage("An error occured in the rifts of the void. The trader was not created.");
                                    logger.log(Level.WARNING, responder.getName() + " failed to create trader.", ex);
                                }
                            }
                        } else {
                            responder.getCommunicator().sendNormalServerMessage("The trader will only set up shop where no other creatures except you are standing.");
                        }
                    } else {
                        responder.getCommunicator().sendNormalServerMessage("The trader will only set up shop inside a finished building or by a market stall.");
                    }
                }
            }
        }
    }

    static void parseMultiPriceQuestion(MultiPriceManageQuestion question) {
        block18: {
            Creature responder = question.getResponder();
            long target = question.getTarget();
            try {
                Creature trader = Server.getInstance().getCreature(target);
                if (!trader.isNpcTrader()) break block18;
                Shop shop = Economy.getEconomy().getShop(trader);
                Properties props = question.getAnswer();
                if (shop == null) {
                    responder.getCommunicator().sendNormalServerMessage("No shop registered for that creature.");
                    break block18;
                }
                if (shop.getOwnerId() == responder.getWurmId()) {
                    Object[] items = trader.getInventory().getAllItems(false);
                    Arrays.sort(items);
                    String key = "";
                    String val = "";
                    int price = 0;
                    Map<Long, Integer> itemMap = question.getItemMap();
                    for (int x = 0; x < items.length; ++x) {
                        if (((Item)items[x]).isFullprice()) continue;
                        price = 0;
                        long id = ((Item)items[x]).getWurmId();
                        Integer bbid = itemMap.get(new Long(id));
                        int bid = bbid;
                        key = bid + "g";
                        val = props.getProperty(key);
                        if (val != null && val.length() > 0) {
                            try {
                                price = Integer.parseInt(val) * 1000000;
                            }
                            catch (NumberFormatException nfe) {
                                responder.getCommunicator().sendNormalServerMessage("Failed to set the gold price for " + ((Item)items[x]).getName() + ". Note that a coin value is in whole numbers, no decimals.");
                            }
                        }
                        if ((val = props.getProperty(key = bid + "s")) != null && val.length() > 0) {
                            try {
                                price += Integer.parseInt(val) * 10000;
                            }
                            catch (NumberFormatException nfe) {
                                responder.getCommunicator().sendNormalServerMessage("Failed to set a silver price for " + ((Item)items[x]).getName() + ". Note that a coin value is in whole numbers, no decimals.");
                            }
                        }
                        if ((val = props.getProperty(key = bid + "c")) != null && val.length() > 0) {
                            try {
                                price += Integer.parseInt(val) * 100;
                            }
                            catch (NumberFormatException nfe) {
                                responder.getCommunicator().sendNormalServerMessage("Failed to set a copper price for " + ((Item)items[x]).getName() + ". Note that a coin value is in whole numbers, no decimals.");
                            }
                        }
                        if ((val = props.getProperty(key = bid + "i")) != null && val.length() > 0) {
                            try {
                                price += Integer.parseInt(val);
                            }
                            catch (NumberFormatException nfe) {
                                responder.getCommunicator().sendNormalServerMessage("Failed to set an iron price for " + ((Item)items[x]).getName() + ". Note that a coin value is in whole numbers, no decimals.");
                            }
                        }
                        ((Item)items[x]).setPrice(price);
                    }
                    responder.getCommunicator().sendNormalServerMessage("The prices are updated.");
                    break block18;
                }
                responder.getCommunicator().sendNormalServerMessage("You don't own that shop.");
            }
            catch (NoSuchCreatureException nsc) {
                responder.getCommunicator().sendNormalServerMessage("No such creature.");
                logger.log(Level.WARNING, responder.getName(), nsc);
            }
            catch (NoSuchPlayerException nsp) {
                responder.getCommunicator().sendNormalServerMessage("No such creature.");
                logger.log(Level.WARNING, responder.getName(), nsp);
            }
        }
    }

    static void parseSinglePriceQuestion(SinglePriceManageQuestion question) {
        block33: {
            Creature responder = question.getResponder();
            Properties props = question.getAnswer();
            long target = question.getTarget();
            if (target == -10L) {
                NameCountList itemNames = new NameCountList();
                int price = 0;
                String val = props.getProperty("gold");
                if (val != null && val.length() > 0) {
                    try {
                        price = Integer.parseInt(val) * 1000000;
                    }
                    catch (NumberFormatException nfe) {
                        responder.getCommunicator().sendNormalServerMessage("Failed to set the gold price. Note that a coin value is in whole numbers, no decimals.");
                    }
                }
                if ((val = props.getProperty("silver")) != null && val.length() > 0) {
                    try {
                        price += Integer.parseInt(val) * 10000;
                    }
                    catch (NumberFormatException nfe) {
                        responder.getCommunicator().sendNormalServerMessage("Failed to set a silver price. Note that a coin value is in whole numbers, no decimals.");
                    }
                }
                if ((val = props.getProperty("copper")) != null && val.length() > 0) {
                    try {
                        price += Integer.parseInt(val) * 100;
                    }
                    catch (NumberFormatException nfe) {
                        responder.getCommunicator().sendNormalServerMessage("Failed to set a copper price. Note that a coin value is in whole numbers, no decimals.");
                    }
                }
                if ((val = props.getProperty("iron")) != null && val.length() > 0) {
                    try {
                        price += Integer.parseInt(val);
                    }
                    catch (NumberFormatException nfe) {
                        responder.getCommunicator().sendNormalServerMessage("Failed to set an iron price. Note that a coin value is in whole numbers, no decimals.");
                    }
                }
                for (Item item : question.getItems()) {
                    if (item.isFullprice()) {
                        responder.getCommunicator().sendNormalServerMessage("You cannot set the price of " + item.getName() + ".");
                        continue;
                    }
                    if (item.getOwnerId() != responder.getWurmId()) {
                        responder.getCommunicator().sendNormalServerMessage("You don't own " + item.getName() + ".");
                        continue;
                    }
                    item.setPrice(price);
                    itemNames.add(item.getName());
                }
                if (!itemNames.isEmpty()) {
                    responder.getCommunicator().sendNormalServerMessage("You set the price to " + Economy.getEconomy().getChangeFor(price).getChangeString() + " for " + itemNames.toString() + ".");
                }
            } else {
                try {
                    Item item = Items.getItem(target);
                    if (item.getOwnerId() == responder.getWurmId()) {
                        int price = 0;
                        long id = item.getWurmId();
                        String key = id + "gold";
                        String val = props.getProperty(key);
                        if (val != null && val.length() > 0) {
                            try {
                                price = Integer.parseInt(val) * 1000000;
                            }
                            catch (NumberFormatException nfe) {
                                responder.getCommunicator().sendNormalServerMessage("Failed to set the gold price for " + item.getName() + ". Note that a coin value is in whole numbers, no decimals.");
                            }
                        }
                        if ((val = props.getProperty(key = id + "silver")) != null && val.length() > 0) {
                            try {
                                price += Integer.parseInt(val) * 10000;
                            }
                            catch (NumberFormatException nfe) {
                                responder.getCommunicator().sendNormalServerMessage("Failed to set a silver price for " + item.getName() + ". Note that a coin value is in whole numbers, no decimals.");
                            }
                        }
                        if ((val = props.getProperty(key = id + "copper")) != null && val.length() > 0) {
                            try {
                                price += Integer.parseInt(val) * 100;
                            }
                            catch (NumberFormatException nfe) {
                                responder.getCommunicator().sendNormalServerMessage("Failed to set a copper price for " + item.getName() + ". Note that a coin value is in whole numbers, no decimals.");
                            }
                        }
                        if ((val = props.getProperty(key = id + "iron")) != null && val.length() > 0) {
                            try {
                                price += Integer.parseInt(val);
                            }
                            catch (NumberFormatException nfe) {
                                responder.getCommunicator().sendNormalServerMessage("Failed to set an iron price for " + item.getName() + ". Note that a coin value is in whole numbers, no decimals.");
                            }
                        }
                        item.setPrice(price);
                        responder.getCommunicator().sendNormalServerMessage("Set price to " + Economy.getEconomy().getChangeFor(price).getChangeString() + ".");
                        break block33;
                    }
                    responder.getCommunicator().sendNormalServerMessage("You don't own that item.");
                }
                catch (NoSuchItemException nsi) {
                    responder.getCommunicator().sendNormalServerMessage("No such item.");
                }
            }
        }
    }

    public static void parseTraderRentalQuestion(TraderRentalQuestion question) {
        block23: {
            Creature responder = question.getResponder();
            Properties props = question.getAnswer();
            if (!responder.isOnSurface()) {
                responder.getCommunicator().sendNormalServerMessage("The trader refuses to work in this cave.");
                return;
            }
            try {
                Item contract = Items.getItem(question.getTarget());
                if (contract.getOwner() != responder.getWurmId()) {
                    responder.getCommunicator().sendNormalServerMessage("You are no longer in possesion of the " + contract.getName() + "!");
                    return;
                }
            }
            catch (NoSuchItemException nsi) {
                responder.getCommunicator().sendNormalServerMessage("The contract no longer exists!");
                return;
            }
            catch (NotOwnedException no) {
                responder.getCommunicator().sendNormalServerMessage("You are no longer in possesion of the contract!");
                return;
            }
            String nname = props.getProperty("ntradername");
            try {
                Items.getItem(question.getTarget());
                String gender = props.getProperty("gender");
                byte sex = 0;
                if (gender.equals("female")) {
                    sex = 1;
                }
                if (nname == null || nname.length() <= 0) break block23;
                if (nname.length() < 3 || nname.length() > 20 || QuestionParser.containsIllegalCharacters(nname)) {
                    if (sex == 0) {
                        nname = QuestionParser.generateGuardMaleName();
                        responder.getCommunicator().sendSafeServerMessage("The name didn't fit the trader, so he chose another one.");
                    } else {
                        responder.getCommunicator().sendSafeServerMessage("The name didn't fit the trader, so she chose another one.");
                        nname = QuestionParser.generateGuardFemaleName();
                    }
                }
                nname = StringUtilities.raiseFirstLetter(nname);
                nname = "Trader_" + nname;
                VolaTile tile = responder.getCurrentTile();
                if (tile == null) break block23;
                Creature t = Economy.getEconomy().getTraderForZone(tile.getTileX(), tile.getTileY(), tile.isOnSurface());
                if (t == null) {
                    Structure struct = tile.getStructure();
                    if (struct != null && struct.isFinished() || responder.getPower() > 0) {
                        Creature[] crets = tile.getCreatures();
                        if (crets != null && crets.length == 1) {
                            int tax = 0;
                            Village v = tile.getVillage();
                            if (v != null) {
                                String taxs = props.getProperty("tax");
                                if (taxs == null || taxs.length() == 0) {
                                    responder.getCommunicator().sendAlertServerMessage("The tax you filled in is not appropriate. Make sure it is a number between 0 and 40.");
                                    return;
                                }
                                try {
                                    tax = Integer.parseInt(taxs);
                                    if (tax < 0 || tax > 40) {
                                        responder.getCommunicator().sendAlertServerMessage("The tax you filled in is not appropriate. Make sure it is a number between 0 and 40.");
                                        return;
                                    }
                                }
                                catch (NumberFormatException nfw) {
                                    responder.getCommunicator().sendAlertServerMessage("The tax you filled in is not appropriate. Make sure it is a whole number between 0 and 40.");
                                    return;
                                }
                            }
                            try {
                                Creature trader = Creature.doNew(9, (float)(tile.getTileX() << 2) + 2.0f, (float)(tile.getTileY() << 2) + 2.0f, 180.0f, responder.getLayer(), nname, sex, responder.getKingdomId());
                                Shop shop = Economy.getEconomy().createShop(trader.getWurmId());
                                if (tax > 0) {
                                    shop.setTax((float)tax / 100.0f);
                                }
                                Items.destroyItem(question.getTarget());
                                if (v != null) {
                                    v.addCitizen(trader, v.getRoleForStatus((byte)3));
                                }
                                break block23;
                            }
                            catch (Exception ex) {
                                responder.getCommunicator().sendAlertServerMessage("An error occured in the rifts of the void. The trader was not created.");
                                logger.log(Level.WARNING, responder.getName() + " failed to create trader.", ex);
                            }
                            break block23;
                        }
                        responder.getCommunicator().sendNormalServerMessage("The trader will only set up shop where no other creatures except you are standing.");
                        break block23;
                    }
                    responder.getCommunicator().sendNormalServerMessage("The trader will only set up shop inside a finished building.");
                    break block23;
                }
                responder.getCommunicator().sendNormalServerMessage("The new trader would be too close to the shop of " + t.getName() + ". He refuses to set up shop here.");
            }
            catch (NoSuchItemException nsi) {
                responder.getCommunicator().sendNormalServerMessage("Failed to locate the contract for that request.");
            }
        }
    }

    private static final void setReputation(Creature responder, int value, String name, long wurmId, boolean perma, Village village) {
        int oldvalue = village.getReputation(wurmId);
        boolean warning = false;
        if (!Servers.localServer.PVPSERVER && Servers.localServer.id != 3 && value > -30 && Players.getInstance().removeKosFor(wurmId)) {
            village.addHistory(responder.getName(), "pardons " + name + ".");
        }
        if (oldvalue != value) {
            if (oldvalue <= -30) {
                if (value > -30) {
                    village.addHistory(responder.getName(), "pardons " + name + ".");
                }
            } else if (value <= -30 && oldvalue > -30) {
                if (!Servers.localServer.PVPSERVER && Servers.localServer.id != 3 && value <= -30) {
                    warning = true;
                    if (Players.getInstance().addKosWarning(new KosWarning(wurmId, value, village, perma))) {
                        responder.getCommunicator().sendNormalServerMessage(name + " will receive a warning to leave the settlement. 3 minutes later the reputation will take the effect and he will be attacked by the guards.");
                        village.addHistory(responder.getName(), "adds " + name + " to the KOS warning list.");
                    } else {
                        responder.getCommunicator().sendNormalServerMessage(name + " is already put up on the kos list, pending activation.");
                    }
                } else {
                    village.addHistory(responder.getName(), "declares " + name + " to be a criminal.");
                }
            }
            if (!warning) {
                Reputation r = village.setReputation(wurmId, value, false, true);
                if (r != null) {
                    r.setPermanent(perma);
                } else {
                    responder.getCommunicator().sendNormalServerMessage("The reputation for " + name + " was deleted.");
                }
            }
        }
    }

    public static void parseReputationQuestion(ReputationQuestion question) {
        Creature responder = question.getResponder();
        long target = question.getTarget();
        try {
            Village village;
            if (target == -10L) {
                village = responder.getCitizenVillage();
            } else {
                Item deed = Items.getItem(target);
                int villageId = deed.getData2();
                village = Villages.getVillage(villageId);
            }
            long touched = -10L;
            if (village == null) {
                responder.getCommunicator().sendNormalServerMessage("No village found.");
            } else {
                Map<Long, Integer> itemMap;
                int value;
                String val;
                String key;
                Reputation[] reputations;
                Properties props;
                block31: {
                    props = question.getAnswer();
                    reputations = village.getReputations();
                    key = "";
                    val = "";
                    value = 0;
                    itemMap = question.getItemMap();
                    key = "nn";
                    val = props.getProperty(key);
                    if (val != null && val.length() > 0) {
                        try {
                            Player player = Players.getInstance().getPlayer(StringUtilities.raiseFirstLetter(val));
                            key = "nr";
                            val = props.getProperty(key);
                            if (val == null || val.length() <= 0) break block31;
                            try {
                                value = Integer.parseInt(val);
                                if (value < 0 && player.getPower() == 0 || value >= 0) {
                                    boolean perma = false;
                                    key = "np";
                                    val = props.getProperty(key);
                                    if (Boolean.parseBoolean(val)) {
                                        perma = true;
                                    }
                                    touched = player.getWurmId();
                                    QuestionParser.setReputation(responder, value, player.getName(), player.getWurmId(), perma, village);
                                    break block31;
                                }
                                responder.getCommunicator().sendNormalServerMessage("You cannot modify the reputation for " + player.getName() + " below 0, since " + player.getHeSheItString() + " is a GM.");
                            }
                            catch (NumberFormatException nfe) {
                                responder.getCommunicator().sendNormalServerMessage("Failed to set the reputation for " + player.getName() + ". Bad value.");
                            }
                        }
                        catch (NoSuchPlayerException nsp) {
                            String name = val;
                            PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(name);
                            if (pinf != null && pinf.wurmId > 0L) {
                                key = "nr";
                                val = props.getProperty(key);
                                if (val == null || val.length() <= 0) break block31;
                                try {
                                    value = Integer.parseInt(val);
                                    if (value < 0 && pinf.getPower() == 0 || value > 0) {
                                        boolean perma = false;
                                        key = "np";
                                        val = props.getProperty(key);
                                        if (Boolean.parseBoolean(val)) {
                                            perma = true;
                                        }
                                        touched = pinf.wurmId;
                                        QuestionParser.setReputation(responder, value, pinf.getName(), pinf.wurmId, perma, village);
                                    }
                                    responder.getCommunicator().sendNormalServerMessage("Make sure " + name + " is a regular player.");
                                }
                                catch (NumberFormatException nfe) {
                                    responder.getCommunicator().sendNormalServerMessage("Failed to set the reputation for " + name + ". Bad value.");
                                }
                            }
                            responder.getCommunicator().sendNormalServerMessage("Failed to locate player with name " + name + ". Make sure he/she has an account.");
                        }
                    }
                }
                for (int x = 0; x < reputations.length; ++x) {
                    value = 0;
                    long id = reputations[x].getWurmId();
                    Integer bbid = itemMap.get(new Long(id));
                    if (bbid == null) continue;
                    try {
                        Player player = Players.getInstance().getPlayer(id);
                        key = bbid + "r";
                        val = props.getProperty(key);
                        if (val == null || val.length() <= 0) continue;
                        try {
                            value = Integer.parseInt(val);
                            if (player.getPower() == 0 || value >= 0 && player.getPower() > 0) {
                                boolean perma = false;
                                key = bbid + "p";
                                val = props.getProperty(key);
                                if (Boolean.parseBoolean(val)) {
                                    perma = true;
                                }
                                if (player.getWurmId() == touched) continue;
                                QuestionParser.setReputation(responder, value, player.getName(), player.getWurmId(), perma, village);
                                continue;
                            }
                            responder.getCommunicator().sendNormalServerMessage("You cannot modify the reputation for " + player.getName() + " below 0, since " + player.getHeSheItString() + " is a GM.");
                        }
                        catch (NumberFormatException nfe) {
                            responder.getCommunicator().sendNormalServerMessage("Failed to set the reputation for " + player.getName() + ". Bad value.");
                        }
                        continue;
                    }
                    catch (NoSuchPlayerException nsp) {
                        key = bbid + "r";
                        val = props.getProperty(key);
                        if (val == null || val.length() <= 0) continue;
                        try {
                            String bname = Players.getInstance().getNameFor(id);
                            PlayerInfo pinf = PlayerInfoFactory.createPlayerInfo(bname);
                            pinf.load();
                            value = Integer.parseInt(val);
                            if (pinf.getPower() != 0 && (value <= 0 || pinf.getPower() <= 0)) continue;
                            boolean perma = false;
                            key = bbid + "p";
                            val = props.getProperty(key);
                            if (Boolean.parseBoolean(val)) {
                                perma = true;
                            }
                            if (pinf.wurmId == touched) continue;
                            QuestionParser.setReputation(responder, value, pinf.getName(), pinf.wurmId, perma, village);
                            continue;
                        }
                        catch (NumberFormatException nfe) {
                            responder.getCommunicator().sendNormalServerMessage("Failed to set the reputation for a player. Bad value.");
                            continue;
                        }
                        catch (IOException iox) {
                            logger.log(Level.WARNING, iox.getMessage(), iox);
                            continue;
                        }
                        catch (NoSuchPlayerException nsp2) {
                            logger.log(Level.WARNING, nsp2.getMessage(), nsp2);
                        }
                    }
                }
            }
            responder.getCommunicator().sendNormalServerMessage("The reputations are updated.");
        }
        catch (NoSuchItemException nsi) {
            responder.getCommunicator().sendNormalServerMessage("No such item.");
            logger.log(Level.WARNING, responder.getName(), nsi);
        }
        catch (NoSuchVillageException nsp) {
            responder.getCommunicator().sendNormalServerMessage("No such village.");
            logger.log(Level.WARNING, responder.getName(), nsp);
        }
    }

    static void parseSetDeityQuestion(SetDeityQuestion question) {
        block23: {
            Properties props = question.getAnswer();
            int type = question.getType();
            Creature responder = question.getResponder();
            long target = question.getTarget();
            if (type == 0) {
                logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
                return;
            }
            if (type == 26) {
                String wid = props.getProperty("wurmid");
                String did = props.getProperty("deityid");
                String fai = props.getProperty("faith");
                String dec = props.getProperty("faithdec");
                String fav = props.getProperty("favor");
                try {
                    Item targ = Items.getItem(target);
                    if (targ.getTemplateId() == 176 && WurmPermissions.maySetFaith(responder)) {
                        int listid = Integer.parseInt(wid);
                        int deitynum = Integer.parseInt(did);
                        float faith = (float)Double.parseDouble(fai + "." + dec);
                        int favor = Integer.parseInt(fav);
                        Player player = question.getPlayer(listid);
                        if (player == null) {
                            responder.getCommunicator().sendNormalServerMessage("No such player!");
                            return;
                        }
                        int deityid = question.getDeityNumberFromArrayPos(deitynum);
                        Deity deity = Deities.getDeity(deityid);
                        if (deity == null) {
                            responder.getCommunicator().sendNormalServerMessage("No such deity!");
                            try {
                                player.setDeity(deity);
                                player.setPriest(false);
                            }
                            catch (IOException iox) {
                                responder.getCommunicator().sendNormalServerMessage("Failed to clear deity! " + iox.getMessage());
                                logger.log(Level.WARNING, responder.getName() + " failed to clear deity " + iox.getMessage(), iox);
                            }
                            break block23;
                        }
                        try {
                            player.setDeity(deity);
                            player.setPriest(faith > 30.0f);
                            if (faith > 30.0f) {
                                PlayerJournal.sendTierUnlock(player, PlayerJournal.getAllTiers().get((byte)10));
                            }
                            player.setFaith(faith);
                            player.setFavor(favor);
                            responder.getCommunicator().sendNormalServerMessage(player.getName() + " now has deity " + deity.name + ", faith " + faith + ", and favour: " + favor + ".");
                            player.getCommunicator().sendNormalServerMessage("You are now a follower of " + deity.name + ".", (byte)2);
                            responder.getLogger().info(player.getName() + " now has deity " + deity.name + ", faith " + faith + ", and favour: " + favor + ".");
                            if (deity.number == 4) {
                                if (Servers.isThisAPvpServer()) {
                                    player.setKingdomId((byte)3);
                                    responder.getCommunicator().sendNormalServerMessage(player.getName() + " now is with the " + "Horde of the Summoned" + ".");
                                    player.getCommunicator().sendNormalServerMessage("You are now with the Horde of the Summoned.");
                                }
                                player.setAlignment(Math.min(-50.0f, player.getAlignment()));
                            } else if (player.getAlignment() < 0.0f) {
                                if (player.getKingdomId() == 3) {
                                    if (player.getCurrentTile().getKingdom() != 0) {
                                        player.setKingdomId(player.getCurrentTile().getKingdom());
                                    } else if (responder.getKingdomId() != 3) {
                                        player.setKingdomId(responder.getKingdomId());
                                    } else {
                                        player.setKingdomId((byte)4);
                                    }
                                }
                                player.setAlignment(50.0f);
                            }
                            if (player.isChampion()) {
                                Server.getInstance().broadCastAlert(player.getName() + " now is a Champion of " + deity.name + ".", true, (byte)2);
                                responder.getLogger().log(Level.WARNING, responder.getName() + " set the deity of real death player " + player.getName() + " to " + deity.name + ".");
                            }
                            break block23;
                        }
                        catch (IOException iox) {
                            responder.getCommunicator().sendNormalServerMessage("Failed to set deity! " + iox.getMessage());
                            logger.log(Level.WARNING, responder.getName() + " failed to set deity " + iox.getMessage(), iox);
                        }
                        break block23;
                    }
                    logger.log(Level.WARNING, responder.getName() + " item used to answer is not a wand! " + wid + ", " + did + "," + fai);
                }
                catch (NumberFormatException nf) {
                    logger.log(Level.WARNING, responder.getName() + ":" + nf.getMessage() + ": " + wid + ", " + did + "," + fai + "," + fav);
                    responder.getCommunicator().sendNormalServerMessage("The values " + wid + ", " + did + "," + fai + "," + fav + " are improper.");
                }
                catch (NoSuchItemException nsi) {
                    logger.log(Level.WARNING, responder.getName() + " tried to use wand but it didn't exist! " + wid + ", " + did + "," + fai);
                }
            }
        }
    }

    static void parseSetKingdomQuestion(SetKingdomQuestion question) {
        block20: {
            Properties props = question.getAnswer();
            int type = question.getType();
            Creature responder = question.getResponder();
            long target = question.getTarget();
            if (type == 0) {
                logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
                return;
            }
            if (type == 37) {
                if (question.getResponder().getPower() <= 0) {
                    String key = "kingd";
                    String val = question.getAnswer().getProperty("kingd");
                    if (Boolean.parseBoolean(val)) {
                        try {
                            byte previousKingdom = responder.getKingdomId();
                            byte targetKingdom = responder.getKingdomTemplateId();
                            if (Servers.isThisAChaosServer() && targetKingdom != 3) {
                                targetKingdom = 4;
                            }
                            if (responder.setKingdomId(targetKingdom, false, false)) {
                                logger.info(responder.getName() + " has just decided to leave " + Kingdoms.getNameFor(previousKingdom) + " and joins " + Kingdoms.getNameFor(targetKingdom));
                                responder.getCommunicator().sendNormalServerMessage("You decide to leave " + Kingdoms.getNameFor(previousKingdom) + " and join " + Kingdoms.getNameFor(targetKingdom) + ". Congratulations!");
                                Server.getInstance().broadCastAction(responder.getName() + " leaves " + Kingdoms.getNameFor(previousKingdom) + "!", responder, 5);
                            }
                        }
                        catch (IOException iox) {
                            logger.log(Level.WARNING, responder.getName() + ":" + iox.getMessage() + ": " + question.getResponder().getName(), iox);
                            responder.getCommunicator().sendNormalServerMessage("The moons are not properly aligned right now. You will have to wait (there is a server error).");
                        }
                    } else {
                        responder.getCommunicator().sendNormalServerMessage("You decide not to leave " + Kingdoms.getNameFor(question.getResponder().getKingdomId()) + " for now.");
                    }
                } else {
                    String wid = props.getProperty("wurmid");
                    String did = props.getProperty("kingdomid");
                    try {
                        int listid = Integer.parseInt(wid);
                        int index = Integer.parseInt(did);
                        Item targ = Items.getItem(target);
                        if ((targ.getTemplateId() == 176 || targ.getTemplateId() == 315) && responder.getPower() >= 2) {
                            Kingdom k = question.getAvailKingdoms().get(index - 1);
                            byte kingdomid = k == null ? (byte)0 : k.getId();
                            Player player = question.getPlayer(listid);
                            if (player == null) {
                                responder.getCommunicator().sendNormalServerMessage("No such player!");
                                return;
                            }
                            String kname = Kingdoms.getNameFor(kingdomid);
                            if (kname.equals("no known kingdom")) {
                                responder.getCommunicator().sendNormalServerMessage("Not setting to no kingdom at the moment!");
                                break block20;
                            }
                            if (player.isChampion()) {
                                responder.getCommunicator().sendNormalServerMessage(player.getName() + " has real death and may not change kingdom.");
                                return;
                            }
                            try {
                                if (player.setKingdomId(kingdomid)) {
                                    responder.getCommunicator().sendNormalServerMessage(player.getName() + " now is part of " + kname + ".");
                                    player.getCommunicator().sendNormalServerMessage("You are now a part of " + kname + ".");
                                    responder.getLogger().log(Level.INFO, "Set kingdom of " + player.getName() + " to " + kname + ".");
                                    player.getCommunicator().sendUpdateKingdomId();
                                    break block20;
                                }
                                responder.getLogger().log(Level.INFO, "Tried to set kingdom of " + player.getName() + " to " + kname + " but it was not allowed.");
                            }
                            catch (IOException iox) {
                                responder.getCommunicator().sendNormalServerMessage("Failed to set kingdom! " + iox.getMessage());
                                logger.log(Level.WARNING, responder.getName() + "failed to set kingdom " + iox.getMessage(), iox);
                            }
                            break block20;
                        }
                        logger.log(Level.WARNING, responder.getName() + " item used to answer is not a wand! " + wid + ", " + did);
                    }
                    catch (NumberFormatException nf) {
                        logger.log(Level.WARNING, responder.getName() + ":" + nf.getMessage() + ": " + wid + ", " + did);
                        responder.getCommunicator().sendNormalServerMessage("The values " + wid + ", " + did + " are improper.");
                    }
                    catch (NoSuchItemException nsi) {
                        logger.log(Level.WARNING, responder.getName() + " tried to use wand but it didn't exist! " + wid + ", " + did);
                    }
                }
            }
        }
    }

    static void parseAskKingdomQuestion(AskKingdomQuestion question) {
        block12: {
            Creature responder = question.getResponder();
            long target = question.target;
            try {
                Creature asker = Server.getInstance().getCreature(target);
                if (asker.getKingdomId() != responder.getKingdomId()) {
                    if (responder instanceof Player) {
                        if (responder.isWithinTileDistanceTo(asker.getTileX(), asker.getTileY(), (int)(asker.getPositionZ() + asker.getAltOffZ()) >> 2, 4)) {
                            String key = "conv";
                            String val = question.getAnswer().getProperty("conv");
                            if (Boolean.parseBoolean(val)) {
                                boolean forceToCustom = false;
                                if (!Servers.localServer.HOMESERVER && Kingdoms.getKingdom(asker.getKingdomId()).isCustomKingdom() && responder.getCitizenVillage() != null && responder.getCitizenVillage().getMayor().wurmId == responder.getWurmId()) {
                                    forceToCustom = true;
                                }
                                if (responder.mayChangeKingdom(null) || forceToCustom) {
                                    try {
                                        if (!responder.setKingdomId(asker.getKingdomId(), forceToCustom)) break block12;
                                        responder.getCommunicator().sendNormalServerMessage("You have now joined " + Kingdoms.getNameFor(responder.getKingdomId()) + "!");
                                        asker.getCommunicator().sendNormalServerMessage(responder.getName() + " has now joined " + Kingdoms.getNameFor(responder.getKingdomId()) + "!");
                                        if (!Kingdoms.getKingdom(asker.getKingdomId()).isCustomKingdom()) break block12;
                                        String toSend = "<" + asker.getName() + "> convinced " + responder.getName() + " to join " + Kingdoms.getNameFor(responder.getKingdomId()) + ".";
                                        Message mess = new Message(asker, 10, "GL-" + Kingdoms.getChatNameFor(asker.getKingdomId()), toSend);
                                        Server.getInstance().addMessage(mess);
                                        WcKingdomChat wc = new WcKingdomChat(WurmId.getNextWCCommandId(), asker.getWurmId(), asker.getName(), toSend, false, asker.getKingdomId(), -1, -1, -1);
                                        if (Servers.localServer.LOGINSERVER) {
                                            wc.sendFromLoginServer();
                                            break block12;
                                        }
                                        wc.sendToLoginServer();
                                    }
                                    catch (IOException iox) {
                                        logger.log(Level.WARNING, responder.getName() + ": " + iox.getMessage(), iox);
                                    }
                                    break block12;
                                }
                                responder.getCommunicator().sendNormalServerMessage("You may not change kingdom too frequently. Also, mayors of a settlement may not change kingdom. You may not join a custom kingdom on a home server.");
                                asker.getCommunicator().sendNormalServerMessage(responder.getName() + " may not change kingdom right now.");
                                break block12;
                            }
                            responder.getCommunicator().sendNormalServerMessage("You decide not to join " + Kingdoms.getNameFor(asker.getKingdomId()) + ".");
                            asker.getCommunicator().sendNormalServerMessage(responder.getName() + " decides not to join " + Kingdoms.getNameFor(asker.getKingdomId()) + ".");
                            break block12;
                        }
                        asker.getCommunicator().sendNormalServerMessage(responder.getName() + " is too far away now.");
                        break block12;
                    }
                    asker.getCommunicator().sendNormalServerMessage("Only players may change kingdom.");
                    break block12;
                }
                asker.getCommunicator().sendNormalServerMessage(responder.getName() + " is already in " + Kingdoms.getNameFor(responder.getKingdomId()) + ".");
            }
            catch (NoSuchCreatureException nsc) {
                responder.getCommunicator().sendNormalServerMessage("The asker is not around any longer.");
            }
            catch (NoSuchPlayerException nsp) {
                responder.getCommunicator().sendNormalServerMessage("The asker is not around any longer.");
            }
        }
    }

    static void parseAskConvertQuestion(AskConvertQuestion question) {
        Creature responder = question.getResponder();
        Item holyItem = question.getHolyItem();
        long target = question.getTarget();
        try {
            Creature asker = Server.getInstance().getCreature(target);
            Deity deity = asker.getDeity();
            if (deity == null) {
                asker.getCommunicator().sendNormalServerMessage("You have no deity.");
                return;
            }
            if (!responder.isPlayer()) {
                asker.getCommunicator().sendNormalServerMessage("You may only convert other players.");
                return;
            }
            if (!responder.isWithinTileDistanceTo(asker.getTileX(), asker.getTileY(), (int)(asker.getPositionZ() + asker.getAltOffZ()) >> 2, 4)) {
                asker.getCommunicator().sendNormalServerMessage(responder.getName() + " is too far away now.");
                return;
            }
            String key = "conv";
            String val = question.getAnswer().getProperty("conv");
            if (!Boolean.parseBoolean(val)) {
                responder.getCommunicator().sendNormalServerMessage("You decide not to listen.");
                asker.getCommunicator().sendNormalServerMessage(responder.getName() + " decides not to listen.");
                return;
            }
            try {
                asker.getCurrentAction();
                responder.getCommunicator().sendNormalServerMessage(asker.getName() + " is too busy to preach right now.");
                asker.getCommunicator().sendNormalServerMessage(responder.getName() + " wants to listen to your preachings but you are too busy.");
                return;
            }
            catch (NoSuchActionException nsa) {
                try {
                    BehaviourDispatcher.action(asker, asker.getCommunicator(), holyItem.getWurmId(), responder.getWurmId(), (short)216);
                }
                catch (FailedException failedException) {
                }
                catch (NoSuchItemException | NoSuchPlayerException | NoSuchBehaviourException | NoSuchCreatureException | NoSuchWallException nsb) {
                    logger.log(Level.WARNING, nsb.getMessage(), nsb);
                }
            }
        }
        catch (NoSuchPlayerException | NoSuchCreatureException nsc) {
            responder.getCommunicator().sendNormalServerMessage("The preacher is not around any longer.");
        }
    }

    public static void parseConvertQuestion(ConvertQuestion question) {
        Creature responder = question.getResponder();
        Item holyItem = question.getHolyItem();
        long target = question.getTarget();
        try {
            Creature asker = Server.getInstance().getCreature(target);
            Deity deity = asker.getDeity();
            if (deity == null) {
                asker.getCommunicator().sendNormalServerMessage("You have no deity.");
                return;
            }
            if (!responder.isWithinTileDistanceTo(asker.getTileX(), asker.getTileY(), (int)(asker.getPositionZ() + asker.getAltOffZ()) >> 2, 4)) {
                asker.getCommunicator().sendNormalServerMessage(responder.getName() + " is too far away now.");
                return;
            }
            String key = "conv";
            String val = question.getAnswer().getProperty("conv");
            if (!Boolean.parseBoolean(val)) {
                responder.getCommunicator().sendNormalServerMessage("You decide not to convert.");
                asker.getCommunicator().sendNormalServerMessage(responder.getName() + " decides not to convert.");
                return;
            }
            if (QuestionParser.canConvertToDeity(responder, deity)) {
                if (!QuestionParser.doesKingdomTemplateAcceptDeity(responder.getKingdomTemplateId(), deity)) {
                    responder.getCommunicator().sendNormalServerMessage("Following that deity would expel you from " + responder.getKingdomName() + ".");
                    return;
                }
                try {
                    Skill preaching = asker.getSkills().getSkillOrLearn(10065);
                    preaching.skillCheck(preaching.getKnowledge(0.0) - 10.0, holyItem, asker.zoneBonus, false, question.getSkillcounter());
                    responder.setChangedDeity();
                    responder.setDeity(deity);
                    responder.setFaith((float)preaching.getKnowledge(0.0) / 5.0f);
                    Deity templateDeity = Deities.getDeity(deity.getTemplateDeity());
                    templateDeity.increaseFavor();
                    if (deity.isHateGod()) {
                        asker.maybeModifyAlignment(-1.0f);
                        responder.setAlignment(Math.min(-1.0f, responder.getAlignment()));
                    } else {
                        asker.maybeModifyAlignment(1.0f);
                        responder.setAlignment(Math.max(1.0f, responder.getAlignment()));
                    }
                    asker.setFavor(Math.max(asker.getFavor(), asker.getFaith() / 2.0f));
                    responder.setFavor(1.0f);
                    responder.getCommunicator().sendNormalServerMessage("You have now converted to " + deity.name + "!");
                    asker.getCommunicator().sendNormalServerMessage(responder.getName() + " has now converted to " + deity.name + "!");
                    asker.achievement(621);
                }
                catch (IOException iox) {
                    responder.getCommunicator().sendNormalServerMessage("You failed to convert to " + deity.name + " due to a server error! Please report this to the GM's.");
                    asker.getCommunicator().sendNormalServerMessage(responder.getName() + " failed to convert to " + deity.name + " due to a server error! Please report this to the GM's.");
                    logger.log(Level.WARNING, responder.getName() + ":" + iox.getMessage(), iox);
                }
            }
        }
        catch (NoSuchPlayerException | NoSuchCreatureException nsc) {
            responder.getCommunicator().sendNormalServerMessage("The preacher is not around any longer.");
        }
    }

    public static final boolean canConvertToDeity(Creature responder, Deity deity) {
        if (responder == null || !responder.isPlayer()) {
            return false;
        }
        if (deity == null) {
            responder.getCommunicator().sendNormalServerMessage("That deity is not available to convert to.");
            return false;
        }
        if (!responder.mayChangeDeity(deity.getNumber())) {
            responder.getCommunicator().sendNormalServerMessage("Your faith cannot change so frequently. You will have to wait.");
            return false;
        }
        if (responder.getDeity() == deity) {
            responder.getCommunicator().sendNormalServerMessage("You already follow the teachings of " + deity.getName() + ".");
            return false;
        }
        return true;
    }

    public static final boolean doesKingdomTemplateAcceptDeity(byte kingdomTemplate, Deity deity) {
        if (kingdomTemplate == 3) {
            if (deity.isFo() || deity.isMagranon() || deity.isVynora()) {
                return false;
            }
        } else if (deity.isLibila()) {
            return kingdomTemplate == 4 && !Servers.localServer.PVPSERVER;
        }
        return true;
    }

    public static final void parseAltarConvertQuestion(AltarConversionQuestion question) {
        Creature responder = question.getResponder();
        Deity deity = question.getDeity();
        long target = question.getTarget();
        try {
            Item altar = Items.getItem(target);
            if (deity == null) {
                responder.getCommunicator().sendNormalServerMessage("The altar has no deity anymore.");
                return;
            }
            if (!responder.isWithinTileDistanceTo(altar.getTileX(), altar.getTileY(), (int)altar.getPosZ() >> 2, 4)) {
                responder.getCommunicator().sendNormalServerMessage("The " + altar.getName() + " is too far away now.");
                return;
            }
            String key = "conv";
            String val = question.getAnswer().getProperty(key);
            if (!Boolean.parseBoolean(val)) {
                responder.getCommunicator().sendNormalServerMessage("You decide not to convert.");
                return;
            }
            if (QuestionParser.canConvertToDeity(responder, deity)) {
                try {
                    if (!QuestionParser.doesKingdomTemplateAcceptDeity(responder.getKingdomTemplateId(), deity)) {
                        responder.getCommunicator().sendNormalServerMessage("Following that deity would expel you from " + responder.getKingdomName() + ".");
                        return;
                    }
                    responder.setChangedDeity();
                    responder.setDeity(deity);
                    responder.setFaith(1.0f);
                    responder.setFavor(1.0f);
                    if (deity.isHateGod()) {
                        responder.setAlignment(Math.min(-1.0f, responder.getAlignment()));
                    } else {
                        responder.setAlignment(Math.max(1.0f, responder.getAlignment()));
                    }
                    responder.getCommunicator().sendNormalServerMessage("You have now converted to " + deity.name + "!");
                }
                catch (IOException iox) {
                    responder.getCommunicator().sendNormalServerMessage("You failed to convert to " + deity.name + " due to a server error! Please report this to the GM's.");
                    logger.log(Level.WARNING, responder.getName() + ":" + iox.getMessage(), iox);
                }
            }
        }
        catch (NoSuchItemException nsc) {
            responder.getCommunicator().sendNormalServerMessage("The altar is not around any longer.");
        }
    }

    public static final void parseAscensionQuestion(AscensionQuestion question) {
        Creature responder = question.getResponder();
        if (responder.isPlayer()) {
            String key = "demig";
            String val = question.getAnswer().getProperty("demig");
            if (!Boolean.parseBoolean(val)) {
                responder.getCommunicator().sendNormalServerMessage("You decide to remain mortal for now. Maybe the chance returns, who knows?");
                logger.log(Level.INFO, responder.getName() + " declined ascension to demigod!");
            }
        }
    }

    public static void parsePriestQuestion(PriestQuestion question) {
        Deity deity;
        Creature responder;
        block20: {
            responder = question.getResponder();
            long target = question.getTarget();
            deity = responder.getDeity();
            if (WurmId.getType(target) == 2) {
                try {
                    Item altar = Items.getItem(target);
                    if (altar.isHugeAltar()) {
                        if (!responder.isWithinTileDistanceTo((int)altar.getPosX() >> 2, (int)altar.getPosY() >> 2, (int)altar.getPosZ() >> 2, 4)) {
                            responder.getCommunicator().sendNormalServerMessage("You must be close to the huge altar in order to become a priest.");
                            return;
                        }
                        break block20;
                    }
                    responder.getCommunicator().sendNormalServerMessage("You must be close to the huge altar in order to become a priest.");
                    return;
                }
                catch (NoSuchItemException nsi) {
                    responder.getCommunicator().sendNormalServerMessage("You can not become a priest right now.");
                    return;
                }
            }
            if (deity == null) {
                responder.getCommunicator().sendNormalServerMessage("You are not even a follower of a faith!");
                return;
            }
            try {
                Creature creature = Server.getInstance().getCreature(question.target);
                if (!responder.isWithinTileDistanceTo((int)creature.getPosX() >> 2, (int)creature.getPosY() >> 2, (int)(creature.getPositionZ() + creature.getAltOffZ()) >> 2, 4)) {
                    responder.getCommunicator().sendNormalServerMessage("You must be closer to the person who asked you.");
                    return;
                }
                if (deity != creature.getDeity()) {
                    responder.getCommunicator().sendNormalServerMessage("You must be of the same faith as " + creature.getName() + ".");
                    return;
                }
            }
            catch (NoSuchCreatureException nsc) {
                responder.getCommunicator().sendNormalServerMessage("You must be close to the person who asked you.");
                return;
            }
            catch (NoSuchPlayerException nsp) {
                responder.getCommunicator().sendNormalServerMessage("You must be close to the person who asked you.");
                return;
            }
        }
        if (deity != null) {
            if (!responder.isPriest()) {
                if (responder.isPlayer()) {
                    String key = "priest";
                    String val = question.getAnswer().getProperty("priest");
                    if (Boolean.parseBoolean(val)) {
                        logger.info(responder.getName() + " has just become a priest of " + deity.name);
                        responder.getCommunicator().sendNormalServerMessage("You have become a priest of " + deity.name + ". Congratulations!");
                        Server.getInstance().broadCastAction(responder.getName() + " is now a priest of " + deity.name + "!", responder, 5);
                        responder.setPriest(true);
                        PlayerJournal.sendTierUnlock((Player)responder, PlayerJournal.getAllTiers().get((byte)10));
                        try {
                            responder.setFavor(responder.getFaith());
                        }
                        catch (IOException iox) {
                            logger.log(Level.WARNING, iox.getMessage(), iox);
                        }
                    } else {
                        responder.getCommunicator().sendNormalServerMessage("You decide not to become a priest for now.");
                    }
                }
            } else {
                responder.getCommunicator().sendNormalServerMessage("You are already a priest.");
            }
        } else {
            responder.getCommunicator().sendNormalServerMessage("You have no deity!");
        }
    }

    public static final void parseRealDeathQuestion(RealDeathQuestion question) {
        block19: {
            Creature responder = question.getResponder();
            Deity deity = responder.getDeity();
            long target = question.getTarget();
            if (!responder.isChampion()) {
                try {
                    Item altar = Items.getItem(target);
                    if (altar.isHugeAltar()) {
                        if (deity != null) {
                            if (deity.accepts(responder.getAlignment())) {
                                if (responder instanceof Player) {
                                    if (Players.getChampionsFromKingdom(responder.getKingdomId(), deity.getNumber()) < 1) {
                                        if (Players.getChampionsFromKingdom(responder.getKingdomId()) < 3) {
                                            if (responder.isWithinTileDistanceTo((int)altar.getPosX() >> 2, (int)altar.getPosY() >> 2, (int)altar.getPosZ() >> 2, 4)) {
                                                String key = "rd";
                                                String val = question.getAnswer().getProperty("rd");
                                                if (Boolean.parseBoolean(val)) {
                                                    responder.becomeChamp();
                                                } else {
                                                    responder.getCommunicator().sendNormalServerMessage("You decide not to become a champion of " + deity.name + ".");
                                                }
                                            } else {
                                                responder.getCommunicator().sendNormalServerMessage(altar.getName() + " is too far away now.");
                                            }
                                        } else {
                                            responder.getCommunicator().sendNormalServerMessage("Your kingdom does not support more champions right now.");
                                        }
                                    } else {
                                        responder.getCommunicator().sendNormalServerMessage(deity.name + " can not support another champion from your kingdom right now.");
                                    }
                                } else {
                                    responder.getCommunicator().sendNormalServerMessage("Only players may become champions.");
                                }
                            } else {
                                responder.getCommunicator().sendNormalServerMessage(deity.name + " would not accept you as " + deity.getHisHerItsString() + " champion right now since you have strayn from the path.");
                            }
                        } else {
                            responder.getCommunicator().sendNormalServerMessage("You no longer follow a deity.");
                        }
                        break block19;
                    }
                    responder.getCommunicator().sendNormalServerMessage("The altar is not of the right type.");
                }
                catch (NoSuchItemException nsc) {
                    responder.getCommunicator().sendNormalServerMessage("The altar is not around any longer.");
                }
            } else {
                responder.getCommunicator().sendNormalServerMessage("You are already a champion of " + deity.name + ".");
            }
        }
    }

    public static final void parseSpawnQuestion(SpawnQuestion question) {
        String spq;
        String sps;
        boolean eserv = false;
        if (Servers.localServer.KINGDOM != question.getResponder().getKingdomId() && (sps = question.getAnswer().getProperty("eserver")) != null) {
            Integer serverNumber;
            ServerEntry toGoTo;
            eserv = true;
            Map<Integer, Integer> servers = question.getServerEntries();
            int i = Integer.parseInt(sps);
            if (i > 0 && ((toGoTo = Servers.getServerWithId(serverNumber = servers.get(i))).getKingdom() == question.getResponder().getKingdomId() || toGoTo.getKingdom() == 0)) {
                try {
                    Player player = Players.getInstance().getPlayer(question.getResponder().getWurmId());
                    player.sendTransfer(Server.getInstance(), toGoTo.EXTERNALIP, Integer.parseInt(toGoTo.EXTERNALPORT), toGoTo.INTRASERVERPASSWORD, toGoTo.getId(), -1, -1, true, false, player.getKingdomId());
                    return;
                }
                catch (NoSuchPlayerException nsp) {
                    logger.log(Level.INFO, "Player " + question.getResponder().getWurmId() + " is no longer available.");
                }
            }
        }
        if ((spq = question.getAnswer().getProperty("spawnpoint")) != null) {
            int i = Integer.parseInt(spq);
            Spawnpoint sp = question.getSpawnpoint(i);
            if (sp == null) {
                return;
            }
            try {
                question.getResponder().spawnArmour = question.getAnswer().getProperty("armour");
                question.getResponder().spawnWeapon = question.getAnswer().getProperty("weapon");
                Player p = Players.getInstance().getPlayer(question.getTarget());
                p.spawn(sp.number);
            }
            catch (NoSuchPlayerException nsp) {
                logger.log(Level.WARNING, "Unknown player trying to spawn?", nsp);
            }
        } else if (!eserv) {
            question.getResponder().getCommunicator().sendNormalServerMessage("You can bring the spawn question back by typing /respawn in a chat window.");
        }
    }

    static final void parseWithdrawMoneyQuestion(WithdrawMoneyQuestion question) {
        block16: {
            Creature responder = question.getResponder();
            if (responder.isDead()) {
                responder.getCommunicator().sendNormalServerMessage("You are dead, and may not withdraw any money.");
                return;
            }
            int type = question.getType();
            if (type == 0) {
                logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
                return;
            }
            if (type == 36) {
                block15: {
                    try {
                        Item token = Items.getItem(question.getTarget());
                        if (token.getTemplateId() == 236) {
                            if (!responder.isWithinDistanceTo(token.getPosX(), token.getPosY(), token.getPosZ(), 30.0f)) {
                                responder.getCommunicator().sendNormalServerMessage("You are too far away from the bank.");
                                return;
                            }
                            break block15;
                        }
                        responder.getCommunicator().sendNormalServerMessage("The " + token.getName() + " does not function as a bank.");
                        return;
                    }
                    catch (NoSuchItemException nsi) {
                        responder.getCommunicator().sendNormalServerMessage("The bank no longer is available as the token is gone.");
                        return;
                    }
                }
                long money = responder.getMoney();
                if (money > 0L) {
                    long valueWithdrawn = QuestionParser.getValueWithdrawn(question);
                    if (valueWithdrawn > 0L) {
                        try {
                            if (responder.chargeMoney(valueWithdrawn)) {
                                Item[] coins = Economy.getEconomy().getCoinsFor(valueWithdrawn);
                                Item inventory = responder.getInventory();
                                for (int x = 0; x < coins.length; ++x) {
                                    inventory.insertItem(coins[x]);
                                }
                                Change withd = Economy.getEconomy().getChangeFor(valueWithdrawn);
                                responder.getCommunicator().sendNormalServerMessage("You withdraw " + withd.getChangeString() + " from the bank.");
                                Change c = new Change(money - valueWithdrawn);
                                responder.getCommunicator().sendNormalServerMessage("New balance: " + c.getChangeString() + ".");
                                logger.info(responder.getName() + " withdraw " + withd.getChangeString() + " from the bank and should have " + c.getChangeString() + " now.");
                                break block16;
                            }
                            responder.getCommunicator().sendNormalServerMessage("You can not withdraw that amount of money at the moment.");
                        }
                        catch (IOException iox) {
                            logger.log(Level.WARNING, "Failed to withdraw money from " + responder.getName() + ":" + iox.getMessage(), iox);
                            responder.getCommunicator().sendNormalServerMessage("The transaction failed. Please contact the game masters using the <i>/dev</i> command.");
                        }
                    } else {
                        responder.getCommunicator().sendNormalServerMessage("No money withdrawn.");
                    }
                } else {
                    responder.getCommunicator().sendNormalServerMessage("You have no money in the bank.");
                }
            }
        }
    }

    static final void parseVillageInfoQuestion(VillageInfo question) {
        block12: {
            Creature responder = question.getResponder();
            int type = question.getType();
            if (type == 0) {
                logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
                return;
            }
            if (type == 14) {
                try {
                    Item token = Items.getItem(question.target);
                    int vill = token.getData2();
                    Village village = Villages.getVillage(vill);
                    long money = responder.getMoney();
                    if (money > 0L) {
                        long valueWithdrawn = QuestionParser.getValueWithdrawn(question);
                        if (valueWithdrawn > 0L) {
                            try {
                                if (village.plan != null) {
                                    if (responder.chargeMoney(valueWithdrawn)) {
                                        village.plan.addMoney(valueWithdrawn);
                                        village.plan.addPayment(responder.getName(), responder.getWurmId(), valueWithdrawn);
                                        Change newch = Economy.getEconomy().getChangeFor(valueWithdrawn);
                                        responder.getCommunicator().sendNormalServerMessage("You pay " + newch.getChangeString() + " to the upkeep fund of " + village.getName() + ".");
                                        logger.log(Level.INFO, responder.getName() + " added " + valueWithdrawn + " irons to " + village.getName() + " upkeep.");
                                    } else {
                                        responder.getCommunicator().sendNormalServerMessage("You don't have that much money.");
                                    }
                                    break block12;
                                }
                                responder.getCommunicator().sendNormalServerMessage("This village does not have an upkeep plan.");
                            }
                            catch (IOException iox) {
                                logger.log(Level.WARNING, "Failed to withdraw money from " + responder.getName() + ":" + iox.getMessage(), iox);
                                responder.getCommunicator().sendNormalServerMessage("The transaction failed. Please contact the game masters using the <i>/dev</i> command.");
                            }
                            break block12;
                        }
                        responder.getCommunicator().sendNormalServerMessage("No money withdrawn.");
                        break block12;
                    }
                    responder.getCommunicator().sendNormalServerMessage("You have no money in the bank.");
                }
                catch (NoSuchItemException nsi) {
                    logger.log(Level.WARNING, responder.getName() + " tried to get info for null token with id " + question.target, nsi);
                    responder.getCommunicator().sendNormalServerMessage("Failed to locate the village for that request. Please contact administration.");
                }
                catch (NoSuchVillageException nsv) {
                    logger.log(Level.WARNING, responder.getName() + " tried to get info for null village for token with id " + question.target);
                    responder.getCommunicator().sendNormalServerMessage("Failed to locate the village for that request. Please contact administration.");
                }
            }
        }
    }

    static final void parseVillageUpkeepQuestion(VillageUpkeep question) {
        block14: {
            Creature responder = question.getResponder();
            int type = question.getType();
            if (type == 0) {
                logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
                return;
            }
            if (type == 120) {
                try {
                    Village village;
                    if (question.target == -10L) {
                        village = question.getResponder().getCitizenVillage();
                    } else {
                        Item token = Items.getItem(question.target);
                        int vill = token.getData2();
                        village = Villages.getVillage(vill);
                    }
                    long money = responder.getMoney();
                    if (money > 0L) {
                        long valueWithdrawn = QuestionParser.getValueWithdrawn(question);
                        if (valueWithdrawn > 0L) {
                            try {
                                if (village.plan != null) {
                                    if (responder.chargeMoney(valueWithdrawn)) {
                                        village.plan.addMoney(valueWithdrawn);
                                        village.plan.addPayment(responder.getName(), responder.getWurmId(), valueWithdrawn);
                                        Change newch = Economy.getEconomy().getChangeFor(valueWithdrawn);
                                        responder.getCommunicator().sendNormalServerMessage("You pay " + newch.getChangeString() + " to the upkeep fund of " + village.getName() + ".");
                                        logger.log(Level.INFO, responder.getName() + " added " + valueWithdrawn + " irons to " + village.getName() + " upkeep.");
                                    } else {
                                        responder.getCommunicator().sendNormalServerMessage("You don't have that much money.");
                                    }
                                    break block14;
                                }
                                responder.getCommunicator().sendNormalServerMessage("This village does not have an upkeep plan.");
                            }
                            catch (IOException iox) {
                                logger.log(Level.WARNING, "Failed to withdraw money from " + responder.getName() + ":" + iox.getMessage(), iox);
                                responder.getCommunicator().sendNormalServerMessage("The transaction failed. Please contact the game masters using the <i>/dev</i> command.");
                            }
                            break block14;
                        }
                        responder.getCommunicator().sendNormalServerMessage("No money withdrawn.");
                        break block14;
                    }
                    responder.getCommunicator().sendNormalServerMessage("You have no money in the bank.");
                }
                catch (NoSuchItemException nsi) {
                    logger.log(Level.WARNING, responder.getName() + " tried to get info for null token with id " + question.target, nsi);
                    responder.getCommunicator().sendNormalServerMessage("Failed to locate the village for that request. Please contact administration.");
                }
                catch (NoSuchVillageException nsv) {
                    logger.log(Level.WARNING, responder.getName() + " tried to get info for null village for token with id " + question.target);
                    responder.getCommunicator().sendNormalServerMessage("Failed to locate the village for that request. Please contact administration.");
                }
            }
        }
    }

    static final void parseTitleCompoundQuestion(TitleCompoundQuestion question) {
        Titles.Title title;
        String meditationAns;
        String occultistAns;
        King king;
        Player responder = (Player)question.getResponder();
        Titles.Title[] titles = responder.getTitles();
        if (titles.length == 0 && responder.getAppointments() == 0L && !responder.isAppointed()) {
            logger.info(String.format("No titles found for %s.", responder.getName()));
            return;
        }
        if (Servers.isThisAPvpServer() && (king = King.getKing(question.getResponder().getKingdomId())) != null && (question.getResponder().getAppointments() != 0L || question.getResponder().isAppointed())) {
            Appointments a = Appointments.getAppointments(king.era);
            for (int x = 0; x < a.officials.length; ++x) {
                int oId = x + 1500;
                String office = question.getAnswer().getProperty("office" + oId);
                if (office == null || !Boolean.parseBoolean(office) || a.officials[x] != question.getResponder().getWurmId()) continue;
                Appointment o = a.getAppointment(oId);
                question.getResponder().getCommunicator().sendNormalServerMessage("You vacate the office of " + o.getNameForGender((byte)0) + ".", (byte)2);
                a.setOfficial(x + 1500, 0L);
            }
        }
        if ((occultistAns = question.getAnswer().getProperty("hideoccultist")) != null) {
            boolean bool = Boolean.parseBoolean(occultistAns);
            responder.setFlag(24, bool);
        }
        if ((meditationAns = question.getAnswer().getProperty("hidemeditation")) != null) {
            boolean bool = Boolean.parseBoolean(meditationAns);
            responder.setFlag(25, bool);
        }
        String t1 = question.getAnswer().getProperty("First");
        String t2 = question.getAnswer().getProperty("Second");
        if (t1 != null) {
            try {
                int id = Integer.parseInt(t1);
                if (id == 0) {
                    responder.setTitle(null);
                } else {
                    title = question.getFirstTitle(id - 1);
                    if (title == null) {
                        return;
                    }
                    responder.setTitle(title);
                }
            }
            catch (NumberFormatException nfe) {
                logger.log(Level.WARNING, responder.getName() + " tried to parse " + t1 + " as int.");
            }
        }
        if (t2 != null) {
            try {
                int id = Integer.parseInt(t2);
                if (id == 0) {
                    responder.setSecondTitle(null);
                } else {
                    title = question.getFirstTitle(id - 1);
                    if (title == null) {
                        return;
                    }
                    if (title == responder.getTitle()) {
                        responder.getCommunicator().sendSafeServerMessage("You cannot use two of the same title.");
                        responder.setSecondTitle(null);
                        return;
                    }
                    responder.setSecondTitle(title);
                }
            }
            catch (NumberFormatException nfe) {
                logger.log(Level.WARNING, responder.getName() + " tried to parse " + t2 + " as int.");
            }
        }
    }

    static final void parseTitleQuestion(TitleQuestion question) {
        String accept;
        String meditationAns;
        String occultistAns;
        King king;
        Player responder = (Player)question.getResponder();
        Titles.Title[] titles = responder.getTitles();
        if (titles.length == 0 && responder.getAppointments() == 0L && !responder.isAppointed()) {
            return;
        }
        if (Servers.isThisAPvpServer() && (king = King.getKing(question.getResponder().getKingdomId())) != null && (question.getResponder().getAppointments() != 0L || question.getResponder().isAppointed())) {
            Appointments a = Appointments.getAppointments(king.era);
            for (int x = 0; x < a.officials.length; ++x) {
                int oId = x + 1500;
                String office = question.getAnswer().getProperty("office" + oId);
                if (office == null || !Boolean.parseBoolean(office) || a.officials[x] != question.getResponder().getWurmId()) continue;
                Appointment o = a.getAppointment(oId);
                question.getResponder().getCommunicator().sendNormalServerMessage("You vacate the office of " + o.getNameForGender((byte)0) + ".", (byte)2);
                a.setOfficial(x + 1500, 0L);
            }
        }
        if ((occultistAns = question.getAnswer().getProperty("hideoccultist")) != null) {
            boolean bool = Boolean.parseBoolean(occultistAns);
            responder.setFlag(24, bool);
        }
        if ((meditationAns = question.getAnswer().getProperty("hidemeditation")) != null) {
            boolean bool = Boolean.parseBoolean(meditationAns);
            responder.setFlag(25, bool);
        }
        if ((accept = question.getAnswer().getProperty("TITLE")) != null) {
            try {
                int id = Integer.parseInt(accept);
                if (id == 0) {
                    responder.setTitle(null);
                } else {
                    Titles.Title title = question.getTitle(id - 1);
                    if (title == null) {
                        return;
                    }
                    responder.setTitle(title);
                }
            }
            catch (NumberFormatException nfe) {
                logger.log(Level.WARNING, responder.getName() + " tried to parse " + accept + " as int.");
            }
        }
    }

    static final void parseServerQuestion(ServerQuestion question) {
        Creature responder = question.getResponder();
        String key = "transferTo";
        String val = question.getAnswer().getProperty(key);
        int transid = 0;
        if (val != null) {
            ServerEntry targetserver;
            try {
                transid = Integer.parseInt(val);
            }
            catch (NumberFormatException nfe) {
                logger.log(Level.WARNING, responder.getName() + " tried to parse " + val + " as int.");
                responder.getCommunicator().sendNormalServerMessage("Illegal value for key " + key);
                return;
            }
            if (transid > 0 && (targetserver = question.getTransferEntry(transid - 1)) != null) {
                if (targetserver.isAvailable(responder.getPower(), responder.isReallyPaying())) {
                    Player playerToTransfer = (Player)responder;
                    if (WurmId.getType(question.getTarget()) == 0) {
                        try {
                            playerToTransfer = Players.getInstance().getPlayer(question.getTarget());
                        }
                        catch (NoSuchPlayerException noSuchPlayerException) {
                            // empty catch block
                        }
                    }
                    if (!responder.equals(playerToTransfer)) {
                        if (playerToTransfer.getPower() > responder.getPower()) {
                            responder.getCommunicator().sendNormalServerMessage("You are too weak to transfer " + playerToTransfer.getName() + " to " + targetserver.name + ".");
                            return;
                        }
                        responder.getCommunicator().sendNormalServerMessage("Transferring " + playerToTransfer.getName() + " to " + targetserver.name + ".");
                        playerToTransfer.getCommunicator().sendNormalServerMessage(responder.getName() + " transfers you to " + targetserver.name + ".");
                        logger.info(responder.getName() + " transfers " + playerToTransfer.getName() + " to " + targetserver.name + ".");
                    } else {
                        playerToTransfer.getCommunicator().sendNormalServerMessage("Transferring to " + targetserver.name + ".");
                        logger.info(playerToTransfer.getName() + " transferring to " + targetserver.name + ".");
                    }
                    Server.getInstance().broadCastAction(playerToTransfer.getName() + " transfers to " + targetserver.name + ".", playerToTransfer, 5);
                    int tilex = targetserver.SPAWNPOINTJENNX;
                    int tiley = targetserver.SPAWNPOINTJENNY;
                    if (playerToTransfer.getPower() <= 0) {
                        byte targetKingdom = playerToTransfer.getKingdomId();
                        if (targetserver.getKingdom() == 4) {
                            targetKingdom = 4;
                        }
                        playerToTransfer.lastKingdom = playerToTransfer.getKingdomId();
                        if (targetKingdom != playerToTransfer.getKingdomId()) {
                            try {
                                playerToTransfer.setKingdomId(targetKingdom);
                            }
                            catch (IOException iOException) {
                                // empty catch block
                            }
                        }
                    }
                    if (playerToTransfer.getKingdomId() == 1) {
                        tilex = targetserver.SPAWNPOINTJENNX;
                        tiley = targetserver.SPAWNPOINTJENNY;
                    } else if (playerToTransfer.getKingdomId() == 3) {
                        tilex = targetserver.SPAWNPOINTLIBX;
                        tiley = targetserver.SPAWNPOINTLIBY;
                    } else if (playerToTransfer.getKingdomId() == 2) {
                        tilex = targetserver.SPAWNPOINTMOLX;
                        tiley = targetserver.SPAWNPOINTMOLY;
                    }
                    playerToTransfer.sendTransfer(Server.getInstance(), targetserver.INTRASERVERADDRESS, Integer.parseInt(targetserver.INTRASERVERPORT), targetserver.INTRASERVERPASSWORD, targetserver.id, tilex, tiley, true, false, playerToTransfer.getKingdomId());
                    playerToTransfer.transferCounter = 30;
                    return;
                }
                responder.getCommunicator().sendNormalServerMessage(targetserver.name + " is not available now.");
            }
        }
        if (responder.getPower() > 2) {
            int addid = -1;
            key = "neighbourServer";
            val = question.getAnswer().getProperty(key);
            if (val != null) {
                try {
                    addid = Integer.parseInt(val);
                    if (addid > 0) {
                        key = "direction";
                        val = question.getAnswer().getProperty(key);
                        if (val != null) {
                            ServerEntry entry = question.getServerEntry(addid - 1);
                            if (entry != null) {
                                if (val.equals("0")) {
                                    val = "NORTH";
                                }
                                if (val.equals("1")) {
                                    val = "EAST";
                                }
                                if (val.equals("2")) {
                                    val = "SOUTH";
                                }
                                if (val.equals("3")) {
                                    val = "WEST";
                                }
                                Servers.addServerNeighbour(entry.id, val);
                                responder.getCommunicator().sendNormalServerMessage("Added server with id " + entry.id + " " + val + " of this server.");
                                logger.info(responder.getName() + " added server with name " + entry.name + " and id " + entry.id + " " + val + " of this server.");
                            } else {
                                responder.getCommunicator().sendNormalServerMessage("Failed to locate the server to add.");
                            }
                        }
                    }
                }
                catch (NumberFormatException nfe) {
                    logger.log(Level.WARNING, responder.getName() + " tried to parse " + val + " as int.");
                    responder.getCommunicator().sendNormalServerMessage("Illegal value for key " + key);
                    return;
                }
            }
            int delid = -1;
            key = "deleteServer";
            val = question.getAnswer().getProperty(key);
            if (val != null) {
                try {
                    delid = Integer.parseInt(val);
                    if (delid > 0) {
                        ServerEntry entry = question.getServerEntry(delid - 1);
                        if (entry != null) {
                            Servers.deleteServerEntry(entry.id);
                            responder.getCommunicator().sendNormalServerMessage("Deleted server with id " + entry.id + ".");
                            logger.info(responder.getName() + " Deleted server with name " + entry.name + " and id " + entry.id + '.');
                        } else {
                            responder.getCommunicator().sendNormalServerMessage("Failed to locate the server to delete.");
                        }
                    }
                }
                catch (NumberFormatException nfe) {
                    logger.log(Level.WARNING, responder.getName() + " tried to parse " + val + " as int.");
                    responder.getCommunicator().sendNormalServerMessage("Illegal value for key " + key);
                    return;
                }
            }
            int id = -1;
            key = "addid";
            val = question.getAnswer().getProperty(key);
            if (val != null && val.length() > 0) {
                try {
                    id = Integer.parseInt(val);
                    if (id < 0) {
                        responder.getCommunicator().sendAlertServerMessage("The id of the server can not be " + id + ".");
                        return;
                    }
                    ServerEntry entry = Servers.getServerWithId(id);
                    if (entry != null) {
                        responder.getCommunicator().sendAlertServerMessage("The id of the server already exists: " + id + ".");
                        return;
                    }
                }
                catch (NumberFormatException nfe) {
                    logger.log(Level.WARNING, responder.getName() + " tried to parse " + val + " as int.");
                    responder.getCommunicator().sendNormalServerMessage("Illegal value for key " + key);
                    return;
                }
            }
            if (id >= 0) {
                key = "addname";
                String name = val = question.getAnswer().getProperty(key);
                if (val != null && val.length() < 4) {
                    responder.getCommunicator().sendAlertServerMessage("The name of the server can not be " + val + ". It is too short.");
                    return;
                }
                key = "addhome";
                val = question.getAnswer().getProperty(key);
                boolean homeServer = false;
                if (val != null && val.equals("true")) {
                    homeServer = true;
                }
                key = "addpayment";
                val = question.getAnswer().getProperty(key);
                boolean isPayment = false;
                if (val != null && val.equals("true")) {
                    isPayment = true;
                }
                key = "addlogin";
                val = question.getAnswer().getProperty(key);
                boolean isLogin = false;
                if (val != null && val.equals("true")) {
                    isLogin = true;
                }
                int jennx = -1;
                key = "addsjx";
                val = question.getAnswer().getProperty(key);
                if (val != null) {
                    try {
                        jennx = Integer.parseInt(val);
                        if (jennx < 0) {
                            responder.getCommunicator().sendAlertServerMessage("Illegal value for start jenn x " + jennx + ".");
                            return;
                        }
                    }
                    catch (NumberFormatException nfe) {
                        logger.log(Level.WARNING, responder.getName() + " tried to parse " + val + " as int.");
                        responder.getCommunicator().sendNormalServerMessage("Illegal value for key " + key);
                        return;
                    }
                }
                int jenny = -1;
                key = "addsjy";
                val = question.getAnswer().getProperty(key);
                if (val != null) {
                    try {
                        jenny = Integer.parseInt(val);
                        if (jenny < 0) {
                            responder.getCommunicator().sendAlertServerMessage("Illegal value for start jenn y " + jenny + ".");
                            return;
                        }
                    }
                    catch (NumberFormatException nfe) {
                        logger.log(Level.WARNING, responder.getName() + " tried to parse " + val + " as int.");
                        responder.getCommunicator().sendNormalServerMessage("Illegal value for key " + key);
                        return;
                    }
                }
                int liby = -1;
                key = "addsly";
                val = question.getAnswer().getProperty(key);
                if (val != null) {
                    try {
                        liby = Integer.parseInt(val);
                        if (liby < 0) {
                            responder.getCommunicator().sendAlertServerMessage("Illegal value for start Libila y " + liby + ".");
                            return;
                        }
                    }
                    catch (NumberFormatException nfe) {
                        logger.log(Level.WARNING, responder.getName() + " tried to parse " + val + " as int.");
                        responder.getCommunicator().sendNormalServerMessage("Illegal value for key " + key);
                        return;
                    }
                }
                int libx = -1;
                key = "addslx";
                val = question.getAnswer().getProperty(key);
                if (val != null) {
                    try {
                        libx = Integer.parseInt(val);
                        if (libx < 0) {
                            responder.getCommunicator().sendAlertServerMessage("Illegal value for start Libila x " + libx + ".");
                            return;
                        }
                    }
                    catch (NumberFormatException nfe) {
                        logger.log(Level.WARNING, responder.getName() + " tried to parse " + val + " as int.");
                        responder.getCommunicator().sendNormalServerMessage("Illegal value for key " + key);
                        return;
                    }
                }
                int molx = -1;
                key = "addsmx";
                val = question.getAnswer().getProperty(key);
                if (val != null) {
                    try {
                        molx = Integer.parseInt(val);
                        if (molx < 0) {
                            responder.getCommunicator().sendAlertServerMessage("Illegal value for start Mol Rehan x " + molx + ".");
                            return;
                        }
                    }
                    catch (NumberFormatException nfe) {
                        logger.log(Level.WARNING, responder.getName() + " tried to parse " + val + " as int.");
                        responder.getCommunicator().sendNormalServerMessage("Illegal value for key " + key);
                        return;
                    }
                }
                int moly = -1;
                key = "addsmy";
                val = question.getAnswer().getProperty(key);
                if (val != null) {
                    try {
                        moly = Integer.parseInt(val);
                        if (moly < 0) {
                            responder.getCommunicator().sendAlertServerMessage("Illegal value for start Mol Rehan y " + moly + ".");
                            return;
                        }
                    }
                    catch (NumberFormatException nfe) {
                        logger.log(Level.WARNING, responder.getName() + " tried to parse " + val + " as int.");
                        responder.getCommunicator().sendNormalServerMessage("Illegal value for key " + key);
                        return;
                    }
                }
                int intraport = -1;
                key = "addintport";
                val = question.getAnswer().getProperty(key);
                if (val != null) {
                    try {
                        intraport = Integer.parseInt(val);
                        if (intraport < 0) {
                            responder.getCommunicator().sendAlertServerMessage("Illegal value for intra server port " + intraport + ".");
                            return;
                        }
                    }
                    catch (NumberFormatException nfe) {
                        logger.log(Level.WARNING, responder.getName() + " tried to parse " + val + " as int.");
                        responder.getCommunicator().sendNormalServerMessage("Illegal value for key " + key);
                        return;
                    }
                }
                int externalport = -1;
                key = "addextport";
                val = question.getAnswer().getProperty(key);
                if (val != null) {
                    try {
                        externalport = Integer.parseInt(val);
                        if (externalport < 0) {
                            responder.getCommunicator().sendAlertServerMessage("Illegal value for external server port " + externalport + ".");
                            return;
                        }
                    }
                    catch (NumberFormatException nfe) {
                        logger.log(Level.WARNING, responder.getName() + " tried to parse " + val + " as int.");
                        responder.getCommunicator().sendNormalServerMessage("Illegal value for key " + key);
                        return;
                    }
                }
                key = "addintip";
                String intraip = val = question.getAnswer().getProperty(key);
                if (val != null && val.length() < 8) {
                    responder.getCommunicator().sendAlertServerMessage("The internal ip address of the server can not be " + val + ". It is too short.");
                    return;
                }
                key = "addextip";
                String externalip = val = question.getAnswer().getProperty(key);
                if (val != null && val.length() < 8) {
                    responder.getCommunicator().sendAlertServerMessage("The external ip address of the server can not be " + val + ". It is too short.");
                    return;
                }
                key = "addintpass";
                String password = val = question.getAnswer().getProperty(key);
                if (val != null && val.length() < 4) {
                    responder.getCommunicator().sendAlertServerMessage("The password of the server can not be " + val + ". It is too short.");
                    return;
                }
                key = "addkingdom";
                val = question.getAnswer().getProperty(key);
                byte kingdom = 0;
                if (homeServer && val != null) {
                    try {
                        kingdom = Byte.parseByte(val);
                    }
                    catch (NumberFormatException nfe) {
                        logger.log(Level.WARNING, responder.getName() + " tried to parse " + val + " as int.");
                        responder.getCommunicator().sendNormalServerMessage("Illegal value for key " + key);
                        return;
                    }
                }
                String _consumerKeyToUse = "";
                String _consumerSecretToUse = "";
                String _applicationToken = "";
                String _applicationSecret = "";
                key = "consumerKeyToUse";
                val = question.getAnswer().getProperty(key);
                if (val == null) {
                    _consumerKeyToUse = val;
                }
                key = "consumerSecretToUse";
                val = question.getAnswer().getProperty(key);
                if (val == null) {
                    _consumerSecretToUse = val;
                }
                key = "applicationToken";
                val = question.getAnswer().getProperty(key);
                if (val == null) {
                    _applicationToken = val;
                }
                key = "applicationSecret";
                val = question.getAnswer().getProperty(key);
                if (val == null) {
                    _applicationSecret = val;
                }
                Servers.registerServer(id, name, homeServer, jennx, jenny, libx, liby, molx, moly, intraip, String.valueOf(intraport), password, externalip, String.valueOf(externalport), isLogin, kingdom, isPayment, _consumerKeyToUse, _consumerSecretToUse, _applicationToken, _applicationSecret, false, false, false);
                responder.getCommunicator().sendAlertServerMessage("You have successfully registered the server " + name + " and may now add it as a neighbour.");
                logger.info(responder.getName() + " successfully registered the server " + name + " with ID " + id + " and may now add it as a neighbour.");
            }
        }
    }

    static final void parseLCMManagementQuestion(LCMManagementQuestion question) {
        String playerName = question.getAnswer().getProperty("name");
        Creature performer = question.getResponder();
        if (playerName.isEmpty()) {
            if (performer != null) {
                performer.getCommunicator().sendNormalServerMessage("You didn't fill in a name.");
            }
            return;
        }
        if (question.getActionType() == 698) {
            Players.appointCA(performer, playerName);
        } else if (question.getActionType() == 699) {
            Players.appointCM(performer, playerName);
        } else if (question.getActionType() == 700) {
            Players.displayLCMInfo(performer, playerName);
        }
    }
}

