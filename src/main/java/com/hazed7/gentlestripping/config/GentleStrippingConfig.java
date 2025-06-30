package com.hazed7.gentlestripping.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "gentlestripping")
public class GentleStrippingConfig implements ConfigData {
    
    @ConfigEntry.Gui.Tooltip
    public boolean preventDurabilityLoss = true;
    
    @ConfigEntry.Gui.Tooltip
    public boolean enableUnstripping = true;
    
    @ConfigEntry.Gui.Tooltip
    public boolean requireSneakingForUnstripping = true;
} 