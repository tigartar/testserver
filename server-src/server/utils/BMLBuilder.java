package com.wurmonline.server.utils;

import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.players.Player;
import java.awt.Color;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BMLBuilder {
   private StringBuilder sb;
   private int openBrackets = 0;

   public static BMLBuilder createBMLUpdate(String... updates) {
      BMLBuilder builder = new BMLBuilder();
      builder.openBracket("update");

      for(String update : updates) {
         builder.addString(update);
      }

      builder.closeBracket();
      return builder;
   }

   public static BMLBuilder createBMLBorderPanel(
      @Nullable BMLBuilder north, @Nullable BMLBuilder west, @Nullable BMLBuilder center, @Nullable BMLBuilder east, @Nullable BMLBuilder south
   ) {
      return createBMLBorderPanel(north, west, center, east, south, 0, 0);
   }

   public static BMLBuilder createBMLBorderPanel(
      @Nullable BMLBuilder north,
      @Nullable BMLBuilder west,
      @Nullable BMLBuilder center,
      @Nullable BMLBuilder east,
      @Nullable BMLBuilder south,
      int width,
      int height
   ) {
      BMLBuilder builder = new BMLBuilder();
      builder.openBracket("border");
      if (width > 0 || height > 0) {
         builder.addString("size=\"" + width + "," + height + "\";");
      }

      if (north != null) {
         builder.addString(north.toString());
      } else {
         builder.addString("null;");
      }

      if (west != null) {
         builder.addString(west.toString());
      } else {
         builder.addString("null;");
      }

      if (center != null) {
         builder.addString(center.toString());
      } else {
         builder.addString("null;");
      }

      if (east != null) {
         builder.addString(east.toString());
      } else {
         builder.addString("null;");
      }

      if (south != null) {
         builder.addString(south.toString());
      } else {
         builder.addString("null;");
      }

      builder.closeBracket();
      return builder;
   }

   public static BMLBuilder createNormalWindow(String id, String question, BMLBuilder content) {
      BMLBuilder header = createCenteredNode(createGenericBuilder().addText(question, null, BMLBuilder.TextType.BOLD, null));
      BMLBuilder center = createScrollPanelNode(true, false);
      center.addString(createVertArrayNode(true).addPassthrough("id", id).addString(content.toString()).toString());
      return createBMLBorderPanel(header, null, center, null, null);
   }

   public static BMLBuilder createNoQuestionWindow(String id, BMLBuilder content) {
      BMLBuilder center = createScrollPanelNode(true, false);
      center.addString(createVertArrayNode(true).addPassthrough("id", id).addString(content.toString()).toString());
      return createBMLBorderPanel(null, null, center, null, null);
   }

   public static BMLBuilder createCenteredNode(BMLBuilder content) {
      return createAlignedNode("center", content);
   }

   public static BMLBuilder createLeftAlignedNode(BMLBuilder content) {
      return createAlignedNode("left", content);
   }

   public static BMLBuilder createRightAlignedNode(BMLBuilder content) {
      return createAlignedNode("right", content);
   }

   public static BMLBuilder createAlignedNode(String alignment, BMLBuilder content) {
      BMLBuilder builder = new BMLBuilder();
      builder.openBracket(alignment);
      builder.addString(content.toString());
      builder.closeBracket();
      return builder;
   }

   public static BMLBuilder createScrollPanelNode(boolean vertical, boolean horizontal) {
      return createScrollPanelNode(vertical, horizontal, 0, 0);
   }

   public static BMLBuilder createScrollPanelNode(boolean vertical, boolean horizontal, int width, int height) {
      BMLBuilder builder = new BMLBuilder();
      builder.openBracket("scroll");
      if (width > 0 || height > 0) {
         builder.addString("size=\"" + width + "," + height + "\";");
      }

      builder.addString("vertical=\"" + vertical + "\";");
      builder.addString("horizontal=\"" + horizontal + "\";");
      return builder;
   }

   public static BMLBuilder createVertArrayNode(boolean rescale) {
      BMLBuilder builder = new BMLBuilder();
      builder.openBracket("varray");
      builder.addString("rescale=\"" + rescale + "\";");
      return builder;
   }

   public static BMLBuilder createHorizArrayNode(boolean rescale) {
      BMLBuilder builder = new BMLBuilder();
      builder.openBracket("harray");
      builder.addString("rescale=\"" + rescale + "\";");
      return builder;
   }

   public static BMLBuilder createTable(int columns) {
      BMLBuilder builder = new BMLBuilder();
      builder.openBracket("table");
      builder.addString("cols=\"" + columns + "\";");
      return builder;
   }

   public static BMLBuilder createTreeList(String id, int columnCount, int height, boolean showHeader) {
      BMLBuilder builder = new BMLBuilder();
      builder.openBracket("tree");
      builder.addString("id=\"" + id + "\";");
      builder.addString("height=\"" + height + "\";");
      builder.addString("cols=\"" + columnCount + "\";");
      builder.addString("showheader=\"" + showHeader + "\";");
      return builder;
   }

   public static BMLBuilder createGenericBuilder() {
      return new BMLBuilder();
   }

   public static String createHeader(String text) {
      return createGenericBuilder().addHeader(text).toString();
   }

   public static String createLabel(String text) {
      return createGenericBuilder().addLabel(text).toString();
   }

   public static String createButton(String id, String text) {
      return createButton(id, text, true);
   }

   public static String createButton(String id, String text, boolean enabled) {
      return createGenericBuilder().addButton(id, text, enabled).toString();
   }

   public static String createButton(String id, String text, int width, int height, boolean enabled) {
      return createGenericBuilder().addButton(id, text, width, height, enabled).toString();
   }

   public static String createInput(String id, @Nullable String text) {
      return createGenericBuilder().addInput(id, text, 0, 0).toString();
   }

   public static String createText(String text) {
      return createGenericBuilder().addText(text).toString();
   }

   public static String createCheckbox(String id, String text, boolean selected) {
      return createGenericBuilder().addCheckbox(id, text, selected).toString();
   }

   public static String createDropdown(String id, String defaultOption, String... options) {
      return createGenericBuilder().addDropdown(id, defaultOption, options).toString();
   }

   public static String createRadioButton(String id, String group, String text, boolean selected) {
      return createGenericBuilder().addRadioButton(id, group, text, selected).toString();
   }

   public static String createImage(String imageSrc, int width, int height) {
      return createGenericBuilder().addImage(imageSrc, width, height).toString();
   }

   public static BMLBuilder.TreeListRowData createTLRD(String text) {
      return new BMLBuilder.TreeListRowData(text);
   }

   public static BMLBuilder.TreeListRowData createTLRD(String id, String text, boolean checkbox, boolean selected) {
      return new BMLBuilder.TreeListRowData(id, text, checkbox, selected);
   }

   private BMLBuilder() {
      this.sb = new StringBuilder();
   }

   public BMLBuilder addHeader(String text) {
      return this.addHeader(text, null);
   }

   public BMLBuilder addHeader(String text, @Nullable Color color) {
      this.openBracket("header");
      if (color != null) {
         this.addColor("color", color);
      }

      this.sb.append("text=\"" + text + "\";");
      this.closeBracket();
      return this;
   }

   public BMLBuilder addText(String text) {
      return this.addText(text, null, null, null);
   }

   public BMLBuilder addText(String text, @Nullable String hover, @Nullable BMLBuilder.TextType type, @Nullable Color color) {
      return this.addText(text, hover, type, color, 0, 0);
   }

   public BMLBuilder addText(String text, @Nullable String hover, @Nullable BMLBuilder.TextType type, @Nullable Color color, int width, int height) {
      this.openBracket("text");
      if (width > 0 || height > 0) {
         this.sb.append("size=\"" + width + "," + height + "\";");
      }

      if (type != null && type != BMLBuilder.TextType.NONE) {
         this.sb.append("type=\"" + type.typeString + "\";");
      }

      if (hover != null) {
         this.sb.append("hover=\"" + hover + "\";");
      }

      if (color != null) {
         this.addColor("color", color);
      }

      this.sb.append("text=\"" + text + "\";");
      this.closeBracket();
      return this;
   }

   public BMLBuilder addLabel(String text) {
      return this.addLabel(text, null, null, null);
   }

   public BMLBuilder addLabel(String text, @Nullable String hover, @Nullable BMLBuilder.TextType type, @Nullable Color color) {
      return this.addLabel(text, hover, type, color, 0, 0);
   }

   public BMLBuilder addLabel(String text, @Nullable String hover, @Nullable BMLBuilder.TextType type, @Nullable Color color, int width, int height) {
      this.openBracket("label");
      if (width > 0 || height > 0) {
         this.sb.append("size=\"" + width + "," + height + "\";");
      }

      if (type != null && type != BMLBuilder.TextType.NONE) {
         this.sb.append("type=\"" + type.typeString + "\";");
      }

      if (hover != null) {
         this.sb.append("hover=\"" + hover + "\";");
      }

      if (color != null) {
         this.addColor("color", color);
      }

      this.sb.append("text=\"" + text + "\";");
      this.closeBracket();
      return this;
   }

   public BMLBuilder addInput(String id, @Nullable String text, int maxChars, int maxLines) {
      return this.addInput(id, null, true, text, maxChars, maxLines, null, null, null);
   }

   public BMLBuilder addInput(
      String id,
      @Nullable String onEnter,
      boolean enabled,
      @Nullable String text,
      int maxChars,
      int maxLines,
      @Nullable Color color,
      @Nullable Color bgColor,
      @Nullable String bgTexture
   ) {
      return this.addInput(id, onEnter, enabled, text, maxChars, maxLines, color, bgColor, bgTexture, 0, 0);
   }

   public BMLBuilder addInput(
      String id,
      @Nullable String onEnter,
      boolean enabled,
      @Nullable String text,
      int maxChars,
      int maxLines,
      @Nullable Color color,
      @Nullable Color bgColor,
      @Nullable String bgTexture,
      int width,
      int height
   ) {
      this.openBracket("input");
      if (width > 0 || height > 0) {
         this.sb.append("size=\"" + width + "," + height + "\";");
      }

      this.sb.append("id=\"" + id + "\";");
      if (onEnter != null) {
         this.sb.append("onenter=\"" + onEnter + "\";");
      }

      this.sb.append("enabled=\"" + enabled + "\";");
      if (text != null) {
         this.sb.append("text=\"" + text + "\";");
      }

      if (maxChars > 0) {
         this.sb.append("maxchars=\"" + maxChars + "\";");
      }

      if (maxLines > 0) {
         this.sb.append("maxlines=\"" + maxLines + "\";");
      }

      if (color != null) {
         this.addColor("color", color);
      }

      if (bgColor != null) {
         this.addColor("bgcolor", bgColor);
      }

      if (bgTexture != null) {
         this.sb.append("bgtexture=\"" + bgTexture + "\";");
      }

      this.closeBracket();
      return this;
   }

   public BMLBuilder addPassthrough(String id, String text) {
      this.openBracket("passthrough");
      this.sb.append("id=\"" + id + "\";");
      this.sb.append("text=\"" + text + "\";");
      this.closeBracket();
      return this;
   }

   public BMLBuilder addDropdown(String id, String defaultOption, String... options) {
      return this.addDropdown(id, defaultOption, 0, 0, options);
   }

   public BMLBuilder addDropdown(String id, String defaultOption, int width, int height, String... options) {
      this.openBracket("dropdown");
      if (width > 0 || height > 0) {
         this.sb.append("size=\"" + width + "," + height + "\";");
      }

      this.sb.append("id=\"" + id + "\";");
      this.sb.append("default=\"" + defaultOption + "\";");
      this.sb.append("options=\"");
      int count = 0;

      for(String s : options) {
         this.sb.append(s);
         if (++count < options.length) {
            this.sb.append(",");
         }
      }

      this.sb.append("\";");
      this.closeBracket();
      return this;
   }

   public BMLBuilder addButton(String id, String text) {
      return this.addButton(id, text, null, null, null, false);
   }

   public BMLBuilder addButton(String id, String text, boolean enabled) {
      return this.addButton(id, text, null, null, null, false, 0, 0, enabled);
   }

   public BMLBuilder addButton(String id, String text, int width, int height, boolean enabled) {
      return this.addButton(id, text, null, null, null, false, width, height, enabled);
   }

   public BMLBuilder addButton(
      String id, String text, @Nullable String confirmQuestion, @Nullable String confirmString, @Nullable String hover, boolean isDefaultButton
   ) {
      return this.addButton(id, text, confirmQuestion, confirmString, hover, isDefaultButton, 0, 0, true);
   }

   public BMLBuilder addButton(
      String id,
      String text,
      @Nullable String confirmQuestion,
      @Nullable String confirmString,
      @Nullable String hover,
      boolean isDefaultButton,
      int width,
      int height,
      boolean enabled
   ) {
      this.openBracket("button");
      if (width > 0 || height > 0) {
         this.sb.append("size=\"" + width + "," + height + "\";");
      }

      this.sb.append("id=\"" + id + "\";");
      this.sb.append("text=\"" + text + "\";");
      if (confirmQuestion != null) {
         this.sb.append("question=\"" + confirmQuestion + "\";");
      }

      if (confirmString != null) {
         this.sb.append("confirm=\"" + confirmString + "\";");
      }

      if (hover != null) {
         this.sb.append("hover=\"" + hover + "\";");
      }

      this.sb.append("default=\"" + isDefaultButton + "\";");
      this.sb.append("enabled=\"" + enabled + "\";");
      this.closeBracket();
      return this;
   }

   public BMLBuilder addRadioButton(String id, String group, String text, boolean selected) {
      return this.addRadioButton(id, group, text, null, selected, true, false);
   }

   public BMLBuilder addRadioButton(String id, String group, String text, @Nullable String hover, boolean selected, boolean enabled, boolean hidden) {
      return this.addRadioButton(id, group, text, hover, selected, enabled, hidden, 0, 0);
   }

   public BMLBuilder addRadioButton(
      String id, String group, String text, @Nullable String hover, boolean selected, boolean enabled, boolean hidden, int width, int height
   ) {
      this.openBracket("radio");
      if (width > 0 || height > 0) {
         this.sb.append("size=\"" + width + "," + height + "\";");
      }

      this.sb.append("id=\"" + id + "\";");
      this.sb.append("group=\"" + group + "\";");
      this.sb.append("text=\"" + text + "\";");
      if (hover != null) {
         this.sb.append("hover=\"" + hover + "\";");
      }

      this.sb.append("selected=\"" + selected + "\";");
      this.sb.append("enabled=\"" + enabled + "\";");
      this.sb.append("hidden=\"" + hidden + "\";");
      this.closeBracket();
      return this;
   }

   public BMLBuilder addCheckbox(String id, String text, boolean selected) {
      return this.addCheckbox(id, text, null, null, null, null, null, selected, true, null);
   }

   public BMLBuilder addCheckbox(
      String id,
      String text,
      @Nullable String confirmQuestion,
      @Nullable String confirmString,
      @Nullable String unconfirmQuestion,
      @Nullable String unconfirmString,
      @Nullable String hover,
      boolean selected,
      boolean enabled,
      @Nullable Color color
   ) {
      return this.addCheckbox(id, text, confirmQuestion, confirmString, unconfirmQuestion, unconfirmString, hover, selected, enabled, color, 0, 0);
   }

   public BMLBuilder addCheckbox(
      String id,
      String text,
      @Nullable String confirmQuestion,
      @Nullable String confirmString,
      @Nullable String unconfirmQuestion,
      @Nullable String unconfirmString,
      @Nullable String hover,
      boolean selected,
      boolean enabled,
      @Nullable Color color,
      int width,
      int height
   ) {
      this.openBracket("checkbox");
      if (width > 0 || height > 0) {
         this.sb.append("size=\"" + width + "," + height + "\";");
      }

      this.sb.append("id=\"" + id + "\";");
      this.sb.append("text=\"" + text + "\";");
      if (confirmQuestion != null) {
         this.sb.append("question=\"" + confirmQuestion + "\";");
      }

      if (confirmString != null) {
         this.sb.append("confirm=\"" + confirmString + "\";");
      }

      if (unconfirmQuestion != null) {
         this.sb.append("unquestion=\"" + unconfirmQuestion + "\";");
      }

      if (unconfirmString != null) {
         this.sb.append("unconfirm=\"" + unconfirmString + "\";");
      }

      if (hover != null) {
         this.sb.append("hover=\"" + hover + "\";");
      }

      this.sb.append("selected=\"" + selected + "\";");
      this.sb.append("enabled=\"" + enabled + "\";");
      if (color != null) {
         this.addColor("color", color);
      }

      this.closeBracket();
      return this;
   }

   public BMLBuilder addImage(String imageSrc, int width, int height) {
      return this.addImage(imageSrc, null, width, height);
   }

   public BMLBuilder addImage(String imageSrc, @Nullable String hoverText, int width, int height) {
      this.openBracket("image");
      if (width > 0 || height > 0) {
         this.sb.append("size=\"" + width + "," + height + "\";");
      }

      this.sb.append("src=\"" + imageSrc + "\";");
      if (hoverText != null) {
         this.sb.append("text=\"" + hoverText + "\";");
      }

      this.closeBracket();
      return this;
   }

   public BMLBuilder addTreeListColumn(String text) {
      return this.addTreeListColumn(text, 0);
   }

   public BMLBuilder addTreeListColumn(String text, int width) {
      this.openBracket("col");
      this.sb.append("text=\"" + text + "\";");
      if (width > 0) {
         this.sb.append("width=\"" + width + "\";");
      }

      this.closeBracket();
      return this;
   }

   public BMLBuilder addTreeListRow(String id, String name, BMLBuilder.TreeListRowData... colData) {
      return this.addTreeListRow(id, name, null, 0, 0, colData);
   }

   public BMLBuilder addTreeListRow(String id, String name, @Nullable String hover, int rarity, int children, BMLBuilder.TreeListRowData... colData) {
      this.openBracket("row");
      this.sb.append("id=\"" + id + "\";");
      this.sb.append("name=\"" + name + "\";");
      if (hover != null) {
         this.sb.append("hover=\"" + hover + "\";");
      }

      this.sb.append("rarity=\"" + rarity + "\";");
      this.sb.append("children=\"" + children + "\";");

      for(BMLBuilder.TreeListRowData col : colData) {
         this.openBracket("col");
         this.sb.append("text=\"" + col.text + "\";");
         if (col.id != null) {
            this.sb.append("id=\"" + col.id + "\";");
         }

         if (col.checkbox) {
            this.sb.append("checkbox=\"" + col.checkbox + "\";");
         }

         if (col.selected) {
            this.sb.append("selected=\"" + col.selected + "\";");
         }

         this.closeBracket();
      }

      this.closeBracket();
      return this;
   }

   public void addColor(String colorType, @Nonnull Color color) {
      this.sb.append(colorType + "=\"" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + "\";");
   }

   public void openBracket(String type) {
      this.sb.append(type);
      this.sb.append("{");
      ++this.openBrackets;
   }

   public void closeBracket() {
      this.sb.append("}");
      --this.openBrackets;
   }

   public BMLBuilder prependString(String s) {
      this.sb.insert(0, s);
      return this;
   }

   public BMLBuilder addString(String s) {
      this.sb.append(s);
      return this;
   }

   @Override
   public String toString() {
      for(int i = 0; i < this.openBrackets; ++i) {
         this.sb.append("}");
      }

      return this.sb.toString();
   }

   public BMLBuilder maybeAddSkipButton() {
      return this.maybeAddSkipButton(null, false);
   }

   public BMLBuilder maybeAddSkipButton(Player p, boolean close) {
      if (!close) {
         if (Servers.localServer.testServer || Server.getInstance().isPS()) {
            return this.addText("", null, null, null, 35, 0)
               .addButton("skip", "Skip Stage", " ", "Are you sure you want to skip this stage of the tutorial?", null, false, 80, 20, true);
         }
      } else {
         if (p == null) {
            return this;
         }

         if (Servers.localServer.testServer
            || Servers.isThisAPvpServer()
            || p.hasFlag(42)
            || System.currentTimeMillis() - p.getSaveFile().creationDate > 14515200000L) {
            return this.addText("", null, null, null, 35, 0)
               .addButton("close", "Close Tutorial", " ", "Are you sure you want to skip the tutorial?", null, false, 80, 20, true);
         }
      }

      return this;
   }

   public static enum TextType {
      NONE(""),
      BOLD("bold"),
      ITALIC("italic"),
      BOLDITALIC("bolditalic");

      private final String typeString;

      private TextType(String typeString) {
         this.typeString = typeString;
      }
   }

   public static class TreeListRowData {
      private String id;
      private String text;
      private boolean checkbox = false;
      private boolean selected = false;

      public TreeListRowData(String text) {
         this(null, text, false, false);
      }

      public TreeListRowData(String id, String text, boolean checkbox, boolean selected) {
         this.id = id;
         this.text = text;
         this.checkbox = checkbox;
         this.selected = selected;
      }
   }
}
