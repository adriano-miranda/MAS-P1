import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

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

        // Esperar la respuesta del SimulatorAgent
        MessageTemplate mt = MessageTemplate.MatchConversationId("join-simulation-request");
        ACLMessage reply = myAgent.receive(mt);
        if (reply != null) {
            // Si se acepta la solicitud, procesar la respuesta
            try {
                SimulationState initialState = (SimulationState) reply.getContentObject();
                ((ParticipantAgent) myAgent).setState(initialState);
                System.out.println(myAgent.getAID().getLocalName() + " ha sido registrado en la simulación.");
                // Aquí podrías iniciar otro comportamiento relacionado con la simulación
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            block(); // Esperar más mensajes si no se recibe respuesta inmediatamente
        }
    }
}
