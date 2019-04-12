module org.l2j.commons {
    requires java.sql;
    requires org.slf4j;
    requires com.zaxxer.hikari;
    requires transitive java.xml.bind;
    requires java.management;
    requires java.compiler;
    requires cache.api;
    requires java.desktop;
    requires io.github.joealisson.primitive;

    exports util;
    exports xml;
    exports crypt;
    exports Commons.src.main.org.l2j.commons.database;
    exports Commons.src.main.org.l2j.commons.database.annotation;
    exports Commons.src.main.org.l2j.commons.configuration;
    exports Commons.src.main.org.l2j.commons.threading;
    exports Commons.src.main.org.l2j.commons.cache;
    exports Commons.src.main.org.l2j.commons.database.handler;
    exports Commons.src.main.org.l2j.commons.network;
    exports Commons.src.main.org.l2j.commons.util.filter;

    uses org.l2j.commons.database.handler.TypeHandler;
    provides org.l2j.commons.database.handler.TypeHandler
        with org.l2j.commons.database.handler.IntegerHandler,
             org.l2j.commons.database.handler.LongHandler,
             org.l2j.commons.database.handler.VoidHandler,
             org.l2j.commons.database.handler.ListHandler,
             org.l2j.commons.database.handler.StringHandler,
             org.l2j.commons.database.handler.IntSetHandler,
             org.l2j.commons.database.handler.EntityHandler;
}