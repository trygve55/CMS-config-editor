import darrylbu.renderer.VerticalTableHeaderCellRenderer;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

public class EntityBiomeGroupWindow extends JFrame {

    private JScrollPane tableScroll;
    private JTable table, tableFixed;
    private DefaultTableModel model, modelFixed;
    private JButton loadConfigButton = new JButton("Open file");
    private JButton saveConfigButton = new JButton("Save");
    private JButton addEntityBiomeGroupButton = new JButton("Add new Entity Biome Group");
    private JButton addBiomeButton = new JButton("Add new biome");
    private JLabel tipLabel = new JLabel("Click on a checkbox to change the spawn of the creature in that biome.");

    private TableCellRenderer headerRenderer = new VerticalTableHeaderCellRenderer();
    private TableCellRenderer headerRendererHighlight = new VerticalTableHeaderCellRenderer(Color.cyan);

    private Object[][] data = new Object[][]{};
    private String[] columns = new String[]{};

    private ConfigReader config;
    private ArrayList<Biome> biomes;
    private ArrayList<EntityBiomeGroup> entityBiomeGroups;

    private int lastHoverRow = -1, lastHoverColumn = -1;


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
                c.setBackground((lastHoverRow == row && lastHoverColumn != 0 && column == 0) ? Color.cyan : new Color(255, 255, 255));
                return c;
            }
        };

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.setColumnSelectionAllowed(true);
        table.setRowSelectionAllowed(true);

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

        //Change values
        table.addMouseListener(new JTableMouseListener());

        //Update highlight
        table.addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                updateHighlight(e);
            }
        });
        //Add listener end

        //Fixed Panel Start
        modelFixed = new DefaultTableModel(data, columns);
        tableFixed = new JTable(modelFixed);
        //Fixed Panel End

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

        Panel tablePanel = new Panel(new BorderLayout());
        tablePanel.add(tableScroll, BorderLayout.CENTER);
        //tablePanel.add(tableFixed, BorderLayout.WEST);

        setLayout(new BorderLayout());
        add(topRowPanel, BorderLayout.PAGE_START);
        add(tablePanel, BorderLayout.CENTER);
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

    private void updateHighlight(MouseEvent e) {
        Point p = e.getPoint();
        int row = table.rowAtPoint(p);
        int col = table.columnAtPoint(p);

        if (row != lastHoverRow) {
            int oldRow = lastHoverRow;
            lastHoverRow = row;

            ((AbstractTableModel) table.getModel()).fireTableCellUpdated(row, 0);
            ((AbstractTableModel) table.getModel()).fireTableCellUpdated(oldRow, 0);



            //Ugly hack start
            table.getColumnModel().getColumn(0).setMinWidth(250);
            for (int i = 1; i < table.getColumnModel().getColumnCount();i++) {
                table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
                table.getColumnModel().getColumn(i).setMaxWidth(14);
            }
            //Ugly hack end
        }

        if (col != lastHoverColumn) {
            int oldCol = lastHoverColumn;
            lastHoverColumn = col;

            if (col != 0)
                table.getColumnModel().getColumn(col).setHeaderRenderer(headerRendererHighlight);

            if (oldCol != -1 && oldCol != 0)
                table.getColumnModel().getColumn(oldCol).setHeaderRenderer(headerRenderer);

            table.getTableHeader().repaint();
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

        DefaultTableModel dm = (DefaultTableModel) table.getModel();
        dm.setDataVector(data, columns);

        /*
        Object[][] fixedData = new Object[entityBiomeGroups.size()][1];
        for (int i = 0;i < entityBiomeGroups.size();i++) {
            //data[i] = new Object[biomes.size() + 1];
            fixedData[i][0] = entityBiomeGroups.get(i).getName();
        }

        DefaultTableModel dm2 = (DefaultTableModel) tableFixed.getModel();
        dm2.setDataVector(fixedData, new String[]{columns[0]});
        */

        table.getColumnModel().getColumn(0).setMinWidth(250);

        for (int i = 1; i < table.getColumnModel().getColumnCount();i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
            table.getColumnModel().getColumn(i).setMaxWidth(14);
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
