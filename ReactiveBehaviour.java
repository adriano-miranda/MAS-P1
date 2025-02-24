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

    private final int[][] directions = {{0, 1}, {-1, 0}, {0, -1}, {1, 0}};  // Right, Up, Left, Down
    private int directionIndex = 0;
    private final int changeDirectionLimit = 4;
    private int directionChanges = 0;
    private final List<Position> visited = new ArrayList<>();

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
                GenericOperator move = getNextMove(state);

                if (state != null && move != null) {
                    Position prePos = state.getPosition();
                    MapNavigationState posState = (MapNavigationState) move.operate(new MapNavigationState(prePos));
                    reply.setContentObject(posState.position);
                    reply.setPerformative(ACLMessage.PROPOSE);
                }

                myAgent.send(reply);

                MessageTemplate mt2 = MessageTemplate.and(
                        MessageTemplate.MatchConversationId("update-state"),
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                ACLMessage informMsg = myAgent.blockingReceive(mt2, 10000);

                if (informMsg != null) {
                    try {
                        SimulationState updatedState = (SimulationState) informMsg.getContentObject();
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

    private GenericOperator getNextMove(SimulationState state) {
        Map map = state.getMap();
        int numRows = map.getNumRows();
        int numCols = map.getNumCols();
        Position currentPosition = state.getPosition();
        Position nextPosition;
        visited.add(currentPosition);
        int resetFlag = 0;

        while (resetFlag < 2) {
            while (directionChanges < changeDirectionLimit + 1) {
                nextPosition = getNewPosition(numRows, numCols, currentPosition);
                if (!nextPosition.equals(visited.get(visited.size() - 1))) {
                    visited.add(new Position(nextPosition.x, nextPosition.y));
                    directionChanges = 0;
                    return moves[directionIndex];
                } else {
                    directionChanges++;
                }
            }
            resetFlag++;
            visited.clear();
            visited.add(currentPosition);
            directionChanges = 0;
        }
        return null;
    }

    private Position getNewPosition(int maxRows, int maxCols, Position position) {
        int[] direction = directions[directionIndex];
        Position nextPosition = new Position(position.x + direction[0], position.y + direction[1]);

        if (0 <= nextPosition.x && nextPosition.x < maxRows &&
                0 <= nextPosition.y && nextPosition.y < maxCols &&
                !contains(visited, nextPosition)) {
            return nextPosition;
        } else {
            directionIndex = (directionIndex + 1) % 4;
            return position;
        }
    }

    private static boolean contains(List<Position> list, Position candidate) {
        for (Position position : list) {
            if (position.equals(candidate)) {
                return true;
            }
        }
        return false;
    }
}



