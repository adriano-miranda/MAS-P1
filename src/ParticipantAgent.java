import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ParticipantAgent extends Agent {

    private int commitment = 1;
    private boolean registered = false;
    private SimulationState agentState;
    private boolean simulatorFound = false;
    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            this.commitment = (int) args[0];
        } else {
            System.err.println(getAID().getLocalName() + ": Error, argumentos inválidos.");
            doDelete();
            return;
        }

        System.out.println(getAID().getLocalName() + " ha sido iniciado.");
        addBehaviour(new CheckSimulatorAgentBehaviour(this, 2000));
        // Iniciar el comportamiento de registro al simulador

        // Agregar el comportamiento RequestActionBehaviour solo si simulationState está listo

        addBehaviour(new GetInitialStateBehaviour());

    }

    public String getCommitment(){
        return  String.valueOf(commitment);
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

    public SimulationState getParticipantState(){
        return agentState;
    }

    @Override
    protected void takeDown() {
        System.out.println(getAID().getLocalName() + " está siendo cerrado.");
        super.takeDown();
    }
}