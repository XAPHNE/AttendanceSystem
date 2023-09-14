module com.bsmi.attendancesystem {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.h2database;
    requires java.sql;
    requires jbcrypt;
    requires MaterialFX;
    requires mysql.connector.j;
    requires org.kordamp.ikonli.javafx;

    opens com.bsmi.attendancesystem to javafx.fxml;
    exports com.bsmi.attendancesystem;
}