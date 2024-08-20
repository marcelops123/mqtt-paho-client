import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;

public class MQTTClientGUI {
    static JLabel label;
    static JButton button;
    private JFrame frame;
    private JTextField brokerField;
    private JTextField urlField;
    private JTextField userField;
    private JTextField passField;
    private JTextField topicField;
    private JTextField messageField;
    private JTextArea messageArea;
    static JButton publishButton;

    static ClienteMQTT client;

    public void GuiUser() {
        frame = new JFrame("MQTT Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);

        brokerField = new JTextField();
        urlField = new JTextField();
        userField = new JTextField();
        passField = new JTextField();
        topicField = new JTextField();
        messageArea = new JTextArea();
        messageField = new JTextField();
        button = new JButton("Conectar");
        publishButton = new JButton("Publicar");

        JPanel panel = new JPanel(new GridLayout(9, 1));
        panel.add(new JLabel("Broker:"));
        panel.add(brokerField);
        panel.add(new JLabel("URL:"));
        panel.add(urlField);
        panel.add(new JLabel("User:"));
        panel.add(userField);
        panel.add(new JLabel("Senha:"));
        panel.add(passField);
        panel.add(new JLabel("Topic:"));
        panel.add(topicField);
        panel.add(new JLabel("Mensagem:"));
        panel.add(messageField);
        panel.add(button);
        panel.add(publishButton);
        publishButton.setEnabled(false);
        frame.add(panel, BorderLayout.NORTH);
        frame.add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.setVisible(true);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ("Conectado!".equals(button.getText())) {
                    client.finalizar();
                    button.setText("Conectar");
                    publishButton.setEnabled(false);
                } else {
                    // Validação básica dos campos de entrada
                    String url = urlField.getText();
                    String user = userField.getText();
                    String pass = passField.getText();

                    if (url.isEmpty() || !url.startsWith("tcp://")) {
                        JOptionPane.showMessageDialog(frame, "Por favor, insira uma URL válida para o broker (deve começar com tcp://).", "Erro de Validação", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    try {
                        client = new ClienteMQTT(url, user.isEmpty() ? null : user, pass.isEmpty() ? null : pass);
                        client.iniciar();
                        new Ouvinte(client, "br/com/paulocollares/#", 0);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(frame, "Erro ao tentar conectar ao broker MQTT: " + ex.getMessage(), "Erro de Conexão", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace(); // Para diagnóstico adicional
                    }
                }
            }
        });

        publishButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String mensagem = messageField.getText() + " - Horário: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(System.currentTimeMillis());
                    client.publicar(topicField.getText(), mensagem.getBytes(), 0);
                    messageArea.append("Mensagem publicada: " + mensagem + "\n");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Erro ao publicar mensagem: " + ex.getMessage(), "Erro de Publicação", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace(); // Para diagnóstico adicional
                }
            }
        });
    }

    public void receivedText(String texto) {
        messageArea.append("Mensagem recebida: " + texto + "\n");
    }

    public void clientConnected(boolean connected) {
        if (connected) {
            button.setText("Conectado!");
            publishButton.setEnabled(true);
        }
    }
}
