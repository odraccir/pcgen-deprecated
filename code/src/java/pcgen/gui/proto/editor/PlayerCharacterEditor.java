/*
 * PlayerCharacterEditor.java
 *
 * Created on February 14, 2008, 8:31 PM
 */
package pcgen.gui.proto.editor;

/**
 *
 * @author  Connor Petty <mistercpp2000@gmail.com>
 */
public class PlayerCharacterEditor extends javax.swing.JFrame {

    /** Creates new form PlayerCharacterEditor */
    public PlayerCharacterEditor() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTableEx1 = new pcgen.gui.util.JTableEx();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTableEx1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"sdfa", "tegh", "wvczs", "yuue"},
                {"ghjd", "asdf", "trtw", "sfdgj"},
                {null, "asdrj", "kjffs", "sfgj"},
                {"dfg", "cbnc", "mnvx", "xssd"}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTableEx1);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new PlayerCharacterEditor().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private pcgen.gui.util.JTableEx jTableEx1;
    // End of variables declaration//GEN-END:variables
    
}
