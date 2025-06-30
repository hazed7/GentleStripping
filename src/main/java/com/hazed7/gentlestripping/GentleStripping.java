package com.hazed7.gentlestripping;

import com.hazed7.gentlestripping.config.GentleStrippingConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GentleStripping implements ModInitializer {
    public static final String MOD_ID = "gentlestripping";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    public static GentleStrippingConfig CONFIG;

    @Override
    public void onInitialize() {
        AutoConfig.register(GentleStrippingConfig.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(GentleStrippingConfig.class).getConfig();
        
        LOGGER.info("GentleStripping initialized - no durability loss for stripping/unstripping logs!");
    }
} 