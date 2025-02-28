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
        p.setParameter(Profile.MAIN_HOST, "localhost");

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

            //The strategies must be one of ["RANDOM", "GREEDY", "SNAKE"]
            // PARTICIPANT 1
            AgentController participantAgentController1 = mainContainer.createNewAgent(
                    "Participant" + "RANDOM", // Nombre del agente participante
                    "ParticipantAgent", // Clase que representa a los participantes
                    new Object[] {20, "RANDOM"} // Parámetros adicionales si es necesario, en este caso commitment
            );
            participantAgentController1.start(); // Iniciar el agente

            // PARTICIPANT 2
            AgentController participantAgentController2 = mainContainer.createNewAgent(
                    "Participant" + "GREEDY",
                    "ParticipantAgent",
                    new Object[] {20, "GREEDY"}
            );
            participantAgentController2.start();

            // PARTICIPANT 3
            AgentController participantAgentController3 = mainContainer.createNewAgent(
                    "Participant" + "SNAKE",
                    "ParticipantAgent",
                    new Object[] {20, "RANDOM"}
            );
            participantAgentController3.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}