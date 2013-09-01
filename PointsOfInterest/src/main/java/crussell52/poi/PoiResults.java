package crussell52.poi;

import org.bukkit.Location;

import java.util.ArrayList;

public class PoiResults extends ArrayList<Poi> {

    private long _created;
    private Location _searchCenter;

    public long getCreated() {
        return _created;
    }

    public Location getSearchCenter()
    {
        return _searchCenter;
    }

    public PoiResults() {

        super();
        _created = System.currentTimeMillis();
    }

    public PoiResults(Location searchCenter) {

        super();
        _created = System.currentTimeMillis();
        _searchCenter = searchCenter;
    }
}
