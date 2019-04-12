module org.l2j.authserver {
    requires org.l2j.commons;
    requires org.slf4j;
    requires io.github.joealisson.mmocore;

    exports org.l2j.authserver;
    opens org.l2j.authserver.data.xml to java.xml.bind;
    opens org.l2j.authserver.settings to org.l2j.commons;
    opens org.l2j.authserver.data.database to org.l2j.commons;
}