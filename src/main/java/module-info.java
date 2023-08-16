module com.bsmi.attendancesystem {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;
    requires MaterialFX;
    requires java.sql;
    requires mysql.connector.j;

    opens com.bsmi.attendancesystem to javafx.fxml;
    exports com.bsmi.attendancesystem;
}