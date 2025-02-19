import jade.core.behaviours.TickerBehaviour;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.core.AID;

public class CheckSimulatorAgentBehaviour extends TickerBehaviour {
    private static final long serialVersionUID = 1L;

    public CheckSimulatorAgentBehaviour(Agent a, long period) {
        super(a, period);
    }

    @Override
    protected void onTick() {
        System.out.println(myAgent.getLocalName() + ": Checking for Simulator Agent in DF...");

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("SimulatorService");
        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(myAgent, template);
            if (result.length > 0) {
                System.out.println(myAgent.getLocalName() + ": Simulator Agent is available.");
                for (DFAgentDescription agentDesc : result) {
                    AID simulatorAgent = agentDesc.getName();
                    System.out.println("Found Simulator Agent: " + simulatorAgent.getLocalName());
                    ((ParticipantAgent) myAgent).setSimulatorFound(true);
                    stop();
                }
            } else {
                System.out.println(myAgent.getLocalName() + ": Simulator Agent NOT found.");
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
}
