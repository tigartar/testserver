package com.wurmonline.server.epic;

import com.wurmonline.server.Constants;
import com.wurmonline.server.Server;
import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class EpicXmlWriter {
   private static final Logger logger = Logger.getLogger(EpicXmlWriter.class.getName());

   private EpicXmlWriter() {
   }

   public static void dumpEntities(HexMap map) {
      if (logger.isLoggable(Level.FINE)) {
         logger.fine("Starting to dump Epic Entities to XML for HexMap: " + map);
      }

      long start = System.nanoTime();

      try {
         EpicEntity[] entities = map.getAllEntities();
         Document document = createEntitiesXmlDocument(entities);
         TransformerFactory transformerFactory = TransformerFactory.newInstance();
         Transformer transformer = transformerFactory.newTransformer();
         DOMSource source = new DOMSource(document);
         File f = new File(Constants.webPath + File.separator + "entities.xml");
         logger.info("Dumping Epic entities to absolute path: " + f.getAbsolutePath());
         f.createNewFile();
         if (f != null) {
            StreamResult result = new StreamResult(new FileOutputStream(f));
            transformer.transform(source, result);
         }
      } catch (Exception var15) {
         logger.log(Level.WARNING, var15.getMessage(), (Throwable)var15);
      } finally {
         long end = System.nanoTime();
         logger.info("Dumping Epic Entities to XML took " + (float)(end - start) / 1000000.0F + " ms");
      }
   }

   static Document createEntitiesXmlDocument(EpicEntity[] entities) throws ParserConfigurationException {
      String root = "entities";
      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      Document document = documentBuilder.newDocument();
      Element rootElement = document.createElement("entities");
      document.appendChild(rootElement);

      for(EpicEntity entity : entities) {
         Element newElement = document.createElement("entity");
         rootElement.appendChild(newElement);
         createNode("name", entity.getName(), document, newElement);
         if (entity.getMapHex() != null) {
            createNode("hexnumber", String.valueOf(entity.getMapHex().getId()), document, newElement);
            createNode("hexname", entity.getMapHex().getName(), document, newElement);
         } else {
            createNode("hexnumber", "-1", document, newElement);
            createNode("hexname", "unknown", document, newElement);
         }

         if (entity.isDeity() || entity.isWurm()) {
            if (System.currentTimeMillis() < entity.getTimeUntilLeave()) {
               long leaveTime = entity.getTimeUntilLeave() - System.currentTimeMillis();
               createNode("timetoleavehex", Server.getTimeFor(leaveTime), document, newElement);
            }

            if (System.currentTimeMillis() < entity.getTimeToNextHex()) {
               long nextTime = entity.getTimeToNextHex() - System.currentTimeMillis();
               createNode("timetonexthex", Server.getTimeFor(nextTime), document, newElement);
            }
         }

         createNode("location", entity.getLocationStatus(), document, newElement);
         if (!entity.isSource() && !entity.isCollectable()) {
            createNode("enemy", entity.getEnemyStatus(), document, newElement);
            createNode("strength", String.valueOf(entity.getAttack()), document, newElement);
            createNode("vitality", String.valueOf(entity.getVitality()), document, newElement);
            int colls = entity.countCollectables();
            createNode("collectibles", String.valueOf(colls), document, newElement);
            if (colls > 0) {
               createNode("collectiblename", entity.getCollectibleName(), document, newElement);
            }
         }

         if (entity.getCarrier() != null) {
            createNode("carrier", entity.getCarrier().getName(), document, newElement);
         }
      }

      return document;
   }

   public static void createNode(String element, String data, Document document, Element rootElement) {
      Element em = document.createElement(element);
      em.appendChild(document.createTextNode(data));
      rootElement.appendChild(em);
   }
}
