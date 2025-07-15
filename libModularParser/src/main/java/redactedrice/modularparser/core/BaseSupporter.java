package redactedrice.modularparser.core;


import java.util.ArrayList;
import java.util.List;

public class BaseSupporter<T> extends BaseModule implements Supporter {
    protected final List<T> submodules = new ArrayList<>();
    protected final Class<T> tClass;

    protected BaseSupporter(String name, Class<T> tClass) {
        super(name);
        this.tClass = tClass;
    }

    @Override
    public boolean handleModule(Module module) {
        if (tClass.isInstance(module)) {
            submodules.add(tClass.cast(module));
        }
        return true;
    }

}
