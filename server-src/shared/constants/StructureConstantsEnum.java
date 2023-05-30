package com.wurmonline.shared.constants;

import java.util.HashMap;
import java.util.logging.Logger;

public enum StructureConstantsEnum {
   NO_WALL((short)14, BuildingTypesEnum.HOUSE, StructureTypeEnum.NO_WALL, StructureMaterialEnum.WOOD),
   WALL_LEFT_ARCH_WOODEN((short)15, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.WOOD),
   WALL_RIGHT_ARCH_WOODEN((short)16, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.WOOD),
   WALL_T_ARCH_WOODEN((short)17, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.WOOD),
   WALL_LEFT_ARCH_STONE((short)18, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.STONE),
   WALL_RIGHT_ARCH_STONE((short)19, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.STONE),
   WALL_SOLID_WOODEN((short)20, BuildingTypesEnum.HOUSE, StructureTypeEnum.SOLID, StructureMaterialEnum.WOOD),
   WALL_WINDOW_WOODEN((short)21, BuildingTypesEnum.HOUSE, StructureTypeEnum.WINDOW, StructureMaterialEnum.WOOD),
   WALL_DOOR_WOODEN((short)22, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOOR, StructureMaterialEnum.WOOD),
   WALL_DOUBLE_DOOR_WOODEN((short)23, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.WOOD),
   WALL_DOOR_ARCHED_WOODEN((short)24, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED, StructureMaterialEnum.WOOD),
   WALL_SOLID_SLATE((short)25, BuildingTypesEnum.HOUSE, StructureTypeEnum.SOLID, StructureMaterialEnum.SLATE),
   WALL_WINDOW_SLATE((short)26, BuildingTypesEnum.HOUSE, StructureTypeEnum.WINDOW, StructureMaterialEnum.SLATE),
   WALL_NARROW_WINDOW_SLATE((short)27, BuildingTypesEnum.HOUSE, StructureTypeEnum.NARROW_WINDOW, StructureMaterialEnum.SLATE),
   WALL_DOOR_SLATE((short)28, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOOR, StructureMaterialEnum.SLATE),
   WALL_DOUBLE_DOOR_SLATE((short)29, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.SLATE),
   WALL_SOLID_STONE((short)30, BuildingTypesEnum.HOUSE, StructureTypeEnum.SOLID, StructureMaterialEnum.PLAIN_STONE),
   WALL_WINDOW_STONE((short)31, BuildingTypesEnum.HOUSE, StructureTypeEnum.WINDOW, StructureMaterialEnum.PLAIN_STONE),
   WALL_DOOR_STONE((short)32, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOOR, StructureMaterialEnum.PLAIN_STONE),
   WALL_DOUBLE_DOOR_STONE((short)33, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.PLAIN_STONE),
   WALL_DOOR_ARCHED_STONE((short)34, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED, StructureMaterialEnum.PLAIN_STONE),
   WALL_SOLID_STONE_DECORATED((short)35, BuildingTypesEnum.HOUSE, StructureTypeEnum.SOLID, StructureMaterialEnum.STONE),
   WALL_WINDOW_STONE_DECORATED((short)36, BuildingTypesEnum.HOUSE, StructureTypeEnum.WINDOW, StructureMaterialEnum.STONE),
   WALL_DOOR_STONE_DECORATED((short)37, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOOR, StructureMaterialEnum.STONE),
   WALL_DOUBLE_DOOR_STONE_DECORATED((short)38, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.STONE),
   WALL_DOOR_ARCHED_STONE_DECORATED((short)39, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED, StructureMaterialEnum.STONE),
   WALL_SOLID_TIMBER_FRAMED((short)40, BuildingTypesEnum.HOUSE, StructureTypeEnum.SOLID, StructureMaterialEnum.TIMBER_FRAMED),
   WALL_WINDOW_TIMBER_FRAMED((short)41, BuildingTypesEnum.HOUSE, StructureTypeEnum.WINDOW, StructureMaterialEnum.TIMBER_FRAMED),
   WALL_DOOR_TIMBER_FRAMED((short)42, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOOR, StructureMaterialEnum.TIMBER_FRAMED),
   WALL_DOUBLE_DOOR_TIMBER_FRAMED((short)43, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.TIMBER_FRAMED),
   WALL_DOOR_ARCHED_TIMBER_FRAMED((short)44, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED, StructureMaterialEnum.TIMBER_FRAMED),
   WALL_ARCHED_SLATE((short)45, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED, StructureMaterialEnum.SLATE),
   WALL_PORTCULLIS_SLATE((short)46, BuildingTypesEnum.HOUSE, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.SLATE),
   WALL_BARRED_SLATE((short)47, BuildingTypesEnum.HOUSE, StructureTypeEnum.BARRED, StructureMaterialEnum.SLATE),
   WALL_ORIEL_SLATE((short)48, BuildingTypesEnum.HOUSE, StructureTypeEnum.ORIEL, StructureMaterialEnum.SLATE),
   WALL_LEFT_ARCH_SLATE((short)49, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.SLATE),
   WALL_RIGHT_ARCH_SLATE((short)50, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.SLATE),
   WALL_T_ARCH_SLATE((short)51, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.SLATE),
   WALL_SOLID_ROUNDED_STONE((short)52, BuildingTypesEnum.HOUSE, StructureTypeEnum.SOLID, StructureMaterialEnum.ROUNDED_STONE),
   WALL_WINDOW_ROUNDED_STONE((short)53, BuildingTypesEnum.HOUSE, StructureTypeEnum.WINDOW, StructureMaterialEnum.ROUNDED_STONE),
   WALL_NARROW_WINDOW_ROUNDED_STONE((short)54, BuildingTypesEnum.HOUSE, StructureTypeEnum.NARROW_WINDOW, StructureMaterialEnum.ROUNDED_STONE),
   WALL_DOOR_ROUNDED_STONE((short)55, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOOR, StructureMaterialEnum.ROUNDED_STONE),
   WALL_DOUBLE_DOOR_ROUNDED_STONE((short)56, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.ROUNDED_STONE),
   WALL_ARCHED_ROUNDED_STONE((short)57, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED, StructureMaterialEnum.ROUNDED_STONE),
   WALL_PORTCULLIS_ROUNDED_STONE((short)58, BuildingTypesEnum.HOUSE, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.ROUNDED_STONE),
   WALL_BARRED_ROUNDED_STONE((short)59, BuildingTypesEnum.HOUSE, StructureTypeEnum.BARRED, StructureMaterialEnum.ROUNDED_STONE),
   WALL_ORIEL_ROUNDED_STONE((short)60, BuildingTypesEnum.HOUSE, StructureTypeEnum.ORIEL, StructureMaterialEnum.ROUNDED_STONE),
   WALL_LEFT_ARCH_ROUNDED_STONE((short)61, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.ROUNDED_STONE),
   WALL_RIGHT_ARCH_ROUNDED_STONE((short)62, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.ROUNDED_STONE),
   WALL_T_ARCH_ROUNDED_STONE((short)63, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.ROUNDED_STONE),
   WALL_SOLID_POTTERY((short)64, BuildingTypesEnum.HOUSE, StructureTypeEnum.SOLID, StructureMaterialEnum.POTTERY),
   WALL_WINDOW_POTTERY((short)65, BuildingTypesEnum.HOUSE, StructureTypeEnum.WINDOW, StructureMaterialEnum.POTTERY),
   WALL_NARROW_WINDOW_POTTERY((short)66, BuildingTypesEnum.HOUSE, StructureTypeEnum.NARROW_WINDOW, StructureMaterialEnum.POTTERY),
   WALL_DOOR_POTTERY((short)67, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOOR, StructureMaterialEnum.POTTERY),
   WALL_DOUBLE_DOOR_POTTERY((short)68, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.POTTERY),
   WALL_ARCHED_POTTERY((short)69, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED, StructureMaterialEnum.POTTERY),
   WALL_PORTCULLIS_POTTERY((short)70, BuildingTypesEnum.HOUSE, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.POTTERY),
   WALL_BARRED_POTTERY((short)71, BuildingTypesEnum.HOUSE, StructureTypeEnum.BARRED, StructureMaterialEnum.POTTERY),
   WALL_ORIEL_POTTERY((short)72, BuildingTypesEnum.HOUSE, StructureTypeEnum.ORIEL, StructureMaterialEnum.POTTERY),
   WALL_LEFT_ARCH_POTTERY((short)73, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.POTTERY),
   WALL_RIGHT_ARCH_POTTERY((short)74, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.POTTERY),
   WALL_T_ARCH_POTTERY((short)75, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.POTTERY),
   WALL_PORTCULLIS_MARBLE((short)76, BuildingTypesEnum.HOUSE, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.MARBLE),
   WALL_BARRED_MARBLE((short)77, BuildingTypesEnum.HOUSE, StructureTypeEnum.BARRED, StructureMaterialEnum.MARBLE),
   WALL_ORIEL_MARBLE((short)78, BuildingTypesEnum.HOUSE, StructureTypeEnum.ORIEL, StructureMaterialEnum.MARBLE),
   WALL_LEFT_ARCH_MARBLE((short)79, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.MARBLE),
   WALL_RIGHT_ARCH_MARBLE((short)80, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.MARBLE),
   WALL_T_ARCH_MARBLE((short)81, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.MARBLE),
   FENCE_ROPE_HIGH((short)104, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.ROPE_HIGH, StructureMaterialEnum.WOOD),
   HEDGE_FLOWER1_LOW((short)105, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_LOW, StructureMaterialEnum.FLOWER1),
   HEDGE_FLOWER1_MEDIUM((short)106, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_MEDIUM, StructureMaterialEnum.FLOWER1),
   HEDGE_FLOWER1_HIGH((short)107, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_HIGH, StructureMaterialEnum.FLOWER1),
   HEDGE_FLOWER2_LOW((short)108, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_LOW, StructureMaterialEnum.FLOWER2),
   HEDGE_FLOWER2_MEDIUM((short)109, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_MEDIUM, StructureMaterialEnum.FLOWER2),
   HEDGE_FLOWER2_HIGH((short)110, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_HIGH, StructureMaterialEnum.FLOWER2),
   HEDGE_FLOWER3_LOW((short)111, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_LOW, StructureMaterialEnum.FLOWER3),
   HEDGE_FLOWER3_MEDIUM((short)112, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_MEDIUM, StructureMaterialEnum.FLOWER3),
   HEDGE_FLOWER3_HIGH((short)113, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_HIGH, StructureMaterialEnum.FLOWER3),
   HEDGE_FLOWER4_LOW((short)114, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_LOW, StructureMaterialEnum.FLOWER4),
   HEDGE_FLOWER4_MEDIUM((short)115, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_MEDIUM, StructureMaterialEnum.FLOWER4),
   HEDGE_FLOWER4_HIGH((short)116, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_HIGH, StructureMaterialEnum.FLOWER4),
   HEDGE_FLOWER5_LOW((short)117, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_LOW, StructureMaterialEnum.FLOWER5),
   HEDGE_FLOWER5_MEDIUM((short)118, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_MEDIUM, StructureMaterialEnum.FLOWER5),
   HEDGE_FLOWER5_HIGH((short)119, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_HIGH, StructureMaterialEnum.FLOWER5),
   HEDGE_FLOWER6_LOW((short)120, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_LOW, StructureMaterialEnum.FLOWER6),
   HEDGE_FLOWER6_MEDIUM((short)121, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_MEDIUM, StructureMaterialEnum.FLOWER6),
   HEDGE_FLOWER6_HIGH((short)122, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_HIGH, StructureMaterialEnum.FLOWER6),
   HEDGE_FLOWER7_LOW((short)123, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_LOW, StructureMaterialEnum.FLOWER7),
   HEDGE_FLOWER7_MEDIUM((short)124, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_MEDIUM, StructureMaterialEnum.FLOWER7),
   HEDGE_FLOWER7_HIGH((short)125, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_HIGH, StructureMaterialEnum.FLOWER7),
   FENCE_MAGIC_STONE((short)126, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MAGIC_FENCE, StructureMaterialEnum.STONE),
   FENCE_GARDESGARD_GATE((short)127, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.GARDESGARD_GATE, StructureMaterialEnum.WOOD),
   WALL_LEFT_ARCH_TIMBER_FRAMED((short)-15, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.TIMBER_FRAMED),
   WALL_RIGHT_ARCH_TIMBER_FRAMED((short)-16, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.TIMBER_FRAMED),
   WALL_T_ARCH_TIMBER_FRAMED((short)-17, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.TIMBER_FRAMED),
   WALL_LEFT_ARCH_STONE_DECORATED((short)-18, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.STONE),
   WALL_RIGHT_ARCH_STONE_DECORATED((short)-19, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.STONE),
   WALL_SOLID_WOODEN_PLAN((short)-20, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_SOLID, StructureMaterialEnum.WOOD),
   WALL_WINDOW_WOODEN_PLAN((short)-21, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_WINDOW, StructureMaterialEnum.WOOD),
   WALL_DOOR_WOODEN_PLAN((short)-22, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_DOOR, StructureMaterialEnum.WOOD),
   WALL_DOUBLE_DOOR_WOODEN_PLAN((short)-23, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_DOUBLE_DOOR, StructureMaterialEnum.WOOD),
   WALL_DOOR_ARCHED_WOODEN_PLAN((short)-24, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED, StructureMaterialEnum.WOOD),
   WALL_SOLID_SANDSTONE((short)-25, BuildingTypesEnum.HOUSE, StructureTypeEnum.SOLID, StructureMaterialEnum.SANDSTONE),
   WALL_WINDOW_SANDSTONE((short)-26, BuildingTypesEnum.HOUSE, StructureTypeEnum.WINDOW, StructureMaterialEnum.SANDSTONE),
   WALL_NARROW_WINDOW_SANDSTONE((short)-27, BuildingTypesEnum.HOUSE, StructureTypeEnum.NARROW_WINDOW, StructureMaterialEnum.SANDSTONE),
   WALL_DOOR_SANDSTONE((short)-28, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOOR, StructureMaterialEnum.SANDSTONE),
   WALL_DOUBLE_DOOR_SANDSTONE((short)-29, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.SANDSTONE),
   WALL_SOLID_STONE_PLAN((short)-30, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_SOLID, StructureMaterialEnum.STONE),
   WALL_WINDOW_STONE_PLAN((short)-31, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_WINDOW, StructureMaterialEnum.STONE),
   WALL_DOOR_STONE_PLAN((short)-32, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_DOOR, StructureMaterialEnum.STONE),
   WALL_DOUBLE_DOOR_STONE_PLAN((short)-33, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_DOUBLE_DOOR, StructureMaterialEnum.STONE),
   WALL_DOOR_ARCHED_PLAN((short)-34, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_ARCHED, StructureMaterialEnum.WOOD),
   WALL_SOLID_TIMBER_FRAMED_PLAN((short)-35, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_SOLID, StructureMaterialEnum.WOOD),
   WALL_WINDOW_TIMBER_FRAMED_PLAN((short)-36, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_WINDOW, StructureMaterialEnum.WOOD),
   WALL_DOOR_TIMBER_FRAMED_PLAN((short)-37, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_DOOR, StructureMaterialEnum.WOOD),
   WALL_DOUBLE_DOOR_TIMBER_FRAMED_PLAN((short)-38, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_DOUBLE_DOOR, StructureMaterialEnum.WOOD),
   WALL_DOOR_ARCHED_TIMBER_FRAMED_PLAN((short)-39, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_ARCHED, StructureMaterialEnum.WOOD),
   WALL_PLAIN_NARROW_WINDOW((short)-40, BuildingTypesEnum.HOUSE, StructureTypeEnum.NARROW_WINDOW, StructureMaterialEnum.PLAIN_STONE),
   WALL_PLAIN_NARROW_WINDOW_PLAN((short)-41, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_NARROW_WINDOW, StructureMaterialEnum.STONE),
   WALL_PORTCULLIS_STONE((short)-42, BuildingTypesEnum.HOUSE, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.STONE),
   WALL_BARRED_STONE((short)-43, BuildingTypesEnum.HOUSE, StructureTypeEnum.BARRED, StructureMaterialEnum.STONE),
   WALL_PORTCULLIS_STONE_DECORATED((short)-44, BuildingTypesEnum.HOUSE, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.STONE),
   WALL_PORTCULLIS_WOOD((short)-45, BuildingTypesEnum.HOUSE, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.WOOD),
   WALL_RUBBLE((short)-46, BuildingTypesEnum.HOUSE, StructureTypeEnum.RUBBLE, StructureMaterialEnum.STONE),
   WALL_BALCONY_TIMBER_FRAMED_PLAN((short)-47, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_BALCONY, StructureMaterialEnum.TIMBER_FRAMED),
   WALL_BALCONY_TIMBER_FRAMED((short)-48, BuildingTypesEnum.HOUSE, StructureTypeEnum.BALCONY, StructureMaterialEnum.TIMBER_FRAMED),
   WALL_JETTY_TIMBER_FRAMED_PLAN((short)-49, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_JETTY, StructureMaterialEnum.TIMBER_FRAMED),
   WALL_JETTY_TIMBER_FRAMED((short)-50, BuildingTypesEnum.HOUSE, StructureTypeEnum.JETTY, StructureMaterialEnum.TIMBER_FRAMED),
   WALL_ORIEL_STONE_DECORATED_PLAN((short)-51, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_ORIEL, StructureMaterialEnum.STONE),
   WALL_ORIEL_STONE_DECORATED((short)-52, BuildingTypesEnum.HOUSE, StructureTypeEnum.ORIEL, StructureMaterialEnum.STONE),
   WALL_CANOPY_WOODEN((short)-53, BuildingTypesEnum.HOUSE, StructureTypeEnum.CANOPY_DOOR, StructureMaterialEnum.WOOD),
   WALL_CANOPY_WOODEN_PLAN((short)-54, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_CANOPY, StructureMaterialEnum.WOOD),
   WALL_WINDOW_WIDE_WOODEN((short)-55, BuildingTypesEnum.HOUSE, StructureTypeEnum.WIDE_WINDOW, StructureMaterialEnum.WOOD),
   WALL_ORIEL_STONE_PLAIN((short)-56, BuildingTypesEnum.HOUSE, StructureTypeEnum.ORIEL, StructureMaterialEnum.PLAIN_STONE),
   WALL_ARCHED_SANDSTONE((short)-57, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED, StructureMaterialEnum.SANDSTONE),
   WALL_PORTCULLIS_SANDSTONE((short)-58, BuildingTypesEnum.HOUSE, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.SANDSTONE),
   WALL_BARRED_SANDSTONE((short)-59, BuildingTypesEnum.HOUSE, StructureTypeEnum.BARRED, StructureMaterialEnum.SANDSTONE),
   WALL_ORIEL_SANDSTONE((short)-60, BuildingTypesEnum.HOUSE, StructureTypeEnum.ORIEL, StructureMaterialEnum.SANDSTONE),
   WALL_LEFT_ARCH_SANDSTONE((short)-61, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.SANDSTONE),
   WALL_RIGHT_ARCH_SANDSTONE((short)-62, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.SANDSTONE),
   WALL_T_ARCH_SANDSTONE((short)-63, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.SANDSTONE),
   WALL_SOLID_RENDERED((short)-64, BuildingTypesEnum.HOUSE, StructureTypeEnum.SOLID, StructureMaterialEnum.RENDERED),
   WALL_WINDOW_RENDERED((short)-65, BuildingTypesEnum.HOUSE, StructureTypeEnum.WINDOW, StructureMaterialEnum.RENDERED),
   WALL_NARROW_WINDOW_RENDERED((short)-66, BuildingTypesEnum.HOUSE, StructureTypeEnum.NARROW_WINDOW, StructureMaterialEnum.RENDERED),
   WALL_DOOR_RENDERED((short)-67, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOOR, StructureMaterialEnum.RENDERED),
   WALL_DOUBLE_DOOR_RENDERED((short)-68, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.RENDERED),
   WALL_ARCHED_RENDERED((short)-69, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED, StructureMaterialEnum.RENDERED),
   WALL_PORTCULLIS_RENDERED((short)-70, BuildingTypesEnum.HOUSE, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.RENDERED),
   WALL_BARRED_RENDERED((short)-71, BuildingTypesEnum.HOUSE, StructureTypeEnum.BARRED, StructureMaterialEnum.RENDERED),
   WALL_ORIEL_RENDERED((short)-72, BuildingTypesEnum.HOUSE, StructureTypeEnum.ORIEL, StructureMaterialEnum.RENDERED),
   WALL_LEFT_ARCH_RENDERED((short)-73, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.RENDERED),
   WALL_RIGHT_ARCH_RENDERED((short)-74, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.RENDERED),
   WALL_T_ARCH_RENDERED((short)-75, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.RENDERED),
   WALL_SOLID_MARBLE((short)-76, BuildingTypesEnum.HOUSE, StructureTypeEnum.SOLID, StructureMaterialEnum.MARBLE),
   WALL_WINDOW_MARBLE((short)-77, BuildingTypesEnum.HOUSE, StructureTypeEnum.WINDOW, StructureMaterialEnum.MARBLE),
   WALL_NARROW_WINDOW_MARBLE((short)-78, BuildingTypesEnum.HOUSE, StructureTypeEnum.NARROW_WINDOW, StructureMaterialEnum.MARBLE),
   WALL_DOOR_MARBLE((short)-79, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOOR, StructureMaterialEnum.MARBLE),
   WALL_DOUBLE_DOOR_MARBLE((short)-80, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.MARBLE),
   WALL_ARCHED_MARBLE((short)-81, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED, StructureMaterialEnum.MARBLE),
   WALL_T_ARCH_STONE((short)-127, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.PLAIN_STONE),
   WALL_T_ARCH_STONE_DECORATED((short)-128, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.STONE),
   WALL_SCAFFOLDING((short)128, BuildingTypesEnum.HOUSE, StructureTypeEnum.SCAFFOLDING, StructureMaterialEnum.CRUDE_WOOD),
   FENCE_WOODEN((short)0, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE, StructureMaterialEnum.WOOD),
   FENCE_PALISADE((short)1, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PALISADE, StructureMaterialEnum.LOG),
   FENCE_STONEWALL((short)2, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_WALL, StructureMaterialEnum.STONE),
   FENCE_WOODEN_GATE((short)3, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.GATE, StructureMaterialEnum.WOOD),
   FENCE_PALISADE_GATE((short)4, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.GATE, StructureMaterialEnum.LOG),
   FENCE_STONEWALL_HIGH((short)5, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_TALL, StructureMaterialEnum.STONE),
   FENCE_IRON((short)6, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS, StructureMaterialEnum.STONE),
   FENCE_IRON_GATE((short)7, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_GATE, StructureMaterialEnum.STONE),
   FENCE_WOVEN((short)8, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.WOVEN, StructureMaterialEnum.WOOD),
   FENCE_WOODEN_PARAPET((short)9, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.WOOD),
   FENCE_STONE_PARAPET((short)10, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.STONE),
   FENCE_STONE_IRON_PARAPET((short)11, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.IRON),
   FENCE_WOODEN_CRUDE((short)12, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE, StructureMaterialEnum.CRUDE_WOOD),
   FENCE_WOODEN_CRUDE_GATE((short)13, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.GATE, StructureMaterialEnum.CRUDE_WOOD),
   FENCE_SLATE((short)82, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE, StructureMaterialEnum.SLATE),
   FENCE_SLATE_IRON((short)83, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS, StructureMaterialEnum.SLATE),
   FENCE_SLATE_IRON_GATE((short)84, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_GATE, StructureMaterialEnum.SLATE),
   FENCE_ROUNDED_STONE((short)85, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE, StructureMaterialEnum.ROUNDED_STONE),
   FENCE_ROUNDED_STONE_IRON((short)86, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS, StructureMaterialEnum.ROUNDED_STONE),
   FENCE_ROUNDED_STONE_IRON_GATE((short)87, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_GATE, StructureMaterialEnum.ROUNDED_STONE),
   FENCE_POTTERY((short)88, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE, StructureMaterialEnum.POTTERY),
   FENCE_POTTERY_IRON((short)89, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS, StructureMaterialEnum.POTTERY),
   FENCE_POTTERY_IRON_GATE((short)90, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_GATE, StructureMaterialEnum.POTTERY),
   FENCE_SANDSTONE((short)91, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE, StructureMaterialEnum.SANDSTONE),
   FENCE_SANDSTONE_IRON((short)92, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS, StructureMaterialEnum.SANDSTONE),
   FENCE_SANDSTONE_IRON_GATE((short)93, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_GATE, StructureMaterialEnum.SANDSTONE),
   FENCE_RENDERED((short)94, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE, StructureMaterialEnum.RENDERED),
   FENCE_RENDERED_IRON((short)95, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS, StructureMaterialEnum.RENDERED),
   FENCE_RENDERED_IRON_GATE((short)96, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_GATE, StructureMaterialEnum.RENDERED),
   FENCE_MARBLE((short)97, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE, StructureMaterialEnum.MARBLE),
   FENCE_MARBLE_IRON((short)98, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS, StructureMaterialEnum.MARBLE),
   FENCE_MARBLE_IRON_GATE((short)99, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_GATE, StructureMaterialEnum.MARBLE),
   FENCE_ROPE_LOW((short)100, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.ROPE_LOW, StructureMaterialEnum.WOOD),
   FENCE_GARDESGARD_LOW((short)101, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.GARDESGARD_LOW, StructureMaterialEnum.WOOD),
   FENCE_GARDESGARD_HIGH((short)102, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.GARDESGARD_HIGH, StructureMaterialEnum.WOOD),
   FENCE_CURB((short)103, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.CURB, StructureMaterialEnum.STONE),
   FENCE_PLAN_WOODEN((short)-1, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_WOODEN, StructureMaterialEnum.WOOD),
   FENCE_PLAN_PALISADE((short)-2, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_PALISADE, StructureMaterialEnum.LOG),
   FENCE_PLAN_STONEWALL((short)-3, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL, StructureMaterialEnum.STONE),
   FENCE_PLAN_PALISADE_GATE((short)-4, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_PALISADE_GATE, StructureMaterialEnum.LOG),
   FENCE_PLAN_WOODEN_GATE((short)-5, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_WOODEN_GATE, StructureMaterialEnum.WOOD),
   FENCE_PLAN_STONEWALL_HIGH((short)-6, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL_HIGH, StructureMaterialEnum.STONE),
   FENCE_PLAN_IRON((short)-7, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS, StructureMaterialEnum.STONE),
   FENCE_PLAN_IRON_GATE((short)-8, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_GATE, StructureMaterialEnum.STONE),
   FENCE_PLAN_WOVEN((short)-9, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.WOVEN, StructureMaterialEnum.WOOD),
   FENCE_PLAN_WOODEN_PARAPET((short)-10, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_WOODEN_PARAPET, StructureMaterialEnum.WOOD),
   FENCE_PLAN_STONE_PARAPET((short)-11, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONE_PARAPET, StructureMaterialEnum.STONE),
   FENCE_PLAN_STONE_IRON_PARAPET((short)-12, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_PARAPET, StructureMaterialEnum.STONE),
   FENCE_PLAN_WOODEN_CRUDE((short)-13, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_CRUDE, StructureMaterialEnum.WOOD),
   FENCE_PLAN_WOODEN_GATE_CRUDE((short)-14, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_CRUDE_GATE, StructureMaterialEnum.WOOD),
   FENCE_PLAN_SLATE((short)-82, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL, StructureMaterialEnum.SLATE),
   FENCE_PLAN_SLATE_IRON((short)-83, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS, StructureMaterialEnum.SLATE),
   FENCE_PLAN_SLATE_IRON_GATE((short)-84, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_GATE, StructureMaterialEnum.SLATE),
   FENCE_PLAN_ROUNDED_STONE((short)-85, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL, StructureMaterialEnum.ROUNDED_STONE),
   FENCE_PLAN_ROUNDED_STONE_IRON((short)-86, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS, StructureMaterialEnum.ROUNDED_STONE),
   FENCE_PLAN_ROUNDED_STONE_IRON_GATE(
      (short)-87, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_GATE, StructureMaterialEnum.ROUNDED_STONE
   ),
   FENCE_PLAN_POTTERY((short)-88, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL, StructureMaterialEnum.POTTERY),
   FENCE_PLAN_POTTERY_IRON((short)-89, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS, StructureMaterialEnum.POTTERY),
   FENCE_PLAN_POTTERY_IRON_GATE((short)-90, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_GATE, StructureMaterialEnum.POTTERY),
   FENCE_PLAN_SANDSTONE((short)-91, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL, StructureMaterialEnum.SANDSTONE),
   FENCE_PLAN_SANDSTONE_IRON((short)-92, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS, StructureMaterialEnum.SANDSTONE),
   FENCE_PLAN_SANDSTONE_IRON_GATE((short)-93, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_GATE, StructureMaterialEnum.SANDSTONE),
   FENCE_PLAN_RENDERED((short)-94, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL, StructureMaterialEnum.RENDERED),
   FENCE_PLAN_RENDERED_IRON((short)-95, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS, StructureMaterialEnum.RENDERED),
   FENCE_PLAN_RENDERED_IRON_GATE((short)-96, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_GATE, StructureMaterialEnum.RENDERED),
   FENCE_PLAN_MARBLE((short)-97, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL, StructureMaterialEnum.MARBLE),
   FENCE_PLAN_MARBLE_IRON((short)-98, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS, StructureMaterialEnum.MARBLE),
   FENCE_PLAN_MARBLE_IRON_GATE((short)-99, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_GATE, StructureMaterialEnum.MARBLE),
   FENCE_PLAN_ROPE_LOW((short)-100, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_ROPE_LOW, StructureMaterialEnum.WOOD),
   FENCE_PLAN_GARDESGARD_LOW((short)-101, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_GARDESGARD_LOW, StructureMaterialEnum.WOOD),
   FENCE_PLAN_GARDESGARD_HIGH((short)-102, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_GARDESGARD_HIGH, StructureMaterialEnum.WOOD),
   FENCE_PLAN_CURB((short)-103, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_CURB, StructureMaterialEnum.STONE),
   FENCE_PLAN_ROPE_HIGH((short)-104, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_ROPE_HIGH, StructureMaterialEnum.WOOD),
   FENCE_PLAN_GARDESGARD_GATE((short)-105, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_GARDESGARD_GATE, StructureMaterialEnum.WOOD),
   FENCE_STONE((short)-106, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE, StructureMaterialEnum.STONE),
   FENCE_PLAN_STONE((short)-107, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONE_FENCE, StructureMaterialEnum.STONE),
   FENCE_IRON_HIGH((short)-108, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL, StructureMaterialEnum.STONE),
   FENCE_PLAN_IRON_HIGH((short)-109, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL, StructureMaterialEnum.STONE),
   FENCE_IRON_GATE_HIGH((short)-110, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL_GATE, StructureMaterialEnum.STONE),
   FENCE_PLAN_IRON_GATE_HIGH((short)-111, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL_GATE, StructureMaterialEnum.STONE),
   FENCE_SLATE_TALL_STONE_WALL((short)129, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_TALL, StructureMaterialEnum.SLATE),
   FENCE_SLATE_PORTCULLIS((short)130, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.SLATE),
   FENCE_SLATE_HIGH_IRON_FENCE((short)131, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL, StructureMaterialEnum.SLATE),
   FENCE_SLATE_HIGH_IRON_FENCE_GATE((short)132, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL_GATE, StructureMaterialEnum.SLATE),
   FENCE_SLATE_STONE_PARAPET((short)133, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.SLATE),
   FENCE_SLATE_CHAIN_FENCE((short)134, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MEDIUM_CHAIN, StructureMaterialEnum.SLATE),
   FENCE_ROUNDED_STONE_TALL_STONE_WALL((short)135, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_TALL, StructureMaterialEnum.ROUNDED_STONE),
   FENCE_ROUNDED_STONE_PORTCULLIS((short)136, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.ROUNDED_STONE),
   FENCE_ROUNDED_STONE_HIGH_IRON_FENCE((short)137, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL, StructureMaterialEnum.ROUNDED_STONE),
   FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE(
      (short)138, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL_GATE, StructureMaterialEnum.ROUNDED_STONE
   ),
   FENCE_ROUNDED_STONE_STONE_PARAPET((short)139, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.ROUNDED_STONE),
   FENCE_ROUNDED_STONE_CHAIN_FENCE((short)140, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MEDIUM_CHAIN, StructureMaterialEnum.ROUNDED_STONE),
   FENCE_SANDSTONE_TALL_STONE_WALL((short)141, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_TALL, StructureMaterialEnum.SANDSTONE),
   FENCE_SANDSTONE_PORTCULLIS((short)142, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.SANDSTONE),
   FENCE_SANDSTONE_HIGH_IRON_FENCE((short)143, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL, StructureMaterialEnum.SANDSTONE),
   FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE((short)144, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL_GATE, StructureMaterialEnum.SANDSTONE),
   FENCE_SANDSTONE_STONE_PARAPET((short)145, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.SANDSTONE),
   FENCE_SANDSTONE_CHAIN_FENCE((short)146, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MEDIUM_CHAIN, StructureMaterialEnum.SANDSTONE),
   FENCE_RENDERED_TALL_STONE_WALL((short)147, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_TALL, StructureMaterialEnum.RENDERED),
   FENCE_RENDERED_PORTCULLIS((short)148, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.RENDERED),
   FENCE_RENDERED_HIGH_IRON_FENCE((short)149, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL, StructureMaterialEnum.RENDERED),
   FENCE_RENDERED_HIGH_IRON_FENCE_GATE((short)150, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL_GATE, StructureMaterialEnum.RENDERED),
   FENCE_RENDERED_STONE_PARAPET((short)151, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.RENDERED),
   FENCE_RENDERED_CHAIN_FENCE((short)152, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MEDIUM_CHAIN, StructureMaterialEnum.RENDERED),
   FENCE_POTTERY_TALL_STONE_WALL((short)153, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_TALL, StructureMaterialEnum.POTTERY),
   FENCE_POTTERY_PORTCULLIS((short)154, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.POTTERY),
   FENCE_POTTERY_HIGH_IRON_FENCE((short)155, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL, StructureMaterialEnum.POTTERY),
   FENCE_POTTERY_HIGH_IRON_FENCE_GATE((short)156, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL_GATE, StructureMaterialEnum.POTTERY),
   FENCE_POTTERY_STONE_PARAPET((short)157, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.POTTERY),
   FENCE_POTTERY_CHAIN_FENCE((short)158, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MEDIUM_CHAIN, StructureMaterialEnum.POTTERY),
   FENCE_MARBLE_TALL_STONE_WALL((short)159, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_TALL, StructureMaterialEnum.MARBLE),
   FENCE_MARBLE_PORTCULLIS((short)160, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.MARBLE),
   FENCE_MARBLE_HIGH_IRON_FENCE((short)161, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL, StructureMaterialEnum.MARBLE),
   FENCE_MARBLE_HIGH_IRON_FENCE_GATE((short)162, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL_GATE, StructureMaterialEnum.MARBLE),
   FENCE_MARBLE_STONE_PARAPET((short)163, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.MARBLE),
   FENCE_MARBLE_CHAIN_FENCE((short)164, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MEDIUM_CHAIN, StructureMaterialEnum.MARBLE),
   FENCE_PLAN_SLATE_TALL_STONE_WALL((short)165, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL_HIGH, StructureMaterialEnum.SLATE),
   FENCE_PLAN_SLATE_PORTCULLIS((short)166, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.SLATE),
   FENCE_PLAN_SLATE_HIGH_IRON_FENCE((short)167, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL, StructureMaterialEnum.SLATE),
   FENCE_PLAN_SLATE_HIGH_IRON_FENCE_GATE(
      (short)168, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL_GATE, StructureMaterialEnum.SLATE
   ),
   FENCE_PLAN_SLATE_STONE_PARAPET((short)169, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.SLATE),
   FENCE_PLAN_SLATE_CHAIN_FENCE((short)170, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MEDIUM_CHAIN, StructureMaterialEnum.SLATE),
   FENCE_PLAN_ROUNDED_STONE_TALL_STONE_WALL(
      (short)171, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL_HIGH, StructureMaterialEnum.ROUNDED_STONE
   ),
   FENCE_PLAN_ROUNDED_STONE_PORTCULLIS((short)172, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.ROUNDED_STONE),
   FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE(
      (short)173, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL, StructureMaterialEnum.ROUNDED_STONE
   ),
   FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE_GATE(
      (short)174, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL_GATE, StructureMaterialEnum.ROUNDED_STONE
   ),
   FENCE_PLAN_ROUNDED_STONE_STONE_PARAPET((short)175, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.ROUNDED_STONE),
   FENCE_PLAN_ROUNDED_STONE_CHAIN_FENCE((short)176, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MEDIUM_CHAIN, StructureMaterialEnum.ROUNDED_STONE),
   FENCE_PLAN_SANDSTONE_TALL_STONE_WALL((short)177, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL_HIGH, StructureMaterialEnum.SANDSTONE),
   FENCE_PLAN_SANDSTONE_PORTCULLIS((short)178, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.SANDSTONE),
   FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE((short)179, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL, StructureMaterialEnum.SANDSTONE),
   FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE_GATE(
      (short)180, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL_GATE, StructureMaterialEnum.SANDSTONE
   ),
   FENCE_PLAN_SANDSTONE_STONE_PARAPET((short)181, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.SANDSTONE),
   FENCE_PLAN_SANDSTONE_CHAIN_FENCE((short)182, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MEDIUM_CHAIN, StructureMaterialEnum.SANDSTONE),
   FENCE_PLAN_RENDERED_TALL_STONE_WALL((short)183, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL_HIGH, StructureMaterialEnum.RENDERED),
   FENCE_PLAN_RENDERED_PORTCULLIS((short)184, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.RENDERED),
   FENCE_PLAN_RENDERED_HIGH_IRON_FENCE((short)185, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL, StructureMaterialEnum.RENDERED),
   FENCE_PLAN_RENDERED_HIGH_IRON_FENCE_GATE(
      (short)186, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL_GATE, StructureMaterialEnum.RENDERED
   ),
   FENCE_PLAN_RENDERED_STONE_PARAPET((short)187, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.RENDERED),
   FENCE_PLAN_RENDERED_CHAIN_FENCE((short)188, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MEDIUM_CHAIN, StructureMaterialEnum.RENDERED),
   FENCE_PLAN_POTTERY_TALL_STONE_WALL((short)189, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL_HIGH, StructureMaterialEnum.POTTERY),
   FENCE_PLAN_POTTERY_PORTCULLIS((short)190, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.POTTERY),
   FENCE_PLAN_POTTERY_HIGH_IRON_FENCE((short)191, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL, StructureMaterialEnum.POTTERY),
   FENCE_PLAN_POTTERY_HIGH_IRON_FENCE_GATE(
      (short)192, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL_GATE, StructureMaterialEnum.POTTERY
   ),
   FENCE_PLAN_POTTERY_STONE_PARAPET((short)193, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.POTTERY),
   FENCE_PLAN_POTTERY_CHAIN_FENCE((short)194, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MEDIUM_CHAIN, StructureMaterialEnum.POTTERY),
   FENCE_PLAN_MARBLE_TALL_STONE_WALL((short)195, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL_HIGH, StructureMaterialEnum.MARBLE),
   FENCE_PLAN_MARBLE_PORTCULLIS((short)196, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.MARBLE),
   FENCE_PLAN_MARBLE_HIGH_IRON_FENCE((short)197, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL, StructureMaterialEnum.MARBLE),
   FENCE_PLAN_MARBLE_HIGH_IRON_FENCE_GATE(
      (short)198, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL_GATE, StructureMaterialEnum.MARBLE
   ),
   FENCE_PLAN_MARBLE_STONE_PARAPET((short)199, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.MARBLE),
   FENCE_PLAN_MARBLE_CHAIN_FENCE((short)200, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MEDIUM_CHAIN, StructureMaterialEnum.MARBLE),
   FLOWERBED_YELLOW((short)-112, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FLOWERBED, StructureMaterialEnum.FLOWER1),
   FLOWERBED_ORANGE_RED((short)-113, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FLOWERBED, StructureMaterialEnum.FLOWER1),
   FLOWERBED_PURPLE((short)-114, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FLOWERBED, StructureMaterialEnum.FLOWER1),
   FLOWERBED_WHITE((short)-115, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FLOWERBED, StructureMaterialEnum.FLOWER1),
   FLOWERBED_BLUE((short)-116, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FLOWERBED, StructureMaterialEnum.FLOWER1),
   FLOWERBED_GREENISH_YELLOW((short)-117, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FLOWERBED, StructureMaterialEnum.FLOWER1),
   FLOWERBED_WHITE_DOTTED((short)-118, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FLOWERBED, StructureMaterialEnum.FLOWER1),
   FENCE_MAGIC_FIRE((short)-119, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MAGIC_FENCE, StructureMaterialEnum.FIRE),
   FENCE_MAGIC_ICE((short)-120, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MAGIC_FENCE, StructureMaterialEnum.ICE),
   FENCE_PLAN_MEDIUM_CHAIN((short)-121, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_MEDIUM_CHAIN, StructureMaterialEnum.STONE),
   FENCE_MEDIUM_CHAIN((short)-122, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MEDIUM_CHAIN, StructureMaterialEnum.STONE),
   FENCE_PLAN_PORTCULLIS((short)-123, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_PORTCULLIS, StructureMaterialEnum.STONE),
   FENCE_PORTCULLIS((short)-124, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.STONE),
   FENCE_RUBBLE((short)-125, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.RUBBLE, StructureMaterialEnum.STONE),
   FENCE_SIEGEWALL((short)-126, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.SIEGWALL, StructureMaterialEnum.WOOD);

   public final short value;
   public final BuildingTypesEnum structureType;
   public final StructureTypeEnum type;
   public final StructureMaterialEnum material;
   private String modelPath = "";
   private String texturePath = "";
   private int icon = -1;
   public final boolean useNewStringBuilder = false;
   private final HashMap<String, StructureConstantsEnum> lookupMap = new HashMap<>();

   private StructureConstantsEnum(short _value, BuildingTypesEnum _structureType, StructureTypeEnum _type, StructureMaterialEnum _material) {
      this.value = _value;
      this.structureType = _structureType;
      this.type = _type;
      this.material = _material;
   }

   public String getModelPath() {
      if (this.modelPath.equals("")) {
         this.modelPath = WallConstants.getModelName(this, (byte)0, 0, true);
      }

      return this.modelPath;
   }

   public String getTexturePath() {
      if (this.texturePath.equals("")) {
         this.texturePath = WallConstants.getTextureName(this, true);
      }

      return this.texturePath;
   }

   public int getIconId() {
      if (this.icon == -1) {
         this.icon = WallConstants.getIconId(this, true);
      }

      return this.icon;
   }

   private String buildModelPathString() {
      StringBuilder sb = new StringBuilder();
      return sb.toString();
   }

   private String buildTexturePathString() {
      return "";
   }

   public String getUpperModelString() {
      return this.modelPath + ".upper";
   }

   public String getDecayedTextureString() {
      return this.texturePath + ".decayed";
   }

   public static StructureConstantsEnum getEnumByINDEX(short id) {
      if (id <= values().length && id >= 0) {
         return values()[id];
      } else {
         Logger.getGlobal().warning("Value not a valid id: " + id + " RETURNING WALL_WINDOW_STONE_PLAN(VAL=-31)!");
         return WALL_WINDOW_STONE_PLAN;
      }
   }

   public static StructureConstantsEnum getEnumByValue(short value) {
      for(StructureConstantsEnum e : values()) {
         if (e.value == value) {
            return e;
         }
      }

      Logger.getGlobal().warning("Reached default return value for value=" + value + " RETURNING WALL_WINDOW_STONE_PLAN(VAL=-31)!");
      return WALL_WINDOW_STONE_PLAN;
   }
}
