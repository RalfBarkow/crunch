package org.pragmaticminds.crunch.api.windowed.filter;

import org.pragmaticminds.crunch.api.records.MRecord;
import org.pragmaticminds.crunch.events.Event;

import java.io.Serializable;
import java.util.Collection;

/**
 * Filters {@link Event}s by itself and the {@link MRecord} {@link java.util.List} that was the base for the creation
 * of the {@link Event}.
 *
 * @author Erwin Wagasow
 * Created by Erwin Wagasow on 16.08.2018
 */
@FunctionalInterface
public interface EventAndRecordsFilter extends Serializable {
    
    /**
     * Filters all Events that should not be passed to further processing.
     * The filtering is based on the {@link Event} itself and on the {@link MRecord} {@link java.util.List} to
     * determine if an Event is to be passed further.
     *
     * @param event of interest, that should be filtered or not.
     * @param records that were used to create the {@link Event}.
     * @return true if the {@link Event} is to be send further for processing, otherwise false.
     */
    boolean apply(Event event, Collection<MRecord> records);
}