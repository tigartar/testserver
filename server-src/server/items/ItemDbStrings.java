/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.items;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.items.DbStrings;

public final class ItemDbStrings
implements DbStrings {
    private static ItemDbStrings instance;

    private ItemDbStrings() {
    }

    @Override
    public String createItem() {
        return "insert into ITEMS (WURMID, TEMPLATEID, NAME,QUALITYLEVEL,ORIGINALQUALITYLEVEL, LASTMAINTAINED, OWNERID, SIZEX, SIZEY, SIZEZ, ZONEID, DAMAGE, ROTATION, PARENTID, WEIGHT, MATERIAL, LOCKID,DESCRIPTION,CREATIONDATE,RARITY,CREATOR,ONBRIDGE,SETTINGS) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    }

    @Override
    public String transferItem() {
        return "insert into ITEMS (WURMID, TEMPLATEID, NAME,QUALITYLEVEL,ORIGINALQUALITYLEVEL, LASTMAINTAINED, OWNERID,SIZEX, SIZEY, SIZEZ, ZONEID, DAMAGE, ROTATION, PARENTID, WEIGHT, MATERIAL, LOCKID,DESCRIPTION,BLESS,ENCHANT,TEMPERATURE, PRICE,BANKED,AUXDATA,CREATIONDATE,CREATIONSTATE,REALTEMPLATE,WORNARMOUR,COLOR,COLOR2,PLACE,POSX,POSY,POSZ,CREATOR,FEMALE,MAILED,MAILTIMES,RARITY,ONBRIDGE,LASTOWNERID,SETTINGS) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
    }

    @Override
    public String loadItem() {
        return "select * from ITEMS where WURMID=?";
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
        return "UPDATE ITEMS SET ZONEID=? WHERE WURMID=?";
    }

    @Override
    public String getZoneId() {
        return "SELECT ZONEID FROM ITEMS WHERE WURMID=?";
    }

    @Override
    public String setParentId() {
        return "UPDATE ITEMS SET PARENTID=? WHERE WURMID=?";
    }

    @Override
    public String getParentId() {
        return "SELECT PARENTID FROM ITEMS WHERE WURMID=?";
    }

    @Override
    public String setTemplateId() {
        return "UPDATE ITEMS SET TEMPLATEID=? WHERE WURMID=?";
    }

    @Override
    public String getTemplateId() {
        return "SELECT TEMPLATEID FROM ITEMS WHERE WURMID=?";
    }

    @Override
    public String setInscription() {
        return "UPDATE INSCRIPTIONS SET INSCRIPTION=? WHERE WURMID=?";
    }

    @Override
    public String getInscription() {
        return "SELECT INSCRIPTION FROM INSCRIPTIONS WHERE WURMID=?";
    }

    @Override
    public String createInscription() {
        return "INSERT INTO INSCRIPTIONS (WURMID, INSCRIPTION, INSCRIBER, PENCOLOR) VALUES (?,?,?,?)";
    }

    @Override
    public String setName() {
        return "UPDATE ITEMS SET NAME=? WHERE WURMID=?";
    }

    @Override
    public String getName() {
        return "SELECT NAME FROM ITEMS WHERE WURMID=?";
    }

    @Override
    public String setRarity() {
        return "UPDATE ITEMS SET RARITY=? WHERE WURMID=?";
    }

    @Override
    public String setDescription() {
        return "UPDATE ITEMS SET DESCRIPTION=? WHERE WURMID=?";
    }

    @Override
    public String getDescription() {
        return "SELECT DESCRIPTION FROM ITEMS WHERE WURMID=?";
    }

    @Override
    public String setPlace() {
        return "UPDATE ITEMS SET PLACE=? WHERE WURMID=?";
    }

    @Override
    public String getPlace() {
        return "SELECT PLACE FROM ITEMS WHERE WURMID=?";
    }

    @Override
    public String setQualityLevel() {
        return "UPDATE ITEMS SET QUALITYLEVEL=? WHERE WURMID=?";
    }

    @Override
    public String getQualityLevel() {
        return "SELECT QUALITYLEVEL FROM ITEMS WHERE WURMID=?";
    }

    @Override
    public String setOriginalQualityLevel() {
        return "UPDATE ITEMS SET ORIGINALQUALITYLEVEL=? WHERE WURMID=?";
    }

    @Override
    public String getOriginalQualityLevel() {
        return "SELECT ORIGINALQUALITYLEVEL FROM ITEMS WHERE WURMID=?";
    }

    @Override
    public String setLastMaintained() {
        return "INSERT INTO ITEMS (LASTMAINTAINED, WURMID) VALUES (?, ?) ON DUPLICATE KEY UPDATE LASTMAINTAINED=VALUES(LASTMAINTAINED)";
    }

    @Override
    public String setLastMaintainedOld() {
        return "UPDATE ITEMS SET LASTMAINTAINED=? WHERE WURMID=?";
    }

    @Override
    public String getLastMaintained() {
        return "SELECT LASTMAINTAINED FROM ITEMS WHERE WURMID=?";
    }

    @Override
    public String setOwnerId() {
        return "UPDATE ITEMS SET OWNERID=? WHERE WURMID=?";
    }

    @Override
    public String setLastOwnerId() {
        return "UPDATE ITEMS SET LASTOWNERID=? WHERE WURMID=?";
    }

    @Override
    public String getOwnerId() {
        return "SELECT OWNERID FROM ITEMS WHERE WURMID=?";
    }

    @Override
    public String setPosXYZRotation() {
        return "UPDATE ITEMS SET POSX=?, POSY=?, POSZ=?, ROTATION=? WHERE WURMID=?";
    }

    @Override
    public String getPosXYZRotation() {
        return "SELECT POSX, POSY, POSZ, ROTATION FROM ITEMS WHERE WURMID=?";
    }

    @Override
    public String setPosXYZ() {
        return "UPDATE ITEMS SET POSX=?, POSY=?, POSZ=? WHERE WURMID=?";
    }

    @Override
    public String getPosXYZ() {
        return "SELECT POSX, POSY, POSZ FROM ITEMS WHERE WURMID=?";
    }

    @Override
    public String setPosXY() {
        return "UPDATE ITEMS SET POSX=?, POSY=? WHERE WURMID=?";
    }

    @Override
    public String getPosXY() {
        return "SELECT POSX, POSY FROM ITEMS WHERE WURMID=?";
    }

    @Override
    public String setPosX() {
        return "UPDATE ITEMS SET POSX=? WHERE WURMID=?";
    }

    @Override
    public String getPosX() {
        return "SELECT POSX FROM ITEMS WHERE WURMID=?";
    }

    @Override
    public String setWeight() {
        return "UPDATE ITEMS SET WEIGHT=? WHERE WURMID=?";
    }

    @Override
    public String getWeight() {
        return "SELECT WEIGHT FROM ITEMS WHERE WURMID=?";
    }

    @Override
    public String setPosY() {
        return "UPDATE ITEMS SET POSY=? WHERE WURMID=?";
    }

    @Override
    public String getPosY() {
        return "SELECT POSY FROM ITEMS WHERE WURMID=?";
    }

    @Override
    public String setPosZ() {
        return "UPDATE ITEMS SET POSZ=? WHERE WURMID=?";
    }

    @Override
    public String getPosZ() {
        return "SELECT POSZ FROM ITEMS WHERE WURMID=?";
    }

    @Override
    public String setRotation() {
        return "UPDATE ITEMS SET ROTATION=? WHERE WURMID=?";
    }

    @Override
    public String getRotation() {
        return "SELECT ROTATION FROM ITEMS WHERE WURMID=?";
    }

    @Override
    public String savePos() {
        return "UPDATE ITEMS SET POSX=?,POSY=?,POSZ=?,ROTATION=?,ONBRIDGE=? WHERE WURMID=?";
    }

    @Override
    public String clearItem() {
        return "UPDATE ITEMS SET NAME=?,DESCRIPTION=?,QUALITYLEVEL=?,ORIGINALQUALITYLEVEL=?,LASTMAINTAINED=?,ENCHANT=?,BANKED=?,SIZEX=?,SIZEY=?,SIZEZ=?,ZONEID=?,DAMAGE=?,PARENTID=?, ROTATION=?,WEIGHT=?,POSX=?,POSY=?,POSZ=?,CREATOR=?,AUXDATA=?,COLOR=?,COLOR2=?,TEMPERATURE=?,CREATIONDATE=?,CREATIONSTATE=0,MATERIAL=?, BLESS=?, MAILED=0, MAILTIMES=0,RARITY=?,CREATIONSTATE=?, OWNERID=-10, LASTOWNERID=-10 WHERE WURMID=?";
    }

    @Override
    public String setDamage() {
        return "INSERT INTO ITEMS (DAMAGE, LASTMAINTAINED, WURMID) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE DAMAGE=VALUES(DAMAGE), LASTMAINTAINED=VALUES(LASTMAINTAINED)";
    }

    @Override
    public String setDamageOld() {
        return "UPDATE ITEMS SET DAMAGE=?, LASTMAINTAINED=? WHERE WURMID=?";
    }

    @Override
    public String getDamage() {
        return "SELECT DAMAGE FROM ITEMS WHERE WURMID=?";
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
        return "UPDATE ITEMS SET TRANSFERRED=? WHERE WURMID=?";
    }

    @Override
    public String getAllItems() {
        return "SELECT * from ITEMS where PARENTID=?";
    }

    @Override
    public String getItem() {
        return "SELECT * from ITEMS where WURMID=?";
    }

    @Override
    public String setBless() {
        return "UPDATE ITEMS SET BLESS=? WHERE WURMID=?";
    }

    @Override
    public String setSizeX() {
        return "UPDATE ITEMS SET SIZEX=? WHERE WURMID=?";
    }

    @Override
    public String getSizeX() {
        return "SELECT SIZEX FROM ITEMS WHERE WURMID=?";
    }

    @Override
    public String setSizeY() {
        return "UPDATE ITEMS SET SIZEY=? WHERE WURMID=?";
    }

    @Override
    public String getSizeY() {
        return "SELECT SIZEY FROM ITEMS WHERE WURMID=?";
    }

    @Override
    public String setSizeZ() {
        return "UPDATE ITEMS SET SIZEZ=? WHERE WURMID=?";
    }

    @Override
    public String getSizeZ() {
        return "SELECT SIZEZ FROM ITEMS WHERE WURMID=?";
    }

    @Override
    public String setLockId() {
        return "UPDATE ITEMS SET LOCKID=? WHERE WURMID=?";
    }

    @Override
    public String setPrice() {
        return "UPDATE ITEMS SET PRICE=? WHERE WURMID=?";
    }

    @Override
    public String setAuxData() {
        return "UPDATE ITEMS SET AUXDATA=? WHERE WURMID=?";
    }

    @Override
    public String setCreationState() {
        return "UPDATE ITEMS SET CREATIONSTATE=? WHERE WURMID=?";
    }

    @Override
    public String setRealTemplate() {
        return "UPDATE ITEMS SET REALTEMPLATE=? WHERE WURMID=?";
    }

    @Override
    public String setColor() {
        return "UPDATE ITEMS SET COLOR=?,COLOR2=? WHERE WURMID=?";
    }

    @Override
    public String setEnchant() {
        return "UPDATE ITEMS SET ENCHANT=? WHERE WURMID=?";
    }

    @Override
    public String setBanked() {
        return "UPDATE ITEMS SET BANKED=? WHERE WURMID=?";
    }

    @Override
    public String getData() {
        return "select * from ITEMDATA where WURMID=?";
    }

    @Override
    public String createData() {
        if (DbConnector.isUseSqlite()) {
            return "insert OR IGNORE into ITEMDATA ( DATA1, DATA2, EXTRA1, EXTRA2, WURMID) values(?,?,?,?,?)";
        }
        return "insert IGNORE into ITEMDATA ( DATA1, DATA2, EXTRA1, EXTRA2, WURMID) values(?,?,?,?,?)";
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
        return "UPDATE ITEMS SET TEMPERATURE=? WHERE WURMID=?";
    }

    @Override
    public String getTemperature() {
        return "SELECT TEMPERATURE FROM ITEMS WHERE WURMID=?";
    }

    @Override
    public String setMaterial() {
        return "UPDATE ITEMS SET MATERIAL=? WHERE WURMID=?";
    }

    @Override
    public String setWornAsArmour() {
        return "UPDATE ITEMS SET WORNARMOUR=? WHERE WURMID=?";
    }

    @Override
    public String setFemale() {
        return "UPDATE ITEMS SET FEMALE=? WHERE WURMID=?";
    }

    @Override
    public String setMailed() {
        return "UPDATE ITEMS SET MAILED=? WHERE WURMID=?";
    }

    @Override
    public String setCreator() {
        return "UPDATE ITEMS SET CREATOR=? WHERE WURMID=?";
    }

    @Override
    public String getZoneItems() {
        return "SELECT * FROM ITEMS WHERE OWNERID=-10";
    }

    @Override
    public String getCreatureItems() {
        return "SELECT * FROM ITEMS WHERE OWNERID=?";
    }

    @Override
    public String getPreloadedItems() {
        return "SELECT * FROM ITEMS WHERE TEMPLATEID=?";
    }

    @Override
    public String getCreatureItemsNonTransferred() {
        return "SELECT WURMID FROM ITEMS WHERE OWNERID=? AND TRANSFERRED=0";
    }

    @Override
    public String updateLastMaintainedBankItem() {
        return "UPDATE ITEMS SET LASTMAINTAINED=? WHERE BANKED=1";
    }

    @Override
    public String getItemWeights() {
        return "SELECT WURMID, WEIGHT,SIZEX,SIZEY,SIZEZ, TEMPLATEID FROM ITEMS";
    }

    @Override
    public String getOwnedItems() {
        return "SELECT OWNERID FROM ITEMS WHERE OWNERID>0 GROUP BY OWNERID";
    }

    @Override
    public String deleteByOwnerId() {
        return "DELETE FROM ITEMS WHERE OWNERID=?";
    }

    @Override
    public String deleteTransferedItem() {
        return "DELETE FROM ITEMS WHERE WURMID=? AND TRANSFERRED=0";
    }

    @Override
    public String deleteItem() {
        return "delete from ITEMS where WURMID=?";
    }

    @Override
    public String getRecycledItems() {
        return "SELECT * FROM ITEMS WHERE TEMPLATEID=? AND BANKED=1";
    }

    @Override
    public String getItemsForZone() {
        return "Select WURMID from ITEMS where ZONEID=? AND BANKED=0";
    }

    @Override
    public String setHidden() {
        return "UPDATE ITEMS SET HIDDEN=? WHERE WURMID=?";
    }

    @Override
    public String setSettings() {
        return "UPDATE ITEMS SET SETTINGS=? WHERE WURMID=?";
    }

    @Override
    public String setMailTimes() {
        return "UPDATE ITEMS SET MAILTIMES=? WHERE WURMID=?";
    }

    @Override
    public String freeze() {
        return "INSERT INTO FROZENITEMS SELECT * FROM ITEMS WHERE WURMID=?";
    }

    @Override
    public String thaw() {
        return "INSERT INTO ITEMS SELECT * FROM FROZENITEMS WHERE WURMID=?";
    }

    public static ItemDbStrings getInstance() {
        if (instance == null) {
            instance = new ItemDbStrings();
        }
        return instance;
    }

    @Override
    public final String getDbStringsType() {
        return "ItemDbStrings";
    }
}

