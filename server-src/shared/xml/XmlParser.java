package com.wurmonline.shared.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public final class XmlParser extends DefaultHandler {
   private final List<XmlNode> nodeStack = new ArrayList<>();
   private XmlNode rootNode = null;

   private XmlParser() {
   }

   public static XmlNode parse(InputStream in) throws IOException, SAXException {
      XMLReader xmlReader = XMLReaderFactory.createXMLReader();
      XmlParser xmlParser = new XmlParser();
      xmlReader.setContentHandler(xmlParser);
      xmlReader.parse(new InputSource(in));
      return xmlParser.rootNode;
   }

   @Override
   public void characters(char[] ch, int start, int length) throws SAXException {
      if (this.nodeStack.size() > 0) {
         this.nodeStack.get(this.nodeStack.size() - 1).setText(new String(ch, start, length));
      }
   }

   @Override
   public void endElement(String uri, String localName, String qName) throws SAXException {
      if (this.nodeStack.size() > 0) {
         this.nodeStack.remove(this.nodeStack.size() - 1);
      }
   }

   @Override
   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      XmlNode xmlNode = new XmlNode(localName, attributes);
      if (this.rootNode == null) {
         this.rootNode = xmlNode;
      }

      if (this.nodeStack.size() > 0) {
         this.nodeStack.get(this.nodeStack.size() - 1).addChild(xmlNode);
      }

      this.nodeStack.add(xmlNode);
   }

   static {
      try {
         XMLReaderFactory.createXMLReader();
      } catch (Exception var3) {
         System.out.println("Failed to load default xml reader.. attempting org.apache.crimson.parser.XMLReaderImpl");
         System.setProperty("org.xml.sax.driver", "org.apache.crimson.parser.XMLReaderImpl");

         try {
            XMLReaderFactory.createXMLReader();
         } catch (SAXException var2) {
            System.out.println("Failed to create XMLReader!!");
            var2.printStackTrace();
         }
      }
   }
}
