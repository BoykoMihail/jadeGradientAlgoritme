/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jada_second_algoritm_boyko;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author boyko_mihail
 */
public class OrderAgent extends Agent {

    private double number;
    private String[] neighborsIdentifire;
    private Random random = new Random();
    private boolean isSend = false;
    private boolean isUpdate = false;
    private double probabilityOfFailure = 1;
    private double probabilityOfDelay = 1;

    protected void setup() {

        Object[] args = getArguments();
        if (args != null && args.length > 1) {
            probabilityOfFailure = Double.parseDouble((String) args[0]);
            probabilityOfDelay = Double.parseDouble((String) args[1]);
            number = Integer.parseInt((String) args[2]);
            neighborsIdentifire = new String[args.length - 3];
            for (int i = 3; i < args.length; ++i) {
                neighborsIdentifire[i - 3] = (String) args[i];
            }
        } else {
            System.out.println("No number");
            doDelete();
        }

        addBehaviour(new SimpleBehaviour(this) {

            public void action() {
                if (getLocalName().compareTo("8") == 0 && !isSend || isUpdate) {
                    ACLMessage startMsg = new ACLMessage(ACLMessage.INFORM);
                    startMsg.setOntology("Send_N");
                    startMsg.setContent((number + random.nextGaussian() * 0.01) + "");
                    for (int i = 0; i < neighborsIdentifire.length; i++) {
                        startMsg.addReceiver(new AID(neighborsIdentifire[i] + "@10.42.0.1:1099/JADE"));
                    }
                    send(startMsg);
                    isSend = true;
                    isUpdate = false;
                }

                try {
                    sendSelfGrad();
                } catch (InterruptedException ex) {
                    Logger.getLogger(OrderAgent.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public boolean done() {
                return false;
            }

            private void sendSelfGrad() throws InterruptedException {

                MessageTemplate m1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                MessageTemplate m2 = MessageTemplate.MatchOntology("Send_N");
                MessageTemplate m3 = MessageTemplate.and(m1, m2);
                ACLMessage msg = blockingReceive(m3, 1200);
                if (msg != null) {
                    String numberOfMes = msg.getContent();
                    System.out.println("Send_N from " + msg.getSender().getLocalName() + " to " + getLocalName() + " = " + numberOfMes);
                    double grad = (Double.parseDouble(numberOfMes) - number);
                    number = number + (0.01) * grad;
                    isUpdate = true;
                    if (Math.log(Math.random()) > Math.log(1.0 - probabilityOfFailure)) {
                        if (Math.log(Math.random()) > Math.log(1.0 - probabilityOfDelay)) {
                            Random randomNum = new Random();
                            Thread.sleep(0 + randomNum.nextInt(2000));
                        }
                        ACLMessage msgUpdate = new ACLMessage(ACLMessage.INFORM);
                        msgUpdate.setOntology("Send_N");
                        msgUpdate.setContent((number + random.nextGaussian() * 0.01) + "");
                        for (int i = 0; i < neighborsIdentifire.length; i++) {
                            msgUpdate.addReceiver(new AID(neighborsIdentifire[i] + "@10.42.0.1:1099/JADE"));
                        }
                        send(msgUpdate);
                        isUpdate = false;
                    } else {
                        System.out.println(getLocalName() + " not send!");
                    }
                }

            }
        });
    }
}
