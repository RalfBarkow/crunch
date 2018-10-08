package org.pragmaticminds.crunch.api.trigger.strategy;

import org.junit.Before;
import org.junit.Test;
import org.pragmaticminds.crunch.api.pipe.ClonerUtil;
import org.pragmaticminds.crunch.api.records.MRecord;
import org.pragmaticminds.crunch.api.trigger.comparator.Supplier;
import org.pragmaticminds.crunch.api.values.TypedValues;
import org.pragmaticminds.crunch.api.values.dates.BooleanValue;
import org.pragmaticminds.crunch.api.values.dates.Value;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.pragmaticminds.crunch.api.trigger.comparator.Suppliers.ChannelExtractors.booleanChannel;
import static org.pragmaticminds.crunch.api.trigger.comparator.Suppliers.ChannelExtractors.stringChannel;
import static org.pragmaticminds.crunch.api.trigger.strategy.TriggerStrategies.*;

/**
 * @author Erwin Wagasow
 * Created by Erwin Wagasow on 27.07.2018
 */
public class TriggerStrategiesTest {
    private TypedValues falseValues;
    private TypedValues trueValues;
    private TypedValues nullValues;
    
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
        Map<String, Value> nullMap = new HashMap<>();
        nullValues = TypedValues.builder()
            .timestamp(System.currentTimeMillis())
            .source("test")
            .values(trueMap)
            .build();
    }
    
    @Test
    public void isToBeTriggeredOnTruePositive() {
        TriggerStrategy strategy = onTrue(getSupplier(true));
        boolean result = strategy.isToBeTriggered(null);
        assertTrue(result);
        
        assertTrue(strategy.getChannelIdentifiers().contains("test"));

        strategy = onTrue(getSupplier());
        result = strategy.isToBeTriggered(null);
        assertFalse(result);
    
        TriggerStrategy clone = ClonerUtil.clone(strategy);
        assertFalse(clone.isToBeTriggered(null));
    }
 
    @Test
    public void isToBeTriggeredOnTrueNegative() {
        TriggerStrategy strategy = onTrue(getSupplier());
        boolean result = strategy.isToBeTriggered(null);
        assertFalse(result);
        
        assertTrue(strategy.getChannelIdentifiers().contains("test"));
    
        TriggerStrategy clone = ClonerUtil.clone(strategy);
        assertFalse(clone.isToBeTriggered(null));
    }
 
    @Test
    public void isToBeTriggeredOnFalsePositive() {
        TriggerStrategy strategy = onFalse(getSupplier(false));
        boolean result = strategy.isToBeTriggered(null);
        assertTrue(result);
        assertTrue(strategy.getChannelIdentifiers().contains("test"));

        strategy = onFalse(getSupplier());
        result = strategy.isToBeTriggered(null);
        assertFalse(result);
    
        TriggerStrategy clone = ClonerUtil.clone(strategy);
        assertFalse(clone.isToBeTriggered(null));
    }
 
    @Test
    public void isToBeTriggeredOnFalseNegative() {
        TriggerStrategy strategy = onFalse(getSupplier());
        boolean result = strategy.isToBeTriggered(null);
        assertFalse(result);
        assertTrue(strategy.getChannelIdentifiers().contains("test"));
    
        TriggerStrategy clone = ClonerUtil.clone(strategy);
        assertFalse(clone.isToBeTriggered(null));
    }
 
    @Test
    public void isToBeTriggeredOnChangePositive() {
        TriggerStrategy strategy = onChange(booleanChannel("val"));
        assertTrue(strategy.getChannelIdentifiers().contains("val"));
    
        strategy.isToBeTriggered(falseValues);
        boolean result = strategy.isToBeTriggered(trueValues);
        assertTrue(result);
    
        strategy.isToBeTriggered(trueValues);
        boolean result2 = strategy.isToBeTriggered(falseValues);
        assertTrue(result2);

        strategy = onChange(getSupplier());
        result = strategy.isToBeTriggered(nullValues);
        assertFalse(result);
        assertTrue(strategy.getChannelIdentifiers().contains("test"));
    }
    
    @Test
    public void isToBeTriggeredOnChangeNegative() {
        TriggerStrategy strategy = onChange(booleanChannel("val"));
        assertTrue(strategy.getChannelIdentifiers().contains("val"));
    
        strategy.isToBeTriggered(falseValues);
        boolean result = strategy.isToBeTriggered(falseValues);
        assertFalse(result);
        
        strategy.isToBeTriggered(trueValues);
        boolean result2 = strategy.isToBeTriggered(trueValues);
        assertFalse(result2);
    }
    
    @Test
    public void isToBeTriggeredOnChangeInitialValue() {
        TriggerStrategy strategy = onChange(booleanChannel("val"), true);
        assertTrue(strategy.getChannelIdentifiers().contains("val"));
    
        boolean result = strategy.isToBeTriggered(falseValues);
        assertTrue(result);
    }
 
    @Test
    public void isToBeTriggeredOnBecomeTruePositive() {
        TriggerStrategy strategy = onBecomeTrue(booleanChannel("val"));
        strategy.isToBeTriggered(falseValues);
        boolean result = strategy.isToBeTriggered(trueValues);
        assertTrue(result);
        assertTrue(strategy.getChannelIdentifiers().contains("val"));

        strategy = onBecomeTrue(getSupplier());
        result = strategy.isToBeTriggered(nullValues);
        assertFalse(result);
        assertTrue(strategy.getChannelIdentifiers().contains("test"));
    }
 
    @Test
    public void isToBeTriggeredOnBecomeTrueNegative() {
        TriggerStrategy strategy = onBecomeTrue(booleanChannel("val"));
        strategy.isToBeTriggered(trueValues);
        boolean result = strategy.isToBeTriggered(falseValues);
        assertFalse(result);
        assertTrue(strategy.getChannelIdentifiers().contains("val"));
    }
    
    @Test
    public void isToBeTriggeredOnBecomeTrueInitialValue() {
        TriggerStrategy strategy = onBecomeTrue(booleanChannel("val"), false);
        assertTrue(strategy.getChannelIdentifiers().contains("val"));
    
        boolean result = strategy.isToBeTriggered(trueValues);
        assertTrue(result);
    }
    
    @Test
    public void isToBeTriggeredOnBecomeFalsePositive() {
        TriggerStrategy strategy = onBecomeFalse(booleanChannel("val"));
        strategy.isToBeTriggered(trueValues);
        boolean result = strategy.isToBeTriggered(falseValues);
        assertTrue(result);
        assertTrue(strategy.getChannelIdentifiers().contains("val"));

        strategy = onBecomeFalse(getSupplier());
        result = strategy.isToBeTriggered(nullValues);
        assertFalse(result);
        assertTrue(strategy.getChannelIdentifiers().contains("test"));
    }

    @Test
    public void isToBeTriggeredOnBecomeFalseNegative() {
        TriggerStrategy strategy = onBecomeFalse(booleanChannel("val"));
        strategy.isToBeTriggered(falseValues);
        boolean result = strategy.isToBeTriggered(trueValues);
        assertFalse(result);
        assertTrue(strategy.getChannelIdentifiers().contains("val"));
    }
    
    @Test
    public void isToBeTriggeredOnBecomeFalseInitialValue() {
        TriggerStrategy strategy = onBecomeFalse(booleanChannel("val"), true);
        
        boolean result = strategy.isToBeTriggered(falseValues);
        assertTrue(result);
        assertTrue(strategy.getChannelIdentifiers().contains("val"));
    }
    
    @Test
    public void isToBeTriggeredOnNull(){
        TriggerStrategy strategy = onNull(stringChannel("null"));
        strategy.isToBeTriggered(trueValues);
        boolean result = strategy.isToBeTriggered(trueValues);
        assertTrue(result);
        assertTrue(strategy.getChannelIdentifiers().contains("null"));
    }
    
    @Test
    public void isToBeTriggeredOnNotNull(){
        TriggerStrategy strategy = onNotNull(booleanChannel("val"));
        strategy.isToBeTriggered(trueValues);
        boolean result = strategy.isToBeTriggered(trueValues);
        assertTrue(result);
        assertTrue(strategy.getChannelIdentifiers().contains("val"));
    }
 
    @Test
    public void isToBeTriggeredAllways() {
        TriggerStrategy strategy = always();
        boolean result = strategy.isToBeTriggered(falseValues);
        assertTrue(result);
        assertTrue(strategy.getChannelIdentifiers().isEmpty());
    }
    
    private Supplier<Boolean> getSupplier() {
        return getSupplier(null);
    }
    
    private Supplier<Boolean> getSupplier(Boolean value) {
        return new InnerBooleanSupplier(value);
    }
    
    public static class InnerBooleanSupplier implements Supplier<Boolean> {
        private Boolean value;
    
        public InnerBooleanSupplier(Boolean value) {
            this.value = value;
        }
    
        @Override
        public Boolean extract(MRecord values) {
            return value;
        }
    
        @Override
        public String getIdentifier() {
            return "null";
        }
    
        @Override
        public Set<String> getChannelIdentifiers() {
            return Collections.singleton("test");
        }
    }
}