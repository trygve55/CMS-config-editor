import java.io.*;
import java.util.ArrayList;

public class ConfigReader {
    private File file;
    private ArrayList<Biome> biomes = new ArrayList<>();
    private ArrayList<EntityBiomeGroup> entityBiomeGroups = new ArrayList<>();

    public ConfigReader(File file) {
        this.file = file;

        BufferedReader br = null;

        try {

            br = new BufferedReader(new FileReader(file));

            boolean inBiomegroupDefaults = false;

            String st;
            while ((st = br.readLine()) != null) {
                if (st.equals("biomegroup-defaults {")) {
                    inBiomegroupDefaults = true;
                } else if (st.equals("}")) {
                    break;
                } else if (inBiomegroupDefaults) {
                    String[] split = st.trim().split(" ", 2);

                    EntityBiomeGroup entityBiomeGroup = new EntityBiomeGroup(split[0].substring(2));

                    for (String s: split[1].substring(1, split[1].length() - 1).split(":")) {

                        if (s.length() == 0)
                            continue;

                        Biome biome = new Biome(s);

                        int biomeIndex = biomes.indexOf(biome);

                        if (biomeIndex != -1) {
                            entityBiomeGroup.addBiome(biomes.get(biomeIndex));
                        } else {
                            biomes.add(biome);
                            entityBiomeGroup.addBiome(new Biome(s));
                        }
                    }

                    entityBiomeGroups.add(entityBiomeGroup);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public File getFile() {
        return file;
    }

    public ArrayList<Biome> getBiomes() {
        return biomes;
    }

    public ArrayList<EntityBiomeGroup> getEntityBiomeGroups() {
        return entityBiomeGroups;
    }

    public int writeConfig() {
        FileWriter fr = null;
        BufferedWriter br = null;

        try{
            fr = new FileWriter(file);
            br = new BufferedWriter(fr);

            br.write("# Configuration file\n" +
                    "\n" +
                    "####################\n" +
                    "# biomegroup-defaults\n" +
                    "####################\n" +
                    "\n" +
                    "biomegroup-defaults {\n");

            for(EntityBiomeGroup e: entityBiomeGroups) {

                br.write("    S:");
                br.write(e.getName());
                br.write(" <");

                for (int i = 0;i < e.getBiomes().size();i++) {

                    br.write(e.getBiomes().get(i).getName());

                    if (i < e.getBiomes().size() - 1)
                        br.write(':');
                }

                br.write(">\n");
            }

            br.write("}\n\n\n");

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }finally{
            try {
                br.close();
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
                return -2;
            }


        }

        return 0;
    }
}
