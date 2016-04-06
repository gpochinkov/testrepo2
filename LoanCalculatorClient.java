
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

public class LoanCalculatorClient extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;
    private CalculateLoan service;
    private JTextField interest;
    private JTextField years;
    private JTextField amount;
    private JTextArea log;
    private JComboBox rateList;
    private Random rnd;

    public LoanCalculatorClient(CalculateLoan service) {

        rnd = new Random();

        setTitle(String.format("Client No%d", rnd.nextInt(100)));

        this.service = service;

        JPanel inputP = new JPanel();

        inputP.setLayout(new GridLayout(4, 2));

        inputP.add(new JLabel("Anual Intrest rate:"));
        interest = new JTextField();
        interest.setHorizontalAlignment(JTextField.RIGHT);
        inputP.add(interest);

        inputP.add(new JLabel("Number Of Years:"));
        years = new JTextField();
        years.setHorizontalAlignment(JTextField.RIGHT);
        inputP.add(years);

        inputP.add(new JLabel("Loan Amount:"));
        amount = new JTextField();
        amount.setHorizontalAlignment(JTextField.RIGHT);
        inputP.add(amount);

        rateList=null;
        try {
            rateList = new JComboBox(service.getRates().toArray());
        } catch (RemoteException ex) {
             JOptionPane.showMessageDialog(null, "Exception caught during remote method invocation:\n " + ex.getMessage(), "Alert!", JOptionPane.ERROR_MESSAGE);
        }
        rateList.setSelectedIndex(0);
        inputP.add(rateList);

        JButton submit = new JButton("Submit");
        submit.addActionListener(this);
        inputP.add(submit);

        add(inputP, BorderLayout.NORTH);

        log = new JTextArea();
        log.setEditable(false);
        add(log, BorderLayout.CENTER);
        setSize(400, 250);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        double interestParse;
        try {
            interestParse = Double.parseDouble(this.interest.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Anual Interest Rate трябва да бъде положително число!");
            return;
        }

        int yearsParse;
        try {
            yearsParse = Integer.parseInt(this.years.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Years трябва да бъдат цяло положително число!");
            return;
        }

        double amountParse;
        try {
            amountParse = Double.parseDouble(this.amount.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(null, "Loan amount трябва да бъде положително число, което е по-голямо от 100!");
            return;
        }
        LoanParams params;
        if (amountParse < 100) {
            JOptionPane.showMessageDialog(null, "Loan Amount трябва да бъде повече от 100!");
            return;
        }
        if (interestParse >= 0 && yearsParse >= 0 && amountParse >= 100) {
            params = new LoanParams(getTitle(),interestParse, yearsParse, rateList.getSelectedItem().toString(), amountParse);
        } else {
            JOptionPane.showMessageDialog(null, "Една или няколко от данните са отрицателно число!\n");
            return;
        }

        try {
            LoanData result = service.calculateLoan(params);
            log.setText("");
            displayMessage(String.format("   Anual Intrest Rate: %.2f\n" +
                    "   Number Of Years: %d\n" +
                    "   Loan Amoun: %.2f %s\n" +
                    "      Result:\n" +
                    "\tMonthly Payment: %.2f BGN\n" +
                    "\tTotal Payment: %.2f BGN\n",
                    params.getInrestRate(), params.getYears(),
                    params.getAmount(), params.getCurencyCode(),
                    result.getMonthlyPayment(), result.getTotalPayment()));

        } catch (RemoteException ex) {
            JOptionPane.showMessageDialog(null, "Exception caught during remote method invocation:\n " + ex.getMessage(), "Alert!", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayMessage(final String messageToDisplay) {
        SwingUtilities.invokeLater(
                new Runnable() {

                    public void run() // updates displayArea
                    {
                        log.append(messageToDisplay); // append message
                    } // end method run
                } // end anonymous inner class
                ); // end call to SwingUtilities.invokeLater
    }

	//main
    public static void main(String[] args) {

        try {
            //connect to the registry
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 3232);
            //looking for the service
            CalculateLoan service = (CalculateLoan) registry.lookup("LoanCalulator");
            //start the client app
            LoanCalculatorClient calc = new LoanCalculatorClient(service);


        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Cannot connect to the server!" + ex.getMessage(), "Alert!", JOptionPane.ERROR_MESSAGE);

        }



    }
}
