package lobbyprotection;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;

public class Mob {

    private String name = null;
    private String getBy = "type";
    private int maxAllowed = 0;
    private Location homeLocation = null;
    private EntityType type;
    private int radius = 0;

    public Mob() {
    }

    public Mob( String name, String getBy, int maxAllowed, Location homeLocation, EntityType type, int radius ) {
        this.name = name;
        this.getBy = getBy;
        this.maxAllowed = maxAllowed;
        this.homeLocation = homeLocation;
        this.type = type;
        this.radius = radius;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return this.name;
    }
    public void setGetBy(String getBy) {
        this.getBy = getBy;
    }
    public String getGetBy() {
        return this.getBy;
    }
    public void setMaxAllowed(int maxAllowed) {
        this.maxAllowed = maxAllowed;
    }
    public int getMaxAllowed() {
        return this.maxAllowed;
    }
    public void setHomeLocation(Location homeLocation) {
        this.homeLocation = homeLocation;
    }
    public Location getHomeLocation() {
        return this.homeLocation;
    }
    public void setType(EntityType type) {
        this.type = type;
    }
    public EntityType getType() {
        return this.type;
    }
    public void setRadius(int radius) {
        this.radius = radius;
    }
    public int getRadius() {
        return this.radius;
    }
}
