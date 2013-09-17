package crussell52.poi.markers;

import crussell52.poi.Poi;
import crussell52.poi.PoiException;
import crussell52.poi.config.Config;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.util.List;

public class MarkerManager {
    private final Plugin _plugin;
    private DynmapCommonAPI _dynmapAPI;
    private MarkerSet _markerSet;

    public MarkerManager(Plugin plugin) throws PoiException, Exception {
        this._plugin = plugin;

        Plugin dynmap = _plugin.getServer().getPluginManager().getPlugin("dynmap");
        if (!(dynmap instanceof DynmapCommonAPI)) {
            throw new PoiException(PoiException.SYSTEM_ERROR, "Dynmap not found.");
        }

        this._dynmapAPI = (DynmapCommonAPI)dynmap;
        _plugin.getLogger().info("Found dynmap...");

        // Check version compatibility.
        // Not a perfect pattern match, but good enough for up to major version 9.
        if (!_dynmapAPI.getDynmapCoreVersion().matches("^[1-9]\\.(?:[89]|[0-9]{2,}).*$")) {
            throw new PoiException(PoiException.SYSTEM_ERROR, "Requires Dynmap 1.8+.");
        }

        // Look for marker API
        if (!_dynmapAPI.markerAPIInitialized()) {
            throw new PoiException(PoiException.SYSTEM_ERROR, "Dynmap marker API is not enabled.");
        }

        // All pre-requisites have been met.
        MarkerAPI markerAPI = _dynmapAPI.getMarkerAPI();
        _markerSet = markerAPI.createMarkerSet("crussell52.poi", "Points of Interest", markerAPI.getMarkerIcons(), false);
    }

    public void addMarker(Poi poi) {
        // Don't add markers to non marker worlds.
        if (!Config.isMapMarkerWorld(poi.getWorld())) {
            return;
        }

        String markerIconID = Config.getPoiType(poi.getType()).getMapIconMarker();
        if (markerIconID == null) {
            markerIconID = Config.getDefaultMapMarkerIcon();
        }
        MarkerIcon markerIcon = _dynmapAPI.getMarkerAPI().getMarkerIcon(markerIconID);

        if (markerIcon == null) {
            _plugin.getLogger().warning("Unrecognized marker icon (" + markerIconID + "). Using system default (sign) instead.");
            markerIcon = _dynmapAPI.getMarkerAPI().getMarkerIcon("sign");
        }

        _markerSet.createMarker("crussell52.poi." + poi.getId(),
                _getLabelMarkup(poi), true,
                poi.getWorld(), poi.getX(), poi.getY(), poi.getZ(),
                markerIcon, false);
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
        return  "<div style=\"text-align:center;\">" +
                "<div style=\"font-weight:bold;text-decoration:underline\">" + Config.getPoiType(poi.getType()).getLabel() + "</div>" +
                poi.getName() + "<br />" +
                "By: " + poi.getOwner() + "<br />" +
                "(ID: " + poi.getId() + ")" +
                "</div>";
    }
}
