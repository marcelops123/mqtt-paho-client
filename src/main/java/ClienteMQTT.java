import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.awt.event.ActionListener;
import java.util.Arrays;

public class ClienteMQTT implements MqttCallbackExtended {

    private final String serverURI;
    private MqttClient client;
    private final MqttConnectOptions mqttOptions;

    public ClienteMQTT(String serverURI, String usuario, String senha) {
        this.serverURI = serverURI;

        mqttOptions = new MqttConnectOptions();
        mqttOptions.setMaxInflight(200);
        mqttOptions.setConnectionTimeout(10); // Aumentei o tempo limite para 10 segundos
        mqttOptions.setKeepAliveInterval(10);
        mqttOptions.setAutomaticReconnect(true);
        mqttOptions.setCleanSession(true); // Alterado para sessão limpa

        if (usuario != null && senha != null) {
            mqttOptions.setUserName(usuario);
            mqttOptions.setPassword(senha.toCharArray());
        }
    }

    public IMqttToken subscribe(int qos, IMqttMessageListener gestorMensagemMQTT, String... topicos) {
        if (client == null || topicos.length == 0) {
            return null;
        }
        int tamanho = topicos.length;
        int[] qoss = new int[tamanho];
        IMqttMessageListener[] listners = new IMqttMessageListener[tamanho];

        for (int i = 0; i < tamanho; i++) {
            qoss[i] = qos;
            listners[i] = gestorMensagemMQTT;
        }
        try {
            return client.subscribeWithResponse(topicos, qoss, listners);
        } catch (MqttException ex) {
            System.out.println(String.format("Erro ao se inscrever nos tópicos %s - %s", Arrays.asList(topicos), ex));
            return null;
        }
    }

    public void unsubscribe(String... topicos) {
        if (client == null || !client.isConnected() || topicos.length == 0) {
            return;
        }
        try {
            client.unsubscribe(topicos);
        } catch (MqttException ex) {
            System.out.println(String.format("Erro ao se desinscrever no tópico %s - %s", Arrays.asList(topicos), ex));
        }
    }

    public ActionListener iniciar() {
        try {
            System.out.println("Conectando no broker MQTT em " + serverURI);

            // Verifica se a URI do broker está no formato correto
            if (serverURI == null || !serverURI.startsWith("tcp://")) {
                throw new IllegalArgumentException("A URI do broker é inválida: " + serverURI);
            }

            client = new MqttClient(serverURI, String.format("cliente_java_%d", System.currentTimeMillis()),
                    new MqttDefaultFilePersistence(System.getProperty("java.io.tmpdir")));
            client.setCallback(this);
            client.connect(mqttOptions);
            System.out.println("Conectado com sucesso ao broker MQTT em " + serverURI);
        } catch (MqttException ex) {
            System.out.println("Erro ao se conectar ao broker MQTT: " + serverURI);
            System.out.println("Código do erro: " + ex.getReasonCode());
            ex.printStackTrace(); // Imprime o stack trace completo
        } catch (IllegalArgumentException ex) {
            System.out.println("Erro de argumento: " + ex.getMessage());
            ex.printStackTrace(); // Imprime o stack trace completo
        } catch (Exception ex) {
            System.out.println("Ocorreu um erro inesperado durante a conexão ao broker MQTT.");
            ex.printStackTrace(); // Imprime o stack trace completo
        }
        return null;
    }

    public void finalizar() {
        if (client == null || !client.isConnected()) {
            return;
        }
        try {
            client.disconnect();
            client.close();
        } catch (MqttException ex) {
            System.out.println("Erro ao desconectar do broker mqtt - " + ex);
        }
    }

    public void publicar(String topic, byte[] payload, int qos) {
        publicar(topic, payload, qos, false);
    }

    public synchronized void publicar(String topic, byte[] payload, int qos, boolean retained) {
        try {
            if (client.isConnected()) {
                client.publish(topic, payload, qos, retained);
                System.out.println(String.format("Tópico %s publicado. %dB", topic, payload.length));
            } else {
                System.out.println("Cliente desconectado, não foi possível publicar o tópico " + topic);
            }
        } catch (MqttException ex) {
            System.out.println("Erro ao publicar " + topic + " - " + ex);
        }
    }

    @Override
    public void connectionLost(Throwable thrwbl) {
        System.out.println("Conexão com o broker perdida -" + thrwbl);
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        System.out.println("Cliente MQTT " + (reconnect ? "reconectado" : "conectado") + " com o broker " + serverURI);
        MQTTClientGUI client = new MQTTClientGUI();
        client.clientConnected(true);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {
    }

    @Override
    public void messageArrived(String topic, MqttMessage mm) throws Exception {
    }
}
