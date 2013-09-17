package crussell52.poi.config;

import crussell52.poi.PoiException;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.util.permissions.DefaultPermissions;

import java.util.HashMap;
import java.util.Map;

public class PoiType
{
    private String _id;
    private String _label;
    private PermissionDefault _defaultPerm;
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

    public PermissionDefault getDefaultPerm()
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
                _defaultPerm = PermissionDefault.getByName(poiTypeConfig.get("defaultPerm").toString());
                if (_defaultPerm == null) {
                    throw new PoiException(PoiException.SYSTEM_ERROR, "Invalid value for POI type defaultPerm.");
                }
            }
            else {
                _defaultPerm = PermissionDefault.TRUE;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new PoiException(PoiException.SYSTEM_ERROR, "Error reading POI type defaultPerm value.");
        }

    }

    public Map<String, Object>toMap() {
        Map<String, Object> map = new HashMap<String, Object>();

        // Manually translate default perm to config string.
        switch (_defaultPerm) {
            case TRUE:
                map.put("defaultPerm", true);
                break;
            case FALSE:
                map.put("defaultPerm", false);
                break;
            case OP:
                map.put("defaultPerm", "op");
                break;
            case NOT_OP:
                map.put("defaultPerm", "notOp");
                break;
        }

        // Translate the other properties.
        map.put("mapMarkerIcon", _mapMarkerIcon);
        map.put("label", _label);
        map.put("id", _id);

        return map;
    }
}
