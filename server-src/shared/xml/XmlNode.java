package com.wurmonline.shared.xml;

import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;

public class XmlNode {
   private final String name;
   private final Attributes attributes;
   private final List<XmlNode> children = new ArrayList<>();
   private String text;

   public XmlNode(String localName, Attributes attributes) {
      this.name = localName;
      this.attributes = attributes;
   }

   public void addChild(XmlNode child) {
      this.children.add(child);
   }

   public void setText(String text) {
      this.text = text;
   }

   public List<XmlNode> getAll(String aName) {
      List<XmlNode> list = new ArrayList<>();

      for(XmlNode xmlNode : this.children) {
         if (xmlNode.name.equals(aName)) {
            list.add(xmlNode);
         }
      }

      return list;
   }

   public XmlNode getFirst(String aName) {
      for(XmlNode xmlNode : this.children) {
         if (xmlNode.name.equals(aName)) {
            return xmlNode;
         }
      }

      return null;
   }

   public String getAttribute(String aName) {
      return this.attributes.getValue(aName);
   }

   public Attributes getAttributes() {
      return this.attributes;
   }

   public List<XmlNode> getChildren() {
      return this.children;
   }

   public String getName() {
      return this.name;
   }

   public String getText() {
      return this.text;
   }

   public String getValue(String string) {
      XmlNode node = this.getFirst(string);
      return node == null ? null : node.getText();
   }
}
