import java.util.List;
import java.util.ArrayList;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
public class NearestItemBehaviour extends CyclicBehaviour {
    @Override
    public void action() {
        ACLMessage msg = receiveMessage("request-action", ACLMessage.REQUEST);
        if (msg != null) {
            handleRequest(msg);
        }
    }

    private ACLMessage receiveMessage(String conversationId, int performative) {
        MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchConversationId(conversationId),
                MessageTemplate.MatchPerformative(performative)
        );
        return myAgent.receive(mt);
    }

    private void handleRequest(ACLMessage msg) {
        try {
            SimulationState state = ((ParticipantAgent) myAgent).getParticipantState();
            Position nextMove = determineBestMove(state);

            if (state != null && nextMove != null) {
                sendMoveProposal(msg, nextMove);
                updateAgentState();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMoveProposal(ACLMessage msg, Position nextMove) throws Exception {
        ACLMessage reply = msg.createReply();
        reply.setContentObject(nextMove);
        reply.setPerformative(ACLMessage.PROPOSE);
        myAgent.send(reply);
    }

    private void updateAgentState() {
        ACLMessage informMsg = receiveMessage("update-state", ACLMessage.INFORM);
        if (informMsg != null) {
            try {
                SimulationState updatedState = (SimulationState) informMsg.getContentObject();
                ((ParticipantAgent) myAgent).setState(updatedState);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Position determineBestMove(SimulationState state) {
        Map map = state.getMap();
        Position currentPos = state.getPosition();
        Position goal = findClosestItem(map, currentPos);
        List<Position> possibleMoves = getAdjacentPositions(currentPos);

        return selectBestMove(map, possibleMoves, goal);
    }

    private List<Position> getAdjacentPositions(Position pos) {
        List<Position> positions = new ArrayList<>();
        positions.add(new Position(pos.x, pos.y + 1)); // Right
        positions.add(new Position(pos.x - 1, pos.y)); // Up
        positions.add(new Position(pos.x, pos.y - 1)); // Left
        positions.add(new Position(pos.x + 1, pos.y)); // Down
        return positions;
    }

    private Position selectBestMove(Map map, List<Position> moves, Position goal) {
        if (goal == null) return null;

        Position bestMove = null;
        int minDist = Integer.MAX_VALUE;

        for (Position move : moves) {
            if (!map.isTrapPosition(move)) {
                int distance = calculateDistance(move, goal);
                if (distance < minDist) {
                    minDist = distance;
                    bestMove = move;
                }
            }
        }
        return bestMove;
    }

    private Position findClosestItem(Map map, Position current) {
        List<Position> items = map.getItemPositions();
        Position closest = null;
        int minDist = Integer.MAX_VALUE;

        for (Position item : items) {
            int distance = calculateDistance(current, item);
            if (distance < minDist) {
                minDist = distance;
                closest = item;
            }
        }
        return closest;
    }

    private int calculateDistance(Position a, Position b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }
}
