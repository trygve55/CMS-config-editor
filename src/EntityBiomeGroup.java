import java.util.ArrayList;
import java.util.Objects;

public class EntityBiomeGroup {
    private String name;
    private ArrayList<Biome> biomes;

    public String getName() {
        return name;
    }

    public ArrayList<Biome> getBiomes() {
        return biomes;
    }

    public void addBiome(Biome biome) {
        this.biomes.add(biome);
    }

    public void removeBiome(Biome biome) {
        this.biomes.remove(biome);
    }

    public EntityBiomeGroup(String name) {
        this.name = name;
        this.biomes = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityBiomeGroup that = (EntityBiomeGroup) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("EntityBiomeGroup{name='");
        sb.append(name);
        sb.append("', biomes=[");
        for (int i = 0; i < biomes.size();i++) {
            sb.append(biomes.get(i).getName());

            if(i != biomes.size() - 1)
                sb.append(", ");
        }
        sb.append("]}");


        return sb.toString();
    }
}
