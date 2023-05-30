package com.wurmonline.shared.constants;

public interface HighwayConstants {
   byte POSSIBLE_LINKS = 0;
   byte ACTUAL_LINKS = 1;
   byte NO_LINKS = 0;
   byte LINK_NORTH = 1;
   byte LINK_NORTH_EAST = 2;
   byte LINK_EAST = 4;
   byte LINK_SOUTH_EAST = 8;
   byte LINK_SOUTH = 16;
   byte LINK_SOUTH_WEST = 32;
   byte LINK_WEST = 64;
   byte LINK_NORTH_WEST = -128;
   byte ALL_LINKS = -1;
   byte NO_GLOW = 0;
   byte GLOW_RED = 1;
   byte GLOW_BLUE = 2;
   byte GLOW_GREEN = 3;
   byte GLOW_YELLOW = 16;
   byte NO_LINK = -1;
   int NOROUTE = 99999;
}
