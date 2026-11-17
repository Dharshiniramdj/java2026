import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

class RegistrationDAO {
    private static final String URL="jdbc:mysql://localhost:3306/campus_events";
    private static final String USER="root", PASSWORD="CHANGE_ME";
    private Connection connect() throws SQLException {return DriverManager.getConnection(URL,USER,PASSWORD);}
    void register(String id,String name,String event) throws SQLException {
        String sql="INSERT INTO registrations(student_id,student_name,event_name) VALUES(?,?,?)";
        try(Connection c=connect();PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,id);p.setString(2,name);p.setString(3,event);p.executeUpdate();
        }
    }
    java.util.List<String[]> list(String event,int offset,int limit) throws SQLException {
        java.util.List<String[]> rows=new ArrayList<>();
        boolean all="All".equals(event);
        String sql="SELECT student_id,student_name,event_name FROM registrations "+
                (all?"":"WHERE event_name=? ")+"ORDER BY id LIMIT ? OFFSET ?";
        try(Connection c=connect();PreparedStatement p=c.prepareStatement(sql)) {
            int pos=1;if(!all)p.setString(pos++,event);
            p.setInt(pos++,limit);p.setInt(pos,offset);
            try(ResultSet rs=p.executeQuery()) {while(rs.next())rows.add(new String[]{rs.getString(1),rs.getString(2),rs.getString(3)});}
        }
        return rows;
    }
}
class RegistrationService {
    private final RegistrationDAO dao=new RegistrationDAO();
    void register(String id,String name,String event) throws SQLException {
        if(id.trim().isEmpty()||name.trim().isEmpty())throw new IllegalArgumentException("ID and name are required");
        if(!id.matches("[A-Za-z0-9_-]{3,20}"))throw new IllegalArgumentException("Invalid student ID");
        dao.register(id.trim(),name.trim(),event);
    }
    java.util.List<String[]> list(String event,int page) throws SQLException {return dao.list(event,page*20,20);}
}
public class EventRegistrationApp extends JFrame {
    private final RegistrationService service=new RegistrationService();
    private final JTextField id=new JTextField(12),name=new JTextField(12);
    private final JComboBox<String> event=new JComboBox<>(new String[]{"Music","Dance","Drama"});
    private final JComboBox<String> filter=new JComboBox<>(new String[]{"All","Music","Dance","Drama"});
    private final DefaultTableModel model=new DefaultTableModel(new String[]{"Student ID","Name","Event"},0);
    private final JTable table=new JTable(model);
    private final JLabel status=new JLabel("Ready");
    private final JButton register=new JButton("Register"),refresh=new JButton("Filter / Refresh"),prev=new JButton("Prev"),next=new JButton("Next"),export=new JButton("Export visible CSV");
    private int page=0;
    public EventRegistrationApp(){
        super("Campus Event Registration");setLayout(new BorderLayout(8,8));
        JMenuBar menu=new JMenuBar();JMenu file=new JMenu("File");JMenuItem exportMenu=new JMenuItem("Export CSV"),exit=new JMenuItem("Exit");
        file.add(exportMenu);file.add(exit);menu.add(file);setJMenuBar(menu);
        JPanel form=new JPanel(new FlowLayout());form.add(new JLabel("Student ID"));form.add(id);form.add(new JLabel("Name"));form.add(name);form.add(event);form.add(register);
        JPanel controls=new JPanel(new FlowLayout());controls.add(new JLabel("Filter"));controls.add(filter);controls.add(refresh);controls.add(prev);controls.add(next);controls.add(export);
        JPanel bottom=new JPanel(new BorderLayout());bottom.add(controls,BorderLayout.NORTH);bottom.add(status,BorderLayout.SOUTH);
        add(form,BorderLayout.NORTH);add(new JScrollPane(table),BorderLayout.CENTER);add(bottom,BorderLayout.SOUTH);
        register.addActionListener(e->registerStudent());
        refresh.addActionListener(e->{page=0;load();});
        filter.addActionListener(e->{page=0;load();});
        prev.addActionListener(e->{page=Math.max(0,page-1);load();});
        next.addActionListener(e->{page++;load();});
        export.addActionListener(e->exportCsv());exportMenu.addActionListener(e->exportCsv());exit.addActionListener(e->dispose());
        setSize(920,450);setDefaultCloseOperation(EXIT_ON_CLOSE);setLocationRelativeTo(null);load();
    }
    private void registerStudent(){
        final String sid=id.getText(),sname=name.getText(),ev=(String)event.getSelectedItem();
        register.setEnabled(false);status.setText("Registering...");
        new SwingWorker<Void,Void>(){
            protected Void doInBackground() throws Exception {service.register(sid,sname,ev);return null;}
            protected void done(){register.setEnabled(true);try{get();status.setText("Registration successful");load();}
            catch(Exception ex){Throwable c=ex.getCause()==null?ex:ex.getCause();String msg=c instanceof SQLIntegrityConstraintViolationException?"Already registered for this event":c.getMessage();
                status.setText("Registration failed");JOptionPane.showMessageDialog(EventRegistrationApp.this,msg);}}
        }.execute();
    }
    private void load(){
        final int requestedPage=page;final String selected=(String)filter.getSelectedItem();
        refresh.setEnabled(false);status.setText("Loading...");
        new SwingWorker<java.util.List<String[]>,Void>(){
            protected java.util.List<String[]> doInBackground() throws Exception{return service.list(selected,requestedPage);}
            protected void done(){refresh.setEnabled(true);try{
                java.util.List<String[]> rows=get();
                if(requestedPage!=page||!selected.equals(filter.getSelectedItem()))return;
                model.setRowCount(0);for(String[] row:rows)model.addRow(row);
                status.setText("Page "+(page+1)+" | "+rows.size()+" rows");
            }catch(Exception ex){status.setText("Load failed: "+ex.getMessage());}}
        }.execute();
    }
    private void exportCsv(){
        JFileChooser chooser=new JFileChooser();if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;
        Path path=chooser.getSelectedFile().toPath();
        try(BufferedWriter out=Files.newBufferedWriter(path)){
            out.write("Student ID,Name,Event");out.newLine();
            for(int r=0;r<model.getRowCount();r++){
                for(int c=0;c<3;c++){if(c>0)out.write(",");String value=String.valueOf(model.getValueAt(r,c));
                    out.write("\""+value.replace("\"","\"\"")+"\"");}out.newLine();
            }
            status.setText("Exported visible page");
        }catch(IOException ex){JOptionPane.showMessageDialog(this,ex.getMessage());}
    }
    public static void main(String[] args){SwingUtilities.invokeLater(()->new EventRegistrationApp().setVisible(true));}
}
