package com.wurmonline.server.items;

import com.wurmonline.server.Features;
import java.util.logging.Logger;

public final class CreationEntryCreator {
   private static final Logger logger = Logger.getLogger(CreationEntryCreator.class.getName());
   private static boolean entriesCreated = false;

   private CreationEntryCreator() {
   }

   public static void createCreationEntries() {
      long start = System.nanoTime();
      createSimpleEntry(1005, 24, 9, 22, false, true, 100.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(1005, 7, 9, 860, false, true, 15.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(1005, 8, 9, 23, false, true, 100.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(1005, 685, 688, 691, false, true, 100.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(1005, 8, 688, 691, false, true, 100.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(1005, 8, 9, 711, false, true, 100.0F, false, false, CreationCategories.WEAPONS);
      createSimpleEntry(1031, 8, 9, 459, false, true, 100.0F, false, false, CreationCategories.BOWS);
      createSimpleEntry(1031, 8, 9, 461, false, true, 100.0F, false, false, CreationCategories.BOWS);
      createSimpleEntry(1031, 8, 9, 460, false, true, 100.0F, false, false, CreationCategories.BOWS);
      createSimpleEntry(1031, 685, 9, 459, false, true, 200.0F, false, false, CreationCategories.BOWS);
      createSimpleEntry(1031, 685, 9, 461, false, true, 200.0F, false, false, CreationCategories.BOWS);
      createSimpleEntry(1031, 685, 9, 460, false, true, 200.0F, false, false, CreationCategories.BOWS);
      createSimpleEntry(1007, 8, 9, 36, false, true, 0.0F, false, false, CreationCategories.KINDLINGS);
      createSimpleEntry(1007, 685, 9, 36, false, true, 0.0F, false, false, CreationCategories.KINDLINGS);
      createSimpleEntry(1007, 24, 9, 36, false, true, 0.0F, false, false, CreationCategories.KINDLINGS);
      createSimpleEntry(1007, 87, 9, 36, false, true, 0.0F, false, false, CreationCategories.KINDLINGS);
      createSimpleEntry(1007, 3, 9, 36, false, true, 0.0F, false, false, CreationCategories.KINDLINGS);
      createSimpleEntry(1007, 90, 9, 36, false, true, 0.0F, false, false, CreationCategories.KINDLINGS);
      createSimpleEntry(1007, 8, 169, 36, false, true, 0.0F, false, false, CreationCategories.KINDLINGS);
      createSimpleEntry(1007, 8, 22, 36, false, true, 0.0F, false, false, CreationCategories.KINDLINGS);
      createSimpleEntry(1007, 685, 169, 36, false, true, 0.0F, false, false, CreationCategories.KINDLINGS);
      createSimpleEntry(1007, 24, 169, 36, false, true, 0.0F, false, false, CreationCategories.KINDLINGS);
      createSimpleEntry(1007, 87, 169, 36, false, true, 0.0F, false, false, CreationCategories.KINDLINGS);
      createSimpleEntry(1007, 3, 169, 36, false, true, 0.0F, false, false, CreationCategories.KINDLINGS);
      createSimpleEntry(1007, 90, 169, 36, false, true, 0.0F, false, false, CreationCategories.KINDLINGS);
      createSimpleEntry(1007, 7, 9, 36, false, true, 0.0F, false, false, CreationCategories.KINDLINGS);
      createSimpleEntry(1007, 7, 169, 36, false, true, 0.0F, false, false, CreationCategories.KINDLINGS);
      createSimpleEntry(1007, 24, 22, 790, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(1010, 143, 36, 37, false, true, 0.0F, false, true, CreationCategories.FIRE);
      createSimpleEntry(1010, 169, 36, 37, true, true, 0.0F, false, true, CreationCategories.FIRE);
      createSimpleEntry(1011, 14, 130, 181, false, true, 0.0F, false, false, CreationCategories.POTTERY);
      createSimpleEntry(1011, 14, 130, 182, false, true, 0.0F, false, false, CreationCategories.POTTERY);
      createSimpleEntry(1011, 14, 130, 183, false, true, 0.0F, false, false, CreationCategories.POTTERY);
      createSimpleEntry(1011, 14, 130, 769, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(1011, 14, 130, 777, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(1011, 14, 130, 789, false, true, 0.0F, false, false, CreationCategories.TOOLS);
      if (Features.Feature.AMPHORA.isEnabled()) {
         createSimpleEntry(1011, 14, 130, 1019, false, true, 0.0F, false, false, CreationCategories.POTTERY);
         createSimpleEntry(1011, 14, 130, 1021, false, true, 0.0F, false, false, CreationCategories.POTTERY);
      }

      if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
         createMetallicEntries(10015, 63, 46, 64, false, true, 0.0F, false, false, CreationCategories.TOOLS);
         createMetallicEntries(10015, 62, 46, 64, false, true, 0.0F, false, false, CreationCategories.TOOLS);
         createMetallicEntries(10010, 185, 46, 148, false, true, 0.0F, false, false, CreationCategories.BLADES);
         createMetallicEntries(10010, 64, 46, 147, false, true, 0.0F, false, false, CreationCategories.BLADES);
         createMetallicEntries(10010, 185, 46, 149, false, true, 0.0F, false, false, CreationCategories.BLADES);
         createMetallicEntries(10010, 185, 46, 269, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createMetallicEntries(10010, 185, 46, 270, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createMetallicEntries(10011, 64, 46, 89, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createMetallicEntries(10011, 64, 46, 523, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createMetallicEntries(10011, 185, 46, 91, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createMetallicEntries(10011, 185, 46, 88, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createMetallicEntries(10011, 185, 46, 293, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createMetallicEntries(10011, 185, 46, 295, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createMetallicEntries(10011, 185, 46, 294, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createMetallicEntries(10011, 185, 46, 708, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
      } else {
         createSimpleEntry(10015, 63, 46, 64, false, true, 0.0F, false, false, CreationCategories.TOOLS);
         createSimpleEntry(10015, 62, 46, 64, false, true, 0.0F, false, false, CreationCategories.TOOLS);
         createSimpleEntry(10010, 185, 46, 148, false, true, 0.0F, false, false, CreationCategories.BLADES);
         createSimpleEntry(10010, 64, 46, 147, false, true, 0.0F, false, false, CreationCategories.BLADES);
         createSimpleEntry(10010, 185, 694, 148, false, true, 0.0F, false, false, CreationCategories.BLADES);
         createSimpleEntry(10010, 64, 694, 147, false, true, 0.0F, false, false, CreationCategories.BLADES);
         createSimpleEntry(10010, 185, 837, 148, false, true, 0.0F, false, false, CreationCategories.BLADES);
         createSimpleEntry(10010, 64, 837, 147, false, true, 0.0F, false, false, CreationCategories.BLADES);
         createSimpleEntry(10010, 185, 698, 148, false, true, 0.0F, false, false, CreationCategories.BLADES);
         createSimpleEntry(10010, 64, 698, 147, false, true, 0.0F, false, false, CreationCategories.BLADES);
         createSimpleEntry(10010, 185, 205, 148, false, true, 0.0F, false, false, CreationCategories.BLADES);
         createSimpleEntry(10010, 64, 205, 147, false, true, 0.0F, false, false, CreationCategories.BLADES);
         createSimpleEntry(10010, 185, 694, 149, false, true, 0.0F, false, false, CreationCategories.BLADES);
         createSimpleEntry(10010, 185, 837, 149, false, true, 0.0F, false, false, CreationCategories.BLADES);
         createSimpleEntry(10010, 185, 698, 149, false, true, 0.0F, false, false, CreationCategories.BLADES);
         createSimpleEntry(10010, 185, 46, 149, false, true, 0.0F, false, false, CreationCategories.BLADES);
         createSimpleEntry(10010, 185, 205, 149, false, true, 0.0F, false, false, CreationCategories.BLADES);
         createSimpleEntry(10010, 185, 46, 269, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10010, 185, 46, 270, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10010, 185, 698, 270, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10010, 185, 205, 270, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10010, 185, 694, 270, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10010, 185, 837, 270, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10011, 64, 46, 89, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 46, 523, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10011, 185, 46, 91, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 185, 46, 88, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 185, 46, 293, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 185, 46, 295, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 185, 46, 294, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 185, 46, 708, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 694, 89, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 694, 523, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10011, 185, 694, 91, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 185, 694, 88, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 185, 694, 293, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 185, 694, 295, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 185, 694, 294, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 185, 694, 708, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 837, 89, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 837, 523, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10011, 185, 837, 91, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 185, 837, 88, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 185, 837, 293, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 185, 837, 295, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 185, 837, 294, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 185, 837, 708, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 205, 89, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 205, 523, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10011, 185, 205, 91, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 185, 205, 88, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 185, 205, 293, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 185, 205, 295, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 185, 205, 294, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 185, 205, 708, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 698, 89, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 698, 523, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10011, 185, 698, 91, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 185, 698, 88, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 185, 698, 293, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 185, 698, 295, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 185, 698, 294, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 185, 698, 708, false, true, 0.0F, false, false, CreationCategories.WEAPON_HEADS);
      }

      createSimpleEntry(1005, 8, 23, 99, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(1005, 8, 23, 862, false, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(1005, 8, 23, 561, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(1005, 8, 23, 397, false, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(1005, 8, 23, 396, false, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(1005, 685, 23, 99, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(1005, 685, 23, 561, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(1005, 685, 23, 397, false, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(1005, 685, 23, 396, false, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(1032, 685, 23, 454, false, true, 0.0F, false, false, CreationCategories.FLETCHING);
      createSimpleEntry(1032, 8, 23, 454, false, true, 0.0F, false, false, CreationCategories.FLETCHING);
      createSimpleEntry(1032, 451, 454, 455, true, true, 0.0F, false, false, CreationCategories.FLETCHING);
      createSimpleEntry(1032, 452, 454, 456, true, true, 0.0F, false, false, CreationCategories.FLETCHING);
      createSimpleEntry(1016, 99, 148, 21, true, true, 0.0F, false, false, CreationCategories.WEAPONS);
      createSimpleEntry(1016, 99, 147, 80, true, true, 0.0F, false, false, CreationCategories.WEAPONS);
      createSimpleEntry(1016, 99, 149, 81, true, true, 0.0F, false, false, CreationCategories.WEAPONS);
      createSimpleEntry(1016, 99, 269, 267, true, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(1016, 709, 711, 705, true, true, 0.0F, false, false, CreationCategories.WEAPONS);
      createSimpleEntry(1016, 710, 709, 707, true, true, 0.0F, false, false, CreationCategories.WEAPONS);
      createSimpleEntry(1016, 711, 708, 706, true, true, 0.0F, false, false, CreationCategories.WEAPONS);
      createSimpleEntry(1016, 23, 89, 3, true, true, 0.0F, false, false, CreationCategories.WEAPONS);
      createSimpleEntry(1016, 23, 91, 90, true, true, 0.0F, false, false, CreationCategories.WEAPONS);
      createSimpleEntry(1016, 23, 88, 87, true, true, 0.0F, false, false, CreationCategories.WEAPONS);
      createSimpleEntry(1016, 23, 270, 268, true, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(1016, 23, 293, 290, true, true, 0.0F, false, false, CreationCategories.WEAPONS);
      createSimpleEntry(1016, 23, 295, 292, true, true, 0.0F, false, false, CreationCategories.WEAPONS);
      createSimpleEntry(1016, 23, 294, 291, true, true, 0.0F, false, false, CreationCategories.WEAPONS);
      createSimpleEntry(1005, 8, 9, 139, false, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(1005, 685, 9, 139, false, true, 0.0F, false, false, CreationCategories.TOOLS);
      if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
         createMetallicEntries(1016, 185, 205, 710, false, true, 10.0F, false, false, CreationCategories.WEAPONS);
         createMetallicEntries(10015, 64, 46, 215, false, true, 400.0F, false, false, CreationCategories.TOOLS);
         createMetallicEntries(10015, 64, 46, 259, false, true, 10.0F, false, false, CreationCategories.COOKING_UTENSILS);
         createMetallicEntries(10015, 64, 46, 257, false, true, 10.0F, false, false, CreationCategories.COOKING_UTENSILS);
         createMetallicEntries(10015, 64, 46, 258, false, true, 10.0F, false, false, CreationCategories.COOKING_UTENSILS);
      } else {
         createSimpleEntry(1016, 185, 205, 710, false, true, 10.0F, false, false, CreationCategories.WEAPONS);
         createSimpleEntry(10015, 64, 46, 215, false, true, 400.0F, false, false, CreationCategories.TOOLS);
         createSimpleEntry(10015, 64, 46, 259, false, true, 10.0F, false, false, CreationCategories.COOKING_UTENSILS);
         createSimpleEntry(10015, 64, 46, 257, false, true, 10.0F, false, false, CreationCategories.COOKING_UTENSILS);
         createSimpleEntry(10015, 64, 46, 258, false, true, 10.0F, false, false, CreationCategories.COOKING_UTENSILS);
         createSimpleEntry(10015, 64, 45, 259, false, true, 10.0F, false, false, CreationCategories.COOKING_UTENSILS);
         createSimpleEntry(10015, 64, 45, 257, false, true, 10.0F, false, false, CreationCategories.COOKING_UTENSILS);
         createSimpleEntry(10015, 64, 45, 258, false, true, 10.0F, false, false, CreationCategories.COOKING_UTENSILS);
         createSimpleEntry(10015, 64, 47, 216, false, true, 400.0F, false, false, CreationCategories.TOOLS);
      }

      createSimpleEntry(10015, 185, 46, 681, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10015, 446, 205, 143, true, true, 10.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(10015, 64, 47, 772, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10015, 64, 46, 773, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10015, 64, 220, 1298, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10015, 64, 49, 1299, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10015, 64, 205, 597, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10015, 64, 44, 599, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10015, 64, 45, 598, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10015, 185, 47, 838, false, true, 0.0F, false, false, 20, 30.0, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10091, 128, 169, 1270, false, true, 0.0F, true, false, CreationCategories.WRITING);
      createSimpleEntry(10091, 747, 745, 748, false, true, 0.0F, false, false, CreationCategories.WRITING);
      createSimpleEntry(10091, 747, 1270, 1272, false, true, 0.0F, false, false, CreationCategories.WRITING);
      createSimpleEntry(10092, 774, 743, 756, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10092, 774, 620, 756, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10007, 8, 743, 749, false, true, 0.0F, false, false, CreationCategories.WRITING);
      createSimpleEntry(10016, 216, 213, 113, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10016, 215, 213, 113, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10016, 216, 213, 109, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10016, 215, 213, 109, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10016, 216, 213, 1427, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10016, 215, 213, 1427, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10016, 215, 213, 704, false, true, 0.0F, false, false, CreationCategories.ANIMAL_EQUIPMENT);
      createSimpleEntry(10016, 216, 213, 704, false, true, 0.0F, false, false, CreationCategories.ANIMAL_EQUIPMENT);
      createSimpleEntry(10016, 215, 213, 1425, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10016, 216, 213, 1425, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10016, 216, 213, 486, false, true, 0.0F, false, false, CreationCategories.RESOURCES);
      createSimpleEntry(10016, 215, 213, 486, false, true, 0.0F, false, false, CreationCategories.RESOURCES);
      createSimpleEntry(10016, 216, 213, 110, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10016, 215, 213, 110, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10016, 216, 213, 114, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10016, 215, 213, 114, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10016, 216, 213, 1426, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10016, 215, 213, 1426, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10016, 215, 213, 2, false, true, 0.0F, false, false, CreationCategories.BAGS);
      createSimpleEntry(10016, 215, 213, 555, false, true, 0.0F, false, false, CreationCategories.SAILS);
      createSimpleEntry(10016, 215, 213, 591, false, true, 0.0F, false, false, CreationCategories.SAILS);
      createSimpleEntry(10016, 215, 213, 554, false, true, 0.0F, false, false, CreationCategories.SAILS);
      createSimpleEntry(10016, 216, 213, 555, false, true, 0.0F, false, false, CreationCategories.SAILS);
      createSimpleEntry(10016, 216, 213, 591, false, true, 0.0F, false, false, CreationCategories.SAILS);
      createSimpleEntry(10016, 216, 213, 554, false, true, 0.0F, false, false, CreationCategories.SAILS);
      createSimpleEntry(10016, 215, 213, 831, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10016, 216, 213, 831, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
         createMetallicEntries(10015, 185, 46, 627, false, true, 0.0F, false, false, CreationCategories.ANIMAL_EQUIPMENT_PART);
         createMetallicEntries(10015, 185, 46, 623, false, true, 0.0F, false, false, CreationCategories.ANIMAL_EQUIPMENT);
         createMetallicEntries(10015, 64, 46, 127, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createMetallicEntries(10010, 64, 46, 154, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createMetallicEntries(10010, 64, 46, 389, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createMetallicEntries(10010, 64, 46, 494, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createMetallicEntries(10010, 64, 46, 709, false, true, 0.0F, false, false, CreationCategories.BLADES);
         createMetallicEntries(10010, 64, 46, 391, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createMetallicEntries(10010, 64, 46, 393, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createMetallicEntries(10010, 64, 46, 395, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createMetallicEntries(10010, 64, 46, 125, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createMetallicEntries(10010, 64, 46, 126, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createMetallicEntries(10015, 185, 46, 75, false, true, 0.0F, false, false, CreationCategories.COOKING_UTENSILS);
         createMetallicEntries(10015, 185, 46, 351, false, true, 0.0F, false, false, CreationCategories.COOKING_UTENSILS);
         createMetallicEntries(10015, 64, 46, 734, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createMetallicEntries(10015, 64, 46, 720, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createMetallicEntries(10015, 185, 46, 721, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createMetallicEntries(10015, 64, 46, 735, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createMetallicEntries(10015, 185, 46, 350, false, true, 0.0F, false, false, CreationCategories.COOKING_UTENSILS);
      } else {
         createSimpleEntry(10015, 185, 46, 627, false, true, 0.0F, false, false, CreationCategories.ANIMAL_EQUIPMENT_PART);
         createSimpleEntry(10015, 185, 46, 623, false, true, 0.0F, false, false, CreationCategories.ANIMAL_EQUIPMENT);
         createSimpleEntry(10015, 185, 205, 627, false, true, 0.0F, false, false, CreationCategories.ANIMAL_EQUIPMENT_PART);
         createSimpleEntry(10015, 185, 205, 623, false, true, 0.0F, false, false, CreationCategories.ANIMAL_EQUIPMENT);
         createSimpleEntry(10043, 185, 44, 623, false, true, 0.0F, false, false, CreationCategories.ANIMAL_EQUIPMENT);
         createSimpleEntry(10043, 185, 45, 623, false, true, 0.0F, false, false, CreationCategories.ANIMAL_EQUIPMENT);
         createSimpleEntry(10015, 64, 46, 127, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10010, 64, 46, 154, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10010, 64, 46, 389, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10010, 64, 46, 494, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10010, 64, 46, 709, false, true, 0.0F, false, false, CreationCategories.BLADES);
         createSimpleEntry(10010, 64, 205, 709, false, true, 0.0F, false, false, CreationCategories.BLADES);
         createSimpleEntry(10010, 64, 698, 709, false, true, 0.0F, false, false, CreationCategories.BLADES);
         createSimpleEntry(10010, 64, 694, 709, false, true, 0.0F, false, false, CreationCategories.BLADES);
         createSimpleEntry(10010, 64, 837, 709, false, true, 0.0F, false, false, CreationCategories.BLADES);
         createSimpleEntry(10010, 64, 46, 391, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10010, 64, 46, 393, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10010, 64, 205, 393, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10010, 64, 694, 393, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10010, 64, 837, 393, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10010, 64, 698, 393, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10010, 64, 46, 395, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10015, 185, 46, 75, false, true, 0.0F, false, false, CreationCategories.COOKING_UTENSILS);
         createSimpleEntry(10015, 185, 46, 351, false, true, 0.0F, false, false, CreationCategories.COOKING_UTENSILS);
         createSimpleEntry(10015, 64, 46, 734, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10015, 64, 221, 720, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10015, 185, 223, 721, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10015, 64, 46, 735, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10015, 185, 46, 350, false, true, 0.0F, false, false, CreationCategories.COOKING_UTENSILS);
         createSimpleEntry(10010, 64, 46, 125, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10010, 64, 46, 126, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10010, 64, 205, 126, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10010, 64, 698, 126, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10010, 64, 694, 126, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10010, 64, 837, 126, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
      }

      createSimpleEntry(10015, 99, 393, 392, true, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(10015, 395, 395, 394, true, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(10015, 185, 205, 582, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(10015, 99, 154, 97, true, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(10015, 99, 389, 388, true, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(10015, 99, 494, 493, true, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(10015, 99, 391, 390, true, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(10015, 23, 127, 62, true, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(10010, 64, 45, 793, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(10042, 128, 141, 73, true, true, 0.0F, false, false, CreationCategories.ALCHEMY);
      createSimpleEntry(10042, 128, 436, 437, true, true, 0.0F, false, false, CreationCategories.ALCHEMY);
      createSimpleEntry(10042, 437, 46, 431, true, true, 0.0F, false, false, CreationCategories.DYES);
      createSimpleEntry(10042, 128, 48, 432, true, true, 0.0F, false, false, CreationCategories.DYES);
      createSimpleEntry(10042, 128, 47, 435, true, true, 0.0F, false, false, CreationCategories.DYES);
      createSimpleEntry(10042, 128, 439, 433, true, true, 0.0F, false, false, CreationCategories.DYES);
      createSimpleEntry(10042, 128, 440, 434, true, true, 0.0F, false, false, CreationCategories.DYES);
      createSimpleEntry(10042, 140, 214, 133, true, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
      createSimpleEntry(10042, 356, 140, 650, true, true, 0.0F, false, false, CreationCategories.HEALING);
      createSimpleEntry(10042, 1254, 214, 133, true, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
      createSimpleEntry(1013, 130, 298, 492, true, true, 0.0F, true, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(1016, 99, 125, 93, true, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(1016, 99, 126, 8, true, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(1005, 24, 9, 632, false, true, 0.0F, false, false, CreationCategories.CART_PARTS);
      createSimpleEntry(10016, 139, 144, 214, false, true, 0.0F, false, false, CreationCategories.RESOURCES);
      createSimpleEntry(10016, 139, 171, 214, false, true, 0.0F, false, false, CreationCategories.RESOURCES);
      createSimpleEntry(10016, 226, 214, 213, false, true, 0.0F, false, false, CreationCategories.RESOURCES);
      createSimpleEntry(10016, 226, 214, 646, false, true, 0.0F, false, false, CreationCategories.RUGS);
      createSimpleEntry(10016, 226, 214, 645, false, true, 0.0F, false, false, CreationCategories.RUGS);
      createSimpleEntry(10016, 226, 214, 644, false, true, 0.0F, false, false, CreationCategories.RUGS);
      createSimpleEntry(10016, 226, 214, 639, false, true, 0.0F, false, false, CreationCategories.RUGS);
      createSimpleEntry(10016, 213, 23, 487, true, true, 0.0F, false, false, CreationCategories.FLAGS);
      createSimpleEntry(10016, 213, 23, 577, true, true, 0.0F, false, false, CreationCategories.FLAGS);
      createSimpleEntry(10016, 213, 23, 579, true, true, 0.0F, false, false, CreationCategories.FLAGS);
      createSimpleEntry(10016, 213, 23, 578, true, true, 0.0F, false, false, CreationCategories.FLAGS);
      createSimpleEntry(10016, 213, 23, 999, true, true, 0.0F, false, false, 10, 35.0, CreationCategories.FLAGS);
      createSimpleEntry(1005, 685, 23, 862, false, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(1020, 169, 385, 652, true, true, 0.0F, false, false, CreationCategories.DECORATION);
      if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
         createMetallicEntries(10013, 185, 46, 284, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createMetallicEntries(10013, 185, 46, 280, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createMetallicEntries(10013, 185, 46, 281, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createMetallicEntries(10013, 185, 46, 282, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createMetallicEntries(10013, 185, 46, 283, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createMetallicEntries(10013, 185, 46, 285, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createMetallicEntries(10013, 185, 46, 286, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createMetallicEntries(10013, 185, 46, 287, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createMetallicEntries(10012, 185, 46, 288, false, true, 10.0F, false, false, CreationCategories.RESOURCES);
         createMetallicEntries(10043, 185, 46, 326, false, true, 10.0F, false, false, CreationCategories.DECORATION);
      } else {
         createSimpleEntry(10013, 185, 205, 284, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 205, 280, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 205, 281, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 205, 282, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 205, 283, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 205, 285, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 205, 286, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 205, 287, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 698, 284, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 698, 280, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 698, 281, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 698, 282, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 698, 283, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 698, 285, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 698, 286, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 698, 287, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 694, 284, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 694, 280, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 694, 281, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 694, 282, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 694, 283, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 694, 285, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 694, 286, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 694, 287, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 837, 284, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 837, 280, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 837, 281, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 837, 282, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 837, 283, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 837, 285, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 837, 286, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 837, 287, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 46, 284, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 46, 280, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 46, 281, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 46, 282, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 46, 283, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 46, 285, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 46, 286, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10013, 185, 46, 287, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
         createSimpleEntry(10012, 185, 205, 288, false, true, 10.0F, false, false, CreationCategories.RESOURCES);
         createSimpleEntry(10012, 185, 46, 288, false, true, 10.0F, false, false, CreationCategories.RESOURCES);
         createSimpleEntry(10012, 185, 694, 288, false, true, 10.0F, false, false, CreationCategories.RESOURCES);
         createSimpleEntry(10012, 185, 837, 288, false, true, 10.0F, false, false, CreationCategories.RESOURCES);
         createSimpleEntry(10012, 185, 698, 288, false, true, 10.0F, false, false, CreationCategories.RESOURCES);
         createSimpleEntry(10012, 185, 47, 288, false, true, 10.0F, false, false, CreationCategories.RESOURCES);
         createSimpleEntry(10012, 185, 223, 288, false, true, 10.0F, false, false, CreationCategories.RESOURCES);
         createSimpleEntry(10012, 185, 45, 288, false, true, 10.0F, false, false, CreationCategories.RESOURCES);
         createSimpleEntry(10012, 185, 44, 288, false, true, 10.0F, false, false, CreationCategories.RESOURCES);
         createSimpleEntry(10043, 185, 205, 326, false, true, 10.0F, false, false, CreationCategories.DECORATION);
         createSimpleEntry(10043, 185, 46, 326, false, true, 10.0F, false, false, CreationCategories.DECORATION);
         createSimpleEntry(10043, 185, 47, 326, false, true, 10.0F, false, false, CreationCategories.DECORATION);
         createSimpleEntry(10043, 185, 223, 326, false, true, 10.0F, false, false, CreationCategories.DECORATION);
         createSimpleEntry(10043, 185, 45, 326, false, true, 10.0F, false, false, CreationCategories.DECORATION);
         createSimpleEntry(10043, 185, 44, 326, false, true, 10.0F, false, false, CreationCategories.DECORATION);
         createSimpleEntry(10043, 185, 694, 326, false, true, 10.0F, false, false, CreationCategories.DECORATION);
         createSimpleEntry(10043, 185, 837, 326, false, true, 10.0F, false, false, CreationCategories.DECORATION);
         createSimpleEntry(10043, 185, 698, 326, false, true, 10.0F, false, false, CreationCategories.DECORATION);
      }

      createSimpleEntry(10013, 185, 372, 478, false, true, 0.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10013, 185, 372, 474, false, true, 0.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10013, 185, 372, 475, false, true, 0.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10013, 185, 372, 476, false, true, 0.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10013, 185, 372, 477, false, true, 0.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10012, 185, 288, 274, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10012, 185, 288, 279, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10012, 185, 288, 278, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10012, 185, 288, 275, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10012, 185, 288, 276, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10012, 185, 288, 277, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10012, 185, 288, 703, false, true, 10.0F, false, false, CreationCategories.ANIMAL_EQUIPMENT);
      createSimpleEntry(10017, 73, 71, 72, true, true, 0.0F, false, false, CreationCategories.RESOURCES);
      createSimpleEntry(10017, 215, 172, 72, false, true, 50.0F, false, false, CreationCategories.RESOURCES);
      createSimpleEntry(10017, 215, 72, 105, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10017, 215, 72, 107, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10017, 215, 72, 103, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10017, 215, 72, 108, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10017, 215, 72, 104, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10017, 215, 72, 106, false, true, 10.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10017, 215, 72, 102, false, true, 10.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10017, 394, 72, 100, false, true, 10.0F, false, false, CreationCategories.RESOURCES);
      createSimpleEntry(10017, 215, 371, 469, false, true, 0.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10017, 215, 371, 470, false, true, 0.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10017, 215, 371, 472, false, true, 0.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10017, 215, 371, 471, false, true, 0.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10017, 215, 371, 473, false, true, 0.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10017, 215, 371, 468, false, true, 0.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10017, 215, 72, 79, false, true, 10.0F, false, false, CreationCategories.BAGS);
      createSimpleEntry(10017, 215, 72, 1, false, true, 10.0F, false, false, CreationCategories.BAGS);
      createSimpleEntry(10017, 215, 72, 462, false, true, 10.0F, false, false, CreationCategories.BAGS);
      createSimpleEntry(10017, 215, 72, 629, false, true, 10.0F, false, false, CreationCategories.ANIMAL_EQUIPMENT_PART);
      createSimpleEntry(10017, 215, 72, 630, false, true, 10.0F, false, false, CreationCategories.ANIMAL_EQUIPMENT_PART);
      createSimpleEntry(10017, 215, 72, 628, false, true, 10.0F, false, false, CreationCategories.ANIMAL_EQUIPMENT_PART);
      createSimpleEntry(10017, 215, 72, 631, false, true, 10.0F, false, false, CreationCategories.ANIMAL_EQUIPMENT_PART);
      createSimpleEntry(10017, 215, 72, 625, false, true, 10.0F, false, false, CreationCategories.ANIMAL_EQUIPMENT_PART);
      createSimpleEntry(10017, 215, 72, 626, false, true, 10.0F, false, false, CreationCategories.ANIMAL_EQUIPMENT_PART);
      createSimpleEntry(10017, 215, 72, 1332, false, true, 10.0F, false, false, CreationCategories.ANIMAL_EQUIPMENT_PART);
      if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
         createMetallicEntries(10015, 64, 46, 131, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createMetallicEntries(10015, 64, 46, 517, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createMetallicEntries(10015, 64, 46, 444, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createMetallicEntries(10011, 64, 46, 451, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createMetallicEntries(10011, 64, 46, 452, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createMetallicEntries(10015, 185, 46, 135, false, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
         createMetallicEntries(10015, 185, 46, 497, false, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
         createMetallicEntries(10015, 185, 46, 675, false, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
         createMetallicEntries(10015, 185, 46, 660, false, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
         createMetallicEntries(10015, 185, 46, 674, false, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
         createMetallicEntries(10011, 64, 46, 123, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createMetallicEntries(10015, 64, 46, 219, false, true, 0.0F, false, false, CreationCategories.TOOLS);
         createMetallicEntries(10015, 64, 46, 701, false, true, 0.0F, false, false, CreationCategories.TOOLS);
         createMetallicEntries(10015, 64, 46, 124, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createMetallicEntries(10015, 64, 46, 24, false, true, 0.0F, false, false, CreationCategories.TOOLS);
      } else {
         createSimpleEntry(10015, 64, 46, 131, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10015, 64, 205, 131, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10015, 64, 47, 131, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10015, 64, 45, 131, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10015, 64, 44, 131, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10015, 64, 223, 131, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10015, 64, 694, 131, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10015, 64, 837, 131, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10015, 64, 698, 131, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10015, 64, 46, 517, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10015, 64, 205, 517, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10015, 64, 47, 517, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10015, 64, 45, 517, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10015, 64, 44, 517, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10015, 64, 223, 517, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10015, 64, 46, 444, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10015, 64, 205, 444, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10015, 64, 47, 444, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10015, 64, 45, 444, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10015, 64, 44, 444, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10015, 64, 223, 444, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10011, 64, 46, 451, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 698, 451, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 694, 451, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 837, 451, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 205, 451, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 47, 451, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 45, 451, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 44, 451, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 223, 451, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 46, 452, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 698, 452, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 694, 452, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 837, 452, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 205, 452, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 47, 452, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 45, 452, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 44, 452, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 223, 452, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10015, 185, 46, 135, false, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
         createSimpleEntry(10015, 185, 46, 497, false, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
         createSimpleEntry(10015, 185, 221, 497, false, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
         createSimpleEntry(10015, 185, 46, 675, false, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
         createSimpleEntry(10015, 185, 46, 660, false, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
         createSimpleEntry(10015, 185, 47, 674, false, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
         createSimpleEntry(10015, 185, 223, 674, false, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
         createSimpleEntry(10015, 185, 221, 674, false, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
         createSimpleEntry(10015, 185, 44, 674, false, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
         createSimpleEntry(10015, 185, 45, 674, false, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
         createSimpleEntry(10011, 64, 46, 123, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10011, 64, 205, 123, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10011, 64, 694, 123, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10011, 64, 837, 123, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10011, 64, 698, 123, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10015, 64, 46, 219, false, true, 0.0F, false, false, CreationCategories.TOOLS);
         createSimpleEntry(10015, 64, 46, 701, false, true, 0.0F, false, false, CreationCategories.TOOLS);
         createSimpleEntry(10015, 64, 46, 124, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10015, 64, 46, 24, false, true, 0.0F, false, false, CreationCategories.TOOLS);
      }

      createSimpleEntry(10015, 64, 46, 217, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10015, 64, 46, 218, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10015, 23, 444, 441, true, true, 10.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(1005, 318, 23, 647, true, true, 10.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(10017, 131, 105, 116, true, true, 0.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10017, 131, 107, 117, true, true, 0.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10017, 131, 103, 119, true, true, 0.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10017, 131, 108, 118, true, true, 0.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10017, 131, 104, 120, true, true, 0.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(10017, 131, 106, 115, true, true, 0.0F, false, false, CreationCategories.ARMOUR);
      createSimpleEntry(1005, 23, 156, 63, true, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(1005, 8, 23, 156, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(1005, 685, 23, 156, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(10015, 185, 205, 609, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10015, 23, 523, 7, true, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(10017, 100, 99, 101, true, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(10015, 9, 497, 496, true, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
      createSimpleEntry(10015, 9, 660, 657, true, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
      createSimpleEntry(10015, 9, 674, 658, true, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
      createSimpleEntry(10015, 185, 221, 136, false, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
      if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
         for(int sourceTemplateId : ItemFactory.metalLumpList) {
            try {
               ItemTemplate lump = ItemTemplateFactory.getInstance().getTemplate(sourceTemplateId);
               CreationEntry temp = createSimpleEntry(10015, sourceTemplateId, 675, 659, true, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
               temp.objectTargetMaterial = lump.getMaterial();
            } catch (NoSuchTemplateException var239) {
            }
         }
      } else {
         createSimpleEntry(10015, 46, 675, 659, true, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
      }

      createSimpleEntry(10015, 23, 123, 20, true, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(10015, 735, 721, 718, true, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10015, 23, 686, 687, true, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(10015, 691, 686, 687, true, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(10015, 23, 1010, 1011, true, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(10015, 691, 1010, 1011, true, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(10015, 23, 124, 27, true, true, 0.0F, false, false, CreationCategories.TOOLS);
      if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
         createMetallicEntries(10014, 185, 46, 86, false, true, 0.0F, false, false, CreationCategories.SHIELDS);
         createMetallicEntries(10014, 185, 46, 4, false, true, 0.0F, false, false, CreationCategories.SHIELDS);
         createMetallicEntries(10014, 185, 46, 83, false, true, 0.0F, false, false, CreationCategories.SHIELDS);
         createMetallicEntries(10015, 185, 46, 121, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createMetallicEntries(10043, 64, 46, 228, false, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
         createMetallicEntries(10043, 64, 46, 232, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createMetallicEntries(10043, 64, 46, 229, false, true, 0.0F, false, false, CreationCategories.JEWELRY);
      } else {
         createSimpleEntry(10014, 185, 46, 86, false, true, 0.0F, false, false, CreationCategories.SHIELDS);
         createSimpleEntry(10014, 185, 46, 4, false, true, 0.0F, false, false, CreationCategories.SHIELDS);
         createSimpleEntry(10014, 185, 46, 83, false, true, 0.0F, false, false, CreationCategories.SHIELDS);
         createSimpleEntry(10014, 185, 698, 86, false, true, 0.0F, false, false, CreationCategories.SHIELDS);
         createSimpleEntry(10014, 185, 698, 4, false, true, 0.0F, false, false, CreationCategories.SHIELDS);
         createSimpleEntry(10014, 185, 698, 83, false, true, 0.0F, false, false, CreationCategories.SHIELDS);
         createSimpleEntry(10014, 185, 205, 86, false, true, 0.0F, false, false, CreationCategories.SHIELDS);
         createSimpleEntry(10014, 185, 205, 4, false, true, 0.0F, false, false, CreationCategories.SHIELDS);
         createSimpleEntry(10014, 185, 205, 83, false, true, 0.0F, false, false, CreationCategories.SHIELDS);
         createSimpleEntry(10014, 185, 694, 86, false, true, 0.0F, false, false, CreationCategories.SHIELDS);
         createSimpleEntry(10014, 185, 694, 4, false, true, 0.0F, false, false, CreationCategories.SHIELDS);
         createSimpleEntry(10014, 185, 694, 83, false, true, 0.0F, false, false, CreationCategories.SHIELDS);
         createSimpleEntry(10014, 185, 837, 86, false, true, 0.0F, false, false, CreationCategories.SHIELDS);
         createSimpleEntry(10014, 185, 837, 4, false, true, 0.0F, false, false, CreationCategories.SHIELDS);
         createSimpleEntry(10014, 185, 837, 83, false, true, 0.0F, false, false, CreationCategories.SHIELDS);
         createSimpleEntry(10015, 185, 46, 121, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10015, 185, 205, 121, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10015, 185, 698, 121, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10015, 185, 694, 121, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10015, 185, 837, 121, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
         createSimpleEntry(10043, 64, 44, 229, false, true, 0.0F, false, false, CreationCategories.JEWELRY);
         createSimpleEntry(10043, 64, 45, 229, false, true, 0.0F, false, false, CreationCategories.JEWELRY);
         createSimpleEntry(10043, 64, 44, 228, false, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
         createSimpleEntry(10043, 64, 45, 228, false, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
         createSimpleEntry(10043, 64, 46, 228, false, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
         createSimpleEntry(10043, 64, 698, 228, false, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
         createSimpleEntry(10043, 64, 694, 228, false, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
         createSimpleEntry(10043, 64, 837, 228, false, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
         createSimpleEntry(10043, 64, 47, 228, false, true, 0.0F, false, false, CreationCategories.LIGHTS_AND_LAMPS);
         createSimpleEntry(10043, 64, 44, 232, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10043, 64, 45, 232, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10043, 64, 46, 232, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10043, 64, 47, 232, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10043, 64, 223, 232, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10043, 64, 221, 232, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10043, 64, 49, 232, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10043, 64, 205, 232, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10043, 64, 694, 232, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10043, 64, 837, 232, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
         createSimpleEntry(10043, 64, 698, 232, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      }

      createSimpleEntry(10015, 185, 46, 188, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10015, 23, 121, 25, true, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(1005, 23, 689, 690, true, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(1005, 691, 689, 690, true, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(10041, 204, 46, 205, true, true, 0.0F, true, false, CreationCategories.RESOURCES);
      createSimpleEntry(10041, 220, 47, 223, true, true, 0.0F, false, false, CreationCategories.RESOURCES);
      createSimpleEntry(10041, 48, 47, 221, true, true, 0.0F, false, false, CreationCategories.RESOURCES);
      createSimpleEntry(10043, 64, 44, 227, false, true, 0.0F, false, false, CreationCategories.STATUETTES);
      createSimpleEntry(10043, 64, 45, 227, false, true, 0.0F, false, false, CreationCategories.STATUETTES);
      createSimpleEntry(10043, 64, 1411, 227, false, true, 0.0F, false, false, CreationCategories.STATUETTES);
      createSimpleEntry(10043, 64, 44, 505, false, true, 0.0F, false, false, CreationCategories.STATUETTES);
      createSimpleEntry(10043, 64, 45, 505, false, true, 0.0F, false, false, CreationCategories.STATUETTES);
      createSimpleEntry(10043, 64, 1411, 505, false, true, 0.0F, false, false, CreationCategories.STATUETTES);
      createSimpleEntry(10043, 64, 44, 506, false, true, 0.0F, false, false, CreationCategories.STATUETTES);
      createSimpleEntry(10043, 64, 45, 506, false, true, 0.0F, false, false, CreationCategories.STATUETTES);
      createSimpleEntry(10043, 64, 1411, 506, false, true, 0.0F, false, false, CreationCategories.STATUETTES);
      createSimpleEntry(10043, 64, 44, 507, false, true, 0.0F, false, false, CreationCategories.STATUETTES);
      createSimpleEntry(10043, 64, 45, 507, false, true, 0.0F, false, false, CreationCategories.STATUETTES);
      createSimpleEntry(10043, 64, 1411, 507, false, true, 0.0F, false, false, CreationCategories.STATUETTES);
      createSimpleEntry(10043, 64, 44, 508, false, true, 0.0F, false, false, CreationCategories.STATUETTES);
      createSimpleEntry(10043, 64, 45, 508, false, true, 0.0F, false, false, CreationCategories.STATUETTES);
      createSimpleEntry(10043, 64, 1411, 508, false, true, 0.0F, false, false, CreationCategories.STATUETTES);
      createSimpleEntry(10043, 64, 44, 230, false, true, 0.0F, false, false, CreationCategories.JEWELRY);
      createSimpleEntry(10043, 64, 45, 230, false, true, 0.0F, false, false, CreationCategories.JEWELRY);
      createSimpleEntry(10043, 64, 1411, 230, false, true, 0.0F, false, false, CreationCategories.JEWELRY);
      createSimpleEntry(10043, 64, 44, 231, false, true, 0.0F, false, false, CreationCategories.JEWELRY);
      createSimpleEntry(10043, 64, 45, 231, false, true, 0.0F, false, false, CreationCategories.JEWELRY);
      createSimpleEntry(10043, 64, 1411, 231, false, true, 0.0F, false, false, CreationCategories.JEWELRY);
      createSimpleEntry(10043, 64, 694, 231, false, true, 0.0F, false, false, CreationCategories.JEWELRY);
      createSimpleEntry(10043, 64, 837, 231, false, true, 0.0F, false, false, CreationCategories.JEWELRY);
      createSimpleEntry(10043, 64, 698, 231, false, true, 0.0F, false, false, CreationCategories.JEWELRY);
      createSimpleEntry(10043, 64, 44, 297, false, true, 100.0F, false, false, CreationCategories.JEWELRY);
      createSimpleEntry(10043, 64, 45, 297, false, true, 100.0F, false, false, CreationCategories.JEWELRY);
      createSimpleEntry(10043, 64, 1411, 297, false, true, 100.0F, false, false, CreationCategories.JEWELRY);
      createSimpleEntry(10043, 64, 698, 297, false, true, 100.0F, false, false, CreationCategories.JEWELRY);
      createSimpleEntry(10043, 64, 694, 297, false, true, 100.0F, false, false, CreationCategories.JEWELRY);
      createSimpleEntry(10043, 64, 837, 297, false, true, 100.0F, false, false, CreationCategories.JEWELRY);
      createSimpleEntry(10043, 229, 232, 233, true, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(10043, 185, 47, 839, false, true, 0.0F, false, false, 20, 30.0, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10043, 185, 44, 840, false, true, 0.0F, false, false, 20, 30.0, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10051, 214, 23, 271, true, true, 100.0F, false, false, CreationCategories.TOYS);
      createSimpleEntry(10074, 97, 146, 132, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10074, 97, 146, 1122, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10074, 97, 785, 786, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10074, 97, 770, 784, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10074, 97, 146, 519, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10074, 97, 1116, 1121, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10074, 97, 770, 1123, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10074, 97, 146, 202, false, true, 10.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(10074, 97, 146, 296, false, true, 10.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(10074, 97, 146, 402, false, true, 10.0F, false, true, CreationCategories.STATUES);
      createSimpleEntry(10074, 97, 146, 399, false, true, 10.0F, false, true, CreationCategories.STATUES);
      createSimpleEntry(10074, 97, 146, 400, false, true, 10.0F, false, true, CreationCategories.STATUES);
      createSimpleEntry(10074, 97, 146, 403, false, true, 10.0F, false, true, CreationCategories.STATUES);
      createSimpleEntry(10074, 97, 146, 398, false, true, 10.0F, false, true, CreationCategories.STATUES);
      createSimpleEntry(10074, 97, 146, 401, false, true, 10.0F, false, true, CreationCategories.STATUES);
      createSimpleEntry(10074, 97, 146, 406, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10074, 97, 785, 787, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10074, 97, 1116, 1124, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10074, 97, 770, 771, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10074, 97, 146, 905, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10074, 97, 785, 906, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10074, 97, 770, 1302, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10074, 97, 1116, 1305, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(1011, 14, 130, 1303, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10074, 97, 146, 408, false, true, 10.0F, false, true, CreationCategories.FOUNTAINS_AND_WELLS);
      createSimpleEntry(10074, 97, 146, 635, false, true, 10.0F, false, true, CreationCategories.FOUNTAINS_AND_WELLS);
      createSimpleEntry(10074, 97, 146, 405, false, true, 10.0F, false, true, CreationCategories.FOUNTAINS_AND_WELLS);
      createSimpleEntry(10074, 97, 146, 593, false, true, 10.0F, false, true, CreationCategories.MINE_DOORS);
      createSimpleEntry(10074, 684, 146, 685, true, true, 10.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(10074, 685, 146, 686, false, true, 10.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(10074, 685, 146, 689, false, true, 10.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(10074, 685, 146, 1010, false, true, 10.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(10074, 97, 146, 686, false, true, 10.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(10074, 97, 146, 689, false, true, 10.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(10074, 97, 146, 1010, false, true, 10.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(1005, 685, 688, 36, false, true, 10.0F, false, false, CreationCategories.KINDLINGS);
      createSimpleEntry(1005, 8, 688, 36, false, true, 10.0F, false, false, CreationCategories.KINDLINGS);
      createSimpleEntry(1005, 93, 688, 36, false, true, 10.0F, false, false, CreationCategories.KINDLINGS);
      createSimpleEntry(1005, 685, 688, 23, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(1005, 8, 688, 23, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(1005, 93, 688, 23, false, true, 10.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
         createMetallicEntries(10034, 64, 46, 167, false, true, 0.0F, false, false, CreationCategories.LOCKS);
         createMetallicEntries(10034, 64, 46, 194, false, true, 0.0F, false, false, CreationCategories.LOCKS);
         createMetallicEntries(10034, 64, 46, 193, false, true, 0.0F, false, false, CreationCategories.LOCKS);
         createMetallicEntries(10034, 64, 46, 463, false, true, 0.0F, false, false, 0, 25.0, CreationCategories.TOOLS);
         createMetallicEntries(10034, 185, 46, 252, false, true, 0.0F, false, false, CreationCategories.LOCKS);
         createMetallicEntries(10034, 185, 46, 568, false, true, 0.0F, false, false, CreationCategories.LOCKS);
         createMetallicEntries(10015, 64, 46, 185, false, true, 0.0F, false, true, CreationCategories.TOOLS);
         createMetallicEntries(10015, 185, 46, 547, false, true, 0.0F, false, true, CreationCategories.SHIPBUILDING);
      } else {
         createSimpleEntry(10034, 64, 46, 167, false, true, 0.0F, false, false, CreationCategories.LOCKS);
         createSimpleEntry(10034, 64, 46, 194, false, true, 0.0F, false, false, CreationCategories.LOCKS);
         createSimpleEntry(10034, 64, 46, 193, false, true, 0.0F, false, false, CreationCategories.LOCKS);
         createSimpleEntry(10034, 64, 694, 167, false, true, 0.0F, false, false, CreationCategories.LOCKS);
         createSimpleEntry(10034, 64, 694, 194, false, true, 0.0F, false, false, CreationCategories.LOCKS);
         createSimpleEntry(10034, 64, 694, 193, false, true, 0.0F, false, false, CreationCategories.LOCKS);
         createSimpleEntry(10034, 64, 837, 167, false, true, 0.0F, false, false, CreationCategories.LOCKS);
         createSimpleEntry(10034, 64, 837, 194, false, true, 0.0F, false, false, CreationCategories.LOCKS);
         createSimpleEntry(10034, 64, 837, 193, false, true, 0.0F, false, false, CreationCategories.LOCKS);
         createSimpleEntry(10034, 64, 698, 167, false, true, 0.0F, false, false, CreationCategories.LOCKS);
         createSimpleEntry(10034, 64, 698, 194, false, true, 0.0F, false, false, CreationCategories.LOCKS);
         createSimpleEntry(10034, 64, 698, 193, false, true, 0.0F, false, false, CreationCategories.LOCKS);
         createSimpleEntry(10034, 64, 46, 463, false, true, 0.0F, false, false, 0, 25.0, CreationCategories.TOOLS);
         createSimpleEntry(10034, 64, 205, 463, false, true, 0.0F, false, false, 0, 25.0, CreationCategories.TOOLS);
         createSimpleEntry(10034, 64, 694, 463, false, true, 0.0F, false, false, 0, 25.0, CreationCategories.TOOLS);
         createSimpleEntry(10034, 64, 837, 463, false, true, 0.0F, false, false, 0, 25.0, CreationCategories.TOOLS);
         createSimpleEntry(10034, 64, 698, 463, false, true, 0.0F, false, false, 0, 25.0, CreationCategories.TOOLS);
         createSimpleEntry(10034, 64, 47, 463, false, true, 0.0F, false, false, 0, 25.0, CreationCategories.TOOLS);
         createSimpleEntry(10034, 185, 46, 252, false, true, 0.0F, false, false, CreationCategories.LOCKS);
         createSimpleEntry(10034, 185, 46, 568, false, true, 0.0F, false, false, CreationCategories.LOCKS);
         createSimpleEntry(10015, 64, 46, 185, false, true, 0.0F, false, true, CreationCategories.TOOLS);
         createSimpleEntry(10015, 185, 49, 547, false, true, 0.0F, false, true, CreationCategories.SHIPBUILDING);
      }

      createSimpleEntry(10034, 168, 130, 342, false, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(10034, 343, 47, 341, false, true, 0.0F, false, false, CreationCategories.LOCKS);
      createSimpleEntry(10034, 341, 130, 342, false, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(1014, 320, 318, 319, false, true, 30.0F, false, false, CreationCategories.ROPES);
      createSimpleEntry(1014, 320, 318, 557, false, true, 30.0F, false, false, CreationCategories.ROPES);
      createSimpleEntry(1014, 320, 318, 559, false, true, 30.0F, false, false, CreationCategories.ROPES);
      createSimpleEntry(1014, 320, 318, 558, false, true, 30.0F, false, false, CreationCategories.ROPES);
      createSimpleEntry(1014, 320, 318, 457, false, true, 30.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(10039, 428, 203, 488, true, true, 0.0F, false, false, CreationCategories.FOOD);
      createSimpleEntry(10039, 66, 203, 488, true, true, 0.0F, false, false, CreationCategories.FOOD);
      createSimpleEntry(10039, 68, 203, 488, true, true, 0.0F, false, false, CreationCategories.FOOD);
      createSimpleEntry(10039, 69, 203, 488, true, true, 0.0F, false, false, CreationCategories.FOOD);
      createSimpleEntry(10039, 67, 203, 488, true, true, 0.0F, false, false, CreationCategories.FOOD);
      createSimpleEntry(10039, 415, 203, 488, true, true, 0.0F, false, false, CreationCategories.FOOD);
      createSimpleEntry(10039, 70, 203, 488, true, true, 0.0F, false, false, CreationCategories.FOOD);
      createSimpleEntry(10039, 464, 203, 488, true, true, 0.0F, false, false, CreationCategories.FOOD);
      createSimpleEntry(10042, 413, 752, 753, false, true, 50.0F, false, false, CreationCategories.WRITING);
      createSimpleEntry(1018, 8, 33, 522, false, true, 0.0F, false, false, CreationCategories.DECORATION);
      createSimpleEntry(1018, 685, 33, 522, false, true, 0.0F, false, false, CreationCategories.DECORATION);
      createSimpleEntry(10043, 185, 45, 325, false, true, 0.0F, false, true, CreationCategories.ALTAR);
      createSimpleEntry(10043, 185, 44, 324, false, true, 0.0F, false, true, CreationCategories.ALTAR);
      createSimpleEntry(10082, 24, 9, 556, false, true, 60.0F, false, false, CreationCategories.SHIPBUILDING);
      createSimpleEntry(10082, 24, 9, 545, false, true, 20.0F, false, false, CreationCategories.SHIPBUILDING);
      createSimpleEntry(10082, 8, 9, 550, false, true, 30.0F, false, false, CreationCategories.SHIPBUILDING);
      createSimpleEntry(10082, 8, 9, 549, false, true, 30.0F, false, false, CreationCategories.SHIPBUILDING);
      createSimpleEntry(10082, 8, 23, 567, false, true, 30.0F, false, false, CreationCategories.SHIPBUILDING);
      createSimpleEntry(10082, 8, 9, 551, false, true, 40.0F, false, false, CreationCategories.SHIPBUILDING);
      createSimpleEntry(10082, 24, 9, 546, false, true, 0.0F, false, false, CreationCategories.SHIPBUILDING);
      createSimpleEntry(10082, 24, 9, 566, false, true, 40.0F, false, false, CreationCategories.SHIPBUILDING);
      createSimpleEntry(10082, 7, 385, 552, false, true, 20.0F, false, false, CreationCategories.SHIPBUILDING);
      createSimpleEntry(10082, 7, 385, 588, false, true, 20.0F, false, false, CreationCategories.SHIPBUILDING);
      createSimpleEntry(10082, 7, 385, 590, false, true, 20.0F, false, false, CreationCategories.SHIPBUILDING);
      createSimpleEntry(10082, 7, 385, 589, false, true, 20.0F, false, false, CreationCategories.SHIPBUILDING);
      createSimpleEntry(10082, 7, 385, 560, false, true, 50.0F, false, false, CreationCategories.SHIPBUILDING);
      createSimpleEntry(1020, 558, 547, 565, true, true, 0.0F, false, false, CreationCategories.SHIPBUILDING);
      createSimpleEntry(10042, 73, 492, 782, true, true, 0.0F, true, false, CreationCategories.RESOURCES);
      createSimpleEntry(10074, 97, 146, 811, false, true, 10.0F, false, true, CreationCategories.STATUES);
      createSimpleEntry(10015, 185, 46, 859, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      createSimpleEntry(10042, 383, 711, 825, true, true, 0.0F, false, false, CreationCategories.MAGIC);
      createSimpleEntry(10042, 377, 711, 826, true, true, 0.0F, false, false, CreationCategories.MAGIC);
      createSimpleEntry(10042, 381, 711, 827, true, true, 0.0F, false, false, CreationCategories.MAGIC);
      createSimpleEntry(10042, 379, 711, 828, true, true, 0.0F, false, false, CreationCategories.MAGIC);
      createSimpleEntry(10042, 375, 711, 829, true, true, 0.0F, false, false, CreationCategories.MAGIC);
      createSimpleEntry(1011, 14, 130, 812, false, true, 0.0F, false, false, CreationCategories.POTTERY);
      createSimpleEntry(10074, 97, 146, 821, false, true, 0.0F, false, false, CreationCategories.DECORATION);
      createSimpleEntry(10015, 64, 221, 902, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(10015, 64, 223, 904, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(10016, 922, 921, 925, false, true, 0.0F, false, false, CreationCategories.RESOURCES);
      createSimpleEntry(10016, 226, 925, 926, false, true, 0.0F, false, false, CreationCategories.RESOURCES);
      createSimpleEntry(10016, 215, 926, 943, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10016, 216, 926, 943, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10016, 215, 926, 954, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10016, 216, 926, 954, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10017, 926, 847, 959, true, true, 0.0F, false, false, CreationCategories.ARMOUR).setFinalMaterial((byte)16);
      createSimpleEntry(10017, 215, 72, 960, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10016, 215, 926, 961, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10016, 216, 926, 961, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10016, 226, 214, 908, false, true, 0.0F, false, false, 0, 20.0, CreationCategories.RUGS).setDepleteFromTarget(3000);
      createSimpleEntry(10016, 226, 214, 909, false, true, 0.0F, false, false, 0, 30.0, CreationCategories.RUGS).setDepleteFromTarget(4000);
      createSimpleEntry(10016, 226, 214, 910, false, true, 0.0F, false, false, 0, 40.0, CreationCategories.RUGS).setDepleteFromTarget(5000);
      createSimpleEntry(10074, 97, 785, 402, false, true, 10.0F, false, true, CreationCategories.STATUES);
      createSimpleEntry(10074, 97, 785, 403, false, true, 10.0F, false, true, CreationCategories.STATUES);
      createSimpleEntry(10074, 97, 785, 398, false, true, 10.0F, false, true, CreationCategories.STATUES);
      createSimpleEntry(10074, 97, 785, 401, false, true, 10.0F, false, true, CreationCategories.STATUES);
      createSimpleEntry(10074, 97, 785, 811, false, true, 10.0F, false, true, CreationCategories.STATUES);
      createSimpleEntry(1011, 14, 130, 1160, false, true, 0.0F, false, false, CreationCategories.POTTERY);
      createSimpleEntry(1011, 14, 130, 1164, false, true, 0.0F, false, false, CreationCategories.POTTERY);
      createSimpleEntry(1011, 14, 130, 1168, false, true, 0.0F, false, false, CreationCategories.POTTERY);
      createSimpleEntry(1011, 14, 130, 1171, false, true, 0.0F, false, false, CreationCategories.POTTERY);
      createSimpleEntry(1011, 14, 130, 1251, false, true, 0.0F, false, false, CreationCategories.POTTERY);
      createSimpleEntry(10015, 64, 220, 1166, false, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(10074, 97, 785, 1167, false, true, 10.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(10074, 97, 785, 1237, false, true, 10.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(1005, 8, 9, 1173, false, true, 0.0F, false, false, CreationCategories.COOKING_UTENSILS);
      int[] metalTypes = new int[]{221, 223, 220, 694, 698, 44, 45, 205, 47, 46, 49, 48, 837};
      int[] runeTypes = new int[]{1290, 1293, 1292, 1289, 1291};

      for(int metal : metalTypes) {
         for(int rune : runeTypes) {
            createSimpleEntry(10074, 1102, metal, rune, true, true, 0.0F, false, false, CreationCategories.MAGIC).setDepleteFromTarget(200);
            createSimpleEntry(10043, 1103, metal, rune, true, true, 0.0F, false, false, CreationCategories.MAGIC).setDepleteFromTarget(200);
            createSimpleEntry(10044, 1104, metal, rune, true, true, 0.0F, false, false, CreationCategories.MAGIC).setDepleteFromTarget(200);
         }
      }

      createSimpleEntry(10074, 1102, 1411, 1290, true, true, 0.0F, false, false, CreationCategories.MAGIC).setDepleteFromTarget(200);
      createSimpleEntry(10043, 1103, 1411, 1290, true, true, 0.0F, false, false, CreationCategories.MAGIC).setDepleteFromTarget(200);
      createSimpleEntry(10044, 1104, 1411, 1290, true, true, 0.0F, false, false, CreationCategories.MAGIC).setDepleteFromTarget(200);
      createSimpleEntry(10041, 44, 45, 1411, true, true, 0.0F, false, false, CreationCategories.RESOURCES);
      createSimpleEntry(10074, 97, 785, 1430, false, true, 10.0F, false, true, CreationCategories.STATUES);
      CreationEntry brownBearRug = createSimpleEntry(10017, 302, 349, 847, true, true, 0.0F, false, false, 0, 30.0, CreationCategories.RUGS);
      brownBearRug.setDepleteFromSource(3000);
      brownBearRug.setDepleteFromTarget(1);
      CreationEntry blackBearRug = createSimpleEntry(10017, 302, 349, 846, true, true, 0.0F, false, false, 0, 30.0, CreationCategories.RUGS);
      blackBearRug.setDepleteFromSource(3000);
      blackBearRug.setDepleteFromTarget(1);
      CreationEntry mountainLionRug = createSimpleEntry(10017, 313, 349, 848, true, true, 0.0F, false, false, 0, 30.0, CreationCategories.RUGS);
      mountainLionRug.setDepleteFromSource(300);
      mountainLionRug.setDepleteFromTarget(1);
      CreationEntry blackWolfRug = createSimpleEntry(10017, 302, 349, 849, true, true, 0.0F, false, false, 0, 30.0, CreationCategories.RUGS);
      blackWolfRug.setDepleteFromSource(3000);
      blackWolfRug.setDepleteFromTarget(1);
      AdvancedCreationEntry lRudder = createAdvancedEntry(10082, 23, 22, 544, false, false, 0.0F, true, false, CreationCategories.SHIPBUILDING);
      lRudder.addRequirement(new CreationRequirement(1, 22, 5, true));
      AdvancedCreationEntry bellCot = createAdvancedEntry(1005, 217, 22, 723, false, false, 0.0F, true, false, CreationCategories.CONSTRUCTION_MATERIAL);
      bellCot.addRequirement(new CreationRequirement(1, 22, 25, true));
      bellCot.addRequirement(new CreationRequirement(2, 9, 4, true));
      AdvancedCreationEntry bellTower = createAdvancedEntry(1005, 718, 723, 722, false, false, 0.0F, true, false, CreationCategories.DECORATION);
      bellTower.addRequirement(new CreationRequirement(1, 319, 1, true));
      AdvancedCreationEntry bellSmall = createAdvancedEntry(10015, 734, 720, 719, false, false, 0.0F, true, false, CreationCategories.TOYS);
      bellSmall.addRequirement(new CreationRequirement(1, 99, 1, true));
      AdvancedCreationEntry lHelm = createAdvancedEntry(10082, 23, 22, 548, false, false, 0.0F, true, true, CreationCategories.SHIPBUILDING);
      lHelm.addRequirement(new CreationRequirement(1, 23, 5, true));
      lHelm.addRequirement(new CreationRequirement(2, 22, 4, true));
      lHelm.addRequirement(new CreationRequirement(3, 561, 8, true));
      AdvancedCreationEntry well = createAdvancedEntry(1013, 132, 130, 608, false, false, 0.0F, true, true, CreationCategories.FOUNTAINS_AND_WELLS);
      well.addRequirement(new CreationRequirement(1, 132, 10, true));
      well.addRequirement(new CreationRequirement(2, 130, 10, true));
      well.addRequirement(new CreationRequirement(3, 319, 1, true));
      well.addRequirement(new CreationRequirement(4, 421, 1, true));
      AdvancedCreationEntry lStern = createAdvancedEntry(10082, 551, 546, 553, false, false, 0.0F, true, false, CreationCategories.SHIPBUILDING);
      lStern.addRequirement(new CreationRequirement(1, 546, 10, true));
      lStern.addRequirement(new CreationRequirement(2, 551, 9, true));
      lStern.addRequirement(new CreationRequirement(3, 561, 20, true));
      AdvancedCreationEntry lRigS = createAdvancedEntry(10082, 589, 555, 564, false, false, 0.0F, true, false, CreationCategories.SHIPBUILDING);
      lRigS.addRequirement(new CreationRequirement(1, 559, 4, true));
      lRigS.addRequirement(new CreationRequirement(2, 549, 2, true));
      lRigS.addRequirement(new CreationRequirement(3, 550, 2, true));
      AdvancedCreationEntry lRigT = createAdvancedEntry(10082, 588, 554, 563, false, false, 0.0F, true, false, CreationCategories.SHIPBUILDING);
      lRigT.addRequirement(new CreationRequirement(1, 559, 2, true));
      lRigT.addRequirement(new CreationRequirement(2, 549, 2, true));
      AdvancedCreationEntry spinRigT = createAdvancedEntry(10082, 588, 591, 584, false, false, 0.0F, true, false, CreationCategories.SHIPBUILDING);
      spinRigT.addRequirement(new CreationRequirement(1, 559, 2, true));
      spinRigT.addRequirement(new CreationRequirement(2, 549, 2, true));
      AdvancedCreationEntry lRigCrows = createAdvancedEntry(10082, 590, 555, 585, false, false, 0.0F, true, false, CreationCategories.SHIPBUILDING);
      lRigCrows.addRequirement(new CreationRequirement(1, 559, 8, true));
      lRigCrows.addRequirement(new CreationRequirement(2, 583, 1, true));
      lRigCrows.addRequirement(new CreationRequirement(3, 550, 4, true));
      lRigCrows.addRequirement(new CreationRequirement(4, 549, 2, true));
      AdvancedCreationEntry tRigCrows = createAdvancedEntry(10082, 552, 555, 587, false, false, 0.0F, true, false, CreationCategories.SHIPBUILDING);
      tRigCrows.addRequirement(new CreationRequirement(1, 559, 16, true));
      tRigCrows.addRequirement(new CreationRequirement(2, 583, 1, true));
      tRigCrows.addRequirement(new CreationRequirement(3, 555, 5, true));
      tRigCrows.addRequirement(new CreationRequirement(4, 550, 8, true));
      tRigCrows.addRequirement(new CreationRequirement(5, 549, 4, true));
      AdvancedCreationEntry lRigSqY = createAdvancedEntry(10082, 552, 555, 586, false, false, 0.0F, true, false, CreationCategories.SHIPBUILDING);
      lRigSqY.addRequirement(new CreationRequirement(1, 559, 12, true));
      lRigSqY.addRequirement(new CreationRequirement(2, 555, 3, true));
      lRigSqY.addRequirement(new CreationRequirement(3, 550, 6, true));
      AdvancedCreationEntry bardingLeather = createAdvancedEntry(10017, 131, 72, 702, false, false, 0.0F, true, false, CreationCategories.ANIMAL_EQUIPMENT);
      bardingLeather.addRequirement(new CreationRequirement(1, 72, 4, true));
      bardingLeather.addRequirement(new CreationRequirement(2, 131, 50, true));
      AdvancedCreationEntry bridle = createAdvancedEntry(10017, 627, 631, 624, false, false, 0.0F, true, false, CreationCategories.ANIMAL_EQUIPMENT);
      bridle.addRequirement(new CreationRequirement(1, 628, 1, true));
      AdvancedCreationEntry saddle = createAdvancedEntry(10017, 625, 629, 621, false, false, 0.0F, true, false, CreationCategories.ANIMAL_EQUIPMENT);
      saddle.addRequirement(new CreationRequirement(1, 626, 1, true));
      AdvancedCreationEntry saddleL = createAdvancedEntry(10017, 625, 630, 622, false, false, 0.0F, true, false, CreationCategories.ANIMAL_EQUIPMENT);
      saddleL.addRequirement(new CreationRequirement(1, 626, 1, true));
      AdvancedCreationEntry saddleBag = createAdvancedEntry(10017, 1332, 102, 1333, false, false, 0.0F, true, false, CreationCategories.ANIMAL_EQUIPMENT);
      saddleBag.addRequirement(new CreationRequirement(1, 102, 1, true));
      saddleBag.addRequirement(new CreationRequirement(2, 1332, 1, true));
      AdvancedCreationEntry birdcage = createAdvancedEntry(10043, 897, 444, 1025, false, false, 0.0F, true, false, CreationCategories.DECORATION);
      birdcage.addRequirement(new CreationRequirement(1, 897, 1, true));
      birdcage.addRequirement(new CreationRequirement(2, 131, 10, true));
      birdcage.addRequirement(new CreationRequirement(3, 444, 4, true));
      birdcage.addRequirement(new CreationRequirement(4, 326, 1, true));
      birdcage.addRequirement(new CreationRequirement(5, 221, 10, true));
      birdcage.addRequirement(new CreationRequirement(6, 464, 1, true));
      birdcage.setIsEpicBuildMissionTarget(false);
      createBoatEntries();
      AdvancedCreationEntry sacknife = createAdvancedEntry(1016, 101, 793, 792, false, false, 0.0F, true, false, CreationCategories.WEAPONS);
      sacknife.addRequirement(new CreationRequirement(1, 376, 3, true));
      sacknife.addRequirement(new CreationRequirement(2, 382, 3, true));
      sacknife.addRequirement(new CreationRequirement(3, 380, 1, true));
      AdvancedCreationEntry torch = createAdvancedEntry(1010, 153, 23, 138, false, false, 0.0F, true, false, CreationCategories.LIGHTS_AND_LAMPS);
      torch.addRequirement(new CreationRequirement(1, 479, 1, true));
      AdvancedCreationEntry oven = createAdvancedEntry(1013, 132, 130, 178, false, false, 0.0F, true, true, CreationCategories.FIRE);
      oven.addRequirement(new CreationRequirement(1, 132, 10, true));
      oven.addRequirement(new CreationRequirement(2, 130, 10, true));
      AdvancedCreationEntry forge = createAdvancedEntry(1013, 132, 130, 180, false, false, 0.0F, true, true, CreationCategories.FIRE);
      forge.addRequirement(new CreationRequirement(1, 132, 10, true));
      forge.addRequirement(new CreationRequirement(2, 130, 10, true));
      AdvancedCreationEntry colossus = createAdvancedEntry(1013, 519, 130, 518, false, false, 0.0F, true, true, CreationCategories.STATUES);
      colossus.addRequirement(new CreationRequirement(1, 519, 1999, true));
      colossus.addRequirement(new CreationRequirement(2, 130, 1999, true));
      colossus.setIsEpicBuildMissionTarget(false);
      AdvancedCreationEntry pylon = createAdvancedEntry(1013, 406, 130, 713, false, false, 0.0F, true, true, CreationCategories.EPIC);
      pylon.addRequirement(new CreationRequirement(1, 406, 100, true));
      pylon.addRequirement(new CreationRequirement(2, 130, 1999, true));
      pylon.addRequirement(new CreationRequirement(3, 132, 1000, true));
      pylon.addRequirement(new CreationRequirement(4, 221, 1000, true));
      pylon.isOnlyCreateEpicTargetMission = true;
      AdvancedCreationEntry shrine = createAdvancedEntry(1005, 406, 130, 712, false, false, 0.0F, true, true, CreationCategories.EPIC);
      shrine.addRequirement(new CreationRequirement(1, 406, 10, true));
      shrine.addRequirement(new CreationRequirement(2, 22, 100, true));
      shrine.addRequirement(new CreationRequirement(3, 218, 10, true));
      shrine.addRequirement(new CreationRequirement(4, 221, 100, true));
      shrine.addRequirement(new CreationRequirement(5, 502, 10, true));
      shrine.isOnlyCreateEpicTargetMission = true;
      AdvancedCreationEntry temple = createAdvancedEntry(1013, 406, 130, 715, false, false, 0.0F, true, true, CreationCategories.EPIC);
      temple.addRequirement(new CreationRequirement(1, 406, 10, true));
      temple.addRequirement(new CreationRequirement(2, 130, 100, true));
      temple.addRequirement(new CreationRequirement(3, 132, 1000, true));
      temple.addRequirement(new CreationRequirement(4, 223, 100, true));
      temple.addRequirement(new CreationRequirement(5, 504, 10, true));
      temple.isOnlyCreateEpicTargetMission = true;
      AdvancedCreationEntry obelisk = createAdvancedEntry(1013, 132, 130, 714, false, false, 0.0F, true, true, CreationCategories.EPIC);
      obelisk.addRequirement(new CreationRequirement(1, 132, 1000, true));
      obelisk.addRequirement(new CreationRequirement(2, 130, 1000, true));
      obelisk.addRequirement(new CreationRequirement(3, 223, 100, true));
      obelisk.isOnlyCreateEpicTargetMission = true;
      AdvancedCreationEntry pillarDecoration = createAdvancedEntry(1013, 132, 130, 736, false, false, 0.0F, true, true, CreationCategories.DECORATION);
      pillarDecoration.addRequirement(new CreationRequirement(1, 132, 50, true));
      pillarDecoration.addRequirement(new CreationRequirement(2, 130, 50, true));
      AdvancedCreationEntry pillar = createAdvancedEntry(1013, 132, 130, 717, false, false, 0.0F, true, true, CreationCategories.EPIC);
      pillar.addRequirement(new CreationRequirement(1, 132, 100, true));
      pillar.addRequirement(new CreationRequirement(2, 130, 100, true));
      pillar.addRequirement(new CreationRequirement(3, 439, 10, true));
      pillar.isOnlyCreateEpicTargetMission = true;
      AdvancedCreationEntry spiritgate = createAdvancedEntry(1013, 132, 130, 716, false, false, 0.0F, true, true, CreationCategories.EPIC);
      spiritgate.addRequirement(new CreationRequirement(1, 132, 1000, true));
      spiritgate.addRequirement(new CreationRequirement(2, 130, 1000, true));
      spiritgate.addRequirement(new CreationRequirement(3, 44, 1000, true));
      spiritgate.isOnlyCreateEpicTargetMission = true;
      AdvancedCreationEntry sbench = createAdvancedEntry(1013, 132, 406, 404, false, false, 0.0F, true, true, CreationCategories.FURNITURE);
      sbench.addRequirement(new CreationRequirement(1, 132, 1, true));
      sbench.addRequirement(new CreationRequirement(2, 130, 2, true));
      AdvancedCreationEntry coff = createAdvancedEntry(1013, 132, 406, 407, false, false, 0.0F, true, true, CreationCategories.STORAGE);
      coff.addRequirement(new CreationRequirement(1, 132, 4, true));
      coff.addRequirement(new CreationRequirement(2, 130, 4, true));
      coff.addRequirement(new CreationRequirement(3, 406, 3, true));
      AdvancedCreationEntry ropetool = createAdvancedEntry(10044, 22, 23, 320, false, false, 0.0F, true, false, CreationCategories.TOOLS);
      ropetool.addRequirement(new CreationRequirement(1, 217, 3, true));
      AdvancedCreationEntry wheelSmall = createAdvancedEntry(10044, 22, 23, 187, false, false, 0.0F, true, false, CreationCategories.CART_PARTS);
      wheelSmall.addRequirement(new CreationRequirement(1, 22, 2, true));
      wheelSmall.addRequirement(new CreationRequirement(2, 23, 1, true));
      AdvancedCreationEntry wheelAxlSmall = createAdvancedEntry(10044, 23, 187, 191, false, true, 0.0F, true, false, CreationCategories.CART_PARTS);
      wheelAxlSmall.addRequirement(new CreationRequirement(1, 187, 1, true));
      AdvancedCreationEntry cartSmall = createAdvancedEntry(10044, 22, 191, 186, false, false, 0.0F, true, true, CreationCategories.CARTS);
      cartSmall.addRequirement(new CreationRequirement(1, 22, 5, true));
      cartSmall.addRequirement(new CreationRequirement(2, 23, 2, true));
      cartSmall.addRequirement(new CreationRequirement(3, 218, 2, true));
      AdvancedCreationEntry cartLarge = createAdvancedEntry(10044, 22, 191, 539, false, false, 0.0F, true, true, CreationCategories.CARTS);
      cartLarge.addRequirement(new CreationRequirement(1, 22, 15, true));
      cartLarge.addRequirement(new CreationRequirement(2, 23, 2, true));
      cartLarge.addRequirement(new CreationRequirement(3, 218, 4, true));
      cartLarge.addRequirement(new CreationRequirement(4, 632, 1, true));
      AdvancedCreationEntry catapultSmall = createAdvancedEntry(10044, 23, 9, 445, false, false, 0.0F, true, true, CreationCategories.WARMACHINES);
      catapultSmall.addRequirement(new CreationRequirement(1, 319, 5, true));
      catapultSmall.addRequirement(new CreationRequirement(2, 191, 2, true));
      catapultSmall.addRequirement(new CreationRequirement(3, 9, 6, true));
      createSimpleEntry(10011, 64, 46, 1126, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
      AdvancedCreationEntry batteringRam = createAdvancedEntry(10044, 860, 217, 1125, false, false, 0.0F, true, true, CreationCategories.WARMACHINES);
      batteringRam.addRequirement(new CreationRequirement(1, 860, 9, true));
      batteringRam.addRequirement(new CreationRequirement(2, 217, 4, true));
      batteringRam.addRequirement(new CreationRequirement(3, 319, 2, true));
      batteringRam.addRequirement(new CreationRequirement(4, 191, 2, true));
      batteringRam.addRequirement(new CreationRequirement(5, 9, 3, true));
      batteringRam.addRequirement(new CreationRequirement(6, 1126, 1, true));
      AdvancedCreationEntry joist = createAdvancedEntry(1005, 23, 9, 429, false, false, 0.0F, true, true, CreationCategories.MINE_DOORS);
      joist.addRequirement(new CreationRequirement(1, 188, 2, true));
      joist.addRequirement(new CreationRequirement(2, 9, 1, true));
      joist.addRequirement(new CreationRequirement(3, 23, 3, true));
      AdvancedCreationEntry floor = createAdvancedEntry(1005, 217, 22, 495, false, false, 0.0F, true, true, CreationCategories.CONSTRUCTION_MATERIAL);
      floor.addRequirement(new CreationRequirement(1, 22, 4, true));
      AdvancedCreationEntry minedoor = createAdvancedEntry(1005, 217, 22, 592, false, false, 0.0F, true, true, CreationCategories.MINE_DOORS);
      minedoor.addRequirement(new CreationRequirement(1, 22, 20, true));
      minedoor.addRequirement(new CreationRequirement(2, 217, 1, true));
      minedoor.addRequirement(new CreationRequirement(3, 167, 1, true));
      AdvancedCreationEntry minedoorst = createAdvancedEntry(10015, 167, 597, 596, false, false, 0.0F, true, true, CreationCategories.MINE_DOORS);
      minedoorst.addRequirement(new CreationRequirement(1, 597, 9, true));
      minedoorst.addRequirement(new CreationRequirement(2, 131, 50, true));
      AdvancedCreationEntry minedoors = createAdvancedEntry(10015, 167, 598, 595, false, false, 0.0F, true, true, CreationCategories.MINE_DOORS);
      minedoors.addRequirement(new CreationRequirement(1, 598, 11, true));
      minedoors.addRequirement(new CreationRequirement(2, 131, 50, true));
      AdvancedCreationEntry minedoorg = createAdvancedEntry(10015, 167, 599, 594, false, false, 0.0F, true, true, CreationCategories.MINE_DOORS);
      minedoorg.addRequirement(new CreationRequirement(1, 599, 11, true));
      minedoorg.addRequirement(new CreationRequirement(2, 131, 50, true));
      AdvancedCreationEntry cheeseDrill = createAdvancedEntry(10044, 309, 23, 65, false, false, 0.0F, true, false, CreationCategories.TOOLS);
      cheeseDrill.addRequirement(new CreationRequirement(1, 22, 5, true));
      cheeseDrill.addRequirement(new CreationRequirement(2, 266, 2, true));
      cheeseDrill.addRequirement(new CreationRequirement(3, 218, 1, true));
      AdvancedCreationEntry fruitPress = createAdvancedEntry(10044, 23, 22, 413, false, false, 0.0F, true, false, CreationCategories.TOOLS);
      fruitPress.addRequirement(new CreationRequirement(1, 22, 3, true));
      fruitPress.addRequirement(new CreationRequirement(2, 266, 2, true));
      fruitPress.addRequirement(new CreationRequirement(3, 218, 1, true));
      AdvancedCreationEntry papyrusPress = createAdvancedEntry(10044, 23, 22, 747, false, false, 0.0F, true, false, CreationCategories.TOOLS);
      papyrusPress.addRequirement(new CreationRequirement(1, 22, 3, true));
      papyrusPress.addRequirement(new CreationRequirement(2, 188, 2, true));
      papyrusPress.addRequirement(new CreationRequirement(3, 218, 2, true));
      AdvancedCreationEntry raftSmall = createAdvancedEntry(10082, 217, 22, 289, false, false, 0.0F, true, true, CreationCategories.SHIPBUILDING);
      raftSmall.addRequirement(new CreationRequirement(1, 22, 3, true));
      raftSmall.addRequirement(new CreationRequirement(2, 9, 4, true));
      raftSmall.addRequirement(new CreationRequirement(3, 217, 7, true));
      AdvancedCreationEntry archeryTarg = createAdvancedEntry(1005, 23, 23, 458, false, false, 0.0F, true, true, CreationCategories.COMBAT_TRAINING);
      archeryTarg.addRequirement(new CreationRequirement(1, 22, 4, true));
      archeryTarg.addRequirement(new CreationRequirement(2, 620, 4, true));
      archeryTarg.addRequirement(new CreationRequirement(3, 217, 7, true));
      archeryTarg.addRequirement(new CreationRequirement(4, 319, 1, true));
      AdvancedCreationEntry buildmarker = createAdvancedEntry(1005, 23, 23, 679, false, false, 0.0F, true, true, CreationCategories.DECORATION);
      buildmarker.addRequirement(new CreationRequirement(1, 22, 4, true));
      buildmarker.addRequirement(new CreationRequirement(2, 217, 1, true));
      AdvancedCreationEntry doll = createAdvancedEntry(1005, 217, 23, 321, false, false, 0.0F, true, false, CreationCategories.COMBAT_TRAINING);
      doll.addRequirement(new CreationRequirement(1, 22, 2, true));
      doll.addRequirement(new CreationRequirement(2, 23, 3, true));
      doll.addRequirement(new CreationRequirement(3, 33, 1, true));
      AdvancedCreationEntry barrell = createAdvancedEntry(10044, 188, 22, 190, false, false, 0.0F, true, true, CreationCategories.STORAGE);
      barrell.addRequirement(new CreationRequirement(1, 22, 4, true));
      barrell.addRequirement(new CreationRequirement(2, 188, 1, true));
      AdvancedCreationEntry hbarrell = createAdvancedEntry(10044, 188, 22, 576, false, false, 0.0F, true, true, CreationCategories.STORAGE);
      hbarrell.addRequirement(new CreationRequirement(1, 22, 24, true));
      hbarrell.addRequirement(new CreationRequirement(2, 188, 3, true));
      AdvancedCreationEntry oilbarrell = createAdvancedEntry(10044, 188, 22, 757, false, false, 0.0F, true, true, CreationCategories.STORAGE);
      oilbarrell.addRequirement(new CreationRequirement(1, 22, 24, true));
      oilbarrell.addRequirement(new CreationRequirement(2, 188, 3, true));
      AdvancedCreationEntry grains = createAdvancedEntry(1005, 188, 22, 661, false, false, 0.0F, true, true, CreationCategories.STORAGE);
      grains.addRequirement(new CreationRequirement(1, 22, 24, true));
      grains.addRequirement(new CreationRequirement(2, 217, 4, true));
      AdvancedCreationEntry bulks = createAdvancedEntry(1005, 188, 22, 662, false, false, 0.0F, true, true, CreationCategories.STORAGE);
      bulks.addRequirement(new CreationRequirement(1, 22, 24, true));
      bulks.addRequirement(new CreationRequirement(2, 217, 4, true));
      AdvancedCreationEntry trash = createAdvancedEntry(1005, 188, 22, 670, false, false, 0.0F, true, true, CreationCategories.STORAGE);
      trash.addRequirement(new CreationRequirement(1, 22, 10, true));
      trash.addRequirement(new CreationRequirement(2, 218, 2, true));
      AdvancedCreationEntry barrels = createAdvancedEntry(10044, 218, 22, 189, false, false, 0.0F, true, false, CreationCategories.STORAGE);
      barrels.addRequirement(new CreationRequirement(1, 22, 4, true));
      AdvancedCreationEntry wineBarrel = createAdvancedEntry(10044, 218, 22, 768, false, false, 0.0F, true, true, CreationCategories.STORAGE);
      wineBarrel.addRequirement(new CreationRequirement(1, 22, 4, true));
      wineBarrel.addRequirement(new CreationRequirement(2, 188, 1, true));
      AdvancedCreationEntry buckets = createAdvancedEntry(10044, 218, 22, 421, false, false, 0.0F, true, false, CreationCategories.STORAGE);
      buckets.addRequirement(new CreationRequirement(1, 22, 4, true));
      AdvancedCreationEntry dredge = createAdvancedEntry(10015, 213, 319, 581, false, false, 0.0F, true, false, CreationCategories.TOOLS);
      dredge.addRequirement(new CreationRequirement(1, 582, 4, true));
      AdvancedCreationEntry crows = createAdvancedEntry(10044, 188, 22, 583, false, false, 0.0F, true, true, CreationCategories.SHIPBUILDING);
      crows.addRequirement(new CreationRequirement(1, 22, 12, true));
      crows.addRequirement(new CreationRequirement(2, 188, 3, true));
      AdvancedCreationEntry chests = createAdvancedEntry(10044, 218, 22, 192, false, false, 0.0F, true, false, CreationCategories.STORAGE);
      chests.addRequirement(new CreationRequirement(1, 22, 3, true));
      AdvancedCreationEntry signp = createAdvancedEntry(10044, 218, 22, 208, false, false, 0.0F, true, false, CreationCategories.SIGNS);
      signp.addRequirement(new CreationRequirement(1, 23, 1, true));
      AdvancedCreationEntry signl = createAdvancedEntry(10044, 218, 22, 209, false, false, 0.0F, true, false, CreationCategories.SIGNS);
      signl.addRequirement(new CreationRequirement(1, 23, 1, true));
      signl.addRequirement(new CreationRequirement(2, 22, 1, true));
      AdvancedCreationEntry signshop = createAdvancedEntry(10044, 218, 22, 656, false, false, 0.0F, true, false, CreationCategories.SIGNS);
      signshop.addRequirement(new CreationRequirement(1, 23, 2, true));
      signshop.addRequirement(new CreationRequirement(2, 22, 2, true));
      AdvancedCreationEntry signs = createAdvancedEntry(10044, 218, 22, 210, false, false, 0.0F, true, false, CreationCategories.SIGNS);
      signs.addRequirement(new CreationRequirement(1, 23, 1, true));
      AdvancedCreationEntry dale = createAdvancedEntry(10036, 36, 9, 74, false, false, 0.0F, true, true, CreationCategories.PRODUCTION);
      dale.addRequirement(new CreationRequirement(1, 9, 20, true));
      dale.addRequirement(new CreationRequirement(2, 26, 2, true));
      AdvancedCreationEntry loom = createAdvancedEntry(10044, 218, 22, 226, false, false, 0.0F, true, true, CreationCategories.TOOLS);
      loom.addRequirement(new CreationRequirement(1, 23, 10, true));
      loom.addRequirement(new CreationRequirement(2, 214, 10, true));
      loom.addRequirement(new CreationRequirement(3, 22, 2, true));
      AdvancedCreationEntry marketstall = createAdvancedEntry(10044, 217, 22, 580, false, false, 0.0F, true, true, CreationCategories.DECORATION);
      marketstall.addRequirement(new CreationRequirement(1, 22, 20, true));
      marketstall.addRequirement(new CreationRequirement(2, 213, 4, true));
      marketstall.addRequirement(new CreationRequirement(3, 23, 4, true));
      AdvancedCreationEntry squareTable = createAdvancedEntry(10044, 23, 22, 262, false, false, 0.0F, true, true, CreationCategories.FURNITURE);
      squareTable.addRequirement(new CreationRequirement(1, 22, 3, true));
      squareTable.addRequirement(new CreationRequirement(2, 23, 3, true));
      AdvancedCreationEntry roundTable = createAdvancedEntry(10044, 23, 22, 260, false, false, 0.0F, true, true, CreationCategories.FURNITURE);
      roundTable.addRequirement(new CreationRequirement(1, 22, 3, true));
      roundTable.addRequirement(new CreationRequirement(2, 23, 3, true));
      AdvancedCreationEntry diningTable = createAdvancedEntry(10044, 23, 22, 264, false, false, 0.0F, true, true, CreationCategories.FURNITURE);
      diningTable.addRequirement(new CreationRequirement(1, 22, 6, true));
      diningTable.addRequirement(new CreationRequirement(2, 23, 3, true));
      AdvancedCreationEntry stoolRound = createAdvancedEntry(10044, 23, 22, 261, false, false, 0.0F, true, false, CreationCategories.FURNITURE);
      stoolRound.addRequirement(new CreationRequirement(1, 22, 1, true));
      stoolRound.addRequirement(new CreationRequirement(2, 23, 1, true));
      AdvancedCreationEntry chair = createAdvancedEntry(10044, 22, 23, 263, false, false, 0.0F, true, true, CreationCategories.FURNITURE);
      chair.addRequirement(new CreationRequirement(1, 22, 2, true));
      chair.addRequirement(new CreationRequirement(2, 23, 2, true));
      AdvancedCreationEntry armchair = createAdvancedEntry(10044, 23, 22, 265, false, false, 0.0F, true, true, CreationCategories.FURNITURE);
      armchair.addRequirement(new CreationRequirement(1, 22, 2, true));
      armchair.addRequirement(new CreationRequirement(2, 23, 2, true));
      armchair.addRequirement(new CreationRequirement(3, 213, 1, true));
      AdvancedCreationEntry bed = createAdvancedEntry(10044, 482, 483, 484, false, false, 0.0F, true, true, CreationCategories.FURNITURE);
      bed.addRequirement(new CreationRequirement(1, 485, 1, true));
      bed.addRequirement(new CreationRequirement(2, 486, 2, true));
      bed.addRequirement(new CreationRequirement(3, 302, 3, true));
      AdvancedCreationEntry tentExplorer = createAdvancedEntry(10044, 23, 213, 863, false, false, 0.0F, true, true, CreationCategories.TENTS);
      tentExplorer.addRequirement(new CreationRequirement(1, 23, 8, true));
      tentExplorer.addRequirement(new CreationRequirement(2, 213, 8, true));
      tentExplorer.addRequirement(new CreationRequirement(3, 559, 6, true));
      tentExplorer.addRequirement(new CreationRequirement(4, 561, 6, true));
      AdvancedCreationEntry tentMilitary = createAdvancedEntry(10044, 23, 213, 864, false, false, 0.0F, true, true, CreationCategories.TENTS);
      tentMilitary.addRequirement(new CreationRequirement(1, 23, 10, true));
      tentMilitary.addRequirement(new CreationRequirement(2, 213, 12, true));
      tentMilitary.addRequirement(new CreationRequirement(3, 559, 10, true));
      tentMilitary.addRequirement(new CreationRequirement(4, 561, 10, true));
      AdvancedCreationEntry pavilion = createAdvancedEntry(10044, 23, 213, 865, false, false, 0.0F, true, true, CreationCategories.TENTS);
      pavilion.addRequirement(new CreationRequirement(1, 23, 10, true));
      pavilion.addRequirement(new CreationRequirement(2, 213, 6, true));
      pavilion.addRequirement(new CreationRequirement(3, 559, 10, true));
      pavilion.addRequirement(new CreationRequirement(4, 561, 10, true));
      AdvancedCreationEntry bedframe = createAdvancedEntry(10044, 217, 22, 483, false, false, 0.0F, true, false, CreationCategories.CONSTRUCTION_MATERIAL);
      bedframe.addRequirement(new CreationRequirement(1, 22, 9, true));
      AdvancedCreationEntry bedheadboard = createAdvancedEntry(10044, 218, 22, 482, false, false, 0.0F, true, false, CreationCategories.CONSTRUCTION_MATERIAL);
      bedheadboard.addRequirement(new CreationRequirement(1, 22, 2, true));
      bedheadboard.addRequirement(new CreationRequirement(2, 23, 2, true));
      AdvancedCreationEntry bedfootboard = createAdvancedEntry(10044, 218, 22, 485, false, false, 0.0F, true, false, CreationCategories.CONSTRUCTION_MATERIAL);
      bedfootboard.addRequirement(new CreationRequirement(1, 22, 1, true));
      bedfootboard.addRequirement(new CreationRequirement(2, 23, 2, true));
      AdvancedCreationEntry altarwood = createAdvancedEntry(10044, 218, 22, 322, false, false, 0.0F, true, true, CreationCategories.ALTAR);
      altarwood.addRequirement(new CreationRequirement(1, 22, 8, true));
      altarwood.addRequirement(new CreationRequirement(2, 213, 2, true));
      altarwood.addRequirement(new CreationRequirement(3, 326, 1, true));
      AdvancedCreationEntry altarStone = createAdvancedEntry(1013, 130, 132, 323, false, false, 0.0F, true, true, CreationCategories.ALTAR);
      altarStone.addRequirement(new CreationRequirement(1, 132, 10, true));
      altarStone.addRequirement(new CreationRequirement(2, 130, 10, true));
      altarStone.addRequirement(new CreationRequirement(3, 326, 1, true));
      AdvancedCreationEntry chestl = createAdvancedEntry(10044, 188, 22, 184, false, false, 0.0F, true, true, CreationCategories.STORAGE);
      chestl.addRequirement(new CreationRequirement(1, 22, 4, true));
      chestl.addRequirement(new CreationRequirement(2, 188, 2, true));
      chestl.addRequirement(new CreationRequirement(3, 218, 1, true));
      AdvancedCreationEntry shieldsm = createAdvancedEntry(10014, 218, 22, 82, false, false, 0.0F, true, false, CreationCategories.SHIELDS);
      shieldsm.addRequirement(new CreationRequirement(1, 188, 1, true));
      AdvancedCreationEntry shieldTurtle = createAdvancedEntry(10014, 218, 898, 899, false, false, 0.0F, true, false, CreationCategories.SHIELDS);
      shieldTurtle.addRequirement(new CreationRequirement(1, 100, 2, true));
      AdvancedCreationEntry shieldmed = createAdvancedEntry(10014, 218, 22, 84, false, false, 0.0F, true, false, CreationCategories.SHIELDS);
      shieldmed.addRequirement(new CreationRequirement(1, 22, 1, true));
      shieldmed.addRequirement(new CreationRequirement(2, 188, 1, true));
      AdvancedCreationEntry shieldla = createAdvancedEntry(10014, 218, 22, 85, false, false, 0.0F, true, false, CreationCategories.SHIELDS);
      shieldla.addRequirement(new CreationRequirement(1, 22, 2, true));
      shieldla.addRequirement(new CreationRequirement(2, 188, 1, true));
      AdvancedCreationEntry towerStone = createAdvancedEntry(1013, 132, 130, 384, false, false, 0.0F, true, true, CreationCategories.TOWERS);
      towerStone.addRequirement(new CreationRequirement(1, 132, 500, true));
      towerStone.addRequirement(new CreationRequirement(2, 130, 500, true));
      towerStone.addRequirement(new CreationRequirement(3, 22, 100, true));
      AdvancedCreationEntry compass = createAdvancedEntry(1020, 418, 76, 480, false, false, 0.0F, true, false, CreationCategories.TOOLS);
      compass.addRequirement(new CreationRequirement(1, 215, 1, true));
      AdvancedCreationEntry puppetFo = createAdvancedEntry(10051, 215, 214, 640, false, true, 0.0F, false, false, CreationCategories.TOYS);
      puppetFo.addRequirement(new CreationRequirement(1, 213, 1, true));
      puppetFo.addRequirement(new CreationRequirement(2, 436, 1, true));
      puppetFo.addRequirement(new CreationRequirement(3, 23, 1, true));
      AdvancedCreationEntry puppetVynora = createAdvancedEntry(10051, 215, 214, 642, false, true, 0.0F, false, false, CreationCategories.TOYS);
      puppetVynora.addRequirement(new CreationRequirement(1, 213, 1, true));
      puppetVynora.addRequirement(new CreationRequirement(2, 364, 1, true));
      puppetVynora.addRequirement(new CreationRequirement(3, 23, 1, true));
      AdvancedCreationEntry puppetLibila = createAdvancedEntry(10051, 215, 214, 643, false, true, 0.0F, false, false, CreationCategories.TOYS);
      puppetLibila.addRequirement(new CreationRequirement(1, 213, 1, true));
      puppetLibila.addRequirement(new CreationRequirement(2, 204, 1, true));
      puppetLibila.addRequirement(new CreationRequirement(3, 23, 1, true));
      AdvancedCreationEntry puppetMagranon = createAdvancedEntry(10051, 215, 214, 641, false, true, 0.0F, false, false, CreationCategories.TOYS);
      puppetMagranon.addRequirement(new CreationRequirement(1, 213, 1, true));
      puppetMagranon.addRequirement(new CreationRequirement(2, 439, 1, true));
      puppetMagranon.addRequirement(new CreationRequirement(3, 23, 1, true));
      AdvancedCreationEntry epicPortal = createAdvancedEntry(1013, 9, 132, 732, false, false, 0.0F, true, true, CreationCategories.EPIC);
      epicPortal.addRequirement(new CreationRequirement(1, 132, 10, true));
      epicPortal.addRequirement(new CreationRequirement(2, 9, 3, true));
      AdvancedCreationEntry mailboxStone = createAdvancedEntry(1013, 132, 130, 511, false, false, 0.0F, true, true, CreationCategories.MAILBOXES);
      mailboxStone.addRequirement(new CreationRequirement(1, 132, 10, true));
      mailboxStone.addRequirement(new CreationRequirement(2, 130, 10, true));
      AdvancedCreationEntry mailboxStone2 = createAdvancedEntry(1013, 132, 130, 513, false, false, 0.0F, true, true, CreationCategories.MAILBOXES);
      mailboxStone2.addRequirement(new CreationRequirement(1, 132, 10, true));
      mailboxStone2.addRequirement(new CreationRequirement(2, 130, 10, true));
      mailboxStone2.addRequirement(new CreationRequirement(3, 213, 2, true));
      AdvancedCreationEntry mailboxWood = createAdvancedEntry(10044, 218, 22, 510, false, false, 0.0F, true, true, CreationCategories.MAILBOXES);
      mailboxWood.addRequirement(new CreationRequirement(1, 22, 8, true));
      mailboxWood.addRequirement(new CreationRequirement(2, 218, 2, true));
      AdvancedCreationEntry mailboxWood2 = createAdvancedEntry(10044, 218, 22, 512, false, false, 0.0F, true, true, CreationCategories.MAILBOXES);
      mailboxWood2.addRequirement(new CreationRequirement(1, 22, 8, true));
      mailboxWood2.addRequirement(new CreationRequirement(2, 218, 2, true));
      mailboxWood2.addRequirement(new CreationRequirement(3, 213, 2, true));
      AdvancedCreationEntry toolbelt = createAdvancedEntry(10017, 517, 102, 516, false, false, 0.0F, true, false, CreationCategories.CLOTHES);
      toolbelt.addRequirement(new CreationRequirement(1, 72, 1, true));
      toolbelt.addRequirement(new CreationRequirement(2, 213, 1, true));
      AdvancedCreationEntry trapSticks = createAdvancedEntry(1005, 217, 22, 610, false, false, 0.0F, true, false, CreationCategories.TRAPS);
      trapSticks.addRequirement(new CreationRequirement(1, 99, 10, true));
      trapSticks.addRequirement(new CreationRequirement(2, 22, 4, true));
      AdvancedCreationEntry trapPole = createAdvancedEntry(1005, 217, 22, 611, false, false, 0.0F, true, false, CreationCategories.TRAPS);
      trapPole.addRequirement(new CreationRequirement(1, 9, 1, true));
      trapPole.addRequirement(new CreationRequirement(2, 22, 4, true));
      trapPole.addRequirement(new CreationRequirement(3, 319, 1, true));
      AdvancedCreationEntry trapCorrosion = createAdvancedEntry(1011, 217, 22, 612, false, false, 0.0F, true, false, CreationCategories.TRAPS);
      trapCorrosion.addRequirement(new CreationRequirement(1, 78, 10, true));
      trapCorrosion.addRequirement(new CreationRequirement(2, 73, 4, true));
      trapCorrosion.addRequirement(new CreationRequirement(3, 457, 1, true));
      AdvancedCreationEntry trapAxe = createAdvancedEntry(1005, 217, 22, 613, false, false, 0.0F, true, false, CreationCategories.TRAPS);
      trapAxe.addRequirement(new CreationRequirement(1, 90, 1, true));
      trapAxe.addRequirement(new CreationRequirement(2, 22, 4, true));
      trapAxe.addRequirement(new CreationRequirement(3, 457, 1, true));
      trapAxe.addRequirement(new CreationRequirement(4, 609, 1, true));
      AdvancedCreationEntry trapDagger = createAdvancedEntry(1005, 217, 22, 614, false, false, 0.0F, true, false, CreationCategories.TRAPS);
      trapDagger.addRequirement(new CreationRequirement(1, 126, 10, true));
      trapDagger.addRequirement(new CreationRequirement(2, 22, 4, true));
      AdvancedCreationEntry trapNet = createAdvancedEntry(1014, 319, 319, 615, false, false, 0.0F, true, false, CreationCategories.TRAPS);
      trapNet.addRequirement(new CreationRequirement(1, 559, 5, true));
      trapNet.addRequirement(new CreationRequirement(2, 23, 1, true));
      AdvancedCreationEntry trapScythe = createAdvancedEntry(1013, 132, 130, 616, false, false, 0.0F, true, false, CreationCategories.TRAPS);
      trapScythe.addRequirement(new CreationRequirement(1, 132, 10, true));
      trapScythe.addRequirement(new CreationRequirement(2, 130, 10, true));
      trapScythe.addRequirement(new CreationRequirement(3, 270, 1, true));
      trapScythe.addRequirement(new CreationRequirement(4, 609, 1, true));
      AdvancedCreationEntry trapMan = createAdvancedEntry(10015, 582, 582, 617, false, false, 0.0F, true, false, CreationCategories.TRAPS);
      trapMan.addRequirement(new CreationRequirement(1, 609, 1, true));
      AdvancedCreationEntry trapBow = createAdvancedEntry(1005, 217, 22, 618, false, false, 0.0F, true, false, CreationCategories.TRAPS);
      trapBow.addRequirement(new CreationRequirement(1, 22, 1, true));
      trapBow.addRequirement(new CreationRequirement(2, 447, 1, true));
      trapBow.addRequirement(new CreationRequirement(3, 457, 1, true));
      trapBow.addRequirement(new CreationRequirement(4, 456, 1, true));
      AdvancedCreationEntry trapRope = createAdvancedEntry(1014, 319, 559, 619, false, false, 0.0F, true, false, CreationCategories.TRAPS);
      trapRope.addRequirement(new CreationRequirement(1, 23, 1, true));
      trapRope.addRequirement(new CreationRequirement(2, 457, 1, true));
      AdvancedCreationEntry villageBoard = createAdvancedEntry(1005, 23, 22, 835, false, false, 0.0F, true, true, CreationCategories.SIGNS);
      villageBoard.addRequirement(new CreationRequirement(1, 22, 2, true));
      villageBoard.addRequirement(new CreationRequirement(2, 23, 1, true));
      villageBoard.addRequirement(new CreationRequirement(3, 218, 3, true));
      AdvancedCreationEntry copperBrazier = createAdvancedEntry(
         10015, 838, 839, 841, false, false, 0.0F, true, true, 20, 30.0, CreationCategories.LIGHTS_AND_LAMPS
      );
      copperBrazier.addRequirement(new CreationRequirement(1, 838, 2, true));
      AdvancedCreationEntry marbleBrazierPillar = createAdvancedEntry(
         1013, 786, 492, 842, false, false, 0.0F, true, true, 20, 30.0, CreationCategories.LIGHTS_AND_LAMPS
      );
      marbleBrazierPillar.addRequirement(new CreationRequirement(1, 786, 49, true));
      marbleBrazierPillar.addRequirement(new CreationRequirement(2, 492, 49, true));
      marbleBrazierPillar.addRequirement(new CreationRequirement(3, 840, 1, true));
      AdvancedCreationEntry wagon = createAdvancedEntry(10044, 22, 191, 850, false, false, 0.0F, true, true, 0, 40.0, CreationCategories.CARTS);
      wagon.addRequirement(new CreationRequirement(1, 191, 1, true));
      wagon.addRequirement(new CreationRequirement(2, 22, 20, true));
      wagon.addRequirement(new CreationRequirement(3, 23, 4, true));
      wagon.addRequirement(new CreationRequirement(4, 218, 10, true));
      wagon.addRequirement(new CreationRequirement(5, 632, 2, true));
      wagon.addRequirement(new CreationRequirement(6, 486, 2, true));
      wagon.setIsEpicBuildMissionTarget(false);
      AdvancedCreationEntry smallCrate = createAdvancedEntry(1005, 22, 217, 851, false, false, 0.0F, true, true, 0, 10.0, CreationCategories.STORAGE);
      smallCrate.addRequirement(new CreationRequirement(1, 22, 10, true));
      AdvancedCreationEntry largeCrate = createAdvancedEntry(1005, 22, 217, 852, false, false, 0.0F, true, true, 0, 60.0, CreationCategories.STORAGE);
      largeCrate.addRequirement(new CreationRequirement(1, 22, 15, true));
      AdvancedCreationEntry shipCarrier = createAdvancedEntry(10044, 22, 191, 853, false, false, 0.0F, true, true, 0, 15.0, CreationCategories.CARTS);
      shipCarrier.addRequirement(new CreationRequirement(1, 191, 1, true));
      shipCarrier.addRequirement(new CreationRequirement(2, 22, 9, true));
      shipCarrier.addRequirement(new CreationRequirement(3, 23, 4, true));
      shipCarrier.addRequirement(new CreationRequirement(4, 218, 4, true));
      shipCarrier.addRequirement(new CreationRequirement(5, 632, 1, true));
      shipCarrier.addRequirement(new CreationRequirement(6, 9, 2, true));
      AdvancedCreationEntry creatureCarrier = createAdvancedEntry(10044, 22, 191, 1410, false, false, 0.0F, true, true, 0, 25.0, CreationCategories.CARTS);
      creatureCarrier.addRequirement(new CreationRequirement(1, 191, 1, true));
      creatureCarrier.addRequirement(new CreationRequirement(2, 22, 9, true));
      creatureCarrier.addRequirement(new CreationRequirement(3, 23, 4, true));
      creatureCarrier.addRequirement(new CreationRequirement(4, 218, 4, true));
      creatureCarrier.addRequirement(new CreationRequirement(5, 632, 1, true));
      creatureCarrier.addRequirement(new CreationRequirement(6, 9, 2, true));
      AdvancedCreationEntry colossusOfVynora = createAdvancedEntry(1013, 519, 130, 869, false, false, 0.0F, true, true, CreationCategories.STATUES);
      colossusOfVynora.setDeityRestriction(3);
      colossusOfVynora.addRequirement(new CreationRequirement(1, 519, 1999, true));
      colossusOfVynora.addRequirement(new CreationRequirement(2, 130, 1999, true));
      colossusOfVynora.addRequirement(new CreationRequirement(3, 599, 10, true));
      colossusOfVynora.setIsEpicBuildMissionTarget(false);
      AdvancedCreationEntry colossusOfMagranon = createAdvancedEntry(1013, 519, 130, 870, false, false, 0.0F, true, true, CreationCategories.STATUES);
      colossusOfMagranon.setDeityRestriction(2);
      colossusOfMagranon.addRequirement(new CreationRequirement(1, 519, 1999, true));
      colossusOfMagranon.addRequirement(new CreationRequirement(2, 130, 1999, true));
      colossusOfMagranon.addRequirement(new CreationRequirement(3, 598, 10, true));
      colossusOfMagranon.setIsEpicBuildMissionTarget(false);
      AdvancedCreationEntry bedsideTable = createAdvancedEntry(10044, 23, 22, 885, false, false, 0.0F, true, true, 0, 25.0, CreationCategories.FURNITURE);
      bedsideTable.addRequirement(new CreationRequirement(1, 22, 3, true));
      bedsideTable.addRequirement(new CreationRequirement(2, 23, 3, true));
      bedsideTable.addRequirement(new CreationRequirement(3, 218, 1, true));
      AdvancedCreationEntry colossusOfFo = createAdvancedEntry(1013, 519, 130, 907, false, false, 0.0F, true, true, CreationCategories.STATUES);
      colossusOfFo.setDeityRestriction(1);
      colossusOfFo.addRequirement(new CreationRequirement(1, 519, 1999, true));
      colossusOfFo.addRequirement(new CreationRequirement(2, 130, 1999, true));
      colossusOfFo.addRequirement(new CreationRequirement(3, 598, 10, true));
      colossusOfFo.setIsEpicBuildMissionTarget(false);
      AdvancedCreationEntry colossusOfLibila = createAdvancedEntry(1013, 519, 130, 916, false, false, 0.0F, true, true, CreationCategories.STATUES);
      colossusOfLibila.setDeityRestriction(4);
      colossusOfLibila.addRequirement(new CreationRequirement(1, 519, 1999, true));
      colossusOfLibila.addRequirement(new CreationRequirement(2, 130, 1999, true));
      colossusOfLibila.addRequirement(new CreationRequirement(3, 772, 10, true));
      colossusOfLibila.setIsEpicBuildMissionTarget(false);
      createSimpleEntry(10015, 185, 221, 897, false, true, 0.0F, false, false, CreationCategories.CONSTRUCTION_MATERIAL);
      AdvancedCreationEntry openFireplace = createAdvancedEntry(1013, 492, 132, 889, false, false, 0.0F, true, true, 0, 35.0, CreationCategories.FIRE);
      openFireplace.addRequirement(new CreationRequirement(1, 132, 9, true));
      openFireplace.addRequirement(new CreationRequirement(2, 492, 9, true));
      openFireplace.addRequirement(new CreationRequirement(3, 22, 2, true));
      AdvancedCreationEntry canopyBed = createAdvancedEntry(10044, 482, 483, 890, false, false, 0.0F, true, true, 0, 65.0, CreationCategories.FURNITURE);
      canopyBed.addRequirement(new CreationRequirement(1, 485, 1, true));
      canopyBed.addRequirement(new CreationRequirement(2, 22, 10, true));
      canopyBed.addRequirement(new CreationRequirement(3, 486, 8, true));
      canopyBed.addRequirement(new CreationRequirement(4, 302, 8, true));
      canopyBed.addRequirement(new CreationRequirement(5, 217, 2, true));
      canopyBed.addRequirement(new CreationRequirement(6, 218, 2, true));
      AdvancedCreationEntry woodenBench = createAdvancedEntry(10044, 218, 22, 891, false, false, 0.0F, true, true, 0, 35.0, CreationCategories.FURNITURE);
      woodenBench.addRequirement(new CreationRequirement(1, 22, 7, true));
      woodenBench.addRequirement(new CreationRequirement(2, 218, 3, true));
      woodenBench.addRequirement(new CreationRequirement(3, 23, 4, true));
      woodenBench.addRequirement(new CreationRequirement(4, 188, 2, true));
      AdvancedCreationEntry wardrobe = createAdvancedEntry(10044, 217, 22, 892, false, false, 0.0F, true, true, 0, 55.0, CreationCategories.FURNITURE);
      wardrobe.addRequirement(new CreationRequirement(1, 22, 11, true));
      wardrobe.addRequirement(new CreationRequirement(2, 23, 4, true));
      wardrobe.addRequirement(new CreationRequirement(3, 217, 3, true));
      wardrobe.addRequirement(new CreationRequirement(4, 218, 2, true));
      AdvancedCreationEntry woodenCoffer = createAdvancedEntry(10044, 218, 22, 893, false, false, 0.0F, true, true, 0, 24.0, CreationCategories.FURNITURE);
      woodenCoffer.addRequirement(new CreationRequirement(1, 22, 5, true));
      woodenCoffer.addRequirement(new CreationRequirement(2, 218, 2, true));
      AdvancedCreationEntry royalThrone = createAdvancedEntry(10044, 217, 22, 894, false, false, 0.0F, true, true, 0, 70.0, CreationCategories.FURNITURE);
      royalThrone.addRequirement(new CreationRequirement(1, 22, 7, true));
      royalThrone.addRequirement(new CreationRequirement(2, 23, 2, true));
      royalThrone.addRequirement(new CreationRequirement(3, 217, 3, true));
      royalThrone.addRequirement(new CreationRequirement(4, 218, 6, true));
      AdvancedCreationEntry washingBowl = createAdvancedEntry(10015, 64, 221, 895, false, true, 0.0F, false, true, 0, 30.0, CreationCategories.FURNITURE);
      washingBowl.setDepleteFromTarget(1500);
      washingBowl.addRequirement(new CreationRequirement(1, 897, 3, true));
      washingBowl.addRequirement(new CreationRequirement(2, 77, 1, true));
      AdvancedCreationEntry tripodTableSmall = createAdvancedEntry(10044, 22, 23, 896, false, false, 0.0F, true, true, 0, 30.0, CreationCategories.FURNITURE);
      tripodTableSmall.addRequirement(new CreationRequirement(1, 22, 1, true));
      tripodTableSmall.addRequirement(new CreationRequirement(2, 23, 1, true));
      tripodTableSmall.addRequirement(new CreationRequirement(3, 218, 2, true));
      AdvancedCreationEntry highBookshelf = createAdvancedEntry(10044, 217, 22, 911, false, false, 0.0F, true, true, 0, 35.0, CreationCategories.FURNITURE);
      highBookshelf.addRequirement(new CreationRequirement(1, 22, 7, true));
      highBookshelf.addRequirement(new CreationRequirement(2, 23, 2, true));
      highBookshelf.addRequirement(new CreationRequirement(3, 217, 3, true));
      AdvancedCreationEntry lowBookshelf = createAdvancedEntry(10044, 217, 22, 912, false, false, 0.0F, true, true, 0, 25.0, CreationCategories.FURNITURE);
      lowBookshelf.addRequirement(new CreationRequirement(1, 22, 3, true));
      lowBookshelf.addRequirement(new CreationRequirement(2, 23, 1, true));
      lowBookshelf.addRequirement(new CreationRequirement(3, 217, 1, true));
      AdvancedCreationEntry emptyHighBookshelf = createAdvancedEntry(
         10044, 217, 22, 1401, false, false, 0.0F, true, true, 0, 35.0, CreationCategories.FURNITURE
      );
      emptyHighBookshelf.addRequirement(new CreationRequirement(1, 22, 7, true));
      emptyHighBookshelf.addRequirement(new CreationRequirement(2, 23, 2, true));
      emptyHighBookshelf.addRequirement(new CreationRequirement(3, 217, 3, true));
      AdvancedCreationEntry emptyLowBookshelf = createAdvancedEntry(
         10044, 217, 22, 1400, false, false, 0.0F, true, true, 0, 25.0, CreationCategories.FURNITURE
      );
      emptyLowBookshelf.addRequirement(new CreationRequirement(1, 22, 3, true));
      emptyLowBookshelf.addRequirement(new CreationRequirement(2, 23, 1, true));
      emptyLowBookshelf.addRequirement(new CreationRequirement(3, 217, 1, true));
      AdvancedCreationEntry barTable = createAdvancedEntry(10044, 218, 22, 1402, false, false, 0.0F, true, true, 0, 25.0, CreationCategories.FURNITURE);
      barTable.addRequirement(new CreationRequirement(1, 22, 14, true));
      barTable.addRequirement(new CreationRequirement(2, 188, 2, true));
      barTable.addRequirement(new CreationRequirement(3, 218, 4, true));
      AdvancedCreationEntry fineHighChair = createAdvancedEntry(10044, 218, 22, 913, false, false, 0.0F, true, true, 0, 60.0, CreationCategories.FURNITURE);
      fineHighChair.addRequirement(new CreationRequirement(1, 22, 2, true));
      fineHighChair.addRequirement(new CreationRequirement(2, 213, 2, true));
      fineHighChair.addRequirement(new CreationRequirement(3, 218, 1, true));
      AdvancedCreationEntry highChair = createAdvancedEntry(10044, 218, 22, 914, false, false, 0.0F, true, true, 0, 50.0, CreationCategories.FURNITURE);
      highChair.addRequirement(new CreationRequirement(1, 22, 2, true));
      highChair.addRequirement(new CreationRequirement(2, 188, 2, true));
      highChair.addRequirement(new CreationRequirement(3, 218, 1, true));
      AdvancedCreationEntry pauperHighChair = createAdvancedEntry(10044, 218, 22, 915, false, false, 0.0F, true, true, 0, 40.0, CreationCategories.FURNITURE);
      pauperHighChair.addRequirement(new CreationRequirement(1, 22, 1, true));
      pauperHighChair.addRequirement(new CreationRequirement(2, 23, 3, true));
      pauperHighChair.addRequirement(new CreationRequirement(3, 218, 1, true));
      if (Features.Feature.AMPHORA.isEnabled()) {
         AdvancedCreationEntry kiln = createAdvancedEntry(1013, 132, 132, 1023, false, false, 0.0F, true, true, CreationCategories.FIRE);
         kiln.addRequirement(new CreationRequirement(1, 132, 18, true));
         kiln.addRequirement(new CreationRequirement(2, 130, 20, true));
         kiln.addRequirement(new CreationRequirement(3, 26, 2, true));
      }

      AdvancedCreationEntry smelter = createAdvancedEntry(1013, 132, 132, 1028, false, false, 0.0F, true, true, 0, 50.0, CreationCategories.FIRE);
      smelter.addRequirement(new CreationRequirement(1, 132, 48, true));
      smelter.addRequirement(new CreationRequirement(2, 130, 50, true));
      smelter.addRequirement(new CreationRequirement(3, 298, 5, true));
      AdvancedCreationEntry simpleDioptra = createAdvancedEntry(10015, 902, 904, 903, false, false, 0.0F, true, false, CreationCategories.TOOLS);
      simpleDioptra.addRequirement(new CreationRequirement(1, 480, 1, true));
      simpleDioptra.addRequirement(new CreationRequirement(2, 23, 3, true));
      AdvancedCreationEntry rangePole = createAdvancedEntry(1005, 711, 213, 901, false, false, 0.0F, true, false, CreationCategories.TOOLS);
      rangePole.addRequirement(new CreationRequirement(1, 213, 3, true));
      rangePole.addRequirement(new CreationRequirement(2, 439, 8, true));
      AdvancedCreationEntry pewpewdie = createAdvancedEntry(10044, 217, 22, 934, false, false, 0.0F, true, true, 0, 40.0, CreationCategories.WARMACHINES);
      pewpewdie.addRequirement(new CreationRequirement(1, 22, 4, true));
      pewpewdie.addRequirement(new CreationRequirement(2, 9, 1, true));
      pewpewdie.addRequirement(new CreationRequirement(3, 859, 3, true));
      pewpewdie.addRequirement(new CreationRequirement(4, 188, 2, true));
      AdvancedCreationEntry siegeShield = createAdvancedEntry(1005, 217, 22, 931, false, false, 0.0F, true, true, 0, 10.0, CreationCategories.WARMACHINES);
      siegeShield.addRequirement(new CreationRequirement(1, 22, 20, true));
      siegeShield.addRequirement(new CreationRequirement(2, 9, 2, true));
      AdvancedCreationEntry ballistaMount = createAdvancedEntry(10044, 217, 9, 933, false, false, 0.0F, true, true, 0, 60.0, CreationCategories.WARMACHINES);
      ballistaMount.addRequirement(new CreationRequirement(1, 22, 8, true));
      AdvancedCreationEntry barrier = createAdvancedEntry(1005, 217, 9, 938, false, false, 0.0F, true, true, 0, 5.0, CreationCategories.WARMACHINES);
      barrier.addRequirement(new CreationRequirement(1, 23, 21, true));
      createSimpleEntry(1032, 935, 23, 932, true, true, 0.0F, false, false, CreationCategories.FLETCHING);
      if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
         createMetallicEntries(10011, 64, 698, 935, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createMetallicEntries(10015, 185, 205, 1115, false, true, 0.0F, false, false, CreationCategories.TOOLS);
      } else {
         createSimpleEntry(10011, 64, 698, 935, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 694, 935, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 837, 935, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 205, 935, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 47, 935, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 45, 935, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 44, 935, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10011, 64, 223, 935, false, true, 10.0F, false, false, CreationCategories.WEAPON_HEADS);
         createSimpleEntry(10015, 185, 205, 1115, false, true, 0.0F, false, false, CreationCategories.TOOLS);
      }

      AdvancedCreationEntry ballista = createAdvancedEntry(10044, 933, 22, 936, false, false, 0.0F, true, true, 0, 60.0, CreationCategories.WARMACHINES);
      ballista.addRequirement(new CreationRequirement(1, 559, 4, true));
      ballista.addRequirement(new CreationRequirement(2, 897, 2, true));
      ballista.addRequirement(new CreationRequirement(3, 22, 4, true));
      ballista.addRequirement(new CreationRequirement(4, 218, 1, true));
      ballista.addRequirement(new CreationRequirement(5, 23, 1, true));
      AdvancedCreationEntry trebuchet = createAdvancedEntry(10044, 9, 9, 937, false, false, 0.0F, true, true, 0, 80.0, CreationCategories.WARMACHINES);
      trebuchet.addRequirement(new CreationRequirement(1, 9, 20, true));
      trebuchet.addRequirement(new CreationRequirement(2, 319, 5, true));
      trebuchet.addRequirement(new CreationRequirement(3, 191, 2, true));
      trebuchet.addRequirement(new CreationRequirement(4, 22, 40, true));
      AdvancedCreationEntry archeryTower = createAdvancedEntry(10044, 217, 22, 939, false, false, 0.0F, true, true, 0, 40.0, CreationCategories.WARMACHINES);
      archeryTower.addRequirement(new CreationRequirement(1, 22, 200, true));
      archeryTower.addRequirement(new CreationRequirement(2, 9, 20, true));
      archeryTower.addRequirement(new CreationRequirement(3, 860, 20, true));
      archeryTower.addRequirement(new CreationRequirement(4, 217, 6, true));
      AdvancedCreationEntry spinningWheel = createAdvancedEntry(10044, 187, 23, 922, false, false, 0.0F, true, true, 0, 30.0, CreationCategories.TOOLS);
      spinningWheel.addRequirement(new CreationRequirement(1, 23, 1, true));
      spinningWheel.addRequirement(new CreationRequirement(2, 22, 3, true));
      spinningWheel.addRequirement(new CreationRequirement(3, 218, 2, true));
      spinningWheel.addRequirement(new CreationRequirement(4, 153, 2, true));
      AdvancedCreationEntry loungeChair = createAdvancedEntry(10044, 22, 23, 923, false, false, 0.0F, true, true, 0, 40.0, CreationCategories.FURNITURE);
      loungeChair.addRequirement(new CreationRequirement(1, 22, 5, true));
      loungeChair.addRequirement(new CreationRequirement(2, 23, 3, true));
      loungeChair.addRequirement(new CreationRequirement(3, 218, 2, true));
      loungeChair.addRequirement(new CreationRequirement(4, 153, 4, true));
      AdvancedCreationEntry royalLoungeChaise = createAdvancedEntry(10044, 22, 23, 924, false, false, 0.0F, true, true, 0, 70.0, CreationCategories.FURNITURE);
      royalLoungeChaise.addRequirement(new CreationRequirement(1, 22, 4, true));
      royalLoungeChaise.addRequirement(new CreationRequirement(2, 23, 1, true));
      royalLoungeChaise.addRequirement(new CreationRequirement(3, 218, 6, true));
      royalLoungeChaise.addRequirement(new CreationRequirement(4, 926, 10, true));
      royalLoungeChaise.addRequirement(new CreationRequirement(5, 925, 4, true));
      AdvancedCreationEntry woodCupboard = createAdvancedEntry(10044, 218, 22, 927, false, false, 0.0F, true, true, 0, 40.0, CreationCategories.FURNITURE);
      woodCupboard.addRequirement(new CreationRequirement(1, 22, 4, true));
      woodCupboard.addRequirement(new CreationRequirement(2, 218, 2, true));
      woodCupboard.addRequirement(new CreationRequirement(3, 23, 2, true));
      woodCupboard.addRequirement(new CreationRequirement(4, 153, 2, true));
      AdvancedCreationEntry alchemyCupboard = createAdvancedEntry(10044, 218, 22, 1117, false, false, 0.0F, true, true, 0, 20.0, CreationCategories.FURNITURE);
      alchemyCupboard.addRequirement(new CreationRequirement(1, 22, 5, true));
      alchemyCupboard.addRequirement(new CreationRequirement(2, 218, 2, true));
      alchemyCupboard.addRequirement(new CreationRequirement(3, 23, 2, true));
      alchemyCupboard.addRequirement(new CreationRequirement(4, 1254, 5, true));
      alchemyCupboard.addRequirement(new CreationRequirement(5, 76, 10, true));
      AdvancedCreationEntry storageUnit = createAdvancedEntry(1005, 217, 22, 1119, false, false, 0.0F, true, true, 0, 30.0, CreationCategories.STORAGE);
      storageUnit.addRequirement(new CreationRequirement(1, 22, 14, true));
      storageUnit.addRequirement(new CreationRequirement(2, 217, 3, true));
      storageUnit.addRequirement(new CreationRequirement(3, 188, 4, true));
      storageUnit.addRequirement(new CreationRequirement(4, 561, 10, true));
      storageUnit.addRequirement(new CreationRequirement(5, 289, 3, true));
      AdvancedCreationEntry roundMarbleTable = createAdvancedEntry(10074, 787, 130, 928, false, false, 0.0F, true, true, 0, 60.0, CreationCategories.FURNITURE);
      roundMarbleTable.addRequirement(new CreationRequirement(1, 787, 1, true));
      roundMarbleTable.addRequirement(new CreationRequirement(2, 130, 3, true));
      AdvancedCreationEntry rectagularMarbleTable = createAdvancedEntry(
         10074, 787, 130, 929, false, false, 0.0F, true, true, 0, 50.0, CreationCategories.FURNITURE
      );
      rectagularMarbleTable.addRequirement(new CreationRequirement(1, 787, 1, true));
      rectagularMarbleTable.addRequirement(new CreationRequirement(2, 130, 4, true));
      AdvancedCreationEntry yellowWoolCap = createAdvancedEntry(10016, 128, 943, 944, true, false, 0.0F, true, false, 0, 20.0, CreationCategories.CLOTHES);
      yellowWoolCap.setUseTemplateWeight(true);
      yellowWoolCap.setColouringCreation(true);
      yellowWoolCap.addRequirement(new CreationRequirement(1, 47, 1, true));
      yellowWoolCap.addRequirement(new CreationRequirement(2, 128, 1, true));
      yellowWoolCap.addRequirement(new CreationRequirement(3, 439, 3, true));
      AdvancedCreationEntry greenWoolCap = createAdvancedEntry(10016, 128, 943, 945, true, false, 0.0F, true, false, 0, 20.0, CreationCategories.CLOTHES);
      greenWoolCap.setUseTemplateWeight(true);
      greenWoolCap.setColouringCreation(true);
      greenWoolCap.addRequirement(new CreationRequirement(1, 47, 1, true));
      AdvancedCreationEntry redWoolCap = createAdvancedEntry(10016, 128, 943, 946, true, false, 0.0F, true, false, 0, 20.0, CreationCategories.CLOTHES);
      redWoolCap.setUseTemplateWeight(true);
      redWoolCap.setColouringCreation(true);
      redWoolCap.addRequirement(new CreationRequirement(1, 439, 2, true));
      AdvancedCreationEntry blueWoolCap = createAdvancedEntry(10016, 128, 943, 947, true, false, 0.0F, true, false, 0, 20.0, CreationCategories.CLOTHES);
      blueWoolCap.setUseTemplateWeight(true);
      blueWoolCap.setColouringCreation(true);
      blueWoolCap.addRequirement(new CreationRequirement(1, 440, 2, true));
      AdvancedCreationEntry NIcoomonWoolHat = createAdvancedEntry(10016, 215, 926, 948, true, false, 5.0F, false, false, 0, 5.0, CreationCategories.CLOTHES);
      NIcoomonWoolHat.depleteSource = false;
      NIcoomonWoolHat.setDepleteFromTarget(200);
      NIcoomonWoolHat.addRequirement(new CreationRequirement(1, 925, 1, true));
      AdvancedCreationEntry CIcoomonWoolHat = createAdvancedEntry(10016, 216, 926, 948, true, false, 5.0F, false, false, 0, 5.0, CreationCategories.CLOTHES);
      CIcoomonWoolHat.depleteSource = false;
      CIcoomonWoolHat.setDepleteFromTarget(200);
      CIcoomonWoolHat.addRequirement(new CreationRequirement(1, 925, 1, true));
      AdvancedCreationEntry coomonWoolHatDark = createAdvancedEntry(10016, 128, 948, 949, true, false, 0.0F, true, false, 0, 20.0, CreationCategories.CLOTHES);
      coomonWoolHatDark.setUseTemplateWeight(true);
      coomonWoolHatDark.setColouringCreation(true);
      coomonWoolHatDark.addRequirement(new CreationRequirement(1, 46, 1, true));
      coomonWoolHatDark.addRequirement(new CreationRequirement(2, 437, 1, true));
      AdvancedCreationEntry coomonWoolHatBrown = createAdvancedEntry(10016, 128, 948, 950, true, false, 0.0F, true, false, 0, 20.0, CreationCategories.CLOTHES);
      coomonWoolHatBrown.setUseTemplateWeight(true);
      coomonWoolHatBrown.setColouringCreation(true);
      coomonWoolHatBrown.addRequirement(new CreationRequirement(1, 439, 3, true));
      coomonWoolHatBrown.addRequirement(new CreationRequirement(2, 47, 2, true));
      coomonWoolHatBrown.addRequirement(new CreationRequirement(3, 440, 1, true));
      coomonWoolHatBrown.addRequirement(new CreationRequirement(4, 128, 2, true));
      AdvancedCreationEntry coomonWoolHatGreen = createAdvancedEntry(10016, 128, 948, 951, true, false, 0.0F, true, false, 0, 20.0, CreationCategories.CLOTHES);
      coomonWoolHatGreen.setUseTemplateWeight(true);
      coomonWoolHatGreen.setColouringCreation(true);
      coomonWoolHatGreen.addRequirement(new CreationRequirement(1, 47, 1, true));
      coomonWoolHatGreen.addRequirement(new CreationRequirement(2, 128, 1, true));
      AdvancedCreationEntry coomonWoolHatRed = createAdvancedEntry(10016, 128, 948, 952, true, false, 0.0F, true, false, 0, 20.0, CreationCategories.CLOTHES);
      coomonWoolHatRed.setUseTemplateWeight(true);
      coomonWoolHatRed.setColouringCreation(true);
      coomonWoolHatRed.addRequirement(new CreationRequirement(1, 439, 2, true));
      coomonWoolHatRed.addRequirement(new CreationRequirement(2, 128, 2, true));
      AdvancedCreationEntry coomonWoolHatBlue = createAdvancedEntry(10016, 128, 948, 953, true, false, 0.0F, true, false, 0, 20.0, CreationCategories.CLOTHES);
      coomonWoolHatBlue.setUseTemplateWeight(true);
      coomonWoolHatBlue.setColouringCreation(true);
      coomonWoolHatBlue.addRequirement(new CreationRequirement(1, 440, 2, true));
      coomonWoolHatBlue.addRequirement(new CreationRequirement(2, 128, 2, true));
      AdvancedCreationEntry foresterWoolHatGreen = createAdvancedEntry(
         10016, 128, 954, 955, true, false, 0.0F, true, false, 0, 10.0, CreationCategories.CLOTHES
      );
      foresterWoolHatGreen.setUseTemplateWeight(true);
      foresterWoolHatGreen.setColouringCreation(true);
      foresterWoolHatGreen.addRequirement(new CreationRequirement(1, 47, 1, true));
      AdvancedCreationEntry foresterWoolHatDark = createAdvancedEntry(
         10016, 128, 954, 956, true, false, 0.0F, true, false, 0, 10.0, CreationCategories.CLOTHES
      );
      foresterWoolHatDark.setUseTemplateWeight(true);
      foresterWoolHatDark.setColouringCreation(true);
      foresterWoolHatDark.addRequirement(new CreationRequirement(1, 46, 1, true));
      foresterWoolHatDark.addRequirement(new CreationRequirement(2, 437, 1, true));
      AdvancedCreationEntry foresterWoolHatBlue = createAdvancedEntry(
         10016, 128, 954, 957, true, false, 0.0F, true, false, 0, 10.0, CreationCategories.CLOTHES
      );
      foresterWoolHatBlue.setUseTemplateWeight(true);
      foresterWoolHatBlue.setColouringCreation(true);
      foresterWoolHatBlue.addRequirement(new CreationRequirement(1, 440, 2, true));
      AdvancedCreationEntry foresterWoolHatRed = createAdvancedEntry(10016, 128, 954, 958, true, false, 0.0F, true, false, 0, 10.0, CreationCategories.CLOTHES);
      foresterWoolHatRed.setUseTemplateWeight(true);
      foresterWoolHatRed.setColouringCreation(true);
      foresterWoolHatRed.addRequirement(new CreationRequirement(1, 439, 2, true));
      AdvancedCreationEntry squireWoolCapGreen = createAdvancedEntry(10016, 128, 961, 962, true, false, 0.0F, true, false, 0, 25.0, CreationCategories.CLOTHES);
      squireWoolCapGreen.setUseTemplateWeight(true);
      squireWoolCapGreen.setColouringCreation(true);
      squireWoolCapGreen.addRequirement(new CreationRequirement(1, 47, 1, true));
      AdvancedCreationEntry squireWoolCapBlue = createAdvancedEntry(10016, 128, 961, 963, true, false, 0.0F, true, false, 0, 25.0, CreationCategories.CLOTHES);
      squireWoolCapBlue.setUseTemplateWeight(true);
      squireWoolCapBlue.setColouringCreation(true);
      squireWoolCapBlue.addRequirement(new CreationRequirement(1, 440, 2, true));
      AdvancedCreationEntry squireWoolCapBlack = createAdvancedEntry(10016, 128, 961, 964, true, false, 0.0F, true, false, 0, 25.0, CreationCategories.CLOTHES);
      squireWoolCapBlack.setUseTemplateWeight(true);
      squireWoolCapBlack.setColouringCreation(true);
      squireWoolCapBlack.addRequirement(new CreationRequirement(1, 46, 1, true));
      squireWoolCapBlack.addRequirement(new CreationRequirement(2, 437, 1, true));
      AdvancedCreationEntry squireWoolCapRed = createAdvancedEntry(10016, 128, 961, 965, true, false, 0.0F, true, false, 0, 25.0, CreationCategories.CLOTHES);
      squireWoolCapRed.setUseTemplateWeight(true);
      squireWoolCapRed.setColouringCreation(true);
      squireWoolCapRed.addRequirement(new CreationRequirement(1, 439, 2, true));
      AdvancedCreationEntry squireWoolCapYellow = createAdvancedEntry(
         10016, 128, 961, 966, true, false, 0.0F, true, false, 0, 25.0, CreationCategories.CLOTHES
      );
      squireWoolCapYellow.setUseTemplateWeight(true);
      squireWoolCapYellow.setColouringCreation(true);
      squireWoolCapYellow.addRequirement(new CreationRequirement(1, 47, 1, true));
      squireWoolCapYellow.addRequirement(new CreationRequirement(2, 128, 1, true));
      squireWoolCapYellow.addRequirement(new CreationRequirement(3, 439, 2, true));
      AdvancedCreationEntry grapeTrellis = createAdvancedEntry(10045, 918, 23, 920, false, false, 0.0F, true, true, 0, 10.0, CreationCategories.DECORATION);
      grapeTrellis.addRequirement(new CreationRequirement(1, 23, 5, true));
      grapeTrellis.addRequirement(new CreationRequirement(2, 218, 1, true));
      AdvancedCreationEntry grapeTrellis1 = createAdvancedEntry(10045, 266, 23, 920, false, false, 0.0F, true, true, 0, 10.0, CreationCategories.DECORATION);
      grapeTrellis1.setObjectSourceMaterial((byte)49);
      grapeTrellis1.addRequirement(new CreationRequirement(1, 23, 5, true));
      grapeTrellis1.addRequirement(new CreationRequirement(2, 218, 1, true));
      AdvancedCreationEntry ivyTrellis = createAdvancedEntry(10045, 917, 23, 919, false, false, 0.0F, true, true, 0, 10.0, CreationCategories.DECORATION);
      ivyTrellis.addRequirement(new CreationRequirement(1, 23, 5, true));
      ivyTrellis.addRequirement(new CreationRequirement(2, 218, 1, true));
      AdvancedCreationEntry roseTrellis = createAdvancedEntry(10045, 1017, 23, 1018, false, false, 0.0F, true, true, 0, 10.0, CreationCategories.DECORATION);
      roseTrellis.addRequirement(new CreationRequirement(1, 23, 5, true));
      roseTrellis.addRequirement(new CreationRequirement(2, 218, 1, true));
      AdvancedCreationEntry roseTrellis1 = createAdvancedEntry(10045, 266, 23, 1018, false, false, 0.0F, true, true, 0, 10.0, CreationCategories.DECORATION);
      roseTrellis1.setObjectSourceMaterial((byte)47);
      roseTrellis1.addRequirement(new CreationRequirement(1, 23, 5, true));
      roseTrellis1.addRequirement(new CreationRequirement(2, 218, 1, true));
      AdvancedCreationEntry hopsTrellis = createAdvancedEntry(10045, 1275, 23, 1274, false, false, 0.0F, true, true, 0, 10.0, CreationCategories.DECORATION);
      hopsTrellis.addRequirement(new CreationRequirement(1, 23, 5, true));
      hopsTrellis.addRequirement(new CreationRequirement(2, 218, 1, true));
      AdvancedCreationEntry tapestryStand = createAdvancedEntry(10044, 22, 23, 987, false, false, 0.0F, true, true, CreationCategories.RESOURCES);
      tapestryStand.addRequirement(new CreationRequirement(1, 23, 3, true));
      tapestryStand.addRequirement(new CreationRequirement(2, 218, 2, true));
      AdvancedCreationEntry tapestryP1 = createAdvancedEntry(10016, 926, 987, 988, false, false, 0.0F, true, true, CreationCategories.DECORATION);
      tapestryP1.addRequirement(new CreationRequirement(1, 926, 7, true));
      tapestryP1.addRequirement(new CreationRequirement(2, 925, 2, true));
      AdvancedCreationEntry tapestryP2 = createAdvancedEntry(10016, 926, 987, 989, false, false, 0.0F, true, true, CreationCategories.DECORATION);
      tapestryP2.addRequirement(new CreationRequirement(1, 926, 7, true));
      tapestryP2.addRequirement(new CreationRequirement(2, 925, 2, true));
      AdvancedCreationEntry tapestryP3 = createAdvancedEntry(10016, 926, 987, 990, false, false, 0.0F, true, true, CreationCategories.DECORATION);
      tapestryP3.addRequirement(new CreationRequirement(1, 926, 7, true));
      tapestryP3.addRequirement(new CreationRequirement(2, 925, 2, true));
      AdvancedCreationEntry tapestryM1 = createAdvancedEntry(10016, 926, 987, 991, false, false, 0.0F, true, true, CreationCategories.DECORATION);
      tapestryM1.addRequirement(new CreationRequirement(1, 926, 7, true));
      tapestryM1.addRequirement(new CreationRequirement(2, 925, 6, true));
      AdvancedCreationEntry tapestryM2 = createAdvancedEntry(10016, 926, 987, 992, false, false, 0.0F, true, true, CreationCategories.DECORATION);
      tapestryM2.addRequirement(new CreationRequirement(1, 926, 7, true));
      tapestryM2.addRequirement(new CreationRequirement(2, 925, 6, true));
      AdvancedCreationEntry tapestryM3 = createAdvancedEntry(10016, 926, 987, 993, false, false, 0.0F, true, true, CreationCategories.DECORATION);
      tapestryM3.addRequirement(new CreationRequirement(1, 926, 7, true));
      tapestryM3.addRequirement(new CreationRequirement(2, 925, 6, true));
      AdvancedCreationEntry tapestryFaeldray = createAdvancedEntry(10016, 926, 987, 994, false, false, 0.0F, true, true, CreationCategories.DECORATION);
      tapestryFaeldray.addRequirement(new CreationRequirement(1, 926, 7, true));
      tapestryFaeldray.addRequirement(new CreationRequirement(2, 925, 6, true));
      AdvancedCreationEntry swordDisplay = createAdvancedEntry(10044, 987, 21, 1030, false, false, 0.0F, true, true, CreationCategories.DECORATION);
      swordDisplay.addRequirement(new CreationRequirement(1, 21, 1, true));
      swordDisplay.addRequirement(new CreationRequirement(2, 86, 1, true));
      AdvancedCreationEntry axeDisplay = createAdvancedEntry(10044, 987, 90, 1031, false, false, 0.0F, true, true, CreationCategories.DECORATION);
      axeDisplay.addRequirement(new CreationRequirement(1, 90, 1, true));
      axeDisplay.addRequirement(new CreationRequirement(2, 86, 1, true));
      AdvancedCreationEntry marblePlanter = createAdvancedEntry(10074, 786, 786, 1001, false, false, 0.0F, true, true, CreationCategories.DECORATION);
      marblePlanter.addRequirement(new CreationRequirement(1, 786, 4, true));
      AdvancedCreationEntry bunchOfRopes = createAdvancedEntry(1014, 319, 319, 1029, false, false, 0.0F, true, false, CreationCategories.ROPES);
      bunchOfRopes.addRequirement(new CreationRequirement(1, 319, 2, true));
      createSimpleEntry(10016, 216, 926, 1071, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10016, 215, 926, 1071, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10016, 216, 213, 1107, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10016, 215, 213, 1107, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10016, 216, 213, 1106, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createSimpleEntry(10016, 215, 213, 1106, false, true, 0.0F, false, false, CreationCategories.CLOTHES);
      createNewClothing(1067, 110, 128, 47, 439, 102);
      createNewClothing(1068, 110, 128, 46, 437, 102);
      createNewClothing(1069, 110, 128, 439, 100, 102);
      createNewClothing(1070, 1107, 128, 439, 47, 440);
      createNewClothing(1072, 1107, 128, 46, 437);
      createNewClothing(1073, 1107, 128, 47);
      createNewClothing(1074, 1106, 128, 47, 439, 100);
      createNewClothing(1105, 1106, 128, 46, 437, 100);
      createNewClothing(1075, 1106, 128, 439, 100);
      createNewClothing(779, 1425, 128, 440);
      createNewClothing(111, 1426, 128, 439);
      createNewClothing(112, 1427, 128, 439);
      AdvancedCreationEntry beeSmoker = createAdvancedEntry(10015, 188, 46, 1243, false, false, 0.0F, true, false, 0, 30.0, CreationCategories.TOOLS);
      beeSmoker.addRequirement(new CreationRequirement(1, 188, 1, true));
      beeSmoker.addRequirement(new CreationRequirement(2, 72, 1, true));
      AdvancedCreationEntry hive = createAdvancedEntry(10044, 23, 22, 1175, false, false, 0.0F, true, true, 0, 25.0, CreationCategories.TOOLS);
      hive.addRequirement(new CreationRequirement(1, 22, 10, true));
      hive.addRequirement(new CreationRequirement(2, 23, 3, true));
      hive.addRequirement(new CreationRequirement(3, 218, 1, true));
      AdvancedCreationEntry waxkit = createAdvancedEntry(10091, 213, 1254, 1255, false, false, 0.0F, true, false, 0, 21.0, CreationCategories.COOKING_UTENSILS);
      waxkit.addRequirement(new CreationRequirement(1, 1269, 1, true));
      waxkit.addRequirement(new CreationRequirement(2, 214, 1, true));
      AdvancedCreationEntry skullMug = createAdvancedEntry(
         1012, 390, 1250, 1253, false, true, 0.0F, false, false, 0, 25.0, CreationCategories.COOKING_UTENSILS
      );
      skullMug.addRequirement(new CreationRequirement(1, 444, 2, true));
      skullMug.addRequirement(new CreationRequirement(2, 1254, 2, true));
      skullMug.setIsEpicBuildMissionTarget(false);
      AdvancedCreationEntry messageBoard = createAdvancedEntry(1005, 23, 22, 1271, false, false, 0.0F, true, true, CreationCategories.SIGNS);
      messageBoard.addRequirement(new CreationRequirement(1, 22, 5, true));
      messageBoard.addRequirement(new CreationRequirement(2, 23, 1, true));
      messageBoard.addRequirement(new CreationRequirement(3, 218, 3, true));
      AdvancedCreationEntry still = createAdvancedEntry(10015, 772, 772, 1178, false, false, 0.0F, true, true, CreationCategories.TOOLS);
      still.addRequirement(new CreationRequirement(1, 772, 5, true));
      still.addRequirement(new CreationRequirement(2, 131, 25, true));
      still.addRequirement(new CreationRequirement(3, 22, 1, true));
      still.addRequirement(new CreationRequirement(4, 188, 2, true));
      AdvancedCreationEntry larder = createAdvancedEntry(10044, 22, 23, 1277, false, false, 0.0F, true, true, CreationCategories.CONTAINER);
      larder.addRequirement(new CreationRequirement(1, 22, 19, true));
      larder.addRequirement(new CreationRequirement(2, 218, 3, true));
      larder.addRequirement(new CreationRequirement(3, 49, 5, true));
      larder.addRequirement(new CreationRequirement(4, 188, 2, true));
      larder.addRequirement(new CreationRequirement(5, 23, 3, true));
      createAdvancedEntry(10015, 1298, 1299, 1296, false, false, 0.0F, true, false, CreationCategories.CONTAINER)
         .addRequirement(new CreationRequirement(1, 76, 1, true))
         .addRequirement(new CreationRequirement(2, 131, 6, true))
         .addRequirement(new CreationRequirement(3, 444, 1, true))
         .addRequirement(new CreationRequirement(4, 100, 2, true))
         .addRequirement(new CreationRequirement(5, 1298, 2, true));
      AdvancedCreationEntry weaponsRackS = createAdvancedEntry(10044, 218, 22, 724, false, false, 0.0F, true, true, CreationCategories.STORAGE);
      weaponsRackS.addRequirement(new CreationRequirement(1, 561, 7, true));
      weaponsRackS.addRequirement(new CreationRequirement(2, 22, 9, true));
      AdvancedCreationEntry weaponsRackP = createAdvancedEntry(10044, 218, 22, 725, false, false, 0.0F, true, true, CreationCategories.STORAGE);
      weaponsRackP.addRequirement(new CreationRequirement(1, 561, 7, true));
      weaponsRackP.addRequirement(new CreationRequirement(2, 22, 9, true));
      AdvancedCreationEntry armourStand = createAdvancedEntry(10044, 218, 22, 759, false, false, 0.0F, true, true, CreationCategories.STORAGE);
      armourStand.addRequirement(new CreationRequirement(1, 561, 7, true));
      armourStand.addRequirement(new CreationRequirement(2, 22, 12, true));
      armourStand.addRequirement(new CreationRequirement(3, 23, 6, true));
      AdvancedCreationEntry weaponsRackB = createAdvancedEntry(10044, 218, 22, 758, false, false, 0.0F, true, true, CreationCategories.STORAGE);
      weaponsRackB.addRequirement(new CreationRequirement(1, 561, 7, true));
      weaponsRackB.addRequirement(new CreationRequirement(2, 22, 9, true));
      AdvancedCreationEntry thatchingTool = createAdvancedEntry(10044, 217, 22, 774, false, false, 0.0F, true, false, CreationCategories.TOOLS);
      thatchingTool.addRequirement(new CreationRequirement(1, 217, 6, true));
      thatchingTool.addRequirement(new CreationRequirement(2, 23, 1, true));
      AdvancedCreationEntry wineBarrelRack = createAdvancedEntry(1005, 217, 860, 1108, false, false, 0.0F, true, true, CreationCategories.CONTAINER);
      wineBarrelRack.addRequirement(new CreationRequirement(1, 860, 1, true));
      AdvancedCreationEntry smallBarrelRack = createAdvancedEntry(1005, 217, 860, 1109, false, false, 0.0F, true, true, CreationCategories.CONTAINER);
      smallBarrelRack.addRequirement(new CreationRequirement(1, 860, 1, true));
      AdvancedCreationEntry planterRack = createAdvancedEntry(1005, 217, 860, 1110, false, false, 0.0F, true, true, CreationCategories.CONTAINER);
      planterRack.addRequirement(new CreationRequirement(1, 22, 4, true));
      AdvancedCreationEntry amphoraRack = createAdvancedEntry(1005, 217, 860, 1111, false, false, 0.0F, true, true, CreationCategories.CONTAINER);
      amphoraRack.addRequirement(new CreationRequirement(1, 22, 4, true));
      AdvancedCreationEntry emptyShelf = createAdvancedEntry(1005, 217, 860, 1412, false, false, 0.0F, true, true, CreationCategories.DECORATION);
      emptyShelf.addRequirement(new CreationRequirement(1, 22, 4, true));
      createSimpleEntry(10091, 394, 748, 1269, false, true, 0.0F, false, false, CreationCategories.WRITING);
      createSimpleEntry(10091, 394, 1272, 1269, false, true, 0.0F, false, false, CreationCategories.WRITING);
      if (Features.Feature.HIGHWAYS.isEnabled()) {
         createSimpleEntry(10031, 97, 146, 1113, false, true, 10.0F, false, false, CreationCategories.HIGHWAY);
         createSimpleEntry(10031, 308, 1113, 1114, true, true, 0.0F, false, false, CreationCategories.HIGHWAY);
         AdvancedCreationEntry waystone = createAdvancedEntry(10031, 97, 146, 1112, false, false, 10.0F, false, false, 0, 21.0, CreationCategories.HIGHWAY);
         waystone.setDepleteFromTarget(5000);
         waystone.addRequirement(new CreationRequirement(1, 23, 1, true));
         waystone.addRequirement(new CreationRequirement(2, 480, 1, true));
      }

      createSimpleEntry(10091, 748, 214, 1409, true, true, 0.0F, false, false, CreationCategories.WRITING);
      createSimpleEntry(10091, 1272, 214, 1409, true, true, 0.0F, false, false, CreationCategories.WRITING);
      AdvancedCreationEntry archJournal = createAdvancedEntry(10017, 1409, 100, 1404, false, false, 0.0F, true, false, CreationCategories.WRITING);
      archJournal.addRequirement(new CreationRequirement(1, 100, 2, true));
      AdvancedCreationEntry almanac = createAdvancedEntry(10017, 1409, 100, 1127, false, false, 0.0F, true, false, CreationCategories.WRITING);
      almanac.addRequirement(new CreationRequirement(1, 100, 2, true));
      if (Features.Feature.WAGONER.isEnabled()) {
         AdvancedCreationEntry wagonerContainer = createAdvancedEntry(1005, 217, 860, 1309, false, false, 0.0F, true, true, CreationCategories.CONTAINER);
         wagonerContainer.addRequirement(new CreationRequirement(1, 860, 1, true));
      }

      AdvancedCreationEntry crateRack = createAdvancedEntry(1005, 217, 860, 1312, false, false, 0.0F, true, true, CreationCategories.CONTAINER);
      crateRack.addRequirement(new CreationRequirement(1, 860, 1, true));
      crateRack.addRequirement(new CreationRequirement(2, 22, 6, true));
      AdvancedCreationEntry bsbRack = createAdvancedEntry(1005, 217, 860, 1315, false, false, 0.0F, true, true, CreationCategories.CONTAINER);
      bsbRack.addRequirement(new CreationRequirement(1, 860, 1, true));
      bsbRack.addRequirement(new CreationRequirement(2, 22, 6, true));
      AdvancedCreationEntry bcu = createAdvancedEntry(10044, 217, 860, 1316, false, false, 0.0F, true, true, 35, 50.0, CreationCategories.CONTAINER);
      bcu.addRequirement(new CreationRequirement(1, 860, 2, true));
      bcu.addRequirement(new CreationRequirement(2, 22, 6, true));
      bcu.addRequirement(new CreationRequirement(3, 662, 4, true));
      if (Features.Feature.TRANSPORTABLE_CREATURES.isEnabled()) {
         AdvancedCreationEntry creatureCrate = createAdvancedEntry(1005, 22, 217, 1311, false, false, 0.0F, true, true, 0, 60.0, CreationCategories.STORAGE);
         creatureCrate.addRequirement(new CreationRequirement(1, 22, 24, true));
         creatureCrate.addRequirement(new CreationRequirement(2, 681, 6, true));
         creatureCrate.addRequirement(new CreationRequirement(3, 217, 16, true));
      }

      AdvancedCreationEntry tapestryEvening = createAdvancedEntry(10016, 926, 987, 1318, false, false, 0.0F, true, true, CreationCategories.DECORATION);
      tapestryEvening.addRequirement(new CreationRequirement(1, 926, 7, true));
      tapestryEvening.addRequirement(new CreationRequirement(2, 925, 6, true));
      AdvancedCreationEntry tapestryMclavin = createAdvancedEntry(10016, 926, 987, 1319, false, false, 0.0F, true, true, CreationCategories.DECORATION);
      tapestryMclavin.addRequirement(new CreationRequirement(1, 926, 7, true));
      tapestryMclavin.addRequirement(new CreationRequirement(2, 925, 6, true));
      AdvancedCreationEntry tapestryEhizellbob = createAdvancedEntry(10016, 926, 987, 1320, false, false, 0.0F, true, true, CreationCategories.DECORATION);
      tapestryEhizellbob.addRequirement(new CreationRequirement(1, 926, 7, true));
      tapestryEhizellbob.addRequirement(new CreationRequirement(2, 925, 6, true));
      createAdvancedMetalicEntry(
         10015,
         64,
         220,
         1341,
         false,
         true,
         0.0F,
         false,
         false,
         CreationCategories.TOOLS,
         new CreationRequirement(1, 790, 3, true),
         new CreationRequirement(2, 100, 1, true)
      );
      AdvancedCreationEntry keepNet = createAdvancedEntry(1014, 188, 319, 1342, false, false, 0.0F, true, false, CreationCategories.TOOLS);
      keepNet.addRequirement(new CreationRequirement(1, 188, 1, true));
      keepNet.addRequirement(new CreationRequirement(2, 214, 2, true));
      keepNet.addRequirement(new CreationRequirement(3, 23, 2, true));
      AdvancedCreationEntry fishingNet = createAdvancedEntry(10016, 214, 214, 1343, false, false, 0.0F, true, false, CreationCategories.TOOLS);
      fishingNet.addRequirement(new CreationRequirement(1, 23, 1, true));
      fishingNet.addRequirement(new CreationRequirement(2, 319, 1, true));
      createSimpleEntry(1005, 8, 23, 1344, false, true, 0.0F, false, false, CreationCategories.TOOLS);
      createSimpleEntry(1005, 685, 23, 1344, false, true, 0.0F, false, false, CreationCategories.TOOLS);
      createMetallicEntries(10043, 64, 46, 1345, false, true, 0.0F, false, false, CreationCategories.TOOLS);
      AdvancedCreationEntry fishingRod = createAdvancedEntry(10044, 1344, 23, 1346, false, false, 0.0F, true, false, CreationCategories.TOOLS);
      fishingRod.addRequirement(new CreationRequirement(1, 1345, 3, true));
      createSimpleEntry(10016, 14, 214, 1347, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(10016, 922, 1347, 1348, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(1014, 320, 1348, 1349, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(10016, 922, 1349, 1350, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(1014, 320, 1350, 1351, false, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(1005, 8, 9, 1356, false, true, 10.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(1005, 685, 9, 1356, false, true, 10.0F, false, false, CreationCategories.TOOL_PARTS);
      createMetallicEntries(10015, 64, 46, 1357, false, true, 1000.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(1020, 8, 1250, 1358, false, true, 900.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(10044, 8, 23, 1367, false, true, 10.0F, false, false, CreationCategories.TOOL_PARTS);
      createMetallicEntries(10043, 64, 46, 1368, false, true, 10.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(10043, 64, 205, 1369, false, true, 10.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(10043, 64, 837, 1369, false, true, 10.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(10043, 64, 694, 1369, false, true, 10.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(10043, 64, 698, 1369, false, true, 10.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(10015, 101, 444, 1370, true, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(10016, 1370, 213, 1371, true, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(10016, 1370, 926, 1371, true, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(1005, 99, 1367, 1372, true, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(10044, 101, 1367, 1373, true, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(10015, 1370, 1368, 1374, true, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
      createSimpleEntry(10043, 1371, 1369, 1375, true, true, 0.0F, false, false, CreationCategories.TOOL_PARTS);
      AdvancedCreationEntry rackRods = createAdvancedEntry(1005, 218, 22, 1393, false, false, 0.0F, true, true, CreationCategories.STORAGE);
      rackRods.addRequirement(new CreationRequirement(1, 561, 7, true));
      rackRods.addRequirement(new CreationRequirement(2, 22, 9, true));
      AdvancedCreationEntry bouy = createAdvancedEntry(1005, 189, 565, 1396, false, false, 0.0F, true, true, CreationCategories.DECORATION);
      bouy.addRequirement(new CreationRequirement(1, 23, 1, true));
      bouy.addRequirement(new CreationRequirement(2, 497, 1, true));
      AdvancedCreationEntry pearlNecklace = createAdvancedEntry(10043, 214, 1397, 1399, false, false, 0.0F, true, false, CreationCategories.DECORATION);
      pearlNecklace.addRequirement(new CreationRequirement(1, 1397, 9, true));
      pearlNecklace.addRequirement(new CreationRequirement(2, 1398, 1, true));
      if (Features.Feature.CHICKEN_COOPS.isEnabled()) {
         AdvancedCreationEntry chickenCoop = createAdvancedEntry(10044, 22, 217, 1432, false, false, 0.0F, true, true, 0, 50.0, CreationCategories.STORAGE);
         chickenCoop.addRequirement(new CreationRequirement(1, 217, 4, true));
         chickenCoop.addRequirement(new CreationRequirement(2, 22, 24, true));
         chickenCoop.addRequirement(new CreationRequirement(3, 23, 8, true));
         chickenCoop.addRequirement(new CreationRequirement(4, 189, 1, true));
      }

      logger.info("Initialising the CreationEntries took " + (float)(System.nanoTime() - start) / 1000000.0F + " millis.");
   }

   private static void createNewClothing(int aArticle, int aSource, int aTarget, int... components) {
      AdvancedCreationEntry article = createAdvancedEntry(
         10016, aSource, aTarget, aArticle, false, false, 0.0F, true, false, 0, 10.0, CreationCategories.CLOTHES
      );
      article.setColouringCreation(true);
      article.setFinalMaterial((byte)17);
      article.setUseTemplateWeight(true);
      int x = 1;

      for(int component : components) {
         article.addRequirement(new CreationRequirement(x++, component, 1, true));
      }
   }

   public static void createBoatEntries() {
      if (!entriesCreated) {
         entriesCreated = true;
         AdvancedCreationEntry lRowBoat = createAdvancedEntry(10082, 560, 560, 490, false, false, 0.0F, true, true, CreationCategories.SHIPS);
         lRowBoat.addRequirement(new CreationRequirement(1, 560, 1, true));
         lRowBoat.addRequirement(new CreationRequirement(2, 553, 1, true));
         lRowBoat.addRequirement(new CreationRequirement(3, 546, 50, true));
         lRowBoat.addRequirement(new CreationRequirement(4, 551, 50, true));
         lRowBoat.addRequirement(new CreationRequirement(5, 561, 50, true));
         lRowBoat.addRequirement(new CreationRequirement(6, 558, 1, true));
         lRowBoat.addRequirement(new CreationRequirement(7, 153, 10, true));
         lRowBoat.addRequirement(new CreationRequirement(8, 556, 2, true));
         lRowBoat.addRequirement(new CreationRequirement(9, 545, 4, true));
         lRowBoat.setIsEpicBuildMissionTarget(false);
         AdvancedCreationEntry lSailBoat = createAdvancedEntry(10082, 560, 560, 491, false, false, 0.0F, true, true, CreationCategories.SHIPS);
         lSailBoat.addRequirement(new CreationRequirement(1, 560, 1, true));
         lSailBoat.addRequirement(new CreationRequirement(2, 553, 1, true));
         lSailBoat.addRequirement(new CreationRequirement(3, 546, 50, true));
         lSailBoat.addRequirement(new CreationRequirement(4, 551, 50, true));
         lSailBoat.addRequirement(new CreationRequirement(5, 561, 50, true));
         lSailBoat.addRequirement(new CreationRequirement(6, 557, 2, true));
         lSailBoat.addRequirement(new CreationRequirement(7, 558, 1, true));
         lSailBoat.addRequirement(new CreationRequirement(8, 153, 10, true));
         lSailBoat.addRequirement(new CreationRequirement(9, 556, 2, true));
         lSailBoat.addRequirement(new CreationRequirement(10, 545, 4, true));
         lSailBoat.addRequirement(new CreationRequirement(11, 563, 1, true));
         lSailBoat.addRequirement(new CreationRequirement(12, 567, 4, true));
         lSailBoat.setIsEpicBuildMissionTarget(false);
         AdvancedCreationEntry lCorbita = createAdvancedEntry(10082, 560, 560, 541, false, false, 0.0F, true, true, CreationCategories.SHIPS);
         lCorbita.addRequirement(new CreationRequirement(1, 560, 2, true));
         lCorbita.addRequirement(new CreationRequirement(2, 553, 1, true));
         lCorbita.addRequirement(new CreationRequirement(3, 546, 200, true));
         lCorbita.addRequirement(new CreationRequirement(4, 551, 200, true));
         lCorbita.addRequirement(new CreationRequirement(5, 561, 400, true));
         lCorbita.addRequirement(new CreationRequirement(6, 564, 1, true));
         lCorbita.addRequirement(new CreationRequirement(7, 557, 8, true));
         lCorbita.addRequirement(new CreationRequirement(8, 558, 4, true));
         lCorbita.addRequirement(new CreationRequirement(9, 544, 2, true));
         lCorbita.addRequirement(new CreationRequirement(10, 566, 40, true));
         lCorbita.addRequirement(new CreationRequirement(11, 153, 50, true));
         lCorbita.addRequirement(new CreationRequirement(12, 556, 2, true));
         lCorbita.addRequirement(new CreationRequirement(13, 567, 10, true));
         lCorbita.addRequirement(new CreationRequirement(14, 584, 1, true));
         lCorbita.setIsEpicBuildMissionTarget(false);
         AdvancedCreationEntry lCog = createAdvancedEntry(10082, 560, 560, 540, false, false, 0.0F, true, true, CreationCategories.SHIPS);
         lCog.addRequirement(new CreationRequirement(1, 560, 2, true));
         lCog.addRequirement(new CreationRequirement(2, 553, 1, true));
         lCog.addRequirement(new CreationRequirement(3, 546, 300, true));
         lCog.addRequirement(new CreationRequirement(4, 551, 200, true));
         lCog.addRequirement(new CreationRequirement(5, 561, 400, true));
         lCog.addRequirement(new CreationRequirement(6, 585, 1, true));
         lCog.addRequirement(new CreationRequirement(7, 557, 8, true));
         lCog.addRequirement(new CreationRequirement(8, 558, 4, true));
         lCog.addRequirement(new CreationRequirement(9, 544, 1, true));
         lCog.addRequirement(new CreationRequirement(10, 566, 60, true));
         lCog.addRequirement(new CreationRequirement(11, 153, 50, true));
         lCog.addRequirement(new CreationRequirement(12, 556, 2, true));
         lCog.addRequirement(new CreationRequirement(13, 567, 10, true));
         lCog.setIsEpicBuildMissionTarget(false);
         AdvancedCreationEntry lKnarr = createAdvancedEntry(10082, 560, 560, 542, false, false, 0.0F, true, true, CreationCategories.SHIPS);
         lKnarr.addRequirement(new CreationRequirement(1, 560, 3, true));
         lKnarr.addRequirement(new CreationRequirement(2, 553, 1, true));
         lKnarr.addRequirement(new CreationRequirement(3, 546, 400, true));
         lKnarr.addRequirement(new CreationRequirement(4, 551, 200, true));
         lKnarr.addRequirement(new CreationRequirement(5, 561, 400, true));
         lKnarr.addRequirement(new CreationRequirement(6, 131, 200, true));
         lKnarr.addRequirement(new CreationRequirement(7, 586, 1, true));
         lKnarr.addRequirement(new CreationRequirement(8, 557, 8, true));
         lKnarr.addRequirement(new CreationRequirement(9, 558, 4, true));
         lKnarr.addRequirement(new CreationRequirement(10, 544, 1, true));
         lKnarr.addRequirement(new CreationRequirement(11, 566, 80, true));
         lKnarr.addRequirement(new CreationRequirement(12, 153, 100, true));
         lKnarr.addRequirement(new CreationRequirement(13, 556, 10, true));
         lKnarr.addRequirement(new CreationRequirement(14, 567, 10, true));
         lKnarr.setIsEpicBuildMissionTarget(false);
         AdvancedCreationEntry lCaravel = createAdvancedEntry(10082, 560, 560, 543, false, false, 0.0F, true, true, CreationCategories.SHIPS);
         lCaravel.addRequirement(new CreationRequirement(1, 560, 3, true));
         lCaravel.addRequirement(new CreationRequirement(2, 553, 1, true));
         lCaravel.addRequirement(new CreationRequirement(3, 546, 400, true));
         lCaravel.addRequirement(new CreationRequirement(4, 551, 300, true));
         lCaravel.addRequirement(new CreationRequirement(5, 561, 600, true));
         lCaravel.addRequirement(new CreationRequirement(6, 563, 1, true));
         lCaravel.addRequirement(new CreationRequirement(7, 557, 12, true));
         lCaravel.addRequirement(new CreationRequirement(8, 558, 8, true));
         lCaravel.addRequirement(new CreationRequirement(9, 544, 1, true));
         lCaravel.addRequirement(new CreationRequirement(10, 566, 80, true));
         lCaravel.addRequirement(new CreationRequirement(11, 548, 1, true));
         lCaravel.addRequirement(new CreationRequirement(12, 153, 150, true));
         lCaravel.addRequirement(new CreationRequirement(13, 556, 10, true));
         lCaravel.addRequirement(new CreationRequirement(14, 567, 10, true));
         lCaravel.addRequirement(new CreationRequirement(15, 587, 1, true));
         lCaravel.addRequirement(new CreationRequirement(16, 564, 1, true));
         lCaravel.addRequirement(new CreationRequirement(17, 584, 1, true));
         lCaravel.setIsEpicBuildMissionTarget(false);
      }
   }

   public static CreationEntry createSimpleEntry(
      int aPrimarySkill,
      int aObjectSource,
      int aObjectTarget,
      int aObjectCreated,
      boolean depleteSource,
      boolean depleteTarget,
      float aPercentageLost,
      boolean depleteEqually,
      boolean aCreateOnGround,
      CreationCategories aCategory
   ) {
      CreationEntry entry = new SimpleCreationEntry(
         aPrimarySkill,
         aObjectSource,
         aObjectTarget,
         aObjectCreated,
         depleteSource,
         depleteTarget,
         aPercentageLost,
         depleteEqually,
         aCreateOnGround,
         aCategory
      );
      CreationMatrix.getInstance().addCreationEntry(entry);
      return entry;
   }

   public static CreationEntry createMetallicEntries(
      int aPrimarySkill,
      int aObjectSource,
      int aObjectTarget,
      int aObjectCreated,
      boolean depleteSource,
      boolean depleteTarget,
      float aPercentageLost,
      boolean depleteEqually,
      boolean aCreateOnGround,
      CreationCategories aCategory
   ) {
      CreationEntry defaultEntry = createSimpleEntry(
         aPrimarySkill,
         aObjectSource,
         aObjectTarget,
         aObjectCreated,
         depleteSource,
         depleteTarget,
         aPercentageLost,
         depleteEqually,
         aCreateOnGround,
         aCategory
      );
      if (ItemFactory.isMetalLump(aObjectTarget)) {
         for(int targetTemplateId : ItemFactory.metalLumpList) {
            if (targetTemplateId != aObjectTarget) {
               createSimpleEntry(
                  aPrimarySkill,
                  aObjectSource,
                  targetTemplateId,
                  aObjectCreated,
                  depleteSource,
                  depleteTarget,
                  aPercentageLost,
                  depleteEqually,
                  aCreateOnGround,
                  aCategory
               );
            }
         }
      } else if (ItemFactory.isMetalLump(aObjectSource)) {
         for(int sourceTemplateId : ItemFactory.metalLumpList) {
            if (sourceTemplateId != aObjectSource) {
               createSimpleEntry(
                  aPrimarySkill,
                  sourceTemplateId,
                  aObjectTarget,
                  aObjectCreated,
                  depleteSource,
                  depleteTarget,
                  aPercentageLost,
                  depleteEqually,
                  aCreateOnGround,
                  aCategory
               );
            }
         }
      }

      return defaultEntry;
   }

   public static CreationEntry createSimpleEntry(
      int aPrimarySkill,
      int aObjectSource,
      int aObjectTarget,
      int aObjectCreated,
      boolean depleteSource,
      boolean depleteTarget,
      float aPercentageLost,
      boolean depleteEqually,
      boolean aCreateOnGround,
      int aCustomCreationCutOff,
      double aMinimumSkill,
      CreationCategories aCategory
   ) {
      CreationEntry entry = new SimpleCreationEntry(
         aPrimarySkill,
         aObjectSource,
         aObjectTarget,
         aObjectCreated,
         depleteSource,
         depleteTarget,
         aPercentageLost,
         depleteEqually,
         aCreateOnGround,
         aCustomCreationCutOff,
         aMinimumSkill,
         aCategory
      );
      CreationMatrix.getInstance().addCreationEntry(entry);
      return entry;
   }

   public static CreationEntry createMetallicEntries(
      int aPrimarySkill,
      int aObjectSource,
      int aObjectTarget,
      int aObjectCreated,
      boolean depleteSource,
      boolean depleteTarget,
      float aPercentageLost,
      boolean depleteEqually,
      boolean aCreateOnGround,
      int aCustomCreationCutOff,
      double aMinimumSkill,
      CreationCategories aCategory
   ) {
      CreationEntry defaultEntry = createSimpleEntry(
         aPrimarySkill,
         aObjectSource,
         aObjectTarget,
         aObjectCreated,
         depleteSource,
         depleteTarget,
         aPercentageLost,
         depleteEqually,
         aCreateOnGround,
         aCustomCreationCutOff,
         aMinimumSkill,
         aCategory
      );
      if (ItemFactory.isMetalLump(aObjectTarget)) {
         for(int targetTemplateId : ItemFactory.metalLumpList) {
            if (targetTemplateId != aObjectTarget) {
               createSimpleEntry(
                  aPrimarySkill,
                  aObjectSource,
                  targetTemplateId,
                  aObjectCreated,
                  depleteSource,
                  depleteTarget,
                  aPercentageLost,
                  depleteEqually,
                  aCreateOnGround,
                  aCustomCreationCutOff,
                  aMinimumSkill,
                  aCategory
               );
            }
         }
      } else if (ItemFactory.isMetalLump(aObjectSource)) {
         for(int sourceTemplateId : ItemFactory.metalLumpList) {
            if (sourceTemplateId != aObjectSource) {
               createSimpleEntry(
                  aPrimarySkill,
                  sourceTemplateId,
                  aObjectTarget,
                  aObjectCreated,
                  depleteSource,
                  depleteTarget,
                  aPercentageLost,
                  depleteEqually,
                  aCreateOnGround,
                  aCustomCreationCutOff,
                  aMinimumSkill,
                  aCategory
               );
            }
         }
      }

      return defaultEntry;
   }

   public static AdvancedCreationEntry createAdvancedEntry(
      int primarySkill,
      int objectSource,
      int objectTarget,
      int objectCreated,
      boolean depleteSource,
      boolean depleteTarget,
      float percentageLost,
      boolean destroyBoth,
      boolean createOnGround,
      CreationCategories category
   ) {
      AdvancedCreationEntry entry = new AdvancedCreationEntry(
         primarySkill, objectSource, objectTarget, objectCreated, depleteSource, depleteTarget, percentageLost, destroyBoth, createOnGround, category
      );
      CreationMatrix.getInstance().addCreationEntry(entry);
      return entry;
   }

   public static AdvancedCreationEntry createAdvancedEntry(
      int primarySkill,
      int objectSource,
      int objectTarget,
      int objectCreated,
      boolean destroyTarget,
      boolean useCapacity,
      float percentageLost,
      boolean destroyBoth,
      boolean createOnGround,
      int customCutOffChance,
      double aMinimumSkill,
      CreationCategories category
   ) {
      AdvancedCreationEntry entry = new AdvancedCreationEntry(
         primarySkill,
         objectSource,
         objectTarget,
         objectCreated,
         destroyTarget,
         useCapacity,
         percentageLost,
         destroyBoth,
         createOnGround,
         customCutOffChance,
         aMinimumSkill,
         category
      );
      CreationMatrix.getInstance().addCreationEntry(entry);
      return entry;
   }

   public static AdvancedCreationEntry createAdvancedMetalicEntry(
      int primarySkill,
      int objectSource,
      int objectTarget,
      int objectCreated,
      boolean depleteSource,
      boolean depleteTarget,
      float percentageLost,
      boolean destroyBoth,
      boolean createOnGround,
      CreationCategories category,
      CreationRequirement... extras
   ) {
      AdvancedCreationEntry defaultEntry = createAdvancedEntry(
         primarySkill, objectSource, objectTarget, objectCreated, depleteSource, depleteTarget, percentageLost, destroyBoth, createOnGround, category
      );

      for(CreationRequirement extra : extras) {
         defaultEntry.addRequirement(extra);
      }

      if (ItemFactory.isMetalLump(objectTarget)) {
         for(int targetTemplateId : ItemFactory.metalLumpList) {
            if (targetTemplateId != objectTarget) {
               AdvancedCreationEntry metalEntry = createAdvancedEntry(
                  primarySkill,
                  objectSource,
                  targetTemplateId,
                  objectCreated,
                  depleteSource,
                  depleteTarget,
                  percentageLost,
                  destroyBoth,
                  createOnGround,
                  category
               );

               for(CreationRequirement extra : extras) {
                  metalEntry.addRequirement(extra);
               }
            }
         }
      } else if (ItemFactory.isMetalLump(objectSource)) {
         for(int sourceTemplateId : ItemFactory.metalLumpList) {
            if (sourceTemplateId != objectSource) {
               AdvancedCreationEntry metalEntry = createAdvancedEntry(
                  primarySkill,
                  sourceTemplateId,
                  objectTarget,
                  objectCreated,
                  depleteSource,
                  depleteTarget,
                  percentageLost,
                  destroyBoth,
                  createOnGround,
                  category
               );

               for(CreationRequirement extra : extras) {
                  metalEntry.addRequirement(extra);
               }
            }
         }
      }

      return defaultEntry;
   }

   public static AdvancedCreationEntry createAdvancedMetalicEntry(
      int primarySkill,
      int objectSource,
      int objectTarget,
      int objectCreated,
      boolean destroyTarget,
      boolean useCapacity,
      float percentageLost,
      boolean destroyBoth,
      boolean createOnGround,
      int customCutOffChance,
      double aMinimumSkill,
      CreationCategories category,
      CreationRequirement... extras
   ) {
      AdvancedCreationEntry defaultEntry = createAdvancedEntry(
         primarySkill,
         objectSource,
         objectTarget,
         objectCreated,
         destroyTarget,
         useCapacity,
         percentageLost,
         destroyBoth,
         createOnGround,
         customCutOffChance,
         aMinimumSkill,
         category
      );

      for(CreationRequirement extra : extras) {
         defaultEntry.addRequirement(extra);
      }

      if (ItemFactory.isMetalLump(objectTarget)) {
         for(int targetTemplateId : ItemFactory.metalLumpList) {
            if (targetTemplateId != objectTarget) {
               AdvancedCreationEntry metalEntry = createAdvancedEntry(
                  primarySkill,
                  objectSource,
                  objectTarget,
                  objectCreated,
                  destroyTarget,
                  useCapacity,
                  percentageLost,
                  destroyBoth,
                  createOnGround,
                  customCutOffChance,
                  aMinimumSkill,
                  category
               );

               for(CreationRequirement extra : extras) {
                  metalEntry.addRequirement(extra);
               }
            }
         }
      } else if (ItemFactory.isMetalLump(objectSource)) {
         for(int sourceTemplateId : ItemFactory.metalLumpList) {
            if (sourceTemplateId != objectSource) {
               AdvancedCreationEntry metalEntry = createAdvancedEntry(
                  primarySkill,
                  objectSource,
                  objectTarget,
                  objectCreated,
                  destroyTarget,
                  useCapacity,
                  percentageLost,
                  destroyBoth,
                  createOnGround,
                  customCutOffChance,
                  aMinimumSkill,
                  category
               );

               for(CreationRequirement extra : extras) {
                  metalEntry.addRequirement(extra);
               }
            }
         }
      }

      return defaultEntry;
   }
}
