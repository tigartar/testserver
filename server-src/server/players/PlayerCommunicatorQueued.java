package com.wurmonline.server.players;

import com.wurmonline.communication.SocketConnection;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.server.Constants;
import com.wurmonline.server.Features;
import com.wurmonline.server.Items;
import com.wurmonline.server.Message;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.bodys.Wound;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.PlayerMove;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.Trade;
import com.wurmonline.server.items.WurmColor;
import com.wurmonline.server.sounds.Sound;
import com.wurmonline.server.structures.Door;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.NoSuchWallException;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Water;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.StructureTypeEnum;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PlayerCommunicatorQueued extends PlayerCommunicator {
   private static final Logger logger = Logger.getLogger(PlayerCommunicatorQueued.class.getName());
   private static final BlockingQueue<PlayerMessage> MESSAGES_TO_PLAYERS = new LinkedBlockingQueue<>();
   private static final String EMPTY_STRING = "";
   private long timeMod = 0L;
   private final DateFormat df = DateFormat.getTimeInstance();
   private long newSeed = (long)(Server.rand.nextInt() & 2147483647);
   private int newSeedPointer = 0;
   private PlayerMove currentmove;
   boolean receivedTicks = false;
   private static final float woundMultiplier = 0.0015259022F;
   private static final int emptyRock = Tiles.encode((short)-100, Tiles.Tile.TILE_CAVE_WALL.id, (byte)0);

   public PlayerCommunicatorQueued(Player aPlayer, SocketConnection aConn) {
      super(aPlayer, aConn);
      logger.info("Created");
   }

   private ByteBuffer getBuffer(int aBufferCapacity) {
      ByteBuffer lBuffer = ByteBuffer.allocate(aBufferCapacity);
      ((Buffer)lBuffer).clear();
      return lBuffer;
   }

   @Override
   public boolean pollNextMove() {
      return super.pollNextMove();
   }

   @Override
   public void sendMessage(Message message) {
      if (this.player.hasLink()) {
         try {
            byte[] byteArray = message.getMessage().getBytes("UTF-8");
            byte[] window = message.getWindow().getBytes();
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)99);
            bb.put((byte)window.length);
            bb.put(window);
            bb.put((byte)message.getRed());
            bb.put((byte)message.getGreen());
            bb.put((byte)message.getBlue());
            bb.putShort((short)byteArray.length);
            bb.put(byteArray);
            bb.put((byte)0);
            this.addMessageToQueue(bb);
         } catch (Exception var5) {
            logger.log(Level.INFO, this.player.getName() + " could not send a message '" + message + "' due to : " + var5.getMessage(), (Throwable)var5);
            this.player.setLink(false);
         }
      }
   }

   private void addMessageToQueue(ByteBuffer aByteBuffer) {
      byte[] lArray = new byte[aByteBuffer.position()];
      System.arraycopy(aByteBuffer.array(), 0, lArray, 0, aByteBuffer.position());
      PlayerMessage lPlayerMessage = new PlayerMessage(new Long(this.player.getWurmId()), lArray);

      try {
         if (Players.getInstance().getPlayer(this.player.getWurmId()) != null) {
            MESSAGES_TO_PLAYERS.add(lPlayerMessage);
            if (logger.isLoggable(Level.FINEST)) {
               logger.finest(
                  "Added "
                     + lPlayerMessage
                     + " message with "
                     + aByteBuffer.position()
                     + " bytes to MESSAGES_TO_PLAYERS, queue size: "
                     + MESSAGES_TO_PLAYERS.size()
               );
            }
         }
      } catch (NoSuchPlayerException var5) {
         logger.log(
            Level.WARNING,
            "Player is not in Players map so could not add "
               + lPlayerMessage
               + " message with "
               + aByteBuffer.position()
               + " bytes to MESSAGES_TO_PLAYERS - "
               + var5.getMessage(),
            (Throwable)var5
         );
      }
   }

   @Override
   public void sendGmMessage(long time, String sender, String message) {
      if (this.player.hasLink()) {
         try {
            String fd = this.df.format(new Date(time));
            byte[] byteArray = (fd + " <" + sender + "> " + message).getBytes("UTF-8");
            byte[] window = "GM".getBytes();
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)99);
            bb.put((byte)window.length);
            bb.put(window);
            bb.put((byte)-56);
            bb.put((byte)-56);
            bb.put((byte)-56);
            bb.putShort((short)byteArray.length);
            bb.put(byteArray);
            bb.put((byte)0);
            this.addMessageToQueue(bb);
         } catch (Exception var9) {
            logger.log(Level.INFO, this.player.getName() + " could not send a GM message '" + message + "' due to : " + var9.getMessage(), (Throwable)var9);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendSafeServerMessage(String message) {
      if (this.player.hasLink()) {
         try {
            byte[] byteArray = message.getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)99);
            bb.put((byte)event.length);
            bb.put(event);
            bb.put((byte)102);
            bb.put((byte)-72);
            bb.put((byte)120);
            bb.putShort((short)byteArray.length);
            bb.put(byteArray);
            bb.put((byte)0);
            this.addMessageToQueue(bb);
         } catch (Exception var4) {
            logger.log(Level.INFO, this.player.getName() + " could not send a message '" + message + "' due to : " + var4.getMessage(), (Throwable)var4);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendNormalServerMessage(String message) {
      if (this.player.hasLink()) {
         try {
            byte[] byteArray = message.getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)99);
            bb.put((byte)event.length);
            bb.put(event);
            bb.put((byte)-1);
            bb.put((byte)-1);
            bb.put((byte)-1);
            bb.putShort((short)byteArray.length);
            bb.put(byteArray);
            bb.put((byte)0);
            this.addMessageToQueue(bb);
         } catch (Exception var4) {
            logger.log(Level.INFO, this.player.getName() + " could not send a message '" + message + "' due to : " + var4.getMessage(), (Throwable)var4);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendCombatNormalMessage(String message) {
      this.sendCombatServerMessage(message, (byte)-1, (byte)-1, (byte)-1);
   }

   @Override
   public void sendCombatAlertMessage(String message) {
      this.sendCombatServerMessage(message, (byte)-1, (byte)-106, (byte)10);
   }

   @Override
   public void sendCombatSafeMessage(String message) {
      this.sendCombatServerMessage(message, (byte)102, (byte)-72, (byte)120);
   }

   @Override
   public void sendCombatServerMessage(String message, byte r, byte g, byte b) {
      if (this.player.hasLink()) {
         try {
            byte[] byteArray = message.getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)99);
            bb.put((byte)combat.length);
            bb.put(combat);
            bb.put(r);
            bb.put(g);
            bb.put(b);
            bb.putShort((short)byteArray.length);
            bb.put(byteArray);
            bb.put((byte)0);
            this.addMessageToQueue(bb);
         } catch (Exception var7) {
            logger.log(Level.INFO, this.player.getName() + ":" + var7.getMessage(), (Throwable)var7);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendAlertServerMessage(String message) {
      if (this.player.hasLink()) {
         try {
            byte[] byteArray = message.getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)99);
            bb.put((byte)event.length);
            bb.put(event);
            bb.put((byte)-1);
            bb.put((byte)-106);
            bb.put((byte)10);
            bb.putShort((short)byteArray.length);
            bb.put(byteArray);
            bb.put((byte)0);
            this.addMessageToQueue(bb);
         } catch (Exception var4) {
            logger.log(Level.INFO, this.player.getName() + ": " + var4.getMessage(), (Throwable)var4);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendAddToInventory(Item item, long inventoryWindow, long rootid, int price) {
      if (this.player.hasLink()) {
         try {
            int weight = item.getFullWeight();
            if (item.isLockable() && item.getLockId() != -10L) {
               try {
                  Item lock = Items.getItem(item.getLockId());
                  if (!this.player.hasKeyForLock(lock)) {
                     weight = item.getFullWeight();
                  }
               } catch (NoSuchItemException var12) {
                  logger.log(
                     Level.WARNING, item.getWurmId() + " has lock " + item.getLockId() + " but that doesn't exist." + var12.getMessage(), (Throwable)var12
                  );
               }
            }

            byte[] byteArray = item.getName().getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)76);
            bb.putLong(inventoryWindow);
            long parentId = 0L;
            if (item.isBanked()) {
               parentId = inventoryWindow;
            } else if (rootid != 0L && item.getParentId() > 0L) {
               parentId = item.getParentId();
            }

            bb.putLong(parentId);
            bb.putLong(item.getWurmId());
            bb.putShort(item.getImageNumber());
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            byteArray = item.getDescription().getBytes("UTF-8");
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            bb.putFloat(item.getQualityLevel());
            bb.putFloat(item.getDamage());
            bb.putInt(weight);
            bb.put((byte)(item.color == -1 ? 0 : 1));
            if (item.color != -1) {
               bb.put((byte)WurmColor.getColorRed(item.color));
               bb.put((byte)WurmColor.getColorGreen(item.color));
               bb.put((byte)WurmColor.getColorBlue(item.color));
            }

            bb.put((byte)(price >= 0 ? 1 : 0));
            if (price >= 0) {
               bb.putInt(price);
            }

            this.addMessageToQueue(bb);
            if (!item.isEmpty(false) && item.isViewableBy(this.player)) {
               this.sendHasMoreItems(inventoryWindow, item.getWurmId());
            }
         } catch (Exception var13) {
            logger.log(Level.INFO, this.player.getName() + ":" + item.getName() + ": " + item.getDescription(), (Throwable)var13);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendUpdateInventoryItem(Item item, long inventoryWindow, int price) {
      if (this.player.hasLink()) {
         try {
            byte[] byteArray = item.getName().getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)68);
            bb.putLong(inventoryWindow);
            bb.putLong(item.getWurmId());
            long parentId = -1L;
            if (item.getParentId() > 0L) {
               parentId = item.getParentId();
            }

            bb.putLong(parentId);
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            byteArray = item.getDescription().getBytes("UTF-8");
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            bb.putFloat(item.getQualityLevel());
            bb.putFloat(item.getDamage());
            bb.putInt(item.getFullWeight());
            bb.put((byte)(item.color == -1 ? 0 : 1));
            if (item.color != -1) {
               bb.put((byte)WurmColor.getColorRed(item.color));
               bb.put((byte)WurmColor.getColorGreen(item.color));
               bb.put((byte)WurmColor.getColorBlue(item.color));
            }

            bb.put((byte)(price >= 0 ? 1 : 0));
            if (price >= 0) {
               bb.putInt(price);
            }

            this.addMessageToQueue(bb);
         } catch (Exception var9) {
            logger.log(Level.INFO, this.player.getName() + ":" + var9.getMessage(), (Throwable)var9);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendUpdateInventoryItem(Item item) {
      long inventoryWindow = item.getTopParent();
      if (this.player == null) {
         logger.log(Level.WARNING, "Player is null ", (Throwable)(new Exception()));
      }

      if (item.getOwnerId() == this.player.getWurmId()) {
         inventoryWindow = -1L;
      }

      this.sendUpdateInventoryItem(item, inventoryWindow, -1);
      if (item.isTraded()) {
         item.getTradeWindow().updateItem(item);
      }
   }

   @Override
   public void sendRemoveFromInventory(Item item, long inventoryWindow) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(17);
            bb.put((byte)-10);
            bb.putLong(inventoryWindow);
            bb.putLong(item.getWurmId());
            this.addMessageToQueue(bb);
         } catch (Exception var5) {
            logger.log(Level.INFO, this.player.getName() + ":" + var5.getMessage(), (Throwable)var5);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendRemoveFromInventory(Item item) {
      if (this.player != null) {
         long inventoryWindow = item.getTopParent();
         if (item.getOwnerId() == this.player.getWurmId()) {
            inventoryWindow = -1L;
         }

         this.sendRemoveFromInventory(item, inventoryWindow);
      }
   }

   @Override
   public void sendOpenInventoryWindow(long inventoryWindow, String title) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(32767);
            byte[] byteArray = title.getBytes("UTF-8");
            bb.put((byte)116);
            bb.putLong(inventoryWindow);
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            this.addMessageToQueue(bb);
         } catch (Exception var6) {
            logger.log(Level.INFO, this.player.getName() + ":" + var6.getMessage(), (Throwable)var6);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public boolean sendCloseInventoryWindow(long inventoryWindow) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(9);
            bb.put((byte)120);
            bb.putLong(inventoryWindow);
            this.addMessageToQueue(bb);
         } catch (Exception var5) {
            logger.log(Level.INFO, this.player.getName() + ":" + var5.getMessage(), (Throwable)var5);
            this.player.setLink(false);
         }
      }

      try {
         return this.player.removeItemWatched(Items.getItem(inventoryWindow));
      } catch (NoSuchItemException var4) {
         return true;
      }
   }

   @Override
   public void sendNewCreature(
      long id,
      String name,
      String model,
      float x,
      float y,
      float z,
      long onBridge,
      float rot,
      byte layer,
      boolean onGround,
      boolean floating,
      boolean isSolid,
      byte kingdomId,
      long face,
      byte blood,
      boolean isUndead,
      boolean isCopy,
      byte modtype
   ) {
      if (this.player.hasLink()) {
         try {
            byte[] byteArray = model.getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)108);
            bb.putLong(id);
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            bb.put((byte)(isSolid ? 1 : 0));
            bb.putFloat(y);
            bb.putFloat(x);
            bb.putFloat(rot);
            if (onGround) {
               if (Structure.isGroundFloorAtPosition(x, y, layer == 0)) {
                  bb.putFloat(z + 0.1F);
               } else {
                  bb.putFloat(-3000.0F);
               }
            } else {
               bb.putFloat(z);
            }

            byteArray = name.getBytes("UTF-8");
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            bb.putLong(onBridge);
            if (floating) {
               bb.put((byte)1);
            } else {
               bb.put((byte)0);
            }

            bb.put(layer);
            if ((WurmId.getType(id) == 0 || isCopy) && !isUndead) {
               bb.put((byte)1);
            } else {
               bb.put((byte)0);
            }

            bb.put((byte)0);
            bb.put(kingdomId);
            bb.putLong(face);
            if ((WurmId.getType(id) == 0 || isCopy) && !isUndead) {
               bb.putInt(Math.abs(generateSoundSourceId(id)));
            }

            bb.put(blood);
            bb.put(modtype);
            bb.put((byte)0);
            this.addMessageToQueue(bb);
         } catch (NullPointerException var24) {
            logger.log(Level.WARNING, this.player.getName() + ":" + var24.getMessage(), (Throwable)var24);
            this.player.setLink(false);
         } catch (Exception var25) {
            logger.log(Level.WARNING, this.player.getName() + ": " + name + " " + id + " " + var25.getMessage(), (Throwable)var25);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendMoveCreature(long id, float x, float y, int rot, boolean keepMoving) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(12);
            bb.put((byte)36);
            bb.putLong(id);
            bb.putFloat(y);
            bb.putFloat(x);
            bb.put((byte)rot);
            this.addMessageToQueue(bb);
         } catch (NullPointerException var8) {
            logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
            this.player.setLink(false);
         } catch (Exception var9) {
            logger.log(Level.INFO, this.player.getName() + ":" + var9.getMessage(), (Throwable)var9);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendMoveCreatureAndSetZ(long id, float x, float y, float z, int rot) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(14);
            bb.put((byte)72);
            bb.putLong(id);
            bb.putFloat(z);
            bb.putFloat(x);
            bb.put((byte)rot);
            bb.putFloat(y);
            this.addMessageToQueue(bb);
         } catch (NullPointerException var8) {
            logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
            this.player.setLink(false);
         } catch (Exception var9) {
            logger.log(Level.INFO, this.player.getName() + ":" + var9.getMessage(), (Throwable)var9);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendCreatureChangedLayer(long wurmid, byte newlayer) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(10);
            bb.put((byte)30);
            bb.putLong(wurmid);
            bb.put(newlayer);
            this.addMessageToQueue(bb);
         } catch (Exception var5) {
            logger.log(Level.INFO, this.player.getName() + ":" + var5.getMessage(), (Throwable)var5);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendDeleteCreature(long id) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(9);
            bb.put((byte)14);
            bb.putLong(id);
            this.addMessageToQueue(bb);
         } catch (NullPointerException var4) {
            logger.log(Level.WARNING, this.player.getName() + ":" + var4.getMessage(), (Throwable)var4);
            this.player.setLink(false);
         } catch (Exception var5) {
            logger.log(Level.INFO, this.player.getName() + ":" + var5.getMessage(), (Throwable)var5);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendTileStripFar(short xStart, short yStart, int width, int height) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)103);
            bb.putShort(xStart);
            bb.putShort(yStart);
            bb.putShort((short)width);
            bb.putShort((short)height);

            for(int x = 0; x < width; ++x) {
               for(int y = 0; y < height; ++y) {
                  int xx = (xStart + x) * 16;
                  int yy = (yStart + y) * 16;
                  if (xx < 0 || xx >= 1 << Constants.meshSize || yy < 0 || yy >= 1 << Constants.meshSize) {
                     xx = 0;
                     yy = 0;
                  }

                  bb.putShort(Tiles.decodeHeight(Server.surfaceMesh.data[xx | yy << Constants.meshSize]));
               }
            }

            for(int x = 0; x < width; ++x) {
               int ms = Constants.meshSize - 4;

               for(int y = 0; y < height; ++y) {
                  int xx = xStart + x;
                  int yy = yStart + y;
                  if (xx < 0 || xx >= 1 << ms || yy < 0 || yy >= 1 << ms) {
                     xx = 0;
                     yy = 0;
                  }

                  bb.put(Server.surfaceMesh.getDistantTerrainTypes()[xx | yy << ms]);
               }
            }

            this.addMessageToQueue(bb);
         } catch (Exception var11) {
            logger.log(Level.INFO, this.player.getName() + ":" + var11.getMessage(), (Throwable)var11);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendTileStrip(short xStart, short yStart, int width, int height) throws IOException {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)73);
            bb.put((byte)(Features.Feature.SURFACEWATER.isEnabled() ? 1 : 0));
            bb.put((byte)(this.player.isSendExtraBytes() ? 1 : 0));
            bb.putShort(yStart);
            bb.putShort((short)width);
            bb.putShort((short)height);
            bb.putShort(xStart);

            for(int x = 0; x < width; ++x) {
               for(int y = 0; y < height; ++y) {
                  int tempTileX = xStart + x;
                  int tempTileY = yStart + y;
                  if (tempTileX < 0 || tempTileX >= 1 << Constants.meshSize || tempTileY < 0 || tempTileY >= 1 << Constants.meshSize) {
                     tempTileX = 0;
                     tempTileY = 0;
                  }

                  bb.putInt(Server.surfaceMesh.data[tempTileX | tempTileY << Constants.meshSize]);
                  if (Features.Feature.SURFACEWATER.isEnabled()) {
                     bb.putShort((short)Water.getSurfaceWater(tempTileX, tempTileY));
                  }

                  if (this.player.isSendExtraBytes()) {
                     bb.put(Server.getClientSurfaceFlags(tempTileX, tempTileY));
                  }
               }
            }

            this.addMessageToQueue(bb);
         } catch (Exception var10) {
            logger.log(Level.INFO, this.player.getName() + ":" + var10.getMessage(), (Throwable)var10);
            this.player.setLink(false);
            throw new IOException(this.player.getName() + ":" + var10.getMessage());
         }
      }
   }

   @Override
   public void sendCaveStrip(short xStart, short yStart, int width, int height) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)102);
            bb.put((byte)(Features.Feature.CAVEWATER.isEnabled() ? 1 : 0));
            bb.put((byte)(this.player.isSendExtraBytes() ? 1 : 0));
            bb.putShort(xStart);
            bb.putShort(yStart);
            bb.putShort((short)width);
            bb.putShort((short)height);
            boolean onSurface = this.player.isOnSurface();

            for(int x = 0; x < width; ++x) {
               for(int y = 0; y < height; ++y) {
                  int xx = xStart + x;
                  int yy = yStart + y;
                  if (xx < 0 || xx >= Zones.worldTileSizeX || yy < 0 || yy >= Zones.worldTileSizeY) {
                     bb.putInt(emptyRock);
                     xx = 0;
                     yy = 0;
                  } else if (!onSurface) {
                     bb.putInt(Server.caveMesh.data[xx | yy << Constants.meshSize]);
                  } else if (Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.data[xx | yy << Constants.meshSize]))) {
                     bb.putInt(this.getDummyWall(xx, yy));
                  } else {
                     bb.putInt(Server.caveMesh.data[xx | yy << Constants.meshSize]);
                  }

                  if (Features.Feature.CAVEWATER.isEnabled()) {
                     bb.putShort((short)Water.getCaveWater(xx, yy));
                  }

                  if (this.player.isSendExtraBytes()) {
                     bb.put(Server.getClientCaveFlags(xx, yy));
                  }
               }
            }

            this.addMessageToQueue(bb);
         } catch (Exception var11) {
            logger.log(Level.INFO, this.player.getName() + ":" + var11.getMessage(), (Throwable)var11);
            this.player.setLink(false);
         }
      }
   }

   private int getDummyWall(int tilex, int tiley) {
      return Tiles.encode(
         Tiles.decodeHeight(Server.caveMesh.data[tilex | tiley << Constants.meshSize]),
         Tiles.Tile.TILE_CAVE_WALL.id,
         Tiles.decodeData(Server.caveMesh.data[tilex | tiley << Constants.meshSize])
      );
   }

   private boolean isCaveWallHidden(int tilex, int tiley) {
      if (!this.isCaveWallSolid(tilex, tiley)) {
         return false;
      } else if (!this.isCaveWallSolid(tilex, tiley - 1)) {
         return false;
      } else if (!this.isCaveWallSolid(tilex + 1, tiley)) {
         return false;
      } else if (!this.isCaveWallSolid(tilex, tiley + 1)) {
         return false;
      } else {
         return this.isCaveWallSolid(tilex - 1, tiley);
      }
   }

   private boolean isCaveWallSolid(int tilex, int tiley) {
      if (tilex < 0 || tilex >= Zones.worldTileSizeX || tiley < 0 || tiley >= Zones.worldTileSizeY) {
         return true;
      } else {
         return Tiles.isSolidCave(Tiles.decodeType(Server.caveMesh.data[tilex | tiley << Constants.meshSize]));
      }
   }

   @Override
   public void sendAvailableActions(byte requestId, List<ActionEntry> availableActions, String helpstring) {
      if (this.player.hasLink()) {
         try {
            if (logger.isLoggable(Level.FINEST)) {
               logger.finest(
                  this.player.getName()
                     + ", sending # of available Actions: "
                     + availableActions.size()
                     + ", requestId: "
                     + requestId
                     + ", availableActions: "
                     + availableActions
                     + ", helpstring: "
                     + helpstring
               );
            } else if (logger.isLoggable(Level.FINER)) {
               logger.finer(
                  this.player.getName()
                     + ", sending # of available Actions: "
                     + availableActions.size()
                     + " , requestId: "
                     + requestId
                     + ", helpstring: "
                     + helpstring
               );
            }

            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)20);
            bb.put(requestId);
            bb.put((byte)availableActions.size());

            for(ActionEntry entry : availableActions) {
               bb.putShort(entry.getNumber());
               String actionString = entry.getActionString();
               byte[] byteArray = actionString.getBytes("UTF-8");
               bb.put((byte)byteArray.length);
               bb.put(byteArray);
               if (entry.isQuickSkillLess()) {
                  bb.put((byte)1);
               } else {
                  bb.put((byte)0);
               }
            }

            byte[] byteArray = helpstring.getBytes("UTF-8");
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            this.addMessageToQueue(bb);
         } catch (Exception var9) {
            logger.log(Level.INFO, this.player.getName() + ":" + var9.getMessage(), (Throwable)var9);
            this.player.setLink(false);
         }
      } else if (logger.isLoggable(Level.FINER)) {
         logger.finer(
            "Not sending Available Actions as Player has lost link, requestId: "
               + String.valueOf((int)requestId)
               + ", availableActions: "
               + availableActions
               + ", helpstring: "
               + helpstring
         );
      }
   }

   @Override
   public void sendItem(Item item, long creatureId, boolean onGroundLevel) {
      if (this.player.hasLink()) {
         try {
            long id = item.getWurmId();
            byte[] byteArray = item.getModelName().getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)-9);
            bb.putLong(id);
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            byteArray = item.getName().getBytes("UTF-8");
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            bb.putFloat(item.getPosX());
            bb.putFloat(item.getPosY());
            bb.putFloat(item.getRotation());
            if (item.isFloating() && item.getPosZ() <= 0.0F) {
               if (item.getCurrentQualityLevel() < 10.0F) {
                  bb.putFloat(-3000.0F);
               } else {
                  bb.putFloat(0.0F);
               }
            } else if (item.getFloorLevel() <= 0 && onGroundLevel) {
               bb.putFloat(-3000.0F);
            } else {
               bb.putFloat(item.getPosZ());
            }

            bb.put((byte)(item.isOnSurface() ? 0 : -1));
            byteArray = item.getDescription().getBytes("UTF-8");
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            bb.putShort(item.getImageNumber());
            this.addMessageToQueue(bb);
            if (logger.isLoggable(Level.FINER)) {
               logger.finer(this.player.getName() + " sent item " + item.getName() + " - " + item.getWurmId());
            }
         } catch (Exception var9) {
            logger.log(Level.WARNING, "Failed to send item: " + this.player.getName() + ":" + item.getWurmId() + ", " + var9.getMessage(), (Throwable)var9);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendRename(Item item, String newName, String newModelName) {
      if (this.player.hasLink()) {
         try {
            long id = item.getWurmId();
            byte[] byteArray = newName.getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)44);
            bb.putLong(id);
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            bb.put(item.getMaterial());
            byteArray = item.getDescription().getBytes("UTF-8");
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            bb.putShort(item.getImageNumber());
            bb.put(item.getRarity());
            this.addMessageToQueue(bb);
         } catch (Exception var8) {
            logger.log(Level.WARNING, "Failed to rename item: " + this.player.getName() + ":" + item.getWurmId() + ", " + var8.getMessage(), (Throwable)var8);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendRemoveItem(Item item) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(9);
            bb.put((byte)10);
            bb.putLong(item.getWurmId());
            if (logger.isLoggable(Level.FINER)) {
               logger.finer(this.player.getName() + " Sending remove " + item.getWurmId());
            }

            this.addMessageToQueue(bb);
         } catch (Exception var3) {
            logger.log(Level.INFO, this.player.getName() + ":" + var3.getMessage(), (Throwable)var3);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendAddSkill(int id, int parentSkillId, String name, float value, float maxValue, int affinities) {
      if (this.player.hasLink()) {
         try {
            byte[] byteArray = name.getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(byteArray.length + 22);
            bb.put((byte)124);
            bb.putLong(BigInteger.valueOf((long)parentSkillId).shiftLeft(32).longValue() + 18L);
            bb.putLong(BigInteger.valueOf((long)id).shiftLeft(32).longValue() + 18L);
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            bb.putFloat(value);
            bb.putFloat(maxValue);
            bb.put((byte)affinities);
            this.addMessageToQueue(bb);
         } catch (Exception var9) {
            logger.log(Level.INFO, this.player.getName() + ":" + var9.getMessage(), (Throwable)var9);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendUpdateSkill(int id, float value, int affinities) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(13);
            bb.put((byte)66);
            bb.putLong(((long)id << 32) + 18L);
            bb.putFloat(value);
            bb.put((byte)affinities);
            this.addMessageToQueue(bb);
         } catch (Exception var5) {
            logger.log(Level.INFO, this.player.getName() + ":" + var5.getMessage(), (Throwable)var5);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendActionControl(long creatureId, String actionString, boolean start, int timeLeft) {
      if (this.player.hasLink()) {
         try {
            byte[] byteArray = "".getBytes("UTF-8");
            if (start) {
               byteArray = actionString.getBytes("UTF-8");
            }

            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)-12);
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            int lTimeLeft = Math.min(timeLeft, 65535);
            bb.putShort((short)lTimeLeft);
            bb.putLong(creatureId);
            this.addMessageToQueue(bb);
         } catch (Exception var9) {
            logger.log(Level.INFO, this.player.getName() + ":" + var9.getMessage(), (Throwable)var9);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendAddEffect(long id, short type, float x, float y, float z, byte layer) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(24);
            bb.put((byte)64);
            bb.putLong(id);
            bb.putShort(type);
            bb.putFloat(x);
            bb.putFloat(y);
            bb.putFloat(z);
            bb.put(layer);
            this.addMessageToQueue(bb);
         } catch (Exception var9) {
            logger.log(Level.INFO, this.player.getName() + ":" + var9.getMessage(), (Throwable)var9);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendRemoveEffect(long id) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(9);
            bb.put((byte)37);
            bb.putLong(id);
            this.addMessageToQueue(bb);
         } catch (Exception var4) {
            logger.log(Level.INFO, this.player.getName() + ":" + var4.getMessage(), (Throwable)var4);
            this.player.setLink(false);
         }
      }
   }

   @Override
   protected void sendStamina(int stamina, int damage) {
      if (this.player.hasLink() && !this.player.isTransferring()) {
         try {
            ByteBuffer bb = this.getBuffer(5);
            bb.put((byte)90);
            short lStamina = (short)((int)((long)(stamina & 65534) | this.newSeed >> this.newSeedPointer++ & 1L));
            bb.putShort(lStamina);
            bb.putShort((short)damage);
            this.addMessageToQueue(bb);
            if (this.newSeedPointer == 32) {
               this.getConnection().encryptRandom.setSeed(this.newSeed & -1L);
               this.getConnection().changeProtocol(this.newSeed);
               this.newSeedPointer = 0;
               this.newSeed = (long)(Server.rand.nextInt() & 2147483647);
            }
         } catch (Exception var5) {
            logger.log(Level.INFO, this.player.getName() + ":" + var5.getMessage(), (Throwable)var5);
            this.player.setLink(false);
         }
      }
   }

   @Override
   protected void sendThirst(int thirst) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(3);
            bb.put((byte)105);
            bb.putShort((short)thirst);
            this.addMessageToQueue(bb);
         } catch (Exception var3) {
            logger.log(Level.INFO, this.player.getName() + ":" + var3.getMessage(), (Throwable)var3);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendHunger(int hunger, float nutrition, float calories, float carbs, float fats, float proteins) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(3);
            bb.put((byte)61);
            bb.putShort((short)hunger);
            bb.put((byte)((int)(nutrition * 100.0F)));
            bb.put((byte)((int)(calories * 100.0F)));
            bb.put((byte)((int)(carbs * 100.0F)));
            bb.put((byte)((int)(fats * 100.0F)));
            bb.put((byte)((int)(proteins * 100.0F)));
            this.addMessageToQueue(bb);
         } catch (Exception var8) {
            logger.log(Level.INFO, this.player.getName() + ":" + var8.getMessage(), (Throwable)var8);
            this.player.setLink(false);
         }
      }
   }

   @Override
   protected void sendWeight(byte weight) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)5);
            bb.put(weight);
            this.addMessageToQueue(bb);
         } catch (Exception var3) {
            logger.log(Level.INFO, this.player.getName() + ":" + var3.getMessage(), (Throwable)var3);
            this.player.setLink(false);
         }
      }
   }

   @Override
   protected void sendSpeedModifier(float speedModifier) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(5);
            bb.put((byte)32);
            bb.putFloat(speedModifier);
            this.addMessageToQueue(bb);
         } catch (Exception var3) {
            logger.log(Level.INFO, this.player.getName() + ":" + var3.getMessage(), (Throwable)var3);
            this.player.setLink(false);
         }
      }
   }

   @Override
   protected void sendTimeLeft(short tenthOfSeconds) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)87);
            bb.putShort(tenthOfSeconds);
            this.addMessageToQueue(bb);
         } catch (Exception var3) {
            logger.log(Level.INFO, this.player.getName() + ":" + var3.getMessage(), (Throwable)var3);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendSingleBuildMarker(long structureId, int tilex, int tiley, byte layer) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(14);
            bb.put((byte)96);
            bb.putLong(structureId);
            bb.put(layer);
            bb.put((byte)1);
            bb.putShort((short)tilex);
            bb.putShort((short)tiley);
            this.addMessageToQueue(bb);
            if (logger.isLoggable(Level.FINER)) {
               logger.finer("adding or removing single marker");
            }
         } catch (Exception var7) {
            logger.log(Level.INFO, this.player.getName() + ":" + var7.getMessage(), (Throwable)var7);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendMultipleBuildMarkers(long structureId, VolaTile[] tiles, byte layer) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)96);
            bb.putLong(structureId);
            bb.put(layer);
            bb.put((byte)tiles.length);

            for(int x = 0; x < tiles.length; ++x) {
               bb.putShort((short)tiles[x].getTileX());
               bb.putShort((short)tiles[x].getTileY());
            }

            this.addMessageToQueue(bb);
         } catch (Exception var7) {
            logger.log(Level.INFO, this.player.getName() + ":" + var7.getMessage(), (Throwable)var7);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendAddStructure(String name, short centerTilex, short centerTiley, long structureId, byte structureType, byte layer) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)112);
            bb.putLong(structureId);
            bb.put(structureType);
            byte[] byteArray = name.getBytes("UTF-8");
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            bb.putShort(centerTilex);
            bb.putShort(centerTiley);
            bb.put(layer);
            this.addMessageToQueue(bb);
         } catch (Exception var10) {
            logger.log(Level.INFO, this.player.getName() + ":" + var10.getMessage(), (Throwable)var10);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendRemoveStructure(long structureId) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(9);
            bb.put((byte)48);
            bb.putLong(structureId);
            this.addMessageToQueue(bb);
         } catch (Exception var4) {
            logger.log(Level.INFO, this.player.getName() + ":" + var4.getMessage(), (Throwable)var4);
            this.player.setLink(false);
         }
      }
   }

   @Override
   protected void sendUpdateFence(Fence fence) {
      this.sendRemoveFence(fence);
      this.sendAddFence(fence);
   }

   @Override
   public void sendAddWall(long structureId, Wall wall) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)49);
            bb.putLong(structureId);
            bb.putShort((short)Math.min(wall.getStartY(), wall.getEndY()));
            bb.putShort((short)Math.min(wall.getStartX(), wall.getEndX()));
            if (wall.isHorizontal()) {
               bb.put((byte)0);
            } else {
               bb.put((byte)1);
            }

            if (wall.isFinished()) {
               bb.put((byte)wall.getType().ordinal());
            } else {
               bb.put((byte)StructureTypeEnum.PLAN.ordinal());
            }

            byte[] byteArray = wall.getMaterialString().getBytes();
            if (logger.isLoggable(Level.FINER)) {
               logger.finer(structureId + " Updating " + wall.getMaterialString());
            }

            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            bb.put((byte)(wall.getColor() == -1 ? 0 : 1));
            if (wall.getColor() != -1) {
               bb.put((byte)WurmColor.getColorRed(wall.getColor()));
               bb.put((byte)WurmColor.getColorGreen(wall.getColor()));
               bb.put((byte)WurmColor.getColorBlue(wall.getColor()));
            }

            this.addMessageToQueue(bb);
         } catch (Exception var6) {
            logger.log(Level.INFO, this.player.getName() + ":" + var6.getMessage(), (Throwable)var6);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendPassable(boolean passable, Door door) {
      if (this.player.hasLink()) {
         try {
            Wall wall = door.getWall();
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)125);
            bb.putLong(door.getStructureId());
            bb.putShort((short)Math.min(wall.getStartX(), wall.getEndX()));
            bb.putShort((short)Math.min(wall.getStartY(), wall.getEndY()));
            if (wall.isHorizontal()) {
               bb.put((byte)0);
            } else {
               bb.put((byte)1);
            }

            if (passable) {
               bb.put((byte)1);
            } else {
               bb.put((byte)0);
            }

            this.addMessageToQueue(bb);
         } catch (NoSuchWallException var5) {
            logger.log(
               Level.WARNING,
               this.player.getName() + ": Trying to make door passable for wall with no id! Structure=" + door.getStructureId(),
               (Throwable)var5
            );
            return;
         } catch (Exception var6) {
            logger.log(Level.INFO, this.player.getName() + ":" + var6.getMessage(), (Throwable)var6);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendOpenDoor(Door door) {
      if (this.player.hasLink()) {
         try {
            Wall wall = door.getWall();
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)122);
            bb.putLong(door.getStructureId());
            bb.putShort((short)Math.min(wall.getStartX(), wall.getEndX()));
            bb.putShort((short)Math.min(wall.getStartY(), wall.getEndY()));
            if (wall.isHorizontal()) {
               bb.put((byte)0);
            } else {
               bb.put((byte)1);
            }

            this.addMessageToQueue(bb);
         } catch (NoSuchWallException var4) {
            logger.log(Level.WARNING, this.player.getName() + ": trying to open door for wall with no id! Structure=" + door.getStructureId(), (Throwable)var4);
            return;
         } catch (Exception var5) {
            logger.log(Level.INFO, this.player.getName() + ":" + var5.getMessage(), (Throwable)var5);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendCloseDoor(Door door) {
      if (this.player.hasLink()) {
         try {
            Wall wall = door.getWall();
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)127);
            bb.putLong(door.getStructureId());
            bb.putShort((short)Math.min(wall.getStartX(), wall.getEndX()));
            bb.putShort((short)Math.min(wall.getStartY(), wall.getEndY()));
            if (wall.isHorizontal()) {
               bb.put((byte)0);
            } else {
               bb.put((byte)1);
            }

            this.addMessageToQueue(bb);
         } catch (NoSuchWallException var4) {
            logger.log(
               Level.WARNING, this.player.getName() + ": trying to close door for wall with no id! Structure=" + door.getStructureId(), (Throwable)var4
            );
            return;
         } catch (Exception var5) {
            logger.log(Level.INFO, this.player.getName() + ":" + var5.getMessage(), (Throwable)var5);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendBml(int width, int height, boolean resizeable, boolean closeable, String content, int r, int g, int b, String title) {
      if (this.player.hasLink()) {
         try {
            byte[] byteArray = title.getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)106);
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            bb.putShort((short)width);
            bb.putShort((short)height);
            bb.put((byte)(closeable ? 1 : 0));
            bb.put((byte)(resizeable ? 1 : 0));
            bb.put((byte)r);
            bb.put((byte)g);
            bb.put((byte)b);
            byteArray = content.getBytes("UTF-8");
            bb.putShort((short)byteArray.length);
            bb.put(byteArray);
            this.addMessageToQueue(bb);
         } catch (Exception var12) {
            logger.log(Level.INFO, this.player.getName() + ":" + var12.getMessage(), (Throwable)var12);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendChangeStructureName(long structureId, String newName) {
      if (this.player.hasLink()) {
         try {
            byte[] byteArray = newName.getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)47);
            bb.put((byte)0);
            bb.putLong(structureId);
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            this.addMessageToQueue(bb);
         } catch (Exception var6) {
            logger.log(Level.INFO, this.player.getName() + ":" + var6.getMessage(), (Throwable)var6);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendUseBinoculars() {
      this.sendClientFeature((byte)1, true);
   }

   @Override
   public void sendStopUseBinoculars() {
      this.sendClientFeature((byte)1, false);
   }

   @Override
   public void sendToggle(int toggle, boolean set) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(3);
            bb.put((byte)62);
            bb.put((byte)toggle);
            bb.put((byte)(set ? 1 : 0));
            this.addMessageToQueue(bb);
         } catch (Exception var4) {
            logger.log(
               Level.INFO,
               "Problem sending toggle (" + toggle + ',' + set + ") to " + this.player.getName() + " due to :" + var4.getMessage(),
               (Throwable)var4
            );
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendTeleport(boolean aLocal) {
      this.sendTeleport(aLocal, true, (byte)0);
   }

   @Override
   public void sendTeleport(boolean aLocal, boolean disembark, byte commandType) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(21);
            bb.put((byte)51);
            bb.putFloat(this.player.getStatus().getPositionX());
            bb.putFloat(this.player.getStatus().getPositionY());
            bb.putFloat(this.player.getStatus().getPositionZ());
            bb.putFloat(this.player.getStatus().getRotation());
            bb.put((byte)(aLocal ? 1 : 0));
            bb.put((byte)(this.player.isOnSurface() ? 0 : -1));
            bb.put((byte)(disembark ? 1 : 0));
            bb.put(commandType);
            this.addMessageToQueue(bb);
            this.currentmove = null;
            this.setMoves(0);
            this.receivedTicks = false;
         } catch (Exception var5) {
            logger.log(
               Level.INFO, "Problem sending teleport (local: " + aLocal + ") to " + this.player.getName() + " due to :" + var5.getMessage(), (Throwable)var5
            );
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendStartTrading(Creature opponent) {
      Trade trade = this.player.getTrade();
      if (trade != null && this.player.hasLink()) {
         try {
            String name = opponent.getName();
            byte[] byteArray = name.getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)119);
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            if (trade.creatureOne == this.player) {
               bb.putLong(1L);
               bb.putLong(2L);
               bb.putLong(3L);
               bb.putLong(4L);
            } else {
               bb.putLong(2L);
               bb.putLong(1L);
               bb.putLong(4L);
               bb.putLong(3L);
            }

            this.addMessageToQueue(bb);
         } catch (Exception var6) {
            logger.log(Level.INFO, this.player.getName() + ":" + var6.getMessage(), (Throwable)var6);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendCloseTradeWindow() {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(1);
            bb.put((byte)121);
            this.addMessageToQueue(bb);
         } catch (Exception var2) {
            logger.log(Level.INFO, this.player.getName() + ":" + var2.getMessage(), (Throwable)var2);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendTradeAgree(Creature agreer, boolean agree) {
      if (this.player.hasLink()) {
         try {
            boolean me = false;
            if (agreer == this.player) {
               me = true;
            }

            if (me && agree) {
               return;
            }

            ByteBuffer bb = this.getBuffer(2);
            bb.put((byte)42);
            bb.put((byte)(agree ? 1 : 0));
            this.addMessageToQueue(bb);
         } catch (Exception var5) {
            logger.log(Level.INFO, this.player.getName() + ":" + var5.getMessage(), (Throwable)var5);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendTradeChanged(int id) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(5);
            bb.put((byte)91);
            bb.putInt(id);
            this.addMessageToQueue(bb);
         } catch (Exception var3) {
            logger.log(Level.INFO, this.player.getName() + ":" + var3.getMessage(), (Throwable)var3);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendAddFence(Fence fence) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)12);
            bb.putShort((short)fence.getTileX());
            bb.putShort((short)fence.getTileY());
            bb.put(fence.getDir().getCode());
            bb.putShort((short)fence.getType().ordinal());
            bb.put((byte)(fence.isFinished() ? 1 : 0));
            bb.put((byte)(fence.getColor() == -1 ? 0 : 1));
            if (fence.getColor() != -1) {
               bb.put((byte)WurmColor.getColorRed(fence.getColor()));
               bb.put((byte)WurmColor.getColorGreen(fence.getColor()));
               bb.put((byte)WurmColor.getColorBlue(fence.getColor()));
            }

            this.addMessageToQueue(bb);
         } catch (Exception var3) {
            logger.log(Level.INFO, this.player.getName() + " adding fence: " + fence + " :" + var3.getMessage(), (Throwable)var3);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendRemoveFence(Fence fence) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)13);
            bb.putShort((short)fence.getTileX());
            bb.putShort((short)fence.getTileY());
            bb.put(fence.getDir().getCode());
            this.addMessageToQueue(bb);
         } catch (Exception var3) {
            logger.log(Level.INFO, this.player.getName() + " problem removing fence: " + fence + " due to :" + var3.getMessage(), (Throwable)var3);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendOpenFence(Fence fence, boolean passable, boolean changePassable) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)83);
            bb.putShort((short)fence.getTileX());
            bb.putShort((short)fence.getTileY());
            bb.put(fence.getDir().getCode());
            bb.put((byte)1);
            if (changePassable) {
               bb.put((byte)(passable ? 1 : 0));
            } else {
               bb.put((byte)2);
            }

            this.addMessageToQueue(bb);
         } catch (Exception var5) {
            logger.log(Level.INFO, this.player.getName() + " problem opening fence: " + fence + " due to :" + var5.getMessage(), (Throwable)var5);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendCloseFence(Fence fence, boolean passable, boolean changePassable) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)83);
            bb.putShort((short)fence.getTileX());
            bb.putShort((short)fence.getTileY());
            bb.put(fence.getDir().getCode());
            bb.put((byte)0);
            if (changePassable) {
               bb.put((byte)(passable ? 1 : 0));
            } else {
               bb.put((byte)2);
            }

            this.addMessageToQueue(bb);
         } catch (Exception var5) {
            logger.log(Level.INFO, this.player.getName() + " problem closing fence: " + fence + " due to :" + var5.getMessage(), (Throwable)var5);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendSound(Sound sound) {
      if (this.player.hasLink()) {
         try {
            String name = sound.getName();
            byte[] byteArray = name.getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)86);
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            bb.putFloat(sound.getPosX());
            bb.putFloat(sound.getPosY());
            bb.putFloat(sound.getPosZ());
            bb.putFloat(sound.getPitch());
            bb.putFloat(sound.getVolume());
            bb.putFloat(sound.getPriority());
            this.addMessageToQueue(bb);
         } catch (Exception var5) {
            logger.log(Level.INFO, this.player.getName() + ":" + var5.getMessage(), (Throwable)var5);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendMusic(Sound sound) {
      if (this.player.hasLink()) {
         try {
            String name = sound.getName();
            byte[] byteArray = name.getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)115);
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            bb.putFloat(sound.getPosX());
            bb.putFloat(sound.getPosY());
            bb.putFloat(sound.getPosZ());
            bb.putFloat(sound.getPitch());
            bb.putFloat(sound.getVolume());
            bb.putFloat(sound.getPriority());
            this.addMessageToQueue(bb);
         } catch (Exception var5) {
            logger.log(Level.INFO, this.player.getName() + ":" + var5.getMessage(), (Throwable)var5);
            this.player.setLink(false);
         }
      }
   }

   @Override
   protected void sendStatus(String status) {
      if (this.player.hasLink()) {
         try {
            byte[] byteArray = status.getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)-18);
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            this.addMessageToQueue(bb);
         } catch (Exception var4) {
            logger.log(Level.INFO, this.player.getName() + ":" + var4.getMessage(), (Throwable)var4);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendAddWound(Wound wound, Item bodyPart) {
      if (this.player.hasLink()) {
         try {
            byte[] byteArray = wound.getName().getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)76);
            if (this.player == wound.getCreature()) {
               bb.putLong(-1L);
            } else {
               Item body = wound.getCreature().getBody().getBodyItem();
               bb.putLong(body.getWurmId());
            }

            long parentId = bodyPart.getWurmId();
            bb.putLong(parentId);
            bb.putLong(wound.getWurmId());
            bb.putShort((short)wound.getWoundIconId());
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            byteArray = wound.getDescription().getBytes("UTF-8");
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            bb.putFloat(100.0F);
            if (logger.isLoggable(Level.FINER)) {
               logger.finer(
                  "Sending wound ID: "
                     + wound.getWurmId()
                     + ", severity: "
                     + wound.getSeverity()
                     + "*"
                     + 0.0015259022F
                     + "="
                     + wound.getSeverity() * 0.0015259022F
               );
            }

            bb.putFloat(wound.getSeverity() * 0.0015259022F);
            bb.putInt(0);
            bb.put((byte)0);
            bb.put((byte)0);
            this.addMessageToQueue(bb);
         } catch (Exception var7) {
            logger.log(Level.INFO, this.player.getName() + ":" + wound.getWoundString(), (Throwable)var7);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendRemoveWound(Wound wound) {
      if (this.player.hasLink()) {
         try {
            if (logger.isLoggable(Level.FINER)) {
               logger.finer("Removing wound ID " + wound.getWurmId() + " from player inventory.");
            }

            ByteBuffer bb = this.getBuffer(17);
            bb.put((byte)-10);
            if (this.player == wound.getCreature()) {
               bb.putLong(-1L);
            } else {
               Item body = wound.getCreature().getBody().getBodyItem();
               bb.putLong(body.getWurmId());
            }

            bb.putLong(wound.getWurmId());
            this.addMessageToQueue(bb);
         } catch (Exception var4) {
            logger.log(Level.INFO, this.player.getName() + ":" + var4.getMessage(), (Throwable)var4);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendUpdateWound(Wound wound, Item bodyPart) {
      if (this.player.hasLink()) {
         try {
            byte[] byteArray = wound.getName().getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)68);
            if (this.player == wound.getCreature()) {
               bb.putLong(-1L);
            } else {
               Item body = wound.getCreature().getBody().getBodyItem();
               bb.putLong(body.getWurmId());
            }

            bb.putLong(wound.getWurmId());
            long parentId = bodyPart.getWurmId();
            bb.putLong(parentId);
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            byteArray = wound.getDescription().getBytes("UTF-8");
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            bb.putFloat(100.0F);
            if (logger.isLoggable(Level.FINER)) {
               logger.finer(
                  "Sending wound ID: "
                     + wound.getWurmId()
                     + ", severity: "
                     + wound.getSeverity()
                     + "*"
                     + 0.0015259022F
                     + "="
                     + wound.getSeverity() * 0.0015259022F
               );
            }

            bb.putFloat(wound.getSeverity() * 0.0015259022F);
            bb.putInt(0);
            bb.put((byte)0);
            bb.put((byte)0);
            this.addMessageToQueue(bb);
         } catch (Exception var7) {
            logger.log(Level.INFO, this.player.getName() + ":" + var7.getMessage(), (Throwable)var7);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendSelfToLocal() {
      if (this.player.hasLink()) {
         try {
            byte[] byteArray = this.player.getName().getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)-13);
            bb.put((byte)local.length);
            bb.put(local);
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            bb.putLong(this.player.getWurmId());
            this.addMessageToQueue(bb);
         } catch (Exception var3) {
            logger.log(Level.WARNING, this.player.getName() + ":" + var3.getMessage(), (Throwable)var3);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendAddVillager(String name, long wurmid) {
      if (this.player.hasLink()) {
         try {
            byte[] byteArray = name.getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)-13);
            bb.put((byte)village.length);
            bb.put(village);
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            bb.putLong(wurmid);
            this.addMessageToQueue(bb);
         } catch (Exception var6) {
            logger.log(Level.WARNING, this.player.getName() + ":" + var6.getMessage(), (Throwable)var6);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendRemoveVillager(String name) {
      if (this.player.hasLink()) {
         try {
            byte[] byteArray = name.getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)114);
            bb.put((byte)village.length);
            bb.put(village);
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            this.addMessageToQueue(bb);
         } catch (Exception var4) {
            logger.log(Level.WARNING, this.player.getName() + ":" + var4.getMessage(), (Throwable)var4);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendAddAlly(String name, long wurmid) {
      if (this.player != null && this.player.hasLink()) {
         try {
            byte[] tempStringArr = name.getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)-13);
            bb.put((byte)alliance.length);
            bb.put(alliance);
            bb.put((byte)tempStringArr.length);
            bb.put(tempStringArr);
            bb.putLong(wurmid);
            this.addMessageToQueue(bb);
         } catch (Exception var6) {
            logger.log(Level.WARNING, this.player.getName() + ':' + var6.getMessage(), (Throwable)var6);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendRemoveAlly(String name) {
      if (this.player != null && this.player.hasLink()) {
         try {
            byte[] tempStringArr = name.getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)114);
            bb.put((byte)alliance.length);
            bb.put(alliance);
            bb.put((byte)tempStringArr.length);
            bb.put(tempStringArr);
            this.addMessageToQueue(bb);
         } catch (Exception var4) {
            logger.log(Level.WARNING, this.player.getName() + ':' + var4.getMessage(), (Throwable)var4);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendAddLocal(String name, long wurmid) {
      if (this.player.hasLink()) {
         try {
            byte[] byteArray = name.getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)-13);
            bb.put((byte)local.length);
            bb.put(local);
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            bb.putLong(wurmid);
            this.addMessageToQueue(bb);
         } catch (Exception var6) {
            logger.log(Level.WARNING, this.player.getName() + ":" + var6.getMessage(), (Throwable)var6);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendRemoveLocal(String name) {
      if (this.player.hasLink()) {
         try {
            byte[] byteArray = name.getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)114);
            bb.put((byte)local.length);
            bb.put(local);
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            this.addMessageToQueue(bb);
         } catch (Exception var4) {
            logger.log(Level.WARNING, this.player.getName() + ":" + var4.getMessage(), (Throwable)var4);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendAddGm(String name, long wurmid) {
      if (this.player.hasLink()) {
         try {
            byte[] byteArray = name.getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)-13);
            bb.put((byte)gms.length);
            bb.put(gms);
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            bb.putLong(wurmid);
            this.addMessageToQueue(bb);
         } catch (Exception var6) {
            logger.log(Level.WARNING, this.player.getName() + ":" + var6.getMessage(), (Throwable)var6);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendRemoveGm(String name) {
      if (this.player.hasLink()) {
         try {
            byte[] byteArray = name.getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)114);
            bb.put((byte)gms.length);
            bb.put(gms);
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            this.addMessageToQueue(bb);
         } catch (Exception var4) {
            logger.log(Level.WARNING, this.player.getName() + ":" + var4.getMessage(), (Throwable)var4);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void changeAttitude(long creatureId, byte status) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(10);
            bb.put((byte)6);
            bb.putLong(creatureId);
            bb.put(status);
            this.addMessageToQueue(bb);
         } catch (Exception var5) {
            logger.log(Level.INFO, this.player.getName() + ":" + var5.getMessage(), (Throwable)var5);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendWeather() {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(29);
            bb.put((byte)46);
            bb.putFloat(Server.getWeather().getCloudiness());
            bb.putFloat(Server.getWeather().getFog());
            bb.putFloat(Server.getWeather().getRain());
            bb.putFloat(Server.getWeather().getXWind());
            bb.putFloat(Server.getWeather().getYWind());
            bb.putFloat(Server.getWeather().getWindRotation());
            bb.putFloat(Server.getWeather().getWindPower());
            this.addMessageToQueue(bb);
            this.sendNormalServerMessage(
               "The wind is now coming from "
                  + Server.getWeather().getWindRotation()
                  + "- strength x="
                  + Server.getWeather().getXWind()
                  + ", y="
                  + Server.getWeather().getYWind()
            );
         } catch (Exception var2) {
            logger.log(Level.INFO, this.player.getName() + ":" + var2.getMessage(), (Throwable)var2);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendDead() {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(3);
            bb.put((byte)65);
            this.addMessageToQueue(bb);
         } catch (Exception var2) {
            logger.log(Level.INFO, this.player.getName() + ":" + var2.getMessage(), (Throwable)var2);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendClimb(boolean climbing) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(2);
            bb.put((byte)79);
            bb.put((byte)(climbing ? 1 : 0));
            this.addMessageToQueue(bb);
         } catch (Exception var3) {
            logger.log(Level.INFO, this.player.getName() + ":" + var3.getMessage(), (Throwable)var3);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendReconnect(String ip, int port, String session) {
      if (logger.isLoggable(Level.FINE)) {
         logger.fine("Sending reconnect to server: " + ip + ':' + port + " to " + this.player);
      }

      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)23);
            byte[] byteArray = ip.getBytes("UTF-8");
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            bb.putInt(port);
            byteArray = session.getBytes("UTF-8");
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            this.addMessageToQueue(bb);
         } catch (Exception var6) {
            logger.log(Level.INFO, this.player.getName() + ":" + var6.getMessage(), (Throwable)var6);
            this.player.setLink(false);
         }
      }
   }

   @Override
   protected void sendHasMoreItems(long inventoryId, long wurmid) {
      if (this.player.hasLink()) {
         try {
            if (logger.isLoggable(Level.FINER)) {
               logger.finer("Inventory " + inventoryId + " containing Wurmid " + wurmid + " has MORE.");
            }

            ByteBuffer bb = this.getBuffer(17);
            bb.put((byte)29);
            bb.putLong(inventoryId);
            bb.putLong(wurmid);
            this.addMessageToQueue(bb);
         } catch (Exception var6) {
            logger.log(Level.INFO, this.player.getName() + ":" + var6.getMessage(), (Throwable)var6);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendIsEmpty(long inventoryId, long wurmid) {
      if (this.player.hasLink()) {
         try {
            if (logger.isLoggable(Level.FINER)) {
               logger.finer("Inventory " + inventoryId + " containing Wurmid " + wurmid + " has no more items.");
            }

            ByteBuffer bb = this.getBuffer(17);
            bb.put((byte)-16);
            bb.putLong(inventoryId);
            bb.putLong(wurmid);
            this.addMessageToQueue(bb);
         } catch (Exception var6) {
            logger.log(Level.INFO, this.player.getName() + ":" + var6.getMessage(), (Throwable)var6);
            this.player.setLink(false);
         }
      }
   }

   @Override
   protected void sendCompass(Item item) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(3);
            bb.put((byte)-30);
            bb.put((byte)0);
            if (item == null) {
               bb.put((byte)0);
            } else {
               bb.put((byte)((int)Math.max(1.0F, item.getCurrentQualityLevel())));
            }

            this.addMessageToQueue(bb);
         } catch (Exception var3) {
            logger.log(Level.INFO, this.player.getName() + ":" + var3.getMessage(), (Throwable)var3);
            this.player.setLink(false);
         }
      }
   }

   @Override
   protected void sendToolbelt(Item item) {
      if (this.player.hasLink()) {
         try {
            if (logger.isLoggable(Level.FINER)) {
               if (item != null) {
                  logger.finer(this.player.getName() + " sending toolbelt with wurmid " + item.getWurmId() + ".");
               } else {
                  logger.finer(this.player.getName() + " sending toolbelt null.");
               }
            }

            ByteBuffer bb = this.getBuffer(3);
            bb.put((byte)-30);
            bb.put((byte)2);
            if (item == null) {
               bb.put((byte)0);
            } else {
               bb.put((byte)((int)Math.max(1.0F, item.getCurrentQualityLevel())));
            }

            this.addMessageToQueue(bb);
         } catch (Exception var3) {
            logger.log(Level.INFO, this.player.getName() + ":" + var3.getMessage(), (Throwable)var3);
            this.player.setLink(false);
         }
      }
   }

   private void sendClientFeature(byte feature, boolean on) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(3);
            bb.put((byte)-30);
            bb.put(feature);
            if (on) {
               bb.put((byte)1);
            } else {
               bb.put((byte)0);
            }

            this.addMessageToQueue(bb);
         } catch (Exception var4) {
            logger.log(Level.INFO, this.player.getName() + ":" + var4.getMessage(), (Throwable)var4);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendServerTime() {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(17);
            bb.put((byte)107);
            bb.putLong(System.currentTimeMillis());
            bb.putLong(WurmCalendar.currentTime + this.timeMod);
            this.addMessageToQueue(bb);
         } catch (Exception var2) {
            logger.log(Level.INFO, this.player.getName() + ":" + var2.getMessage(), (Throwable)var2);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendAttachEffect(long targetId, byte effectType, byte data0, byte data1, byte data2, byte dimension) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(13);
            bb.put((byte)109);
            bb.putLong(targetId);
            bb.put(effectType);
            bb.put(data0);
            bb.put(data1);
            bb.put(data2);
            bb.put(dimension);
            if (logger.isLoggable(Level.FINEST)) {
               logger.finest(this.player.getName() + ": " + targetId + ", light colour: " + data0 + ", " + data1 + ", " + data2);
            }

            this.addMessageToQueue(bb);
         } catch (Exception var9) {
            logger.log(Level.INFO, this.player.getName() + ":" + var9.getMessage(), (Throwable)var9);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendRemoveEffect(long targetId, byte effectType) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(10);
            bb.put((byte)18);
            bb.putLong(targetId);
            bb.put(effectType);
            this.addMessageToQueue(bb);
            if (logger.isLoggable(Level.FINER)) {
               logger.finer(this.player.getName() + " removing :" + targetId + ", light " + effectType);
            }
         } catch (Exception var5) {
            logger.log(Level.INFO, this.player.getName() + ":" + var5.getMessage(), (Throwable)var5);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendWieldItem(
      long creatureId,
      byte slot,
      String modelname,
      byte rarity,
      int colorRed,
      int colorGreen,
      int colorBlue,
      int secondaryColorRed,
      int secondaryColorGreen,
      int secondaryColorBlue
   ) {
      if (this.player.hasLink()) {
         try {
            if (creatureId == -1L || WurmId.getType(creatureId) == 0) {
               ByteBuffer bb = this.getBuffer(32767);
               bb.put((byte)101);
               bb.putLong(creatureId);
               bb.put(slot);
               byte[] byteArray = modelname.getBytes("UTF-8");
               bb.putShort((short)byteArray.length);
               bb.put(byteArray);
               bb.put(rarity);
               bb.putInt(colorRed);
               bb.putInt(colorGreen);
               bb.putInt(colorBlue);
               bb.putInt(secondaryColorRed);
               bb.putInt(secondaryColorGreen);
               bb.putInt(secondaryColorBlue);
               this.addMessageToQueue(bb);
            }
         } catch (Exception var14) {
            logger.log(Level.INFO, this.player.getName() + ":" + var14.getMessage(), (Throwable)var14);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendUseItem(
      long creatureId,
      String modelname,
      byte rarity,
      int colorRed,
      int colorGreen,
      int colorBlue,
      int secondaryColorRed,
      int secondaryColorGreen,
      int secondaryColorBlue
   ) {
      if (this.player.hasLink()) {
         try {
            if (creatureId == -1L || WurmId.getType(creatureId) == 0) {
               ByteBuffer bb = this.getBuffer(32767);
               bb.put((byte)110);
               bb.putLong(creatureId);
               byte[] byteArray = modelname.getBytes("UTF-8");
               bb.putShort((short)byteArray.length);
               bb.put(byteArray);
               bb.put(rarity);
               bb.putFloat((float)colorRed);
               bb.putFloat((float)colorGreen);
               bb.putFloat((float)colorBlue);
               bb.putInt(secondaryColorRed);
               bb.putInt(secondaryColorGreen);
               bb.putInt(secondaryColorBlue);
               this.addMessageToQueue(bb);
            }
         } catch (Exception var13) {
            logger.log(Level.INFO, this.player.getName() + ":" + var13.getMessage(), (Throwable)var13);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendStopUseItem(long creatureId) {
      if (this.player.hasLink()) {
         try {
            if (creatureId == -1L || WurmId.getType(creatureId) == 0) {
               ByteBuffer bb = this.getBuffer(9);
               bb.put((byte)71);
               bb.putLong(creatureId);
               this.addMessageToQueue(bb);
            }
         } catch (Exception var4) {
            logger.log(Level.INFO, this.player.getName() + ":" + var4.getMessage(), (Throwable)var4);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendRepaint(long wurmid, byte r, byte g, byte b, byte alpha, byte paintType) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(14);
            bb.put((byte)92);
            bb.putLong(wurmid);
            bb.put(r);
            bb.put(g);
            bb.put(b);
            bb.put(alpha);
            bb.put(paintType);
            this.addMessageToQueue(bb);
         } catch (Exception var9) {
            logger.log(Level.INFO, this.player.getName() + ": " + var9.getMessage(), (Throwable)var9);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendResize(long wurmid, byte xscaleMod, byte yscaleMod, byte zscaleMod) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(12);
            bb.put((byte)74);
            bb.putLong(wurmid);
            bb.put(xscaleMod);
            bb.put(yscaleMod);
            bb.put(zscaleMod);
            this.addMessageToQueue(bb);
         } catch (Exception var7) {
            logger.log(Level.INFO, this.player.getName() + ":" + var7.getMessage(), (Throwable)var7);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendNewMovingItem(
      long id,
      String name,
      String model,
      float x,
      float y,
      float z,
      long onBridge,
      float rot,
      byte layer,
      boolean onGround,
      boolean floating,
      boolean isSolid,
      byte material,
      byte rarity
   ) {
      if (this.player.hasLink()) {
         try {
            byte[] byteArray = model.getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)108);
            bb.putLong(id);
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            bb.put((byte)(isSolid ? 1 : 0));
            bb.putFloat(y);
            bb.putFloat(x);
            bb.putLong(onBridge);
            bb.putFloat(rot);
            if (onGround) {
               if (Structure.isGroundFloorAtPosition(x, y, layer == 0)) {
                  bb.putFloat(z + 0.1F);
               } else {
                  bb.putFloat(-3000.0F);
               }
            } else {
               bb.putFloat(z);
            }

            byteArray = name.getBytes("UTF-8");
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            if (floating) {
               bb.put((byte)1);
            } else {
               bb.put((byte)0);
            }

            bb.put((byte)2);
            bb.put(layer);
            bb.put(material);
            bb.put((byte)0);
            bb.put(rarity);
            this.addMessageToQueue(bb);
            if (logger.isLoggable(Level.FINER)) {
               logger.finer(this.player.getName() + " sent creature " + name + " model= " + model + " x " + x + " y " + y + " z " + z);
            }
         } catch (NullPointerException var19) {
            logger.log(Level.WARNING, this.player.getName() + ":" + var19.getMessage(), (Throwable)var19);
            this.player.setLink(false);
         } catch (Exception var20) {
            logger.log(Level.WARNING, this.player.getName() + ":" + var20.getMessage(), (Throwable)var20);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendMoveMovingItem(long id, float x, float y, int rot) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(12);
            bb.put((byte)36);
            bb.putLong(id);
            bb.putFloat(x);
            bb.putFloat(y);
            bb.put((byte)rot);
            this.addMessageToQueue(bb);
         } catch (NullPointerException var7) {
            logger.log(Level.WARNING, var7.getMessage(), (Throwable)var7);
            this.player.setLink(false);
         } catch (Exception var8) {
            logger.log(Level.INFO, this.player.getName() + ":" + var8.getMessage(), (Throwable)var8);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendMoveMovingItemAndSetZ(long id, float x, float y, float z, int rot) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(14);
            bb.put((byte)72);
            bb.putLong(id);
            bb.putFloat(x);
            bb.putFloat(y);
            bb.putFloat(z);
            bb.put((byte)rot);
            this.addMessageToQueue(bb);
         } catch (NullPointerException var8) {
            logger.log(Level.WARNING, var8.getMessage(), (Throwable)var8);
            this.player.setLink(false);
         } catch (Exception var9) {
            logger.log(Level.INFO, this.player.getName() + ":" + var9.getMessage(), (Throwable)var9);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendMovingItemChangedLayer(long wurmid, byte newlayer) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(10);
            bb.put((byte)30);
            bb.putLong(wurmid);
            bb.put(newlayer);
            this.addMessageToQueue(bb);
         } catch (Exception var5) {
            logger.log(Level.INFO, this.player.getName() + ":" + var5.getMessage(), (Throwable)var5);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendDeleteMovingItem(long id) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(9);
            bb.put((byte)14);
            bb.putLong(id);
            this.addMessageToQueue(bb);
         } catch (NullPointerException var4) {
            logger.log(Level.WARNING, this.player.getName() + ":" + var4.getMessage(), (Throwable)var4);
            this.player.setLink(false);
         } catch (Exception var5) {
            logger.log(Level.INFO, this.player.getName() + ":" + var5.getMessage(), (Throwable)var5);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendShutDown(String reason, boolean requested) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(3);
            bb.put((byte)4);
            byte[] tempStringArr = reason.getBytes("UTF-8");
            bb.putShort((short)tempStringArr.length);
            bb.put(tempStringArr);
            bb.put((byte)(requested ? 1 : 0));
            this.addMessageToQueue(bb);
         } catch (Exception var5) {
            logger.log(Level.INFO, this.player.getName() + ":" + var5.getMessage(), (Throwable)var5);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void attachCreature(long source, long target, float offx, float offy, float offz, int seatId) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(29);
            bb.put((byte)111);
            bb.putLong(source);
            bb.putLong(target);
            bb.putFloat(offx);
            bb.putFloat(offy);
            bb.putFloat(offz);
            bb.put((byte)seatId);
            this.addMessageToQueue(bb);
         } catch (Exception var10) {
            logger.log(Level.INFO, this.player.getName() + ":" + var10.getMessage(), (Throwable)var10);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void setVehicleController(
      long playerId,
      long targetId,
      float offx,
      float offy,
      float offz,
      float maxDepth,
      float maxHeight,
      float maxHeightDiff,
      float vehicleRotation,
      int seatId
   ) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(45);
            bb.put((byte)63);
            bb.putLong(playerId);
            bb.putLong(targetId);
            bb.putFloat(offx);
            bb.putFloat(offy);
            bb.putFloat(offz);
            bb.putFloat(maxDepth);
            bb.putFloat(maxHeight);
            bb.putFloat(maxHeightDiff);
            bb.putFloat(vehicleRotation);
            bb.put((byte)seatId);
            this.addMessageToQueue(bb);
         } catch (Exception var14) {
            logger.log(Level.INFO, this.player.getName() + ":" + var14.getMessage(), (Throwable)var14);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendAnimation(long creatureId, String animationName, boolean looping, boolean freezeAtFinish) {
      if (this.player.hasLink() && creatureId > 0L) {
         try {
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)24);
            bb.putLong(creatureId);
            byte[] byteArray = animationName.getBytes("UTF-8");
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            if (looping) {
               bb.put((byte)1);
            } else {
               bb.put((byte)0);
            }

            if (freezeAtFinish) {
               bb.put((byte)1);
            } else {
               bb.put((byte)0);
            }

            this.addMessageToQueue(bb);
         } catch (Exception var8) {
            logger.log(Level.INFO, this.player.getName() + ":" + var8.getMessage(), (Throwable)var8);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendCombatOptions(byte[] options, short tenthsOfSeconds) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)98);
            bb.put((byte)options.length);
            bb.put(options);
            bb.putShort(tenthsOfSeconds);
            this.addMessageToQueue(bb);
         } catch (NullPointerException var4) {
            logger.log(Level.WARNING, this.player.getName() + ":" + var4.getMessage(), (Throwable)var4);
            this.player.setLink(false);
         } catch (Exception var5) {
            logger.log(Level.INFO, this.player.getName() + ":" + var5.getMessage(), (Throwable)var5);
            this.player.setLink(false);
         }
      }
   }

   @Override
   protected void sendCombatStatus(float distanceToTarget, float footing, byte stance) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(10);
            bb.put((byte)-14);
            bb.putFloat(distanceToTarget);
            bb.putFloat(footing);
            bb.put(stance);
            this.addMessageToQueue(bb);
         } catch (NullPointerException var5) {
            logger.log(Level.WARNING, this.player.getName() + ":" + var5.getMessage(), (Throwable)var5);
            this.player.setLink(false);
         } catch (Exception var6) {
            logger.log(Level.INFO, this.player.getName() + ":" + var6.getMessage(), (Throwable)var6);
            this.player.setLink(false);
         }
      }
   }

   @Override
   protected void sendStunned(boolean stunned) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)28);
            if (stunned) {
               bb.put((byte)1);
            } else {
               bb.put((byte)0);
            }

            this.addMessageToQueue(bb);
         } catch (Exception var3) {
            logger.log(Level.INFO, this.player.getName() + ":" + var3.getMessage(), (Throwable)var3);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendSpecialMove(short move, String movename) {
      if (this.player.hasLink()) {
         try {
            byte[] byteArray = movename.getBytes("UTF-8");
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)-17);
            bb.putShort(move);
            bb.put((byte)byteArray.length);
            bb.put(byteArray);
            this.addMessageToQueue(bb);
         } catch (Exception var5) {
            logger.log(Level.INFO, this.player.getName() + ":" + var5.getMessage(), (Throwable)var5);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendToggleShield(boolean on) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(32767);
            bb.put((byte)-17);
            bb.putShort((short)105);
            if (on) {
               bb.put((byte)1);
            } else {
               bb.put((byte)0);
            }

            this.addMessageToQueue(bb);
         } catch (Exception var3) {
            logger.log(Level.INFO, this.player.getName() + ":" + var3.getMessage(), (Throwable)var3);
            this.player.setLink(false);
         }
      }
   }

   @Override
   protected void sendTarget(long id) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(9);
            bb.put((byte)25);
            bb.putLong(id);
            this.addMessageToQueue(bb);
         } catch (Exception var4) {
            logger.log(Level.INFO, this.player.getName() + ":" + var4.getMessage(), (Throwable)var4);
            this.player.setLink(false);
         }
      }
   }

   @Override
   protected void sendFightStyle(byte style) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(2);
            bb.put((byte)26);
            bb.put(style);
            this.addMessageToQueue(bb);
         } catch (Exception var3) {
            logger.log(Level.INFO, this.player.getName() + ":" + var3.getMessage(), (Throwable)var3);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void setCreatureDamage(long wurmid, float damagePercent) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(13);
            bb.put((byte)11);
            bb.putLong(wurmid);
            bb.putFloat(damagePercent);
            this.addMessageToQueue(bb);
         } catch (Exception var5) {
            logger.log(Level.INFO, this.player.getName() + ":" + var5.getMessage(), (Throwable)var5);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendWindImpact(byte windimpact) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(2);
            bb.put((byte)117);
            bb.put(windimpact);
            this.addMessageToQueue(bb);
            this.player.sentWind = System.currentTimeMillis();
         } catch (Exception var3) {
            logger.log(Level.INFO, this.player.getName() + ":" + var3.getMessage(), (Throwable)var3);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendMountSpeed(short mountSpeed) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(2);
            bb.put((byte)60);
            bb.putShort(mountSpeed);
            this.addMessageToQueue(bb);
            this.player.sentMountSpeed = System.currentTimeMillis();
         } catch (Exception var3) {
            logger.log(Level.INFO, this.player.getName() + ":" + var3.getMessage(), (Throwable)var3);
            this.player.setLink(false);
         }
      }
   }

   @Override
   public void sendRotate(long itemId, float rotation) {
      if (this.player.hasLink()) {
         try {
            ByteBuffer bb = this.getBuffer(13);
            bb.put((byte)67);
            bb.putLong(itemId);
            bb.putFloat(rotation);
            this.addMessageToQueue(bb);
         } catch (Exception var5) {
            logger.log(Level.INFO, this.player.getName() + ":" + var5.getMessage(), (Throwable)var5);
            this.player.setLink(false);
         }
      }
   }

   static BlockingQueue<PlayerMessage> getMessageQueue() {
      return MESSAGES_TO_PLAYERS;
   }

   @Override
   public String toString() {
      return super.toString();
   }
}
