import darrylbu.renderer.VerticalTableHeaderCellRenderer;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;

public class EntityBiomeGroupWindow extends JFrame {

    private JScrollPane tableScroll;
    private JTable table;
    private DefaultTableModel model;
    private JButton loadConfigButton = new JButton("Open file");
    private JButton saveConfigButton = new JButton("Save");
    private JButton addEntityBiomeGroupButton = new JButton("Add new Entity Biome Group");
    private JButton addBiomeButton = new JButton("Add new biome");
    private JLabel tipLabel = new JLabel("Click on a checkbox to change the spawn of the creature in that biome.");

    private TableCellRenderer headerRenderer = new VerticalTableHeaderCellRenderer();

    private Object[][] data = new Object[][]{};
    private String[] columns = new String[]{};

    private ConfigReader config;
    private ArrayList<Biome> biomes;
    private ArrayList<EntityBiomeGroup> entityBiomeGroups;


    public EntityBiomeGroupWindow() throws HeadlessException {
        super("Custom Mob Spawner Config Editor");

        //Prepare table start
        model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == 0) ? String.class : Boolean.class;
            }
        };

        table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) c.setBackground(row % 2 == 0 ? getBackground() : new Color(229, 229, 229));
                return c;
            }
        };

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setReorderingAllowed(false);

        tableScroll = new JScrollPane(table);
        //Prepare table end

        //Add listener start
        loadConfigButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileSelector();
            }
        });

        saveConfigButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (config.writeConfig() == 0)
                    JOptionPane.showMessageDialog(tableScroll, "File Saved");
            }
        });

        addBiomeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = JOptionPane.showInputDialog(tableScroll,"Name of new biome:");
                if (input != null && input.length() > 0)
                    addBiome(input);
            }
        });

        table.addMouseListener(new JTableMouseListener());
        //Add listener end

        //Set buttons start
        addBiomeButton.setEnabled(false);
        saveConfigButton.setEnabled(false);
        addEntityBiomeGroupButton.setEnabled(false);
        //Set buttons end

        //Building page start
        Panel topRowPanel = new Panel(new FlowLayout());
        topRowPanel.add(loadConfigButton);
        topRowPanel.add(saveConfigButton);
        topRowPanel.add(addBiomeButton);
        topRowPanel.add(addEntityBiomeGroupButton);

        setLayout(new BorderLayout());
        add(topRowPanel, BorderLayout.PAGE_START);
        add(tableScroll, BorderLayout.CENTER);
        add(tipLabel, BorderLayout.PAGE_END);
        //Building page end

        setMinimumSize(new Dimension(800,600));
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void fileSelector() {
        String defaultDirectory = null;

        //Default for Linux
        defaultDirectory = System.getProperty("user.home") + "\\.minecraft\\config\\CustomSpawner\\overworld";

        //Detect Windows
        if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0)
            defaultDirectory = System.getProperty("user.home") + "\\Appdata\\Roaming\\.minecraft\\config\\CustomSpawner\\overworld";

        //Detect MacOS
        if (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0)
            defaultDirectory = System.getProperty("user.home") + "\\Library\\Application Support\\minecraft\\config\\CustomSpawner\\overworld";

        //Check if directory exists
        if (!new File(defaultDirectory).exists()) {
            defaultDirectory = System.getProperty("user.home");
            JOptionPane.showMessageDialog(tableScroll, "Default directory not found. Did you run Minecraft at least once after installing Custom Mob Spawner?");
        }

        final JFileChooser fc = new JFileChooser(defaultDirectory);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new FileFilter() {

            public String getDescription() {
                return "EntityBiomeGroups.cfg";
            }

            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                } else {
                    return f.getName().endsWith("EntityBiomeGroups.cfg");
                }
            }
        });

        int returnVal = fc.showOpenDialog(this);

        File file = fc.getSelectedFile();

        if (returnVal == 0) {
            loadConfig(file);
        }
    }

    private void updateTable() {
        columns = new String[biomes.size() + 1];
        columns[0] = "EntityBiomeGroup";
        for (int i = 0;i < biomes.size();i++)
            columns[i + 1] = biomes.get(i).getName();


        data = new Object[entityBiomeGroups.size()][biomes.size() + 1];
        for (int i = 0;i < entityBiomeGroups.size();i++) {
            data[i] = new Object[biomes.size() + 1];
            data[i][0] = entityBiomeGroups.get(i).getName();

            for (int j = 0;j < biomes.size();j++) {
                data[i][j + 1] = entityBiomeGroups.get(i).getBiomes().contains(biomes.get(j));
            }
        }

        if (table != null) {
            DefaultTableModel dm = (DefaultTableModel) table.getModel();
            dm.setDataVector(data, columns);
            // notifies the JTable that the model has changed

            table.getColumnModel().getColumn(0).setMinWidth(250);

            for (int i = 1; i < table.getColumnModel().getColumnCount();i++) {
                table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
                table.getColumnModel().getColumn(i).setMaxWidth(14);
            }
        }

        addBiomeButton.setEnabled(true);
        saveConfigButton.setEnabled(true);
        addEntityBiomeGroupButton.setEnabled(true);
    }

    private void loadConfig(File file) {
        config = new ConfigReader(file);

        biomes = config.getBiomes();
        entityBiomeGroups = config.getEntityBiomeGroups();

        updateTable();
    }

    private void addBiome(String name) {
        biomes.add(new Biome(name));

        updateTable();
    }

    class JTableMouseListener implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (table.getSelectedColumn() == 0) {

            } else if (data[table.getSelectedRow()][table.getSelectedColumn()].equals(true)) {
                entityBiomeGroups.get(table.getSelectedRow()).removeBiome(biomes.get(table.getSelectedColumn() - 1));
                data[table.getSelectedRow()][table.getSelectedColumn()] = false;
            } else {
                entityBiomeGroups.get(table.getSelectedRow()).addBiome(biomes.get(table.getSelectedColumn() - 1));
                data[table.getSelectedRow()][table.getSelectedColumn()] = true;
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }
}
