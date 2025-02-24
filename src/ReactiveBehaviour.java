import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import java.util.List;
public class ReactiveBehaviour extends CyclicBehaviour {
    private final GenericOperator[] moves = {
            new MoveRightOperator(), new MoveUpOperator(),
            new MoveLeftOperator(), new MoveDownOperator()
    };

    private final int[][] directions = {{0, 1}, {-1, 0}, {0, -1}, {1, 0}};
    private int directionIndex = 0;
    private final int changeDirectionLimit = 4;
    private int directionChanges = 0;
    private final List<Position> visited = new ArrayList<>();

    @Override
    public void action() {
        ACLMessage msg = receiveMessage("request-action", ACLMessage.REQUEST);
        if (msg != null) {
            processRequest(msg);
        }
    }

    private ACLMessage receiveMessage(String conversationId, int performative) {
        MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchConversationId(conversationId),
                MessageTemplate.MatchPerformative(performative)
        );
        return myAgent.receive(mt);
    }

    private void processRequest(ACLMessage msg) {
        try {
            SimulationState state = ((ParticipantAgent) myAgent).getParticipantState();
            GenericOperator move = determineNextMove(state);

            if (state != null && move != null) {
                sendMoveProposal(msg, move);
                updateAgentState();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMoveProposal(ACLMessage msg, GenericOperator move) throws Exception {
        ACLMessage reply = msg.createReply();
        MapNavigationState newState = (MapNavigationState) move.operate(new MapNavigationState(((ParticipantAgent) myAgent).getParticipantState().getPosition()));
        Position nextPosition = newState.position;
        reply.setContentObject(nextPosition);
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

    private GenericOperator determineNextMove(SimulationState state) {
        Map map = state.getMap();
        Position currentPosition = state.getPosition();
        visited.add(currentPosition);

        int attempts = 0;
        while (attempts < 2) {
            for (int i = 0; i < changeDirectionLimit; i++) {
                Position nextPosition = getNewPosition(map, currentPosition);
                if (!visited.contains(nextPosition)) {
                    visited.add(nextPosition);
                    directionChanges = 0;
                    return moves[directionIndex];
                }
                directionChanges++;
            }
            resetVisitedPositions(currentPosition);
            attempts++;
        }
        return null;
    }

    private Position getNewPosition(Map map, Position position) {
        int[] direction = directions[directionIndex];
        Position nextPosition = new Position(position.x + direction[0], position.y + direction[1]);

        directionIndex = (directionIndex + 1) % 4;
        return position;
    }

    private void resetVisitedPositions(Position position) {
        visited.clear();
        visited.add(position);
        directionChanges = 0;
    }
}
