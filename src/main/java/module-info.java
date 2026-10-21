module com.bsmi.attendancesystem {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.h2database;
    requires java.sql;
    requires flyway.core;
    requires jbcrypt;
    requires MaterialFX;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires org.kordamp.ikonli.fontawesome;

    // Flyway reads the SQL files through the class loader, which needs the folder opened
    opens db.migration;
    opens com.bsmi.attendancesystem to javafx.fxml;
    exports com.bsmi.attendancesystem;
}