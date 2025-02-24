import java.util.LinkedList;

import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class SimulationManagerBehaviour extends Behaviour {

    public void action() {

        LinkedList<Participant> participants = ((SimulatorAgent)myAgent).getParticipants();

        while (!((SimulatorAgent)myAgent).simulationComplete())
        {
            System.out.printf("\n%s: starting simulation round %d\n", myAgent.getAID().getLocalName(), ((SimulatorAgent)myAgent).roundCount);

            // Loop through all participants and ask for their next action
            for(Participant participant : participants)
            {
                // Ask movement
                ACLMessage reqp = new ACLMessage(ACLMessage.REQUEST);

                reqp.setSender(myAgent.getAID());
                reqp.setConversationId("request-action");
                reqp.setReplyWith("request"+System.currentTimeMillis());
                reqp.addReceiver(participant.getAgentAID());

                System.out.println("\n"+myAgent.getAID().getLocalName()+": Sending ACTION request to "+ participant.getAgentAID().getLocalName());
                myAgent.send(reqp);

                MessageTemplate mt = MessageTemplate.and(
                        MessageTemplate.MatchConversationId("request-action"),
                        MessageTemplate.MatchInReplyTo(reqp.getReplyWith()));
                mt = MessageTemplate.and(mt, MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
                /* agent totally stops until receiving this type of message or no response in 10s,
                   then assuming opportunity for response passed */
                ACLMessage msg = myAgent.blockingReceive(mt, 10000);
                if (msg != null)
                {
                    // Process action
                    try {
                        Position requestedPosition = (Position)msg.getContentObject();

                        // Check if valid, process action, and calculate new participant state
                        SimulationState newSimulationState = processAction(requestedPosition, participant);
                        // Return state
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setConversationId("update-state");

                        // setContent() and setContentObject both write to the same field of the ACLMessage
                        // hence no possible to set both of them separatedly
                        reply.setContentObject(newSimulationState);
                        myAgent.send(reply);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            } // end processing actions for this round

            // Show simulation state
            ((SimulatorAgent)myAgent).showOverallState();

            // Increase round counter
            ((SimulatorAgent)myAgent).roundCount++;

            // Check wheter map needs to be updated
            // Notice that next request for decision is based on the preceding state
            // Thus effectively meaning possible wrong decision,
            // regardless of client agent's correspsonding commitment configurations
            ((SimulatorAgent)myAgent).checkMapMustChange();
        }
    }

    public boolean done() {
        return ((SimulatorAgent)myAgent).simulationComplete();
    }

    private LinkedList<Position> getParticipantsPositions()
    {
        LinkedList<Participant> participants = ((SimulatorAgent)myAgent).getParticipants();
        LinkedList<Position> participantsPos = new LinkedList<Position>();

        for(Participant participant : participants)
        {
            participantsPos.add(participant.getSimulationState().getPosition());
        }

        return participantsPos;
    }

    private boolean occupiedByAgentPosition(Position pos)
    {
        return getParticipantsPositions().contains(pos);
    }

    private SimulationState processAction(Position newPosition, Participant participant)
    {
        // Obtener el estado actual del participante
        SimulationState newState = participant.getSimulationState();
        Map currentMap = ((SimulatorAgent)myAgent).getCurrentMap();

        // Verificar si el movimiento es válido
        boolean validRequest = isValidMovement(newPosition, false) && !occupiedByAgentPosition(newPosition);

        // Enviar un mensaje al participante indicando si el movimiento es válido o no
        ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
        reply.setConversationId("movement-validation");
        reply.addReceiver(participant.getAgentAID());


        if (validRequest) {
            System.out.println("Requested position " + newPosition.toString() + " is valid");
            reply.setContent("VALID");
        } else {
            System.out.println("Requested position " + newPosition.toString() + " is invalid");
            System.out.println("Requested position " + newPosition.toString() + " is invalid");
            reply.setContent("INVALID");
            newPosition = participant.getSimulationState().getPosition(); // No se mueve
        }
        myAgent.send(reply);

        int remainingCommitmentSteps = participant.decreaseCommitmentSteps(1);

        // Procesar el movimiento válido y actualizar el estado
        if (validRequest) {
            // Verificar si el movimiento lleva a un item o trampa
            if (currentMap.isItemPosition(newPosition)) {
                participant.increaseItemCounter(1);
                currentMap.clearPosition(newPosition);
                currentMap.generateNewItem();
            } else if (currentMap.isTrapPosition(newPosition)) {
                participant.increaseTrapCounter(1);
                newPosition = participant.getSimulationState().getPosition(); // Queda atrapado en su posición
            }
        } else {
            newPosition = participant.getSimulationState().getPosition(); // No se mueve
        }

        // Crear un nuevo estado con la posición actualizada
        if (remainingCommitmentSteps == 0) {
            try {
                newState = new SimulationState((Map) currentMap.clone(), newPosition);
            } catch (Exception e) {
                e.printStackTrace();
            }
            participant.resetCommitmentSteps();
        } else {
            newState = new SimulationState(participant.getSimulationState().getMap(), newPosition);
        }

        participant.updateState(newState); // Actualizar el estado del participante

        return newState;
    }


    private boolean isValidMovement(Position pos, boolean avoidTraps)
    {
        boolean valid = true;

        valid &= ((SimulatorAgent)myAgent).getCurrentMap().withinMapLimits(pos);
        if (avoidTraps)
            valid &= !((SimulatorAgent)myAgent).getCurrentMap().isTrapPosition(pos);

        return valid;
    }
}