import javax.swing.*;
import java.awt.*;
public class LibrarySearch extends JFrame {
    private final JTextField query=new JTextField(18);
    private final JComboBox<String> category=new JComboBox<>(new String[]{"All","Programming","Science"});
    private final DefaultListModel<String> model=new DefaultListModel<>();
    private final JList<String> results=new JList<>(model);
    private final JLabel status=new JLabel("Ready");
    private int page=0;
    private final String[] books={"Java Basics","Algorithms","Physics","Swing GUI","Data Science","Databases"};
    public LibrarySearch(){
        super("Campus Library Search"); setLayout(new BorderLayout(8,8));
        JLabel title=new JLabel("Campus Library",SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD,18f));
        JPanel search=new JPanel(new FlowLayout()); JButton find=new JButton("Search");
        search.add(new JLabel("Title:"));search.add(query);search.add(category);search.add(find);
        JPanel north=new JPanel(new BorderLayout());north.add(title,BorderLayout.NORTH);north.add(search,BorderLayout.SOUTH);
        add(north,BorderLayout.NORTH);add(new JScrollPane(results),BorderLayout.CENTER);
        JPanel nav=new JPanel(new FlowLayout()); JButton prev=new JButton("Prev"),next=new JButton("Next"),reserve=new JButton("Reserve");
        nav.add(prev);nav.add(next);nav.add(reserve);
        JPanel south=new JPanel(new BorderLayout());south.add(nav,BorderLayout.NORTH);south.add(status,BorderLayout.SOUTH);add(south,BorderLayout.SOUTH);
        find.addActionListener(e->{page=0;refresh();});
        prev.addActionListener(e->{page=Math.max(0,page-1);refresh();});
        next.addActionListener(e->{page++;refresh();});
        reserve.addActionListener(e->JOptionPane.showMessageDialog(this,results.getSelectedValue()==null?"Select a book":"Reserved: "+results.getSelectedValue()));
        refresh();setSize(650,420);setDefaultCloseOperation(EXIT_ON_CLOSE);setLocationRelativeTo(null);
    }
    private void refresh(){
        java.util.List<String> filtered=new java.util.ArrayList<>();
        for(String b:books)if(b.toLowerCase().contains(query.getText().trim().toLowerCase()))filtered.add(b);
        int pages=Math.max(1,(filtered.size()+2)/3);page=Math.min(page,pages-1);
        model.clear();for(int i=page*3;i<Math.min(filtered.size(),page*3+3);i++)model.addElement(filtered.get(i));
        status.setText("Page "+(page+1)+" of "+pages+" | "+filtered.size()+" results");
    }
    public static void main(String[] args){SwingUtilities.invokeLater(()->new LibrarySearch().setVisible(true));}
}
