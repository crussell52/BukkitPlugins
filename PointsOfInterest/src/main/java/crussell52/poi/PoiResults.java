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

    public int indexOf(Poi poi)
    {
        if (poi == null) {
            for (int i = 0; i < this.size(); i++) {
                if (this.get(i) == null) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < this.size(); i++) {
                if (this.get(i).getId() == poi.getId()) {
                    return i;
                }
            }
        }

        return -1;
    }
}
