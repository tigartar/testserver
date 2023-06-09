package com.wurmonline.server.database.migrations;

import com.wurmonline.server.database.MysqlConnectionFactory;
import com.wurmonline.server.database.WurmDatabaseSchema;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import javax.annotation.Nonnull;

public class MysqlMigrator extends Migrator<MysqlConnectionFactory> {
   private static final Path DEFAULT_MIGRATIONS_DIR = new File("migrations").toPath();

   public MysqlMigrator(WurmDatabaseSchema migrationSchema, MysqlConnectionFactory connectionFactory) {
      super(connectionFactory, Collections.singletonList(DEFAULT_MIGRATIONS_DIR), flyway -> {
         flyway.setDataSource(connectionFactory.getUrl(), connectionFactory.getUser(), connectionFactory.getPassword());
         WurmDatabaseSchema[] schemas = WurmDatabaseSchema.values();
         ArrayList<String> migrationSchemas = new ArrayList<>(schemas.length);

         for(WurmDatabaseSchema schema : schemas) {
            if (!schema.equals(migrationSchema)) {
               migrationSchemas.add(schema.getDatabase());
            }
         }

         migrationSchemas.add(0, migrationSchema.getDatabase());
         flyway.setSchemas(migrationSchemas.toArray(new String[migrationSchemas.size()]));
      });
   }

   @Nonnull
   @Override
   public MigrationResult migrate() {
      return super.migrate();
   }
}
