module tanks.game {
    requires java.desktop;
    requires org.checkerframework.checker.qual;

    exports Tanks;
    opens Tanks;
}