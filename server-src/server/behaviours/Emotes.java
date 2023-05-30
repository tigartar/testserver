package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.combat.CombatEngine;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.items.NotOwnedException;
import com.wurmonline.server.structures.Blocking;
import com.wurmonline.server.structures.BridgePart;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.Floor;
import com.wurmonline.server.structures.NoSuchStructureException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Structures;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.shared.constants.SoundNames;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class Emotes implements ItemTypes, ActionTypes, SoundNames, MiscConstants {
   private static List<ActionEntry> defaultNiceEmotes = new LinkedList<>();
   private static List<ActionEntry> defaultNeutralEmotes = new LinkedList<>();
   private static List<ActionEntry> defaultOffensiveEmotes = new LinkedList<>();
   private static List<ActionEntry> allEmotes = new LinkedList<>();
   private static final Logger logger = Logger.getLogger(Emotes.class.getName());
   public static final short EMOTES = 2000;
   public static final short SMILE = 2000;
   public static final short CHUCKLE = 2001;
   public static final short APPLAUD = 2002;
   public static final short HUG = 2003;
   public static final short KISS = 2004;
   public static final short GROVEL = 2005;
   public static final short WORSHIP = 2006;
   public static final short COMFORT = 2007;
   public static final short DANCE = 2008;
   public static final short FLIRT = 2009;
   public static final short BOW = 2010;
   public static final short HKISS = 2011;
   public static final short TICKLE = 2012;
   public static final short WAVE = 2013;
   public static final short CALL = 2014;
   public static final short POKE = 2015;
   public static final short EYEROLL = 2016;
   public static final short DISBELIEVE = 2017;
   public static final short WORRY = 2018;
   public static final short DISAGREE = 2019;
   public static final short TEASE = 2020;
   public static final short LAUGH = 2021;
   public static final short CRY = 2022;
   public static final short POINT = 2023;
   public static final short FOLLOW = 2030;
   public static final short GOODBYE = 2031;
   public static final short LEAD = 2032;
   public static final short THAT_WAY = 2033;
   public static final short WRONG_WAY = 2034;
   public static final short SPIT = 2024;
   public static final short FART = 2025;
   public static final short INSULT = 2026;
   public static final short PUSH = 2027;
   public static final short CURSE = 2028;
   public static final short SLAP = 2029;
   public static final ActionEntry[] emoteEntrys = new ActionEntry[]{
      new ActionEntry((short)2000, "Smile", "smiling", new int[]{0}, 20),
      new ActionEntry((short)2001, "Chuckle", "chuckling", new int[]{0}, 20),
      new ActionEntry((short)2002, "Applaud", "applauding", new int[]{0}, 20),
      new ActionEntry((short)2003, "Hug", "hugging", new int[]{0}),
      new ActionEntry((short)2004, "Kiss", "kissing", new int[]{0}),
      new ActionEntry((short)2005, "Grovel", "grovelling", new int[]{0}, 20),
      new ActionEntry((short)2006, "Worship", "worshipping", new int[]{0}, 20),
      new ActionEntry((short)2007, "Comfort", "comforting", new int[]{0}, 20),
      new ActionEntry((short)2008, "Dance", "dancing", new int[]{0}, 20),
      new ActionEntry((short)2009, "Flirt", "flirting", new int[]{0}, 20),
      new ActionEntry((short)2010, "Bow", "bowing", new int[]{0}, 20),
      new ActionEntry((short)2011, "Kiss hand", "kissing", new int[]{0}),
      new ActionEntry((short)2012, "Tickle", "tickling", new int[]{0}),
      new ActionEntry((short)2013, "Wave", "waving", new int[]{0}, 200),
      new ActionEntry((short)2014, "Call", "calling", new int[]{0}, 200),
      new ActionEntry((short)2015, "Poke", "poking", new int[]{0}),
      new ActionEntry((short)2016, "Roll with the eyes", "rolling with the eyes", new int[]{0}, 20),
      new ActionEntry((short)2017, "Disbelieve", "disbelieving", new int[]{0}, 20),
      new ActionEntry((short)2018, "Worry", "worrying", new int[]{0}, 20),
      new ActionEntry((short)2019, "Disagree", "disagreeing", new int[]{0}, 20),
      new ActionEntry((short)2020, "Tease", "teasing", new int[]{0}, 20),
      new ActionEntry((short)2021, "Laugh", "laughing", new int[]{0}, 20),
      new ActionEntry((short)2022, "Cry", "crying", new int[]{0}, 20),
      new ActionEntry((short)2023, "Point", "pointing", new int[]{0}, 100),
      new ActionEntry((short)2024, "Spit", "spitting", new int[]{0}, 10),
      new ActionEntry((short)2025, "Fart", "farting", new int[]{0}, 20),
      new ActionEntry((short)2026, "Insult", "insulting", new int[]{0}, 40),
      new ActionEntry((short)2027, "Push", "pushing", new int[]{0}),
      new ActionEntry((short)2028, "Curse", "cursing", new int[]{0}, 20),
      new ActionEntry((short)2029, "Slap", "slapping", new int[]{0}),
      new ActionEntry((short)2030, "Follow", "following", new int[]{0}, 20),
      new ActionEntry((short)2031, "Goodbye", "saying goodbye", new int[]{0}, 200),
      new ActionEntry((short)2032, "Lead", "leading", new int[]{0}, 20),
      new ActionEntry((short)2033, "That way", "that way", new int[]{0}, 20),
      new ActionEntry((short)2034, "Wrong way", "wrong way", new int[]{0}, 20)
   };

   private Emotes() {
   }

   private static void createDefaultNiceEmotes() {
      defaultNiceEmotes.add(emoteEntrys[0]);
      defaultNiceEmotes.add(emoteEntrys[1]);
      defaultNiceEmotes.add(emoteEntrys[2]);
      defaultNiceEmotes.add(emoteEntrys[3]);
      defaultNiceEmotes.add(emoteEntrys[4]);
      defaultNiceEmotes.add(emoteEntrys[5]);
      defaultNiceEmotes.add(emoteEntrys[6]);
      defaultNiceEmotes.add(emoteEntrys[7]);
      defaultNiceEmotes.add(emoteEntrys[8]);
      defaultNiceEmotes.add(emoteEntrys[9]);
      defaultNiceEmotes.add(emoteEntrys[10]);
      defaultNiceEmotes.add(emoteEntrys[11]);
      defaultNiceEmotes.add(emoteEntrys[12]);
   }

   private static void createDefaultNeutralEmotes() {
      defaultNeutralEmotes.add(emoteEntrys[13]);
      defaultNeutralEmotes.add(emoteEntrys[14]);
      defaultNeutralEmotes.add(emoteEntrys[15]);
      defaultNeutralEmotes.add(emoteEntrys[16]);
      defaultNeutralEmotes.add(emoteEntrys[17]);
      defaultNeutralEmotes.add(emoteEntrys[18]);
      defaultNeutralEmotes.add(emoteEntrys[19]);
      defaultNeutralEmotes.add(emoteEntrys[20]);
      defaultNeutralEmotes.add(emoteEntrys[21]);
      defaultNeutralEmotes.add(emoteEntrys[22]);
      defaultNeutralEmotes.add(emoteEntrys[23]);
      defaultNeutralEmotes.add(emoteEntrys[30]);
      defaultNeutralEmotes.add(emoteEntrys[31]);
      defaultNeutralEmotes.add(emoteEntrys[32]);
      defaultNeutralEmotes.add(emoteEntrys[33]);
      defaultNeutralEmotes.add(emoteEntrys[34]);
   }

   private static void createDefaultOffensiveEmotes() {
      defaultOffensiveEmotes.add(emoteEntrys[24]);
      defaultOffensiveEmotes.add(emoteEntrys[25]);
      defaultOffensiveEmotes.add(emoteEntrys[26]);
      defaultOffensiveEmotes.add(emoteEntrys[27]);
      defaultOffensiveEmotes.add(emoteEntrys[28]);
      defaultOffensiveEmotes.add(emoteEntrys[29]);
   }

   static List<ActionEntry> getEmoteList() {
      return allEmotes;
   }

   private static final void createEmoteList() {
      allEmotes.add(new ActionEntry((short)-3, "Emotes", "emoting", new int[]{0}));
      List<ActionEntry> niceEmotes = getDefaultNiceEmotes();
      List<ActionEntry> neutralEmotes = getDefaultNeutralEmotes();
      List<ActionEntry> offensiveEmotes = getDefaultOffensiveEmotes();
      allEmotes.add(new ActionEntry((short)(-niceEmotes.size()), "Nice", "emoting nice", new int[]{0}));

      for(ActionEntry entry : niceEmotes) {
         allEmotes.add(entry);
      }

      allEmotes.add(new ActionEntry((short)(-neutralEmotes.size()), "Neutral", "emoting neutral", new int[]{0}));

      for(ActionEntry entry : neutralEmotes) {
         allEmotes.add(entry);
      }

      allEmotes.add(new ActionEntry((short)(-offensiveEmotes.size()), "Offensive", "emoting offensive", new int[]{0}));

      for(ActionEntry entry : offensiveEmotes) {
         allEmotes.add(entry);
      }
   }

   private static List<ActionEntry> getDefaultNiceEmotes() {
      return defaultNiceEmotes;
   }

   private static List<ActionEntry> getDefaultNeutralEmotes() {
      return defaultNeutralEmotes;
   }

   private static List<ActionEntry> getDefaultOffensiveEmotes() {
      return defaultOffensiveEmotes;
   }

   static void emoteAt(short emote, Creature performer, Creature receiver) {
      String emoteGender = ".male";
      if (performer.getSex() == 1) {
         emoteGender = ".female";
      }

      String emoteSound = "";
      String performerString = "";
      String receiverString = "";
      String bcastString = "";
      String pname = performer.getNameWithGenus();
      String rname = receiver.getNameWithGenus();
      if (emote == 2000) {
         performerString = "You smile at " + rname + ".";
         receiverString = pname + " smiles at you.";
         bcastString = pname + " smiles at " + rname + ".";
         if (receiver.isPlayer()) {
            performer.achievement(176);
         }
      } else if (emote == 2001) {
         emoteSound = "sound.emote.chuckle";
         performerString = "You chuckle happily as you think of " + rname + ".";
         receiverString = pname + " looks at you and chuckles happily.";
         bcastString = pname + " looks at " + rname + " and chuckles happily.";
      } else if (emote == 2002) {
         emoteSound = "sound.emote.applaud";
         performerString = "You applaud " + rname + " for " + receiver.getHisHerItsString() + " efforts.";
         receiverString = pname + " gives you a round of applause.";
         bcastString = pname + " gives " + rname + " a round of applause.";
      } else if (emote == 2003) {
         if (receiver.isGhost()) {
            performerString = "You try to hug " + rname + " but the air is too confusing.";
            receiverString = pname + " tries hugs you.";
            bcastString = pname + " awkwardly hugs " + rname + " mid-air.";
         } else {
            performerString = "You hug " + rname + ".";
            receiverString = pname + " hugs you.";
            bcastString = pname + " hugs " + rname + ".";
            if (receiver.isPlayer()) {
               performer.achievement(175);
            }
         }
      } else if (emote == 2030) {
         emoteSound = "sound.emote.follow";
         performerString = StringUtil.format("You tell %s to follow.", rname);
         receiverString = StringUtil.format("%s tells you to follow.", pname);
         bcastString = StringUtil.format("%s tells %s to follow %s.", pname, rname, performer.getHimHerItString());
      } else if (emote == 2031) {
         emoteSound = "sound.emote.goodbye";
         performerString = StringUtil.format("You say goodbye to %s.", rname);
         receiverString = StringUtil.format("%s says goodbye to you.", pname);
         bcastString = StringUtil.format("%s says goodbye to %s.", pname, rname);
      } else if (emote == 2032) {
         emoteSound = "sound.emote.lead";
         performerString = StringUtil.format("You ask %s to lead the way.", rname);
         receiverString = StringUtil.format("%s asks you to lead the way.", pname);
         bcastString = StringUtil.format("%s asks %s to lead the way.", pname, rname);
      } else if (emote == 2033) {
         emoteSound = "sound.emote.that.way";
         performerString = StringUtil.format("You tell %s to go that way.", rname);
         receiverString = StringUtil.format("%s tells you to go that way.", pname);
         bcastString = StringUtil.format("%s tells %s to go that way.", pname, rname);
      } else if (emote == 2034) {
         emoteSound = "sound.emote.wrong.way";
         performerString = StringUtil.format("You tell %s to not go that way.", rname);
         receiverString = StringUtil.format("%s tells you to not go that way.", pname);
         bcastString = StringUtil.format("%s tells %s to not go that way.", pname, rname);
      } else if (emote == 2004) {
         emoteSound = "sound.emote.kiss";
         if (receiver.isGhost()) {
            performerString = "You try to kiss " + rname + " but the air leaves no mark.";
            receiverString = pname + " tries kiss you.";
            bcastString = pname + " blows a kiss towards " + rname + ".";
         } else {
            performerString = "You kiss " + rname + ".";
            receiverString = pname + " kisses you.";
            bcastString = pname + " kisses " + rname + ".";
         }
      } else if (emote == 2005) {
         performerString = "You grovel in the dirt before " + rname + ".";
         receiverString = pname + " grovels in the dirt before you.";
         bcastString = pname + " grovels in the dirt before " + rname + ".";
      } else if (emote == 2006) {
         performerString = "You fall to your knees and worship " + rname + ".";
         receiverString = pname + " falls to " + performer.getHisHerItsString() + " knees and worships you.";
         bcastString = pname + " falls to the ground and worships " + rname + ".";
      } else if (emote == 2007) {
         if (receiver.isGhost()) {
            performerString = "You try to pat " + rname + " but without physical contact it is pointless.";
            receiverString = pname + " tries comfort you.";
            bcastString = pname + " pretends to comfort " + rname + " from a distance.";
         } else {
            performerString = "You gently comfort " + rname + ".";
            receiverString = pname + " gently comforts you.";
            bcastString = pname + " gently comforts " + rname + ".";
         }
      } else if (emote == 2008) {
         performerString = "You dance and frolic with " + rname + ".";
         receiverString = pname + " joyfully dances around with you.";
         bcastString = pname + " dances around with " + rname + ".";
         if (receiver.isPlayer()) {
            performer.achievement(181);
         }
      } else if (emote == 2009) {
         performerString = "You flirtilly wink at " + rname + ".";
         receiverString = pname + " winks invitingly to you.";
         bcastString = pname + " seems to have something in the eye.";
         if (receiver.isPlayer()) {
            performer.achievement(179);
         }
      } else if (emote == 2010) {
         performerString = "You bow before " + rname + ".";
         receiverString = pname + " bows before you.";
         bcastString = pname + " bows before " + rname + ".";
      } else if (emote == 2011) {
         if (receiver.isGhost()) {
            performerString = "You bend down and gently pretend to kiss " + rname + "'s hand. " + receiver.getHeSheItString() + " is not impressed.";
            receiverString = pname + " gently bends down and kisses your hand.";
            bcastString = pname + " gently bends down and pretends to kiss " + rname + "'s hand.";
         } else {
            performerString = "You bend down and gently kiss " + rname + "'s hand.";
            receiverString = pname + " gently bends down and kisses your hand.";
            bcastString = pname + " gently bends down and kisses " + rname + "'s hand.";
         }
      } else if (emote == 2012) {
         if (receiver.isGhost()) {
            performerString = "You try to tickle " + rname + " but only encounter air.";
            receiverString = pname + " tickles you! Tee-hee! Not.";
            bcastString = pname + " tries to tickle the air around " + rname + " playfully  but.. well.";
         } else {
            performerString = "You tickle " + rname + ".";
            receiverString = pname + " tickles you! Tee-hee!";
            bcastString = pname + " tickles " + rname + " playfully.";
         }
      } else if (emote == 2013) {
         emoteSound = "sound.emote.wave";
         performerString = "You wave at " + rname + ".";
         receiverString = pname + " waves at you.";
         bcastString = pname + " waves at " + rname + ".";
         if (receiver.isHorse()) {
            performer.achievement(174);
         }
      } else if (emote == 2014) {
         emoteSound = "sound.emote.call";
         performerString = "You call out to " + rname + " for " + receiver.getHisHerItsString() + " attention.";
         receiverString = "You hear " + pname + " call out for your attention.";
         bcastString = pname + " calls out for " + rname + "'s attention.";
      } else if (emote == 2021) {
         emoteSound = "sound.emote.laugh";
         performerString = "You laugh hysterically at " + rname + ".";
         receiverString = pname + " laughs hysterically and seems to think you are really funny.";
         bcastString = pname + " laughs hysterically at " + rname + ".";
      } else if (emote == 2022) {
         if (receiver.isGhost()) {
            emoteSound = "sound.emote.cry";
            performerString = "You pretend to cry on " + rname + "'s shoulder.";
            receiverString = pname + " pretends to cry on your shoulder.";
            bcastString = pname + " pretends to cry on " + rname + "'s shoulder.";
         } else {
            emoteSound = "sound.emote.cry";
            performerString = "You cry on " + rname + "'s shoulder.";
            receiverString = pname + " cries on your shoulder.";
            bcastString = pname + " cries on " + rname + "'s shoulder.";
         }
      } else if (emote == 2023) {
         performerString = "You point at " + rname + ".";
         receiverString = pname + " points at you.";
         bcastString = pname + " points at " + rname + ".";
      } else if (emote == 2015) {
         performerString = "You poke " + rname + " in the ribs.";
         receiverString = pname + " pokes you in the ribs.";
         bcastString = pname + " pokes " + rname + " in the ribs.";
      } else if (emote == 2016) {
         performerString = "You roll your eyes at " + rname + ".";
         receiverString = pname + " rolls " + performer.getHisHerItsString() + " eyes in your direction.";
         bcastString = pname + " rolls " + performer.getHisHerItsString() + " eyes at " + rname + ".";
      } else if (emote == 2017) {
         performerString = "You stare at " + rname + " sceptically.";
         receiverString = pname + " stares at you sceptically.";
         bcastString = pname + " stares at " + rname + " sceptically.";
      } else if (emote == 2018) {
         emoteSound = "sound.emote.worry";
         performerString = "You sigh loudly and wonder what will happen to " + rname + ".";
         receiverString = pname + " lets out a worrying sigh in your direction.";
         bcastString = pname + " lets out a loud sigh, obviously worrying about " + rname + ".";
      } else if (emote == 2019) {
         emoteSound = "sound.emote.disagree";
         performerString = "You roll your eyes and shake your head in disagreement.";
         receiverString = pname + " shakes " + performer.getHisHerItsString() + " head and rolls " + performer.getHisHerItsString() + " eyes in disagreement.";
         bcastString = pname + " shakes " + performer.getHisHerItsString() + " head and rolls " + performer.getHisHerItsString() + " eyes in disagreement.";
      } else if (emote == 2020) {
         emoteSound = "sound.emote.tease";
         performerString = "You tease " + rname + " for " + receiver.getHisHerItsString() + " hairstyle.";
         receiverString = pname + " teases you for your hairstyle.";
         bcastString = pname + " teases " + rname + " for " + receiver.getHisHerItsString() + " hairstyle.";
      } else if (emote == 2024) {
         emoteSound = "sound.emote.spit";
         if (receiver.isGhost()) {
            performerString = "You spit at " + rname + "'s face but it passes through.";
            receiverString = pname + " spits in your face!";
            bcastString = pname + " spits through " + rname + "'s face!";
         } else {
            performerString = "You spit in " + rname + "'s face.";
            receiverString = pname + " spits in your face!";
            bcastString = pname + " spits in " + rname + "'s face!";
         }
      } else if (emote == 2025) {
         emoteSound = "sound.emote.fart";
         performerString = "You fart in " + rname + "'s direction.";
         receiverString = pname + " farts in your direction.";
         bcastString = pname + " farts in the general direction of " + rname + ".";
      } else if (emote == 2026) {
         emoteSound = "sound.emote.insult";
         performerString = "You call " + rname + " names.";
         receiverString = pname + " calls you different names.";
         bcastString = pname + " calls " + rname + " names.";
      } else if (emote == 2027) {
         if (receiver.isGhost()) {
            performerString = "You try to push " + rname + " in the chest but nothing is there is it?";
            receiverString = pname + " tries to push you in the chest.";
            bcastString = pname + " tries to push " + rname + " in the chest but passes through. Eerie.";
         } else {
            performerString = "You push " + rname + " in the chest.";
            receiverString = pname + " pushes you in the chest.";
            bcastString = pname + " pushes " + rname + " in the chest.";
         }
      } else if (emote == 2028) {
         emoteSound = "sound.emote.curse";
         String hisher = receiver.getHisHerItsString();
         performerString = "You let out heavy curses involving " + rname + ", " + hisher + " family and " + hisher + " relatives.";
         receiverString = pname + " lets out a long tirade of curses involving you, your family and your relatives.";
         bcastString = pname + " lets out a long tirade of curses involving " + rname + ", " + hisher + " family and " + hisher + " relatives.";
      } else if (emote == 2029) {
         if (receiver.isGhost()) {
            emoteSound = "sound.combat.miss.light";
            performerString = "You slap " + rname + " in the area of the face.";
            receiverString = pname + " slaps you around where your face used to be.";
            bcastString = pname + " slaps " + rname + " around where " + receiver.getHisHerItsString() + " face used to be.";
         } else {
            emoteSound = "sound.emote.slap";
            performerString = "You slap " + rname + " in the face.";
            receiverString = pname + " slaps you in the face.";
            bcastString = pname + " slaps " + rname + " in the face.";
         }
      }

      if (performer.mayEmote()) {
         if (emoteSound != null && emoteSound.length() > 0) {
            performer.makeEmoteSound();
            Methods.sendSound(performer, emoteSound + emoteGender);
         }

         performer.playAnimation(emoteEntrys[emote - 2000].getActionString().toLowerCase(), false);
      }

      Server.getInstance().broadCastAction(bcastString, performer, receiver, 5);
      performer.getCommunicator().sendNormalServerMessage(performerString);
      receiver.getCommunicator().sendNormalServerMessage(receiverString);
      if (receiver.hasTrait(10) && (Server.rand.nextInt(6) == 0 || Servers.localServer.testServer)) {
         performerString = rname + " looks angry!";
         bcastString = rname + " looks angry with " + pname + "!";
         Server.getInstance().broadCastAction(bcastString, performer, receiver, 5);
         performer.getCommunicator().sendNormalServerMessage(performerString);
         if (performer.isWithinDistanceTo(receiver.getPosX(), receiver.getPosY(), receiver.getPositionZ() + receiver.getAltOffZ(), 5.0F, 0.0F)
            && Blocking.getBlockerBetween(receiver, performer, 5) == null) {
            try {
               CombatEngine.addWound(
                  receiver,
                  performer,
                  (byte)3,
                  performer.getBody().getRandomWoundPos(),
                  4000.0,
                  performer.getArmourMod(),
                  "bite",
                  null,
                  0.0F,
                  0.0F,
                  false,
                  false,
                  false,
                  false
               );
            } catch (Exception var11) {
               logger.log(Level.WARNING, var11.getMessage(), (Throwable)var11);
            }
         }
      }
   }

   static void emoteAt(short emote, Creature performer, Item target) {
      String pname = performer.getNameWithGenus();
      String rname = target.getNameWithGenus();
      boolean isBodyPart = target.isBodyPartAttached();
      if (target.isInventory()) {
         performer.getCommunicator().sendNormalServerMessage("You can't interact with that.");
      } else {
         emoteAt(emote, performer, pname, rname, target, isBodyPart);
      }
   }

   static void emoteAt(short emote, Creature performer, Wall wall) {
      String pname = performer.getNameWithGenus();

      try {
         Structure structure = Structures.getStructure(wall.getStructureId());
         String rname = structure.getName();
         int tilex = wall.getTileX();
         int tiley = wall.getTileY();
         int tilez = (int)(performer.getStatus().getPositionZ() + performer.getAltOffZ()) >> 2;
         if (!performer.isWithinTileDistanceTo(tilex, tiley, tilez, emoteEntrys[emote - 2000].getRange() / 4)) {
            emote(emote, performer);
         } else {
            emoteAt(emote, performer, pname, rname, null, false);
         }
      } catch (NoSuchStructureException var9) {
         performer.getCommunicator().sendNormalServerMessage("You can't interact with that.");
      }
   }

   static void emoteAt(short emote, Creature performer, Floor floor) {
      String pname = performer.getNameWithGenus();

      try {
         Structure structure = Structures.getStructure(floor.getStructureId());
         String rname = structure.getName();
         int tilex = floor.getTileX();
         int tiley = floor.getTileY();
         int tilez = (int)(performer.getStatus().getPositionZ() + performer.getAltOffZ()) >> 2;
         if (!performer.isWithinTileDistanceTo(tilex, tiley, tilez, emoteEntrys[emote - 2000].getRange() / 4)) {
            emote(emote, performer);
         } else {
            emoteAt(emote, performer, pname, rname, null, false);
         }
      } catch (NoSuchStructureException var9) {
         performer.getCommunicator().sendNormalServerMessage("You can't interact with that.");
      }
   }

   static void emoteAt(short emote, Creature performer, BridgePart brisgePart) {
      String pname = performer.getNameWithGenus();

      try {
         Structure structure = Structures.getStructure(brisgePart.getStructureId());
         String rname = structure.getName();
         int tilex = brisgePart.getTileX();
         int tiley = brisgePart.getTileY();
         int tilez = (int)(performer.getStatus().getPositionZ() + performer.getAltOffZ()) >> 2;
         if (!performer.isWithinTileDistanceTo(tilex, tiley, tilez, emoteEntrys[emote - 2000].getRange() / 4)) {
            emote(emote, performer);
         } else {
            emoteAt(emote, performer, pname, rname, null, false);
         }
      } catch (NoSuchStructureException var9) {
         performer.getCommunicator().sendNormalServerMessage("You can't interact with that.");
      }
   }

   static void emoteAt(short emote, Creature performer, Wound wound) {
      String pname = performer.getNameWithGenus();
      String rname = "a " + wound.getDescription() + " wound";
      emoteAt(emote, performer, pname, rname, null, false);
   }

   static void emoteAt(short emote, Creature performer, Fence fence) {
      String pname = performer.getNameWithGenus();
      String rname = "a " + fence.getName();
      int tilex = fence.getTileX();
      int tiley = fence.getTileY();
      int tilez = (int)(performer.getStatus().getPositionZ() + performer.getAltOffZ()) >> 2;
      if (!performer.isWithinTileDistanceTo(tilex, tiley, tilez, emoteEntrys[emote - 2000].getRange() / 4)) {
         emote(emote, performer);
      } else {
         emoteAt(emote, performer, pname, rname, null, false);
      }
   }

   static void emoteAt(short emote, Creature performer, int tilex, int tiley, int tilez, int tile) {
      if (Tiles.isTree(Tiles.decodeType(tile))) {
         if (!performer.isWithinTileDistanceTo(tilex, tiley, tilez, emoteEntrys[emote - 2000].getRange() / 4)) {
            emote(emote, performer);
         } else {
            String pname = performer.getNameWithGenus();
            String rname = "a tree";
            emoteAt(emote, performer, pname, "a tree", null, false);
         }
      } else if (Tiles.isBush(Tiles.decodeType(tile))) {
         if (!performer.isWithinTileDistanceTo(tilex, tiley, tilez, emoteEntrys[emote - 2000].getRange() / 4)) {
            emote(emote, performer);
         } else {
            String pname = performer.getNameWithGenus();
            String rname = "a bush";
            emoteAt(emote, performer, pname, "a bush", null, false);
         }
      } else {
         emote(emote, performer);
      }
   }

   private static void emoteAt(short emote, Creature performer, String pname, String rname, @Nullable Item item, boolean isBodyPart) {
      String performerString = "";
      String bcastString = "";
      String detString = rname;
      String emoteSound = "";
      String emoteGender = ".male";
      if (performer.getSex() == 1) {
         emoteGender = ".female";
      }

      if (item != null) {
         detString = "the " + item.getName();
      }

      Creature c = null;
      if (isBodyPart && performer.getWurmId() != item.getOwnerId()) {
         try {
            c = Server.getInstance().getCreature(item.getOwnerId());
         } catch (NoSuchCreatureException var16) {
         } catch (NoSuchPlayerException var17) {
         }
      }

      String ownerString = "your";
      String possessionString = performer.getHisHerItsString();
      if (c != null) {
         possessionString = c.getNameWithGenus() + (c.getName().endsWith("s") ? "'" : "'s");
         ownerString = possessionString;
      }

      if (emote == 2000) {
         if (isBodyPart) {
            performerString = "You smile at " + ownerString + " " + item.getName() + ".";
            bcastString = pname + " smiles at " + possessionString + " " + item.getName() + ".";
         } else {
            performerString = "You smile at " + detString + ".";
            bcastString = pname + " smiles at " + rname + ".";
         }
      } else if (emote == 2001) {
         emoteSound = "sound.emote.chuckle";
         if (isBodyPart) {
            performerString = "You chuckle happily as you think of " + ownerString + " " + item.getName() + ".";
            bcastString = pname + " looks at " + possessionString + " " + item.getName() + " and chuckles happily.";
         } else {
            performerString = "You chuckle happily as you think of " + detString + ".";
            bcastString = pname + " looks at " + rname + " and chuckles happily.";
         }
      } else if (emote == 2002) {
         emoteSound = "sound.emote.applaud";
         if (isBodyPart) {
            performerString = "You applaud " + ownerString + " " + item.getName() + ".";
            bcastString = pname + " gives a round of applause to " + possessionString + " " + item.getName() + ".";
         } else {
            performerString = "You applaud " + detString + " for its efforts.";
            bcastString = pname + " gives " + rname + " a round of applause.";
         }
      } else if (emote == 2003) {
         if (isBodyPart) {
            performerString = "You grab " + ownerString + " " + item.getName() + " in a futile attempt of hugging.";
            bcastString = pname + " grabs " + possessionString + " " + item.getName() + " in a futile attempt of hugging.";
         } else {
            performerString = "You press " + detString + " close to your chest.";
            bcastString = pname + " presses " + rname + " close to " + performer.getHisHerItsString() + " chest.";
         }
      } else if (emote == 2004) {
         emoteSound = "sound.emote.kiss";
         if (isBodyPart) {
            performerString = "You would really love to kiss " + ownerString + " " + item.getName() + ", wouldn't you?";
            bcastString = pname + " stares a bit too long at " + possessionString + " " + item.getName() + ". What are they thinking about?";
         } else {
            performerString = "You fondly kiss " + detString + ".";
            bcastString = pname + " fondly kisses " + rname + ".";
         }
      } else if (emote == 2005) {
         if (isBodyPart) {
            performerString = "You would really love to grovel before " + ownerString + " " + item.getName() + ", wouldn't you?";
            bcastString = pname + " looks conflicted as their gaze lingers a bit too long on " + possessionString + " " + item.getName() + ".";
         } else {
            performerString = "You throw yourself to the ground and grovel in the dirt before " + detString + ".";
            bcastString = pname + " grovels in the dirt before " + rname + ".";
         }
      } else if (emote == 2006) {
         if (isBodyPart) {
            performerString = "You worship " + ownerString + " beautiful " + item.getName() + ".";
            bcastString = pname + " worships " + possessionString + " " + item.getName() + ".";
         } else {
            performerString = "You fall to your knees and worship " + detString + ".";
            bcastString = pname + " falls to the ground and worships " + rname + ".";
         }
      } else if (emote == 2030) {
         emoteSound = "sound.emote.follow";
         if (isBodyPart) {
            performerString = StringUtil.format("You tell %s %s to follow.", ownerString, item.getName());
            bcastString = StringUtil.format("%s tells %s %s to follow %s lead.", pname, possessionString, item.getName(), performer.getHisHerItsString());
         } else {
            performerString = StringUtil.format("You command %s to follow.", detString);
            bcastString = StringUtil.format("%s commands %s to follow %s.", pname, rname, performer.getHimHerItString());
         }
      } else if (emote == 2031) {
         emoteSound = "sound.emote.goodbye";
         if (isBodyPart) {
            performerString = StringUtil.format("You say goodbye to %s.", item.getName());
            bcastString = StringUtil.format("%s say goodbye to %s.", pname, item.getName());
         } else {
            performerString = StringUtil.format("You say goodbye to %s.", detString);
            bcastString = StringUtil.format("%s say goodbye to %s.", pname, rname);
         }
      } else if (emote == 2032) {
         emoteSound = "sound.emote.lead";
         if (isBodyPart) {
            performerString = StringUtil.format("You tell %s to lead the way.", item.getName());
            bcastString = StringUtil.format("%s tells %s to lead the way.", pname, item.getName());
         } else {
            performerString = StringUtil.format("You tell %s to lead the way.", detString);
            bcastString = StringUtil.format("%s tells %s to lead the way.", pname, rname);
         }
      } else if (emote == 2033) {
         emoteSound = "sound.emote.that.way";
         if (isBodyPart) {
            performerString = StringUtil.format("You tell %s to go that way.", item.getName());
            bcastString = StringUtil.format("%s tells %s to go that way.", pname, item.getName());
         } else {
            performerString = StringUtil.format("You tell %s to go that way.", detString);
            bcastString = StringUtil.format("%s tells %s to go that way.", pname, rname);
         }
      } else if (emote == 2034) {
         emoteSound = "sound.emote.wrong.way";
         if (isBodyPart) {
            performerString = StringUtil.format("You tell %s to not go that way.", item.getName());
            bcastString = StringUtil.format("%s tells %s to not go that way.", pname, item.getName());
         } else {
            performerString = StringUtil.format("You tell %s to not go that way.", detString);
            bcastString = StringUtil.format("%s tells %s to not go that way.", pname, rname);
         }
      } else if (emote == 2007) {
         if (isBodyPart) {
            performerString = "You gently comfort " + ownerString + " " + item.getName() + ".";
            bcastString = pname + " gently comforts " + possessionString + " " + item.getName() + ".";
         } else {
            performerString = "You gently pat " + detString + ".";
            bcastString = pname + " gently pats " + rname + ".";
         }
      } else if (emote == 2008) {
         if (isBodyPart) {
            performerString = "You wiggle and shake " + ownerString + " " + item.getName() + " a little.";
            bcastString = pname + " wiggles and shakes " + possessionString + " " + item.getName() + ".";
         } else {
            performerString = "You dance and frolic around " + detString + ".";
            bcastString = pname + " dances around " + rname + ".";
         }
      } else if (emote == 2009) {
         if (isBodyPart) {
            performerString = "You flirt around with " + ownerString + " " + item.getName() + " but get no response.";
            bcastString = pname + " smiles and winks at " + possessionString + " " + item.getName() + ".";
         } else {
            performerString = "You flirtilly wink at " + detString + ".";
            bcastString = pname + " smiles and winks at " + rname + ".";
         }
      } else if (emote == 2010) {
         if (isBodyPart) {
            performerString = "You wish you could bow before " + ownerString + " " + item.getName() + ".";
            bcastString = pname + " sort of half bows towards " + possessionString + " " + item.getName() + " before stopping awkwardly.";
         } else {
            performerString = "You bow before " + detString + ".";
            bcastString = pname + " bows before " + rname + ".";
         }
      } else if (emote == 2011) {
         if (isBodyPart) {
            performerString = "You bend down and gently kiss " + ownerString + " hand.";
            bcastString = pname + " gently bends down and kisses " + possessionString + " hand.";
         } else {
            performerString = "You bend down and gently kiss " + detString + ".";
            bcastString = pname + " gently bends down and kisses " + rname + ".";
         }
      } else if (emote == 2012) {
         if (isBodyPart) {
            performerString = "You tickle " + ownerString + " " + item.getName() + ". Tee-hee.";
            bcastString = pname + " tickles " + possessionString + " " + item.getName() + " playfully.";
         } else {
            performerString = "You tickle " + detString + " playfully.";
            if (item != null) {
               bcastString = pname + " tickles " + performer.getHisHerItsString() + " " + item.getName() + " playfully.";
            } else {
               bcastString = pname + " tickles " + detString + " playfully.";
            }
         }
      } else if (emote == 2013) {
         if (isBodyPart) {
            performerString = "You frantically try to wave to " + ownerString + " " + item.getName() + ".";
            bcastString = pname + " frantically tries to wave to " + possessionString + " " + item.getName() + ".";
         } else {
            try {
               if (item != null) {
                  item.getOwner();
                  performerString = "You wave " + detString + " frantically.";
                  bcastString = pname + " waves " + rname + " frantically.";
               } else {
                  emoteSound = "sound.emote.wave";
                  performerString = "You wave at " + detString + " frantically.";
                  bcastString = pname + " waves at " + rname + " frantically.";
               }
            } catch (NotOwnedException var15) {
               emoteSound = "sound.emote.wave";
               performerString = "You wave at " + detString + " frantically.";
               bcastString = pname + " waves at " + rname + " frantically.";
            }
         }
      } else if (emote == 2014) {
         emoteSound = "sound.emote.call";
         if (isBodyPart) {
            performerString = "You call out to " + ownerString + " " + item.getName() + ".";
            bcastString = pname + " calls out to " + possessionString + " " + item.getName() + ".";
         } else {
            performerString = "You call out to " + detString + ". It better be ready now!";
            bcastString = pname + " calls out to " + rname + ".";
         }
      } else if (emote == 2021) {
         emoteSound = "sound.emote.laugh";
         if (isBodyPart) {
            performerString = "You laugh hysterically at " + ownerString + " " + item.getName() + ".";
            bcastString = pname + " laughs hysterically at " + possessionString + " " + item.getName() + ".";
         } else {
            performerString = "You laugh hysterically at " + detString + ".";
            bcastString = pname + " laughs hysterically at " + rname + ".";
         }
      } else if (emote == 2022) {
         emoteSound = "sound.emote.cry";
         if (isBodyPart) {
            performerString = "You cry over " + ownerString + " " + item.getName() + ".";
            bcastString = pname + " cries over " + possessionString + " " + item.getName() + ".";
         } else {
            performerString = "You cry over " + detString + ".";
            bcastString = pname + " cries over " + rname + ".";
         }
      } else if (emote == 2023) {
         if (isBodyPart) {
            performerString = "You point at " + ownerString + " " + item.getName() + ".";
            bcastString = pname + " points at " + possessionString + " " + item.getName() + ".";
         } else {
            performerString = "You point at " + detString + ".";
            bcastString = pname + " points at " + rname + ".";
         }
      } else if (emote == 2015) {
         if (isBodyPart) {
            performerString = "You poke " + ownerString + " " + item.getName() + ".";
            bcastString = pname + " pokes " + possessionString + " " + item.getName() + ".";
         } else {
            performerString = "You poke " + detString + ".";
            bcastString = pname + " pokes " + rname + ".";
         }
      } else if (emote == 2016) {
         if (isBodyPart) {
            performerString = "You roll your eyes at " + ownerString + " " + item.getName() + ".";
            bcastString = pname + " rolls " + performer.getHisHerItsString() + " eyes at " + possessionString + " " + item.getName() + ".";
         } else {
            performerString = "You roll your eyes at " + detString + ".";
            bcastString = pname + " rolls with " + performer.getHisHerItsString() + " eyes at " + rname + ".";
         }
      } else if (emote == 2017) {
         if (isBodyPart) {
            performerString = "You stare at " + ownerString + " " + item.getName() + " sceptically.";
            bcastString = pname + " stares at " + possessionString + " " + item.getName() + " sceptically.";
         } else {
            performerString = "You stare at " + detString + " sceptically.";
            bcastString = pname + " stares at " + rname + " sceptically.";
         }
      } else if (emote == 2018) {
         if (isBodyPart) {
            performerString = "You sigh and wonder what the future holds for " + ownerString + " " + item.getName() + ".";
            bcastString = pname + " lets out a loud sigh, obviously worrying about " + possessionString + " " + item.getName() + ".";
         } else {
            performerString = "You sigh and wonder what the future holds for " + detString + ".";
            bcastString = pname + " lets out a loud sigh, obviously worrying about " + rname + ".";
         }
      } else if (emote == 2019) {
         emoteSound = "sound.emote.disagree";
         if (isBodyPart) {
            performerString = "You look at " + ownerString + " " + item.getName() + " and shake your head in disagreement.";
            bcastString = pname
               + " looks at "
               + possessionString
               + " "
               + item.getName()
               + " and shakes "
               + performer.getHisHerItsString()
               + " head in disagreement.";
         } else {
            performerString = "You roll your eyes and shake your head in disagreement with " + detString + ".";
            bcastString = pname
               + " shakes "
               + performer.getHisHerItsString()
               + " head and rolls "
               + performer.getHisHerItsString()
               + " eyes in disagreement with "
               + rname
               + ".";
         }
      } else if (emote == 2020) {
         emoteSound = "sound.emote.tease";
         if (isBodyPart) {
            performerString = "You try to tease people with " + ownerString + " " + item.getName() + ".";
            bcastString = pname + " tries to tease you with " + possessionString + " " + item.getName() + ".";
         } else {
            performerString = "You try to tease people with " + detString + ".";
            bcastString = pname + " tries to tease you with " + rname + ".";
         }
      } else if (emote == 2024) {
         emoteSound = "sound.emote.spit";
         if (isBodyPart) {
            performerString = "You drool all over " + ownerString + " " + item.getName() + ".";
            bcastString = pname + " drools all over " + possessionString + " " + item.getName() + " .";
         } else {
            performerString = "You spit at " + detString + ".";
            bcastString = pname + " spits at " + rname + ".";
         }
      } else if (emote == 2025) {
         emoteSound = "sound.emote.fart";
         if (isBodyPart) {
            performerString = "You fart.";
            bcastString = pname + " farts.";
         } else {
            performerString = "You fart on " + detString + ".";
            bcastString = pname + " farts on " + rname + ".";
         }
      } else if (emote == 2026) {
         emoteSound = "sound.emote.insult";
         if (isBodyPart) {
            performerString = "You call " + ownerString + " " + item.getName() + " names.";
            bcastString = pname + " calls " + possessionString + " " + item.getName() + " names.";
         } else {
            performerString = "You call " + detString + " names.";
            bcastString = pname + " calls " + rname + " names.";
         }
      } else if (emote == 2027) {
         if (isBodyPart) {
            performerString = "You try to push " + ownerString + " " + item.getName() + " to no avail.";
            bcastString = pname + " tries to push " + possessionString + " " + item.getName() + " to no avail.";
         } else {
            performerString = "You wish you could push around " + detString + ".";
         }
      } else if (emote == 2028) {
         emoteSound = "sound.emote.curse";
         if (isBodyPart) {
            performerString = "You let out heavy curses over " + ownerString + " " + item.getName() + ".";
            bcastString = pname + " lets out a long tirade of curses over " + possessionString + " " + item.getName() + ".";
         } else {
            performerString = "You let out heavy curses over " + detString + ".";
            bcastString = pname + " lets out a long tirade of curses over " + rname + ".";
         }
      } else if (emote == 2029) {
         emoteSound = "sound.emote.slap";
         if (isBodyPart) {
            performerString = "You slap " + ownerString + " " + item.getName() + ". Ouch!";
            bcastString = pname + " slaps " + possessionString + " " + item.getName() + " and causes some pain.";
         } else {
            performerString = "You slap at " + detString + ".";
            bcastString = pname + " slaps at " + rname + ".";
         }
      }

      if (performer.mayEmote()) {
         performer.makeEmoteSound();
         Methods.sendSound(performer, emoteSound + emoteGender);
         performer.playAnimation(emoteEntrys[emote - 2000].getActionString().toLowerCase(), false);
      }

      Server.getInstance().broadCastAction(bcastString, performer, 5);
      performer.getCommunicator().sendNormalServerMessage(performerString);
   }

   static void emote(short emote, Creature performer) {
      String performerString = "";
      String bcastString = "";
      String pname = performer.getNameWithGenus();
      if (emote == 2000) {
         performerString = "You smile happily.";
         bcastString = pname + " smiles happily.";
      } else if (emote == 2001) {
         performerString = "You chuckle.";
         bcastString = pname + " chuckles.";
      } else if (emote == 2002) {
         performerString = "You give a round of applause.";
         bcastString = pname + " gives a round of applause.";
      } else if (emote == 2003) {
         performerString = "You look around for someone to hug but find only yourself.";
      } else if (emote == 2004) {
         performerString = "You fondly blow kisses to the air.";
         bcastString = pname + " fondly blows kisses to the air.";
      } else if (emote == 2005) {
         performerString = "You throw yourself to the ground and grovel in the dirt, humiliated.";
         bcastString = pname + " grovels in the dirt, humiliated.";
      } else if (emote == 2030) {
         performerString = "You command your imaginary friend to follow you.";
         bcastString = StringUtil.format("%s commands someone or something to follow %s.", pname, performer.getHimHerItString());
      } else if (emote == 2031) {
         performerString = "You stare vacantly into the sky and whisper goodbye.";
         bcastString = StringUtil.format("%s stares vacantly into the sky and whispers goodbye.", pname);
      } else if (emote == 2032) {
         performerString = "You gaze into the sky and ask the gods for guidance.";
         bcastString = StringUtil.format("%s turns %s gaze into the sky and asks for guidance.", pname, performer.getHisHerItsString());
      } else if (emote == 2033) {
         performerString = "Yes this is the way, I'm sure of it. You mumble quietly.";
         bcastString = StringUtil.format("%s mumbles something.", pname);
      } else if (emote == 2034) {
         performerString = "With tear filled eyes you look around you, you were so sure this was the way home.";
         bcastString = StringUtil.format("%s looks around with tear filled eyes.", pname);
      } else if (emote == 2006) {
         performerString = "You fall to your knees and worship.";
         bcastString = pname + " falls to the ground and worships.";
      } else if (emote == 2007) {
         performerString = "You try to cheer up. Cheer up!";
      } else if (emote == 2008) {
         performerString = "You dance and frolic.";
         bcastString = pname + " dances around joyfully.";
      } else if (emote == 2009) {
         performerString = "You flirtilly wink all around.";
         bcastString = pname + " smiles and winks at everyone nearby.";
      } else if (emote == 2010) {
         performerString = "You bow deeply.";
         bcastString = pname + " bows deeply.";
      } else if (emote == 2011) {
         performerString = "You bend down and gently kiss your own hand.";
         bcastString = pname + " gently bends down and kisses " + performer.getHisHerItsString() + " own hand.";
      } else if (emote == 2012) {
         performerString = "You tickle the air! Weehee!";
         bcastString = pname + " gleefully tickles, tickles, tickles the air around " + performer.getHimHerItString() + "!";
      } else if (emote == 2013) {
         performerString = "You wave.";
         bcastString = pname + " waves.";
      } else if (emote == 2014) {
         performerString = "You call out to the wind! Now or never!";
         bcastString = pname + " calls out to the wind, boosting morale.";
      } else if (emote == 2021) {
         performerString = "You laugh hysterically.";
         bcastString = pname + " laughs hysterically.";
      } else if (emote == 2022) {
         performerString = "You cry bitter tears.";
         bcastString = pname + " cries.";
      } else if (emote == 2023) {
         performerString = "You point forward.";
         bcastString = pname + " points forward.";
      } else if (emote == 2015) {
         performerString = "You poke the air in front of you.";
         bcastString = pname + " pokes with " + performer.getHisHerItsString() + " finger in mid-air.";
      } else if (emote == 2016) {
         performerString = "You roll your eyes.";
         bcastString = pname + " rolls with " + performer.getHisHerItsString() + " eyes.";
      } else if (emote == 2017) {
         performerString = "You stare sceptically into the darkest corners of eternity.";
         bcastString = pname + " suddenly has a very sceptic look upon " + performer.getHisHerItsString() + " face.";
      } else if (emote == 2018) {
         performerString = "You sigh and worry about the future.";
         bcastString = pname + " lets out a huge sigh, obviously worrying about something.";
      } else if (emote == 2019) {
         performerString = "You roll your eyes and shake your head in disagreement with the situation.";
         bcastString = pname
            + " shakes "
            + performer.getHisHerItsString()
            + " head and rolls "
            + performer.getHisHerItsString()
            + " eyes in disagreement with something.";
      } else if (emote == 2020) {
         performerString = "You try to tease everyone around you.";
         bcastString = pname + " obviously tries to tease everyone into action.";
      } else if (emote == 2024) {
         performerString = "You spit.";
         bcastString = pname + " spits.";
      } else if (emote == 2025) {
         performerString = "You fart.";
         bcastString = pname + " farts.";
      } else if (emote == 2026) {
         performerString = "You call yourself names.";
         bcastString = pname + " mumbles aggressively.";
      } else if (emote == 2027) {
         performerString = "You wish you could push someone around.";
      } else if (emote == 2028) {
         performerString = "You let out heavy curses.";
         bcastString = pname + " lets out a long tirade of curses.";
      } else if (emote == 2029) {
         performerString = "You slap yourself in the face. Stupid!";
         bcastString = pname + " slaps " + performer.getHisHerItsString() + " in the face, as if regretting something.";
      }

      if (performer.mayEmote()) {
         performer.makeEmoteSound();
         performer.playAnimation(emoteEntrys[emote - 2000].getActionString().toLowerCase(), false);
      }

      Server.getInstance().broadCastAction(bcastString, performer, 5);
      performer.getCommunicator().sendNormalServerMessage(performerString);
   }

   static {
      createDefaultNiceEmotes();
      createDefaultNeutralEmotes();
      createDefaultOffensiveEmotes();
      createEmoteList();
   }
}
