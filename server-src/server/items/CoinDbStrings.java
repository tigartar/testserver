/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.items;

import com.wurmonline.server.items.DbStrings;

public final class CoinDbStrings
implements DbStrings {
    private static CoinDbStrings instance;

    private CoinDbStrings() {
    }

    @Override
    public String createItem() {
        return "insert into COINS (WURMID, TEMPLATEID, NAME,QUALITYLEVEL,ORIGINALQUALITYLEVEL, LASTMAINTAINED, OWNERID, SIZEX, SIZEY, SIZEZ, ZONEID, DAMAGE, ROTATION, PARENTID, WEIGHT, MATERIAL, LOCKID,DESCRIPTION,CREATIONDATE,RARITY,CREATOR,ONBRIDGE,SETTINGS) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    }

    @Override
    public String transferItem() {
        return "insert into COINS (WURMID, TEMPLATEID, NAME,QUALITYLEVEL,ORIGINALQUALITYLEVEL, LASTMAINTAINED, OWNERID, SIZEX, SIZEY, SIZEZ, ZONEID, DAMAGE, ROTATION, PARENTID, WEIGHT, MATERIAL, LOCKID,DESCRIPTION,BLESS,ENCHANT,TEMPERATURE, PRICE,BANKED,AUXDATA,CREATIONDATE,CREATIONSTATE,REALTEMPLATE,WORNARMOUR,COLOR,COLOR2,PLACE,POSX,POSY,POSZ,CREATOR,FEMALE,MAILED,MAILTIMES,RARITY,ONBRIDGE,LASTOWNERID,SETTINGS) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    }

    @Override
    public String loadItem() {
        return "select * from COINS where WURMID=?";
    }

    @Override
    public String loadEffects() {
        return "select * from EFFECTS where OWNER=?";
    }

    @Override
    public String getLock() {
        return "select * from LOCKS where WURMID=?";
    }

    @Override
    public String getKeys() {
        return "select KEYID from ITEMKEYS where LOCKID=?";
    }

    @Override
    public String addKey() {
        return "INSERT INTO ITEMKEYS (LOCKID,KEYID) VALUES(?,?)";
    }

    @Override
    public String removeKey() {
        return "DELETE FROM ITEMKEYS WHERE KEYID=? AND LOCKID=?";
    }

    @Override
    public String createLock() {
        return "insert into LOCKS ( WURMID, LOCKED) values(?,?)";
    }

    @Override
    public String setZoneId() {
        return "UPDATE COINS SET ZONEID=? WHERE WURMID=?";
    }

    @Override
    public String getZoneId() {
        return "SELECT ZONEID FROM COINS WHERE WURMID=?";
    }

    @Override
    public String setParentId() {
        return "UPDATE COINS SET PARENTID=? WHERE WURMID=?";
    }

    @Override
    public String getParentId() {
        return "SELECT PARENTID FROM COINS WHERE WURMID=?";
    }

    @Override
    public String setTemplateId() {
        return "UPDATE COINS SET TEMPLATEID=? WHERE WURMID=?";
    }

    @Override
    public String getTemplateId() {
        return "SELECT TEMPLATEID FROM COINS WHERE WURMID=?";
    }

    @Override
    public String setName() {
        return "UPDATE COINS SET NAME=? WHERE WURMID=?";
    }

    @Override
    public String getName() {
        return "SELECT NAME FROM COINS WHERE WURMID=?";
    }

    @Override
    public String setDescription() {
        return "UPDATE COINS SET DESCRIPTION=? WHERE WURMID=?";
    }

    @Override
    public String getInscription() {
        return "SELECT INSCRIPTION FROM INSCRIPTIONS WHERE WURMID=?";
    }

    @Override
    public String setInscription() {
        return "UPDATE INSCRIPTIONS SET INSCRIPTION = ? WHERE WURMID=?";
    }

    @Override
    public String createInscription() {
        return "insert into INSCRIPTIONS (WURMID, INSCRIPTION, INSCRIBER) VALUES (?,?,?)";
    }

    @Override
    public String setRarity() {
        return "UPDATE COINS SET RARITY=? WHERE WURMID=?";
    }

    @Override
    public String getDescription() {
        return "SELECT DESCRIPTION FROM COINS WHERE WURMID=?";
    }

    @Override
    public String setPlace() {
        return "UPDATE COINS SET PLACE=? WHERE WURMID=?";
    }

    @Override
    public String getPlace() {
        return "SELECT PLACE FROM COINS WHERE WURMID=?";
    }

    @Override
    public String setQualityLevel() {
        return "UPDATE COINS SET QUALITYLEVEL=? WHERE WURMID=?";
    }

    @Override
    public String getQualityLevel() {
        return "SELECT QUALITYLEVEL FROM COINS WHERE WURMID=?";
    }

    @Override
    public String setOriginalQualityLevel() {
        return "UPDATE COINS SET ORIGINALQUALITYLEVEL=? WHERE WURMID=?";
    }

    @Override
    public String getOriginalQualityLevel() {
        return "SELECT ORIGINALQUALITYLEVEL FROM COINS WHERE WURMID=?";
    }

    @Override
    public String setLastMaintained() {
        return "INSERT INTO COINS (LASTMAINTAINED, WURMID) VALUES (?, ?) ON DUPLICATE KEY UPDATE LASTMAINTAINED=VALUES(LASTMAINTAINED)";
    }

    @Override
    public String setLastMaintainedOld() {
        return "UPDATE COINS SET LASTMAINTAINED=? WHERE WURMID=?";
    }

    @Override
    public String getLastMaintained() {
        return "SELECT LASTMAINTAINED FROM COINS WHERE WURMID=?";
    }

    @Override
    public String setOwnerId() {
        return "UPDATE COINS SET OWNERID=? WHERE WURMID=?";
    }

    @Override
    public String setLastOwnerId() {
        return "UPDATE COINS SET LASTOWNERID=? WHERE WURMID=?";
    }

    @Override
    public String getOwnerId() {
        return "SELECT OWNERID FROM COINS WHERE WURMID=?";
    }

    @Override
    public String setPosXYZRotation() {
        return "UPDATE COINS SET POSX=?, POSY=?, POSZ=?, ROTATION=? WHERE WURMID=?";
    }

    @Override
    public String getPosXYZRotation() {
        return "SELECT POSX, POSY, POSZ, ROTATION FROM COINS WHERE WURMID=?";
    }

    @Override
    public String setPosXYZ() {
        return "UPDATE COINS SET POSX=?, POSY=?, POSZ=? WHERE WURMID=?";
    }

    @Override
    public String getPosXYZ() {
        return "SELECT POSX, POSY, POSZ FROM COINS WHERE WURMID=?";
    }

    @Override
    public String setPosXY() {
        return "UPDATE COINS SET POSX=?, POSY=? WHERE WURMID=?";
    }

    @Override
    public String getPosXY() {
        return "SELECT POSX, POSY FROM COINS WHERE WURMID=?";
    }

    @Override
    public String setPosX() {
        return "UPDATE COINS SET POSX=? WHERE WURMID=?";
    }

    @Override
    public String getPosX() {
        return "SELECT POSX FROM COINS WHERE WURMID=?";
    }

    @Override
    public String setWeight() {
        return "UPDATE COINS SET WEIGHT=? WHERE WURMID=?";
    }

    @Override
    public String getWeight() {
        return "SELECT WEIGHT FROM COINS WHERE WURMID=?";
    }

    @Override
    public String setPosY() {
        return "UPDATE COINS SET POSY=? WHERE WURMID=?";
    }

    @Override
    public String getPosY() {
        return "SELECT POSY FROM COINS WHERE WURMID=?";
    }

    @Override
    public String setPosZ() {
        return "UPDATE COINS SET POSZ=? WHERE WURMID=?";
    }

    @Override
    public String getPosZ() {
        return "SELECT POSZ FROM COINS WHERE WURMID=?";
    }

    @Override
    public String setRotation() {
        return "UPDATE COINS SET ROTATION=? WHERE WURMID=?";
    }

    @Override
    public String getRotation() {
        return "SELECT ROTATION FROM COINS WHERE WURMID=?";
    }

    @Override
    public String savePos() {
        return "UPDATE COINS SET POSX=?,POSY=?,POSZ=?,ROTATION=?,ONBRIDGE=? WHERE WURMID=?";
    }

    @Override
    public String clearItem() {
        return "UPDATE COINS SET NAME=?,DESCRIPTION=?,QUALITYLEVEL=?,ORIGINALQUALITYLEVEL=?,LASTMAINTAINED=?,ENCHANT=?,BANKED=?,SIZEX=?,SIZEY=?,SIZEZ=?,ZONEID=?,DAMAGE=?,PARENTID=?, ROTATION=?,WEIGHT=?,POSX=?,POSY=?,POSZ=?,CREATOR=?,AUXDATA=?,COLOR=?,COLOR2=?,TEMPERATURE=?,CREATIONDATE=?,CREATIONSTATE=0,MATERIAL=?, BLESS=?, MAILED=0, MAILTIMES=0,RARITY=?,CREATIONSTATE=?, OWNERID=-10, LASTOWNERID=-10 WHERE WURMID=?";
    }

    @Override
    public String setDamage() {
        return "INSERT INTO COINS (DAMAGE, LASTMAINTAINED, WURMID) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE LASTMAINTAINED=VALUES(LASTMAINTAINED)";
    }

    @Override
    public String setDamageOld() {
        return "UPDATE COINS SET DAMAGE=?, LASTMAINTAINED=? WHERE WURMID=?";
    }

    @Override
    public String getDamage() {
        return "SELECT DAMAGE FROM COINS WHERE WURMID=?";
    }

    @Override
    public String setLocked() {
        return "UPDATE LOCKS SET LOCKED=? WHERE WURMID=?";
    }

    @Override
    public String getLocked() {
        return "SELECT LOCKED FROM LOCKS WHERE WURMID=?";
    }

    @Override
    public String setTransferred() {
        return "UPDATE COINS SET TRANSFERRED=? WHERE WURMID=?";
    }

    @Override
    public String getAllItems() {
        return "SELECT * from COINS where PARENTID=?";
    }

    @Override
    public String getItem() {
        return "SELECT * from COINS where WURMID=?";
    }

    @Override
    public String setBless() {
        return "UPDATE COINS SET BLESS=? WHERE WURMID=?";
    }

    @Override
    public String setSizeX() {
        return "UPDATE COINS SET SIZEX=? WHERE WURMID=?";
    }

    @Override
    public String getSizeX() {
        return "SELECT SIZEX FROM COINS WHERE WURMID=?";
    }

    @Override
    public String setSizeY() {
        return "UPDATE COINS SET SIZEY=? WHERE WURMID=?";
    }

    @Override
    public String getSizeY() {
        return "SELECT SIZEY FROM COINS WHERE WURMID=?";
    }

    @Override
    public String setSizeZ() {
        return "UPDATE COINS SET SIZEZ=? WHERE WURMID=?";
    }

    @Override
    public String getSizeZ() {
        return "SELECT SIZEZ FROM COINS WHERE WURMID=?";
    }

    @Override
    public String setLockId() {
        return "UPDATE COINS SET LOCKID=? WHERE WURMID=?";
    }

    @Override
    public String setPrice() {
        return "UPDATE COINS SET PRICE=? WHERE WURMID=?";
    }

    @Override
    public String setAuxData() {
        return "UPDATE COINS SET AUXDATA=? WHERE WURMID=?";
    }

    @Override
    public String setCreationState() {
        return "UPDATE COINS SET CREATIONSTATE=? WHERE WURMID=?";
    }

    @Override
    public String setRealTemplate() {
        return "UPDATE COINS SET REALTEMPLATE=? WHERE WURMID=?";
    }

    @Override
    public String setColor() {
        return "UPDATE COINS SET COLOR=?,COLOR2=? WHERE WURMID=?";
    }

    @Override
    public String setEnchant() {
        return "UPDATE COINS SET ENCHANT=? WHERE WURMID=?";
    }

    @Override
    public String setBanked() {
        return "UPDATE COINS SET BANKED=? WHERE WURMID=?";
    }

    @Override
    public String getData() {
        return "select * from ITEMDATA where WURMID=?";
    }

    @Override
    public String createData() {
        return "insert into ITEMDATA ( DATA1, DATA2, EXTRA1, EXTRA2, WURMID) values(?,?,?,?,?)";
    }

    @Override
    public String updateData1() {
        return "update ITEMDATA set DATA1=? where WURMID=?";
    }

    @Override
    public String updateData2() {
        return "update ITEMDATA set DATA2=? where WURMID=?";
    }

    @Override
    public String updateExtra1() {
        return "update ITEMDATA set EXTRA1=? where WURMID=?";
    }

    @Override
    public String updateExtra2() {
        return "update ITEMDATA set EXTRA2=? where WURMID=?";
    }

    @Override
    public String updateAllData() {
        return "update ITEMDATA set DATA1=?, DATA2=?, EXTRA1=?, EXTRA2=? where WURMID=?";
    }

    @Override
    public String setTemperature() {
        return "UPDATE COINS SET TEMPERATURE=? WHERE WURMID=?";
    }

    @Override
    public String getTemperature() {
        return "SELECT TEMPERATURE FROM COINS WHERE WURMID=?";
    }

    @Override
    public String setMaterial() {
        return "UPDATE COINS SET MATERIAL=? WHERE WURMID=?";
    }

    @Override
    public String setWornAsArmour() {
        return "UPDATE COINS SET WORNARMOUR=? WHERE WURMID=?";
    }

    @Override
    public String setFemale() {
        return "UPDATE COINS SET FEMALE=? WHERE WURMID=?";
    }

    @Override
    public String setMailed() {
        return "UPDATE COINS SET MAILED=? WHERE WURMID=?";
    }

    @Override
    public String setCreator() {
        return "UPDATE COINS SET CREATOR=? WHERE WURMID=?";
    }

    @Override
    public String getZoneItems() {
        return "SELECT * FROM COINS WHERE OWNERID=-10";
    }

    @Override
    public String getCreatureItems() {
        return "SELECT * FROM COINS WHERE OWNERID=?";
    }

    @Override
    public String getPreloadedItems() {
        return "SELECT * FROM COINS WHERE TEMPLATEID=?";
    }

    @Override
    public String getCreatureItemsNonTransferred() {
        return "SELECT WURMID FROM COINS WHERE OWNERID=? AND TRANSFERRED=0";
    }

    @Override
    public String updateLastMaintainedBankItem() {
        return "UPDATE COINS SET LASTMAINTAINED=? WHERE BANKED=1";
    }

    @Override
    public String getItemWeights() {
        return "SELECT WURMID, WEIGHT,SIZEX,SIZEY,SIZEZ, TEMPLATEID FROM COINS";
    }

    @Override
    public String getOwnedItems() {
        return "SELECT OWNERID FROM COINS WHERE OWNERID>0 GROUP BY OWNERID";
    }

    @Override
    public String deleteByOwnerId() {
        return "UPDATE COINS SET OWNERID=-10 WHERE OWNERID=?";
    }

    @Override
    public String deleteTransferedItem() {
        return "DELETE FROM COINS WHERE WURMID=? AND TRANSFERRED=0";
    }

    @Override
    public String deleteItem() {
        return "delete from COINS where WURMID=?";
    }

    @Override
    public String getRecycledItems() {
        return "SELECT * FROM COINS WHERE TEMPLATEID=? AND BANKED=1";
    }

    @Override
    public String getItemsForZone() {
        return "Select WURMID from COINS where ZONEID=? AND BANKED=0";
    }

    @Override
    public String setHidden() {
        return "UPDATE COINS SET HIDDEN=? WHERE WURMID=?";
    }

    @Override
    public String setSettings() {
        return "UPDATE COINS SET SETTINGS=? WHERE WURMID=?";
    }

    @Override
    public String setMailTimes() {
        return "UPDATE COINS SET MAILTIMES=? WHERE WURMID=?";
    }

    @Override
    public String freeze() {
        return "INSERT INTO FROZENITEMS SELECT * FROM ITEMS WHERE WURMID=?";
    }

    @Override
    public String thaw() {
        return "INSERT INTO ITEMS SELECT * FROM FROZENITEMS WHERE WURMID=?";
    }

    public static CoinDbStrings getInstance() {
        if (instance == null) {
            instance = new CoinDbStrings();
        }
        return instance;
    }

    @Override
    public final String getDbStringsType() {
        return "CoinDbStrings";
    }
}

