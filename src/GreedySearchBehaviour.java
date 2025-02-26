import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class GreedySearchBehaviour extends CyclicBehaviour {

    GenericOperator[] moves = { new MoveRightOperator(), new MoveUpOperator(),
            new MoveLeftOperator(), new MoveDownOperator() };
    Random rand = new Random();
    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchConversationId("request-action"),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        ACLMessage msg = myAgent.receive(mt);

        if (msg != null) {
            try {
                ACLMessage reply = msg.createReply();

                SimulationState ss = ((ParticipantAgent) myAgent).getParticipantState();
                GenericOperator move = getNextMove(ss);

                if (ss != null) {
                    System.out.println("sending propose operator:" + move.toString());
                    Position pre_pos = ss.getPosition();
                    MapNavigationState pos_state = (MapNavigationState) move.operate(new MapNavigationState(pre_pos));
                    reply.setContentObject(pos_state.position);
                    reply.setPerformative(ACLMessage.PROPOSE);
                }

                myAgent.send(reply);

                MessageTemplate mt2 = MessageTemplate.and(
                        MessageTemplate.MatchConversationId("update-state"),
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                ACLMessage inform_msg = myAgent.blockingReceive(mt2, 10000);
                if (msg != null) {
                    try {
                        SimulationState updatedState = (SimulationState) inform_msg.getContentObject();
                        ((ParticipantAgent) myAgent).setState(updatedState);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public GenericOperator getNextMove(SimulationState ss) {
        Map map = ss.getMap();
        Position currentPosition = ss.getPosition();
        Position goalPosition = findNearestResource(map, currentPosition);
        List<Position> possibleMoves = new LinkedList<>();

        Position right_pos = new Position(currentPosition.x, currentPosition.y + 1);
        possibleMoves.add(right_pos);
        Position up_pos = new Position(currentPosition.x - 1, currentPosition.y);
        possibleMoves.add(up_pos);
        Position left_pos = new Position(currentPosition.x, currentPosition.y - 1);
        possibleMoves.add(left_pos);
        Position down_pos = new Position(currentPosition.x + 1, currentPosition.y);
        possibleMoves.add(down_pos);

        int minDistance = Integer.MAX_VALUE;
        int index = 0;
        int nextMove = 0;

        if (goalPosition != null) {
            for (Position position : possibleMoves) {
                if (!map.isTrapPosition(position)) {
                    int distance = calculateDistance(position, goalPosition);
                    if (distance < minDistance) {
                        minDistance = distance;
                        nextMove = index;
                    }
                }
                index++;
            }
        }else{
            return getRandomMove(ss);
        }
        return moves[nextMove];
    }

    private GenericOperator getRandomMove(SimulationState ss) {
        List<GenericOperator> possibleMoves = new LinkedList<>();
        Position pos = ss.getPosition();
        int numRows = ss.getMap().getNumRows();
        int numCols = ss.getMap().getNumCols();

        if (pos.getY() < numCols - 1) { // Puede moverse a la derecha
            possibleMoves.add(new MoveRightOperator());
        }
        if (pos.getX() > 0) { // Puede moverse hacia arriba
            possibleMoves.add(new MoveUpOperator());
        }
        if (pos.getY() > 0) { // Puede moverse a la izquierda
            possibleMoves.add(new MoveLeftOperator());
        }
        if (pos.getX() < numRows - 1) { // Puede moverse hacia abajo
            possibleMoves.add(new MoveDownOperator());
        }

        if (possibleMoves.isEmpty()) {
            return null; // No hay movimientos posibles
        }

        return possibleMoves.get(rand.nextInt(possibleMoves.size()));
    }


    private Position findNearestResource(Map map, Position currentPosition) {
        List<Position> resources = map.getItemPositions();
        Position nearestResource = null;
        int minDistance = Integer.MAX_VALUE;

        if (!resources.isEmpty()) {
            for (Position resource : resources) {
                int distance = calculateDistance(currentPosition, resource);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestResource = resource;
                }
            }
            return nearestResource;
        }
        return null;
    }

    private int calculateDistance(Position currentPosition, Position resource) {
        return Math.abs(currentPosition.x - resource.x) + Math.abs(currentPosition.y - resource.y);
    }

}