/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.shared.constants;

import com.wurmonline.shared.constants.BuildingTypesEnum;
import com.wurmonline.shared.constants.StructureMaterialEnum;
import com.wurmonline.shared.constants.StructureTypeEnum;
import com.wurmonline.shared.constants.WallConstants;
import java.util.HashMap;
import java.util.logging.Logger;

public enum StructureConstantsEnum {
    NO_WALL(14, BuildingTypesEnum.HOUSE, StructureTypeEnum.NO_WALL, StructureMaterialEnum.WOOD),
    WALL_LEFT_ARCH_WOODEN(15, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.WOOD),
    WALL_RIGHT_ARCH_WOODEN(16, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.WOOD),
    WALL_T_ARCH_WOODEN(17, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.WOOD),
    WALL_LEFT_ARCH_STONE(18, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.STONE),
    WALL_RIGHT_ARCH_STONE(19, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.STONE),
    WALL_SOLID_WOODEN(20, BuildingTypesEnum.HOUSE, StructureTypeEnum.SOLID, StructureMaterialEnum.WOOD),
    WALL_WINDOW_WOODEN(21, BuildingTypesEnum.HOUSE, StructureTypeEnum.WINDOW, StructureMaterialEnum.WOOD),
    WALL_DOOR_WOODEN(22, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOOR, StructureMaterialEnum.WOOD),
    WALL_DOUBLE_DOOR_WOODEN(23, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.WOOD),
    WALL_DOOR_ARCHED_WOODEN(24, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED, StructureMaterialEnum.WOOD),
    WALL_SOLID_SLATE(25, BuildingTypesEnum.HOUSE, StructureTypeEnum.SOLID, StructureMaterialEnum.SLATE),
    WALL_WINDOW_SLATE(26, BuildingTypesEnum.HOUSE, StructureTypeEnum.WINDOW, StructureMaterialEnum.SLATE),
    WALL_NARROW_WINDOW_SLATE(27, BuildingTypesEnum.HOUSE, StructureTypeEnum.NARROW_WINDOW, StructureMaterialEnum.SLATE),
    WALL_DOOR_SLATE(28, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOOR, StructureMaterialEnum.SLATE),
    WALL_DOUBLE_DOOR_SLATE(29, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.SLATE),
    WALL_SOLID_STONE(30, BuildingTypesEnum.HOUSE, StructureTypeEnum.SOLID, StructureMaterialEnum.PLAIN_STONE),
    WALL_WINDOW_STONE(31, BuildingTypesEnum.HOUSE, StructureTypeEnum.WINDOW, StructureMaterialEnum.PLAIN_STONE),
    WALL_DOOR_STONE(32, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOOR, StructureMaterialEnum.PLAIN_STONE),
    WALL_DOUBLE_DOOR_STONE(33, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.PLAIN_STONE),
    WALL_DOOR_ARCHED_STONE(34, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED, StructureMaterialEnum.PLAIN_STONE),
    WALL_SOLID_STONE_DECORATED(35, BuildingTypesEnum.HOUSE, StructureTypeEnum.SOLID, StructureMaterialEnum.STONE),
    WALL_WINDOW_STONE_DECORATED(36, BuildingTypesEnum.HOUSE, StructureTypeEnum.WINDOW, StructureMaterialEnum.STONE),
    WALL_DOOR_STONE_DECORATED(37, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOOR, StructureMaterialEnum.STONE),
    WALL_DOUBLE_DOOR_STONE_DECORATED(38, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.STONE),
    WALL_DOOR_ARCHED_STONE_DECORATED(39, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED, StructureMaterialEnum.STONE),
    WALL_SOLID_TIMBER_FRAMED(40, BuildingTypesEnum.HOUSE, StructureTypeEnum.SOLID, StructureMaterialEnum.TIMBER_FRAMED),
    WALL_WINDOW_TIMBER_FRAMED(41, BuildingTypesEnum.HOUSE, StructureTypeEnum.WINDOW, StructureMaterialEnum.TIMBER_FRAMED),
    WALL_DOOR_TIMBER_FRAMED(42, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOOR, StructureMaterialEnum.TIMBER_FRAMED),
    WALL_DOUBLE_DOOR_TIMBER_FRAMED(43, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.TIMBER_FRAMED),
    WALL_DOOR_ARCHED_TIMBER_FRAMED(44, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED, StructureMaterialEnum.TIMBER_FRAMED),
    WALL_ARCHED_SLATE(45, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED, StructureMaterialEnum.SLATE),
    WALL_PORTCULLIS_SLATE(46, BuildingTypesEnum.HOUSE, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.SLATE),
    WALL_BARRED_SLATE(47, BuildingTypesEnum.HOUSE, StructureTypeEnum.BARRED, StructureMaterialEnum.SLATE),
    WALL_ORIEL_SLATE(48, BuildingTypesEnum.HOUSE, StructureTypeEnum.ORIEL, StructureMaterialEnum.SLATE),
    WALL_LEFT_ARCH_SLATE(49, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.SLATE),
    WALL_RIGHT_ARCH_SLATE(50, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.SLATE),
    WALL_T_ARCH_SLATE(51, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.SLATE),
    WALL_SOLID_ROUNDED_STONE(52, BuildingTypesEnum.HOUSE, StructureTypeEnum.SOLID, StructureMaterialEnum.ROUNDED_STONE),
    WALL_WINDOW_ROUNDED_STONE(53, BuildingTypesEnum.HOUSE, StructureTypeEnum.WINDOW, StructureMaterialEnum.ROUNDED_STONE),
    WALL_NARROW_WINDOW_ROUNDED_STONE(54, BuildingTypesEnum.HOUSE, StructureTypeEnum.NARROW_WINDOW, StructureMaterialEnum.ROUNDED_STONE),
    WALL_DOOR_ROUNDED_STONE(55, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOOR, StructureMaterialEnum.ROUNDED_STONE),
    WALL_DOUBLE_DOOR_ROUNDED_STONE(56, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.ROUNDED_STONE),
    WALL_ARCHED_ROUNDED_STONE(57, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED, StructureMaterialEnum.ROUNDED_STONE),
    WALL_PORTCULLIS_ROUNDED_STONE(58, BuildingTypesEnum.HOUSE, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.ROUNDED_STONE),
    WALL_BARRED_ROUNDED_STONE(59, BuildingTypesEnum.HOUSE, StructureTypeEnum.BARRED, StructureMaterialEnum.ROUNDED_STONE),
    WALL_ORIEL_ROUNDED_STONE(60, BuildingTypesEnum.HOUSE, StructureTypeEnum.ORIEL, StructureMaterialEnum.ROUNDED_STONE),
    WALL_LEFT_ARCH_ROUNDED_STONE(61, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.ROUNDED_STONE),
    WALL_RIGHT_ARCH_ROUNDED_STONE(62, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.ROUNDED_STONE),
    WALL_T_ARCH_ROUNDED_STONE(63, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.ROUNDED_STONE),
    WALL_SOLID_POTTERY(64, BuildingTypesEnum.HOUSE, StructureTypeEnum.SOLID, StructureMaterialEnum.POTTERY),
    WALL_WINDOW_POTTERY(65, BuildingTypesEnum.HOUSE, StructureTypeEnum.WINDOW, StructureMaterialEnum.POTTERY),
    WALL_NARROW_WINDOW_POTTERY(66, BuildingTypesEnum.HOUSE, StructureTypeEnum.NARROW_WINDOW, StructureMaterialEnum.POTTERY),
    WALL_DOOR_POTTERY(67, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOOR, StructureMaterialEnum.POTTERY),
    WALL_DOUBLE_DOOR_POTTERY(68, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.POTTERY),
    WALL_ARCHED_POTTERY(69, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED, StructureMaterialEnum.POTTERY),
    WALL_PORTCULLIS_POTTERY(70, BuildingTypesEnum.HOUSE, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.POTTERY),
    WALL_BARRED_POTTERY(71, BuildingTypesEnum.HOUSE, StructureTypeEnum.BARRED, StructureMaterialEnum.POTTERY),
    WALL_ORIEL_POTTERY(72, BuildingTypesEnum.HOUSE, StructureTypeEnum.ORIEL, StructureMaterialEnum.POTTERY),
    WALL_LEFT_ARCH_POTTERY(73, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.POTTERY),
    WALL_RIGHT_ARCH_POTTERY(74, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.POTTERY),
    WALL_T_ARCH_POTTERY(75, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.POTTERY),
    WALL_PORTCULLIS_MARBLE(76, BuildingTypesEnum.HOUSE, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.MARBLE),
    WALL_BARRED_MARBLE(77, BuildingTypesEnum.HOUSE, StructureTypeEnum.BARRED, StructureMaterialEnum.MARBLE),
    WALL_ORIEL_MARBLE(78, BuildingTypesEnum.HOUSE, StructureTypeEnum.ORIEL, StructureMaterialEnum.MARBLE),
    WALL_LEFT_ARCH_MARBLE(79, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.MARBLE),
    WALL_RIGHT_ARCH_MARBLE(80, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.MARBLE),
    WALL_T_ARCH_MARBLE(81, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.MARBLE),
    FENCE_ROPE_HIGH(104, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.ROPE_HIGH, StructureMaterialEnum.WOOD),
    HEDGE_FLOWER1_LOW(105, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_LOW, StructureMaterialEnum.FLOWER1),
    HEDGE_FLOWER1_MEDIUM(106, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_MEDIUM, StructureMaterialEnum.FLOWER1),
    HEDGE_FLOWER1_HIGH(107, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_HIGH, StructureMaterialEnum.FLOWER1),
    HEDGE_FLOWER2_LOW(108, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_LOW, StructureMaterialEnum.FLOWER2),
    HEDGE_FLOWER2_MEDIUM(109, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_MEDIUM, StructureMaterialEnum.FLOWER2),
    HEDGE_FLOWER2_HIGH(110, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_HIGH, StructureMaterialEnum.FLOWER2),
    HEDGE_FLOWER3_LOW(111, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_LOW, StructureMaterialEnum.FLOWER3),
    HEDGE_FLOWER3_MEDIUM(112, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_MEDIUM, StructureMaterialEnum.FLOWER3),
    HEDGE_FLOWER3_HIGH(113, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_HIGH, StructureMaterialEnum.FLOWER3),
    HEDGE_FLOWER4_LOW(114, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_LOW, StructureMaterialEnum.FLOWER4),
    HEDGE_FLOWER4_MEDIUM(115, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_MEDIUM, StructureMaterialEnum.FLOWER4),
    HEDGE_FLOWER4_HIGH(116, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_HIGH, StructureMaterialEnum.FLOWER4),
    HEDGE_FLOWER5_LOW(117, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_LOW, StructureMaterialEnum.FLOWER5),
    HEDGE_FLOWER5_MEDIUM(118, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_MEDIUM, StructureMaterialEnum.FLOWER5),
    HEDGE_FLOWER5_HIGH(119, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_HIGH, StructureMaterialEnum.FLOWER5),
    HEDGE_FLOWER6_LOW(120, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_LOW, StructureMaterialEnum.FLOWER6),
    HEDGE_FLOWER6_MEDIUM(121, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_MEDIUM, StructureMaterialEnum.FLOWER6),
    HEDGE_FLOWER6_HIGH(122, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_HIGH, StructureMaterialEnum.FLOWER6),
    HEDGE_FLOWER7_LOW(123, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_LOW, StructureMaterialEnum.FLOWER7),
    HEDGE_FLOWER7_MEDIUM(124, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_MEDIUM, StructureMaterialEnum.FLOWER7),
    HEDGE_FLOWER7_HIGH(125, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.HEDGE_HIGH, StructureMaterialEnum.FLOWER7),
    FENCE_MAGIC_STONE(126, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MAGIC_FENCE, StructureMaterialEnum.STONE),
    FENCE_GARDESGARD_GATE(127, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.GARDESGARD_GATE, StructureMaterialEnum.WOOD),
    WALL_LEFT_ARCH_TIMBER_FRAMED(-15, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.TIMBER_FRAMED),
    WALL_RIGHT_ARCH_TIMBER_FRAMED(-16, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.TIMBER_FRAMED),
    WALL_T_ARCH_TIMBER_FRAMED(-17, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.TIMBER_FRAMED),
    WALL_LEFT_ARCH_STONE_DECORATED(-18, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.STONE),
    WALL_RIGHT_ARCH_STONE_DECORATED(-19, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.STONE),
    WALL_SOLID_WOODEN_PLAN(-20, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_SOLID, StructureMaterialEnum.WOOD),
    WALL_WINDOW_WOODEN_PLAN(-21, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_WINDOW, StructureMaterialEnum.WOOD),
    WALL_DOOR_WOODEN_PLAN(-22, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_DOOR, StructureMaterialEnum.WOOD),
    WALL_DOUBLE_DOOR_WOODEN_PLAN(-23, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_DOUBLE_DOOR, StructureMaterialEnum.WOOD),
    WALL_DOOR_ARCHED_WOODEN_PLAN(-24, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED, StructureMaterialEnum.WOOD),
    WALL_SOLID_SANDSTONE(-25, BuildingTypesEnum.HOUSE, StructureTypeEnum.SOLID, StructureMaterialEnum.SANDSTONE),
    WALL_WINDOW_SANDSTONE(-26, BuildingTypesEnum.HOUSE, StructureTypeEnum.WINDOW, StructureMaterialEnum.SANDSTONE),
    WALL_NARROW_WINDOW_SANDSTONE(-27, BuildingTypesEnum.HOUSE, StructureTypeEnum.NARROW_WINDOW, StructureMaterialEnum.SANDSTONE),
    WALL_DOOR_SANDSTONE(-28, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOOR, StructureMaterialEnum.SANDSTONE),
    WALL_DOUBLE_DOOR_SANDSTONE(-29, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.SANDSTONE),
    WALL_SOLID_STONE_PLAN(-30, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_SOLID, StructureMaterialEnum.STONE),
    WALL_WINDOW_STONE_PLAN(-31, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_WINDOW, StructureMaterialEnum.STONE),
    WALL_DOOR_STONE_PLAN(-32, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_DOOR, StructureMaterialEnum.STONE),
    WALL_DOUBLE_DOOR_STONE_PLAN(-33, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_DOUBLE_DOOR, StructureMaterialEnum.STONE),
    WALL_DOOR_ARCHED_PLAN(-34, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_ARCHED, StructureMaterialEnum.WOOD),
    WALL_SOLID_TIMBER_FRAMED_PLAN(-35, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_SOLID, StructureMaterialEnum.WOOD),
    WALL_WINDOW_TIMBER_FRAMED_PLAN(-36, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_WINDOW, StructureMaterialEnum.WOOD),
    WALL_DOOR_TIMBER_FRAMED_PLAN(-37, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_DOOR, StructureMaterialEnum.WOOD),
    WALL_DOUBLE_DOOR_TIMBER_FRAMED_PLAN(-38, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_DOUBLE_DOOR, StructureMaterialEnum.WOOD),
    WALL_DOOR_ARCHED_TIMBER_FRAMED_PLAN(-39, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_ARCHED, StructureMaterialEnum.WOOD),
    WALL_PLAIN_NARROW_WINDOW(-40, BuildingTypesEnum.HOUSE, StructureTypeEnum.NARROW_WINDOW, StructureMaterialEnum.PLAIN_STONE),
    WALL_PLAIN_NARROW_WINDOW_PLAN(-41, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_NARROW_WINDOW, StructureMaterialEnum.STONE),
    WALL_PORTCULLIS_STONE(-42, BuildingTypesEnum.HOUSE, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.STONE),
    WALL_BARRED_STONE(-43, BuildingTypesEnum.HOUSE, StructureTypeEnum.BARRED, StructureMaterialEnum.STONE),
    WALL_PORTCULLIS_STONE_DECORATED(-44, BuildingTypesEnum.HOUSE, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.STONE),
    WALL_PORTCULLIS_WOOD(-45, BuildingTypesEnum.HOUSE, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.WOOD),
    WALL_RUBBLE(-46, BuildingTypesEnum.HOUSE, StructureTypeEnum.RUBBLE, StructureMaterialEnum.STONE),
    WALL_BALCONY_TIMBER_FRAMED_PLAN(-47, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_BALCONY, StructureMaterialEnum.TIMBER_FRAMED),
    WALL_BALCONY_TIMBER_FRAMED(-48, BuildingTypesEnum.HOUSE, StructureTypeEnum.BALCONY, StructureMaterialEnum.TIMBER_FRAMED),
    WALL_JETTY_TIMBER_FRAMED_PLAN(-49, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_JETTY, StructureMaterialEnum.TIMBER_FRAMED),
    WALL_JETTY_TIMBER_FRAMED(-50, BuildingTypesEnum.HOUSE, StructureTypeEnum.JETTY, StructureMaterialEnum.TIMBER_FRAMED),
    WALL_ORIEL_STONE_DECORATED_PLAN(-51, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_ORIEL, StructureMaterialEnum.STONE),
    WALL_ORIEL_STONE_DECORATED(-52, BuildingTypesEnum.HOUSE, StructureTypeEnum.ORIEL, StructureMaterialEnum.STONE),
    WALL_CANOPY_WOODEN(-53, BuildingTypesEnum.HOUSE, StructureTypeEnum.CANOPY_DOOR, StructureMaterialEnum.WOOD),
    WALL_CANOPY_WOODEN_PLAN(-54, BuildingTypesEnum.HOUSE, StructureTypeEnum.HOUSE_PLAN_CANOPY, StructureMaterialEnum.WOOD),
    WALL_WINDOW_WIDE_WOODEN(-55, BuildingTypesEnum.HOUSE, StructureTypeEnum.WIDE_WINDOW, StructureMaterialEnum.WOOD),
    WALL_ORIEL_STONE_PLAIN(-56, BuildingTypesEnum.HOUSE, StructureTypeEnum.ORIEL, StructureMaterialEnum.PLAIN_STONE),
    WALL_ARCHED_SANDSTONE(-57, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED, StructureMaterialEnum.SANDSTONE),
    WALL_PORTCULLIS_SANDSTONE(-58, BuildingTypesEnum.HOUSE, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.SANDSTONE),
    WALL_BARRED_SANDSTONE(-59, BuildingTypesEnum.HOUSE, StructureTypeEnum.BARRED, StructureMaterialEnum.SANDSTONE),
    WALL_ORIEL_SANDSTONE(-60, BuildingTypesEnum.HOUSE, StructureTypeEnum.ORIEL, StructureMaterialEnum.SANDSTONE),
    WALL_LEFT_ARCH_SANDSTONE(-61, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.SANDSTONE),
    WALL_RIGHT_ARCH_SANDSTONE(-62, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.SANDSTONE),
    WALL_T_ARCH_SANDSTONE(-63, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.SANDSTONE),
    WALL_SOLID_RENDERED(-64, BuildingTypesEnum.HOUSE, StructureTypeEnum.SOLID, StructureMaterialEnum.RENDERED),
    WALL_WINDOW_RENDERED(-65, BuildingTypesEnum.HOUSE, StructureTypeEnum.WINDOW, StructureMaterialEnum.RENDERED),
    WALL_NARROW_WINDOW_RENDERED(-66, BuildingTypesEnum.HOUSE, StructureTypeEnum.NARROW_WINDOW, StructureMaterialEnum.RENDERED),
    WALL_DOOR_RENDERED(-67, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOOR, StructureMaterialEnum.RENDERED),
    WALL_DOUBLE_DOOR_RENDERED(-68, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.RENDERED),
    WALL_ARCHED_RENDERED(-69, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED, StructureMaterialEnum.RENDERED),
    WALL_PORTCULLIS_RENDERED(-70, BuildingTypesEnum.HOUSE, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.RENDERED),
    WALL_BARRED_RENDERED(-71, BuildingTypesEnum.HOUSE, StructureTypeEnum.BARRED, StructureMaterialEnum.RENDERED),
    WALL_ORIEL_RENDERED(-72, BuildingTypesEnum.HOUSE, StructureTypeEnum.ORIEL, StructureMaterialEnum.RENDERED),
    WALL_LEFT_ARCH_RENDERED(-73, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_LEFT, StructureMaterialEnum.RENDERED),
    WALL_RIGHT_ARCH_RENDERED(-74, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_RIGHT, StructureMaterialEnum.RENDERED),
    WALL_T_ARCH_RENDERED(-75, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.RENDERED),
    WALL_SOLID_MARBLE(-76, BuildingTypesEnum.HOUSE, StructureTypeEnum.SOLID, StructureMaterialEnum.MARBLE),
    WALL_WINDOW_MARBLE(-77, BuildingTypesEnum.HOUSE, StructureTypeEnum.WINDOW, StructureMaterialEnum.MARBLE),
    WALL_NARROW_WINDOW_MARBLE(-78, BuildingTypesEnum.HOUSE, StructureTypeEnum.NARROW_WINDOW, StructureMaterialEnum.MARBLE),
    WALL_DOOR_MARBLE(-79, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOOR, StructureMaterialEnum.MARBLE),
    WALL_DOUBLE_DOOR_MARBLE(-80, BuildingTypesEnum.HOUSE, StructureTypeEnum.DOUBLE_DOOR, StructureMaterialEnum.MARBLE),
    WALL_ARCHED_MARBLE(-81, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED, StructureMaterialEnum.MARBLE),
    WALL_T_ARCH_STONE(-127, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.PLAIN_STONE),
    WALL_T_ARCH_STONE_DECORATED(-128, BuildingTypesEnum.HOUSE, StructureTypeEnum.ARCHED_T, StructureMaterialEnum.STONE),
    WALL_SCAFFOLDING(128, BuildingTypesEnum.HOUSE, StructureTypeEnum.SCAFFOLDING, StructureMaterialEnum.CRUDE_WOOD),
    FENCE_WOODEN(0, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE, StructureMaterialEnum.WOOD),
    FENCE_PALISADE(1, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PALISADE, StructureMaterialEnum.LOG),
    FENCE_STONEWALL(2, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_WALL, StructureMaterialEnum.STONE),
    FENCE_WOODEN_GATE(3, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.GATE, StructureMaterialEnum.WOOD),
    FENCE_PALISADE_GATE(4, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.GATE, StructureMaterialEnum.LOG),
    FENCE_STONEWALL_HIGH(5, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_TALL, StructureMaterialEnum.STONE),
    FENCE_IRON(6, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS, StructureMaterialEnum.STONE),
    FENCE_IRON_GATE(7, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_GATE, StructureMaterialEnum.STONE),
    FENCE_WOVEN(8, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.WOVEN, StructureMaterialEnum.WOOD),
    FENCE_WOODEN_PARAPET(9, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.WOOD),
    FENCE_STONE_PARAPET(10, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.STONE),
    FENCE_STONE_IRON_PARAPET(11, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.IRON),
    FENCE_WOODEN_CRUDE(12, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE, StructureMaterialEnum.CRUDE_WOOD),
    FENCE_WOODEN_CRUDE_GATE(13, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.GATE, StructureMaterialEnum.CRUDE_WOOD),
    FENCE_SLATE(82, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE, StructureMaterialEnum.SLATE),
    FENCE_SLATE_IRON(83, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS, StructureMaterialEnum.SLATE),
    FENCE_SLATE_IRON_GATE(84, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_GATE, StructureMaterialEnum.SLATE),
    FENCE_ROUNDED_STONE(85, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE, StructureMaterialEnum.ROUNDED_STONE),
    FENCE_ROUNDED_STONE_IRON(86, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS, StructureMaterialEnum.ROUNDED_STONE),
    FENCE_ROUNDED_STONE_IRON_GATE(87, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_GATE, StructureMaterialEnum.ROUNDED_STONE),
    FENCE_POTTERY(88, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE, StructureMaterialEnum.POTTERY),
    FENCE_POTTERY_IRON(89, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS, StructureMaterialEnum.POTTERY),
    FENCE_POTTERY_IRON_GATE(90, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_GATE, StructureMaterialEnum.POTTERY),
    FENCE_SANDSTONE(91, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE, StructureMaterialEnum.SANDSTONE),
    FENCE_SANDSTONE_IRON(92, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS, StructureMaterialEnum.SANDSTONE),
    FENCE_SANDSTONE_IRON_GATE(93, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_GATE, StructureMaterialEnum.SANDSTONE),
    FENCE_RENDERED(94, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE, StructureMaterialEnum.RENDERED),
    FENCE_RENDERED_IRON(95, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS, StructureMaterialEnum.RENDERED),
    FENCE_RENDERED_IRON_GATE(96, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_GATE, StructureMaterialEnum.RENDERED),
    FENCE_MARBLE(97, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE, StructureMaterialEnum.MARBLE),
    FENCE_MARBLE_IRON(98, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS, StructureMaterialEnum.MARBLE),
    FENCE_MARBLE_IRON_GATE(99, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_GATE, StructureMaterialEnum.MARBLE),
    FENCE_ROPE_LOW(100, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.ROPE_LOW, StructureMaterialEnum.WOOD),
    FENCE_GARDESGARD_LOW(101, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.GARDESGARD_LOW, StructureMaterialEnum.WOOD),
    FENCE_GARDESGARD_HIGH(102, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.GARDESGARD_HIGH, StructureMaterialEnum.WOOD),
    FENCE_CURB(103, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.CURB, StructureMaterialEnum.STONE),
    FENCE_PLAN_WOODEN(-1, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_WOODEN, StructureMaterialEnum.WOOD),
    FENCE_PLAN_PALISADE(-2, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_PALISADE, StructureMaterialEnum.LOG),
    FENCE_PLAN_STONEWALL(-3, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL, StructureMaterialEnum.STONE),
    FENCE_PLAN_PALISADE_GATE(-4, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_PALISADE_GATE, StructureMaterialEnum.LOG),
    FENCE_PLAN_WOODEN_GATE(-5, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_WOODEN_GATE, StructureMaterialEnum.WOOD),
    FENCE_PLAN_STONEWALL_HIGH(-6, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL_HIGH, StructureMaterialEnum.STONE),
    FENCE_PLAN_IRON(-7, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS, StructureMaterialEnum.STONE),
    FENCE_PLAN_IRON_GATE(-8, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_GATE, StructureMaterialEnum.STONE),
    FENCE_PLAN_WOVEN(-9, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.WOVEN, StructureMaterialEnum.WOOD),
    FENCE_PLAN_WOODEN_PARAPET(-10, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_WOODEN_PARAPET, StructureMaterialEnum.WOOD),
    FENCE_PLAN_STONE_PARAPET(-11, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONE_PARAPET, StructureMaterialEnum.STONE),
    FENCE_PLAN_STONE_IRON_PARAPET(-12, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_PARAPET, StructureMaterialEnum.STONE),
    FENCE_PLAN_WOODEN_CRUDE(-13, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_CRUDE, StructureMaterialEnum.WOOD),
    FENCE_PLAN_WOODEN_GATE_CRUDE(-14, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_CRUDE_GATE, StructureMaterialEnum.WOOD),
    FENCE_PLAN_SLATE(-82, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL, StructureMaterialEnum.SLATE),
    FENCE_PLAN_SLATE_IRON(-83, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS, StructureMaterialEnum.SLATE),
    FENCE_PLAN_SLATE_IRON_GATE(-84, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_GATE, StructureMaterialEnum.SLATE),
    FENCE_PLAN_ROUNDED_STONE(-85, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL, StructureMaterialEnum.ROUNDED_STONE),
    FENCE_PLAN_ROUNDED_STONE_IRON(-86, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS, StructureMaterialEnum.ROUNDED_STONE),
    FENCE_PLAN_ROUNDED_STONE_IRON_GATE(-87, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_GATE, StructureMaterialEnum.ROUNDED_STONE),
    FENCE_PLAN_POTTERY(-88, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL, StructureMaterialEnum.POTTERY),
    FENCE_PLAN_POTTERY_IRON(-89, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS, StructureMaterialEnum.POTTERY),
    FENCE_PLAN_POTTERY_IRON_GATE(-90, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_GATE, StructureMaterialEnum.POTTERY),
    FENCE_PLAN_SANDSTONE(-91, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL, StructureMaterialEnum.SANDSTONE),
    FENCE_PLAN_SANDSTONE_IRON(-92, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS, StructureMaterialEnum.SANDSTONE),
    FENCE_PLAN_SANDSTONE_IRON_GATE(-93, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_GATE, StructureMaterialEnum.SANDSTONE),
    FENCE_PLAN_RENDERED(-94, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL, StructureMaterialEnum.RENDERED),
    FENCE_PLAN_RENDERED_IRON(-95, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS, StructureMaterialEnum.RENDERED),
    FENCE_PLAN_RENDERED_IRON_GATE(-96, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_GATE, StructureMaterialEnum.RENDERED),
    FENCE_PLAN_MARBLE(-97, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL, StructureMaterialEnum.MARBLE),
    FENCE_PLAN_MARBLE_IRON(-98, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS, StructureMaterialEnum.MARBLE),
    FENCE_PLAN_MARBLE_IRON_GATE(-99, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_GATE, StructureMaterialEnum.MARBLE),
    FENCE_PLAN_ROPE_LOW(-100, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_ROPE_LOW, StructureMaterialEnum.WOOD),
    FENCE_PLAN_GARDESGARD_LOW(-101, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_GARDESGARD_LOW, StructureMaterialEnum.WOOD),
    FENCE_PLAN_GARDESGARD_HIGH(-102, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_GARDESGARD_HIGH, StructureMaterialEnum.WOOD),
    FENCE_PLAN_CURB(-103, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_CURB, StructureMaterialEnum.STONE),
    FENCE_PLAN_ROPE_HIGH(-104, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_ROPE_HIGH, StructureMaterialEnum.WOOD),
    FENCE_PLAN_GARDESGARD_GATE(-105, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_GARDESGARD_GATE, StructureMaterialEnum.WOOD),
    FENCE_STONE(-106, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE, StructureMaterialEnum.STONE),
    FENCE_PLAN_STONE(-107, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONE_FENCE, StructureMaterialEnum.STONE),
    FENCE_IRON_HIGH(-108, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL, StructureMaterialEnum.STONE),
    FENCE_PLAN_IRON_HIGH(-109, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL, StructureMaterialEnum.STONE),
    FENCE_IRON_GATE_HIGH(-110, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL_GATE, StructureMaterialEnum.STONE),
    FENCE_PLAN_IRON_GATE_HIGH(-111, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL_GATE, StructureMaterialEnum.STONE),
    FENCE_SLATE_TALL_STONE_WALL(129, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_TALL, StructureMaterialEnum.SLATE),
    FENCE_SLATE_PORTCULLIS(130, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.SLATE),
    FENCE_SLATE_HIGH_IRON_FENCE(131, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL, StructureMaterialEnum.SLATE),
    FENCE_SLATE_HIGH_IRON_FENCE_GATE(132, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL_GATE, StructureMaterialEnum.SLATE),
    FENCE_SLATE_STONE_PARAPET(133, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.SLATE),
    FENCE_SLATE_CHAIN_FENCE(134, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MEDIUM_CHAIN, StructureMaterialEnum.SLATE),
    FENCE_ROUNDED_STONE_TALL_STONE_WALL(135, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_TALL, StructureMaterialEnum.ROUNDED_STONE),
    FENCE_ROUNDED_STONE_PORTCULLIS(136, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.ROUNDED_STONE),
    FENCE_ROUNDED_STONE_HIGH_IRON_FENCE(137, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL, StructureMaterialEnum.ROUNDED_STONE),
    FENCE_ROUNDED_STONE_HIGH_IRON_FENCE_GATE(138, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL_GATE, StructureMaterialEnum.ROUNDED_STONE),
    FENCE_ROUNDED_STONE_STONE_PARAPET(139, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.ROUNDED_STONE),
    FENCE_ROUNDED_STONE_CHAIN_FENCE(140, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MEDIUM_CHAIN, StructureMaterialEnum.ROUNDED_STONE),
    FENCE_SANDSTONE_TALL_STONE_WALL(141, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_TALL, StructureMaterialEnum.SANDSTONE),
    FENCE_SANDSTONE_PORTCULLIS(142, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.SANDSTONE),
    FENCE_SANDSTONE_HIGH_IRON_FENCE(143, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL, StructureMaterialEnum.SANDSTONE),
    FENCE_SANDSTONE_HIGH_IRON_FENCE_GATE(144, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL_GATE, StructureMaterialEnum.SANDSTONE),
    FENCE_SANDSTONE_STONE_PARAPET(145, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.SANDSTONE),
    FENCE_SANDSTONE_CHAIN_FENCE(146, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MEDIUM_CHAIN, StructureMaterialEnum.SANDSTONE),
    FENCE_RENDERED_TALL_STONE_WALL(147, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_TALL, StructureMaterialEnum.RENDERED),
    FENCE_RENDERED_PORTCULLIS(148, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.RENDERED),
    FENCE_RENDERED_HIGH_IRON_FENCE(149, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL, StructureMaterialEnum.RENDERED),
    FENCE_RENDERED_HIGH_IRON_FENCE_GATE(150, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL_GATE, StructureMaterialEnum.RENDERED),
    FENCE_RENDERED_STONE_PARAPET(151, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.RENDERED),
    FENCE_RENDERED_CHAIN_FENCE(152, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MEDIUM_CHAIN, StructureMaterialEnum.RENDERED),
    FENCE_POTTERY_TALL_STONE_WALL(153, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_TALL, StructureMaterialEnum.POTTERY),
    FENCE_POTTERY_PORTCULLIS(154, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.POTTERY),
    FENCE_POTTERY_HIGH_IRON_FENCE(155, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL, StructureMaterialEnum.POTTERY),
    FENCE_POTTERY_HIGH_IRON_FENCE_GATE(156, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL_GATE, StructureMaterialEnum.POTTERY),
    FENCE_POTTERY_STONE_PARAPET(157, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.POTTERY),
    FENCE_POTTERY_CHAIN_FENCE(158, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MEDIUM_CHAIN, StructureMaterialEnum.POTTERY),
    FENCE_MARBLE_TALL_STONE_WALL(159, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_TALL, StructureMaterialEnum.MARBLE),
    FENCE_MARBLE_PORTCULLIS(160, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.MARBLE),
    FENCE_MARBLE_HIGH_IRON_FENCE(161, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL, StructureMaterialEnum.MARBLE),
    FENCE_MARBLE_HIGH_IRON_FENCE_GATE(162, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_IRON_BARS_TALL_GATE, StructureMaterialEnum.MARBLE),
    FENCE_MARBLE_STONE_PARAPET(163, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.MARBLE),
    FENCE_MARBLE_CHAIN_FENCE(164, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MEDIUM_CHAIN, StructureMaterialEnum.MARBLE),
    FENCE_PLAN_SLATE_TALL_STONE_WALL(165, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL_HIGH, StructureMaterialEnum.SLATE),
    FENCE_PLAN_SLATE_PORTCULLIS(166, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.SLATE),
    FENCE_PLAN_SLATE_HIGH_IRON_FENCE(167, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL, StructureMaterialEnum.SLATE),
    FENCE_PLAN_SLATE_HIGH_IRON_FENCE_GATE(168, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL_GATE, StructureMaterialEnum.SLATE),
    FENCE_PLAN_SLATE_STONE_PARAPET(169, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.SLATE),
    FENCE_PLAN_SLATE_CHAIN_FENCE(170, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MEDIUM_CHAIN, StructureMaterialEnum.SLATE),
    FENCE_PLAN_ROUNDED_STONE_TALL_STONE_WALL(171, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL_HIGH, StructureMaterialEnum.ROUNDED_STONE),
    FENCE_PLAN_ROUNDED_STONE_PORTCULLIS(172, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.ROUNDED_STONE),
    FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE(173, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL, StructureMaterialEnum.ROUNDED_STONE),
    FENCE_PLAN_ROUNDED_STONE_HIGH_IRON_FENCE_GATE(174, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL_GATE, StructureMaterialEnum.ROUNDED_STONE),
    FENCE_PLAN_ROUNDED_STONE_STONE_PARAPET(175, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.ROUNDED_STONE),
    FENCE_PLAN_ROUNDED_STONE_CHAIN_FENCE(176, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MEDIUM_CHAIN, StructureMaterialEnum.ROUNDED_STONE),
    FENCE_PLAN_SANDSTONE_TALL_STONE_WALL(177, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL_HIGH, StructureMaterialEnum.SANDSTONE),
    FENCE_PLAN_SANDSTONE_PORTCULLIS(178, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.SANDSTONE),
    FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE(179, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL, StructureMaterialEnum.SANDSTONE),
    FENCE_PLAN_SANDSTONE_HIGH_IRON_FENCE_GATE(180, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL_GATE, StructureMaterialEnum.SANDSTONE),
    FENCE_PLAN_SANDSTONE_STONE_PARAPET(181, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.SANDSTONE),
    FENCE_PLAN_SANDSTONE_CHAIN_FENCE(182, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MEDIUM_CHAIN, StructureMaterialEnum.SANDSTONE),
    FENCE_PLAN_RENDERED_TALL_STONE_WALL(183, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL_HIGH, StructureMaterialEnum.RENDERED),
    FENCE_PLAN_RENDERED_PORTCULLIS(184, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.RENDERED),
    FENCE_PLAN_RENDERED_HIGH_IRON_FENCE(185, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL, StructureMaterialEnum.RENDERED),
    FENCE_PLAN_RENDERED_HIGH_IRON_FENCE_GATE(186, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL_GATE, StructureMaterialEnum.RENDERED),
    FENCE_PLAN_RENDERED_STONE_PARAPET(187, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.RENDERED),
    FENCE_PLAN_RENDERED_CHAIN_FENCE(188, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MEDIUM_CHAIN, StructureMaterialEnum.RENDERED),
    FENCE_PLAN_POTTERY_TALL_STONE_WALL(189, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL_HIGH, StructureMaterialEnum.POTTERY),
    FENCE_PLAN_POTTERY_PORTCULLIS(190, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.POTTERY),
    FENCE_PLAN_POTTERY_HIGH_IRON_FENCE(191, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL, StructureMaterialEnum.POTTERY),
    FENCE_PLAN_POTTERY_HIGH_IRON_FENCE_GATE(192, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL_GATE, StructureMaterialEnum.POTTERY),
    FENCE_PLAN_POTTERY_STONE_PARAPET(193, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.POTTERY),
    FENCE_PLAN_POTTERY_CHAIN_FENCE(194, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MEDIUM_CHAIN, StructureMaterialEnum.POTTERY),
    FENCE_PLAN_MARBLE_TALL_STONE_WALL(195, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_STONEWALL_HIGH, StructureMaterialEnum.MARBLE),
    FENCE_PLAN_MARBLE_PORTCULLIS(196, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.MARBLE),
    FENCE_PLAN_MARBLE_HIGH_IRON_FENCE(197, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL, StructureMaterialEnum.MARBLE),
    FENCE_PLAN_MARBLE_HIGH_IRON_FENCE_GATE(198, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_IRON_BARS_TALL_GATE, StructureMaterialEnum.MARBLE),
    FENCE_PLAN_MARBLE_STONE_PARAPET(199, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PARAPET, StructureMaterialEnum.MARBLE),
    FENCE_PLAN_MARBLE_CHAIN_FENCE(200, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MEDIUM_CHAIN, StructureMaterialEnum.MARBLE),
    FLOWERBED_YELLOW(-112, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FLOWERBED, StructureMaterialEnum.FLOWER1),
    FLOWERBED_ORANGE_RED(-113, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FLOWERBED, StructureMaterialEnum.FLOWER1),
    FLOWERBED_PURPLE(-114, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FLOWERBED, StructureMaterialEnum.FLOWER1),
    FLOWERBED_WHITE(-115, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FLOWERBED, StructureMaterialEnum.FLOWER1),
    FLOWERBED_BLUE(-116, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FLOWERBED, StructureMaterialEnum.FLOWER1),
    FLOWERBED_GREENISH_YELLOW(-117, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FLOWERBED, StructureMaterialEnum.FLOWER1),
    FLOWERBED_WHITE_DOTTED(-118, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FLOWERBED, StructureMaterialEnum.FLOWER1),
    FENCE_MAGIC_FIRE(-119, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MAGIC_FENCE, StructureMaterialEnum.FIRE),
    FENCE_MAGIC_ICE(-120, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MAGIC_FENCE, StructureMaterialEnum.ICE),
    FENCE_PLAN_MEDIUM_CHAIN(-121, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_MEDIUM_CHAIN, StructureMaterialEnum.STONE),
    FENCE_MEDIUM_CHAIN(-122, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.MEDIUM_CHAIN, StructureMaterialEnum.STONE),
    FENCE_PLAN_PORTCULLIS(-123, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.FENCE_PLAN_PORTCULLIS, StructureMaterialEnum.STONE),
    FENCE_PORTCULLIS(-124, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.PORTCULLIS, StructureMaterialEnum.STONE),
    FENCE_RUBBLE(-125, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.RUBBLE, StructureMaterialEnum.STONE),
    FENCE_SIEGEWALL(-126, BuildingTypesEnum.ALLFENCES, StructureTypeEnum.SIEGWALL, StructureMaterialEnum.WOOD);

    public final short value;
    public final BuildingTypesEnum structureType;
    public final StructureTypeEnum type;
    public final StructureMaterialEnum material;
    private String modelPath = "";
    private String texturePath = "";
    private int icon = -1;
    public final boolean useNewStringBuilder = false;
    private final HashMap<String, StructureConstantsEnum> lookupMap = new HashMap();

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
        if (id <= StructureConstantsEnum.values().length && id >= 0) {
            return StructureConstantsEnum.values()[id];
        }
        Logger.getGlobal().warning("Value not a valid id: " + id + " RETURNING WALL_WINDOW_STONE_PLAN(VAL=-31)!");
        return WALL_WINDOW_STONE_PLAN;
    }

    public static StructureConstantsEnum getEnumByValue(short value) {
        for (StructureConstantsEnum e : StructureConstantsEnum.values()) {
            if (e.value != value) continue;
            return e;
        }
        Logger.getGlobal().warning("Reached default return value for value=" + value + " RETURNING WALL_WINDOW_STONE_PLAN(VAL=-31)!");
        return WALL_WINDOW_STONE_PLAN;
    }
}

