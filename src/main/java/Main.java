public class Main {

    static MQTTClientGUI guiUser = new MQTTClientGUI();

    public static void main(String[] args) {
        try {
            guiUser.GuiUser();
        } catch (Exception ex) {
            System.out.println("Erro ao iniciar a interface do cliente MQTT: " + ex.getMessage());
            ex.printStackTrace(); // Imprime o stack trace completo para diagnóstico
        }
    }
}
