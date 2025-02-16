import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.behaviours.Behaviour;
import java.io.Serializable;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.Random;

public class RequestActionBehaviour extends CyclicBehaviour {

    public void action() {
        //Si recibo un request
        while (((ParticipantAgent) myAgent).getRegistered()){
            System.out.println("Action, not registered agent yet");
        }
        try {
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchConversationId("request-action"),
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
            ACLMessage msg = myAgent.receive(mt);
            //Envio una respuesta
            if (msg != null) {

                Participant participant = (Participant) myAgent;
                SimulationState simulationState = participant.getSimulationState(); // Accedemos directamente
                if(simulationState!=null) {
                    Position currentPosition = simulationState.getPosition();


                    Position[] possibleMoves = {
                            new Position(currentPosition.x + 1, currentPosition.y),
                            new Position(currentPosition.x - 1, currentPosition.y),
                            new Position(currentPosition.x, currentPosition.y + 1),
                            new Position(currentPosition.x, currentPosition.y - 1)
                    };

                    Position newPosition = possibleMoves[new Random().nextInt(possibleMoves.length)];

                    ACLMessage response = new ACLMessage(ACLMessage.INFORM);
                    response.setSender(participant.getAgentAID());
                    response.setConversationId("request-action");
                    response.addReceiver(new AID("SimulatorAgent", AID.ISLOCALNAME));
                    response.setReplyWith(msg.getReplyWith());
                    response.setContent(newPosition.toString());

                    myAgent.send(response);
                    System.out.println(myAgent.getAID().getLocalName() + " sent new random position: " + newPosition);
                }
            } else {
                block();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}