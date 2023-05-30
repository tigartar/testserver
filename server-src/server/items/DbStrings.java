package com.wurmonline.server.items;

public interface DbStrings {
   String createItem();

   String transferItem();

   String loadItem();

   String loadEffects();

   String getLock();

   String getKeys();

   String addKey();

   String removeKey();

   String createLock();

   String setZoneId();

   String getZoneId();

   String setParentId();

   String getParentId();

   String setTemplateId();

   String getTemplateId();

   String setInscription();

   String getInscription();

   String createInscription();

   String setName();

   String getName();

   String setRarity();

   String setDescription();

   String getDescription();

   String setPlace();

   String getPlace();

   String setQualityLevel();

   String getQualityLevel();

   String setOriginalQualityLevel();

   String getOriginalQualityLevel();

   String setLastMaintained();

   String setLastMaintainedOld();

   String getLastMaintained();

   String setOwnerId();

   String setLastOwnerId();

   String getOwnerId();

   String setPosXYZRotation();

   String getPosXYZRotation();

   String setPosXYZ();

   String getPosXYZ();

   String setPosXY();

   String getPosXY();

   String setPosX();

   String getPosX();

   String setWeight();

   String getWeight();

   String setPosY();

   String getPosY();

   String setPosZ();

   String getPosZ();

   String setRotation();

   String getRotation();

   String savePos();

   String clearItem();

   String setDamage();

   String setDamageOld();

   String getDamage();

   String setLocked();

   String getLocked();

   String setTransferred();

   String getAllItems();

   String getItem();

   String setBless();

   String setSizeX();

   String getSizeX();

   String setSizeY();

   String getSizeY();

   String setSizeZ();

   String getSizeZ();

   String setLockId();

   String setPrice();

   String setAuxData();

   String setCreationState();

   String setRealTemplate();

   String setColor();

   String setEnchant();

   String setBanked();

   String getData();

   String createData();

   String updateData1();

   String updateData2();

   String updateExtra1();

   String updateExtra2();

   String updateAllData();

   String setTemperature();

   String getTemperature();

   String setMaterial();

   String setWornAsArmour();

   String setFemale();

   String setMailed();

   String setCreator();

   String getZoneItems();

   String getCreatureItems();

   String getPreloadedItems();

   String getCreatureItemsNonTransferred();

   String updateLastMaintainedBankItem();

   String getItemWeights();

   String getOwnedItems();

   String deleteByOwnerId();

   String deleteTransferedItem();

   String deleteItem();

   String getRecycledItems();

   String getItemsForZone();

   String setHidden();

   String setSettings();

   String setMailTimes();

   String freeze();

   String thaw();

   String getDbStringsType();
}
