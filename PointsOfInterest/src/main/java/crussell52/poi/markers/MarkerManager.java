package crussell52.poi.markers;

import crussell52.poi.Poi;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import java.util.List;

public class MarkerManager {
    private final Plugin _plugin;
    private DynmapCommonAPI _dynmapAPI;
    private MarkerSet _markerSet;

    public MarkerManager(Plugin plugin) throws Exception {
        this._plugin = plugin;

        Plugin dynmap = _plugin.getServer().getPluginManager().getPlugin("dynmap");
        if (dynmap instanceof DynmapCommonAPI) {
            this._dynmapAPI = (DynmapCommonAPI)dynmap;
            _plugin.getLogger().info("Found dynmap...");

            // Check version compatibility.
            // Not a perfect pattern match, but good enough for up to major version 9.
            if (_dynmapAPI.getDynmapCoreVersion().matches("^[1-9]\\.(?:[89]|[0-9]{2,}).*$")) {
                // Look for marker API
                if (_dynmapAPI.markerAPIInitialized()) {
                    // All pre-requisites have been met.

                    MarkerAPI markerAPI = _dynmapAPI.getMarkerAPI();
                    _markerSet= markerAPI.createMarkerSet("crussell52.poi", "Points of Interest", markerAPI.getMarkerIcons(), false);
                    _markerSet.setDefaultMarkerIcon(markerAPI.getMarkerIcon("sign"));
                    return;
                }
                else {
                    _plugin.getLogger().info("Dynmap marker API is not enabled.");
                }
            }
            else {
                _plugin.getLogger().info("Dynmap version too old. Version 1.8+ required.");
            }
        }

        throw new Exception();
    }

    public void addMarker(Poi poi) {

        _markerSet.createMarker("crussell52.poi." + poi.getId(),
                _getLabelMarkup(poi), false,
                poi.getWorld(), poi.getX(), poi.getY(), poi.getZ(),
                _markerSet.getDefaultMarkerIcon(), false);
    }

    public void removeMarker(Poi poi) {
        _markerSet.findMarker("crussell52.poi." + poi.getId()).deleteMarker();
    }

    public void removeMarkers() {
        for (Marker marker : _markerSet.getMarkers()) {
            marker.deleteMarker();
        }
    }

    public void setMarkers(List<Poi> poiList) {
        removeMarkers();
        for (Poi poi : poiList) {
            addMarker(poi);
        }
    }

    private String _getLabelMarkup(Poi poi)
    {
        return  poi.getName() + "<br />" +
                "By: " + poi.getOwner() + "<br />" +
                "ID: " + poi.getId();
    }
}
