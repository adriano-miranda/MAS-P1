import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ParticipantAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println(getAID().getLocalName() + " ha sido iniciado.");

        // Iniciar el comportamiento de registro al simulador
        addBehaviour(new RegisterRequestBehaviour());
    }

    @Override
    protected void takeDown() {
        System.out.println(getAID().getLocalName() + " está siendo cerrado.");
        super.takeDown();
    }
}