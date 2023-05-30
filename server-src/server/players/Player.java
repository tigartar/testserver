package com.wurmonline.server.players;

import com.wurmonline.communication.SocketConnection;
import com.wurmonline.server.Constants;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.Features;
import com.wurmonline.server.HistoryManager;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.LoginServerWebConnection;
import com.wurmonline.server.Message;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.Team;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.banks.Bank;
import com.wurmonline.server.banks.BankSlot;
import com.wurmonline.server.banks.BankUnavailableException;
import com.wurmonline.server.banks.Banks;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.MethodsCreatures;
import com.wurmonline.server.behaviours.MethodsReligion;
import com.wurmonline.server.behaviours.NoSuchActionException;
import com.wurmonline.server.behaviours.Seat;
import com.wurmonline.server.behaviours.Vehicle;
import com.wurmonline.server.behaviours.Vehicles;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.combat.ArmourTemplate;
import com.wurmonline.server.creatures.CombatHandler;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureCommunicator;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.Delivery;
import com.wurmonline.server.creatures.NoArmourException;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import com.wurmonline.server.creatures.Npc;
import com.wurmonline.server.creatures.SpellEffects;
import com.wurmonline.server.creatures.SpellEffectsEnum;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.economy.Shop;
import com.wurmonline.server.endgames.EndGameItem;
import com.wurmonline.server.endgames.EndGameItems;
import com.wurmonline.server.epic.Effectuator;
import com.wurmonline.server.highways.Route;
import com.wurmonline.server.intra.IntraServerConnection;
import com.wurmonline.server.intra.PlayerTransfer;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.items.Recipe;
import com.wurmonline.server.items.WurmMail;
import com.wurmonline.server.kingdom.Appointment;
import com.wurmonline.server.kingdom.Appointments;
import com.wurmonline.server.kingdom.King;
import com.wurmonline.server.kingdom.Kingdom;
import com.wurmonline.server.kingdom.Kingdoms;
import com.wurmonline.server.questions.ChallengeInfoQuestion;
import com.wurmonline.server.questions.ConchQuestion;
import com.wurmonline.server.questions.DropInfoQuestion;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.questions.Questions;
import com.wurmonline.server.questions.SimplePopup;
import com.wurmonline.server.questions.SpawnQuestion;
import com.wurmonline.server.skills.Affinities;
import com.wurmonline.server.skills.AffinitiesTimed;
import com.wurmonline.server.skills.Affinity;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.SkillsFactory;
import com.wurmonline.server.sounds.Sound;
import com.wurmonline.server.spells.Cooldowns;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.statistics.ChallengePointEnum;
import com.wurmonline.server.statistics.ChallengeSummary;
import com.wurmonline.server.steam.SteamId;
import com.wurmonline.server.support.Tickets;
import com.wurmonline.server.support.VoteQuestion;
import com.wurmonline.server.support.VoteQuestions;
import com.wurmonline.server.tutorial.Mission;
import com.wurmonline.server.tutorial.MissionPerformed;
import com.wurmonline.server.tutorial.MissionPerformer;
import com.wurmonline.server.tutorial.MissionTrigger;
import com.wurmonline.server.tutorial.MissionTriggers;
import com.wurmonline.server.tutorial.OldMission;
import com.wurmonline.server.tutorial.TriggerEffect;
import com.wurmonline.server.tutorial.TriggerEffects;
import com.wurmonline.server.villages.Citizen;
import com.wurmonline.server.villages.PvPAlliance;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.webinterface.WcAddFriend;
import com.wurmonline.server.webinterface.WcGlobalPM;
import com.wurmonline.server.webinterface.WcRemoveFriendship;
import com.wurmonline.server.webinterface.WcVoting;
import com.wurmonline.server.zones.FaithZone;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VirtualZone;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.PlayerOnlineStatus;
import com.wurmonline.shared.constants.ProtoConstants;
import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class Player extends Creature implements ProtoConstants {
   private static final Logger logger = Logger.getLogger(Player.class.getName());
   public static final long changeKingdomTime = 1209600000L;
   public static final long pvpDeathTime = 10800000L;
   public static final long sleepBonusIdleTimeout = 600000L;
   public static final long playerCombatTime = 300000L;
   public static final int minEnemyPresence = Servers.localServer.testServer ? 30 : 900;
   public static final int maxEnemyPresence = 1200;
   public static int newAffinityChance = 500;
   private final Set<Integer> kosPopups = new HashSet<>();
   private Map<Long, Creature> links = null;
   private Team team = null;
   private boolean mayInviteTeam = true;
   private Set<Long> phantasms = null;
   private static final PlayerInfo emptyInfo = new DbPlayerInfo("unkown");
   private PlayerInfo saveFile = emptyInfo;
   private long receivedLinkloss = 0L;
   private byte lastLinksSent = 0;
   private byte CRBonusCounter = 0;
   private byte CRBonus = 0;
   private byte farwalkerSeconds = 0;
   private int secondsToLogout = -1;
   public int secondsToLinkDeath = -1;
   public long lastSleepBonusActivity = 0L;
   public long lastActivity = 0L;
   public long startedSleepBonus = 0L;
   public int myceliumHealCounter = -1;
   private int favorGainSecondsLeft = 0;
   public Question question = null;
   private boolean fullyLoaded = false;
   private int lastMeditateX = -1;
   private int lastMeditateY = -1;
   public boolean justCombined = false;
   private long lastChatted = 0L;
   private long lastChattedLocal = 0L;
   private long lastMadeEmoteSound = 0L;
   public long startedTrading = System.currentTimeMillis();
   private int loginStep = 0;
   private boolean newPlayer = false;
   private LoginHandler loginHandler;
   public boolean loggedout = false;
   public byte lastKingdom = 0;
   private boolean legal = true;
   private boolean isTransferring = false;
   private Set<Item> itemsWatched = null;
   public float secondsPlayed = 1.0F;
   private int secondsPlayedSinceLinkloss = 1;
   private int pushCounter = 0;
   private long lastSentWarning;
   private boolean watchingBank = false;
   public Set<Spawnpoint> spawnpoints;
   private byte spnums = 0;
   public long sentClimbing = 0L;
   public long sentWind = 0L;
   public long sentMountSpeed = 0L;
   public boolean acceptsInvitations = false;
   private int maxNumActions = 2;
   public int stuckCounter = 0;
   public boolean GMINVULN = true;
   public boolean suiciding = false;
   public long lastSuicide = 0L;
   public short lastSentQuestion = 0;
   public int transferCounter = 0;
   public boolean moveWarned = false;
   public long moveWarnedTime = 0L;
   public long peakMoves = 0L;
   private short lastSentServerTime = 1;
   public long lastReferralQuestion = 0L;
   public boolean hasColoredChat = false;
   public int customRedChat = 255;
   public int customGreenChat = 140;
   public int customBlueChat = 0;
   private String affstring = null;
   private byte affcounter = 0;
   private boolean archeryMode = false;
   public boolean gotHash = false;
   public static final float minFavorLinked = 10.0F;
   private int moneySendCounter = 0;
   private boolean frozen = false;
   private boolean mayAttack = false;
   private boolean maySteal = false;
   public boolean sentChallenge = false;
   private int windowOfCreation = 0;
   private int windowOfAffinity = 0;
   public boolean isOnFire;
   public boolean hasReceivedInitialValreiData = false;
   private String disconnectReason = "You have been idle and was disconnected.";
   private double villageSkillModifier = 0.0;
   private int enemyPresenceCounter = 0;
   private int sendSleepCounter = 0;
   private Set<Creature> sparrers = null;
   private Set<Creature> duellers = null;
   public long lastDecreasedFatigue = System.currentTimeMillis();
   private int colorr = 0;
   private int colorg = 0;
   private int colorb = 0;
   private boolean hasLoveEffect = false;
   public long lastStoppedDragging = 0L;
   private int conchticker = 0;
   private static final long playerTutorialCutoffNumber = 37862368084224L;
   private String eigcId = "";
   public boolean kickedOffBoat = false;
   private boolean hasFingerEffect = false;
   private boolean hasCrownEffect = false;
   private int crownInfluence = 0;
   private boolean markedByOrb;
   private int teleportCounter = 0;
   private int tilesMovedDragging = 0;
   private int tilesMovedRiding = 0;
   private int tilesMoved = 0;
   private int tilesMovedDriving = 0;
   private int tilesMovedPassenger = 0;
   protected static final int MINRANK = 1000;
   private String afkMessage = "Sorry but I am not available at the moment, please leave a message and I'll get back to you as soon as I can.";
   private boolean respondingAsGM = false;
   private byte nextActionRarity = 0;
   public boolean justCreated = false;
   private Set<MapAnnotation> mapAnnotations = new HashSet<>();
   private final Map<Integer, PlayerVote> playerQuestionVotes = new ConcurrentHashMap<>();
   private boolean gotVotes = false;
   private boolean canVote = false;
   private boolean askedForVotes = false;
   private int mailItemsWaiting = 0;
   private int deliveriesWaiting = 0;
   private int deliveriesFailed = 0;
   private boolean gmLight = true;
   private long sendResponseTo = -10L;
   private int waitingForFriendCount = -1;
   private String waitingForFriendName = "";
   private Friend.Category waitingForFriendCategory = Friend.Category.Other;
   private boolean askingFriend = false;
   private long taggedItemId = -10L;
   private String taggedItem = "";
   final Map<Long, Long> privateEffects = new ConcurrentHashMap<>();
   private byte rarityShader = 0;
   private int raritySeconds = 0;
   private Recipe viewingRecipe = null;
   private boolean isWritingRecipe = false;
   private boolean hasCookbookOpen = false;
   private int studied = 0;
   private long whenStudied = 0L;
   private long removePvPDeathTimer = 0L;
   private String clientVersion = "UNKNOWN";
   private String clientSystem = "UNKNOWN";
   int messages = 0;
   private ConcurrentHashMap<Integer, Float> scoresToClear;
   private static final int KINGLIMIT = 100000;

   private Player(int aId, SocketConnection serverConnection) throws Exception {
      super(CreatureTemplateFactory.getInstance().getTemplate(aId));
      if (Constants.useQueueToSendDataToPlayers) {
         this.communicator = new PlayerCommunicatorQueued(this, serverConnection);
      } else {
         this.communicator = new PlayerCommunicator(this, serverConnection);
      }

      serverConnection.setLogin(true);
      this.musicPlayer = new MusicPlayer(this);
   }

   private Player(int aId) throws Exception {
      super(CreatureTemplateFactory.getInstance().getTemplate(aId));
      if (Constants.useQueueToSendDataToPlayers) {
         this.communicator = new CreatureCommunicator(this);
      } else {
         this.communicator = new CreatureCommunicator(this);
      }

      this.musicPlayer = new MusicPlayer(this);
      this.justCreated = true;
   }

   public Player(PlayerInfo aSaveFile, SocketConnection serverConnection) throws Exception {
      if (Constants.useQueueToSendDataToPlayers) {
         this.communicator = new PlayerCommunicatorQueued(this, serverConnection);
      } else {
         this.communicator = new PlayerCommunicator(this, serverConnection);
      }

      serverConnection.setLogin(true);
      this.saveFile = aSaveFile;
      if (this.saveFile.undeadType == 0 || this.saveFile.currentServer == Servers.localServer.id) {
         aSaveFile.setLogin();
         this.setName(aSaveFile.getName());
         this.setWurmId(aSaveFile.getPlayerId(), 0.0F, 0.0F, 0.0F, 0);
         this.status.load();
         if (!Constants.useQueueToSendDataToPlayers) {
            this.setFightingStyle(aSaveFile.fightmode, true);
            this.status.checkStaminaEffects(65535);
         }

         this.template = this.status.getTemplate();
         this.getMovementScheme().initalizeModifiersWithTemplate();
         this.skills = SkillsFactory.createSkills(this.getWurmId());
         this.sentClimbing = 0L;
         this.setPersonalSeed();
         this.setFinestAppointment();
         this.musicPlayer = new MusicPlayer(this);
         if (this.getPlayingTime() == 0L) {
            this.justCreated = true;
         }
      }
   }

   public Player(PlayerInfo aSaveFile) throws Exception {
      this.communicator = new CreatureCommunicator(this);
      this.saveFile = aSaveFile;
      this.setName(aSaveFile.getName());
      this.setWurmId(aSaveFile.getPlayerId(), 0.0F, 0.0F, 0.0F, 0);
      this.status.load();
      this.template = this.status.getTemplate();
      this.getMovementScheme().initalizeModifiersWithTemplate();
      this.skills = SkillsFactory.createSkills(this.getWurmId());
      this.sentClimbing = 0L;
      this.musicPlayer = new MusicPlayer(this);
      this.justCreated = true;
   }

   public final void addGlobalEffect(Long effectId, int seconds) {
      this.privateEffects.put(effectId, System.currentTimeMillis() + (long)seconds * 1000L);
   }

   public final boolean hasGlobalEffect(long id) {
      return this.privateEffects.get(id) != null;
   }

   public final void addItemEffect(long id, int tilex, int tiley, float posz) {
      long effectId = id <= 0L ? Long.MAX_VALUE - (long)Server.rand.nextInt(1000) : id;
      if (!this.hasGlobalEffect(effectId)) {
         logger.log(Level.INFO, "Sending gloobal eff to " + this.getName() + " " + tilex + "," + tiley + " " + posz);
         this.getCommunicator().sendAddEffect(effectId, (short)4, (float)(tilex << 2), (float)(tiley << 2), posz, (byte)0);
         this.addGlobalEffect(effectId, 300);
      }
   }

   private final void pollGlobalEffects() {
      HashSet<Long> toRemove = new HashSet<>();

      for(Entry<Long, Long> effect : this.privateEffects.entrySet()) {
         if (System.currentTimeMillis() > effect.getValue()) {
            toRemove.add(effect.getKey());
         }
      }

      for(Long eff : toRemove) {
         this.privateEffects.remove(eff);
         this.getCommunicator().sendRemoveEffect(eff);
      }
   }

   public void initialisePlayer(PlayerInfo aSaveFile) {
      if (Constants.useQueueToSendDataToPlayers) {
         this.setFightingStyle(aSaveFile.fightmode, true);
         this.status.checkStaminaEffects(65535);
      }
   }

   @Override
   public boolean isPlayer() {
      return true;
   }

   @Override
   public Logger getLogger() {
      return Players.getLogger(this);
   }

   @Override
   public boolean isLogged() {
      return this.saveFile.logging;
   }

   @Override
   public boolean hasColoredChat() {
      return this.hasColoredChat;
   }

   @Override
   public int getCustomGreenChat() {
      return this.customGreenChat;
   }

   @Override
   public int getCustomRedChat() {
      return this.customRedChat;
   }

   @Override
   public int getCustomBlueChat() {
      return this.customBlueChat;
   }

   public void checkBodyInventoryConsistency() throws Exception {
      if (this.status.getBodyId() == -10L) {
         this.status.createNewBody();
      }

      if (this.status.getInventoryId() == -10L) {
         this.status.createNewPossessions();
      }
   }

   public void setLoginHandler(@Nullable LoginHandler handler) {
      this.loginHandler = handler;
   }

   public boolean mayHearDevTalk() {
      return this.getPower() >= 2 || this.saveFile.mayHearDevTalk;
   }

   public boolean mayHearMgmtTalk() {
      return this.getPower() >= 1 || this.saveFile.mayMute || this.saveFile.mayHearDevTalk || this.saveFile.playerAssistant;
   }

   public LoginHandler getLoginhandler() {
      return this.loginHandler;
   }

   public void setFullyLoaded() {
      this.fullyLoaded = true;
   }

   public boolean isFullyLoaded() {
      return this.fullyLoaded;
   }

   @Override
   public byte getAttitude(Creature aTarget) {
      if (this.getPower() > 0) {
         return (byte)(this.getPower() >= 5 ? 6 : 3);
      } else if (this.opponent == aTarget) {
         return 2;
      } else if (this.getSaveFile().pet != -10L && aTarget.getWurmId() == this.getSaveFile().pet) {
         return 1;
      } else if (aTarget.getDominator() != null && aTarget.getDominator() != this) {
         return this.getAttitude(aTarget.getDominator());
      } else if (aTarget.isReborn() && this.getKingdomTemplateId() == 3) {
         return 0;
      } else if (aTarget.getKingdomId() != 0 && !this.isFriendlyKingdom(aTarget.getKingdomId())) {
         return 2;
      } else if (!aTarget.hasAttackedUnmotivated() || !aTarget.isPlayer() && aTarget.isDominated() && aTarget.getDominator() == this) {
         if (this.citizenVillage != null) {
            if (aTarget.citizenVillage == this.citizenVillage) {
               return 1;
            }

            if (this.citizenVillage.isAlly(aTarget)) {
               return 1;
            }

            if (this.citizenVillage.isEnemy(aTarget.citizenVillage)) {
               return 2;
            }

            if (this.citizenVillage.isEnemy(aTarget)) {
               return 2;
            }

            if (aTarget.getCitizenVillage() != null && aTarget.getCitizenVillage().isEnemy(this)) {
               return 2;
            }

            if (this.citizenVillage.getReputation(aTarget) <= -30) {
               return 0;
            }
         }

         if (this.getKingdomId() != 3 && aTarget.getReputation() < 0) {
            return 2;
         } else if (aTarget.isAggHuman()) {
            return (byte)(aTarget.getKingdomId() != 0 && this.isFriendlyKingdom(aTarget.getKingdomId()) && aTarget.isDominated() ? 0 : 2);
         } else {
            return (byte)(aTarget.isPlayer() && this.isFriend(aTarget.getWurmId()) ? 7 : 0);
         }
      } else {
         return 2;
      }
   }

   public void sendReligion() {
      this.getCommunicator().sendAddSkill(2147483644, 2147483643, "Favor", this.saveFile.getFavor(), this.saveFile.getFavor(), 0);
      this.getCommunicator().sendAddSkill(2147483645, 2147483643, "Faith", this.saveFile.getFaith(), this.saveFile.getFaith(), 0);
      this.getCommunicator().sendAddSkill(2147483642, 2147483643, "Alignment", this.saveFile.getAlignment(), this.saveFile.getAlignment(), 0);
   }

   @Override
   public float getBaseCombatRating() {
      return this.template.baseCombatRating;
   }

   @Override
   public void calculateZoneBonus(int tilex, int tiley, boolean surfaced) {
      try {
         if (Servers.localServer.HOMESERVER) {
            if (this.currentKingdom == 0) {
               this.currentKingdom = Servers.localServer.KINGDOM;
               this.getCommunicator().sendNormalServerMessage("You enter " + Kingdoms.getNameFor(this.currentKingdom) + ".");
            }
         } else {
            this.setCurrentKingdom(this.getCurrentKingdom());
         }

         float initial = this.zoneBonus;
         this.zoneBonus = 0.0F;
         Deity deity = this.getDeity();
         if (deity != null) {
            if (this.isChampion() && this.getCurrentKingdom() != this.getKingdomId()) {
               this.zoneBonus = 50.0F;
            }

            if (!this.isChampion()) {
               FaithZone z = Zones.getFaithZone(tilex, tiley, surfaced);
               if (z != null) {
                  if (z.getCurrentRuler() == deity) {
                     this.zoneBonus += 5.0F;
                     if (this.getFaith() > 30.0F) {
                        this.zoneBonus += 10.0F;
                     }

                     if (this.getFaith() > 90.0F) {
                        this.zoneBonus += this.getFaith() - 90.0F;
                     }

                     if (Features.Feature.NEWDOMAINS.isEnabled()) {
                        this.zoneBonus += (float)z.getStrengthForTile(tilex, tiley, surfaced) / 2.0F;
                     } else {
                        this.zoneBonus += (float)z.getStrength() / 2.0F;
                     }
                  } else if ((Features.Feature.NEWDOMAINS.isEnabled() ? z.getStrengthForTile(tilex, tiley, surfaced) : z.getStrength()) == 0
                     && this.getFaith() >= 90.0F) {
                     this.zoneBonus = 5.0F + this.getFaith() - 90.0F;
                  }
               } else if (this.getFaith() >= 90.0F) {
                  this.zoneBonus = 5.0F + this.getFaith() - 90.0F;
               }
            }

            if (initial != this.zoneBonus) {
               if (this.zoneBonus == 0.0F) {
                  this.getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.FAITHBONUS);
               } else {
                  this.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.FAITHBONUS, 100000, this.zoneBonus);
               }
            }
         }
      } catch (NoSuchZoneException var7) {
         logger.log(Level.WARNING, "No faith zone at " + tilex + "," + tiley + ", surf=" + surfaced);
      }
   }

   public final void sendKarma() {
      this.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.KARMA, 100000, (float)this.saveFile.karma);
   }

   public final void sendScenarioKarma() {
      if (Servers.localServer.EPIC) {
         this.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.SCENARIOKARMA, 100000, (float)this.saveFile.getScenarioKarma());
      }
   }

   @Override
   public void setSpam(boolean spam) {
      this.saveFile.setSpamMode(spam);
      if (this.saveFile.spamMode()) {
         this.getCommunicator().sendNormalServerMessage("You are now in spam mode.");
      } else {
         this.getCommunicator().sendNormalServerMessage("You are now in nospam mode.");
      }
   }

   @Override
   public boolean spamMode() {
      return this.saveFile.spamMode();
   }

   public void setQuestion(@Nullable Question aQuestion) {
      if (this.question != null) {
         Questions.removeQuestion(this.question);
      }

      this.question = aQuestion;
   }

   public Question getCurrentQuestion() {
      return this.question;
   }

   public void setViewingRecipe(Recipe recipe) {
      this.viewingRecipe = recipe;
   }

   public Recipe getViewingRecipe() {
      return this.viewingRecipe;
   }

   public void setIsWritingRecipe(boolean isWriting) {
      this.isWritingRecipe = isWriting;
   }

   public boolean isWritingRecipe() {
      return this.isWritingRecipe;
   }

   public void setStudied(int studied) {
      this.studied = studied;
      if (studied > 0) {
         this.whenStudied = WurmCalendar.getCurrentTime();
      } else {
         this.whenStudied = 0L;
      }
   }

   public int getStudied() {
      return this.studied;
   }

   public void setIsViewingCookbook() {
      this.hasCookbookOpen = true;
   }

   public boolean isViewingCookbook() {
      return this.hasCookbookOpen;
   }

   @Override
   public List<Route> getHighwayPath() {
      return this.saveFile.getHighwayPath();
   }

   @Override
   public void setHighwayPath(String newDestination, List<Route> newPath) {
      this.saveFile.setHighwayPath(newDestination, newPath);
   }

   @Override
   public String getHighwayPathDestination() {
      return this.saveFile.getHighwayPathDestination();
   }

   public void setSaveFile(PlayerInfo aSaveFile) throws Exception {
      this.saveFile = aSaveFile;
      this.setName(aSaveFile.getName());
      if (!this.guest) {
         aSaveFile.save();
      }

      this.setPersonalSeed();
      this.setFightingStyle(aSaveFile.fightmode, true);
   }

   private void setPersonalSeed() {
      Random personalRandom = new Random((long)this.name.hashCode());
      if (this.getPower() > 0) {
         this.colorr = 200 + personalRandom.nextInt(50);
         this.colorg = 200 + personalRandom.nextInt(50);
         this.colorb = 200 + personalRandom.nextInt(50);
      } else if (this.getKingdomId() <= 4 && this.getKingdomId() >= 0) {
         this.colorr = 127 + personalRandom.nextInt(100);
         this.colorg = 127 + personalRandom.nextInt(100);
         this.colorb = 127 + personalRandom.nextInt(100);
      } else {
         Kingdom k = Kingdoms.getKingdomOrNull(this.getKingdomId());
         if (k != null) {
            this.colorr = k.getColorRed();
            this.colorg = k.getColorGreen();
            this.colorb = k.getColorBlue();
         }
      }
   }

   @Override
   public byte getColorRed() {
      return this.getPower() < 2 ? (byte)this.template.colorRed : (byte)this.colorr;
   }

   @Override
   public byte getColorGreen() {
      return this.getPower() < 2 ? (byte)this.template.colorRed : (byte)this.colorg;
   }

   @Override
   public byte getColorBlue() {
      return this.getPower() < 2 ? (byte)this.template.colorRed : (byte)this.colorb;
   }

   @Override
   public boolean isDead() {
      return this.saveFile.dead;
   }

   private void setDead(boolean dead) {
      this.saveFile.setDead(dead);
   }

   @Override
   public boolean isLegal() {
      return this.legal;
   }

   @Override
   public void setLegal(boolean mode) {
      if (!Servers.localServer.PVPSERVER) {
         this.legal = true;
         this.getCommunicator().sendNormalServerMessage("You will always stay within legal limits in these lands.");
      } else if (this.getKingdomTemplateId() != 3
         && (this.getCitizenVillage() != null && this.getCitizenVillage().getMayor().wurmId == this.getWurmId() || this.isKing())) {
         this.legal = true;
         if (this.isKing()) {
            this.getCommunicator()
               .sendNormalServerMessage(
                  "As the ruler of "
                     + Kingdoms.getNameFor(this.getKingdomId())
                     + " you may not risk joining the Horde of the Summoned by performing illegal actions."
               );
         } else {
            this.getCommunicator()
               .sendNormalServerMessage("As the ruler of a settlement you may not risk joining the Horde of the Summoned by performing illegal actions.");
         }
      } else {
         this.legal = mode;
         if (this.legal) {
            this.getCommunicator().sendNormalServerMessage("You will now stay within legal limits.");
         } else {
            this.getCommunicator().sendNormalServerMessage("You will no longer care about local laws.");
         }
      }

      this.getCommunicator().sendToggle(2, this.legal);
   }

   @Override
   public void setAutofight(boolean mode) {
      this.saveFile.setAutofight(mode);
      if (mode) {
         this.getCommunicator().sendNormalServerMessage("You will now select stance and special moves automatically in combat.");
         this.getCommunicator().sendNormalServerMessage("You may always do any available moves manually anyway.");
         this.getCommunicator().sendNormalServerMessage("You will still have to select normal, aggressive or defensive stance.");
         this.getCommunicator().sendNormalServerMessage("You will also have to shield bash, taunt or throw items manually.");
         this.getCommunicator().sendSpecialMove((short)-1, "N/A");
         this.getCommunicator().sendCombatOptions(CombatHandler.NO_COMBAT_OPTIONS, (short)-1);
      } else {
         this.getCommunicator().sendNormalServerMessage("You will now have to make manual stance decisions in combat.");
         if (this.isFighting()) {
            this.getCombatHandler().setSentAttacks(false);
            this.getCombatHandler().calcAttacks(false);
         }
      }

      this.getCommunicator().sendToggle(4, mode);
   }

   @Override
   public boolean isAutofight() {
      return this.saveFile.autoFighting;
   }

   @Override
   public boolean isArcheryMode() {
      return this.archeryMode;
   }

   public void setArcheryMode(boolean mode) {
      this.archeryMode = mode;
      if (mode) {
         this.getCommunicator().sendNormalServerMessage("You will now throw items if you double-click an enemy.");
         this.getCommunicator().sendNormalServerMessage("If you wield a bow you will try to shoot instead.");
      } else {
         this.getCommunicator().sendNormalServerMessage("You will no longer use ranged attacks while double-clicking.");
      }

      this.getCommunicator().sendToggle(100, mode);
   }

   @Override
   public void setFaithMode(boolean mode) {
      this.faithful = mode;
      if (this.faithful) {
         this.getCommunicator().sendNormalServerMessage("You will try to obey your gods wishes accordingly.");
      } else {
         this.getCommunicator().sendNormalServerMessage("You may now go against the will of your god.");
      }

      this.getCommunicator().sendToggle(1, this.faithful);
   }

   @Override
   public void setClimbing(boolean climbing) throws IOException {
      if (this.saveFile.climbing && !climbing && this.secondsPlayed > 120.0F) {
         this.sentClimbing = System.currentTimeMillis();
      }

      this.saveFile.setClimbing(climbing);
      if (climbing) {
         this.getCommunicator().sendNormalServerMessage("You will now attempt to climb steep areas.");
      } else {
         this.getCommunicator().sendNormalServerMessage("You will no longer climb.");
      }

      this.getCommunicator().sendClimb(climbing);
      this.getCommunicator().sendToggle(0, climbing);
      this.staminaPollCounter = 2;
   }

   @Override
   public boolean mayChangeDeity(int targetDeity) {
      return this.saveFile.mayChangeDeity(targetDeity);
   }

   @Override
   public boolean mayChangeKingdom(Creature converter) {
      boolean isPlayerConversion = converter != null;
      boolean convertingToCustom = isPlayerConversion && converter.isOfCustomKingdom();
      if (Servers.localServer.challengeServer && this.getPower() <= 0) {
         return false;
      } else {
         if (this.getCitizenVillage() != null && this.getCitizenVillage().getMayor() != null && this.getCitizenVillage().getMayor().wurmId == this.getWurmId()
            )
          {
            if (!convertingToCustom) {
               if (isPlayerConversion) {
                  converter.getCommunicator().sendNormalServerMessage("You cannot convert the mayor and their deed to a template kingdom!");
               }

               return false;
            }

            if (this.getCitizenVillage().isCapital()) {
               if (isPlayerConversion) {
                  converter.getCommunicator()
                     .sendNormalServerMessage("You cannot convert the mayor and their deed, because their deed is the capital of their kingdom.");
               }

               return false;
            }

            int mindist = Kingdoms.minKingdomDist;
            Village village = this.getCitizenVillage();
            int startX = village.getStartX() - 5 - village.getPerimeterSize() - mindist;
            int startY = village.getStartY() - 5 - village.getPerimeterSize() - mindist;
            int endX = village.getEndX() + 5 + village.getPerimeterSize() + mindist;
            int endY = village.getEndY() + 5 + village.getPerimeterSize() + mindist;
            int startExclusionX = village.getStartX() - 5 - village.getPerimeterSize() - mindist / 2;
            int startExclusionY = village.getStartY() - 5 - village.getPerimeterSize() - mindist / 2;
            int endExclusionX = village.getEndX() + 5 + village.getPerimeterSize() + mindist / 2;
            int endExclusionY = village.getEndY() + 5 + village.getPerimeterSize() + mindist / 2;

            for(Village v : Villages.getVillagesWithin(startX, startY, endX, endY)) {
               if (v.getId() != this.getVillageId() && v.kingdom == this.getKingdomId()) {
                  if (isPlayerConversion) {
                     converter.getCommunicator()
                        .sendNormalServerMessage("You cannot convert the mayor and their deed, because there are deeds nearby of their own kingdom.");
                     converter.getCommunicator()
                        .sendNormalServerMessage("If they were to convert, their deed would be very close to other deeds of their old kingdom.");
                     if (this.getPower() >= 2) {
                        converter.getCommunicator()
                           .sendNormalServerMessage(
                              "The nearest deed is " + v.getName() + " which is located at (" + v.getTokenX() + ", " + v.getTokenY() + ")"
                           );
                     }
                  }

                  return false;
               }
            }

            if (!Zones.isKingdomBlocking(startX, startY, endX, endY, (byte)0, startExclusionX, startExclusionY, endExclusionX, endExclusionY)) {
               if (isPlayerConversion) {
                  converter.getCommunicator()
                     .sendNormalServerMessage("You cannot convert the mayor and their deed, because there cannot be any kingdom influence nearby.");
               }

               return false;
            }
         }

         if (this.isKing()) {
            if (isPlayerConversion) {
               converter.getCommunicator().sendNormalServerMessage("You cannot convert the king of another kingdom!");
            }

            return false;
         } else if (convertingToCustom && this.isChampion()) {
            if (isPlayerConversion) {
               converter.getCommunicator().sendNormalServerMessage("You cannot convert a champion to your kingdom.");
            }

            return false;
         } else {
            if (this.getPower() <= 0) {
               boolean canConvert = System.currentTimeMillis() - this.saveFile.lastChangedKindom > this.getChangeKingdomLimit();
               if (!canConvert) {
                  if (isPlayerConversion) {
                     converter.getCommunicator().sendNormalServerMessage(this.getName() + " has converted too recently.");
                  }

                  return false;
               }
            }

            return true;
         }
      }
   }

   public final long getChangeKingdomLimit() {
      return this.getKingdomTemplateId() == 3 ? 2419200000L : 1209600000L;
   }

   @Override
   public void increaseChangedKingdom(boolean setTimeStamp) throws IOException {
      this.saveFile.setChangedKingdom((byte)(this.saveFile.getChangedKingdom() + 1), setTimeStamp);
   }

   @Override
   public void setChangedDeity() throws IOException {
      this.saveFile.setChangedDeity();
      this.achievement(556);
   }

   public PlayerInfo getSaveFile() {
      return this.saveFile;
   }

   public boolean isReimbursed() {
      return this.saveFile.isReimbursed();
   }

   @Override
   public long getPlayingTime() {
      return this.saveFile.lastLogin > 0L ? this.saveFile.playingTime + System.currentTimeMillis() - this.saveFile.lastLogin : this.saveFile.playingTime;
   }

   @Override
   public boolean mayLeadMoreCreatures() {
      return this.followers == null || this.followers.size() < 4 || this.getPower() >= 2 && this.followers.size() < 10;
   }

   @Override
   public void dropLeadingItem(Item item) {
      if (this.followers != null && !this.followers.isEmpty()) {
         Set<Creature> toRemove = new HashSet<>();

         for(Entry<Creature, Item> entry : this.followers.entrySet()) {
            Item titem = entry.getValue();
            if (titem != null && titem.equals(item)) {
               toRemove.add(entry.getKey());
            }
         }

         if (toRemove.size() > 0) {
            for(Creature creature : toRemove) {
               this.followers.remove(creature);
               creature.setLeader(null);
            }
         }
      }
   }

   @Override
   public boolean isItemLeading(Item item) {
      if (this.followers != null) {
         for(Item litem : this.followers.values()) {
            if (litem != null && litem.equals(item)) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public Item getLeadingItem(Creature follower) {
      if (this.followers != null) {
         for(Entry<Creature, Item> entry : this.followers.entrySet()) {
            Creature creature = entry.getKey();
            if (follower != null && follower.equals(creature)) {
               return entry.getValue();
            }
         }
      }

      return null;
   }

   @Override
   public Creature getFollowedCreature(Item leadingItem) {
      if (this.followers != null) {
         for(Entry<Creature, Item> entry : this.followers.entrySet()) {
            Item titem = entry.getValue();
            if (titem != null && titem.equals(leadingItem)) {
               return entry.getKey();
            }
         }
      }

      return null;
   }

   @Override
   public void addFollower(Creature follower, Item leadingItem) {
      if (this.followers == null) {
         this.followers = new HashMap<>();
      }

      this.followers.put(follower, leadingItem);
   }

   @Override
   public boolean addItemWatched(Item watched) {
      if (this.itemsWatched == null) {
         this.itemsWatched = new HashSet<>();
      }

      if (!this.itemsWatched.contains(watched)) {
         this.itemsWatched.add(watched);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean isItemWatched(Item watched) {
      return true;
   }

   @Override
   public boolean removeItemWatched(Item watched) {
      if (this.itemsWatched != null && this.itemsWatched.contains(watched)) {
         this.itemsWatched.remove(watched);
         return true;
      } else {
         return false;
      }
   }

   private Item[] getItemsWatched() {
      return this.itemsWatched == null ? new Item[0] : this.itemsWatched.toArray(new Item[this.itemsWatched.size()]);
   }

   private void checkItemsWatched() {
      if (this.itemsWatched != null && this.itemsWatched.size() > 0) {
         Item[] itemArr = this.getItemsWatched();

         for(int x = 0; x < itemArr.length; ++x) {
            if (!this.hasLink()) {
               itemArr[x].removeWatcher(this, false);
               this.removeItemWatched(itemArr[x]);
            } else {
               Item checkItem = itemArr[x];
               if (checkItem.getTemplateId() == 1342 && checkItem.getData() != -1L) {
                  try {
                     checkItem = Items.getItem(checkItem.getData());
                  } catch (NoSuchItemException var6) {
                     logger.log(Level.WARNING, var6.getMessage(), (Throwable)var6);
                  }
               }

               if (checkItem.getWurmId() != this.getVehicle()
                  && !this.isWithinDistanceTo(
                     checkItem.getPosX(),
                     checkItem.getPosY(),
                     checkItem.getPosZ(),
                     checkItem.isVehicle() && !checkItem.isTent() ? (float)Math.max(6, checkItem.getSizeZ() / 100) : 6.0F
                  )) {
                  if (this.getPower() > 0) {
                     logger.log(
                        Level.INFO,
                        "Stopping watching "
                           + itemArr[x].getName()
                           + " because not within distance to "
                           + itemArr[x].getPosX() / 4.0F
                           + ", "
                           + itemArr[x].getPosY() / 4.0F
                     );
                  }

                  this.getCommunicator().sendCloseInventoryWindow(itemArr[x].getWurmId());
                  itemArr[x].removeWatcher(this, false);
                  this.removeItemWatched(itemArr[x]);
                  if (itemArr[x].getTemplateId() == 1342 && itemArr[x].getData() != -1L) {
                     this.getCommunicator().sendUpdateSelectBar(checkItem.getWurmId(), false);
                  }
               }
            }
         }
      }

      if (this.watchingBank) {
         if (this.currentVillage != null) {
            try {
               Item token = this.currentVillage.getToken();
               if (!this.isWithinDistanceTo(token.getPosX(), token.getPosY(), token.getPosZ(), 12.0F)) {
                  this.closeBank();
               }
            } catch (NoSuchItemException var5) {
               this.closeBank();
            }
         } else {
            this.closeBank();
         }
      }
   }

   public void openBank() {
      try {
         Bank bank = Banks.getBank(this.getWurmId());
         if (bank != null) {
            bank.open();
            BankSlot[] slots = bank.slots;
            if (slots != null) {
               String lName = bank.getCurrentVillage().getName();
               this.getCommunicator().sendOpenInventoryWindow(bank.id, "Bank of " + lName);

               for(int x = 0; x < slots.length; ++x) {
                  if (slots[x] != null) {
                     slots[x].item.addWatcher(bank.id, this);
                  }
               }

               this.watchingBank = true;
            }
         } else {
            this.getCommunicator().sendNormalServerMessage("You have no bank account.");
         }
      } catch (BankUnavailableException var5) {
         this.getCommunicator().sendNormalServerMessage(var5.getMessage());
      }
   }

   public boolean isNewTutorial() {
      return this.getWurmId() > 37862368084224L;
   }

   public boolean startBank(Village village) {
      if (Banks.startBank(this.getWurmId(), 5, village.getId())) {
         this.getCommunicator().sendNormalServerMessage("You open a bank account here. Congratulations!");
         return true;
      } else {
         Bank bank = Banks.getBank(this.getWurmId());
         if (bank != null) {
            try {
               Village vill = bank.getCurrentVillage();
               if (vill != null) {
                  this.getCommunicator().sendNormalServerMessage("You already have a bank account in " + vill.getName() + ".");
               } else {
                  this.getCommunicator().sendNormalServerMessage("You already have a bank account but it is unavailable. Talk to the administrators.");
               }
            } catch (BankUnavailableException var4) {
               this.getCommunicator().sendNormalServerMessage("You already have a bank account but need to transfer it to a village.");
            }
         }

         return false;
      }
   }

   public void closeBank() {
      if (this.watchingBank) {
         this.watchingBank = false;
         Bank bank = Banks.getBank(this.getWurmId());
         if (bank != null) {
            bank.open = false;
            BankSlot[] slots = bank.slots;
            if (slots != null) {
               for(int x = 0; x < slots.length; ++x) {
                  if (slots[x] != null) {
                     slots[x].item.removeWatcher(this, false);
                  }
               }
            }

            this.getCommunicator().sendCloseInventoryWindow(bank.id);
         }
      }
   }

   @Override
   public void trainSkill(String sname) throws Exception {
   }

   @Override
   public void savePosition(int zoneid) throws IOException {
      this.status.savePosition(this.getWurmId(), true, zoneid, false);
   }

   @Override
   public void save() throws IOException {
      if (this.fullyLoaded) {
         this.saveFile.save();
         this.status.save();
         this.status.savePosition(this.getWurmId(), true, this.status.getZoneId(), true);
         this.possessions.save();
         this.skills.save();
      }
   }

   public void sendToWorld() {
      try {
         Zones.getZone(this.getTileX(), this.getTileY(), this.isOnSurface()).addCreature(this.getWurmId());
      } catch (NoSuchCreatureException var2) {
      } catch (NoSuchZoneException var3) {
      } catch (NoSuchPlayerException var4) {
      }
   }

   public void setLogout() {
      if (this.isSignedIn()) {
         this.getCommunicator().signOut("");
      }

      if (this.question != null) {
         this.question.timedOut();
      }

      this.saveFile.logout();
   }

   @Override
   public boolean isTransferring() {
      return this.isTransferring;
   }

   public void setIsTransferring(boolean _isTransferring) {
      this.isTransferring = _isTransferring;
   }

   @Override
   public boolean isOnCurrentServer() {
      return Servers.localServer.id == this.getSaveFile().currentServer;
   }

   public void sleep() throws Exception {
      if (!this.guest) {
         if (this.getStatus() != null) {
            this.getStatus().savePosition(this.getWurmId(), true, this.getStatus().getZoneId(), true);
         }

         if (this.fullyLoaded) {
            try {
               this.setLogout();
               this.saveFile.save();
               this.status.save();
               if (!this.saveFile.hasMovedInventory()) {
                  this.possessions.sleep(Servers.localServer.isChallengeOrEpicServer());
               }

               this.getBody().sleep(this, Servers.localServer.EPIC);
               this.skills.save();
            } catch (Exception var4) {
               logger.log(Level.WARNING, "Error when sleeping player id " + this.getWurmId() + " : " + var4.getMessage(), (Throwable)var4);
            }
         } else {
            try {
               if (this.possessions != null && this.possessions.getInventory() != null) {
                  Item[] items = this.possessions.getInventory().getAllItems(true);

                  for(int x = 0; x < items.length; ++x) {
                     Items.removeItem(items[x].getWurmId());
                  }
               }
            } catch (Exception var8) {
               logger.log(
                  Level.INFO, "Error when removing inventory items while sleeping player id " + this.getWurmId() + " : " + var8.getMessage(), (Throwable)var8
               );
            }

            try {
               if (this.getBody() != null) {
                  Item[] items = this.getBody().getAllItems();

                  for(int x = 0; x < items.length; ++x) {
                     Items.removeItem(items[x].getWurmId());
                  }
               }
            } catch (Exception var7) {
               logger.log(
                  Level.INFO, "Error when removing body items while sleeping player id " + this.getWurmId() + " : " + var7.getMessage(), (Throwable)var7
               );
            }
         }
      } else {
         try {
            this.skills.delete();
         } catch (Exception var3) {
            logger.log(Level.INFO, "Error when deleting guest skills: " + var3.getMessage(), (Throwable)var3);
         }

         try {
            Item[] items = this.possessions.getInventory().getAllItems(true);

            for(int x = 0; x < items.length; ++x) {
               if (items[x].isUnique() && !items[x].isRoyal()) {
                  this.dropItem(items[x]);
               } else {
                  Items.decay(items[x].getWurmId(), items[x].getDbStrings());
               }
            }
         } catch (Exception var6) {
            logger.log(Level.INFO, "Error when decaying guest items: " + var6.getMessage(), (Throwable)var6);
         }

         try {
            Item[] items = this.getBody().getAllItems();

            for(int x = 0; x < items.length; ++x) {
               if (items[x].isUnique() && !items[x].isRoyal()) {
                  this.dropItem(items[x]);
               } else {
                  Items.decay(items[x].getWurmId(), items[x].getDbStrings());
               }
            }
         } catch (Exception var5) {
            logger.log(Level.INFO, "Error when decaying guest items: " + var5.getMessage(), (Throwable)var5);
         }
      }

      ItemBonus.clearBonuses(this.getWurmId());
   }

   @Override
   public void stopLeading() {
      if (this.followers != null) {
         Creature[] folls = this.followers.keySet().toArray(new Creature[this.followers.size()]);

         for(int x = 0; x < folls.length; ++x) {
            folls[x].setLeader(null);
         }

         this.followers.clear();
         this.followers = null;
      }
   }

   @Override
   public boolean isLoggedOut() {
      return this.loggedout;
   }

   public void logout() {
      if (!this.loggedout) {
         try {
            this.stopLeading();
            Vehicle lVehicle = Vehicles.getVehicleForId(this.getVehicle());
            if (lVehicle != null) {
               if (lVehicle.isCreature()) {
                  this.disembark(false);
               } else {
                  try {
                     Item item = Items.getItem(this.getVehicle());
                     if (!item.isBoat() || this.isChampion()) {
                        this.disembark(false);
                     }
                  } catch (NoSuchItemException var3) {
                  }
               }
            }

            this.clearLinks();
            this.disableLink();
            if (this.battle != null) {
               this.battle.removeCreature(this);
            }

            this.trimAttackers(true);
            this.sleep();
            if (this.possessions != null) {
               this.possessions.clearOwner();
            }

            this.destroyVisionArea();
            if (this.movementScheme.getDraggedItem() != null) {
               Items.stopDragging(this.movementScheme.getDraggedItem());
            }

            this.loggedout = true;
            this.actions.clear();
            this.communicator.resetTicker();
            this.communicator.player = null;
            this.communicator.resetConnection();
            if (this.getSpellEffects() != null) {
               this.getSpellEffects().sleep();
            }

            this.communicator = new CreatureCommunicator(this);
            Questions.removeQuestions(this);
            this.question = null;
            this.checkItemsWatched();
            if (this.getPet() != null && this.getPet().isAnimal() && !this.getPet().isReborn() && !this.getPet().isStayonline()) {
               this.getPet().goOffline = true;
            }

            this.getSaveFile().lastLogin = 0L;
            if (this.sparrers != null) {
               Iterator<Creature> it = this.sparrers.iterator();

               while(it.hasNext()) {
                  ((Player)it.next()).removeSparrer(this);
               }
            }

            this.sparrers = null;
            if (this.duellers != null) {
               Iterator<Creature> it = this.duellers.iterator();

               while(it.hasNext()) {
                  ((Player)it.next()).removeDuellist(this);
               }
            }

            this.getStatus().savePosition(this.getWurmId(), true, this.getStatus().getZoneId(), true);
            this.setTeam(null, false);
            this.duellers = null;
            logger.log(Level.INFO, "Logout complete for " + this);
            if (this.isUndead()) {
               IntraServerConnection.deletePlayer(this.getWurmId());
            }
         } catch (Exception var4) {
            logger.log(
               Level.WARNING, "Problem logging out player ID " + this.getWurmId() + ", name: " + this.getName() + " : " + var4.getMessage(), (Throwable)var4
            );
         }
      }
   }

   public void logoutIn(int seconds, String reason) {
      if (this.hasLink()) {
         this.disconnectReason = reason;
         if (this.secondsToLinkDeath < 0) {
            logger.log(Level.INFO, "Setting " + this.getName() + " to log off in " + seconds + " " + reason);
            this.secondsToLinkDeath = seconds;
            this.secondsToLogout = seconds + 1;
            this.communicator.setReady(false);
         }
      }
   }

   @Override
   public void endTrade() {
   }

   public void createSomeItems(float modifier, boolean reimburse) {
      if (!this.isUndead()) {
         try {
            if (Servers.localServer.testServer) {
               Item inventory = this.getInventory();
               float ql = 20.0F;
               Item c = createItem(274, 20.0F);
               inventory.insertItem(c);
               c.setAuxData((byte)1);
               c = createItem(274, 20.0F);
               inventory.insertItem(c);
               c.setAuxData((byte)1);
               c = createItem(279, 20.0F);
               inventory.insertItem(c);
               c.setAuxData((byte)1);
               c = createItem(277, 20.0F);
               inventory.insertItem(c);
               c.setAuxData((byte)1);
               c = createItem(277, 20.0F);
               inventory.insertItem(c);
               c.setAuxData((byte)1);
               c = createItem(278, 20.0F);
               inventory.insertItem(c);
               c.setAuxData((byte)1);
               c = createItem(278, 20.0F);
               inventory.insertItem(c);
               c.setAuxData((byte)1);
               c = createItem(275, 20.0F);
               inventory.insertItem(c);
               c.setAuxData((byte)1);
               c = createItem(276, 20.0F);
               inventory.insertItem(c);
               c = createItem(4, 20.0F);
               inventory.insertItem(c);
               int x = Server.rand.nextInt(3);
               if (x == 0) {
                  c = createItem(87, 50.0F);
               } else if (x == 1) {
                  c = createItem(21, 50.0F);
               } else if (x == 2) {
                  c = createItem(290, 50.0F);
               }

               c.setAuxData((byte)1);
               inventory.insertItem(c);
               if (Server.rand.nextInt(20) == 0) {
                  c.enchant((byte)(1 + Server.rand.nextInt(12)));
               }

               if (Server.rand.nextInt(20) == 0) {
                  ItemSpellEffects effs = new ItemSpellEffects(c.getWurmId());
                  SpellEffect eff = new SpellEffect(c.getWurmId(), (byte)(13 + Server.rand.nextInt(7)), (float)Server.rand.nextInt(90), 20000000);
                  effs.addSpellEffect(eff);
               }

               c = createItem(447, 20.0F);
               inventory.insertItem(c);
               if (this.getDeity() != null) {
                  int statnumber = 0;
                  switch(this.getDeity().number) {
                     case 1:
                        statnumber = 505;
                        break;
                     case 2:
                        statnumber = 507;
                        break;
                     case 3:
                        statnumber = 508;
                        break;
                     case 4:
                        statnumber = 506;
                  }

                  if (statnumber != 0) {
                     c = createItem(statnumber, 80.0F);
                     c.setMaterial((byte)7);
                     inventory.insertItem(c);
                  }
               }

               Item q = createItem(462, 20.0F);
               inventory.insertItem(q);

               for(int a = 0; a < 20; ++a) {
                  c = createItem(455, 20.0F);
                  q.insertItem(c, true);
               }

               q = createItem(462, 20.0F);
               inventory.insertItem(q);

               for(int a = 0; a < 20; ++a) {
                  c = createItem(456, 20.0F);
                  q.insertItem(c, true);
               }

               for(int a = 0; a < 10; ++a) {
                  c = createItem(457, 20.0F);
                  inventory.insertItem(c, true);
               }

               c = createItem(516, 20.0F);
               inventory.insertItem(c);
               c.setAuxData((byte)1);
               c = createItem(861, 20.0F);
               inventory.insertItem(c);
               c.setAuxData((byte)1);
            } else {
               Item inventory = this.getInventory();
               float ql = 30.0F;
               Item c = createItem(7, ql);
               inventory.insertItem(c);
               c.setAuxData((byte)1);
               c = createItem(84, ql);
               inventory.insertItem(c);
               c.setAuxData((byte)1);
               ql = 10.0F;
               c = createItem(8, ql);
               inventory.insertItem(c);
               c.setAuxData((byte)1);
               ql = 50.0F;
               c = createItem(143, ql);
               inventory.insertItem(c);
               c.setAuxData((byte)1);
               ql = 2.0F;
               c = createItem(77, ql);
               inventory.insertItem(c);
               c.setAuxData((byte)1);
               ql = 10.0F;
               c = createItem(1, ql);
               inventory.insertItem(c);
               c.setAuxData((byte)1);
               c = createItem(25, ql);
               inventory.insertItem(c);
               c.setAuxData((byte)1);
               c = createItem(24, ql);
               inventory.insertItem(c);
               c.setAuxData((byte)1);
               c = createItem(20, ql);
               inventory.insertItem(c);
               c.setAuxData((byte)1);
               c = createItem(27, ql);
               inventory.insertItem(c);
               c.setAuxData((byte)1);
               ql = Servers.localServer.challengeServer ? 41.0F : 20.0F;
               c = createItem(516, ql);
               inventory.insertItem(c);
               c.setAuxData((byte)1);
               ql = Servers.localServer.challengeServer ? 40.0F : 10.0F;
               c = createItem(319, ql);
               inventory.insertItem(c);
               c.setAuxData((byte)1);
               if (Servers.localServer.isChallengeServer()) {
                  ql = 40.0F;
                  c = createItem(274, ql);
                  inventory.insertItem(c);
                  c.setAuxData((byte)1);
                  c = createItem(274, ql);
                  inventory.insertItem(c);
                  c.setAuxData((byte)1);
                  c = createItem(279, ql);
                  inventory.insertItem(c);
                  c.setAuxData((byte)1);
                  c = createItem(277, ql);
                  inventory.insertItem(c);
                  c.setAuxData((byte)1);
                  c = createItem(277, ql);
                  inventory.insertItem(c);
                  c.setAuxData((byte)1);
                  c = createItem(278, ql);
                  inventory.insertItem(c);
                  c.setAuxData((byte)1);
                  c = createItem(278, ql);
                  inventory.insertItem(c);
                  c.setAuxData((byte)1);
                  c = createItem(275, ql);
                  inventory.insertItem(c);
                  c.setAuxData((byte)1);
                  c = createItem(276, ql);
                  inventory.insertItem(c);
                  c.setAuxData((byte)1);
                  int x = Server.rand.nextInt(3);
                  if (x == 0) {
                     c = createItem(87, 50.0F);
                  } else if (x == 1) {
                     c = createItem(21, 50.0F);
                  } else if (x == 2) {
                     c = createItem(290, 50.0F);
                  }

                  c.setAuxData((byte)1);
                  inventory.insertItem(c);
               } else {
                  float var19 = 50.0F;
                  c = createItem(21, var19);
                  inventory.insertItem(c);
                  c.setAuxData((byte)1);
                  ql = 30.0F;
                  c = createItem(105, ql);
                  inventory.insertItem(c);
                  c.setAuxData((byte)1);
                  c = createItem(105, ql);
                  inventory.insertItem(c);
                  c.setAuxData((byte)1);
                  c = createItem(107, ql);
                  inventory.insertItem(c);
                  c.setAuxData((byte)1);
                  c = createItem(106, ql);
                  inventory.insertItem(c);
                  c.setAuxData((byte)1);
                  c = createItem(106, ql);
                  inventory.insertItem(c);
                  c.setAuxData((byte)1);
                  c = createItem(103, ql);
                  inventory.insertItem(c);
                  c.setAuxData((byte)1);
                  c = createItem(103, ql);
                  inventory.insertItem(c);
                  c.setAuxData((byte)1);
                  c = createItem(108, ql);
                  inventory.insertItem(c);
                  c.setAuxData((byte)1);
                  c = createItem(104, ql);
                  inventory.insertItem(c);
                  c.setAuxData((byte)1);
               }

               c = createItem(480, ql);
               c.setAuxData((byte)1);
               inventory.insertItem(c);
               c = createItem(861, ql);
               c.setAuxData((byte)1);
               inventory.insertItem(c);
               c = createItem(862, ql);
               c.setAuxData((byte)1);
               inventory.insertItem(c);
               c = createItem(781, 20.0F);
               inventory.insertItem(c);
               this.wearItems();
            }
         } catch (Exception var9) {
            logger.log(Level.INFO, "Failed to create some items for the test server.", (Throwable)var9);
         }
      }
   }

   @Override
   public void setSecondsToLogout(int seconds) {
      if (this.hasLink() && seconds > this.secondsToLogout) {
         this.secondsToLogout = seconds;
      }
   }

   public void checkPaymentUpdate() {
   }

   private void pollPayment() {
      long tl = this.saveFile.getPaymentExpire();
      if (tl > System.currentTimeMillis()) {
         tl -= System.currentTimeMillis();
         if (tl < 3600000L && System.currentTimeMillis() - this.lastSentWarning > 600000L) {
            this.lastSentWarning = System.currentTimeMillis();
            this.getCommunicator().sendAlertServerMessage("Your premium time expires within the hour.", (byte)1);
         } else if (tl < 86400000L) {
            if (System.currentTimeMillis() - this.lastSentWarning > 3600000L) {
               this.lastSentWarning = System.currentTimeMillis();
               this.getCommunicator().sendAlertServerMessage("Your premium time expires today.", (byte)1);
            }
         } else if (tl < 604800000L && System.currentTimeMillis() - this.lastSentWarning > 86400000L) {
            this.lastSentWarning = System.currentTimeMillis();
            this.getCommunicator().sendAlertServerMessage("Your premium time expires this week.", (byte)1);
         }
      }
   }

   public boolean pollDead() {
      if (this.isDead()) {
         if (this.secondsToLogout < -1) {
            return true;
         } else {
            if (!this.hasLink() && this.secondsToLogout > -1) {
               --this.secondsToLogout;
            }

            if (this.secondsToLinkDeath > 0) {
               --this.secondsToLinkDeath;
               if (this.secondsToLinkDeath == 2 && !this.isTransferring()) {
                  this.communicator.sendShutDown(this.disconnectReason, false);
               }

               if (this.secondsToLinkDeath == 0) {
                  this.setFrozen(false);
                  this.setLink(false);
               }
            }

            return this.loggedout;
         }
      } else {
         return false;
      }
   }

   public void receivedCmd(int cmd) {
      if (!this.gotHash && this.secondsPlayedSinceLinkloss > 1200 && this.hasLink()) {
         this.gotHash = true;
      }
   }

   private void pollAlcohol() {
      if (this.secondsPlayed % 20.0F == 0.0F) {
         if (this.getAlcohol() > 0.0F) {
            this.setAlcohol(this.getAlcohol() - 1.0F);
         } else if (this.getAlcoholAddiction() > 0L) {
            this.saveFile.setAlcoholTime(this.getAlcoholAddiction() - 1L);
            if (this.getAlcoholAddiction() > 1000L && this.getAlcoholAddiction() % 100L == 0L) {
               try {
                  this.getCommunicator().sendNormalServerMessage("You tremble and shake from withdrawal.");
                  this.getCurrentAction().setFailSecond(this.getCurrentAction().getCounterAsFloat() + 1.0F);
                  this.getCurrentAction().setPower(-40.0F);
                  this.achievement(295);
               } catch (NoSuchActionException var2) {
               }
            }
         }
      }
   }

   private void nutcase(Cultist cultist) {
      int result = Server.rand.nextInt(cultist.getLevel() + 10);
      String toBroadCast = " twitches nervously.";
      switch(result) {
         case 1:
            toBroadCast = " suddenly coughs and looks nervously around.";
            break;
         case 2:
            toBroadCast = " gives you a scared look.";
            break;
         case 3:
            toBroadCast = " stares at you with black eyes.";
            break;
         case 4:
            toBroadCast = " shows " + this.getHisHerItsString() + " teeth and snarls at you.";
            break;
         case 5:
            toBroadCast = " scorns someone invisible.";
            break;
         case 6:
            toBroadCast = " curses loudly.";
            break;
         case 7:
            toBroadCast = " spits and froths disgustingly.";
            break;
         case 8:
            toBroadCast = " scratches " + this.getHisHerItsString() + " skin wildly for a few seconds.";
            break;
         case 9:
            toBroadCast = " looks at you with disgust.";
            break;
         case 10:
            toBroadCast = " suddenly whimpers.";
            break;
         case 11:
            toBroadCast = " makes some erratic twitching moves.";
            break;
         case 12:
            toBroadCast = " stares at the sky.";
            break;
         case 13:
            toBroadCast = " stares at " + this.getHisHerItsString() + " palm.";
            break;
         case 14:
            toBroadCast = " drools a bit.";
            break;
         case 15:
            toBroadCast = " wipes " + this.getHisHerItsString() + " nose clean from some gooey snot.";
            break;
         case 16:
            toBroadCast = " murmurs something about 'unfair.. danger...'.";
            break;
         case 17:
            toBroadCast = " pats " + this.getHimHerItString() + "self on the back.";
            break;
         case 18:
            toBroadCast = " suddenly has a haunted look in the eyes.";
            break;
         case 19:
            toBroadCast = " screams out loud!";
            break;
         case 20:
            toBroadCast = " looks for something on the ground.";
            break;
         case 21:
            toBroadCast = " wipes some tears from " + this.getHisHerItsString() + " eyes.";
            break;
         default:
            toBroadCast = " twitches nervously.";
      }

      this.getCommunicator().sendNormalServerMessage("You feel strange and out of time.");
      Server.getInstance().broadCastAction(this.getName() + toBroadCast, this, 5);
   }

   private void sendNewPhantasm(boolean insanity) {
      if (this.phantasms == null) {
         this.phantasms = new HashSet<>();
      }

      float px = this.getPosX() - 5.0F + Server.rand.nextFloat() * 10.0F;
      float py = this.getPosY() - 5.0F + Server.rand.nextFloat() * 10.0F;
      if (!this.isOnSurface()) {
         px = this.getPosX() - 1.0F + Server.rand.nextFloat() * 2.0F;
         py = this.getPosY() - 1.0F + Server.rand.nextFloat() * 2.0F;
      }

      long newCid = calculatePhantasmId((int)px >> 2, (int)py >> 2, this.getLayer());
      if (!this.phantasms.contains(newCid)) {
         int templateId = 11;
         int rand = Server.rand.nextInt(10);
         if (rand == 0) {
            templateId = 12;
         } else if (rand == 1) {
            templateId = 57;
         } else if (rand == 2) {
            templateId = 18;
         } else if (rand == 3) {
            templateId = 19;
         } else if (rand == 4) {
            templateId = 23;
         } else if (rand == 5) {
            templateId = 58;
         } else if (rand == 6) {
            templateId = 35;
         }

         try {
            CreatureTemplate ct = CreatureTemplateFactory.getInstance().getTemplate(templateId);
            String ctname = ct.getName();
            if (insanity) {
               int nnam = Server.rand.nextInt(10);
               switch(nnam) {
                  case 0:
                     ctname = "Terror";
                     break;
                  case 1:
                     ctname = "Pus";
                     break;
                  case 2:
                     ctname = "Rotten Blood";
                     break;
                  case 3:
                     ctname = "Silent Death";
                     break;
                  case 4:
                     ctname = "Sickness";
                     break;
                  case 5:
                     ctname = "Watcher";
                     break;
                  case 6:
                     ctname = "Scorn";
                     break;
                  case 7:
                     ctname = "Omen";
                     break;
                  case 8:
                     ctname = "Ratatosk";
                     break;
                  case 9:
                     ctname = "Pain";
                     break;
                  default:
                     ctname = "Doom";
               }
            }

            try {
               float pz = Zones.calculateHeight(px, py, this.isOnSurface());
               double lNewrot = Math.atan2((double)(this.getStatus().getPositionY() - py), (double)(this.getStatus().getPositionX() - px));
               this.setRotation((float)(lNewrot * (180.0 / Math.PI)) + 90.0F);
               this.getCommunicator()
                  .sendNewCreature(
                     newCid,
                     ctname,
                     ct.getModelName(),
                     px,
                     py,
                     pz,
                     this.getBridgeId(),
                     (float)lNewrot,
                     (byte)this.getLayer(),
                     this.getFloorLevel() <= 0,
                     false,
                     true,
                     (byte)-1,
                     this.getFace(),
                     (byte)0,
                     false,
                     false,
                     (byte)0
                  );
               this.getCommunicator().setCreatureDamage(newCid, 100.0F);
               this.phantasms.add(newCid);
            } catch (NoSuchZoneException var13) {
            }
         } catch (NoSuchCreatureTemplateException var14) {
         }
      }
   }

   public static long calculatePhantasmId(int tileX, int tileY, int layer) {
      return (long)(((tileX & 65535) << 40) + ((tileY & 65535) << 16) + (layer & 65535));
   }

   @Override
   public boolean shouldStopTrading(boolean firstCall) {
      if (firstCall) {
         this.getTrade().end(this, false);
      }

      return System.currentTimeMillis() - this.startedTrading > 60000L;
   }

   @Override
   public void startTrading() {
      this.startedTrading = System.currentTimeMillis();
   }

   private final void checkSendLinkStatus() {
      if (this.links != null) {
         int numlinks = this.getLinks().length;
         if (this.lastLinksSent != numlinks) {
            if (numlinks == 0) {
               this.getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.LINKS);
            } else {
               this.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.LINKS, 100000, (float)numlinks);
            }

            this.lastLinksSent = (byte)numlinks;
         }
      }
   }

   @Override
   public byte getRarity() {
      byte rarity = 0;
      if (Servers.isThisATestServer() && this.nextActionRarity != 0) {
         rarity = this.nextActionRarity;
         this.nextActionRarity = 0;
      } else if (this.windowOfCreation > 0) {
         this.windowOfCreation = 0;
         float faintChance = 1.0F;
         int supPremModifier = 0;
         if (this.isPaying()) {
            faintChance = 1.03F;
            supPremModifier = 3;
         }

         if (Server.rand.nextFloat() * 10000.0F <= faintChance) {
            rarity = 3;
         } else if (Server.rand.nextInt(100) <= 0 + supPremModifier) {
            rarity = 2;
         } else if (Server.rand.nextBoolean()) {
            rarity = 1;
         }
      }

      return rarity;
   }

   public boolean shouldGiveAffinity(int currAffinityCount, boolean isCharacteristic) {
      if (!Features.Feature.AFFINITY_GAINS.isEnabled()) {
         return false;
      } else {
         if (this.isPaying() && this.windowOfAffinity > 0) {
            float chance = 1.0F / ((float)newAffinityChance * (isCharacteristic ? 3.0F : 1.0F) * (float)(currAffinityCount + 1));
            if (Server.rand.nextFloat() < chance) {
               this.windowOfAffinity = 0;
               return true;
            }
         }

         return false;
      }
   }

   public void resetInactivity(boolean sleepBonus) {
      if (sleepBonus && this.isSBIdleOffEnabled() && !this.getSaveFile().frozenSleep) {
         this.lastSleepBonusActivity = System.currentTimeMillis();
      }

      this.lastActivity = System.currentTimeMillis();
   }

   public boolean isSBIdleOffEnabled() {
      return !this.hasFlag(43);
   }

   public long getSleepBonusInactivity() {
      return System.currentTimeMillis() - this.lastSleepBonusActivity;
   }

   public long getInactivity() {
      return System.currentTimeMillis() - this.lastActivity;
   }

   public boolean isBlockingPvP() {
      if (Servers.localServer.isChallengeOrEpicServer() && !Server.getInstance().isPS() && !Servers.isThisATestServer()) {
         return false;
      } else {
         return !this.hasFlag(44);
      }
   }

   @Override
   public boolean poll() {
      if (!this.isFullyLoaded()) {
         this.getCommunicator().setAvailableMoves(24);
         return false;
      } else {
         if (this.pushCounter > 0) {
            --this.pushCounter;
         }

         if (this.lastSentQuestion-- < 0) {
            this.lastSentQuestion = 0;
         }

         if (this.guardSecondsLeft > 0) {
            --this.guardSecondsLeft;
         }

         if (this.breedCounter > 0) {
            --this.breedCounter;
         }

         if (this.raritySeconds > 0) {
            --this.raritySeconds;
            if (this.raritySeconds <= 0) {
               this.setRarityShader((byte)0);
            }
         }

         if (this.conchticker > 0) {
            --this.conchticker;
            if (this.conchticker == 0) {
               SimplePopup toSend = new SimplePopup(
                  this, "Something in your pocket", "You suddenly notice a conch in your pocket. Maybe you should examine it?"
               );
               toSend.sendQuestion();
            }
         }

         if (this.farwalkerSeconds > 0) {
            --this.farwalkerSeconds;
            if (this.farwalkerSeconds == 0) {
               this.getMovementScheme().setFarwalkerMoveMod(false);
               this.getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.FARWALKER);
               this.getStatus().sendStateString();
            }
         }

         this.decreaseOpportunityCounter();
         if (this.CRBonusCounter > 0) {
            --this.CRBonusCounter;
            if (this.CRBonusCounter == 0) {
               if (this.CRBonus > 0) {
                  --this.CRBonus;
                  if (this.CRBonus > 0) {
                     this.CRBonusCounter = 3;
                  } else {
                     this.getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.CR_BONUS);
                  }
               }

               this.getStatus().sendStateString();
            }
         }

         if (this.removePvPDeathTimer > 0L) {
            --this.removePvPDeathTimer;
            if (this.removePvPDeathTimer == 0L) {
               Players.getInstance().removePvPDeath(this.getWurmId());
               if (Players.getInstance().hasPvpDeaths(this.getWurmId())) {
                  this.removePvPDeathTimer = 10800000L;
               }
            }
         }

         if (this.loggedout) {
            --this.secondsToLinkDeath;
            if (this.secondsToLinkDeath <= 0) {
               this.setLink(false);
            }

            --this.secondsToLogout;
            this.loggedout = false;
            this.logout();
            if (this.waitingForFriendCount >= 0) {
               this.sendFriendTimedOut();
            }

            return true;
         } else {
            if (++this.lastSentServerTime >= 60) {
               this.pollGlobalEffects();
               this.saveFile.checkIfResetSellEarning();
               this.getCommunicator().sendServerTime();
               this.lastSentServerTime = 0;
               if (this.getCultist() != null && this.getCultist().getPath() == 4 && this.getCultist().getLevel() > 5 && Server.rand.nextInt(2000) == 0) {
                  this.sendNewPhantasm(true);
               }
            }

            if (this.musicPlayer != null) {
               this.musicPlayer.tickSecond();
            }

            if (Servers.localServer.isChallengeServer()) {
               this.clearChallengeScores();
            }

            this.movementScheme.decreaseFreeMoveCounter();
            if (this.windowOfCreation > 0) {
               --this.windowOfCreation;
            } else if (Server.rand.nextInt(3600) == 0) {
               this.windowOfCreation = 20;
               if (this.getCitizenVillage() != null) {
                  this.windowOfCreation += (int)Math.min(10.0F, this.getCitizenVillage().getFaithCreateValue());
               }
            }

            if (this.windowOfAffinity > 0) {
               --this.windowOfAffinity;
            } else if (Server.rand.nextInt(7200) == 0) {
               this.windowOfAffinity = 15;
            }

            if (this.transferCounter > 0 && --this.transferCounter <= 0 && this.hasLink()) {
               this.getCommunicator().sendAlertServerMessage("You may now move again.");
               this.getCommunicator().setReady(true);
               this.getMovementScheme().resumeSpeedModifier();
            }

            this.checkSendLinkStatus();
            if (this.getVisionArea() != null && this.getVisionArea().isInitialized()) {
               if (this.getVisionArea().getSurface() != null) {
                  this.getVisionArea().getSurface().pollVisibleVehicles();
               }

               if (this.getVisionArea().getUnderGround() != null) {
                  this.getVisionArea().getUnderGround().pollVisibleVehicles();
               }
            }

            if (this.justCombined) {
               this.justCombined = false;
               this.getMovementScheme().resumeSpeedModifier();
            }

            if (this.enemyPresenceCounter > 0) {
               if (Servers.isThisAPvpServer()) {
                  ++this.enemyPresenceCounter;
               }

               if (this.enemyPresenceCounter == minEnemyPresence) {
                  this.getCommunicator()
                     .sendAlertServerMessage(
                        "Something is wrong. An irritating feeling comes over you and you cannot focus. Your normal skillgains suffer.", (byte)4
                     );
                  this.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.ENEMY, 100000, 50.0F);
               }

               if (this.enemyPresenceCounter == 1200) {
                  this.getCommunicator()
                     .sendAlertServerMessage(
                        "You now feel greatly disturbed by an enemy presence. While your normal skills still suffer, your aggressive actions are probably more effective.",
                        (byte)4
                     );
                  this.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.ENEMY, 100000, 100.0F);
               }
            }

            if (this.secondsPlayed % 60.0F == 0.0F && this.isFighting()) {
               this.resetInactivity(true);
            }

            if (!this.getSaveFile().frozenSleep) {
               if (this.hasSleepBonus()) {
                  if (this.sendSleepCounter++ > 30) {
                     this.getCommunicator().sendSleepInfo();
                     this.sendSleepCounter = 0;
                  }
               } else {
                  this.getSaveFile().frozenSleep = true;
                  this.getCommunicator().sendNormalServerMessage("You feel the last of your sleep bonus run out.", (byte)2);
                  this.getCommunicator().sendSleepInfo();
               }

               if (this.isSBIdleOffEnabled()) {
                  if (this.getSleepBonusInactivity() >= 600000L) {
                     this.getCommunicator().sendNormalServerMessage("Auto-freezing sleep bonus after " + Server.getTimeFor(600000L) + " of inactivity.");
                     this.getSaveFile().frozenSleep = true;
                     this.getCommunicator().sendNormalServerMessage("You refrain from using your sleep bonus, but may turn it back on immediately.");
                     this.getCommunicator().sendSleepInfo();
                  }

                  if (this.getSleepBonusInactivity() >= 299000L && this.getSleepBonusInactivity() < 300000L) {
                     this.getCommunicator()
                        .sendAlertServerMessage(
                           "You have been inactive and your sleep bonus will auto-freeze in "
                              + Server.getTimeFor(600000L - this.getSleepBonusInactivity())
                              + "."
                        );
                  }

                  if (this.getSleepBonusInactivity() >= 539000L && this.getSleepBonusInactivity() < 540000L) {
                     this.getCommunicator()
                        .sendAlertServerMessage(
                           "You have been inactive and your sleep bonus will auto-freeze in "
                              + Server.getTimeFor(600000L - this.getSleepBonusInactivity())
                              + "."
                        );
                  }
               }
            }

            if (this.opponentCounter > 0 && this.opponent == null && --this.opponentCounter == 0) {
               this.lastOpponent = null;
               this.getCombatHandler().setCurrentStance(-1, (byte)15);
               this.combatRound = 0;
            }

            if (!Servers.localServer.testServer) {
               this.pollStealAttack();
            } else {
               this.maySteal = true;
               this.mayAttack = true;
            }

            this.status.pollDetectInvis();
            this.stuckCounter = Math.max(0, --this.stuckCounter);
            boolean remove = false;
            if (this.getSpellEffects() != null) {
               this.getSpellEffects().poll();
            }

            this.attackTarget();
            if (!this.isFighting() && this.fightlevel > 0 && Server.getSecondsUptime() % 20 == 0) {
               this.fightlevel = (byte)Math.max(0, this.fightlevel - 1);
               this.getCommunicator().sendFocusLevel(this.getWurmId());
            }

            if (this.loggedout) {
               return true;
            } else {
               if (this.isKing()) {
                  if (this.secondsPlayed == 60.0F && !this.sentChallenge) {
                     King king = King.getKing(this.getKingdomId());
                     if (king != null && king.hasBeenChallenged() && king.getChallengeAcceptedDate() <= 0L) {
                        this.getCommunicator()
                           .sendSafeServerMessage(
                              "In two minutes you will automatically be presented with the Royal Challenge popup. You may do this manually instead by using the command /challenge"
                           );
                     }
                  }

                  if (Server.rand.nextInt(10) == 0) {
                     King king = King.getKing(this.getKingdomId());
                     if (king != null) {
                        if (king.getChallengeAcceptedDate() > 0L) {
                           if (System.currentTimeMillis() > king.getChallengeAcceptedDate()) {
                              if (this.isInOwnDuelRing()) {
                                 if (Servers.localServer.testServer) {
                                    long secs = (System.currentTimeMillis() - king.getChallengeAcceptedDate()) / 1000L;
                                    this.getCommunicator().sendAlertServerMessage(secs + " passed of 300 on this test server.");
                                    if (System.currentTimeMillis() - king.getChallengeAcceptedDate() > 300000L) {
                                       king.passedChallenge();
                                    }
                                 } else if (System.currentTimeMillis() - king.getChallengeAcceptedDate() > 1800000L) {
                                    king.passedChallenge();
                                 }

                                 if (Server.rand.nextInt(10) == 0) {
                                    this.getCommunicator().sendAlertServerMessage("Unseen eyes watch you.");
                                 }
                              } else {
                                 king.setFailedChallenge();
                                 this.getCommunicator().sendAlertServerMessage("You have failed the challenge! You are now at the mercy of your subjects.");
                              }
                           }
                        } else if (king.hasBeenChallenged()) {
                           if (!this.sentChallenge && this.secondsPlayed > 180.0F) {
                              MethodsCreatures.sendChallengeKingQuestion(this);
                           }
                        } else {
                           this.sentChallenge = false;
                        }
                     }
                  }
               }

               if (Server.rand.nextInt(this.isPriest() ? '負' : 72000) == 0) {
                  float mod = 1.0F;
                  if (this.getFaith() != 0.0F) {
                     mod = (100.0F - this.getFaith() / 2.0F) / 100.0F;
                  }

                  if (Servers.localServer.PVPSERVER) {
                     if (this.getAlignment() < 0.0F) {
                        EndGameItem altar = EndGameItems.getEvilAltar();
                        if (altar != null && this.isWithinDistanceTo(altar.getItem().getPosX(), altar.getItem().getPosY(), altar.getItem().getPosZ(), 200.0F)) {
                           mod /= 2.0F;
                        }
                     } else if (this.getAlignment() > 0.0F) {
                        EndGameItem altar = EndGameItems.getGoodAltar();
                        if (altar != null && this.isWithinDistanceTo(altar.getItem().getPosX(), altar.getItem().getPosY(), altar.getItem().getPosZ(), 200.0F)) {
                           mod /= 2.0F;
                        }
                     }
                  }

                  mod = Math.min(mod, 1.0F);
                  if (!(this.getAlignment() < -2.0F) && (!(this.getAlignment() < 0.0F) || this.getFaith() != 0.0F)) {
                     if (this.getAlignment() > 2.0F || this.getAlignment() > 0.0F && this.getFaith() == 0.0F) {
                        try {
                           if (MethodsReligion.mayReceiveAlignment(this)) {
                              this.setAlignment(this.getAlignment() - 1.0F * mod);
                           }
                        } catch (IOException var8) {
                           logger.log(Level.WARNING, this.getName(), (Throwable)var8);
                        }
                     }
                  } else {
                     try {
                        if (MethodsReligion.mayReceiveAlignment(this)) {
                           this.setAlignment(this.getAlignment() + 1.0F * mod);
                        }
                     } catch (IOException var9) {
                        logger.log(Level.WARNING, this.getName(), (Throwable)var9);
                     }
                  }

                  if (this.citizenVillage != null) {
                     this.citizenVillage.setLogin();
                  }
               }

               this.pollPayment();
               if (this.secondsToLogout > -1) {
                  --this.secondsToLogout;
               }

               if (this.myceliumHealCounter > -1) {
                  --this.myceliumHealCounter;
               }

               if (this.getCultist() != null) {
                  this.getCultist().poll();
               }

               --this.secondsToLinkDeath;
               if (this.secondsToLinkDeath == 2 && !this.isTransferring()) {
                  this.communicator.sendShutDown(this.disconnectReason, false);
               }

               if (this.secondsToLinkDeath == 0) {
                  this.setFrozen(false);
                  this.setLink(false);
               }

               if (this.affcounter > 0 && --this.affcounter <= 0) {
                  this.getCommunicator().sendNormalServerMessage(this.affstring);
                  this.affstring = null;
               }

               if (this.battle != null && System.currentTimeMillis() - this.battle.getEndTime() > 240000L) {
                  this.battle.removeCreature(this);
               }

               if (!this.isDead()) {
                  this.pollItems();
                  this.saveFile.pollResistances(this.communicator);
                  this.checkLantern();
                  this.checkBreedCounter();
                  this.spreadCrownInfluence();
                  if (this.crownInfluence > 0) {
                     this.setCrownInfluence(this.crownInfluence - 1);
                  }
               }

               if (this.receivedLinkloss != 0L) {
                  this.sentClimbing = 0L;
                  this.sentWind = 0L;
                  if (this.secondsToLogout <= 0
                     && (this.getCommunicator().getCurrentmove() == null || this.getCommunicator().getCurrentmove().getNext() == null)) {
                     if (this.battle != null && this.opponent == null) {
                        this.battle.removeCreature(this);
                     }

                     if (this.battle == null && this.opponent == null) {
                        Server.getInstance().addCreatureToRemove(this);
                        remove = true;
                     } else if (this.getSaveFile().currentServer != Servers.localServer.id) {
                        Server.getInstance().addCreatureToRemove(this);
                        remove = true;
                     }
                  } else if (this.loggedout) {
                     remove = true;
                  }

                  if (this.communicator != null) {
                     this.communicator.setAvailableMoves(24);
                  }
               } else {
                  ++this.secondsPlayed;
                  ++this.secondsPlayedSinceLinkloss;
                  if (this.sentClimbing != 0L) {
                     if (this.getVisionArea() != null && this.getVisionArea().isInitialized() && this.isFullyLoaded() && this.transferCounter == 0) {
                        long now = System.currentTimeMillis();
                        if (now - this.sentClimbing > 30000L) {
                           this.getCommunicator().sendAlertServerMessage("You failed to respond in time. Disconnecting.");
                           this.logoutIn(5, "Game client communication was disrupted and you were disconnected.");
                           this.sentClimbing = 0L;
                        }
                     } else {
                        this.sentClimbing = System.currentTimeMillis();
                     }
                  }

                  if (this.sentWind != 0L) {
                     if (this.getVisionArea() != null && this.getVisionArea().isInitialized() && this.isFullyLoaded() && this.transferCounter == 0) {
                        long now = System.currentTimeMillis();
                        if (now - this.sentWind > 120000L) {
                           this.sentWind = 0L;
                        }
                     } else {
                        this.sentWind = System.currentTimeMillis();
                     }
                  }

                  if (this.sentMountSpeed != 0L) {
                     if (this.getVisionArea() != null && this.getVisionArea().isInitialized() && this.isFullyLoaded() && this.transferCounter == 0) {
                        long now = System.currentTimeMillis();
                        if (now - this.sentMountSpeed > 120000L) {
                           this.sentMountSpeed = 0L;
                        }
                     } else {
                        this.sentMountSpeed = System.currentTimeMillis();
                     }
                  }

                  if (this.communicator != null) {
                     if (this.getVisionArea() != null && this.getVisionArea().getSurface() != null) {
                        this.getVisionArea().getSurface().moveAllCreatures();
                     }

                     if (this.getVisionArea() != null && this.getVisionArea().getUnderGround() != null) {
                        this.getVisionArea().getUnderGround().moveAllCreatures();
                     }

                     if (this.secondsPlayed % 10.0F == 0.0F) {
                        if (this.teleports > 50) {
                           this.logoutIn(5, "Teleport loop");
                        }

                        this.teleports = 0;
                     }

                     this.communicator.tickSecond();
                  }

                  if (this.secondsPlayed % 500.0F == 0.0F && this.isChampion() && System.currentTimeMillis() - this.getChampTimeStamp() > 14515200000L) {
                     this.getCommunicator().sendSafeServerMessage("Your time as a Champion of " + this.getDeity().name + " has ended. Glory to you!", (byte)2);
                     Server.getInstance()
                        .broadCastSafe(
                           this.getDeity().name
                              + " has decided to let "
                              + this.getName()
                              + " step down as Champion. Glorious be "
                              + this.getHeSheItString()
                              + " who lives forever in the Eternal Records!"
                        );
                     this.revertChamp();
                  }
               }

               this.pollAlcohol();
               if (this.saveFile.checkFatigue()) {
                  this.communicator.sendSafeServerMessage("You feel rested.");
               }

               this.checkItemsWatched();
               if (this.isTeleporting() && !this.isWithinTeleportTime()) {
                  Players.getInstance().logoutPlayer(this);
               }

               if (this.getBody() != null) {
                  this.getBody().poll();
               } else {
                  logger.log(Level.WARNING, this.getName() + "'s body is null.");
               }

               if (this.isClimbing() || this.isTeleporting() || this.getMovementScheme().isIntraTeleporting() || this.getMovementScheme().isKeyPressed()) {
                  this.getStatus().setNormalRegen(false);
               } else if (this.vehicle != -10L) {
                  if (this.currentTile != null) {
                     boolean noStam = false;
                     short[] steepness = Creature.getTileSteepness(this.currentTile.tilex, this.currentTile.tiley, this.isOnSurface());
                     Vehicle vehic = Vehicles.getVehicleForId(this.vehicle);
                     if (vehic != null) {
                        if (steepness[0] > -13 && steepness[1] > 20) {
                           noStam = true;
                        }

                        if (!vehic.creature) {
                           try {
                              Item vehicleObj = Items.getItem(this.vehicle);
                              if (vehicleObj.isBoat()) {
                                 noStam = false;
                              }
                           } catch (NoSuchItemException var7) {
                              logger.log(Level.INFO, var7.getMessage(), (Throwable)var7);
                           }
                        } else if (steepness[0] < -13) {
                           noStam = true;
                        }
                     }

                     if (noStam) {
                        this.getStatus().setNormalRegen(false);
                     }
                  } else {
                     this.getStatus().setNormalRegen(false);
                  }
               } else if ((double)(this.getPositionZ() + this.getAltOffZ()) < -1.3) {
                  this.getStatus().setNormalRegen(false);
               }

               this.pollStamina();
               this.getStatus().pollFat();
               if (this.damageCounter > 0) {
                  --this.damageCounter;
                  if (this.damageCounter <= 0) {
                     this.removeWoundMod();
                     this.getStatus().sendStateString();
                  }
               }

               if (this.webArmourModTime > 0.0F && this.webArmourModTime-- <= 1.0F) {
                  this.webArmourModTime = 0.0F;
                  if (this.getMovementScheme().setWebArmourMod(false, 0.0F)) {
                     this.getMovementScheme().setWebArmourMod(false, 0.0F);
                  }

                  if (!this.isPaying() && this.getSkills() != null) {
                     this.getSkills().paying = false;
                  }
               }

               if (this.secondsPlayed % 10.0F == 0.0F) {
                  if (this.hasLoveEffect) {
                     if (this.getCultist() == null || this.getCultist().mayStartLoveEffect()) {
                        this.hasLoveEffect = false;
                        this.refreshAttitudes();
                        this.getCommunicator().sendNormalServerMessage("The stream of love dissipates.");
                     }
                  } else if (this.getCultist() != null && this.getCultist().hasLoveEffect()) {
                     this.hasLoveEffect = true;
                  }

                  this.saveFile.pollReputation(System.currentTimeMillis());
                  this.checkVehicleSpeeds();
                  if (Server.rand.nextInt(6) == 0) {
                     Cultist cultist = this.getCultist();
                     if (cultist != null && cultist.getPath() == 4 && Server.rand.nextInt(40 - Math.min(19, cultist.getLevel())) == 0) {
                        this.nutcase(cultist);
                     }
                  }
               }

               if (this.secondsPlayed % 5.0F == 0.0F) {
                  this.pollFavor();
                  if (this.favorGainSecondsLeft > 0) {
                     this.favorGainSecondsLeft -= 5;
                  }
               }

               if (this.secondsPlayed % 60.0F == 0.0F) {
                  this.checkPaymentUpdate();
                  if (this.getPower() < 2) {
                     if (this.secondsPlayed == 540.0F) {
                        if (this.getCommunicator().isInvulnerable()) {
                           this.getCommunicator()
                              .sendAlertServerMessage("You will be logged off in one minute since you are invulnerable. Move around a little to prevent this.");
                        }
                     } else if (this.secondsPlayed == 600.0F && this.getCommunicator().isInvulnerable()) {
                        this.getCommunicator().sendAlertServerMessage("You have been idle for too long and will be logged off.");
                        logger.log(
                           Level.INFO, "Logging off " + this.getName() + " since " + this.getHeSheItString() + " has been invulnerable for ten minutes."
                        );
                        this.logoutIn(10, "You have been idle for too long.");
                        this.achievement(82);
                     }
                  }

                  try {
                     int newActs = Math.max(2, (int)(this.skills.getSkill(100).getKnowledge(0.0) / 10.0));
                     if (newActs != this.maxNumActions) {
                        this.getCommunicator().sendNormalServerMessage("You may now queue " + newActs + " actions.");
                     }

                     this.maxNumActions = newActs;
                  } catch (NoSuchSkillException var6) {
                     this.skills.learn(100, 20.0F);
                     this.maxNumActions = 2;
                  }
               }

               if (this.moneySendCounter++ > 59) {
                  this.moneySendCounter = 0;
                  if (this.saveFile.getMoneyToSend() > 0L) {
                     LoginServerWebConnection lsw = new LoginServerWebConnection();
                     if (lsw.addMoney(this, this.getName(), this.saveFile.getMoneyToSend(), "Sold items")) {
                        this.getCommunicator().sendSafeServerMessage("You receive " + this.saveFile.getMoneyToSend() + " iron coins.");
                        this.saveFile.resetMoneyToSend();
                     } else {
                        this.getCommunicator()
                           .sendAlertServerMessage(
                              "We failed to contact the ingame bank. You may not receive the "
                                 + this.saveFile.getMoneyToSend()
                                 + " iron coins you have sold items for."
                           );
                     }
                  }
               }

               this.sendItemsTaken();
               this.sendItemsDropped();
               this.trimAttackers(false);
               this.numattackerslast = this.numattackers;
               this.numattackers = 0;
               this.hasAddedToAttack = false;
               AffinitiesTimed.poll(this);
               if (!remove) {
                  if (this.secondsPlayed % 60.0F == 0.0F) {
                     Set<WurmMail> waitingMail = WurmMail.getWaitingMailFor(this.getWurmId());
                     if (this.mailItemsWaiting != waitingMail.size()) {
                        this.mailItemsWaiting = waitingMail.size();
                        if (!waitingMail.isEmpty()) {
                           this.getCommunicator()
                              .sendServerMessage(
                                 "You sense imps whispering your name and saying you have " + waitingMail.size() + " mail waiting to be picked up.",
                                 255,
                                 200,
                                 20,
                                 (byte)2
                              );
                        }
                     }

                     Delivery[] waitingDeliveries = Delivery.getWaitingDeliveries(this.getWurmId());
                     if (this.deliveriesWaiting != waitingDeliveries.length) {
                        this.deliveriesWaiting = waitingDeliveries.length;
                        if (this.deliveriesWaiting > 0) {
                           this.getCommunicator()
                              .sendServerMessage(
                                 "You sense a wagoner whispering your name and saying you have "
                                    + (this.deliveriesWaiting == 1 ? "a delivery" : this.deliveriesWaiting + " deliveries")
                                    + " waiting to be accepted.",
                                 255,
                                 200,
                                 20,
                                 (byte)2
                              );
                        }
                     }

                     Delivery[] lostDeliveries = Delivery.getLostDeliveries(this.getWurmId());
                     if (this.deliveriesFailed != lostDeliveries.length) {
                        if (lostDeliveries.length > this.deliveriesFailed) {
                           this.getCommunicator()
                              .sendServerMessage(
                                 "You sense a wagoner whispering your name and saying you have one or more deliveries that have lost their wagoner.",
                                 255,
                                 200,
                                 20,
                                 (byte)2
                              );
                        }

                        this.deliveriesFailed = lostDeliveries.length;
                     }
                  }

                  if (!this.askedForVotes && this.secondsPlayed % 3.0F == 0.0F) {
                     this.getVotes();
                  }

                  if (this.gotVotes && this.secondsPlayed % 5.0F == 0.0F) {
                     this.gotVotes(false);
                  }

                  if (this.secondsPlayed == 10.0F) {
                     Tickets.sendRequiresAckMessage(this);
                  }

                  if (this.waitingForFriendCount == 0) {
                     this.sendNormalServerMessage(this.waitingForFriendName + " is not currently available, please try again later.");
                     this.sendFriendTimedOut();
                  } else if (this.waitingForFriendCount >= 0) {
                     --this.waitingForFriendCount;
                  }

                  if (this.whenStudied > 0L && this.whenStudied < WurmCalendar.getCurrentTime() - 900000L) {
                     System.out.println(this.whenStudied + " <> " + (WurmCalendar.getCurrentTime() - 900000L));
                     this.sendNormalServerMessage("You have forgotten whatever it was you studied.");
                     this.setStudied(0);
                  }
               } else if (this.waitingForFriendCount >= 0) {
                  this.sendFriendTimedOut();
               }

               return remove;
            }
         }
      }
   }

   private void sendFriendTimedOut() {
      PlayerState pstate = PlayerInfoFactory.getPlayerState(this.waitingForFriendName);
      if (pstate.getServerId() == Servers.getLocalServerId()) {
         try {
            Player p = Players.getInstance().getPlayer(this.waitingForFriendName);
            p.remoteAddFriend(this.getName(), this.getKingdomId(), (byte)3, false, true);
         } catch (NoSuchPlayerException var3) {
         }
      } else {
         WcAddFriend waf = new WcAddFriend(this.getName(), this.getKingdomId(), this.waitingForFriendName, (byte)3, true);
         if (Servers.isThisLoginServer()) {
            waf.sendToPlayerServer(this.waitingForFriendName);
         } else {
            waf.sendToLoginServer();
         }
      }

      this.waitingForFriendName = "";
      this.waitingForFriendCount = -1;
   }

   @Override
   public final boolean checkLoyaltyProgram() {
      if (this.hasFlag(10)
         || this.saveFile.paymentExpireDate <= 0L
         || this.saveFile.isFlagSet(63)
         || System.currentTimeMillis() >= this.saveFile.paymentExpireDate && this.getPower() <= 0) {
         return false;
      } else {
         this.setPremStuff();
         return true;
      }
   }

   private final void setPremStuff() {
      (new Thread("setPremStuff-Thread-" + this.getWurmId()) {
         @Override
         public void run() {
            if (!Servers.localServer.LOGINSERVER) {
               Player.this.contactLoginServerForAwards(true);
            } else {
               Player.this.setFlag(10, true);
               AwardLadder.awardTotalLegacy(Player.this.saveFile);
            }
         }
      }).start();
   }

   private final void contactLoginServerForAwards(boolean sendMess) {
      LoginServerWebConnection lsw = new LoginServerWebConnection();
      int[] premAndSilver = lsw.getPremTimeSilvers(this.getWurmId());
      if (premAndSilver[0] >= 0) {
         this.setFlag(10, true);
         if (this.saveFile.awards == null) {
            this.saveFile.awards = new Awards(this.getWurmId(), 0, premAndSilver[0], 0, 0, premAndSilver[1], 0L, 0, 0, true);
         } else {
            this.saveFile.awards.setMonthsPaidEver(premAndSilver[0]);
            this.saveFile.awards.setSilversPaidEver(premAndSilver[1]);
            this.saveFile.awards.update();
         }

         AwardLadder.awardTotalLegacy(this.saveFile);
      } else if (sendMess) {
         this.getCommunicator().sendAlertServerMessage("The login server is unavailable. Please try later.");
      }
   }

   public final void setUndeadType(byte udtype) {
      this.saveFile.undeadType = udtype;
   }

   @Override
   public final boolean isUndead() {
      return this.saveFile.undeadType != 0;
   }

   @Override
   public final byte getUndeadType() {
      return this.saveFile.undeadType;
   }

   @Override
   public final String getUndeadTitle() {
      if (!this.isUndead()) {
         return "";
      } else {
         switch(this.saveFile.undeadType) {
            case 0:
               return "";
            case 1:
               return "Zombie";
            case 2:
            default:
               return "";
            case 3:
               return "Ghost";
            case 4:
               return "Spectre";
            case 5:
               return "Lich";
            case 6:
               return "Lich King";
            case 7:
               return "Ghast";
            case 8:
               return "Ghoul";
         }
      }
   }

   private void checkVehicleSpeeds() {
      if (Server.rand.nextInt(8) == 0 && this.getVehicle() != -10L) {
         Vehicle vehic = Vehicles.getVehicleForId(this.getVehicle());
         if (vehic != null) {
            if (this.isVehicleCommander()) {
               vehic.updateDraggedSpeed(false);
            }

            if (vehic.creature) {
               try {
                  Creature c = Server.getInstance().getCreature(this.getVehicle());
                  if (c.isOnFire() && Server.rand.nextInt(10) == 0) {
                     int dam = (int)((float)(1000 + Server.rand.nextInt(4000)) * (100.0F - this.getSpellDamageProtectBonus()) / 100.0F);
                     if (dam > 1000) {
                        Wound wound = null;
                        boolean dead = false;

                        try {
                           byte pos = this.getBody().getRandomWoundPos();
                           if (Server.rand.nextInt(10) <= 6 && this.getBody().getWounds() != null) {
                              wound = this.getBody().getWounds().getWoundAtLocation(pos);
                              if (wound != null) {
                                 dead = wound.modifySeverity(dam);
                                 wound.setBandaged(false);
                                 this.setWounded();
                              }
                           }

                           if (wound == null) {
                              this.addWoundOfType(null, (byte)4, 1, true, 1.0F, true, (double)dam, 0.0F, 0.0F, false, false);
                           }

                           if (dead) {
                              return;
                           }
                        } catch (Exception var7) {
                           logger.log(Level.WARNING, this.getName() + ' ' + var7.getMessage(), (Throwable)var7);
                        }
                     }
                  }
               } catch (Exception var8) {
               }
            }
         }
      }
   }

   @Override
   public float getSecondsPlayed() {
      return this.secondsPlayed;
   }

   @Override
   public boolean checkPrayerFaith() {
      return this.saveFile.checkPrayerFaith();
   }

   @Override
   public void setPrayerSeconds(int prayerSeconds) {
      this.favorGainSecondsLeft = prayerSeconds;
   }

   @Override
   public void pollFavor() {
      float lMod = 1.0F;
      if (this.saveFile.getFavor() < this.saveFile.getFaith()) {
         try {
            Action act = this.getCurrentAction();
            if (act.getNumber() == 141) {
               lMod = 0.5F;
            }
         } catch (NoSuchActionException var5) {
         }

         if (this.favorGainSecondsLeft > 0) {
            lMod *= 2.0F;
         }

         if (this.hasSpiritFavorgain) {
            lMod *= 1.05F;
         }

         lMod *= 1.0F + Math.min(this.status.getFats(), 1.0F) / 3.0F;
         lMod = (float)((double)lMod * (1.15F + this.getMovementScheme().armourMod.getModifier()));
         if (this.getDeity() != null && this.getFaith() >= 35.0F && this.getDeity().isFavorRegenerator()) {
            lMod *= 1.1F;
         }

         if (lMod > 0.0F) {
            try {
               this.saveFile.setFavor(this.saveFile.getFavor() + lMod * (100.0F / (Math.max(1.0F, this.saveFile.getFavor()) * 25.0F)));
            } catch (IOException var4) {
               logger.log(Level.INFO, this.getName() + " " + var4.getMessage(), (Throwable)var4);
            }
         }
      } else if (this.saveFile.getFavor() > this.saveFile.getFaith()) {
         try {
            this.saveFile.setFavor(this.saveFile.getFaith());
         } catch (IOException var3) {
            logger.log(Level.INFO, this.getName() + " " + var3.getMessage(), (Throwable)var3);
         }
      }
   }

   @Override
   public boolean sendTransfer(
      Server senderServer,
      String targetIp,
      int targetPort,
      String serverpass,
      int targetServerId,
      int tilex,
      int tiley,
      boolean surfaced,
      boolean toOrFromEpic,
      byte targetKingdomId
   ) {
      if (this.isTrading()) {
         this.getTrade().end(this, true);
      }

      try {
         if (!toOrFromEpic) {
            PlayerTransfer.willItemsTransfer(this, true, targetServerId);
         }

         ServerEntry entry = Servers.getServerWithId(targetServerId);
         if (entry != null && this.getPower() <= 0 && (Server.getInstance().isPS() || !entry.isChallengeOrEpicServer())) {
            if (Server.getInstance().isPS() && Servers.localServer.PVPSERVER || Servers.isThisAChaosServer()) {
               this.saveFile.setChaosKingdom(this.getKingdomId());
            }

            if (targetKingdomId != 0 || (!Server.getInstance().isPS() || !entry.PVPSERVER) && !entry.isChaosServer()) {
               if (Server.getInstance().isPS() && entry.HOMESERVER || Servers.isThisAChaosServer()) {
                  targetKingdomId = entry.getKingdom() != 0 ? entry.getKingdom() : 4;
               }
            } else {
               targetKingdomId = this.saveFile.getChaosKingdom() == 0 ? 4 : this.saveFile.getChaosKingdom();
            }
         }

         this.getCommunicator().setGroundOffset(0, true);
         this.removeIllusion();
         PlayerTransfer pt = new PlayerTransfer(
            senderServer, this, targetIp, targetPort, serverpass, targetServerId, tilex, tiley, surfaced, toOrFromEpic, targetKingdomId
         );
         Server.getInstance().addIntraCommand(pt);
         this.setLogout();
         return true;
      } catch (Exception var13) {
         logger.log(Level.WARNING, var13.getMessage(), (Throwable)var13);
         return false;
      }
   }

   @Override
   public int getSecondsToLogout() {
      if (this.getSaveFile().currentServer != Servers.localServer.id) {
         return 1;
      } else {
         if (this.getPower() > 0) {
            this.secondsToLogout = 5;
         } else {
            boolean enemyHomeServer = Servers.localServer.isChallengeOrEpicServer()
               && Servers.localServer.HOMESERVER
               && Servers.localServer.KINGDOM != this.getKingdomId();
            if (enemyHomeServer && this.getCurrentVillage() != null) {
               return 2419200;
            }

            if ((long)this.secondsToLogout < 300L && this.currentTile != null && this.currentTile.getKingdom() != this.getKingdomId()) {
               return 300;
            }

            if (this.getEnemyPresense() > 0) {
               if (Servers.localServer.PVPSERVER
                  && Servers.localServer.isChallengeOrEpicServer()
                  && !Servers.localServer.HOMESERVER
                  && this.getCurrentVillage() != null
                  && this.currentTile != null
                  && this.currentTile.getKingdom() == this.getKingdomId()) {
                  return 3600;
               }

               return 180;
            }

            if ((long)this.secondsToLogout < 60L) {
               if (this.citizenVillage != null) {
                  VolaTile t = this.getCurrentTile();
                  if (t != null) {
                     if (t.getVillage() != null && t.getVillage() == this.citizenVillage) {
                        return Math.max(this.secondsToLogout, 0);
                     }

                     return 60;
                  }

                  return 0;
               }

               return 60;
            }
         }

         return this.secondsToLogout;
      }
   }

   public int getSecondsPlayedSinceLinkLoss() {
      return this.secondsPlayedSinceLinkloss;
   }

   public void setLink(boolean up) {
      if (!up) {
         if (this.receivedLinkloss == 0L) {
            this.receivedLinkloss = System.currentTimeMillis();
            this.secondsPlayedSinceLinkloss = 1;
            this.hasSentPoison = false;
            this.resetLastSentToolbelt();
            if (this.getVehicle() != -10L) {
               Vehicle v = Vehicles.getVehicleForId(this.getVehicle());
               if (v != null && v.isChair()) {
                  this.disembark(false);
               }
            }

            this.setLastVehicle(this.getVehicle(), this.getSeatType());
            Players.getInstance().sendConnectInfo(this, " lost link.", this.receivedLinkloss, PlayerOnlineStatus.LOST_LINK);
            if (this.communicator != null) {
               this.communicator.setReady(false);
            }

            this.secondsToLogout = Math.max(this.secondsToLogout, this.getSecondsToLogout());
            this.hasSentPoison = false;
            logger.log(Level.INFO, this.name + " lost link " + this.secondsToLogout + " secstologout.");

            try {
               if (this.getBody() != null && this.getBody().getBodyItem() != null) {
                  this.getBody().getBodyItem().removeWatcher(this, false);
               }

               if (this.getInventory() != null) {
                  this.getInventory().removeWatcher(this, false);
               }
            } catch (Exception var4) {
               logger.log(Level.WARNING, this.getName() + " " + var4.getMessage(), (Throwable)var4);
            }

            this.cancelTeleport();
            this.setTeleporting(false);
            this.teleportCounter = 0;

            try {
               this.save();
               this.destroyVisionArea();
            } catch (Exception var3) {
               logger.log(Level.WARNING, "Failed to save player " + this.name, (Throwable)var3);
            }

            if (this.isTrading()) {
               this.getTrade().end(this, false);
            }

            Questions.removeQuestion(this.question);
            this.closeBank();
            if (this.isDead()) {
               Players.getInstance().logoutPlayer(this);
            }
         }
      } else {
         this.secondsPlayedSinceLinkloss = 1;
         this.receivedLinkloss = 0L;
         this.loggedout = false;
      }
   }

   public void setLoginStep(int step) {
      this.loginStep = step;
   }

   public int getLoginStep() {
      return this.loginStep;
   }

   public boolean isNew() {
      return this.newPlayer;
   }

   public void setNewPlayer(boolean newp) {
      this.newPlayer = newp;
   }

   @Override
   public boolean hasLink() {
      return this.receivedLinkloss == 0L;
   }

   public static Player doNewPlayer(int templateId, SocketConnection serverConnection) throws Exception {
      return new Player(templateId, serverConnection);
   }

   public static Player doNewPlayer(int templateId) throws Exception {
      return new Player(templateId);
   }

   public Friend[] getFriends() {
      return this.saveFile != null ? this.saveFile.getFriends() : new Friend[0];
   }

   @Nullable
   public final Friend getFriend(long friendId) {
      return this.saveFile != null ? this.saveFile.getFriend(friendId) : null;
   }

   public void addFriend(long wurmId, byte catId, String note) {
      this.saveFile.addFriend(wurmId, catId, note, false);

      try {
         Player friend = Players.getInstance().getPlayer(wurmId);
         this.getCommunicator().sendFriend(new PlayerState(friend.getWurmId(), friend.getName(), friend.getLastLogin(), PlayerOnlineStatus.ONLINE), note);
      } catch (NoSuchPlayerException var7) {
         PlayerState pstate = PlayerInfoFactory.getPlayerState(wurmId);
         if (pstate != null) {
            this.getCommunicator().sendFriend(pstate, note);
         }
      }

      if (this.saveFile.getFriends().length > 49) {
         this.achievement(150);
      }
   }

   public void removeFriend(long friendWurmId) {
      PlayerState fState = PlayerInfoFactory.getPlayerState(friendWurmId);
      String friendName = fState != null ? fState.getPlayerName() : "Unknown";
      PlayerInfoFactory.breakFriendship(this.getName(), this.getWurmId(), friendName, friendWurmId);
   }

   public void updateFriendData(long friendWurmId, byte catId, String note) {
      this.saveFile.updateFriendData(friendWurmId, catId, note);
   }

   public long removeFriend(String friendName) {
      long friendWurmId = PlayerInfoFactory.breakFriendship(this.getName(), this.getWurmId(), friendName);
      if (friendWurmId != -10L) {
         this.getCommunicator().sendNormalServerMessage(friendName + " is no longer on your friend list.");
      } else {
         this.getCommunicator().sendNormalServerMessage("Could not find a player called " + friendName + ".");
      }

      return friendWurmId;
   }

   public void removeMeFromFriendsList(long wurmId, String friendName) {
      WcRemoveFriendship wrf = new WcRemoveFriendship(this.getName(), this.getWurmId(), friendName, wurmId);
      if (!Servers.isThisLoginServer()) {
         wrf.sendToLoginServer();
      } else {
         wrf.sendFromLoginServer();
      }
   }

   public boolean isFriend(long wurmId) {
      Friend[] friends = this.getFriends();

      for(Friend friend : friends) {
         if (friend.getFriendId() == wurmId) {
            return true;
         }
      }

      return false;
   }

   public void reimburse() {
      if (!this.isUndead()) {
         this.checkInitialTitles();
         this.checkJournalAchievements();
         if (this.getDeity() == null) {
            this.setFlag(74, true);
         }

         if (!this.saveFile.isReimbursed()) {
            if (!WurmCalendar.isChristmas() && !WurmCalendar.isEaster()) {
               try {
                  Item inventory = this.getInventory();
                  if (this.getPower() >= (Servers.localServer.testServer ? 2 : 4)) {
                     Item wand = createItem(176, 99.0F);
                     inventory.insertItem(wand);
                     logger.info("Reimbursed " + this.name + " with an Ebony Dev Wand: " + wand);
                  } else if (this.getPower() >= 2) {
                     Item wand = createItem(315, 99.0F);
                     inventory.insertItem(wand);
                     logger.info("Reimbursed " + this.name + " with an GM Wand: " + wand);
                  }
               } catch (Exception var7) {
                  logger.log(Level.INFO, "Failed to reimb " + this.name, (Throwable)var7);
               }
            }

            if (Servers.localServer.testServer) {
               try {
                  Item thingy = ItemFactory.createItem(480, 70.0F, this.getName());
                  this.getInventory().insertItem(thingy, true);
                  Item thingy2 = ItemFactory.createItem(516, 70.0F, this.getName());
                  this.getInventory().insertItem(thingy2, true);
                  Item thingy3 = ItemFactory.createItem(301, 60.0F, this.getName());
                  this.getInventory().insertItem(thingy3, true);
               } catch (Exception var6) {
               }

               try {
                  this.saveFile.setReimbursed(true);
               } catch (IOException var5) {
               }
            }

            if (!WurmCalendar.isChristmas() && !WurmCalendar.isEaster()) {
               try {
                  this.saveFile.setReimbursed(true);
               } catch (IOException var4) {
               }
            }
         }

         if (Features.Feature.GIFT_PACKS.isEnabled()) {
            this.reimbursePacks(false);
         }

         this.reimbAnniversaryGift(false);
      }
   }

   public final void reimbursePacks(boolean override) {
      if (!this.hasFlag(46) || override) {
         if (!this.isPaying() && !override || Servers.localServer.isChallengeServer()) {
            return;
         }

         try {
            Item thingy = ItemFactory.createItem(1097, 70.0F, this.getName());
            this.getInventory().insertItem(thingy, true);
            this.setFlag(46, true);
         } catch (Exception var6) {
         }

         if (this.hasFlag(47) || override) {
            try {
               Item thingy = ItemFactory.createItem(1098, 70.0F, this.getName());
               this.getInventory().insertItem(thingy, true);
               Item mask = ItemFactory.createItem(1099, 90.0F + Server.rand.nextFloat() * 10.0F, this.getName());
               this.getInventory().insertItem(mask, true);
               this.setFlag(47, false);
            } catch (Exception var5) {
            }

            try {
               this.addMoney(50000L);
            } catch (Exception var4) {
            }
         }
      }
   }

   public final void reimbAnniversaryGift(boolean override) {
      if (Features.Feature.EXTRAGIFT.isEnabled() && (!this.hasFlag(49) || override)) {
         if (!this.isPaying() && !override || Servers.localServer.isChallengeServer()) {
            return;
         }

         if (this.getSaveFile() != null) {
            int daysPrem = 1;
            if (!override && this.getSaveFile().awards == null) {
               this.contactLoginServerForAwards(false);
               if (this.getSaveFile().awards != null) {
                  daysPrem = Math.max(
                     this.getSaveFile().awards.getMonthsPaidSinceReset() * 30,
                     Math.max(this.getSaveFile().awards.getDaysPrem(), this.getSaveFile().awards.getMonthsPaidEver() * 30)
                  );
               } else {
                  logger.log(Level.WARNING, this.getName() + " no premium time/silvers received from login server..");
               }
            } else {
               daysPrem = override
                  ? 100 + Server.rand.nextInt(900)
                  : Math.max(
                     this.getSaveFile().awards.getMonthsPaidSinceReset() * 30,
                     Math.max(this.getSaveFile().awards.getDaysPrem(), this.getSaveFile().awards.getMonthsPaidEver() * 30)
                  );
            }

            float ql = Math.max(20.0F, Math.min(99.99F, (float)daysPrem / 10.0F));

            try {
               Item thingy = ItemFactory.createItem(1100, ql, this.getName());
               if (daysPrem > 900) {
                  thingy.setRarity((byte)3);
                  thingy.setMaterial((byte)34);
               } else if (daysPrem > 600) {
                  thingy.setRarity((byte)2);
                  thingy.setMaterial((byte)11);
               } else if (daysPrem > 300) {
                  thingy.setRarity((byte)1);
                  switch(Server.rand.nextInt(5)) {
                     case 0:
                        thingy.setMaterial((byte)66);
                        break;
                     case 1:
                        thingy.setMaterial((byte)45);
                        break;
                     case 2:
                        thingy.setMaterial((byte)42);
                        break;
                     case 3:
                        thingy.setMaterial((byte)38);
                        break;
                     case 4:
                        thingy.setMaterial((byte)39);
                  }
               } else {
                  thingy.setMaterial((byte)69);
               }

               thingy.setAuxData((byte)1);

               try {
                  Item champy = ItemFactory.createItem(1101, ql, this.getName());
                  if (daysPrem > 800) {
                     champy.setRarity((byte)3);
                  } else if (daysPrem > 600) {
                     champy.setRarity((byte)2);
                  } else if (daysPrem > 300) {
                     champy.setRarity((byte)1);
                  }

                  thingy.insertItem(champy, true);
               } catch (Exception var6) {
                  logger.log(Level.WARNING, var6.getMessage(), (Throwable)var6);
               }

               this.getInventory().insertItem(thingy, true);
               this.getCommunicator().sendSafeServerMessage("There's a new item in your inventory! Happy 10 Years Anniversary!");
            } catch (Exception var7) {
               logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
            }

            this.setFlag(49, true);
         }
      }
   }

   public void setIpaddress(String ipaddress) {
      try {
         if (this.saveFile == null) {
            logger.log(Level.WARNING, "Savefile is null for " + this.name);
         } else {
            this.saveFile.setIpaddress(ipaddress);
         }
      } catch (Exception var3) {
         logger.log(Level.WARNING, "Failed to set ipaddress=" + ipaddress + " for player " + this.name, (Throwable)var3);
      }
   }

   public SteamId getSteamId() {
      return this.saveFile.getSteamId();
   }

   public void setSteamID(SteamId steamId) {
      try {
         if (this.saveFile == null) {
            logger.log(Level.WARNING, "Savefile is null for " + this.name);
         } else {
            this.saveFile.setSteamId(steamId);
         }
      } catch (Exception var3) {
         logger.log(Level.WARNING, "Failed to set SteamId of " + steamId.getSteamID64() + " for player " + this.name, (Throwable)var3);
      }
   }

   public boolean hasPlantedSign() {
      return this.saveFile.hasPlantedSign();
   }

   public void plantSign() {
      try {
         if (this.getPower() == 0) {
            this.saveFile.setPlantedSign();
         }
      } catch (Exception var2) {
         logger.log(Level.WARNING, this.name + " " + var2.getMessage(), (Throwable)var2);
      }
   }

   public void ban(String reason, long expiry) throws Exception {
      this.saveFile.setBanned(true, reason, expiry);
      Players.getInstance().addBannedIp(this.communicator.getConnection().getIp(), "[" + this.getName() + "] " + reason, expiry);
      this.logoutIn(5, "You have been banned. Reason: " + reason);
   }

   public Ban getBan() {
      if (!this.saveFile.isBanned()) {
         return null;
      } else if (System.currentTimeMillis() <= this.saveFile.banexpiry) {
         return new PlayerBan(this.saveFile.getName(), this.saveFile.banreason, this.saveFile.banexpiry);
      } else {
         try {
            this.saveFile.setBanned(false, "", 0L);
         } catch (Exception var2) {
            logger.log(Level.WARNING, "Unbanning " + this.getName() + " failed!:" + var2.getMessage(), (Throwable)var2);
         }

         return null;
      }
   }

   @Override
   public boolean isIgnored(long playerId) {
      if (WurmId.getType(playerId) == 0) {
         return this.saveFile.isIgnored(playerId);
      } else {
         if (WurmId.getType(playerId) == 1) {
            try {
               Creature creature = Creatures.getInstance().getCreature(playerId);
               if (creature.isWagoner()) {
                  return this.hasFlag(54);
               }
            } catch (NoSuchCreatureException var4) {
               logger.log(Level.WARNING, var4.getMessage(), (Throwable)var4);
            }
         }

         return false;
      }
   }

   public boolean addIgnored(long playerId) throws IOException {
      return this.saveFile.addIgnored(playerId, false);
   }

   public boolean removeIgnored(long playerId) throws IOException {
      return this.saveFile.removeIgnored(playerId);
   }

   public long[] getIgnored() {
      return this.saveFile.getIgnored();
   }

   @Override
   public boolean isPaying() {
      return true;
   }

   @Override
   public boolean isReallyPaying() {
      return true;
   }

   public final boolean isQAAccount() {
      return this.hasFlag(26);
   }

   @Override
   public int getPower() {
      return this.guest ? 0 : this.saveFile.getPower();
   }

   public void setPaymentExpire(long paymentExpire) throws IOException {
      this.saveFile.setPaymentExpire(paymentExpire);
   }

   public long getPaymentExpire() {
      return this.saveFile.getPaymentExpire();
   }

   public void setPower(byte power) throws IOException {
      this.saveFile.setPower(power);
   }

   public void setRank(int newRank) throws IOException {
      this.saveFile.setRank(newRank);
      this.getCommunicator().sendSafeServerMessage("Your battlerank is now " + newRank + ".");
   }

   @Override
   public void modifyRanking() {
      StringBuilder attackerStringbuilder = new StringBuilder();
      if (!this.isNewbie() && this.isPaying()) {
         if (this.getFightingSkill() == null || this.getFightingSkill().getKnowledge() < 20.0) {
            this.attackers = null;
            return;
         }

         int rank = this.getRank();
         List<Player> validAttackers = new ArrayList<>();
         int totRank = 0;
         int highestRank = 0;
         int lowestRank = 9999;
         long now = System.currentTimeMillis();
         if (this.attackers != null && this.attackers.size() > 0) {
            int count = 0;
            int numberUnknown = 1;

            for(Long attackerId : this.attackers.keySet()) {
               ++count;
               Long time = this.attackers.get(attackerId);
               if (WurmId.getType(attackerId) == 0) {
                  try {
                     Player player = Players.getInstance().getPlayer(attackerId);
                     attackerStringbuilder.append(player.getName());
                     if (count != this.attackers.size()) {
                        attackerStringbuilder.append(", ");
                     }

                     if (player.isPaying() && !Players.getInstance().isOverKilling(attackerId, this.getWurmId()) && now - time < 600000L) {
                        if (!Servers.localServer.isChallengeServer() || this.getKingdomId() != player.getKingdomId()) {
                           try {
                              Players.getInstance().addKill(attackerId, this.getWurmId(), this.getName());
                           } catch (Exception var30) {
                              logger.log(
                                 Level.INFO,
                                 "Failed to add kill for " + player.getName() + ":" + this.getName() + " - " + var30.getMessage(),
                                 (Throwable)var30
                              );
                           }
                        }

                        totRank += Math.max(player.getRank(), rank - 500);
                        highestRank = Math.max(player.getRank(), highestRank);
                        lowestRank = Math.min(player.getRank(), lowestRank);
                        validAttackers.add(player);
                     }
                  } catch (NoSuchPlayerException var32) {
                  }
               } else if (WurmId.getType(attackerId) == 1
                  && Features.Feature.PVE_DEATHTABS.isEnabled()
                  && !Servers.localServer.PVPSERVER
                  && !this.hasFlag(59)
                  && now - time < 900000L) {
                  String nameString = "Defeated Foe #" + numberUnknown;

                  try {
                     Creature creature = Creatures.getInstance().getCreature(attackerId);
                     nameString = creature.getNameWithoutFatStatus();
                  } catch (NoSuchCreatureException var31) {
                     ++numberUnknown;
                     boolean easterEgg = false;
                     float chance = Server.rand.nextFloat();
                     if (chance >= 0.99F) {
                        nameString = "Carebear";
                        easterEgg = true;
                     } else if ((double)chance >= 0.98) {
                        nameString = "Wogic";
                        easterEgg = true;
                     } else if ((double)chance >= 0.97) {
                        nameString = "Lag Monster";
                        easterEgg = true;
                     } else if ((double)chance >= 0.96) {
                        nameString = "Test Minion";
                        easterEgg = true;
                     } else if ((double)chance >= 0.95) {
                        nameString = "Server Bug";
                        easterEgg = true;
                     } else if ((double)chance >= 0.94) {
                        nameString = "Developer";
                        easterEgg = true;
                     } else if ((double)chance >= 0.93) {
                        nameString = "Server Hamster";
                        easterEgg = true;
                     } else if ((double)chance >= 0.92) {
                        nameString = "Hell Unicorn";
                        easterEgg = true;
                     } else if ((double)chance >= 0.91) {
                        nameString = "Heaven Scorpius";
                        easterEgg = true;
                     }

                     if (easterEgg) {
                        int random = Server.rand.nextInt(6);
                        String age = "";
                        switch(random) {
                           case 0:
                              age = "adolescent";
                           case 1:
                              age = "young";
                           case 2:
                              age = "mature";
                           case 3:
                              age = "aged";
                           case 4:
                              age = "old";
                           case 5:
                              age = "venerable";
                           default:
                              age = "";
                              nameString = age + " " + nameString;
                        }
                     }
                  }

                  attackerStringbuilder.append(nameString);
                  if (count != this.attackers.size()) {
                     attackerStringbuilder.append(", ");
                  }
               }
            }

            if (validAttackers.size() > 0) {
               int avgRank = totRank / validAttackers.size();
               int rankVariance = highestRank - lowestRank;
               int rankDiff = avgRank - rank;
               int points = rankDiff > 0 ? 15 - rankDiff / 25 : 15 - rankDiff / 5;
               int pointsEach = points / validAttackers.size();
               int pointsBonus = 0;
               int totalPointsGiven = 0;
               Set<Byte> kingdomsInvolved = new HashSet<>();
               if (points / 2 >= validAttackers.size()) {
                  pointsEach = points / 2 / validAttackers.size();
                  pointsBonus = points / 2;
               } else if (points < validAttackers.size()) {
                  pointsEach = 0;
                  pointsBonus = points;
               }

               for(Player p : validAttackers) {
                  if (p.getKingdomId() != this.getKingdomId() || p.isEnemyOnChaos(this)) {
                     try {
                        int bonus = pointsBonus > 0 ? (int)((float)((highestRank - p.getRank()) / 2) / (float)rankVariance * (float)pointsBonus) : 0;
                        p.checkBattleTitle(p.getRank(), p.getRank() + pointsEach + bonus);
                        p.setRank(p.getRank() + pointsEach + bonus);
                        if (rank > 1000) {
                           p.setKarma(p.getKarma() + (pointsEach + bonus) * 50);
                        }

                        totalPointsGiven += pointsEach + bonus;
                        if (!kingdomsInvolved.contains(p.getKingdomId())) {
                           kingdomsInvolved.add(p.getKingdomId());
                        }
                     } catch (IOException var29) {
                        logger.log(Level.WARNING, this.getName() + ": failed to give " + pointsEach + " to " + p.getName(), (Throwable)var29);
                     }
                  }
               }

               if (!Servers.localServer.isChallengeServer() || Server.rand.nextInt(5) == 0) {
                  Affinity[] affs = Affinities.getAffinities(this.getWurmId());
                  if (affs.length > 1 || this.isChampion() && affs.length > 0) {
                     List<Player> possibleGainers = new ArrayList<>();

                     for(Player p : validAttackers) {
                        if (p.getKingdomId() != this.getKingdomId() || p.isEnemyOnChaos(this)) {
                           possibleGainers.add(p);
                        }
                     }

                     if (possibleGainers.size() > 0) {
                        boolean affinityGiven = false;
                        Player randomPlayer = possibleGainers.get(Server.rand.nextInt(possibleGainers.size()));
                        int sknum = affs[Server.rand.nextInt(affs.length)].skillNumber;
                        Skill deceasedSkill = this.getSkills().getSkillOrLearn(sknum);

                        while(!affinityGiven && possibleGainers.size() > 0) {
                           Skill killerSkill = randomPlayer.getSkills().getSkillOrLearn(sknum);
                           float chanceToGain = deceasedSkill.affinity >= killerSkill.affinity - 1 ? 1.0F : 0.5F;
                           if (Server.rand.nextFloat() <= chanceToGain) {
                              if (killerSkill.affinity == 0) {
                                 randomPlayer.getCommunicator()
                                    .sendNormalServerMessage(
                                       "You realize that you have developed an affinity for " + SkillSystem.getNameFor(sknum).toLowerCase() + ".", (byte)2
                                    );
                              } else {
                                 randomPlayer.getCommunicator()
                                    .sendNormalServerMessage(
                                       "You realize that your affinity for " + SkillSystem.getNameFor(sknum).toLowerCase() + " has grown stronger.", (byte)2
                                    );
                              }

                              Affinities.setAffinity(randomPlayer.getWurmId(), sknum, killerSkill.affinity + 1, false);
                              logger.log(
                                 Level.INFO, randomPlayer.getName() + " receives affinity " + SkillSystem.getNameFor(sknum) + " from " + this.getName()
                              );
                              affinityGiven = true;
                           } else {
                              possibleGainers.remove(randomPlayer);
                              randomPlayer = possibleGainers.get(Server.rand.nextInt(possibleGainers.size()));
                           }
                        }

                        Affinities.decreaseAffinity(this.getWurmId(), sknum, 1);
                        if (!affinityGiven) {
                           randomPlayer = validAttackers.get(Server.rand.nextInt(validAttackers.size()));
                           if (randomPlayer.getKingdomId() != this.getKingdomId() || randomPlayer.isEnemyOnChaos(this)) {
                              AffinitiesTimed at = AffinitiesTimed.getTimedAffinitiesByPlayer(randomPlayer.getWurmId(), true);
                              at.add(sknum, 604800L);
                              randomPlayer.getCommunicator()
                                 .sendNormalServerMessage(
                                    "You realize that you have more of an insight about " + SkillSystem.getNameFor(sknum).toLowerCase() + ".", (byte)2
                                 );
                              at.sendTimedAffinity(randomPlayer, sknum);
                              logger.log(
                                 Level.INFO, this.getName() + " loses affinity " + SkillSystem.getNameFor(sknum) + " from death via " + randomPlayer.getName()
                              );
                           }
                        }

                        for(Player p : validAttackers) {
                           if (p != randomPlayer && (p.getKingdomId() != this.getKingdomId() || p.isEnemyOnChaos(this))) {
                              AffinitiesTimed at = AffinitiesTimed.getTimedAffinitiesByPlayer(p.getWurmId(), true);
                              at.add(sknum, (long)(86400.0F * (1.0F + Server.rand.nextFloat())));
                              p.getCommunicator()
                                 .sendNormalServerMessage(
                                    "You realize that you have more of an insight about " + SkillSystem.getNameFor(sknum).toLowerCase() + ".", (byte)2
                                 );
                              at.sendTimedAffinity(p, sknum);
                           }
                        }
                     }
                  } else {
                     int sknum = SkillSystem.getRandomSkillNum();

                     for(Player p : validAttackers) {
                        AffinitiesTimed at = AffinitiesTimed.getTimedAffinitiesByPlayer(p.getWurmId(), true);
                        at.add(sknum, (long)(3600.0F * (3.0F + Server.rand.nextFloat() * 3.0F)));
                        p.getCommunicator()
                           .sendNormalServerMessage(
                              "You realize that you have more of an insight about " + SkillSystem.getNameFor(sknum).toLowerCase() + ".", (byte)2
                           );
                        at.sendTimedAffinity(p, sknum);
                     }
                  }
               }

               if (!this.isChampion() && totalPointsGiven > 0) {
                  try {
                     this.setRank(Math.max(1000, rank - (int)((float)totalPointsGiven * 0.75F)));
                     Players.printRanks();
                  } catch (IOException var28) {
                     logger.log(Level.WARNING, this.getName() + ": failed to set rank to " + (rank - (int)((float)totalPointsGiven * 0.75F)), (Throwable)var28);
                  }
               }

               King king = King.getKing(this.getKingdomId());
               if (king != null) {
                  int levelsLost = this.getRoyalLevels();

                  for(Byte b : kingdomsInvolved) {
                     King k = King.getKing(b);
                     if (k != null) {
                        k.addLevelsKilled(Math.max(1, levelsLost / kingdomsInvolved.size()), this.getName(), levelsLost);
                        king.addLevelsLost(Math.max(1, levelsLost / kingdomsInvolved.size()));
                     }
                  }
               }
            }
         }
      }

      if (attackerStringbuilder.toString().length() > 0) {
         Players.getInstance().broadCastDeathInfo(this, attackerStringbuilder.toString());
      }

      this.attackers = null;
   }

   public int getRoyalLevels() {
      int nums = 0;
      if (this.isKing()) {
         nums = 20;
      }

      King k = King.getKing(this.getKingdomId());
      if (k != null) {
         return Appointments.getAppointments(k.era).getAppointmentLevels(this.getAppointments(), this.getWurmId()) + nums;
      } else {
         return King.currentEra > 0 ? Appointments.getAppointments(King.currentEra).getAppointmentLevels(this.getAppointments(), this.getWurmId()) + nums : 0;
      }
   }

   public void setAffString(String string) {
      if (this.affstring != null) {
         this.getCommunicator().sendNormalServerMessage(this.affstring);
      }

      this.affstring = string;
      this.affcounter = 10;
   }

   public void checkBattleTitle(int oldrank, int newrank) {
      if (oldrank < 1100 && newrank >= 1100) {
         this.addTitle(Titles.Title.Warrior);
      }

      if (oldrank < 1500 && newrank >= 1500) {
         this.addTitle(Titles.Title.Warrior_Minor);
      }

      if (oldrank < 1900 && newrank >= 1900) {
         this.addTitle(Titles.Title.Warrior_Master);
      }
   }

   public void checkInitialBattleTitles() {
      int br = this.getRank();
      if (br >= 1100) {
         this.addTitle(Titles.Title.Warrior);
      }

      if (br >= 1500) {
         this.addTitle(Titles.Title.Warrior_Minor);
      }

      if (br >= 1900) {
         this.addTitle(Titles.Title.Warrior_Master);
      }
   }

   public void checkFaithTitles() {
      float f = this.getFaith();
      if (f >= 50.0F) {
         this.addTitle(Titles.Title.Faith);
      }

      if (f >= 70.0F) {
         this.addTitle(Titles.Title.Faith_Minor);
      }

      if (f >= 90.0F) {
         this.addTitle(Titles.Title.Faith_Master);
      }

      if (f >= 100.0F) {
         this.addTitle(Titles.Title.Faith_Legend);
      }

      if (f == 30.0F) {
         this.achievement(569);
      }

      if (f >= 40.0F) {
         this.maybeTriggerAchievement(608, true);
      }

      if (f >= 70.0F) {
         this.maybeTriggerAchievement(618, true);
      }

      if (f >= 90.0F) {
         this.maybeTriggerAchievement(630, true);
      }
   }

   public void maybeTriggerAchievement(int achievementId, boolean shouldTrigger) {
      if (shouldTrigger) {
         if (!Achievements.hasAchievement(this.getWurmId(), achievementId)) {
            this.achievement(achievementId);
         }
      }
   }

   public void checkJournalAchievements() {
      this.maybeTriggerAchievement(548, this.getCultist() != null);
      this.maybeTriggerAchievement(556, this.getDeity() != null);
      this.maybeTriggerAchievement(569, this.getFaith() >= 30.0F);
      this.maybeTriggerAchievement(570, this.getCultist() != null && this.getCultist().getLevel() >= 4);
      this.maybeTriggerAchievement(572, this.getTitles().length >= 15);
      this.maybeTriggerAchievement(578, this.getCultist() != null && this.getCultist().getLevel() >= 7);
      this.maybeTriggerAchievement(599, this.getCultist() != null && this.getCultist().getLevel() >= 9);
      this.maybeTriggerAchievement(579, this.getTitles().length >= 30);
      this.maybeTriggerAchievement(591, this.getTitles().length >= 60);
      this.maybeTriggerAchievement(604, this.isPriest());
      this.maybeTriggerAchievement(608, this.getFaith() >= 40.0F);
      this.maybeTriggerAchievement(618, this.getFaith() >= 70.0F);
      this.maybeTriggerAchievement(630, this.getFaith() >= 90.0F);
      if (this.getDeity() != null) {
         this.maybeTriggerAchievement(626, this.getDeity().isHateGod() ? this.getAlignment() == -100.0F : this.getAlignment() == 100.0F);
      }

      if (this.hasFlag(70)) {
         this.addTitle(Titles.Title.Journal_T6);
      }
   }

   public void checkInitialTitles() {
      if (this.getPlayingTime() > 259200000L) {
         this.checkInitialBattleTitles();
         Skill[] sk = this.skills.getSkills();
         int count = 0;

         for(int x = 0; x < sk.length; ++x) {
            sk[x].checkInitialTitle();
            if (sk[x].getKnowledge() >= 50.0) {
               ++count;
            }
         }

         if (count >= 10) {
            this.maybeTriggerAchievement(598, true);
         }
      }
   }

   private short[] getSpawnPointOutside(Village village) {
      return !this.isPaying() && Zones.isVillagePremSpawn(village) ? new short[]{-1, -1} : village.getOutsideSpawn();
   }

   public void sendSpawnQuestion() {
      if (this.isUndead()) {
         this.spawn((byte)0);
      } else {
         if (this.spawnpoints == null) {
            this.calculateSpawnPoints();
         }

         if (this.spawnpoints != null) {
            SpawnQuestion q = new SpawnQuestion(this, "In the darkness", "Select where you will reenter the light:", this.getWurmId());
            q.sendQuestion();
         }
      }
   }

   public void spawn(byte spawnPoint) {
      if (this.isDead()) {
         this.addNewbieBuffs();
         this.setLayer(0, false);
         boolean found = false;
         this.justSpawned = true;
         if (this.isUndead()) {
            float[] txty = findRandomSpawnX(false, false);
            float posX = txty[0];
            float posY = txty[1];
            this.setTeleportPoints(posX, posY, 0, 0);
            this.startTeleporting();
            found = true;
            this.getCommunicator().sendNormalServerMessage("You are cast back into the horrible light.");
         } else {
            if (this.spawnpoints != null) {
               for(Spawnpoint sp : this.spawnpoints) {
                  if (sp.number == spawnPoint) {
                     this.setTeleportPoints(sp.tilex, sp.tiley, sp.surfaced ? 0 : -1, 0);
                     this.startTeleporting();
                     found = true;
                     this.getCommunicator().sendNormalServerMessage("You are cast back into the light.");
                     break;
                  }
               }
            }

            if (!found) {
               if (Servers.localServer.randomSpawns) {
                  float[] txty = findRandomSpawnX(true, true);
                  float posX = txty[0];
                  float posY = txty[1];
                  this.setTeleportPoints(posX, posY, 0, 0);
               } else if (this.getKingdomId() == 3 && Servers.localServer.SPAWNPOINTLIBX != -1) {
                  this.setTeleportPoints((float)Servers.localServer.SPAWNPOINTLIBX, (float)Servers.localServer.SPAWNPOINTLIBY, 0, 0);
               } else if (this.getKingdomId() == 2 && Servers.localServer.SPAWNPOINTMOLX != -1) {
                  this.setTeleportPoints((float)Servers.localServer.SPAWNPOINTMOLX, (float)Servers.localServer.SPAWNPOINTMOLY, 0, 0);
               } else {
                  this.setTeleportPoints((float)Servers.localServer.SPAWNPOINTJENNX, (float)Servers.localServer.SPAWNPOINTJENNX, 0, 0);
               }

               this.getCommunicator().sendNormalServerMessage("You are cast back into the light where it all began.");
               this.startTeleporting();
            }
         }

         this.getCommunicator().sendTeleport(false);
         this.setDead(false);
         this.spawnpoints = null;
      }
   }

   private boolean calculateMissionSpawnPoint() {
      MissionPerformer mp = MissionPerformed.getMissionPerformer(this.getWurmId());
      if (mp != null) {
         MissionPerformed[] perfs = mp.getAllMissionsPerformed();

         for(int x = 0; x < perfs.length; ++x) {
            if (!perfs[x].isInactivated() && !perfs[x].isCompleted() && !perfs[x].isFailed() && perfs[x].isStarted()) {
               Mission mission = perfs[x].getMission();
               if (mission != null) {
                  MissionTrigger spawnPoint = MissionTriggers.getRespawnTriggerForMission(mission.getId(), perfs[x].getState());
                  if (spawnPoint != null) {
                     Spawnpoint sp = spawnPoint.getSpawnPoint();
                     if (sp != null) {
                        if (this.spawnpoints != null) {
                           this.spawnpoints.clear();
                        } else {
                           this.spawnpoints = new HashSet<>();
                        }

                        this.spawnpoints.add(sp);
                        return true;
                     }
                  }
               }
            }
         }
      }

      return false;
   }

   public boolean sendLastMissionInformation() {
      Questions.removeQuestions(this);
      boolean sent = false;
      MissionPerformer mp = MissionPerformed.getMissionPerformer(this.getWurmId());
      if (mp != null) {
         if (this.saveFile.getLastTrigger() > 0) {
            TriggerEffect eff = TriggerEffects.getTriggerEffect(this.saveFile.getLastTrigger());
            if (eff != null && eff.sendTriggerDescription(this)) {
               sent = true;
            }
         }

         if (!sent) {
            MissionPerformed[] perfs = mp.getAllMissionsPerformed();

            for(int x = 0; x < perfs.length; ++x) {
               if (!perfs[x].isInactivated() && !perfs[x].isCompleted() && !perfs[x].isFailed() && perfs[x].isStarted()) {
                  Mission mission = perfs[x].getMission();
                  if (mission != null && mission.getInstruction() != null && mission.getInstruction().length() > 0) {
                     SimplePopup pop = new SimplePopup(this, "Mission start", mission.getInstruction());
                     pop.sendQuestion();
                     sent = true;
                  }
               }
            }
         }
      }

      return sent;
   }

   public void setLastTrigger(int triggerEffect) {
      this.saveFile.setLastTrigger(triggerEffect);
   }

   public void calculateSpawnPoints() {
      long start = System.currentTimeMillis();
      this.spawnpoints = new HashSet<>();
      if (!this.calculateMissionSpawnPoint()) {
         if (this.isNewTutorial() && Servers.localServer.entryServer && this.getKingdomId() == 4) {
            short tpx = (short)Servers.localServer.SPAWNPOINTJENNX;
            short tpy = (short)Servers.localServer.SPAWNPOINTJENNY;
            Village[] villages = Villages.getVillages();

            for(Village vill : villages) {
               if (vill.isPermanent && vill.kingdom == 4) {
                  try {
                     tpx = (short)vill.getToken().getTileX();
                     tpy = (short)vill.getToken().getTileY();
                     if (vill.getReputation(this) > -30 && Zones.isGoodTileForSpawn(tpx, tpy, vill.isOnSurface(), true)) {
                        Spawnpoint spa = new Spawnpoint(this.spnums++, vill.getName(), tpx, tpy, true);
                        this.spawnpoints.add(spa);
                     }
                  } catch (NoSuchItemException var15) {
                  }
               }
            }
         } else {
            short tpx = (short)Servers.localServer.SPAWNPOINTJENNX;
            short tpy = (short)Servers.localServer.SPAWNPOINTJENNY;
            this.spnums = 0;
            if (Servers.localServer.randomSpawns) {
               Item[] spawns = Items.getSpawnPoints();

               for(Item spawn : spawns) {
                  Spawnpoint spa = new Spawnpoint(
                     this.spnums++,
                     "Spawnpoint " + spawn.getDescription(),
                     (short)(spawn.getTileX() - 4 + Server.rand.nextInt(9)),
                     (short)(spawn.getTileY() - 4 + Server.rand.nextInt(9)),
                     true
                  );
                  this.spawnpoints.add(spa);
               }
            }

            Set<Village> villageSet = new HashSet<>();
            if (Servers.localServer.HOMESERVER && this.getKingdomId() != Servers.localServer.KINGDOM) {
               if (this.getKingdomTemplateId() == 3) {
                  if (Servers.localServer.SPAWNPOINTLIBX > 0) {
                     tpx = (short)Servers.localServer.SPAWNPOINTLIBX;
                     tpy = (short)Servers.localServer.SPAWNPOINTLIBY;
                  }
               } else if (this.getKingdomTemplateId() == 2) {
                  if (Servers.localServer.SPAWNPOINTMOLX > 0) {
                     tpx = (short)Servers.localServer.SPAWNPOINTMOLX;
                     tpy = (short)Servers.localServer.SPAWNPOINTMOLY;
                  }
               } else if (this.getKingdomTemplateId() == 1) {
                  if (Servers.localServer.SPAWNPOINTJENNX > 0) {
                     tpx = (short)Servers.localServer.SPAWNPOINTJENNX;
                     tpy = (short)Servers.localServer.SPAWNPOINTJENNY;
                  }
               } else {
                  tpx = (short)Servers.localServer.SPAWNPOINTJENNX;
                  tpy = (short)Servers.localServer.SPAWNPOINTJENNY;
                  Village[] villages = Villages.getVillages();

                  for(Village vill : villages) {
                     if ((vill.isPermanent || vill.isCapital())
                        && vill.kingdom == this.getKingdomId()
                        && (vill.isPermanent || vill.isAlly(this) || vill.isCitizen(this))) {
                        boolean ok = true;
                        if (!this.isPaying() && Zones.isVillagePremSpawn(vill)) {
                           ok = false;
                        }

                        if (ok) {
                           try {
                              tpx = (short)vill.getToken().getTileX();
                              tpy = (short)vill.getToken().getTileY();
                              if (vill.getReputation(this) > -30 && Zones.isGoodTileForSpawn(tpx, tpy, vill.isOnSurface(), true)) {
                                 Spawnpoint spa = new Spawnpoint(this.spnums++, vill.getName(), tpx, tpy, true);
                                 this.spawnpoints.add(spa);
                                 if (!villageSet.contains(vill)) {
                                    villageSet.add(vill);
                                 }
                              }

                              if (!Servers.localServer.entryServer) {
                                 short[] sp = this.getSpawnPointOutside(vill);
                                 tpx = sp[0];
                                 tpy = sp[1];
                                 if (Zones.isGoodTileForSpawn(sp[0], sp[1], vill.isOnSurface())) {
                                    Spawnpoint ohome = new Spawnpoint(this.spnums++, "Outside " + vill.getName(), tpx, tpy, vill.isOnSurface());
                                    this.spawnpoints.add(ohome);
                                    if (!villageSet.contains(vill)) {
                                       villageSet.add(vill);
                                    }
                                 }
                              }
                           } catch (NoSuchItemException var17) {
                           }
                        }
                     }
                  }
               }
            } else {
               Village[] villages = Villages.getVillages();

               for(Village vill : villages) {
                  if ((vill.isCapital() || vill.isPermanent) && vill.kingdom == this.getKingdomId()) {
                     boolean ok = true;
                     if (!this.isPaying() && !Servers.localServer.isChaosServer() && Zones.isVillagePremSpawn(vill)) {
                        ok = false;
                     }

                     if (ok) {
                        try {
                           if (vill.isPermanent || vill.isAlly(this) || vill.isCitizen(this)) {
                              tpx = (short)vill.getToken().getTileX();
                              tpy = (short)vill.getToken().getTileY();
                              if (vill.getReputation(this) > -30 && Zones.isGoodTileForSpawn(tpx, tpy, vill.isOnSurface(), true)) {
                                 Spawnpoint spa = new Spawnpoint(this.spnums++, vill.getName(), tpx, tpy, true);
                                 this.spawnpoints.add(spa);
                                 if (!villageSet.contains(vill)) {
                                    villageSet.add(vill);
                                 }
                              }

                              if (!Servers.localServer.entryServer) {
                                 short[] sp = this.getSpawnPointOutside(vill);
                                 tpx = sp[0];
                                 tpy = sp[1];
                                 if (Zones.isGoodTileForSpawn(tpx, tpy, vill.isOnSurface())) {
                                    Spawnpoint ohome = new Spawnpoint(this.spnums++, "Outside " + vill.getName(), tpx, tpy, vill.isOnSurface());
                                    this.spawnpoints.add(ohome);
                                    if (!villageSet.contains(vill)) {
                                       villageSet.add(vill);
                                    }
                                 }
                              }
                           }
                        } catch (NoSuchItemException var19) {
                        }
                     }
                  }

                  Kingdom k = Kingdoms.getKingdom(this.getKingdomId());
                  if (k != null && k.isCustomKingdom()) {
                     Village v = Villages.getCapital(this.getKingdomId());
                     if (v == null && this.spawnpoints.isEmpty()) {
                        v = Villages.getFirstVillageForKingdom(this.getKingdomId());
                     }

                     if (v != null && v.getReputation(this) > -30 && !villageSet.contains(v)) {
                        boolean ok = true;
                        if (!this.isPaying() && !Servers.localServer.isChaosServer() && Zones.isVillagePremSpawn(v)) {
                           ok = false;
                        }

                        if (ok) {
                           try {
                              tpx = (short)v.getToken().getTileX();
                              tpy = (short)v.getToken().getTileY();
                           } catch (NoSuchItemException var18) {
                              logger.log(Level.WARNING, v.getName() + " no token.");
                              tpx = (short)v.getTokenX();
                              tpy = (short)v.getTokenY();
                           }
                        }
                     }
                  }
               }
            }

            Village hometown = null;
            if (Servers.localServer.entryServer) {
               tpx = 468;
               tpy = 548;
            }

            VolaTile t = Zones.getTileOrNull(tpx, tpy, true);
            if (t != null) {
               hometown = t.getVillage();
               if (hometown != null) {
                  if (!villageSet.contains(hometown)) {
                     boolean ok = true;
                     if (!this.isPaying() && Zones.isVillagePremSpawn(hometown)) {
                        ok = false;
                     }

                     if (ok) {
                        if (hometown.getReputation(this) > -30 && Zones.isGoodTileForSpawn(tpx, tpy, hometown.isOnSurface(), true)) {
                           Spawnpoint spa = new Spawnpoint(this.spnums++, hometown.getName(), tpx, tpy, true);
                           this.spawnpoints.add(spa);
                           villageSet.add(hometown);
                        }

                        if (!Servers.localServer.entryServer) {
                           short[] sp = this.getSpawnPointOutside(hometown);
                           tpx = sp[0];
                           tpy = sp[1];
                           if (Zones.isGoodTileForSpawn(tpx, tpy, hometown.isOnSurface())) {
                              Spawnpoint ohome = new Spawnpoint(this.spnums++, "Outside " + hometown.getName(), tpx, tpy, hometown.isOnSurface());
                              this.spawnpoints.add(ohome);
                              villageSet.add(hometown);
                           }
                        }
                     }
                  }
               } else if ((!Servers.localServer.randomSpawns || Items.getSpawnPoints().length == 0) && Zones.isGoodTileForSpawn(tpx, tpy, true, true)) {
                  Spawnpoint spa = new Spawnpoint(this.spnums++, "Start", tpx, tpy, true);
                  this.spawnpoints.add(spa);
               }
            } else if ((!Servers.localServer.randomSpawns || Items.getSpawnPoints().length == 0) && Zones.isGoodTileForSpawn(tpx, tpy, true, true)) {
               Spawnpoint spa = new Spawnpoint(this.spnums++, "Start", tpx, tpy, true);
               this.spawnpoints.add(spa);
            }

            int tents = 0;

            for(Item i : Items.getTents()) {
               if (i.getZoneId() > 0 && i.getLastOwnerId() == this.getWurmId()) {
                  if (tents >= 50) {
                     this.sendNormalServerMessage("You can only have 50 tent spawn points. Skipping the rest...");
                     break;
                  }

                  VolaTile tentTile = Zones.getTileOrNull(i.getTileX(), i.getTileY(), i.isOnSurface());
                  if (tentTile != null && tentTile.getKingdom() == this.getKingdomId()) {
                     boolean ok = true;
                     if (!this.isPaying() && Zones.isPremSpawnZoneAt(i.getTileX(), i.getTileY())) {
                        ok = false;
                     }

                     if (ok) {
                        Spawnpoint spa = new Spawnpoint(this.spnums++, "Tent " + i.getDescription(), (short)i.getTileX(), (short)i.getTileY(), i.isOnSurface());
                        this.spawnpoints.add(spa);
                        ++tents;
                     }
                  }
               }
            }

            if (this.citizenVillage != null) {
               if (hometown != this.citizenVillage && !villageSet.contains(this.citizenVillage)) {
                  boolean ok = true;
                  if (!this.isPaying() && Zones.isVillagePremSpawn(this.citizenVillage)) {
                     ok = false;
                  }

                  if (ok) {
                     villageSet.add(this.citizenVillage);
                     short[] sp = this.citizenVillage.getSpawnPoint();
                     tpx = sp[0];
                     tpy = sp[1];
                     if (Zones.isGoodTileForSpawn(tpx, tpy, this.citizenVillage.isOnSurface(), true)) {
                        Spawnpoint home = new Spawnpoint(this.spnums++, this.citizenVillage.getName(), tpx, tpy, this.citizenVillage.isOnSurface());
                        this.spawnpoints.add(home);
                     }

                     try {
                        sp = this.citizenVillage.getTokenCoords();
                        tpx = sp[0];
                        tpy = sp[1];
                        String spawnName = "Token of " + this.citizenVillage.getName();
                        if (!Zones.isGoodTileForSpawn(tpx, tpy, this.citizenVillage.isOnSurface(), true)) {
                           spawnName = spawnName + " (Warning: Steep)";
                        }

                        Spawnpoint token = new Spawnpoint(this.spnums++, spawnName, tpx, tpy, this.citizenVillage.isOnSurface());
                        this.spawnpoints.add(token);
                     } catch (NoSuchItemException var16) {
                     }

                     sp = this.getSpawnPointOutside(this.citizenVillage);
                     tpx = sp[0];
                     tpy = sp[1];
                     if (tpx > 0 && tpy > 0 && Zones.isGoodTileForSpawn(tpx, tpy, this.citizenVillage.isOnSurface())) {
                        Spawnpoint ohome = new Spawnpoint(
                           this.spnums++, "Outside " + this.citizenVillage.getName(), tpx, tpy, this.citizenVillage.isOnSurface()
                        );
                        this.spawnpoints.add(ohome);
                     }
                  }
               }

               Village[] alliances = this.citizenVillage.getAllies();

               for(int x = 0; x < alliances.length; ++x) {
                  if (!alliances[x].isDisbanding()
                     && this.spnums < 40
                     && Math.abs(this.getTileX() - alliances[x].getTokenX()) < 100
                     && Math.abs(this.getTileY() - alliances[x].getTokenY()) < 100
                     && !villageSet.contains(alliances[x])
                     && alliances[x].getReputation(this) > -30) {
                     boolean ok = true;
                     if (!this.isPaying() && Zones.isVillagePremSpawn(alliances[x])) {
                        ok = false;
                     }

                     if (ok) {
                        villageSet.add(alliances[x]);
                        short[] sp = alliances[x].getSpawnPoint();
                        tpx = sp[0];
                        tpy = sp[1];
                        if (Zones.isGoodTileForSpawn(tpx, tpy, alliances[x].isOnSurface(), true)) {
                           Spawnpoint home = new Spawnpoint(this.spnums++, alliances[x].getName(), tpx, tpy, alliances[x].isOnSurface());
                           this.spawnpoints.add(home);
                        }

                        if (!Servers.localServer.entryServer) {
                           sp = this.getSpawnPointOutside(alliances[x]);
                           tpx = sp[0];
                           tpy = sp[1];
                           if (tpx > 0 && tpy > 0 && Zones.isGoodTileForSpawn(tpx, tpy, alliances[x].isOnSurface())) {
                              Spawnpoint ohome = new Spawnpoint(this.spnums++, "Outside " + alliances[x].getName(), tpx, tpy, alliances[x].isOnSurface());
                              this.spawnpoints.add(ohome);
                           }
                        }
                     }
                  }
               }
            }

            if (this.spawnpoints.size() == 0) {
               for(int tries = 0; tries < 50; ++tries) {
                  tpx = (short)Zones.safeTileX(Server.rand.nextInt(Zones.worldTileSizeX));
                  tpy = (short)Zones.safeTileY(Server.rand.nextInt(Zones.worldTileSizeY));
                  if (Zones.isGoodTileForSpawn(tpx, tpy, true)) {
                     break;
                  }
               }

               Spawnpoint ohome = new Spawnpoint(this.spnums++, "Somewhere", tpx, tpy, true);
               this.spawnpoints.add(ohome);
            }

            logger.info("Calculating spawn points for " + this.getName() + " took " + (System.currentTimeMillis() - start) + "ms");
         }
      }
   }

   @Override
   public final boolean maySummonCorpse() {
      return System.currentTimeMillis() - this.saveFile.getLastDeath() > 300000L;
   }

   @Override
   public final long getTimeToSummonCorpse() {
      return Math.max(0L, this.saveFile.getLastDeath() + 300000L - System.currentTimeMillis());
   }

   @Override
   public void setDeathEffects(boolean freeDeath, int dtilex, int dtiley) {
      this.saveFile.died();
      this.setDead(true);
      this.removeWoundMod();
      this.getStatus().sendStateString();
      this.closeBank();
      if (this.isLit) {
         try {
            this.isLit = false;
            this.getCurrentTile().setHasLightSource(this, null);
         } catch (Exception var9) {
            if (logger.isLoggable(Level.FINE)) {
               logger.log(Level.FINE, "Problem checking tile for " + this);
            }
         }
      }

      this.movementScheme.haltSpeedModifier();
      this.getCommunicator().sendNormalServerMessage("You are halted on the way to the netherworld by a dark spirit, demanding knowledge.");
      double lMod = 0.25;
      if (this.isUndead()) {
         this.getCommunicator().sendNormalServerMessage("The spirit refuses to let you through and throws you back with extreme force!");
      } else {
         this.getCommunicator().sendNormalServerMessage("The spirit touches you and you feel drained.");
      }

      if (this.getDeity() != null && this.getDeity().isDeathProtector() && this.getFaith() >= 60.0F && this.getFavor() >= 30.0F && Server.rand.nextInt(4) > 0) {
         this.getCommunicator().sendNormalServerMessage(this.getDeity().name + " is with you and keeps you safe from the spirit's touch.");
         lMod = 0.125;
      }

      VolaTile tile = this.getCurrentTile();
      if (!this.suiciding
         && tile.getVillage() != null
         && (tile.getVillage() == this.getCitizenVillage() || this.getCitizenVillage() != null && this.getCitizenVillage().isAlly(tile.getVillage()))) {
         lMod *= 0.1;
      }

      if (this.getKingdomTemplateId() != 3 && this.getReputation() < 0) {
         lMod *= 5.0;
      }

      if (this.isDeathProtected() && Server.rand.nextInt(10) > 0) {
         this.getCommunicator().sendSafeServerMessage("The ancient symbol of the stone preserves your sanity and knowledge in the nether world.");
         lMod *= 0.5;
      }

      this.status.removeWounds();
      this.status.modifyStamina2(-100.0F);
      this.status.modifyHunger(-10000, 0.5F);
      this.status.modifyThirst(-10000.0F);
      if (!freeDeath) {
         if (this.battle != null) {
            this.battle.addCasualty(this);
         }

         boolean pvp = this.modifyFightSkill(dtilex, dtiley);
         this.modifyRanking();
         if (pvp) {
            this.addPvPDeath();
         }

         if (!this.isUndead()) {
            this.punishSkills(lMod, pvp);
         }
      }

      if (this.isTrading()) {
         this.getTrade().end(this, false);
      }

      if (this.isChampion()) {
         try {
            this.setRealDeath((byte)(this.saveFile.realdeath - 1));
            if (this.saveFile.realdeath <= 0) {
               this.revertChamp();
               HistoryManager.addHistory(this.getName(), "has fallen");
            }
         } catch (IOException var8) {
            logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
         }
      }

      tile.deleteCreature(this);
      this.destroyVisionArea();
      this.suiciding = false;
      this.saveFile.clearSpellResistances(this.communicator);
      if (!Servers.localServer.entryServer && this.getKingdomId() != 0 && Servers.localServer.HOMESERVER && Servers.localServer.KINGDOM != this.getKingdomId()
         )
       {
         ServerEntry entry = Servers.getClosestSpawnServer(this.getKingdomId());
         if (entry != null && entry.isAvailable(this.getPower(), this.isPaying())) {
            this.setMissionDeathEffects();
            this.setDeathProtected(false);
            logger.log(Level.INFO, "Transferring " + this.getName() + " to " + entry.name);
            this.communicator.sendDead();
            if (this.sendTransfer(
               Server.getInstance(),
               entry.INTRASERVERADDRESS,
               Integer.parseInt(entry.INTRASERVERPORT),
               entry.INTRASERVERPASSWORD,
               entry.id,
               -1,
               -1,
               true,
               false,
               this.getKingdomId()
            )) {
               return;
            }

            logger.log(Level.WARNING, this.getName() + " failed to transfer.");
            this.sendSpawnQuestion();
         } else {
            this.sendSpawnQuestion();
         }
      } else if (!this.isUndead()) {
         this.sendSpawnQuestion();
      }

      this.setMissionDeathEffects();
      this.setDeathProtected(false);
      this.trimAttackers(true);
      if (!this.isUndead()) {
         if (this.hasLink()) {
            this.communicator.sendDead();
         } else {
            Server.getInstance().addCreatureToRemove(this);
         }
      } else if (this.hasLink()) {
         this.sendSpawnQuestion();
      } else {
         Server.getInstance().addCreatureToRemove(this);
      }
   }

   private void addPvPDeath() {
      Players.getInstance().addPvPDeath(this.getWurmId());
      this.removePvPDeathTimer = 10800000L;
   }

   @Override
   public boolean isSuiciding() {
      return this.suiciding;
   }

   @Override
   public boolean mayAttack(Creature cret) {
      if (cret.getPower() == 0
         && cret.isPlayer()
         && !this.isOnPvPServer()
         && this.getKingdomTemplateId() == cret.getKingdomId()
         && this.getKingdomTemplateId() != 3
         && this.getKingdomId() == Servers.localServer.KINGDOM
         && (this.getCitizenVillage() == null || !this.getCitizenVillage().isEnemy(cret))
         && !this.isDuelOrSpar(cret)) {
         return false;
      } else if (this.opponent == cret) {
         return super.mayAttack(cret);
      } else {
         return cret.isPlayer() && !this.mayAttack ? this.mayAttack : super.mayAttack(cret);
      }
   }

   public static final float[] findRandomSpawnX(boolean checkBeach, boolean useSpawnStones) {
      if (useSpawnStones) {
         Item[] spawns = Items.getSpawnPoints();
         if (spawns.length > 0) {
            Item spawn = spawns[Server.rand.nextInt(spawns.length)];
            return new float[]{spawn.getPosX() - 12.0F + Server.rand.nextFloat() * 25.0F, spawn.getPosY() - 12.0F + Server.rand.nextFloat() * 25.0F};
         }
      }

      int tries = 0;

      while(tries++ < 1000000) {
         try {
            float posx = (float)((int)(Server.rand.nextFloat() * Zones.worldMeterSizeX));
            float posy = (float)((int)(Server.rand.nextFloat() * Zones.worldMeterSizeY));
            float posz = Zones.calculateHeight(posx, posy, true);
            if (posz > -1.0F) {
               short[] st = getTileSteepness((int)posx >> 2, (int)posy >> 2, true);
               if (st[1] < 20 && (!checkBeach || posz < 0.5F)) {
                  return new float[]{posx, posy};
               }
            }
         } catch (Exception var7) {
         }
      }

      float posx = (float)((int)(Server.rand.nextFloat() * Zones.worldMeterSizeX));
      float posy = (float)((int)(Server.rand.nextFloat() * Zones.worldMeterSizeY));
      return new float[]{posx, posy};
   }

   private void checkMayAttack() {
      Skill bodys = null;
      Skill sstrength = null;

      try {
         bodys = this.skills.getSkill(102);
      } catch (NoSuchSkillException var6) {
      }

      try {
         sstrength = this.skills.getSkill(105);
      } catch (NoSuchSkillException var5) {
      }

      if (bodys == null || sstrength == null) {
         this.mayAttack = false;
      } else if (Servers.localServer.HOMESERVER) {
         if (!(bodys.getKnowledge(0.0) < 20.5) && !(sstrength.getKnowledge(0.0) < 20.5)) {
            this.mayAttack = true;
         } else {
            this.mayAttack = false;
         }
      } else {
         try {
            bodys = this.skills.getSkill(1);
         } catch (NoSuchSkillException var4) {
         }

         if (bodys == null) {
            this.mayAttack = false;
         } else if (!this.isGuest() && !(bodys.getKnowledge(0.0) < 1.5)) {
            this.mayAttack = true;
         } else {
            this.mayAttack = false;
         }
      }

      if (this.isUndead()) {
         this.mayAttack = true;
      }
   }

   @Override
   public boolean maySteal() {
      return this.maySteal ? super.mayAttack(null) : this.maySteal;
   }

   private void checkMaySteal() {
      Skill bodys = null;

      try {
         bodys = this.skills.getSkill(104);
      } catch (NoSuchSkillException var3) {
      }

      if (bodys == null) {
         this.maySteal = false;
      } else if (!this.isGuest() && !(bodys.getKnowledge(0.0) < 20.5)) {
         this.maySteal = true;
      } else {
         this.maySteal = false;
      }
   }

   @Override
   public boolean isNewbie() {
      Skill bodys = null;

      try {
         bodys = this.skills.getSkill(1);
      } catch (NoSuchSkillException var3) {
      }

      if (bodys == null) {
         return true;
      } else {
         return this.isGuest() || bodys.getKnowledge() < 1.5;
      }
   }

   public int getRank() {
      return this.saveFile.getRank();
   }

   public int getMaxRank() {
      return this.saveFile.getMaxRank();
   }

   public long getLastLogin() {
      return this.saveFile.getLastLogin();
   }

   public long getLastLogout() {
      return this.saveFile.getLastLogout();
   }

   @Override
   public boolean isInvulnerable() {
      if (this.getPower() > 0) {
         return this.GMINVULN;
      } else {
         return this.getCommunicator().isInvulnerable();
      }
   }

   public boolean checkTileInvulnerability() {
      if (this.getCurrentTile() != null) {
         if (this.getCurrentTile().getKingdom() != this.getKingdomId()) {
            return false;
         }

         if (this.getCurrentTile().getVillage() != null) {
            if (this.getCurrentTile().getVillage().isCitizen(this) || this.getCurrentTile().getVillage().isAlly(this.citizenVillage)) {
               return true;
            }

            if (Servers.localServer.PVPSERVER) {
               return false;
            }

            if (this.getCurrentTile().getVillage().isEnemy(this.citizenVillage) || this.getCurrentTile().getVillage().getReputation(this) <= -30) {
               return false;
            }
         } else if (Servers.localServer.PVPSERVER) {
            return false;
         }

         Creature[] crets = null;

         for(int x = this.getCurrentTile().tilex - 10; x <= this.getCurrentTile().tilex + 10; ++x) {
            for(int y = this.getCurrentTile().tiley - 10; y <= this.getCurrentTile().tiley + 10; ++y) {
               if (x > 0 && y > 0 && x < Zones.worldTileSizeX && y < Zones.worldTileSizeY) {
                  VolaTile t = Zones.getTileOrNull(x, y, this.isOnSurface());
                  if (t != null) {
                     crets = t.getCreatures();

                     for(int c = 0; c < crets.length; ++c) {
                        if (!crets[c].isHuman() && crets[c].getAttitude(this) == 2) {
                           return true;
                        }
                     }
                  }
               }
            }
         }
      }

      return false;
   }

   public int getWarnings() {
      return this.saveFile.getWarnings();
   }

   public long getLastWarned() {
      return this.saveFile.getLastWarned();
   }

   public String getWarningStats(long lastWarned) {
      return this.saveFile.getWarningStats(lastWarned);
   }

   @Override
   public float getAlignment() {
      return this.saveFile.alignment;
   }

   @Override
   public float getFaith() {
      return this.isPaying() ? this.saveFile.faith : Math.min(30.0F, this.saveFile.faith);
   }

   @Override
   public Deity getDeity() {
      return this.saveFile.deity;
   }

   @Override
   public boolean maybeModifyAlignment(float modification) {
      boolean checkDirection = false;
      if (this.saveFile.getAlignment() > 0.0F && modification > 0.0F) {
         checkDirection = true;
      } else if (this.saveFile.getAlignment() < 0.0F && modification < 0.0F) {
         checkDirection = true;
      }

      if (checkDirection) {
         if (!MethodsReligion.mayReceiveAlignment(this)) {
            return false;
         }

         MethodsReligion.setReceivedAlignment(this);
      }

      try {
         this.saveFile.setAlignment(this.saveFile.getAlignment() + modification);
         return true;
      } catch (IOException var4) {
         logger.log(Level.WARNING, this.getName() + " " + var4.getMessage(), (Throwable)var4);
         return false;
      }
   }

   @Override
   public void setAlignment(float alignment) throws IOException {
      this.saveFile.setAlignment(alignment);
   }

   @Override
   public void modifyFaith(float modifier) {
      if (modifier > 0.0F || !this.isChampion()) {
         this.saveFile.modifyFaith(modifier);
         this.checkFaithTitles();
      }
   }

   @Override
   public void setFaith(float faith) throws IOException {
      this.saveFile.setFaith(faith);
      this.checkFaithTitles();
   }

   @Override
   public void setDeity(Deity deity) throws IOException {
      this.saveFile.setDeity(deity);
      if (deity == null) {
         this.getCommunicator().sendNormalServerMessage("You no longer follow a deity.");
      } else {
         this.getCommunicator().sendNormalServerMessage("You will now pray to " + deity.name + ".");
      }

      this.clearLinks();
      this.refreshAttitudes();
   }

   @Override
   public void setPriest(boolean priest) {
      if (!priest) {
         this.clearLinks();
      }

      this.saveFile.setPriest(priest);
   }

   @Override
   public boolean isPriest() {
      return this.saveFile.isPaying() && this.saveFile.isPriest;
   }

   @Override
   public void setCheated(String reason) {
      this.saveFile.setCheated(reason);
   }

   @Override
   public float getFavor() {
      return this.saveFile.favor;
   }

   @Override
   public float getFavorLinked() {
      float fav = this.saveFile.favor;
      if (this.links != null && this.links.size() > 0) {
         for(Creature c : this.links.values()) {
            if (c.isWithinDistanceTo(this, 20.0F)) {
               fav += Math.max(0.0F, c.getFavor() - 10.0F);
            }
         }
      }

      return fav;
   }

   @Override
   public void setFavor(float favor) throws IOException {
      this.saveFile.setFavor(favor);
   }

   @Override
   public void depleteFavor(float favorToRemove, boolean combatSpell) throws IOException {
      float sumremoved = 0.0F;
      if (this.links != null && this.links.size() > 0) {
         for(Creature c : this.links.values()) {
            if (c.isWithinDistanceTo(this, 20.0F) && sumremoved < favorToRemove && c.getFavor() > 0.0F) {
               float removed = Math.min(Math.max(0.0F, c.getFavor() - 10.0F), favorToRemove - sumremoved);
               sumremoved += removed;
               c.setFavor(c.getFavor() - removed);
            }
         }
      }

      this.setFavor(this.getFavor() - (favorToRemove - sumremoved));
      this.achievement(638, (int)Math.floor((double)(favorToRemove - sumremoved)));
      if (favorToRemove >= 50.0F) {
         this.achievement(619);
      }
   }

   @Override
   public boolean isTrader() {
      return true;
   }

   @Override
   public void makeEmoteSound() {
      this.lastMadeEmoteSound = System.currentTimeMillis();
   }

   @Override
   public boolean mayEmote() {
      return System.currentTimeMillis() - this.lastMadeEmoteSound > 5000L;
   }

   @Override
   public boolean isChampion() {
      return this.saveFile.realdeath > 0;
   }

   @Override
   public void setRealDeath(byte realdeathcounter) throws IOException {
      this.saveFile.setRealDeath(realdeathcounter);
   }

   @Override
   public boolean modifyChampionPoints(int championPointsModifier) {
      boolean isZero = this.saveFile.setChampionPoints((short)Math.max(0, this.saveFile.championPoints + championPointsModifier));
      if (isZero) {
         this.getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.CHAMP_POINTS);
      } else {
         this.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.CHAMP_POINTS, 100000, (float)this.getChampionPoints());
      }

      return isZero;
   }

   public void sendAddChampionPoints() {
      if (this.getChampionPoints() > 0) {
         this.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.CHAMP_POINTS, 100000, (float)this.getChampionPoints());
      }
   }

   public int getChampionPoints() {
      return this.saveFile.championPoints;
   }

   @Override
   public int getFatigueLeft() {
      return this.saveFile.power > 0 ? 20000 : this.saveFile.fatigueSecsLeft;
   }

   @Override
   public void decreaseFatigue() {
      if (this.saveFile.power <= 0) {
         this.saveFile.decreaseFatigue();
         this.lastDecreasedFatigue = System.currentTimeMillis();
      }
   }

   @Override
   public void setFatigue(int fatigueToAdd) {
      if (this.saveFile.power <= 0) {
         int toset = this.saveFile.hardSetFatigueSecs(fatigueToAdd);
         this.saveFile.setFatigueSecs(toset, this.saveFile.lastFatigue);
      }
   }

   public long getVersion() {
      return this.saveFile.version;
   }

   @Override
   public void mute(boolean mute, String reason, long expiry) {
      this.saveFile.setMuted(mute, reason, expiry);
      this.saveFile.mutesReceived = 0;
   }

   @Override
   public boolean isMute() {
      if (this.saveFile.isMute()) {
         if (this.saveFile.muteexpiry < System.currentTimeMillis()) {
            this.mute(false, "", 0L);
            return false;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public long getMoney() {
      if (Servers.localServer.id == Servers.loginServer.id) {
         return this.saveFile.money;
      } else {
         LoginServerWebConnection lsw = new LoginServerWebConnection();
         return lsw.getMoney(this);
      }
   }

   @Override
   public boolean addMoney(long moneyToAdd) throws IOException {
      if (Servers.localServer.id == Servers.loginServer.id) {
         this.saveFile.setMoney(this.saveFile.money + moneyToAdd);
         return true;
      } else {
         LoginServerWebConnection lsw = new LoginServerWebConnection();
         if (lsw.addMoney(
            this,
            this.getName(),
            moneyToAdd,
            DateFormat.getInstance().format(new Date()).replace(" ", "") + Server.rand.nextInt(100) + Servers.localServer.name
         )) {
            this.saveFile.setMoney(this.saveFile.money + moneyToAdd);
            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public boolean chargeMoney(long moneyToCharge) throws IOException {
      if (Servers.localServer.id == Servers.loginServer.id) {
         if (this.saveFile.money - moneyToCharge < 0L) {
            return false;
         } else {
            this.saveFile.setMoney(this.saveFile.money - moneyToCharge);
            return true;
         }
      } else {
         LoginServerWebConnection lsw = new LoginServerWebConnection();
         long newBalance = lsw.chargeMoney(this.name, moneyToCharge);
         if (newBalance >= 0L) {
            this.saveFile.setMoney(newBalance);
            return true;
         } else {
            logger.warning(this.getName() + " failed to withdraw money from the bank for moneyToCharge: " + moneyToCharge);
            this.getCommunicator().sendAlertServerMessage("Failed to contact the bank or the balance did not match. Please try later.");
            return false;
         }
      }
   }

   @Override
   public boolean setSex(byte sex, boolean creation) {
      try {
         this.status.setSex(sex);
         this.saveFile.setSex(sex);
         this.setVisible(false);
         if (this.hasLink()) {
            this.getCommunicator().sendChangeModelName(this.getWurmId(), this.getModelName());
         }

         this.setVisible(true);
         return true;
      } catch (IOException var4) {
         return false;
      }
   }

   @Override
   public boolean isClimbing() {
      return this.saveFile.climbing;
   }

   @Override
   public void setMoney(long newMoney) throws IOException {
      this.saveFile.setMoney(newMoney);
   }

   @Override
   public boolean acceptsInvitations() {
      return this.acceptsInvitations;
   }

   private void pollStealAttack() {
      if (this.secondsPlayed < 2.0F || this.secondsPlayed % 1000.0F == 0.0F) {
         if (this.secondsPlayed > 999.0F) {
            if (this.maySteal) {
               this.checkMaySteal();
               if (!this.maySteal) {
                  this.getCommunicator().sendAlertServerMessage("You may no longer steal things.");
               }
            } else {
               this.checkMaySteal();
               if (this.maySteal) {
                  this.getCommunicator().sendAlertServerMessage("You now feel confident enough to steal things.");
               }
            }

            if (this.mayAttack) {
               this.checkMayAttack();
               if (!this.mayAttack) {
                  this.getCommunicator().sendAlertServerMessage("You may no longer attack people.");
               }
            } else {
               this.checkMayAttack();
               if (this.mayAttack) {
                  this.getCommunicator().sendSafeServerMessage("You now feel confident enough to attack other people.");
               }
            }
         } else {
            this.checkMaySteal();
            this.checkMayAttack();
         }
      }
   }

   @Override
   public boolean pollAge() {
      return false;
   }

   @Override
   public long getFace() {
      return this.saveFile.face;
   }

   @Override
   public void setReputation(int reputation) {
      int oldrep = this.getReputation();
      if (this.getKingdomTemplateId() != 3) {
         if (this.getPower() > 0 && reputation < 0) {
            return;
         }

         int diff = oldrep - reputation;
         if (diff > 0 && this.getCitizenVillage() != null) {
            this.getCitizenVillage().setVillageRep(this.getCitizenVillage().getVillageReputation() + diff);
         }

         if (reputation < -200) {
            if (!Servers.isThisAChaosServer()) {
               if ((this.getCitizenVillage() == null || this.getCitizenVillage().getMayor().wurmId != this.getWurmId())
                  && !this.isKing()
                  && oldrep > reputation) {
                  try {
                     if (this.setKingdomId((byte)3)) {
                        this.getCommunicator().sendAlertServerMessage("You join the Horde of the Summoned.", (byte)2);
                        logger.info(this.getName() + " joins HOTS as their reputation is " + reputation);
                     }
                  } catch (IOException var5) {
                     logger.log(Level.WARNING, this.getName() + ":" + var5.getMessage(), (Throwable)var5);
                  }
               }

               if (oldrep > reputation && this.getKingdomTemplateId() != 3) {
                  this.getCommunicator()
                     .sendAlertServerMessage(
                        "Your reputation is decreasing deeply. It will take a very long time to recover. Eventually you should seek out the Horde of The Summoned."
                     );
               }
            }
         } else {
            if (oldrep >= 0 && reputation < 0) {
               this.getCommunicator().sendAlertServerMessage("You are now an outlaw. Other players may now kill you on sight!", (byte)4);
               this.refreshVisible();
               this.sendAttitudeChange();
               if (this.getCitizenVillage() != null
                  && this.getCitizenVillage().getMayor().wurmId != this.getWurmId()
                  && !this.isKing()
                  && !Servers.localServer.isChallengeOrEpicServer()) {
                  this.getCitizenVillage().removeCitizen(this);
               }
            } else if (reputation >= 0 && oldrep < 0) {
               this.getCommunicator().sendSafeServerMessage("You are no longer considered an outlaw.", (byte)2);
               this.refreshVisible();
               this.sendAttitudeChange();
            }

            if (oldrep >= -100 && reputation < -100) {
               this.getCommunicator().sendAlertServerMessage("Kingdom guards will now kill you on sight!", (byte)4);
            }

            if (oldrep >= -180 && reputation < -180) {
               this.getCommunicator().sendAlertServerMessage("You are very close to joining the Horde of the Summoned!", (byte)4);
            }
         }
      }

      this.saveFile.setReputation(reputation);
      this.refreshAttitudes();
   }

   @Override
   public int getReputation() {
      return this.saveFile.getReputation();
   }

   @Override
   public void addTitle(Titles.Title title) {
      if (this.saveFile.addTitle(title)) {
         this.getCommunicator().sendNormalServerMessage("You have just received the title '" + title.getName(this.isNotFemale()) + "'!", (byte)2);
         if (this.getTitles().length >= 15) {
            this.maybeTriggerAchievement(572, true);
         }

         if (this.getTitles().length >= 30) {
            this.maybeTriggerAchievement(579, true);
         }

         if (this.getTitles().length >= 60) {
            this.maybeTriggerAchievement(591, true);
         }
      }
   }

   @Override
   public void removeTitle(Titles.Title title) {
      if (this.saveFile.removeTitle(title)) {
         this.getCommunicator().sendNormalServerMessage("You have just lost the title '" + title.getName(this.isNotFemale()) + "'!", (byte)2);
      }

      if (this.getTitle() == title) {
         this.setTitle(null);
      }
   }

   public Titles.Title[] getTitles() {
      return this.saveFile.getTitles();
   }

   @Override
   public void setSecondTitle(@Nullable Titles.Title title) {
      this.saveFile.secondTitle = title;
      if (title != null && title.isRoyalTitle()) {
         this.setFinestAppointment();
      }

      if (!this.isDead() && this.getCurrentTile() != null) {
         this.getCurrentTile().makeInvisible(this);

         try {
            this.getCurrentTile().makeVisible(this);
         } catch (NoSuchPlayerException | NoSuchCreatureException var3) {
         }
      }

      this.getCommunicator().sendOwnTitles();
      if (title == null && this.getTitle() == null) {
         this.getCommunicator().sendNormalServerMessage("You will use no title for now.");
      } else {
         this.getCommunicator().sendNormalServerMessage("Your title is now " + this.getTitleString() + ".");
      }
   }

   @Override
   public void setTitle(@Nullable Titles.Title title) {
      this.saveFile.title = title;
      if (title != null && title.isRoyalTitle()) {
         this.setFinestAppointment();
      }

      if (!Features.Feature.COMPOUND_TITLES.isEnabled()) {
         if (!this.isDead() && this.getCurrentTile() != null) {
            this.getCurrentTile().makeInvisible(this);

            try {
               this.getCurrentTile().makeVisible(this);
            } catch (NoSuchCreatureException var3) {
            } catch (NoSuchPlayerException var4) {
            }
         }

         this.getCommunicator().sendOwnTitles();
         if (title != null) {
            if (title.isRoyalTitle()) {
               this.getCommunicator().sendNormalServerMessage("Your title is now " + this.saveFile.kingdomtitle + ".");
            } else {
               this.getCommunicator().sendNormalServerMessage("Your title is now " + this.saveFile.title.getName(this.isNotFemale()) + ".");
            }
         } else {
            this.getCommunicator().sendNormalServerMessage("You will use no title for now.");
         }
      }
   }

   @Override
   public Titles.Title getSecondTitle() {
      return this.saveFile.secondTitle;
   }

   @Override
   public Titles.Title getTitle() {
      return this.saveFile.title;
   }

   @Override
   public String getKingdomTitle() {
      return this.saveFile.kingdomtitle;
   }

   @Override
   public void setFinestAppointment() {
      if (this.saveFile.appointments != 0L || this.isAppointed()) {
         if (this.isKing()) {
            this.saveFile.kingdomtitle = King.getRulerTitle(this.getSex() == 0, this.getKingdomId());
         } else {
            Appointments apps = King.getCurrentAppointments(this.getKingdomId());
            if (apps != null) {
               Appointment app = apps.getFinestAppointment(this.saveFile.appointments, this.getWurmId());
               if (app != null) {
                  if (app.getType() == 1) {
                     this.saveFile.kingdomtitle = "Order of the " + app.getNameForGender(this.getSex());
                  } else {
                     this.saveFile.kingdomtitle = app.getNameForGender(this.getSex());
                  }
               }
            }
         }
      }
   }

   @Override
   public boolean hasPet() {
      return this.saveFile.pet != -10L;
   }

   @Override
   public boolean mayMute() {
      return this.getPower() >= 2 || this.saveFile.mayMute;
   }

   @Override
   public void setPet(long petId) {
      this.saveFile.setPet(petId);
   }

   @Override
   public Creature getPet() {
      return this.saveFile.pet > 0L ? Server.getInstance().getCreatureOrNull(this.saveFile.pet) : null;
   }

   public long getAlcoholAddiction() {
      return this.saveFile.alcoholAddiction;
   }

   public long getNicotineAddiction() {
      return this.saveFile.nicotineAddiction;
   }

   public float getAlcohol() {
      return this.saveFile.alcohol;
   }

   public float getNicotine() {
      return this.saveFile.nicotine;
   }

   public void setAlcohol(float newAlcohol) {
      this.saveFile.setAlcohol(newAlcohol);
   }

   public void setNicotine(float newNicotine) {
      this.saveFile.setNicotine(newNicotine);
   }

   @Override
   public boolean hasSleepBonus() {
      return this.saveFile.hasSleepBonus();
   }

   @Override
   public boolean isFrozen() {
      return this.frozen;
   }

   @Override
   public void toggleFrozen(Creature freezer) {
      if (this.frozen) {
         this.getMovementScheme().setFreezeMod(false);
         this.getCommunicator().sendSafeServerMessage(freezer.getName() + " gives you your movement back.");
      } else {
         this.getMovementScheme().setFreezeMod(true);
         this.getCommunicator().sendAlertServerMessage(freezer.getName() + " has paralyzed you!");
      }

      this.frozen = !this.frozen;
   }

   public void setFrozen(boolean _frozen) {
      if (this.frozen != _frozen) {
         if (this.frozen) {
            this.getMovementScheme().setFreezeMod(false);
            this.getCommunicator().sendSafeServerMessage("You may now move again.");
         } else {
            if (Constants.devmode) {
               this.getCommunicator().sendAlertServerMessage("You've been frozen!");
            }

            this.getMovementScheme().setFreezeMod(true);
         }

         this.frozen = _frozen;
      }
   }

   @Override
   protected void setLastVehicle(long _lastvehicle, byte _seatType) {
      this.saveFile.setLastVehicle(_lastvehicle);
   }

   @Override
   public Seat getSeat() {
      if (this.vehicle > -10L) {
         Vehicle vehic = Vehicles.getVehicleForId(this.vehicle);
         if (vehic != null) {
            for(int x = 0; x < vehic.seats.length; ++x) {
               if (vehic.seats[x].occupant == this.getWurmId()) {
                  return vehic.seats[x];
               }
            }
         }
      }

      return null;
   }

   @Override
   public void disembark(boolean teleport) {
      this.disembark(teleport, -1, -1);
   }

   @Override
   public boolean isOnFire() {
      return this.isOnFire;
   }

   @Override
   public byte getFireRadius() {
      return (byte)(this.isOnFire() ? 10 : 0);
   }

   @Override
   public double getVillageSkillModifier() {
      return this.villageSkillModifier;
   }

   @Override
   public void setVillageSkillModifier(double newModifier) {
      this.villageSkillModifier = newModifier;
   }

   public void checkLantern() {
      if (this.getVisionArea() != null && this.getVisionArea().isInitialized()) {
         if (this.getPower() >= 2) {
            if (this.gmLight && !this.isLit) {
               this.getCurrentTile().setHasLightSource(this, (byte)this.colorr, (byte)this.colorg, (byte)this.colorb, (byte)40);
               this.isLit = true;
               this.getCommunicator().sendNormalServerMessage("Someone blesses you with a personal light.");
            } else if (!this.gmLight && this.isLit) {
               this.getCurrentTile().setHasLightSource(this, null);
               this.isLit = false;
               this.getCommunicator().sendNormalServerMessage("Your light leaves you.");
            }
         } else if (this.isLit || !this.isVisible() || this.getPlayingTime() >= 86400000L && (!Servers.localServer.entryServer || !this.isPlayerAssistant())) {
            if ((!Servers.localServer.entryServer || !this.isPlayerAssistant()) && this.isLit && (this.getPlayingTime() > 86400000L || !this.isVisible())) {
               this.getCurrentTile().setHasLightSource(this, null);
               this.isLit = false;
               if (!this.isUndead()) {
                  this.getCommunicator().sendNormalServerMessage("The light leaves you.");
               }
            }
         } else if (this.getBestLightsource() == null) {
            this.getCurrentTile().setHasLightSource(this, (byte)80, (byte)80, (byte)80, (byte)5);
            this.isLit = true;
            if (!this.isUndead()) {
               this.getCommunicator().sendNormalServerMessage("The deities bless you with a faint light.");
            }
         }
      }
   }

   public void sendLantern(VirtualZone watcher) {
      if (this.isLit && this.isVisibleTo(watcher.getWatcher())) {
         watcher.sendAttachCreatureEffect(this, (byte)0, (byte)80, (byte)80, (byte)80, (byte)1);
      }
   }

   @Override
   public void setTheftWarned(boolean warned) {
      this.saveFile.setTheftwarned(warned);
   }

   @Override
   public void checkTheftWarnQuestion() {
      if (!this.saveFile.isTheftWarned) {
         if (this.question != null && this.question.getType() == 49) {
            return;
         }

         DropInfoQuestion quest = new DropInfoQuestion(this, "Theft prevention notification", "A word of warning!", -1L);
         quest.sendQuestion();
      }
   }

   @Override
   public void checkChallengeWarnQuestion() {
      if (Servers.localServer.isChallengeServer() && !this.hasFlag(27)) {
         ChallengeInfoQuestion quest = new ChallengeInfoQuestion(this);
         quest.sendQuestion();
      }
   }

   @Override
   public void setChallengeWarned(boolean warned) {
      this.setFlag(27, true);
   }

   @Override
   public void addEnemyPresense() {
      if (this.enemyPresenceCounter <= 0) {
         this.enemyPresenceCounter = 1;
         if (Servers.localServer.PVPSERVER
            && Servers.localServer.isChallengeOrEpicServer()
            && !Servers.localServer.HOMESERVER
            && this.getCurrentVillage() != null
            && this.currentTile != null
            && this.currentTile.getKingdom() == this.getKingdomId()) {
            this.setSecondsToLogout(3600);
            return;
         }

         this.setSecondsToLogout(300);
      }
   }

   @Override
   public void removeEnemyPresense() {
      if (this.enemyPresenceCounter > minEnemyPresence) {
         this.getCommunicator().sendSafeServerMessage("The feeling of insecurity and anger leaves you and you can focus better now.", (byte)4);
         this.getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.ENEMY);
      }

      this.enemyPresenceCounter = 0;
      if (Servers.localServer.PVPSERVER && Servers.localServer.isChallengeOrEpicServer() && !Servers.localServer.HOMESERVER) {
         this.secondsToLogout = 0;
         this.secondsToLogout = this.getSecondsToLogout();
      }
   }

   @Override
   public int getEnemyPresense() {
      return this.enemyPresenceCounter;
   }

   @Override
   public boolean hasNoReimbursement() {
      return this.saveFile.noReimbursementLeft;
   }

   @Override
   public boolean isDeathProtected() {
      return this.saveFile.deathProtected;
   }

   @Override
   public void setDeathProtected(boolean _deathProtected) {
      this.saveFile.setDeathProtected(_deathProtected);
      if (_deathProtected) {
         this.getCommunicator().sendAddStatusEffect(SpellEffectsEnum.DEATH_PROTECTION, Integer.MAX_VALUE);
      } else {
         this.getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.DEATH_PROTECTION);
      }
   }

   public void setLastChangedVillage(long _lastChanged) {
      this.saveFile.setLastChangedVillage(_lastChanged);
   }

   @Override
   public long mayChangeVillageInMillis() {
      return Math.max(this.saveFile.lastChangedVillage - System.currentTimeMillis() + 86400000L, 0L);
   }

   @Override
   public void saveFightMode(byte _mode) {
      this.saveFile.saveFightMode(_mode);
   }

   @Override
   public void loadAffinities() {
      Affinity[] affs = Affinities.getAffinities(this.getWurmId());
      if (affs.length > 0) {
         for(int x = 0; x < affs.length; ++x) {
            try {
               Skill s = this.skills.getSkill(affs[x].skillNumber);
               s.affinity = affs[x].number;
            } catch (NoSuchSkillException var4) {
            }
         }
      }
   }

   @Override
   public void increaseAffinity(int skillnumber, int aValue) {
      int lValue = aValue;
      Affinity[] affs = Affinities.getAffinities(this.getWurmId());
      if (affs.length > 0) {
         for(int x = 0; x < affs.length; ++x) {
            if (affs[x].skillNumber == skillnumber) {
               lValue += affs[x].number;
            }
         }
      }

      Affinities.setAffinity(this.getWurmId(), skillnumber, lValue, false);
   }

   @Override
   public void decreaseAffinity(int skillnumber, int value) {
      Affinities.decreaseAffinity(this.getWurmId(), skillnumber, value);
   }

   @Override
   public boolean isOnHostileHomeServer() {
      return !Servers.localServer.entryServer && Servers.localServer.HOMESERVER && this.getKingdomId() != Servers.localServer.KINGDOM;
   }

   public void checkAffinity() {
      if (this.saveFile.eligibleForAffinity()) {
         Affinity[] affs = Affinities.getAffinities(this.getWurmId());
         if (affs.length == 0) {
            Affinities.setAffinity(this.getWurmId(), SkillSystem.getRandomSkillNum(), 1, false);
         }
      }
   }

   public boolean isAspiringKing() {
      try {
         if (this.getCurrentAction().getNumber() == 353) {
            return true;
         }
      } catch (NoSuchActionException var2) {
      }

      return false;
   }

   @Override
   public boolean isSparring(Creature aOpponent) {
      return this.sparrers != null && this.sparrers.contains(aOpponent);
   }

   @Override
   public boolean isDuelling(Creature aOpponent) {
      return this.duellers != null && this.duellers.contains(aOpponent);
   }

   public void addDuellist(Creature aOpponent) {
      if (this.duellers == null) {
         this.duellers = new HashSet<>();
      }

      this.duellers.add(aOpponent);
   }

   public void addSparrer(Creature aOpponent) {
      if (this.sparrers == null) {
         this.sparrers = new HashSet<>();
      }

      this.sparrers.add(aOpponent);
   }

   public void removeDuellist(Creature aOpponent) {
      if (this.duellers != null) {
         this.duellers.remove(aOpponent);
         this.getCommunicator().sendNormalServerMessage("You may no longer duel " + aOpponent.getName() + " safely.");
      }
   }

   public void removeSparrer(Creature aOpponent) {
      if (this.sparrers != null) {
         this.sparrers.remove(aOpponent);
         this.getCommunicator().sendNormalServerMessage("You may no longer spar with " + aOpponent.getName() + " safely.");
      }
   }

   @Override
   public boolean isDuelOrSpar(Creature aOpponent) {
      if (aOpponent == this) {
         return true;
      } else if (this.isInOwnDuelRing()) {
         return true;
      } else {
         return this.isSparring(aOpponent) || this.isDuelling(aOpponent);
      }
   }

   @Override
   public void setChangedTileCounter() {
      this.getMovementScheme().touchFreeMoveCounter();
   }

   @Override
   public int getTutorialLevel() {
      return this.saveFile.tutorialLevel;
   }

   @Override
   public void setTutorialLevel(int newLevel) {
      this.saveFile.setTutorialLevel(newLevel);
      if (newLevel >= 12 && newLevel != 9999) {
         this.achievement(141);
      }
   }

   @Override
   public void missionFinished(boolean reward, boolean sendpopup) {
      OldMission m = OldMission.getMission(this.getTutorialLevel(), this.getKingdomId());
      if (reward && m != null && m.itemTemplateRewardId > 0) {
         Item inventory = this.getInventory();

         try {
            for(int x = 0; x < m.itemTemplateRewardNumbers; ++x) {
               Item i = createItem(m.itemTemplateRewardId, m.itemTemplateRewardQL);
               if (m.setNewbieItemByte) {
                  i.setAuxData((byte)1);
               }

               this.getCommunicator().sendSafeServerMessage("You receive " + i.getNameWithGenus() + ".");
               inventory.insertItem(i);
            }
         } catch (Exception var7) {
            logger.log(
               Level.WARNING,
               this.getName() + " failed reward " + m.itemTemplateRewardNumbers + ", " + m.itemTemplateRewardId + " for " + this.getTutorialLevel(),
               (Throwable)var7
            );
         }
      }

      this.setTutorialLevel(this.getTutorialLevel() + 1);
      if (sendpopup) {
         if (m != null && m.doneString.length() > 0) {
            SimplePopup popup = new SimplePopup(this, "Mission accomplished!", m.doneString);
            popup.sendQuestion();
         } else {
            SimplePopup popup = new SimplePopup(this, "Mission accomplished!", "You should go see if there are more instructions for you.");
            popup.sendQuestion();
         }
      }
   }

   @Override
   public String getCurrentMissionInstruction() {
      if (this.skippedTutorial()) {
         return "You skipped the tutorial and have to reactivate it.";
      } else if (this.getTutorialLevel() == 9999) {
         return "You have finished the tutorial.";
      } else {
         OldMission m = OldMission.getMission(this.getTutorialLevel(), this.getKingdomId());
         if (m != null) {
            StringBuilder toRet = new StringBuilder();
            toRet.append(m.title);
            toRet.append(": ");
            toRet.append(m.missionDescription);
            if (m.missionDescription2 != null) {
               toRet.append(m.missionDescription2);
            }

            if (m.missionDescription3 != null) {
               toRet.append(m.missionDescription3);
            }

            return toRet.toString();
         } else {
            return "";
         }
      }
   }

   public boolean isNearCave() {
      if (this.getVisionArea() != null) {
         return this.getVisionArea().isNearCave();
      } else {
         return !this.isOnSurface();
      }
   }

   @Override
   public byte getFarwalkerSeconds() {
      return this.farwalkerSeconds;
   }

   @Override
   public void setFarwalkerSeconds(byte seconds) {
      this.farwalkerSeconds = seconds;
      if (this.isPlayer()) {
         if (this.farwalkerSeconds > 0) {
            this.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.FARWALKER, this.farwalkerSeconds, (float)this.farwalkerSeconds);
         } else {
            this.getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.FARWALKER);
         }
      }
   }

   @Override
   public void activeFarwalkerAmulet(Item amulet) {
      if (this.getVehicle() == -10L) {
         this.setFarwalkerSeconds((byte)45);
         this.getMovementScheme().setFarwalkerMoveMod(true);
         this.getStatus().sendStateString();
         this.getCommunicator().sendNormalServerMessage("Your legs tingle and you feel unstoppable.");
         if (amulet.getTemplateId() == 527) {
            Server.getInstance().broadCastAction(this.getName() + " fiddles with a strange amulet.", this, 5);
            amulet.setQualityLevel(amulet.getQualityLevel() - 1.0F);
         } else {
            Server.getInstance().broadCastAction(this.getName() + " uses the " + amulet.getName() + ".", this, 5);
         }
      } else {
         this.getCommunicator().sendNormalServerMessage("Nothing happens.");
      }
   }

   @Override
   public void activePotion(Item potion) {
      if (potion.getTemplateId() == 5) {
         if (this.CRBonus < 5) {
            this.CRBonusCounter = 10;
            this.CRBonus = (byte)Math.min(5, this.CRBonus + 2);
            this.getCommunicator().sendNormalServerMessage("You feel nimble and sharp like a blade.");
            Server.getInstance().broadCastAction(this.getName() + " drinks a strange green-glowing potion.", this, 5);
            Items.destroyItem(potion.getWurmId());
            this.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.CR_BONUS, this.CRBonusCounter, (float)this.CRBonus);
            this.achievement(85);
         } else {
            this.getCommunicator().sendNormalServerMessage("You are already bursting with energy.");
         }
      } else if (potion.getTemplateId() == 836) {
         int skillNum = SkillSystem.getRandomSkillNum();
         Affinity[] affs = Affinities.getAffinities(this.getWurmId());

         for(boolean found = false; !found; skillNum = SkillSystem.getRandomSkillNum()) {
            boolean hasAffinity = false;

            for(Affinity affinity : affs) {
               if (affinity.getSkillNumber() == skillNum) {
                  hasAffinity = true;
                  if (affinity.getNumber() < 5) {
                     Affinities.setAffinity(this.getWurmId(), skillNum, affinity.getNumber() + 1, false);
                     String skillString = SkillSystem.getNameFor(skillNum);
                     found = true;
                     this.getCommunicator().sendSafeServerMessage("Aahh! You feel better at " + skillString + "!");
                  }
                  break;
               }
            }

            if (!found && !hasAffinity) {
               Affinities.setAffinity(this.getWurmId(), skillNum, 1, false);
               this.getCommunicator().sendSafeServerMessage("Aahh! You feel better somehow.. more skillful!");
               found = true;
            }
         }
      } else if (potion.getTemplateId() == 834) {
         if (this.getVehicle() != -10L) {
            this.getCommunicator()
               .sendNormalServerMessage(
                  "You suddenly notice a huge label on the potion with a crossed over boat and a crossed over horse, indicating that it will have no effect while mounted."
               );
            return;
         }

         SpellEffects effs = this.getSpellEffects();
         if (effs == null) {
            effs = this.createSpellEffects();
         }

         SpellEffect eff = effs.getSpellEffect((byte)72);
         if (eff == null) {
            this.getCommunicator().sendNormalServerMessage("You change appearance!");
            Server.getInstance().broadCastAction(this.getName() + " drinks a yellow potion.", this, 5);
            Items.destroyItem(potion.getWurmId());
            eff = new SpellEffect(this.getWurmId(), (byte)72, 100.0F, (int)(20.0F * potion.getQualityLevel()), (byte)9, (byte)0, true);
            effs.addSpellEffect(eff);
            int num = Server.rand.nextInt(12);

            try {
               switch(num) {
                  case 0:
                     if (this.status.getSex() == 0) {
                        this.setModelName("model.creature.humanoid.human.player.zombie.male");
                     } else if (this.status.getSex() == 1) {
                        this.setModelName("model.creature.humanoid.human.player.zombie.female");
                     }
                     break;
                  case 1:
                     this.setModelName(CreatureTemplateFactory.getInstance().getTemplate(88).getModelName());
                     break;
                  case 2:
                     this.setModelName(CreatureTemplateFactory.getInstance().getTemplate(87).getModelName());
                     break;
                  case 3:
                     this.setModelName(CreatureTemplateFactory.getInstance().getTemplate(12).getModelName());
                     break;
                  case 4:
                     this.setModelName(CreatureTemplateFactory.getInstance().getTemplate(10).getModelName());
                     break;
                  case 5:
                     this.setModelName(CreatureTemplateFactory.getInstance().getTemplate(75).getModelName());
                     break;
                  case 6:
                     this.setModelName(CreatureTemplateFactory.getInstance().getTemplate(55).getModelName());
                     break;
                  case 7:
                     this.setModelName(CreatureTemplateFactory.getInstance().getTemplate(11).getModelName());
                     break;
                  case 8:
                     this.setModelName(CreatureTemplateFactory.getInstance().getTemplate(23).getModelName());
                     break;
                  case 9:
                     this.setModelName(ItemTemplateFactory.getInstance().getTemplate(814).getModelName());
                     break;
                  case 10:
                     this.setModelName(ItemTemplateFactory.getInstance().getTemplate(190).getModelName());
                     break;
                  case 11:
                     this.setModelName(ItemTemplateFactory.getInstance().getTemplate(177).getModelName());
                     break;
                  default:
                     logger.warning("rand.nextInt(12) returned an unexepected value: " + num);
               }
            } catch (Exception var11) {
               logger.log(Level.WARNING, var11.getMessage(), (Throwable)var11);
            }
         } else {
            this.getCommunicator().sendNormalServerMessage("You have already changed appearance.");
         }
      }
   }

   @Override
   public void setModelName(String newModelName) {
      boolean wasVisible = this.isVisible();
      if (this.isVisible()) {
         this.setVisible(false);
      }

      this.saveFile.setModelName(newModelName);
      if (this.hasLink()) {
         this.getCommunicator().sendChangeModelName(this.getWurmId(), this.getModelName());
      }

      if (this.getPower() <= 0 || wasVisible) {
         this.setVisible(true);
      }
   }

   @Override
   public final String getModelName() {
      if (!this.saveFile.getModelName().equals("Human")) {
         return this.saveFile.getModelName();
      } else {
         StringBuilder s = new StringBuilder();
         s.append(this.template.getModelName());
         if (this.status.getSex() == 0) {
            s.append(".male");
         }

         if (this.status.getSex() == 1) {
            s.append(".female");
         }

         if (this.getKingdomId() != 0) {
            s.append('.');
            Kingdom kingdomt = Kingdoms.getKingdom(this.getKingdomId());
            if (kingdomt.getTemplate() != this.getKingdomId()) {
               s.append(Kingdoms.getSuffixFor(kingdomt.getTemplate()));
            }

            s.append(Kingdoms.getSuffixFor(this.getKingdomId()));
            if (this.status.disease > 0) {
               s.append("diseased");
            }
         } else if (this.status.disease > 0) {
            s.append(".diseased");
         }

         return s.toString();
      }
   }

   @Override
   public byte getCRCounterBonus() {
      return this.CRBonus;
   }

   @Override
   public String toString() {
      return "Player [id: " + this.getWurmId() + ", name: " + this.name + ']';
   }

   @Override
   public long getAppointments() {
      return this.saveFile.appointments;
   }

   @Override
   public void addAppointment(int aid) {
      this.saveFile.addAppointment(aid);
      this.setFinestAppointment();
   }

   public void addAppointment(Appointment a, Creature performer) {
      Communicator pc = performer.getCommunicator();
      Communicator c = this.getCommunicator();
      King k = King.getKing(this.getKingdomId());
      Appointments apps = King.getCurrentAppointments(this.getKingdomId());
      if (a != null && performer != null) {
         if (apps == null) {
            pc.sendNormalServerMessage("You have no titles to give out!");
         } else if (!King.isKing(performer.getWurmId(), this.getKingdomId())) {
            pc.sendNormalServerMessage("Only the ruler of " + this.getName() + "'s kingdom may appoint them!");
         } else if (!this.acceptsInvitations()) {
            pc.sendNormalServerMessage(this.getName() + " needs to type /invitations first.");
         } else {
            switch(a.getType()) {
               case 0:
                  if (this.hasAppointment(a.getId())) {
                     pc.sendNormalServerMessage(this.getName() + " has already been awarded the title of " + a.getNameForGender(this.getSex()) + ".");
                     return;
                  }

                  if (apps.getAvailTitlesForId(a.getId()) < 1) {
                     pc.sendNormalServerMessage("You may not award the " + a.getNameForGender(this.getSex()) + " to more people right now.");
                     return;
                  }

                  this.addAppointment(a.getId());
                  this.achievement(324);
                  apps.useTitle(a.getId());
                  break;
               case 1:
                  if (this.hasAppointment(a.getId())) {
                     pc.sendNormalServerMessage(this.getName() + " has already been appointed to the order of " + a.getNameForGender(this.getSex()) + ".");
                     return;
                  }

                  if (apps.getAvailOrdersForId(a.getId()) < 1) {
                     pc.sendNormalServerMessage("You may not award the " + a.getNameForGender(this.getSex()) + " to more people right now.");
                     return;
                  }

                  this.achievement(325);
                  this.addAppointment(a.getId());
                  apps.useOrder(a.getId());
                  break;
               case 2:
                  if (apps.officials[a.getId() - 1500] == this.getWurmId()) {
                     pc.sendNormalServerMessage(this.getName() + " is already appointed to the office of" + a.getNameForGender(this.getSex()) + ".");
                     return;
                  }

                  if (apps.isOfficeSet(a.getId())) {
                     pc.sendNormalServerMessage("The office as " + a.getNameForGender((byte)0) + " has already been set this week.");
                     return;
                  }

                  if (apps.officials[a.getId() - 1500] > 0L) {
                     Player op = Players.getInstance().getPlayerOrNull(apps.officials[a.getId() - 1500]);
                     if (op == null) {
                        PlayerInfo pinf = PlayerInfoFactory.getPlayerInfoWithWurmId(apps.officials[a.getId() - 1500]);
                        if (pinf != null) {
                           pc.sendNormalServerMessage("Unable to notify " + pinf.getName() + " of their removal from office.");
                        }
                     } else {
                        op.getCommunicator()
                           .sendNormalServerMessage(
                              "You are hereby notified that you have been removed from the office of " + a.getNameForGender(op.getSex()) + ".", (byte)2
                           );
                     }
                  }

                  apps.setOfficial(a.getId(), this.getWurmId());
                  this.achievement(323);
                  k.addAppointment(a);
                  break;
               default:
                  pc.sendNormalServerMessage("That appointment is invalid.");
                  return;
            }

            k.addAppointment(a);
            pc.sendNormalServerMessage(
               "You award the " + a.getNameForGender(this.getSex()) + " of " + Kingdoms.getNameFor(this.getKingdomId()) + " to " + this.getName() + ".",
               (byte)2
            );
            c.sendNormalServerMessage(
               "You have graciously been awarded the "
                  + a.getNameForGender(this.getSex())
                  + " of "
                  + Kingdoms.getNameFor(this.getKingdomId())
                  + " by "
                  + k.getRulerTitle()
                  + " "
                  + performer.getName()
                  + "!",
               (byte)2
            );
            HistoryManager.addHistory(
               this.getName(),
               "receives the "
                  + a.getNameForGender(this.getSex())
                  + " of "
                  + Kingdoms.getNameFor(this.getKingdomId())
                  + " from "
                  + k.getRulerTitle()
                  + " "
                  + performer.getName()
                  + "."
            );
         }
      }
   }

   @Override
   public void removeAppointment(int aid) {
      this.saveFile.removeAppointment(aid);
      this.setFinestAppointment();
   }

   @Override
   public boolean hasAppointment(int aid) {
      return this.saveFile.hasAppointment(aid);
   }

   @Override
   public String getAppointmentTitles() {
      Appointments apps = King.getCurrentAppointments(this.getKingdomId());
      if (apps != null) {
         StringBuilder buf = new StringBuilder();
         String titles = apps.getOffices(this.getWurmId(), this.getSex() == 0);
         if (titles.length() > 0) {
            buf.append(this.getName());
            buf.append(" is ");
            buf.append(titles);
            buf.append(" of ");
            buf.append(Kingdoms.getNameFor(this.getKingdomId()));
            buf.append(". ");
         }

         if (this.saveFile.appointments != 0L) {
            titles = apps.getTitles(this.saveFile.appointments, this.getSex() == 0);
            if (titles.length() > 0) {
               buf.append(this.getName());
               buf.append(" is ");
               buf.append(titles);
               buf.append(". ");
            }

            titles = apps.getOrders(this.saveFile.appointments, this.getSex() == 0);
            if (titles.length() > 0) {
               buf.append(this.getName());
               buf.append(" has received the ");
               buf.append(titles);
               buf.append(". ");
            }
         }

         return buf.toString();
      } else {
         return "";
      }
   }

   @Override
   public String getAnnounceString() {
      StringBuilder buf = new StringBuilder();
      buf.append(this.getName());
      if (this.isKing()) {
         buf.append(", ");
         buf.append(King.getRulerTitle(this.getSex() == 0, this.getKingdomId()));
      }

      buf.append(" of ");
      buf.append(Kingdoms.getNameFor(this.getKingdomId()));
      if (this.saveFile.appointments != 0L || this.isAppointed()) {
         Appointments apps = King.getCurrentAppointments(this.getKingdomId());
         if (apps != null) {
            buf.append(", ");
            String titles = apps.getOffices(this.getWurmId(), this.getSex() == 0);
            boolean added = false;
            if (titles.length() > 0) {
               buf.append(titles);
               added = true;
            }

            titles = apps.getTitles(this.saveFile.appointments, this.getSex() == 0);
            if (titles.length() > 0) {
               if (added) {
                  buf.append(", ");
               }

               buf.append(titles);
               added = true;
            }

            titles = apps.getOrders(this.saveFile.appointments, this.getSex() == 0);
            if (titles.length() > 0) {
               if (added) {
                  buf.append(", ");
               }

               buf.append("recipient of the ");
               buf.append(titles);
            }
         }
      }

      buf.append(". ");
      return buf.toString();
   }

   @Override
   public boolean isKing() {
      return King.isKing(this.getWurmId(), this.getKingdomId());
   }

   @Override
   public void clearRoyalty() {
      this.saveFile.clearAppointments();
   }

   public void sendPopup(String title, String message) {
      SimplePopup popup = new SimplePopup(this, title, message);
      popup.sendQuestion();
   }

   @Override
   public boolean isAppointed() {
      Appointments apps = King.getCurrentAppointments(this.getKingdomId());
      return apps != null ? apps.isAppointed(this.getWurmId()) : false;
   }

   @Override
   public int getPushCounter() {
      return this.pushCounter;
   }

   @Override
   public void setPushCounter(int val) {
      this.pushCounter = val;
   }

   @Override
   public int getMaxNumActions() {
      return this.maxNumActions;
   }

   @Override
   public boolean isPlayerAssistant() {
      return this.saveFile.isPlayerAssistant();
   }

   public boolean mayAppointPlayerAssistant() {
      return this.saveFile.mayAppointPlayerAssistant() || this.getPower() >= 1;
   }

   public void setPlayerAssistant(boolean assistant) {
      if (assistant) {
         this.addTitle(Titles.Title.PA);
      } else {
         this.removeTitle(Titles.Title.PA);
      }

      this.saveFile.setIsPlayerAssistant(assistant);
   }

   public void setMayAppointPlayerAssistant(boolean assistant) {
      this.saveFile.setMayAppointPlayerAssistant(assistant);
   }

   @Override
   public boolean seesPlayerAssistantWindow() {
      return (!Servers.localServer.HOMESERVER && !Servers.localServer.EPIC || this.isOnHostileHomeServer()) && !Servers.localServer.isChallengeServer()
         ? false
         : this.saveFile.seesPlayerAssistantWindow();
   }

   public boolean maySeeGVHelpWindow() {
      return (this.isPlayerAssistant() || this.mayMute() || this.getPower() > 0) && !Server.getInstance().isPS();
   }

   public boolean seesGVHelpWindow() {
      return this.maySeeGVHelpWindow() && !this.hasFlag(45);
   }

   @Override
   public final void setLastTaggedTerr(byte newKingdom) {
      this.saveFile.setLastTaggedTerr(newKingdom);
   }

   @Override
   public boolean mustChangeTerritory() {
      return Servers.localServer.isChallengeOrEpicServer()
         && this.isChampion()
         && this.getDeity() != null
         && System.currentTimeMillis() - this.saveFile.lastMovedBetweenKingdom > 259200000L;
   }

   @Override
   public byte getLastTaggedKingdom() {
      return this.saveFile.lastTaggedKindom;
   }

   public boolean togglePlayerAssistantWindow(boolean seeWindow) {
      if (this.saveFile.togglePlayerAssistantWindow(seeWindow)) {
         Players.getInstance().sendPAWindow(this);
         return true;
      } else {
         Players.getInstance().partPAChannel(this);
         return false;
      }
   }

   public boolean toggleGVHelpWindow(boolean seeWindow) {
      if (this.maySeeGVHelpWindow() && seeWindow) {
         Players.getInstance().sendGVHelpWindow(this);
         this.setFlag(45, false);
         return true;
      } else {
         this.setFlag(45, true);
         return false;
      }
   }

   @Override
   public int getMeditateX() {
      return this.lastMeditateX;
   }

   @Override
   public int getMeditateY() {
      return this.lastMeditateY;
   }

   @Override
   public void setMeditateX(int tilex) {
      this.lastMeditateX = tilex;
   }

   @Override
   public void setMeditateY(int tiley) {
      this.lastMeditateY = tiley;
   }

   @Override
   public Cultist getCultist() {
      return Cultist.getCultist(this.getWurmId());
   }

   @Override
   public void addLink(Creature creature) {
      if (this.links == null) {
         this.links = new HashMap<>();
      }

      this.links.put(creature.getWurmId(), creature);
      this.getCommunicator()
         .sendNormalServerMessage(
            creature.getName()
               + " links with your faith. You may now use "
               + creature.getHisHerItsString()
               + " favor to cast spells while "
               + creature.getHeSheItString()
               + " is within about 4 tiles."
         );
   }

   @Override
   public void removeLink(long wurmid) {
      if (this.links != null) {
         this.links.remove(wurmid);
      }
   }

   public void pollActions() {
      if (!this.loggedout && this.actions.poll(this) && this.isFighting()) {
         this.setFighting();
      }
   }

   @Override
   public int getNumLinks() {
      return this.links != null ? this.getLinks().length : 0;
   }

   @Override
   public void clearLinks() {
      if (this.links != null) {
         for(Creature c : this.links.values()) {
            c.setLinkedTo(-10L, false);
         }

         this.links.clear();
         this.getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.LINKS);
      }
   }

   @Override
   public void setLinkedTo(long wid, boolean linkback) {
      if (this.linkedTo != -10L && this.linkedTo != wid) {
         try {
            Creature c = Server.getInstance().getCreature(this.linkedTo);
            this.getCommunicator().sendNormalServerMessage("You are no longer linked to " + c.getName() + ".");
            c.getCommunicator().sendNormalServerMessage(this.getName() + " is no longer linked to you" + ".");
            if (linkback) {
               c.removeLink(this.getWurmId());
            }
         } catch (NoSuchCreatureException var7) {
            this.getCommunicator().sendNormalServerMessage("You are no longer linked.");
            this.linkedTo = -10L;
         } catch (NoSuchPlayerException var8) {
            this.getCommunicator().sendNormalServerMessage("You are no longer linked.");
            this.linkedTo = -10L;
         }
      }

      if (wid != -10L && this.linkedTo != wid) {
         try {
            Creature c = Server.getInstance().getCreature(wid);
            this.getCommunicator()
               .sendNormalServerMessage(
                  "You link your faith with "
                     + c.getName()
                     + " and "
                     + c.getHeSheItString()
                     + " may now use your favor to cast spells while you're within about 4 tiles."
               );
            if (linkback) {
               c.addLink(this);
            }

            this.linkedTo = wid;
         } catch (NoSuchCreatureException var5) {
            this.getCommunicator().sendNormalServerMessage("You fail to link.");
            this.linkedTo = -10L;
         } catch (NoSuchPlayerException var6) {
            this.getCommunicator().sendNormalServerMessage("You fail to link.");
            this.linkedTo = -10L;
         }
      } else {
         this.linkedTo = wid;
      }

      this.getStatus().sendStateString();
   }

   @Override
   public void disableLink() {
      this.setLinkedTo(-10L, true);
   }

   @Override
   public Creature[] getLinks() {
      if (this.links != null && this.links.size() > 0) {
         Set<Creature> toadd = new HashSet<>();

         for(Creature c : this.links.values()) {
            if (this.isWithinDistanceTo(c, 20.0F)) {
               toadd.add(c);
            }
         }

         return toadd.size() == 0 ? emptyCreatures : toadd.toArray(new Creature[toadd.size()]);
      } else {
         return emptyCreatures;
      }
   }

   @Override
   public void sendRemovePhantasms() {
      if (this.phantasms != null) {
         for(Long c : this.phantasms) {
            this.getCommunicator().sendDeleteCreature(c);
         }

         this.phantasms.clear();
      }
   }

   @Override
   public boolean isMissionairy() {
      return this.saveFile.priestType == 0;
   }

   @Override
   public long getLastChangedPriestType() {
      return this.saveFile.lastChangedPriestType;
   }

   @Override
   public void setPriestType(byte type) {
      this.saveFile.setNewPriestType(type, System.currentTimeMillis());
   }

   @Override
   public long getLastChangedJoat() {
      return this.saveFile.lastChangedJoat;
   }

   @Override
   public void resetJoat() {
      this.saveFile.setChangedJoat();
   }

   @Override
   public Team getTeam() {
      return this.team;
   }

   @Override
   public void setTeam(Team newTeam, boolean sendRemove) {
      if (newTeam == null) {
         this.mayInviteTeam = true;
         if (this.team != null) {
            this.team.creaturePartedTeam(this, sendRemove);
            this.getCommunicator().sendNormalServerMessage("You have been removed from the team.");
         }
      } else {
         if (this.team != null) {
            this.team.creaturePartedTeam(this, sendRemove);
         }

         newTeam.creatureJoinedTeam(this);
      }

      this.team = newTeam;
   }

   @Override
   public boolean isTeamLeader() {
      return this.team == null ? false : this.team.isTeamLeader(this);
   }

   @Override
   public boolean mayInviteTeam() {
      return this.mayInviteTeam;
   }

   @Override
   public void setMayInviteTeam(boolean mayInvite) {
      this.mayInviteTeam = mayInvite;
   }

   public void chatted() {
      this.lastChatted = System.currentTimeMillis();
   }

   public boolean isActiveInChat() {
      return System.currentTimeMillis() - this.lastChatted < 300000L;
   }

   public void chattedLocal() {
      this.lastChattedLocal = System.currentTimeMillis();
   }

   public boolean isActiveInLocalChat() {
      return System.currentTimeMillis() - this.lastChattedLocal < 300000L;
   }

   public boolean hasFreeTransfer() {
      return this.saveFile.hasFreeTransfer;
   }

   @Override
   public boolean hasSkillGain() {
      return this.saveFile.hasSkillGain;
   }

   @Override
   public boolean setHasSkillGain(boolean hasSkillGain) {
      this.getSkills().hasSkillGain = hasSkillGain;
      return this.saveFile.setHasSkillGain(hasSkillGain);
   }

   private final void setMissionDeathEffects() {
      MissionPerformer mp = MissionPerformed.getMissionPerformer(this.getWurmId());
      if (mp != null) {
         MissionPerformed[] perfs = mp.getAllMissionsPerformed();

         for(int x = 0; x < perfs.length; ++x) {
            if (!perfs[x].isInactivated() && !perfs[x].isCompleted() && !perfs[x].isFailed() && perfs[x].isStarted()) {
               Mission mission = perfs[x].getMission();
               if (mission != null && mission.isFailOnDeath()) {
                  perfs[x].setState(-1.0F, this.getWurmId());
               }
            }
         }
      }
   }

   @Override
   public void setDraggedItem(@Nullable Item dragged) {
      if (dragged == null) {
         this.lastStoppedDragging = System.currentTimeMillis();
      }

      this.movementScheme.setDraggedItem(dragged);
   }

   @Override
   public void setLastKingdom() {
      this.lastKingdom = this.getKingdomId();
   }

   @Override
   public long getChampTimeStamp() {
      return this.saveFile.championTimeStamp;
   }

   @Override
   public void becomeChamp() {
      Deity deity = this.getDeity();
      String deityName = "deity";
      if (deity != null) {
         deityName = deity.name;
      }

      try {
         if (!this.isPriest()) {
            this.setPriest(true);
            PlayerJournal.sendTierUnlock(this, PlayerJournal.getAllTiers().get((byte)10));
         }

         this.setFaith(99.99F);
         this.setFavor(99.99F);
         this.setChangedDeity();
         this.setRealDeath((byte)3);
         this.getSaveFile().setChampionTimeStamp();
         Skill bodyStrength = null;
         Skill stamina = null;
         Skill bodyControl = null;
         Skill mindlogical = null;
         Skill mindspeed = null;
         Skill soulstrength = null;
         Skill souldepth = null;
         Skill prayer = null;
         Skill exorcism = null;
         Skill channeling = null;

         try {
            prayer = this.skills.getSkill(10066);
            prayer.setKnowledge(Math.max(prayer.getKnowledge(), Math.min(80.0, prayer.getKnowledge() + 50.0)), false);
         } catch (NoSuchSkillException var23) {
            this.skills.learn(10066, 50.0F);
         }

         try {
            channeling = this.skills.getSkill(10067);
            this.getSaveFile().setChampChanneling((float)channeling.getKnowledge());
            channeling.setKnowledge(
               Math.max(channeling.getKnowledge(), Math.max(channeling.getKnowledge(), Math.min(80.0, channeling.getKnowledge() + 50.0))), false
            );
         } catch (NoSuchSkillException var22) {
            this.skills.learn(10067, 50.0F);
         }

         try {
            exorcism = this.skills.getSkill(10068);
            exorcism.setKnowledge(Math.max(exorcism.getKnowledge(), Math.min(80.0, exorcism.getKnowledge() + 50.0)), false);
         } catch (NoSuchSkillException var21) {
            this.skills.learn(10068, 50.0F);
         }

         try {
            bodyStrength = this.skills.getSkill(102);
            bodyStrength.setKnowledge(bodyStrength.getKnowledge() + 5.0, false);
         } catch (NoSuchSkillException var20) {
            this.skills.learn(102, 30.0F);
         }

         try {
            stamina = this.skills.getSkill(103);
            stamina.setKnowledge(stamina.getKnowledge() + 5.0, false);
         } catch (NoSuchSkillException var19) {
            this.skills.learn(103, 30.0F);
         }

         try {
            bodyControl = this.skills.getSkill(104);
            bodyControl.setKnowledge(bodyControl.getKnowledge() + 5.0, false);
         } catch (NoSuchSkillException var18) {
            this.skills.learn(104, 30.0F);
         }

         try {
            mindlogical = this.skills.getSkill(100);
            mindlogical.setKnowledge(mindlogical.getKnowledge() + 5.0, false);
         } catch (NoSuchSkillException var17) {
            this.skills.learn(100, 30.0F);
         }

         try {
            mindspeed = this.skills.getSkill(101);
            mindspeed.setKnowledge(mindspeed.getKnowledge() + 5.0, false);
         } catch (NoSuchSkillException var16) {
            this.skills.learn(101, 30.0F);
         }

         try {
            soulstrength = this.skills.getSkill(105);
            soulstrength.setKnowledge(soulstrength.getKnowledge() + 5.0, false);
         } catch (NoSuchSkillException var15) {
            this.skills.learn(105, 30.0F);
         }

         try {
            souldepth = this.skills.getSkill(106);
            souldepth.setKnowledge(souldepth.getKnowledge() + 5.0, false);
         } catch (NoSuchSkillException var14) {
            this.skills.learn(106, 30.0F);
         }

         this.getCommunicator().sendNormalServerMessage("You have now become a Champion of " + deityName + "!");
         Server.getInstance().broadCastAlert(this.getName() + " is now a Champion of " + deityName + "!", false);
         HistoryManager.addHistory(this.getName(), "is now a Champion of " + deityName);
         if (this.saveFile.lastTaggedKindom == 0) {
            this.setLastTaggedTerr(this.getKingdomId());
         } else {
            this.setLastTaggedTerr((byte)0);
         }

         this.checkInitialTitles();
      } catch (IOException var24) {
         logger.log(Level.WARNING, this.getName() + ":" + var24.getMessage(), (Throwable)var24);
      }
   }

   @Override
   public long getLastChangedCluster() {
      return this.saveFile.lastChangedCluster;
   }

   @Override
   public void setLastChangedCluster() {
      this.saveFile.lastChangedCluster = System.currentTimeMillis();
   }

   @Override
   public void revertChamp() {
      try {
         Deity deity = this.getDeity();
         String deityName = "deity";
         if (deity != null) {
            deityName = deity.name;
         }

         this.setFaith(50.0F);
         this.setFavor(50.0F);
         this.setRealDeath((byte)0);
         this.saveFile.switchChamp();
         this.saveFile.setChampionPoints((short)0);
         this.getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.FARWALKER);
         this.saveFile.setChampionTimeStamp();
         Skill bodyStrength = null;
         Skill stamina = null;
         Skill bodyControl = null;
         Skill mindlogical = null;
         Skill mindspeed = null;
         Skill soulstrength = null;
         Skill souldepth = null;
         Skill prayer = null;
         Skill exorcism = null;
         Skill channeling = null;

         try {
            prayer = this.skills.getSkill(10066);
            prayer.setKnowledge(Math.max(10.0, prayer.getKnowledge() - 50.0), false, true);
         } catch (NoSuchSkillException var23) {
            this.skills.learn(10066, 10.0F);
         }

         try {
            channeling = this.skills.getSkill(10067);
            channeling.setKnowledge(Math.max((double)this.saveFile.champChanneling, channeling.getKnowledge() - 50.0), false, true);
         } catch (NoSuchSkillException var22) {
            this.skills.learn(10067, 10.0F);
         }

         try {
            exorcism = this.skills.getSkill(10068);
            exorcism.setKnowledge(Math.max(10.0, exorcism.getKnowledge() - 50.0), false, true);
         } catch (NoSuchSkillException var21) {
            this.skills.learn(10068, 10.0F);
         }

         try {
            bodyStrength = this.skills.getSkill(102);
            bodyStrength.setKnowledge(bodyStrength.getKnowledge() - 6.0, false, true);
         } catch (NoSuchSkillException var20) {
            this.skills.learn(102, 20.0F);
         }

         try {
            stamina = this.skills.getSkill(103);
            stamina.setKnowledge(stamina.getKnowledge() - 6.0, false, true);
         } catch (NoSuchSkillException var19) {
            this.skills.learn(103, 20.0F);
         }

         try {
            bodyControl = this.skills.getSkill(104);
            bodyControl.setKnowledge(bodyControl.getKnowledge() - 6.0, false, true);
         } catch (NoSuchSkillException var18) {
            this.skills.learn(104, 20.0F);
         }

         try {
            mindlogical = this.skills.getSkill(100);
            mindlogical.setKnowledge(mindlogical.getKnowledge() - 6.0, false, true);
         } catch (NoSuchSkillException var17) {
            this.skills.learn(100, 20.0F);
         }

         try {
            mindspeed = this.skills.getSkill(101);
            mindspeed.setKnowledge(mindspeed.getKnowledge() - 6.0, false, true);
         } catch (NoSuchSkillException var16) {
            this.skills.learn(101, 20.0F);
         }

         try {
            soulstrength = this.skills.getSkill(105);
            soulstrength.setKnowledge(soulstrength.getKnowledge() - 6.0, false, true);
         } catch (NoSuchSkillException var15) {
            this.skills.learn(105, 20.0F);
         }

         try {
            souldepth = this.skills.getSkill(106);
            souldepth.setKnowledge(souldepth.getKnowledge() - 6.0, false, true);
         } catch (NoSuchSkillException var14) {
            this.skills.learn(106, 20.0F);
         }

         this.getCommunicator().sendNormalServerMessage("You are no longer a Champion of " + deityName + "!");
         Server.getInstance().broadCastAlert(this.getName() + " is no longer a Champion of " + deityName + "!", false);
         HistoryManager.addHistory(this.getName(), "is no longer a Champion of " + deityName);
         this.addTitle(Titles.Title.Champ_Previous);
      } catch (IOException var24) {
         logger.log(Level.WARNING, this.getName() + ":" + var24.getMessage(), (Throwable)var24);
      }
   }

   @Override
   public void setVotedKing(boolean voted) {
      this.saveFile.setVotedKing(voted);
   }

   @Override
   public boolean hasVotedKing() {
      return this.saveFile.votedKing;
   }

   @Override
   public boolean isCaredFor() {
      return Creatures.getInstance().isCreatureProtected(this.getWurmId());
   }

   @Override
   public long getCareTakerId() {
      return -10L;
   }

   @Override
   public int getNumberOfPossibleCreatureTakenCareOf() {
      return !this.isPaying() ? 1 : (int)(1.0 + this.getAnimalHusbandrySkillValue() / 10.0);
   }

   @Override
   public final void sendDeityEffectBonuses() {
      if (!Servers.localServer.PVPSERVER) {
         if (this.getDeity() != null) {
            if (Effectuator.getDeityWithStaminaRegain() == this.getDeity().number) {
               this.sendAddDeityEffectBonus(3);
            }

            if (Effectuator.getDeityWithCombatRating() == this.getDeity().number) {
               this.sendAddDeityEffectBonus(2);
            }

            if (Effectuator.getDeityWithSpeedBonus() == this.getDeity().number) {
               this.sendAddDeityEffectBonus(1);
            }

            if (Effectuator.getDeityWithFavorGain() == this.getDeity().number) {
               this.sendAddDeityEffectBonus(4);
            }
         }
      } else {
         if (Effectuator.getKingdomTemplateWithStaminaRegain() == this.getKingdomTemplateId()) {
            this.sendAddDeityEffectBonus(3);
         }

         if (Effectuator.getKingdomTemplateWithCombatRating() == this.getKingdomTemplateId()) {
            this.sendAddDeityEffectBonus(2);
         }

         if (Effectuator.getKingdomTemplateWithSpeedBonus() == this.getKingdomTemplateId()) {
            this.sendAddDeityEffectBonus(1);
         }

         if (Effectuator.getKingdomTemplateWithFavorGain() == this.getKingdomTemplateId()) {
            this.sendAddDeityEffectBonus(4);
         }
      }
   }

   @Override
   public final void sendAddDeityEffectBonus(int effectNumber) {
      switch(effectNumber) {
         case 1:
            this.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.DEITY_MOVEBONUS, 100000, 0.05F);
            this.getMovementScheme().setHasSpiritSpeed(true);
            break;
         case 2:
            this.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.DEITY_CRBONUS, 100000, 1.0F);
            this.setHasSpiritFervor(true);
            break;
         case 3:
            this.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.DEITY_STAMINAGAIN, 100000, 0.1F);
            this.setHasSpiritStamina(true);
            break;
         case 4:
            this.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.DEITY_FAVORGAIN, 100000, 0.05F);
            this.setHasSpiritFavorgain(true);
      }
   }

   @Override
   public final void setHasSpiritFervor(boolean hasSpiritFervor) {
      this.getCombatHandler().setHasSpiritFervor(hasSpiritFervor);
   }

   @Override
   public final void setHasSpiritFavorgain(boolean hasFavorGain) {
      this.hasSpiritFavorgain = hasFavorGain;
   }

   @Override
   public final void sendRemoveDeityEffectBonus(int effectNumber) {
      switch(effectNumber) {
         case 1:
            this.getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.DEITY_MOVEBONUS);
            this.getMovementScheme().setHasSpiritSpeed(false);
            break;
         case 2:
            this.getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.DEITY_CRBONUS);
            this.setHasSpiritFervor(false);
            break;
         case 3:
            this.getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.DEITY_STAMINAGAIN);
            this.setHasSpiritStamina(false);
            break;
         case 4:
            this.getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.DEITY_FAVORGAIN);
            this.setHasSpiritFavorgain(false);
      }
   }

   public final void setEigcClientId(String eigcIdUsed) {
      this.eigcId = eigcIdUsed;
   }

   public final String getEigcId() {
      return this.eigcId;
   }

   @Override
   public boolean mayUseLastGasp() {
      return this.getPositionZ() + this.getAltOffZ() <= 0.0F && this.getStatus().getStamina() < 5000 ? this.saveFile.mayUseLastGasp() : false;
   }

   @Override
   public void useLastGasp() {
      try {
         this.setClimbing(true);
         this.saveFile.useLastGasp();
         this.getStatus().modifyStamina(500.0F);
         this.getCommunicator().sendNormalServerMessage("You draw on your last inner resources and may drag yourself out of the water.");
      } catch (IOException var2) {
         logger.log(Level.WARNING, this.getName() + ":" + var2.getMessage());
      }
   }

   @Override
   public final boolean isUsingLastGasp() {
      return this.getPositionZ() + this.getAltOffZ() > 10.0F ? false : this.saveFile.isUsingLastGasp();
   }

   @Override
   public final void setKickedOffBoat(boolean kicked) {
      this.kickedOffBoat = kicked;
   }

   @Override
   public final boolean wasKickedOffBoat() {
      return this.kickedOffBoat;
   }

   public void disableKosPopups(int villageId) {
      this.kosPopups.add(villageId);
   }

   public boolean acceptsKosPopups(int villageId) {
      return this.kosPopups.contains(villageId);
   }

   @Override
   public boolean hasFingerEffect() {
      return this.hasFingerEffect;
   }

   @Override
   public void setHasFingerEffect(boolean eff) {
      this.hasFingerEffect = eff;
      this.sendHasFingerEffect();
   }

   @Override
   public void sendHasFingerEffect() {
      if (this.hasFingerEffect) {
         this.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.FINGER_FO_EFFECT, 100000, 100.0F);
      } else {
         this.getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.FINGER_FO_EFFECT);
      }
   }

   @Override
   public boolean hasFingerOfFoBonus() {
      Player[] players = Players.getInstance().getPlayers();

      for(Player p : players) {
         if (p.isWithinDistanceTo(this, 50.0F) && p.hasFingerEffect() && p.isFriendlyKingdom(this.getKingdomId())) {
            return true;
         }
      }

      return false;
   }

   public boolean hasCrownEffect() {
      return this.hasCrownEffect;
   }

   @Override
   public void setHasCrownEffect(boolean eff) {
      this.hasCrownEffect = eff;
   }

   @Override
   public void sendHasCrownEffect() {
      if (this.crownInfluence == 4) {
         this.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.CROWN_MAGRANON_EFFECT, 100000, 100.0F);
      } else if (this.crownInfluence == 0) {
         this.getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.CROWN_MAGRANON_EFFECT);
      }
   }

   @Override
   public void setCrownInfluence(int influence) {
      boolean send = false;
      if (this.crownInfluence == 0 && influence == 4) {
         send = true;
      }

      if (this.crownInfluence > 0 && influence == 0) {
         send = true;
      }

      this.crownInfluence = influence;
      if (send) {
         this.sendHasCrownEffect();
      }
   }

   @Override
   public boolean hasCrownInfluence() {
      return this.crownInfluence > 0;
   }

   private final void spreadCrownInfluence() {
      if (this.hasCrownEffect()) {
         Player[] players = Players.getInstance().getPlayers();

         for(Player p : players) {
            if (p.isWithinDistanceTo(this, 50.0F) && p.isFriendlyKingdom(this.getKingdomId())) {
               p.setCrownInfluence(4);
            }
         }
      }
   }

   public final void setMarkedByOrb(boolean marked) {
      this.markedByOrb = marked;
      this.sendMarkedByOrb();
   }

   public final void sendMarkedByOrb() {
      if (this.markedByOrb) {
         this.getCommunicator().sendAddSpellEffect(SpellEffectsEnum.ORB_DOOM_EFFECT, 20, 100.0F);
      } else {
         this.getCommunicator().sendRemoveSpellEffect(SpellEffectsEnum.ORB_DOOM_EFFECT);
      }
   }

   public final boolean isMarkedByOrb() {
      return this.markedByOrb;
   }

   @Override
   public int getEpicServerId() {
      return this.saveFile.epicServerId;
   }

   @Override
   public byte getEpicServerKingdom() {
      return this.saveFile.epicKingdom;
   }

   @Override
   public short getHotaWins() {
      return this.saveFile.getHotaWins();
   }

   @Override
   public void setHotaWins(short wins) {
      this.saveFile.setHotaWins(wins);
      this.getCommunicator().sendNormalServerMessage("You now have " + wins + " wins in the Hunt of the Ancients!");
   }

   @Override
   public final int getTeleportCounter() {
      return this.teleportCounter;
   }

   public final void setTeleportCounter(int counter) {
      this.teleportCounter = counter;
   }

   @Override
   public void achievement(int achievementId) {
      Achievements.triggerAchievement(this.getWurmId(), achievementId);
   }

   @Override
   public void achievement(int achievementId, int counterModifier) {
      Achievements.triggerAchievement(this.getWurmId(), achievementId, counterModifier);
   }

   @Override
   protected void addTileMovedDragging() {
      if (this.tilesMovedDragging++ > 25) {
         this.tilesMovedDragging = 0;
         this.achievement(65);
      }
   }

   @Override
   protected void addTileMovedRiding() {
      if (this.tilesMovedRiding++ > 4000) {
         this.tilesMovedRiding = 0;
         if (this.getSecondsPlayed() <= 3600.0F) {
            this.achievement(75);
         }

         this.achievement(76);
      }
   }

   @Override
   protected void addTileMoved() {
      if (this.tilesMoved++ > 250) {
         this.tilesMoved = 0;
         this.achievement(62);
      }

      if (!this.hasFlag(42) && this.tilesMoved == 14) {
         this.setFlag(42, true);
         if (ConchQuestion.isThisAdventureServer()) {
            try {
               this.conchticker = 60;
               Item conch = ItemFactory.createItem(1024, 80.0F + Server.rand.nextFloat() * 20.0F, "");
               this.getInventory().insertItem(conch, true);
            } catch (Exception var4) {
            }
         }
      }

      if (this.getDraggedItem() != null) {
         this.addTileMovedDragging();
      }

      if (this.getVehicle() != -10L) {
         Vehicle vehic = Vehicles.getVehicleForId(this.getVehicle());
         if (vehic != null) {
            try {
               Item item = Items.getItem(this.getVehicle());
               if (item.getTemplateId() == 539) {
                  if (vehic.getPilotId() == this.getWurmId()) {
                     this.addTileMovedDriving();
                  } else {
                     this.addTileMovedPassenger();
                  }
               }
            } catch (NoSuchItemException var3) {
            }

            if (vehic.isCreature() && vehic.getPilotId() == this.getWurmId()) {
               this.addTileMovedRiding();
            }
         }
      }
   }

   @Override
   protected void addTileMovedDriving() {
      if (this.tilesMovedDriving++ > 4000) {
         this.tilesMovedDriving = 0;
         this.achievement(73);
      }
   }

   @Override
   protected void addTileMovedPassenger() {
      if (this.tilesMovedPassenger++ > 4000) {
         this.tilesMovedPassenger = 0;
         this.achievement(74);
      }
   }

   @Override
   public void playPersonalSound(String soundName) {
      float offsetx = 4.0F * Server.rand.nextFloat();
      float offsety = 4.0F * Server.rand.nextFloat();

      try {
         Sound so = new Sound(
            soundName,
            this.getPosX() - 2.0F + offsetx,
            this.getPosY() - 2.0F + offsety,
            Zones.calculateHeight(this.getPosX(), this.getPosY(), this.isOnSurface()) + 1.0F,
            1.0F,
            1.0F,
            5.0F
         );
         this.getCommunicator().sendSound(so);
      } catch (NoSuchZoneException var5) {
      }

      if (soundName.equals("sound.fx.drumroll")) {
         this.getCommunicator().sendRarityEvent();
      }
   }

   @Override
   public int getKarma() {
      return this.saveFile.getKarma();
   }

   @Override
   public void setKarma(int newKarma) {
      this.saveFile.setKarma(newKarma);
      this.sendKarma();
   }

   @Override
   public void modifyKarma(int points) {
      if (points > 0 || this.getPower() <= 1) {
         this.saveFile.setKarma(points + this.getKarma());
         this.sendKarma();
      }
   }

   @Override
   public boolean fireTileLog() {
      return true;
   }

   @Override
   public void sendActionControl(String actionString, boolean start, int timeLeft) {
      VolaTile playerCurrentTile = this.getCurrentTile();
      this.sendToLoggers("Action string " + actionString + ", starting=" + start + ", time left " + timeLeft);
      if (playerCurrentTile != null) {
         playerCurrentTile.sendActionControl(this, actionString, start, timeLeft);
      }
   }

   public void setBlood(byte blood) {
      this.getSaveFile().setBlood(blood);
   }

   @Override
   public byte getBlood() {
      return this.getSaveFile().getBlood();
   }

   @Override
   public boolean hasAnyAbility() {
      return this.getSaveFile().abilities != 0L;
   }

   @Override
   public boolean hasAbility(int abilityBit) {
      if (this.getPower() >= 4 && Servers.isThisATestServer()) {
         return true;
      } else {
         return this.getSaveFile().abilities != 0L ? this.getSaveFile().isAbilityBitSet(abilityBit) : false;
      }
   }

   @Override
   public boolean hasFlag(int flagBit) {
      return this.getSaveFile().flags != 0L ? this.getSaveFile().isFlagSet(flagBit) : false;
   }

   @Override
   public int getAbilityTitleVal() {
      return this.getSaveFile().abilityTitle;
   }

   @Override
   public final String getAbilityTitle() {
      return this.getSaveFile().abilityTitle > -1 ? Abilities.getAbilityString(this.getSaveFile().abilityTitle) + " " : "";
   }

   @Override
   public final void setAbilityTitle(int newTitle) {
      this.getSaveFile().setCurrentAbilityTitle(newTitle);
      this.refreshVisible();
      this.getCommunicator().sendSafeServerMessage("You will henceforth be known as the " + this.getAbilityTitle() + this.getName());
   }

   @Override
   public void setFlag(int number, boolean value) {
      this.getSaveFile().setFlag(number, value);
   }

   @Override
   public void setAbility(int number, boolean value) {
      this.getSaveFile().setAbility(number, value);
   }

   @Override
   public void setTagItem(long itemId, String itemName) {
      this.taggedItemId = itemId;
      this.taggedItem = itemName;
   }

   @Override
   public String getTaggedItemName() {
      return this.taggedItem;
   }

   @Override
   public long getTaggedItemId() {
      return this.taggedItemId;
   }

   public boolean isKingdomChat() {
      if (this.isUndead()) {
         return false;
      } else {
         return !this.hasFlag(29);
      }
   }

   public boolean isTradeChannel() {
      if (this.isUndead()) {
         return false;
      } else {
         return !this.hasFlag(31);
      }
   }

   public boolean showKingdomStartMessage() {
      return !this.hasFlag(35);
   }

   public boolean showGlobalKingdomStartMessage() {
      return !this.hasFlag(36);
   }

   public boolean showTradeStartMessage() {
      return !this.hasFlag(37);
   }

   public boolean isVillageChatShowing() {
      return !this.hasFlag(38);
   }

   public boolean showVillageMessage() {
      return !this.hasFlag(39);
   }

   public boolean isAllianceChatShowing() {
      return !this.hasFlag(40);
   }

   public boolean showAllianceMessage() {
      return !this.hasFlag(41);
   }

   public boolean isGlobalChat() {
      if (Servers.localServer.isChallengeServer()) {
         return false;
      } else {
         return !this.hasFlag(30);
      }
   }

   @Override
   public void setScenarioKarma(int newKarma) {
      this.getSaveFile().setScenarioKarma(newKarma);
      this.sendScenarioKarma();
   }

   @Override
   public int getScenarioKarma() {
      return this.getSaveFile().getScenarioKarma();
   }

   @Override
   public boolean knowsKarmaSpell(int karmaSpellActionNum) {
      switch(karmaSpellActionNum) {
         case 547:
            return Abilities.isCrone(this) || Abilities.isOccultist(this);
         case 548:
            return Abilities.isNorn(this) || Abilities.isEnchanter(this);
         case 549:
            return Abilities.isSorceror(this) || Abilities.isSorceress(this);
         case 550:
         case 560:
            return Abilities.isEvocator(this) || Abilities.isFortuneTeller(this);
         case 551:
            return Abilities.isInquisitor(this) || Abilities.isHag(this);
         case 552:
         case 562:
            return Abilities.isConjurer(this) || Abilities.isMesmeriser(this);
         case 553:
            return Abilities.isValkyrie(this) || Abilities.isBerserker(this);
         case 554:
            return Abilities.isSiren(this) || Abilities.isSpellbinder(this);
         case 555:
            return Abilities.isWitchHunter(this) || Abilities.isSoothSayer(this);
         case 556:
         case 558:
            return Abilities.isDiviner(this) || Abilities.isIllusionist(this);
         case 557:
            return Abilities.isMedium(this) || Abilities.isSummoner(this);
         case 561:
         case 634:
            return Abilities.isDruid(this);
         case 629:
            return Abilities.isWorgMaster(this);
         case 630:
         case 631:
            return Abilities.isNecromancer(this) || Abilities.isWitch(this);
         case 686:
            return Abilities.isIncinerator(this);
         default:
            return false;
      }
   }

   public boolean allowIncomingPMs(String senderName, byte fromPower, long senderId, boolean aFriend, byte fromKingdom, int fromServer) {
      if (fromPower >= 2) {
         if (Servers.isThisATestServer()) {
            this.sendNormalServerMessage("Message from GM, so allowing.");
         }

         return true;
      } else if (this.isIgnored(senderId)) {
         if (Servers.isThisATestServer()) {
            if (this.isFriend(senderId)) {
               this.sendNormalServerMessage("Message from Friend (" + senderName + "), but you are ignoring them.");
            } else {
               this.sendNormalServerMessage("Message from " + senderName + ", but you are ignoring them.");
            }
         }

         return false;
      } else if (this.hasFlag(4) || !aFriend && !this.isFriend(senderId)) {
         if (this.hasFlag(1)) {
            if (Servers.isThisATestServer()) {
               this.sendNormalServerMessage("Message from " + senderName + ", but no PMs set, so disallowing.");
            }

            return false;
         } else if (!this.hasFlag(2) && fromKingdom != this.getKingdomId()) {
            if (Servers.isThisATestServer()) {
               this.sendNormalServerMessage(
                  "Message from " + senderName + ", wrong kingdom (theirs " + fromKingdom + " yours " + this.getKingdomId() + "), so disallowing."
               );
            }

            return false;
         } else if (!this.hasFlag(3) && Servers.getLocalServerId() != fromServer) {
            if (Servers.isThisATestServer()) {
               this.sendNormalServerMessage("Message from " + senderName + ", but XServer NOT set, so disallowing.");
            }

            return false;
         } else {
            return true;
         }
      } else {
         if (Servers.isThisATestServer()) {
            this.sendNormalServerMessage("Message from Friend, so allowing.");
         }

         return true;
      }
   }

   public boolean respondMGMTTab(String targetName, String optionalTargetNo) {
      String title = "CM";
      if (this.getPower() >= 2) {
         title = "GM";
      }

      String tno = optionalTargetNo.length() > 0 ? " " + optionalTargetNo : "";
      String msg = "Hello, " + title + " responding to your support call" + tno + ".";
      String tname = LoginHandler.raiseFirstLetter(targetName);
      if (this.getSaveFile().hasPMTarget(targetName)) {
         return true;
      } else {
         PlayerInfo pInfo = PlayerInfoFactory.createPlayerInfo(tname);
         if (pInfo != null) {
            try {
               pInfo.load();
               if (pInfo != null && pInfo.currentServer == Servers.getLocalServerId()) {
                  this.getSaveFile().addPMTarget(targetName, pInfo.wurmId);
                  this.showPM(this.getName(), targetName, msg, false);
                  Player p = Players.getInstance().getPlayer(targetName);
                  if (!p.sendPM(this.getName(), this.getWurmId(), msg)) {
                     this.showPMWarn(targetName, targetName + " is not currently available.");
                  }
               }
            } catch (IOException var9) {
               this.showPMWarn(targetName, targetName + " not found.");
            } catch (NoSuchPlayerException var10) {
               this.showPMWarn(targetName, targetName + " not online.");
            }
         }

         return true;
      }
   }

   public boolean respondGMTab(String targetName, String optionalTargetNo) {
      this.respondingAsGM = true;
      String title = "CM";
      if (this.getPower() >= 2) {
         title = "GM";
      }

      String tno = optionalTargetNo.length() > 0 ? " " + optionalTargetNo : "";
      this.sendPM(targetName, "Hello, " + title + " responding to your support call" + tno + ".", false, true);
      this.respondingAsGM = false;
      return true;
   }

   public void sendPM(String targetName, String _message, boolean _emote, boolean override) {
      String tname = LoginHandler.raiseFirstLetter(targetName);
      if (this.getSaveFile().hasPMTarget(targetName)) {
         this.sendPM((byte)2, tname, this.getSaveFile().getPMTargetId(targetName), _message, _emote, override);
      } else {
         PlayerInfo pInfo = PlayerInfoFactory.createPlayerInfo(tname);
         if (pInfo != null) {
            try {
               pInfo.load();
               this.sendPM((byte)2, tname, pInfo.wurmId, _message, _emote, override);
               return;
            } catch (IOException var9) {
            }
         }

         Creature npc = Creatures.getInstance().getNpc(tname);
         if (npc != null) {
            Npc cret = (Npc)npc;
            cret.getChatManager().addChat(this.getName(), _message);
            this.showPM(this.getName(), LoginHandler.raiseFirstLetter(targetName), _message, _emote);
         } else if (Servers.isThisLoginServer()) {
            this.sendPM((byte)2, tname, -10L, _message, _emote, override);
         } else {
            WcGlobalPM wgi = new WcGlobalPM(
               WurmId.getNextWCCommandId(),
               (byte)0,
               this.getPseudoPower(),
               this.getWurmId(),
               this.getName(),
               this.getKingdomId(),
               0,
               -10L,
               tname,
               false,
               _message,
               _emote,
               override
            );
            wgi.sendToLoginServer();
         }
      }
   }

   public void sendPM(byte reply, String targetName, long targetId, String _message, boolean _emote, boolean override) {
      if (targetId == -10L) {
         this.sendNormalServerMessage("Player not found with the name " + targetName + '.');
      } else {
         if (reply == 2) {
            if (!override && this.isIgnored(targetId)) {
               this.sendNormalServerMessage("You are ignoring " + targetName + " and can not pm.");
               this.getSaveFile().removePMTarget(targetName);
               return;
            }

            this.getSaveFile().addPMTarget(targetName, targetId);
            this.showPM(this.getName(), targetName, _message, _emote);
            if (this.isAFK()) {
               this.showPMWarn(targetName, "You are AFK");
            }

            boolean myFriend = this.isFriend(targetId);
            PlayerState pState = PlayerInfoFactory.getPlayerState(targetId);
            if (pState != null && pState.getServerId() == Servers.getLocalServerId()) {
               try {
                  Player p = Players.getInstance().getPlayer(targetName);
                  if (!p.sendPM(
                     this.getPseudoPower(),
                     this.getName(),
                     this.getWurmId(),
                     myFriend,
                     _message,
                     _emote,
                     this.getKingdomId(),
                     Servers.getLocalServerId(),
                     override
                  )) {
                     this.sendPM((byte)6, targetName, targetId, _message, _emote, override);
                  } else if (p.isAFK()) {
                     this.sendPM((byte)7, targetName, targetId, p.getAFKMessage(), true, override);
                  }
               } catch (NoSuchPlayerException var12) {
                  this.sendPM((byte)6, targetName, targetId, _message, _emote, override);
               }
            } else if (Servers.isThisLoginServer()) {
               PlayerInfo pInfo = PlayerInfoFactory.createPlayerInfo(targetName);
               if (pInfo != null) {
                  WcGlobalPM wgi = new WcGlobalPM(
                     WurmId.getNextWCCommandId(),
                     (byte)3,
                     this.getPseudoPower(),
                     this.getWurmId(),
                     this.getName(),
                     this.getKingdomId(),
                     pInfo.currentServer,
                     targetId,
                     targetName,
                     myFriend,
                     _message,
                     _emote,
                     override
                  );
                  wgi.sendToServer(pInfo.currentServer);
               }
            } else {
               WcGlobalPM wgi = new WcGlobalPM(
                  WurmId.getNextWCCommandId(),
                  (byte)3,
                  this.getPseudoPower(),
                  this.getWurmId(),
                  this.getName(),
                  this.getKingdomId(),
                  0,
                  targetId,
                  targetName,
                  myFriend,
                  _message,
                  _emote,
                  override
               );
               wgi.sendToLoginServer();
            }
         } else if (reply == 5) {
            this.showPMWarn(targetName, targetName + " is ignoring you. ");
            this.getSaveFile().removePMTarget(targetName);
         } else if (reply == 6) {
            this.showPMWarn(targetName, targetName + " is not currently available, please try again later. ");
            this.getSaveFile().removePMTarget(targetName);
         } else if (reply == 7) {
            this.showPM(targetName, targetName, _message, true);
         } else {
            this.sendNormalServerMessage("Unknown reply " + reply + ". ");
         }
      }
   }

   public boolean sendPM(
      byte power, String senderName, long senderId, boolean aFriend, String _message, boolean _emote, byte kingdomId, int serverId, boolean override
   ) {
      if (!this.hasLink()) {
         return false;
      } else if (this.getSaveFile().hasPMTarget(senderName)) {
         this.showPM(senderName, senderName, _message, _emote);
         return true;
      } else if (!override && !this.allowIncomingPMs(senderName, power, senderId, aFriend, kingdomId, serverId)) {
         return false;
      } else {
         this.showPM(senderName, senderName, _message, _emote);
         this.getSaveFile().addPMTarget(senderName, senderId);
         return true;
      }
   }

   public boolean sendPM(String senderName, long senderId, String _message) {
      if (!this.hasLink()) {
         return false;
      } else {
         this.showPM(senderName, senderName, _message, false);
         this.getSaveFile().addPMTarget(senderName, senderId);
         return true;
      }
   }

   public final void showPM(String senderName, String windowTitle, String _message, boolean _emote) {
      Message mess = new Message(this, (byte)3, "PM: " + windowTitle, (_emote ? "" : "<" + senderName + "> ") + _message);
      mess.setReceiver(this.getWurmId());
      if (_emote) {
         mess.setColorR(228);
         mess.setColorG(244);
         mess.setColorB(138);
      }

      Server.getInstance().addMessage(mess);
   }

   public final void showPMWarn(String windowTitle, String warnMessage) {
      Message msg = new Message(this, (byte)3, "PM: " + windowTitle, "<System> " + warnMessage);
      msg.setColorR(255);
      msg.setColorG(155);
      msg.setColorB(155);
      msg.setReceiver(this.getWurmId());
      Server.getInstance().addMessage(msg);
   }

   private void sendNormalServerMessage(String message) {
      this.sendServerMessage(message, -1, -1, -1);
   }

   private void sendServerMessage(String message, int red, int green, int blue) {
      Message msg = new Message(this, (byte)17, ":Event", message, red, green, blue);
      msg.setReceiver(this.getWurmId());
      Server.getInstance().addMessage(msg);
   }

   public void closePM(String targetName) {
      this.getSaveFile().removePMTarget(targetName);
   }

   private byte getPseudoPower() {
      byte power = (byte)this.getPower();
      if (power >= 2) {
         return power;
      } else {
         return (byte)(this.respondingAsGM ? 2 : 0);
      }
   }

   @Override
   public float getFireResistance() {
      return !this.hasAbility(34) && !this.hasAbility(35) && !this.hasAbility(44) ? 0.0F : 0.85F;
   }

   @Override
   public float getColdResistance() {
      return !this.hasAbility(12) && !this.hasAbility(31) ? 0.0F : 0.85F;
   }

   @Override
   public float getDiseaseResistance() {
      return !this.hasAbility(1) && !this.hasAbility(15) ? 0.0F : 0.85F;
   }

   @Override
   public float getPhysicalResistance() {
      return !this.hasAbility(3) && !this.hasAbility(16) ? 0.0F : 0.1F;
   }

   @Override
   public float getPierceResistance() {
      return !this.hasAbility(2) && !this.hasAbility(13) ? 0.0F : 0.85F;
   }

   @Override
   public float getSlashResistance() {
      return !this.hasAbility(9) && !this.hasAbility(27) ? 0.0F : 0.85F;
   }

   @Override
   public float getCrushResistance() {
      return !this.hasAbility(11) && !this.hasAbility(30) ? 0.0F : 0.85F;
   }

   @Override
   public float getBiteResistance() {
      return !this.hasAbility(10) && !this.hasAbility(29) ? 0.0F : 0.85F;
   }

   @Override
   public float getPoisonResistance() {
      return !this.hasAbility(7) && !this.hasAbility(20) ? 0.0F : 0.85F;
   }

   @Override
   public float getWaterResistance() {
      return !this.hasAbility(6) && !this.hasAbility(32) ? 0.0F : 0.85F;
   }

   @Override
   public float getAcidResistance() {
      return !this.hasAbility(8) && !this.hasAbility(24) ? 0.0F : 0.85F;
   }

   @Override
   public float getInternalResistance() {
      return !this.hasAbility(33) && !this.hasAbility(41) && !this.hasAbility(42) && !this.hasAbility(43) ? 0.0F : 0.85F;
   }

   @Override
   public float getFireVulnerability() {
      return !this.hasAbility(33) && !this.hasAbility(41) && !this.hasAbility(42) && !this.hasAbility(43) ? 0.0F : 1.1F;
   }

   @Override
   public float getColdVulnerability() {
      return !this.hasAbility(8) && !this.hasAbility(24) ? 0.0F : 1.1F;
   }

   @Override
   public float getDiseaseVulnerability() {
      return !this.hasAbility(6) && !this.hasAbility(32) ? 0.0F : 1.1F;
   }

   @Override
   public float getPhysicalVulnerability() {
      return !this.hasAbility(7) && !this.hasAbility(20) ? 0.0F : 1.05F;
   }

   @Override
   public float getPierceVulnerability() {
      return !this.hasAbility(10) && !this.hasAbility(29) ? 0.0F : 1.1F;
   }

   @Override
   public float getSlashVulnerability() {
      return !this.hasAbility(11) && !this.hasAbility(30) ? 0.0F : 1.1F;
   }

   @Override
   public float getCrushVulnerability() {
      return !this.hasAbility(9) && !this.hasAbility(27) ? 0.0F : 1.1F;
   }

   @Override
   public float getBiteVulnerability() {
      return !this.hasAbility(2) && !this.hasAbility(13) && !this.hasAbility(44) ? 0.0F : 1.1F;
   }

   @Override
   public float getPoisonVulnerability() {
      return !this.hasAbility(3) && !this.hasAbility(16) ? 0.0F : 1.1F;
   }

   @Override
   public float getWaterVulnerability() {
      return !this.hasAbility(1) && !this.hasAbility(15) ? 0.0F : 1.1F;
   }

   @Override
   public float getAcidVulnerability() {
      return !this.hasAbility(12) && !this.hasAbility(31) ? 0.0F : 1.1F;
   }

   @Override
   public float getInternalVulnerability() {
      return !this.hasAbility(34) && !this.hasAbility(35) ? 0.0F : 1.1F;
   }

   public boolean isSignedIn() {
      return this.getSaveFile().isSessionFlagSet(0);
   }

   public void setSignedIn(boolean _signedIn) {
      this.getSaveFile().setSessionFlag(0, _signedIn);
   }

   public boolean canSignIn() {
      return this.isPlayerAssistant() || this.mayMute();
   }

   public boolean isAFK() {
      return this.getSaveFile().isSessionFlagSet(1);
   }

   public void setAFK(boolean _afk) {
      this.getSaveFile().setSessionFlag(1, _afk);
   }

   public String getAFKMessage() {
      return this.afkMessage;
   }

   public void setAFKMessage(String newAFKMessage) {
      this.afkMessage = newAFKMessage;
   }

   public boolean isSendExtraBytes() {
      return true;
   }

   public void setSendExtraBytes(boolean _sendExtraBytes) {
      this.getSaveFile().setSessionFlag(2, _sendExtraBytes);
   }

   public void setClientVersion(String newVersion) {
      this.clientVersion = newVersion;
   }

   public void setClientSystem(String newSystem) {
      this.clientSystem = newSystem;
   }

   public final String getClientVersion() {
      return this.clientVersion;
   }

   public final String getClientSystem() {
      return this.clientSystem;
   }

   public void setNextActionRarity(byte newRarity) {
      this.nextActionRarity = newRarity;
   }

   public final boolean canUseFreeVillageTeleport() {
      return !this.hasFlag(21);
   }

   public void setUsedFreeVillageTeleport() {
      this.setFlag(21, true);
   }

   public boolean isCreationWindowOpen() {
      return this.getSaveFile().isSessionFlagSet(3);
   }

   public void setCreationWindowOpen(boolean isOpen) {
      this.getSaveFile().setSessionFlag(3, isOpen);
   }

   public void setPrivateMapPOIList(Set<MapAnnotation> annotations) {
      this.mapAnnotations = annotations;
   }

   private final boolean addPrivateMapPOI(MapAnnotation annotation) {
      if (this.mapAnnotations.size() < 500) {
         this.mapAnnotations.add(annotation);
         return true;
      } else {
         this.getCommunicator().sendNormalServerMessage("You can only have a maximum of 500 private annotations.");
         return false;
      }
   }

   public void addMapPOI(MapAnnotation annotation, boolean send) {
      switch(annotation.getType()) {
         case 0:
            if (this.addPrivateMapPOI(annotation) && send) {
               this.getCommunicator().sendMapAnnotations(new MapAnnotation[]{annotation});
            }
            break;
         case 1:
            if (this.citizenVillage != null && !this.citizenVillage.addVillageMapAnnotation(annotation, send) && send) {
               this.getCommunicator().sendNormalServerMessage("You can only have a maximum of 500 village annotations.");
            }
            break;
         case 2:
            if (this.citizenVillage != null && this.citizenVillage.getAllianceNumber() != 0) {
               PvPAlliance alliance = PvPAlliance.getPvPAlliance(this.citizenVillage.getAllianceNumber());
               if (alliance != null && !alliance.addAllianceMapAnnotation(annotation, send) && send) {
                  this.getCommunicator().sendNormalServerMessage("You can only have a maximum of 500 alliance annotations.");
               }
            }
            break;
         default:
            logger.log(Level.WARNING, "Trying to add annotation of unknown type: " + annotation.getType());
      }
   }

   public final void removeMapPOI(MapAnnotation annotation) {
      switch(annotation.getType()) {
         case 0:
            this.removePrivatePOI(annotation);
            break;
         case 1:
            if (this.citizenVillage != null) {
               this.citizenVillage.removeVillageMapAnnotation(annotation);
            }
            break;
         case 2:
            if (this.citizenVillage != null && this.citizenVillage.getAllianceNumber() != 0) {
               PvPAlliance alliance = PvPAlliance.getPvPAlliance(this.citizenVillage.getAllianceNumber());
               if (alliance != null) {
                  alliance.removeAllianceMapAnnotation(annotation);
               }
            }
            break;
         default:
            logger.log(Level.WARNING, "Trying to remove annotation of unkown type: " + annotation.getType());
      }
   }

   private void removePrivatePOI(MapAnnotation annotation) {
      if (this.mapAnnotations.contains(annotation)) {
         this.mapAnnotations.remove(annotation);

         try {
            MapAnnotation.deleteAnnotation(annotation.getId());
         } catch (IOException var3) {
            logger.log(Level.WARNING, "Error when deleting annotation: " + annotation.getId() + " : " + var3.getMessage(), (Throwable)var3);
         }
      }
   }

   private MapAnnotation getPrivateAnnotationById(long id) {
      for(MapAnnotation anno : this.mapAnnotations) {
         if (anno.getId() == id) {
            return anno;
         }
      }

      return null;
   }

   private MapAnnotation getVillageAnnotationById(long id) {
      for(MapAnnotation anno : this.getVillageAnnotations()) {
         if (anno.getId() == id) {
            return anno;
         }
      }

      return null;
   }

   private MapAnnotation getAllianceAnnotationById(long id) {
      for(MapAnnotation anno : this.getAllianceAnnotations()) {
         if (anno.getId() == id) {
            return anno;
         }
      }

      return null;
   }

   public final MapAnnotation getAnnotation(long id, byte type) {
      switch(type) {
         case 0:
            return this.getPrivateAnnotationById(id);
         case 1:
            return this.getVillageAnnotationById(id);
         case 2:
            return this.getAllianceAnnotationById(id);
         default:
            logger.log(Level.WARNING, "There is no such annotation type : " + type);
            return null;
      }
   }

   public final Set<MapAnnotation> getPrivateMapAnnotations() {
      return this.mapAnnotations;
   }

   public final Set<MapAnnotation> getVillageAnnotations() {
      Set<MapAnnotation> annos = new HashSet<>();
      if (this.citizenVillage != null) {
         annos.addAll(this.citizenVillage.getVillageMapAnnotations());
      }

      return annos;
   }

   public final Set<MapAnnotation> getAllianceAnnotations() {
      Set<MapAnnotation> annos = new HashSet<>();
      if (this.citizenVillage != null && this.citizenVillage.getAllianceNumber() != 0) {
         PvPAlliance alliance = PvPAlliance.getPvPAlliance(this.citizenVillage.getAllianceNumber());
         if (alliance != null) {
            annos.addAll(alliance.getAllianceMapAnnotations());
         }
      }

      return annos;
   }

   public final Set<MapAnnotation> getAllMapAnnotations() {
      Set<MapAnnotation> annos = new HashSet<>();
      annos.addAll(this.mapAnnotations);
      Village vill = Villages.getVillageForCreature(this);
      if (vill != null) {
         annos.addAll(vill.getVillageMapAnnotations());
         if (vill.getAllianceNumber() != 0) {
            PvPAlliance alliance = PvPAlliance.getPvPAlliance(vill.getAllianceNumber());
            if (alliance != null) {
               annos.addAll(alliance.getAllianceMapAnnotations());
            }
         }
      }

      return annos;
   }

   public void createNewMapPOI(String poiName, byte type, int x, int y, String server, byte icon) {
      long poiPos = BigInteger.valueOf((long)x).shiftLeft(32).longValue() + (long)y;
      long ownerID = 0L;
      switch(type) {
         case 0:
            ownerID = this.getWurmId();
            break;
         case 1:
            if (this.citizenVillage == null) {
               return;
            }

            ownerID = (long)this.citizenVillage.getId();
            break;
         case 2:
            if (this.citizenVillage != null) {
               if (this.citizenVillage.getAllianceNumber() == 0) {
                  return;
               }

               ownerID = (long)this.citizenVillage.getAllianceNumber();
            }
            break;
         default:
            logger.log(Level.WARNING, "Trying to add annotation of unknown type: " + type);
            return;
      }

      try {
         MapAnnotation mapAnnotation = MapAnnotation.createNew(poiName, type, poiPos, ownerID, server, icon);
         this.addMapPOI(mapAnnotation, true);
      } catch (IOException var12) {
         logger.log(Level.WARNING, "Error when creating new map annotation: " + var12.getMessage(), (Throwable)var12);
      }
   }

   public void sendAllMapAnnotations() {
      Set<MapAnnotation> anno = this.getAllMapAnnotations();
      MapAnnotation[] annotations = new MapAnnotation[anno.size()];
      anno.toArray(annotations);
      this.getCommunicator().sendMapAnnotations(annotations);
   }

   public final boolean isAllowedToEditVillageMap() {
      if (this.citizenVillage != null) {
         if (this.citizenVillage.getMayor().getId() == this.getWurmId()) {
            return true;
         }

         Citizen citizen = this.citizenVillage.getCitizen(this.getWurmId());
         if (citizen != null) {
            return citizen.getRole().mayManageMap();
         }
      }

      return false;
   }

   public final boolean isAllowedToEditAllianceMap() {
      if (this.isAllowedToEditVillageMap() && this.citizenVillage != null && this.citizenVillage.getAllianceNumber() != 0) {
         PvPAlliance alliance = PvPAlliance.getPvPAlliance(this.citizenVillage.getAllianceNumber());
         if (alliance != null) {
            Citizen cit = this.citizenVillage.getCitizen(this.getWurmId());
            if (cit != null) {
               return cit.getRole().isDiplomat();
            }
         }
      }

      return false;
   }

   public void sendClearVillageMapAnnotations() {
      this.getCommunicator().sendClearMapAnnotationsOfType((byte)1);
   }

   public void sendClearAllianceMapAnnotations() {
      this.getCommunicator().sendClearMapAnnotationsOfType((byte)2);
   }

   @Override
   public long getMoneyEarnedBySellingLastHour() {
      return this.saveFile.getMoneyEarnedBySellingLastHour();
   }

   @Override
   public void addMoneyEarnedBySellingLastHour(long money) {
      this.saveFile.addMoneyEarnedBySellingLastHour(money);
      if (this.messages++ < 10) {
         this.getCommunicator().sendSafeServerMessage("You receive " + money + " irons. Your bank account will be updated shortly.");
      } else {
         this.getCommunicator().sendSafeServerMessage("You receive " + money + " irons.");
      }
   }

   public void addPlayerVote(PlayerVote pv) {
      this.playerQuestionVotes.put(pv.getQuestionId(), pv);
      this.checkCanVote();
   }

   public void removePlayerVote(int questionId) {
      if (this.playerQuestionVotes.containsKey(questionId)) {
         this.playerQuestionVotes.remove(questionId);
      }

      this.checkCanVote();
   }

   public PlayerVote getPlayerVote(int qId) {
      return this.playerQuestionVotes.get(qId);
   }

   public boolean containsPlayerVote(int qId) {
      return this.playerQuestionVotes.containsKey(qId);
   }

   public boolean hasVoted(int aQuestionId) {
      return this.containsPlayerVote(aQuestionId) ? this.getPlayerVote(aQuestionId).hasVoted() : false;
   }

   public void getVotes() {
      this.askedForVotes = true;
      if (Servers.isThisLoginServer()) {
         PlayerVote[] pvs = PlayerVotes.getPlayerVotes(this.getWurmId());
         this.setVotes(pvs);
      } else {
         int[] ids = VoteQuestions.getVoteQuestionIds(this);
         if (ids.length == 0) {
            this.playerQuestionVotes.clear();
            this.gotVotes = true;
         } else {
            WcVoting wv = new WcVoting(this.getWurmId(), VoteQuestions.getVoteQuestionIds(this));
            wv.sendToLoginServer();
         }
      }
   }

   public void setVotes(PlayerVote[] playerVotes) {
      this.gotVotes = false;
      this.playerQuestionVotes.clear();

      for(PlayerVote pv : playerVotes) {
         this.addPlayerVote(pv);
      }

      this.fillVotes();
      this.gotVotes = true;
   }

   public void fillVotes() {
      this.canVote = false;
      VoteQuestion[] vqs = VoteQuestions.getVoteQuestions(this);

      for(VoteQuestion vq : vqs) {
         if (!this.containsPlayerVote(vq.getQuestionId())) {
            PlayerVote pv = new PlayerVote(this.getWurmId(), vq.getQuestionId());
            this.addPlayerVote(pv);
            this.canVote = true;
         }
      }
   }

   public void checkCanVote() {
      this.canVote = false;
      VoteQuestion[] vqs = VoteQuestions.getVoteQuestions(this);

      for(VoteQuestion vq : vqs) {
         if (vq.canVote(this) && !this.hasVoted(vq.getQuestionId())) {
            this.canVote = true;
         }
      }
   }

   public void gotVotes(boolean aNewOne) {
      this.gotVotes = false;
      if (this.canVote) {
         if (aNewOne) {
            this.getCommunicator().sendServerMessage("A new Poll is just available that you can vote on, use /poll to access it.", 250, 150, 250);
         } else {
            this.getCommunicator().sendServerMessage("There is a Poll available that you can vote on, use /poll to access it.", 250, 150, 250);
         }
      }
   }

   public void toggleGMLight() {
      this.gmLight = !this.gmLight;
   }

   public boolean askingFriend() {
      return this.askingFriend;
   }

   public String waitingForFriend() {
      return this.waitingForFriendName;
   }

   public void setAddFriendTimout(int newCount, Friend.Category friendsCategory) {
      this.waitingForFriendCount = newCount;
      this.waitingForFriendCategory = friendsCategory;
   }

   public void setAskFriend(String friendsName, Friend.Category friendsCategory) {
      this.waitingForFriendName = friendsName;
      this.waitingForFriendCount = 300;
      this.askingFriend = true;
      this.waitingForFriendCategory = friendsCategory;
   }

   public byte remoteAddFriend(String aFriendsName, byte aKingdom, byte aReply, boolean xServer, boolean xKingdom) {
      if (this.waitingForFriendName.equalsIgnoreCase(aFriendsName) && this.askingFriend) {
         this.waitingForFriendName = "";
         this.waitingForFriendCount = -1;
         this.askingFriend = false;
         switch(aReply) {
            case 1:
               this.sendNormalServerMessage("Unknown player " + aFriendsName + '.');
               break;
            case 2:
               if (Servers.isThisATestServer()) {
                  this.sendNormalServerMessage("(test only: " + aFriendsName + " is offline.)");
               }

               this.sendNormalServerMessage(aFriendsName + " is not currently available, please try again later.");
               break;
            case 3:
               if (Servers.isThisATestServer()) {
                  this.sendNormalServerMessage("(test only: " + aFriendsName + " did not respond in time.)");
               }

               this.sendNormalServerMessage(aFriendsName + " is not currently available, please try again later.");
               break;
            case 4:
               if (Servers.isThisATestServer()) {
                  this.sendNormalServerMessage("(test only: " + aFriendsName + " is busy.)");
               }

               this.sendNormalServerMessage(aFriendsName + " is not currently available, please try again later.");
            case 5:
            case 7:
            default:
               break;
            case 6:
               PlayerState pstate = PlayerInfoFactory.getPlayerState(aFriendsName);
               if (Servers.isThisATestServer()) {
                  this.sendNormalServerMessage("(test only: adding " + aFriendsName + " under " + this.waitingForFriendCategory.name() + ".)");
               }

               this.addFriend(pstate.getPlayerId(), this.waitingForFriendCategory.getCatId(), "");
               return 5;
            case 8:
               this.sendNormalServerMessage(aFriendsName + " is ignoring you.");
         }
      } else if (this.waitingForFriendName.equalsIgnoreCase(aFriendsName) && !this.askingFriend) {
         this.waitingForFriendName = "";
         this.waitingForFriendCount = -1;
         this.askingFriend = false;
         if (aReply == 5) {
            PlayerState pstate = PlayerInfoFactory.getPlayerState(aFriendsName);
            if (Servers.isThisATestServer()) {
               this.sendNormalServerMessage("(test only: adding " + aFriendsName + " under " + this.waitingForFriendCategory.name() + ".)");
            }

            this.addFriend(pstate.getPlayerId(), this.waitingForFriendCategory.getCatId(), "");
         } else if (aReply == 3 && Servers.isThisATestServer()) {
            this.sendNormalServerMessage("(test only: Out of time to respond to " + aFriendsName + ".)");
         }
      } else if (aReply == 0) {
         if (this.waitingForFriendName.length() != 0) {
            return 4;
         }

         PlayerState pstate = PlayerInfoFactory.getPlayerState(aFriendsName);
         if (this.isIgnored(pstate.getPlayerId())) {
            return 8;
         }

         if (this.hasFlag(1)) {
            return 2;
         }

         if (this.getKingdomId() != aKingdom && !this.hasFlag(2) | !xKingdom) {
            return 2;
         }

         if (xServer && !this.hasFlag(3)) {
            return 2;
         }

         this.sendServerMessage(aFriendsName + " is asking to be your friend. Use '/addfriend " + aFriendsName + " <category>' to allow them.", -1, -1, -1);
         this.waitingForFriendName = aFriendsName;
         this.waitingForFriendCount = 300;
         this.askingFriend = false;
      }

      return 7;
   }

   @Override
   public final float addSpellResistance(short spellId) {
      return this.saveFile.addSpellResistance(spellId);
   }

   @Override
   public final SpellResistance getSpellResistance(short spellId) {
      return this.saveFile.getSpellResistance(spellId);
   }

   public final void sendSpellResistances() {
      this.saveFile.sendSpellResistances(this.communicator);
   }

   @Override
   public final void setArmourLimitingFactor(float factor, boolean initializing) {
      this.saveFile.setArmourLimitingFactor(factor, this.communicator, initializing);
   }

   @Override
   public final float getArmourLimitingFactor() {
      return this.saveFile.getArmourLimitingFactor();
   }

   @Override
   public void recalcLimitingFactor(@Nullable Item currentItem) {
      Item[] boditems = this.getBody().getContainersAndWornItems();
      float currLimit = 0.3F;

      for(Item i : boditems) {
         if (i.isArmour()) {
            try {
               if (i.equals(this.getArmour((byte)1))) {
                  continue;
               }
            } catch (NoSpaceException | NoArmourException var10) {
            }

            ArmourTemplate armour = ArmourTemplate.getArmourTemplate(i.getTemplateId());
            if (armour != null) {
               if (armour.getLimitFactor() < currLimit) {
                  currLimit = armour.getLimitFactor();
               }
            } else {
               logger.log(Level.WARNING, "Armour is not in Armour list  " + i.getName() + ".");
            }
         }
      }

      if (currentItem != null) {
         try {
            if (!currentItem.equals(this.getArmour((byte)1))) {
               ArmourTemplate armour = ArmourTemplate.getArmourTemplate(currentItem.getTemplateId());
               if (armour != null && armour.getLimitFactor() < currLimit) {
                  currLimit = armour.getLimitFactor();
               }
            }
         } catch (NoSpaceException | NoArmourException var9) {
         }
      }

      this.setArmourLimitingFactor(currLimit, currentItem == null);
   }

   @Override
   public void addChallengeScore(int type, float scoreAdded) {
      if (Servers.localServer.isChallengeServer()) {
         if (this.scoresToClear == null) {
            this.scoresToClear = new ConcurrentHashMap<>();
         }

         Float score = this.scoresToClear.get(type);
         if (score == null) {
            score = scoreAdded;
         } else {
            score = score + scoreAdded;
         }

         this.scoresToClear.put(type, score);
      }
   }

   private final void clearChallengeScores() {
      if (this.scoresToClear != null) {
         for(Entry<Integer, Float> entry : this.scoresToClear.entrySet()) {
            Integer type = entry.getKey();
            Float value = entry.getValue();
            ChallengeSummary.addToScore(this.saveFile, type, value);
            ChallengeSummary.addToScore(this.saveFile, ChallengePointEnum.ChallengePoint.OVERALL.getEnumtype(), value);
         }

         this.scoresToClear.clear();
      }
   }

   @Override
   public boolean checkCoinAward(int chance) {
      if (Server.rand.nextInt(chance) == 0) {
         Shop kingsMoney = Economy.getEconomy().getKingsShop();
         if (kingsMoney.getMoney() > 100000L) {
            int coinRand = Server.rand.nextInt(10);
            int coin = 50;
            byte var11;
            switch(coinRand) {
               case 0:
                  var11 = 50;
                  break;
               case 1:
               case 2:
               case 3:
               case 4:
                  var11 = 54;
                  break;
               case 5:
               case 6:
               case 7:
               case 8:
                  var11 = 58;
                  break;
               case 9:
                  var11 = 52;
                  break;
               default:
                  var11 = 50;
            }

            try {
               float faintChance = 1.0F;
               int supPremModifier = 0;
               byte rarity = 1;
               if (this.isPaying()) {
                  faintChance = 1.03F;
                  supPremModifier = 3;
               }

               if (Server.rand.nextFloat() * 10000.0F <= faintChance) {
                  rarity = 3;
               } else if (Server.rand.nextInt(100) <= 0 + supPremModifier) {
                  rarity = 2;
               }

               Item coinItem = ItemFactory.createItem(var11, (float)(60 + Server.rand.nextInt(20)), rarity, "");
               this.getInventory().insertItem(coinItem, true);
               kingsMoney.setMoney(kingsMoney.getMoney() - (long)Economy.getValueFor(var11));
               this.getCommunicator().sendRarityEvent();
               return true;
            } catch (NoSuchTemplateException var9) {
               logger.log(Level.WARNING, "No template for item coin");
            } catch (FailedException var10) {
               logger.log(Level.WARNING, var10.getMessage() + ": coin");
            }
         }
      }

      return false;
   }

   public long getRespondTo() {
      return this.sendResponseTo;
   }

   public void clearRespondTo() {
      this.sendResponseTo = -10L;
   }

   @Override
   public String getAllianceName() {
      if (this.getCitizenVillage() != null) {
         int allianceNumber = this.getCitizenVillage().getAllianceNumber();
         if (allianceNumber > 0) {
            PvPAlliance alliance = PvPAlliance.getPvPAlliance(allianceNumber);
            if (alliance != null) {
               return alliance.getName();
            }
         }
      }

      return "";
   }

   public String getVillageName() {
      return this.getCitizenVillage() != null ? this.getCitizenVillage().getName() : "";
   }

   @Override
   public String getKingdomName() {
      return Kingdoms.getNameFor(this.getKingdomId());
   }

   public long getLastChangedPath() {
      return this.saveFile.getLastChangedPath();
   }

   public void setLastChangedPath(long lastChangedPath) {
      this.saveFile.setLastChangedPath(lastChangedPath);
   }

   public long getPlotCourseCooldown() {
      Cooldowns cd = Cooldowns.getCooldownsFor(this.getWurmId(), false);
      return cd != null ? cd.isAvaibleAt(717) : 0L;
   }

   public void addPlotCourseCooldown(long cooldown) {
      Cooldowns cd = Cooldowns.getCooldownsFor(this.getWurmId(), true);
      cd.addCooldown(717, System.currentTimeMillis() + cooldown, false);
   }

   public void checkKingdom() {
      if (Servers.isThisAChaosServer() && this.getPower() <= 0) {
         String reason = "You have no kingdom.";
         byte kingdomId = this.getKingdomId();
         boolean changeKingdom = kingdomId == 0;
         boolean isPmk = kingdomId < 0 || kingdomId > 4;
         if (!changeKingdom && isPmk) {
            Kingdom k = Kingdoms.getKingdomOrNull(this.getKingdomId());
            if (k == null) {
               reason = "Your kingdom no longer exists.";
               changeKingdom = true;
            } else if (!k.existsHere()) {
               reason = k.getName() + " no longer exists here.";
               changeKingdom = true;
            }
         }

         if (changeKingdom) {
            try {
               this.getCommunicator().sendSafeServerMessage(reason + " You are now a member of " + Kingdoms.getNameFor((byte)4));
               this.setKingdomId((byte)4, true, false);
               logger.log(Level.INFO, this.getName() + ": Invalid kingdom, moving to " + Kingdoms.getNameFor((byte)4));
            } catch (IOException var6) {
            }
         }
      }
   }

   public String checkCourseRestrictions() {
      return (this.isFighting() || this.getEnemyPresense() > 0) && this.getSecondsPlayed() > 300.0F
         ? "There are enemies in the vicinity. You fail to focus on a course."
         : "";
   }

   @Override
   public byte getRarityShader() {
      if (this.getBonusForSpellEffect((byte)22) > 70.0F) {
         return 2;
      } else {
         return this.getBonusForSpellEffect((byte)22) > 0.0F ? 1 : this.rarityShader;
      }
   }

   public void setRarityShader(byte rarityShader) {
      this.rarityShader = rarityShader;
      this.getCurrentTile().setNewRarityShader(this);
      if (rarityShader != 0) {
         this.raritySeconds = 100;
      }
   }
}
