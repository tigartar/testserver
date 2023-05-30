package com.wurmonline.server;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;
import winterwell.jtwitter.OAuthSignpostClient;
import winterwell.jtwitter.Twitter;

public final class Twit {
   private static final Logger logger = Logger.getLogger(Twit.class.getName());
   private final String sender;
   private final String twit;
   private final String consumerKey;
   private final String consumerSecret;
   private final String oauthToken;
   private final String oauthTokenSecret;
   private final boolean isVillage;
   private static final Twit[] emptyTwits = new Twit[0];
   @GuardedBy("TWITS_RW_LOCK")
   private static final List<Twit> twits = new LinkedList<>();
   private static final ReentrantReadWriteLock TWITS_RW_LOCK = new ReentrantReadWriteLock();
   private static final Twit.TwitterThread twitterThread = new Twit.TwitterThread();

   public Twit(
      String senderName,
      String toTwit,
      String consumerKeyToUse,
      String consumerSecretToUse,
      String applicationToken,
      String applicationSecret,
      boolean _isVillage
   ) {
      this.sender = senderName;
      this.twit = toTwit.substring(0, Math.min(toTwit.length(), 279));
      this.consumerKey = consumerKeyToUse;
      this.consumerSecret = consumerSecretToUse;
      this.oauthToken = applicationToken;
      this.oauthTokenSecret = applicationSecret;
      this.isVillage = _isVillage;
   }

   private static final Twit[] getTwitsArray() {
      try {
         TWITS_RW_LOCK.writeLock().lock();
         if (twits.size() > 0) {
            Twit[] toReturn = new Twit[twits.size()];
            int x = 0;

            for(ListIterator<Twit> it = twits.listIterator(); it.hasNext(); ++x) {
               toReturn[x] = it.next();
            }

            return toReturn;
         }
      } finally {
         TWITS_RW_LOCK.writeLock().unlock();
      }

      return emptyTwits;
   }

   private static final void removeTwit(Twit twit) {
      try {
         TWITS_RW_LOCK.writeLock().lock();
         twits.remove(twit);
      } finally {
         TWITS_RW_LOCK.writeLock().unlock();
      }
   }

   private static void pollTwits() {
      Twit[] twitarr = getTwitsArray();
      if (twitarr.length > 0) {
         for(int y = 0; y < twitarr.length; ++y) {
            try {
               twitJTwitter(twitarr[y]);
               removeTwit(twitarr[y]);
            } catch (Exception var3) {
               if (var3.getMessage().startsWith("Already tweeted!")
                  || var3.getMessage().startsWith("Forbidden")
                  || var3.getMessage().startsWith("Unauthorized")
                  || var3.getMessage().startsWith("Invalid")) {
                  logger.log(Level.INFO, "Removed duplicate or unauthorized " + twitarr[y].twit);
                  removeTwit(twitarr[y]);
               } else if (twitarr[y].isVillage) {
                  logger.log(Level.INFO, "Twitting failed for village " + var3.getMessage() + " Removing.");
                  removeTwit(twitarr[y]);
               } else {
                  if (twitarr[y].twit == null || twitarr[y].twit.length() == 0) {
                     removeTwit(twitarr[y]);
                  }

                  logger.log(Level.INFO, "Twitting failed for server " + var3.getMessage() + ". Trying later.");
               }
            }
         }
      }
   }

   public static final void twit(Twit twit) {
      if (twit != null) {
         try {
            TWITS_RW_LOCK.writeLock().lock();
            twits.add(twit);
         } finally {
            TWITS_RW_LOCK.writeLock().unlock();
         }
      }
   }

   private static void twitJTwitter(Twit twit) {
      logger.log(Level.INFO, "creating oauthClient for " + twit.twit);
      OAuthSignpostClient oauthClient = new OAuthSignpostClient(twit.consumerKey, twit.consumerSecret, twit.oauthToken, twit.oauthTokenSecret);
      logger.log(Level.INFO, "creating twitter for " + twit.twit);
      Twitter twitter = new Twitter(twit.sender, oauthClient);
      twitter.setStatus(twit.twit);
      logger.log(Level.INFO, "done sending twit " + twit.twit);
   }

   public static final Twit.TwitterThread getTwitterThread() {
      return twitterThread;
   }

   private static class TwitterThread implements Runnable {
      TwitterThread() {
      }

      @Override
      public void run() {
         try {
            long start = System.nanoTime();
            Twit.pollTwits();
            float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
            if (lElapsedTime > (float)Constants.lagThreshold) {
               Twit.logger.info("Finished calling Twit.pollTwits(), which took " + lElapsedTime + " millis.");
            }
         } catch (RuntimeException var4) {
            Twit.logger.log(Level.WARNING, "Caught exception in ScheduledExecutorService while calling Twit.pollTwits()", (Throwable)var4);
            throw var4;
         }
      }
   }
}
