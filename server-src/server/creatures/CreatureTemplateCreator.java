/*
 * Decompiled with CFR 0.152.
 */
package com.wurmonline.server.creatures;

import com.wurmonline.server.Features;
import com.wurmonline.server.Server;
import com.wurmonline.server.Servers;
import com.wurmonline.server.combat.ArmourTemplate;
import com.wurmonline.server.creatures.AttackAction;
import com.wurmonline.server.creatures.AttackIdentifier;
import com.wurmonline.server.creatures.AttackValues;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.CreatureTemplateIds;
import com.wurmonline.server.creatures.ai.scripts.FishAI;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.skills.SkillsFactory;
import com.wurmonline.shared.constants.CreatureTypes;
import com.wurmonline.shared.constants.ItemMaterials;
import com.wurmonline.shared.constants.SoundNames;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CreatureTemplateCreator
implements CreatureTypes,
CreatureTemplateIds,
SoundNames,
ItemMaterials {
    private static final Logger logger = Logger.getLogger(CreatureTemplateCreator.class.getName());

    private CreatureTemplateCreator() {
    }

    public static final int getDragonLoot(int type) {
        switch (type) {
            case 89: {
                return 979;
            }
            case 91: {
                return 980;
            }
            case 16: {
                return 977;
            }
            case 90: {
                return 986;
            }
            case 92: {
                return 975;
            }
            case 18: {
                return 976;
            }
            case 104: {
                return 978;
            }
            case 17: {
                return 973;
            }
            case 103: {
                return 974;
            }
            case 19: {
                return 975;
            }
        }
        return 0;
    }

    public static final int getRandomDrakeId() {
        int rng = Server.rand.nextInt(5);
        switch (rng) {
            case 0: {
                return 103;
            }
            case 1: {
                return 17;
            }
            case 2: {
                return 104;
            }
            case 3: {
                return 19;
            }
            case 4: {
                return 18;
            }
        }
        return 104;
    }

    public static final int getRandomDragonOrDrakeId() {
        int rng = Server.rand.nextInt(10);
        switch (rng) {
            case 0: {
                return 91;
            }
            case 1: {
                return 104;
            }
            case 2: {
                return 16;
            }
            case 3: {
                return 103;
            }
            case 4: {
                return 89;
            }
            case 5: {
                return 18;
            }
            case 6: {
                return 90;
            }
            case 7: {
                return 17;
            }
            case 8: {
                return 92;
            }
            case 9: {
                return 19;
            }
        }
        return 103;
    }

    public static void createCreatureTemplates() {
        logger.info("Starting to create Creature Templates");
        long start = System.currentTimeMillis();
        CreatureTemplateCreator.createCreatureTemplate(1, "Human", "Humans", "Another explorer.");
        CreatureTemplateCreator.createCreatureTemplate(9, "Salesman", "Salesman", "An envoy from the king, buying and selling items.");
        CreatureTemplateCreator.createCreatureTemplate(3, "Brown cow", "Brown cows", "A brown docile cow.");
        CreatureTemplateCreator.createCreatureTemplate(7, "guardTough", "guardToughs", "This warrior would pose problems for any intruder.");
        CreatureTemplateCreator.createCreatureTemplate(8, "guardBrutal", "guardBrutals", "Not many people would like to cross this warrior.");
        CreatureTemplateCreator.createCreatureTemplate(10, "Black wolf", "Black wolves", "This dark shadow of the forests glares hungrily at you.");
        CreatureTemplateCreator.createCreatureTemplate(11, "Troll", "Trolls", "A dark green stinking troll. Always hungry. Always deadly.");
        CreatureTemplateCreator.createCreatureTemplate(12, "Brown bear", "Brown bears", "The brown bear has a distinctive hump on the shoulders, and long deadly claws on the front paws.");
        CreatureTemplateCreator.createCreatureTemplate(42, "Black bear", "Black bears", "The black bear looks pretty kind, but has strong, highly curved claws ready to render you to pieces.");
        CreatureTemplateCreator.createCreatureTemplate(13, "Large rat", "Large rats", "This is an unnaturally large version of a standard black rat.");
        CreatureTemplateCreator.createCreatureTemplate(43, "Cave bug", "Cave bugs", "Some kind of unnaturally large and deformed insect lunges at you from the dark. It has a grey carapace, with small patches of lichen growing here and there.");
        CreatureTemplateCreator.createCreatureTemplate(14, "Mountain lion", "Mountain lions", "Looking like a huge cat, it is tawny-coloured, with a small head and small, rounded, black-tipped ears.");
        CreatureTemplateCreator.createCreatureTemplate(15, "Wild cat", "Wild cats", "A small wild cat, fierce and aggressive.");
        CreatureTemplateCreator.createCreatureTemplate(2, "Joe the Stupe", "Joe the Stupes", "A hollow-eyed person is standing here, potentially dangerous but stupid as ever.");
        CreatureTemplateCreator.createCreatureTemplate(16, "Red dragon", "Red dragons", "The menacing huge dragon, with scales in every possible red color.");
        CreatureTemplateCreator.createCreatureTemplate(17, "Green dragon hatchling", "Green dragon hatchlings", "The green dragon hatchling is not as large as a full-grown dragon and unable to fly.");
        CreatureTemplateCreator.createCreatureTemplate(18, "Black dragon hatchling", "Black dragon hatchlings", "The black dragon hatchling is not as large as a full-grown dragon and unable to fly.");
        CreatureTemplateCreator.createCreatureTemplate(19, "White dragon hatchling", "White dragon hatchlings", "The white dragon hatchling is not as large as a full-grown dragon and unable to fly.");
        CreatureTemplateCreator.createCreatureTemplate(20, "Forest giant", "Forest giants", "With an almost sad look upon its face, this filthy giant might be mistaken for a harmless huge baby.");
        CreatureTemplateCreator.createCreatureTemplate(21, "Unicorn", "Unicorns", "A bright white unicorn with a slender twisted horn.");
        CreatureTemplateCreator.createCreatureTemplate(118, "Unicorn Foal", "Unicorn Foals", "A small bright white unicorn foal with a budding horn.");
        CreatureTemplateCreator.createCreatureTemplate(22, "Kyklops", "Kyklops", "This large drooling one-eyed giant is obviously too stupid to feel any mercy.");
        CreatureTemplateCreator.createCreatureTemplate(23, "Goblin", "Goblins", "This small, dirty creature looks at you greedily, and would go into a frenzy if you show pain.");
        CreatureTemplateCreator.createCreatureTemplate(25, "Huge spider", "Huge spiders", "Monstrously huge and fast, these spiders love to be played with.");
        CreatureTemplateCreator.createCreatureTemplate(56, "Lava spider", "Lava spiders", "Lava spiders usually lurk in their lava pools, catching curious prey.");
        CreatureTemplateCreator.createCreatureTemplate(26, "Goblin leader", "Goblin leaders", "Always on the brink of cackling wildly, this creature is possibly insane.");
        CreatureTemplateCreator.createCreatureTemplate(27, "Troll king", "Troll kings", "This troll has a scary clever look in his eyes. He surely knows what he is doing.");
        CreatureTemplateCreator.createCreatureTemplate(28, "Spirit guard", "Spirit guards", "This fierce spirit vaguely resembles a human warrior, and for some reason guards here.");
        CreatureTemplateCreator.createCreatureTemplate(29, "Spirit sentry", "Spirit sentries", "This spirit vaguely resembles a human being, and for some reason guards here.");
        CreatureTemplateCreator.createCreatureTemplate(30, "Spirit avenger", "Spirit avengers", "This restless spirit vaguely resembles a human being, that for some reason has chosen to guard this place.");
        CreatureTemplateCreator.createCreatureTemplate(31, "Spirit brute", "Spirit brutes", "This fierce spirit seems restless and upset but for some reason has chosen to guard this place.");
        CreatureTemplateCreator.createCreatureTemplate(32, "Spirit templar", "Spirit templars", "The spirit of a proud knight has decided to protect this place.");
        CreatureTemplateCreator.createCreatureTemplate(33, "Spirit shadow", "Spirit shadows", "A dark humanoid shadow looms about, its intentions unclear.");
        CreatureTemplateCreator.createCreatureTemplate(34, "Jenn-Kellon tower guard", "Jenn-Kellon tower guards", "This person seems to be able to put up some resistance. These guards will help defend you if you say help.");
        CreatureTemplateCreator.createCreatureTemplate(35, "Horde of the Summoned tower guard", "Horde of the Summoned tower guards", "This person seems to be able to put up some resistance. These guards will help defend you if you say help.");
        CreatureTemplateCreator.createCreatureTemplate(36, "Mol-Rehan tower guard", "Mol-Rehan tower guards", "This person seems to be able to put up some resistance. These guards will help defend you if you say help.");
        CreatureTemplateCreator.createCreatureTemplate(67, "Isles tower guard", "Isles tower guards", "This person seems to be able to put up some resistance. These guards will help defend you if you say help.");
        CreatureTemplateCreator.createCreatureTemplate(41, "Bartender", "Bartenders", "A fat and jolly bartender, eager to help people settling in.");
        CreatureTemplateCreator.createCreatureTemplate(46, "Santa Claus", "Santa Clauses", "Santa Claus is standing here, with a jolly face behind his huge white beard.");
        CreatureTemplateCreator.createCreatureTemplate(47, "Evil Santa", "Evil Santas", "Some sort of Santa Claus is standing here, with a fat belly, yellow eyes, and a bad breath.");
        CreatureTemplateCreator.createCreatureTemplate(37, "Wild boar", "Wild boars", "A large and strong boar is grunting here.");
        CreatureTemplateCreator.createCreatureTemplate(39, "Mountain gorilla", "Mountain gorillas", "This normally calm mountain gorilla may suddenly become a very fierce and dangerous foe if annoyed.");
        CreatureTemplateCreator.createCreatureTemplate(38, "Anaconda", "Anacondas", "An over 3 meters long muscle, this grey-green snake is formidable.");
        CreatureTemplateCreator.createCreatureTemplate(40, "Rabid hyena", "Rabid hyenas", "Normally this doglike creature would act very cowardly, but some sickness seems to have driven it mad and overly aggressive.");
        CreatureTemplateCreator.createCreatureTemplate(44, "Pig", "Pigs", "A pig is here, wallowing in the mud.");
        CreatureTemplateCreator.createCreatureTemplate(45, "Hen", "Hens", "A fine hen proudly prods around here.");
        CreatureTemplateCreator.createCreatureTemplate(52, "Rooster", "Roosters", "A proud rooster struts around here.");
        CreatureTemplateCreator.createCreatureTemplate(48, "Chicken", "Chickens", "A cute chicken struts around here.");
        CreatureTemplateCreator.createCreatureTemplate(51, "Dog", "Dogs", "Occasionally this dog will bark and scratch itself behind the ears.");
        CreatureTemplateCreator.createCreatureTemplate(50, "Calf", "Calves", "This calf looks happy and free.");
        CreatureTemplateCreator.createCreatureTemplate(49, "Bull", "Bulls", "This bull looks pretty menacing.");
        CreatureTemplateCreator.createCreatureTemplate(82, "Bison", "Bison", "The bison are impressive creatures when moving in hordes.");
        CreatureTemplateCreator.createCreatureTemplate(64, "Horse", "Horses", "Horses like this one have many uses.");
        CreatureTemplateCreator.createCreatureTemplate(65, "Foal", "Foals", "A foal skips around here merrily.");
        CreatureTemplateCreator.createCreatureTemplate(53, "Easter bunny", "Easter bunnies", "Wow, the mystical easter bunny skips around here joyfully!");
        CreatureTemplateCreator.createCreatureTemplate(54, "Deer", "Deer", "A fallow deer is here, watching for enemies.");
        CreatureTemplateCreator.createCreatureTemplate(55, "Pheasant", "Pheasants", "The pheasant slowly paces here, vigilant as always.");
        CreatureTemplateCreator.createCreatureTemplate(57, "Lava fiend", "Lava fiends", "These lava creatures enter the surface through lava pools, probably in order to hunt. Or burn.");
        CreatureTemplateCreator.createCreatureTemplate(58, "Crocodile", "Crocodiles", "This meat-eating reptile swims very well but may also perform quick rushes on land in order to catch you.");
        CreatureTemplateCreator.createCreatureTemplate(59, "Scorpion", "Scorpions", "The monstruously large type of scorpion found in woods and caves here is fairly aggressive.");
        CreatureTemplateCreator.createCreatureTemplate(60, "Tormentor", "Tormentors", "A particularly grim person stands here, trying to sort things out.");
        CreatureTemplateCreator.createCreatureTemplate(61, "Guide", "Guides", "A rather stressed out person is here giving instructions on how to survive to everyone who just arrived.");
        CreatureTemplateCreator.createCreatureTemplate(62, "Lady of the lake", "Ladies of the lake", "The hazy shape of a female spirit lingers below the waves.");
        CreatureTemplateCreator.createCreatureTemplate(63, "Cobra King", "Cobra Kings", "A huge menacing king cobra is guarding here, head swaying back and forth.");
        CreatureTemplateCreator.createCreatureTemplate(66, "Child", "Children", "A small child is here, exploring the world.");
        CreatureTemplateCreator.createCreatureTemplate(68, "Avenger of the Light", "Avengers of the Light", "Some kind of giant lumbers here, hunting humans.");
        CreatureTemplateCreator.createCreatureTemplate(69, "Zombie", "Zombies", "A very bleak humanlike creature stands here, looking abscent-minded.");
        CreatureTemplateCreator.createCreatureTemplate(70, "Sea Serpent", "Sea Serpents", "Sea Serpents are said to sleep in the dark caves of the abyss for years, then head to the surface to hunt once they get hungry.");
        CreatureTemplateCreator.createCreatureTemplate(71, "Huge shark", "Huge sharks", "These huge sharks were apparently not just a rumour. How horrendous!");
        CreatureTemplateCreator.createCreatureTemplate(72, "Sol Demon", "Sol Demons", "This demon has been released from Sol.");
        CreatureTemplateCreator.createCreatureTemplate(73, "Deathcrawler minion", "Deathcrawler minions", "The Deathcrawler minions usually spawn in large numbers. They have deadly poisonous bites.");
        CreatureTemplateCreator.createCreatureTemplate(74, "Spawn of Uttacha", "Spawns of Uttacha", "Uttacha is a vengeful demigod who lives in the depths of an ocean on Valrei. These huge larvae are hungry and confused abominations here.");
        CreatureTemplateCreator.createCreatureTemplate(75, "Son of Nogump", "Sons of Nogump", "Nogump the dirty has given birth to this foul two-headed giant wielding a huge twohanded sword.");
        CreatureTemplateCreator.createCreatureTemplate(76, "Drakespirit", "Drakespirits", "Drakespirits are usually found in their gardens on Valrei. They are hungry and aggressive.");
        CreatureTemplateCreator.createCreatureTemplate(77, "Eaglespirit", "Eaglespirits", "The Eaglespirits live on a glacier on Valrei. They will attack if hungry or threatened.");
        CreatureTemplateCreator.createCreatureTemplate(78, "Epiphany of Vynora", "Epiphanies of Vynora", "This female creature is almost see-through, and you wonder if she is made of water or thoughts alone.");
        CreatureTemplateCreator.createCreatureTemplate(79, "Juggernaut of Magranon", "Juggernauts of Magranon", "A ferocious beast indeed, the juggernaut can crush mountains with its horned forehead.");
        CreatureTemplateCreator.createCreatureTemplate(80, "Manifestation of Fo", "Manifestations of Fo", "Something seems to have gone wrong as Fo tried to create his manifestation. The thorns are not loving at all and it seems very aggressive.");
        CreatureTemplateCreator.createCreatureTemplate(81, "Incarnation of Libila", "Incarnations of Libila", "This terrifying female apparition has something disturbing over it. As if it's just one facet of Libila.");
        CreatureTemplateCreator.createCreatureTemplate(83, "Hell Horse", "Hell Horses", "This fiery creature is rumoured to be the mounts of the demons of Sol.");
        CreatureTemplateCreator.createCreatureTemplate(117, "Hell Foal", "Hell Foals", "This fiery creature is rumoured to grow up to be a mount of the demons of Sol.");
        CreatureTemplateCreator.createCreatureTemplate(84, "Hell Hound", "Hell Hounds", "The hell hound is said to be spies and assassins for the demons of Sol.");
        CreatureTemplateCreator.createCreatureTemplate(85, "Hell Scorpious", "Hell Scorpii", "The pets of the demons of Sol are very playful.");
        CreatureTemplateCreator.createCreatureTemplate(86, "Worg", "Worgs", "This wolf-like creature is unnaturally big and clumsy. The Worg seems finicky and nervous, which makes it unpredictable and dangerous to deal with.");
        CreatureTemplateCreator.createCreatureTemplate(87, "Skeleton", "Skeletons", "This abomination has been animated by powerful magic.");
        CreatureTemplateCreator.createCreatureTemplate(88, "Wraith", "Wraiths", "The wraith is born of darkness and shuns the daylight.");
        CreatureTemplateCreator.createCreatureTemplate(93, "Seal", "Seals", "These creatures love to bathe in the sun and go for a swim hunting fish.");
        CreatureTemplateCreator.createCreatureTemplate(94, "Tortoise", "Tortoises", "The tortoise is pretty harmless but can pinch you quite bad with its bite.");
        CreatureTemplateCreator.createCreatureTemplate(95, "Crab", "Crabs", "Crabs are known to hide well and walk sideways.");
        CreatureTemplateCreator.createCreatureTemplate(96, "Sheep", "Sheep", "A mythical beast of legends, it stares back at you with blood filled eyes and froth around the mouth.");
        CreatureTemplateCreator.createCreatureTemplate(97, "Blue whale", "Blue whales", "These gigantic creatures travel huge distances looking for food, while singing their mysterious songs.");
        CreatureTemplateCreator.createCreatureTemplate(98, "Seal cub", "Seal cubs", "A young seal, waiting to be fed luscious fish.");
        CreatureTemplateCreator.createCreatureTemplate(99, "Dolphin", "Dolphins", "A playful dolphin. They have been known to defend sailors in distress from their natural enemy, the shark.");
        CreatureTemplateCreator.createCreatureTemplate(100, "Octopus", "Octopi", "Larger specimen have been known to pull whole ships down into the abyss. Luckily this one is small.");
        CreatureTemplateCreator.createCreatureTemplate(101, "Lamb", "Lambs", "A small cuddly ball of fluff.");
        CreatureTemplateCreator.createCreatureTemplate(102, "Ram", "Rams", "A mythical beast of legends, it stares back at you with blood filled eyes and froth around the mouth.");
        CreatureTemplateCreator.createCreatureTemplate(89, "Black dragon", "Black dragons", "The menacing huge dragon, with scales as dark as the night.");
        CreatureTemplateCreator.createCreatureTemplate(91, "Blue dragon", "Blue dragons", "The menacing huge dragon, with dark blue scales.");
        CreatureTemplateCreator.createCreatureTemplate(90, "Green dragon", "Green dragons", "The menacing huge dragon, with emerald green scales.");
        CreatureTemplateCreator.createCreatureTemplate(92, "White dragon", "White dragons", "The menacing huge dragon, with snow white scales.");
        CreatureTemplateCreator.createCreatureTemplate(104, "Blue dragon hatchling", "Blue dragon hatchlings", "The blue dragon hatchling is not as large as a full-grown dragon and unable to fly.");
        CreatureTemplateCreator.createCreatureTemplate(103, "Red dragon hatchling", "Red dragon hatchlings", "The red dragon hatchling is not as large as a full-grown dragon and unable to fly.");
        CreatureTemplateCreator.createCreatureTemplate(105, "Fog Spider", "Fog Spiders", "Usually only encountered under foggy conditions, this creature is often considered an Omen.");
        CreatureTemplateCreator.createCreatureTemplate(106, "Rift Beast", "Rift Beasts", "These vile creatures emerge from the rift in great numbers.");
        CreatureTemplateCreator.createCreatureTemplate(107, "Rift Jackal", "Rift Jackals", "The Jackals accompany the Beasts as they spew out of the rift.");
        CreatureTemplateCreator.createCreatureTemplate(108, "Rift Ogre", "Rift Ogres", "The Rift Ogres seem to bully Beasts and Jackals into following orders.");
        CreatureTemplateCreator.createCreatureTemplate(109, "Rift Warmaster", "Rift Warmasters", "These plan and lead attacks from the rift.");
        CreatureTemplateCreator.createCreatureTemplate(111, "Rift Ogre Mage", "Rift Ogre Magi", "Ogre Mages have mysterious powers.");
        CreatureTemplateCreator.createCreatureTemplate(110, "Rift Caster", "Rift Casters", "Proficient spell casters, but they seem to avoid direct contact.");
        CreatureTemplateCreator.createCreatureTemplate(112, "Rift Summoner", "Rift Summoners", "Summoners seem to be able to call for aid from the Rift.");
        CreatureTemplateCreator.createCreatureTemplate(113, "NPC Human", "NPC Humans", "A relatively normal person stands here waiting for something to happen.");
        if (Features.Feature.WAGONER.isEnabled()) {
            CreatureTemplateCreator.createCreatureTemplate(114, "NPC Wagoner", "NPC Wagoners", "A relatively normal person stands here waiting to help transport bulk goods.");
            CreatureTemplateCreator.createCreatureTemplate(115, "Wagon Creature", "Wagon Creatures", "The wagon creature is only used for hauling a wagoner's wagon.");
        }
        if (Servers.localServer.testServer) {
            CreatureTemplateCreator.createCreatureTemplate(116, "Weapon Test Dummy", "Weapon Test Dummies", "An immortal that shouts out any damage that it receives, then immediately heals.");
        }
        CreatureTemplateCreator.createCreatureTemplate(119, "Fish", "fishs", "a fish of some type or other.");
        long end = System.currentTimeMillis();
        logger.info("Creating Creature Templates took " + (end - start) + " ms");
    }

    private static void createCreatureTemplate(int id, String name, String plural, String longDesc) {
        Skills skills = SkillsFactory.createSkills(name);
        try {
            skills.learnTemp(102, 20.0f);
            skills.learnTemp(104, 20.0f);
            skills.learnTemp(103, 20.0f);
            skills.learnTemp(100, 20.0f);
            skills.learnTemp(101, 20.0f);
            skills.learnTemp(105, 20.0f);
            skills.learnTemp(106, 20.0f);
            if (id == 1) {
                CreatureTemplateCreator.createHumanTemplate(id, name, plural, longDesc, skills);
            } else if (id == 66) {
                CreatureTemplateCreator.createKidTemplate(id, name, plural, longDesc, skills);
            } else if (id == 3) {
                CreatureTemplateCreator.createBrownCowTemplate(id, name, plural, longDesc, skills);
            } else if (id == 50) {
                CreatureTemplateCreator.createCalfTemplate(id, name, plural, longDesc, skills);
            } else if (id == 49) {
                CreatureTemplateCreator.createBullTemplate(id, name, plural, longDesc, skills);
            } else if (id == 82) {
                CreatureTemplateCreator.createBisonTemplate(id, name, plural, longDesc, skills);
            } else if (id == 64) {
                CreatureTemplateCreator.createHorseTemplate(id, name, plural, longDesc, skills);
            } else if (id == 65) {
                CreatureTemplateCreator.createFoalTemplate(id, name, plural, longDesc, skills);
            } else if (id == 54) {
                CreatureTemplateCreator.createDeerTemplate(id, name, plural, longDesc, skills);
            } else if (id == 52) {
                CreatureTemplateCreator.createRoosterTemplate(id, name, plural, longDesc, skills);
            } else if (id == 55) {
                CreatureTemplateCreator.createPheasantTemplate(id, name, plural, longDesc, skills);
            } else if (id == 45) {
                CreatureTemplateCreator.createHenTemplate(id, name, plural, longDesc, skills);
            } else if (id == 48) {
                CreatureTemplateCreator.createChickenTemplate(id, name, plural, longDesc, skills);
            } else if (id == 9) {
                CreatureTemplateCreator.createSalesmanTemplate(id, name, plural, longDesc, skills);
            } else if (id == 41) {
                CreatureTemplateCreator.createBartenderTemplate(id, name, plural, longDesc, skills);
            } else if (id == 46) {
                CreatureTemplateCreator.createSantaClausTemplate(id, name, plural, longDesc, skills);
            } else if (id == 47) {
                CreatureTemplateCreator.createEvilSantaTemplate(id, name, plural, longDesc, skills);
            } else if (id == 61) {
                CreatureTemplateCreator.createGuideTemplate(id, name, plural, longDesc, skills);
            } else if (id == 60) {
                CreatureTemplateCreator.createGuideHotsTemplate(id, name, plural, longDesc, skills);
            } else if (id == 4) {
                CreatureTemplateCreator.createGuardLenientTemplate(id, name, plural, longDesc, skills);
            } else if (id == 5) {
                CreatureTemplateCreator.createGuardDecentTemplate(id, name, plural, longDesc, skills);
            } else if (id == 6) {
                CreatureTemplateCreator.createGuardAbleTemplate(id, name, plural, longDesc, skills);
            } else if (id == 7) {
                CreatureTemplateCreator.createGuardToughTemplate(id, name, plural, longDesc, skills);
            } else if (id == 8) {
                CreatureTemplateCreator.createGuardBrutalTemplate(id, name, plural, longDesc, skills);
            } else if (id == 32) {
                CreatureTemplateCreator.createGuardSpiritGoodDangerousTemplate(id, name, plural, longDesc, skills);
            } else if (id == 33) {
                CreatureTemplateCreator.createGuardSpiritEvilDangerousTemplate(id, name, plural, longDesc, skills);
            } else if (id == 30 || id == 31) {
                CreatureTemplateCreator.createGuardSpiritAbleTemplate(id, name, plural, longDesc, skills);
            } else if (id == 29) {
                CreatureTemplateCreator.createGuardSpiritEvilLenientTemplate(id, name, plural, longDesc, skills);
            } else if (id == 28) {
                CreatureTemplateCreator.createGuardSpiritGoodLenientTemplate(id, name, plural, longDesc, skills);
            } else if (id == 10) {
                CreatureTemplateCreator.createBlackWolfTemplate(id, name, plural, longDesc, skills);
            } else if (id == 51) {
                CreatureTemplateCreator.createDogTemplate(id, name, plural, longDesc, skills);
            } else if (id == 58) {
                CreatureTemplateCreator.createCrocodileTemplate(id, name, plural, longDesc, skills);
            } else if (id == 53) {
                CreatureTemplateCreator.createEasterBunnyTemplate(id, name, plural, longDesc, skills);
            } else if (id == 12) {
                CreatureTemplateCreator.createBearBrownTemplate(id, name, plural, longDesc, skills);
            } else if (id == 42) {
                CreatureTemplateCreator.createBearBlackTemplate(id, name, plural, longDesc, skills);
            } else if (id == 21) {
                CreatureTemplateCreator.createUnicornTemplate(id, name, plural, longDesc, skills);
            } else if (id == 118) {
                CreatureTemplateCreator.createUnicornFoalTemplate(id, name, plural, longDesc, skills);
            } else if (id == 59) {
                CreatureTemplateCreator.createScorpionTemplate(id, name, plural, longDesc, skills);
            } else if (id == 23) {
                CreatureTemplateCreator.createGoblinTemplate(id, name, plural, longDesc, skills);
            } else if (id == 26) {
                CreatureTemplateCreator.createGoblinLeaderTemplate(id, name, plural, longDesc, skills);
            } else if (id == 11) {
                CreatureTemplateCreator.createTrollTemplate(id, name, plural, longDesc, skills);
            } else if (id == 27) {
                CreatureTemplateCreator.createTrollKingTemplate(id, name, plural, longDesc, skills);
            } else if (id == 22) {
                CreatureTemplateCreator.createCyclopsTemplate(id, name, plural, longDesc, skills);
            } else if (id == 20) {
                CreatureTemplateCreator.createForestGiantTemplate(id, name, plural, longDesc, skills);
            } else if (id == 18) {
                CreatureTemplateCreator.createDrakeBlackTemplate(id, name, plural, longDesc, skills);
            } else if (id == 17) {
                CreatureTemplateCreator.createDrakeGreenTemplate(id, name, plural, longDesc, skills);
            } else if (id == 19) {
                CreatureTemplateCreator.createDrakeWhiteTemplate(id, name, plural, longDesc, skills);
            } else if (id == 103) {
                CreatureTemplateCreator.createDrakeRedTemplate(id, name, plural, longDesc, skills);
            } else if (id == 104) {
                CreatureTemplateCreator.createDrakeBlueTemplate(id, name, plural, longDesc, skills);
            } else if (id == 16) {
                CreatureTemplateCreator.createDragonRedTemplate(id, name, plural, longDesc, skills);
            } else if (id == 89) {
                CreatureTemplateCreator.createDragonBlackTemplate(id, name, plural, longDesc, skills);
            } else if (id == 91) {
                CreatureTemplateCreator.createDragonBlueTemplate(id, name, plural, longDesc, skills);
            } else if (id == 90) {
                CreatureTemplateCreator.createDragonGreenTemplate(id, name, plural, longDesc, skills);
            } else if (id == 92) {
                CreatureTemplateCreator.createDragonWhiteTemplate(id, name, plural, longDesc, skills);
            } else if (id == 13) {
                CreatureTemplateCreator.createRatLargeTemplate(id, name, plural, longDesc, skills);
            } else if (id == 14) {
                CreatureTemplateCreator.createLionMountainTemplate(id, name, plural, longDesc, skills);
            } else if (id == 43) {
                CreatureTemplateCreator.createCaveBugTemplate(id, name, plural, longDesc, skills);
            } else if (id == 15) {
                CreatureTemplateCreator.createCatWildTemplate(id, name, plural, longDesc, skills);
            } else if (id == 2) {
                CreatureTemplateCreator.createDummyDollTemplate(id, name, plural, longDesc, skills);
            } else if (id == 34 || id == 35 || id == 36 || id == 67) {
                CreatureTemplateCreator.createGuardKingdomTowerTemplate(id, name, plural, longDesc, skills);
            } else if (id == 39) {
                CreatureTemplateCreator.createGorillaMagranonTemplate(id, name, plural, longDesc, skills);
            } else if (id == 37) {
                CreatureTemplateCreator.createBoarFoTemplate(id, name, plural, longDesc, skills);
            } else if (id == 68) {
                CreatureTemplateCreator.createAvengerOfLightTemplate(id, name, plural, longDesc, skills);
            } else if (id == 70) {
                CreatureTemplateCreator.createSeaSerpentTemplate(id, name, plural, longDesc, skills);
            } else if (id == 71) {
                CreatureTemplateCreator.createSharkHugeTemplate(id, name, plural, longDesc, skills);
            } else if (id == 40) {
                CreatureTemplateCreator.createHyenaLabilaTemplate(id, name, plural, longDesc, skills);
            } else if (id == 25) {
                CreatureTemplateCreator.createSpiderTemplate(id, name, plural, longDesc, skills);
            } else if (id == 56) {
                CreatureTemplateCreator.createLavaSpiderTemplate(id, name, plural, longDesc, skills);
            } else if (id == 57) {
                CreatureTemplateCreator.createLavaCreatureTemplate(id, name, plural, longDesc, skills);
            } else if (id == 44) {
                CreatureTemplateCreator.createPigTemplate(id, name, plural, longDesc, skills);
            } else if (id == 38) {
                CreatureTemplateCreator.createAnadondaTemplate(id, name, plural, longDesc, skills);
            } else if (id == 63) {
                CreatureTemplateCreator.createKingCobraTemplate(id, name, plural, longDesc, skills);
            } else if (id == 62) {
                CreatureTemplateCreator.createLadyLakeTemplate(id, name, plural, longDesc, skills);
            } else if (id == 69) {
                CreatureTemplateCreator.createZombieTemplate(id, name, plural, longDesc, skills);
            } else if (id == 72) {
                CreatureTemplateCreator.createDemonSolTemplate(id, name, plural, longDesc, skills);
            } else if (id == 73) {
                CreatureTemplateCreator.createDeathCrawlerMinionTemplate(id, name, plural, longDesc, skills);
            } else if (id == 74) {
                CreatureTemplateCreator.createSpawnUttachaTemplate(id, name, plural, longDesc, skills);
            } else if (id == 75) {
                CreatureTemplateCreator.createSonOfNogumpTemplate(id, name, plural, longDesc, skills);
            } else if (id == 76) {
                CreatureTemplateCreator.createDrakeSpiritTemplate(id, name, plural, longDesc, skills);
            } else if (id == 77) {
                CreatureTemplateCreator.createEagleSpiritTemplate(id, name, plural, longDesc, skills);
            } else if (id == 78) {
                CreatureTemplateCreator.createEpiphanyVynoraTemplate(id, name, plural, longDesc, skills);
            } else if (id == 79) {
                CreatureTemplateCreator.createMagranonJuggernautTemplate(id, name, plural, longDesc, skills);
            } else if (id == 80) {
                CreatureTemplateCreator.createManifestationFoTemplate(id, name, plural, longDesc, skills);
            } else if (id == 81) {
                CreatureTemplateCreator.createIncarnationLibilaTemplate(id, name, plural, longDesc, skills);
            } else if (id == 83) {
                CreatureTemplateCreator.createHellHorseTemplate(id, name, plural, longDesc, skills);
            } else if (id == 117) {
                CreatureTemplateCreator.createHellFoalTemplate(id, name, plural, longDesc, skills);
            } else if (id == 84) {
                CreatureTemplateCreator.createHellHoundTemplate(id, name, plural, longDesc, skills);
            } else if (id == 85) {
                CreatureTemplateCreator.createHellScorpionTemplate(id, name, plural, longDesc, skills);
            } else if (id == 86) {
                CreatureTemplateCreator.createWorgTemplate(id, name, plural, longDesc, skills);
            } else if (id == 87) {
                CreatureTemplateCreator.createSkeletonTemplate(id, name, plural, longDesc, skills);
            } else if (id == 88) {
                CreatureTemplateCreator.createWraithTemplate(id, name, plural, longDesc, skills);
            } else if (id == 93) {
                CreatureTemplateCreator.createSealTemplate(id, name, plural, longDesc, skills);
            } else if (id == 94) {
                CreatureTemplateCreator.createTortoiseTemplate(id, name, plural, longDesc, skills);
            } else if (id == 95) {
                CreatureTemplateCreator.createCrabTemplate(id, name, plural, longDesc, skills);
            } else if (id == 101) {
                CreatureTemplateCreator.createLambTemplate(id, name, plural, longDesc, skills);
            } else if (id == 96) {
                CreatureTemplateCreator.createSheepTemplate(id, name, plural, longDesc, skills);
            } else if (id == 102) {
                CreatureTemplateCreator.createRamTemplate(id, name, plural, longDesc, skills);
            } else if (id == 97) {
                CreatureTemplateCreator.createBlueWhaleTemplate(id, name, plural, longDesc, skills);
            } else if (id == 98) {
                CreatureTemplateCreator.createSealCubTemplate(id, name, plural, longDesc, skills);
            } else if (id == 99) {
                CreatureTemplateCreator.createDolphinTemplate(id, name, plural, longDesc, skills);
            } else if (id == 100) {
                CreatureTemplateCreator.createOctopusTemplate(id, name, plural, longDesc, skills);
            } else if (id == 105) {
                CreatureTemplateCreator.createFogSpiderTemplate(id, name, plural, longDesc, skills);
            } else if (id == 106) {
                CreatureTemplateCreator.createRiftTemplateOne(id, name, plural, longDesc, skills);
            } else if (id == 107) {
                CreatureTemplateCreator.createRiftTemplateTwo(id, name, plural, longDesc, skills);
            } else if (id == 108) {
                CreatureTemplateCreator.createRiftTemplateThree(id, name, plural, longDesc, skills);
            } else if (id == 109) {
                CreatureTemplateCreator.createRiftTemplateFour(id, name, plural, longDesc, skills);
            } else if (id == 110) {
                CreatureTemplateCreator.createRiftCasterTemplate(id, name, plural, longDesc, skills);
            } else if (id == 111) {
                CreatureTemplateCreator.createRiftOgreMageTemplate(id, name, plural, longDesc, skills);
            } else if (id == 112) {
                CreatureTemplateCreator.createRiftSummonerTemplate(id, name, plural, longDesc, skills);
            } else if (id == 113) {
                CreatureTemplateCreator.createNpcHumanTemplate(id, name, plural, longDesc, skills);
            } else if (id == 119) {
                CreatureTemplateCreator.createFishTemplate(id, name, plural, longDesc, skills);
            } else if (logger.isLoggable(Level.FINE)) {
                logger.fine("Using standard creature skills and characteristics for template id: " + id);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void createHellScorpionTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 30.0f);
        skills.learnTemp(104, 15.0f);
        skills.learnTemp(103, 40.0f);
        skills.learnTemp(100, 2.0f);
        skills.learnTemp(101, 5.0f);
        skills.learnTemp(105, 70.0f);
        skills.learnTemp(106, 4.0f);
        skills.learnTemp(10052, 44.0f);
        int[] types = new int[]{7, 6, 13, 16, 29, 32, 34, 39, 46, 55, 60, 61};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.multiped.scorpion.hell", types, (byte)8, skills, (short)5, (byte)0, (short)130, (short)30, (short)20, "sound.death.insect", "sound.death.insect", "sound.combat.hit.insect", "sound.combat.hit.insect", 0.3f, 8.0f, 14.0f, 13.0f, 0.0f, 0.0f, 0.75f, 1700, new int[]{439}, 7, 64, (byte)82);
        temp.setHandDamString("claw");
        temp.setBreathDamString("sting");
        temp.setAlignment(-40.0f);
        temp.setMaxAge(100);
        temp.setBaseCombatRating(18.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_STUDDED);
        temp.combatDamageType = (byte)2;
        temp.setMaxGroupAttackSize(5);
        temp.setColorBlue(255);
        temp.setColorGreen(255);
        temp.setColorRed(255);
        temp.setOnFire(false);
        temp.setGlowing(false);
        temp.setMaxPercentOfCreatures(0.02f);
    }

    private static void createHellHoundTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 20.0f);
        skills.learnTemp(104, 45.0f);
        skills.learnTemp(103, 25.0f);
        skills.learnTemp(100, 10.0f);
        skills.learnTemp(101, 15.0f);
        skills.learnTemp(105, 60.0f);
        skills.learnTemp(106, 12.0f);
        skills.learnTemp(10052, 40.0f);
        int[] types = new int[]{7, 6, 13, 3, 29, 36, 39, 32, 46, 55, 60, 61};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.dog.hell", types, (byte)3, skills, (short)7, (byte)0, (short)40, (short)20, (short)100, "sound.death.dog", "sound.death.dog", "sound.combat.hit.dog", "sound.combat.hit.dog", 0.6f, 10.0f, 0.0f, 12.0f, 0.0f, 0.0f, 1.2f, 300, new int[]{204}, 10, 94, (byte)74);
        temp.setHandDamString("claw");
        temp.setAlignment(-60.0f);
        temp.setMaxAge(35);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_LEATHER);
        temp.setBaseCombatRating(14.0f);
        temp.combatDamageType = 1;
        temp.setMaxGroupAttackSize(4);
        temp.setColorBlue(255);
        temp.setColorGreen(255);
        temp.setColorRed(255);
        temp.setOnFire(false);
        temp.setGlowing(true);
        temp.setMaxPercentOfCreatures(0.03f);
    }

    private static void createWorgTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 25.0f);
        skills.learnTemp(104, 25.0f);
        skills.learnTemp(103, 35.0f);
        skills.learnTemp(100, 10.0f);
        skills.learnTemp(101, 15.0f);
        skills.learnTemp(105, 20.0f);
        skills.learnTemp(106, 12.0f);
        skills.learnTemp(10052, 50.0f);
        int[] types = new int[]{7, 6, 13, 3, 29, 36, 39};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.wolf.worg", types, (byte)3, skills, (short)7, (byte)0, (short)40, (short)20, (short)100, "sound.death.dog", "sound.death.dog", "sound.combat.hit.dog", "sound.combat.hit.dog", 0.2f, 10.0f, 0.0f, 12.0f, 0.0f, 0.0f, 1.2f, 300, new int[0], 10, 94, (byte)87);
        temp.setHandDamString("claw");
        temp.setAlignment(-60.0f);
        temp.setMaxAge(4);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_STUDDED);
        temp.setBaseCombatRating(14.0f);
        temp.combatDamageType = (byte)2;
        temp.setMaxGroupAttackSize(4);
    }

    private static void createHellHorseTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 35.0f);
        skills.learnTemp(104, 20.0f);
        skills.learnTemp(103, 40.0f);
        skills.learnTemp(100, 7.0f);
        skills.learnTemp(101, 7.0f);
        skills.learnTemp(105, 72.0f);
        skills.learnTemp(106, 5.0f);
        skills.learnTemp(10052, 38.0f);
        int[] types = new int[]{7, 41, 3, 14, 9, 27, 32, 6, 39, 55};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.horse.hell", types, (byte)1, skills, (short)3, (byte)0, (short)180, (short)50, (short)250, "sound.death.horse", "sound.death.horse", "sound.combat.hit.horse", "sound.combat.hit.horse", 1.0f, 5.0f, 7.0f, 10.0f, 0.0f, 0.0f, 1.8f, 100, new int[]{307, 306, 140, 71, 309, 308, 308}, 5, 0, (byte)79);
        temp.setMaxAge(200);
        temp.setBaseCombatRating(9.0f);
        temp.combatDamageType = (byte)4;
        temp.setCombatMoves(new int[]{10});
        temp.setAlignment(-40.0f);
        temp.setHandDamString("kick");
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CLOTH);
        temp.setMaxGroupAttackSize(4);
        temp.setColorBlue(255);
        temp.setColorGreen(255);
        temp.setColorRed(255);
        temp.setOnFire(false);
        temp.setGlowing(true);
        temp.setMaxPercentOfCreatures(0.03f);
        temp.setChildTemplateId(117);
        temp.isHorse = true;
        temp.setColourNames(new String[]{"ash", "cinder", "envious", "shadow", "pestilential", "nightshade", "incandescent", "molten"});
    }

    private static void createHellFoalTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 15.0f);
        skills.learnTemp(104, 10.0f);
        skills.learnTemp(103, 15.0f);
        skills.learnTemp(100, 3.0f);
        skills.learnTemp(101, 3.0f);
        skills.learnTemp(105, 35.0f);
        skills.learnTemp(106, 2.0f);
        skills.learnTemp(10052, 15.0f);
        int[] types = new int[]{7, 3, 14, 9, 27, 32, 6, 39, 55, 63};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.horse.hell.foal", types, (byte)1, skills, (short)3, (byte)0, (short)100, (short)50, (short)75, "sound.death.horse", "sound.death.horse", "sound.combat.hit.horse", "sound.combat.hit.horse", 1.0f, 3.0f, 4.0f, 4.0f, 0.0f, 0.0f, 1.2f, 100, new int[]{307, 306, 140, 71, 309, 308, 308}, 5, 0, (byte)79);
        temp.setMaxAge(100);
        temp.setBaseCombatRating(5.0f);
        temp.combatDamageType = (byte)4;
        temp.setAlignment(-40.0f);
        temp.setHandDamString("kick");
        temp.setAdultFemaleTemplateId(83);
        temp.setAdultMaleTemplateId(83);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CLOTH);
        temp.setMaxGroupAttackSize(2);
        temp.setColorBlue(255);
        temp.setColorGreen(255);
        temp.setColorRed(255);
        temp.setOnFire(false);
        temp.setGlowing(true);
        temp.setMaxPercentOfCreatures(0.015f);
        temp.isHorse = true;
        temp.setCorpseName("hellhorse.foal.");
        temp.setColourNames(new String[]{"ash", "cinder", "envious", "shadow", "pestilential", "nightshade", "incandescent", "molten"});
    }

    private static void createIncarnationLibilaTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 80.0f);
        skills.learnTemp(104, 45.0f);
        skills.learnTemp(103, 70.0f);
        skills.learnTemp(100, 44.0f);
        skills.learnTemp(101, 30.0f);
        skills.learnTemp(105, 40.0f);
        skills.learnTemp(106, 49.0f);
        skills.learnTemp(10052, 75.0f);
        int[] types = new int[]{8, 13, 16, 27, 36, 12, 62, 24, 25, 40, 45, 47};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.giant.incarnation", types, (byte)0, skills, (short)20, (byte)1, (short)570, (short)100, (short)60, "sound.death.libila.incarnation", "sound.death.libila.incarnation", "sound.combat.hit.libila.incarnation", "sound.combat.hit.libila.incarnation", 0.03f, 7.0f, 30.0f, 30.0f, 40.0f, 0.0f, 1.5f, 10, new int[]{683, 683, 308, 308}, 10, 5, (byte)87);
        temp.setAlignment(-100.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_PLATE);
        temp.setBaseCombatRating(86.0f);
        temp.setMaxGroupAttackSize(30);
        temp.combatDamageType = 1;
        temp.setCombatMoves(new int[]{7, 2, 1});
        temp.hasHands = true;
    }

    private static void createManifestationFoTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 70.0f);
        skills.learnTemp(104, 35.0f);
        skills.learnTemp(103, 70.0f);
        skills.learnTemp(100, 24.0f);
        skills.learnTemp(101, 30.0f);
        skills.learnTemp(105, 50.0f);
        skills.learnTemp(106, 59.0f);
        skills.learnTemp(10052, 75.0f);
        int[] types = new int[]{8, 13, 16, 27, 36, 12, 24, 40, 45, 47, 62};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.giant.manifestation", types, (byte)0, skills, (short)20, (byte)0, (short)570, (short)100, (short)60, "sound.death.fo.manifestation", "sound.death.fo.manifestation", "sound.combat.hit.fo.manifestation", "sound.combat.hit.fo.manifestation", 0.03f, 10.0f, 30.0f, 30.0f, 40.0f, 0.0f, 1.5f, 10, new int[]{683, 683, 308, 308}, 10, 5, (byte)87);
        temp.setAlignment(100.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_PLATE);
        temp.setBaseCombatRating(86.0f);
        temp.setMaxGroupAttackSize(30);
        temp.combatDamageType = 0;
        temp.setCombatMoves(new int[]{8, 1});
        temp.hasHands = true;
    }

    private static void createMagranonJuggernautTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 90.0f);
        skills.learnTemp(104, 35.0f);
        skills.learnTemp(103, 90.0f);
        skills.learnTemp(100, 14.0f);
        skills.learnTemp(101, 10.0f);
        skills.learnTemp(105, 20.0f);
        skills.learnTemp(106, 19.0f);
        skills.learnTemp(10052, 75.0f);
        int[] types = new int[]{8, 13, 16, 27, 36, 12, 24, 40, 45, 47, 62};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.giant.juggernaut", types, (byte)0, skills, (short)20, (byte)0, (short)570, (short)100, (short)60, "sound.death.magranon.juggernaut", "sound.death.magranon.juggernaut", "sound.combat.hit.magranon.juggernaut", "sound.combat.hit.magranon.juggernaut", 0.03f, 10.0f, 30.0f, 30.0f, 40.0f, 0.0f, 1.5f, 10, new int[]{683, 683, 308, 308}, 10, 5, (byte)87);
        temp.setAlignment(100.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_PLATE);
        temp.setBaseCombatRating(96.0f);
        temp.setMaxGroupAttackSize(30);
        temp.combatDamageType = 0;
        temp.setCombatMoves(new int[]{8, 5, 1});
        temp.hasHands = true;
    }

    private static void createEpiphanyVynoraTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 50.0f);
        skills.learnTemp(104, 65.0f);
        skills.learnTemp(103, 80.0f);
        skills.learnTemp(100, 24.0f);
        skills.learnTemp(101, 30.0f);
        skills.learnTemp(105, 80.0f);
        skills.learnTemp(106, 39.0f);
        skills.learnTemp(10052, 75.0f);
        int[] types = new int[]{8, 24, 13, 16, 27, 62, 36, 12, 40, 45, 47};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.giant.epiphany", types, (byte)0, skills, (short)20, (byte)0, (short)570, (short)100, (short)60, "sound.death.vynora.epiphany", "sound.death.vynora.epiphany", "sound.combat.hit.vynora.epiphany", "sound.combat.hit.vynora.epiphany", 0.03f, 10.0f, 24.0f, 26.0f, 0.0f, 0.0f, 1.5f, 10, new int[]{683, 683, 308, 308}, 10, 5, (byte)87);
        temp.setAlignment(100.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CHAIN);
        temp.setBaseCombatRating(96.0f);
        temp.setMaxGroupAttackSize(30);
        temp.combatDamageType = (byte)2;
        temp.setCombatMoves(new int[]{7, 5});
        temp.hasHands = true;
        temp.setPaintMode(1);
        temp.setGlowing(true);
    }

    private static void createEagleSpiritTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 20.0f);
        skills.learnTemp(104, 35.0f);
        skills.learnTemp(103, 30.0f);
        skills.learnTemp(100, 24.0f);
        skills.learnTemp(101, 20.0f);
        skills.learnTemp(105, 50.0f);
        skills.learnTemp(106, 29.0f);
        skills.learnTemp(10052, 45.0f);
        int[] types = new int[]{41, 8, 13, 16, 29, 12, 24, 40, 22, 46, 50};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.eagle.spirit", types, (byte)6, skills, (short)30, (byte)1, (short)150, (short)90, (short)320, "sound.death.dragon", "sound.death.dragon", "sound.combat.hit.dragon", "sound.combat.hit.dragon", 0.1f, 7.0f, 10.0f, 13.0f, 17.0f, 0.0f, 1.9f, 500, new int[]{683, 303, 308, 308, 310}, 20, 49, (byte)77);
        temp.setHeadbuttDamString("tailwhip");
        temp.setKickDamString("wingbuff");
        temp.setAlignment(0.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CHAIN);
        temp.setEggLayer(true);
        temp.setEggTemplateId(77);
        temp.setBaseCombatRating(15.0f);
        temp.setMaxGroupAttackSize(6);
        temp.combatDamageType = 1;
        temp.setCombatMoves(new int[]{1});
    }

    private static void createDrakeSpiritTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 20.0f);
        skills.learnTemp(104, 65.0f);
        skills.learnTemp(103, 30.0f);
        skills.learnTemp(100, 34.0f);
        skills.learnTemp(101, 40.0f);
        skills.learnTemp(105, 80.0f);
        skills.learnTemp(106, 39.0f);
        skills.learnTemp(10052, 75.0f);
        int[] types = new int[]{41, 8, 13, 16, 29, 12, 40, 22, 24, 46, 50};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.drake.spirit", types, (byte)6, skills, (short)30, (byte)1, (short)150, (short)90, (short)320, "sound.death.drakespirit", "sound.death.drakespirit", "sound.combat.hit.drakespirit", "sound.combat.hit.drakespirit", 0.1f, 7.0f, 10.0f, 13.0f, 17.0f, 0.0f, 1.9f, 500, new int[]{683, 303, 308, 308, 310}, 20, 49, (byte)77);
        temp.setHeadbuttDamString("tailwhip");
        temp.setKickDamString("wingbuff");
        temp.setAlignment(0.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_LEATHER_DRAGON);
        temp.setEggLayer(true);
        temp.setEggTemplateId(76);
        temp.setBaseCombatRating(27.0f);
        temp.setMaxGroupAttackSize(10);
        temp.combatDamageType = (byte)2;
        temp.setHandDamString("claw");
        temp.setKickDamString("claw");
        temp.setCombatMoves(new int[]{1, 7});
    }

    private static void createSonOfNogumpTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 80.0f);
        skills.learnTemp(104, 35.0f);
        skills.learnTemp(103, 80.0f);
        skills.learnTemp(100, 7.0f);
        skills.learnTemp(101, 17.0f);
        skills.learnTemp(105, 30.0f);
        skills.learnTemp(106, 29.0f);
        skills.learnTemp(10052, 80.0f);
        skills.learnTemp(10053, 80.0f);
        skills.learnTemp(10054, 50.0f);
        skills.learnTemp(10055, 70.0f);
        skills.learnTemp(1000, 90.0f);
        skills.learnTemp(10028, 90.0f);
        int[] types = new int[]{30, 8, 13, 16, 27, 24, 40, 45, 46, 50};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.nogump.son", types, (byte)4, skills, (short)20, (byte)0, (short)570, (short)200, (short)80, "sound.death.nogump.son", "sound.death.nogump.son", "sound.combat.hit.nogump.son", "sound.combat.hit.nogump.son", 0.3f, 26.0f, 30.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1500, new int[]{683}, 20, 49, (byte)81);
        temp.setAlignment(10.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_STUDDED);
        temp.keepSex = true;
        temp.setBaseCombatRating(6.0f);
        temp.setMaxGroupAttackSize(10);
        temp.setCombatMoves(new int[]{8, 1});
        temp.hasHands = true;
    }

    private static void createSpawnUttachaTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 15.0f);
        skills.learnTemp(104, 15.0f);
        skills.learnTemp(103, 50.0f);
        skills.learnTemp(100, 3.0f);
        skills.learnTemp(101, 10.0f);
        skills.learnTemp(105, 4.0f);
        skills.learnTemp(106, 2.0f);
        skills.learnTemp(10052, 30.0f);
        int[] types = new int[]{7, 13, 16, 29, 39, 24, 40, 46, 50};
        int biteDamage = 15;
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.spawn.uttacha", types, (byte)9, skills, (short)5, (byte)1, (short)250, (short)100, (short)150, "sound.death.uttacha.spawn", "sound.death.uttacha.spawn", "sound.combat.hit.deathcrawler", "sound.combat.hit.deathcrawler", 0.7f, 7.0f, 0.0f, 15.0f, 0.0f, 0.0f, 0.5f, 1500, new int[]{153, 683}, 10, 34, (byte)81);
        temp.setAlignment(-10.0f);
        temp.setMaxAge(200);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_LEATHER);
        temp.setBaseCombatRating(12.0f);
        temp.combatDamageType = (byte)10;
        temp.setMaxGroupAttackSize(8);
        temp.setHandDamString("bite");
    }

    private static void createDeathCrawlerMinionTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 15.0f);
        skills.learnTemp(104, 25.0f);
        skills.learnTemp(103, 20.0f);
        skills.learnTemp(100, 3.0f);
        skills.learnTemp(101, 10.0f);
        skills.learnTemp(105, 30.0f);
        skills.learnTemp(106, 5.0f);
        skills.learnTemp(10052, 40.0f);
        int[] types = new int[]{7, 13, 16, 29, 39, 24, 40, 46, 50};
        int biteDamage = 15;
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.deathcrawler", types, (byte)8, skills, (short)5, (byte)0, (short)250, (short)575, (short)198, "sound.death.deathcrawler", "sound.death.deathcrawler", "sound.combat.hit.deathcrawler", "sound.combat.hit.deathcrawler", 0.3f, 8.0f, 0.0f, 15.0f, 0.0f, 0.0f, 1.1f, 1500, new int[]{683, 310}, 10, 34, (byte)87);
        temp.setBoundsValues(-1.065511f, -2.90318f, 1.065511f, 3.029689f);
        temp.setAlignment(-10.0f);
        temp.setMaxAge(200);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CHAIN);
        temp.setBaseCombatRating(14.0f);
        temp.combatDamageType = (byte)5;
        temp.setMaxGroupAttackSize(10);
        temp.setHandDamString("claw");
        temp.setUsesNewAttacks(true);
        temp.addPrimaryAttack(new AttackAction("strike", AttackIdentifier.STRIKE, new AttackValues(8.0f, 0.02f, 3.0f, 2, 1, 1, false, 3, 1.0f)));
        temp.addPrimaryAttack(new AttackAction("poison strike", AttackIdentifier.STRIKE, new AttackValues(8.0f, 0.02f, 8.0f, 2, 1, 5, false, 3, 1.0f)));
        temp.addSecondaryAttack(new AttackAction("bite", AttackIdentifier.BITE, new AttackValues(15.0f, 0.08f, 4.0f, 3, 1, 3, false, 4, 1.1f)));
        temp.addSecondaryAttack(new AttackAction("claw", AttackIdentifier.CLAW, new AttackValues(10.0f, 0.04f, 4.0f, 3, 1, 0, false, 5, 1.2f)));
        temp.setCombatMoves(new int[]{1});
    }

    private static void createDemonSolTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 35.0f);
        skills.learnTemp(104, 35.0f);
        skills.learnTemp(103, 30.0f);
        skills.learnTemp(100, 6.0f);
        skills.learnTemp(101, 30.0f);
        skills.learnTemp(105, 50.0f);
        skills.learnTemp(106, 20.0f);
        skills.learnTemp(10052, 70.0f);
        int[] types = new int[]{8, 13, 16, 29, 30, 36, 39, 24, 40, 45, 46, 50, 55};
        int biteDamage = 20;
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.demon.sol", types, (byte)0, skills, (short)7, (byte)0, (short)150, (short)100, (short)150, "sound.death.demon", "sound.death.demon", "sound.combat.hit.demon", "sound.combat.hit.demon", 0.5f, 6.0f, 10.0f, 20.0f, 6.0f, 12.0f, 1.5f, 1550, new int[]{204, 636, 683}, 30, 49, (byte)87);
        temp.setHandDamString("burn");
        temp.setAlignment(-90.0f);
        temp.setMaxAge(200);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CHAIN);
        temp.setBaseCombatRating(14.0f);
        temp.combatDamageType = (byte)4;
        temp.setOnFire(true);
        temp.setFireRadius((byte)5);
        temp.setMaxGroupAttackSize(6);
    }

    private static void createZombieTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 30.0f);
        skills.learnTemp(104, 20.0f);
        skills.learnTemp(103, 40.0f);
        skills.learnTemp(100, 20.0f);
        skills.learnTemp(101, 20.0f);
        skills.learnTemp(105, 20.0f);
        skills.learnTemp(106, 20.0f);
        skills.learnTemp(1007, 1.0f);
        skills.learnTemp(1009, 1.0f);
        skills.learnTemp(1008, 1.0f);
        skills.learnTemp(1019, 1.0f);
        skills.learnTemp(10049, 1.0f);
        skills.learnTemp(1011, 1.0f);
        skills.learnTemp(10033, 1.0f);
        skills.learnTemp(10031, 1.0f);
        skills.learnTemp(10052, 70.0f);
        int[] types = new int[]{13, 17, 7, 25, 29};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.human.player", types, (byte)0, skills, (short)80, (byte)0, (short)180, (short)20, (short)35, "sound.combat.death.zombie", "sound.combat.death.zombie", "sound.combat.hit.zombie", "sound.combat.hit.zombie", 1.0f, 5.0f, 3.0f, 7.0f, 0.0f, 0.0f, 0.6f, 0, new int[0], 25, 100, (byte)2);
        temp.setBaseCombatRating(8.0f);
        temp.hasHands = true;
        temp.setAlignment(-20.0f);
        temp.setMaxAge(2);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_LEATHER);
        temp.combatDamageType = 0;
        temp.setPaintMode(1);
        temp.setMaxGroupAttackSize(3);
    }

    private static void createSkeletonTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 10.0f);
        skills.learnTemp(104, 20.0f);
        skills.learnTemp(103, 20.0f);
        skills.learnTemp(100, 5.0f);
        skills.learnTemp(101, 10.0f);
        skills.learnTemp(105, 10.0f);
        skills.learnTemp(106, 10.0f);
        skills.learnTemp(10052, 70.0f);
        int[] types = new int[]{13, 7, 24};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.human.skeleton", types, (byte)0, skills, (short)80, (byte)0, (short)180, (short)20, (short)35, "sound.combat.death.skeleton", "sound.combat.death.skeleton", "sound.combat.hit.skeleton", "sound.combat.hit.skeleton", 1.0f, 3.0f, 3.0f, 7.0f, 0.0f, 0.0f, 0.6f, 0, new int[0], 25, 100, (byte)2);
        temp.setBaseCombatRating(8.0f);
        temp.hasHands = true;
        temp.setAlignment(-20.0f);
        temp.setMaxAge(2);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CHAIN);
        temp.combatDamageType = 0;
        temp.physicalResistance = 0.2f;
        temp.acidVulnerability = 3.0f;
        temp.fireVulnerability = 2.0f;
        temp.setMaxGroupAttackSize(3);
    }

    private static void createLadyLakeTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 15.0f);
        skills.learnTemp(104, 15.0f);
        skills.learnTemp(103, 10.0f);
        skills.learnTemp(100, 10.0f);
        skills.learnTemp(101, 10.0f);
        skills.learnTemp(105, 90.0f);
        skills.learnTemp(106, 99.0f);
        skills.learnTemp(10052, 40.0f);
        int[] types = new int[]{0, 17, 18, 40, 20, 37};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.human.ladylake", types, (byte)0, skills, (short)5, (byte)1, (short)180, (short)20, (short)35, "sound.death.male", "sound.death.female", "sound.combat.hit.male", "sound.combat.hit.female", 0.001f, 10.0f, 20.0f, 0.0f, 0.0f, 0.0f, 0.8f, 0, new int[]{308, 308}, 3, 1, (byte)80);
        temp.setKeepSex(true);
        temp.setBaseCombatRating(99.0f);
        temp.setMaxGroupAttackSize(10);
        temp.setCombatDamageType((byte)1);
        temp.hasHands = true;
    }

    private static void createKingCobraTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 50.0f);
        skills.learnTemp(104, 45.0f);
        skills.learnTemp(103, 30.0f);
        skills.learnTemp(100, 42.0f);
        skills.learnTemp(101, 42.0f);
        skills.learnTemp(105, 99.0f);
        skills.learnTemp(106, 3.0f);
        skills.learnTemp(10052, 95.0f);
        int[] types = new int[]{0, 3, 40, 20};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.snake.kingcobra", types, (byte)9, skills, (short)5, (byte)0, (short)20, (short)20, (short)450, "sound.death.snake", "sound.death.snake", "sound.combat.hit.snake", "sound.combat.hit.snake", 0.001f, 10.0f, 0.0f, 20.0f, 0.0f, 30.0f, 0.8f, 0, new int[]{303, 310}, 1, 1, (byte)86);
        temp.setHandDamString("bite");
        temp.setBreathDamString("squeeze");
        temp.setMaxAge(1000000);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_LEATHER);
        temp.setBaseCombatRating(87.0f);
        temp.setMaxGroupAttackSize(10);
        temp.combatDamageType = 0;
        temp.setAlignment(-100.0f);
    }

    private static void createAnadondaTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 30.0f);
        skills.learnTemp(104, 25.0f);
        skills.learnTemp(103, 10.0f);
        skills.learnTemp(100, 2.0f);
        skills.learnTemp(101, 4.0f);
        skills.learnTemp(105, 30.0f);
        skills.learnTemp(106, 3.0f);
        skills.learnTemp(10052, 65.0f);
        int[] types = new int[]{7, 3, 29, 12, 6, 39, 24, 60, 61};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.snake.anaconda", types, (byte)9, skills, (short)10, (byte)0, (short)20, (short)20, (short)350, "sound.death.snake", "sound.death.snake", "sound.combat.hit.snake", "sound.combat.hit.snake", 0.3f, 0.0f, 0.0f, 6.0f, 0.0f, 10.0f, 0.8f, 50, new int[]{303, 310}, 10, 24, (byte)86);
        temp.setBreathDamString("squeeze");
        temp.setMaxAge(100);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_LEATHER);
        temp.setBaseCombatRating(13.0f);
        temp.setMaxGroupAttackSize(4);
        temp.combatDamageType = 0;
        temp.setMaxPercentOfCreatures(0.02f);
    }

    private static void createPigTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 10.0f);
        skills.learnTemp(104, 15.0f);
        skills.learnTemp(103, 10.0f);
        skills.learnTemp(100, 2.0f);
        skills.learnTemp(101, 4.0f);
        skills.learnTemp(105, 10.0f);
        skills.learnTemp(106, 3.0f);
        skills.learnTemp(10052, 5.0f);
        int[] types = new int[]{7, 3, 43, 27, 14, 32};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.pig", types, (byte)3, skills, (short)10, (byte)0, (short)50, (short)50, (short)150, "sound.death.pig", "sound.death.pig", "sound.combat.hit.pig", "sound.combat.hit.pig", 1.0f, 1.0f, 0.0f, 2.0f, 0.0f, 0.0f, 0.8f, 20, new int[]{303, 140, 310, 308, 308}, 10, 54, (byte)84);
        temp.setHandDamString("kick");
        temp.setMaxAge(100);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CLOTH);
        temp.setBaseCombatRating(2.0f);
        temp.setMaxGroupAttackSize(3);
        temp.combatDamageType = 0;
        temp.setMaxPercentOfCreatures(0.03f);
    }

    private static void createLavaCreatureTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 25.0f);
        skills.learnTemp(104, 35.0f);
        skills.learnTemp(103, 30.0f);
        skills.learnTemp(100, 12.0f);
        skills.learnTemp(101, 14.0f);
        skills.learnTemp(105, 90.0f);
        skills.learnTemp(106, 5.0f);
        skills.learnTemp(10052, 50.0f);
        int[] types = new int[]{7, 6, 13, 16, 40, 29, 30, 34, 32, 36, 39, 45, 55, 61};
        int biteDamage = 10;
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.lavacreature", types, (byte)0, skills, (short)5, (byte)0, (short)150, (short)100, (short)150, "sound.death.lizard", "sound.death.lizard", "sound.combat.hit.lizard", "sound.combat.hit.lizard", 0.5f, 6.0f, 10.0f, 10.0f, 0.0f, 0.0f, 1.0f, 1500, new int[]{204, 446, 636}, 10, 34, (byte)87);
        temp.setHandDamString("burn");
        temp.setAlignment(-20.0f);
        temp.setMaxAge(200);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_PLATE);
        temp.setBaseCombatRating(16.0f);
        temp.combatDamageType = (byte)4;
        temp.setOnFire(false);
        temp.setMaxGroupAttackSize(6);
        temp.setGlowing(false);
        temp.setSubterranean(true);
        temp.setMaxPercentOfCreatures(0.03f);
    }

    private static void createLavaSpiderTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 20.0f);
        skills.learnTemp(104, 45.0f);
        skills.learnTemp(103, 35.0f);
        skills.learnTemp(100, 8.0f);
        skills.learnTemp(101, 10.0f);
        skills.learnTemp(105, 40.0f);
        skills.learnTemp(106, 2.0f);
        skills.learnTemp(10052, 70.0f);
        int[] types = new int[]{7, 6, 13, 3, 29, 39, 55, 60, 61};
        int biteDamage = 7;
        if (Servers.localServer.PVPSERVER) {
            biteDamage = 10;
        }
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.multiped.spider.lava", types, (byte)8, skills, (short)5, (byte)0, (short)150, (short)100, (short)150, "sound.death.spider", "sound.death.spider", "sound.combat.hit.spider", "sound.combat.hit.spider", 0.6f, 6.0f, 0.0f, biteDamage, 0.0f, 0.0f, 0.9f, 500, new int[]{204, 636}, 10, 54, (byte)82);
        temp.setHandDamString("claw");
        temp.setAlignment(-50.0f);
        temp.setMaxAge(200);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_PLATE);
        temp.setBaseCombatRating(13.0f);
        temp.combatDamageType = (byte)4;
        temp.setOnFire(true);
        temp.setFireRadius((byte)40);
        temp.setMaxGroupAttackSize(6);
        temp.setGlowing(true);
        temp.setDenName("spider lair");
        temp.setDenMaterial((byte)15);
        temp.setSubterranean(true);
        temp.setMaxPercentOfCreatures(0.05f);
    }

    private static void createFogSpiderTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 25.0f);
        skills.learnTemp(104, 45.0f);
        skills.learnTemp(103, 35.0f);
        skills.learnTemp(100, 8.0f);
        skills.learnTemp(101, 12.0f);
        skills.learnTemp(105, 40.0f);
        skills.learnTemp(106, 2.0f);
        skills.learnTemp(10052, 70.0f);
        int[] types = new int[]{7, 6, 13, 3, 29, 39};
        int biteDamage = 11;
        if (Servers.localServer.PVPSERVER) {
            biteDamage = 14;
        }
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.multiped.spider.fog", types, (byte)8, skills, (short)5, (byte)0, (short)150, (short)100, (short)150, "sound.death.spider", "sound.death.spider", "sound.combat.hit.spider", "sound.combat.hit.spider", 0.2f, 3.0f, 0.0f, biteDamage, 0.0f, 0.0f, 0.8f, 500, new int[0], 15, 74, (byte)82);
        temp.setHandDamString("claw");
        temp.setAlignment(0.0f);
        temp.setMaxAge(30);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_PLATE);
        temp.setBaseCombatRating(15.0f);
        temp.combatDamageType = (byte)5;
        temp.setMaxGroupAttackSize(6);
        temp.setMaxPercentOfCreatures(0.01f);
        temp.setCombatMoves(new int[]{11});
    }

    private static void createSpiderTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 20.0f);
        skills.learnTemp(104, 45.0f);
        skills.learnTemp(103, 35.0f);
        skills.learnTemp(100, 8.0f);
        skills.learnTemp(101, 10.0f);
        skills.learnTemp(105, 40.0f);
        skills.learnTemp(106, 2.0f);
        skills.learnTemp(10052, 40.0f);
        int[] types = new int[]{7, 6, 13, 3, 29, 39, 60, 61};
        int biteDamage = 6;
        if (Servers.localServer.PVPSERVER) {
            biteDamage = 10;
        }
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.multiped.spider.huge", types, (byte)8, skills, (short)5, (byte)0, (short)150, (short)100, (short)150, "sound.death.spider", "sound.death.spider", "sound.combat.hit.spider", "sound.combat.hit.spider", 0.7f, 0.0f, 0.0f, biteDamage, 0.0f, 0.0f, 1.2f, 500, new int[]{636, 308, 308, 308, 308}, 10, 74, (byte)82);
        temp.setHandDamString("claw");
        temp.setAlignment(-50.0f);
        temp.setMaxAge(100);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_PLATE);
        temp.setBaseCombatRating(10.0f);
        temp.combatDamageType = (byte)2;
        temp.setMaxGroupAttackSize(6);
        temp.setDenName("spider lair");
        temp.setDenMaterial((byte)15);
        temp.setSubterranean(true);
        temp.setMaxPercentOfCreatures(0.08f);
    }

    private static void createHyenaLabilaTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 20.0f);
        skills.learnTemp(104, 45.0f);
        skills.learnTemp(103, 35.0f);
        skills.learnTemp(100, 8.0f);
        skills.learnTemp(101, 10.0f);
        skills.learnTemp(105, 40.0f);
        skills.learnTemp(106, 2.0f);
        skills.learnTemp(10052, 40.0f);
        int[] types = new int[]{7, 41, 25, 13, 3, 29, 36, 39};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.hyena.rabid", types, (byte)3, skills, (short)10, (byte)0, (short)40, (short)20, (short)100, "sound.death.dog", "sound.death.dog", "sound.combat.hit.dog", "sound.combat.hit.dog", 0.6f, 10.0f, 0.0f, 12.0f, 0.0f, 0.0f, 1.2f, 300, new int[0], 10, 94, (byte)87);
        temp.setHandDamString("claw");
        temp.setAlignment(-50.0f);
        temp.setMaxAge(5);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CLOTH);
        temp.setBaseCombatRating(14.0f);
        temp.combatDamageType = 1;
        temp.setMaxGroupAttackSize(8);
        temp.setMaxPercentOfCreatures(0.01f);
    }

    private static void createSharkHugeTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 20.0f);
        skills.learnTemp(104, 45.0f);
        skills.learnTemp(103, 50.0f);
        skills.learnTemp(100, 14.0f);
        skills.learnTemp(101, 10.0f);
        skills.learnTemp(105, 40.0f);
        skills.learnTemp(106, 5.0f);
        skills.learnTemp(10052, 85.0f);
        skills.learnTemp(1023, 80.0f);
        int[] types = new int[]{8, 38, 6, 13, 16, 29, 44, 40, 48, 37, 61};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.fish.shark.huge", types, (byte)9, skills, (short)3, (byte)0, (short)100, (short)1000, (short)100, "sound.death.snake", "sound.death.snake", "sound.combat.hit.snake", "sound.combat.hit.snake", 0.2f, 0.0f, 0.0f, 16.0f, 10.0f, 0.0f, 1.0f, 100, new int[]{308, 308, 310}, 40, 59, (byte)85);
        temp.setAlignment(-20.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CHAIN);
        temp.setBaseCombatRating(12.0f);
        temp.combatDamageType = (byte)3;
        temp.hasHands = false;
        temp.setMaxAge(40);
        temp.offZ = -1.4f;
        temp.setBonusCombatRating(12.0f);
        temp.setMaxGroupAttackSize(8);
    }

    private static void createBlueWhaleTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 50.0f);
        skills.learnTemp(104, 20.0f);
        skills.learnTemp(103, 50.0f);
        skills.learnTemp(100, 14.0f);
        skills.learnTemp(101, 10.0f);
        skills.learnTemp(105, 40.0f);
        skills.learnTemp(106, 5.0f);
        skills.learnTemp(10052, 85.0f);
        int[] types = new int[]{8, 38, 13, 3, 29, 44, 48, 37, 60, 61};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.fish.blue.whale", types, (byte)9, skills, (short)3, (byte)0, (short)200, (short)1000, (short)300, "sound.death.snake", "sound.death.snake", "sound.combat.hit.snake", "sound.combat.hit.snake", 0.3f, 0.0f, 0.0f, 25.0f, 15.0f, 0.0f, 1.0f, 100, new int[]{308, 308}, 40, 59, (byte)85);
        temp.setAlignment(-20.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CHAIN);
        temp.setBaseCombatRating(18.0f);
        temp.combatDamageType = (byte)3;
        temp.hasHands = false;
        temp.setMaxAge(80);
        temp.offZ = -1.4f;
        temp.setBonusCombatRating(12.0f);
        temp.setMaxPopulationOfCreatures(3);
        temp.setMaxGroupAttackSize(20);
    }

    private static void createDolphinTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 25.0f);
        skills.learnTemp(104, 65.0f);
        skills.learnTemp(103, 50.0f);
        skills.learnTemp(100, 14.0f);
        skills.learnTemp(101, 10.0f);
        skills.learnTemp(105, 40.0f);
        skills.learnTemp(106, 5.0f);
        skills.learnTemp(10052, 60.0f);
        int[] types = new int[]{8, 38, 13, 16, 29, 44, 48, 37, 60, 61};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.fish.dolphin", types, (byte)9, skills, (short)3, (byte)0, (short)80, (short)250, (short)50, "sound.death.snake", "sound.death.snake", "sound.combat.hit.snake", "sound.combat.hit.snake", 0.7f, 0.0f, 0.0f, 16.0f, 10.0f, 0.0f, 1.0f, 100, new int[]{308, 308}, 40, 59, (byte)85);
        temp.setAlignment(-20.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CHAIN);
        temp.setBaseCombatRating(9.0f);
        temp.combatDamageType = (byte)3;
        temp.hasHands = false;
        temp.setMaxAge(80);
        temp.offZ = -1.4f;
        temp.setBonusCombatRating(7.0f);
        temp.setMaxPercentOfCreatures(0.01f);
        temp.setMaxPopulationOfCreatures(150);
        temp.setMaxGroupAttackSize(4);
    }

    private static void createOctopusTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 35.0f);
        skills.learnTemp(104, 45.0f);
        skills.learnTemp(103, 40.0f);
        skills.learnTemp(100, 14.0f);
        skills.learnTemp(101, 10.0f);
        skills.learnTemp(105, 40.0f);
        skills.learnTemp(106, 5.0f);
        skills.learnTemp(10052, 60.0f);
        int[] types = new int[]{8, 38, 13, 16, 29, 44, 48, 37, 60, 61};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.fish.octopus", types, (byte)9, skills, (short)3, (byte)0, (short)100, (short)100, (short)100, "sound.death.snake", "sound.death.snake", "sound.combat.hit.snake", "sound.combat.hit.snake", 1.0f, 0.0f, 0.0f, 14.0f, 12.0f, 0.0f, 1.0f, 100, new int[]{308, 308, 752}, 40, 59, (byte)85);
        temp.setAlignment(-20.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CHAIN);
        temp.setBaseCombatRating(9.0f);
        temp.combatDamageType = (byte)3;
        temp.hasHands = false;
        temp.setMaxAge(80);
        temp.offZ = -1.4f;
        temp.setBonusCombatRating(7.0f);
        temp.setMaxPercentOfCreatures(0.01f);
        temp.setMaxPopulationOfCreatures(150);
        temp.setMaxGroupAttackSize(3);
    }

    private static void createSealTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 20.0f);
        skills.learnTemp(104, 45.0f);
        skills.learnTemp(103, 30.0f);
        skills.learnTemp(100, 6.0f);
        skills.learnTemp(101, 8.0f);
        skills.learnTemp(105, 10.0f);
        skills.learnTemp(106, 15.0f);
        skills.learnTemp(10052, 25.0f);
        int[] types = new int[]{8, 12, 35, 3, 29, 44, 48, 51, 32, 60, 61};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.fish.seal", types, (byte)9, skills, (short)3, (byte)0, (short)100, (short)100, (short)100, "sound.death.snake", "sound.death.snake", "sound.combat.hit.snake", "sound.combat.hit.snake", 0.7f, 0.0f, 0.0f, 8.0f, 5.0f, 0.0f, 0.5f, 100, new int[]{140, 140, 71, 71, 310}, 40, 59, (byte)85);
        temp.setAlignment(-20.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CHAIN);
        temp.setBaseCombatRating(7.0f);
        temp.setChildTemplateId(98);
        temp.combatDamageType = (byte)3;
        temp.hasHands = false;
        temp.setMaxAge(40);
        temp.offZ = -1.4f;
        temp.setMaxPercentOfCreatures(0.02f);
        temp.setMaxGroupAttackSize(4);
        temp.setUsesNewAttacks(true);
        temp.setBoundsValues(-0.975f, -0.9f, 0.975f, 0.9f);
        temp.addPrimaryAttack(new AttackAction("bite", AttackIdentifier.BITE, new AttackValues(8.0f, 0.04f, 5.0f, 2, 1, 3, false, 3, 1.0f)));
        temp.addPrimaryAttack(new AttackAction("strike", AttackIdentifier.STRIKE, new AttackValues(5.0f, 0.04f, 6.0f, 2, 1, 1, false, 2, 1.0f)));
        temp.addSecondaryAttack(new AttackAction("bite", AttackIdentifier.BITE, new AttackValues(7.0f, 0.1f, 7.0f, 2, 1, 3, false, 4, 1.1f)));
        temp.addSecondaryAttack(new AttackAction("headbutt", AttackIdentifier.HEADBUTT, new AttackValues(7.0f, 0.1f, 7.0f, 2, 1, 0, false, 4, 1.1f)));
        if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
            temp.setVision((short)4);
        }
    }

    private static void createSealCubTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 10.0f);
        skills.learnTemp(104, 15.0f);
        skills.learnTemp(103, 15.0f);
        skills.learnTemp(100, 6.0f);
        skills.learnTemp(101, 8.0f);
        skills.learnTemp(105, 10.0f);
        skills.learnTemp(106, 15.0f);
        skills.learnTemp(10052, 15.0f);
        int[] types = new int[]{8, 12, 35, 3, 29, 44, 48, 51, 32, 63};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.fish.seal.cub", types, (byte)9, skills, (short)3, (byte)0, (short)80, (short)90, (short)70, "sound.death.snake", "sound.death.snake", "sound.combat.hit.snake", "sound.combat.hit.snake", 0.7f, 0.0f, 0.0f, 8.0f, 5.0f, 0.0f, 0.5f, 100, new int[]{140, 140, 71, 71, 310}, 40, 59, (byte)85);
        temp.setAlignment(-20.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CHAIN);
        temp.setBaseCombatRating(1.0f);
        temp.setAdultFemaleTemplateId(93);
        temp.setAdultMaleTemplateId(93);
        temp.combatDamageType = (byte)3;
        temp.hasHands = false;
        temp.setMaxAge(20);
        temp.setMaxGroupAttackSize(2);
        temp.offZ = -1.4f;
        if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
            temp.setVision((short)3);
        }
    }

    private static void createSeaSerpentTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 40.0f);
        skills.learnTemp(104, 55.0f);
        skills.learnTemp(103, 60.0f);
        skills.learnTemp(100, 24.0f);
        skills.learnTemp(101, 10.0f);
        skills.learnTemp(105, 80.0f);
        skills.learnTemp(106, 5.0f);
        skills.learnTemp(10052, 85.0f);
        skills.learnTemp(1023, 80.0f);
        int[] types = new int[]{8, 38, 6, 13, 16, 29, 44, 40, 37, 61};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.snake.serpent.sea", types, (byte)9, skills, (short)20, (byte)0, (short)100, (short)1000, (short)100, "sound.death.snake", "sound.death.snake", "sound.combat.hit.snake", "sound.combat.hit.snake", 0.05f, 0.0f, 0.0f, 56.0f, 30.0f, 0.0f, 2.0f, 50, new int[]{308, 308, 310}, 40, 59, (byte)85);
        temp.setAlignment(-20.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CHAIN);
        temp.setBaseCombatRating(76.0f);
        temp.setMaxGroupAttackSize(25);
        temp.combatDamageType = 0;
        temp.hasHands = false;
        temp.setMaxAge(400);
        temp.setSizeModX(200);
        temp.setSizeModY(200);
        temp.setSizeModZ(200);
        temp.setMaxPopulationOfCreatures(4);
        temp.offZ = -5.0f;
    }

    private static void createAvengerOfLightTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 40.0f);
        skills.learnTemp(104, 55.0f);
        skills.learnTemp(103, 60.0f);
        skills.learnTemp(100, 24.0f);
        skills.learnTemp(101, 30.0f);
        skills.learnTemp(105, 80.0f);
        skills.learnTemp(106, 39.0f);
        skills.learnTemp(10052, 75.0f);
        int[] types = new int[]{8, 13, 16, 27, 36, 12, 24, 45, 50, 62, 65};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.avenger.light", types, (byte)0, skills, (short)20, (byte)0, (short)370, (short)100, (short)60, "sound.death.giant", "sound.death.giant", "sound.combat.hit.giant", "sound.combat.hit.giant", 0.1f, 10.0f, 24.0f, 26.0f, 0.0f, 0.0f, 1.8f, 100, new int[]{308, 308, 310}, 40, 20, (byte)81);
        temp.setAlignment(-20.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CHAIN);
        temp.setBaseCombatRating(76.0f);
        temp.setMaxGroupAttackSize(25);
        temp.combatDamageType = 0;
        temp.setCombatMoves(new int[]{1, 7});
        temp.hasHands = true;
    }

    private static void createBoarFoTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 30.0f);
        skills.learnTemp(104, 35.0f);
        skills.learnTemp(103, 40.0f);
        skills.learnTemp(100, 2.0f);
        skills.learnTemp(101, 8.0f);
        skills.learnTemp(105, 34.0f);
        skills.learnTemp(106, 3.0f);
        skills.learnTemp(10052, 40.0f);
        int[] types = new int[]{7, 41, 24, 13, 3, 27, 36, 39};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.boar.wild", types, (byte)3, skills, (short)10, (byte)0, (short)50, (short)50, (short)150, "sound.death.pig", "sound.death.pig", "sound.combat.hit.pig", "sound.combat.hit.pig", 0.6f, 6.0f, 0.0f, 7.0f, 10.0f, 0.0f, 1.2f, 300, new int[]{92, 140, 303}, 10, 94, (byte)84);
        temp.setHandDamString("kick");
        temp.setAlignment(10.0f);
        temp.setMaxAge(5);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CLOTH);
        temp.setBaseCombatRating(14.0f);
        temp.combatDamageType = 0;
        temp.setMaxGroupAttackSize(4);
        temp.setMaxPercentOfCreatures(0.01f);
    }

    private static void createGorillaMagranonTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 40.0f);
        skills.learnTemp(104, 25.0f);
        skills.learnTemp(103, 40.0f);
        skills.learnTemp(100, 8.0f);
        skills.learnTemp(101, 10.0f);
        skills.learnTemp(105, 30.0f);
        skills.learnTemp(106, 7.0f);
        skills.learnTemp(10052, 40.0f);
        int[] types = new int[]{7, 24, 13, 3, 30, 27, 36, 39, 45};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.gorilla.mountain", types, (byte)0, skills, (short)10, (byte)0, (short)210, (short)50, (short)50, "sound.death.gorilla", "sound.death.gorilla", "sound.combat.hit.gorilla", "sound.combat.hit.gorilla", 0.6f, 6.0f, 0.0f, 10.0f, 0.0f, 0.0f, 1.2f, 300, new int[]{303, 308, 308}, 10, 94, (byte)78);
        temp.setHandDamString("claw");
        temp.setAlignment(10.0f);
        temp.setMaxAge(10);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CLOTH);
        temp.setBaseCombatRating(14.0f);
        temp.combatDamageType = 0;
        temp.setMaxGroupAttackSize(6);
        temp.hasHands = true;
        temp.setMaxPercentOfCreatures(0.01f);
    }

    private static void createGuardKingdomTowerTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 20.0f);
        skills.learnTemp(104, 17.0f);
        skills.learnTemp(103, 21.0f);
        skills.learnTemp(100, 15.0f);
        skills.learnTemp(101, 15.0f);
        skills.learnTemp(105, 15.0f);
        skills.learnTemp(106, 17.0f);
        if (id == 34) {
            skills.learnTemp(10005, 45.0f);
        } else if (id == 35) {
            skills.learnTemp(10024, 45.0f);
        } else if (id == 36) {
            skills.learnTemp(10061, 45.0f);
        } else if (id == 67) {
            skills.learnTemp(10028, 45.0f);
        }
        skills.learnTemp(10020, 45.0f);
        skills.learnTemp(10052, 45.0f);
        String modelname = "model.creature.humanoid.human.guard.tower";
        int[] types = new int[]{21, 12, 13, 17, 45};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.human.guard.tower", types, (byte)0, skills, (short)10, (byte)0, (short)180, (short)20, (short)35, "sound.death.male", "sound.death.female", "sound.combat.hit.male", "sound.combat.hit.female", 0.3f, Servers.localServer.isChallengeServer() ? 6.0f : 4.0f, 7.0f, 0.0f, 0.0f, 0.0f, 1.0f, 200, new int[0], 30, 80, (byte)80);
        int cr = Servers.localServer.isChallengeServer() ? 12 : 6;
        temp.setBaseCombatRating(cr);
        temp.setMaxAge(20);
        temp.combatDamageType = 1;
        temp.setMaxGroupAttackSize(6);
        temp.setNoSkillgain(true);
        temp.hasHands = true;
    }

    private static void createDummyDollTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 20.0f);
        skills.learnTemp(104, 20.0f);
        skills.learnTemp(103, 60.0f);
        skills.learnTemp(100, 20.0f);
        skills.learnTemp(101, 20.0f);
        skills.learnTemp(105, 20.0f);
        skills.learnTemp(106, 20.0f);
        skills.learnTemp(10052, 10.0f);
        int[] types = new int[]{12, 13, 17};
        CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.human.player", types, (byte)0, skills, (short)5, (byte)0, (short)180, (short)20, (short)35, "sound.death.male", "sound.death.female", "sound.combat.hit.male", "sound.combat.hit.female", 0.2f, 2.0f, 3.0f, 0.0f, 0.0f, 0.0f, 0.5f, 0, new int[0], 5, 0, (byte)87);
    }

    private static void createCatWildTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 8.0f);
        skills.learnTemp(104, 13.0f);
        skills.learnTemp(103, 10.0f);
        skills.learnTemp(100, 6.0f);
        skills.learnTemp(101, 8.0f);
        skills.learnTemp(105, 20.0f);
        skills.learnTemp(106, 1.0f);
        skills.learnTemp(10052, 3.0f);
        int[] types = new int[]{7, 3, 6, 32, 29, 60, 61};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.cat.wild", types, (byte)3, skills, (short)3, (byte)0, (short)20, (short)10, (short)300, "sound.death.cat", "sound.death.cat", "sound.combat.hit.cat", "sound.combat.hit.cat", 1.0f, 1.0f, 0.0f, 3.0f, 0.0f, 0.0f, 0.7f, 500, new int[]{313}, 5, 10, (byte)75);
        temp.setHandDamString("claw");
        temp.setKickDamString("claw");
        temp.setMaxAge(40);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CLOTH);
        temp.setBaseCombatRating(2.0f);
        temp.combatDamageType = 1;
        temp.setMaxGroupAttackSize(2);
        temp.setDenName("wildcat hideout");
        temp.setDenMaterial((byte)15);
        temp.setMaxPercentOfCreatures(0.02f);
    }

    private static void createCaveBugTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 10.0f);
        skills.learnTemp(104, 13.0f);
        skills.learnTemp(103, 5.0f);
        skills.learnTemp(100, 7.0f);
        skills.learnTemp(101, 8.0f);
        skills.learnTemp(105, 40.0f);
        skills.learnTemp(106, 4.0f);
        skills.learnTemp(10052, 16.0f);
        int[] types = new int[]{7, 3, 6, 13, 27, 32, 34, 60, 61};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.insect.cavebug", types, (byte)3, skills, (short)5, (byte)0, (short)30, (short)30, (short)30, "sound.death.insect", "sound.death.insect", "sound.combat.hit.insect", "sound.combat.hit.insect", 0.9f, 3.0f, 0.0f, 6.0f, 0.0f, 0.0f, 0.6f, 50, new int[]{439, 439, 439, 439, 439, 439, 439, 439, 439}, 10, 30, (byte)82);
        temp.setHandDamString("claw");
        temp.setKickDamString("claw");
        temp.setMaxAge(100);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_PLATE);
        temp.setBaseCombatRating(5.0f);
        temp.combatDamageType = 1;
        temp.setMaxGroupAttackSize(2);
        temp.setDenName("cave bug mound");
        temp.setDenMaterial((byte)15);
        temp.setSubterranean(true);
        temp.setMaxPercentOfCreatures(0.03f);
    }

    private static void createTortoiseTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 22.0f);
        skills.learnTemp(104, 6.0f);
        skills.learnTemp(103, 5.0f);
        skills.learnTemp(100, 14.0f);
        skills.learnTemp(101, 6.0f);
        skills.learnTemp(105, 30.0f);
        skills.learnTemp(106, 24.0f);
        skills.learnTemp(10052, 36.0f);
        int[] types = new int[]{7, 3, 6, 35, 44, 28, 32, 51, 48, 12, 60, 61};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.tortoise", types, (byte)3, skills, (short)5, (byte)0, (short)30, (short)30, (short)30, "sound.death.insect", "sound.death.insect", "sound.combat.hit.insect", "sound.combat.hit.insect", 0.3f, 0.0f, 0.0f, 12.0f, 0.0f, 0.0f, 0.3f, 50, new int[]{898, 308, 308, 92}, 10, 10, (byte)85);
        temp.setMaxAge(100);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_PLATE);
        temp.setBaseCombatRating(12.0f);
        temp.setMaxGroupAttackSize(3);
        temp.setMaxPercentOfCreatures(0.02f);
        if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
            temp.setVision((short)6);
        }
    }

    private static void createCrabTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 12.0f);
        skills.learnTemp(104, 16.0f);
        skills.learnTemp(103, 5.0f);
        skills.learnTemp(100, 4.0f);
        skills.learnTemp(101, 6.0f);
        skills.learnTemp(105, 20.0f);
        skills.learnTemp(106, 4.0f);
        skills.learnTemp(10052, 16.0f);
        int[] types = new int[]{7, 3, 6, 13, 27, 32, 51, 48, 12, 60, 61};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.crab", types, (byte)3, skills, (short)5, (byte)0, (short)30, (short)30, (short)30, "sound.death.insect", "sound.death.insect", "sound.combat.hit.insect", "sound.combat.hit.insect", 0.7f, 5.0f, 0.0f, 6.0f, 0.0f, 0.0f, 0.8f, 50, new int[]{900, 900, 308, 308, 900}, 10, 30, (byte)85);
        temp.setHandDamString("claw");
        temp.setKickDamString("claw");
        temp.setMaxAge(100);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_PLATE);
        temp.setBaseCombatRating(5.0f);
        temp.combatDamageType = 1;
        temp.setMaxGroupAttackSize(3);
        temp.setMaxPercentOfCreatures(0.03f);
    }

    private static void createLionMountainTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 15.0f);
        skills.learnTemp(104, 3.0f);
        skills.learnTemp(103, 15.0f);
        skills.learnTemp(100, 7.0f);
        skills.learnTemp(101, 8.0f);
        skills.learnTemp(105, 25.0f);
        skills.learnTemp(106, 4.0f);
        skills.learnTemp(10052, 6.0f);
        int[] types = new int[]{7, 3, 6, 13, 30, 32, 29, 60, 61};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.lion.mountain", types, (byte)3, skills, (short)5, (byte)0, (short)60, (short)30, (short)90, "sound.death.lion", "sound.death.lion", "sound.combat.hit.lion", "sound.combat.hit.lion", 0.95f, 3.0f, 0.0f, 5.0f, 0.0f, 0.0f, 1.0f, 1200, new int[]{92, 305, 313, 308, 308}, 10, 40, (byte)75);
        temp.setHandDamString("claw");
        temp.setKickDamString("claw");
        temp.setMaxAge(100);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CLOTH);
        temp.setBaseCombatRating(3.0f);
        temp.combatDamageType = 1;
        temp.setMaxGroupAttackSize(2);
        temp.setDenName("mountain lion hideout");
        temp.setDenMaterial((byte)15);
        temp.setMaxPercentOfCreatures(0.01f);
    }

    private static void createRatLargeTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 5.0f);
        skills.learnTemp(104, 10.0f);
        skills.learnTemp(103, 10.0f);
        skills.learnTemp(100, 6.0f);
        skills.learnTemp(101, 8.0f);
        skills.learnTemp(105, 15.0f);
        skills.learnTemp(106, 1.0f);
        skills.learnTemp(10052, 2.0f);
        int[] types = new int[]{7, 3, 6, 12, 13, 27, 32, 60, 61};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.rat.large", types, (byte)3, skills, (short)3, (byte)0, (short)20, (short)10, (short)50, "sound.death.rat", "sound.death.rat", "sound.combat.hit.rat", "sound.combat.hit.rat", 1.0f, 1.0f, 0.0f, 2.0f, 0.0f, 0.0f, 0.7f, 400, new int[]{313, 310, 308, 308}, 10, 40, (byte)78);
        temp.setHandDamString("claw");
        temp.setMaxAge(30);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CLOTH);
        temp.setBaseCombatRating(2.0f);
        temp.combatDamageType = (byte)2;
        temp.setMaxGroupAttackSize(2);
        temp.setDenName("garbage pile");
        temp.setSubterranean(true);
        temp.setMaxPercentOfCreatures(0.03f);
    }

    private static void createDragonRedTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 80.0f);
        skills.learnTemp(104, 90.0f);
        skills.learnTemp(103, 99.0f);
        skills.learnTemp(100, 60.0f);
        skills.learnTemp(101, 57.0f);
        skills.learnTemp(105, 60.0f);
        skills.learnTemp(106, 30.0f);
        skills.learnTemp(10052, 95.0f);
        skills.learnTemp(1023, 80.0f);
        int[] types = new int[]{20, 19, 41, 7, 6, 13, 16, 32, 29, 12, 40};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.dragon.red", types, (byte)6, skills, (short)20, (byte)0, (short)280, (short)210, (short)666, "sound.death.dragon", "sound.death.dragon", "sound.combat.hit.dragon", "sound.combat.hit.dragon", 0.017f, 35.0f, 38.0f, 53.0f, 67.0f, 0.0f, 1.6f, 500, new int[]{867, 868, 303, 308, 308, 310}, 40, 99, (byte)76);
        temp.setHeadbuttDamString("tailwhip");
        temp.setKickDamString("wingbuff");
        temp.setAlignment(-90.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_SCALE_DRAGON);
        temp.setEggLayer(true);
        temp.setEggTemplateId(16);
        temp.setBaseCombatRating(100.0f);
        temp.combatDamageType = (byte)2;
        temp.setCombatMoves(new int[]{1, 2, 3});
    }

    private static void createDragonBlueTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 80.0f);
        skills.learnTemp(104, 90.0f);
        skills.learnTemp(103, 95.0f);
        skills.learnTemp(100, 56.0f);
        skills.learnTemp(101, 57.0f);
        skills.learnTemp(105, 60.0f);
        skills.learnTemp(106, 30.0f);
        skills.learnTemp(10052, 90.0f);
        skills.learnTemp(1023, 80.0f);
        int[] types = new int[]{20, 19, 41, 7, 6, 13, 16, 32, 29, 12, 40};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.dragon.blue", types, (byte)6, skills, (short)20, (byte)0, (short)280, (short)210, (short)666, "sound.death.dragon", "sound.death.dragon", "sound.combat.hit.dragon", "sound.combat.hit.dragon", 0.1f, 35.0f, 38.0f, 50.0f, 63.0f, 0.0f, 1.6f, 500, new int[]{867, 868, 303, 308, 308, 310}, 40, 99, (byte)76);
        temp.setHeadbuttDamString("tailwhip");
        temp.setKickDamString("wingbuff");
        temp.setAlignment(-90.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_SCALE_DRAGON);
        temp.setEggLayer(true);
        temp.setEggTemplateId(91);
        temp.setBaseCombatRating(100.0f);
        temp.combatDamageType = (byte)2;
        temp.setCombatMoves(new int[]{1, 2, 3});
    }

    private static void createDragonGreenTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 84.0f);
        skills.learnTemp(104, 90.0f);
        skills.learnTemp(103, 90.0f);
        skills.learnTemp(100, 56.0f);
        skills.learnTemp(101, 57.0f);
        skills.learnTemp(105, 60.0f);
        skills.learnTemp(106, 30.0f);
        skills.learnTemp(10052, 90.0f);
        skills.learnTemp(1023, 80.0f);
        int[] types = new int[]{20, 19, 41, 7, 6, 13, 16, 32, 29, 12, 40};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.dragon.green", types, (byte)6, skills, (short)20, (byte)0, (short)280, (short)210, (short)666, "sound.death.dragon", "sound.death.dragon", "sound.combat.hit.dragon", "sound.combat.hit.dragon", 0.1f, 35.0f, 38.0f, 50.0f, 58.0f, 0.0f, 1.6f, 500, new int[]{867, 868, 303, 308, 308, 310}, 40, 99, (byte)76);
        temp.setHeadbuttDamString("tailwhip");
        temp.setKickDamString("wingbuff");
        temp.setAlignment(-90.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_SCALE_DRAGON);
        temp.setEggLayer(true);
        temp.setEggTemplateId(90);
        temp.setBaseCombatRating(100.0f);
        temp.combatDamageType = (byte)2;
        temp.setCombatMoves(new int[]{1, 2, 3});
    }

    private static void createDragonBlackTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 80.0f);
        skills.learnTemp(104, 90.0f);
        skills.learnTemp(103, 90.0f);
        skills.learnTemp(100, 56.0f);
        skills.learnTemp(101, 57.0f);
        skills.learnTemp(105, 70.0f);
        skills.learnTemp(106, 30.0f);
        skills.learnTemp(10052, 90.0f);
        skills.learnTemp(1023, 80.0f);
        int[] types = new int[]{20, 19, 41, 7, 6, 13, 16, 32, 29, 12, 40};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.dragon.black", types, (byte)6, skills, (short)20, (byte)0, (short)280, (short)210, (short)666, "sound.death.dragon", "sound.death.dragon", "sound.combat.hit.dragon", "sound.combat.hit.dragon", 0.2f, 35.0f, 38.0f, 58.0f, 62.0f, 0.0f, 1.6f, 500, new int[]{867, 868, 303, 308, 308, 310}, 40, 99, (byte)76);
        temp.setHeadbuttDamString("tailwhip");
        temp.setKickDamString("wingbuff");
        temp.setAlignment(-90.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_SCALE_DRAGON);
        temp.setEggLayer(true);
        temp.setEggTemplateId(89);
        temp.setBaseCombatRating(100.0f);
        temp.combatDamageType = (byte)2;
        temp.setCombatMoves(new int[]{1, 2, 3});
    }

    private static void createDragonWhiteTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 80.0f);
        skills.learnTemp(104, 90.0f);
        skills.learnTemp(103, 85.0f);
        skills.learnTemp(100, 56.0f);
        skills.learnTemp(101, 57.0f);
        skills.learnTemp(105, 70.0f);
        skills.learnTemp(106, 30.0f);
        skills.learnTemp(10052, 90.0f);
        skills.learnTemp(1023, 80.0f);
        int[] types = new int[]{20, 19, 41, 7, 6, 13, 16, 32, 29, 12, 40};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.dragon.white", types, (byte)6, skills, (short)20, (byte)0, (short)280, (short)210, (short)666, "sound.death.dragon", "sound.death.dragon", "sound.combat.hit.dragon", "sound.combat.hit.dragon", 0.1f, 35.0f, 38.0f, 55.0f, 60.0f, 0.0f, 1.6f, 500, new int[]{867, 868, 303, 308, 308, 310}, 40, 99, (byte)76);
        temp.setHeadbuttDamString("tailwhip");
        temp.setKickDamString("wingbuff");
        temp.setAlignment(-90.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_SCALE_DRAGON);
        temp.setEggLayer(true);
        temp.setEggTemplateId(92);
        temp.setBaseCombatRating(100.0f);
        temp.combatDamageType = (byte)2;
        temp.setCombatMoves(new int[]{1, 2, 3});
    }

    private static void createDrakeRedTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 50.0f);
        skills.learnTemp(104, 65.0f);
        skills.learnTemp(103, 70.0f);
        skills.learnTemp(100, 27.0f);
        skills.learnTemp(101, 40.0f);
        skills.learnTemp(105, 40.0f);
        skills.learnTemp(106, 29.0f);
        skills.learnTemp(10052, 80.0f);
        skills.learnTemp(1023, 80.0f);
        int[] types = new int[]{20, 19, 41, 7, 12, 6, 13, 16, 32, 29, 40};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.drake.red", types, (byte)6, skills, (short)20, (byte)1, (short)170, (short)100, (short)450, "sound.death.dragon", "sound.death.dragon", "sound.combat.hit.dragon", "sound.combat.hit.dragon", 0.15f, 20.0f, 26.0f, 52.0f, 54.0f, 0.0f, 1.8f, 500, new int[]{867, 868, 303, 308, 308, 310}, 40, 99, (byte)76);
        temp.setHeadbuttDamString("tailwhip");
        temp.setKickDamString("wingbuff");
        temp.setAlignment(-80.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_LEATHER_DRAGON);
        temp.setEggLayer(true);
        temp.setEggTemplateId(103);
        temp.setBaseCombatRating(95.0f);
        temp.combatDamageType = (byte)2;
        temp.setCombatMoves(new int[]{1, 2, 3});
    }

    private static void createDrakeBlueTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 55.0f);
        skills.learnTemp(104, 75.0f);
        skills.learnTemp(103, 65.0f);
        skills.learnTemp(100, 27.0f);
        skills.learnTemp(101, 34.0f);
        skills.learnTemp(105, 60.0f);
        skills.learnTemp(106, 29.0f);
        skills.learnTemp(10052, 80.0f);
        skills.learnTemp(1023, 80.0f);
        int[] types = new int[]{20, 19, 41, 7, 12, 6, 13, 16, 32, 29, 40};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.drake.blue", types, (byte)6, skills, (short)20, (byte)1, (short)170, (short)100, (short)450, "sound.death.dragon", "sound.death.dragon", "sound.combat.hit.dragon", "sound.combat.hit.dragon", 0.15f, 20.0f, 26.0f, 50.0f, 58.0f, 0.0f, 1.8f, 500, new int[]{867, 868, 303, 308, 308, 310}, 40, 99, (byte)76);
        temp.setHeadbuttDamString("tailwhip");
        temp.setKickDamString("wingbuff");
        temp.setAlignment(-80.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_LEATHER_DRAGON);
        temp.setEggLayer(true);
        temp.setEggTemplateId(104);
        temp.setBaseCombatRating(95.0f);
        temp.combatDamageType = (byte)2;
        temp.setCombatMoves(new int[]{1, 2});
    }

    private static void createDrakeWhiteTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 40.0f);
        skills.learnTemp(104, 55.0f);
        skills.learnTemp(103, 60.0f);
        skills.learnTemp(100, 24.0f);
        skills.learnTemp(101, 30.0f);
        skills.learnTemp(105, 35.0f);
        skills.learnTemp(106, 39.0f);
        skills.learnTemp(10052, 75.0f);
        skills.learnTemp(1023, 80.0f);
        int[] types = new int[]{20, 19, 41, 7, 6, 13, 16, 32, 29, 12, 40};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.drake.white", types, (byte)6, skills, (short)30, (byte)1, (short)150, (short)90, (short)420, "sound.death.dragon", "sound.death.dragon", "sound.combat.hit.dragon", "sound.combat.hit.dragon", 0.2f, 21.0f, 24.0f, 53.0f, 57.0f, 45.0f, 1.9f, 500, new int[]{867, 868, 303, 308, 308, 310}, 40, 99, (byte)76);
        temp.setHeadbuttDamString("tailwhip");
        temp.setKickDamString("wingbuff");
        temp.setBreathDamString("burn");
        temp.setAlignment(-60.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_LEATHER_DRAGON);
        temp.setEggLayer(true);
        temp.setEggTemplateId(19);
        temp.setBaseCombatRating(95.0f);
        temp.combatDamageType = (byte)2;
        temp.setCombatMoves(new int[]{1, 2});
    }

    private static void createDrakeGreenTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 60.0f);
        skills.learnTemp(104, 65.0f);
        skills.learnTemp(103, 80.0f);
        skills.learnTemp(100, 17.0f);
        skills.learnTemp(101, 27.0f);
        skills.learnTemp(105, 50.0f);
        skills.learnTemp(106, 24.0f);
        skills.learnTemp(10052, 80.0f);
        skills.learnTemp(1023, 80.0f);
        int[] types = new int[]{20, 19, 41, 7, 6, 13, 16, 32, 29, 12, 40};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.drake.green", types, (byte)6, skills, (short)20, (byte)0, (short)180, (short)110, (short)480, "sound.death.dragon", "sound.death.dragon", "sound.combat.hit.dragon", "sound.combat.hit.dragon", 0.15f, 20.0f, 24.0f, 55.0f, 56.0f, 0.0f, 1.6f, 500, new int[]{867, 868, 303, 308, 308, 310}, 40, 99, (byte)76);
        temp.setHeadbuttDamString("tailwhip");
        temp.setKickDamString("wingbuff");
        temp.setAlignment(-60.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_LEATHER_DRAGON);
        temp.setEggLayer(true);
        temp.setEggTemplateId(17);
        temp.setBaseCombatRating(96.0f);
        temp.combatDamageType = (byte)2;
        temp.setCombatMoves(new int[]{1, 2, 5});
    }

    private static void createDrakeBlackTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 50.0f);
        skills.learnTemp(104, 75.0f);
        skills.learnTemp(103, 70.0f);
        skills.learnTemp(100, 27.0f);
        skills.learnTemp(101, 37.0f);
        skills.learnTemp(105, 55.0f);
        skills.learnTemp(106, 29.0f);
        skills.learnTemp(10052, 85.0f);
        skills.learnTemp(1023, 80.0f);
        int[] types = new int[]{20, 19, 41, 7, 12, 6, 13, 16, 32, 29, 40};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.drake.black", types, (byte)6, skills, (short)20, (byte)1, (short)170, (short)100, (short)450, "sound.death.dragon", "sound.death.dragon", "sound.combat.hit.dragon", "sound.combat.hit.dragon", 0.12f, 20.0f, 26.0f, 56.0f, 58.0f, 0.0f, 1.8f, 500, new int[]{867, 868, 303, 308, 308, 310}, 40, 99, (byte)76);
        temp.setHeadbuttDamString("tailwhip");
        temp.setKickDamString("wingbuff");
        temp.setAlignment(-70.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_LEATHER_DRAGON);
        temp.setEggLayer(true);
        temp.setEggTemplateId(18);
        temp.setBaseCombatRating(98.0f);
        temp.combatDamageType = (byte)2;
        temp.setCombatMoves(new int[]{1, 2, 6});
    }

    private static void createForestGiantTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 40.0f);
        skills.learnTemp(104, 25.0f);
        skills.learnTemp(103, 60.0f);
        skills.learnTemp(100, 5.0f);
        skills.learnTemp(101, 5.0f);
        skills.learnTemp(105, 60.0f);
        skills.learnTemp(106, 19.0f);
        skills.learnTemp(10052, 65.0f);
        skills.learnTemp(1023, 80.0f);
        skills.learnTemp(10064, 60.0f);
        int[] types = new int[]{20, 7, 6, 13, 16, 27, 32, 40, 45};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.giant.forest", types, (byte)0, skills, (short)20, (byte)0, (short)370, (short)100, (short)60, "sound.death.giant", "sound.death.giant", "sound.combat.hit.giant", "sound.combat.hit.giant", 0.02f, 10.0f, 24.0f, 26.0f, 0.0f, 0.0f, 1.5f, 1800, new int[]{308, 308, 310, 868, 867}, 40, 99, (byte)87);
        temp.setAlignment(-20.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_STUDDED);
        temp.setBaseCombatRating(76.0f);
        temp.combatDamageType = 0;
        temp.setCombatMoves(new int[]{1, 5, 6});
        temp.hasHands = true;
    }

    private static void createCyclopsTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 80.0f);
        skills.learnTemp(104, 35.0f);
        skills.learnTemp(103, 80.0f);
        skills.learnTemp(100, 7.0f);
        skills.learnTemp(101, 7.0f);
        skills.learnTemp(105, 55.0f);
        skills.learnTemp(106, 29.0f);
        skills.learnTemp(10052, 80.0f);
        skills.learnTemp(10064, 90.0f);
        int[] types = new int[]{30, 20, 7, 6, 13, 16, 27, 32, 40, 45};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.kyklops", types, (byte)5, skills, (short)20, (byte)0, (short)570, (short)200, (short)80, "sound.death.giant", "sound.death.giant", "sound.combat.hit.giant", "sound.combat.hit.giant", 0.015f, 26.0f, 30.0f, 0.0f, 0.0f, 0.0f, 1.8f, 1800, new int[]{308, 310, 868, 867}, 40, 99, (byte)81);
        temp.setAlignment(-10.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_STUDDED);
        temp.keepSex = true;
        temp.setBaseCombatRating(86.0f);
        temp.combatDamageType = 0;
        temp.setCombatMoves(new int[]{4, 1, 6});
        temp.hasHands = true;
    }

    private static void createTrollKingTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 70.0f);
        skills.learnTemp(104, 45.0f);
        skills.learnTemp(103, 70.0f);
        skills.learnTemp(100, 15.0f);
        skills.learnTemp(101, 20.0f);
        skills.learnTemp(105, 45.0f);
        skills.learnTemp(106, 29.0f);
        skills.learnTemp(1023, 80.0f);
        skills.learnTemp(10052, 80.0f);
        skills.learnTemp(10064, 90.0f);
        int[] types = new int[]{20, 7, 6, 13, 16, 18, 29, 30, 32, 40, 45};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.troll.king", types, (byte)0, skills, (short)20, (byte)0, (short)270, (short)60, (short)60, "sound.death.troll", "sound.death.troll", "sound.combat.hit.troll", "sound.combat.hit.troll", 0.02f, 20.0f, 0.0f, 26.0f, 0.0f, 0.0f, 1.7f, 200, new int[]{303, 310, 868, 867}, 40, 99, (byte)81);
        temp.setHandDamString("claw");
        temp.setAlignment(-60.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_RING);
        temp.keepSex = true;
        temp.setBaseCombatRating(86.0f);
        temp.combatDamageType = (byte)2;
        temp.setCombatMoves(new int[]{4, 1});
        temp.hasHands = true;
    }

    private static void createTrollTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 40.0f);
        skills.learnTemp(104, 25.0f);
        skills.learnTemp(103, 40.0f);
        skills.learnTemp(100, 8.0f);
        skills.learnTemp(101, 10.0f);
        skills.learnTemp(105, 39.0f);
        skills.learnTemp(106, 7.0f);
        skills.learnTemp(10052, 40.0f);
        skills.learnTemp(10064, 70.0f);
        int[] types = new int[]{7, 6, 40, 13, 16, 18, 29, 30, 32, 36, 39, 45, 60, 61};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.troll.standard", types, (byte)0, skills, (short)5, (byte)0, (short)230, (short)50, (short)50, "sound.death.troll", "sound.death.troll", "sound.combat.hit.troll", "sound.combat.hit.troll", 0.4f, 8.0f, 4.0f, 12.0f, 0.0f, 0.0f, 1.2f, 1700, new int[]{303, 310}, 10, 94, (byte)81);
        temp.setHandDamString("claw");
        temp.setLeaderTemplateId(27);
        temp.setAlignment(-50.0f);
        temp.setMaxAge(300);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_STUDDED);
        temp.setBaseCombatRating(12.0f);
        temp.setBonusCombatRating(5.0f);
        temp.combatDamageType = (byte)2;
        temp.setMaxGroupAttackSize(8);
        temp.setDenName("troll mound");
        temp.setDenMaterial((byte)15);
        temp.setSubterranean(true);
        temp.hasHands = true;
        temp.setMaxPercentOfCreatures(0.06f);
        temp.setUsesNewAttacks(true);
        temp.setBoundsValues(-0.5f, -0.5f, 0.5f, 0.5f);
        temp.addPrimaryAttack(new AttackAction("maul", AttackIdentifier.MAUL, new AttackValues(7.0f, 0.04f, 6.0f, 3, 2, 0, true, 3, 1.4f)));
        temp.addPrimaryAttack(new AttackAction("strike", AttackIdentifier.STRIKE, new AttackValues(7.0f, 0.04f, 4.0f, 3, 1, 0, false, 3, 1.4f)));
        temp.addSecondaryAttack(new AttackAction("kick", AttackIdentifier.KICK, new AttackValues(4.0f, 0.04f, 5.0f, 3, 1, 0, false, 3, 2.1f)));
        temp.addSecondaryAttack(new AttackAction("bite", AttackIdentifier.BITE, new AttackValues(10.0f, 0.08f, 7.0f, 3, 1, 3, false, 3, 2.0f)));
        temp.addSecondaryAttack(new AttackAction("kick", AttackIdentifier.CLAW, new AttackValues(5.0f, 0.1f, 7.0f, 3, 1, 1, false, 3, 1.8f)));
    }

    private static void createGoblinLeaderTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 30.0f);
        skills.learnTemp(104, 25.0f);
        skills.learnTemp(103, 50.0f);
        skills.learnTemp(100, 19.0f);
        skills.learnTemp(101, 25.0f);
        skills.learnTemp(105, 60.0f);
        skills.learnTemp(106, 19.0f);
        skills.learnTemp(10052, 60.0f);
        skills.learnTemp(10027, 90.0f);
        skills.learnTemp(10006, 90.0f);
        skills.learnTemp(1023, 80.0f);
        int[] types = new int[]{20, 7, 6, 13, 16, 29, 30, 32, 40, 45};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.goblin.leader", types, (byte)0, skills, (short)20, (byte)0, (short)150, (short)30, (short)20, "sound.death.goblin", "sound.death.goblin", "sound.combat.hit.goblin", "sound.combat.hit.goblin", 0.14f, 18.0f, 25.0f, 0.0f, 0.0f, 0.0f, 1.5f, 1200, new int[]{303, 868, 867}, 40, 99, (byte)81);
        temp.setHandDamString("claw");
        temp.setAlignment(-50.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CHAIN);
        temp.keepSex = true;
        temp.setBaseCombatRating(68.0f);
        temp.combatDamageType = (byte)2;
        temp.setCombatMoves(new int[]{4});
        temp.hasHands = true;
    }

    private static void createGoblinTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 20.0f);
        skills.learnTemp(104, 15.0f);
        skills.learnTemp(103, 20.0f);
        skills.learnTemp(100, 12.0f);
        skills.learnTemp(101, 15.0f);
        skills.learnTemp(105, 26.0f);
        skills.learnTemp(106, 7.0f);
        skills.learnTemp(10052, 14.0f);
        int[] types = new int[]{7, 6, 13, 16, 29, 30, 32, 34, 45, 60, 61};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.goblin.standard", types, (byte)0, skills, (short)5, (byte)0, (short)130, (short)30, (short)20, "sound.death.goblin", "sound.death.goblin", "sound.combat.hit.goblin", "sound.combat.hit.goblin", 0.7f, 3.0f, 5.0f, 0.0f, 0.0f, 0.0f, 0.7f, 1500, new int[]{1250}, 10, 94, (byte)81);
        temp.setHandDamString("claw");
        temp.setLeaderTemplateId(26);
        temp.setAlignment(-40.0f);
        temp.setMaxAge(100);
        temp.setBaseCombatRating(6.0f);
        temp.combatDamageType = (byte)2;
        temp.setMaxGroupAttackSize(2);
        temp.setDenName("goblin hut");
        temp.hasHands = true;
        temp.setMaxPercentOfCreatures(0.06f);
    }

    private static void createScorpionTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 20.0f);
        skills.learnTemp(104, 15.0f);
        skills.learnTemp(103, 20.0f);
        skills.learnTemp(100, 2.0f);
        skills.learnTemp(101, 25.0f);
        skills.learnTemp(105, 40.0f);
        skills.learnTemp(106, 2.0f);
        skills.learnTemp(10052, 24.0f);
        int[] types = new int[]{7, 41, 6, 13, 16, 29, 32, 34, 39, 60, 61};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.multiped.scorpion", types, (byte)8, skills, (short)5, (byte)0, (short)130, (short)30, (short)20, "sound.death.insect", "sound.death.insect", "sound.combat.hit.insect", "sound.combat.hit.insect", 0.4f, 6.0f, 10.0f, 13.0f, 0.0f, 0.0f, 0.75f, 1700, new int[]{92, 439}, 7, 64, (byte)82);
        temp.setHandDamString("claw");
        temp.setBreathDamString("sting");
        temp.setAlignment(-40.0f);
        temp.setMaxAge(100);
        temp.setBaseCombatRating(8.0f);
        temp.setBonusCombatRating(8.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_STUDDED);
        temp.combatDamageType = (byte)2;
        temp.setMaxGroupAttackSize(6);
        temp.setDenName("scorpion stone");
        temp.setDenMaterial((byte)15);
        temp.setMaxPercentOfCreatures(0.05f);
        temp.setNoServerSounds(true);
    }

    private static void createUnicornTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 30.0f);
        skills.learnTemp(104, 30.0f);
        skills.learnTemp(103, 20.0f);
        skills.learnTemp(100, 14.0f);
        skills.learnTemp(101, 14.0f);
        skills.learnTemp(105, 60.0f);
        skills.learnTemp(106, 14.0f);
        skills.learnTemp(10052, 35.0f);
        int[] types = new int[]{7, 41, 3, 32, 28, 9, 39, 35};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.unicorn", types, (byte)1, skills, (short)5, (byte)0, (short)180, (short)50, (short)250, "sound.death.horse", "sound.death.horse", "sound.combat.hit.horse", "sound.combat.hit.horse", 0.7f, 6.0f, 10.0f, 8.0f, 0.0f, 0.0f, 1.6f, 1500, new int[]{92, 311, 71}, 10, 60, (byte)79);
        temp.setAlignment(100.0f);
        temp.setHandDamString("kick");
        temp.setMaxAge(400);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CLOTH);
        temp.setBaseCombatRating(11.0f);
        temp.combatDamageType = 0;
        temp.setMaxGroupAttackSize(4);
        temp.setDenName("unicorn rustle");
        temp.setMaxPercentOfCreatures(0.02f);
        temp.setChildTemplateId(118);
        if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
            temp.setVision((short)6);
        }
    }

    private static void createUnicornFoalTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 15.0f);
        skills.learnTemp(104, 15.0f);
        skills.learnTemp(103, 10.0f);
        skills.learnTemp(100, 6.0f);
        skills.learnTemp(101, 5.0f);
        skills.learnTemp(105, 30.0f);
        skills.learnTemp(106, 10.0f);
        skills.learnTemp(10052, 15.0f);
        int[] types = new int[]{7, 3, 9, 28, 32, 63, 35, 39};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.unicorn.foal", types, (byte)1, skills, (short)3, (byte)0, (short)100, (short)50, (short)75, "sound.death.horse", "sound.death.horse", "sound.combat.hit.horse", "sound.combat.hit.horse", 0.9f, 2.0f, 4.0f, 3.0f, 0.0f, 0.0f, 1.2f, 900, new int[]{311, 71, 309}, 5, 20, (byte)79);
        temp.setAlignment(100.0f);
        temp.setHandDamString("kick");
        temp.setMaxAge(100);
        temp.setAdultFemaleTemplateId(21);
        temp.setAdultMaleTemplateId(21);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CLOTH);
        temp.setBaseCombatRating(5.0f);
        temp.setMaxGroupAttackSize(2);
        temp.combatDamageType = 0;
        temp.setCorpseName("unicorn.foal.");
        if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
            temp.setVision((short)3);
        }
    }

    private static void createBearBlackTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 26.0f);
        skills.learnTemp(104, 26.0f);
        skills.learnTemp(103, 26.0f);
        skills.learnTemp(100, 4.0f);
        skills.learnTemp(101, 4.0f);
        skills.learnTemp(105, 30.0f);
        skills.learnTemp(106, 4.0f);
        skills.learnTemp(10052, 30.0f);
        int[] types = new int[]{7, 3, 6, 13, 29, 32, 39, 60, 61};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.bear.black", types, (byte)2, skills, (short)5, (byte)0, (short)160, (short)50, (short)50, "sound.death.bear", "sound.death.bear", "sound.combat.hit.bear", "sound.combat.hit.bear", 0.8f, 4.0f, 0.0f, 11.0f, 0.0f, 0.0f, 1.0f, 1500, new int[]{303, 302}, 10, 80, (byte)72);
        temp.setHandDamString("maul");
        temp.setMaxAge(200);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_STUDDED);
        temp.setBaseCombatRating(9.0f);
        temp.combatDamageType = 0;
        temp.setMaxGroupAttackSize(6);
        temp.setDenName("bear cave");
        temp.setDenMaterial((byte)15);
        temp.setMaxPercentOfCreatures(0.05f);
    }

    private static void createBearBrownTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 30.0f);
        skills.learnTemp(104, 30.0f);
        skills.learnTemp(103, 30.0f);
        skills.learnTemp(100, 4.0f);
        skills.learnTemp(101, 4.0f);
        skills.learnTemp(105, 30.0f);
        skills.learnTemp(106, 4.0f);
        skills.learnTemp(10052, 40.0f);
        int[] types = new int[]{8, 41, 3, 6, 12, 13, 32, 29, 39, 60, 61};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.bear.brown", types, (byte)2, skills, (short)5, (byte)0, (short)230, (short)50, (short)50, "sound.death.bear", "sound.death.bear", "sound.combat.hit.bear", "sound.combat.hit.bear", 0.75f, 7.0f, 0.0f, 10.0f, 0.0f, 0.0f, 1.2f, 1500, new int[]{92, 303, 302}, 10, 70, (byte)72);
        temp.setBoundsValues(-0.5f, -1.0f, 0.5f, 1.42f);
        temp.setHandDamString("maul");
        temp.setMaxAge(200);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_STUDDED);
        temp.setBaseCombatRating(9.0f);
        temp.combatDamageType = 0;
        temp.setMaxGroupAttackSize(4);
        temp.setDenName("bear cave");
        temp.setDenMaterial((byte)15);
        temp.setMaxPercentOfCreatures(0.04f);
        temp.setUsesNewAttacks(true);
        temp.addPrimaryAttack(new AttackAction("maul", AttackIdentifier.STRIKE, new AttackValues(7.0f, 0.01f, 6.0f, 3, 1, 0, false, 2, 1.0f)));
        temp.addPrimaryAttack(new AttackAction("gnaw", AttackIdentifier.BITE, new AttackValues(5.0f, 0.02f, 8.0f, 3, 1, 3, false, 4, 1.1f)));
        temp.addSecondaryAttack(new AttackAction("bite", AttackIdentifier.BITE, new AttackValues(10.0f, 0.05f, 6.0f, 2, 1, 3, false, 3, 1.1f)));
        temp.addSecondaryAttack(new AttackAction("scratch", AttackIdentifier.STRIKE, new AttackValues(7.0f, 0.05f, 6.0f, 2, 1, 1, false, 8, 1.0f)));
    }

    private static void createEasterBunnyTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 5.0f);
        skills.learnTemp(104, 25.0f);
        skills.learnTemp(103, 70.0f);
        skills.learnTemp(100, 3.0f);
        skills.learnTemp(101, 6.0f);
        skills.learnTemp(105, 20.0f);
        skills.learnTemp(106, 10.0f);
        skills.learnTemp(10052, 40.0f);
        int[] types = new int[]{8, 3, 4, 30, 35};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.easterbunny", types, (byte)3, skills, (short)5, (byte)0, (short)20, (short)30, (short)50, "sound.death.wolf", "sound.death.wolf", "sound.combat.hit.wolf", "sound.combat.hit.wolf", 0.85f, 11.0f, 0.0f, 9.0f, 0.0f, 0.0f, 2.0f, 1900, new int[]{92, 305, 466}, 0, 0, (byte)78);
        temp.setHandDamString("claw");
        temp.setKickDamString("claw");
        temp.setMaxAge(3);
        temp.setEggLayer(true);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CLOTH);
        temp.setBaseCombatRating(50.0f);
        temp.combatDamageType = (byte)2;
        temp.setMaxGroupAttackSize(15);
    }

    private static void createCrocodileTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 35.0f);
        skills.learnTemp(104, 35.0f);
        skills.learnTemp(103, 20.0f);
        skills.learnTemp(100, 6.0f);
        skills.learnTemp(101, 12.0f);
        skills.learnTemp(105, 65.0f);
        skills.learnTemp(106, 1.0f);
        skills.learnTemp(10052, 50.0f);
        int[] types = new int[]{6, 41, 7, 3, 13, 32, 29, 12, 39, 60, 61};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.crocodile", types, (byte)3, skills, (short)5, (byte)0, (short)80, (short)30, (short)120, "sound.death.croc", "sound.death.croc", "sound.combat.hit.croc", "sound.combat.hit.croc", 0.35f, 6.0f, 0.0f, 10.0f, 0.0f, 0.0f, 1.2f, 400, new int[]{92, 305, 71, 310}, 6, 40, (byte)78);
        temp.setHandDamString("claw");
        temp.setKickDamString("claw");
        temp.setMaxAge(200);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_PLATE);
        temp.setBaseCombatRating(9.0f);
        temp.setBonusCombatRating(7.0f);
        temp.combatDamageType = (byte)2;
        temp.setMaxGroupAttackSize(4);
        temp.setDenName("crocodile lair");
        temp.setDenMaterial((byte)15);
        temp.setMaxPercentOfCreatures(0.04f);
    }

    private static void createDogTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 15.0f);
        skills.learnTemp(104, 25.0f);
        skills.learnTemp(103, 20.0f);
        skills.learnTemp(100, 6.0f);
        skills.learnTemp(101, 7.0f);
        skills.learnTemp(105, 15.0f);
        skills.learnTemp(106, 1.0f);
        skills.learnTemp(10052, 7.0f);
        int[] types = new int[]{8, 3, 13, 43, 32, 27, 12};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.dog", types, (byte)3, skills, (short)5, (byte)0, (short)80, (short)30, (short)120, "sound.death.dog", "sound.death.dog", "sound.combat.hit.dog", "sound.combat.hit.dog", 0.95f, 2.0f, 0.0f, 3.0f, 0.0f, 0.0f, 1.2f, 100, new int[]{92, 140, 305, 313}, 20, 10, (byte)74);
        temp.setHandDamString("claw");
        temp.setKickDamString("claw");
        temp.setMaxAge(70);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CLOTH);
        temp.setBaseCombatRating(3.0f);
        temp.combatDamageType = (byte)2;
        temp.setMaxGroupAttackSize(2);
        temp.setDenMaterial((byte)15);
        temp.setMaxPercentOfCreatures(0.01f);
    }

    private static void createBlackWolfTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 20.0f);
        skills.learnTemp(104, 25.0f);
        skills.learnTemp(103, 30.0f);
        skills.learnTemp(100, 6.0f);
        skills.learnTemp(101, 7.0f);
        skills.learnTemp(105, 20.0f);
        skills.learnTemp(106, 1.0f);
        skills.learnTemp(10052, 10.0f);
        int[] types = new int[]{8, 3, 6, 13, 32, 29, 60, 61};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.wolf.black", types, (byte)3, skills, (short)5, (byte)0, (short)80, (short)30, (short)150, "sound.death.wolf", "sound.death.wolf", "sound.combat.hit.wolf", "sound.combat.hit.wolf", 0.85f, 3.0f, 0.0f, 5.0f, 0.0f, 0.0f, 1.2f, 1500, new int[]{140, 92, 305, 302}, 20, 60, (byte)74);
        temp.setHandDamString("claw");
        temp.setKickDamString("claw");
        temp.setMaxAge(70);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CLOTH);
        temp.setBaseCombatRating(6.0f);
        temp.combatDamageType = (byte)2;
        temp.setMaxGroupAttackSize(3);
        temp.setDenName("wolf den");
        temp.setDenMaterial((byte)15);
        temp.setMaxPercentOfCreatures(0.08f);
    }

    private static void createGuardSpiritGoodLenientTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 30.0f);
        skills.learnTemp(104, 30.0f);
        skills.learnTemp(103, 30.0f);
        skills.learnTemp(100, 15.0f);
        skills.learnTemp(101, 15.0f);
        skills.learnTemp(105, 15.0f);
        skills.learnTemp(106, 17.0f);
        skills.learnTemp(10052, 30.0f);
        int[] types = new int[]{22, 23, 12, 13};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.human.spirit.guard", types, (byte)0, skills, (short)5, (byte)0, (short)180, (short)20, (short)35, "sound.death.spirit.male", "sound.death.spirit.female", "sound.combat.hit.spirit.male", "sound.combat.hit.spirit.female", 0.4f, 3.0f, 5.0f, 0.0f, 0.0f, 0.0f, 1.5f, 100, new int[0], 100, 100, (byte)2);
        temp.setAlignment(40.0f);
        temp.setBaseCombatRating(6.0f);
        temp.combatDamageType = (byte)2;
        temp.setMaxGroupAttackSize(4);
        temp.setNoSkillgain(true);
        temp.hasHands = true;
    }

    private static void createGuardSpiritEvilLenientTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 30.0f);
        skills.learnTemp(104, 30.0f);
        skills.learnTemp(103, 30.0f);
        skills.learnTemp(100, 15.0f);
        skills.learnTemp(101, 15.0f);
        skills.learnTemp(105, 15.0f);
        skills.learnTemp(106, 17.0f);
        skills.learnTemp(10052, 30.0f);
        int[] types = new int[]{22, 23, 12, 13};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.human.spirit.sentry", types, (byte)0, skills, (short)5, (byte)0, (short)180, (short)20, (short)35, "sound.death.spirit.male", "sound.death.spirit.female", "sound.combat.hit.spirit.male", "sound.combat.hit.spirit.female", 0.4f, 3.0f, 5.0f, 0.0f, 0.0f, 0.0f, 1.5f, 100, new int[0], 100, 100, (byte)2);
        temp.setAlignment(-40.0f);
        temp.setBaseCombatRating(6.0f);
        temp.combatDamageType = (byte)2;
        temp.setMaxGroupAttackSize(4);
        temp.setNoSkillgain(true);
        temp.hasHands = true;
    }

    private static void createWraithTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 20.0f);
        skills.learnTemp(104, 40.0f);
        skills.learnTemp(103, 23.0f);
        skills.learnTemp(100, 18.0f);
        skills.learnTemp(101, 21.0f);
        skills.learnTemp(105, 19.0f);
        skills.learnTemp(106, 17.0f);
        skills.learnTemp(10052, 50.0f);
        int[] types = new int[]{22, 13, 24};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.human.spirit.wraith", types, (byte)0, skills, (short)25, (byte)0, (short)180, (short)20, (short)35, "sound.death.spirit.male", "sound.death.spirit.female", "sound.combat.hit.spirit.male", "sound.combat.hit.spirit.female", 0.3f, 4.0f, 0.0f, 5.0f, 0.0f, 0.0f, 1.5f, 100, new int[0], 100, 100, (byte)2);
        temp.setAlignment(-80.0f);
        temp.setBaseCombatRating(24.0f);
        temp.combatDamageType = (byte)8;
        temp.setMaxGroupAttackSize(4);
        temp.hasHands = true;
        temp.physicalResistance = 0.3f;
        temp.fireVulnerability = 2.0f;
    }

    private static void createGuardSpiritAbleTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 30.0f);
        skills.learnTemp(104, 30.0f);
        skills.learnTemp(103, 33.0f);
        skills.learnTemp(100, 18.0f);
        skills.learnTemp(101, 21.0f);
        skills.learnTemp(105, 19.0f);
        skills.learnTemp(106, 17.0f);
        skills.learnTemp(10052, 50.0f);
        int[] types = new int[]{22, 23, 12, 13};
        String model = "model.creature.humanoid.human.spirit.avenger";
        if (id == 31) {
            model = "model.creature.humanoid.human.spirit.brute";
        }
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, model, types, (byte)0, skills, (short)25, (byte)0, (short)180, (short)20, (short)35, "sound.death.spirit.male", "sound.death.spirit.female", "sound.combat.hit.spirit.male", "sound.combat.hit.spirit.female", 0.3f, 4.0f, 5.0f, 0.0f, 0.0f, 0.0f, 1.5f, 100, new int[0], 100, 100, (byte)2);
        if (id == 31) {
            temp.setAlignment(-50.0f);
        } else {
            temp.setAlignment(50.0f);
        }
        temp.setBaseCombatRating(8.0f);
        temp.combatDamageType = 1;
        temp.setMaxGroupAttackSize(4);
        temp.hasHands = true;
    }

    private static void createGuardSpiritEvilDangerousTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 30.0f);
        skills.learnTemp(104, 30.0f);
        skills.learnTemp(103, 35.0f);
        skills.learnTemp(100, 17.0f);
        skills.learnTemp(101, 27.0f);
        skills.learnTemp(105, 24.0f);
        skills.learnTemp(106, 24.0f);
        skills.learnTemp(10052, 80.0f);
        int[] types = new int[]{22, 23, 12, 13};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.human.spirit.shadow", types, (byte)0, skills, (short)45, (byte)0, (short)180, (short)20, (short)35, "sound.death.spirit.male", "sound.death.spirit.female", "sound.combat.hit.spirit.male", "sound.combat.hit.spirit.female", 0.3f, 5.0f, 7.0f, 5.0f, 0.0f, 0.0f, 1.5f, 100, new int[0], 100, 100, (byte)2);
        temp.setHandDamString("claw");
        temp.setKickDamString("claw");
        temp.setAlignment(-70.0f);
        temp.setBaseCombatRating(Servers.localServer.isChallengeOrEpicServer() ? 25.0f : 20.0f);
        temp.combatDamageType = 1;
        temp.setMaxGroupAttackSize(Servers.localServer.isChallengeOrEpicServer() ? 4 : 6);
        temp.hasHands = true;
    }

    private static void createGuardSpiritGoodDangerousTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 30.0f);
        skills.learnTemp(104, 30.0f);
        skills.learnTemp(103, 35.0f);
        skills.learnTemp(100, 17.0f);
        skills.learnTemp(101, 27.0f);
        skills.learnTemp(105, 24.0f);
        skills.learnTemp(106, 24.0f);
        skills.learnTemp(10052, 80.0f);
        int[] types = new int[]{22, 23, 12, 13};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.human.spirit.templar", types, (byte)0, skills, (short)45, (byte)0, (short)180, (short)20, (short)35, "sound.death.spirit.male", "sound.death.spirit.female", "sound.combat.hit.spirit.male", "sound.combat.hit.spirit.female", 0.3f, 5.0f, 7.0f, 5.0f, 0.0f, 0.0f, 1.5f, 100, new int[0], 100, 100, (byte)2);
        temp.setAlignment(70.0f);
        temp.setBaseCombatRating(Servers.localServer.isChallengeOrEpicServer() ? 25.0f : 20.0f);
        temp.combatDamageType = 1;
        temp.setMaxGroupAttackSize(Servers.localServer.isChallengeOrEpicServer() ? 4 : 6);
        temp.hasHands = true;
    }

    private static void createGuardBrutalTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 30.0f);
        skills.learnTemp(104, 30.0f);
        skills.learnTemp(103, 36.0f);
        skills.learnTemp(100, 30.0f);
        skills.learnTemp(101, 30.0f);
        skills.learnTemp(105, 30.0f);
        skills.learnTemp(106, 30.0f);
        skills.learnTemp(10005, 90.0f);
        skills.learnTemp(10028, 80.0f);
        skills.learnTemp(10025, 80.0f);
        skills.learnTemp(10001, 80.0f);
        skills.learnTemp(10024, 80.0f);
        skills.learnTemp(10023, 80.0f);
        skills.learnTemp(10021, 80.0f);
        skills.learnTemp(10020, 80.0f);
        skills.learnTemp(10006, 90.0f);
        skills.learnTemp(10052, 90.0f);
        int[] types = new int[]{12, 13, 17, 24, 45, 40, 21, 53, 7};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.human.guard.tower", types, (byte)0, skills, (short)10, (byte)0, (short)180, (short)20, (short)35, "sound.death.male", "sound.death.female", "sound.combat.hit.male", "sound.combat.hit.female", 0.2f, 5.0f, 7.0f, 0.0f, 0.0f, 0.0f, 1.0f, 100, new int[0], 100, 100, (byte)80);
        temp.setBaseCombatRating(23.0f);
        temp.setMaxGroupAttackSize(6);
        temp.setNoSkillgain(true);
        temp.hasHands = true;
    }

    private static void createGuardToughTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 24.0f);
        skills.learnTemp(104, 22.0f);
        skills.learnTemp(103, 31.0f);
        skills.learnTemp(100, 18.0f);
        skills.learnTemp(101, 21.0f);
        skills.learnTemp(105, 19.0f);
        skills.learnTemp(106, 17.0f);
        skills.learnTemp(10005, 90.0f);
        skills.learnTemp(10028, 60.0f);
        skills.learnTemp(10025, 60.0f);
        skills.learnTemp(10001, 60.0f);
        skills.learnTemp(10024, 60.0f);
        skills.learnTemp(10023, 60.0f);
        skills.learnTemp(10021, 60.0f);
        skills.learnTemp(10020, 60.0f);
        skills.learnTemp(10006, 60.0f);
        skills.learnTemp(10052, 90.0f);
        int[] types = new int[]{12, 13, 17, 24, 45, 40, 53, 7};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.human.guard.tower", types, (byte)0, skills, (short)10, (byte)0, (short)180, (short)20, (short)35, "sound.death.male", "sound.death.female", "sound.combat.hit.male", "sound.combat.hit.female", 0.3f, 5.0f, 7.0f, 0.0f, 0.0f, 0.0f, 1.0f, 100, new int[0], 70, 100, (byte)80);
        temp.setBaseCombatRating(20.0f);
        temp.setMaxGroupAttackSize(4);
        temp.setNoSkillgain(true);
        temp.hasHands = true;
    }

    private static void createGuardAbleTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 22.0f);
        skills.learnTemp(104, 20.0f);
        skills.learnTemp(103, 26.0f);
        skills.learnTemp(100, 18.0f);
        skills.learnTemp(101, 18.0f);
        skills.learnTemp(105, 19.0f);
        skills.learnTemp(106, 17.0f);
        skills.learnTemp(10005, 70.0f);
        skills.learnTemp(10028, 60.0f);
        skills.learnTemp(10025, 60.0f);
        skills.learnTemp(10001, 60.0f);
        skills.learnTemp(10024, 60.0f);
        skills.learnTemp(10023, 60.0f);
        skills.learnTemp(10021, 60.0f);
        skills.learnTemp(10020, 60.0f);
        skills.learnTemp(10006, 60.0f);
        skills.learnTemp(10052, 75.0f);
        int[] types = new int[]{11, 12, 13, 17, 45, 40, 53, 7};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.human.guard.tower", types, (byte)0, skills, (short)5, (byte)0, (short)180, (short)20, (short)35, "sound.death.male", "sound.death.female", "sound.combat.hit.male", "sound.combat.hit.female", 0.4f, 4.0f, 5.0f, 0.0f, 0.0f, 0.0f, 1.0f, 100, new int[0], 60, 100, (byte)80);
        temp.setBaseCombatRating(99.0f);
        temp.setMaxGroupAttackSize(4);
        temp.setNoSkillgain(true);
        temp.hasHands = true;
    }

    private static void createGuardDecentTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 20.0f);
        skills.learnTemp(104, 17.0f);
        skills.learnTemp(103, 21.0f);
        skills.learnTemp(100, 15.0f);
        skills.learnTemp(101, 15.0f);
        skills.learnTemp(105, 15.0f);
        skills.learnTemp(106, 17.0f);
        skills.learnTemp(10005, 45.0f);
        skills.learnTemp(10028, 45.0f);
        skills.learnTemp(10025, 45.0f);
        skills.learnTemp(10001, 45.0f);
        skills.learnTemp(10024, 45.0f);
        skills.learnTemp(10023, 45.0f);
        skills.learnTemp(10021, 45.0f);
        skills.learnTemp(10020, 45.0f);
        skills.learnTemp(10006, 45.0f);
        skills.learnTemp(10052, 45.0f);
        int[] types = new int[]{11, 12, 13, 17, 45, 40, 53, 7};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.human.guardDecent", types, (byte)0, skills, (short)5, (byte)0, (short)180, (short)20, (short)35, "sound.death.male", "sound.death.female", "sound.combat.hit.male", "sound.combat.hit.female", 0.5f, 3.0f, 4.0f, 0.0f, 0.0f, 0.0f, 1.0f, 100, new int[0], 50, 100, (byte)80);
        temp.setBaseCombatRating(99.0f);
        temp.setMaxGroupAttackSize(4);
        temp.setNoSkillgain(true);
        temp.hasHands = true;
    }

    private static void createGuardLenientTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 17.0f);
        skills.learnTemp(104, 17.0f);
        skills.learnTemp(103, 18.0f);
        skills.learnTemp(100, 15.0f);
        skills.learnTemp(101, 15.0f);
        skills.learnTemp(105, 15.0f);
        skills.learnTemp(106, 15.0f);
        skills.learnTemp(10005, 40.0f);
        skills.learnTemp(10028, 40.0f);
        skills.learnTemp(10025, 40.0f);
        skills.learnTemp(10001, 40.0f);
        skills.learnTemp(10024, 40.0f);
        skills.learnTemp(10023, 40.0f);
        skills.learnTemp(10021, 40.0f);
        skills.learnTemp(10020, 40.0f);
        skills.learnTemp(10006, 40.0f);
        skills.learnTemp(10052, 40.0f);
        int[] types = new int[]{11, 12, 13, 17, 45, 40, 53, 7};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.human.guardLenient", types, (byte)0, skills, (short)5, (byte)0, (short)180, (short)20, (short)35, "sound.death.male", "sound.death.female", "sound.combat.hit.male", "sound.combat.hit.female", 0.6f, 4.0f, 5.0f, 0.0f, 0.0f, 0.0f, 1.0f, 100, new int[0], 40, 100, (byte)80);
        temp.setMaxGroupAttackSize(4);
        temp.setBaseCombatRating(99.0f);
        temp.setNoSkillgain(true);
        temp.hasHands = true;
    }

    private static void createGuideHotsTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 15.0f);
        skills.learnTemp(104, 15.0f);
        skills.learnTemp(103, 10.0f);
        skills.learnTemp(100, 10.0f);
        skills.learnTemp(101, 10.0f);
        skills.learnTemp(105, 99.0f);
        skills.learnTemp(106, 24.0f);
        skills.learnTemp(10052, 40.0f);
        int[] types = new int[]{0, 4, 17};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.human.guide", types, (byte)0, skills, (short)5, (byte)0, (short)180, (short)20, (short)35, "sound.death.male", "sound.death.female", "sound.combat.hit.male", "sound.combat.hit.female", 1.0f, 1.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.8f, 0, new int[0], 3, 0, (byte)80);
        temp.keepSex = true;
        temp.setBaseCombatRating(99.0f);
        temp.setMaxGroupAttackSize(4);
        temp.combatDamageType = 1;
        temp.setTutorial(true);
        temp.hasHands = true;
    }

    private static void createGuideTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 15.0f);
        skills.learnTemp(104, 15.0f);
        skills.learnTemp(103, 10.0f);
        skills.learnTemp(100, 10.0f);
        skills.learnTemp(101, 10.0f);
        skills.learnTemp(105, 99.0f);
        skills.learnTemp(106, 24.0f);
        skills.learnTemp(10052, 40.0f);
        int[] types = new int[]{0, 4, 17};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.human.guide", types, (byte)0, skills, (short)5, (byte)1, (short)180, (short)20, (short)35, "sound.death.male", "sound.death.female", "sound.combat.hit.male", "sound.combat.hit.female", 1.0f, 1.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.8f, 0, new int[0], 3, 0, (byte)80);
        temp.keepSex = true;
        temp.setBaseCombatRating(99.0f);
        temp.setMaxGroupAttackSize(4);
        temp.combatDamageType = 1;
        temp.setTutorial(true);
        temp.hasHands = true;
    }

    private static void createEvilSantaTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 15.0f);
        skills.learnTemp(104, 15.0f);
        skills.learnTemp(103, 10.0f);
        skills.learnTemp(100, 10.0f);
        skills.learnTemp(101, 10.0f);
        skills.learnTemp(105, 99.0f);
        skills.learnTemp(106, 24.0f);
        skills.learnTemp(10052, 40.0f);
        int[] types = new int[]{0, 4, 17};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.human.evilsanta", types, (byte)0, skills, (short)5, (byte)0, (short)180, (short)20, (short)35, "sound.death.male", "sound.death.female", "sound.combat.hit.male", "sound.combat.hit.female", 1.0f, 1.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.8f, 0, new int[0], 3, 0, (byte)80);
        temp.keepSex = true;
        temp.setBaseCombatRating(99.0f);
        temp.combatDamageType = 1;
        temp.hasHands = true;
    }

    private static void createSantaClausTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 15.0f);
        skills.learnTemp(104, 15.0f);
        skills.learnTemp(103, 10.0f);
        skills.learnTemp(100, 10.0f);
        skills.learnTemp(101, 10.0f);
        skills.learnTemp(105, 99.0f);
        skills.learnTemp(106, 24.0f);
        skills.learnTemp(10052, 40.0f);
        int[] types = new int[]{0, 4, 17};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.human.santa", types, (byte)0, skills, (short)5, (byte)0, (short)180, (short)20, (short)35, "sound.death.male", "sound.death.female", "sound.combat.hit.male", "sound.combat.hit.female", 1.0f, 1.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.8f, 0, new int[]{310}, 3, 0, (byte)80);
        temp.keepSex = true;
        temp.setBaseCombatRating(99.0f);
        temp.combatDamageType = 1;
        temp.hasHands = true;
    }

    private static void createBartenderTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 15.0f);
        skills.learnTemp(104, 15.0f);
        skills.learnTemp(103, 10.0f);
        skills.learnTemp(100, 10.0f);
        skills.learnTemp(101, 10.0f);
        skills.learnTemp(105, 99.0f);
        skills.learnTemp(106, 14.0f);
        skills.learnTemp(10052, 40.0f);
        int[] types = new int[]{0, 4, 17, 26};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.human.bartender", types, (byte)0, skills, (short)2, (byte)0, (short)180, (short)20, (short)35, "sound.death.male", "sound.death.female", "sound.combat.hit.male", "sound.combat.hit.female", 1.0f, 1.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.8f, 0, new int[0], 3, 0, (byte)80);
        temp.setBaseCombatRating(79.0f);
        temp.hasHands = true;
    }

    private static void createSalesmanTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 15.0f);
        skills.learnTemp(104, 15.0f);
        skills.learnTemp(103, 10.0f);
        skills.learnTemp(100, 30.0f);
        skills.learnTemp(101, 30.0f);
        skills.learnTemp(105, 99.0f);
        skills.learnTemp(106, 4.0f);
        skills.learnTemp(10052, 40.0f);
        int[] types = new int[]{0, 1, 4, 5, 12, 17};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.human.salesman", types, (byte)0, skills, (short)2, (byte)0, (short)180, (short)20, (short)35, "sound.death.male", "sound.death.female", "sound.combat.hit.male", "sound.combat.hit.female", 1.0f, 1.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.8f, 0, new int[0], 3, 0, (byte)80);
        temp.setBaseCombatRating(70.0f);
        temp.hasHands = true;
    }

    private static void createChickenTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 1.0f);
        skills.learnTemp(104, 5.0f);
        skills.learnTemp(103, 1.0f);
        skills.learnTemp(100, 5.0f);
        skills.learnTemp(101, 4.0f);
        skills.learnTemp(105, 5.0f);
        skills.learnTemp(106, 1.0f);
        skills.learnTemp(10052, 1.0f);
        int[] types = new int[]{7, 3, 14, 43, 28, 32, 63};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.chicken", types, (byte)7, skills, (short)3, (byte)0, (short)10, (short)5, (short)10, "sound.death.hen", "sound.death.hen", "sound.combat.hit.hen", "sound.combat.hit.hen", 1.0f, 0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 0.5f, 100, new int[0], 1, 0, (byte)77);
        temp.setHandDamString("claw");
        temp.setKickDamString("claw");
        temp.setMaxAge(20);
        temp.setAdultFemaleTemplateId(45);
        temp.setAdultMaleTemplateId(52);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_NONE);
        temp.setBaseCombatRating(1.0f);
        temp.combatDamageType = (byte)2;
        temp.setColourNames(new String[]{"white", "brown", "black"});
        if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
            temp.setVision((short)3);
        }
    }

    private static void createHenTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 3.0f);
        skills.learnTemp(104, 15.0f);
        skills.learnTemp(103, 1.0f);
        skills.learnTemp(100, 5.0f);
        skills.learnTemp(101, 4.0f);
        skills.learnTemp(105, 5.0f);
        skills.learnTemp(106, 1.0f);
        skills.learnTemp(10052, 5.0f);
        int[] types = new int[]{7, 3, 14, 43, 28, 32, 35, 49, 64};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.hen", types, (byte)7, skills, (short)3, (byte)1, (short)30, (short)14, (short)50, "sound.death.hen", "sound.death.hen", "sound.combat.hit.hen", "sound.combat.hit.hen", 1.0f, 0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 0.5f, 100, new int[]{140, 1352, 1352, 1352}, 1, 0, (byte)77);
        temp.setHandDamString("claw");
        temp.setKickDamString("claw");
        temp.setEggLayer(true);
        temp.setEggTemplateId(48);
        temp.setMaxAge(20);
        temp.keepSex = true;
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_NONE);
        temp.setBaseCombatRating(1.0f);
        temp.combatDamageType = (byte)2;
        temp.setMaxPercentOfCreatures(0.02f);
        temp.setColourNames(new String[]{"white", "brown", "black"});
        if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
            temp.setVision((short)3);
        }
    }

    private static void createPheasantTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 5.0f);
        skills.learnTemp(104, 25.0f);
        skills.learnTemp(103, 5.0f);
        skills.learnTemp(100, 5.0f);
        skills.learnTemp(101, 4.0f);
        skills.learnTemp(105, 10.0f);
        skills.learnTemp(106, 1.0f);
        skills.learnTemp(10052, 7.0f);
        int[] types = new int[]{7, 3, 28, 32, 35, 49, 60, 61};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.pheasant", types, (byte)7, skills, (short)3, (byte)0, (short)30, (short)14, (short)50, "sound.death.pheasant", "sound.death.pheasant", "sound.combat.hit.pheasant", "sound.combat.hit.pheasant", 1.0f, 1.0f, 0.0f, 1.5f, 0.0f, 0.0f, 1.0f, 100, new int[]{140, 1352, 1352, 1352, 1352, 1352}, 1, 0, (byte)78);
        temp.setHandDamString("claw");
        temp.setKickDamString("claw");
        temp.setMaxAge(100);
        temp.setBaseCombatRating(1.0f);
        temp.combatDamageType = (byte)2;
        temp.setMaxGroupAttackSize(2);
        temp.setMaxPercentOfCreatures(0.01f);
        if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
            temp.setVision((short)4);
        }
    }

    private static void createRoosterTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 5.0f);
        skills.learnTemp(104, 25.0f);
        skills.learnTemp(103, 5.0f);
        skills.learnTemp(100, 5.0f);
        skills.learnTemp(101, 4.0f);
        skills.learnTemp(105, 10.0f);
        skills.learnTemp(106, 1.0f);
        skills.learnTemp(10052, 10.0f);
        int[] types = new int[]{7, 3, 14, 43, 28, 32, 64};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.rooster", types, (byte)7, skills, (short)3, (byte)0, (short)30, (short)14, (short)50, "sound.death.hen", "sound.death.hen", "sound.combat.hit.hen", "sound.combat.hit.hen", 1.0f, 0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 100, new int[]{140, 1352, 1352, 1352}, 1, 0, (byte)77);
        temp.setHandDamString("claw");
        temp.setKickDamString("claw");
        temp.setMaxAge(30);
        temp.keepSex = true;
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_NONE);
        temp.setBaseCombatRating(1.0f);
        temp.setMaxGroupAttackSize(2);
        temp.combatDamageType = (byte)2;
        temp.setMaxPercentOfCreatures(0.002f);
        temp.setColourNames(new String[]{"brown", "white", "black"});
        if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
            temp.setVision((short)4);
        }
    }

    private static void createDeerTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 15.0f);
        skills.learnTemp(104, 33.0f);
        skills.learnTemp(103, 20.0f);
        skills.learnTemp(100, 5.0f);
        skills.learnTemp(101, 4.0f);
        skills.learnTemp(105, 10.0f);
        skills.learnTemp(106, 8.0f);
        skills.learnTemp(10052, 10.0f);
        int[] types = new int[]{7, 3, 9, 28, 32, 49, 35, 60, 61};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.deer", types, (byte)1, skills, (short)10, (byte)0, (short)70, (short)50, (short)50, "sound.death.deer", "sound.death.deer", "sound.combat.hit.deer", "sound.combat.hit.deer", 1.0f, 1.0f, 1.0f, 0.5f, 1.0f, 0.0f, 1.5f, 30, new int[]{307, 306, 71, 140, 309, 308, 308, 310}, 5, 10, (byte)78);
        temp.setHandDamString("kick");
        temp.setMaxAge(100);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_NONE);
        temp.setBaseCombatRating(2.0f);
        temp.setMaxGroupAttackSize(3);
        temp.combatDamageType = 0;
        temp.setMaxPercentOfCreatures(0.005f);
        if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
            temp.setVision((short)9);
        }
    }

    private static void createFoalTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 10.0f);
        skills.learnTemp(104, 10.0f);
        skills.learnTemp(103, 10.0f);
        skills.learnTemp(100, 3.0f);
        skills.learnTemp(101, 2.0f);
        skills.learnTemp(105, 5.0f);
        skills.learnTemp(106, 1.0f);
        skills.learnTemp(10052, 5.0f);
        int[] types = new int[]{7, 12, 3, 43, 14, 9, 28, 32, 63};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.foal", types, (byte)1, skills, (short)3, (byte)0, (short)100, (short)50, (short)50, "sound.death.horse", "sound.death.horse", "sound.combat.hit.horse", "sound.combat.hit.horse", 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 100, new int[]{307, 140, 306, 71, 309, 308, 308}, 5, 0, (byte)79);
        temp.setHandDamString("kick");
        temp.setMaxAge(100);
        temp.setAdultFemaleTemplateId(64);
        temp.setAdultMaleTemplateId(64);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_NONE);
        temp.setBaseCombatRating(3.0f);
        temp.setMaxGroupAttackSize(2);
        temp.combatDamageType = 0;
        temp.isHorse = true;
        temp.setColourNames(new String[]{"grey", "brown", "gold", "black", "white", "piebald pinto", "blood bay", "ebony black", "skewbald pinto", "gold buckskin", "black silver", "appaloosa", "chestnut"});
        if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
            temp.setVision((short)3);
        }
    }

    private static void createHorseTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 25.0f);
        skills.learnTemp(104, 20.0f);
        skills.learnTemp(103, 40.0f);
        skills.learnTemp(100, 7.0f);
        skills.learnTemp(101, 7.0f);
        skills.learnTemp(105, 22.0f);
        skills.learnTemp(106, 5.0f);
        skills.learnTemp(10052, 28.0f);
        int[] types = new int[]{7, 12, 41, 43, 3, 14, 9, 28, 32};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.horse", types, (byte)1, skills, (short)3, (byte)0, (short)180, (short)50, (short)250, "sound.death.horse", "sound.death.horse", "sound.combat.hit.horse", "sound.combat.hit.horse", 1.0f, 1.0f, 2.5f, 1.5f, 2.0f, 0.0f, 1.5f, 100, new int[]{307, 306, 140, 71, 309, 308, 308}, 5, 0, (byte)79);
        temp.setMaxAge(200);
        temp.setChildTemplateId(65);
        temp.setBaseCombatRating(6.0f);
        temp.combatDamageType = 0;
        temp.setAlignment(100.0f);
        temp.setMaxGroupAttackSize(3);
        temp.setHandDamString("kick");
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CLOTH);
        temp.isHorse = true;
        temp.setMaxPercentOfCreatures(0.1f);
        temp.setColourNames(new String[]{"grey", "brown", "gold", "black", "white", "piebald pinto", "blood bay", "ebony black", "skewbald pinto", "gold buckskin", "black silver", "appaloosa", "chestnut"});
        if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
            temp.setVision((short)4);
        }
    }

    private static void createBisonTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 25.0f);
        skills.learnTemp(104, 23.0f);
        skills.learnTemp(103, 30.0f);
        skills.learnTemp(100, 5.0f);
        skills.learnTemp(101, 4.0f);
        skills.learnTemp(105, 10.0f);
        skills.learnTemp(106, 3.0f);
        skills.learnTemp(10052, 15.0f);
        int[] types = new int[]{7, 3, 43, 14, 9, 28, 32, 49, 35, 15};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.bison", types, (byte)1, skills, (short)10, (byte)0, (short)180, (short)50, (short)250, "sound.death.bison", "sound.death.bison", "sound.combat.hit.bison", "sound.combat.hit.bison", 0.3f, 5.0f, 5.0f, 10.0f, 4.0f, 0.0f, 0.8f, 30, new int[]{307, 306, 140, 71, 309, 308, 308, 304, 304}, 5, 10, (byte)73);
        temp.setHandDamString("kick");
        temp.setMaxAge(50);
        temp.keepSex = false;
        temp.setBaseCombatRating(4.0f);
        temp.combatDamageType = 0;
        temp.setMaxGroupAttackSize(3);
        temp.setBonusCombatRating(14.0f);
        temp.setMaxPercentOfCreatures(0.01f);
        if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
            temp.setVision((short)7);
        }
    }

    private static void createBullTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 25.0f);
        skills.learnTemp(104, 23.0f);
        skills.learnTemp(103, 30.0f);
        skills.learnTemp(100, 5.0f);
        skills.learnTemp(101, 4.0f);
        skills.learnTemp(105, 10.0f);
        skills.learnTemp(106, 3.0f);
        skills.learnTemp(10052, 15.0f);
        int[] types = new int[]{7, 41, 3, 43, 14, 9, 28, 32};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.bull", types, (byte)1, skills, (short)3, (byte)0, (short)180, (short)50, (short)250, "sound.death.cow.brown", "sound.death.cow.brown", "sound.combat.hit.cow.brown", "sound.combat.hit.cow.brown", 1.0f, 2.0f, 2.0f, 3.0f, 4.0f, 0.0f, 0.5f, 100, new int[]{307, 306, 140, 71, 309, 308, 308, 312, 312}, 5, 10, (byte)73);
        temp.setHandDamString("kick");
        temp.setMaxAge(50);
        temp.keepSex = true;
        temp.setBaseCombatRating(4.0f);
        temp.combatDamageType = 0;
        temp.setMaxGroupAttackSize(3);
        temp.setMateTemplateId(3);
        temp.setMaxPercentOfCreatures(0.02f);
        if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
            temp.setVision((short)5);
        }
    }

    private static void createCalfTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 10.0f);
        skills.learnTemp(104, 10.0f);
        skills.learnTemp(103, 10.0f);
        skills.learnTemp(100, 3.0f);
        skills.learnTemp(101, 2.0f);
        skills.learnTemp(105, 5.0f);
        skills.learnTemp(106, 1.0f);
        skills.learnTemp(10052, 5.0f);
        int[] types = new int[]{7, 3, 14, 43, 9, 28, 32, 49, 35, 63};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.calf", types, (byte)1, skills, (short)6, (byte)0, (short)100, (short)50, (short)100, "sound.death.cow.brown", "sound.death.cow.brown", "sound.combat.hit.cow.brown", "sound.combat.hit.cow.brown", 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 100, new int[]{307, 140, 306, 71, 309, 308, 308}, 5, 0, (byte)73);
        temp.setHandDamString("kick");
        temp.setMaxAge(100);
        temp.setAdultFemaleTemplateId(3);
        temp.setAdultMaleTemplateId(49);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_NONE);
        temp.setBaseCombatRating(3.0f);
        temp.setMaxGroupAttackSize(2);
        temp.setCombatDamageType((byte)0);
        if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
            temp.setVision((short)3);
        }
    }

    private static void createBrownCowTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 20.0f);
        skills.learnTemp(104, 20.0f);
        skills.learnTemp(103, 30.0f);
        skills.learnTemp(100, 5.0f);
        skills.learnTemp(101, 4.0f);
        skills.learnTemp(105, 10.0f);
        skills.learnTemp(106, 1.0f);
        skills.learnTemp(10052, 8.0f);
        int[] types = new int[]{7, 41, 3, 43, 14, 15, 9, 28, 32, 49, 35};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.cow", types, (byte)1, skills, (short)3, (byte)1, (short)180, (short)50, (short)250, "sound.death.cow.brown", "sound.death.cow.brown", "sound.combat.hit.cow.brown", "sound.combat.hit.cow.brown", 1.0f, 1.0f, 1.0f, 0.0f, 2.0f, 0.0f, 0.5f, 100, new int[]{307, 306, 140, 71, 309, 308, 308}, 5, 0, (byte)73);
        temp.keepSex = true;
        temp.setMaxAge(100);
        temp.setBaseCombatRating(1.0f);
        temp.setChildTemplateId(50);
        temp.setMateTemplateId(49);
        temp.setMaxGroupAttackSize(2);
        temp.combatDamageType = 0;
        temp.setMaxPercentOfCreatures(0.02f);
        if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
            temp.setVision((short)4);
        }
    }

    private static void createLambTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 7.0f);
        skills.learnTemp(104, 7.0f);
        skills.learnTemp(103, 7.0f);
        skills.learnTemp(100, 3.0f);
        skills.learnTemp(101, 2.0f);
        skills.learnTemp(105, 5.0f);
        skills.learnTemp(106, 1.0f);
        skills.learnTemp(10052, 5.0f);
        int[] types = new int[]{7, 3, 43, 14, 54, 9, 28, 32, 49, 35, 63};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.lamb", types, (byte)1, skills, (short)3, (byte)0, (short)50, (short)30, (short)30, "sound.death.deer", "sound.death.deer", "sound.combat.hit.deer", "sound.combat.hit.deer", 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.5f, 100, new int[]{140, 309, 308, 308}, 5, 0, (byte)83);
        temp.setMaxAge(100);
        temp.setAdultFemaleTemplateId(96);
        temp.setAdultMaleTemplateId(102);
        temp.combatDamageType = 0;
        temp.setBaseCombatRating(1.0f);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_NONE);
        if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
            temp.setVision((short)3);
        }
    }

    private static void createSheepTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 17.0f);
        skills.learnTemp(104, 17.0f);
        skills.learnTemp(103, 25.0f);
        skills.learnTemp(100, 5.0f);
        skills.learnTemp(101, 4.0f);
        skills.learnTemp(105, 10.0f);
        skills.learnTemp(106, 1.0f);
        skills.learnTemp(10052, 8.0f);
        int[] types = new int[]{7, 3, 43, 14, 54, 15, 9, 28, 32, 49, 35, 52};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.sheep", types, (byte)1, skills, (short)3, (byte)1, (short)50, (short)50, (short)30, "sound.death.deer", "sound.death.deer", "sound.combat.hit.deer", "sound.combat.hit.deer", 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.5f, 100, new int[]{140, 309, 308, 308}, 5, 0, (byte)83);
        temp.setMaxAge(100);
        temp.keepSex = true;
        temp.setChildTemplateId(101);
        temp.setMateTemplateId(102);
        temp.setBaseCombatRating(1.0f);
        temp.setMaxGroupAttackSize(2);
        temp.combatDamageType = 0;
        temp.setMaxPercentOfCreatures(0.03f);
        if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
            temp.setVision((short)5);
        }
    }

    private static void createRamTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 23.0f);
        skills.learnTemp(104, 18.0f);
        skills.learnTemp(103, 35.0f);
        skills.learnTemp(100, 6.0f);
        skills.learnTemp(101, 6.0f);
        skills.learnTemp(105, 20.0f);
        skills.learnTemp(106, 3.0f);
        skills.learnTemp(10052, 22.0f);
        int[] types = new int[]{7, 3, 43, 14, 54, 9, 28, 32, 49, 35, 52};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.sheep", types, (byte)1, skills, (short)3, (byte)0, (short)50, (short)50, (short)30, "sound.death.deer", "sound.death.deer", "sound.combat.hit.deer", "sound.combat.hit.deer", 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.5f, 100, new int[]{140, 309, 308, 308, 304, 304}, 5, 7, (byte)83);
        temp.setHandDamString("headbutt");
        temp.setMaxAge(100);
        temp.setChildTemplateId(101);
        temp.setMateTemplateId(96);
        temp.keepSex = true;
        temp.setBaseCombatRating(5.0f);
        temp.setMaxGroupAttackSize(3);
        temp.combatDamageType = 0;
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CLOTH);
        temp.setMaxPercentOfCreatures(0.05f);
        if (Features.Feature.CREATURE_MOVEMENT_CHANGES.isEnabled()) {
            temp.setVision((short)5);
        }
    }

    private static void createKidTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 10.0f);
        skills.learnTemp(104, 10.0f);
        skills.learnTemp(103, 10.0f);
        skills.learnTemp(100, 3.0f);
        skills.learnTemp(101, 2.0f);
        skills.learnTemp(105, 5.0f);
        skills.learnTemp(106, 1.0f);
        skills.learnTemp(10052, 5.0f);
        int[] types = new int[]{7, 14, 27, 32, 45};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.human.child", types, (byte)0, skills, (short)5, (byte)0, (short)100, (short)30, (short)20, "sound.death.male.child", "sound.death.female.child", "sound.combat.hit.male.child", "sound.combat.hit.female.child", 1.0f, 2.0f, 0.0f, 3.0f, 2.0f, 0.0f, 1.0f, 100, new int[0], 25, 10, (byte)80);
        temp.setMaxAge(10);
        temp.setAdultFemaleTemplateId(1);
        temp.setAdultMaleTemplateId(1);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_NONE);
        temp.setBaseCombatRating(3.0f);
        temp.combatDamageType = 0;
        temp.hasHands = true;
    }

    private static void createHumanTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        float num = Servers.localServer.getSkillbasicval();
        skills.learn(1, num);
        skills.learn(3, num);
        skills.learn(2, num);
        skills.learn(102, num);
        skills.learn(104, Servers.localServer.getSkillbcval());
        skills.learn(103, num);
        skills.learn(100, Servers.localServer.getSkillmindval());
        skills.learn(101, num);
        skills.learn(105, num);
        skills.learn(106, num);
        skills.learn(1023, Servers.localServer.getSkillfightval());
        int[] types = new int[]{1, 12, 13, 17, 45, 7};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.human.player", types, (byte)0, skills, (short)80, (byte)0, (short)180, (short)20, (short)35, "sound.death.male", "sound.death.female", "sound.combat.hit.male", "sound.combat.hit.female", 1.0f, 1.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.5f, 0, new int[0], 25, 100, (byte)80);
        temp.setBaseCombatRating(4.0f);
        temp.setChildTemplateId(66);
        temp.setMaxGroupAttackSize(7);
        temp.setAdultFemaleTemplateId(1);
        temp.setAdultMaleTemplateId(1);
        temp.hasHands = true;
    }

    private static void createRiftTemplateOne(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 20.0f);
        skills.learnTemp(104, 35.0f);
        skills.learnTemp(103, 25.0f);
        skills.learnTemp(100, 8.0f);
        skills.learnTemp(101, 10.0f);
        skills.learnTemp(105, 30.0f);
        skills.learnTemp(106, 2.0f);
        skills.learnTemp(10052, 40.0f);
        int[] types = new int[]{7, 13, 3, 29, 6, 12};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.quadraped.beast.rift", types, (byte)3, skills, (short)8, (byte)0, (short)40, (short)20, (short)100, "sound.death.dog", "sound.death.dog", "sound.combat.hit.dog", "sound.combat.hit.dog", 0.3f, 12.0f, 0.0f, 14.0f, 15.0f, 0.0f, 1.6f, 300, new int[]{636}, 10, 90, (byte)74);
        temp.setHandDamString("claw");
        temp.setAlignment(-50.0f);
        temp.setMaxAge(50);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CLOTH);
        temp.setBaseCombatRating(14.0f);
        temp.combatDamageType = 1;
        temp.setMaxGroupAttackSize(8);
        temp.setMaxPercentOfCreatures(0.01f);
    }

    private static void createRiftTemplateTwo(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 25.0f);
        skills.learnTemp(104, 25.0f);
        skills.learnTemp(103, 30.0f);
        skills.learnTemp(100, 12.0f);
        skills.learnTemp(101, 14.0f);
        skills.learnTemp(105, 30.0f);
        skills.learnTemp(106, 12.0f);
        skills.learnTemp(10052, 40.0f);
        int[] types = new int[]{7, 6, 13, 16, 40, 29, 30, 34, 39, 45, 55, 18, 12};
        int biteDamage = 10;
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.jackal.rift", types, (byte)0, skills, (short)8, (byte)0, (short)150, (short)100, (short)150, "sound.death.troll", "sound.death.troll", "sound.combat.hit.troll", "sound.combat.hit.troll", 0.2f, 15.0f, 6.0f, 10.0f, 0.0f, 0.0f, 1.4f, 700, new int[]{636}, 10, 80, (byte)81);
        temp.setHandDamString("burn");
        temp.setAlignment(-20.0f);
        temp.setMaxAge(50);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_CHAIN);
        temp.setBaseCombatRating(19.0f);
        temp.combatDamageType = (byte)2;
        temp.setMaxGroupAttackSize(6);
        temp.setMaxPercentOfCreatures(0.01f);
    }

    private static void createRiftTemplateThree(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 25.0f);
        skills.learnTemp(104, 25.0f);
        skills.learnTemp(103, 30.0f);
        skills.learnTemp(100, 12.0f);
        skills.learnTemp(101, 14.0f);
        skills.learnTemp(105, 30.0f);
        skills.learnTemp(106, 12.0f);
        skills.learnTemp(10052, 40.0f);
        int[] types = new int[]{7, 6, 13, 16, 40, 29, 30, 34, 39, 45, 55, 18, 12};
        int biteDamage = 10;
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.ogre.rift", types, (byte)0, skills, (short)8, (byte)0, (short)450, (short)100, (short)150, "sound.death.troll", "sound.death.troll", "sound.combat.hit.troll", "sound.combat.hit.troll", 0.1f, 20.0f, 10.0f, 10.0f, 0.0f, 0.0f, 1.4f, 700, new int[]{636}, 10, 84, (byte)81);
        temp.setHandDamString("burn");
        temp.setAlignment(-20.0f);
        temp.setMaxAge(50);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_STUDDED);
        temp.setBaseCombatRating(19.0f);
        temp.combatDamageType = 0;
        temp.setMaxGroupAttackSize(8);
        temp.setMaxPercentOfCreatures(0.01f);
    }

    private static void createRiftTemplateFour(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 25.0f);
        skills.learnTemp(104, 25.0f);
        skills.learnTemp(103, 30.0f);
        skills.learnTemp(100, 12.0f);
        skills.learnTemp(101, 14.0f);
        skills.learnTemp(105, 30.0f);
        skills.learnTemp(106, 12.0f);
        skills.learnTemp(10052, 40.0f);
        int[] types = new int[]{7, 6, 13, 16, 40, 29, 30, 34, 39, 45, 55, 18, 12};
        int biteDamage = 40;
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.warmaster.rift", types, (byte)0, skills, (short)8, (byte)0, (short)450, (short)100, (short)150, "sound.death.troll", "sound.death.troll", "sound.combat.hit.troll", "sound.combat.hit.troll", 0.03f, 32.0f, 20.0f, 40.0f, 0.0f, 0.0f, 1.6f, 700, new int[]{636}, 20, 94, (byte)74);
        temp.setHandDamString("burn");
        temp.setAlignment(-20.0f);
        temp.setMaxAge(50);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_PLATE);
        temp.setBaseCombatRating(19.0f);
        temp.combatDamageType = 1;
        temp.setMaxGroupAttackSize(20);
        temp.setMaxPercentOfCreatures(0.01f);
    }

    private static void createRiftCasterTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 20.0f);
        skills.learnTemp(104, 20.0f);
        skills.learnTemp(103, 20.0f);
        skills.learnTemp(100, 32.0f);
        skills.learnTemp(101, 34.0f);
        skills.learnTemp(105, 20.0f);
        skills.learnTemp(106, 15.0f);
        skills.learnTemp(10052, 40.0f);
        skills.learnTemp(10067, 50.0f);
        int[] types = new int[]{7, 6, 13, 16, 40, 29, 30, 34, 39, 45, 55, 18, 12};
        int biteDamage = 10;
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.jackal.rift.caster", types, (byte)0, skills, (short)8, (byte)0, (short)150, (short)100, (short)150, "sound.death.troll", "sound.death.troll", "sound.combat.hit.troll", "sound.combat.hit.troll", 0.2f, 15.0f, 6.0f, 10.0f, 0.0f, 0.0f, 1.4f, 700, new int[]{636}, 10, 90, (byte)74);
        temp.setHandDamString("burn");
        temp.setAlignment(-20.0f);
        temp.setMaxAge(50);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_STUDDED);
        temp.setBaseCombatRating(14.0f);
        temp.combatDamageType = (byte)4;
        temp.setMaxGroupAttackSize(6);
        temp.setMaxPercentOfCreatures(0.001f);
    }

    private static void createRiftOgreMageTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 20.0f);
        skills.learnTemp(104, 20.0f);
        skills.learnTemp(103, 20.0f);
        skills.learnTemp(100, 42.0f);
        skills.learnTemp(101, 44.0f);
        skills.learnTemp(105, 30.0f);
        skills.learnTemp(106, 15.0f);
        skills.learnTemp(10052, 50.0f);
        skills.learnTemp(10067, 50.0f);
        int[] types = new int[]{7, 6, 13, 16, 40, 29, 30, 34, 39, 45, 55, 18, 12};
        int biteDamage = 10;
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.ogre.rift.mage", types, (byte)0, skills, (short)8, (byte)0, (short)350, (short)100, (short)150, "sound.death.troll", "sound.death.troll", "sound.combat.hit.troll", "sound.combat.hit.troll", 0.1f, 20.0f, 10.0f, 10.0f, 0.0f, 0.0f, 1.4f, 700, new int[]{636}, 10, 90, (byte)81);
        temp.setHandDamString("burn");
        temp.setAlignment(-50.0f);
        temp.setMaxAge(50);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_LEATHER_DRAGON);
        temp.setBaseCombatRating(19.0f);
        temp.combatDamageType = (byte)9;
        temp.setMaxGroupAttackSize(8);
        temp.setMaxPercentOfCreatures(0.001f);
    }

    private static void createRiftSummonerTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 40.0f);
        skills.learnTemp(104, 25.0f);
        skills.learnTemp(103, 30.0f);
        skills.learnTemp(100, 18.0f);
        skills.learnTemp(101, 14.0f);
        skills.learnTemp(105, 40.0f);
        skills.learnTemp(106, 15.0f);
        skills.learnTemp(10052, 45.0f);
        skills.learnTemp(10067, 60.0f);
        int[] types = new int[]{7, 6, 13, 16, 40, 29, 30, 34, 39, 45, 55, 18, 12};
        int biteDamage = 10;
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.jackal.rift.summoner", types, (byte)0, skills, (short)8, (byte)0, (short)150, (short)100, (short)150, "sound.death.troll", "sound.death.troll", "sound.combat.hit.troll", "sound.combat.hit.troll", 0.2f, 15.0f, 6.0f, 10.0f, 0.0f, 0.0f, 1.4f, 700, new int[]{636}, 10, 70, (byte)74);
        temp.setHandDamString("claw");
        temp.setAlignment(-30.0f);
        temp.setMaxAge(50);
        temp.setArmourType(ArmourTemplate.ARMOUR_TYPE_STUDDED);
        temp.setBaseCombatRating(12.0f);
        temp.combatDamageType = 1;
        temp.setMaxGroupAttackSize(6);
        temp.setMaxPercentOfCreatures(0.001f);
    }

    private static void createNpcHumanTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        skills.learnTemp(102, 15.0f);
        skills.learnTemp(104, 15.0f);
        skills.learnTemp(103, 10.0f);
        skills.learnTemp(100, 10.0f);
        skills.learnTemp(101, 10.0f);
        skills.learnTemp(105, 99.0f);
        skills.learnTemp(106, 24.0f);
        skills.learnTemp(10052, 40.0f);
        int[] types = new int[]{0, 4, 17};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.humanoid.human.player", types, (byte)0, skills, (short)5, (byte)0, (short)180, (short)20, (short)35, "sound.death.male", "sound.death.female", "sound.combat.hit.male", "sound.combat.hit.female", 1.0f, 1.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.8f, 0, new int[0], 3, 0, (byte)80);
        temp.setBaseCombatRating(99.0f);
        temp.setMaxGroupAttackSize(4);
        temp.combatDamageType = 1;
        temp.hasHands = true;
    }

    private static void createFishTemplate(int id, String name, String plural, String longDesc, Skills skills) throws IOException {
        int[] types = new int[]{4, 38, 37};
        CreatureTemplate temp = CreatureTemplateFactory.getInstance().createCreatureTemplate(id, name, plural, longDesc, "model.creature.fish", types, (byte)9, skills, (short)3, (byte)0, (short)10, (short)10, (short)100, "sound.death.snake", "sound.death.snake", "sound.combat.hit.snake", "sound.combat.hit.snake", 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 100, new int[0], 40, 59, (byte)85);
        temp.offZ = -1.4f;
        temp.setCreatureAI(new FishAI());
    }
}

