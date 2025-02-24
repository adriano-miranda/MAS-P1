import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class RegisterRequestBehaviour extends OneShotBehaviour {
    @Override
    public void action() {
        // Crear el mensaje de solicitud
        ACLMessage requestMessage = new ACLMessage(ACLMessage.REQUEST);
        requestMessage.setSender(myAgent.getAID());
        requestMessage.setConversationId("join-simulation-request");
        requestMessage.addReceiver(new jade.core.AID("SimulatorAgent", jade.core.AID.ISLOCALNAME));

        // Definir el contenido del mensaje (por ejemplo, el compromiso del agente)
        int agentCommitment = 10; // Este valor puede cambiar según el agente
        requestMessage.setContent(String.valueOf(agentCommitment));

        // Enviar el mensaje al SimulatorAgent
        myAgent.send(requestMessage);
        System.out.println(myAgent.getAID().getLocalName() + " ha enviado solicitud para unirse a la simulación.");

    }
}
