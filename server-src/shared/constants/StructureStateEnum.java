/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.shared.constants;

import java.util.logging.Logger;

public enum StructureStateEnum {
    UNINITIALIZED(0),
    INITIALIZED(1),
    STATE_2_NEEDED(2),
    STATE_3_NEEDED(3),
    STATE_4_NEEDED(4),
    STATE_5_NEEDED(5),
    STATE_6_NEEDED(6),
    STATE_7_NEEDED(7),
    STATE_8_NEEDED(8),
    STATE_9_NEEDED(9),
    STATE_10_NEEDED(10),
    STATE_11_NEEDED(11),
    STATE_12_NEEDED(12),
    STATE_13_NEEDED(13),
    STATE_14_NEEDED(14),
    STATE_15_NEEDED(15),
    STATE_16_NEEDED(16),
    STATE_17_NEEDED(17),
    STATE_18_NEEDED(18),
    STATE_19_NEEDED(19),
    STATE_20_NEEDED(20),
    STATE_21_NEEDED(21),
    STATE_22_NEEDED(22),
    STATE_23_NEEDED(23),
    STATE_24_NEEDED(24),
    STATE_25_NEEDED(25),
    STATE_26_NEEDED(26),
    STATE_27_NEEDED(27),
    STATE_28_NEEDED(28),
    STATE_29_NEEDED(29),
    STATE_30_NEEDED(30),
    STATE_31_NEEDED(31),
    STATE_32_NEEDED(32),
    STATE_33_NEEDED(33),
    STATE_34_NEEDED(34),
    STATE_35_NEEDED(35),
    STATE_36_NEEDED(36),
    STATE_37_NEEDED(37),
    STATE_38_NEEDED(38),
    STATE_39_NEEDED(39),
    STATE_40_NEEDED(40),
    STATE_41_NEEDED(41),
    STATE_42_NEEDED(42),
    STATE_43_NEEDED(43),
    STATE_44_NEEDED(44),
    STATE_45_NEEDED(45),
    STATE_46_NEEDED(46),
    STATE_47_NEEDED(47),
    STATE_48_NEEDED(48),
    STATE_49_NEEDED(49),
    STATE_50_NEEDED(50),
    STATE_51_NEEDED(51),
    STATE_52_NEEDED(52),
    STATE_53_NEEDED(53),
    STATE_54_NEEDED(54),
    STATE_55_NEEDED(55),
    STATE_56_NEEDED(56),
    STATE_57_NEEDED(57),
    STATE_58_NEEDED(58),
    STATE_59_NEEDED(59),
    STATE_60_NEEDED(60),
    STATE_61_NEEDED(61),
    STATE_62_NEEDED(62),
    STATE_63_NEEDED(63),
    STATE_64_NEEDED(64),
    STATE_65_NEEDED(65),
    STATE_66_NEEDED(66),
    STATE_67_NEEDED(67),
    STATE_68_NEEDED(68),
    STATE_69_NEEDED(69),
    STATE_70_NEEDED(70),
    STATE_71_NEEDED(71),
    STATE_72_NEEDED(72),
    STATE_73_NEEDED(73),
    STATE_74_NEEDED(74),
    STATE_75_NEEDED(75),
    STATE_76_NEEDED(76),
    STATE_77_NEEDED(77),
    STATE_78_NEEDED(78),
    STATE_79_NEEDED(79),
    STATE_80_NEEDED(80),
    STATE_81_NEEDED(81),
    STATE_82_NEEDED(82),
    STATE_83_NEEDED(83),
    STATE_84_NEEDED(84),
    STATE_85_NEEDED(85),
    STATE_86_NEEDED(86),
    STATE_87_NEEDED(87),
    STATE_88_NEEDED(88),
    STATE_89_NEEDED(89),
    STATE_90_NEEDED(90),
    STATE_91_NEEDED(91),
    STATE_92_NEEDED(92),
    STATE_93_NEEDED(93),
    STATE_94_NEEDED(94),
    STATE_95_NEEDED(95),
    STATE_96_NEEDED(96),
    STATE_97_NEEDED(97),
    STATE_98_NEEDED(98),
    STATE_99_NEEDED(99),
    STATE_100_NEEDED(100),
    STATE_101_NEEDED(101),
    STATE_102_NEEDED(102),
    STATE_103_NEEDED(103),
    STATE_104_NEEDED(104),
    STATE_105_NEEDED(105),
    STATE_106_NEEDED(106),
    STATE_107_NEEDED(107),
    STATE_108_NEEDED(108),
    STATE_109_NEEDED(109),
    STATE_110_NEEDED(110),
    STATE_111_NEEDED(111),
    STATE_112_NEEDED(112),
    STATE_113_NEEDED(113),
    STATE_114_NEEDED(114),
    STATE_115_NEEDED(115),
    STATE_116_NEEDED(116),
    STATE_117_NEEDED(117),
    STATE_118_NEEDED(118),
    STATE_119_NEEDED(119),
    STATE_120_NEEDED(120),
    STATE_121_NEEDED(121),
    STATE_122_NEEDED(122),
    STATE_123_NEEDED(123),
    STATE_124_NEEDED(124),
    STATE_125_NEEDED(125),
    STATE_126_NEEDED(126),
    FINISHED(127),
    WALL_PLAN(0);

    public final byte state;
    private static final Logger logger;

    private StructureStateEnum(byte value) {
        this.state = value;
    }

    public static StructureStateEnum getStateByValue(byte value) {
        if (value >= 0 && value < StructureStateEnum.values().length) {
            return StructureStateEnum.values()[value];
        }
        logger.warning("Value not a valid state: " + value + " RETURNING PLAN(VAL=0)!");
        return WALL_PLAN;
    }

    static {
        logger = Logger.getLogger(StructureStateEnum.class.getName());
    }
}

