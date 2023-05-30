package com.wurmonline.server.items;

import com.wurmonline.server.Features;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.combat.ArmourTemplate;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class TempItem extends Item {
   private static final Logger logger = Logger.getLogger(TempItem.class.getName());

   TempItem() {
   }

   @Override
   public void bless(int blesser) {
      if (this.bless == 0) {
         this.bless = (byte)blesser;
      }
   }

   @Override
   public void setOwnerStuff(ItemTemplate templ) {
   }

   @Override
   public void enchant(byte ench) {
      if (this.enchantment != ench) {
         this.enchantment = ench;
      }
   }

   @Override
   public void setColor(int _color) {
      this.color = _color;
   }

   @Override
   public void setColor2(int _color) {
      this.color2 = _color;
   }

   @Override
   public void setLastOwnerId(long oid) {
      this.lastOwner = oid;
   }

   public TempItem(String aName, ItemTemplate aTemplate, float qLevel, @Nullable String aCreator) throws IOException {
      super(-10L, aName, aTemplate, qLevel, (byte)0, (byte)0, -10L, aCreator);
   }

   public TempItem(long wurmId, short aPlace, String aName, ItemTemplate aTemplate, float qLevel, @Nullable String aCreator) throws IOException {
      super(wurmId, aName, aTemplate, qLevel, (byte)1, (byte)0, -10L, aCreator);
      this.setPlace(aPlace);
   }

   public TempItem(String aName, short aPlace, ItemTemplate aTemplate, float aQualityLevel, String aCreator) throws IOException {
      super(aName, aPlace, aTemplate, aQualityLevel, (byte)0, (byte)0, -10L, aCreator);
   }

   public TempItem(
      String aName, ItemTemplate aTemplate, float aQualityLevel, float aPosX, float aPosY, float aPosZ, float aRotation, long bridgeId, String aCreator
   ) throws IOException {
      super(aName, aTemplate, aQualityLevel, aPosX, aPosY, aPosZ, aRotation, (byte)0, (byte)0, bridgeId, aCreator);
   }

   @Override
   void create(float aQualityLevel, long aCreationDate) throws IOException {
      this.qualityLevel = aQualityLevel;
      this.lastMaintained = aCreationDate;
   }

   @Override
   void load() throws Exception {
   }

   @Override
   public void loadEffects() {
   }

   @Override
   void setPlace(short aPlace) {
      this.place = aPlace;
   }

   @Override
   public short getPlace() {
      return this.place;
   }

   @Override
   public void setLastMaintained(long time) {
      this.lastMaintained = time;
   }

   @Override
   public long getLastMaintained() {
      return this.lastMaintained;
   }

   @Override
   public boolean setQualityLevel(float newLevel) {
      this.qualityLevel = newLevel;
      return false;
   }

   @Override
   public long getOwnerId() {
      return this.ownerId;
   }

   @Override
   public boolean setOwnerId(long aOwnerId) {
      this.ownerId = aOwnerId;
      return true;
   }

   @Override
   public boolean getLocked() {
      return this.locked;
   }

   @Override
   public void setLocked(boolean aLocked) {
      this.locked = aLocked;
   }

   @Override
   public int getTemplateId() {
      return this.template.getTemplateId();
   }

   @Override
   public void setTemplateId(int aId) {
      try {
         this.template = ItemTemplateFactory.getInstance().getTemplate(aId);
      } catch (NoSuchTemplateException var3) {
         logger.log(Level.WARNING, var3.getMessage(), (Throwable)var3);
      }
   }

   @Override
   public void setZoneId(int aId, boolean isOnSurface) {
      this.surfaced = isOnSurface;
      this.zoneId = aId;
   }

   @Override
   public int getZoneId() {
      if (this.parentId != -10L && Items.isItemLoaded(this.parentId)) {
         try {
            Item parent = Items.getItem(this.parentId);
            return parent.getZoneId();
         } catch (NoSuchItemException var2) {
            logger.log(Level.WARNING, "This REALLY shouldn't happen! parentId: " + this.parentId, (Throwable)var2);
         }
      }

      return this.zoneId;
   }

   @Override
   public boolean setDescription(String desc) {
      this.description = desc;
      return false;
   }

   @Override
   public String getDescription() {
      return this.description;
   }

   @Override
   public void setName(String newname) {
      this.name = newname;
   }

   @Override
   public void setName(String newname, boolean sendUpdate) {
      this.setName(newname);
   }

   @Override
   public boolean setInscription(String aInscription, String aInscriber) {
      return this.setInscription(aInscription, aInscriber, 0);
   }

   @Override
   public boolean setInscription(String aInscription, String aInscriber, int penColour) {
      this.inscription.setInscription(aInscription);
      this.inscription.setInscriber(aInscriber);
      this.inscription.setPenColour(penColour);
      return true;
   }

   @Override
   public float getRotation() {
      return this.rotation;
   }

   @Override
   public void setPos(float aPosX, float aPosY, float aPosZ, float aRotation, long bridgeId) {
      this.posX = aPosX;
      this.posY = aPosY;
      this.posZ = aPosZ;
      this.rotation = aRotation;
      this.onBridge = bridgeId;
   }

   @Override
   public void setPosXYZRotation(float _posX, float _posY, float _posZ, float _rot) {
      this.posX = _posX;
      this.posY = _posY;
      this.posZ = _posZ;
      this.rotation = _rot;
   }

   @Override
   public void setPosXYZ(float _posX, float _posY, float _posZ) {
      this.posX = _posX;
      this.posY = _posY;
      this.posZ = _posZ;
   }

   @Override
   public void setPosXY(float _posX, float _posY) {
      this.posX = _posX;
      this.posY = _posY;
   }

   @Override
   public void setPosX(float aPosX) {
      this.posX = aPosX;
   }

   @Override
   public void setPosY(float aPosY) {
      this.posY = aPosY;
   }

   @Override
   public void setPosZ(float aPosZ) {
      this.posZ = aPosZ;
   }

   @Override
   public void setRotation(float aRotation) {
      this.rotation = aRotation;
   }

   @Override
   public float getQualityLevel() {
      return this.qualityLevel;
   }

   @Override
   public float getDamage() {
      return this.damage;
   }

   @Override
   public Set<Item> getItems() {
      if (this.items == null) {
         this.items = new HashSet<>();
      }

      return this.items;
   }

   @Override
   public Item[] getItemsAsArray() {
      return this.items == null ? new Item[0] : this.items.toArray(new Item[this.items.size()]);
   }

   @Override
   public void setParentId(long pid, boolean isOnSurface) {
      this.surfaced = isOnSurface;
      if (this.parentId != pid) {
         if (pid != -10L) {
            try {
               Item parent = Items.getItem(pid);
               if (this.ownerId != parent.getOwnerId() && (parent.getPosX() != this.getPosX() || parent.getPosY() != this.getPosY())) {
                  this.setPosXYZ(this.getPosX(), this.getPosY(), this.getPosZ());
               }
            } catch (NoSuchItemException var6) {
               logger.log(Level.WARNING, var6.getMessage(), (Throwable)var6);
            }
         } else {
            if (this.watchers != null) {
               for(Creature watcher : this.watchers) {
                  watcher.getCommunicator().sendRemoveFromInventory(this);
               }
            }

            this.watchers = null;
         }

         this.parentId = pid;
      }
   }

   @Override
   public long getParentId() {
      return this.parentId;
   }

   @Override
   void setSizeX(int sizex) {
      this.sizeX = sizex;
   }

   @Override
   void setSizeY(int sizey) {
      this.sizeY = sizey;
   }

   @Override
   void setSizeZ(int sizez) {
      this.sizeZ = sizez;
   }

   @Override
   public int getSizeX() {
      return this.sizeX > 0 ? this.sizeX : this.template.getSizeX();
   }

   @Override
   public int getSizeY() {
      return this.sizeY > 0 ? this.sizeY : this.template.getSizeY();
   }

   @Override
   public int getSizeZ() {
      return this.sizeZ > 0 ? this.sizeZ : this.template.getSizeZ();
   }

   @Override
   public void setOriginalQualityLevel(float qlevel) {
   }

   @Override
   public float getOriginalQualityLevel() {
      return this.originalQualityLevel;
   }

   @Override
   public boolean setDamage(float dam) {
      float modifier = 1.0F;
      float difference = dam - this.damage;
      if (difference > 0.0F && this.getSpellEffects() != null) {
         modifier = this.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_DAMAGETAKEN);
         difference *= modifier;
      }

      return this.setDamage(this.damage + difference, false);
   }

   @Override
   public boolean setDamage(float dam, boolean override) {
      this.damage = dam;
      if (dam >= 100.0F) {
         this.setQualityLevel(0.0F);
         this.checkDecay();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void setData1(int data1) {
      if (this.data == null) {
         this.data = new ItemData(this.id, data1, -1, -1, -1);
      }

      this.data.data1 = data1;
   }

   @Override
   public void setData2(int data2) {
      if (this.data == null) {
         this.data = new ItemData(this.id, -1, data2, -1, -1);
      }

      this.data.data2 = data2;
   }

   @Override
   public void setData(int data1, int data2) {
      if (this.data == null) {
         this.data = new ItemData(this.id, data1, data2, -1, -1);
      }

      this.data.data1 = data1;
      this.data.data2 = data2;
   }

   @Override
   public int getData1() {
      return this.data != null ? this.data.data1 : -1;
   }

   @Override
   public int getData2() {
      return this.data != null ? this.data.data2 : -1;
   }

   @Override
   public int getWeightGrams() {
      if (this.getSpellEffects() == null) {
         return this.weight;
      } else {
         float modifier = this.getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_WEIGHT);
         return (int)((float)this.weight * modifier);
      }
   }

   @Override
   public boolean setWeight(int w, boolean destroyOnWeightZero) {
      return this.setWeight(w, destroyOnWeightZero, false);
   }

   @Override
   public boolean setWeight(int w, boolean destroyOnWeightZero, boolean sameOwner) {
      if (this.weight <= 0) {
         Items.destroyItem(this.id);
         return true;
      } else {
         this.weight = w;
         if (this.parentId != -10L) {
            this.updateParents();
         }

         return false;
      }
   }

   @Override
   public byte getMaterial() {
      return this.material;
   }

   @Override
   public void setMaterial(byte mat) {
      this.material = mat;
   }

   @Override
   public long getLockId() {
      return this.lockid;
   }

   @Override
   public void setLockId(long lid) {
      this.lockid = lid;
   }

   @Override
   void addItem(@Nullable Item item, boolean loading) {
      if (item != null) {
         this.getItems().add(item);
         if (this.parentId != -10L) {
            this.updateParents();
         }
      } else {
         logger.warning("Ignored attempt to add a null item to " + this);
      }
   }

   @Override
   void removeItem(Item item) {
      if (this.items != null) {
         this.items.remove(item);
      }

      if (item.wornAsArmour) {
         item.setWornAsArmour(false, this.getOwnerId());
      }

      if (this.parentId != -10L) {
         this.updateParents();
      }
   }

   @Override
   public void setPrice(int newPrice) {
      this.price = newPrice;
   }

   @Override
   public void setTemperature(short temp) {
      this.temperature = temp;
   }

   @Override
   public void setBanked(boolean bank) {
      this.banked = bank;
   }

   @Override
   public void setAuxData(byte auxdata) {
      this.auxbyte = auxdata;
   }

   @Override
   public void setCreationState(byte newState) {
      this.creationState = newState;
   }

   @Override
   public void setRealTemplate(int rTemplate) {
      this.realTemplate = rTemplate;
   }

   @Override
   void setWornAsArmour(boolean wornArmour, long newOwner) {
      if (this.wornAsArmour != wornArmour) {
         this.wornAsArmour = wornArmour;
         if (this.wornAsArmour) {
            try {
               Creature creature = Server.getInstance().getCreature(newOwner);
               ArmourTemplate armour = ArmourTemplate.getArmourTemplate(this.template.templateId);
               if (armour != null) {
                  float moveModChange = armour.getMoveModifier();
                  if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
                     moveModChange *= ArmourTemplate.getMaterialMovementModifier(this.getMaterial());
                  } else if (Servers.localServer.isChallengeOrEpicServer()) {
                     if (this.getMaterial() == 57 || this.getMaterial() == 67) {
                        moveModChange *= 0.9F;
                     } else if (this.getMaterial() == 56) {
                        moveModChange *= 0.95F;
                     }
                  }

                  creature.getMovementScheme().armourMod.setModifier(creature.getMovementScheme().armourMod.getModifier() - (double)moveModChange);
               }
            } catch (NoSuchPlayerException var9) {
               logger.log(Level.WARNING, "Worn armour on unknown player: ", (Throwable)var9);
            } catch (NoSuchCreatureException var10) {
               logger.log(Level.WARNING, "Worn armour on unknown creature: ", (Throwable)var10);
            }
         } else {
            try {
               Creature creature = Server.getInstance().getCreature(this.getOwnerId());
               ArmourTemplate armour = ArmourTemplate.getArmourTemplate(this.template.templateId);
               if (armour != null) {
                  float moveModChange = armour.getMoveModifier();
                  if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
                     moveModChange *= ArmourTemplate.getMaterialMovementModifier(this.getMaterial());
                  } else if (Servers.localServer.isChallengeOrEpicServer()) {
                     if (this.getMaterial() == 57 || this.getMaterial() == 67) {
                        moveModChange *= 0.9F;
                     } else if (this.getMaterial() == 56) {
                        moveModChange *= 0.95F;
                     }
                  }

                  creature.getMovementScheme().armourMod.setModifier(creature.getMovementScheme().armourMod.getModifier() + (double)moveModChange);
               }
            } catch (NoSuchPlayerException var7) {
               logger.log(Level.WARNING, "Worn armour on unknown player: ", (Throwable)var7);
            } catch (NoSuchCreatureException var8) {
               logger.log(Level.WARNING, "Worn armour on unknown creature: ", (Throwable)var8);
            }
         }
      }
   }

   @Override
   public void savePosition() {
   }

   @Override
   public void setFemale(boolean _female) {
      this.female = _female;
   }

   @Override
   public void setTransferred(boolean trans) {
      this.transferred = trans;
   }

   @Override
   void addNewKey(long keyId) {
   }

   @Override
   void removeNewKey(long keyId) {
   }

   @Override
   public void setMailed(boolean _mailed) {
      this.mailed = _mailed;
   }

   @Override
   public void setCreator(String _creator) {
      this.creator = _creator;
   }

   @Override
   public void setHidden(boolean _hidden) {
      this.hidden = _hidden;
   }

   @Override
   public void setDbStrings(DbStrings dbStrings) {
   }

   @Override
   public DbStrings getDbStrings() {
      return null;
   }

   @Override
   void clear(
      long wurmId,
      String _creator,
      float posx,
      float posy,
      float posz,
      float _rot,
      String _desc,
      String _name,
      float _qualitylevel,
      byte _material,
      byte aRarity,
      long bridgeId
   ) {
      this.id = wurmId;
      this.creator = _creator;
      this.posX = posx;
      this.posY = posy;
      this.posZ = posz;
      this.description = _desc;
      this.name = _name;
      this.qualityLevel = _qualitylevel;
      this.originalQualityLevel = this.qualityLevel;
      this.rotation = _rot;
      this.zoneId = -10;
      this.parentId = -10L;
      this.sizeX = this.template.getSizeX();
      this.sizeY = this.template.getSizeY();
      this.sizeZ = this.template.getSizeZ();
      this.weight = this.template.getWeightGrams();
      this.lastMaintained = WurmCalendar.currentTime;
      this.creationDate = WurmCalendar.currentTime;
      this.creationState = 0;
      this.banked = false;
      this.damage = 0.0F;
      this.enchantment = 0;
      this.material = _material;
      this.rarity = aRarity;
      this.onBridge = bridgeId;
      this.creationState = 0;
   }

   @Override
   public void setMailTimes(byte times) {
   }

   @Override
   public void returnFromFreezer() {
   }

   @Override
   public void moveToFreezer() {
   }

   @Override
   public void deleteInDatabase() {
   }

   @Override
   public boolean setRarity(byte newRarity) {
      if (newRarity != this.rarity) {
         this.rarity = newRarity;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void savePermissions() {
   }

   @Override
   boolean saveInscription() {
      return false;
   }

   @Override
   public void setExtra1(int extra1) {
      if (this.data == null) {
         this.data = new ItemData(this.id, -1, -1, -1, -1);
      }

      this.data.extra1 = extra1;
   }

   @Override
   public void setExtra2(int extra2) {
      if (this.data == null) {
         this.data = new ItemData(this.id, -1, -1, -1, -1);
      }

      this.data.extra2 = extra2;
   }

   @Override
   public void setExtra(int extra1, int extra2) {
      if (this.data == null) {
         this.data = new ItemData(this.id, -1, -1, -1, -1);
      }

      this.data.extra1 = extra1;
      this.data.extra2 = extra2;
   }

   @Override
   public int getExtra1() {
      return this.data != null ? this.data.extra1 : -1;
   }

   @Override
   public int getExtra2() {
      return this.data != null ? this.data.extra2 : -1;
   }

   @Override
   public void setAllData(int data1, int data2, int extra1, int extra2) {
      if (this.data == null) {
         this.data = new ItemData(this.id, -1, -1, -1, -1);
      }

      this.data.data1 = data1;
      this.data.data2 = data2;
      this.data.extra1 = extra1;
      this.data.extra2 = extra2;
   }

   @Override
   public void setPlacedOnParent(boolean onParent) {
      this.placedOnParent = onParent;
   }

   @Override
   public boolean isItem() {
      return true;
   }
}
