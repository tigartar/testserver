package com.wurmonline.server.tutorial;

import com.wurmonline.server.MiscConstants;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public final class OldMission implements MiscConstants {
   private static final Logger logger = Logger.getLogger(OldMission.class.getName());
   public final int number;
   public String title = "";
   public String missionDescription = "";
   public String missionDescription2 = "";
   public String missionDescription3 = "";
   public boolean setNewbieItemByte = false;
   public String doneString = "";
   public String checkBoxString = "";
   public int itemTemplateRewardId = -1;
   public int itemTemplateRewardNumbers = 1;
   public float itemTemplateRewardQL = 10.0F;
   public static final int FINISHED = 9999;
   private static final Map<Integer, OldMission> wlmissions = new HashMap<>();
   private static final Map<Integer, OldMission> blmissions = new HashMap<>();

   public boolean hasCheckBox() {
      return !this.checkBoxString.equals("");
   }

   private OldMission(int _number, byte kingdom) {
      this.number = _number;
      if (kingdom == 3) {
         blmissions.put(this.number, this);
      } else {
         wlmissions.put(this.number, this);
      }
   }

   private OldMission(
      int aNumber,
      byte aKingdom,
      String aTitle,
      String aMissionDescriptionOne,
      String aMissionDescriptionTwo,
      String aMissionDescriptionThree,
      String aCheckBoxString,
      int aItemTemplateRewardId,
      int aItemTemplateRewardNumbers,
      float aItemTemplateRewardQL,
      boolean aSetNewbieItemByte
   ) {
      this.number = aNumber;
      if (aKingdom == 3) {
         blmissions.put(this.number, this);
      } else {
         wlmissions.put(this.number, this);
      }

      this.title = aTitle;
      this.missionDescription = aMissionDescriptionOne;
      this.missionDescription2 = aMissionDescriptionTwo;
      this.missionDescription3 = aMissionDescriptionThree;
      this.checkBoxString = aCheckBoxString;
      this.itemTemplateRewardId = aItemTemplateRewardId;
      this.itemTemplateRewardNumbers = aItemTemplateRewardNumbers;
      this.itemTemplateRewardQL = aItemTemplateRewardQL;
      this.setNewbieItemByte = aSetNewbieItemByte;
   }

   public static OldMission getMission(int num, byte kingdom) {
      return kingdom == 3 ? blmissions.get(num) : wlmissions.get(num);
   }

   private static void createMissions() {
      long start = System.currentTimeMillis();
      OldMission mj0 = new OldMission(0, (byte)1);
      mj0.title = "Initial instructions";
      mj0.missionDescription = "Welcome to these lands. We are at war, I have little time and you have a lot to learn in order to survive.";
      mj0.missionDescription2 = "I suggest we get you started immediately.";
      mj0.missionDescription3 = "Please press F2 to check your skills, and F3 to look at your inventory. Also press c in order to see your character window.";
      mj0.checkBoxString = "I have done that now";
      OldMission ml0 = new OldMission(0, (byte)3);
      ml0.title = "Initial instructions";
      ml0.missionDescription = "Stop fooling around! We are at war, I have little time and you have a lot to learn in order to survive.";
      ml0.missionDescription2 = "We need to get you started immediately.";
      ml0.missionDescription3 = "Press F2 to check your skills, and F3 to look at your inventory. Also press c in order to see your character window.";
      ml0.checkBoxString = "I have done that now";
      OldMission mj1 = new OldMission(1, (byte)1);
      mj1.title = "Equipping";
      mj1.missionDescription = "Okay, click the hatchet in the inventory window. Notice how it becomes 'selected' at the bottom of the inventory window.";
      mj1.checkBoxString = "I have done that now";
      OldMission ml1 = new OldMission(1, (byte)3);
      ml1.title = "Equipping";
      ml1.missionDescription = "Open your inventory window for gods sake and click the hatchet. It should become 'selected' at the bottom of the inventory window.";
      ml1.checkBoxString = "I have done that now";
      OldMission mj2 = new OldMission(2, (byte)1);
      mj2.title = "Wearing";
      mj2.missionDescription = "You should wear the shield on your left arm.";
      mj2.missionDescription2 = "Put the shield in the Shield slot. Good against dangerous creatures. Make sure you wield your sword.";
      mj2.checkBoxString = "Ok that worked";
      OldMission ml2 = new OldMission(2, (byte)3);
      ml2.title = "Wearing";
      ml2.missionDescription = "You should wear the shield on your left arm.";
      ml2.missionDescription2 = "Pput the shield in the Shield slot. Dangerous creatures or the enemy may show up. Make sure you wield your sword.";
      ml2.checkBoxString = "Yes, master";
      OldMission mj3 = new OldMission(3, (byte)1);
      mj3.title = "Cutting wood";
      mj3.missionDescription = "You must learn how to gather resources. Select your hatchet in your inventory by double-clicking it and go find a tree outside this village.";
      mj3.missionDescription2 = "Right-click the tree and cut it down.";
      mj3.missionDescription3 = "Then chop the tree up and bring some wood.";
      mj3.doneString = "Take the wood by right-clicking it and selecting Take and bring it back with you.";
      OldMission ml3 = new OldMission(3, (byte)3);
      ml3.title = "Cutting wood";
      ml3.missionDescription = "Time to stop loitering! Now go find a tree outside this village.";
      ml3.missionDescription2 = "Right-click the tree and cut it down.";
      ml3.missionDescription3 = "Then chop the tree up and bring some wood.";
      ml3.doneString = "Take the wood by right-clicking it and selecting Take and bring it back with you.";
      OldMission mj4 = new OldMission(4, (byte)1);
      mj4.title = "Creating kindling";
      mj4.missionDescription = "In order to make kindling, use an axe, a saw or a knife and right-click a log.";
      OldMission ml4 = new OldMission(4, (byte)3);
      ml4.title = "Creating kindling";
      ml4.missionDescription = "In order to make kindling, use an axe, a saw or a knife and right-click a log.";
      OldMission m = new OldMission(5, (byte)1);
      m.title = "Lighting a fire";
      m.missionDescription = "Now use the flint and steel on the kindling in order to light it.";
      m = new OldMission(5, (byte)3);
      m.title = "Lighting a fire";
      m.missionDescription = "Use the flint and steel on the kindling in order to light it.";
      m = new OldMission(6, (byte)1);
      m.title = "Gathering food";
      m.missionDescription = "In order to keep well fed you may of course kill animals or farm crops.";
      m.missionDescription2 = "The easiest way to find food is however to look on grass tiles for it.";
      m.missionDescription3 = "Go out and forage now. Right-click on green grass tiles.";
      m = new OldMission(6, (byte)3);
      m.title = "Gathering food";
      m.missionDescription = "The mycelium that grows all around is a blessing.";
      m.missionDescription2 = "If you stand on it it will fill you up. You may also absorb mycelium in order to heal your wounds.";
      m.checkBoxString = "Understood";
      m = new OldMission(7, (byte)1);
      m.title = "The bartender";
      m.missionDescription = "There is a bartender around here somewhere. Go find him and ask for refreshments.";
      m.missionDescription2 = "You will receive free refreshments the first 24 hours here.";
      m.missionDescription3 = "This means that you have a good reason to stay closeby and start learning things.";
      m = new OldMission(7, (byte)3);
      m.title = "The bartender";
      m.missionDescription = "There is a bartender around here somewhere. Go find him and ask for refreshments.";
      m.missionDescription2 = "You will receive free refreshments the first 24 hours here.";
      m.missionDescription3 = "This means that you have a good reason to stay closeby and start learning things.";
      m = new OldMission(8, (byte)1);
      m.title = "Digging";
      m.missionDescription = "A very common way to find resources like clay is to use a shovel and dig.";
      m.missionDescription2 = "You also need to flatten land in order to build on it.";
      m.missionDescription3 = "Go out and dig some dirt now. Drop the dirt before returning since it is pretty heavy to carry around.";
      m = new OldMission(8, (byte)3);
      m.title = "Digging";
      m.missionDescription = "A very common way to find resources like clay is to use a shovel and dig.";
      m.missionDescription2 = "You also need to flatten land in order to build on it.";
      m.missionDescription3 = "Go out and dig some dirt now. Drop the dirt before returning since it is pretty heavy to carry around.";
      m = new OldMission(9, (byte)1);
      m.title = "Planks";
      m.missionDescription = "With the saw you have you may create planks. I want you to try one now.";
      m.missionDescription2 = "When that is done all you need are some nails and you can start building a house.";
      m = new OldMission(9, (byte)3);
      m.title = "Planks";
      m.missionDescription = "With the saw you have you may create planks. Try one now.";
      m.missionDescription2 = "When that is done all you need are some nails and you can start building a house.";
      m = new OldMission(10, (byte)1);
      m.title = "Mining";
      m.missionDescription = "Now go use the pickaxe on some rock to get the feel of mining. Select tunnel which eventually will open up into the mountain.";
      m.missionDescription2 = "You may find valuable ore inside a mountain, and even precious gems.";
      m.missionDescription3 = "If you find iron ore, you may smelt it in a fire and use the iron to create an anvil and nails.";
      m.itemTemplateRewardId = 59;
      m.itemTemplateRewardNumbers = 1;
      m.itemTemplateRewardQL = 10.0F;
      m.setNewbieItemByte = false;
      m = new OldMission(10, (byte)3);
      m.title = "Mining";
      m.missionDescription = "Now go use the pickaxe on some rock to get the feel of mining. Select tunnel which eventually will open up into the mountain.";
      m.missionDescription2 = "You may find valuable ore inside a mountain, and even precious gems.";
      m.missionDescription3 = "If you find iron ore, you may smelt it in a fire and use the iron to create an anvil nails.";
      m.itemTemplateRewardId = 59;
      m.itemTemplateRewardNumbers = 1;
      m.itemTemplateRewardQL = 10.0F;
      m.setNewbieItemByte = false;
      m = new OldMission(11, (byte)1);
      m.title = "Final words";
      m.missionDescription = "That's all. A few final advice:";
      m.missionDescription2 = "Try to find a bed when logging off. If you sleep long enough you will be invigorated and receive a bonus for a while when you return.";
      m.missionDescription3 = "When it comes to fighting.. I can't teach you that. Initially, just do as much as possible. Standing still doing nothing is usually less effective. Good luck!";
      m.checkBoxString = "Thank you, I am done here.";
      m = new OldMission(11, (byte)3);
      m.title = "Final words";
      m.missionDescription = "That's all. A few final advice:";
      m.missionDescription2 = "Try to find a bed when logging off. If you sleep long enough you will be invigorated and receive a bonus for a while when you return.";
      m.missionDescription3 = "When it comes to fighting.. I can't teach you that. Initially, just do as much as possible. Standing still doing nothing is usually less effective. Stay alive!";
      m.checkBoxString = "I am ready to venture into the darkness!";
      OldMission ml9999 = new OldMission(9999, (byte)3);
      ml9999.title = "Continuing the instructions";
      ml9999.missionDescription = "Will you continue to follow my instructions?";
      ml9999.checkBoxString = "Yes";
      OldMission mj9999 = new OldMission(9999, (byte)1);
      mj9999.title = "Continuing the instructions";
      mj9999.missionDescription = "Have you come for more instructions?";
      mj9999.checkBoxString = "Yes";
      logger.info(
         "Finished creating "
            + wlmissions.size()
            + " WL and "
            + blmissions.size()
            + " BL Tutorial Missions, that took "
            + (System.currentTimeMillis() - start)
            + " ms"
      );
   }

   static {
      createMissions();
   }
}
