import java.util.Objects;

public class Biome {
    private String name;

    public String getName() {
        return name;
    }

    public Biome(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Biome biome = (Biome) o;
        return Objects.equals(name, biome.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Biome{" +
                "name='" + name + '\'' +
                '}';
    }
}
