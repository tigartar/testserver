/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server;

import com.wurmonline.server.Constants;
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
    @GuardedBy(value="TWITS_RW_LOCK")
    private static final List<Twit> twits = new LinkedList<Twit>();
    private static final ReentrantReadWriteLock TWITS_RW_LOCK = new ReentrantReadWriteLock();
    private static final TwitterThread twitterThread = new TwitterThread();

    public Twit(String senderName, String toTwit, String consumerKeyToUse, String consumerSecretToUse, String applicationToken, String applicationSecret, boolean _isVillage) {
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
                ListIterator<Twit> it = twits.listIterator();
                while (it.hasNext()) {
                    toReturn[x] = it.next();
                    ++x;
                }
                Twit[] twitArray = toReturn;
                return twitArray;
            }
        }
        finally {
            TWITS_RW_LOCK.writeLock().unlock();
        }
        return emptyTwits;
    }

    private static final void removeTwit(Twit twit) {
        try {
            TWITS_RW_LOCK.writeLock().lock();
            twits.remove(twit);
        }
        finally {
            TWITS_RW_LOCK.writeLock().unlock();
        }
    }

    private static void pollTwits() {
        Twit[] twitarr = Twit.getTwitsArray();
        if (twitarr.length > 0) {
            for (int y = 0; y < twitarr.length; ++y) {
                try {
                    Twit.twitJTwitter(twitarr[y]);
                    Twit.removeTwit(twitarr[y]);
                    continue;
                }
                catch (Exception ex) {
                    if (ex.getMessage().startsWith("Already tweeted!") || ex.getMessage().startsWith("Forbidden") || ex.getMessage().startsWith("Unauthorized") || ex.getMessage().startsWith("Invalid")) {
                        logger.log(Level.INFO, "Removed duplicate or unauthorized " + twitarr[y].twit);
                        Twit.removeTwit(twitarr[y]);
                        continue;
                    }
                    if (twitarr[y].isVillage) {
                        logger.log(Level.INFO, "Twitting failed for village " + ex.getMessage() + " Removing.");
                        Twit.removeTwit(twitarr[y]);
                        continue;
                    }
                    if (twitarr[y].twit == null || twitarr[y].twit.length() == 0) {
                        Twit.removeTwit(twitarr[y]);
                    }
                    logger.log(Level.INFO, "Twitting failed for server " + ex.getMessage() + ". Trying later.");
                }
            }
        }
    }

    public static final void twit(Twit twit) {
        if (twit != null) {
            try {
                TWITS_RW_LOCK.writeLock().lock();
                twits.add(twit);
            }
            finally {
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

    public static final TwitterThread getTwitterThread() {
        return twitterThread;
    }

    static /* synthetic */ void access$000() {
        Twit.pollTwits();
    }

    static /* synthetic */ Logger access$100() {
        return logger;
    }

    private static class TwitterThread
    implements Runnable {
        TwitterThread() {
        }

        @Override
        public void run() {
            try {
                long start = System.nanoTime();
                Twit.pollTwits();
                float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0f;
                if (lElapsedTime > (float)Constants.lagThreshold) {
                    logger.info("Finished calling Twit.pollTwits(), which took " + lElapsedTime + " millis.");
                }
            }
            catch (RuntimeException e) {
                logger.log(Level.WARNING, "Caught exception in ScheduledExecutorService while calling Twit.pollTwits()", e);
                throw e;
            }
        }
    }
}

