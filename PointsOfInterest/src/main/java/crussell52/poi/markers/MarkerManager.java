package crussell52.poi.markers;

import crussell52.poi.Config;
import crussell52.poi.Poi;
import crussell52.poi.PoiException;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
        Set<MarkerIcon> markerIcons = markerAPI.getMarkerIcons();
        _markerSet = markerAPI.createMarkerSet("crussell52.poi", "Points of Interest", new TreeSet<MarkerIcon>(), false);

        // Iterate over available markerIcons
        String markerIconID;
        boolean haveWhitelist = Config.getMapMarkerWhitelist().size() > 0;
        for (MarkerIcon markerIcon : markerIcons) {
            // Allow the marker so long as it is not in the black list and the white
            // list is empty or contains the marker id.
            markerIconID = markerIcon.getMarkerIconID();
            if (!Config.getMapMarkerBlacklist().contains(markerIconID) &&
                    (!haveWhitelist || Config.getMapMarkerWhitelist().contains(markerIconID))) {
                _markerSet.addAllowedMarkerIcon(markerIcon);
            }
        }
    }

    public void addMarker(Poi poi) {

        _markerSet.createMarker("crussell52.poi." + poi.getId(),
                _getLabelMarkup(poi), true,
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
