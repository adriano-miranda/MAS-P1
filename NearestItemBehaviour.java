import java.util.List;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class NearestItemBehaviour extends CyclicBehaviour {
    private final GenericOperator[] moves = {
            new MoveRightOperator(), new MoveUpOperator(),
            new MoveLeftOperator(), new MoveDownOperator()
    };

    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchConversationId("request-action"),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        ACLMessage msg = myAgent.receive(mt);
        if (msg != null) {
            try {
                ACLMessage reply = msg.createReply();
                SimulationState state = ((ParticipantAgent) myAgent).getParticipantState();

                if (state != null) {
                    GenericOperator bestMove = determineBestMove(state);
                    Position newPos = ((MapNavigationState) bestMove.operate(new MapNavigationState(state.getPosition()))).position;

                    reply.setContentObject(newPos);
                    reply.setPerformative(ACLMessage.PROPOSE);
                    myAgent.send(reply);
                }

                // Esperar validación del movimiento
                MessageTemplate mtValidation = MessageTemplate.MatchConversationId("movement-validation");
                ACLMessage validationMsg = myAgent.blockingReceive(mtValidation, 5000);

                if (validationMsg != null && validationMsg.getContent().equals("INVALID")) {
                    System.out.println(myAgent.getLocalName() + ": Received INVALID movement response, staying in place.");
                } else {
                    // Recibir el estado actualizado
                    MessageTemplate mtUpdate = MessageTemplate.MatchConversationId("update-state");
                    ACLMessage informMsg = myAgent.blockingReceive(mtUpdate, 5000);

                    if (informMsg != null) {
                        try {
                            SimulationState updatedState = (SimulationState) informMsg.getContentObject();
                            ((ParticipantAgent) myAgent).setState(updatedState);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private GenericOperator determineBestMove(SimulationState state) {
        Position currentPosition = state.getPosition();
        Map map = state.getMap();
        List<Position> items = map.getItemPositions();
        Position closestItem = findClosestItem(items, currentPosition);

        if (closestItem == null) return null;

        int dx = closestItem.x - currentPosition.x;
        int dy = closestItem.y - currentPosition.y;

        if (Math.abs(dx) > Math.abs(dy)) {
            System.out.println();
            return dx > 0 ? new MoveRightOperator() : new MoveLeftOperator();
        } else {
            return dy > 0 ? new MoveDownOperator() : new MoveUpOperator();
        }
    }

    private Position findClosestItem(List<Position> items, Position currentPosition) {
        Position closest = null;
        int minDistance = Integer.MAX_VALUE;

        for (Position item : items) {
            int distance = Math.abs(item.x - currentPosition.x) + Math.abs(item.y - currentPosition.y);
            if (distance < minDistance) {
                minDistance = distance;
                closest = item;
            }
        }

        return closest;
    }
}
