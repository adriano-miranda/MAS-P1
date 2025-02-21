import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.Random;

public class RandomBehaviour extends CyclicBehaviour {

    Random rand = new Random();
    GenericOperator[] moves = {new MoveDownOperator(), new MoveLeftOperator(),
            new MoveRightOperator(), new MoveUpOperator()};

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

                //select random move
                GenericOperator move = moves[rand.nextInt(moves.length)];
                //get propose position
                SimulationState ss = ((ParticipantAgent)myAgent).getParticipantState();
                if (ss!=null) {
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
}