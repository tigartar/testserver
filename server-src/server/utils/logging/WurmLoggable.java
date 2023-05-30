package com.wurmonline.server.utils.logging;

public interface WurmLoggable {
   String getDatabaseInsertStatement();

   @Override
   String toString();
}
