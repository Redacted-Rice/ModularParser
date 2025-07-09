package redactedrice.modularparser;


public interface Module {
    String getName();

    void setParser(ModularParser parser);

    public void configure();
}
