/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.WurmHarvestables;
import com.wurmonline.server.behaviours.Methods;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.questions.Question;
import java.text.DecimalFormat;
import java.util.Properties;
import java.util.logging.Level;

public final class ShowHarvestableInfo
extends Question {
    private Item paper;
    private Item almanac;
    private WurmHarvestables.Harvestable harvestable;
    private int filter = 0;
    private static final String red = "color=\"255,127,127\"";
    private static final String green = "color=\"127,255,127\"";
    private static final String orange = "color=\"255,177,40\";";
    private static final DecimalFormat df = new DecimalFormat("#.##");
    private static final int ALL = 0;
    private static final int HARVESTABLE = 1;
    private static final int ALMOST_RIPE = 2;
    private static final int NEARLY_HARVESTABLE = 3;

    public ShowHarvestableInfo(Creature aResponder, Item paper, WurmHarvestables.Harvestable harvestable) {
        super(aResponder, ShowHarvestableInfo.getReportName(paper, harvestable), ShowHarvestableInfo.getReportName(paper, harvestable), 141, -10L);
        this.almanac = null;
        this.paper = paper;
        this.harvestable = harvestable;
    }

    private static String getReportName(Item paper, WurmHarvestables.Harvestable harvestable) {
        return LoginHandler.raiseFirstLetter(harvestable.getName() + " report (" + df.format(paper.getCurrentQualityLevel()) + ")");
    }

    public ShowHarvestableInfo(Creature aResponder, Item almanac) {
        this(aResponder, almanac, 0);
    }

    public ShowHarvestableInfo(Creature aResponder, Item almanac, int summarytype) {
        super(aResponder, ShowHarvestableInfo.getSummaryName(almanac, summarytype), ShowHarvestableInfo.getSummaryName(almanac, summarytype), 141, -10L);
        this.almanac = almanac;
        this.paper = null;
        this.harvestable = null;
        this.filter = summarytype;
    }

    private static String getSummaryName(Item almanac, int summarytype) {
        String summaryType;
        switch (summarytype) {
            case 1: {
                summaryType = " Harvestable";
                break;
            }
            case 2: {
                summaryType = " Almost Ripe";
                break;
            }
            case 3: {
                summaryType = " (Nearly) Harvestable";
                break;
            }
            default: {
                summaryType = "";
            }
        }
        String almanacType = almanac.getTemplateId() == 1127 ? "Almanac" : almanac.getName();
        return almanacType + summaryType + " Summary";
    }

    @Override
    public void answer(Properties answers) {
        this.setAnswer(answers);
        if (this.type == 0) {
            logger.log(Level.INFO, "Received answer for a question with NOQUESTION.");
            return;
        }
        if (this.type == 141 && this.almanac != null) {
            boolean show = this.getBooleanProp("show");
            String sbyid = this.getAnswer().getProperty("by");
            int byid = Integer.parseInt(sbyid);
            if (show) {
                ShowHarvestableInfo shi = new ShowHarvestableInfo(this.getResponder(), this.almanac, byid);
                shi.sendQuestion();
            }
        }
    }

    @Override
    public void sendQuestion() {
        StringBuilder buf = new StringBuilder();
        String dropdown = "";
        if (this.almanac != null) {
            dropdown = "button{text=\"Show\";id=\"show\"};label{text=\" \"};dropdown{id=\"by\";default=\"" + this.filter + "\";options=\"All,Harvestables,Almost Ripe,(Nearly) Harvestable\"};";
        }
        buf.append("border{border{size=\"20,20\";null;null;label{type='bold';text=\"" + this.question + "\"};harray{" + dropdown + "label{text=\" \"};button{text=\"Close\";id=\"close\"};label{text=\" \"}};null;}null;scroll{vertical=\"true\";horizontal=\"false\";varray{rescale=\"true\";passthrough{id=\"id\";text=\"" + this.getId() + "\"}");
        if (this.harvestable != null) {
            switch (this.harvestable.getHarvestableId()) {
                case 1: {
                    buf.append(this.showOliveInfo());
                    break;
                }
                case 2: {
                    buf.append(this.showGrapeInfo());
                    break;
                }
                case 3: {
                    buf.append(this.showCherryInfo());
                    break;
                }
                case 4: {
                    buf.append(this.showAppleInfo());
                    break;
                }
                case 5: {
                    buf.append(this.showLemonInfo());
                    break;
                }
                case 6: {
                    buf.append(this.showOleanderInfo());
                    break;
                }
                case 7: {
                    buf.append(this.showCamelliaInfo());
                    break;
                }
                case 8: {
                    buf.append(this.showLavenderInfo());
                    break;
                }
                case 9: {
                    buf.append(this.showMapleInfo());
                    break;
                }
                case 10: {
                    buf.append(this.showRoseInfo());
                    break;
                }
                case 11: {
                    buf.append(this.showChestnutInfo());
                    break;
                }
                case 12: {
                    buf.append(this.showWalnutInfo());
                    break;
                }
                case 13: {
                    buf.append(this.showPineInfo());
                    break;
                }
                case 14: {
                    buf.append(this.showHazelInfo());
                    break;
                }
                case 15: {
                    buf.append(this.showHopsInfo());
                    break;
                }
                case 16: {
                    buf.append(this.showOakInfo());
                    break;
                }
                case 17: {
                    buf.append(this.showOrangeInfo());
                    break;
                }
                case 18: {
                    buf.append(this.showRaspberryInfo());
                    break;
                }
                case 19: {
                    buf.append(this.showBlueberryInfo());
                    break;
                }
                case 20: {
                    buf.append(this.showLingonberryInfo());
                }
            }
            buf.append(this.showHarvestableTimes());
        } else if (this.almanac != null) {
            buf.append(this.showSummary());
        }
        buf.append("label{text=\"\"}");
        buf.append("}};null;null;}");
        this.getResponder().getCommunicator().sendBml(500, 450, true, true, buf.toString(), 200, 200, 200, this.title);
    }

    private String showSummary() {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"\"}");
        Item[] reports = Methods.getBestReports(this.getResponder(), this.almanac);
        if (reports.length == 0) {
            buf.append("label{text=\"No Reports found!\"}");
        } else {
            if (this.almanac.getOwnerId() == this.getResponder().getWurmId() && reports.length == 20) {
                this.getResponder().achievement(575);
            }
            boolean showedSome = false;
            buf.append("table{rows=\"1\";cols=\"5\";text{type=\"bold\";text=\"Report\"};label{type=\"bold\";text=\"QL\"};label{type=\"bold\";text=\"State\"};label{type=\"bold\";text=\"Harvest Start\"};label{type=\"bold\";text=\"Harvest End\"};");
            for (Item report : reports) {
                WurmHarvestables.Harvestable harvestable = report.getHarvestable();
                if (harvestable != null) {
                    boolean output = false;
                    long howGood = (long)(report.getCurrentQualityLevel() * 86400.0f);
                    String harvestStart = "";
                    String harvestEnd = "";
                    String colourStart = "";
                    String colourState = "";
                    if (harvestable.isHarvestable() && this.showSummaryHarvestables()) {
                        if (this.knowHarvestStart(harvestable, howGood)) {
                            harvestStart = WurmCalendar.getDaysFrom(harvestable.getSeasonStart());
                            harvestEnd = WurmCalendar.getDaysFrom(harvestable.getSeasonEnd());
                        } else if (this.knowHarvestEnd(harvestable, howGood)) {
                            harvestEnd = WurmCalendar.getDaysFrom(harvestable.getSeasonEnd());
                        }
                        colourStart = green;
                        colourState = green;
                        output = true;
                    } else if (harvestable.isAlmostRipe() && this.showSummaryAlmostRipe()) {
                        if (this.knowHarvestStart(harvestable, howGood)) {
                            harvestStart = WurmCalendar.getDaysFrom(harvestable.getSeasonStart());
                        } else {
                            harvestStart = WurmCalendar.getDaysFrom(harvestable.getDefaultSeasonStart());
                            colourStart = orange;
                        }
                        if (this.knowHarvestEnd(harvestable, howGood)) {
                            harvestEnd = WurmCalendar.getDaysFrom(harvestable.getSeasonEnd());
                        } else {
                            harvestEnd = WurmCalendar.getDaysFrom(harvestable.getDefaultSeasonEnd());
                            colourStart = orange;
                        }
                        output = true;
                    } else if (this.showSummaryAll()) {
                        if (this.knowHarvestStart(harvestable, howGood)) {
                            harvestStart = WurmCalendar.getDaysFrom(harvestable.getSeasonStart());
                        } else {
                            harvestStart = WurmCalendar.getDaysFrom(harvestable.getDefaultSeasonStart());
                            colourStart = orange;
                        }
                        output = true;
                    }
                    if (!output) continue;
                    buf.append("label{" + colourState + "text=\"" + harvestable.getName() + "\"};label{" + colourState + "text=\"" + df.format(report.getCurrentQualityLevel()) + "\"};label{" + colourState + "text=\"" + harvestable.getState() + "\"};label{" + colourStart + "text=\"" + harvestStart + "\"};label{" + colourState + "text=\"" + harvestEnd + "\"};");
                    showedSome = true;
                    continue;
                }
                buf.append("label{color=\"255,127,127\"text=\"Invalid\"};label{color=\"255,127,127\"text=\"\"};label{color=\"255,127,127\"text=\"Unknown\"};label{color=\"255,127,127\"text=\"Unknown\"};label{color=\"255,127,127\"text=\"Unknown\"};");
            }
            buf.append("}");
            if (!showedSome) {
                buf.append("label{text=\"Nothing to show!\"}");
            }
            buf.append("label{text=\"\"}");
            buf.append("label{type=\"bold\";text=\"Notes:\"}");
            buf.append("label{color=\"127,255,127\"text=\"Green text is for currently harvestable.\"}");
            buf.append("label{color=\"255,177,40\";text=\"Times in orange are the default ones.\"}");
            buf.append("label{text=\"White text is from the best report.\"}");
        }
        return buf.toString();
    }

    private boolean showSummaryAll() {
        return this.filter == 0;
    }

    private boolean showSummaryHarvestables() {
        return this.filter == 0 || this.filter == 1 || this.filter == 3;
    }

    private boolean showSummaryAlmostRipe() {
        return this.filter == 0 || this.filter == 2 || this.filter == 3;
    }

    private String showHarvestableTimes() {
        StringBuilder buf = new StringBuilder();
        buf.append("label{text=\"\"}");
        buf.append("label{type=\"bold\";text=\"Harvestable Times\"}");
        long howGood = (long)(this.paper.getCurrentQualityLevel() * 86400.0f);
        if (this.harvestable.isHarvestable()) {
            buf.append("text{text=\"It is currently harvestable.\"}");
            if (this.knowHarvestStart(this.harvestable, howGood)) {
                buf.append("text{text=\"The harvest season started " + WurmCalendar.getDaysFrom(this.harvestable.getSeasonStart()) + "\"}");
            }
            if (this.knowHarvestEnd(this.harvestable, howGood)) {
                buf.append("text{text=\"The harvest season should end in " + WurmCalendar.getDaysFrom(this.harvestable.getSeasonEnd()) + "\"}");
            }
        } else if (this.harvestable.isAlmostRipe()) {
            if (this.knowHarvestStart(this.harvestable, howGood)) {
                buf.append("text{text=\"The harvest season will start in " + WurmCalendar.getDaysFrom(this.harvestable.getSeasonStart()) + "\"}");
            } else {
                buf.append("text{text=\"The default harvest season is in " + WurmCalendar.getDaysFrom(this.harvestable.getDefaultSeasonStart()) + "\"}");
            }
            if (this.knowHarvestEnd(this.harvestable, howGood)) {
                buf.append("text{text=\"The harvest season should end in " + WurmCalendar.getDaysFrom(this.harvestable.getSeasonEnd()) + "\"}");
            }
        } else if (this.knowHarvestStart(this.harvestable, howGood)) {
            buf.append("text{text=\"The harvest season will start in " + WurmCalendar.getDaysFrom(this.harvestable.getSeasonStart()) + "\"}");
        } else {
            buf.append("text{text=\"The default harvest season is in " + WurmCalendar.getDaysFrom(this.harvestable.getDefaultSeasonStart()) + " This is only approximate as it can be plus or minus 2 weeks from that date. \"}");
        }
        return buf.toString();
    }

    private boolean knowHarvestEnd(WurmHarvestables.Harvestable harvestable, long howGood) {
        return harvestable.getSeasonEnd() - howGood < WurmCalendar.getCurrentTime();
    }

    private boolean knowHarvestStart(WurmHarvestables.Harvestable harvestable, long howGood) {
        if (harvestable.isHarvestable()) {
            return harvestable.getSeasonStart() + howGood > WurmCalendar.getCurrentTime();
        }
        return harvestable.getSeasonStart() - howGood < WurmCalendar.getCurrentTime();
    }

    private String showOliveInfo() {
        StringBuilder buf = new StringBuilder();
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.tree.olive\";size=\"128,128\";text=\"Olive tree\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"The olive tree, known by the botanical name Olea wurmea, meaning ''Wurm olive'', is a small tree with a wide, gnarled trunk.  It is found and cultivated in many places and considered naturalized in all the lands of Wurm.  It is part of the Oleaceae family, and represents the earliest source of oil in Wurm history.\"}");
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.produce.olive\";size=\"128,128\";text=\"Olive fruit\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"The fruit of the olive tree, also called the olive, is of major agricultural importance in wurm as the source of olive oil. The tree and its fruit give their name to the plant family. The word ''oil'' in multiple languages ultimately derives from the name of this tree and its fruit, signifying its importance in many economies.\"}");
        return buf.toString();
    }

    private String showGrapeInfo() {
        StringBuilder buf = new StringBuilder();
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.bush.grape\";size=\"128,128\";text=\"Grape bush\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"The grape bush is a deciduous, woody vine, of the flowering plant genus Vitis.  Reaching a height of about 1.5 meters when fully grown, it can cover large areas if allowed to grow wild.  Vitis vinifera is found throughout the lands of Wurm, but it has evolved into warmer and cooler climate sub-varieties.\"}");
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.trellis.grape\";size=\"128,128\";text=\"Grape trellis\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"In recent years, thanks to advances in horticultural and gardening techniques, grapes have become available to be grown on trellises.  While purists bemoan the loss of rustic charm of fields of grape vines, farmers everywhere insist that the flavour of trellis-grown grapes is every bit as good as 'natural' ones.\"}");
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.produce.grape\";size=\"128,128\";text=\"Green and Blue grapes\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"Grapes can be eaten fresh as table grapes or they can be used for making wine, jam, juice, jelly, or various other dishes. Grapes are a non-climacteric type of fruit, generally occurring in clusters.\"}");
        buf.append("label{text=\" \"}");
        buf.append("text{text=\"Green grapes, preferring a cooler climate, only grow in the North; warmer climate blue grapes are found in Southern regions.\"}");
        return buf.toString();
    }

    private String showCherryInfo() {
        StringBuilder buf = new StringBuilder();
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.tree.cherry\";size=\"128,128\";text=\"Cherry tree\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"The Wurmian cherry, or Prunus avia wurmosa, is found in most areas.  Known for its distinctive flowers, which occur in small corymbs of several together, the fruit is smooth skinned, with a weak groove (or no groove) along one side.\"}");
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.produce.cherry\";size=\"128,128\";text=\"Cherries\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"Cherry fruit is a fleshy drupe (stone fruit), growing in clusters of 2 or 3.  Wurmians have been known to use cherries for a variety of cooked dishes, as well as several drinks (both alcoholic and non-alcoholic).  It is also of great economic value to alchemists, as the juice may be used for tile transformation.\"}");
        buf.append("label{text=\" \"}");
        buf.append("text{text=\"Due to the small size of Prunus trees, cherry wood is time-consuming to harvest in large quantities, and requires advanced carpentry techniques to combine the inevitable small harvested logs into usable lumber.\"}");
        return buf.toString();
    }

    private String showAppleInfo() {
        StringBuilder buf = new StringBuilder();
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.tree.apple\";size=\"128,128\";text=\"Apple tree\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"The apple tree (Malus pumila, commonly and erroneously called Malus domestica) is a deciduous tree best known for its sweet, pomaceous fruit, the apple. It is cultivated Wurmwide as a fruit tree, and is the most widely grown species in the genus Malus.  It is related to the common rose, and is one of the oldest known fruit trees in the world. \"}");
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.produce.apple\";size=\"128,128\";text=\"Green Apple\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"Malus domestica was first discovered by an adventurer called Bramley, and is thus commonly known as the Bramley apple, or simply Bramleys.  It is usually eaten cooked due to its sourness, although some epicureans have been known to eat this apple raw in order to cleanse the palate.  It is more usually used for cooking, appearing in a variety of sauces, pies and other dishes.  A peculiarity of the variety is that when cooked, it becomes fluffy and golden, taking on a much lighter flavour.\"}");
        buf.append("label{text=\" \"}");
        buf.append("text{text=\"Apples may be pressed to produce a cloudy and much-prized juice, which is rumoured to have uses in alchemy.\"}");
        return buf.toString();
    }

    private String showLemonInfo() {
        StringBuilder buf = new StringBuilder();
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.tree.lemon\";size=\"128,128\";text=\"Lemon tree\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"The lemon, or Citrus limon, is a species of small evergreen tree in the flowering plant family Rutaceae.  It is native to Wurm, and is found in all areas of the world.  Originally used for ornamental or medicinal purposes, lemons are now produced in large-scale cultivation.\"}");
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.produce.lemon\";size=\"128,128\";text=\"Lemon\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"The tree's ellipsoidal yellow fruit is used for culinary and non-culinary purposes throughout Wurm.  The juice, pulp and rind (zest) are all used in cooking and baking, and the juice also has useful alchemical properties.  The juice of the lemon is about 5% to 6% citric acid, which gives a distinctive sour taste; this makes it a key ingredient in drinks and foods such as lemonade.\"}");
        return buf.toString();
    }

    private String showOleanderInfo() {
        StringBuilder buf = new StringBuilder();
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.bush.oleander\";size=\"128,128\";text=\"Oleander bush\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"Nerium oleander is a shrub in the dogbane family (Apocynaceae), toxic in all its parts. It is the only species currently classified in the genus Nerium. It is most commonly known as oleander, from its superficial resemblance to the unrelated olive (Olea). It is so widely cultivated that no precise region of origin has been identified, though Chaos has been suggested.\"}");
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.produce.oleander\";size=\"128,128\";text=\"Oleander leaves\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"Oleander is one of the most poisonous commonly grown garden plants.  However, an early Wurmian discovery was a method of cooking that leached the poison from the leaves, allowing them to be used in cooking.  Although much research has been carried out into producing a usable poison from oleander, these efforts have so far proved unsuccessful.\"}");
        return buf.toString();
    }

    private String showCamelliaInfo() {
        StringBuilder buf = new StringBuilder();
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.bush.camellia\";size=\"128,128\";text=\"Camellia bush\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"The camellia bush is a genus of flowering plants in the family Theaceae. They are found in all isles of wurm, from Independence to Release and the Epic isles. The genus was named by Linnaeus after Georgius Josephus Camellus, who worked in Independance and described a species of camellia (although Linnaeus did not refer to Kamel's account when discussing the genus). Camellias are famous throughout Wurm; they are known as chahua, ''tea flower'', an apt designation.\"}");
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.produce.camellia\";size=\"128,128\";text=\"Camellia leaves\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"Leaves of C. sinensis are processed to create the popular beverage, tea.  After being harvested at the most auspicious time, the leaves are either steamed or roasted, before being dried.  The flavour of the tea is largely determined by the level of oxidation of the leaves.  The prepared leaves are then steeped in hot water to produce a fortifying drink; the flavour may be further adjusted by addition of sugar or honey, or lemon juice.  There are even rumours of barbarians in far Celebration who add maple syrup to their tea, although this is commonly considered hearsay only.\"}");
        return buf.toString();
    }

    private String showLavenderInfo() {
        StringBuilder buf = new StringBuilder();
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.bush.lavender\";size=\"128,128\";text=\"Lavender bush\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"Lavandula (common name lavender) is a genus of flowering plants in the mint family, Lamiaceae. It is native to the Old World and is found in all lands of Wurm. Many members of the genus are cultivated extensively in temperate climates as ornamental plants for garden and landscape use, even as hedges, for use as culinary herbs.\"}");
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.produce.lavender\";size=\"128,128\";text=\"Lavender\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"The most widely cultivated species, Lavandula angustifolia, is often referred to as lavender, and there is a color named for the shade of the flowers of this species.  The flowers have a distinctive odour, and the bush is often cultivated into low hedges.\"}");
        return buf.toString();
    }

    private String showMapleInfo() {
        StringBuilder buf = new StringBuilder();
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.tree.maple\";size=\"128,128\";text=\"Maple tree\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"Acer is a genus of trees or shrubs commonly known as maple. The type species of the genus is the sycamore maple, Acer pseudoplatanus, the most common maple species in Wurm.  Originally from Celebration, it is known for its sweet sap and light-coloured wood.\"}");
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.produce.maple\";size=\"128,128\";text=\"Maple Sap\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"Maple syrup is usually made from the xylem sap of  maple trees. In cold climates, these trees store starch in their trunks and roots before the winter; the starch is then converted to sugar that rises in the sap in late winter and early spring. Maple trees are tapped by boring holes into their trunks and collecting the exuded sap, which is processed by heating to evaporate much of the water, leaving the concentrated syrup.\"}");
        return buf.toString();
    }

    private String showRoseInfo() {
        StringBuilder buf = new StringBuilder();
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.bush.rose\";size=\"128,128\";text=\"Rose bush\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"A rose is a woody perennial flowering plant of the genus Rosa, in the family Rosaceae, named for the flower it bears. They form a group of plants that can be erect shrubs, climbing or trailing, with stems that are often armed with sharp thorns. Flowers vary in size and shape and are usually large and showy, although only red ones have been grown so far. Species, cultivars and hybrids are all widely grown for their beauty and often are fragrant. Roses have acquired cultural significance in many societies.\"}");
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.trellis.rose\";size=\"128,128\";text=\"Rose trellis\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"Rose bushes may be trained to climb trellises, forming attractive ornamental wall coverings.  Rose trellises have a firm place in popular imagination, with a number of folk tales and plays taking place under one.\"}");
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.produce.rose\";size=\"128,128\";text=\"Rose flowers\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"The majority of ornamental roses are hybrids that were bred for their flowers. A few specialised species are grown for their attractive or scented foliage.\"}");
        return buf.toString();
    }

    private String showChestnutInfo() {
        StringBuilder buf = new StringBuilder();
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.tree.chestnut\";size=\"128,128\";text=\"Chestnut tree\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"The chestnut group is a genus (Castanea) of species of deciduous trees in the beech family Fagaceae, native to temperate regions of Wurm.  Often growing in tightly-packed groves, they produce a sweet nut late in the year.  Older chestnut trees can grow to prodigious thickness.\"}");
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.produce.chestnut\";size=\"128,128\";text=\"Chestnut\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"Chestnut trees produce a sweet nut, also called chestnuts.  The name comes from the spiny husk covering the nuts; opening up this 'chest' reveals the sweet treasure inside.  Younger Wurmians often celebrate winter by throwing the prickly nuts at each other.\"}");
        return buf.toString();
    }

    private String showWalnutInfo() {
        StringBuilder buf = new StringBuilder();
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.tree.walnut\";size=\"128,128\";text=\"Walnut tree\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"A walnut is any tree of the genus Juglans (Family Juglandaceae).  Found throughout the lands of Wurm, it forms a mid-height tree with distinctive dark green leaves.  The wood of the walnut tree is particularly dense, and thus of interest to coal makers.\"}");
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.produce.walnut\";size=\"128,128\";text=\"Walnut\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"Technically a walnut is the seed of a drupe or drupaceous nut, and thus not a true botanical nut. It is used for food after being processed while green for pickled walnuts or after full ripening for its nutmeat. The walnut is nutrient-dense with protein and essential fatty acids.\"}");
        return buf.toString();
    }

    private String showPineInfo() {
        StringBuilder buf = new StringBuilder();
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.tree.pine\";size=\"128,128\";text=\"Pine tree\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"A pine is a conifer in the genus Pinus, of the family Pinaceae. Pinus is the sole genus in the subfamily Pinoideae.  Found everywhere, the wood is resinous and fast-growing, and is often used for crafting or firewood.  Pine trees have a special cultural significance, as they are used to signal present drop-off sites to Santa around Christmas.\"}");
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.produce.pine\";size=\"128,128\";text=\"Pinenuts\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"Pine trees produce a hard, woody cone, which contains the edible seeds.  To retrieve the pine nuts, the cones must be opened - this is often done over a low fire or in an oven.  Although some people extol the virtues of pine cones over chestnuts for projectile use, most denizens of Wurm completely ignore the existence of pine cones, preferring to focus on the nuts instead.  Pine nuts are widely used in cooking.\"}");
        return buf.toString();
    }

    private String showHazelInfo() {
        StringBuilder buf = new StringBuilder();
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.bush.hazel\";size=\"128,128\";text=\"Hazel bush\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"Hazels have simple, rounded leaves with double-serrate margins. The flowers are produced very early in spring before the leaves, and are monoecious, with single-sex catkins: the male catkins are pale yellow and 5-12 cm long, and the female ones are very small and largely concealed in the buds, with only the bright-red, 1-to-3 mm-long styles visible. \"}");
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.produce.hazel\";size=\"128,128\";text=\"Hazelnut\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"The fruits are nuts 1-2.5 cm long and 1-2 cm diameter, surrounded by a husk which partly to fully encloses the nut.  Hazelnuts are sweet, and often roasted before being used in a variety of dishes.\"}");
        return buf.toString();
    }

    private String showHopsInfo() {
        StringBuilder buf = new StringBuilder();
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.trellis.hops\";size=\"128,128\";text=\"Hops trellis\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"The hop plant is a vigorous, climbing, herbaceous perennial.  Although wild hop seedlings may be found, the plants are usually cultivated by training them up trellises.  Having a distinctive odour, hops are a relatively modern cultivated plant, having massively grown in popularity after the discovery of brewing.  Hops come in a wide variety of cultivars, giving many different aromas and flavours.\"}");
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.produce.hops\";size=\"128,128\";text=\"Hops\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"Hops are the flowers (also called seed cones or strobiles) of the hop plant Humulus lupulus. They are used primarily as a flavoring and stability agent in beer, to which they impart bitter, zesty, or citric flavours; though they are also used for various purposes in other beverages and herbal medicine. \"}");
        return buf.toString();
    }

    private String showOakInfo() {
        StringBuilder buf = new StringBuilder();
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.tree.oak\";size=\"128,128\";text=\"Oak tree\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"The mighty oak is a tree in the genus Quercus of the beech family, Fagaceae.  Oak trees are one of the slowest-growing plants known to Wurmkind, producing an exceptionally tough wood; this is much prized by tool-makers, as being more durable than other timber.\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"Oaks have spirally arranged leaves, with lobate margins in many species; some have serrated leaves or entire leaf with smooth margins. Also, the acorns contain tannic acid, as do the leaves, which helps to guard from fungi and insects. Many deciduous species are marcescent, not dropping dead leaves until spring. In spring, a single oak tree produces both male flowers (in the form of catkins) and small female flowers.  The high acidity of the tree often kills other plants in the vicinity.\"}");
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.produce.oak\";size=\"128,128\";text=\"Acorn\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"The fruit is a nut called an acorn, borne in a cup-like structure known as a cupule; each acorn contains one seed (rarely two or three). The acorns contain tannic acid, which may be extracted by skilled alchemists for use in dye production.  Acorns may be found on the ground at any time, but the best quality ones are harvested directly from the trees when ripe.\"}");
        return buf.toString();
    }

    private String showOrangeInfo() {
        StringBuilder buf = new StringBuilder();
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.tree.orange\";size=\"128,128\";text=\"Orange tree\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"The orange tree is of the citrus species Citrus sinensis in the family Rutaceae.  The first oranges were bred as a hybrid variety of lemon on Pristine, but due to general benevolence coupled with poor biosecurity practices, sprouts can now be found everywhere in Wurm.\"}");
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.produce.orange\";size=\"128,128\";text=\"Orange\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"The orange fruit, or sweet orange, also gives its name to the colour.  A segmented fruit, with a thick rind, the flesh and juice are sweet and high in citric acid.  During inter-kingdom battles, the opposing sides sometimes take a break around half way through the fight to eat fresh slices of orange.\"}");
        return buf.toString();
    }

    private String showRaspberryInfo() {
        StringBuilder buf = new StringBuilder();
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.bush.raspberry\";size=\"128,128\";text=\"Raspberry bush\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"The raspberry bush is a perennial with woody stems, belonging to the Rubus genus.  A relation of the rose, it tends to form thickets of canes.  The stems have small thorns, as do the backs of the leaves.  Raspberry canes spread by basel shoots, or suckering, and prefer a well-drained soil.\"}");
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.produce.raspberry\";size=\"128,128\";text=\"Raspberry\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"The fruit of the raspberry bush, also referred to as raspberries, consists of a multitude of small globules, attached to a central husk.  The fruit forms during the summer, coming to full ripeness late in the year; the berries are most commonly a light red, but may also be purple, black, or rarely pale yellow or white.  Prized for their sweetness and flavour, raspberries are best eaten fresh, but may also be cooked into many dishes, both savoury and sweet.\"}");
        return buf.toString();
    }

    private String showBlueberryInfo() {
        StringBuilder buf = new StringBuilder();
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.bush.blueberry\";size=\"128,128\";text=\"Blueberry busg\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"Growing in low shrubs, members of the Cyanococcus bush produce bell-shaped flowers and dark blue berries.  Originally cultivated on Independance, they have been carried to other lands.  Some other families of plants are also commonly referred to as blueberries, mainly derived from the myrtles.  The bush is frost-hardy, and will grow in most soils, although it prefers well-drained, slightly alkaline conditions.\"}");
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.produce.blueberry\";size=\"128,128\";text=\"Blueberry\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"Blueberries start out pale green, but darken as they ripen, ending up a very dark purple.  The berries are small, and grow in clusters, having a natural protective wax coating.  Typically harvested in mid-summer, blueberries are known for staining tongues blue all over Wurm.\"}");
        return buf.toString();
    }

    private String showLingonberryInfo() {
        StringBuilder buf = new StringBuilder();
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.bush.lingonberry\";size=\"128,128\";text=\"Lingonberry bush\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"Vaccinium vitis-idaea (lingonberry, partridgeberry, or cowberry) is a short evergreen shrub in the heath family that bears edible fruit, native to tundra throughout Wurm. Lingonberries are picked in the wild and used to accompany a variety of dishes.\"}");
        buf.append("text{text=\"\"}");
        buf.append("image{src=\"img.almanac.produce.lingonberry\";size=\"128,128\";text=\"Lingonberry\"}");
        buf.append("text{text=\"\"}");
        buf.append("text{text=\"The berries are quite tart, so they are often cooked and sweetened before eating in the form of lingonberry jam or juice.  The berries are also popular as a wild picked fruit in Release, where they are locally known as partridgeberries or redberries, and on the mainland of Celebration, where they are known as foxberries. In this region they are incorporated into jams, syrups, and baked goods, such as pies, scones, and muffins.\"}");
        return buf.toString();
    }
}

