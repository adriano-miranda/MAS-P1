import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/* Simulator Agent behaviour to handle participant's registrations requests */
public class GetInitialStateBehaviour extends SimpleBehaviour {

    private boolean received = false;

    public void action() {
        // Define the message template to filter messages
        MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchConversationId("join-simulation-request"),
                MessageTemplate.MatchPerformative(ACLMessage.AGREE)
        );

        // Receive message
        ACLMessage reply = myAgent.receive(mt);

        if (reply != null) {
            try {
                SimulationState initialState = (SimulationState) reply.getContentObject();
                System.out.println("Received AGREE from: " + reply.getSender().getName());
                System.out.println("Initial simulation map: \n" + initialState.getMap().toString());
                System.out.println("Initial simulation position: \n" + initialState.getPosition().toString());

                ((ParticipantAgent) myAgent).setState(initialState);
                received = true;
            } catch (UnreadableException e) {
                e.printStackTrace(); // Manejo de la excepción (puedes registrar el error o tomar otra acción)
            }
        } else {
            block(); // Evita el uso innecesario de CPU
        }
    }

    @Override
    public boolean done() {
        return received; // Stop the behavior after receiving the first message
    }

}