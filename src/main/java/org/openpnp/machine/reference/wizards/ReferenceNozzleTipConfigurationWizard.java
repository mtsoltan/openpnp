/*
 * Copyright (C) 2011 Jason von Nieda <jason@vonnieda.org>
 * 
 * This file is part of OpenPnP.
 * 
 * OpenPnP is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * OpenPnP is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with OpenPnP. If not, see
 * <http://www.gnu.org/licenses/>.
 * 
 * For more information about OpenPnP visit http://openpnp.org
 */

package org.openpnp.machine.reference.wizards;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import org.openpnp.ConfigurationListener;
import org.openpnp.gui.components.ComponentDecorators;
import org.openpnp.gui.support.AbstractConfigurationWizard;
import org.openpnp.gui.support.DoubleConverter;
import org.openpnp.gui.support.IntegerConverter;
import org.openpnp.gui.support.LengthConverter;
import org.openpnp.machine.reference.ReferenceNozzleTip;
import org.openpnp.model.Configuration;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

public class ReferenceNozzleTipConfigurationWizard extends AbstractConfigurationWizard {
    private final ReferenceNozzleTip nozzleTip;
    private JPanel panelDwellTime;
    private JLabel lblPickDwellTime;
    private JLabel lblPlaceDwellTime;
    private JLabel lblDwellTime;
    private JTextField pickDwellTf;
    private JTextField placeDwellTf;

    private Set<org.openpnp.model.Package> compatiblePackages = new HashSet<>();
    private JPanel panel;
    private JLabel lblName;
    private JTextField nameTf;


    public ReferenceNozzleTipConfigurationWizard(ReferenceNozzleTip nozzleTip) {
        this.nozzleTip = nozzleTip;
        
        panel = new JPanel();
        panel.setBorder(new TitledBorder(null, "Properties", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        contentPanel.add(panel);
        panel.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,},
            new RowSpec[] {
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,}));
        
        lblName = new JLabel("Name");
        panel.add(lblName, "2, 2, right, default");
        
        nameTf = new JTextField();
        panel.add(nameTf, "4, 2, fill, default");
        nameTf.setColumns(10);
        
        panelDwellTime = new JPanel();
        panelDwellTime.setBorder(new TitledBorder(null, "Dwell Times", TitledBorder.LEADING, TitledBorder.TOP, null, null));
        contentPanel.add(panelDwellTime);
        panelDwellTime.setLayout(new FormLayout(new ColumnSpec[] {
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC,
                FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"),},
            new RowSpec[] {
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,
                FormSpecs.RELATED_GAP_ROWSPEC,
                FormSpecs.DEFAULT_ROWSPEC,}));
          
        lblPickDwellTime = new JLabel("Pick Dwell Time (ms)");
        panelDwellTime.add(lblPickDwellTime, "2, 2, right, default");
        
        pickDwellTf = new JTextField();
        panelDwellTime.add(pickDwellTf, "4, 2");
        pickDwellTf.setColumns(10);
        
        lblPlaceDwellTime = new JLabel("Place Dwell Time (ms)");
        panelDwellTime.add(lblPlaceDwellTime, "2, 4, right, default");
        
        placeDwellTf = new JTextField();
        panelDwellTime.add(placeDwellTf, "4, 4");
        placeDwellTf.setColumns(10);
        
        CellConstraints cc = new CellConstraints();
        lblDwellTime = new JLabel("Note: Total Dwell Time is the sum of Nozzle Dwell Time plus the Nozzle Tip Dwell Time.");
        panelDwellTime.add(lblDwellTime, cc.xywh(2, 6, 5, 1));
       
    }
    

    @Override
    public void createBindings() {
        LengthConverter lengthConverter = new LengthConverter();
        IntegerConverter intConverter = new IntegerConverter();
        DoubleConverter doubleConverter = new DoubleConverter(Configuration.get().getLengthDisplayFormat());

        addWrappedBinding(nozzleTip, "name", nameTf, "text");
        
        addWrappedBinding(nozzleTip, "pickDwellMilliseconds", pickDwellTf, "text", intConverter);
        addWrappedBinding(nozzleTip, "placeDwellMilliseconds", placeDwellTf, "text", intConverter);
        
        ComponentDecorators.decorateWithAutoSelect(nameTf);
        
        ComponentDecorators.decorateWithAutoSelect(pickDwellTf);
        ComponentDecorators.decorateWithAutoSelect(placeDwellTf);
    }

    @Override
    protected void loadFromModel() {
        compatiblePackages.clear();
        compatiblePackages.addAll(nozzleTip.getCompatiblePackages());
        super.loadFromModel();
    }

    @Override
    protected void saveToModel() {
        nozzleTip.setCompatiblePackages(compatiblePackages);
        super.saveToModel();
    }

    public class PackagesTableModel extends AbstractTableModel {
        private String[] columnNames = new String[] {"Package Id", "Compatible?"};
        private List<org.openpnp.model.Package> packages;

        public PackagesTableModel() {
            Configuration.get().addListener(new ConfigurationListener.Adapter() {
                public void configurationComplete(Configuration configuration) throws Exception {
                    refresh();
                }
            });
        }

        public void refresh() {
            packages = new ArrayList<>(Configuration.get().getPackages());
            fireTableDataChanged();
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return (packages == null) ? 0 : packages.size();
        }

        public org.openpnp.model.Package getPackage(int index) {
            return packages.get(index);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            try {
                org.openpnp.model.Package pkg = packages.get(rowIndex);
                if (columnIndex == 1) {
                    if ((Boolean) aValue) {
                        compatiblePackages.add(pkg);
                    }
                    else {
                        compatiblePackages.remove(pkg);
                    }
                    notifyChange();
                }
            }
            catch (Exception e) {
                // TODO: dialog, bad input
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 1) {
                return Boolean.class;
            }
            return super.getColumnClass(columnIndex);
        }

        public Object getValueAt(int row, int col) {
            switch (col) {
                case 0:
                    return packages.get(row).getId();
                case 1:
                    return compatiblePackages.contains(packages.get(row));
                default:
                    return null;
            }
        }
    }
}
