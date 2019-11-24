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

/**
 *
 * @author boyko_mihail
 */
public class OrderAgent extends Agent {

    private double number;
    private int countOfNeighbors;
    private String[] neighborsIdentifire;
    private Random random = new Random();
    private double summ = 0;
    private double ratio = 1;

    protected void setup() {

        Object[] args = getArguments();
        if (args != null && args.length > 1) {
            ratio = Double.parseDouble((String) args[0]);
            
            number = Integer.parseInt((String) args[1]);
            System.out.println("Number is " + number);

            countOfNeighbors = Integer.parseInt((String) args[2]);
            System.out.println("countOfNeighbors is " + countOfNeighbors);

            if (args.length < 3 + countOfNeighbors) {
                System.out.println("No neighborsdentifire");
                doDelete();
            }
            neighborsIdentifire = new String[countOfNeighbors];
            for (int i = 0; i < countOfNeighbors; ++i) {
                neighborsIdentifire[i] = (String) args[i + 3];
            }
        } else {
            System.out.println("No number");
            doDelete();
        }

        addBehaviour(new SimpleBehaviour(this) {

            public void action() {
                ACLMessage msgNew = new ACLMessage(ACLMessage.INFORM);
                msgNew.setOntology("MessAboutMean_Number");
                msgNew.setContent((number + random.nextGaussian() * 0.1) + "");
                for (int i = 0; i < neighborsIdentifire.length; i++) {
                    msgNew.addReceiver(new AID(neighborsIdentifire[i] + "@10.42.0.1:1099/JADE"));
//                    System.out.println("MessAboutMean_Gradient send: " + msgNew.getSender().getLocalName() + "->" + getLocalName() + " summ = " + number);
                }
                send(msgNew);

                sendSelfGrad();
            }

            @Override
            public boolean done() {
                return false;
            }

            private void sendSelfGrad() {

                MessageTemplate m1 = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                MessageTemplate m2 = MessageTemplate.MatchOntology("MessAboutMean_Number");
                MessageTemplate m3 = MessageTemplate.and(m1, m2);
                ACLMessage msg = blockingReceive(m3, 1200);
                if (msg != null) {
                    String num = msg.getContent();
                    System.out.println("MessAboutMean_Number give: " + msg.getSender().getLocalName() + "->" + getLocalName() + " summ = " + num);
                    summ += (Double.parseDouble(num) - number);
                    
                    if (Math.log(Math.random()) > Math.log(1.0 - ratio)) {
                        System.out.println("1 of : " + getLocalName() + " number = " + number);
                        number = number + (0.1/countOfNeighbors) * summ ;
                        summ = 0;

                        ACLMessage msgNew = new ACLMessage(ACLMessage.INFORM);
                        msgNew.setOntology("MessAboutMean_Number");
                        msgNew.setContent((number + random.nextGaussian() * 0.1) + "");
//                        msgNew.setContent((number) + "");
                        for (int i = 0; i < neighborsIdentifire.length; i++) {
                            msgNew.addReceiver(new AID(neighborsIdentifire[i] + "@10.42.0.1:1099/JADE"));
//                            System.out.println("MessAboutMean_Gradient send: " + msgNew.getSender().getLocalName() + "->" + getLocalName() + " summ = " + number);
                        }
                        send(msgNew);
                   } else {
                        System.out.println("0 of : " + getLocalName() + " number = " + number );
                    }
                }

            }
        });
    }
}
