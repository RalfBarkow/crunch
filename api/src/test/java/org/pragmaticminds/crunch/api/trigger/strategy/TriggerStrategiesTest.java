package org.pragmaticminds.crunch.api.trigger.strategy;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pragmaticminds.crunch.api.values.TypedValues;
import org.pragmaticminds.crunch.api.values.dates.BooleanValue;
import org.pragmaticminds.crunch.api.values.dates.Value;

import java.util.HashMap;
import java.util.Map;

import static org.pragmaticminds.crunch.api.trigger.comparator.Suppliers.ChannelExtractors.booleanChannel;
import static org.pragmaticminds.crunch.api.trigger.strategy.TriggerStrategies.*;

/**
 * @author Erwin Wagasow
 * Created by Erwin Wagasow on 27.07.2018
 */
public class TriggerStrategiesTest {
    private TypedValues falseValues;
    private TypedValues trueValues;
    
    @Before
    public void setUp() throws Exception {
        Map<String, Value> falseMap = new HashMap<>();
        falseMap.put("val", new BooleanValue(false));
        falseValues = TypedValues.builder()
            .timestamp(System.currentTimeMillis())
            .source("test")
            .values(falseMap)
            .build();
        Map<String, Value> trueMap = new HashMap<>();
        trueMap.put("val", new BooleanValue(true));
        trueValues = TypedValues.builder()
            .timestamp(System.currentTimeMillis())
            .source("test")
            .values(trueMap)
            .build();
    }
    
    @Test
    public void isToBeTriggeredOnTruePositive() {
        TriggerStrategy strategy = onTrue(values -> true);
        boolean result = strategy.isToBeTriggered(null);
        Assert.assertTrue(result);
    }
 
    @Test
    public void isToBeTriggeredOnTrueNegative() {
        TriggerStrategy strategy = onTrue(values -> false);
        boolean result = strategy.isToBeTriggered(null);
        Assert.assertFalse(result);
    }
 
    @Test
    public void isToBeTriggeredOnFalsePositive() {
        TriggerStrategy strategy = onFalse(values -> false);
        boolean result = strategy.isToBeTriggered(null);
        Assert.assertTrue(result);
    }
 
    @Test
    public void isToBeTriggeredOnFalseNegative() {
        TriggerStrategy strategy = onFalse(values -> true);
        boolean result = strategy.isToBeTriggered(null);
        Assert.assertFalse(result);
    }
 
    @Test
    public void isToBeTriggeredOnChangePositive() {
        TriggerStrategy strategy = onChange(booleanChannel("val"));
        
        strategy.isToBeTriggered(falseValues);
        boolean result = strategy.isToBeTriggered(trueValues);
        Assert.assertTrue(result);
    
        strategy.isToBeTriggered(trueValues);
        boolean result2 = strategy.isToBeTriggered(falseValues);
        Assert.assertTrue(result2);
    }
 
    @Test
    public void isToBeTriggeredOnChangeNegative() {
        TriggerStrategy strategy = onChange(booleanChannel("val"));
    
        strategy.isToBeTriggered(falseValues);
        boolean result = strategy.isToBeTriggered(falseValues);
        Assert.assertFalse(result);
    
        strategy.isToBeTriggered(trueValues);
        boolean result2 = strategy.isToBeTriggered(trueValues);
        Assert.assertFalse(result2);
    }
 
    @Test
    public void isToBeTriggeredOnBecomeTruePositive() {
        TriggerStrategy strategy = onBecomeTrue(booleanChannel("val"));
        strategy.isToBeTriggered(falseValues);
        boolean result = strategy.isToBeTriggered(trueValues);
        Assert.assertTrue(result);
    }
 
    @Test
    public void isToBeTriggeredOnBecomeTrueNegative() {
        TriggerStrategy strategy = onBecomeTrue(booleanChannel("val"));
        strategy.isToBeTriggered(trueValues);
        boolean result = strategy.isToBeTriggered(falseValues);
        Assert.assertFalse(result);
    }
 
    @Test
    public void isToBeTriggeredOnBecomeFalsePositive() {
        TriggerStrategy strategy = onBecomeFalse(booleanChannel("val"));
        strategy.isToBeTriggered(trueValues);
        boolean result = strategy.isToBeTriggered(falseValues);
        Assert.assertTrue(result);
    }
 
    @Test
    public void isToBeTriggeredOnBecomeFalseNegative() {
        TriggerStrategy strategy = onBecomeFalse(booleanChannel("val"));
        strategy.isToBeTriggered(falseValues);
        boolean result = strategy.isToBeTriggered(trueValues);
        Assert.assertFalse(result);
    }
 
    @Test
    public void isToBeTriggeredAllways() {
        TriggerStrategy strategy = always();
        boolean result = strategy.isToBeTriggered(falseValues);
        Assert.assertTrue(result);
    }
}