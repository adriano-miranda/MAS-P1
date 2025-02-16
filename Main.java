import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class Main {

    public static void main(String[] args) {
        // Configuración del entorno JADE (para crear un contenedor)
        Runtime rt = Runtime.instance();

        Profile p = new ProfileImpl();

        // Crear un contenedor principal con el perfil configurado
        AgentContainer mainContainer = rt.createMainContainer(p);

        try {
            // Crear y poner en marcha el agente 'SimulatorAgent'
            AgentController simulatorAgentController = mainContainer.createNewAgent(
                    "SimulatorAgent", // Nombre del agente
                    "SimulatorAgent", // Nombre de la clase que implementa el agente
                    new Object[] {} // Parámetros para el constructor del agente (vacío)
            );

            simulatorAgentController.start(); // Iniciar el agente
            System.out.println("SimulatorAgent ha sido iniciado.");

            // Crear y poner en marcha los participantes (2 agentes de ejemplo)
            for (int i = 1; i <= 3; i++) {
                final int participantId = i;
                AgentController participantAgentController = mainContainer.createNewAgent(
                        "Participant" + participantId, // Nombre del agente participante
                        "ParticipantAgent", // Clase que representa a los participantes
                        new Object[] {10} // Parámetros adicionales si es necesario, en este caso commitment
                );

                participantAgentController.start(); // Iniciar el agente
                System.out.println("Participant" + participantId + " ha sido iniciado.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}