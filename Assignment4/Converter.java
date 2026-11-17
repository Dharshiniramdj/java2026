import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
public class Converter extends JFrame  {
    private final JTextField amount=new JTextField(10);
    private final JComboBox<String> target=new JComboBox<>(new String[]{"USD","EUR"});
    private final JLabel result=new JLabel(" ");
    private final JButton button=new JButton("Convert");
    public Converter(){
        super("Currency Converter – Converter");setLayout(new FlowLayout());
        add(new JLabel("INR"));add(amount);add(target);add(button);add(result);
        ActionListener listener=e->convert();button.addActionListener(listener);amount.addActionListener(listener);
        addWindowListener(new WindowAdapter(){@Override public void windowClosing(WindowEvent e){dispose();}});
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);pack();setLocationRelativeTo(null);
    }
    private void convert(){
        try {
            double value=Double.parseDouble(amount.getText().trim());
            if(!Double.isFinite(value)||value<0)throw new NumberFormatException();
            double rate="USD".equals(target.getSelectedItem())?0.012:0.011;
            String text=String.format("%.2f %s",value*rate,target.getSelectedItem());
            result.setText(text);JOptionPane.showMessageDialog(this,text);
        }catch(NumberFormatException ex){result.setText("Invalid amount");JOptionPane.showMessageDialog(this,"Invalid amount");}
    }

    public static void main(String[] args){SwingUtilities.invokeLater(()->new Converter().setVisible(true));}
}
