import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ParticipantAgent extends Agent {

    private int commitment = 1;
    private boolean registered = false;
    private SimulationState agentState;
    private boolean simulatorFound = false;
    private boolean last_move_valid = true;
    private String strategy = "RANDOM";
    public List<Position> visitedPositions = new ArrayList<Position>(); // Set to track visited positions

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.commitment = (int) args[0];
            this.strategy = (String) args[1];

        } else {
            System.err.println(getAID().getLocalName() + ": Error, argumentos inválidos.");
            doDelete();
            return;
        }

        System.out.println(getAID().getLocalName() + " ha sido iniciado.");
        addBehaviour(new CheckSimulatorAgentBehaviour(this, 2000));


        addBehaviour(new GetInitialStateBehaviour());

    }

    public int getCommitment(){
        return  commitment;
    }



    public void setSimulatorFound(boolean set){
        this.simulatorFound = set;
    }
    public void setRegistered(boolean set){
        registered = set;
    }

    public boolean getRegistered(){
        return registered;
    }
    public void setState(SimulationState simulationState){
        agentState = simulationState;
    }

    public boolean getLastValid(){
        return last_move_valid;
    }

    public String getStrategy(){return strategy;}
    public void setStrategy(String strategy){
        this.strategy = strategy;
    }

    public void setLastValid(boolean valid){
        last_move_valid = valid;
    }
    public SimulationState getParticipantState(){
        return agentState;
    }

    @Override
    protected void takeDown() {
        System.out.println(getAID().getLocalName() + " está siendo cerrado.");
        super.takeDown();
    }
}