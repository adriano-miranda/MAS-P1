import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class GreedySearchBehaviour extends CyclicBehaviour {

    Position last_visited;
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
                    Position pre_pos = ss.getPosition();
                    if (!((ParticipantAgent) myAgent).visitedPositions.contains(pre_pos)){
                        ((ParticipantAgent) myAgent).visitedPositions.add(new Position(pre_pos.x,
                                pre_pos.y));
                    }

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
        int count_visited = 0;
        if (goalPosition != null) {
            for (Position position : possibleMoves) {
                if (!map.isTrapPosition(position) && !((ParticipantAgent) myAgent).visitedPositions.contains(position) &&
                map.withinMapLimits(position)) { // Check if the position has been visited
                    int distance = calculateDistance(position, goalPosition);
                    if (distance < minDistance) {
                        minDistance = distance;
                        nextMove = index;
                    }
                }else{
                    count_visited++;
                }
                index++;
            }

            if (count_visited == 4){
                ((ParticipantAgent) myAgent).visitedPositions.clear();
                return getRandomMove(ss);
            }
        } else {
            return getRandomMove(ss);
        }
        return moves[nextMove];
    }

    private GenericOperator getRandomMove(SimulationState ss) {
        List<GenericOperator> possibleMoves = new LinkedList<>();
        Position pos = ss.getPosition();
        int numRows = ss.getMap().getNumRows();
        int numCols = ss.getMap().getNumCols();

        // Move Right
        Position rightPos = new Position(pos.getX(), pos.getY() + 1);
        if (pos.getY() < numCols - 1 && !ss.getMap().isTrapPosition(rightPos)) { // Check for trap
            possibleMoves.add(new MoveRightOperator());
        }

        // Move Up
        Position upPos = new Position(pos.getX() - 1, pos.getY());
        if (pos.getX() > 0 && !ss.getMap().isTrapPosition(upPos)) { // Check for trap
            possibleMoves.add(new MoveUpOperator());
        }

        // Move Left
        Position leftPos = new Position(pos.getX(), pos.getY() - 1);
        if (pos.getY() > 0 && !ss.getMap().isTrapPosition(leftPos)) { // Check for trap
            possibleMoves.add(new MoveLeftOperator());
        }

        // Move Down
        Position downPos = new Position(pos.getX() + 1, pos.getY());
        if (pos.getX() < numRows - 1 && !ss.getMap().isTrapPosition(downPos)) { // Check for trap
            possibleMoves.add(new MoveDownOperator());
        }

        // If no valid moves are left, return null
        if (possibleMoves.isEmpty()) {
            return null; // No valid movements
        }

        // Return a random valid move
        return possibleMoves.get(rand.nextInt(possibleMoves.size()));
    }

    private Position getPositionForMove(SimulationState ss, GenericOperator move) {
        Position currentPosition = ss.getPosition();
        if (move instanceof MoveRightOperator) {
            return new Position(currentPosition.x, currentPosition.y + 1);
        } else if (move instanceof MoveUpOperator) {
            return new Position(currentPosition.x - 1, currentPosition.y);
        } else if (move instanceof MoveLeftOperator) {
            return new Position(currentPosition.x, currentPosition.y - 1);
        } else if (move instanceof MoveDownOperator) {
            return new Position(currentPosition.x + 1, currentPosition.y);
        }
        return null;
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
