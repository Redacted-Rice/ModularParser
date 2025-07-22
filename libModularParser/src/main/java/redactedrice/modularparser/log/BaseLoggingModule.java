package redactedrice.modularparser.log;


import redactedrice.modularparser.core.BaseModule;

public class BaseLoggingModule extends BaseModule {
    protected LogSupporter logger;

    protected BaseLoggingModule(String name) {
        super(name);
    }

    @Override
    public void setModuleRefs() {
        logger = parser.getSupporterOfType(LogSupporter.class);
    }
}
