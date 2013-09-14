package crussell52.poi.config;

import crussell52.poi.PoiException;

import java.util.HashMap;
import java.util.Map;

public class PoiType
{
    private String _id;
    private String _label;
    private boolean _defaultPerm;
    private String _mapMarkerIcon;

    public String getID()
    {
        return _id;
    }

    public String getLabel()
    {
        return _label;
    }

    public String getMapIconMarker()
    {
        return _mapMarkerIcon;
    }

    public boolean getDefaultPerm()
    {
        return _defaultPerm;
    }

    public PoiType(String id, String label)
    {
        _id = id;
        _label = label;
    }

    public PoiType(Map<String, Object> poiTypeConfig) throws PoiException {
        try {
            if (poiTypeConfig.containsKey("id")) {
                _id = (String) poiTypeConfig.get("id");
                if (_id.equals("") || _id.matches("\\s")) {
                    throw new PoiException(PoiException.SYSTEM_ERROR, "The POI type id can not be empty and can not contain spaces.");
                }
            }
            else {
                throw new PoiException(PoiException.SYSTEM_ERROR, "The POI type id is missing.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new PoiException(PoiException.SYSTEM_ERROR, "Error reading POI type id.");
        }

        try {
            if (poiTypeConfig.containsKey("label")) {
                _label = (String) poiTypeConfig.get("label");
                if (_label.equals("")) {
                    throw new PoiException(PoiException.SYSTEM_ERROR, "The POI type label can not be empty.");
                }
            }
            else {
                throw new PoiException(PoiException.SYSTEM_ERROR, "The POI type label is missing.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new PoiException(PoiException.SYSTEM_ERROR, "Error reading POI type label.");
        }

        try {
            if (poiTypeConfig.containsKey("mapMarkerIcon")) {
                _mapMarkerIcon = (String) poiTypeConfig.get("mapMarkerIcon");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new PoiException(PoiException.SYSTEM_ERROR, "Error reading POI type label.");
        }

        try {
            if (poiTypeConfig.containsKey("defaultPerm")) {
                _defaultPerm = (Boolean) poiTypeConfig.get("defaultPerm");
            }
            else {
                _defaultPerm = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new PoiException(PoiException.SYSTEM_ERROR, "Error reading POI type defaultPerm value.");
        }

    }

    public Map<String, Object>toMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", _id);
        map.put("label", _label);
        map.put("defaultPerm", _defaultPerm);
        map.put("mapMarkerIcon", _mapMarkerIcon);

        return map;
    }
}
