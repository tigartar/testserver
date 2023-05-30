package com.wurmonline.server.items;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Items;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.questions.MailSendConfirmQuestion;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.shared.constants.CounterTypes;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WurmMail implements TimeConstants, MiscConstants, CounterTypes, Comparable<WurmMail> {
   public static final byte MAIL_TYPE_PREPAID = 0;
   public static final byte MAIL_TYPE_CASHONDELIVERY = 1;
   public final byte type;
   public final long itemId;
   public long sender;
   public long receiver;
   public final long price;
   public final long sent;
   public long expiration;
   public final int sourceserver;
   public boolean rejected = false;
   private static final ConcurrentHashMap<Long, Set<WurmMail>> mails = new ConcurrentHashMap<>();
   private static final ConcurrentHashMap<Long, Long> mailsByItemId = new ConcurrentHashMap<>();
   private static final Logger logger = Logger.getLogger(WurmMail.class.getName());
   private static final String GET_ALL_MAIL = "SELECT * FROM MAIL";
   private static final String DELETE_MAIL = "DELETE FROM MAIL WHERE ITEMID=?";
   private static final String SAVE_MAIL = "INSERT INTO MAIL (ITEMID,TYPE,SENDER,RECEIVER,PRICE,SENT,EXPIRATION,SOURCESERVER,RETURNED ) VALUES(?,?,?,?,?,?,?,?,?)";
   private static final String UPDATE_MAIL = "UPDATE MAIL SET TYPE=?,SENDER=?,RECEIVER=?,PRICE=?,SENT=?,EXPIRATION=?,RETURNED=? WHERE ITEMID=?";
   public static final int maxNumberMailsToDisplay = 100;

   public WurmMail(
      byte _type, long _itemid, long _sender, long _receiver, long _price, long _sent, long _expiration, int _sourceserver, boolean _rejected, boolean loading
   ) {
      this.type = _type;
      this.itemId = _itemid;
      this.sender = _sender;
      this.receiver = _receiver;
      this.price = _price;
      this.sent = _sent;
      this.expiration = _expiration;
      this.rejected = _rejected;
      this.sourceserver = _sourceserver;
      if (loading) {
         addWurmMail(this);
      }
   }

   public static final void addWurmMail(WurmMail mail) {
      if (mail.isExpired() && !mail.isRejected()) {
         if (mail.sourceserver == Servers.localServer.id && !mail.rejected) {
            long oldRec = mail.receiver;
            mail.receiver = mail.sender;
            mail.sender = oldRec;
            mail.expiration = System.currentTimeMillis() + 1209600000L;
            mail.rejected = true;
            mail.update();
            getMailsFor(mail.receiver).add(mail);
            mailsByItemId.put(new Long(mail.itemId), new Long(mail.receiver));
         }
      } else if (mail.isRejected() && mail.isExpired()) {
         deleteMail(mail.getItemId());
      } else {
         getMailsFor(mail.receiver).add(mail);
         mailsByItemId.put(new Long(mail.itemId), new Long(mail.receiver));
      }
   }

   public static final void poll() {
      Set<WurmMail> toDelete = new HashSet<>();

      for(Set<WurmMail> mailset : mails.values()) {
         for(WurmMail m : mailset) {
            if (m.isExpired()) {
               logger.log(Level.INFO, "Checking expired WurmMail " + m.itemId);
               if (m.sourceserver != Servers.localServer.id && !m.isRejected()) {
                  logger.log(Level.INFO, "Trying to return expired WurmMail " + m.itemId);
                  int targetServer = m.sourceserver;

                  try {
                     Item toReturn = Items.getItem(m.getItemId());
                     if (Servers.getServerWithId(targetServer).isAvailable(5, true)) {
                        logger.log(Level.INFO, "Returning to " + targetServer);
                        m.expiration = System.currentTimeMillis() + 1209600000L;
                        m.setRejected(true);
                        long sender = m.receiver;
                        m.receiver = m.sender;
                        m.sender = sender;
                        Set<WurmMail> oneMail = new HashSet<>();
                        oneMail.add(m);
                        Item[] itemarr = new Item[]{toReturn};
                        if (!MailSendConfirmQuestion.sendMailSetToServer(m.receiver, null, targetServer, oneMail, m.sender, itemarr)) {
                           toDelete.add(m);
                        }
                     }
                  } catch (NoSuchItemException var11) {
                     toDelete.add(m);
                     break;
                  }
               } else if (m.isRejected()) {
                  logger.log(Level.INFO, "Deleting expired rejected mail " + m.getItemId() + " sent to " + m.getReceiver() + " from " + m.getSender());
                  toDelete.add(m);
               }
            }
         }
      }

      for(WurmMail deleted : toDelete) {
         Items.destroyItem(deleted.getItemId());
         removeMail(deleted.getItemId());
         logger.log(Level.INFO, "Deleted WurmMail " + deleted.getItemId());
      }

      toDelete.clear();
   }

   public static final boolean isItemInMail(long itemId) {
      return mailsByItemId.get(itemId) != null;
   }

   public boolean isRejected() {
      return this.rejected;
   }

   public void setRejected(boolean aRejected) {
      this.rejected = aRejected;
   }

   public byte getType() {
      return this.type;
   }

   public long getItemId() {
      return this.itemId;
   }

   public long getSender() {
      return this.sender;
   }

   public long getReceiver() {
      return this.receiver;
   }

   public long getPrice() {
      return this.price;
   }

   public long getSent() {
      return this.sent;
   }

   public long getExpiration() {
      return this.expiration;
   }

   public int getSourceserver() {
      return this.sourceserver;
   }

   public String getName() {
      try {
         Item item = Items.getItem(this.itemId);
         return item.getName();
      } catch (NoSuchItemException var2) {
         return "UnKnown";
      }
   }

   public boolean isExpired() {
      return this.expiration < System.currentTimeMillis();
   }

   public static final Set<WurmMail> getWaitingMailFor(long receiverid) {
      Set<WurmMail> set = mails.get(new Long(receiverid));
      Set<WurmMail> toReturn = new HashSet<>();
      if (set != null) {
         for(WurmMail mail : set) {
            if (mail.sent < System.currentTimeMillis()) {
               toReturn.add(mail);
            }
         }
      }

      return toReturn;
   }

   public static final Set<WurmMail> getMailsFor(long receiverid) {
      Set<WurmMail> set = mails.get(new Long(receiverid));
      if (set == null) {
         set = new HashSet<>();
         mails.put(new Long(receiverid), set);
      }

      return set;
   }

   public static final Set<WurmMail> getSentMailsFor(long receiverid, int maxNumbers) {
      Set<WurmMail> set = getMailsFor(receiverid);
      Set<WurmMail> toReturn = new HashSet<>();
      int nums = 0;

      for(WurmMail toAdd : set) {
         if (toAdd.sent < System.currentTimeMillis()) {
            toReturn.add(toAdd);
            ++nums;
         }

         if (nums >= maxNumbers) {
            break;
         }
      }

      return toReturn;
   }

   public static final Set<WurmMail> getMailsSendBy(long senderid) {
      Set<WurmMail> sent = new HashSet<>();

      for(Set<WurmMail> mailset : mails.values()) {
         for(WurmMail m : mailset) {
            if (m.sender == senderid) {
               sent.add(m);
            }
         }
      }

      return sent;
   }

   public static final long getReceiverForItem(long itemId) {
      Long receiver = mailsByItemId.get(new Long(itemId));
      return receiver == null ? -10L : receiver;
   }

   public static final WurmMail getWurmMailForItem(long itemId) {
      Long receiver = mailsByItemId.get(new Long(itemId));
      if (receiver == null) {
         return null;
      } else {
         Set<WurmMail> set = getMailsFor(receiver);
         if (set != null) {
            for(WurmMail m : set) {
               if (m.itemId == itemId) {
                  return m;
               }
            }
         }

         return null;
      }
   }

   public static final WurmMail[] getAllMail() {
      Set<WurmMail> sent = new HashSet<>();

      for(Set<WurmMail> mailset : mails.values()) {
         for(WurmMail m : mailset) {
            sent.add(m);
         }
      }

      return sent.toArray(new WurmMail[sent.size()]);
   }

   public static final void removeMail(long itemId) {
      Long receiver = mailsByItemId.get(new Long(itemId));
      if (receiver != null) {
         Set<WurmMail> set = getMailsFor(receiver);
         if (set != null) {
            WurmMail toRemove = null;

            for(WurmMail m : set) {
               if (m.itemId == itemId) {
                  toRemove = m;
                  break;
               }
            }

            if (toRemove != null) {
               set.remove(toRemove);
            }
         }

         mailsByItemId.remove(new Long(itemId));
         deleteMail(itemId);
      }
   }

   public static final void loadAllMails() throws IOException {
      long start = System.nanoTime();
      int loadedMails = 0;
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("SELECT * FROM MAIL");

         for(rs = ps.executeQuery(); rs.next(); ++loadedMails) {
            new WurmMail(
               rs.getByte("TYPE"),
               rs.getLong("ITEMID"),
               rs.getLong("SENDER"),
               rs.getLong("RECEIVER"),
               rs.getLong("PRICE"),
               rs.getLong("SENT"),
               rs.getLong("EXPIRATION"),
               rs.getInt("SOURCESERVER"),
               rs.getBoolean("RETURNED"),
               true
            );
         }
      } catch (SQLException var13) {
         logger.log(Level.WARNING, "Problem loading Mails from database due to " + var13.getMessage(), (Throwable)var13);
         throw new IOException(var13);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, rs);
         DbConnector.returnConnection(dbcon);
         long end = System.nanoTime();
         logger.info("Loaded " + loadedMails + " Mails from the database took " + (float)(end - start) / 1000000.0F + " ms");
      }
   }

   public static final void deleteMail(long itemid) {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("DELETE FROM MAIL WHERE ITEMID=?");
         ps.setLong(1, itemid);
         ps.executeUpdate();
      } catch (SQLException var8) {
         logger.log(Level.WARNING, itemid + " : " + var8.getMessage(), (Throwable)var8);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public final void createInDatabase() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("INSERT INTO MAIL (ITEMID,TYPE,SENDER,RECEIVER,PRICE,SENT,EXPIRATION,SOURCESERVER,RETURNED ) VALUES(?,?,?,?,?,?,?,?,?)");
         ps.setLong(1, this.itemId);
         ps.setByte(2, this.type);
         ps.setLong(3, this.sender);
         ps.setLong(4, this.receiver);
         ps.setLong(5, this.price);
         ps.setLong(6, this.sent);
         ps.setLong(7, this.expiration);
         ps.setInt(8, this.sourceserver);
         ps.setBoolean(9, this.rejected);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, this.itemId + " : " + var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   private final void update() {
      Connection dbcon = null;
      PreparedStatement ps = null;

      try {
         dbcon = DbConnector.getItemDbCon();
         ps = dbcon.prepareStatement("UPDATE MAIL SET TYPE=?,SENDER=?,RECEIVER=?,PRICE=?,SENT=?,EXPIRATION=?,RETURNED=? WHERE ITEMID=?");
         ps.setByte(1, this.type);
         ps.setLong(2, this.sender);
         ps.setLong(3, this.receiver);
         ps.setLong(4, this.price);
         ps.setLong(5, this.sent);
         ps.setLong(6, this.expiration);
         ps.setBoolean(7, this.rejected);
         ps.setLong(8, this.itemId);
         ps.executeUpdate();
      } catch (SQLException var7) {
         logger.log(Level.WARNING, this.itemId + " : " + var7.getMessage(), (Throwable)var7);
      } finally {
         DbUtilities.closeDatabaseObjects(ps, null);
         DbConnector.returnConnection(dbcon);
      }
   }

   public int compareTo(WurmMail otherWurmMail) {
      return this.getName().compareTo(otherWurmMail.getName());
   }
}
