package com.wurmonline.server.utils;

public interface WurmDbUpdatable {
   String getDatabaseUpdateStatement();

   @Override
   String toString();
}
