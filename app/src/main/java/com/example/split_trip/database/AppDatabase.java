
package com.example.split_trip.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.split_trip.models.Trip;
import com.example.split_trip.models.Expense;
import com.example.split_trip.models.TripMember;
import com.example.split_trip.models.VisitedPlace;

@Database(
        entities = {Trip.class, Expense.class, TripMember.class, VisitedPlace.class},
        version = 3,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TripDao tripDao();

    public abstract ExpenseDao expenseDao();

    public abstract TripMemberDao tripMemberDao();

    public abstract VisitedPlaceDao VisitedPlaceDao();

    public static final Migration MIGRATION_1_2 =
            new Migration(1, 2) {
                @Override
                public void migrate(SupportSQLiteDatabase database) {
                    database.execSQL(
                            "CREATE TABLE IF NOT EXISTS `trip_members` (" +
                                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                    "`tripId` INTEGER NOT NULL, " +
                                    "`name` TEXT, " +
                                    "`email` TEXT, " +
                                    "`creator` INTEGER NOT NULL, " +
                                    "FOREIGN KEY(`tripId`) REFERENCES `trips`(`id`) " +
                                    "ON UPDATE NO ACTION ON DELETE CASCADE)"
                    );

                    database.execSQL(
                            "CREATE INDEX IF NOT EXISTS `index_trip_members_tripId` " +
                                    "ON `trip_members` (`tripId`)"
                    );
                }
            };

    public static final Migration MIGRATION_2_3 =
            new Migration(2, 3) {
                @Override
                public void migrate(SupportSQLiteDatabase database) {

                    database.execSQL(
                            "CREATE TABLE IF NOT EXISTS `visited_places` (" +
                                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                                    "`tripId` INTEGER NOT NULL, " +
                                    "`name` TEXT, " +
                                    "`location` TEXT, " +
                                    "`visitDate` INTEGER NOT NULL, " +
                                    "`note` TEXT, " +
                                    "FOREIGN KEY(`tripId`) REFERENCES `trips`(`id`) " +
                                    "ON UPDATE NO ACTION ON DELETE CASCADE)"
                    );

                    database.execSQL(
                            "CREATE INDEX IF NOT EXISTS `index_visited_places_tripId` " +
                                    "ON `visited_places` (`tripId`)"
                    );
                }
            };
}
