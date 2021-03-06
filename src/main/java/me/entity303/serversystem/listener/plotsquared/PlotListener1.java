package me.entity303.serversystem.listener.plotsquared;

import com.google.common.base.Optional;
import com.intellectualcrafters.plot.flag.Flags;
import com.plotsquared.bukkit.events.PlayerEnterPlotEvent;
import com.plotsquared.bukkit.events.PlayerLeavePlotEvent;
import me.entity303.serversystem.main.ServerSystem;
import me.entity303.serversystem.utils.MessageUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlotListener1 extends MessageUtils implements Listener {

    public PlotListener1(ServerSystem plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlotEnter(PlayerEnterPlotEvent e) {
        if (e.getPlot().hasFlag(Flags.TIME)) {
            Optional<Long> optionalLong = e.getPlot().getFlag(Flags.TIME);
            if (optionalLong.isPresent())
                PlotListener.TIME_MAP.put(e.getPlayer(), optionalLong.get());
        }
    }

    @EventHandler
    public void onPlotLeave(PlayerLeavePlotEvent e) {
        PlotListener.TIME_MAP.remove(e.getPlayer());
    }
}
