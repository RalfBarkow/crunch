package org.pragmaticminds.crunch.api;

import org.pragmaticminds.crunch.api.events.EventHandler;
import org.pragmaticminds.crunch.api.function.def.FunctionDef;
import org.pragmaticminds.crunch.api.values.dates.Value;

import java.io.Serializable;
import java.util.Map;

/**
 * The default approach to implement an evaluation function
 *
 * @author Erwin Wagasow
 * Created by Erwin Wagasow on 19.10.2017
 *
 * @deprecated Part of the old API
 */
@Deprecated
public abstract class EvalFunction<T> implements Serializable {

    private transient EventHandler eventHandler;

    public EventHandler getEventHandler() {
        return eventHandler;
    }

    public void setEventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    /**
     * @return a Structure that defines all properties of the current {@link EvalFunction} implementation
     */
    public abstract FunctionDef getFunctionDef();

    /**
     * is started before the processing of the records begins
     *
     * @param literals     the constant in values
     * @param eventHandler the interface to fire results into the system
     */
    public abstract void setup(Map<String, Value> literals, EventHandler eventHandler);

    /**
     * processes single record values
     *
     * @param timestamp the corresponding time stamp for the record
     * @param channels  contains the values of a record
     * @return output value of this {@link EvalFunction}
     */
    public abstract T eval(long timestamp, Map<String, Value> channels);

    /**
     * is called after the processing of all records for cleaning up and firing the last results
     */
    public abstract void finish();

}
