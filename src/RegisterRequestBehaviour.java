import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class RegisterRequestBehaviour extends OneShotBehaviour {
    @Override
    public void action() {
        ACLMessage requestMessage = new ACLMessage(ACLMessage.REQUEST);
        requestMessage.setSender(myAgent.getAID());
        requestMessage.setConversationId("join-simulation-request");
        requestMessage.addReceiver(new jade.core.AID("SimulatorAgent", jade.core.AID.ISLOCALNAME));

        int agentCommitment = ((ParticipantAgent) myAgent).getCommitment();
        requestMessage.setContent(String.valueOf(agentCommitment));

        myAgent.send(requestMessage);
        System.out.println(myAgent.getAID().getLocalName() + " has sent a request to join the simulation");

    }
}
