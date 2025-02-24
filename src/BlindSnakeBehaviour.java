import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import java.util.List;

public class BlindSnakeBehaviour extends CyclicBehaviour {

    GenericOperator[] moves = {new MoveRightOperator(),
            new MoveUpOperator(), new MoveLeftOperator(),
            new MoveDownOperator()};

    private int[][] directions = {{0, 1}, {-1, 0}, {0, -1}, {1, 0}};  // right, up, left, down
    private int direction_index = 0;
    private int change_direction_limit = 4;
    private int direction_changes = 0;
    private List<Position> visited = new ArrayList<Position>();


    @Override
    public void action() {
        MessageTemplate mt = MessageTemplate.and(
                MessageTemplate.MatchConversationId("request-action"),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));

        ACLMessage msg = myAgent.receive(mt);
        if(msg != null){
            try {
                ACLMessage reply = msg.createReply();
                //This method automatically sets several parameters of the new message based on the received 
                //message, including receiver,language, ontology, protocol, conversation-id, in-reply-to, and reply-with

                //get propose position
                SimulationState ss = ((ParticipantAgent)myAgent).getParticipantState();
                GenericOperator move = getNextMove(ss);

                if (ss!=null) {
                    System.out.println("sending propose operator:" + move.toString());
                    Position pre_pos = ss.getPosition();
                    MapNavigationState pos_state = (MapNavigationState)move.operate(new MapNavigationState(pre_pos));
                    reply.setContentObject(pos_state.position);
                    reply.setPerformative(ACLMessage.PROPOSE);
                }
                myAgent.send(reply);

                MessageTemplate mt2 = MessageTemplate.and(
                        MessageTemplate.MatchConversationId("update-state"),
                        MessageTemplate.MatchPerformative(ACLMessage.INFORM));
                ACLMessage inform_msg = myAgent.blockingReceive(mt2, 10000);
                if (msg != null)
                {
                    try {
                        SimulationState updatedState = (SimulationState)inform_msg.getContentObject();
                        ((ParticipantAgent)myAgent).setState(updatedState);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private GenericOperator getNextMove(SimulationState ss){
        Map map = ss.getMap();
        int numRows = map.getNumRows();
        int numCols = map.getNumCols();
        Position currentPosition = ss.getPosition();
        Position nextPosition = null;
        visited.add(currentPosition);
        int reset_flag = 0;//safe flag to guarantee end of loop when is unable to propose a position


        while (reset_flag<2) {
            //try to move in different 4 directions
            while (direction_changes < change_direction_limit+1) {
                nextPosition = getNewPosition(numRows, numCols, currentPosition);
                if (!nextPosition.equals(visited.get(visited.size() - 1))) {
                    visited.add(new Position(nextPosition.x, nextPosition.y));
                    direction_changes = 0;
                    reset_flag=0;
                    return moves[direction_index];
                } else {
                    direction_changes++;
                }
            }
            reset_flag++;

            //canot move to an unvisited position
            //reset visited positions
            visited.clear();
            visited.add(nextPosition);
            direction_changes = 0;
        }

        return null;
    }

    private Position getNewPosition(int maxRows, int maxCols, Position position) {

        int[] direction = directions[direction_index];
        Position nextPosition = new Position(position.x + direction[0], position.y + direction[1]);

        if (0 <= nextPosition.x && nextPosition.x < maxRows &&
                0 <= nextPosition.y && nextPosition.y < maxCols &&
                !contains(visited, nextPosition)) {
            return nextPosition;
        } else {
            direction_index = (direction_index + 1) % 4;
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